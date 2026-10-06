# 판매자 셀프서비스 — AS-IS 전수대조 갭목록 + 실사용 판정

작성 2026-09-22. 방식: [[as-is-parity-exhaustive-audit-method]] + [[defer-saleson-dependent-unused-features]].
연관 결정: [[provider-portal-split-deferred]](제공자웹 분리는 현행 gift 내 /seller/* 유지·보류), [[gift-seller-portal-unauthenticated]].

## 1. AS-IS 답례품제공자(seller) 기능 전수 (`seller/i18n/*`, `*SellerController`)

| AS-IS 메뉴 | 컨트롤러 | 성격 |
|---|---|---|
| gift-item 답례품 등록/관리 | GiftItemSellerController | 답례품 live |
| item 상품관리 | SellerItemController | SalesOn 원제품 |
| order 주문관리 | SellerOrderController | 답례품 live |
| shipment 출고/송장 | SellerShipmentController | 답례품 live |
| shipment-return 반품출고 | SellerShipmentReturnController | 답례품 live |
| remittance 정산 | SellerRemittanceController | 답례품 live |
| qna/qna-item/qna-locgov | SellerQnaController 등 | 답례품 live |
| notice/sellerNotice 공지 | SellerNoticeController 등 | 답례품 live |
| mall 판매자몰 | MallController | SalesOn(판매자 개별몰) |
| user 계정 | SellerUserController | member 영역 |
| magicline 전자서명 | MagicLineSellerController | SalesOn/외부 |
| sale-edit, temp-process | - | SalesOn/임시 |
| categoriesfilter | CategoriesFilterSellerController | SalesOn |

## 2. TO-BE(MSA) 현재 커버리지

**이미 구현(인증 기반, gift 내 /seller·/my)** — GiftController + SellerPortalController + SellerApiController:
- 답례품 **등록/수정**(/register, /gifts/{id}/edit), **옵션·텍스트옵션·추가구성 편집**(이번 라운드), **재고조정**(/stock), **판매중지**(/stop·/discontinue)
- **본인 답례품 목록 + 주문현황 대시보드**(/seller/dashboard, ROLE_PROVIDER JWT 인증)
- **Q&A 답변**(/my/inquiries, /inquiries/{id}/answer)
- 입점업체(Seller) admin CRUD(SellerApiController)

**현재 admin(운영관리) 전용 — 판매자 셀프 아님**:
- **송장/출고 등록**: order `/api/admin/orders/{id}/invoice`. OrderMyApiController:307~311 주석 "송장등록·배송상태변경은 admin으로만 수행" 명시.
- **정산(remittance)**: admin `settlements` + SettlementService + Settlement/OrderLedger.
- **반품출고**: order DeliveryReturnAdminApiController(admin).

## 3. 갭목록

| # | 갭 | AS-IS | MSA | 판정 |
|---|---|---|---|---|
| P1 | 판매자 출고/송장 등록 | SellerShipmentController(판매자가 송장입력) | admin 전용 | **live 갭 — 판매자 이관 여부 결정 필요** |
| P2 | 판매자 정산 조회 | SellerRemittanceController | admin 전용 | **live 갭 — 결정 필요** |
| P3 | 판매자 반품출고 처리 | SellerShipmentReturnController | admin 전용 | **live 갭 — 결정 필요** |
| P4 | 판매자 공지 조회 | SellerISysNoticeController(/seller/sys-notice: 목록/상세/첨부다운로드) | **구현 완료(2026-09-22)** | ✅ |
| P5 | 판매자 주문관리 | SellerOrderController **extends OrderManagerController**(=운영자 주문관리 전체: 송장/배송상태/취소 포함) | 대시보드 조회만 | **정정: P1과 동일 영역(전체 주문관리 상속) → 경량 아님, P1~P3와 함께 보류** |
| P6 | mall 판매자몰 | MallController | 없음 | **SalesOn 종속 → 보류+기록** |
| P7 | magicline 전자서명 | MagicLineSellerController | 없음 | **SalesOn/외부 → 보류+기록** |
| P8 | item 상품관리·sale-edit·temp-process·categoriesfilter | - | 없음 | **SalesOn 종속 → 보류+기록** |
| P9 | user 계정관리 | SellerUserController | member 영역 | member 서비스 소관(별도) |

## 4. 결론 / 권고 (결정 필요)

- **핵심 판매자 기능(등록/수정/옵션/재고/판매중지/Q&A/주문현황)은 이미 인증 기반으로 구현**되어 있다 → 유지.
- **P6~P8(mall·magicline·item·sale-edit 등 SalesOn 종속)은 현재 미사용 → 보류+기록**(DA 설계 대기).
- **P1~P3(출고/송장·정산·반품출고)은 답례품 live 기능이나 현재 MSA는 admin(운영관리) 전용**이다. 이걸 판매자 셀프로 이관/추가하는 것은 **[[provider-portal-split-deferred]](제공자웹 분리 보류) 결정**에 걸린다 → 보류.
  - ⚠ **정정(2026-09-22)**: 이전 판에 "ISP상 판매자가 직접 출고/정산하는가 범위"라고 적었으나, **원본 ISP 문서는 리포지토리에 없고(요약본 docs/isp-detailed-design-summary.md만 존재), 특정 페이지로 뒷받침되지 않는다.** 오히려 요약본상 **정산(op_settlement)은 "운영관리(admin) MSA" 소관**으로 설계됨(원본 p.259/373/379). 출고/운송장등록은 "주문관리 MSA" 기능으로 나열되나 수행 주체(판매자 vs 운영자)는 요약본에 명시 없음. 판매자 출고/정산 주체 근거는 AS-IS 소스(Seller*Controller)와 접근권한 매트릭스(답례품제공자 롤·UI_S* 화면ID)뿐.
- **사용자 결정(2026-09-22)**: P4만 지금 구현(P5는 위 정정으로 P1과 동일영역 판명 → 보류), P1~P3·P5는 제공자웹 분리 결정과 함께 보류, P6~P8(SalesOn)은 기록+보류.

## 5. P4 구현 (2026-09-22, 완료)

AS-IS `SellerISysNoticeController`(/seller/sys-notice) 그대로 재현. 판매자 공지 데이터(admin op_sys_notice_seller)는 admin 소유라 gift 셀프포털이 admin 내부 API를 호출한다.
- **admin** `SellerNoticeInternalApiController`(/api/seller-notices): 목록(노출필터 useYn/displayFlag≠'N', 제목·기간 검색)/상세(조회수 증가)/첨부다운로드. X-Internal-Secret 가드.
- **gift** `SellerNoticeClient`(gift→admin, X-Internal-Secret) + `SellerNoticeController`(/seller/sys-notice/list·/detail/{id}·/file-download/{fileId}, JWT ROLE_PROVIDER 인증) + 템플릿 `seller-notice-list.html`(공지사항, No./제목/조회수/등록일시, 공지 라벨·첨부표시, 총 N건, 제목·기간 검색)·`seller-notice-detail.html`(제목/등록일시/조회/내용/첨부자료). 대시보드에 "공지사항" 링크 추가. gift config `ghlove.admin-service.base-url` 추가.
- admin·gift 컴파일 OK. 검증 시드: admin.op_sys_notice_seller 2건(1002 공지·1003 일반). **재기동 시 활성**.
