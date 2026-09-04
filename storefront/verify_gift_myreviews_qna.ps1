param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\gift-mypage"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-giftmypage"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9353","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9353/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9353/json/new?about:blank" -Method PUT
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
    $ms = New-Object System.IO.MemoryStream
    $buffer = New-Object byte[] 1MB
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
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 1400; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

$loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN RESULT: $loginResult"

Nav-And-Wait "http://localhost:5173/mypage" 2
$cardsCheck = Eval '(function(){ var links = Array.from(document.querySelectorAll("a")); var review = links.find(function(a){ return a.getAttribute("href") === "/mypage/gift-reviews"; }); var qna = links.find(function(a){ return a.getAttribute("href") === "/mypage/gift-qna"; }); return (review ? "review:OK" : "review:MISSING") + "," + (qna ? "qna:OK" : "qna:MISSING"); })()'
Write-Output "MYPAGE CARDS: $cardsCheck"
Shot "01-mypage-dashboard"

Nav-And-Wait "http://localhost:5173/mypage/gift-reviews" 2
Shot "02-gift-reviews"
$reviewRowCount = Eval "document.querySelectorAll('.item_list-group tr.list-items').length"
Write-Output "REVIEW ROWS: $reviewRowCount"

Nav-And-Wait "http://localhost:5173/mypage/gift-qna" 2
Shot "03-gift-qna"
$qnaRowCount = Eval "document.querySelectorAll('.item_list-group tr.list-items').length"
Write-Output "QNA ROWS: $qnaRowCount"

$filterCheck = Eval '(function(){ var input = document.getElementById("itemName"); if(!input) return "NOT_FOUND"; input.value = "test"; input.dispatchEvent(new Event("input")); var btns = document.querySelectorAll(".btn-box.many.mid button"); if(btns.length < 2) return "BTN_NOT_FOUND"; btns[1].click(); return "clicked"; })()'
Write-Output "QNA FILTER TEST: $filterCheck"
Start-Sleep -Milliseconds 500
Shot "04-gift-qna-filtered"

Nav-And-Wait "http://localhost:5173/" 2
$footerCheck = Eval '(function(){ var open = document.querySelector("a.open-modal"); if(open) open.click(); return "clicked-sitemap-open"; })()'
Write-Output "SITEMAP OPEN: $footerCheck"
Start-Sleep -Milliseconds 500
$footerLinks = Eval '(function(){ var links = Array.from(document.querySelectorAll(".sitemap__col--mypage a")); var r = links.find(function(a){ return a.getAttribute("href") === "/mypage/gift-reviews"; }); var q = links.find(function(a){ return a.getAttribute("href") === "/mypage/gift-qna"; }); return (r ? "footer-review:OK" : "footer-review:MISSING") + "," + (q ? "footer-qna:OK" : "footer-qna:MISSING"); })()'
Write-Output "FOOTER SITEMAP LINKS: $footerLinks"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
