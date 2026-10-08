-- 주문번호 생성규칙을 AS-IS(OrderServiceImpl.getNewOrderCode)와 동일한 "K" + 10자리 0-패딩
-- 숫자로 바꾸면서, 기존에 TO-BE 포맷("O"+yyyyMMddHHmmss+4자리 난수)으로 생성된 주문 19건도
-- 같은 형식으로 일괄 변환한다. order_id를 참조하는 모든 테이블(품목/배송/클레임/포인트사용
-- 내역/admin 통계 readmodel)을 한 트랜잭션에서 같이 바꾼다 - 하나만 바뀌면 조인이 끊어진다.
--
-- 매핑은 created_date 오름차순으로 K0000000001부터 순번을 매긴다(AS-IS의 누적 시퀀스값을
-- 그대로 옮기는 게 아니라 TO-BE가 1부터 새로 시작 - migration-admin-privacy-access-log-seq.sql
-- 등 이 저장소의 기존 관례와 동일).

BEGIN;

CREATE TEMP TABLE order_code_mapping (old_id varchar(50), new_id varchar(50));
INSERT INTO order_code_mapping (old_id, new_id) VALUES
    ('O202609010941118853', 'K0000000001'),
    ('O202609021441371624', 'K0000000002'),
    ('O202609021442584403', 'K0000000003'),
    ('O202609052118371944', 'K0000000004'),
    ('O202609052118542743', 'K0000000005'),
    ('O202609052119448534', 'K0000000006'),
    ('O202609052221425052', 'K0000000007'),
    ('O202609081317166919', 'K0000000008'),
    ('O202609081318489677', 'K0000000009'),
    ('O202609091453352465', 'K0000000010'),
    ('O202609091722296199', 'K0000000011'),
    ('O202609101009497769', 'K0000000012'),
    ('O202609101029031357', 'K0000000013'),
    ('O202609211347303155', 'K0000000014'),
    ('O202609211347306331', 'K0000000015'),
    ('O202609220906304838', 'K0000000016'),
    ('O202609221255364598', 'K0000000017'),
    ('O202609221307013733', 'K0000000018'),
    ('O202609221330328830', 'K0000000019');

-- 자식부터: od_order_item / od_shipment / od_claim은 order_id를 그대로 참조한다.
UPDATE ord.od_order_item oi SET order_id = m.new_id
  FROM order_code_mapping m WHERE oi.order_id = m.old_id;

UPDATE ord.od_shipment s SET order_id = m.new_id
  FROM order_code_mapping m WHERE s.order_id = m.old_id;

UPDATE ord.od_claim c SET order_id = m.new_id
  FROM order_code_mapping m WHERE c.order_id = m.old_id;

-- 부모(PK) 마지막.
UPDATE ord.od_order o SET order_id = m.new_id
  FROM order_code_mapping m WHERE o.order_id = m.old_id;

-- point.g_cntr_use_point.order_code는 멀티아이템 주문이면 "주문번호#항목번호" 형태다
-- (예: O202609220906304838#1) - 접미사를 보존한 채 머리글만 교체한다.
UPDATE point.g_cntr_use_point p SET order_code = m.new_id || substring(p.order_code from length(m.old_id) + 1)
  FROM order_code_mapping m
 WHERE p.order_code = m.old_id OR p.order_code LIKE m.old_id || '#%';

-- admin 쪽 readmodel(denormalized 통계 테이블)도 같은 order_id를 들고 있다.
UPDATE admin.stat_order_ledger sol SET order_id = m.new_id
  FROM order_code_mapping m WHERE sol.order_id = m.old_id;

UPDATE admin.stat_claim_ledger scl SET order_id = m.new_id
  FROM order_code_mapping m WHERE scl.order_id = m.old_id;

-- 앞으로 생성될 주문번호용 시퀀스 - 이미 쓴 19개 다음부터 이어지게 20으로 맞춘다.
CREATE SEQUENCE IF NOT EXISTS ord.op_order_code_seq;
SELECT setval('ord.op_order_code_seq', 19, true);

COMMIT;
