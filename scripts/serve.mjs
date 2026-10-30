import http from 'node:http';
import { readFile } from 'node:fs/promises';
const files = {'/':'index.html','/index.html':'index.html','/app.js':'app.js','/style.css':'style.css','/config.js':'config.js','/lucide.js':'lucide.js','/mt5_worker.py':'mt5_worker.py'};
http.createServer(async (req,res) => {
  const file=files[new URL(req.url,'http://localhost').pathname];
  if (!file) {res.writeHead(404);res.end();return;}
  try { const body=await readFile(`public/${file}`);res.setHeader('Content-Type',file.endsWith('.js')?'text/javascript':file.endsWith('.css')?'text/css':'text/html');res.end(body); }
  catch {res.writeHead(500);res.end('Run npm run build first.');}
}).listen(3000,'127.0.0.1',()=>console.log('V3.5: http://127.0.0.1:3000'));
