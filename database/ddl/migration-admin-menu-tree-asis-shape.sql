-- op_menu 트리 구조를 AS-IS와 동일하게 맞춘다. 메뉴관리 화면(1409)이 이 구조에 의존한다.
--  * AS-IS는 menu_id=0('root') 행이 있고 최상위 메뉴의 menu_parent_id가 0이다.
--    TO-BE는 root 행이 없고 최상위가 menu_parent_id IS NULL이었다(전수 대조에서 21건 불일치로 나왔던 것).
--    AS-IS 계층 쿼리는 START WITH MENU_ID='0' CONNECT BY PRIOR MENU_ID = MENU_PARENT_ID 라서
--    root 행과 parent=0 이 없으면 트리가 전개되지 않는다.
--  * AS-IS는 menu_type(1=상단 2=LNB섹션 3=링크)을 채운다. TO-BE는 183행 전부 NULL이어서
--    메뉴관리의 ID 채번(레벨별 MAX+1000/+100/+1)과 목록 들여쓰기가 동작하지 않았다.
-- 재실행 가능.

-- [1] root 행 (AS-IS export의 (0,NULL,'root',NULL,NULL,'N','2') 그대로)
INSERT INTO admin.op_menu (menu_id, menu_parent_id, menu_name, menu_url, menu_seq, display_flag, status_code)
SELECT 0, NULL, 'root', NULL, NULL, 'N', '2'
WHERE NOT EXISTS (SELECT 1 FROM admin.op_menu WHERE menu_id = 0);

-- [2] 최상위 메뉴의 부모를 0으로
UPDATE admin.op_menu SET menu_parent_id = 0 WHERE menu_parent_id IS NULL AND menu_id <> 0;

-- [3] menu_type 채우기 - 깊이 그대로 1/2/3
UPDATE admin.op_menu SET menu_type = 1 WHERE menu_parent_id = 0;
UPDATE admin.op_menu m SET menu_type = 2
 WHERE m.menu_parent_id <> 0
   AND EXISTS (SELECT 1 FROM admin.op_menu p WHERE p.menu_id = m.menu_parent_id AND p.menu_parent_id = 0);
UPDATE admin.op_menu m SET menu_type = 3
 WHERE m.menu_parent_id <> 0
   AND EXISTS (SELECT 1 FROM admin.op_menu p WHERE p.menu_id = m.menu_parent_id AND p.menu_parent_id <> 0);
