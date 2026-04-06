$ErrorActionPreference = 'Continue'

$microRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $microRoot
$frontendDir = Join-Path $projectRoot "fixlocal-frontend"
$logDir = Join-Path $microRoot "logs"
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

$backendServices = @(
  @{ Name = "api-gateway"; Port = 8080 },
  @{ Name = "auth-service"; Port = 8081 },
  @{ Name = "user-service"; Port = 8082 },
  @{ Name = "booking-service"; Port = 8084 },
  @{ Name = "chat-service"; Port = 8085 },
  @{ Name = "notification-service"; Port = 8086 },
  @{ Name = "payment-service"; Port = 8087 },
  @{ Name = "review-service"; Port = 8088 },
  @{ Name = "dispute-service"; Port = 8089 },
  @{ Name = "testimonial-service"; Port = 8090 },
  @{ Name = "admin-service"; Port = 8091 }
)

$portsToClear = @($backendServices | ForEach-Object { $_.Port }) + @(5173)
$stopped = @{}

Write-Host "Stopping old service/frontend processes (if any)..."
foreach ($port in $portsToClear) {
  $pids = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique

  foreach ($procId in $pids) {
    if (-not $stopped.ContainsKey($procId)) {
      try {
        Stop-Process -Id $procId -Force -ErrorAction Stop
        $stopped[$procId] = $true
        Write-Host "Stopped PID $procId (port $port)"
      }
      catch {
        Write-Host "Could not stop PID $procId (port $port): $($_.Exception.Message)"
      }
    }
  }
}

if ($stopped.Count -eq 0) {
  Write-Host "No existing listeners found on backend/frontend ports."
}

Write-Host "Starting microservices..."
& (Join-Path $microRoot "start-all-services.ps1")

Write-Host "Waiting for backend services to warm up..."
Start-Sleep -Seconds 45

Write-Host "Starting frontend (Vite)..."
$frontendLog = Join-Path $logDir "frontend.log"
"[$(Get-Date -Format o)] Starting frontend" | Out-File -FilePath $frontendLog -Append
$frontendCmd = "/c cd /d `"$frontendDir`" && npm run dev >> `"$frontendLog`" 2>&1"
Start-Process -FilePath "cmd.exe" -ArgumentList $frontendCmd -WindowStyle Hidden

Write-Host "Waiting for frontend to boot..."
Start-Sleep -Seconds 8

Write-Host "Checking backend health..."
$allBackendHealthy = $true
foreach ($svc in $backendServices) {
  $url = "http://localhost:$($svc.Port)/actuator/health"

  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 4
    if ($response.StatusCode -eq 200) {
      Write-Host "[UP]   $($svc.Name) on :$($svc.Port)"
    }
    else {
      Write-Host "[DOWN] $($svc.Name) on :$($svc.Port) (HTTP $($response.StatusCode))"
      $allBackendHealthy = $false
    }
  }
  catch {
    Write-Host "[DOWN] $($svc.Name) on :$($svc.Port)"
    $allBackendHealthy = $false
  }
}

Write-Host "Checking frontend health..."
$frontendHealthy = $false

for ($i = 0; $i -lt 12; $i++) {
  if (Test-NetConnection -ComputerName "127.0.0.1" -Port 5173 -InformationLevel Quiet) {
    $frontendHealthy = $true
    break
  }
  Start-Sleep -Seconds 2
}

if ($frontendHealthy) {
  for ($i = 0; $i -lt 4; $i++) {
    try {
      $frontendResp = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:5173" -TimeoutSec 4
      if ($frontendResp.StatusCode -ge 200 -and $frontendResp.StatusCode -lt 500) {
        $frontendHealthy = $true
        break
      }
    }
    catch {
      Start-Sleep -Seconds 2
    }
  }
}

if (-not $frontendHealthy) {
  Write-Host "[DOWN] frontend on :5173"
} else {
  try {
    $frontendResp = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:5173" -TimeoutSec 4
    if ($frontendResp.StatusCode -ge 200 -and $frontendResp.StatusCode -lt 500) {
      $frontendHealthy = $true
    }
  }
  catch {
    # Ignore occasional HTTP probe failures if TCP listener is already up.
  }
}

if ($frontendHealthy) {
  Write-Host "[UP]   frontend on :5173"
} else {
  Write-Host "[DOWN] frontend on :5173"
}

if ($allBackendHealthy -and $frontendHealthy) {
  Write-Host "Restart complete: all microservices + frontend are up."
  exit 0
}

Write-Host "Restart finished, but some processes are still down. Check microservices/logs/*.log"
exit 1
