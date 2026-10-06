---
name: gift-seller-selfservice-parity
description: "판매자(답례품제공자) 셀프서비스 AS-IS 전수대조. 핵심기능 이미 구현+P4 판매자공지 조회 구현 완료. P1~P3·P5(출고/정산/반품=전체주문관리)는 제공자웹분리와 함께 보류, P6~P8 SalesOn 보류."
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T06:02:55.987Z
---

**판매자 셀프서비스** 전수대조+구현 (2026-09-22). 갭목록: `docs/gift-seller-selfservice-parity-audit.md`.

**이미 구현(유지)**: 답례품 등록/수정/옵션·추가구성/재고조정/판매중지/Q&A답변/본인목록+주문현황 대시보드(SellerPortalController, ROLE_PROVIDER JWT)/입점업체 CRUD.

**P4 판매자 공지 조회 — 구현 완료**: AS-IS SellerISysNoticeController(/seller/sys-notice) 재현. 데이터는 admin 소유(op_sys_notice_seller)라 gift→admin 내부호출.
- admin `SellerNoticeInternalApiController`(/api/seller-notices, X-Internal-Secret): 목록(useYn/displayFlag≠N 노출필터)/상세(조회수증가)/첨부다운로드.
- gift `SellerNoticeClient` + `SellerNoticeController`(/seller/sys-notice/list·detail·file-download) + 템플릿 2개(AS-IS 컬럼 No./제목/조회수/등록일시, 공지라벨·첨부, 검색·총N건). config `ghlove.admin-service.base-url=8086` 추가. 시드 admin.op_sys_notice_seller 2건. 컴파일 OK, **재기동 시 활성**.

**보류(결정)**:
- **P5 정정**: AS-IS SellerOrderController extends OrderManagerController = 운영자 주문관리 전체(송장/배송상태/취소) 상속 → 경량 아님, P1과 동일영역.
- **P1~P3·P5(출고/송장·정산·반품출고)**: 현재 MSA admin(운영관리) 전용. 판매자 셀프 이관은 [[provider-portal-split-deferred]](제공자웹 분리 보류)에 걸림 → 보류.
- **P6~P8(mall 판매자몰·magicline 전자서명·item상품관리·sale-edit·temp-process·categoriesfilter)**: SalesOn 종속·미사용 → [[defer-saleson-dependent-unused-features]] 보류+기록.

**⚠ ISP 근거 주의**: "판매자가 직접 출고/정산" 을 ISP 근거로 단정했으나 원본 ISP는 리포에 없고(요약본 docs/isp-detailed-design-summary.md만), 오히려 요약본상 정산=운영관리(admin) 소관(원본 p.259/373/379). 출고 수행주체는 요약본에 명시 없음. → ISP 인용은 원본 확인 후에만.

관련 [[as-is-parity-exhaustive-audit-method]] [[defer-saleson-dependent-unused-features]].
