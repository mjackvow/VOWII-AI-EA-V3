# Verification — V3.5.3 (2026-11-01)

- 21 backend tests passed; production frontend build passed.
- Real Deriv account authentication and read-only portfolio succeed. Numeric-string balance parsing was corrected; the connected real USD account has zero available balance. No purchase was attempted.
- BTC/USD (cryBTCUSD) returns fresh ticks and 100 actual H1 candles. Catalog contains 89 symbols.
- Real browser workflow passed: missing workspace key directs to connection settings; Save & Verify succeeds; RUN MODELS ANALYSIS submits BTC/USD to the backend; NVIDIA Nemotron Bolt returns valid analysis; saved reasoning and WAIT signal appear in the frontend. Provider latency approximately 2.8 seconds. No JavaScript errors.
- Browser regression checks passed: four tabs, mobile widths, no login/signup, active model status, saved output refresh, symbol changes and reload restoration.
- Production is explicitly limited to one verified model. Previously failing candidates remain excluded unless AI_ACTIVE_MODELS is deliberately changed. Single-model results are identified in the UI. An active-model failure still returns WAIT.
- Network failures, unreadable responses, expired sessions, malformed balances, stale data, duplicate purchase calls, missing confirmation, expired quotes and unknown broker outcomes have explicit handling or regression coverage.
- Supabase advisors: private backend-only tables intentionally have RLS with no client policies. Supabase Auth leaked-password protection remains disabled; this app does not use Supabase password login. Reference: https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection
- No promise of zero future errors or guaranteed trades: provider/network availability and broker contract availability remain external dependencies. Real purchase execution has not been tested, and zero balance prevents it.
