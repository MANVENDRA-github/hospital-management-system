# Installs everything needed to run the Hospital Management System without Docker.
# Default mode uses H2 + no broker, so Postgres and RabbitMQ are NOT needed.
# Run from a normal PowerShell — winget will prompt for elevation per package.
#
# Idempotent: re-running will skip packages that are already installed.

$ErrorActionPreference = 'Stop'

$packages = @(
    @{ Id = 'Git.Git';                Name = 'Git'         },
    @{ Id = 'Microsoft.OpenJDK.21';   Name = 'JDK 21'      },
    @{ Id = 'Apache.Maven';           Name = 'Maven'       },
    @{ Id = 'OpenJS.NodeJS.LTS';      Name = 'Node.js LTS' }
)

function Test-WingetInstalled {
    param([string]$Id)
    $output = winget list --id $Id --exact 2>&1 | Out-String
    return $output -match $Id
}

Write-Host '== HMS prerequisites installer ==' -ForegroundColor Cyan
Write-Host '(Default mode: H2 + no broker. No Postgres / RabbitMQ install required.)' -ForegroundColor DarkGray
Write-Host ''

if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    Write-Error 'winget not available. Install "App Installer" from the Microsoft Store first.'
    exit 1
}

foreach ($pkg in $packages) {
    Write-Host "[$($pkg.Name)]" -ForegroundColor Yellow

    if (Test-WingetInstalled -Id $pkg.Id) {
        Write-Host "  Already installed. Skipping." -ForegroundColor Green
        continue
    }

    Write-Host "  Installing $($pkg.Id) (UAC prompt may appear)..."
    winget install --id $pkg.Id --source winget --accept-source-agreements --accept-package-agreements --silent
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "  $($pkg.Name) install returned exit code $LASTEXITCODE. If it was already installed under a different package id, you can ignore this."
    }
}

Write-Host ''
Write-Host '== All packages processed ==' -ForegroundColor Cyan
Write-Host ''
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host '  1. CLOSE this terminal and open a fresh PowerShell so PATH picks up the new tools.'
Write-Host '  2. Verify:  java -version ; mvn -v ; node --version'
Write-Host '  3. Start backend:  .\scripts\start-backend.ps1'
Write-Host '  4. Start frontend: cd frontend ; npm install ; npm run dev'
Write-Host ''
Write-Host 'Default admin login: admin@gmail.com / admin@123 (baked-in for local dev).'
Write-Host ''
