---
name: admin-interceptor-asis-exempt-paths
description: AS-IS OpmanagerHandlerInterceptor.isManagerPage 예외(40개) 경로는 TO-BE WebConfig excludePathPatterns에도 넣어야 한다 - 안 넣으면 메뉴권한 없는 롤이 공통 조회를 부를 때 권한없음 HTML이 와서 화면이 깨진다(15102 시군구 undefined)
metadata:
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T10:40:59.507Z
---

AS-IS는 관리자 로그인·메뉴권한 검사를 Spring Security가 아니라 `OpmanagerHandlerInterceptor`가 하고(SecurityConfig에
/opmanager/** 인증 규칙 없음), `isManagerPage()`의 예외 경로는 **로그인·메뉴권한 둘 다 안 본다**. TO-BE `AdminAuthInterceptor`는
경로를 메뉴(가장 긴 접두 menu_url)로 판정해 권한 없으면 /admin/access-denied로 302 → ajax가 HTML 문자열을 받는다.

**Why:** 2026-10-13 오프라인 담당자(ROLE_ADMIN_7)로 15102 테스트 시 `/give-state/options-by-locgovCode`가 기부금전체현황(14103)
메뉴로 판정돼 302 → 화면 JS가 HTML을 한 글자씩 돌아 시군구가 전부 "undefined" → 그 값으로 부과요청이 나가 "지방세외 시스템 연결에
실패하였습니다."(사용자는 우회 실패로 오인). 같은 날 `/offgive/sci-result`, `/give-state/options-by-locgovCode` 예외 추가로 해결.

**2026-10-14 1라운드 이식**: /healthcheck("Server is alive."), /error/404, /offgive/pdbtest(예외 추가, pCrypto 없음 → ERROR칸),
/file/**(시스템관리자 검사 - 사용자 결정으로 downloadFile·sendMail에도 검사 추가, AS-IS는 무검사), /fileDownload/downloadByProgramData
(PRJ_NOTICE, 파일명 토큰은 donation SealCipher로 pCrypto 대체 + storefront 공지 첨부). 대상 아님: present/login·transfer(AS-IS 컨트롤러
없음), log/exceldownload-reason(AS-IS 자체가 500). **2라운드(미착수)**: 로그인 서브시스템 "화면·흐름만 AS-IS"(사용자 결정, BCrypt·세션은
TO-BE 유지) - login 화면/메시지/임시비번/PKI·금융인증/잠금/비번변경, manager-request 폼 계열, accessdenied, popup/temp, emailModifyInfo.

**How to apply:** 화면 이식 때 그 화면이 부르는 공통 조회가 AS-IS 예외 목록에 있으면 TO-BE excludePathPatterns에도 넣는다.
AS-IS 예외 중 TO-BE에 아직 대응 경로가 없는 것(이식 시 같이 처리): designated-donation/partList, welfareCenter/wlfrCntrMngList,
user/popup/emailModifyInfo/, log/exceldownload-reason(→TO-BE /common/opmanager/privacy-access-log, 메뉴 미매칭이라 현재 통과).
나머지는 로그인·PKI·매직라인·파일서버(/file/*) 경로라 TO-BE 로그인 흐름이 따로 있다. 점검법: 화면 URL을 op_menu
(status_code='1') 최장 접두로 판정해 그 롤의 op_menu_right가 있는지 본다(메뉴 미매칭은 TO-BE에선 통과).
