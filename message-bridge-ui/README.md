# message-bridge-ui

A one-page UI for testing the message-bridge API (the Spring Boot app in the parent folder): pick a channel, enter a recipient and a message, and send. Built with React,
Vite, Tailwind CSS v4 and [coss ui](https://coss.com/ui) components.

## Run locally

```bash
cd message-bridge-ui
pnpm install
pnpm dev
```

Open http://localhost:5173. The page calls its own `/v1/...` path, and the Vite dev
server forwards it to `https://message-bridge.onrender.com`, so the browser never makes
a cross-site call and CORS never applies. To call a local backend directly instead:

```bash
VITE_API_URL=http://localhost:8080 pnpm dev
```

## Checks

```bash
pnpm lint      # oxlint
pnpm build     # typecheck, then production build into dist/
```

## Deploy to Vercel

1. In Vercel, choose **Add New > Project** and import the `message-bridge` repository.
2. Set **Root Directory** to `message-bridge-ui`. Vercel then detects Vite: build
   command `pnpm build`, output directory `dist`.
3. Deploy.

`vercel.json` forwards `/v1/...` to the Render API, so the deployed page also calls its
own origin and needs no CORS setup. If the API moves, change the URL there and in
`vite.config.ts`.

## Notes

- On a Twilio trial account the message must be a template name such as
  `sms_2fa` or `sms_internal_alerts`, and the number must be verified in Twilio.
- Render's free plan sleeps when idle; the first request can take about a minute.
- Components in `src/components/ui` come from the coss ui registry
  (`pnpm dlx shadcn@4.21.4 add @coss/<name>`); update them with the CLI, not by hand.
