import {createDeriv} from '../supabase/functions/trading-api/deriv.js';
const api=createDeriv({});const symbols=await api.symbols();console.log(JSON.stringify({count:symbols.length,markets:[...new Set(symbols.map(s=>s.market))]}));
for(const symbol of symbols.filter(s=>s.open).slice(0,2)){const q=await api.snapshot(symbol.symbol,'M5');console.log(JSON.stringify({symbol:q.symbol,price:q.price,candles:q.candles.length,last:q.candles.at(-1),stale:q.stale}));}
