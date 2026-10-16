-- 답례품관리 > 카테고리 그룹 관리(메뉴 16102)의 menu_url을 AS-IS 이식 화면으로 교체
--
-- 기존 /admin/category-teams 는 대그룹/그룹/그룹 배너/대그룹 추천상품을 다루는 TO-BE 자체 화면이었고, AS-IS 카테고리 그룹 관리
-- (/opmanager/categories-team-group/list)는 대그룹 아래 1차 카테고리 그룹을 보여주고 그룹 등록/수정/삭제/정렬순서변경을 한다.
-- 화면을 AS-IS 그대로 이식했으므로(CategoriesTeamGroupManagerController, templates/categories-team-group/*, gift 서비스
-- /api/admin/categories-team-group) 메뉴가 그쪽을 가리키게 한다. /categories-team-group 으로 두면 하위 경로(/list, /group/create,
-- /group/edit/…)가 모두 같은 메뉴(16102)로 권한·브레드크럼 매칭된다. 옛 /admin/category-teams 화면 자체는 지우지 않는다(메뉴 연결만 끊음).
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-16102-categories-team-group.sql

UPDATE admin.op_menu
   SET menu_url = '/categories-team-group'
 WHERE menu_id = 16102;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (16101, 16102);
