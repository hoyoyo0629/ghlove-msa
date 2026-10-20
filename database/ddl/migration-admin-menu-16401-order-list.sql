-- 답례품관리 > 주문관리 > 전체주문관리(메뉴 16401)의 menu_url을 AS-IS 이식 화면으로 교체 (2026-10-19)
--
-- 기존 /admin/orders 는 TO-BE가 지어낸 화면(헤더 단위 목록 + 상태변경/CSV)이었고 16401~16406·16409가 전부 같은 주소를 가리켰다.
-- AS-IS 전체주문관리(/opmanager/order/list)를 그대로 옮겼으므로(OrderListController, templates/order/list-all.html) 16401만 새 주소를 가리키게 한다.
-- 나머지(신규주문·배송준비중·배송중·배송완료·구매확정·오프라인 주문관리)는 각자 AS-IS 화면으로 옮길 때까지 기존 /admin/orders 를 그대로 둔다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<admindb 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U admindb -d ghlove_core < migration-admin-menu-16401-order-list.sql

UPDATE admin.op_menu
   SET menu_url = '/order/list'
 WHERE menu_id = 16401;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id BETWEEN 16401 AND 16409
 ORDER BY menu_id;
