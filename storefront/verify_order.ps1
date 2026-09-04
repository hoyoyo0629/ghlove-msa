param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\order"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-order"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9341","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9341/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9341/json/new?about:blank" -Method PUT
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
Send-CDP "Page.captureScreenshot" @{ format = "png" } | Out-Null
Receive-CDP $script:msgId 15 | Out-Null
Nav-And-Wait "http://localhost:5173/" 2

$loginResult = $null
for ($attempt = 1; $attempt -le 3 -and [string]::IsNullOrWhiteSpace($loginResult); $attempt++) {
    $loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
    if ([string]::IsNullOrWhiteSpace($loginResult)) { Start-Sleep -Seconds 1 }
}
Write-Output "LOGIN RESULT: $loginResult"

# 장바구니에 답례품 1건 추가 (강남구 item 1000)
$addResult = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/cart/items', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({itemId:1000, quantity:1})}); return r.status + ' ' + (await r.text()); })()"
Write-Output "CART ADD RESULT: $addResult"

Nav-And-Wait "http://localhost:5173/cart" 2
Shot "01-cart"

$cartItemId = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/cart'); const groups = await r.json(); return groups[0].lines[0].cartItemId; })()"
Write-Output "CART ITEM ID: $cartItemId"

Nav-And-Wait ("http://localhost:5173/checkout?cartItemId=" + $cartItemId) 2
Shot "02-checkout"

# 결제 완료 (API 직접 호출로 write 경로 검증)
$completeResult = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/checkout/complete', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({cartItemId:[$cartItemId], receiverName:'Tester', receiverPhone:'010-1111-2222', deliveryAddress:'Seoul Gangnam-gu', deliveryAddressDetail:'Unit 101', requestNote:'', couponByCartItem:{}})}); return r.status + ' ' + (await r.text()); })()"
Write-Output "CHECKOUT COMPLETE RESULT: $completeResult"

$orderIdsJson = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/orders'); const orders = await r.json(); return JSON.stringify(orders.slice(0,1).map(o => o.orderId)); })()"
Write-Output "LATEST ORDER: $orderIdsJson"
$orderId = ($orderIdsJson | ConvertFrom-Json)[0]

Nav-And-Wait ("http://localhost:5173/checkout/done?orderIds=" + $orderId) 2
Shot "03-order-complete"

Nav-And-Wait "http://localhost:5173/orders" 2
Shot "04-my-orders"

# 주문확정까지 대기 (choreography SAGA 비동기 처리)
$status = ""
for ($i = 0; $i -lt 10 -and $status -ne "CONFIRMED"; $i++) {
    Start-Sleep -Seconds 1
    $status = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/orders/$orderId'); const o = await r.json(); return o.orderStatus; })()"
}
Write-Output "ORDER STATUS: $status"

Nav-And-Wait ("http://localhost:5173/orders/" + $orderId) 2
Shot "05-order-detail"

Nav-And-Wait "http://localhost:5173/claims/my" 2
Shot "06-claims-my"

Nav-And-Wait "http://localhost:5173/my/coupons" 2
Shot "07-my-coupons"

Nav-And-Wait "http://localhost:5173/coupons" 2
Shot "08-coupons-claimable"

Nav-And-Wait "http://localhost:5173/coupons/offline" 2
Shot "09-coupons-offline"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
