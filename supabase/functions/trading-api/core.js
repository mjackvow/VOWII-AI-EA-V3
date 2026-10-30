export class ApiError extends Error { constructor(status, message) { super(message); this.status=status; } }
const fail=(status,message)=>{throw new ApiError(status,message);};
const positive=n=>typeof n==='number' && Number.isFinite(n) && n>0;
const uuid=s=>typeof s==='string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(s);
const symbolOK=s=>typeof s==='string' && /^[A-Za-z0-9._-]{1,24}$/.test(s);
export function fresh(timestamp, maxAge=15000, now=Date.now()) {
  const age=now-Date.parse(timestamp); return Number.isFinite(age) && age>=-2000 && age<=maxAge;
}
export function validateDecision(value, quote) {
  if (!value || !['BUY','SELL','WAIT'].includes(value.action) || !Number.isInteger(value.confidence) || value.confidence<0 || value.confidence>100 || typeof value.reasoning!=='string' || value.reasoning.length<10 || value.reasoning.length>6000) fail(502,'AI returned an invalid decision.');
  if (value.action!=='WAIT') {
    const entry=value.action==='BUY'?quote.ask:quote.bid;
    if (!positive(value.stopLoss)||!positive(value.takeProfit)) fail(502,'AI returned invalid protective levels.');
    if (value.action==='BUY' ? !(value.stopLoss<entry && value.takeProfit>entry) : !(value.stopLoss>entry && value.takeProfit<entry)) fail(502,'AI protective levels are on the wrong side of the current quote.');
  }
  return {action:value.action,confidence:value.confidence,reasoning:value.reasoning,stopLoss:value.action==='WAIT'?null:value.stopLoss,takeProfit:value.action==='WAIT'?null:value.takeProfit};
}
export function validateOrder(analysis, tick, bridge, lots, now=Date.now()) {
  if (!fresh(analysis.created_at,60000,now)) fail(409,'Analysis expired. Run analysis again.');
  if (!fresh(tick?.timestamp,15000,now)) fail(409,'Broker quote is stale or unavailable.');
  if (!fresh(bridge?.last_seen,15000,now)||bridge.account_mode!=='real') fail(409,'A connected real MT5 account is required.');
  const d=analysis.payload;
  if (!['BUY','SELL'].includes(d.action)) fail(409,'WAIT decisions cannot be traded.');
  if (d.quote?.source!=='MT5' || d.account!==bridge.account || d.server!==bridge.server) fail(409,'Analysis does not match the connected broker account.');
  if (!positive(lots)||lots>1 || !positive(tick.volumeMin)||!positive(tick.volumeMax)||!positive(tick.volumeStep) || lots<tick.volumeMin || lots>tick.volumeMax || Math.abs(lots/tick.volumeStep-Math.round(lots/tick.volumeStep))>1e-6) fail(400,'Invalid lot size. Maximum application size is 1 lot.');
  validateDecision(d,tick);
  const old=d.action==='BUY'?d.quote.ask:d.quote.bid, current=d.action==='BUY'?tick.ask:tick.bid;
  if (Math.abs(current-old)/old>0.002) fail(409,'Price moved more than 0.2%. Run analysis again.');
  return {symbol:analysis.symbol,action:d.action,lots,stopLoss:d.stopLoss,takeProfit:d.takeProfit,referencePrice:current,account:bridge.account,server:bridge.server,maxDeviationPct:0.2};
}
async function digest(text) {return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(text)))).map(x=>x.toString(16).padStart(2,'0')).join('');}
export function createHandler(env, fetcher=fetch) {
  const base=env.SUPABASE_URL;
  async function jsonFetch(url, options={}, label='Upstream service') {
    const response=await fetcher(url,{...options,signal:AbortSignal.timeout(45000)});
    if (!response.ok) fail(response.status===429?429:502,`${label} returned HTTP ${response.status}.`);
    if (response.status===204) return null;
    const text=await response.text(); return text?JSON.parse(text):null;
  }
  async function db(path,method='GET',body,prefer='return=representation') {
    path=path.replace(/^(bridge_connections|market_ticks|analyses|orders|request_limits)(?=\?|$)/,'workspace_$1').replace('rpc/claim_order','rpc/claim_workspace_order').replace('rpc/reserve_analysis','rpc/reserve_workspace_analysis');
    return jsonFetch(`${base}/rest/v1/${path}`,{method,headers:{apikey:env.SUPABASE_SERVICE_ROLE_KEY,Authorization:`Bearer ${env.SUPABASE_SERVICE_ROLE_KEY}`,'Content-Type':'application/json',Prefer:prefer},body:body===undefined?undefined:JSON.stringify(body)},'Database');
  }
  async function identity(req,worker=false) {
    const token=(req.headers.get('Authorization')||'').replace(/^Bearer /,'');
    if (!token) fail(401,'Add your app connection key in MT5 Bridge settings.');
    if(worker) {
      const rows=await db(`bridge_connections?token_hash=eq.${await digest(token)}&select=*`);
      if (!rows.length) fail(401,'Invalid bridge credential.');
      return {id:rows[0].user_id,bridge:rows[0]};
    }
    const rows=await db(`workspaces?access_key_hash=eq.${await digest(token)}&enabled=eq.true&select=id,enabled`);
    if(!rows.length) fail(401,'Invalid app connection key.');
    return rows[0];
  }
  async function enabled(user) {
    const rows=await db(`workspaces?id=eq.${user.id}&enabled=eq.true&select=id`);
    if(!rows.length) fail(403,'Workspace disabled.');
  }
  async function bridgeFor(user) {return (await db(`bridge_connections?user_id=eq.${user.id}&select=*`))[0]||null;}
  async function market(user) {
    const rows=user?await db(`market_ticks?user_id=eq.${user.id}&select=payload`):[];
    const ticks=rows.map(r=>({...r.payload,stale:!fresh(r.payload.timestamp)}));
    const results=await Promise.allSettled(['BTC-USD','ETH-USD'].map(async symbol=>{
      const t=await jsonFetch(`https://api.exchange.coinbase.com/products/${symbol}/ticker`,{},'Coinbase');
      const bid=Number(t.bid),ask=Number(t.ask),price=Number(t.price);
      if(!positive(bid)||!positive(ask)||!positive(price)||bid>ask||!Number.isFinite(Date.parse(t.time))) fail(502,'Invalid Coinbase quote.');
      return {symbol,price,bid,ask,timestamp:t.time,source:'Coinbase Exchange',stale:!fresh(t.time),tradable:false};
    }));
    const errors=[]; results.forEach(r=>r.status==='fulfilled'?ticks.push(r.value):errors.push(r.reason.message));
    if(env.MASSIVE_API_KEY && user) {
      const fx=await Promise.allSettled([['EUR','USD'],['GBP','USD'],['USD','JPY']].map(async ([from,to])=>{
        const data=await jsonFetch(`https://api.massive.com/v1/last_quote/currencies/${from}/${to}`,{headers:{Authorization:`Bearer ${env.MASSIVE_API_KEY}`}},'Massive');
        const q=data.last;
        if(!q||!positive(q.bid)||!positive(q.ask)||q.bid>q.ask||!positive(q.timestamp)) fail(502,'Invalid Massive quote.');
        const timestamp=new Date(q.timestamp).toISOString();
        return {symbol:`${from}/${to}`,price:(q.bid+q.ask)/2,bid:q.bid,ask:q.ask,timestamp,source:'Massive',stale:!fresh(timestamp),tradable:false};
      }));
      fx.forEach(r=>r.status==='fulfilled'?ticks.push(r.value):errors.push(r.reason.message));
    }
    return {quotes:ticks,errors,receivedAt:new Date().toISOString()};
  }
  return async function handle(req) {
    const headers={'Content-Type':'application/json','Cache-Control':'no-store','Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, apikey, content-type','Access-Control-Allow-Methods':'GET, POST, OPTIONS'};
    const response=(data,status=200)=>new Response(JSON.stringify(data),{status,headers});
    if(req.method==='OPTIONS') return new Response(null,{status:204,headers});
    try {
      const path=new URL(req.url).pathname.replace(/^.*\/trading-api/,'').replace(/\/$/,'')||'/';
      if(path==='/health' && req.method==='GET') return response({version:'3.5.0',backend:'online',aiConfigured:Boolean(env.OPENROUTER_API_KEY&&env.AI_MODELS),keyConfigured:Boolean(env.OPENROUTER_API_KEY),modelsConfigured:Boolean(env.AI_MODELS),accessMode:'workspace-key'});
      if(path==='/market' && req.method==='GET' && !req.headers.get('Authorization')) return response(await market(null));
      const worker=path.startsWith('/worker/'); const user=await identity(req,worker);
      await enabled(user);
      let body={};
      if(req.method==='POST') {
        const raw=await req.text(); if(raw.length>400000) fail(413,'Request too large.');
        try {body=JSON.parse(raw);} catch {fail(400,'Invalid JSON.');}
      }
      if(path==='/market' && req.method==='GET') return response(await market(user));
      if(path==='/status' && req.method==='GET') {
        const b=await bridgeFor(user);
        return response({userId:user.id,enabled:true,bridge:b?{account:b.account,server:b.server,accountMode:b.account_mode,connected:fresh(b.last_seen),lastSeen:b.last_seen,telemetry:b.telemetry}:null});
      }
      if(path==='/analyses' && req.method==='GET') return response(await db(`analyses?user_id=eq.${user.id}&order=created_at.desc&limit=50`));
      if(path==='/orders' && req.method==='GET') return response(await db(`orders?user_id=eq.${user.id}&order=created_at.desc&limit=50`));
      if(path==='/bridge/register' && req.method==='POST') {
        await enabled(user);
        const existing=await bridgeFor(user);
        if(existing && fresh(existing.last_seen)) fail(409,'Stop the existing worker before replacing its credential.');
        const token=crypto.randomUUID()+crypto.randomUUID();
        await db('bridge_connections?on_conflict=user_id','POST',{user_id:user.id,token_hash:await digest(token),account:null,server:null,account_mode:null,last_seen:null,telemetry:null},'resolution=merge-duplicates,return=representation');
        await db(`market_ticks?user_id=eq.${user.id}`,'DELETE');
        return response({token,message:'Store in the Windows worker BRIDGE_TOKEN environment variable. It is shown only once.'});
      }
      if(path==='/worker/heartbeat' && req.method==='POST') {
        if(typeof body.account!=='string'||!/^\d{1,20}$/.test(body.account)||typeof body.server!=='string'||body.server.length>100||body.accountMode!=='real'||!Array.isArray(body.quotes)||body.quotes.length>30) fail(400,'Invalid real-account heartbeat.');
        if (user.bridge.account && (user.bridge.account!==body.account || user.bridge.server!==body.server)) fail(409,'Bridge account changed. Register a new bridge credential.');
        const quotes=body.quotes.map(q=>{
          if(!symbolOK(q.symbol)||!positive(q.bid)||!positive(q.ask)||q.bid>q.ask||!fresh(q.timestamp,86400000)||!positive(q.volumeMin)||!positive(q.volumeMax)||!positive(q.volumeStep)) fail(400,'Invalid broker tick.');
          const candles=Array.isArray(q.candles)?q.candles.slice(-100):[];
          if(candles.some(c=>!Number.isFinite(c.time)||!positive(c.open)||!positive(c.close)||!positive(c.high)||!positive(c.low)||c.high<Math.max(c.open,c.close)||c.low>Math.min(c.open,c.close))) fail(400,'Invalid broker candles.');
          return {user_id:user.id,symbol:q.symbol,payload:{symbol:q.symbol,bid:q.bid,ask:q.ask,price:(q.bid+q.ask)/2,timestamp:q.timestamp,source:'MT5',tradable:true,account:body.account,server:body.server,volumeMin:q.volumeMin,volumeMax:q.volumeMax,volumeStep:q.volumeStep,candles,timeframe:'H1'},updated_at:new Date().toISOString()};
        });
        const telemetry={};for(const k of ['balance','equity','freeMargin']) {if(typeof body.telemetry?.[k]!=='number'||!Number.isFinite(body.telemetry[k])) fail(400,'Invalid account telemetry.');telemetry[k]=body.telemetry[k];}
        if(quotes.length) await db('market_ticks?on_conflict=user_id,symbol','POST',quotes,'resolution=merge-duplicates,return=representation');
        await db(`bridge_connections?user_id=eq.${user.id}`,'PATCH',{account:body.account,server:body.server,account_mode:'real',telemetry,last_seen:new Date().toISOString()});
        return response({accepted:quotes.length});
      }
      if(path==='/worker/claim' && req.method==='POST') {
        if(!fresh(user.bridge.last_seen)||user.bridge.account_mode!=='real') fail(409,'Fresh real-account heartbeat required.');
        return response(await db('rpc/claim_order','POST',{owner_id:user.id}));
      }
      if(path==='/worker/result' && req.method==='POST') {
        if(!uuid(body.id)||!['filled','partially_filled','rejected','unknown'].includes(body.status)) fail(400,'Invalid execution result.');
        if(['filled','partially_filled'].includes(body.status) && (!body.result?.deal || !positive(body.result?.volume) || !positive(body.result?.price))) fail(400,'A broker deal, volume and price are required to report a fill.');
        const rows=await db(`orders?id=eq.${body.id}&user_id=eq.${user.id}&status=in.(claimed,unknown)`,'PATCH',{status:body.status,broker_result:body.result||{}});
        return response({updated:rows.length});
      }
      if(path==='/analyze' && req.method==='POST') {
        await enabled(user); if(!symbolOK(body.symbol)) fail(400,'Invalid symbol.');
        if(!env.OPENROUTER_API_KEY||!env.AI_MODELS) fail(503,'Configure OPENROUTER_API_KEY and AI_MODELS on the backend.');
        if(body.image!==undefined && (typeof body.image!=='string'||body.image.length>300000||!/^data:image\/(jpeg|png|webp);base64,[A-Za-z0-9+/=]+$/.test(body.image))) fail(400,'Invalid chart image.');
        const rows=await db(`market_ticks?user_id=eq.${user.id}&symbol=eq.${encodeURIComponent(body.symbol)}&select=payload`);
        const quote=rows[0]?.payload, bridge=await bridgeFor(user);
        if(!quote || !fresh(quote.timestamp) || !fresh(bridge?.last_seen)) fail(409,'Fresh MT5 data is required for broker analysis.');
        if(quote.account!==bridge.account || quote.server!==bridge.server) fail(409,'Quote account does not match the connected terminal.');
        if(!Array.isArray(quote.candles)||quote.candles.length<30) fail(409,'At least 30 real H1 candles are required.');
        if(!fresh(new Date(quote.candles.at(-1).time*1000).toISOString(),7200000)) fail(409,'Broker candle history is stale.');
        const models=[...new Set(env.AI_MODELS.split(',').map(s=>s.trim()).filter(Boolean))].slice(0,3);
        if(!models.length) fail(503,'No AI models configured.');
        const catalog=await jsonFetch('https://openrouter.ai/api/v1/models',{},'AI model catalog');
        if(models.some(m=>!catalog.data?.some(item=>item.id===m))) fail(503,'A configured AI model is not available in the provider catalog.');
        if(body.image && models.some(m=>!catalog.data.find(x=>x.id===m)?.architecture?.input_modalities?.includes('image'))) fail(400,'Chart images require every configured AI_MODELS entry to support image input. Use symbol scan or configure vision models.');
        if(!await db('rpc/reserve_analysis','POST',{owner_id:user.id})) fail(429,'Allow 60 seconds between AI requests.');
        const prompt='Analyze only the supplied broker quote and H1 candles. No news or other indicators are supplied. Return JSON with action BUY, SELL or WAIT; confidence integer 0..100 (subjective, not a calibrated probability); reasoning string; stopLoss and takeProfit positive numbers for BUY/SELL, null for WAIT. Do not claim execution, certainty or unavailable data. Prefer WAIT when evidence is insufficient.';
        const results=await Promise.allSettled(models.map(async model=>{
          const started=Date.now();
          const answer=await jsonFetch('https://openrouter.ai/api/v1/chat/completions',{method:'POST',headers:{Authorization:`Bearer ${env.OPENROUTER_API_KEY}`,'Content-Type':'application/json'},body:JSON.stringify({model,messages:[{role:'system',content:prompt},{role:'user',content:body.image?[{type:'text',text:JSON.stringify(quote)},{type:'image_url',image_url:{url:body.image}}]:JSON.stringify(quote)}],response_format:{type:'json_object'},max_tokens:1200,temperature:0.1})},'AI provider');
          let value;try{value=JSON.parse(answer.choices?.[0]?.message?.content);}catch{fail(502,'AI response was not valid JSON.');}
          return {...validateDecision(value,quote),model,providerRequestId:answer.id,latencyMs:Date.now()-started};
        }));
        const decisions=results.map((r,i)=>r.status==='fulfilled'?{...r.value,status:'ok'}:{model:models[i],status:'error',error:r.reason.message});
        const successful=decisions.filter(d=>d.status==='ok');
        const aligned=successful.length===models.length && successful.every(d=>d.action===successful[0].action);
        const choice=aligned?successful[0]:{action:'WAIT',confidence:0,stopLoss:null,takeProfit:null,reasoning:'Models disagreed or a provider failed. No executable signal.'};
        const payload={...choice,confidence:aligned?Math.min(...successful.map(d=>d.confidence)):0,decisions,quote,account:bridge.account,server:bridge.server,timeframe:'H1',imageAnalyzed:Boolean(body.image),modelCount:models.length,successfulCount:successful.length};
        return response((await db('analyses','POST',{user_id:user.id,symbol:body.symbol,payload}))[0],201);
      }
      if(path==='/orders' && req.method==='POST') {
        await enabled(user);
        if(!uuid(body.analysisId)||!uuid(body.idempotencyKey)||body.confirmRealTrade!==true) fail(400,'Analysis, idempotency key and real-trade confirmation are required.');
        const existing=await db(`orders?user_id=eq.${user.id}&analysis_id=eq.${body.analysisId}`); if(existing.length) return response(existing[0]);
        const analysis=(await db(`analyses?id=eq.${body.analysisId}&user_id=eq.${user.id}`))[0]; if(!analysis) fail(404,'Analysis not found.');
        const tick=(await db(`market_ticks?user_id=eq.${user.id}&symbol=eq.${encodeURIComponent(analysis.symbol)}&select=payload`))[0]?.payload;
        const payload=validateOrder(analysis,tick,await bridgeFor(user),body.lots);
        const order=await db('orders?on_conflict=user_id,analysis_id','POST',{user_id:user.id,analysis_id:analysis.id,idempotency_key:body.idempotencyKey,payload},'resolution=ignore-duplicates,return=representation');
        return response(order[0]||(await db(`orders?user_id=eq.${user.id}&analysis_id=eq.${analysis.id}`))[0],202);
      }
      fail(404,'Endpoint not found.');
    } catch(error) {
      return response({error:error instanceof ApiError?error.message:'Service unavailable. No success or execution has been assumed.'},error instanceof ApiError?error.status:503);
    }
  };
}
