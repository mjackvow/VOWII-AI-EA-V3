// Only fixed, documented provider origins may receive server-side credentials.
const PROVIDERS = {
  openrouter: {label:'OpenRouter', base:'https://openrouter.ai/api/v1'},
  unorouter: {label:'UnoRouter', base:'https://api.unorouter.com/v1'},
  huggingface: {label:'Hugging Face', base:'https://router.huggingface.co/v1'},
  nvidia: {label:'NVIDIA', base:'https://integrate.api.nvidia.com/v1'}
};
export function parseModels(raw='') {
  let input=raw.trim();
  if(input.startsWith('{')&&input.endsWith('}')) input=input.slice(1,-1).trim();
  if(!input) return [];
  const entries=input.split(',').map(s=>s.trim()).filter(Boolean);
  if(entries.length>12) throw Error('AI_MODELS supports at most 12 entries; no entries were discarded.');
  const seen=new Set();
  return entries.map(entry=>{
    const match=entry.match(/^([A-Za-z0-9._:/-]+)(?:\s*\(([^()]+)\))?$/);
    if(!match) throw Error('Invalid AI_MODELS entry. Use model/id(Provider), separated by commas.');
    const model=match[1],tag=match[2]?.toLowerCase().replace(/[\s_-]/g,'');
    // This exact free Qwen ID is published by UnoRouter, not OpenRouter.
    const provider=tag|| (model==='alibaba/qwen3.6-35b-a3b:free'?'unorouter':'openrouter');
    if(!PROVIDERS[provider]) throw Error('Unknown AI provider annotation. Use OpenRouter, UnoRouter, HuggingFace or Nvidia.');
    const secretName=provider==='nvidia' ? (model.startsWith('mistralai/')?'NVIDIA_MISTRAL_API_KEY':model.startsWith('deepseek-ai/')?'NVIDIA_DEEPSEEK_API_KEY':'NVIDIA_NEMOTRON_API_KEY') : {openrouter:'OPENROUTER_API_KEY',unorouter:'UNOROUTER_API_KEY',huggingface:'HUGGING_FACE_API_KEY'}[provider];
    return {model,provider,providerLabel:PROVIDERS[provider].label,secretName};
  }).filter(m=>{const k=m.provider+':'+m.model;if(seen.has(k))return false;seen.add(k);return true;});
}
export function configuration(env) {
  try {const models=parseModels(env.AI_MODELS);return {models:models.map(m=>({...m,keyConfigured:Boolean(env[m.secretName]?.trim())})),error:models.length?null:'AI_MODELS is empty.'};}
  catch(error){return {models:[],error:error.message};}
}
export function parseOutput(content) {
  if(typeof content!=='string')throw Error('Provider returned no text output.');
  const text=content.trim().replace(/^```(?:json)?\s*/i,'').replace(/\s*```$/,'');
  try{return JSON.parse(text);}catch{throw Error('Provider output is not valid JSON.');}
}
export function createModelRouter(env,fetcher=fetch) {
  const cache=new Map();
  async function request(url,key,body,timeout=15000) {
    let response;
    try {response=await fetcher(url,{method:body?'POST':'GET',redirect:'error',headers:{Authorization:`Bearer ${key}`,...(body?{'Content-Type':'application/json'}:{})},body:body?JSON.stringify(body):undefined,signal:AbortSignal.timeout(timeout)});}
    catch(error){throw Error(error.name==='TimeoutError'||error.name==='AbortError'?'Provider request timed out.':'Provider connection failed.');}
    if(!response.ok)throw Error(`Provider returned HTTP ${response.status}${response.status===401?' (credential rejected)':response.status===402?' (billing or credit required)':response.status===429?' (rate limited)':''}.`);
    try{return await response.json();}catch{throw Error('Provider returned an invalid JSON response.');}
  }
  async function resolve(m) {
    if(!env[m.secretName]?.trim())throw Error(`Missing ${m.secretName}.`);
    const key=m.provider+':'+m.secretName;
    let entry=cache.get(key);
    if(!entry||entry.expires<Date.now()) {
      const promise=request(PROVIDERS[m.provider].base+'/models',env[m.secretName].trim()).then(x=>{if(!Array.isArray(x.data))throw Error('Provider model catalog is invalid.');return x.data;});
      entry={promise,expires:Date.now()+60000};cache.set(key,entry);
      promise.catch(()=>cache.delete(key));
    }
    const models=await entry.promise;
    let found=models.find(x=>x.id===m.model);
    // UnoRouter also exposes some vendor-qualified website IDs as bare API IDs.
    if(!found&&m.provider==='unorouter')found=models.find(x=>x.id===m.model.slice(m.model.indexOf('/')+1));
    if(!found)throw Error('Requested model is not listed by this provider. No substitute was used.');
    return found;
  }
  async function catalog() {
    const c=configuration(env);
    const models=await Promise.all(c.models.map(async m=>{try{const resolved=await resolve(m);return {...m,status:'available',resolvedModel:resolved.id,imageInput:resolved.architecture?.input_modalities?.includes('image')??null};}catch(e){return {...m,status:'unavailable',error:e.message};}}));
    return {models,error:c.error,checkedAt:new Date().toISOString()};
  }
  async function complete(m,messages,{image=false,diagnostic=false}={}) {
    const started=Date.now();
    const resolved=await resolve(m);
    const modalities=resolved.architecture?.input_modalities;
    if(image&&Array.isArray(modalities)&&!modalities.includes('image'))throw Error('This model does not support chart images. Use symbol scan.');
    const body={model:resolved.id,messages,max_tokens:diagnostic?1024:2048,temperature:0.1,stream:false};
    if(['openrouter','unorouter'].includes(m.provider))body.response_format={type:'json_object'};
    const result=await request(PROVIDERS[m.provider].base+'/chat/completions',env[m.secretName].trim(),body,85000);
    if(result.choices?.[0]?.finish_reason==='length')throw Error('Provider output was truncated; no decision accepted.');
    const value=parseOutput(result.choices?.[0]?.message?.content);
    return {value,model:m.model,provider:m.provider,providerLabel:m.providerLabel,resolvedModel:resolved.id,returnedModel:result.model||null,providerRequestId:result.id||null,latencyMs:Date.now()-started};
  }
  return {catalog,complete};
}
