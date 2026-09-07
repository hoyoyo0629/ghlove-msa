-- ===================================================================
-- TO-BE DB 재구성(2026-09): 물리 2개(gift + 나머지), 논리 6개 스키마 구조로 전환.
-- 이 파일은 이제 자신의 단독 DB가 아니라 'ghlove_core' DB 안의 'ord' 스키마에 적용한다.
-- 실행예: psql -U postgres -d ghlove_core -f service-order.sql
-- ===================================================================
CREATE SCHEMA IF NOT EXISTS ord;
SET search_path TO ord;

-- Service: order
-- ===================================================================
-- AS-IS 운영 DB 원본 스키마 기준 자동 생성 (55개 테이블)
-- 출처: 운영사이트 DB dump(CUBRID) -> PostgreSQL 변환. 인덱스는 이번 생성에서 제외(추후 실사용 쿼리 패턴 기반으로 추가).
-- 서비스간 FK는 제거됨(no cross-service FK 원칙).
-- ===================================================================

-- 대행구매 암복호화키 정보
CREATE TABLE IF NOT EXISTS G_AGENCY_PRIVATE_KEY (
    -- 사용자 세션 아이디
    USER_SESSION_ID              VARCHAR(50) NOT NULL,
    -- 개인키(복호화키)
    PRIVATE_KEY                  VARCHAR(5000) NOT NULL,
    -- 공개키(암호화키)
    PUBLIC_KEY                   VARCHAR(5000) NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_SESSION_ID)
);

-- 주문 대행 주문 로그
CREATE TABLE IF NOT EXISTS G_ORDER_AGENCY_ORDER_LOG (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품 순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 관리자 아이디
    MANAGER_ID                   BIGINT NOT NULL,
    -- 관리자 이름
    MANAGER_NM                   VARCHAR(50) NOT NULL,
    -- 관리자 지자체코드
    MANAGER_LCLGV_CD             VARCHAR(10),
    -- 행정 복지 센터 아이디
    PBADMS_WLFR_CNTR_ID          BIGINT NOT NULL,
    -- 사용자 아이디
    USER_ID                      BIGINT NOT NULL,
    -- 등록 일시
    REG_DT                       TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE)
);

-- 정산확인파일첨부
CREATE TABLE IF NOT EXISTS G_REMITTANCE_FILE (
    -- 정산 ID
    REMITTANCE_ID                BIGINT NOT NULL,
    -- 파일일련번호
    FILE_SEQ                     INTEGER NOT NULL,
    -- 파일명
    FILE_NAME                    VARCHAR(255),
    -- 업로드 파일명
    ORG_FILE_NAME                VARCHAR(255),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 실제저장 파일경로
    PATH_NAME                    VARCHAR(255),
    PRIMARY KEY (REMITTANCE_ID, FILE_SEQ)
);

-- 장바구니
CREATE TABLE IF NOT EXISTS OP_CART (
    -- 장바구니 고유번호
    CART_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 세션 ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 상품 고유번호
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 상품 구성 옵션(JSON TYPE)
    OPTIONS                      VARCHAR(4000),
    -- 배송지 지불 방법 (1 : 선불, 2 : 착불)
    SHIPPING_PAYMENT_TYPE        VARCHAR(1) NOT NULL,
    -- 묶음기준 코드
    SHIPPING_GROUP_CODE          VARCHAR(20) NOT NULL,
    -- 추가구성(Y:추가구성)
    ADDITION_ITEM_FLAG           VARCHAR(1) NOT NULL,
    -- 추가구성의 부모 상품 ID
    PARENT_ITEM_ID               INTEGER NOT NULL,
    -- 세트상품(Y:세트상품)
    SET_ITEM_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 생성일
    CREATED_DATE                 VARCHAR(14),
    TEXT_OPTION                  VARCHAR(4000),
    PRIMARY KEY (CART_ID)
);

-- 세트 장바구니
CREATE TABLE IF NOT EXISTS OP_CART_SET (
    -- 장바구니 고유번호
    CART_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품 고유번호
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 상품 구성 옵션(JSON TYPE)
    OPTIONS                      VARCHAR(4000),
    -- 세트상품 장바구니 ID
    PARENT_CART_ID               INTEGER,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CART_ID)
);

-- 현금 영수증 신청 목록 - 로그성 테이블 아님 현금영수증 금액이 0이면 취소된 거래 
CREATE TABLE IF NOT EXISTS OP_CASH_RECEIPT (
    -- 현금 영수증 ID
    CASH_RECEIPT_ID              INTEGER NOT NULL,
    -- PG사
    CASH_RECEIPT_PG_SERVICE_TYPE VARCHAR(40),
    -- 주문 코드
    ORDER_CODE                   VARCHAR(40) NOT NULL,
    -- 주문 순서
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 지불 순서
    PAYMENT_SEQUENCE             INTEGER NOT NULL,
    -- 현금영수증 금액
    CASH_RECEIPT_AMOUNT          INTEGER NOT NULL,
    -- 현금영수증 면세
    CASH_RECEIPT_TAX_FREE_AMOUNT INTEGER NOT NULL,
    -- 현금영수증 신청자명
    CASH_RECEIPT_NAME            VARCHAR(50) NOT NULL,
    -- 현금영수증 신청 정보 (전화번호 OR 사업자)
    CASH_RECEIPT_CODE            VARCHAR(50) NOT NULL,
    -- 현금영수증 신청 구분 (1: 사업자, 2 : 일반)
    CASH_RECEIPT_TYPE            VARCHAR(1) NOT NULL,
    -- 현금 영수증 발행 번호
    CASH_RECEIPT_ISSUE_NUMBER    VARCHAR(50),
    -- 현금 영수증 발행 일자
    CASH_RECEIPT_ISSUE_DATE      VARCHAR(14),
    -- 상태 (1:신청, 2:발행, 3:취소)
    CASH_RECEIPT_STATUS_CODE     VARCHAR(1) NOT NULL,
    -- 신청 일자
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CASH_RECEIPT_ID)
);

-- 현금영수증
CREATE TABLE IF NOT EXISTS OP_CASHBILL (
    ID                           BIGINT NOT NULL,
    CASHBILL_CODE                VARCHAR(20),
    CASHBILL_TYPE                VARCHAR(20),
    CREATED_BY                   VARCHAR(50),
    CREATED_DATE                 VARCHAR(14),
    CUSTOMER_NAME                VARCHAR(50),
    ORDER_CODE                   VARCHAR(50),
    PG_SERVICE                   VARCHAR(20),
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_CASHBILL_ISSUE (
    ID                           BIGINT NOT NULL,
    AMOUNT                       BIGINT NOT NULL,
    CANCELED_DATE                VARCHAR(14),
    CASHBILL_ID                  BIGINT,
    CASHBILL_ISSUE_TYPE          VARCHAR(20),
    CASHBILL_STATUS              VARCHAR(20),
    CREATED_DATE                 VARCHAR(14),
    ISSUED_DATE                  VARCHAR(14),
    ITEM_NAME                    VARCHAR(200),
    MGT_KEY                      VARCHAR(255),
    TAX_TYPE                     VARCHAR(20),
    UPDATE_BY                    VARCHAR(100),
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ID)
);

-- 클레임 메모
CREATE TABLE IF NOT EXISTS OP_CLAIM_MEMO (
    -- 클레임 메모 ID
    CLAIM_MEMO_ID                INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 사용자명
    USER_NAME                    VARCHAR(350) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50),
    -- 처리 상태 (1: 처리중, 2: 처리완료)
    CLAIM_STATUS                 VARCHAR(1) NOT NULL,
    -- 메모
    MEMO                         VARCHAR(1500) NOT NULL,
    -- 회원ID
    MANAGER_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 관리자 로그인 ID
    MANAGER_LOGIN_ID             VARCHAR(60) NOT NULL,
    -- 상태
    DATA_STATUS_CODE             DOUBLE PRECISION NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CLAIM_MEMO_ID)
);

-- 쿠폰 테이블
CREATE TABLE IF NOT EXISTS OP_COUPON (
    -- 쿠폰 고유ID
    COUPON_ID                    INTEGER NOT NULL,
    -- (WEB, MOBILE, APP)
    COUPON_TYPE                  VARCHAR(255) DEFAULT '',
    -- 쿠폰명
    COUPON_NAME                  VARCHAR(100) NOT NULL,
    -- 쿠폰설명
    COUPON_COMMENT               VARCHAR(500),
    COUPON_ISSUE_TYPE            VARCHAR(1) NOT NULL DEFAULT '0',
    -- 쿠폰 발급 기간 시작일
    COUPON_ISSUE_START_DATE      VARCHAR(14),
    -- 쿠폰 발급 기간 종료일
    COUPON_ISSUE_END_DATE        VARCHAR(14),
    COUPON_APPLY_TYPE            VARCHAR(1) NOT NULL DEFAULT '0',
    -- 일자 설정
    COUPON_APPLY_DAY             INTEGER,
    -- 쿠폰 사용 기간 시작일
    COUPON_APPLY_START_DATE      VARCHAR(14),
    -- 쿠폰 사용 기간 종료일
    COUPON_APPLY_END_DATE        VARCHAR(14),
    -- 발행 시점(1:일반, 2:회원가입, 3:생일, 4:상품구매 후 발행, 5:첫구매)
    COUPON_TARGET_TIME_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰대상 (1.전체, 2.선택 회원, 3.회원 등급)
    COUPON_TARGET_USER_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 회원 등급 ID (EX. 1||2||3)
    COUPON_TARGET_USER_LEVEL     VARCHAR(4000),
    -- 선택 회원 JSON STRING
    COUPON_TARGET_USER           TEXT,
    -- 사용가능 상품 판매가 (개당)
    COUPON_PAY_RESTRICTION       INTEGER NOT NULL DEFAULT -1,
    COUPON_CONCURRENTLY          VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰 할인 금액 타입 (1:원, 2:%)
    COUPON_PAY_TYPE              VARCHAR(1) NOT NULL,
    -- 쿠폰 할인 금액
    COUPON_PAY                   INTEGER NOT NULL,
    -- %할인 최대 할인금액
    COUPON_DISCOUNT_LIMIT_PRICE  INTEGER DEFAULT -1,
    -- 쿠폰상품조건 (1.전체상품, 2.특정상품)
    COUPON_TARGET_ITEM_TYPE      VARCHAR(1) NOT NULL DEFAULT '0',
    -- 특정상품 발행 조건
    COUPON_TARGET_ITEM           TEXT,
    -- 쿠폰 사용 유무
    COUPON_FLAG                  VARCHAR(1) NOT NULL DEFAULT 'Y',
    COUPON_OFFLINE_FLAG          VARCHAR(1) DEFAULT 'N',
    -- 생일 쿠폰 다운로드 기간
    COUPON_BIRTHDAY              VARCHAR(2),
    -- 상태 (0: 임시저장, 1:쿠폰발행완료, 9 : 삭제)
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '1',
    -- 총 다운로드 가능수량
    COUPON_DOWNLOAD_LIMIT        INTEGER NOT NULL DEFAULT -1,
    -- 회원별 다운로드 가능 수량
    COUPON_DOWNLOAD_USER_LIMIT   INTEGER NOT NULL DEFAULT -1,
    -- Y : 사용하지 않은 쿠폰이 있어도 여러개 다운로드 가능
    COUPON_MULITPLE_DOWNLOAD_FLAG VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 수정한 관리자 이름
    UPDATE_USER_NAME             VARCHAR(50),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 쿠폰 직접 입력 여부
    DIRECT_INPUT_FLAG            VARCHAR(1) DEFAULT 'N',
    -- 쿠폰 직접 입력 값
    DIRECT_INPUT_VALUE           VARCHAR(100),
    PRIMARY KEY (COUPON_ID)
);

-- 발행된 오프라인 할인쿠폰
CREATE TABLE IF NOT EXISTS OP_COUPON_OFFLINE (
    -- 쿠폰 오프라인 ID
    COUPON_OFFLINE_ID            INTEGER NOT NULL,
    -- 쿠폰 ID
    COUPON_ID                    INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 쿠폰 오프라인 코드
    COUPON_OFFLINE_CODE          VARCHAR(30) NOT NULL,
    -- 쿠폰 사용 유무
    COUPON_USED_FLAG             VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 쿠폰 사용일자
    COUPON_USED_DATE             VARCHAR(14),
    -- 발행일
    PUBLISHED_DATE               VARCHAR(20) NOT NULL,
    PRIMARY KEY (COUPON_OFFLINE_ID)
);

-- 정기발행쿠폰 테이블
CREATE TABLE IF NOT EXISTS OP_COUPON_REGULAR (
    -- 쿠폰 고유ID
    COUPON_ID                    INTEGER NOT NULL,
    -- (WEB, MOBILE, APP)
    COUPON_TYPE                  VARCHAR(255) DEFAULT '',
    -- 쿠폰명
    COUPON_NAME                  VARCHAR(100) NOT NULL,
    -- 쿠폰설명
    COUPON_COMMENT               VARCHAR(500),
    COUPON_ISSUE_TYPE            VARCHAR(1) NOT NULL DEFAULT '0',
    -- 정기쿠폰 발행 기간 시작일
    COUPON_ISSUE_START_DATE      VARCHAR(14),
    -- 정기쿠폰 발행 기간 종료일
    COUPON_ISSUE_END_DATE        VARCHAR(14),
    -- 발행 시점(1:일반, 2:회원가입, 3:생일, 4:상품구매 후 발행, 5:첫구매)
    COUPON_TARGET_TIME_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰대상 (1.전체, 2.선택 회원, 3.회원 등급)
    COUPON_TARGET_USER_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 회원 등급 ID (EX. 1||2||3)
    COUPON_TARGET_USER_LEVEL     VARCHAR(4000),
    -- 선택 회원 JSON STRING
    COUPON_TARGET_USER           TEXT,
    -- 사용가능 상품 판매가 (개당)
    COUPON_PAY_RESTRICTION       INTEGER NOT NULL DEFAULT -1,
    COUPON_CONCURRENTLY          VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰 할인 금액 타입 (1:원, 2:%)
    COUPON_PAY_TYPE              VARCHAR(1) NOT NULL,
    -- 쿠폰 할인 금액
    COUPON_PAY                   INTEGER NOT NULL,
    -- %할인 최대 할인금액
    COUPON_DISCOUNT_LIMIT_PRICE  INTEGER DEFAULT -1,
    -- 쿠폰상품조건 (1.전체상품, 2.특정상품)
    COUPON_TARGET_ITEM_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 특정상품 발행 조건
    COUPON_TARGET_ITEM           TEXT,
    -- 쿠폰 사용 유무
    COUPON_FLAG                  VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 생일 쿠폰 다운로드 기간
    COUPON_BIRTHDAY              VARCHAR(2),
    -- 상태 (0: 임시저장, 1:쿠폰발행완료, 9 : 삭제)
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '0',
    -- 총 다운로드 가능수량
    COUPON_DOWNLOAD_LIMIT        INTEGER NOT NULL DEFAULT -1,
    -- 회원별 다운로드 가능 수량
    COUPON_DOWNLOAD_USER_LIMIT   INTEGER NOT NULL DEFAULT -1,
    -- Y : 사용하지 않은 쿠폰이 있어도 여러개 다운로드 가능
    COUPON_MULITPLE_DOWNLOAD_FLAG VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 수정한 관리자 이름
    UPDATE_USER_NAME             VARCHAR(50),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (COUPON_ID)
);

-- 쿠폰 상품 설정 테이블
CREATE TABLE IF NOT EXISTS OP_COUPON_TARGET_ITEM (
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 쿠폰ID
    COUPON_ID                    INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_ID, COUPON_ID)
);

-- 쿠폰 발행시 발급대상이 선택 회원인 경우에 사용
CREATE TABLE IF NOT EXISTS OP_COUPON_TARGET_USER (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 쿠폰ID
    COUPON_ID                    INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (USER_ID, COUPON_ID)
);

-- 고객 쿠폰 내역
CREATE TABLE IF NOT EXISTS OP_COUPON_USER (
    -- 쿠폰 내역 고유ID
    COUPON_USER_ID               INTEGER NOT NULL,
    -- 쿠폰 고유ID
    COUPON_ID                    INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 쇼핑 채널
    COUPON_TYPE                  VARCHAR(255) NOT NULL,
    -- 쿠폰명
    COUPON_NAME                  VARCHAR(100) NOT NULL,
    -- 쿠폰설명
    COUPON_COMMENT               VARCHAR(500) NOT NULL,
    COUPON_APPLY_TYPE            VARCHAR(1) NOT NULL DEFAULT '0',
    -- 쿠폰 사용 기간 시작일
    COUPON_APPLY_START_DATE      VARCHAR(14),
    -- 쿠폰 사용 기간 종료일
    COUPON_APPLY_END_DATE        VARCHAR(14),
    -- 사용가능 상품 판매가 (개당)
    COUPON_PAY_RESTRICTION       INTEGER NOT NULL DEFAULT -1,
    COUPON_CONCURRENTLY          VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰 할인 금액 타입 (1:원, 2:%)
    COUPON_PAY_TYPE              VARCHAR(1) NOT NULL DEFAULT '1',
    -- 쿠폰 할인 금액
    COUPON_PAY                   INTEGER NOT NULL,
    -- %할인 최대 할인금액
    COUPON_DISCOUNT_LIMIT_PRICE  INTEGER DEFAULT -1,
    -- 쿠폰상품조건 (1.전체상품, 2.특정상품)
    COUPON_TARGET_ITEM_TYPE      VARCHAR(1) NOT NULL DEFAULT '1',
    -- 상태 (0:다운받음, 1:사용완료)
    DATA_STATUS_CODE             VARCHAR(1),
    -- 쿠폰 다운로드일자
    COUPON_DOWNLOAD_DATE         VARCHAR(14) NOT NULL,
    -- 쿠폰 사용일자
    COUPON_USED_DATE             VARCHAR(14),
    -- 주문번호
    ORDER_CODE                   VARCHAR(50),
    -- 주문SEQUENCE
    ORDER_SEQUENCE               INTEGER,
    -- 상품SEQUENCE
    ITEM_SEQUENCE                INTEGER,
    -- 할인금액
    DISCOUNT_AMOUNT              INTEGER,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (COUPON_USER_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_coupon_user_coupon_user_id_seq START WITH 1000;
ALTER TABLE OP_COUPON_USER ALTER COLUMN COUPON_USER_ID SET DEFAULT nextval('op_coupon_user_coupon_user_id_seq');
ALTER SEQUENCE op_coupon_user_coupon_user_id_seq OWNED BY OP_COUPON_USER.COUPON_USER_ID;

-- 배송업체
CREATE TABLE IF NOT EXISTS OP_DELIVERY_COMPANY (
    -- 배송업체 ID
    DELIVERY_COMPANY_ID          INTEGER NOT NULL,
    -- 배송업체명
    DELIVERY_COMPANY_NAME        VARCHAR(30) NOT NULL,
    -- 대표연락처
    TEL_NUMBER                   VARCHAR(20),
    -- 배송조회 URL
    DELIVERY_COMPANY_URL         VARCHAR(255),
    -- 전송방법(1:GET, 2:POST)
    SEND_FLAG                    VARCHAR(1) NOT NULL DEFAULT '1',
    -- 송장번호 파라미터
    DELIVERY_NUMBER_PARAMETER    VARCHAR(255),
    -- 사용유무(Y:사용, N:사용안함)
    USE_FLAG                     VARCHAR(1) NOT NULL DEFAULT 'Y',
    PRIMARY KEY (DELIVERY_COMPANY_ID)
);

-- 주문정보 MASTER
CREATE TABLE IF NOT EXISTS OP_ORDER (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 주문 총액
    ORDER_TOTAL_AMOUNT           INTEGER NOT NULL,
    -- 결제 총액
    PAY_AMOUNT                   INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 회원 LOGIN ID
    LOGIN_ID                     VARCHAR(300),
    -- 구매자 이름
    BUYER_NAME                   VARCHAR(350) NOT NULL,
    -- 전화번호
    PHONE                        VARCHAR(210),
    -- 전화번호
    MOBILE                       VARCHAR(210),
    -- 이메일
    EMAIL                        VARCHAR(420),
    -- 우편번호
    ZIPCODE                      VARCHAR(7),
    -- 새 우편번호
    NEW_ZIPCODE                  VARCHAR(5),
    -- 시도
    SIDO                         VARCHAR(20),
    -- 시군구
    SIGUNGU                      VARCHAR(20),
    -- 읍면동
    EUPMYEONDONG                 VARCHAR(20),
    -- 주소 
    ADDRESS                      VARCHAR(1000),
    -- 상세 주소
    ADDRESS_DETAIL               VARCHAR(1785),
    -- 주문 상태 코드 (1: 정상, 2: 취소)
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL,
    -- 주문자 IP
    IP                           VARCHAR(300),
    -- 환불 은행명
    RETURN_BANK_NAME             VARCHAR(350),
    -- 환불 계좌주
    RETURN_BANK_IN_NAME          VARCHAR(350),
    -- 가상계좌 번호
    RETURN_VIRTUAL_NO            VARCHAR(350),
    -- 주문 관리자 메모
    ORDER_ADMIN_MEMO             VARCHAR(1500),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE)
);

-- 주문별 추가 결제 금액 정보
CREATE TABLE IF NOT EXISTS OP_ORDER_ADD_PAYMENT (
    -- 고유번호
    ADD_PAYMENT_ID               INTEGER NOT NULL,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50),
    -- 주문순번
    ORDER_SEQUENCE               INTEGER,
    -- 환불 이슈번호
    REFUND_CODE                  VARCHAR(50),
    -- 이슈 코드
    ISSUE_CODE                   VARCHAR(50) NOT NULL,
    -- 내용
    SUBJECT                      VARCHAR(255) NOT NULL,
    -- 추가금 타입 (1:추가, 2:할인)
    ADD_PAYMENT_TYPE             INTEGER NOT NULL,
    -- 금액
    AMOUNT                       INTEGER NOT NULL,
    -- 매출 일자
    SALES_DATE                   VARCHAR(14),
    -- 매출 취소 일자
    SALES_CANCEL_DATE            VARCHAR(14),
    -- 정산ID 
    REMITTANCE_ID                BIGINT,
    -- 정산 금액
    REMITTANCE_AMOUNT            INTEGER NOT NULL,
    -- 정산 예정일
    REMITTANCE_EXPECTED_DATE     VARCHAR(8),
    -- 정산일자
    REMITTANCE_DATE              VARCHAR(8),
    -- 정산 상태 (1:대기, 2:재정산, 9 : 완료)
    REMITTANCE_STATUS_CODE       VARCHAR(1) NOT NULL DEFAULT '1',
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ADD_PAYMENT_ID)
);

-- 관리자 대량주문 마스터 
CREATE TABLE IF NOT EXISTS OP_ORDER_ADMIN (
    -- 작업일자
    WORK_DATE                    VARCHAR(8) NOT NULL,
    -- 작업차수
    WORK_SEQUENCE                INTEGER NOT NULL,
    -- 관리자 이름
    INSERT_MANAGER_NAME          VARCHAR(50) NOT NULL,
    -- 상태코드
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (WORK_DATE, WORK_SEQUENCE)
);

CREATE TABLE IF NOT EXISTS OP_ORDER_ADMIN_DETAIL (
    -- 작업 일자
    WORK_DATE                    VARCHAR(8) NOT NULL,
    -- 작업 순번
    WORK_SEQUENCE                INTEGER NOT NULL,
    -- 상품 순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 주문 GROUP_CODE
    ORDER_GROUP_CODE             VARCHAR(10) NOT NULL,
    -- EXCEL TEMPLATE VERSION
    TEMPLATE_VERSION             VARCHAR(8) NOT NULL,
    -- EXCEL DATA (JSON)
    EXCEL_DATA                   VARCHAR NOT NULL,
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '1',
    -- 판매가
    SALE_PRICE                   INTEGER,
    -- 처리자
    UPDATE_MANAGER_NAME          VARCHAR(50),
    -- 처리일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (WORK_DATE, WORK_SEQUENCE, ITEM_SEQUENCE)
);

-- 주문 취소 신청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_CANCEL_APPLY (
    -- 이슈번호
    CLAIM_CODE                   VARCHAR(50) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트상품 순번
    PARENT_ITEM_SEQUENCE         INTEGER,
    -- 취소 요청일
    CANCEL_APPLY_DATE            VARCHAR(8) NOT NULL,
    -- 환불 신청 번호
    REFUND_CODE                  VARCHAR(50) DEFAULT '',
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 클레임 등록 주체 (01:구매자, 02 : 판매자)
    CLAIM_APPLY_SUBJECT          VARCHAR(2) NOT NULL,
    -- 수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 클레임 사유 코드
    CANCEL_REASON                VARCHAR(1) NOT NULL,
    -- 클레임 사유 TEXT
    CANCEL_REASON_TEXT           VARCHAR(255) NOT NULL,
    -- 클레임 사유 상세
    CANCEL_REASON_DETAIL         VARCHAR(255) NOT NULL,
    -- 메모
    CANCEL_MEMO                  TEXT,
    -- 취소 거부 사유
    CANCEL_REFUSAL_REASON_TEXT   VARCHAR(255),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE)
);

CREATE TABLE IF NOT EXISTS OP_ORDER_CANCEL_FAIL (
    ID                           BIGINT NOT NULL,
    APPROVAL_TYPE                VARCHAR(50) NOT NULL,
    CANCEL_AMOUNT                INTEGER NOT NULL,
    CANCEL_REASON                VARCHAR(255) NOT NULL,
    CANCEL_REQUESTER             VARCHAR(1) NOT NULL,
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    PAY_DATE                     VARCHAR(14) NOT NULL,
    PG_KEY                       VARCHAR(100) NOT NULL,
    PG_SERVICE_TYPE              VARCHAR(50) NOT NULL,
    TAX_AMOUNT                   INTEGER NOT NULL,
    TAX_FREE_AMOUNT              INTEGER NOT NULL,
    PRIMARY KEY (ID)
);

-- 입점사별 환불/취소 배송비 정보 - 사용안할 예정
CREATE TABLE IF NOT EXISTS OP_ORDER_CLAIM_SHIPPING (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL DEFAULT '',
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER NOT NULL DEFAULT 0,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 환불신청 번호
    REFUND_CODE                  VARCHAR(50) NOT NULL DEFAULT '',
    -- 금액
    AMOUNT                       INTEGER NOT NULL,
    -- 정산금액
    REMITTANCE_AMOUNT            INTEGER NOT NULL,
    -- 처리자
    MANAGER_USER_NAME            VARCHAR(50) NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(50) NOT NULL,
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, SELLER_ID, REFUND_CODE)
);

-- 변경 예정 - 교환 요청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_EXCHANGE_APPLY (
    -- 이슈번호
    CLAIM_CODE                   VARCHAR(50) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트상품순번
    PARENT_ITEM_SEQUENCE         INTEGER,
    -- 교환 요청일
    EXCHANGE_APPLY_DATE          VARCHAR(8) NOT NULL,
    -- 요청수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 판매자ID
    SHIPMENT_RETURN_SELLER_ID    BIGINT NOT NULL DEFAULT 0,
    -- SHIPMENT_RETURN_ID
    SHIPMENT_RETURN_ID           INTEGER NOT NULL,
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 10:회수중, 11:회수완료, 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 클레임 등록 주체 (01:구매자, 02 : 판매자)
    CLAIM_APPLY_SUBJECT          VARCHAR(2) NOT NULL,
    -- 교환 받는사람 이름
    EXCHANGE_RECEIVE_NAME        VARCHAR(210) NOT NULL,
    -- 교환 받는사람 전화번호
    EXCHANGE_RECEIVE_PHONE       VARCHAR(210),
    -- 교환 받는사람 휴대폰
    EXCHANGE_RECEIVE_MOBILE      VARCHAR(210) NOT NULL,
    -- 교환 받는사람 우편번호
    EXCHANGE_RECEIVE_ZIPCODE     VARCHAR(7),
    EXCHANGE_RECEIVE_SIDO        VARCHAR(20),
    EXCHANGE_RECEIVE_SIGUNGU     VARCHAR(20),
    EXCHANGE_RECEIVE_EUPMYEONDONG VARCHAR(20),
    -- 교환 받는사람 주소 
    EXCHANGE_RECEIVE_ADDRESS     VARCHAR(1000),
    -- 교환 받는사람 상세주소
    EXCHANGE_RECEIVE_ADDRESS2    VARCHAR(1785),
    -- 교환 배송 회사 ID
    EXCHANGE_DELIVERY_COMPANY_ID INTEGER,
    -- 교환 배송 회사 이름
    EXCHANGE_DELIVERY_COMPANY_NAME VARCHAR(50),
    -- 교환 배송 송장번호
    EXCHANGE_DELIVERY_NUMBER     VARCHAR(50),
    -- 교환 배송추적 URL
    EXCHANGE_DELIVERY_COMPANY_URL VARCHAR(255),
    -- 교환 배송 시작일
    EXCHANGE_DELIVERY_DATE       VARCHAR(14),
    -- EXCHANGE_SHIPPING_ASK_TYPE
    EXCHANGE_SHIPPING_ASK_TYPE   VARCHAR(1) NOT NULL,
    -- 교환 고객발송 송장 번호
    EXCHANGE_SHIPPING_NUMBER     VARCHAR(30),
    -- 교환 고객발송 택배사명
    EXCHANGE_SHIPPING_COMPANY_NAME VARCHAR(30),
    -- 교환 고객발송 배송추적 URL
    EXCHANGE_SHIPPING_COMPANY_URL VARCHAR(255),
    -- 교환 고객발송 시작일
    EXCHANGE_SHIPPING_START_DATE VARCHAR(8),
    -- 교환 원인 재공? (1:판매자, 2:고객)
    EXCHANGE_REASON              VARCHAR(1) NOT NULL,
    -- 교환 사유
    EXCHANGE_REASON_TEXT         VARCHAR(255) NOT NULL,
    -- 교환 사유 상세
    EXCHANGE_REASON_DETAIL       VARCHAR(255) NOT NULL,
    -- 교환 관련 메모
    EXCHANGE_MEMO                TEXT,
    -- 교환 거절 사유 (사용자 공유)
    EXCHANGE_REFUSAL_REASON_TEXT VARCHAR(255),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CLAIM_CODE)
);

CREATE TABLE IF NOT EXISTS OP_ORDER_GIFT_ITEM (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    GIFT_GROUP_ID                BIGINT NOT NULL,
    GIFT_ITEM_CODE               VARCHAR(30) NOT NULL,
    GIFT_ITEM_ID                 BIGINT NOT NULL,
    GIFT_ITEM_NAME               VARCHAR(30) NOT NULL,
    GIFT_ORDER_STATUS            VARCHAR(15) NOT NULL,
    GIFT_SEQUENCE                INTEGER NOT NULL,
    GROUP_TYPE                   VARCHAR(15) NOT NULL,
    IMAGE                        VARCHAR(255),
    ITEM_SEQUENCE                INTEGER NOT NULL,
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE               INTEGER NOT NULL,
    PRICE                        INTEGER NOT NULL,
    SELLER_ID                    BIGINT NOT NULL,
    VALID_END_DATE               TIMESTAMP,
    VALID_START_DATE             TIMESTAMP,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_ORDER_GIFT_ITEM_LOG (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    GIFT_GROUP_ID                BIGINT NOT NULL,
    GIFT_ITEM_CODE               VARCHAR(30) NOT NULL,
    GIFT_ITEM_ID                 BIGINT NOT NULL,
    GIFT_ITEM_NAME               VARCHAR(30) NOT NULL,
    GIFT_ORDER_STATUS            VARCHAR(15) NOT NULL,
    GIFT_SEQUENCE                INTEGER NOT NULL,
    GROUP_TYPE                   VARCHAR(15) NOT NULL,
    ITEM_SEQUENCE                INTEGER NOT NULL,
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE               INTEGER NOT NULL,
    ORG_GIFT_ORDER_STATUS        VARCHAR(15) NOT NULL,
    USER_TYPE                    VARCHAR(10),
    PRIMARY KEY (ID)
);

-- 주문 상품 테이블 - 금액 정보는 개당 가격만 저장
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품 순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 상태코드
    ORDER_STATUS                 VARCHAR(2) NOT NULL,
    -- 배송 정책 순번
    SHIPPING_SEQUENCE            INTEGER NOT NULL,
    -- 배송지 정보 ID
    SHIPPING_INFO_SEQUENCE       INTEGER NOT NULL,
    -- 주문 경로
    DEVICE_TYPE                  VARCHAR(15) NOT NULL
);

-- 주문을 시도하는 상품들을 임시저장함
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_BUY_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL,
    -- 세션 ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 상품 진열 순서
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 배송정책 순번
    SHIPPING_INDEX               INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 필수 옵션 (JSON)
    OPTIONS                      TEXT,
    -- 일반 상품 쿠폰
    COUPON_USER_ID               INTEGER DEFAULT 0,
    -- 회원ID
    ADD_COUPON_USER_ID           BIGINT NOT NULL DEFAULT 0,
    -- 배송비 지불 방법(1:선불, 2:착불)
    SHIPPING_PAYMENT_TYPE        VARCHAR(1) NOT NULL,
    -- 추가 구성품 여부
    ADDITION_ITEM_FLAG           VARCHAR(1) NOT NULL,
    -- 추가 구성품 부모 SEQUENCE
    PARENT_ITEM_SEQUENCE         INTEGER NOT NULL,
    -- 추가 구성품 부모 상품 아이디
    PARENT_ITEM_ID               INTEGER NOT NULL,
    -- 에스크로 사용여부
    ESCROW_STATUS                VARCHAR(2) NOT NULL DEFAULT 'N',
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 캠페인 ID
    CAMPAIGN_CODE                VARCHAR(50),
    -- 세트상품 여부
    SET_ITEM_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    PRIMARY KEY (USER_ID, SESSION_ID, ORDER_CODE, ITEM_SEQUENCE)
);

-- 주문상품_HOLD
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_HOLD (
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE               INTEGER NOT NULL,
    ITEM_SEQUENCE                INTEGER NOT NULL,
    ORDER_STATUS                 VARCHAR(2) NOT NULL,
    SHIPPING_SEQUENCE            INTEGER NOT NULL,
    SHIPPING_INFO_SEQUENCE       INTEGER NOT NULL,
    DEVICE_TYPE                  VARCHAR(15) NOT NULL,
    ADDITION_ITEM_FLAG           VARCHAR(1) NOT NULL DEFAULT 'N',
    PARENT_ITEM_SEQUENCE         INTEGER NOT NULL,
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    GUEST_FLAG                   VARCHAR(1) NOT NULL,
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    CATEGORY_TEAM_ID             INTEGER,
    CATEGORY_GROUP_ID            INTEGER,
    CATEGORY_ID                  INTEGER,
    SHIPMENT_ID                  INTEGER NOT NULL DEFAULT 0,
    SHIPMENT_RETURN_ID           INTEGER NOT NULL DEFAULT 0,
    COUPON_USER_ID               INTEGER,
    ADD_COUPON_USER_ID           BIGINT NOT NULL DEFAULT 0,
    ITEM_ID                      INTEGER,
    ITEM_CODE                    VARCHAR(30),
    ITEM_USER_CODE               VARCHAR(30) NOT NULL,
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    FREE_GIFT_NAME               VARCHAR(255),
    IMAGE_SRC                    VARCHAR(255),
    SHIPMENT_GROUP_CODE          VARCHAR(50),
    SHIPPING_RETURN              INTEGER NOT NULL,
    PURCHASE_PRICE               INTEGER NOT NULL,
    COST_PRICE                   INTEGER NOT NULL,
    PRICE                        INTEGER NOT NULL,
    OPTION_PRICE                 INTEGER NOT NULL,
    SALE_PRICE                   INTEGER NOT NULL,
    QUANTITY                     INTEGER NOT NULL DEFAULT 0,
    CLAIM_QUANTITY               INTEGER DEFAULT 0,
    ORDER_QUANTITY               INTEGER NOT NULL DEFAULT 0,
    COUPON_DISCOUNT_PRICE        INTEGER DEFAULT 0,
    SPOT_SALE_FLAG               VARCHAR(1) DEFAULT 'N',
    SPOT_TYPE                    VARCHAR(1),
    SPOT_DISCOUNT_PRICE          INTEGER,
    LEVEL_ID                     INTEGER,
    LEVEL_NAME                   VARCHAR(50),
    USER_LEVEL_DISCOUNT_RATE     DOUBLE PRECISION,
    USER_LEVEL_DISCOUNT_PRICE    INTEGER,
    ESCROW_STATUS                VARCHAR(2) NOT NULL DEFAULT 'N',
    TAX_TYPE                     VARCHAR(1) NOT NULL,
    COMMISSION_BASE_PRICE        INTEGER NOT NULL,
    COMMISSION_RATE              DOUBLE PRECISION NOT NULL,
    COMMISSION_PRICE             INTEGER NOT NULL,
    COMMISSION_TYPE              VARCHAR(1) DEFAULT '1',
    SUPPLY_PRICE                 INTEGER NOT NULL,
    REMITTANCE_ID                BIGINT,
    REMITTANCE_TYPE              VARCHAR(1),
    REMITTANCE_DAY               VARCHAR(2),
    REMITTANCE_EXPECTED_DATE     VARCHAR(8),
    REMITTANCE_DATE              VARCHAR(8),
    REMITTANCE_STATUS_CODE       VARCHAR(1) DEFAULT '1',
    ADMIN_DISCOUNT_PRICE         INTEGER,
    ADMIN_DISCOUNT_DETAIL        TEXT,
    SELLER_DISCOUNT_PRICE        INTEGER,
    SELLER_DISCOUNT_DETAIL       TEXT,
    BRAND                        VARCHAR(50),
    OPTIONS                      TEXT,
    DELIVERY_TYPE                VARCHAR(1) NOT NULL,
    DELIVERY_COMPANY_ID          INTEGER,
    DELIVERY_COMPANY_NAME        VARCHAR(50),
    DELIVERY_NUMBER              VARCHAR(100),
    DELIVERY_COMPANY_URL         VARCHAR(255),
    SHIPMENT_RETURN_TYPE         VARCHAR(1) NOT NULL,
    POINT_TYPE                   VARCHAR(1),
    POINT_CONFIG_TYPE            VARCHAR(1) DEFAULT '1',
    POINT                        INTEGER DEFAULT 0,
    POINT_LOG                    VARCHAR(255),
    EARN_POINT                   INTEGER,
    SELLER_POINT                 INTEGER NOT NULL,
    EARN_POINT_FLAG              VARCHAR(1) DEFAULT 'N',
    RETURN_POINT_FLAG            VARCHAR(1) DEFAULT 'Y',
    CANCEL_FLAG                  VARCHAR(1) DEFAULT 'N',
    REFUND_STATUS                VARCHAR(1) DEFAULT '0',
    ITEM_RETURN_FLAG             VARCHAR(1) DEFAULT 'Y',
    REVENUE_SALES_STATUS         VARCHAR(2) NOT NULL DEFAULT '',
    PAY_DATE                     VARCHAR(14) DEFAULT '',
    SHIPPING_READY_DATE          VARCHAR(14) DEFAULT '',
    SHIPPING_DATE                VARCHAR(14) DEFAULT '',
    SHIPPING_FINISH_DATE         VARCHAR(14) DEFAULT '',
    CANCEL_REQUEST_DATE          VARCHAR(14),
    CANCEL_REQUEST_FINISH_DATE   VARCHAR(14),
    SALES_DATE                   VARCHAR(14),
    SALES_CANCEL_DATE            VARCHAR(14),
    CONFIRM_DATE                 VARCHAR(14) DEFAULT '',
    RETURN_REQUEST_DATE          VARCHAR(14),
    RETURN_REQUEST_FINISH_DATE   VARCHAR(14),
    EXCHANGE_REQUEST_DATE        VARCHAR(14),
    UPDATED_ADMIN_USER_NAME      VARCHAR(300),
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    CAMPAIGN_CODE                VARCHAR(50),
    SET_ITEM_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    SET_DISCOUNT_TYPE            VARCHAR(1),
    SET_DISCOUNT_PRICE           INTEGER DEFAULT 0,
    LOCGOV_CODE                  VARCHAR(10),
    MOBILE_NUMBER                VARCHAR(100),
    ETC_AMT                      INTEGER DEFAULT 0,
    FILLER1                      VARCHAR(300),
    FILLER2                      VARCHAR(300),
    FILLER3                      VARCHAR(300),
    FILLER4                      VARCHAR(300),
    FILLER5                      VARCHAR(300),
    FILLER6                      VARCHAR(300),
    FILLER7                      VARCHAR(300),
    FILLER8                      VARCHAR(300),
    FILLER9                      VARCHAR(300),
    FILLER10                     VARCHAR(300),
    -- HOLD 확인상태 (N : 미확인상태초기값, S: SELLER 확인상태, M: MANAGER 승인상태 , R: MANAGER 승인반려 상태)
    HOLD_CONFIRM_STATUS          VARCHAR(1) DEFAULT 'N',
    -- 판매자 확인일시
    HOLD_CONFIRM_DATE_SELLER     VARCHAR(14),
    -- 관리자 승인일시
    HOLD_CONFIRM_DATE_MGR        VARCHAR(14),
    -- 관리자 반려일시
    HOLD_REJECT_DATE_MGR         VARCHAR(14),
    -- 확인 판매자 아이디
    HOLID_CONFIRM_SELLER_USER_ID BIGINT,
    -- 승인 관리자 아이디
    HOLID_CONFIRM_MGR_ID         BIGINT,
    -- 반려 관리자 아이디
    HOLID_REJECT_MGR_ID          BIGINT,
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE)
);

-- 주문상품_HOLD_변경이력
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_HOLD_HIST (
    -- 등록순번
    REG_SEQ                      BIGINT NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품 순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 변경전 HOLD_확인상태 (N : 미확인상태(초기값), S: SELLER 확인상태, M: MANAGER 승인상태 , R: MANAGER 승인반려 상태)
    HOLD_CONFRIM_STATUS_BEF      VARCHAR(1) NOT NULL,
    -- 변경후 HOLD_확인상태 (N : 미확인상태(초기값), S: SELLER 확인상태, M: MANAGER 승인상태 , R: MANAGER 승인반려 상태)
    HOLD_CONFRIM_STATUS_AFT      VARCHAR(1) NOT NULL,
    -- 생성자 회원구분(S: SELLER, M: MANAGER)
    CREATED_USER_TP              VARCHAR(1) NOT NULL,
    -- 생성자 사용자 아이디 (판매자인경우 SELLER_USER_ID, 관라지 인 경우 OP_MANAGER USER_ID)
    CREATED_USER_ID              BIGINT NOT NULL,
    -- 생성일시
    CREATED_DATE                 VARCHAR(14) DEFAULT 'TO_CHAR(SYS_DATETIME,',
    PRIMARY KEY (REG_SEQ)
);
CREATE SEQUENCE IF NOT EXISTS op_order_item_hold_hist_reg_seq_seq START WITH 1000;
ALTER TABLE OP_ORDER_ITEM_HOLD_HIST ALTER COLUMN REG_SEQ SET DEFAULT nextval('op_order_item_hold_hist_reg_seq_seq');
ALTER SEQUENCE op_order_item_hold_hist_reg_seq_seq OWNED BY OP_ORDER_ITEM_HOLD_HIST.REG_SEQ;

-- 주문 세트상품 테이블 - 금액 정보는 개당 가격만 저장
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문 순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품 순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 비회원여부(Y:비회원)
    GUEST_FLAG                   VARCHAR(1) NOT NULL,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER,
    -- 상품 코드
    ITEM_CODE                    VARCHAR(30),
    -- 사용자 노출 상품 코드
    ITEM_USER_CODE               VARCHAR(30) NOT NULL,
    -- 상품명
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    -- 이미지 경로
    IMAGE_SRC                    VARCHAR(255),
    -- 매입가
    PURCHASE_PRICE               INTEGER NOT NULL,
    -- 원가
    COST_PRICE                   INTEGER NOT NULL,
    -- 단가(개당가격)
    PRICE                        INTEGER NOT NULL,
    -- 옵션가(개당가격)
    OPTION_PRICE                 INTEGER NOT NULL,
    -- 개당 판매가
    SALE_PRICE                   INTEGER NOT NULL,
    -- 상품 구매 수량
    QUANTITY                     INTEGER NOT NULL DEFAULT 0,
    CLAIM_QUANTITY               INTEGER NOT NULL DEFAULT 0,
    -- 주문수량
    ORDER_QUANTITY               INTEGER NOT NULL DEFAULT 0,
    -- 스팟 세일 여부 (Y: 세일가)
    SPOT_SALE_FLAG               VARCHAR(1) DEFAULT 'N',
    -- 스팟할인 부담 (1:운영사, 2:판매자)
    SPOT_TYPE                    VARCHAR(1),
    -- 스팟 할인 금액 (개당가격)
    SPOT_DISCOUNT_PRICE          INTEGER,
    -- 부과세면세여부 (1:과세, 2:면세)
    TAX_TYPE                     VARCHAR(1) NOT NULL,
    -- 수수료 기준 금액
    COMMISSION_BASE_PRICE        INTEGER NOT NULL,
    -- 수수료율
    COMMISSION_RATE              DOUBLE PRECISION NOT NULL,
    -- 수수료
    COMMISSION_PRICE             INTEGER NOT NULL,
    -- 수수료설정 (1:입점업체 수수료로 설정, 2: 상품별 수수료로 설정, 3: 공급가 설정)
    COMMISSION_TYPE              VARCHAR(1) DEFAULT '1',
    -- 공급가
    SUPPLY_PRICE                 INTEGER NOT NULL,
    -- 운영사 부담의 할인 (개당가격)
    ADMIN_DISCOUNT_PRICE         INTEGER,
    -- 운영사 부담의 할인 상세
    ADMIN_DISCOUNT_DETAIL        TEXT,
    -- 판매자 부담의 할인 (개당가격)
    SELLER_DISCOUNT_PRICE        INTEGER,
    -- 판매자 부담의 할인 상세
    SELLER_DISCOUNT_DETAIL       TEXT,
    -- 브랜드
    BRAND                        VARCHAR(50),
    -- 상품 구성 옵션(JSON TYPE)
    OPTIONS                      TEXT,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE, SET_ITEM_SEQUENCE)
);

-- 주문을 시도하는 세트상품들을 임시저장함
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET_BUY_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 세션ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50),
    -- 상품 진열 순서
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 필수 옵션 (JSON)
    OPTIONS                      TEXT,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 생성일
    CREATED_DATE                 VARCHAR(14)
);

-- 주문을 시도하는 세트상품의 상품들을 임시저장함
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 세션ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 상품 진열 순서
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 필수 옵션 (JSON)
    OPTIONS                      TEXT,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    TEXT_OPTION                  VARCHAR(4000)
);

-- 주문을 시도하는 상품들을 임시저장함
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 세션 ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 상품 진열 순서
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 수량
    QUANTITY                     INTEGER NOT NULL,
    -- 필수 옵션 (JSON)
    OPTIONS                      TEXT,
    -- 일반 상품 쿠폰
    COUPON_USER_ID               INTEGER DEFAULT 0,
    -- 회원ID
    ADD_COUPON_USER_ID           BIGINT NOT NULL DEFAULT 0,
    -- 배송비 지불 방법(1:선불, 2:착불)
    SHIPPING_PAYMENT_TYPE        VARCHAR(1) NOT NULL,
    -- 추가 구성품 여부
    ADDITION_ITEM_FLAG           VARCHAR(1) NOT NULL,
    -- 추가 구성품 부모 상품 SEQUENCE
    PARENT_ITEM_SEQUENCE         INTEGER NOT NULL,
    -- 추가 구성품 부모 상품 ID
    PARENT_ITEM_ID               INTEGER,
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 캠페인 ID
    CAMPAIGN_CODE                VARCHAR(50),
    -- 세트상품 여부
    SET_ITEM_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    TEXT_OPTION                  VARCHAR(4000),
    PRIMARY KEY (USER_ID, SESSION_ID, ITEM_SEQUENCE, CREATED_DATE)
);

-- 주문 로그
CREATE TABLE IF NOT EXISTS OP_ORDER_LOG (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED_AT                   TIMESTAMP,
    -- 수정일시
    UPDATED_AT                   TIMESTAMP,
    -- 등록일시
    CREATED_AT_TEXT              VARCHAR(255),
    -- 등록 사용자 정보
    CREATED_BY                   VARCHAR(500),
    -- 접속IP
    IP                           VARCHAR(300),
    -- 상품명
    ITEM_NAME                    VARCHAR(255),
    -- 상품순번
    ITEM_SEQUENCE                INTEGER,
    -- 로그거래타입
    LOG_TYPE                     VARCHAR(30) NOT NULL,
    -- 주문코드
    ORDER_CODE                   VARCHAR(50),
    -- 주문순번
    ORDER_SEQUENCE               INTEGER,
    -- 주문상태
    ORDER_STATUS                 VARCHAR(20),
    -- (전)주문상태
    ORG_ORDER_STATUS             VARCHAR(20),
    -- 수정일시
    UPDATED_AT_TEXT              VARCHAR(255),
    -- 수정 사용자 정보
    UPDATED_BY                   VARCHAR(500),
    -- 사용자구분
    USER_TYPE                    VARCHAR(10),
    PRIMARY KEY (ID)
);

-- 상품 결제정보 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_PAYMENT (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 결제 순번
    PAYMENT_SEQUENCE             INTEGER NOT NULL,
    -- 결제 구분 (1:결제, 2:취소)
    PAYMENT_TYPE                 VARCHAR(1) NOT NULL,
    -- PG 정보 ID
    ORDER_PG_DATA_ID             INTEGER NOT NULL DEFAULT 0,
    -- 결제 수단
    APPROVAL_TYPE                VARCHAR(50) NOT NULL,
    -- 주문 경로
    DEVICE_TYPE                  VARCHAR(14) NOT NULL,
    -- 신용카드 간편결제 수단
    CARD_EASY_TYPE               VARCHAR(50),
    -- 계좌번호
    BANK_VIRTUAL_NO              VARCHAR(255),
    -- 입금예정자 이름
    BANK_IN_NAME                 VARCHAR(700),
    -- 입금예정일(만료일)
    BANK_DATE                    VARCHAR(14),
    -- 금액
    AMOUNT                       INTEGER NOT NULL,
    -- 부과세 면세 금액
    TAX_FREE_AMOUNT              INTEGER NOT NULL,
    -- 취소 금액
    CANCEL_AMOUNT                INTEGER NOT NULL DEFAULT 0,
    -- 잔여액
    REMAINING_AMOUNT             INTEGER NOT NULL DEFAULT 0,
    -- 결제일
    PAY_DATE                     VARCHAR(14),
    -- 즉시 결제 여부
    NOW_PAYMENT_FLAG             VARCHAR(1) NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 환불 구분(Y: 환불 시 결제 부분 취소가 아닌 은행으로 직접 환불 처리를 경우) 
    REFUND_FLAG                  VARCHAR(1),
    -- 결제 정보 요약
    PAYMENT_SUMMARY              VARCHAR(1785),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, PAYMENT_SEQUENCE)
);

-- 결제정보 임시저장 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_PAYMENT_BUY_TEMP (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 결제 타입
    APPROVAL_TYPE                VARCHAR(50) NOT NULL,
    -- SERVICE 구분
    SERVICE_TYPE                 VARCHAR(50) NOT NULL,
    -- 금액
    AMOUNT                       INTEGER NOT NULL,
    -- 면세 금액
    TAX_FREE_AMOUNT              INTEGER NOT NULL,
    -- 계좌번호
    BANK_VIRTUAL_NO              VARCHAR(255),
    -- 입금예정자 이름
    BANK_IN_NAME                 VARCHAR(100),
    -- 입금 예정일 (만료일)
    BANK_DATE                    VARCHAR(14),
    -- PG MID
    SERVICE_MID                  VARCHAR(100),
    -- PG KEY
    SERVICE_KEY                  VARCHAR(100),
    -- 등록일
    CREATED_DATE                 VARCHAR(100),
    PRIMARY KEY (ORDER_CODE)
);

-- PG 결제 정보 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_PG_DATA (
    -- 결제정보ID
    ORDER_PG_DATA_ID             INTEGER NOT NULL DEFAULT 0,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL DEFAULT '0',
    -- PG Service Type
    PG_SERVICE_TYPE              VARCHAR(50) NOT NULL,
    -- PG Service Mid
    PG_SERVICE_MID               VARCHAR(100) NOT NULL,
    -- PG Service Key
    PG_SERVICE_KEY               VARCHAR(100) NOT NULL,
    -- PG 결제 타입
    PG_PAYMENT_TYPE              VARCHAR(100) NOT NULL,
    -- PG 거래 번호
    PG_KEY                       VARCHAR(100) NOT NULL,
    -- PG 응담 코드
    PG_AUTH_CODE                 VARCHAR(100),
    -- PG 전달 데이터
    PG_PROC_INFO                 TEXT NOT NULL,
    -- 부분취소 가능여부 (Y:가능, N:불가능)
    PART_CANCEL_FLAG             VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 부분취소 불가능 사유
    PART_CANCEL_DETAIL           VARCHAR(500),
    -- 결제 금액
    PG_AMOUNT                    INTEGER NOT NULL,
    -- 최초 결제 일자
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (ORDER_PG_DATA_ID)
);

-- 비용 환불 정보
CREATE TABLE IF NOT EXISTS OP_ORDER_REFUND (
    -- 이슈번호
    REFUND_CODE                  VARCHAR(50) NOT NULL,
    -- 등록일
    REFUND_DATE                  VARCHAR(8) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상태 (1 : 신청, 2 : 처리 완료)
    REFUND_STATUS_CODE           VARCHAR(1) NOT NULL,
    -- 신청자
    REQUEST_MANAGER_USER_NAME    VARCHAR(350),
    -- 처리자
    PROCESS_MANAGER_USER_NAME    VARCHAR(350),
    -- 환불 은행명
    RETURN_BANK_NAME             VARCHAR(350),
    -- 환불 계좌번호
    RETURN_VIRTUAL_NO            VARCHAR(350),
    -- 환불 계좌주
    RETURN_BANK_IN_NAME          VARCHAR(350),
    -- 신청일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (REFUND_CODE)
);

-- 반품 신청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_RETURN_APPLY (
    -- 이슈번호
    CLAIM_CODE                   VARCHAR(50) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트상품 순번
    PARENT_ITEM_SEQUENCE         INTEGER,
    -- 환불 요청일
    RETURN_APPLY_DATE            VARCHAR(8) NOT NULL,
    -- 환불 신청 번호
    REFUND_CODE                  VARCHAR(50),
    -- 판매자ID
    SHIPMENT_RETURN_SELLER_ID    BIGINT NOT NULL DEFAULT 0,
    -- 반송지 ID
    SHIPMENT_RETURN_ID           INTEGER NOT NULL,
    -- 신청수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 10:회수중, 11:회수완료, 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 반품 원인 재공? (1:판매자, 2:고객)
    RETURN_REASON                VARCHAR(1) NOT NULL,
    -- 반품 사유
    RETURN_REASON_TEXT           VARCHAR(255) NOT NULL,
    -- 반품 사유 상세
    RETURN_REASON_DETAIL         VARCHAR(255) NOT NULL,
    -- 클레임 등록 주체 (01:구매자, 02 : 판매자)
    CLAIM_APPLY_SUBJECT          VARCHAR(2) NOT NULL,
    -- 반품 신청 이전 주문상태 - 환불 취소 신청용
    PREVIOUS_ORDER_STATUS        VARCHAR(2),
    -- 반품 보내는사람 이름
    RETURN_RESERVE_NAME          VARCHAR(210) NOT NULL,
    -- 반품 보내는사람 전화번호
    RETURN_RESERVE_PHONE         VARCHAR(210),
    -- 반품 보내는사람 휴대폰
    RETURN_RESERVE_MOBILE        VARCHAR(210) NOT NULL,
    -- 반품 보내는사람 우편번호
    RETURN_RESERVE_ZIPCODE       VARCHAR(10),
    RETURN_RESERVE_SIDO          VARCHAR(20),
    RETURN_RESERVE_SIGUNGU       VARCHAR(20),
    RETURN_RESERVE_EUPMYEONDONG  VARCHAR(20),
    -- 반품 보내는사람 주소
    RETURN_RESERVE_ADDRESS       VARCHAR(1000),
    -- 반품 보내는사람 상세주소
    RETURN_RESERVE_ADDRESS2      VARCHAR(1785),
    -- 반품 배송 타입
    RETURN_SHIPPING_ASK_TYPE     VARCHAR(1) NOT NULL,
    -- 반품 고객발송 송장 번호
    RETURN_SHIPPING_NUMBER       VARCHAR(30),
    -- 반품 고객발송 택배사명
    RETURN_SHIPPING_COMPANY_NAME VARCHAR(30),
    -- 반품 고객발송 배송추적 URL
    RETURN_SHIPPING_COMPANY_URL  VARCHAR(255),
    -- 회수 배송비
    COLLECTION_SHIPPING_AMOUNT   INTEGER,
    -- 반품 관련 메모
    RETURN_MEMO                  TEXT,
    -- 반품거절사유 - 구매자 노출용
    RETURN_REFUSAL_REASON_TEXT   VARCHAR(255),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CLAIM_CODE)
);

-- 회원별 이전 매출액
CREATE TABLE IF NOT EXISTS OP_ORDER_SALES (
    -- 회원 PK
    USER_ID                      BIGINT,
    -- 매출일
    PAY_DATE                     VARCHAR(8),
    -- 매출액
    AMOUNT                       INTEGER DEFAULT 0
);

-- SMS 중복 발송 방지용 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_SEND_MESSAGE_LOG (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 구분코드
    TEMPLATE_ID                  VARCHAR(50) NOT NULL,
    -- 송장번호
    DELIVERY_NUMBER              VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL
);

-- 세트주문 취소 신청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_CANCEL_APPLY (
    -- 클레임 코드
    CLAIM_CODE                   VARCHAR(20) NOT NULL DEFAULT '',
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 세트 부모 상품 클레임 코드
    SET_CLAIM_CODE               VARCHAR(50) NOT NULL,
    -- 취소 요청일
    CANCEL_APPLY_DATE            VARCHAR(8) NOT NULL,
    -- 환불 신청 번호
    REFUND_CODE                  VARCHAR(50) DEFAULT '',
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CLAIM_CODE)
);

-- 세트주문 교환요청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_EXCHANGE_APPLY (
    -- 클레임 코드
    CLAIM_CODE                   VARCHAR(20) NOT NULL DEFAULT '',
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 세트 부모 상품 클레임 코드
    SET_CLAIM_CODE               VARCHAR(50) NOT NULL,
    -- 교환 요청일
    EXCHANGE_APPLY_DATE          VARCHAR(8) NOT NULL,
    -- 요청수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 10:회수중, 11:회수완료, 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE)
);

-- 세트반품 신청 내역
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_RETURN_APPLY (
    -- 클레임 코드
    CLAIM_CODE                   VARCHAR(20) NOT NULL DEFAULT '',
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER NOT NULL,
    -- 세트 부모 상품 순번
    SET_ITEM_SEQUENCE            INTEGER NOT NULL,
    -- 세트 부모 상품 클레임 코드
    SET_CLAIM_CODE               VARCHAR(50) NOT NULL,
    -- 환불 요청일
    RETURN_APPLY_DATE            VARCHAR(8) NOT NULL,
    -- 환불 신청 번호
    REFUND_CODE                  VARCHAR(50),
    -- 신청수량
    CLAIM_APPLY_QUANTITY         INTEGER NOT NULL,
    -- 클래임 상태 (01:신청, 02:보류(처리중), 03:승인(환불대기), 10:회수중, 11:회수완료, 04:완료(환불완료), 99:거절)
    CLAIM_STATUS                 VARCHAR(2) NOT NULL,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE)
);

-- 주문 배송정책 정보
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL,
    -- 배송정책 순번
    SHIPPING_SEQUENCE            INTEGER NOT NULL,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 배송비 구분 (1: 무료배송, 2: 판매자조건부, 3:출고지조건부, 4:상품조건부, 5:개당배송비, 6:고정배송비)
    SHIPPING_TYPE                VARCHAR(1) NOT NULL,
    -- 배송정책 묶음배송 기준 코드
    SHIPMENT_GROUP_CODE          VARCHAR(50),
    -- 묶음기준 코드
    SHIPPING_GROUP_CODE          VARCHAR(20),
    ISLAND_TYPE                  VARCHAR(20),
    -- 박스당 제한수량
    SHIPPING_ITEM_COUNT          INTEGER NOT NULL,
    -- 배송비
    SHIPPING                     INTEGER NOT NULL,
    -- 제주도 추가배송비
    SHIPPING_EXTRA_CHARGE1       INTEGER NOT NULL,
    -- 도서산간 추가배송비
    SHIPPING_EXTRA_CHARGE2       INTEGER NOT NULL,
    -- 조건부 무료배송 금액
    SHIPPING_FREE_AMOUNT         INTEGER NOT NULL,
    -- 사용자가 부담해야되는 배송비
    REAL_SHIPPING                INTEGER NOT NULL,
    -- 사용자가 부담해야되는 배송비(변경 전)
    PREVIOUS_REAL_SHIPPING       INTEGER,
    -- 배송비쿠폰으로 할인된 배송비 금액
    DISCOUNT_SHIPPING            INTEGER,
    -- 사용한 배송비 쿠폰수량
    SHIPPING_COUPON_COUNT        INTEGER,
    -- 배송지 지불 방법 (1 : 선불, 2 : 착불)
    SHIPPING_PAYMENT_TYPE        VARCHAR(1) NOT NULL,
    -- 사용자 결제 배송비
    PAY_SHIPPING                 INTEGER NOT NULL,
    -- 사용자 결제 배송비(변경 전)
    PREVIOUS_PAY_SHIPPING        INTEGER,
    -- 고객 환불 배송비
    RETURN_SHIPPING              INTEGER,
    -- 고객 환불 배송비(변경 전)
    PREVIOUS_RETURN_SHIPPING     INTEGER,
    -- 환불 여부 (Y : 환불)
    RETURN_FLAG                  VARCHAR(1) DEFAULT 'N',
    -- 정산 ID
    REMITTANCE_ID                BIGINT,
    -- 정산금액
    REMITTANCE_AMOUNT            INTEGER NOT NULL,
    -- 정산금액(변경 전)
    PREVIOUS_REMITTANCE_AMOUNT   INTEGER,
    -- 정산 예정일
    REMITTANCE_EXPECTED_DATE     VARCHAR(8),
    -- 정산 상태 (1:대기, 9:완료)
    REMITTANCE_STATUS_CODE       VARCHAR(1) NOT NULL DEFAULT '1',
    -- 정산일자
    REMITTANCE_DATE              VARCHAR(8),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, SHIPPING_SEQUENCE)
);

-- 배송정책 임시저장 TABLE
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_BUY_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 세션 ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 배송정책 순번
    SHIPPING_INDEX               INTEGER NOT NULL,
    -- 이름
    RECEIVE_NAME                 VARCHAR(1000),
    -- 휴대폰번호
    RECEIVE_MOBILE               VARCHAR(1000),
    -- 전화번호
    RECEIVE_PHONE                VARCHAR(1000),
    -- 우편번호
    RECEIVE_ZIPCODE              VARCHAR(10),
    -- 신 우편번호
    RECEIVE_NEW_ZIPCODE          VARCHAR(5),
    -- 시도
    RECEIVE_SIDO                 VARCHAR(20),
    -- 시군구
    RECEIVE_SIGUNGU              VARCHAR(20),
    -- 읍면동
    RECEIVE_EUPMYEONDONG         VARCHAR(20),
    -- 주소
    RECEIVE_ADDRESS              VARCHAR(1000),
    -- 상세 주소
    RECEIVE_ADDRESS_DETAIL       VARCHAR(255),
    -- 배송 요청사항
    CONTENT                      VARCHAR(255),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (USER_ID, SESSION_ID, ORDER_CODE, SHIPPING_INDEX)
);

-- 배송비 쿠폰 임시 저장 TABLE
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_CP_BUY_TEMP (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 배송정책 구분코드
    SHIPPING_GROUP_CODE          VARCHAR(20) NOT NULL,
    -- 쿠폰 사용 수량
    USE_COUPON_COUNT             INTEGER NOT NULL,
    -- 할인금액
    DISCOUNT_AMOUNT              INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (ORDER_CODE, USER_ID, SHIPPING_GROUP_CODE)
);

-- 배송지 정보 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_INFO (
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL DEFAULT '0',
    -- 주문순번
    ORDER_SEQUENCE               INTEGER NOT NULL DEFAULT 0,
    -- 수취인 정보 순번
    SHIPPING_INFO_SEQUENCE       INTEGER NOT NULL DEFAULT 0,
    -- 2015.8.1부터 시행하는 우편번호 (수취인)
    RECEIVE_NEW_ZIPCODE          VARCHAR(5),
    -- 수취인 우편번호
    RECEIVE_ZIPCODE              VARCHAR(10),
    -- 시도
    RECEIVE_SIDO                 VARCHAR(20),
    -- 시군구
    RECEIVE_SIGUNGU              VARCHAR(20),
    -- 읍면동
    RECEIVE_EUPMYEONDONG         VARCHAR(20),
    -- 수취인 주소
    RECEIVE_ADDRESS              VARCHAR(1000),
    -- 수취인 상세 주소
    RECEIVE_ADDRESS_DETAIL       VARCHAR(1785),
    -- 수취인 이름
    RECEIVE_NAME                 VARCHAR(350) NOT NULL,
    -- 수취인 연락처 1
    RECEIVE_PHONE                VARCHAR(210),
    -- 수취인 연락처 2
    RECEIVE_MOBILE               VARCHAR(210) NOT NULL,
    -- 요청사항
    MEMO                         VARCHAR(255),
    -- 등록일(ymdhis)
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, SHIPPING_INFO_SEQUENCE)
);

-- 주문정보 임시 테이블
CREATE TABLE IF NOT EXISTS OP_ORDER_TEMP (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 세션 ID
    SESSION_ID                   VARCHAR(120) NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL DEFAULT '',
    -- 이름
    USER_NAME                    VARCHAR(1000),
    -- 주문에 사용한 포인트
    POINT_DISCOUNT_AMOUNT        INTEGER DEFAULT 0,
    -- 주문에 사용한 상품쿠폰 할인 금액
    ITEM_COUPON_DISCOUNT_AMOUNT  INTEGER DEFAULT 0,
    -- 주문에 사용한 장바구니쿠폰 할인 금액
    CART_COUPON_DISCOUNT_AMOUNT  INTEGER DEFAULT 0,
    -- 주문에 사용한 배송비 할인쿠폰수
    SHIPPING_COUPON_USE_COUNT    INTEGER DEFAULT 0,
    -- 주문에 사용한 배송비 할인액
    SHIPPING_COUPON_DISCOUNT_AMT INTEGER DEFAULT 0,
    -- 장바구니쿠폰 사용 내역
    CART_COUPON_USE_DATA         TEXT,
    -- 주문 결제 금액
    ORDER_PAY_AMOUNT             INTEGER NOT NULL,
    -- 회사명
    COMPANY_NAME                 VARCHAR(50),
    -- 이메일
    EMAIL                        VARCHAR(1000),
    -- 휴대폰번호
    MOBILE                       VARCHAR(1000),
    -- 전화번호
    PHONE                        VARCHAR(1000),
    -- 우편번호
    ZIPCODE                      VARCHAR(10),
    -- 신 우편번호
    NEW_ZIPCODE                  VARCHAR(5),
    -- 시도
    SIDO                         VARCHAR(20),
    -- 시군구
    SIGUNGU                      VARCHAR(20),
    -- 읍면동
    EUPMYEONDONG                 VARCHAR(20),
    -- 주소
    ADDRESS                      VARCHAR(1000),
    -- 상세주소
    ADDRESS_DETAIL               VARCHAR(1000),
    -- 배송 요청일
    DELIVERY_REQ_DAY             VARCHAR(10),
    -- 배송 요청시간
    DELIVERY_REQ_HOUR            VARCHAR(100),
    -- 영수증
    RECEIPT_NAME                 VARCHAR(50),
    -- 현금영수증 발행 타입 (0 : 발행 안함, 1 : 사업자, 2 : 일반)
    CASH_RECEIPT_TYPE            VARCHAR(1) DEFAULT '0',
    -- 현금영수증 신청 번호
    CASH_RECEIPT_CODE            VARCHAR(30),
    -- 현금영수증 발행 타입 (NONE : 발행안함, BUSINESS : 사업자, PERSONAL : 개인)
    CASHBILL_TYPE                VARCHAR(20) DEFAULT 'NONE',
    -- 현금영수증 신청 번호
    CASHBILL_CODE                VARCHAR(30),
    -- 배송지 주소록 저장 여부 (Y: 저장, N: 저장안함)
    SAVE_DELIVERY_FLAG           VARCHAR(1) DEFAULT 'N',
    -- 배송지 주소록 저장 명칭
    SAVE_DELIVERY_NAME           VARCHAR(50),
    -- 구매 경로
    DEVICE_TYPE                  VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (USER_ID, SESSION_ID, ORDER_CODE)
);

-- 정산마스터
CREATE TABLE IF NOT EXISTS OP_REMITTANCE (
    -- 정산 ID
    REMITTANCE_ID                BIGINT NOT NULL,
    -- 정산상태 (1: 정산예정,3: 정산확정,4:정산확인,5:지급완료,9: 정산마감)
    REMITTANCE_STATUS_CODE       VARCHAR(1) DEFAULT '1',
    -- 정산확정일자(지자체관리자 정산확정시 설정됨)
    CONFIRM_DATE                 VARCHAR(8),
    -- 정산확인일자(판매자 정산확인시 설정됨)
    CONFIRM_DATE_SELLER          VARCHAR(8),
    -- 지급완료일자(판매자 정산확인시 설정됨)
    PAYMENT_DATE                 VARCHAR(8),
    -- 정산마감일자(판매자 입금확인시 설정됨)
    FINISHING_DATE               VARCHAR(8),
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL,
    -- 확인 시점 정산금액
    FINISHING_AMOUNT             INTEGER DEFAULT 0,
    -- 확인자 이름
    FINISHING_MANAGER_NAME       VARCHAR(1000),
    -- 입금 계좌번호
    BANK_NAME                    VARCHAR(1000),
    -- 계좌주명
    BANK_IN_NAME                 VARCHAR(210),
    -- 입금계좌번호
    BANK_ACCOUNT_NUMBER          VARCHAR(350),
    -- 생성일
    CREATED_DATE                 VARCHAR(8),
    -- 정산연월
    REMITTANCE_YM                VARCHAR(6),
    PRIMARY KEY (REMITTANCE_ID)
);

-- 정산마감상세
CREATE TABLE IF NOT EXISTS OP_REMITTANCE_DETAIL (
    REMITTANCE_ID                BIGINT NOT NULL,
    REMITTANCE_DETAIL_ID         INTEGER NOT NULL,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    -- 정산KEY
    REMITTANCE_KEY               VARCHAR(50),
    -- 상품구분
    ITEM_TYPE                    VARCHAR(50),
    -- 고객사 상품코드
    ITEM_USER_CODE               VARCHAR(30),
    -- 상품명
    ITEM_NAME                    VARCHAR(200),
    -- 옵션
    OPTIONS                      VARCHAR(4000),
    -- 판매가
    SALE_PRICE                   INTEGER,
    -- 판매자부담금액
    SELLER_DISCOUNT_PRICE        INTEGER,
    -- 판매자부담상세
    SELLER_DISCOUNT_DETAIL       VARCHAR(4000),
    -- 판매자포인트
    SELLER_POINT                 INTEGER,
    -- 세트상품여부
    SET_ITEM_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 세트할인가
    SET_DISCOUNT_PRICE           INTEGER,
    -- 수수료기준금액
    COMMISSION_BASE_PRICE        INTEGER,
    -- 수수료비율
    COMMISSION_RATE              DOUBLE PRECISION,
    -- 수수료
    COMMISSION_PRICE             INTEGER,
    -- 수수료구분
    COMMISSION_TYPE              VARCHAR(1) DEFAULT '1',
    -- 공급가
    SUPPLY_PRICE                 INTEGER,
    -- 정산가
    REMITTANCE_PRICE             INTEGER,
    -- 수량
    QUANTITY                     INTEGER,
    -- 생성일
    CREATED_DATE                 VARCHAR(14),
    -- 주문순번
    ORDER_SEQUENCE               INTEGER DEFAULT 0,
    -- 상품순번
    ITEM_SEQUENCE                INTEGER DEFAULT 0,
    -- 기타금액
    ETC_AMT                      INTEGER DEFAULT 0,
    PRIMARY KEY (REMITTANCE_ID, REMITTANCE_DETAIL_ID)
);

-- 출고지
CREATE TABLE IF NOT EXISTS OP_SHIPMENT (
    -- 출고지 ID
    SHIPMENT_ID                  INTEGER NOT NULL DEFAULT 0,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 주소지명
    ADDRESS_NAME                 VARCHAR(100) NOT NULL,
    -- 이름
    NAME                         VARCHAR(1000),
    -- 전화번호
    TELEPHONE_NUMBER             VARCHAR(1000),
    -- 우편번호
    ZIPCODE                      VARCHAR(10) NOT NULL,
    -- 주소
    ADDRESS                      VARCHAR(1000),
    -- 상세주소
    ADDRESS_DETAIL               VARCHAR(255) NOT NULL,
    -- 기본 출고지 여부 (Y: 기본 출고지, N: 일반 출고지)
    DEFAULT_ADDRESS_FLAG         VARCHAR(1) NOT NULL,
    -- 배송비
    SHIPPING                     INTEGER NOT NULL,
    -- 배송비무료 주문금액
    SHIPPING_FREE_AMOUNT         INTEGER NOT NULL,
    -- 추가배송비 - 제주도
    SHIPPING_EXTRA_CHARGE1       INTEGER NOT NULL,
    -- 추가배송비 - 도서지역
    SHIPPING_EXTRA_CHARGE2       INTEGER NOT NULL,
    -- 배송정책 묶음배송 기준 코드
    SHIPMENT_GROUP_CODE          VARCHAR(50),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (SHIPMENT_ID)
);

-- 반송지
CREATE TABLE IF NOT EXISTS OP_SHIPMENT_RETURN (
    -- 반송지 ID
    SHIPMENT_RETURN_ID           INTEGER NOT NULL DEFAULT 0,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 주소명
    ADDRESS_NAME                 VARCHAR(100) NOT NULL,
    -- 이름
    NAME                         VARCHAR(350),
    -- 전화번호
    TELEPHONE_NUMBER             VARCHAR(140),
    -- 우편번호
    ZIPCODE                      VARCHAR(10) NOT NULL,
    -- 주소
    ADDRESS                      VARCHAR(1000),
    -- 상세 주소
    ADDRESS_DETAIL               VARCHAR(1785),
    -- 기본 주소 설정 여부 (Y: 기본 주소로 설정, N: 기본주소 아님)
    DEFAULT_ADDRESS_FLAG         VARCHAR(255) NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (SHIPMENT_RETURN_ID)
);

-- same-service FKs, added after every table exists (avoids creation-order failures)
ALTER TABLE OP_CART_SET ADD CONSTRAINT fk_op_cart_set_parent_cart_id FOREIGN KEY (PARENT_CART_ID) REFERENCES OP_CART (CART_ID);
ALTER TABLE OP_CASHBILL_ISSUE ADD CONSTRAINT fk_op_cashbill_issue_cashbill_id FOREIGN KEY (CASHBILL_ID) REFERENCES OP_CASHBILL (ID);

-- ===================================================================
-- 기존 파일에서 보존된 내용 (AS-IS에 없는 프로젝트 고유 테이블/컬럼/시퀀스/시드 데이터)
-- ===================================================================

CREATE TABLE IF NOT EXISTS OP_CONDITION (
    CONDITION_ID      INTEGER PRIMARY KEY,
    CATEGORY_CODE     VARCHAR(50),
    CONDITION_TITLE   VARCHAR(255),
    USE_YN            CHAR(1),
    CREATED_DATE      VARCHAR(20),
    UPDATED_DATE      VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS OP_CONDITION_DETAIL (
    DETAIL_ID        INTEGER PRIMARY KEY,
    CONDITION_ID     INTEGER,
    DETAIL_TITLE     VARCHAR(255),
    USE_YN           CHAR(1),
    ORDERING         INTEGER,
    CREATED_DATE     VARCHAR(20),
    UPDATED_DATE     VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS OP_DELIVERY_HOPE (
    DELIVERY_HOPE_ID      INTEGER PRIMARY KEY,
    DELIVERY_HOPE_TIME    VARCHAR(50),
    DELIVERY_HOPE_INDEX   INTEGER
);

CREATE TABLE IF NOT EXISTS OP_ORDER_BATCH_TARGET (
    BATCH_KEY         VARCHAR(255) NOT NULL,
    ORDER_CODE        VARCHAR(50) NOT NULL,
    START_DATE        VARCHAR(14),
    BATCH_TYPE        VARCHAR(20),
    TARGET_INDEX      INTEGER,
    TARGET_CODE       VARCHAR(50),
    ERROR_MESSAGE     TEXT
    -- TODO: primary key unclear — guess composite (BATCH_KEY, ORDER_CODE, TARGET_INDEX)
);

-- =====================================================================
-- MVP additions for the new order microservice implementation (SFR-006).
-- OP_ORDER above has a composite (ORDER_CODE, ORDER_SEQUENCE) PK and no
-- line-item columns of its own (those live in the separate OP_ORDER_ITEM
-- child table) - modeling that properly plus the new SAGA outcome-tracking
-- columns this service needs would mean stitching together two AS-IS
-- tables neither designed for single-item choreography orders. Like
-- point's PT_POINT_LEDGER/PT_POINT_BALANCE, this service uses a small
-- fresh table instead (OD_ prefix) rather than force-fitting AS-IS.
-- MVP scope is a single line item per order (no cart/multi-item yet).
-- =====================================================================

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
('ORDER_STATUS', 'ko', 'PENDING',   '처리중', 1, 'Y'),
('ORDER_STATUS', 'ko', 'CONFIRMED', '주문확정', 2, 'Y'),
('ORDER_STATUS', 'ko', 'CANCELLED', '취소됨', 3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 주문 (choreography SAGA로 gift/point와 비동기 연계 - SFR-006/SFR-008).
-- STOCK_OUTCOME/POINT_OUTCOME은 각각 gift/point 서비스가 발행한 결과 이벤트를
-- 반영하는 필드로, 둘 다 채워지면 주문을 확정(CONFIRMED)하거나 취소(CANCELLED)한다.
CREATE TABLE IF NOT EXISTS OD_ORDER (
    ORDER_ID         VARCHAR(50) PRIMARY KEY,
    USER_ID          BIGINT      NOT NULL,
    ITEM_ID          BIGINT      NOT NULL,
    SELLER_ID        BIGINT,
    ITEM_NAME        VARCHAR(255),
    QUANTITY         INTEGER     NOT NULL,
    UNIT_PRICE       INTEGER     NOT NULL,
    POINT_AMOUNT     BIGINT      NOT NULL,
    ORDER_STATUS     VARCHAR(20) NOT NULL,
    STOCK_OUTCOME    VARCHAR(20),
    POINT_OUTCOME    VARCHAR(20),
    CANCEL_REASON    VARCHAR(255),
    CREATED_DATE     TIMESTAMP   NOT NULL DEFAULT now(),
    UPDATED_DATE     TIMESTAMP
);

-- =====================================================================
-- Round 2 additions: 클레임 프로세스 - 반품/교환 (SFR-006). Approving a claim
-- reuses the exact same order-cancellation compensation path as a plain
-- customer cancel (publishes ORDER_CANCELLED -> gift/point restore stock/
-- points) - 교환(exchange) is handled as return-then-reorder rather than an
-- atomic item swap, a common real-world e-commerce simplification, so RETURN
-- and EXCHANGE only differ in label/reason, not in processing.
-- =====================================================================

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('ORDER_STATUS', 'ko', 'CLAIM_REQUESTED', '반품/교환 신청중', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('CLAIM_TYPE', 'ko', 'RETURN',   '반품', 1, 'Y'),
('CLAIM_TYPE', 'ko', 'EXCHANGE', '교환', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('CLAIM_STATUS', 'ko', 'REQUESTED', '접수', 1, 'Y'),
('CLAIM_STATUS', 'ko', 'APPROVED',  '승인', 2, 'Y'),
('CLAIM_STATUS', 'ko', 'REJECTED',  '거절', 3, 'Y'),
('CLAIM_STATUS', 'ko', 'COMPLETED', '처리완료', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

CREATE TABLE IF NOT EXISTS OD_CLAIM (
    CLAIM_ID         BIGSERIAL PRIMARY KEY,
    ORDER_ID         VARCHAR(50)  NOT NULL,
    CLAIM_TYPE       VARCHAR(20)  NOT NULL, -- RETURN / EXCHANGE
    REASON           VARCHAR(255),
    STATUS           VARCHAR(20)  NOT NULL, -- REQUESTED / APPROVED / REJECTED / COMPLETED
    CREATED_DATE     TIMESTAMP    NOT NULL DEFAULT now(),
    PROCESSED_DATE   TIMESTAMP
);

-- =====================================================================
-- Round 2 (SFR-006 gap fill): 배송관리 (송장등록/배송상태추적/배송완료/수취확인/
-- 배송지변경). 택배사 시스템과의 실제 연계(스마트택배 등)는 스펙상 "운영관리
-- 서비스가 담당"하는 역할분리라 admin 쪽에 SmartDeliveryClient로 별도로 둔다 -
-- order는 배송 상태값 자체(주문에 귀속된 데이터)만 갖고, 실제 캐리어 조회 결과를
-- 반영하는 건 지금은 운영자가 수동으로(admin이 조회한 값을 보고) 입력한다.
-- =====================================================================
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS CARRIER_CODE VARCHAR(50);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS INVOICE_NO VARCHAR(50);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DELIVERY_STATUS VARCHAR(20);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS SHIPPED_DATE TIMESTAMP;

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DELIVERED_DATE TIMESTAMP;

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS CONFIRMED_DATE TIMESTAMP;

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DELIVERY_ADDRESS VARCHAR(255);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DELIVERY_ADDRESS_DETAIL VARCHAR(255);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('DELIVERY_STATUS', 'ko', 'SHIPPED',    '발송완료', 1, 'Y'),
('DELIVERY_STATUS', 'ko', 'IN_TRANSIT', '배송중',   2, 'Y'),
('DELIVERY_STATUS', 'ko', 'DELIVERED',  '배송완료', 3, 'Y'),
('DELIVERY_STATUS', 'ko', 'CONFIRMED',  '구매확정', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('DELIVERY_CARRIER', 'ko', 'CJ',    'CJ대한통운', 1, 'Y'),
('DELIVERY_CARRIER', 'ko', 'POST',  '우체국택배', 2, 'Y'),
('DELIVERY_CARRIER', 'ko', 'HANJIN','한진택배',   3, 'Y'),
('DELIVERY_CARRIER', 'ko', 'LOTTE', '롯데택배',   4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 3: 장바구니 (AS-IS cart/index.html). 기부 포인트는 기부한 지자체 답례품에만
-- 쓸 수 있어서(장바구니 화면의 지자체별 "잔여 포인트"/"주문불가"), 주문에도 그 답례품의
-- 지자체를 남겨야 point 서비스가 어느 지자체 포인트를 차감할지 판단할 수 있다.
-- 장바구니 자체는 단일품목 OD_ORDER를 여러 번 만드는 방식으로 체크아웃하므로
-- (order.saga 계약을 다중품목으로 바꾸지 않기 위해) 장바구니 테이블은 회원+답례품당
-- 한 행만 갖는 단순한 목록이다.
-- =====================================================================
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS LOCGOV_CODE VARCHAR(50);

CREATE TABLE IF NOT EXISTS OD_CART_ITEM (
    CART_ITEM_ID     BIGSERIAL PRIMARY KEY,
    USER_ID          BIGINT      NOT NULL,
    ITEM_ID          BIGINT      NOT NULL,
    QUANTITY         INTEGER     NOT NULL,
    CREATED_DATE     TIMESTAMP   NOT NULL DEFAULT now(),
    UPDATED_DATE     TIMESTAMP,
    UNIQUE (USER_ID, ITEM_ID)
);

-- =====================================================================
-- Round 4: 주문결제/주문완료 (AS-IS order/step1.html, order/step2.html). AS-IS의
-- 배송지 주소록 관리(여러 배송지 저장/선택)와 PG 결제 연동은 이 MSA 범위 밖(진짜 결제
-- 게이트웨이가 없음 - 포인트 차감이 곧 "결제")이라 뺐고, 이번 결제 1건에 쓸 받는사람
-- 정보만 주문 시점에 입력받아 그대로 저장한다.
-- =====================================================================
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS RECEIVER_NAME VARCHAR(100);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS RECEIVER_PHONE VARCHAR(20);

ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS REQUEST_NOTE VARCHAR(150);

-- =====================================================================
-- Coupon 서브시스템 (AS-IS opmanager/coupon+coupon-regular+coupon-use, 회원 coupon/mypage).
-- OP_COUPON* 테이블은 550테이블 스캔 당시 이미 실존 확인됐으나(빈 테이블) COUPON_ID/
-- COUPON_OFFLINE_ID PK 컬럼에 시퀀스가 없었다(OP_SELLER/OP_BRAND와 동일한 배치스캔 누락
-- 패턴) - 여기서 보강한다.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_coupon_coupon_id_seq START WITH 1;
ALTER TABLE OP_COUPON ALTER COLUMN COUPON_ID SET DEFAULT nextval('op_coupon_coupon_id_seq');
ALTER SEQUENCE op_coupon_coupon_id_seq OWNED BY OP_COUPON.COUPON_ID;

CREATE SEQUENCE IF NOT EXISTS op_coupon_offline_coupon_offline_id_seq START WITH 1;
ALTER TABLE OP_COUPON_OFFLINE ALTER COLUMN COUPON_OFFLINE_ID SET DEFAULT nextval('op_coupon_offline_coupon_offline_id_seq');
ALTER SEQUENCE op_coupon_offline_coupon_offline_id_seq OWNED BY OP_COUPON_OFFLINE.COUPON_OFFLINE_ID;

CREATE SEQUENCE IF NOT EXISTS op_coupon_regular_coupon_id_seq START WITH 1;
ALTER TABLE OP_COUPON_REGULAR ALTER COLUMN COUPON_ID SET DEFAULT nextval('op_coupon_regular_coupon_id_seq');
ALTER SEQUENCE op_coupon_regular_coupon_id_seq OWNED BY OP_COUPON_REGULAR.COUPON_ID;

-- 주문에 실제 적용된 쿠폰 - 체크아웃 시점 할인 반영용(OD_ORDER.POINT_AMOUNT는 이미 할인
-- 반영된 실제 차감 포인트가 저장된다. COUPON_ISSUE_ID는 OP_COUPON_USER.COUPON_USER_ID 참조).
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS COUPON_ISSUE_ID INTEGER;
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DISCOUNT_AMOUNT BIGINT NOT NULL DEFAULT 0;

-- =====================================================================
-- admin 주문관리 콘솔 (AS-IS opmanager/order - OrderManagerController, 3700줄/70+ 엔드포인트
-- 중 검색/상세/상태변경/클레임처리큐/엑셀다운로드 핵심을 재구현). 이 라운드에서 admin이
-- 호출하는 신규 admin-only REST API(/api/admin/orders/**, /api/admin/claims/**)와, 지금까지
-- 무인증 상태였던 기존 /claims, /claims/{id}/approve|reject|complete,
-- /orders/{orderId}/invoice, /orders/{orderId}/delivery-status를 전부 AdminApiAuthInterceptor
-- (공유시크릿 헤더 검증)로 게이트한다 - "아직 운영자 로그인 모델이 없어서 무인증"이었던
-- 실보안결함을 이걸로 해소한다.
-- =====================================================================
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS ADMIN_MEMO VARCHAR(1000);

-- SFR-005 재검토 라운드 - "배송비·택배사 설정, 배송정책"을 gift의 답례품별 설정을 참조해
-- 주문 시점에 계산한 배송비. POINT_AMOUNT(실제 차감액)엔 이미 합산돼 있고, 이 컬럼은
-- 화면에 상품금액/배송비 내역을 분리해서 보여주기 위한 표시용이다.
ALTER TABLE OD_ORDER ADD COLUMN IF NOT EXISTS DELIVERY_FEE BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS OD_CLAIM_MEMO (
    CLAIM_MEMO_ID    BIGSERIAL PRIMARY KEY,
    CLAIM_ID         BIGINT        NOT NULL,
    MANAGER_ID       BIGINT,
    MANAGER_NAME     VARCHAR(100),
    MEMO             VARCHAR(1000) NOT NULL,
    CREATED_DATE     TIMESTAMP     NOT NULL DEFAULT now()
);

ALTER TABLE OD_CLAIM_MEMO ADD CONSTRAINT fk_od_claim_memo_claim_id
    FOREIGN KEY (CLAIM_ID) REFERENCES OD_CLAIM (CLAIM_ID);

CREATE TABLE IF NOT EXISTS OD_EXCEL_DOWNLOAD_LOG (
    DOWNLOAD_LOG_ID  BIGSERIAL PRIMARY KEY,
    MANAGER_ID       BIGINT,
    MANAGER_NAME     VARCHAR(100),
    DOWNLOAD_REASON  VARCHAR(500)  NOT NULL,
    SEARCH_CONDITION VARCHAR(1000),
    ROW_COUNT        INTEGER,
    CREATED_DATE     TIMESTAMP     NOT NULL DEFAULT now()
);

-- 새 테이블은 postgres 소유로 생성되므로 앱 접속계정(orderdb)에 명시적으로 권한을 줘야 한다
-- (다른 OD_* 테이블과 동일한 배치스캔/보강 패턴).
GRANT ALL PRIVILEGES ON OD_CLAIM_MEMO TO orderdb;
GRANT ALL PRIVILEGES ON OD_EXCEL_DOWNLOAD_LOG TO orderdb;
GRANT ALL PRIVILEGES ON SEQUENCE od_claim_memo_claim_memo_id_seq TO orderdb;
GRANT ALL PRIVILEGES ON SEQUENCE od_excel_download_log_download_log_id_seq TO orderdb;
