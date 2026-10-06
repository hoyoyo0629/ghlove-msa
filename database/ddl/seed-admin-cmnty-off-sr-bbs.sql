-- 오프라인 담당자 SR 게시판(메뉴 11406) 기능확인용 시드.
--
-- 작성자는 4601 오프라인담당자 시드(`seed-admin-off-person-in-charge.sql`)가 만든 담당자들을 쓴다:
--   9701 김주담(011 농협은행 / 서울중앙지점) · 9705 정주담(012 농축협 / 수원농축협)
--   9707 강부담(035 제주은행 / 제주본점) · 1000 관리자(시스템) · 1004 행안부
-- 그래서 소속 표기 3가지(시스템 관리자 / 행정안전부 / [은행명] 지점명)와
-- 은행 검색(011·012·035)·소속지점 LIKE 검색을 모두 확인할 수 있다.
--
-- 댓글에는 지자체 담당자(1001, ROLE_ADMIN_5)도 하나 달아 둔다 - AS-IS 화면이 댓글에만
-- 지자체 소속 분기를 갖고 있어서(메뉴 권한은 1·2·7·8인데 과거 데이터를 고려한 분기) 그 경로도 확인한다.

insert into admin.g_cmnty_off_sr_bbs
    (bbs_ttl, bbs_cn, use_yn, notice_yn, inq_cnt, frst_crt_id, frst_crt_dt, last_mdfcn_id, last_mdfcn_dt, is_secret)
values
    ('[공지] 오프라인 접수 오류 신고 방법', '<p>지점에서 발생한 오류는 이 게시판으로 알려주세요.</p>',
     'Y', 'Y', 15, 1000, now() - interval '8 day', 1000, now() - interval '7 day', 'N'),
    ('기탁서 출력 시 글자 겹침', '<p>서울중앙지점에서 기탁서 출력이 겹쳐 나옵니다.</p>',
     'Y', 'N', 6, 9701, now() - interval '5 day', null, null, 'N'),
    ('[비밀글] 전자납부번호 중복 발생 건', '<p>작성자와 시스템 관리자만 볼 수 있어야 합니다.</p>',
     'Y', 'N', 3, 9705, now() - interval '4 day', null, null, 'Y'),
    ('제주본점 단말 접속 지연', '<p>오전에만 접속이 느립니다.</p>',
     'Y', 'N', 2, 9707, now() - interval '2 day', null, null, 'N'),
    ('행안부 공지 - 연말 접수 일정', '<p>연말 접수 마감 일정을 공유합니다.</p>',
     'Y', 'N', 9, 1004, now() - interval '1 day', null, null, 'N'),
    ('삭제된 글 - 목록·상세에 보이면 안 된다', '<p>use_yn = N</p>',
     'N', 'N', 0, 9701, now() - interval '1 day', 9701, now() - interval '1 day', 'N');

-- 본문 첨부: '기탁서 출력 시 글자 겹침'에 1건
insert into admin.g_cmnty_off_sr_bbs_file
    (bbs_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, '기탁서_출력오류.png', '20261004130000000_기탁서_출력오류.png', 'png', 1536000, 1,
       'cmnty/off-sr-bbs', 'Y', b.frst_crt_id, b.frst_crt_dt
  from admin.g_cmnty_off_sr_bbs b
 where b.bbs_ttl = '기탁서 출력 시 글자 겹침' and b.use_yn = 'Y';

-- 댓글: '기탁서 출력 시 글자 겹침'에 3건(시스템 관리자 / 지자체 담당자 / 오프라인 부담당자) + 삭제 1건
insert into admin.g_cmnty_off_sr_bbs_cmnt (bbs_id, cmnt_cn, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, c.cmnt_cn, c.use_yn, c.frst_crt_id, now() - c.ago
  from admin.g_cmnty_off_sr_bbs b
  join (values
            ('재현 확인했습니다. 양식 여백을 조정하겠습니다.', 'Y', 1000, interval '5 day'),
            ('저희 지자체에서도 같은 증상이 있었습니다.',      'Y', 1001, interval '4 day'),
            ('서울북부지점도 동일합니다.',                     'Y', 9704, interval '3 day'),
            ('삭제된 댓글 - 보이면 안 된다',                   'N', 9704, interval '3 day')
       ) as c(cmnt_cn, use_yn, frst_crt_id, ago) on true
 where b.bbs_ttl = '기탁서 출력 시 글자 겹침' and b.use_yn = 'Y';

-- 댓글 첨부: 시스템 관리자(1000) 댓글에 1건
insert into admin.g_cmnty_off_sr_bbs_cmnt_file
    (cmnt_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select c.cmnt_id, '수정된_기탁서양식.hwp', '20261004130100000_수정된_기탁서양식.hwp', 'hwp', 204800, 1,
       'cmnty/off-sr-bbs-cmnt', 'Y', 1000, now() - interval '5 day'
  from admin.g_cmnty_off_sr_bbs_cmnt c
 where c.use_yn = 'Y' and c.frst_crt_id = 1000
   and c.cmnt_cn = '재현 확인했습니다. 양식 여백을 조정하겠습니다.';
