-- 담당자용 FAQ(11405) 질문유형 탭이 '전체' 하나만 뜨던 것 수정 (2026-10-07).
--
-- 원인: admin 서비스의 공통코드 조회 경로가 두 갈래다.
--   - JPA 엔티티 CommonCode  -> admin.ADMIN_COMMON_CODE   (CommonCodeService.labelsOf 가 쓴다)
--   - 네이티브 SQL 10여 곳    -> admin.op_common_code
-- migration-admin-cmnty-faq-type-codes.sql 이 11건을 op_common_code 에 넣었는데,
-- 화면은 labelsOf("CMNTY_FAQ_TYPE") = JPA 경로라 admin_common_code 를 본다. 그래서 0건이었다.
--
-- 사용자 결정(2026-10-07): 이 화면은 엔티티가 보는 ADMIN_COMMON_CODE 를 바라보게 한다.
-- 코드는 고치지 않고 데이터만 맞춘다(읽는 지점이 CmntyFaqBbsAdminService.faqTypes() 한 곳뿐이라
-- 목록·등록·수정·상세가 같이 해결된다).
--
-- 값은 AS-IS Java enum saleson.common.enumeration.FaqType 11종을 코드·라벨·순서 그대로 옮긴 것이다.
-- ※ 컬럼명이 op_common_code 와 다르다: language -> code_language.
-- ※ op_common_code 쪽 11건은 남겨 두었다(지우는 것은 파괴적이라 사용자 판단).
--    두 표에 같은 코드가 중복으로 존재하는 상태이며, 표 일원화는 별도 과제다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i <psql> ... 또는
--       psql -h 10.0.110.37 -p 15432 -U admindb -d ghlove_core -f (이 파일)

insert into admin.admin_common_code (code_type, code_language, id, label, detail, ordering, use_yn)
values
    ('CMNTY_FAQ_TYPE', 'ko', 'F_LOGIN',           '회원가입/로그인', '회원가입/로그인',  1, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_CNTR_SYSTEM',     '기부하기',        '기부하기',         2, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_CNTR_POINT',      '기부포인트',      '기부포인트',       3, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_OFF_CNTR',        '오프라인기부',    '오프라인기부',     4, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_CNTR_DESIGNATED', '특정사업기부',    '특정사업기부',     5, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_API_PLATFORM',    '세액공제',        '세액공제',         6, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_PRESENT_PURC',    '답례품',          '답례품',           7, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_ORDER',           '주문/배송',       '주문/배송',        8, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_OPEN',            '민간플랫폼',      '민간플랫폼',       9, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_SYSTEM',          '시스템',          '시스템',          10, 'Y'),
    ('CMNTY_FAQ_TYPE', 'ko', 'F_ETC',             '기타',            '기타',            11, 'Y')
on conflict (code_type, code_language, id) do nothing;
