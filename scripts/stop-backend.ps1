# Stops the backend services that start-backend.ps1 spawned.
# Reads PIDs from scripts\.running-pids.txt and kills the spawned PowerShell windows
# (which in turn kills the mvn / java child processes they're hosting).

$ErrorActionPreference = 'Continue'

$pidFile = Join-Path $PSScriptRoot '.running-pids.txt'
if (-not (Test-Path $pidFile)) {
    Write-Host 'No PID file found at scripts\.running-pids.txt - nothing to stop.'
    Write-Host 'If you have rogue java processes, you can run:'
    Write-Host '  Get-Process java | Stop-Process -Force'
    return
}

$ids = Get-Content $pidFile | Where-Object { $_ -match '^\d+$' }
foreach ($id in $ids) {
    try {
        # Kill the PowerShell host and any descendants (mvn, java, etc.)
        $proc = Get-CimInstance Win32_Process -Filter "ParentProcessId=$id" -ErrorAction SilentlyContinue
        foreach ($child in $proc) {
            try { Stop-Process -Id $child.ProcessId -Force -ErrorAction SilentlyContinue } catch {}
        }
        Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
        Write-Host "  stopped PID $id" -ForegroundColor Yellow
    } catch {
        Write-Host "  PID $id was not running"
    }
}

Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
Write-Host '== Done =='

# Belt-and-braces: kill leftover java processes hosting Spring Boot
Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    try { ($_.CommandLine -match 'spring-boot') } catch { $false }
} | ForEach-Object {
    Write-Host "  killing leftover java PID $($_.Id)"
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}
