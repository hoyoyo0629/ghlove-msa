-- 1404 사용자 권한 관리와 1405 사용자 권한그룹 관리가 둘 다 /admin/roles를 가리키고 있었다.
-- AS-IS는 서로 다른 화면이다: 1404 = /opmanager/user-group/role/list (그룹별 메뉴권한 3단 체크박스),
-- 1405 = /opmanager/user-group/list (그룹 목록). 1404를 전용 화면으로 바로잡는다.
-- 재실행 가능.
UPDATE admin.op_menu SET menu_url = '/admin/roles/matrix' WHERE menu_id = 1404;
