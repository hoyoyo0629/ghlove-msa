-- 오프라인기부접수(기탁서 등록, admin 15102) 답례품 주문 - AS-IS OrderServiceImpl.saveOffGiveOrder +
-- NgDonationBatchServiceImpl.procOffOrder 이식 (2026-10-13).
-- 대상 DB: ghlove_core (ord 스키마)
--
-- AS-IS는 기탁서 등록 때 답례품 주문을 '입금대기'(ORDER_STATUS '0')로 만들고 OP_ORDER_ITEM.FILLER9에
-- 기부일련번호(CNTR_SN)를 남긴다. 수납완료(CNTR_STTUS_CODE '200') 뒤 배치(procOffOrder)가 FILLER9로
-- 그 주문을 찾아 결제완료('10')·포인트 사용·재고 차감을 하고 OFF_PROC_YN='Y'로 처리여부를 남긴다.
--
-- TO-BE 품목(od_order_item = AS-IS ITEM_SEQUENCE)에 같은 두 값을 둔다. FILLER9는 AS-IS의 범용 여분
-- 컬럼이라 이름만 실제 의미(cntr_sn, donation.g_cntr.cntr_sn과 같은 길이)로 바꿨다.
-- OFF_PROC_YN 기본값 'N'은 AS-IS DDL(off_proc_yn VARCHAR(1) DEFAULT 'N')과 같다.

ALTER TABLE ord.od_order_item ADD COLUMN IF NOT EXISTS cntr_sn varchar(50);
ALTER TABLE ord.od_order_item ADD COLUMN IF NOT EXISTS off_proc_yn varchar(1) DEFAULT 'N';
CREATE INDEX IF NOT EXISTS ix_od_order_item_cntr_sn ON ord.od_order_item (cntr_sn);

-- 주문상태 라벨 - AS-IS ShopUtils.getOrderStatusLabel("0") = "입금대기"
INSERT INTO ord.op_common_code (code_type, code_language, id, label, ordering, use_yn) VALUES
('ORDER_STATUS', 'ko', 'WAITING_DEPOSIT', '입금대기', 0, 'Y')
ON CONFLICT (code_type, code_language, id) DO NOTHING;
