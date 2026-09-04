param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\offline"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-offline"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9348","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9348/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9348/json/new?about:blank" -Method PUT
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

Nav-And-Wait "http://localhost:5173/mypage/donations" 2
Shot "01-my-donations-with-offline-link"

$offlineLinkClick = Eval "(function(){ var links = Array.from(document.querySelectorAll('a')); var link = links.find(function(a){ return a.getAttribute('href') === '/mypage/donations/offline'; }); if(!link) return 'NOT_FOUND'; link.click(); return 'clicked'; })()"
Write-Output "OFFLINE LINK CLICK: $offlineLinkClick"
Start-Sleep -Seconds 1
Shot "02-offline-form-empty"

$selectResult = Eval "(function(){ var upperSel = document.getElementById('offlineUpperLocgov'); var locSel = document.getElementById('offlineLocgov'); if(!upperSel || !locSel) return 'NOT_FOUND'; var opts = Array.from(locSel.options).filter(function(o){return o.value;}); var target = opts.find(function(o){return o.value==='26350';}) || opts[0]; locSel.value = target.value; locSel.dispatchEvent(new Event('change',{bubbles:true})); return 'selected:' + target.value; })()"
Write-Output "LOCGOV SELECT: $selectResult"
Start-Sleep -Milliseconds 500
Shot "03-offline-form-locgov-selected"

$amountFill = Eval "(function(){ var el = document.getElementById('offlineAmount'); if(!el) return 'NOT_FOUND'; var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set; setter.call(el, '7000'); el.dispatchEvent(new Event('input', {bubbles:true})); return 'filled:' + el.value; })()"
Write-Output "AMOUNT FILL: $amountFill"

$bankFill = Eval "(function(){ var el = document.getElementById('rceptBankNm'); if(!el) return 'NOT_FOUND'; var codes = [72,111,110,103,32,71,105,108,100,111,110,103,32,47,32,78,111,110,103,72,121,117,112]; var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set; setter.call(el, String.fromCharCode.apply(null, codes)); el.dispatchEvent(new Event('input', {bubbles:true})); return 'filled:' + el.value; })()"
Write-Output "BANK MEMO FILL: $bankFill"
Start-Sleep -Milliseconds 300
Shot "04-offline-form-ready"

$submitClick = Eval "(function(){ window.alert = function(){}; var btn = document.getElementById('offlineSubmitBtn'); if(!btn) return 'NOT_FOUND'; btn.click(); return 'clicked'; })()"
Write-Output "SUBMIT CLICK: $submitClick"
Start-Sleep -Seconds 2
Shot "05-offline-form-success"

$myDonations = Eval "(async () => { const r = await fetch('http://localhost:5173/donation/api/my/donations?period=ALL'); return JSON.stringify(await r.json()); })()"
Write-Output "MY DONATIONS STATE: $myDonations"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
