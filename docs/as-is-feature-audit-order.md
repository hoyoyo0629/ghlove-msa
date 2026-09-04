# AS-IS 기능 감사 - order(주문) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(saleson 백엔드 `ghlove-web` + Vue2 `ghlove-frontend`) → `ghlove-msa` order/admin/storefront

## 조사 방법 메모

`ghlove-web`의 고객용 주문/결제 컨트롤러(`saleson.shop.order.OrderController`, `OrderMobileController`)는 **본문 전체가 주석처리된 죽은 코드**임을 확인했다(`private final OrderService orderService;` 필드부터 `step1`/`save`/`pay`/각종 PG(LG Dacom, PAYCO, CJ, KCP, 이니시스, KSPay, easypay) 콜백까지 전부 `//` 처리, `grep -nE "^\t@(Get|Post)Mapping"` 결과 0건). 즉 AS-IS 자체에서 현금 PG 기반 결제 플로우는 실사용되지 않는다 — `sfr_gap_fill_round`/`domain_point_rate_policy` 메모대로 실제 구매수단은 기부포인트이므로 이는 당연한 결과다. 이 감사는 이 죽은 코드 자체의 이관 여부는 묻지 않고(스킵 근거 충족), 살아있는 코드(`OrderManagerController`(admin), `OrderClaimApplyController`(고객 클레임 신청), `CartController`, 쿠폰/정산 컨트롤러 등)와 실제 화면(JSP/Vue2 html)을 기준으로 감사했다.

## AS-IS 기능 전수 목록

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 고객 - 장바구니(지자체별 그룹핑, 포인트잔액 표시) | `ghlove-frontend/cart/index.html`, `saleson.shop.cart.CartController` | **구현됨** | `order/.../web/CartController.java`, `storefront/.../CartView.vue` (`cart_locgov_point_architecture` 메모) |
| 고객 - 주문서작성/결제(PG 연동) | `saleson.shop.order.OrderController`(**전체 주석처리, 죽은 코드**), `ghlove-frontend/order/step1.html` | **의도적 제외 확인됨** | AS-IS 자체가 죽은 코드 - 실구매는 포인트 결제(`CheckoutController`)로 구현됨 |
| 고객 - 네이버페이 간편구매(오픈마켓 연동) | `CartController`(`configPgService.isDisplayNaverPayFlag()` 게이트), `components/ui/naver/naver-pay-button.vue`, `POST /api/open-market/checkOutReturn` | **미구현** | 아래 gap 상세 참고(낮음) |
| 고객 - 주문완료 | `ghlove-frontend/order/step2.html` | **구현됨** | `storefront/.../OrderCompleteView.vue` (`vue3_storefront_migration_round3_order`) |
| 고객 - 주문내역 조회(마이페이지, 기간/답례품명 검색) | `mypage/orderList.html`, `MypageController#order` | **구현됨** | `order/.../web/OrderController.java` `/my`, `storefront/.../MyOrdersView.vue` |
| 고객 - 주문상세(배송정보/운송장/영수확인) | `mypage/orderDetail.html`, `MypageController#order-detail` | **구현됨** | `OrderController#detail`, `OrderDetailView.vue` |
| 고객 - 구매확정(수령확인) | `mypage/orderDetail.html` | **구현됨** | `OrderController#confirmReceipt` |
| 고객 - 배송지 변경 | `mypage/orderDetail.html` | **구현됨** | `OrderController#changeDeliveryAddress` |
| 고객 - 배송지 관리(등록/수정/목록, 기본배송지) | `saleson.shop.userdelivery.UserDeliveryController`(`/delivery/list,write,edit`), `mypage/deliveryInfo.html` | **구현됨** | `storefront` `/mypage/delivery`, `/mypage/delivery/new`, `/mypage/delivery/:id/edit` (`vue3_storefront_migration_round2_mypage`) |
| 고객 - 취소/반품/교환 신청(사유입력) | `saleson.shop.order.claimapply.OrderClaimApplyController`(`/order-claim-apply/{cancel,return,exchange}`) | **구현됨** | `ClaimController#request`, `MyClaimsView.vue` |
| 고객 - 취소/반품/교환 내역 조회 | `mypage/orderCancel.html` | **구현됨** | `ClaimController#myClaims` (`/claims/my`) |
| 클레임 처리방식 - 교환(원 상품↔새 상품 실제 맞교환) | `OrderManagerController` `claim/exchange/process`, `order-item-exchange.jsp` | **의도적 제외 확인됨(코드에 명시)** | `ClaimService` 주석: "교환은 원 주문을 취소 처리한 뒤 사용자가 새로 주문하는 방식(반품+재주문)으로 처리" - 아이템 실물 교환이 아니라 단순화된 반품+재구매 흐름. 의도적 설계로 코드에 이미 문서화되어 있음 |
| 고객 - 쿠폰함(보유쿠폰/사용) | `saleson.shop.coupon.CouponController`, `mypage/old/coupon-list.html` | **구현됨** | `CouponMyController`, `MyCouponsView.vue` (`coupon_subsystem_round`) |
| 고객 - 오프라인쿠폰 등록(쿠폰코드 입력) | `CouponController` | **구현됨** | `OfflineCouponClaimView.vue` |
| 고객 - 체크아웃 쿠폰 할인 적용 | `OrderController`(죽은코드), `CouponController` | **구현됨** | `CheckoutApiController` (실제 포인트차감까지 E2E검증, `coupon_subsystem_round`) |
| **관리자 - 주문 전체 목록(상태별 탭: 신규주문/입금대기/배송준비/배송중/배송완료/구매확정/취소/반품/교환, 기간·회원·지자체 검색)** | `OrderManagerController`(`/opmanager/order/list`,`offList`), JSP 11종(`order/list/all,new-order,shipping-ready,shipping,finish,confirm,cancel,return,exchange,refund,waiting-deposit`) | **미구현** | 아래 gap 상세 참고(높음) |
| **관리자 - 주문 상세(주문정보 변경, 관리자메모)** | `OrderManagerController#detail`,`orderInfoChange`,`changeAdminMemo` | **미구현** | 아래 gap 상세 참고(높음) |
| **관리자 - 클레임 승인/거절/처리큐(반품·교환·취소)** | `OrderManagerController#claimReturnProcess,claimExchangeProcess,claimCancelProcess` (`/opmanager/order/{pageType}/claim/*`) | **부분구현(인증 없음)** | 아래 gap 상세 참고(높음) - `order` 서비스에 `/claims`로 존재하나 운영자 로그인/RBAC 전혀 없음(코드 주석에 명시된 알려진 미비) |
| 관리자 - 클레임메모(반품/교환 건별 상담 메모) | `saleson.shop.claimmemo.ClaimMemoManagerController`(`/opmanager/claim-memo`), `OrderManagerController` `claim-memo/create,list,update` | **미구현** | 위 클레임 처리큐 gap에 포함 |
| 관리자 - 운송장번호 등록/배송상태 변경 화면 | `OrderManagerController` `shipping/change-shipping-number` | **부분구현(인증 없음)** | `order` 서비스에 `POST /orders/{orderId}/invoice`,`/delivery-status` API는 존재하나 코드 주석에 "판매자·운영자가 쓰는 경로라 본인확인 범위 밖(아직 이 MSA에 운영자 로그인 모델 없음)"이라고 명시 - 화면도 RBAC도 없음 |
| 관리자 - 결제수단 변경(무통장↔카드 등) | `OrderManagerController` `{pageType}/change-pay` | **의도적 제외 확인됨(추정)** | 실구매가 포인트 결제라 현금 PG 결제수단 변경 자체가 무의미(원조사 방법론 참고) |
| 관리자 - 입금대기(무통장입금) 목록/취소 | `OrderManagerController` `waiting-deposit` | **의도적 제외 확인됨(추정)** | 상동 - 실결제가 포인트이므로 무통장입금 대기 상태가 발생하지 않음 |
| 관리자 - 전화주문 대리등록(신규주문/모바일신규주문) | `OrderManagerController` `new-order`,`new-order-mobile`, `call-find-user.jsp` | **미구현** | 위 주문관리 gap에 포함(중간 우선순위) |
| 관리자 - 주문대행(order-agency, 위탁업체 조회) | `saleson.shop.orderagency.OrderAgencyController`(`/opmanager/order-agency`) | **미구현** | 위 주문관리 gap과 통합 검토 권장(낮음) |
| 관리자 - 택배사(배송업체) 마스터 관리 | `saleson.shop.deliverycompany.DeliveryCompanyManagerController`(`/opmanager/delivery-company`) | **미구현** | `order`는 `DELIVERY_CARRIER` CommonCode만 사용, 전용 CRUD 화면 없음(낮음, CommonCode로 충분할 가능성) |
| 관리자 - 주문/클레임 관련 엑셀 다운로드(전 상태 탭) | `OrderManagerController` 각 탭별 `*-excel` 매핑 10종 이상 | **미구현** | admin 콘솔 전반에 엑셀 다운로드 자체가 없음(point 서비스 감사에서도 동일 지적, 프로젝트 공통 gap으로 보임) |
| 관리자 - 주문건수 대시보드 위젯 | `CommonController` `opmanager/order-count`,`seller/order-count` | **미구현** | 주문관리 화면이 없으니 위젯도 없음 |
| 판매자(셀러) 콘솔 - 주문관리 | `saleson.shop.order.SellerOrderController extends OrderManagerController` | **미구현** | AS-IS 자체가 admin(OrderManagerController)과 화면/로직 100% 공유(상속) - admin 주문관리 신설 시 자연히 커버 범위 판단 필요, 별도 판매자 인증체계는 프로젝트에 없음 |
| 지자체 송금/정산(답례품 제공자 계좌입금) | `saleson.shop.remittance.RemittanceManagerController` | **구현됨** | `admin/.../SettlementController`(`GENERATED→INVOICED→DEPOSITED→CLOSED`) - AS-IS "지자체송금"과 동일 프로세스로 이미 확인됨(`offgive_and_remittance_rounds` 메모) |
| 관리자 - 쿠폰 CRUD/발행/타겟설정 | `saleson.shop.coupon.CouponManagerController`(`/opmanager/coupon`) | **구현됨** | `admin/.../CouponAdminController.java` (list/new/edit/copy/delete/publish/target-items/target-users/offline-codes/usage - 1:1 대응) |
| 관리자 - 정기쿠폰(회원가입 등 조건부 자동발급) | `saleson.shop.couponregular.CouponRegularManagerController` | **구현됨** | `CouponAdminController` `/coupon-regular/*` |
| 관리자 - 쿠폰 사용내역 조회 | `saleson.shop.couponuse.CouponUseManagerController` | **구현됨** | `admin/.../templates/coupon/usage.html` |
| 관리자 - 택배사 배송추적(SmartDelivery 연계) | 명세상 admin 소관(스코프 분리) | **구현됨** | `admin/.../DeliveryTrackingController`, `SmartDeliveryClient` - 기존 감사 완료 항목(배경 메모 참고) |
| 고객 - 후기작성 유도(주문내역에서 "후기작성" 버튼) | `mypage/orderList.html`(343행,495행,1317행), `orderDetail.html`(215행,856행) - `saleson.shop.item.ItemController#create-review` 링크 | **미구현** | 아래 gap 상세 참고(낮음) |
| 다품목 주문의 품목별 부분취소 | AS-IS `OD_ORDER`가 다품목 주문 지원(`order-item-cancel.jsp` 등 품목단위 처리 UI 존재) | **의도적 제외 확인됨** | `cart_locgov_point_architecture` 메모: 이 프로젝트는 원래부터 단일품목 주문 아키텍처(체크아웃이 N개 독립 단일품목 SAGA로 팬아웃) - 재설계 범위 밖으로 이미 결정됨 |
| 배송비 계산 로직(상품별 shippingType 1~6) | `OpenMarketController`(NaverPay 연동 payload 전용), `step1.html`(죽은 페이지 내 3건) | **의도적 제외 확인됨(신규 확인)** | `cart/index.html`·`orderDetail.html` 등 실사용 페이지에는 배송비 언급이 사실상 없음(0~1건) - 죽은 PG 결제플로우에 종속된 잔재로 판단, 답례품은 사실상 전부 무료배송으로 운영되는 것으로 보임 |
| 주문서 임시저장 | 고객 화면(`step1.html`,`cart/index.html`,`orderList.html`) 어디에도 "임시저장" 문구/버튼 없음 | **해당없음(AS-IS 자체 미존재)** | `CartController#save-order-item-temp`/`ConfigManagerController#order-temp-config`는 죽은 PG 플로우의 세션 연속성용 백엔드 배관일 뿐, 고객 노출 기능이 아님 |
| 재구매 버튼 | 고객 화면 어디에도 "재구매" 문구 없음 | **해당없음(AS-IS 자체 미존재)** | grep 결과 0건 |

## Gap 상세

### 우선순위: 높음

**1. 관리자 - 주문관리 콘솔 전체 부재 (목록/상세/상태변경/클레임 처리큐)**

AS-IS `OrderManagerController`(`ghlove-web/src/main/java/saleson/shop/order/OrderManagerController.java`, 3000줄 이상)는 `/opmanager/order/*` 아래 다음을 제공했다:
- 상태별 탭 11종 목록: 전체(`list`), 오프라인(`offList`), 입금대기(`waiting-deposit`), 신규주문(`new-order`), 배송준비(`shipping-ready`), 배송중(`shipping`), 배송완료(`finish`), 구매확정(`confirm`), 취소(`cancel`), 반품(`return`), 교환(`exchange`)
- 주문 상세: 주문정보 변경(`order-info/change`), 관리자메모(`admin-memo/change`), 품목상세(`item-detail`)
- 클레임 처리: 반품/교환/취소 각각의 처리(`claim/{return,exchange,cancel}/process`), 목록(`claim/{return,exchange,cancel}/list`), 로그(`claim/return-log`,`exchange-log`)
- 클레임메모: `saleson.shop.claimmemo.ClaimMemoManagerController`(별도 컨트롤러, 반품/교환 상담이력)
- 운송장번호 변경(`shipping/change-shipping-number`)
- 전화주문 대리등록: `new-order`,`new-order-mobile` + 회원검색 팝업(`call-find-user.jsp`)
- 각 탭별 엑셀 다운로드 10종 이상

TO-BE `admin/src/main/java/com/ghlove/admin/web/` 디렉토리를 전체 확인한 결과 `Order*Controller` 자체가 존재하지 않는다. `OrderClient`(`admin/.../service/OrderClient.java`)와 `OrderLedger`(`admin/.../domain/OrderLedger.java`)는 통계(`StatsService`)·정산(`SettlementService`)·재동기화(`/stats/resync`) 용도로만 order 서비스를 조회하며, 주문을 검색/열람/상태변경하는 화면은 admin에 전혀 없다.

한편 `order` 서비스 자체에 `ClaimController`(`order/.../web/ClaimController.java`)가 `/claims`(승인큐), `/claims/{id}/approve,reject,complete`를 제공하지만, 코드 주석에 명시된 대로 **완전히 비인증** 상태다:
```
/** /claims (운영자 승인 큐)는 판매자/운영자용 경로라 이 라운드의 "본인 확인" 범위 밖 -
 *  아직 이 MSA에 운영자 로그인 모델이 없다. */
```
`OrderController`의 운송장 등록(`/orders/{orderId}/invoice`)·배송상태 변경(`/delivery-status`)도 동일하게 비인증이다. 이 주석은 `admin_rbac_6tier_role_round`(ROLE_ADMIN_1~6 전면 구현) 이전에 작성된 것으로 보이며, 현재는 admin에 완비된 RBAC 체계가 있으므로 더 이상 "아직 운영자 로그인 모델이 없어서"라는 전제가 성립하지 않는다 - 즉 이 gap은 admin RBAC 완성 후 방치된 실제 결함이다.

**필요 작업**:
1. admin에 `주문관리` 메뉴 신설: 목록(상태별 탭 - 최소 신규주문/배송준비/배송중/배송완료/구매확정/취소·반품·교환은 필요, 입금대기·결제수단변경은 포인트결제 모델상 불필요 판단) + 상세(관리자메모 포함)
2. 클레임 처리큐(`/claims`, `/claims/{id}/approve,reject,complete`)를 order에서 admin으로 이관하거나, 최소한 admin RBAC 미들웨어로 감싸 운영자만 접근 가능하도록 게이트 - 현재 인터넷에 노출된 URL을 아는 누구나 임의 주문의 반품/교환/취소를 승인·거절할 수 있는 상태(보안 결함에 가까움)
3. 운송장번호 등록/배송상태 변경 화면 신설 + 동일하게 RBAC 게이트
4. 클레임메모(상담이력) 엔티티 신규 추가
5. 우선순위는 낮지만 함께 검토: 전화주문 대리등록(신규주문), order-agency, 택배사 마스터관리, 엑셀 다운로드(admin 공통 컴포넌트로 다른 서비스 감사와 통합 권장 - point 서비스 감사에서도 동일 지적됨)

### 우선순위: 중간

**2. 관리자 - 전화주문 대리등록(신규주문)**

AS-IS `OrderManagerController#newOrder`(`new-order`,`new-order-mobile`)는 상담원이 전화로 접수한 주문을 회원 검색(`call-find-user.jsp`) 후 대신 등록하는 기능이다. TO-BE에는 이 경로 자체가 없다. 위 gap #1(주문관리 콘솔)의 하위 기능으로 함께 구현 권장.

**3. 판매자(셀러) 콘솔 주문관리 범위 판단**

AS-IS `SellerOrderController extends OrderManagerController`로 판매자와 운영자가 완전히 동일한 화면/로직을 `ShopUtils.isSellerPage()` 분기로만 나눠 쓴다(셀러는 본인 물품 주문만 필터링). 이 프로젝트에는 판매자 전용 로그인/인증 체계가 없으므로(`donation_receipt_as_is_parity` 등 기존 결정과 일관되게 seller 도메인은 admin 하위 CRUD로만 존재 - `seller` 템플릿 디렉토리 확인됨), gap #1 해결 시 "판매자용 주문조회는 셀러 인증이 별도로 필요한 더 큰 프로젝트"로 분리해 스코프 아웃할지 결정 필요.

### 우선순위: 낮음

**4. 클레임 처리큐 관련 세부기능(클레임메모, 처리로그, 재배송비 계산)**

AS-IS는 반품/교환 처리 시 상담메모(`ClaimMemoManagerController`), 처리이력 로그(`return-log`,`exchange-log` 팝업), 교환 시 재배송비 계산(`re-shipping-amount`)까지 제공했다. TO-BE `Claim` 엔티티(`order/.../domain/Claim.java`)는 `claimId,orderId,claimType,reason,status,createdDate,processedDate`만 가진 매우 단순한 모델이다. gap #1 해결 규모에 따라 함께 검토.

**5. 네이버페이 간편구매(오픈마켓 연동)**

`CartController`가 `configPgService.isDisplayNaverPayFlag()`로 게이트하는 네이버페이 퀵바이 버튼(`naver-pay-button.vue` → `POST /api/open-market/checkOutReturn`)이 AS-IS에 존재한다. `external_integrations_architecture` 메모의 "enabled=false mock-gated" 패턴이 적용될 법한 연계이지만 order 서비스에는 대응 코드가 전혀 없다. 죽은 PG 플로우와 달리 이 버튼은 `cart/index.html`(실사용 페이지)에도 노출되므로 완전한 죽은 코드로 단정하기는 어려우나, 실제 운영 사이트(ilovegohyang.go.kr)에서 노출 플래그가 꺼져 있을 가능성이 높다(교차확인 권장). 확정 전까지 낮은 우선순위로 보류 권장.

**6. 주문내역 화면의 "후기작성" 유도 버튼**

AS-IS `mypage/orderList.html`/`orderDetail.html`은 배송완료/구매확정된 주문 옆에 "후기작성" 버튼을 노출해 `ItemController#create-review{openerReload}/{orderCode}/{itemUserCode}`로 연결한다. TO-BE `storefront/.../MyOrdersView.vue`,`OrderDetailView.vue`에는 이 CTA가 없다(gift 서비스의 답례품후기 작성 화면 자체는 `vue3_storefront_migration_round4_gift`/`round10`에서 이미 구현됨 - 진입 동선만 빠져있음). 낮은 공수로 닫을 수 있는 gap.

**7. 택배사 마스터관리, 주문대행(order-agency)**

`DeliveryCompanyManagerController`(택배사 코드/추적URL 관리로 추정, AS-IS 도메인 필드까지는 미확인)와 `OrderAgencyController`(위탁업체가 지자체 주문건을 조회하는 읽기전용 화면으로 보임)는 실사용 빈도가 낮고 CommonCode로 상당부분 대체 가능해 보여 낮은 우선순위로 분류. gap #1 규모 확정 시 함께 검토.

## 완전히 구현 확인됨 (요약)

- 장바구니(지자체별 그룹핑, 포인트잔액) - `CartView.vue`
- 주문서 작성~완료~조회~상세~구매확정~배송지변경(포인트결제 기반, PG 아님) - `order/.../web/OrderController.java`, `MyOrdersView.vue`, `OrderDetailView.vue`, `OrderCompleteView.vue`
- 배송지(주소록) 관리 - storefront `/mypage/delivery`
- 취소/반품/교환 신청 및 본인 내역 조회 - `ClaimController#request,myClaims`, `MyClaimsView.vue` (교환=반품+재주문 단순화는 코드에 이미 문서화된 의도적 설계)
- 쿠폰함/오프라인쿠폰 등록/체크아웃 할인적용 - `coupon_subsystem_round`에서 이미 E2E 검증
- 관리자 쿠폰 CRUD/정기쿠폰/사용내역 - `CouponAdminController` (AS-IS 3개 컨트롤러와 1:1 대응)
- 지자체송금/정산(답례품 제공자 계좌입금) - `SettlementController` (`offgive_and_remittance_rounds`에서 이미 확인)
- 택배사 배송추적(SmartDelivery) - `DeliveryTrackingController` (배경 메모의 명시적 역할분리)
- 단일품목 주문 아키텍처, 배송비 미적용 - AS-IS 자체 죽은코드/비활성 확인됨(이번 감사에서 신규 확인)
