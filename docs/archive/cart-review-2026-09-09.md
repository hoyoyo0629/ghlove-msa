# 장바구니 상세 분석 및 수정 리포트

작성일: 2026-09-09
대상: `order` 서비스 장바구니/체크아웃, `storefront` 장바구니/주문결제 화면
방식: 전 계층 코드 정독(엔티티·리포지토리·서비스·컨트롤러·SPA·Thymeleaf·DDL) → 문제 분류 → 우선순위 순 수정 → 컴파일/테스트/빌드 검증
검증: `order` compileJava ✓, `CartServiceTest` 13건 전부 통과 ✓, `storefront` vite build ✓
**추후 검토 대상은 §4 잔여과제.**

---

## 0. 요약

| 심각도 | 항목 | 상태 |
|---|---|---|
| 🔴 심각 | 체크아웃 부분 실패 시 유령 SAGA 이벤트 (재고·포인트 영구 누수) | 수정 |
| 🔴 심각 | 화면 결제금액 ≠ 실제 차감 포인트 (배송비·쿠폰할인 미반영) | 수정 |
| 🟠 중간 | `GET /api/cart`가 데이터를 삭제하는 부수효과 | 유지(의도된 동작, 문서화) |
| 🟠 중간 | DB 트랜잭션 안에서 동기 HTTP 호출 / N+1 | 수정 |
| 🟠 중간 | 재고 검증이 결제 버튼까지 미뤄짐 | 수정 |
| 🟠 중간 | 동시 담기 시 UNIQUE 충돌 → 500 | 수정 |
| 🟠 중간 | "30일간 보관" 안내문에 대응하는 정리 배치 없음 | **잔여** |
| 🟠 중간 | 테스트 0건 | 수정(13건 신설) |
| 🟢 낮음 | 절대 URL 하드코딩 / Thymeleaf 중복본 / `buyNow()` 우회 | **잔여** |
| 🟠 중간 | 쿠폰 기능 비활성화 (AS-IS 미사용, 자동발급 중단 + 화면 숨김) | 조치 완료 |

---

## 1. 구조 파악

### 1-1. 구성 요소

| 계층 | 파일 | 역할 |
|---|---|---|
| 엔티티 | `order/.../domain/CartItem.java` | `OD_CART_ITEM` — userId/itemId/quantity |
| 리포지토리 | `order/.../repository/CartItemRepository.java` | 파생 쿼리 4개 |
| 서비스 | `order/.../service/CartService.java` | add / updateQuantity / remove / view / viewSelected / quote / checkout |
| DTO | `order/.../service/CartGroup.java`, `CartLine.java` | 지자체 그룹 / 행 |
| 웹(SPA) | `order/.../web/CartApiController.java` | `/api/cart*` JSON |
| 웹(Thymeleaf) | `order/.../web/CartController.java` + `templates/cart.html` | 동일 로직의 SSR 중복본 |
| 프런트 | `storefront/src/views/order/CartView.vue` | 담기는 `GiftDetailView.vue` |
| 후속 | `CheckoutApiController.java`, `CheckoutView.vue` | review → preview → complete |
| DDL | `database/ddl/service-order.sql` (Round 3) | `UNIQUE(USER_ID, ITEM_ID)` |

### 1-2. 핵심 설계 (유지)

- **장바구니 ≠ 주문.** 체크아웃은 선택된 행마다 `OrderService.createOrder()`를 반복 호출해 **단일품목 주문 N건**을 만든다. `order.saga` 계약을 다중품목으로 바꾸지 않으려는 의도적 선택.
- **지자체 그룹핑이 도메인 제약.** 기부 포인트는 기부한 지자체 답례품에만 쓸 수 있어 `locgovCode`로 묶고 그룹별 잔여 포인트와 비교한다.
- **옵션 없음.** 답례품이 단일 옵션이라 회원+답례품당 1행.
- **인증은 공유 JWT 쿠키.** 과거 `?userId=` IDOR은 `JwtVerifier`로 이미 해소됨. 모든 쿼리에 `AndUserId`가 붙어 있어 재발 여지 없음.

---

## 2. 🔴 심각 (2건, 전부 수정)

### 2-1. 체크아웃 부분 실패 → 유령 SAGA 이벤트

- **원인**: `checkout()`이 한 트랜잭션 안에서 `createOrder()`를 루프로 도는데, `createOrder()`는 저장 직후 **Kafka로 즉시 발행**했다(`AFTER_COMMIT` 아님). 3건 중 3번째에서 재고부족 등으로 실패하면 DB는 3건 전부 롤백되지만 1·2번의 `ORDER_CREATED`는 이미 브로커에 나가 있다. gift는 재고를 예약하고 point는 포인트를 차감하는데 **대응 주문 행이 없어 보상 이벤트조차 발생하지 않는 영구 누수**가 된다.
- **두 번째 트리거**: 동일 쿠폰을 두 행에 선택. 사전 검증 루프의 `previewDiscount`는 둘 다 통과하지만 첫 주문의 `applyToOrder`가 쿠폰을 `USED`로 바꿔 두 번째 행에서 터진다. UI가 중복 선택을 막지 않았다.
- **수정 (2단계)**:
  1. `OrderSagaPublisher.send()`가 트랜잭션 동기화가 활성이면 `afterCommit`에 발행을 등록하고, 아니면 즉시 보낸다. 롤백 시 발행 자체가 없다.
  2. `CartService.priceSelected()`를 신설해 **주문을 한 건도 만들기 전에** 모든 실패 사유를 던진다 — 품절/미승인, 재고부족, 쿠폰 중복선택, 사용불가 쿠폰, 장바구니에 없는 행.
- **파급 주의**: 1번은 장바구니 범위를 넘는다. `publishConfirmed`/`Cancelled`/`DeliveryUpdated`/`ClaimUpdated`도 전부 커밋 이후 발행으로 바뀐다. 어느 쪽이든 롤백 시 발행하지 않는 게 맞는 동작이지만, SAGA 타이밍이 미세하게 뒤로 밀린다.
- 파일: `order/.../event/OrderSagaPublisher.java`, `order/.../service/CartService.java`

### 2-2. 화면 결제금액 ≠ 실제 차감 포인트

- **원인**:

  | | 수정 전 화면 | 실제 `POINT_AMOUNT` |
  |---|---|---|
  | 배송비 | `무료배송` 하드코딩 | `calculateDeliveryFee()` 결과 가산 |
  | 쿠폰할인 | `totalPoint` 고정, 쿠폰 선택해도 재계산 안 함 | `lineTotal - discount` |

  `OrderService`는 이미 배송정책 6종 + 제주/도서산간 추가배송비를 계산하는데 장바구니가 이를 전혀 반영하지 않았다. 게다가 체크아웃의 포인트 충분 검사도 배송비를 뺀 금액으로 해서, **검사는 통과했는데 SAGA에서 포인트 부족으로 주문이 취소되는** 경로가 구조적으로 존재했다.
- **수정**:
  - `CartLine`에 `deliveryFee`/`payable`, `CartGroup`에 `groupDeliveryFee`/`groupPayable` 추가. 포인트 부족 판정을 전부 `groupPayable` 기준으로 교체(SPA·Thymeleaf·서버 3곳 모두).
  - `OrderService.calculateDeliveryFee` → `public deliveryFeeOf(...)`로 승격해 사전 계산에 재사용.
  - **신규 `POST /api/checkout/preview`** → `CartService.quote()`. 결제(`checkout`)와 **같은 `priceSelected()`** 를 타므로 화면 금액과 차감액이 어긋날 수 없다. 쿠폰 할인식(정액/정률·수량비례·최소주문금액·할인상한)을 JS에 복제하지 않기 위해 서버 계산을 그대로 쓴다.
  - `CheckoutView.vue`가 쿠폰 선택·배송지 변경 시 `watch`로 재계산. "무료배송" 하드코딩을 답례품 포인트 / 쿠폰 할인 / 배송비 / 최종 결제 포인트 내역으로 교체.
- 파일: `CartLine.java`, `CartGroup.java`, `CartService.java`, `OrderService.java`, `CheckoutApiController.java`, `CheckoutController.java`, `CartView.vue`, `CheckoutView.vue`, `cart.html`, `checkout.html`

---

## 3. 🟠 중간 (수정한 것)

### 3-1. 트랜잭션 밖으로 + N+1 축소
- `view()`에서 `@Transactional` 제거. 항목 수만큼의 답례품 조회(HTTP)와 그룹 수만큼의 지자체명/포인트 조회를 DB 커넥션을 쥔 채로 하고 있었다.
- 파생 delete는 트랜잭션이 필요하므로 `CartItemRepository.deleteByCartItemIdInAndUserId`에 `@Modifying @Transactional`을 직접 선언 — 기존 주석에 있던 self-invocation 주의사항도 함께 해소됐다.
- `viewSelected()`가 장바구니 전체 대신 선택 행만 조회(`findByCartItemIdInAndUserIdOrderByCreatedDateDesc`).
- `OrderService.createOrder(userId, gift, ...)` 오버로드 추가 → 체크아웃이 같은 답례품을 두 번 조회하지 않는다.

### 3-2. 재고 검증 앞당김
`add`/`updateQuantity`가 `soldOut` 플래그만 보고 `stockQuantity`를 무시해 999999개를 담을 수 있었고 결제 버튼에서야 실패했다. 이제 두 메서드 모두 재고와 상한(`MAX_QUANTITY = 999`)을 검증한다. `add`는 **이미 담긴 수량과 합산한 값**으로 검증한다.

### 3-3. 동시 담기 UNIQUE 충돌
`add()`의 조회-후-저장이 `UNIQUE(USER_ID, ITEM_ID)`와 경합하면 `DataIntegrityViolationException`이 그대로 500으로 나갔다. `add()`에서 `@Transactional`을 떼고(같은 트랜잭션 안에서는 rollback-only라 재시도 불가), 충돌 시 상대가 방금 만든 행을 다시 읽어 합산 저장한다.

### 3-4. 테스트 신설
`order/src/test/java/com/ghlove/order/service/CartServiceTest.java` — **order 모듈 최초의 테스트**. 13건 전부 통과.
덮는 시나리오: 재고초과 담기 / 합산 재고초과 / 수량상한 / UNIQUE 경합 재시도 / 수량변경 재고검증 / 품절 자동삭제 + 배송비 반영 / 선택행만 조회 / 뒤쪽 줄 실패 시 주문 0건 / 동일쿠폰 중복 / 미존재 장바구니행 / 배송비 포함 포인트부족 / 성공 시 줄당 1주문 + 장바구니 비우기 / 미리보기 = 결제 계산 일치.

### 3-5. 유지 결정: `GET /api/cart`의 삭제 부수효과
`view()`가 품절/미승인 항목을 조용히 삭제하는 것은 화면 안내문("장바구니답례품이 품절되면 자동으로 목록에서 삭제됩니다")과 AS-IS 동작에 맞는 **의도된 설계**라 바꾸지 않았다. 다만 조회가 쓰기를 겸한다는 점을 코드 주석에 명시했다. 재검토한다면 §4-4 참조.

---

## 4. 잔여과제 (추후 검토)

### 4-1. "30일간 보관" 정리 배치 부재 🟠
화면 안내문은 "장바구니답례품은 30일간 보관됩니다"라고 하는데, `order` 모듈의 `@Scheduled`는 `CouponBatchScheduler` 하나뿐이라 **장바구니가 무기한 누적**된다.
- 선택지: (a) 안내문대로 30일 경과 행 삭제 배치 신설, (b) 안내문을 실제 동작에 맞게 수정.
- (a)라면 `CREATED_DATE` 기준인지 `UPDATED_DATE` 기준인지 정책 결정 필요. 수량 변경으로 갱신된 행의 보관기간이 연장되는지가 갈린다.
- 운영 데이터를 받아본 뒤 누적 규모를 보고 정하는 게 낫다.

### 4-2. 절대 URL 하드코딩 🟢
`CartController.loginRedirect()`가 `localhost:8081`/`8085`를 하드코딩하고, 썸네일도 `giftServiceBaseUrl + path`로 절대화되어 JSON에 `localhost:8084`가 실려 나간다.
→ **운영 도메인 전환 계획의 "절대 URL 29곳" 정리와 함께** 처리. 단독으로 손대지 않는다.

### 4-3. Thymeleaf 중복본 🟢
`templates/cart.html` + `CartController`, `templates/checkout.html` + `CheckoutController`는 SPA와 완전 중복이다. 이번에는 삭제하지 않고 **SPA와 같은 금액 기준으로 맞춰만 뒀다**(방치하면 두 화면이 서로 다른 금액을 보여준다).
→ admin/storefront 방향 결정 후 중복 템플릿 일괄 정리 시 함께 삭제.
- 주의: 지금은 Thymeleaf 체크아웃에 쿠폰 실시간 미리보기가 없다(폼 제출형이라 쿠폰 할인은 결제 시점에만 확정). 삭제하지 않고 계속 쓸 거라면 이 격차를 메워야 한다.

### 4-4. `buyNow()` 우회 🟢
`GiftDetailView.buyNow()`가 "장바구니에 담고 → 전체 장바구니 조회 → 방금 담은 행 찾기"로 돌아간다. 이미 담긴 상품이면 **수량이 누적된 채** 결제로 넘어간다(1개 사려 했는데 3개 결제).
- 제대로 고치려면 "즉시구매"용 경로(장바구니를 거치지 않는 단건 체크아웃)가 필요해 이번 범위 밖으로 뒀다.

### 4-5. 쿠폰 기능 비활성화 ✅ (2026-09-09 조치 완료)

AS-IS에서도 쿠폰 관련 기능은 현재 사용하지 않는 것으로 확인. **서비스 로직·DB·API·화면 컴포넌트는 전부 남겨두고, 자동발급을 멈추고 화면 진입점만 감췄다**(나중에 다시 켤 수 있어야 하므로 삭제하지 않는다).

**마스터 스위치**: `order/application.yml`의 `ghlove.coupon.enabled: false` 하나가 배치·회원API·회원화면·체크아웃 쿠폰적용을 전부 좌우한다. `true`로 되돌리면 백엔드는 통째로 복구된다(프런트는 별도 3곳).

| 계층 | 대상 | 조치 | 복구 방법 |
|---|---|---|---|
| 발급 | 가입/생일/정기 배치 | `CouponBatchScheduler` `@ConditionalOnProperty` → 빈 미등록 | 프로퍼티 `true` |
| 발급 | 구매후/첫구매 트리거 | `OrderService.issuePurchaseTriggeredCoupons()` 본문 전체 주석 (호출부 유지) | 본문 주석 해제 |
| API | `/api/my/coupons`, `/api/coupons**` | `CouponMyApiController` `@ConditionalOnProperty` → **404** | 프로퍼티 `true` |
| API | Thymeleaf 회원 쿠폰화면 (`/my/coupons`, `/coupons`, `/coupons/offline`) | `CouponMyController` `@ConditionalOnProperty` → **404** | 프로퍼티 `true` |
| API | 체크아웃 쿠폰 노출/적용 | `CouponService.usableIssuesForItem()` 빈 목록 + `previewDiscount()` 예외 → **요청 본문에 쿠폰ID를 직접 실어 보내도 할인이 붙지 않는다**(`applyToOrder`도 이 메서드를 거침) | 프로퍼티 `true` |
| 화면 | `storefront` 라우트 3개 | 주석 처리 + `/mypage`로 redirect (이 SPA엔 404 라우트가 없어 빈 화면 방지) | 주석 해제 + redirect 3줄 삭제 |
| 화면 | storefront 마이페이지 "쿠폰함" 타일 | `MyPageView.vue` HTML 주석 | 주석 해제 |
| 화면 | **member 서비스** 마이페이지 "쿠폰함" 타일 | `member/templates/mypage.html` — order의 `/my/coupons`로 가는 절대링크. 대상이 404가 되므로 함께 주석 | 주석 해제 |
| 화면 | 주문결제 쿠폰 select | `CheckoutView.vue` `const COUPON_ENABLED = false`로 `v-if` 차단. 쿠폰을 못 고르니 할인은 항상 0이라 라인/그룹/합계 할인 표시는 `discount > 0` 조건으로 자동 소멸 | 플래그 `true` |
| 보존 | 화면 파일 3개 | `MyCouponsView.vue`, `ClaimableCouponsView.vue`, `OfflineCouponClaimView.vue` **유지** — 라우트만 끊어 vite 번들에서 빠진다(dist에 coupon 청크 없음 확인) | 라우트 복구 시 자동 |

**의도적으로 남긴 것 — admin 관리 API `/api/admin/coupons**`, `/api/admin/coupon-regular**` (`CouponApiController`).**
- `admin`의 `CouponAdminController`가 `OrderClient`로 이 API를 실제 호출한다. 막으면 admin 쿠폰 화면이 500으로 깨진다.
- 다만 그 화면들은 `fragments/admin-nav.html`에 링크가 없어 **원래부터 메뉴에서 도달 불가**다(URL 직접 입력으로만 접근).
- 즉 회원 쪽은 완전히 차단됐고, 운영자가 URL을 직접 알고 들어가면 기존 쿠폰 데이터를 조회/편집할 수 있는 상태다. 이것까지 막으려면 `CouponApiController`에 같은 `@ConditionalOnProperty`를 달면 되지만, admin 화면이 깨지는 것을 감수해야 한다 — 필요해지면 그때 결정.

**자동발급 배치는 2026-09-09 시점에 이미 껐다(조치 완료).** 화면만 감추면 회원이 볼 수 없는 쿠폰이 매일 쌓이기 때문이다.
- `CouponBatchScheduler`에 `@ConditionalOnProperty("ghlove.coupon.auto-issue.enabled")` 부착 + `application.yml`에 `false` 명시 → 빈 자체가 등록되지 않는다. 코드는 지우지 않았으므로 `true`로 되돌리면 그대로 다시 돈다.
- 중단 대상: 가입쿠폰(`issueSignupCoupons`) / 생일쿠폰(`issueBirthdayCoupons`) / 정기발행(`reissueRegularCoupons`).

**구매 트리거 자동발급도 2026-09-09 중단(조치 완료).** `OrderService.issuePurchaseTriggeredCoupons()`가 주문 확정(SAGA `resolveIfReady`) 시 `issueAfterItemPurchase`(발행시점 4:상품구매후) / `issueFirstPurchaseCoupons`(5:첫구매)를 호출하던 경로다.
- 배치가 아니라 SAGA 확정 흐름 안이라 설정 스위치 대신 **메서드 본문 전체를 주석 처리**하고 경위 주석을 달았다. 호출부(`resolveIfReady`)는 그대로 두고 메서드만 빈 껍데기로 만들었으므로, 되살리려면 **본문 주석만 풀면 된다.**
- `CouponService`의 발급 로직과 DB는 손대지 않았다.

이로써 쿠폰 자동발급 4종(가입/생일/정기 = 설정 OFF, 구매후/첫구매 = 코드 주석)이 전부 멈췄다. **남은 작업은 화면 진입점 숨김뿐이다(위 표).**

### 4-6. 답례품 조회 N+1 (구조적) 🟢
장바구니 항목 수만큼 `GET /api/gifts/{id}`를 동기 호출하는 구조 자체는 남아 있다(중복 호출만 제거). gift에 다건 조회 API(`GET /api/gifts?ids=`)가 생기면 한 번에 줄일 수 있다.

### 4-7. 재검증 필요 🟠
이번 수정은 컴파일·단위테스트·프런트 빌드까지만 확인했다. **6개 서비스 실기동 후 실호출 검증은 하지 않았다.** 특히 아래 두 가지는 실환경에서 확인해야 한다.
- `OrderSagaPublisher`의 `afterCommit` 전환이 gift/point 소비자 쪽 타이밍에 영향이 없는지 (SAGA 전체에 걸리는 변경).
- 배송정책이 실제로 설정된 답례품(`GIFT_SHIPPING_TYPE` 2~6)으로 장바구니 → 결제 → 포인트 차감까지의 금액이 일치하는지. 현재 개발 DB의 답례품은 대부분 무료배송(type 1)이라 배송비 경로가 실데이터로 검증되지 않았다.

---

## 5. 변경 파일

**order (백엔드)**
- `event/OrderSagaPublisher.java` — 커밋 이후 발행
- `service/CartService.java` — 사전검증(`priceSelected`)/미리보기(`quote`)/재고검증/UNIQUE 재시도/트랜잭션 정리
- `service/CartLine.java`, `service/CartGroup.java` — `deliveryFee`/`payable` 추가
- `service/OrderService.java` — `deliveryFeeOf` 공개, gift 재사용 오버로드
- `repository/CartItemRepository.java` — 파생 delete에 `@Modifying @Transactional`, 선택행 조회 메서드
- `web/CheckoutApiController.java` — `POST /api/checkout/preview` 신설, 합계 기준 교체
- `web/CheckoutController.java` — 합계 기준 교체, `totalItemPoint`/`totalDeliveryFee` 추가
- `templates/cart.html`, `templates/checkout.html` — 배송비 표시/주문불가 판정 기준
- `src/test/.../CartServiceTest.java` — 신설(13건)

**storefront (SPA)**
- `views/order/CartView.vue` — 배송비 표시, `payable` 기준 주문불가 판정
- `views/order/CheckoutView.vue` — `preview` 연동, 쿠폰·배송지 변경 시 실시간 재계산
