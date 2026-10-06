-- 메뉴 11403 지자체FAQ 등록 - **AS-IS와 같이 '중지' 상태로** 넣는다.
--
-- AS-IS op_menu 실측(`OP_MENU_202610010945.sql`):
--   (11403, 11400, '지자체FAQ', '/opmanager/community/locv-faq/list', 3, 'N', '2')
--   → display_flag='N', status_code='2'(중지). 즉 AS-IS에서 이 메뉴는 꺼져 있다.
--
-- TO-BE op_menu에는 이 행이 **아예 없었다**. [[as-is-parity-includes-disabled-state]] 원칙
-- ("AS-IS에서 숨김/비활성이면 우리도 동일하게 유지 - 빼지도 켜지도 말 것")에 따라
-- 행을 만들되 중지 상태를 그대로 둔다.
--
-- status_code='2'라 MenuService.visibleMenus()(status_code='1'만 조회)에 걸리지 않으므로
-- nav에도 안 나오고 breadcrumb/activeMenu 해석에도 영향이 없다 - 등록만 되는 셈이다.
--
-- ★ 화면 이식은 하지 않았다(사용자 결정 필요): 이 메뉴의 AS-IS 컨트롤러가 다루는 표
--   op_community_locgovfaq 는 **TO-BE에서 고객센터 공개 FAQ(/faqs, /api/faqs)의 실데이터**(63행)이고
--   같은 URL(/community/locv-faq)에 그걸 관리하는 TO-BE 화면이 이미 살아서 쓰이고 있다.
--   AS-IS가 꺼 둔 화면을 그 위에 덮으면 살아있는 공개 FAQ 관리 화면이 깨질 수 있다.

insert into admin.op_menu
    (menu_id, menu_parent_id, menu_type, menu_name, menu_code, menu_url, menu_seq, menu_icon,
     display_flag, status_code)
select 11403, 11400, 3, '지자체FAQ', null, '/community/locv-faq', 3, null, 'N', '2'
 where not exists (select 1 from admin.op_menu where menu_id = 11403);

-- AS-IS op_menu_right 실측에 11403 행은 없다(메뉴가 중지라 권한도 부여돼 있지 않다) - 추가하지 않는다.
