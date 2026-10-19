-- 답례품 승인상태(OP_ITEM.DATA_STATUS_CODE)를 AS-IS 숫자코드로 전환 (2026-10-16, 답례품관리 목록 이식)
--
-- AS-IS OP_ITEM.DATA_STATUS_CODE 는 0:임시등록(이관) 1:정상 20:등록신청 21:등록반려 30:수정신청 31:수정반려 40:삭제신청 41:삭제반려
-- 90:판매종료 99:삭제 의 숫자코드다. TO-BE 는 이 칸에 PENDING/APPROVED/REJECTED/STOPPED/DISCONTINUED 문자값을 넣어 쓰고 있어서
-- 답례품관리 목록(상태 IN ('0','1','90'))·기탁서 대표답례품(상태 '1')·AS-IS 이관 데이터와 값이 맞지 않았다.
--   APPROVED -> 1, PENDING -> 20, REJECTED -> 21, STOPPED -> 90, DISCONTINUED -> 99
-- GIFT_STATUS 공통코드(판매자 포털 상태 라벨)도 같은 코드로 다시 키잉한다.
--
-- 적용: gift DB(public) - psql -d gift 로 실행. 여러 번 실행해도 안전(이미 숫자코드면 변경 없음).
-- 코드(gift/order/storefront/admin)는 같은 날 함께 숫자코드로 바뀌었으므로 이 SQL 과 서비스 재기동을 같이 해야 한다.

UPDATE op_item
   SET data_status_code = CASE data_status_code
                              WHEN 'APPROVED'     THEN '1'
                              WHEN 'PENDING'      THEN '20'
                              WHEN 'REJECTED'     THEN '21'
                              WHEN 'STOPPED'      THEN '90'
                              WHEN 'DISCONTINUED' THEN '99'
                          END
 WHERE data_status_code IN ('APPROVED', 'PENDING', 'REJECTED', 'STOPPED', 'DISCONTINUED');

DELETE FROM op_common_code
 WHERE code_type = 'GIFT_STATUS'
   AND id IN ('PENDING', 'APPROVED', 'REJECTED', 'STOPPED', 'DISCONTINUED');

INSERT INTO op_common_code (code_type, code_language, id, label, ordering, use_yn) VALUES
('GIFT_STATUS', 'ko', '20', '승인대기', 1, 'Y'),
('GIFT_STATUS', 'ko', '1',  '승인',     2, 'Y'),
('GIFT_STATUS', 'ko', '21', '반려',     3, 'Y'),
('GIFT_STATUS', 'ko', '90', '판매중지', 4, 'Y'),
('GIFT_STATUS', 'ko', '99', '폐지',     5, 'Y')
ON CONFLICT (code_type, code_language, id) DO NOTHING;
