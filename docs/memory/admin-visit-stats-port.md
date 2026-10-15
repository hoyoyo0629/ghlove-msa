---
name: admin-visit-stats-port
description: 통계>접속통계(stats/visit/index) 신규 이식 - OP_VISIT_COUNT 1,096행 적재, 메뉴 URL을 AS-IS 값으로 교정, 접속 수집 쪽은 아직 없음
metadata:
  type: project
---

2026-10-14 이식. 화면이 아예 없었다(메뉴는 있는데 라우트 없음). AS-IS `StatsManagerController#visit/visitPost/visitDay` +
`stats/visit/index.jsp`/`day.jsp` + `stats-mapper.xml`(getVisitSummary·getMonthStatsList·getDayStatsList).

- 사양: 요약 6칸(전체/오늘/어제/월별최대/일별최대/일별최소) · 검색기간(시작년~종료년, 2013~올해) · 월별 가로막대(월 이름
  클릭 → `/stats/visit/day` ajax로 그 달 일별 막대만 교체) · 이번 달 일별 세로막대(기록 없는 날 0으로 채움, 퍼센트는 소수
  2자리). **GET은 막대를 비우고 POST에서 채운다**(AS-IS 빈 목록 하드코딩). 구현: `VisitStatsService`(native 집계)·
  `VisitStatsController`·`templates/stats/visit.html`·`visit-day.html`(조각).
- ★메뉴 URL 데이터 오류 교정: TO-BE `op_menu` 6101이 `/stats`였는데 AS-IS는 `/opmanager/stats/visit/index`다 - 접두어
  매칭 기준이 달라 라우트와 안 맞았다. 개발DB에서 `/stats/visit/index`로 UPDATE(다른 환경은 같은 교정 필요).
- ★데이터 적재: TO-BE `admin.op_visit_count`가 0건이라 AS-IS CUBRID 1,096행(2023-01-01~2026-06-09, 합계 10,285,133)을
  그대로 적재. 접속 상세 `OP_VISIT`(1,028만건·IP 포함)는 이 화면이 안 써서 적재 안 함.
- ★**접속을 이 표에 쌓는 쪽이 TO-BE에 없다**(AS-IS `saveVisitData` 인터셉터+쿠키 IS_VISITED) - 오늘/어제는 계속 0이고
  2026-06-09 이후 데이터는 안 늘어난다. storefront 접속 수집은 별도 과제(결정 필요).
- AS-IS 월별 막대에 `</c:choose>ß` 오타(화면에 "ß" 한 글자가 찍힘)가 있어 옮기지 않았다. 엑셀(`visit/download-excel`)은
  ROLE_EXCEL(판매자 전용) 버튼이라 운영자에겐 안 보여 만들지 않았다. `/stats/referer`(방문자접속경로, 6102)는 TO-BE 메뉴에
  없어 별도(미요청).
- `line_chart.png` 404는 AS-IS 저장소에도 없는 이미지라 AS-IS와 같다.
- 검증: 요약값·2025~2026 월별 18행(비율 합 100)·6월 일별(6/9=3건=42.86%) 확인, 콘솔 오류는 위 404뿐, 레이아웃 정상.
