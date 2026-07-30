# PulseBoard Frontend

React (Vite) frontend for PulseBoard: a private admin dashboard and a public status page,
talking to the Spring Boot backend over REST.

## Pages

- `/` - Admin dashboard: register endpoints, see live status tiles, expand a card for a
  24h/7-day response time chart plus uptime%, p50, and p95.
- `/status` - Public status page: green/yellow/red per service, no admin controls. This is
  the page you'd actually share/link publicly.

Both pages poll the backend every 30 seconds so they stay current with the scheduler's
60-second check cycle without a manual refresh.

## Run locally

```bash
npm install
cp .env.example .env   # defaults to http://localhost:8080
npm run dev
```

Requires the backend running (see `../pulseboard/README.md`) - the admin dashboard needs
`GET/POST /api/endpoints` to be reachable.

## Build

```bash
npm run build
```

Outputs static files to `dist/` - this is what gets deployed to Vercel.

## Deploy (Vercel)

1. Push this repo to GitHub.
2. In Vercel: New Project → import the repo → set **Root Directory** to `pulseboard-frontend`.
3. Framework preset: Vite (auto-detected).
4. Add environment variable `VITE_API_BASE_URL` = your deployed Railway backend URL
   (e.g. `https://pulseboard-backend-production.up.railway.app`).
5. Deploy. `vercel.json` in this folder already handles SPA routing so `/status` works
   on a hard refresh, not just client-side navigation.
