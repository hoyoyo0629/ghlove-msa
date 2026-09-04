param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\point-reservations"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-pointresv"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9358","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9358/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9358/json/new?about:blank" -Method PUT
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

# 1. MyPointsView link -> internal route (no external nav)
Nav-And-Wait "http://localhost:5173/mypage/points" 2
$linkHref = Eval "(function(){ var links = document.querySelectorAll('.links a'); for (var i=0;i<links.length;i++){ if(links[i].textContent.indexOf('\uC608\uC57D \uAD00\uB9AC') >= 0) return links[i].getAttribute('href'); } return 'NOT_FOUND'; })()"
Write-Output "RESERVATIONS LINK HREF (should be internal /mypage/points/reservations): $linkHref"
$clickLink = Eval '(function(){ var links = document.querySelectorAll(".links a"); for (var i=0;i<links.length;i++){ if(links[i].getAttribute("href") === "/mypage/points/reservations"){ links[i].click(); return "clicked"; } } return "NOT_FOUND"; })()'
Write-Output "CLICK RESERVATIONS LINK: $clickLink"
Start-Sleep -Milliseconds 500
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname')"
Shot "01-reservations-page"

# 2. create a reservation
Nav-And-Wait "http://localhost:5173/mypage/points/reservations" 2
$beforeAvail = Eval "(function(){ var el = document.querySelector('#availableBalance'); return el ? el.value : 'NOT_FOUND'; })()"
Write-Output "AVAILABLE BEFORE: $beforeAvail"
$fillAmount = Eval "(function(){ var input = document.querySelector('#amount'); input.value = '100'; input.dispatchEvent(new Event('input')); return 'filled'; })()"
Write-Output "FILL AMOUNT: $fillAmount"
$fillReason = Eval "(function(){ var input = document.querySelector('#reason'); input.value = 'cdp-test-reservation'; input.dispatchEvent(new Event('input')); return 'filled'; })()"
Write-Output "FILL REASON: $fillReason"
$submitForm = Eval "(async function(){ var form = document.querySelector('form'); form.dispatchEvent(new Event('submit', { cancelable: true })); await new Promise(r => setTimeout(r, 1200)); return 'submitted'; })()" 20
Write-Output "SUBMIT: $submitForm"
$afterCreate = Eval "(function(){ var rows = document.querySelectorAll('.list-items'); return rows.length; })()"
Write-Output "ROWS AFTER CREATE: $afterCreate"
$errMsg = Eval "(function(){ var e = document.querySelector('.error'); return e ? e.textContent : ''; })()"
Write-Output "ERROR MSG (should be empty): $errMsg"
Shot "02-after-create"

# 3. confirm the newest RESERVED row
$confirmClick = Eval '(function(){ var rows = document.querySelectorAll(".list-items"); for (var i=0;i<rows.length;i++){ var btn = rows[i].querySelector(".formBtn:not(.cancellation)"); if(btn){ btn.click(); return "clicked"; } } return "NOT_FOUND"; })()'
Write-Output "CONFIRM CLICK: $confirmClick"
Start-Sleep -Milliseconds 800
$afterAvail = Eval "(function(){ var el = document.querySelector('#availableBalance'); return el ? el.value : 'NOT_FOUND'; })()"
Write-Output "AVAILABLE AFTER CONFIRM (should be 100 less than before): $afterAvail"
Shot "03-after-confirm"

# 4. create+release another reservation
$fillAmount2 = Eval "(function(){ var input = document.querySelector('#amount'); input.value = '50'; input.dispatchEvent(new Event('input')); return 'filled'; })()"
Write-Output "FILL AMOUNT 2: $fillAmount2"
$submitForm2 = Eval "(async function(){ var form = document.querySelector('form'); form.dispatchEvent(new Event('submit', { cancelable: true })); await new Promise(r => setTimeout(r, 1200)); return 'submitted'; })()" 20
Write-Output "SUBMIT 2: $submitForm2"
$releaseClick = Eval '(function(){ var rows = document.querySelectorAll(".list-items"); for (var i=0;i<rows.length;i++){ var btn = rows[i].querySelector(".formBtn.cancellation"); if(btn){ btn.click(); return "clicked"; } } return "NOT_FOUND"; })()'
Write-Output "RELEASE CLICK: $releaseClick"
Start-Sleep -Milliseconds 800
$finalAvail = Eval "(function(){ var el = document.querySelector('#availableBalance'); return el ? el.value : 'NOT_FOUND'; })()"
Write-Output "AVAILABLE AFTER RELEASE (should equal after-confirm value, released doesn't consume): $finalAvail"
Shot "04-after-release"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
