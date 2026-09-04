# AS-IS 기능 감사 — 회원(member) 서비스

작성일: 2026-09-03
범위: `ghlove-web`(saleson 백엔드 + opmanager 관리자콘솔) / `ghlove-frontend`(Vue2 고객화면)의 회원(User) 도메인 전체를
`ghlove-msa`의 `member`/`admin`/`storefront`와 대조. admin 서비스의 6단계 RBAC(ROLE_ADMIN_1~6) 자체와 인증서(PKI)
로그인은 이미 별도 라운드에서 구현 완료로 확인되어 이번 감사 범위에서 제외했다(`admin_rbac_6tier_role_round`,
`admin_console_auth` 메모 참고).

## 1. AS-IS 회원 도메인 기능 전수 목록

### 1-A. 고객용(회원 본인) 기능 — storefront/member 서비스

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 회원가입/로그인/로그아웃 | `ghlove-web/.../saleson/shop/user/UserController.java`(join/login), `ghlove-frontend/users/join.html,login.html` | 완료 | member `AuthController`/`AuthApiController` + storefront |
| 아이디/비밀번호 찾기·비밀번호변경 | `UserController.java`(find-id/find-password/change-password), `users/find-idpw.html` | 완료 | round `login_findidpw_and_mypage_redirect_round` |
| 회원정보수정 | `UserController.java`(/modify), `users/modify.html` | 완료 | member `ProfileController`/storefront ProfileView |
| 회원탈퇴 | `UserController.java`(/secede), `users/secede.html` | 완료 | member 탈퇴 플로우 |
| 휴면전환(자동배치)/본인 해제(로그인시 재활성화) | `UserController.java`(sleep-user/wakeup-user) | 완료 | member `DormancyController`(`/batch/dormancy`,`/reactivate`) |
| 관심 지자체 등록/해제 | `mypage/intrstLocGov.html` | 완료 | storefront ProfileView (round9에서 form-urlencoded 계약버그 수정) |
| 배송지 관리 | `saleson/shop/userdelivery/*` | 완료 | member `DeliveryController`/`DeliveryApiController` |
| SNS(카카오/네이버) 로그인 연동, 디지털원패스 본인인증 | `saleson/shop/usersns`,`snsuser`, `users/onepass-*.html` | 완료(mock-gated) | `external_integrations_architecture` 메모, `OnePassClient`/`OAuth2LoginClient` |
| 관리자권한/역할 신청+승인대기 | `saleson/shop/user/ManagerRequestController.java`(비로그인 신청폼) | 완료 | member `RoleRequestController`+admin 승인큐 |
| 마이페이지 상세 7종(1:1문의/관심지자체/회원정보수정/관심답례품/기부혜택증/기부포인트/기부내역) | `mypage/*.html` | 완료 | `mypage_detail_screens_round` |
| 개인정보처리방침 등 정책 동의 | 회원가입 약관동의 | 완료 | `vue3_storefront_migration_round13_policy` |
| 개인정보 파기절차(탈퇴 후 유예기간 파기) | — | 완료 | `sfr_gap_fill_round2_mfa_destruction_sla`, member `UserDataDestructionLog`/`DataDestructionController` |
| SNS 계정 연동/해제(로그인 아닌 마이페이지 자기관리) | `saleson/shop/usersns/UserSnsController.java`(setup-sns/disconnect-sns) | 확인 필요(낮음) | 카카오/네이버 OAuth 로그인 자체는 대체됐으나, "이미 가입된 계정에 SNS를 나중에 연결/해제"하는 자기관리 UI가 별도로 있는지는 미검증. 아래 gap 참고 |

### 1-B. 관리자용(opmanager) "회원관리" 기능 — admin 서비스

이 카테고리가 이번 감사의 핵심 발견이다. **admin 서비스에는 회원(고객) 통합 관리 화면이 전혀 없다.**
`admin/src/main/java/com/ghlove/admin/service/MemberClient.java`를 확인한 결과 admin↔member 연동은
다음 3가지 읽기전용 호출뿐이다: ①관리자권한신청서 자동입력용 `GET /api/users/{id}`, ②통계 재동기화용
`GET /api/admin/members/all`, ③로그인/액션 로그 화면용 `GET /api/admin/login-log`,`/api/admin/user-action-log`.
회원을 검색/조회/수정/정지/등급변경/포인트지급/강제탈퇴 하는 화면, 관리자(운영자) 계정을 CRUD하는 화면,
지자체 담당자 계정을 관리하는 화면이 admin 템플릿 어디에도 없다.

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 회원(고객) 통합 목록/등록/수정/삭제, 관리자role·메뉴권한 관리, 상세팝업(정보/수정/탈퇴/비번변경/쿠폰/포인트/주문내역) | `saleson/shop/user/UserManagerController.java`(`/opmanager/user/*`, 2000+줄) | **미구현** | §2-1 |
| 일반회원관리(PII 마스킹+비밀번호 재확인 열람, 기부내역/포인트내역 팝업, 탈퇴사유+누계 표시) | `saleson/shop/user/GeneralCustomerManagerController.java`(`/opmanager/user/customer/*`) | **미구현** | §2-1, UserManagerController를 대체하는 최신 버전으로 보임 |
| 지자체 계정/정보 관리(직인이미지, 지자체별 포인트정책, 부서, SYS/LOC 권한분기) | `saleson/shop/user/LocgovManagerController.java`(`/opmanager/user/locgov/*`) | **미구현** | §2-2 |
| 지자체 담당자(정/부, ROLE_ADMIN_5·6) 계정관리 | `saleson/shop/user/LocgovPersonInChargeManagerController.java`(`/opmanager/user/locgov-charger/*`) | **미구현** | §2-2 |
| 오프라인 접수 담당자 계정관리(생성/수정/삭제/엑셀/상태변경/정담당-부담당 교체) | `saleson/shop/user/OffPersonInChargeManagerController.java`(`/opmanager/user/off-charger/*`) | **미구현** | §2-2 |
| 운영자 담당자 계정관리 | `saleson/shop/user/OperPersonInChargeManagerController.java`(`/opmanager/user/oper-charger/*`) | **미구현** | §2-2 |
| 탈퇴회원 목록/탈퇴사유 상세조회(읽기전용) | `saleson/shop/user/SecedeUserManagerController.java`(`/opmanager/user/secede-user/*`) | **미구현** | §2-3 |
| 휴면회원 목록/검색 + 관리자가 개별 회원 수동 해제(wakeup) | `saleson/shop/user/SleepUserManagerController.java`(`/opmanager/user/sleep-user/*`) | **부분구현** | member `DormancyController`는 배치실행(건수만 표시)+본인셀프해제뿐, 목록조회·검색·관리자의 개별 wakeup 없음. §2-3 |
| 회원등급(포인트등급/할인등급) 마스터 관리 | `saleson/shop/userlevel/UserLevelManagerController.java`(`/opmanager/user-level/*`) | **미구현** | §2-4, member `UserDetail.levelId` 필드는 존재하나 값을 채우는 관리화면이 없어 죽은 FK 상태 |
| 지자체 명예사용자(시도민증 이미지, 등급기준) 관리 + 열람이력 통계 | `saleson/shop/lclgvHnrUser/LclgvHnrUserManagerController.java`(`/opmanager/lclgvHnrUser/*`) | **미구현** | §2-5, 고객화면(명예기부자 등급 표시)은 `storefront/.../HonorCertificatesView.vue`로 간이 구현되어 있으나 그 등급/이미지를 지자체별로 세팅하는 관리자 화면은 없음 |
| 사용자권한그룹 관리 | `saleson/shop/usergroup/UserGroupController.java`(`/opmanager/user-group/*`) | 의도적 제외(중복 확인됨) | §2-6, 내부적으로 `roleService.insertRole()`을 그대로 호출 — admin의 `/admin/access/*`(RBAC role 관리)와 동일 엔티티를 다루는 AS-IS의 또다른 메뉴 진입점일 뿐. 신규 구현 불필요 |
| 관리자 권한요청(PKI 인증서 신청폼 포함) | `saleson/shop/user/ManagerRequestController.java`(`/opmanager/manager-request/*`) | 완료(대응 확인됨) | admin `ManagerRequestController`(`/admin/manager-requests/*`) + `CertLoginController`(`/admin/my-cert/*`)로 대응. PKI 신청폼 세부 문항 1:1 대조는 안 함 |
| 로그인 실패횟수 기반 계정 잠금 | AS-IS grep 결과 없음(`saleson/shop/user`,`saleson/shop/auth`, `ghlove-common` 전체에 lockout/failCount 로직 부재) | 해당없음 | AS-IS 자체에 없는 기능으로 확인됨 — 스킵 근거 있음 |
| 다중 디바이스/동시세션 제한 | AS-IS 소스에서 명시적 세션제한 로직 미확인 | 확인 필요(낮음) | 프레임워크 레벨(Spring Security concurrent-session) 설정일 가능성이 높아 애플리케이션 기능 감사 범위 밖으로 판단, 배포설정 검토 시 별도 확인 권장 |

## 2. Gap 상세

### 2-1. [높음] 관리자 콘솔 "회원(고객) 관리" 화면 전체 부재

**왜 필요한가**: 운영자가 민원 대응, 결제/포인트 오류 정정, 부정가입 의심 계정 탈퇴처리 등을 하려면 회원을
검색해서 상세를 보고 직접 조작할 수 있는 화면이 반드시 필요하다. 현재 admin은 회원 데이터를 **전혀 조회할 수
없다** — member 서비스에 로그인해서 DB를 직접 봐야 하는 상태다. 이 프로젝트의 admin 콘솔이 "123개 화면
전부 완료"로 기록되어 있지만(`admin_console_real_asis_markup_round`), 그 목록에 회원관리 카테고리가
빠져 있었다는 것이 이번 감사의 핵심 발견이다.

**AS-IS 동작 방식** (두 컨트롤러가 공존, 후자가 최신):
- `UserManagerController`(`/opmanager/user/*`): `customer/list-old`(구버전 목록), `customer/create`(회원 직접등록),
  `customer/point`·`customer/point-by-excel`(전체/엑셀 일괄 포인트지급), `manager/list`·`create`·`edit`·`delete`
  (ROLE_OPMANAGER 관리자 계정 CRUD), `manager-role/list`·`create`·`edit`(Role/메뉴권한 CRUD — 이건 admin에
  `/admin/access/*`로 이미 있음, 중복 불필요), 팝업 시리즈: `popup/details`(회원상세), `popup/edit`(정보수정),
  `popup/sns-user`(SNS연동상태), `popup/delete`(관리자에 의한 강제탈퇴, 탈퇴사유 기록), `popup/password`
  (비밀번호 강제변경/SMS·이메일 발송), `popup/coupon`(보유쿠폰 내역), `popup/point/{pointType}`(포인트내역,
  기본포인트/배송비쿠폰 등 pointType별), `popup/point-create`(개별 포인트 지급·차감), `popup/order`(주문내역).
- `GeneralCustomerManagerController`(`/opmanager/user/customer/*`, 신버전): 목록/상세는 **기본적으로 PII를
  마스킹**해서 보여주고, `popup/password/{userId}`(관리자 자신의 비밀번호 재확인) → `popup/access/{userId}`
  (통과 시에만 마스킹 해제된 상세 표시)라는 2단계 열람 절차를 거친다. 상세에는 회원누계
  (`getGeneralCustomerCumulativeTotal`), 기부내역(`details/{userId}/cntr-list`), 포인트내역
  (`details/{userId}/point-list`), 배송지(`delivery/{userId}`)가 탭/팝업으로 딸려 있고, 탈퇴는
  `popup/secede`에서 사유를 받아 처리하며 처리자 본인이 탈퇴 대상이면 로그아웃까지 트리거한다.

  **주의**: 기존 메모 `admin_pii_display_no_masking`은 "AS-IS opmanager 화면은 마스킹 없이 노출"이라고
  기록했는데, 이는 구버전 `UserManagerController.popup/details`(마스킹 없음)만 확인한 결과였다.
  신버전 `GeneralCustomerManagerController`는 **명시적으로 마스킹+비밀번호 재확인 열람** 패턴을 쓴다.
  회원관리 화면을 새로 만들 때는 신버전(GeneralCustomerManagerController) 쪽 PII 마스킹 정책을 따라야
  기존 메모와 상충하지 않게 재정리된다 — 즉 "opmanager는 마스킹 안 함" 규칙은 회원관리 화면에는
  적용하지 말 것.

**TO-BE에서 뭘 만들어야 하는가**:
- admin 신규 컨트롤러 `MemberAdminController`(가칭) + 템플릿 `member/*` (또는 `user/*`): 목록(검색조건:
  로그인ID/이름/이메일/전화번호/가입일/상태), 상세(마스킹 기본, 비밀번호 재확인 후 언마스킹), 정보수정,
  강제탈퇴(사유입력), 비밀번호 강제변경, 포인트내역+개별지급/차감(point 서비스 API 필요),
  쿠폰내역(order/coupon 서비스), 주문내역(order 서비스), 기부내역(donation 서비스), 배송지(member 서비스).
- member 서비스에 admin 전용 API 신설 필요: 목록/검색(`GET /api/admin/members?...`), 상세(마스킹 여부
  파라미터 또는 별도 unmask 엔드포인트), 수정, 강제탈퇴, 비밀번호 강제변경. 현재 `UserApiController`는
  `/api/users/{userId}`(단건, MemberInfo record — 마스킹 없음, 프리필 전용), `/api/admin/members/all`
  (통계용 스냅샷), `/api/admin/login-log`,`/api/admin/user-action-log`만 있고 admin이 CUD를 걸 수 있는
  API가 전무하다.
- 회원 직접 등록(`customer/create`, 신규가입쿠폰 자동발급 포함)은 우선순위 낮음(관리자가 대신 가입시키는
  기능은 실사용 빈도 낮음) — 필요시 후순위.

### 2-2. [높음] 지자체·오프라인·운영자 "담당자 계정" 관리 화면 부재

**왜 필요한가**: 현재 admin은 관리자권한신청(`/admin/manager-requests/*`) 승인 시 **계정이 생성되는 것**까지는
커버하지만, 승인 후 이미 존재하는 담당자 계정을 목록으로 보거나, 담당 지자체를 바꾸거나, 정담당/부담당을
교체하거나(`auth-swap`), 비밀번호를 초기화하거나, 계정을 삭제하는 사후관리 화면이 전혀 없다. 신청→승인
플로우만으로는 "이 지자체 담당자가 퇴사해서 계정을 지워야 한다" 같은 운영 시나리오를 처리할 수 없다.

**AS-IS 동작 방식**:
- `LocgovManagerController`(`/opmanager/user/locgov/*`): 로그인한 관리자의 역할(SYS=시스템/행안부 전체목록,
  LOC=지자체담당자 본인 지자체로 자동 리다이렉트)에 따라 분기. 지자체 정보(직인이미지 `sealView`, 연도별
  포인트정책 `point/{stdrYear}/{locgovCode}`, 부서 `dept/popup`) CRUD.
- `LocgovPersonInChargeManagerController`(`/opmanager/user/locgov-charger/*`): 지자체 담당자 계정
  목록/수정/삭제, 상위지자체 기준 드릴다운(`locgov/{upperLocgovCode}/list`).
- `OffPersonInChargeManagerController`(`/opmanager/user/off-charger/*`): 오프라인 담당자 계정
  목록/수정/삭제/엑셀다운로드/상태변경(`status/edit`)/**정담당↔부담당 교체(`auth-swap`)**/비밀번호
  초기화(`popup/password-init`)/생성(`create`,`createProcess`).
- `OperPersonInChargeManagerController`(`/opmanager/user/oper-charger/*`): 운영자 담당자 계정
  목록/수정/삭제(단순 CRUD, 위 두 개보다 기능 적음).

**TO-BE에서 뭘 만들어야 하는가**: admin에 "담당자 계정 관리" 화면 신설 — 이미 있는 6단계 RBAC(ROLE_ADMIN_1~6)
계정을 role/지자체 소속별로 목록조회+검색하고, 소속 지자체 변경, 계정 삭제, 비밀번호 초기화(이메일/SMS
재발송 또는 강제 재설정), 정/부 담당자 교체 기능을 추가. 지자체 정보(직인이미지/포인트정책) CRUD는
`designated_donation_admin_round`에서 만든 부서관리와 겹치지 않는지 먼저 확인 후, 없는 부분(직인이미지,
연도별 포인트정책)만 보강.

### 2-3. [중간] 탈퇴회원 조회 화면 + 휴면회원 관리자용 목록/개별해제

**왜 필요한가**: 탈퇴 처리 자체(2-1에서 다룸)와는 별개로, "이미 탈퇴한 회원이 몇 명이고 왜 탈퇴했는지"를
집계·조회하는 화면, 그리고 "현재 휴면 상태인 회원이 누구인지 목록으로 보고 특정 회원만 관리자가 수동으로
깨워주는" 화면이 AS-IS에는 있지만 TO-BE엔 없다.

**AS-IS 동작 방식**:
- `SecedeUserManagerController`(`/opmanager/user/secede-user/*`): `list`(탈퇴일 검색조건), `popup/reason-details/{userId}`
  (탈퇴사유 상세) — 읽기전용.
- `SleepUserManagerController`(`/opmanager/user/sleep-user/*`): `list`(최종로그인일 기준 검색), `wakeup`(POST,
  관리자가 특정 회원 1명을 휴면 해제).

**TO-BE 현황**: member `DormancyController`는 `/batch/dormancy`(전체 일괄 휴면전환 실행, 처리건수만 표시,
대상자 목록 없음)와 `/reactivate`(회원 본인이 로그인ID+비번으로 셀프 해제)만 있다. 관리자가 목록에서 검색해
개별 회원을 골라 깨워주는 기능, 탈퇴회원 통계/사유 조회 기능이 없다.

**TO-BE에서 뭘 만들어야 하는가**: admin에 "탈퇴회원 관리"(읽기전용 목록+사유 상세), "휴면회원 관리"(검색
가능한 목록 + 개별 wakeup 버튼) 2개 화면 신설. member 서비스에 상태값(휴면/탈퇴) 기준 목록 조회 API와
관리자용 개별 wakeup API(`POST /api/admin/members/{userId}/wakeup`) 추가 필요.

### 2-4. [중간] 회원등급(포인트등급) 마스터 관리 화면 부재

**왜 필요한가**: `UserDetail.levelId`가 이미 member 도메인에 존재하고, admin `CouponAdminController`에도
`targetUserLevel`로 등급별 쿠폰 타겟팅 필드가 있다 — 즉 "등급"이라는 개념은 이미 코드 여러 곳에서 쓰이고
있는데, 정작 그 등급 자체(이름, 그룹, 등급별 혜택/할인율)를 정의·관리하는 화면이 없다. 현재는 값을 채울
방법이 없는 죽은 FK/파라미터 상태다.

**AS-IS 동작 방식**: `UserLevelManagerController`(`/opmanager/user-level/*`) — 그룹코드별 등급 목록
(`list/{groupCode}`), 등급 생성/수정/삭제(`create/{groupCode}`,`edit/{levelId}`,`delete/{levelId}`),
등급별 첨부파일(뱃지 이미지 등) 삭제.

**TO-BE에서 뭘 만들어야 하는가**: admin에 "회원등급 관리" 화면 신설(등급 그룹/등급 CRUD), member 서비스에
등급 마스터 테이블+API 추가. 우선순위는 중간 — 등급 개념이 실제 할인/혜택 로직에서 얼마나 쓰이는지
(현재는 쿠폰 타겟팅 필드 정도만 확인됨) 재확인 후 실제 활용도에 따라 조정 가능.

### 2-5. [중간] 지자체 명예사용자(시도민증) 관리 화면 부재

**왜 필요한가**: 고향사랑기부 특유의 "명예기부자" 등급(골드/실버/브론즈 등, 지자체별 기준금액 다름) 표시는
storefront `HonorCertificatesView.vue`에 **연도·지자체별 등급 카드 목록**으로 간단히 구현되어 있다(주석에
"AS-IS mypage/honorList.html 재현"이라고 명시됨). 그러나 AS-IS의 "시도민증" 이미지 스와이퍼 UI가 참조하는
지자체별 커스텀 이미지(`/upload/lclgvHnrUserMng/{locgovCd}/{rprsImgNm}`)와 등급 기준을 **지자체 담당자가
직접 설정하는 관리자 화면**은 도입 자체가 안 됐다. 즉 지금 storefront가 보여주는 등급은 하드코딩된 로직으로
산정될 뿐, 지자체별로 다르게 설정할 방법이 없다.

**AS-IS 동작 방식**: `LclgvHnrUserManagerController`(`/opmanager/lclgvHnrUser/*`) — 역할에 따라 분기(SYS/MOIS
관리자는 전체 지자체 목록, 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체 설정폼으로 즉시 리다이렉트),
`lclgvHnrUserMng/form/{lclgvCd}`(시도민증 이미지 업로드+등급기준 설정), `lclgvHnrUserViewHist/list`(+엑셀)
(회원들이 명예사용자 화면을 열람한 이력 통계).

**TO-BE에서 뭘 만들어야 하는가**: admin에 "지자체 명예사용자 관리" 화면 신설(이미지 업로드, 등급기준 입력),
donation 또는 member 서비스에 지자체별 설정 테이블+API. storefront `HonorCertificatesView.vue`는 이 설정값을
읽어와 이미지/기준을 반영하도록 보강. 열람이력 통계는 우선순위를 더 낮게 잡아도 무방(부가 기능).

### 2-6. [정보] 사용자권한그룹 관리 — 신규 구현 불필요(중복 확인됨)

`UserGroupController`(`/opmanager/user-group/*`)의 `create`/`edit` 액션은 내부적으로 `Role role` 파라미터를
받아 `roleService.insertRole(role)`/`updateRole(role)`을 그대로 호출한다 — `UserManagerController`의
`manager-role/*`(현재 admin `/admin/access/*`로 대응 완료)와 완전히 같은 `Role` 엔티티를 다루는 AS-IS의
또 다른 메뉴 진입점일 뿐이다. 별도 구현 불필요.

### 2-7. [낮음/확인필요] SNS 계정 자기관리(연동/해제) 화면

`UserSnsController`/`SnsUserController`(`/sns-user`,`/sns-user-delete`)의 `setup-sns`/`disconnect-sns`가
"이미 로그인된 기존 계정에 나중에 SNS 계정을 연결하거나 해제하는" 마이페이지 자기관리 기능인지, 아니면
로그인 자체의 일부(현재 `ExternalLoginController`로 대체 완료)인지 코드 레벨로 100% 확정하지 못했다.
컨트롤러 타이틀이 "특집페이지"(구 이벤트성 캠페인)로 되어 있어 레거시 캠페인 잔재일 가능성이 있다.
우선순위 낮음 — 실제 착수 전 AS-IS 라이브사이트(마이페이지 내 SNS 연동 메뉴 존재 여부)를 재확인 권장.

## 3. 완전히 구현 확인됨 (요약)

회원가입/로그인/로그아웃, 아이디·비밀번호 찾기/변경, 회원정보수정, 회원탈퇴, 휴면전환 배치+본인 셀프해제,
관심지자체, 배송지 관리, SNS/디지털원패스 로그인 연동(mock-gated), 역할신청+승인큐, 마이페이지 상세 7종,
정책 동의, 개인정보 파기절차, 관리자권한요청(PKI 포함) — 위 §1-A, §1-B 표에 상세 및 근거 라운드/파일 기재.
