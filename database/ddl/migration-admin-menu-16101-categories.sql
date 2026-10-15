-- 답례품관리 > 카테고리관리(메뉴 16101)의 menu_url을 AS-IS 이식 화면으로 교체
--
-- 기존 /admin/gift-categories 는 GIFT_SUBCATEGORY(스토어프론트 GNB 메가메뉴)를 고치는 TO-BE 자체 화면이었고,
-- AS-IS 카테고리관리(/opmanager/categories/list)는 OP_CATEGORY 1~4차 트리(jsTree 드래그앤드롭 이동 + SEO)다.
-- 화면을 AS-IS 그대로 이식했으므로(CategoriesManagerController, templates/categories/*, gift 서비스
-- /api/admin/categories) 메뉴가 그쪽을 가리키게 한다. /categories 로 두면 하위 경로(/categories/list, /edit/…,
-- /tree-list …)가 모두 같은 메뉴(16101)로 권한·브레드크럼 매칭된다.
-- 옛 /admin/gift-categories 화면 자체는 지우지 않는다(메뉴에서 연결만 끊는다).
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-16101-categories.sql

UPDATE admin.op_menu
   SET menu_url = '/categories'
 WHERE menu_id = 16101;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16101, 16102);
