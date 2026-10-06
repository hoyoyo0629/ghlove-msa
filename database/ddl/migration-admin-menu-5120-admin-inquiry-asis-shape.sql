-- 관리자 문의(내부문의) 메뉴를 AS-IS 모양으로 바로잡는다.
-- 2026-10-06, AS-IS 개발DB(ghlove2.OP_MENU) 직접 조회로 확인.
--
-- 무엇이 틀렸나
--  AS-IS에는 이 화면이 **답례품관리 > 관리자 문의 > 관리자 문의(menu_id 16651,
--  /opmanager/qna-admin/list)** 로 존재한다. TO-BE는
--   ① 16651의 menu_url을 `/qna-admin`으로 잘못 넣어 **5112 Q&A와 같은 URL**을 가리키게 하고
--      (`/qna-admin`은 AS-IS QnaOpenManagerController = 공개 Q&A = 5112다),
--   ② 실제 화면(`/admin/internal-inquiry`, InternalInquiryAdminController)은
--      **AS-IS에 없는 menu_id 5120 "내부문의 관리"** 를 새로 만들어 고객센터>문의관리 아래 달아놨다.
--  그래서 1404 사용자 권한 관리의 메뉴권한 트리에도 AS-IS에 없는 항목이 하나 더 나왔다
--  (AS-IS 119건 vs TO-BE 120건, 차이는 5120 하나뿐이었다).
--
-- 조치
--  1) 16651의 menu_url을 실제 화면 경로로 바로잡는다. AS-IS 경로(/opmanager/qna-admin/list)에
--     대응하는 TO-BE 경로가 /admin/internal-inquiry다.
--  2) 5120과 그 메뉴권한 6건을 제거한다. 16651은 이미 AS-IS와 같은 권한 6건
--     (ROLE_ADMIN_1~6)을 갖고 있어 옮길 것이 없다 - 확인 후 제거한다.
--  화면·컨트롤러(InternalInquiryAdminController)는 그대로 둔다. 없어지는 건 중복 메뉴 항목뿐이다.
--
-- 재실행 안전.

-- 1) AS-IS 16651이 가리켜야 할 실제 경로
UPDATE admin.op_menu
   SET menu_url = '/admin/internal-inquiry'
 WHERE menu_id = 16651
   AND menu_name = '관리자 문의';

-- 2) AS-IS에 없는 5120 제거. 16651이 ROLE_ADMIN_1~6 권한을 이미 갖고 있을 때만 지운다
--    (권한이 통째로 사라지는 사고 방지 - AS-IS 16651 권한과 동일한 집합이다).
DELETE FROM admin.op_menu_right
 WHERE menu_id = 5120
   AND (SELECT count(*) FROM admin.op_menu_right WHERE menu_id = 16651) >= 6;

DELETE FROM admin.op_menu
 WHERE menu_id = 5120
   AND NOT EXISTS (SELECT 1 FROM admin.op_menu_right WHERE menu_id = 5120);
