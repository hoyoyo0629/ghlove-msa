-- 지자체담당자관리(메뉴 4402)를 자기 화면으로 분리 - 구조갭 정리 완료
--
-- 4501(운영관리자)과 함께 /admin/person-in-charge 한 화면의 scope 탭으로 통합돼 있던 것을
-- AS-IS대로 분리한다(migration-admin-menu-4501-oper-charger.sql이 먼저 4501을 분리했다).
--
--   4402 → /admin/person-in-charge/locgov  (AS-IS /opmanager/user/locgov-charger)
--   4501 → /admin/person-in-charge/oper    (AS-IS /opmanager/user/oper-charger)
--
-- 이후 기존 통합화면(/admin/person-in-charge)은 어느 메뉴도 가리키지 않는다 - AS-IS에 없는
-- TO-BE 자체 화면이라 지우지 않고 남겨만 둔다(시스템관리의 /log/levy, 액션로그 화면과 같은 처리).
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-4402-locgov-charger.sql

UPDATE admin.op_menu
   SET menu_url = '/admin/person-in-charge/locgov'
 WHERE menu_id = 4402;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id IN (4402, 4501)
 ORDER BY menu_id;
