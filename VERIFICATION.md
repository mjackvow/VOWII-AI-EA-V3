# Verification — V3.5.2

- npm test: 20 passing tests, including provider routing, selected-market data, WAIT on provider failure, explicit purchase confirmation, concurrent purchase prevention, stale quotes, stake caps and ambiguous outcomes.
- npm run build: passed.
- Browser: original four-tab layout, no login/signup, 89 market options, live Deriv tick and 100 candles, no JavaScript errors, no mobile horizontal overflow.
- Browser fixture: saved outputs refresh automatically, clear on market change and restore on reload. Provider errors remain visible.
- Deployed Edge Function version 18: public health, markets and selected-market snapshot verified.
- Real analysis of 1HZ100V/M5/5-minute forecast persisted all six results. NVIDIA Bolt returned a valid WAIT with reasoning tied to the supplied candles. Hugging Face returned 402, DeepSeek returned 403, Mistral timed out, and two UnoRouter responses were truncated. Consensus correctly remained WAIT; these provider issues are unresolved.
- Deriv account access: API key exists; DERIV_APP_ID missing and account endpoint returns 401. Live execution cannot be verified until current account credentials and registered app ID work.
- Execution tests used mocks only. No real purchases were submitted.
- Old MT5 runtime routes and worker files removed. Historical database tables preserved.
