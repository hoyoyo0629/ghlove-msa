---
name: signup-point-built-but-off
description: "회원가입 축하포인트 - AS-IS 동일하게 구현하되 설정값 0(비활성)으로 둠. point가 MEMBER_JOINED 구독해 적립, POINT_JOIN 양수로 바꾸면 켜짐"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-23T01:07:56.754Z
---

**2026-09-23 구현 완료 · 설정 0(AS-IS 동일).** 사용자 지시: "as-is하고 동일하게 기능 구현해놓고 설정도 동일하게 0으로". [[as-is-parity-includes-disabled-state]] 원칙(기능은 만들되 AS-IS에서 꺼져 있으면 우리도 꺼둠)에 부합.

- **동작**: member가 `member.lifecycle`로 발행하는 `MEMBER_JOINED`를 point의 `MemberEventListener`(group point-service)가 구독 → `PointService.creditForSignup(userId)`.
- **금액**: `SYSTEM_CONFIG/POINT_JOIN`(point.OP_COMMON_CODE, =AS-IS OP_CONFIG.POINT_JOIN, 관리자 config/point.jsp "회원가입시 포인트"). **현재 '0' = 미적립**(AS-IS `earnPoint("join")`의 `point!=0` 가드 재현). 시드: `database/ddl/migration-point-signup-config.sql`.
- **멱등/스코프**: REF_KEY `JOIN-<userId>`로 회원당 1회, locgov 미연결(AS-IS DEFAULT_POINT_CODE), 만료는 openLot 공통.
- **켜는 법**: `point.op_common_code`의 POINT_JOIN code_value를 양수로. **기능검증**은 임시로 양수 설정 → 가입 → 마이페이지/point 잔액에 "회원가입 포인트" EARN 확인 → 0으로 복원(설정은 DB에서 live 읽어 재기동 불요).
- AS-IS가 실제 0(비활성)인 근거: 리포에 POINT_JOIN 시드 없음 + 사용자가 가입 테스트서 적립 본 적 없음. 상세 `docs/member-event-publishing-design.md` §4-3.
- 관련: [[member-service-deferred-items]](이벤트 발행 Phase A), [[defer-saleson-dependent-unused-features]](원래 보류였다가 사용자 지시로 구현).
