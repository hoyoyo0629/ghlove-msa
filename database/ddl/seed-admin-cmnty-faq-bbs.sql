-- 담당자용 FAQ(메뉴 11405) 기능확인용 시드.
--
-- 질문유형 탭 필터·질문유형 컬럼 표기·공지·비밀글·첨부·댓글을 모두 밟아볼 수 있게 짰다.
-- 질문유형 코드는 CMNTY_FAQ_TYPE(= AS-IS enum FaqType)의 id를 쓴다.
--
-- ★예전 TO-BE 골격이 남긴 1건(faq_type='1', 쇼핑몰 FAQ_TYPE 코드)은 그대로 둔다 -
--   질문유형 라벨을 못 찾는 행이 목록에서 어떻게 보이는지(AS-IS는 빈 값) 확인하는 데 쓰인다.

insert into admin.g_cmnty_faq_bbs
    (bbs_ttl, bbs_cn, faq_type, use_yn, notice_yn, inq_cnt, frst_crt_id, frst_crt_dt,
     last_mdfcn_id, last_mdfcn_dt, is_secret)
values
    ('[공지] FAQ 작성 기준', '<p>담당자가 자주 묻는 질문을 유형별로 등록해 주세요.</p>',
     'F_SYSTEM', 'Y', 'Y', 33, 1000, now() - interval '12 day', 1000, now() - interval '11 day', 'N'),
    ('기부금 영수증은 언제 발급되나요?', '<p>연말정산 간소화 자료는 다음 해 1월에 제공됩니다.</p>',
     'F_API_PLATFORM', 'Y', 'N', 18, 1000, now() - interval '7 day', null, null, 'N'),
    ('오프라인 기부 접수 취소 방법', '<p>지점에서 당일에 한해 취소할 수 있습니다.</p>',
     'F_OFF_CNTR', 'Y', 'N', 11, 1004, now() - interval '5 day', null, null, 'N'),
    ('답례품 배송 조회가 안 될 때', '<p>배송사 연동 지연일 수 있습니다.</p>',
     'F_ORDER', 'Y', 'N', 7, 1001, now() - interval '3 day', null, null, 'N'),
    ('[비밀글] 포인트 소멸 기준 내부 안내', '<p>작성자와 시스템 관리자만 볼 수 있어야 합니다.</p>',
     'F_CNTR_POINT', 'Y', 'N', 4, 1001, now() - interval '2 day', null, null, 'Y'),
    ('삭제된 FAQ - 목록·상세에 보이면 안 된다', '<p>use_yn = N</p>',
     'F_ETC', 'N', 'N', 0, 1001, now() - interval '1 day', 1001, now() - interval '1 day', 'N');

-- 본문 첨부: '기부금 영수증은 언제 발급되나요?'에 1건
insert into admin.g_cmnty_faq_bbs_file
    (bbs_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, '연말정산_안내.pdf', '20261004140000000_연말정산_안내.pdf', 'pdf', 3145728, 1,
       'cmnty/faq-bbs', 'Y', b.frst_crt_id, b.frst_crt_dt
  from admin.g_cmnty_faq_bbs b
 where b.bbs_ttl = '기부금 영수증은 언제 발급되나요?' and b.use_yn = 'Y';

-- 댓글: '답례품 배송 조회가 안 될 때'에 2건 + 삭제 1건
insert into admin.g_cmnty_faq_bbs_cmnt (bbs_id, cmnt_cn, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, c.cmnt_cn, c.use_yn, c.frst_crt_id, now() - c.ago
  from admin.g_cmnty_faq_bbs b
  join (values
            ('배송사 연동 주기가 1시간입니다.',     'Y', 1000, interval '3 day'),
            ('안내 문구에 추가하면 좋겠습니다.',    'Y', 1004, interval '2 day'),
            ('삭제된 댓글 - 보이면 안 된다',        'N', 1004, interval '2 day')
       ) as c(cmnt_cn, use_yn, frst_crt_id, ago) on true
 where b.bbs_ttl = '답례품 배송 조회가 안 될 때' and b.use_yn = 'Y';
