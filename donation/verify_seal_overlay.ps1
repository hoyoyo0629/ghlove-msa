$profileDir = Join-Path $env:TEMP "edge-cdp-profile-sealtest"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }
$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9377","--user-data-dir=$profileDir","about:blank" -PassThru
$ready = $false
for ($i=0; $i -lt 30; $i++) { try { Invoke-RestMethod -Uri "http://127.0.0.1:9377/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 } }
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }
$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9377/json/new?about:blank" -Method PUT
$wsUrl = $tab.webSocketDebuggerUrl
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$cts = New-Object System.Threading.CancellationTokenSource
$ws.ConnectAsync([Uri]$wsUrl, $cts.Token).Wait()
$script:msgId = 0
function Send-CDP($method, $params) {
    $script:msgId++; $id = $script:msgId
    $json = (@{ id = $id; method = $method; params = $params } | ConvertTo-Json -Depth 10 -Compress)
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $seg = New-Object System.ArraySegment[byte] (,$bytes)
    $ws.SendAsync($seg, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $cts.Token).Wait()
    return $id
}
function Receive-CDP($expectId, $timeoutSec = 20) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        $ms = New-Object System.IO.MemoryStream
        $buffer = New-Object byte[] 1MB
        do {
            $seg = New-Object System.ArraySegment[byte] (,$buffer)
            $task = $ws.ReceiveAsync($seg, $cts.Token)
            if (-not $task.Wait(2000)) { $result = $null; break }
            $result = $task.Result
            if ($result.Count -gt 0) { $ms.Write($buffer, 0, $result.Count) }
        } while ($result -and -not $result.EndOfMessage)
        if ($result) {
            $text = [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
            try { $obj = $text | ConvertFrom-Json; if ($obj.id -eq $expectId) { return $obj } } catch {}
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
function Shot($path) {
    $id = Send-CDP "Page.captureScreenshot" @{ format = "png" }
    $resp = Receive-CDP $id 15
    [IO.File]::WriteAllBytes($path, [Convert]::FromBase64String($resp.result.data))
}
Send-CDP "Page.enable" @{} | Out-Null; Receive-CDP $script:msgId | Out-Null
Send-CDP "Runtime.enable" @{} | Out-Null; Receive-CDP $script:msgId | Out-Null
# certificate-print.html calls window.print() on load, which blanks headless screenshots - stub it out.
Send-CDP "Page.addScriptToEvaluateOnNewDocument" @{ source = "window.print = function(){};" } | Out-Null
Receive-CDP $script:msgId | Out-Null
# A4 print page is tall - widen the viewport so the seal near the bottom of the front page is visible.
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 900; height = 1300; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:8081/login" 2
$login = Eval "(async () => { const r = await fetch('/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN: $login"

# 단일 기부건(D202609030911067388, locgov 11680=강남구)만 골라 확인증 출력 폼을 직접 제출
Nav-And-Wait "http://localhost:8082/receipts" 2
$submit = Eval "(function(){
  var f = document.createElement('form');
  f.method = 'POST'; f.action = '/receipts/certificate'; f.target = '_self';
  var m = document.createElement('input'); m.name='mode'; m.value='print'; f.appendChild(m);
  var c = document.createElement('input'); c.name='cntrSn'; c.value='D202609030911067388'; f.appendChild(c);
  document.body.appendChild(f);
  f.submit();
  return 'submitted';
})()"
Write-Output "SUBMIT: $submit"
Start-Sleep -Seconds 2

Write-Output "URL: $(Eval 'location.href')"
$sealPresent = Eval "document.querySelectorAll('img.seal-area').length"
Write-Output "SEAL IMG COUNT ON PAGE: $sealPresent"
$sealSrcLen = Eval "(function(){ var el = document.querySelector('img.seal-area'); return el ? el.src.length : -1; })()"
Write-Output "SEAL DATA URI LENGTH (should be large, >1000): $sealSrcLen"
$topLocGov = Eval "(function(){ var els = document.querySelectorAll('td'); for (var i=0;i<els.length;i++){ if (els[i].previousElementSibling && els[i].previousElementSibling.textContent.indexOf('기부지자체')>=0) return els[i].textContent; } return document.body.innerText.slice(0,300); })()"
Write-Output "TOP LOCGOV CELL: $topLocGov"

Eval "document.querySelector('img.seal-area').scrollIntoView({block:'center'})" | Out-Null
Start-Sleep -Milliseconds 300
Shot "$PSScriptRoot\seal-test-screenshot.png"
Write-Output "Screenshot saved."

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
