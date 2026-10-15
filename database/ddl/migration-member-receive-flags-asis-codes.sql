-- 2026-10-15 수신동의를 AS-IS 값으로 전환(사용자 승인). 실행 DB: ghlove_core. 판매자 상태코드는 migration-gift-seller-status-asis-codes.sql(gift DB).
--
-- 1) member.op_user_detail RECEIVE_EMAIL/SMS/PBANC/KAKAO
--    AS-IS 정본 "0"=수신 / "1"=비수신. TO-BE 회원정보수정이 Y/N으로 저장해 운영자 화면·국민비서 문자 대상('0'만 동의)에서
--    동의 회원이 비동의로 보였다. 코드도 같은 날 0/1 저장으로 바꿨다(MemberService). 빈 값(null)은 건드리지 않는다.
--    실행 DB: ghlove_core
UPDATE member.op_user_detail SET receive_email = CASE receive_email WHEN 'Y' THEN '0' WHEN 'N' THEN '1' ELSE receive_email END,
                                 receive_sms   = CASE receive_sms   WHEN 'Y' THEN '0' WHEN 'N' THEN '1' ELSE receive_sms   END,
                                 receive_pbanc = CASE receive_pbanc WHEN 'Y' THEN '0' WHEN 'N' THEN '1' ELSE receive_pbanc END,
                                 receive_kakao = CASE receive_kakao WHEN 'Y' THEN '0' WHEN 'N' THEN '1' ELSE receive_kakao END
 WHERE receive_email IN ('Y', 'N') OR receive_sms IN ('Y', 'N') OR receive_pbanc IN ('Y', 'N') OR receive_kakao IN ('Y', 'N');

