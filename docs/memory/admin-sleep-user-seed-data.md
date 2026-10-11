---
name: admin-sleep-user-seed-data
description: "휴면회원관리(admin 4107, /admin/sleep-users) 확인용 가짜 시드 25건 적재(2026-10-08) - member.op_user STATUS_CODE='DORMANT'"
metadata:
  node_type: memory
  type: project
---

`database/ddl/seed-member-sleep-users.sql`(적용 완료) - `member.op_user`에 `dormant01`~
`dormant25` 25건을 STATUS_CODE='DORMANT'로 추가(기존 계정 변경 없음), `op_user_detail`에
주소(전체 시/도 공식명 - "서울특별시"/"경기도" 등, [[storefront-address-widget-mismatch-deferred]]
조사에서 축약형 문제를 인지한 뒤 시드는 전부 정식명으로 작성)까지 같이 넣었다.

최종방문일(login_date)은 **오늘이 아니라 3~13개월 전으로 분산**했다 - AS-IS 자체 설계가
"휴면회원은 정의상 로그인이 오래됐으므로 검색 기본값(오늘)으로는 보통 비어 있고, 운영자가
직접 기간을 넓혀 조회하는 UX"([[admin-member-area-port-progress]] 4107 항목)라서다. 화면에서
그냥 검색하면 0건이고, 가입일 범위의 **"전체" 또는 "1년" 버튼**을 눌러야 25건이 다 보인다
(testuser01 가입일 검색 때와 같은 패턴 - 버그 아님).

[[admin-member-date-range-api-format-bug]]를 먼저 고친 뒤에 이 시드를 넣었다 - 그 버그가
남아있었으면 날짜범위를 넓혀도 어차피 member API가 500→빈 결과였을 것이다.

기부누적액·포인트잔액 컬럼용 donation/point 데이터는 넣지 않았다(화면은 값 없으면 '-'로
표시하므로 공백 상태로도 화면 확인은 가능) - 필요하면 추가 요청.
