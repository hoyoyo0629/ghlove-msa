-- 메세지 관리(메뉴 1402)의 menu_url 설정
--
-- AS-IS 메뉴 1402는 /opmanager/message/list로 연결되는 OP_COMMON_MESSAGE 관리화면이지만
-- TO-BE에는 화면이 없어 op_menu.menu_url이 비어 있었다(메뉴를 눌러도 갈 곳이 없는 상태).
-- 화면을 이식했으므로(CommonMessageAdminController, templates/message/*) /message를 넣는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-1402-message.sql

UPDATE admin.op_menu
   SET menu_url = '/message'
 WHERE menu_id = 1402;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id = 1402;
