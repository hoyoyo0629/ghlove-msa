param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\guides"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-guides"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9351","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9351/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9351/json/new?about:blank" -Method PUT
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

# guide1
Nav-And-Wait "http://localhost:5173/guide1" 2
Shot "01-guide1"
$err1 = Eval "document.querySelector('.give-info__title') ? 'OK' : 'MISSING'"
Write-Output "guide1 title: $err1"

# guide2 (9 tabs)
Nav-And-Wait "http://localhost:5173/guide2" 2
Shot "02-guide2-tab0"
$tab2check = Eval "(function(){ var wraps = document.querySelectorAll('.btn_h3_wrap'); return 'wraps:' + wraps.length; })()"
Write-Output "guide2 tabs: $tab2check"
$clickTab3 = Eval '(function(){ var btn = document.querySelector("[data-tab=\"3\"]"); if (!btn) return "NOT_FOUND"; btn.click(); return "clicked"; })()'
Write-Output "guide2 click tab3(login): $clickTab3"
Start-Sleep -Milliseconds 500
Shot "03-guide2-tab3-login"
$tab3state = Eval '(function(){ var pane = document.querySelector("[data-tab-con=\"3\"]"); return pane ? getComputedStyle(pane).display : "NOT_FOUND"; })()'
Write-Output "guide2 tab3 pane display: $tab3state"

# guide5 (3 tabs, vue reactive)
Nav-And-Wait "http://localhost:5173/guide5" 2
Shot "04-guide5-tab0"
$clickTab5_2 = Eval "(function(){ var btns = document.querySelectorAll('.tab_group.offline .tab_items'); if (btns.length < 3) return 'NOT_ENOUGH:' + btns.length; btns[2].click(); return 'clicked'; })()"
Write-Output "guide5 click tab2(gift): $clickTab5_2"
Start-Sleep -Milliseconds 500
Shot "05-guide5-tab2-gift"

# guide6
Nav-And-Wait "http://localhost:5173/guide6" 2
Shot "06-guide6"

# list-select
Nav-And-Wait "http://localhost:5173/list-select" 2
Shot "07-list-select-empty"
$selectProvince = Eval "(function(){ var sel = document.getElementById('selectArea'); if(!sel) return 'NOT_FOUND'; sel.value='44000'; sel.dispatchEvent(new Event('change')); return 'selected'; })()"
Write-Output "list-select select province: $selectProvince"
Start-Sleep -Milliseconds 800
Shot "08-list-select-cities"
$cheongyang = [string]::Join('', @(52397,50577,44400 | ForEach-Object { [char]$_ }))
$clickCity = Eval ('(function(){ var target = String.fromCharCode(52397,50577,44400); var items = document.querySelectorAll(".list-items"); var el = Array.from(items).find(function(li){ return li.textContent.indexOf(target) >= 0; }); if(!el) return "NOT_FOUND:" + items.length; el.click(); return "clicked"; })()')
Write-Output "list-select click city ${cheongyang}: $clickCity"
Start-Sleep -Seconds 1
Shot "09-list-select-fund-info-tab"
$clickProjectTab = Eval ('(function(){ var target = String.fromCharCode(44592,44552,49324,50629); var links = document.querySelectorAll(".tab-menu a"); var el = Array.from(links).find(function(a){ return a.textContent.indexOf(target) >= 0; }); if(!el) return "NOT_FOUND"; el.click(); return "clicked"; })()')
Write-Output "list-select click project tab: $clickProjectTab"
Start-Sleep -Milliseconds 500
Shot "10-list-select-fund-project-tab"
$projectCount = Eval "document.querySelectorAll('.city-notice table tbody tr.cursor').length"
Write-Output "list-select project rows: $projectCount"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
