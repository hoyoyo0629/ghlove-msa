<#
  서비스 로그를 실시간(tail -f)으로 본다. dev-all.ps1 로 기동했을 때 생기는 logs\<서비스>.log 를 읽는다.

  사용 예:
    .\scripts\dev-logs.ps1 member         # member 로그 마지막 80줄 + 실시간
    .\scripts\dev-logs.ps1 donation -Tail 200
    .\scripts\dev-logs.ps1 storefront
  (Ctrl+C 로 빠져나오면 서비스는 계속 떠 있음 — 로그 보기만 중단)
#>
param(
  [Parameter(Mandatory = $true, Position = 0)]
  [string]$Service,
  [int]$Tail = 80
)
$root = Split-Path -Parent $PSScriptRoot
$log = Join-Path $root "logs\$Service.log"
if (-not (Test-Path $log)) {
  Write-Host "로그 파일이 없습니다: $log" -ForegroundColor Red
  Write-Host "먼저 .\scripts\dev-all.ps1 로 기동했는지, 서비스명이 맞는지 확인하세요." -ForegroundColor Yellow
  Write-Host "가능한 서비스: member donation point gift order admin storefront" -ForegroundColor Yellow
  exit 1
}
Write-Host "==== $log (Ctrl+C 로 종료) ====" -ForegroundColor Cyan
Get-Content -Path $log -Tail $Tail -Wait
