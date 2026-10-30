import {mkdir,copyFile,readFile} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
execFileSync(process.execPath,['node_modules/tailwindcss/lib/cli.js','-i','web/base.css','-o','web/style.css','--minify'],{stdio:'inherit'});
await mkdir('public',{recursive:true});
for(const name of ['index.html','app.js','style.css','config.js']){
 const text=await readFile(`web/${name}`,'utf8');
 if(/sk-or-v1-|nvapi-|Math\.random|paper-api\.alpaca|TradingPass2026/.test(text))throw Error(`Unsafe legacy content in ${name}`);
 await copyFile(`web/${name}`,`public/${name}`);
}
await copyFile('node_modules/lucide/dist/umd/lucide.js','public/lucide.js');
await copyFile('bridge/mt5_worker.py','public/mt5_worker.py');
console.log('Built V3.5 with original interface and live backend.');
