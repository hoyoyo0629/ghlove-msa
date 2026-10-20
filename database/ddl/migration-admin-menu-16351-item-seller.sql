-- 답례품관리 > 답례품승인관리(메뉴 16351)의 menu_url을 AS-IS 이식 화면으로 교체 (2026-10-19)
--
-- 기존 /admin/gift-items 는 TO-BE가 지어낸 화면(목록 + 승인/반려/CSV/대표상품/일괄처리)이었다. AS-IS 답례품승인관리
-- (/opmanager/item/seller/list)를 그대로 옮겼으므로(ItemManageController /item/seller/*, templates/item/seller-*) 메뉴가 그쪽을 가리키게 한다.
-- 상품 상세/수정 폼(AS-IS item/seller/edit/{코드})은 아직 이식 전이라 /item/seller/edit/{코드}가 기존 /admin/gift-items/{id}/edit 로 넘긴다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<admindb 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U admindb -d ghlove_core < migration-admin-menu-16351-item-seller.sql

UPDATE admin.op_menu
   SET menu_url = '/item/seller/list'
 WHERE menu_id = 16351;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16301, 16351);
