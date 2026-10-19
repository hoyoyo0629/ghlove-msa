---
name: report-statistics-analysis-db-pending-da
description: 통계>보고서 6223~6227 - AS-IS 분석DB+월간배치 구조. TO-BE는 10-15에 통계표 22종을 admin 스키마에 만들고 AS-IS 데이터 적재해 화면 이식 완료, 채우는 배치는 미이식(데이터 고정). 표 위치·배치 원천은 DA 설계안 이후(2026-10-19 사용자). PL 공유 문서 있음
metadata:
  type: project
---

AS-IS에 "ReadModel" 이름은 없고, 별도 분석DB(ghlove_analysis, `DatabaseAnalysis.java`·`analysis-mapper.xml`)에
statisticsReportBatch(사용)·statisticsYearReport* 4(중지)가 월별/연간 통계를 쌓고 통계>보고서 5화면(6223~6227)이 그것만 읽는다.
★정정(10-19, pull 후 확인): TO-BE 5화면은 2026-10-15 [[admin-report-general-port]]에서 AS-IS 방식으로 재이식됨 - 통계표 22종(화면·엑셀 14 + 연간 8)을 admin 스키마에 AS-IS 이름 그대로 두고 AS-IS 데이터 적재. 조회모델 실시간 계산은 옛 구현(폐기). 표를 채우는 statisticsReportBatch 등은 미이식이라 데이터가 늘지 않음, 통계표 지자체코드는 옛 체계.

**Why:** 사용자 "현재는 분석DB가 없으니까 DA설계안이 나온 뒤에 어떻게 할지 결정하는게 맞다"(2026-10-19). PL 공유용 분석 문서:
https://claude.ai/code/artifact/29405d4b-1ca9-4ebd-b117-73ac9b47a10a
**How to apply:** DA 설계안 전에는 통계 배치 이식·통계표 위치 변경에 손대지 말 것(화면은 이미 AS-IS 방식). 공유 문서도 10-19 정정 반영함.
관련 [[defer-readmodels-pending-da-design]], [[batch-job-port-decisions]](원장 남은 사항 6번).
