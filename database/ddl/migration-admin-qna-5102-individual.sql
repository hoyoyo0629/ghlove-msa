-- 1:1 문의 (메뉴 5102) 이식 - AS-IS /opmanager/qna (QnaManagerController)
--
-- (1) 메뉴 URL 교정: /admin/shop-inquiries → /admin/inquiries
--     기존 TO-BE 5102는 AS-IS **입점문의**(InquiryManagerController, /opmanager/inquiry)를
--     조회 전용으로 옮겨 놓은 것이었고, 표도 AS-IS의 OP_STORE_INQUIRY가 아닌 OP_SHOP_INQUIRY를
--     새로 만들어 쓰고 있었다(0행). 1:1문의의 AS-IS 정본은 OP_QNA이고 Q&A(5112)와 같은 표다 -
--     구분은 QNA_TYPE('0'=1:1문의, '1'=상품문의(5103 중지), '2'=Q&A).
--     * AS-IS 입점문의는 op_menu에 행이 없어 메뉴로 접근할 수 없는 화면이고(SalesOn 쇼핑몰
--       입점 상담) op_store_inquiry도 0행이다. 별도 판단 대상으로 남긴다.
--
-- (2) 1:1문의 검증용 시드 5건 (QNA_TYPE='0')
--     기존 op_qna 9건은 Q&A(5112) 라운드에서 qna_type='2'로 채워 둔 시드라, 1:1문의 화면에는
--     조회할 행이 없었다. 공개 마이페이지 1:1문의 화면도 같은 행을 쓴다(qna_type='0' 필터).
--     회원은 member.op_user의 기존 시드 회원(1000~1002)을 그대로 쓴다.
--     QNA_GROUP은 공통코드 QNA_GROUPS의 id다.

-- (1) 메뉴 URL 교정
update admin.op_menu set menu_url = '/admin/inquiries' where menu_id = 5102;

-- (2) 1:1문의 시드 (답변 2건 포함)
insert into admin.op_qna (qna_id, qna_type, qna_group, subject, question, user_id, user_name, email,
                          created_date, answer_count, secret_flag, display_flag, hits, data_status_code, use_yn)
values
(1100, '0', '3',  '회원정보의 휴대폰 번호를 바꾸고 싶습니다',
       '회원정보 수정 화면에서 휴대폰 번호 변경이 저장되지 않습니다. 확인 부탁드립니다.',
       1000, '김진호', 'hoyoyo0629@naver.com', '20260901101500', 1, 'Y', 'Y', 0, '0', 'Y'),
(1101, '0', '6',  '기부확인증에 주소가 잘못 표기되어 있습니다',
       '발급받은 기부확인증의 주소가 이전 주소로 나옵니다. 재발급이 가능한가요?',
       1000, '김진호', 'hoyoyo0629@naver.com', '20260903143000', 0, 'Y', 'Y', 0, '0', 'Y'),
(1102, '0', '9',  '결제가 두 번 된 것 같습니다',
       '같은 기부금이 두 번 결제된 것으로 보입니다. 확인 후 환불 부탁드립니다.',
       1001, '홍길동', 'testuser01@example.com', '20260905091000', 1, 'Y', 'Y', 0, '0', 'Y'),
(1103, '0', '10', '답례품 배송지를 변경하고 싶습니다',
       '주문한 답례품의 배송지를 아직 바꿀 수 있는지 궁금합니다.',
       1002, '이서연', 'mypagetest01@example.com', '20260908160000', 0, 'N', 'Y', 0, '0', 'Y'),
(1104, '0', '3',  '탈퇴하면 기부내역도 사라지나요?',
       '회원 탈퇴 시 그동안의 기부내역과 포인트가 어떻게 되는지 알고 싶습니다.',
       1002, '이서연', 'mypagetest01@example.com', '20260910113000', 0, 'N', 'Y', 0, '0', 'Y')
on conflict (qna_id) do nothing;

insert into admin.op_qna_answer (qna_answer_id, qna_id, answer, title, answer_date, user_id,
                                 send_sms_flag, send_mail_flag, data_status_code)
values
(1100, 1100, '안녕하세요. 휴대폰 번호 변경은 본인인증을 다시 거쳐야 저장됩니다. 인증 후 다시 시도해 주시기 바랍니다.',
       '문의에 대한 답변입니다.', '20260901171000', 1001, 'N', 'N', '0'),
(1101, 1102, '안녕하세요. 중복 결제분을 확인하여 환불 처리하였습니다. 영업일 기준 3일 내 입금됩니다.',
       '문의에 대한 답변입니다.', '20260905151500', 1001, 'N', 'N', '0')
on conflict (qna_answer_id) do nothing;

-- 시퀀스를 시드 뒤로 옮겨 둔다(신규 등록이 충돌하지 않도록).
select setval('admin.op_qna_qna_id_seq',
              greatest(coalesce((select max(qna_id) + 1 from admin.op_qna), 1000), 1000), false);
select setval('admin.op_qna_answer_qna_answer_id_seq',
              greatest(coalesce((select max(qna_answer_id) + 1 from admin.op_qna_answer), 1000), 1000), false);
