param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\order"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-cart-int"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9342","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9342/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9342/json/new?about:blank" -Method PUT
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

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

$loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN RESULT: $loginResult"

# 두 지자체 답례품을 각각 담아 다중 그룹 화면을 만든다 (26350 해운대는 포인트 0 -> 주문불가 케이스도 같이 검증)
Eval "(async () => { await fetch('http://localhost:5173/order/api/cart/items', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({itemId:1000, quantity:1})}); await fetch('http://localhost:5173/order/api/cart/items', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({itemId:1001, quantity:1})}); return 'ok'; })()" | Out-Null

Nav-And-Wait "http://localhost:5173/cart" 2
Shot "10-cart-two-groups"

# 첫 번째 라인 수량을 2로 변경 -> '변경' 버튼 클릭 (이 액션은 내부적으로 load()를 다시 불러
# selected를 전체선택으로 리셋한다 - 그래서 체크해제보다 먼저 실행한다)
$qtyResult = Eval "(function(){ var inputs = document.querySelectorAll('input[type=number]'); if(inputs.length===0) return 'NO_QTY_INPUT'; var input = inputs[0]; input.value = '2'; input.dispatchEvent(new Event('input', {bubbles:true})); var btn = input.parentElement.querySelector('button.modify'); if(!btn) return 'NO_BTN'; btn.click(); return 'clicked'; })()"
Write-Output "QTY CHANGE CLICK: $qtyResult"
Start-Sleep -Seconds 1
Shot "12-cart-after-qty-change"

$cartAfter = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/cart'); return JSON.stringify(await r.json()); })()"
Write-Output "CART STATE: $cartAfter"

# 첫 번째 그룹 체크 해제(한글 텍스트 매칭은 CDP 왕복시 인코딩이 깨질 수 있어
# [[feedback_bash_curl_korean_encoding]]과 동일 함정을 피하려 인덱스로만 선택) -> 선택삭제
# 클릭 시 이 그룹은 남고 나머지 그룹만 삭제되어야 한다.
$uncheckResult = Eval "(function(){ var groups = document.querySelectorAll('.cart-group'); if(groups.length===0) return 'NOT_FOUND'; var target = groups[0].querySelector('.cart_top input[type=checkbox]'); if(!target) return 'NO_CHECKBOX'; target.click(); return 'clicked:' + target.checked; })()"
Write-Output "UNCHECK GROUP: $uncheckResult"
Shot "11-cart-first-group-unchecked"

$deleteResult = Eval "(function(){ var btn = document.querySelector('button.del'); if(!btn) return 'NO_DELETE_BTN'; btn.click(); return 'clicked'; })()"
Write-Output "DELETE CLICK: $deleteResult"
Start-Sleep -Seconds 1
Shot "13-cart-after-delete-selected"

$cartFinal = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/cart'); return JSON.stringify(await r.json()); })()"
Write-Output "CART FINAL: $cartFinal"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
