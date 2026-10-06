-- 구조갭 해소: 메뉴 5112 Q&A의 menu_url이 **빈 값**이어서 메뉴가 보이는데도 클릭이 되지 않았다.
--
-- AS-IS url은 `/opmanager/qna-open/list`(QnaOpenManagerController)이고, 표는 **op_qna/op_qna_answer/
-- op_qna_file/op_qna_answer_file**이다 - 1:1 문의(5102)와 **같은 표를 공유**하고 조회조건만 다르다.
--   - 5102 1:1 문의 : qna_type = '0'(개인) 으로 고정
--   - 5112 Q&A     : AS-IS가 qna_type 필터를 **주석 처리**해 두어 사실상 전체를 본다
--     (`// qnaParam.setQnaType(Qna.QNA_GROUP_TYPE_QNA);` - 값은 '2'다)
--
-- TO-BE에는 이 표들을 쓰는 관리화면이 이미 `/qna-admin`(QnaAdminController)으로 있고 메뉴에만
-- 연결돼 있지 않았다. 그래서 5112를 그 URL로 연결한다.
--
-- ★이름 주의: TO-BE `/qna-admin`은 **공개 Q&A(5112)** 관리화면이고,
--   AS-IS `/opmanager/qna-admin`은 **내부문의(5120, G_QNA_ADMIN)**다. 서로 다른 화면이니
--   혼동하지 말 것(TO-BE 내부문의는 `/admin/internal-inquiry`에 있다).

update admin.op_menu
   set menu_url = '/qna-admin'
 where menu_id = 5112 and (menu_url is null or menu_url = '');
