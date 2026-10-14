---
name: admin-sales-day-statistics-port
description: 통계>일자별 매출(shop-statistics/sales/day) AS-IS 이식 - 17칸 2단헤더·취소 음수 규칙·주문방법 축은 TO-BE에 데이터가 없어 "-"
metadata:
  type: project
---

# 통계 > 일자별 매출 (AS-IS `/opmanager/shop-statistics/sales/day`)

2026-10-13 이식. TO-BE에 있던 화면은 **발명된 축소판**이었다 - 5칸짜리 표
(기간/결제건수/결제포인트/취소건수/취소포인트)에 "전액 포인트결제 기준. 미지정 시 최근 30일."
이라는 **AS-IS에 없는 안내문구**까지 달려 있었다. AS-IS 사양으로 다시 만들었다.

## AS-IS 사양 (day.jsp 128줄 + include/date-list.jsp 130줄)

- **GET 진입에서는 조회하지 않는다**(빈 표). POST에서만 집계한다 - 컨트롤러가 GET에서 모델에
  아무것도 안 담는다.
- 검색조건은 **기간 + "소계 노출" 라디오** 둘뿐. 판매자 select 는 AS-IS가 통째로 주석처리
  상태라 켜지 않았다([[as-is-parity-includes-disabled-state]]).
- **날짜버튼이 6종**: 오늘/1주일/**15일**/한달/3개월/1년. 다른 화면의 5종과 다르다.
- 표는 **2단 헤더 17칸**: 일자(rowspan2) · 주문방법(rowspan2) ·
  결제 6(건수/상품판매가/할인/판매액/배송비/소계) · 취소 6(동일) · 합계 3(판매액/배송비/합계).
- **★취소는 전부 음수로 담는다**(건수까지 `-1`). 그래서 "합계 = 판매 + 취소"가 그대로 순액이다.
  요약줄의 "취소 : -10건" 도 AS-IS가 음수를 그대로 찍는 것이다 - 버그 아님.
- 파생식(AS-IS `BaseStats`): 소계 = 판매액 + 배송비 · 합계판매액 = 판매액 + 취소판매액 ·
  합계 = 합계판매액 + 합계배송비 · 총주문건수 = 판매건수 + 취소건수.
- 소계 노출 'N'일 때만 표에 `odd-even` 줄무늬 클래스가 붙는다.
- 엑셀 버튼은 `<sec:authorize access="hasRole('ROLE_EXCEL')">` 안에 있다.

## ★ROLE_EXCEL은 운영자에게 없다 - 엑셀 버튼은 AS-IS에서도 안 보인다

`ROLE_EXCEL` 은 AS-IS에서 **판매자 계정에만** 붙는다(`SellerServiceImpl`·`SellerUserServiceImpl`).
운영자에게 주는 코드가 없고 `OP_ROLE` 에 행도 없다. 즉 **AS-IS 운영자 화면에서도 이 버튼은
뜨지 않는다.** 비활성 상태 그대로 옮겼다 - 엔드포인트
(`/shop-statistics/sales/day/excel-download`)는 만들어 뒀으므로 권한만 주면 바로 쓸 수 있다.

## ★주문방법(DEVICE_TYPE) 축 - TO-BE에 데이터가 없다

AS-IS는 `OP_ORDER_ITEM.DEVICE_TYPE`(PC/MOBILE/IOS/ANDROID, enum `DeviceType`)으로 **한 일자를
여러 행으로 쪼개고** 일자 셀에 rowspan을 건다. TO-BE 실제 주문표 `ord.od_order`(37컬럼, 44행)에
**그 컬럼이 없다**(실측 확인). 나머지 16칸은 전부 계산 가능하다:
`unit_price`·`option_price`·`quantity`·`discount_amount`·`delivery_fee`.

**사용자 결정(2026-10-13)**: 컬럼 구조는 AS-IS 그대로 두고 **값만 "-"** 로 표시한다.
→ 일자당 한 행이 된다. 나중에 수집을 켜면 그대로 채워진다.
(대안이던 "DEVICE_TYPE 컬럼 신설 + UA 수집"은 기존 44건을 소급할 수 없어 보류.)

## 금액 매핑 (AS-IS 컬럼 → TO-BE)

| AS-IS | TO-BE |
|---|---|
| 상품판매가 `(PURCHASE_PRICE + OPTION_PRICE) * QUANTITY` | `(unit_price + option_price) * quantity` |
| 할인 (쿠폰·판매자·스팟·등급·세트 **5종 합**) | `discount_amount` **하나** (TO-BE는 분리 보관 안 함) |
| 판매액 `SALE_PRICE * QUANTITY` | 상품판매가 − 할인 |
| 배송비 | `delivery_fee` |
| 취소 판정 `SALES_CANCEL_DATE` | `order_status = 'CANCELLED'` (나머지는 판매분) |

## 구현

- **order** `OrderDateStatsService` 신규(네이티브 집계, day/month/year 공용) +
  `GET /api/admin/orders/stats/date`
- **admin** `OrderAdminClient.dateStats()` + `DateStatRow`(파생식은 AS-IS BaseStats 그대로 record 메서드로)
- **admin** `ShopStatisticsTierBController` - GET/POST/엑셀 3개. 기존 `period()` 는
  month/year 만 맡도록 분리(그 둘은 아직 옛 축소판 `sales-period.html` 이다 - 같은 방식으로
  보강하면 된다. AS-IS도 `date-list.jsp` 를 셋이 공유한다)
- **템플릿** `shop-statistics/sales-day.html` 신규

**검증**: GET 진입 빈 표 · 헤더 1행 `일자/주문방법/결제(6)/취소(6)/합계(3)` · 2행 15칸 ·
날짜버튼 6종 · 소계 라디오 N기본 · 엑셀버튼 숨김 · 검색 후 12일자 ·
합계 `34 / 1,876,000 / 0 / 1,876,000 / 6,000 / 1,882,000 / -10 / -638,000 / 0 / -638,000 /
-14,000 / -652,000 / 1,238,000 / -8,000 / 1,230,000`(파생식 전부 일치) ·
소계 노출 ON → 소계행 12 + `odd-even` 제거 · 콘솔/HTTP 오류 0.
order·admin `test`+`bootJar` EXIT=0.

## 월별 매출(sales/month) 이식 (2026-10-13)

일자별과 **표가 같아서** AS-IS처럼 조각 하나를 공유하게 바꿨다
(`fragments/stats-date-list.html` 의 `table(...)`·`summary(...)` - AS-IS `include/date-list.jsp` 대응).
일자별 화면도 이 조각을 쓰도록 전환했고, 엑셀 작성도 `writeDateStatsExcel()` 로 합쳤다
(AS-IS Day/Month ExcelView 가 같은 컬럼이다).

**일자별과 다른 점만**
- 기간이 datepicker 가 아니라 **년/월 select 4개**(startYear/startMonth/endYear/endMonth).
  년도는 AS-IS 그대로 **2010~올해**, 월은 01~12 두 자리.
- **날짜 버튼이 없다.**
- 기간 → 날짜 변환은 AS-IS `getStartDate()/getEndDate()` 의 month 분기 그대로:
  `startYear+startMonth+"01"` ~ `endYear+endMonth+**"31"**`(말일 보정 없이 31을 쓴다).
- 네 값 모두 **게터가 기본값(올해/이번 달)을 만든다** - 일자별과 같은 패턴.
- 표의 일자 칸이 **그 달의 일자별 화면으로 드릴다운**한다.
  일자별 화면은 AS-IS가 onclick 을 주석처리해 아무 동작도 안 한다.

### 결제타입별 매출(sales/payment) 이식 (2026-10-13)

AS-IS payment.jsp 262줄. 앞의 세 화면과 달리 **표가 둘**이고 구조가 꽤 다르다.

- 검색: 기간(datepicker + 날짜버튼 6종, 일자별과 동일) + **결제타입 체크박스**(ORDER_PAY_TYPE 코드).
  ★체크박스는 **데이터를 거르고 열은 늘 전체를 그린다**(AS-IS 매퍼가 approvalTypes 로 IN 필터).
- 표① 결제타입별 총 매출금액 요약(열 = 전체 코드), 표② **날짜 × 결제타입 × 기기(PC/모바일/기타)** 피벗.
- 표②는 **결과가 있을 때만** 그린다. 날짜 행은 조회 결과가 아니라 **기간 전체를 하루씩**
  (endDate → startDate 역순) 만든다 - 값 없는 날도 0원 행이 나온다.
- 금액 색: 매출총액 칸은 0 회색 / 음수 빨강 굵게 / 양수 **파랑** 굵게, 타입별 칸은 0 회색 / 그 외 굵게.
- `.board_list` 에 `overflow-x:auto`, 꼬리 `<style> .board_list_table td{border:1px solid #ECECE1}`.
- ★h3·location 문구가 **"일자별 매출"** 로 되어 있다(AS-IS 복붙 흔적) - 어차피 op.manager.js 가
  메뉴명으로 덮어쓰므로 화면에는 "결제타입별 매출"이 보인다. 그대로 뒀다.
- ★엑셀만 `Shop.downloadExcelOrder(..., **false**)` 라 **개인정보 사유 모달을 거치지 않는다**
  (다른 엑셀 화면과 다르다).

**★데이터가 없는 축이 둘 - 사용자 결정(2026-10-13)으로 구조는 AS-IS 유지**
1. **결제타입**: TO-BE 주문표에 결제수단 컬럼이 없다. 전액 기부포인트 결제이기 때문
   (`OrderReconciliationRepository` 주석 "TO-BE는 결제수단이 포인트뿐"). → 코드 12종 열을 그대로
   두고 **`give-point`(기부포인트) 열에만 금액**, 나머지 11종은 0원.
2. **기기**: device_type 부재(주문방법 때와 같은 사정). → PC/모바일/기타 3칸을 그대로 두고
   **금액은 전부 "기타"** 에 넣는다(PC·모바일 0원).
   → 12×3 = **36칸 중 1칸에만 값**이 들어간다. 수집이 생기면 그대로 갈라진다.

**집계는 기존 것을 재사용**했다 - `dateStats("day", ...)` 의 `totalPayAmount()`(= 판매 + 취소,
취소가 음수라 순액)가 AS-IS의 `payAmount - cancelAmount` 와 같다. 새 API를 만들지 않았다.

**★발명 코드 제거 + 매핑 충돌**: 기존에 같은 URL(`/shop-statistics/sales/payment`)을 쓰는
축소판 메서드가 이미 있어서 **`Ambiguous mapping` 으로 기동 실패**했다(테스트 3건이 그래서 깨짐 -
DB 문제처럼 보였지만 아니었다). 옛 메서드를 제거했다. 서비스의 `paymentStats()` 는 호출부가
사라졌지만 남겨 뒀다(다른 축소판 화면들과 함께 정리할 때 같이 볼 것).

**검증**: GET 진입 기간 `20261001~20261013`(이번 달 1일~오늘)·체크박스 12개·안내문구 ·
검색 후 요약표 13칸(총액 955,000원 + 12종, **기부포인트 열만 955,000원**) ·
피벗 헤더1 14칸(날짜·매출총액 + 12종 colspan=3)·헤더2 **36칸** · 날짜 **13행**(값 없는 날 포함) ·
10/07 행 매출총액 322,000원 / 기부포인트 PC·모바일 0원, **기타 322,000원** ·
`overflow-x:auto` 적용 · 콘솔/HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

## ★★기간 조건은 timestamp 비교로 - AS-IS 문자열 방식을 물려받지 않는다 (2026-10-13 사용자 지시)

**AS-IS가 `99`를 쓸 수 있었던 이유**: AS-IS 날짜 컬럼이 전부 `VARCHAR(yyyyMMddHHmmss)` 라
조건이 `SALES_DATE BETWEEN CONCAT(#{startDate},'000000') AND CONCAT(#{endDate},'595959')`
**문자열 비교**다. 그래서 ① 없는 날짜 `20261099` 도 에러 없이 "상한"으로 동작하고
② `'595959'`(59시59분59초) 같은 값으로 하루 전체를 덮는 것도 가능했다.
**JSP/EL 에서 말일(28/29/30/31)을 계산할 방법이 마땅치 않아 생긴 편법**이지, 의도된 설계가 아니다.

TO-BE `ord.od_order.created_date` 는 **진짜 timestamp** 라 그 사고방식을 가져올 이유가 없다.
사용자 지시("99 붙이는 건 말도 안 된다, 쿼리로 잡아야지")로 `OrderDateStatsService` 의 WHERE 를
바꿨다:
```sql
-- before: to_char(o.created_date,'YYYYMMDD') >= :startDate  (컬럼에 함수 → 인덱스 못 탐)
-- after
and o.created_date >= :startTs
and o.created_date <  :endTsExclusive   -- 종료일 +1일, "미만" 비교
```
- 종료일을 **다음날 00:00 미만**으로 잡아 그 날 23:59:59 까지 빠짐없이 들어간다.
- 옛 링크·북마크가 `YYYYMM99` 를 들고 들어와도 깨지지 않게 `parseYmd()` 가 **일자를 말일로 당긴다**.
- GROUP BY 키는 `to_char(...)` 그대로다(필터가 아니라 키 생성이라 인덱스와 무관).

**검증**: DB에서 옛 문자열 방식 vs 새 timestamp 방식을 4개 구간으로 직접 대조 -
`20260101~20261231`(44건/2,644,500) · `20261001~20261031`(20/1,211,000) ·
`20260901~20260930`(24/1,433,500) · `20261007` 하루(6/398,000) **전부 일치**.
종료일 경계도 확인(10/07 마지막 주문 20:05 포함). 화면도 전후 동일하고,
**옛 `20261099` 를 넣어도 10월 결과가 그대로** 나온다(방어 동작 확인).

**남은 권고**: `ord.od_order` 에 인덱스가 **PK 하나뿐**이다(`od_order_pkey`). 지금은 44건이라
무관하지만 운영 데이터가 들어오면 `created_date` 인덱스가 필요하다 - DDL 변경이라 보류.

### ★AS-IS에서 의도적으로 벗어난 것 1건 - 드릴다운 endDate

AS-IS는 `?startDate=YYYYMM01&endDate=YYYYMM**99**` 를 넘긴다. 문자열 비교 상한으로는 동작하지만
**받는 일자별 화면의 기간 입력칸에 `20261099` 라는 없는 날짜가 그대로 찍힌다**
(AS-IS가 `getEndDate()` 에서 값을 그대로 돌려주기 때문). 처음엔 verbatim 으로 옮겼는데
사용자가 "잘못된 일자로 셋팅된다. 99" 로 지적해서 **실제 말일**을 넘기도록 바꿨다
(`@opDate.monthEnd()` 신설, 202610 → 20261031). **조회 범위는 둘이 완전히 같다** -
바뀌는 건 화면에 보이는 기간뿐이다. AS-IS와 똑같이 되돌리려면 이 한 곳만 `99` 로 돌리면 된다.
실측: 2026-10 → `20261001~20261031`, 2026-09 → `20260901~20260930`, 그 상태로 검색하면 10월 3일자만.
- 일자 칸 표기는 6자리라 `@opDate.**ym**()` 을 새로 만들어 썼다(yyyyMM → yyyy-MM).

**검증**: 기본값 2026/10~2026/10 · 년도 17개(2010~2026)·월 12개 · 날짜버튼 0 · GET 진입 빈 표 ·
2026/01~12 검색 시 2026-10/2026-09 두 달, 합계가 일자별과 완전히 일치
(`34 / 1,876,000 / … / 1,230,000`) · 드릴다운 클릭 → 일자별 화면에 `20261001~20261099` 전달 ·
일자별 회귀(소계 노출 12행, odd-even 해제) 정상 · 콘솔/HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

## 년도별 매출(sales/year) 이식 (2026-10-13) - 세 화면 완료

월별과 같은 모양이고 **년도 select 2개만**(월 select 없음, 2010~올해) 다르다.
기간은 `startYear+"0101"` ~ `endYear+"1231"`(AS-IS getStartDate/getEndDate 의 year 분기).
표의 일자 칸은 **그 해의 월별 화면으로 드릴다운**한다
(`?startYear=YYYY&startMonth=01&endYear=YYYY&endMonth=12` - AS-IS 그대로).

이로써 **일자별·월별·년도별 세 화면이 모두 AS-IS 사양**이고 조각 하나
(`fragments/stats-date-list`)를 공유한다 - AS-IS가 `include/date-list.jsp` 를 공유하는 것과 같다.
`viewType` 분기도 AS-IS와 같이 year→월별 / month→일자별 / day→동작없음 세 가지다.

**발명 코드 제거**: 옛 축소판 `shop-statistics/sales-period.html`(5칸 표 + "전액 포인트결제 기준"
같은 AS-IS에 없는 문구)은 참조가 사라져 **삭제**했다. 서비스의 `periodStats()` 는 다른 화면
(답례품 구매현황 전체·결제타입별 등)이 아직 쓰고 있어 남겼다.

**검증**: 년도별 GET 진입 - 년도 select 2개·월 select 0개·년도 17개(2010~2026)·소계 N·빈 표 ·
2025~2026 검색 → 2026 한 행 · 드릴다운 클릭 → 월별 화면에 2026/01~2026/12 전달 확인 ·
**세 화면 합계가 모두 1,230,000 으로 일치**(일자별 12행 / 월별 2행 / 년도별 1행) ·
콘솔·HTTP 오류 0. admin `test`+`bootJar` EXIT=0.

## ★기간 기본값은 "이번 달 1일 ~ 오늘" - 게터가 만든다 (2026-10-13 2차 지적)

사용자가 "겉보기엔 다 된 것 같지만 동일하게" 재지시해서 다시 대조하니 **기간 기본값**이 달랐다.

AS-IS `StatisticsParam` 은 필드에 기본값이 없고 **게터가 만든다**:
```java
getStartDate() → 비면 DateUtils.getToday("yyyyMM") + "01"   // 이번 달 1일
getEndDate()   → 비면 DateUtils.getToday("yyyyMMdd")         // 오늘
```
JSP가 `path="startDate"` 로 바인딩하니 **입력칸도 비어 보이지 않고**, 조건 없이 검색해도
**이번 달만** 집계된다. TO-BE는 필드를 그대로 돌려줘서 입력칸이 비고 **전체 기간**이 조회됐다
(겉보기엔 멀쩡해서 놓치기 쉽다).

→ `DayStatsParam` 게터에 같은 기본값을 넣었다. 엑셀 링크의 쿼리스트링도 게터를 쓰게 바꿔
목록과 엑셀이 항상 같은 기간을 본다.

**실측**: GET 진입에 `20261001 ~ 20261013` 이 채워지고, 조건 없이 검색하면 이번 달 3일자 /
14건 / 955,000원만 나온다(이전에는 전체 기간이 나왔다).

**How to apply:** AS-IS 검색 파라미터 클래스는 **필드가 아니라 게터에 기본값을 숨겨 두는** 경우가
있다. 화면이 비어 보이지 않는데 TO-BE만 비어 있으면 게터부터 열어볼 것.

**함정**: Playwright에서 **검색 버튼을 실제로 클릭하면 `/admin/login` 으로 튕긴다** -
[[playwright-smarteditor-submit-quirk]] 와 같은 증상이고 화면 버그가 아니다.
검증은 `document.getElementById('statisticsParam').submit()` 으로 우회한다.
