---
name: business-exception-logging
description: 업무예외는 각 서비스 XxxException 생성자에서 WARN 자동 로깅 — 로직 구현 시 throw로 던지면 자동 관찰됨
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-17T05:31:45.983Z
---

기능 테스트/로직 구현 시 "앱이 catch해서 처리하는 업무예외(틀린 비번, 검증 실패, 업무규칙 위반 등)"도 **로그로 확인 가능해야 한다**는 사용자 요구.

AS-IS 재현 코드는 업무실패를 전부 커스텀 예외(`MemberException`, `DonationException`, `PointException`, `GiftException`, `OrderException`/`ClaimException`, admin의 `ManagerException`·`QnaException`·`ContentException`·`AccessException`·`CertLoginException`·`CommonCodeException`·`SettlementException`·`RepresentativeBannerException`, `ExternalAuthException`)로 던지는데, 컨트롤러의 **인라인 `try/catch`(중앙 @ControllerAdvice 없음, 40여 곳)**가 로깅 없이 에러응답으로 바꿔 삼켰다.

**적용한 규칙(2026-09-17):** 각 서비스의 위 예외 클래스 **생성자에서 `log.warn("[업무예외] <클래스>: {}", message)`** 로 자동 로깅. throw/catch 위치와 무관하게 남고, 인라인 catch 40여 곳을 안 건드린다. 15개 예외 클래스 전부 적용·컴파일 검증 완료.

**Why:** 업무예외가 200 응답 + 조용히 삼켜져 로그에 안 남으면 로직 검증이 어렵다. 생성자 로깅은 미래 코드까지 자동 커버(새 `throw new XxxException(...)` 는 별도 조치 없이 관찰됨).

**How to apply:**
- 새 업무규칙 실패는 **반드시 해당 서비스 `XxxException` 을 throw** → 자동 WARN. `[업무예외]` 태그로 grep 가능.
- 예외가 아닌 **결과객체/플래그로 실패를 표현하면 로그에 안 남으니**, 그 경로엔 서비스에서 직접 `log.warn` 을 넣어야 한다.
- 새 도메인 예외 클래스를 만들 때도 동일 패턴(생성자 WARN 로깅) 유지.
- 스택은 노이즈라 메시지만. 원인 예외 있는 생성자(`ExternalAuthException(String,Throwable)`)만 cause 포함.
- 관련: SQL은 P6Spy 인라인 로깅([[receipt-commercial-sw-replacement]] 아님), 로컬 실행/로그는 `docs/local-run-guide.md`. AS-IS 대조 원칙 [[as-is-logic-is-the-spec]].
