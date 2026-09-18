# docs/inventory/*.tsv 를 서비스별 sheet 로 묶어 하나의 .xlsx 로 만든다.
# 사용: powershell -File build-xlsx.ps1
$ErrorActionPreference = 'Stop'
$dir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$out  = Join-Path $dir 'as-is-inventory.xlsx'

# sheet 순서 = 분석 순서
$order = @('member','donation','point','gift','order','admin','common')

$files = @()
foreach ($n in $order) {
    $p = Join-Path $dir "$n.tsv"
    if (Test-Path $p) { $files += ,@($n, $p) }
}
if ($files.Count -eq 0) { throw "no .tsv found in $dir" }

$xl = New-Object -ComObject Excel.Application
$xl.Visible = $false
$xl.DisplayAlerts = $false
$wb = $xl.Workbooks.Add()

# 판정별 색 (ARGB -> BGR int)
$colors = @{
    '죽은코드'     = 12632256   # 회색
    '재현누락'     = 13421823   # 연분홍
    '버그'         = 13421823
    '버그(양쪽)'   = 13421823
    '의도적축소'   = 15132390   # 연회청
    '대응있음'     = 13561798   # 연녹
    '확인필요'     = 10480895   # 연주황
    '근거확인필요' = 10480895
    '조치완료'     = 5296274    # 진녹 (이번 작업으로 해소)
    '부분'         = 14083324   # 연보라
    '보류'         = 14083324
    '참고'         = 15921906   # 아주 연한 회청
    '차이(경미)'   = 15921906
    '개발우회'     = 12632256
}

$i = 0
foreach ($f in $files) {
    $name = $f[0]; $path = $f[1]
    if ($i -lt $wb.Worksheets.Count) { $ws = $wb.Worksheets.Item($i + 1) }
    else { $ws = $wb.Worksheets.Add([System.Reflection.Missing]::Value, $wb.Worksheets.Item($wb.Worksheets.Count)) }
    $ws.Name = $name

    $lines = Get-Content -LiteralPath $path -Encoding UTF8
    $r = 1
    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $cells = $line -split "`t"
        for ($c = 0; $c -lt $cells.Count; $c++) {
            $ws.Cells.Item($r, $c + 1).Value2 = $cells[$c]
        }
        if ($r -gt 1 -and $cells.Count -ge 6) {
            $verdict = $cells[5]
            if ($colors.ContainsKey($verdict)) {
                $ws.Range($ws.Cells.Item($r,1), $ws.Cells.Item($r,7)).Interior.Color = $colors[$verdict]
            }
        }
        $r++
    }

    # 헤더 서식 + 틀고정 + 자동필터 + 열너비
    $hdr = $ws.Range($ws.Cells.Item(1,1), $ws.Cells.Item(1,7))
    $hdr.Font.Bold = $true
    $hdr.Interior.Color = 4210752
    $hdr.Font.Color = 16777215
    $ws.Rows.Item(1).AutoFilter() | Out-Null
    $ws.Activate()
    $xl.ActiveWindow.FreezePanes = $false
    $ws.Range("A2").Select() | Out-Null
    $xl.ActiveWindow.FreezePanes = $true
    $ws.Columns.Item(1).ColumnWidth = 11
    $ws.Columns.Item(2).ColumnWidth = 16
    $ws.Columns.Item(3).ColumnWidth = 52
    $ws.Columns.Item(4).ColumnWidth = 40
    $ws.Columns.Item(5).ColumnWidth = 36
    $ws.Columns.Item(6).ColumnWidth = 12
    $ws.Columns.Item(7).ColumnWidth = 34
    $ws.Range($ws.Cells.Item(1,1), $ws.Cells.Item($r-1,7)).VerticalAlignment = -4160
    Write-Host ("{0,-10} {1,4} rows" -f $name, ($r - 1))
    $i++
}

# 남는 기본 시트 제거
while ($wb.Worksheets.Count -gt $files.Count) {
    $wb.Worksheets.Item($wb.Worksheets.Count).Delete()
}

$wb.Worksheets.Item(1).Activate()
if (Test-Path $out) { Remove-Item $out -Force }
$wb.SaveAs($out, 51)   # 51 = xlOpenXMLWorkbook
$wb.Close($false)
$xl.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($xl) | Out-Null
Write-Host "saved: $out"
