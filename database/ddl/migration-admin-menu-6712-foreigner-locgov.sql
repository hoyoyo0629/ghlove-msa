-- 통계 > 외국인 기부 현황 > 지자체별(메뉴 6712)의 menu_url 설정
--
-- AS-IS 메뉴 6712는 /opmanager/give/statistics/foreigner/locgov(기부금 모금현황 지자체별 화면의 외국인 변형)로
-- 연결되지만 TO-BE에는 화면이 없어 op_menu.menu_url이 비어 있었다("누락된 메뉴").
-- 화면을 이식했으므로(GiveStatisticsController#foreignerLocgov, templates/give/give-statistics-locgov.html의 foreigner 분기 +
-- 월별 상세 give-statistics-locgov-detail.html) 일반 지자체별(6707 /give-statistics/locgov)과 같은 규칙으로
-- /give-statistics/foreigner/locgov 를 넣는다. 권한(op_menu_right)은 6712=ROLE_ADMIN_1~6이 이미 들어 있다
-- (지자체 담당자 5/6은 목록 대신 자기 지자체 상세로 바로 이동한다).
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-6712-foreigner-locgov.sql

UPDATE admin.op_menu
   SET menu_url = '/give-statistics/foreigner/locgov'
 WHERE menu_id = 6712;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id IN (6707, 6711, 6712);
