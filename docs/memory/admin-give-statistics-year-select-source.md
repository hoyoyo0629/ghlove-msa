---
name: admin-give-statistics-year-select-source
description: 통계 화면의 년도 select는 YYYY 공통코드다 - 데이터에 있는 연도만 뽑으면 선택지가 한 개로 줄어 다른 해를 못 본다
metadata:
  type: project
---

AS-IS 통계 화면들(`give/statistics/all`, `give/statistics/locgov`)의 **년도 select 는
`CodeUtils.getCodeList("YYYY")` 공통코드**다. JSP가 `${yyyy}` 를 그대로 돌린다.

**TO-BE는 `giveStateService.availableYears()`**(기부 데이터에 실제로 있는 연도)를 쓰고 있었다.
개발DB에 2026년치만 있어서 **선택지가 "2026" 하나뿐**이었고, 다른 해를 볼 방법이 없었다.
화면이 멀쩡해 보여 눈치채기 어려운 종류의 갭이다(2026-10-13 교정).

- 코드표: `admin.op_common_code` 의 `YYYY` 타입 **10행(2022~2031)**, 그중 `use_yn='Y'` 는
  **2023~2026** 네 개다. AS-IS `getCodeList` 도 사용중인 것만 주므로 결과가 같다.
  (2027 이후는 N - 해가 바뀌면 공통코드 관리에서 켜는 운영 방식으로 보인다.)
- `GiveStatisticsController.yearCodes()` 로 뽑아 **전체(all)·지자체별(locgov) 두 화면이 공유**한다.
  코드가 비어 있으면 예전 방식(데이터 기준)으로 물러선다 - 화면이 통째로 못 쓰게 되는 것보다 낫다.
- 기본 선택은 AS-IS와 같이 **올해**(파라미터가 비면 `LocalDate.now().getYear()`).

**Why:** "데이터에 있는 연도만 보여주면 되지 않나"가 그럴듯해 보이지만, 그러면 **데이터가
없는 해는 조회 자체가 불가능**하다(0건인지 확인할 방법이 없다). AS-IS가 코드표를 쓰는 이유다.

**How to apply:** 통계·현황 화면에 연도 select 를 만들 때는 `YYYY` 공통코드를 쓴다.
다른 화면에도 같은 패턴이 남아 있는지 `availableYears()` 호출부를 확인할 것.

## ★차트 축은 서비스에서 빈 구간을 0으로 채워야 한다 (같은 날 2차 지적)

사용자가 **"시간대별 기부금액이 AS-IS는 0원이라도 그래프가 그려진다"** 고 지적.

AS-IS 쿼리는 `GROUP BY CNTR_HOUR` 라서 **기부가 있는 시간만** 나오고, 0건인 날은 **0행**이다.
그런데 AS-IS `GiveStatisticsServiceImpl` 이 쿼리 결과 위에 **루프를 돌며 빈 구간을 0으로 메운다**:
- `statisticsGiveAllTotalByHour` → `for (i=0; i<=23)` **24행 보장**
- `statisticsGiveAllTotalByMonth` → `for (i=1; i<=12)` **12행 보장**
- (`...ByAge` 도 10~70대 7행 보장 - 그 차트는 AS-IS가 주석처리 상태)

TO-BE `donation GiveStatService` 에는 이 보정이 없어서
- 시간대별: 데이터 없는 날 → **0행 → 축도 선도 없는 빈 그래프**(y축이 -1.0~1.0으로 뜬다)
- 월별: **데이터 있는 달만** x축에 찍혔다(실측 `1,2,3,5,6,8,9` - 4·7·10·11·12월 누락)

→ AS-IS와 같은 자리(서비스 계층)에 채우기를 넣었다. 실측으로 월별 12칸
`4,4,2,0,1,3,0,1,16,0,0,0`, 시간대별 24칸 전부 0(평평한 선이 그려짐) 확인.

**How to apply:** 차트용 집계 API를 만들 때 **GROUP BY 결과를 그대로 내보내지 말 것.**
축이 고정된 차트(월 12·시 24·나이대 7 등)는 서비스에서 빈 구간을 0으로 채워 내보낸다.
안 그러면 "데이터가 없는 날은 그래프가 아예 안 보이는" 증상이 된다.

**참고 - 전체(all) 화면 자체는 이미 AS-IS 이식이 끝나 있었다**: 요약 3카드 +
Chart.js 차트 5종(월별 기부건수 line / 월별 기부건수 bar(온·오프라인 누적) / 월별 기부인원 /
월별 기부금액 / 시간대별 기부금액 + 최근 7일 select), `op.chart.js`·`chart.min.js` 자산,
꼬리 `<style> .hourGraph canvas`, AS-IS가 주석처리한 나이대별 차트 2개 제외까지 그대로다.
**차트 제목이 1·2번 모두 "월별 기부건수"인 것도 AS-IS 그대로**(오타처럼 보이지만 원본이 그렇다).
실측: 카드 22,482,000 / 9 / 31, 캔버스 5개 렌더, 콘솔·HTTP 오류 0.
