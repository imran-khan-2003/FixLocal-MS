$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $root "logs"
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

$services = @(
  @{ Name = "auth-service"; Pom = "auth-service/pom.xml"; Port = 8081 },
  @{ Name = "user-service"; Pom = "user-service/pom.xml"; Port = 8082 },
  @{ Name = "booking-service"; Pom = "booking-service/pom.xml"; Port = 8084 },
  @{ Name = "location-service"; Pom = "location-service/pom.xml"; Port = 8092 },
  @{ Name = "chat-service"; Pom = "chat-service/pom.xml"; Port = 8085 },
  @{ Name = "notification-service"; Pom = "notification-service/pom.xml"; Port = 8086 },
  @{ Name = "payment-service"; Pom = "payment-service/pom.xml"; Port = 8087 },
  @{ Name = "review-service"; Pom = "review-service/pom.xml"; Port = 8088 },
  @{ Name = "dispute-service"; Pom = "dispute-service/pom.xml"; Port = 8089 },
  @{ Name = "testimonial-service"; Pom = "testimonial-service/pom.xml"; Port = 8090 },
  @{ Name = "admin-service"; Pom = "admin-service/pom.xml"; Port = 8091 },
  @{ Name = "api-gateway"; Pom = "api-gateway/pom.xml"; Port = 8080 }
)

function Test-ServiceHealth([int]$port) {
  try {
    $resp = Invoke-WebRequest -UseBasicParsing -Uri ("http://localhost:{0}/actuator/health" -f $port) -TimeoutSec 3
    return $resp.StatusCode -eq 200
  }
  catch {
    return $false
  }
}

foreach ($svc in $services) {
  $pomPath = Join-Path $root $svc.Pom
  $logPath = Join-Path $logDir ("{0}.log" -f $svc.Name)

  if (Test-ServiceHealth $svc.Port) {
    Write-Host "$($svc.Name) is already healthy on port $($svc.Port). Skipping startup."
    continue
  }

  Write-Host "Starting $($svc.Name)..."

  "[$(Get-Date -Format o)] Starting $($svc.Name)" | Out-File -FilePath $logPath -Append
  $cmdLine = "/c mvn -Dmaven.test.skip=true -DskipTests -f `"$pomPath`" spring-boot:run >> `"$logPath`" 2>&1"
  Start-Process -FilePath "cmd.exe" -ArgumentList $cmdLine -WindowStyle Hidden
}

Write-Host "All startup commands issued."
Write-Host "Run .\\check-services.ps1 after ~30-90 seconds to verify health endpoints."
