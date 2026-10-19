-- 2026-10-15 배치 이식: 배송완료·구매확정·스마트택배 배치가 읽는 기준일을 AS-IS OP_CONFIG 값으로 채운다
-- (AS-IS _op_config__202610071247.sql: shipping_complete_date '7', confirm_purchase_date '7', smart_delivery_criteria_date '2').
-- TO-BE는 앞의 둘이 비어 있었다. 이미 값이 있으면 건드리지 않는다. 실행 DB: ghlove_core.
UPDATE admin.op_config SET shipping_complete_date = '7' WHERE coalesce(shipping_complete_date, '') = '';
UPDATE admin.op_config SET confirm_purchase_date = '7' WHERE coalesce(confirm_purchase_date, '') = '';
UPDATE admin.op_config SET smart_delivery_criteria_date = '2' WHERE coalesce(smart_delivery_criteria_date, '') = '';
