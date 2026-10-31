import {readFile} from 'node:fs/promises';
const key=(await readFile('connection-key.txt','utf8')).trim();
const base='https://lpzhommiasicrscndipm.supabase.co/functions/v1/trading-api';
for(const path of ['/health','/models']){const r=await fetch(base+path,{headers:{Authorization:'Bearer '+key}});console.log(JSON.stringify({path,status:r.status,data:await r.json()}));}
