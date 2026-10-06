-- ============================================================================
-- 주문(order) 멀티아이템 재설계 · Phase 2 — 헤더 주문(OD_ORDER) 단일품목 컬럼 완화
-- ----------------------------------------------------------------------------
-- 신 모델에서 od_order는 "주문 헤더"이고 품목 정보(item_id/quantity/unit_price)는
-- od_order_item으로 이관된다. 헤더 행이 이 컬럼들 없이 저장될 수 있도록 NOT NULL을 푼다.
-- 레거시 단일품목 경로(OrderService.createOrder)는 계속 이 컬럼들을 채우므로 하위호환된다.
-- (컬럼 완전 제거는 레거시 경로 폐기 후 별도 마이그레이션에서)
--
-- 적용: docker exec -i ghlove-postgres psql -U postgres -d ghlove_core -f (이 파일)
-- ============================================================================
SET search_path TO ord;

ALTER TABLE od_order ALTER COLUMN item_id    DROP NOT NULL;
ALTER TABLE od_order ALTER COLUMN quantity   DROP NOT NULL;
ALTER TABLE od_order ALTER COLUMN unit_price DROP NOT NULL;
