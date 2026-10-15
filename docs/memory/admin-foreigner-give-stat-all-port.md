---
name: admin-foreigner-give-stat-all-port
description: 통계>외국인 기부 현황>전체(6711) 이식 - 기부금 모금현황 전체 화면에 foreignerFlag 분기, 메뉴 URL 누락이 원인, AS-IS 검색(POST) 405 결함 해소
metadata:
  type: project
---

2026-10-15 이식("누락된 메뉴"). 6711 메뉴 행은 TO-BE에 있었지만 `menu_url`이 NULL이라 눌러도 아무 화면도 안 열렸다
(6706 일반 전체는 `/give-statistics/all`). 6712(지자체별)도 같은 상태이고 **아직 미이식**(URL 그대로 NULL, LNB에서도 안 보임).

- AS-IS는 별도 화면이 아니라 `give/statistics/all/detail.jsp`를 `searchParam.foreignerFlag == 'Y'`로 재사용한다. 다른 점: 카드 3개·월별 차트 4개 제목에
  "외국인 " 접두어, 시간대별 차트 블록은 숨김 빈 div로 대체, 페이지 제목은 JS가 "외국인 기부 현황 (전체)"로 덮어씀, ajax URL이 `foreigner/all/`,
  집계 쿼리에 `GC.FOREIGN_STATUS_CODE != '0'`. 구현: `GiveStatisticsController.foreignerAll/…`(+`allView` 공통화), `give-statistics-all.html`의
  `foreigner` 분기(JS는 폼의 hidden foreignerFlag를 읽음), donation `GiveStatRepository/Service/GiveStatAdminApiController`에 `foreigner` 인자,
  `GiveStatClient` 3메서드에 인자 추가. 메뉴는 `migration-admin-menu-6711-foreigner-all.sql`(개발DB 적용 완료).
- ★AS-IS 결함 둘(그대로 안 옮김): ① `foreigner/all`에는 **POST 매핑이 없어** 검색 버튼이 405 - 요약 카드는 POST(`total`)에서만 채워지므로 AS-IS 외국인 화면은 카드가 영영 안 채워진다. TO-BE는 일반 화면처럼 GET(년도 파라미터)에서 요약을 계산한다. ② 외국인 나이대 ajax(`foreigner/all/{year}/age`)는 locgovCode 자리에 년도를 넣는 오버로드 실수가 있는데 UI에서 호출하지 않는 죽은 코드(차트 주석처리).
  또 `init()`이 `#giveDate` 없이도 `statisticsAllByTime()`을 불러 `foreigner/all/undefined/hour`를 POST한다 - AS-IS 동작이라 그대로(빈 24행 응답, 그리지 않음).
- ★데이터: TO-BE `donation.g_cntr.foreign_status_code` 컬럼은 있고(엔티티 미매핑, 기본 '0') 개발DB 47건 전부 '0'이라 외국인 화면은 0으로 나온다.
  AS-IS 실데이터는 코드 '1'(1,761건)·'3'(1,175건)가 외국인이다. 값을 세팅하는 쪽(실명인증 결과 반영)은 TO-BE에 없다. 검증은 기부 3건을 잠시 '1'/'3'으로 바꿔
  (1,510,000원·3명·3건) 확인하고 전부 '0'으로 되돌림.
- 관련: [[admin-give-statistics-year-select-source]](같은 화면의 년도 select·0 채움 규칙 공유).
- admin+donation 재기동 필요.

## 외국인 기부 현황 > 지자체별(6712) + 지자체 상세 이식 - 2026-10-15
6712도 `menu_url` NULL("누락된 메뉴")이라 `migration-admin-menu-6712-foreigner-locgov.sql`로 `/give-statistics/foreigner/locgov` 설정(개발DB 적용). 권한은 AS-IS와 같이 ROLE_ADMIN_1~6.
- 일반 지자체별 목록(6707)과 같은 템플릿·핸들러를 `foreigner` 플래그로 공유(`GiveStatisticsController.locgovList(foreigner,…)`, `give-statistics-locgov.html`): 제목 "외국인 기부 현황 (지자체)"(AS-IS는 일반도 JS로 "기부금 모금현황"을 덮어써서 TO-BE 일반 화면 제목도 그에 맞춤),
  초기화/검색 URL, hidden foreignerFlag(엑셀 URL에도 실림 - `locgov/excel`이 `foreignerFlag`를 받음), 집계 `GC.FOREIGN_STATUS_CODE != '0'`(donation `locgovList`에 `foreigner` 인자).
- ★**지자체 상세 신설**(`give-statistics-locgov-detail.html`, `/give-statistics/[foreigner/]locgov/{code}` + `/{year}/month`): 지자체명 링크의 도착지로 AS-IS `locgov/detail.jsp`(요약 3카드+월별 4차트, 시간대별 없음, "목록" 버튼). 일반 지자체별에도 쓰인다 - TO-BE 일반 목록에서 "보류"였던 드릴다운이 이로써 해소.
  링크 규칙은 AS-IS 그대로: 일반=그 행의 년도, 외국인=선택된 년도(GET `?shCntrYear=`). AS-IS 상세는 GET에서 카드 미계산이지만 TO-BE는 계산(전체 화면과 같은 이유).
- ★지자체 담당자(ROLE_ADMIN_5/6): AS-IS처럼 **목록 없이 자기 지자체 상세로 redirect**(일반 목록도 같은 규칙으로 바뀜 - 예전 TO-BE는 목록을 자기 지자체로 필터해 보여줬다). 다른 지자체 코드로 접근하면 "등록된 지자체만 이용할 수 있습니다." alert 후 자기 상세, 소속이 없으면 "지자체 등록 후 이용할 수 있습니다." 후 홈. 상세에서는 "목록" 버튼이 숨는다.
- 검증: 개발데이터 외국인 4건을 잠시 '1'/'3'으로 바꿔(순천 2건 25,000·곡성·해운대) 목록 3행·정렬(금액)·링크→상세 카드 25,000/2/2·년도 검색·목록 버튼·엑셀(암호화, No 오름차순, 금액순) 확인, pwtest_loc의 locgov_code를 46150으로 잠시 바꿔 redirect·alert 확인 후 전부 원복.
- admin+donation 재기동 필요.
