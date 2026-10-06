-- A라운드 B1: AS-IS에 있고 TO-BE에도 이미 화면이 구현됐으나 메뉴트리 재시드 때 'TO-BE 미대응'으로
-- 판단해 display='N'·url=NULL로 숨겨둔 메뉴들을 실제 구현 URL로 배선하고 노출(Y)시킨다.
-- (권한 OP_MENU_RIGHT는 재시드 때 이미 들어가 있음. 화면 컨트롤러도 기배포 → 재기동 불필요, 새로고침만.)
SET search_path TO admin;
UPDATE op_menu SET menu_url='/admin/gift-inquiries',             display_flag='Y' WHERE menu_id=16552; -- 답례품 Q&A
UPDATE op_menu SET menu_url='/admin/delivery-companies',         display_flag='Y' WHERE menu_id=1305;  -- 배송업체 관리
UPDATE op_menu SET menu_url='/designated-projects/analysis',     display_flag='Y' WHERE menu_id=17201; -- 특정사업 지자체별통계
UPDATE op_menu SET menu_url='/designated-projects/analysis',     display_flag='Y' WHERE menu_id=17202; -- 특정사업 월별통계
UPDATE op_menu SET menu_url='/designated-projects/banners',      display_flag='Y' WHERE menu_id=17301; -- 특정사업 기부 배너관리
UPDATE op_menu SET menu_url='/admin/honor-users/view-history',   display_flag='Y' WHERE menu_id=19103; -- 기부혜택증 열람현황
