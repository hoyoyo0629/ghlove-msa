-- 답례품관리 > 답례품관리(메뉴 16301)의 menu_url을 AS-IS 이식 화면으로 교체 (2026-10-16)
--
-- 기존 /admin/gift-items 는 TO-BE가 지어낸 화면(목록 + 승인/반려/CSV/대표상품/일괄처리)이었다. AS-IS 답례품관리
-- (/opmanager/item/list)를 그대로 옮겼으므로(ItemManageController, templates/item/*) 메뉴가 그쪽을 가리키게 한다.
-- 16351 답례품승인관리는 아직 AS-IS(/opmanager/item/seller/list)로 옮기지 않아 기존 /admin/gift-items 를 그대로 둔다
-- (등록/수정 폼도 AS-IS item/create·item/edit 이식 전까지 그 화면을 쓴다).
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-16301-item.sql

UPDATE admin.op_menu
   SET menu_url = '/item'
 WHERE menu_id = 16301;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16301, 16351);
