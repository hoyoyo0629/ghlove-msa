-- 연계 로그 메뉴 1413~1416을 각각의 화면으로 분리
--
-- AS-IS는 네 개의 **별개 화면**이다(컬럼 11/9/34/37, 제목·검색조건·원천표가 모두 다르다):
--   1413 서울부과로그관리 → /opmanager/log/gif-seoul-buga   (gif_seoul)
--   1414 서울수납로그관리 → /opmanager/log/gif-seoul-sunap  (gif_etax_sunap)
--   1415 지방부과로그관리 → /opmanager/log/gif-stnd-buga    (g_next_buga_request + g_locgov)
--   1416 지방수납로그관리 → /opmanager/log/gif-stnd-sunap   (g_next_sunap_response)
--
-- TO-BE는 네 메뉴가 모두 /log/levy 한 화면("연계 로그 관리 (국세청 부과·수납)")을 가리키고
-- 있었다. AS-IS대로 4개 화면을 이식했으므로(LevyLinkLogController) menu_url도 각각으로 바로잡는다.
--
-- 기존 /log/levy 화면(TO-BE가 만든 DONATION_LEVY 통합 조회 + 백필)은 지우지 않는다 - AS-IS에
-- 없는 운영 도구라 메뉴에서만 빠지고 경로는 그대로 남는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-1413-1416-levy-split.sql

UPDATE admin.op_menu SET menu_url = '/log/levy/seoul-buga'  WHERE menu_id = 1413;
UPDATE admin.op_menu SET menu_url = '/log/levy/seoul-sunap' WHERE menu_id = 1414;
UPDATE admin.op_menu SET menu_url = '/log/levy/stnd-buga'   WHERE menu_id = 1415;
UPDATE admin.op_menu SET menu_url = '/log/levy/stnd-sunap'  WHERE menu_id = 1416;

SELECT menu_id, menu_name, menu_url, menu_type, status_code
  FROM admin.op_menu
 WHERE menu_id BETWEEN 1413 AND 1416
 ORDER BY menu_id;
