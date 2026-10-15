-- 2026-10-15 판매자 상태코드를 AS-IS 값으로 전환(사용자 승인). 실행 DB: gift.
-- AS-IS OP_SELLER.STATUS_CODE 2=사용 / 3=중지 / 5=승인대기(seller/list.jsp·form.jsp). TO-BE가 쓰던 0대기/1승인/2반려/3중지는
-- '2'가 AS-IS 사용·TO-BE 반려로 충돌했다(이메일 발송 대상 조회가 AS-IS '2'를 그대로 써 반려 업체를 고르게 돼 있었다).
-- 1→2, 0→5, 3 그대로. 반려(2)는 AS-IS에 없는 상태라 없앴다(전환 시점 0건). 코드도 같은 날 전환(Seller.STATUS_*).
UPDATE op_seller SET status_code = CASE status_code WHEN '1' THEN '2' WHEN '0' THEN '5' ELSE status_code END
 WHERE status_code IN ('0', '1');
