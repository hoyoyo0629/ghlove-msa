# B5 — 지정기부 월별통계(특정사업 월별통계) AS-IS 완전 재현 스펙

출처: AS-IS `designated-donation/analysis/month.jsp` + `DesignatedDonationManagerController`(/opmanager/designated-donation/analysis/month/{campaign,amountraised,amount}) + `designated-donation-mapper.xml`(selectDesignatedLocgovMonth{Campaign,AmountRaised,Amount}, 각 1231/1322/1441행). TO-BE 재현용.

## 화면 구성(month.jsp)
- 필터: 시도(shWdr, 본사 1~4만 노출)/지자체(shLocgovCode, 시도 선택 시 cascading)/조회년도(selYear)/상태(prjStatus: 0전체·2진행·9종료 라디오).
- 차트 3개(Chart.js, AS-IS와 동일 — `chart.min.js`+`op.chart.js` admin에 복사됨):
  1. **사업구분별 사업 진행건 비율** `designatedLocgovMonthCampaignChart` (type:pie, labels 취약계층/문화·예술/자원봉사/복리증진, 색 `rgb(255,99,132)`/`rgb(54,162,235)`/`rgb(255,205,86)`/`rgb(32,169,59)`, data=비율%). 아래 표: 1행 비율(%), 2행 건수(건). columnTpDesc.
  2. **모금액 비율** `designatedLocgovMonthAmountRaisedChart` (type:pie, 같은 색/labels, data=모금액비율%). 아래 표 3행: 비율(%)/모금액(원)/목표모금액(원). (TP1=모금액비율, TP2=모금액, TP3=목표모금액)
  3. **월별 모금액 추이** `designatedLocgovMonthAmountChart` (`ChartCommon.drawChartMultiYaxis('...','bar',{scales:{x:{stacked:false},y:{stacked:false}}}, monthArr, amountArr, {x:'월',y:'원'}, "FIXED")). amountArr=[목표금액(원) #00215A, 모금액(원) #09C2C7]. 아래 표 4행(목표금액/모금액/참여자수/사업건수)×(1~12월+합계).
- 조회: 페이지 로드 시 fnSearch()=3개 ajax 동시 호출. jQuery 사용($.post, $('#..').text, Common.numberFormat) → **AS-IS jquery 복사 필요**.

## 사업구분 코드(DSGN_DNTN_BIZ_SE_CD)
100 취약계층 · 200 문화/예술 · 300 자원봉사 · 400 복리증진.

## 백엔드 집계 3종 (CUBRID→Postgres 포팅)
공통: 대상 사업 = G_DSGN_DNTN_BIZ_MNG(P), 년도(selYear) 기간겹침 필터
`(BGNG_YMD BETWEEN yr0101..yr1231) OR (END_YMD BETWEEN ..) OR (yr0101 BETWEEN BGNG..END) OR (yr1231 BETWEEN BGNG..END)`,
시도/시군구 필터(shLocgovCode=LCLGV_CD 직접 / shWdr=상위코드로 G_LOCGOV에서 하위 LOCGOV_CODE IN), 부서(dsgncntrPartId>0 → DEPT_ID).
모금액 조인 = G_CNTR 집계 LEFT JOIN: `SUM(CAST(CNTR_AMT AS BIGINT)) ... GROUP BY DSGN_DNTN_BIZ_ID` where `CNTR_DE<=오늘(YYYYMMDD) AND DELETE_AT='N' AND STTEMNT_PAY_DE IS NOT NULL AND CNTR_STTUS_CODE='200' AND DSGN_DNTN_BIZ_ID>0`.
상태파생(PRJ_STATUS): STTS_CD='2'(진행)인데 (모금≥목표 OR END_YMD<오늘) 이면 '9'(종료)로 간주. 그 외 STTS_CD 그대로. prjStatus 필터는 이 파생값 기준(0이면 전체).

1. **campaign**(selectDesignatedLocgovMonthCampaign): 사업구분별 사업 '건수'와 '비율'. 2행(TP1 비율=FLOOR(cnt*10000/total/100), TP2 건수). 컬럼 prjBsns100~400, prjBsnsTot, columnTpDesc. 사업 1건=1 카운트(GROUP BY BIZ_ID 후 BSNS_TYPE별 집계).
2. **amountraised**(selectDesignatedLocgovMonthAmountRaised): 사업구분별 모금액/목표금액/비율. 3행(TP1 비율=FLOOR(amt*10000/sumamt/100), TP2 모금액, TP3 목표모금액). 목표금액은 END_YMD<=yr1231인 사업만 합산(MAX per biz).
3. **amount**(selectDesignatedLocgovMonthAmount): 월별 m01~m12 + total, 4행(TP1 목표금액/TP2 모금액/TP3 참여자수(CNTR_CNT)/TP4 사업건수). 목표금액 월 = PRJ_ED_DT(종료월)에 귀속(END_YMD<=yr1231 & 월=MM), 모금액/참여자 월 = CNTR_DE의 MM. 사업건수 월 = 사업 종료월 기준.
   ※ CUBRID SUBSTR(x,5,2)=MMDD의 월 두자리. DATE_FORMAT(NOW(),'%Y%m%D')는 오늘(주의: %D는 버그성이나 그대로 동작범위 내).

## DTO(DesignatedStat 재현 필드)
`tp, columnTpDesc, prjBsns100, prjBsns200, prjBsns300, prjBsns400, prjBsnsTot`(campaign/amountraised),
`columnTpDesc, m01..m12, total`(amount). JSON 키 camelCase.

## TO-BE 구현 계획
- donation: `DesignatedStatController`(or 기존 DesignatedAdminApiController 확장) 3 GET 엔드포인트 `/api/designated-projects/admin/analysis/month/{campaign,amountraised,amount}?shWdr&shLocgovCode&selYear&prjStatus`. 네이티브 집계(위 SQL Postgres 포팅: NVL→COALESCE, CAST AS BIGINT, to_char(now(),'YYYYMMDD'), substr). 지자체 스코프는 admin에서 넘김(지자체 담당자=자기 locgov 강제).
- admin: DesignatedProjectClient 3 메서드 + 컨트롤러가 month 페이지 라우트(`/designated-projects/analysis/month`?) 또는 기존 analysis 교체. 템플릿 = AS-IS month.jsp verbatim(필터+canvas3+표3), JS는 campaign/amountraised/amount 함수 그대로(엔드포인트만 admin 경로), jQuery+chart.min.js+op.chart.js 로드.
- 자산: `chart.min.js`,`op.chart.js` 복사완료. **jquery.min.js는 AS-IS(`ghlove-web/static/content/...`)에서 복사** 필요. `Common.numberFormat` 등 유틸도 AS-IS에서 가져오거나 동등 구현.
- 메뉴: 17202(월별통계)→ 이 페이지로 재배선(현재 B1에서 /designated-projects/analysis로 임시배선). 17201(지자체별통계)은 analysis/locgov 별도(AS-IS analysis/locgov.jsp — 차트 유무 추후 확인).

## 상태
**구현 완료(2026-10-01) · 재기동 사용자.**
- donation: `DesignatedStatRepository`(네이티브 3종, 라이브 DB 검증 통과) + `DesignatedStatService`(BsnsStatRow/MonthStatRow) + `DesignatedAdminApiController`에 `/api/designated-projects/admin/analysis/month/{campaign,amountraised,amount}` GET 3종. 컴파일 OK.
- admin: `DesignatedProjectClient`에 monthCampaign/monthAmountRaised/monthAmount + records(BsnsStat/MonthAmount); `DesignatedProjectAdminController`에 `/designated-projects/analysis/month` 페이지 + 데이터 3종(GET+POST, {isSuccess,data} 봉투, 지자체담당자 자기locgov 강제); 템플릿 `designated/analysis-month.html`(month.jsp verbatim: 필터+canvas3+표3, JS 그대로, 집계 URL만 admin 경로·시도→시군구는 로컬필터). 컴파일 OK.
- 에셋: jquery-1.11.0 / op.common.js(Common.numberFormat·isUndefined) / chart.min.js / op.chart.js(ChartCommon.drawChart·drawChartMultiYaxis) 로드 — 전부 AS-IS 복사본. `$.log`은 AS-IS에서도 미정의(월별통계 JSP 전용·정상경로 미호출)라 그대로 둠=parity.
- DB: 메뉴 17202(월별통계)→`/designated-projects/analysis/month` 재배선 적용(migration-admin-menu-B5-designated-month.sql).

### AS-IS→TO-BE 스키마 치환(값 사전만, 로직 동일)
- 확정기부: AS-IS `CNTR_STTUS_CODE='200' AND STTEMNT_PAY_DE IS NOT NULL` → TO-BE `CNTR_STTUS_CODE='COMPLETED'`(TO-BE는 정산일자 미채움=전행 NULL, 기존 analysis도 COMPLETED만으로 확정판정).
- 사업상태: '1/2/9' ↔ PENDING/OPEN/CLOSED. 쿼리 내부에서 AS-IS 코드로 환산 후 상태파생(진행→모금≥목표 또는 종료일경과 시 종료)·prjStatus(0/2/9) 필터 동일.
- 조인키: G_CNTR.PRJ_ID(死컬럼, 전부 0) 대신 DSGN_DNTN_BIZ_ID.

### 검증(2026 전체 기준, 라이브 DB)
campaign 91건(비율 25/24/24/26=100), amountraised 모금액합 16,662,000(시드 5건 일치·FLOOR 비율 99%), amount 월귀속(5월 6,000,000/6월 10,652,000/9월 10,000·참여자 5·목표금액 종료월 귀속) 전부 AS-IS 로직대로.

### 남은 통계화면(B5 잔여, 순서 재결정 대상)
17201 지자체별통계(analysis/locgov 전용화면 분리), give-statistics all/locgov/operate, shop-statistics report/dashboard/sales, 관심지자체 등.
