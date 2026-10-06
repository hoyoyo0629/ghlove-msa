-- ============================================================================
-- 주문 멀티아이템 재설계 · gift 소비자 — 출고 단위 재고예약 추적(GIFT_SHIPMENT_STOCK)
-- ----------------------------------------------------------------------------
-- 레거시 GIFT_ORDER_STOCK(주문=1품목, PK=order_id)의 출고·다품목 버전. 한 출고(shipment)의
-- **품목(order_item)마다** 한 행. 멱등(같은 shipment 재전달 시 중복예약 방지) + 보상
-- (SHIPMENT_CANCELLED/ITEM_CANCELLED 시 실제 예약분만 복원) 판단에 쓴다.
--
-- ★ PK는 order_item_id다(item_id 아님): 같은 답례품의 서로 다른 옵션이 한 출고에 2줄로
--   담길 수 있어 (shipment_id,item_id)로는 충돌한다. order_item_id는 전역 유일.
--
-- 적용: docker exec -i ghlove-postgres psql -U gift -d gift -f (이 파일)
-- ============================================================================
DROP TABLE IF EXISTS gift_shipment_stock;
CREATE TABLE gift_shipment_stock (
    order_item_id bigint                      NOT NULL,
    shipment_id   bigint                      NOT NULL,
    item_id       bigint                      NOT NULL,
    quantity      integer                     NOT NULL,
    status        varchar(20)                 NOT NULL,   -- RESERVED / RESTORED
    created_date  timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT gift_shipment_stock_pkey PRIMARY KEY (order_item_id)
);
CREATE INDEX ix_gift_shipment_stock_shipment ON gift_shipment_stock (shipment_id);
