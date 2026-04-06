$ErrorActionPreference = 'Continue'

$services = @(
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

$allHealthy = $true

foreach ($svc in $services) {
  $url = "http://localhost:$($svc.Port)/actuator/health"

  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 3
    if ($response.StatusCode -eq 200) {
      Write-Host "[UP]   $($svc.Name) on :$($svc.Port)" -ForegroundColor Green
    }
    else {
      Write-Host "[DOWN] $($svc.Name) on :$($svc.Port) (HTTP $($response.StatusCode))" -ForegroundColor Red
      $allHealthy = $false
    }
  }
  catch {
    Write-Host "[DOWN] $($svc.Name) on :$($svc.Port)" -ForegroundColor Red
    $allHealthy = $false
  }
}

if ($allHealthy) {
  Write-Host "All microservices are healthy." -ForegroundColor Green
  exit 0
}

Write-Host "Some services are down. Check microservices/logs/*.log" -ForegroundColor Yellow
exit 1
