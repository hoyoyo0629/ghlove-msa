-- 담당자용 FAQ(메뉴 11405)의 '질문유형' 코드 적재.
--
-- AS-IS는 이 목록을 **Java enum**(`saleson.common.enumeration.FaqType`)으로 들고
-- `enumMapper.get("FaqType")`으로 화면에 내려준다. 저장되는 값은 enum 이름(getCode() = name())이고
-- 화면에 보이는 글자는 title이다. 아래 11건은 그 enum을 **코드·라벨 그대로** 옮긴 것이다.
--
-- TO-BE는 이런 코드성 데이터를 op_common_code에서 조회하는 것이 관례라(라벨을 템플릿이나
-- 자바 enum에 박으면 AS-IS가 문구를 바꿨을 때 갈라진다) 새 코드유형 CMNTY_FAQ_TYPE으로 넣는다.
--
-- ★이미 있는 FAQ_TYPE(1~6 회원정보/주문결제/배송/교환반품수리/포인트/기타)은 **다른 화면 것**이다
--   (SalesOn 원제품의 쇼핑몰 FAQ). 예전 TO-BE 골격이 담당자용 FAQ 화면에서 그 코드를 쓰고 있었는데
--   AS-IS와 코드셋이 전혀 달라 잘못된 연결이었다 - 그래서 코드유형을 분리했다.

insert into admin.op_common_code (code_type, language, id, label, detail, ordering, use_yn)
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
on conflict (code_type, language, id) do nothing;
