---
name: point-readmodel-paused-pending-db-design
description: point 서비스 CQRS ReadModel 신설을 2026-09-09 중간에 중지 - DB 설계 완료 후 재개. 테이블/엔티티는 이미 만들어졌고 배포는 안 됨
metadata:
  type: project
---

SFR-004 "CQRS 기반 ReadModel 제공" 미충족 건을 착수했다가 **DB 설계가 끝난 뒤 재개**하기로 하고 2026-09-09 중지했다.

**이미 적용된 것 (되돌리려면 이것들을 지우면 된다):**
- 개발 DB(`ghlove_core`, `point` 스키마)에 조회 전용 테이블 2개 생성, 둘 다 **비어 있음**:
  - `pt_rm_locgov_point` (PK: user_id, locgov_code, stdr_year / cntr_amt, earned_amount, used_amount, remaining_amount, lot_remaining, updated_date)
  - `pt_rm_expiring_point` (PK: user_id, expiration_date, locgov_code / remaining_amount, updated_date)
- `database/ddl/service-point.sql` 끝에 위 DDL + 설계 의도 주석 블록 추가
- `point/src/main/java/com/ghlove/point/readmodel/` 에 6개 파일 (엔티티 2, 복합키 2, 리포지토리 2)

**아직 안 한 것:** 프로젝션 서비스(원장에서 재계산), `point.ledger` 자가 구독 리스너, 조회 경로 전환, 기존 데이터 백필, 재동기화 엔드포인트.

**중요:** 자바 코드는 **컴파일·배포하지 않았다.** 8083에 떠 있는 point jar는 이전 빌드라 새 테이블·새 클래스를 전혀 모르며 동작에 영향이 없다. 재개 시 `cd point && ./gradlew bootJar` 후 재기동해야 반영된다.

**Why 중지:** 사용자가 DB 설계를 진행 중이며, ReadModel 테이블 구조가 그 결과와 어긋나면 두 번 일하게 된다.

**How to apply (재개 시 지켜야 할 설계 결정):**
1. 쓰기모델(`PT_POINT_LEDGER`/`PT_POINT_BALANCE`)은 손대지 않는다. 원장행마다 이미 발행되는 `point.ledger` 이벤트를 **point가 스스로 구독**해 해당 회원분을 **통째로 재계산**한다 - 델타 누적이 아니라 전량 재계산이라 Kafka at-least-once 재전달·컨슈머 재시작에도 값이 어긋나지 않는다. 컨슈머 group-id는 admin 것과 겹치지 않게 할 것(현재 point의 기본 group-id는 `point-service`).
2. **차감·예약 가능 여부 판단에는 ReadModel을 쓰지 않는다.** 이벤트 지연만큼 뒤처질 수 있어 초과 사용이 가능해진다 - `deductForOrder`/`reserve`/`confirmReservation`/`usePoints`와 order가 부르는 `/api/balance-by-locgov`는 계속 쓰기모델(`balanceByLocgov`)을 직접 읽는다. ReadModel은 **표시용 조회 전용**(마이페이지 요약·상세, 만료 안내).
3. `remaining_amount`(원장 부호합)와 `lot_remaining`(미소진 lot 합)을 한 행에 나란히 둔다 - 정상이면 같아야 하고, 어긋나면 지자체 없는 차감 같은 사고 신호다([[point-reservation-unused-decision-deferred]]에 적힌 과거 사고와 같은 유형).

분석 원문과 잔여 목록은 `docs/point-service-analysis-2026-09-09.md` §3-1, §7.
