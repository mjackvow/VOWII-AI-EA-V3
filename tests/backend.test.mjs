import test from 'node:test';
import assert from 'node:assert/strict';
import {fresh,validateDecision,validateOrder,createHandler} from '../supabase/functions/trading-api/core.js';
const now=Date.now(), time=new Date(now).toISOString();
const quote={bid:100,ask:100.1,timestamp:time,volumeMin:0.01,volumeMax:10,volumeStep:0.01,source:'MT5'};
const decision={action:'BUY',confidence:70,reasoning:'Trend evidence in the supplied candles.',stopLoss:99,takeProfit:103};
const bridge={last_seen:time,account_mode:'real',account:'123',server:'Broker'};
const analysis={symbol:'TEST',created_at:time,payload:{...decision,quote,account:'123',server:'Broker'}};
test('stale, invalid and future timestamps are rejected',()=>{assert.equal(fresh(time,15000,now),true);for(const t of ['bad',new Date(now-16000).toISOString(),new Date(now+3000).toISOString()])assert.equal(fresh(t,15000,now),false);});
test('AI malformed decisions and invalid stops fail closed',()=>{for(const d of [{...decision,action:'STRONG BUY'},{...decision,confidence:101},{...decision,confidence:'70'},{...decision,stopLoss:101},{...decision,takeProfit:NaN}])assert.throws(()=>validateDecision(d,quote));assert.equal(validateDecision({...decision,action:'WAIT'},quote).stopLoss,null);});
test('fresh real-account order is validated against broker limits',()=>assert.equal(validateOrder(analysis,quote,bridge,0.01,now).lots,0.01));
test('orders reject missing ticks, old signals, wrong accounts, WAIT and bad lots',()=>{
  for(const args of [[analysis,null,bridge,0.01],[{...analysis,created_at:new Date(now-61000).toISOString()},quote,bridge,0.01],[analysis,quote,{...bridge,account_mode:'demo'},0.01],[analysis,quote,{...bridge,account:'456'},0.01],[{...analysis,payload:{...analysis.payload,action:'WAIT'}},quote,bridge,0.01],[analysis,quote,bridge,0.015],[analysis,quote,bridge,Infinity],[analysis,quote,bridge,1.01]])assert.throws(()=>validateOrder(...args,now));
});
test('adverse price movement blocks order',()=>assert.throws(()=>validateOrder(analysis,{...quote,bid:101,ask:101.1},bridge,0.01,now)));
test('health is public, account routes require authentication',async()=>{const handler=createHandler({},()=>{throw Error('Must not call upstream');});const health=await handler(new Request('https://example.test/trading-api/health'));assert.equal(health.status,200);for(const path of ['/status','/orders','/worker/claim','/models','/ai/check'])assert.equal((await handler(new Request('https://example.test/trading-api'+path))).status,401);});
test('invalid connection key cannot read orders',async()=>{let calls=0;const handler=createHandler({SUPABASE_URL:'https://db.test'},async()=>{calls++;return new Response('[]');});const r=await handler(new Request('https://example.test/trading-api/orders',{headers:{Authorization:'Bearer bad'}}));assert.equal(r.status,401);assert.equal(calls,1);});
test('disabled workspace cannot call AI',async()=>{let calls=0;const handler=createHandler({SUPABASE_URL:'https://db.test'},async()=>new Response(JSON.stringify(++calls===1?[{id:'11111111-1111-4111-8111-111111111111'}]:[])));const r=await handler(new Request('https://example.test/trading-api/analyze',{method:'POST',headers:{Authorization:'Bearer test'},body:'{}'}));assert.equal(r.status,403);});

test('worker request without verified hash is denied',async()=>{const handler=createHandler({SUPABASE_URL:'https://db.test'},async()=>new Response('[]'));const r=await handler(new Request('https://example.test/trading-api/worker/claim',{method:'POST',headers:{Authorization:'Bearer bad'},body:'{}'}));assert.equal(r.status,401);});
