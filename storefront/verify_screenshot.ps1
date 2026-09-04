param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-storefront"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9334","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try {
        $v = Invoke-RestMethod -Uri "http://127.0.0.1:9334/json/version" -TimeoutSec 1
        $ready = $true
        break
    } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9334/json/new?about:blank" -Method PUT
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
            try {
                $obj = $text | ConvertFrom-Json
                if ($obj.id -eq $expectId) { return $obj }
            } catch { }
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
    return $resp.result.result.value
}

function Shot($name) {
    # 스크린샷 직전에 더미 Eval 왕복을 한 번 넣어 컴포지터 프레임이 실제로 준비된 뒤에
    # 캡처하게 한다 - Page.navigate 직후 바로 캡처하면 headless가 빈 화면을 반환하는 경우가 있었음.
    Eval "document.body.offsetHeight" | Out-Null
    $id = Send-CDP "Page.captureScreenshot" @{ format = "png"; fromSurface = $true; captureBeyondViewport = $false }
    $shot = Receive-CDP $id 15
    if ($null -eq $shot -or $null -eq $shot.result.data) { Write-Output "FAILED (no data): $name"; return }
    $bytes = [Convert]::FromBase64String($shot.result.data)
    [IO.File]::WriteAllBytes((Join-Path $OutDir "$name.png"), $bytes)
    Write-Output "SAVED: $name"
}

Send-CDP "Page.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Runtime.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 2200; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

# 새로 만든 탭의 첫 스크린샷은 headless Chrome의 첫 컴포지터 프레임이 아직 준비되기 전에
# 캡처되어 빈 화면이 저장되는 경우가 있었다(재현 확인됨) - 실제로 쓸 스크린샷을 찍기 전에
# 버릴 워밍업 캡처를 한 번 해서 렌더 파이프라인을 한 바퀴 돌려둔다.
Nav-And-Wait "http://localhost:5173/" 3
Send-CDP "Page.captureScreenshot" @{ format = "png" } | Out-Null
Receive-CDP $script:msgId 15 | Out-Null
Start-Sleep -Seconds 1

# 1) 홈
Nav-And-Wait "http://localhost:5173/" 2
Shot "01-home"

# 2) 로그인 화면
Nav-And-Wait "http://localhost:5173/login" 2
Shot "02-login"

# 3) 로그인 시도 (testuser01 / 테스트 계정 - 실패해도 화면 확인용)
$loginResult = Eval "(async () => { const r = await fetch('/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()"
Write-Output "LOGIN RESULT: $loginResult"

Nav-And-Wait "http://localhost:5173/" 2
Shot "03-home-after-login-attempt"

# 4) 회원가입 화면
Nav-And-Wait "http://localhost:5173/signup" 2
Shot "04-signup"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
