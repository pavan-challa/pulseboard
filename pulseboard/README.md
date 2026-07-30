# PulseBoard Backend

Spring Boot backend for PulseBoard - all three phases implemented. See the repo root
`README.md` for architecture diagram, design pattern write-up, and the Railway/Vercel
deployment guide; this file is the backend-specific dev reference.

## Stack / versions

- Spring Boot **4.1.0** (current supported line - Spring Boot 3.5 hit OSS end-of-life on
  June 30, 2026, so this project is pinned to the actively patched release instead)
- Java 21
- MySQL 8+
- Maven
- Docker (optional, for local integration testing / the actual deploy artifact)

Nothing in this codebase touches the parts of Spring Boot 4 that actually changed
behavior (Jackson 3, Spring Security defaults, Undertow) - plain JPA entities, a
scheduled job, and REST controllers - so the upgrade from 3.x is invisible here.

## Project layout

```
pulseboard/
├── pom.xml
├── Dockerfile
├── docker-compose.yml          # backend + MySQL, for local testing
└── src/
    ├── main/java/com/pulseboard/
    │   ├── PulseboardApplication.java     # @SpringBootApplication + @EnableScheduling
    │   ├── model/                         # Endpoint, CheckResult, Incident, enums
    │   ├── repository/                    # Spring Data JPA repositories
    │   ├── strategy/                      # Strategy: HealthCheckStrategy + HttpGetCheckStrategy
    │   ├── service/                       # CheckService, IncidentService, StatsService,
    │   │                                  # StatusService, EndpointService
    │   ├── observer/                      # Observer: AlertObserver, Logging/EmailAlertObserver
    │   ├── scheduler/                     # HealthCheckScheduler (@Scheduled, every 60s)
    │   ├── controller/                    # EndpointController, PublicStatusController
    │   ├── util/                          # PercentileCalculator
    │   └── dto/                           # Request/response DTOs
    ├── main/resources/
    │   ├── application.properties
    │   └── schema.sql                     # the 3 tables, run automatically on startup
    └── test/java/com/pulseboard/util/     # PercentileCalculatorTest
```

## 1. Local MySQL setup

Install MySQL 8 if you don't have it (macOS: `brew install mysql && brew services start mysql`),
**or** skip this entirely and use `docker compose up --build` from this folder instead,
which runs MySQL for you.

For a bare-metal local MySQL:

```sql
CREATE DATABASE IF NOT EXISTS pulseboard;
```

`schema.sql` creates the three tables automatically every time the app starts
(`spring.sql.init.mode=always`), using `CREATE TABLE IF NOT EXISTS`, so it's safe to
restart repeatedly without wiping data.

Edit `src/main/resources/application.properties` and set your real credentials:

```properties
spring.datasource.username=root
spring.datasource.password=changeme
```

## 2. Run it

Maven, against a MySQL you're running yourself:

```bash
mvn spring-boot:run
```

Or Docker Compose (backend + MySQL together, nothing to install):

```bash
docker compose up --build
```

Either way, check `http://localhost:8080/actuator/health` → `{"status":"UP"}`.

## 3. Try it

Register an endpoint:

```bash
curl -X POST http://localhost:8080/api/endpoints \
  -H "Content-Type: application/json" \
  -d '{"name": "My E-Commerce Store", "url": "https://your-app.vercel.app"}'
```

Within 60 seconds the scheduler pings it and writes a row to `check_results`.

```bash
curl http://localhost:8080/api/endpoints                    # list + current status
curl http://localhost:8080/api/endpoints/1/checks            # last 24h (?hours=168 for 7d)
curl http://localhost:8080/api/endpoints/1/stats             # p50/p95 + uptime% (?hours=168 too)
curl http://localhost:8080/api/public/status                 # green/yellow/red, public page's data source
```

To see incident detection + alerting in action, register an endpoint pointing at a URL
that will fail (e.g. a random unused port on localhost) and watch the logs after two
60-second cycles - `LoggingAlertObserver` logs an `INCIDENT OPENED` line even without any
mail configuration. Fix the URL and it logs `INCIDENT RESOLVED` on the next successful check.

## Email alerts

Commented out by default in `application.properties`. Uncomment and fill in real SMTP
credentials (Gmail: use an "app password", not your account password) to make
`EmailAlertObserver` active - it's conditional on `spring.mail.host` being set, so the app
runs fine with or without it configured.

## Tests

```bash
mvn test
```

`PercentileCalculatorTest` covers the p50/p95 math directly (pure function, no Spring
context or DB needed). The GitHub Actions workflow (`.github/workflows/ci.yml` at the
repo root) runs this against a real MySQL service container on every push to `main`.

## A note on verification

I don't have Maven, a Java 21 JDK, or root access to install them in my sandbox, so I
wasn't able to run `mvn compile`/`mvn test` myself here - I reviewed every file by hand
for correctness instead. Please run `mvn spring-boot:run` (or `docker compose up --build`)
on your machine as the real check, and send me the error output if anything doesn't
compile or connect - I'll fix it immediately.
