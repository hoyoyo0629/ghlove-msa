-- [DEV ONLY] 개발서버 DBeaver 편의: 계정별로 재로그인하지 않고 모든 스키마/DB를 자유롭게
-- 조회/수정(SELECT/INSERT/UPDATE/DELETE/DDL)하도록 6개 앱 롤(member/donation/point/orderdb/
-- admindb/gift)에 교차 ALL 권한을 부여한다. 운영에는 절대 적용하지 말 것.
-- postgres 컨테이너 데이터볼륨이 초기화되면 이 스크립트를 다시 실행한다.
--   실행: docker exec -i ghlove-postgres psql -U postgres -d ghlove_core -f - < 이 파일의 첫 블록
--         docker exec -i ghlove-postgres psql -U postgres -d gift        -f - < 이 파일의 둘째 블록

-- ===== DB: ghlove_core (schemas: admin, donation, member, ord, point, public) =====
\connect ghlove_core
DO $$
DECLARE
  r text;
  s text;
  roles   text[] := ARRAY['member','donation','point','orderdb','admindb','gift'];
  schemas text[] := ARRAY['admin','donation','member','ord','point','public'];
BEGIN
  FOREACH r IN ARRAY roles LOOP
    EXECUTE format('GRANT CONNECT ON DATABASE ghlove_core TO %I', r);
    FOREACH s IN ARRAY schemas LOOP
      EXECUTE format('GRANT USAGE, CREATE ON SCHEMA %I TO %I', s, r);
      EXECUTE format('GRANT ALL ON ALL TABLES IN SCHEMA %I TO %I', s, r);
      EXECUTE format('GRANT ALL ON ALL SEQUENCES IN SCHEMA %I TO %I', s, r);
      EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA %I GRANT ALL ON TABLES TO %I', s, r);
      EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA %I GRANT ALL ON SEQUENCES TO %I', s, r);
    END LOOP;
    EXECUTE format('ALTER ROLE %I IN DATABASE ghlove_core SET search_path = admin, donation, member, ord, point, public', r);
  END LOOP;
END $$;

-- ===== DB: gift (schema: public) =====
\connect gift
DO $$
DECLARE
  r text;
  roles text[] := ARRAY['member','donation','point','orderdb','admindb','gift'];
BEGIN
  FOREACH r IN ARRAY roles LOOP
    EXECUTE format('GRANT CONNECT ON DATABASE gift TO %I', r);
    EXECUTE format('GRANT USAGE, CREATE ON SCHEMA public TO %I', r);
    EXECUTE format('GRANT ALL ON ALL TABLES IN SCHEMA public TO %I', r);
    EXECUTE format('GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO %I', r);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE gift IN SCHEMA public GRANT ALL ON TABLES TO %I', r);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE gift IN SCHEMA public GRANT ALL ON SEQUENCES TO %I', r);
    EXECUTE format('ALTER ROLE %I IN DATABASE gift SET search_path = public', r);
  END LOOP;
END $$;
