param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\gift"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-gift"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9343","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9343/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9343/json/new?about:blank" -Method PUT
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
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 2400; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

$loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN RESULT: $loginResult"

Nav-And-Wait "http://localhost:5173/gifts" 2
Shot "01-gift-list-all"

Nav-And-Wait "http://localhost:5173/gifts?categoryCode=AGRI" 2
Shot "02-gift-list-agri"

$searchResult = Eval "(async () => { const r = await fetch('http://localhost:5173/gift/api/gifts?q=%ED%95%9C%EC%9A%B0'); return JSON.stringify(await r.json()); })()"
Write-Output "SEARCH RESULT (q=%ED%95%9C%EC%9A%B0 = 한우): $searchResult"

Nav-And-Wait "http://localhost:5173/gifts/seasonal" 2
Shot "03-gift-seasonal"

Nav-And-Wait "http://localhost:5173/gifts/community-business" 2
Shot "04-gift-community-business"

Nav-And-Wait "http://localhost:5173/gifts/1000" 2
Shot "05-gift-detail-1000"

$wishlistClick = Eval "(function(){ var btn = document.querySelector('.wishBtn'); if(!btn) return 'NOT_FOUND'; btn.click(); return 'clicked'; })()"
Write-Output "WISHLIST TOGGLE CLICK: $wishlistClick"
Start-Sleep -Seconds 1
Shot "06-gift-detail-wishlisted"

$reviewTabClick = Eval "(function(){ var links = document.querySelectorAll('.nav-tabs .nav-link'); if(links.length < 2) return 'NOT_FOUND'; links[1].click(); return 'clicked'; })()"
Write-Output "REVIEW TAB CLICK: $reviewTabClick"
Start-Sleep -Milliseconds 800
Shot "07-gift-detail-review-tab"

$reviewSubmit = Eval "(function(){ var ids=['subject','content','score']; var vals=[String.fromCharCode(51221,47568,32,51339,50500,50836), String.fromCharCode(48176,49569,46020,32,48736,47476,44256,32,54408,51656,46020,32,51339,49845,45768,45796,46), '5']; for (var i=0;i<ids.length;i++){ var el=document.getElementById(ids[i]); if(!el) return 'NOT_FOUND:'+ids[i]; el.value=vals[i]; el.dispatchEvent(new Event('input',{bubbles:true})); } var form = document.querySelector('#nav-review form'); if(!form) return 'NO_FORM'; var btn = form.querySelector('button[type=submit]'); btn.click(); return 'clicked'; })()"
Write-Output "REVIEW SUBMIT CLICK: $reviewSubmit"
Start-Sleep -Seconds 1
Shot "08-gift-detail-review-submitted"

$qnaTabClick = Eval "(function(){ var links = document.querySelectorAll('.nav-tabs .nav-link'); if(links.length < 3) return 'NOT_FOUND'; links[2].click(); return 'clicked'; })()"
Write-Output "QNA TAB CLICK: $qnaTabClick"
Start-Sleep -Milliseconds 800

$qnaSubmit = Eval "(function(){ var q = document.getElementById('question'); if(!q) return 'NOT_FOUND'; q.value=String.fromCharCode(48176,49569,51008,32,50620,47560,45208,32,44152,47532,45208,50836,63); q.dispatchEvent(new Event('input',{bubbles:true})); var form = document.querySelector('#nav-qna form'); var btn = form.querySelector('button[type=submit]'); btn.click(); return 'clicked'; })()"
Write-Output "QNA SUBMIT CLICK: $qnaSubmit"
Start-Sleep -Seconds 1
Shot "09-gift-detail-qna-submitted"

$cartAddResult = Eval "(function(){ var btn = document.querySelector('.buyBtn.addToCart'); if(!btn) return 'NOT_FOUND'; window.confirm = function(){ return false; }; btn.click(); return 'clicked'; })()"
Write-Output "ADD TO CART CLICK: $cartAddResult"
Start-Sleep -Seconds 1

$cartState = Eval "(async () => { const r = await fetch('http://localhost:5173/order/api/cart'); return JSON.stringify(await r.json()); })()"
Write-Output "CART STATE AFTER ADD: $cartState"

Nav-And-Wait "http://localhost:5173/gifts/1004" 2
Shot "10-gift-detail-1004-hoengseong"

Nav-And-Wait "http://localhost:5173/mypage/wishlist" 2
Shot "11-mypage-wishlist"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
