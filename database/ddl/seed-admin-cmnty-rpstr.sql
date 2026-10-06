-- 자료실(메뉴 11402) 기능확인용 시드.
--
-- 소속 표기 3가지 + 공지 + 첨부 유무 아이콘 + 소프트삭제 제외를 밟아볼 수 있게 짰다.
-- 자료실은 댓글도 비밀글도 없다(표에 컬럼이 없다).
-- 첨부 행만 넣고 디스크 파일은 만들지 않는다 - 다운로드 시 AS-IS처럼 오류 응답이 나오는 것까지 확인용.

insert into admin.g_cmnty_rpstr
    (rpstr_ttl, rpstr_cn, use_yn, notice_yn, inq_cnt, frst_crt_id, frst_crt_dt, last_mdfcn_id, last_mdfcn_dt)
values
    ('[공지] 자료실 이용 안내', '<p>담당자 업무자료를 공유하는 공간입니다.</p>',
     'Y', 'Y', 41, 1000, now() - interval '14 day', 1000, now() - interval '13 day'),
    ('2026년 고향사랑기부제 운영 지침', '<p>개정된 운영 지침을 첨부합니다.</p>',
     'Y', 'N', 27, 1004, now() - interval '6 day', null, null),
    ('답례품 등록 매뉴얼(지자체용)', '<p>옵션 등록 절차가 바뀌었습니다.</p>',
     'Y', 'N', 13, 1001, now() - interval '4 day', null, null),
    ('월별 기부현황 보고 양식', '<p>보고 양식을 통일했습니다.</p>',
     'Y', 'N', 9, 1002, now() - interval '2 day', null, null),
    ('삭제된 자료 - 목록·상세에 보이면 안 된다', '<p>use_yn = N</p>',
     'N', 'N', 0, 1001, now() - interval '1 day', 1001, now() - interval '1 day');

-- 첨부: '2026년 고향사랑기부제 운영 지침'(1건) / '답례품 등록 매뉴얼(지자체용)'(1건)
insert into admin.g_cmnty_file
    (rpstr_id, orgnl_atch_file_nm, atch_file_nm, atch_file_extn_nm, atch_file_sz, atch_file_seq,
     atch_file_path_nm, use_yn, frst_crt_id, frst_crt_dt)
select r.rpstr_id, f.orgnl, f.saved, f.extn, f.sz, 1, 'cmnty/databoard', 'Y', r.frst_crt_id, r.frst_crt_dt
  from admin.g_cmnty_rpstr r
  join (values
            ('2026년 고향사랑기부제 운영 지침', '운영지침_2026.pdf',   '20261004150000000_운영지침_2026.pdf',   'pdf',  5242880::bigint),
            ('답례품 등록 매뉴얼(지자체용)',    '답례품등록_매뉴얼.hwp', '20261004150100000_답례품등록_매뉴얼.hwp', 'hwp',  716800::bigint)
       ) as f(ttl, orgnl, saved, extn, sz) on f.ttl = r.rpstr_ttl
 where r.use_yn = 'Y';
