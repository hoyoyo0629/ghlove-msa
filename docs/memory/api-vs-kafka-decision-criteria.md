---
name: api-vs-kafka-decision-criteria
description: 서비스 간 통신을 API 동기 호출로 할지 Kafka 이벤트로 할지 판단 기준 - 서비스 종류가 아니라 상호작용 성격이 기준(사용자 반복 질문)
metadata: 
  node_type: memory
  type: reference
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-21T00:38:00.834Z
---

**사용자가 반복해서 헷갈려하는 주제.** MSA에서 서비스 간 통신을 **API(동기 RestClient)** vs **Kafka(비동기 이벤트)** 중 무엇으로 할지의 기준. **서비스가 무엇이냐(기부/포인트/답례품)가 아니라 "상호작용의 성격"이 기준**이다 - member도 point/gift/donation을 다 부른다.

**판단 순서(자문):**
1. **조회(값 읽기)인가?** → 무조건 **API**. 이벤트로는 "지금 이 값 줘"가 안 됨.
2. **쓰기인데 호출자가 결과를 즉시 알고 실패 시 자기 작업을 중단/롤백해야 하나?** → **API**. (예: 회원탈퇴 시 포인트 소멸 - 실패하면 탈퇴를 막아야 원장 정합성 유지, 사용자도 화면에서 성공/실패 즉시 수신)
3. **"이미 일어난 사실"을 전파하고 소비 측이 좀 늦게 처리해도 되나?** → **Kafka**. (예: 기부완료→포인트적립 - 기부는 이미 커밋, 적립은 부수효과, point 장애가 기부를 막으면 안 됨=강결합 회피, 지연 허용)
4. **여러 서비스 거치는 다단계 트랜잭션 + 실패 시 보상 필요?** → **Kafka SAGA**. (주문 결제/재고)

**이 코드베이스 실제 분포:**
- Kafka: `donation/event/DonationEventPublisher`→`point/event/DonationEventListener`(기부완료→적립), `admin/event/*StatsListener`(통계 ReadModel=CQRS, 소비자 여럿·실시간 불필요), `order|gift|point/event/OrderSaga*`(주문 SAGA+보상).
- API 동기: `member/service/PointClient·DonationClient·GiftClient`(마이페이지 조회 - 주석에 "의도적인 동기 호출 예외"), **회원탈퇴 시 포인트소멸·관심답례품/관심지자체 삭제**(member.withdraw가 각 서비스 `/api/admin/*` 내부API 동기 호출, X-Internal-Secret 보호).

**한마디:** "내가 지금 결과를 알아야 하나?"→API. "확정된 사실을 알리기만 하면 되나?"→Kafka.

**탈퇴 크로스서비스 에러 처리(현재 설계):** SAGA 보상 없음. 대신 (1) 크로스 호출을 개인정보 삭제보다 앞에 배치(실패 시 회원 ACTIVE 유지→재시도 가능), (2) member 로컬은 @Transactional 롤백, (3) 크로스 작업이 전부 멱등(전량 삭제/소멸)이라 재탈퇴로 최종 수렴. 관련 [[as-is-logic-is-the-spec]].

**상세 참고 문서(2026-09-21 작성):** `docs/cross-service-transaction-patterns.md` — 연산 성격별 3패턴(삭제=순서+멱등재시도 / 더하기=멱등키 cntrSn / 차감·다단계=SAGA 보상)을 실제 코드(withdraw·creditForDonation·order SAGA)와 함께 정리한 팀 참고 문서. 크로스서비스 쓰기 기능 만들 때 이 문서부터 볼 것.
