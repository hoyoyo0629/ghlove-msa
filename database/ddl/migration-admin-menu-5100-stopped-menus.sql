-- 고객센터>문의관리(5100)의 **AS-IS 중지 메뉴 5건** 등록.
--
-- AS-IS op_menu 실측(`OP_MENU_202610010945.sql`) - 다섯 건 모두 display_flag='N', status_code='2'(중지):
--   (5101,5100,'상품평 관리','/opmanager/item/review/list',1,'N','2')
--   (5103,5100,'상품문의','/opmanager/qna-item/list',6,'N','2')
--   (5105,5100,'민원신고','/opmanager/claim-memo/list',7,'N','2')
--   (5106,5100,'행사이벤트','/opmanager/event/list',3,'N','2')
--   (5109,5100,'매뉴얼관리','/opmanager/manual/list',9,'N','2')
--
-- TO-BE op_menu에는 이 다섯 행이 **아예 없었다**. [[as-is-parity-includes-disabled-state]] 원칙
-- ("AS-IS에서 숨김/비활성이면 우리도 동일하게 유지 - 빼지도 켜지도 말 것")에 따라
-- 행을 만들되 중지 상태를 그대로 둔다. 11403 지자체FAQ와 같은 처리다.
--
-- status_code='2'라 MenuService.visibleMenus()(='1'만 조회)에 걸리지 않으므로 nav에도 나오지 않고
-- breadcrumb/activeMenu 해석에도 영향이 없다 - 등록만 되는 셈이고 화면 이식 대상도 아니다.
--
-- menu_url은 TO-BE 관례(kebab-case, /admin 접두어)로 적되 **실제 화면은 없다**(중지 메뉴라 불필요).
-- 5109 매뉴얼관리(구)는 AS-IS에서 5200 매뉴얼관리 중분류(5201 관리자매뉴얼/5202 사용자매뉴얼)로
-- 대체된 옛 단일 메뉴다.

insert into admin.op_menu
    (menu_id, menu_parent_id, menu_type, menu_name, menu_code, menu_url, menu_seq, menu_icon,
     display_flag, status_code)
select v.menu_id, 5100, 3, v.menu_name, null, v.menu_url, v.menu_seq, null, 'N', '2'
  from (values
            (5101, '상품평 관리', '/admin/item-reviews', 1),
            (5106, '행사이벤트',  '/admin/events',       3),
            (5103, '상품문의',    '/admin/gift-inquiries', 6),
            (5105, '민원신고',    '/admin/claim-memos',  7),
            (5109, '매뉴얼관리',  '/admin/manuals',      9)
       ) as v(menu_id, menu_name, menu_url, menu_seq)
 where not exists (select 1 from admin.op_menu m where m.menu_id = v.menu_id);

-- AS-IS op_menu_right에도 이 다섯 메뉴의 권한행은 없다(중지라 부여돼 있지 않다) - 추가하지 않는다.
