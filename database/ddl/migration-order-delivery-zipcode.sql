-- 주문 배송지 우편번호 - AS-IS OP_ORDER_SHIPPING_INFO.RECEIVE_ZIPCODE 자리 (2026-10-14).
-- 대상 DB: ghlove_core (ord 스키마)
--
-- 기탁서 상세(admin 15102 등록 후 화면, AS-IS getOffgive)는 "배송지 주소" 첫 줄에 우편번호(rcvPost)를 보여준다.
-- TO-BE od_order에는 주소·상세주소만 있고 우편번호 칸이 없어 기탁서 등록 때 받은 우편번호가 버려졌다.
-- 기존 행은 비어 있다(채울 원천값 없음).

ALTER TABLE ord.od_order ADD COLUMN IF NOT EXISTS delivery_zipcode varchar(10);
