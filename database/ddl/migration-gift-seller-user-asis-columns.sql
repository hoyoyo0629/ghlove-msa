-- 판매자 계정(OP_SELLER_USER) AS-IS 컬럼 3개 추가 + 기존 판매자에 계정행 보강 (2026-10-15, 답례품제공자 관리 이식)
--
-- AS-IS OP_SELLER_USER에는 PWD_CHG_SELLER_YN(초기화 비밀번호 변경 대상 여부), MBER_DN(공동인증서 DN), MBER_FIN_DN(금융인증서 DN)이
-- 있는데 gift 테이블에는 없었다(MBER_CI·RECEIVE_SMS는 2026-10-13 migration-gift-seller-user-sms-columns.sql로 추가).
-- 답례품제공자 관리 목록은 AS-IS처럼 OP_SELLER inner join OP_SELLER_USER 라서 계정행이 없는 판매자는 목록에 나오지 않는다 - 계정행이
-- 없던 기존 판매자 18건에 계정행을 채운다(사용자번호=판매자번호, 상태는 AS-IS 매핑: 사용2→정상9, 중지3→차단2, 해지4→탈퇴3, 승인대기5→가입대기1).
--
-- 적용: gift DB(public) - psql -d gift 로 실행

ALTER TABLE op_seller_user ADD COLUMN IF NOT EXISTS pwd_chg_seller_yn VARCHAR(1);
ALTER TABLE op_seller_user ADD COLUMN IF NOT EXISTS mber_dn VARCHAR(200);
ALTER TABLE op_seller_user ADD COLUMN IF NOT EXISTS mber_fin_dn VARCHAR(200);

COMMENT ON COLUMN op_seller_user.pwd_chg_seller_yn IS '판매자 비밀번호 변경 여부 (Y:변경 필요(초기화됨), N:변경 완료)';
COMMENT ON COLUMN op_seller_user.mber_dn IS '공동인증서 DN';
COMMENT ON COLUMN op_seller_user.mber_fin_dn IS '금융인증서 DN';

INSERT INTO op_seller_user (user_id, login_id, password, user_name, email, phone_number, status_code, login_count, login_fail_count,
                            password_type, updated_date, created_date, locgov_code, receive_sms)
SELECT s.seller_id, s.login_id, s.password, s.seller_name, s.email, s.phone_number,
       CASE s.status_code WHEN '2' THEN 9 WHEN '3' THEN 2 WHEN '4' THEN 3 WHEN '5' THEN 1 ELSE 2 END,
       0, 0, 'N', COALESCE(s.updated_date, s.created_date), s.created_date, SUBSTR(s.login_id, 1, 5), '0'
  FROM op_seller s
 WHERE s.login_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM op_seller_user u WHERE u.login_id = s.login_id);

SELECT count(*) AS sellers, (SELECT count(*) FROM op_seller_user) AS seller_users FROM op_seller;
