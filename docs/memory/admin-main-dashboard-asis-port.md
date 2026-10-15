---
name: admin-main-dashboard-asis-port
description: 관리자 메인(/admin) AS-IS main/index.jsp 재이식(2026-10-15) - 발명 위젯 제거, 권한별 위젯·5개 서비스 집계 API·콜 등록 팝업·로그인 직후 오프라인 담당자 안내. REPORT_DASHBOARD는 배치 미이식이라 0
metadata:
  type: project
---

2026-10-15 사용자 지시 "관리자 메인화면(http://localhost:8086/admin) 이식". 이전 TO-BE 화면(오늘 주문 건수·오늘 신규 회원·
최근 공지사항)은 **AS-IS에 없는 발명**이었다(docs/as-is-parity.md 3(b)의 위젯 목록도 SalesOn 원제품 기준 오판 - 실제 화면과 다름).

**AS-IS 정본**: `OpmanagerController.index` + `main/index.jsp`(1,065줄) + `CommonController /common/opmanager/*` 12개 +
`MainController /opmanager/popup/insertCall` + 매퍼 `slave-main-mapper.xml`(main-info·boardInfo·차트·chartInfo·gift-search·
get-user·알림동의) / `main-mapper.xml`(main-search·call-search·gift-amount·콜등록 - 같은 id라도 두 파일 SQL이 다르니 서비스가
어느 매퍼를 부르는지 확인할 것).
- 레이아웃: `main == 'main'`이면 LNB·.contents 래퍼 없음 → `.mainArea`가 `#container` 바로 아래. 브레드크럼도 없다.
- **"메인화면 데이터 조회" 버튼을 눌러야 조회**(AS-IS가 서버 부하 때문에 바꾼 것). 하단 표는 기부일자 입력 후 검색.
- 화면 권한(JSP c:choose): 7·8·11 OFF(오프라인 접수현황만) / 5·6·10 LOC(기부·답례품 카드+차트) / 1~4 SYS(전부) / 그 외 아무것도 없음.
  쿼리 권한(getLoginUserAdminRoleCheck)은 별개: 5·6 LOC, 7·8 OFF, 11 WCM, **10 포함 나머지 SYS** - 10은 LOC 화면에 전체 데이터.
- AS-IS 라벨 엇갈림 그대로: "국민비서 동의 회원"에 sms_count, "SMS 동의 회원"에 pbanc_count.

**구현**: 서비스별 `AdminDashboardService`+`AdminDashboardApiController`(member `/api/admin/members/dashboard/*`, donation·point
`/api/admin/dashboard/*`, order `/api/admin/orders/stats/dashboard-present`, gift `/api/admin/gifts/dashboard/*`) →
admin `AdminMainClient`/`AdminMainService`/`AdminHomeController`(데이터 주소는 AS-IS `/common/opmanager/*` 그대로, 응답 `{isSuccess,data}`).
admin 소유 표(G_MNGR_REQST·OP_QNA·OP_CALL·REPORT_DASHBOARD)는 admin이 직접. 콜 등록 팝업 `/admin/popup/insertCall`.
로그인 성공(ID/PW·인증서) 시 세션 `loginNow` → 메인 첫 진입에 7·8에게 "{은행} {지점}지점 {이름}님으로 로그인…" alert.
설문조사·엑셀암호화 팝업은 POP_ADMIN period/period2 기간판정(현재 둘 다 만료라 안 뜸).

**코드 치환**: G_CNTR '200'→COMPLETED / OP_USER '9'→ACTIVE / 판매자 '2'(AS-IS 값 그대로, 10-15 전환) /
답례품 DATA_STATUS '1'→APPROVED / OP_ORDER_ITEM 65·75 제외 → 품목행(od_order_item, item_status≠CANCELLED) +
품목행 없는 단품주문(od_order, order_status≠CANCELLED). 판매자 지자체코드가 TO-BE에 없어 답례품의 locgov_code 사용.
수신동의는 AS-IS대로 '0'만 센다(10-15 member 저장값 통일로 이제 정확).

**남은 것**: ①"기부현황 복사" 영역(get-user)은 REPORT_DASHBOARD를 읽는데 그걸 채우는 AS-IS 배치(`confirmDonationForReportBatch`,
JobServiceImpl)가 미이식이라 늘 0 → [[missing-batch-jobs-port-together]](사용자: 메인 끝나면 누락 배치 전부 찾아 이식).
②`/opmanager/dashboard`(main/dashboard.jsp, index 옛 사본, 링크 0건)는 사용자 지시로 같은 날 `/admin/dashboard`로 이식 -
진입 즉시 조회·알림동의 없음·카드값이 백만원인데 "(원)" 라벨·mainGift 상대경로 404까지 AS-IS 그대로.
③판매자 상태코드는 같은 날 AS-IS 값(2·3·5)으로 전환 완료 → [[gift-seller-status-asis-codes]]. 메인 집계도 '2'.
④수신동의도 같은 날 AS-IS 0/1로 통일 → [[receive-sms-encoding-split]] - 메인 알림동의 수가 이제 실제 동의 회원을 센다.
관련 [[admin-login-asis-parity-2026-10-14]], [[service-logic-full-audit-planned]].
