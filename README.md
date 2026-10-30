# VOWII AI EA V3.5

Original Scanner, Live Signal, Archives and MT5 Bridge layout, connected to a real backend. Login and signup are removed. Simulated prices, seeded signals, fake broker tickets and fallback model votes are removed. Android opens the same web interface rather than maintaining a second simulated engine.

## Setup

Frontend: https://vowii-ai-ea-v3-5.vercel.app/
Backend: https://lpzhommiasicrscndipm.supabase.co/functions/v1/trading-api

1. In Supabase project VOWII-AI-EA-V3.5, Edge Functions → Secrets, add OPENROUTER_API_KEY privately. Add a second secret named AI_MODELS containing comma-separated OpenRouter model IDs, without quotes. The catalog is validated on each analysis. Up to three models are supported. For example: `google/gemini-3.8-flash,deepseek/deepseek-v4.1-flash` (catalog verified during this release). Image scanning requires every configured model to support image input; symbol scanning does not.
2. In MT5 Bridge, paste the private app connection key and press Save & Verify. The provisioned key is in the local, gitignored connection-key.txt file, never deployed or committed. It stays in sessionStorage for this browser tab. There are no user accounts or signup screens. Keep this key private: it authorizes access to your workspace. Clear Connection removes it from the tab.
3. Press Pair Windows Worker. Copy the separately generated worker token into BRIDGE_TOKEN on Windows. The original blank MQL5 timer has been replaced with bridge/mt5_worker.py, an actual MT5 Python worker. No broker password is sent to the website.
4. Install the pinned package in bridge/requirements.txt using Windows Python 3.12. Configure process environment variables using bridge/.env.example as a reference; the script does not load .env files automatically. Set MT5_ACCOUNT, MT5_SERVER and exact broker MT5_SYMBOLS. Use the logged-in real MT5 terminal with external Python trading enabled. ENABLE_LIVE_TRADING=true is required to run the worker. Start `python bridge/mt5_worker.py`.
5. After real broker quotes appear, run analysis. Trade submission requires a fresh BUY/SELL decision, lot size, the real-trade checkbox, and confirmation of the specific order. No trades are placed during tests.

Optionally configure MASSIVE_API_KEY on Supabase for FX reference quotes. Connected chat market-data plugins do not automatically provide runtime API credentials or subscriptions to the deployed app. Earlier Git commits exposed provider credentials; revoke those with their providers and use fresh secrets.

## Data and execution

Coinbase BTC-USD and ETH-USD reference quotes stream over WebSocket and refresh through the backend. They cannot authorize MT5 orders. The worker sends real broker ticks every three seconds, and the frontend polls every five seconds. This is sampled live data, not lossless tick history. Quotes older than 15 seconds are marked stale and cannot authorize an order. Only H1 closed candles are currently supported; other original timeframe controls explain that limitation.

AI receives broker bid/ask and up to 100 actual H1 candles, plus the optional chart image. Each configured model is called. Provider IDs, responses, errors and latency are saved. Failures or direction disagreement produce WAIT. Unanimous valid responses use the first model's protective levels and minimum reported confidence. Confidence is subjective, not calibrated. No news, order-book depth, COT or invented indicator values are shown. Analyses are automatically archived in Supabase.

A signal can produce at most one queued order. The worker checks real account identity, permissions, timestamps, lot limits, stops, price movement, projected stop-loss exposure and broker order_check before order_send. Default projected loss cap is 0.5% of equity, configurable up to 1%. Maximum application order size is one lot. Keep bridge-state.sqlite on persistent storage and run one worker per credential. Durable receipts are written before sending; ambiguous results become UNKNOWN and are never automatically resent. A fill needs an actual broker deal ID, volume and price. There is no portfolio-wide daily drawdown circuit breaker; slippage can exceed estimated stop loss.

## Database and files

The additive supabase/workspaces.sql creates service-only workspace tables with RLS and separate atomic claim/rate-limit functions. Connection and worker credentials are stored only as SHA-256 hashes. The handler checks workspace authorization on private routes; public health and exchange quotes require no key. verify_jwt=false permits this custom authentication. Service-role credentials never reach the frontend.

The earlier auth tables and policies remain untouched; the new application does not use them. supabase/schema.sql records the original bootstrap. Do not rerun either schema blindly against an existing project. release_files stores private, versioned source snapshots. Private app-files and release-artifacts Storage buckets also exist. Application source is executed from Edge Functions/Vercel, not directly from database rows.

## Development

```
npm ci
npm test
python -m unittest discover -s tests -p test_worker.py
npm run build
npm start
```

Tailwind and Lucide are pinned and bundled locally. The build rejects known exposed-key patterns and simulation code. Public configuration contains only the Supabase URL and publishable key. Backend source: supabase/functions/trading-api. Frontend: web. Android: app; SDK compilation and device checks remain outstanding. See VERIFICATION.md for completed checks and remaining live setup.
