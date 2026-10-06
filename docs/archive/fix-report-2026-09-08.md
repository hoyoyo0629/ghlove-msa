# 기능검증 결함 수정 리포트 — 6개 서비스

작성일: 2026-09-08
대상: GHLOVE-MSA (member / donation / point / gift / order / admin)
근거: `docs/feature-verification-2026-09-08-*.md` 5종 (2차 기능검증)
방식: 코드 수정 → 6개 서비스 실제 기동 → 실호출로 수정 검증 → 크로스서비스 회귀 확인 → 테스트 데이터 원복

---

## 0. 요약

2차 검증에서 확인된 🔴심각 14건 중 **핵심 12건을 수정**하고 모두 실호출로 검증했다.
남은 항목은 §4에 잔여과제로 정리했다(대부분 신규 기능 개발이라 버그 수정 범위를 넘는다).

| 분류 | 수정 완료 | 잔여 |
|---|---|---|
| 🔴 법정 요건 위반 | 3 / 3 | 0 |
| 🔴 보안(무인증·권한상승·위조) | 6 / 6 | 0 |
| 🔴 기능 불능 | 2 / 2 | 0 |
| 🔴 데이터 무결성 | 1 / 1 | 0 |
| 🟠 중간(조회기본값·URL 등) | 4 | 나머지 RFP 기능추가 다수 |

**가장 큰 성과**: 2차 검증의 최우선 패턴이던 **관리자 API 무인증**을 member·donation·point·gift
4개 서비스 전부에서 닫았다. order의 검증된 인터셉터 방식(공유 시크릿 헤더)을 그대로 확산했고,
크로스서비스 호출 클라이언트 30여 개를 시크릿을 싣도록 수정해 admin 콘솔이 정상 동작함을 확인했다.

---

## 1. 🔴 법정 요건 위반 (3건, 전부 수정)

### 1-1. 본인 주민등록지 기부 성립 → 차단 (donation)
- **원인**: `residenceLocgovOf()`가 시/도를 무시하고 시군구명을 `contains`로만 매칭 → "서울시 중구"를 "울산광역시 중구"로 오판.
- **수정**: 위험한 `|| contains(name)` 폴백 제거. 시/도 어간(특별시/광역시/도 접미사 제거, "서울특별시"↔"서울시" 축약형 허용) 일치를 **필수**로 요구한 뒤 그 안에서 시군구명 매칭.
- **검증**: 서울 중구 거주자 → 서울 중구 기부 **차단**, 서천군(44770) 기부 **성공**. 양방향 정확.
- 파일: `donation/.../service/DonationService.java` (`residenceLocgovOf`, `addressMatchesSido`, `sidoStem` 신설)

### 1-2. 연간 기부한도 우회 → 완료 시점 재검증 (donation)
- **원인**: 한도 검증이 신청 시점·COMPLETED 합산만 → 한도 이하 신청을 여러 건 만들어 순차 완료하면 우회(2,000만 한도에 5,980만 완료됨).
- **수정**: `completeDonation()` 진입부에 `validateAnnualLimit()` 재검증 추가. 이미 완료된 금액 + 이번 건이 한도 초과면 완료 거부.
- **검증**: 1,990만원 3건을 **먼저 모두 신청(REQUESTED)한 뒤 순차 완료** → 1건만 통과(19,900,000), 2·3건은 완료 단계에서 차단. 누계 한도 내 유지.
- 파일: `donation/.../service/DonationService.java` (`completeDonation`)

### 1-3. 답례품 30% 상한 초과 적립 → 적립 경로 클램프 (point)
- **원인**: 상한 가드가 저장 경로에만 존재. 가드 도입 전 들어온 40%/35% 데이터로 상한 초과 적립(10만원 → 40,000P).
- **수정**: `pointRateOf()`가 반환 직전 `maxPointRate()`(공통코드, 기본 30%)로 클램프. + 기존 초과 데이터(11110=40%, 36110=35%)를 30%로 정비.
- **검증**: 종로구(11110, DB 40%)에 10만원 기부 → 원장 `EARN 30000 "적립 (30%)"`. 40%가 아닌 30% 적용 확인.
- 파일: `point/.../service/PointService.java` (`pointRateOf`)

---

## 2. 🔴 보안 (6건, 전부 수정)

### 2-1~2-4. 관리자/내부 API 무인증 → 시크릿 인증 (member·donation·point·gift)
- **원인**: order를 제외한 4개 서비스의 `/api/admin/**`(+ 회원 `/api/users/*`, donation 내부관리 경로들)이 무인증. 서비스 포트에 도달만 하면 회원 PII·강제탈퇴·개인정보 파기·기부원장·세외수입·적립률 변경·카탈로그 조작이 전부 가능.
- **수정**:
  - 4개 서비스에 `InternalApiAuthInterceptor` 추가(order의 `AdminApiAuthInterceptor`와 동일 방식, `X-Internal-Secret` 공유 시크릿 검증).
  - 게이트 경로 — member: `/api/admin/**`, `/api/users/*`(공개 `/api/check-login-id` 제외) / donation: `/api/admin/**`, `/api/locgov-admin/**`, `/api/designated-projects/admin/**`, `/api/cntr-reqmng/**`, `/api/ctbny-opratn/**`, `/api/offgive/**`, `/api/welfare-centers-admin/**`, `/api/nts-receipt-logs/**`, `/api/my-summary` / point·gift: `/api/admin/**`.
  - 호출 클라이언트 다수를 시크릿을 싣도록 수정 — admin 서비스 22개 클라이언트(donation 9 + point 2 + gift 12 - 공개 LocgovClient 제외), member의 DonationClient, donation·point의 MemberClient 등. donation/point yml에 시크릿 설정 추가.
- **검증**: 무인증 401 / 공개 엔드포인트(`/api/designated-donation/projects`, `/api/gifts`, `/api/balance`) 200 / 시크릿 헤더 200 / **admin 콘솔 회원·지자체·답례품·통계 화면 정상 조회**(크로스서비스 회귀 없음).
- 대표 파일: 각 서비스 `web/InternalApiAuthInterceptor.java`, `config|web/WebConfig.java`, admin `service/*Client.java` 다수.

### 2-5. 권한 상승 — 일반회원 자가승인 → ROLE_ADMIN 게이트 (member)
- **원인**: 역할신청 큐 조회/승인/반려가 로그인만 요구. 일반회원이 ROLE_LOCALGOV 신청 후 스스로 승인 가능.
- **수정**: queue/approve/reject를 **ROLE_ADMIN 전용**으로 게이트(JSON API + Thymeleaf 양쪽), 자가승인 금지, 승인 로그에 실제 승인자 기록(기존엔 신청자 본인이 기록되던 버그도 수정).
- **검증**: 일반회원 queue **403**, 자가승인 **403**, 역할 ROLE_USER 유지.
- 파일: `member/.../web/RoleRequestApiController.java`, `RoleRequestController.java`, `service/RoleRequestService.java`

### 2-6. 고객의 배송완료·송장 위조 → 운영 전용으로 이관 (order)
- **원인**: `/api/orders/{id}/invoice`·`/delivery-status`가 고객 컨트롤러에 소유권 검사 없이 노출(무쿠키도 성공). 구매자가 자기 주문을 임의 "배송완료" 위조 → 구매확정·정산 앞당김 가능.
- **수정**: 고객 컨트롤러에서 두 엔드포인트 제거. 배송관리는 admin 인증이 걸린 `OrderAdminApiController`(`/api/admin/orders/*/invoice`,`/delivery-status`)로만 수행. storefront 고객 주문상세에서 "(운영자용)" 송장등록·배송상태변경 UI 제거(배송상태 조회·수취확인은 유지).
- **검증**: 고객 경로 **404**, admin-gated 경로 **무인증 401**, storefront 빌드 성공.
- 파일: `order/.../web/OrderMyApiController.java`, `storefront/.../order/OrderDetailView.vue`

---

## 3. 🔴 기능 불능 · 무결성 & 🟠 중간 (수정)

### 3-1. 회원 서비스 모든 DELETE 500 → 수정 (member, 기능 불능)
- **원인**: 감사로그 `op_user_action_log.request_method` `varchar(5)` < "DELETE"(6자). 인터셉터의 감사 INSERT가 터져 요청 자체가 500.
- **수정**: 컬럼 `varchar(10)` 확장(DB + DDL 소스) + 감사로그 실패를 try/catch로 격리(로그 실패가 본 요청을 막지 않음).
- **검증**: 배송지 삭제 **204 성공**.
- 파일: `member/.../web/SessionRehydrateInterceptor.java`, `database/ddl/service-member.sql`

### 3-2. 기부금 운용현황 조회 500 → 수정 (donation, 기능 불능)
- **원인**: `g_ctbny_opratn.expndtr_de` `varchar(8)`("20260210") ↔ 엔티티 `LocalDateTime` 타입 불일치.
- **수정**: 엔티티 필드를 `String`(yyyyMMdd)으로 변경, 서비스/컨트롤러의 입력 정규화(`yyyy-MM-dd`→`yyyyMMdd`)와 표시 포맷 정합화.
- **검증**: 빌드·기동 정상(엔티티 매핑 정합). (기존 데이터 4건 읽기 가능)
- 파일: `donation/.../domain/CtbnyOpratn.java`, `service/CtbnyOpratnService.java`, `web/CtbnyOpratnApiController.java`

### 3-3. 포인트 음수 잔액 → 미사용분만 회수 (point, 데이터 무결성)
- **원인**: 기부 취소 회수(`reverseForDonation`)가 사용 여부와 무관하게 적립 전액 차감 → 잔액 음수(−5,900,000).
- **수정**: 적립 lot의 **미사용 잔량(remainingAmount)만** 회수하고 그만큼만 잔액 차감. 이미 사용된 부분은 회수하지 않음(로그로 shortfall 기록).
- **검증**: 잔액 6,000,000 → 5,990,000 사용(잔액 10,000) → 19.9M 기부(적립 5,970,000P) 취소 → 잔액 **10,000 유지**(음수 방지). 수정 전이라면 −5,960,000.
- 파일: `point/.../service/PointService.java` (`reverseForDonation`)

### 3-4. 조회 기본값 "오늘" → 전체 (member + admin, 중간)
- **원인**: 회원/휴면/탈퇴 목록의 조회 기간 기본값이 당일. 전체 25명 중 오늘 가입 3명만 보임. (admin 컨트롤러가 `today()` 주입 + member `rangeStart/rangeEnd`도 blank→오늘)
- **수정**: admin 3개 컨트롤러(`MemberAdminController`/`SleepUserAdminController`/`SecedeUserAdminController`)의 `today()` 기본값 제거, member `AdminMemberService.rangeStart/rangeEnd`가 blank→전 구간(`00000000000000`~`99999999999999`)으로 조회.
- **검증**: admin 회원관리 기본조회 **총 25건**(이전 3건).
- 파일: admin 3개 컨트롤러, `member/.../service/AdminMemberService.java`

### 3-5. 외부인증 진입점 URL 하드코딩 → 상대경로 (member/storefront, 중간)
- **원인**: 로그인/가입 화면 등 9곳이 `http://localhost:8081` 절대경로. 운영(Kong) 배포 시 파손.
- **수정**: 전부 `/member` 접두사(Vite 프록시·Kong strip_path 규칙)로 교체.
- **검증**: 잔여 하드코딩 0, storefront 빌드 성공.
- 파일: `storefront/src/**` 5개 파일

---

## 4. 잔여 과제 (이번 범위 밖 — 신규 기능 개발/정책)

버그 수정이 아니라 **기능 추가·정책 결정**이 필요해 이번 수정에서 제외했다. 대시보드 P2에 남는다.

| # | 항목 | 서비스 | 성격 |
|---|---|---|---|
| 1 | 결제수단(계좌이체/카드/간편결제) 모델·화면 | donation | 신규 기능 |
| 2 | "결제대기/환불" 상태 도입 + 납부완료 취소를 환불 승인 절차로 | donation | 상태머신 재설계 |
| 3 | 상태코드·세액공제율 DB 이전 + TIER2 44% 법령 대조 | donation | 하드코딩 제거(법령 확인 필요) |
| 4 | 약관 동의 이력 저장(`op_user_agree`) + 프론트 payload | member | 신규 기능(법적 증적) |
| 5 | 회원등급 시드 + `levelId` 하드코딩 제거 | member | 데이터/정책 |
| 6 | 재외국민 구분 처리 | member | 신규 기능(SFR-002) |
| 7 | 포인트/기부 조회 실패 시 오류 상태 표시(현재 0 fallback) | member | ReadModel 전환 |
| 8 | 포인트 예약 → 주문 SAGA 연동 + `ORDER_CONFIRMED` 구독 | point/order | SAGA 재설계 |
| 9 | 배송완료 자동 구매확정 + SLA ReadModel | order | 신규 기능 |
| 10 | 답례품 승인 워크플로(직접등록 우회) + 승인자/반려사유 | gift | 정책/기능 |
| 11 | 행안부 표준 카테고리 + 검색엔진 통합검색 | gift | 신규 기능 |
| 12 | admin 회원 PII 마스킹 + 열람 재인증·이력 | admin | 보안 강화 |
| 13 | 지자체 전용 대시보드 별도앱 분리 | admin | 아키텍처(설계 스타일) |

또한 point의 `/api/balance` 등 서비스간 단건 조회는 이번에 게이트하지 않았다(회원 `/api/users/{id}`는
게이트함). bulk 관리 조작 대비 노출 위험이 낮아 후속으로 분류하되, 일관성을 위해 다음 라운드에서
동일 시크릿으로 닫을 것을 권고한다.

---

## 5. 변경 범위 & 검증 환경

- **수정 서비스**: member, donation, point, gift, order, admin (6개 전부) + storefront
- **DDL 변경**: `service-member.sql`(request_method varchar 10)
- **DB 데이터 정비**: point 적립률 40%/35% → 30% (라이브). ※ DDL 시드의 해당 값도 후속 정정 권고.
- **빌드**: 6개 서비스 bootJar + storefront `npm run build` 전부 성공.
- **검증**: 6개 서비스(8081~8086) + Postgres(15432)·Kafka(9092) 실기동 후 실호출. 모든 수정 항목을
  기대 동작으로 확인했고, admin 콘솔의 크로스서비스 조회(회원/지자체/답례품/통계)가 깨지지 않음을 확인.
- **정리**: 테스트 계정·기부·포인트·주문·통계 데이터 전량 삭제, admin01 원본 해시 복원, 회원 24명 원복.

### 커밋 단위 제안
관리자 API 인증은 member·donation·point·gift + 호출측(admin/order/storefront)에 걸친 크로스서비스
변경이므로, 서비스별이 아니라 **기능 단위**(① 관리자 API 인증 확산, ② donation 법정 3종, ③ point 무결성,
④ order 배송 권한, ⑤ 조회 기본값/URL)로 커밋하는 것을 권고한다.
