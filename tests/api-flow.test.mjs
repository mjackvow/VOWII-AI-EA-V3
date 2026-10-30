import test from 'node:test';
import assert from 'node:assert/strict';
import {createHandler} from '../supabase/functions/trading-api/core.js';
const uid='11111111-1111-4111-8111-111111111111',aid='22222222-2222-4222-8222-222222222222';
function fixture({providerFails=false}={}){
  const timestamp=new Date().toISOString();
  const quote={symbol:'EURUSD',source:'MT5',account:'123',server:'Broker',bid:1.1,ask:1.1001,timestamp,volumeMin:.01,volumeMax:10,volumeStep:.01,candles:Array.from({length:30},(_,i)=>({time:Math.floor(Date.now()/1000)-(30-i)*3600,open:1.1,high:1.11,low:1.09,close:1.1}))};
  const bridge={user_id:uid,account:'123',server:'Broker',account_mode:'real',last_seen:timestamp};
  let saved=null,orders=[],calls=0;
  const fetcher=async(url,options={})=>{
    const p=new URL(url),body=options.body?JSON.parse(options.body):null;
    let result;
    if(p.pathname==='/rest/v1/workspaces')result=[{id:uid,enabled:true}];
    else if(p.pathname==='/api/v1/models')result={data:[{id:'test/model'}]};
    else if(p.pathname==='/api/v1/chat/completions'){
      calls++;assert.match(body.messages[1].content,/candles/);
      if(providerFails)return new Response('{}',{status:503});
      result={id:'real-provider-id-fixture',choices:[{message:{content:JSON.stringify({action:'BUY',confidence:72,reasoning:'Analysis of the supplied candle sequence.',stopLoss:1.09,takeProfit:1.12})}}]};
    }else if(p.pathname==='/rest/v1/workspace_market_ticks')result=[{payload:quote}];
    else if(p.pathname==='/rest/v1/workspace_bridge_connections')result=[bridge];
    else if(p.pathname==='/rest/v1/rpc/reserve_workspace_analysis')result=true;
    else if(p.pathname==='/rest/v1/workspace_analyses'){
      if(options.method==='POST')saved={...body,id:aid,created_at:timestamp};
      result=saved?[saved]:[];
    }else if(p.pathname==='/rest/v1/workspace_orders'){
      if(options.method==='POST')orders.push({...body,id:'33333333-3333-4333-8333-333333333333',status:'queued'});
      result=orders;
    }else throw Error('Unexpected URL '+url);
    return new Response(JSON.stringify(result));
  };
  const handler=createHandler({SUPABASE_URL:'https://db.test',ALLOWED_USER_IDS:uid,OPENROUTER_API_KEY:'test-only',AI_MODELS:'test/model'},fetcher);
  return {call:async(path,body)=>{const r=await handler(new Request('https://db.test/functions/v1/trading-api'+path,{method:'POST',headers:{Authorization:'Bearer fixture'},body:JSON.stringify(body)}));return {status:r.status,body:await r.json()};},getSaved:()=>saved,getOrders:()=>orders,getCalls:()=>calls};
}
test('authenticated analysis persists actual provider output; repeated order requests return one queued order',async()=>{
  const f=fixture();const a=await f.call('/analyze',{symbol:'EURUSD'});assert.equal(a.status,201);assert.equal(a.body.payload.decisions[0].providerRequestId,'real-provider-id-fixture');assert.equal(f.getCalls(),1);
  const payload={analysisId:aid,idempotencyKey:aid,lots:.01,confirmRealTrade:true};const first=await f.call('/orders',payload),second=await f.call('/orders',payload);
  assert.equal(first.status,202);assert.equal(first.body.status,'queued');assert.equal(second.body.id,first.body.id);assert.equal(f.getOrders().length,1);assert.equal(first.body.broker_result,undefined);
});
test('provider failure produces a recorded WAIT and cannot queue an order',async()=>{
  const f=fixture({providerFails:true});const a=await f.call('/analyze',{symbol:'EURUSD'});assert.equal(a.status,201);assert.equal(a.body.payload.action,'WAIT');assert.equal(a.body.payload.successfulCount,0);assert.equal(a.body.payload.decisions[0].status,'error');
  const order=await f.call('/orders',{analysisId:aid,idempotencyKey:aid,lots:.01,confirmRealTrade:true});assert.equal(order.status,409);assert.equal(f.getOrders().length,0);
});
test('missing user confirmation cannot create an order',async()=>{const f=fixture();const r=await f.call('/orders',{analysisId:aid,idempotencyKey:aid,lots:.01});assert.equal(r.status,400);assert.equal(f.getOrders().length,0);});
