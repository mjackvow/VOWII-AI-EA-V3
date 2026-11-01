export class DerivError extends Error { constructor(message,status=503,definitive=false){super(message);this.status=status;this.definitive=definitive;} }
export const PUBLIC_WS='wss://api.derivws.com/trading/v1/options/ws/public';
const positive=n=>typeof n==='number'&&Number.isFinite(n)&&n>0;
export const timeframes={M5:300,M15:900,M30:1800,H1:3600,H4:14400,D1:86400};
export async function connectDeriv(url,Socket=WebSocket){
 const parsed=new URL(url);if(parsed.protocol!=='wss:'||parsed.hostname!=='api.derivws.com'||!parsed.pathname.startsWith('/trading/v1/options/ws/'))throw new DerivError('Invalid Deriv connection URL.');
 const ws=new Socket(url),pending=new Map();let seq=0;
 const rejectAll=()=>{for(const p of pending.values()){clearTimeout(p.timer);p.reject(new DerivError('Deriv connection closed.'));}pending.clear();};
 ws.addEventListener('message',event=>{let data;try{data=JSON.parse(event.data);}catch{return;}const p=pending.get(data.req_id);if(!p)return;clearTimeout(p.timer);pending.delete(data.req_id);if(data.error){const code=/^[A-Za-z0-9_]{1,60}$/.test(data.error.code||'')?data.error.code:'RequestRejected';p.reject(new DerivError(`Deriv rejected request (${code}).`,409,true));}else p.resolve(data);});
 ws.addEventListener('close',rejectAll);ws.addEventListener('error',rejectAll);
 await new Promise((resolve,reject)=>{const timer=setTimeout(()=>{ws.close();reject(new DerivError('Deriv connection timed out.'));},12000);ws.addEventListener('open',()=>{clearTimeout(timer);resolve();},{once:true});ws.addEventListener('error',()=>{clearTimeout(timer);reject(new DerivError('Deriv connection failed.'));},{once:true});});
 return {request(body){return new Promise((resolve,reject)=>{const req_id=++seq;const timer=setTimeout(()=>{pending.delete(req_id);reject(new DerivError('Deriv response timed out; execution outcome may be unknown.'));},15000);pending.set(req_id,{resolve,reject,timer});try{ws.send(JSON.stringify({...body,req_id}));}catch{clearTimeout(timer);pending.delete(req_id);reject(new DerivError('Deriv send failed.'));}});},close(){rejectAll();ws.close();}};
}
export function createDeriv(env,fetcher=fetch,connect=connectDeriv){
 let symbolCache,accountCache;const histories=new Map();
 async function rest(path,method='GET'){
  const key=env.DERIV_API_KEY||env.DERIV_API_SECRET;if(!key)throw new DerivError('Set DERIV_API_KEY in Supabase secrets.');
  const headers={Authorization:`Bearer ${key}`,'Content-Type':'application/json'};if(env.DERIV_APP_ID)headers['Deriv-App-ID']=env.DERIV_APP_ID;
  let r;try{r=await fetcher('https://api.derivws.com'+path,{method,headers,redirect:'error',signal:AbortSignal.timeout(15000),...(method==='POST'?{body:'{}'}:{})});}catch{throw new DerivError('Deriv account service is unavailable.');}
  if(!r.ok)throw new DerivError(`Deriv account API returned HTTP ${r.status}. ${!env.DERIV_APP_ID?'Set DERIV_APP_ID for a personal access token.':'Check that the token and App ID are valid for the current Deriv Options API with trade scope.'}`);
  const data=await r.json();return data.data;
 }
 async function symbols(){
  if(symbolCache&&symbolCache.until>Date.now())return symbolCache.data;
  const ws=await connect(PUBLIC_WS);try{const r=await ws.request({active_symbols:'brief'});if(!Array.isArray(r.active_symbols))throw new DerivError('Invalid Deriv symbols response.');const data=r.active_symbols.filter(s=>/^\w{2,30}$/.test(s.underlying_symbol)).map(s=>({symbol:s.underlying_symbol,name:s.underlying_symbol_name,market:s.market,submarket:s.submarket,open:s.exchange_is_open===1&&s.is_trading_suspended===0}));symbolCache={until:Date.now()+60000,data};return data;}finally{ws.close();}
 }
 async function snapshot(symbol,timeframe='H1'){
  const granularity=timeframes[timeframe];if(!granularity)throw new DerivError('Unsupported timeframe.',400);
  const meta=(await symbols()).find(s=>s.symbol===symbol);if(!meta)throw new DerivError('Symbol is not in the current Deriv catalog.',400);
  const ws=await connect(PUBLIC_WS);try{
   const cacheKey=symbol+':'+timeframe;let cached=histories.get(cacheKey);
   const history=cached&&cached.until>Date.now()?Promise.resolve(cached.data):ws.request({ticks_history:symbol,end:'latest',count:101,style:'candles',granularity}).then(r=>{if(!Array.isArray(r.candles))throw new DerivError('Deriv candle history unavailable.');histories.set(cacheKey,{until:Date.now()+30000,data:r.candles});if(histories.size>100)histories.delete(histories.keys().next().value);return r.candles;});
   const [tickResponse,raw]=await Promise.all([ws.request({ticks:symbol}),history]);const t=tickResponse.tick;
   if(t?.symbol!==symbol||!positive(t.quote)||!positive(t.epoch))throw new DerivError('Invalid Deriv tick.');
   const candles=raw.filter(c=>c.epoch+granularity<=Math.floor(Date.now()/1000)).slice(-100).map(c=>({time:c.epoch,open:c.open,high:c.high,low:c.low,close:c.close}));
   if(candles.some(c=>!positive(c.time)||![c.open,c.high,c.low,c.close].every(positive)||c.high<Math.max(c.open,c.close)||c.low>Math.min(c.open,c.close)))throw new DerivError('Invalid Deriv candle data.');
   return {...meta,source:'Deriv',price:t.quote,bid:positive(t.bid)?t.bid:null,ask:positive(t.ask)?t.ask:null,timestamp:new Date(t.epoch*1000).toISOString(),stale:Date.now()-t.epoch*1000>15000||t.epoch*1000>Date.now()+2000,timeframe,granularity,candles};
  }finally{ws.close();}
 }
 async function account(force=false){
  if(!force&&accountCache&&accountCache.until>Date.now())return accountCache.data;
  const accounts=await rest('/trading/v1/options/accounts');if(!Array.isArray(accounts))throw new DerivError('Invalid Deriv account response.');
  const real=accounts.filter(a=>a.account_type==='real'&&a.status==='active');
  const selected=env.DERIV_ACCOUNT_ID?real.find(a=>a.account_id===env.DERIV_ACCOUNT_ID):real.length===1?real[0]:null;
  if(!selected)throw new DerivError(real.length>1?'Set DERIV_ACCOUNT_ID to select one of your active real Options accounts.':'No matching active real Deriv Options account.');
  if(!/^[A-Za-z0-9_-]{1,40}$/.test(selected.account_id)||typeof selected.balance!=='number'||!Number.isFinite(selected.balance))throw new DerivError('Invalid Deriv account details.');
  const data={account:selected.account_id,currency:selected.currency,balance:selected.balance,accountMode:'real',connected:true};accountCache={until:Date.now()+15000,data};return data;
 }
 async function session(fn){const a=await account(true);const otp=await rest(`/trading/v1/options/accounts/${encodeURIComponent(a.account)}/otp`,'POST');const url=new URL(otp?.url);if(url.hostname!=='api.derivws.com'||url.protocol!=='wss:'||url.pathname!=='/trading/v1/options/ws/real')throw new DerivError('Deriv did not return a real-account session.');const ws=await connect(url.toString());try{return await fn(ws,a);}finally{ws.close();}}
 return {symbols,snapshot,account,session};
}
export function validateDerivAnalysis(analysis,quote,duration,now=Date.now()){
 if(!analysis||analysis.payload?.quote?.source!=='Deriv'||analysis.payload.product!=='rise_fall')throw new DerivError('Run a fresh Deriv Rise/Fall analysis first.',409);
 const age=now-Date.parse(analysis.created_at);
 if(!Number.isFinite(age)||age>120000||age< -2000)throw new DerivError('Analysis expired. Run analysis again.',409);
 const d=analysis.payload;if(!['BUY','SELL'].includes(d.action))throw new DerivError('WAIT decisions cannot be traded.',409);
 const tickAge=now-Date.parse(quote.timestamp);
 if(analysis.symbol!==quote.symbol||quote.stale||!quote.open||!positive(quote.price)||!positive(d.quote.price)||!Number.isFinite(tickAge)||tickAge>15000||tickAge< -2000)throw new DerivError('Fresh data for the same open market is required.',409);
 if(d.duration!==duration)throw new DerivError('Duration changed. Run analysis for the selected duration.',409);
 if(Math.abs(quote.price-d.quote.price)/d.quote.price>0.002)throw new DerivError('Market moved more than 0.2%. Run analysis again.',409);
 return d.action==='BUY'?'CALL':'PUT';
}
export function validateStake(stake,account,maxStake=10){
 if(!positive(maxStake)||!positive(account.balance)||!positive(stake)||Math.abs(Math.round(stake*100)-stake*100)>1e-8||stake>maxStake||stake>account.balance*0.01)throw new DerivError('Stake must have at most two decimals and be within the server cap and 1% of available balance.',400);
 if(account.accountMode!=='real')throw new DerivError('A real Deriv Options account is required.',409);
}
