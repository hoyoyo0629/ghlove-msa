-- 고객센터 FAQ 질문유형 코드 복구 - **live 결함 수정**.
--
-- 증상: 고객센터 공개 FAQ(`/faqs`·`/api/faqs`)와 그 관리화면(`/community/locv-faq`)에서
--       질문유형이 **전부 빈 값**으로 보인다. `op_community_locgovfaq` 63행의 faq_type은
--       JOIN·DONATE·POINT·… 인데 `op_common_code`의 FAQ_TYPE에는 그 코드가 없기 때문이다.
--
-- 원인: `database/ddl/service-admin.sql`(4962행~)이 이 11건을 넣으려 하면서 대상 표를
--       **`ADMIN_COMMON_CODE`**로 적었다. 실제 표는 `admin.op_common_code`라서 INSERT가
--       반영되지 않았고, FAQ_TYPE에는 SalesOn 원제품의 쇼핑몰 FAQ 분류(1~6
--       회원정보/주문결제/배송/교환반품수리/포인트/기타)만 남아 있었다.
--
-- 조치:
--   1) AS-IS 질문유형 11종을 넣는다. 라벨은 AS-IS Java enum `saleson.common.enumeration.FaqType`
--      그대로이고, id는 이 표(`op_community_locgovfaq.faq_type`)에 실제로 저장돼 있는 짧은 코드다.
--   2) 쇼핑몰 잔재 1~6은 **use_yn='N'으로 숨긴다**(삭제하지 않는다 - 되돌릴 수 있게).
--      `CommonCodeService.labelsOf()`가 use_yn='Y'만 추리므로 화면에서는 11종만 보인다.
--      FAQ_TYPE을 읽는 곳은 `/faqs`·`/api/faqs`·`/community/locv-faq` 세 곳뿐이고 모두
--      이 11종이 맞는 화면이다(실측). 담당자용 FAQ(메뉴 11405)는 별도 코드유형
--      `CMNTY_FAQ_TYPE`(enum 이름 F_* 를 id로 쓴다)을 보므로 영향이 없다.

insert into admin.op_common_code (code_type, language, id, label, detail, ordering, use_yn)
values
    ('FAQ_TYPE', 'ko', 'JOIN',       '회원가입/로그인', '회원가입/로그인',  1, 'Y'),
    ('FAQ_TYPE', 'ko', 'DONATE',     '기부하기',        '기부하기',         2, 'Y'),
    ('FAQ_TYPE', 'ko', 'POINT',      '기부포인트',      '기부포인트',       3, 'Y'),
    ('FAQ_TYPE', 'ko', 'OFFLINE',    '오프라인기부',    '오프라인기부',     4, 'Y'),
    ('FAQ_TYPE', 'ko', 'DESIGNATED', '특정사업기부',    '특정사업기부',     5, 'Y'),
    ('FAQ_TYPE', 'ko', 'TAX',        '세액공제',        '세액공제',         6, 'Y'),
    ('FAQ_TYPE', 'ko', 'GIFT',       '답례품',          '답례품',           7, 'Y'),
    ('FAQ_TYPE', 'ko', 'ORDER',      '주문/배송',       '주문/배송',        8, 'Y'),
    ('FAQ_TYPE', 'ko', 'PRIVATE',    '민간플랫폼',      '민간플랫폼',       9, 'Y'),
    ('FAQ_TYPE', 'ko', 'SYSTEM',     '시스템',          '시스템',          10, 'Y'),
    ('FAQ_TYPE', 'ko', 'ETC',        '기타',            '기타',            11, 'Y')
on conflict (code_type, language, id) do nothing;

-- 쇼핑몰 잔재(1~6) 숨김 - 삭제하지 않는다.
update admin.op_common_code
   set use_yn = 'N'
 where code_type = 'FAQ_TYPE' and language = 'ko' and id in ('1','2','3','4','5','6');

-- 데이터 1행 보정: id=1000 '로그인이 안 돼요'가 faq_type='1'(쇼핑몰 잔재 코드 '회원정보')로
-- 들어가 있어 질문유형이 빈 값으로 보였다. 제목대로 'JOIN'(회원가입/로그인)으로 맞춘다.
-- (예전 TO-BE 화면이 쇼핑몰 FAQ_TYPE을 쓰던 시절에 등록된 흔적 - 다른 62행은 모두 올바른 코드다.)
update admin.op_community_locgovfaq
   set faq_type = 'JOIN'
 where id = 1000 and faq_type = '1';
