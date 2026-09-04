param(
    [string]$LoginId = "admin01",
    [string]$Password = "Test1234!",
    [string[]]$Urls,
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\shots\audit"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9333","--user-data-dir=$profileDir","about:blank" -PassThru

# Wait for CDP to be ready
$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try {
        $v = Invoke-RestMethod -Uri "http://127.0.0.1:9333/json/version" -TimeoutSec 1
        $ready = $true
        break
    } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

# Open a tab
$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9333/json/new?about:blank" -Method PUT
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
            try {
                $obj = $text | ConvertFrom-Json
                if ($obj.id -eq $expectId) { return $obj }
            } catch { }
        }
    }
    return $null
}

function Nav-And-Wait($url, $waitSec = 3) {
    $id = Send-CDP "Page.navigate" @{ url = $url }
    Receive-CDP $id | Out-Null
    Start-Sleep -Seconds $waitSec
}

Send-CDP "Page.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Runtime.enable" @{} | Out-Null
Receive-CDP $script:msgId | Out-Null
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 1400; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

# Navigate to the admin origin first (need same-origin for fetch)
Nav-And-Wait "http://localhost:8086/admin/login" 3

# Perform login via fetch() inside the page context (cookies set normally by the browser)
$loginScript = @'
(async function() {
  try {
    const r1 = await fetch('/admin/login/check', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:'loginId=__LOGINID__&password=__PASSWORD__'});
    const j1 = await r1.json();
    if (j1.status !== 'CODE_SENT') { return 'STEP1:' + JSON.stringify(j1); }
    const r2 = await fetch('/admin/login/verify', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:'loginId=__LOGINID__&code=' + j1.devCode});
    const j2 = await r2.json();
    return 'STEP2:' + JSON.stringify(j2);
  } catch (e) {
    return 'ERROR:' + e.toString() + ' href=' + location.href;
  }
})()
'@
$loginScript = $loginScript.Replace('__LOGINID__', $LoginId).Replace('__PASSWORD__', $Password)

$loginOk = $false
for ($attempt = 1; $attempt -le 3 -and -not $loginOk; $attempt++) {
    $id = Send-CDP "Runtime.evaluate" @{ expression = $loginScript; awaitPromise = $true; returnByValue = $true }
    $resp = Receive-CDP $id 15
    $val = $resp.result.result.value
    Write-Output "LOGIN ATTEMPT $($attempt): $val"
    if ($val -match '"status":"OK"') { $loginOk = $true } else { Start-Sleep -Seconds 2 }
}
if (-not $loginOk) { Write-Error "Login failed after retries"; $ws.Dispose(); Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue; exit 1 }

foreach ($u in $Urls) {
    try {
        $full = "http://localhost:8086$u"
        Nav-And-Wait $full 2
        $id = Send-CDP "Page.captureScreenshot" @{ format = "png" }
        $shot = Receive-CDP $id 15
        if ($null -eq $shot -or $null -eq $shot.result.data) { Write-Output "FAILED (no data): $u"; continue }
        $bytes = [Convert]::FromBase64String($shot.result.data)
        $fname = ($u.TrimStart('/') -replace '[/\\:?&=]', '_') + ".png"
        [IO.File]::WriteAllBytes((Join-Path $OutDir $fname), $bytes)
        Write-Output "SAVED: $u -> $fname"
    } catch {
        Write-Output "FAILED ($u): $($_.Exception.Message)"
    }
}

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
