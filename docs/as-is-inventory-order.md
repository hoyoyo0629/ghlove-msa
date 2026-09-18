# AS-IS 인벤토리 — order 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

## 1. 화면 생사 판정

order 도메인은 화면이 적고 **전부 live**다. 다른 도메인과 달리 백업/날짜본이 `old/`로만 분리돼 있다.

| AS-IS 화면 | inbound | MSA |
|---|---|---|
| `cart/index.html` (장바구니) | 160 | `GET /cart` |
| `order/step1.html` (주문/결제) | 8 | `POST /checkout` |
| `order/step2.html` (주문완료) | 13 | `GET /checkout/done` |
| `mypage/orderList.html` | 28 | `GET /my` |
| `mypage/orderDetail.html` | 4 | `GET /orders/{orderId}` |
| `mypage/orderCancel.html` | 12 | `GET /claims/my` |
| `mypage/deliveryInfo.html` | 9 | member `/delivery` (배송지 관리는 member 배정) |

죽은코드: `cart/old/index_old.html`, `order/old/{no-member_saleson, step1_saleson, step2_saleson}.html`
> 파일명의 `_saleson` 접미사가 원제품 원본이라는 표시다 — point의 `starpoint-mapper.xmlx`, gift의 옵션 쿼리와 같은 계열의 잔재.

**클레임 팝업은 화면이 아니라 컴포넌트다**: `components/ui/modal-order_cancle.vue`(취소), `modal-return.vue`(반품), `modal-exchange.vue`(교환). 목록·상세에서 `getCancelPop`/`getReturnPop`/`getExchangePop`으로 팝업을 열고, 각 모달이 `cancelProcess`/`returnProcess`/`exchangeProcess`로 제출한다.

## 2. 컨트롤러 엔드포인트

### 2-1. `/api/cart` (5)
| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `GET ''` | (직접) | `cart/index.html` | live |
| `POST /add` | `addToCart` | **17개 화면**(목록·상세·카테고리·이벤트·마을기업·지자체선택·제철식품 등) | live |
| `POST /delete` | `deleteCart` | `cart/index.html`, `components/ui/cart.vue` | live |
| `POST /update-quantity` | `updateCartQuantity` | `cart/index.html` | live |
| `POST shipping-payment-type` | `updateShippingPaymentType` | **없음** | **죽은코드** |

### 2-2. `/api/order` — 결제 경로가 두 벌, 살아있는 것은 하나
| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `POST /buy` | `buyOrder` | `cart/index.html:652`, `items/details-main.html`(바로선택), `item_tab-ali.vue`, `users/*`(주문 후 로그인 복귀) | live |
| `POST /payment-step` | `paymentStep` | `order/step1.html:836` | live |
| **`POST /giveGoodsSavePay`** | `giveGoodsSavePay` | `order/step1.html:1836` | **live — 실제 주문저장/결제** |
| `GET /detail` | `getOrder` | `orderDetail`, `writeReview`, `order/step2` | live |
| `POST /confirm-purchase` | `confirmPurchase` | `orderDetail`, `orderList` | live |
| `POST /shpping-complete` | `shppingComplete` | `orderDetail`, `orderList` | live |
| `GET /cancel-apply` | `getCancelPop` | `orderDetail`, `orderList` | live |
| `POST /cancel-apply` | `cancelProcess` | `modal-order_cancle.vue` | live |
| `GET /return-apply` | `getReturnPop` | `orderDetail`, `orderList` | live |
| `POST /return-apply` | `returnProcess` | `modal-return.vue` | live |
| `GET /exchange-apply` | `getExchangePop` | `orderDetail`, `orderList` | live |
| `POST /exchange-apply` | `exchangeProcess` | `modal-exchange.vue` | live |
| `POST /refund-amount` | `getRefundAmount` | `modal-order_cancle.vue` | live |
| **`POST /save`** | `orderSave` | **없음** | **죽은코드** |
| **`POST /pay`** | `pay` | **없음** | **죽은코드** |
| **`POST /cancel`** | `orderCancel` | **없음** | **죽은코드** |
| `GET /coupons` | `getCoupons` | **없음** | **죽은코드** ([[coupon-feature-unused-hide-ui]]와 일관) |
| `POST /redirect-pay` | (래퍼 없음) | **없음** | **죽은코드** |
| `GET /easypay/easypay_request` | — | **없음** | **죽은코드** (원제품 PG) |
| `GET /naverpay/payment` | `naverApiPayment` | **없음** | **죽은코드** (원제품 PG) |

> **`save`/`pay`는 원제품(SalesOn)의 일반 커머스 결제 경로이고, 고향사랑e음이 실제로 쓰는 것은 `giveGoodsSavePay`("답례품 저장+결제")다.** donation의 `/api/ngdonation` vs `/api/regiontax`, point의 `OP_POINT` vs 기부포인트와 **완전히 같은 구조** — 원제품 경로가 남아 있고 고향사랑e음 전용 경로가 실사용된다.

## 3. 매퍼 쿼리 (271 중 사장 21)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `order-mapper.xml` | `saleson.shop.order.OrderMapper` | 115 | **14** |
| `order-claim-apply-mapper.xml` | `...order.claimapply.OrderClaimApplyMapper` | 57 | 2 |
| `order-shipping-mapper.xml` | `...order.shipping.OrderShippingMapper` | 36 | 1 |
| `cart-mapper.xml` | `saleson.shop.cart.CartMapper` | 19 | 1 |
| `order-payment-mapper.xml` | `...order.payment.OrderPaymentMapper` | 17 | 2 |
| `order-refund-mapper.xml` | `...order.refund.OrderRefundMapper` | 9 | 0 |
| `order-add-payment-mapper.xml` | `...order.addpayment.OrderAddPaymentMapper` | 7 | 1 |
| `claim-mapper.xml` | `saleson.shop.claim.ClaimMapper` | 6 | 0 |
| `deliveryhope-mapper.xml` | `...deliveryhope.DeliveryHopeMapper` | 5 | 1 |
| **합계** | | **271** | **22** |

(`order-give-point-mapper` 16건은 point 도메인에서 다뤘다)

### 3-1. 사장 쿼리의 성격 — 배치 취소/반품 경로
`order-mapper` 사장 14건 중 **6건이 `...ForCancelBatch`/`...ForReturnBatch` 계열**이다: `insertOrderItemForCancelBatch`, `insertOrderItemForReturnBatch`, `insertOrderShippingForCancelBatch`, `updateOrderItemForCancelBatch`, `updateOrderItemForReturnBatch`, `updateOrderShippingForCancelBatch`. `updateCancelStatus`·`updateReturnStatus`도 호출 0이다.
→ **배치로 일괄 취소/반품하던 경로가 통째로 죽어 있다.** 실제 클레임 처리는 `OrderClaimApplyServiceImpl`이 건별로 돈다.

나머지: `insertOrderCancelTarget`(XML 고아), `deleteOrderItemSetBuyTemp`, `getActiveItemsByParam`, `getOrderCodeCountForOrderTemp`, `getOrderExchangeApplyByOrderItemId`, `updateOrderPayment`
`order-payment` 사장 2건은 **무통장 입금대기 배치**(`getWaitingDeposit*ForBatch`) — 고향사랑e음은 포인트 결제라 무통장이 없다.
`order-claim-apply` 사장 2건: `getReturnShippingNumberCheck`, `updateReturnStatus`

## 4. MSA 대응 요약

MSA order는 컨트롤러 15개 / 엔드포인트 약 80개. 쿠폰(`CouponApiController` 20 + `CouponMy*` 9)이 큰 비중을 차지하는데, 쿠폰은 [[coupon-feature-unused-hide-ui]]로 **화면 진입점만 숨긴 상태**다(AS-IS `/api/order/coupons`도 호출 0이라 판정이 일치한다).

**대응있음**
- 장바구니 4종 → `/cart`, `/cart/items`, `/cart/items/{id}/quantity`, `/cart/items/delete` (+ API 변형)
- 주문/결제 → `POST /checkout`(검토) → `/checkout/complete`(확정) → `/checkout/done`(완료). AS-IS `paymentStep` → `giveGoodsSavePay` → `step2`와 3단계 구조가 대응한다.
- 주문 목록·상세 → `GET /my`, `GET /orders/{orderId}`
- 클레임 3종(취소/반품/교환) → `POST /orders/{orderId}/claim` + `claimType`, 조회 `GET /claims/my`
- 운영 클레임 처리 → `/claims`, `/claims/{id}/approve|reject|complete` (+ `/api/admin/claims/*`)
- 구매확정 → `POST /orders/{orderId}/confirm-receipt`
- 송장·배송상태 → `POST /orders/{orderId}/invoice`, `/delivery-status`
- 배송지 변경 → `POST /orders/{orderId}/delivery-address`

**의도적축소**
- PG 연계 전부(`easypay`, `naverpay`, `redirect-pay`) — AS-IS에서도 이미 호출 0이라 **사실상 이식 대상이 아니다**
- 무통장 입금대기 배치

**확인필요 / 미해결**
- **`shpping-complete`(배송완료) 대응**: MSA는 `POST /orders/{orderId}/delivery-status`로 운영자가 바꾸는데, AS-IS는 회원이 목록·상세에서 직접 호출한다(`orderList`/`orderDetail`). 주체가 다르다.
- **자동 구매확정**: [[cart-review-remaining-tasks]]에 기록된 order 3-1 "배송완료 자동처리·자동 구매확정" — 정책 결정 대기.
- `getRefundAmount`(환불예상액 계산) 대응 — MSA 클레임에 환불액 미리보기가 있는지
- `POST /api/cart/shipping-payment-type`(배송비 결제방식) — AS-IS도 죽은코드라 이식 불요

## 5. 조치 후보

**A. 확인 필요**
1. 배송완료 처리의 **주체**(회원 vs 운영자) — AS-IS는 회원이 누른다
2. 환불예상액 계산(`refund-amount`) 대응
3. 자동 구매확정 정책 (기존 보류 항목)

**B. 이식 금지 — AS-IS에서 이미 죽은 것**
- `/api/order/{save, pay, cancel, coupons, redirect-pay}`, `easypay`, `naverpay`
- `/api/cart/shipping-payment-type`
- `order-mapper` 배치 취소/반품 6건 + 8건, `order-payment` 무통장 배치 2건
- `cart/old/*`, `order/old/*_saleson.html`

---

## 6. 조치 결과 (2026-09-10)

### 6-1. AS-IS 주문상태 규칙 규명

`mypage/orderList.html:285~345`의 버튼별 `v-show` 조건에서 상태 전이 규칙 전체가 나온다.

| 주문상태 | 의미 | 가능한 액션 |
|---|---|---|
| 10, 20 | 결제완료 / 배송준비 | **주문취소** |
| 30 | 배송중 | **배송완료**(회원이 누른다) |
| 35 | 배송완료 | **구매확정**, **교환신청**, **반품신청** |
| 55 | 교환배송중 | 교환배송완료 |
| 58 | 교환배송완료 | 구매확정 |
| 40 | 구매확정 | 후기작성 |

교환·반품에는 조건이 더 붙는다: `itemReturnFlag == 'Y'`(반품가능 상품) + `mobileItemYn == 'N'`(모바일교환권 제외).
목록 상단 안내(`:180`)가 이 설계를 한 줄로 요약한다 — **"배송중인 답례품은 교환/반품이 불가합니다. 배송완료 버튼 클릭 후 신청 가능합니다."**

즉 AS-IS의 "배송완료"는 배송사 상태 갱신이 아니라 **구매자의 수령 확인**이고, 그것이 교환/반품의 관문이다.

### 6-2. 발견한 갭 3건과 조치

MSA는 `orderStatus`(PENDING/CONFIRMED/CANCELLED)와 `deliveryStatus`(SHIPPED/IN_TRANSIT/DELIVERED/CONFIRMED) **두 축**을 쓰는데, 전이 검증이 `orderStatus`만 보고 있었다.

| # | 갭 | 조치 |
|---|---|---|
| 1 | **발송 후에도 주문취소가 됐다** — `cancel()`이 `orderStatus == CONFIRMED`만 확인했다. 이미 나간 물건의 재고·포인트가 보상 트랜잭션으로 되돌아간다. | `OrderService.isShipped()` 추가 → 발송 이후 취소 차단 |
| 2 | **배송 전에도 반품/교환이 됐다** — `ClaimService.request()`가 배송상태를 안 봤다. | `OrderService.isDelivered()` → 배송완료 이후에만 신청 가능 |
| 3 | **회원이 배송완료를 누를 수 없었다** — MSA는 운영자용 `/delivery-status`만 있었다. AS-IS의 관문이 통째로 빠져 있어, 2번을 고치면 회원이 반품을 신청할 방법 자체가 사라진다. | `OrderService.markDelivered(orderId, userId)` + `POST /orders/{id}/mark-delivered` (+ API 변형). 운영자용과 달리 **본인 주문만**, 목적지는 배송완료로 고정 |

화면도 AS-IS 조건에 맞췄다(`detail.html`): 주문취소는 발송 전에만, 배송완료 버튼은 `SHIPPED`/`IN_TRANSIT`일 때만, 반품/교환 폼은 `DELIVERED` 이후에만. 배송중에는 AS-IS와 같은 안내 문구를 띄운다.

> 2번과 3번은 **한 묶음**이다. 3번 없이 2번만 넣었으면 반품 경로가 막혀버린다 — 상태 전이를 고칠 때 그 상태로 가는 수단이 있는지 함께 봐야 한다.

### 6-3. 환불예상액 (`refund-amount`)

AS-IS의 이 API는 **PG 환불 계산**이 본체다 — `ConfigPg`/`pgType` 분기, `OrderPgData`, kspay 현금영수증, 카드 부분취소(`partCancel`) 수량·금액 계산. MSA는 **포인트 단일 결제**라 그 계산 구조 자체가 성립하지 않는다(환불액 = 결제 포인트 그대로).

그래서 API를 옮기는 대신 **사용자 가치만** 재현했다 — 반품/교환 폼 위에 "반품 승인 시 N P가 복원됩니다. (교환은 포인트 복원 없이 재발송됩니다.)"를 표시한다. 결제 포인트는 이미 결제정보 블록에 있어 별도 조회가 필요 없다.

### 6-4. 검증
order 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 발송 전(`delivery_status` null) 주문에 반품신청 | `배송완료된 주문만 반품/교환을 신청할 수 있습니다...` |
| `SHIPPED` 상태에서 주문취소 | `이미 발송된 주문은 취소할 수 없습니다. 반품/교환을 신청해 주세요.` |
| 배송중 → 회원이 배송완료 | 204, `delivery_status = DELIVERED` |
| 배송완료 후 반품신청 | 204, 클레임 `REQUESTED` 생성 |
| **타인이 배송완료 시도** | `본인 주문만 배송완료 처리할 수 있습니다.` |

검증에 쓴 주문(`O202609101029031357`)의 상태와 생성된 클레임은 원복·삭제 확인.

### 6-5. 미처리
- **자동 구매확정 / 배송완료 자동처리** — [[cart-review-remaining-tasks]] order 3-1. AS-IS 배치 존재 여부 확인 후 정책 결정 필요.
- 교환·반품의 추가 조건(`itemReturnFlag`, `mobileItemYn`) — MSA `Gift`에 "반품 가능 여부" 플래그가 있는지 확인 후 적용.

### 6-6. 교환·반품의 상품 조건 (`itemReturnFlag` / `mobileItemYn`)

두 컬럼 모두 **MSA에 이미 이관돼 있었다** — `op_item.item_return_flag`(DDL 주석 "반품 가능여부 (Y/N)", 기본 `Y`), `op_item.mobile_item_yn`("모바일상품여부", 기본 `N`). `Gift` 엔티티에는 `itemReturnFlag`만 매핑돼 있고 `mobileItemYn`은 빠져 있었으며, 둘 다 **어디서도 읽지 않았다**.

AS-IS는 교환·반품 버튼을 `itemReturnFlag == 'Y' && mobileItemYn == 'N'`일 때만 보여준다(`mypage/orderList.html:318,321`). 모바일교환권은 실물 배송이 없어 반품 대상이 아니다.

- gift: `Gift.mobileItemYn` 매핑 추가, `GiftItemInfoDto`에 두 플래그 포함
- order: `GiftItemInfo.returnable()` — `itemReturnFlag == 'Y' && mobileItemYn != 'Y'`. **값이 없으면 DDL 기본값대로 가능으로 본다.**
- `ClaimService.request()`가 RETURN/EXCHANGE일 때 검사, `OrderController.detail()`이 화면에서 폼을 숨김(조회 실패 시엔 폼을 보여주고 실제 차단은 신청 시 — 표시 실패로 기능을 막지 않는다)

### 6-7. 이 과정에서 잡은 자기 실수 — 취소에까지 배송완료 조건이 걸려 있었다

§6-2에서 넣은 배송완료 조건이 `ClaimService.request()` 진입부에 있어 **`claimType`과 무관하게 모든 클레임에 적용**됐다. AS-IS는 정반대다 — **주문취소는 발송 전(10·20)에만, 교환·반품은 배송완료(35) 후에만** 가능하다. 그대로 뒀으면 클레임 경로의 주문취소가 영영 불가능했다.

유형별로 분기하도록 고쳤다:
```
RETURN/EXCHANGE → isDelivered(order) 필수 + gift.returnable() 필수
그 외(CANCEL)   → isShipped(order)이면 거부  (상품 플래그는 보지 않음)
```

> MSA는 취소 경로가 둘이다 — `OrderService.cancel()`(전용)과 `ClaimService.request(CANCEL)`(클레임). AS-IS는 취소도 클레임(`/api/order/cancel-apply`)으로 처리하므로 **둘 다 같은 규칙**이어야 한다. 한쪽만 고치면 규칙이 갈린다.

### 6-8. 검증 (6-6, 6-7)
| 확인 | 결과 |
|---|---|
| gift API 응답 | `"itemReturnFlag":"Y","mobileItemYn":"N"` |
| 배송완료 + 반품가능 상품 → RETURN | 204 |
| `item_return_flag='N'` → RETURN | `이 답례품은 교환·반품이 불가합니다.` |
| `mobile_item_yn='Y'` → EXCHANGE | 동일 차단 |
| 반품불가 상품의 주문상세 화면 | 반품/교환 폼 블록 **미노출**(0개) |
| 원복 후 | 폼 블록 **노출**(1개) |
| **발송 전 + 반품불가 상품 → CANCEL** | **204 (상품 조건 미적용)** |
| 발송 후 → CANCEL | `이미 발송된 주문은 취소할 수 없습니다...` |

검증에 쓴 주문 상태·클레임과 답례품 플래그는 전부 원복 확인.
