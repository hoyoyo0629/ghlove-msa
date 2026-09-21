# 크로스서비스 데이터 일관성 패턴 — 여러 서비스에 걸친 쓰기를 어떻게 안전하게 하나

> **읽는 사람:** ghlove-msa에서 **한 요청이 여러 서비스(member/donation/point/gift/order)의 데이터를 바꾸는** 기능을 만들거나 리뷰하는 개발자.
> **한 줄 요지:** 단일 DB의 `@Transactional`은 서비스 경계를 못 넘는다. **연산의 성격(삭제냐, 더하기냐, 되돌려야 하냐)에 따라 다른 도구**를 골라 써라.

작성: 2026-09-21 · 근거 코드는 본문에 `파일:라인`으로 링크.

---

## TL;DR — 연산 성격별 3패턴

| 연산 성격 | 예시 | 멱등한가? | 쓰는 도구 | 대표 코드 |
|---|---|---|---|---|
| **삭제(0으로/제거)** | 회원탈퇴 | 자동 멱등(0을 또 0으로 해도 0) | **크로스 호출을 앞에 배치 + 멱등 재시도** | `MemberService.withdraw()` |
| **더하기(입금/적립)** | 포인트 적립 | 그냥 두면 멱등 아님(더하기+더하기=2배) | **멱등 키**(중복 방지) | `PointService.creditForDonation()` |
| **차감·예약(되돌려야 함)** | 주문 결제(재고차감+포인트차감) | 아님 + 다단계 | **SAGA + 보상 트랜잭션** | order/gift/point `OrderSaga*` |

**핵심 원칙:** "무엇을 하느냐"를 먼저 본다. 삭제는 재시도로, 더하기는 멱등키로, 되돌려야 하는 다단계는 SAGA 보상으로.

---

## 1. 왜 어려운가 — `@Transactional`은 서비스 경계를 못 넘는다

DB 트랜잭션은 **"전부 아니면 전무(All or Nothing)"**를 보장한다. 하지만 **하나의 데이터베이스 안에서만** 작동한다.

- 같은 DB(같은 은행) 안의 여러 작업 → 트랜잭션으로 묶여 안전.
- 다른 서비스(다른 은행)의 작업 → **하나의 트랜잭션으로 못 묶는다.** A 서비스가 B 서비스에게 "너도 롤백해"를 강제할 수 없다.

AS-IS(레거시 SalesOn 모놀리스)는 모든 데이터가 **하나의 CUBRID DB**에 있어서 이 고민이 없었다(탈퇴 도중 어디서 터지든 전부 롤백). MSA로 쪼개면서 이 "공짜 원자성"을 잃었고, 그래서 아래 패턴들이 필요해졌다.

> **HTTP 호출(RestClient)이든 Kafka 이벤트든, 다른 서비스에서 이미 커밋된 변경은 우리 트랜잭션 롤백으로 되돌아오지 않는다.** 이 문장이 이 문서 전체의 출발점이다.

---

## 2. 패턴 1 — 삭제: 크로스 호출을 앞에 배치 + 멱등 재시도

**대표 사례: 회원탈퇴** [`MemberService.withdraw()`](../member/src/main/java/com/ghlove/member/service/MemberService.java)

탈퇴는 4곳의 데이터를 건드린다: member(개인정보/CI/권한), point(포인트 소멸), gift(관심답례품 삭제), donation(관심지자체 삭제). 이 중 point/gift/donation은 HTTP 호출이라 우리 트랜잭션에 못 묶인다.

### 실행 순서 (이 순서가 안전장치다)

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

### 안전장치 3종

1. **순서 배치** — 크로스 호출(③④⑤)을 **개인정보 삭제(⑥~) 앞에** 둔다. 남의 은행에서 실패하면 개인정보는 아직 안 건드린 상태라 **깨끗하게 재시도**할 수 있다. ([주석 근거](../member/src/main/java/com/ghlove/member/service/MemberService.java))
2. **`@Transactional`(내 은행 묶기)** — member 내부(⑥~⑩)는 여전히 "전부 아니면 전무". 반쪽짜리 member 데이터는 안 생긴다.
3. **멱등성** — 크로스 작업이 전부 전량 삭제/소멸(`deleteByUserId`, 잔여 전량 소멸)이라 **몇 번 실행해도 결과가 같다.** 재시도가 안전하게 완결된다.

### 부분 실패 시나리오

| 어디서 터지나 | 결과 | 수습 |
|---|---|---|
| ③ point 소멸(맨 앞) | 개인정보 안 건드림, 회원 ACTIVE | 재시도하면 흔적 없이 처음부터 |
| ④/⑤ 중간 크로스 | 포인트만 소멸됨 + 회원 ACTIVE (중간상태) | 재시도 → 멱등이라 남은 것만 마저 처리 |
| ⑦ member 저장 중 | 크로스 3개는 됨, member는 @Transactional로 롤백 | 재시도 → 멱등이라 안전 완결 |

**한계:** 사용자가 재시도를 안 하고 포기하면 "포인트만 소멸 + 회원 살아있는" 상태가 남을 수 있다. 다만 (a) 최종 상태는 재시도 한 번이면 수렴하고, (b) 소멸된 포인트를 "복원"하는 건 오히려 위험하므로 되돌리지 않는 게 맞다. 치명적 데이터 파괴가 아니라 고객센터로 복구 가능한 수준.

---

## 3. 패턴 2 — 더하기: 멱등 키(idempotency key)

**대표 사례: 포인트 적립** [`PointService.creditForDonation()`](../point/src/main/java/com/ghlove/point/service/PointService.java)

더하기(입금/적립)는 **멱등이 아니다.** "1만원 더해"를 두 번 하면 2만원이 된다. 재시도가 곧 **중복 입금**이 된다. Kafka는 같은 이벤트를 두 번 배달할 수 있으므로(at-least-once) 이 문제는 실제로 발생한다.

### 해결: 거래마다 고유 번호를 붙이고 "이미 했니?"를 먼저 묻는다

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

## 4. 패턴 3 — 차감·다단계: SAGA + 보상 트랜잭션

**대표 사례: 주문 결제** (order 오케스트레이터 + gift 재고 + point 포인트)

주문은 두 단계를 거친다: **① gift 재고 예약(차감) → ② point 포인트 차감.** 둘 다 "되돌려야 하는" 연산이고 여러 서비스에 걸쳐 있어, 하나의 트랜잭션으로 못 묶는다. → SAGA로 처리한다.

**SAGA란:** 긴 거래를 하나의 트랜잭션 대신 **각 단계는 자기 DB에 바로 커밋**하고, **중간에 실패하면 앞 단계를 거꾸로 되돌리는(보상)** 방식.

### 성공 흐름

```
order  : 주문 생성(PENDING) ──[ORDER_CREATED]──▶
gift   : 재고 예약 성공        ──[STOCK_RESERVED]──▶ order
point  : 포인트 차감 성공      ──[POINT_DEDUCTED]──▶ order
order  : 두 결과 다 성공 → 주문 확정(CONFIRMED)
```
[`OrderService.resolveIfReady()`](../order/src/main/java/com/ghlove/order/service/OrderService.java) — `RESERVED && DEDUCTED`면 확정.

### 실패 흐름 (핵심 — 보상)

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

### 오케스트레이터 패턴

여기서 **order 서비스가 "지휘자"**다. 각 단계 결과를 `Order` 행에 모으고([`onStockReserved`/`onPointDeducted` 등](../order/src/main/java/com/ghlove/order/service/OrderService.java)), 둘 다 모이면 판정 → 성공이면 확정, 실패면 보상 지시(CANCELLED). gift와 point는 서로를 모르고, 가운데서 order가 취합·지휘한다.

사용자 요청에 의한 **확정 주문 취소**도 같은 메커니즘을 재사용한다: order가 `ORDER_CANCELLED`를 발행하면 gift/point가 각자 재고·포인트를 보상한다([`OrderService` 주문취소](../order/src/main/java/com/ghlove/order/service/OrderService.java)).

---

## 5. 곁들임 — API 동기 호출 vs Kafka 이벤트, 언제 뭘 쓰나

크로스서비스 통신 수단(동기 API vs 비동기 Kafka)도 **상호작용 성격**으로 고른다(서비스 종류가 아님):

1. **조회(값 읽기)?** → 무조건 **API 동기**. (마이페이지 잔액/목록 등 - `member/service/PointClient·DonationClient·GiftClient`)
2. **쓰기인데 호출자가 결과를 즉시 알고 실패 시 자기 작업을 중단해야?** → **API 동기**. (탈퇴 시 포인트소멸·관심삭제 - 실패하면 탈퇴 중단)
3. **"이미 일어난 사실"을 전파하고 소비 측이 늦게 처리해도?** → **Kafka**. (기부완료→적립 - point 장애가 기부를 막으면 안 됨)
4. **여러 서비스 거치는 다단계 + 실패 시 보상?** → **Kafka SAGA**. (주문 결제)

---

## 6. 의사결정 기록 — 왜 탈퇴엔 SAGA를 안 썼나

탈퇴는 전부 **삭제** 연산이라 **되돌릴 게 없다.** 삭제한 것을 "보상"으로 되살리는 것은 오히려 위험하다(지운 개인정보를 되살린다?). 삭제는 자동으로 멱등이라 **순서 배치 + 멱등 재시도**로 충분히 안전하다. 반대로 주문은 재고·포인트를 **빼는(되돌려야 하는)** 연산이라 SAGA 보상이 반드시 필요했다.

> SAGA는 "복잡한 게 항상 더 안전"해서 쓰는 게 아니라, **되돌려야 하는 연산이 여러 서비스에 걸쳐 있을 때** 쓰는 것이다. 삭제·멱등 연산에 SAGA를 얹으면 복잡도만 늘고 이득이 없다.

---

## 7. 새 크로스서비스 기능을 만들 때 체크리스트

- [ ] 이 요청이 바꾸는 데이터가 **몇 개 서비스**에 걸쳐 있나? 한 서비스면 그냥 `@Transactional`로 끝.
- [ ] 각 크로스 작업의 **연산 성격**은? (삭제 / 더하기 / 차감·예약)
- [ ] **삭제**면: 크로스 호출을 로컬 파괴적 변경보다 **앞에** 두고, 각 작업을 **멱등**(전량 기준)으로 만들었나?
- [ ] **더하기/차감**이면: 도메인 **고유번호(멱등 키)**를 실어 보내고, 받는 쪽이 "이미 처리함"을 확인하나?
- [ ] **되돌려야 하는 다단계**면: 각 단계의 **보상(반대 작업)**을 정의했고, 오케스트레이터가 결과를 취합해 실패 시 보상을 지시하나?
- [ ] 통신 수단(§5): 즉시 결과가 필요하면 API, 확정된 사실 전파면 Kafka.
- [ ] 부분 실패 시 남는 "중간 상태"를 적어보고, 그게 **재시도/보상으로 수렴**하는지 확인했나?

---

관련 문서/메모: 이 문서는 세션 대화(2026-09-21, 회원탈퇴 크로스서비스 처리 검토)에서 정리됨. 통신 수단 판단 기준은 팀 내부 메모 `api-vs-kafka-decision-criteria`와 동일 원칙.
