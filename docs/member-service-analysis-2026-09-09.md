# 회원(member) 서비스 상세분석 — 2026-09-09

분석 기준: **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본:
- AS-IS: `C:\workspace\ghlove`
  - 화면 `ghlove-frontend/users/*.html`, `ghlove-frontend/mypage/index.html`
  - API `ghlove-api/src/main/java/saleson/api/user/UserController.java`
  - 서비스/매퍼 `ghlove-common/.../shop/user/UserServiceImpl.java`, `sqlmapper/cubrid/user-mapper.xml`
- RFP: `docs/requirements.md` SFR-002
- ISP: `docs/isp-detailed-design-summary.md` (p.403 애그리거트, p.415/427 이벤트스토밍, p.325 SW목록, p.481 계정통합관리)

---

## 1. 요구사항 대비 충족 현황

| RFP SFR-002 항목 | 상태 | 근거 |
|---|---|---|
| 통합 계정체계(일반/재외국민/지자체담당자/답례품제공자/시스템운영자) | 충족 | `USER_TYPE` 공통코드 5건이 RFP 문구와 정확히 일치 |
| RBAC, 승인·해지, 권한 부여·회수 | 충족 | `UserRole`/`UserRoleRequest`, `roles/request`·`roles/queue` |
| 외부 인증 연계(금융인증서/카카오/네이버/간편인증) | 충족(축소) | 5종 모두 경로 구현, 방화벽 미개방으로 `enabled:false` 모의통과 |
| 토큰/세션 관리(만료·재인증), 로그인/로그아웃, 비번변경, ID/PW 찾기 | **부분** | 액세스 120분 + 리프레시 14일 재인증은 있음. **비밀번호 만료 정책은 미구현**(§3-3) |
| 계정 잠금(실패 횟수 초과) | 충족 | 실패 누적 → `STATUS_LOCKED`, 비밀번호 찾기로 해제 |
| 휴면 전환/해제, 탈퇴 | 충족 | `DormancyController`, `reactivate.html`, `withdraw.html` |
| MFA 선택 적용 | 충족 | 마이페이지에서 회원이 직접 on/off |
| 감사 로그(로그인 성공·실패, 권한 변경, 잠금/해제) | 충족 | `LoginLog`/`UserChangeLog`/`UserActionLog` |
| 데이터 파기 절차 | 충족 | `UserDataDestructionLog` |

ISP 대조:
- 애그리거트 분리(Member/Auth/ProviderAccount, p.403) — 도메인 모델은 대체로 대응.
- **도메인 이벤트(회원가입완료/본인인증성공·실패/로그인성공·실패) 미발행**(§3-1).
- **조회모델(회원정보뷰/인증이력뷰/세션뷰) 없음**(§3-2).
- 계정통합관리 Keycloak(p.481) 미도입(§3-5).
- 회원관리 POD SW목록의 MagicLine 4(PKI공인인증, p.325) — 전자서명 기능 미구현(§3-4).

---

## 2. 화면 대응표

| AS-IS | MSA | 비고 |
|---|---|---|
| `users/join.html` | `signup.html` | 대응 |
| `users/login.html` | `login.html` | 대응 |
| `users/find-idpw.html` | `find-idpw.html` | 대응 |
| `users/modify.html` | `profile.html` + `personal-info.html` | 대응(이름·생년월일을 분리) |
| `users/secede.html` | `withdraw.html` | 대응(잔여포인트 경고표 포함) |
| `mypage/index.html` | `mypage.html` | 타일 5종(주문조회/취소반품교환/답례품 후기/답례품Q&A/배송지 관리) 일치 |
| `mypage/deliveryInfo.html` | `delivery/list.html`·`write.html` | 대응 |
| `users/jusoPopup.html` | (없음) | Daum 우편번호 위젯으로 대체 — 의도적 축소 |
| `users/mobile-auth-result.html` | (없음) | 휴대폰 본인인증 콜백. 연계 미개방 — 의도적 축소 |
| `users/onepass-join/result/_secede.html` | 부분 | 로그인 경로만 구현. **연동해지 화면 없음** |
| `users/secede-kakao.html` | (없음) | `withdraw.html` 주석에 축소 사유 기록됨 |
| `users/sign-certificate.html` | **(없음)** | 전자서명 등록/해제 — §3-4 |
| (없음) | `roles/*`, `reactivate.html`, `coming-soon.html` | RFP 요구(RBAC·휴면해제) 대응 신규 |

---

## 3. 기능 갭

### 3-1. member에 이벤트 발행이 전혀 없음 (요구사항 미충족, 중)
`build.gradle` 의존성, `application.yml` 설정, 자바 코드 **모두 Kafka 흔적 0**. RFP 구현원칙 "서비스간 통신은 Kafka 이벤트 발행/구독이 원칙이며 동기 REST 호출은 최소화"와 ISP 이벤트스토밍(회원관리 도메인 이벤트 = 회원가입완료 / 본인인증성공·실패 / 로그인성공·실패)에 어긋난다. 현재 다른 서비스(point/order/donation/admin)는 member를 **전부 동기 REST**로만 호출한다.

### 3-2. ISP 조회모델 3종 없음 (요구사항 미충족, 중)
ISP가 회원관리 조회모델로 "회원정보뷰 / 인증이력뷰 / 세션뷰"를 명시하나 별도 조회 모델 없이 원본 테이블을 직접 읽는다. point 서비스와 같은 유형의 갭.

### 3-3. 비밀번호 변경 주기(만료) 정책 미구현 (재현 누락, 중)
`member.op_user`에 **`password_expired_date`(기본값 '20240101')와 `password_type` 컬럼이 실재**하는데 `User` 엔티티가 둘 다 매핑조차 하지 않는다. AS-IS는 `/passwordtype`, `/changeUserPasswordLater` API와 `UserServiceImpl.getPasswordExpiredDate()`로 비밀번호 변경 시 만료일을 갱신하고 만료 시 변경을 유도하며 "나중에 변경"도 처리한다. RFP의 "인증토큰/세션 관리(만료, 재인증 정책)"에 해당하는 항목.

### 3-4. 전자서명(공동인증서) 등록/해제 없음 (재현 누락, 소~중)
AS-IS `UserController`의 `/signRegister` `/signRemove` `/checkSign` `/getNonce` + `users/sign-certificate.html`. ISP도 회원관리 POD SW 인벤토리에 **MagicLine 4(PKI공인인증, 상용)**를 명시해 근거가 2축에 있다. 다만 상용 PKI 모듈 연계라 다른 외부연계와 같은 성격의 축소로 볼 여지가 있다 — 판단 필요.

### 3-5. Keycloak 미도입 (설계 미반영, 판단 필요)
ISP p.481이 계정통합관리 솔루션으로 Keycloak(OIDC 기반 IAM)을 채택하고 AD/LDAP 연동까지 전제한다. 현재는 서비스별 자체 JWT. 로컬 개발 단계에서는 합리적 생략이나 **클라우드 이관 시 중앙 IAM 도입이 설계 전제**임을 기억할 것.

### 3-6. 외부 인증수단 연동해지 경로 없음 (재현 누락, 소)
AS-IS `onepass_secede.html`(디지털원패스 연동해지), `secede-kakao.html`(카카오 연동해지 후 탈퇴). MSA는 연결(`UserSns`, `User.kakaoUserId`)만 있고 해지 UI가 없다. 소셜 로그인이 모의 상태라 의도적 축소로 볼 수 있으나, 연계가 열리면 필수.

---

## 4. 레이아웃 (AS-IS 기준)

**CSS 링크 누락 문제는 없다.** AS-IS 회원 화면들이 공통으로 링크하는 `layout.css`/`main.css`/`popup.css`/`event.css`/`header-footer_ali.css` 5개가 member 모듈에 없지만, **각 파일이 정의하는 클래스 중 member 템플릿이 실제로 쓰는 것이 있는지 클래스 단위로 대조한 결과 사실상 없다** — 필요한 규칙은 이미 `site.css`/`new.css`/`style.css`에 들어 있다. (order 모듈의 `mypage-order.css` 누락과 달리 여기는 실제 결함이 아님을 확인.)

유일한 실제 갭:

| 항목 | 내용 |
|---|---|
| `.list-none` | `delivery/list.html`의 "등록된 배송지가 없습니다" 문구에 쓰이는데 member CSS에 정의 0건. AS-IS `event.css`는 `text-align:center; padding:90px 0 150px`을 준다 — 지금은 가운데정렬·여백 없이 왼쪽에 붙어 나온다 |

마이페이지 타일 구성은 AS-IS 5종과 일치(쿠폰함은 별건으로 이미 숨김 처리).

---

## 5. 조치 내역 (2026-09-09)

### 3-3 비밀번호 유효기간 정책 (구현)
- `User` 엔티티에 `PASSWORD_EXPIRED_DATE` / `PASSWORD_TYPE` 매핑 추가 — 컬럼은 AS-IS 스키마와 함께 이미 있었는데 매핑이 빠져 정책이 죽어 있었다.
- 주기는 공통코드 `SYSTEM_CONFIG/LIFE_TIME_PASSWORD`(180일)에서 읽고 미설정 시 AS-IS와 같은 180일 폴백 — 하드코딩 금지 원칙.
- 비밀번호가 새로 정해지는 **4개 지점 전부**에서 만료일 갱신: 회원가입, 현장가입(임시비밀번호), 비밀번호 재설정, 비밀번호 변경. 현장가입은 AS-IS와 같이 `PASSWORD_TYPE='T'`로 남겨 다음 로그인에서 변경을 요구한다.
- 로그인 성공 후 만료 또는 임시비밀번호면 `/password?expired=1`로 보낸다(원래 목적지는 `target`으로 이어짐).
- **"나중에 변경"**(AS-IS `changeUserPasswordLater`) — 비밀번호는 그대로 두고 만료일만 다음 주기로 미룬다. 임시비밀번호는 미룰 수 없다. 감사로그 `PASSWORD_CHANGE_POSTPONED` 기록.

**기존 데이터 처리**: `op_user` 35건이 전부 `20240101`이었다. 이건 AS-IS 스키마 이관 시 들어간 placeholder이지 실제 마지막 변경일이 아니라, 그대로 두면 **전 회원이 다음 로그인에서 즉시 만료 처리**된다. 정책 도입 시점부터 유효기간을 시작하도록 미래 날짜로 한 번 밀었다(DDL에도 마이그레이션으로 기록).

검증(실측): 정상 로그인 → `/` · 만료 상태 로그인 → `/password?expired=1` · "나중에 변경" → `/`, 만료일 `20270308`로 연장 · 연기 후 재로그인 정상 · 임시비밀번호는 "임시 비밀번호는 반드시 변경해야 합니다"로 연기 거부 · 감사로그 `PASSWORD_CHANGE_POSTPONED` 기록 확인.

### 4 `.list-none` (구현)
AS-IS `event.css`의 규칙(`text-align:center; padding:90px 0 150px`)을 `site.css`에 추가. 배송지 목록의 "등록된 배송지가 없습니다"가 가운데 정렬로 나온다.

## 6. 잔여 (미조치)

| # | 항목 | 사유 / 필요한 결정 |
|---|---|---|
| 3-1 | 이벤트 발행 전무(Kafka 의존성·설정·코드 0) | 지금 member 이벤트를 구독할 서비스가 없다. 발행만 먼저 넣을지, 소비처(예: admin 통계·감사)와 함께 설계할지 결정 필요 |
| 3-2 | ISP 조회모델 3종(회원정보뷰/인증이력뷰/세션뷰) | point ReadModel이 DB 설계 대기 중이라 같은 시점에 판단 |
| 3-4 | 전자서명(공동인증서) 등록/해제 | 상용 PKI(MagicLine) 연계 필요. 다른 외부연계와 같은 축소로 볼지 결정 필요 |
| 3-5 | Keycloak 미도입 | 인프라 결정. 클라우드 이관 시 중앙 IAM이 설계 전제 |
| 3-6 | 디지털원패스/카카오 연동해지 | 소셜 로그인 실연계 개방 후 착수 |
