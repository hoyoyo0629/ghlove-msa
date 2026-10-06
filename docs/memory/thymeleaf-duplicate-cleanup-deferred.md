---
name: thymeleaf-duplicate-cleanup-deferred
description: Thymeleaf 폐기 1차(대민 중복 삭제)는 5개 서비스 완료, 2차(살아있는 기능 Vue3 전환) 착수 - donation은 의존성 완전 제거(1/5), member reactivate 전환, 나머지 4개는 의존성 유지
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T01:55:10.892Z
---

**1차(대민 중복 Thymeleaf 폐기) 실행 완료 — 2026-09-17.** 이전(2026-09-09)까지 "admin 방향 결정 대기"로 보류였으나, 사용자가 "완전 제거" 공격적 방향을 지시해 실행했다. 판매자/제공자/운영관리는 PL 그룹 결정 대기라 제외. **admin 링크 선행조건은 결과적으로 불필요했다** — GNB는 storefront(AppHeader/AppFooter)가 SPA 라우트로 걸고, 각 서비스의 대민 뷰@Controller만 걷어내면 됐다.

**실행량(전부 `gradlew compileJava` exit 0 검증):**
- member: 템플릿 13 + 컨트롤러 4 삭제, AuthController 수술(find-idpw AJAX만 존치)
- donation: 템플릿 21 + 컨트롤러 8 삭제, InterestLocgovController 수술(add/remove AJAX만)
- point: 템플릿 3, PointController 수술(expire 배치만)
- gift: 템플릿 5(list·detail·wishlist·my-reviews·my-qna), GiftController 수술(판매자+토글/좋아요/재입고 AJAX만)
- order: 템플릿 10 + 컨트롤러 4 삭제, ClaimController 수술(운영자 큐만)

**SPA가 직접 부르는 비-/api AJAX는 반드시 존치:** donation `POST /interest-locgovs`·`/interest-locgovs/{code}/delete`, gift `POST /wishlist/{itemId}/toggle`. 나머지는 전부 `/api/*` 트윈이 있어 뷰 컨트롤러를 지워도 안전.

**매핑표 오판 2건을 내용 대조로 정정([[verify-screen-by-content-not-route]]):** ① gift/inquiries는 GiftQnaView(구매자)가 아니라 **판매자 문의답변**(currentSellerId) → 유지. ② gift/events·event-detail(지역이벤트)은 SPA `/events`(admin 운영이벤트)와 **다른 기능** → 유지-미커버.

**2차 착수(2026-09-17, 최소범위·완주 우선):**
- ✅ **donation — Thymeleaf 의존성 완전 제거 완료(첫 서비스).** 유일하게 남아있던 대민 라이브 `receipt-official-print`(공식영수증)를 storefront `OfficialReceiptPrintView.vue`(`/print/official-receipt/:cntrSn`) + `GET /api/my/receipts/official/{cntrSn}`로 이관. 직인 합성 PDF `/pdf`·`/print-log`는 Thymeleaf가 아니라 유지. `spring-boot-starter-thymeleaf` 제거, compileJava·vite build 통과, 뷰 렌더링 0건 확인.
- ✅ **member — 휴면해제를 AS-IS와 동일하게 재구현(의존성은 유지).** 처음엔 `/reactivate` 페이지+아이디/비번 재입력 폼+로그인 링크로 옮겼으나 **AS-IS와 달라 사용자 지적**받고 바로잡음([[check-as-is-source-when-analyzing]] 흐름추적 참고). AS-IS(op.saleson.js:1169)대로: 로그인 시 휴면회원이면 비밀번호 본인확인 후 서버가 `SLEEP_USER` 반환→LoginView가 그 자리에서 `confirm("휴면해제 하시겠습니까?")`→`POST /api/auth/recovery`(세션 대기 userId로, 자격증명 재입력 없음)→재로그인 유도. `MemberService.checkCredentials`는 휴면을 하드차단하지 않고 통과, `reactivateById`(구 reactivate 대체). DormancyController·reactivate.html·ReactivateView·/reactivate 라우트·링크 전부 제거. login·external-login×3이 남아 member `thymeleaf` 의존성은 유지.
- ⏸ 나머지는 대기(point/order 운영자·gift 판매자/지역이벤트·member 외부인증).

**아직 `spring-boot-starter-thymeleaf`를 뗀 서비스는 donation 1개뿐.** 나머지 4개는 살아있는 기능이 남아서다(2차 = Vue3 전환 대상):
- 대민 미커버(SPA 만들면 제거 가능): member reactivate([[member-service-deferred-items]] 휴면해제)·external-login×3(외부인증), gift events/event-detail(지역이벤트), donation receipt-official-print(공식영수증 서버PDF, [[receipt-commercial-sw-replacement]])
- 판매자/운영(PL 결정 대기, 범위밖): gift my/register/edit/seller-dashboard/inquiries([[provider-portal-split-deferred]]), order claims/queue, point expire-batch(운영자 소멸배치)

상세 매핑·정정 근거는 `docs/thymeleaf-decommission-map.md`. 관련 [[admin-frontend-is-thymeleaf-not-jsp]] [[scope-migration-not-greenfield]].

**[2026-09-22 실측·3게이트 정리]** 서비스별 템플릿 수: **admin 225 / gift 12 / member 8 / order 6 / point 5 / donation 0**. starter-thymeleaf 의존성 보유: admin·gift·member·order·point(donation만 제거됨). "전면 제거" 시점을 못 박는 유일한 열쇠는 **admin(운영관리웹) Vue 전환 결정**인데 [[admin-frontend-is-thymeleaf-not-jsp]] 미정 → 225장이 실질 병목. 3게이트: ①🟩 도메인 중복 잔재(member/order/point+gift 구매자분 ≈30장)=지금 정리 가능, ②🟨 gift 판매자 포털=[[provider-portal-split-deferred]] 제공자웹 신설 시 걷힘, ③🟥 admin 225=Vue전환 결정 종속. 사용자 지시(2026-09-22): 이 로드맵은 저장만·이후 재결정, 지금은 gift 옵션작업 집중. "언제 다 걷어내나"에 날짜를 붙이려면 admin Vue전환을 별도 결정지점으로 올려야 함.
