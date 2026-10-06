-- 기부혜택증관리 구조갭 해소: 19101(설정 관리=목록)과 19102(설정=폼)가 TO-BE에서 같은 URL
-- (/admin/honor-users)을 가리키고 있었다.
--
-- AS-IS export(OP_MENU_202610010945.sql) 실측:
--   (19101,19100,'기부혜택증 설정 관리','/opmanager/lclgvHnrUser/lclgvHnrUserMng/list',1,'Y','1')
--   (19102,19100,'기부혜택증 설정',    '/opmanager/lclgvHnrUser/lclgvHnruserMng/form',2,'Y','1')
--   (19103,19100,'기부혜택증 열람현황','/opmanager/lclgvHnrUser/lclgvHnrUserViewHist/list',3,'Y','1')
-- 즉 19101은 목록, 19102는 설정 폼인 별개 화면이다(4402·4501과 같은 유형의 구조갭).
--
-- AS-IS 19102는 지자체코드 없는 /form이고, 지자체 담당자(ROLE_ADMIN_5·6)가 누르면 자기 지자체
-- 설정 폼으로 들어가고 그 외 권한은 홈으로 돌려보낸다(LclgvHnrUserManagerController 그대로).

UPDATE admin.op_menu
   SET menu_url = '/admin/honor-users/form'
 WHERE menu_id = 19102;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (19101, 19102, 19103)
 ORDER BY menu_id;
