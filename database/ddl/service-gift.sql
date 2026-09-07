-- Service: gift
-- ===================================================================
-- AS-IS 운영 DB 원본 스키마 기준 자동 생성 (84개 테이블)
-- 출처: 운영사이트 DB dump(CUBRID) -> PostgreSQL 변환. 인덱스는 이번 생성에서 제외(추후 실사용 쿼리 패턴 기반으로 추가).
-- 서비스간 FK는 제거됨(no cross-service FK 원칙).
-- ===================================================================

CREATE TABLE IF NOT EXISTS B_GIFT_SELL (
    CNTR_YMD                     VARCHAR(8) NOT NULL,
    UPPER_LOCGOV_CODE            VARCHAR(10) NOT NULL,
    UPPER_LOCGOV_NM              VARCHAR(50) NOT NULL,
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    LOCGOV_NM                    VARCHAR(50) NOT NULL,
    ITEM_ID                      INTEGER NOT NULL,
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    SELL_CNT                     BIGINT NOT NULL DEFAULT 0,
    SELL_PRICE                   BIGINT NOT NULL DEFAULT 0,
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (CNTR_YMD, LOCGOV_CODE, ITEM_ID)
);

-- 상품 이미지 설명(링크뷰)
CREATE TABLE IF NOT EXISTS G_GDS_IMG_EXPLN_LINK_VIEW (
    -- 상품 사용자 코드
    GDS_USER_CD                  VARCHAR(30) NOT NULL,
    -- 이미지 순서
    IMG_SEQ                      INTEGER NOT NULL,
    -- 이미지 설명
    IMG_EXPLN                    VARCHAR(5000) NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (GDS_USER_CD, IMG_SEQ)
);

-- 관심 답례품
CREATE TABLE IF NOT EXISTS G_INTRST_RTNPSNT (
    -- 회원 고유번호
    USER_ID                      BIGINT NOT NULL,
    -- 등록 일련번호
    REGIST_SN                    INTEGER NOT NULL,
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10) NOT NULL,
    -- 등록 일자
    REGIST_DE                    VARCHAR(8) NOT NULL,
    -- CATEGORY_CODE
    CATEGORY_CODE                VARCHAR(50),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (USER_ID, REGIST_SN)
);

-- 아이템 컨텐츠 이미지 설명정보
CREATE TABLE IF NOT EXISTS G_ITEM_CONTENT_IMG_DESC (
    -- 답례품 상세 이미지 ID
    ITEM_CONTENT_IMG_ID          BIGINT NOT NULL,
    -- 답례품 ID
    ITEM_ID                      BIGINT NOT NULL,
    -- 답례품 상세 이미지 설명
    IMG_DESC                     VARCHAR(5000) NOT NULL,
    -- 답례품 상세 이미지 순번
    IMG_SEQ                      INTEGER NOT NULL,
    -- 등록 user id
    CREATED_USER_ID              BIGINT NOT NULL,
    -- 등록일
    CREATED_DATE                 TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (ITEM_CONTENT_IMG_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_item_content_img_desc_item_content_img_id_seq START WITH 1000;
ALTER TABLE G_ITEM_CONTENT_IMG_DESC ALTER COLUMN ITEM_CONTENT_IMG_ID SET DEFAULT nextval('g_item_content_img_desc_item_content_img_id_seq');
ALTER SEQUENCE g_item_content_img_desc_item_content_img_id_seq OWNED BY G_ITEM_CONTENT_IMG_DESC.ITEM_CONTENT_IMG_ID;

-- 지자체 오프라인 대표 상품 관리
CREATE TABLE IF NOT EXISTS G_LCLGV_OFF_RPRS_GDS_MNG (
    -- 지방자치단체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    -- 상품(답례품) 아이디
    GDS_ID                       BIGINT NOT NULL,
    -- 정렬 순서
    SORT_SEQ                     SMALLINT NOT NULL,
    -- 옵션
    OPTIONS                      VARCHAR(4000),
    -- 최초 등록자 ID
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (GDS_ID, LCLGV_CD)
);

-- 지자체 행정 복지 센터 관리
CREATE TABLE IF NOT EXISTS G_LCLGV_PBADMS_WLFR_CNTR_MNG (
    -- 행정 복지 센터 아이디
    PBADMS_WLFR_CNTR_ID          BIGINT NOT NULL,
    -- 지자체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    -- 행정 복지 센터 명
    PBADMS_WLFR_CNTR_NM          VARCHAR(50) NOT NULL,
    -- 행정 복지 센터 코드
    PBADMS_WLFR_CNTR_CD          VARCHAR(20),
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
    PRIMARY KEY (PBADMS_WLFR_CNTR_ID)
);
CREATE SEQUENCE IF NOT EXISTS g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq START WITH 1000;
ALTER TABLE G_LCLGV_PBADMS_WLFR_CNTR_MNG ALTER COLUMN PBADMS_WLFR_CNTR_ID SET DEFAULT nextval('g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq');
ALTER SEQUENCE g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq OWNED BY G_LCLGV_PBADMS_WLFR_CNTR_MNG.PBADMS_WLFR_CNTR_ID;

-- 지자체 대표 상품 관리
CREATE TABLE IF NOT EXISTS G_LCLGV_RPRS_GDS_MNG (
    -- 지자체 코드
    LCLGV_CD                     VARCHAR(10) NOT NULL,
    -- 상품(답례품) 아이디
    GDS_ID                       BIGINT NOT NULL,
    -- 정렬 순서
    SORT_SEQ                     SMALLINT NOT NULL,
    -- 최초 등록자 ID
    FRST_RGTR_ID                 BIGINT NOT NULL,
    -- 최초 등록 일시
    FRST_REG_DT                  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (GDS_ID, LCLGV_CD)
);

-- 통합검색 내가찾은검색어
CREATE TABLE IF NOT EXISTS G_MYRECENT (
    -- 최신 키워드 ID
    RECENT_ID                    INTEGER NOT NULL,
    -- 키워드
    KEYWORD                      VARCHAR(50) NOT NULL,
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 작성일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (RECENT_ID)
);

-- 제철식품관
CREATE TABLE IF NOT EXISTS G_SEASON_FOOD (
    -- 제철식품관 월
    SEASON_FOOD_MONTH            INTEGER NOT NULL,
    -- 제철식품관 등록순번
    REG_SEQ                      INTEGER NOT NULL DEFAULT 1,
    -- 제철식품관 키워드
    SEASON_FOOD_KEYWORD          VARCHAR(1000),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (SEASON_FOOD_MONTH, REG_SEQ)
);

-- 제철식품관상품
CREATE TABLE IF NOT EXISTS G_SEASON_FOOD_ITEM (
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 제철식품관월
    SEASON_FOOD_MONTH            INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (ITEM_ID, SEASON_FOOD_MONTH)
);

-- 특산물관 등록상품
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM (
    -- 특산물관 관리 아이디
    SPCL_ITEM_MNG_ID             BIGINT NOT NULL,
    -- 특산물 등록 상품
    ITEM_ID                      BIGINT NOT NULL,
    -- 상품 표시 순서
    DISPLAY_ORDER                INTEGER NOT NULL,
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID, ITEM_ID)
);

-- 특산물관 관리
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM_MNG (
    -- 특산물관 관리 아이디
    SPCL_ITEM_MNG_ID             BIGINT NOT NULL,
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(100) NOT NULL,
    -- 특산물 소개
    SPCL_ITEM_INFO               VARCHAR(1000),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID)
);

-- 특산물관 키워드
CREATE TABLE IF NOT EXISTS G_SPCL_ITEM_MNG_KEYWORD (
    -- 특산물관 관리 아이디
    SPCL_ITEM_MNG_ID             BIGINT NOT NULL,
    -- 등록순번
    REG_SEQ                      INTEGER NOT NULL DEFAULT 1,
    -- 특산물 상품 키워드
    SPCL_ITEM_KEYWORD            VARCHAR(1000),
    -- 최초 등록자 ID
    FRST_REGISTER_ID             BIGINT,
    -- 최초 등록 시점
    FRST_REGIST_PNTTM            TIMESTAMP NOT NULL DEFAULT now(),
    -- 최종 수정자 ID
    LAST_UPDUSR_ID               BIGINT,
    -- 최종 수정 시점
    LAST_UPDT_PNTTM              TIMESTAMP,
    PRIMARY KEY (SPCL_ITEM_MNG_ID, REG_SEQ)
);

-- 상품 브랜드
CREATE TABLE IF NOT EXISTS OP_BRAND (
    -- 브랜드 ID
    BRAND_ID                     INTEGER NOT NULL,
    -- 브랜드명
    BRAND_NAME                   VARCHAR(255) NOT NULL,
    -- 브랜드 이미지 (로고)
    BRAND_IMAGE                  VARCHAR(255),
    -- 브랜드 컨텐츠. (상단)
    BRAND_CONTENT                TEXT,
    -- 공개여부(Y: 공개, N: 비공개)
    DISPLAY_FLAG                 VARCHAR(1),
    -- 회원ID
    UPDATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10),
    PRIMARY KEY (BRAND_ID)
);

-- 상품 브랜드 카테고리
CREATE TABLE IF NOT EXISTS OP_BRAND_CATEGORY (
    -- 상품분류ID
    BRAND_CATEGORY_ID            INTEGER NOT NULL DEFAULT 0,
    -- 브랜드 ID
    BRAND_ID                     INTEGER NOT NULL,
    -- 카테고리ID
    CATEGORY_ID                  INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (BRAND_CATEGORY_ID)
);

-- 카테고리
CREATE TABLE IF NOT EXISTS OP_CATEGORY (
    -- 카테고리 고유ID
    CATEGORY_ID                  INTEGER NOT NULL,
    -- 카테고리 1차(000) 2차(000) 3차(000) 4차(000) 총 12자리 코드
    CATEGORY_CODE                VARCHAR(50) NOT NULL,
    -- 카테고리 그룹 ID
    CATEGORY_GROUP_ID            INTEGER,
    -- 카테고리 URL
    CATEGORY_URL                 VARCHAR(50) NOT NULL,
    -- 카테고리 명
    CATEGORY_NAME                VARCHAR(200) NOT NULL,
    -- 카테고리 타입
    CATEGORY_TYPE                VARCHAR(1) NOT NULL,
    -- 카테고리 상단꾸미기 HTML
    CATEGORY_HEADER              VARCHAR,
    -- 카테고리 하단꾸미기 HTML
    CATEGORY_FOOTER              VARCHAR,
    -- 카테고리 배너꾸미기 HTML
    CATEGORY_BANNER              VARCHAR,
    -- 카테고리 광고영역 HTML
    CATEGORY_ADVERTISEMENT       VARCHAR,
    -- 카테고리 모바일 영역
    CATEGORY_MOBILE_HTML         VARCHAR,
    -- 카테고리 모바일 상단꾸미기 HTML
    CATEGORY_MOBILE_HTML_HEADER  VARCHAR,
    -- 카테고리1차 (000)
    CATEGORY_CLASS1              VARCHAR(3) NOT NULL,
    -- 카테고리2차 (000)
    CATEGORY_CLASS2              VARCHAR(3) NOT NULL,
    -- 카테고리3차 (000)
    CATEGORY_CLASS3              VARCHAR(4) NOT NULL,
    -- 카테고리4차 (000)
    CATEGORY_CLASS4              VARCHAR(4) NOT NULL,
    -- 카테고리 단계
    CATEGORY_LEVEL               VARCHAR(1) NOT NULL,
    -- 카테고리 정렬
    ORDERING                     INTEGER NOT NULL,
    -- 카테고리 사용 유무
    CATEGORY_FLAG                VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 접근제어 (1:누구나, 2:회원만)
    ACCESS_TYPE                  VARCHAR(1) NOT NULL DEFAULT '1',
    -- 카테고리 배경이미지 (그룹페이지에서 노출)
    BACKGROUND_IMAGE             VARCHAR(100),
    -- 카테고리 SEO TITLE
    TITLE                        VARCHAR,
    -- 카테고리 SEO KEYWORDS
    KEYWORDS                     VARCHAR,
    -- 카테고리 SEO DESCRIPTION
    DESCRIPTION                  VARCHAR,
    -- 카테고리 SEO H1
    HEADER_CONTENTS1             VARCHAR,
    -- 카테고리 SEO H2
    HEADER_CONTENTS2             VARCHAR,
    -- 카테고리 SEO H3
    HEADER_CONTENTS3             VARCHAR,
    -- 카테고리 SEO THEMAWORD TITLE
    THEMAWORD_TITLE              VARCHAR,
    -- 카테고리 SEO THEMAWORD DESCRIPTION
    THEMAWORD_DESCRIPTION        VARCHAR,
    -- 랭크 페이지 SEO TITLE
    RANK_TITLE                   VARCHAR,
    -- 랭크 페이지 SEO KEYWORDS
    RANK_KEYWORDS                VARCHAR,
    -- 랭크 페이지 SEO DESCRIPTION
    RANK_DESCRIPTION             VARCHAR,
    -- 랭크 페이지 SEO H1
    RANK_HEADERCONTENTS1         VARCHAR,
    -- 랭크 페이지 SEO THEMAWORD TITLE
    RANK_THEMAWORD_TITLE         VARCHAR,
    -- 랭크 페이지 SEO THEMAWORD DESCRIPTION
    RANK_THEMAWORD_DESCRIPTION   VARCHAR,
    -- 리뷰 페이지 SEO TITLE
    REVIEW_TITLE                 VARCHAR,
    -- 리뷰 페이지 SEO KEYWORDS
    REVIEW_KEYWORDS              VARCHAR,
    -- 리뷰 페이지 SEO DESCRIPTION
    REVIEW_DESCRIPTION           VARCHAR,
    -- 리뷰 페이지 SEO H1
    REVIEW_HEADERCONTENTS1       VARCHAR,
    -- 리뷰 페이지 SEO THEMAWORD TITLE
    REVIEW_THEMAWORD_TITLE       VARCHAR,
    -- 리뷰 페이지 SEO THEMAWORD DESCRIPTION
    REVIEW_THEMAWORD_DESCRIPTION VARCHAR,
    PRIMARY KEY (CATEGORY_ID)
);

-- 카테고리 팀 그룹 화면 관리
CREATE TABLE IF NOT EXISTS OP_CATEGORY_EDIT (
    -- 메인 편집 고유ID
    CATEGORY_EDIT_ID             INTEGER NOT NULL DEFAULT 0,
    -- 팀 또는 그룹 코드값
    CODE                         VARCHAR(50) NOT NULL,
    -- 편집화면 종류 (1. 메인 2. 팀 3.그룹 4.카테고리)
    EDIT_KIND                    VARCHAR(1) NOT NULL,
    -- 편집화면 위치
    EDIT_POSITION                VARCHAR(50) NOT NULL,
    -- HTML 내용
    EDIT_CONTENT                 VARCHAR,
    -- 프로모션 이미지
    EDIT_IMAGE                   VARCHAR(100),
    -- 프로모션 URL
    EDIT_URL                     VARCHAR(255),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CATEGORY_EDIT_ID)
);

CREATE TABLE IF NOT EXISTS OP_CATEGORY_FILTER (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CATEGORY_ID                  INTEGER NOT NULL,
    FILTER_GROUP_ID              BIGINT NOT NULL,
    ORDERING                     INTEGER,
    PRIMARY KEY (ID)
);

-- 카테고리 그룹
CREATE TABLE IF NOT EXISTS OP_CATEGORY_GROUP (
    -- 카테고리 그룹 아이디
    CATEGORY_GROUP_ID            INTEGER NOT NULL,
    -- 카테고리 팀 아이디
    CATEGORY_TEAM_ID             INTEGER NOT NULL,
    -- 그룹명
    NAME                         VARCHAR(100),
    -- 그룹코드(7E에서는 LINK)
    CODE                         VARCHAR(50),
    -- 등록날짜
    CREATED_DATE                 VARCHAR(14),
    -- 수정날짜
    UPDATED_DATE                 VARCHAR(14),
    -- 노출여부 (Y:노출, N:비노출)
    CATEGORY_GROUP_FLAG          VARCHAR(1) DEFAULT 'Y',
    -- 접근제어 (1:누구나, 2:회원만)
    ACCESS_TYPE                  VARCHAR(1) DEFAULT '1',
    -- DEFCATE
    DEFCATE                      CHAR(1),
    -- SEO_페이지_meta_title
    TITLE                        VARCHAR,
    -- SEO_페이지_meta_keyword
    KEYWORDS                     VARCHAR,
    -- SEO_페이지_meta_description
    DESCRIPTION                  VARCHAR,
    -- SEO_페이지_h1
    HEADER_CONTENTS1             VARCHAR,
    -- SEO_페이지_h2
    HEADER_CONTENTS2             VARCHAR,
    -- SEO_페이지_h3
    HEADER_CONTENTS3             VARCHAR,
    -- SEO_페이지_테마워드_제목(하단에 노출됨)
    THEMAWORD_TITLE              VARCHAR,
    -- SEO_페이지_테마워드_내용(하단에 노출됨)
    THEMAWORD_DESCRIPTION        VARCHAR,
    -- SEO_랭킹페이지_meta_title
    RANK_TITLE                   VARCHAR,
    -- SEO_랭킹페이지_meta_keyword
    RANK_KEYWORDS                VARCHAR,
    -- SEO_랭킹페이지_meta_description
    RANK_DESCRIPTION             VARCHAR,
    -- SEO_랭킹페이지_h1
    RANK_HEADERCONTENTS1         VARCHAR,
    -- SEO_랭킹페이지_테마워드_제목(하단에 노출됨)
    RANK_THEMAWORD_TITLE         VARCHAR,
    -- SEO_랭킹페이지_테마워드_내용(하단에 노출됨)
    RANK_THEMAWORD_DESCRIPTION   VARCHAR,
    -- 정렬기능
    ORDERING                     INTEGER,
    ITEM_LIST                    VARCHAR(250),
    PRIMARY KEY (CATEGORY_GROUP_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_category_group_category_group_id_seq START WITH 1000;
ALTER TABLE OP_CATEGORY_GROUP ALTER COLUMN CATEGORY_GROUP_ID SET DEFAULT nextval('op_category_group_category_group_id_seq');
ALTER SEQUENCE op_category_group_category_group_id_seq OWNED BY OP_CATEGORY_GROUP.CATEGORY_GROUP_ID;

-- 그룹별 베너
CREATE TABLE IF NOT EXISTS OP_CATEGORY_GROUP_BANNER (
    -- 카테고리 그룹 배너 아이디
    CATEGORY_GROUP_BANNER_ID     INTEGER NOT NULL,
    -- 카테고리 그룹 아이디
    CATEGORY_GROUP_ID            INTEGER NOT NULL,
    -- 타이틀
    TITLE                        VARCHAR(255) NOT NULL,
    -- 링크URL
    LINK_URL                     VARCHAR(255) NOT NULL,
    -- 파일명
    FILE_NAME                    VARCHAR(255) NOT NULL,
    -- 노출순서
    DISPLAY_ORDER                INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (CATEGORY_GROUP_BANNER_ID)
);

-- 카테고리 팀
CREATE TABLE IF NOT EXISTS OP_CATEGORY_TEAM (
    -- 카테고리 팀 고유번호
    CATEGORY_TEAM_ID             INTEGER NOT NULL,
    -- 팀이름
    NAME                         VARCHAR(100) NOT NULL,
    -- 팀코드(7E에서는 LINK)
    CODE                         VARCHAR(50) NOT NULL,
    -- 등록날짜
    CREATED_DATE                 VARCHAR(14),
    -- 수정날짜
    UPDATED_DATE                 VARCHAR(14),
    -- 노출여부 (Y:노출, N:비노출)
    CATEGORY_TEAM_FLAG           VARCHAR(1) DEFAULT 'Y',
    -- SEO_페이지_meta_title
    TITLE                        VARCHAR,
    -- SEO_페이지_meta_keyword
    KEYWORDS                     VARCHAR,
    -- SEO_페이지_meta_description
    DESCRIPTION                  VARCHAR,
    -- SEO_페이지_h1
    HEADER_CONTENTS1             VARCHAR,
    -- SEO_페이지_h2
    HEADER_CONTENTS2             VARCHAR,
    -- SEO_페이지_h3
    HEADER_CONTENTS3             VARCHAR,
    -- SEO_페이지_테마워드_제목(하단에 노출됨)
    THEMAWORD_TITLE              VARCHAR,
    -- SEO_페이지_테마워드_내용(하단에 노출됨)
    THEMAWORD_DESCRIPTION        VARCHAR,
    -- SEO_랭킹페이지_meta_title
    RANK_TITLE                   VARCHAR,
    -- SEO_랭킹페이지_meta_keyword
    RANK_KEYWORDS                VARCHAR,
    -- SEO_랭킹페이지_meta_description
    RANK_DESCRIPTION             VARCHAR,
    -- SEO_랭킹페이지_h1
    RANK_HEADERCONTENTS1         VARCHAR,
    -- SEO_랭킹페이지_테마워드_제목(하단에 노출됨)
    RANK_THEMAWORD_TITLE         VARCHAR,
    -- SEO_랭킹페이지_테마워드_내용(하단에 노출됨)
    RANK_THEMAWORD_DESCRIPTION   VARCHAR,
    -- SEO_리뷰페이지_meta_title
    REVIEW_TITLE                 VARCHAR,
    -- SEO_리뷰페이지_meta_keyword
    REVIEW_KEYWORDS              VARCHAR,
    -- SEO_리뷰페이지_meta_description
    REVIEW_DESCRIPTION           VARCHAR,
    -- SEO_리뷰페이지_h1
    REVIEW_HEADERCONTENTS1       VARCHAR,
    -- SEO_리뷰페이지_테마워드_제목(하단에 노출됨)
    REVIEW_THEMAWORD_TITLE       VARCHAR,
    -- SEO_리뷰페이지_테마워드_내용(하단에 노출됨)
    REVIEW_THEMAWORD_DESCRIPTION VARCHAR,
    -- 베스트 아이템 노출 타입
    BEST_ITEM_DISPLAY_TYPE       VARCHAR(1) NOT NULL DEFAULT 'A',
    -- 정렬
    ORDERING                     INTEGER,
    PRIMARY KEY (CATEGORY_TEAM_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_category_team_category_team_id_seq START WITH 1000;
ALTER TABLE OP_CATEGORY_TEAM ALTER COLUMN CATEGORY_TEAM_ID SET DEFAULT nextval('op_category_team_category_team_id_seq');
ALTER SEQUENCE op_category_team_category_team_id_seq OWNED BY OP_CATEGORY_TEAM.CATEGORY_TEAM_ID;

-- 팀 카테고리 추천 상품 관리
CREATE TABLE IF NOT EXISTS OP_CATEGORY_TEAM_ITEM (
    -- 팀 카테고리 추천 상품 관리 고유ID
    CATEGORY_TEAM_ITEM_ID        INTEGER NOT NULL,
    -- 팀케테고리 ID
    CATEGORY_TEAM_ID             INTEGER NOT NULL,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (CATEGORY_TEAM_ITEM_ID)
);

-- 에디터 전시관리 공통
CREATE TABLE IF NOT EXISTS OP_DISPLAY_EDITOR (
    -- 공통 전시 코드
    DISPLAY_GROUP_CODE           VARCHAR(40) NOT NULL,
    -- 하위 전시 코드
    DISPLAY_SUB_CODE             VARCHAR(40),
    -- 전시 위치
    VIEW_TARGET                  VARCHAR(40) NOT NULL DEFAULT 'ALL',
    -- 내용
    DISPLAY_EDITOR_CONTENT       TEXT NOT NULL,
    -- 정렬순서
    ORDERING                     INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL
);

-- 화면 공통코드
CREATE TABLE IF NOT EXISTS OP_DISPLAY_GROUP_CODE (
    -- 화면전시 공통 코드
    DISPLAY_GROUP_CODE           VARCHAR(40) NOT NULL,
    -- 화면전시 공통 코드 이름
    DISPLAY_GROUP_CODE_NAME      VARCHAR(50),
    -- 사용자 TEMPLATE 정보(파일명)
    DISPLAY_TEMPLATE_CODE        VARCHAR(50),
    PRIMARY KEY (DISPLAY_GROUP_CODE)
);

-- 이미지 전시관리 공통
CREATE TABLE IF NOT EXISTS OP_DISPLAY_IMAGE (
    -- 공통 전시 코드
    DISPLAY_GROUP_CODE           VARCHAR(40) NOT NULL,
    -- 하위 전시 코드
    DISPLAY_SUB_CODE             VARCHAR(40),
    -- 전시 위치
    VIEW_TARGET                  VARCHAR(40) NOT NULL DEFAULT 'ALL',
    -- 전시 이미지
    DISPLAY_IMAGE                VARCHAR(100) NOT NULL,
    -- URL
    DISPLAY_URL                  VARCHAR(255),
    -- 내용
    DISPLAY_CONTENT              VARCHAR(1000),
    -- 정렬순서
    ORDERING                     INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 배경색
    DISPLAY_COLOR                VARCHAR(20)
);

-- 상품 전시관리 공통
CREATE TABLE IF NOT EXISTS OP_DISPLAY_ITEM (
    -- 공통 전시 코드
    DISPLAY_GROUP_CODE           VARCHAR(40) NOT NULL,
    -- 하위 전시 코드
    DISPLAY_SUB_CODE             VARCHAR(40),
    -- 전시 위치
    VIEW_TARGET                  VARCHAR(40) NOT NULL DEFAULT 'ALL',
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 정렬순서
    ORDERING                     INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL
);

-- 화면구성
CREATE TABLE IF NOT EXISTS OP_DISPLAY_TEMPLATE (
    -- 사용자 TEMPLATE 정보(파일명)
    DISPLAY_TEMPLATE_CODE        VARCHAR(40) NOT NULL,
    -- 영역 설정값 JSON
    DISPLAY_SETTING_VALUE        TEXT NOT NULL,
    PRIMARY KEY (DISPLAY_TEMPLATE_CODE)
);

-- 특집페이지
CREATE TABLE IF NOT EXISTS OP_FEATURED (
    -- 특집페이지 고유ID
    FEATURED_ID                  INTEGER NOT NULL,
    -- 기획전/이벤트 구분 넘버(1:기획전, 2:이벤트)
    FEATURED_CLASS               INTEGER NOT NULL DEFAULT 1,
    -- 특집페이지 구분
    FEATURED_TYPE                VARCHAR(1) NOT NULL DEFAULT '1',
    -- 특집페이지 페이지 URL
    FEATURED_URL                 VARCHAR(50) NOT NULL,
    -- 특집페이지 코드
    FEATURED_CODE                VARCHAR(50) NOT NULL,
    -- 특집페이지 명
    FEATURED_NAME                VARCHAR(255) NOT NULL,
    -- 특집페이지 간단한 설명
    FEATURED_SIMPLE_CONTENT      VARCHAR(255) NOT NULL,
    -- 특집페이지 내용
    FEATURED_CONTENT             VARCHAR NOT NULL,
    -- 특집페이지 대표 이미지
    FEATURED_IMAGE               VARCHAR(255),
    -- 모바일페이지 대표이미지
    FEATURED_IMAGE_MOBILE        VARCHAR(255),
    -- 특집페이지 썸네일 이미지
    THUMBNAIL_IMAGE              VARCHAR(255),
    -- 모바일페이지 썸네일 이미지
    THUMBNAIL_IMAGE_MOBILE       VARCHAR(255),
    -- 특집페이지 사용 유무
    FEATURED_FLAG                VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 외부링크
    LINK                         VARCHAR(100) NOT NULL,
    -- 외부링크 target="_blank"
    LINK_TARGET_FLAG             VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 외부링크 rel="nofollow"
    LINK_REL_FLAG                VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 리스트페이지 공개여부
    DISPLAY_LIST_FLAG            VARCHAR(1) DEFAULT 'Y',
    -- 특집 페이지 SEO TITLE
    TITLE                        VARCHAR,
    -- 특집 페이지 SEO KEYWORDS
    KEYWORDS                     VARCHAR,
    -- 특집 페이지 SEO DESCRIPTION
    DESCRIPTION                  VARCHAR,
    -- 특집 페이지 SEO H1
    HEADER_CONTENTS1             VARCHAR,
    -- 특집 페이지 SEO THEMAWORD TITLE
    THEMAWORD_TITLE              VARCHAR,
    -- 특집 페이지 SEO THEMAWORD DESCRIPTION
    THEMAWORD_DESCRIPTION        VARCHAR,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING_ESTHETIC            INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING_NAIL                INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING_MATSUGE_EXTENSION   INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING_HAIR                INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING_SALE_OUTLETS        INTEGER NOT NULL DEFAULT 0,
    -- 등록날짜
    CREATED_DATE                 VARCHAR(14),
    -- 리스트 형태(1행 2열, 1행 4열)
    LIST_TYPE                    VARCHAR(20),
    -- 선택상품 그룹형태 (Y:기본선택,N:사용자 그룹)
    PROD_STATE                   CHAR(1),
    -- 페이지 접근권한
    ACCESS_AUTH                  VARCHAR(4),
    -- 시작일
    START_DATE                   VARCHAR(12),
    -- 시작일 시작시간
    START_TIME                   VARCHAR(2),
    -- 마감일
    END_DATE                     VARCHAR(12),
    -- 마감일 마감시간
    END_TIME                     VARCHAR(2),
    -- 댓글 사용 여부
    REPLY_USED_FLAG              VARCHAR(1) DEFAULT 'N',
    -- 이벤트 코드
    EVENT_CODE                   VARCHAR(10),
    -- 특집페이지 주최/주관 명
    FEATURED_HOST                VARCHAR(255),
    -- 대표연락처 앞번호
    FEATURED_PHONE_NO1           VARCHAR(255),
    -- 대표연락처 중간번호
    FEATURED_PHONE_NO2           VARCHAR(255),
    -- 대표연락처 뒷번호
    FEATURED_PHONE_NO3           VARCHAR(255),
    -- 목록용 이미지
    FEATURED_LIST_IMAGE          VARCHAR(255),
    -- 목록용 이미지 썸네일
    THUMBNAIL_LIST_IMAGE         VARCHAR(255),
    -- 지자체 코드
    LOCGOV_CODE                  VARCHAR(10),
    PRIMARY KEY (FEATURED_ID)
);

-- 기획전 배너
CREATE TABLE IF NOT EXISTS OP_FEATURED_BANNER (
    -- 기획전 배너 ID
    FEATURED_BANNER_ID           INTEGER NOT NULL,
    -- 기획전 배너 색상
    FEATURED_BANNER_COLOR        VARCHAR(20),
    -- 배너 좌상 타이틀
    BANNER_LEFT_TOP_TITLE        VARCHAR(50),
    -- 배너 좌상 링크
    BANNER_LEFT_TOP_LINK         VARCHAR(255),
    -- 배너 좌상 이미지
    BANNER_LEFT_TOP_IMAGE        VARCHAR(255),
    -- 배너 좌하1 타이틀
    BANNER_LEFT_BOTTOM1_TITLE    VARCHAR(50),
    -- 배너 좌하1 링크
    BANNER_LEFT_BOTTOM1_LINK     VARCHAR(255),
    -- 배너 좌하1 이미지
    BANNER_LEFT_BOTTOM1_IMAGE    VARCHAR(255),
    -- 베너 좌하2 타이틀
    BANNER_LEFT_BOTTOM2_TITLE    VARCHAR(50),
    -- 배너 좌하2 링크
    BANNER_LEFT_BOTTOM2_LINK     VARCHAR(255),
    -- 배너 좌하2 이미지
    BANNER_LEFT_BOTTOM2_IMAGE    VARCHAR(255),
    -- 배너 중앙 타이틀
    BANNER_CENTER_TITLE          VARCHAR(50),
    -- 배너 중앙 링크
    BANNER_CENTER_LINK           VARCHAR(255),
    -- 배너 중앙 이미지
    BANNER_CENTER_IMAGE          VARCHAR(255),
    -- 배너 우상 타이틀
    BANNER_RIGHT_TOP_TITLE       VARCHAR(50),
    -- 배너 우상 링크
    BANNER_RIGHT_TOP_LINK        VARCHAR(255),
    -- 배너 우상 이미지
    BANNER_RIGHT_TOP_IMAGE       VARCHAR(255),
    -- 배너 우하1 타이틀
    BANNER_RIGHT_BOTTOM1_TITLE   VARCHAR(50),
    -- 배너 우하1 링크
    BANNER_RIGHT_BOTTOM1_LINK    VARCHAR(255),
    -- 배너 우하1 이미지
    BANNER_RIGHT_BOTTOM1_IMAGE   VARCHAR(255),
    -- 배너 우하2 타이틀
    BANNER_RIGHT_BOTTOM2_TITLE   VARCHAR(50),
    -- 배너 우하2 링크
    BANNER_RIGHT_BOTTOM2_LINK    VARCHAR(255),
    -- 배너 우하2 이미지
    BANNER_RIGHT_BOTTOM2_IMAGE   VARCHAR(255),
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (FEATURED_BANNER_ID)
);

-- 기획전 상품
CREATE TABLE IF NOT EXISTS OP_FEATURED_BANNER_ITEM (
    -- 기획전 배너페이지 위치
    FEATURED_BANNER_POSITION     VARCHAR(50),
    -- 상품ID
    ITEM_ID                      INTEGER,
    -- 정렬 순서
    DISPLAY_ORDER                INTEGER,
    -- 등록 날짜
    CREATED_DATE                 VARCHAR(14)
);

-- 기획페이지 상품정보
CREATE TABLE IF NOT EXISTS OP_FEATURED_ITEM (
    -- 등록순번
    REG_SEQ                      BIGINT NOT NULL,
    -- 기획페이지 고유ID
    FEATURED_ID                  INTEGER NOT NULL,
    -- 기획페이지 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 상품 노출 순서
    DISPLAY_ORDER                INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 사용자 정의 그룹
    USER_DEF_GROUP               VARCHAR(255),
    -- 사용자 정의 그룹 노출 순서
    USER_DEF_GROUP_ORDER         INTEGER,
    PRIMARY KEY (REG_SEQ)
);
CREATE SEQUENCE IF NOT EXISTS op_featured_item_reg_seq_seq START WITH 1000;
ALTER TABLE OP_FEATURED_ITEM ALTER COLUMN REG_SEQ SET DEFAULT nextval('op_featured_item_reg_seq_seq');
ALTER SEQUENCE op_featured_item_reg_seq_seq OWNED BY OP_FEATURED_ITEM.REG_SEQ;

-- 이벤트 댓글 정보
CREATE TABLE IF NOT EXISTS OP_FEATURED_REPLY (
    -- ID
    ID                           BIGINT NOT NULL,
    -- 이벤트 ID
    FEATURED_ID                  INTEGER NOT NULL,
    -- 유저 ID
    USER_ID                      BIGINT NOT NULL,
    -- 유저명
    USER_NAME                    VARCHAR(20) NOT NULL,
    -- 댓글 내용
    REPLY_CONTENT                TEXT,
    -- (0: 정상, 1:삭제)
    DATA_STATUS                  VARCHAR(1) DEFAULT '0',
    -- 생성일
    CREATED                      VARCHAR(14) NOT NULL,
    -- 수정일
    UPDATED                      VARCHAR(14),
    -- 생성자
    CREATED_BY                   BIGINT NOT NULL,
    -- 수정자
    UPDATED_BY                   BIGINT,
    PRIMARY KEY (ID)
);

-- 답례품 리뷰 필터코드
CREATE TABLE IF NOT EXISTS OP_FILTER_CODE (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일시
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 라벨
    LABEL                        VARCHAR(1000) NOT NULL,
    -- 라벨코드
    LABEL_CODE                   VARCHAR(1000),
    -- 라벨이미지
    LABEL_IMAGE                  VARCHAR(1000),
    -- 순서
    ORDERING                     INTEGER,
    FILTER_GROUP_ID              BIGINT NOT NULL,
    PRIMARY KEY (ID)
);

-- 답례품 리뷰 필터그룹
CREATE TABLE IF NOT EXISTS OP_FILTER_GROUP (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일시
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 설명
    DESCRIPTION                  VARCHAR(1000) NOT NULL,
    -- 필터유형
    FILTER_TYPE                  VARCHAR(255),
    -- 라벨
    LABEL                        VARCHAR(1000) NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_GIFT_GROUP (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    DATA_STATUS                  VARCHAR(10) NOT NULL,
    GROUP_TYPE                   VARCHAR(15) NOT NULL,
    NAME                         VARCHAR(30) NOT NULL,
    OVER_ORDER_PRICE             INTEGER,
    VALID_END_DATE               TIMESTAMP,
    VALID_START_DATE             TIMESTAMP,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_GIFT_GROUP_ITEM (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    GIFT_ITEM_ID                 BIGINT,
    GIFT_GROUP_ID                BIGINT,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_GIFT_ITEM (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CODE                         VARCHAR(30) NOT NULL,
    DATA_STATUS                  VARCHAR(10) NOT NULL,
    IMAGE                        VARCHAR(255),
    NAME                         VARCHAR(30) NOT NULL,
    PRICE                        INTEGER NOT NULL,
    SELLER_ID                    BIGINT NOT NULL,
    VALID_END_DATE               TIMESTAMP,
    VALID_START_DATE             TIMESTAMP,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_GIFT_ITEM_LOG (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    DATA_STATUS                  VARCHAR(10),
    GIFT_ITEM_ID                 BIGINT,
    IMAGE                        VARCHAR(255),
    NAME                         VARCHAR(30),
    PRICE                        INTEGER,
    VALID_END_DATE               TIMESTAMP,
    VALID_START_DATE             TIMESTAMP,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_GIFT_ITEM_RELATION (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    ITEM_ID                      INTEGER NOT NULL,
    GIFT_ITEM_ID                 BIGINT,
    PRIMARY KEY (ID)
);

-- 상품정보
CREATE TABLE IF NOT EXISTS OP_ITEM (
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 상품코드
    ITEM_CODE                    VARCHAR(30),
    -- 고객사 상품코드 (입점쇼핑몰에서는 사용하지 않음)
    ITEM_USER_CODE               VARCHAR(30),
    -- 상품명
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    -- 상품 간단 설명
    ITEM_SUMMARY                 VARCHAR(200),
    -- 상품 데이터 형태 (1: 일반상품, 2: 추가구성상품)
    ITEM_DATA_TYPE               VARCHAR(1) NOT NULL DEFAULT '1',
    -- 상품구분 (1:일반상품, 2:사업자상품, 3:세트상품)
    ITEM_TYPE                    VARCHAR(1),
    -- 상품유형 - 신상품 (Y:신상품, N: 일반상품)
    ITEM_NEW_FLAG                VARCHAR(1) DEFAULT 'N',
    -- 상품라벨 (0:없음, 2:NEW, 3:SALE, 4:사기) - 사용안함
    ITEM_LABEL                   VARCHAR(1) NOT NULL DEFAULT '1',
    -- 상품유형 - 무료배송 (1:선택)
    ITEM_TYPE1                   VARCHAR(1),
    -- 상품유형 - 대금상환 (1:선택)
    ITEM_TYPE2                   VARCHAR(1),
    -- 상품유형 - 신상품 (1:선택) - 사용안함.
    ITEM_TYPE3                   VARCHAR(1),
    -- 상품유형 - 추천상품 (1:선택)
    ITEM_TYPE4                   VARCHAR(1),
    -- 상품유형 - 스테디셀러 상품 (1:선택)
    ITEM_TYPE5                   VARCHAR(1),
    -- 상품유형
    ITEM_TYPE6                   VARCHAR(1),
    -- 상품유형
    ITEM_TYPE7                   VARCHAR(1),
    -- 상품유형
    ITEM_TYPE8                   VARCHAR(1),
    -- 상품유형
    ITEM_TYPE9                   VARCHAR(1),
    -- 상품유형
    ITEM_TYPE10                  VARCHAR(1),
    -- 전용상품 (00: 전체, 01:로그인전용 + COMMON_CODE > ITEM_PRIVATE_TYPE)
    PRIVATE_TYPE                 VARCHAR(20) DEFAULT '00',
    -- 공개여부 (Y:공개, N:비공개)
    DISPLAY_FLAG                 VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 미성년자 구매 가능여부 (Y:구매가능, N:구매불가)
    MINOR_ALLOW_FLAG             VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 원산지
    ORIGIN_COUNTRY               VARCHAR(50),
    -- 제조사
    MANUFACTURER                 VARCHAR(50),
    -- 브랜드ID (NULL: 자사제품, 기타: 타사제품) OP_SHOP_BRAND참고
    BRAND_ID                     INTEGER,
    -- 브랜드
    BRAND                        VARCHAR(50),
    -- 색상
    COLOR                        VARCHAR(20),
    -- 상품 무게
    WEIGHT                       VARCHAR(20),
    -- 비회원 구매 구분 (1:가능, 2: 가격표시안됨, 3:상세페이지 접근불가)
    NONMEMBER_ORDER_TYPE         VARCHAR(1),
    -- 부과세면세여부 (1:과세, 2:면세)
    TAX_TYPE                     VARCHAR(1) DEFAULT '2',
    -- 수수료설정 (1:입점업체 수수료로 설정, 2: 상품별 수수료로 설정)
    COMMISSION_TYPE              VARCHAR(1) DEFAULT '1',
    -- 수수료율
    COMMISSION_RATE              DOUBLE PRECISION DEFAULT 0.000000,
    -- 가격 입력 기준 (1: 공급가기준-수수료자동입력, 2: 수수료기준-공급가 자동입력)
    PRICE_CRITERIA               VARCHAR(1) DEFAULT '0',
    -- 정가
    ITEM_PRICE                   VARCHAR(20),
    -- 원가 
    COST_PRICE                   INTEGER DEFAULT 0,
    -- 공급가
    SUPPLY_PRICE                 INTEGER DEFAULT 0,
    -- 판매가 (회원)
    SALE_PRICE                   INTEGER NOT NULL DEFAULT 0,
    -- 비회원 판매가 별도 적용 여부 (Y: 적용, N: 미적용)
    SALE_PRICE_NONMEMBER_FLAG    VARCHAR(1) NOT NULL DEFAULT '0',
    -- 판매가 (비회원)
    SALE_PRICE_NONMEMBER         INTEGER,
    -- 스팟 판매 여부 (Y: 사용, N:사용안함)
    SPOT_FLAG                    VARCHAR(1) DEFAULT 'N',
    -- 스팟 기간 구분(1: 시점, 2: 기간) (시점 ex: 20190101 ~ 20190131 기간 동안 특정 시간만 노출, 기간 ex: 20190101 07:00 ~ 20190131 08:00 시작일과 종료일)
    SPOT_DATE_TYPE               VARCHAR(1),
    -- 스팟 판매 구분 (1.운영자 할인, 2: 판매자 할인)
    SPOT_TYPE                    VARCHAR(1),
    -- 스팟 적용 그룹 (적용할 회원그룹코드 - 미입력시 전체 회원적용)
    SPOT_APPLY_GROUP             VARCHAR(20),
    -- 스팟 판매 할인금액 (스팟상품) - SPOT_DISCOUNT_AMOUNT
    SPOT_DISCOUNT_AMOUNT         INTEGER DEFAULT 0,
    -- 스팟 판매 시작일
    SPOT_START_DATE              VARCHAR(8),
    -- 스팟 판매 종료일
    SPOT_END_DATE                VARCHAR(8),
    -- 스판 판매 시작시간
    SPOT_START_TIME              VARCHAR(6),
    -- 스판 판매 종료시간
    SPOT_END_TIME                VARCHAR(6),
    -- 스팟 판매 요일
    SPOT_WEEK_DAY                VARCHAR(7),
    -- 판매포인트 (포인트상품)
    SALE_POINT                   INTEGER DEFAULT 0,
    -- 판매자 부담 할인 여부 (Y:사용, N:사용안함)
    SELLER_DISCOUNT_FLAG         VARCHAR(1),
    -- 할인구분 (1:금액, 2:비율)
    SELLER_DISCOUNT_TYPE         VARCHAR(1),
    -- 할인금액(율)
    SELLER_DISCOUNT_AMOUNT       INTEGER DEFAULT 0,
    -- 판매자 포인트(상품별 포인트) 지급 여부 (Y: 사용, N: 사용안함) 
    SELLER_POINT_FLAG            VARCHAR(1) DEFAULT 'N',
    -- 품절 여부 (0:정상, 1: 품절)
    SOLD_OUT                     VARCHAR(1) NOT NULL,
    -- 재고 연동 유무(Y:연동, N:연동안함-무제한)
    STOCK_FLAG                   VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 재고수량
    STOCK_QUANTITY               INTEGER DEFAULT -1,
    -- 재고관리코드 (판매자관리코드)
    STOCK_CODE                   VARCHAR(50),
    -- 재입고 자동 설정 (Y:설정, N:설정안함)
    STOCK_SCHEDULE_AUTO_FLAG     VARCHAR(1),
    -- 재입고 예정 표시 방법 (
    STOCK_SCHEDULE_TYPE          VARCHAR(10),
    -- 재입고 날짜
    STOCK_SCHEDULE_DATE          VARCHAR(20),
    -- 재입고 예정 텍스트
    STOCK_SCHEDULE_TEXT          VARCHAR(40),
    -- 최소 주문 수량
    ORDER_MIN_QUANTITY           INTEGER DEFAULT -1,
    -- 최대 주문 수량
    ORDER_MAX_QUANTITY           INTEGER DEFAULT -1,
    -- 판매수량
    SALE_QUANTITY                INTEGER NOT NULL DEFAULT 0,
    -- 상품옵션 사용유무 (Y:사용, N:사용안함)
    ITEM_OPTION_FLAG             VARCHAR(1),
    -- 상품 옵션 타입 (S:선택형, S2:조합형, S3:3개조합형, T:텍스트형)
    ITEM_OPTION_TYPE             VARCHAR(2),
    -- 옵션명1
    ITEM_OPTION_TITLE1           VARCHAR(30),
    -- 옵션명2
    ITEM_OPTION_TITLE2           VARCHAR(30),
    -- 옵션명3
    ITEM_OPTION_TITLE3           VARCHAR(30),
    -- 추가구성 사용 (Y:사용, N:사용안함)
    ITEM_ADDITION_FLAG           VARCHAR(1),
    -- 사은품 여부 (Y:사용, N:사용안함)
    FREE_GIFT_FLAG               VARCHAR(1),
    -- 사은품명
    FREE_GIFT_NAME               VARCHAR(255),
    -- 검색어
    ITEM_KEYWORD                 VARCHAR(300),
    -- 상품상세설명
    DETAIL_CONTENT               VARCHAR,
    -- 상품상세설명 (모바일)
    DETAIL_CONTENT_MOBILE        VARCHAR,
    -- 상품정보고시 유형 코드
    ITEM_NOTICE_CODE             VARCHAR(50),
    -- 상품대표이미지
    ITEM_IMAGE                   VARCHAR(255),
    -- 소속 팀
    TEAM                         VARCHAR(30),
    -- 신상품 공개일시 (신상품, 공개인 경우 최초1회 등록됨)
    OPENTIME                     VARCHAR(14),
    -- 기본상품?
    BASE_ITEM                    TEXT,
    -- 자사/타사 (1:자사, 2:타사)
    OTHER_FLAG                   VARCHAR(1),
    -- 추천상품 포함여부 (Y:포함, N:포함하지않음)
    RECOMMEND_FLAG               VARCHAR(1),
    -- 관련상품 표시 방법 (1:동일 카테고리 자동, 2:직접선택)
    RELATION_ITEM_DISPLAY_TYPE   VARCHAR(1),
    -- 택배사 ID
    DELIVERY_COMPANY_ID          INTEGER,
    -- 택배사명
    DELIVERY_COMPANY_NAME        VARCHAR(30),
    -- 배송구분 (1:본사배송, 2:업체배송)
    DELIVERY_TYPE                VARCHAR(1),
    -- 출고지ID
    SHIPMENT_ID                  INTEGER,
    -- 배송정책 묶음배송 기준 코드
    SHIPMENT_GROUP_CODE          VARCHAR(50),
    -- 반송지 구분 (1:본사반송, 2:업체반송)
    SHIPMENT_RETURN_TYPE         VARCHAR(1),
    -- 반송지ID
    SHIPMENT_RETURN_ID           INTEGER,
    -- 배송비 구분 (1: 무료배송, 2: 판매자조건부, 3:출고지조건부, 4:상품조건부, 5:개당배송비, 6:고정배송비)
    SHIPPING_TYPE                VARCHAR(1),
    -- 묶음기준 코드
    SHIPPING_GROUP_CODE          VARCHAR(20),
    -- 배송비
    SHIPPING                     INTEGER,
    -- 조건부 무료배송 금액
    SHIPPING_FREE_AMOUNT         INTEGER,
    -- 개당배송비 부과 시 기준 상품 수량 (n개 당 얼마)
    SHIPPING_ITEM_COUNT          INTEGER DEFAULT 1,
    -- 제주도 추가 배송비
    SHIPPING_EXTRA_CHARGE1       INTEGER,
    -- 도서산간 추가 배송비
    SHIPPING_EXTRA_CHARGE2       INTEGER,
    -- 반품/교환 배송비 (편도기준금액)
    SHIPPING_RETURN              INTEGER,
    -- 반품 가능여부 (Y/N)
    ITEM_RETURN_FLAG             VARCHAR(1) DEFAULT 'Y',
    -- 조회수
    HITS                         INTEGER NOT NULL DEFAULT 0,
    -- 브라우저 타이틀
    SEO_TITLE                    VARCHAR(255),
    -- 메타 로봇 INDEX여부 (Y:인덱스시킴, N: meta-noindex)
    SEO_INDEX_FLAG               VARCHAR(1),
    -- 메타 키워드
    SEO_KEYWORDS                 VARCHAR(255),
    -- 메타 설명
    SEO_DESCRIPTION              VARCHAR(255),
    -- H1
    SEO_HEADER_CONTENTS1         VARCHAR(255),
    -- 테마워트 타이틀
    SEO_THEMAWORD_TITLE          VARCHAR(255),
    -- 테마워드 내용
    SEO_THEMAWORD_DESCRIPTION    TEXT,
    -- 담당MD
    MD_ID                        VARCHAR(11),
    -- 담당MD명
    MD_NAME                      VARCHAR(50),
    -- 쿠폰 사용 가능 여부 (Y : 사용가능)
    COUPON_USE_FLAG              VARCHAR(1) DEFAULT 'Y',
    -- 데이터 상태 메세지
    DATA_STATUS_MESSAGE          TEXT,
    -- 데이터 상태코드 (1: 정상, 20:등록신청, 21: 등록반려, 30:수정신청, 31: 수정반려, 40: 삭제신청, 41:삭제 반려, 90:판매종료, 99:삭제)
    DATA_STATUS_CODE             VARCHAR(10) NOT NULL DEFAULT '1',
    -- 회원ID
    UPDATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 사용자 화면(전시관리) 쪽에 노출여부
    FRONT_DISPLAY_FLAG           VARCHAR(1) DEFAULT 'Y',
    -- erp연동정보
    ERP_EXCEPTION_TYPE           VARCHAR(1),
    -- ITEM_NUMBER
    ITEM_NUMBER                  VARCHAR(255),
    -- ITEM_SALE_STATUS_TEXT
    ITEM_SALE_STATUS_TEXT        VARCHAR(255),
    -- NOINDEX_YN
    NOINDEX_YN                   VARCHAR(255),
    -- 네이버쇼핑 상품명
    NAVER_SHOPPING_ITEM_NAME     VARCHAR(100),
    -- 네이버쇼핑 사용여부 (Y:사용, N:미사용)
    NAVER_SHOPPING_FLAG          VARCHAR(1) DEFAULT 'N',
    -- 네이버페이 사용여부 (Y:사용, N:미사용)
    NAVER_PAY_FLAG               VARCHAR(1) DEFAULT 'Y',
    -- 세트상품 할인구분 (1:금액, 2:비율)
    SET_DISCOUNT_TYPE            VARCHAR(1),
    -- 세트상품 할인금액(율)
    SET_DISCOUNT_AMOUNT          INTEGER,
    -- 대표상품여부
    REPRESENTATIVE_ITEM_YN       VARCHAR(1) DEFAULT 'N',
    -- 성인상품여부
    ADULT_ITEM_YN                VARCHAR(1) DEFAULT 'N',
    -- 모바일상품여부
    MOBILE_ITEM_YN               VARCHAR(1) DEFAULT 'N',
    -- 답례품 제공자 전용 답례품 코드(고유코드)
    ITEM_SELLER_CODE             VARCHAR(30),
    ITEM_CLOSE_DT                VARCHAR(14),
    ITEM_TEXT_OPTION_TITLE1      VARCHAR(51),
    ITEM_TEXT_OPTION_TITLE2      VARCHAR(51),
    ITEM_TEXT_OPTION_TITLE3      VARCHAR(51),
    ITEM_TEXT_OPTION_FLAG        VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (ITEM_ID)
);

-- 추가구성 상품
CREATE TABLE IF NOT EXISTS OP_ITEM_ADDITION (
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 추가구성 상품ID
    ADDITION_ITEM_ID             INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (ITEM_ID, ADDITION_ITEM_ID)
);

CREATE TABLE IF NOT EXISTS OP_ITEM_BAKUP_0611_OPTION (
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    ITEM_CODE                    VARCHAR(30) NOT NULL,
    ITEM_USER_CODE               VARCHAR(30) NOT NULL,
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    ITEM_SUMMARY                 VARCHAR(200),
    ITEM_DATA_TYPE               VARCHAR(1) NOT NULL DEFAULT '1',
    ITEM_TYPE                    VARCHAR(1) NOT NULL,
    ITEM_NEW_FLAG                VARCHAR(1) DEFAULT 'N',
    ITEM_LABEL                   VARCHAR(1) NOT NULL DEFAULT '1',
    ITEM_TYPE1                   VARCHAR(1),
    ITEM_TYPE2                   VARCHAR(1),
    ITEM_TYPE3                   VARCHAR(1),
    ITEM_TYPE4                   VARCHAR(1),
    ITEM_TYPE5                   VARCHAR(1),
    ITEM_TYPE6                   VARCHAR(1),
    ITEM_TYPE7                   VARCHAR(1),
    ITEM_TYPE8                   VARCHAR(1),
    ITEM_TYPE9                   VARCHAR(1),
    ITEM_TYPE10                  VARCHAR(1),
    PRIVATE_TYPE                 VARCHAR(20) DEFAULT '00',
    DISPLAY_FLAG                 VARCHAR(1) NOT NULL DEFAULT 'Y',
    MINOR_ALLOW_FLAG             VARCHAR(1) NOT NULL DEFAULT 'Y',
    ORIGIN_COUNTRY               VARCHAR(50),
    MANUFACTURER                 VARCHAR(50),
    BRAND_ID                     INTEGER,
    BRAND                        VARCHAR(50),
    COLOR                        VARCHAR(20),
    WEIGHT                       VARCHAR(20),
    NONMEMBER_ORDER_TYPE         VARCHAR(1),
    TAX_TYPE                     VARCHAR(1) DEFAULT '2',
    COMMISSION_TYPE              VARCHAR(1) DEFAULT '1',
    COMMISSION_RATE              DOUBLE PRECISION DEFAULT 0.000000,
    PRICE_CRITERIA               VARCHAR(1) DEFAULT '0',
    ITEM_PRICE                   VARCHAR(20),
    COST_PRICE                   INTEGER DEFAULT 0,
    SUPPLY_PRICE                 INTEGER DEFAULT 0,
    SALE_PRICE                   INTEGER NOT NULL DEFAULT 0,
    SALE_PRICE_NONMEMBER_FLAG    VARCHAR(1) NOT NULL DEFAULT '0',
    SALE_PRICE_NONMEMBER         INTEGER,
    SPOT_FLAG                    VARCHAR(1) DEFAULT 'N',
    SPOT_DATE_TYPE               VARCHAR(1),
    SPOT_TYPE                    VARCHAR(1),
    SPOT_APPLY_GROUP             VARCHAR(20),
    SPOT_DISCOUNT_AMOUNT         INTEGER DEFAULT 0,
    SPOT_START_DATE              VARCHAR(8),
    SPOT_END_DATE                VARCHAR(8),
    SPOT_START_TIME              VARCHAR(6),
    SPOT_END_TIME                VARCHAR(6),
    SPOT_WEEK_DAY                VARCHAR(7),
    SALE_POINT                   INTEGER DEFAULT 0,
    SELLER_DISCOUNT_FLAG         VARCHAR(1),
    SELLER_DISCOUNT_TYPE         VARCHAR(1),
    SELLER_DISCOUNT_AMOUNT       INTEGER DEFAULT 0,
    SELLER_POINT_FLAG            VARCHAR(1) DEFAULT 'N',
    SOLD_OUT                     VARCHAR(1) NOT NULL,
    STOCK_FLAG                   VARCHAR(1) NOT NULL DEFAULT 'N',
    STOCK_QUANTITY               INTEGER DEFAULT -1,
    STOCK_CODE                   VARCHAR(50),
    STOCK_SCHEDULE_AUTO_FLAG     VARCHAR(1),
    STOCK_SCHEDULE_TYPE          VARCHAR(10),
    STOCK_SCHEDULE_DATE          VARCHAR(20),
    STOCK_SCHEDULE_TEXT          VARCHAR(40),
    ORDER_MIN_QUANTITY           INTEGER DEFAULT -1,
    ORDER_MAX_QUANTITY           INTEGER DEFAULT -1,
    SALE_QUANTITY                INTEGER NOT NULL DEFAULT 0,
    ITEM_OPTION_FLAG             VARCHAR(1),
    ITEM_OPTION_TYPE             VARCHAR(2),
    ITEM_OPTION_TITLE1           VARCHAR(30),
    ITEM_OPTION_TITLE2           VARCHAR(30),
    ITEM_OPTION_TITLE3           VARCHAR(30),
    ITEM_ADDITION_FLAG           VARCHAR(1),
    FREE_GIFT_FLAG               VARCHAR(1),
    FREE_GIFT_NAME               VARCHAR(255),
    ITEM_KEYWORD                 VARCHAR(300),
    DETAIL_CONTENT               VARCHAR,
    DETAIL_CONTENT_MOBILE        VARCHAR,
    ITEM_NOTICE_CODE             VARCHAR(50),
    ITEM_IMAGE                   VARCHAR(255),
    TEAM                         VARCHAR(30),
    OPENTIME                     VARCHAR(14),
    BASE_ITEM                    TEXT,
    OTHER_FLAG                   VARCHAR(1),
    RECOMMEND_FLAG               VARCHAR(1),
    RELATION_ITEM_DISPLAY_TYPE   VARCHAR(1),
    DELIVERY_COMPANY_ID          INTEGER,
    DELIVERY_COMPANY_NAME        VARCHAR(30),
    DELIVERY_TYPE                VARCHAR(1),
    SHIPMENT_ID                  INTEGER NOT NULL,
    SHIPMENT_GROUP_CODE          VARCHAR(50),
    SHIPMENT_RETURN_TYPE         VARCHAR(1),
    SHIPMENT_RETURN_ID           INTEGER NOT NULL,
    SHIPPING_TYPE                VARCHAR(1) NOT NULL,
    SHIPPING_GROUP_CODE          VARCHAR(20) NOT NULL,
    SHIPPING                     INTEGER,
    SHIPPING_FREE_AMOUNT         INTEGER,
    SHIPPING_ITEM_COUNT          INTEGER DEFAULT 1,
    SHIPPING_EXTRA_CHARGE1       INTEGER,
    SHIPPING_EXTRA_CHARGE2       INTEGER,
    SHIPPING_RETURN              INTEGER,
    ITEM_RETURN_FLAG             VARCHAR(1) DEFAULT 'Y',
    HITS                         INTEGER NOT NULL DEFAULT 0,
    SEO_TITLE                    VARCHAR(255),
    SEO_INDEX_FLAG               VARCHAR(1),
    SEO_KEYWORDS                 VARCHAR(255),
    SEO_DESCRIPTION              VARCHAR(255),
    SEO_HEADER_CONTENTS1         VARCHAR(255),
    SEO_THEMAWORD_TITLE          VARCHAR(255),
    SEO_THEMAWORD_DESCRIPTION    TEXT,
    MD_ID                        VARCHAR(11),
    MD_NAME                      VARCHAR(50),
    COUPON_USE_FLAG              VARCHAR(1) DEFAULT 'Y',
    DATA_STATUS_MESSAGE          TEXT,
    DATA_STATUS_CODE             VARCHAR(2) NOT NULL DEFAULT '1',
    UPDATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    UPDATED_DATE                 VARCHAR(14),
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    CREATED_DATE                 VARCHAR(14),
    FRONT_DISPLAY_FLAG           VARCHAR(1) DEFAULT 'Y',
    ERP_EXCEPTION_TYPE           VARCHAR(1),
    ITEM_NUMBER                  VARCHAR(255),
    ITEM_SALE_STATUS_TEXT        VARCHAR(255),
    NOINDEX_YN                   VARCHAR(255),
    NAVER_SHOPPING_ITEM_NAME     VARCHAR(100),
    NAVER_SHOPPING_FLAG          VARCHAR(1) DEFAULT 'N',
    NAVER_PAY_FLAG               VARCHAR(1) DEFAULT 'Y',
    SET_DISCOUNT_TYPE            VARCHAR(1),
    SET_DISCOUNT_AMOUNT          INTEGER,
    REPRESENTATIVE_ITEM_YN       VARCHAR(1) DEFAULT 'N',
    ADULT_ITEM_YN                VARCHAR(1) DEFAULT 'N',
    MOBILE_ITEM_YN               VARCHAR(1) DEFAULT 'N'
);

-- 상품분류
CREATE TABLE IF NOT EXISTS OP_ITEM_CATEGORY (
    -- 상품분류ID
    ITEM_CATEGORY_ID             INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 카테고리ID
    CATEGORY_ID                  INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_CATEGORY_ID)
);

-- 답례품 필터
CREATE TABLE IF NOT EXISTS OP_ITEM_FILTER (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일시
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 필터코드ID
    FILTER_CODE_ID               BIGINT NOT NULL,
    -- 필터그룹ID
    FILTER_GROUP_ID              BIGINT NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 순서
    ORDERING                     INTEGER,
    PRIMARY KEY (ID)
);

-- 상품 조회 수
CREATE TABLE IF NOT EXISTS OP_ITEM_HIT (
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 조회수
    HITS                         INTEGER
);

-- 상품상세이미지
CREATE TABLE IF NOT EXISTS OP_ITEM_IMAGE (
    -- 상품이미지ID
    ITEM_IMAGE_ID                INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 이미지명
    IMAGE_NAME                   VARCHAR(255) NOT NULL,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_IMAGE_ID)
);

-- 상품상세이미지
CREATE TABLE IF NOT EXISTS OP_ITEM_INFO (
    -- 상품정보ID
    ITEM_INFO_ID                 INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품정보코드 (7esthe 사용안함)
    INFO_CODE                    VARCHAR(255) NOT NULL,
    -- 상품정보항목
    TITLE                        VARCHAR(255) NOT NULL,
    -- 상품정보내용목
    DESCRIPTION                  VARCHAR(1000),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_INFO_ID)
);

-- 상품정보(모바일)
CREATE TABLE IF NOT EXISTS OP_ITEM_INFO_MOBILE (
    -- 상품정보ID
    ITEM_INFO_ID                 INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품정보코드 (7esthe 사용안함)
    INFO_CODE                    VARCHAR(255) NOT NULL,
    -- 상품정보항목
    TITLE                        VARCHAR(255) NOT NULL,
    -- 상품정보내용목
    DESCRIPTION                  VARCHAR(1000),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_INFO_ID)
);

-- 상품정보 변경로그
CREATE TABLE IF NOT EXISTS OP_ITEM_LOG (
    -- 상품ID
    ITEM_LOG_ID                  INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품코드
    ITEM_USER_CODE               VARCHAR(30) NOT NULL DEFAULT '0',
    -- 상품명
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    -- 공개여부 (Y:공개, N:비공개)
    DISPLAY_FLAG                 VARCHAR(1) NOT NULL DEFAULT 'Y',
    -- 품절 여부 (0:정상, 1: 품절)
    SOLD_OUT                     VARCHAR(1) NOT NULL,
    -- 원가
    COST_PRICE                   INTEGER,
    -- 정가
    ITEM_PRICE                   VARCHAR(20),
    -- 판매가격
    SALE_PRICE                   INTEGER NOT NULL,
    -- 수수료설정 (1:입점업체 수수료로 설정, 2: 상품별 수수료로 설정)
    COMMISSION_TYPE              VARCHAR(1),
    -- 수수료율
    COMMISSION_RATE              DOUBLE PRECISION,
    -- 가격 입력 기준 (1: 공급가기준-수수료자동입력, 2: 수수료기준-공급가 자동입력)
    PRICE_CRITERIA               VARCHAR(1),
    -- 수정위치 (
    PROCESS_PAGE                 VARCHAR(10),
    -- 등록자ID - 관리자
    CREATED_MANAGER_ID           INTEGER DEFAULT 0,
    -- 판매자ID
    CREATED_SELLER_ID            BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 처리 구분 (
    ACTION_TYPE                  VARCHAR(30),
    PRIMARY KEY (ITEM_LOG_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_item_log_item_log_id_seq START WITH 1000;
ALTER TABLE OP_ITEM_LOG ALTER COLUMN ITEM_LOG_ID SET DEFAULT nextval('op_item_log_item_log_id_seq');
ALTER SEQUENCE op_item_log_item_log_id_seq OWNED BY OP_ITEM_LOG.ITEM_LOG_ID;

-- 마켓 상품 코드
CREATE TABLE IF NOT EXISTS OP_ITEM_MALL_CODE (
    -- 상품 ID
    ITEM_ID                      INTEGER,
    -- 오픈마켓 ID
    MALL_CONFIG_ID               INTEGER,
    -- 오픈마켓 상품코드
    PRODUCT_CODE                 VARCHAR(50)
);

-- 상품 정보 고시 항목
CREATE TABLE IF NOT EXISTS OP_ITEM_NOTICE (
    -- 상품고시코드
    ITEM_NOTICE_CODE             VARCHAR(3) NOT NULL,
    -- 상품고시유형명
    ITEM_NOTICE_TITLE            VARCHAR(50) NOT NULL,
    -- 상품고시항목명
    NOTICE_TITLE                 VARCHAR(50) NOT NULL DEFAULT '',
    -- 상품고시항목 입력방법
    NOTICE_DESCRIPTION           VARCHAR(255),
    -- 노출순서
    ORDERING                     INTEGER,
    PRIMARY KEY (ITEM_NOTICE_CODE, NOTICE_TITLE)
);

-- 상품주문옵션
CREATE TABLE IF NOT EXISTS OP_ITEM_OPTION (
    -- 상품옵션ID
    ITEM_OPTION_ID               INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 선택정보타입 (S:선택형, S2:조합형, S3:3개조합형, T:텍스트형)
    OPTION_TYPE                  VARCHAR(2) NOT NULL,
    -- 옵션 출력 방법 (select, radio) - select 고정.
    OPTION_DISPLAY_TYPE          VARCHAR(10),
    -- 옵션 숨김 (Y:표시, N:숨김) - 사용안함
    OPTION_HIDE_FLAG             VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 옵션명1
    OPTION_NAME1                 VARCHAR(255) NOT NULL,
    -- 옵션명2
    OPTION_NAME2                 VARCHAR(255),
    -- 옵션명3
    OPTION_NAME3                 VARCHAR(255),
    -- 추가금액
    OPTION_PRICE                 INTEGER,
    -- 원가
    OPTION_COST_PRICE            INTEGER,
    -- 추가금액(비회원) - 사용안함
    OPTION_PRICE_NONMEMBER       INTEGER,
    -- 재고연동여부 (Y: 연동, N:연동안함)
    OPTION_STOCK_FLAG            VARCHAR(1) DEFAULT 'N',
    -- 재고수량
    OPTION_STOCK_QUANTITY        INTEGER DEFAULT 0,
    -- 관리코드 (재고관리코드)
    OPTION_STOCK_CODE            VARCHAR(30),
    -- 재입고 예정일 - 사용안함.
    OPTION_STOCK_SCHEDULE_DATE   VARCHAR(50) DEFAULT '0',
    -- 재입고 안내문구 - 사용안함.
    OPTION_STOCK_SCHEDULE_TEXT   VARCHAR(50) DEFAULT '0',
    -- 품절여부 (Y:품절, N:판메)
    OPTION_SOLD_OUT_FLAG         VARCHAR(40) DEFAULT 'N',
    -- 노출여부 (Y:노출, N:숨김)
    OPTION_DISPLAY_FLAG          VARCHAR(1) DEFAULT 'Y',
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_OPTION_ID)
);
-- SFR-005 재검토 라운드 - 배치스캔 원본엔 시퀀스가 안 붙어있어(다른 유사 테이블과 동일한
-- 관례, OP_ITEM_ORDERING 참고) 직접 추가한다. 답례품 옵션(사이즈/색상 등 다중옵션) 카탈로그
-- 관리 기능이 이 테이블 하나만 실제 코드로 매핑돼 있었던 gap을 닫는다.
CREATE SEQUENCE IF NOT EXISTS op_item_option_item_option_id_seq START WITH 1000;
ALTER TABLE OP_ITEM_OPTION ALTER COLUMN ITEM_OPTION_ID SET DEFAULT nextval('op_item_option_item_option_id_seq');
ALTER SEQUENCE op_item_option_item_option_id_seq OWNED BY OP_ITEM_OPTION.ITEM_OPTION_ID;

-- 상품옵션이미지
CREATE TABLE IF NOT EXISTS OP_ITEM_OPTION_IMAGE (
    -- 상품옵션이미지 ID
    ITEM_OPTION_IMAGE_ID         INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품옵션ID
    ITEM_OPTION_ID               INTEGER NOT NULL DEFAULT 0,
    -- 옵션명
    OPTION_NAME                  VARCHAR(255) NOT NULL,
    -- 옵션이미지명
    OPTION_IMAGE                 VARCHAR(255),
    PRIMARY KEY (ITEM_OPTION_IMAGE_ID)
);

-- 상품 옵션이 품절인 상품 정보 (배치로 자동등록)
CREATE TABLE IF NOT EXISTS OP_ITEM_OPTION_SOLDOUT (
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 옵션 품절 여부
    ITEM_OPTION_SOLD_OUT_FLAG    VARCHAR(1),
    PRIMARY KEY (ITEM_ID)
);

-- 상품노출순서
CREATE TABLE IF NOT EXISTS OP_ITEM_ORDERING (
    -- 상품분류ID
    ITEM_ORDERING_ID             INTEGER NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품카테고리ID
    CATEGORY_ID                  INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_ORDERING_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_item_ordering_item_ordering_id_seq START WITH 1000;
ALTER TABLE OP_ITEM_ORDERING ALTER COLUMN ITEM_ORDERING_ID SET DEFAULT nextval('op_item_ordering_item_ordering_id_seq');
ALTER SEQUENCE op_item_ordering_item_ordering_id_seq OWNED BY OP_ITEM_ORDERING.ITEM_ORDERING_ID;

-- 동시구매상품
CREATE TABLE IF NOT EXISTS OP_ITEM_OTHER (
    -- 연관상품ID
    ITEM_OTHER_ID                INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 주문상품ID
    OTHER_ITEM_ID                INTEGER NOT NULL DEFAULT 0,
    -- 카운팅
    COUNTING                     INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (ITEM_OTHER_ID)
);

-- 관련상품
CREATE TABLE IF NOT EXISTS OP_ITEM_RELATION (
    -- 관련상품ID
    ITEM_RELATION_ID             INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 삼품ID
    RELATED_ITEM_ID              INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_RELATION_ID)
);

-- 상품리뷰
CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW (
    -- 상품평ID
    ITEM_REVIEW_ID               BIGINT NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 주문번호
    ORDER_CODE                   VARCHAR(50) NOT NULL DEFAULT '0',
    -- 제목
    SUBJECT                      VARCHAR(255) NOT NULL,
    -- 내용
    CONTENT                      VARCHAR NOT NULL,
    -- 평가점수
    SCORE                        INTEGER NOT NULL DEFAULT 0,
    -- 상품평 채택여부 (Y:채택, N:채택안됨)
    RECOMMEND_FLAG               VARCHAR(1),
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 작성자명
    USER_NAME                    VARCHAR(350),
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 공개여부 (Y:공개, N:비공개)
    DISPLAY_FLAG                 VARCHAR(1),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 판매자ID
    POINT_PAYMENT                VARCHAR(1) DEFAULT 'N',
    -- 포인트 지급유무(Y:지급, N:미지급)
    POINT                        INTEGER DEFAULT 0,
    -- 포인트
    POINT_PAYMENT_DATE           VARCHAR(14),
    -- 데이터상태코드 0 : 정상 1: 삭제
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '0',
    -- 상품옵션 노출 여부 (Y/N)
    DISPLAY_OPTIONS_FLAG         VARCHAR(1) DEFAULT 'N',
    -- 상품 구성 옵션(JSON TYPE)
    OPTIONS                      TEXT,
    -- 관리자 댓글
    ADMIN_COMMENT                VARCHAR(2000),
    -- 도움됐어요 수
    LIKE_COUNT                   INTEGER DEFAULT 0,
    -- 리뷰답변자로그인ID
    ANSWER_LOGIN_ID              VARCHAR(300),
    -- 리뷰답변일
    ANSWER_DATE                  VARCHAR(14),
    PRIMARY KEY (ITEM_REVIEW_ID)
);

-- 답례품 리뷰 필터
CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW_FILTER (
    ID                           BIGINT NOT NULL,
    -- 등록일시
    CREATED                      TIMESTAMP,
    -- 등록자
    CREATED_BY                   BIGINT,
    -- 수정일시
    UPDATED                      TIMESTAMP,
    -- 수정자
    UPDATED_BY                   BIGINT,
    -- 필터코드ID
    FILTER_CODE_ID               BIGINT NOT NULL,
    -- 필터그룹ID
    FILTER_GROUP_ID              BIGINT NOT NULL,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 상품리뷰ID
    ITEM_REVIEW_ID               INTEGER NOT NULL,
    -- 순서
    ORDERING                     INTEGER,
    PRIMARY KEY (ID)
);

-- 상품 리뷰 이미지
CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW_IMAGE (
    -- 상품후기 이미지 ID
    ITEM_REVIEW_IMAGE_ID         BIGINT NOT NULL,
    -- 상품후기 ID
    ITEM_REVIEW_ID               BIGINT,
    -- 상품후기 이미지
    REVIEW_IMAGE                 VARCHAR(1000),
    -- 노출순서
    ORDERING                     INTEGER,
    -- 생성일자
    CREATED_DATE                 VARCHAR(50),
    PRIMARY KEY (ITEM_REVIEW_IMAGE_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_item_review_image_item_review_image_id_seq START WITH 1000;
ALTER TABLE OP_ITEM_REVIEW_IMAGE ALTER COLUMN ITEM_REVIEW_IMAGE_ID SET DEFAULT nextval('op_item_review_image_item_review_image_id_seq');
ALTER SEQUENCE op_item_review_image_item_review_image_id_seq OWNED BY OP_ITEM_REVIEW_IMAGE.ITEM_REVIEW_IMAGE_ID;

CREATE TABLE IF NOT EXISTS OP_ITEM_REVIEW_LIKE (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    IP                           VARCHAR(20),
    ITEM_REVIEW_ID               INTEGER NOT NULL,
    USER_ID                      BIGINT,
    PRIMARY KEY (ID)
);

-- 판매자 가격 변경 요청
CREATE TABLE IF NOT EXISTS OP_ITEM_SALE_EDIT (
    -- 가격변경요청ID
    ITEM_SALE_EDIT_ID            INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 판매자명
    SELLER_NAME                  VARCHAR(50) NOT NULL,
    -- 상품코드
    ITEM_CODE                    VARCHAR(30) NOT NULL,
    -- 상품명
    ITEM_NAME                    VARCHAR(200) NOT NULL,
    -- 판매가
    SALE_PRICE                   INTEGER NOT NULL DEFAULT 0,
    -- 정가
    ITEM_PRICE                   VARCHAR(20),
    -- 원가
    COST_PRICE                   INTEGER NOT NULL DEFAULT 0,
    -- (0:승인대기, 1:승인완료, 2:승인거절)
    STATUS                       VARCHAR(1) NOT NULL DEFAULT '0',
    -- 메시지
    MESSAGE                      TEXT,
    -- 생성일
    CREATED_DATE                 VARCHAR(14),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_SALE_EDIT_ID)
);

-- 세트 상품 매핑 테이블
CREATE TABLE IF NOT EXISTS OP_ITEM_SET (
    -- 세트상품 매핑 ID
    ITEM_SET_ID                  BIGINT NOT NULL,
    -- 세트상품 ID
    PARENT_ITEM_ID               INTEGER NOT NULL DEFAULT 0,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 구매수량
    QUANTITY                     INTEGER DEFAULT 1,
    -- 노출순서
    ORDERING                     INTEGER DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    PRIMARY KEY (ITEM_SET_ID)
);
CREATE SEQUENCE IF NOT EXISTS op_item_set_item_set_id_seq START WITH 1000;
ALTER TABLE OP_ITEM_SET ALTER COLUMN ITEM_SET_ID SET DEFAULT nextval('op_item_set_item_set_id_seq');
ALTER SEQUENCE op_item_set_item_set_id_seq OWNED BY OP_ITEM_SET.ITEM_SET_ID;

-- 검색어 자동완성
CREATE TABLE IF NOT EXISTS OP_KEYWORD (
    -- 키워드
    KEYWORD                      VARCHAR(255) NOT NULL,
    -- 검색어 구분(1:자동생성, 2:고객검색)
    KEYWORD_TYPE                 VARCHAR(1) NOT NULL DEFAULT '1',
    -- 등록일/검색일
    CREATED_DATE                 VARCHAR(14) NOT NULL DEFAULT '',
    -- 검색어 자소 분리
    KEYWORD_SEPERATION           VARCHAR(255) NOT NULL DEFAULT '',
    -- 가중치
    WEIGHT                       INTEGER DEFAULT 0,
    PRIMARY KEY (KEYWORD, KEYWORD_TYPE, CREATED_DATE)
);

-- 메인에 노출되는 상품을 TEMPLATE_ID 구분으로 관리함
CREATE TABLE IF NOT EXISTS OP_MAIN_DISPLAY_ITEM (
    -- 템플릿 ID
    TEMPLATE_ID                  VARCHAR(40) NOT NULL,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 전시 순서
    DISPLAY_ORDER                INTEGER NOT NULL,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL
);

-- 오픈마켓 연동설정
CREATE TABLE IF NOT EXISTS OP_MALL_CONFIG (
    -- 설정ID
    MALL_CONFIG_ID               INTEGER NOT NULL,
    -- 오픈마켓 로그인 ID
    MALL_LOGIN_ID                VARCHAR(50) NOT NULL,
    -- 오픈마켓 구분
    MALL_TYPE                    VARCHAR(50) NOT NULL,
    -- 오픈마켓 API KEY
    MALL_API_KEY                 VARCHAR(255) NOT NULL,
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL,
    -- 수집 결과 (1: 정상, 2 : 비정상, 3 : 수집중)
    STATUS_CODE                  VARCHAR(1),
    -- 최종 수집일자
    LAST_DATE                    VARCHAR(14),
    -- 최종 수집시 검색 시작일
    LAST_SEARCH_START_DATE       VARCHAR(14),
    -- 최종 수집시 검색 종료일
    LAST_SEARCH_END_DATE         VARCHAR(14),
    -- 클레임 수집 결과 (1: 정상, 2 : 비정상, 3 : 수집중)
    CLAIM_STATUS_CODE            VARCHAR(1),
    -- 클레임 일자
    LAST_CLAIM_DATE              VARCHAR(14),
    -- 클레임 최종 수집 검색 시작일
    LAST_CLAIM_SEARCH_START_DATE VARCHAR(14),
    -- 클레임 최종 수집 검색 종료일
    LAST_CLAIM_SEARCH_END_DATE   VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (MALL_CONFIG_ID)
);

-- 오픈마켓 주문 데이터
CREATE TABLE IF NOT EXISTS OP_MALL_ORDER (
    -- 주문ID
    MALL_ORDER_ID                INTEGER NOT NULL,
    -- 상품번호
    ITEM_ID                      INTEGER,
    -- 매칭된 옵션번호 (ex 1^^^2)
    MATCHED_OPTIONS              VARCHAR(255),
    -- 설정ID
    MALL_CONFIG_ID               INTEGER NOT NULL,
    -- 오픈마켓 타입
    MALL_TYPE                    VARCHAR(50) NOT NULL,
    -- 판매 상태
    ORDER_ITEM_STATUS            VARCHAR(10) NOT NULL,
    -- 판매 상태 라벨
    ORDER_ITEM_STATUS_LABEL      VARCHAR(50),
    -- 오픈마켓 주문번호
    ORDER_CODE                   VARCHAR(100) NOT NULL,
    -- 오픈마켓 주문 순번
    ORDER_INDEX                  INTEGER NOT NULL,
    -- 묶음배송 코드
    SHIPPING_GROUP_CODE          VARCHAR(50) NOT NULL,
    -- 묶음배송 여부 (Y:묶음, N:개별)
    SHIPPING_GROUP_FLAG          VARCHAR(1) NOT NULL,
    -- 추가구성품 여부
    ADDITION_ITEM_FLAG           VARCHAR(1) NOT NULL,
    -- 추가구성품일때 원상품 번호
    PARENT_PRODUCT_CODE          INTEGER NOT NULL,
    -- 오픈마켓 배송코드
    SHIPPING_CODE                VARCHAR(50) NOT NULL,
    -- 오픈마켓 상품번호
    PRODUCT_CODE                 VARCHAR(50) NOT NULL,
    -- 오픈마켓 옵션코드
    OPTION_CODE                  VARCHAR(100) NOT NULL,
    -- 오픈마켓 상품명
    PRODUCT_NAME                 VARCHAR(255) NOT NULL,
    -- 오픈마켓 옵션명
    OPTION_NAME                  VARCHAR(255) NOT NULL,
    -- 판매가 (객단가)
    SELL_PRICE                   INTEGER NOT NULL,
    -- 구매수량
    QUANTITY                     INTEGER NOT NULL,
    -- 주문취소 수량
    CANCEL_QUANTITY              INTEGER DEFAULT 0,
    -- 판매자 할인금액
    SELLER_DISCOUNT_AMOUNT       INTEGER NOT NULL,
    -- 판매금액(총금액)
    SALE_AMOUNT                  INTEGER NOT NULL,
    -- 옵션가
    OPTION_AMOUNT                INTEGER NOT NULL,
    -- 배송비
    PAY_SHIPPING                 INTEGER NOT NULL,
    -- 배송비 착불 여부
    PAY_SHIPPING_TYPE            VARCHAR(20) NOT NULL,
    -- 도서산간 배송비
    ISLAND_PAY_SHIPPING          INTEGER NOT NULL,
    -- 도서산간 배송비 착불 여부
    ISLAND_PAY_SHIPPING_TYPE     VARCHAR(5) NOT NULL,
    -- 쇼핑몰 상품코드
    MALL_PRODUCT_CODE            VARCHAR(100) NOT NULL,
    -- 오픈마켓 회원 ID
    MEMBER_ID                    VARCHAR(40),
    -- 오픈마켓 회원 등급
    MEMBER_TYPE                  VARCHAR(40),
    -- 오픈마켓 회원 NO
    MEMBER_NO                    INTEGER DEFAULT 0,
    -- 구매자 이름
    BUYER_NAME                   VARCHAR(50) NOT NULL,
    -- 구매자 전화번호
    BUYER_TELEPHONE_NUMBER       VARCHAR(20) NOT NULL,
    -- 구매자 휴대전화 번호
    BUYER_PHONE_NUMBER           VARCHAR(20) NOT NULL,
    -- 구매자 우편번호
    BUYER_ZIPCODE                VARCHAR(7),
    -- 구매자 주소
    BUYER_ADDRESS                VARCHAR(100),
    -- 구매자 상세 주소
    BUYER_ADDRESS_DETAIL         VARCHAR(255),
    -- 받는사람 이름
    RECEIVER_NAME                VARCHAR(50) NOT NULL,
    -- 받는사람 전화번호
    RECEIVER_TELEPHONE_NUMBER    VARCHAR(20) NOT NULL,
    -- 받는사람 휴대전화 번호
    RECEIVER_PHONE_NUMBER        VARCHAR(20) NOT NULL,
    -- 받는사람 우편번호
    RECEIVER_ZIPCODE             VARCHAR(7) NOT NULL,
    -- 받는사람 주소
    RECEIVER_ADDRESS             VARCHAR(100) NOT NULL,
    -- 받는사람 상세 주소
    RECEIVER_ADDRESS_DETAIL      VARCHAR(255) NOT NULL,
    -- 배송 요청사항
    CONTENT                      VARCHAR(255) NOT NULL,
    -- 시스템 메시지
    SYSTEM_MESSAGE               VARCHAR(255),
    -- 결제완료 일시
    PAY_DATE                     VARCHAR(14) NOT NULL,
    -- 주문 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 클레임 요청 일시
    CLAIM_APPLY_DATE             VARCHAR(14),
    PRIMARY KEY (MALL_ORDER_ID)
);

-- 오픈마켓 주문취소
CREATE TABLE IF NOT EXISTS OP_MALL_ORDER_CANCEL (
    -- 주문ID
    MALL_ORDER_ID                INTEGER NOT NULL,
    -- 클레임 코드
    CLAIM_CODE                   VARCHAR(100) NOT NULL,
    -- 클레임 수량
    CLAIM_QUANTITY               INTEGER NOT NULL,
    -- 클래임 상태 (01:신청, 02:완료)
    CLAIM_STATUS                 VARCHAR(50) NOT NULL,
    -- 클레임 등록 주체 (01:구매자, 02 : 판매자)
    CLAIM_APPLY_SUBJECT          VARCHAR(10) NOT NULL,
    -- 클레임 사유 코드
    CANCEL_REASON                VARCHAR(255),
    -- 클레임 사유 상세
    CANCEL_REASON_TEXT           VARCHAR(255),
    -- 취소 거부 사유 코드
    CANCEL_REFUSAL_RESON         VARCHAR(10),
    -- 취소 거부 사유
    CANCEL_REFUSAL_RESON_TEXT    VARCHAR(255),
    -- 취소 요청일
    CANCEL_APPLY_DATE            VARCHAR(14) NOT NULL,
    PRIMARY KEY (MALL_ORDER_ID, CLAIM_CODE)
);

-- 오픈마켓 주문교환
CREATE TABLE IF NOT EXISTS OP_MALL_ORDER_EXCHANGE (
    -- 주문ID
    MALL_ORDER_ID                INTEGER NOT NULL,
    -- 클레임 번호
    CLAIM_CODE                   VARCHAR(50) NOT NULL,
    -- 클레임 수량
    CLAIM_QUANTITY               INTEGER NOT NULL,
    -- 클레임 상태
    CLAIM_STATUS                 VARCHAR(3) NOT NULL,
    -- 사유
    EX_REASON                    VARCHAR(10) NOT NULL,
    -- 사유 상세
    EX_REASON_TEXT               VARCHAR(255) NOT NULL,
    -- 수거지 이름
    EX_COLLECTION_NAME           VARCHAR(40),
    -- 수거지 전화번호
    EX_COLLECTION_TEL_NUMBER     VARCHAR(40),
    -- 수거지 휴대폰
    EX_COLLECTION_PHONE_NUMBER   VARCHAR(40),
    -- 수거지 우편번호
    EX_COLLECTION_ZIPCODE        VARCHAR(10),
    -- 수거지 우편번호 순번
    EX_COLLECTION_ZIPCODE_SEQ    VARCHAR(10),
    -- 수거지 주소
    EX_COLLECTION_ADDRESS        VARCHAR(255),
    -- 수거지 주소 상세
    EX_COLLECTION_ADDRESS_DETAIL VARCHAR(255),
    -- 수거지 주소 타입
    EX_COLLECTION_ADDRESS_TYPE   VARCHAR(5),
    -- 수거지 주소 건물번호
    EX_COLLECTION_ADDRESS_BILNO  VARCHAR(50),
    -- 교환상품 발송 방법
    EX_SHIPPING_TYPE             VARCHAR(5),
    -- 받는사람 이름
    EX_RECEIVER_NAME             VARCHAR(40) NOT NULL,
    -- 받는사람 전화번호
    EX_RECEIVER_TEL_NUMBER       VARCHAR(40) NOT NULL,
    -- 받는사람 휴대폰 번호
    EX_RECEIVER_PHONE_NUMBER     VARCHAR(40) NOT NULL,
    -- 받는사람 우편번호
    EX_RECEIVER_ZIPCODE          VARCHAR(10) NOT NULL,
    -- 받는사람 우편번호 순번
    EX_RECEIVER_ZIPCODE_SEQ      VARCHAR(10),
    -- 받는사람 주소
    EX_RECEIVER_ADDRESS          VARCHAR(255) NOT NULL,
    -- 받는사람 주소 상세
    EX_RECEIVER_ADDRESS_DETAIL   VARCHAR(255) NOT NULL,
    -- 주소 타입
    EX_RECEIVER_ADDRESS_TYPE     VARCHAR(5) NOT NULL,
    -- 건물번호
    EX_RECEIVER_ADDRESS_BILNO    VARCHAR(50),
    -- 교환 배송비
    EX_SHIPPING_AMOUNT           INTEGER,
    -- 교환 추가 배송비
    EX_ADD_SHIPPING_AMOUNT       INTEGER,
    -- 교환 배송비 결제 방식
    EX_SHIPPING_PAYMENT_TYPE     VARCHAR(5) NOT NULL,
    -- 사용자 직접 발송 - 송장번호
    EX_SHIPPING_NUMBER           VARCHAR(50),
    -- 사용자 직접 발송 - 택배사 코드
    EX_SHIPPING_COMPANY_CODE     VARCHAR(10),
    -- 교환 거부 사유
    EX_REFUSAL_REASON            VARCHAR(10),
    -- 교환 거부 사유 상세
    EX_REFUSAL_REASON_TEXT       VARCHAR(255),
    -- 교환 발송 - 택배사 코드
    RESEND_DELIVERY_COMPANY_CODE VARCHAR(10),
    -- 교환 발송 - 송장번호
    RESEND_DELIVERY_NUMBER       VARCHAR(50),
    -- 신청일
    EX_APPLY_DATE                VARCHAR(14) NOT NULL,
    -- 완료일
    EX_END_DATE                  VARCHAR(14),
    PRIMARY KEY (MALL_ORDER_ID, CLAIM_CODE)
);

-- 오픈마켓 주문반품
CREATE TABLE IF NOT EXISTS OP_MALL_ORDER_RETURN (
    -- 주문ID
    MALL_ORDER_ID                INTEGER NOT NULL,
    -- 클레임코드
    CLAIM_CODE                   VARCHAR(100) NOT NULL,
    -- 클레임 상태
    CLAIM_STATUS                 VARCHAR(20) NOT NULL,
    -- 클레임 수량
    CLAIM_QUANTITY               INTEGER NOT NULL,
    -- 클레임 사유코드(반품)
    RT_REASON                    VARCHAR(50) NOT NULL,
    -- 사유코드에 대한 상세내역
    RT_REASON_TEXT               VARCHAR(255) NOT NULL,
    -- 수거지 이름
    RT_COLLECTION_NAME           VARCHAR(40),
    -- 수거지 전화번호
    RT_COLLECTION_TEL_NUMBER     VARCHAR(40),
    -- 수거지 휴대폰번호
    RT_COLLECTION_PHONE_NUMBER   VARCHAR(40),
    -- 수거지 우편번호
    RT_COLLECTION_ZIPCODE        VARCHAR(10),
    -- 수거지 우편번호 순번
    RT_COLLECTION_ZIPCODE_SEQ    VARCHAR(10),
    -- 수거지 기본주소
    RT_COLLECTION_ADDRESS        VARCHAR(255),
    -- 수거지 상세주소
    RT_COLLECTION_ADDRESS_DETAIL VARCHAR(255),
    -- 수거지 주소 유형
    RT_COLLECTION_ADDRESS_TYPE   VARCHAR(5),
    -- 수거지 건물관리번호
    RT_COLLECTION_ADDRESS_BILNO  VARCHAR(50),
    -- 반품상품 발송방법
    RT_SHIPPING_TYPE             VARCHAR(50),
    -- 반품배송비
    RT_SHIPPING_AMOUNT           INTEGER,
    -- 11번가 지정반품 택배비
    RT_DEFAULT_SHIPPING_AMOUNT   INTEGER,
    -- 추가 배송비
    RT_ADD_SHIPPING_AMOUNT       INTEGER,
    -- 추가 배송비 부담
    RT_ADD_SHIPPING_TYPE         VARCHAR(50),
    -- 구매자 차감 배송비
    RT_DEDUCTION_SHIPPING_AMOUNT INTEGER,
    -- 결제방법
    RT_SHIPPING_PAYMENT_TYPE     VARCHAR(50),
    -- 수거송장번호
    RT_SHIPPING_NUMBER           VARCHAR(50),
    -- 수거택배사코드
    RT_SHIPPING_COMPANY_CODE     VARCHAR(10),
    -- 반품보류 사유
    RT_HOLD_REASON               VARCHAR(10),
    -- 반품보류 사유 상세
    RT_HOLD_REASON_TEXT          VARCHAR(255),
    -- 반품거절 사유
    RT_REFUSAL_REASON            VARCHAR(10),
    -- 반품거절 사유 상세
    RT_REFUSAL_REASON_TEXT       VARCHAR(255),
    -- 요청일
    RT_APPLY_DATE                VARCHAR(14) NOT NULL,
    -- 완료일
    RT_END_DATE                  VARCHAR(14),
    PRIMARY KEY (MALL_ORDER_ID, CLAIM_CODE)
);

-- 모바일 카테고리 수정
CREATE TABLE IF NOT EXISTS OP_MOBILE_CATEGORY_EDIT (
    -- 카테고리 수정 ID
    CATEGORY_EDIT_ID             INTEGER DEFAULT 0,
    -- 코드
    CODE                         VARCHAR(50),
    -- 종류 수정
    EDIT_KIND                    VARCHAR(1),
    -- 위치 수정
    EDIT_POSITION                VARCHAR(50),
    -- 내용 수정
    EDIT_CONTENT                 VARCHAR,
    -- 이미지 수정
    EDIT_IMAGE                   VARCHAR(100),
    -- url 수정
    EDIT_URL                     VARCHAR(100),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14)
);

-- 상품랭킹
CREATE TABLE IF NOT EXISTS OP_RANKING (
    -- 상품랭킹ID
    RANKING_ID                   INTEGER NOT NULL DEFAULT 0,
    -- 상품분류코드
    CATEGORY_URL                 VARCHAR(50) NOT NULL DEFAULT '0',
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 노출순서
    ORDERING                     INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (RANKING_ID)
);

-- 배치로 추출된 랭킹
CREATE TABLE IF NOT EXISTS OP_RANKING_BATCH (
    -- 랭킹 타입 (1: TOP_100 2: GROUP MAIN 3: 카테고리 )
    RANKING_TYPE                 VARCHAR(2) NOT NULL,
    -- 타입별 코드
    RANKING_CODE                 VARCHAR(100) NOT NULL,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 노출 순서 (ASC)
    ORDERING                     INTEGER NOT NULL,
    -- 상태 ( 0: 산정준비 1: 산정) 
    DATA_STATUS_CODE             VARCHAR(1) NOT NULL DEFAULT '0'
);

-- 상품 랭킹 설정
CREATE TABLE IF NOT EXISTS OP_RANKING_CONFIG (
    -- 랭킹 산정 조건 CODE
    RANK_CONFIG_CODE             VARCHAR(50) NOT NULL,
    -- 판매금액 기준 산정 기준 일수
    SALE_PRICE_DAYS              INTEGER NOT NULL DEFAULT 0,
    -- 판매금액 기준 산정 가중치
    SALE_PRICE_WEIGHT            INTEGER NOT NULL DEFAULT 0,
    -- 판매수량 기준 산정 기준 일수
    SALE_COUNT_DAYS              INTEGER NOT NULL DEFAULT 0,
    -- 판매수량 기준 산정 가중치
    SALE_COUNT_WEIGHT            INTEGER NOT NULL DEFAULT 0,
    -- 상품리뷰 기준 산정 기준 일수
    ITEM_REVIEW_DAYS             INTEGER NOT NULL DEFAULT 0,
    -- 상품리뷰 기준 산정 가중치
    ITEM_REVIEW_WEIGHT           INTEGER NOT NULL DEFAULT 0,
    -- 상품조회수 기준 산정 가중치
    ITEM_HIT_WEIGHT              INTEGER NOT NULL DEFAULT 0,
    -- 생성일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (RANK_CONFIG_CODE)
);

-- 재입고알림 신청 리스트
CREATE TABLE IF NOT EXISTS OP_RESTOCK_NOTICE (
    -- 재입고알림 신청 ID
    RESTOCK_NOTICE_ID            BIGINT NOT NULL,
    -- 상품 ID
    ITEM_ID                      INTEGER NOT NULL,
    -- 회원 ID
    USER_ID                      BIGINT NOT NULL,
    -- 발송 여부 (Y:발송, N:미발송)
    SEND_FLAG                    VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    PRIMARY KEY (RESTOCK_NOTICE_ID)
);

CREATE TABLE IF NOT EXISTS OP_REVIEW_FILTER (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CATEGORY_ID                  INTEGER NOT NULL,
    FILTER_GROUP_ID              BIGINT NOT NULL,
    ORDERING                     INTEGER,
    PRIMARY KEY (ID)
);

-- 판매자 기본정보
CREATE TABLE IF NOT EXISTS OP_SELLER (
    -- 판매자ID
    SELLER_ID                    BIGINT NOT NULL DEFAULT 0,
    -- 판매자명
    SELLER_NAME                  VARCHAR(300),
    -- 담당MD 이름
    LOGIN_ID                     VARCHAR(300) NOT NULL,
    -- 로그인 비밀번호
    PASSWORD                     VARCHAR(300),
    -- 담당자명
    USER_NAME                    VARCHAR(350),
    -- 담당자 전화번호
    TELEPHONE_NUMBER             VARCHAR(210),
    -- 담당자 휴대폰번호
    PHONE_NUMBER                 VARCHAR(210),
    -- 팩스번호
    FAX_NUMBER                   VARCHAR(210),
    -- 담당자 이메일
    EMAIL                        VARCHAR(420),
    -- 우편번호
    POST                         VARCHAR(7),
    -- 주소
    ADDRESS                      VARCHAR(1000),
    -- 상세주소
    ADDRESS_DETAIL               VARCHAR(1785),
    -- 주문담당자명
    SECOND_USER_NAME             VARCHAR(350),
    -- 주문담당자 전화번호
    SECOND_TELEPHONE_NUMBER      VARCHAR(210),
    -- 주문담당자 휴대폰 번호
    SECOND_PHONE_NUMBER          VARCHAR(210),
    -- 주문담당자 이메일
    SECOND_EMAIL                 VARCHAR(420),
    -- 상호
    COMPANY_NAME                 VARCHAR(50),
    -- 대표자명
    REPRESENTATIVE_NAME          VARCHAR(350),
    -- 사업자번호
    BUSINESS_NUMBER              VARCHAR(12),
    -- 사업장소재지
    BUSINESS_LOCATION            VARCHAR(255),
    -- 업태
    BUSINESS_TYPE                VARCHAR(25),
    -- 종목
    BUSINESS_ITEMS               VARCHAR(25),
    -- 수수료율
    COMMISSION_RATE              DOUBLE PRECISION,
    -- 정산주기 (1: 일정산, 2: 주정산, 3:15일정산, 4:월정산)
    REMITTANCE_TYPE              VARCHAR(1),
    -- 정산일 (월 정산일 경우 정산일자) 
    REMITTANCE_DAY               VARCHAR(2),
    -- 정산- 입급은행명
    BANK_NAME                    VARCHAR(30),
    -- 정상- 계좌주명
    BANK_IN_NAME                 VARCHAR(350),
    -- 정산-입급계좌번호
    BANK_ACCOUNT_NUMBER          VARCHAR(350),
    -- 판매자 조건부 배송비 설정 여부 (Y: 설정, N: 미설정)
    SHIPPING_FLAG                VARCHAR(50) DEFAULT 'N',
    -- 판매자 조건부 배송비
    SHIPPING                     INTEGER,
    -- 판매자 조건부 배송비 무료배송 금액
    SHIPPING_FREE_AMOUNT         INTEGER,
    -- 추가배송비 - 제주도
    SHIPPING_EXTRA_CHARGE1       INTEGER,
    -- 추가배송비 - 도서산간
    SHIPPING_EXTRA_CHARGE2       INTEGER,
    -- 미니몰 상단 컨텐츠.
    HEADER_CONTENT               VARCHAR,
    -- 상품 승인 타입 (1:운영자승인, 2:자동승인)
    ITEM_APPROVAL_TYPE           VARCHAR(1) DEFAULT '1',
    -- 주문안내 SMS 발송시간 (
    SMS_SEND_TIME                VARCHAR(2),
    -- SMS 발송여부 - MD (Y:발송, N: 발송안함)
    SMS_MD_FLAG                  VARCHAR(1) DEFAULT 'N',
    -- 담당MD ID
    MD_ID                        INTEGER,
    -- 담당MD 이름
    MD_NAME                      VARCHAR(1000),
    STATUS_CODE                  VARCHAR(1),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 회원ID
    CREATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 회원ID
    UPDATED_USER_ID              BIGINT NOT NULL DEFAULT 0,
    -- 통신판매업신고번호
    MAIL_ORDER_NUMBER            VARCHAR(30),
    -- 구매안전 이용확인번호
    BUY_SAFETY_USE_CONFIRM_NUMBER VARCHAR(30),
    -- 과세구분 (1:과세, 2:비과세)
    TAX_TYPE                     VARCHAR(1) DEFAULT '1',
    -- 지차체 코드
    LOCGOV_CODE                  VARCHAR(10),
    -- 사업자등록증(파일명)
    FILE_NAME_CERTIFICATE1       VARCHAR(255),
    -- 통신판매신고증(파일명)
    FILE_NAME_CERTIFICATE2       VARCHAR(255),
    -- 구매안전이용확인증(파일명)
    FILE_NAME_CERTIFICATE3       VARCHAR(255),
    -- 성인상품등록가능여부
    ADULT_ITEM_YN                VARCHAR(1) DEFAULT 'N',
    COMMUNITY_BUSINESS_YN        VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (SELLER_ID)
);

-- 관리자정보_제공자
CREATE TABLE IF NOT EXISTS OP_SELLER_USER (
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 로그인ID
    LOGIN_ID                     VARCHAR(300),
    -- 비밀번호
    PASSWORD                     VARCHAR(300),
    -- 이름
    USER_NAME                    VARCHAR(350),
    -- 이메일
    EMAIL                        VARCHAR(420),
    -- 휴대폰번호
    PHONE_NUMBER                 VARCHAR(210),
    -- 상태코드(1: 가입대기, 2:차단, 3:탈퇴, 4:휴면계정, 5:휴면대기 6:탈퇴대기, 9:정상)
    STATUS_CODE                  BIGINT,
    -- 로그인 회수
    LOGIN_COUNT                  BIGINT,
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
    -- 패스워드 타입 (N:정상 T:임시)
    PASSWORD_TYPE                VARCHAR(1) NOT NULL DEFAULT 'N',
    -- 패스워드 유효일자
    PASSWORD_EXPIRED_DATE        VARCHAR(8),
    -- 수정일
    UPDATED_DATE                 VARCHAR(14),
    -- 등록일
    CREATED_DATE                 VARCHAR(14),
    -- 지차체 코드
    LOCGOV_CODE                  VARCHAR(10)
);

-- 답례품 제공자 로그인
CREATE TABLE IF NOT EXISTS OP_SELLER_USER_LOGIN (
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

CREATE TABLE IF NOT EXISTS OP_STORE (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    ADDRESS                      VARCHAR(100) NOT NULL,
    ADDRESS_DETAIL               VARCHAR(250),
    END_TIME                     VARCHAR(4),
    NAME                         VARCHAR(50) NOT NULL,
    NEW_POST                     VARCHAR(50),
    POST                         VARCHAR(8) NOT NULL,
    SIDO                         VARCHAR(20) NOT NULL,
    START_TIME                   VARCHAR(4),
    STORE_TYPE                   VARCHAR(255),
    TEL_NUMBER                   VARCHAR(20),
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_STYLE_BOOK (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    CONTENT                      VARCHAR(1000),
    IMAGE                        VARCHAR(1000),
    ORDERING                     INTEGER NOT NULL,
    TITLE                        VARCHAR(200),
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS OP_STYLE_BOOK_ITEM (
    ID                           BIGINT NOT NULL,
    CREATED                      TIMESTAMP,
    CREATED_BY                   BIGINT,
    UPDATED                      TIMESTAMP,
    UPDATED_BY                   BIGINT,
    ITEM_ID                      INTEGER NOT NULL,
    ORDERING                     INTEGER NOT NULL,
    STYLE_BOOK_ID                BIGINT,
    PRIMARY KEY (ID)
);

-- 관심상품
CREATE TABLE IF NOT EXISTS OP_WISHLIST (
    -- 관심상품ID
    WISHLIST_ID                  INTEGER NOT NULL DEFAULT 0,
    -- 관심상품 그룹ID
    WISHLIST_GROUP_ID            INTEGER NOT NULL DEFAULT 0,
    -- 상품ID
    ITEM_ID                      INTEGER NOT NULL DEFAULT 0,
    -- 상품 옵션
    ITEM_OPTION                  VARCHAR(255),
    -- 상품 옵션 그룹명
    ITEM_OPTION_GROUP_NAME       VARCHAR(255),
    -- 상품 옵션명
    ITEM_OPTION_NAME             VARCHAR(255),
    -- 회원ID
    USER_ID                      BIGINT NOT NULL DEFAULT 0,
    -- 등록일
    CREATED_DATE                 VARCHAR(14) NOT NULL,
    -- 임시 옵션
    TEMP_OPTION                  VARCHAR(255),
    -- 상품옵션1
    ITEM_OPTION_1                VARCHAR(255),
    -- 상품옵션2
    ITEM_OPTION_2                VARCHAR(255),
    -- 상품옵션3
    ITEM_OPTION_3                VARCHAR(255),
    -- 상품옵션4
    ITEM_OPTION_4                VARCHAR(255),
    -- 상품옵션5
    ITEM_OPTION_5                VARCHAR(255),
    -- 상품옵션6
    ITEM_OPTION_6                VARCHAR(255),
    -- 상품옵션7
    ITEM_OPTION_7                VARCHAR(255),
    -- 상품옵션8
    ITEM_OPTION_8                VARCHAR(255),
    PRIMARY KEY (WISHLIST_ID)
);

-- same-service FKs, added after every table exists (avoids creation-order failures)
ALTER TABLE OP_CATEGORY_GROUP ADD CONSTRAINT fk_op_category_group_category_team_id FOREIGN KEY (CATEGORY_TEAM_ID) REFERENCES OP_CATEGORY_TEAM (CATEGORY_TEAM_ID);
ALTER TABLE OP_CATEGORY_GROUP_BANNER ADD CONSTRAINT fk_op_category_group_banner_category_group_id FOREIGN KEY (CATEGORY_GROUP_ID) REFERENCES OP_CATEGORY_GROUP (CATEGORY_GROUP_ID);
ALTER TABLE OP_FEATURED_BANNER_ITEM ADD CONSTRAINT fk_op_featured_banner_item_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_FEATURED_ITEM ADD CONSTRAINT fk_op_featured_item_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_FEATURED_REPLY ADD CONSTRAINT fk_op_featured_reply_featured_id FOREIGN KEY (FEATURED_ID) REFERENCES OP_FEATURED (FEATURED_ID);
ALTER TABLE OP_FILTER_CODE ADD CONSTRAINT fk_op_filter_code_filter_group_id FOREIGN KEY (FILTER_GROUP_ID) REFERENCES OP_FILTER_GROUP (ID);
ALTER TABLE OP_GIFT_GROUP_ITEM ADD CONSTRAINT fk_op_gift_group_item_gift_item_id FOREIGN KEY (GIFT_ITEM_ID) REFERENCES OP_GIFT_ITEM (ID);
ALTER TABLE OP_GIFT_GROUP_ITEM ADD CONSTRAINT fk_op_gift_group_item_gift_group_id FOREIGN KEY (GIFT_GROUP_ID) REFERENCES OP_GIFT_GROUP (ID);
ALTER TABLE OP_GIFT_ITEM_RELATION ADD CONSTRAINT fk_op_gift_item_relation_gift_item_id FOREIGN KEY (GIFT_ITEM_ID) REFERENCES OP_GIFT_ITEM (ID);
ALTER TABLE OP_ITEM_ADDITION ADD CONSTRAINT fk_op_item_addition_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_ADDITION ADD CONSTRAINT fk_op_item_addition_addition_item_id FOREIGN KEY (ADDITION_ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_INFO ADD CONSTRAINT fk_op_item_info_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_INFO_MOBILE ADD CONSTRAINT fk_op_item_info_mobile_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_MALL_CODE ADD CONSTRAINT fk_op_item_mall_code_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_MALL_CODE ADD CONSTRAINT fk_op_item_mall_code_mall_config_id FOREIGN KEY (MALL_CONFIG_ID) REFERENCES OP_MALL_CONFIG (MALL_CONFIG_ID);
ALTER TABLE OP_ITEM_OPTION_IMAGE ADD CONSTRAINT fk_op_item_option_image_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_ORDERING ADD CONSTRAINT fk_op_item_ordering_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_OTHER ADD CONSTRAINT fk_op_item_other_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_RELATION ADD CONSTRAINT fk_op_item_relation_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_ITEM_SET ADD CONSTRAINT fk_op_item_set_parent_item_id FOREIGN KEY (PARENT_ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_MAIN_DISPLAY_ITEM ADD CONSTRAINT fk_op_main_display_item_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_MALL_ORDER_CANCEL ADD CONSTRAINT fk_op_mall_order_cancel_mall_order_id FOREIGN KEY (MALL_ORDER_ID) REFERENCES OP_MALL_ORDER (MALL_ORDER_ID);
ALTER TABLE OP_MALL_ORDER_EXCHANGE ADD CONSTRAINT fk_op_mall_order_exchange_mall_order_id FOREIGN KEY (MALL_ORDER_ID) REFERENCES OP_MALL_ORDER (MALL_ORDER_ID);
ALTER TABLE OP_MALL_ORDER_RETURN ADD CONSTRAINT fk_op_mall_order_return_mall_order_id FOREIGN KEY (MALL_ORDER_ID) REFERENCES OP_MALL_ORDER (MALL_ORDER_ID);
ALTER TABLE OP_RESTOCK_NOTICE ADD CONSTRAINT fk_op_restock_notice_item_id FOREIGN KEY (ITEM_ID) REFERENCES OP_ITEM (ITEM_ID);
ALTER TABLE OP_STYLE_BOOK_ITEM ADD CONSTRAINT fk_op_style_book_item_style_book_id FOREIGN KEY (STYLE_BOOK_ID) REFERENCES OP_STYLE_BOOK (ID);

-- ===================================================================
-- 기존 파일에서 보존된 내용 (AS-IS에 없는 프로젝트 고유 테이블/컬럼/시퀀스/시드 데이터)
-- ===================================================================

CREATE TABLE IF NOT EXISTS MIG_OP_ITEM_IMAGE (
    ITEM_IMAGE_ID BIGINT PRIMARY KEY, -- guess: type/PK inferred from naming only, parameterType is a generic HashMap
    ITEM_ID BIGINT,
    IMAGE_NAME VARCHAR(255),
    ORDERING INTEGER,
    CREATED_DATE TIMESTAMP,
    SERIAL_NUM VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS OP_DISPLAY_SNS (
    SNS_ID                  INTEGER         PRIMARY KEY,
    SNS_TOKEN               VARCHAR(255),
    SNS_TYPE                VARCHAR(50),
    ORDERING                 INTEGER,
    UPDATED_DATE            TIMESTAMP,
    CREATED_DATE            TIMESTAMP
);

-- =====================================================================
-- Round X (마이페이지 상세화면 7종 중 "관심답례품"): PK 컬럼에 시퀀스가 없었다(레거시
-- 배치 스캔은 시퀀스까지는 못 잡아냄) - 다른 gift 테이블과 동일한 컨벤션으로 추가.
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_wishlist_wishlist_id_seq START WITH 1000;

ALTER TABLE OP_WISHLIST ALTER COLUMN WISHLIST_ID SET DEFAULT nextval('op_wishlist_wishlist_id_seq');

ALTER SEQUENCE op_wishlist_wishlist_id_seq OWNED BY OP_WISHLIST.WISHLIST_ID;

INSERT INTO OP_WISHLIST (WISHLIST_ID, WISHLIST_GROUP_ID, ITEM_ID, USER_ID, CREATED_DATE) VALUES
(1000, 1000, 1000, 1000, to_char(now(),'YYYYMMDDHH24MISS')),
(1001, 1001, 1001, 1000, to_char(now(),'YYYYMMDDHH24MISS')),
(1002, 1002, 1002, 1001, to_char(now(),'YYYYMMDDHH24MISS')),
(1003, 1003, 1003, 1001, to_char(now(),'YYYYMMDDHH24MISS')),
(1004, 1004, 1000, 1002, to_char(now(),'YYYYMMDDHH24MISS'))
ON CONFLICT (WISHLIST_ID) DO NOTHING;

-- 시퀀스가 START WITH 1000인데 위에서 명시적으로 1000~1004를 채워 넣었으므로, 다음
-- nextval() 호출이 다시 1000을 반환해 PK 충돌이 난다(실제로 겪은 버그) - 시드 최대값
-- 이후로 반드시 당겨줘야 한다.
SELECT setval('op_wishlist_wishlist_id_seq', 1004);

-- =====================================================================
-- MVP additions for the new gift microservice implementation (SFR-005).
-- OP_ITEM above (~150 columns) is a generic legacy shopping-mall product
-- table (SEO fields, Naver Shopping flags, adult-item flags, etc.) - the new
-- service reuses it (same table/PK, matching the "reuse AS-IS table, map
-- only the columns you need" approach used for OP_USER/G_CNTR in the other
-- services) rather than inventing a parallel table, but only maps/populates
-- a small subset of columns relevant to 답례품 관리. DATA_STATUS_CODE is
-- repurposed here as the 지자체 승인 workflow status (PENDING/APPROVED/
-- REJECTED/STOPPED), sourced from OP_COMMON_CODE like everywhere else.
-- OP_CATEGORY (also ~40 SEO-heavy legacy columns) is likewise not reused -
-- 행안부 표준 카테고리 is modeled as a plain OP_COMMON_CODE list instead,
-- consistent with the no-hardcoding principle already applied elsewhere.
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
('GIFT_CATEGORY', 'ko', 'AGRI',     '농축산물',   1, 'Y'),
('GIFT_CATEGORY', 'ko', 'SEAFOOD',  '수산물',     2, 'Y'),
('GIFT_CATEGORY', 'ko', 'PROCESSED','가공식품',   3, 'Y'),
('GIFT_CATEGORY', 'ko', 'LIVING',   '생활용품',   4, 'Y'),
('GIFT_CATEGORY', 'ko', 'VOUCHER',  '지역상품권', 5, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_STATUS', 'ko', 'PENDING',  '승인대기', 1, 'Y'),
('GIFT_STATUS', 'ko', 'APPROVED', '승인',     2, 'Y'),
('GIFT_STATUS', 'ko', 'REJECTED', '반려',     3, 'Y'),
('GIFT_STATUS', 'ko', 'STOPPED',  '판매중지', 4, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- New column: OP_ITEM has no single category-code column in the AS-IS schema
-- (categorization there goes through the heavyweight OP_ITEM_CATEGORY/
-- OP_CATEGORY tables this service doesn't use) - add a plain one.
ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS CATEGORY_CODE VARCHAR(50);

-- New column: same reasoning as CATEGORY_CODE above - AS-IS's real OP_ITEM has no
-- locgov-code column at all (this platform's core "browse by 지자체" model doesn't
-- exist in the legacy generic shopping-mall schema), and Gift.java maps it directly.
ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS LOCGOV_CODE VARCHAR(50);

-- ID generation for OP_ITEM (AS-IS had no auto-increment; added for the new gift service implementation)
CREATE SEQUENCE IF NOT EXISTS op_item_item_id_seq START WITH 1000;

ALTER TABLE OP_ITEM ALTER COLUMN ITEM_ID SET DEFAULT nextval('op_item_item_id_seq');

ALTER SEQUENCE op_item_item_id_seq OWNED BY OP_ITEM.ITEM_ID;

-- Seed a few sample gifts so the public listing isn't empty on first run
INSERT INTO OP_ITEM (SELLER_ID, ITEM_NAME, ITEM_SUMMARY, DETAIL_CONTENT, CATEGORY_CODE, LOCGOV_CODE,
                      SALE_PRICE, STOCK_QUANTITY, SOLD_OUT, DISPLAY_FLAG, DATA_STATUS_CODE, CREATED_DATE)
SELECT * FROM (VALUES
    (9001::BIGINT, '강남구 한우 선물세트', '1++등급 한우 등심 선물세트 1kg', '엄선된 강남구 인증 한우입니다.', 'AGRI', '11230', 80000, 50, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9002::BIGINT, '해운대 건어물 세트', '멸치, 오징어채 등 건어물 모음', '해운대 앞바다에서 잡은 건어물입니다.', 'SEAFOOD', '26350', 35000, 30, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9003::BIGINT, '순천 지역사랑상품권 3만원권', '순천시 지역사랑상품권', '순천시 가맹점에서 사용 가능한 상품권입니다.', 'VOUCHER', '46150', 30000, 0, '1', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9004::BIGINT, '제주 감귤 생활용품 세트', '검수 전 신규 등록 상품', '제주 감귤로 만든 천연 비누 세트입니다.', 'LIVING', '50000', 25000, 100, '0', 'N', 'PENDING', to_char(now(), 'YYYYMMDDHH24MISS'))
) AS seed(seller_id, item_name, item_summary, detail_content, category_code, locgov_code, sale_price, stock_quantity, sold_out, display_flag, data_status_code, created_date)
WHERE NOT EXISTS (SELECT 1 FROM OP_ITEM);

-- =====================================================================
-- Round 2 additions: order service integration (SFR-006 choreography SAGA).
-- Tracks stock reservations made in response to order.saga ORDER_CREATED
-- events, both for idempotency (don't double-reserve on Kafka redelivery)
-- and so ORDER_CANCELLED knows whether this service actually has anything
-- to restore.
-- =====================================================================
CREATE TABLE IF NOT EXISTS GIFT_ORDER_STOCK (
    ORDER_ID       VARCHAR(50) PRIMARY KEY,
    ITEM_ID        BIGINT      NOT NULL,
    QUANTITY       INTEGER     NOT NULL,
    STATUS         VARCHAR(20) NOT NULL, -- RESERVED / RESTORED
    CREATED_DATE   TIMESTAMP   NOT NULL DEFAULT now()
);

-- =====================================================================
-- Round 3 additions: 리뷰/문의, 이미지 업로드(썸네일).
-- OP_ITEM_REVIEW / OP_ITEM_REVIEW_IMAGE / OP_ITEM_IMAGE already existed as
-- empty AS-IS tables (batch DDL generation) but none had PK generation -
-- add sequences/defaults per the same gotcha as OP_ITEM.ITEM_ID above.
-- There is no AS-IS per-item Q&A table in this service's own DB (the
-- closest AS-IS analog, OP_SHOP_INQUIRY, was assigned to admin's DB by the
-- batch split and mixes in seller-onboarding fields this doesn't need) -
-- add a small MVP-scoped G_ITEM_INQUIRY table instead, same rationale as
-- ORDER service's new OD_CLAIM table.
-- =====================================================================

CREATE SEQUENCE IF NOT EXISTS op_item_review_review_id_seq START WITH 1;

ALTER TABLE OP_ITEM_REVIEW ALTER COLUMN ITEM_REVIEW_ID SET DEFAULT nextval('op_item_review_review_id_seq');

ALTER SEQUENCE op_item_review_review_id_seq OWNED BY OP_ITEM_REVIEW.ITEM_REVIEW_ID;

-- 실버그: AS-IS OP_ITEM_REVIEW.CREATED_DATE는 다른 테이블들과 같은 legacy VARCHAR(14)
-- (yyyyMMddHHmmss) 컬럼인데, Review.java의 CREATED_DATE는 처음부터 LocalDateTime으로
-- 매핑돼 있었다(detail.html의 #temporals.format(r.createdDate, ...), my-reviews.html의
-- #strings.toString(r.createdDate).substring(0,10) 둘 다 진짜 Temporal 타입을 전제함 -
-- String으로 바꾸면 이 템플릿들이 깨진다). 시드 데이터에 리뷰가 한 번도 없어서 이 타입
-- 불일치가 발각되지 않고 있었다 - 답례품몰 라운드에서 실제로 리뷰를 등록해보다가
-- "value too long for type character varying(14)" INSERT 실패로 발견함. 엔티티가 아니라
-- 컬럼 쪽을 고친다(다른 서비스의 STATUS_CODE/날짜필드 타입버그와 동일한 성격).
ALTER TABLE OP_ITEM_REVIEW ALTER COLUMN CREATED_DATE TYPE TIMESTAMP USING NULL;

CREATE SEQUENCE IF NOT EXISTS op_item_review_image_id_seq START WITH 1;

ALTER TABLE OP_ITEM_REVIEW_IMAGE ALTER COLUMN ITEM_REVIEW_IMAGE_ID SET DEFAULT nextval('op_item_review_image_id_seq');

ALTER SEQUENCE op_item_review_image_id_seq OWNED BY OP_ITEM_REVIEW_IMAGE.ITEM_REVIEW_IMAGE_ID;

CREATE SEQUENCE IF NOT EXISTS op_item_image_id_seq START WITH 1;

ALTER TABLE OP_ITEM_IMAGE ALTER COLUMN ITEM_IMAGE_ID SET DEFAULT nextval('op_item_image_id_seq');

ALTER SEQUENCE op_item_image_id_seq OWNED BY OP_ITEM_IMAGE.ITEM_IMAGE_ID;

CREATE TABLE IF NOT EXISTS G_ITEM_INQUIRY (
    INQUIRY_ID       BIGSERIAL PRIMARY KEY,
    ITEM_ID          BIGINT      NOT NULL,
    USER_ID          BIGINT      NOT NULL,
    QUESTION         TEXT        NOT NULL,
    SECRET_YN        CHAR(1)     NOT NULL DEFAULT 'N',
    ANSWER           TEXT,
    ANSWERED_DATE    TIMESTAMP,
    STATUS           VARCHAR(20) NOT NULL,
    CREATED_DATE     TIMESTAMP   NOT NULL DEFAULT now(),
    DISPLAY_FLAG     VARCHAR(1)  NOT NULL DEFAULT 'Y'
);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_INQUIRY_STATUS', 'ko', 'WAITING',  '답변대기', 1, 'Y'),
('GIFT_INQUIRY_STATUS', 'ko', 'ANSWERED', '답변완료', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- SFR-005 "고객 후기/문의 관리(후기/문의/답변/신고)" - AS-IS에 없던 신규 요구사항이라
-- OP_/G_ 접두사 없이 이 프로젝트의 "신규 테이블" 관례(USER_DATA_DESTRUCTION_LOG 등)를 따른다.
-- 회원 1인당 리뷰/문의 1건당 중복신고를 막기 위해 (대상ID, USER_ID) 유니크 인덱스를 둔다.
CREATE TABLE IF NOT EXISTS ITEM_REVIEW_REPORT (
    REPORT_ID       BIGSERIAL PRIMARY KEY,
    ITEM_REVIEW_ID  BIGINT      NOT NULL,
    USER_ID         BIGINT      NOT NULL,
    REASON          VARCHAR(500),
    CREATED_DATE    TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_item_review_report_unique ON ITEM_REVIEW_REPORT (ITEM_REVIEW_ID, USER_ID);

CREATE TABLE IF NOT EXISTS ITEM_INQUIRY_REPORT (
    REPORT_ID    BIGSERIAL PRIMARY KEY,
    INQUIRY_ID   BIGINT      NOT NULL,
    USER_ID      BIGINT      NOT NULL,
    REASON       VARCHAR(500),
    CREATED_DATE TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_item_inquiry_report_unique ON ITEM_INQUIRY_REPORT (INQUIRY_ID, USER_ID);

-- =====================================================================
-- Round 4 (SFR-005 gap fill): 답례품 수정/노출정책/썸네일 자동생성.
-- =====================================================================
ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS DISPLAY_TYPE VARCHAR(20);

ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS DISPLAY_START_DATE VARCHAR(8);

ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS DISPLAY_END_DATE VARCHAR(8);

-- 기부금액 대비 선택 가능 기준 (이 답례품을 받으려면 최소 이만큼 기부해야 함) - 값만
-- 보관/노출하고, 실제 기부 신청 화면과의 연동은 donation 서비스 쪽 과제로 남겨둔다.
ALTER TABLE OP_ITEM ADD COLUMN IF NOT EXISTS MIN_DONATION_AMOUNT INTEGER;

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_DISPLAY_TYPE', 'ko', 'ALWAYS',  '상시노출', 1, 'Y'),
('GIFT_DISPLAY_TYPE', 'ko', 'LIMITED', '한시노출', 2, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- SFR-005 재검토 라운드 - "배송비·택배사 설정, 배송정책" gap을 닫는다. DELIVERY_COMPANY_NAME/
-- SHIPPING*/ITEM_RETURN_FLAG 컬럼은 OP_ITEM에 이미 있었으나(배치스캔 원본) 지금까지 코드가
-- 전혀 매핑하지 않고 있었다 - 새 컬럼 추가가 아니라 Gift.java에 매핑만 추가하면 된다.
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_SHIPPING_TYPE', 'ko', '1', '무료배송',       1, 'Y'),
('GIFT_SHIPPING_TYPE', 'ko', '2', '판매자조건부',   2, 'Y'),
('GIFT_SHIPPING_TYPE', 'ko', '3', '출고지조건부',   3, 'Y'),
('GIFT_SHIPPING_TYPE', 'ko', '4', '상품조건부',     4, 'Y'),
('GIFT_SHIPPING_TYPE', 'ko', '5', '개당배송비',     5, 'Y'),
('GIFT_SHIPPING_TYPE', 'ko', '6', '고정배송비',     6, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 폐지(영구 종료) - 판매중지(STOPPED, 재개 가능)와 구분되는 별도 상태.
-- DATA_STATUS_CODE가 AS-IS에서 VARCHAR(10)이라 'DISCONTINUED'(12자)가 안 들어가서 확장 -
-- curl로 실제 폐지 처리를 검증하다가 500 에러(value too long)로 발견.
ALTER TABLE OP_ITEM ALTER COLUMN DATA_STATUS_CODE TYPE VARCHAR(20);

INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_STATUS', 'ko', 'DISCONTINUED', '폐지', 5, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- 썸네일 자동생성(소/중/대 3종, 비동기) - 원본 이미지명은 그대로 두고 리사이즈된
-- 파생 파일명만 추가 컬럼에 기록한다. ThumbnailService가 @Async로 채운다.
ALTER TABLE OP_ITEM_IMAGE ADD COLUMN IF NOT EXISTS THUMBNAIL_SMALL VARCHAR(255);

ALTER TABLE OP_ITEM_IMAGE ADD COLUMN IF NOT EXISTS THUMBNAIL_MEDIUM VARCHAR(255);

ALTER TABLE OP_ITEM_IMAGE ADD COLUMN IF NOT EXISTS THUMBNAIL_LARGE VARCHAR(255);

-- =====================================================================
-- Round 5: TO-BE 메인페이지를 AS-IS(https://www.ilovegohyang.go.kr/main.html)
-- 실제 화면과 맞추는 작업 - AS-IS 답례품 카테고리는 6종(관광서비스 포함)인데
-- 이 서비스는 5종만 시딩돼 있었다. ordering=0으로 맨 앞에 추가.
-- =====================================================================
INSERT INTO OP_COMMON_CODE (CODE_TYPE, CODE_LANGUAGE, ID, LABEL, ORDERING, USE_YN) VALUES
('GIFT_CATEGORY', 'ko', 'TOUR', '관광서비스', 0, 'Y')
ON CONFLICT (CODE_TYPE, CODE_LANGUAGE, ID) DO NOTHING;

-- =====================================================================
-- Round 6: 답례품몰 GNB "전체 카테고리" 메가메뉴 (order 서비스가 소비) - AS-IS는
-- 이 소분류 트리를 GET /api/category(운영 DB, 로컬 소스에는 seed 파일이 없음)로
-- 내려주므로, 실제 라이브 사이트(ilovegohyang.go.kr)에서 그대로 가져온 진짜
-- 51개 소분류명이다 (대분류 6개당 실제 서비스와 동일한 개수/순서). GIFT_CATEGORY
-- 코드에 딸린 하위 분류라 부모-자식 구조가 필요해 OP_COMMON_CODE가 아닌 별도
-- 테이블로 둔다 - gift 자체는 이 세부 분류로 답례품을 필터링하지 않고
-- (OP_ITEM.CATEGORY_CODE는 대분류까지만 있음) 메뉴 표시/링크 용도로만 쓰인다.
-- =====================================================================
CREATE TABLE IF NOT EXISTS GIFT_SUBCATEGORY (
    SUBCATEGORY_ID   BIGSERIAL PRIMARY KEY,
    CATEGORY_CODE    VARCHAR(20)  NOT NULL, -- GIFT_CATEGORY.ID (TOUR/AGRI/SEAFOOD/PROCESSED/LIVING/VOUCHER)
    NAME             VARCHAR(100) NOT NULL,
    ORDERING         INTEGER      NOT NULL,
    -- 답례품 카테고리 관리(admin /admin/gift-categories, AS-IS CategoriesManagerController의
    -- SEO 메타 편집 기능 대응) - AS-IS OP_CATEGORY의 TITLE/KEYWORDS/DESCRIPTION 서브셋.
    META_TITLE       VARCHAR(200),
    META_KEYWORDS    VARCHAR(500),
    META_DESCRIPTION VARCHAR(500)
);

INSERT INTO GIFT_SUBCATEGORY (CATEGORY_CODE, NAME, ORDERING) VALUES
('TOUR', '지역축제', 1),
('TOUR', '여행/숙박/캠핑', 2),
('TOUR', '문화/공연', 3),
('TOUR', '문화체험/관광체험/레포츠', 4),
('TOUR', '박물관/미술관/유적', 5),
('TOUR', '기타 관광서비스', 6),
('AGRI', '곡물류', 1),
('AGRI', '과일류', 2),
('AGRI', '채소/버섯류', 3),
('AGRI', '임산물', 4),
('AGRI', '은행/잣/견과류', 5),
('AGRI', '한방약초', 6),
('AGRI', '소고기', 7),
('AGRI', '돼지고기', 8),
('AGRI', '닭고기', 9),
('AGRI', '오리고기', 10),
('AGRI', '달걀', 11),
('AGRI', '기타 농축산물', 12),
('SEAFOOD', '생선', 1),
('SEAFOOD', '건어물', 2),
('SEAFOOD', '활어회', 3),
('SEAFOOD', '김/해초', 4),
('SEAFOOD', '해산물/어패류', 5),
('SEAFOOD', '홍게/대게', 6),
('SEAFOOD', '게장', 7),
('SEAFOOD', '젓갈/소금', 8),
('SEAFOOD', '기타 수산물', 9),
('PROCESSED', '양념류', 1),
('PROCESSED', '한과/떡/제과/간식', 2),
('PROCESSED', '김치', 3),
('PROCESSED', '장/오일/장아찌', 4),
('PROCESSED', '식초', 5),
('PROCESSED', '밀키트/간편식', 6),
('PROCESSED', '잼/조청/청류', 7),
('PROCESSED', '참기름/들기름', 8),
('PROCESSED', '조미료', 9),
('PROCESSED', '고추장', 10),
('PROCESSED', '반찬류', 11),
('PROCESSED', '차/음료/즙류', 12),
('PROCESSED', '건강식품', 13),
('PROCESSED', '차류', 14),
('PROCESSED', '주류/전통주', 15),
('PROCESSED', '기타 가공식품', 16),
('LIVING', '화장품/비누', 1),
('LIVING', '주방세제', 2),
('LIVING', '도자기/생활목기', 3),
('LIVING', '제기/병풍', 4),
('LIVING', '편백베개/방향제', 5),
('LIVING', '기타 생활용품', 6),
('VOUCHER', '지역사랑상품권', 1),
('VOUCHER', '기타 상품권', 2);

-- 메가메뉴 3단계(대분류 > 중분류 > 개별품목)의 마지막 단 - 운영 사이트 GET /api/category
-- 응답을 그대로 옮긴 실제 데이터. 중분류 중 일부만 개별품목을 갖는다(운영 사이트도 동일).
CREATE TABLE IF NOT EXISTS GIFT_SUBCATEGORY_ITEM (
    SUBCATEGORY_ITEM_ID BIGSERIAL PRIMARY KEY,
    SUBCATEGORY_ID       BIGINT       NOT NULL REFERENCES GIFT_SUBCATEGORY(SUBCATEGORY_ID),
    NAME                  VARCHAR(100) NOT NULL,
    ORDERING              INTEGER      NOT NULL
);

INSERT INTO GIFT_SUBCATEGORY_ITEM (SUBCATEGORY_ID, NAME, ORDERING)
SELECT s.subcategory_id, v.name, v.ordering
FROM GIFT_SUBCATEGORY s
JOIN (VALUES
    ('AGRI', '곡물류', '쌀(백미)', 1), ('AGRI', '곡물류', '현미', 2), ('AGRI', '곡물류', '흑미', 3),
    ('AGRI', '곡물류', '찹쌀', 4), ('AGRI', '곡물류', '콩/팥', 5), ('AGRI', '곡물류', '보리', 6),
    ('AGRI', '곡물류', '기타 잡곡', 7),
    ('AGRI', '과일류', '사과', 1), ('AGRI', '과일류', '배', 2), ('AGRI', '과일류', '토마토', 3),
    ('AGRI', '과일류', '블루베리', 4), ('AGRI', '과일류', '감/곶감', 5), ('AGRI', '과일류', '참외', 6),
    ('AGRI', '과일류', '포도', 7), ('AGRI', '과일류', '매실', 8), ('AGRI', '과일류', '기타 과일', 9),
    ('AGRI', '과일류', '멜론', 10),
    ('AGRI', '채소/버섯류', '고구마', 1), ('AGRI', '채소/버섯류', '고추/고추가루', 2),
    ('AGRI', '채소/버섯류', '배추/무', 3), ('AGRI', '채소/버섯류', '마늘', 4),
    ('AGRI', '채소/버섯류', '감자/돼지감자', 5), ('AGRI', '채소/버섯류', '버섯', 6),
    ('AGRI', '채소/버섯류', '옥수수/호박', 7), ('AGRI', '채소/버섯류', '당근', 8),
    ('AGRI', '채소/버섯류', '기타 채소', 9),
    ('AGRI', '임산물', '더덕/산나물', 1), ('AGRI', '임산물', '산삼/인삼', 2),
    ('AGRI', '임산물', '건나물', 3), ('AGRI', '임산물', '기타 임산물', 4),
    ('AGRI', '한방약초', '인삼', 1), ('AGRI', '한방약초', '도라지', 2),
    ('AGRI', '한방약초', '하수오/백수오', 3), ('AGRI', '한방약초', '울금', 4),
    ('AGRI', '한방약초', '와송', 5), ('AGRI', '한방약초', '기타 한방', 6),
    ('PROCESSED', '건강식품', '꿀', 1), ('PROCESSED', '건강식품', '엑기스/즙', 2),
    ('PROCESSED', '건강식품', '분말/가루', 3), ('PROCESSED', '건강식품', '환/캡슐', 4)
) AS v(category_code, subcategory_name, name, ordering)
    ON s.category_code = v.category_code AND s.name = v.subcategory_name;

-- 답례품몰 GNB "제철식품관"/"마을기업관" 데모 데이터. G_SEASON_FOOD/G_SEASON_FOOD_ITEM과
-- OP_SELLER.COMMUNITY_BUSINESS_YN은 배치 스캔으로 이미 존재했지만 애플리케이션 레이어가
-- 없었다 - 이번에 처음 얹는다.
INSERT INTO g_season_food (season_food_month, season_food_keyword, reg_seq, frst_regist_pnttm)
VALUES (8, '한여름 제철 답례품', 1, now())
ON CONFLICT DO NOTHING;

INSERT INTO g_season_food_item (item_id, season_food_month, frst_regist_pnttm)
VALUES (1000, 8, now()), (1009, 8, now()), (1011, 8, now()), (1016, 8, now()), (1020, 8, now())
ON CONFLICT DO NOTHING;

INSERT INTO op_seller (seller_id, login_id, seller_name, community_business_yn)
VALUES (9502, 'seller9502', '청주시마을기업협동조합', 'Y'),
       (9504, 'seller9504', '충주시마을기업협동조합', 'Y'),
       (9509, 'seller9509', '북구마을기업협동조합', 'Y')
ON CONFLICT (seller_id) DO UPDATE SET community_business_yn = 'Y';

-- =====================================================================
-- AS-IS VIEW 이관 (db-dump/VIEW/view_search_event.sql). 원본은 OP_FEATURED
-- 단일 테이블 - 이 프로젝트가 GNB "이벤트" 메뉴용으로 새로 설계한 OP_EVENT와는
-- 완전히 별개(AS-IS 원본 "특집/프로모션 배너" 개념). date_format(CURRENT_DATETIME,
-- '%Y%m%d') -> to_char(now(),'YYYYMMDD')로 변환.
-- =====================================================================
CREATE OR REPLACE VIEW view_search_event AS
SELECT f.featured_id,
       f.featured_name,
       f.featured_content,
       f.featured_host,
       concat('/featured/eventDetail.html?pages=', CAST(f.featured_id AS VARCHAR)) AS detail_url,
       f.start_date,
       f.end_date,
       f.featured_phone_no1,
       f.featured_phone_no2,
       f.featured_phone_no3,
       concat('/upload/featured/', CAST(f.featured_id AS VARCHAR), f.featured_list_image) AS list_image_url,
       concat('/upload/featured/', CAST(f.featured_id AS VARCHAR), f.featured_image_mobile) AS mobile_image_url
FROM op_featured f
WHERE f.featured_flag = 'Y'
  AND to_char(now(), 'YYYYMMDD') BETWEEN f.start_date AND f.end_date;

-- =====================================================================
-- 입점업체관리 (AS-IS opmanager/seller "입점업체관리") - OP_SELLER.SELLER_ID가 배치
-- 스캔 당시 시퀀스 없이 DEFAULT 0이었다(관리자 CRUD 화면을 새로 붙이며 발견). 기존
-- 실 데이터가 9000번대라 충돌을 피해 10000부터 새 시퀀스를 시작한다. 답례품
-- 아이템이 참조하지만 정작 OP_SELLER 행이 없던 판매자(9001~9004)도 최소 정보로
-- 백필한다(point 원장 누락과 같은 성격의 데이터 정합성 갭).
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_seller_seller_id_seq START WITH 10000;
ALTER TABLE OP_SELLER ALTER COLUMN SELLER_ID SET DEFAULT nextval('op_seller_seller_id_seq');
ALTER SEQUENCE op_seller_seller_id_seq OWNED BY OP_SELLER.SELLER_ID;

INSERT INTO OP_SELLER (SELLER_ID, SELLER_NAME, LOGIN_ID, COMPANY_NAME, STATUS_CODE, CREATED_DATE, CREATED_USER_ID)
SELECT DISTINCT i.SELLER_ID, '미등록 판매자 ' || i.SELLER_ID, 'seller' || i.SELLER_ID, '미등록 판매자 ' || i.SELLER_ID,
       '1', to_char(now(), 'YYYYMMDDHH24MISS'), 0
FROM OP_ITEM i
WHERE i.SELLER_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM OP_SELLER s WHERE s.SELLER_ID = i.SELLER_ID);

-- =====================================================================
-- 브랜드관리 (AS-IS opmanager/brand) - OP_BRAND.BRAND_ID가 배치 스캔 당시 시퀀스 없이
-- 존재했다(관리자 CRUD 화면을 새로 붙이며 발견).
-- =====================================================================
CREATE SEQUENCE IF NOT EXISTS op_brand_brand_id_seq START WITH 1000;
ALTER TABLE OP_BRAND ALTER COLUMN BRAND_ID SET DEFAULT nextval('op_brand_brand_id_seq');
ALTER SEQUENCE op_brand_brand_id_seq OWNED BY OP_BRAND.BRAND_ID;

-- =====================================================================
-- Round 7 (답례품몰 Vue3 전환): 최초 4개뿐이던 시드 답례품을 지자체 특산품 11개로
-- 확충하고, 그동안 비어있던 OP_ITEM_IMAGE도 채운다(실제 사진 - Wikimedia Commons
-- 공개 라이선스 이미지를 다운로드해 FileStorageService/ThumbnailService와 동일한
-- 규칙(UUID 파일명 + 소/중/대 썸네일)으로 gift/uploads/gift/에 배치했다). SELLER_ID로
-- 매칭해 신규 아이템만 골라 삽입하므로(9005~9015가 이미 있으면 스킵) 재실행해도 안전.
-- 지역사랑상품권 2건(1002/1014)은 실물 상품 사진이 없는 카테고리 특성상 이미지 없이 둔다.
-- =====================================================================
INSERT INTO OP_ITEM (SELLER_ID, ITEM_NAME, ITEM_SUMMARY, DETAIL_CONTENT, CATEGORY_CODE, LOCGOV_CODE,
                      SALE_PRICE, STOCK_QUANTITY, SOLD_OUT, DISPLAY_FLAG, DATA_STATUS_CODE, CREATED_DATE)
SELECT * FROM (VALUES
    (9005::BIGINT, '횡성한우 등심 선물세트', '1등급 이상 횡성한우 등심 1kg', '횡성군 인증 한우 브랜드입니다.', 'AGRI', '51730', 120000, 40, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9006::BIGINT, '청송 사과 선물세트', '당도 높은 청송 부사 사과 5kg', '해발 높은 청송에서 재배한 사과입니다.', 'AGRI', '47750', 45000, 60, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9007::BIGINT, '나주 배 선물세트', '과즙 가득한 나주 신고배 5kg', '나주시 특산 신고배입니다.', 'AGRI', '12170', 40000, 55, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9008::BIGINT, '보성 녹차 선물세트', '보성 다원 녹차잎으로 만든 프리미엄 녹차 세트', '보성 다원에서 재배한 녹차입니다.', 'PROCESSED', '12750', 38000, 70, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9009::BIGINT, '완도 전복 세트', '싱싱한 완도산 전복 1kg', '청정 완도 해역에서 양식한 전복입니다.', 'SEAFOOD', '12850', 90000, 25, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9010::BIGINT, '논산 딸기 세트', '달콤한 논산 설향 딸기 2kg', '논산시 특산 설향 딸기입니다.', 'AGRI', '44230', 32000, 45, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9011::BIGINT, '부여 밤 선물세트', '알이 굵은 부여 햇밤 3kg', '부여군에서 수확한 햇밤입니다.', 'AGRI', '44760', 28000, 50, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9012::BIGINT, '상주 곶감 세트', '자연건조로 만든 상주 곶감 30입', '상주시 특산 곶감입니다.', 'AGRI', '47250', 42000, 35, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9013::BIGINT, '고창 복분자즙 세트', '고창 복분자로 만든 건강즙 30포', '고창군 특산 복분자로 만들었습니다.', 'PROCESSED', '52790', 35000, 65, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9014::BIGINT, '강진 청자 머그컵 세트', '강진 청자 장인이 만든 머그컵 2p 세트', '강진 청자 도예가가 직접 제작했습니다.', 'LIVING', '12780', 48000, 20, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS')),
    (9015::BIGINT, '제주 지역사랑상품권 5만원권', '제주 가맹점 사용 가능 상품권', '제주도 가맹점에서 사용 가능한 상품권입니다.', 'VOUCHER', '50000', 30000, 80, '0', 'Y', 'APPROVED', to_char(now(), 'YYYYMMDDHH24MISS'))
) AS seed(seller_id, item_name, item_summary, detail_content, category_code, locgov_code, sale_price, stock_quantity, sold_out, display_flag, data_status_code, created_date)
WHERE NOT EXISTS (SELECT 1 FROM OP_ITEM WHERE SELLER_ID = seed.seller_id);

INSERT INTO OP_SELLER (SELLER_ID, SELLER_NAME, LOGIN_ID, COMPANY_NAME, STATUS_CODE, CREATED_DATE, CREATED_USER_ID)
SELECT DISTINCT i.SELLER_ID, '미등록 판매자 ' || i.SELLER_ID, 'seller' || i.SELLER_ID, '미등록 판매자 ' || i.SELLER_ID,
       '1', to_char(now(), 'YYYYMMDDHH24MISS'), 0
FROM OP_ITEM i
WHERE i.SELLER_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM OP_SELLER s WHERE s.SELLER_ID = i.SELLER_ID);

INSERT INTO OP_ITEM_IMAGE (ITEM_ID, IMAGE_NAME, ORDERING, CREATED_DATE, THUMBNAIL_SMALL, THUMBNAIL_MEDIUM, THUMBNAIL_LARGE)
SELECT i.ITEM_ID, x.image_name, 0, to_char(now(), 'YYYYMMDDHH24MISS'),
       regexp_replace(x.image_name, '\.jpg$', '_small.png'),
       regexp_replace(x.image_name, '\.jpg$', '_medium.png'),
       regexp_replace(x.image_name, '\.jpg$', '_large.png')
FROM OP_ITEM i
JOIN (VALUES
    (9005, '72764724-3b31-4fb8-937f-54585a2b0234.jpg'),
    (9006, '870aa48d-0fd3-4df6-a9c0-b6f413a88886.jpg'),
    (9007, 'b307c4e9-47ad-454a-954f-6f1057cb8789.jpg'),
    (9008, '60733033-93c4-42e8-9801-600fb0d47e14.jpg'),
    (9009, 'f74b2da1-924d-418c-8721-69f483391186.jpg'),
    (9010, '4768b5b8-ca78-452d-b1d4-6847eabeae65.jpg'),
    (9011, '0c857e96-6372-4e86-9255-c1858f8f6896.jpg'),
    (9012, '92d141a7-6c44-4f56-be80-f00f7ce821fc.jpg'),
    (9013, '4dd878a2-cf2c-457c-846d-80546fa64087.jpg'),
    (9014, '699af444-4adb-4f3a-91cd-3e9adc33ed64.jpg')
) AS x(seller_id, image_name) ON i.SELLER_ID = x.seller_id
WHERE NOT EXISTS (SELECT 1 FROM OP_ITEM_IMAGE ii WHERE ii.ITEM_ID = i.ITEM_ID);

INSERT INTO OP_ITEM_IMAGE (ITEM_ID, IMAGE_NAME, ORDERING, CREATED_DATE, THUMBNAIL_SMALL, THUMBNAIL_MEDIUM, THUMBNAIL_LARGE)
SELECT x.item_id, x.image_name, 0, to_char(now(), 'YYYYMMDDHH24MISS'),
       regexp_replace(x.image_name, '\.jpg$', '_small.png'),
       regexp_replace(x.image_name, '\.jpg$', '_medium.png'),
       regexp_replace(x.image_name, '\.jpg$', '_large.png')
FROM (VALUES
    (1000, '23c1795d-9371-45d5-9d2c-5f54a71a085d.jpg'),
    (1001, '33201915-c5ec-483f-ba18-7fc9b27322e7.jpg'),
    (1003, '9a0dbb0f-315b-4fdd-a233-8fd5fe0d1a69.jpg')
) AS x(item_id, image_name)
WHERE NOT EXISTS (SELECT 1 FROM OP_ITEM_IMAGE ii WHERE ii.ITEM_ID = x.item_id);
