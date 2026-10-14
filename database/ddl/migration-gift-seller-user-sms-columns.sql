-- 판매자 계정(OP_SELLER_USER)에 AS-IS 문자 수신 컬럼 2개 추가 (2026-10-13).
-- 대상 DB: gift (public 스키마)
--
-- 국민비서 "답례품 주문시"(PRESENT_ORDER) 문자의 답례품 제공자 수신정보를 AS-IS
-- sellerUserMapper.getSellerUserByLoginIdForSms가 OP_SELLER_USER.MBER_CI(CI)·RECEIVE_SMS(수신동의)에서
-- 읽는다. TO-BE 테이블엔 두 컬럼이 없었다 - AS-IS DDL 그대로(타입·기본값·의미) 추가한다.
--   mber_ci     VARCHAR(200)              '답례품제공자CI'
--   receive_sms VARCHAR(1) DEFAULT '0'    'SMS수신여부 (0:수신, 1:미수신)'
-- AS-IS에 있는 나머지 3개(pwd_chg_seller_yn, mber_dn, mber_fin_dn)는 판매자 로그인(인증서) 쪽 컬럼이라
-- 그 기능을 이식할 때 추가한다.
--
-- 재실행 안전(IF NOT EXISTS).
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres psql -U postgres -d gift -f /dev/stdin < migration-gift-seller-user-sms-columns.sql

ALTER TABLE op_seller_user ADD COLUMN IF NOT EXISTS mber_ci VARCHAR(200);
ALTER TABLE op_seller_user ADD COLUMN IF NOT EXISTS receive_sms VARCHAR(1) DEFAULT '0';

COMMENT ON COLUMN op_seller_user.mber_ci IS '답례품제공자CI';
COMMENT ON COLUMN op_seller_user.receive_sms IS 'SMS수신여부 (0:수신, 1:미수신)';
