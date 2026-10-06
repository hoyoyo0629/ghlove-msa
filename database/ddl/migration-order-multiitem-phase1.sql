-- ============================================================================
-- 주문(order) 멀티아이템 재설계 · Phase 1 — 출고(OD_SHIPMENT)·품목(OD_ORDER_ITEM) 신설
-- ----------------------------------------------------------------------------
-- 설계안: docs/order-multiitem-redesign-plan.md (§2 도메인 모델, §5 마이그레이션)
-- 목표 계층: Order(OD_ORDER=주문헤더) └ Shipment(OD_SHIPMENT=출고/지자체그룹, AS-IS ORDER_SEQUENCE)
--            └ OrderItem(OD_ORDER_ITEM=품목, AS-IS ITEM_SEQUENCE)
--
-- Phase 1은 **가산적(additive)** 이다: 새 테이블만 만들고 기존 od_order 컬럼은 손대지 않는다.
-- 단일품목 경로(CartService.checkout→OrderService.createOrder)는 그대로 병행 동작한다.
-- 헤더 축소(item/option/qty 컬럼 제거)와 백필은 체크아웃·읽기 경로가 신 모델로 넘어간
-- 이후(Phase 2 이후)에 별도 마이그레이션으로 수행한다.
--
-- 적용: docker exec -i ghlove-postgres psql -U postgres -d ghlove_core -f (이 파일 내용)
-- ============================================================================
SET search_path TO ord;

-- ── 출고 = AS-IS ORDER_SEQUENCE ─────────────────────────────────────────────
--   주문 내 지자체(+판매자) 그룹. 포인트 차감이 지자체 단위이므로 출고 = SAGA·정산 기본 단위.
CREATE SEQUENCE IF NOT EXISTS od_shipment_shipment_id_seq;
CREATE TABLE IF NOT EXISTS od_shipment (
    shipment_id     bigint       NOT NULL DEFAULT nextval('od_shipment_shipment_id_seq'),
    order_id        varchar(50)  NOT NULL,            -- FK 없이 od_order.order_id 참조
    locgov_code     varchar(50),                      -- 이 출고의 지자체(포인트/정산 경계)
    seller_id       bigint,                           -- AS-IS는 지자체+판매자 그룹, 현행은 지자체 위주
    point_amount    bigint       NOT NULL DEFAULT 0,  -- 이 출고(지자체) 결제포인트 소계 = SAGA 차감 단위
    delivery_fee    bigint       NOT NULL DEFAULT 0,  -- 출고 단위 배송비
    shipment_status varchar(20)  NOT NULL DEFAULT 'PENDING',  -- 출고 상태(하위 품목 상태 집계 파생)
    stock_outcome   varchar(20),                      -- 출고 단위 SAGA 재고예약 결과(품목 전부 성공해야 성공)
    point_outcome   varchar(20),                      -- 출고 단위 SAGA 포인트차감 결과
    cancel_reason   varchar(255),
    carrier_code    varchar(50),                      -- 배송(SFR-006) — 출고 단위 송장
    invoice_no      varchar(50),
    delivery_status varchar(20),
    shipped_date    timestamp,
    delivered_date  timestamp,
    confirmed_date  timestamp,
    created_date    timestamp    NOT NULL DEFAULT now(),
    updated_date    timestamp,
    CONSTRAINT od_shipment_pkey PRIMARY KEY (shipment_id)
);
CREATE INDEX IF NOT EXISTS ix_od_shipment_order ON od_shipment (order_id);

-- ── 품목 = AS-IS ITEM_SEQUENCE ──────────────────────────────────────────────
--   부분취소/부분반품/교환의 단위(정본). 옵션 스냅샷(optionName/optionPrice)이 여기로 귀속된다.
CREATE SEQUENCE IF NOT EXISTS od_order_item_order_item_id_seq;
CREATE TABLE IF NOT EXISTS od_order_item (
    order_item_id   bigint       NOT NULL DEFAULT nextval('od_order_item_order_item_id_seq'),
    shipment_id     bigint       NOT NULL,            -- 소속 출고
    order_id        varchar(50)  NOT NULL,            -- 비정규화(주문 단위 조회 편의)
    item_id         bigint       NOT NULL,
    seller_id       bigint,
    item_name       varchar(255),                     -- 주문시점 상품명 스냅샷
    option_name     varchar(200),                     -- 옵션 스냅샷(단일품목 모델의 od_order.option_name 이관 대상)
    option_price    integer      NOT NULL DEFAULT 0,
    quantity        integer      NOT NULL,
    unit_price      integer      NOT NULL,             -- 기본 판매가(옵션가 제외)
    point_amount    bigint       NOT NULL DEFAULT 0,   -- 품목 소계 = (unit_price+option_price)*qty - discount
    item_status     varchar(20)  NOT NULL DEFAULT 'PENDING',  -- 품목 단위 상태(부분처리 정본)
    coupon_issue_id integer,                           -- 품목에 적용된 쿠폰 발급건
    discount_amount bigint       NOT NULL DEFAULT 0,
    cancel_reason   varchar(255),
    created_date    timestamp    NOT NULL DEFAULT now(),
    updated_date    timestamp,
    CONSTRAINT od_order_item_pkey PRIMARY KEY (order_item_id)
);
CREATE INDEX IF NOT EXISTS ix_od_order_item_shipment ON od_order_item (shipment_id);
CREATE INDEX IF NOT EXISTS ix_od_order_item_order ON od_order_item (order_id);

-- ── 권한 (기존 od_* 테이블과 동일하게 orderdb 롤에 부여) ─────────────────────
GRANT SELECT, INSERT, UPDATE, DELETE ON od_shipment, od_order_item TO orderdb;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE od_shipment_shipment_id_seq, od_order_item_order_item_id_seq TO orderdb;
