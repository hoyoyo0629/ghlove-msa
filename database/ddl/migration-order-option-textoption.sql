-- gift 옵션 다형체계 재현 Phase 4: 각인(필수 추가정보) 값을 장바구니→주문 라인에 저장.
-- AS-IS textOption(구매자 입력값, 제목별 값을 '||'로 연결)을 담는다.
-- docs/gift-option-redesign-plan.md Phase 4.

ALTER TABLE ord.od_cart_item  ADD COLUMN IF NOT EXISTS text_option varchar(200);
ALTER TABLE ord.od_order_item ADD COLUMN IF NOT EXISTS text_option varchar(200);

-- 각인이 다르면 같은 답례품·옵션이라도 별도 장바구니 라인이어야 한다(AS-IS 동일).
-- 유니크 키에 text_option을 포함. 각인 없는 라인은 ''로 정규화해 NULL-distinct 문제를 피한다.
UPDATE ord.od_cart_item SET text_option = '' WHERE text_option IS NULL;
ALTER TABLE ord.od_cart_item ALTER COLUMN text_option SET DEFAULT '';
ALTER TABLE ord.od_cart_item ALTER COLUMN text_option SET NOT NULL;
ALTER TABLE ord.od_cart_item DROP CONSTRAINT IF EXISTS od_cart_item_user_item_option_key;
ALTER TABLE ord.od_cart_item ADD CONSTRAINT od_cart_item_user_item_option_text_key
    UNIQUE (user_id, item_id, item_option_id, text_option);
