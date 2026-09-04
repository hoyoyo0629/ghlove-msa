param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\mypage"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-mypage"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9340","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9340/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9340/json/new?about:blank" -Method PUT
$wsUrl = $tab.webSocketDebuggerUrl
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$cts = New-Object System.Threading.CancellationTokenSource
$ws.ConnectAsync([Uri]$wsUrl, $cts.Token).Wait()

$script:msgId = 0
function Send-CDP($method, $params) {
    $script:msgId++
    $id = $script:msgId
    $obj = @{ id = $id; method = $method; params = $params }
    $json = $obj | ConvertTo-Json -Depth 10 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $seg = New-Object System.ArraySegment[byte] (,$bytes)
    $ws.SendAsync($seg, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $cts.Token).Wait()
    return $id
}
function Receive-FullMessage($timeoutMs = 2000) {
    $chunkSize = 1MB
    $ms = New-Object System.IO.MemoryStream
    $buffer = New-Object byte[] $chunkSize
    do {
        $seg = New-Object System.ArraySegment[byte] (,$buffer)
        $task = $ws.ReceiveAsync($seg, $cts.Token)
        if (-not $task.Wait($timeoutMs)) { return $null }
        $result = $task.Result
        if ($result.Count -gt 0) { $ms.Write($buffer, 0, $result.Count) }
    } while (-not $result.EndOfMessage)
    return [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
}
function Receive-CDP($expectId, $timeoutSec = 20) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        $text = Receive-FullMessage 2000
        if ($null -ne $text -and $text.Length -gt 0) {
            try { $obj = $text | ConvertFrom-Json; if ($obj.id -eq $expectId) { return $obj } } catch { }
        }
    }
    return $null
}
function Nav-And-Wait($url, $waitSec = 2) {
    $id = Send-CDP "Page.navigate" @{ url = $url }
    Receive-CDP $id | Out-Null
    Start-Sleep -Seconds $waitSec
}
function Eval($expr, $timeoutSec = 15) {
    $id = Send-CDP "Runtime.evaluate" @{ expression = $expr; awaitPromise = $true; returnByValue = $true }
    $resp = Receive-CDP $id $timeoutSec
    if ($null -ne $resp.result.exceptionDetails) { return "EXC: " + ($resp.result.exceptionDetails | ConvertTo-Json -Depth 10 -Compress) }
    return $resp.result.result.value
}
function Shot($name) {
    Eval "document.body.offsetHeight" | Out-Null
    $id = Send-CDP "Page.captureScreenshot" @{ format = "png"; fromSurface = $true }
    $shot = Receive-CDP $id 15
    if ($null -eq $shot -or $null -eq $shot.result.data) { Write-Output "FAILED (no data): $name"; return }
    $bytes = [Convert]::FromBase64String($shot.result.data)
    [IO.File]::WriteAllBytes((Join-Path $OutDir "$name.png"), $bytes)
    Write-Output "SAVED: $name ($($bytes.Length) bytes)"
}

Send-CDP "Page.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Runtime.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 2200; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

# 워밍업 - 첫 네비게이션 직후는 문서가 아직 about:blank인 채로 Runtime.evaluate가 실행되는
# 경우가 있었다(재현됨: relative fetch URL 파싱 실패). 두 번째 네비게이션부터는 항상 안정적이라
# '/'를 한 번 더 갔다와서 실제 페이지 컨텍스트가 자리잡을 시간을 준다.
Nav-And-Wait "http://localhost:5173/" 3
Send-CDP "Page.captureScreenshot" @{ format = "png" } | Out-Null
Receive-CDP $script:msgId 15 | Out-Null
Nav-And-Wait "http://localhost:5173/" 2

# 로그인 - 상대경로 fetch가 문서 컨텍스트 타이밍에 따라 간헐적으로 깨져서(재현됨) 절대
# URL로 고정하고, 응답이 안 올 때를 대비해 재시도도 둔다.
$loginResult = $null
for ($attempt = 1; $attempt -le 3 -and [string]::IsNullOrWhiteSpace($loginResult); $attempt++) {
    $loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
    if ([string]::IsNullOrWhiteSpace($loginResult)) { Start-Sleep -Seconds 1 }
}
Write-Output "LOGIN RESULT: $loginResult"

$pages = @(
    @{ path = "/mypage"; name = "01-mypage-hub" },
    @{ path = "/mypage/profile"; name = "02-profile" },
    @{ path = "/mypage/delivery"; name = "03-delivery-list" },
    @{ path = "/mypage/delivery/new"; name = "04-delivery-new" },
    @{ path = "/mypage/interest-locgovs"; name = "05-interest-locgovs" },
    @{ path = "/mypage/honor-certificates"; name = "06-honor-certificates" },
    @{ path = "/mypage/donations"; name = "07-donations" },
    @{ path = "/mypage/wishlist"; name = "08-wishlist" },
    @{ path = "/mypage/points"; name = "09-points" },
    @{ path = "/mypage/qna"; name = "10-qna" }
)

foreach ($p in $pages) {
    Nav-And-Wait ("http://localhost:5173" + $p.path) 2
    $errText = Eval "(function(){var e=document.querySelector('vite-error-overlay'); return e ? 'VITE_ERROR' : 'ok';})()"
    if ($errText -ne "ok") { Write-Output "$($p.name): $errText" }
    Shot $p.name
}

# 배송지 등록 write 경로 스모크 테스트
Nav-And-Wait "http://localhost:5173/mypage/delivery" 2
$createResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/delivery', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({title:'Home', userName:'Tester', mobile:'010-1111-2222', address:'Gangnam-gu Seoul', addressDetail:'Unit 101', makeDefault:true})}); return JSON.stringify(await r.json()); })()"
Write-Output "DELIVERY CREATE RESULT: $createResult"
Nav-And-Wait "http://localhost:5173/mypage/delivery" 2
Shot "11-delivery-after-create"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
