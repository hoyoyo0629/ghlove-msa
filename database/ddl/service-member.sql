-- ===================================================================
-- TO-BE DB 재구성(2026-09): 물리 2개(gift + 나머지), 논리 6개 스키마 구조로 전환.
-- 이 파일은 이제 자신의 단독 DB가 아니라 'ghlove_core' DB 안의 'member' 스키마에 적용한다.
-- 실행예: psql -U postgres -d ghlove_core -f service-member.sql
-- ===================================================================
CREATE SCHEMA IF NOT EXISTS member;
SET search_path TO member;

-- Service: member
-- ===================================================================
-- AS-IS 운영 DB 원본 스키마 기준 자동 생성 (31개 테이블)
-- 출처: 운영사이트 DB dump(CUBRID) -> PostgreSQL 변환. 인덱스는 이번 생성에서 제외(추후 실사용 쿼리 패턴 기반으로 추가).
-- 서비스간 FK는 제거됨(no cross-service FK 원칙).
-- ===================================================================

-- 개인정보열람이력
CREATE TABLE IF NOT EXISTS G_INDVDLINFO_READNG_HIST (
    -- 열람일련번호
    READNG_SN                    BIGINT NOT NULL,
    -- 회원고유번호
    USER_ID                      BIGINT NOT NULL,
    -- 열람일시
    READNG_DT                    TIMESTAMP NOT NULL,
    -- 대상회원고유번호
    TRGET_USER_ID                BIGINT NOT NULL,
    PRIMARY KEY (READNG_SN)
);

-- 회원?퇴
CREATE TABLE IF NOT EXISTS G_MBER_SECSN (
    -- 탈퇴 년도
    SECSN_YEAR                   VARCHAR(4) NOT NULL,
    -- 회원 고유번호
    USER_ID                      BIGINT NOT NULL,
    -- 회원 CI
    MBER_CI                      VARCHAR(200) NOT NULL,
    -- 기부 금액
    CNTR_AMT                     INTEGER,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL,
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (SECSN_YEAR, USER_ID, MBER_CI)
);

-- 네이버 인증 로그인 정보 관리
CREATE TABLE IF NOT EXISTS G_NAVER_AUTH_LOGIN_INFO_MNG (
    -- 인증 임시 아이디(tx_id)
    AUTH_TEMP_ID                 VARCHAR(50) NOT NULL,
    -- 토큰
    ACCESS_TOKEN                 VARCHAR(10000) NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ACCESS_TOKEN)
);

-- 회원 그룹
CREATE TABLE IF NOT EXISTS OP_GROUP (
    -- 그룹코드
    GROUP_CODE                   VARCHAR(10) NOT NULL,
    -- 그룹 이름
    GROUP_NAME                   VARCHAR(30),
    -- 그룹 설명
    GROUP_EXPLANATION            VARCHAR(50),
    -- 생성날짜
    CREATED_DATE                 VARCHAR(30),
    -- 생성자 
    CREATED_USER_ID              VARCHAR(30),
    -- 수정날짜
    UPDATED_DATE                 VARCHAR(30),
    -- 수정자
    UPDATED_USER_ID              VARCHAR(30),
    PRIMARY KEY (GROUP_CODE)
);

-- 회원가입 환경 설정(포맷)
CREATE TABLE IF NOT EXISTS OP_JOIN_CONFIG (
    -- 상점 ID
    SHOP_CONFIG_ID               BIGINT NOT NULL,
    -- 닉네임 (0:필수입력, 1:선택입력, 2:사용안함)
    NICKNAME                     VARCHAR(1),
    -- 회사명 (0:필수입력, 1:선택입력, 2:사용안함)
    COMPANY_NAME                 VARCHAR(1),
    -- 회사명_가타카나 (0:필수입력, 1:선택입력, 2:사용안함)
    COMPANY_NAME_KATAKANA        VARCHAR(1),
    -- 직위 (0:필수입력, 1:선택입력, 2:사용안함)
    POSTION                      VARCHAR(1),
    -- 이름 (0:필수입력, 1:선택입력, 2:사용안함)
    USER_NAME                    VARCHAR(1),
    -- 이름_가타카나 (0:필수입력, 1:선택입력, 2:사용안함)
    USER_NAME_KATAKANA           VARCHAR(1),
    -- 영업 (0:필수입력, 1:선택입력, 2:사용안함)
    BUSINESS                     VARCHAR(1),
    -- 이메일 (0:필수입력, 1:선택입력, 2:사용안함)
    EMAIL                        VARCHAR(1),
    -- 주소 (0:필수입력, 1:선택입력, 2:사용안함)
    ADDRESS                      VARCHAR(1),
    -- 전화 (0:필수입력, 1:선택입력, 2:사용안함)
    TEL                          VARCHAR(1),
    -- 팩스 (0:필수입력, 1:선택입력, 2:사용안함)
    FAX                          VARCHAR(1),
    -- 휴대폰 (0:필수입력, 1:선택입력, 2:사용안함)
    PHONE                        VARCHAR(1),
    -- 이메일 수신 여부 (0:필수입력, 1:선택입력, 2:사용안함)
    RECEIVE_MAIL                 VARCHAR(1),
    -- 카달로그 앱 (0:필수입력, 1:선택입력, 2:사용안함)
    CATALOG_APP                  VARCHAR(1),
    -- 성별 (0:필수입력, 1:선택입력, 2:사용안함)
    SEX                          VARCHAR(1),
    -- 연령 (0:필수입력, 1:선택입력, 2:사용안함)
    AGE                          VARCHAR(1),
    -- 생년월일
    BIRTHDAY                     VARCHAR(14),
    -- 사업자번호
    BUSINESS_NUMBER              VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL DEFAULT '00000000000000',
    -- 수정일
    UPDATED_DATE                 VARCHAR(14) NOT NULL DEFAULT '00000000000000',
    -- SMS수신여부
    RECEIVE_SMS                  VARCHAR(1)
);

-- 권한계층
CREATE TABLE IF NOT EXISTS OP_ROLE_HIERARCHY (
    -- 권한ID
    AUTHORITY                    VARCHAR(20) NOT NULL,
    -- 권한부모ID
    PARENT_AUTHORITY             VARCHAR(20) NOT NULL,
    PRIMARY KEY (AUTHORITY)
);

-- 세션 정보
CREATE TABLE IF NOT EXISTS OP_SESSION (
    -- ID
    PRIMARY_ID                   CHAR(36) NOT NULL,
    -- 세션ID
    SESSION_ID                   CHAR(36) NOT NULL,
    -- 생성 시간
    CREATION_TIME                BIGINT NOT NULL,
    -- 마지막 접속 시간
    LAST_ACCESS_TIME             BIGINT NOT NULL,
    -- 세션 OPEN 최대시간
    MAX_INACTIVE_INTERVAL        INTEGER NOT NULL,
    -- 만료시간
    EXPIRY_TIME                  BIGINT NOT NULL,
    -- 인증정보
    PRINCIPAL_NAME               VARCHAR(100),
    PRIMARY KEY (PRIMARY_ID)
);

-- 세션 정보 상세
CREATE TABLE IF NOT EXISTS OP_SESSION_ATTRIBUTES (
    -- ID
    SESSION_PRIMARY_ID           CHAR(36) NOT NULL,
    -- 속성명
    ATTRIBUTE_NAME               VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES              BYTEA,
    PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
);

-- SNS USER MASTER TABLE
CREATE TABLE IF NOT EXISTS OP_SNS_USER (
    -- 내부시퀀스
    SNS_USER_ID                  BIGINT NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- OP_USER 및 OP_USER_DETAIL 정보 입력 완료 시간
    CERTIFIED_DATE               VARCHAR(14),
    PRIMARY KEY (SNS_USER_ID)
);

-- SNS USER DETAIL TABLE
CREATE TABLE IF NOT EXISTS OP_SNS_USER_DETAIL (
    -- 내부시퀀스
    SNS_DETAIL_ID                BIGINT NOT NULL,
    -- SNS_USER_ID (FK)
    SNS_USER_ID                  BIGINT NOT NULL,
    -- SNS 제공 사용자 고유 ID
    SNS_ID                       VARCHAR(20) NOT NULL,
    -- SNS종류 (naver, facebook, kakao)
    SNS_TYPE                     VARCHAR(20) NOT NULL,
    -- SNS 제공 사용자명
    SNS_NAME                     VARCHAR(350) NOT NULL,
    -- SNS 제공 사용자 EMAIL
    EMAIL                        VARCHAR(350),
    -- 사용자별 SNS 등록 순번
    CREATED_ORDER                INTEGER NOT NULL DEFAULT 0,
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (SNS_DETAIL_ID)
);

-- 회원 정보
CREATE TABLE IF NOT EXISTS OP_USER (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 사용자ID
    LOGIN_ID                     VARCHAR(300) NOT NULL,
    -- 비밀번호
    PASSWORD                     VARCHAR(300),
    -- 사용자명
    USER_NAME                    VARCHAR(300),
    -- 이메일
    EMAIL                        VARCHAR(300),
    -- 상태코드 - AS-IS는 BIGINT(1:가입대기,2:차단,3:탈퇴,4:휴면계정,9:정상)이지만, 이 프로젝트는
    -- 다른 서비스와 동일하게 의미가 드러나는 문자열 코드(ACTIVE/LOCKED/WITHDRAWN/DORMANT,
    -- MemberService)를 쓴다 - 배치 스캔 당시 타입을 그대로 옮겨서 회원가입 자체가 500으로
    -- 실패하던 버그를 여기서 수정한다(VARCHAR로 재정의).
    STATUS_CODE                  VARCHAR(20),
    -- 로그인 회수
    LOGIN_COUNT                  BIGINT,
    -- 휴면 안내 메일 발송일
    SLEEP_MAIL_SEND_DATE         VARCHAR(14),
    -- 최종로그인날짜
    LOGIN_DATE                   VARCHAR(14),
    -- 차단일
    DENY_DATE                    VARCHAR(14),
    -- 탈퇴일
    LEAVE_DATE                   VARCHAR(14),
    -- 로그인 실패 횟수
    LOGIN_FAIL_COUNT             INTEGER NOT NULL DEFAULT 0,
    -- 최종 로그인시도 일시
    LOGIN_TRY_DATE               VARCHAR(14),
    -- 패스워드 타입 (N:정상 T:임시 P:카카오)
    PASSWORD_TYPE                VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 패스워드 유효일자
    PASSWORD_EXPIRED_DATE        VARCHAR(8) DEFAULT '20240101',
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10),
    -- 회원 CI
    MBER_CI                      VARCHAR(200),
    -- 회원 DI
    MBER_DI                      VARCHAR(200),
    -- 회원 DN
    MBER_DN                      VARCHAR(200),
    MBER_FIN_DN                  VARCHAR(200),
    -- 가입 구분 코드
    SBSCRB_SE_CODE               VARCHAR(10),
    -- 로그인 경로 코드
    LOGIN_PATH_CODE              VARCHAR(10),
    -- 디지털원패스userkey
    USER_KEY                     VARCHAR(30),
    -- 카카오 사용자 번호
    KAKAO_USER_KEY               VARCHAR(30),
    FOREIGN_STATUS_CODE          VARCHAR(2) NOT NULL DEFAULT '0',
    -- 네이버 사용자 번호
    NAVER_USER_KEY               VARCHAR(100),
    CREATE_DATE                  VARCHAR(255),
    PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS OP_USER_AGREE (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    AGREE                        VARCHAR(1),
    LOGIN_ID                     VARCHAR(255),
    POLICY_ID                    INTEGER,
    POLICY_TYPE                  VARCHAR(1),
    TITLE                        VARCHAR(255),
    USER_ID                      BIGINT,
    PRIMARY KEY (ID)
);
CREATE SEQUENCE IF NOT EXISTS op_user_agree_id_seq START WITH 1000;
ALTER TABLE OP_USER_AGREE ALTER COLUMN ID SET DEFAULT nextval('op_user_agree_id_seq');
ALTER SEQUENCE op_user_agree_id_seq OWNED BY OP_USER_AGREE.ID;

-- 사용자 인증
CREATE TABLE IF NOT EXISTS OP_USER_AUTH (
    -- 앱 키
    APP_KEY                      VARCHAR(50) NOT NULL,
    -- 서비스 타입
    SERVICE_TYPE                 VARCHAR(50) NOT NULL,
    -- 서비스 모드
    SERVICE_MODE                 VARCHAR(10) NOT NULL,
    -- 서비스 타겟
    SERVICE_TARGET               VARCHAR(50) NOT NULL,
    -- 사용자 IP
    USER_IP                      VARCHAR(50) NOT NULL,
    -- 인증키
    AUTH_KEY                     VARCHAR(80),
    -- 인증자명
    AUTH_NAME                    VARCHAR(20),
    -- 인증자 성별
    AUTH_SEX                     VARCHAR(1),
    -- 인증자 생일
    AUTH_BIRTH_DAY               VARCHAR(8),
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '0',
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (APP_KEY, SERVICE_TYPE)
);

-- 사용자 생년월일
CREATE TABLE IF NOT EXISTS OP_USER_BIRTHDAY (
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    BIRTHDAY                     VARCHAR(300),
    CREATED_DATE                 VARCHAR(14),
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS OP_USER_BIRTHDAY_TEMP (
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    BIRTHDAY                     VARCHAR(300),
    CREATED_DATE                 VARCHAR(14),
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_ID)
);

-- 사용자의 개인정보를 변경한 이력 관리 테이블
CREATE TABLE IF NOT EXISTS OP_USER_CHANGE_LOG (
    -- 테이블 고유 ID
    CHANGE_LOG_ID                INTEGER NOT NULL,
    -- 유저 ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 파라미터
    PARAMETER                    VARCHAR(2000),
    -- IP
    REMOTE_ADDR                  VARCHAR(100),
    -- 수정한 매니저 ID
    MANAGER_ID                   BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (CHANGE_LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_user_change_log_change_log_id_seq START WITH 1000;
ALTER TABLE OP_USER_CHANGE_LOG ALTER COLUMN CHANGE_LOG_ID SET DEFAULT nextval('op_user_change_log_change_log_id_seq');
ALTER SEQUENCE op_user_change_log_change_log_id_seq OWNED BY OP_USER_CHANGE_LOG.CHANGE_LOG_ID;

-- 사용자 정보
CREATE TABLE IF NOT EXISTS OP_USER_CI (
    USER_ID                      BIGINT NOT NULL,
    MBER_CI                      VARCHAR(200),
    PRIMARY KEY (USER_ID)
);

-- 회원별 배송지 설정 정보
CREATE TABLE IF NOT EXISTS OP_USER_DELIVERY (
    -- 회원별 배송지 설정 정보ID
    USER_DELIVERY_ID             INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 기본주소 체크
    DEFAULT_FLAG                 VARCHAR(1) DEFAULT 'N',
    -- 타이틀
    TITLE                        VARCHAR(50),
    -- 구매자 이름
    USER_NAME                    VARCHAR(350),
    -- 구매자 연락처 1
    PHONE                        VARCHAR(210),
    -- 구매자 연락처 2
    MOBILE                       VARCHAR(210),
    -- 2015.8.1부터 시행하는 우편번호
    NEW_ZIPCODE                  VARCHAR(5),
    -- 구매자 우편번호
    ZIPCODE                      VARCHAR(10),
    -- 시도
    SIDO                         VARCHAR(20),
    -- 시군구
    SIGUNGU                      VARCHAR(20),
    -- 읍면동
    EUPMYEONDONG                 VARCHAR(20),
    -- 구매자 주소
    ADDRESS                      VARCHAR(1000),
    -- 구매자 상세 주소
    ADDRESS_DETAIL               VARCHAR(1785),
    -- 생성일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (USER_DELIVERY_ID)
);

-- 회원 상세 정보
CREATE TABLE IF NOT EXISTS OP_USER_DETAIL (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 회원 GROUP CODE
    GROUP_CODE                   VARCHAR(10),
    -- 회원 레벨 등급
    LEVEL_ID                     INTEGER NOT NULL,
    -- 회원 레벨 만료일
    USER_LEVEL_EXPIRATION_DATE   VARCHAR(8),
    -- 2015.8.1부터 시행하는 새 우편번호
    NEW_POST                     VARCHAR(50),
    -- 우편번호
    POST                         VARCHAR(300),
    -- 기본주소
    ADDRESS                      VARCHAR(300),
    -- 상세주소
    ADDRESS_DETAIL               VARCHAR(300),
    -- 전화번호
    TEL_NUMBER                   VARCHAR(140),
    -- 핸드폰번호
    PHONE_NUMBER                 VARCHAR(300),
    -- 팩스 번호
    FAX_NUMBER                   VARCHAR(20),
    -- 수신여부 (0:수신, 1:비수신)
    RECEIVE_EMAIL                VARCHAR(1),
    -- 수신여부 (0:수신, 1:비수신)
    RECEIVE_SMS                  VARCHAR(1),
    -- 수신여부 (0:수신, 1:비수신)
    RECEIVE_PUSH                 VARCHAR(1),
    -- 성별
    GENDER                       VARCHAR(300),
    -- 연령대
    AGE                          VARCHAR(3) DEFAULT '0',
    -- 적립금
    POINT                        INTEGER DEFAULT 0,
    -- 구매 횟 수
    BUY_COUNT                    INTEGER DEFAULT 0,
    -- 구매 총 가격
    BUY_PRICE                    INTEGER DEFAULT 0,
    -- 가장 최근 구매 날짜
    LAST_BUY_DATE                VARCHAR(14),
    -- 탈퇴 사유
    LEAVE_REASON                 VARCHAR(255),
    -- 가입 경로(0:PC, 1:MOBILE)
    SITE_FLAG                    VARCHAR(100),
    -- 사용 여부
    USE_FLAG                     VARCHAR(1) DEFAULT 'Y',
    -- 생년월일 분류(1:양력, 2:음력)
    BIRTHDAY_TYPE                VARCHAR(1),
    -- 생년월일
    BIRTHDAY                     VARCHAR(300),
    -- 탈퇴코드
    LEAVE_CODE                   VARCHAR(10),
    -- 강제탈퇴ID
    LEAVE_USER_ID                BIGINT,
    -- 광고 수신여부 (0:수신, 1비수신)
    RECEIVE_PBANC                VARCHAR(1),
    -- 수신여부 (0:수신, 1:비수신)
    RECEIVE_KAKAO                VARCHAR(1),
    PRIMARY KEY (USER_ID)
);

-- 사용자 이벤트
CREATE TABLE IF NOT EXISTS OP_USER_EVENT (
    -- 이벤트 순번
    EVENT_SN                     INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL,
    -- 사용자명
    USER_NAME                    VARCHAR(300),
    -- 핸드폰번호
    PHONE_NUMBER                 VARCHAR(300),
    -- 이벤트명
    EVENT_NAME                   VARCHAR(300),
    -- 생성일시
    CREATE_DATE                  TIMESTAMP DEFAULT now(),
    PRIMARY KEY (EVENT_SN)
);
CREATE SEQUENCE IF NOT EXISTS op_user_event_event_sn_seq START WITH 1000;
ALTER TABLE OP_USER_EVENT ALTER COLUMN EVENT_SN SET DEFAULT nextval('op_user_event_event_sn_seq');
ALTER SEQUENCE op_user_event_event_sn_seq OWNED BY OP_USER_EVENT.EVENT_SN;

-- 유저가 속한 그룹
CREATE TABLE IF NOT EXISTS OP_USER_GROUP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 그룹 아이디
    GROUP_ID                     INTEGER NOT NULL,
    PRIMARY KEY (USER_ID, GROUP_ID)
);

-- 그룹 생성 로그
CREATE TABLE IF NOT EXISTS OP_USER_GROUP_LOG (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 그룹코드
    GROUP_CODE                   VARCHAR(10),
    -- 그룹명
    GROUP_NAME                   VARCHAR(10),
    -- 관리자명
    ADMIN_USER_NAME              VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14)
);

-- 사용자 회원 등급
CREATE TABLE IF NOT EXISTS OP_USER_LEVEL (
    -- 사용자 회원 등급 ID
    LEVEL_ID                     INTEGER NOT NULL DEFAULT 0,
    -- 사용자 그룹
    GROUP_CODE                   VARCHAR(10) NOT NULL DEFAULT 'default',
    -- 등급
    DEPTH                        INTEGER NOT NULL,
    -- 등급명
    LEVEL_NAME                   VARCHAR(20) NOT NULL,
    -- 파일 이름
    FILE_NAME                    VARCHAR(255),
    -- 기준금액 시작
    PRICE_START                  INTEGER NOT NULL DEFAULT 0,
    -- 기준금액 끝
    PRICE_END                    INTEGER NOT NULL DEFAULT 0,
    -- 상시 할인율
    DISCOUNT_RATE                DOUBLE PRECISION NOT NULL DEFAULT 0.000000,
    -- 적립률
    POINT_RATE                   DOUBLE PRECISION NOT NULL DEFAULT 0.000000,
    -- 배송비 할인 쿠폰
    SHIPPING_COUPON_COUNT        INTEGER NOT NULL DEFAULT 0,
    -- 유지기간
    RETENTION_PERIOD             INTEGER NOT NULL DEFAULT 0,
    -- 기준기간
    REFERENCE_PERIOD             INTEGER NOT NULL DEFAULT 0,
    -- 매출 제외 기준일
    EXCEPT_REFERENCE_PERIOD      INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (LEVEL_ID)
);

-- 사용자 등급 변경 로그
-- LOG_ID는 원본 덤프에 없던 서로게이트 PK(D11 회원등급관리 라운드에서 추가) - 원본 테이블은
-- PK 지정이 없는 순수 로그성 테이블이라 JPA 매핑을 위해 추가했다(as-is-schema-import-550-tables
-- memory의 "의도적 override" 관행과 동일).
CREATE TABLE IF NOT EXISTS OP_USER_LEVEL_LOG (
    -- 로그 ID(신규 서로게이트 PK)
    LOG_ID                        BIGSERIAL PRIMARY KEY,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 그룹 코드
    GROUP_CODE                   VARCHAR(10),
    -- 등급 ID
    LEVEL_ID                     INTEGER,
    -- 등급명
    LEVEL_NAME                   VARCHAR(20),
    -- 관리자명
    ADMIN_USER_NAME              VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14)
);

-- 사용자 로그인
CREATE TABLE IF NOT EXISTS OP_USER_LOGIN (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED_AT                   TIMESTAMP,
    -- 수정일시
    UPDATED_AT                   TIMESTAMP,
    -- 세션ID
    SESSION_ID                   VARCHAR(1000) NOT NULL,
    -- 사용자ID
    USER_ID                      BIGINT NOT NULL,
    PRIMARY KEY (ID)
);

-- 로그인 로그
CREATE TABLE IF NOT EXISTS OP_USER_LOGIN_LOG (
    -- 로그인 로그 ID
    LOGIN_LOG_ID                 INTEGER NOT NULL,
    -- 로그인 타입
    LOGIN_TYPE                   VARCHAR(100) NOT NULL,
    -- 사용자ID
    LOGIN_ID                     VARCHAR(300) NOT NULL,
    -- 로그인 성공유무 (Y: 성공 N: 실패)
    SUCCESS_FLAG                 VARCHAR(1) NOT NULL DEFAULT 'N',
    -- ip주소
    REMOTE_ADDR                  VARCHAR(300),
    -- 비고
    MEMO                         VARCHAR(200),
    -- 로그인 일자
    LOGIN_DATE                   VARCHAR(14) NOT NULL,
    PRIMARY KEY (LOGIN_LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_user_login_log_login_log_id_seq START WITH 1000;
ALTER TABLE OP_USER_LOGIN_LOG ALTER COLUMN LOGIN_LOG_ID SET DEFAULT nextval('op_user_login_log_login_log_id_seq');
ALTER SEQUENCE op_user_login_log_login_log_id_seq OWNED BY OP_USER_LOGIN_LOG.LOGIN_LOG_ID;

-- =====================================================================
-- 회원 액션 로그 (admin opmanager/log/user-action-log 재현) - admindb에도 동일 이름의
-- 배치 스캔 잔재 테이블이 있지만 그건 원본 단일DB 시절의 유령 테이블이다(DB per Service
-- 상 회원 활동은 member가 실제로 소유해야 함) - 이 테이블이 진짜 authoritative source다.
-- SessionRehydrateInterceptor가 로그인된 회원의 상태변경(POST/PUT/PATCH/DELETE) 요청마다
-- 기록하고, admin은 cross-service API로 읽기만 한다.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_USER_ACTION_LOG (
    ACTION_LOG_ID                 INTEGER NOT NULL,
    CREATED_DATE                  VARCHAR(14) NOT NULL,
    REMOTE_ADDR                   VARCHAR(300),
    REQUEST_URI                   VARCHAR(500),
    REQUEST_METHOD                VARCHAR(10),
    LOGIN_ID                      VARCHAR(300) NOT NULL,
    PRIMARY KEY (ACTION_LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_user_action_log_action_log_id_seq START WITH 1000;
ALTER TABLE OP_USER_ACTION_LOG ALTER COLUMN ACTION_LOG_ID SET DEFAULT nextval('op_user_action_log_action_log_id_seq');
ALTER SEQUENCE op_user_action_log_action_log_id_seq OWNED BY OP_USER_ACTION_LOG.ACTION_LOG_ID;

-- 사용자 부모정보
CREATE TABLE IF NOT EXISTS OP_USER_PARENT (
    PARENT_ID                    BIGINT NOT NULL,
    USER_ID                      BIGINT,
    USER_NAME                    VARCHAR(300),
    -- 성별
    GENDER                       VARCHAR(300),
    -- 국적
    NATIONAL_INFO                VARCHAR(300),
    CI                           VARCHAR(300),
    DI                           VARCHAR(300),
    DN                           VARCHAR(300),
    -- 통신사
    CELL_CORP                    VARCHAR(300),
    -- 전화번호
    CELL_NO                      VARCHAR(300),
    -- 등록일시
    CREATED_AT                   TIMESTAMP,
    PRIMARY KEY (PARENT_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_user_parent_parent_id_seq START WITH 1000;
ALTER TABLE OP_USER_PARENT ALTER COLUMN PARENT_ID SET DEFAULT nextval('op_user_parent_parent_id_seq');
ALTER SEQUENCE op_user_parent_parent_id_seq OWNED BY OP_USER_PARENT.PARENT_ID;

-- 회원별 권한
CREATE TABLE IF NOT EXISTS OP_USER_ROLE (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 권한ID
    AUTHORITY                    VARCHAR(50) NOT NULL,
    PRIMARY KEY (USER_ID, AUTHORITY)
);

-- 휴면계정 테이블
CREATE TABLE IF NOT EXISTS OP_USER_SLEEP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 이름
    USER_NAME                    VARCHAR(500),
    -- 이메일
    EMAIL                        VARCHAR(500),
    -- 2015.8.1부터 시행하는 새 우편번호
    NEW_POST                     VARCHAR(500),
    -- 우편번호
    POST                         VARCHAR(500),
    -- 주소
    ADDRESS                      VARCHAR(500),
    -- 상세 주소
    ADDRESS_DETAIL               VARCHAR(500),
    -- 전화번호
    TEL_NUMBER                   VARCHAR(500),
    -- 휴대폰번호
    PHONE_NUMBER                 VARCHAR(500),
    -- 팩스 번호
    FAX_NUMBER                   VARCHAR(500),
    -- 생년월일 분류(1:양력, 2:음력)
    BIRTHDAY_TYPE                VARCHAR(500),
    -- 생년월일
    BIRTHDAY                     VARCHAR(500),
    -- 휴면일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (USER_ID)
);

-- USER SNS TABLE
CREATE TABLE IF NOT EXISTS OP_USER_SNS (
    -- 내부시퀀스
    SNS_USER_ID                  BIGINT NOT NULL,
    -- SNS 제공 사용자 고유 ID
    SNS_ID                       VARCHAR(50) NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- SNS종류 (naver, facebook, kakao)
    SNS_TYPE                     VARCHAR(20) NOT NULL,
    -- SNS 제공 사용자명
    SNS_NAME                     VARCHAR(350),
    -- SNS 제공 사용자 EMAIL
    EMAIL                        VARCHAR(420),
    -- 사용자별 SNS 등록 순번
    CREATED_ORDER                INTEGER NOT NULL DEFAULT 0,
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 정보 입력 완료 시간
    CERTIFIED_DATE               VARCHAR(14),
    PRIMARY KEY (SNS_USER_ID)
);

-- same-service FKs, added after every table exists (avoids creation-order failures)
ALTER TABLE OP_SESSION_ATTRIBUTES ADD CONSTRAINT fk_op_session_attributes_session_primary_id FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES OP_SESSION (PRIMARY_ID);
ALTER TABLE OP_SNS_USER ADD CONSTRAINT fk_op_sns_user_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);
ALTER TABLE OP_SNS_USER_DETAIL ADD CONSTRAINT fk_op_sns_user_detail_sns_user_id FOREIGN KEY (SNS_USER_ID) REFERENCES OP_SNS_USER (SNS_USER_ID);
ALTER TABLE OP_USER_BIRTHDAY ADD CONSTRAINT fk_op_user_birthday_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);
ALTER TABLE OP_USER_DELIVERY ADD CONSTRAINT fk_op_user_delivery_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);
ALTER TABLE OP_USER_DETAIL ADD CONSTRAINT fk_op_user_detail_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);
ALTER TABLE OP_USER_GROUP ADD CONSTRAINT fk_op_user_group_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);
ALTER TABLE OP_USER_PARENT ADD CONSTRAINT fk_op_user_parent_user_id FOREIGN KEY (USER_ID) REFERENCES OP_USER (USER_ID);

-- ===================================================================
-- 기존 파일에서 보존된 내용 (AS-IS에 없는 프로젝트 고유 테이블/컬럼/시퀀스/시드 데이터)
-- ===================================================================

CREATE TABLE IF NOT EXISTS OP_CUSTOMER (
    CUSTOMER_CODE               VARCHAR(50)  PRIMARY KEY,
    CUSTOMER_NAME                VARCHAR(255),
    USER_ID                      BIGINT,
    CUSTOMER_TYPE                VARCHAR(50),
    CUSTOMER_GROUP               VARCHAR(50),
    BUSINESS_NUMBER              VARCHAR(50),
    TEL_NUMBER                   VARCHAR(50),
    BOSS_NAME                    VARCHAR(255),
    CATEGORY                     VARCHAR(255),
    EVENT                        VARCHAR(255),
    ZIPCODE                      VARCHAR(20),
    ADDRESS                      TEXT,
    ADDRESS_DETAIL               VARCHAR(255),
    MEMO                         TEXT,
    STAFF_NAME                   VARCHAR(255),
    STAFF_DEPARTMENT             VARCHAR(255),
    STAFF_TEL_NUMBER             VARCHAR(50),
    STAFF_PHONE_NUMBER           VARCHAR(50),
    BANK_NUMBER                  VARCHAR(50),
    BANK_NAME                    VARCHAR(255),
    BANK_IN_NAME                 VARCHAR(255),
    BANK_CMS_CODE                VARCHAR(50),
    CUSTOMER_STAFF_NAME          VARCHAR(255),
    CUSTOMER_STAFF_POSITION      VARCHAR(255),
    CUSTOMER_STAFF_TEL_NUMBER    VARCHAR(50),
    CUSTOMER_STAFF_PHONE_NUMBER  VARCHAR(50),
    CUSTOMER_STAFF_EMAIL         VARCHAR(255),
    DM_ZIPCODE                   VARCHAR(20),
    DM_ADDRESS                   TEXT,
    DM_ADDRESS_DETAIL            VARCHAR(255),
    BUSINESS_NUMBER_CODE         VARCHAR(50),
    FAX_GROUP                    VARCHAR(50),
    FAX_NUMBER                   VARCHAR(50),
    HOMEPAGE                     VARCHAR(255),
    -- guess: Customer.java declares createDate/updateDate as String, populated
    -- via CommonMapper.datetime ('%Y%m%d%H%i%s'); kept as VARCHAR not TIMESTAMP
    CREATE_DATE                  VARCHAR(20),
    UPDATE_DATE                  VARCHAR(20)
);

-- Common code table for member service (no-hardcoding principle: code-type data lives in DB, not Java enums)
-- Structure mirrors AS-IS OP_COMMON_CODE (see ghlove-msa/database/ddl/service-admin.sql) for future consolidation.
CREATE TABLE IF NOT EXISTS OP_COMMON_CODE (
    CODE_TYPE       VARCHAR(50)  NOT NULL,
    CODE_LANGUAGE      VARCHAR(10)  NOT NULL,
    ID              VARCHAR(50)  NOT NULL,
    LABEL           VARCHAR(255),
    DETAIL          TEXT,
    ORDERING        INTEGER,
    USE_YN          CHAR(1),
    UP_ID           VARCHAR(50),
    CODE_VALUE      VARCHAR(255),
    EXTENSION_CODE  VARCHAR(50),
    MAPPING_CODE    VARCHAR(50),
    PRIMARY KEY (CODE_TYPE, CODE_LANGUAGE, ID)
);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('USER_STATUS', 'ko', 'ACTIVE',    '정상',   1, 'Y'),
('USER_STATUS', 'ko', 'DORMANT',   '휴면',   2, 'Y'),
('USER_STATUS', 'ko', 'LOCKED',    '잠금',   3, 'Y'),
('USER_STATUS', 'ko', 'WITHDRAWN', '탈퇴',   4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('USER_TYPE', 'ko', 'GENERAL',  '일반회원',      1, 'Y'),
('USER_TYPE', 'ko', 'OVERSEAS', '재외국민',      2, 'Y'),
('USER_TYPE', 'ko', 'LOCALGOV', '지자체 담당자', 3, 'Y'),
('USER_TYPE', 'ko', 'PROVIDER', '답례품 제공자', 4, 'Y'),
('USER_TYPE', 'ko', 'ADMIN',    '시스템운영자',  5, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('LOGIN_PATH', 'ko', 'EMAIL', '이메일',  1, 'Y'),
('LOGIN_PATH', 'ko', 'KAKAO', '카카오',  2, 'Y'),
('LOGIN_PATH', 'ko', 'NAVER', '네이버',  3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- ID generation for OP_USER (AS-IS had no auto-increment; added for the new member service implementation)
CREATE SEQUENCE IF NOT EXISTS op_user_user_id_seq START WITH 1000;

ALTER TABLE OP_USER ALTER COLUMN USER_ID SET DEFAULT nextval('op_user_user_id_seq');

ALTER SEQUENCE op_user_user_id_seq OWNED BY OP_USER.USER_ID;

-- ID generation for OP_USER_LOGIN_LOG (AS-IS had no auto-increment; added for login audit logging, SFR-002)
CREATE SEQUENCE IF NOT EXISTS op_user_login_log_id_seq;

ALTER TABLE OP_USER_LOGIN_LOG ALTER COLUMN LOGIN_LOG_ID SET DEFAULT nextval('op_user_login_log_id_seq');

ALTER SEQUENCE op_user_login_log_id_seq OWNED BY OP_USER_LOGIN_LOG.LOGIN_LOG_ID;

-- RBAC role labels (no-hardcoding principle: role display names come from OP_COMMON_CODE, not Java)
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('ROLE', 'ko', 'ROLE_USER',     '일반 사용자',   1, 'Y'),
('ROLE', 'ko', 'ROLE_LOCALGOV', '지자체 담당자', 2, 'Y'),
('ROLE', 'ko', 'ROLE_PROVIDER', '답례품 제공자', 3, 'Y'),
('ROLE', 'ko', 'ROLE_ADMIN',    '시스템 운영자', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 2: 외부 연계시스템 (디지털원패스 SSO, 카카오/네이버 SNS 로그인). AS-IS의
-- OP_USER.MBER_CI/MBER_DI/MBER_DN(본인인증 CI/DI)와 OP_USER_SNS(SNS 연동)를
-- 그대로 재사용 - 둘 다 배치 스캔으로 이미 이 서비스 DB에 존재하지만
-- OP_USER_SNS는 PK 시퀀스가 없었다 (OP_ITEM 등과 동일한 패턴).
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_user_sns_sns_user_id_seq;

ALTER TABLE OP_USER_SNS ALTER COLUMN SNS_USER_ID SET DEFAULT nextval('op_user_sns_sns_user_id_seq');

ALTER SEQUENCE op_user_sns_sns_user_id_seq OWNED BY OP_USER_SNS.SNS_USER_ID;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('LOGIN_PATH', 'ko', 'ONEPASS', '디지털원패스', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 3 (SFR-002 gap fill): 지자체담당자/제공자 역할 신청·승인 워크플로,
-- 휴면계정 전환/해제 배치. USER_STATUS.DORMANT 코드는 이미 배치 스캔으로
-- 존재했지만 실제 전환/해제 로직은 없었다.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_USER_ROLE_REQUEST (
    REQUEST_ID       BIGSERIAL PRIMARY KEY,
    USER_ID          BIGINT      NOT NULL,
    REQUESTED_ROLE   VARCHAR(50) NOT NULL,
    REASON           VARCHAR(255),
    STATUS           VARCHAR(20) NOT NULL,
    CREATED_DATE     TIMESTAMP   NOT NULL DEFAULT now(),
    PROCESSED_DATE   TIMESTAMP
);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('ROLE_REQUEST_STATUS', 'ko', 'REQUESTED', '심사중', 1, 'Y'),
('ROLE_REQUEST_STATUS', 'ko', 'APPROVED',  '승인',   2, 'Y'),
('ROLE_REQUEST_STATUS', 'ko', 'REJECTED',  '반려',   3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 휴면 전환 기준일 (기본 365일 미접속, 하드코딩 금지 원칙에 따라 DB 관리)
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, CODE_VALUE, ORDERING, USE_YN) VALUES
('SYSTEM_CONFIG', 'ko', 'DORMANT_INACTIVE_DAYS', '휴면 전환 기준(미접속 일수)', '365', 1, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 4 (마이페이지 답례품 영역 gap fill): "배송지 관리". OP_USER_DELIVERY는
-- 배치 스캔으로 이미 존재했지만(AS-IS 주소록 테이블), 다른 배치 스캔 테이블과
-- 동일하게 PK 시퀀스가 없었다.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_user_delivery_user_delivery_id_seq START WITH 1000;

ALTER TABLE OP_USER_DELIVERY ALTER COLUMN USER_DELIVERY_ID SET DEFAULT nextval('op_user_delivery_user_delivery_id_seq');

ALTER SEQUENCE op_user_delivery_user_delivery_id_seq OWNED BY OP_USER_DELIVERY.USER_DELIVERY_ID;

-- =====================================================================
-- "마이페이지 > 회원탈퇴" (AS-IS users/secede.html) - OP_USER_DETAIL.LEAVE_CODE는
-- 배치 스캔으로 이미 존재했지만("guess: 셀렉트 컬럼으로만 보임") 어떤 코드 값이 들어가는지
-- 몰라서 처음엔 자체발명 값으로 미매핑을 채웠다가, "09. 공통코드 목록.xlsx"에서 실제
-- AS-IS 값(001~005)을 확인해 소급 정정한다. withdraw.html은 leaveCodeList를 그대로
-- 순회 렌더링하므로 템플릿 변경은 필요 없다.
-- =====================================================================
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('LEAVE_CODE', 'ko', '001', '탈퇴 후 재가입을 위해서', 1, 'Y'),
('LEAVE_CODE', 'ko', '002', '사고 싶은 상품이 없어서', 2, 'Y'),
('LEAVE_CODE', 'ko', '003', '자주 이용하지 않아서', 3, 'Y'),
('LEAVE_CODE', 'ko', '004', '서비스 및 고객지원이 만족스럽지 않아서', 4, 'Y'),
('LEAVE_CODE', 'ko', '005', '광고성 알림이 너무 많이 와서', 5, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- LOGIN_PATH ("09. 공통코드 목록.xlsx" 실제값) - User.loginPathCode가 처음엔 "EMAIL"/
-- provider명 그대로(자체발명값)였는데, 실제 AS-IS 공통코드는 100/300/500/600이다.
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('LOGIN_PATH', 'ko', '100', 'ID/PWD', 1, 'Y'),
('LOGIN_PATH', 'ko', '200', '간편인증', 2, 'Y'),
('LOGIN_PATH', 'ko', '300', '디지털원패스', 3, 'Y'),
('LOGIN_PATH', 'ko', '400', 'PKI', 4, 'Y'),
('LOGIN_PATH', 'ko', '500', '카카오인증', 5, 'Y'),
('LOGIN_PATH', 'ko', '600', '네이버인증', 6, 'Y'),
('LOGIN_PATH', 'ko', '700', '민간개방API', 7, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- SFR-002 "다중 인증체계(MFA) 선택 적용" gap fill - 일반회원이 마이페이지에서 스스로 켜고
-- 끄는 opt-in 옵션. AS-IS에는 대응 컬럼이 없다(관리자 콘솔의 강제 이메일 2차인증과 별개).
ALTER TABLE OP_USER ADD COLUMN IF NOT EXISTS MFA_ENABLED VARCHAR(1) NOT NULL DEFAULT 'N';

-- SFR-002 "데이터 파기 절차: 로그 데이터 분리 보관(익명화), 법적 보관 의무 데이터 별도 관리,
-- 파기 이력 기록 및 보고" gap fill. 탈퇴(WITHDRAWN) 후 일정 유예기간이 지나면 개인식별정보를
-- 익명화하고(행 자체는 FK 무결성을 위해 유지), 그 파기 사실을 별도 이력 테이블에 남긴다.
CREATE TABLE IF NOT EXISTS USER_DATA_DESTRUCTION_LOG (
    DESTRUCTION_ID   BIGSERIAL    PRIMARY KEY,
    USER_ID          BIGINT       NOT NULL,
    DESTROYED_FIELDS VARCHAR(500),
    REASON           VARCHAR(200),
    DESTROYED_DATE   VARCHAR(14)  NOT NULL
);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, CODE_VALUE, ORDERING, USE_YN) VALUES
('SYSTEM_CONFIG', 'ko', 'WITHDRAWN_DATA_RETENTION_DAYS', '탈퇴회원 개인정보 보관기간(일)', '30', 90, 'Y'),
('SYSTEM_CONFIG', 'ko', 'LOGIN_LOG_RETENTION_DAYS', '로그인 로그 익명화 보관기간(일)', '365', 91, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- SFR-002 "인증토큰/세션 관리(만료, 재인증 정책)" - AS-IS에 없던 신규 테이블(그래서 OP_
-- 접두사 없이 USER_DATA_DESTRUCTION_LOG와 동일한 관례). GH_AUTH 액세스 JWT(기본 120분)가
-- 만료돼도 재로그인 없이 새 액세스 토큰을 받을 수 있도록, 별도의 장기(기본 14일) 불투명
-- 토큰을 회전(rotate) 방식으로 발급/폐기한다.
CREATE TABLE IF NOT EXISTS USER_REFRESH_TOKEN (
    TOKEN_ID     BIGSERIAL    PRIMARY KEY,
    USER_ID      BIGINT       NOT NULL,
    TOKEN        VARCHAR(64)  NOT NULL,
    EXPIRES_AT   VARCHAR(14)  NOT NULL,
    CREATED_DATE VARCHAR(14)  NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_refresh_token_token ON USER_REFRESH_TOKEN (TOKEN);
