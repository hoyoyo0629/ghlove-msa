-- 배치 실행로그 조회(메뉴 7209)의 menu_url 설정
--
-- AS-IS 메뉴 7209는 /opmanager/batch-log/list(OP_BATCH_EXECUTION 조회)로 연결되지만 TO-BE에는
-- 화면이 없어 op_menu.menu_url이 비어 있었다. 화면을 이식했으므로(BatchLogAdminController,
-- templates/batch-log/list.html) /batch-log를 넣는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-7209-batch-log.sql

UPDATE admin.op_menu
   SET menu_url = '/batch-log'
 WHERE menu_id = 7209;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id = 7209;
