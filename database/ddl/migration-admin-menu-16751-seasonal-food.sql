-- 답례품관리 > 제철식품관 관리 > 제철식품관 관리(메뉴 16751)의 menu_url을 AS-IS 이식 화면으로 지정 (2026-10-19)
--
-- AS-IS op_menu 16751 = /opmanager/seasonal-food/list (상태 1·표시 Y, 권한 ROLE_ADMIN_1~4). TO-BE 시드는 URL이 NULL이라 LNB에서 안 보였다.
-- TO-BE 메뉴 URL은 /opmanager 접두 없이 적는다(SeasonalFoodController, templates/seasonal-food/).
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<admindb 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U admindb -d ghlove_core < migration-admin-menu-16751-seasonal-food.sql

UPDATE admin.op_menu
   SET menu_url = '/seasonal-food/list'
 WHERE menu_id = 16751;

SELECT menu_id, menu_parent_id, menu_name, menu_url, display_flag, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16750, 16751)
 ORDER BY menu_id;
