# VOWII AI EA V3.5.3

The original Scanner, Live Signal and Archives interface now uses Deriv market data and a Deriv Trading panel. Login/signup and the MT5 bridge have been removed.

Frontend: https://vowii-ai-ea-v3-5.vercel.app/
Backend: https://lpzhommiasicrscndipm.supabase.co/functions/v1/trading-api

## Configuration

Configure provider credentials and AI_MODELS privately in Supabase Edge Function secrets. Optional provider annotations (Huggingface), (UnoRouter), (Nvidia), and (OpenRouter) route each model to its own provider. All six configured models are retained. API credentials never enter frontend files.

For execution, set DERIV_API_KEY to a current Deriv Options personal access token with trading access and DERIV_APP_ID to your registered app ID. Legacy MT5 credentials are not used. If multiple active real Options accounts exist, set DERIV_ACCOUNT_ID. Set DERIV_WORKSPACE_ID to the owning workspace when multiple workspaces are enabled. DERIV_MAX_STAKE defaults to 10 account-currency units; every purchase is additionally capped at 1% of the current available balance. No portfolio-wide daily loss limit is implemented.

Save your private app connection key in Deriv Trading. It authorizes the workspace without a login/signup flow, remains in sessionStorage for the current tab, and can be cleared. The local connection-key.txt is gitignored and must never be published. Broker credentials remain server-side.

## Markets, analysis and orders

The dropdown loads the current Deriv symbol catalog, grouped by market. Prices stream through Deriv's public WebSocket, with backend snapshots refreshed every five seconds. Timeframes M5, M15, M30, H1, H4 and D1 request up to 100 real closed candles. Stale or closed markets cannot authorize analysis or purchases. Closed markets remain identified in the catalog.

Each analysis sends the selected symbol, spot, timestamp, selected-timeframe candles and selected forecast duration to every configured AI model. Optional chart images supplement the same market data. Results and provider failures are persisted and refreshed on the frontend. All models must return valid, aligned directions; failure or disagreement produces WAIT. Confidence is subjective, not a probability of profit.

Rise/Fall uses CALL for BUY, PUT for SELL, and no purchase for WAIT. A fresh analysis can create one quote. Review the account, stake, duration, maximum price and payout before checking the real-money confirmation and approving the purchase. Backend requests a fresh proposal on the purchase connection and rejects worse terms, changed accounts, stale signals or excessive price movement. The database atomically claims the order before sending a buy. Unknown results are never automatically resent; inspect Deriv history before taking further action. A purchase may lose the entire stake.

Public prices do not require an account connection. A configured secret alone does not prove account access: the Deriv Trading panel displays the actual account API result. Contract availability is validated for that account and symbol at quote time.

## Development and verification

Run npm ci, npm test, npm run build, then npm start. Tests cover provider routing, selected-market prompt data, fail-closed consensus, quote expiry, stake limits, explicit confirmation, concurrent purchase prevention and ambiguous broker responses. Broker purchase tests use fixtures; no real purchases are made during verification. Browser scripts support a locally installed Playwright module via PLAYWRIGHT_MODULE.

Supabase stores analyses and contract receipts, plus versioned source snapshots in release_files. Edge Functions execute the backend code; stored source files are not executable database programs. Historical MT5 database tables are retained, but their API routes and worker source are retired.

## Current active model set

Production currently uses only nvidia/nemotron-3.5-bolt-e4b(Nvidia), verified with actual BTC/USD analysis. AI_ACTIVE_MODELS can explicitly override this selection using the same comma-separated syntax. AI_MODELS retains the previous candidate list, but is not the active production selection. Do not add candidates until actual market-analysis responses have been tested. Any failure in the active set still yields WAIT; failed votes are never silently omitted. With one model, the result is a single-model opinion, not independent consensus.

The RUN MODELS ANALYSIS button directs an unconnected user to workspace settings, and displays readiness or the blocking reason. Open local connection-key.txt and paste its contents into the private workspace key field, then Save & Verify. This is independent of the Deriv API credential. A numeric-string Deriv balance is normalized and validated; zero balance permits analysis and account reads but blocks purchase quotes.
