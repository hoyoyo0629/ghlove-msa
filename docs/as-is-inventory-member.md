# AS-IS 인벤토리 — member 도메인

> 기준: [[as-is-logic-is-the-spec]] — AS-IS 서비스로직이 정본, RFP(`docs/requirements.md`)·ISP(`docs/isp-detailed-design-summary.md`)는 신규/변경 건에만 적용.
> 판정: `대응있음` / `재현누락` / `의도적축소` / **`죽은코드`**(AS-IS에서 이미 도달불가·미호출)
> 작성 2026-09-10

## 1. 화면 생사 판정 (users/, mypage/ 회원분)

살아있는 화면은 inbound 참조가 있는 것만 인정했다. `_bak`/`-backup`/`_YYYYMMDD`/`old/`는 라우팅상 도달 경로가 없다.

| AS-IS 화면 | 판정 | 근거 |
|---|---|---|
| `users/login.html` | live | 진입 참조 110 |
| `users/join.html` | live | 진입 참조 155 |
| `users/modify.html` | live | 진입 참조 40 |
| `users/secede.html` | live | 진입 참조 10 |
| `users/find-idpw.html` | live | 진입 참조 14 |
| `users/secede-kakao.html` | live | `modify.html:1429`, `secede.html:287,291` |
| `users/onepass_secede.html` | live | `modify.html:983` |
| `users/onepass-join.html` | live | `onepass-result.html:113,149` |
| `users/onepass-result.html` | live | 서버 리다이렉트 착지 (`AuthController:1663~1717`) |
| `users/mobile-auth-result.html` | live | 서버 리다이렉트 착지 (`AuthController` mobile-auth-callback) |
| `users/jusoPopup.html` | live | join/modify/onepass-join 팝업 |
| `users/sns/naver-callback.html` | live | `op.saleson.js:25 naverLoginCallback` |
| **`users/sign-certificate.html`** | **죽은코드(도달불가)** | 유일 진입점 `login.html:1271`이 존재하지 않는 `sign-certificate1.html`을 가리킴 — AS-IS 오타 결함 |
| `users/login-backup.html` | 죽은코드 | inbound 0 |
| `users/login_bak.html` | 죽은코드 | inbound 0 |
| `users/login_test.html` | 죽은코드 | inbound 0 |
| `users/login_20241115.html` | 죽은코드 | inbound 0 |
| `users/join_20241115.html` | 죽은코드 | inbound 0 |
| `users/modify-backup.html` | 죽은코드 | inbound 0 |
| `users/old/*.html` (8개) | 죽은코드 | 디렉터리 통째 미참조 |

### 1-1. 화면 상수가 가리키는 존재하지 않는 경로
`modules/op.saleson.js`의 `$s.pages`:
- `SLEEP_USER: "/users/sleep-user.html"` → **파일 없음**(`users/old/`에만 존재). 리다이렉트 호출부 `op.saleson.js:4504,4743` 전부 주석.
- `CHANGE_PASSWORD: "/users/change-password.html"` → **파일 없음**(동일). 호출부 `4751` 주석.

→ AS-IS에서 **휴면회원 전용 화면·비밀번호변경 전용 화면은 비활성**. MSA `DormancyController` 재현 시 이 사실을 근거로 삼아야 한다(RFP/ISP 신규 요건 여부 별도 확인 필요).

## 2. 컨트롤러 엔드포인트 인벤토리

### 2-1. `saleson.api.user.JoinController` — `/api/join`
| 엔드포인트 | 메서드 | AS-IS 호출처 | 판정 |
|---|---|---|---|
| POST `/entryForm` | `entryForm` | `join.html:1029,1045` | |
| POST `/getSubLocGov` | `getSubLocGov` | `join.html:1071` | |
| POST `/getUserInfoByUserId` | `getUserInfoByUserId` | `join.html:1122` | |
| POST `/join` | `join` | `join.html:796` | |
| POST `/onepassJoin` | `onepassJoin` | `onepass-join.html` | |
| POST `/getPolicyInfo` | `getPolicyInfo` | join/onepass-join | |
| POST `/checkMobileAuth` | `checkMobileAuth` | `join.html:1297` | |
| POST `/checkOnepassMobileAuth` | `checkOnepassMobileAuth` | `onepass-join.html` | |
| POST `/testRandomId` | `testRandomId` | **없음** | **죽은코드** |

### 2-2. `saleson.api.auth.AuthController` — `/api/auth`
회원기능 상당수가 `UserController`로 이관된 뒤 **잔존**했다. 아래 `호출처 없음`은 전부 `modules/op.saleson.js`에 래퍼만 남고 화면 호출부가 0인 것.

| 엔드포인트 | 래퍼(op.saleson.js) | AS-IS 호출처 | 판정 |
|---|---|---|---|
| POST `/token` | `getAuthToken` | login/join/modify/find-idpw, `static/js/financ.js` | live |
| POST `/sns-token` | `getAuthSnsToken` | `static/js/sns.js` | live |
| POST `/sns-join` | `snsJoin` | `static/js/sns.js` | live |
| POST `/disconnect-sns` | `disconnectSns` | `static/js/sns.js` | live |
| POST `/check-sns-join` | `checkSnsJoin` | `static/js/sns.js` | live |
| GET `/sns-info` | `getSnsInfo` | `order/step1.html`, `admin/order-agency/step1.html` | live |
| POST `/check-password` | `checkPassword` | secede / secede-kakao / onepass_secede | live |
| GET `/mobile-auth` | `mobileAuth` | join/modify/find-idpw/onepass-join, `auth.vue`, `mobile_auth.vue` | live |
| `/mobile-auth-callback` | (서버) | 본인인증 PG 콜백 | live |
| GET `/onepass-login` | `onepassLogin` | `login.html` | live |
| POST `/onepass-callback` | (서버) | 온패스 콜백 | live |
| POST `/onepass-token` | `onepassAuthToken` | `onepass-result.html` | live |
| POST `/onepass-cancel` | — | `login-backup.html`만 | **죽은코드** |
| POST `/onepass-unlink` | — | 확인필요 | |
| POST `/getAccessInfo` | — | `login-backup.html`만 | **죽은코드 후보** |
| POST `/getSimpleAuthResult` | `getSimpleAuthResult` | `login.html` | live |
| POST `/login-statistics` | — | **없음** | **죽은코드** |
| GET/POST `/me` | `getMember`/`updateMember` | **없음** (실제는 `/api/user/getUserInfo`,`/modifyUser`) | **죽은코드** |
| POST `/secede` | `secedeMember` | **없음** (실제는 `/api/user/secede`) | **죽은코드** |
| POST `/join` | `joinMember` | **없음** (실제는 `/api/join/join`) | **죽은코드** |
| POST `/send-auth-number` | `sendAuthNumber` | **없음** | **죽은코드** |
| POST `/check-auth-number` | `checkAuthNumber` | **없음** | **죽은코드** |
| POST `/find-id` | `findId` | **없음** | **죽은코드** |
| POST `/find-password-step1` | `findPasswordStep1` | **없음** | **죽은코드** |
| POST `/find-password-step2` | `findPasswordStep2` | **없음** | **죽은코드** |
| POST `/change-password` | `changePassword` | **없음** (실제는 `/api/user/changeUserPassword`) | **죽은코드** |
| POST `/delay-change-password` | `delayChangePassword` | **없음** | **죽은코드** |
| POST `/recovery` | `recovery` | **없음** | **죽은코드** |
| GET `/saleson-id` | `salesonId` | **없음** | **죽은코드** |
| POST `/guest-token` | `getAuthGuestToken` | **없음** | **죽은코드** |
| GET `/auth-me` | — | **없음** | **죽은코드** |
| POST `/asis-token` | — | **없음** | **죽은코드** |
| GET `/druh-mig-check` | — | **없음** | **죽은코드** |
| GET `/session-timeout` | `getSessionTimeout` | **없음** | **죽은코드** |
| (주석) `/mobile-auth` 1886, `/mobile-auth-callback` 1947 | — | 소스 주석처리 | **죽은코드** |

### 2-3. `saleson.api.user.UserController` — `/api/user` (회원 실제 동선)
| 엔드포인트 | AS-IS 호출처 | 판정 |
|---|---|---|
| POST `/getUserInfo` | `modify.html`, `popup-layer-kakao.vue` | live |
| POST `/modifyUser` | `modify.html` | live |
| POST `/modifyReceive` | `popup-layer-kakao.vue` | live |
| POST `/confirmPresentPassword` | `modify.html` | live |
| POST `/changeUserPassword` | login/modify/find-idpw, `financ.js` | live |
| POST `/changeUserPasswordForNoLogin` | login/find-idpw, `financ.js` | live |
| POST `/changeUserPasswordLater` | `login.html` | live |
| POST `/changeUserPwdForSignNoLogin` | login/find-idpw, `financ.js` | live |
| POST `/getSecedeInfo` | secede / secede-kakao / onepass_secede | live |
| POST `/secede` | `secede.html` | live |
| POST `/modifyMobileAuth` | `modify.html` | live |
| POST `/checkMobileAuth` | login/modify/find-idpw, `financ.js` | live |
| POST `/checkMobileAuthPwd` | `find-idpw.html`, `financ.js` | live |
| POST `/checkSign` | login/find-idpw | live |
| POST `/signRegister` | `sign-certificate.html`(도달불가)만 | **죽은코드** |
| POST `/signRemove` | `sign-certificate.html`(도달불가)만 | **죽은코드** |
| POST `/financPid` | `financ.js` | live |
| POST `/getLoginCiInfo` | `financ.js` | live |
| POST `/getNonce` | `financ.js` | live |
| POST `/passwordtype` | login / onepass-result / `financ.js` | live |
| POST `/simpleMberCi` | `login.html` | live |

> 전자서명: **검증(`checkSign`)만 살아있고 등록·삭제 경로가 도달불가**. 등록 없이 검증만 도는 반쪽 상태 — MSA 재현 시 그대로 옮길 대상이 아니다.

### 2-4. `saleson.api.mypage.old.MypageController` — `/api/mypage/2`
클래스 전체 600줄. 프론트 래퍼 `getPoints`(`/api/mypage/points`), `getGrade`(`/api/mypage/grade`)는 **`/2` 없이** 호출해 현행 `MypageController`·`old` 어느 쪽에도 매칭되지 않는다. → **패키지 통째 죽은코드**.


## 3. 매퍼 쿼리 인벤토리 (member 관련 9개 XML / 227 쿼리)

판정 방식: 쿼리 id → 매퍼 인터페이스 선언 여부 → 소스 전체에서 `.<id>(` 호출 존재 여부.
AS-IS는 **master/slave 매퍼 이중 주입**(`slaveSecedeUserMapper` 등)과 대문자 필드명(`JoinMapper.getUserInfoByUserId(...)`)이 섞여 있어, 주입 변수명 가정 방식은 오탐이 난다. 호출 자체를 추적하는 방식으로 교차검증했다.

| 매퍼 | namespace | 쿼리수 | 사장 |
|---|---|---|---|
| `user-mapper.xml` | `saleson.shop.user.UserMapper` | 131 | **12** |
| `mypage-mapper.xml` | `saleson.shop.mypage.MypageMapper` | 32 | 0 |
| `generalcustomer-mapper.xml` | `saleson.shop.user.GeneralCustomerMapper` | 27 | 0 |
| `policy-mapper.xml` | `saleson.shop.policy.PolicyMapper` | 10 | 0 |
| `user-sns-mapper.xml` | `saleson.shop.usersns.UserSnsMapper` | 9 | 0 |
| `join-mapper.xml` | `saleson.shop.user.JoinMapper` | 8 | 0 |
| `userauth-mapper.xml` | `saleson.common.userauth.UserAuthMapper` | 5 | 0 |
| `secedeuser-mapper.xml` | `saleson.shop.user.SecedeUserMapper` | 3 | 0 |
| `sleepuser-mapper.xml` | `saleson.shop.user.SleepUserMapper` | 2 | 0 |
| **합계** | | **227** | **12** |

### 3-1. `user-mapper.xml` 사장 쿼리 12건 (죽은코드)
- `getUserListForExcel` — **XML에만 존재**. `UserMapper.java` 인터페이스에 선언조차 없음(완전 고아).
- 인터페이스 선언은 있으나 호출부 0:
  `deleteUserById`, `deleteUserRoleByAuthority`, `getConfirmPurchaseRequestUserList`, `getConfirmPurchaseUserList`,
  `getPasswordCount`, `getPasswordCountByUser`, `getUserCountByUserId`, `getUserCountByUserLevel`,
  `getUserRoleListByLoginId`, `getUserRoleListByUserId`, `mergeUserRole`

> `getPasswordCount*`는 **비밀번호 재사용 금지(직전 N개 대조)** 용도로 보이는데 호출부가 없다 → AS-IS에서 해당 정책 미작동. MSA에 이식할 대상이 아니다(RFP/ISP에 신규 요건으로 있는지는 별도 확인 필요).

## 4. 서비스 로직 대조 — 핵심 3개 플로우

### 4-1. 회원가입 (`JoinController.join` → `joinService.insertUserAndUserDetail`)

AS-IS 실행 순서와 각 단계의 MSA 대응:

| # | AS-IS 규칙 (근거) | MSA (`MemberService.signup`) | 판정 |
|---|---|---|---|
| 1 | `bindingResult.hasErrors() \|\| mberCi 없음 \|\| mberDi 없음 \|\| birthdayFull 없음` → `BAD_REQUEST` (`JoinController:165~170`) | 검사 없음 | **재현누락** — 본인인증(CI/DI) 없이 가입 가능 |
| 2 | `password` 공란 → `BAD_REQUEST` | `password != passwordConfirm` 검사만 | 부분 |
| 3 | `!userInfo.isAuth()` → `NOT_EXIST_AUTH` (`:182`) | 없음 | **재현누락** |
| 4 | `userService.getUserInfoByCi()` 결과 있으면 `DUPLICATION_CI_JOIN_USER` (`:186~193`) — **CI 기준 1인 1계정 강제** | 없음 | **재현누락** (핵심 정책) |
| 5 | `userService.checkDuplication()` — ①`config.deniedId` 콤마목록의 **금지 아이디** 대조 ②`getUserCountByUserInfo` (`UserServiceImpl:1755~1785`) | `userRepository.findByLoginId` 만 | **재현누락** — 금지 아이디 목록 미구현 |
| 6 | `userService.selectNewUserId()` 별도 채번 (`OP_USER` 시퀀스 주석처리됨) | JPA identity | 의도적축소(DB 상이) |
| 7 | `userDetail.setLoginPathCode("100")` | `LOGIN_PATH_IDPW = "100"` | **대응있음** (값 일치) |
| 8 | `joinService.insertUserAndUserDetail(user, userDetail)` | `userRepository.save` + `userDetailRepository.save` + `userRoleRepository.save(ROLE_USER)` | 대응있음 |
| 9 | `parentResponse` 있으면 `OP_USER_PARENT` insert (법정대리인 CI/DI/성명/내외국인/성별) (`:213~241`) | 없음 | **재현누락** — 14세 미만 법정대리인 |

`userModifyDataSet` (`JoinController:384~471`)이 UserDetail에 채우는 항목 중 MSA `SignupForm`(9필드)이 **수집하지 않는 것**:
`telNumber`, `post`/`newPost`(우편번호), `birthdayType`(양/음력), `gender`,
`receiveEmail`·`receiveSms`·`receivePbanc`·`receiveKakao`(**수신동의 4종**),
`locGovList[]`(**관심 지자체 다중선택**), `rtnpsntList[]`(**관심 답례품**),
`mberCi`·`mberDi`·`mberDn`·`mberFinDn`, `locgovCode`, `userKey`

> 단, AS-IS `insertUserDetail`은 `GENDER`를 **리터럴 `null`로 기록**하고 `NEW_POST`/`BIRTHDAY_TYPE`은 INSERT문에 아예 없다. DTO에만 있고 저장되지 않는 필드가 섞여 있으므로, 이식 대상은 **INSERT문 기준**으로 잡아야 한다.
> MSA `UserDetail` 엔티티에 없는 실제 컬럼: `TEL_NUMBER`, `FAX_NUMBER`, `AGE`, `BUY_COUNT`, `BUY_PRICE`, `SITE_FLAG`.

MSA가 **추가로** 하는 것: 가입 환영 알림톡 `WELCOME_SIGNUP` 발송 → AS-IS 대응 여부 확인 필요(RFP/ISP 근거 없으면 [[scope-migration-not-greenfield]] 위반).

### 4-2. 로그인 (`AuthController.getUserToken` `/api/auth/token`)

AS-IS는 **단일 엔드포인트가 3개 역할을 분기**한다: `ROLE_USER` / `ROLE_OPMANAGER` / `ROLE_SELLER`.
(`ROLE_OPMANAGER`·`ROLE_SELLER`는 loginId에서 `OPMANAGER_LOGIN_KEY`/`SELLER_LOGIN_KEY` 접두를 제거해 처리 — `:1246~1250`)

| AS-IS 규칙 (근거) | MSA | 판정 |
|---|---|---|
| 로그인 실패 5회 이상 → 계정 잠금 안내 (`:1200`) | `MAX_LOGIN_FAIL_COUNT = 5` → `STATUS_LOCKED` | **대응있음** |
| `passwordType`: `N`=일반, `T`=임시, **`P`=카카오** (`:1199` 주석) | `N`/`T`만 존재 — **`P` 없음** | **재현누락** |
| `!"P".equals(passwordType) && loginFailCount >= 5` — 카카오 회원은 잠금안내 **제외** (`:1200`) | 예외 없음 | **재현누락** |
| `!"P".equals(passwordType) && passwordExpiredDateDiff <= 0` → `PASSWORD_EXPIRED` (`:1232`) | `passwordExpiredDate` + `/password?expired=` 있으나 **`P` 예외 없음** | 부분 — SNS 회원이 비번만료 대상이 됨 |
| `"T".equals(passwordType)` → `PASSWORD_TEMP` (`:1235`) | `PASSWORD_TYPE_TEMPORARY` | 대응있음 |
| `"4".equals(statusCode)` → `SLEEP_USER` (`:1238`) | `STATUS_DORMANT` + `/reactivate` | 대응있음(코드체계 상이) |
| `userKey 있음 && loginPath=="300"` → `ONEPASS_USER` (`:1241`) | `ExternalLoginController` onepass (mock) | 의도적축소(외부연계) |
| `ROLE_ADMIN_7`/`ROLE_ADMIN_8`(오프라인 담당자) → 사용자 페이지 접근 차단 `OFF_ACCESS_FRONT` (`:1204~1211`) | 없음 | **재현누락** |
| ASIS 인증 API 통신 (`:1215`) | 없음 | 의도적축소(레거시 연계) |
| `INCORRECT_PASSWORD_LOCK` / `UNAUTHORIZED_LOCK` + `getUserLockMessage()` 분기 | 단일 메시지 | 부분 |

> `code`는 **순차 대입이라 마지막이 이긴다**: 우선순위 `ONEPASS_USER > SLEEP_USER > PASSWORD_TEMP > PASSWORD_EXPIRED`.
> MSA 카카오 가입(`ExternalLoginService:146`)은 랜덤 비밀번호만 넣고 `passwordType`을 설정하지 않아, **SNS 회원이 비밀번호 만료·5회 잠금 정책에 걸린다**. AS-IS와 동작이 다르다.

### 4-3. 전자서명 (반쪽 상태)
- `checkSign`(검증)만 live — `login.html`, `find-idpw.html`
- `signRegister`/`signRemove`(등록·삭제)는 `sign-certificate.html`에서만 호출되는데 **그 화면이 도달불가**
- → AS-IS 운영상 **등록 경로 없이 검증만 도는 상태**. 그대로 이식할 대상이 아니다. [[member-service-deferred-items]]의 "전자서명" 항목 판단 근거로 사용.

## 5. 화면 이벤트 핸들러 대조

### 5-1. `users/login.html` → MSA `login.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `setTab(1)` / `setTab(2)` / `setTab(3)` | 로그인 방식 탭 **3개** | `loginSetTab(1)` / `loginSetTab(2)` — **2개** | **재현누락** (탭 1개 부족) |
| `@submit.prevent="submit"` + `@click="submitBefore"` | 일반 로그인(전처리 후 제출) | 폼 submit | 대응있음 |
| `@keydown="enterInput($event)"` | 엔터 제출 | 확인필요 | |
| `@keyup="checkPwd($event)"` | 실시간 비밀번호 유효성 | 확인필요 | |
| `@click.prevent="submitKakao"` | 카카오 로그인 | `startKakaoCert()` | 대응있음(mock) |
| `@click.prevent="submitNaver"` | 네이버 로그인 | 없음 | **재현누락** |
| `@click="financLogin()"` | 금융인증서 로그인 | `/login/finance-cert` (mock) | 의도적축소 |
| `@click="openMobileAuth()"` | 휴대폰 본인인증 레이어 | 확인필요 | |
| `@click="onepassLoginBefore()"` / `@submit.prevent="onepassLoginBefore"` / `@keyup="changeOnePassId()"` / `@keydown="enterInputOnepass($event)"` | 원패스 로그인(전용 입력 4핸들러) | `/onepass-login` (mock) | 의도적축소 |
| `@click="changPwd()"` / `@click="changPwdLater()"` | 비번만료 시 변경 / 나중에 | `/password`, `/password/postpone` | 대응있음 |
| `@click="closePopupLayer()"` | 레이어 닫기 | 확인필요 | |

### 5-2. `users/join.html` → MSA `signup.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `@click="checkIdUsedYn()"` | 아이디 중복확인 | `checkLoginId()` | 대응있음 |
| `@change="changeId()"` | 아이디 변경 시 중복확인 상태 초기화 | 확인필요 | |
| `@keyup="checkPwd()"` | 실시간 비밀번호 규칙 검사 | 확인필요 | |
| `@click="eyeToggle()"` | 비밀번호 보기 토글 | `eyeToggle('password')` | 대응있음 |
| `@change="emailChange($event)"` | 이메일 도메인 선택 | `onEmailDomainChange()` | 대응있음 |
| `@click="searchAddress()"` | 주소검색 팝업(`jusoPopup.html`) | `signup.html`엔 없음 / `profile.html`엔 있음 | **재현누락**(가입 단계) |
| `@change="getSubLocGovList($event)"` | 상위 지자체 선택 → 하위 목록 로드 (`/api/join/getSubLocGov`) | 없음 | **재현누락** |
| `@click="addRow()"` / `@click="deleteRow(data, index)"` | **관심 지자체 다중 행 추가/삭제** | 없음 (프로필에만 `addInterestLocgov`/`removeInterestLocgov`) | **재현누락**(가입 단계) |
| `@click="openMobileAuth()"` | 휴대폰 본인인증 | `/signup/mobile-auth` (mock) | 의도적축소 |
| `@click="submit()"` | 가입 제출 | `onsubmit="return beforeSubmit()"` | 대응있음 |
| `@click="closePopupKakao()"` | 가입완료 모달 닫기 | 완료 모달 닫기 | 대응있음 |
| `@click="goToMain()"` ×2 | 메인 이동 | `location.href='/'` | 대응있음 |

MSA가 **추가로** 가진 것: `goToStep(1|2|3)` 3단계 위저드, `toggleAll()` 전체약관, `onRequiredChange()`, `onAdParentChange/onAdChildChange` 수신동의 토글, `startNaverJoin()`.

### 5-3. 수신동의 — 양쪽 모두 비정상 (별개 원인)
- **AS-IS**: `join.html:374~414`의 수신동의 라디오(`receivePbanc`/`receiveSms`/`receiveEmail`/`receiveKakao`)가 **전부 주석 처리**. 기본값은 `"1"`이고 `join.html:827~838`에서 `'0'`일 때만 세션 동의 플래그를 세우므로, 일반 가입은 **항상 4종 미동의('1')로 저장**된다. 화면에 남은 "국민비서·SMS 수신동의 / email 수신동의" 문구는 **가입완료 모달 안의 안내 텍스트**이지 입력이 아니다(“미동의를 선택하실 경우 마이페이지 > 동의 항목을 미선택으로 체크하시기 바랍니다”).
  → 값 규약: **`0` = 동의, `1` = 미동의**
- **MSA**: `signup.html:106~119`의 체크박스 4종(`adSms`/`adEmail`/`adPbanc`/`adKakao`)에 **`name`도 `th:field`도 없다**. `SignupForm`에 대응 필드도 없다. 사용자가 체크해도 서버로 전송되지 않아 `UserDetail.receive*`는 **null**로 남는다. → **배선 끊김(버그)**
- 라벨 대응도 어긋난다: MSA는 `id="adSms"`에 "국민비서", `id="adPbanc"`에 "SMS" 라벨을 붙였다. AS-IS는 `receivePbanc`가 국민비서(공공알림), `receiveSms`가 SMS다. → **id와 라벨이 서로 뒤바뀜**

### 5-4. 가입 완료 후 동선
| | AS-IS (`join.html:796~845`) | MSA (`AuthController:117~133`) |
|---|---|---|
| 1 | `/api/join/join` 성공 | `memberService.signup(form)` |
| 2 | `$s.ev.log.joinUser(userId)` — GA 이벤트 로깅 | 없음 → **재현누락** |
| 3 | `getAuthToken({loginType:'ROLE_USER', ...})` **자동 로그인** | `session.setAttribute` + `authCookieSupport.issue` **자동 로그인** — 대응있음 |
| 4 | 세션 `LOGIN_FIRST='Y'` | 없음 → **재현누락** |
| 5 | `receive*=='0'`이면 `EMAIL_AGREE`/`SMS_AGREE`/`PBANC_AGREE`/`KAKAO_AGREE` 세션 플래그 | 없음 → 재현누락(단 AS-IS도 기본값 '1'이라 실제 미작동) |
| 6 | `$s.redirect($s.pages.INDEX)` — **메인 이동** | `redirect:/signup?completed=1` — **가입화면 완료 모달** | 차이(경미). MSA 모달은 AS-IS의 인증가입 경로 `#joinComplete` 모달을 재현한 것 |

### 5-4. `users/modify.html` → MSA `profile.html` / `personal-info.html`
| AS-IS 핸들러 | 역할 | MSA | 판정 |
|---|---|---|---|
| `@click="getMobileAuth('name')"` / `('phone')` / `('password')` | **용도별 본인인증 3종** | 없음 | **재현누락** |
| `@click="checkPresentPwd()"` | 현재 비밀번호 확인 | `/api/password/verify` | 대응있음 |
| `@click="changPwd()"` | 비밀번호 변경 | `/api/password` | 대응있음 |
| `@click="onepassCancel()"` | 원패스 연동 해제 | 없음 | **재현누락** ([[member-service-deferred-items]] "SNS 연동해지") |
| `@click="kakaoLinkClear()"` | 카카오 연동 해제 | 없음 | **재현누락** (동상) |
| `@click="goToSecede()"` | 탈퇴 화면 이동 | 있음 | 대응있음 |
| `@click="searchAddress()"` | 주소검색 | `searchAddress()` | 대응있음 |
| `@change="upperLocgovChange($event)"` | 상위 지자체 변경 | `filterAddLocgov()` | 대응있음(형태 상이) |
| `@click="addRow()"` / `@click="deleteRow(data,index)"` | 관심 지자체 추가/삭제 | `addInterestLocgov()` / `removeInterestLocgov(this)` | 대응있음 |
| `@click="authLayer()"` | 인증 레이어 열기 | 확인필요 | |
| `@change="emailChange($event)"` | 이메일 도메인 | `onEmailDomainChange()` | 대응있음 |
| `@keyup="checkPwd()"` | 실시간 비번 검사 | 확인필요 | |

### 5-5. `users/secede.html` → MSA `withdraw.html`
| AS-IS | MSA | 판정 |
|---|---|---|
| `@click="submitConfirm()"` | `onsubmit="return beforeSubmit()"` | 대응있음 |
| `@click="goToModify()"` | `location.href=this.dataset.url` | 대응있음 |
| `@click="focusTar($event)"` | 없음 | 확인필요(접근성 포커스 이동) |

### 5-6. `users/find-idpw.html` → MSA `find-idpw.html`
| AS-IS | MSA | 판정 |
|---|---|---|
| `swapTab('id')` / `swapTab('password')` | `setTab('id')` / `setTab('pw')` | 대응있음 |
| `@submit.prevent="findId"` | `resetId()` | 대응있음 |
| `@submit.prevent="findPasswordStep1"` / `findPasswordStep2` | `authSendCode()` / `authVerify()` / `pwReset()` | 대응있음 |
| `authLayer()` / `authLayerPwd()` | `openAuthModal('id')` / `openAuthModal('pw')` | 대응있음 |
| `changePwd()` | `resetPw()` | 대응있음 |
| `goLogin()` / `goJoin()` / `goMain()` | `location.href` 3종 | 대응있음 |
| — | `authBypass()` / `startMockAuth()` | MSA 전용(개발 우회) |

## 6. 조치 후보 (우선순위)

**A. 버그 — AS-IS 재현 의도가 있는데 배선이 끊긴 것**
1. `signup.html` 수신동의 4종에 `th:field` 부재 → 폼 전송 누락. `SignupForm`에 `receiveSms/receiveEmail/receivePbanc/receiveKakao` 추가 필요. 값 규약 `0=동의 / 1=미동의`.
2. 동 체크박스의 `id`↔라벨 뒤바뀜(`adSms`=국민비서, `adPbanc`=SMS).
3. `ExternalLoginService`가 SNS 가입 시 `passwordType='P'`를 설정하지 않아 SNS 회원이 비밀번호 만료·5회 잠금 정책에 걸림.

**B. 재현누락 — AS-IS에 살아있는 로직인데 MSA에 없는 것**
4. 가입 시 **CI 기준 1인 1계정** 검사(`DUPLICATION_CI_JOIN_USER`)
5. 가입 시 본인인증 필수(`mberCi`/`mberDi`/`birthdayFull` + `isAuth`)
6. 금지 아이디 목록(`config.deniedId`) 검사
7. 가입 단계의 **관심 지자체 다중선택**(`addRow`/`deleteRow`/`getSubLocGov`) — 프로필엔 있으나 가입엔 없음
8. 가입 단계 주소검색(`searchAddress`)
9. `modify` 화면의 용도별 본인인증 3종(`name`/`phone`/`password`)
10. SNS·원패스 **연동 해제**(`kakaoLinkClear`/`onepassCancel`)
11. 로그인 탭 3번째(AS-IS 3개 / MSA 2개), 네이버 로그인 버튼
12. `ROLE_ADMIN_7/8` 오프라인 담당자의 사용자페이지 접근 차단
13. 14세 미만 법정대리인(`OP_USER_PARENT`)

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `/api/auth/*` 회원기능 16개 엔드포인트, `/api/mypage/2` 패키지 전체, `user-mapper` 사장쿼리 12건
- 전자서명 등록/삭제(`signRegister`/`signRemove`) — 화면 도달불가
- 휴면·비번변경 **전용 화면**(`sleep-user.html`/`change-password.html`) — 파일 자체가 없음
- 비밀번호 재사용 금지(`getPasswordCount*`) — 호출부 0

**D. 근거 확인 필요 (RFP/ISP 대조 대상)**
- MSA 전용 추가분: 가입 환영 알림톡 `WELCOME_SIGNUP`, 3단계 위저드, MFA, `authBypass`/`startMockAuth`

---

## 7. 조치 결과 (2026-09-10)

### 7-0. 초판 오판 정정 4건
검증 방식의 사각지대 때문에 §5에서 잘못 판정한 항목이다.

| 항목 | 초판 판정 | 실제 | 원인 |
|---|---|---|---|
| 로그인 탭 3개 | 재현누락(탭 부족) | **대응있음** — AS-IS `login.html:273~274`의 `setTab(3)`은 **주석 처리**. 살아있는 탭은 2개 | 주석 여부를 보지 않고 핸들러만 셈 |
| 네이버 로그인 버튼 | 재현누락 | **대응있음** — MSA `login.html:86`에 `<a th:href="@{/login/naver}">` 존재 | `onclick=`만 grep해 `<a href>` 이동을 놓침 |
| `/api/user/modifyReceive` | 재현누락 | **대응있음** — MSA `profile.html:158~161`에 수신동의 라디오 4종(`name` 정상), `ProfileController:143~146`이 바인딩 | 가입 화면만 보고 프로필을 보지 않음 |
| `getMobileAuth('name'/'phone'/'password')` | 재현누락 | **의도적축소** — Siren24 PCC 유료연동 부재로 `/coming-soon` 대체. `profile.html:16~19`에 명시 | 해당 화면의 주석을 읽지 않음 |

> 교훈: 화면 이벤트 대조는 `onclick`/`@click`만으로 부족하다. **`<a href>` 내비게이션, 주석 처리 여부, 대응 화면이 여러 개로 쪼개진 경우**를 함께 봐야 한다. [[verify-screen-by-content-not-route]]

### 7-1. 완료 (12건)

**버그 수정**
1. **SNS 회원 `passwordType='P'` 누락** — AS-IS `KakaoLinkServiceImpl:702`가 KAKAO/NAVER 로그인·연동 때마다 `P`를 찍고, 이를 3곳에서 읽어 비밀번호 만료(`AuthController:1232`, `JwtTokenAuthenticationFilter:542`)와 5회 잠금(`:1200`)에서 제외한다. MSA는 `P` 자체가 없어 **SNS 회원이 비밀번호 만료 대상이 되고 잠길 수 있었다**.
   - `ExternalLoginService`: `PASSWORD_TYPE_SNS` + `SNS_PASSWORD_TYPE_PROVIDERS`(KAKAO/NAVER) 도입, `linkNewAccount`(신규연동)와 `linkOrJoin`의 기존회원 분기(`stampSnsPasswordType`) 양쪽에 적용 — AS-IS가 **매 로그인마다** 다시 찍는 것을 그대로 따랐다.
   - `MemberService.passwordChangeRequired()`: `P`면 즉시 `false`.
   - `MemberService.checkCredentials()`: `justLocked` 조건에 `&& !P` 추가.

**정책 재현**
2. **가입 시 본인인증 필수** (`JoinController:165~170`, `:182`) — `MemberService.requireVerifiedIdentity()`. 이 환경은 인증 게이트웨이가 없어 `DevBypassSettings.identityVerificationBypass`가 켜진 동안만 건너뛴다(로컬 `application.yml`은 `true`, **기본값·운영은 `false`**라 AS-IS와 동일하게 막힌다).
3. **CI 기준 1인 1계정** (`JoinController:186~193` `DUPLICATION_CI_JOIN_USER`) — 같은 메서드. **CI가 들어온 경우엔 우회 여부와 무관하게 항상 검사**한다(카카오 인증서비스처럼 실제 CI가 오는 경로를 막아야 하므로).
4. `SignupForm`에 `mberCi`/`mberDi` 추가, `signup()`이 `User.MBER_CI`/`MBER_DI`에 저장.
5. **금지 아이디 목록** (`UserServiceImpl:1755~1785` `config.deniedId`) — `MemberPolicySettings`(신규) + `MemberService.loginIdAvailable()`. AS-IS가 **아이디 중복확인 엔드포인트에서도 같은 `checkDuplication()`을 태우므로**(`JoinController:129`), MSA `/api/check-login-id`도 같은 규칙으로 바꿨다. 설정 키 `ghlove.member.denied-login-ids`(admin 설정 이관 시 주입원만 교체).
6. **오프라인 담당자 차단** (`AuthController:1204~1212` `OFF_ACCESS_FRONT`) — `MemberService.rejectOfflineManager()`. `ROLE_ADMIN_7`(주담당자)/`ROLE_ADMIN_8`(부담당자)의 사용자 페이지 로그인을 막는다.
   - **AS-IS와의 의도적 차이**: AS-IS는 비밀번호 확인 *전에* 검사해서 비밀번호를 모르는 사람도 응답만으로 "그 아이디가 오프라인 담당자"임을 알아낼 수 있다. MSA는 비밀번호 확인 *후*로 옮겼다 — 정상 이용자가 겪는 동작은 동일하다.

**가입 화면 재현**
7. **주소찾기** (`join.html` `searchAddress()`) — `signup.html`에 우편번호 입력 + 주소찾기 버튼 + Daum 우편번호 위젯. `SignupForm.post` 추가 → `UserDetail.POST` 저장(AS-IS `insertUserDetail`의 POST 컬럼).
8. **관심 지자체 선택** (`join.html` `getSubLocGovList`/`addRow`/`deleteRow`) — `signup.html` step 3에 시·도 → 시·군·구 필터(`filterAddLocgov`)와 추가/삭제 칩(`addSignupLocgov`/`removeSignupLocgov`). 목록은 `AuthController.signupForm`이 `DonationClient.allLocgovs()`로 공급(263건 확인).
   - 관심지자체는 donation이 보유하고 `POST /interest-locgovs`가 쿠키 인증이라, 가입 시점엔 화면에만 담아두고 `sessionStorage`로 넘겨 **가입완료 모달(자동 로그인으로 `GH_AUTH` 발급된 뒤)** 에서 donation(:8082)으로 등록한다. donation의 `@CrossOrigin`이 이미 `8081` + `allowCredentials`를 허용한다.
   - AS-IS는 시·군·구를 서버(`/api/join/getSubLocGov`)에서 매번 조회하지만, MSA는 전체 목록을 한 번 받아 클라이언트에서 거른다(왕복 제거).

### 7-2. 검증
`member` 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 금지 아이디 `admin`/`test`/`ghlove` | `{"available":false}` |
| 기존 회원 `test007` | `{"available":false}` |
| 미사용 `newuser999` | `{"available":true}` |
| CI 있는 가입 | `{"status":"OK"}` |
| 같은 CI 재가입 | `{"status":"ERROR","message":"이미 가입된 본인인증 정보입니다..."}` |
| 금지 아이디로 가입 | `{"status":"ERROR","message":"이미 사용 중인 아이디입니다."}` |
| 우회 OFF(8091 별도 기동), 본인인증 없이 가입 | `{"status":"ERROR","message":"본인인증을 완료해야 회원가입을 할 수 있습니다."}` |
| 우회 OFF, CI/DI 갖춰 가입 | `{"status":"OK"}` |
| `/signup` 렌더 | 우편번호 input 1, 주소찾기 버튼, 지자체 option 263 |

검증용 계정 2건(`citest001`/`withci001`)은 `op_user_role`→`op_user_detail`→`op_user` 순으로 삭제 완료(차단된 시도는 애초에 행을 만들지 않았다).

### 7-3. 미처리 — 결정이 필요한 것
- **수신동의**: 사용자 지시로 현행 유지. 값 규약은 확인해 둠 — **`0`=동의, `1`=미동의**(`join.html:827~838`, `popup-layer-kakao.vue:191~194`).
- **카카오 알림톡**(RFP `requirements.md:94` 신규): 사용자 지시로 추후.
- **SNS·원패스 연동 해제**(`kakaoLinkClear`/`onepassCancel`/`/api/auth/disconnect-sns`): [[member-service-deferred-items]]의 결정 대기 항목.
- **`secede-kakao.html` / `onepass_secede.html`** 전용 탈퇴 화면: SNS 연동 해제와 같은 묶음.
- **14세 미만 법정대리인**(`OP_USER_PARENT`): MSA에 테이블은 이미 존재(`member.op_user_parent`). AS-IS는 보호자 휴대폰인증 모달(`join.html:126 getParentModal`)을 거치는데 이 환경엔 본인인증 연계가 없어, 인증 개방 전에는 재현 불가.
- **`passwordtype` 조회 / `modifyMobileAuth`**: 본인인증 연계 의존.
- **`UserDetail` 미보유 컬럼**(`TEL_NUMBER`/`FAX_NUMBER`/`AGE`/`BUY_COUNT`/`BUY_PRICE`/`SITE_FLAG`): 화면 노출처와 함께 정할 사항.
