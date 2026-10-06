-- 내부문의(지자체담당자↔본사, AS-IS QnaAdminManagerController) - G_QNA_ADMIN / G_QNA_ADMIN_ANSWER는
-- 테이블만 이관돼 있고 시퀀스가 없어 admin 서비스가 PK를 생성할 수 없었다. admin 관례(op_notice_notice_id_seq 등)대로
-- 명명한 시퀀스를 만든다. 테이블은 0행이므로 1부터 시작.
CREATE SEQUENCE IF NOT EXISTS admin.g_qna_admin_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS admin.g_qna_admin_answer_id_seq START WITH 1 INCREMENT BY 1;
GRANT USAGE, SELECT, UPDATE ON admin.g_qna_admin_id_seq, admin.g_qna_admin_answer_id_seq TO admindb;

-- 내비게이션 메뉴 등록 (부모 100 = 커뮤니티/고객센터, 형제: 1:1문의관리(17)/지자체FAQ(36)/FAQ게시판(41)).
-- 본사(ROLE_ADMIN_1~4)는 UNRESTRICTED라 자동 노출, 지자체담당자(5/6)는 OP_MENU_RIGHT 필요(문의 작성용).
INSERT INTO admin.op_menu (menu_id, menu_parent_id, menu_type, menu_name, menu_code, menu_url, menu_seq, display_flag, status_code)
VALUES (1903, 100, NULL, '내부문의 관리', NULL, '/admin/internal-inquiry', 6, 'Y', '1')
ON CONFLICT (menu_id) DO NOTHING;
INSERT INTO admin.op_menu_right (menu_right_id, authority, menu_id)
SELECT (SELECT COALESCE(MAX(menu_right_id),0) FROM admin.op_menu_right)+1, 'ROLE_ADMIN_5', 1903
WHERE NOT EXISTS (SELECT 1 FROM admin.op_menu_right WHERE menu_id=1903 AND authority='ROLE_ADMIN_5');
INSERT INTO admin.op_menu_right (menu_right_id, authority, menu_id)
SELECT (SELECT COALESCE(MAX(menu_right_id),0) FROM admin.op_menu_right)+1, 'ROLE_ADMIN_6', 1903
WHERE NOT EXISTS (SELECT 1 FROM admin.op_menu_right WHERE menu_id=1903 AND authority='ROLE_ADMIN_6');
