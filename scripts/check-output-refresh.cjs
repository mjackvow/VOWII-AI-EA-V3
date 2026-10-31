const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'C:/Users/Jekie/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const assert=require('assert/strict');
(async()=>{
 const browser=await chromium.launch({headless:true,channel:'msedge'});
 try{
 const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>sessionStorage.setItem('vowii-key','isolated-browser-fixture'));
 let revision=1;
 const quote=symbol=>({symbol,source:'MT5',price:1.1,bid:1.1,ask:1.1001,tradable:true,stale:false,timestamp:new Date().toISOString()});
 await page.route('**/functions/v1/trading-api/**',route=>{
 const path=new URL(route.request().url()).pathname.split('trading-api')[1];let data={};
 if(path==='/health')data={modelCount:6,aiConfigured:true};
 if(path==='/market')data={quotes:[quote('EURUSD'),quote('GBPUSD')]};
 if(path==='/status')data={bridge:{connected:true,account:'test-account',server:'Test'}};
 if(path==='/orders')data=[];
 if(path==='/models')data={models:[{model:'model/test',provider:'unorouter',providerLabel:'UnoRouter',status:'available'}],checks:[{model:'model/test',provider:'unorouter',status:'error',error:'Provider returned HTTP 403.'}]};
 if(path==='/analyses')data=[{id:String(revision),symbol:'EURUSD',created_at:new Date().toISOString(),payload:{action:'WAIT',confidence:0,quote:quote('EURUSD'),modelCount:6,successfulCount:3,reasoning:'Backend output revision '+revision,decisions:[{model:'model/test',providerLabel:'UnoRouter',status:'error',error:'Provider returned HTTP 403.'}]}}];
 return route.fulfill({json:data});
 });
 await page.goto('http://127.0.0.1:3000/');
 await page.waitForFunction(()=>document.getElementById('models-consensus-text').textContent.includes('revision 1'));
 assert.match(await page.locator('#model-health').innerText(),/HTTP 403/);
 revision=2;
 await page.waitForFunction(()=>document.getElementById('models-consensus-text').textContent.includes('revision 2'),{timeout:15000});
 await page.locator('#models-market-select').selectOption('GBPUSD');
 assert.match(await page.locator('#models-consensus-text').innerText(),/No analysis/);
 await page.locator('#models-market-select').selectOption('EURUSD');
 assert.match(await page.locator('#models-consensus-text').innerText(),/revision 2/);
 await page.reload();await page.waitForFunction(()=>document.getElementById('models-consensus-text').textContent.includes('revision 2'));
 assert.deepEqual(errors,[]);console.log('PASS: persisted outputs load, refresh automatically, clear on symbol change and restore on reload; provider errors remain visible.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
