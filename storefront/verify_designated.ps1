param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\designated"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-designated"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9346","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9346/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9346/json/new?about:blank" -Method PUT
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
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1440; height = 2600; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

$loginResult = Eval "(async () => { const r = await fetch('http://localhost:5173/member/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify({loginId:'testuser01', password:'Test1234!'})}); return JSON.stringify(await r.json()); })()" 20
Write-Output "LOGIN RESULT: $loginResult"

# 1) 목록 화면 (기본 필터: 진행중/모금율순)
Nav-And-Wait "http://localhost:5173/designated-donation" 2
Shot "01-designated-list"

# 정렬을 참여금액순으로 변경
$sortClick = Eval "(function(){ var prices = document.querySelectorAll('.filter_price'); if(prices.length<1) return 'NOT_FOUND'; prices[0].click(); return 'opened'; })()"
Write-Output "SORT DROPDOWN OPEN: $sortClick"
Start-Sleep -Milliseconds 300
$applyAmount = Eval "(function(){ var btns = Array.from(document.querySelectorAll('.drop_select button')); var btn = btns.find(function(b){return b.textContent.trim()==='참여금액순';}); if(!btn) return 'NOT_FOUND'; btn.click(); return 'clicked'; })()"
Write-Output "APPLY SORT AMOUNT: $applyAmount"
Start-Sleep -Seconds 1
Shot "02-designated-list-sorted-amount"

# 특정 사업(1000번, 이미지가 있는 강남구 어린이 도서관) 상세로 직접 이동
Nav-And-Wait "http://localhost:5173/designated-donation/1000" 2
Shot "03-designated-detail-intro-tab"

$reviewTabClick = Eval "(function(){ var links = document.querySelectorAll('.item_tab .nav-link'); if(links.length<2) return 'NOT_FOUND'; links[1].click(); return 'clicked'; })()"
Write-Output "REVIEW TAB CLICK: $reviewTabClick"
Start-Sleep -Milliseconds 500
Shot "04-designated-detail-cheer-tab-empty"

$qnaTabClick = Eval "(function(){ var links = document.querySelectorAll('.item_tab .nav-link'); if(links.length<3) return 'NOT_FOUND'; links[2].click(); return 'clicked'; })()"
Write-Output "QNA TAB CLICK: $qnaTabClick"
Start-Sleep -Milliseconds 500
Shot "05-designated-detail-notice-tab"

# "기부하기" 버튼 -> /donate?locgovCode=11680&prjId=1000 로 이동
$donateBtnClick = Eval "(function(){ var btn = document.querySelector('.prj_donation-btns .formBtn.donation'); if(!btn) return 'NOT_FOUND'; btn.click(); return 'clicked'; })()"
Write-Output "DONATE BUTTON CLICK: $donateBtnClick"
Start-Sleep -Seconds 1
Shot "06-donate-form-prefilled-from-project"

$currentUrl = Eval "location.pathname + location.search"
Write-Output "CURRENT URL AFTER DONATE CLICK: $currentUrl"

$projectSelectValue = Eval "(function(){ var sel = document.getElementById('projectJump'); return sel ? sel.value : 'NOT_FOUND'; })()"
Write-Output "PROJECT DROPDOWN PRESELECTED VALUE: $projectSelectValue"

# 거주지 확인 (testuser01 주소는 종로구, 이 사업은 강남구라 겹치지 않음)
$verifyClick = Eval "(function(){ var btn = document.getElementById('postBtn'); if(!btn) return 'NOT_FOUND'; window.alert = function(){}; btn.click(); return 'clicked'; })()"
Write-Output "VERIFY RESIDENCE CLICK: $verifyClick"
Start-Sleep -Seconds 1
Shot "07-donate-form-residence-verified"

$cheerMsgFill = Eval "(function(){ var el = document.getElementById('cheerMsg'); if(!el) return 'NOT_FOUND'; var setter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set; setter.call(el, String.fromCharCode(65,66,67) + ' cheer'); el.dispatchEvent(new Event('input', {bubbles:true})); return 'filled:' + el.value; })()"
Write-Output "CHEER MSG FILL: $cheerMsgFill"

$amountClick = Eval "(function(){ var btns = document.querySelectorAll('.give_cntr_amt'); if(btns.length===0) return 'NOT_FOUND'; btns[0].click(); return 'clicked:' + btns[0].textContent; })()"
Write-Output "AMOUNT QUICK-ADD CLICK: $amountClick"
Start-Sleep -Milliseconds 500

$checkClick = Eval "(function(){ var all = document.getElementById('checkAll'); if(!all) return 'NOT_FOUND'; all.click(); return 'clicked'; })()"
Write-Output "CHECK ALL AGREE CLICK: $checkClick"
Start-Sleep -Milliseconds 500
Shot "08-donate-form-ready-designated-submit"

$submitClick = Eval "(function(){ window.alert = function(){}; var btn = document.getElementById('submitBtn'); if(!btn) return 'NOT_FOUND'; btn.click(); return 'clicked'; })()"
Write-Output "SUBMIT CLICK: $submitClick"
Start-Sleep -Seconds 2
Shot "09-after-designated-submit"

$myDonations = Eval "(async () => { const r = await fetch('http://localhost:5173/donation/api/my/donations?period=ALL'); return JSON.stringify(await r.json()); })()"
Write-Output "MY DONATIONS STATE: $myDonations"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
