# Local setup without Docker

Run the full stack with **just JDK 21 + Maven + Node.js**. No Postgres, no RabbitMQ, no Docker, no `.env` editing.

How: each backend service ships with sensible defaults — H2 file-based DB, baked-in JWT secret, hardcoded admin user (`admin@gmail.com` / `admin@123`). The Docker / Postgres / RabbitMQ flow still works when the `docker` Spring profile is active (which Docker Compose sets automatically), so case-study evaluators get the "real infra" version with `docker compose up`.

## What gets installed

| Tool | Why |
|---|---|
| Git | clone the repo |
| JDK 21 | run all 8 backend services |
| Maven 3.9+ | build & launch the modules |
| Node.js LTS (20+) | run the React frontend |

That's it.

## Step 1 — Install prerequisites

Open PowerShell:

```powershell
winget install --id Git.Git --source winget --accept-source-agreements --accept-package-agreements
winget install --id Microsoft.OpenJDK.21 --source winget --accept-source-agreements --accept-package-agreements
winget install --id Apache.Maven --source winget --accept-source-agreements --accept-package-agreements
winget install --id OpenJS.NodeJS.LTS --source winget --accept-source-agreements --accept-package-agreements
```

UAC prompts will fire for each — click **Yes**. Close the terminal and open a fresh PowerShell, then verify:

```powershell
git --version
java -version
mvn -v
node --version
```

All four should print versions.

(Or run `.\scripts\install-prereqs.ps1` — it does the same thing in one go.)

## Step 2 — Clone the repo

```powershell
cd C:\dev          # or wherever
git clone <your-repo-url>
cd hospital-management-system
```

## Step 3 — Start the backend

```powershell
.\scripts\start-backend.ps1
```

This opens 8 PowerShell windows, one per service:
- `eureka-server` (8761) — service registry
- `api-gateway` (8080) — reactive gateway, JWT validation
- `auth-service` (8081) — `auth_db` (H2 file at `backend/auth-service/data/`)
- `patient-service` (8082) — `patient_db`
- `doctor-service` (8083) — `doctor_db`
- `appointment-service` (8084) — `appointment_db`
- `billing-service` (8085) — `billing_db`
- `laboratory-service` (8086) — `lab_db`

Eureka starts first, then the others stagger in. Wait ~60 seconds for everything to register.

> **Auto-billing flow note:** Without RabbitMQ running, the appointment-completed → auto-bill event flow is a **no-op log line** (you'll see `[no broker] AppointmentCompletedEvent: ...` in appointment-service's window). Manual bill creation through the UI still works. To get the real async flow, run `docker compose up` instead.

## Step 4 — Start the frontend

In a separate PowerShell window (or VS Code terminal):

```powershell
cd D:\hospital-management-system\frontend
npm install        # first time only
npm run dev
```

Open http://localhost:5173 and log in:

```
admin@gmail.com  /  admin@123
```

## Stopping everything

```powershell
.\scripts\stop-backend.ps1
```

Then Ctrl+C the frontend window.

## Tests & coverage

```powershell
cd D:\hospital-management-system\backend
mvn verify
```

Runs JUnit + JaCoCo across all services. Coverage reports per module: `<service>/target/site/jacoco/index.html`. The strict 100% rule will fail the build if anything is uncovered.

## Persisting / wiping data

H2 file-based databases live under each service's `data/` folder (e.g. `backend/auth-service/data/auth_db.mv.db`). They survive restarts. To start fresh:

```powershell
Get-ChildItem backend -Recurse -Directory -Filter data | Remove-Item -Recurse -Force
```

These files are git-ignored.

## Switching to the Docker / Postgres / RabbitMQ stack

Anything that runs in `docker compose up -d` activates the `docker` Spring profile, which switches every service to:
- PostgreSQL (containerized)
- Real RabbitMQ broker (containerized)
- Eureka via Docker network names

No code changes needed. Just:

```powershell
copy .env.example .env       # for the docker profile credentials
docker compose up -d --build
```

→ http://localhost:5173, same login. (See README "Quick start" for the Docker flow.)

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `java: command not found` after install | Open a **fresh** PowerShell — PATH only refreshes for new processes. |
| Port 8081 already in use | Something else is on that port. `Get-NetTCPConnection -LocalPort 8081` to find it. |
| Service can't reach Eureka | Eureka takes ~15s to start. The launcher waits, but if you started manually, give it a beat. |
| Frontend shows "Network Error" on login | Backend not fully started. Check that all 8 windows show "Started ... in N seconds". |
| `mvn verify` fails 100% coverage rule | Open the JaCoCo report and add the missing test. |
| `[no broker] AppointmentCompletedEvent` in logs | Expected in default profile. Switch to `docker compose up` for the real broker flow. |

## Why does this still satisfy the case-study brief?

- **DB per service:** each service has its own H2 file (`auth_db.mv.db`, `patient_db.mv.db`, etc.) — different databases, different files, different connections.
- **Async / non-blocking:** `@Async` on `AppointmentEventPublisher.publishCompleted` makes the publish call non-blocking. Real broker delivery happens under the `docker` profile.
- **Externalized config:** every default is overridable via env var (`JWT_SECRET`, `ADMIN_EMAIL`, `POSTGRES_*`, etc.) — defaults are *for local dev only*.
- **Independent deployable services:** every service is a self-contained Spring Boot app with its own DB and `application.yml`.
