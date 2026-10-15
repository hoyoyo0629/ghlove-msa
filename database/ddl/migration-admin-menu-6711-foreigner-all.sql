-- 통계 > 외국인 기부 현황 > 전체(메뉴 6711)의 menu_url 설정
--
-- AS-IS 메뉴 6711은 /opmanager/give/statistics/foreigner/all(기부금 모금현황 전체 화면의 외국인 변형)로 연결되지만
-- TO-BE에는 화면이 없어 op_menu.menu_url이 비어 있었다(메뉴만 있고 눌러도 아무 데도 못 가는 "누락된 메뉴").
-- 화면을 이식했으므로(GiveStatisticsController#foreignerAll, templates/give/give-statistics-all.html의 foreigner 분기)
-- 일반 전체(6706 /give-statistics/all)와 같은 규칙으로 /give-statistics/foreigner/all 을 넣는다.
-- 6712(지자체별)는 화면이 아직 없어 그대로 둔다. 권한(op_menu_right)은 AS-IS와 같이 6711=ROLE_ADMIN_1~4가 이미 들어 있다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-6711-foreigner-all.sql

UPDATE admin.op_menu
   SET menu_url = '/give-statistics/foreigner/all'
 WHERE menu_id = 6711;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id IN (6706, 6711, 6712);
