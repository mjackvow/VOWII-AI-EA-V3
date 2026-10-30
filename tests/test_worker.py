import datetime as dt
import importlib.util
import pathlib
import sqlite3
import time
import types
import unittest

spec=importlib.util.spec_from_file_location('worker', pathlib.Path(__file__).parents[1]/'bridge'/'mt5_worker.py')
worker=importlib.util.module_from_spec(spec)
spec.loader.exec_module(worker)

class Terminal:
    ACCOUNT_TRADE_MODE_REAL=2
    ORDER_TYPE_BUY=0
    ORDER_TYPE_SELL=1
    ORDER_FILLING_FOK=0
    ORDER_FILLING_IOC=1
    TRADE_ACTION_DEAL=1
    ORDER_TIME_GTC=0
    TRADE_RETCODE_DONE=10009
    TRADE_RETCODE_DONE_PARTIAL=10010
    TRADE_RETCODE_PLACED=10008
    TRADE_RETCODE_TIMEOUT=10012
    TRADE_RETCODE_CONNECTION=10031
    calls=0
    def symbol_select(self,*args):return True
    def symbol_info(self,*args):return types.SimpleNamespace(volume_min=.01,volume_max=1,volume_step=.01,trade_stops_level=10,point=.01,filling_mode=1)
    def symbol_info_tick(self,*args):return types.SimpleNamespace(bid=100,ask=100.1,time_msc=time.time()*1000)
    def order_calc_profit(self,*args):return -10
    def order_check(self,*args):return types.SimpleNamespace(retcode=0)
    def order_send(self,*args):
        self.calls+=1
        return types.SimpleNamespace(retcode=10009,deal=123,order=456,volume=.01,price=100.1,comment='Filled')

class WorkerTests(unittest.TestCase):
    def setUp(self):
        self.mt=Terminal()
        self.conn=sqlite3.connect(':memory:')
        self.conn.execute('create table receipts(id text primary key,result text,acknowledged integer)')
        self.account=types.SimpleNamespace(login=123,server='Broker',trade_mode=2,trade_allowed=True,trade_expert=True,equity=10000)
        self.order={'id':'test-order','created_at':dt.datetime.now(dt.timezone.utc).isoformat(),'payload':{'account':'123','server':'Broker','symbol':'TEST','action':'BUY','lots':.01,'stopLoss':99,'takeProfit':103,'referencePrice':100.1}}
    def tearDown(self):self.conn.close()
    def test_duplicate_order_is_sent_only_once(self):
        for _ in range(2):self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'filled')
        self.assertEqual(self.mt.calls,1)
    def test_restart_after_send_never_resends(self):
        self.conn.execute('insert into receipts values(?,null,0)',('test-order',))
        self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'unknown')
        self.assertEqual(self.mt.calls,0)
    def test_demo_account_is_rejected_before_send(self):
        self.account.trade_mode=0
        self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'rejected')
        self.assertEqual(self.mt.calls,0)
    def test_risk_limit_is_enforced(self):
        self.account.equity=100
        self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'rejected')
        self.assertEqual(self.mt.calls,0)
    def test_no_broker_response_is_unknown(self):
        self.mt.order_send=lambda req:None
        self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'unknown')
    def test_stale_order_is_rejected(self):
        self.order['created_at']=(dt.datetime.now(dt.timezone.utc)-dt.timedelta(seconds=31)).isoformat()
        self.assertEqual(worker.execute_once(self.mt,self.conn,self.order,self.account,.5)['status'],'rejected')
        self.assertEqual(self.mt.calls,0)

if __name__=='__main__':unittest.main()
