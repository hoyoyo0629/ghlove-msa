-- SR 게시판(메뉴 11404) 기능확인용 시드.
--
-- 화면 분기를 모두 밟아볼 수 있게 짰다:
--   - 소속 표기 3가지(시스템/행안부/지자체) + 공지글 + 비밀글
--   - 제목 옆 아이콘 2종: 첨부 있음(클립) / 비밀글(자물쇠)
--   - 댓글 3건(1건은 삭제됨) + 댓글 첨부 2건(1건은 삭제됨)
--   - 삭제된 게시글 1건(목록·상세에서 모두 빠져야 한다)
--
-- 첨부파일 행은 넣지만 디스크 파일은 만들지 않는다 - 다운로드를 눌렀을 때 AS-IS와 같이
-- "파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오." alert이 뜨는 것까지 확인용이다.
-- 파일 크기는 B/KB/MB 세 구간을 다 보도록 다르게 넣었다(AS-IS 표기 공식 확인).

insert into admin.g_cmnty_sr_bbs
    (bbs_ttl, bbs_cn, use_yn, notice_yn, inq_cnt, frst_crt_id, frst_crt_dt, last_mdfcn_id, last_mdfcn_dt, is_secret)
values
    ('[공지] SR 접수 절차 안내', '<p>건의사항은 이 게시판으로 접수해 주세요.</p>',
     'Y', 'Y', 21, 1000, now() - interval '10 day', 1000, now() - interval '9 day', 'N'),
    ('기부금 통계 화면 정렬 개선 요청', '<p>등록일 역순 정렬이 기본이었으면 합니다.</p>',
     'Y', 'N', 8, 1001, now() - interval '5 day', null, null, 'N'),
    ('[비밀글] 로그인 오류 재현 로그 첨부', '<p>작성자와 시스템 관리자만 볼 수 있어야 합니다.</p>',
     'Y', 'N', 2, 1001, now() - interval '3 day', null, null, 'Y'),
    ('답례품 재고 동기화 지연 문의', '<p>주문 후 재고 반영이 늦습니다.</p>',
     'Y', 'N', 4, 1004, now() - interval '2 day', null, null, 'N'),
    ('삭제된 SR 글 - 목록·상세에 보이면 안 된다', '<p>use_yn = N</p>',
     'N', 'N', 0, 1002, now() - interval '1 day', 1002, now() - interval '1 day', 'N');

-- 본문 첨부: '기부금 통계 화면 정렬 개선 요청'(1건) / '[비밀글] 로그인 오류…'(1건)
insert into admin.g_cmnty_sr_bbs_file
    (bbs_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, f.orgnl, f.saved, f.extn, f.sz, 1, 'cmnty/sr-bbs', 'Y', b.frst_crt_id, b.frst_crt_dt
  from admin.g_cmnty_sr_bbs b
  join (values
            ('기부금 통계 화면 정렬 개선 요청',          '정렬개선_화면캡쳐.png', '20261004120000000_정렬개선_화면캡쳐.png', 'png',  2411724::bigint),
            ('[비밀글] 로그인 오류 재현 로그 첨부', '로그인오류_로그.zip',   '20261004120100000_로그인오류_로그.zip',   'zip',  36864::bigint)
       ) as f(ttl, orgnl, saved, extn, sz) on f.ttl = b.bbs_ttl
 where b.use_yn = 'Y';

-- 댓글: '기부금 통계 화면 정렬 개선 요청'에 3건(마지막은 삭제됨)
insert into admin.g_cmnty_sr_bbs_cmnt (bbs_id, cmnt_cn, use_yn, frst_crt_id, frst_crt_dt)
select b.bbs_id, c.cmnt_cn, c.use_yn, c.frst_crt_id, now() - c.ago
  from admin.g_cmnty_sr_bbs b
  join (values
            ('확인했습니다. 다음 배포에 반영 예정입니다.', 'Y', 1000, interval '4 day'),
            ('추가로 페이지당 건수도 기억되면 좋겠습니다.', 'Y', 1001, interval '3 day'),
            ('삭제된 댓글 - 보이면 안 된다',               'N', 1001, interval '3 day')
       ) as c(cmnt_cn, use_yn, frst_crt_id, ago) on true
 where b.bbs_ttl = '기부금 통계 화면 정렬 개선 요청' and b.use_yn = 'Y';

-- 댓글 첨부: 시스템 관리자(1000)가 쓴 첫 댓글에 2건(1건은 삭제됨)
insert into admin.g_cmnty_sr_bbs_cmnt_file
    (cmnt_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select c.cmnt_id, f.orgnl, f.saved, f.extn, f.sz, f.seq, 'cmnty/sr-bbs-cmnt', f.use_yn, 1000, now() - interval '4 day'
  from admin.g_cmnty_sr_bbs_cmnt c
  join (values
            ('반영계획.xlsx', '20261004120200000_반영계획.xlsx', 'xlsx', 512::bigint,    1, 'Y'),
            ('삭제된첨부.pdf', '20261004120300000_삭제된첨부.pdf', 'pdf', 1048577::bigint, 2, 'N')
       ) as f(orgnl, saved, extn, sz, seq, use_yn) on true
 where c.use_yn = 'Y' and c.frst_crt_id = 1000
   and c.cmnt_cn = '확인했습니다. 다음 배포에 반영 예정입니다.';
