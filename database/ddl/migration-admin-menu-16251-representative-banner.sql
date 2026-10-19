-- 답례품관리 > 대표배너관리(메뉴 16251)의 menu_url을 AS-IS 이식 화면으로 교체
--
-- 기존 /admin/main-banners 는 같은 G_REPRST_BANNER(PROCESS_TYPE='ITEM')를 다루는 TO-BE 축소 구현(노출순서 입력칸·토글/삭제 버튼 등 AS-IS에 없는
-- 것들)이었다. AS-IS 대표배너관리(/opmanager/representative-banner/list)를 그대로 옮겼으므로(ItemRepresentativeBannerController,
-- templates/representative-banner/*; 서비스는 특정사업 기부 배너관리와 공유) 메뉴가 그쪽을 가리키게 한다. 옛 화면(MainBannerAdminController,
-- MainBannerRepBannerService, main-banner-admin 템플릿)은 삭제했다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-menu-16251-representative-banner.sql

UPDATE admin.op_menu
   SET menu_url = '/representative-banner'
 WHERE menu_id = 16251;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id = 16251;
