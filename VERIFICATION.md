# V3.5 verification

- Node tests: 12 passed. Validated stale/future quotes, malformed AI output, wrong account, invalid volume, WAIT, explicit confirmation, private workspace access and single-order behavior. Provider tests use fixtures; they are not evidence of a live paid AI response.
- Python worker tests: 6 passed, covering broker execution validation and durable receipt behavior. No actual orders were sent.
- Production build: passed with pinned Tailwind/Lucide dependencies.
- Browser: original four tabs checked in Edge at desktop and 390px mobile; no page JavaScript errors or horizontal overflow. Live Coinbase quotes appeared. No login/signup forms exist. Private connection key was accepted; absent MT5 worker correctly reports DISCONNECTED and disables analysis.
- Database: additive workspace tables created with RLS and no anon/authenticated access. Existing auth tables and policies were preserved. Source snapshots use the backend-only release_files table.

Original V3.5 limitation (model configuration is now present): a real Windows MT5 worker is required to verify actual provider analysis and broker execution end to end. No live trade, provider quality, profitability or perfect operation is claimed. Android SDK build/device verification is outstanding.


## V3.5.1

All six model IDs resolved in their assigned provider catalogs with keys configured. Real connection checks on 2026-10-30 succeeded for UnoRouter Nemotron Ultra, UnoRouter Qwen and NVIDIA Nemotron Bolt. Hugging Face GLM returned HTTP 402 (billing/credit), UnoRouter DeepSeek HTTP 403, and NVIDIA Mistral timed out twice, including an 85-second attempt. These recorded results are not market analyses or trading signals.

The Node suite now has 20 tests, including six-model consensus, partial failure, provider credential isolation, free-model preservation and invalid outputs. The Python suite has six worker tests. A browser fixture verifies persisted output load, automatic refresh, symbol changes and reload. No production fake quotes or real orders are created by these tests. Actual broker connection, trade fills and Android SDK/device checks remain unverified.
