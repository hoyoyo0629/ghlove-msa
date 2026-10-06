-- 고객센터 FAQ (메뉴 5104) 이식 - AS-IS /opmanager/faq
--
-- 세 가지를 바로잡는다.
--
-- (1) 메뉴 5104 'FAQ'의 menu_url이 /community/faq-bbs 였다 - 그건 커뮤니티 '담당자FAQ'(11405)
--     화면이다. 고객센터 FAQ를 누르면 다른 메뉴의 화면이 열렸다. → /faq-admin
--
-- (2) op_faq 채번 수단이 없었다. AS-IS는 시퀀스 OP_FAQ_SEQ(initialValue 630000)를 쓴다.
--
-- (3) 공개 FAQ가 엉뚱한 표를 읽고 있었다.
--     AS-IS 정본은 op_faq다 - 운영자 화면(5104)·공개 페이지(/faq/list.html)·공개 API(/api/faq)가
--     모두 op_faq를 보고, 통합검색 뷰 view_search_faq도 op_faq를 보며 /faq/list.html로 링크한다.
--     그런데 TO-BE 초기 시드는 FAQ 63건을 op_community_locgovfaq(AS-IS에서 **중지된** 메뉴 11403
--     '지자체FAQ'의 표)에 넣고, 질문유형 코드도 AS-IS enum(F_LOGIN…)이 아닌 자체 코드
--     (JOIN·DONATE…)로 바꿔 두었다. 라벨 11건이 AS-IS enum과 글자까지 같아 코드만 1:1로 되돌려
--     op_faq로 옮긴다.
--
--     원본 행은 지우지 않는다(표 정리는 사용자 판단). 중지된 11403 화면이 계속 그 표를 본다.

-- (2) 채번 시퀀스
create sequence if not exists admin.op_faq_seq as bigint start with 630000 increment by 1;

-- (3) 공개 FAQ 63건을 정본 표로 이관 (질문유형 코드 1:1 번역)
insert into admin.op_faq (id, faq_type, title, content, hit, use_yn, created, created_by, updated, updated_by)
select l.id,
       case l.faq_type
            when 'JOIN'       then 'F_LOGIN'            -- 회원가입/로그인
            when 'DONATE'     then 'F_CNTR_SYSTEM'      -- 기부하기
            when 'POINT'      then 'F_CNTR_POINT'       -- 기부포인트
            when 'OFFLINE'    then 'F_OFF_CNTR'         -- 오프라인기부
            when 'DESIGNATED' then 'F_CNTR_DESIGNATED'  -- 특정사업기부
            when 'TAX'        then 'F_API_PLATFORM'     -- 세액공제
            when 'GIFT'       then 'F_PRESENT_PURC'     -- 답례품
            when 'ORDER'      then 'F_ORDER'            -- 주문/배송
            when 'PRIVATE'    then 'F_OPEN'             -- 민간플랫폼
            when 'SYSTEM'     then 'F_SYSTEM'           -- 시스템
            when 'ETC'        then 'F_ETC'              -- 기타
            else l.faq_type
       end,
       l.subject,
       l.content,
       l.hits,
       coalesce(l.use_yn, 'Y'),
       l.created_date,
       l.admin_id,
       coalesce(l.updated_date, l.created_date),
       l.updated_by
  from admin.op_community_locgovfaq l
 where not exists (select 1 from admin.op_faq f where f.id = l.id);

-- 옮긴 id가 시퀀스 시작값(630000)보다 작아 충돌할 일은 없지만, 안전하게 맞춰 둔다.
select setval('admin.op_faq_seq',
              greatest(coalesce((select max(id) + 1 from admin.op_faq), 630000), 630000), false);

-- (1) 메뉴 URL 교정
update admin.op_menu set menu_url = '/faq-admin' where menu_id = 5104;
