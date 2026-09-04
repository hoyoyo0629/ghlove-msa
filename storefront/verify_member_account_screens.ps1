param(
    [string]$OutDir = "C:\Users\GHLOVE~1\AppData\Local\Temp\claude\storefront-shots\member-account"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$profileDir = Join-Path $env:TEMP "edge-cdp-profile-memberacct"
if (Test-Path $profileDir) { Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue }

$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$proc = Start-Process -FilePath $edge -ArgumentList "--headless=new","--disable-gpu","--remote-debugging-port=9354","--user-data-dir=$profileDir","about:blank" -PassThru

$ready = $false
for ($i=0; $i -lt 30; $i++) {
    try { Invoke-RestMethod -Uri "http://127.0.0.1:9354/json/version" -TimeoutSec 1 | Out-Null; $ready = $true; break } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ready) { Write-Error "CDP not ready"; exit 1 }

$tab = Invoke-RestMethod -Uri "http://127.0.0.1:9354/json/new?about:blank" -Method PUT
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
    if ($ws.State -ne [System.Net.WebSockets.WebSocketState]::Open) {
        throw "WebSocket not open (state=$($ws.State))"
    }
    $ms = New-Object System.IO.MemoryStream
    $buffer = New-Object byte[] 1MB
    do {
        $seg = New-Object System.ArraySegment[byte] (,$buffer)
        $task = $ws.ReceiveAsync($seg, $cts.Token)
        try {
            if (-not $task.Wait($timeoutMs)) { return $null }
        } catch {
            throw "WebSocket receive failed: $($_.Exception.InnerException.Message)"
        }
        $result = $task.Result
        if ($result.Count -gt 0) { $ms.Write($buffer, 0, $result.Count) }
    } while (-not $result.EndOfMessage)
    return [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
}
function Receive-CDP($expectId, $timeoutSec = 20) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        try {
            $text = Receive-FullMessage 2000
        } catch {
            Write-Output "SOCKET ERROR (Receive-CDP): $($_.Exception.Message)"
            return $null
        }
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
Send-CDP "Emulation.setDeviceMetricsOverride" @{ width = 1280; height = 1400; deviceScaleFactor = 1; mobile = $false } | Out-Null
Receive-CDP $script:msgId | Out-Null

Nav-And-Wait "http://localhost:5173/" 3
Nav-And-Wait "http://localhost:5173/" 2

# ---- 0. create throwaway account for destructive tests (password change / withdraw) ----
$signup = Eval '(async () => { const r = await fetch("/member/api/auth/signup", {method:"POST", headers:{"Content-Type":"application/json"}, credentials:"include", body: JSON.stringify({loginId:"e2etest01", password:"Xk9Trees#Zq", passwordConfirm:"Xk9Trees#Zq", userName:"E2E Tester", email:"e2etest01@example.com", phoneNumber:"010-9999-0001", birthday:"1990-01-01", address:"", addressDetail:""})}); return JSON.stringify(await r.json()); })()' 20
Write-Output "SIGNUP: $signup"

# ---- 1. find-idpw: ID lookup using real testuser01 (read-only, safe) ----
Nav-And-Wait "http://localhost:5173/find-idpw" 2
Eval "window.alert = function(m){ window.__lastAlert = m; }; window.confirm = function(){ return true; }; 'overridden'" | Out-Null
Shot "01-find-idpw"
$openModal = Eval '(function(){ var btns = document.querySelectorAll(".blueBtn.u-confirm"); if(!btns.length) return "NOT_FOUND"; btns[0].click(); return "clicked"; })()'
Write-Output "OPEN AUTH MODAL (id tab): $openModal"
Start-Sleep -Milliseconds 400
$clickFinanc = Eval '(function(){ var btns = document.querySelectorAll(".authentication-box button"); if(!btns.length) return "NOT_FOUND"; btns[0].click(); return "clicked"; })()'
Write-Output "START MOCK AUTH: $clickFinanc"
Start-Sleep -Milliseconds 300
$testUserNameChars = @(53580,49828,53944,50976,51200,49) | ForEach-Object { [char]$_ }
$testUserName = [string]::Join('', $testUserNameChars)
$fillForm = Eval "(function(){ var name = document.getElementById('authUserName'); var phone = document.getElementById('authPhoneNumber'); name.value = '$testUserName'; name.dispatchEvent(new Event('input')); phone.value = '010-1234-5678'; phone.dispatchEvent(new Event('input')); return 'filled'; })()"
Write-Output "FILL ID FORM: $fillForm"
$sendCode = Eval '(function(){ var btns = document.querySelectorAll(".overlayer-body-wrap .btn-group button"); if(!btns.length) return "NOT_FOUND"; btns[btns.length-1].click(); return "clicked"; })()'
Write-Output "SEND CODE: $sendCode"
Start-Sleep -Milliseconds 800
Shot "02-find-idpw-code-step"
$devCode = Eval "(function(){ var el = document.querySelector('.form_wrap_line.certificate p[style]'); return el ? el.textContent : 'NOT_FOUND'; })()"
Write-Output "DEV CODE HINT: $devCode"
$codeMatch = [regex]::Match($devCode, '\d{4,8}')
if ($codeMatch.Success) {
    $code = $codeMatch.Value
    $fillCode = Eval "(function(){ var el = document.getElementById('authCode'); el.value = '$code'; el.dispatchEvent(new Event('input')); return 'filled'; })()"
    $verify = Eval '(function(){ var btns = document.querySelectorAll(".overlayer-body-wrap .btn-group button"); if(!btns.length) return "NOT_FOUND"; btns[btns.length-1].click(); return "clicked"; })()'
    Write-Output "VERIFY CODE: $verify"
    Start-Sleep -Milliseconds 800
    Shot "03-find-idpw-id-result"
    $resultId = Eval "document.querySelector('.resultID-box') ? document.querySelector('.resultID-box').textContent : 'NOT_FOUND'"
    Write-Output "ID RESULT: $resultId"
} else {
    Write-Output "COULD NOT PARSE DEV CODE"
}

# ---- 2. password change + withdraw using throwaway e2etest01 ----
$login1 = Eval '(async () => { const r = await fetch("/member/api/auth/login", {method:"POST", headers:{"Content-Type":"application/json"}, credentials:"include", body: JSON.stringify({loginId:"e2etest01", password:"Xk9Trees#Zq"})}); return JSON.stringify(await r.json()); })()' 20
Write-Output "LOGIN e2etest01: $login1"

Nav-And-Wait "http://localhost:5173/mypage/password" 2
Eval "window.alert = function(m){ window.__lastAlert = m; }; window.confirm = function(){ return true; }; 'overridden'" | Out-Null
Shot "04-password-change"
$setCurPw = Eval '(function(){ var el = document.getElementById("currentPassword"); el.value = "Xk9Trees#Zq"; el.dispatchEvent(new Event("input")); return "elValue=" + el.value; })()'
Write-Output "SET CURRENT PW: $setCurPw"
$verifyCur = Eval '(function(){ var btns = document.querySelectorAll(".info-field-items.newpw button"); btns[0].click(); return "clicked"; })()'
Start-Sleep -Milliseconds 400
$alert1 = Eval "window.__lastAlert || 'none'"
Write-Output "ALERT AFTER VERIFY CLICK: $alert1"
Write-Output "VERIFY CURRENT PW: $verifyCur"
Start-Sleep -Milliseconds 800
Shot "05-password-change-verified"
$setNew = Eval '(function(){ var np = document.getElementById("newPassword"); var npc = document.getElementById("newPasswordConfirm"); np.value = "Zq7Forest#Ab"; np.dispatchEvent(new Event("input")); npc.value = "Zq7Forest#Ab"; npc.dispatchEvent(new Event("input")); return "filled"; })()'
Write-Output "FILL NEW PW: $setNew"
Start-Sleep -Milliseconds 300
Shot "06-password-rules"
$submitPw = Eval '(function(){ var btn = document.querySelector("form button[type=submit]"); if(!btn) return "NOT_FOUND"; btn.click(); return "clicked"; })()'
Write-Output "SUBMIT PW CHANGE: $submitPw"
Start-Sleep -Milliseconds 1000
$afterPwUrl = Eval "location.pathname + location.search"
Write-Output "AFTER PW CHANGE URL: $afterPwUrl"
Shot "07-after-password-change"

# re-login with NEW password for role-request/queue + withdraw tests
$login2 = Eval '(async () => { const r = await fetch("/member/api/auth/login", {method:"POST", headers:{"Content-Type":"application/json"}, credentials:"include", body: JSON.stringify({loginId:"e2etest01", password:"Zq7Forest#Ab"})}); return JSON.stringify(await r.json()); })()' 20
Write-Output "RE-LOGIN with new password: $login2"

# ---- 3. role-request ----
Nav-And-Wait "http://localhost:5173/mypage/role-request" 2
Eval "window.alert = function(m){ window.__lastAlert = m; }; window.confirm = function(){ return true; }; 'overridden'" | Out-Null
Shot "08-role-request"
$submitRole = Eval '(function(){ var textarea = document.getElementById("reason"); textarea.value = "E2E test request"; textarea.dispatchEvent(new Event("input")); var btn = document.querySelector("form button[type=submit]"); btn.click(); return "clicked"; })()'
Write-Output "SUBMIT ROLE REQUEST: $submitRole"
Start-Sleep -Milliseconds 800
Shot "09-role-request-submitted"
$myReqRows = Eval "document.querySelectorAll('.item_list-group tr.list-items').length"
Write-Output "MY ROLE REQUESTS ROWS: $myReqRows"

# ---- 4. role-queue: approve the just-submitted request ----
Nav-And-Wait "http://localhost:5173/mypage/role-queue" 2
Shot "10-role-queue"
$queueRows = Eval "document.querySelectorAll('.item_list-group tr.list-items').length"
Write-Output "QUEUE ROWS: $queueRows"
$approveClick = Eval '(function(){ var btns = document.querySelectorAll(".item_list-group tr.list-items .formBtn:not(.cancellation)"); if(!btns.length) return "NOT_FOUND"; btns[0].click(); return "clicked"; })()'
Write-Output "APPROVE CLICK: $approveClick"
Start-Sleep -Milliseconds 800
Shot "11-role-queue-after-approve"
$queueRowsAfter = Eval "document.querySelectorAll('.item_list-group tr.list-items').length"
Write-Output "QUEUE ROWS AFTER APPROVE: $queueRowsAfter"

# ---- 5. withdraw (destructive, e2etest01 only) ----
Nav-And-Wait "http://localhost:5173/mypage/withdraw" 2
Eval "window.alert = function(m){ window.__lastAlert = m; }; window.confirm = function(){ return true; }; 'overridden'" | Out-Null
Shot "12-withdraw"
$fillWithdraw = Eval '(function(){ var radios = document.querySelectorAll("input[name=leaveCode]"); if(!radios.length) return "NO_RADIOS"; radios[0].checked = true; radios[0].dispatchEvent(new Event("change")); var pw = document.getElementById("password"); pw.value = "Zq7Forest#Ab"; pw.dispatchEvent(new Event("input")); return "filled:" + radios.length; })()'
Write-Output "FILL WITHDRAW FORM: $fillWithdraw"
$submitWithdraw = Eval '(function(){ var btn = document.querySelector("form button[type=submit]"); if(!btn) return "NOT_FOUND"; btn.click(); return "clicked"; })()'
Write-Output "SUBMIT WITHDRAW: $submitWithdraw"
Start-Sleep -Milliseconds 1000
$afterWithdrawUrl = Eval "location.pathname + location.search"
Write-Output "AFTER WITHDRAW URL: $afterWithdrawUrl"
Shot "13-after-withdraw"

$ws.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, "done", $cts.Token).Wait()
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Write-Output "DONE"
