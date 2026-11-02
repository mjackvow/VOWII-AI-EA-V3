import test from 'node:test';
import assert from 'node:assert/strict';
import {parseModels,configuration,createModelRouter,parseOutput} from '../supabase/functions/trading-api/models.js';
const list='{zai-org/GLM-5.3-Flash(Huggingface),nvidia/nemotron-3-ultra-550b-a55b:free(UnoRouter),alibaba/qwen3.6-35b-a3b:free,deepseek/deepseek-v4.1-flash(UnoRouter),mistralai/mistral-large-4-1t-a52b(Nvidia),nvidia/nemotron-3.5-bolt-e4b(Nvidia)}';
test('all six annotated entries route to the requested providers and family-specific secrets',()=>{
 const m=parseModels(list);assert.equal(m.length,6);assert.deepEqual(m.map(x=>x.provider),['huggingface','unorouter','unorouter','unorouter','nvidia','nvidia']);assert.equal(m[4].secretName,'NVIDIA_MISTRAL_API_KEY');assert.equal(m[5].secretName,'NVIDIA_NEMOTRON_API_KEY');assert.equal(parseModels('deepseek-ai/deepseek-v4.1-flash(Nvidia)')[0].secretName,'NVIDIA_DEEPSEEK_API_KEY');
});
test('unsupported annotations and excess entries fail instead of truncating or falling back',()=>{assert.throws(()=>parseModels('model/id(Unknown)'));assert.throws(()=>parseModels(Array(13).fill('model/id').join(',')));assert.ok(configuration({AI_MODELS:'bad model'}).error);});
test('provider credentials are sent only to matching fixed origins',async()=>{
 const seen=[];const env={AI_MODELS:list,HUGGING_FACE_API_KEY:'hf-key',UNOROUTER_API_KEY:'uno-key',NVIDIA_MISTRAL_API_KEY:'mistral-key',NVIDIA_NEMOTRON_API_KEY:'nemo-key'};
 const router=createModelRouter(env,async(url,o)=>{seen.push({url,key:o.headers.Authorization});if(url.endsWith('/models'))return Response.json({data:parseModels(list).map(x=>({id:x.model}))});return Response.json({id:'response-1',choices:[{message:{content:'{"status":"ok"}'}}]});});
 for(const m of parseModels(list))await router.complete(m,[]);
 for(const {url,key} of seen){if(url.includes('huggingface'))assert.equal(key,'Bearer hf-key');else if(url.includes('unorouter'))assert.equal(key,'Bearer uno-key');else{assert.ok(url.startsWith('https://integrate.api.nvidia.com/'));assert.ok(['Bearer mistral-key','Bearer nemo-key'].includes(key));}}
});
test('free models cannot silently become paid and missing keys make no network requests',async()=>{
 let calls=0;const router=createModelRouter({UNOROUTER_API_KEY:'private'},async()=>{calls++;return Response.json({data:[{id:'nvidia/nemotron-3-ultra-550b-a55b'}]});});
 await assert.rejects(router.complete(parseModels(list)[1],[]),/No substitute/);
 await assert.rejects(router.complete(parseModels(list)[0],[]),/Missing HUGGING_FACE/);assert.equal(calls,1);
});
test('catalogs may list the same UnoRouter model by bare API slug',async()=>{const m=parseModels(list)[2];const router=createModelRouter({UNOROUTER_API_KEY:'key'},async(url,o)=>url.endsWith('/models')?Response.json({data:[{id:'qwen3.6-35b-a3b:free'}]}):Response.json({choices:[{message:{content:'{"status":"ok"}'}}]}));const result=await router.complete(m,[]);assert.equal(result.resolvedModel,'qwen3.6-35b-a3b:free');});
test('provider rejects, invalid JSON and truncated outputs cannot become votes',async()=>{assert.throws(()=>parseOutput('pretend success'));assert.deepEqual(parseOutput('```json\n{"ok":true}\n```'),{ok:true});const router=createModelRouter({OPENROUTER_API_KEY:'secret'},async(url)=>url.endsWith('/models')?Response.json({data:[{id:'test/model'}]}):Response.json({choices:[{finish_reason:'length',message:{content:'{"action":"BUY"}'}}]}));await assert.rejects(router.complete(parseModels('test/model')[0],[]),/truncated/);});
