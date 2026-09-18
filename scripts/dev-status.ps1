<#
  로컬 개발환경 상태 점검. 각 백엔드 포트/프론트/도커 컨테이너의 기동 여부를 표로 보여준다.
  사용: .\scripts\dev-status.ps1
#>
$services = [ordered]@{ member = 8081; donation = 8082; point = 8083; gift = 8084; order = 8085; admin = 8086 }

function Get-PortState([int]$Port) {
  $c = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($c) { return [pscustomobject]@{ Up = $true; Pid = $c.OwningProcess } }
  return [pscustomobject]@{ Up = $false; Pid = $null }
}

Write-Host ""
Write-Host ("{0,-12} {1,-6} {2,-6} {3}" -f '서비스', '포트', '상태', 'PID') -ForegroundColor White
Write-Host ("-" * 34)
foreach ($name in $services.Keys) {
  $st = Get-PortState $services[$name]
  if ($st.Up) {
    Write-Host ("{0,-12} {1,-6} {2,-6} {3}" -f $name, $services[$name], 'UP', $st.Pid) -ForegroundColor Green
  } else {
    Write-Host ("{0,-12} {1,-6} {2,-6} {3}" -f $name, $services[$name], 'DOWN', '-') -ForegroundColor DarkGray
  }
}
# 프론트
$fe = Get-PortState 5173
if ($fe.Up) { Write-Host ("{0,-12} {1,-6} {2,-6} {3}" -f 'storefront', 5173, 'UP', $fe.Pid) -ForegroundColor Green }
else { Write-Host ("{0,-12} {1,-6} {2,-6} {3}" -f 'storefront', 5173, 'DOWN', '-') -ForegroundColor DarkGray }

# 인프라(docker)
Write-Host ""
Write-Host "인프라(docker):" -ForegroundColor White
$names = @('ghlove-postgres', 'ghlove-kafka')
foreach ($n in $names) {
  $running = docker ps --filter "name=$n" --format "{{.Names}}" 2>$null
  if ($running -eq $n) { Write-Host ("  {0,-18} UP" -f $n) -ForegroundColor Green }
  else { Write-Host ("  {0,-18} DOWN" -f $n) -ForegroundColor DarkGray }
}
Write-Host ""
