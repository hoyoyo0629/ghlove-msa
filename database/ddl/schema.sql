-- =====================================================================
-- GHLOVE reverse-engineered PostgreSQL schema
-- =====================================================================
-- This schema was NOT derived from an original DDL script or ERD -- none
-- exist for this legacy application. GHLOVE originally ran on CUBRID, and
-- the CUBRID schema itself was never captured anywhere accessible to us.
--
-- Instead, this file was reconstructed by 9 parallel analysis passes, each
-- reading a slice of the ~150 Spring/MyBatis mapper XML files (and, where
-- helpful, the corresponding Java domain/DTO classes) and inferring:
--   - which tables exist, from INSERT INTO / resultMap / SELECT column usage
--   - column names, from bound parameters (#{...}) and resultMap mappings
--   - column types, guessed from Java field types, comparison operators,
--     literal values, and naming conventions (e.g. *_FLAG/*_YN => CHAR(1),
--     *_DATE => VARCHAR date-strings where CommonMapper.datetime/date SQL
--     fragments are used instead of native TIMESTAMP columns, etc.)
--   - primary keys, guessed from WHERE-clause lookup patterns, MyBatis
--     useGeneratedKeys usage, and ON DUPLICATE KEY / MERGE semantics
--
-- IMPORTANT: types and primary keys throughout this file are BEST-EFFORT
-- GUESSES, not verified facts. Many tables carry inline "-- guess:" or
-- "-- TODO: primary key unclear" comments from the original analysis
-- explaining the reasoning (or lack of a confident answer). Treat every
-- column type and PK here as something to validate against real production
-- data / query logs before relying on it, especially for constraints,
-- ranges, and precision.
--
-- No FOREIGN KEY constraints are declared anywhere in this file. We do not
-- yet have reliable enough cross-table reference information (mapper JOINs
-- alone are not sufficient evidence of a real FK relationship in a legacy
-- app like this one), so all tables are left standalone at this stage.
--
-- MERGE NOTE: 9 independent analysis batches (batch_00 .. batch_07, formerly
-- separate files) were merged into this single file. 23 tables were found
-- and defined independently by more than one batch (each batch only saw a
-- partial slice of the mapper files, so two batches sometimes found INSERTs
-- for the same table from different mapper files, or one batch mis-guessed
-- ownership of a table). Those 23 were reconciled by hand: columns are the
-- UNION of every column seen across the conflicting definitions, and any
-- column where the batches guessed different types or primary keys carries
-- an inline "-- CONFLICT:" comment recording both original guesses and
-- which one was kept (chosen for being the more specific/plausible type,
-- or -- for primary keys -- the more confident single/composite natural
-- key). Search this file for "CONFLICT:" to find every such spot.
--
-- Column/table names are preserved exactly as found in the source SQL
-- (case as-is; PostgreSQL folds unquoted identifiers to lowercase, so mixed
-- case here is cosmetic except where columns are explicitly double-quoted,
-- e.g. "LANGUAGE", "YEAR", "DEPTH" -- reserved words / keywords kept quoted
-- to preserve the exact original column name).
--
-- Tables are ordered alphabetically by table name below, which loosely
-- clusters related families together (B_CNTR_*, G_*, OP_ITEM_*, OP_ORDER_*,
-- OP_USER_*, etc.) while keeping the whole file easy to navigate/search.
-- =====================================================================

CREATE TABLE IF NOT EXISTS b_cntr_addr ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	cntr_locgov       VARCHAR(255),
	cntr_amt          NUMERIC(15,2),
	cntr_rate         VARCHAR(20),
	frst_reg_dt       TIMESTAMP
);

-- =====================================================================
-- source: bix5-mapper.xml
-- Batch-generated statistics/reporting tables (fully truncated via DELETE
-- then repopulated via INSERT..SELECT on every run - no application-level
-- domain classes or resultMaps; columns/types inferred purely from SQL).
-- =====================================================================
CREATE TABLE IF NOT EXISTS B_CNTR_AMT ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	CNTR_AMT          NUMERIC(15,2),
	PRE_AMT           NUMERIC(15,2),
	CNTR_CNT          INTEGER,
	PRE_CNT           INTEGER,
	AMT_RATE          VARCHAR(20), -- stored via TO_CHAR(ROUND(...))
	CNT_RATE          VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS B_CNTR_AMT_BIZ ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	CNTR_AMT          NUMERIC(15,2),
	BIZ_AMT           NUMERIC(15,2),
	CNTR_RATE         VARCHAR(20),
	BIZ_RATE          VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS b_cntr_amt_cat ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	cntr_cost         VARCHAR(255), -- amount-bucket label, e.g. '10만원 미만'
	cntr_cnt          INTEGER,
	cntr_rate         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS B_CNTR_AMT_MM ( -- TODO: primary key unclear; guess composite (CNTR_YY, CNTR_MM, LOCGOV_CODE) per MERGE ON clause
	CNTR_YY           VARCHAR(4),
	CNTR_MM           VARCHAR(2),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	PRE_AMT           NUMERIC(15,2),
	CNTR_AMT          NUMERIC(15,2),
	AMT_RATE          VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS B_CNTR_BIZ ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	BIZ_NM            VARCHAR(255),
	CNTR_AMT          NUMERIC(15,2),
	CNTR_RATE         VARCHAR(20),
	CNTR_STATUS       VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS b_cntr_old ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	cntr_old          VARCHAR(255), -- age-bucket label, e.g. '20대'
	cntr_cnt          INTEGER,
	cntr_rate         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS B_CNTR_PATH ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	CNTR_PATH         VARCHAR(255),
	CNTR_AMT          NUMERIC(15,2),
	CNTR_RATE         VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS B_GIFT_CAT ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	gift_cat          VARCHAR(255), -- category label, e.g. '농축산물'
	gift_cnt          INTEGER,
	cntr_rate         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS B_GIFT_CAT_SELL ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	gift_cat          VARCHAR(255),
	cntr_order_cnt    INTEGER,
	cntr_rate         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS b_gift_cnt ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	gift_cnt          INTEGER
);

CREATE TABLE IF NOT EXISTS B_GIFT_DELI ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	seller_id         VARCHAR(255), -- actually populated with company_name, not a numeric id
	gift_nm           VARCHAR(255),
	DELI_TM           NUMERIC(15,2) -- average shipping hours
);

CREATE TABLE IF NOT EXISTS B_GIFT_POINT ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	UPPER_LOCGOV_CODE VARCHAR(20),
	UPPER_LOCGOV_NM   VARCHAR(255),
	LOCGOV_CODE       VARCHAR(20),
	LOCGOV_NM         VARCHAR(255),
	POINT_CAT         VARCHAR(255), -- price-bucket label, e.g. '~1.5만'
	GIFT_CNT          INTEGER,
	CNTR_RATE         VARCHAR(20),
	FRST_REG_DT       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS b_gift_usepoint ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	cntr_point        NUMERIC(15,2),
	cntr_rate         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS B_GIFT_USEPOINT_CPR ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YMD          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	all_use_point     NUMERIC(15,2),
	use_point         NUMERIC(15,2)
);

CREATE TABLE IF NOT EXISTS B_POINT_USE ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_ymd          VARCHAR(8),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	sum_cre_pnt       NUMERIC(15,2),
	sum_use_pnt       NUMERIC(15,2),
	sum_pnt           NUMERIC(15,2),
	sum_del_pnt       NUMERIC(15,2),
	use_rate          VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS B_POINT_USE_YEAR ( -- TODO: primary key unclear (full reload snapshot table)
	CNTR_YY           VARCHAR(4),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	SUM_CRE_PNT       NUMERIC(15,2)
);

CREATE TABLE IF NOT EXISTS B_POINT_YEAR ( -- TODO: primary key unclear (full reload snapshot table)
	cntr_yy           VARCHAR(4),
	upper_locgov_code VARCHAR(20),
	upper_locgov_nm   VARCHAR(255),
	locgov_code       VARCHAR(20),
	locgov_nm         VARCHAR(255),
	crePnt            NUMERIC(15,2),
	use1              NUMERIC(15,2),
	use2              NUMERIC(15,2),
	use3              NUMERIC(15,2),
	use4              NUMERIC(15,2),
	exPnt             NUMERIC(15,2)
);

-- =====================================================================
-- source: ngdonation-mapper.xml
-- Domain: saleson.shop.donation.support.SeoulParam
-- =====================================================================
CREATE TABLE IF NOT EXISTS GIF_HOMETAX (
    if_no           VARCHAR(50) NOT NULL,
    elctrn_pay_no   VARCHAR(50),
    user_id         BIGINT,
    dnt_dt          VARCHAR(14),
    conb_cd         VARCHAR(50),
    dnt_amt         NUMERIC(15,2),
    cntr_type       VARCHAR(10),
    mem_ci          VARCHAR(255),
    biz_no          VARCHAR(50),
    result_code     VARCHAR(10),
    result_msg      VARCHAR(255),
    if_st_dt        VARCHAR(14),
    PRIMARY KEY (if_no)
);

-- =====================================================================
-- source: order-agency-mapper.xml
-- Domain: saleson.shop.orderagency.domain.OrderAgencyLoginConfirmInfo
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_AGENCY_LOGIN_CONFIRM_INFO (
    USER_SESSION_ID     VARCHAR(255) NOT NULL,
    VALID_ACCESS_CD     VARCHAR(50) NOT NULL,
    MANAGER_ID          BIGINT,
    CNTRBTR_MOBILE      VARCHAR(255),
    PRIVATE_KEY         VARCHAR(255),
    CNTRBTR_ID          VARCHAR(50),
    FRST_REG_DT         TIMESTAMP,
    PRIMARY KEY (USER_SESSION_ID, VALID_ACCESS_CD) -- guess: no surrogate id present; delete also keys off MANAGER_ID alone
);

CREATE TABLE IF NOT EXISTS G_CATALOG_CARD_NEWS_IMG_DESC ( -- TODO: primary key unclear; guess composite (CARD_NEWS_ID, IMG_SEQ)
	CARD_NEWS_ID      BIGINT,
	IMG_DESC          VARCHAR(500),
	IMG_SEQ           INTEGER,
	FRST_REGISTER_ID  BIGINT,
	FRST_REGIST_PNTTM TIMESTAMP,
	PRIMARY KEY (CARD_NEWS_ID, IMG_SEQ)
);

CREATE TABLE IF NOT EXISTS G_CATALOG_CARD_NEWS_MNG (
	CARD_NEWS_ID       BIGINT PRIMARY KEY, -- useGeneratedKeys keyProperty="cardNewsId"
	CATALOG_YEAR       INTEGER,
	CATALOG_NO         INTEGER,
	CARD_NEWS_SUBJECT  VARCHAR(255),
	CARD_NEWS_CN       TEXT,
	DELETE_YN          CHAR(1),
	FRST_REGISTER_ID   BIGINT,
	FRST_REGIST_PNTTM  TIMESTAMP,
	LAST_UPDUSR_ID     BIGINT,
	LAST_UPDT_PNTTM    TIMESTAMP
);

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

-- =====================================================================
-- source: catalog-mapper.xml
-- Domain classes: saleson.shop.catalog.domain.{CatalogMng,CatalogCardNews,
--   CatalogCardNewsImageExplain,CatalogContentMng,CatalogContentImageExplain}
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_CATALOG_MNG (
	CATALOG_YEAR      INTEGER,
	CATALOG_NO        INTEGER,
	DISPLAY_YN        CHAR(1),
	FRST_REGISTER_ID  BIGINT,
	FRST_REGIST_PNTTM TIMESTAMP,
	LAST_UPDUSR_ID    BIGINT,
	LAST_UPDT_PNTTM   TIMESTAMP,
	PRIMARY KEY (CATALOG_YEAR, CATALOG_NO)
);

-- =====================================================================
-- cmnty-mapper.xml — 4 parallel bulletin-board families (bbs/sr_bbs/
-- faq_bbs/off_sr_bbs), each with bbs + cmnt + file(+cmnt_file) tables,
-- plus a separate "rpstr" board. All *_ID columns are DB-generated
-- identities (never supplied by the app in INSERT statements, several use
-- MyBatis useGeneratedKeys). Domain classes are java.util.Date typed for
-- frst_crt_dt/last_mdfcn_dt, so those columns are modeled as TIMESTAMP.
-- =====================================================================

-- source: cmnty-mapper.xml (addBbs / updateInqCnt / deleteBbs / updateBbs)
-- domain: saleson.shop.community.doamin.CmntyBbsDto
CREATE TABLE IF NOT EXISTS g_cmnty_bbs (
    bbs_id          BIGSERIAL PRIMARY KEY,
    bbs_ttl         VARCHAR(255),
    bbs_cn          TEXT,
    notice_yn       CHAR(1),
    is_secret       CHAR(1),
    inq_cnt         BIGINT,
    use_yn          CHAR(1),
    frst_crt_id     BIGINT,
    frst_crt_dt     TIMESTAMP,
    last_mdfcn_id   BIGINT,
    last_mdfcn_dt   TIMESTAMP
);

-- source: cmnty-mapper.xml (addCmnt / deleteCmnt / updateCmntCn)
-- domain: saleson.shop.community.doamin.CmntyCmntDto
CREATE TABLE IF NOT EXISTS g_cmnty_cmnt (
    cmnt_id         BIGSERIAL PRIMARY KEY,
    bbs_id          BIGINT,
    cmnt_cn         TEXT,
    use_yn          CHAR(1),
    frst_crt_id     BIGINT,
    frst_crt_dt     TIMESTAMP,
    last_mdfcn_id   BIGINT,
    last_mdfcn_dt   TIMESTAMP
);

-- source: cmnty-mapper.xml (insertFaqBbs, useGeneratedKeys keyProperty=bbsId)
-- domain: saleson.shop.community.doamin.CmntyFaqBbsDto
CREATE TABLE IF NOT EXISTS G_CMNTY_FAQ_BBS (
    BBS_ID          BIGSERIAL PRIMARY KEY,
    BBS_TTL         VARCHAR(255),
    BBS_CN          TEXT,
    FAQ_TYPE        VARCHAR(50),
    NOTICE_YN       CHAR(1),
    IS_SECRET       CHAR(1),
    INQ_CNT         BIGINT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (addFaqBbsCmnt / deleteFaqBbsCmnt / updateFaqBbsCmnt)
-- domain: saleson.shop.community.doamin.CmntyFaqBbsCmntDto
CREATE TABLE IF NOT EXISTS G_CMNTY_FAQ_BBS_CMNT (
    CMNT_ID         BIGSERIAL PRIMARY KEY,
    BBS_ID          BIGINT,
    CMNT_CN         TEXT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (insertFaqBbsCmntFile)
-- domain: saleson.shop.community.doamin.CmntyFaqBbsCmntFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_FAQ_BBS_CMNT_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    CMNT_ID              BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertFaqBbsFile)
-- domain: saleson.shop.community.doamin.CmntyFaqBbsFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_FAQ_BBS_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    BBS_ID               BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertRpstrFile)
-- domain: saleson.shop.community.doamin.CmntyFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    RPSTR_ID             BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertOffSrBbs, useGeneratedKeys keyProperty=bbsId)
-- domain: saleson.shop.community.doamin.CmntyOffSrBbsDto
CREATE TABLE IF NOT EXISTS G_CMNTY_OFF_SR_BBS (
    BBS_ID          BIGSERIAL PRIMARY KEY,
    BBS_TTL         VARCHAR(255),
    BBS_CN          TEXT,
    NOTICE_YN       CHAR(1),
    IS_SECRET       CHAR(1),
    INQ_CNT         BIGINT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (addOffSrBbsCmnt / deleteOffSrBbsCmnt / updateOffSrBbsCmnt)
-- domain: saleson.shop.community.doamin.CmntyOffSrBbsCmntDto
CREATE TABLE IF NOT EXISTS G_CMNTY_OFF_SR_BBS_CMNT (
    CMNT_ID         BIGSERIAL PRIMARY KEY,
    BBS_ID          BIGINT,
    CMNT_CN         TEXT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (insertOffSrBbsCmntFile)
-- domain: saleson.shop.community.doamin.CmntyOffSrBbsCmntFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_OFF_SR_BBS_CMNT_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    CMNT_ID              BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertOffSrBbsFile)
-- domain: saleson.shop.community.doamin.CmntyOffSrBbsFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_OFF_SR_BBS_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    BBS_ID               BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertRpstr, useGeneratedKeys keyProperty=rpstrId)
-- domain: saleson.shop.community.doamin.CmntyRpstrDto
CREATE TABLE IF NOT EXISTS G_CMNTY_RPSTR (
    RPSTR_ID        BIGSERIAL PRIMARY KEY,
    RPSTR_TTL       VARCHAR(255),
    RPSTR_CN        TEXT,
    NOTICE_YN       CHAR(1),
    INQ_CNT         BIGINT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (insertSrBbs, useGeneratedKeys keyProperty=bbsId)
-- domain: saleson.shop.community.doamin.CmntySrBbsDto
CREATE TABLE IF NOT EXISTS G_CMNTY_SR_BBS (
    BBS_ID          BIGSERIAL PRIMARY KEY,
    BBS_TTL         VARCHAR(255),
    BBS_CN          TEXT,
    NOTICE_YN       CHAR(1),
    IS_SECRET       CHAR(1),
    INQ_CNT         BIGINT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (addSrBbsCmnt / deleteSrBbsCmnt / updateSrBbsCmnt)
-- domain: saleson.shop.community.doamin.CmntySrBbsCmntDto
CREATE TABLE IF NOT EXISTS G_CMNTY_SR_BBS_CMNT (
    CMNT_ID         BIGSERIAL PRIMARY KEY,
    BBS_ID          BIGINT,
    CMNT_CN         TEXT,
    USE_YN          CHAR(1),
    FRST_CRT_ID     BIGINT,
    FRST_CRT_DT     TIMESTAMP,
    LAST_MDFCN_ID   BIGINT,
    LAST_MDFCN_DT   TIMESTAMP
);

-- source: cmnty-mapper.xml (insertSrBbsCmntFile)
-- domain: saleson.shop.community.doamin.CmntySrBbsCmntFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_SR_BBS_CMNT_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    CMNT_ID              BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- source: cmnty-mapper.xml (insertSrBbsFile)
-- domain: saleson.shop.community.doamin.CmntySrBbsFileDto
CREATE TABLE IF NOT EXISTS G_CMNTY_SR_BBS_FILE (
    FILE_ID              BIGSERIAL PRIMARY KEY,
    BBS_ID               BIGINT,
    ORGNL_ATCH_FILE_NM   VARCHAR(255),
    ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_EXTN_NM    VARCHAR(50),
    ATCH_FILE_SZ         BIGINT,
    ATCH_FILE_SEQ        INTEGER,
    ATCH_FILE_PATH_NM    VARCHAR(255),
    USE_YN               CHAR(1),
    FRST_CRT_ID          BIGINT,
    FRST_CRT_DT          TIMESTAMP,
    LAST_MDFCN_ID        BIGINT,
    LAST_MDFCN_DT        TIMESTAMP
);

-- =====================================================================
-- source: ngdonation-mapper.xml (insertSntrBuga / insertContryBuga / insertNextBugaCntr; columns merged with
-- UPDATE ... SET clauses across ngdonation-mapper.xml, mypage-mapper.xml, offgive-mapper.xml, order-give-point-mapper.xml)
-- Domain: saleson.shop.donation.support.SeoulParam / ContryParam / domain.HonorCntr (donation "G_CNTR" core table)
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_CNTR (
    CNTR_SN                 VARCHAR(50) NOT NULL, -- generated via g_cntr_cntr_sn.next_value sequence, concatenated w/ timestamp -> string id
    CNTR_DE                 VARCHAR(8),
    USER_ID                 BIGINT,
    PSITN_LOCGOV_CODE       VARCHAR(50),
    CNTR_LOCGOV_CODE        VARCHAR(50),
    CNTR_AMT                NUMERIC(15,2),
    CNTR_POINT              INTEGER,
    CNTR_BLCE_POINT         INTEGER,
    PAY_VALID_DE            VARCHAR(8),
    CNTR_PATH_CODE          VARCHAR(10),
    CNTR_STTUS_CODE         VARCHAR(10),
    ELCTRN_PAY_NO           VARCHAR(50),
    SEOUL_TRGET_AT          CHAR(1),
    INFO_AGRE_AT            CHAR(1),
    RTNPSNT_REQST_CODE      VARCHAR(10),
    FRST_REGISTER_ID        BIGINT,
    FRST_REGIST_PNTTM       VARCHAR(14),
    DELETE_AT               CHAR(1),
    FOREIGN_STATUS_CODE     VARCHAR(10),
    DSGN_DNTN_BIZ_ID        INTEGER,
    STTEMNT_PAY_DE          VARCHAR(14),
    POINT_END_DE            VARCHAR(8),
    NTS_STTUS_MSSAGE        VARCHAR(255),
    RCEPT_BANK_CODE         VARCHAR(50),
    RCEPT_BANK_NM           VARCHAR(255),
    LINK_INSTT_CD           VARCHAR(50),
    SUNAP_PROCSS_YN         CHAR(1),
    LAST_UPDUSR_ID          BIGINT,
    LAST_UPDT_PNTTM         VARCHAR(14),
    PRIMARY KEY (CNTR_SN)
);

-- source: locgov-mapper.xml
CREATE TABLE IF NOT EXISTS G_CNTR_LMTT (
    LMTT_BGN_DE       VARCHAR(8),
    LMTT_END_DE       VARCHAR(8),
    LOCGOV_CODE       VARCHAR(50),   -- guess: not a declared PK, but updateCntrLmmt/deleteCntrLmmt key off LOCGOV_CODE alone
    VIOLT_RESN_CODE    VARCHAR(50),
    VIOLT_RESN_CN      TEXT,
    FRST_REGISTER_ID   BIGINT,
    FRST_REGIST_PNTTM  TIMESTAMP,
    LAST_UPDUSR_ID     BIGINT,
    LAST_UPDT_PNTTM    TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE)  -- guess: see note above; assumes at most one active limitation record per locgov
);

-- =====================================================================
-- source: mypage-mapper.xml
-- Domain: saleson.shop.mypage.support.ReceiptParam
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_CNTR_RCIPT (
    CNTR_OUTPT_SN       INTEGER NOT NULL,
    CNTR_SN             VARCHAR(50) NOT NULL,
    ELCTRN_PAY_NO       VARCHAR(50),
    ISSU_DE             VARCHAR(8),
    FRST_REGISTER_ID    BIGINT,
    FRST_REGIST_PNTTM   VARCHAR(14),
    LAST_UPDUSR_ID      BIGINT,
    LAST_UPDT_PNTTM     VARCHAR(14),
    PRIMARY KEY (CNTR_OUTPT_SN)
);

-- ---------------------------------------------------------------------
-- source: give-state-mapper.xml (giveReqmngInsert / giveReqmngApprove / giveReqmngCancel)
-- domain: saleson.shop.give.givestate.domain.GiveStateTest
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_CNTR_REQMNG (
    REQ_ID                      BIGINT          PRIMARY KEY,
    LOGIN_ID                    VARCHAR(255),    -- crypto_enc encrypted value
    USER_NAME                   VARCHAR(255),    -- crypto_enc encrypted value
    LOCGOV_CODE                 VARCHAR(50),
    STTEMNT_PAY_DE              TIMESTAMP,
    CNTR_AMT                    NUMERIC(15,2),
    CNTR_REQMNG_CODE            VARCHAR(50),     -- 100: 과오납, 200: 포인트생성
    DISCRIPTION                 TEXT,
    FRST_REGISTER_ID            BIGINT,
    FRST_REGIST_PNTTM           TIMESTAMP,
    REQ_STATUS_CODE             VARCHAR(50),     -- 100:요청 200:취소 999:승인
    CNTR_SN                     VARCHAR(50),
    TAX_SYS_CANCEL_DE           TIMESTAMP,
    RELATED_DOC_DPT_NM          VARCHAR(255),
    RELATED_DOC_NUM             VARCHAR(100),
    RELATED_DOC_DE              TIMESTAMP,
    LAST_UPDUSR_ID              BIGINT,
    LAST_UPDT_PNTTM             TIMESTAMP,
    APPR_DT                     TIMESTAMP,
    CANCLE_DT                   TIMESTAMP,
    USE_YN                      CHAR(1)          -- compared to 'Y' in WHERE clauses
);

-- =====================================================================
-- source: ngdonation-mapper.xml
-- Domain: saleson.shop.donation.CntrTaxLogDto
-- =====================================================================
CREATE TABLE IF NOT EXISTS g_cntr_tax_log (
    elctrn_pay_no       VARCHAR(50) NOT NULL,
    sttemnt_pay_de      VARCHAR(14),
    cntr_amt            NUMERIC(15,2),
    tax_status_code     VARCHAR(10),
    conb_cd             VARCHAR(50),
    cntr_type           VARCHAR(10),
    nts_res_code        VARCHAR(10),
    nts_res_mssage      VARCHAR(255),
    elcr_apl_cd         VARCHAR(50),
    frst_regist_pnttm   TIMESTAMP, -- populated via SYS_DATETIME (native function), unlike most other *_pnttm columns in this codebase
    last_updt_pnttm     TIMESTAMP,
    PRIMARY KEY (elctrn_pay_no)
);

-- ---------------------------------------------------------------------
-- source: give-state-mapper.xml (insertCntrTaxTemp / updateCntrTaxTemp)
-- domain: saleson.shop.donation.CntrTaxTempDto
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_CNTR_TAX_TEMP (
    cntr_sn                     VARCHAR(50)     PRIMARY KEY,  -- guess: one temp tax row per contribution
    sttemnt_pay_de              TIMESTAMP,
    user_id                     BIGINT,
    mber_ci                     VARCHAR(255),
    cntr_amt                    NUMERIC(15,2),
    elctrn_pay_no                VARCHAR(50),
    biz_no                       VARCHAR(50),
    elcr_apl_cd                  VARCHAR(10),
    is_send                      INTEGER          -- referenced in updateCntrTaxTemp ("is_send = 0")
);

-- ---------------------------------------------------------------------
-- source: give-state-mapper.xml (insertCntrTaxTempLog)
-- domain: saleson.shop.donation.CntrTaxTempDto
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_CNTR_TAX_TEMP_LOG (
    -- TODO: primary key unclear (append-only log, no explicit ID column in mapper)
    cntr_sn                     VARCHAR(50),
    confirm_user_id             BIGINT,
    elcr_apl_cd                  VARCHAR(10),
    etc                          VARCHAR(255),
    frst_regist_pnttm           TIMESTAMP
);

-- =====================================================================
-- source: give-point-expiration-mapper.xml, generalcustomer-mapper.xml (batch_02),
--         order-give-point-mapper.xml (batch_04) -- merged (2 independent guesses)
-- Domain: saleson.shop.givepointexpiration.domain.GivePointExpirationTarget /
--         saleson.shop.user.domain.SecedeCntr / saleson.shop.order.givepoint.support.OrderGivePoint
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_CNTR_USE_POINT (
    CNTR_SN                 VARCHAR(50)     NOT NULL,
    USE_SN                  INTEGER         NOT NULL,
    POINT_USE_DE            VARCHAR(8), -- CONFLICT: batch_02 guessed TIMESTAMP, batch_04 guessed VARCHAR(8) (CommonMapper.date convention); chose VARCHAR(8)
    CNTR_USE_POINT          NUMERIC(15,2), -- CONFLICT: batch_02 guessed NUMERIC(15,2), batch_04 guessed INTEGER; chose NUMERIC(15,2) for decimal precision
    USER_ID                 BIGINT,
    PSITN_LOCGOV_CODE       VARCHAR(50),
    CNTR_LOCGOV_CODE        VARCHAR(50),
    USE_SE_CODE             VARCHAR(10),
    USE_CN                  TEXT, -- CONFLICT: batch_02 guessed VARCHAR(255), batch_04 guessed TEXT; chose TEXT
    ORDER_CODE              VARCHAR(50),
    URL_ADRES               VARCHAR(255),
    FRST_REGISTER_ID        BIGINT,
    FRST_REGIST_PNTTM       VARCHAR(14), -- CONFLICT: batch_02 guessed TIMESTAMP, batch_04 guessed VARCHAR(14) (CommonMapper.datetime convention); chose VARCHAR(14)
    LAST_UPDUSR_ID          BIGINT,
    LAST_UPDT_PNTTM         VARCHAR(14), -- CONFLICT: see FRST_REGIST_PNTTM
    PRIMARY KEY (CNTR_SN, USE_SN)
);

-- ---------------------------------------------------------------------
-- source: give-opertaion-mapper.xml (insertCtbnyOpratn)
-- domain: saleson.shop.give.giveoperation.domain.CtbnyOpratn
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_CTBNY_OPRATN (
    REGIST_SN                BIGINT          PRIMARY KEY,
    LOCGOV_CODE               VARCHAR(50),
    BSNS_PURPS_CODE          VARCHAR(50),
    BSNS_NM                  VARCHAR(255),
    BSNS_CN                  TEXT,
    EXPNDTR_DE                TIMESTAMP,
    EXPNDTR_AMT               NUMERIC(15,2),
    FRST_REGISTER_ID          BIGINT,
    FRST_REGIST_PNTTM        TIMESTAMP,
    LAST_UPDUSR_ID            BIGINT,
    LAST_UPDT_PNTTM          TIMESTAMP,
    RM                        TEXT
);

-- ---------------------------------------------------------------------
-- source: give-opertaion-mapper.xml (insertCtbnyOpratnFile)
-- domain: saleson.shop.give.giveoperation.domain.CtbnyOpratnFile
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_CTBNY_OPRATN_FILE (
    REGIST_FILE_ID            BIGINT          PRIMARY KEY,
    REGIST_SN                 BIGINT          NOT NULL,
    FILE_NM                   VARCHAR(255),
    ORGINL_FILE_NM             VARCHAR(255),
    FILE_TY                   VARCHAR(50),
    SORT_ORDR                  INTEGER,
    FRST_REGISTER_ID           BIGINT,
    FRST_REGIST_PNTTM         TIMESTAMP,
    LAST_UPDUSR_ID             BIGINT,
    LAST_UPDT_PNTTM           TIMESTAMP
);

-- source: locgov-mapper.xml
CREATE TABLE IF NOT EXISTS G_CTBNY_SETUP (
    STDR_YEAR           VARCHAR(4),
    LOCGOV_CODE         VARCHAR(50),
    LMT_AMT             INTEGER,
    POINT_RATE          NUMERIC(5,2),
    POINT_VALID_PD       INTEGER,
    FRST_REGISTER_ID     BIGINT,
    FRST_REGIST_PNTTM    TIMESTAMP,
    LAST_UPDUSR_ID       BIGINT,
    LAST_UPDT_PNTTM      TIMESTAMP,
    PRIMARY KEY (STDR_YEAR, LOCGOV_CODE)
);

-- source: designated-donation-mapper.xml (insertDsgncntrConfirmLog)
-- domain: saleson.shop.designateddonation.domain.DesignatedDonation (approval log)
-- TODO: primary key unclear — an append-only audit log with no ID column
-- referenced anywhere in the mapper; a surrogate identity is likely present
-- in the real schema but cannot be confirmed from the SQL alone.
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_APRV_LOG (
    DSGN_DNTN_BIZ_ID                    BIGINT,
    APRV_BFR_DSGN_DNTN_BIZ_STTS_CD      VARCHAR(10),
    APRV_AFTR_DSGN_DNTN_BIZ_STTS_CD     VARCHAR(10),
    FRST_RGTR_ID                        BIGINT
);

-- source: designated-donation-mapper.xml (insertImgDescList)
-- domain: saleson.shop.designateddonation.domain.PrjImageExplain
-- TODO: primary key unclear (composite of DSGN_DNTN_BIZ_ID + IMG_SEQ, based
-- on how rows are queried/deleted together, but never singly by an ID)
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_CN_IMG_EXPLN (
    DSGN_DNTN_BIZ_ID  BIGINT,
    IMG_EXPLN         VARCHAR(1000),
    IMG_SEQ           INTEGER,
    FRST_RGTR_ID      BIGINT
);

-- source: designated-donation-mapper.xml (insertDsgncntrPartMng)
-- domain: saleson.shop.designateddonation.domain.DesignatedPart
-- guess: DSGN_DNTN_BIZ_DEPT_ID not in the INSERT column list -> auto identity.
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_DEPT_MNG (
    DSGN_DNTN_BIZ_DEPT_ID   BIGSERIAL PRIMARY KEY,
    DSGN_DNTN_BIZ_DEPT_NM   VARCHAR(255),
    LCLGV_CD                VARCHAR(20),
    USE_YN                  CHAR(1),
    FRST_RGTR_ID            BIGINT,
    LAST_RGTR_ID            BIGINT,
    FRST_REG_DT              TIMESTAMP,
    LAST_REG_DT              TIMESTAMP
);

-- source: designated-donation-mapper.xml (insertDsgncntrPartMppng)
-- domain: saleson.shop.designateddonation.domain.DesignatedPart (partUserId)
-- guess: composite primary key (USER_ID, DSGN_DNTN_BIZ_DEPT_ID) — a manager
-- can be mapped to more than one department, deleteDsgncntrPartMppng only
-- filters by USER_ID (bulk delete of a user's mappings).
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_DEPT_MNGR_MPNG (
    USER_ID                 BIGINT NOT NULL,
    DSGN_DNTN_BIZ_DEPT_ID    BIGINT NOT NULL,
    FRST_RGTR_ID             BIGINT,
    LAST_RGTR_ID             BIGINT,
    PRIMARY KEY (USER_ID, DSGN_DNTN_BIZ_DEPT_ID)
);

-- source: designated-donation-mapper.xml (insertPrjImage)
-- domain: saleson.shop.designateddonation.domain.PrjImage
-- guess: DSGN_DNTN_BIZ_IMG_ID not in the INSERT column list -> auto identity.
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_IMG_MNG (
    DSGN_DNTN_BIZ_IMG_ID  BIGSERIAL PRIMARY KEY,
    DSGN_DNTN_BIZ_ID      BIGINT,
    IMG_NM                VARCHAR(255),
    IMG_SEQ               INTEGER,
    FRST_RGTR_ID          BIGINT
);

-- =====================================================================
-- designated-donation-mapper.xml — Korean local-government "designated
-- donation" (지정기부) module. Domain classes use java.sql.Timestamp for
-- audit columns (FRST_REG_DT / LAST_REG_DT), so those are TIMESTAMP; the
-- project start/end dates are String (yyyyMMdd) so kept as VARCHAR(8).
-- =====================================================================

-- source: designated-donation-mapper.xml (insertDesignatedDonationInfo,
-- useGeneratedKeys keyProperty=prjId)
-- domain: saleson.shop.designateddonation.domain.DesignatedDonation
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_MNG (
    DSGN_DNTN_BIZ_ID           BIGSERIAL PRIMARY KEY,
    DSGN_DNTN_BIZ_TTL          VARCHAR(255),
    DSGN_DNTN_BIZ_CN           TEXT,
    DSGN_DNTN_BIZ_BGNG_YMD     VARCHAR(8),
    DSGN_DNTN_BIZ_END_YMD      VARCHAR(8),
    GOAL_AMT                   BIGINT,
    DSGN_DNTN_BIZ_STTS_CD      VARCHAR(10),
    RLS_YN                     CHAR(1),
    DSGN_DNTN_BIZ_SE_CD        VARCHAR(50),
    DSGN_DNTN_BIZ_SE_DTL_CD    VARCHAR(50),
    LCLGV_CD                   VARCHAR(20),
    DSGN_DNTN_BIZ_ETC_CN       TEXT,
    DSGN_DNTN_BIZ_RPRS_IMG     VARCHAR(255),
    DSGN_DNTN_BIZ_DEPT_ID      BIGINT,
    FRST_RGTR_ID               BIGINT,
    LAST_RGTR_ID                BIGINT,
    FRST_REG_DT                 TIMESTAMP,
    LAST_REG_DT                 TIMESTAMP
);

-- source: designated-donation-mapper.xml (insertDesignatedDonationNoticeImgDesc)
-- domain: saleson.shop.designateddonation.domain.PrjNoticeImageExplain
-- TODO: primary key unclear (composite of DSGN_DNTN_BIZ_NTC_ID + IMG_SEQ)
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_CN_IMG_EXPLN (
    DSGN_DNTN_BIZ_NTC_ID  BIGINT,
    IMG_EXPLN             VARCHAR(1000),
    IMG_SEQ               INTEGER,
    FRST_RGTR_ID          BIGINT
);

-- source: designated-donation-mapper.xml (insertDesignatedDonationNoticeFile)
-- domain: saleson.shop.designateddonation.domain.PrjNoticeFile
-- guess: PRIMARY KEY inferred from deleteDesignatedDonationNoticeFile, which
-- filters by DSGN_DNTN_BIZ_NTC_ID + FILE_SEQ together.
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_FILE_MNG (
    DSGN_DNTN_BIZ_NTC_ID  BIGINT      NOT NULL,
    FILE_SEQ              INTEGER     NOT NULL,
    FILE_NM               VARCHAR(255),
    PATH_NM               VARCHAR(500),
    ORGNFL_NM             VARCHAR(255),
    FRST_RGTR_ID          BIGINT,
    PRIMARY KEY (DSGN_DNTN_BIZ_NTC_ID, FILE_SEQ)
);

-- source: designated-donation-mapper.xml (insertDesignatedDonationNotice,
-- useGeneratedKeys keyProperty=prjNoticeId)
-- domain: saleson.shop.designateddonation.domain.DesignatedDonationNotice
CREATE TABLE IF NOT EXISTS G_DSGN_DNTN_BIZ_NTC_MNG (
    DSGN_DNTN_BIZ_NTC_ID   BIGSERIAL PRIMARY KEY,
    DSGN_DNTN_BIZ_ID       BIGINT,
    DSGN_DNTN_BIZ_NTC_TTL  VARCHAR(255),
    DSGN_DNTN_BIZ_NTC_CN   TEXT,
    RLS_YN                 CHAR(1),
    FRST_RGTR_ID           BIGINT,
    LAST_RGTR_ID           BIGINT,
    LAST_REG_DT            TIMESTAMP
);

-- source: main-mapper.xml
-- TODO: primary key unclear -- no natural id; STDR_DE is a per-day snapshot, likely (STDR_DE) unique but not enforced in the mapper.
CREATE TABLE IF NOT EXISTS G_DY_GRAMT (
    CREATED_AT          VARCHAR(20),  -- DATE_FORMAT(SYS_DATETIME, '%Y%m%d%H%i%s%f')
    STDR_DE             VARCHAR(8),
    LAST_YEAR_GRAMT       BIGINT,
    NOW_YEAR_GRAMT        BIGINT
);

-- =====================================================================
-- source: locgov-mapper.xml (batch_03), ngdonation-mapper.xml (batch_04) -- merged
-- Domain: saleson.shop.donation.domain.HonorCntr
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_HONOR_CNTRBTR (
    STDR_YEAR                   INTEGER NOT NULL,
    LOCGOV_CODE                 VARCHAR(50) NOT NULL,
    USER_ID                     BIGINT NOT NULL,
    HONOR_CNTRBTR_LEVEL_CODE    VARCHAR(50), -- CONFLICT: batch_03 guessed VARCHAR(50), batch_04 guessed VARCHAR(10); chose VARCHAR(50)
    FRST_REGISTER_ID            BIGINT,
    FRST_REGIST_PNTTM           VARCHAR(14), -- CONFLICT: batch_03 guessed TIMESTAMP, batch_04 guessed VARCHAR(14) (CommonMapper.datetime convention); chose VARCHAR(14)
    LAST_UPDUSR_ID               BIGINT,
    LAST_UPDT_PNTTM              VARCHAR(14), -- CONFLICT: see FRST_REGIST_PNTTM
    PRIMARY KEY (STDR_YEAR, LOCGOV_CODE, USER_ID)
);

-- ---------------------------------------------------------------------
-- source: generalcustomer-mapper.xml (indvdlinfoReadngHist)
-- parameterType: saleson.shop.user.support.GeneralCustomerSearchParam (no dedicated domain class)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS g_indvdlinfo_readng_hist (
    readng_sn                  BIGINT          PRIMARY KEY,  -- guess: "_sn" sequence-number naming convention
    user_id                     BIGINT,
    readng_dt                  TIMESTAMP,
    trget_user_id               BIGINT
);

-- =====================================================================
-- source: join-mapper.xml (batch_03), ngdonation-mapper.xml (batch_04) -- merged
-- Domain: saleson.shop.user.domain.LocGovInfo
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_INTRST_LOCGOV (
    LOCGOV_CODE          VARCHAR(50) NOT NULL,
    USER_ID              BIGINT NOT NULL,
    REGIST_DE            VARCHAR(8),
    FRST_REGISTER_ID      BIGINT,
    FRST_REGIST_PNTTM     VARCHAR(14), -- CONFLICT: batch_03 guessed TIMESTAMP, batch_04 guessed VARCHAR(14) (CommonMapper.datetime convention); chose VARCHAR(14)
    LAST_UPDUSR_ID        BIGINT,
    LAST_UPDT_PNTTM       VARCHAR(14), -- CONFLICT: see FRST_REGIST_PNTTM
    -- CONFLICT (PK column order): batch_03 declared (LOCGOV_CODE, USER_ID), batch_04 declared (USER_ID, LOCGOV_CODE);
    -- same two natural-key columns either way, kept batch_03's ordering ("관심 지자체" interested-locgov link table)
    PRIMARY KEY (LOCGOV_CODE, USER_ID)
);

-- source: join-mapper.xml
CREATE TABLE IF NOT EXISTS G_INTRST_RTNPSNT (
    USER_ID              BIGINT,
    REGIST_SN            INTEGER,         -- per-user running sequence, computed via MAX(REGIST_SN)+1 subquery
    LOCGOV_CODE          VARCHAR(50),
    REGIST_DE            VARCHAR(8),
    CATEGORY_CODE         VARCHAR(50),
    FRST_REGISTER_ID       BIGINT,
    FRST_REGIST_PNTTM      TIMESTAMP,
    LAST_UPDUSR_ID         BIGINT,
    LAST_UPDT_PNTTM        TIMESTAMP,
    PRIMARY KEY (USER_ID, REGIST_SN)
);

-- source: item-mapper.xml
CREATE TABLE IF NOT EXISTS G_ITEM_CONTENT_IMG_DESC (
    ITEM_ID            INTEGER,
    IMG_DESC           TEXT,
    IMG_SEQ            INTEGER,
    CREATED_USER_ID     BIGINT,
    PRIMARY KEY (ITEM_ID, IMG_SEQ)  -- guess: no explicit PK in mapper; composite of item + per-item sequence is the natural key
);

-- =====================================================================
-- source: lclgvHnrUser-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_LCLGV_HNR_USER_RWRD_IMG_EXPLN (
    LCLGV_CD          VARCHAR(50),
    IMG_EXPLN         TEXT,
    IMG_SEQ           INTEGER,
    FRST_RGTR_ID      BIGINT,
    PRIMARY KEY (LCLGV_CD, IMG_SEQ)   -- guess: composite of locgov + per-locgov image sequence (delete/select both key off LCLGV_CD, ordered by IMG_SEQ)
);

-- source: item-mapper.xml
-- TODO: primary key unclear -- (LCLGV_CD, GDS_ID) may repeat with different OPTIONS values (offline item variant), no id column in INSERT list.
CREATE TABLE IF NOT EXISTS G_LCLGV_OFF_RPRS_GDS_MNG (
    LCLGV_CD       VARCHAR(50),
    GDS_ID         INTEGER,
    SORT_SEQ       INTEGER,
    FRST_RGTR_ID   BIGINT,
    OPTIONS        VARCHAR(255)
);

-- =====================================================================
-- source: offgive-mapper.xml (batch_04), welfarecenter-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.offgive.domain.WlfrCntrMng / saleson.shop.welfarecenter.WelfareCenterMapper
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_LCLGV_PBADMS_WLFR_CNTR_MNG (
    -- CONFLICT: batch_04 guessed INTEGER GENERATED BY DEFAULT AS IDENTITY, batch_07 guessed BIGINT
    -- (domain field "pbadmsWlfrCntrId" is a Java long); chose BIGINT identity
    PBADMS_WLFR_CNTR_ID    BIGINT GENERATED BY DEFAULT AS IDENTITY,
    LCLGV_CD               VARCHAR(50),
    PBADMS_WLFR_CNTR_NM    VARCHAR(255),
    PBADMS_WLFR_CNTR_CD    VARCHAR(50),
    USE_YN                 CHAR(1),
    FRST_REG_DT            TIMESTAMP,
    PRIMARY KEY (PBADMS_WLFR_CNTR_ID)
);

-- source: item-mapper.xml
-- guess: PK inferred as (LCLGV_CD, GDS_ID) -- deleteLclgvRprsGds removes all rows for a LCLGV_CD, insert is bulk per-item.
CREATE TABLE IF NOT EXISTS G_LCLGV_RPRS_GDS_MNG (
    LCLGV_CD       VARCHAR(50),
    GDS_ID         INTEGER,
    SORT_SEQ       INTEGER,
    FRST_RGTR_ID   BIGINT,
    PRIMARY KEY (LCLGV_CD, GDS_ID)
);

-- =====================================================================
-- source: locgov-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_LOCGOV (
    LOCGOV_CODE            VARCHAR(50) PRIMARY KEY,
    UPPER_LOCGOV_NM         VARCHAR(255),
    LOCGOV_NM              VARCHAR(255),
    UPPER_LOCGOV_CODE       VARCHAR(50),
    LOCGOV_INTRCN_CN        TEXT,
    CHARGER_CTTPC           VARCHAR(255),   -- crypto_enc()'d
    CHARGER_NM              VARCHAR(255),   -- crypto_enc()'d
    CHARGER_EMAIL           VARCHAR(255),   -- crypto_enc()'d
    CHARGER_PSITN_DEPT       VARCHAR(255),
    LOCGOV_HMPG             VARCHAR(255),
    LOCGOV_POPLTN_CO         VARCHAR(50),
    LOCGOV_AR               VARCHAR(50),
    LOCGOV_SPCPRD            VARCHAR(255),
    GCCT_USE_AT             CHAR(1),
    ETRCSH_USE_AT            CHAR(1),
    LOCGOV_BUDGET_AMT        BIGINT,
    BIZRNO                  VARCHAR(50),
    LOCGOV_ZIP              VARCHAR(20),
    BASS_ADRES              VARCHAR(255),
    DTL_ADRES               VARCHAR(255),
    ACHLQR_SLE_AT            CHAR(1),
    USE_AT                  CHAR(1),
    FRST_REGISTER_ID         BIGINT,
    FRST_REGIST_PNTTM        TIMESTAMP,
    LAST_UPDUSR_ID           BIGINT,
    LAST_UPDT_PNTTM          TIMESTAMP,
    PROCESS_DEPT_CODE        VARCHAR(50),
    ADMINIST_INSTT_CODE      VARCHAR(50),
    STDR_1LEVEL_AMT          INTEGER,
    STDR_2LEVEL_AMT          INTEGER,
    STDR_3LEVEL_AMT          INTEGER,
    OFFCS_NM                VARCHAR(255),
    OFFCS_FILE_NM            VARCHAR(255),
    ORGINL_FILE_NM           VARCHAR(255),
    FIS_SP                  VARCHAR(50)
);

-- source: locgov-mapper.xml
CREATE TABLE IF NOT EXISTS G_LOCGOV_DEPT_HIST (
    LOCGOV_CODE          VARCHAR(50),
    DEPT_HIST_NO          VARCHAR(20),   -- generated via NVL(MAX(DEPT_HIST_NO)+1,1) subquery, per-locgov running sequence
    PROCESS_DEPT_CODE      VARCHAR(50),
    FRST_REGISTER_ID       BIGINT,
    FRST_REGIST_PNTTM      TIMESTAMP,
    LAST_UPDUSR_ID         BIGINT,
    LAST_UPDT_PNTTM        TIMESTAMP,
    PRIMARY KEY (LOCGOV_CODE, DEPT_HIST_NO)
);

-- =====================================================================
-- source: locgov-image-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_LOCGOV_IMAGE (
    LOCGOV_CODE          VARCHAR(50) PRIMARY KEY,
    PC_FILE_NAME          VARCHAR(255),
    MOBILE_FILE_NAME       VARCHAR(255),
    FRST_REGISTER_ID       BIGINT,
    FRST_REGIST_PNTTM      TIMESTAMP,
    LAST_UPDUSR_ID         BIGINT,
    LAST_UPDT_PNTTM        TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: generalcustomer-mapper.xml (insertTotalCntrAmt)
-- domain: saleson.shop.user.domain.SecedeCntrAmt
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_MBER_SECSN (
    SECSN_YEAR                VARCHAR(4)      NOT NULL,
    USER_ID                    BIGINT          NOT NULL,
    MBER_CI                    VARCHAR(255),
    CNTR_AMT                   INTEGER,        -- Java field is Integer cntrAmt
    FRST_REGISTER_ID           BIGINT,
    FRST_REGIST_PNTTM         TIMESTAMP,
    LAST_UPDUSR_ID             BIGINT,
    LAST_UPDT_PNTTM           TIMESTAMP,
    -- guess: one contribution snapshot per user per year
    PRIMARY KEY (SECSN_YEAR, USER_ID)
);

-- =====================================================================
-- source: managerrequest-mapper.xml
-- Domain: saleson.shop.user.domain.ManagerRequest
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_MNGR_REQST (
    USER_ID             BIGINT NOT NULL,
    REQST_SN            INTEGER NOT NULL, -- next value computed per USER_ID
    LOGIN_ID            VARCHAR(255),
    LOCGOV_CODE         VARCHAR(50),
    REQST_SE_CODE       VARCHAR(50),
    PSITN_CODE          VARCHAR(50),
    PSITN_NM            VARCHAR(255),
    PSITN_DEPT_NM       VARCHAR(255),
    OFCPS_NM            VARCHAR(255),
    CTTPC               VARCHAR(255),
    CONFM_STTUS_CODE    VARCHAR(10),
    REJECT_RESN         TEXT,
    FRST_REGISTER_ID    BIGINT,
    FRST_REGIST_PNTTM   VARCHAR(14),
    LAST_UPDUSR_ID      BIGINT,
    LAST_UPDT_PNTTM     VARCHAR(14),
    PRIMARY KEY (USER_ID, REQST_SN)
);

-- =====================================================================
-- source: manual-mapper.xml
-- Domain: saleson.shop.manual.domain.Manual
-- table name kept lower-case, exactly as in SQL (g_mnl)
-- =====================================================================
CREATE TABLE IF NOT EXISTS g_mnl (
    mnl_sn              INTEGER NOT NULL,
    menu_se_code        VARCHAR(50),
    menu_url            VARCHAR(255),
    menu_nm             VARCHAR(255),
    file_nm             VARCHAR(255),
    orginl_file_nm      VARCHAR(255),
    file_ty             VARCHAR(50),
    inqire_co           INTEGER,
    menu_sj             VARCHAR(255),
    menu_cn             TEXT,
    frst_register_id    BIGINT,
    frst_regist_pnttm   VARCHAR(14),
    last_updusr_id      BIGINT,
    last_updt_pnttm     VARCHAR(14),
    PRIMARY KEY (mnl_sn)
);

-- source: totalsearch-myrecent-mapper.xml (saleson.shop.totalsearch.TotalSearchMapper)
CREATE TABLE IF NOT EXISTS G_MYRECENT (
    RECENT_ID INTEGER PRIMARY KEY,
    KEYWORD VARCHAR(255),
    USER_ID BIGINT,
    CREATED_DATE TIMESTAMP
);

-- =====================================================================
-- source: kakao-link-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_NAVER_AUTH_LOGIN_INFO_MNG (
    AUTH_TEMP_ID     VARCHAR(100) PRIMARY KEY,  -- guess: looked up by AUTH_TEMP_ID (deleteNaverAuthLoginInfoByTxId, selectNaverAuthLoginInfoByTxId) so treated as unique key
    ACCESS_TOKEN     VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- source: give-state-mapper.xml (insertGiveSunapInfo — INSERT INTO ... SELECT)
-- no resultMap/domain class; columns taken from the SELECT list of the insert
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS G_NEXT_SUNAP_RESPONSE (
    -- TODO: primary key unclear. LINK_MNG_KEY is used for lookups
    -- (getSunapInfo WHERE LINK_MNG_KEY = ...) but this looks like an
    -- append-style response/log table, so uniqueness is not guaranteed.
    LINK_MNG_KEY                VARCHAR(50),
    SGB_CD                      VARCHAR(50),
    SGB_NM                      VARCHAR(255),
    TAXT_NO                     VARCHAR(50),
    UNTY_TAXN_NO                VARCHAR(50),
    DPT_CD                      VARCHAR(50),
    DBT_NM                      VARCHAR(255),
    SPCL_FIS_BIZ_CD             VARCHAR(50),
    SPCL_FIS_BIZ_NM             VARCHAR(255),
    FYR                         VARCHAR(10),
    ACT_SE_CD                   VARCHAR(10),
    ACT_SE_NM                   VARCHAR(255),
    RPRS_TXM_CD                 VARCHAR(50),
    RPRS_TXM_NM                 VARCHAR(255),
    OPER_ITEM_CD                VARCHAR(50),
    OPER_ITEM_NM                VARCHAR(255),
    LVY_NO                      VARCHAR(50),
    ITM_NO                      VARCHAR(10),
    ELCTRN_PAY_NO               VARCHAR(50),
    RCVMT_NO                    VARCHAR(10),
    RCVMT_SE_CD                 VARCHAR(10),
    RCVMT_SE_NM                 VARCHAR(255),
    RCVMT_YMD                   TIMESTAMP,
    ACT_YMD                     TIMESTAMP,
    TSF_YMD                     TIMESTAMP,
    RCVMT_PCT_AMT               NUMERIC(15,2),
    RCVMT_ADTN_AMT              NUMERIC(15,2),
    RCVMT_INTR_AMT              NUMERIC(15,2),
    RCEPT_BANK_NM               VARCHAR(255),
    RCVMT_TY_CD                 VARCHAR(50),
    RCVMT_TY                    VARCHAR(50),
    RSVE_ITEM1                  VARCHAR(255),
    RSVE_ITEM2                  VARCHAR(255),
    RSVE_ITEM3                  VARCHAR(255),
    RSVE_ITEM4                  VARCHAR(255),
    RSVE_ITEM5                  VARCHAR(255),
    FRST_REGIST_PNTTM           TIMESTAMP
);

-- =====================================================================
-- source: order-mapper.xml
-- Domain: saleson.shop.order.domain.OrderAgencyOrderLog
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_ORDER_AGENCY_ORDER_LOG (
    ORDER_CODE               VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE            INTEGER NOT NULL,
    ITEM_SEQUENCE              INTEGER NOT NULL,
    MANAGER_ID                  BIGINT,
    MANAGER_LCLGV_CD             VARCHAR(50),
    MANAGER_NM                    VARCHAR(255),
    PBADMS_WLFR_CNTR_ID            INTEGER,
    USER_ID                         BIGINT,
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE)
);

-- source: cubrid/qustnr-mapper.xml
-- Domain: saleson.shop.qustnr.domain.Qestnar
-- guess: *_DE / *_PNTTM columns are Java Strings populated via
--        CommonMapper.datetime => VARCHAR, not TIMESTAMP.
CREATE TABLE IF NOT EXISTS G_QESTNAR (
    QUSTNR_SN         BIGINT NOT NULL,
    QUSTNR_SJ         VARCHAR(255),
    QUSTNR_PURPS      TEXT,
    QUSTNR_BGN_DE     VARCHAR(14),
    QUSTNR_END_DE     VARCHAR(14),
    FRST_REGISTER_ID  BIGINT,
    FRST_REGIST_PNTTM VARCHAR(14),
    LAST_UPDUSR_ID    BIGINT,
    LAST_UPDT_PNTTM   VARCHAR(14),
    SRVY_TRGT         VARCHAR(255),
    PRIMARY KEY (QUSTNR_SN)
);

-- =====================================================================
-- Batch 05b DDL — inferred from MyBatis mapper XML (CUBRID legacy SQL)
-- Scope: qna-admin, qna, qustnr, ranking-batch, ranking-config, ranking,
--        remittance, representativebanner, restock-notice, role,
--        search mappers
-- Ownership rule: a table is only defined here if this batch's mapper
-- files contain an INSERT INTO for it (see report for skipped tables).
-- =====================================================================


-- source: cubrid/qna-admin-mapper.xml
-- Domain: saleson.shop.qnaadmin.domain.QnaAdmin
-- guess: 지자체(local-government) Q&A. CREATED_DATE is compared with
--        TIMESTAMP(CONCAT(...),'yyyyMMddHHmmss') in SELECTs, consistent
--        with a native TIMESTAMP column.
CREATE TABLE IF NOT EXISTS G_QNA_ADMIN (
    QNA_ADMIN_ID     BIGINT NOT NULL,
    QNA_GROUP        VARCHAR(50),
    QNA_TYPE         VARCHAR(50),
    SUBJECT          VARCHAR(255),
    QUESTION         TEXT,
    USER_ID          BIGINT,
    USER_NAME        VARCHAR(255),
    SELLER_ID        BIGINT,
    SELLER_NAME      VARCHAR(255),
    EMAIL            VARCHAR(255),
    ITEM_ID          INTEGER,
    ORDER_CODE       VARCHAR(50),
    ANSWER_COUNT     BIGINT,
    SECRET_FLAG      CHAR(1),
    DISPLAY_FLAG     CHAR(1),
    DATA_STATUS_CODE VARCHAR(10),
    USE_YN           CHAR(1),
    HITS             INTEGER,
    LOCGOV_CODE      VARCHAR(50),
    CREATED_DATE     TIMESTAMP,
    PRIMARY KEY (QNA_ADMIN_ID)
);

-- source: cubrid/qna-admin-mapper.xml
-- Domain: saleson.shop.qnaadmin.domain.QnaAdminAnswer
CREATE TABLE IF NOT EXISTS G_QNA_ADMIN_ANSWER (
    QNA_ADMIN_ANSWER_ID BIGINT NOT NULL,
    QNA_ADMIN_ID        BIGINT,
    ANSWER               TEXT,
    USER_ID              BIGINT,
    TITLE                VARCHAR(255),
    SEND_SMS_FLAG        CHAR(1),
    SEND_MAIL_FLAG       CHAR(1),
    DATA_STATUS_CODE     VARCHAR(10),
    ANSWER_DATE          TIMESTAMP,
    PRIMARY KEY (QNA_ADMIN_ANSWER_ID)
);

-- source: cubrid/qna-admin-mapper.xml
-- Domain: saleson.shop.qnaadmin.domain.QnaAdminAnswerFile (extends QnaAdminFileBase)
CREATE TABLE IF NOT EXISTS G_QNA_ADMIN_ANSWER_FILE (
    QNA_ADMIN_ANSWER_FILE_ID BIGINT NOT NULL,
    QNA_ADMIN_ANSWER_ID      BIGINT,
    FILE_NAME                VARCHAR(255),
    FILE_TY                  VARCHAR(50),
    ORDERING                 INTEGER,
    CREATED_DATE             TIMESTAMP,
    ORG_FILE_NAME            VARCHAR(255),
    PATH_NAME                VARCHAR(255),
    PRIMARY KEY (QNA_ADMIN_ANSWER_FILE_ID)
);

-- source: cubrid/qna-admin-mapper.xml
-- Domain: saleson.shop.qnaadmin.domain.QnaAdminFile (extends QnaAdminFileBase)
CREATE TABLE IF NOT EXISTS G_QNA_ADMIN_FILE (
    QNA_ADMIN_FILE_ID BIGINT NOT NULL,
    QNA_ADMIN_ID      BIGINT,
    FILE_NAME         VARCHAR(255),
    FILE_TY           VARCHAR(50),
    ORDERING          INTEGER,
    CREATED_DATE      TIMESTAMP,
    ORG_FILE_NAME     VARCHAR(255),
    PATH_NAME         VARCHAR(255),
    PRIMARY KEY (QNA_ADMIN_FILE_ID)
);

-- source: cubrid/qustnr-mapper.xml
-- Domain: saleson.shop.qustnr.domain.QustnrIem
CREATE TABLE IF NOT EXISTS G_QUSTNR_IEM (
    QUSTNR_SN         BIGINT NOT NULL,
    QUSTNR_QESITM_SN  BIGINT NOT NULL,
    QUSTNR_IEM_SN     BIGINT NOT NULL,
    IEM_SN            INTEGER,
    IEM_CN            VARCHAR(255),
    ETC_ANSWER_AT     VARCHAR(1),
    FRST_REGISTER_ID  BIGINT,
    FRST_REGIST_PNTTM VARCHAR(14),
    LAST_UPDUSR_ID    BIGINT,
    LAST_UPDT_PNTTM   VARCHAR(14),
    PRIMARY KEY (QUSTNR_SN, QUSTNR_QESITM_SN, QUSTNR_IEM_SN)
);

-- source: cubrid/qustnr-mapper.xml
-- Domain: saleson.shop.qustnr.domain.QustnrQesitm
CREATE TABLE IF NOT EXISTS G_QUSTNR_QESITM (
    QUSTNR_SN         BIGINT NOT NULL,
    QUSTNR_QESITM_SN  BIGINT NOT NULL,
    QESTN_SN          BIGINT,
    QESTN_TY_CODE     VARCHAR(50),
    QESTN_CN          TEXT,
    ANSWER_CHOISE_CO  VARCHAR(50),
    FRST_REGISTER_ID  BIGINT,
    FRST_REGIST_PNTTM VARCHAR(14),
    LAST_UPDUSR_ID    BIGINT,
    LAST_UPDT_PNTTM   VARCHAR(14),
    PARENT_SN         INTEGER,
    PRIMARY KEY (QUSTNR_SN, QUSTNR_QESITM_SN)
);

-- source: cubrid/qustnr-mapper.xml
-- Domain: saleson.shop.qustnr.domain.QustnrRspnsResult
-- guess: no single-row lookup key defined; rows are inserted per
--        (survey, question, respondent, item) and bulk-deleted by QUSTNR_SN,
--        QUSTNR_QESITM_SN and/or QUSTNR_IEM_SN, so a composite natural key of
--        (QUSTNR_SN, QUSTNR_QESITM_SN, USER_ID, QUSTNR_IEM_SN) is used.
-- TODO: primary key unclear
CREATE TABLE IF NOT EXISTS G_QUSTNR_RSPNS_RESULT (
    QUSTNR_SN          BIGINT NOT NULL,
    QUSTNR_QESITM_SN   BIGINT NOT NULL,
    USER_ID            BIGINT NOT NULL,
    QUSTNR_IEM_SN      BIGINT,
    RESPOND_ANSWER_CN  VARCHAR(255),
    ETC_ANSWER_CN      VARCHAR(255),
    FRST_REGISTER_ID   BIGINT,
    FRST_REGIST_PNTTM  VARCHAR(14),
    LAST_UPDUSR_ID     BIGINT,
    LAST_UPDT_PNTTM    VARCHAR(14)
);

-- =====================================================================
-- source: ngdonation-mapper.xml
-- Domain: saleson.shop.log.support.RelayLogParam
-- =====================================================================
CREATE TABLE IF NOT EXISTS g_relay_log (
    user_id             BIGINT,
    relay_type          VARCHAR(10),
    cntr_locgov_code    VARCHAR(50),
    cntr_sn             VARCHAR(50),
    relay_result_code   VARCHAR(10),
    frst_regist_pnttm   VARCHAR(14) -- default/auto-populated, not present in explicit column list of insertRelayLog
    -- TODO: primary key unclear — append-only log table, no natural single-row lookup key in this mapper
);

-- source: cubrid/remittance-mapper.xml
-- Domain: saleson.shop.remittance.domain.RemittanceFile
-- guess: composite PK (REMITTANCE_ID, FILE_SEQ) inferred from
--        deleteRemittanceFile / getRemittanceFile WHERE clauses.
CREATE TABLE IF NOT EXISTS G_REMITTANCE_FILE (
    REMITTANCE_ID     BIGINT NOT NULL,
    FILE_SEQ          INTEGER NOT NULL,
    FILE_NAME         VARCHAR(255),
    PATH_NAME         VARCHAR(255),
    ORG_FILE_NAME     VARCHAR(255),
    FRST_REGISTER_ID  BIGINT,
    FRST_REGIST_PNTTM TIMESTAMP,
    LAST_UPDUSR_ID    BIGINT,
    LAST_UPDT_PNTTM   TIMESTAMP,
    PRIMARY KEY (REMITTANCE_ID, FILE_SEQ)
);

-- source: cubrid/representativebanner-mapper.xml
-- Domain: saleson.shop.representativebanner.domain.RepresentativeBanner
-- guess: FRST_REGIST_PNTTM / LAST_UPDT_PNTTM populated via
--        CommonMapper.datetime (String) => VARCHAR(14), not TIMESTAMP,
--        despite the Java createdDate field also being typed as String.
CREATE TABLE IF NOT EXISTS G_REPRST_BANNER (
    REPRST_BANNER_ID  INTEGER NOT NULL,
    TITLE             VARCHAR(255),
    FILE_NAME_PC      VARCHAR(255),
    FILE_NAME_MOBILE  VARCHAR(255),
    LINK_URL          VARCHAR(255),
    USE_YN            CHAR(1),
    DISPLAY_ORDER     INTEGER,
    BANNER_CONTENT    TEXT,
    PROCESS_TYPE      VARCHAR(50),
    FRST_REGISTER_ID  BIGINT,
    FRST_REGIST_PNTTM VARCHAR(14),
    LAST_UPDUSR_ID    BIGINT,
    LAST_UPDT_PNTTM   VARCHAR(14),
    PRIMARY KEY (REPRST_BANNER_ID)
);

-- =====================================================================
-- Batch 06 DDL — inferred from MyBatis mapper XML (CUBRID legacy SQL)
-- Scope: seasonal-food, secedeuser, security, seller/*, sellerconfirm,
--        sendmaillog, sendsmslog, seo, shadow-login-log, shipment,
--        shipment-return, sleepuser, sms, smsconfig, sns-user,
--        speciality-item, statistics-locgov mappers
-- Ownership rule: a table is only defined here if this batch's mapper
-- files contain an INSERT INTO for it (see report for skipped tables).
-- =====================================================================


-- source: cubrid/seasonal-food-mapper.xml
-- Domain: saleson.shop.seasonalfood.domain.SeasonalFood
-- guess: PK inferred as composite (SEASON_FOOD_MONTH, REG_SEQ) since a month
--        can have multiple keyword rows (REG_SEQ orders keywords within a
--        month); DELETE only filters by SEASON_FOOD_MONTH (bulk delete).
CREATE TABLE IF NOT EXISTS G_SEASON_FOOD (
    SEASON_FOOD_MONTH   INTEGER NOT NULL,
    SEASON_FOOD_KEYWORD VARCHAR(255),
    REG_SEQ              INTEGER NOT NULL,
    FRST_REGISTER_ID     BIGINT,
    FRST_REGIST_PNTTM    TIMESTAMP,
    LAST_UPDUSR_ID        BIGINT,
    LAST_UPDT_PNTTM       TIMESTAMP,
    PRIMARY KEY (SEASON_FOOD_MONTH, REG_SEQ)
);

-- source: item-mapper.xml
-- guess: PK inferred as (ITEM_ID, SEASON_FOOD_MONTH) -- Item.java holds a List<Integer> seasonFoodMonthList, implying an item can
-- have multiple season-food months, so ITEM_ID alone is not guaranteed unique.
CREATE TABLE IF NOT EXISTS G_SEASON_FOOD_ITEM (
    ITEM_ID              INTEGER,
    SEASON_FOOD_MONTH      INTEGER,
    FRST_REGISTER_ID       BIGINT,
    FRST_REGIST_PNTTM      TIMESTAMP,
    PRIMARY KEY (ITEM_ID, SEASON_FOOD_MONTH)
);

-- source: cubrid/speciality-item-mapper.xml
-- Domain: saleson.shop.specialityitem.domain.SpecialityItem (extends Item)
-- guess: PK inferred as composite (SPCL_ITEM_MNG_ID, ITEM_ID) — mapping
--        table linking a speciality-item group to selected OP_ITEM rows;
--        DELETE only filters by SPCL_ITEM_MNG_ID (bulk delete).
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM (
    SPCL_ITEM_MNG_ID    BIGINT NOT NULL,
    ITEM_ID              BIGINT NOT NULL,
    DISPLAY_ORDER        INTEGER,
    FRST_REGISTER_ID     BIGINT,
    FRST_REGIST_PNTTM    TIMESTAMP,
    LAST_UPDUSR_ID        BIGINT,
    LAST_UPDT_PNTTM       TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID, ITEM_ID)
);

-- source: cubrid/speciality-item-mapper.xml
-- Domain: saleson.shop.specialityitem.domain.SpecialityItemManage
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM_MNG (
    SPCL_ITEM_MNG_ID    BIGINT NOT NULL,
    LOCGOV_CODE          VARCHAR(50),
    SPCL_ITEM_INFO       TEXT,
    FRST_REGISTER_ID     BIGINT,
    FRST_REGIST_PNTTM    TIMESTAMP,
    LAST_UPDUSR_ID        BIGINT,
    LAST_UPDT_PNTTM       TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID)
);

-- source: cubrid/speciality-item-mapper.xml
-- Domain: saleson.shop.specialityitem.support.SpecialityItemParam (insert param)
-- guess: PK inferred as composite (SPCL_ITEM_MNG_ID, REG_SEQ) — REG_SEQ
--        orders multiple keywords per speciality-item group; DELETE only
--        filters by SPCL_ITEM_MNG_ID (bulk delete).
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM_MNG_KEYWORD (
    SPCL_ITEM_MNG_ID    BIGINT NOT NULL,
    SPCL_ITEM_KEYWORD   VARCHAR(255),
    REG_SEQ              INTEGER NOT NULL,
    FRST_REGISTER_ID     BIGINT,
    FRST_REGIST_PNTTM    TIMESTAMP,
    LAST_UPDUSR_ID        BIGINT,
    LAST_UPDT_PNTTM       TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID, REG_SEQ)
);

-- =====================================================================
-- source: maintenance-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS G_SR_MAINTENANCE (
    BBS_ID                     BIGINT PRIMARY KEY,   -- useGeneratedKeys="true" keyProperty="bbsId"
    SR_NO                      VARCHAR(50),
    KI_TYPE                    VARCHAR(10),
    REQ_CHANNEL                 VARCHAR(50),
    REQ_TYPE                    VARCHAR(50),
    PROCESS_TYPE                VARCHAR(50),
    PROCESS_STATE                VARCHAR(50),
    REQ_USER_PHONE_NUMBER          VARCHAR(255),  -- crypto_enc()'d
    REQ_USER_INFO                VARCHAR(255),  -- crypto_enc()'d
    REQ_CRT_DATE                 VARCHAR(8),    -- DATE_FORMAT(SYSDATE,'%Y%m%d')
    REQ_DT                      TIMESTAMP,
    BBS_TTL                     VARCHAR(255),
    BBS_CN                      TEXT,
    PROCESS_CN                   TEXT,          -- crypto_enc()'d
    PROCESS_MANAGER_NM             VARCHAR(255),  -- crypto_enc()'d
    PROCESS_MANAGER_ID             VARCHAR(50),
    PROCESS_RECEIPT_DATE            VARCHAR(8),
    PROCESS_TARGET_END_DATE          VARCHAR(8),
    PROCESS_START_DATE             VARCHAR(8),
    PROCESS_END_DATE               VARCHAR(8),
    URGENT_YN                    CHAR(1),
    REQ_SOURCE                   VARCHAR(50),
    INQ_CNT                      INTEGER,
    RM                          TEXT,
    USE_YN                       CHAR(1),
    FRST_CRT_ID                  BIGINT,
    FRST_CRT_DT                  TIMESTAMP,
    LAST_MDFCN_ID                 BIGINT,
    LAST_MDFCN_DT                 TIMESTAMP
);

-- source: maintenance-mapper.xml
CREATE TABLE IF NOT EXISTS G_SR_MAINTENANCE_FILE (
    FILE_ID                 BIGINT PRIMARY KEY,   -- guess: not in INSERT column list (auto-increment), selected/deleted by FILE_ID elsewhere
    BBS_ID                  BIGINT,
    ORGNL_ATCH_FILE_NM         VARCHAR(255),
    ATCH_FILE_NM              VARCHAR(255),
    ATCH_FILE_EXTN_NM          VARCHAR(20),
    ATCH_FILE_SZ              BIGINT,
    ATCH_FILE_SEQ              INTEGER,
    ATCH_FILE_PATH_NM          VARCHAR(255),
    USE_YN                   CHAR(1),
    FRST_CRT_ID               BIGINT,
    FRST_CRT_DT               TIMESTAMP,
    LAST_MDFCN_ID              BIGINT,
    LAST_MDFCN_DT              TIMESTAMP
);

-- =====================================================================
-- Batch 07 — DDL inferred from MyBatis mapper XML (CUBRID -> PostgreSQL)
-- Source mappers analyzed:
--   statistics-mapper.xml, stats-mapper.xml, store-inquiry-mapper.xml,
--   stsfdg-mapper.xml, sys-notice-seller-mapper.xml, temp-enc-data-mapper.xml,
--   temp-process-mapper.xml, totalsearch-myrecent-mapper.xml, transfer-mapper.xml,
--   user-mapper.xml, user-sns-mapper.xml, userDelivery-mapper.xml,
--   userauth-mapper.xml, usergroup-mapper.xml, userlevel-mapper.xml,
--   userrole-mapper.xml, vendor-mapper.xml, welfarecenter-mapper.xml,
--   wishlist-mapper.xml, zipcode-mapper.xml
--
-- Ownership rule: a table is only defined here if this batch's files contain
-- an INSERT INTO for it. Tables only ever SELECTed/JOINed in this batch are
-- skipped (see report) so another batch's owning mapper can define them.
-- =====================================================================


-- source: stsfdg-mapper.xml (saleson.shop.stsfdg.StsfdgMapper.joinSurvey)
-- Simple survey-submission log table. No SELECT/lookup exists in this batch,
-- so PK is a guess based on naming convention (serial number column).
CREATE TABLE IF NOT EXISTS g_stsfdg (
    stsfdg_sn INTEGER PRIMARY KEY, -- guess: PK inferred from "_sn" (serial number) naming, no lookup query to confirm
    menu_url VARCHAR(255),
    menu_nm VARCHAR(255), -- populated via subquery against op_common_code, not a bound param
    stsfdg TEXT,
    frst_regist_pnttm TIMESTAMP
);

-- source: temp-enc-data-mapper.xml (saleson.shop.disposable.TempDataMapper)
CREATE TABLE IF NOT EXISTS G_TEMP_ENC_DATA (
    DATA_ID VARCHAR(255) PRIMARY KEY,
    ENC_DATA TEXT,
    REG_DT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- source: transfer-mapper.xml (saleson.shop.transfer.TransferMapper.insertOpItemImage)
-- NOTE: temp_op_item_image insert in this file is entirely commented out (dead code) - not created.
CREATE TABLE IF NOT EXISTS MIG_OP_ITEM_IMAGE (
    ITEM_IMAGE_ID BIGINT PRIMARY KEY, -- guess: type/PK inferred from naming only, parameterType is a generic HashMap
    ITEM_ID BIGINT,
    IMAGE_NAME VARCHAR(255),
    ORDERING INTEGER,
    CREATED_DATE TIMESTAMP,
    SERIAL_NUM VARCHAR(50)
);

-- =====================================================================
-- source: nuri2-mapper.xml
-- Domain: (no resultMap/parameterType class; inferred from INSERT column list)
-- =====================================================================
CREATE TABLE IF NOT EXISTS NURI2_NRMSG_DATA (
    MSG_KEY             BIGINT NOT NULL, -- generated via MSG_KEY_SERIAL.NEXT_VALUE sequence
    MSG_STATE           INTEGER,
    INPUT_DATE          TIMESTAMP,
    RES_DATE            TIMESTAMP,
    ALT_COUNTRY_CODE    VARCHAR(10),
    PHONE               VARCHAR(50),
    CALLBACK            VARCHAR(50),
    MSG_TYPE_1          VARCHAR(10),
    CONTENTS_TYPE_1     VARCHAR(10),
    ALT_SENDER_KEY      VARCHAR(255),
    ALT_TEMPLATE_CODE   VARCHAR(255),
    ALT_JSON            TEXT,
    MSG_TYPE_2          VARCHAR(10),
    CONTENTS_TYPE_2     VARCHAR(10),
    XMS_SUBJECT         VARCHAR(255),
    XMS_TEXT            TEXT,
    PRIMARY KEY (MSG_KEY)
);

-- =====================================================================
-- source: accountnumber-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ACCOUNT_NUMBER (
	SHOP_CONFIG_ID    INTEGER,
	ACCOUNT_NUMBER_ID INTEGER,
	BANK_NAME         VARCHAR(255),
	ACCOUNT_NUMBER    VARCHAR(50),
	ACCOUNT_HOLDER    VARCHAR(255),
	USE_FLAG          CHAR(1),   -- compared to 'Y'
	USER_ID           BIGINT,
	CREATED           TIMESTAMP,
	PRIMARY KEY (SHOP_CONFIG_ID, ACCOUNT_NUMBER_ID) -- guess: always looked up/updated together
);

-- =====================================================================
-- GHLOVE schema reverse-engineered from MyBatis mapper XML (batch_00)
-- Source mappers: access, accountnumber, alimtalk, attendance, banword,
--   batch-job, batch-log, batch/categories, batch/couponregular,
--   batch/featured, batch/item, batch/ranking, bix5, board, brand,
--   businesscode, calendar, cardbenefits, cart, catalog, categories
-- Column/table names preserved exactly as found in SQL (case as-is;
-- Postgres folds unquoted identifiers to lowercase).
-- =====================================================================


-- =====================================================================
-- source: access-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ALLOW_IP (
	ALLOW_IP_ID     INTEGER PRIMARY KEY,
	ACCESS_TYPE     VARCHAR(50),
	REMOTE_ADDR     VARCHAR(255), -- guess: encrypted via crypto_enc(), stored as opaque string
	DISPLAY_FLAG    CHAR(1),      -- Y/N flag, compared to 'N'/'Y' in mapper
	CREATED_USER    VARCHAR(50),
	CREATED_DATE    TIMESTAMP,
	UPDATED_USER    VARCHAR(50),
	UPDATED_DATE    TIMESTAMP
);

-- =====================================================================
-- source: attendance-mapper.xml
-- Domain classes: saleson.shop.attendance.domain.{Attendance,AttendanceConfig,AttendanceCheck,AttendanceEvent}
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ATTENDANCE (
	ATTENDANCE_ID    INTEGER PRIMARY KEY,
	"YEAR"           VARCHAR(4),
	"MONTH"          VARCHAR(2),
	CONTENT_TOP      TEXT,
	CONTENT_BOTTOM   TEXT,
	UPDATED_BY       VARCHAR(50),
	UPDATED_DATE     TIMESTAMP,
	CREATED_BY       VARCHAR(50),
	CREATED_DATE     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_CHECK (
	ATTENDANCE_CHECK_ID INTEGER PRIMARY KEY,
	ATTENDANCE_ID        INTEGER,
	USER_ID              BIGINT,
	CHECKED_DATE         VARCHAR(20),
	CHECKED_TIME         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_CONFIG (
	ATTENDANCE_CONFIG_ID INTEGER PRIMARY KEY,
	ATTENDANCE_ID        INTEGER,
	EVENT_CODE           VARCHAR(50),
	CONTINUE_YN          CHAR(1),
	DAYS                 INTEGER,
	UPDATED_BY           VARCHAR(50),
	UPDATED_DATE         TIMESTAMP,
	CREATED_BY           VARCHAR(50),
	CREATED_DATE         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OP_ATTENDANCE_EVENT (
	ATTENDANCE_EVENT_ID INTEGER PRIMARY KEY,
	ATTENDANCE_ID        INTEGER,
	USER_ID              BIGINT,
	EVENT_CODE           VARCHAR(50),
	CONTINUE_YN          CHAR(1),
	DAYS                 INTEGER,
	CHECKED_DAYS         INTEGER,
	SUCCESS_YN           CHAR(1),
	UPDATED_DATE         TIMESTAMP
);

-- =====================================================================
-- source: banword-mapper.xml
-- Domain class: saleson.shop.banword.domain.BanWord
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_BAN_WORD (
	BAN_WORD_ID   INTEGER PRIMARY KEY,
	BAN_WORD      VARCHAR(255),
	USER_ID       BIGINT,
	USERNAME      VARCHAR(255),
	CREATION_DATE TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OP_BATCH_EXECUTION ( -- TODO: primary key unclear (merged via ON DUPLICATE KEY, no explicit unique cols visible)
	BATCH_TYPE      VARCHAR(255),
	EXECUTION_DATE  VARCHAR(20),
	START_TIME      VARCHAR(20),
	END_TIME        VARCHAR(20),
	"RESULT"        CHAR(1),
	MESSAGE         TEXT
);

-- =====================================================================
-- source: batch-job-mapper.xml
-- Domain classes: saleson.batch.domain.{BatchJob}, saleson.common.scheduling.domain.BatchExecution
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_BATCH_JOB (
	BATCH_JOB_ID              VARCHAR(50) PRIMARY KEY,
	JOB_NAME                  VARCHAR(255),
	JOB_METHOD                VARCHAR(255),
	TRIGGER_TYPE              CHAR(1),  -- 1:심플, 2:크론
	TRIGGER_REPEAT_SECONDS    INTEGER,
	TRIGGER_CRON_EXPRESSION   VARCHAR(255),
	BATCH_STATUS              CHAR(1),  -- 1:실행중, 2:정지
	BATCH_EXCUTE_DATE         VARCHAR(20),
	BATCH_APPLY_FLAG          CHAR(1),  -- 0:적용전, 1:적용완료
	ORDERING                  INTEGER
);

-- =====================================================================
-- source: board-mapper.xml
-- Domain classes: com.onlinepowers.board.domain.{Board,BoardCfg,BoardComment}
-- (external framework classes, not present in this repo - types inferred
-- from resultMap column list + naming convention)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_BOARD_CFG (
	BOARD_CODE            VARCHAR(50) PRIMARY KEY,
	GROUP_CODE            VARCHAR(50),
	"LANGUAGE"            VARCHAR(10),
	SUBJECT               VARCHAR(255),
	BOARD_ADMIN           VARCHAR(255),
	BOARD_TEMPLATE        VARCHAR(255),
	BOARD_LAYOUT          VARCHAR(255),
	BOARD_SKIN            VARCHAR(255),
	BOARD_HEADER          TEXT,
	BOARD_FOOTER          TEXT,
	LIST_AUTHORITY        VARCHAR(50),
	READ_AUTHORITY        VARCHAR(50),
	WRITE_AUTHORITY       VARCHAR(50),
	REPLY_AUTHORITY       VARCHAR(50),
	ANSWER_AUTHORITY      VARCHAR(50),
	COMMENT_AUTHORITY     VARCHAR(50),
	UPLOAD_AUTHORITY      VARCHAR(50),
	DOWNLOAD_AUTHORITY    VARCHAR(50),
	APPROVAL_AUTHORITY    VARCHAR(50),
	SORT_FIELD            VARCHAR(50),
	PAGE_SIZE              INTEGER,
	BLOCK_SIZE              INTEGER,
	SUBJECT_LENGTH          INTEGER,
	CONTENT_LENGTH          INTEGER,
	UPLOAD_COUNT            INTEGER,
	UPLOAD_SIZE             INTEGER,
	UPLOAD_EXTENSION        VARCHAR(255),
	UPLOAD_THUMBNAIL        CHAR(1),
	UPLOAD_ENCRYPT          CHAR(1),
	MODIFY_COMMENT_COUNT    INTEGER,
	POINT_READ              INTEGER,
	POINT_WRITE             INTEGER,
	POINT_COMMENT           INTEGER,
	POINT_DOWNLOAD          INTEGER,
	DISABLE_TAGS            VARCHAR(255),
	SHOW_NEW_ICON           CHAR(1),
	SHOW_HOT_ICON           CHAR(1),
	NOTICE_BOARD_ID         INTEGER,
	CATEGORY_LIST           VARCHAR(255),
	USE_CATEGORY            CHAR(1),
	USE_CALENDAR            CHAR(1),
	USE_NOTICE              CHAR(1),
	USE_REPLY               CHAR(1),
	USE_ANSWER              CHAR(1),
	USE_SEARCH              CHAR(1),
	USE_EMAIL               CHAR(1),
	USE_TAG                 CHAR(1),
	USE_SECRET              CHAR(1),
	USE_EDITOR              CHAR(1),
	USE_SWFUPLOAD            CHAR(1),
	USE_RSS                 CHAR(1),
	USE_COMMENT             CHAR(1),
	USE_GOOD                CHAR(1),
	USE_NO_GOOD             CHAR(1),
	USE_SOCIAL              CHAR(1),
	USE_APPROVAL            CHAR(1),
	USE_VIEW_LIST           CHAR(1),
	USE_VIEW_IMAGE          CHAR(1),
	USE_VIEW_MOVIE          CHAR(1),
	USE_VIEW_FLV            CHAR(1),
	ETC_TITLE1              VARCHAR(255),
	ETC_TITLE2              VARCHAR(255),
	ETC_TITLE3              VARCHAR(255),
	ETC_TITLE4              VARCHAR(255),
	ETC_TITLE5              VARCHAR(255),
	ETC_TITLE6              VARCHAR(255),
	ETC_TITLE7              VARCHAR(255),
	ETC_TITLE8              VARCHAR(255),
	ETC_TITLE9              VARCHAR(255),
	ETC_TITLE10             VARCHAR(255),
	ETC1                    VARCHAR(255),
	ETC2                    VARCHAR(255),
	ETC3                    VARCHAR(255),
	ETC4                    VARCHAR(255),
	ETC5                    VARCHAR(255),
	ETC6                    VARCHAR(255),
	ETC7                    VARCHAR(255),
	ETC8                    VARCHAR(255),
	ETC9                    VARCHAR(255),
	ETC10                   VARCHAR(255),
	STATUS_CODE             VARCHAR(2),
	CREATION_DATE           TIMESTAMP,
	NEW_ICON_DATE           TIMESTAMP
);

CREATE TABLE IF NOT EXISTS OP_BOARD_COMMENT (
	BOARD_ID           INTEGER,
	BOARD_COMMENT_ID   INTEGER PRIMARY KEY,
	BOARD_CODE         VARCHAR(50),
	USER_ID            BIGINT,
	USER_NAME          VARCHAR(255),
	COMMENTS           TEXT,
	PASSWORD           VARCHAR(255),
	REMOTE_ADDR        VARCHAR(50),
	CREATION_DATE      TIMESTAMP,
	CREATED_TIMESTAMP  TIMESTAMP,
	MANAGER_ID         BIGINT
);

-- Dynamic per-board data table: real table names are OP_BOARD_DATA_<boardTable>
-- (e.g. OP_BOARD_DATA_NOTICE, OP_BOARD_DATA_FAQ, ...), substituted via
-- #{boardTable} in the mapper. This is a representative/template definition.
CREATE TABLE IF NOT EXISTS OP_BOARD_DATA ( -- guess: template for OP_BOARD_DATA_<boardTable> dynamic tables
	BOARD_ID              INTEGER PRIMARY KEY,
	BOARD_CODE            VARCHAR(50),
	GROUP_ID              INTEGER,
	STEP                  INTEGER,
	"DEPTH"               INTEGER,
	PARENT_ID             INTEGER,
	CATEGORY              VARCHAR(50),
	SUBJECT               VARCHAR(255),
	CONTENT               TEXT,
	ANSWER                TEXT,
	USER_ID               BIGINT,
	PASSWORD              VARCHAR(255),
	USER_NAME             VARCHAR(255),
	EMAIL                 VARCHAR(255),
	HOMEPAGE              VARCHAR(255),
	TAG                   VARCHAR(255),
	SCHEDULE_DATE         TIMESTAMP,
	HIT                   INTEGER,
	FILE_COUNT            INTEGER,
	COMMENT_COUNT         INTEGER,
	HTML                  CHAR(1),
	NOTICE                CHAR(1),
	SECRET                CHAR(1),
	STATUS_CODE           VARCHAR(2),
	ANSWER_STATUS_CODE    VARCHAR(2),
	REMOTE_ADDR           VARCHAR(50),
	UPDATED_USER_ID       BIGINT,
	UPDATED_USER_NAME     VARCHAR(255),
	UPDATED_DATE          TIMESTAMP,
	CREATION_DATE         TIMESTAMP,
	CREATION_TIMESTAMP    TIMESTAMP,
	ETC1                  VARCHAR(255),
	ETC2                  VARCHAR(255),
	ETC3                  VARCHAR(255),
	ETC4                  VARCHAR(255),
	ETC5                  VARCHAR(255),
	ETC6                  VARCHAR(255),
	ETC7                  VARCHAR(255),
	ETC8                  VARCHAR(255),
	ETC9                  VARCHAR(255),
	ETC10                 VARCHAR(255),
	MANAGER_ID            BIGINT
);

CREATE TABLE IF NOT EXISTS OP_BOARD_NEW (
	BOARD_ID       INTEGER,
	BOARD_NEW_ID   INTEGER PRIMARY KEY,
	GROUP_CODE     VARCHAR(50),
	BOARD_CODE     VARCHAR(50),
	USER_ID        BIGINT,
	SUBJECT        VARCHAR(255),
	CONTENT        TEXT,
	TAG            VARCHAR(255),
	SECRET         CHAR(1),
	CREATION_DATE  TIMESTAMP
);

-- NOTE: OP_BOARD_TAG and OP_BOARD_RECOMMEND are referenced (DELETE only) in this
-- file, never INSERTed here, so not owned/defined in this batch.


-- =====================================================================
-- source: brand-mapper.xml
-- Domain classes: saleson.shop.brand.domain.{Brand,BrandCategory}
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_BRAND (
	BRAND_ID         INTEGER PRIMARY KEY,
	BRAND_NAME       VARCHAR(255),
	BRAND_IMAGE      VARCHAR(255),
	BRAND_CONTENT    TEXT,
	DISPLAY_FLAG     CHAR(1),
	UPDATED_USER_ID  BIGINT,
	UPDATED_DATE     TIMESTAMP,
	CREATED_USER_ID  BIGINT,
	CREATED_DATE     TIMESTAMP,
	LOCGOV_CODE      VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS OP_BRAND_CATEGORY (
	BRAND_CATEGORY_ID INTEGER PRIMARY KEY,
	BRAND_ID          INTEGER,
	CATEGORY_ID       INTEGER,
	CREATED_DATE      TIMESTAMP
);

-- =====================================================================
-- source: businesscode-mapper.xml
-- Domain class: saleson.shop.businesscode.domain.BusinessCode
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_BUSINESS_CODE (
	BUSINESS_CODE_ID INTEGER PRIMARY KEY,
	SHOP_CONFIG_ID   INTEGER,
	CODE_TYPE        VARCHAR(50),
	"LANGUAGE"       VARCHAR(10),
	ID               VARCHAR(50),
	ORDERING         INTEGER,
	USE_YN           CHAR(1)
);

-- =====================================================================
-- source: calendar-mapper.xml
-- Domain class: saleson.shop.calendar.domain.Calendar
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_CALENDAR (
	CALENDAR_ID     INTEGER PRIMARY KEY,
	CALENDAR_YEAR   VARCHAR(4),
	CALENDAR_MONTH  VARCHAR(2),
	CALENDAR_DAY    VARCHAR(2),
	SUBJECT         VARCHAR(255),
	HDAY            VARCHAR(1) -- holiday flag
);

-- =====================================================================
-- source: cardbenefits-mapper.xml
-- Domain class: saleson.shop.cardbenefits.domain.CardBenefits
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_CARD_BENEFITS (
	BENEFITS_ID    INTEGER PRIMARY KEY,
	SUBJECT        VARCHAR(255),
	CONTENT        TEXT,
	START_DATE     VARCHAR(20),
	END_DATE       VARCHAR(20),
	CREATED_DATE   VARCHAR(20)
);

-- =====================================================================
-- source: cart-mapper.xml
-- Domain class: saleson.shop.cart.domain.Cart
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_CART (
	CART_ID                INTEGER PRIMARY KEY,
	SESSION_ID              VARCHAR(255),
	USER_ID                 BIGINT,
	ITEM_ID                 INTEGER,
	QUANTITY                INTEGER,
	OPTIONS                 VARCHAR(255),
	SHIPPING_PAYMENT_TYPE   VARCHAR(1),
	SHIPPING_GROUP_CODE     VARCHAR(50),
	ADDITION_ITEM_FLAG      CHAR(1),
	PARENT_ITEM_ID          INTEGER,
	SET_ITEM_FLAG           CHAR(1),
	CREATED_DATE            TIMESTAMP,
	TEXT_OPTION             VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS OP_CART_SET ( -- TODO: primary key unclear (no single ID column in insert list)
	CART_ID          INTEGER,
	ITEM_ID          INTEGER,
	QUANTITY         INTEGER,
	OPTIONS          VARCHAR(255),
	PARENT_CART_ID   INTEGER,
	CREATED_DATE     TIMESTAMP
);

-- =====================================================================
-- source: categories-mapper.xml
-- Domain class: saleson.shop.categories.domain.Categories (+ nested saleson.shop.seo.domain.Seo)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_CATEGORY (
	CATEGORY_ID                    INTEGER PRIMARY KEY,
	CATEGORY_CODE                  VARCHAR(50),
	CATEGORY_GROUP_ID              INTEGER,
	CATEGORY_URL                   VARCHAR(255),
	CATEGORY_NAME                  VARCHAR(255),
	CATEGORY_TYPE                  VARCHAR(1),
	CATEGORY_HEADER                TEXT,
	CATEGORY_MOBILE_HTML           TEXT,
	CATEGORY_MOBILE_HTML_HEADER    TEXT,
	CATEGORY_ADVERTISEMENT         TEXT,
	CATEGORY_FOOTER                TEXT,
	CATEGORY_BANNER                VARCHAR(255),
	CATEGORY_CLASS1                VARCHAR(20),
	CATEGORY_CLASS2                VARCHAR(20),
	CATEGORY_CLASS3                VARCHAR(20),
	CATEGORY_CLASS4                VARCHAR(20),
	CATEGORY_LEVEL                 INTEGER,
	ORDERING                       INTEGER,
	CATEGORY_FLAG                  CHAR(1),
	ACCESS_TYPE                    VARCHAR(1),
	CHILD_MAX_LEVEL                INTEGER,
	CATEGORY_COUNT                 INTEGER,
	TITLE                          VARCHAR(255),
	KEYWORDS                       VARCHAR(500),
	DESCRIPTION                    TEXT,
	HEADER_CONTENTS1               TEXT,
	HEADER_CONTENTS2               TEXT,
	HEADER_CONTENTS3               TEXT,
	THEMAWORD_TITLE                VARCHAR(255),
	THEMAWORD_DESCRIPTION          TEXT,
	RANK_TITLE                     VARCHAR(255),
	RANK_KEYWORDS                  VARCHAR(500),
	RANK_DESCRIPTION               TEXT,
	RANK_HEADERCONTENTS1           TEXT,
	RANK_THEMAWORD_TITLE           VARCHAR(255),
	RANK_THEMAWORD_DESCRIPTION     TEXT,
	REVIEW_TITLE                   VARCHAR(255),
	REVIEW_KEYWORDS                VARCHAR(500),
	REVIEW_DESCRIPTION             TEXT,
	REVIEW_HEADERCONTENTS1         TEXT,
	REVIEW_THEMAWORD_TITLE         VARCHAR(255),
	REVIEW_THEMAWORD_DESCRIPTION   TEXT
);

-- ---------------------------------------------------------------------
-- source: categoriesedit-mapper.xml (insertCategoryEdit)
-- domain: saleson.shop.categoriesedit.domain.CategoriesEdit
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CATEGORY_EDIT (
    CATEGORY_EDIT_ID  INTEGER PRIMARY KEY,
    CODE              VARCHAR(50),
    EDIT_KIND         VARCHAR(50),
    EDIT_POSITION     VARCHAR(50),
    EDIT_CONTENT      TEXT,
    EDIT_IMAGE        VARCHAR(255),
    EDIT_URL          VARCHAR(255),
    CREATED_DATE      VARCHAR(20),
    UPDATED_DATE      VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: categoriesteamgroup-mapper.xml (insertCategoriesGroup)
-- domain: saleson.shop.categoriesteamgroup.domain.CategoriesGroup
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CATEGORY_GROUP (
    CATEGORY_GROUP_ID         INTEGER PRIMARY KEY,
    CATEGORY_TEAM_ID          INTEGER,
    NAME                      VARCHAR(255),
    CODE                      VARCHAR(50),
    CATEGORY_GROUP_FLAG       CHAR(1),
    ACCESS_TYPE               VARCHAR(50),
    DEFCATE                   VARCHAR(50),
    TITLE                     VARCHAR(255),
    KEYWORDS                  VARCHAR(255),
    DESCRIPTION               TEXT,
    HEADER_CONTENTS1          TEXT,
    HEADER_CONTENTS2          TEXT,
    HEADER_CONTENTS3          TEXT,
    THEMAWORD_TITLE           VARCHAR(255),
    THEMAWORD_DESCRIPTION     TEXT,
    RANK_TITLE                VARCHAR(255),
    RANK_KEYWORDS             VARCHAR(255),
    RANK_DESCRIPTION          TEXT,
    RANK_HEADERCONTENTS1      TEXT,
    RANK_THEMAWORD_TITLE      VARCHAR(255),
    RANK_THEMAWORD_DESCRIPTION TEXT,
    ORDERING                  INTEGER,
    ITEM_LIST                 TEXT,
    CREATED_DATE              VARCHAR(20),
    UPDATED_DATE              VARCHAR(20)
);

-- =====================================================================
-- GHLOVE schema reverse-engineered from MyBatis/CUBRID SQL mappers
-- Batch 03
-- Source mappers (cubrid/*.xml):
--   groupbanner, help, inquiry, item-addition, item-front, item, join,
--   kakao-link, keyword, lclgvHnrUser, locgFaq, locgov-databoard,
--   locgov-image, locgov, log-manage, login-log, mailconfig, main,
--   mainbanner, maindisplayitem, maintenance, manager-action-log
--
-- Ownership rule: a CREATE TABLE is emitted here only if this batch's
-- mapper files contain an INSERT INTO for that table. Tables that are
-- only ever SELECTed/JOINed/UPDATEd in this batch are skipped (see
-- report) so another batch's owning mapper can define them.
-- =====================================================================


-- =====================================================================
-- source: groupbanner-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_CATEGORY_GROUP_BANNER (
    CATEGORY_GROUP_BANNER_ID   INTEGER PRIMARY KEY,
    CATEGORY_GROUP_ID          INTEGER,
    TITLE                      VARCHAR(255),
    LINK_URL                   VARCHAR(255),
    FILE_NAME                  VARCHAR(255),
    DISPLAY_ORDER               INTEGER,
    CREATED_DATE               TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: categoriesteamgroup-mapper.xml (insertCategoriesTeam)
-- domain: saleson.shop.categoriesteamgroup.domain.CategoriesTeam
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CATEGORY_TEAM (
    CATEGORY_TEAM_ID          INTEGER PRIMARY KEY,
    NAME                      VARCHAR(255),
    CODE                      VARCHAR(50),
    CATEGORY_TEAM_FLAG        CHAR(1),
    TITLE                     VARCHAR(255),
    KEYWORDS                  VARCHAR(255),
    DESCRIPTION               TEXT,
    HEADER_CONTENTS1          TEXT,
    HEADER_CONTENTS2          TEXT,
    HEADER_CONTENTS3          TEXT,
    THEMAWORD_TITLE           VARCHAR(255),
    THEMAWORD_DESCRIPTION     TEXT,
    RANK_TITLE                VARCHAR(255),
    RANK_KEYWORDS             VARCHAR(255),
    RANK_DESCRIPTION          TEXT,
    RANK_HEADERCONTENTS1      TEXT,
    RANK_THEMAWORD_TITLE      VARCHAR(255),
    RANK_THEMAWORD_DESCRIPTION TEXT,
    REVIEW_TITLE              VARCHAR(255),
    REVIEW_KEYWORDS           VARCHAR(255),
    REVIEW_DESCRIPTION        TEXT,
    REVIEW_HEADERCONTENTS1    TEXT,
    REVIEW_THEMAWORD_TITLE    VARCHAR(255),
    REVIEW_THEMAWORD_DESCRIPTION TEXT,
    ORDERING                  INTEGER,
    BEST_ITEM_DISPLAY_TYPE    VARCHAR(50),
    CREATED_DATE              VARCHAR(20),
    UPDATED_DATE              VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: categoriesteamgroup-mapper.xml (insertCategoryTeamItem)
-- domain: saleson.shop.categoriesteamgroup.domain.CategoryTeamItem
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CATEGORY_TEAM_ITEM (
    CATEGORY_TEAM_ITEM_ID  INTEGER PRIMARY KEY,
    CATEGORY_TEAM_ID       INTEGER,
    ITEM_ID                INTEGER,
    CREATED_DATE           VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: claim-mapper.xml (insertClaimMemo)
-- domain: saleson.shop.claim.domain.ClaimMemo
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CLAIM_MEMO (
    CLAIM_MEMO_ID     INTEGER PRIMARY KEY,
    USER_ID           BIGINT,
    USER_NAME         VARCHAR(255),
    ORDER_CODE        VARCHAR(50),
    CLAIM_STATUS      VARCHAR(10),
    MEMO              TEXT,
    MANAGER_USER_ID   BIGINT,
    MANAGER_LOGIN_ID  VARCHAR(255),
    DATA_STATUS_CODE  CHAR(1),
    CREATED_DATE      VARCHAR(20)
);

-- =====================================================================
-- source: code-mapper.xml (batch_01), framework-code-mapper.xml (batch_02) -- merged
-- Domain: saleson.shop.code.domain.Code / com.onlinepowers.framework.repository.CodeInfo
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_COMMON_CODE (
    CODE_TYPE       VARCHAR(50)  NOT NULL,
    "LANGUAGE"      VARCHAR(10)  NOT NULL,
    ID              VARCHAR(50)  NOT NULL,
    LABEL           VARCHAR(255),
    DETAIL          TEXT, -- CONFLICT: batch_01 guessed TEXT, batch_02 guessed VARCHAR(255); chose TEXT
    ORDERING        INTEGER,
    USE_YN          CHAR(1),
    UP_ID           VARCHAR(50),
    CODE_VALUE      VARCHAR(255),
    EXTENSION_CODE  VARCHAR(50),
    MAPPING_CODE    VARCHAR(50),
    PRIMARY KEY (CODE_TYPE, "LANGUAGE", ID)
);

-- =====================================================================
-- source: demo-mapper.xml (batch_01), menu-manager-mapper.xml / message-mapper.xml (batch_04) -- merged
-- Domain: saleson.shop.message.domain.Message
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_COMMON_MESSAGE (
    ID          VARCHAR(50) NOT NULL, -- CONFLICT: batch_01 guessed VARCHAR(10), batch_04 guessed VARCHAR(50) (needed for 'MENU_%'-prefixed ids); chose VARCHAR(50)
    "LANGUAGE"  VARCHAR(10) NOT NULL,
    MESSAGE     TEXT,
    PRIMARY KEY (ID, "LANGUAGE")
);

-- ---------------------------------------------------------------------
-- source: comment-mapper.xml (insertComment)
-- domain: saleson.shop.comment.dto.CommentDto
-- note: USER_NAME/LOCGOV_NM/COMMENT_DEPTH seen in SELECT are pulled in via
-- LEFT JOIN to op_manager/g_locgov or computed with CASE, not real columns.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COMMUNITY_COMMENT (
    BOARD_COMMENT_ID    BIGINT PRIMARY KEY,
    PARENT_COMMENT_ID   BIGINT,
    BOARD_ID            BIGINT,
    BOARD_CODE          VARCHAR(50),
    USER_ID             BIGINT,
    COMMENTS            TEXT,
    IS_DELETE           CHAR(1),
    CREATED_DATE        VARCHAR(20),
    UPDATED_DATE        VARCHAR(20)
);

-- =====================================================================
-- source: locgov-databoard-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_COMMUNITY_DATABOARD (
    DATA_ID               INTEGER PRIMARY KEY,
    SUBJECT               VARCHAR(255),
    URL                   VARCHAR(255),
    URL_TYPE              VARCHAR(10),   -- guess: not in insertDataboard's column list, only ever computed in SELECTs; kept nullable
    CATEGORY_TEAM          VARCHAR(255),
    USER_NAME             VARCHAR(255),   -- crypto_enc()'d
    PASSWORD              VARCHAR(255),
    EMAIL                 VARCHAR(255),
    CONTENT               TEXT,
    HITS                  INTEGER,
    BOARD_CODE            VARCHAR(50),
    SUB_CATEGORY           VARCHAR(50),
    CREATED_DATE           TIMESTAMP,
    TARGET_OPTION          VARCHAR(50),
    REL_OPTION             VARCHAR(50),
    NOTICE_FLAG            CHAR(1),
    VISIBLE_TYPE           VARCHAR(10),
    SELLER_SELECT_FLAG      CHAR(1),
    FILE_TY                VARCHAR(50),   -- guess: only produced via subquery GROUP_CONCAT in SELECTs, not in insert list
    USE_YN                 CHAR(1)
);

-- source: locgov-databoard-mapper.xml
-- NOTE: the front-end query fragments in this same mapper (whereFrontDataboard, insertDataboardFile's ordering subquery) reference
-- a table named OP_DATA_BOARD_FILE, which folds to a different lowercase identifier than op_community_databoard_file below.
-- This looks like a naming inconsistency/typo in the legacy code; the table actually targeted by INSERT INTO is
-- op_community_databoard_file, so that is the one defined here.
CREATE TABLE IF NOT EXISTS OP_COMMUNITY_DATABOARD_FILE (
    DATA_FILE_ID       INTEGER PRIMARY KEY,
    DATA_ID            INTEGER,
    FILE_NAME          VARCHAR(255),
    FILE_TY            VARCHAR(50),
    ORDERING           INTEGER,
    CREATED_DATE        TIMESTAMP,
    ORG_FILE_NAME       VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- source: community-mapper.xml (insertFreeBoard)
-- domain: saleson.shop.community.doamin.CommunityDto
-- guess: createdDate/updatedDate are java.util.Date in CommunityDto (unlike
-- most other *_DATE fields in this codebase, which are String), so TIMESTAMP.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COMMUNITY_FREEBOARD (
    ID            INTEGER PRIMARY KEY,
    USER_ID       BIGINT,
    SUBJECT       VARCHAR(255),
    CONTENT       TEXT,
    USEYN         CHAR(1),
    NOTICE_FLAG   CHAR(1),
    HITS          INTEGER,
    IS_DELETE     CHAR(1),
    CREATED_DATE  TIMESTAMP,
    UPDATED_DATE  TIMESTAMP
);

-- =====================================================================
-- source: locgFaq-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_COMMUNITY_LOCGOVFAQ (
    ID               INTEGER PRIMARY KEY,
    SUBJECT          VARCHAR(255),
    CONTENT          TEXT,
    ADMIN_ID         VARCHAR(50),
    UPDATED_BY       VARCHAR(50),
    FAQ_TYPE         VARCHAR(50),
    USE_YN           CHAR(1),
    CREATED_DATE     TIMESTAMP,
    UPDATED_DATE     TIMESTAMP,
    HITS             INTEGER
);

-- ---------------------------------------------------------------------
-- source: condition-mapper.xml (insertCondition)
-- domain: saleson.shop.condition.domain.Condition
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CONDITION (
    CONDITION_ID      INTEGER PRIMARY KEY,
    CATEGORY_CODE     VARCHAR(50),
    CONDITION_TITLE   VARCHAR(255),
    USE_YN            CHAR(1),
    CREATED_DATE      VARCHAR(20),
    UPDATED_DATE      VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: condition-mapper.xml (insertConditionDetail)
-- domain: saleson.shop.condition.domain.ConditionDetail
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_CONDITION_DETAIL (
    DETAIL_ID        INTEGER PRIMARY KEY,
    CONDITION_ID     INTEGER,
    DETAIL_TITLE     VARCHAR(255),
    USE_YN           CHAR(1),
    ORDERING         INTEGER,
    CREATED_DATE     VARCHAR(20),
    UPDATED_DATE     VARCHAR(20)
);

-- =====================================================================
-- source: batch/couponregular-mapper-batch.xml (batch_00), coupon-mapper.xml (batch_01) -- merged
-- Domain: saleson.shop.coupon.domain.Coupon
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_COUPON (
    COUPON_ID                     INTEGER PRIMARY KEY,
    COUPON_TYPE                   VARCHAR(255), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_01 guessed VARCHAR(255); chose VARCHAR(255)
    COUPON_NAME                   VARCHAR(255),
    COUPON_COMMENT                TEXT,
    COUPON_ISSUE_TYPE             CHAR(1),
    COUPON_ISSUE_START_DATE       VARCHAR(20),
    COUPON_ISSUE_END_DATE         VARCHAR(20),
    COUPON_APPLY_TYPE             CHAR(1),
    COUPON_APPLY_DAY              VARCHAR(50), -- CONFLICT: batch_00 guessed VARCHAR(50) (day-of-week list), batch_01 guessed INTEGER; chose VARCHAR(50)
    COUPON_APPLY_START_DATE       VARCHAR(20),
    COUPON_APPLY_END_DATE         VARCHAR(20),
    COUPON_TARGET_TIME_TYPE       VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_01 guessed VARCHAR(10); chose VARCHAR(10) (consistent with OP_COUPON_REGULAR/OP_COUPON_USER)
    COUPON_TARGET_USER_TYPE       VARCHAR(10), -- CONFLICT: see COUPON_TARGET_TIME_TYPE
    COUPON_TARGET_USER_LEVEL      VARCHAR(255),
    COUPON_TARGET_USER            TEXT,
    COUPON_TARGET_ITEM_TYPE       VARCHAR(10), -- CONFLICT: see COUPON_TARGET_TIME_TYPE
    COUPON_TARGET_ITEM            TEXT,
    COUPON_PAY_RESTRICTION        INTEGER, -- CONFLICT: batch_00 guessed VARCHAR(50), batch_01 guessed INTEGER; chose INTEGER (consistent with OP_COUPON_REGULAR/OP_COUPON_USER)
    COUPON_CONCURRENTLY           VARCHAR(10), -- CONFLICT: batch_00 guessed CHAR(1), batch_01 guessed VARCHAR(10); chose VARCHAR(10)
    COUPON_PAY_TYPE                VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_01 guessed VARCHAR(10); chose VARCHAR(10)
    COUPON_PAY                    NUMERIC(15,2), -- CONFLICT: batch_00 guessed NUMERIC(15,2), batch_01 guessed VARCHAR(50); chose NUMERIC(15,2)
    COUPON_DISCOUNT_LIMIT_PRICE   NUMERIC(15,2), -- CONFLICT: batch_00 guessed NUMERIC(15,2), batch_01 guessed INTEGER; chose NUMERIC(15,2)
    COUPON_DOWNLOAD_LIMIT         INTEGER,
    COUPON_DOWNLOAD_USER_LIMIT    INTEGER,
    COUPON_MULITPLE_DOWNLOAD_FLAG CHAR(1),
    COUPON_FLAG                   CHAR(1),
    COUPON_OFFLINE_FLAG           CHAR(1),
    COUPON_BIRTHDAY               CHAR(1), -- CONFLICT: batch_00 guessed CHAR(1), batch_01 guessed VARCHAR(20); chose CHAR(1) (consistent with other COUPON_*_FLAG columns)
    DATA_STATUS_CODE              VARCHAR(2), -- CONFLICT: batch_00 guessed VARCHAR(2), batch_01 guessed CHAR(1); chose VARCHAR(2)
    UPDATE_USER_NAME              VARCHAR(255),
    UPDATED_DATE                  VARCHAR(20),
    CREATED_DATE                  VARCHAR(20),
    DIRECT_INPUT_FLAG             CHAR(1),
    DIRECT_INPUT_VALUE            VARCHAR(255),
    INFLUENCER_USER_ID            BIGINT,
    TOTAL_DOWNLOAD_COUNT          INTEGER, -- from resultMap only, not in either INSERT
    TOTAL_USED_COUNT              INTEGER  -- from resultMap only, not in either INSERT
);

-- ---------------------------------------------------------------------
-- source: coupon-mapper.xml (insertCouponOffline)
-- domain: saleson.shop.coupon.domain.CouponOffline
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COUPON_OFFLINE (
    COUPON_OFFLINE_ID   INTEGER PRIMARY KEY,
    COUPON_ID           INTEGER,
    USER_ID             BIGINT,
    COUPON_OFFLINE_CODE VARCHAR(50),
    COUPON_USED_FLAG    CHAR(1),
    COUPON_USED_DATE    VARCHAR(20),
    PUBLISHED_DATE      VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: coupon-regular-mapper.xml (insertCouponRegular)
-- domain: saleson.shop.coupon.domain.Coupon (regular/recurring coupon variant)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COUPON_REGULAR (
    COUPON_ID                      INTEGER PRIMARY KEY,
    COUPON_TYPE                    VARCHAR(255),
    COUPON_NAME                    VARCHAR(255),
    COUPON_COMMENT                 TEXT,
    COUPON_ISSUE_TYPE               CHAR(1),
    COUPON_ISSUE_START_DATE        VARCHAR(20),
    COUPON_ISSUE_END_DATE          VARCHAR(20),
    COUPON_TARGET_TIME_TYPE        VARCHAR(10),
    COUPON_TARGET_USER_TYPE        VARCHAR(10),
    COUPON_TARGET_USER_LEVEL       VARCHAR(255),
    COUPON_TARGET_USER             TEXT,
    COUPON_TARGET_ITEM_TYPE        VARCHAR(10),
    COUPON_TARGET_ITEM             TEXT,
    COUPON_PAY_RESTRICTION          INTEGER,
    COUPON_CONCURRENTLY            VARCHAR(10),
    COUPON_PAY_TYPE                VARCHAR(10),
    COUPON_PAY                     VARCHAR(50),
    COUPON_DISCOUNT_LIMIT_PRICE     INTEGER,
    COUPON_DOWNLOAD_LIMIT           INTEGER,
    COUPON_DOWNLOAD_USER_LIMIT      INTEGER,
    COUPON_MULITPLE_DOWNLOAD_FLAG  CHAR(1),
    COUPON_FLAG                    CHAR(1),
    COUPON_BIRTHDAY                VARCHAR(20),
    DATA_STATUS_CODE               CHAR(1),
    CREATED_DATE                   VARCHAR(20),
    UPDATED_DATE                   VARCHAR(20),
    UPDATE_USER_NAME                VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- source: coupon-mapper.xml (insertCouponTargetItem)
-- domain: saleson.shop.coupon.domain.Coupon (couponTargetItems)
-- TODO: primary key unclear (join table between coupons and items)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COUPON_TARGET_ITEM (
    ITEM_ID       INTEGER,
    COUPON_ID     INTEGER,
    CREATED_DATE  VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: coupon-mapper.xml (insertCouponTargetUser / insertCouponTargetUserOne)
-- domain: saleson.shop.coupon.domain.Coupon (couponTargetUsers)
-- TODO: primary key unclear (no single-row lookup in mapper; treated as a
-- join table between coupons and users)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COUPON_TARGET_USER (
    USER_ID       BIGINT,
    COUPON_ID     INTEGER,
    CREATED_DATE  VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: coupon-mapper.xml (insertDownloadCoupon)
-- domain: saleson.shop.coupon.domain.CouponUser
-- guess: COUPON_USER_ID is never in the INSERT column list -> auto identity.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_COUPON_USER (
    COUPON_USER_ID              SERIAL PRIMARY KEY,
    COUPON_ID                   INTEGER,
    USER_ID                     BIGINT,
    COUPON_TYPE                 VARCHAR(255),
    COUPON_NAME                 VARCHAR(255),
    COUPON_COMMENT              TEXT,
    COUPON_APPLY_TYPE            CHAR(1),
    COUPON_APPLY_START_DATE     VARCHAR(20),
    COUPON_APPLY_END_DATE       VARCHAR(20),
    COUPON_PAY_RESTRICTION       INTEGER,
    COUPON_CONCURRENTLY         VARCHAR(10),
    COUPON_PAY_TYPE             VARCHAR(10),
    COUPON_PAY                  INTEGER,
    COUPON_DISCOUNT_LIMIT_PRICE  INTEGER,
    COUPON_TARGET_ITEM_TYPE     VARCHAR(10),
    DATA_STATUS_CODE            CHAR(1),
    COUPON_DOWNLOAD_DATE        VARCHAR(20),
    COUPON_USED_DATE            VARCHAR(20),
    ORDER_CODE                  VARCHAR(50),
    ORDER_SEQUENCE               INTEGER,
    ITEM_SEQUENCE                INTEGER,
    DISCOUNT_AMOUNT              INTEGER,
    CREATED_DATE                VARCHAR(20)
);

-- =====================================================================
-- Batch 01 DDL — reverse-engineered from MyBatis mapper XML (CUBRID -> PostgreSQL)
-- Mappers analyzed: categoriesedit, categoriesteamgroup, change-log, claim,
--                    cmnty, cntntsstsfdg, code, comment, community, condition,
--                    config-isms, config, coupon, coupon-regular, customer,
--                    databoard, deliverycompany, deliveryhope, demo,
--                    designated-donation
-- Ownership rule: a table is only defined here if this batch's mapper files
-- contain an INSERT INTO for it. Tables only ever SELECTed/JOINed/UPDATEd in
-- this batch are assumed to be owned by another batch and are NOT created
-- here (see the accompanying report for the full skip list).
-- Date convention: columns backed by a Java String field populated via
-- CommonMapper.datetime (format DATE_FORMAT(NOW(),'%Y%m%d%H%i%s')) are kept
-- as VARCHAR, matching the Java type. Columns backed by java.util.Date /
-- java.sql.Timestamp Java fields are modeled as TIMESTAMP.
-- =====================================================================


-- ---------------------------------------------------------------------
-- source: customer-mapper.xml (calibration reference for this batch)
-- domain: saleson.shop.customer.domain.Customer
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- source: databoard-mapper.xml (insertDataboard)
-- domain: saleson.shop.databoard.domain.Databoard
-- note: URL_TYPE/NEW_FLAG/FILE_TY(on select) are computed in SELECT, not
-- stored on this table (FILE_TY here is only the DataboardFile file-type,
-- kept since it appears in the resultMap and is inserted for other rows).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DATA_BOARD (
    DATA_ID              INTEGER PRIMARY KEY,
    SUBJECT              VARCHAR(255),
    URL                  VARCHAR(500),
    CATEGORY_TEAM        VARCHAR(255),
    USER_NAME            VARCHAR(255),
    PASSWORD             VARCHAR(255),
    EMAIL                VARCHAR(255),
    CONTENT              TEXT,
    HITS                 INTEGER,
    BOARD_CODE           VARCHAR(50),
    SUB_CATEGORY         VARCHAR(50),
    CREATED_DATE         VARCHAR(20),
    TARGET_OPTION        VARCHAR(10),
    REL_OPTION           VARCHAR(10),
    NOTICE_FLAG          CHAR(1),
    VISIBLE_TYPE         INTEGER,
    SELLER_SELECT_FLAG   CHAR(1),
    USE_YN               CHAR(1)
);

-- ---------------------------------------------------------------------
-- source: databoard-mapper.xml (insertDataboardFile)
-- domain: saleson.shop.databoard.domain.DataboardFile
-- guess: DATA_FILE_ID is a String in Java and is compared via
-- crypto_enc('normal', DATA_FILE_ID, '') = #{dataFileId} (encrypted lookup),
-- so kept as VARCHAR rather than an integer identity.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DATA_BOARD_FILE (
    DATA_FILE_ID    VARCHAR(50) PRIMARY KEY,
    DATA_ID         INTEGER,
    FILE_NAME       VARCHAR(255),
    FILE_TY         VARCHAR(50),
    ORDERING        INTEGER,
    CREATED_DATE    VARCHAR(20),
    ORG_FILE_NAME   VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- source: deliverycompany-mapper.xml (insertDeliveryCompany)
-- domain: saleson.shop.deliverycompany.domain.DeliveryCompany
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DELIVERY_COMPANY (
    DELIVERY_COMPANY_ID         INTEGER PRIMARY KEY,
    DELIVERY_COMPANY_NAME       VARCHAR(255),
    TEL_NUMBER                  VARCHAR(50),
    DELIVERY_COMPANY_URL        VARCHAR(500),
    SEND_FLAG                   CHAR(1),
    DELIVERY_NUMBER_PARAMETER   VARCHAR(255),
    USE_FLAG                    CHAR(1)
);

-- ---------------------------------------------------------------------
-- source: deliveryhope-mapper.xml (insertDeliveryHope)
-- domain: saleson.shop.deliveryhope.domain.DeliveryHope
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DELIVERY_HOPE (
    DELIVERY_HOPE_ID      INTEGER PRIMARY KEY,
    DELIVERY_HOPE_TIME    VARCHAR(50),
    DELIVERY_HOPE_INDEX   INTEGER
);

-- ---------------------------------------------------------------------
-- source: display-mapper.xml (insertDisplayEditor)
-- domain: saleson.shop.display.domain.DisplayEditor
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DISPLAY_EDITOR (
    DISPLAY_GROUP_CODE     VARCHAR(50)     NOT NULL,
    DISPLAY_SUB_CODE       VARCHAR(50),
    VIEW_TARGET            VARCHAR(50),
    DISPLAY_EDITOR_CONTENT TEXT,
    ORDERING                INTEGER,
    CREATED_DATE           TIMESTAMP
    -- TODO: primary key unclear (no single-row lookup in mapper; rows are
    -- selected/deleted as a set by GROUP_CODE/SUB_CODE/VIEW_TARGET)
);

-- ---------------------------------------------------------------------
-- source: display-mapper.xml (insertDisplayImage)
-- domain: saleson.shop.display.domain.DisplayImage
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DISPLAY_IMAGE (
    DISPLAY_GROUP_CODE     VARCHAR(50)     NOT NULL,
    DISPLAY_SUB_CODE       VARCHAR(50),
    VIEW_TARGET            VARCHAR(50),   -- guess: mapped in resultMap/where but absent from INSERT column list
    DISPLAY_IMAGE          VARCHAR(255),
    DISPLAY_URL             VARCHAR(255),
    DISPLAY_CONTENT        TEXT,
    DISPLAY_COLOR          VARCHAR(50),
    ORDERING                INTEGER,
    CREATED_DATE           TIMESTAMP
    -- TODO: primary key unclear (getDisplayImageByParam looks up by
    -- GROUP_CODE+SUB_CODE+VIEW_TARGET+ORDERING together, but no explicit ID column)
);

-- =====================================================================
-- Batch 02 DDL — reverse-engineered from MyBatis mapper XML (CUBRID -> PostgreSQL)
-- Mappers analyzed: display, email, exceldownload-log, externalapi, featured,
--                    featuredbanner, framework-code, framework-common,
--                    framework-file, framework-message, framework-sequence,
--                    framework-token, generalcustomer, give-opertaion,
--                    give-point-expiration, give-point, give-state,
--                    give-statistics, group
-- Ownership rule: a table is only defined here if an INSERT INTO for it
-- appears in this batch's mapper files. Tables only ever SELECTed/JOINed/
-- UPDATEd in this batch are assumed to be owned by another batch and are
-- NOT created here (see report for the full skip list).
-- =====================================================================


-- ---------------------------------------------------------------------
-- source: display-mapper.xml (insertDisplayItem)
-- domain: saleson.shop.display.domain.DisplayItem (extends Item)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DISPLAY_ITEM (
    DISPLAY_GROUP_CODE     VARCHAR(50)     NOT NULL,
    DISPLAY_SUB_CODE       VARCHAR(50),
    VIEW_TARGET            VARCHAR(50),
    ITEM_ID                INTEGER         NOT NULL,
    ORDERING                INTEGER,
    CREATED_DATE           TIMESTAMP,
    -- guess: no single-row lookup exists in the mapper; composite key inferred
    -- from the columns used together in delete/select WHERE clauses.
    PRIMARY KEY (DISPLAY_GROUP_CODE, DISPLAY_SUB_CODE, VIEW_TARGET, ITEM_ID)
);

-- ---------------------------------------------------------------------
-- source: display-mapper.xml (insertDisplaySns)
-- domain: saleson.shop.display.domain.DisplaySns
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_DISPLAY_SNS (
    SNS_ID                  INTEGER         PRIMARY KEY,
    SNS_TOKEN               VARCHAR(255),
    SNS_TYPE                VARCHAR(50),
    ORDERING                 INTEGER,
    UPDATED_DATE            TIMESTAMP,
    CREATED_DATE            TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: email-mapper.xml (insertEmail)
-- domain: saleson.shop.email.domain.Email
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_EMAIL (
    EMAIL_ID                BIGINT          PRIMARY KEY,   -- long emailId, useGeneratedKeys
    SUBJECT                 VARCHAR(255),
    CONTENT                 TEXT,
    SEND_DATE               TIMESTAMP,
    STATUS                  VARCHAR(10),
    AUTH_TARGET             VARCHAR(255),
    FRST_REGISTER_ID        BIGINT,
    FRST_REGIST_PNTTM       TIMESTAMP,
    LAST_UPDUSR_ID          BIGINT,
    LAST_UPDT_PNTTM         TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: email-mapper.xml (insertEmailDetail)
-- domain: saleson.shop.email.domain.EmailDetail
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_EMAIL_DETAIL (
    EMAIL_ID                BIGINT          NOT NULL,
    AUTHORITY               VARCHAR(50)     NOT NULL,
    -- guess: junction table between email and role/authority; no single ID column
    PRIMARY KEY (EMAIL_ID, AUTHORITY)
);

-- ---------------------------------------------------------------------
-- source: email-mapper.xml (insertEmailFile)
-- domain: saleson.shop.email.domain.EmailFile
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_EMAIL_FILE (
    EMAIL_FILE_ID           INTEGER         PRIMARY KEY,  -- guess: selected but not in INSERT list; likely auto-increment
    EMAIL_ID                BIGINT          NOT NULL,
    FILE_NAME               VARCHAR(255),
    FILE_TY                 VARCHAR(50),
    ORDERING                 INTEGER,
    CREATED_DATE            TIMESTAMP,
    ORG_FILE_NAME           VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- source: featured-mapper.xml (insertFeatured / mergeEvent / updateEventCode)
-- domain: saleson.shop.featured.domain.Featured
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_FEATURED (
    FEATURED_ID                  INTEGER          PRIMARY KEY,
    FEATURED_CLASS               INTEGER,
    FEATURED_TYPE                VARCHAR(10),
    FEATURED_URL                 VARCHAR(255),
    FEATURED_CODE                VARCHAR(255),
    FEATURED_NAME                VARCHAR(255),
    FEATURED_SIMPLE_CONTENT      TEXT,
    FEATURED_CONTENT             TEXT,
    FEATURED_IMAGE               VARCHAR(255),
    FEATURED_IMAGE_MOBILE        VARCHAR(255),
    THUMBNAIL_IMAGE              VARCHAR(255),
    THUMBNAIL_IMAGE_MOBILE       VARCHAR(255),
    FEATURED_FLAG                CHAR(1),
    DISPLAY_LIST_FLAG            CHAR(1),
    LINK                         VARCHAR(255),
    LINK_TARGET_FLAG             CHAR(1),
    LINK_REL_FLAG                CHAR(1),
    ORDERING                      INTEGER,
    TITLE                        VARCHAR(255),
    KEYWORDS                     VARCHAR(255),
    DESCRIPTION                  TEXT,
    HEADER_CONTENTS1             TEXT,
    HEADER_CONTENTS2             TEXT,
    HEADER_CONTENTS3             TEXT,
    THEMAWORD_TITLE              VARCHAR(255),
    THEMAWORD_DESCRIPTION        TEXT,
    CREATED_DATE                 TIMESTAMP,
    LIST_TYPE                    VARCHAR(10),
    PROD_STATE                   VARCHAR(10),
    -- guess: START/END DATE and TIME kept as separate strings and CONCAT()-ed
    -- in WHERE clauses (e.g. CONCAT(START_DATE,START_TIME) > ...), so they are
    -- modeled as VARCHAR rather than TIMESTAMP to match actual query usage.
    START_DATE                   VARCHAR(8),
    START_TIME                   VARCHAR(6),
    END_DATE                     VARCHAR(8),
    END_TIME                     VARCHAR(6),
    REPLY_USED_FLAG              CHAR(1),
    FEATURED_HOST                VARCHAR(255),
    FEATURED_PHONE_NO1           VARCHAR(20),
    FEATURED_PHONE_NO2           VARCHAR(20),
    FEATURED_PHONE_NO3           VARCHAR(20),
    FEATURED_LIST_IMAGE          VARCHAR(255),
    THUMBNAIL_LIST_IMAGE         VARCHAR(255),
    LOCGOV_CODE                  VARCHAR(50),
    ACCESS_AUTH                  VARCHAR(50),
    EVENT_CODE                   VARCHAR(50),
    -- guess: category-specific ordering columns, referenced only in ORDER BY
    ORDERING_ESTHETIC            INTEGER,
    ORDERING_NAIL                INTEGER,
    ORDERING_MATSUGE_EXTENSION   INTEGER,
    ORDERING_HAIR                INTEGER,
    ORDERING_SALE_OUTLETS        INTEGER
);

-- ---------------------------------------------------------------------
-- source: featured-mapper.xml (insertFeaturedItem)
-- domain: saleson.shop.featured.support.FeaturedItem (extends Item)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_FEATURED_ITEM (
    FEATURED_ID                  INTEGER          NOT NULL,
    ITEM_ID                      INTEGER          NOT NULL,
    DISPLAY_ORDER                INTEGER,
    CREATED_DATE                 TIMESTAMP,
    USER_DEF_GROUP               VARCHAR(50),
    USER_DEF_GROUP_ORDER         VARCHAR(50),
    -- guess: one row per featured/item pairing
    PRIMARY KEY (FEATURED_ID, ITEM_ID)
);

-- ---------------------------------------------------------------------
-- source: featured-mapper.xml (insertFeaturedReply)
-- domain: saleson.shop.featured.domain.FeaturedReply
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_FEATURED_REPLY (
    ID                           BIGINT           PRIMARY KEY,
    FEATURED_ID                  INTEGER,
    USER_ID                      BIGINT,
    USER_NAME                    VARCHAR(255),
    REPLY_CONTENT                TEXT,
    DATA_STATUS                  VARCHAR(10),   -- 0: 정상, 1: 삭제
    CREATED                      TIMESTAMP,
    UPDATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED_BY                   BIGINT
);

-- ---------------------------------------------------------------------
-- source: framework-file-mapper.xml (insert id="add")
-- domain: UploadFile (com.onlinepowers.framework.file — not present in this repo; typed from naming convention)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_FILE (
    FILE_ID                       INTEGER          PRIMARY KEY,
    REF_CODE                      VARCHAR(50),
    REF_ID                        INTEGER,
    FILE_NAME                     VARCHAR(255),
    FILE_SIZE                      INTEGER,
    FILE_TYPE                     VARCHAR(50),
    FILE_DESCRIPTION              VARCHAR(255),
    UPLOAD_TYPE                   VARCHAR(10),
    USE_ENCRYPT                   CHAR(1),
    DOWNLOAD_COUNT                 INTEGER,
    ORDERING                       INTEGER,
    STATUS_CODE                   VARCHAR(10),
    CREATED_DATE                  TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: framework-file-mapper.xml (insert id="addFileTemp")
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_FILE_TEMP (
    FILE_ID                       INTEGER          PRIMARY KEY,
    REF_CODE                      VARCHAR(50),
    REF_ID                        INTEGER,
    FILE_NAME                     VARCHAR(255),
    FILE_SIZE                      INTEGER,
    FILE_TYPE                     VARCHAR(50),
    FILE_DESCRIPTION              VARCHAR(255),
    UPLOAD_TYPE                   VARCHAR(10),
    USE_ENCRYPT                   CHAR(1),
    DOWNLOAD_COUNT                 INTEGER,
    ORDERING                       INTEGER,
    STATUS_CODE                   VARCHAR(10),
    CREATED_DATE                  TIMESTAMP
);

-- =====================================================================
-- source: group-mapper.xml (batch_02), usergroup-mapper.xml (batch_07) -- merged (identical definitions)
-- Domain: saleson.shop.group.domain.Group / saleson.shop.usergroup.UserGroupMapper
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_GROUP (
    GROUP_CODE          VARCHAR(50)  PRIMARY KEY,
    GROUP_NAME          VARCHAR(255),
    GROUP_EXPLANATION   TEXT,
    CREATED_DATE        TIMESTAMP,
    CREATED_USER_ID     VARCHAR(50),
    UPDATED_DATE        TIMESTAMP,
    UPDATED_USER_ID     VARCHAR(50)
);

-- =====================================================================
-- source: mypage-mapper.xml
-- Domain: saleson.shop.mypage.support.ReceiptParam
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_HONOR_VIEW_HIST (
    VIEW_DT     TIMESTAMP, -- populated via now(), a genuine CUBRID datetime function (not the string-format CommonMapper fragment)
    USER_ID     BIGINT,
    LCLGV_CD    VARCHAR(50)
    -- TODO: primary key unclear — insert-only view-history log, no single-row lookup in this mapper
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- Domain: saleson.shop.item.domain.Item / ItemBase
-- item-mapper.xml is the primary/authoritative definition; batch/item-mapper-batch.xml
-- contributes additional columns seen only in its Excel-export insert/update flows.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM (
    ITEM_ID                       INTEGER PRIMARY KEY,
    VENDOR_ID                     INTEGER, -- from batch_00 only
    SELLER_ID                     BIGINT,
    ITEM_CODE                     VARCHAR(50),
    ITEM_SHORT_CODE                VARCHAR(50), -- from batch_03 only
    ITEM_USER_CODE                VARCHAR(50),
    ITEM_NAME                     VARCHAR(255),
    ITEM_SUMMARY                  TEXT, -- CONFLICT: batch_00 guessed VARCHAR(500), batch_03 guessed TEXT; chose TEXT
    ITEM_DATA_TYPE                 VARCHAR(10), -- from batch_03 only
    ITEM_TYPE                     VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    ITEM_LABEL                    VARCHAR(50), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_03 guessed VARCHAR(10); chose VARCHAR(50)
    ITEM_LABEL_SOLD_OUT           VARCHAR(50), -- from batch_00 only
    ITEM_NEW_FLAG                 CHAR(1),
    ITEM_TYPE1                    VARCHAR(50),
    ITEM_TYPE2                    VARCHAR(50),
    ITEM_TYPE3                    VARCHAR(50),
    ITEM_TYPE4                    VARCHAR(50),
    ITEM_TYPE5                    VARCHAR(50),
    ITEM_TYPE6                    VARCHAR(50), -- from batch_03 only (guess: SELECT-only, naming-convention typed)
    ITEM_TYPE7                    VARCHAR(50), -- from batch_03 only
    ITEM_TYPE8                    VARCHAR(50), -- from batch_03 only
    ITEM_TYPE9                    VARCHAR(50), -- from batch_03 only
    ITEM_TYPE10                   VARCHAR(50), -- from batch_03 only
    PRIVATE_TYPE                  VARCHAR(10), -- from batch_03 only
    MD_ID                         VARCHAR(50), -- from batch_03 only
    MD_NAME                       VARCHAR(255), -- from batch_03 only
    DISPLAY_FLAG                  CHAR(1),
    ORIGIN_COUNTRY                VARCHAR(255),
    MANUFACTURER                  VARCHAR(255),
    BRAND_ID                      INTEGER,
    BRAND                         VARCHAR(255),
    COLOR                         VARCHAR(255), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_03 guessed VARCHAR(255); chose VARCHAR(255)
    WEIGHT                        VARCHAR(50),
    NONMEMBER_ORDER_TYPE          VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    TAX_TYPE                      VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    PRICE_CRITERIA                 VARCHAR(10), -- from batch_03 only
    ITEM_PRICE                    VARCHAR(50), -- CONFLICT: batch_00 guessed INTEGER (KRW, no decimals), batch_03 guessed VARCHAR(50) (Java field ItemBase.itemPrice is String); chose VARCHAR(50)
    SALE_PRICE                    INTEGER,
    SALE_PRICE_NONMEMBER_FLAG      CHAR(1), -- from batch_03 only
    SALE_PRICE_NONMEMBER          INTEGER,
    SALE_POINT                    INTEGER,
    SUPPLY_PRICE                  INTEGER,
    COST_PRICE                    INTEGER,
    COMMISSION_TYPE                VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    COMMISSION_RATE                NUMERIC(5,2),
    VENDOR_DISCOUNT_FLAG            CHAR(1), -- from batch_00 only
    VENDOR_DISCOUNT_TYPE           VARCHAR(1), -- from batch_00 only
    VENDOR_DISCOUNT_AMOUNT         INTEGER, -- from batch_00 only
    SALE_FEE_TYPE                  VARCHAR(1), -- from batch_00 only
    SALE_FEE_RATE                  NUMERIC(5,2), -- from batch_00 only
    COUPON_USE_FLAG                CHAR(1),
    SELLER_DISCOUNT_FLAG           CHAR(1),
    SELLER_DISCOUNT_TYPE           VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    SELLER_DISCOUNT_AMOUNT         INTEGER,
    SELLER_POINT_FLAG              CHAR(1),
    SET_DISCOUNT_TYPE               VARCHAR(10), -- from batch_03 only
    SET_DISCOUNT_AMOUNT             INTEGER, -- from batch_03 only
    SOLD_OUT                       VARCHAR(5), -- CONFLICT: batch_00 guessed CHAR(1), batch_03 guessed VARCHAR(5) ('0'/'1' code); chose VARCHAR(5)
    SPOT_FLAG                       CHAR(1), -- from batch_03 only
    SPOT_DATE_TYPE                  VARCHAR(10), -- from batch_03 only
    SPOT_TYPE                       VARCHAR(10), -- from batch_03 only
    SPOT_APPLY_GROUP                VARCHAR(50), -- from batch_03 only
    SPOT_DISCOUNT_AMOUNT            INTEGER, -- from batch_03 only
    SPOT_START_DATE                 VARCHAR(20), -- from batch_03 only
    SPOT_END_DATE                   VARCHAR(20), -- from batch_03 only
    SPOT_START_TIME                 VARCHAR(10), -- from batch_03 only
    SPOT_END_TIME                   VARCHAR(10), -- from batch_03 only
    SPOT_WEEK_DAY                   VARCHAR(10), -- from batch_03 only
    STOCK_FLAG                      CHAR(1),
    STOCK_CODE                      VARCHAR(50), -- from batch_03 only
    STOCK_QUANTITY                  INTEGER,
    STOCK_SCHEDULE_AUTO_FLAG         CHAR(1),
    STOCK_SCHEDULE_TYPE              VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    STOCK_SCHEDULE_DATE              VARCHAR(20), -- from batch_03 only
    OPTION_STOCK_SCHEDULE_DATE       VARCHAR(20), -- from batch_00 only (distinct column, separate from STOCK_SCHEDULE_DATE)
    STOCK_SCHEDULE_TEXT              VARCHAR(255),
    ORDER_MIN_QUANTITY               INTEGER,
    ORDER_MAX_QUANTITY               INTEGER,
    SALE_QUANTITY                    INTEGER,
    ITEM_OPTION_FLAG                 CHAR(1),
    ITEM_OPTION_TYPE                 VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    ITEM_OPTION_TITLE1               VARCHAR(255), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_03 guessed VARCHAR(255); chose VARCHAR(255)
    ITEM_OPTION_TITLE2               VARCHAR(255), -- CONFLICT: see ITEM_OPTION_TITLE1
    ITEM_OPTION_TITLE3               VARCHAR(255), -- CONFLICT: see ITEM_OPTION_TITLE1
    ITEM_TEXT_OPTION_FLAG            CHAR(1), -- from batch_03 only
    ITEM_TEXT_OPTION_TITLE1          VARCHAR(255), -- from batch_03 only
    ITEM_TEXT_OPTION_TITLE2          VARCHAR(255), -- from batch_03 only
    ITEM_TEXT_OPTION_TITLE3          VARCHAR(255), -- from batch_03 only
    ITEM_ADDITION_FLAG               CHAR(1),
    FREE_GIFT_FLAG                   CHAR(1),
    FREE_GIFT_NAME                   VARCHAR(255),
    ITEM_KEYWORD                     VARCHAR(500), -- CONFLICT: batch_00 guessed VARCHAR(500), batch_03 guessed VARCHAR(255); chose VARCHAR(500)
    SIMPLE_CONTENT                   TEXT, -- from batch_00 only
    LIST_CONTENT                     TEXT, -- from batch_00 only
    DETAIL_CONTENT                   TEXT,
    DETAIL_CONTENT_MOBILE            TEXT,
    USE_MANUAL                       VARCHAR(255), -- from batch_00 only
    MAKE_MANUAL                      VARCHAR(255), -- from batch_00 only
    ITEM_IMAGE                       VARCHAR(255),
    TEAM                             VARCHAR(255),
    OPENTIME                         VARCHAR(255), -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed VARCHAR(255); chose VARCHAR(255)
    BASE_ITEM                        VARCHAR(255), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_03 guessed VARCHAR(255); chose VARCHAR(255)
    OTHER_FLAG                       VARCHAR(10), -- CONFLICT: batch_00 guessed CHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    RECOMMEND_FLAG                   CHAR(1),
    RELATION_ITEM_DISPLAY_TYPE       VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    DELIVERY_TYPE                    VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    DELIVERY_ID                      INTEGER, -- from batch_00 only
    DELIVERY_CHARGE_ID               INTEGER, -- from batch_00 only
    DELIVERY_COMPANY_ID              INTEGER,
    DELIVERY_COMPANY_NAME            VARCHAR(255), -- from batch_03 only
    SHIPMENT_ID                      INTEGER,
    SHIPMENT_GROUP_CODE              VARCHAR(50), -- from batch_03 only
    SHIPMENT_RETURN_TYPE             VARCHAR(10), -- from batch_03 only
    SHIPMENT_RETURN_ID               INTEGER,
    SHIPPING_TYPE                    VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(1), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    SHIPPING_GROUP_CODE              VARCHAR(50), -- from batch_03 only
    SHIPPING                         INTEGER,
    SHIPPING_FREE_AMOUNT             INTEGER,
    SHIPPING_ITEM_COUNT              INTEGER,
    SHIPPING_EXTRA_CHARGE1           INTEGER,
    SHIPPING_EXTRA_CHARGE2           INTEGER,
    SHIPPING_RETURN                  INTEGER,
    ITEM_RETURN_FLAG                 CHAR(1),
    DELIVERY_INFO                    TEXT, -- from batch_00 only
    HITS                             INTEGER,
    SEO_TITLE                        VARCHAR(255),
    SEO_INDEX_FLAG                   CHAR(1),
    SEO_NO_INDEX_DISPLAY_FLAG        CHAR(1), -- from batch_00 only
    SEO_KEYWORDS                     VARCHAR(500), -- CONFLICT: batch_00 guessed VARCHAR(500), batch_03 guessed VARCHAR(255); chose VARCHAR(500)
    SEO_DESCRIPTION                  TEXT,
    SEO_HEADER_CONTENTS1              TEXT,
    SEO_THEMAWORD_TITLE               VARCHAR(255),
    SEO_THEMAWORD_DESCRIPTION         TEXT,
    ITEM_NOTICE_CODE                  VARCHAR(50),
    DATA_STATUS_CODE                  VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(2), batch_03 guessed VARCHAR(10); chose VARCHAR(10)
    DATA_STATUS_MESSAGE               VARCHAR(255), -- from batch_03 only
    UPDATED_USER_ID                   BIGINT,
    UPDATED_DATE                      VARCHAR(20), -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20) (matches CommonMapper.datetime string convention used pervasively elsewhere in this codebase)
    CREATED_USER_ID                   BIGINT,
    CREATED_DATE                      VARCHAR(20), -- CONFLICT: see UPDATED_DATE
    ERP_EXCEPTION_TYPE                VARCHAR(10), -- from batch_03 only
    NAVER_SHOPPING_ITEM_NAME          VARCHAR(255), -- from batch_03 only
    NAVER_SHOPPING_FLAG               CHAR(1), -- from batch_03 only
    NAVER_PAY_FLAG                    CHAR(1), -- from batch_03 only
    MOBILE_ITEM_YN                    CHAR(1), -- from batch_03 only
    ADULT_ITEM_YN                     CHAR(1), -- from batch_03 only
    ITEM_CLOSE_DT                     VARCHAR(20), -- from batch_03 only
    REVIEW_COUNT                      VARCHAR(20), -- from batch_03 only (guess: aggregated string, not a real persisted column type)
    REVIEW_SCORE                      VARCHAR(20), -- from batch_03 only
    REPRESENTATIVE_ITEM_YN             CHAR(1), -- from batch_03 only
    LOCGOV_CODE                        VARCHAR(50)
);

-- =====================================================================
-- source: item-addition-mapper.xml (authoritative definition of OP_ITEM_ADDITION)
-- NOTE: item-mapper.xml also contains "INSERT INTO OP_ITEM_ADDITION (ITEM_ID,
-- ADDITION_ITEM_ID)" (method insertItemAddtion) which targets a completely
-- different, narrower column set (a 2-column item<->addition-item junction).
-- This looks like a legacy naming collision/bug in the app rather than two
-- real schemas; the fuller definition below (from ItemAddition.java /
-- item-addition-mapper.xml) is treated as authoritative. getParentAdditionItemId
-- in item-mapper.xml also selects ADDITION_ITEM_ID from this table, which does
-- not exist in this column list -- flagging for manual review.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_ADDITION (
    ITEM_ADDITION_ID        INTEGER PRIMARY KEY,
    ITEM_ID                 INTEGER,
    ADDITION_ITEM_NAME      VARCHAR(255),
    ADDITION_SALE_PRICE     VARCHAR(50),   -- guess: Java field is String (ADDITION_SALE_PRICE), not numeric
    ADDITION_STOCK_FLAG     CHAR(1),
    ADDITION_STOCK_QUANTITY INTEGER,
    ADDITION_STOCK_CODE     VARCHAR(50),
    ADDITION_TAX_TYPE       VARCHAR(10),
    ADDITION_WEIGHT         VARCHAR(50),
    ADDITION_DISPLAY_FLAG   CHAR(1),
    CREATED_DATE            TIMESTAMP
    -- ADDITION_ITEM_ID INTEGER  -- referenced by item-mapper.xml's insertItemAddtion / getParentAdditionItemId but not part of
                                  -- this table's column list; possibly a distinct junction table sharing the same name (see note above).
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_CATEGORY (
    ITEM_CATEGORY_ID    INTEGER PRIMARY KEY,
    ITEM_ID              INTEGER,
    CATEGORY_ID          INTEGER,
    ORDERING             INTEGER,
    CREATED_DATE         VARCHAR(20) -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20) for consistency with sibling OP_ITEM_* tables
);

-- source: item-mapper.xml (mergeItemHits: INSERT ... VALUES (#{value}, 1) ON DUPLICATE KEY UPDATE HITS = HITS + 1)
CREATE TABLE IF NOT EXISTS OP_ITEM_HIT (
    ITEM_ID     INTEGER PRIMARY KEY,
    HITS        INTEGER
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_IMAGE (
    ITEM_IMAGE_ID    INTEGER PRIMARY KEY,
    ITEM_ID          INTEGER,
    IMAGE_NAME       VARCHAR(255),
    ORDERING         INTEGER,
    CREATED_DATE     VARCHAR(20) -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20) for consistency with sibling OP_ITEM_* tables
);

CREATE TABLE IF NOT EXISTS OP_ITEM_INFO (
	ITEM_INFO_ID   INTEGER PRIMARY KEY,
	ITEM_ID        INTEGER,
	ITEM_NOTICE_CODE VARCHAR(50),
	INFO_CODE      VARCHAR(50),
	TITLE          VARCHAR(255),
	DESCRIPTION    TEXT,
	CREATED_DATE   VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS OP_ITEM_INFO_MOBILE (
	ITEM_INFO_ID   INTEGER PRIMARY KEY,
	ITEM_ID        INTEGER,
	INFO_CODE      VARCHAR(50),
	TITLE          VARCHAR(255),
	DESCRIPTION    TEXT,
	CREATED_DATE   VARCHAR(20)
);

-- source: item-mapper.xml
-- TODO: primary key unclear -- no id column in INSERT INTO OP_ITEM_LOG; appears to be an append-only audit log table (likely has an
-- auto-increment surrogate key not exposed through this mapper).
CREATE TABLE IF NOT EXISTS OP_ITEM_LOG (
    ITEM_ID               INTEGER,
    ITEM_USER_CODE         VARCHAR(50),
    ITEM_NAME             VARCHAR(255),
    DISPLAY_FLAG           CHAR(1),
    SOLD_OUT              VARCHAR(5),
    COST_PRICE            INTEGER,
    ITEM_PRICE            VARCHAR(50),
    SALE_PRICE            INTEGER,
    COMMISSION_TYPE        VARCHAR(10),
    COMMISSION_RATE        NUMERIC(5,2),
    PRICE_CRITERIA         VARCHAR(10),
    PROCESS_PAGE           VARCHAR(50),
    CREATED_MANAGER_ID      BIGINT,
    CREATED_SELLER_ID       BIGINT,
    CREATED_DATE           TIMESTAMP,
    ACTION_TYPE            VARCHAR(50)
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_OPTION (
    ITEM_OPTION_ID             INTEGER PRIMARY KEY,
    ITEM_ID                    INTEGER,
    OPTION_TYPE                VARCHAR(50), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_03 guessed VARCHAR(10); chose VARCHAR(50)
    OPTION_DISPLAY_TYPE        VARCHAR(50), -- CONFLICT: see OPTION_TYPE
    OPTION_HIDE_FLAG           CHAR(1),
    OPTION_NAME1               VARCHAR(255),
    OPTION_NAME2               VARCHAR(255),
    OPTION_NAME3               VARCHAR(255),
    OPTION_STOCK_CODE          VARCHAR(50),
    OPTION_COST_PRICE          INTEGER,
    OPTION_PRICE               INTEGER,
    OPTION_PRICE_NONMEMBER     INTEGER,
    OPTION_STOCK_FLAG          CHAR(1),
    OPTION_SOLD_OUT_FLAG       CHAR(1),
    OPTION_STOCK_QUANTITY      INTEGER,
    OPTION_STOCK_SCHEDULE_TEXT VARCHAR(255),
    OPTION_STOCK_SCHEDULE_DATE VARCHAR(20),
    OPTION_DISPLAY_FLAG        CHAR(1),
    CREATED_USER_ID            BIGINT,
    CREATED_DATE               VARCHAR(20) -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20)
);

-- source: item-mapper.xml (insertItemOptionSoldout is a bulk SELECT..INSERT recompute of this table)
CREATE TABLE IF NOT EXISTS OP_ITEM_OPTION_SOLDOUT (
    ITEM_ID                     INTEGER PRIMARY KEY,
    ITEM_OPTION_SOLD_OUT_FLAG      CHAR(1)
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_ORDERING (
    ITEM_ORDERING_ID INTEGER PRIMARY KEY,
    ITEM_ID          INTEGER,
    CATEGORY_ID      INTEGER,
    ORDERING         INTEGER,
    CREATED_DATE     VARCHAR(20) -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20) for consistency with sibling OP_ITEM_* tables
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), item-mapper.xml (batch_03) -- merged
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ITEM_RELATION (
    ITEM_RELATION_ID INTEGER PRIMARY KEY,
    ITEM_ID          INTEGER,
    RELATED_ITEM_ID  INTEGER,
    ORDERING         INTEGER,
    CREATED_DATE     VARCHAR(20) -- CONFLICT: batch_00 guessed VARCHAR(20), batch_03 guessed TIMESTAMP; chose VARCHAR(20) for consistency with sibling OP_ITEM_* tables
);

-- source: item-mapper.xml
CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW (
    ITEM_REVIEW_ID        INTEGER PRIMARY KEY,
    ITEM_ID               INTEGER,
    ORDER_CODE            VARCHAR(50),
    SUBJECT               VARCHAR(255),
    CONTENT               TEXT,
    SCORE                 INTEGER,
    RECOMMEND_FLAG         CHAR(1),
    USER_ID               BIGINT,
    USER_NAME             VARCHAR(255),
    SELLER_ID             BIGINT,
    DISPLAY_FLAG           CHAR(1),
    CREATED_DATE          TIMESTAMP,
    POINT_PAYMENT          CHAR(1),
    POINT                 INTEGER,
    POINT_PAYMENT_DATE      VARCHAR(20),
    DISPLAY_OPTIONS_FLAG    CHAR(1),
    OPTIONS               VARCHAR(255)
);

-- source: item-mapper.xml
CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW_IMAGE (
    ITEM_REVIEW_IMAGE_ID    BIGINT PRIMARY KEY,  -- guess: deleteItemReviewImageById uses parameterType Long -> surrogate PK not listed in INSERT column list (likely auto-increment)
    ITEM_REVIEW_ID          INTEGER,
    REVIEW_IMAGE            VARCHAR(255),
    ORDERING                INTEGER,
    CREATED_DATE            TIMESTAMP
);

-- source: item-mapper.xml
CREATE TABLE IF NOT EXISTS OP_ITEM_SALE_EDIT (
    ITEM_SALE_EDIT_ID    INTEGER PRIMARY KEY,
    ITEM_ID              INTEGER,
    SELLER_ID            BIGINT,
    SELLER_NAME          VARCHAR(255),   -- crypto_enc()'d in the mapper -> stored encrypted, still textual
    ITEM_CODE            VARCHAR(50),
    ITEM_NAME            VARCHAR(255),
    SALE_PRICE           INTEGER,
    ITEM_PRICE           VARCHAR(50),
    COST_PRICE           INTEGER,
    STATUS               VARCHAR(10),
    MESSAGE              VARCHAR(255),
    CREATED_DATE         TIMESTAMP,
    UPDATED_DATE         TIMESTAMP
);

-- source: item-mapper.xml
-- guess: PK inferred as (PARENT_ITEM_ID, ITEM_ID) -- a set-item to component-item link; no surrogate id present in INSERT list.
CREATE TABLE IF NOT EXISTS OP_ITEM_SET (
    PARENT_ITEM_ID    INTEGER,
    ITEM_ID           INTEGER,
    QUANTITY          INTEGER,
    ORDERING          INTEGER,
    CREATED_DATE      TIMESTAMP,
    PRIMARY KEY (PARENT_ITEM_ID, ITEM_ID)
);

-- =====================================================================
-- source: keyword-mapper.xml
-- =====================================================================
-- TODO: primary key unclear -- mergeItemKeyword relies on "ON DUPLICATE KEY UPDATE" implying a unique constraint (likely
-- KEYWORD + KEYWORD_TYPE + CREATED_DATE) but no single natural PK column is evident from the mapper alone.
CREATE TABLE IF NOT EXISTS OP_KEYWORD (
    KEYWORD               VARCHAR(255),
    KEYWORD_TYPE           VARCHAR(10),
    CREATED_DATE           VARCHAR(8),   -- guess: CommonMapper.date -> YYYYMMDD string, used in BETWEEN date-string comparisons
    KEYWORD_SEPERATION      VARCHAR(255),
    WEIGHT                 INTEGER
);

-- source: stats-mapper.xml (saleson.shop.stats.StatsMapper.insertLoginCount)
CREATE TABLE IF NOT EXISTS OP_LOGIN_COUNT (
    LOGIN_DATE VARCHAR(20),
    LOGIN_TYPE VARCHAR(50),
    LOGIN_COUNT INTEGER,
    PRIMARY KEY (LOGIN_DATE, LOGIN_TYPE) -- guess: inferred from "ON DUPLICATE KEY UPDATE LOGIN_COUNT = LOGIN_COUNT + 1"
);

-- =====================================================================
-- source: login-log-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_LOGIN_LOG (
    LOGIN_LOG_ID     INTEGER PRIMARY KEY,   -- guess: not in INSERT column list (auto-increment), but selected/filtered by LOGIN_LOG_ID elsewhere
    LOGIN_TYPE       VARCHAR(20),
    LOGIN_ID         VARCHAR(255),    -- crypto_enc()'d
    SUCCESS_FLAG      CHAR(1),
    REMOTE_ADDR       VARCHAR(255),    -- crypto_enc()'d
    MEMO             VARCHAR(255),
    LOGIN_DATE        TIMESTAMP
);

-- =====================================================================
-- source: log-manage-mapper.xml
-- =====================================================================
-- TODO: primary key unclear -- insertTodayLog only ever writes CREATED_DATE; likely has an unexposed auto-increment id.
CREATE TABLE IF NOT EXISTS OP_LOG_MANAGE (
    CREATED_DATE     TIMESTAMP
);

-- =====================================================================
-- source: mailconfig-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MAIL_CONFIG (
    MAIL_CONFIG_ID           INTEGER PRIMARY KEY,
    SMS_CONFIG               VARCHAR(50),
    TEMPLATE_ID              VARCHAR(50),
    TITLE                    VARCHAR(255),
    BUYER_SUBJECT             VARCHAR(255),
    ADMIN_SUBJECT             VARCHAR(255),
    SELLER_SUBJECT            VARCHAR(255),
    BUYER_CONTENT             TEXT,
    ADMIN_CONTENT             TEXT,
    SELLER_CONTENT            TEXT,
    MOBILE_BUYER_SUBJECT       VARCHAR(255),
    MOBILE_ADMIN_SUBJECT       VARCHAR(255),
    MOBILE_SELLER_SUBJECT      VARCHAR(255),
    MOBILE_BUYER_CONTENT       TEXT,
    MOBILE_ADMIN_CONTENT       TEXT,
    MOBILE_SELLER_CONTENT      TEXT,
    BUYER_SEND_FLAG           CHAR(1),
    ADMIN_SEND_FLAG           CHAR(1),
    SELLER_SEND_FLAG          CHAR(1),
    BUYER_TAG_USE             CHAR(1),
    ADMIN_TAG_USE             CHAR(1),
    SELLER_TAG_USE            CHAR(1),
    CREATED_DATE             TIMESTAMP
);

-- =====================================================================
-- source: mainbanner-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MAIN_BANNER (
    BANNER_ID           INTEGER PRIMARY KEY,
    TITLE               VARCHAR(255),
    CONTENTS            TEXT,
    LINK_URL            VARCHAR(255),
    DISPLAY_ORDER        INTEGER,
    CREATED_DATE         TIMESTAMP,
    PC_FILE_NAME         VARCHAR(255),
    M_FILE_NAME          VARCHAR(255),
    PC_ORG_FILE_NAME      VARCHAR(255),
    M_ORG_FILE_NAME       VARCHAR(255),
    DISPLAY_FLAG         CHAR(1)
);

-- =====================================================================
-- source: maindisplayitem-mapper.xml
-- =====================================================================
-- guess: PK inferred as (TEMPLATE_ID, ITEM_ID) -- deleteMainDisplayItemByTemplateId removes all rows for a TEMPLATE_ID; no
-- surrogate id column present.
CREATE TABLE IF NOT EXISTS OP_MAIN_DISPLAY_ITEM (
    TEMPLATE_ID       VARCHAR(50),
    ITEM_ID           INTEGER,
    DISPLAY_ORDER      INTEGER,
    CREATED_DATE      TIMESTAMP,
    PRIMARY KEY (TEMPLATE_ID, ITEM_ID)
);

-- =====================================================================
-- source: managerrequest-mapper.xml (batch_04), user-mapper.xml (batch_07) -- merged
-- Domain: com.onlinepowers.framework.security.userdetails.User / saleson.shop.user.domain.ManagerRequest
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MANAGER (
    USER_ID                 BIGINT PRIMARY KEY,
    LOGIN_ID                VARCHAR(255),
    PASSWORD                VARCHAR(255),
    USER_NAME               VARCHAR(255),
    EMAIL                   VARCHAR(255),
    PHONE_NUMBER            VARCHAR(255), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(50); chose VARCHAR(255)
    STATUS_CODE             VARCHAR(10),
    LOGIN_COUNT             INTEGER,
    LOGIN_DATE              VARCHAR(14), -- CONFLICT: batch_04 guessed VARCHAR(14) (CommonMapper.datetime convention), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
    DENY_DATE               VARCHAR(14), -- CONFLICT: see LOGIN_DATE
    LEAVE_DATE               VARCHAR(14), -- CONFLICT: see LOGIN_DATE
    LOGIN_FAIL_COUNT        INTEGER,
    LOGIN_TRY_DATE          VARCHAR(14),
    PASSWORD_TYPE           VARCHAR(10),
    PASSWORD_EXPIRED_DATE   VARCHAR(14), -- CONFLICT: see LOGIN_DATE
    AUTH_EXPIRED_DATE       VARCHAR(14),
    UPDATED_DATE            VARCHAR(14), -- CONFLICT: see LOGIN_DATE
    CREATED_DATE            VARCHAR(14), -- CONFLICT: see LOGIN_DATE
    LOCGOV_CODE             VARCHAR(50),
    BANK_CODE               VARCHAR(50),
    EMP_ID                  VARCHAR(50),
    PSITN_NM                VARCHAR(255),
    PSITN_DEPT_NM           VARCHAR(255),
    OFCPS_NM                VARCHAR(255),
    INFO_UPDT_DE            VARCHAR(8) -- CONFLICT: batch_04 guessed VARCHAR(8), batch_07 guessed TIMESTAMP; chose VARCHAR(8)
);

-- =====================================================================
-- source: manager-action-log-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MANAGER_ACTION_LOG (
    ACTION_LOG_ID     BIGINT PRIMARY KEY,   -- guess: not in INSERT column list (auto-increment); resultMap/selects key off it
    LOGIN_ID          VARCHAR(255),   -- crypto_enc()'d
    LOGIN_TYPE        VARCHAR(20),
    REQUEST_URI        VARCHAR(255),
    REQUEST_METHOD      VARCHAR(10),
    REMOTE_ADDR        VARCHAR(255),   -- crypto_enc()'d
    CREATED_DATE       TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: change-log-mapper.xml (insertManagerChangeLog)
-- domain: saleson.shop.log.domain.ChangeLog
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_MANAGER_CHANGE_LOG (
    CHANGE_LOG_ID  SERIAL PRIMARY KEY,
    USER_ID        BIGINT,
    PARAMETER      TEXT,
    REMOTE_ADDR    VARCHAR(50),
    MANAGER_ID     BIGINT,
    CREATED_DATE   VARCHAR(20),
    CHANGE_TYPE    VARCHAR(50)
);

-- =====================================================================
-- Batch 04 DDL — reverse-engineered from MyBatis mapper XML (CUBRID -> PostgreSQL)
-- Mapper files analyzed:
--   manager-hist-mapper.xml, managerrequest-mapper.xml, manual-mapper.xml,
--   menu-manager-mapper.xml, menu-mapper.xml, message-mapper.xml,
--   mobilecategoriesedit-mapper.xml, mypage-mapper.xml, ngdonation-mapper.xml,
--   notice-mapper.xml, nuri2-mapper.xml, offgive-mapper.xml, openmarket-mapper.xml,
--   order-add-payment-mapper.xml, order-admin-mapper.xml, order-agency-mapper.xml,
--   order-claim-apply-mapper.xml, order-give-point-mapper.xml, order-mapper.xml,
--   order-payment-mapper.xml
--
-- NOTE on date columns: throughout this codebase, "date/datetime" columns are
-- populated via the CommonMapper.datetime / CommonMapper.date SQL fragments,
-- which produce formatted strings ('yyyyMMddHHmmss' / 'yyyyMMdd'), and the
-- corresponding Java domain fields (e.g. saleson.shop.order.domain.OrderItem,
-- Order, OrderPayment, OrderShipping) are typed as String, not Date/Timestamp.
-- These are therefore modeled as VARCHAR(14) / VARCHAR(8) rather than TIMESTAMP,
-- consistent with how CUBRID actually stores them in this legacy app.
-- =====================================================================


-- =====================================================================
-- source: manager-hist-mapper.xml
-- Domain: saleson.shop.log.domain.ManagerHist
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MANAGER_HIST (
    HIST_ID         BIGINT GENERATED BY DEFAULT AS IDENTITY, -- guess: surrogate id implied by property "histId"; not present in INSERT column list
    USER_ID         BIGINT NOT NULL,
    HIST_SEQ        INTEGER NOT NULL,
    LOGIN_ID        VARCHAR(255),
    USER_NAME       VARCHAR(255),
    EMAIL           VARCHAR(255),
    PHONE_NUMBER    VARCHAR(255),
    STATUS_CODE     VARCHAR(10),
    UPDATED_DATE    VARCHAR(14),
    LOCGOV_CODE     VARCHAR(50),
    BANK_CODE       VARCHAR(50),
    EMP_ID          VARCHAR(50),
    PSITN_NM        VARCHAR(255),
    PSITN_DEPT_NM   VARCHAR(255),
    OFCPS_NM        VARCHAR(255),
    AUTHORITY       VARCHAR(50),
    INFO_UPDT_DE    VARCHAR(8),
    REG_USERTXT     VARCHAR(255),
    PRIMARY KEY (USER_ID, HIST_SEQ) -- guess: next HIST_SEQ computed per USER_ID (see insertManagerHist), composite natural key
);

-- source: login-log-mapper.xml
-- TODO: primary key unclear -- AUTH_NUM is looked up by LOGIN_ID + recency (ORDER BY created_at DESC LIMIT 1); no single
-- natural PK evident; likely an unexposed auto-increment id.
CREATE TABLE IF NOT EXISTS OP_MANAGER_LOGIN_EMAIL (
    AUTH_NUM      VARCHAR(10),
    LOGIN_ID      VARCHAR(255),
    CREATED_AT    TIMESTAMP
);

-- =====================================================================
-- source: password-log-mapper.xml
-- Domain: saleson.shop.log.domain.PasswordLog
-- guess: no surrogate id column found in mapper/domain class; the table
--   is append-only (queried by USER_ID, ordered by CREATED_DATE), so no
--   reliable single/composite PK could be inferred.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MANAGER_PASSWORD_LOG (
    USER_ID       BIGINT NOT NULL,
    PASSWORD      VARCHAR(255),
    CREATED_DATE  VARCHAR(20)
    -- TODO: primary key unclear
);

-- =====================================================================
-- source: menu-manager-mapper.xml / menu-mapper.xml (both insert OP_MENU; merged definition)
-- Domain: saleson.shop.menu.domain.Menu / Menu (com.onlinepowers.framework.web.opmanager.menu)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MENU (
    MENU_ID             INTEGER NOT NULL,
    MENU_PARENT_ID      INTEGER,
    MENU_TYPE           VARCHAR(10),
    MENU_NAME           VARCHAR(255),
    MENU_CODE           VARCHAR(50),
    MENU_URL            VARCHAR(255),
    MENU_SEQ            INTEGER,
    MENU_ICON           VARCHAR(255),
    DISPLAY_FLAG        CHAR(1), -- Y/N pattern confirmed via display_flag = 'Y' comparisons
    STATUS_CODE         VARCHAR(10),
    MENU_PAGE_CONTENT   TEXT,
    FILE_NM             VARCHAR(255),
    ORGINL_FILE_NM      VARCHAR(255),
    PRIMARY KEY (MENU_ID)
);

-- =====================================================================
-- source: menu-mapper.xml (com.onlinepowers.framework.web.opmanager.menu.MenuMapper)
-- Domain: MenuRight
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MENU_RIGHT (
    MENU_RIGHT_ID   INTEGER, -- guess: surrogate id (property menuRightId), not consistently populated (insertMenuRightByRole omits it)
    MENU_ID         INTEGER NOT NULL,
    AUTHORITY       VARCHAR(50) NOT NULL
    -- TODO: primary key unclear — deleteMenuRightByAuthority/ByMenuId suggest (MENU_ID, AUTHORITY) is the effective natural key
);

-- =====================================================================
-- source: menu-manager-mapper.xml
-- Domain: saleson.shop.menu.domain.Menu
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MENU_SELLER (
    MENU_ID             INTEGER NOT NULL,
    MENU_PARENT_ID      INTEGER,
    MENU_TYPE           VARCHAR(10),
    MENU_NAME           VARCHAR(255),
    MENU_CODE           VARCHAR(50),
    MENU_URL            VARCHAR(255),
    MENU_SEQ            INTEGER,
    DISPLAY_FLAG        CHAR(1),
    STATUS_CODE         VARCHAR(10),
    MENU_PAGE_CONTENT   TEXT,
    PRIMARY KEY (MENU_ID)
);

-- =====================================================================
-- source: mobilecategoriesedit-mapper.xml
-- Domain: saleson.shop.categoriesedit.domain.CategoriesEdit
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_MOBILE_CATEGORY_EDIT (
    CATEGORY_EDIT_ID    INTEGER NOT NULL,
    CODE                VARCHAR(50),
    EDIT_KIND           VARCHAR(50),
    EDIT_POSITION       VARCHAR(50),
    EDIT_CONTENT        TEXT,
    EDIT_IMAGE          VARCHAR(255),
    EDIT_URL            VARCHAR(255),
    CREATED_DATE        VARCHAR(14),
    UPDATED_DATE        VARCHAR(14),
    PRIMARY KEY (CATEGORY_EDIT_ID)
);

-- =====================================================================
-- source: notice-mapper.xml
-- Domain: saleson.shop.notice.domain.Notice
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_NOTICE (
    NOTICE_ID               INTEGER NOT NULL,
    SUBJECT                 VARCHAR(255),
    URL                     VARCHAR(255),
    CATEGORY_TEAM           VARCHAR(255),
    USER_NAME               VARCHAR(255),
    PASSWORD                VARCHAR(255),
    EMAIL                   VARCHAR(255),
    CONTENT                 TEXT,
    HITS                    INTEGER,
    BOARD_CODE              VARCHAR(50),
    SUB_CATEGORY            VARCHAR(50),
    CREATED_DATE            VARCHAR(14),
    TARGET_OPTION           VARCHAR(50),
    REL_OPTION              VARCHAR(50),
    NOTICE_FLAG             CHAR(1), -- Y/N pattern confirmed (IFNULL(notice_flag,'N') = 'Y')
    VISIBLE_TYPE            INTEGER,
    SELLER_SELECT_FLAG      CHAR(1),
    LOCGOV_CODE             VARCHAR(50),
    USE_YN                  CHAR(1),
    PRIMARY KEY (NOTICE_ID)
);

-- =====================================================================
-- source: notice-mapper.xml
-- Domain: saleson.shop.notice.domain.NoticeSeller
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_NOTICE_SELLER (
    NOTICE_SELLER_ID    INTEGER NOT NULL,
    NOTICE_ID           INTEGER NOT NULL,
    SELLER_ID           BIGINT,
    CREATED_DATE        VARCHAR(14),
    PRIMARY KEY (NOTICE_SELLER_ID)
);

-- =====================================================================
-- source: order-admin-mapper.xml / order-mapper.xml (insertOrder / insertOffOrder — merged, union of both column sets)
-- Domain: saleson.shop.order.domain.Order / Buyer
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER (
    ORDER_CODE              VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE          INTEGER NOT NULL,
    ORDER_TOTAL_AMOUNT      INTEGER,
    PAY_AMOUNT              INTEGER,
    USER_ID                 BIGINT,
    LOGIN_ID                VARCHAR(255),
    BUYER_NAME              VARCHAR(255),
    PHONE                   VARCHAR(50),
    MOBILE                  VARCHAR(50),
    EMAIL                   VARCHAR(255),
    ZIPCODE                 VARCHAR(20),
    NEW_ZIPCODE             VARCHAR(20),
    SIDO                    VARCHAR(50),
    SIGUNGU                 VARCHAR(50),
    EUPMYEONDONG            VARCHAR(50),
    ADDRESS                 TEXT,
    ADDRESS_DETAIL          TEXT,
    DATA_STATUS_CODE        VARCHAR(10),
    IP                      VARCHAR(50),
    LOCGOV_CODE             VARCHAR(50), -- referenced but commented-out in every INSERT seen; kept nullable
    ORDER_ADMIN_MEMO        TEXT,
    CREATED_DATE            VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE)
);

-- =====================================================================
-- source: order-add-payment-mapper.xml
-- Domain: saleson.shop.order.addpayment.domain.OrderAddPayment
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ADD_PAYMENT (
    ADD_PAYMENT_ID          INTEGER NOT NULL,
    SELLER_ID               BIGINT,
    ORDER_CODE              VARCHAR(50),
    ORDER_SEQUENCE          INTEGER,
    REFUND_CODE             VARCHAR(50),
    ISSUE_CODE              VARCHAR(50),
    SUBJECT                 VARCHAR(255),
    ADD_PAYMENT_TYPE        VARCHAR(10),
    AMOUNT                  INTEGER,
    REMITTANCE_AMOUNT       INTEGER,
    SALES_DATE              VARCHAR(14),
    SALES_CANCEL_DATE       VARCHAR(14),
    REMITTANCE_DATE         VARCHAR(14),
    REMITTANCE_EXPECTED_DATE VARCHAR(14),
    REMITTANCE_STATUS_CODE  VARCHAR(10),
    CREATED_DATE            VARCHAR(14),
    PRIMARY KEY (ADD_PAYMENT_ID)
);

-- =====================================================================
-- source: order-admin-mapper.xml
-- Domain: saleson.shop.order.admin.domain.OrderAdmin
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ADMIN (
    WORK_DATE               VARCHAR(8) NOT NULL,
    WORK_SEQUENCE           INTEGER NOT NULL,
    INSERT_MANAGER_NAME     VARCHAR(255),
    DATA_STATUS_CODE        VARCHAR(10),
    CREATED_DATE            VARCHAR(14),
    PRIMARY KEY (WORK_DATE, WORK_SEQUENCE)
);

-- =====================================================================
-- source: order-admin-mapper.xml
-- Domain: saleson.shop.order.admin.domain.OrderAdminDetail
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ADMIN_DETAIL (
    WORK_DATE               VARCHAR(8) NOT NULL,
    WORK_SEQUENCE           INTEGER NOT NULL,
    ITEM_SEQUENCE           INTEGER NOT NULL,
    ORDER_GROUP_CODE        VARCHAR(50),
    TEMPLATE_VERSION        VARCHAR(50),
    EXCEL_DATA               TEXT,
    DATA_STATUS_CODE        VARCHAR(10),
    SALE_PRICE               INTEGER,
    UPDATE_MANAGER_NAME     VARCHAR(255),
    UPDATED_DATE             VARCHAR(14),
    CREATED_DATE             VARCHAR(14),
    PRIMARY KEY (WORK_DATE, WORK_SEQUENCE, ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (batch-processing work queue)
-- Domain: (no resultMap/parameterType class; inferred from INSERT column list, insertOrderCancelTarget etc.)
-- =====================================================================
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
-- source: order-claim-apply-mapper.xml (batch_04), temp-process-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.order.claimapply.domain.OrderCancelApply
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_CANCEL_APPLY (
    -- CONFLICT (PK): batch_04 keyed on CLAIM_CODE alone (generated as 'C-<date>-<seq>', consistent with sibling
    -- OP_ORDER_RETURN_APPLY/OP_ORDER_EXCHANGE_APPLY), batch_07 guessed composite (ORDER_CODE, ORDER_SEQUENCE,
    -- ITEM_SEQUENCE) "not independently verified"; chose CLAIM_CODE as the more confident natural key
    CLAIM_CODE                      VARCHAR(50) PRIMARY KEY,
    ORDER_CODE                      VARCHAR(50),
    ORDER_SEQUENCE                   INTEGER,
    ITEM_SEQUENCE                    INTEGER,
    PARENT_ITEM_SEQUENCE             INTEGER,
    REFUND_CODE                       VARCHAR(50),
    CANCEL_APPLY_DATE                  VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(8), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    CLAIM_STATUS                        VARCHAR(10),
    CLAIM_APPLY_SUBJECT                  VARCHAR(255), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(10); chose VARCHAR(255)
    CLAIM_APPLY_QUANTITY                  INTEGER,
    CANCEL_REASON                          VARCHAR(50), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(10); chose VARCHAR(50)
    CANCEL_REASON_TEXT                      VARCHAR(255),
    CANCEL_REASON_DETAIL                     TEXT,
    CANCEL_MEMO                               TEXT,
    CANCEL_REFUSAL_REASON_TEXT                 TEXT,
    UPDATED_DATE                                TIMESTAMP,
    CREATED_DATE                                VARCHAR(14) -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
);

-- =====================================================================
-- source: order-claim-apply-mapper.xml (batch_04), temp-process-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.order.claimapply.domain.OrderExchangeApply
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_EXCHANGE_APPLY (
    -- CONFLICT (PK): batch_04 keyed on CLAIM_CODE alone (generated as 'E-<date>-<seq>'), batch_07 guessed
    -- composite (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE); chose CLAIM_CODE as the more confident natural key
    CLAIM_CODE                       VARCHAR(50) PRIMARY KEY,
    ORDER_CODE                       VARCHAR(50),
    ORDER_SEQUENCE                    INTEGER,
    ITEM_SEQUENCE                     INTEGER,
    PARENT_ITEM_SEQUENCE              INTEGER,
    CLAIM_APPLY_QUANTITY               INTEGER,
    SHIPMENT_RETURN_SELLER_ID           BIGINT,
    SHIPMENT_RETURN_ID                   BIGINT, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed BIGINT; chose BIGINT for consistency with SHIPMENT_RETURN_SELLER_ID
    CLAIM_STATUS                          VARCHAR(10),
    CLAIM_APPLY_SUBJECT                    VARCHAR(255), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(10); chose VARCHAR(255)
    EXCHANGE_APPLY_DATE                     VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(8), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    EXCHANGE_RECEIVE_NAME                    VARCHAR(255),
    EXCHANGE_RECEIVE_PHONE                    VARCHAR(50),
    EXCHANGE_RECEIVE_MOBILE                    VARCHAR(50),
    EXCHANGE_RECEIVE_ZIPCODE                    VARCHAR(20),
    EXCHANGE_RECEIVE_SIDO                        VARCHAR(100), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100)
    EXCHANGE_RECEIVE_SIGUNGU                      VARCHAR(100), -- CONFLICT: see EXCHANGE_RECEIVE_SIDO
    EXCHANGE_RECEIVE_EUPMYEONDONG                  VARCHAR(100), -- CONFLICT: see EXCHANGE_RECEIVE_SIDO
    EXCHANGE_RECEIVE_ADDRESS                        TEXT,
    EXCHANGE_RECEIVE_ADDRESS2                        TEXT, -- CONFLICT: batch_04 guessed TEXT, batch_07 guessed VARCHAR(255); chose TEXT
    EXCHANGE_DELIVERY_COMPANY_ID                      INTEGER, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed VARCHAR(50); chose INTEGER (consistent with OP_ITEM.DELIVERY_COMPANY_ID)
    EXCHANGE_DELIVERY_COMPANY_NAME                      VARCHAR(255),
    EXCHANGE_DELIVERY_NUMBER                             VARCHAR(100), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100)
    EXCHANGE_DELIVERY_COMPANY_URL                         VARCHAR(500), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(500); chose VARCHAR(500)
    EXCHANGE_DELIVERY_DATE                                  VARCHAR(14), -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
    EXCHANGE_SHIPPING_ASK_TYPE                                VARCHAR(10),
    EXCHANGE_SHIPPING_NUMBER                                   VARCHAR(100), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100)
    EXCHANGE_SHIPPING_COMPANY_NAME                               VARCHAR(255),
    EXCHANGE_SHIPPING_COMPANY_URL                                 VARCHAR(500), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(500); chose VARCHAR(500)
    EXCHANGE_SHIPPING_START_DATE                                    VARCHAR(14), -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
    EXCHANGE_REASON                                                   VARCHAR(50), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(10); chose VARCHAR(50)
    EXCHANGE_REASON_TEXT                                                VARCHAR(255),
    EXCHANGE_REASON_DETAIL                                                TEXT,
    EXCHANGE_MEMO                                                           TEXT,
    EXCHANGE_REFUSAL_REASON_TEXT                                              TEXT,
    UPDATED_DATE                                                                TIMESTAMP,
    CREATED_DATE                                                                VARCHAR(14) -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
);

-- =====================================================================
-- source: order-admin-mapper.xml / order-mapper.xml / order-claim-apply-mapper.xml (insertOrderItem and its
-- copy-for-claim variants — merged into the single richest column set; this is the central order line-item table)
-- Domain: saleson.shop.order.domain.OrderItem / BuyItem / ItemPrice
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM (
    ORDER_ITEM_ID                INTEGER, -- guess: surrogate id used only by legacy cancel/return-batch insert flows; main flow keys off composite below
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE                INTEGER NOT NULL,
    ITEM_SEQUENCE                 INTEGER NOT NULL,
    ORDER_STATUS                  VARCHAR(10),
    ORDER_ITEM_STATUS             VARCHAR(10),
    SHIPPING_SEQUENCE             INTEGER,
    SHIPPING_INFO_SEQUENCE        INTEGER,
    ORDER_SHIPPING_ID             INTEGER,
    ORDER_SHIPPING_INFO_ID        INTEGER,
    ORDER_PAYMENT_ID              INTEGER,
    ADDITION_ITEM_FLAG            CHAR(1),
    PARENT_ITEM_SEQUENCE          INTEGER,
    USER_ID                       BIGINT,
    GUEST_FLAG                    CHAR(1),
    SELLER_ID                     BIGINT,
    CATEGORY_TEAM_ID              INTEGER,
    CATEGORY_GROUP_ID             INTEGER,
    CATEGORY_ID                   INTEGER,
    SHIPMENT_ID                   INTEGER,
    SHIPMENT_RETURN_ID            INTEGER,
    COUPON_USER_ID                INTEGER,
    ADD_COUPON_USER_ID            INTEGER,
    ITEM_ID                       INTEGER,
    ITEM_CODE                     VARCHAR(50),
    ITEM_USER_CODE                VARCHAR(50),
    ITEM_NAME                     VARCHAR(255),
    FREE_GIFT_NAME                VARCHAR(255),
    IMAGE_SRC                     VARCHAR(255),
    DEVICE_TYPE                   VARCHAR(10),
    DELIVERY_TYPE                 VARCHAR(10),
    SHIPMENT_GROUP_CODE           VARCHAR(50),
    SHIPPING_RETURN               INTEGER,
    SHIPMENT_RETURN_TYPE          VARCHAR(10),
    PURCHASE_PRICE                INTEGER,
    COST_PRICE                    INTEGER,
    PRICE                         INTEGER,
    OPTION_PRICE                  INTEGER,
    SALE_PRICE                    INTEGER,
    QUANTITY                      INTEGER,
    ORDER_QUANTITY                INTEGER,
    CLAIM_QUANTITY                INTEGER,
    COUPON_DISCOUNT_PRICE         INTEGER,
    COUPON_DISCOUNT_AMOUNT        INTEGER,
    SALE_AMOUNT                   INTEGER,
    SPOT_SALE_FLAG                CHAR(1),
    SPOT_TYPE                     VARCHAR(10),
    SPOT_DISCOUNT_PRICE           INTEGER,
    TAX_TYPE                      VARCHAR(10),
    COMMISSION_BASE_PRICE         INTEGER,
    COMMISSION_RATE                NUMERIC(5,2),
    COMMISSION_PRICE              INTEGER,
    COMMISSION_TYPE                VARCHAR(10),
    SUPPLY_PRICE                  INTEGER,
    REMITTANCE_TYPE                VARCHAR(10),
    REMITTANCE_DAY                 VARCHAR(10),
    REMITTANCE_EXPECTED_DATE       VARCHAR(14),
    REMITTANCE_DATE                 VARCHAR(14),
    REMITTANCE_STATUS_CODE         VARCHAR(10),
    SELLER_DISCOUNT_PRICE         INTEGER,
    SELLER_DISCOUNT_AMOUNT        INTEGER,
    SELLER_DISCOUNT_DETAIL        VARCHAR(255),
    ADMIN_DISCOUNT_PRICE          INTEGER,
    ADMIN_DISCOUNT_AMOUNT         INTEGER,
    ADMIN_DISCOUNT_DETAIL         VARCHAR(255),
    BRAND                          VARCHAR(255),
    OPTIONS                        VARCHAR(255),
    TEXT_OPTION                    TEXT,
    DELIVERY_COMPANY_ID            INTEGER,
    DELIVERY_COMPANY_NAME          VARCHAR(255),
    DELIVERY_COMPANY_URL           VARCHAR(255),
    DELIVERY_NUMBER                VARCHAR(50),
    POINT_CONFIG_TYPE              VARCHAR(10),
    POINT_TYPE                      VARCHAR(10),
    POINT                            INTEGER,
    POINT_LOG                       VARCHAR(255),
    EARN_POINT                      INTEGER,
    EARN_POINT_FLAG                 CHAR(1),
    SELLER_POINT                    INTEGER,
    RETURN_POINT_FLAG               CHAR(1),
    ITEM_RETURN_FLAG                 CHAR(1),
    REVENUE_SALES_STATUS             VARCHAR(10),
    ESCROW_STATUS                    CHAR(1),
    LEVEL_ID                          INTEGER,
    LEVEL_NAME                        VARCHAR(255),
    USER_LEVEL_DISCOUNT_RATE          NUMERIC(5,2),
    USER_LEVEL_DISCOUNT_PRICE         INTEGER,
    CAMPAIGN_CODE                     VARCHAR(50),
    SET_ITEM_FLAG                     CHAR(1),
    SET_DISCOUNT_TYPE                 VARCHAR(10),
    SET_DISCOUNT_PRICE                INTEGER,
    LOCGOV_CODE                       VARCHAR(50),
    FILLER9                           VARCHAR(255), -- generic spare column, used to link to G_CNTR.CNTR_SN for offline gift orders
    UPDATED_ADMIN_USER_NAME           VARCHAR(255),
    OFF_PROC_YN                       CHAR(1),
    CANCEL_FLAG                       CHAR(1),
    REFUND_STATUS                     VARCHAR(10),
    PAY_DATE                          VARCHAR(14),
    SHIPPING_READY_DATE               VARCHAR(14),
    SHIPPING_DATE                     VARCHAR(14),
    SHIPPING_FINISH_DATE              VARCHAR(14),
    CANCEL_REQUEST_DATE               VARCHAR(14),
    CANCEL_REQUEST_FINISH_DATE        VARCHAR(14),
    CONFIRM_DATE                      VARCHAR(14),
    RETURN_REQUEST_DATE               VARCHAR(14),
    RETURN_REQUEST_FINISH_DATE        VARCHAR(14),
    EXCHANGE_REQUEST_DATE             VARCHAR(14),
    SALES_DATE                        VARCHAR(14),
    SALES_CANCEL_DATE                 VARCHAR(14),
    CREATED_DATE                      VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.BuyItem
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_BUY_TEMP (
    USER_ID                    BIGINT,
    SESSION_ID                   VARCHAR(255),
    ORDER_CODE                   VARCHAR(50),
    ITEM_SEQUENCE                 INTEGER,
    SHIPPING_INDEX                 INTEGER,
    ITEM_ID                         INTEGER,
    QUANTITY                        INTEGER,
    OPTIONS                          VARCHAR(255),
    COUPON_USER_ID                   INTEGER,
    ADD_COUPON_USER_ID                INTEGER,
    SHIPPING_PAYMENT_TYPE              VARCHAR(10),
    ADDITION_ITEM_FLAG                  CHAR(1),
    PARENT_ITEM_SEQUENCE                INTEGER,
    PARENT_ITEM_ID                       INTEGER,
    SET_ITEM_FLAG                         CHAR(1),
    ESCROW_STATUS                          CHAR(1),
    CREATED_DATE                            VARCHAR(14),
    CAMPAIGN_CODE                            VARCHAR(50)
    -- TODO: primary key unclear — pre-checkout staging table, guess composite (USER_ID/SESSION_ID, ORDER_CODE, ITEM_SEQUENCE)
);

-- source: temp-process-mapper.xml (saleson.shop.tempprocess.TempProcessMapper.insertHoldOrderListLog)
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_HOLD_HIST (
    REG_SEQ BIGINT PRIMARY KEY, -- guess: inferred from "SELECT IFNULL(MAX(REG_SEQ),0)+1" pattern
    ORDER_CODE VARCHAR(50),
    ORDER_SEQUENCE INTEGER,
    ITEM_SEQUENCE INTEGER,
    HOLD_CONFRIM_STATUS_BEF VARCHAR(10),
    HOLD_CONFRIM_STATUS_AFT VARCHAR(10),
    CREATED_USER_TP VARCHAR(10),
    CREATED_USER_ID VARCHAR(50),
    CREATED_DATE TIMESTAMP
);

-- =====================================================================
-- source: order-mapper.xml / order-claim-apply-mapper.xml (insertOrderItemSet and copy-for-claim variants — merged)
-- Domain: saleson.shop.order.domain.BuyItem (set-item sub-rows of OP_ORDER_ITEM)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET (
    ORDER_CODE               VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE            INTEGER NOT NULL,
    ITEM_SEQUENCE             INTEGER NOT NULL,
    SET_ITEM_SEQUENCE         INTEGER NOT NULL,
    USER_ID                   BIGINT,
    GUEST_FLAG                 CHAR(1),
    SELLER_ID                  BIGINT,
    ITEM_ID                    INTEGER,
    ITEM_CODE                  VARCHAR(50),
    ITEM_USER_CODE             VARCHAR(50),
    ITEM_NAME                  VARCHAR(255),
    IMAGE_SRC                  VARCHAR(255),
    PURCHASE_PRICE             INTEGER,
    COST_PRICE                 INTEGER,
    PRICE                      INTEGER,
    OPTION_PRICE               INTEGER,
    SALE_PRICE                 INTEGER,
    QUANTITY                   INTEGER,
    CLAIM_QUANTITY             INTEGER,
    ORDER_QUANTITY             INTEGER,
    SPOT_SALE_FLAG             CHAR(1),
    SPOT_TYPE                  VARCHAR(10),
    SPOT_DISCOUNT_PRICE        INTEGER,
    TAX_TYPE                   VARCHAR(10),
    COMMISSION_BASE_PRICE      INTEGER,
    COMMISSION_RATE            NUMERIC(5,2),
    COMMISSION_PRICE           INTEGER,
    COMMISSION_TYPE            VARCHAR(10),
    SUPPLY_PRICE                INTEGER,
    SELLER_DISCOUNT_PRICE      INTEGER,
    SELLER_DISCOUNT_DETAIL     VARCHAR(255),
    ADMIN_DISCOUNT_PRICE       INTEGER,
    ADMIN_DISCOUNT_DETAIL      VARCHAR(255),
    BRAND                       VARCHAR(255),
    OPTIONS                     VARCHAR(255),
    CREATED_DATE                VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE, SET_ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.BuyItem
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET_BUY_TEMP (
    USER_ID              BIGINT,
    SESSION_ID             VARCHAR(255),
    ORDER_CODE             VARCHAR(50),
    ITEM_SEQUENCE           INTEGER,
    ITEM_ID                  INTEGER,
    QUANTITY                 INTEGER,
    OPTIONS                   VARCHAR(255),
    SET_ITEM_SEQUENCE          INTEGER,
    CREATED_DATE                VARCHAR(14)
    -- TODO: primary key unclear — pre-checkout staging table
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.BuyItem
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_SET_TEMP (
    USER_ID              BIGINT,
    SESSION_ID            VARCHAR(255),
    ITEM_SEQUENCE          INTEGER,
    ITEM_ID                 INTEGER,
    QUANTITY                INTEGER,
    OPTIONS                 VARCHAR(255),
    SET_ITEM_SEQUENCE        INTEGER,
    CREATED_DATE              VARCHAR(14)
    -- TODO: primary key unclear — pre-checkout staging table
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.BuyItem
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_ITEM_TEMP (
    USER_ID                   BIGINT,
    SESSION_ID                 VARCHAR(255),
    ITEM_SEQUENCE               INTEGER,
    ITEM_ID                     INTEGER,
    QUANTITY                    INTEGER,
    OPTIONS                     VARCHAR(255),
    SHIPPING_PAYMENT_TYPE       VARCHAR(10),
    ADDITION_ITEM_FLAG          CHAR(1),
    PARENT_ITEM_SEQUENCE        INTEGER,
    PARENT_ITEM_ID               INTEGER,
    SET_ITEM_FLAG                CHAR(1),
    CREATED_DATE                 VARCHAR(14),
    CAMPAIGN_CODE                 VARCHAR(50),
    TEXT_OPTION                   TEXT
    -- TODO: primary key unclear — pre-checkout staging table keyed loosely by (USER_ID/SESSION_ID, ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-admin-mapper.xml / order-payment-mapper.xml / order-mapper.xml (batch_04),
--         temp-process-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.order.domain.OrderPayment
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_PAYMENT (
    ORDER_CODE               VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE            INTEGER NOT NULL,
    PAYMENT_SEQUENCE          INTEGER NOT NULL,
    PAYMENT_TYPE              VARCHAR(10),
    ORDER_PG_DATA_ID          INTEGER, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed BIGINT; chose INTEGER for consistency with OP_ORDER_PG_DATA.ORDER_PG_DATA_ID
    APPROVAL_TYPE             VARCHAR(50),
    DEVICE_TYPE                VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(10), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    CARD_EASY_TYPE            VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(10), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    BANK_VIRTUAL_NO           VARCHAR(50),
    BANK_IN_NAME              VARCHAR(255), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(100); chose VARCHAR(255)
    BANK_DATE                 VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    AMOUNT                     INTEGER, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed NUMERIC(15,2); chose INTEGER for consistency with OP_ORDER_ITEM price columns
    TAX_FREE_AMOUNT            INTEGER, -- CONFLICT: see AMOUNT
    CANCEL_AMOUNT               INTEGER, -- CONFLICT: see AMOUNT
    REMAINING_AMOUNT            INTEGER, -- CONFLICT: see AMOUNT
    PAY_DATE                    VARCHAR(14), -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
    NOW_PAYMENT_FLAG            CHAR(1),
    REFUND_FLAG                  CHAR(1),
    PAYMENT_SUMMARY              VARCHAR(500), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(500); chose VARCHAR(500)
    CREATED_DATE                 VARCHAR(14), -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, PAYMENT_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.BuyPayment
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_PAYMENT_BUY_TEMP (
    ORDER_CODE          VARCHAR(50),
    APPROVAL_TYPE        VARCHAR(50),
    SERVICE_TYPE          VARCHAR(50),
    AMOUNT                 INTEGER,
    TAX_FREE_AMOUNT         INTEGER,
    BANK_VIRTUAL_NO         VARCHAR(255),
    BANK_IN_NAME            VARCHAR(255),
    BANK_DATE               VARCHAR(14),
    SERVICE_MID              VARCHAR(50),
    SERVICE_KEY              VARCHAR(255),
    CREATED_DATE              VARCHAR(14)
    -- TODO: primary key unclear — pre-checkout staging table
);

-- =====================================================================
-- source: order-mapper.xml
-- Domain: saleson.shop.order.domain.OrderPgData
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_PG_DATA (
    ORDER_PG_DATA_ID      INTEGER NOT NULL,
    ORDER_CODE            VARCHAR(50),
    PG_SERVICE_TYPE       VARCHAR(50),
    PG_SERVICE_MID        VARCHAR(50),
    PG_SERVICE_KEY        VARCHAR(255),
    PG_PAYMENT_TYPE       VARCHAR(10),
    PG_KEY                VARCHAR(255),
    PG_AUTH_CODE          VARCHAR(255),
    PG_PROC_INFO          TEXT,
    PART_CANCEL_FLAG      CHAR(1),
    PART_CANCEL_DETAIL    TEXT,
    PG_AMOUNT             INTEGER,
    CREATED_DATE          VARCHAR(14),
    PRIMARY KEY (ORDER_PG_DATA_ID)
);

-- =====================================================================
-- source: order-refund-mapper.xml (batch_05a), temp-process-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.order.refund.domain.OrderRefund
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_REFUND (
    REFUND_CODE                  VARCHAR(50) PRIMARY KEY,
    REFUND_DATE                  VARCHAR(20), -- CONFLICT: batch_05a guessed VARCHAR(8), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE                INTEGER NOT NULL,
    REFUND_STATUS_CODE           VARCHAR(10), -- CONFLICT: batch_05a guessed CHAR(1), batch_07 guessed VARCHAR(10); chose VARCHAR(10)
    REQUEST_MANAGER_USER_NAME    VARCHAR(500), -- CONFLICT: batch_05a guessed VARCHAR(500) (stored encrypted), batch_07 guessed VARCHAR(255); chose VARCHAR(500)
    PROCESS_MANAGER_USER_NAME    VARCHAR(500), -- CONFLICT: see REQUEST_MANAGER_USER_NAME
    RETURN_BANK_NAME              VARCHAR(500), -- CONFLICT: batch_05a guessed VARCHAR(500) (encrypted), batch_07 guessed VARCHAR(100); chose VARCHAR(500)
    RETURN_VIRTUAL_NO             VARCHAR(500), -- CONFLICT: batch_05a guessed VARCHAR(500) (encrypted), batch_07 guessed VARCHAR(50); chose VARCHAR(500)
    RETURN_BANK_IN_NAME           VARCHAR(500), -- CONFLICT: batch_05a guessed VARCHAR(500) (encrypted), batch_07 guessed VARCHAR(100); chose VARCHAR(500)
    CREATED_DATE                  VARCHAR(20), -- CONFLICT: batch_05a guessed VARCHAR(20), batch_07 guessed TIMESTAMP; chose VARCHAR(20)
    UPDATED_DATE                  VARCHAR(20) -- CONFLICT: see CREATED_DATE
);

-- =====================================================================
-- source: order-claim-apply-mapper.xml (batch_04), temp-process-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.order.claimapply.domain.OrderReturnApply
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_RETURN_APPLY (
    -- CONFLICT (PK): batch_04 keyed on CLAIM_CODE alone (generated as 'R-<date>-<seq>'), batch_07 guessed
    -- composite (ORDER_CODE, ORDER_SEQUENCE, ITEM_SEQUENCE); chose CLAIM_CODE as the more confident natural key
    CLAIM_CODE                      VARCHAR(50) PRIMARY KEY,
    ORDER_CODE                      VARCHAR(50),
    ORDER_SEQUENCE                   INTEGER,
    ITEM_SEQUENCE                    INTEGER,
    PARENT_ITEM_SEQUENCE             INTEGER,
    RETURN_APPLY_DATE                 VARCHAR(20), -- CONFLICT: batch_04 guessed VARCHAR(8), batch_07 guessed VARCHAR(20); chose VARCHAR(20)
    REFUND_CODE                       VARCHAR(50),
    SHIPMENT_RETURN_SELLER_ID          BIGINT,
    SHIPMENT_RETURN_ID                  BIGINT, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed BIGINT; chose BIGINT for consistency with SHIPMENT_RETURN_SELLER_ID
    CLAIM_APPLY_SUBJECT                  VARCHAR(255), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(10); chose VARCHAR(255)
    CLAIM_STATUS                          VARCHAR(10),
    CLAIM_APPLY_QUANTITY                   INTEGER,
    PREVIOUS_ORDER_STATUS                   VARCHAR(10),
    RETURN_RESERVE_NAME                     VARCHAR(255),
    RETURN_RESERVE_PHONE                    VARCHAR(50),
    RETURN_RESERVE_MOBILE                    VARCHAR(50),
    RETURN_RESERVE_ZIPCODE                   VARCHAR(20),
    RETURN_RESERVE_SIDO                       VARCHAR(100), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100)
    RETURN_RESERVE_SIGUNGU                     VARCHAR(100), -- CONFLICT: see RETURN_RESERVE_SIDO
    RETURN_RESERVE_EUPMYEONDONG                 VARCHAR(100), -- CONFLICT: see RETURN_RESERVE_SIDO
    RETURN_RESERVE_ADDRESS                       TEXT,
    RETURN_RESERVE_ADDRESS2                       TEXT, -- CONFLICT: batch_04 guessed TEXT, batch_07 guessed VARCHAR(255); chose TEXT
    RETURN_SHIPPING_ASK_TYPE                       VARCHAR(10),
    RETURN_SHIPPING_NUMBER                          VARCHAR(100), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100)
    RETURN_SHIPPING_COMPANY_NAME                     VARCHAR(255),
    RETURN_SHIPPING_COMPANY_URL                       VARCHAR(500), -- CONFLICT: batch_04 guessed VARCHAR(255), batch_07 guessed VARCHAR(500); chose VARCHAR(500)
    COLLECTION_SHIPPING_AMOUNT                          INTEGER, -- CONFLICT: batch_04 guessed INTEGER, batch_07 guessed NUMERIC(15,2); chose INTEGER for consistency with OP_ORDER_ITEM price columns
    RETURN_REASON                                        VARCHAR(50), -- CONFLICT: batch_04 guessed VARCHAR(50), batch_07 guessed VARCHAR(10); chose VARCHAR(50)
    RETURN_REASON_TEXT                                    VARCHAR(255),
    RETURN_REASON_DETAIL                                   TEXT,
    RETURN_MEMO                                             TEXT,
    RETURN_REFUSAL_REASON_TEXT                               TEXT,
    RETURN_REAL_SHIPPING                                      INTEGER,
    UPDATED_DATE                                                TIMESTAMP,
    CREATED_DATE                                                VARCHAR(14) -- CONFLICT: batch_04 guessed VARCHAR(14), batch_07 guessed TIMESTAMP; chose VARCHAR(14)
);

-- =====================================================================
-- source: order-mapper.xml
-- Domain: saleson.shop.order.domain.OrderSendMessageLog
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SEND_MESSAGE_LOG (
    ORDER_CODE         VARCHAR(50),
    TEMPLATE_ID        VARCHAR(50),
    DELIVERY_NUMBER    VARCHAR(50),
    CREATED_DATE       VARCHAR(14)
    -- TODO: primary key unclear — lookup uses (ORDER_CODE, TEMPLATE_ID[, DELIVERY_NUMBER]) but duplicates are plausible (append-only log)
);

-- =====================================================================
-- source: order-claim-apply-mapper.xml
-- Domain: saleson.shop.order.claimapply.domain.OrderCancelApply (set-item sub-rows)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_CANCEL_APPLY (
    CLAIM_CODE            VARCHAR(50) NOT NULL,
    ORDER_CODE             VARCHAR(50),
    ORDER_SEQUENCE          INTEGER,
    ITEM_SEQUENCE            INTEGER,
    SET_ITEM_SEQUENCE         INTEGER NOT NULL,
    SET_CLAIM_CODE             VARCHAR(50),
    REFUND_CODE                 VARCHAR(50),
    CANCEL_APPLY_DATE             VARCHAR(8),
    CLAIM_STATUS                   VARCHAR(10),
    CLAIM_APPLY_QUANTITY            INTEGER,
    CREATED_DATE                     VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE, SET_ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-claim-apply-mapper.xml
-- Domain: saleson.shop.order.claimapply.domain.OrderExchangeApply (set-item sub-rows)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_EXCHANGE_APPLY (
    CLAIM_CODE            VARCHAR(50) NOT NULL,
    ORDER_CODE             VARCHAR(50),
    ORDER_SEQUENCE          INTEGER,
    ITEM_SEQUENCE            INTEGER,
    SET_ITEM_SEQUENCE         INTEGER NOT NULL,
    SET_CLAIM_CODE             VARCHAR(50),
    CLAIM_APPLY_QUANTITY         INTEGER,
    CLAIM_STATUS                   VARCHAR(10),
    EXCHANGE_APPLY_DATE              VARCHAR(8),
    CREATED_DATE                       VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE, SET_ITEM_SEQUENCE)
);

-- =====================================================================
-- source: order-claim-apply-mapper.xml
-- Domain: saleson.shop.order.claimapply.domain.OrderReturnApply (set-item sub-rows)
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SET_RETURN_APPLY (
    CLAIM_CODE            VARCHAR(50) NOT NULL,
    ORDER_CODE             VARCHAR(50),
    ORDER_SEQUENCE          INTEGER,
    ITEM_SEQUENCE            INTEGER,
    SET_ITEM_SEQUENCE         INTEGER NOT NULL,
    SET_CLAIM_CODE             VARCHAR(50),
    RETURN_APPLY_DATE            VARCHAR(8),
    REFUND_CODE                   VARCHAR(50),
    CLAIM_STATUS                   VARCHAR(10),
    CLAIM_APPLY_QUANTITY            INTEGER,
    CREATED_DATE                     VARCHAR(14),
    PRIMARY KEY (CLAIM_CODE, SET_ITEM_SEQUENCE) -- guess: composite, mirrors OP_ORDER_ITEM_SET's SET_ITEM_SEQUENCE granularity
);

-- =====================================================================
-- source: order-admin-mapper.xml / order-mapper.xml (insertOrderShipping / insertOrderShippingForCancelBatch — merged;
-- richest set from order-mapper.xml's main insertOrderShipping)
-- Domain: saleson.shop.order.domain.Shipping / OrderShipping
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING (
    ORDER_SHIPPING_ID            INTEGER, -- guess: surrogate id used by cancel-batch flow only; main flow keys off ORDER_CODE+ORDER_SEQUENCE+SHIPPING_SEQUENCE
    ORDER_CODE                   VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE                INTEGER NOT NULL,
    SHIPPING_SEQUENCE             INTEGER NOT NULL,
    SELLER_ID                     BIGINT,
    SHIPPING_TYPE                 VARCHAR(10),
    SHIPMENT_GROUP_CODE           VARCHAR(50),
    SHIPPING_GROUP_CODE           VARCHAR(50),
    ISLAND_TYPE                   VARCHAR(10),
    SHIPPING_ITEM_COUNT           INTEGER,
    SHIPPING                      INTEGER,
    SHIPPING_EXTRA_CHARGE1        INTEGER,
    SHIPPING_EXTRA_CHARGE2        INTEGER,
    SHIPPING_FREE_AMOUNT          INTEGER,
    REAL_SHIPPING                 INTEGER,
    SHIPPING_PAYMENT_TYPE         VARCHAR(10),
    PAY_SHIPPING                  INTEGER,
    DISCOUNT_SHIPPING             INTEGER,
    SHIPPING_COUPON_COUNT         INTEGER,
    REMITTANCE_AMOUNT             INTEGER,
    PREVIOUS_REAL_SHIPPING        INTEGER,
    PREVIOUS_PAY_SHIPPING         INTEGER,
    PREVIOUS_REMITTANCE_AMOUNT    INTEGER,
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, SHIPPING_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.Receiver
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_BUY_TEMP (
    USER_ID                   BIGINT,
    SESSION_ID                  VARCHAR(255),
    ORDER_CODE                  VARCHAR(50),
    SHIPPING_INDEX               INTEGER,
    RECEIVE_NAME                  VARCHAR(255),
    RECEIVE_MOBILE                 VARCHAR(255),
    RECEIVE_PHONE                   VARCHAR(255),
    RECEIVE_ZIPCODE                  VARCHAR(20),
    RECEIVE_NEW_ZIPCODE               VARCHAR(20),
    RECEIVE_SIDO                       VARCHAR(50),
    RECEIVE_SIGUNGU                     VARCHAR(50),
    RECEIVE_EUPMYEONDONG                 VARCHAR(50),
    RECEIVE_ADDRESS                       TEXT,
    RECEIVE_ADDRESS_DETAIL                 TEXT,
    CONTENT                                 TEXT,
    CREATED_DATE                             VARCHAR(14)
    -- TODO: primary key unclear — pre-checkout staging table, guess composite (USER_ID/SESSION_ID, ORDER_CODE, SHIPPING_INDEX)
);

-- =====================================================================
-- source: order-mapper.xml
-- Domain: saleson.shop.order.domain.ShippingCoupon
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_CP_BUY_TEMP (
    ORDER_CODE            VARCHAR(50) NOT NULL,
    USER_ID                BIGINT NOT NULL,
    SHIPPING_GROUP_CODE     VARCHAR(50) NOT NULL,
    USE_COUPON_COUNT         INTEGER,
    DISCOUNT_AMOUNT           INTEGER,
    CREATED_DATE              VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, USER_ID, SHIPPING_GROUP_CODE)
);

-- =====================================================================
-- source: order-admin-mapper.xml / order-mapper.xml (insertOrderShippingInfo / insertOffOrderShippingInfo — merged)
-- Domain: saleson.shop.order.domain.OrderShippingInfo / BuyAdminReceiver
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_SHIPPING_INFO (
    ORDER_CODE                  VARCHAR(50) NOT NULL,
    ORDER_SEQUENCE               INTEGER NOT NULL,
    SHIPPING_INFO_SEQUENCE       INTEGER NOT NULL,
    RECEIVE_NEW_ZIPCODE          VARCHAR(20),
    RECEIVE_ZIPCODE              VARCHAR(20),
    RECEIVE_SIDO                 VARCHAR(50),
    RECEIVE_SIGUNGU              VARCHAR(50),
    RECEIVE_EUPMYEONDONG         VARCHAR(50),
    RECEIVE_ADDRESS              TEXT,
    RECEIVE_ADDRESS_DETAIL       TEXT,
    RECEIVE_NAME                 VARCHAR(255),
    RECEIVE_PHONE                VARCHAR(50),
    RECEIVE_MOBILE               VARCHAR(50),
    MEMO                         TEXT,
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ORDER_CODE, ORDER_SEQUENCE, SHIPPING_INFO_SEQUENCE)
);

-- =====================================================================
-- source: order-mapper.xml (checkout staging table)
-- Domain: saleson.shop.order.domain.Buy
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_ORDER_TEMP (
    USER_ID                          BIGINT,
    SESSION_ID                        VARCHAR(255),
    ORDER_CODE                        VARCHAR(50) NOT NULL,
    USER_NAME                          VARCHAR(255),
    EMAIL                               VARCHAR(255),
    MOBILE                              VARCHAR(50),
    PHONE                               VARCHAR(50),
    ZIPCODE                             VARCHAR(20),
    NEW_ZIPCODE                         VARCHAR(20),
    SIDO                                 VARCHAR(50),
    SIGUNGU                              VARCHAR(50),
    EUPMYEONDONG                         VARCHAR(50),
    ADDRESS                              TEXT,
    ADDRESS_DETAIL                       TEXT,
    POINT_DISCOUNT_AMOUNT                INTEGER,
    SHIPPING_COUPON_USE_COUNT             INTEGER,
    SHIPPING_COUPON_DISCOUNT_AMT           INTEGER,
    ITEM_COUPON_DISCOUNT_AMOUNT            INTEGER,
    CART_COUPON_DISCOUNT_AMOUNT            INTEGER,
    ORDER_PAY_AMOUNT                        INTEGER,
    DELIVERY_REQ_DAY                        VARCHAR(50),
    DELIVERY_REQ_HOUR                       VARCHAR(50),
    CART_COUPON_USE_DATA                    TEXT,
    CASHBILL_TYPE                           VARCHAR(10),
    CASHBILL_CODE                           VARCHAR(50),
    SAVE_DELIVERY_FLAG                      CHAR(1),
    SAVE_DELIVERY_NAME                      VARCHAR(255),
    DEVICE_TYPE                             VARCHAR(10),
    CREATED_DATE                            VARCHAR(14),
    PRIMARY KEY (ORDER_CODE) -- guess: one staging row per in-progress order code
);

-- =====================================================================
-- source: point-mapper.xml
-- Domain: saleson.shop.point.domain.Point
-- guess: POINT_ID is a sequence-driven surrogate key (see OP_SEQUENCE /
--   updatePointSequence in this same mapper). GROUP_CODE, POINT_MANAGER_ID,
--   ORDER_ITEM_ID and ORDER_ID appear only in the resultMap or in a small
--   number of legacy-looking queries (getOrderReturnPointByParam,
--   getReturnPointListByParam) that are not exercised by the main
--   insert/select paths — kept as nullable/likely-unused legacy columns.
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_POINT (
    POINT_ID           INTEGER PRIMARY KEY,
    POINT_TYPE          VARCHAR(50),  -- guess: e.g. 'point', 'shipping'
    SAVED_TYPE           VARCHAR(10),
    SAVED_YEAR           VARCHAR(4),
    SAVED_MONTH          VARCHAR(2),
    SAVED_POINT          INTEGER,
    POINT                INTEGER,
    REASON               VARCHAR(255),
    USER_ID              BIGINT,
    MANAGER_USER_ID      BIGINT,
    ORDER_CODE           VARCHAR(50),
    ORDER_SEQUENCE       INTEGER,
    ITEM_SEQUENCE        INTEGER,
    EXPIRATION_DATE      VARCHAR(8),  -- guess: YYYYMMDD
    CREATED_DATE         VARCHAR(20),
    GROUP_CODE           VARCHAR(50),  -- guess: legacy/unused, seen only in resultMap
    POINT_MANAGER_ID     INTEGER,      -- guess: legacy/unused, seen only in resultMap
    ORDER_ITEM_ID        BIGINT,       -- guess: legacy column referenced in getOrderReturnPointByParam
    ORDER_ID             VARCHAR(50)   -- guess: legacy column referenced in getReturnPointListByParam
);

-- =====================================================================
-- source: batch/item-mapper-batch.xml (batch_00), point-mapper.xml (batch_05a) -- merged
-- Domain: saleson.shop.point.domain.PointConfig
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_POINT_CONFIG (
    POINT_CONFIG_ID INTEGER PRIMARY KEY,
    CONFIG_TYPE     VARCHAR(50), -- CONFLICT: batch_00 guessed VARCHAR(50), batch_05a guessed VARCHAR(10); chose VARCHAR(50)
    PERIOD_TYPE     VARCHAR(50), -- CONFLICT: see CONFIG_TYPE
    POINT_TYPE      VARCHAR(50),
    POINT           NUMERIC(15,2),
    START_DATE      VARCHAR(8),
    START_TIME      VARCHAR(2),
    END_DATE        VARCHAR(8),
    END_TIME        VARCHAR(2),
    ITEM_ID         INTEGER,
    REPEAT_DAY      VARCHAR(50), -- CONFLICT: batch_00 guessed VARCHAR(50) (day-of-week list), batch_05a guessed VARCHAR(2); chose VARCHAR(50)
    STATUS_CODE     VARCHAR(10), -- CONFLICT: batch_00 guessed VARCHAR(2), batch_05a guessed VARCHAR(10); chose VARCHAR(10)
    CREATED_USER_ID BIGINT,
    CREATED_DATE    VARCHAR(20)
);

-- source: point-mapper.xml
-- Domain: saleson.shop.point.domain.PointUsed
CREATE TABLE IF NOT EXISTS OP_POINT_USED (
    POINT_USED_ID        INTEGER PRIMARY KEY,
    POINT_ID             INTEGER NOT NULL,
    POINT_USED_GROUP_ID  INTEGER,
    USED_TYPE            VARCHAR(10), -- guess: '1' used, '2' expired
    POINT                INTEGER,
    DETAILS              VARCHAR(255),
    ORDER_CODE           VARCHAR(50),
    MANAGER_USER_ID      BIGINT,
    CREATED_DATE         VARCHAR(20),
    REMAINING_POINT      INTEGER,
    ORDER_ITEM_ID        BIGINT -- guess: legacy column, referenced in getOrderReturnPointTargetUsedHistoryByParam
);

-- =====================================================================
-- source: policy-mapper.xml
-- Domain: saleson.shop.policy.domain.Policy
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_POLICY (
    POLICY_ID          INTEGER PRIMARY KEY,
    POLICY_TYPE        VARCHAR(10), -- 0 agreement, 1 privacy policy, 2 trader law, 3 marketing, 5 copyright
    CONTENT            TEXT,
    CREATED_DATE       VARCHAR(20),
    CREATED_USER_ID    BIGINT,
    TITLE              VARCHAR(255),
    EXHIBITION_STATUS  CHAR(1), -- guess: Y/N
    UPDATED_DATE       VARCHAR(20),
    UPDATED_LOGIN_ID   VARCHAR(50)
);

-- =====================================================================
-- source: popup-mapper.xml
-- Domain: saleson.shop.popup.domain.Popup
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_POPUP (
    POPUP_ID          INTEGER PRIMARY KEY,
    POPUP_CLOSE       VARCHAR(5),  -- guess: close-behavior code
    POPUP_TYPE        VARCHAR(10),
    POPUP_STYLE       VARCHAR(10),
    SUBJECT           VARCHAR(255),
    CONTENT           TEXT,
    START_DATE        VARCHAR(8),
    START_TIME        VARCHAR(2),
    END_DATE          VARCHAR(8),
    END_TIME          VARCHAR(2),
    WIDTH             INTEGER,
    HEIGHT            INTEGER,
    TOP_POSITION      INTEGER,
    LEFT_POSITION     INTEGER,
    POPUP_IMAGE       VARCHAR(255),
    IMAGE_LINK        VARCHAR(255),
    BACKGROUND_COLOR  VARCHAR(20)
);

-- =====================================================================
-- source: privacy-access-mapper.xml
-- Domain: saleson.shop.log.support.PrivacyLogHistParam
-- guess: FK PRIVACY_ACCESS_LOG_ID -> OP_PRIVACY_ACCESS_LOG.ID (that table
--   is only UPDATEd in this batch, not INSERTed, so it is not defined
--   here — see report).
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_PRIVACY_ACCESS_LOG_HIST (
    HIST_ID               BIGINT PRIMARY KEY,
    CREATED_AT            VARCHAR(20),
    MANAGER_ID            BIGINT,
    PRIVACY_ACCESS_LOG_ID BIGINT,
    REASON                VARCHAR(255),
    REASON_TYPE           VARCHAR(50)
);

-- source: cubrid/qna-mapper.xml
-- Domain: saleson.shop.qna.domain.Qna
-- guess: CREATED_DATE is a Java String, populated via CommonMapper.datetime
--        (DATE_FORMAT(NOW(),'%Y%m%d%H%i%s')) and compared with CONCAT(...) in
--        WHERE clauses => modeled as VARCHAR(14), not TIMESTAMP.
--        USER_NAME/EMAIL are stored CRYPTO_ENC-encrypted (ciphertext), kept
--        as VARCHAR(255).
CREATE TABLE IF NOT EXISTS OP_QNA (
    QNA_ID           INTEGER NOT NULL,
    QNA_GROUP        VARCHAR(50),
    QNA_TYPE         VARCHAR(50),
    SUBJECT          VARCHAR(255),
    QUESTION         TEXT,
    USER_ID          BIGINT,
    USER_NAME        VARCHAR(255),
    EMAIL            VARCHAR(255),
    ITEM_ID          INTEGER,
    SELLER_ID        BIGINT,
    ORDER_CODE       VARCHAR(50),
    ANSWER_COUNT     INTEGER,
    SECRET_FLAG      CHAR(1),
    DISPLAY_FLAG     CHAR(1),
    CREATED_DATE     VARCHAR(14),
    DATA_STATUS_CODE VARCHAR(10),
    QNA_IMAGE        VARCHAR(255),
    USE_YN           CHAR(1),
    HITS             INTEGER,
    PRIMARY KEY (QNA_ID)
);

-- source: cubrid/qna-mapper.xml
-- Domain: saleson.shop.qna.domain.QnaAnswer
-- guess: ANSWER_DATE populated via CommonMapper.datetime (String in Java) =>
--        VARCHAR(14). HITS/SECRET_FLAG are referenced via SELECT * /
--        addQnaAnswerHitCount / getQnaByQnaAnswerId subquery columns but are
--        not present on the Java VO.
CREATE TABLE IF NOT EXISTS OP_QNA_ANSWER (
    QNA_ANSWER_ID    INTEGER NOT NULL,
    QNA_ID           INTEGER,
    ANSWER           TEXT,
    USER_ID          BIGINT,
    TITLE            VARCHAR(255),
    SEND_SMS_FLAG    CHAR(1),
    SEND_MAIL_FLAG   CHAR(1),
    ANSWER_DATE      VARCHAR(14),
    DATA_STATUS_CODE VARCHAR(10),
    HITS             INTEGER, -- guess: not in VO, referenced via SELECT * / addQnaAnswerHitCount
    SECRET_FLAG      CHAR(1), -- guess: not in VO, referenced via SELECT * / getQnaByQnaAnswerId
    PRIMARY KEY (QNA_ANSWER_ID)
);

-- source: cubrid/qna-mapper.xml
-- Domain: saleson.shop.qna.domain.QnaOpenFile
CREATE TABLE IF NOT EXISTS OP_QNA_ANSWER_FILE (
    QNA_ANSWER_FILE_ID BIGINT NOT NULL, -- guess: PK; front-end obfuscates it via crypto_enc()
    QNA_ANSWER_ID       INTEGER,
    FILE_NAME           VARCHAR(255),
    FILE_TY             VARCHAR(50),
    ORDERING            INTEGER,
    CREATED_DATE        VARCHAR(14),
    ORG_FILE_NAME       VARCHAR(255),
    PRIMARY KEY (QNA_ANSWER_FILE_ID)
);

-- source: cubrid/qna-mapper.xml
-- Domain: saleson.shop.qna.domain.QnaOpenFile
-- guess: CREATED_DATE populated via CommonMapper.datetime => VARCHAR(14).
CREATE TABLE IF NOT EXISTS OP_QNA_FILE (
    QNA_FILE_ID   INTEGER NOT NULL,
    QNA_ID        INTEGER,
    FILE_NAME     VARCHAR(255),
    FILE_TY       VARCHAR(50),
    ORDERING      INTEGER,
    CREATED_DATE  VARCHAR(14),
    ORG_FILE_NAME VARCHAR(255),
    PRIMARY KEY (QNA_FILE_ID)
);

-- =====================================================================
-- source: batch/ranking-mapper-batch.xml (batch_00), ranking-mapper.xml (batch_05b) -- merged (identical definitions)
-- Domain: saleson.shop.ranking.domain.Ranking
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_RANKING (
    RANKING_ID    INTEGER PRIMARY KEY,
    CATEGORY_URL  VARCHAR(255),
    ITEM_ID       INTEGER,
    ORDERING      INTEGER
);

-- source: cubrid/ranking-batch-mapper.xml
-- Domain: saleson.shop.rankingbatch.support.RankingBatchParam (no dedicated domain VO)
-- guess: batch-generated ranking snapshot table; no explicit single-row PK
--        lookup exists (rows are bulk inserted/deleted by RANKING_TYPE +
--        RANKING_CODE), ORDERING gives per-item rank within the batch.
-- TODO: primary key unclear (RANKING_TYPE, RANKING_CODE, ITEM_ID) likely composite
CREATE TABLE IF NOT EXISTS OP_RANKING_BATCH (
    RANKING_TYPE     VARCHAR(10) NOT NULL,
    RANKING_CODE     VARCHAR(50) NOT NULL,
    ITEM_ID          INTEGER NOT NULL,
    ORDERING         INTEGER,
    DATA_STATUS_CODE VARCHAR(10)
);

-- source: cubrid/remittance-mapper.xml
-- Domain: saleson.shop.remittance.domain.Remittance (extends Seller)
-- guess: CONFIRM_DATE/FINISHING_DATE/CONFIRM_DATE_SELLER/PAYMENT_DATE are
--        Java Strings ('00000000' literal used as an empty-date sentinel in
--        insertRemittanceMasterNew) => VARCHAR(8). BANK_NAME/BANK_IN_NAME/
--        BANK_ACCOUNT_NUMBER are the INSERT column names (no REMITTANCE_
--        prefix), even though RemittanceResult SELECT aliases them as
--        REMITTANCE_BANK_NAME etc -- storage column names kept as-is.
CREATE TABLE IF NOT EXISTS OP_REMITTANCE (
    REMITTANCE_ID           BIGINT NOT NULL,
    CONFIRM_DATE            VARCHAR(8),
    FINISHING_DATE          VARCHAR(8),
    SELLER_ID               BIGINT,
    FINISHING_AMOUNT        INTEGER,
    FINISHING_MANAGER_NAME  VARCHAR(255),
    BANK_NAME               VARCHAR(100),
    BANK_IN_NAME            VARCHAR(255),
    BANK_ACCOUNT_NUMBER     VARCHAR(255),
    REMITTANCE_STATUS_CODE  VARCHAR(10),
    CONFIRM_DATE_SELLER     VARCHAR(8),
    PAYMENT_DATE            VARCHAR(8),
    CREATED_DATE            VARCHAR(14),
    PRIMARY KEY (REMITTANCE_ID)
);

-- source: cubrid/remittance-mapper.xml
-- Domain: saleson.shop.remittance.domain.RemittanceDetail
-- guess: REMITTANCE_DETAIL_ID column only appears in insertRemittanceDetailNew
--        (populated via ROWNUM); the older insertRemittanceDetail has no
--        explicit id column, implying an auto-increment/sequence-backed id
--        in CUBRID -- kept as PK. COMMISSION_RATE is a float rate => NUMERIC(5,2).
CREATE TABLE IF NOT EXISTS OP_REMITTANCE_DETAIL (
    REMITTANCE_DETAIL_ID   BIGINT NOT NULL, -- guess: PK, sequence/identity-generated
    REMITTANCE_ID          BIGINT,
    ORDER_CODE             VARCHAR(50),
    REMITTANCE_KEY         VARCHAR(100),
    ITEM_TYPE              VARCHAR(20),
    ITEM_USER_CODE         VARCHAR(50),
    ITEM_NAME              VARCHAR(255),
    OPTIONS                VARCHAR(255),
    SALE_PRICE             NUMERIC(15,2),
    SELLER_DISCOUNT_PRICE  NUMERIC(15,2),
    SELLER_DISCOUNT_DETAIL VARCHAR(255),
    SELLER_POINT           NUMERIC(15,2),
    SET_ITEM_FLAG          CHAR(1),
    SET_DISCOUNT_PRICE     NUMERIC(15,2),
    COMMISSION_BASE_PRICE  NUMERIC(15,2),
    COMMISSION_RATE        NUMERIC(5,2),
    COMMISSION_PRICE       NUMERIC(15,2),
    COMMISSION_TYPE        VARCHAR(20),
    SUPPLY_PRICE           NUMERIC(15,2),
    REMITTANCE_PRICE       NUMERIC(15,2),
    QUANTITY               INTEGER,
    ETC_AMT                NUMERIC(15,2),
    ORDER_SEQUENCE         INTEGER,
    ITEM_SEQUENCE          INTEGER,
    CREATED_DATE           VARCHAR(14),
    PRIMARY KEY (REMITTANCE_DETAIL_ID)
);

-- source: cubrid/restock-notice-mapper.xml
-- Domain: saleson.shop.restocknotice.domain.RestockNotice
-- guess: CREATED_DATE populated via CommonMapper.datetime (String) => VARCHAR(14).
CREATE TABLE IF NOT EXISTS OP_RESTOCK_NOTICE (
    RESTOCK_NOTICE_ID INTEGER NOT NULL,
    ITEM_ID           INTEGER,
    USER_ID           BIGINT,
    SEND_FLAG         CHAR(1),
    CREATED_DATE      VARCHAR(14),
    PRIMARY KEY (RESTOCK_NOTICE_ID)
);

-- source: cubrid/role-mapper.xml
-- Domain: com.onlinepowers.framework.web.opmanager.role.domain.Role (external framework class, not in this repo)
-- guess: two distinct id styles appear in the mapper -- AUTHORITY-keyed admin
--        roles via insert/updateRole/deleteRoleByAuthority, and a
--        ROLE_ID/ROLE_SEQ/ROLE_PARENT_ID hierarchy via getRoleList/getRole --
--        most likely the same OP_ROLE table serving two feature generations.
--        No INSERT INTO exists for ROLE_ID/ROLE_PARENT_ID/ROLE_SEQ in this
--        batch, so they are included defensively based on SELECT usage only.
CREATE TABLE IF NOT EXISTS OP_ROLE (
    ROLE_ID         INTEGER, -- guess: only read via getRoleList/getRole, never inserted in this batch
    ROLE_PARENT_ID  INTEGER, -- guess: self-referencing hierarchy column, read-only in this batch
    ROLE_SEQ        INTEGER, -- guess: ORDER BY column, read-only in this batch
    AUTHORITY       VARCHAR(100) NOT NULL,
    ROLE_NAME       VARCHAR(255),
    ROLE_DESC       VARCHAR(255),
    CREATED_DATE    TIMESTAMP,
    CREATED_USER_ID BIGINT,
    UPDATED_DATE    TIMESTAMP,
    UPDATE_USER_ID  BIGINT,
    PRIMARY KEY (AUTHORITY)
);

-- source: cubrid/search-mapper.xml
-- Domain: saleson.shop.search.domain.Search
-- guess: SEARCH_START_DATE/SEARCH_END_DATE/CREATED_DATE are Java Strings
--        compared directly against CommonMapper.datetime output => VARCHAR(14).
CREATE TABLE IF NOT EXISTS OP_SEARCH (
    SEARCH_ID                     INTEGER NOT NULL,
    SEARCH_CONTENTS                VARCHAR(255),
    SEARCH_LINK                     VARCHAR(255),
    SEARCH_MOBILE_LINK               VARCHAR(255),
    SEARCH_LINK_TARGET_FLAG           CHAR(1),
    SEARCH_MOBILE_LINK_TARGET_FLAG     CHAR(1),
    SEARCH_START_DATE                   VARCHAR(14),
    SEARCH_END_DATE                      VARCHAR(14),
    CREATED_DATE                          VARCHAR(14),
    PRIMARY KEY (SEARCH_ID)
);

-- source: cubrid/seller/seller-mapper.xml
-- Domain: saleson.seller.main.domain.Seller
-- guess: *_DATE columns are populated via DATE_FORMAT(NOW(),'%Y%m%d%H%i%s')
--        (see CommonMapper.datetime) and the Java field type is String, so
--        they are modeled as VARCHAR(20) rather than TIMESTAMP.
CREATE TABLE IF NOT EXISTS OP_SELLER (
    SELLER_ID                       BIGINT NOT NULL,
    SELLER_NAME                     VARCHAR(255),
    LOGIN_ID                        VARCHAR(255),
    PASSWORD                        VARCHAR(255),
    USER_NAME                       VARCHAR(255),
    TELEPHONE_NUMBER                VARCHAR(255),
    PHONE_NUMBER                    VARCHAR(255),
    FAX_NUMBER                      VARCHAR(255),
    EMAIL                           VARCHAR(255),
    POST                            VARCHAR(20),
    ADDRESS                         TEXT,
    ADDRESS_DETAIL                  VARCHAR(255),
    SECOND_USER_NAME                VARCHAR(255),
    SECOND_TELEPHONE_NUMBER         VARCHAR(255),
    SECOND_PHONE_NUMBER             VARCHAR(255),
    SECOND_EMAIL                    VARCHAR(255),
    COMPANY_NAME                    VARCHAR(255),
    REPRESENTATIVE_NAME             VARCHAR(255),
    BUSINESS_NUMBER                 VARCHAR(50),
    BUSINESS_LOCATION               VARCHAR(255),
    BUSINESS_TYPE                   VARCHAR(255),
    BUSINESS_ITEMS                  VARCHAR(255),
    COMMISSION_RATE                 NUMERIC(5,2),
    REMITTANCE_TYPE                 VARCHAR(20),
    REMITTANCE_DAY                  VARCHAR(20),
    BANK_NAME                       VARCHAR(100),
    BANK_IN_NAME                    VARCHAR(255),
    BANK_ACCOUNT_NUMBER             VARCHAR(255),
    SHIPPING_FLAG                   CHAR(1), -- guess: *_FLAG => Y/N pattern
    SHIPPING                        INTEGER,
    SHIPPING_FREE_AMOUNT            INTEGER,
    SHIPPING_EXTRA_CHARGE1          INTEGER,
    SHIPPING_EXTRA_CHARGE2          INTEGER,
    ITEM_APPROVAL_TYPE              VARCHAR(20),
    SMS_SEND_TIME                   VARCHAR(20),
    MD_ID                           INTEGER,
    MD_NAME                         VARCHAR(255),
    HEADER_CONTENT                  TEXT,
    STATUS_CODE                     VARCHAR(10),
    CREATED_DATE                    VARCHAR(20),
    CREATED_USER_ID                 BIGINT,
    UPDATED_DATE                    VARCHAR(20),
    UPDATED_USER_ID                 BIGINT,
    MAIL_ORDER_NUMBER               VARCHAR(50),
    BUY_SAFETY_USE_CONFIRM_NUMBER   VARCHAR(50),
    TAX_TYPE                        VARCHAR(20),
    FILE_NAME_CERTIFICATE1          VARCHAR(255),
    FILE_NAME_CERTIFICATE2          VARCHAR(255),
    FILE_NAME_CERTIFICATE3          VARCHAR(255),
    LOCGOV_CODE                     VARCHAR(50),
    ADULT_ITEM_YN                   CHAR(1),
    COMMUNITY_BUSINESS_YN           CHAR(1),
    PRIMARY KEY (SELLER_ID)
);

-- source: cubrid/seller/seller-user-mapper.xml
-- Domain: com.onlinepowers.framework.security.userdetails.User (base, not in
--         this repo) extended by saleson.shop.user.domain.SellerUser
-- guess: column list assembled from INSERT column list + SELECT/UPDATE
--        usage across this mapper and security-mapper.xml/seller-mapper.xml.
CREATE TABLE IF NOT EXISTS OP_SELLER_USER (
    USER_ID                  BIGINT NOT NULL,
    LOGIN_ID                 VARCHAR(255),
    PASSWORD                 VARCHAR(255),
    PHONE_NUMBER              VARCHAR(255),
    USER_NAME                VARCHAR(255),
    EMAIL                    VARCHAR(255),
    LOGIN_COUNT              INTEGER,
    LOGIN_DATE               VARCHAR(20),
    LOGIN_FAIL_COUNT         INTEGER,
    LOGIN_TRY_DATE           VARCHAR(20),
    PASSWORD_TYPE            VARCHAR(10),
    PASSWORD_EXPIRED_DATE    VARCHAR(20),
    UPDATED_DATE             VARCHAR(20),
    DENY_DATE                VARCHAR(20),
    LEAVE_DATE               VARCHAR(20),
    STATUS_CODE              VARCHAR(10),
    CREATED_DATE             VARCHAR(20),
    LOCGOV_CODE              VARCHAR(50),
    PWD_CHG_SELLER_YN        CHAR(1),
    MBER_DN                  VARCHAR(255),
    MBER_FIN_DN              VARCHAR(255),
    MBER_CI                  VARCHAR(255),
    RECEIVE_SMS              VARCHAR(10), -- guess: naming suggests flag/code, usage in this mapper is inconsistent (see insertSellerUser binding #{dormancyMailSent})
    PRIMARY KEY (USER_ID)
);

-- source: cubrid/sendmaillog-mapper.xml
-- Domain: saleson.shop.sendmaillog.domain.SendMailLog
-- guess: SEND_MAIL_LOG_ID is not present in the INSERT column list, implying
--        a DB-generated identity/sequence value.
CREATE TABLE IF NOT EXISTS OP_SEND_MAIL_LOG (
    SEND_MAIL_LOG_ID    SERIAL,
    ORDER_CODE          VARCHAR(50),
    USER_ID             BIGINT,
    VENDOR_ID           INTEGER,
    SEND_LOGIN_ID       VARCHAR(255),
    SEND_NAME           VARCHAR(255),
    SEND_EMAIL          VARCHAR(255),
    RECEIVE_LOGIN_ID    VARCHAR(255),
    RECEIVE_NAME        VARCHAR(255),
    RECEIVE_EMAIL       VARCHAR(255),
    SUBJECT             VARCHAR(500),
    CONTENT             TEXT,
    SEND_FLAG           CHAR(1),
    SEND_DATE           VARCHAR(20),
    SUSIN_NUMBER        VARCHAR(50),
    SEND_TYPE           VARCHAR(50),
    ORDER_STATUS        VARCHAR(20),
    MAIL_TYPE           VARCHAR(20),
    USE_TAG_FLAG        CHAR(1),
    CREATED_DATE        VARCHAR(20),
    PRIMARY KEY (SEND_MAIL_LOG_ID)
);

-- source: cubrid/sendsmslog-mapper.xml
-- Domain: saleson.shop.sendsmslog.domain.SendSmsLog
-- guess: SEND_SMS_LOG_ID is not present in the INSERT column list, implying
--        a DB-generated identity/sequence value.
CREATE TABLE IF NOT EXISTS OP_SEND_SMS_LOG (
    SEND_SMS_LOG_ID      SERIAL,
    ORDER_CODE           VARCHAR(50),
    USER_ID              BIGINT,
    SEND_TEL_NUMBER      VARCHAR(20),
    RECEIVE_TEL_NUMBER   VARCHAR(20),
    ORDER_STATUS         VARCHAR(20),
    CONTENT              TEXT,
    SEND_TYPE            VARCHAR(50),
    CREATED_DATE         VARCHAR(20),
    PRIMARY KEY (SEND_SMS_LOG_ID)
);

-- source: cubrid/seo-mapper.xml
-- Domain: saleson.shop.seo.domain.Seo
CREATE TABLE IF NOT EXISTS OP_SEO (
    SEO_ID                    INTEGER NOT NULL,
    SEO_URL                   VARCHAR(255),
    TITLE                     VARCHAR(255),
    KEYWORDS                  VARCHAR(255),
    DESCRIPTION               TEXT,
    HEADER_CONTENTS1          TEXT,
    HEADER_CONTENTS2          TEXT,
    HEADER_CONTENTS3          TEXT,
    THEMAWORD_TITLE           VARCHAR(255),
    THEMAWORD_DESCRIPTION     TEXT,
    INDEX_FLAG                CHAR(1),
    CREATED_USER_ID           BIGINT,
    CREATED_DATE              VARCHAR(20),
    PRIMARY KEY (SEO_ID)
);

-- ---------------------------------------------------------------------
-- source: framework-sequence-mapper.xml (insertKey / insertSequenceBy)
-- domain: com.onlinepowers.framework.sequence.domain.Sequence (not present in this repo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_SEQUENCE (
    SEQUENCE_KEY                  VARCHAR(100)     PRIMARY KEY,
    SEQUENCE_ID                    BIGINT
);

-- source: cubrid/shadow-login-log-mapper.xml
-- Domain: saleson.shop.shadowlogin.domain.ShadowLoginLog
CREATE TABLE IF NOT EXISTS OP_SHADOW_LOGIN_LOG (
    SHADOW_LOGIN_LOG_ID   INTEGER NOT NULL,
    MANAGER_ID            BIGINT,
    LOGIN_TARGET_ID       BIGINT,
    LOGIN_TARGET          VARCHAR(50),
    LOGIN_DATE            VARCHAR(20),
    LOGOUT_DATE           VARCHAR(20),
    PRIMARY KEY (SHADOW_LOGIN_LOG_ID)
);

-- source: cubrid/shipment-mapper.xml
-- Domain: saleson.shop.shipment.domain.Shipment
CREATE TABLE IF NOT EXISTS OP_SHIPMENT (
    SHIPMENT_ID              INTEGER NOT NULL,
    SELLER_ID                BIGINT,
    ADDRESS_NAME             VARCHAR(255),
    NAME                     VARCHAR(255),
    TELEPHONE_NUMBER         VARCHAR(255),
    ZIPCODE                  VARCHAR(20),
    ADDRESS                  TEXT,
    ADDRESS_DETAIL           VARCHAR(255),
    DEFAULT_ADDRESS_FLAG     CHAR(1),
    SHIPPING                 INTEGER,
    SHIPPING_FREE_AMOUNT     INTEGER,
    SHIPPING_EXTRA_CHARGE1   INTEGER,
    SHIPPING_EXTRA_CHARGE2   INTEGER,
    SHIPMENT_GROUP_CODE      VARCHAR(50),
    CREATED_DATE             VARCHAR(20),
    UPDATED_DATE             VARCHAR(20),
    PRIMARY KEY (SHIPMENT_ID)
);

-- source: cubrid/shipment-return-mapper.xml
-- Domain: saleson.shop.shipmentreturn.domain.ShipmentReturn
CREATE TABLE IF NOT EXISTS OP_SHIPMENT_RETURN (
    SHIPMENT_RETURN_ID       INTEGER NOT NULL,
    SELLER_ID                BIGINT,
    ADDRESS_NAME             VARCHAR(255),
    NAME                     VARCHAR(255),
    TELEPHONE_NUMBER         VARCHAR(255),
    ZIPCODE                  VARCHAR(20),
    ADDRESS                  TEXT,
    ADDRESS_DETAIL           VARCHAR(255),
    DEFAULT_ADDRESS_FLAG     CHAR(1),
    CREATED_DATE             VARCHAR(20),
    PRIMARY KEY (SHIPMENT_RETURN_ID)
);

-- =====================================================================
-- source: inquiry-mapper.xml
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_SHOP_INQUIRY (
    INQUIRY_ID          INTEGER PRIMARY KEY,
    INQUIRY_TYPE        VARCHAR(50),
    ITEM_CODE           VARCHAR(50),
    ITEM_NAME           VARCHAR(255),
    USER_NAME           VARCHAR(255),
    USER_EMAIL          VARCHAR(255),
    TEL_NUMBER          VARCHAR(50),
    INQUIRY_SUBJECT     VARCHAR(255),
    INQUIRY_CONTENT     TEXT,
    ANSWER_FLAG         INTEGER,          -- guess: compared as 0/1 int (I.ANSWER_FLAG = 1 / = 0), Java field is int not Y/N char
    INQUIRY_IMG         VARCHAR(255),
    CREATED_DATE        TIMESTAMP
);

-- source: cubrid/smsconfig-mapper.xml
-- Domain: saleson.shop.smsconfig.domain.SmsConfig
CREATE TABLE IF NOT EXISTS OP_SMS_CONFIG (
    SMS_CONFIG_ID     INTEGER NOT NULL,
    TEMPLATE_ID       VARCHAR(50),
    SMS_TYPE          VARCHAR(50),
    BUYER_TITLE       VARCHAR(255),
    BUYER_CONTENT     TEXT,
    ADMIN_TITLE       VARCHAR(255),
    ADMIN_CONTENT     TEXT,
    BUYER_SEND_FLAG   CHAR(1),
    ADMIN_SEND_FLAG   CHAR(1),
    CREATED_DATE      VARCHAR(20),
    PRIMARY KEY (SMS_CONFIG_ID)
);

-- source: store-inquiry-mapper.xml (saleson.shop.storeinquiry.StoreInquiryMapper)
CREATE TABLE IF NOT EXISTS OP_STORE_INQUIRY (
    STORE_INQUIRY_ID INTEGER PRIMARY KEY,
    COMPANY VARCHAR(255),
    USER_NAME VARCHAR(255),
    PHONE_NUMBER VARCHAR(50),
    EMAIL VARCHAR(255),
    HOMEPAGE VARCHAR(255),
    CONTENT TEXT,
    FILE_NAME VARCHAR(255),
    STATUS VARCHAR(10), -- guess: domain field is String but compared to unquoted numeric literals (0/1/2) in SQL
    CREATION_DATE TIMESTAMP
);

-- source: sys-notice-seller-mapper.xml (saleson.shop.notice.SysNoticeSellerMapper.insertNotice)
CREATE TABLE IF NOT EXISTS OP_SYS_NOTICE_SELLER (
    NOTICE_ID INTEGER PRIMARY KEY, -- useGeneratedKeys="true" keyProperty="noticeId" -> DB-generated
    SUBJECT VARCHAR(255),
    CONTENT TEXT,
    HITS INTEGER,
    BOARD_CODE VARCHAR(50),
    SUB_CATEGORY VARCHAR(50),
    NOTICE_FLAG CHAR(1),
    DISPLAY_FLAG CHAR(1),
    LOCGOV_CODE VARCHAR(50),
    USE_YN CHAR(1),
    FRST_CRT_ID BIGINT,
    FRST_CRT_DT TIMESTAMP,
    LAST_MDFCN_ID BIGINT,
    LAST_MDFCN_DT TIMESTAMP
);

-- source: sys-notice-seller-mapper.xml (saleson.shop.notice.SysNoticeSellerMapper.insertSysNoticeSellerFile)
CREATE TABLE IF NOT EXISTS OP_SYS_NOTICE_SELLER_FILE (
    FILE_ID BIGINT PRIMARY KEY, -- guess: domain field "fileId" (long); insert relies on DB default/sequence, not explicitly set
    NOTICE_ID INTEGER,
    ORGNL_ATCH_FILE_NM VARCHAR(255),
    ATCH_FILE_NM VARCHAR(255),
    ATCH_FILE_EXTN_NM VARCHAR(20),
    ATCH_FILE_SZ BIGINT,
    ATCH_FILE_SEQ INTEGER,
    ATCH_FILE_PATH_NM VARCHAR(500),
    USE_YN CHAR(1),
    FRST_CRT_ID BIGINT,
    FRST_CRT_DT TIMESTAMP,
    LAST_MDFCN_ID BIGINT,
    LAST_MDFCN_DT TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: framework-token-mapper.xml (insertToken)
-- domain: com.onlinepowers.framework.security.token.domain.Token (not present in this repo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_TOKEN (
    REQUEST_TOKEN                  VARCHAR(255)     PRIMARY KEY,
    REQUEST_TYPE                   VARCHAR(50),
    ACCESS_TOKEN                    VARCHAR(255),
    REMOTE_ADDR                     VARCHAR(50),
    EXPIRE_DATE                     TIMESTAMP,
    CREATED_DATE                    TIMESTAMP
);

-- source: user-mapper.xml (saleson.shop.user.UserMapper.insertUser)
-- Columns beyond the INSERT list were confirmed via getUserByLoginId / getUserByUserId
-- SELECT queries that alias U.<column> directly from OP_USER.
CREATE TABLE IF NOT EXISTS OP_USER (
    USER_ID BIGINT PRIMARY KEY,
    LOGIN_ID VARCHAR(255),
    PASSWORD VARCHAR(255),
    USER_NAME VARCHAR(255),
    EMAIL VARCHAR(255),
    STATUS_CODE VARCHAR(10),
    LOGIN_COUNT INTEGER,
    SLEEP_MAIL_SEND_DATE TIMESTAMP,
    LOGIN_DATE TIMESTAMP,
    DENY_DATE TIMESTAMP,
    LEAVE_DATE TIMESTAMP,
    LOGIN_FAIL_COUNT INTEGER,
    LOGIN_TRY_DATE TIMESTAMP,
    PASSWORD_TYPE VARCHAR(20),
    PASSWORD_EXPIRED_DATE TIMESTAMP,
    UPDATED_DATE TIMESTAMP,
    CREATED_DATE TIMESTAMP,
    LOCGOV_CODE VARCHAR(50),
    MBER_CI VARCHAR(255),
    MBER_DI VARCHAR(255),
    MBER_DN VARCHAR(255),
    SBSCRB_SE_CODE VARCHAR(10),
    LOGIN_PATH_CODE VARCHAR(10),
    USER_KEY VARCHAR(255)
);

-- source: manager-action-log-mapper.xml
CREATE TABLE IF NOT EXISTS OP_USER_ACTION_LOG (
    ACTION_LOG_ID     BIGINT PRIMARY KEY,   -- guess: getUserActionLogId computes next id via MAX(action_log_id)+1, so app-managed sequence rather than DB identity
    LOGIN_ID          VARCHAR(255),
    LOGIN_TYPE        VARCHAR(20),
    REQUEST_URI        VARCHAR(255),
    REQUEST_METHOD      VARCHAR(10),
    REMOTE_ADDR        VARCHAR(255),
    CREATED_DATE       TIMESTAMP
);

-- source: userauth-mapper.xml (saleson.common.userauth.UserAuthMapper)
-- guess: no single-column PK; rows are looked up via APP_KEY+SERVICE_TYPE+SERVICE_MODE
-- (+DATA_STATUS_CODE as a state flag), so no PRIMARY KEY is declared here.
CREATE TABLE IF NOT EXISTS OP_USER_AUTH (
    APP_KEY VARCHAR(255),
    SERVICE_TYPE VARCHAR(50),
    SERVICE_MODE VARCHAR(50), -- IPIN, PCC
    SERVICE_TARGET VARCHAR(50), -- JOIN, FIND-ID, FIND-PASSWORD
    USER_IP VARCHAR(50),
    AUTH_KEY VARCHAR(255),
    AUTH_NAME VARCHAR(255),
    AUTH_SEX VARCHAR(10),
    AUTH_BIRTH_DAY VARCHAR(20),
    DATA_STATUS_CODE INTEGER, -- guess: compared against unquoted 0 in WHERE clause
    CREATED_DATE TIMESTAMP
    -- TODO: primary key unclear (natural key is APP_KEY+SERVICE_TYPE+SERVICE_MODE+DATA_STATUS_CODE, not unique over time)
);

-- source: user-mapper.xml (saleson.shop.user.UserMapper.insertUserBirthday / insertUserBirthdayDec)
CREATE TABLE IF NOT EXISTS OP_USER_BIRTHDAY (
    USER_ID BIGINT PRIMARY KEY, -- guess: deleteUserBirthdayDec wipes the whole table and insertUserBirthdayDec repopulates it 1 row per user, implying USER_ID is unique
    BIRTHDAY VARCHAR(255),
    CREATED_DATE TIMESTAMP
);

-- ---------------------------------------------------------------------
-- source: change-log-mapper.xml (insertUserChangeLog)
-- domain: saleson.shop.log.domain.ChangeLog
-- guess: CHANGE_LOG_ID is never supplied by the INSERT, so it is treated as
-- an auto-generated identity column.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OP_USER_CHANGE_LOG (
    CHANGE_LOG_ID  SERIAL PRIMARY KEY,
    USER_ID        BIGINT,
    PARAMETER      TEXT,
    REMOTE_ADDR    VARCHAR(50),
    MANAGER_ID     BIGINT,
    CREATED_DATE   VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- source: generalcustomer-mapper.xml (insertSecedeCustomer)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS op_user_ci (
    user_id                     BIGINT          PRIMARY KEY,
    mber_ci                     VARCHAR(255)
);

-- source: userDelivery-mapper.xml (saleson.shop.userdelivery.UserDeliveryMapper)
CREATE TABLE IF NOT EXISTS OP_USER_DELIVERY (
    USER_DELIVERY_ID BIGINT PRIMARY KEY,
    USER_ID BIGINT,
    DEFAULT_FLAG CHAR(1), -- Y/N, confirmed via "DEFAULT_FLAG = 'Y'" comparisons
    TITLE VARCHAR(255),
    USER_NAME VARCHAR(255),
    PHONE VARCHAR(50),
    MOBILE VARCHAR(50),
    NEW_ZIPCODE VARCHAR(20),
    ZIPCODE VARCHAR(20),
    SIDO VARCHAR(100),
    SIGUNGU VARCHAR(100),
    EUPMYEONDONG VARCHAR(100),
    ADDRESS TEXT,
    ADDRESS_DETAIL VARCHAR(255),
    CREATED_DATE TIMESTAMP
);

-- source: user-mapper.xml (saleson.shop.user.UserMapper.insertUserDetail / updateUserDetail)
-- Additional columns (NEW_POST, RECEIVE_PUSH, BIRTHDAY_TYPE, POINT, LEAVE_CODE, LEAVE_USER_ID)
-- confirmed via defaultColumns <sql> fragment, updateUserDetail SET clause and
-- getUserByLoginId SELECT (UD.leave_code / UD.leave_user_id).
CREATE TABLE IF NOT EXISTS OP_USER_DETAIL (
    USER_ID BIGINT PRIMARY KEY,
    GROUP_CODE VARCHAR(50),
    LEVEL_ID INTEGER,
    USER_LEVEL_EXPIRATION_DATE TIMESTAMP,
    NEW_POST VARCHAR(20),
    POST VARCHAR(20),
    ADDRESS TEXT,
    ADDRESS_DETAIL VARCHAR(255),
    TEL_NUMBER VARCHAR(50),
    PHONE_NUMBER VARCHAR(50),
    FAX_NUMBER VARCHAR(50),
    RECEIVE_EMAIL CHAR(1),
    RECEIVE_SMS CHAR(1),
    RECEIVE_PUSH CHAR(1),
    RECEIVE_PBANC CHAR(1),
    RECEIVE_KAKAO CHAR(1),
    GENDER VARCHAR(10),
    AGE VARCHAR(10),
    POINT INTEGER,
    BUY_COUNT INTEGER,
    BUY_PRICE NUMERIC(15,2),
    LAST_BUY_DATE TIMESTAMP,
    LEAVE_REASON TEXT,
    SITE_FLAG CHAR(1),
    USE_FLAG CHAR(1),
    BIRTHDAY_TYPE VARCHAR(10),
    BIRTHDAY VARCHAR(255), -- stored encrypted (crypto_enc), not a native DATE
    LEAVE_CODE VARCHAR(50), -- guess: seen only as a bare select column, no domain field/insert confirming type
    LEAVE_USER_ID BIGINT -- guess: seen only as a bare select column
);

-- source: usergroup-mapper.xml (saleson.shop.usergroup.UserGroupMapper.insertUserGroups / insertGroupsOfUsers)
CREATE TABLE IF NOT EXISTS OP_USER_GROUP (
    GROUP_ID INTEGER,
    USER_ID BIGINT,
    PRIMARY KEY (GROUP_ID, USER_ID)
);

-- source: usergroup-mapper.xml (saleson.shop.usergroup.UserGroupMapper.insertUserGroupLog)
-- Log table, no natural single-column PK found.
CREATE TABLE IF NOT EXISTS OP_USER_GROUP_LOG (
    USER_ID BIGINT,
    GROUP_CODE VARCHAR(50),
    GROUP_NAME VARCHAR(255),
    ADMIN_USER_NAME VARCHAR(255),
    CREATED_DATE TIMESTAMP
    -- TODO: primary key unclear (append-only log table)
);

-- source: userlevel-mapper.xml (saleson.shop.userlevel.UserLevelMapper.insertUserLevel)
CREATE TABLE IF NOT EXISTS OP_USER_LEVEL (
    LEVEL_ID INTEGER PRIMARY KEY,
    GROUP_CODE VARCHAR(50),
    "DEPTH" INTEGER, -- reserved-word column name, quoted here to preserve exact source name
    LEVEL_NAME VARCHAR(255),
    FILE_NAME VARCHAR(255),
    PRICE_START INTEGER,
    PRICE_END INTEGER,
    DISCOUNT_RATE NUMERIC(5,2),
    POINT_RATE NUMERIC(5,2),
    SHIPPING_COUPON_COUNT INTEGER,
    RETENTION_PERIOD INTEGER,
    REFERENCE_PERIOD INTEGER,
    EXCEPT_REFERENCE_PERIOD INTEGER,
    CREATED_DATE TIMESTAMP
);

-- source: user-mapper.xml (insertUserLevelLogByUserLevel) and userlevel-mapper.xml (insertUserLevelLog)
-- Log table, no natural single-column PK found in either mapper.
CREATE TABLE IF NOT EXISTS OP_USER_LEVEL_LOG (
    USER_ID BIGINT,
    GROUP_CODE VARCHAR(50),
    LEVEL_ID INTEGER,
    LEVEL_NAME VARCHAR(255),
    ADMIN_USER_NAME VARCHAR(255),
    CREATED_DATE TIMESTAMP
    -- TODO: primary key unclear (append-only log table, no lookup-by-id query in either owning mapper)
);

-- source: login-log-mapper.xml
CREATE TABLE IF NOT EXISTS OP_USER_LOGIN_LOG (
    LOGIN_LOG_ID     INTEGER PRIMARY KEY,   -- guess: see OP_LOGIN_LOG
    LOGIN_TYPE       VARCHAR(20),
    LOGIN_ID         VARCHAR(255),
    SUCCESS_FLAG      CHAR(1),
    REMOTE_ADDR       VARCHAR(255),
    MEMO             VARCHAR(255),
    LOGIN_DATE        TIMESTAMP
);

-- source: user-mapper.xml (saleson.shop.user.UserMapper.insertUserParent)
-- Domain class saleson.shop.user.domain.UserParent has parentId/ci/di/cellCorp fields that
-- are NOT set by this INSERT statement (values passed as null or omitted); kept as guessed
-- columns since they are very likely part of the real table (parentId as surrogate PK).
CREATE TABLE IF NOT EXISTS OP_USER_PARENT (
    PARENT_ID BIGINT PRIMARY KEY, -- guess: domain field "parentId", not present in this INSERT's column list (likely auto-increment)
    USER_ID BIGINT,
    USER_NAME VARCHAR(255),
    GENDER VARCHAR(10),
    NATIONAL_INFO VARCHAR(255),
    CI VARCHAR(255), -- guess: domain field "ci" not referenced by this particular INSERT
    DI VARCHAR(255), -- guess: domain field "di" not referenced by this particular INSERT
    DN VARCHAR(255),
    CELL_CORP VARCHAR(50), -- guess: domain field "cellCorp" not referenced by this particular INSERT
    CELL_NO VARCHAR(255),
    CREATED_AT TIMESTAMP
);

-- =====================================================================
-- source: personincharge-mapper.xml (batch_05a), userrole-mapper.xml / user-mapper.xml (batch_07) -- merged
-- Domain: saleson.shop.user.domain.PersonInCharge
-- =====================================================================
CREATE TABLE IF NOT EXISTS OP_USER_ROLE (
    USER_ID    BIGINT NOT NULL,
    AUTHORITY  VARCHAR(100) NOT NULL, -- CONFLICT: batch_05a guessed VARCHAR(50), batch_07 guessed VARCHAR(100); chose VARCHAR(100) for consistency with OP_ROLE.AUTHORITY
    PRIMARY KEY (USER_ID, AUTHORITY)
);

-- source: user-mapper.xml (saleson.shop.user.UserMapper.insertSleepUser)
-- Full column list confirmed via getUserForWakeup SELECT (NEW_POST/TEL_NUMBER/FAX_NUMBER/
-- BIRTHDAY_TYPE are commented out of the INSERT but selected back, so the columns exist).
CREATE TABLE IF NOT EXISTS OP_USER_SLEEP (
    USER_ID BIGINT PRIMARY KEY,
    USER_NAME VARCHAR(255),
    EMAIL VARCHAR(255),
    NEW_POST VARCHAR(20),
    POST VARCHAR(20),
    ADDRESS TEXT,
    ADDRESS_DETAIL VARCHAR(255),
    TEL_NUMBER VARCHAR(50),
    PHONE_NUMBER VARCHAR(50),
    FAX_NUMBER VARCHAR(50),
    BIRTHDAY_TYPE VARCHAR(10),
    BIRTHDAY VARCHAR(255),
    CREATED_DATE TIMESTAMP
);

-- source: user-sns-mapper.xml (saleson.shop.usersns.UserSnsMapper)
CREATE TABLE IF NOT EXISTS OP_USER_SNS (
    SNS_USER_ID INTEGER PRIMARY KEY,
    SNS_ID VARCHAR(255),
    USER_ID BIGINT,
    SNS_TYPE VARCHAR(50), -- naver, facebook, kakao
    SNS_NAME VARCHAR(255),
    EMAIL VARCHAR(255),
    CREATED_DATE TIMESTAMP,
    CERTIFIED_DATE TIMESTAMP
);

-- source: stats-mapper.xml (saleson.shop.stats.StatsMapper.insertVisit)
-- resultType alias "Visit" -> saleson.shop.stats.domain.Visit (has a visitId field, but VISIT_ID
-- is never listed in the explicit INSERT column list; selectOpVisitIdNextValue implies a
-- sequence-backed surrogate key exists and is auto-populated by the DB on insert).
CREATE TABLE IF NOT EXISTS OP_VISIT (
    VISIT_ID BIGINT PRIMARY KEY, -- guess: not in explicit INSERT column list; inferred from domain field "visitId" + op_visit_id sequence
    VISIT_DATE VARCHAR(20),
    VISIT_TIME VARCHAR(20),
    "LANGUAGE" VARCHAR(10),
    REMOTE_ADDR VARCHAR(255), -- stored encrypted via crypto_enc
    REFERER VARCHAR(1000),
    AGENT VARCHAR(1000),
    "DOMAIN" VARCHAR(255),
    DOMAIN_NAME VARCHAR(255),
    BROWSER VARCHAR(100),
    OS VARCHAR(100),
    WEEKDAY VARCHAR(10)
);

-- source: stats-mapper.xml (saleson.shop.stats.StatsMapper.updateVisitCount, id="updateVisitCount" but is an INSERT)
-- Column list is positional/unnamed in the mapper ("VALUES (#{visitDate}, 'ko', 1)");
-- only VISIT_DATE and VISIT_COUNT are ever referenced elsewhere by name.
CREATE TABLE IF NOT EXISTS OP_VISIT_COUNT (
    VISIT_DATE VARCHAR(20) PRIMARY KEY, -- guess: PK inferred from "ON DUPLICATE KEY UPDATE VISIT_COUNT = VISIT_COUNT + 1" against a bare VISIT_DATE value
    LOCALE_CODE VARCHAR(10), -- guess: unnamed 2nd column, literal value is always 'ko'; real column name unconfirmed
    VISIT_COUNT INTEGER
);

-- source: wishlist-mapper.xml (saleson.shop.wishlist.WishlistMapper)
CREATE TABLE IF NOT EXISTS OP_WISHLIST (
    WISHLIST_ID INTEGER PRIMARY KEY,
    WISHLIST_GROUP_ID INTEGER,
    ITEM_ID INTEGER,
    ITEM_OPTION VARCHAR(255),
    ITEM_OPTION_GROUP_NAME VARCHAR(255),
    ITEM_OPTION_NAME VARCHAR(255),
    USER_ID BIGINT,
    CREATED_DATE TIMESTAMP
);

-- =====================================================================
-- source: main-mapper.xml
-- =====================================================================
-- TODO: primary key unclear -- CREATED_DATE is used for point lookups (WHERE created_date = ...) but a separate
-- REPORT_CREATEDATE column (auto-populated, not in the INSERT list, used for ORDER BY) suggests an unexposed auto-increment id.
CREATE TABLE IF NOT EXISTS REPORT_DASHBOARD (
    ON_CNTR_CNT          INTEGER,
    ON_CNTR_AMT          BIGINT,
    OFF_CNTR_CNT         INTEGER,
    OFF_CNTR_AMT         BIGINT,
    PRESENT_CNT          INTEGER,
    PRESENT_AMT          BIGINT,
    CALL_KOOKMIN         BIGINT,
    CALL_LOV             BIGINT,
    CALL_GIVER           BIGINT,
    CALL_NHBANK          BIGINT,
    CALL_AMT             BIGINT,
    PRESENT              BIGINT,
    TOTAL_CNTR           BIGINT,
    TOTAL_CNTRAMT        BIGINT,
    TOTAL_USER           BIGINT,
    CREATED_DATE         VARCHAR(20),   -- guess: passed in as #{createdDate} from a batch job, format unconfirmed (compared to shCntrDeStart/End)
    ON_CNTR_PRJ_CNT       INTEGER,
    ON_CNTR_PRJ_AMT       BIGINT,
    OFF_CNTR_PRJ_CNT      INTEGER,
    OFF_CNTR_PRJ_AMT      BIGINT,
    REPORT_CREATEDATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- guess: not in INSERT column list; used only in ORDER BY, assumed auto-populated
);

-- =====================================================================
-- source: alimtalk-mapper.xml
-- Domain class: saleson.common.alimtalk.Alimtalk (all fields String)
-- =====================================================================
CREATE TABLE IF NOT EXISTS TIF_ALIMTALK_INFO (
	IF_REQ_ID            VARCHAR(20) PRIMARY KEY, -- 연계 생성 아이디, formatted yyyyMMddHHmmssSSS
	TEMPLATE_CODE        VARCHAR(50),
	RECEIPT_NUM          VARCHAR(50),
	RECEIVER_NUM         VARCHAR(50),
	RECEIVER_NAME        VARCHAR(255),
	ORDER_DATE           VARCHAR(20),
	ORDER_NO             VARCHAR(50),
	ORDER_NAME           VARCHAR(255),
	CLAIM_REASON_DETAIL  TEXT,
	IF_START_TIME        VARCHAR(20), -- formatted yyyyMMddHHmmss
	IF_END_TIME          VARCHAR(20),
	IF_STATUS_CD         CHAR(1),     -- S:성공, F:실패
	IF_ERR_MSG           TEXT
);

-- source: cubrid/sms-mapper.xml
-- Domain: saleson.common.sms.domain.TifIpsSndngM
-- guess: PK inferred as INSTT_CRT_SN (institution-created serial number) —
--        updateOpSequence tracks MAX(INSTT_CRT_SN) against OP_SEQUENCE for
--        key 'TIF_IPS_SNDNG_M', and getSmsSendList orders by it. LIST_SN is
--        assigned the same generated value in every INSERT variant seen, so
--        it may be a duplicate/alias column rather than a separate key.
-- TODO: primary key unclear (LIST_SN vs INSTT_CRT_SN both look like candidates)
CREATE TABLE IF NOT EXISTS TIF_IPS_SNDNG_M (
    INFO_CRT_DT          TIMESTAMP,
    LIST_SN              BIGINT,
    SVC_ID               VARCHAR(50),
    SVC_GRP_ID           VARCHAR(50),
    INSTT_CRT_SN         BIGINT NOT NULL,
    INSTT_CRT_DOC_ID     VARCHAR(255),
    PRVC_IDNTFC_INFO     VARCHAR(255),
    PRVC_IDNTFC_SE_CD    VARCHAR(20),
    SNDNG_CNTNTS         TEXT,
    IMG1_URL_INFO        VARCHAR(500),
    IMG2_URL_INFO        VARCHAR(500),
    IMG3_URL_INFO        VARCHAR(500),
    ESB_IF_ID            VARCHAR(100),
    ESB_TX_ID            VARCHAR(100),
    ESB_INIT_TIME        VARCHAR(20),
    ESB_TX_TIME          VARCHAR(20),
    ESB_COMPT_TIME       VARCHAR(20),
    ESB_STATUS_CD        VARCHAR(10),
    ESB_WORK_GBN         VARCHAR(10),
    ESB_ERR_MSG          TEXT,
    PRIMARY KEY (INSTT_CRT_SN)
);
