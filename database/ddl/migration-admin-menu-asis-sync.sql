-- admin.op_menu / op_menu_right 를 AS-IS 운영DB export 기준으로 전수 동기화한다.
-- 생성원본: OP_MENU_202610010945.sql / OP_MENU_RIGHT_202610010946.sql (2026-10-02 생성)
-- AS-IS nav 노출규칙 = MENU_TYPE=3 AND STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재
--   (menu-mapper.xml getFirstMenuList/getSecondAndThirdMenuList, ROLE_SUPERVISOR만 권한조인 우회).
--   DISPLAY_FLAG는 SELECT 목록에만 있고 WHERE에 전혀 쓰이지 않는다 - TO-BE가 이 컬럼으로
--   필터링하던 것이 근본 불일치였다. 컬럼값은 AS-IS와 동일하게 맞춰두고, 필터는 코드에서 교체한다.
-- 신규 메뉴 5120(내부문의 관리)은 AS-IS에 없는 TO-BE 전용이라 동기화 대상에서 제외하고 권한만 부여한다.

-- [1] op_menu display_flag/status_code/menu_seq 동기화 (15건)
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 1402;  -- 메세지 관리
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 5112;  -- Q&A
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 6102;  -- 방문자접속경로
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 6711;  -- 전체
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 6712;  -- 지자체별
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 6900;  -- 기부금 운영현황
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 6901;  -- 기부금 운영현황
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 7209;  -- 배치 실행로그 조회
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 14201;  -- 기금사업 등록/관리
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16407;  -- 신규주문(모바일)
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16408;  -- 발송준비중(모바일)
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16751;  -- 제철식품관 관리
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16801;  -- 특산물관 관리
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16901;  -- 미완료 주문 목록
UPDATE admin.op_menu SET display_flag = 'Y' WHERE menu_id = 16902;  -- 미완료 정산 목록

-- [2] op_menu_right 누락 보충 (18행) - AS-IS에 있는 ROLE_ADMIN_7/8(오프라인담당자),
--     ROLE_ADMIN_10 행까지 그대로 넣는다. TO-BE에 해당 롤 계정이 아직 없어 화면 영향은 없고,
--     롤이 추가되는 순간 AS-IS와 동일하게 동작한다.
-- op_menu_right에는 (menu_id, authority) 유니크 제약이 없어 ON CONFLICT가 듣지 않는다.
-- 재실행해도 중복이 생기지 않게 NOT EXISTS로 거른다.
INSERT INTO admin.op_menu_right (menu_id, authority)
SELECT v.mid, v.auth FROM (VALUES
  (4601, 'ROLE_ADMIN_7'),
  (4601, 'ROLE_ADMIN_8'),
  (5201, 'ROLE_ADMIN_7'),
  (5201, 'ROLE_ADMIN_8'),
  (5202, 'ROLE_ADMIN_7'),
  (5202, 'ROLE_ADMIN_8'),
  (11401, 'ROLE_ADMIN_10'),
  (11402, 'ROLE_ADMIN_10'),
  (11406, 'ROLE_ADMIN_7'),
  (11406, 'ROLE_ADMIN_8'),
  (15101, 'ROLE_ADMIN_7'),
  (15101, 'ROLE_ADMIN_8'),
  (15102, 'ROLE_ADMIN_7'),
  (15102, 'ROLE_ADMIN_8'),
  (17101, 'ROLE_ADMIN_10'),
  (17102, 'ROLE_ADMIN_10'),
  (17201, 'ROLE_ADMIN_10'),
  (17202, 'ROLE_ADMIN_10')
) AS v(mid, auth)
WHERE EXISTS (SELECT 1 FROM admin.op_menu m WHERE m.menu_id = v.mid)
  AND NOT EXISTS (SELECT 1 FROM admin.op_menu_right r WHERE r.menu_id = v.mid AND r.authority = v.auth);

-- [3] op_menu_right 초과행 제거 (0건)

-- [4] TO-BE 전용 메뉴 권한 부여 - nav가 권한조인으로 바뀌면 권한행이 없는 메뉴는 사라지므로,
--     AS-IS에 대응 메뉴가 없는 신규 화면에는 운영자 롤을 명시해 둔다.
INSERT INTO admin.op_menu_right (menu_id, authority)
SELECT 5120, v.a FROM (VALUES ('ROLE_ADMIN_1'),('ROLE_ADMIN_2'),('ROLE_ADMIN_3'),('ROLE_ADMIN_4')) AS v(a)
WHERE EXISTS (SELECT 1 FROM admin.op_menu WHERE menu_id = 5120)
  AND NOT EXISTS (SELECT 1 FROM admin.op_menu_right r WHERE r.menu_id = 5120 AND r.authority = v.a);

-- [검증] 롤별 nav 리프 수 (AS-IS 기준: 1=114, 2=87, 3=95, 4=85, 5=53, 6=51)
-- select r.authority, count(*) from admin.op_menu m join admin.op_menu_right r on r.menu_id = m.menu_id
--  where m.status_code = '1' and m.menu_url is not null and m.menu_url <> '' group by 1 order by 1;
