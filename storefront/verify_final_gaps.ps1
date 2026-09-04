$profileDir = Join-Path $env:TEMP "edge-cdp-profile-finalgaps"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }
$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9361","--user-data-dir=$profileDir","about:blank" -PassThru
$ready = $false
for ($i=0; $i -lt 30; $i++) { try { Invoke-RestMethod -Uri "http://127.0.0.1:9361/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 } }
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }
$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9361/json/new?about:blank" -Method PUT
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
Send-CDP "Page.enable" @{} | Out-Null; Receive-CDP $script:msgId | Out-Null
Send-CDP "Runtime.enable" @{} | Out-Null; Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2
$login = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN: $login"

Nav-And-Wait "http://localhost:5173/mypage/points" 2
$expireLinkGone = Eval "document.querySelectorAll(`"a[href*='batch/expire']`").length"
Write-Output "BATCH EXPIRE LINKS REMAINING (should be 0): $expireLinkGone"
$rows = Eval "document.querySelectorAll('.loc_mall button').length"
Write-Output "GIFT MALL BUTTONS: $rows"
$click = Eval "(function(){ var b = document.querySelector('.loc_mall button'); if(!b) return 'NOT_FOUND'; b.click(); return 'clicked'; })()"
Write-Output "CLICK GIFT MALL BTN: $click"
Start-Sleep -Milliseconds 600
Write-Output "URL AFTER CLICK: $(Eval 'location.pathname + location.search')"

Nav-And-Wait "http://localhost:5173/mypage/donations" 2
$comingSoonHref = Eval "(function(){ var a = document.querySelector(`"a[href*='coming-soon']`"); return a ? a.getAttribute('href') : 'NOT_FOUND'; })()"
Write-Output "COMING-SOON HREF (should be 8081): $comingSoonHref"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
