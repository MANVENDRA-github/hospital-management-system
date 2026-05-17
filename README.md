# Hospital Management System

A microservices-based Hospital Management System built with Spring Boot, Spring Cloud, PostgreSQL, RabbitMQ, and React.

## Architecture

| Service              | Port | Database         | Purpose                                  |
|----------------------|------|------------------|------------------------------------------|
| eureka-server        | 8761 | -                | Service discovery (Netflix Eureka)       |
| api-gateway          | 8080 | -                | Single entry point, JWT validation       |
| auth-service         | 8081 | auth_db          | Registration, login, JWT issuance        |
| patient-service      | 8082 | patient_db       | Patient records (CRUD)                   |
| doctor-service       | 8083 | doctor_db        | Doctor profiles, specialization search   |
| appointment-service  | 8084 | appointment_db   | Bookings; calls patient/doctor via Feign |
| billing-service      | 8085 | billing_db       | Bills; consumes appointment events       |
| laboratory-service   | 8086 | lab_db           | Lab tests and results                    |
| frontend (React/Vite)| 5173 | -                | UI; talks only to the gateway            |

Roles: `ADMIN`, `DOCTOR`, `PATIENT`.

### Roles & permissions matrix

| Operation                              | ADMIN | DOCTOR | PATIENT |
|----------------------------------------|:-----:|:------:|:-------:|
| Register / Login                       | ✓     | ✓      | ✓       |
| Patient: create / update               | ✓     |        | ✓       |
| Patient: delete                        | ✓     |        |         |
| Patient: read by id                    | ✓     | ✓      | ✓       |
| Patient: list all                      | ✓     | ✓      |         |
| Doctor: create / delete                | ✓     |        |         |
| Doctor: update                         | ✓     | ✓      |         |
| Doctor: read / search by specialization| ✓     | ✓      | ✓       |
| Appointment: book / cancel             | ✓     |        | ✓       |
| Appointment: complete                  | ✓     | ✓      |         |
| Appointment: list all                  | ✓     |        |         |
| Appointment: by patient                | ✓     |        | ✓       |
| Appointment: by doctor                 | ✓     | ✓      |         |
| Billing: create / mark paid            | ✓     |        |         |
| Billing: read by id / by patient       | ✓     |        | ✓       |
| Lab: book test / upload result         | ✓     | ✓      |         |
| Lab: read by id / by patient           | ✓     | ✓      | ✓       |
| Lab: list all                          | ✓     |        |         |

### Async event flow

When an appointment is marked `COMPLETED`, `appointment-service` publishes an `AppointmentCompletedEvent` to a RabbitMQ topic exchange (`hms.appointment.exchange`, routing key `appointment.completed`). `billing-service` consumes the event from the bound queue (`hms.appointment.completed.billing`) and auto-creates a pending bill for the patient — unless one already exists for that appointment ID (idempotent).

## Diagrams

All required case-study diagrams are in [`docs/diagrams/`](docs/diagrams/):

- [Architecture](docs/diagrams/architecture.md)
- [Use case](docs/diagrams/use-case.md)
- [Service interaction](docs/diagrams/service-interaction.md)
- [Class diagram](docs/diagrams/class.md)
- [ER diagram](docs/diagrams/er.md)
- [Sequence diagrams](docs/diagrams/sequence.md)

All authored as Mermaid blocks — they render directly on GitHub.

## Prerequisites

- Docker Desktop (the only hard requirement)
- Optional for local backend dev outside Docker: Java 21, Maven 3.9+
- Optional for frontend dev outside Docker: Node.js 20+

## Quick start

```bash
git clone <this-repo>
cd hospital-management-system
cp .env.example .env
docker compose up -d
```

Wait ~60 seconds for all services to register with Eureka.

- Frontend: http://localhost:5173
- Gateway:  http://localhost:8080
- Eureka:   http://localhost:8761
- RabbitMQ management: http://localhost:15672 (user/pass from `.env`)

### Default admin

A seed admin is created on first boot of `auth-service`:

```
email:    admin@gmail.com
password: admin@123
```

### Calling the API directly

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@gmail.com","password":"admin@123"}'

# Use the returned token
curl http://localhost:8080/api/patients \
  -H 'Authorization: Bearer <token>'
```

### Postman

Import [`docs/postman/HMS.postman_collection.json`](docs/postman/HMS.postman_collection.json) into Postman. Run the **Auth → Login** request once — the `bearerToken` collection variable is auto-populated, and every subsequent request uses it.

## Swagger / OpenAPI

Each service exposes Swagger:

- http://localhost:8081/swagger-ui.html (auth)
- http://localhost:8082/swagger-ui.html (patient)
- ... and so on

## Running without Docker

You can also run the full stack with **just JDK 21 + Maven + Node.js** — no Docker, no Postgres, no RabbitMQ, no `.env` editing. Each service ships with H2 file-based DB defaults and baked-in admin credentials.

```powershell
.\scripts\install-prereqs.ps1     # one-time: installs Git, JDK 21, Maven, Node via winget
# Open a fresh PowerShell so PATH picks up
.\scripts\start-backend.ps1       # opens 8 service windows
cd frontend; npm install; npm run dev
```

→ http://localhost:5173, log in with `admin@gmail.com` / `admin@123`.

The async appointment→bill auto-flow becomes a no-op log line in this mode (it requires a real RabbitMQ broker — start that via `docker compose up`). Everything else (CRUD, role-based access, manual billing, lab tests, the whole UI) works identically.

Full walkthrough + troubleshooting in [`SETUP.md`](SETUP.md).

## Tests & coverage

```bash
cd backend
mvn verify                 # runs unit tests + JaCoCo (100% line/branch enforced)
```

Coverage reports per module: `backend/<service>/target/site/jacoco/index.html`.

## SonarQube (optional)

```bash
# 1. Boot SonarQube + its dedicated Postgres (separate volume from the HMS DB).
docker compose --profile quality up -d

# 2. Wait ~60s for SonarQube to be ready, then open http://localhost:9000.
#    Default login: admin / admin (you'll be prompted to change it).

# 3. Generate a user token: My Account → Security → Generate. Copy it.

# 4. Run analysis from the backend directory.
cd backend
mvn verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

Per-module reports appear under `backend/<service>/target/site/jacoco/jacoco.xml` and are uploaded to SonarQube. The case-study target is **zero High/Critical issues**.

## Centralized logging (optional)

Each service emits structured JSON logs to stdout (via `logstash-logback-encoder`) when running with the `docker` Spring profile. Bring up Loki + Promtail + Grafana on demand:

```bash
docker compose --profile observability up -d
```

- Promtail tails Docker container stdout, ships logs to Loki, tagged by `service` and `container` labels.
- Grafana is available at http://localhost:3000 (login `admin` / `admin`).
- A pre-provisioned dashboard "HMS — Service logs" is available under the **HMS** folder.

## Repository layout

```
hospital-management-system/
├── backend/              Maven multi-module (8 services)
│   ├── eureka-server/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── patient-service/
│   ├── doctor-service/
│   ├── appointment-service/
│   ├── billing-service/
│   └── laboratory-service/
├── frontend/             React + Vite + TypeScript
├── infra/
│   ├── postgres/init.sql Creates 6 per-service databases on first boot
│   ├── loki/             Loki config (observability profile)
│   ├── promtail/         Promtail config (observability profile)
│   └── grafana/          Datasource + dashboard provisioning
├── docs/
│   ├── diagrams/         Architecture, use case, service interaction, class, ER, sequence (Mermaid)
│   └── postman/          Importable Postman collection
├── scripts/              Path B (no-Docker) helper scripts: install-prereqs, init-databases, start-backend, stop-backend
├── docker-compose.yml    Brings the whole stack up; `quality` and `observability` are opt-in profiles
├── .env.example          Copy to .env before first run
├── SETUP.md              Path B walkthrough (no Docker)
└── .github/workflows/    CI: mvn -B verify on push + frontend build
```

## Compose profiles at a glance

| Command                                              | What it brings up                                       |
|------------------------------------------------------|---------------------------------------------------------|
| `docker compose up -d`                               | Everything required: Postgres, RabbitMQ, Eureka, Gateway, all 6 business services, frontend |
| `docker compose --profile observability up -d`       | Adds Loki + Promtail + Grafana (logs aggregation)       |
| `docker compose --profile quality up -d`             | Adds SonarQube + its dedicated Postgres                 |

## Mandatory case-study checklist

| Requirement                                            | Where it lives                                                              |
|--------------------------------------------------------|-----------------------------------------------------------------------------|
| 4+ services + Auth, independent deploy                 | 8 modules under `backend/`, each its own Spring Boot app + Dockerfile       |
| Database per service                                   | `infra/postgres/init.sql` creates 6 separate DBs                            |
| API Gateway only access + service discovery            | Spring Cloud Gateway (8080) + Netflix Eureka (8761)                         |
| JWT auth, BCrypt, role-based                           | `auth-service` issues; gateway + per-service `JwtAuthFilter` validate       |
| Async / non-blocking processing                        | RabbitMQ event flow: appointment-completed → billing auto-bill              |
| Inter-service via WebClient/Feign/RestTemplate         | OpenFeign in `appointment-service` (`PatientClient`, `DoctorClient`)        |
| JUnit 5 + Mockito + 100% JaCoCo                        | `mvn verify`; check rule enforces 100% line + branch (excluding DTO/entity/config/Application/SecurityConfig) |
| SonarQube enabled                                      | `--profile quality` + `mvn sonar:sonar`                                     |
| Centralized logging                                    | JSON via `logstash-logback-encoder` → Loki/Grafana on `--profile observability` |
| SOLID, layered, DTOs, global exception handling        | controller/service/repository/dto/exception layers per service              |
| No shared dependencies across services                 | Each service has its own copy of `JwtAuthFilter`, security config, etc.     |
| Swagger/OpenAPI per service                            | `springdoc-openapi-starter-webmvc-ui` per service                           |
| Required diagrams                                      | `docs/diagrams/*.md` (Mermaid)                                              |
