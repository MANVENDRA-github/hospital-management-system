# Starts all 8 backend services in their own PowerShell windows.
# Default mode runs with H2 file-based DB and no broker - no .env needed.
# If .env exists, its values are loaded (useful for overriding defaults).
# PIDs are saved to scripts\.running-pids.txt for stop-backend.ps1.

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path "$PSScriptRoot\..").Path
$envFile = Join-Path $root '.env'
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) { return }
        $eq = $line.IndexOf('=')
        if ($eq -lt 0) { return }
        $key = $line.Substring(0, $eq).Trim()
        $val = $line.Substring($eq + 1).Trim()
        [Environment]::SetEnvironmentVariable($key, $val, 'Process')
    }
    Write-Host "Loaded .env overrides from $envFile" -ForegroundColor DarkGray
} else {
    Write-Host "No .env file found - using H2 + no-broker defaults baked into application.yml." -ForegroundColor DarkGray
}

# Make sure we're not pointing at Docker container names locally
$env:EUREKA_HOST = 'localhost'

# Resolve mvn and java in the PARENT shell so child windows don't depend on PATH inheritance.
# (Conda profiles / -NoProfile combinations can strip Chocolatey's bin dir from child PATH.)
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnCmd) {
    Write-Error "mvn not found on PATH in this shell. Run 'mvn -v' to confirm, then re-run this script."
    exit 1
}
$mvnExe = $mvnCmd.Source
$parentPath = $env:Path
Write-Host "Using mvn: $mvnExe" -ForegroundColor DarkGray

$services = @(
    @{ Name = 'eureka-server';        Color = 'Cyan'    },
    @{ Name = 'api-gateway';          Color = 'Magenta' },
    @{ Name = 'auth-service';         Color = 'Yellow'  },
    @{ Name = 'patient-service';      Color = 'Green'   },
    @{ Name = 'doctor-service';       Color = 'Green'   },
    @{ Name = 'appointment-service';  Color = 'Blue'    },
    @{ Name = 'billing-service';      Color = 'Blue'    },
    @{ Name = 'laboratory-service';   Color = 'Blue'    }
)

$backendDir = Join-Path $root 'backend'
$pidFile = Join-Path $PSScriptRoot '.running-pids.txt'
if (Test-Path $pidFile) { Remove-Item $pidFile -Force }

Write-Host '== Starting HMS backend services ==' -ForegroundColor Cyan
Write-Host "Project root: $root"
Write-Host "Backend dir:  $backendDir"
Write-Host ''

$first = $true
foreach ($svc in $services) {
    $name = $svc.Name
    $title = "HMS - $name"
    $escapedPath = $parentPath -replace "'", "''"
    $cmd = "`$env:Path = '$escapedPath'; Set-Location '$backendDir'; `$Host.UI.RawUI.WindowTitle = '$title'; Write-Host 'Starting $name...' -ForegroundColor $($svc.Color); & '$mvnExe' -pl $name spring-boot:run"

    $proc = Start-Process -FilePath 'powershell.exe' -ArgumentList '-NoProfile', '-NoExit', '-Command', $cmd -PassThru -WindowStyle Normal
    Add-Content -Path $pidFile -Value $proc.Id
    Write-Host ("  Started {0,-22} (PID {1})" -f $name, $proc.Id) -ForegroundColor $svc.Color

    if ($first) {
        Write-Host '    Eureka takes ~15s to register itself; waiting before starting the rest...' -ForegroundColor DarkGray
        Start-Sleep -Seconds 15
        $first = $false
    } else {
        Start-Sleep -Seconds 2
    }
}

Write-Host ''
Write-Host '== All services started ==' -ForegroundColor Cyan
Write-Host ''
Write-Host 'Wait ~60s for everything to register with Eureka, then visit:'
Write-Host '  Eureka:   http://localhost:8761'
Write-Host '  Gateway:  http://localhost:8080'
Write-Host '  Frontend: http://localhost:5173  (run separately: cd frontend; npm install; npm run dev)'
Write-Host ''
Write-Host "PIDs written to: $pidFile"
Write-Host 'When you are done, run: .\scripts\stop-backend.ps1'
