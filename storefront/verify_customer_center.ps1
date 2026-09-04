param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\customer-center"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-customercenter"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9359","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9359/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9359/json/new?about:blank" -Method PUT
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
    if ($ws.State -ne [System.Net.WebSockets.WebSocketState]::Open) { throw "WebSocket not open (state=$($ws.State))" }
    $ms = New-Object System.IO.MemoryStream
    $buffer = New-Object byte[] 1MB
    do {
        $seg = New-Object System.ArraySegment[byte] (,$buffer)
        $task = $ws.ReceiveAsync($seg, $cts.Token)
        try { if (-not $task.Wait($timeoutMs)) { return $null } } catch { throw "receive failed: $($_.Exception.InnerException.Message)" }
        $result = $task.Result
        if ($result.Count -gt 0) { $ms.Write($buffer, 0, $result.Count) }
    } while (-not $result.EndOfMessage)
    return [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
}
function Receive-CDP($expectId, $timeoutSec = 20) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        try { $text = Receive-FullMessage 2000 } catch { Write-Output "SOCKET ERROR: $($_.Exception.Message)"; return $null }
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
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1280; height = 1600; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

$login = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN: $login"

# 1. GNB header links -> internal routes
Nav-And-Wait "http://localhost:5173/" 2
$gnbCheck = Eval @'
(function(){
  var links = Array.from(document.querySelectorAll('a'));
  var wanted = ['공지사항','자료실','FAQ'];
  var found = {};
  links.forEach(function(a){ if (wanted.indexOf(a.textContent.trim()) >= 0) found[a.textContent.trim()] = a.getAttribute('href'); });
  return JSON.stringify(found);
})()
'@
Write-Output "GNB LINKS: $gnbCheck"

# 2. Notices list -> detail
Nav-And-Wait "http://localhost:5173/notices" 2
Shot "01-notices-list"
$noticeCount = Eval "document.querySelectorAll('.list-notice-group tr').length"
Write-Output "NOTICE ROWS: $noticeCount"
$clickNotice = Eval "(function(){ var a = document.querySelector('.list-notice-group a.list-items'); if(!a) return 'NOT_FOUND'; a.click(); return 'clicked'; })()"
Write-Output "CLICK NOTICE: $clickNotice"
Start-Sleep -Milliseconds 600
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname')"
Shot "02-notice-detail"
$noticeTitle = Eval "(function(){ var t = document.querySelector('.page-title'); return t ? t.textContent : 'NOT_FOUND'; })()"
Write-Output "NOTICE DETAIL TITLE: $noticeTitle"

# 3. Data board list -> detail -> download link
Nav-And-Wait "http://localhost:5173/data-board" 2
Shot "03-data-board-list"
$clickBoard = Eval "(function(){ var a = document.querySelector('.list-notice-group a.list-items'); if(!a) return 'NOT_FOUND'; a.click(); return 'clicked'; })()"
Write-Output "CLICK DATA BOARD: $clickBoard"
Start-Sleep -Milliseconds 600
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname')"
Shot "04-data-board-detail"
$downloadHref = Eval "(function(){ var a = document.querySelector('a.downloadFn'); return a ? a.getAttribute('href') : 'NO_FILES'; })()"
Write-Output "DOWNLOAD LINK HREF: $downloadHref"

# 4. Qna board list -> detail
Nav-And-Wait "http://localhost:5173/qna/board" 2
Shot "05-qna-board-list"
$qnaRows = Eval "document.querySelectorAll('.list-notice-group tr').length"
Write-Output "QNA BOARD ROWS: $qnaRows"
$clickQna = Eval "(function(){ var a = document.querySelector('.list-notice-group a.list-items'); if(!a) return 'NOT_FOUND'; a.click(); return 'clicked'; })()"
Write-Output "CLICK QNA ROW: $clickQna"
Start-Sleep -Milliseconds 600
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname')"
Shot "06-qna-board-detail"

# 5. FAQ accordion + hit count
Nav-And-Wait "http://localhost:5173/faqs" 2
Shot "07-faq-list"
$faqToggle = Eval "(function(){ var h = document.querySelector('.notice-header.dropdown-toggle'); if(!h) return 'NOT_FOUND'; h.click(); return 'clicked'; })()"
Write-Output "FAQ TOGGLE: $faqToggle"
Start-Sleep -Milliseconds 500
$faqOpen = Eval "(function(){ var li = document.querySelector('.list-items.dropdown'); return li ? li.classList.contains('show') : false; })()"
Write-Output "FAQ OPEN STATE: $faqOpen"
Shot "08-faq-open"

# 6. Events list (filter) -> detail
Nav-And-Wait "http://localhost:5173/events?ing=Y" 2
Shot "09-events-list"
$eventCount = Eval "document.querySelectorAll('.evt-list-items').length"
Write-Output "EVENT CARDS: $eventCount"
$clickEvent = Eval "(function(){ var a = document.querySelector('.evt-list-items a.img_cover_link'); if(!a) return 'NOT_FOUND'; a.click(); return 'clicked'; })()"
Write-Output "CLICK EVENT: $clickEvent"
Start-Sleep -Milliseconds 600
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname')"
Shot "10-event-detail"
$eventTitle = Eval "(function(){ var t = document.querySelector('.page-title'); return t ? t.textContent : 'NOT_FOUND'; })()"
Write-Output "EVENT DETAIL TITLE: $eventTitle"

# 7. Events province -> city cascade
Nav-And-Wait "http://localhost:5173/events?ing=Y" 2
$provinceOptions = Eval "document.querySelector('#upperLocgovCode').options.length"
Write-Output "PROVINCE OPTIONS: $provinceOptions"
$pickProvince = Eval "(function(){ var sel = document.querySelector('#upperLocgovCode'); if(sel.options.length < 2) return 'NOT_ENOUGH'; sel.value = sel.options[1].value; sel.dispatchEvent(new Event('change')); return sel.value; })()"
Write-Output "PICKED PROVINCE: $pickProvince"
Start-Sleep -Milliseconds 800
$cityOptions = Eval "document.querySelector('#locgovCode').options.length"
Write-Output "CITY OPTIONS AFTER CASCADE: $cityOptions"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
