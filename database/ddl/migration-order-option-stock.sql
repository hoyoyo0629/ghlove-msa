-- gift 옵션 다형체계 재현 Phase 5: 옵션단위 재고. 옵션(itemOptionId)이 option_stock_flag='Y'면
-- 그 옵션의 재고(op_item_option.option_stock_quantity)를 차감/복원한다(AS-IS parity).
-- 이를 위해 주문품목·출고재고예약에 item_option_id를 실어 나른다.
-- docs/gift-option-redesign-plan.md Phase 5.

-- order (ghlove_core, ord 스키마)
ALTER TABLE ord.od_order_item ADD COLUMN IF NOT EXISTS item_option_id bigint;

-- gift (gift DB, public 스키마) : ALTER는 별도 psql -U gift 로 적용
-- ALTER TABLE gift_shipment_stock ADD COLUMN IF NOT EXISTS item_option_id bigint;
