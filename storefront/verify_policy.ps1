param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\policy"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-policy"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9356","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9356/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9356/json/new?about:blank" -Method PUT
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

Nav-And-Wait "http://localhost:5173/policy/privacy" 2
Shot "01-policy-privacy"
$titleCheck = Eval "document.querySelector('.page-title-txt') ? document.querySelector('.page-title-txt').textContent : 'NOT_FOUND'"
Write-Output "PRIVACY TITLE: $titleCheck"
$contentCheck = Eval "document.querySelector('.fnb-detail') ? document.querySelector('.fnb-detail').textContent.length : 0"
Write-Output "PRIVACY CONTENT LENGTH: $contentCheck"

Nav-And-Wait "http://localhost:5173/policy/copyright" 2
Shot "02-policy-copyright"
$titleCheck2 = Eval "document.querySelector('.page-title-txt') ? document.querySelector('.page-title-txt').textContent : 'NOT_FOUND'"
Write-Output "COPYRIGHT TITLE: $titleCheck2"

Nav-And-Wait "http://localhost:5173/policy/auth" 2
Shot "03-policy-auth"
$titleCheck3 = Eval "document.querySelector('.page-title-txt') ? document.querySelector('.page-title-txt').textContent : 'NOT_FOUND'"
Write-Output "AUTH TITLE: $titleCheck3"

Nav-And-Wait "http://localhost:5173/" 2
$footerOpen = Eval '(function(){ var a = document.querySelector("a.open-modal"); if(a) a.click(); return "clicked"; })()'
Write-Output "OPEN SITEMAP/FOOTER MODAL: $footerOpen"
Start-Sleep -Milliseconds 400
$footerLinks = Eval '(function(){ var p = document.querySelector("a[href=\"/policy/privacy\"]"); var c = document.querySelector("a[href=\"/policy/copyright\"]"); var t = document.querySelector("a[href=\"/policy/auth\"]"); return (p?"p:OK":"p:MISSING") + "," + (c?"c:OK":"c:MISSING") + "," + (t?"t:OK":"t:MISSING"); })()'
Write-Output "FOOTER POLICY LINKS: $footerLinks"

Nav-And-Wait "http://localhost:5173/signup" 2
$signupLinks = Eval "(function(){ var links = Array.from(document.querySelectorAll('a.moreView')); return links.map(function(a){ return a.getAttribute('href'); }).join(','); })()"
Write-Output "SIGNUP TERMS LINKS: $signupLinks"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
