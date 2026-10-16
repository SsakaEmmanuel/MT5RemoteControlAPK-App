# Payment feature plan (inactive)

Status: **OFF — not ready for launch**.

The app and website will eventually share the same account identity. Paid access should be attached to that Supabase user ID, not to a separate app-only account.

Suggested future flow:
1. User chooses a plan on the website/app.
2. A trusted server creates a checkout session with the selected payment provider.
3. Provider sends a signed webhook to a server-side endpoint.
4. Server validates the webhook and records payment/subscription status.
5. Server-side authorization checks subscription status before unlocking paid features.
6. Admin can review payment state, refunds, cancellations, and support issues.

Rules:
- Do not store card details in the app or database.
- Never mark a user as paid just because they return to a success URL.
- Never place payment secrets in client-side JavaScript, an APK, or GitHub.
- Keep the feature flag disabled until provider choice, pricing, legal terms, webhook verification, access control, and end-to-end tests are complete.
- Payment and trading permissions are separate. A paid subscription must never automatically enable live trading.
