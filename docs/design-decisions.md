# 설계 결정·재설계 계획

> 2026-10-06 통합. 아직 살아 있는 설계 문서 7개를 합친 것이다(완료된 1회성 리포트는 `docs/archive/`).
> 결정 자체의 최신 상태는 메모리(`*-decision`, `defer-*`)가 정본이고, 이 문서는 **근거와 선택지**를 담는다.

## 목차

- [1. 계정 모델 설계](#1-계정-모델-설계) — `account-model-design.md`
- [2. 서비스 간 트랜잭션 패턴 (API vs Kafka)](#2-서비스-간-트랜잭션-패턴-api-vs-kafka) — `cross-service-transaction-patterns.md`
- [3. member 이벤트 발행 설계](#3-member-이벤트-발행-설계) — `member-event-publishing-design.md`
- [4. gift 옵션 다형 체계 재설계](#4-gift-옵션-다형-체계-재설계) — `gift-option-redesign-plan.md`
- [5. order 멀티아이템 재설계](#5-order-멀티아이템-재설계) — `order-multiitem-redesign-plan.md`
- [6. B5 특정사업 월별 통계 명세](#6-b5-특정사업-월별-통계-명세) — `b5-designated-month-stats-spec.md`
- [7. Thymeleaf 폐기 맵](#7-thymeleaf-폐기-맵) — `thymeleaf-decommission-map.md`

---

## 1. 계정 모델 설계

> 통합 전 파일: `docs/account-model-design.md`

## 계정 모델 설계 — 개인계정/업무계정 분리

작성일: 2026-09-09
근거: `docs/requirements.md`(RFP SFR-002), `docs/requirements.md` 후반부 ISP 요약(ISP 상세설계 6차),
AS-IS 원본 소스(`C:\workspace\ghlove`)
결정: **한 사람이 개인계정과 업무계정을 각각 갖는다** (ISP의 `ProviderAccount` 별도 애그리거트 방향)

---

### 1. 결정 사항

SFR-002가 정의한 5개 사용자 유형을 **계정 성격**으로 2분류하고, **진입 경로**로 3분할한다.

| 사용자 유형 | 계정 성격 | 진입 경로 | 계정 생성 방식 |
|---|---|---|---|
| 일반회원 | 개인계정 | 대민웹(storefront) | 자가 가입 |
| 재외국민 | 개인계정 | 대민웹(storefront) | 자가 가입 |
| 답례품 제공자 | **업무계정** | 제공자 포털 | 운영자 발급 → 초기 로그인 → 비밀번호 변경 |
| 지자체 담당자 | **업무계정** | 업무·운영웹(admin) | 운영자 발급/승인 |
| 시스템운영자 | **업무계정** | 업무·운영웹(admin) | 운영자 발급 |

동일인이 두 계정을 갖는다. 지자체 공무원이 개인 자격으로 다른 지자체에 기부하려면 **개인계정으로
대민웹에 로그인**한다. 업무계정으로는 기부·장바구니·마이페이지에 접근할 수 없다.

---

### 2. 근거

#### 2-1. RFP SFR-002의 "통합 계정체계"는 이 분리와 충돌하지 않는다

> 통합 계정체계: 일반회원, 재외국민, 지자체 담당자, 답례품 제공자, 시스템운영자 — 모든 사용자
> 유형을 하나의 계정 체계로 통합 관리
> RBAC(역할기반 접근제어) 적용, 지자체 담당자/답례품 제공자의 승인·해지, 권한 부여·회수 절차 표준화

통합의 대상은 **계정 저장소와 인증 체계**이고, 분리의 대상은 **권한과 진입 경로**다. ISP는 이를
Keycloak 단일 IAM(원본 p.481)으로 계정을 통합하면서 프론트엔드는 3개로 나누는 방식으로 실현한다.
"하나의 계정 체계"가 "한 사람당 하나의 계정"을 뜻하지는 않는다.

#### 2-2. ISP는 프론트엔드를 3개로 분리한다

- 원본 p.11 / p.217: API Gateway를 통해 **대민웹 / 관리자(운영)웹 / 답례품제공자(운영)웹** 3개
  프론트엔드가 접근
- 원본 p.189 저장소 구성: `gohyang-fe-www`(대민 기부자), `gohyang-fe-work`(업무웹),
  `gohyang-fe-operation`(운영관리웹)

#### 2-3. 제공자 계정은 회원과 별도 애그리거트다 (결정적 근거)

원본 p.403 애그리거트 할당:

- `MemberRegistered` → **Member** 애그리거트
- `LoginSucceeded`/`LoginFailed` → **Auth** 애그리거트
- `ProviderAccountCreated` / `InitialLoginSucceeded` / `PasswordChanged` → **ProviderAccount** 애그리거트

제공자는 자가 가입하지 않는다. 계정을 발급받아 초기 로그인 후 비밀번호를 변경하는 **업무계정**이다.

#### 2-4. 관리자 접근은 물리적으로 분리된 전제다

원본 p.535(N2SF 보안설계): 회원정보·결제정보·관리자계정정보는 전부 S등급이며, 접근통제에
**"관리자 계정 간 IP 제어·MFA 적용"**, 통신경로에 **"관리자 접속 시 VPN을 통한 암호화 접근"**이
명시된다. 답례품 제공자는 민간 사업자이므로 이 관리자망에 넣을 수 없다 — ISP가 제공자웹을
관리자웹과 별도 프론트로 뽑아둔 이유다.

#### 2-5. 운영자 계정 저장소는 설계상 선택지가 열려 있다

원본 p.173 `op_menu`/`op_role`/`op_acl` 비고: **"운영자/담당자 계정(별도 Admin User 또는 회원마스터)"**.
별도 Admin User 테이블을 두는 현재 구현이 ISP 설계 범위 안에 있다.

#### 2-6. AS-IS 원본의 권한 체계 (1차 자료) — ISP 매트릭스를 대체하는 근거

ISP 원본에는 권한을 직접 정의한 구간이 두 군데 있다(페이지는 PDF 뷰어 기준 — 부록 A 참고).

- **PDF p.61·78·90**: AS-IS 단위기능 전수목록 + **화면ID별 접근권한 매트릭스**(시스템담당자/
  지자체담당자(주·부)/답례품제공자/오프라인담당자)
- **PDF p.363~381**: **메뉴별 접근권한 정의**

두 구간 모두 본문이 **이미지**라 텍스트 추출이 되지 않는다(222MB, `pdftotext`가 페이지번호만
반환). `isp-detailed-design-summary.md`는 전자를 "존재와 축"만 기록했고, 후자는 p.373·379를
인용하되 `op_menu`/`op_role`/`op_acl`의 **컬럼 구성만** 요약해 두 구간의 실제 권한 정의는
**아직 분석되지 않은 상태**다.

ISP 문서는 AS-IS를 분석한 2차 자료이므로, 그 사이 1차 자료인 AS-IS 소스에서 같은 축을 확인했다.

**계정 유형 4종** — `ghlove-common/.../saleson/common/enumeration/AuthorityType.java`:

| 코드 | 이름 |
|---|---|
| `ROLE_USER` | 회원 |
| `ROLE_SELLER` | 판매자 |
| `ROLE_SELLER_MASTER` | 기본 판매 운영자 |
| `ROLE_OPMANAGER` | 관리자 |

→ **판매자(답례품 제공자)가 회원과 나란한 최상위 계정 유형**이다. §1의 결정을 AS-IS 원본이
그대로 뒷받침한다. 또한 판매자에도 **주/부 구분**(`SELLER_MASTER` = 기본 판매 운영자)이 있다.

**관리자 세부 8단계** — `ghlove-common/.../saleson/api/common/enumerated/UserAdminRole.java`:

| 그룹 | 이름 | 역할 코드 | 우리 구현 |
|---|---|---|---|
| SYS | 시스템 관리자 | ROLE_ADMIN_1 / 2 | ✅ `admin01`, `admin02` |
| MOIS | 행안부 관리자 | ROLE_ADMIN_3 / 4 | ✅ `mois01`, `mois02` |
| LOC | 지자체 관리자 | ROLE_ADMIN_5 / 6 | ✅ `operator01`(11230), `locgovtest2`(26350) |
| OFF | 오프라인 관리자 | **ROLE_ADMIN_7 / 8** | ❌ **미구현** |

**AS-IS는 8단계, 우리 구현은 6단계다.** `Manager.java`의 `bankCode` 주석은 "D8 오프라인담당자
(ROLE_ADMIN_7/8) 전용 확장 컬럼"이라며 이미 인지하고 있었으나, `Role.java`·`MenuService.java`·
`ManagerRequestService.java`의 주석은 "AS-IS 실제 6단계"라고 **잘못 적혀 있어 2026-09-09에 정정**했다.
`as-is-feature-audit-member.md` §2-2의 "오프라인 접수 담당자 계정관리 미구현"과 같은 뿌리다.


#### 2-7. AS-IS 화면정의서 대조 — 관리자와 제공자 화면은 완전히 분리되어 있다

`1.AS-IS\2. 문서\doc\고향사랑e음 시스템참조자료\`의 화면정의서 2종에서 화면ID를 전수 추출해
대조한 결과다(이 두 PDF는 100MB 미만이라 `pdftotext`로 화면ID 추출이 가능하다 — 한글 본문은
폰트 서브셋이라 추출되지 않지만 영문·숫자인 화면ID는 나온다).

| 문서 | 화면ID 체계 | 화면 수 | 교차 |
|---|---|---|---|
| `2026_KLID_AD_521_화면정의서_관리자_현행화.pdf` (19MB) | `UI_M*` | **244개** | `UI_S` 0건 |
| `2026_KLID_AD_521_화면정의서_답례품판매관리자_현행화.pdf` (8.1MB) | `UI_S*` | **44개** | `UI_M` 1건(주1) |

주1) 그 1건은 `UI_M01010100`(`login-total`)으로, **두 문서 앞부분 범례 페이지의 양식 샘플**이다
(관리자 문서 16,737줄 중 216번째, 판매자 문서 161번째 줄에 각 1회). 실제 화면 목록에 포함된 것이
아니므로 "로그인 화면을 공유했다"는 근거로 쓸 수 없다. `login-total`은 대민 로그인 화면의 CSS
클래스이며 우리 `LoginView.vue`도 같은 클래스를 쓴다.

**AS-IS는 화면ID 접두사(M/S)부터 산출물 문서까지 관리자와 답례품제공자를 완전히 갈라놨다.**
§2-2(ISP 프론트 3분할), §2-3(ProviderAccount 별도 애그리거트), §2-4(N2SF 관리자망 VPN·IP제어)와
같은 방향이며, 진입 경로 분리 결정의 가장 구체적인 근거다.

---

### 3. 현재 구현 실태

조사 결과, **업무계정 스키마는 이미 대부분 존재하고 인증 배선만 어긋나 있다.**

#### 3-1. 시스템운영자·지자체 담당자 — 이미 업무계정 (✅ 방향 일치)

`admin.OP_MANAGER`가 `member.OP_USER`와 완전히 분리된 계정 테이블로 존재한다.

- 공통 자격증명 컬럼: `LOGIN_ID`, `PASSWORD`, `USER_NAME`, `EMAIL`, `STATUS_CODE`,
  `LOGIN_FAIL_COUNT`, `LOGIN_TRY_DATE`
- 업무계정 전용 컬럼: `AUTHORITY`(ROLE_ADMIN_1~6), `LOCGOV_CODE`(소속 지자체),
  `CERT_SUBJECT_DN`(PKI 인증서 DN), `EMP_ID`(직원번호), `BANK_CODE`
- 로그인: `ManagerAuthController` — 아이디/비밀번호 + 이메일 인증코드 + PKI 인증서 로그인
- 현재 계정 6개: `admin01`(ROLE_ADMIN_1) ~ `locgovtest2`(ROLE_ADMIN_6)

#### 3-2. 답례품 제공자 — 스키마는 업무계정, 인증은 대민계정 재사용 (❌ 어긋남)

`gift.OP_SELLER`에 `LOGIN_ID`, `PASSWORD`, `USER_NAME`, `EMAIL`, `STATUS_CODE`가 **이미 있다.**
그런데 실제 인증은 이 컬럼을 쓰지 않는다.

- `Seller.memberUserId` 주석: *"LOGIN_ID/PASSWORD는 실제로 검증하는 로직이 없는 죽은 컬럼이라
  대신 이 컬럼으로 본인 확인을 한다"*
- `SellerPortalController.dashboard()`가 `jwtVerifier.currentUserId(request)`로 **대민 로그인
  세션**을 읽고 `sellerRepository.findByMemberUserId(...)`로 판매자를 찾는다
- DB 실태: `login_id`는 전부 채워져 있으나 `password`는 **전 행 NULL**, `member_user_id`는
  9001번 한 건만 연결됨

즉 제공자가 대민웹 계정(ROLE_PROVIDER)으로 로그인해 제공자 포털에 들어가는 상태다. 이번 결정에
정면으로 어긋나는 유일한 지점이다.

#### 3-3. 대민웹에 관리 기능이 섞여 있다 (❌ 정리 필요)

- `member`의 `RoleRequestService.REQUESTABLE_ROLES = ["ROLE_LOCALGOV", "ROLE_PROVIDER"]` —
  **대민 계정이 업무 역할을 신청**하는 경로
- storefront 마이페이지에 `RoleRequestView`(역할 신청)와 `RoleQueueView`(**승인 대기열**)가 있다.
  승인 큐는 ROLE_ADMIN 게이트로 403이 나지만, 화면 자체가 고객 메뉴에 남아 있다
- `ROLE_LOCALGOV`는 부여·회수 로직만 있고 **그 역할로 들어갈 화면이 없다** — 죽은 역할이다
  (admin 콘솔은 `OP_MANAGER` 계정을 쓰므로 이 역할과 무관)
- 현재 보유 현황: `ROLE_LOCALGOV` 2건(`e2etest01`, `localgovtest`), `ROLE_PROVIDER` 1건
  (`providertest`) — 전부 테스트 계정이라 정리 부담 없음

#### 3-4. 대민웹은 역할 분기가 없다

`AppHeader.vue`에 역할 기반 메뉴 분기가 전혀 없다. 개인계정만 대민웹에 들어온다는 전제가 서면
분기 자체가 불필요해지므로, 이 상태는 결정 이후에도 그대로 두면 된다.

#### 3-5. 기능별 접근권한 매트릭스는 이미 구현되어 있다 (✅)

ISP p.78·p.90의 화면ID별 접근권한 매트릭스에 해당하는 구조가 admin에 있다.

- `OP_MENU`(메뉴 121개) × `OP_MENU_RIGHT`(메뉴-권한 매핑) 테이블
- `MenuService`가 요청 URI를 `OP_MENU.menuUrl`과 매칭해 권한 확인
- `UNRESTRICTED_ROLES`(ROLE_ADMIN_1~4, 시스템·행안부)는 AS-IS `ROLE_SUPERVISOR`와 동일하게
  매트릭스를 거치지 않고 항상 통과
- `LOCGOV_SCOPED_ROLES`(ROLE_ADMIN_5~6, 지자체)만 실제 `OP_MENU_RIGHT` 매핑 확인
- AS-IS와의 의도적 차이: AS-IS는 "매칭 메뉴 없으면 예외"지만 여기선 fail-open(통과)

현재 매핑 데이터:

| authority | 허용 메뉴 수 |
|---|---|
| ROLE_ADMIN_5 | 17 |
| ROLE_ADMIN_6 | 17 |
| `ROLE_ADMIN`(구형 값) | 2 |

전체 121개 메뉴 중 지자체 담당자에게 17개가 열려 있다. **구형 값 `ROLE_ADMIN` 2건은 잔재**로,
어느 역할에도 매칭되지 않으므로 정리 대상이다.

---

### 4. TO-BE 계정 모델

```
[개인계정]                          [업무계정]
member.OP_USER                      admin.OP_MANAGER          gift.OP_SELLER
  · 자가 가입                          · 운영자 발급               · 운영자 발급
  · ROLE_USER 단일                     · AUTHORITY               · 자체 LOGIN_ID/PASSWORD
  · 대민웹 로그인                        ROLE_ADMIN_1~6(~8)        · 주/부 구분(AS-IS
       │                                · 업무·운영웹 로그인          SELLER_MASTER)
       │                                     │                        │
       ▼                                     ▼                        ▼
  storefront(5173)                     admin(8086)              제공자 포털
  기부/장바구니/마이페이지                지자체·운영 업무            상품·주문·정산
```

**세 진입 경로는 서로의 세션을 신뢰하지 않는다.** 대민웹 JWT로 제공자 포털에 들어갈 수 없고,
그 역도 마찬가지다.

#### 동일인 연결

`OP_SELLER.MEMBER_USER_ID`는 **인증 용도로는 폐기**하되 컬럼은 남긴다 — 정산 통지나 동일인
식별 같은 업무 참조에는 쓸 수 있고, 지우면 되돌리기 어렵기 때문이다. 인증에 쓰지 않는다는 것을
주석으로 명확히 한다.

---

### 5. 이행 작업

우선순위 순. 각 항목은 독립적으로 적용 가능하다.

| # | 작업 | 대상 | 성격 |
|---|---|---|---|
| 1 | `OP_SELLER.PASSWORD`를 실제 자격증명으로 살리고 제공자 포털 로그인 화면 신설(아이디/비밀번호 + 초기 비밀번호 변경 강제) | gift | 신규 |
| 2 | `SellerPortalController`를 제공자 세션 기반으로 전환, `findByMemberUserId` 인증 제거 | gift | 전환 |
| 3 | 운영자가 제공자 계정을 발급/정지/비밀번호 초기화하는 화면 | admin | 신규 |
| 4 | `ProviderAccountCreated`/`InitialLoginSucceeded`/`PasswordChanged` 이벤트 발행(ISP p.403) | gift | 신규 |
| 5 | `REQUESTABLE_ROLES`에서 `ROLE_PROVIDER`·`ROLE_LOCALGOV` 제거 — 업무 역할은 신청이 아니라 발급 | member | 제거 |
| 6 | 마이페이지에서 `RoleRequestView`/`RoleQueueView` 및 라우트 제거 | storefront | 제거 |
| 7 | 기존 `ROLE_PROVIDER`/`ROLE_LOCALGOV` 보유 계정 정리(회수) 및 `op_user_role` 데이터 정비 | member | 데이터 |
| 8 | 제공자 계정의 주/부 구분(AS-IS `SELLER`/`SELLER_MASTER`) 도입 여부 결정 후 반영 | gift | 설계 |
| 9 | `OP_MENU_RIGHT`의 구형 `ROLE_ADMIN` 매핑 2건 정리 | admin | 데이터 |
| 10 | 오프라인담당자(ROLE_ADMIN_7/8) 도입 — `as-is-feature-audit-member.md` §2-2의 오프라인 담당자 계정관리와 함께 | admin | 신규 |
| 11 | 지자체 담당자 업무 화면을 admin 안에 둘지 별도 업무웹으로 뺄지 결정 후 정리 | admin | 설계 |

5·6·7번은 **기존 기능을 없애는 작업**이다. 현재 그 경로로 발급된 역할은 테스트 계정 3건뿐이라
정리 부담은 없다(§3-3).

---

### 6. 남은 결정 사항

1. **오프라인담당자(ROLE_ADMIN_7/8) 도입 여부** — AS-IS에는 있고 우리에겐 없다. 오프라인 기부
   접수 담당자가 실제 운영에 필요한 역할이라면 8단계로 맞춰야 한다(이행 작업 #10).
2. **업무웹과 운영관리웹의 분리 여부** — ISP는 `fe-work`(지자체 담당자)와 `fe-operation`(시스템
   운영자)을 별도 프론트로 설계한다. 현재는 admin 하나에 `AUTHORITY`로 메뉴를 가르는 구조다.
   ISP를 따르려면 프론트를 하나 더 떼야 하는데, 이는 아키텍처 스타일 문제로 `fix-report`의
   잔여과제 #13("지자체 전용 대시보드 별도앱 분리")과 같은 항목이다.
3. **제공자 포털의 물리적 위치** — **잠정 결정: 현행 유지(gift 서비스 안 `/seller/*`), 최종 결정은
   운영데이터 적재 또는 개발DB 구축 이후로 미룬다.**

   *판단 근거(2026-09-09)*: "관리자 콘솔과 합칠 것인가"는 **닫힌 문제**다 — §2-7의 화면ID 대조
   (관리자 `UI_M` 244개 / 제공자 `UI_S` 44개, 교차 0건), ISP의 프론트 3분할, N2SF의 관리자망
   VPN·IP제어(민간 사업자에게 부여 불가)가 모두 분리를 가리킨다. 게다가 **현재 구현도 이미
   분리 상태**다(admin 8086 / 제공자 포털은 gift 8084, 별도 프로세스·별도 세션).

   남은 것은 "**별도 프론트엔드 앱으로 뺄 것인가**"인데, 지금은 이르다고 본다.
   - 제공자 화면은 답례품·주문·정산 데이터에 밀착돼 있어 앱을 떼면 API 왕복만 늘어난다
   - admin도 같은 스타일(Thymeleaf + 도메인 서비스 내장)이라 일관된다
   - ISP의 3-프론트 분리는 배포 단위 관점이고, "분리" 요건 자체는 이미 충족된다

   **진짜 문제는 위치가 아니라 규모다** — AS-IS 44개 화면 대비 현재 제공자 포털은
   `seller-dashboard.html` **1개**뿐이다. 합칠지 나눌지를 정할 단계가 아니라 채울 단계다.

   *재검토 시점과 기준*: 운영데이터를 적재하거나 개발DB가 구축돼 실제 제공자 데이터·화면 규모가
   드러난 뒤에 다시 판단한다. 그때 아래 중 하나라도 해당하면 별도 앱 분리를 검토한다.
   - 제공자 화면이 20~30개를 넘어 gift 서비스의 배포 주기가 답례품 API와 어긋나기 시작할 때
   - 제공자에게만 필요한 인증·보안 정책(전용 도메인, IP 화이트리스트 등)이 생길 때
   - 제공자 포털 트래픽이 대민 답례품 조회와 상호 간섭할 때

   *잔여 리스크*: gift 서비스가 대민 조회 API와 제공자 업무 화면을 겸한다. 다만 이는 Kong 라우팅과
   네트워크 정책으로 통제할 문제지 앱을 쪼개서 풀 문제는 아니다.
4. **Keycloak 도입 여부** — ISP는 중앙 IAM으로 세 계정 체계를 통합 관리하는 전제다(p.481).
   현재는 서비스별 자체 JWT다. 로컬 개발 단계에서는 합리적 생략이나, 이관 시 재검토 대상이다.

---

### 부록 A. ISP 원본의 권한 관련 페이지 (미분석 구간 포함)

- 파일: `C:\Users\ghlove008\Desktop\고향사랑e음\0. 문서\1.ISP 분석자료\[고향사랑e음]25년 클라우드 네이티브 전환 상세설계(6차).pdf` (222MB)
- **`isp-detailed-design-summary.md`의 "원본 p.NNN" 표기는 PDF 뷰어 페이지 기준이다.**
  문서에 인쇄된 페이지 번호와는 다르고, 장마다 번호가 리셋되어 **일정한 오프셋이 없다**
  (PDF p.61 → 인쇄 p.42, PDF p.373 → 인쇄 p.181).

| PDF 페이지 | 인쇄 페이지 | 내용 | 요약본 반영 상태 |
|---|---|---|---|
| 61 · 78 · 90 | 42 · 59 · 71 | AS-IS 단위기능 전수목록 + **화면ID별 접근권한 매트릭스**(시스템담당자/지자체담당자(주·부)/답례품제공자/오프라인담당자) | §3.3에 **존재와 축만** 기록, 매트릭스 내용 미요약 |
| **363~381** | **171~189** | **메뉴별 접근권한 정의** | p.373·379를 인용했으나 `op_menu`/`op_role`/`op_acl`의 **컬럼 구성만** 요약. 권한 정의 내용 자체는 **미분석** |

두 구간 모두 **본문이 이미지**라 `pdftotext`로는 페이지 번호만 추출된다. PDF p.61에는
`2025_KLIS_AD_508__1.2.xlsx`라는 임베드 개체의 파일명 라벨이 보이지만 실제 파일은 PDF 안에
들어있지 않고(ZIP 시그니처 스캔 결과 없음), 그 엑셀 원본도 이 PC에서 찾지 못했다.

읽으려면 사람이 뷰어로 직접 열거나, PDF 렌더링 도구(`pdftoppm`·`qpdf` 등 — 현재 개발 PC에 없음)를
설치해 해당 페이지를 이미지로 뽑아야 한다.

§2-6은 이 매트릭스를 못 본 상태에서 AS-IS 소스 1차 자료로 같은 축을 확인한 결과다. 원본을 실제로
열어보면 **화면ID·메뉴 단위의 세부 권한**을 얻을 수 있고, 우리 `OP_MENU_RIGHT` 매핑(현재
ROLE_ADMIN_5·6에 각 17개)과 1:1 대조가 가능하다 — 이행 작업 #9·#10을 진행할 때 확인할 가치가 있다.

#### 함께 참고할 수 있는 자료 — 화면정의서 (분석 완료, §2-7)

`C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\2. 문서\doc\고향사랑e음 시스템참조자료\`에
화면정의서가 대상별로 나뉘어 있다 — `..._관리자_현행화.pdf`(19MB), `..._답례품판매관리자_현행화.pdf`
(8.1MB), `..._사용자.pdf`. 이 파일들은 100MB 미만이라 `pdftotext`로 **화면ID 추출이 가능**하고,
그 결과를 §2-7에 정리했다(관리자 `UI_M` 244개 / 제공자 `UI_S` 44개, 교차 0건).

단 **한글 본문은 폰트 서브셋이라 추출되지 않는다** — 화면 이름·설명·권한 표기가 필요하면 사람이
뷰어로 열어야 한다. ISP 원본의 두 미분석 구간(위 표)과 이 화면정의서를 함께 보면 화면ID 단위
권한을 우리 `OP_MENU_RIGHT` 매핑과 1:1 대조할 수 있다.

---

## 2. 서비스 간 트랜잭션 패턴 (API vs Kafka)

> 통합 전 파일: `docs/cross-service-transaction-patterns.md`

## 크로스서비스 데이터 일관성 패턴 — 여러 서비스에 걸친 쓰기를 어떻게 안전하게 하나

> **읽는 사람:** ghlove-msa에서 **한 요청이 여러 서비스(member/donation/point/gift/order)의 데이터를 바꾸는** 기능을 만들거나 리뷰하는 개발자.
> **한 줄 요지:** 단일 DB의 `@Transactional`은 서비스 경계를 못 넘는다. **연산의 성격(삭제냐, 더하기냐, 되돌려야 하냐)에 따라 다른 도구**를 골라 써라.

작성: 2026-09-21 · 근거 코드는 본문에 `파일:라인`으로 링크.

---

### TL;DR — 연산 성격별 3패턴

| 연산 성격 | 예시 | 멱등한가? | 쓰는 도구 | 대표 코드 |
|---|---|---|---|---|
| **삭제(0으로/제거)** | 회원탈퇴 | 자동 멱등(0을 또 0으로 해도 0) | **크로스 호출을 앞에 배치 + 멱등 재시도** | `MemberService.withdraw()` |
| **더하기(입금/적립)** | 포인트 적립 | 그냥 두면 멱등 아님(더하기+더하기=2배) | **멱등 키**(중복 방지) | `PointService.creditForDonation()` |
| **차감·예약(되돌려야 함)** | 주문 결제(재고차감+포인트차감) | 아님 + 다단계 | **SAGA + 보상 트랜잭션** | order/gift/point `OrderSaga*` |

**핵심 원칙:** "무엇을 하느냐"를 먼저 본다. 삭제는 재시도로, 더하기는 멱등키로, 되돌려야 하는 다단계는 SAGA 보상으로.

---

### 1. 왜 어려운가 — `@Transactional`은 서비스 경계를 못 넘는다

DB 트랜잭션은 **"전부 아니면 전무(All or Nothing)"**를 보장한다. 하지만 **하나의 데이터베이스 안에서만** 작동한다.

- 같은 DB(같은 은행) 안의 여러 작업 → 트랜잭션으로 묶여 안전.
- 다른 서비스(다른 은행)의 작업 → **하나의 트랜잭션으로 못 묶는다.** A 서비스가 B 서비스에게 "너도 롤백해"를 강제할 수 없다.

AS-IS(레거시 SalesOn 모놀리스)는 모든 데이터가 **하나의 CUBRID DB**에 있어서 이 고민이 없었다(탈퇴 도중 어디서 터지든 전부 롤백). MSA로 쪼개면서 이 "공짜 원자성"을 잃었고, 그래서 아래 패턴들이 필요해졌다.

> **HTTP 호출(RestClient)이든 Kafka 이벤트든, 다른 서비스에서 이미 커밋된 변경은 우리 트랜잭션 롤백으로 되돌아오지 않는다.** 이 문장이 이 문서 전체의 출발점이다.

---

### 2. 패턴 1 — 삭제: 크로스 호출을 앞에 배치 + 멱등 재시도

**대표 사례: 회원탈퇴** [`MemberService.withdraw()`](../member/src/main/java/com/ghlove/member/service/MemberService.java)

탈퇴는 4곳의 데이터를 건드린다: member(개인정보/CI/권한), point(포인트 소멸), gift(관심답례품 삭제), donation(관심지자체 삭제). 이 중 point/gift/donation은 HTTP 호출이라 우리 트랜잭션에 못 묶인다.

#### 실행 순서 (이 순서가 안전장치다)

```
① 비밀번호 확인                                    (member)
② 연간한도 스냅샷                                   (member 내부)
─── 남의 은행(HTTP) 구간 ── 개인정보 삭제보다 "먼저" ───
③ point   : 포인트 소멸
④ gift    : 관심답례품 삭제
⑤ donation: 관심지자체 삭제
─── 내 은행(@Transactional) 구간 ───
⑥ CI 백업 → ⑦ 개인정보 NULL·status=WITHDRAWN → ⑧ 상세 NULL → ⑨ 권한 삭제 → ⑩ 이력
```

#### 안전장치 3종

1. **순서 배치** — 크로스 호출(③④⑤)을 **개인정보 삭제(⑥~) 앞에** 둔다. 남의 은행에서 실패하면 개인정보는 아직 안 건드린 상태라 **깨끗하게 재시도**할 수 있다. ([주석 근거](../member/src/main/java/com/ghlove/member/service/MemberService.java))
2. **`@Transactional`(내 은행 묶기)** — member 내부(⑥~⑩)는 여전히 "전부 아니면 전무". 반쪽짜리 member 데이터는 안 생긴다.
3. **멱등성** — 크로스 작업이 전부 전량 삭제/소멸(`deleteByUserId`, 잔여 전량 소멸)이라 **몇 번 실행해도 결과가 같다.** 재시도가 안전하게 완결된다.

#### 부분 실패 시나리오

| 어디서 터지나 | 결과 | 수습 |
|---|---|---|
| ③ point 소멸(맨 앞) | 개인정보 안 건드림, 회원 ACTIVE | 재시도하면 흔적 없이 처음부터 |
| ④/⑤ 중간 크로스 | 포인트만 소멸됨 + 회원 ACTIVE (중간상태) | 재시도 → 멱등이라 남은 것만 마저 처리 |
| ⑦ member 저장 중 | 크로스 3개는 됨, member는 @Transactional로 롤백 | 재시도 → 멱등이라 안전 완결 |

**한계:** 사용자가 재시도를 안 하고 포기하면 "포인트만 소멸 + 회원 살아있는" 상태가 남을 수 있다. 다만 (a) 최종 상태는 재시도 한 번이면 수렴하고, (b) 소멸된 포인트를 "복원"하는 건 오히려 위험하므로 되돌리지 않는 게 맞다. 치명적 데이터 파괴가 아니라 고객센터로 복구 가능한 수준.

---

### 3. 패턴 2 — 더하기: 멱등 키(idempotency key)

**대표 사례: 포인트 적립** [`PointService.creditForDonation()`](../point/src/main/java/com/ghlove/point/service/PointService.java)

더하기(입금/적립)는 **멱등이 아니다.** "1만원 더해"를 두 번 하면 2만원이 된다. 재시도가 곧 **중복 입금**이 된다. Kafka는 같은 이벤트를 두 번 배달할 수 있으므로(at-least-once) 이 문제는 실제로 발생한다.

#### 해결: 거래마다 고유 번호를 붙이고 "이미 했니?"를 먼저 묻는다

```java
public void creditForDonation(DonationCompletedEvent event) {
    if (pointLedgerRepository.existsByRefKeyAndTxnType(event.cntrSn(), TXN_EARN)) {
        log.info("Donation {} already credited - skipping duplicate event");
        return;                          // ← 이미 적립했으면 무시
    }
    // ... 여기서부터 실제 적립(더하기)
}
```

- `event.cntrSn()`(기부 일련번호) = **멱등 키**.
- `existsByRefKeyAndTxnType(cntrSn, EARN)` = "이 기부로 이미 포인트 줬나?" 확인.
- 이미 줬으면 no-op. → 이벤트가 두 번 와도 **딱 한 번만** 적립된다.

내부 관리자 재적립 API도 같은 방식으로 멱등하다: [`PointApiController` `/api/admin/credit-for-donation`](../point/src/main/java/com/ghlove/point/web/PointApiController.java) — 호출 전에 `existsByRefKeyAndTxnType`로 중복을 막는다.

> **규칙:** 다른 서비스의 "더하기/차감" 원장을 건드리는 요청에는 **반드시 멱등 키(cntrSn, orderId 등 도메인 고유번호)를 실어 보내고, 받는 쪽이 "이미 처리함"을 확인**하게 한다.

---

### 4. 패턴 3 — 차감·다단계: SAGA + 보상 트랜잭션

**대표 사례: 주문 결제** (order 오케스트레이터 + gift 재고 + point 포인트)

주문은 두 단계를 거친다: **① gift 재고 예약(차감) → ② point 포인트 차감.** 둘 다 "되돌려야 하는" 연산이고 여러 서비스에 걸쳐 있어, 하나의 트랜잭션으로 못 묶는다. → SAGA로 처리한다.

**SAGA란:** 긴 거래를 하나의 트랜잭션 대신 **각 단계는 자기 DB에 바로 커밋**하고, **중간에 실패하면 앞 단계를 거꾸로 되돌리는(보상)** 방식.

#### 성공 흐름

```
order  : 주문 생성(PENDING) ──[ORDER_CREATED]──▶
gift   : 재고 예약 성공        ──[STOCK_RESERVED]──▶ order
point  : 포인트 차감 성공      ──[POINT_DEDUCTED]──▶ order
order  : 두 결과 다 성공 → 주문 확정(CONFIRMED)
```
[`OrderService.resolveIfReady()`](../order/src/main/java/com/ghlove/order/service/OrderService.java) — `RESERVED && DEDUCTED`면 확정.

#### 실패 흐름 (핵심 — 보상)

포인트가 부족해 ②가 실패했는데 ①재고는 이미 예약(차감)된 상황:

```
gift   : 재고 예약 성공        ──[STOCK_RESERVED]──▶ order   ← 재고 이미 뺐음
point  : 포인트 차감 실패      ──[POINT_DEDUCT_FAILED]──▶ order
order  : 하나 실패 → 주문 취소(CANCELLED) ──[ORDER_CANCELLED]──▶
gift   : restoreStockForOrder()  ← 예약한 재고 도로 복구 (보상!)
point  : (차감분 있으면) 환불     ← 보상
```

- [`OrderService.resolveIfReady()`](../order/src/main/java/com/ghlove/order/service/OrderService.java) — 하나라도 실패면 `CANCELLED` + `publishCancelled`.
- [`gift/OrderSagaListener`](../gift/src/main/java/com/ghlove/gift/event/OrderSagaListener.java) — `ORDER_CANCELLED` 수신 → `restoreStockForOrder()` = **재고 복구(보상)**.

**보상 트랜잭션 = 롤백을 못 하니, "반대 작업(재고 복구/포인트 환불)"을 명시적으로 실행**해서 되돌리는 것.

#### 오케스트레이터 패턴

여기서 **order 서비스가 "지휘자"**다. 각 단계 결과를 `Order` 행에 모으고([`onStockReserved`/`onPointDeducted` 등](../order/src/main/java/com/ghlove/order/service/OrderService.java)), 둘 다 모이면 판정 → 성공이면 확정, 실패면 보상 지시(CANCELLED). gift와 point는 서로를 모르고, 가운데서 order가 취합·지휘한다.

사용자 요청에 의한 **확정 주문 취소**도 같은 메커니즘을 재사용한다: order가 `ORDER_CANCELLED`를 발행하면 gift/point가 각자 재고·포인트를 보상한다([`OrderService` 주문취소](../order/src/main/java/com/ghlove/order/service/OrderService.java)).

---

### 5. 곁들임 — API 동기 호출 vs Kafka 이벤트, 언제 뭘 쓰나

크로스서비스 통신 수단(동기 API vs 비동기 Kafka)도 **상호작용 성격**으로 고른다(서비스 종류가 아님):

1. **조회(값 읽기)?** → 무조건 **API 동기**. (마이페이지 잔액/목록 등 - `member/service/PointClient·DonationClient·GiftClient`)
2. **쓰기인데 호출자가 결과를 즉시 알고 실패 시 자기 작업을 중단해야?** → **API 동기**. (탈퇴 시 포인트소멸·관심삭제 - 실패하면 탈퇴 중단)
3. **"이미 일어난 사실"을 전파하고 소비 측이 늦게 처리해도?** → **Kafka**. (기부완료→적립 - point 장애가 기부를 막으면 안 됨)
4. **여러 서비스 거치는 다단계 + 실패 시 보상?** → **Kafka SAGA**. (주문 결제)

---

### 6. 의사결정 기록 — 왜 탈퇴엔 SAGA를 안 썼나

탈퇴는 전부 **삭제** 연산이라 **되돌릴 게 없다.** 삭제한 것을 "보상"으로 되살리는 것은 오히려 위험하다(지운 개인정보를 되살린다?). 삭제는 자동으로 멱등이라 **순서 배치 + 멱등 재시도**로 충분히 안전하다. 반대로 주문은 재고·포인트를 **빼는(되돌려야 하는)** 연산이라 SAGA 보상이 반드시 필요했다.

> SAGA는 "복잡한 게 항상 더 안전"해서 쓰는 게 아니라, **되돌려야 하는 연산이 여러 서비스에 걸쳐 있을 때** 쓰는 것이다. 삭제·멱등 연산에 SAGA를 얹으면 복잡도만 늘고 이득이 없다.

---

### 7. 새 크로스서비스 기능을 만들 때 체크리스트

- [ ] 이 요청이 바꾸는 데이터가 **몇 개 서비스**에 걸쳐 있나? 한 서비스면 그냥 `@Transactional`로 끝.
- [ ] 각 크로스 작업의 **연산 성격**은? (삭제 / 더하기 / 차감·예약)
- [ ] **삭제**면: 크로스 호출을 로컬 파괴적 변경보다 **앞에** 두고, 각 작업을 **멱등**(전량 기준)으로 만들었나?
- [ ] **더하기/차감**이면: 도메인 **고유번호(멱등 키)**를 실어 보내고, 받는 쪽이 "이미 처리함"을 확인하나?
- [ ] **되돌려야 하는 다단계**면: 각 단계의 **보상(반대 작업)**을 정의했고, 오케스트레이터가 결과를 취합해 실패 시 보상을 지시하나?
- [ ] 통신 수단(§5): 즉시 결과가 필요하면 API, 확정된 사실 전파면 Kafka.
- [ ] 부분 실패 시 남는 "중간 상태"를 적어보고, 그게 **재시도/보상으로 수렴**하는지 확인했나?

---

관련 문서/메모: 이 문서는 세션 대화(2026-09-21, 회원탈퇴 크로스서비스 처리 검토)에서 정리됨. 통신 수단 판단 기준은 팀 내부 메모 `api-vs-kafka-decision-criteria`와 동일 원칙.

---

## 3. member 이벤트 발행 설계

> 통합 전 파일: `docs/member-event-publishing-design.md`

## member 이벤트 발행 설계 (소비처 포함)

작성 2026-09-22. member 서비스는 현재 Kafka 흔적이 전무 — 발행/소비 모두 없다. 이 문서는
**무엇을 발행하고 누가 소비할지**를 ISP 이벤트스토밍 + 기존 MSA 패턴에 근거해 설계한다.
구현은 §7 결정 확정 후 착수(임의 착수 금지, RFP/ISP 신규는 사용자 확인 필수).

### 1. 근거
- **ISP p.403 이벤트스토밍(회원/인증 도메인)** — `MemberRegistered`(회원가입완료)→Member 애그리거트,
  `IdentityVerified`/`IdentityVerificationFailed`→Auth, `LoginSucceeded`/`LoginFailed`→Auth(정상 계정만
  성공). 제공자계정은 별도 애그리거트로 `ProviderAccountCreated`/`InitialLoginSucceeded`/`PasswordChanged`.
- **ISP p.439** — "API Gateway + 이벤트 기반 비동기통신". **RFP** 구현원칙 — 서비스간 통신은 Kafka
  이벤트 원칙, 동기 REST 최소화.
- **기존 MSA 패턴(이미 구축됨)** — donation/gift/order/point가 발행, admin이 소비(consume-only).
  - 발행: `KafkaTemplate<String,Object>` + `JsonSerializer`, aggregate별 lifecycle 토픽 1개, `eventType`는
    payload가 아닌 **Kafka 헤더**, key로 파티션 순서보장. 예: [DonationEventPublisher](../donation/src/main/java/com/ghlove/donation/event/DonationEventPublisher.java)(topic `donation.lifecycle`, key=cntrSn).
  - 소비: admin은 `StringDeserializer`로 raw JSON을 받아 수동 파싱(서비스 경계 넘어 Java 이벤트 클래스
    공유 안 함), `*StatsListener`→`StatsService`가 **로컬 통계 ReadModel** 적재. group-id `admin-service`.

### 2. 현황 (member가 이미 가진 것)
member는 발행만 없을 뿐, 아래를 **이미 로컬 DB에 기록**한다:
- `LoginLog` — 로그인 성공("Y")/실패("N", 사유메모, IP). MemberService의 로그인 처리 4개 지점에서 기록.
- `UserActionLog` — 회원 액션 감사(AS-IS OP_USER_ACTION_LOG). `AuditLogService.recordAction`.
- `UserChangeLog` — 회원정보 변경 이력.
admin은 회원 관련 소비 리스너가 아직 없다(donation/order/point/gift stats만 소비).
AS-IS `PointServiceImpl:691`에 **"회원가입 포인트"** 적립 로직 존재 → MSA/활성 여부 확인 대상(§4-3).

### 3. 발행 이벤트 설계
토픽 `member.lifecycle` 하나에 모든 회원/인증 확정사실을 싣는다(기존 패턴 동일). key는 순서보장
단위, `eventType` 헤더로 구분, payload는 JSON record.

| eventType | 발행 시점 | key | payload(핵심 필드) | ISP 근거 |
|---|---|---|---|---|
| `MEMBER_JOINED` | 회원가입 완료 | userId | userId, loginId, loginPathCode(가입경로), sbscrbSeCode, joinedAt | MemberRegistered |
| `LOGIN_SUCCEEDED` | 로그인 성공 | userId | userId, loginId, remoteAddr, at | LoginSucceeded |
| `LOGIN_FAILED` | 로그인 실패 | loginId | loginId, remoteAddr, reason, at | LoginFailed (미존재 계정일 수 있어 key=loginId) |
| `MEMBER_WITHDRAWN` | 탈퇴(일반+연동해지) | userId | userId, leaveCode, at | Member lifecycle(통계/감사) |
| `MEMBER_DORMANT` | 휴면 전환(SFR-002 배치) | userId | userId, at | Member lifecycle |

**보류(발행지점 부재/외부연계)**:
- `IDENTITY_VERIFIED`/`IDENTITY_VERIFICATION_FAILED` — MSA는 본인인증(CI/DI) 외부연계가 미구현(dev
  bypass)이라 발행할 확정사실 지점이 실질 부재. 인증 게이트웨이 개방 시. → [[member-service-deferred-items]]
- `ProviderAccountCreated`/`InitialLoginSucceeded`/`PasswordChanged` — 제공자계정은 별도 애그리거트.
  제공자 포털 분리와 함께. → [[provider-portal-split-deferred]]

### 4. 소비처 설계
#### 4-1. admin 회원 통계 — `MemberStatsListener` → `StatsService` (ReadModel)
- 집계: 일자별·가입경로별 가입수, 로그인 성공/실패 카운트, 탈퇴/휴면 수 → 운영관리 대시보드.
- 기존 `DonationStatsListener` 패턴 그대로(raw JSON 파싱, group `admin-service`, 로컬 ReadModel 적재).
- **주의**: 이는 신규 ReadModel → [[defer-readmodels-pending-da-design]] 규칙상 **DA 설계 후 구축(보류)**.

#### 4-2. 로그인이력/감사 조회 — API vs 이벤트
member가 이미 LoginLog/UserActionLog를 로컬 보관하므로, 운영관리에서 감사 조회 방법 2안:
- **(A) 동기 API** — admin이 member 감사조회 API를 호출(조회·즉시결과 → [[api-vs-kafka-decision-criteria]]상 API 적합). 이벤트 불필요, 원본이 단일 진실.
- **(B) 이벤트 축적** — member 발행 → admin 자체 감사 ReadModel 축적(대량 집계·독립 조회 유리, 그러나 이중 저장).
- **권고**: 감사 **이력 조회 = (A) API**, 통계 **집계 = (B) 이벤트(§4-1)**. → 감사 전용 이벤트 추가발행은 만들지 않음.

#### 4-3. point 회원가입 축하 포인트 — [2026-09-23 구현 완료 · 설정 0(AS-IS 동일)]
**사용자 지시로 AS-IS와 동일하게 기능은 구현하고 설정값은 0(비활성)으로 둠** ([[as-is-parity-includes-disabled-state]]).
- point `MemberEventListener`(topic member.lifecycle, group point-service) → MEMBER_JOINED → `PointService.creditForSignup(userId)`.
- `creditForSignup`: `SYSTEM_CONFIG/POINT_JOIN`(=AS-IS OP_CONFIG.POINT_JOIN)만큼 EARN 적립, reason "회원가입 포인트",
  REF_KEY `JOIN-<userId>`로 회원당 1회 멱등, locgov 미연결(AS-IS DEFAULT_POINT_CODE), 만료는 openLot 공통. **0이면 미적립**(AS-IS `point!=0` 가드).
- 설정 시드: `point.OP_COMMON_CODE` SYSTEM_CONFIG/POINT_JOIN='0' (`database/ddl/migration-point-signup-config.sql`). 컴파일 OK, **point 재기동 후 활성**.
- 켜려면 POINT_JOIN을 양수로. (원래 판정 근거는 아래 유지)

<details><summary>원래 보류 판정(2026-09-23, 참고)</summary>
- AS-IS `PointServiceImpl.earnPoint("join")`은 일반가입/SNS가입(`UserServiceImpl:530,575`)에서 **무조건 호출**되나,
  지급액이 `Config.pointJoin`(=`OP_CONFIG.POINT_JOIN`, 관리자화면 `config/point.jsp` M00513 "회원가입시 포인트")이고
  **0이면 `point!=0` 가드로 실제 적립 안 함**.
- **판정**: 리포지토리에 `POINT_JOIN` 시드 없음 + 사용자가 "가입 테스트에서 포인트 적립 본 적 없음" 증언 → live에서
  **POINT_JOIN=0(비활성)로 판단**. SalesOn 설정 미이관 패턴([[honor-tier-thresholds-unset]]과 동일).
- **처리**: [[defer-saleson-dependent-unused-features]]로 **보류**(MEMBER_JOINED→point 적립 소비자 미구현). 확정은 AS-IS
  운영관리 포인트설정 화면의 "회원가입시 포인트" 값으로. >0이면 point가 `MEMBER_JOINED` 구독 → `earnPoint("join")` 이식.
</details>

### 5. 정합성 / 순서 / 멱등 / 실패
- 순서: 같은 사용자 관련 이벤트는 동일 토픽+key로 파티션 내 순서보장(기존 donation 교훈 반영).
- 멱등: 소비자는 (userId+eventType+at) 또는 이벤트ID로 중복 무시.
- 실패: 발행 실패는 현행 패턴대로 `whenComplete` 로그(비차단). 트랜잭셔널 아웃박스(정확히-1회)는 향후 과제.

### 6. Phasing (ReadModel-defer 준수)
- **Phase A — 지금 가능(발행만)**: member에 KafkaTemplate + `MemberEventPublisher` + `KafkaTopicConfig` +
  application.yml producer 블록 추가, §3의 5개 이벤트 발행지점 삽입. **publish-only** — 소비자 없어도
  무해(로그로 관찰), 기존 4개 서비스 발행과 동형. RFP "이벤트 원칙" 즉시 충족.
- **Phase B — DA 설계 후**: admin `MemberStatsListener` + `StatsService` 회원통계 ReadModel.
- **보류**: 본인인증 이벤트(외부연계), ProviderAccount 이벤트(포털분리), 감사 이벤트(API로 대체),
  point 축하포인트(활성확인).

### 7. 결정 (2026-09-22 확정)
1. **Phase A만(발행) 지금 진행** — member 발행 인프라 + 5개 이벤트 발행지점. admin 통계 ReadModel
   (Phase B)은 [[defer-readmodels-pending-da-design]]대로 DA 설계까지 보류.
2. **감사 이력 = (B) 이벤트로 admin에 축적** 방향. 단 별도 "감사 전용 이벤트"는 만들지 않는다 —
   §3의 `LOGIN_SUCCEEDED`/`LOGIN_FAILED`/`MEMBER_WITHDRAWN`/`MEMBER_DORMANT`가 곧 감사 소스이므로,
   Phase A 발행이 감사-이벤트 방향을 이미 충족한다. admin 감사 ReadModel 소비는 Phase B(보류).
3. **point 회원가입 축하포인트**: 별도 확인 후 판단(현재 미포함).

### 8. Phase A 구현 대상 (this pass)
- member `build.gradle` spring-kafka 추가, `application.yml` producer 블록, `KafkaTopicConfig`(member.lifecycle).
- `MemberEventPublisher` + 이벤트 record 5종.
- 발행지점: 가입완료 / 로그인 성공 / 로그인 실패 / 탈퇴(일반+연동해지) / 휴면전환.

---

## 4. gift 옵션 다형 체계 재설계

> 통합 전 파일: `docs/gift-option-redesign-plan.md`

## gift 옵션 다형 체계 재설계 계획 (AS-IS 동일 재현)

> 근거: [[gift-option-parity-audit]] 갭목록 + 사용자 결정(2026-09-22).
> 방침: **기능 자체(서비스+화면)는 AS-IS와 동일하게 풀 구현**하되, 판매자 등록화면에서
> AS-IS처럼 **S2(2조합형)·T(텍스트형 옵션)는 CSS 숨김**(마크업 존재·비노출)으로 처리한다.
> 판매자(답례품관리자) / 운영자(admin) 분리도 AS-IS 동일. 옵션 재고는 **옵션단위**(AS-IS parity).
>
> **기준점([[as-is-parity-includes-disabled-state]])**: AS-IS에 기능이 있으면 다 만들되, AS-IS 소스가
> 숨김(CSS)/주석/비활성이면 MSA도 동일하게 숨김/주석/비활성으로 남긴다(빼지도 켜지도 않음).

### 0. 전제 — DDL 불요 (스키마 이미 존재)

로컬 gift DB에 AS-IS 이관 스키마가 그대로 있다. **추가 DDL 없이 애플리케이션 계층만 구현**한다.

- `op_item_option`: item_option_id(PK), item_id, **option_type**, option_display_type, option_hide_flag,
  **option_name1/2/3**, option_price, option_cost_price, option_price_nonmember, **option_stock_flag/quantity/code**,
  option_stock_schedule_date/text, **option_sold_out_flag**, option_display_flag, created_user_id/date.
- `op_item`: **item_option_flag, item_option_type, item_text_option_flag, item_text_option_title1/2/3**.
- `op_item_addition`: (item_id, addition_item_id) — 추가구성 조인.
- `op_item_option_soldout`: 아이템 단위 품절 요약(목록/상세 조인).

> 유일한 예외: 주문측(OrderItem)에 각인값(textOption)·itemOptionId 저장 컬럼이 없으면 그때만 최소 ALTER.
> Phase 4에서 판정.

### 1. AS-IS 동일 구현 대상 (전 옵션형태)

| 옵션형태 | 판매자 등록화면 | 서비스/데이터 | 구매자 화면 |
|---|---|---|---|
| S 선택형 | **노출** | 구현 | 단일 드롭다운 |
| S3 3조합형 | **노출** | 구현 | name1→name2→name3 종속 드롭다운 |
| S2 2조합형 | **숨김(hidden)** | 구현(비노출) | (편성 시) 2단 드롭다운 |
| T 텍스트형 옵션 | **숨김(hidden)** | 구현(비노출) | (편성 시) 옵션별 텍스트 |
| 각인=필수 추가정보(itemTextOptionFlag/Title1~3) | **노출**(별도 섹션) | 구현 | 필수 추가정보 입력칸 |
| 추가구성(itemAdditionFlag) | **노출**(form.jsp:1686~ "추가구성상품") | 구현(별도 item_data_type=2 + op_item_addition) | 별도 부가품 선택 |

핵심: **기능은 다 만들되 S2·T는 판매자 화면에서만 숨김.** 데이터·서비스·구매흐름·조회는 전부 동작하게.

### 2. 주문 연동 설계 (order 멀티아이템 위 배선)

- 이미 OrderItem에 `optionName`·`optionPrice`(단일) 존재 → 확장 배선.
- **선택형/조합형**: 장바구니 담기 시 `itemOptionId` 전달 → checkout에서 옵션 검증·optionName 조립
  (S: `제목: 값`, S2/S3: `제목1:값1 | 제목2:값2 [| 제목3:값3]`)·추가금액(option_price)을 결제금액·OrderItem에 반영.
- **각인(필수 추가정보)**: 구매자 입력값을 `textOption`(`||` 구분)으로 OrderItem에 저장 → 주문/마이페이지 표시.
- **추가구성**: AS-IS와 동일하게 **별도 주문 라인(OrderItem)** 으로 담김(본품과 독립 수량·가격).
- **옵션단위 재고(AS-IS parity)**: gift 재고예약 SAGA를 `option_stock_flag='Y'`인 옵션에 대해
  **itemOptionId 단위**로 차감/복원. option_stock_flag='N'이면 재고 무관.

### 3. 페이즈

#### Phase 1 — gift 모델/서비스 (기능 풀 구현, 화면 무관) ✅ 완료(2026-09-22, gift 컴파일 OK)
- ✅ `GiftOption` 확장: name2/3, display_type, hide_flag, cost_price, price_nonmember, stock_code, schedule_* 필드.
- ✅ `Gift`에 itemOptionFlag/Type, itemTextOptionFlag, itemTextOptionTitle1/2/3, itemDataType, itemAdditionFlag 매핑.
- ✅ `GiftAddition`/`GiftAdditionId`/`GiftAdditionRepository`(op_item_addition) 신설.
- ✅ `GiftOptionService`: `saveOptions`(S/S2/S3/T 전체교체), `saveTextOptions`(각인, 30자·`:|<>` 검증),
  `saveAdditions`(추가구성 교체), `additionsOf`. 기존 단일 register/edit 호환.
- ✅ buyer 상세(GiftPublicApiController): OptionDto에 optionType·name2/3, AdditionDto 신설, DetailResponse에
  additions 추가(옵션형태·각인은 gift 필드로 노출).
- ✅ API: PUT `/api/admin/gift-items/{itemId}/options/bulk|text|additions`.

#### Phase 2 — 판매자 등록화면 (S2·T 숨김) ✅ 완료(2026-09-22, gift 컴파일 OK)
- **배치 확정(A안)**: 현행 gift 판매자 포털(Thymeleaf, edit.html)에 API-first로 얹음.
- ✅ edit.html에 옵션 에디터 추가: 옵션 사용여부 Y/N, 옵션형태 라디오 S/S2/S3/T(**S2·T는
  `style="display:none"` 숨김 = AS-IS 동일**), 형태별 조합 그리드(name1/2/3·추가금액·재고연동·수량·숨김),
  필수 추가정보 섹션(제목1~3·30자·`: | < >` 금지 안내), 추가구성(ID 목록) 편성.
- ✅ **판매자 스코프 엔드포인트**(GiftController, currentSellerId 소유권): GET/POST `/gifts/{id}/options`,
  POST `/gifts/{id}/text-options`, POST `/gifts/{id}/additions`. (/api/admin/** 옵션 API는
  InternalApiAuthInterceptor로 내부전용이라 브라우저 미사용 → 판매자 경로 신설.)

#### Phase 3 — 구매자 storefront 상세(GiftDetailView) ✅ 완료(2026-09-22, storefront build OK)
- ✅ itemOptionType 분기: S 단일 드롭다운(기존), S2·S3 종속 드롭다운(sel1→sel2→sel3 → chosenOption 해석),
  재고 N개/[품절]/추가금액(+) 표시.
- ✅ 필수 추가정보 입력칸(itemTextOptionTitle1~3 → textOption `||`), 추가구성 체크박스(additions).
- ✅ 검증: V1 옵션 미선택 차단, V2 각인 미입력 차단, V3 품절 차단.
- ⚠ textOption·additionItemIds는 payload에 실리나 order 장바구니가 아직 미소비 → **Phase 4에서 배선**
  (Jackson이 미지원 필드 무시하므로 현재 무해).

#### Phase 4 — 장바구니/체크아웃 배선 ✅ 완료(2026-09-22, gift·order 컴파일·테스트·build OK)
- ✅ **최소 ALTER**: od_cart_item·od_order_item에 text_option 컬럼 추가. od_cart_item 유니크키를
  (user,item,option)→(user,item,option,**text_option**)로 교체(각인 다르면 별도 라인, ''로 정규화).
  `database/ddl/migration-order-option-textoption.sql` (적용 완료).
- ✅ CartItem/OrderItem에 textOption 필드. AddRequest에 textOption·additionItemIds.
- ✅ CartService.add(…,textOption,additionItemIds): 각인 스냅샷·4튜플 dedup, 추가구성은 **별도 라인**으로 추가.
  checkoutMultiItem이 OrderItem.textOption 복사. gift 단건 옵션 API는 조합명(name1/2/3) 결합.
- ✅ CartLine·CheckoutLine에 textOption 노출, CartView·CheckoutView에 "각인 : …" 표시.
- ✅ (부수) 이전 세션 단일옵션 작업으로 깨져 있던 CartServiceTest 5건(3-arg finder·7-arg createOrder) 정정.

#### Phase 5 — 옵션단위 재고 SAGA ✅ 완료(2026-09-22, gift·order 컴파일·테스트 OK)
- ✅ **최소 ALTER**: od_order_item·GIFT_SHIPMENT_STOCK에 item_option_id 추가
  (`database/ddl/migration-order-option-stock.sql`, 적용 완료).
- ✅ OrderItem에 itemOptionId(checkout에서 CartItem→OrderItem 복사), ShipmentCreatedEvent.Line(order·gift)에 itemOptionId.
- ✅ gift reserveStockForShipment: 옵션이 option_stock_flag='Y'면 **옵션단위**(op_item_option.option_stock_quantity)
  로 선검증·차감·품절갱신(option_sold_out_flag), 아니면 기존 답례품단위. 예약행(GiftShipmentStock)에 itemOptionId 기록.
- ✅ restoreReservation: itemOptionId 있으면 옵션 재고 복원, 없으면 답례품 재고 복원(SHIPMENT_CANCELLED·ITEM_CANCELLED 공용).
- ⚠ op_item_option_soldout 요약테이블은 미갱신(옵션행 option_sold_out_flag로 직접 표기) - AS-IS 요약 재현은 후순위.

#### Phase 6 — 조회/표시/포인트 ✅ 완료(2026-09-22, order 컴파일·테스트·storefront build OK)
- ✅ 주문상세(OrderMyApiController.OrderItemDto)·주문완료(CheckoutApiController.DoneItemDto)에 textOption 노출.
- ✅ storefront OrderDetailView·OrderCompleteView에 "각인 [ … ]" 표시(장바구니·체크아웃은 Phase 4).
- ✅ point: 옵션 추가금액은 이미 OrderItem.pointAmount=(salePrice+optionPrice)*qty에 포함돼 출고 포인트차감/기부포인트에
  그대로 반영(checkoutMultiItem). 추가 코드 불요 확인.

### 완료 요약
**Phase 1~6 코드 완료(2026-09-22). 전 서비스 컴파일·테스트·storefront build OK. 재기동 시 활성.**
적용 DDL: `migration-order-option-textoption.sql`, `migration-order-option-stock.sql`(+ gift DB gift_shipment_stock ALTER).

#### 후속 보강(2026-09-22, 사용자 지적 반영)
- ✅ **추가구성 자식 답례품 생성 구현**(AS-IS 동일): `GiftService.createAdditionChild`가 추가상품명/가격/재고로
  item_data_type=2 자식 OP_ITEM을 생성(본품 seller/locgov/category 상속, displayFlag='N'로 목록 숨김,
  dataStatusCode='APPROVED'로 주문가능) → op_item_addition 연결. `GiftOptionService.saveAdditions`가
  교체 시 기존 자식까지 삭제. edit.html 추가구성 UI를 (상품명·가격·재고) 그리드로 교체 + 조회 GET.
  (기존 "ID 링크" 단순화 폐기.)
- ✅ **옵션 전부 품절 → 목록 품절뱃지**(AS-IS op_item_option_soldout 요약 대체): MSA는 그 배치 파생캐시를
  안 쓰고, `GiftOptionService.optionSoldOutItemIds`가 조회시점에 "표시옵션 전부 품절" 아이템을 계산해
  목록 카드 soldOut에 OR. 시드(item 1026: 재고50·옵션 전부품절)로 판정 검증 완료(실화면은 재기동 후).
- 남은 후순위: S2·T 실데이터 E2E(판매자 화면 숨김이라 시드 필요).

### 4. 미결/확인
- Phase 2 판매자 UI 스택(Vue storefront vs gift 서빙) — 착수 시 확인.
- ~~추가구성 편성 UI 존재 여부~~ → **확인완료**: form.jsp:1686~ "추가구성상품"(itemAdditionFlag) 노출.
  각 추가구성은 item_data_type=2 별도 OP_ITEM + op_item_addition 연결. Gift에 itemDataType·itemAdditionFlag 매핑 필요.
- OrderItem 각인/optionId 저장 컬럼 유무 → Phase 4에서 최소 ALTER 판정.
- **옵션단위 재고 이해 확인**: "AS-IS 동일"에 따라 option_stock_flag='Y' 옵션은 옵션단위 차감. (사용자 확인 요청)

---

## 5. order 멀티아이템 재설계

> 통합 전 파일: `docs/order-multiitem-redesign-plan.md`

## 주문(order) 도메인 멀티아이템 재설계 설계안

> **문서 목적 / 독자**: 이 문서는 order 도메인을 "1주문=1답례품"에서 **"1주문=여러 답례품(AS-IS 계층)"**으로 재설계하기 위한 **검토용 설계안**이다. order 서비스를 구현·리뷰할 개발자와 아키텍처 리뷰어가 대상이다. 착수 전 이 문서를 리뷰해 범위·순서·리스크에 합의한 뒤 단계별로 구현한다.
>
> 작성 2026-09-21. 배경 결정 근거는 [as-is-logic-is-the-spec], [api-vs-kafka-decision-criteria], [point-use-tracking-g-cntr-use-point] 메모 및 `docs/design-decisions.md` §2 참고.

---

### 1. 배경과 결정

#### 1-1. 현행(AS-IS 대비 이탈)
- **MSA 현행**: 1주문 = 1답례품. 체크아웃(`CartService.checkout`)이 장바구니의 **행마다 `OrderService.createOrder`를 호출해 별도 단일품목 주문**을 만든다. 한 번 결제해도 품목 수만큼 주문번호가 생긴다.
- **AS-IS**: 1주문(`ORDER_CODE`) → **출고(`ORDER_SEQUENCE`, 지자체/판매자 그룹)** → **품목(`ITEM_SEQUENCE`)** 3단 계층. 한 번 결제 = 주문 1건, 그 안에 여러 지자체·여러 품목.

#### 1-2. 왜 바꾸나 (근거)
- **RFP·ISP 어디에도 "단일품목 주문" 요건이 없다.** RFP SFR-006은 "장바구니·결제·주문상태 관리"만 명시. SAGA는 "포인트 차감·복원"에 적용하라는 것이지 주문 분할을 강제하지 않는다.
- **ISP는 오히려 멀티아이템에 가깝다.** 이벤트스토밍(원본 p.415/427)에 주문관리 명령이 "주문생성 / **출고생성** / 운송장등록 / 배송완료 / 반품·교환…"으로 **주문과 출고를 분리** 설계 → AS-IS의 `ORDER_CODE→ORDER_SEQUENCE(출고)` 계층과 동일 방향.
- 단일품목은 **구현자의 SAGA 단순화 선택**이었을 뿐 정본(AS-IS)·요건과 어긋난다. 옵션·부분취소·부분반품 등 후속 기능이 전부 AS-IS와 달라지는 근본 원인이다.

#### 1-3. 결정
**AS-IS처럼 멀티아이템 주문 계층으로 재설계한다(옵션 2).** 단, 규모가 커서 이 설계안 리뷰 후 단계별로 착수한다.

---

### 2. 목표 도메인 모델

#### 2-1. 계층 (AS-IS 대응)
```
Order (주문 헤더)                      = AS-IS ORDER_CODE
 └─ Shipment (출고 = 지자체+판매자 그룹) = AS-IS ORDER_SEQUENCE
     └─ OrderItem (품목)               = AS-IS ITEM_SEQUENCE
```

- **Order (주문 헤더, `OD_ORDER` 재정의)**: orderId, userId, createdDate, 주문 전체 상태(요약), 배송지 정보(받는이/주소/전화/요구사항 — 주문 단위 공통), 총 주문포인트/총 취소포인트/총 결제포인트, 총 배송비.
- **Shipment (출고, 신규 `OD_SHIPMENT`)**: shipmentId, orderId, **locgovCode(+sellerId)**, 배송상태(deliveryStatus), 택배사(carrierCode)/송장(invoiceNo), 출고 단위 배송비. — **포인트 차감이 지자체 단위이므로 출고=지자체 그룹이 SAGA·정산의 기본 단위**가 된다.
- **OrderItem (품목, 신규 `OD_ORDER_ITEM`)**: orderItemId, shipmentId(또는 orderId+locgov), itemId, itemName(스냅샷), **optionName/optionPrice(스냅샷)**, quantity, unitPrice, pointAmount(품목 소계), **품목별 상태**(주문/취소/반품/교환 - 부분처리용), couponIssueId/discount.

> **옵션은 OrderItem 속성**이다. 방금(2026-09-21) 단일품목 모델에 옵션을 얹었는데, 재설계 시 옵션 스냅샷 필드(optionName/optionPrice)는 그대로 OrderItem으로 이동한다.

#### 2-2. 상태 모델
- **품목 단위 상태**가 정본이다(AS-IS도 ITEM_SEQUENCE 단위 상태). 부분취소/부분반품이 여기서 나온다: 10 결제대기 / 20 결제완료 / 30 배송중 / 35 배송완료 / 40 구매확정 / 취소·반품·교환 계열.
- **출고 상태** = 그 출고 품목들의 배송상태(송장·배송중·완료).
- **주문 헤더 상태** = 품목 상태들의 요약(전부 확정=확정, 일부 취소=부분취소 등) — 표시용 파생.

---

### 3. 이벤트 / SAGA 재설계

#### 3-1. 현행 SAGA (단일품목)
`order.saga` 토픽. `ORDER_CREATED` → gift `STOCK_RESERVED`/`STOCK_RESERVE_FAILED` + point `POINT_DEDUCTED`/`POINT_DEDUCT_FAILED` → order `resolveIfReady`(재고예약 && 포인트차감 → `ORDER_CONFIRMED`, 아니면 `ORDER_CANCELLED`). 보상은 주문(=품목) 1건 단위.

#### 3-2. 목표 SAGA (멀티아이템)
핵심 난점은 **한 주문이 여러 지자체를 포함**한다는 것 — 포인트는 지자체별로만 쓸 수 있어 **차감은 출고(지자체) 단위**, 재고 예약은 **품목 단위**다.

**선택지 A — 출고(지자체) 단위 SAGA (권장)**
- 주문 1건을 **출고 N개(지자체별)** 로 나눠 각 출고를 독립 SAGA 인스턴스로 처리. 상관ID = shipmentId.
- `SHIPMENT_CREATED(shipmentId, orderId, locgov, items[])` → gift가 **품목별 재고예약**(출고 내 모든 품목 성공해야 출고 성공) + point가 **출고 단위 포인트차감**(그 지자체 소계) → order가 출고 확정/취소.
- 출고 단위로 부분 성공 가능(강남구 출고는 성공, 상주 출고는 재고부족으로 취소). 주문 헤더는 출고 결과들을 집계.
- **장점**: 포인트(지자체)·정산(지자체) 경계와 일치, 부분처리 자연스러움. **AS-IS ORDER_SEQUENCE=출고 단위와 정확히 대응**.

**선택지 B — 주문 단위 원자 SAGA**
- 주문 전체가 all-or-nothing. 한 품목이라도 실패하면 주문 전체 취소. 구현은 단순하나 부분처리·부분결제 불가 → AS-IS와 다름. **비권장**.

→ **선택지 A(출고 단위 SAGA)** 를 채택. 보상 트랜잭션도 출고/품목 단위로 정의(부분취소=해당 품목만 재고복원+포인트복원).

#### 3-3. 이벤트 스키마 변화
- 기존 `ORDER_CREATED(orderId, itemId, quantity, pointAmount, locgov)` → `SHIPMENT_CREATED(shipmentId, orderId, locgovCode, userId, lines[{orderItemId, itemId, optionId, quantity, pointAmount}], groupPointAmount)`.
- `STOCK_RESERVED/FAILED`, `POINT_DEDUCTED/FAILED`, `CONFIRMED/CANCELLED`를 **shipmentId 상관ID**로. 재고 결과는 품목 리스트 단위.
- 부분 클레임: `ITEM_CANCEL_REQUESTED / ITEM_RETURN_REQUESTED / ITEM_EXCHANGE_REQUESTED(orderItemId)` → 승인 시 `ITEM_CANCELLED` → gift 재고복원 + point 포인트복원(그 품목분).
- admin 통계 리스너(`stat_order_ledger`)도 출고/품목 단위 반영으로 수정.

---

### 4. 영향 범위 (변경 대상)

| 영역 | 변경 |
|---|---|
| **DB (ord 스키마)** | `OD_ORDER` 헤더로 축소 + `OD_SHIPMENT`·`OD_ORDER_ITEM` 신설. FK 없이(서비스 내부도 미사용 원칙) orderId/shipmentId 참조 |
| **체크아웃** | `CartService.checkout`: 행마다 주문 → **장바구니 전체를 주문 1건**, 지자체별 출고, 행별 품목. 옵션 스냅샷은 품목으로 |
| **SAGA** | 출고 단위 인스턴스로 재작성. gift `reserveStockForOrder`→품목 리스트, point `deductForOrder`→출고(지자체) 단위 |
| **주문상세** | 방금 만든 단일 레이아웃 → **지자체(출고) 그룹 + 품목 목록** 반복(AS-IS orderDetail.html 원형 그대로). 이미 CSS·구조는 재현돼 있어 데이터만 다차원화 |
| **주문목록** | 주문 1건에 대표품목+외 N건 표기 |
| **클레임** | `ClaimService`: 주문 단위 → **품목 단위** 부분취소/반품/교환 |
| **포인트** | 출고(지자체)별 포인트 차감/복원 — 이미 지자체별 lot 소진 로직 있음([point-use-tracking-g-cntr-use-point]) |
| **admin** | 주문/정산/통계 조회모델을 출고·품목 단위로 |
| **프론트** | 장바구니(이미 그룹 있음)·체크아웃·주문상세·주문목록·클레임 화면 |

---

### 5. 마이그레이션 계획

기존 단일품목 주문(`od_order` 각 행)을 **주문1+출고1+품목1**로 승격한다.
1. 신규 테이블 생성(`od_shipment`, `od_order_item`).
2. 백필: 각 기존 `od_order` 행 → `od_order`(헤더, 배송지/포인트 합계 유지) 유지 + `od_shipment`(그 주문의 locgov 1개) 1건 + `od_order_item`(itemId/option/qty/point) 1건 생성. 기존 컬럼(itemId/optionName 등)은 품목으로 이관 후 헤더에서 제거(또는 뷰 유지).
3. 클레임(`od_claim`)의 order 참조 → orderItem 참조로 매핑(기존은 1:1이라 자연 승계).
4. 이벤트/SAGA는 **신규 주문부터** 신 스키마로. 진행 중 주문이 없도록 배포 타이밍 조율(로컬은 무관).

---

### 6. 단계별 구현 순서 (제안)

1. **Phase 1 — 스키마·엔티티** ✅**완료(2026-09-21)**: `OD_SHIPMENT`/`OD_ORDER_ITEM` DDL(`database/ddl/migration-order-multiitem-phase1.sql`, ord 스키마 적용) + 엔티티(`Shipment`/`OrderItem`)·리포지토리(`ShipmentRepository`/`OrderItemRepository`). **가산적**: 기존 od_order 컬럼 미변경, 단일품목 경로 그대로 병행. ddl-auto=none이라 기동검증 무영향, 아직 호출부 없음.
2. **Phase 2 — 체크아웃 (order-side)** 🔶**진행중(2026-09-21)**: 아래 2-1 참조. **order 서비스 신규 경로 완성**(레거시와 병행, 아직 미배선). gift/point 소비자 전환은 Phase 3.
3. **Phase 3 — SAGA 재작성** 🔶**진행중(2026-09-21)**: gift/point 소비자 완성(아래 6-3). SAGA 루프 전체 배선·컴파일. 미배선(체크아웃 컨트롤러 전환 전이라 실트래픽 없음).
4. **Phase 4 — 주문상세/목록/완료 화면** ✅**완료(2026-09-21, 아래 6-4)** + **체크아웃 flip 반영**(재기동 시 활성).
5. **Phase 5 — 클레임/배송**: 5a 완료(per-item 재고·포인트), 5b 완료(품목 단위 부분취소/반품/교환), **5c 배송(출고 단위 송장·배송상태·구매확정)은 seller/admin 의존이라 보류**. 아래 6-5.
6. **Phase 6 — admin/통계·마이그레이션·point조회모델**: 조회모델·백필·refKey 표시.

각 Phase 끝에 E2E 검증(다지자체 장바구니 결제 → 주문 1건 → 부분취소).

#### 6-1. SAGA 구조 결정 (2026-09-21 확정)
- **선택지 A(출고=지자체 단위 SAGA)** 채택 — AS-IS `ORDER_SEQUENCE`(출고)와 정확히 대응, 포인트·정산 경계 일치. 상관ID=shipmentId.
- **부분실패 UX(§7)**는 Phase 3(SAGA)에서만 영향 → 그 시점에 재확인.

#### 6-2. Phase 2 order-side 산출물 (2026-09-21)
> ⚠ **핵심 제약**: 체크아웃(쓰기)·SAGA·읽기 화면은 **원자적 전환**만 가능(중간 배포 시 신규 주문이 확정 불가/화면 깨짐). 그래서 **레거시 단일품목 경로를 기본값으로 유지한 채 신규 출고 단위 경로를 병행 구축**하고, gift/point 소비자·읽기까지 준비되면 마지막에 컨트롤러를 한 번에 전환한다.

- **스키마**: `migration-order-multiitem-phase2.sql` — `od_order`의 item_id/quantity/unit_price NOT NULL 해제(헤더는 품목컬럼 없이 저장, 레거시는 계속 채움 → 하위호환).
- **이벤트(신규, order.saga 토픽·key=orderId)**: `SHIPMENT_CREATED`(order→gift/point, 품목 lines[] 포함), `SHIPMENT_STOCK_RESERVED/FAILED`(gift→order), `SHIPMENT_POINT_DEDUCTED/FAILED`(point→order), `SHIPMENT_CANCELLED`(order→gift/point 보상).
- **체크아웃**: `CartService.checkoutMultiItem` — 검증(priceSelected)·포인트 부족판정은 레거시와 동일. 주문 헤더1 + 지자체별 출고N + 품목M 저장, 쿠폰 확정소진, 출고마다 `SHIPMENT_CREATED` 발행. 반환=orderId 1건. (레거시 `checkout`은 그대로 존치)
- **출고 SAGA 해소**: `ShipmentSagaService` — 출고별 stock&&point 결과 집계→CONFIRMED/CANCELLED(취소 시 `SHIPMENT_CANCELLED`로 반대편 보상), 주문 헤더는 출고들 종결 후 CONFIRMED/CANCELLED/**PARTIALLY_CONFIRMED**(부분성공) 집계. `OrderSagaListener`에 SHIPMENT_* 케이스 배선.
- **아직 안 함(다음)**: 읽기 화면(done/목록/상세), 컨트롤러 전환, 기존 주문 백필.

#### 6-3. Phase 3 gift/point 소비자 산출물 (2026-09-21)
- **gift**: `migration-gift-shipment-stock.sql`(GIFT_SHIPMENT_STOCK, PK=(shipment_id,item_id)) + `GiftShipmentStock` 엔티티/리포지토리. `GiftService.reserveStockForShipment`(출고 품목 전량 선검증 후 차감, all-or-nothing, 멱등)·`restoreStockForShipment`(예약분만 복원). 리스너에 `SHIPMENT_CREATED`→예약→`SHIPMENT_STOCK_RESERVED/FAILED`, `SHIPMENT_CANCELLED`→복원 배선.
- **point**: `PointService.deductForShipment`(출고=지자체 단위 차감, refKey=`orderId#shipmentId`로 출고별 멱등·독립복원)·`restoreForShipment`. 리스너에 `SHIPMENT_CREATED`→차감→`SHIPMENT_POINT_DEDUCTED/FAILED`, `SHIPMENT_CANCELLED`→복원 배선. `consumeLots`/`gCntrUsePoint`도 refKey 단위.
  - ⚠ **Phase 6 유의**: point 조회모델(기부포인트 상세)의 '답례품 주문번호' 표시는 refKey의 `#` 앞부분(orderId)을 써야 한다(현재는 refKey 원문). 신 모델 flip 전에 처리 필요.
- **이벤트 계약 로컬 사본**: gift/point에 `ShipmentCreatedEvent`(subset)·`ShipmentCancelledEvent`(subset) + 각자 발행하는 결과 이벤트 레코드. order 원본과 구조 동기화 유지.

#### 6-4. Phase 4 읽기 화면 + flip 산출물 (2026-09-21)
- **읽기 API(신 모델, 레거시 폴백 포함)**: `OrderMyApiController` 주문목록(대표품목 "외 N건"+itemCount)·주문상세(헤더+출고그룹 shipments[]→품목 items[], 결제=상품소계+배송비-취소분). `CheckoutApiController.done` 주문완료(출고 그룹+품목). **출고행이 없으면(백필 전 레거시 주문) 헤더를 단일 출고·품목으로 어댑팅** → 기존 주문도 백필 없이 정상 표시.
- **프런트**: `OrderDetailView`(출고→품목 2중 루프, 품목별 상태/후기, 출고별 배송상태), `OrderCompleteView`(그룹 items[]+헤더 배송지/배송비), `MyOrdersView`(대표품목·itemCount, 다품목은 목록 후기버튼 숨김).
- **flip**: `CheckoutApiController.complete` → `checkoutMultiItem`(주문 1건 반환). 레거시 `CartService.checkout`은 롤백용 존치. **재기동+빌드 시 활성**.
- **flip 후 알려진 미커버(Phase 5/6)**: ① 주문취소/구매확정/반품·교환이 아직 헤더/주문 단위(출고·품목 단위 아님) ② admin 주문목록의 답례품명 등 헤더 품목컬럼은 신 주문에서 NULL(조회모델 Phase 6) ③ point 기부포인트 상세 '주문번호'=refKey 원문 ④ 기존 주문 백필 미실행(폴백으로 표시).
- **E2E 검증 필요(재기동 후)**: 다지자체 장바구니 결제 → 주문 1건(주문번호 1개) → 지자체별 출고 확정 → 주문상세 그룹/품목 표시 → 포인트 지자체별 차감 확인.

---

### 7. 리스크 / 검토 포인트

- **포인트 차감 단위**: 출고(지자체) 단위가 맞다(포인트는 지자체 전용). 주문 단위로 묶으면 안 됨.
- **부분 실패 UX**: 다지자체 주문에서 한 지자체만 재고부족 시 — 그 출고만 취소하고 나머지는 확정할지(선택지 A), 주문 전체 실패로 볼지(선택지 B). **A 권장**이나 제품 결정 필요.
- **주문번호 체계**: 현재 품목마다 orderId. 재설계 후 주문 1개 = orderId 1개, 출고/품목은 하위 시퀀스. 기존 데이터·외부 참조(리뷰의 orderCode 등) 영향 점검.
- **정산(admin)**: 지자체·판매자별 정산이 출고 단위와 정합. 현 통계 리스너 재작성 필요.
- **범위 확정 전 착수 금지**: 이 문서 리뷰에서 선택지 A/B와 Phase 범위를 확정한 뒤 Phase 1 시작.

#### 6-5. Phase 5 산출물 (2026-09-21)
- **★ Phase 3 버그 수정**: `GIFT_SHIPMENT_STOCK` PK를 (shipment_id,item_id)→**order_item_id**로 변경. 같은 답례품의 서로 다른 옵션이 한 출고에 2줄로 담기면 (shipment,item)으로는 PK 충돌했다. 재고 선검증도 답례품별 합산 수요로 수정.
- **5a per-item 보상 토대**:
  - gift: `reserveStockForShipment`/`restoreStockForShipment`가 orderItemId 단위 기록, `restoreStockForItem(orderItemId)` 추가. 이벤트 `SHIPMENT_CREATED.lines[]`에 orderItemId 추가.
  - point: 차감 원장을 **품목(orderItemId) 단위로 분할**(refKey=`orderId#orderItemId`), 잔액검증은 출고 총액 1회. `restoreForShipment(orderItemIds)`=품목별 복원 루프, `restoreForItem(orderId,orderItemId)` 추가. `SHIPMENT_CREATED.lines[{orderItemId,pointAmount}]`.
- **5b 품목 단위 부분취소/반품/교환**:
  - order: `ShipmentSagaService.cancelItem`(품목→CANCELLED, `ITEM_CANCELLED` 발행, 출고 전량취소 시 출고 CANCELLED, 헤더 재집계=품목 기준 CONFIRMED/CANCELLED/PARTIALLY_CONFIRMED). `ITEM_CANCELLED` 이벤트(gift/point가 그 품목분만 복원).
  - `OrderService.cancelItem`(발송 전 즉시 부분취소), `ClaimService` 품목 단위 재작성(request/approve/reject/complete, complete→cancelItem). `Claim`에 order_item_id/shipment_id 컬럼.
  - API: `POST /api/orders/{id}/items/{orderItemId}/cancel`, `.../claim`. 프런트 주문상세 품목별 버튼(주문취소/교환·반품/후기) + 선택품목 클레임 폼.
- **5c 보류(seller/admin 의존)**: 출고 단위 송장등록/배송상태/배송완료/구매확정. 현재 delivery 필드는 Shipment에 있으나 세팅 경로(seller/admin)가 신 모델 미대응 → return/exchange는 출고가 DELIVERED여야 신청 가능하므로 5c 이후 완전 검증됨.
- **DDL 적용됨**: `migration-gift-shipment-stock.sql`(재작성, order_item_id PK), `od_claim` order_item_id/shipment_id 컬럼(ALTER).

#### 6-6. Phase 6 산출물 (2026-09-21)
- **★ 백필 미실행 결정**: 기존 단일품목 주문을 출고/품목행으로 백필하지 **않는다**. 이유: 기존 주문의 재고·포인트 예약은 레거시 추적(GIFT_ORDER_STOCK PK=orderId, pt_point_ledger refKey=orderId)에 있는데, 백필로 품목행만 만들면 신 per-item 보상 경로(refKey=`orderId#orderItemId`, GIFT_SHIPMENT_STOCK)가 그 예약을 못 찾아 복원이 실패한다. 대신 **기존 주문은 레거시 경로로 처리**: 읽기는 폴백(헤더→단일 출고·품목), 취소는 주문 단위(`/api/orders/{id}/cancel`→ORDER_CANCELLED→레거시 복원). 프런트가 `orderItemId` 유무로 분기(있으면 품목 단위, 없으면 주문 단위, 레거시는 클레임 버튼 숨김).
- **point 조회모델 표시**: 기부포인트 상세 '답례품 주문번호'가 refKey 원문 대신 `#` 앞부분(주문번호)만 표시(`PointService.displayOrderCode`). 멀티아이템 주문은 품목마다 USE 행이 생겨 AS-IS의 품목별 사용내역 표시와도 더 부합.
- **admin 주문목록 헤더 요약**: `checkoutMultiItem`이 헤더의 표시용 컬럼(itemName="대표품목 외 N건", quantity=총수량, itemId/sellerId=대표, locgovCode=단일지자체만)을 채운다 — 권위 데이터는 OrderItem, 헤더는 admin 주문목록·검색·마이주문 목록이 읽는 **요약 캐시**. admin 콘솔이 신 주문에서 빈칸 없이 표시된다.
- **여전히 보류(admin 재개 시)**: ① admin 통계(`stat_order_ledger`)는 헤더 집계가 ORDER_CONFIRMED를 발행하지 않아 신 주문 미반영 → 출고/품목 단위 이벤트로 재작성 필요 ② 멀티셀러 주문의 판매자별 admin 필터는 헤더 대표 sellerId로는 부정확 → OrderItem 기준 조회로 재작성 필요 ③ 5c 출고 단위 배송 라이프사이클.

---

## 6. B5 특정사업 월별 통계 명세

> 통합 전 파일: `docs/b5-designated-month-stats-spec.md`

## B5 — 지정기부 월별통계(특정사업 월별통계) AS-IS 완전 재현 스펙

출처: AS-IS `designated-donation/analysis/month.jsp` + `DesignatedDonationManagerController`(/opmanager/designated-donation/analysis/month/{campaign,amountraised,amount}) + `designated-donation-mapper.xml`(selectDesignatedLocgovMonth{Campaign,AmountRaised,Amount}, 각 1231/1322/1441행). TO-BE 재현용.

### 화면 구성(month.jsp)
- 필터: 시도(shWdr, 본사 1~4만 노출)/지자체(shLocgovCode, 시도 선택 시 cascading)/조회년도(selYear)/상태(prjStatus: 0전체·2진행·9종료 라디오).
- 차트 3개(Chart.js, AS-IS와 동일 — `chart.min.js`+`op.chart.js` admin에 복사됨):
  1. **사업구분별 사업 진행건 비율** `designatedLocgovMonthCampaignChart` (type:pie, labels 취약계층/문화·예술/자원봉사/복리증진, 색 `rgb(255,99,132)`/`rgb(54,162,235)`/`rgb(255,205,86)`/`rgb(32,169,59)`, data=비율%). 아래 표: 1행 비율(%), 2행 건수(건). columnTpDesc.
  2. **모금액 비율** `designatedLocgovMonthAmountRaisedChart` (type:pie, 같은 색/labels, data=모금액비율%). 아래 표 3행: 비율(%)/모금액(원)/목표모금액(원). (TP1=모금액비율, TP2=모금액, TP3=목표모금액)
  3. **월별 모금액 추이** `designatedLocgovMonthAmountChart` (`ChartCommon.drawChartMultiYaxis('...','bar',{scales:{x:{stacked:false},y:{stacked:false}}}, monthArr, amountArr, {x:'월',y:'원'}, "FIXED")). amountArr=[목표금액(원) #00215A, 모금액(원) #09C2C7]. 아래 표 4행(목표금액/모금액/참여자수/사업건수)×(1~12월+합계).
- 조회: 페이지 로드 시 fnSearch()=3개 ajax 동시 호출. jQuery 사용($.post, $('#..').text, Common.numberFormat) → **AS-IS jquery 복사 필요**.

### 사업구분 코드(DSGN_DNTN_BIZ_SE_CD)
100 취약계층 · 200 문화/예술 · 300 자원봉사 · 400 복리증진.

### 백엔드 집계 3종 (CUBRID→Postgres 포팅)
공통: 대상 사업 = G_DSGN_DNTN_BIZ_MNG(P), 년도(selYear) 기간겹침 필터
`(BGNG_YMD BETWEEN yr0101..yr1231) OR (END_YMD BETWEEN ..) OR (yr0101 BETWEEN BGNG..END) OR (yr1231 BETWEEN BGNG..END)`,
시도/시군구 필터(shLocgovCode=LCLGV_CD 직접 / shWdr=상위코드로 G_LOCGOV에서 하위 LOCGOV_CODE IN), 부서(dsgncntrPartId>0 → DEPT_ID).
모금액 조인 = G_CNTR 집계 LEFT JOIN: `SUM(CAST(CNTR_AMT AS BIGINT)) ... GROUP BY DSGN_DNTN_BIZ_ID` where `CNTR_DE<=오늘(YYYYMMDD) AND DELETE_AT='N' AND STTEMNT_PAY_DE IS NOT NULL AND CNTR_STTUS_CODE='200' AND DSGN_DNTN_BIZ_ID>0`.
상태파생(PRJ_STATUS): STTS_CD='2'(진행)인데 (모금≥목표 OR END_YMD<오늘) 이면 '9'(종료)로 간주. 그 외 STTS_CD 그대로. prjStatus 필터는 이 파생값 기준(0이면 전체).

1. **campaign**(selectDesignatedLocgovMonthCampaign): 사업구분별 사업 '건수'와 '비율'. 2행(TP1 비율=FLOOR(cnt*10000/total/100), TP2 건수). 컬럼 prjBsns100~400, prjBsnsTot, columnTpDesc. 사업 1건=1 카운트(GROUP BY BIZ_ID 후 BSNS_TYPE별 집계).
2. **amountraised**(selectDesignatedLocgovMonthAmountRaised): 사업구분별 모금액/목표금액/비율. 3행(TP1 비율=FLOOR(amt*10000/sumamt/100), TP2 모금액, TP3 목표모금액). 목표금액은 END_YMD<=yr1231인 사업만 합산(MAX per biz).
3. **amount**(selectDesignatedLocgovMonthAmount): 월별 m01~m12 + total, 4행(TP1 목표금액/TP2 모금액/TP3 참여자수(CNTR_CNT)/TP4 사업건수). 목표금액 월 = PRJ_ED_DT(종료월)에 귀속(END_YMD<=yr1231 & 월=MM), 모금액/참여자 월 = CNTR_DE의 MM. 사업건수 월 = 사업 종료월 기준.
   ※ CUBRID SUBSTR(x,5,2)=MMDD의 월 두자리. DATE_FORMAT(NOW(),'%Y%m%D')는 오늘(주의: %D는 버그성이나 그대로 동작범위 내).

### DTO(DesignatedStat 재현 필드)
`tp, columnTpDesc, prjBsns100, prjBsns200, prjBsns300, prjBsns400, prjBsnsTot`(campaign/amountraised),
`columnTpDesc, m01..m12, total`(amount). JSON 키 camelCase.

### TO-BE 구현 계획
- donation: `DesignatedStatController`(or 기존 DesignatedAdminApiController 확장) 3 GET 엔드포인트 `/api/designated-projects/admin/analysis/month/{campaign,amountraised,amount}?shWdr&shLocgovCode&selYear&prjStatus`. 네이티브 집계(위 SQL Postgres 포팅: NVL→COALESCE, CAST AS BIGINT, to_char(now(),'YYYYMMDD'), substr). 지자체 스코프는 admin에서 넘김(지자체 담당자=자기 locgov 강제).
- admin: DesignatedProjectClient 3 메서드 + 컨트롤러가 month 페이지 라우트(`/designated-projects/analysis/month`?) 또는 기존 analysis 교체. 템플릿 = AS-IS month.jsp verbatim(필터+canvas3+표3), JS는 campaign/amountraised/amount 함수 그대로(엔드포인트만 admin 경로), jQuery+chart.min.js+op.chart.js 로드.
- 자산: `chart.min.js`,`op.chart.js` 복사완료. **jquery.min.js는 AS-IS(`ghlove-web/static/content/...`)에서 복사** 필요. `Common.numberFormat` 등 유틸도 AS-IS에서 가져오거나 동등 구현.
- 메뉴: 17202(월별통계)→ 이 페이지로 재배선(현재 B1에서 /designated-projects/analysis로 임시배선). 17201(지자체별통계)은 analysis/locgov 별도(AS-IS analysis/locgov.jsp — 차트 유무 추후 확인).

### 상태
**구현 완료(2026-10-01) · 재기동 사용자.**
- donation: `DesignatedStatRepository`(네이티브 3종, 라이브 DB 검증 통과) + `DesignatedStatService`(BsnsStatRow/MonthStatRow) + `DesignatedAdminApiController`에 `/api/designated-projects/admin/analysis/month/{campaign,amountraised,amount}` GET 3종. 컴파일 OK.
- admin: `DesignatedProjectClient`에 monthCampaign/monthAmountRaised/monthAmount + records(BsnsStat/MonthAmount); `DesignatedProjectAdminController`에 `/designated-projects/analysis/month` 페이지 + 데이터 3종(GET+POST, {isSuccess,data} 봉투, 지자체담당자 자기locgov 강제); 템플릿 `designated/analysis-month.html`(month.jsp verbatim: 필터+canvas3+표3, JS 그대로, 집계 URL만 admin 경로·시도→시군구는 로컬필터). 컴파일 OK.
- 에셋: jquery-1.11.0 / op.common.js(Common.numberFormat·isUndefined) / chart.min.js / op.chart.js(ChartCommon.drawChart·drawChartMultiYaxis) 로드 — 전부 AS-IS 복사본. `$.log`은 AS-IS에서도 미정의(월별통계 JSP 전용·정상경로 미호출)라 그대로 둠=parity.
- DB: 메뉴 17202(월별통계)→`/designated-projects/analysis/month` 재배선 적용(migration-admin-menu-B5-designated-month.sql).

#### AS-IS→TO-BE 스키마 치환(값 사전만, 로직 동일)
- 확정기부: AS-IS `CNTR_STTUS_CODE='200' AND STTEMNT_PAY_DE IS NOT NULL` → TO-BE `CNTR_STTUS_CODE='COMPLETED'`(TO-BE는 정산일자 미채움=전행 NULL, 기존 analysis도 COMPLETED만으로 확정판정).
- 사업상태: '1/2/9' ↔ PENDING/OPEN/CLOSED. 쿼리 내부에서 AS-IS 코드로 환산 후 상태파생(진행→모금≥목표 또는 종료일경과 시 종료)·prjStatus(0/2/9) 필터 동일.
- 조인키: G_CNTR.PRJ_ID(死컬럼, 전부 0) 대신 DSGN_DNTN_BIZ_ID.

#### 검증(2026 전체 기준, 라이브 DB)
campaign 91건(비율 25/24/24/26=100), amountraised 모금액합 16,662,000(시드 5건 일치·FLOOR 비율 99%), amount 월귀속(5월 6,000,000/6월 10,652,000/9월 10,000·참여자 5·목표금액 종료월 귀속) 전부 AS-IS 로직대로.

#### 남은 통계화면(B5 잔여, 순서 재결정 대상)
17201 지자체별통계(analysis/locgov 전용화면 분리), give-statistics all/locgov/operate, shop-statistics report/dashboard/sales, 관심지자체 등.

---

## 7. Thymeleaf 폐기 맵

> 통합 전 파일: `docs/thymeleaf-decommission-map.md`

## 대민 Thymeleaf → storefront SPA 커버리지 매핑 (폐기 대상 판정)

- 작성: 2026-09-17
- 목적: 5개 서비스(member·donation·point·gift·order)의 Thymeleaf 페이지가 **이미 storefront Vue3 SPA로 대체되었는지** 대조해, 안전하게 삭제할 목록을 확정한다.
- 전제: 대민 프론트의 Vue3+SPA(정본)는 **이미 storefront에 존재**한다. 따라서 이 작업은 "신규 전환"이 아니라 **중복 Thymeleaf 폐기(decommission)**다.
- 범위 제외: 판매자/제공자·운영관리는 PL 그룹 결정 후 별도. 이 5개 서비스 안에 섞여 있는 그 성격의 템플릿은 **유지**로 둔다.

### 판정 범례

| 판정 | 의미 | 조치 |
|---|---|---|
| **삭제** | SPA 라우트가 이미 대체 | 커버 확인 후 Thymeleaf 페이지+뷰@Controller 삭제 |
| **확인필요** | SPA에 유사 라우트는 있으나 완전 대체 여부 미확정 | 삭제 전 화면 대조 필요 |
| **유지-프린트** | 서버렌더 출력 페이지(SPA 화면 아님) | 유지(별도 결정) |
| **유지-범위밖** | 판매자/운영/배치 = 제외군 | 유지 |
| **유지-미커버** | 대민이지만 SPA에 아직 없음 | SPA 구현 전까지 유지 |

### member (22개: 페이지 17 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| home.html | `/`(home) | `/` | 삭제 |
| login.html | `/login` | `/login` | 삭제 |
| signup.html | `/signup` | `/signup` | 삭제 |
| find-idpw.html | `/find-idpw` | `/find-idpw` | 삭제 |
| mypage.html | `/mypage` | `/mypage` | 삭제 |
| profile.html | `/profile` | `/mypage/profile` | 삭제 |
| password.html | `/mypage/password` | `/mypage/password` | 삭제 |
| withdraw.html | `/mypage/withdraw` | `/mypage/withdraw` | 삭제 |
| delivery/list.html | `/delivery/list` | `/mypage/delivery` | 삭제 |
| delivery/write.html | `/delivery/edit/{id}` | `/mypage/delivery/new`,`/:id/edit` | 삭제 |
| roles/request.html | `/roles/request` | `/mypage/role-request` | 삭제 |
| roles/queue.html | `/roles/queue` | `/mypage/role-queue` | 삭제 |
| personal-info.html | `/profile/personal-info` | `/mypage/profile`? | **확인필요** |
| reactivate.html | `/reactivate` | (없음) | **유지-미커버** (휴면 재활성화) |
| coming-soon.html | `/coming-soon` | (없음) | **유지-미커버** (준비중 placeholder) |
| external-login-disabled.html | `/signup/finance-cert`,`/signup/mobile-auth` | (login 탭 일부) | **유지-미커버** (외부인증 연계, 보류군) |
| external-login-mock.html | (개발 mock) | — | **유지-미커버** (dev) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

### donation (26개: 페이지 21 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| donate.html | `/donate` | `/donate` | 삭제 |
| my.html | `/my` | `/mypage/donations` | 삭제 |
| receipts.html | `/receipts` | `/mypage/receipts` | 삭제 |
| designated-list.html | `/designated` | `/designated-donation` | 삭제 |
| designated-detail.html | `/designated/{id}` | `/designated-donation/:id` | 삭제 |
| honor.html | `/honor` | `/honor` | 삭제 |
| honor-certificates.html | `/honor/certificates` | `/mypage/honor-certificates` | 삭제 |
| interest-locgovs.html | `/interest-locgovs` | `/mypage/interest-locgovs` | 삭제 |
| guide/guide1,2,5,6.html | `/guide/*` | `/guide1,2,5,6` | 삭제 |
| guide/list-select.html | `/guide/list-select` | `/list-select` | 삭제 |
| policy/auth,copyright,privacy.html | `/policy/*` | `/policy/:slug` | 삭제 |
| certificate.html | `/certificate` | `/mypage/receipts`? | **확인필요** (기부확인증 화면) |
| honor-estimate.html | `/honor/estimate` | `/mypage/tax-credit-estimate`? | **확인필요** |
| offline.html | `/donations/offline` | `/mypage/donations/offline`? | **확인필요** (대민 조회 vs 담당자 접수) |
| receipt-official-print.html | `/receipts/official/{sn}` | (프린트) | **유지-프린트** (공식영수증 PDF) |
| certificate-print.html | `/certificate/print` | `/print/receipt-certificate`? | **확인필요-프린트** (SPA 라우트 존재) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

### point (8개: 페이지 4 + fragment 4)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| my.html | `/my` | `/mypage/points` | 삭제 |
| reservations.html | `/reservations` | `/mypage/points/reservations` | 삭제 |
| detail.html | `/detail` | `/mypage/points`? | **확인필요** (상세 vs 목록) |
| expire-batch.html | `/batch/expire` | (배치) | **유지-범위밖** (배치) |
| fragments/×4 | — | — | 페이지와 함께 삭제 |

### gift (17개: 페이지 12 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| list.html | `/gifts`(list) | `/gifts` | 삭제 |
| detail.html | `/gifts/{itemId}` | `/gifts/:itemId` | 삭제 |
| events.html | `/events` | `/events` | 삭제 |
| event-detail.html | `/events/{id}` | `/events/:id` | 삭제 |
| wishlist.html | `/wishlist` | `/mypage/wishlist` | 삭제 |
| my-reviews.html | `/my/reviews` | `/mypage/gift-reviews` | 삭제 |
| my-qna.html | `/my/qna` | `/mypage/gift-qna` | 삭제 |
| inquiries.html | `/my/inquiries` | (없음?) | **확인필요** (문의 vs Q&A) |
| my.html | `/my` | (gift 마이 허브) | **확인필요** |
| register.html | `/register` | — | **유지-범위밖** (판매자 등록) |
| edit.html | `/gifts/{itemId}/edit` | — | **유지-범위밖** (판매자 수정) |
| seller-dashboard.html | `/seller/dashboard` | — | **유지-범위밖** (판매자 대시보드) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

### order (16개: 페이지 11 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| cart.html | `/cart` | `/cart` | 삭제 |
| checkout.html | `/checkout` | `/checkout` | 삭제 |
| order-complete.html | `/order-complete` | `/checkout/done` | 삭제 |
| my.html | `/my` | `/orders` | 삭제 |
| detail.html | `/detail/{id}` | `/orders/:orderId` | 삭제 |
| claims/my.html | `/claims/my` | `/claims/my` | 삭제 |
| coupon/my.html | `/my/coupons` | `/my/coupons` | 삭제 |
| coupon/claimable.html | `/coupons` | `/coupons` | 삭제 |
| coupon/offline-claim.html | `/coupons/offline` | `/coupons/offline` | **확인필요** (대민 수령 vs 담당자) |
| create.html | `/`,`/orders` | `/checkout`? | **확인필요** (주문생성 흐름) |
| claims/queue.html | `/claims`(queue) | — | **유지-범위밖** (클레임 처리) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

### 확인필요 9건 판정 확정 (2026-09-17, 화면 대조 완료)

| # | 페이지 | 대조 근거 | 확정 |
|---|---|---|---|
| 1 | member/personal-info | "개인정보 수정" = ProfileView "회원 정보 수정" | **삭제** |
| 2 | donation/certificate | "기부확인증" = ReceiptListView | **삭제** |
| 3 | donation/honor-estimate | "세액공제 예상액" = TaxCreditEstimateView (지자체 추가혜택 섹션은 SPA 미보유·현재 비어있음) | **삭제** |
| 4 | donation/offline | OfflineDonationView(`/mypage/donations/offline`) 존재 | **삭제** |
| 5 | point/detail | "기부포인트 현황" = MyPointsView "기부포인트 조회" | **삭제** |
| 6 | gift/inquiries | "상품문의 관리" = GiftQnaView "답례품 Q&A" | **삭제** |
| 7 | **gift/my** | "내 답례품 관리" + 컨트롤러가 `currentSellerId`/`OP_SELLER` 사용 → **판매자 페이지** | **유지-범위밖** |
| 8 | order/create | "포인트로 주문하기" = CheckoutView "주문/결제" | **삭제** |
| 9 | order/coupon-offline-claim | 쿠폰은 SPA에서 숨김(redirect)+컴포넌트 존재, 일관 | **삭제** |
| + | donation/certificate-print | ReceiptPrintView(`/print/receipt-certificate`) 존재 | **삭제** |

### 확정 삭제 목록 (대민 중복, 54개 페이지)

- **member (13)**: home, login, signup, find-idpw, mypage, profile, personal-info, password, withdraw, delivery/list, delivery/write, roles/request, roles/queue
- **donation (20)**: donate, my, receipts, certificate, certificate-print, designated-list, designated-detail, honor, honor-estimate, honor-certificates, interest-locgovs, guide/guide1·2·5·6, guide/list-select, policy/auth·copyright·privacy
- **point (3)**: my, detail, reservations
- **gift (8)**: list, detail, events, event-detail, wishlist, my-reviews, my-qna, inquiries
- **order (10)**: cart, checkout, order-complete, my, detail, claims/my, coupon/my, coupon/claimable, coupon/offline-claim, create

### 유지 목록 (삭제 금지)

- **유지-프린트**: donation/receipt-official-print (공식영수증 PDF)
- **유지-범위밖(판매자/운영/배치)**: gift/register·edit·seller-dashboard·**my**, point/expire-batch, order/claims/queue
- **유지-미커버(대민이나 SPA 미구현)**: member/reactivate·coming-soon·external-login-disabled·external-login-mock
- **fragments (24)**: **유지** — 위 "유지" 페이지들이 header/footer/floating/alert 프래그먼트를 계속 쓰므로 삭제 불가

### 중요: Thymeleaf 의존성은 5개 서비스 모두 유지

각 서비스에 유지 페이지가 남는다(member 4·donation 1·point 1·gift 4·order 1 + fragments). 따라서 **이번 삭제로 `spring-boot-starter-thymeleaf`를 제거할 수 있는 서비스는 없다.** 목표는 "대민 중복 54개 폐기 + SPA 정본화"까지다.

### 집계 (구버전 추정 — 위 확정본으로 대체됨)

| 판정 | 수 |
|---|---|
| **삭제(커버 확정)** | ~43 페이지 (+ 딸린 fragment 24) |
| **확인필요** | 9 (personal-info, certificate, honor-estimate, donation offline, point detail, gift inquiries·my, order create·offline-claim) |
| **유지-프린트** | receipt-official-print (+ certificate-print 확인필요) |
| **유지-범위밖** | gift register·edit·seller-dashboard, point expire-batch, order claims/queue |
| **유지-미커버** | member reactivate·coming-soon·external-login×2 |

### 실행 결과 (2026-09-17, 1차 완료 — 5개 서비스 전부 컴파일 통과)

**대민 중복 Thymeleaf 폐기 + 뷰@Controller 수술을 5개 서비스 전부 완료했다.** 각 서비스 컴파일 검증(`gradlew compileJava` exit 0).

| 서비스 | 삭제 템플릿 | 삭제/수술 컨트롤러 | 남은 Thymeleaf(=2차 대상) |
|---|---|---|---|
| member | 13 | 4 삭제(Delivery/MyPage/Profile/RoleRequest) + AuthController 수술(find-idpw AJAX만) | login, external-login-disabled/mock, reactivate + fragments×5 |
| donation | 21 (offline 포함) | 8 삭제(Donation/Receipt/HonorBenefit/Guide/Policy/Designated/Offline/FundProject) + InterestLocgov 수술(add/remove AJAX만) | receipt-official-print + fragments×5 |
| point | 3 | PointController 수술(배치만) | expire-batch + fragments×4 |
| gift | 5 (list·detail·wishlist·my-reviews·my-qna) | GiftController 수술(판매자+토글/좋아요/재입고 AJAX만) | events·event-detail·inquiries·my·register·edit·seller-dashboard + fragments×5 |
| order | 10 | 4 삭제(Cart/Checkout/Order/CouponMy) + Claim 수술(운영자 큐만) | claims/queue + fragments×5 |

**SPA가 직접 호출하는 비-`/api` AJAX(유지 필수):** donation `POST /interest-locgovs`·`/interest-locgovs/{code}/delete`(ProfileView/ListSelectView/InterestLocgovsView), gift `POST /wishlist/{itemId}/toggle`(GiftList/Detail/Wishlist). 나머지는 전부 `/api/*` 트윈이 대체.

#### 매핑표 오판 정정 (내용 대조로 발견 — [[verify-screen-by-content-not-route]])

- **gift/inquiries** → 삭제(#6)로 찍혔으나 실제 `GET /my/inquiries`는 `currentSellerId`/`inquiriesForSeller`를 쓰는 **판매자 문의답변** 화면. GiftQnaView(구매자 `/api/my/qna`)와 별개 → **유지-범위밖**.
- **gift/events·event-detail** → 삭제로 찍혔으나 FeaturedController의 **지역이벤트**(featured). SPA `/events`는 customer/EventListView가 **admin `/api/events`**(운영 이벤트)를 부르는 다른 기능 → **유지-미커버**.
- **donation/offline** → 확정삭제목록(20)엔 누락됐으나 OfflineDonationView(`/api/my/donations/offline`)가 대체 → offline.html·OfflineDonationController **삭제 완료**(실제 21개 삭제).

### 2차 진행 현황 (2026-09-17)

**최소범위·완주 우선으로 donation부터.** "지금 의존성을 0으로 만들 수 있는 유일한 서비스는 donation"(남은 라이브 1개가 대민/범위내)이라 gift(판매자 5개가 PL 대기라 막힘)보다 앞세웠다.

- ✅ **donation — Thymeleaf 의존성 완전 제거 완료.** `receipt-official-print`(공식 기부금영수증 조회)를 storefront `OfficialReceiptPrintView.vue`(`/print/official-receipt/:cntrSn`, bare)로 이관. 백엔드는 `GET /api/my/receipts/official/{cntrSn}`(DonationMyApiController) 신설 + `OfficialReceiptController`의 뷰 메서드 제거(직인 합성 PDF `/pdf`·인쇄이력 `/print-log`는 Thymeleaf가 아니라 유지). `MyDonationsView`의 "영수증출력" 링크를 새 프린트 라우트로 교체. 템플릿(fragments 포함)·`spring-boot-starter-thymeleaf` 의존성 제거. compileJava·vite build 모두 통과. **donation 뷰 렌더링 0건 확인.**
- ✅ **member — 휴면해제를 AS-IS와 동일하게 재구현(의존성은 유지).** 1차 시도(`/reactivate` 페이지+아이디/비번 재입력 폼+로그인 링크)는 AS-IS와 달라 사용자 지적으로 폐기하고, AS-IS(`op.saleson.js:1169`) 흐름 그대로 재구현: 로그인 시 휴면회원이면 비밀번호 본인확인 후 서버가 `SLEEP_USER` 반환 → LoginView가 그 자리에서 `confirm("휴면해제 하시겠습니까?")` → `POST /api/auth/recovery`(세션 대기 userId, 자격증명 재입력 없음) → 재로그인 유도. `MemberService.checkCredentials`가 휴면을 하드차단하던 것을 제거(통과시킨 뒤 로그인단에서 분기), `reactivate(loginId,pw)`→`reactivateById(userId)`. `DormancyController`·`reactivate.html`·`ReactivateView`·`/reactivate` 라우트·링크 전부 제거. **login·external-login×3이 남아 member의 `thymeleaf` 의존성은 그대로**(외부인증 보류군).
- ⏸ **나머지 전부 대기**(위 분류대로): point expire-batch·order claims/queue(운영자), gift 판매자 포털 5 + events×2(PL/지역이벤트), member login·external-login(외부연계 보류).

### 2차(살아있는 기능 Vue3 전환) 대상 — Thymeleaf 의존성 제거의 전제

1차로 `thymeleaf` 의존성을 제거할 수 있는 서비스는 **없다**(전부 살아있는 기능이 남음). 2차는 두 부류:
- **대민 미커버(SPA 만들면 즉시 제거 가능):** member `reactivate`(SFR-002 휴면해제)·`external-login`×3(원패스/카카오/금융/간편, 외부연계 보류군), gift `events`/`event-detail`(지역이벤트), donation `receipt-official-print`(공식영수증 프린트·서버PDF).
- **판매자/운영(PL 그룹 결정 대기, 범위밖):** gift `my`/`register`/`edit`/`seller-dashboard`/`inquiries`, order `claims/queue`, point `expire-batch`(운영자 소멸배치).

### 결론 / 다음 단계

- **"5개 서비스 Thymeleaf 완전 소거"는 이 단계로 달성되지 않는다** — 프린트·판매자·운영·미커버 페이지가 남으므로, 대부분 서비스는 `thymeleaf` 의존성을 유지한다. 이 단계의 목표는 **대민 중복 폐기 + SPA 정본화**.
- **삭제 순서**: ① 확인필요 9건을 화면 대조로 확정 → ② 삭제 확정분(≈43+α) Thymeleaf 페이지·뷰@Controller·fragment 제거 → ③ storefront에서 각 화면 정상 확인 → ④ 서비스별 잔여 Thymeleaf 0인 곳만 의존성 제거.
- **확인필요 9건은 삭제 금지** — SPA가 완전 대체하는지 페이지별 대조 후에만.
