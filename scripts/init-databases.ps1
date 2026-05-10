# Creates the 6 per-service Postgres databases used by HMS.
# Idempotent: skips databases that already exist.
#
# Usage:
#   .\scripts\init-databases.ps1
# Optionally specify Postgres host/port/user, defaults to localhost:5432 / postgres.

param(
    [string]$PgHost = 'localhost',
    [int]   $PgPort = 5432,
    [string]$PgUser = 'postgres'
)

$ErrorActionPreference = 'Stop'

# Locate psql — winget installs Postgres to C:\Program Files\PostgreSQL\<ver>\bin
$psql = (Get-Command psql -ErrorAction SilentlyContinue)?.Path
if (-not $psql) {
    $candidate = Get-ChildItem 'C:\Program Files\PostgreSQL' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'bin\psql.exe' } |
        Where-Object { Test-Path $_ } |
        Select-Object -First 1
    if ($candidate) { $psql = $candidate }
}
if (-not $psql) {
    Write-Error 'psql.exe not found. Make sure PostgreSQL is installed and either on PATH or under C:\Program Files\PostgreSQL\.'
    exit 1
}

$securePass = Read-Host -AsSecureString "Enter password for Postgres user '$PgUser'"
$plainPass = [System.Net.NetworkCredential]::new('', $securePass).Password
$env:PGPASSWORD = $plainPass

$databases = @('auth_db', 'patient_db', 'doctor_db', 'appointment_db', 'billing_db', 'lab_db')

Write-Host ''
Write-Host '== Creating HMS databases ==' -ForegroundColor Cyan

foreach ($db in $databases) {
    $exists = & $psql -U $PgUser -h $PgHost -p $PgPort -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$db'" 2>$null
    if ($exists -eq '1') {
        Write-Host "  $db already exists" -ForegroundColor Green
    } else {
        & $psql -U $PgUser -h $PgHost -p $PgPort -d postgres -c "CREATE DATABASE $db" | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Remove-Item Env:PGPASSWORD
            Write-Error "Failed to create database '$db'. Check the password and that Postgres is running."
            exit 1
        }
        Write-Host "  $db created" -ForegroundColor Green
    }
}

Remove-Item Env:PGPASSWORD

Write-Host ''
Write-Host '== Done ==' -ForegroundColor Cyan
Write-Host ''
Write-Host "Reminder: edit .env so POSTGRES_PASSWORD matches the password you just used."
Write-Host ''
