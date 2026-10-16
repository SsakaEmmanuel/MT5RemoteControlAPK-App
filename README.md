# ScalpingBot Android app — shared account + inactive payments

This Android WebView shell opens the same published ScalpingBot website:
https://ssakaemmanuel.github.io/ScalpingBot/

## Shared login, registration and verification
The app uses the same website and existing Supabase authentication project as the website. This means users should register only once and can use the same email/password account on both platforms. A login session is stored separately on each platform, so signing in on the website does not automatically sign the user in inside the app.

Email verification currently uses the website's configured Supabase redirect:
https://ssakaemmanuel.github.io/ScalpingBot/
Test the full flow in the APK: register, open verification email, verify, return to the app, and sign in. Do not assume verification is complete until the Supabase user shows as confirmed.

## Payments — deliberately inactive
Payments are NOT implemented or enabled. The feature flag in `product-config/payment-feature.json` is `enabled: false`. This package contains planning notes only; it does not collect money, show an active checkout, create payment links, or mark accounts as paid.

Before launch, choose a payment provider and decide plans/prices, refund rules, renewal/cancellation rules, and what access each plan unlocks. Then implement payment verification server-side (never trust a browser/app-only "paid" flag), test it, and only enable the feature when the product is ready to be published.

## Build APK using GitHub Actions
1. Create a GitHub repository named `MT5RemoteControlAPK`.
2. Extract this ZIP and upload its contents to the repository root, not the ZIP itself.
3. Open **Actions**. The build workflow runs on pushes to `main` or `master`; you can also run **Build Android APK** manually.
4. Open the successful workflow run and download `MT5RemoteControlAPK-debug-apk` from Artifacts.
5. Extract that artifact and install the debug APK on your Android phone.

## Security and trading status
- This starter does not place trades or directly connect to MT5.
- Keep trading disabled until a secure backend/MT5 bridge, server-enforced risk limits, and demo testing are complete.
- Never commit Supabase service-role keys, database passwords, broker credentials, MT5 passwords, or payment-provider secrets.
