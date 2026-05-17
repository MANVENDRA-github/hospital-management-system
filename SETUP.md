# Setup guide — clone & run on a clean Windows laptop

This is the **zero-to-running** walkthrough for someone who just cloned the repo and has nothing else installed. No Docker, no Postgres, no RabbitMQ, no `.env` file required.

Each backend service ships with sensible defaults: H2 file-based DB, baked-in JWT secret, hardcoded admin user. The Docker / Postgres / RabbitMQ flow still works under the `docker` Spring profile — see the last section.

**Time to first login: ~10 minutes** (most of it Maven downloading dependencies the first time).

---

## What you'll install

| Tool | Why | Where it comes from |
|---|---|---|
| Git | Clone the repo | winget |
| JDK 21 | Run all 8 backend services | winget (Microsoft OpenJDK) |
| Maven 3.9+ | Build & launch the modules | **Chocolatey** (winget no longer ships Apache.Maven) |
| Node.js LTS (20+) | Run the React frontend | winget |

---

## Step 1 — Install the prerequisites

Open **PowerShell** (the regular one — *not* Anaconda Prompt and *not* "Windows PowerShell ISE"). Run:

```powershell
winget install --id Git.Git -e --accept-source-agreements --accept-package-agreements
winget install --id Microsoft.OpenJDK.21 -e --accept-source-agreements --accept-package-agreements
winget install --id OpenJS.NodeJS.LTS -e --accept-source-agreements --accept-package-agreements
```

UAC prompts will fire — click **Yes** for each.

### Maven — install via Chocolatey

`Apache.Maven` was removed from winget. Easiest path is Chocolatey:

```powershell
# 1. Install Chocolatey itself (one-time)
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 2. Install Maven
choco install maven -y
```

> **Manual alternative:** download `apache-maven-3.9.x-bin.zip` from <https://maven.apache.org/download.cgi>, extract to `C:\Program Files\Apache\maven`, add `C:\Program Files\Apache\maven\bin` to **System PATH** (Settings → System → About → Advanced system settings → Environment Variables → Path → Edit → New). Then open a fresh PowerShell.

### Verify everything

**Close PowerShell entirely** and open a **fresh** PowerShell window. PATH only refreshes for new processes — using the same window won't see the new tools.

```powershell
git --version
java -version
mvn -v
node --version
```

All four should print versions. If any says **"not recognized"**, see **Troubleshooting → PATH** below.

---

## Step 2 — Clone the repo

```powershell
cd C:\dev          # or wherever you keep code; create the folder if needed
git clone https://github.com/<your-github-user>/hospital-management-system.git
cd hospital-management-system
```

---

## Step 3 — Start the backend

```powershell
.\scripts\start-backend.ps1
```

The script opens **8 PowerShell windows**, one per service:

| Service | Port | Purpose |
|---|---|---|
| `eureka-server` | 8761 | Service registry |
| `api-gateway` | 8080 | Reactive gateway, JWT validation |
| `auth-service` | 8081 | `auth_db` (H2 file) |
| `patient-service` | 8082 | `patient_db` |
| `doctor-service` | 8083 | `doctor_db` |
| `appointment-service` | 8084 | `appointment_db` |
| `billing-service` | 8085 | `billing_db` |
| `laboratory-service` | 8086 | `lab_db` |

**Wait ~3–5 minutes on first run** while Maven downloads dependencies. After that, watch each window — when it prints `Started <ServiceName>Application in N seconds`, that service is ready.

Verify everything registered by opening **http://localhost:8761** — you should see 7 instances (gateway + 6 services) listed as `UP`.

> **Auto-billing flow note:** without RabbitMQ, the appointment-completed → auto-bill event flow is a **no-op log line** (`[no broker] AppointmentCompletedEvent: ...` in the appointment-service window). Manual bill creation through the UI still works. To get the real async flow, run `docker compose up` instead.

---

## Step 4 — Start the frontend

In a **separate** PowerShell window (don't close the 8 service windows):

```powershell
cd C:\dev\hospital-management-system\frontend
npm ci             # first time only — pinned install from package-lock.json
npm run dev
```

Open **http://localhost:5173** and log in:

```
Email:    admin@gmail.com
Password: admin@123
```

You're in. Use the sidebar to navigate Patients / Doctors / Appointments / Billing / Lab Tests.

---

## Stopping everything

```powershell
.\scripts\stop-backend.ps1
```

This kills the 8 backend windows and any leftover Spring Boot `java` processes. Then `Ctrl+C` the frontend window.

---

## Tests & coverage

```powershell
cd C:\dev\hospital-management-system\backend
mvn verify
```

Runs JUnit + JaCoCo across every service. Coverage reports per module: `<service>/target/site/jacoco/index.html`. The strict 100% rule will fail the build if anything is uncovered.

---

## Persisting / wiping data

H2 file-based databases live under each service's `data/` folder (e.g. `backend/auth-service/data/auth_db.mv.db`). They survive restarts. To start fresh:

```powershell
Get-ChildItem backend -Recurse -Directory -Filter data | Remove-Item -Recurse -Force
```

These files are git-ignored, so they never end up in the repo.

---

## Troubleshooting

### `mvn` / `java` / `node` "is not recognized"

Almost always a PATH-not-refreshed problem. Try in order:

1. **Open a fresh PowerShell** — PATH only updates for processes started *after* the install.
2. **Refresh PATH in the current shell:**
   ```powershell
   $env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path','User')
   mvn -v
   ```
3. **If you have Anaconda / Miniconda installed:** its profile auto-activates `(base)` and can overwrite PATH in every new shell. Disable it once:
   ```powershell
   conda config --set auto_activate_base false
   ```
   Close all PowerShell windows and open a fresh one. The `(base)` prefix will be gone and your normal PATH will work.
4. **Chocolatey-only fallback:** `refreshenv` reloads PATH in the current shell without restarting.

### Frontend shows "Network Error" / 503 on login

The backend isn't fully up yet. Check:

1. **Open http://localhost:8761** — all 7 services should show `UP`. If `AUTH-SERVICE` is missing, the login will 503.
2. **Look at the `HMS - auth-service` window** — last line should say `Started AuthServiceApplication in N seconds`. If it shows a stack trace, something crashed; paste it for help.
3. **First boot is slow.** Wait 60–90 seconds after the script starts, then try again.

### "Port already in use"

Something else is on one of our ports (8761 / 8080 / 8081–8086 / 5173).

```powershell
Get-NetTCPConnection -LocalPort 8081 | Select-Object OwningProcess
Get-Process -Id <pid-from-above>
```

Stop the offending process (or change ports in the service's `application.yml`).

### A backend window flashes errors and stays open

Read the last ~20 lines in that window. Common culprits:

- **`BUILD FAILURE`** at the top → Maven couldn't compile something. Usually a Java mismatch — confirm `java -version` shows 21.
- **H2 lock error** → another service is already using that DB. Run `.\scripts\stop-backend.ps1` and start again.
- **`APPLICATION FAILED TO START`** → real bug. Paste the stack trace and the service name.

### `mvn verify` fails on coverage

The 100% rule failed. Open the JaCoCo report (`backend/<service>/target/site/jacoco/index.html`) and add the missing test.

### Em-dash / weird parser errors when running scripts

If you see something like `The string is missing the terminator: "` from a `.ps1` script, it's likely a Unicode character (em-dash) that PowerShell 5.1 misreads under Windows-1252. All shipped scripts in this repo are ASCII-only — but if you edited one, replace any `—` with `-`.

---

## Switching to the Docker / Postgres / RabbitMQ stack

When you want the "real infra" version (different DB per service via Postgres, real async broker via RabbitMQ):

```powershell
copy .env.example .env       # for the docker profile credentials
docker compose up -d --build
```

No code changes needed — the `docker` Spring profile (set automatically by Compose) switches every service to PostgreSQL + RabbitMQ + Eureka via Docker network names. Frontend at http://localhost:5173, same login.

---

## Why does the no-Docker mode still satisfy the case-study brief?

- **DB per service:** each service has its own H2 file (`auth_db.mv.db`, `patient_db.mv.db`, etc.) — different databases, different files, different connections.
- **Async / non-blocking:** `@Async` on `AppointmentEventPublisher.publishCompleted` makes the publish call non-blocking. Real broker delivery happens under the `docker` profile.
- **Externalized config:** every default is overridable via env var (`JWT_SECRET`, `ADMIN_EMAIL`, `POSTGRES_*`, etc.). Defaults are *for local dev only*.
- **Independent deployable services:** every service is a self-contained Spring Boot app with its own DB and `application.yml`.
