-- 운영관리자(메뉴 4501)를 자기 화면으로 분리
--
-- AS-IS는 지자체담당자관리(4402)와 운영관리자(4501)가 **별개 컨트롤러·JSP**다:
--   4402 → /opmanager/user/locgov-charger/list  (대상 ROLE_ADMIN_5·6, 지자체 스코프, 지자체명 검색)
--   4501 → /opmanager/user/oper-charger/list    (대상 ROLE_ADMIN_1~4, 회원구분 체크박스, 권한·소속부서 컬럼)
--
-- TO-BE는 두 화면을 /admin/person-in-charge 한 화면의 scope 탭으로 통합해 두고 **두 메뉴가 같은
-- URL을 가리켰다**(시스템관리의 1404·1405와 같은 유형의 구조갭). AS-IS대로 4501을 분리한다.
--
-- 4402는 아직 통합화면(/admin/person-in-charge)을 가리킨다 - 그 화면의 기본 scope가 locgov라
-- 4402 메뉴를 누르면 지자체담당자 내용이 나오므로 중간상태로도 앞뒤가 맞다. 4402 전용 화면을
-- 이식하면 그때 같은 방식으로 /admin/person-in-charge/locgov로 바꾼다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-4501-oper-charger.sql

UPDATE admin.op_menu
   SET menu_url = '/admin/person-in-charge/oper'
 WHERE menu_id = 4501;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id IN (4402, 4501)
 ORDER BY menu_id;
