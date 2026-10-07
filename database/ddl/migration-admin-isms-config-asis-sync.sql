-- 시스템관리 > ISMS관리(/isms-config) AS-IS 데이터 동기화 (2026-10-07).
--
-- 기존 시드(service-admin.sql)는 "배치 스캔 당시 0건이라 실제 키를 몰라 합리적인 기본값으로
-- 시드"한 발명 데이터였다(AD_SEND_START_HOUR/AD_SEND_END_HOUR/AD_SEND_RESTRICT - AS-IS에
-- 존재하지 않는 키). 사용자가 AS-IS CUBRID 개발DB에서 직접 export한 실데이터
-- (OP_CONFIG_ISMS_202610071026.sql, 12행)로 교체한다. SESSION_TIMEOUT_MANAGER는 기존에도
-- 있었지만 ISMS_TYPE이 '0'(공통)으로 잘못 들어가 있었다 - AS-IS는 '1'(관리자)이다.

DELETE FROM OP_CONFIG_ISMS
 WHERE KEY IN ('AD_SEND_START_HOUR', 'AD_SEND_END_HOUR', 'AD_SEND_RESTRICT', 'SESSION_TIMEOUT_MANAGER');

INSERT INTO OP_CONFIG_ISMS (KEY, VALUE, DESCRIPTION, USE_YN, ORDERING, ISMS_TYPE, UPDATE_DATE) VALUES
('LIFE_TIME_MANAGER_ACTION_LOG', '5',  '관리자 메뉴사용 이력 유효일수 (년)',   'Y', 1,  '1', '20260914121218'),
('LIFE_TIME_MANAGER_CHANGE_LOG', '10', '관리자 정보 변경이력 유효일수 (년)',   'Y', 2,  '1', '20260914121218'),
('LIFE_TIME_LOGIN_LOG',          '10', '관리자 로그인 이력 유효일수 (월)',     'Y', 3,  '1', '20260914121218'),
('SESSION_TIMEOUT_MANAGER',      '60', '관리자 세션 TIMEOUT(분)',             'Y', 4,  '1', '20260914121218'),
('UNUSED_MANAGER',               '35', '관리자 계정 미사용 기간(일수)',        'Y', 5,  '1', '20260914121218'),
('LIFE_TIME_USER_CHANGE_LOG',    '10', '회원 정보 변경이력 유효일수 (년)',     'Y', 6,  '2', '20260914121218'),
('LIFE_TIME_USER_LOGIN_LOG',     '10', '회원 로그인 이력 유효일수 (월)',       'Y', 7,  '2', '20260914121218'),
('SESSION_TIMEOUT_USER',         '30', '회원 세션 TIMEOUT(분)',               'Y', 8,  '2', '20260914121218'),
('FAIL_PASSWORD_COUNT',          '5',  '패스워드 오입력 횟수(횟수)',           'Y', 9,  '0', '20260914121218'),
('LIFE_TIME_PASSWORD',           '180','패스워드 유효일수',                   'Y', 11, '0', '20260914121218'),
('LOCK_TIME_PASSWORD',           '0',  '로그인 실패로 인한 Lock 시간(분)',     'Y', 13, '2', '20260914121218'),
('LIFE_TIME_QUERY_LOG',          '5',  'QUERY 이력 유효일수 (년)',            'Y', 14, '0', '20260914121218')
ON CONFLICT (KEY) DO UPDATE SET
    VALUE = EXCLUDED.VALUE, DESCRIPTION = EXCLUDED.DESCRIPTION, USE_YN = EXCLUDED.USE_YN,
    ORDERING = EXCLUDED.ORDERING, ISMS_TYPE = EXCLUDED.ISMS_TYPE, UPDATE_DATE = EXCLUDED.UPDATE_DATE;
