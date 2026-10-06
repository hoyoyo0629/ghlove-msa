-- A라운드 B2: 콘텐츠 만족도(만족도 조사) 신규 구현 후 메뉴 배선. (AS-IS cntnts-stsfdg, 통계>만족도 조사)
SET search_path TO admin;
UPDATE op_menu SET menu_url='/admin/content-satisfaction', display_flag='Y' WHERE menu_id=5108;
