<#
  로컬 백엔드 서비스 일괄 중지 스크립트.
  각 포트를 LISTEN 하고 있는 프로세스를 프로세스 트리째 종료한다(taskkill /T /F).

  사용 예:
    .\scripts\dev-stop.ps1            # 백엔드 6종 중지
    .\scripts\dev-stop.ps1 -Front     # + storefront(5173)
    .\scripts\dev-stop.ps1 -Daemon    # + gradle 데몬까지 정리
#>
param(
  [switch]$Front,
  [switch]$Daemon
)
$root = Split-Path -Parent $PSScriptRoot
$services = [ordered]@{ member = 8081; donation = 8082; point = 8083; gift = 8084; order = 8085; admin = 8086 }

$ports = @($services.Values)
if ($Front) { $ports += 5173 }

foreach ($port in $ports) {
  $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  if (-not $conns) {
    Write-Host (":{0,-5} 미기동" -f $port) -ForegroundColor DarkGray
    continue
  }
  $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
  foreach ($procId in $pids) {
    Write-Host (":{0,-5} 종료 (PID {1})" -f $port, $procId) -ForegroundColor Yellow
    taskkill /PID $procId /T /F 2>$null | Out-Null
  }
}

if ($Daemon) {
  Write-Host "gradle 데몬 정리 (member gradlew --stop)" -ForegroundColor Cyan
  $anySvc = Join-Path $root 'member'
  if (Test-Path (Join-Path $anySvc 'gradlew.bat')) {
    Start-Process -FilePath cmd.exe -ArgumentList '/c', 'gradlew.bat --stop' -WorkingDirectory $anySvc -WindowStyle Hidden -Wait | Out-Null
  }
}

Write-Host ""
Write-Host "중지 완료. 상태확인: .\scripts\dev-status.ps1" -ForegroundColor Cyan
