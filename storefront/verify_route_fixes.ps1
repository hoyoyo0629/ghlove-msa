param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\route-fixes"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-routefix"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9357","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9357/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9357/json/new?about:blank" -Method PUT
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

# 1. home search form -> internal /gifts?q=
Nav-And-Wait "http://localhost:5173/" 2
$searchTermChars = @(54620,50864) | ForEach-Object { [char]$_ }
$searchTerm = [string]::Join('', $searchTermChars)
$fillSearch = Eval "(function(){ var input = document.querySelector('.search-bar input'); input.value = '$searchTerm'; input.dispatchEvent(new Event('input')); return 'filled'; })()"
Write-Output "FILL SEARCH: $fillSearch"
$submitSearch = Eval '(function(){ var form = document.querySelector(".search-bar form"); form.dispatchEvent(new Event("submit", { cancelable: true })); return "submitted"; })()'
Write-Output "SUBMIT SEARCH: $submitSearch"
Start-Sleep -Milliseconds 500
$searchUrl = Eval "location.pathname + location.search"
Write-Output "SEARCH RESULT URL: $searchUrl"
Shot "01-home-search-result"

# 2. home category shortcut -> /gifts?categoryCode=
Nav-And-Wait "http://localhost:5173/" 2
$catClick = Eval '(function(){ var a = document.querySelector(".gift__link"); if(!a) return "NOT_FOUND"; a.click(); return "clicked"; })()'
Write-Output "CATEGORY CLICK: $catClick"
Start-Sleep -Milliseconds 500
$catUrl = Eval "location.pathname + location.search"
Write-Output "CATEGORY URL: $catUrl"

# 3. gift list card "do_btn" -> /donate?locgovCode=
Nav-And-Wait "http://localhost:5173/gifts" 2
$doBtnClick = Eval '(function(){ var btn = document.querySelector(".do_btn"); if(!btn) return "NOT_FOUND"; btn.click(); return "clicked"; })()'
Write-Output "DO_BTN CLICK: $doBtnClick"
Start-Sleep -Milliseconds 500
$donateUrl = Eval "location.pathname + location.search"
Write-Output "DONATE URL (from gift card): $donateUrl"
Shot "02-donate-from-gift-card"

# 4. interest-locgovs buttons
Nav-And-Wait "http://localhost:5173/mypage/interest-locgovs" 2
Shot "03-interest-locgovs"
$hasRows = Eval "document.querySelectorAll('.list-items').length"
Write-Output "INTEREST LOCGOV ROWS: $hasRows"
if ([int]$hasRows -gt 0) {
    $donaBtn = Eval '(function(){ var btn = document.querySelector(".formBtn.dona"); if(!btn) return "NOT_FOUND"; btn.click(); return "clicked"; })()'
    Write-Output "INTEREST-LOCGOV DONA CLICK: $donaBtn"
    Start-Sleep -Milliseconds 500
    Write-Output "URL AFTER DONA CLICK: $(Eval 'location.pathname + location.search')"
}

# 5. AppFooter sitemap links
Nav-And-Wait "http://localhost:5173/" 2
Eval '(function(){ var a = document.querySelector("a.open-modal"); if(a) a.click(); return "clicked"; })()' | Out-Null
Start-Sleep -Milliseconds 400
$footerLinks = Eval '(function(){ var g = document.querySelector("a[href=\"/gifts\"]"); var s = document.querySelector("a[href=\"/gifts/seasonal\"]"); return (g?"gifts:OK":"gifts:MISSING") + "," + (s?"seasonal:OK":"seasonal:MISSING"); })()'
Write-Output "FOOTER SITEMAP LINKS: $footerLinks"

# 6. list-select gift tab
Nav-And-Wait "http://localhost:5173/list-select?upperLocgovCode=44000" 2
$selectCity = Eval '(function(){ var items = document.querySelectorAll(".list-items"); if(!items.length) return "NOT_FOUND"; items[0].click(); return "clicked"; })()'
Write-Output "SELECT CITY: $selectCity"
Start-Sleep -Milliseconds 800
$giftTabHref = Eval "(function(){ var links = document.querySelectorAll('.tab-menu a'); for (var i=0;i<links.length;i++){ if(links[i].textContent.indexOf('\uB2F5\uB840\uD488') >= 0) return links[i].getAttribute('href'); } return 'NOT_FOUND'; })()"
Write-Output "GIFT TAB HREF: $giftTabHref"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
