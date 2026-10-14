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

### ★결제타입 순서가 체크박스↔표마다 달라 보이던 것 - 2차 정렬 키 누락 (2026-10-14 사용자 지적)

`CommonCodeRepository.findByCodeTypeOrderByOrdering`(= `commonCodeService.labelsOf` 가 쓰는 쿼리)에
**`ordering` 뒤 2차 정렬 키가 없었다**. `ORDER_PAY_TYPE` 12종은 전부 `ordering IS NULL`(동률)이라
`ORDER BY ordering nulls first` 만으로는 **동률 사이 순서를 SQL이 보장하지 않는다** -
PostgreSQL이 그 순간의 실행계획대로 아무 순서나 돌려줘도 표준 위반이 아니다.
체크박스(GET 요청)와 표 헤더(POST 요청, 검색 후)는 **서로 다른 SQL 실행**이라 각기 다른 순서로
나올 수 있었다 - 사용자가 실제로 겪은 증상이다.

`c.id` 를 2차 키로 추가해 매 호출이 항상 같은 순서를 내도록 고쳤다
(`order by c.ordering nulls first, c.id`). 같은 문제가 있던
`findAllByOrderByCodeTypeAscOrderingAsc`(공통코드 관리 화면)도 같이 고쳤다 -
새로고침마다 동률 구간 순서가 바뀌어 보이는 것을 막는다. **`labelsOf` 는 다른 여러 화면도
공유해서 쓰므로**(알림·팝업 등) 이 수정이 그 화면들의 코드 순서도 같이 안정시킨다.

**검증**: 같은 GET 요청을 5회 반복해 체크박스 순서가 **매번 동일**함을 확인(수정 전에는
이론상 매 요청 달라질 수 있었다) · GET 체크박스 순서 == POST 체크박스 순서 == POST 표 헤더
순서 **완전히 일치**(수정 후 실측 동일 문자열) · admin `test`+`bootJar` EXIT=0.

### 체크박스 필터는 설계대로 동작한다 (같은 날 질문)

"결제타입 체크여부와 상관없이 다 조회되는게 맞아?" 질문에 대한 확인:
**체크 안 함(기본) → 전체 조회**가 AS-IS 규칙이다(매퍼가 `approvalTypes.length > 0` 일 때만
IN 필터를 건다 - 빈 배열이면 필터 자체가 안 걸린다). TO-BE도 `PaymentStatsParam.includesGivePoint()`
로 같은 규칙을 재현했다.

다만 **TO-BE 주문은 전액 기부포인트 결제뿐**이라(위 "데이터가 없는 축이 둘" 참고), 기부포인트를
포함해 체크하거나 아무것도 체크하지 않으면 늘 같은 결과가 나오고, 기부포인트를 **빼고** 체크하면
0건이 된다. 그래서 겉보기엔 "체크해도 안 해도 똑같다"로 보이기 쉽다 - 실제로는 필터가 걸리고
있고, 단지 테스트 데이터의 결제수단이 한 종류뿐이라 체感이 안 되는 것뿐이다.

**실측(2026-10-14)**: 미체크 → 1,230,000원(전체) · **신용카드+은행입금만 체크**(기부포인트
제외) → "등록된 데이터가 없습니다." / 0원(필터가 실제로 걸림 확인) · 기부포인트만 체크 →
1,230,000원(전체와 동일, 다른 결제수단 데이터가 없으므로).

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

## 답례품 구매현황 전체(sales/all) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 581~675줄 + `all.jsp`(416줄) + 매퍼
`getDateStatsListGhloveQuery`. 앞의 일/월/년/결제타입 네 화면과는 **다른 쿼리/다른 부호규칙**을
쓰는 별도 화면이라 그대로 재사용할 수 없었다.

- 검색조건은 **년도(YYYY 공통코드) 하나뿐**, GET/POST 둘 다 조회한다(일자별과 달리 GET에서도
  집계한다).
- `getDateStatsListGhloveQuery` 는 **`DEVICE_TYPE` 컬럼이 없다** - 카드가 한 세트뿐인 이유.
  ★취소를 **양수**로 담아 "합계 = 주문 − 취소"다(일/월/년 화면은 취소가 음수라
  "합계 = 판매 + 취소") - 여기서만 부호 규칙이 반대다. `AllSalesCard.of()` 에서
  `Math.abs()`로 변환해 맞췄다.
- 카드 3세트(주문/취소·반품/합계) × 2(건수/금액) + 월별 바차트 2개(금액/건수, 주문·취소반품
  2계열) - `ChartCommon.drawChart` 재사용(AS-IS 로컬 `drawChart` 함수 중복 안 함).
- 기존 축소판 `ShopStatisticsTierBService.allSales()`/`AllSalesSummary` 는 제거.

**검증(여분 포트, DB 직접대조)**: 2026년 조회 → 카드 "주문 38건/1,876,000원", "취소·반품
10건/638,000원", "합계 28건/1,238,000원" · 차트 라벨 1~12, 데이터는 9월(793,000/17건,
취소 510,000/7건)·10월(1,083,000/21건, 취소 128,000/3건)에만 정상 분포 · 콘솔/HTTP 오류 0.

★`od_order` 를 직접 쿼리해 재확인(2026년 비취소 38 / 취소 10 / 전체 48)했고 화면 숫자와
**완전 일치** - 이전에 일/월/년 화면에서 검증했던 "34건"과 다른 이유는 버그가 아니라 그 사이
개발DB에 테스트 주문이 더 쌓였기 때문(최신 주문 생성시각이 검증 당일 오전이었다). 같은 화면을
시차를 두고 재검증할 때는 항상 "그때는 맞았는데 지금 다르다"가 데이터 변화인지 버그인지
먼저 DB count로 가른다.

admin `compileJava`+`test`+`bootJar` EXIT=0.

## 지자체별 구매현황(sales/locgov) 이식 (2026-10-14) - mois/locgov 역할분기 3단 화면

AS-IS `ShopStatisticsManagerController` 675~924줄 + `sales/locgov-mois.jsp`(지자체 목록, 306줄)
+ `sales/locgov.jsp`(답례품 목록, 283줄) + `sales/detail.jsp`(회원별 상세 팝업, 88줄) + 매퍼
`getDateStatsListGhloveQuery`의 `extra="LOCGOV"/"ITEM"/"USER"` 분기. 이전 구현
(`ShopStatisticsTierBController` "M. 지자체별 구매현황")은 연도만 받는 발명된 축소판
(역할분기·WDR select·정렬·페이징·엑셀 전부 없음)이라 전면 교체했다. **별도 컨트롤러**
(`ShopStatisticsLocgovController`)로 분리했다(TierB와 매핑 경로는 그대로 `/shop-statistics/sales/locgov`
하나지만, 로직량이 커서 파일을 나눴다).

### AS-IS 사양 - 한 URL이 역할+파라미터로 세 화면을 낸다

- `GET/POST /sales/locgov`: **mois**(ROLE_ADMIN_1~4) 이고 `locgovCode` 없음 → **지자체별 목록**
  (locgov-mois.jsp). 그 외(**locgov**(ROLE_ADMIN_5~6)이거나, mois인데 locgovCode를 골랐음) →
  **답례품별 목록**(locgov.jsp). `MenuService.isLocgovScoped(manager)`가 AS-IS 역할분기와 같다
  ([[admin-interest-locgov-port]]에서 쓴 것 재사용).
- 지자체별 목록: WDR→LOCGOV_CODE 2단 select(캐스케이딩, 관심지자체와 같은 client-side
  `ALL_LOCGOVS` 필터 방식) + 년/월 select, **정렬가능 6칸**(주문/반품환불/합계 × 건수/금액,
  `orderBySelect`/`sort_arrow` AS-IS JS 그대로) + 페이징(10/20/50/100) + 좌측 엑셀 버튼.
  지자체명 클릭 → 같은 URL에 locgovCode를 꽂아 POST(`detail()` JS가 동적 form 생성).
- 답례품별 목록: 년/월 + 검색구분(답례품명/상호명)+검색어, 정렬 6칸 동일, 답례품명 클릭 →
  회원별 상세 **팝업**(`window.open`, 700x580). 엑셀 버튼 + **mois만** [목록] 버튼(scoped는
  목록 자체가 없다). AS-IS 주석 그대로 "개인정보 없어서 팝업 없이" - 엑셀 둘 다 사유모달 없음
  (`Shop.downloadExcelOrder(..., false)`).
- 상세 팝업: 회원별 주문/반품환불만(합계 칸 없음), 페이지당 **5건**, 정렬 UI 없음, 엑셀 없음.

### ★취소 양수, AS-IS 쿼리가 LOCGOV/ITEM/USER 세 분기를 공유

`getDateStatsListGhloveQuery`는 sales/all과 같은 "Ghlove" 계열 쿼리라 **취소를 양수**로 담고
"합계 = 주문 − 취소"다(일/월/년 화면과 부호가 반대). 세 분기 모두 `S.COMPANY_NAME`·`I.ITEM_NAME`·
`I.ITEM_USER_CODE`·`OI.LOCGOV_CODE`를 **공통으로 항상 선택**한다(GROUP_OBJECT만 분기별로 다름) -
처음에 USER 분기 SQL에 item_name을 null로 빼놓았다가 상세팝업 제목이 비는 버그를 냈다(아래 참고).

### ★데이터가 없는 필드 - "답례품 코드"(ITEM_USER_CODE)

TO-BE `gift.Gift` 엔티티에 AS-IS `OP_ITEM.ITEM_USER_CODE`(답례품 코드, PK인 ITEM_ID와 별개인
업무코드) 대응 컬럼이 **아예 없다**(이식 안 됨, DB 검색으로 확인). 주문방법(DEVICE_TYPE)·
결제타입과 같은 사정이라 같은 원칙으로 열 구조는 두고 값만 `"-"`로 비웠다.

### 구현

- **order** `OrderLocgovStatsService` 신규 - `byLocgov`/`byItem`/`byUser` 세 메서드, 전부
  `ord.od_order`(locgov_code·item_id·item_name·seller_id·user_id가 이미 비정규화돼 있어
  order 혼자 집계 가능) 네이티브 쿼리. **지자체명(donation G_LOCGOV)·상호명(gift OP_SELLER)·
  회원명(member)은 다른 서비스 소유**라 order는 원시 키(`GroupRow`)만 돌려주고 admin이
  `LocgovClient`/`GiftClient.sellers()`/`MemberClient.fetchOrNull`로 각각 이름을 붙인다
  (관심지자체와 같은 cross-service enrichment 패턴, N+1 허용 - 데이터가 소량). 신규 엔드포인트
  `GET /api/admin/orders/stats/locgov-group`(extra/startYearMonth/endYearMonth/locgovCode/itemId).
  ★기간은 [[order-date-timestamp-comparison]]과 같은 원칙으로 timestamp 비교(월 범위
  `>= 시작월1일 and < 종료월+1개월1일`) - AS-IS `locgovSearchStartYearMonth+'00000000'~
  locgovSearchEndYearMonth+'99999999'` 문자열 상한을 물려받지 않는다.
- **admin** `OrderAdminClient.locgovGroupStats()` + `GroupRow` 레코드(order와 같은 모양).
  `ShopStatisticsLocgovService` 신규 - `locgovRows`/`itemRows`/`userRows`/`itemNameOf` 네
  메서드로 enrichment 담당. `ShopStatisticsLocgovController` 신규 - GET/POST 목록(역할분기),
  엑셀 2종, 상세 팝업. **정렬·필터·페이징은 메모리에서** 처리(관심지자체와 같은 패턴 - 그룹
  건수가 소량이라 서버사이드 페이징 불필요). `ShopStatisticsTierBController`/`Service`의 옛
  "M. 지자체별 구매현황"(`locgov`/`locgovDetail`/`locgovStats`/`locgovUserStats`/`LocgovStat`)은
  제거(매핑 충돌 방지 - [[admin-sales-day-statistics-port]] 결제타입별 때 겪은 Ambiguous
  mapping과 같은 함정이라 미리 제거).
- **템플릷** `shop-statistics/sales-locgov-mois.html`(신규) · `sales-locgov.html`(전면 재작성) ·
  `sales-locgov-detail.html`(전면 재작성, AS-IS detail.jsp 그대로 팝업 레이아웃).

### ★★CSS 함정 발견 - `<span>`으로 건수를 감싸면 "총 N건"이 세 줄로 쪼개진다

`opmanager.css:1123` `.count_title h5 span{display:block;...}`가 **h5 안의 span을 블록으로
바꾼다** - AS-IS JSP는 `<c:out>`(태그 없음)이라 문제가 없는데, Thymeleaf로 옮기며
`<span th:text="${count}">0</span>`처럼 감싸면 "총" "12" "건"이 세 줄로 쪼개져 보인다.
기존 코드 40여 화면 대부분이 `th:remove="tag"`를 붙여 렌더링 결과에서 span 자체를 지우는
식으로 이미 피해가고 있었는데(`member-admin/list.html` 등), **이 패턴을 안 쓴 화면 3곳이
이미 쇼핑통계 작업 이전부터 같은 버그 상태였다**: `statistics-locgov/list.html`(관심지자체,
2026-10-14 같은 세션 작업분)·`designated/approval-log-popup.html`·`featured-admin/list.html`.
이번에 발견해 **5곳 전부**(위 3곳 + 신규 2개) 수정했다 - 스크린샷으로 수정 전/후 비교해
"총 12건"이 한 줄로 돌아오는 것 확인.

**How to apply**: `<h5>총 <span th:text="...">...</span>건</h5>` 패턴을 새로 쓸 때마다
`th:remove="tag"`를 span에 붙일 것. `count_title` 쓰는 화면을 포팅할 때 체크리스트에 추가.

### ★테스트데이터 함정 - 상호명 "-" 는 버그가 아니라 seller_id 불일치

검증 중 답례품별 목록의 "상호명"이 전부 `-`로 나와 처음엔 버그로 의심했다. DB 직접조회로
확인한 결과 `ord.od_order.seller_id`(501~508)와 `gift.op_seller.seller_id`(9001~9015,
9502/9504/9509)가 **전혀 겹치지 않는다** - 관리자 수기등록 주문 샘플데이터([[admin-order-manual-registration-port-progress]])의
seller_id가 가짜값이라 실제 판매자 테이블과 매칭되지 않는 것뿐이다. 코드의 `getOrDefault(sellerId, "-")`
폴백이 의도대로 동작한 것 - 실데이터가 들어오면 정상적으로 상호명이 채워진다.

### 검증 (Playwright, 여분 포트, 두 계정)

- **mois**(pwtest_sys): GET 목록(조회 안 함) - 지자체 2단 select·년/월 select·안내문구 확인 ·
  검색(조건없음) → 총 12건, 10+2 페이지 분할 ·  **정렬**(건수·금액 6키 × asc/desc) 실측
  일치(orderPoint desc → 360,000/320,000/225,000 내림차순 확인, 기본정렬은 지자체명 가나다순
  확인) · 지자체 클릭 drill-down → 답례품별 목록(품목 1건, [목록] 버튼 보임) · 답례품명 클릭 →
  상세 팝업(700x580, 제목 "한우 선물세트", 회원 3명, 합계 칸 없음) · 검색구분(상호명/답례품명)
  필터 정상(없는 상호명 → 0건, "한우" → 1건 정확히 매칭) · itemsPerPage 변경 정상 · 엑셀 2종
  (지자체별·답례품별) 200 응답 + 올바른 xlsx content-type.
- **locgov**(pwtest_loc, 11230): GET 진입 즉시 답례품별 목록(자기 지자체로 강제) · [목록] 버튼
  없음 확인 · 다른 지자체(26350) locgovCode를 쿼리로 직접 넣어도 **차단되어 자기 지자체 유지**
  (관심지자체와 같은 보호).
- 콘솔/HTTP 오류 0건. 스크린샷으로 세 화면(지자체목록/답례품목록/상세팝업) 전부 레이아웃·버튼
  정상 확인(위 CSS 수정 반영 후).
- order·admin `test`+`bootJar` EXIT=0.

## 월별 지자체별 답례품 현황(sales/month-locgov) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 2332~2489줄 + `sales/month-locgov.jsp`(409줄) + 매퍼
`getMonthLocgovItemStat`(DENSE_RANK·GROUP_CONCAT 범벅의 196줄 CUBRID SQL) + `MonthLocgovStatExcel`.
이전 구현(`ShopStatisticsTierBController` "L. 월별 지자체별 답례품 현황")은 연/월만 받아 지자체
TOP3 주문금액 순위만 보여주는 축소판(7칸 카테고리 현황·WDR select·엑셀 전부 없음)이라 전면
교체했다.

### ★처음엔 "SalesOn 미사용"으로 오판할 뻔했다 - 실은 TO-BE에 1:1 대응 체계가 이미 있었다

표 왼쪽 절반("지자체별 답례품 선정/등록 현황")은 AS-IS가 `OP_CATEGORY_GROUP.CODE`
(tour/farm/aquatic/process/daily/ticket 6종)로 품목을 분류한다. TO-BE gift 서비스의
`op_category_group`/`op_category_team`은 전혀 다른 용도(기획전/특가전)로 재사용되고 있어서
([[gift-category-tree-parity-audit]]가 말하는 "SalesOn op_category 4단은 0건·보류"와 같은
테이블명이라 처음엔 [[defer-saleson-dependent-unused-features]] 대상으로 오판할 뻔했다.

**실제로는** `gift.Gift.categoryCode` 필드(TOUR/AGRI/SEAFOOD/PROCESSED/LIVING/VOUCHER)가
AS-IS 6종과 **1:1 대응**하고(gift 자체 `OP_COMMON_CODE` "GIFT_CATEGORY" 라벨까지
관광서비스/농축산물/수산물/가공식품/생활용품/지역상품권으로 정확히 일치, AS-IS CUBRID
`OP_CATEGORY_GROUP`과 라벨·순서 동일 확인) `GiftPublicApiController.CATEGORY_ICONS`에 이미
스토어프론트 아이콘 매핑으로 박혀 있었다 - storefront 카테고리 메가메뉴가 쓰는 **살아있는
분류체계**였다. AS-IS가 OP_ITEM_CATEGORY(품목-카테고리 다대다, 51,027건 실데이터 확인)로
하던 걸 TO-BE는 품목에 직접 category_code 하나(다대일, 더 단순)로 재설계해 둔 것뿐이었다.

**교훈**: 테이블명이 같다고 같은 기능이 아니다 - 실데이터·실제 소비 코드(`GiftPublicApiController`
같은)까지 확인해야 "미사용"과 "이미 포팅됨, 다른 구조"를 구분할 수 있다.

### AS-IS 사양 - 표 왼쪽은 월과 무관한 카탈로그 스냅샷

- 왼쪽 7칸(합계+6카테고리)은 **"현재 승인·노출중"인 답례품 종류 수** - 선택한 년/월과
  **무관**하다(SQL을 재확인: 카테고리 카운트가 월별 주문 서브쿼리 LEFT JOIN **바깥**에서
  카탈로그 자체를 센다). 오른쪽 4칸(주문건수/금액/TOP3)만 월 필터가 걸린다. 칼럼명
  "선정/등록 현황" vs "구매 현황"의 의미 그대로였다.
- TOP1/2/3(최다판매 품목)은 **동률 허용**(AS-IS DENSE_RANK + GROUP_CONCAT) - 동률이면 이름을
  줄바꿈으로 묶고 건수는 하나만 보여준다(동률이니 값이 같다). "합계(전체)" 요약 3행 블록은
  **검색조건(지자체)으로 좁혀진 결과 안에서의 합계**이고 항상 표 맨 위에 고정으로 나온다
  (AS-IS `list[0].*TotalAll` 하드코딩 블록 재현).
- 취소 주문은 집계에서 아예 뺀다(별도 취소 열이 없다 - AS-IS `ORDER_STATUS IN (...)` 활성
  상태만 포함).
- 페이지네이션 없음(AS-IS도 itemsPerPage select가 주석처리돼 꺼져 있다).

### 구현

- **gift** `GiftApiController.allForResync()`/`ItemSnapshotDto`에 `displayFlag` 추가(기존엔
  dataStatusCode만 있어 "승인+노출중" 전체를 못 걸렀다) - 다른 화면이 이미 쓰던 공유 DTO라
  필드만 얹었다(하위호환, 기존 호출부는 그대로).
- **order** `OrderLocgovStatsService.byItemAllLocgovs()` 신규 - [[admin-sales-day-statistics-port]]
  지자체별(sales/locgov) 작업 때 만든 `byItem()`의 "전체 지자체 버전"(locgovCode 필터 없이
  전체를 한 번에 품목 단위로 그룹). 기존 `/api/admin/orders/stats/locgov-group` 엔드포인트에
  `extra=ITEM_ALL` 분기만 추가해 재사용했다(새 엔드포인트 안 만듦).
- **admin** `ShopStatisticsMonthLocgovService` 신규 - 카탈로그 스냅샷(`giftClient.allForResync()`
  APPROVED+Y 필터, locgov×categoryCode로 집계) + 월별 주문 통계(order API) + 동률 TOP3(Java
  Comparator 기반 dense rank, SQL 윈도우함수 대신 메모리 계산 - 데이터량이 작아 충분)를 조합.
  `ShopStatisticsMonthLocgovController` 신규 - GET/POST + 엑셀(2행 헤더 병합 + "합계(전체)"
  고정행, `ExcelDownloadWriter`의 단일헤더 헬퍼로는 부족해 POI를 직접 써서 AS-IS
  `MonthLocgovStatExcel`의 병합 구조를 그대로 재현).
  `ShopStatisticsTierBController`/`Service`의 옛 "L." 섹션(`monthLocgov`/`monthLocgovTop3`/
  `LocgovTop3Row`)과, 그것만 쓰던 `currentYear()`/`currentMonth()`(다른 호출부 없음 확인 후
  죽은코드로 같이 제거)를 삭제했다.
- **템플릿** `shop-statistics/sales-month-locgov.html` 전면 재작성 - 2행 헤더(rowspan2 지역·
  지자체명 + colspan7/colspan4 그룹) + 지자체마다 3행(rowspan) TOP1/2/3 구조.
  `th:remove="tag"`를 처음부터 넣어 [[admin-count-title-span-block-css-trap]]을 재발 없이
  피했다.

### 검증 (Playwright, 여분 포트 3개 - admin:8099/order:8095/gift:8096)

- GET 진입: 전부 0·"데이터가 없습니다"(AS-IS GET이 `Collections.EMPTY_LIST`를 하드코딩하는 것과
  동일) · 지자체 2단 select·년(4)/월(12) select 정상.
- 검색(2026-10, 전체): 총 246건(활성 G_LOCGOV 전체 행수) · 합계(전체) 카테고리
  `23(=14+3+2+2+2+0)` - **DB 직접조회로 실측 일치**(카탈로그 AGRI 14/SEAFOOD 3/PROCESSED 2/
  LIVING 2/VOUCHER 2/TOUR 0) · 주문건수 21·금액 1,083,000 - **sales/all 10월 수치와 교차
  일치**(같은 데이터 소스이므로 당연하지만 두 독립 구현이 같은 답을 낸 것으로 교차검증) ·
  TOP1 "곡물 선물세트\n전통주 세트\n지역 특산 꿀 2병"(동률 3건, DB 확인) 정상 표시.
  서울특별시 중구 행: 주문1건/32,000원/TOP1 "지역 특산 사과 5kg"(1건) - DB와 일치.
- 지자체 필터(강남구=11230)로 좁히기: 합계(전체)가 그 지자체 수치로 그대로 수축(카테고리
  8/0/8/0/0/0/0=AGRI 8건, DB와 일치) · 표 1행으로 줄어듦.
- 엑셀 200 응답 + 올바른 xlsx content-type.
- 콘솔/HTTP 오류 0건. 스크린샷으로 레이아웃·버튼·2행 헤더 병합·3행 rowspan 전부 정상 확인.

★테스트데이터 함정: 강남구의 10월 주문 2건이 금액 0원으로 나왔는데, DB 확인 결과
`unit_price IS NULL`인 테스트 행(오늘 방금 생성된 것으로 보임)이라 `coalesce(unit_price,0)`가
의도대로 0을 돌려준 것 - 집계 로직 버그 아님.

gift·order·admin `test`+`bootJar` EXIT=0.

## 농협 사업자 답례품 현황(sales/nh-item-sales) 이식 (2026-10-14)

AS-IS `ShopStatisticsManagerController` 3270~3409줄 + `sales/nh-item-sales.jsp`(273줄) + 매퍼
`getNhItemSalesStats`(슬레이브 전용 `SlaveShopStatisticsMapper`, 196줄). 이전 구현
(`ShopStatisticsTierBController` "Q. 농협 사업자 답례품 현황")은 **완전히 잘못 짚었다** -
판매자 `bankName`에 "농협"이 포함되는지로 걸렀는데, AS-IS는 그게 아니다.

### ★★"농협 사업자"의 진짜 정의 - bankName 아니라 사업자번호 매칭

AS-IS는 `G_NH_STAT_DATA_TEMP`(외부에서 받은 농협 소속 사업자번호 목록, AS-IS CUBRID 실측
**6,779건**)와 `OP_SELLER.BUSINESS_NUMBER`를 매칭해 농협 판매자를 가린다. "은행 계좌가
농협인지"와 "그 사업체가 농협 소속인지"는 전혀 다른 질문이다 - 이전 구현은 질문 자체를
잘못 세웠다.

TO-BE에 **DDL은 이미 있었다**(`database/ddl/service-admin.sql`의 `G_NH_STAT_DATA_TEMP`,
admin 스키마)인데 **테이블이 비어 있었다**(0건). AS-IS CUBRID에서 6,779건을 그대로 export해
TO-BE admin 스키마에 적재했다(엔티티 `NhStatDataTemp`/리포지토리 신규 - DDL만 있고 JPA
연결은 안 돼 있었다). 검증용으로 기존 판매자(seller_id=9002, 실제 주문 이력 있는 판매자) 한
곳의 business_number를 적재한 번호 중 하나(`101-82-13600`)로 세팅해 종단간 검증했다.

### 품목 목록은 카탈로그 전체 - 승인·노출 필터가 없다

AS-IS 베이스 서브쿼리(`OP_ITEM LEFT JOIN OP_ITEM_CATEGORY`)에는 [[admin-sales-day-statistics-port]]
월별 지자체별 답례품 현황의 카탈로그 스냅샷과 달리 `DATA_STATUS_CODE`/`DISPLAY_FLAG` 필터가
**아예 없다** - 농협 판매자의 모든 품목(승인대기·중지·비노출 포함)이 전부 나온다. 실제로
`DISCONTINUED`/`display_flag='N'` 품목도 화면에 뜨는 게 맞다(검증에서 실측 확인).

### ★AS-IS `ORDER_STATUS = '40'` 단일코드 - 다른 화면들의 "취소 아닌 전부"보다 좁다

일/월/년 화면군은 `ORDER_STATUS <> CANCELLED`(취소 아니면 전부)를 쓰는데, 이 화면은
`ORDER_STATUS = '40'` **딱 하나**로 좁힌다. TO-BE `order_status` 값 집합
(WAITING_DEPOSIT → CONFIRMED → CANCELLED)에서 '40'에 대응하는 값은 결제가 확인된
`CONFIRMED` 하나뿐이라고 보고 그렇게 좁혔다(입금대기는 AS-IS도 포함 안 하는 것으로 해석).

### ★"초기화" 버튼의 AS-IS 링크는 명백한 복붙 버그 - 자기 자신으로 되돌렸다

AS-IS JSP의 초기화 버튼은 `/remittance/expected/list`(정산예정목록, 전혀 다른 화면)로
이동한다 - 다른 화면(정산/이체) 템플릿을 복붙하며 안 고친 흔적이다. 이 시리즈의 다른 모든
화면이 예외 없이 "초기화 → 자기 자신"이고, 이 링크를 그대로 두면 사용자가 엉뚱한 화면으로
튕기는 실질적 불편이라 판단해 자기 자신으로 되돌아가게 고쳤다(AS-IS 문구·레이아웃·집계
로직은 전부 verbatim, 이 한 군데만 명백한 오류로 보고 교정).

### "하위카테고리"·"상품코드" - 데이터 없음

AS-IS `CATEGORY_DETAIL_NMS`(OP_CATEGORY 세부분류)·`ITEM_USER_CODE`는 TO-BE 답례품 모델에
대응 필드가 없다(지자체별 화면의 "답례품 코드"와 같은 사정) - "-"로 비웠다. "카테고리"
(상위, GIFT_CATEGORY 6종)는 월별 지자체별 답례품 현황과 같은 라벨 매핑을 재사용했다.

### 구현

- **DDL** 변경 없음(테이블은 이미 있었다, 데이터만 적재).
- **admin** `NhStatDataTemp` 엔티티 + `NhStatDataTempRepository` 신규.
  `ShopStatisticsNhItemSalesService` 신규 - NH 사업자번호 집합(admin 자체 DB, 빠름) +
  `giftClient.sellers()`(businessNumber 매칭으로 NH 판매자 추출) + `giftClient.allForResync()`
  (그 판매자들의 전체 품목, 상태 필터 없음) + 주문 통계(order 신규 API)를 조합. 검색구분
  (사업자번호/아이디/상호명/답례품명)·WDR 지자체 필터·페이징(10/50/100/200/500, 다른 화면과
  다른 증가폭)은 메모리에서 처리(관심지자체·지자체별과 같은 패턴).
  `ShopStatisticsNhItemSalesController` 신규 - GET/POST + 엑셀(사유모달 없음, AS-IS
  `Shop.downloadExcelOrder(..., false)` 그대로).
  `ShopStatisticsTierBController`/`Service`의 옛 "Q." 섹션(`nhItemSales()`, bankName 휴리스틱)을
  제거했다.
- **order** `OrderLocgovStatsService.nhItemSaleStats()` 신규 - `ORDER_STATUS='CONFIRMED'`
  단일조건으로 품목별 주문건수/판매수량(SUM(QUANTITY))/판매액을 집계. 새 엔드포인트
  `GET /api/admin/orders/stats/nh-item`.
- **템플릿** `shop-statistics/sales-nh-item-sales.html` 전면 재작성. `th:remove="tag"`를
  처음부터 넣어 [[admin-count-title-span-block-css-trap]]을 재발 없이 피했다.

### 검증 (Playwright, 여분 포트 3개 - admin:8099/order:8095/gift:8096)

- GET 진입: 전부 0·"데이터가 없습니다" · 지자체 2단 select·검색구분 5종·년(4)/월(12)·
  출력수(10/50/100/200/500) 전부 정상.
- 검색(2026-09, 전체): 총 5건 - seller_id=9002(NH 매칭) 소속 품목 5개 전부 노출(DISCONTINUED·
  display=N 포함 - 상태필터 없음 확인) · 카테고리 라벨(AGRI→농축산물, SEAFOOD→수산물) 정상 ·
  "해운대 건어물 세트" 주문1/판매1/35,000원, "선물포장 상자" 주문1/판매4/0원 - **DB 직접조회와
  정확히 일치**(두 품목만 실제 9월 CONFIRMED 주문 있음, 나머지 3품목은 0으로 올바르게 채워짐).
- 검색구분(상호명) 없는 값 → 0건 · 사업자번호(101-82-13600) 정확매칭 → 동일 5건(필터 정상
  동작, 데이터가 단일 판매자뿐이라 체感은 약함).
- itemsPerPage 변경 정상 · 엑셀 200 응답 + 올바른 xlsx content-type.
- 콘솔/HTTP 오류 0건. 스크린샷으로 레이아웃·버튼 정상 확인(13칸 표, 페이지네이션 조각 정상).

gift·order·admin `test`+`bootJar` EXIT=0.

## ★엑셀 No 컬럼 방향 버그 수정 (2026-10-14, 사용자 지적)

농협 사업자 답례품 현황 엑셀을 사용자가 스크린샷으로 화면과 대조해 **No 컬럼이 반대 방향**
(화면은 5→1 내림차순, 엑셀은 1→5 오름차순)임을 지적했다. 확인해보니 지자체별(sales/locgov)
엑셀 2개도 같은 실수였다(`int no = 0; ... ++no`로 짜서 오름차순이 되어 있었다) - 3곳 모두
AS-IS 엑셀 View 소스(`ShopStatisticsMoisExcelView`/`ShopStatisticsLocgovExcelView`는
`length-(rowIndex-2)` 내림차순, 농협은 컨트롤러의 `idx--` 내림차순)를 다시 대조해 전부
`no--`(내림차순)로 고쳤다. **단 관심지자체 엑셀(`StatisticsLocgovExcelView`)은 그대로 뒀다** -
AS-IS 자체가 `++index`(오름차순)라 화면(내림차순)과 원래 다르다. 자세한 내용과 재발 방지
절차는 [[admin-excel-no-column-direction-mismatch]] 참고.

POI로 복호화해 수정 후 셀 값 직접 대조(지자체별 목록 12→11→10→9→8, 농협 5→4→3→2→1) -
전부 화면 번호와 일치 확인. admin `test`+`bootJar` EXIT=0.

## 카테고리별(sales/categories) 이식 (2026-10-14)

AS-IS `salesCategories`(1599~1662줄) + `categories.jsp` + 슬레이브 매퍼 `getCategoriesStatsList`.
이전 구현(TierB "H.")은 카테고리코드별 건수/금액 한 줄짜리 축소판이라 교체. `/sales/category`
(AS-IS 미사용 엑셀 전용 별개 화면)의 redirect만 남겼다.

- 사양: 기간(startDate~endDate, 기본 이번달1일~오늘, 날짜버튼 + "전체"는 ROLE_ADMIN_1/5만) +
  지자체 select(adminRole=SYS만, 지자체담당자는 숨김+자기 지자체 고정). 표는 지자체 × 6카테고리
  (수산물/농축산물/지역상품권/생활용품/가공식품/관광서비스) 건수·금액 + 총계. **엑셀·페이지네이션
  없음**(AS-IS도 없다). GET은 조회 안 함. 숫자는 c:out 그대로라 천단위 콤마 없음. No.는 오름차순.
- AS-IS `ORDER_STATUS='40'`→`CONFIRMED`, 카테고리는 `gift.Gift.categoryCode`(AS-IS 6종과 1:1).
  카테고리 없는/지자체 null/gift 카탈로그에 없는 품목은 AS-IS 내부조인처럼 제외. 정렬은 지자체명.
- 구현: order `OrderLocgovStatsService.locgovItemQuantityStats`(일자 범위·지자체×품목) +
  `GET /api/admin/orders/stats/category-item`, admin `ShopStatisticsCategoriesService/Controller`
  신규, 템플릿 재작성(`th:remove="tag"` 적용).
- 검증: 2026-09-01~10-31 → 4행(강남구 농축산 2/160000, 제주 도청 상품권 2/60000+생활용품
  5/25000, 부여군 농축산 1/28000, 해운대 수산물 1/35000) - DB 직접 집계와 일치. 지자체 필터
  정상. 콘솔/HTTP 오류 0. ★지자체담당자 화면은 메뉴권한표상 ROLE_ADMIN_5만 허용인데 테스트계정
  pwtest_loc는 ROLE_ADMIN_6이라 접근거부(AS-IS 권한표대로) - **ROLE_ADMIN_5 계정이 없어
  LOC 분기(select 숨김·자기지자체 고정)는 미검증**.

## 답례품 선호도(shop-statistics/wish/list) 이식 (2026-10-14)

AS-IS `wishList` GET/POST + `wish/list.jsp` + `getOpWishlistList` + `ShopWishMoisExcelView`/`ShopWishLocgovExcelView`.
이전 구현(TierB "N.")은 상품ID·찜건수 한 표짜리 축소판이라 교체. order 연동 없이 admin이 gift(찜·품목·판매자)+
donation(지자체)를 조합(`ShopStatisticsWishService/Controller` 신규).

- 사양: 찜 행을 품목별로 COUNT → "등록수" 내림차순. 지자체 필터(locgovCode 우선, 없으면 upperLocgovCode)·답례품명
  부분일치·페이징(10/20/50/100, 메모리). mois(ROLE_ADMIN_1~4)는 지자체 2단 select + "지자체" 열, 지자체담당자는
  자기 지자체명(상위명+시군구명을 **공백 없이** 붙임) 고정 + 지자체 열 없음. GET은 조회 안 함. OP_ITEM·OP_SELLER·
  G_LOCGOV 내부조인이라 셋 중 하나라도 없는 품목은 제외(TO-BE는 품목 자신의 locgovCode 사용).
- ★엑셀 둘이 **서로 다르다**(AS-IS 그대로): mois = No. 오름차순·6열(지자체 포함)·시트 제목행 문구 "답례품 구매현황"
  (복붙 흔적)·파일명 "답례품 선호도_…" / locgov = No. 내림차순·5열·제목 "답례품 선호도"·파일명 "답례품선호도_…".
  금액은 숫자셀(#,##0). 둘 다 사유모달 없음. [[admin-excel-no-column-direction-mismatch]] 재확인.
- "상품코드(ITEM_CODE)" 없음 → 답례품명 링크는 AS-IS 프론트 `/items/details.html?code=` 대신 storefront
  `http://localhost:5173/gifts/{itemId}` 새창(설문 미리보기와 같은 방식).
- 검증: 찜 9품목(강진 머그컵 2건 선두 등) DB와 일치, 이름/지자체 필터·선택값 유지·페이지수 변경 정상, 두 엑셀 POI
  복호화로 방향·열·제목 확인, 콘솔/HTTP 오류 0, 스크린샷 정상. ★지자체담당자(ROLE_ADMIN_5/6) 분기는 테스트
  계정 한계로 미검증(이 메뉴 권한을 가진 5번 계정 없음).
