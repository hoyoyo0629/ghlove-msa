-- 관리자 문의(메뉴 16651, AS-IS opmanager/qna-admin) AS-IS 이식에 맞춘 데이터 정리 (2026-10-20)
--
-- AS-IS G_QNA_ADMIN은 "답례품 제공자(판매자)가 판매자 포털 '지자체 문의'로 자기 지자체에 남기는 문의"이고, 운영관리 화면은 지자체 담당자가 답만 한다.
-- 예전 TO-BE 화면(InternalInquiryAdminController)은 이걸 "지자체 담당자 → 본사 문의"로 잘못 읽어 운영관리에서 문의를 새로 쓰게 만들었고,
-- 그 화면으로 만든 행 1건(qna_admin_id=1, 상태값도 AS-IS에 없는 'Y')이 남아 있다. AS-IS 목록 조건(DATA_STATUS_CODE='0' AND DISPLAY_FLAG='Y')에도
-- 걸리지 않는 발명 데이터라 지운다(답변·첨부 없음 확인 후).
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -i -e PGPASSWORD=<admindb 비밀번호> postgres:16-alpine \
--         psql -h <DB 호스트> -p 15432 -U admindb -d ghlove_core < migration-admin-qna-admin-asis-port.sql

DELETE FROM admin.g_qna_admin q
 WHERE q.qna_admin_id = 1
   AND q.data_status_code = 'Y'
   AND NOT EXISTS (SELECT 1 FROM admin.g_qna_admin_answer a WHERE a.qna_admin_id = q.qna_admin_id)
   AND NOT EXISTS (SELECT 1 FROM admin.g_qna_admin_file f WHERE f.qna_admin_id = q.qna_admin_id);

SELECT count(*) AS remaining_rows FROM admin.g_qna_admin;
