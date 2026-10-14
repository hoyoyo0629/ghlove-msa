---
name: admin-excel-no-column-direction-mismatch
description: 엑셀 No 컬럼을 ++no(오름차순)로 짜면 화면의 내림차순 No와 어긋난다 - AS-IS는 화면마다 방향이 다르니 소스를 봐야 한다. 2026-10-14 사용자 지적으로 3곳 발견·수정
metadata:
  type: feedback
---

지자체별(sales/locgov)·농협 사업자 답례품 현황(sales/nh-item-sales) 엑셀을 새로 만들며 "No"
컬럼을 `int no = 0; ... String.valueOf(++no)`(1부터 오름차순)으로 짰는데, **화면은
`pagination.itemNumber - i.count`(총건수부터 내려오는 내림차순)**을 쓰고 있어서 화면 No.5가
엑셀에서는 No.1로 나오는 불일치가 생겼다. 사용자가 스크린샷으로 직접 대조해 지적했다.

**AS-IS를 다시 보니 화면마다 방향이 다르다** - 짐작으로 통일하면 안 된다:
- `ShopStatisticsMoisExcelView`/`ShopStatisticsLocgovExcelView`(지자체별 매출 엑셀 2종):
  `length - (rowIndex - 2)` → **내림차순**(화면과 일치해야 맞다)
- `MonthLocgovStatExcel`류(농협 사업자 답례품 현황의 컨트롤러 직접 로직): `idx = list.size(); idx--`
  → **내림차순**
- `StatisticsLocgovExcelView`(관심지자체): `++index` → **오름차순** - 이 화면은 AS-IS 자체가
  화면(내림차순)과 엑셀(오름차순)이 서로 다르다. 버그처럼 보이지만 AS-IS가 실제로 그렇다 -
  "맞춰서 고치면" 오히려 AS-IS parity를 깨는 것이니 그대로 둔다.

**How to apply**: 엑셀에 No 컬럼을 추가할 때 "화면과 같은 방향이겠지"로 짐작하지 말고, 반드시
AS-IS 엑셀 View 클래스(`*ExcelView.java`)의 No 로직을 직접 찾아 확인할 것. 다른 화면과
다르더라도 그게 AS-IS 실제 동작이면 그대로 포팅한다. 검증도 HTTP status/content-type만으로
끝내지 말고 POI로 직접 복호화해 셀 값을 읽어 화면과 대조해야 이런 방향 불일치를 잡을 수
있다(이번에 content-type만 확인하고 넘어갔다가 놓쳤다).
