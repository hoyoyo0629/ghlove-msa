-- 회원가입 축하 포인트 설정 (AS-IS OP_CONFIG.POINT_JOIN, 관리자 config/point.jsp "회원가입시 포인트").
-- point 서비스가 member.lifecycle의 MEMBER_JOINED를 소비해 이 값만큼 적립한다(PointService.creditForSignup).
-- AS-IS와 동일하게 기본값 0(비활성) - 값이 0이면 적립 행을 만들지 않는다. 지자체/운영 정책으로
-- 회원가입 포인트를 켜려면 이 code_value를 양수로 바꾼다.
INSERT INTO point.OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN, CODE_VALUE)
VALUES ('SYSTEM_CONFIG', 'ko', 'POINT_JOIN', '회원가입시 포인트(P)', 4, 'Y', '0')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;
