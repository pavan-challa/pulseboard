# PulseBoard

Self-hosted application health monitoring. PulseBoard pings your own deployed projects
every 60 seconds, tracks response time and uptime history, opens an incident and emails
you the moment something breaks, and gives you a public status page to point users at -
all without paying for an external SaaS.

Built as a portfolio project by Pavan Challa, following a Repository / Service Layer /
Strategy / Observer architecture on top of Spring Boot + MySQL, with a React frontend.

## Repo layout

```
.
├── pulseboard/             # Spring Boot backend (Java 21)
├── pulseboard-frontend/    # React (Vite) frontend
├── docker-compose.yml.     # (inside pulseboard/) local backend + MySQL stack
└── .github/workflows/ci.yml # build + test on every push to main
```

Open the repo root in VS Code as a single workspace - both `pulseboard/` and
`pulseboard-frontend/` are ordinary subfolders, so the Java and JS tooling in VS Code
(Extension Pack for Java, ESLint, etc.) pick each one up independently.

## Architecture

```mermaid
flowchart LR
    subgraph Frontend [React - Vercel]
        Admin[Admin Dashboard /]
        Public[Public Status Page /status]
    end

    subgraph Backend [Spring Boot - Railway]
        API[REST Controllers]
        Sched[HealthCheckScheduler<br/>@Scheduled every 60s]
        Svc[Service Layer<br/>CheckService / IncidentService / StatsService]
        Strat[Strategy: HealthCheckStrategy<br/>HttpGetCheckStrategy]
        Obs[Observer: AlertObserver<br/>Logging + Email]
    end

    DB[(MySQL<br/>endpoints / check_results / incidents)]
    Target[Your deployed project<br/>e.g. Mini E-Commerce Store]
    Mail[Email alert]

    Admin -->|register endpoint, view stats| API
    Public -->|poll /api/public/status| API
    API --> Svc
    Svc --> DB
    Sched --> Strat
    Strat -->|HTTP GET| Target
    Sched --> Svc
    Svc -->|2+ consecutive DOWN| Obs
    Obs --> Mail
```

## Design patterns, mapped to code

- **Repository** - `EndpointRepository`, `CheckResultRepository`, `IncidentRepository`
  (`pulseboard/src/main/java/com/pulseboard/repository`)
- **Service Layer** - `CheckService`, `IncidentService`, `StatsService`, `StatusService`,
  `EndpointService` sit between controllers and repositories
- **Strategy** - `HealthCheckStrategy` interface / `HttpGetCheckStrategy` implementation
  (`.../strategy`). The scheduler and `CheckService` depend only on the interface, so a
  second check type (POST with an auth header, matching an expected response body) is a
  new class, not a rewrite.
- **Observer** - `AlertObserver` interface, with `LoggingAlertObserver` (always on) and
  `EmailAlertObserver` (active once SMTP is configured) both registered automatically as
  Spring beans (`.../observer`). `IncidentService` notifies every observer without
  knowing how many exist or what they do - a Slack or SMS handler later is a third class.

## Interview talking points this project covers

**How do you handle the scheduler if the monitoring app itself goes down?**
It doesn't self-heal - if the JVM process dies, checks stop until it's restarted (Railway
auto-restarts on crash). Within the process, each endpoint check in
`HealthCheckScheduler.runChecks()` is wrapped in its own try/catch, so one endpoint
throwing (bad URL, DNS failure) can't kill the scheduled method or the scheduler thread
for the rest of the run.

**Why a relational DB for time-series-like data, and what's the tradeoff?**
`check_results` is an append-only table keyed by `endpoint_id` + `checked_at` - a
relational DB with an index on those columns comfortably answers "last 24h" or "last 7d"
range queries for a personal-project scale of data. The real tradeoff shows up in
`StatsService`: MySQL has no native `PERCENTILE_CONT`, so p50/p95 are computed by pulling
the window's rows back and sorting in application memory rather than in SQL. That's fine
at thousands of rows; at real time-series scale you'd reach for a purpose-built TSDB
(InfluxDB, TimescaleDB) or pre-aggregate into hourly rollups instead of scanning raw rows.

**How do you prevent an alert storm?**
`IncidentService.evaluate()` only opens an incident on the transition into 2 consecutive
DOWN checks, and only resolves it on the transition back to UP. While an incident stays
open, every subsequent failed 60-second check is a no-op as far as alerting goes - one
incident produces at most two emails (opened, resolved), never one per check.

**Difference between a check failing and an incident being declared?**
A single `CheckResult` with `status=DOWN` is just a data point - blips happen (a slow
DNS resolution, a cold start). An `Incident` is only created after `IncidentService` sees
two consecutive DOWN results for the same endpoint, which is what actually triggers
alerting and shows red on the public status page.

## Phases delivered

- **Phase 1** - MVP: register endpoint, `@Scheduled` HTTP GET check every 60s, results
  stored in MySQL, admin dashboard shows current status + 24h history.
- **Phase 2** - Incident detection (2+ consecutive DOWN), email alerts on open/resolve via
  Observer pattern, public status page, 7-day response time graphs, p50/p95 + uptime%.
- **Phase 3** - Dockerized backend, GitHub Actions CI (build + test on push to `main`),
  Railway (backend + MySQL) / Vercel (frontend) deployment, pointed at a real live
  endpoint.

## Quick start (local)

```bash
# Backend + MySQL together, via Docker
cd pulseboard
docker compose up --build
# -> http://localhost:8080/actuator/health should return {"status":"UP"}

# Frontend, in a second terminal
cd pulseboard-frontend
npm install
cp .env.example .env
npm run dev
# -> http://localhost:5173
```

Or run the backend directly with Maven instead of Docker - see `pulseboard/README.md`.

## Deploying for real

### Backend + MySQL on Railway

1. Push this repo to GitHub.
2. In Railway: New Project → Deploy from GitHub repo → set the service's **root
   directory** to `pulseboard` (so Railway finds the `Dockerfile` there).
3. Add a MySQL plugin to the same Railway project.
4. In the backend service's Variables tab, set (referencing the MySQL plugin's own
   variables with Railway's `${{ServiceName.VAR}}` syntax):
   - `SPRING_DATASOURCE_URL` = `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC`
   - `SPRING_DATASOURCE_USERNAME` = `${{MySQL.MYSQLUSER}}`
   - `SPRING_DATASOURCE_PASSWORD` = `${{MySQL.MYSQLPASSWORD}}`
   - `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` (optional, for
     email alerts - Spring Boot picks these up automatically, no code change needed)
   - `APP_ALERT_RECIPIENT_EMAIL` = wherever you want incident emails sent
5. Railway sets `PORT` automatically; Spring Boot's `server.port` should read it - if
   Railway's build doesn't pick it up automatically, add `SERVER_PORT=${{PORT}}` explicitly.
6. Deploy. Note the generated `*.up.railway.app` URL - the frontend needs it.

### Frontend on Vercel

See `pulseboard-frontend/README.md` - set **Root Directory** to `pulseboard-frontend` and
`VITE_API_BASE_URL` to the Railway URL from the step above.

### Point PulseBoard at your live e-commerce project

Once both are deployed, open the admin dashboard and register your Mini E-Commerce
Store's real Vercel URL as an endpoint. That's what makes this a monitoring tool watching
a real running system rather than a demo pinging itself.

## Known limitations / honest scope notes

- **No authentication.** Every API endpoint, including endpoint registration, is
  currently open - the "private admin dashboard" vs. "public status page" split is a
  frontend routing distinction only, not a backend security boundary. Adding Spring
  Security to lock down everything except `/api/public/**` is the natural next step if
  this needs to be genuinely private.
- **Percentiles are computed in Java, not SQL** (see the p50/p95 talking point above) -
  fine at this scale, a known tradeoff at larger scale.
- **Single global check interval.** `check_interval_seconds` exists per-endpoint in the
  schema, but the scheduler currently checks every registered endpoint on one shared
  60-second `@Scheduled` cycle rather than honoring a per-endpoint interval.
