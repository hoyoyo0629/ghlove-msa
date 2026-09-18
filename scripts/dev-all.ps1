<#
  로컬 백엔드 서비스 일괄 기동 스크립트.
  각 서비스를 gradlew bootRun 으로 백그라운드 실행하고 표준출력/에러를
  logs\<서비스>.log 한 파일로 모은다. 이미 포트가 LISTEN 이면 건너뛴다.

  사용 예:
    .\scripts\dev-all.ps1                 # 백엔드 6종만 기동
    .\scripts\dev-all.ps1 -Front          # + storefront(vite 5173)
    .\scripts\dev-all.ps1 -Infra          # + docker(postgres/kafka) 먼저 기동
    .\scripts\dev-all.ps1 -Only member,donation   # 지정 서비스만
    .\scripts\dev-all.ps1 -Infra -Front   # 인프라 + 백엔드 + 프론트 한 번에
#>
param(
  [string[]]$Only,
  [switch]$Front,
  [switch]$Infra
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

# 서비스명 -> 포트 (기동 순서 = 선언 순서)
$services = [ordered]@{ member = 8081; donation = 8082; point = 8083; gift = 8084; order = 8085; admin = 8086 }

$logDir = Join-Path $root 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Test-PortUp([int]$Port) {
  $c = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  return [bool]$c
}

function Start-InBackground([string]$Name, [string]$WorkDir, [string]$Cmd, [string]$Log) {
  # Windows PowerShell 5.1 의 Start-Process 인자 인용이 && 나 리다이렉션(>)을 깨뜨리므로,
  # cd + 실행 + 표준출력/에러 병합리다이렉션을 담은 런처 .cmd 를 만들어 숨김 실행한다.
  # (모든 경로/명령이 파일 안에 리터럴로 들어가 따옴표 문제를 원천 차단하고, 로그도 한 파일로 합쳐진다)
  $runDir = Join-Path (Split-Path $Log -Parent) '_run'
  New-Item -ItemType Directory -Force -Path $runDir | Out-Null
  $launcher = Join-Path $runDir "$Name.cmd"
  $body = "@echo off`r`ncd /d `"$WorkDir`"`r`ncall $Cmd 1> `"$Log`" 2>&1`r`n"
  Set-Content -Path $launcher -Value $body -Encoding ASCII
  Start-Process -FilePath $launcher -WindowStyle Hidden | Out-Null
}

if ($Infra) {
  Write-Host "[infra] docker 컨테이너 기동 (ghlove-postgres, ghlove-kafka)" -ForegroundColor Cyan
  docker start ghlove-postgres ghlove-kafka | Out-Null
}

$targets = @($services.Keys)
if ($Only) { $targets = @($services.Keys | Where-Object { $Only -contains $_ }) }

foreach ($name in $targets) {
  $port = $services[$name]
  $svcPath = Join-Path $root $name
  $log = Join-Path $logDir "$name.log"
  if (Test-PortUp $port) {
    Write-Host ("[{0,-9}] 이미 :{1} LISTEN — 건너뜀" -f $name, $port) -ForegroundColor Yellow
    continue
  }
  if (-not (Test-Path (Join-Path $svcPath 'gradlew.bat'))) {
    Write-Host ("[{0,-9}] gradlew.bat 없음 — 건너뜀" -f $name) -ForegroundColor Red
    continue
  }
  Write-Host ("[{0,-9}] 기동 :{1}   로그 -> logs\{0}.log" -f $name, $port) -ForegroundColor Green
  # 이 환경의 cmd 는 현재 디렉터리를 실행 경로로 검색하지 않으므로(NoDefaultCurrentDirectoryInExePath),
  # cd 후에도 반드시 명시적 상대경로 .\gradlew.bat 로 호출해야 한다.
  Start-InBackground $name $svcPath '.\gradlew.bat bootRun' $log
}

if ($Front) {
  $fePort = 5173
  $fePath = Join-Path $root 'storefront'
  if (Test-PortUp $fePort) {
    Write-Host "[storefront] 이미 :5173 — 건너뜀" -ForegroundColor Yellow
  } else {
    $feLog = Join-Path $logDir 'storefront.log'
    Write-Host "[storefront] 기동 :5173   로그 -> logs\storefront.log" -ForegroundColor Green
    Start-InBackground 'storefront' $fePath 'npm run dev' $feLog
  }
}

Write-Host ""
Write-Host "기동 명령 전송 완료. 스프링 부팅에 서비스당 20~40초 걸립니다." -ForegroundColor Cyan
Write-Host "  상태확인 : .\scripts\dev-status.ps1" -ForegroundColor Cyan
Write-Host "  로그보기 : .\scripts\dev-logs.ps1 member" -ForegroundColor Cyan
Write-Host "  전체중지 : .\scripts\dev-stop.ps1" -ForegroundColor Cyan
