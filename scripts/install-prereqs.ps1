# Installs everything needed to run the Hospital Management System without Docker.
# Run from a normal PowerShell — winget will prompt for elevation per package.
#
# Idempotent: re-running will skip packages that are already installed.

$ErrorActionPreference = 'Stop'

$packages = @(
    @{ Id = 'Git.Git';                          Name = 'Git'              },
    @{ Id = 'Microsoft.OpenJDK.21';             Name = 'JDK 21'           },
    @{ Id = 'Apache.Maven';                     Name = 'Maven'            },
    @{ Id = 'PostgreSQL.PostgreSQL.16';         Name = 'PostgreSQL 16'    },
    @{ Id = 'RabbitMQ.Server';                  Name = 'RabbitMQ Server'  },
    @{ Id = 'OpenJS.NodeJS.LTS';                Name = 'Node.js LTS'      }
)

function Test-WingetInstalled {
    param([string]$Id)
    $output = winget list --id $Id --exact 2>&1 | Out-String
    return $output -match $Id
}

Write-Host '== HMS prerequisites installer ==' -ForegroundColor Cyan
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
Write-Host '  2. Verify:  java -version ; mvn -v ; psql --version ; node --version'
Write-Host '  3. The PostgreSQL installer asked for a password during install — REMEMBER IT.'
Write-Host '     You will paste it into .env (POSTGRES_PASSWORD) and use it when running'
Write-Host '     scripts\init-databases.ps1.'
Write-Host ''
Write-Host '  4. Then continue with: copy .env.example .env  (and edit POSTGRES_PASSWORD)'
Write-Host '                          .\scripts\init-databases.ps1'
Write-Host '                          .\scripts\start-backend.ps1'
Write-Host ''
