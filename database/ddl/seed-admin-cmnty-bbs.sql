-- 소통방(메뉴 11401) 기능확인용 시드.
--
-- AS-IS 화면의 분기를 모두 밟아볼 수 있게 짰다:
--   - 소속 표기 3가지: 시스템 관리자(ROLE_ADMIN_1) / 행정안전부(ROLE_ADMIN_4) / 지자체(5·6)
--   - 공지글(상단 고정) 1건, 비밀글 1건, 댓글 달린 글 1건, 삭제된 글(use_yn='N') 1건
--   - 지자체 작성자는 서로 다른 시도(11230 서울 성북구 / 26350 부산 금정구)에 둬서
--     소속 검색(시도→시군구)이 실제로 걸러지는지 확인할 수 있다.
--
-- 작성자는 admin.op_manager에 이미 있는 담당자들을 쓴다(1000 관리자/1004 행안부주담당자/
-- 1001 운영자(지자체 11230)/1002 locgovtest2(지자체 26350)).
-- 비밀글은 1001이 썼으므로 1001 본인과 시스템 관리자(1000·1003)만 열람 가능해야 한다.

insert into admin.g_cmnty_bbs
    (bbs_ttl, bbs_cn, use_yn, notice_yn, inq_cnt, frst_crt_id, frst_crt_dt, last_mdfcn_id, last_mdfcn_dt, is_secret)
values
    ('[공지] 소통방 이용 안내', '<p>타인의 개인정보를 작성하지 않도록 유의해 주세요.</p>',
     'Y', 'Y', 12, 1000, now() - interval '9 day', 1000, now() - interval '8 day', 'N'),
    ('기부금 영수증 발급 관련 문의', '<p>연말정산 기간 영수증 발급 절차를 공유합니다.</p>',
     'Y', 'N', 5, 1004, now() - interval '6 day', null, null, 'N'),
    ('답례품 재고 등록 시 주의사항', '<p>옵션별 재고를 따로 넣어야 합니다.</p>',
     'Y', 'N', 3, 1001, now() - interval '4 day', null, null, 'N'),
    ('[비밀글] 담당자 연락처 변경 요청', '<p>작성자와 시스템 관리자만 볼 수 있어야 합니다.</p>',
     'Y', 'N', 1, 1001, now() - interval '3 day', null, null, 'Y'),
    ('지역 행사 일정 공유', '<p>다음 달 기부 행사 일정입니다.</p>',
     'Y', 'N', 7, 1002, now() - interval '2 day', null, null, 'N'),
    ('삭제된 글 - 목록에 보이면 안 된다', '<p>use_yn = N</p>',
     'N', 'N', 0, 1002, now() - interval '1 day', 1002, now() - interval '1 day', 'N');

-- 댓글: '답례품 재고 등록 시 주의사항'(1001 글)에 2건 + 삭제된 댓글 1건.
-- 삭제된 댓글은 댓글 수([N])와 목록에 모두 빠져야 한다.
insert into admin.g_cmnty_cmnt (bbs_id, cmnt_cn, use_yn, frst_crt_id, frst_crt_dt, last_mdfcn_id, last_mdfcn_dt)
select b.bbs_id, c.cmnt_cn, c.use_yn, c.frst_crt_id, now() - c.ago, null, null
  from admin.g_cmnty_bbs b
  join (values
            ('옵션 단위로 넣어야 한다는 점 확인했습니다.', 'Y', 1000, interval '3 day'),
            ('감사합니다. 지자체에도 공유하겠습니다.',     'Y', 1002, interval '2 day'),
            ('삭제된 댓글 - 보이면 안 된다',               'N', 1002, interval '2 day')
       ) as c(cmnt_cn, use_yn, frst_crt_id, ago) on true
 where b.bbs_ttl = '답례품 재고 등록 시 주의사항';
