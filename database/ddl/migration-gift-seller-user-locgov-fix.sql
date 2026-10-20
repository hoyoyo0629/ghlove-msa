-- 판매자 계정(OP_SELLER_USER) 지자체코드 바로잡기 (2026-10-20)
--
-- migration-gift-seller-user-asis-columns.sql(2026-10-15)이 계정행 없던 판매자 18건을 보강하면서 LOCGOV_CODE를 SUBSTR(login_id, 1, 5)로 넣었다
-- (AS-IS 판매자 아이디가 지자체코드로 시작한다는 가정). 그런데 TO-BE 개발 판매자는 아이디가 seller9001 같은 형식이라 'selle'가 들어갔다.
-- AS-IS 개발DB는 9,229건 중 9,215건이 계정 지자체코드 = 판매자 지자체코드다. 관리자 문의(16651) 목록의 지자체 칸이 이 값을 쓰므로
-- 숫자가 아닌 값만 판매자(OP_SELLER)의 지자체코드로 바꾼다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<gift 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U gift -d gift < migration-gift-seller-user-locgov-fix.sql

UPDATE op_seller_user su
   SET locgov_code = s.locgov_code
  FROM op_seller s
 WHERE s.login_id = su.login_id
   AND s.locgov_code IS NOT NULL
   AND (su.locgov_code IS NULL OR su.locgov_code !~ '^[0-9]+$');

SELECT coalesce(locgov_code, '<null>') AS locgov_code, count(*) FROM op_seller_user GROUP BY 1 ORDER BY 1;
