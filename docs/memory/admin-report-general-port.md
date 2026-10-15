---
name: admin-report-general-port
description: 통계>보고서>총괄 현황(shop-statistics/report/general) AS-IS 이식 - 사전집계 통계표 14종을 AS-IS 이름 그대로 신설·적재, 탭4개 화면+14시트 엑셀. 통계 배치는 TO-BE에 없음
metadata:
  type: project
---

2026-10-15 이식. 이전 구현(연도 입력 하나·탭 없음·엑셀 없음, 회원/기부/답례품 원장 즉석집계)은 AS-IS에 없는 발명이라 통째로 교체
(`ReportStatisticsController.general`·`ReportStatisticsService`의 총괄 전용 메서드 삭제. 총괄누계/지자체별/연간 화면은 아직 옛 구현 그대로).

- ★AS-IS는 즉석 집계가 아니라 **통계 배치가 미리 적재한 표를 SELECT만** 한다. 화면은 MBR_TNOCS_STATS·DNTN_TNOCS_STATS·
  DNTN_TNOCS_MAX_AMT_STATS·GDS_TNOCS_STATS 4종, 엑셀은 14종(+MCTPV_*·LCLGV_*·DNTN_PATH/AMT/AGE_TNOCS·HAB_DNTN_MCTPV).
  14종 전부 admin 스키마에 AS-IS 이름·컬럼 그대로 만들고(database/ddl/service-admin.sql) AS-IS CUBRID 2,066행을 적재
  (database/ddl/migration-admin-report-stats-asis-load.sql, 개발DB 적용 완료). 다른 보고서 화면(총괄누계·지자체별·연간)도 같은 표를 쓰니 이식할 때 재사용.
- ★이 표들을 채우는 AS-IS 배치(`setStatisticsReport`, 연령배치 birthdayBatch)는 TO-BE에 없다 - 2026-06 이후 데이터는 안 늘어난다
  ([[missing-batch-jobs-port-together]]). AS-IS 화면의 연령배치·통계배치·연통계 버튼은 주석처리라 안 켰다.
- 화면: 탭 4개(회원 수/기부건수/기부건수(2000만원)/답례품건수)를 한 페이지에 다 그리고 JS가 전환. **GET은 표 비움, POST에서 채움**.
  ★GET 직후 년도 select가 빈칸인 건 AS-IS JS 그대로(탭 클릭 때 monthChange(undefined)가 select를 비움) - 버그 아님. 탭을 누른 뒤 년도를 골라야 한다.
  월 select는 display:none(직전월까지만 조회). 검색 onclick `search()`는 AS-IS도 미정의 함수라 옮기지 않음.
- ★"엑셀" 버튼은 `report/lclgv/excel-download`(지자체별 화면과 같은 주소)를 부른다. 암호화 없음(AbstractSXSSFExcelView 계열 =
  AsisSxssfExcelView). 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체로 고정+시트 3장, 그 외 14장. 전년대비 칸은 화면(op:numberFormat, 천단위·"%")과
  엑셀(`%.2f`+"%" 문자열, 콤마 없음)이 서로 다르게 찍힌다 - AS-IS 그대로.
- 구현: `ShopStatisticsReportGeneralService`(native Tuple→Map, 월 컬럼 접미사 jan~dec)·`ShopStatisticsReportGeneralController`·
  `web/support/ShopStatisticsReportExcelView`·`templates/shop-statistics/report-general.html`(JS는 data 속성으로 값 전달).
- 검증: Playwright로 GET/탭4/검색/초기화/엑셀, 2025 서울 130.66%·회원 27,539 등 DB와 일치, 엑셀 14시트 POI로 확인,
  지자체담당자 3시트 확인(pwtest_loc 11230은 통계표에 없는 지자체라 0행, 메뉴 권한은 직접 URL로 우회해 확인).
- admin 재기동 필요(+DB에 DDL·적재 반영).

## 총괄 누계 현황(report/general-total) 이식 - 2026-10-15
같은 14종 통계표를 재사용(DNTN_PATH/AMT/AGE_TNOCS_STATS·HAB_DNTN_MCTPV_TNOCS_STATS). 이전 원장 즉석집계 4표 구현 교체
(`ReportStatisticsController.generalTotal`·`ReportStatisticsService` 누계 전용 메서드 삭제). 구현: `ShopStatisticsReportGeneralTotalController`,
`ShopStatisticsReportGeneralService.generalTotal()`, `report-general-total.html`. 메뉴 URL은 TO-BE가 kebab(`general-total`, AS-IS는 generalTotal) - 그대로 둠.
- 총괄 현황과 같은 틀(탭4·GET 빈 표·POST 채움·"엑셀"=lclgv/excel-download 14시트)이되 **월 select가 화면에 보이고**, 끝에 monthChange 호출이 없다.
- ★AS-IS JS 그대로의 비대칭: 3번 탭(연령별)은 단위 캡션을 "기부방법별" div로 켠다(연령별 캡션 div는 어떤 탭에서도 안 켜짐, 4번 탭은 캡션 없음).
- ★거주지역->기부지역 표만 값에 콤마 없음(c:out). 금액별/연령별은 JSP의 코드별 c:if 사슬이라 정의 코드만 칸이 나온다('00'은 라벨+값 2칸).
- ★개발 데이터에 연령 코드 '50'이 40대·50대 둘 다 들어 있어(AS-IS 데이터 결함) 정렬이 모호 -> `order by dntn_age_cd, dntn_age`로 고정(화면·엑셀 모두).
- 데이터가 2026-05까지라 2026/6~9를 검색하면 빈 표(그리고 년도 select가 빈칸으로 돌아감 - crtrYr undefined, AS-IS 동작).
- admin 재기동 필요.

## 지자체별 누계 현황(report/mctpv) 이식 - 2026-10-15
MCTPV_DNTN_STATS·MCTPV_DNTN_MAX_AMT_STATS·MCTPV_GDS_STATS 3표 재사용. 이전 구현(연도 입력·원장 즉석집계) 교체
(`ReportStatisticsController.mctpv` 삭제 - 같은 컨트롤러의 `lclgv`와 `report-locgov.html`은 아직 옛 구현, 지자체 현황 이식 때 교체).
구현: `ShopStatisticsReportMctpvController`, `ShopStatisticsReportGeneralService.mctpvRows()`, `report-mctpv.html`.
- 총괄 누계와 같은 틀(탭 3개, 기본 탭 3번, 월 select 보임, GET 빈 표, 엑셀=lclgv/excel-download). crtrYr은 세 탭 모두 "기부금 누계 표 마지막 행 기준년".
- 값은 원→백만원 반올림(`(int)Math.round(원/1e6)`, StatisticsReport.getNowYrMJan). 세 표 모두 캡션 "(단위: 백만원, %)"(답례품 표도).
- ★2000만원 탭(MAX_AMT)만 값 0과 전년대비 '0'을 "-"로 찍는다(JSP에 `== 0` 분기가 24+12곳). 다른 두 탭은 0 그대로. 월 구간 정합은 awk로 전수 확인.
- admin 재기동 필요.

## 지자체 현황(report/lclgv) 이식 - 2026-10-15
LCLGV_AOD_STATS·LCLGV_AOD_MAX_AMT_STATS·LCLGV_GDS_STATS 3표 재사용. 옛 구현(연도 입력·원장 즉석집계)과 공유 템플릿 `report-locgov.html` 삭제,
`ReportStatisticsController`는 이제 연간통계(`year`)만 남음(다음 이식 대상). 구현: `ShopStatisticsReportLclgvController`(GET/POST + `options-by-lclgv` JSON),
`ShopStatisticsReportGeneralService.lclgvRows/mctpvOptions/lclgvOptions/upperLocgovCode`, `report-lclgv.html`.
- 지자체별 누계와 같은 틀 + **시/도 → 시/군/구 2단 select**(시/도 선택 시 `POST /shop-statistics/report/lclgv/options-by-lclgv`, 3번 탭은 답례품 표·그 외 기부 표 기준). 월 select는 숨김.
- 표는 높이 700px 스크롤 박스에 시/도·지자체 두 열 sticky. 단위 천원(`(int)Math.round(원/1000)`). 기부금 두 탭만 0→"-"(값·전년대비), 답례품 탭은 0 그대로.
- ★탭 JS의 "마지막 행" 출처가 탭마다 다르다(1·3번=기부금 표, 2번=답례품 표). 1·2번 탭은 AS-IS 오타(shLclgv가 비면 #shMctpv를 비움, 아니면 #shLclgv에 시/도 코드를 넣음)가 있지만 바로 뒤 searchSigungu가 복구해 결과 동일 - 그대로 옮김.
- ★지자체 담당자(ROLE_ADMIN_5/6)는 select 대신 소속 이름 텍스트, 조회·엑셀 범위가 소속으로 고정(광역이면 시/도만, 기초면 시/도+지자체). 엑셀은 이제 화면에서 고른 시/도·지자체 조건을 시트 5~10(+기부/답례품 지자체 시트)에 그대로 적용(`ReportParam.shMctpv/shLclgv`).
- ★**코드 불일치 주의**: 통계표(AS-IS 개발DB 적재)의 지자체 코드는 옛 체계(인천 중구 28110, 동구 28140 …)인데 TO-BE donation 지자체 목록은 개편 코드(인천 28125·28155 …)라 겹치는 게 거의 없다(순천시 46150 정도). 그래서 지자체 담당자 계정의 locgov_code가 통계표에 없으면 빈 표가 나온다(TO-BE 통계 배치가 생기면 TO-BE 코드로 쌓여 해소). 검증은 pwtest_loc의 locgov_code를 46150으로 잠시 바꿔 했고 11230으로 되돌림.
- admin 재기동 필요.

## 연간통계 현황(report/year) 이식 - 2026-10-15 (보고서 5화면 전부 완료)
옛 구현(최근 5년 연도별 건수·금액 표)과 `ReportStatisticsController/Service`·`report-year.html` 전부 삭제(보고서 스위트의 옛 구현 파일 제로).
구현: `ShopStatisticsReportYearController`(GET 화면 + `lclgv/excel-pastYearStatsStored`), `ShopStatisticsReportYearService`(CUBRID→PG 변환 쿼리),
`web/support/ShopStatisticsStoredPastYearExcelView`, `report-year.html`.
- ★화면에는 **표시되는 데이터가 없다**: 년도(YYYY 공통코드 중 올해 이전·2022/2023 제외)+시/도+시/군/구 select와 "엑셀"·"초기화"뿐이다. 엑셀이 그 지자체의 연간 집계 시트 8장(총괄·기부방법별·기부금액별·기부연령별·주소지 광역/전체·월별·답례품 인기순).
- 표 8종 신설·적재(AS-IS 이름 그대로): YEAR_SUM/PATH/PRICE/AGE/PSINT/MONTH/GOODS_AMT_TNOCS + STAT_LOCGOV(통계용 지자체 243건). 1,507행, `migration-admin-report-year-stats-asis-load.sql`. ★AS-IS 개발DB에서도 연간표는 표마다 **정확히 200행**(일부 지자체만)만 들어 있어, 지자체에 따라 시트가 비어 나온다(예: 2025 순천시는 총괄·방법별·연령별만 값, 주소지·월별·답례품은 빈 시트). 이 표를 채우는 연통계 배치(statisticsYearBatch)는 TO-BE에 없음.
- 엑셀 버튼: 시/도·시/군/구가 비면 alert(상위 지자체를 선택해 주세요 / 지자체을 선택해 주세요) 후 중단. 시/도를 고른 뒤 시/군/구 select는 AS-IS가 tabId를 안 넘겨 기부(AOD) 통계표 기준 목록이 채워진다. 지자체 담당자는 select 없이 소속 이름만 보이고 서버가 소속으로 덮어쓴다(광역이면 시/도=지자체=광역 코드).
- CUBRID→PG 변환: decode→CASE, nvl→coalesce, 문자열 컬럼 SUM→`sum(cast(.. as numeric))`, 정렬 불명 쿼리는 `order by ctid`(등록일시 순 쿼리는 `frst_reg_dt, ctid`)로 적재 순서 고정. 주소지 광역 비율은 `trunc(cast(.. as numeric(5,1)),1)`.
- 지자체 칸 이름 규칙은 `ShopStatisticsReportGeneralService.scope()`로 공유(지자체 현황·연간통계). `search()`처럼 AS-IS 미정의 함수는 이 화면에 없다(monthChange는 #shMonth가 없어 AS-IS도 no-op이라 verbatim 유지, 연통계집계-적재 버튼은 주석처리라 안 켬).
- admin 재기동 필요 + DDL·적재 SQL 적용 필요(개발DB는 적용 완료).

## ★정정(2026-10-15 사용자 지적) - 첫 진입 기본값 4화면(총괄·총괄누계·지자체별누계·지자체)
위 본문의 "기본 탭은 4번/3번"·"GET 직후 년도 select가 빈칸인 건 AS-IS JS 그대로"는 **틀린 서술**이다. 원인 두 가지:
1. AS-IS `StatisticsParam.getTabId()`는 **비어 있으면 "1"을 돌려준다**(필드는 null이지만 getter 기본값). 컨트롤러·JSP 모두 getter를 쓰므로 첫 진입 탭은 **항상 1번**(회원 수 / 기부방법별 / 지자체별 기부금 누계 / 지자체별 기부금). 필드(null)만 보고 "else → 마지막 탭"으로 읽었다. → `ReportParam.getTabId()`에 같은 기본값. ★JSP/JS가 쓰는 값은 필드가 아니라 getter 반환값인지 항상 확인할 것(StatisticsParam처럼 getter에 기본값 로직이 있는 DTO 주의).
2. 첫 진입에는 조회된 행이 없어 탭 JS의 `crtrYr`(마지막 행의 기준년)이 undefined → `monthChange(undefined, …)`가 년도 select를 비우고 월을 12로 만든다. AS-IS 컨트롤러가 직전월(shYear/shMonth)을 계산해 모델에 담는 의도와 맞지 않는 결과라, 행이 없으면 서버가 정한 년도(`data-search-year`)로 대체하게 했다(템플릿 4곳의 crtrYr 헬퍼). 결과: 첫 진입 = 1번 탭 + 직전월(오늘 2026-10 → 2026년 9월), 데이터 없는 달을 검색해도 년도가 유지됨. 숨김 월 select도 9라 엑셀이 12개월 시트로 나오던 것도 해소.
