# AS-IS 충실도 감사 (대민 SPA 전환분)

- 작성: 2026-09-17
- 목적: 이미 storefront Vue3 SPA로 전환된 **대민 화면**을 AS-IS 소스와 대조해, 휴면해제·영수증출력에서 드러난 것과 같은 **흐름·문구·에러처리 divergence를 선제 발굴·목록화**한다. 쓰다가 발견하는 반복을 줄이는 게 목적.
- 방법: 화면 단위로 (1) AS-IS 프론트 흐름(`ghlove-frontend/**`, 특히 API 허브 `modules/op.saleson.js`) + (2) AS-IS 서버(`ghlove-web`/`ghlove-api`/`ghlove-common`) + (3) MSA 구현(백엔드 + storefront)을 대조. 라우트·파일명이 아니라 **동작·문구·분기**로 확인([[verify-screen-by-content-not-route]]). `old/`·주석처리 코드는 비활성이므로 라이브 경로만.
- 판정: **버그 / 재현누락(미배선) / 의도적 축소(외부연계·상용SW 대체 등) / 확인필요** 로 구분. 이 프로젝트엔 의도적 축소가 많으므로 함부로 "누락"으로 몰지 않는다.
- 상태: **진행 중(v1).** AUTH(로그인) 우선 완료. 나머지 영역은 하단 커버리지 체크리스트.

---

## 수정 반영 (2026-09-17, 컴파일·빌드 통과 · 런타임 검증 대기 → [[pending-verify-dormancy-receipt]])

- **A1 수정**: 로그인에 비번만료/임시비번 배선. `MemberService.passwordChangeCode()`/`changePasswordOnLogin()`/`postponePasswordChangeAndLogin()` 추가, `loginWithMfaCheck`가 휴면 다음에 이를 검사해 `PASSWORD_EXPIRED`/`PASSWORD_TEMP` 신호. `AuthApiController`에 `/api/auth/change-password`·`/api/auth/delay-change-password`. `LoginView`: PASSWORD_TEMP→"임시 비밀번호 사용자 입니다."+find-idpw, PASSWORD_EXPIRED→변경 모달("변경하고 로그인"/"나중에 변경").
- **D1 수정**: 누락 5건 완료 알림 복원(AS-IS 문구 그대로) — 문의등록·장바구니삭제(OrderDetailView·CartView·GiftDetailView).
- **D2 수정**: 반품/교환 신청 전 확인 프롬프트 + 유형별 완료문구.
- **E 수정**: 답례품 상세 재입고 알림·상품평 좋아요 배선. 백엔드 `requestRestockNotice`를 JSON화, `ReviewService.likedBy()`+DTO에 `likeCount`/`likedByMe`/`restockRequested` 추가, `GiftDetailView`에 버튼 2종.
- **A3 수정**: 세션만료 401 전역처리. `api/http.js`에 `setUnauthorizedHandler` 훅(3개 요청함수 모두 401 감지), `main.js`가 "로그인 후 이용이 가능합니다." 안내 + `/login?target=` 이동 + 로그인 스토어 초기화(중복 리다이렉트 가드). **프론트 전용이라 서비스 재기동 불필요**(storefront만).
- **A2 종결(해당없음)**: OBUJE_FAIL은 서버가 반환하지 않는 죽은 프론트 분기 → MSA 미재현이 충실(아래 A2 참조).
- **G 수정**: 체크아웃 결제 전 검증 추가 - `CheckoutView.submit`에 받는사람/연락처/우편번호/배송지주소/상세주소/포인트부족/동의 검증(AS-IS step1.html 문구). 프론트 전용.
- 저위험 tail 감사 종료: 포인트(H)·특정사업(I) 충실, 체크아웃(G) 수정. **대민 전 영역 감사 완료.**

## 요약 (심각도순)

| # | 영역 | 판정 | 심각도 | 한줄 |
|---|---|---|---|---|
| A1 | 로그인 비번만료/임시비번 | 재현누락(미배선) | **높음** | `passwordChangeRequired` 등 로직은 포팅했으나 로그인이 호출 안 함 → 만료·임시비번 강제변경이 아예 안 걸림 |
| A2 | 로그인 OBUJE_FAIL(생년월일 추가입력) | 해당없음 | - | 서버가 반환 안 하는 죽은 프론트 분기 → MSA 미재현이 충실 |
| A3 | 세션만료 401 전역처리 | 수정완료 | 중 | http.js 훅+main.js에서 401시 안내+`/login?target=` 이동(프론트 전용) |
| D1 | 액션 완료 알림 | 재현누락 | 중 | 대다수 충실, **특정 5건**(문의등록·장바구니삭제·주문취소/구매확정/클레임)만 완료문구 누락 |
| E | 답례품 상세 미배선 | 재현누락 | 중 | 재입고 알림·상품평 좋아요 = 백엔드 있는데 SPA 버튼 없음 |
| G | 체크아웃 필드검증 | 수정완료 | 중 | 결제 전 받는사람/주소/우편번호/상세주소/포인트부족/동의 검증 추가(프론트 전용) |
| A0 | 로그인 휴면(SLEEP_USER) | 수정완료 | - | 2026-09-17 AS-IS와 동일화([[pending-verify-dormancy-receipt]]) |

---

## A. 인증(로그인) — `LoginView.vue` / `AuthApiController` / `MemberService`

AS-IS 로그인 응답 핸들러(`op.saleson.js:1157~1224`)는 `/api/auth/token` 응답의 `code`로 분기한다. 코드 전량:

| AS-IS code | AS-IS 동작(op.saleson.js) | MSA 처리 | 판정 |
|---|---|---|---|
| (정상) | 로그인 완료 | `status:"OK"` | ✅ |
| `SLEEP_USER` | :1169 confirm→`/api/auth/recovery` | 2026-09-17 동일 구현 | ✅ (검증대기) |
| `PASSWORD_EXPIRED` | :1182 비번변경 모달 / 로그인 리다이렉트 | **없음** | ❌ A1 |
| `PASSWORD_TEMP` | :1201 `alert("임시 비밀번호 사용자 입니다.")`→find-idpw | **없음** | ❌ A1 |
| `OBUJE_FAIL` | :1163 생년월일 입력창(#temp-birthday) 노출, 토큰제거 | 없음(불필요) | ✅ A2(죽은 분기) |
| `ONEPASS_USER` | 주석처리(비활성) | 없음 | ✅ (AS-IS도 비활성) |

### A1. 비밀번호 만료/임시비밀번호가 로그인에 미배선 — 재현누락(높음)

- **AS-IS**: 만료(`PASSWORD_EXPIRED`)면 비밀번호 변경 모달을 띄우고("나중에 변경" 가능), 임시비번(`PASSWORD_TEMP`, passwordType 'T')이면 `alert("임시 비밀번호 사용자 입니다.")` 후 비밀번호찾기로 보내 **반드시 변경**시킨다.
- **MSA 현황**: 관련 로직은 이미 있다 — `User.passwordExpiredDate`/`passwordType('N'/'T'/'P')`, `MemberService.passwordChangeRequired()`([MemberService.java:953](../member/src/main/java/com/ghlove/member/service/MemberService.java#L953)), `postponePasswordChange()`(:973, "나중에 변경"), 만료일 계산(:924). **그런데 `passwordChangeRequired`/`postponePasswordChange`의 호출부가 0곳** — 로그인(`loginWithMfaCheck`/`AuthApiController.login`)이 이걸 전혀 부르지 않아 만료·임시비번 회원이 그냥 `OK`로 로그인된다. 임시비번('T') 회원은 담당자 방문접수(`walkInRegister`, :210)로 실제 생성되므로 도달 가능한 경로다.
- **필요 조치**: 로그인 성공 지점에서 `passwordChangeRequired(user)` 검사 → 응답 코드 추가(`PASSWORD_EXPIRED`/`PASSWORD_TEMP`) → SPA가 비밀번호 변경 안내(만료는 "나중에 변경" 허용=`postponePasswordChange` 노출, 임시는 강제). find-idpw의 직접 재설정+자동로그인은 의도적 재아키텍처라 그대로 두되, 임시비번 표출만 로그인에 붙이면 된다.

### A2. OBUJE_FAIL(생년월일 추가입력) — 해당없음(죽은 프론트 분기, 재현 불필요)

- **AS-IS 프론트**: 로그인 응답이 `OBUJE_FAIL`이면 `vm.birthday`를 채우고 생년월일 입력 UI(`#temp-birthday`)를 노출한 뒤 토큰을 제거한다(op.saleson.js:1163).
- **트리거 확인 결과(2026-09-17)**: AS-IS **서버는 이 코드를 반환하지 않는다.** 로그인 토큰 발급부(`ghlove-api/saleson/api/auth/AuthController.java`)가 설정하는 `code`는 `PASSWORD_EXPIRED`(:1233)·`PASSWORD_TEMP`(:1236)·`SLEEP_USER`(:1239) 셋뿐이고, 저장소 전체 grep에서 `OBUJE`/`OBUJE_FAIL`은 **op.saleson.js(프론트)에만** 존재한다. 즉 주석처리된 `ONEPASS_USER`처럼 **더 이상 트리거되지 않는 레거시 프론트 핸들러**다.
- **판정**: MSA가 재현하지 않는 게 **라이브 AS-IS 동작과 일치(충실)**. 조치 불필요.
- **부수 확인**: 서버 실사용 코드 3종이 A1·휴면에서 MSA에 심은 코드(PASSWORD_EXPIRED/PASSWORD_TEMP/SLEEP_USER)와 정확히 일치함을 교차검증.

### A3. 에러 표출·401 처리 방식(전역) — 확인필요(중)

- **AS-IS**(`op.saleson.js:4454 handleApiExeption`): API 에러 시 (a) `redirect` 필드 있으면 이동, (b) **401이면 `alert("로그인 후 이용이 가능합니다.")` + `/login?target=` 자동이동**, (c) 그 외 서버 메시지를 **모달 `$s.alert`** 로 표시.
- **MSA**(`api/http.js`): 실패를 `throw new Error(message)` → 각 뷰가 `catch`해 **화면 내 텍스트**(errorMessage)로 표시. 401 전역 자동이동은 라우터 가드(requiresAuth)로 커버되나, **이미 로그인한 뒤 세션 만료로 API가 401을 주는 경우**의 일괄 처리는 없다(뷰마다 제각각).
- **판정**: 모달 vs 인라인은 UX 선택차라 반드시 틀린 건 아니나, **세션만료(401) 일괄 처리 부재**는 실사용 중 로그인이 풀렸을 때 화면이 조용히 깨질 수 있어 점검 필요.
- **증거(임시분기 산재)**: 401 전역처리가 없어 뷰마다 제각각이다 — 예: `DonateView.verifyResidence`는 `if (e.message.includes('로그인')) router.push('/login…')`로 문자열 매칭 우회, `OfficialReceiptPrintView`는 alert+close, 대부분 뷰는 그냥 인라인 텍스트. requiresAuth 라우터 가드는 **진입 시점**만 커버하고 **이미 열린 화면에서 API가 401을 주는 경우**는 못 잡는다.
- **권고(수정안, 미적용)**: `api/http.js`의 공용 `request()`에서 `res.status === 401`을 잡아 (a) 로그인 스토어 초기화 + (b) `/login?target=<현재경로>`로 이동(가능하면 "로그인 후 이용이 가능합니다." 안내). 라우터/스토어를 http 모듈에 직접 물리기 어려우면 커스텀 이벤트나 콜백 주입으로 App 레벨에서 처리. AS-IS `handleApiExeption`의 401 분기와 1:1 대응.

---

## B. 아이디/비밀번호 찾기 — `FindIdPwView.vue` / `AuthController`(find-idpw AJAX)

AS-IS `users/find-idpw.html` + `op.saleson.js`(`findId`/`findPasswordStep1`/`findPasswordStep2`) 대조. **대체로 충실 — 숨은 결함 없음.**

- **비번찾기 = 새 비밀번호 직접 입력(임시비번 발급 아님).** AS-IS step2 폼이 "새 비밀번호 입력/확인"(find-idpw.html:218,225)으로 사용자가 직접 정한다. MSA도 동일(직접 재설정). → **A1의 "find-idpw 직접재설정=재아키텍처"라던 초기 서술은 오류. 사실 AS-IS와 동일하다.** (임시비번 'T'는 find-idpw가 아니라 담당자 방문접수/관리자 발급 경로에서만 생긴다.)
- **비밀번호 복잡도 규칙 일치.** AS-IS(find-idpw.html:630~660 `checkPwd`): 9~20자·3개이상 반복/연속문자 금지·loginId 미포함. MSA `validatePasswordComplexity`([MemberService.java:557](../member/src/main/java/com/ghlove/member/service/MemberService.java#L557))가 동일 규칙을 **서버측에서 재검증**(AS-IS는 클라이언트만) → 오히려 더 견고.
- **본인인증은 전자서명(magicline)/휴대폰 = 의도적 축소.** AS-IS는 `/api/magicline/signedFormRGhlove` 등 외부 인증망으로 본인확인(find-idpw.html:802). MSA는 이 외부연계가 없어 dev bypass로 대체([[member-service-deferred-items]]). → 재현누락 아님, 알려진 보류.
- ⚠ **확인필요(경미):** 비번 재설정 성공 후 이동 — MSA `resetPwSubmit`은 자동로그인 후 `/mypage`. AS-IS가 자동로그인인지 로그인화면 이동인지는 step2 최종 콜백에서 재확인 필요(현재 MSA 주석은 "AS-IS처럼 자동로그인"이라 주장).

## C. 회원가입 — `SignupView.vue` / `AuthApiController`(signup)

이미 인벤토리 전수조사에서 대조·판정된 영역이라([[as-is-inventory-procedure]], [[continue-vs-rebuild-msa-decision]]) 여기서는 재도출하지 않고 요지만 옮긴다.

- **구현 분기는 AS-IS 일치**로 확인됨. 단 인벤토리가 재현누락으로 찍은 **① 본인인증(`isAuth`) ② 법정대리인(만14세 미만, `OP_USER_PARENT`)** 은 실제로 미구현 = 인벤토리가 정직한 갭. 둘 다 외부연계·정책 게이트라 보류군.
- 추가 정밀대조가 필요하면 이 항목을 별도 라운드로 승격(약관 필수/선택 동의 매핑, 아이디 중복확인 문구, 주소검색 팝업 동작 등).

## D. 마이페이지 주문 상세/클레임 — `OrderDetailView.vue`

AS-IS `mypage/orderDetail.html` + 클레임 모달(`components/ui/modal-return.vue`·`modal-exchange.vue`·`modal-order_cancle.vue`) 대조.

### D1. 액션 "완료" 알림 누락 — 재현누락(중, 전역 가능성)

- **AS-IS**: 주요 액션 성공 시 모달 `$s.alert("...되었습니다.")`로 완료를 알린다 — 구매확정 "구매확정이 완료되었습니다."(orderDetail.html:976), 취소 "취소신청 되었습니다."(modal-order_cancle.vue:360), 반품 "반품신청 되었습니다."(modal-return.vue:301), 교환 "교환신청 되었습니다."(modal-exchange.vue:286).
- **MSA**: `OrderDetailView`의 `submitClaim`/`cancelOrder`/`confirmReceipt`는 성공 후 **조용히 `load()`만** 하고 완료 문구를 안 띄운다. 사용자가 처리 여부를 확신하기 어렵다.
- **확산 실측 결과: 전역 아님, 특정 5건.** storefront 전 화면을 훑으니 대다수는 AS-IS 완료문구를 (일부는 AS-IS 파일·라인 주석까지 달아) **충실히 보존**했다 — 예: 답례품Q&A/후기 삭제 "삭제되었습니다."(GiftQnaView:36·GiftReviewsView:35), 관심지자체 등록(ListSelectView:90), 포인트 사용(MyPointsView:24), 기탁서 접수(OfflineDonationView:118), 장바구니 담기(GiftDetailView:60). **누락된 것만 추리면 다음 5건:**

  | 액션 | 빠진 AS-IS 문구 | 위치 |
  |---|---|---|
  | 답례품 문의 등록 | "답례품Q&A가 등록되었습니다." (items/details-main.html:2612) | `GiftDetailView.submitInquiry` |
  | 장바구니 선택삭제 | "해당 답례품이 장바구니에서 삭제되었습니다." (cart/index.html:552) | `CartView.deleteSelected` |
  | 주문 취소 | "취소신청 되었습니다." (modal-order_cancle.vue:360) | `OrderDetailView.cancelOrder` |
  | 구매확정 | "구매확정이 완료되었습니다." (orderDetail.html:976) | `OrderDetailView.confirmReceipt` |
  | 반품/교환 신청 | "반품신청/교환신청 되었습니다." (modal-return/exchange.vue) | `OrderDetailView.submitClaim` |

  나머지(후기작성=AS-IS도 완료alert 없음, 위시토글=아이콘만)는 **갭 아님**. → 권장: 이 5곳에만 완료 알림 추가(문구는 위 AS-IS 그대로).

### D2. 클레임 신청에 확인 프롬프트 없음 — 경미

- AS-IS는 반품/교환/취소를 **유형별 모달**로 확인받는다. MSA는 `cancelOrder`/`confirmReceipt`엔 `confirm()`이 있으나 **반품/교환(`submitClaim`)엔 확인 없이 바로 신청**된다. 최소한 신청 전 confirm + 완료 알림(D1)을 붙이는 게 일관적.

> 주문 목록(`MyOrdersView`)의 상태별 건수 요약·기간/상태/지자체 필터는 AS-IS `orderList.html`(my_order_status)와 구조 일치 — 별도 결함 없음(코드 대조).

## E. 답례품 상세 — SPA 미배선 기능 — 재현누락(중) — `GiftDetailView.vue`

백엔드에 엔드포인트는 있는데 SPA가 호출하지 않아 화면에 없는 기능들. **휴면해제와 같은 "능력 있음 + 흐름 미배선" 패턴.**

- **재입고 알림**: AS-IS는 품절 답례품 상세에 "재입고 알림" 버튼(ItemController:879, `POST /api/item/restock`). MSA 백엔드에 `GiftController.requestRestockNotice`(`POST /gifts/{itemId}/restock-notice`)가 있으나 `GiftDetailView`는 `soldOut` 표시만 하고 신청 버튼/호출이 없다([GiftDetailView.vue:245](../storefront/src/views/gift/GiftDetailView.vue#L245) 경고문구만). → 품절 시 재입고 알림 불가.
- **상품평 좋아요**: AS-IS는 리뷰에 좋아요(비로그인 IP 허용, ItemController:899, `add-like`). MSA 백엔드에 `GiftController.likeReview`(`POST /reviews/{id}/like`)가 있으나 SPA는 신고(report)만 호출하고 좋아요는 없다.
- **조치**: 두 버튼을 GiftDetailView에 배선(재입고=품절 조건부, 좋아요=리뷰행). 백엔드는 이미 준비돼 있어 프론트 추가만으로 복원 가능.

## F. 기부하기 — `DonateView.vue` — 대체로 충실

AS-IS `donation/donation-main.html` 대조. 제출 검증(지자체 미선택→거주지 미확인→고용관계(checkA)→기부자확인(checkB)→최소 100원→100원 단위)·법정 동의문구·적립예상·특정사업/응원메시지 흐름이 AS-IS와 일치. **새 결함 없음.**

- AS-IS에만 있는 검증들은 전부 **알려진 의도적 축소**(외부연계 미개방)로 확인:
  - "본인인증 미완료상태입니다…"(donation-main.html:995) → MSA는 본인인증(isAuth) 미구현([[member-service-deferred-items]], 인벤토리 갭과 일치).
  - "주민번호를 입력해주세요/뒷자리 오류/주민등록번호 오류"(1110·1117·1244) → MSA는 등록주소 기반이라 주민번호 실검증 없음. **단, juminNo2 입력칸(비밀번호형)은 화면에 있으나 `verifyResidence`가 locgovCode만 보내 값이 안 쓰인다** — 행정정보 연계가 붙기 전까지의 의도적 dead input(코드 주석에 명시). 정보성으로만 남김.
  - 외국인 거소·체류만료(1170~1182) → 외국인 기부 흐름 미구현(축소).

## G. 체크아웃(결제) — `CheckoutView.vue` — 재현누락(중)

AS-IS `order/step1.html` 대조. 서버 quote 재사용(preview=complete 동일계산)·쿠폰 숨김([[coupon-feature-unused-hide-ui]], 문서화)·동의 필수는 충실. 그러나 **결제 전 필드 검증이 AS-IS보다 얕다.**

- **AS-IS**(step1.html)는 결제 직전 각 항목을 alert로 막는다: "주문자 이름이 없습니다."(:1627)·"주문자 주소를 입력해주세요."(:1637)·"주문자 우편번호를 입력해주세요."(:1647/1657)·"주문자 상세주소를 입력해주세요."(:1667)·지자체별 "…포인트가 부족합니다."(:1425)·"구매에 동의해주시기 바랍니다."(:1578).
- **MSA**(`submit`)는 **`form.agree`만 검사**한다. 받으시는분·주소·우편번호 입력칸에 `required`가 붙어 있으나, 결제 버튼이 `<button type="button" @click="submit">`(폼 submit 아님)이라 **HTML5 required가 발동하지 않는다** → 빈 값으로도 결제 시도가 나간다. 포인트 부족도 `insufficient()`는 있으나 `submit`에서 안 쓴다(장바구니→체크아웃 진입 시 `CartView.goCheckout`에서만 검사).
- **심각도**: 서버(`OrderService.checkout`)가 배송지 필수면 서버 에러(errorMessage)로 드러나 조용히 깨지진 않으나, AS-IS의 구체적 필드 안내 대비 UX 저하 + 배송지 없이 주문 생성 가능성(서버 허용 시)이 있어 **확인·보강 권장**.
- **조치**: `submit`에 받는사람/주소/우편번호/상세주소/포인트부족 검증을 AS-IS 문구로 추가(작은 수정).

## H. 마이페이지 포인트 — `MyPointsView.vue` — 충실

AS-IS `mypage/cntrPoint.html` 대조. 연도·지자체별 집계·기부처 표시가 AS-IS와 일치. "포인트 사용" 폼은 AS-IS엔 없는 개발용 수동차감 기능(백엔드 `manual-point-use` 스위치로 gate, 기본 off)이라 정상. 완료문구("포인트가 사용되었습니다.") 있음. **새 결함 없음.**

## I. 특정사업 목록/상세 — `DesignatedList/DetailView.vue` — 충실(지도 위젯만 범위밖)

AS-IS `designated-donation/index-main.html`·`details.html` 대조. 상태/정렬(최근·참여금액·모금율·종료임박)/사업구분 필터·페이지네이션·모금현황·응원메시지·기간표기 재현. **"지자체별 검색" 지도 팝업**(`fragments/dsg-search-header.html`)만 미포팅 = gift 답례품몰·order 장바구니가 각자 헤더의 같은 지도 위젯을 남긴 것과 **동일한 의도적 경계**(locgovCode 쿼리·키워드검색 q는 지원). [[donation-service-deferred-items]]의 지도선택 보류와 일치 → 재현누락 아님.

## 커버리지 체크리스트 (다음 감사 대상)

- [x] 인증 - 로그인 응답 코드 분기 (A1~A3)
- [x] 인증 - 아이디/비번찾기(FindIdPwView): **대체로 충실**(B), 본인인증만 의도적 축소
- [x] 인증 - 회원가입(SignupView): 인벤토리 기존판정 요지 반영(C), 정밀대조는 필요시 승격
- [x] 마이페이지 - 주문조회/상세(MyOrders/OrderDetail): D1(완료알림 누락, 전역 가능성)·D2(클레임 확인)
- [x] 답례품 - 목록/상세(GiftList/Detail): D1 확인(문의등록 완료문구 누락 1건), 후기=충실. 재입고 알림은 SPA 미배선(별도 갭, gift 감사 참조)
- [x] 장바구니(Cart): D1 확인(선택삭제 완료문구 누락 1건), 담기·검증문구 충실. Checkout 쿠폰·동의는 미대조(남김)
- [x] 기부(Donate): **충실**(F), 갭은 전부 알려진 축소(본인인증·주민번호·외국인)
- [x] 전역 - 401 처리(A3): 증거·권고안 정리(미적용). 성공알림(D1)은 수정 완료
- [x] 마이페이지 - 포인트(MyPoints): **충실**(H)
- [x] 장바구니→주문(Checkout): **G 발견**(결제 전 필드검증 얕음, 중)
- [x] 기부 특정사업(DesignatedList/Detail): **충실**(I), 지도 위젯만 의도적 범위밖
- [ ] (필요시) 회원가입 정밀대조 승격(C) — 나머지 대민 대상은 모두 감사 완료
- [ ] (선택) G 보강, A3와 함께 세션·검증 UX 마감

> 각 항목은 AS-IS `op.saleson.js`의 해당 API 호출 + 화면 html + 서버 컨트롤러를 함께 대조해 채운다.
