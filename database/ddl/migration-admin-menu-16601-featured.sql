-- 답례품관리 > 이벤트 관리 > 이벤트 관리(메뉴 16601)의 menu_url을 AS-IS 이식 화면으로 교체 (2026-10-20)
--
-- AS-IS op_menu 16601 = /opmanager/featured/list (권한 ROLE_ADMIN_1~6). TO-BE는 옛 발명 화면 /admin/featured를 가리키고 있었다.
-- TO-BE 메뉴 URL은 /opmanager 접두 없이 적는다(FeaturedManagerController, templates/featured/).
-- 등록·수정(/featured/create, /featured/edit/{번호})은 MenuService.URI_ALIASES가 이 메뉴로 묶는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<admindb 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U admindb -d ghlove_core < migration-admin-menu-16601-featured.sql

UPDATE admin.op_menu
   SET menu_url = '/featured/list'
 WHERE menu_id = 16601;

SELECT menu_id, menu_parent_id, menu_name, menu_url, display_flag, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16600, 16601)
 ORDER BY menu_id;
