-- ===================================================================
-- TO-BE DB 재구성(2026-09): 물리 2개(gift + 나머지), 논리 6개 스키마 구조로 전환.
-- 이 파일은 이제 자신의 단독 DB가 아니라 'ghlove_core' DB 안의 'point' 스키마에 적용한다.
-- 실행예: psql -U postgres -d ghlove_core -f service-point.sql
-- ===================================================================
CREATE SCHEMA IF NOT EXISTS point;
SET search_path TO point;

-- Service: point
-- ===================================================================
-- AS-IS 운영 DB 원본 스키마 기준 자동 생성 (10개 테이블)
-- 출처: 운영사이트 DB dump(CUBRID) -> PostgreSQL 변환. 인덱스는 이번 생성에서 제외(추후 실사용 쿼리 패턴 기반으로 추가).
-- 서비스간 FK는 제거됨(no cross-service FK 원칙).
-- ===================================================================

-- 기부 사용 포인트
CREATE TABLE IF NOT EXISTS G_CNTR_USE_POINT (
    CNTR_SN                      VARCHAR(50) NOT NULL,
    -- 사용 일련번호
    USE_SN                       INTEGER NOT NULL,
    -- 포인트 사용 일자
    POINT_USE_DE                 VARCHAR(8) NOT NULL,
    -- 기부사용포인트
    CNTR_USE_POINT               BIGINT,
    -- 회원 고유번호
    USER_ID                      BIGINT NOT NULL,
    PSITN_LOCGOV_CODE            VARCHAR(10) NOT NULL,
    CNTR_LOCGOV_CODE             VARCHAR(10) NOT NULL,
    -- 사용 구분 코드(1사용, 2소멸,3탈퇴)
    USE_SE_CODE                  VARCHAR(10),
    -- 사용 내용
    USE_CN                       VARCHAR(2000),
    -- ORDER_CODE
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문URL
    URL_ADRES                    VARCHAR(200),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (CNTR_SN, USE_SN)
);

CREATE TABLE IF NOT EXISTS G_CNTR_USE_POINT_HISTORY (
    CNTR_SN                      VARCHAR(50) NOT NULL,
    POINT_USE_DE                 VARCHAR(8) NOT NULL,
    CNTR_USE_POINT               BIGINT NOT NULL,
    USER_ID                      BIGINT NOT NULL,
    PSITN_LOCGOV_CODE            VARCHAR(10) NOT NULL,
    CNTR_LOCGOV_CODE             VARCHAR(10) NOT NULL,
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    REMARK                       VARCHAR(300) NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CNTR_SN)
);

-- 출석체크 마스터
CREATE TABLE IF NOT EXISTS OP_ATTENDANCE (
    -- ID
    ATTENDANCE_ID                BIGINT NOT NULL,
    -- 출석 해당 연도
    YEAR                         VARCHAR(4) NOT NULL,
    -- 출석 해당 월
    MONTH                        VARCHAR(2) NOT NULL,
    -- 상단 컨텐츠
    CONTENT_TOP                  VARCHAR,
    -- 하단 컨텐츠
    CONTENT_BOTTOM               VARCHAR,
    -- 수정자 아이디
    UPDATED_BY                   VARCHAR(20),
    -- 최종 수정 날짜
    UPDATED_DATE                 VARCHAR(14),
    -- 생성자 아이디
    CREATED_BY                   VARCHAR(20),
    -- 생성 날짜
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ATTENDANCE_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_attendance_attendance_id_seq START WITH 1000;
ALTER TABLE OP_ATTENDANCE ALTER COLUMN ATTENDANCE_ID SET DEFAULT nextval('op_attendance_attendance_id_seq');
ALTER SEQUENCE op_attendance_attendance_id_seq OWNED BY OP_ATTENDANCE.ATTENDANCE_ID;

-- 출석체크
CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_CHECK (
    -- ID
    ATTENDANCE_CHECK_ID          BIGINT NOT NULL,
    -- ATTENDANCE_ID
    ATTENDANCE_ID                BIGINT,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL,
    -- 출석체크일자
    CHECKED_DATE                 VARCHAR(8) NOT NULL,
    -- 출석체크시간
    CHECKED_TIME                 VARCHAR(8) NOT NULL,
    PRIMARY KEY (ATTENDANCE_CHECK_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_attendance_check_attendance_check_id_seq START WITH 1000;
ALTER TABLE OP_ATTENDANCE_CHECK ALTER COLUMN ATTENDANCE_CHECK_ID SET DEFAULT nextval('op_attendance_check_attendance_check_id_seq');
ALTER SEQUENCE op_attendance_check_attendance_check_id_seq OWNED BY OP_ATTENDANCE_CHECK.ATTENDANCE_CHECK_ID;

-- 출석체크 이벤트 설정
CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_CONFIG (
    -- ID
    ATTENDANCE_CONFIG_ID         BIGINT NOT NULL,
    -- ATTENDANCE_ID
    ATTENDANCE_ID                BIGINT NOT NULL,
    -- 이벤트 코드
    EVENT_CODE                   VARCHAR(20) NOT NULL,
    -- 연속출석여부
    CONTINUE_YN                  VARCHAR(1) NOT NULL,
    -- 출석일수
    DAYS                         INTEGER NOT NULL,
    -- 수정자 아이디
    UPDATED_BY                   VARCHAR(20),
    -- 최종 수정 날짜
    UPDATED_DATE                 VARCHAR(14),
    -- 생성자 아이디
    CREATED_BY                   VARCHAR(20),
    -- 생성 날짜
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ATTENDANCE_CONFIG_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_attendance_config_attendance_config_id_seq START WITH 1000;
ALTER TABLE OP_ATTENDANCE_CONFIG ALTER COLUMN ATTENDANCE_CONFIG_ID SET DEFAULT nextval('op_attendance_config_attendance_config_id_seq');
ALTER SEQUENCE op_attendance_config_attendance_config_id_seq OWNED BY OP_ATTENDANCE_CONFIG.ATTENDANCE_CONFIG_ID;

-- 출석체크 이벤트 달성정보
CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_EVENT (
    -- ID
    ATTENDANCE_EVENT_ID          BIGINT NOT NULL,
    -- ATTENDANCE_ID
    ATTENDANCE_ID                BIGINT,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL,
    -- 이벤트 코드
    EVENT_CODE                   VARCHAR(20) NOT NULL,
    -- 연속출석여부
    CONTINUE_YN                  VARCHAR(1),
    -- 이벤트당첨출석일수
    DAYS                         INTEGER NOT NULL DEFAULT 0,
    -- 출석체크일수
    CHECKED_DAYS                 INTEGER NOT NULL DEFAULT 0,
    -- 이벤트달성여부
    SUCCESS_YN                   VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 최종수정일자
    UPDATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (ATTENDANCE_EVENT_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_attendance_event_attendance_event_id_seq START WITH 1000;
ALTER TABLE OP_ATTENDANCE_EVENT ALTER COLUMN ATTENDANCE_EVENT_ID SET DEFAULT nextval('op_attendance_event_attendance_event_id_seq');
ALTER SEQUENCE op_attendance_event_attendance_event_id_seq OWNED BY OP_ATTENDANCE_EVENT.ATTENDANCE_EVENT_ID;

-- 포인트 적립 및 사용 가능 포인트 내역
CREATE TABLE IF NOT EXISTS OP_POINT (
    -- ID
    POINT_ID                     INTEGER NOT NULL,
    -- 포인트 구분 (point : 포인트)
    POINT_TYPE                   VARCHAR(20) NOT NULL DEFAULT 'point',
    -- 적립 구분 (1:일괄지급, 2:개별지급)
    SAVED_TYPE                   VARCHAR(1) NOT NULL DEFAULT '2',
    -- 적립 년도
    SAVED_YEAR                   VARCHAR(4),
    -- 적립 월
    SAVED_MONTH                  VARCHAR(2),
    -- 적립된 포인트 
    SAVED_POINT                  INTEGER NOT NULL DEFAULT 0,
    -- 사용 가능한 포인트
    POINT                        INTEGER NOT NULL DEFAULT 0,
    -- 적립 사유
    REASON                       VARCHAR(255) NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 회원ID
    MANAGER_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 주문 번호
    ORDER_CODE                   VARCHAR(50),
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER,
    -- 주문 상품 순번
    ITEM_SEQUENCE                INTEGER,
    -- 만료일
    EXPIRATION_DATE              VARCHAR(8) NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (POINT_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_point_point_id_seq START WITH 1000;
ALTER TABLE OP_POINT ALTER COLUMN POINT_ID SET DEFAULT nextval('op_point_point_id_seq');
ALTER SEQUENCE op_point_point_id_seq OWNED BY OP_POINT.POINT_ID;

-- 적립금설정
CREATE TABLE IF NOT EXISTS OP_POINT_CONFIG (
    -- 적립금설정ID
    POINT_CONFIG_ID              INTEGER NOT NULL DEFAULT 0,
    -- 설정구분 (1: 공통설정, 2: 상품설정)
    CONFIG_TYPE                  VARCHAR(1) NOT NULL,
    -- 기간구분 (1:일반설정, 2:특정일)
    PERIOD_TYPE                  VARCHAR(1) NOT NULL,
    -- 적립구분 (1:비율, 2:금액)
    POINT_TYPE                   VARCHAR(1) NOT NULL,
    -- 적립포인트
    POINT                        DOUBLE PRECISION NOT NULL DEFAULT 0.00000000000000,
    -- 시작일
    START_DATE                   VARCHAR(8) NOT NULL,
    -- 시작시간
    START_TIME                   VARCHAR(2) NOT NULL,
    -- 종료일
    END_DATE                     VARCHAR(8) NOT NULL,
    -- 종료시간
    END_TIME                     VARCHAR(2) NOT NULL,
    -- 기간내 포인트 적립 반복일자 (값이 있는 경우 PERIOD_TYPE=2)
    REPEAT_DAY                   VARCHAR(2) NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER DEFAULT 0,
    -- 상태코드 (1:정상, 2:삭제)
    STATUS_CODE                  VARCHAR(1) DEFAULT '1',
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (POINT_CONFIG_ID)
);

-- 포인트 사용 내역
CREATE TABLE IF NOT EXISTS OP_POINT_USED (
    -- 포인트 사용 내역 ID
    POINT_USED_ID                INTEGER NOT NULL DEFAULT 0,
    -- 사용포인트 GROUP KEY
    POINT_USED_GROUP_ID          INTEGER NOT NULL,
    -- 포인트 ID
    POINT_ID                     INTEGER NOT NULL DEFAULT 0,
    -- 사용 구분 (1: 사용, 2:소멸)
    USED_TYPE                    VARCHAR(1) NOT NULL DEFAULT '0',
    -- 사용한 포인트
    POINT                        INTEGER NOT NULL DEFAULT 0,
    -- 사용 내역
    DETAILS                      VARCHAR(255) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50),
    -- 회원ID
    MANAGER_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 사용일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 환불시 복원될 포인트
    REMAINING_POINT              INTEGER DEFAULT 0,
    PRIMARY KEY (POINT_USED_ID)
);

-- same-service FKs, added after every table exists (avoids creation-order failures)
ALTER TABLE OP_ATTENDANCE_CHECK ADD CONSTRAINT fk_op_attendance_check_attendance_id FOREIGN KEY (ATTENDANCE_ID) REFERENCES OP_ATTENDANCE (ATTENDANCE_ID);
ALTER TABLE OP_ATTENDANCE_CONFIG ADD CONSTRAINT fk_op_attendance_config_attendance_id FOREIGN KEY (ATTENDANCE_ID) REFERENCES OP_ATTENDANCE (ATTENDANCE_ID);
ALTER TABLE OP_ATTENDANCE_EVENT ADD CONSTRAINT fk_op_attendance_event_attendance_id FOREIGN KEY (ATTENDANCE_ID) REFERENCES OP_ATTENDANCE (ATTENDANCE_ID);

-- ===================================================================
-- 기존 파일에서 보존된 내용 (AS-IS에 없는 프로젝트 고유 테이블/컬럼/시퀀스/시드 데이터)
-- ===================================================================

-- =====================================================================
-- MVP additions for the new point microservice implementation (SFR-004).
-- The legacy OP_POINT/OP_POINT_USED/OP_POINT_CONFIG/G_CNTR_USE_POINT tables
-- above are shopping-mall/attendance-event point tables (order codes, item
-- sequences, attendance streaks) that don't give the ledger/balance
-- separation SFR-004 requires, so the new service uses its own tables
-- below instead of force-fitting them.
-- =====================================================================

-- Common code table for point service (no-hardcoding principle). Structure
-- mirrors member/donation's OP_COMMON_CODE - each service owns its own copy.
CREATE TABLE IF NOT EXISTS OP_COMMON_CODE (
    CODE_TYPE       VARCHAR(50)  NOT NULL,
    CODE_LANGUAGE   VARCHAR(10)  NOT NULL,
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
('PT_TXN_TYPE', 'ko', 'EARN',    '적립', 1, 'Y'),
('PT_TXN_TYPE', 'ko', 'USE',     '사용', 2, 'Y'),
('PT_TXN_TYPE', 'ko', 'REVERSE', '취소회수', 3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 기본 적립률/유효기간 (지자체별 설정이 없을 때의 기본값 - 하드코딩 금지 원칙에 따라 DB 보관)
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, CODE_VALUE, ORDERING, USE_YN) VALUES
('SYSTEM_CONFIG', 'ko', 'DEFAULT_POINT_RATE', '기본 포인트 적립률(%, 지자체 설정 없을 때)', '30', 1, 'Y'),
('SYSTEM_CONFIG', 'ko', 'POINT_VALID_DAYS', '포인트 유효기간(일)', '1825', 2, 'Y'),
('SYSTEM_CONFIG', 'ko', 'MAX_POINT_RATE', '포인트 지급률 상한(%, 고향사랑 기부금법 제8조 답례품 제공한도)', '30', 3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 지자체별 기부포인트 적립률 (연도별). point 서비스는 donation 서비스의 DB를 직접 읽을 수
-- 없으므로(DB per Service) 자체 사본을 보관한다 - donation의 G_CTBNY_SETUP.POINT_RATE와
-- 같은 개념이지만 별도 소유 데이터.
CREATE TABLE IF NOT EXISTS PT_LOCGOV_POINT_RATE (
    STDR_YEAR    VARCHAR(4)   NOT NULL,
    LOCGOV_CODE  VARCHAR(50)  NOT NULL,
    POINT_RATE   NUMERIC(5,2) NOT NULL,
    -- admin 지자체관리 화면의 "포인트 지급률 등록/이력조회"가 쓰는 감사 컬럼 (원래 이 표에는
    -- 없었으나 admin 쓰기 경로가 생기며 추가 - AS-IS의 포인트 지급률 변경이력 팝업(변경일/작성자)과
    -- 동등하게 보여주기 위함. 연도별 행 자체가 이력이라 별도 이력 테이블은 두지 않는다.)
    LAST_UPDT_PNTTM  TIMESTAMP,
    LAST_UPDUSR_NM   VARCHAR(50),
    PRIMARY KEY (STDR_YEAR, LOCGOV_CODE)
);

INSERT INTO PT_LOCGOV_POINT_RATE (STDR_YEAR, LOCGOV_CODE, POINT_RATE) VALUES
('2026', '11230', 30.00),
('2026', '26350', 30.00),
('2026', '36110', 35.00),
('2026', '50000', 30.00),
('2026', '46150', 40.00)
ON CONFLICT (STDR_YEAR, LOCGOV_CODE) DO NOTHING;

-- 회원별 잔액 (원장과 분리 관리 - SFR-004 명시 요구사항)
CREATE TABLE IF NOT EXISTS PT_POINT_BALANCE (
    USER_ID       BIGINT PRIMARY KEY,
    BALANCE       BIGINT NOT NULL DEFAULT 0,
    UPDATED_DATE  TIMESTAMP
);

-- 포인트 거래 원장 (append-only). POINT_AMOUNT는 부호 있는 값(+적립/-사용/-취소회수).
CREATE TABLE IF NOT EXISTS PT_POINT_LEDGER (
    LEDGER_ID        BIGSERIAL PRIMARY KEY,
    USER_ID          BIGINT       NOT NULL,
    LOCGOV_CODE      VARCHAR(50),
    TXN_TYPE         VARCHAR(20)  NOT NULL,
    POINT_AMOUNT     BIGINT       NOT NULL,
    REASON           VARCHAR(255),
    REF_KEY          VARCHAR(100), -- 예: donation 서비스의 CNTR_SN, 주문코드 등 (FK 아님 - 서비스 경계)
    EXPIRATION_DATE  VARCHAR(8),
    CREATED_DATE     TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_pt_point_ledger_user ON PT_POINT_LEDGER(USER_ID);

CREATE INDEX IF NOT EXISTS idx_pt_point_ledger_ref ON PT_POINT_LEDGER(REF_KEY, TXN_TYPE);

-- Round 2 addition: order service integration (SFR-006 choreography SAGA).
-- USE covers order-triggered deductions too (REF_KEY=order.saga의 orderId);
-- RESTORE is the compensating credit when a cancelled order's deduction is undone.
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('PT_TXN_TYPE', 'ko', 'RESTORE', '주문취소 복원', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 3 addition: 소멸 처리 (SFR-004 "소멸 처리"). Every credit-type ledger
-- row (EARN/RESTORE) is a FIFO "lot": REMAINING_AMOUNT starts equal to
-- POINT_AMOUNT and is drawn down as later USE/REVERSE debits consume it
-- oldest-expiring-first, so the expiration batch only extinguishes points
-- that are genuinely still unspent, not a user's whole balance. Existing
-- EARN/RESTORE rows created before this migration have REMAINING_AMOUNT/
-- EXPIRATION_DATE left NULL and are simply invisible to lot consumption/
-- expiration (acceptable for this dev DB's pre-existing test data - a real
-- backfill would need to reconstruct historical remaining balances).
-- =====================================================================
ALTER TABLE PT_POINT_LEDGER ADD COLUMN IF NOT EXISTS REMAINING_AMOUNT BIGINT;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('PT_TXN_TYPE', 'ko', 'EXPIRE', '유효기간 소멸', 5, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 4 (SFR-004 gap fill): 포인트 예약/예약해제. 지금까지는 order.saga의
-- ORDER_CREATED에 반응해 즉시 차감(deductForOrder)했는데, 요구사항은 결제 확정
-- 전 "예약(hold)" 상태를 두라고 명시한다. 기존 order SAGA 흐름은 그대로 두고
-- (호환성 유지, 실제 서비스가 아직 이 예약 API를 호출하도록 재배선되진 않음 -
-- "틀만 먼저" 요청에 따라 병행 가능한 기능으로 추가) 원장/잔액은 건드리지 않고
-- 예약 행만 쌓아 가용잔액(availableBalance = balance - 활성 예약 합계)을
-- 별도로 계산하는 방식을 택했다 - 예약 취소가 보상 트랜잭션 없이 즉시 가능해짐.
-- =====================================================================
CREATE TABLE IF NOT EXISTS PT_POINT_RESERVATION (
    RESERVATION_ID   BIGSERIAL PRIMARY KEY,
    USER_ID          BIGINT      NOT NULL,
    AMOUNT           BIGINT      NOT NULL,
    REF_KEY          VARCHAR(50),
    REASON           VARCHAR(255),
    STATUS           VARCHAR(20) NOT NULL, -- RESERVED / CONFIRMED / RELEASED
    CREATED_DATE     TIMESTAMP   NOT NULL DEFAULT now(),
    RESOLVED_DATE    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pt_point_reservation_user ON PT_POINT_RESERVATION(USER_ID, STATUS);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('PT_RESERVATION_STATUS', 'ko', 'RESERVED',  '예약중', 1, 'Y'),
('PT_RESERVATION_STATUS', 'ko', 'CONFIRMED', '확정',   2, 'Y'),
('PT_RESERVATION_STATUS', 'ko', 'RELEASED',  '해제됨', 3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 만료 예정 포인트 안내 기간 (기본 30일, 하드코딩 금지 원칙에 따라 DB 관리)
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, CODE_VALUE, ORDERING, USE_YN) VALUES
('SYSTEM_CONFIG', 'ko', 'POINT_EXPIRY_NOTICE_DAYS', '소멸 예정 안내 기준일(일)', '30', 1, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;
