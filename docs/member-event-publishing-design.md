# member 이벤트 발행 설계 (소비처 포함)

작성 2026-09-22. member 서비스는 현재 Kafka 흔적이 전무 — 발행/소비 모두 없다. 이 문서는
**무엇을 발행하고 누가 소비할지**를 ISP 이벤트스토밍 + 기존 MSA 패턴에 근거해 설계한다.
구현은 §7 결정 확정 후 착수(임의 착수 금지, RFP/ISP 신규는 사용자 확인 필수).

## 1. 근거
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

## 2. 현황 (member가 이미 가진 것)
member는 발행만 없을 뿐, 아래를 **이미 로컬 DB에 기록**한다:
- `LoginLog` — 로그인 성공("Y")/실패("N", 사유메모, IP). MemberService의 로그인 처리 4개 지점에서 기록.
- `UserActionLog` — 회원 액션 감사(AS-IS OP_USER_ACTION_LOG). `AuditLogService.recordAction`.
- `UserChangeLog` — 회원정보 변경 이력.
admin은 회원 관련 소비 리스너가 아직 없다(donation/order/point/gift stats만 소비).
AS-IS `PointServiceImpl:691`에 **"회원가입 포인트"** 적립 로직 존재 → MSA/활성 여부 확인 대상(§4-3).

## 3. 발행 이벤트 설계
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

## 4. 소비처 설계
### 4-1. admin 회원 통계 — `MemberStatsListener` → `StatsService` (ReadModel)
- 집계: 일자별·가입경로별 가입수, 로그인 성공/실패 카운트, 탈퇴/휴면 수 → 운영관리 대시보드.
- 기존 `DonationStatsListener` 패턴 그대로(raw JSON 파싱, group `admin-service`, 로컬 ReadModel 적재).
- **주의**: 이는 신규 ReadModel → [[defer-readmodels-pending-da-design]] 규칙상 **DA 설계 후 구축(보류)**.

### 4-2. 로그인이력/감사 조회 — API vs 이벤트
member가 이미 LoginLog/UserActionLog를 로컬 보관하므로, 운영관리에서 감사 조회 방법 2안:
- **(A) 동기 API** — admin이 member 감사조회 API를 호출(조회·즉시결과 → [[api-vs-kafka-decision-criteria]]상 API 적합). 이벤트 불필요, 원본이 단일 진실.
- **(B) 이벤트 축적** — member 발행 → admin 자체 감사 ReadModel 축적(대량 집계·독립 조회 유리, 그러나 이중 저장).
- **권고**: 감사 **이력 조회 = (A) API**, 통계 **집계 = (B) 이벤트(§4-1)**. → 감사 전용 이벤트 추가발행은 만들지 않음.

### 4-3. point 회원가입 축하 포인트 — [2026-09-23 구현 완료 · 설정 0(AS-IS 동일)]
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

## 5. 정합성 / 순서 / 멱등 / 실패
- 순서: 같은 사용자 관련 이벤트는 동일 토픽+key로 파티션 내 순서보장(기존 donation 교훈 반영).
- 멱등: 소비자는 (userId+eventType+at) 또는 이벤트ID로 중복 무시.
- 실패: 발행 실패는 현행 패턴대로 `whenComplete` 로그(비차단). 트랜잭셔널 아웃박스(정확히-1회)는 향후 과제.

## 6. Phasing (ReadModel-defer 준수)
- **Phase A — 지금 가능(발행만)**: member에 KafkaTemplate + `MemberEventPublisher` + `KafkaTopicConfig` +
  application.yml producer 블록 추가, §3의 5개 이벤트 발행지점 삽입. **publish-only** — 소비자 없어도
  무해(로그로 관찰), 기존 4개 서비스 발행과 동형. RFP "이벤트 원칙" 즉시 충족.
- **Phase B — DA 설계 후**: admin `MemberStatsListener` + `StatsService` 회원통계 ReadModel.
- **보류**: 본인인증 이벤트(외부연계), ProviderAccount 이벤트(포털분리), 감사 이벤트(API로 대체),
  point 축하포인트(활성확인).

## 7. 결정 (2026-09-22 확정)
1. **Phase A만(발행) 지금 진행** — member 발행 인프라 + 5개 이벤트 발행지점. admin 통계 ReadModel
   (Phase B)은 [[defer-readmodels-pending-da-design]]대로 DA 설계까지 보류.
2. **감사 이력 = (B) 이벤트로 admin에 축적** 방향. 단 별도 "감사 전용 이벤트"는 만들지 않는다 —
   §3의 `LOGIN_SUCCEEDED`/`LOGIN_FAILED`/`MEMBER_WITHDRAWN`/`MEMBER_DORMANT`가 곧 감사 소스이므로,
   Phase A 발행이 감사-이벤트 방향을 이미 충족한다. admin 감사 ReadModel 소비는 Phase B(보류).
3. **point 회원가입 축하포인트**: 별도 확인 후 판단(현재 미포함).

## 8. Phase A 구현 대상 (this pass)
- member `build.gradle` spring-kafka 추가, `application.yml` producer 블록, `KafkaTopicConfig`(member.lifecycle).
- `MemberEventPublisher` + 이벤트 record 5종.
- 발행지점: 가입완료 / 로그인 성공 / 로그인 실패 / 탈퇴(일반+연동해지) / 휴면전환.
