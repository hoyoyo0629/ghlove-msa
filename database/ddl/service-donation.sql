-- ===================================================================
-- TO-BE DB 재구성(2026-09): 물리 2개(gift + 나머지), 논리 6개 스키마 구조로 전환.
-- 이 파일은 이제 자신의 단독 DB가 아니라 'ghlove_core' DB 안의 'donation' 스키마에 적용한다.
-- 실행예: psql -U postgres -d ghlove_core -f service-donation.sql
-- ===================================================================
CREATE SCHEMA IF NOT EXISTS donation;
SET search_path TO donation;

-- Service: donation
-- ===================================================================
-- AS-IS 운영 DB 원본 스키마 기준 자동 생성 (75개 테이블)
-- 출처: 운영사이트 DB dump(CUBRID) -> PostgreSQL 변환. 인덱스는 이번 생성에서 제외(추후 실사용 쿼리 패턴 기반으로 추가).
-- 서비스간 FK는 제거됨(no cross-service FK 원칙).
-- ===================================================================

-- 행정기관코드 법정동 매핑 테이블
CREATE TABLE IF NOT EXISTS G_ADM_LOCGOV (
    -- 행정기관코드(동)
    ADM_CD                       VARCHAR(20) NOT NULL,
    -- 행정동
    ADM_SECT_NM                  VARCHAR(1000),
    -- 법정동 코드(시군구)
    LOCGOV_CODE                  VARCHAR(20),
    PRIMARY KEY (ADM_CD)
);

-- 대행구매 로그인 확인 정보
CREATE TABLE IF NOT EXISTS G_AGENCY_LOGIN_CONFIRM_INFO (
    -- 사용자 세션 아이디
    USER_SESSION_ID              VARCHAR(50) NOT NULL,
    -- 유효 접근 코드(토큰 암호화)
    VALID_ACCESS_CD              VARCHAR(10000) NOT NULL,
    -- 개인키(복호화키)
    PRIVATE_KEY                  VARCHAR(5000) NOT NULL,
    -- 관리자 아이디
    MANAGER_ID                   BIGINT NOT NULL,
    -- 기부자 휴대전화번호
    CNTRBTR_MOBILE               VARCHAR(50) NOT NULL,
    -- 기부자 아이디
    CNTRBTR_ID                   BIGINT NOT NULL DEFAULT 0,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_SESSION_ID, VALID_ACCESS_CD)
);

-- 소식지 카드 뉴스 관리
CREATE TABLE IF NOT EXISTS G_CATALOG_CARD_NEWS_IMG_DESC (
    -- 카드 뉴스 아이디
    CARD_NEWS_ID                 BIGINT NOT NULL,
    -- 이미지 설명
    IMG_DESC                     VARCHAR(500) NOT NULL,
    -- 이미지 순번
    IMG_SEQ                      INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CARD_NEWS_ID, IMG_SEQ)
);

-- 소식지 카드 뉴스 관리
CREATE TABLE IF NOT EXISTS G_CATALOG_CARD_NEWS_MNG (
    -- 카드 뉴스 아이디
    CARD_NEWS_ID                 BIGINT NOT NULL,
    -- 소식지 발간 년도
    CATALOG_YEAR                 INTEGER NOT NULL,
    -- 소식지 발간 호수
    CATALOG_NO                   INTEGER NOT NULL,
    -- 카드 뉴스 제목
    CARD_NEWS_SUBJECT            VARCHAR(500) NOT NULL,
    -- 카드 뉴스 내용
    CARD_NEWS_CN                 VARCHAR,
    -- 삭제 여부
    DELETE_YN                    VARCHAR(5) NOT NULL DEFAULT 'N',
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CARD_NEWS_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_catalog_card_news_mng_card_news_id_seq START WITH 1000;
ALTER TABLE G_CATALOG_CARD_NEWS_MNG ALTER COLUMN CARD_NEWS_ID SET DEFAULT nextval('g_catalog_card_news_mng_card_news_id_seq');
ALTER SEQUENCE g_catalog_card_news_mng_card_news_id_seq OWNED BY G_CATALOG_CARD_NEWS_MNG.CARD_NEWS_ID;

-- 인기 답례품 관리
CREATE TABLE IF NOT EXISTS G_CATALOG_MNG (
    -- 소식지 발간 년도
    CATALOG_YEAR                 INTEGER NOT NULL,
    -- 소식지 발간 호수
    CATALOG_NO                   INTEGER NOT NULL,
    -- 소식지 대민화면 표시 여부
    DISPLAY_YN                   VARCHAR(5) NOT NULL DEFAULT 'N',
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CATALOG_YEAR, CATALOG_NO)
);

-- 지정기부 응원메시지 등록
CREATE TABLE IF NOT EXISTS G_CHEER_MSG (
    CNTR_SN                      VARCHAR(50) NOT NULL,
    -- 응원 메시지 내용
    CHEER_MSG                    VARCHAR(1000),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CNTR_SN)
);

-- 기부
CREATE TABLE IF NOT EXISTS G_CNTR (
    CNTR_SN                      VARCHAR(50) NOT NULL,
    -- 기부 일자
    CNTR_DE                      VARCHAR(8) NOT NULL,
    USER_ID                      BIGINT NOT NULL,
    PSITN_LOCGOV_CODE            VARCHAR(10) NOT NULL,
    CNTR_LOCGOV_CODE             VARCHAR(10) NOT NULL,
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 기부포인트
    CNTR_POINT                   BIGINT NOT NULL DEFAULT 0,
    -- 기부잔액
    CNTR_BLCE_POINT              BIGINT DEFAULT 0,
    -- 포인트 만료 일자
    POINT_END_DE                 VARCHAR(8),
    -- 신고 납부 일자
    STTEMNT_PAY_DE               VARCHAR(8),
    -- 납부 유효 일자
    PAY_VALID_DE                 VARCHAR(8),
    -- 기부 경로 코드
    CNTR_PATH_CODE               VARCHAR(10),
    -- 기부 상태 코드
    CNTR_STTUS_CODE              VARCHAR(10),
    -- 기부자 의견 내용
    CNTRBTR_OPINION_CN           VARCHAR(2000),
    -- 결제 방법 코드
    SETLE_MTH_CODE               VARCHAR(10),
    -- 기부 사용 목적 코드
    CNTR_USE_PURPS_CODE          VARCHAR(10),
    -- 전자 납부 번호 (Donation 엔티티에 매핑 안 됨 - 이 프로젝트엔 전자수납 연계가 없다.
    -- NOT NULL로 생성돼 있어서 기부 생성 자체가 항상 실패하던 버그를 실제로 발견해 nullable로 수정)
    ELCTRN_PAY_NO                VARCHAR(30),
    -- 서울시 대상 여부
    SEOUL_TRGET_AT               VARCHAR(1),
    -- 접수은행코드
    RCEPT_BANK_CODE              VARCHAR(10),
    -- 접수은행명(지점명)
    RCEPT_BANK_NM                VARCHAR(100),
    -- 접수자명
    RCEPTER_NM                   VARCHAR(50),
    -- 답례품신청코드
    RTNPSNT_REQST_CODE           VARCHAR(10),
    -- 답례품신청여부(사용안함)
    RTNPSNT_REQST_AT             VARCHAR(255),
    -- 행정정보이용동의여부
    INFO_AGRE_AT                 VARCHAR(1),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점 (Donation.frstRegistPnttm은 String 필드 - AS-IS 관행대로
    -- yyyyMMddHHmmss 포맷 문자열로 저장. 한때 TIMESTAMP로 잘못 선언돼 있었으나
    -- 실제 라이브 DB/엔티티와 맞춰 VARCHAR로 정정)
    FRST_REGIST_PNTTM            VARCHAR(14),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 취소여부
    DELETE_AT                    VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 국세청상태코드
    NTS_STTUS_MSSAGE             VARCHAR(50),
    TMP_BLCE_POINT               BIGINT DEFAULT 0,
    FOREIGN_STATUS_CODE          VARCHAR(2) NOT NULL DEFAULT '0',
    -- 지정기부 프로젝트 관리번호
    PRJ_ID                       BIGINT DEFAULT 0,
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT DEFAULT 0,
    -- 연계 기관 코드(전자납부번호 생성시점)
    LINK_INSTT_CD                VARCHAR(20),
    SUNAP_PROCSS_YN              VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (CNTR_SN)
);

CREATE TABLE IF NOT EXISTS G_CNTR_CHENAP (
    ID                           BIGINT NOT NULL,
    ELCTRN_PAY_NO                VARCHAR(30),
    PRIMARY KEY (ID)
);
CREATE SEQUENCE IF NOT EXISTS g_cntr_chenap_id_seq START WITH 1000;
ALTER TABLE G_CNTR_CHENAP ALTER COLUMN ID SET DEFAULT nextval('g_cntr_chenap_id_seq');
ALTER SEQUENCE g_cntr_chenap_id_seq OWNED BY G_CNTR_CHENAP.ID;

-- 기부 제한
CREATE TABLE IF NOT EXISTS G_CNTR_LMTT (
    -- 제한 시작 일자
    LMTT_BGN_DE                  VARCHAR(8) NOT NULL,
    -- 제한 종료 일자
    LMTT_END_DE                  VARCHAR(8) NOT NULL,
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 위반 사유 코드
    VIOLT_RESN_CODE              VARCHAR(10) NOT NULL,
    -- 위반 사유 내용
    VIOLT_RESN_CN                VARCHAR(500),
    -- 등록자 명
    REGISTER_NM                  VARCHAR(50),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LMTT_BGN_DE, LMTT_END_DE, LOCGOV_CODE)
);

-- 기부_지자체변경이력
CREATE TABLE IF NOT EXISTS G_CNTR_LOCGOV_HISTORY (
    CNTR_SN                      VARCHAR(50) NOT NULL,
    -- 이력순번
    LOCGOV_HIST_NO               INTEGER,
    -- 변경전기부지자체
    BFR_CNTR_LOCGOV_CODE         VARCHAR(10),
    -- 변경후기부지자체
    AFT_CNTR_LOCGOV_CODE         VARCHAR(10),
    -- 변경전지자체
    BFR_PSITN_LOCGOV_CODE        VARCHAR(10),
    -- 변경후지자체
    AFT_PSITN_LOCGOV_CODE        VARCHAR(10),
    -- 최초등록자ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초등록시점
    FRST_REGIST_PNTTM            TIMESTAMP,
    -- 최종수정자ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종수정시점
    LAST_UPDT_PNTTM              TIMESTAMP
);

-- 기부 영수증
CREATE TABLE IF NOT EXISTS G_CNTR_RCIPT (
    -- 기부 일련번호
    CNTR_SN                      VARCHAR(20) NOT NULL,
    -- 전자 납부 번호
    ELCTRN_PAY_NO                VARCHAR(30) NOT NULL,
    -- 기부출력 일련번호
    CNTR_OUTPT_SN                INTEGER NOT NULL,
    -- 발급 일자
    ISSU_DE                      VARCHAR(8) NOT NULL,
    -- 발급 횟수
    ISSU_CO                      INTEGER,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (CNTR_SN, ELCTRN_PAY_NO, CNTR_OUTPT_SN)
);

-- 기부_변경신청
CREATE TABLE IF NOT EXISTS G_CNTR_REQMNG (
    REQ_ID                       BIGINT NOT NULL,
    -- 기부자ID
    LOGIN_ID                     VARCHAR(300) NOT NULL,
    -- 이름
    USER_NAME                    VARCHAR(300),
    -- 지자체코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 신고 납부 일자
    STTEMNT_PAY_DE               VARCHAR(8) NOT NULL,
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 기부금변경 코드(100: 과오납, 200: 포인트 생성, 300: 배치 동작 처리, 400: 수납취소)
    CNTR_REQMNG_CODE             VARCHAR(10),
    -- 비고(사유)
    DISCRIPTION                  VARCHAR(300),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 사용여부
    USE_YN                       VARCHAR(1) DEFAULT 'Y',
    -- 기부금변경 코드(100: 과오납, 200: 포인트 생성, 300: 배치 동작 처리, 400: 수납취소)
    REQ_STATUS_CODE              VARCHAR(10) NOT NULL,
    -- 기부금변경 승인일시
    APPR_DT                      TIMESTAMP,
    -- 기부금변경 취소일시
    CANCLE_DT                    TIMESTAMP,
    CNTR_SN                      VARCHAR(50),
    -- 세외수입 시스템 취소일(과오납 처리시)
    TAX_SYS_CANCEL_DE            VARCHAR(8),
    -- 관련문서 생산부서명(과오납 처리시)
    RELATED_DOC_DPT_NM           VARCHAR(300),
    -- 관련문서 문서번호(과오납 처리시)
    RELATED_DOC_NUM              VARCHAR(300),
    -- 관련문서 시행일(과오납 처리시)
    RELATED_DOC_DE               VARCHAR(300),
    PRIMARY KEY (REQ_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_cntr_reqmng_req_id_seq START WITH 1000;
ALTER TABLE G_CNTR_REQMNG ALTER COLUMN REQ_ID SET DEFAULT nextval('g_cntr_reqmng_req_id_seq');
ALTER SEQUENCE g_cntr_reqmng_req_id_seq OWNED BY G_CNTR_REQMNG.REQ_ID;

-- 기부 영수증 처리 신고 내역
CREATE TABLE IF NOT EXISTS G_CNTR_TAX_LOG (
    -- 로그아이디
    LOG_ID                       BIGINT NOT NULL,
    -- 전자납부번호
    ELCTRN_PAY_NO                VARCHAR(30) NOT NULL,
    -- 수납일자
    STTEMNT_PAY_DE               VARCHAR(8) NOT NULL,
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 배치결과코드
    TAX_STATUS_CODE              VARCHAR(5),
    -- 영수증처리결과코드
    NTS_RES_CODE                 VARCHAR(50),
    -- 영수증처리결과메세지
    NTS_RES_MSSAGE               VARCHAR(50),
    -- 기부금코드
    CONB_CD                      VARCHAR(2),
    -- 기부타입
    CNTR_TYPE                    VARCHAR(2),
    -- 신청구분코드
    ELCR_APL_CD                  VARCHAR(2),
    -- 등록일시
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 수정일시
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_cntr_tax_log_log_id_seq START WITH 1000;
ALTER TABLE G_CNTR_TAX_LOG ALTER COLUMN LOG_ID SET DEFAULT nextval('g_cntr_tax_log_log_id_seq');
ALTER SEQUENCE g_cntr_tax_log_log_id_seq OWNED BY G_CNTR_TAX_LOG.LOG_ID;

-- 기부내역 영수증 로그
CREATE TABLE IF NOT EXISTS G_CNTR_TAX_LOG3 (
    -- 전자납부번호
    ELCTRN_PAY_NO                VARCHAR(30),
    -- 신고납부일자
    STTEMNT_PAY_DE               VARCHAR(8),
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 영수증 상태코드
    TAX_STATUS_CODE              VARCHAR(5),
    -- 국세청 상태 코드
    NTS_RES_CODE                 VARCHAR(50),
    -- 국세청 상태 메시지
    NTS_RES_MSSAGE               VARCHAR(50),
    -- 기부금 코드(43:고향사랑기부금)
    CONB_CD                      VARCHAR(2),
    -- 기부자신분확인구분코드(01:개인)
    CNTR_TYPE                    VARCHAR(2),
    ELCR_APL_CD                  VARCHAR(2),
    -- 최초 등록일시
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록일시
    LAST_UPDT_PNTTM              TIMESTAMP
);

-- 기부내역 영수증 임시
CREATE TABLE IF NOT EXISTS G_CNTR_TAX_TEMP (
    CNTR_SN                      VARCHAR(50),
    STTEMNT_PAY_DE               VARCHAR(8),
    CONB_CD                      VARCHAR(8) DEFAULT '43',
    -- 회원ID
    USER_ID                      BIGINT,
    -- 회원CI
    MBER_CI                      VARCHAR(200),
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 전자납부번호
    ELCTRN_PAY_NO                VARCHAR(30),
    BIZ_NO                       VARCHAR(50),
    ELCR_APL_CD                  VARCHAR(2) DEFAULT '',
    IS_SEND                      INTEGER DEFAULT 0,
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now()
);

-- 기부내역 임시테이블2
CREATE TABLE IF NOT EXISTS G_CNTR_TEMP2 (
    -- 전자납부번호
    USER_ID                      VARCHAR(30) NOT NULL,
    -- 기부금액
    CNTR_SUM_AMT                 BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (USER_ID)
);

-- 위기브 기부내역
CREATE TABLE IF NOT EXISTS G_CNTR_WEGIVE (
    -- 기부 일련번호
    CNTR_SN                      BIGINT NOT NULL,
    -- 기부일
    CNTR_DE                      VARCHAR(100),
    -- 기부금액
    CNTR_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 기부자 명
    USER_NAME                    VARCHAR(100),
    -- 기부자 CI
    MBER_CI                      VARCHAR(100),
    -- 기부 일시
    FRST_REG_DT                  TIMESTAMP,
    PRIMARY KEY (CNTR_SN)
);
CREATE SEQUENCE IF NOT EXISTS g_cntr_wegive_cntr_sn_seq START WITH 1000;
ALTER TABLE G_CNTR_WEGIVE ALTER COLUMN CNTR_SN SET DEFAULT nextval('g_cntr_wegive_cntr_sn_seq');
ALTER SEQUENCE g_cntr_wegive_cntr_sn_seq OWNED BY G_CNTR_WEGIVE.CNTR_SN;

-- 기부금 운용
CREATE TABLE IF NOT EXISTS G_CTBNY_OPRATN (
    -- 등록 일련번호
    REGIST_SN                    INTEGER NOT NULL,
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 사업 목적 코드
    BSNS_PURPS_CODE              VARCHAR(10),
    -- 사업 명
    BSNS_NM                      VARCHAR(100),
    -- 사업 내용
    BSNS_CN                      VARCHAR,
    -- 비고
    RM                           VARCHAR(2000),
    -- 지출 일자
    EXPNDTR_DE                   VARCHAR(8),
    -- 지출 금액
    EXPNDTR_AMT                  INTEGER,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (REGIST_SN)
);

-- 기부금 운용 파일
CREATE TABLE IF NOT EXISTS G_CTBNY_OPRATN_FILE (
    -- 등록 파일 ID
    REGIST_FILE_ID               INTEGER NOT NULL,
    -- 등록 일련번호
    REGIST_SN                    INTEGER NOT NULL,
    -- 파일 명
    FILE_NM                      VARCHAR(200) NOT NULL,
    -- 파일 유형
    FILE_TY                      VARCHAR(50),
    -- 정렬 순서
    SORT_ORDR                    INTEGER,
    -- 원본 파일 명
    ORGINL_FILE_NM               VARCHAR(200),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (REGIST_FILE_ID, REGIST_SN)
);

-- 기부금 설정
CREATE TABLE IF NOT EXISTS G_CTBNY_SETUP (
    -- 기준 년도
    STDR_YEAR                    VARCHAR(4) NOT NULL,
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 한도 금액
    LMT_AMT                      INTEGER NOT NULL,
    -- 포인트 비율
    POINT_RATE                   NUMERIC(5,2) NOT NULL,
    -- 포인트 유효 기간
    POINT_VALID_PD               INTEGER,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 최종 수정자 명 (admin 지자체관리 화면 "포인트 지급률 변경이력" 표시용 - admin의 매니저
    -- 정보는 별도 서비스라 FK 대신 쓰기 시점 값을 그대로 들고 있는다. AS-IS 대비 추가 컬럼)
    LAST_UPDUSR_NM                VARCHAR(50),
    PRIMARY KEY (STDR_YEAR, LOCGOV_CODE)
);

CREATE TABLE IF NOT EXISTS G_DISASTER_ZONE (
    LOCGOV_CODE                  VARCHAR(10),
    ZONE_ID                      INTEGER NOT NULL,
    LOCGOV_NM                    VARCHAR(100),
    F_DISASTER_ST                VARCHAR(10),
    F_DISASTER_ED                VARCHAR(10),
    S_DISATER_ST                 VARCHAR(10),
    S_DISASTER_ED                VARCHAR(10),
    PRIMARY KEY (ZONE_ID)
);

-- 지정 기부 사업 승인 로그
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_APRV_LOG (
    -- 지정 기부 사업 승인 아이디
    DSGN_DNTN_BIZ_APRV_ID        BIGINT NOT NULL,
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT NOT NULL,
    -- 승인 이전 지정 기부 사업 상태 코드
    APRV_BFR_DSGN_DNTN_BIZ_STTS_CD VARCHAR(10),
    -- 승인 이후 지정 기부 사업 상태 코드
    APRV_AFTR_DSGN_DNTN_BIZ_STTS_CD VARCHAR(10) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_APRV_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_APRV_LOG ALTER COLUMN DSGN_DNTN_BIZ_APRV_ID SET DEFAULT nextval('g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq OWNED BY G_DSGN_DNTN_BIZ_APRV_LOG.DSGN_DNTN_BIZ_APRV_ID;

-- 지정 기부 사업 내용 이미지 설명
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_CN_IMG_EXPLN (
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT NOT NULL,
    -- 지정 기부 사업 내용 이미지 아이디
    DSGN_DNTN_BIZ_CN_IMG_ID      BIGINT NOT NULL,
    -- 이미지 순서
    IMG_SEQ                      INTEGER NOT NULL,
    -- 이미지 설명
    IMG_EXPLN                    VARCHAR(5000) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_CN_IMG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_CN_IMG_EXPLN ALTER COLUMN DSGN_DNTN_BIZ_CN_IMG_ID SET DEFAULT nextval('g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq OWNED BY G_DSGN_DNTN_BIZ_CN_IMG_EXPLN.DSGN_DNTN_BIZ_CN_IMG_ID;

-- 지정 기부 사업 부서 관리
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_DEPT_MNG (
    -- 지정 기부 사업 부서 아이디
    DSGN_DNTN_BIZ_DEPT_ID        BIGINT NOT NULL,
    -- 지정 기부 사업 부서 명
    DSGN_DNTN_BIZ_DEPT_NM        VARCHAR(50) NOT NULL,
    -- 지방자치단체 코드
    LCLGV_CD                     VARCHAR(10),
    -- 사용 여부
    USE_YN                       VARCHAR(5) NOT NULL DEFAULT 'Y',
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT NOT NULL,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 지정 기부 사업 부서 코드
    DSGN_DNTN_BIZ_DEPT_CD        VARCHAR(20),
    PRIMARY KEY (DSGN_DNTN_BIZ_DEPT_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_DEPT_MNG ALTER COLUMN DSGN_DNTN_BIZ_DEPT_ID SET DEFAULT nextval('g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq OWNED BY G_DSGN_DNTN_BIZ_DEPT_MNG.DSGN_DNTN_BIZ_DEPT_ID;

-- 지정 기부 사업 부서 관리자 매핑
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_DEPT_MNGR_MPNG (
    -- 사용자 아이디
    USER_ID                      BIGINT NOT NULL,
    -- 지정 기부 사업 부서 아이디
    DSGN_DNTN_BIZ_DEPT_ID        BIGINT NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT NOT NULL,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_ID)
);

-- 지정 기부 사업 이미지 관리
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_IMG_MNG (
    -- 지정 기부 사업 이미지 아이디
    DSGN_DNTN_BIZ_IMG_ID         BIGINT NOT NULL,
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT NOT NULL,
    -- 이미지 명
    IMG_NM                       VARCHAR(255) NOT NULL,
    -- 이미지 순서
    IMG_SEQ                      INTEGER NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_IMG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_IMG_MNG ALTER COLUMN DSGN_DNTN_BIZ_IMG_ID SET DEFAULT nextval('g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq OWNED BY G_DSGN_DNTN_BIZ_IMG_MNG.DSGN_DNTN_BIZ_IMG_ID;

-- 지정 기부 사업 관리
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_MNG (
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT NOT NULL,
    -- 지정 기부 사업 제목
    DSGN_DNTN_BIZ_TTL            VARCHAR(255),
    -- 지정 기부 사업 내용
    DSGN_DNTN_BIZ_CN             VARCHAR,
    -- 지정 기부 사업 시작 일자
    DSGN_DNTN_BIZ_BGNG_YMD       VARCHAR(8) NOT NULL,
    -- 지정 기부 사업 종료 일자
    DSGN_DNTN_BIZ_END_YMD        VARCHAR(8) NOT NULL,
    -- 목표 금액
    GOAL_AMT                     BIGINT NOT NULL DEFAULT 0,
    -- 지정 기부 사업 상태 코드(1:대기, 2:진행 9:종료)
    DSGN_DNTN_BIZ_STTS_CD        VARCHAR(10) NOT NULL,
    -- 공개 여부(Y: 공개, N: 비공개)
    RLS_YN                       VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 지정 기부 사업 대표 이미지
    DSGN_DNTN_BIZ_RPRS_IMG       VARCHAR(255),
    -- 지정 기부 사업 구분 코드
    DSGN_DNTN_BIZ_SE_CD          VARCHAR(20),
    -- 지방자치단체 코드
    LCLGV_CD                     VARCHAR(10),
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP,
    -- 지정 기부 사업 구분 상세 코드(부문)
    DSGN_DNTN_BIZ_SE_DTL_CD      VARCHAR(20),
    -- 지정 기부 사업 기타 내용
    DSGN_DNTN_BIZ_ETC_CN         VARCHAR,
    -- 지정 기부 사업 부서 아이디
    DSGN_DNTN_BIZ_DEPT_ID        BIGINT DEFAULT 0,
    PRIMARY KEY (DSGN_DNTN_BIZ_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_MNG ALTER COLUMN DSGN_DNTN_BIZ_ID SET DEFAULT nextval('g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq OWNED BY G_DSGN_DNTN_BIZ_MNG.DSGN_DNTN_BIZ_ID;

-- 지정 기부 사업 공지 내용 이미지 설명
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_CN_IMG_EXPLN (
    -- 지정 기부 사업 공지 아이디
    DSGN_DNTN_BIZ_NTC_ID         BIGINT NOT NULL,
    -- 이미지 순서
    IMG_SEQ                      INTEGER NOT NULL,
    -- 이미지 설명
    IMG_EXPLN                    VARCHAR(5000) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_NTC_ID, IMG_SEQ)
);

-- 지정 기부 사업 공지 파일 관리
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_FILE_MNG (
    -- 지정 기부 사업 공지 아이디
    DSGN_DNTN_BIZ_NTC_ID         BIGINT NOT NULL,
    -- 파일 순서
    FILE_SEQ                     INTEGER NOT NULL,
    -- 파일 명
    FILE_NM                      VARCHAR(255) NOT NULL,
    -- 경로 명
    PATH_NM                      VARCHAR(255) NOT NULL,
    -- 원파일 명
    ORGNFL_NM                    VARCHAR(255) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_NTC_ID, FILE_SEQ)
);

-- 지정 기부 사업 공지 관리
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_MNG (
    -- 지정 기부 사업 공지 아이디
    DSGN_DNTN_BIZ_NTC_ID         BIGINT NOT NULL,
    -- 지정 기부 사업 아이디
    DSGN_DNTN_BIZ_ID             BIGINT NOT NULL,
    -- 지정 기부 사업 공지 제목
    DSGN_DNTN_BIZ_NTC_TTL        VARCHAR(255) NOT NULL,
    -- 지정 기부 사업 공지 내용
    DSGN_DNTN_BIZ_NTC_CN         VARCHAR,
    -- 공개 여부(Y: 공개, N: 비공개)
    RLS_YN                       VARCHAR(1) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT NOT NULL,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGN_DNTN_BIZ_NTC_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq START WITH 1000;
ALTER TABLE G_DSGN_DNTN_BIZ_NTC_MNG ALTER COLUMN DSGN_DNTN_BIZ_NTC_ID SET DEFAULT nextval('g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq');
ALTER SEQUENCE g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq OWNED BY G_DSGN_DNTN_BIZ_NTC_MNG.DSGN_DNTN_BIZ_NTC_ID;

-- 지정 기부 사업 사용자 메시지
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_USER_MSG (
    -- 기부 일련번호
    DNTN_SN                      VARCHAR(20) NOT NULL,
    -- 사용자 메시지
    USER_MSG                     VARCHAR(100),
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT NOT NULL,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DNTN_SN)
);

-- 지정기부 부서 관리
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PART_MNG (
    -- 지정기부 부서 아이디
    DSGNCNTR_PART_ID             BIGINT NOT NULL,
    -- 부서명
    DSGNCNTR_PART_NAME           VARCHAR(50) NOT NULL,
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(20),
    -- 사용 여부
    USE_YN                       VARCHAR(5) NOT NULL DEFAULT 'Y',
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (DSGNCNTR_PART_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgncntr_part_mng_dsgncntr_part_id_seq START WITH 1000;
ALTER TABLE G_DSGNCNTR_PART_MNG ALTER COLUMN DSGNCNTR_PART_ID SET DEFAULT nextval('g_dsgncntr_part_mng_dsgncntr_part_id_seq');
ALTER SEQUENCE g_dsgncntr_part_mng_dsgncntr_part_id_seq OWNED BY G_DSGNCNTR_PART_MNG.DSGNCNTR_PART_ID;

-- 지정기부 부서 - 관리자 매핑
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PART_USER_MPPNG (
    -- 사용자 아이디
    USER_ID                      BIGINT NOT NULL,
    -- 지정기부 부서 아이디
    DSGNCNTR_PART_ID             BIGINT NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (USER_ID)
);

-- 지정기부 프로젝트 이미지 정보
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PRJ_IMG (
    -- 프로젝트 이미지 ID
    DSGNCNTR_PRJ_IMAGE_ID        BIGINT NOT NULL,
    -- 프로젝트 ID
    PRJ_ID                       BIGINT NOT NULL,
    -- 이미지 파일 명
    IMAGE_NAME                   VARCHAR(255) NOT NULL,
    -- 정렬 순서
    ORDERING                     INTEGER NOT NULL,
    -- 등록일시
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 등록 사용자 ID
    FRST_REGISTER_PNTTM          TIMESTAMP DEFAULT now(),
    PRIMARY KEY (DSGNCNTR_PRJ_IMAGE_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq START WITH 1000;
ALTER TABLE G_DSGNCNTR_PRJ_IMG ALTER COLUMN DSGNCNTR_PRJ_IMAGE_ID SET DEFAULT nextval('g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq');
ALTER SEQUENCE g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq OWNED BY G_DSGNCNTR_PRJ_IMG.DSGNCNTR_PRJ_IMAGE_ID;

-- 지정기부 프로젝트 관리
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PRJ_MNG (
    -- 지정기부 프로젝트 관리번호
    PRJ_ID                       BIGINT NOT NULL,
    -- 프로젝트 제목(사업명)
    PRJ_SUBJECT                  VARCHAR(255),
    -- 프로젝트 소개
    PRJ_CN                       VARCHAR,
    -- 프로젝트 기간 시작일자
    PRJ_ST_DT                    VARCHAR(8) NOT NULL,
    -- 프로젝트 기간 종료일자
    PRJ_ED_DT                    VARCHAR(8) NOT NULL,
    -- 목표금액
    TARGET_AMT                   BIGINT NOT NULL DEFAULT 0,
    -- 프로젝트 상태코드(1:대기, 2:진행, 9:종료)
    PRJ_STATUS                   VARCHAR(1) NOT NULL,
    -- 공개여부(Y: 공개, N: 비공개)
    DISPLAY_FLAG                 VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 프로젝트 대표이미지
    PRJ_IMAGE                    VARCHAR(255),
    -- 사업구분 코드
    BSNS_TYPE                    VARCHAR(20),
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(20),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 기타 입력
    CONTENT_ETC                  VARCHAR,
    -- 부서 아이디
    DSGNCNTR_PART_ID             BIGINT DEFAULT 0,
    -- 사업구분하위코드
    BSNS_SUB_TYPE                VARCHAR(20),
    PRIMARY KEY (PRJ_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgncntr_prj_mng_prj_id_seq START WITH 1000;
ALTER TABLE G_DSGNCNTR_PRJ_MNG ALTER COLUMN PRJ_ID SET DEFAULT nextval('g_dsgncntr_prj_mng_prj_id_seq');
ALTER SEQUENCE g_dsgncntr_prj_mng_prj_id_seq OWNED BY G_DSGNCNTR_PRJ_MNG.PRJ_ID;

-- 지정기부 공지사항
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PRJ_NOTICE (
    -- 지정기부 공지사항 아이디
    PRJ_NOTICE_ID                BIGINT NOT NULL,
    -- 지정기부 아이디
    PRJ_ID                       BIGINT NOT NULL,
    -- 지정기부 공지사항 제목
    PRJ_NOTICE_SUBJECT           VARCHAR(255) NOT NULL,
    -- 지정기부 공지내용
    PRJ_NOTICE_CN                VARCHAR,
    -- 사용자 화면 공개 여부
    DISPLAY_YN                   VARCHAR(5) NOT NULL DEFAULT 'Y',
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (PRJ_NOTICE_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_dsgncntr_prj_notice_prj_notice_id_seq START WITH 1000;
ALTER TABLE G_DSGNCNTR_PRJ_NOTICE ALTER COLUMN PRJ_NOTICE_ID SET DEFAULT nextval('g_dsgncntr_prj_notice_prj_notice_id_seq');
ALTER SEQUENCE g_dsgncntr_prj_notice_prj_notice_id_seq OWNED BY G_DSGNCNTR_PRJ_NOTICE.PRJ_NOTICE_ID;

-- 지정기부 공지사항 첨부 파일
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PRJ_NOTICE_FILE (
    -- 지정기부 공지사항 ID
    PRJ_NOTICE_ID                BIGINT NOT NULL,
    -- 파일 업로드 순번
    FILE_SEQ                     INTEGER NOT NULL,
    -- 저장된 파일명
    FILE_NAME                    VARCHAR(255) NOT NULL,
    -- 저장된 디렉토리 경로
    PATH_NAME                    VARCHAR(255) NOT NULL,
    -- 원본 파일명
    ORG_FILE_NAME                VARCHAR(255) NOT NULL,
    -- 등록 사용자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (PRJ_NOTICE_ID, FILE_SEQ)
);

-- 웹접근성 위한 지정기부 공지사항 이미지 설명 정보
CREATE TABLE IF NOT EXISTS G_DSGNCNTR_PRJ_NOTICE_IMG_DESC (
    -- 공지사항 아이디
    PRJ_NOTICE_ID                BIGINT NOT NULL,
    -- 이미지 설명
    IMG_DESC                     VARCHAR(5000) NOT NULL,
    -- 이미지 순번
    IMG_SEQ                      INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (PRJ_NOTICE_ID, IMG_SEQ)
);

-- 매일 총금액
CREATE TABLE IF NOT EXISTS G_DY_GRAMT (
    -- 생성일
    CREATED_AT                   VARCHAR(17) NOT NULL,
    -- 기준일자
    STDR_DE                      VARCHAR(8) NOT NULL,
    -- 최종년도총금액
    LAST_YEAR_GRAMT              BIGINT,
    -- 지금년도총금액
    NOW_YEAR_GRAMT               BIGINT,
    PRIMARY KEY (CREATED_AT)
);

-- 지로번호
CREATE TABLE IF NOT EXISTS G_GIRO (
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 지자체 명
    LOCGOV_NM                    VARCHAR(50),
    -- 이용 기관 코드
    USE_INSTT_CODE               VARCHAR(2),
    -- 지로 번호
    GIRO_NO                      VARCHAR(7),
    PRIMARY KEY (LOCGOV_CODE)
);

-- 명예 기부자
CREATE TABLE IF NOT EXISTS G_HONOR_CNTRBTR (
    STDR_YEAR                    VARCHAR(4) NOT NULL,
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    USER_ID                      BIGINT NOT NULL,
    -- 명예 기부자 레벨 코드
    HONOR_CNTRBTR_LEVEL_CODE     VARCHAR(10),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (STDR_YEAR, LOCGOV_CODE, USER_ID)
);

-- 명예 기부자 기준
CREATE TABLE IF NOT EXISTS G_HONOR_CNTRBTR_STDR (
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 기준 년도
    STDR_YEAR                    VARCHAR(4) NOT NULL,
    -- 기부 금액
    CNTR_AMT                     INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE)
);

-- 행정 기관 코드
CREATE TABLE IF NOT EXISTS G_INSTT_CODE (
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 상위 지자체 명
    UPPER_LOCGOV_NM              VARCHAR(50),
    -- 지자체 명
    LOCGOV_NM                    VARCHAR(50),
    -- 지자체 매핑 코드
    LOCGOV_MAPNG_CODE            VARCHAR(10),
    -- 행정 기관 코드
    ADMINIST_INSTT_CODE          VARCHAR(10),
    -- 기관 매핑 코드
    INSTT_MAPNG_CODE             VARCHAR(10),
    -- 사용 여부
    USE_AT                       VARCHAR(1),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE)
);

-- 관심 지자체
CREATE TABLE IF NOT EXISTS G_INTRST_LOCGOV (
    -- 회원 고유번호
    USER_ID                      BIGINT NOT NULL,
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 등록 일자
    REGIST_DE                    VARCHAR(8) NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (USER_ID, LOCGOV_CODE)
);

-- 지자체 명예 사용자 혜택 이미지 설명
CREATE TABLE IF NOT EXISTS G_LCLGV_HNR_USER_RWRD_IMG_EXPLN (
    -- 지자체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    -- 이미지 순서
    IMG_SEQ                      INTEGER NOT NULL,
    -- 이미지 설명
    IMG_EXPLN                    VARCHAR(5000) NOT NULL,
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (LCLGV_CD, IMG_SEQ)
);

-- 지자체 명예 사용자 설정 관리
CREATE TABLE IF NOT EXISTS G_LCLGV_HNR_USER_STNG_MNG (
    -- 지자체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    -- 골드 등급 기부 금액
    GLD_GRD_DNTN_AMT             INTEGER NOT NULL DEFAULT 0,
    -- 실버 등급 기부 금액
    SLVR_GRD_DNTN_AMT            INTEGER NOT NULL DEFAULT 0,
    -- 브론즈 등급 기부 금액
    BRNZ_GRD_DNTN_AMT            INTEGER NOT NULL DEFAULT 0,
    -- 명예 사용자 혜택
    HNR_USER_RWRD                VARCHAR,
    -- 대표 이미지 명
    RPRS_IMG_NM                  VARCHAR(255),
    -- 명예 사용자 선정 구분 코드
    HNR_USER_SLCTN_SE_CD         VARCHAR(20),
    -- 사용 여부
    USE_YN                       VARCHAR(5) NOT NULL DEFAULT 'N',
    -- 최초 등록자 아이디
    FRST_RGTR_ID                 BIGINT NOT NULL DEFAULT 0,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 등록자 아이디
    LAST_RGTR_ID                 BIGINT NOT NULL DEFAULT 0,
    -- 최종 등록 일시
    LAST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 명예사용자 설정 제목(기부확인증 명칭)
    HNR_USER_STNG_TTL            VARCHAR(255),
    PRIMARY KEY (LCLGV_CD)
);

-- 연계 기관 암호화 키 관리
CREATE TABLE IF NOT EXISTS G_LINK_INSTT_ENC_KEY_MNG (
    -- 암호화 키 버전
    ENC_KEY_VER                  VARCHAR(10) NOT NULL,
    -- 암호화 키
    ENC_KEY                      VARCHAR(1000) NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ENC_KEY_VER)
);

-- 지자체
CREATE TABLE IF NOT EXISTS G_LOCGOV (
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 상위 지자체 명
    UPPER_LOCGOV_NM              VARCHAR(50),
    -- 지자체명
    LOCGOV_NM                    VARCHAR(50) NOT NULL,
    -- 상위 지자체 코드
    UPPER_LOCGOV_CODE            VARCHAR(10) NOT NULL,
    -- 지자체 소개
    LOCGOV_INTRCN_CN             VARCHAR(4000),
    -- 담당자연락처
    CHARGER_CTTPC                VARCHAR(300),
    -- 담당자명
    CHARGER_NM                   VARCHAR(300),
    -- 담당자 소속 부서
    CHARGER_PSITN_DEPT           VARCHAR(50),
    -- 지자체 홈페이지
    LOCGOV_HMPG                  VARCHAR(100),
    -- 지자체 인구 수
    LOCGOV_POPLTN_CO             VARCHAR(100),
    -- 지자체 면적
    LOCGOV_AR                    VARCHAR(100),
    -- 지자체 특산물
    LOCGOV_SPCPRD                VARCHAR(500),
    -- 상품권 사용 여부
    GCCT_USE_AT                  VARCHAR(1),
    -- 전자화폐 사용 여부
    ETRCSH_USE_AT                VARCHAR(1),
    -- 지자체 예산
    LOCGOV_BUDGET_AMT            BIGINT DEFAULT 0,
    -- 사업자번호
    BIZRNO                       VARCHAR(50),
    -- 지자체 우편번호
    LOCGOV_ZIP                   VARCHAR(10),
    -- 기본 주소
    BASS_ADRES                   VARCHAR(100),
    -- 상세 주소
    DTL_ADRES                    VARCHAR(100),
    -- 주류 판매 여부
    ACHLQR_SLE_AT                VARCHAR(1),
    -- 사용 여부
    USE_AT                       VARCHAR(1),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    -- 처리부서코드
    PROCESS_DEPT_CODE            VARCHAR(30),
    -- 행정기관코드
    ADMINIST_INSTT_CODE          VARCHAR(10),
    -- 직인 명
    OFFCS_NM                     VARCHAR(200),
    -- 직인 파일 명
    OFFCS_FILE_NM                VARCHAR(200),
    -- 원본 파일 명
    ORGINL_FILE_NM               VARCHAR(200),
    -- 기준 1레벨 금액
    STDR_1LEVEL_AMT              INTEGER DEFAULT 0,
    -- 기준 2레벨 금액
    STDR_2LEVEL_AMT              INTEGER DEFAULT 0,
    -- 기준 3레벨 금액
    STDR_3LEVEL_AMT              INTEGER DEFAULT 0,
    FIS_SP                       VARCHAR(10),
    CHARGER_EMAIL                VARCHAR(100),
    ORDERING                     BIGINT,
    PRIMARY KEY (LOCGOV_CODE)
);

-- 지자체 부서변경이력
CREATE TABLE IF NOT EXISTS G_LOCGOV_DEPT_HIST (
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    DEPT_HIST_NO                 INTEGER NOT NULL,
    -- 처리부서코드
    PROCESS_DEPT_CODE            VARCHAR(20),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL,
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE, DEPT_HIST_NO)
);

CREATE TABLE IF NOT EXISTS G_LOCGOV_FAV_ITEM_MNG (
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 소식지 발간 년도
    CATALOG_YEAR                 INTEGER NOT NULL,
    -- 소식지 발간 호수
    CATALOG_NO                   INTEGER NOT NULL,
    -- 삭제 여부
    DELETE_YN                    VARCHAR(5) NOT NULL DEFAULT 'N',
    -- 인기 답례품 ID
    ITEM_ID                      BIGINT NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT NOT NULL,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT NOT NULL,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (LOCGOV_CODE, CATALOG_YEAR, CATALOG_NO)
);

-- 지자체 답례품배경이미지
CREATE TABLE IF NOT EXISTS G_LOCGOV_IMAGE (
    -- 지자체코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- PC용 이미지 파일명
    PC_FILE_NAME                 VARCHAR(255),
    -- 모바일용 파일명
    MOBILE_FILE_NAME             VARCHAR(255),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE)
);

-- 부가정보
CREATE TABLE IF NOT EXISTS G_NEXT_BUGA_REQUEST (
    -- 연계관리키
    LINK_MNG_KEY                 VARCHAR(50) NOT NULL,
    -- 자치단체코드
    SGB_CD                       VARCHAR(7) NOT NULL,
    -- 연계대상코드
    LINK_TRGT_CD                 VARCHAR(15) NOT NULL,
    -- 부서코드
    DPT_CD                       VARCHAR(7) NOT NULL,
    -- 특별회계사업코드(회계구분이 51,61 인경우에만 해당)
    SPCL_FIS_BIZ_CD              VARCHAR(4),
    -- 회계연도 부과연도
    FYR                          VARCHAR(4),
    ACT_SE_CD                    VARCHAR(2),
    -- 대표세입과목코드
    RPRS_TXM_CD                  VARCHAR(6),
    -- 운영항목코드
    OPER_ITEM_CD                 VARCHAR(3),
    -- 부과일자
    LVY_YMD                      VARCHAR(8),
    -- 최초본세금액 국세+시도세+시군구세 합계금액
    FRST_PCT_AMT                 VARCHAR(15),
    -- 최초납기일자
    FRST_PID_YMD                 VARCHAR(8),
    -- 납부자구분코드 01:개인 02:법인 03:단체 05:외국인
    PYR_SE_CD                    VARCHAR(2),
    -- 납부자번호 - 빼고 숫자만
    PYR_NO                       VARCHAR(13),
    PYR_NM                       VARCHAR(200),
    -- 대표납부자번호 법인의 경우 대표 납부자번호 필수
    RPRS_PYR_NO                  VARCHAR(24),
    -- 대표납부자명 법인의 경우 대표 납부자명 필수
    RPRS_PYR_NM                  VARCHAR(200),
    PYR_STT_CD                   VARCHAR(2),
    LOTNO_ROAD_ADDR_SE_CD        VARCHAR(2),
    -- 우편번호
    ZIP                          VARCHAR(6),
    -- 도로명코드
    ROAD_NM_CD                   VARCHAR(12),
    -- 건물본번
    BMNO                         VARCHAR(5),
    -- 건물부번
    BSNO                         VARCHAR(5),
    -- 법정동코드
    STDG_CD                      VARCHAR(10),
    -- 행정동코드
    DONG_CD                      VARCHAR(10),
    ROAD_NM_DADDR                VARCHAR(1000),
    -- 물건지명
    GL_NM                        VARCHAR(500),
    -- 부과처리시 필요한 부과근거 1(법적부과근거)
    MNG_ITEM_CN1                 VARCHAR(200),
    -- 부과 요청 진행 코드
    BUGA_STATUS_CD               VARCHAR(3),
    -- 응답결과코드
    LINK_RST_CD                  VARCHAR(30),
    -- 응답결과메세지
    LINK_RST_MSG                 VARCHAR(200),
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (LINK_MNG_KEY)
);

CREATE TABLE IF NOT EXISTS G_NEXT_BUGA_REQUEST_TEMP (
    LINK_MNG_KEY                 VARCHAR(50) NOT NULL,
    SGB_CD                       VARCHAR(7) NOT NULL,
    LINK_TRGT_CD                 VARCHAR(15) NOT NULL,
    DPT_CD                       VARCHAR(7) NOT NULL,
    SPCL_FIS_BIZ_CD              VARCHAR(4),
    FYR                          VARCHAR(4),
    ACT_SE_CD                    VARCHAR(2),
    RPRS_TXM_CD                  VARCHAR(6),
    OPER_ITEM_CD                 VARCHAR(3),
    LVY_YMD                      VARCHAR(8),
    FRST_PCT_AMT                 VARCHAR(15),
    FRST_PID_YMD                 VARCHAR(8),
    PYR_SE_CD                    VARCHAR(2),
    PYR_NO                       VARCHAR(13),
    PYR_NM                       VARCHAR(200),
    RPRS_PYR_NO                  VARCHAR(24),
    RPRS_PYR_NM                  VARCHAR(200),
    PYR_STT_CD                   VARCHAR(2),
    LOTNO_ROAD_ADDR_SE_CD        VARCHAR(2),
    ZIP                          VARCHAR(6),
    ROAD_NM_CD                   VARCHAR(12),
    BMNO                         VARCHAR(5),
    BSNO                         VARCHAR(5),
    STDG_CD                      VARCHAR(10),
    DONG_CD                      VARCHAR(10),
    ROAD_NM_DADDR                VARCHAR(20),
    GL_NM                        VARCHAR(500),
    MNG_ITEM_CN1                 VARCHAR(200),
    BUGA_STATUS_CD               VARCHAR(3),
    LINK_RST_CD                  VARCHAR(30),
    LINK_RST_MSG                 VARCHAR(200),
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (LINK_MNG_KEY)
);

-- 수납결과
CREATE TABLE IF NOT EXISTS G_NEXT_SUNAP_RESPONSE (
    -- 연계관리키
    LINK_MNG_KEY                 VARCHAR(50) NOT NULL,
    -- 자치단체코드
    SGB_CD                       VARCHAR(7) NOT NULL,
    -- 자치단체코드 명
    SGB_NM                       VARCHAR(60),
    -- 과세번호
    TAXN_NO                      VARCHAR(50),
    -- 통합과세번호
    UNTY_TAXN_NO                 VARCHAR(31),
    -- 부서코드
    DPT_CD                       VARCHAR(7) NOT NULL,
    -- 부서코드
    DPT_NM                       VARCHAR(200),
    -- 특별회계사업코드(회계구분이 51,61 인경우에만 해당)
    SPCL_FIS_BIZ_CD              VARCHAR(4) NOT NULL,
    -- 특별회계사업코드(회계구분이 51,61 인경우에만 해당)
    SPCL_FIS_BIZ_NM              VARCHAR(100),
    -- 회계연도 부과연도
    FYR                          VARCHAR(4),
    ACT_SE_CD                    VARCHAR(2),
    ACT_SE_NM                    VARCHAR(100),
    -- 대표세입과목코드
    RPRS_TXM_CD                  VARCHAR(6),
    -- 대표세입과목코드
    RPRS_TXM_NM                  VARCHAR(100),
    -- 운영항목코드
    OPER_ITEM_CD                 VARCHAR(3),
    -- 운영항목코드
    OPER_ITEM_NM                 VARCHAR(100),
    LVY_NO                       VARCHAR(6),
    ITM_NO                       VARCHAR(2),
    -- 전자납부번호
    EPAY_NO                      VARCHAR(19) NOT NULL,
    -- 수납번호
    RCVMT_NO                     VARCHAR(2),
    -- 수납구분코드
    RCVMT_SE_CD                  VARCHAR(2),
    -- 수납구분명
    RCVMT_SE_NM                  VARCHAR(100),
    -- 수납일자
    RCVMT_YMD                    VARCHAR(8),
    -- 회계일자
    ACT_YMD                      VARCHAR(8),
    -- 이체일자
    TSF_YMD                      VARCHAR(8),
    -- 수납본세금액
    RCVMT_PCT_AMT                VARCHAR(15),
    -- 수납가산금액
    RCVMT_ADTN_AMT               VARCHAR(15),
    -- 수납이자금액
    RCVMT_INTR_AMT               VARCHAR(15),
    -- 은행명
    BANK_NM                      VARCHAR(30),
    -- 수납유형코드
    RCVMT_TY_CD                  VARCHAR(2),
    -- 수납유형
    RCVMT_TY                     VARCHAR(300),
    -- 예비항목1
    RSVE_ITEM1                   VARCHAR(200),
    -- 예비항목2
    RSVE_ITEM2                   VARCHAR(200),
    -- 예비항목3
    RSVE_ITEM3                   VARCHAR(200),
    -- 예비항목4
    RSVE_ITEM4                   VARCHAR(200),
    -- 예비항목5
    RSVE_ITEM5                   VARCHAR(200),
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (LINK_MNG_KEY)
);

-- 지불 로그
CREATE TABLE IF NOT EXISTS G_PAY_LOG (
    -- 지불 로그 아이디
    PAY_LOG_ID                   BIGINT NOT NULL,
    -- 전자 납부 번호
    ELCTRN_PAY_NO                VARCHAR(30) NOT NULL,
    -- 지불 방법
    PAY_METHOD                   VARCHAR(100),
    -- 지불 시작 일시
    PAY_START_DT                 TIMESTAMP,
    -- 지줄 종료 일시
    PAY_END_DT                   TIMESTAMP,
    -- 지불 진행
    PAY_PROCESS                  VARCHAR(100),
    -- 최초등록아이디
    FRST_REG_ID                  BIGINT NOT NULL,
    -- 최초등록일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정 아이디
    LAST_MDFCN_ID                BIGINT,
    -- 최종 수정 일시
    LAST_MDFCN_DT                TIMESTAMP,
    PRIMARY KEY (PAY_LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_pay_log_pay_log_id_seq START WITH 1000;
ALTER TABLE G_PAY_LOG ALTER COLUMN PAY_LOG_ID SET DEFAULT nextval('g_pay_log_pay_log_id_seq');
ALTER SEQUENCE g_pay_log_pay_log_id_seq OWNED BY G_PAY_LOG.PAY_LOG_ID;

-- 웹접근성 위한 지정기부 프로젝트 이미지 설명 정보
CREATE TABLE IF NOT EXISTS G_PRJ_CONTENT_IMG_DESC (
    -- 프로젝트 상세 이미지 ID
    PRJ_CONTENT_IMG_ID           BIGINT NOT NULL,
    -- 지정기부 프로젝트 관리번호
    PRJ_ID                       BIGINT NOT NULL,
    -- 프로젝트 상세 이미지 설명
    IMG_DESC                     VARCHAR(5000) NOT NULL,
    -- 프로젝트 상세 이미지 순번
    IMG_SEQ                      INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (PRJ_CONTENT_IMG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_prj_content_img_desc_prj_content_img_id_seq START WITH 1000;
ALTER TABLE G_PRJ_CONTENT_IMG_DESC ALTER COLUMN PRJ_CONTENT_IMG_ID SET DEFAULT nextval('g_prj_content_img_desc_prj_content_img_id_seq');
ALTER SEQUENCE g_prj_content_img_desc_prj_content_img_id_seq OWNED BY G_PRJ_CONTENT_IMG_DESC.PRJ_CONTENT_IMG_ID;

CREATE TABLE IF NOT EXISTS G_RELAY_LOG (
    RELAY_LOG_ID                 INTEGER NOT NULL,
    USER_ID                      BIGINT NOT NULL,
    RELAY_TYPE                   VARCHAR(25) NOT NULL,
    CNTR_LOCGOV_CODE             VARCHAR(10) NOT NULL,
    CNTR_SN                      VARCHAR(20),
    RELAY_RESULT_CODE            VARCHAR(10) NOT NULL,
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (RELAY_LOG_ID)
);

-- 외부연계 인터페이스_서울시 이택스 대사파일
CREATE TABLE IF NOT EXISTS GIF_ETAX_DAESA (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    EPAY_NO                      VARCHAR(19) NOT NULL,
    -- 요청매체
    COM_REQ_MECHE                VARCHAR(20) NOT NULL,
    -- 요청일자
    COM_REQ_DT                   VARCHAR(8) NOT NULL,
    -- 요청일자
    COM_REQ_TM                   VARCHAR(6) NOT NULL,
    -- 전문관리번호
    COM_PAY_MSG_NO               VARCHAR(12) NOT NULL,
    -- api key
    ACCESS_KEY                   VARCHAR(32) NOT NULL,
    -- 기관코드
    ORG_C                        VARCHAR(32) NOT NULL,
    -- 회계세목코드
    SEMOK_CD                     VARCHAR(8),
    -- 납부금액
    SUNAP_AMT                    INTEGER NOT NULL DEFAULT 0,
    -- 납부일자
    SUNAP_DT                     VARCHAR(8) NOT NULL,
    -- 응답코드
    RST_CD                       VARCHAR(3) NOT NULL,
    -- 응답메세지
    RST_MSG                      VARCHAR(100),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

-- 외부 연계 이텍스 부과정보
CREATE TABLE IF NOT EXISTS GIF_ETAX_SUNAP (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    EPAY_NO                      VARCHAR(19) NOT NULL,
    -- 요청매체
    COM_REQ_MECHE                VARCHAR(20) NOT NULL,
    -- 요청일자
    COM_REQ_DT                   VARCHAR(8) NOT NULL,
    -- 요청일자
    COM_REQ_TM                   VARCHAR(6) NOT NULL,
    -- 전문관리번호
    COM_PAY_MSG_NO               VARCHAR(12) NOT NULL,
    -- api key
    ACCESS_KEY                   VARCHAR(32) NOT NULL,
    -- 기관코드
    ORG_C                        VARCHAR(32) NOT NULL,
    -- 납부여부
    SUNAP_YN                     VARCHAR(32) NOT NULL,
    -- 납부금액
    SUNAP_AMT                    INTEGER NOT NULL DEFAULT 0,
    -- 납부일자
    SUNAP_DT                     VARCHAR(8) NOT NULL,
    -- 응답코드
    RST_CD                       VARCHAR(3) NOT NULL,
    -- 응답메세지
    RST_MSG                      VARCHAR(100),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

-- 외부연계_서울시 이택스 부과정보 타매체
CREATE TABLE IF NOT EXISTS GIF_ETAX_SUNAP_ETC (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    EPAY_NO                      VARCHAR(19) NOT NULL,
    -- 요청매체
    COM_REQ_MECHE                VARCHAR(20) NOT NULL,
    -- 요청일자
    COM_REQ_DT                   VARCHAR(8) NOT NULL,
    -- 요청일자
    COM_REQ_TM                   VARCHAR(6) NOT NULL,
    -- 전문관리번호
    COM_PAY_MSG_NO               VARCHAR(12) NOT NULL,
    -- api key
    ACCESS_KEY                   VARCHAR(32) NOT NULL,
    -- 기관코드
    ORG_C                        VARCHAR(32) NOT NULL,
    -- 납부여부
    SUNAP_YN                     VARCHAR(32) NOT NULL,
    -- 납부금액
    SUNAP_AMT                    INTEGER NOT NULL DEFAULT 0,
    -- 납부일자
    SUNAP_DT                     VARCHAR(8) NOT NULL,
    -- 응답코드
    RST_CD                       VARCHAR(3) NOT NULL,
    -- 응답메세지
    RST_MSG                      VARCHAR(100),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

-- 외부연계 인터페이스_마스터
CREATE TABLE IF NOT EXISTS GIF_EXTERNAL_MST (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    ENAPBU_NO                    VARCHAR(19) NOT NULL,
    -- 인터페이스 상태
    STATUS                       VARCHAR(1) NOT NULL,
    -- 재전송 횟수
    RTY_CNT                      INTEGER NOT NULL DEFAULT 0,
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

-- 국세청 전자영수증
CREATE TABLE IF NOT EXISTS GIF_HOMETAX (
    -- 연계인터페이스번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    ELCTRN_PAY_NO                VARCHAR(19) NOT NULL,
    -- 사용자ID
    USER_ID                      BIGINT NOT NULL,
    -- 기부일
    DNT_DT                       VARCHAR(8) NOT NULL,
    -- 기부금코드
    CONB_CD                      VARCHAR(2) NOT NULL,
    -- 기부금액
    DNT_AMT                      VARCHAR(20) NOT NULL,
    -- 기부자신분확인구분코드
    CNTR_TYPE                    VARCHAR(2) NOT NULL,
    -- 기부자CI
    MEM_CI                       VARCHAR(300) NOT NULL,
    -- 지자체사업자번호
    BIZ_NO                       VARCHAR(300) NOT NULL,
    -- 응답코드
    RESULT_CODE                  VARCHAR(100),
    -- 응답메시지
    RESULT_MSG                   VARCHAR(100),
    -- 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

-- 외부연계 인터페이스_서울시 세외
CREATE TABLE IF NOT EXISTS GIF_SEOUL (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    ENAPBU_NO                    VARCHAR(19) NOT NULL,
    -- 기관코드
    SIGU_CD                      VARCHAR(7) NOT NULL,
    -- 세목코드
    SEMOK_CD                     VARCHAR(8) NOT NULL,
    -- 과세년월
    TAX_YM                       VARCHAR(6) NOT NULL,
    -- 과세구분
    TAX_GUBUN                    VARCHAR(1) NOT NULL,
    -- 시도코드
    SIDO_CD                      VARCHAR(2) NOT NULL,
    -- 납세자명
    NAP_NM                       VARCHAR(80) NOT NULL,
    -- 납세자구분
    NAP_GUBUN                    VARCHAR(2) NOT NULL,
    -- 본세합계
    TAX_AMT                      INTEGER NOT NULL DEFAULT 0,
    -- 시세
    SISE                         INTEGER NOT NULL DEFAULT 0,
    -- 거주상태
    RESIDE_STATUS                VARCHAR(2) NOT NULL,
    -- 물건구분
    MUL_GUBUN                    VARCHAR(2) NOT NULL,
    -- 물건명
    MUL_NM                       VARCHAR(100) NOT NULL,
    -- 대장번호
    BOOK_NO                      VARCHAR(30),
    -- 대장번호
    SYS_GUBUN                    VARCHAR(10),
    -- 에러코드
    ERROR_CD                     VARCHAR(3) NOT NULL,
    -- 에러메세지
    ERROR_MSG                    VARCHAR(2000),
    -- insert key
    INSERT_KEY                   VARCHAR(6),
    -- insert ak
    INSERT_AK                    VARCHAR(15),
    -- 처리건수
    RESULT_CNT                   VARCHAR(10),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    NAP_ID                       VARCHAR(1000),
    PRIMARY KEY (IF_NO)
);

--  표준세외(현세대) 부과자료
CREATE TABLE IF NOT EXISTS GIF_STND_BUGA_NOW (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    ELCT_PAY_NO                  VARCHAR(19) NOT NULL,
    -- 전문메세지길이
    COM_MSG_LEN                  VARCHAR(8) NOT NULL,
    -- 인터페이스ID
    COM_IF_ID                    VARCHAR(15) NOT NULL,
    -- source system
    COM_SOURCE                   VARCHAR(11) NOT NULL,
    -- target system
    COM_TARGET                   VARCHAR(11) NOT NULL,
    -- 연계 메세지키
    COM_MSG_KEY                  VARCHAR(38) NOT NULL,
    -- 연계 유형코드
    COM_TYPE_CD                  VARCHAR(3) NOT NULL,
    -- 결과코드
    COM_RST_CD                   VARCHAR(3) NOT NULL,
    -- 시스템코드
    SYS_CD                       VARCHAR(4) NOT NULL,
    -- 부서코드
    DEPT_CD                      VARCHAR(11) NOT NULL,
    -- 회계연도
    FISYY                        VARCHAR(4) NOT NULL,
    -- 회계구분
    FIS_SP                       VARCHAR(2) NOT NULL,
    -- 세목코드
    PTCL_CD                      VARCHAR(6) NOT NULL,
    -- 부과일자
    IMPS_DT                      VARCHAR(8) NOT NULL,
    -- 최초본세
    INIT_PRCP_TAX_AMT            INTEGER NOT NULL,
    -- 최종본세
    LST_PRCP_TAX_AMT             INTEGER NOT NULL,
    -- 최초납기일자
    INIT_DUE_DT                  VARCHAR(8) NOT NULL,
    -- 납기후금액
    LST_DUE_DT                   INTEGER,
    -- 납기후일자
    AF_DUE_DT                    VARCHAR(8),
    AF_DUE_AMT                   INTEGER,
    -- 부과구분
    IMPS_SP                      VARCHAR(2) NOT NULL,
    -- 감경구분
    DECS_SP                      VARCHAR(2) NOT NULL,
    -- 납부자구분
    TXPR_SP                      VARCHAR(2) NOT NULL,
    -- 납부자전화번호
    TXPR_TEL_NO                  VARCHAR(15),
    -- 납부자휴대폰
    TXPR_MPHN_NO                 VARCHAR(15),
    -- 납부자이메일
    TXPR_EML                     VARCHAR(30),
    -- 새주조여부
    NEW_ADDR_YN                  VARCHAR(1) NOT NULL,
    -- 도로명코드
    TXPR_ROAD_CD                 VARCHAR(12) NOT NULL,
    -- 지하여부
    TXPR_BD_FLR_SP               VARCHAR(1),
    -- 건물본번
    TXPR_BD_PRCP_NO              VARCHAR(5) NOT NULL,
    -- 건물부번
    TXPR_BD_SUB_NO               VARCHAR(5) NOT NULL,
    -- 상태코드
    STAT_CD                      VARCHAR(2) NOT NULL,
    -- 특별회계사업코드
    SPCL_FIS_BIZ_CD              VARCHAR(5) NOT NULL,
    -- 우편번호
    TXPR_ZIP_NO                  VARCHAR(5) NOT NULL,
    -- 법정동코드
    TXPR_LGLVIL_CD               VARCHAR(10),
    -- 행정동코드
    TXPR_TWNVIL_CD               VARCHAR(10),
    -- 납부자산
    TXPR_MT                      VARCHAR(2),
    -- 번지
    TXPR_ADDR_NO                 VARCHAR(4),
    -- 호
    TXPR_ADDR_HO                 VARCHAR(4),
    -- 특수주소
    TXPR_SPCL_ADDR               VARCHAR(100),
    -- 특수주소동
    TXPR_SPCL_ADDR_DONG          VARCHAR(20),
    -- 특수주소호
    TXPR_SPCL_ADDR_HO            VARCHAR(10),
    -- 통
    TXPR_ADDR_TONG               VARCHAR(3),
    -- 반
    TXPR_ADDR_BAN                VARCHAR(3),
    -- 건물관리번호
    TXPR_BD_MNG_NO               VARCHAR(25) NOT NULL,
    -- 물건지명
    OBJ_NM                       VARCHAR(100) NOT NULL,
    -- 부과대상구분코드
    TAX_OBJ_SP                   VARCHAR(20) NOT NULL,
    -- 물건지새주소여부
    TAX_OBJ_NEW_ADDR_YN          VARCHAR(1) NOT NULL,
    -- 관리항목1
    MNG_HTM1                     VARCHAR(100),
    -- 관리항목2
    MNG_HTM2                     VARCHAR(30),
    -- 관리항목3
    MNG_HTM3                     VARCHAR(100),
    -- 관리항목4
    MNG_HTM4                     VARCHAR(100),
    -- 관리항목5
    MNG_HTM5                     VARCHAR(30),
    -- 관리항목6
    MNG_HTM6                     VARCHAR(30),
    -- 비고
    RMK                          VARCHAR(255),
    -- 최초작업자ID
    INIT_WRKR_ID                 VARCHAR(20),
    -- 은행코드
    BANK_CD                      VARCHAR(7),
    -- 납부자통주소
    TXPR_FULL_ADDR               VARCHAR(300),
    -- 납부자기본주소
    TXPR_BASIC_ADDR              VARCHAR(500),
    -- 납부자상세주소
    TXPR_BASIC_DTL_ADDR          VARCHAR(500),
    -- 연계결과코드
    RESULT_CODE                  VARCHAR(3) NOT NULL,
    -- 연계결과메세지
    RESULT_MSG                   VARCHAR(500),
    -- 부과키
    IMPS_KEY                     VARCHAR(500),
    -- 연계결과값
    RESULT                       VARCHAR(500),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    TXPR_NO                      VARCHAR(1000),
    TXPR_NM                      VARCHAR(1000),
    TXPR_DTL_ADDR                VARCHAR(1000),
    PRIMARY KEY (IF_NO)
);

-- 외부연계 인터페이스_표준세외(현세대) 지자체별 웹서비스 URL
CREATE TABLE IF NOT EXISTS GIF_STND_JIJACHE_NOW (
    -- 지자체코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 웹서비스 URL
    WEB_URL                      VARCHAR(200) NOT NULL,
    PRIMARY KEY (LOCGOV_CODE)
);

-- 표준세외(현세대) 수납자료
CREATE TABLE IF NOT EXISTS GIF_STND_SUNAP_NOW (
    -- 인터페이스 번호
    IF_NO                        VARCHAR(30) NOT NULL,
    -- 전자납부번호
    ELCT_PAY_NO                  VARCHAR(19) NOT NULL,
    -- 전문메세지길이
    COM_MSG_LEN                  VARCHAR(8) NOT NULL,
    -- 인터페이스ID
    COM_IF_ID                    VARCHAR(15) NOT NULL,
    -- source system
    COM_SOURCE                   VARCHAR(11) NOT NULL,
    -- target system
    COM_TARGET                   VARCHAR(11) NOT NULL,
    -- 연계 메세지키
    COM_MSG_KEY                  VARCHAR(38) NOT NULL,
    -- 연계 유형코드
    COM_TYPE_CD                  VARCHAR(3) NOT NULL,
    -- 결과코드
    COM_RST_CD                   VARCHAR(3) NOT NULL,
    -- 기부금연계코드
    CONN_KEY                     VARCHAR(30) NOT NULL,
    -- 연계결과코드
    RESULT_CODE                  VARCHAR(30) NOT NULL,
    -- 연계결과메세지
    RESULT_MSG                   VARCHAR(100),
    -- 연계결과값
    RESULT                       VARCHAR(50),
    -- 수납본세
    RCPT_PRCP_TAX_AMT            INTEGER,
    -- 수납가산금
    RCPT_ADD_AMT                 INTEGER,
    -- 수납분이자
    DIVD_RECPLTT_AMT             INTEGER,
    -- 회계일자
    FIS_DT                       VARCHAR(8),
    -- 소인일자
    WRK_DT                       VARCHAR(8),
    -- 수납구분
    RCPT_SP                      VARCHAR(2),
    -- 수납일자
    RCPT_DT                      VARCHAR(8),
    -- 수납유형
    RCPT_TYP                     VARCHAR(3),
    -- 은행코드
    BANK_CD                      VARCHAR(8),
    -- 묶은번호
    BNDL_NO                      VARCHAR(3),
    -- 이체일자
    TRNR_DT                      VARCHAR(8),
    -- 인터페이스 시작일자
    IF_ST_DT                     TIMESTAMP NOT NULL DEFAULT now(),
    -- 인터페이스 종료일자
    IF_ED_DT                     TIMESTAMP,
    PRIMARY KEY (IF_NO)
);

CREATE TABLE IF NOT EXISTS OP_EVENT_CODE (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CHANGE_CODE                  VARCHAR(100),
    CONTENTS                     VARCHAR(500),
    EVENT_CODE                   VARCHAR(20) NOT NULL,
    REDIRECTION                  BIGINT,
    TYPE                         VARCHAR(10) NOT NULL,
    USER_ID                      BIGINT,
    UTM_QUERY_STRING             VARCHAR(255),
    CAMPAIGN_ID                  BIGINT,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_EVENT_CODE_LOG (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CHANNEL                      VARCHAR(255),
    CODE_TYPE                    VARCHAR(10) NOT NULL,
    EVENT_CODE                   VARCHAR(20),
    EVENT_UID                    VARCHAR(100) NOT NULL,
    ITEM_USER_CODE               VARCHAR(50),
    LOG_DETAIL                   VARCHAR(100),
    LOG_TYPE                     VARCHAR(10) NOT NULL,
    ORDER_CODE                   VARCHAR(50),
    SOURCE_USER_ID               BIGINT,
    UID                          VARCHAR(100),
    USER_ID                      BIGINT,
    UTM_CAMPAIGN                 VARCHAR(255),
    UTM_CONTENT                  VARCHAR(255),
    UTM_ITEM                     VARCHAR(255),
    UTM_MEDIUM                   VARCHAR(255),
    UTM_SOURCE                   VARCHAR(255),
    PRIMARY KEY (ID)
);

-- 이벤트 연관 상품
CREATE TABLE IF NOT EXISTS OP_EVENT_ITEM (
    -- ID
    ID                           BIGINT NOT NULL,
    -- 등록일
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 버전
    VERSION                      BIGINT,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 정렬순서
    ORDERING                     INTEGER NOT NULL,
    -- 이벤트 ID
    EVENT_ID                     BIGINT,
    PRIMARY KEY (ID)
);

-- 이벤트 댓글
CREATE TABLE IF NOT EXISTS OP_EVENT_REPLY (
    -- ID
    ID                           BIGINT NOT NULL,
    -- 등록일
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 버전
    VERSION                      BIGINT,
    -- 댓글 내용
    CONTENT                      VARCHAR(2000) NOT NULL,
    -- 상태(NORMAL:일반, DELETE:삭제)
    DATA_STATUS                  VARCHAR(10) NOT NULL,
    -- 이벤트 ID
    EVENT_ID                     BIGINT NOT NULL,
    -- 유저 ID
    USER_ID                      BIGINT NOT NULL,
    PRIMARY KEY (ID)
);

-- 명예기부자 전시 이력
CREATE TABLE IF NOT EXISTS OP_HONOR_VIEW_HIST (
    -- 전시일시
    VIEW_DT                      TIMESTAMP NOT NULL,
    -- 사용자아이디
    USER_ID                      BIGINT NOT NULL,
    -- 지방자치단체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    PRIMARY KEY (VIEW_DT, USER_ID, LCLGV_CD)
);

-- 도서산간 및 제주지역
CREATE TABLE IF NOT EXISTS OP_ISLAND (
    ID                           BIGINT NOT NULL,
    -- 등록일
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 주소
    ADDRESS                      VARCHAR(255),
    -- 구분(JEJU:제주 ISLAND: 산간)
    ISLAND_TYPE                  VARCHAR(20),
    -- 우편번호
    ZIPCODE                      VARCHAR(7),
    PRIMARY KEY (ID)
);

-- 시도 맵핑
CREATE TABLE IF NOT EXISTS OP_SIDO_MAPPING (
    -- 시도 맵핑 그룹 키
    SIDO_MAPPING_GROUP_KEY       INTEGER,
    -- 시도명
    SIDO_NAME                    VARCHAR(50),
    -- 시도 데이터
    SIDO_DATA                    VARCHAR(50)
);

-- 특별재난선포지역
CREATE TABLE IF NOT EXISTS OP_SPEL_DSTR_ZN (
    ID                           BIGINT NOT NULL,
    -- 대통령공고령
    PSDNT_NOTI_NO                BIGINT NOT NULL,
    -- 상위지자체명
    UPPER_LOCGOV_NM              VARCHAR(30),
    -- 상위지자체코드
    UPPER_LOCGOV_CODE            VARCHAR(10),
    -- 지자체명
    LOCGOV_NM                    VARCHAR(30),
    -- 지자체코드
    LOCGOV_CODE                  VARCHAR(10),
    -- 재난선포사유
    NOTI_REASON                  VARCHAR(50),
    -- 선포일
    NOTI_DATE                    VARCHAR(8),
    -- 종료일
    END_DATE                     VARCHAR(8),
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ID)
);
CREATE SEQUENCE IF NOT EXISTS op_spel_dstr_zn_id_seq START WITH 1000;
ALTER TABLE OP_SPEL_DSTR_ZN ALTER COLUMN ID SET DEFAULT nextval('op_spel_dstr_zn_id_seq');
ALTER SEQUENCE op_spel_dstr_zn_id_seq OWNED BY OP_SPEL_DSTR_ZN.ID;

-- same-service FKs, added after every table exists (avoids creation-order failures)
ALTER TABLE G_CNTR ADD CONSTRAINT fk_g_cntr_cntr_locgov_code FOREIGN KEY (CNTR_LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_CNTR ADD CONSTRAINT fk_g_cntr_psitn_locgov_code FOREIGN KEY (PSITN_LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_CNTR_LMTT ADD CONSTRAINT fk_g_cntr_lmtt_locgov_code FOREIGN KEY (LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_CTBNY_OPRATN ADD CONSTRAINT fk_g_ctbny_opratn_locgov_code FOREIGN KEY (LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_CTBNY_OPRATN_FILE ADD CONSTRAINT fk_g_ctbny_opratn_file_regist_sn FOREIGN KEY (REGIST_SN) REFERENCES G_CTBNY_OPRATN (REGIST_SN);
ALTER TABLE G_HONOR_CNTRBTR ADD CONSTRAINT fk_g_honor_cntrbtr_locgov_code FOREIGN KEY (LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_INTRST_LOCGOV ADD CONSTRAINT fk_g_intrst_locgov_locgov_code FOREIGN KEY (LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_LOCGOV_DEPT_HIST ADD CONSTRAINT fk_g_locgov_dept_hist_locgov_code FOREIGN KEY (LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);
ALTER TABLE G_RELAY_LOG ADD CONSTRAINT fk_g_relay_log_cntr_locgov_code FOREIGN KEY (CNTR_LOCGOV_CODE) REFERENCES G_LOCGOV (LOCGOV_CODE);

-- ===================================================================
-- 기존 파일에서 보존된 내용 (AS-IS에 없는 프로젝트 고유 테이블/컬럼/시퀀스/시드 데이터)
-- ===================================================================

CREATE TABLE IF NOT EXISTS G_CATALOG_CONTENT_IMG_DESC ( -- TODO: primary key unclear; guess composite (CATALOG_CONTENT_ID, IMG_SEQ)
	CATALOG_CONTENT_ID BIGINT,
	IMG_DESC           VARCHAR(500),
	IMG_SEQ            INTEGER,
	FRST_REGISTER_ID   BIGINT,
	FRST_REGIST_PNTTM  TIMESTAMP,
	PRIMARY KEY (CATALOG_CONTENT_ID, IMG_SEQ)
);

CREATE TABLE IF NOT EXISTS G_CATALOG_CONTENT_MNG (
	CATALOG_CONTENT_ID       BIGINT PRIMARY KEY, -- useGeneratedKeys keyProperty="catalogContentId"
	CATALOG_YEAR             INTEGER,
	CATALOG_NO               INTEGER,
	CONTENT_TYPE             VARCHAR(50),
	CONTENT_SUB_TYPE         VARCHAR(50),
	CATALOG_CONTENT_SUBJECT  VARCHAR(255),
	THUMBNAIL_IMG_PATH       VARCHAR(255),
	CATALOG_CONTENT_CN       TEXT,
	THUMBNAIL_IMG_PATH2      VARCHAR(255),
	CATALOG_CONTENT_CN2      TEXT,
	LOCGOV_CODE              VARCHAR(20),
	DISPLAY_YN               CHAR(1),
	BANNER_YN                CHAR(1),
	FRST_REGISTER_ID         BIGINT,
	FRST_REGIST_PNTTM        TIMESTAMP,
	LAST_UPDUSR_ID           BIGINT,
	LAST_UPDT_PNTTM          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS G_CNTR_TAX_TEMP_LOG (
    -- TODO: primary key unclear (append-only log, no explicit ID column in mapper)
    cntr_sn                     VARCHAR(50),
    confirm_user_id             BIGINT,
    elcr_apl_cd                  VARCHAR(10),
    etc                          VARCHAR(255),
    frst_regist_pnttm           TIMESTAMP
);

-- =====================================================================
-- MVP additions for the new donation microservice implementation (SFR-003).
-- G_LOCGOV and G_CNTR above are the AS-IS reverse-engineered tables; the
-- service implementation only populates/reads a subset of their columns.
-- =====================================================================

-- Common code table for donation service (no-hardcoding principle: code-type
-- data lives in DB, not Java enums). Structure mirrors member's OP_COMMON_CODE
-- (see ghlove-msa/database/ddl/service-member.sql) - each service owns its own
-- copy per DB-per-service, not a shared table.
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
('CNTR_STATUS', 'ko', 'REQUESTED', '신청',     1, 'Y'),
('CNTR_STATUS', 'ko', 'COMPLETED', '납부완료', 2, 'Y'),
('CNTR_STATUS', 'ko', 'CANCELLED', '취소',     3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- Seed a handful of local governments so the MVP donation form has real choices
INSERT INTO G_LOCGOV (LOCGOV_CODE, LOCGOV_NM, UPPER_LOCGOV_NM, UPPER_LOCGOV_CODE, USE_AT) VALUES
('11230', '강남구', '서울특별시', '11000', 'Y'),
('26350', '해운대구', '부산광역시', '26000', 'Y'),
('36110', '세종특별자치시', NULL, '36000', 'Y'),
('50000', '제주특별자치도', '제주특별자치도', '50000', 'Y'),
('46150', '순천시', '전라남도', '46000', 'Y')
ON CONFLICT (LOCGOV_CODE) DO NOTHING;

-- =====================================================================
-- Round 2 additions: 지정기부사업 + 연간 기부한도 검증 (SFR-003)
-- =====================================================================

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('DSGN_STATUS', 'ko', 'OPEN',   '진행중', 1, 'Y'),
('DSGN_STATUS', 'ko', 'CLOSED', '종료',   2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- General (system-wide) settings that aren't per-code lookups still belong in
-- the common-code table per the no-hardcoding principle (코드성 데이터/설정값 등),
-- using CODE_VALUE for the actual number instead of a bespoke settings table.
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, CODE_VALUE, ORDERING, USE_YN) VALUES
('SYSTEM_CONFIG', 'ko', 'ANNUAL_TOTAL_LIMIT', '연간 총 기부한도(원, 전체 지자체 합산)', '20000000', 1, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 지자체별 연간 기부한도 (STDR_YEAR, LOCGOV_CODE 별로 다름 — 하드코딩 금지 원칙)
INSERT INTO G_CTBNY_SETUP (STDR_YEAR, LOCGOV_CODE, LMT_AMT, POINT_RATE) VALUES
('2026', '11230', 5000000, 30.00),
('2026', '26350', 3000000, 30.00),
('2026', '36110', 3000000, 30.00),
('2026', '50000', 3000000, 30.00),
('2026', '46150', 3000000, 30.00)
ON CONFLICT (STDR_YEAR, LOCGOV_CODE) DO NOTHING;

-- 지정기부사업 시드 데이터
INSERT INTO G_DSGN_DNTN_BIZ_MNG (DSGN_DNTN_BIZ_TTL, DSGN_DNTN_BIZ_CN, DSGN_DNTN_BIZ_BGNG_YMD, DSGN_DNTN_BIZ_END_YMD, GOAL_AMT, DSGN_DNTN_BIZ_STTS_CD, RLS_YN, LCLGV_CD, FRST_RGTR_ID)
SELECT ttl, cn, bgng, end_, goal, stts, rls, lclgv, 1000 FROM (VALUES
    ('강남구 어린이 도서관 리모델링', '노후 어린이 도서관을 새단장합니다.', '20260101', '20261231', 30000000::BIGINT, 'OPEN', 'Y', '11230'),
    ('해운대 해변 반려동물 놀이터 조성', '반려동물과 함께하는 해변 공원을 만듭니다.', '20260101', '20261231', 15000000::BIGINT, 'OPEN', 'Y', '26350'),
    ('순천만 정원 생태 보전 사업', '순천만 습지 생태계 보전 활동을 지원합니다.', '20260101', '20261231', 20000000::BIGINT, 'OPEN', 'Y', '46150')
) AS seed(ttl, cn, bgng, end_, goal, stts, rls, lclgv)
WHERE NOT EXISTS (SELECT 1 FROM G_DSGN_DNTN_BIZ_MNG);

-- 메인화면 "특정사업에 기부하기" 카드에 실제 이미지가 보이도록 추가한 시드 데이터
-- (2026-08-14 라운드). 이미지는 AS-IS static 리소스에 실제로 존재하던 지정기부사업/재난지원
-- 홍보 이미지를 그대로 재사용한다(가짜 스톡사진이 아님).
INSERT INTO G_LOCGOV (LOCGOV_CODE, LOCGOV_NM, UPPER_LOCGOV_NM, UPPER_LOCGOV_CODE, USE_AT) VALUES
('51110', '춘천시', '강원특별자치도', '51000', 'Y'),
('44790', '청양군', '충청남도', '44000', 'Y'),
('46530', '곡성군', '전라남도', '46000', 'Y')
ON CONFLICT (LOCGOV_CODE) DO NOTHING;

INSERT INTO G_DSGN_DNTN_BIZ_MNG (DSGN_DNTN_BIZ_TTL, DSGN_DNTN_BIZ_CN, DSGN_DNTN_BIZ_BGNG_YMD, DSGN_DNTN_BIZ_END_YMD, GOAL_AMT, DSGN_DNTN_BIZ_STTS_CD, RLS_YN, LCLGV_CD, DSGN_DNTN_BIZ_RPRS_IMG, FRST_RGTR_ID)
SELECT ttl, cn, bgng, end_, goal, stts, rls, lclgv, img, 1000 FROM (VALUES
    ('청양군 수해복구 지원사업', '집중호우로 피해를 입은 주민들의 삶터 복구를 지원합니다.', '20260101', '20261231', 25000000::BIGINT, 'OPEN', 'Y', '44790', '/images/new/202508081741530159_M.jpg'),
    ('춘천시 취약지역 건강돌봄 캠페인', '취약지역 자살 및 고독사 예방을 위한 지역건강돌봄 캠페인입니다.', '20260101', '20261231', 18000000::BIGINT, 'OPEN', 'Y', '51110', '/images/new/202504301133410131_M.jpeg'),
    ('곡성군 배 재배농가 지원사업', '곡성 심청배 재배농가의 안정적인 판로 확보를 지원합니다.', '20260101', '20261231', 12000000::BIGINT, 'OPEN', 'Y', '46530', '/images/new/202508201312560094_M.jpg')
) AS seed(ttl, cn, bgng, end_, goal, stts, rls, lclgv, img)
WHERE (SELECT COUNT(*) FROM G_DSGN_DNTN_BIZ_MNG) < 4;

-- =====================================================================
-- Round 2 additions: 기부확인증 (AS-IS 마이페이지 > 기부확인증, receiptList.html /
-- getReceiptInitInfo·getReceiptPopInfo). G_LOCGOV.UPPER_LOCGOV_CODE and
-- BIZRNO already existed as empty AS-IS columns (batch-generated schema,
-- never seeded) - backfill them so the certificate's 시·도 filter and
-- "(사업자번호 : ...)" line have real values. UPPER_LOCGOV_CODE follows the
-- real 행정표준코드 시/도 코드(5자리, 예: 서울='11000')를 그대로 따른다 - Round X의
-- G_LOCGOV 전량 시딩(시/도 243건)도 동일하게 5자리를 쓰므로 여기서도 맞춘다
-- (전에는 2자리로 잘못 백필해서 두 라운드 데이터의 UPPER_LOCGOV_CODE 표현이 서로
-- 달랐다 - 장바구니 GNB "지자체몰 선택하기" 지도 팝업이 upperLocgovCode로
-- 그룹핑하면서 드러남).
-- =====================================================================
UPDATE G_LOCGOV SET UPPER_LOCGOV_CODE = SUBSTRING(LOCGOV_CODE, 1, 2) || '000'
WHERE UPPER_LOCGOV_CODE IS NULL OR UPPER_LOCGOV_CODE = '' OR LENGTH(UPPER_LOCGOV_CODE) = 2;

UPDATE G_LOCGOV SET BIZRNO = CASE LOCGOV_CODE
    WHEN '11230' THEN '113-83-00512'
    WHEN '26350' THEN '609-83-00219'
    WHEN '36110' THEN '308-83-00107'
    WHEN '50000' THEN '410-83-00033'
    WHEN '46150' THEN '206-83-00441'
    ELSE BIZRNO
END
WHERE BIZRNO IS NULL OR BIZRNO = '';

INSERT INTO OP_SPEL_DSTR_ZN (PSDNT_NOTI_NO, UPPER_LOCGOV_NM, UPPER_LOCGOV_CODE, LOCGOV_NM, LOCGOV_CODE, NOTI_REASON, NOTI_DATE, END_DATE)
SELECT 34521, '서울특별시', '11000', '강남구', '11230', '집중호우 특별재난지역 선포', '20260101', '20261231'
WHERE NOT EXISTS (SELECT 1 FROM OP_SPEL_DSTR_ZN);

-- =====================================================================
-- Round 3: 외부 연계시스템 통합 지점 (세외수입 부과/수납, 국세청 홈택스 전자기부금영수증).
-- AS-IS NgDonationRelayServiceImpl(sntrBugaInsert/contryBugaInsert,
-- etaxSunapInfo/contrySunapInfo, sendNtsEreceipt)의 실제 요청/응답 흐름은
-- 재현하되, 방화벽이 열리기 전까지는 LocalTaxClient/NtsClient가
-- ghlove.integrations.*.enabled=false일 때 모크 응답으로 동작한다 (완료 처리
-- 자체는 지금과 동일하게 계속 성공). 1 기부건(CNTR_SN) : 1 추적행.
-- =====================================================================
CREATE TABLE IF NOT EXISTS DONATION_LEVY (
    CNTR_SN               VARCHAR(50) PRIMARY KEY,
    BUGA_NO               VARCHAR(50),
    BUGA_DATE             TIMESTAMP,
    SUNAP_YN              CHAR(1),
    SUNAP_DATE            TIMESTAMP,
    NTS_STATUS            VARCHAR(20),
    NTS_RECEIPT_NO        VARCHAR(50),
    NTS_REGISTERED_DATE   TIMESTAMP,
    NTS_ERROR_MESSAGE     VARCHAR(500)
);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('NTS_STATUS', 'ko', 'PENDING',    '등록대기', 1, 'Y'),
('NTS_STATUS', 'ko', 'REGISTERED', '등록완료', 2, 'Y'),
('NTS_STATUS', 'ko', 'FAILED',     '등록실패', 3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 4 (SFR-003 gap fill): 기탁서·오프라인 기부 등록, 기부혜택(소득공제) 관리·발급.
-- CNTR_PATH_CODE는 AS-IS에 이미 있던 컬럼이지만 공통코드가 한 번도 채워진 적이
-- 없었다(지금까지 DonationService가 "ONLINE" 문자열을 그냥 하드코딩) - 이번에
-- 같이 바로잡는다. RCEPT_BANK_CODE/RCEPT_BANK_NM도 AS-IS 컬럼이 있었지만
-- 매핑되지 않은 상태였고, 오프라인(기탁서) 접수 시 입금 정보 메모 용도로 쓴다.
-- =====================================================================
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('CNTR_PATH', 'ko', 'ONLINE',  '온라인', 1, 'Y'),
('CNTR_PATH', 'ko', 'OFFLINE', '오프라인(기탁서)', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 지자체별 기부혜택 안내문구 (국세청 세액공제는 전국 공통 법정 비율이라 계산은
-- 서비스 로직으로 하고, 이 테이블은 지자체가 추가로 안내하고 싶은 문구만 관리한다)
CREATE TABLE IF NOT EXISTS G_HONOR_BENEFIT (
    LOCGOV_CODE    VARCHAR(50) PRIMARY KEY,
    BENEFIT_DESC   TEXT,
    UPDATED_DATE   TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO G_HONOR_BENEFIT (LOCGOV_CODE, BENEFIT_DESC) VALUES
('11230', '강남구에 기부하신 분께는 매년 상반기 지역 축제 초청권을 추가로 드립니다.')
ON CONFLICT (LOCGOV_CODE) DO NOTHING;

-- 메인화면 "총 기부금" 위젯의 "전년 동기 대비 %" 계산용 2025년 더미 데이터(테스트 데이터,
-- 실제 발생 기부 아님) - ghlove-msa는 아직 2026년에 시작해 실제 전년도 데이터가 없다.
INSERT INTO G_CNTR (CNTR_SN, CNTR_DE, USER_ID, PSITN_LOCGOV_CODE, CNTR_LOCGOV_CODE, CNTR_AMT, CNTR_PATH_CODE, CNTR_STTUS_CODE, ELCTRN_PAY_NO, FRST_REGIST_PNTTM)
VALUES
('D202501151030001000', '20250115', 1000, '11230', '11230', 1500000.00, 'ONLINE', 'COMPLETED', 'D202501151030001000', '20250115103000'),
('D202502101030001001', '20250210', 1001, '26350', '26350', 800000.00, 'ONLINE', 'COMPLETED', 'D202502101030001001', '20250210103000'),
('D202503051030001002', '20250305', 1002, '46150', '46150', 1200000.00, 'ONLINE', 'COMPLETED', 'D202503051030001002', '20250305103000'),
('D202504201030001000', '20250420', 1000, '50000', '50000', 900000.00, 'ONLINE', 'COMPLETED', 'D202504201030001000', '20250420103000'),
('D202505121030001001', '20250512', 1001, '11230', '11230', 2000000.00, 'ONLINE', 'COMPLETED', 'D202505121030001001', '20250512103000'),
('D202506181030001002', '20250618', 1002, '26350', '26350', 1100000.00, 'ONLINE', 'COMPLETED', 'D202506181030001002', '20250618103000'),
('D202507251030001000', '20250725', 1000, '46150', '46150', 1090000.00, 'ONLINE', 'COMPLETED', 'D202507251030001000', '20250725103000'),
('D202508031030001001', '20250803', 1001, '11230', '11230', 1100000.00, 'ONLINE', 'COMPLETED', 'D202508031030001001', '20250803103000')
ON CONFLICT (CNTR_SN) DO NOTHING;

-- =====================================================================
-- Round X: 답례품몰 장바구니 GNB '지자체몰 선택하기' 지도 팝업 - 이전까지는
-- 테스트용 8건뿐이라 지도에서 지역 클릭해도 시군구 목록이 거의 비어보였다.
-- 실제 대한민국 행정구역 전체(17개 시도 + 산하 시군구, 243건)를 라이브
-- 사이트(ilovegohyang.go.kr)의 실제 GET /api/ngdonation/sigunguList 응답을
-- 그대로 가져와 시딩한다 - 고정된 공개 행정구역 데이터라 정확성이 중요하고,
-- AS-IS 로컬 소스에는 이 데이터가 없다(운영 DB 전용).
-- =====================================================================
INSERT INTO G_LOCGOV (LOCGOV_CODE, LOCGOV_NM, UPPER_LOCGOV_NM, UPPER_LOCGOV_CODE, USE_AT) VALUES
('11000', '시청', '서울특별시', '11000', 'Y'),
('11110', '종로구', '서울특별시', '11000', 'Y'),
('11140', '중구', '서울특별시', '11000', 'Y'),
('11170', '용산구', '서울특별시', '11000', 'Y'),
('11200', '성동구', '서울특별시', '11000', 'Y'),
('11215', '광진구', '서울특별시', '11000', 'Y'),
('11230', '동대문구', '서울특별시', '11000', 'Y'),
('11260', '중랑구', '서울특별시', '11000', 'Y'),
('11290', '성북구', '서울특별시', '11000', 'Y'),
('11305', '강북구', '서울특별시', '11000', 'Y'),
('11320', '도봉구', '서울특별시', '11000', 'Y'),
('11350', '노원구', '서울특별시', '11000', 'Y'),
('11380', '은평구', '서울특별시', '11000', 'Y'),
('11410', '서대문구', '서울특별시', '11000', 'Y'),
('11440', '마포구', '서울특별시', '11000', 'Y'),
('11470', '양천구', '서울특별시', '11000', 'Y'),
('11500', '강서구', '서울특별시', '11000', 'Y'),
('11530', '구로구', '서울특별시', '11000', 'Y'),
('11545', '금천구', '서울특별시', '11000', 'Y'),
('11560', '영등포구', '서울특별시', '11000', 'Y'),
('11590', '동작구', '서울특별시', '11000', 'Y'),
('11620', '관악구', '서울특별시', '11000', 'Y'),
('11650', '서초구', '서울특별시', '11000', 'Y'),
('11680', '강남구', '서울특별시', '11000', 'Y'),
('11710', '송파구', '서울특별시', '11000', 'Y'),
('11740', '강동구', '서울특별시', '11000', 'Y'),
('12000', '시청', '전남광주통합특별시', '12000', 'Y'),
('12110', '목포시', '전남광주통합특별시', '12000', 'Y'),
('12130', '여수시', '전남광주통합특별시', '12000', 'Y'),
('12150', '순천시', '전남광주통합특별시', '12000', 'Y'),
('12170', '나주시', '전남광주통합특별시', '12000', 'Y'),
('12190', '광양시', '전남광주통합특별시', '12000', 'Y'),
('12210', '동구', '전남광주통합특별시', '12000', 'Y'),
('12240', '서구', '전남광주통합특별시', '12000', 'Y'),
('12270', '남구', '전남광주통합특별시', '12000', 'Y'),
('12300', '북구', '전남광주통합특별시', '12000', 'Y'),
('12330', '광산구', '전남광주통합특별시', '12000', 'Y'),
('12710', '담양군', '전남광주통합특별시', '12000', 'Y'),
('12720', '곡성군', '전남광주통합특별시', '12000', 'Y'),
('12730', '구례군', '전남광주통합특별시', '12000', 'Y'),
('12740', '고흥군', '전남광주통합특별시', '12000', 'Y'),
('12750', '보성군', '전남광주통합특별시', '12000', 'Y'),
('12760', '화순군', '전남광주통합특별시', '12000', 'Y'),
('12770', '장흥군', '전남광주통합특별시', '12000', 'Y'),
('12780', '강진군', '전남광주통합특별시', '12000', 'Y'),
('12790', '해남군', '전남광주통합특별시', '12000', 'Y'),
('12800', '영암군', '전남광주통합특별시', '12000', 'Y'),
('12810', '무안군', '전남광주통합특별시', '12000', 'Y'),
('12820', '함평군', '전남광주통합특별시', '12000', 'Y'),
('12830', '영광군', '전남광주통합특별시', '12000', 'Y'),
('12840', '장성군', '전남광주통합특별시', '12000', 'Y'),
('12850', '완도군', '전남광주통합특별시', '12000', 'Y'),
('12860', '진도군', '전남광주통합특별시', '12000', 'Y'),
('12870', '신안군', '전남광주통합특별시', '12000', 'Y'),
('26000', '시청', '부산광역시', '26000', 'Y'),
('26110', '중구', '부산광역시', '26000', 'Y'),
('26140', '서구', '부산광역시', '26000', 'Y'),
('26170', '동구', '부산광역시', '26000', 'Y'),
('26200', '영도구', '부산광역시', '26000', 'Y'),
('26230', '부산진구', '부산광역시', '26000', 'Y'),
('26260', '동래구', '부산광역시', '26000', 'Y'),
('26290', '남구', '부산광역시', '26000', 'Y'),
('26320', '북구', '부산광역시', '26000', 'Y'),
('26350', '해운대구', '부산광역시', '26000', 'Y'),
('26380', '사하구', '부산광역시', '26000', 'Y'),
('26410', '금정구', '부산광역시', '26000', 'Y'),
('26440', '강서구', '부산광역시', '26000', 'Y'),
('26470', '연제구', '부산광역시', '26000', 'Y'),
('26500', '수영구', '부산광역시', '26000', 'Y'),
('26530', '사상구', '부산광역시', '26000', 'Y'),
('26710', '기장군', '부산광역시', '26000', 'Y'),
('27000', '시청', '대구광역시', '27000', 'Y'),
('27110', '중구', '대구광역시', '27000', 'Y'),
('27140', '동구', '대구광역시', '27000', 'Y'),
('27170', '서구', '대구광역시', '27000', 'Y'),
('27200', '남구', '대구광역시', '27000', 'Y'),
('27230', '북구', '대구광역시', '27000', 'Y'),
('27260', '수성구', '대구광역시', '27000', 'Y'),
('27290', '달서구', '대구광역시', '27000', 'Y'),
('27710', '달성군', '대구광역시', '27000', 'Y'),
('27720', '군위군', '대구광역시', '27000', 'Y'),
('28000', '시청', '인천광역시', '28000', 'Y'),
('28710', '강화군', '인천광역시', '28000', 'Y'),
('28720', '옹진군', '인천광역시', '28000', 'Y'),
('28125', '제물포구', '인천광역시', '28000', 'Y'),
('28155', '영종구', '인천광역시', '28000', 'Y'),
('28177', '미추홀구', '인천광역시', '28000', 'Y'),
('28185', '연수구', '인천광역시', '28000', 'Y'),
('28200', '남동구', '인천광역시', '28000', 'Y'),
('28237', '부평구', '인천광역시', '28000', 'Y'),
('28245', '계양구', '인천광역시', '28000', 'Y'),
('28275', '서해구', '인천광역시', '28000', 'Y'),
('28290', '검단구', '인천광역시', '28000', 'Y'),
('30000', '시청', '대전광역시', '30000', 'Y'),
('30110', '동구', '대전광역시', '30000', 'Y'),
('30140', '중구', '대전광역시', '30000', 'Y'),
('30170', '서구', '대전광역시', '30000', 'Y'),
('30200', '유성구', '대전광역시', '30000', 'Y'),
('30230', '대덕구', '대전광역시', '30000', 'Y'),
('31000', '시청', '울산광역시', '31000', 'Y'),
('31110', '중구', '울산광역시', '31000', 'Y'),
('31140', '남구', '울산광역시', '31000', 'Y'),
('31170', '동구', '울산광역시', '31000', 'Y'),
('31200', '북구', '울산광역시', '31000', 'Y'),
('31710', '울주군', '울산광역시', '31000', 'Y'),
('36000', '시청', '세종특별자치시', '36000', 'Y'),
('41000', '도청', '경기도', '41000', 'Y'),
('41110', '수원시', '경기도', '41000', 'Y'),
('41130', '성남시', '경기도', '41000', 'Y'),
('41150', '의정부시', '경기도', '41000', 'Y'),
('41170', '안양시', '경기도', '41000', 'Y'),
('41190', '부천시', '경기도', '41000', 'Y'),
('41210', '광명시', '경기도', '41000', 'Y'),
('41220', '평택시', '경기도', '41000', 'Y'),
('41250', '동두천시', '경기도', '41000', 'Y'),
('41270', '안산시', '경기도', '41000', 'Y'),
('41280', '고양시', '경기도', '41000', 'Y'),
('41290', '과천시', '경기도', '41000', 'Y'),
('41310', '구리시', '경기도', '41000', 'Y'),
('41360', '남양주시', '경기도', '41000', 'Y'),
('41370', '오산시', '경기도', '41000', 'Y'),
('41390', '시흥시', '경기도', '41000', 'Y'),
('41410', '군포시', '경기도', '41000', 'Y'),
('41430', '의왕시', '경기도', '41000', 'Y'),
('41450', '하남시', '경기도', '41000', 'Y'),
('41460', '용인시', '경기도', '41000', 'Y'),
('41480', '파주시', '경기도', '41000', 'Y'),
('41500', '이천시', '경기도', '41000', 'Y'),
('41550', '안성시', '경기도', '41000', 'Y'),
('41570', '김포시', '경기도', '41000', 'Y'),
('41590', '화성시', '경기도', '41000', 'Y'),
('41610', '광주시', '경기도', '41000', 'Y'),
('41630', '양주시', '경기도', '41000', 'Y'),
('41650', '포천시', '경기도', '41000', 'Y'),
('41670', '여주시', '경기도', '41000', 'Y'),
('41800', '연천군', '경기도', '41000', 'Y'),
('41820', '가평군', '경기도', '41000', 'Y'),
('41830', '양평군', '경기도', '41000', 'Y'),
('43000', '도청', '충청북도', '43000', 'Y'),
('43110', '청주시', '충청북도', '43000', 'Y'),
('43130', '충주시', '충청북도', '43000', 'Y'),
('43150', '제천시', '충청북도', '43000', 'Y'),
('43720', '보은군', '충청북도', '43000', 'Y'),
('43730', '옥천군', '충청북도', '43000', 'Y'),
('43740', '영동군', '충청북도', '43000', 'Y'),
('43745', '증평군', '충청북도', '43000', 'Y'),
('43750', '진천군', '충청북도', '43000', 'Y'),
('43760', '괴산군', '충청북도', '43000', 'Y'),
('43770', '음성군', '충청북도', '43000', 'Y'),
('43800', '단양군', '충청북도', '43000', 'Y'),
('44000', '도청', '충청남도', '44000', 'Y'),
('44130', '천안시', '충청남도', '44000', 'Y'),
('44150', '공주시', '충청남도', '44000', 'Y'),
('44180', '보령시', '충청남도', '44000', 'Y'),
('44200', '아산시', '충청남도', '44000', 'Y'),
('44210', '서산시', '충청남도', '44000', 'Y'),
('44230', '논산시', '충청남도', '44000', 'Y'),
('44250', '계룡시', '충청남도', '44000', 'Y'),
('44710', '금산군', '충청남도', '44000', 'Y'),
('44760', '부여군', '충청남도', '44000', 'Y'),
('44770', '서천군', '충청남도', '44000', 'Y'),
('44790', '청양군', '충청남도', '44000', 'Y'),
('44800', '홍성군', '충청남도', '44000', 'Y'),
('44810', '예산군', '충청남도', '44000', 'Y'),
('44825', '태안군', '충청남도', '44000', 'Y'),
('44270', '당진시', '충청남도', '44000', 'Y'),
('47000', '도청', '경상북도', '47000', 'Y'),
('47110', '포항시', '경상북도', '47000', 'Y'),
('47130', '경주시', '경상북도', '47000', 'Y'),
('47150', '김천시', '경상북도', '47000', 'Y'),
('47170', '안동시', '경상북도', '47000', 'Y'),
('47190', '구미시', '경상북도', '47000', 'Y'),
('47210', '영주시', '경상북도', '47000', 'Y'),
('47230', '영천시', '경상북도', '47000', 'Y'),
('47250', '상주시', '경상북도', '47000', 'Y'),
('47280', '문경시', '경상북도', '47000', 'Y'),
('47290', '경산시', '경상북도', '47000', 'Y'),
('47730', '의성군', '경상북도', '47000', 'Y'),
('47750', '청송군', '경상북도', '47000', 'Y'),
('47760', '영양군', '경상북도', '47000', 'Y'),
('47770', '영덕군', '경상북도', '47000', 'Y'),
('47820', '청도군', '경상북도', '47000', 'Y'),
('47830', '고령군', '경상북도', '47000', 'Y'),
('47840', '성주군', '경상북도', '47000', 'Y'),
('47850', '칠곡군', '경상북도', '47000', 'Y'),
('47900', '예천군', '경상북도', '47000', 'Y'),
('47920', '봉화군', '경상북도', '47000', 'Y'),
('47930', '울진군', '경상북도', '47000', 'Y'),
('47940', '울릉군', '경상북도', '47000', 'Y'),
('48000', '도청', '경상남도', '48000', 'Y'),
('48120', '창원시', '경상남도', '48000', 'Y'),
('48170', '진주시', '경상남도', '48000', 'Y'),
('48220', '통영시', '경상남도', '48000', 'Y'),
('48240', '사천시', '경상남도', '48000', 'Y'),
('48250', '김해시', '경상남도', '48000', 'Y'),
('48270', '밀양시', '경상남도', '48000', 'Y'),
('48310', '거제시', '경상남도', '48000', 'Y'),
('48330', '양산시', '경상남도', '48000', 'Y'),
('48720', '의령군', '경상남도', '48000', 'Y'),
('48730', '함안군', '경상남도', '48000', 'Y'),
('48740', '창녕군', '경상남도', '48000', 'Y'),
('48820', '고성군', '경상남도', '48000', 'Y'),
('48840', '남해군', '경상남도', '48000', 'Y'),
('48850', '하동군', '경상남도', '48000', 'Y'),
('48860', '산청군', '경상남도', '48000', 'Y'),
('48870', '함양군', '경상남도', '48000', 'Y'),
('48880', '거창군', '경상남도', '48000', 'Y'),
('48890', '합천군', '경상남도', '48000', 'Y'),
('50000', '도청', '제주특별자치도', '50000', 'Y'),
('51000', '도청', '강원특별자치도', '51000', 'Y'),
('51110', '춘천시', '강원특별자치도', '51000', 'Y'),
('51130', '원주시', '강원특별자치도', '51000', 'Y'),
('51150', '강릉시', '강원특별자치도', '51000', 'Y'),
('51170', '동해시', '강원특별자치도', '51000', 'Y'),
('51190', '태백시', '강원특별자치도', '51000', 'Y'),
('51210', '속초시', '강원특별자치도', '51000', 'Y'),
('51230', '삼척시', '강원특별자치도', '51000', 'Y'),
('51720', '홍천군', '강원특별자치도', '51000', 'Y'),
('51730', '횡성군', '강원특별자치도', '51000', 'Y'),
('51750', '영월군', '강원특별자치도', '51000', 'Y'),
('51760', '평창군', '강원특별자치도', '51000', 'Y'),
('51770', '정선군', '강원특별자치도', '51000', 'Y'),
('51780', '철원군', '강원특별자치도', '51000', 'Y'),
('51790', '화천군', '강원특별자치도', '51000', 'Y'),
('51800', '양구군', '강원특별자치도', '51000', 'Y'),
('51810', '인제군', '강원특별자치도', '51000', 'Y'),
('51820', '고성군', '강원특별자치도', '51000', 'Y'),
('51830', '양양군', '강원특별자치도', '51000', 'Y'),
('52000', '도청', '전북특별자치도', '52000', 'Y'),
('52110', '전주시', '전북특별자치도', '52000', 'Y'),
('52130', '군산시', '전북특별자치도', '52000', 'Y'),
('52140', '익산시', '전북특별자치도', '52000', 'Y'),
('52180', '정읍시', '전북특별자치도', '52000', 'Y'),
('52190', '남원시', '전북특별자치도', '52000', 'Y'),
('52210', '김제시', '전북특별자치도', '52000', 'Y'),
('52710', '완주군', '전북특별자치도', '52000', 'Y'),
('52720', '진안군', '전북특별자치도', '52000', 'Y'),
('52730', '무주군', '전북특별자치도', '52000', 'Y'),
('52740', '장수군', '전북특별자치도', '52000', 'Y'),
('52750', '임실군', '전북특별자치도', '52000', 'Y'),
('52770', '순창군', '전북특별자치도', '52000', 'Y'),
('52790', '고창군', '전북특별자치도', '52000', 'Y'),
('52800', '부안군', '전북특별자치도', '52000', 'Y')
ON CONFLICT (LOCGOV_CODE) DO NOTHING;

-- =====================================================================
-- Round Y (마이페이지 상세화면 7종 중 "기부혜택증"): G_LOCGOV.STDR_1/2/3LEVEL_AMT는
-- 이미 배치 스캔 구간에 정의돼 있었지만(명예기부자 등급 임계값) 지금까지 어떤 서비스도
-- 채워넣거나 읽지 않았다 - 실제 컬럼을 활용해 등급을 산정한다(발명한 규칙 아님).
-- 초기 테스트 시드 5개 지자체에만 값을 채운다(나머지 243개 Round X 지자체는 임계값이
-- NULL로 남아 기부혜택증 대상에서 조용히 제외된다 - upsertHonorTier()가 NULL이면
-- 스킵하도록 짜여있음).
-- =====================================================================
UPDATE G_LOCGOV SET STDR_1LEVEL_AMT = 100000, STDR_2LEVEL_AMT = 500000, STDR_3LEVEL_AMT = 1000000
WHERE LOCGOV_CODE IN ('11230', '26350', '36110', '50000', '46150');

-- HONOR_STD ("09. 공통코드 목록.xlsx" 실제 AS-IS 값으로 소급 정정) - 자체발명
-- LEVEL1/2/3("동백"/"골드"/"명예")였는데 실제 공통코드는 100/200/300(우수/최우수/특급).
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('HONOR_STD', 'ko', '100', '우수', 1, 'Y'),
('HONOR_STD', 'ko', '200', '최우수',  2, 'Y'),
('HONOR_STD', 'ko', '300', '특급',  3, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 이미 위에서 시딩된 2025년 G_CNTR COMPLETED 기부(userId 1000~1002)를 upsertHonorTier()
-- 로직대로 직접 계산해 채운 값 - 예: 1000번이 11230에 1,500,000원(2025) 완료했으니
-- STDR_3LEVEL_AMT(100만원) 이상 → 300(특급).
INSERT INTO G_HONOR_CNTRBTR (STDR_YEAR, LOCGOV_CODE, USER_ID, HONOR_CNTRBTR_LEVEL_CODE, FRST_REGIST_PNTTM) VALUES
(2025, '11230', 1000, '300', to_timestamp('20250115103000','YYYYMMDDHH24MISS')),
(2025, '50000', 1000, '200', to_timestamp('20250420103000','YYYYMMDDHH24MISS')),
(2025, '26350', 1001, '200', to_timestamp('20250210103000','YYYYMMDDHH24MISS')),
(2025, '11230', 1001, '300', to_timestamp('20250512103000','YYYYMMDDHH24MISS')),
(2025, '46150', 1002, '300', to_timestamp('20250305103000','YYYYMMDDHH24MISS'))
ON CONFLICT (STDR_YEAR, LOCGOV_CODE, USER_ID) DO NOTHING;

-- =====================================================================
-- Round Z (마이페이지 상세화면 7종 중 "관심지자체"): G_INTRST_LOCGOV는 이미 배치 스캔
-- 구간에 정의돼 있었지만 어떤 서비스도 읽거나 쓰지 않았다.
-- =====================================================================
INSERT INTO G_INTRST_LOCGOV (LOCGOV_CODE, USER_ID, REGIST_DE) VALUES
('11230', 1000, '20250115'),
('50000', 1000, '20250420'),
('26350', 1001, '20250210'),
('46150', 1002, '20250305'),
('36110', 1002, '20260701')
ON CONFLICT (LOCGOV_CODE, USER_ID) DO NOTHING;

-- =====================================================================
-- Round AA (특정사업에 기부하기 목록/상세): AS-IS designated-donation/index-main.html은
-- "사업구분" 필터를 갖고 있다. 로컬 AS-IS 소스의 prjList는 주석 처리된 구버전 라벨이라
-- 운영중인 홈페이지(ilovegohyang.go.kr/designated-donation/index-main.html)에서 실제
-- 렌더링된 드롭다운 텍스트를 그대로 가져왔다(괄호 부연설명·가운뎃점 표기까지 동일).
-- DSGN_DNTN_BIZ_SE_CD 컬럼은 배치 스캔 구간에 이미 있었지만 지금까지 어떤 서비스도
-- 채우지 않았다.
-- =====================================================================
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('DSGN_BSNS_TYPE', 'ko', '100', '사회적 취약계층의 지원 및 청소년의 육성ㆍ보호', 1, 'Y'),
('DSGN_BSNS_TYPE', 'ko', '200', '지역 주민의 문화ㆍ예술ㆍ보건 등의 증진', 2, 'Y'),
('DSGN_BSNS_TYPE', 'ko', '300', '시민참여, 자원봉사 등 지역공동체 활성화 지원(주민참여형 사업)', 3, 'Y'),
('DSGN_BSNS_TYPE', 'ko', '400', '그 밖에 주민의 복리 증진에 필요한 사업의 추진(취약계층, 문화ㆍ예술ㆍ보건, 자원봉사에 포함되지 않는 부문)', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 기존 6개 사업을 실제 사업 내용에 맞춰 분류(발명한 값이 아니라 이미 있는 CN 기준 분류)
UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '200' WHERE DSGN_DNTN_BIZ_TTL = '강남구 어린이 도서관 리모델링';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '400' WHERE DSGN_DNTN_BIZ_TTL = '해운대 해변 반려동물 놀이터 조성';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '400' WHERE DSGN_DNTN_BIZ_TTL = '순천만 정원 생태 보전 사업';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '100' WHERE DSGN_DNTN_BIZ_TTL = '청양군 수해복구 지원사업';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '100' WHERE DSGN_DNTN_BIZ_TTL = '춘천시 취약지역 건강돌봄 캠페인';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_SE_CD = '300' WHERE DSGN_DNTN_BIZ_TTL = '곡성군 배 재배농가 지원사업';

-- G_LOCGOV에 이름이 같은 지자체가 두 코드로 중복 존재하는 경우(예전 5개짜리 테스트 시드 vs
-- 이후 실제 행정구역 243행 시드)가 있는데, "지자체별 검색" 지도 팝업(LocgovMapProvince.ALL)은
-- 항상 새 시드 쪽 코드만 보여준다. 위 3개 사업은 지어질 당시 예전 코드를 썼던 탓에 지도
-- 팝업으로는 절대 조회되지 않는 상태였다 - 실사용자가 발견(2026-08-19). 지역명/표시값은
-- 그대로고(강남구는 완전 동일, 순천시·곡성군은 상위지역 표기가 "전라남도"에서 "전남광주통합
-- 특별시"로 바뀜 - 두 표기 다 이미 이 프로젝트에 존재하는 실제 지자체명) 지도로 실제 클릭
-- 가능한 코드로만 맞춘 것 - G_LOCGOV 중복 자체는 건드리지 않는다(더 큰 별도 작업).
UPDATE G_DSGN_DNTN_BIZ_MNG SET LCLGV_CD = '11680' WHERE DSGN_DNTN_BIZ_TTL = '강남구 어린이 도서관 리모델링';

UPDATE G_DSGN_DNTN_BIZ_MNG SET LCLGV_CD = '12150' WHERE DSGN_DNTN_BIZ_TTL = '순천만 정원 생태 보전 사업';

UPDATE G_DSGN_DNTN_BIZ_MNG SET LCLGV_CD = '12720' WHERE DSGN_DNTN_BIZ_TTL = '곡성군 배 재배농가 지원사업';

-- 목록 화면의 "종료" 상태 필터를 실제로 확인할 수 있도록 종료된 사업 1건 시드
-- (이미지는 AS-IS static 리소스에 실제로 있던 홍보 이미지 재사용, 새로 만든 것이 아님).
INSERT INTO G_DSGN_DNTN_BIZ_MNG (DSGN_DNTN_BIZ_TTL, DSGN_DNTN_BIZ_CN, DSGN_DNTN_BIZ_BGNG_YMD, DSGN_DNTN_BIZ_END_YMD, GOAL_AMT, DSGN_DNTN_BIZ_STTS_CD, RLS_YN, LCLGV_CD, DSGN_DNTN_BIZ_SE_CD, DSGN_DNTN_BIZ_RPRS_IMG, FRST_RGTR_ID)
SELECT '거창군 산불피해 복구 지원사업', '대형 산불로 피해를 입은 임야와 농가의 복구를 지원합니다.', '20250301', '20250831', 20000000::BIGINT, 'CLOSED', 'Y', '48880', '100', '/images/new/202507181736420436_M.png', 1000
WHERE NOT EXISTS (SELECT 1 FROM G_DSGN_DNTN_BIZ_MNG WHERE DSGN_DNTN_BIZ_TTL = '거창군 산불피해 복구 지원사업');

-- 페이지네이션 실동작 확인용 100건 시드 - 실제 존재하는 지자체(G_LOCGOV 243행 시드) 100곳을
-- 무작위 샘플링해 4개 사업구분에 고르게 분산했다(대략 진행중 80건/종료 20건). 프로젝트명은
-- 실제 지자체명 기반으로 지어낸 것이지만, 지자체 코드/이름 자체는 실존 데이터다.
INSERT INTO G_DSGN_DNTN_BIZ_MNG (DSGN_DNTN_BIZ_TTL, DSGN_DNTN_BIZ_CN, DSGN_DNTN_BIZ_BGNG_YMD, DSGN_DNTN_BIZ_END_YMD, GOAL_AMT, DSGN_DNTN_BIZ_STTS_CD, RLS_YN, LCLGV_CD, DSGN_DNTN_BIZ_SE_CD, FRST_RGTR_ID)
SELECT ttl, cn, bgng, end_, goal, stts, rls, lclgv, se_cd, 1000 FROM (VALUES
('진도군 독거노인 돌봄 지원사업', '진도군 지역 독거노인의 안전한 생활을 지원합니다.', '20250101', '20250630', 5000000::BIGINT, 'CLOSED', 'Y', '12860', '100'),
('해남군 문화의집 시설개선사업', '해남군 주민 문화공간인 문화의집 시설을 개선합니다.', '20260101', '20261231', 5370000::BIGINT, 'OPEN', 'Y', '12790', '200'),
('부산광역시 북구 청년 창업지원 플랫폼 구축사업', '부산광역시 북구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 5740000::BIGINT, 'OPEN', 'Y', '26320', '300'),
('의성군 반려동물 놀이터 조성사업', '의성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.', '20260101', '20261231', 6110000::BIGINT, 'OPEN', 'Y', '47730', '400'),
('청양군 아동급식 지원사업', '청양군 결식 우려 아동에게 급식을 지원합니다.', '20260101', '20261231', 6480000::BIGINT, 'OPEN', 'Y', '44790', '100'),
('영덕군 작은도서관 리모델링사업', '영덕군 노후 작은도서관을 새단장합니다.', '20250101', '20250630', 6850000::BIGINT, 'CLOSED', 'Y', '47770', '200'),
('산청군 청년 창업지원 플랫폼 구축사업', '산청군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 7220000::BIGINT, 'OPEN', 'Y', '48860', '300'),
('청주시 재난안전 대피시설 확충사업', '청주시 재난 발생 시 주민 대피시설을 확충합니다.', '20260101', '20261231', 7590000::BIGINT, 'OPEN', 'Y', '43110', '400'),
('대구광역시 동구 장애인 이동지원 차량 지원사업', '대구광역시 동구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.', '20260101', '20261231', 7960000::BIGINT, 'OPEN', 'Y', '27140', '100'),
('안동시 체육시설 현대화사업', '안동시 노후 체육시설을 현대화합니다.', '20260101', '20261231', 8330000::BIGINT, 'OPEN', 'Y', '47170', '200'),
('강동구 청년 창업지원 플랫폼 구축사업', '강동구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20250101', '20250630', 8700000::BIGINT, 'CLOSED', 'Y', '11740', '300'),
('부산광역시 중구 가로등 LED 교체사업', '부산광역시 중구 노후 가로등을 LED로 교체해 안전을 개선합니다.', '20260101', '20261231', 9070000::BIGINT, 'OPEN', 'Y', '26110', '400'),
('계룡시 취약계층 겨울나기 지원사업', '계룡시 취약계층 가구의 난방비와 방한용품을 지원합니다.', '20260101', '20261231', 9440000::BIGINT, 'OPEN', 'Y', '44250', '100'),
('경산시 지역예술인 창작지원사업', '경산시 지역 예술인의 창작활동을 지원합니다.', '20260101', '20261231', 9810000::BIGINT, 'OPEN', 'Y', '47290', '200'),
('하남시 청년 창업지원 플랫폼 구축사업', '하남시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 10180000::BIGINT, 'OPEN', 'Y', '41450', '300'),
('관악구 노후 상수도 시설개선사업', '관악구 노후 상수도관을 정비해 주민 불편을 해소합니다.', '20250101', '20250630', 10550000::BIGINT, 'CLOSED', 'Y', '11620', '400'),
('괴산군 청소년 자립 지원센터 운영', '괴산군 위기청소년의 자립을 돕는 지원센터를 운영합니다.', '20260101', '20261231', 10920000::BIGINT, 'OPEN', 'Y', '43760', '100'),
('창녕군 보건지소 의료장비 확충사업', '창녕군 보건지소의 진료 장비를 확충합니다.', '20260101', '20261231', 11290000::BIGINT, 'OPEN', 'Y', '48740', '200'),
('연제구 청년 창업지원 플랫폼 구축사업', '연제구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 11660000::BIGINT, 'OPEN', 'Y', '26470', '300'),
('양천구 공영주차장 확충사업', '양천구 부족한 공영주차장을 확충합니다.', '20260101', '20261231', 12030000::BIGINT, 'OPEN', 'Y', '11470', '400'),
('연수구 독거노인 돌봄 지원사업', '연수구 지역 독거노인의 안전한 생활을 지원합니다.', '20250101', '20250630', 12400000::BIGINT, 'CLOSED', 'Y', '28185', '100'),
('논산시 문화의집 시설개선사업', '논산시 주민 문화공간인 문화의집 시설을 개선합니다.', '20260101', '20261231', 12770000::BIGINT, 'OPEN', 'Y', '44230', '200'),
('중랑구 청년 창업지원 플랫폼 구축사업', '중랑구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 13140000::BIGINT, 'OPEN', 'Y', '11260', '300'),
('횡성군 반려동물 놀이터 조성사업', '횡성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.', '20260101', '20261231', 13510000::BIGINT, 'OPEN', 'Y', '51730', '400'),
('속초시 아동급식 지원사업', '속초시 결식 우려 아동에게 급식을 지원합니다.', '20260101', '20261231', 13880000::BIGINT, 'OPEN', 'Y', '51210', '100'),
('곡성군 작은도서관 리모델링사업', '곡성군 노후 작은도서관을 새단장합니다.', '20250101', '20250630', 14250000::BIGINT, 'CLOSED', 'Y', '12720', '200'),
('익산시 청년 창업지원 플랫폼 구축사업', '익산시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 14620000::BIGINT, 'OPEN', 'Y', '52140', '300'),
('완도군 재난안전 대피시설 확충사업', '완도군 재난 발생 시 주민 대피시설을 확충합니다.', '20260101', '20261231', 14990000::BIGINT, 'OPEN', 'Y', '12850', '400'),
('송파구 장애인 이동지원 차량 지원사업', '송파구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.', '20260101', '20261231', 15360000::BIGINT, 'OPEN', 'Y', '11710', '100'),
('양평군 체육시설 현대화사업', '양평군 노후 체육시설을 현대화합니다.', '20260101', '20261231', 15730000::BIGINT, 'OPEN', 'Y', '41830', '200'),
('당진시 청년 창업지원 플랫폼 구축사업', '당진시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20250101', '20250630', 16100000::BIGINT, 'CLOSED', 'Y', '44270', '300'),
('천안시 가로등 LED 교체사업', '천안시 노후 가로등을 LED로 교체해 안전을 개선합니다.', '20260101', '20261231', 16470000::BIGINT, 'OPEN', 'Y', '44130', '400'),
('울주군 취약계층 겨울나기 지원사업', '울주군 취약계층 가구의 난방비와 방한용품을 지원합니다.', '20260101', '20261231', 16840000::BIGINT, 'OPEN', 'Y', '31710', '100'),
('무주군 지역예술인 창작지원사업', '무주군 지역 예술인의 창작활동을 지원합니다.', '20260101', '20261231', 17210000::BIGINT, 'OPEN', 'Y', '52730', '200'),
('영주시 청년 창업지원 플랫폼 구축사업', '영주시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 17580000::BIGINT, 'OPEN', 'Y', '47210', '300'),
('종로구 노후 상수도 시설개선사업', '종로구 노후 상수도관을 정비해 주민 불편을 해소합니다.', '20250101', '20250630', 17950000::BIGINT, 'CLOSED', 'Y', '11110', '400'),
('광산구 청소년 자립 지원센터 운영', '광산구 위기청소년의 자립을 돕는 지원센터를 운영합니다.', '20260101', '20261231', 18320000::BIGINT, 'OPEN', 'Y', '12330', '100'),
('대전광역시 중구 보건지소 의료장비 확충사업', '대전광역시 중구 보건지소의 진료 장비를 확충합니다.', '20260101', '20261231', 18690000::BIGINT, 'OPEN', 'Y', '30140', '200'),
('노원구 청년 창업지원 플랫폼 구축사업', '노원구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 19060000::BIGINT, 'OPEN', 'Y', '11350', '300'),
('고성군 공영주차장 확충사업', '고성군 부족한 공영주차장을 확충합니다.', '20260101', '20261231', 19430000::BIGINT, 'OPEN', 'Y', '48820', '400'),
('남해군 독거노인 돌봄 지원사업', '남해군 지역 독거노인의 안전한 생활을 지원합니다.', '20250101', '20250630', 19800000::BIGINT, 'CLOSED', 'Y', '48840', '100'),
('양구군 문화의집 시설개선사업', '양구군 주민 문화공간인 문화의집 시설을 개선합니다.', '20260101', '20261231', 20170000::BIGINT, 'OPEN', 'Y', '51800', '200'),
('평창군 청년 창업지원 플랫폼 구축사업', '평창군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 20540000::BIGINT, 'OPEN', 'Y', '51760', '300'),
('고성군 반려동물 놀이터 조성사업', '고성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.', '20260101', '20261231', 20910000::BIGINT, 'OPEN', 'Y', '51820', '400'),
('영암군 아동급식 지원사업', '영암군 결식 우려 아동에게 급식을 지원합니다.', '20260101', '20261231', 21280000::BIGINT, 'OPEN', 'Y', '12800', '100'),
('영양군 작은도서관 리모델링사업', '영양군 노후 작은도서관을 새단장합니다.', '20250101', '20250630', 21650000::BIGINT, 'CLOSED', 'Y', '47760', '200'),
('문경시 청년 창업지원 플랫폼 구축사업', '문경시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 22020000::BIGINT, 'OPEN', 'Y', '47280', '300'),
('울산광역시 동구 재난안전 대피시설 확충사업', '울산광역시 동구 재난 발생 시 주민 대피시설을 확충합니다.', '20260101', '20261231', 22390000::BIGINT, 'OPEN', 'Y', '31170', '400'),
('삼척시 장애인 이동지원 차량 지원사업', '삼척시 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.', '20260101', '20261231', 22760000::BIGINT, 'OPEN', 'Y', '51230', '100'),
('충주시 체육시설 현대화사업', '충주시 노후 체육시설을 현대화합니다.', '20260101', '20261231', 23130000::BIGINT, 'OPEN', 'Y', '43130', '200'),
('금정구 청년 창업지원 플랫폼 구축사업', '금정구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20250101', '20250630', 23500000::BIGINT, 'CLOSED', 'Y', '26410', '300'),
('용인시 가로등 LED 교체사업', '용인시 노후 가로등을 LED로 교체해 안전을 개선합니다.', '20260101', '20261231', 23870000::BIGINT, 'OPEN', 'Y', '41460', '400'),
('검단구 취약계층 겨울나기 지원사업', '검단구 취약계층 가구의 난방비와 방한용품을 지원합니다.', '20260101', '20261231', 24240000::BIGINT, 'OPEN', 'Y', '28290', '100'),
('남원시 지역예술인 창작지원사업', '남원시 지역 예술인의 창작활동을 지원합니다.', '20260101', '20261231', 24610000::BIGINT, 'OPEN', 'Y', '52190', '200'),
('고령군 청년 창업지원 플랫폼 구축사업', '고령군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 24980000::BIGINT, 'OPEN', 'Y', '47830', '300'),
('양주시 노후 상수도 시설개선사업', '양주시 노후 상수도관을 정비해 주민 불편을 해소합니다.', '20250101', '20250630', 25350000::BIGINT, 'CLOSED', 'Y', '41630', '400'),
('신안군 청소년 자립 지원센터 운영', '신안군 위기청소년의 자립을 돕는 지원센터를 운영합니다.', '20260101', '20261231', 25720000::BIGINT, 'OPEN', 'Y', '12870', '100'),
('용산구 보건지소 의료장비 확충사업', '용산구 보건지소의 진료 장비를 확충합니다.', '20260101', '20261231', 26090000::BIGINT, 'OPEN', 'Y', '11170', '200'),
('진천군 청년 창업지원 플랫폼 구축사업', '진천군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 26460000::BIGINT, 'OPEN', 'Y', '43750', '300'),
('포항시 공영주차장 확충사업', '포항시 부족한 공영주차장을 확충합니다.', '20260101', '20261231', 26830000::BIGINT, 'OPEN', 'Y', '47110', '400'),
('나주시 독거노인 돌봄 지원사업', '나주시 지역 독거노인의 안전한 생활을 지원합니다.', '20250101', '20250630', 27200000::BIGINT, 'CLOSED', 'Y', '12170', '100'),
('통영시 문화의집 시설개선사업', '통영시 주민 문화공간인 문화의집 시설을 개선합니다.', '20260101', '20261231', 27570000::BIGINT, 'OPEN', 'Y', '48220', '200'),
('가평군 청년 창업지원 플랫폼 구축사업', '가평군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 27940000::BIGINT, 'OPEN', 'Y', '41820', '300'),
('사하구 반려동물 놀이터 조성사업', '사하구 반려동물과 주민이 함께하는 놀이터를 조성합니다.', '20260101', '20261231', 28310000::BIGINT, 'OPEN', 'Y', '26380', '400'),
('대구광역시 북구 아동급식 지원사업', '대구광역시 북구 결식 우려 아동에게 급식을 지원합니다.', '20260101', '20261231', 28680000::BIGINT, 'OPEN', 'Y', '27230', '100'),
('칠곡군 작은도서관 리모델링사업', '칠곡군 노후 작은도서관을 새단장합니다.', '20250101', '20250630', 29050000::BIGINT, 'CLOSED', 'Y', '47850', '200'),
('영월군 청년 창업지원 플랫폼 구축사업', '영월군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 29420000::BIGINT, 'OPEN', 'Y', '51750', '300'),
('청송군 재난안전 대피시설 확충사업', '청송군 재난 발생 시 주민 대피시설을 확충합니다.', '20260101', '20261231', 29790000::BIGINT, 'OPEN', 'Y', '47750', '400'),
('옹진군 장애인 이동지원 차량 지원사업', '옹진군 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.', '20260101', '20261231', 30160000::BIGINT, 'OPEN', 'Y', '28720', '100'),
('진안군 체육시설 현대화사업', '진안군 노후 체육시설을 현대화합니다.', '20260101', '20261231', 30530000::BIGINT, 'OPEN', 'Y', '52720', '200'),
('광양시 청년 창업지원 플랫폼 구축사업', '광양시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20250101', '20250630', 30900000::BIGINT, 'CLOSED', 'Y', '12190', '300'),
('서대문구 가로등 LED 교체사업', '서대문구 노후 가로등을 LED로 교체해 안전을 개선합니다.', '20260101', '20261231', 31270000::BIGINT, 'OPEN', 'Y', '11410', '400'),
('영도구 취약계층 겨울나기 지원사업', '영도구 취약계층 가구의 난방비와 방한용품을 지원합니다.', '20260101', '20261231', 31640000::BIGINT, 'OPEN', 'Y', '26200', '100'),
('은평구 지역예술인 창작지원사업', '은평구 지역 예술인의 창작활동을 지원합니다.', '20260101', '20261231', 32010000::BIGINT, 'OPEN', 'Y', '11380', '200'),
('강북구 청년 창업지원 플랫폼 구축사업', '강북구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 32380000::BIGINT, 'OPEN', 'Y', '11305', '300'),
('부산광역시 남구 노후 상수도 시설개선사업', '부산광역시 남구 노후 상수도관을 정비해 주민 불편을 해소합니다.', '20250101', '20250630', 32750000::BIGINT, 'CLOSED', 'Y', '26290', '400'),
('금산군 청소년 자립 지원센터 운영', '금산군 위기청소년의 자립을 돕는 지원센터를 운영합니다.', '20260101', '20261231', 33120000::BIGINT, 'OPEN', 'Y', '44710', '100'),
('밀양시 보건지소 의료장비 확충사업', '밀양시 보건지소의 진료 장비를 확충합니다.', '20260101', '20261231', 33490000::BIGINT, 'OPEN', 'Y', '48270', '200'),
('영종구 청년 창업지원 플랫폼 구축사업', '영종구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 33860000::BIGINT, 'OPEN', 'Y', '28155', '300'),
('김천시 공영주차장 확충사업', '김천시 부족한 공영주차장을 확충합니다.', '20260101', '20261231', 34230000::BIGINT, 'OPEN', 'Y', '47150', '400'),
('대덕구 독거노인 돌봄 지원사업', '대덕구 지역 독거노인의 안전한 생활을 지원합니다.', '20250101', '20250630', 34600000::BIGINT, 'CLOSED', 'Y', '30230', '100'),
('미추홀구 문화의집 시설개선사업', '미추홀구 주민 문화공간인 문화의집 시설을 개선합니다.', '20260101', '20261231', 34970000::BIGINT, 'OPEN', 'Y', '28177', '200'),
('남양주시 청년 창업지원 플랫폼 구축사업', '남양주시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 35340000::BIGINT, 'OPEN', 'Y', '41360', '300'),
('군위군 반려동물 놀이터 조성사업', '군위군 반려동물과 주민이 함께하는 놀이터를 조성합니다.', '20260101', '20261231', 35710000::BIGINT, 'OPEN', 'Y', '27720', '400'),
('김제시 아동급식 지원사업', '김제시 결식 우려 아동에게 급식을 지원합니다.', '20260101', '20261231', 36080000::BIGINT, 'OPEN', 'Y', '52210', '100'),
('군산시 작은도서관 리모델링사업', '군산시 노후 작은도서관을 새단장합니다.', '20250101', '20250630', 36450000::BIGINT, 'CLOSED', 'Y', '52130', '200'),
('서초구 청년 창업지원 플랫폼 구축사업', '서초구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 36820000::BIGINT, 'OPEN', 'Y', '11650', '300'),
('안성시 재난안전 대피시설 확충사업', '안성시 재난 발생 시 주민 대피시설을 확충합니다.', '20260101', '20261231', 37190000::BIGINT, 'OPEN', 'Y', '41550', '400'),
('성동구 장애인 이동지원 차량 지원사업', '성동구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.', '20260101', '20261231', 37560000::BIGINT, 'OPEN', 'Y', '11200', '100'),
('화천군 체육시설 현대화사업', '화천군 노후 체육시설을 현대화합니다.', '20260101', '20261231', 37930000::BIGINT, 'OPEN', 'Y', '51790', '200'),
('수영구 청년 창업지원 플랫폼 구축사업', '수영구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20250101', '20250630', 38300000::BIGINT, 'CLOSED', 'Y', '26500', '300'),
('강화군 가로등 LED 교체사업', '강화군 노후 가로등을 LED로 교체해 안전을 개선합니다.', '20260101', '20261231', 38670000::BIGINT, 'OPEN', 'Y', '28710', '400'),
('창원시 취약계층 겨울나기 지원사업', '창원시 취약계층 가구의 난방비와 방한용품을 지원합니다.', '20260101', '20261231', 39040000::BIGINT, 'OPEN', 'Y', '48120', '100'),
('제주특별자치도 지역예술인 창작지원사업', '제주특별자치도 지역 예술인의 창작활동을 지원합니다.', '20260101', '20261231', 39410000::BIGINT, 'OPEN', 'Y', '50000', '200'),
('도봉구 청년 창업지원 플랫폼 구축사업', '도봉구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 39780000::BIGINT, 'OPEN', 'Y', '11320', '300'),
('유성구 노후 상수도 시설개선사업', '유성구 노후 상수도관을 정비해 주민 불편을 해소합니다.', '20250101', '20250630', 40150000::BIGINT, 'CLOSED', 'Y', '30200', '400'),
('파주시 청소년 자립 지원센터 운영', '파주시 위기청소년의 자립을 돕는 지원센터를 운영합니다.', '20260101', '20261231', 40520000::BIGINT, 'OPEN', 'Y', '41480', '100'),
('서울특별시 강서구 보건지소 의료장비 확충사업', '서울특별시 강서구 보건지소의 진료 장비를 확충합니다.', '20260101', '20261231', 40890000::BIGINT, 'OPEN', 'Y', '11500', '200'),
('대전광역시 동구 청년 창업지원 플랫폼 구축사업', '대전광역시 동구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.', '20260101', '20261231', 41260000::BIGINT, 'OPEN', 'Y', '30110', '300'),
('서천군 공영주차장 확충사업', '서천군 부족한 공영주차장을 확충합니다.', '20260101', '20261231', 41630000::BIGINT, 'OPEN', 'Y', '44770', '400')
) AS seed(ttl, cn, bgng, end_, goal, stts, rls, lclgv, se_cd)
WHERE (SELECT COUNT(*) FROM G_DSGN_DNTN_BIZ_MNG) < 50;

-- =====================================================================
-- Round AB (특정사업에 기부하기 상세화면 - 응원메시지/공지사항 탭): AS-IS details.html은
-- 기부자가 남긴 응원메시지(G_CNTR.CHEER_MSG - 배치스캔 구간에 없던 진짜 신규 컬럼)와
-- 특정사업별 공지사항(별도 테이블)을 보여준다. 공지사항 테이블은 로컬 AS-IS 소스/배치스캔
-- 어디에도 실제 스키마가 없어 이 프로젝트의 G_ 접두사 관례를 따라 새로 설계했다.
-- =====================================================================
ALTER TABLE G_CNTR ADD COLUMN IF NOT EXISTS CHEER_MSG VARCHAR(100);

CREATE TABLE IF NOT EXISTS G_DSGN_PRJ_NOTICE (
    PRJ_NOTICE_ID     BIGINT PRIMARY KEY,
    DSGN_DNTN_BIZ_ID  BIGINT NOT NULL,
    PRJ_NOTICE_SUBJECT VARCHAR(255),
    PRJ_NOTICE_CN     TEXT,
    FRST_REGIST_PNTTM VARCHAR(14)
);

CREATE SEQUENCE IF NOT EXISTS g_dsgn_prj_notice_prj_notice_id_seq START WITH 1000;

ALTER TABLE G_DSGN_PRJ_NOTICE ALTER COLUMN PRJ_NOTICE_ID SET DEFAULT nextval('g_dsgn_prj_notice_prj_notice_id_seq');

ALTER SEQUENCE g_dsgn_prj_notice_prj_notice_id_seq OWNED BY G_DSGN_PRJ_NOTICE.PRJ_NOTICE_ID;

-- 상세화면 "사업소개" 탭 테스트용 실제 컨텐츠(AS-IS는 리치에디터 HTML을 그대로 렌더링 -
-- th:utext로 렌더링하므로 이미지도 본문에 직접 들어간다). 이미지는 각 사업과 실제로
-- 관련된 홍보사진을 재사용(예: 청양군 수해사진, 춘천시 캠페인 배너, 곡성심청배 농가사진 -
-- 새로 만들지 않음). 운영중인 홈페이지의 실제 사업소개 글 구조(추진배경/지원계획/
-- 모금기간/모금목표액/대상/기대효과)를 그대로 따랐다.
UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_CN = '<p>&nbsp;</p>
<p><img src="/images/new/202508081741530159_M.jpg" alt="청양군 수해복구 지원사업"></p>
<p>■ 추진배경<br>
2025년 여름 집중호우로 청양군 곳곳의 주택과 농경지가 침수 피해를 입었습니다. 특히 홀로 사시는
어르신 가구는 복구 인력과 비용을 마련하기 어려워 피해가 장기화되고 있습니다.</p>
<p>■ 지원계획<br>
기부금은 침수 피해 주택의 수리비, 임시 거처 지원, 파손된 생활집기 교체 비용으로 사용됩니다.</p>
<p>■ 모금기간<br>
2026년 1월 ~ 2026년 12월(1년간)</p>
<p>■ 모금목표액<br>
2천 5백만 원</p>
<p>■ 대상<br>
청양군에 주민등록이 되어 있고 2025년 수해로 주택 피해를 입은 가구</p>
<p>■ 기대효과<br>
피해 주민들이 하루 빨리 일상으로 돌아갈 수 있도록 돕고, 재난 대응 공동체 기반을 다집니다.</p>
<p>&nbsp;</p>' WHERE DSGN_DNTN_BIZ_TTL = '청양군 수해복구 지원사업';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_CN = '<p>&nbsp;</p>
<p><img src="/images/new/202504301133410131_M.jpeg" alt="춘천시 취약지역 건강돌봄 캠페인"></p>
<p>■ 추진배경<br>
취약지역에 거주하는 어르신들은 정기적인 병원 방문과 건강관리가 어려워 만성질환 악화와 고독사
위험에 노출되어 있습니다.</p>
<p>■ 지원계획<br>
방문 건강상담, 만성질환 관리 물품 지원, 정서적 돌봄을 위한 정기 방문 프로그램을 운영합니다.</p>
<p>■ 모금기간<br>
2026년 1월 ~ 2026년 12월(1년간)</p>
<p>■ 모금목표액<br>
1천 8백만 원</p>
<p>■ 대상<br>
춘천시 취약지역에 거주하는 65세 이상 독거 어르신</p>
<p>■ 기대효과<br>
정기적인 돌봄으로 고독사를 예방하고, 지역사회가 함께 어르신을 살피는 문화를 만듭니다.</p>
<p>&nbsp;</p>' WHERE DSGN_DNTN_BIZ_TTL = '춘천시 취약지역 건강돌봄 캠페인';

UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_CN = '<p>&nbsp;</p>
<p><img src="/images/new/202508201312560094_M.jpg" alt="곡성군 배 재배농가 지원사업"></p>
<p>■ 추진배경<br>
곡성 심청배 재배농가는 고령화와 판로 부족으로 어려움을 겪고 있습니다. 우수한 품질에도 불구하고
안정적인 판로가 없어 수확기마다 가격 변동에 취약합니다.</p>
<p>■ 지원계획<br>
공동 저장·선별 시설 개선과 온라인 직거래 판로 개척을 지원해 농가 소득 안정을 돕습니다.</p>
<p>■ 모금기간<br>
2026년 1월 ~ 2026년 12월(1년간)</p>
<p>■ 모금목표액<br>
1천 2백만 원</p>
<p>■ 대상<br>
곡성군 내 배 재배 농가</p>
<p>■ 기대효과<br>
농가의 안정적인 소득 기반을 마련하고, 곡성 심청배의 지역 대표 특산물 위상을 높입니다.</p>
<p>&nbsp;</p>' WHERE DSGN_DNTN_BIZ_TTL = '곡성군 배 재배농가 지원사업';

-- 상세화면 "응원메시지(기부내역)" 탭 데모용 - 새 기부를 만들지 않고 이미 있던 완료된
-- 지정기부 3건에 응원 문구만 채운다.
UPDATE G_CNTR SET CHEER_MSG = '항상 응원합니다. 화이팅!' WHERE CNTR_SN = 'D202608131619055444';

UPDATE G_CNTR SET CHEER_MSG = '작은 힘이지만 보탬이 되었으면 합니다.' WHERE CNTR_SN = 'D202608131619054180';

UPDATE G_CNTR SET CHEER_MSG = '좋은 사업 잘 만들어주세요!' WHERE CNTR_SN = 'D202608131501279488';

-- 상세화면 "공지사항" 탭 테스트용 시드 (다른 사업들은 빈 상태 화면 확인용으로 그대로 둔다).
INSERT INTO G_DSGN_PRJ_NOTICE (DSGN_DNTN_BIZ_ID, PRJ_NOTICE_SUBJECT, PRJ_NOTICE_CN, FRST_REGIST_PNTTM)
SELECT * FROM (VALUES
    (6::BIGINT, '곡성군 배 재배농가 지원사업 모금 경과 안내', '<p>기부해 주신 모든 분들께 감사드립니다. 현재까지 모금된 기부금은 저장·선별 시설 개선 공사 준비에 사용되고 있습니다.</p>', '20260215103000'),
    (6::BIGINT, '농가 방문 현장 스케치 공유', '<p>지난 달 곡성 심청배 재배농가를 방문해 현장을 살펴보았습니다. 사업 진행 상황은 추후 다시 안내드리겠습니다.</p>', '20260310140000')
) AS seed(biz_id, subject, cn, reg_dt)
WHERE NOT EXISTS (SELECT 1 FROM G_DSGN_PRJ_NOTICE);

-- 순천만(id=3) 사업소개 - 이미지는 붙이지 않는다. AS-IS 라이브 사이트에서 순천/생태/습지/
-- 갯벌/정원 키워드로 실제 등록된 사업을 다 찾아봤지만 진짜 순천만과 관련된 사진이 없었고,
-- "정원" 키워드로 나온 두 결과(울산/화순)는 전부 그 지자체 로고·문구가 이미지에 박혀있어
-- 순천만에 갖다 쓰면 다른 지자체 콘텐츠를 도용하는 셈이라 억지로 넣지 않았다(이전에 영암
-- 홍보이미지를 잘못 쓸 뻔했던 것과 같은 실수를 반복하지 않기 위함). 텍스트 본문만 채운다.
UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_CN = '<p>&nbsp;</p>
<p>■ 추진배경<br>
순천만은 세계 5대 연안습지 중 하나로 갈대밭과 갯벌이 어우러진 생태 보고입니다. 그러나 기후
변화와 방문객 증가로 습지 훼손과 생태계 교란 우려가 커지고 있습니다.</p>
<p>■ 지원계획<br>
탐방로 정비, 외래식물 제거, 철새 서식지 보호구역 관리 등 습지 생태계 보전 활동에 기부금을
사용합니다.</p>
<p>■ 모금기간<br>
2026년 1월 ~ 2026년 12월(1년간)</p>
<p>■ 모금목표액<br>
2천만 원</p>
<p>■ 대상<br>
순천만 정원 및 습지 생태 보전에 관심 있는 모든 분</p>
<p>■ 기대효과<br>
세계적인 생태관광지로서 순천만의 가치를 지키고, 후대에 물려줄 자연유산을 보전합니다.</p>
<p>&nbsp;</p>' WHERE DSGN_DNTN_BIZ_TTL = '순천만 정원 생태 보전 사업';

-- 공지사항 테스트 데이터 확대 - 곡성군 외 여러 사업(진행중/종료 섞어서)에도 추가한다.
INSERT INTO G_DSGN_PRJ_NOTICE (DSGN_DNTN_BIZ_ID, PRJ_NOTICE_SUBJECT, PRJ_NOTICE_CN, FRST_REGIST_PNTTM)
SELECT * FROM (VALUES
    (3::BIGINT, '순천만 탐방로 정비 공사 안내', '<p>2026년 3월부터 순천만 습지 탐방로 일부 구간에서 정비 공사가 진행됩니다. 방문 시 우회 안내를 참고해 주세요.</p>', '20260301090000'),
    (4::BIGINT, '청양군 수해복구 지원사업 1차 지원 완료', '<p>모금된 기부금으로 침수 피해 가구 12세대에 대한 1차 주택 수리 지원을 완료했습니다. 성원해 주셔서 감사합니다.</p>', '20260220153000'),
    (5::BIGINT, '춘천시 건강돌봄 캠페인 방문 일정 안내', '<p>3월부터 취약지역 어르신 대상 정기 방문이 시작됩니다. 자세한 일정은 춘천시 홈페이지를 참고해 주세요.</p>', '20260225110000'),
    (5::BIGINT, '캠페인 참여 자원봉사자 모집', '<p>어르신 돌봄 활동에 함께할 자원봉사자를 모집합니다. 관심 있으신 분은 춘천시청으로 문의해 주세요.</p>', '20260405093000'),
    (7::BIGINT, '거창군 산불피해 복구 지원사업 종료 안내', '<p>모금 기간 종료에 따라 산불피해 복구 지원사업이 마감되었습니다. 기부해 주신 모든 분들께 진심으로 감사드립니다.</p>', '20250901100000')
) AS seed(biz_id, subject, cn, reg_dt)
WHERE (SELECT COUNT(*) FROM G_DSGN_PRJ_NOTICE) < 3;

-- 사용자 피드백: 운영중인 홈페이지의 "사업소개"는 텍스트보다 이미지 여러 장이 위주다
-- (실제로 라이브 API로 확인 - 프로젝트당 1~9장). 순천만은 이미지 매칭 문제로 텍스트만
-- 넣었었는데, "아무 이미지나 여러 개"면 된다는 요청에 따라 이미 이 프로젝트에 있는 실제
-- 이미지 5장(다른 사업들에도 쓰인 것과 동일한 실제 파일)을 본문 위쪽에 추가한다.
UPDATE G_DSGN_DNTN_BIZ_MNG SET DSGN_DNTN_BIZ_CN = '<p>&nbsp;</p>
<p><img src="/images/new/202504301133410131_M.jpeg" alt="순천만 정원 생태 보전 사업"></p>
<p><img src="/images/new/202507181736420436_M.png" alt="순천만 정원 생태 보전 사업"></p>
<p><img src="/images/new/202508081741530159_M.jpg" alt="순천만 정원 생태 보전 사업"></p>
<p><img src="/images/new/20250819171944_M.png" alt="순천만 정원 생태 보전 사업"></p>
<p><img src="/images/new/202508201312560094_M.jpg" alt="순천만 정원 생태 보전 사업"></p>
<p>&nbsp;</p>
<p>■ 추진배경<br>
순천만은 세계 5대 연안습지 중 하나로 갈대밭과 갯벌이 어우러진 생태 보고입니다. 그러나 기후
변화와 방문객 증가로 습지 훼손과 생태계 교란 우려가 커지고 있습니다.</p>
<p>■ 지원계획<br>
탐방로 정비, 외래식물 제거, 철새 서식지 보호구역 관리 등 습지 생태계 보전 활동에 기부금을
사용합니다.</p>
<p>■ 모금기간<br>
2026년 1월 ~ 2026년 12월(1년간)</p>
<p>■ 모금목표액<br>
2천만 원</p>
<p>■ 대상<br>
순천만 정원 및 습지 생태 보전에 관심 있는 모든 분</p>
<p>■ 기대효과<br>
세계적인 생태관광지로서 순천만의 가치를 지키고, 후대에 물려줄 자연유산을 보전합니다.</p>
<p>&nbsp;</p>' WHERE DSGN_DNTN_BIZ_TTL = '순천만 정원 생태 보전 사업';

-- =====================================================================
-- 기금사업소개(list-select.html)의 "지자체정보" 탭 - 이미 있던 레거시 컬럼
-- (LOCGOV_INTRCN_CN/CHARGER_*/LOCGOV_HMPG/LOCGOV_POPLTN_CO/LOCGOV_BUDGET_AMT)
-- 을 이번 라운드에 처음 매핑했다. 특정사업기부 시드가 있는 지자체 중 대표
-- 5곳만 테스트 데이터로 채운다(전수 백필은 스코프 밖).
-- =====================================================================
UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>순천시는 대한민국 생태수도로 불리는 전라남도 남부의 도시로, 순천만 습지와 순천만국가정원 등 생태 자원이 풍부합니다. 고향사랑기부금은 순천만 습지 보전 및 지역 생태관광 활성화 사업에 우선적으로 사용됩니다.</p>',
  charger_nm = '김민석', charger_cttpc = '061-749-3821', charger_psitn_dept = '기획예산과',
  locgov_hmpg = 'https://www.suncheon.go.kr', locgov_popltn_co = '270000', locgov_budget_amt = 1250000
WHERE locgov_code = '46150';

UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>청양군은 충청남도의 대표적인 농업 도시로, 구기자와 고추 등 특산물로 유명합니다. 고향사랑기부금은 농가 지원 및 지역 특산품 브랜드화 사업에 사용됩니다.</p>',
  charger_nm = '이정훈', charger_cttpc = '041-940-2521', charger_psitn_dept = '재정경제과',
  locgov_hmpg = 'https://www.cheongyang.go.kr', locgov_popltn_co = '30000', locgov_budget_amt = 420000
WHERE locgov_code = '44790';

UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>춘천시는 강원특별자치도의 도청 소재지로, 호수와 산으로 둘러싸인 자연경관과 닭갈비·막국수 등 먹거리로 잘 알려져 있습니다. 고향사랑기부금은 청년 정착 지원 및 관광 인프라 조성에 사용됩니다.</p>',
  charger_nm = '박서연', charger_cttpc = '033-250-3412', charger_psitn_dept = '세정과',
  locgov_hmpg = 'https://www.chuncheon.go.kr', locgov_popltn_co = '280000', locgov_budget_amt = 1180000
WHERE locgov_code = '51110';

UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>곡성군은 전라남도의 대표적인 배 재배 지역으로, 섬진강을 따라 형성된 청정 자연환경이 특징입니다. 고향사랑기부금은 배 재배농가 지원사업 및 귀농·귀촌 지원에 사용됩니다.</p>',
  charger_nm = '정민아', charger_cttpc = '061-360-8421', charger_psitn_dept = '기획감사실',
  locgov_hmpg = 'https://www.gokseong.go.kr', locgov_popltn_co = '27000', locgov_budget_amt = 380000
WHERE locgov_code = '46530';

UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>무주군은 전북특별자치도 산간지역에 위치한 청정 관광도시로, 무주반딧불축제와 덕유산국립공원으로 유명합니다. 고향사랑기부금은 지역 축제 지원 및 청정환경 보전사업에 사용됩니다.</p>',
  charger_nm = '한지원', charger_cttpc = '063-320-2606', charger_psitn_dept = '자치행정과',
  locgov_hmpg = 'https://www.muju.go.kr', locgov_popltn_co = '23000', locgov_budget_amt = 350000
WHERE locgov_code = '52730';

-- 서울특별시 시청(11000) - 기금사업소개 화면 검증에 사용자가 실제 운영사이트 element로
-- 준 참고 예시와 동일한 실제 수치로 백필한다.
UPDATE g_locgov SET
  locgov_intrcn_cn = '<p>약자와 동행하는 서울! 우리 사회 양극화를 해소하고 더불어 살아가는 상생과 공존의 도시<br>글로벌 매력도시 서울! 세계인 누구나 살고 싶고 찾아오고 싶고 일하고 싶고 투자하고 싶은 매력적인 도시<br><br>''동행·매력 특별시'' 서울특별시입니다.</p>',
  charger_nm = '김소영', charger_cttpc = '02-2133-6876', charger_psitn_dept = '재정담당관',
  locgov_hmpg = 'https://www.seoul.go.kr', locgov_popltn_co = '9384325', locgov_budget_amt = 45740518
WHERE locgov_code = '11000';

-- 서울특별시 시청(11000) 기금사업소개 탭 페이징 검증용 테스트 데이터 7건.
INSERT INTO g_dsgn_dntn_biz_mng (dsgn_dntn_biz_id, dsgn_dntn_biz_ttl, dsgn_dntn_biz_cn, dsgn_dntn_biz_bgng_ymd, dsgn_dntn_biz_end_ymd, goal_amt, dsgn_dntn_biz_stts_cd, rls_yn, dsgn_dntn_biz_se_cd, lclgv_cd, frst_rgtr_id) VALUES
(200, '서울 취약계층 동행 돌봄 지원사업', '<p>1인가구 어르신과 취약계층을 위한 돌봄 서비스를 확충합니다.</p>', '20260101', '20261231', 50000000, 'OPEN', 'Y', '100', '11000', 1000),
(201, '서울 청년 자립 지원 프로그램', '<p>취업 준비 청년을 위한 자립 지원 프로그램을 운영합니다.</p>', '20260101', '20261231', 40000000, 'OPEN', 'Y', '200', '11000', 1000),
(202, '서울 골목상권 활성화 사업', '<p>전통시장과 골목상권 활성화를 위한 지원사업입니다.</p>', '20260201', '20261130', 35000000, 'OPEN', 'Y', '300', '11000', 1000),
(203, '서울 반려동물 동반 공원 조성', '<p>반려동물과 함께할 수 있는 도심 공원을 조성합니다.</p>', '20260301', '20261031', 25000000, 'OPEN', 'Y', '400', '11000', 1000),
(204, '서울 취약아동 교육격차 해소사업', '<p>취약계층 아동의 교육 격차 해소를 위한 사업입니다.</p>', '20250301', '20250831', 30000000, 'OPEN', 'Y', '100', '11000', 1000),
(205, '서울 글로벌 매력도시 홍보사업', '<p>서울의 매력을 세계에 알리는 홍보사업입니다.</p>', '20250101', '20250630', 20000000, 'OPEN', 'Y', '300', '11000', 1000),
(206, '서울 상생 공존 마을공동체 지원', '<p>지역 마을공동체 활성화를 지원하는 사업입니다.</p>', '20250601', '20251231', 28000000, 'OPEN', 'Y', '200', '11000', 1000)
ON CONFLICT (dsgn_dntn_biz_id) DO NOTHING;

-- =====================================================================
-- 기부하기(일반기부) Vue3 전환 라운드 - 지정기부사업 대표이미지 15건 추가(사용자 요청).
-- 1000번대 사업 상당수는 이 DDL 시드가 아니라 admin 지정기부관리 콘솔로 실제 등록된
-- 라이브 데이터라(위 INSERT는 200~206 7건만 시드) 이 UPDATE는 그 행이 존재하는 DB에서만
-- 의미가 있다 - 신규 프로비저닝된 DB에는 200번만 적용되고 나머지는 대상 행 자체가 없어
-- 조용히 스킵된다(안전, 하지만 재현되진 않는다는 점은 알아둘 것).
-- =====================================================================
UPDATE g_dsgn_dntn_biz_mng SET dsgn_dntn_biz_rprs_img = x.img
FROM (VALUES
    (1000::BIGINT, '/images/new/20260902154204001_M.jpg'),
    (1001::BIGINT, '/images/new/20260902154204002_M.jpg'),
    (1002::BIGINT, '/images/new/20260902154204003_M.jpg'),
    (1007::BIGINT, '/images/new/20260902154204004_M.jpg'),
    (1008::BIGINT, '/images/new/20260902154204005_M.jpg'),
    (1010::BIGINT, '/images/new/20260902154204006_M.jpg'),
    (1014::BIGINT, '/images/new/20260902154204007_M.jpg'),
    (1015::BIGINT, '/images/new/20260902154204008_M.jpg'),
    (1016::BIGINT, '/images/new/20260902154204009_M.jpg'),
    (1018::BIGINT, '/images/new/20260902154204010_M.jpg'),
    (1024::BIGINT, '/images/new/20260902154205011_M.jpg'),
    (1026::BIGINT, '/images/new/20260902154205012_M.jpg'),
    (1030::BIGINT, '/images/new/20260902154205013_M.jpg'),
    (1043::BIGINT, '/images/new/20260902154205014_M.jpg'),
    (200::BIGINT, '/images/new/20260902154205015_M.jpg')
) AS x(id, img)
WHERE g_dsgn_dntn_biz_mng.dsgn_dntn_biz_id = x.id
  AND (g_dsgn_dntn_biz_mng.dsgn_dntn_biz_rprs_img IS NULL OR g_dsgn_dntn_biz_mng.dsgn_dntn_biz_rprs_img = '');

-- =====================================================================
-- 기부금 지출내역 (AS-IS opmanager/give/give-operation, G_CTBNY_OPRATN/_FILE) - 지자체
-- 담당자가 고향사랑기부금 사용 내역을 등록한다(고향사랑 기부금법상 공개 의무). 배치
-- 스캔 당시 PK만 있고 시퀀스가 없던 테이블이라 다른 테이블과 동일 패턴으로 보강한다.
-- admin 서비스가 관리 화면(로그인/RBAC)을 갖고 cross-service API(/api/ctbny-opratn)로
-- 이 테이블에 쓴다 - DB per Service 원칙상 admin이 직접 쓸 수 없다.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS g_ctbny_opratn_regist_sn_seq START WITH 1000;

ALTER TABLE G_CTBNY_OPRATN ALTER COLUMN REGIST_SN SET DEFAULT nextval('g_ctbny_opratn_regist_sn_seq');

ALTER SEQUENCE g_ctbny_opratn_regist_sn_seq OWNED BY G_CTBNY_OPRATN.REGIST_SN;

CREATE SEQUENCE IF NOT EXISTS g_ctbny_opratn_file_regist_file_id_seq START WITH 1000;

ALTER TABLE G_CTBNY_OPRATN_FILE ALTER COLUMN REGIST_FILE_ID SET DEFAULT nextval('g_ctbny_opratn_file_regist_file_id_seq');

ALTER SEQUENCE g_ctbny_opratn_file_regist_file_id_seq OWNED BY G_CTBNY_OPRATN_FILE.REGIST_FILE_ID;

-- 기부금 사용 지출내역 테스트 데이터 (give-state 집계 화면에 이미 나오는 지자체 - 순천시/
-- 강남구/해운대구 - 기준으로 2026년 지출 몇 건씩).
INSERT INTO G_CTBNY_OPRATN (REGIST_SN, LOCGOV_CODE, BSNS_PURPS_CODE, BSNS_NM, BSNS_CN, EXPNDTR_DE, EXPNDTR_AMT, FRST_REGIST_PNTTM, LAST_UPDT_PNTTM, RM) VALUES
(1000, '46150', 'VULNERABLE', '순천시 취약계층 난방비 지원', '독거노인 및 취약계층 가구 200세대에 겨울철 난방비를 지원했습니다.', '20260210', 12000000, now(), now(), NULL),
(1001, '46150', 'YOUTH', '순천만 청년 창업 지원', '청년 창업가 10팀에게 초기 사업화 자금을 지원했습니다.', '20260415', 8000000, now(), now(), NULL),
(1002, '11230', 'COMMUNITY', '강남구 마을공동체 활성화', '주민자치회 주관 마을축제 및 공동체 프로그램을 운영했습니다.', '20260320', 5000000, now(), now(), NULL),
(1003, '26350', 'WELFARE', '해운대구 주민복리 증진사업', '경로당 냉난방기 교체 및 편의시설을 개선했습니다.', '20260505', 6000000, now(), now(), '경로당 12개소')
ON CONFLICT (REGIST_SN) DO NOTHING;

SELECT setval('g_ctbny_opratn_regist_sn_seq', 1003);

-- =====================================================================
-- 기부금 변경신청 (AS-IS G_CNTR_REQMNG, opmanager/give/give-reqmng) - 배치 스캔 당시
-- PK만 있고 시퀀스가 없던 테이블이라 다른 테이블과 동일 패턴으로 보강한다. admin이
-- cross-service API(/api/cntr-reqmng)로 이 테이블에 쓴다.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS g_cntr_reqmng_req_id_seq START WITH 1000;

ALTER TABLE G_CNTR_REQMNG ALTER COLUMN REQ_ID SET DEFAULT nextval('g_cntr_reqmng_req_id_seq');

ALTER SEQUENCE g_cntr_reqmng_req_id_seq OWNED BY G_CNTR_REQMNG.REQ_ID;

ALTER TABLE G_DSGN_DNTN_BIZ_APRV_LOG ADD COLUMN IF NOT EXISTS FRST_REGIST_PNTTM TIMESTAMP DEFAULT now();

-- =====================================================================
-- AS-IS VIEW 이관 (db-dump/VIEW). CUBRID 문법(대괄호 식별자, _utf8'...' COLLATE
-- utf8_bin, date_format)을 Postgres로 변환. 원본은 [g_locgov] 단일 테이블 기준이라
-- 서비스 경계 문제 없이 그대로 이식 가능.
-- =====================================================================
CREATE OR REPLACE VIEW view_search_locgov AS
SELECT locgov_code,
       upper_locgov_nm,
       locgov_nm,
       locgov_popltn_co,
       locgov_hmpg,
       locgov_intrcn_cn,
       concat('/donation/map-select.html?locgovCode=', locgov_code) AS detail_url
FROM g_locgov;

-- =====================================================================
-- 연계 로그 관리 (AS-IS opmanager/log/relay-log, G_RELAY_LOG) - DONATION_LEVY가
-- "현재 상태"(기부 1건당 1행, upsert)라면 G_RELAY_LOG는 "시도 이력"(부과/수납/국세청
-- 등록 각 호출마다 1행 append)이다. 배치 스캔 당시 PK만 있고 시퀀스가 없던 테이블이라
-- 다른 테이블과 동일 패턴으로 보강한다.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS g_relay_log_relay_log_id_seq START WITH 1000;

ALTER TABLE G_RELAY_LOG ALTER COLUMN RELAY_LOG_ID SET DEFAULT nextval('g_relay_log_relay_log_id_seq');

ALTER SEQUENCE g_relay_log_relay_log_id_seq OWNED BY G_RELAY_LOG.RELAY_LOG_ID;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('RELAY_TYPE', 'ko', 'BUGA',        '세외수입 부과',        1, 'Y'),
('RELAY_TYPE', 'ko', 'SUNAP',       '세외수입 수납확인',     2, 'Y'),
('RELAY_TYPE', 'ko', 'NTS_RECEIPT', '국세청 전자기부금영수증 등록', 3, 'Y'),
('RELAY_RESULT', 'ko', 'SUCCESS', '성공', 1, 'Y'),
('RELAY_RESULT', 'ko', 'FAIL',    '실패', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- CNTR_PATH ("09. 공통코드 목록.xlsx" 실제 AS-IS 값으로 소급 정정) - Donation.cntrPathCode가
-- 처음엔 ONLINE/OFFLINE(자체 발명값)이었는데, 실제 AS-IS 공통코드는 100/200이다.
-- =====================================================================
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('CNTR_PATH', 'ko', '100', '온라인', 1, 'Y'),
('CNTR_PATH', 'ko', '200', '오프라인', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

UPDATE G_CNTR SET CNTR_PATH_CODE = '100' WHERE CNTR_PATH_CODE = 'ONLINE';
UPDATE G_CNTR SET CNTR_PATH_CODE = '200' WHERE CNTR_PATH_CODE = 'OFFLINE';

-- =====================================================================
-- 기부하기(일반기부) Vue3 전환 라운드 - 실버그: HonorCntrbtr.stdrYear는 Integer로 매핑돼
-- 있는데 G_HONOR_CNTRBTR.STDR_YEAR는 위 CREATE TABLE에서 AS-IS 그대로 VARCHAR(4)로
-- 선언돼 있었다(PK의 일부이기도 함). 명예기부자 등급은 completeDonation()이 결제완료
-- 처리 때마다 자동 산정하는데, 지금까지 결제완료를 실제로 눌러본 적이 없어서(REQUESTED
-- 상태로만 시드/테스트됨) findByStdrYearAndLocgovCodeAndUserId() 조회가 "character
-- varying = integer" 타입 불일치로 매번 500을 내는 걸 몰랐다 - 기부하기 화면을 새로
-- 만들고 실제로 결제완료 처리까지 눌러보다가 발견함. 기존 데이터가 전부 4자리 숫자라
-- 컬럼을 INTEGER로 바꾼다(다른 서비스의 CREATED_DATE 타입버그와 반대로, 여기는 PK를
-- 포함해 숫자로 쓰이는 게 자연스러워 엔티티가 아니라 컬럼 쪽을 고쳤다).
-- =====================================================================
ALTER TABLE G_HONOR_CNTRBTR ALTER COLUMN STDR_YEAR TYPE INTEGER USING STDR_YEAR::INTEGER;
