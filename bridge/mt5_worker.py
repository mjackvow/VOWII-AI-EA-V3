"""Real MT5 bridge. Run on Windows beside a logged-in terminal; never fabricates fills."""
import datetime as dt
import json
import math
import os
import sqlite3
import time
import urllib.error
import urllib.request


def fresh(timestamp, seconds=15):
    age = time.time() - dt.datetime.fromisoformat(timestamp.replace('Z', '+00:00')).timestamp()
    return -2 <= age <= seconds


def build_request(mt5, order, account, max_risk_pct):
    p = order['payload']
    if not fresh(order['created_at'], 30):
        raise ValueError('Order expired before broker execution')
    if str(account.login) != p['account'] or account.server != p['server']:
        raise ValueError('Broker account does not match the order')
    if account.trade_mode != mt5.ACCOUNT_TRADE_MODE_REAL or not account.trade_allowed or not account.trade_expert:
        raise ValueError('A real account with automated trading enabled is required')
    if not mt5.symbol_select(p['symbol'], True):
        raise ValueError('Broker symbol unavailable')
    info = mt5.symbol_info(p['symbol'])
    tick = mt5.symbol_info_tick(p['symbol'])
    if info is None or tick is None or not (-2 <= time.time() - tick.time_msc / 1000 <= 15):
        raise ValueError('Broker tick unavailable or stale')
    if p['action'] not in ('BUY', 'SELL'):
        raise ValueError('Only BUY or SELL is executable')
    buy = p['action'] == 'BUY'
    price = tick.ask if buy else tick.bid
    lots, sl, tp = p['lots'], p['stopLoss'], p['takeProfit']
    if not all(isinstance(x, (int, float)) and math.isfinite(x) and x > 0 for x in (lots, sl, tp, price, p['referencePrice'])):
        raise ValueError('Invalid order values')
    if not info.volume_min <= lots <= min(info.volume_max, 1.0) or abs(lots / info.volume_step - round(lots / info.volume_step)) > 1e-6:
        raise ValueError('Lot size outside broker or application limits')
    if abs(price - p['referencePrice']) / p['referencePrice'] > 0.002:
        raise ValueError('Market moved beyond allowed deviation')
    distance = info.trade_stops_level * info.point
    # Broker validates stops against the closing side of the spread.
    if buy and not (sl < tick.bid - distance and tp > tick.bid + distance):
        raise ValueError('Buy protective levels invalid at broker price')
    if not buy and not (sl > tick.ask + distance and tp < tick.ask - distance):
        raise ValueError('Sell protective levels invalid at broker price')
    order_type = mt5.ORDER_TYPE_BUY if buy else mt5.ORDER_TYPE_SELL
    loss = mt5.order_calc_profit(order_type, p['symbol'], lots, price, sl)
    if loss is None or not math.isfinite(loss) or loss >= 0 or account.equity <= 0 or abs(loss) > account.equity * max_risk_pct / 100:
        raise ValueError('Stop-loss exposure exceeds configured equity risk limit')
    if info.filling_mode & 1:
        filling = mt5.ORDER_FILLING_FOK
    elif info.filling_mode & 2:
        filling = mt5.ORDER_FILLING_IOC
    else:
        raise ValueError('Broker supports neither FOK nor IOC for this symbol')
    return {'action': mt5.TRADE_ACTION_DEAL, 'symbol': p['symbol'], 'volume': lots,
            'type': order_type, 'price': price, 'sl': sl, 'tp': tp, 'deviation': 10,
            'magic': 350035, 'comment': 'V35-' + order['id'][:20],
            'type_time': mt5.ORDER_TIME_GTC, 'type_filling': filling}


def execute_once(mt5, conn, order, account, max_risk_pct):
    existing = conn.execute('select result from receipts where id=?', (order['id'],)).fetchone()
    if existing:
        return json.loads(existing[0]) if existing[0] else {'id': order['id'], 'status': 'unknown', 'result': {'message': 'Worker interrupted after claiming. Inspect broker history; never resend automatically.'}}
    # Commit before sending: a crash at any subsequent point cannot trigger another send.
    conn.execute('insert into receipts(id,result,acknowledged) values(?,null,0)', (order['id'],))
    conn.commit()
    sent = False
    try:
        request = build_request(mt5, order, account, max_risk_pct)
        check = mt5.order_check(request)
        if check is None or check.retcode != 0:
            raise ValueError('Broker preflight rejected: ' + (check.comment if check else 'no response'))
        sent = True
        result = mt5.order_send(request)
        if result is None:
            receipt = {'id': order['id'], 'status': 'unknown', 'result': {'message': 'Broker returned no response. Check terminal history.'}}
        else:
            status = 'unknown'
            if result.retcode == mt5.TRADE_RETCODE_DONE and result.deal and result.volume > 0:
                status = 'filled'
            elif result.retcode == mt5.TRADE_RETCODE_DONE_PARTIAL and result.deal and result.volume > 0:
                status = 'partially_filled'
            elif result.retcode not in (mt5.TRADE_RETCODE_PLACED, mt5.TRADE_RETCODE_TIMEOUT, mt5.TRADE_RETCODE_CONNECTION, mt5.TRADE_RETCODE_DONE, mt5.TRADE_RETCODE_DONE_PARTIAL):
                status = 'rejected'
            receipt = {'id': order['id'], 'status': status, 'result': {'deal': str(result.deal), 'order': str(result.order), 'volume': result.volume, 'price': result.price, 'retcode': result.retcode, 'message': result.comment}}
    except Exception as exc:
        receipt = {'id': order['id'], 'status': 'unknown' if sent else 'rejected', 'result': {'message': str(exc)}}
    conn.execute('update receipts set result=? where id=?', (json.dumps(receipt), order['id']))
    conn.commit()
    return receipt


def main():
    import MetaTrader5 as mt5
    api = os.environ['BACKEND_URL'].rstrip('/')
    if not api.startswith('https://'):
        raise ValueError('HTTPS backend required')
    token = os.environ['BRIDGE_TOKEN']
    expected_account = os.environ['MT5_ACCOUNT']
    expected_server = os.environ['MT5_SERVER']
    symbols = [s.strip() for s in os.environ.get('MT5_SYMBOLS', 'XAUUSD,EURUSD,GBPUSD,USDJPY').split(',') if s.strip()]
    max_risk = float(os.environ.get('MAX_RISK_PCT', '0.5'))
    if not math.isfinite(max_risk) or not 0 < max_risk <= 1:
        raise ValueError('MAX_RISK_PCT must be greater than 0 and at most 1')
    if os.environ.get('ENABLE_LIVE_TRADING') != 'true':
        raise ValueError('Set ENABLE_LIVE_TRADING=true to explicitly enable the real-account worker')
    def call(path, body):
        req = urllib.request.Request(api + path, data=json.dumps(body).encode(), headers={'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'}, method='POST')
        with urllib.request.urlopen(req, timeout=20) as response:
            return json.load(response)
    if not mt5.initialize():
        raise RuntimeError('Cannot connect to the local MT5 terminal')
    conn = sqlite3.connect(os.environ.get('BRIDGE_STATE_PATH', 'bridge-state.sqlite'))
    conn.execute('create table if not exists receipts(id text primary key,result text,acknowledged integer not null default 0)')
    candles_cache, candles_at = {}, 0
    try:
        while True:
            try:
                account = mt5.account_info()
                if account is None or str(account.login) != expected_account or account.server != expected_server or account.trade_mode != mt5.ACCOUNT_TRADE_MODE_REAL:
                    raise ValueError('Expected real MT5 account is not connected')
                quotes = []
                reload_candles = time.time() - candles_at > 60
                for symbol in symbols:
                    if not mt5.symbol_select(symbol, True):
                        continue
                    info, tick = mt5.symbol_info(symbol), mt5.symbol_info_tick(symbol)
                    if info is None or tick is None or tick.bid <= 0 or tick.ask < tick.bid or time.time() - tick.time > 86400:
                        continue
                    if reload_candles:
                        rates = mt5.copy_rates_from_pos(symbol, mt5.TIMEFRAME_H1, 1, 100)
                        candles_cache[symbol] = [] if rates is None else [{'time': int(c['time']), 'open': float(c['open']), 'high': float(c['high']), 'low': float(c['low']), 'close': float(c['close']), 'volume': int(c['tick_volume'])} for c in rates]
                    quotes.append({'symbol': symbol, 'bid': tick.bid, 'ask': tick.ask, 'timestamp': dt.datetime.fromtimestamp(tick.time_msc / 1000, dt.timezone.utc).isoformat(), 'volumeMin': info.volume_min, 'volumeMax': info.volume_max, 'volumeStep': info.volume_step, 'candles': candles_cache.get(symbol, [])})
                if reload_candles:
                    candles_at = time.time()
                call('/worker/heartbeat', {'account': str(account.login), 'server': account.server, 'accountMode': 'real', 'telemetry': {'balance': account.balance, 'equity': account.equity, 'freeMargin': account.margin_free}, 'quotes': quotes})
                # Retry acknowledgements, never order_send. Preserve receipts across restarts.
                for oid, raw in conn.execute('select id,result from receipts where acknowledged=0').fetchall():
                    receipt = json.loads(raw) if raw else {'id': oid, 'status': 'unknown', 'result': {'message': 'Worker restarted during execution. Check broker history.'}}
                    ack = call('/worker/result', receipt)
                    if ack.get('updated'):
                        conn.execute('update receipts set acknowledged=1 where id=?', (oid,))
                        conn.commit()
                for order in call('/worker/claim', {}):
                    receipt = execute_once(mt5, conn, order, account, max_risk)
                    ack = call('/worker/result', receipt)
                    if ack.get('updated'):
                        conn.execute('update receipts set acknowledged=1 where id=?', (order['id'],))
                        conn.commit()
                    print('Order', order['id'], receipt['status'], flush=True)
            except Exception as exc:
                print('Bridge unavailable:', type(exc).__name__, str(exc), flush=True)
            time.sleep(3)
    finally:
        conn.close()
        mt5.shutdown()


if __name__ == '__main__':
    main()
