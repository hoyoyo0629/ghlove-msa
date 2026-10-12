-- 자료실(메뉴 5107) 첨부파일 ID 시퀀스
--
-- AS-IS DataboardServiceImpl은 첨부 한 건마다
--   itemImage.setDataFileId(String.valueOf(sequenceService.getId("OP_DATA_BOARD_FILE")))
-- 로 CUBRID serial에서 번호를 받는다. TO-BE에는 그 시퀀스가 없어 그동안 UUID 문자열을
-- 넣으려 했는데 DATA_FILE_ID 컬럼이 integer라 첨부 업로드는 실행되면 반드시 실패하는
-- 코드였다(아무도 쓰지 않아 드러나지 않았다). AS-IS와 같은 숫자 채번으로 바로잡는다.
--
-- 시작값은 기존 시드의 최대값(1005) 다음부터.

CREATE SEQUENCE IF NOT EXISTS admin.op_data_board_file_seq START WITH 1006 INCREMENT BY 1;

SELECT setval('admin.op_data_board_file_seq',
              GREATEST((SELECT COALESCE(MAX(data_file_id), 1005) FROM admin.op_data_board_file), 1005));
