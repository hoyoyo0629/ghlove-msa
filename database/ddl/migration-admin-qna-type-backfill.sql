-- Q&A 관리(메뉴 5112) 조회를 위한 qna_type 보정.
--
-- AS-IS는 op_qna 한 표를 세 화면이 공유하고 `qna_type`으로 가른다:
--   '0' 1:1문의(5102) / '1' 상품문의(5103, AS-IS 메뉴 중지) / '2' 공개 Q&A(5112)
-- 목록 SQL도 `QNA_TYPE = 2`를 하드코딩하고 있다(QnaMapper.getFrontQnaOpenManagerList).
--
-- TO-BE에 시드된 9행은 `qna_type`이 **전부 NULL**이어서 그대로면 Q&A 목록이 0건이 된다.
-- 내용(기부/답례품/배송/포인트 문의 + 공개게시판용 secret_flag·display_flag·hits)으로 보면
-- 전부 **공개 Q&A** 데이터이고, 실제로 공개 Q&A 게시판(/qna)이 이 행들을 읽고 있다.
-- 그래서 SQL 조건을 느슨하게 바꾸는 대신 **데이터에 '2'를 채운다**(AS-IS 의미에 맞춤).
--
-- 1:1문의(5102)는 TO-BE가 별도 표(OP_SHOP_INQUIRY)를 쓰고 있어 이 보정과 무관하다 -
-- 그 화면을 op_qna로 옮기는 작업은 ② 순서의 마지막(1:1문의 보강)에서 다룬다.

update admin.op_qna
   set qna_type = '2'
 where qna_type is null;
