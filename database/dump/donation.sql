--
-- PostgreSQL database dump
--

\restrict BuGAacFr4VxyhNUg2Uah1iuH4glFu1VuEJA3aNnH2JCrnu20fJ44fKqMlb60OXO

-- Dumped from database version 17.10 (Debian 17.10-1.pgdg13+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.g_relay_log DROP CONSTRAINT IF EXISTS fk_g_relay_log_cntr_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_locgov_dept_hist DROP CONSTRAINT IF EXISTS fk_g_locgov_dept_hist_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_intrst_locgov DROP CONSTRAINT IF EXISTS fk_g_intrst_locgov_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_honor_cntrbtr DROP CONSTRAINT IF EXISTS fk_g_honor_cntrbtr_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_ctbny_opratn DROP CONSTRAINT IF EXISTS fk_g_ctbny_opratn_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_ctbny_opratn_file DROP CONSTRAINT IF EXISTS fk_g_ctbny_opratn_file_regist_sn;
ALTER TABLE IF EXISTS ONLY public.g_cntr DROP CONSTRAINT IF EXISTS fk_g_cntr_psitn_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_cntr_lmtt DROP CONSTRAINT IF EXISTS fk_g_cntr_lmtt_locgov_code;
ALTER TABLE IF EXISTS ONLY public.g_cntr DROP CONSTRAINT IF EXISTS fk_g_cntr_cntr_locgov_code;
ALTER TABLE IF EXISTS ONLY public.op_spel_dstr_zn DROP CONSTRAINT IF EXISTS op_spel_dstr_zn_pkey;
ALTER TABLE IF EXISTS ONLY public.op_island DROP CONSTRAINT IF EXISTS op_island_pkey;
ALTER TABLE IF EXISTS ONLY public.op_honor_view_hist DROP CONSTRAINT IF EXISTS op_honor_view_hist_pkey;
ALTER TABLE IF EXISTS ONLY public.op_event_reply DROP CONSTRAINT IF EXISTS op_event_reply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_event_item DROP CONSTRAINT IF EXISTS op_event_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_event_code DROP CONSTRAINT IF EXISTS op_event_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_event_code_log DROP CONSTRAINT IF EXISTS op_event_code_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_common_code DROP CONSTRAINT IF EXISTS op_common_code_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_stnd_sunap_now DROP CONSTRAINT IF EXISTS gif_stnd_sunap_now_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_stnd_jijache_now DROP CONSTRAINT IF EXISTS gif_stnd_jijache_now_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_stnd_buga_now DROP CONSTRAINT IF EXISTS gif_stnd_buga_now_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_seoul DROP CONSTRAINT IF EXISTS gif_seoul_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_hometax DROP CONSTRAINT IF EXISTS gif_hometax_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_external_mst DROP CONSTRAINT IF EXISTS gif_external_mst_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_etax_sunap DROP CONSTRAINT IF EXISTS gif_etax_sunap_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_etax_sunap_etc DROP CONSTRAINT IF EXISTS gif_etax_sunap_etc_pkey;
ALTER TABLE IF EXISTS ONLY public.gif_etax_daesa DROP CONSTRAINT IF EXISTS gif_etax_daesa_pkey;
ALTER TABLE IF EXISTS ONLY public.g_relay_log DROP CONSTRAINT IF EXISTS g_relay_log_pkey;
ALTER TABLE IF EXISTS ONLY public.g_prj_content_img_desc DROP CONSTRAINT IF EXISTS g_prj_content_img_desc_pkey;
ALTER TABLE IF EXISTS ONLY public.g_pay_log DROP CONSTRAINT IF EXISTS g_pay_log_pkey;
ALTER TABLE IF EXISTS ONLY public.g_next_sunap_response DROP CONSTRAINT IF EXISTS g_next_sunap_response_pkey;
ALTER TABLE IF EXISTS ONLY public.g_next_buga_request_temp DROP CONSTRAINT IF EXISTS g_next_buga_request_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.g_next_buga_request DROP CONSTRAINT IF EXISTS g_next_buga_request_pkey;
ALTER TABLE IF EXISTS ONLY public.g_locgov DROP CONSTRAINT IF EXISTS g_locgov_pkey;
ALTER TABLE IF EXISTS ONLY public.g_locgov_image DROP CONSTRAINT IF EXISTS g_locgov_image_pkey;
ALTER TABLE IF EXISTS ONLY public.g_locgov_fav_item_mng DROP CONSTRAINT IF EXISTS g_locgov_fav_item_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_locgov_dept_hist DROP CONSTRAINT IF EXISTS g_locgov_dept_hist_pkey;
ALTER TABLE IF EXISTS ONLY public.g_link_instt_enc_key_mng DROP CONSTRAINT IF EXISTS g_link_instt_enc_key_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_lclgv_hnr_user_stng_mng DROP CONSTRAINT IF EXISTS g_lclgv_hnr_user_stng_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_lclgv_hnr_user_rwrd_img_expln DROP CONSTRAINT IF EXISTS g_lclgv_hnr_user_rwrd_img_expln_pkey;
ALTER TABLE IF EXISTS ONLY public.g_intrst_locgov DROP CONSTRAINT IF EXISTS g_intrst_locgov_pkey;
ALTER TABLE IF EXISTS ONLY public.g_instt_code DROP CONSTRAINT IF EXISTS g_instt_code_pkey;
ALTER TABLE IF EXISTS ONLY public.g_honor_cntrbtr_stdr DROP CONSTRAINT IF EXISTS g_honor_cntrbtr_stdr_pkey;
ALTER TABLE IF EXISTS ONLY public.g_honor_cntrbtr DROP CONSTRAINT IF EXISTS g_honor_cntrbtr_pkey;
ALTER TABLE IF EXISTS ONLY public.g_honor_benefit DROP CONSTRAINT IF EXISTS g_honor_benefit_pkey;
ALTER TABLE IF EXISTS ONLY public.g_giro DROP CONSTRAINT IF EXISTS g_giro_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dy_gramt DROP CONSTRAINT IF EXISTS g_dy_gramt_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_prj_notice DROP CONSTRAINT IF EXISTS g_dsgncntr_prj_notice_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_prj_notice_img_desc DROP CONSTRAINT IF EXISTS g_dsgncntr_prj_notice_img_desc_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_prj_notice_file DROP CONSTRAINT IF EXISTS g_dsgncntr_prj_notice_file_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_prj_mng DROP CONSTRAINT IF EXISTS g_dsgncntr_prj_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_prj_img DROP CONSTRAINT IF EXISTS g_dsgncntr_prj_img_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_part_user_mppng DROP CONSTRAINT IF EXISTS g_dsgncntr_part_user_mppng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgncntr_part_mng DROP CONSTRAINT IF EXISTS g_dsgncntr_part_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_prj_notice DROP CONSTRAINT IF EXISTS g_dsgn_prj_notice_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_user_msg DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_user_msg_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_ntc_mng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_ntc_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_ntc_file_mng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_ntc_file_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_ntc_cn_img_expln DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_ntc_cn_img_expln_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_mng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_img_mng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_img_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_dept_mngr_mpng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_dept_mngr_mpng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_dept_mng DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_dept_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_cn_img_expln DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_cn_img_expln_pkey;
ALTER TABLE IF EXISTS ONLY public.g_dsgn_dntn_biz_aprv_log DROP CONSTRAINT IF EXISTS g_dsgn_dntn_biz_aprv_log_pkey;
ALTER TABLE IF EXISTS ONLY public.g_disaster_zone DROP CONSTRAINT IF EXISTS g_disaster_zone_pkey;
ALTER TABLE IF EXISTS ONLY public.g_ctbny_setup DROP CONSTRAINT IF EXISTS g_ctbny_setup_pkey;
ALTER TABLE IF EXISTS ONLY public.g_ctbny_opratn DROP CONSTRAINT IF EXISTS g_ctbny_opratn_pkey;
ALTER TABLE IF EXISTS ONLY public.g_ctbny_opratn_file DROP CONSTRAINT IF EXISTS g_ctbny_opratn_file_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_wegive DROP CONSTRAINT IF EXISTS g_cntr_wegive_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_temp2 DROP CONSTRAINT IF EXISTS g_cntr_temp2_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_tax_log DROP CONSTRAINT IF EXISTS g_cntr_tax_log_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_reqmng DROP CONSTRAINT IF EXISTS g_cntr_reqmng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_rcipt DROP CONSTRAINT IF EXISTS g_cntr_rcipt_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr DROP CONSTRAINT IF EXISTS g_cntr_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_lmtt DROP CONSTRAINT IF EXISTS g_cntr_lmtt_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_chenap DROP CONSTRAINT IF EXISTS g_cntr_chenap_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cheer_msg DROP CONSTRAINT IF EXISTS g_cheer_msg_pkey;
ALTER TABLE IF EXISTS ONLY public.g_catalog_mng DROP CONSTRAINT IF EXISTS g_catalog_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_catalog_content_mng DROP CONSTRAINT IF EXISTS g_catalog_content_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_catalog_content_img_desc DROP CONSTRAINT IF EXISTS g_catalog_content_img_desc_pkey;
ALTER TABLE IF EXISTS ONLY public.g_catalog_card_news_mng DROP CONSTRAINT IF EXISTS g_catalog_card_news_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_catalog_card_news_img_desc DROP CONSTRAINT IF EXISTS g_catalog_card_news_img_desc_pkey;
ALTER TABLE IF EXISTS ONLY public.g_agency_login_confirm_info DROP CONSTRAINT IF EXISTS g_agency_login_confirm_info_pkey;
ALTER TABLE IF EXISTS ONLY public.g_adm_locgov DROP CONSTRAINT IF EXISTS g_adm_locgov_pkey;
ALTER TABLE IF EXISTS ONLY public.donation_levy DROP CONSTRAINT IF EXISTS donation_levy_pkey;
ALTER TABLE IF EXISTS public.op_spel_dstr_zn ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_relay_log ALTER COLUMN relay_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_prj_content_img_desc ALTER COLUMN prj_content_img_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_pay_log ALTER COLUMN pay_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgncntr_prj_notice ALTER COLUMN prj_notice_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgncntr_prj_mng ALTER COLUMN prj_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgncntr_prj_img ALTER COLUMN dsgncntr_prj_image_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgncntr_part_mng ALTER COLUMN dsgncntr_part_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_prj_notice ALTER COLUMN prj_notice_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_ntc_mng ALTER COLUMN dsgn_dntn_biz_ntc_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_mng ALTER COLUMN dsgn_dntn_biz_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_img_mng ALTER COLUMN dsgn_dntn_biz_img_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_dept_mng ALTER COLUMN dsgn_dntn_biz_dept_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_cn_img_expln ALTER COLUMN dsgn_dntn_biz_cn_img_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_dsgn_dntn_biz_aprv_log ALTER COLUMN dsgn_dntn_biz_aprv_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_ctbny_opratn_file ALTER COLUMN regist_file_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_ctbny_opratn ALTER COLUMN regist_sn DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_cntr_wegive ALTER COLUMN cntr_sn DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_cntr_tax_log ALTER COLUMN log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_cntr_reqmng ALTER COLUMN req_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_cntr_chenap ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_catalog_card_news_mng ALTER COLUMN card_news_id DROP DEFAULT;
DROP VIEW IF EXISTS public.view_search_locgov;
DROP SEQUENCE IF EXISTS public.op_spel_dstr_zn_id_seq;
DROP TABLE IF EXISTS public.op_spel_dstr_zn;
DROP TABLE IF EXISTS public.op_sido_mapping;
DROP TABLE IF EXISTS public.op_island;
DROP TABLE IF EXISTS public.op_honor_view_hist;
DROP TABLE IF EXISTS public.op_event_reply;
DROP TABLE IF EXISTS public.op_event_item;
DROP TABLE IF EXISTS public.op_event_code_log;
DROP TABLE IF EXISTS public.op_event_code;
DROP TABLE IF EXISTS public.op_common_code;
DROP TABLE IF EXISTS public.gif_stnd_sunap_now;
DROP TABLE IF EXISTS public.gif_stnd_jijache_now;
DROP TABLE IF EXISTS public.gif_stnd_buga_now;
DROP TABLE IF EXISTS public.gif_seoul;
DROP TABLE IF EXISTS public.gif_hometax;
DROP TABLE IF EXISTS public.gif_external_mst;
DROP TABLE IF EXISTS public.gif_etax_sunap_etc;
DROP TABLE IF EXISTS public.gif_etax_sunap;
DROP TABLE IF EXISTS public.gif_etax_daesa;
DROP SEQUENCE IF EXISTS public.g_relay_log_relay_log_id_seq;
DROP TABLE IF EXISTS public.g_relay_log;
DROP SEQUENCE IF EXISTS public.g_prj_content_img_desc_prj_content_img_id_seq;
DROP TABLE IF EXISTS public.g_prj_content_img_desc;
DROP SEQUENCE IF EXISTS public.g_pay_log_pay_log_id_seq;
DROP TABLE IF EXISTS public.g_pay_log;
DROP TABLE IF EXISTS public.g_next_sunap_response;
DROP TABLE IF EXISTS public.g_next_buga_request_temp;
DROP TABLE IF EXISTS public.g_next_buga_request;
DROP TABLE IF EXISTS public.g_locgov_image;
DROP TABLE IF EXISTS public.g_locgov_fav_item_mng;
DROP TABLE IF EXISTS public.g_locgov_dept_hist;
DROP TABLE IF EXISTS public.g_locgov;
DROP TABLE IF EXISTS public.g_link_instt_enc_key_mng;
DROP TABLE IF EXISTS public.g_lclgv_hnr_user_stng_mng;
DROP TABLE IF EXISTS public.g_lclgv_hnr_user_rwrd_img_expln;
DROP TABLE IF EXISTS public.g_intrst_locgov;
DROP TABLE IF EXISTS public.g_instt_code;
DROP TABLE IF EXISTS public.g_honor_cntrbtr_stdr;
DROP TABLE IF EXISTS public.g_honor_cntrbtr;
DROP TABLE IF EXISTS public.g_honor_benefit;
DROP TABLE IF EXISTS public.g_giro;
DROP TABLE IF EXISTS public.g_dy_gramt;
DROP SEQUENCE IF EXISTS public.g_dsgncntr_prj_notice_prj_notice_id_seq;
DROP TABLE IF EXISTS public.g_dsgncntr_prj_notice_img_desc;
DROP TABLE IF EXISTS public.g_dsgncntr_prj_notice_file;
DROP TABLE IF EXISTS public.g_dsgncntr_prj_notice;
DROP SEQUENCE IF EXISTS public.g_dsgncntr_prj_mng_prj_id_seq;
DROP TABLE IF EXISTS public.g_dsgncntr_prj_mng;
DROP SEQUENCE IF EXISTS public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq;
DROP TABLE IF EXISTS public.g_dsgncntr_prj_img;
DROP TABLE IF EXISTS public.g_dsgncntr_part_user_mppng;
DROP SEQUENCE IF EXISTS public.g_dsgncntr_part_mng_dsgncntr_part_id_seq;
DROP TABLE IF EXISTS public.g_dsgncntr_part_mng;
DROP SEQUENCE IF EXISTS public.g_dsgn_prj_notice_prj_notice_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_prj_notice;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_user_msg;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_ntc_mng;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_ntc_file_mng;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_ntc_cn_img_expln;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_mng;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_img_mng;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_dept_mngr_mpng;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_dept_mng;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_cn_img_expln;
DROP SEQUENCE IF EXISTS public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq;
DROP TABLE IF EXISTS public.g_dsgn_dntn_biz_aprv_log;
DROP TABLE IF EXISTS public.g_disaster_zone;
DROP TABLE IF EXISTS public.g_ctbny_setup;
DROP SEQUENCE IF EXISTS public.g_ctbny_opratn_regist_sn_seq;
DROP SEQUENCE IF EXISTS public.g_ctbny_opratn_file_regist_file_id_seq;
DROP TABLE IF EXISTS public.g_ctbny_opratn_file;
DROP TABLE IF EXISTS public.g_ctbny_opratn;
DROP SEQUENCE IF EXISTS public.g_cntr_wegive_cntr_sn_seq;
DROP TABLE IF EXISTS public.g_cntr_wegive;
DROP TABLE IF EXISTS public.g_cntr_temp2;
DROP TABLE IF EXISTS public.g_cntr_tax_temp_log;
DROP TABLE IF EXISTS public.g_cntr_tax_temp;
DROP SEQUENCE IF EXISTS public.g_cntr_tax_log_log_id_seq;
DROP TABLE IF EXISTS public.g_cntr_tax_log3;
DROP TABLE IF EXISTS public.g_cntr_tax_log;
DROP SEQUENCE IF EXISTS public.g_cntr_reqmng_req_id_seq;
DROP TABLE IF EXISTS public.g_cntr_reqmng;
DROP TABLE IF EXISTS public.g_cntr_rcipt;
DROP TABLE IF EXISTS public.g_cntr_locgov_history;
DROP TABLE IF EXISTS public.g_cntr_lmtt;
DROP SEQUENCE IF EXISTS public.g_cntr_chenap_id_seq;
DROP TABLE IF EXISTS public.g_cntr_chenap;
DROP TABLE IF EXISTS public.g_cntr;
DROP TABLE IF EXISTS public.g_cheer_msg;
DROP TABLE IF EXISTS public.g_catalog_mng;
DROP TABLE IF EXISTS public.g_catalog_content_mng;
DROP TABLE IF EXISTS public.g_catalog_content_img_desc;
DROP SEQUENCE IF EXISTS public.g_catalog_card_news_mng_card_news_id_seq;
DROP TABLE IF EXISTS public.g_catalog_card_news_mng;
DROP TABLE IF EXISTS public.g_catalog_card_news_img_desc;
DROP TABLE IF EXISTS public.g_agency_login_confirm_info;
DROP TABLE IF EXISTS public.g_adm_locgov;
DROP TABLE IF EXISTS public.donation_levy;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: donation_levy; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.donation_levy (
    cntr_sn character varying(50) NOT NULL,
    buga_no character varying(50),
    buga_date timestamp without time zone,
    sunap_yn character(1),
    sunap_date timestamp without time zone,
    nts_status character varying(20),
    nts_receipt_no character varying(50),
    nts_registered_date timestamp without time zone,
    nts_error_message character varying(500)
);


--
-- Name: g_adm_locgov; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_adm_locgov (
    adm_cd character varying(20) NOT NULL,
    adm_sect_nm character varying(1000),
    locgov_code character varying(20)
);


--
-- Name: g_agency_login_confirm_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_agency_login_confirm_info (
    user_session_id character varying(50) NOT NULL,
    valid_access_cd character varying(10000) NOT NULL,
    private_key character varying(5000) NOT NULL,
    manager_id bigint NOT NULL,
    cntrbtr_mobile character varying(50) NOT NULL,
    cntrbtr_id bigint DEFAULT 0 NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_catalog_card_news_img_desc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_catalog_card_news_img_desc (
    card_news_id bigint NOT NULL,
    img_desc character varying(500) NOT NULL,
    img_seq integer NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_catalog_card_news_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_catalog_card_news_mng (
    card_news_id bigint NOT NULL,
    catalog_year integer NOT NULL,
    catalog_no integer NOT NULL,
    card_news_subject character varying(500) NOT NULL,
    card_news_cn character varying,
    delete_yn character varying(5) DEFAULT 'N'::character varying NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_catalog_card_news_mng_card_news_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_catalog_card_news_mng_card_news_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_catalog_card_news_mng_card_news_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_catalog_card_news_mng_card_news_id_seq OWNED BY public.g_catalog_card_news_mng.card_news_id;


--
-- Name: g_catalog_content_img_desc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_catalog_content_img_desc (
    catalog_content_id bigint NOT NULL,
    img_desc character varying(500),
    img_seq integer NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone
);


--
-- Name: g_catalog_content_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_catalog_content_mng (
    catalog_content_id bigint NOT NULL,
    catalog_year integer,
    catalog_no integer,
    content_type character varying(50),
    content_sub_type character varying(50),
    catalog_content_subject character varying(255),
    thumbnail_img_path character varying(255),
    catalog_content_cn text,
    thumbnail_img_path2 character varying(255),
    catalog_content_cn2 text,
    locgov_code character varying(20),
    display_yn character(1),
    banner_yn character(1),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_catalog_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_catalog_mng (
    catalog_year integer NOT NULL,
    catalog_no integer NOT NULL,
    display_yn character varying(5) DEFAULT 'N'::character varying NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_cheer_msg; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cheer_msg (
    cntr_sn character varying(50) NOT NULL,
    cheer_msg character varying(1000),
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_cntr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr (
    cntr_sn character varying(50) NOT NULL,
    cntr_de character varying(8) NOT NULL,
    user_id bigint NOT NULL,
    psitn_locgov_code character varying(10) NOT NULL,
    cntr_locgov_code character varying(10) NOT NULL,
    cntr_amt bigint DEFAULT 0 NOT NULL,
    cntr_point bigint DEFAULT 0 NOT NULL,
    cntr_blce_point bigint DEFAULT 0,
    point_end_de character varying(8),
    sttemnt_pay_de character varying(8),
    pay_valid_de character varying(8),
    cntr_path_code character varying(10),
    cntr_sttus_code character varying(10),
    cntrbtr_opinion_cn character varying(2000),
    setle_mth_code character varying(10),
    cntr_use_purps_code character varying(10),
    elctrn_pay_no character varying(30) NOT NULL,
    seoul_trget_at character varying(1),
    rcept_bank_code character varying(10),
    rcept_bank_nm character varying(100),
    rcepter_nm character varying(50),
    rtnpsnt_reqst_code character varying(10),
    rtnpsnt_reqst_at character varying(255),
    info_agre_at character varying(1),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone,
    delete_at character varying(1) DEFAULT 'N'::character varying NOT NULL,
    nts_sttus_mssage character varying(50),
    tmp_blce_point bigint DEFAULT 0,
    foreign_status_code character varying(2) DEFAULT '0'::character varying NOT NULL,
    prj_id bigint DEFAULT 0,
    dsgn_dntn_biz_id bigint DEFAULT 0,
    link_instt_cd character varying(20),
    sunap_procss_yn character varying(1) DEFAULT 'N'::character varying,
    cheer_msg character varying(100)
);


--
-- Name: g_cntr_chenap; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_chenap (
    id bigint NOT NULL,
    elctrn_pay_no character varying(30)
);


--
-- Name: g_cntr_chenap_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_cntr_chenap_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_cntr_chenap_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_cntr_chenap_id_seq OWNED BY public.g_cntr_chenap.id;


--
-- Name: g_cntr_lmtt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_lmtt (
    lmtt_bgn_de character varying(8) NOT NULL,
    lmtt_end_de character varying(8) NOT NULL,
    locgov_code character varying(10) NOT NULL,
    violt_resn_code character varying(10) NOT NULL,
    violt_resn_cn character varying(500),
    register_nm character varying(50),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_locgov_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_locgov_history (
    cntr_sn character varying(50) NOT NULL,
    locgov_hist_no integer,
    bfr_cntr_locgov_code character varying(10),
    aft_cntr_locgov_code character varying(10),
    bfr_psitn_locgov_code character varying(10),
    aft_psitn_locgov_code character varying(10),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_rcipt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_rcipt (
    cntr_sn character varying(20) NOT NULL,
    elctrn_pay_no character varying(30) NOT NULL,
    cntr_outpt_sn integer NOT NULL,
    issu_de character varying(8) NOT NULL,
    issu_co integer,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_reqmng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_reqmng (
    req_id bigint NOT NULL,
    login_id character varying(300) NOT NULL,
    user_name character varying(300),
    locgov_code character varying(10) NOT NULL,
    sttemnt_pay_de character varying(8) NOT NULL,
    cntr_amt bigint DEFAULT 0 NOT NULL,
    cntr_reqmng_code character varying(10),
    discription character varying(300),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone,
    use_yn character varying(1) DEFAULT 'Y'::character varying,
    req_status_code character varying(10) NOT NULL,
    appr_dt timestamp without time zone,
    cancle_dt timestamp without time zone,
    cntr_sn character varying(50),
    tax_sys_cancel_de character varying(8),
    related_doc_dpt_nm character varying(300),
    related_doc_num character varying(300),
    related_doc_de character varying(300)
);


--
-- Name: g_cntr_reqmng_req_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_cntr_reqmng_req_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_cntr_reqmng_req_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_cntr_reqmng_req_id_seq OWNED BY public.g_cntr_reqmng.req_id;


--
-- Name: g_cntr_tax_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_tax_log (
    log_id bigint NOT NULL,
    elctrn_pay_no character varying(30) NOT NULL,
    sttemnt_pay_de character varying(8) NOT NULL,
    cntr_amt bigint DEFAULT 0 NOT NULL,
    tax_status_code character varying(5),
    nts_res_code character varying(50),
    nts_res_mssage character varying(50),
    conb_cd character varying(2),
    cntr_type character varying(2),
    elcr_apl_cd character varying(2),
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_tax_log3; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_tax_log3 (
    elctrn_pay_no character varying(30),
    sttemnt_pay_de character varying(8),
    cntr_amt bigint DEFAULT 0 NOT NULL,
    tax_status_code character varying(5),
    nts_res_code character varying(50),
    nts_res_mssage character varying(50),
    conb_cd character varying(2),
    cntr_type character varying(2),
    elcr_apl_cd character varying(2),
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_tax_log_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_cntr_tax_log_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_cntr_tax_log_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_cntr_tax_log_log_id_seq OWNED BY public.g_cntr_tax_log.log_id;


--
-- Name: g_cntr_tax_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_tax_temp (
    cntr_sn character varying(50),
    sttemnt_pay_de character varying(8),
    conb_cd character varying(8) DEFAULT '43'::character varying,
    user_id bigint,
    mber_ci character varying(200),
    cntr_amt bigint DEFAULT 0 NOT NULL,
    elctrn_pay_no character varying(30),
    biz_no character varying(50),
    elcr_apl_cd character varying(2) DEFAULT ''::character varying,
    is_send integer DEFAULT 0,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_cntr_tax_temp_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_tax_temp_log (
    cntr_sn character varying(50),
    confirm_user_id bigint,
    elcr_apl_cd character varying(10),
    etc character varying(255),
    frst_regist_pnttm timestamp without time zone
);


--
-- Name: g_cntr_temp2; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_temp2 (
    user_id character varying(30) NOT NULL,
    cntr_sum_amt bigint DEFAULT 0 NOT NULL
);


--
-- Name: g_cntr_wegive; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_wegive (
    cntr_sn bigint NOT NULL,
    cntr_de character varying(100),
    cntr_amt bigint DEFAULT 0 NOT NULL,
    user_name character varying(100),
    mber_ci character varying(100),
    frst_reg_dt timestamp without time zone
);


--
-- Name: g_cntr_wegive_cntr_sn_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_cntr_wegive_cntr_sn_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_cntr_wegive_cntr_sn_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_cntr_wegive_cntr_sn_seq OWNED BY public.g_cntr_wegive.cntr_sn;


--
-- Name: g_ctbny_opratn; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_ctbny_opratn (
    regist_sn integer NOT NULL,
    locgov_code character varying(10) NOT NULL,
    bsns_purps_code character varying(10),
    bsns_nm character varying(100),
    bsns_cn character varying,
    rm character varying(2000),
    expndtr_de character varying(8),
    expndtr_amt integer,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_ctbny_opratn_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_ctbny_opratn_file (
    regist_file_id integer NOT NULL,
    regist_sn integer NOT NULL,
    file_nm character varying(200) NOT NULL,
    file_ty character varying(50),
    sort_ordr integer,
    orginl_file_nm character varying(200),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_ctbny_opratn_file_regist_file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_ctbny_opratn_file_regist_file_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_ctbny_opratn_file_regist_file_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_ctbny_opratn_file_regist_file_id_seq OWNED BY public.g_ctbny_opratn_file.regist_file_id;


--
-- Name: g_ctbny_opratn_regist_sn_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_ctbny_opratn_regist_sn_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_ctbny_opratn_regist_sn_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_ctbny_opratn_regist_sn_seq OWNED BY public.g_ctbny_opratn.regist_sn;


--
-- Name: g_ctbny_setup; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_ctbny_setup (
    stdr_year character varying(4) NOT NULL,
    locgov_code character varying(10) NOT NULL,
    lmt_amt integer NOT NULL,
    point_rate numeric(5,2) NOT NULL,
    point_valid_pd integer,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_disaster_zone; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_disaster_zone (
    locgov_code character varying(10),
    zone_id integer NOT NULL,
    locgov_nm character varying(100),
    f_disaster_st character varying(10),
    f_disaster_ed character varying(10),
    s_disater_st character varying(10),
    s_disaster_ed character varying(10)
);


--
-- Name: g_dsgn_dntn_biz_aprv_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_aprv_log (
    dsgn_dntn_biz_aprv_id bigint NOT NULL,
    dsgn_dntn_biz_id bigint NOT NULL,
    aprv_bfr_dsgn_dntn_biz_stts_cd character varying(10),
    aprv_aftr_dsgn_dntn_biz_stts_cd character varying(10) NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now()
);


--
-- Name: g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq OWNED BY public.g_dsgn_dntn_biz_aprv_log.dsgn_dntn_biz_aprv_id;


--
-- Name: g_dsgn_dntn_biz_cn_img_expln; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_cn_img_expln (
    dsgn_dntn_biz_id bigint NOT NULL,
    dsgn_dntn_biz_cn_img_id bigint NOT NULL,
    img_seq integer NOT NULL,
    img_expln character varying(5000) NOT NULL,
    frst_rgtr_id bigint,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq OWNED BY public.g_dsgn_dntn_biz_cn_img_expln.dsgn_dntn_biz_cn_img_id;


--
-- Name: g_dsgn_dntn_biz_dept_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_dept_mng (
    dsgn_dntn_biz_dept_id bigint NOT NULL,
    dsgn_dntn_biz_dept_nm character varying(50) NOT NULL,
    lclgv_cd character varying(10),
    use_yn character varying(5) DEFAULT 'Y'::character varying NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    dsgn_dntn_biz_dept_cd character varying(20)
);


--
-- Name: g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq OWNED BY public.g_dsgn_dntn_biz_dept_mng.dsgn_dntn_biz_dept_id;


--
-- Name: g_dsgn_dntn_biz_dept_mngr_mpng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_dept_mngr_mpng (
    user_id bigint NOT NULL,
    dsgn_dntn_biz_dept_id bigint NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_img_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_img_mng (
    dsgn_dntn_biz_img_id bigint NOT NULL,
    dsgn_dntn_biz_id bigint NOT NULL,
    img_nm character varying(255) NOT NULL,
    img_seq integer NOT NULL,
    frst_rgtr_id bigint,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq OWNED BY public.g_dsgn_dntn_biz_img_mng.dsgn_dntn_biz_img_id;


--
-- Name: g_dsgn_dntn_biz_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_mng (
    dsgn_dntn_biz_id bigint NOT NULL,
    dsgn_dntn_biz_ttl character varying(255),
    dsgn_dntn_biz_cn character varying,
    dsgn_dntn_biz_bgng_ymd character varying(8) NOT NULL,
    dsgn_dntn_biz_end_ymd character varying(8) NOT NULL,
    goal_amt bigint DEFAULT 0 NOT NULL,
    dsgn_dntn_biz_stts_cd character varying(10) NOT NULL,
    rls_yn character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    dsgn_dntn_biz_rprs_img character varying(255),
    dsgn_dntn_biz_se_cd character varying(20),
    lclgv_cd character varying(10),
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint,
    last_reg_dt timestamp without time zone,
    dsgn_dntn_biz_se_dtl_cd character varying(20),
    dsgn_dntn_biz_etc_cn character varying,
    dsgn_dntn_biz_dept_id bigint DEFAULT 0
);


--
-- Name: g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq OWNED BY public.g_dsgn_dntn_biz_mng.dsgn_dntn_biz_id;


--
-- Name: g_dsgn_dntn_biz_ntc_cn_img_expln; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_ntc_cn_img_expln (
    dsgn_dntn_biz_ntc_id bigint NOT NULL,
    img_seq integer NOT NULL,
    img_expln character varying(5000) NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_ntc_file_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_ntc_file_mng (
    dsgn_dntn_biz_ntc_id bigint NOT NULL,
    file_seq integer NOT NULL,
    file_nm character varying(255) NOT NULL,
    path_nm character varying(255) NOT NULL,
    orgnfl_nm character varying(255) NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_ntc_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_ntc_mng (
    dsgn_dntn_biz_ntc_id bigint NOT NULL,
    dsgn_dntn_biz_id bigint NOT NULL,
    dsgn_dntn_biz_ntc_ttl character varying(255) NOT NULL,
    dsgn_dntn_biz_ntc_cn character varying,
    rls_yn character varying(1) NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq OWNED BY public.g_dsgn_dntn_biz_ntc_mng.dsgn_dntn_biz_ntc_id;


--
-- Name: g_dsgn_dntn_biz_user_msg; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_dntn_biz_user_msg (
    dntn_sn character varying(20) NOT NULL,
    user_msg character varying(100),
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgn_prj_notice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgn_prj_notice (
    prj_notice_id bigint NOT NULL,
    dsgn_dntn_biz_id bigint NOT NULL,
    prj_notice_subject character varying(255),
    prj_notice_cn text,
    frst_regist_pnttm character varying(14)
);


--
-- Name: g_dsgn_prj_notice_prj_notice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgn_prj_notice_prj_notice_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgn_prj_notice_prj_notice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgn_prj_notice_prj_notice_id_seq OWNED BY public.g_dsgn_prj_notice.prj_notice_id;


--
-- Name: g_dsgncntr_part_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_part_mng (
    dsgncntr_part_id bigint NOT NULL,
    dsgncntr_part_name character varying(50) NOT NULL,
    locgov_code character varying(20),
    use_yn character varying(5) DEFAULT 'Y'::character varying NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgncntr_part_mng_dsgncntr_part_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgncntr_part_mng_dsgncntr_part_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgncntr_part_mng_dsgncntr_part_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgncntr_part_mng_dsgncntr_part_id_seq OWNED BY public.g_dsgncntr_part_mng.dsgncntr_part_id;


--
-- Name: g_dsgncntr_part_user_mppng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_part_user_mppng (
    user_id bigint NOT NULL,
    dsgncntr_part_id bigint NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgncntr_prj_img; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_prj_img (
    dsgncntr_prj_image_id bigint NOT NULL,
    prj_id bigint NOT NULL,
    image_name character varying(255) NOT NULL,
    ordering integer NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_register_pnttm timestamp without time zone DEFAULT now()
);


--
-- Name: g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq OWNED BY public.g_dsgncntr_prj_img.dsgncntr_prj_image_id;


--
-- Name: g_dsgncntr_prj_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_prj_mng (
    prj_id bigint NOT NULL,
    prj_subject character varying(255),
    prj_cn character varying,
    prj_st_dt character varying(8) NOT NULL,
    prj_ed_dt character varying(8) NOT NULL,
    target_amt bigint DEFAULT 0 NOT NULL,
    prj_status character varying(1) NOT NULL,
    display_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    prj_image character varying(255),
    bsns_type character varying(20),
    locgov_code character varying(20),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone,
    content_etc character varying,
    dsgncntr_part_id bigint DEFAULT 0,
    bsns_sub_type character varying(20)
);


--
-- Name: g_dsgncntr_prj_mng_prj_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgncntr_prj_mng_prj_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgncntr_prj_mng_prj_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgncntr_prj_mng_prj_id_seq OWNED BY public.g_dsgncntr_prj_mng.prj_id;


--
-- Name: g_dsgncntr_prj_notice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_prj_notice (
    prj_notice_id bigint NOT NULL,
    prj_id bigint NOT NULL,
    prj_notice_subject character varying(255) NOT NULL,
    prj_notice_cn character varying,
    display_yn character varying(5) DEFAULT 'Y'::character varying NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgncntr_prj_notice_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_prj_notice_file (
    prj_notice_id bigint NOT NULL,
    file_seq integer NOT NULL,
    file_name character varying(255) NOT NULL,
    path_name character varying(255) NOT NULL,
    org_file_name character varying(255) NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgncntr_prj_notice_img_desc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dsgncntr_prj_notice_img_desc (
    prj_notice_id bigint NOT NULL,
    img_desc character varying(5000) NOT NULL,
    img_seq integer NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_dsgncntr_prj_notice_prj_notice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_dsgncntr_prj_notice_prj_notice_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_dsgncntr_prj_notice_prj_notice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_dsgncntr_prj_notice_prj_notice_id_seq OWNED BY public.g_dsgncntr_prj_notice.prj_notice_id;


--
-- Name: g_dy_gramt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_dy_gramt (
    created_at character varying(17) NOT NULL,
    stdr_de character varying(8) NOT NULL,
    last_year_gramt bigint,
    now_year_gramt bigint
);


--
-- Name: g_giro; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_giro (
    locgov_code character varying(10) NOT NULL,
    locgov_nm character varying(50),
    use_instt_code character varying(2),
    giro_no character varying(7)
);


--
-- Name: g_honor_benefit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_honor_benefit (
    locgov_code character varying(50) NOT NULL,
    benefit_desc text,
    updated_date timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_honor_cntrbtr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_honor_cntrbtr (
    stdr_year character varying(4) NOT NULL,
    locgov_code character varying(10) NOT NULL,
    user_id bigint NOT NULL,
    honor_cntrbtr_level_code character varying(10),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_honor_cntrbtr_stdr; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_honor_cntrbtr_stdr (
    locgov_code character varying(10) NOT NULL,
    stdr_year character varying(4) NOT NULL,
    cntr_amt integer NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_instt_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_instt_code (
    locgov_code character varying(10) NOT NULL,
    upper_locgov_nm character varying(50),
    locgov_nm character varying(50),
    locgov_mapng_code character varying(10),
    administ_instt_code character varying(10),
    instt_mapng_code character varying(10),
    use_at character varying(1),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_intrst_locgov; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_intrst_locgov (
    user_id bigint NOT NULL,
    locgov_code character varying(10) NOT NULL,
    regist_de character varying(8) NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_lclgv_hnr_user_rwrd_img_expln; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_lclgv_hnr_user_rwrd_img_expln (
    lclgv_cd character varying(10) NOT NULL,
    img_seq integer NOT NULL,
    img_expln character varying(5000) NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_lclgv_hnr_user_stng_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_lclgv_hnr_user_stng_mng (
    lclgv_cd character varying(10) NOT NULL,
    gld_grd_dntn_amt integer DEFAULT 0 NOT NULL,
    slvr_grd_dntn_amt integer DEFAULT 0 NOT NULL,
    brnz_grd_dntn_amt integer DEFAULT 0 NOT NULL,
    hnr_user_rwrd character varying,
    rprs_img_nm character varying(255),
    hnr_user_slctn_se_cd character varying(20),
    use_yn character varying(5) DEFAULT 'N'::character varying NOT NULL,
    frst_rgtr_id bigint DEFAULT 0 NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint DEFAULT 0 NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    hnr_user_stng_ttl character varying(255)
);


--
-- Name: g_link_instt_enc_key_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_link_instt_enc_key_mng (
    enc_key_ver character varying(10) NOT NULL,
    enc_key character varying(1000) NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_locgov; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_locgov (
    locgov_code character varying(10) NOT NULL,
    upper_locgov_nm character varying(50),
    locgov_nm character varying(50) NOT NULL,
    upper_locgov_code character varying(10) NOT NULL,
    locgov_intrcn_cn character varying(4000),
    charger_cttpc character varying(300),
    charger_nm character varying(300),
    charger_psitn_dept character varying(50),
    locgov_hmpg character varying(100),
    locgov_popltn_co character varying(100),
    locgov_ar character varying(100),
    locgov_spcprd character varying(500),
    gcct_use_at character varying(1),
    etrcsh_use_at character varying(1),
    locgov_budget_amt bigint DEFAULT 0,
    bizrno character varying(50),
    locgov_zip character varying(10),
    bass_adres character varying(100),
    dtl_adres character varying(100),
    achlqr_sle_at character varying(1),
    use_at character varying(1),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone,
    process_dept_code character varying(30),
    administ_instt_code character varying(10),
    offcs_nm character varying(200),
    offcs_file_nm character varying(200),
    orginl_file_nm character varying(200),
    stdr_1level_amt integer DEFAULT 0,
    stdr_2level_amt integer DEFAULT 0,
    stdr_3level_amt integer DEFAULT 0,
    fis_sp character varying(10),
    charger_email character varying(100),
    ordering bigint
);


--
-- Name: g_locgov_dept_hist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_locgov_dept_hist (
    locgov_code character varying(10) NOT NULL,
    dept_hist_no integer NOT NULL,
    process_dept_code character varying(20),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_locgov_fav_item_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_locgov_fav_item_mng (
    locgov_code character varying(10) NOT NULL,
    catalog_year integer NOT NULL,
    catalog_no integer NOT NULL,
    delete_yn character varying(5) DEFAULT 'N'::character varying NOT NULL,
    item_id bigint NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint NOT NULL,
    last_updt_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_locgov_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_locgov_image (
    locgov_code character varying(10) NOT NULL,
    pc_file_name character varying(255),
    mobile_file_name character varying(255),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_next_buga_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_next_buga_request (
    link_mng_key character varying(50) NOT NULL,
    sgb_cd character varying(7) NOT NULL,
    link_trgt_cd character varying(15) NOT NULL,
    dpt_cd character varying(7) NOT NULL,
    spcl_fis_biz_cd character varying(4),
    fyr character varying(4),
    act_se_cd character varying(2),
    rprs_txm_cd character varying(6),
    oper_item_cd character varying(3),
    lvy_ymd character varying(8),
    frst_pct_amt character varying(15),
    frst_pid_ymd character varying(8),
    pyr_se_cd character varying(2),
    pyr_no character varying(13),
    pyr_nm character varying(200),
    rprs_pyr_no character varying(24),
    rprs_pyr_nm character varying(200),
    pyr_stt_cd character varying(2),
    lotno_road_addr_se_cd character varying(2),
    zip character varying(6),
    road_nm_cd character varying(12),
    bmno character varying(5),
    bsno character varying(5),
    stdg_cd character varying(10),
    dong_cd character varying(10),
    road_nm_daddr character varying(1000),
    gl_nm character varying(500),
    mng_item_cn1 character varying(200),
    buga_status_cd character varying(3),
    link_rst_cd character varying(30),
    link_rst_msg character varying(200),
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_next_buga_request_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_next_buga_request_temp (
    link_mng_key character varying(50) NOT NULL,
    sgb_cd character varying(7) NOT NULL,
    link_trgt_cd character varying(15) NOT NULL,
    dpt_cd character varying(7) NOT NULL,
    spcl_fis_biz_cd character varying(4),
    fyr character varying(4),
    act_se_cd character varying(2),
    rprs_txm_cd character varying(6),
    oper_item_cd character varying(3),
    lvy_ymd character varying(8),
    frst_pct_amt character varying(15),
    frst_pid_ymd character varying(8),
    pyr_se_cd character varying(2),
    pyr_no character varying(13),
    pyr_nm character varying(200),
    rprs_pyr_no character varying(24),
    rprs_pyr_nm character varying(200),
    pyr_stt_cd character varying(2),
    lotno_road_addr_se_cd character varying(2),
    zip character varying(6),
    road_nm_cd character varying(12),
    bmno character varying(5),
    bsno character varying(5),
    stdg_cd character varying(10),
    dong_cd character varying(10),
    road_nm_daddr character varying(20),
    gl_nm character varying(500),
    mng_item_cn1 character varying(200),
    buga_status_cd character varying(3),
    link_rst_cd character varying(30),
    link_rst_msg character varying(200),
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_next_sunap_response; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_next_sunap_response (
    link_mng_key character varying(50) NOT NULL,
    sgb_cd character varying(7) NOT NULL,
    sgb_nm character varying(60),
    taxn_no character varying(50),
    unty_taxn_no character varying(31),
    dpt_cd character varying(7) NOT NULL,
    dpt_nm character varying(200),
    spcl_fis_biz_cd character varying(4) NOT NULL,
    spcl_fis_biz_nm character varying(100),
    fyr character varying(4),
    act_se_cd character varying(2),
    act_se_nm character varying(100),
    rprs_txm_cd character varying(6),
    rprs_txm_nm character varying(100),
    oper_item_cd character varying(3),
    oper_item_nm character varying(100),
    lvy_no character varying(6),
    itm_no character varying(2),
    epay_no character varying(19) NOT NULL,
    rcvmt_no character varying(2),
    rcvmt_se_cd character varying(2),
    rcvmt_se_nm character varying(100),
    rcvmt_ymd character varying(8),
    act_ymd character varying(8),
    tsf_ymd character varying(8),
    rcvmt_pct_amt character varying(15),
    rcvmt_adtn_amt character varying(15),
    rcvmt_intr_amt character varying(15),
    bank_nm character varying(30),
    rcvmt_ty_cd character varying(2),
    rcvmt_ty character varying(300),
    rsve_item1 character varying(200),
    rsve_item2 character varying(200),
    rsve_item3 character varying(200),
    rsve_item4 character varying(200),
    rsve_item5 character varying(200),
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_pay_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_pay_log (
    pay_log_id bigint NOT NULL,
    elctrn_pay_no character varying(30) NOT NULL,
    pay_method character varying(100),
    pay_start_dt timestamp without time zone,
    pay_end_dt timestamp without time zone,
    pay_process character varying(100),
    frst_reg_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_mdfcn_id bigint,
    last_mdfcn_dt timestamp without time zone
);


--
-- Name: g_pay_log_pay_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_pay_log_pay_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_pay_log_pay_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_pay_log_pay_log_id_seq OWNED BY public.g_pay_log.pay_log_id;


--
-- Name: g_prj_content_img_desc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_prj_content_img_desc (
    prj_content_img_id bigint NOT NULL,
    prj_id bigint NOT NULL,
    img_desc character varying(5000) NOT NULL,
    img_seq integer NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_prj_content_img_desc_prj_content_img_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_prj_content_img_desc_prj_content_img_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_prj_content_img_desc_prj_content_img_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_prj_content_img_desc_prj_content_img_id_seq OWNED BY public.g_prj_content_img_desc.prj_content_img_id;


--
-- Name: g_relay_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_relay_log (
    relay_log_id integer NOT NULL,
    user_id bigint NOT NULL,
    relay_type character varying(25) NOT NULL,
    cntr_locgov_code character varying(10) NOT NULL,
    cntr_sn character varying(20),
    relay_result_code character varying(10) NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_relay_log_relay_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_relay_log_relay_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_relay_log_relay_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_relay_log_relay_log_id_seq OWNED BY public.g_relay_log.relay_log_id;


--
-- Name: gif_etax_daesa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_etax_daesa (
    if_no character varying(30) NOT NULL,
    epay_no character varying(19) NOT NULL,
    com_req_meche character varying(20) NOT NULL,
    com_req_dt character varying(8) NOT NULL,
    com_req_tm character varying(6) NOT NULL,
    com_pay_msg_no character varying(12) NOT NULL,
    access_key character varying(32) NOT NULL,
    org_c character varying(32) NOT NULL,
    semok_cd character varying(8),
    sunap_amt integer DEFAULT 0 NOT NULL,
    sunap_dt character varying(8) NOT NULL,
    rst_cd character varying(3) NOT NULL,
    rst_msg character varying(100),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: gif_etax_sunap; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_etax_sunap (
    if_no character varying(30) NOT NULL,
    epay_no character varying(19) NOT NULL,
    com_req_meche character varying(20) NOT NULL,
    com_req_dt character varying(8) NOT NULL,
    com_req_tm character varying(6) NOT NULL,
    com_pay_msg_no character varying(12) NOT NULL,
    access_key character varying(32) NOT NULL,
    org_c character varying(32) NOT NULL,
    sunap_yn character varying(32) NOT NULL,
    sunap_amt integer DEFAULT 0 NOT NULL,
    sunap_dt character varying(8) NOT NULL,
    rst_cd character varying(3) NOT NULL,
    rst_msg character varying(100),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: gif_etax_sunap_etc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_etax_sunap_etc (
    if_no character varying(30) NOT NULL,
    epay_no character varying(19) NOT NULL,
    com_req_meche character varying(20) NOT NULL,
    com_req_dt character varying(8) NOT NULL,
    com_req_tm character varying(6) NOT NULL,
    com_pay_msg_no character varying(12) NOT NULL,
    access_key character varying(32) NOT NULL,
    org_c character varying(32) NOT NULL,
    sunap_yn character varying(32) NOT NULL,
    sunap_amt integer DEFAULT 0 NOT NULL,
    sunap_dt character varying(8) NOT NULL,
    rst_cd character varying(3) NOT NULL,
    rst_msg character varying(100),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: gif_external_mst; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_external_mst (
    if_no character varying(30) NOT NULL,
    enapbu_no character varying(19) NOT NULL,
    status character varying(1) NOT NULL,
    rty_cnt integer DEFAULT 0 NOT NULL,
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: gif_hometax; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_hometax (
    if_no character varying(30) NOT NULL,
    elctrn_pay_no character varying(19) NOT NULL,
    user_id bigint NOT NULL,
    dnt_dt character varying(8) NOT NULL,
    conb_cd character varying(2) NOT NULL,
    dnt_amt character varying(20) NOT NULL,
    cntr_type character varying(2) NOT NULL,
    mem_ci character varying(300) NOT NULL,
    biz_no character varying(300) NOT NULL,
    result_code character varying(100),
    result_msg character varying(100),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: gif_seoul; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_seoul (
    if_no character varying(30) NOT NULL,
    enapbu_no character varying(19) NOT NULL,
    sigu_cd character varying(7) NOT NULL,
    semok_cd character varying(8) NOT NULL,
    tax_ym character varying(6) NOT NULL,
    tax_gubun character varying(1) NOT NULL,
    sido_cd character varying(2) NOT NULL,
    nap_nm character varying(80) NOT NULL,
    nap_gubun character varying(2) NOT NULL,
    tax_amt integer DEFAULT 0 NOT NULL,
    sise integer DEFAULT 0 NOT NULL,
    reside_status character varying(2) NOT NULL,
    mul_gubun character varying(2) NOT NULL,
    mul_nm character varying(100) NOT NULL,
    book_no character varying(30),
    sys_gubun character varying(10),
    error_cd character varying(3) NOT NULL,
    error_msg character varying(2000),
    insert_key character varying(6),
    insert_ak character varying(15),
    result_cnt character varying(10),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone,
    nap_id character varying(1000)
);


--
-- Name: gif_stnd_buga_now; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_stnd_buga_now (
    if_no character varying(30) NOT NULL,
    elct_pay_no character varying(19) NOT NULL,
    com_msg_len character varying(8) NOT NULL,
    com_if_id character varying(15) NOT NULL,
    com_source character varying(11) NOT NULL,
    com_target character varying(11) NOT NULL,
    com_msg_key character varying(38) NOT NULL,
    com_type_cd character varying(3) NOT NULL,
    com_rst_cd character varying(3) NOT NULL,
    sys_cd character varying(4) NOT NULL,
    dept_cd character varying(11) NOT NULL,
    fisyy character varying(4) NOT NULL,
    fis_sp character varying(2) NOT NULL,
    ptcl_cd character varying(6) NOT NULL,
    imps_dt character varying(8) NOT NULL,
    init_prcp_tax_amt integer NOT NULL,
    lst_prcp_tax_amt integer NOT NULL,
    init_due_dt character varying(8) NOT NULL,
    lst_due_dt integer,
    af_due_dt character varying(8),
    af_due_amt integer,
    imps_sp character varying(2) NOT NULL,
    decs_sp character varying(2) NOT NULL,
    txpr_sp character varying(2) NOT NULL,
    txpr_tel_no character varying(15),
    txpr_mphn_no character varying(15),
    txpr_eml character varying(30),
    new_addr_yn character varying(1) NOT NULL,
    txpr_road_cd character varying(12) NOT NULL,
    txpr_bd_flr_sp character varying(1),
    txpr_bd_prcp_no character varying(5) NOT NULL,
    txpr_bd_sub_no character varying(5) NOT NULL,
    stat_cd character varying(2) NOT NULL,
    spcl_fis_biz_cd character varying(5) NOT NULL,
    txpr_zip_no character varying(5) NOT NULL,
    txpr_lglvil_cd character varying(10),
    txpr_twnvil_cd character varying(10),
    txpr_mt character varying(2),
    txpr_addr_no character varying(4),
    txpr_addr_ho character varying(4),
    txpr_spcl_addr character varying(100),
    txpr_spcl_addr_dong character varying(20),
    txpr_spcl_addr_ho character varying(10),
    txpr_addr_tong character varying(3),
    txpr_addr_ban character varying(3),
    txpr_bd_mng_no character varying(25) NOT NULL,
    obj_nm character varying(100) NOT NULL,
    tax_obj_sp character varying(20) NOT NULL,
    tax_obj_new_addr_yn character varying(1) NOT NULL,
    mng_htm1 character varying(100),
    mng_htm2 character varying(30),
    mng_htm3 character varying(100),
    mng_htm4 character varying(100),
    mng_htm5 character varying(30),
    mng_htm6 character varying(30),
    rmk character varying(255),
    init_wrkr_id character varying(20),
    bank_cd character varying(7),
    txpr_full_addr character varying(300),
    txpr_basic_addr character varying(500),
    txpr_basic_dtl_addr character varying(500),
    result_code character varying(3) NOT NULL,
    result_msg character varying(500),
    imps_key character varying(500),
    result character varying(500),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone,
    txpr_no character varying(1000),
    txpr_nm character varying(1000),
    txpr_dtl_addr character varying(1000)
);


--
-- Name: gif_stnd_jijache_now; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_stnd_jijache_now (
    locgov_code character varying(10) NOT NULL,
    web_url character varying(200) NOT NULL
);


--
-- Name: gif_stnd_sunap_now; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gif_stnd_sunap_now (
    if_no character varying(30) NOT NULL,
    elct_pay_no character varying(19) NOT NULL,
    com_msg_len character varying(8) NOT NULL,
    com_if_id character varying(15) NOT NULL,
    com_source character varying(11) NOT NULL,
    com_target character varying(11) NOT NULL,
    com_msg_key character varying(38) NOT NULL,
    com_type_cd character varying(3) NOT NULL,
    com_rst_cd character varying(3) NOT NULL,
    conn_key character varying(30) NOT NULL,
    result_code character varying(30) NOT NULL,
    result_msg character varying(100),
    result character varying(50),
    rcpt_prcp_tax_amt integer,
    rcpt_add_amt integer,
    divd_recpltt_amt integer,
    fis_dt character varying(8),
    wrk_dt character varying(8),
    rcpt_sp character varying(2),
    rcpt_dt character varying(8),
    rcpt_typ character varying(3),
    bank_cd character varying(8),
    bndl_no character varying(3),
    trnr_dt character varying(8),
    if_st_dt timestamp without time zone DEFAULT now() NOT NULL,
    if_ed_dt timestamp without time zone
);


--
-- Name: op_common_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_common_code (
    code_type character varying(50) NOT NULL,
    code_language character varying(10) NOT NULL,
    id character varying(50) NOT NULL,
    label character varying(255),
    detail text,
    ordering integer,
    use_yn character(1),
    up_id character varying(50),
    code_value character varying(255),
    extension_code character varying(50),
    mapping_code character varying(50)
);


--
-- Name: op_event_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_event_code (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    change_code character varying(100),
    contents character varying(500),
    event_code character varying(20) NOT NULL,
    redirection bigint,
    type character varying(10) NOT NULL,
    user_id bigint,
    utm_query_string character varying(255),
    campaign_id bigint
);


--
-- Name: op_event_code_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_event_code_log (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    channel character varying(255),
    code_type character varying(10) NOT NULL,
    event_code character varying(20),
    event_uid character varying(100) NOT NULL,
    item_user_code character varying(50),
    log_detail character varying(100),
    log_type character varying(10) NOT NULL,
    order_code character varying(50),
    source_user_id bigint,
    uid character varying(100),
    user_id bigint,
    utm_campaign character varying(255),
    utm_content character varying(255),
    utm_item character varying(255),
    utm_medium character varying(255),
    utm_source character varying(255)
);


--
-- Name: op_event_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_event_item (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    version bigint,
    item_id integer NOT NULL,
    ordering integer NOT NULL,
    event_id bigint
);


--
-- Name: op_event_reply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_event_reply (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    version bigint,
    content character varying(2000) NOT NULL,
    data_status character varying(10) NOT NULL,
    event_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: op_honor_view_hist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_honor_view_hist (
    view_dt timestamp without time zone NOT NULL,
    user_id bigint NOT NULL,
    lclgv_cd character varying(10) NOT NULL
);


--
-- Name: op_island; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_island (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    address character varying(255),
    island_type character varying(20),
    zipcode character varying(7)
);


--
-- Name: op_sido_mapping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_sido_mapping (
    sido_mapping_group_key integer,
    sido_name character varying(50),
    sido_data character varying(50)
);


--
-- Name: op_spel_dstr_zn; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_spel_dstr_zn (
    id bigint NOT NULL,
    psdnt_noti_no bigint NOT NULL,
    upper_locgov_nm character varying(30),
    upper_locgov_code character varying(10),
    locgov_nm character varying(30),
    locgov_code character varying(10),
    noti_reason character varying(50),
    noti_date character varying(8),
    end_date character varying(8),
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: op_spel_dstr_zn_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_spel_dstr_zn_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_spel_dstr_zn_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_spel_dstr_zn_id_seq OWNED BY public.op_spel_dstr_zn.id;


--
-- Name: view_search_locgov; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.view_search_locgov AS
 SELECT locgov_code,
    upper_locgov_nm,
    locgov_nm,
    locgov_popltn_co,
    locgov_hmpg,
    locgov_intrcn_cn,
    concat('/donation/map-select.html?locgovCode=', locgov_code) AS detail_url
   FROM public.g_locgov;


--
-- Name: g_catalog_card_news_mng card_news_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_card_news_mng ALTER COLUMN card_news_id SET DEFAULT nextval('public.g_catalog_card_news_mng_card_news_id_seq'::regclass);


--
-- Name: g_cntr_chenap id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_chenap ALTER COLUMN id SET DEFAULT nextval('public.g_cntr_chenap_id_seq'::regclass);


--
-- Name: g_cntr_reqmng req_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_reqmng ALTER COLUMN req_id SET DEFAULT nextval('public.g_cntr_reqmng_req_id_seq'::regclass);


--
-- Name: g_cntr_tax_log log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_tax_log ALTER COLUMN log_id SET DEFAULT nextval('public.g_cntr_tax_log_log_id_seq'::regclass);


--
-- Name: g_cntr_wegive cntr_sn; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_wegive ALTER COLUMN cntr_sn SET DEFAULT nextval('public.g_cntr_wegive_cntr_sn_seq'::regclass);


--
-- Name: g_ctbny_opratn regist_sn; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn ALTER COLUMN regist_sn SET DEFAULT nextval('public.g_ctbny_opratn_regist_sn_seq'::regclass);


--
-- Name: g_ctbny_opratn_file regist_file_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn_file ALTER COLUMN regist_file_id SET DEFAULT nextval('public.g_ctbny_opratn_file_regist_file_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_aprv_log dsgn_dntn_biz_aprv_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_aprv_log ALTER COLUMN dsgn_dntn_biz_aprv_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_cn_img_expln dsgn_dntn_biz_cn_img_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_cn_img_expln ALTER COLUMN dsgn_dntn_biz_cn_img_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_dept_mng dsgn_dntn_biz_dept_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_dept_mng ALTER COLUMN dsgn_dntn_biz_dept_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_img_mng dsgn_dntn_biz_img_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_img_mng ALTER COLUMN dsgn_dntn_biz_img_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_mng dsgn_dntn_biz_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_mng ALTER COLUMN dsgn_dntn_biz_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq'::regclass);


--
-- Name: g_dsgn_dntn_biz_ntc_mng dsgn_dntn_biz_ntc_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_ntc_mng ALTER COLUMN dsgn_dntn_biz_ntc_id SET DEFAULT nextval('public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq'::regclass);


--
-- Name: g_dsgn_prj_notice prj_notice_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_prj_notice ALTER COLUMN prj_notice_id SET DEFAULT nextval('public.g_dsgn_prj_notice_prj_notice_id_seq'::regclass);


--
-- Name: g_dsgncntr_part_mng dsgncntr_part_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_part_mng ALTER COLUMN dsgncntr_part_id SET DEFAULT nextval('public.g_dsgncntr_part_mng_dsgncntr_part_id_seq'::regclass);


--
-- Name: g_dsgncntr_prj_img dsgncntr_prj_image_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_img ALTER COLUMN dsgncntr_prj_image_id SET DEFAULT nextval('public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq'::regclass);


--
-- Name: g_dsgncntr_prj_mng prj_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_mng ALTER COLUMN prj_id SET DEFAULT nextval('public.g_dsgncntr_prj_mng_prj_id_seq'::regclass);


--
-- Name: g_dsgncntr_prj_notice prj_notice_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_notice ALTER COLUMN prj_notice_id SET DEFAULT nextval('public.g_dsgncntr_prj_notice_prj_notice_id_seq'::regclass);


--
-- Name: g_pay_log pay_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_pay_log ALTER COLUMN pay_log_id SET DEFAULT nextval('public.g_pay_log_pay_log_id_seq'::regclass);


--
-- Name: g_prj_content_img_desc prj_content_img_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_prj_content_img_desc ALTER COLUMN prj_content_img_id SET DEFAULT nextval('public.g_prj_content_img_desc_prj_content_img_id_seq'::regclass);


--
-- Name: g_relay_log relay_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_relay_log ALTER COLUMN relay_log_id SET DEFAULT nextval('public.g_relay_log_relay_log_id_seq'::regclass);


--
-- Name: op_spel_dstr_zn id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_spel_dstr_zn ALTER COLUMN id SET DEFAULT nextval('public.op_spel_dstr_zn_id_seq'::regclass);


--
-- Data for Name: donation_levy; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.donation_levy (cntr_sn, buga_no, buga_date, sunap_yn, sunap_date, nts_status, nts_receipt_no, nts_registered_date, nts_error_message) FROM stdin;
D202501151030001000	MOCK-BUGA-D202501151030001000	2026-08-24 17:37:33.84218	Y	2026-08-24 17:37:33.843263	REGISTERED	MOCK-NTS-D202501151030001000	2026-08-24 17:37:33.909399	\N
D202502101030001001	MOCK-BUGA-D202502101030001001	2026-08-24 17:37:33.922731	Y	2026-08-24 17:37:33.923794	REGISTERED	MOCK-NTS-D202502101030001001	2026-08-24 17:37:33.950121	\N
D202503051030001002	MOCK-BUGA-D202503051030001002	2026-08-24 17:37:33.966598	Y	2026-08-24 17:37:33.967686	REGISTERED	MOCK-NTS-D202503051030001002	2026-08-24 17:37:33.983905	\N
D202504201030001000	MOCK-BUGA-D202504201030001000	2026-08-24 17:37:33.998367	Y	2026-08-24 17:37:33.999486	REGISTERED	MOCK-NTS-D202504201030001000	2026-08-24 17:37:34.018448	\N
D202505121030001001	MOCK-BUGA-D202505121030001001	2026-08-24 17:37:34.052208	Y	2026-08-24 17:37:34.054236	REGISTERED	MOCK-NTS-D202505121030001001	2026-08-24 17:37:34.070996	\N
D202506181030001002	MOCK-BUGA-D202506181030001002	2026-08-24 17:37:34.084474	Y	2026-08-24 17:37:34.08608	REGISTERED	MOCK-NTS-D202506181030001002	2026-08-24 17:37:34.10167	\N
D202507251030001000	MOCK-BUGA-D202507251030001000	2026-08-24 17:37:34.114989	Y	2026-08-24 17:37:34.116076	REGISTERED	MOCK-NTS-D202507251030001000	2026-08-24 17:37:34.141138	\N
D202508031030001001	MOCK-BUGA-D202508031030001001	2026-08-24 17:37:34.168748	Y	2026-08-24 17:37:34.174048	REGISTERED	MOCK-NTS-D202508031030001001	2026-08-24 17:37:34.211828	\N
\.


--
-- Data for Name: g_adm_locgov; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_adm_locgov (adm_cd, adm_sect_nm, locgov_code) FROM stdin;
\.


--
-- Data for Name: g_agency_login_confirm_info; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_agency_login_confirm_info (user_session_id, valid_access_cd, private_key, manager_id, cntrbtr_mobile, cntrbtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_catalog_card_news_img_desc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_catalog_card_news_img_desc (card_news_id, img_desc, img_seq, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_catalog_card_news_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_catalog_card_news_mng (card_news_id, catalog_year, catalog_no, card_news_subject, card_news_cn, delete_yn, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_catalog_content_img_desc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_catalog_content_img_desc (catalog_content_id, img_desc, img_seq, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_catalog_content_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_catalog_content_mng (catalog_content_id, catalog_year, catalog_no, content_type, content_sub_type, catalog_content_subject, thumbnail_img_path, catalog_content_cn, thumbnail_img_path2, catalog_content_cn2, locgov_code, display_yn, banner_yn, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_catalog_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_catalog_mng (catalog_year, catalog_no, display_yn, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cheer_msg; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cheer_msg (cntr_sn, cheer_msg, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr (cntr_sn, cntr_de, user_id, psitn_locgov_code, cntr_locgov_code, cntr_amt, cntr_point, cntr_blce_point, point_end_de, sttemnt_pay_de, pay_valid_de, cntr_path_code, cntr_sttus_code, cntrbtr_opinion_cn, setle_mth_code, cntr_use_purps_code, elctrn_pay_no, seoul_trget_at, rcept_bank_code, rcept_bank_nm, rcepter_nm, rtnpsnt_reqst_code, rtnpsnt_reqst_at, info_agre_at, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, delete_at, nts_sttus_mssage, tmp_blce_point, foreign_status_code, prj_id, dsgn_dntn_biz_id, link_instt_cd, sunap_procss_yn, cheer_msg) FROM stdin;
D202501151030001000	20250115	1000	11230	11230	1500000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202501151030001000	\N	\N	\N	\N	\N	\N	\N	\N	2025-01-15 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202502101030001001	20250210	1001	26350	26350	800000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202502101030001001	\N	\N	\N	\N	\N	\N	\N	\N	2025-02-10 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202503051030001002	20250305	1002	46150	46150	1200000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202503051030001002	\N	\N	\N	\N	\N	\N	\N	\N	2025-03-05 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202504201030001000	20250420	1000	50000	50000	900000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202504201030001000	\N	\N	\N	\N	\N	\N	\N	\N	2025-04-20 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202505121030001001	20250512	1001	11230	11230	2000000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202505121030001001	\N	\N	\N	\N	\N	\N	\N	\N	2025-05-12 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202506181030001002	20250618	1002	26350	26350	1100000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202506181030001002	\N	\N	\N	\N	\N	\N	\N	\N	2025-06-18 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202507251030001000	20250725	1000	46150	46150	1090000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202507251030001000	\N	\N	\N	\N	\N	\N	\N	\N	2025-07-25 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
D202508031030001001	20250803	1001	11230	11230	1100000	0	0	\N	\N	\N	100	COMPLETED	\N	\N	\N	D202508031030001001	\N	\N	\N	\N	\N	\N	\N	\N	2025-08-03 10:30:00	\N	\N	N	\N	0	0	0	0	\N	N	\N
\.


--
-- Data for Name: g_cntr_chenap; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_chenap (id, elctrn_pay_no) FROM stdin;
\.


--
-- Data for Name: g_cntr_lmtt; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_lmtt (lmtt_bgn_de, lmtt_end_de, locgov_code, violt_resn_code, violt_resn_cn, register_nm, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_locgov_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_locgov_history (cntr_sn, locgov_hist_no, bfr_cntr_locgov_code, aft_cntr_locgov_code, bfr_psitn_locgov_code, aft_psitn_locgov_code, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_rcipt; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_rcipt (cntr_sn, elctrn_pay_no, cntr_outpt_sn, issu_de, issu_co, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_reqmng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_reqmng (req_id, login_id, user_name, locgov_code, sttemnt_pay_de, cntr_amt, cntr_reqmng_code, discription, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, use_yn, req_status_code, appr_dt, cancle_dt, cntr_sn, tax_sys_cancel_de, related_doc_dpt_nm, related_doc_num, related_doc_de) FROM stdin;
\.


--
-- Data for Name: g_cntr_tax_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_tax_log (log_id, elctrn_pay_no, sttemnt_pay_de, cntr_amt, tax_status_code, nts_res_code, nts_res_mssage, conb_cd, cntr_type, elcr_apl_cd, frst_regist_pnttm, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_tax_log3; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_tax_log3 (elctrn_pay_no, sttemnt_pay_de, cntr_amt, tax_status_code, nts_res_code, nts_res_mssage, conb_cd, cntr_type, elcr_apl_cd, frst_regist_pnttm, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_tax_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_tax_temp (cntr_sn, sttemnt_pay_de, conb_cd, user_id, mber_ci, cntr_amt, elctrn_pay_no, biz_no, elcr_apl_cd, is_send, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_tax_temp_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_tax_temp_log (cntr_sn, confirm_user_id, elcr_apl_cd, etc, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_temp2; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_temp2 (user_id, cntr_sum_amt) FROM stdin;
\.


--
-- Data for Name: g_cntr_wegive; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_wegive (cntr_sn, cntr_de, cntr_amt, user_name, mber_ci, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_ctbny_opratn; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_ctbny_opratn (regist_sn, locgov_code, bsns_purps_code, bsns_nm, bsns_cn, rm, expndtr_de, expndtr_amt, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
1000	46150	VULNERABLE	순천시 취약계층 난방비 지원	독거노인 및 취약계층 가구 200세대에 겨울철 난방비를 지원했습니다.	\N	20260210	12000000	\N	2026-08-24 05:11:57.346638	\N	2026-08-24 05:11:57.346638
1001	46150	YOUTH	순천만 청년 창업 지원	청년 창업가 10팀에게 초기 사업화 자금을 지원했습니다.	\N	20260415	8000000	\N	2026-08-24 05:11:57.346638	\N	2026-08-24 05:11:57.346638
1002	11230	COMMUNITY	강남구 마을공동체 활성화	주민자치회 주관 마을축제 및 공동체 프로그램을 운영했습니다.	\N	20260320	5000000	\N	2026-08-24 05:11:57.346638	\N	2026-08-24 05:11:57.346638
1003	26350	WELFARE	해운대구 주민복리 증진사업	경로당 냉난방기 교체 및 편의시설을 개선했습니다.	경로당 12개소	20260505	6000000	\N	2026-08-24 05:11:57.346638	\N	2026-08-24 05:11:57.346638
\.


--
-- Data for Name: g_ctbny_opratn_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_ctbny_opratn_file (regist_file_id, regist_sn, file_nm, file_ty, sort_ordr, orginl_file_nm, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_ctbny_setup; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_ctbny_setup (stdr_year, locgov_code, lmt_amt, point_rate, point_valid_pd, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
2026	11230	5000000	30.00	\N	\N	2026-08-24 05:11:57.142983	\N	\N
2026	26350	3000000	30.00	\N	\N	2026-08-24 05:11:57.142983	\N	\N
2026	36110	3000000	30.00	\N	\N	2026-08-24 05:11:57.142983	\N	\N
2026	50000	3000000	30.00	\N	\N	2026-08-24 05:11:57.142983	\N	\N
2026	46150	3000000	30.00	\N	\N	2026-08-24 05:11:57.142983	\N	\N
\.


--
-- Data for Name: g_disaster_zone; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_disaster_zone (locgov_code, zone_id, locgov_nm, f_disaster_st, f_disaster_ed, s_disater_st, s_disaster_ed) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_aprv_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_aprv_log (dsgn_dntn_biz_aprv_id, dsgn_dntn_biz_id, aprv_bfr_dsgn_dntn_biz_stts_cd, aprv_aftr_dsgn_dntn_biz_stts_cd, frst_rgtr_id, frst_reg_dt, frst_regist_pnttm) FROM stdin;
1000	204	OPEN	CLOSED	0	2026-08-24 14:45:53.061838	2026-08-24 14:45:52.965962
1001	205	OPEN	CLOSED	0	2026-08-24 14:45:53.091701	2026-08-24 14:45:52.965962
1002	206	OPEN	CLOSED	0	2026-08-24 14:45:53.097546	2026-08-24 14:45:52.965962
\.


--
-- Data for Name: g_dsgn_dntn_biz_cn_img_expln; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_cn_img_expln (dsgn_dntn_biz_id, dsgn_dntn_biz_cn_img_id, img_seq, img_expln, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_dept_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_dept_mng (dsgn_dntn_biz_dept_id, dsgn_dntn_biz_dept_nm, lclgv_cd, use_yn, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt, dsgn_dntn_biz_dept_cd) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_dept_mngr_mpng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_dept_mngr_mpng (user_id, dsgn_dntn_biz_dept_id, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_img_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_img_mng (dsgn_dntn_biz_img_id, dsgn_dntn_biz_id, img_nm, img_seq, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_mng (dsgn_dntn_biz_id, dsgn_dntn_biz_ttl, dsgn_dntn_biz_cn, dsgn_dntn_biz_bgng_ymd, dsgn_dntn_biz_end_ymd, goal_amt, dsgn_dntn_biz_stts_cd, rls_yn, dsgn_dntn_biz_rprs_img, dsgn_dntn_biz_se_cd, lclgv_cd, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt, dsgn_dntn_biz_se_dtl_cd, dsgn_dntn_biz_etc_cn, dsgn_dntn_biz_dept_id) FROM stdin;
1003	청양군 수해복구 지원사업	<p>&nbsp;</p>\n<p><img src="/images/new/202508081741530159_M.jpg" alt="청양군 수해복구 지원사업"></p>\n<p>■ 추진배경<br>\n2025년 여름 집중호우로 청양군 곳곳의 주택과 농경지가 침수 피해를 입었습니다. 특히 홀로 사시는\n어르신 가구는 복구 인력과 비용을 마련하기 어려워 피해가 장기화되고 있습니다.</p>\n<p>■ 지원계획<br>\n기부금은 침수 피해 주택의 수리비, 임시 거처 지원, 파손된 생활집기 교체 비용으로 사용됩니다.</p>\n<p>■ 모금기간<br>\n2026년 1월 ~ 2026년 12월(1년간)</p>\n<p>■ 모금목표액<br>\n2천 5백만 원</p>\n<p>■ 대상<br>\n청양군에 주민등록이 되어 있고 2025년 수해로 주택 피해를 입은 가구</p>\n<p>■ 기대효과<br>\n피해 주민들이 하루 빨리 일상으로 돌아갈 수 있도록 돕고, 재난 대응 공동체 기반을 다집니다.</p>\n<p>&nbsp;</p>	20260101	20261231	25000000	OPEN	Y	/images/new/202508081741530159_M.jpg	100	44790	1000	2026-08-24 05:11:57.155375	\N	\N	\N	\N	0
1001	해운대 해변 반려동물 놀이터 조성	반려동물과 함께하는 해변 공원을 만듭니다.	20260101	20261231	15000000	OPEN	Y	\N	400	26350	1000	2026-08-24 05:11:57.147676	\N	\N	\N	\N	0
1004	춘천시 취약지역 건강돌봄 캠페인	<p>&nbsp;</p>\n<p><img src="/images/new/202504301133410131_M.jpeg" alt="춘천시 취약지역 건강돌봄 캠페인"></p>\n<p>■ 추진배경<br>\n취약지역에 거주하는 어르신들은 정기적인 병원 방문과 건강관리가 어려워 만성질환 악화와 고독사\n위험에 노출되어 있습니다.</p>\n<p>■ 지원계획<br>\n방문 건강상담, 만성질환 관리 물품 지원, 정서적 돌봄을 위한 정기 방문 프로그램을 운영합니다.</p>\n<p>■ 모금기간<br>\n2026년 1월 ~ 2026년 12월(1년간)</p>\n<p>■ 모금목표액<br>\n1천 8백만 원</p>\n<p>■ 대상<br>\n춘천시 취약지역에 거주하는 65세 이상 독거 어르신</p>\n<p>■ 기대효과<br>\n정기적인 돌봄으로 고독사를 예방하고, 지역사회가 함께 어르신을 살피는 문화를 만듭니다.</p>\n<p>&nbsp;</p>	20260101	20261231	18000000	OPEN	Y	/images/new/202504301133410131_M.jpeg	100	51110	1000	2026-08-24 05:11:57.155375	\N	\N	\N	\N	0
1000	강남구 어린이 도서관 리모델링	노후 어린이 도서관을 새단장합니다.	20260101	20261231	30000000	OPEN	Y	\N	200	11680	1000	2026-08-24 05:11:57.147676	\N	\N	\N	\N	0
1006	거창군 산불피해 복구 지원사업	대형 산불로 피해를 입은 임야와 농가의 복구를 지원합니다.	20250301	20250831	20000000	CLOSED	Y	/images/new/202507181736420436_M.png	100	48880	1000	2026-08-24 05:11:57.251171	\N	\N	\N	\N	0
1007	진도군 독거노인 돌봄 지원사업	진도군 지역 독거노인의 안전한 생활을 지원합니다.	20250101	20250630	5000000	CLOSED	Y	\N	100	12860	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1008	해남군 문화의집 시설개선사업	해남군 주민 문화공간인 문화의집 시설을 개선합니다.	20260101	20261231	5370000	OPEN	Y	\N	200	12790	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1009	부산광역시 북구 청년 창업지원 플랫폼 구축사업	부산광역시 북구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	5740000	OPEN	Y	\N	300	26320	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1010	의성군 반려동물 놀이터 조성사업	의성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.	20260101	20261231	6110000	OPEN	Y	\N	400	47730	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1011	청양군 아동급식 지원사업	청양군 결식 우려 아동에게 급식을 지원합니다.	20260101	20261231	6480000	OPEN	Y	\N	100	44790	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1012	영덕군 작은도서관 리모델링사업	영덕군 노후 작은도서관을 새단장합니다.	20250101	20250630	6850000	CLOSED	Y	\N	200	47770	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1013	산청군 청년 창업지원 플랫폼 구축사업	산청군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	7220000	OPEN	Y	\N	300	48860	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1014	청주시 재난안전 대피시설 확충사업	청주시 재난 발생 시 주민 대피시설을 확충합니다.	20260101	20261231	7590000	OPEN	Y	\N	400	43110	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1015	대구광역시 동구 장애인 이동지원 차량 지원사업	대구광역시 동구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.	20260101	20261231	7960000	OPEN	Y	\N	100	27140	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1016	안동시 체육시설 현대화사업	안동시 노후 체육시설을 현대화합니다.	20260101	20261231	8330000	OPEN	Y	\N	200	47170	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1017	강동구 청년 창업지원 플랫폼 구축사업	강동구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20250101	20250630	8700000	CLOSED	Y	\N	300	11740	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1018	부산광역시 중구 가로등 LED 교체사업	부산광역시 중구 노후 가로등을 LED로 교체해 안전을 개선합니다.	20260101	20261231	9070000	OPEN	Y	\N	400	26110	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1019	계룡시 취약계층 겨울나기 지원사업	계룡시 취약계층 가구의 난방비와 방한용품을 지원합니다.	20260101	20261231	9440000	OPEN	Y	\N	100	44250	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1020	경산시 지역예술인 창작지원사업	경산시 지역 예술인의 창작활동을 지원합니다.	20260101	20261231	9810000	OPEN	Y	\N	200	47290	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1021	하남시 청년 창업지원 플랫폼 구축사업	하남시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	10180000	OPEN	Y	\N	300	41450	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1022	관악구 노후 상수도 시설개선사업	관악구 노후 상수도관을 정비해 주민 불편을 해소합니다.	20250101	20250630	10550000	CLOSED	Y	\N	400	11620	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1023	괴산군 청소년 자립 지원센터 운영	괴산군 위기청소년의 자립을 돕는 지원센터를 운영합니다.	20260101	20261231	10920000	OPEN	Y	\N	100	43760	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1024	창녕군 보건지소 의료장비 확충사업	창녕군 보건지소의 진료 장비를 확충합니다.	20260101	20261231	11290000	OPEN	Y	\N	200	48740	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1025	연제구 청년 창업지원 플랫폼 구축사업	연제구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	11660000	OPEN	Y	\N	300	26470	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1026	양천구 공영주차장 확충사업	양천구 부족한 공영주차장을 확충합니다.	20260101	20261231	12030000	OPEN	Y	\N	400	11470	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1027	연수구 독거노인 돌봄 지원사업	연수구 지역 독거노인의 안전한 생활을 지원합니다.	20250101	20250630	12400000	CLOSED	Y	\N	100	28185	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1028	논산시 문화의집 시설개선사업	논산시 주민 문화공간인 문화의집 시설을 개선합니다.	20260101	20261231	12770000	OPEN	Y	\N	200	44230	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1029	중랑구 청년 창업지원 플랫폼 구축사업	중랑구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	13140000	OPEN	Y	\N	300	11260	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1030	횡성군 반려동물 놀이터 조성사업	횡성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.	20260101	20261231	13510000	OPEN	Y	\N	400	51730	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1031	속초시 아동급식 지원사업	속초시 결식 우려 아동에게 급식을 지원합니다.	20260101	20261231	13880000	OPEN	Y	\N	100	51210	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1032	곡성군 작은도서관 리모델링사업	곡성군 노후 작은도서관을 새단장합니다.	20250101	20250630	14250000	CLOSED	Y	\N	200	12720	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1033	익산시 청년 창업지원 플랫폼 구축사업	익산시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	14620000	OPEN	Y	\N	300	52140	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1034	완도군 재난안전 대피시설 확충사업	완도군 재난 발생 시 주민 대피시설을 확충합니다.	20260101	20261231	14990000	OPEN	Y	\N	400	12850	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1035	송파구 장애인 이동지원 차량 지원사업	송파구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.	20260101	20261231	15360000	OPEN	Y	\N	100	11710	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1036	양평군 체육시설 현대화사업	양평군 노후 체육시설을 현대화합니다.	20260101	20261231	15730000	OPEN	Y	\N	200	41830	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1037	당진시 청년 창업지원 플랫폼 구축사업	당진시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20250101	20250630	16100000	CLOSED	Y	\N	300	44270	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1038	천안시 가로등 LED 교체사업	천안시 노후 가로등을 LED로 교체해 안전을 개선합니다.	20260101	20261231	16470000	OPEN	Y	\N	400	44130	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1039	울주군 취약계층 겨울나기 지원사업	울주군 취약계층 가구의 난방비와 방한용품을 지원합니다.	20260101	20261231	16840000	OPEN	Y	\N	100	31710	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1040	무주군 지역예술인 창작지원사업	무주군 지역 예술인의 창작활동을 지원합니다.	20260101	20261231	17210000	OPEN	Y	\N	200	52730	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1041	영주시 청년 창업지원 플랫폼 구축사업	영주시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	17580000	OPEN	Y	\N	300	47210	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1042	종로구 노후 상수도 시설개선사업	종로구 노후 상수도관을 정비해 주민 불편을 해소합니다.	20250101	20250630	17950000	CLOSED	Y	\N	400	11110	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1043	광산구 청소년 자립 지원센터 운영	광산구 위기청소년의 자립을 돕는 지원센터를 운영합니다.	20260101	20261231	18320000	OPEN	Y	\N	100	12330	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1044	대전광역시 중구 보건지소 의료장비 확충사업	대전광역시 중구 보건지소의 진료 장비를 확충합니다.	20260101	20261231	18690000	OPEN	Y	\N	200	30140	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1045	노원구 청년 창업지원 플랫폼 구축사업	노원구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	19060000	OPEN	Y	\N	300	11350	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1046	고성군 공영주차장 확충사업	고성군 부족한 공영주차장을 확충합니다.	20260101	20261231	19430000	OPEN	Y	\N	400	48820	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1047	남해군 독거노인 돌봄 지원사업	남해군 지역 독거노인의 안전한 생활을 지원합니다.	20250101	20250630	19800000	CLOSED	Y	\N	100	48840	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1048	양구군 문화의집 시설개선사업	양구군 주민 문화공간인 문화의집 시설을 개선합니다.	20260101	20261231	20170000	OPEN	Y	\N	200	51800	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1049	평창군 청년 창업지원 플랫폼 구축사업	평창군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	20540000	OPEN	Y	\N	300	51760	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1050	고성군 반려동물 놀이터 조성사업	고성군 반려동물과 주민이 함께하는 놀이터를 조성합니다.	20260101	20261231	20910000	OPEN	Y	\N	400	51820	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1051	영암군 아동급식 지원사업	영암군 결식 우려 아동에게 급식을 지원합니다.	20260101	20261231	21280000	OPEN	Y	\N	100	12800	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1052	영양군 작은도서관 리모델링사업	영양군 노후 작은도서관을 새단장합니다.	20250101	20250630	21650000	CLOSED	Y	\N	200	47760	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1053	문경시 청년 창업지원 플랫폼 구축사업	문경시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	22020000	OPEN	Y	\N	300	47280	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1054	울산광역시 동구 재난안전 대피시설 확충사업	울산광역시 동구 재난 발생 시 주민 대피시설을 확충합니다.	20260101	20261231	22390000	OPEN	Y	\N	400	31170	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1055	삼척시 장애인 이동지원 차량 지원사업	삼척시 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.	20260101	20261231	22760000	OPEN	Y	\N	100	51230	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1056	충주시 체육시설 현대화사업	충주시 노후 체육시설을 현대화합니다.	20260101	20261231	23130000	OPEN	Y	\N	200	43130	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1057	금정구 청년 창업지원 플랫폼 구축사업	금정구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20250101	20250630	23500000	CLOSED	Y	\N	300	26410	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1058	용인시 가로등 LED 교체사업	용인시 노후 가로등을 LED로 교체해 안전을 개선합니다.	20260101	20261231	23870000	OPEN	Y	\N	400	41460	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1059	검단구 취약계층 겨울나기 지원사업	검단구 취약계층 가구의 난방비와 방한용품을 지원합니다.	20260101	20261231	24240000	OPEN	Y	\N	100	28290	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1060	남원시 지역예술인 창작지원사업	남원시 지역 예술인의 창작활동을 지원합니다.	20260101	20261231	24610000	OPEN	Y	\N	200	52190	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1061	고령군 청년 창업지원 플랫폼 구축사업	고령군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	24980000	OPEN	Y	\N	300	47830	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1062	양주시 노후 상수도 시설개선사업	양주시 노후 상수도관을 정비해 주민 불편을 해소합니다.	20250101	20250630	25350000	CLOSED	Y	\N	400	41630	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1063	신안군 청소년 자립 지원센터 운영	신안군 위기청소년의 자립을 돕는 지원센터를 운영합니다.	20260101	20261231	25720000	OPEN	Y	\N	100	12870	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1064	용산구 보건지소 의료장비 확충사업	용산구 보건지소의 진료 장비를 확충합니다.	20260101	20261231	26090000	OPEN	Y	\N	200	11170	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1065	진천군 청년 창업지원 플랫폼 구축사업	진천군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	26460000	OPEN	Y	\N	300	43750	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1066	포항시 공영주차장 확충사업	포항시 부족한 공영주차장을 확충합니다.	20260101	20261231	26830000	OPEN	Y	\N	400	47110	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1067	나주시 독거노인 돌봄 지원사업	나주시 지역 독거노인의 안전한 생활을 지원합니다.	20250101	20250630	27200000	CLOSED	Y	\N	100	12170	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1068	통영시 문화의집 시설개선사업	통영시 주민 문화공간인 문화의집 시설을 개선합니다.	20260101	20261231	27570000	OPEN	Y	\N	200	48220	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1069	가평군 청년 창업지원 플랫폼 구축사업	가평군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	27940000	OPEN	Y	\N	300	41820	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1070	사하구 반려동물 놀이터 조성사업	사하구 반려동물과 주민이 함께하는 놀이터를 조성합니다.	20260101	20261231	28310000	OPEN	Y	\N	400	26380	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1071	대구광역시 북구 아동급식 지원사업	대구광역시 북구 결식 우려 아동에게 급식을 지원합니다.	20260101	20261231	28680000	OPEN	Y	\N	100	27230	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1072	칠곡군 작은도서관 리모델링사업	칠곡군 노후 작은도서관을 새단장합니다.	20250101	20250630	29050000	CLOSED	Y	\N	200	47850	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1073	영월군 청년 창업지원 플랫폼 구축사업	영월군 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	29420000	OPEN	Y	\N	300	51750	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1074	청송군 재난안전 대피시설 확충사업	청송군 재난 발생 시 주민 대피시설을 확충합니다.	20260101	20261231	29790000	OPEN	Y	\N	400	47750	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1075	옹진군 장애인 이동지원 차량 지원사업	옹진군 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.	20260101	20261231	30160000	OPEN	Y	\N	100	28720	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1076	진안군 체육시설 현대화사업	진안군 노후 체육시설을 현대화합니다.	20260101	20261231	30530000	OPEN	Y	\N	200	52720	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1077	광양시 청년 창업지원 플랫폼 구축사업	광양시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20250101	20250630	30900000	CLOSED	Y	\N	300	12190	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1078	서대문구 가로등 LED 교체사업	서대문구 노후 가로등을 LED로 교체해 안전을 개선합니다.	20260101	20261231	31270000	OPEN	Y	\N	400	11410	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1079	영도구 취약계층 겨울나기 지원사업	영도구 취약계층 가구의 난방비와 방한용품을 지원합니다.	20260101	20261231	31640000	OPEN	Y	\N	100	26200	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1080	은평구 지역예술인 창작지원사업	은평구 지역 예술인의 창작활동을 지원합니다.	20260101	20261231	32010000	OPEN	Y	\N	200	11380	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1081	강북구 청년 창업지원 플랫폼 구축사업	강북구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	32380000	OPEN	Y	\N	300	11305	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1082	부산광역시 남구 노후 상수도 시설개선사업	부산광역시 남구 노후 상수도관을 정비해 주민 불편을 해소합니다.	20250101	20250630	32750000	CLOSED	Y	\N	400	26290	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1083	금산군 청소년 자립 지원센터 운영	금산군 위기청소년의 자립을 돕는 지원센터를 운영합니다.	20260101	20261231	33120000	OPEN	Y	\N	100	44710	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1084	밀양시 보건지소 의료장비 확충사업	밀양시 보건지소의 진료 장비를 확충합니다.	20260101	20261231	33490000	OPEN	Y	\N	200	48270	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1085	영종구 청년 창업지원 플랫폼 구축사업	영종구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	33860000	OPEN	Y	\N	300	28155	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1086	김천시 공영주차장 확충사업	김천시 부족한 공영주차장을 확충합니다.	20260101	20261231	34230000	OPEN	Y	\N	400	47150	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1087	대덕구 독거노인 돌봄 지원사업	대덕구 지역 독거노인의 안전한 생활을 지원합니다.	20250101	20250630	34600000	CLOSED	Y	\N	100	30230	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1088	미추홀구 문화의집 시설개선사업	미추홀구 주민 문화공간인 문화의집 시설을 개선합니다.	20260101	20261231	34970000	OPEN	Y	\N	200	28177	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1089	남양주시 청년 창업지원 플랫폼 구축사업	남양주시 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	35340000	OPEN	Y	\N	300	41360	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1090	군위군 반려동물 놀이터 조성사업	군위군 반려동물과 주민이 함께하는 놀이터를 조성합니다.	20260101	20261231	35710000	OPEN	Y	\N	400	27720	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1091	김제시 아동급식 지원사업	김제시 결식 우려 아동에게 급식을 지원합니다.	20260101	20261231	36080000	OPEN	Y	\N	100	52210	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1092	군산시 작은도서관 리모델링사업	군산시 노후 작은도서관을 새단장합니다.	20250101	20250630	36450000	CLOSED	Y	\N	200	52130	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1093	서초구 청년 창업지원 플랫폼 구축사업	서초구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	36820000	OPEN	Y	\N	300	11650	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1094	안성시 재난안전 대피시설 확충사업	안성시 재난 발생 시 주민 대피시설을 확충합니다.	20260101	20261231	37190000	OPEN	Y	\N	400	41550	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1095	성동구 장애인 이동지원 차량 지원사업	성동구 거동이 불편한 주민을 위한 이동지원 차량을 지원합니다.	20260101	20261231	37560000	OPEN	Y	\N	100	11200	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1096	화천군 체육시설 현대화사업	화천군 노후 체육시설을 현대화합니다.	20260101	20261231	37930000	OPEN	Y	\N	200	51790	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1097	수영구 청년 창업지원 플랫폼 구축사업	수영구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20250101	20250630	38300000	CLOSED	Y	\N	300	26500	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1098	강화군 가로등 LED 교체사업	강화군 노후 가로등을 LED로 교체해 안전을 개선합니다.	20260101	20261231	38670000	OPEN	Y	\N	400	28710	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1099	창원시 취약계층 겨울나기 지원사업	창원시 취약계층 가구의 난방비와 방한용품을 지원합니다.	20260101	20261231	39040000	OPEN	Y	\N	100	48120	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1100	제주특별자치도 지역예술인 창작지원사업	제주특별자치도 지역 예술인의 창작활동을 지원합니다.	20260101	20261231	39410000	OPEN	Y	\N	200	50000	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1101	도봉구 청년 창업지원 플랫폼 구축사업	도봉구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	39780000	OPEN	Y	\N	300	11320	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1102	유성구 노후 상수도 시설개선사업	유성구 노후 상수도관을 정비해 주민 불편을 해소합니다.	20250101	20250630	40150000	CLOSED	Y	\N	400	30200	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1103	파주시 청소년 자립 지원센터 운영	파주시 위기청소년의 자립을 돕는 지원센터를 운영합니다.	20260101	20261231	40520000	OPEN	Y	\N	100	41480	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1104	서울특별시 강서구 보건지소 의료장비 확충사업	서울특별시 강서구 보건지소의 진료 장비를 확충합니다.	20260101	20261231	40890000	OPEN	Y	\N	200	11500	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1105	대전광역시 동구 청년 창업지원 플랫폼 구축사업	대전광역시 동구 지역 청년의 창업을 지원하는 플랫폼을 구축합니다.	20260101	20261231	41260000	OPEN	Y	\N	300	30110	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1106	서천군 공영주차장 확충사업	서천군 부족한 공영주차장을 확충합니다.	20260101	20261231	41630000	OPEN	Y	\N	400	44770	1000	2026-08-24 05:11:57.256444	\N	\N	\N	\N	0
1005	곡성군 배 재배농가 지원사업	<p>&nbsp;</p>\n<p><img src="/images/new/202508201312560094_M.jpg" alt="곡성군 배 재배농가 지원사업"></p>\n<p>■ 추진배경<br>\n곡성 심청배 재배농가는 고령화와 판로 부족으로 어려움을 겪고 있습니다. 우수한 품질에도 불구하고\n안정적인 판로가 없어 수확기마다 가격 변동에 취약합니다.</p>\n<p>■ 지원계획<br>\n공동 저장·선별 시설 개선과 온라인 직거래 판로 개척을 지원해 농가 소득 안정을 돕습니다.</p>\n<p>■ 모금기간<br>\n2026년 1월 ~ 2026년 12월(1년간)</p>\n<p>■ 모금목표액<br>\n1천 2백만 원</p>\n<p>■ 대상<br>\n곡성군 내 배 재배 농가</p>\n<p>■ 기대효과<br>\n농가의 안정적인 소득 기반을 마련하고, 곡성 심청배의 지역 대표 특산물 위상을 높입니다.</p>\n<p>&nbsp;</p>	20260101	20261231	12000000	OPEN	Y	/images/new/202508201312560094_M.jpg	300	12720	1000	2026-08-24 05:11:57.155375	\N	\N	\N	\N	0
1002	순천만 정원 생태 보전 사업	<p>&nbsp;</p>\n<p><img src="/images/new/202504301133410131_M.jpeg" alt="순천만 정원 생태 보전 사업"></p>\n<p><img src="/images/new/202507181736420436_M.png" alt="순천만 정원 생태 보전 사업"></p>\n<p><img src="/images/new/202508081741530159_M.jpg" alt="순천만 정원 생태 보전 사업"></p>\n<p><img src="/images/new/20250819171944_M.png" alt="순천만 정원 생태 보전 사업"></p>\n<p><img src="/images/new/202508201312560094_M.jpg" alt="순천만 정원 생태 보전 사업"></p>\n<p>&nbsp;</p>\n<p>■ 추진배경<br>\n순천만은 세계 5대 연안습지 중 하나로 갈대밭과 갯벌이 어우러진 생태 보고입니다. 그러나 기후\n변화와 방문객 증가로 습지 훼손과 생태계 교란 우려가 커지고 있습니다.</p>\n<p>■ 지원계획<br>\n탐방로 정비, 외래식물 제거, 철새 서식지 보호구역 관리 등 습지 생태계 보전 활동에 기부금을\n사용합니다.</p>\n<p>■ 모금기간<br>\n2026년 1월 ~ 2026년 12월(1년간)</p>\n<p>■ 모금목표액<br>\n2천만 원</p>\n<p>■ 대상<br>\n순천만 정원 및 습지 생태 보전에 관심 있는 모든 분</p>\n<p>■ 기대효과<br>\n세계적인 생태관광지로서 순천만의 가치를 지키고, 후대에 물려줄 자연유산을 보전합니다.</p>\n<p>&nbsp;</p>	20260101	20261231	20000000	OPEN	Y	\N	400	12150	1000	2026-08-24 05:11:57.147676	\N	\N	\N	\N	0
200	서울 취약계층 동행 돌봄 지원사업	<p>1인가구 어르신과 취약계층을 위한 돌봄 서비스를 확충합니다.</p>	20260101	20261231	50000000	OPEN	Y	\N	100	11000	1000	2026-08-24 05:11:57.325249	\N	\N	\N	\N	0
201	서울 청년 자립 지원 프로그램	<p>취업 준비 청년을 위한 자립 지원 프로그램을 운영합니다.</p>	20260101	20261231	40000000	OPEN	Y	\N	200	11000	1000	2026-08-24 05:11:57.325249	\N	\N	\N	\N	0
202	서울 골목상권 활성화 사업	<p>전통시장과 골목상권 활성화를 위한 지원사업입니다.</p>	20260201	20261130	35000000	OPEN	Y	\N	300	11000	1000	2026-08-24 05:11:57.325249	\N	\N	\N	\N	0
203	서울 반려동물 동반 공원 조성	<p>반려동물과 함께할 수 있는 도심 공원을 조성합니다.</p>	20260301	20261031	25000000	OPEN	Y	\N	400	11000	1000	2026-08-24 05:11:57.325249	\N	\N	\N	\N	0
204	서울 취약아동 교육격차 해소사업	<p>취약계층 아동의 교육 격차 해소를 위한 사업입니다.</p>	20250301	20250831	30000000	CLOSED	Y	\N	100	11000	1000	2026-08-24 05:11:57.325249	\N	2026-08-24 14:45:53.044462	\N	\N	0
205	서울 글로벌 매력도시 홍보사업	<p>서울의 매력을 세계에 알리는 홍보사업입니다.</p>	20250101	20250630	20000000	CLOSED	Y	\N	300	11000	1000	2026-08-24 05:11:57.325249	\N	2026-08-24 14:45:53.091701	\N	\N	0
206	서울 상생 공존 마을공동체 지원	<p>지역 마을공동체 활성화를 지원하는 사업입니다.</p>	20250601	20251231	28000000	CLOSED	Y	\N	200	11000	1000	2026-08-24 05:11:57.325249	\N	2026-08-24 14:45:53.096844	\N	\N	0
\.


--
-- Data for Name: g_dsgn_dntn_biz_ntc_cn_img_expln; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_ntc_cn_img_expln (dsgn_dntn_biz_ntc_id, img_seq, img_expln, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_ntc_file_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_ntc_file_mng (dsgn_dntn_biz_ntc_id, file_seq, file_nm, path_nm, orgnfl_nm, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_ntc_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_ntc_mng (dsgn_dntn_biz_ntc_id, dsgn_dntn_biz_id, dsgn_dntn_biz_ntc_ttl, dsgn_dntn_biz_ntc_cn, rls_yn, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_dntn_biz_user_msg; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_dntn_biz_user_msg (dntn_sn, user_msg, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_dsgn_prj_notice; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgn_prj_notice (prj_notice_id, dsgn_dntn_biz_id, prj_notice_subject, prj_notice_cn, frst_regist_pnttm) FROM stdin;
1000	6	곡성군 배 재배농가 지원사업 모금 경과 안내	<p>기부해 주신 모든 분들께 감사드립니다. 현재까지 모금된 기부금은 저장·선별 시설 개선 공사 준비에 사용되고 있습니다.</p>	20260215103000
1001	6	농가 방문 현장 스케치 공유	<p>지난 달 곡성 심청배 재배농가를 방문해 현장을 살펴보았습니다. 사업 진행 상황은 추후 다시 안내드리겠습니다.</p>	20260310140000
1002	3	순천만 탐방로 정비 공사 안내	<p>2026년 3월부터 순천만 습지 탐방로 일부 구간에서 정비 공사가 진행됩니다. 방문 시 우회 안내를 참고해 주세요.</p>	20260301090000
1003	4	청양군 수해복구 지원사업 1차 지원 완료	<p>모금된 기부금으로 침수 피해 가구 12세대에 대한 1차 주택 수리 지원을 완료했습니다. 성원해 주셔서 감사합니다.</p>	20260220153000
1004	5	춘천시 건강돌봄 캠페인 방문 일정 안내	<p>3월부터 취약지역 어르신 대상 정기 방문이 시작됩니다. 자세한 일정은 춘천시 홈페이지를 참고해 주세요.</p>	20260225110000
1005	5	캠페인 참여 자원봉사자 모집	<p>어르신 돌봄 활동에 함께할 자원봉사자를 모집합니다. 관심 있으신 분은 춘천시청으로 문의해 주세요.</p>	20260405093000
1006	7	거창군 산불피해 복구 지원사업 종료 안내	<p>모금 기간 종료에 따라 산불피해 복구 지원사업이 마감되었습니다. 기부해 주신 모든 분들께 진심으로 감사드립니다.</p>	20250901100000
\.


--
-- Data for Name: g_dsgncntr_part_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_part_mng (dsgncntr_part_id, dsgncntr_part_name, locgov_code, use_yn, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_part_user_mppng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_part_user_mppng (user_id, dsgncntr_part_id, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_prj_img; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_prj_img (dsgncntr_prj_image_id, prj_id, image_name, ordering, frst_register_id, frst_register_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_prj_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_prj_mng (prj_id, prj_subject, prj_cn, prj_st_dt, prj_ed_dt, target_amt, prj_status, display_flag, prj_image, bsns_type, locgov_code, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, content_etc, dsgncntr_part_id, bsns_sub_type) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_prj_notice; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_prj_notice (prj_notice_id, prj_id, prj_notice_subject, prj_notice_cn, display_yn, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_prj_notice_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_prj_notice_file (prj_notice_id, file_seq, file_name, path_name, org_file_name, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dsgncntr_prj_notice_img_desc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dsgncntr_prj_notice_img_desc (prj_notice_id, img_desc, img_seq, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_dy_gramt; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_dy_gramt (created_at, stdr_de, last_year_gramt, now_year_gramt) FROM stdin;
\.


--
-- Data for Name: g_giro; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_giro (locgov_code, locgov_nm, use_instt_code, giro_no) FROM stdin;
\.


--
-- Data for Name: g_honor_benefit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_honor_benefit (locgov_code, benefit_desc, updated_date) FROM stdin;
11230	강남구에 기부하신 분께는 매년 상반기 지역 축제 초청권을 추가로 드립니다.	2026-08-24 05:11:57.182227
\.


--
-- Data for Name: g_honor_cntrbtr; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_honor_cntrbtr (stdr_year, locgov_code, user_id, honor_cntrbtr_level_code, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
2025	11230	1000	300	\N	2025-01-15 10:30:00	\N	\N
2025	11230	1001	300	\N	2025-05-12 10:30:00	\N	\N
2025	46150	1002	300	\N	2025-03-05 10:30:00	\N	\N
2025	50000	1000	200	\N	2025-04-20 10:30:00	\N	\N
2025	26350	1001	200	\N	2025-02-10 10:30:00	\N	\N
\.


--
-- Data for Name: g_honor_cntrbtr_stdr; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_honor_cntrbtr_stdr (locgov_code, stdr_year, cntr_amt, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_instt_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_instt_code (locgov_code, upper_locgov_nm, locgov_nm, locgov_mapng_code, administ_instt_code, instt_mapng_code, use_at, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_intrst_locgov; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_intrst_locgov (user_id, locgov_code, regist_de, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
1000	11230	20250115	\N	2026-08-24 05:11:57.215141	\N	\N
1000	50000	20250420	\N	2026-08-24 05:11:57.215141	\N	\N
1001	26350	20250210	\N	2026-08-24 05:11:57.215141	\N	\N
1002	46150	20250305	\N	2026-08-24 05:11:57.215141	\N	\N
1002	36110	20260701	\N	2026-08-24 05:11:57.215141	\N	\N
\.


--
-- Data for Name: g_lclgv_hnr_user_rwrd_img_expln; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_lclgv_hnr_user_rwrd_img_expln (lclgv_cd, img_seq, img_expln, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_lclgv_hnr_user_stng_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_lclgv_hnr_user_stng_mng (lclgv_cd, gld_grd_dntn_amt, slvr_grd_dntn_amt, brnz_grd_dntn_amt, hnr_user_rwrd, rprs_img_nm, hnr_user_slctn_se_cd, use_yn, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt, hnr_user_stng_ttl) FROM stdin;
\.


--
-- Data for Name: g_link_instt_enc_key_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_link_instt_enc_key_mng (enc_key_ver, enc_key, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_locgov; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_locgov (locgov_code, upper_locgov_nm, locgov_nm, upper_locgov_code, locgov_intrcn_cn, charger_cttpc, charger_nm, charger_psitn_dept, locgov_hmpg, locgov_popltn_co, locgov_ar, locgov_spcprd, gcct_use_at, etrcsh_use_at, locgov_budget_amt, bizrno, locgov_zip, bass_adres, dtl_adres, achlqr_sle_at, use_at, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, process_dept_code, administ_instt_code, offcs_nm, offcs_file_nm, orginl_file_nm, stdr_1level_amt, stdr_2level_amt, stdr_3level_amt, fis_sp, charger_email, ordering) FROM stdin;
46150	전라남도	순천시	46000	<p>순천시는 대한민국 생태수도로 불리는 전라남도 남부의 도시로, 순천만 습지와 순천만국가정원 등 생태 자원이 풍부합니다. 고향사랑기부금은 순천만 습지 보전 및 지역 생태관광 활성화 사업에 우선적으로 사용됩니다.</p>	061-749-3821	김민석	기획예산과	https://www.suncheon.go.kr	270000	\N	\N	\N	\N	1250000	206-83-00441	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.130541	\N	\N	\N	\N	\N	\N	\N	100000	500000	1000000	\N	\N	\N
44790	충청남도	청양군	44000	<p>청양군은 충청남도의 대표적인 농업 도시로, 구기자와 고추 등 특산물로 유명합니다. 고향사랑기부금은 농가 지원 및 지역 특산품 브랜드화 사업에 사용됩니다.</p>	041-940-2521	이정훈	재정경제과	https://www.cheongyang.go.kr	30000	\N	\N	\N	\N	420000	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.151574	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11110	서울특별시	종로구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11140	서울특별시	중구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11170	서울특별시	용산구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11200	서울특별시	성동구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11215	서울특별시	광진구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11260	서울특별시	중랑구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11290	서울특별시	성북구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11305	서울특별시	강북구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11320	서울특별시	도봉구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11350	서울특별시	노원구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11380	서울특별시	은평구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11410	서울특별시	서대문구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11440	서울특별시	마포구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11470	서울특별시	양천구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11500	서울특별시	강서구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11530	서울특별시	구로구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11545	서울특별시	금천구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11560	서울특별시	영등포구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11590	서울특별시	동작구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11620	서울특별시	관악구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11650	서울특별시	서초구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11680	서울특별시	강남구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11710	서울특별시	송파구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11740	서울특별시	강동구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12000	전남광주통합특별시	시청	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12110	전남광주통합특별시	목포시	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12130	전남광주통합특별시	여수시	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12150	전남광주통합특별시	순천시	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12170	전남광주통합특별시	나주시	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12190	전남광주통합특별시	광양시	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12210	전남광주통합특별시	동구	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12240	전남광주통합특별시	서구	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12270	전남광주통합특별시	남구	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12300	전남광주통합특별시	북구	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12330	전남광주통합특별시	광산구	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12710	전남광주통합특별시	담양군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12720	전남광주통합특별시	곡성군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12730	전남광주통합특별시	구례군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12740	전남광주통합특별시	고흥군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12750	전남광주통합특별시	보성군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12760	전남광주통합특별시	화순군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12770	전남광주통합특별시	장흥군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12780	전남광주통합특별시	강진군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12790	전남광주통합특별시	해남군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12800	전남광주통합특별시	영암군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12810	전남광주통합특별시	무안군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12820	전남광주통합특별시	함평군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12830	전남광주통합특별시	영광군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12840	전남광주통합특별시	장성군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11230	서울특별시	강남구	11000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	113-83-00512	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.130541	\N	\N	\N	\N	\N	\N	\N	100000	500000	1000000	\N	\N	\N
26350	부산광역시	해운대구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	609-83-00219	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.130541	\N	\N	\N	\N	\N	\N	\N	100000	500000	1000000	\N	\N	\N
36110	\N	세종특별자치시	36000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	308-83-00107	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.130541	\N	\N	\N	\N	\N	\N	\N	100000	500000	1000000	\N	\N	\N
50000	\N	제주특별자치도	50000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	410-83-00033	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.130541	\N	\N	\N	\N	\N	\N	\N	100000	500000	1000000	\N	\N	\N
12850	전남광주통합특별시	완도군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12860	전남광주통합특별시	진도군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
12870	전남광주통합특별시	신안군	12000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26000	부산광역시	시청	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26110	부산광역시	중구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26140	부산광역시	서구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26170	부산광역시	동구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26200	부산광역시	영도구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26230	부산광역시	부산진구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26260	부산광역시	동래구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26290	부산광역시	남구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26320	부산광역시	북구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26380	부산광역시	사하구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26410	부산광역시	금정구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26440	부산광역시	강서구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26470	부산광역시	연제구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26500	부산광역시	수영구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26530	부산광역시	사상구	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
26710	부산광역시	기장군	26000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27000	대구광역시	시청	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27110	대구광역시	중구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27140	대구광역시	동구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27170	대구광역시	서구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27200	대구광역시	남구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27230	대구광역시	북구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27260	대구광역시	수성구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27290	대구광역시	달서구	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27710	대구광역시	달성군	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
27720	대구광역시	군위군	27000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28000	인천광역시	시청	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28710	인천광역시	강화군	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28720	인천광역시	옹진군	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28125	인천광역시	제물포구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28155	인천광역시	영종구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28177	인천광역시	미추홀구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28185	인천광역시	연수구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28200	인천광역시	남동구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28237	인천광역시	부평구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28245	인천광역시	계양구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28275	인천광역시	서해구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
28290	인천광역시	검단구	28000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30000	대전광역시	시청	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30110	대전광역시	동구	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30140	대전광역시	중구	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30170	대전광역시	서구	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30200	대전광역시	유성구	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
30230	대전광역시	대덕구	30000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31000	울산광역시	시청	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31110	울산광역시	중구	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31140	울산광역시	남구	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31170	울산광역시	동구	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31200	울산광역시	북구	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
31710	울산광역시	울주군	31000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
36000	세종특별자치시	시청	36000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41000	경기도	도청	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41110	경기도	수원시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41130	경기도	성남시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41150	경기도	의정부시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41170	경기도	안양시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41190	경기도	부천시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41210	경기도	광명시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41220	경기도	평택시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41250	경기도	동두천시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41270	경기도	안산시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41280	경기도	고양시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41290	경기도	과천시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41310	경기도	구리시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41360	경기도	남양주시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41370	경기도	오산시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41390	경기도	시흥시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41410	경기도	군포시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41430	경기도	의왕시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41450	경기도	하남시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41460	경기도	용인시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41480	경기도	파주시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41500	경기도	이천시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41550	경기도	안성시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41570	경기도	김포시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41590	경기도	화성시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41610	경기도	광주시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41630	경기도	양주시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41650	경기도	포천시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41670	경기도	여주시	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41800	경기도	연천군	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41820	경기도	가평군	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
41830	경기도	양평군	41000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43000	충청북도	도청	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43110	충청북도	청주시	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43130	충청북도	충주시	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43150	충청북도	제천시	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43720	충청북도	보은군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43730	충청북도	옥천군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43740	충청북도	영동군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43745	충청북도	증평군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43750	충청북도	진천군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43760	충청북도	괴산군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43770	충청북도	음성군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
43800	충청북도	단양군	43000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44000	충청남도	도청	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44130	충청남도	천안시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44150	충청남도	공주시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44180	충청남도	보령시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44200	충청남도	아산시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44210	충청남도	서산시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44230	충청남도	논산시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44250	충청남도	계룡시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44710	충청남도	금산군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44760	충청남도	부여군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44770	충청남도	서천군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44800	충청남도	홍성군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44810	충청남도	예산군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44825	충청남도	태안군	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
44270	충청남도	당진시	44000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47000	경상북도	도청	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47110	경상북도	포항시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47130	경상북도	경주시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47150	경상북도	김천시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47170	경상북도	안동시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47190	경상북도	구미시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47210	경상북도	영주시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47230	경상북도	영천시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47250	경상북도	상주시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47280	경상북도	문경시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47290	경상북도	경산시	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47730	경상북도	의성군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47750	경상북도	청송군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47760	경상북도	영양군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47770	경상북도	영덕군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47820	경상북도	청도군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47830	경상북도	고령군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47840	경상북도	성주군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47850	경상북도	칠곡군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47900	경상북도	예천군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47920	경상북도	봉화군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47930	경상북도	울진군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
47940	경상북도	울릉군	47000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48000	경상남도	도청	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48120	경상남도	창원시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48170	경상남도	진주시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48220	경상남도	통영시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48240	경상남도	사천시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48250	경상남도	김해시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48270	경상남도	밀양시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48310	경상남도	거제시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48330	경상남도	양산시	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48720	경상남도	의령군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48730	경상남도	함안군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48740	경상남도	창녕군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48820	경상남도	고성군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48840	경상남도	남해군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48850	경상남도	하동군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48860	경상남도	산청군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48870	경상남도	함양군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48880	경상남도	거창군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
48890	경상남도	합천군	48000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51000	강원특별자치도	도청	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51130	강원특별자치도	원주시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51150	강원특별자치도	강릉시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51170	강원특별자치도	동해시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51190	강원특별자치도	태백시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51210	강원특별자치도	속초시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51230	강원특별자치도	삼척시	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51720	강원특별자치도	홍천군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51730	강원특별자치도	횡성군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51750	강원특별자치도	영월군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51760	강원특별자치도	평창군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51770	강원특별자치도	정선군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51780	강원특별자치도	철원군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51790	강원특별자치도	화천군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51800	강원특별자치도	양구군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51810	강원특별자치도	인제군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51820	강원특별자치도	고성군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51830	강원특별자치도	양양군	51000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52000	전북특별자치도	도청	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52110	전북특별자치도	전주시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52130	전북특별자치도	군산시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52140	전북특별자치도	익산시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52180	전북특별자치도	정읍시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52190	전북특별자치도	남원시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52210	전북특별자치도	김제시	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52710	전북특별자치도	완주군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52720	전북특별자치도	진안군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52740	전북특별자치도	장수군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52750	전북특별자치도	임실군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52770	전북특별자치도	순창군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52790	전북특별자치도	고창군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52800	전북특별자치도	부안군	52000	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	0	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
51110	강원특별자치도	춘천시	51000	<p>춘천시는 강원특별자치도의 도청 소재지로, 호수와 산으로 둘러싸인 자연경관과 닭갈비·막국수 등 먹거리로 잘 알려져 있습니다. 고향사랑기부금은 청년 정착 지원 및 관광 인프라 조성에 사용됩니다.</p>	033-250-3412	박서연	세정과	https://www.chuncheon.go.kr	280000	\N	\N	\N	\N	1180000	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.151574	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
46530	전라남도	곡성군	46000	<p>곡성군은 전라남도의 대표적인 배 재배 지역으로, 섬진강을 따라 형성된 청정 자연환경이 특징입니다. 고향사랑기부금은 배 재배농가 지원사업 및 귀농·귀촌 지원에 사용됩니다.</p>	061-360-8421	정민아	기획감사실	https://www.gokseong.go.kr	27000	\N	\N	\N	\N	380000	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.151574	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
52730	전북특별자치도	무주군	52000	<p>무주군은 전북특별자치도 산간지역에 위치한 청정 관광도시로, 무주반딧불축제와 덕유산국립공원으로 유명합니다. 고향사랑기부금은 지역 축제 지원 및 청정환경 보전사업에 사용됩니다.</p>	063-320-2606	한지원	자치행정과	https://www.muju.go.kr	23000	\N	\N	\N	\N	350000	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
11000	서울특별시	시청	11000	<p>약자와 동행하는 서울! 우리 사회 양극화를 해소하고 더불어 살아가는 상생과 공존의 도시<br>글로벌 매력도시 서울! 세계인 누구나 살고 싶고 찾아오고 싶고 일하고 싶고 투자하고 싶은 매력적인 도시<br><br>'동행·매력 특별시' 서울특별시입니다.</p>	02-2133-6876	김소영	재정담당관	https://www.seoul.go.kr	9384325	\N	\N	\N	\N	45740518	\N	\N	\N	\N	\N	Y	\N	2026-08-24 05:11:57.19575	\N	\N	\N	\N	\N	\N	\N	0	0	0	\N	\N	\N
\.


--
-- Data for Name: g_locgov_dept_hist; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_locgov_dept_hist (locgov_code, dept_hist_no, process_dept_code, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_locgov_fav_item_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_locgov_fav_item_mng (locgov_code, catalog_year, catalog_no, delete_yn, item_id, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_locgov_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_locgov_image (locgov_code, pc_file_name, mobile_file_name, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_next_buga_request; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_next_buga_request (link_mng_key, sgb_cd, link_trgt_cd, dpt_cd, spcl_fis_biz_cd, fyr, act_se_cd, rprs_txm_cd, oper_item_cd, lvy_ymd, frst_pct_amt, frst_pid_ymd, pyr_se_cd, pyr_no, pyr_nm, rprs_pyr_no, rprs_pyr_nm, pyr_stt_cd, lotno_road_addr_se_cd, zip, road_nm_cd, bmno, bsno, stdg_cd, dong_cd, road_nm_daddr, gl_nm, mng_item_cn1, buga_status_cd, link_rst_cd, link_rst_msg, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_next_buga_request_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_next_buga_request_temp (link_mng_key, sgb_cd, link_trgt_cd, dpt_cd, spcl_fis_biz_cd, fyr, act_se_cd, rprs_txm_cd, oper_item_cd, lvy_ymd, frst_pct_amt, frst_pid_ymd, pyr_se_cd, pyr_no, pyr_nm, rprs_pyr_no, rprs_pyr_nm, pyr_stt_cd, lotno_road_addr_se_cd, zip, road_nm_cd, bmno, bsno, stdg_cd, dong_cd, road_nm_daddr, gl_nm, mng_item_cn1, buga_status_cd, link_rst_cd, link_rst_msg, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_next_sunap_response; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_next_sunap_response (link_mng_key, sgb_cd, sgb_nm, taxn_no, unty_taxn_no, dpt_cd, dpt_nm, spcl_fis_biz_cd, spcl_fis_biz_nm, fyr, act_se_cd, act_se_nm, rprs_txm_cd, rprs_txm_nm, oper_item_cd, oper_item_nm, lvy_no, itm_no, epay_no, rcvmt_no, rcvmt_se_cd, rcvmt_se_nm, rcvmt_ymd, act_ymd, tsf_ymd, rcvmt_pct_amt, rcvmt_adtn_amt, rcvmt_intr_amt, bank_nm, rcvmt_ty_cd, rcvmt_ty, rsve_item1, rsve_item2, rsve_item3, rsve_item4, rsve_item5, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_pay_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_pay_log (pay_log_id, elctrn_pay_no, pay_method, pay_start_dt, pay_end_dt, pay_process, frst_reg_id, frst_reg_dt, last_mdfcn_id, last_mdfcn_dt) FROM stdin;
\.


--
-- Data for Name: g_prj_content_img_desc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_prj_content_img_desc (prj_content_img_id, prj_id, img_desc, img_seq, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: g_relay_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_relay_log (relay_log_id, user_id, relay_type, cntr_locgov_code, cntr_sn, relay_result_code, frst_regist_pnttm) FROM stdin;
1024	1000	BUGA	11230	D202501151030001000	SUCCESS	2026-08-24 17:37:33.84218
1025	1000	SUNAP	11230	D202501151030001000	SUCCESS	2026-08-24 17:37:33.843263
1026	1000	NTS_RECEIPT	11230	D202501151030001000	SUCCESS	2026-08-24 17:37:33.909399
1027	1001	BUGA	26350	D202502101030001001	SUCCESS	2026-08-24 17:37:33.922731
1028	1001	SUNAP	26350	D202502101030001001	SUCCESS	2026-08-24 17:37:33.923794
1029	1001	NTS_RECEIPT	26350	D202502101030001001	SUCCESS	2026-08-24 17:37:33.950121
1030	1002	BUGA	46150	D202503051030001002	SUCCESS	2026-08-24 17:37:33.966598
1031	1002	SUNAP	46150	D202503051030001002	SUCCESS	2026-08-24 17:37:33.967686
1032	1002	NTS_RECEIPT	46150	D202503051030001002	SUCCESS	2026-08-24 17:37:33.983905
1033	1000	BUGA	50000	D202504201030001000	SUCCESS	2026-08-24 17:37:33.998367
1034	1000	SUNAP	50000	D202504201030001000	SUCCESS	2026-08-24 17:37:33.999486
1035	1000	NTS_RECEIPT	50000	D202504201030001000	SUCCESS	2026-08-24 17:37:34.018448
1036	1001	BUGA	11230	D202505121030001001	SUCCESS	2026-08-24 17:37:34.052208
1037	1001	SUNAP	11230	D202505121030001001	SUCCESS	2026-08-24 17:37:34.054236
1038	1001	NTS_RECEIPT	11230	D202505121030001001	SUCCESS	2026-08-24 17:37:34.070996
1039	1002	BUGA	26350	D202506181030001002	SUCCESS	2026-08-24 17:37:34.084474
1040	1002	SUNAP	26350	D202506181030001002	SUCCESS	2026-08-24 17:37:34.08608
1041	1002	NTS_RECEIPT	26350	D202506181030001002	SUCCESS	2026-08-24 17:37:34.10167
1042	1000	BUGA	46150	D202507251030001000	SUCCESS	2026-08-24 17:37:34.114989
1043	1000	SUNAP	46150	D202507251030001000	SUCCESS	2026-08-24 17:37:34.116076
1044	1000	NTS_RECEIPT	46150	D202507251030001000	SUCCESS	2026-08-24 17:37:34.141138
1045	1001	BUGA	11230	D202508031030001001	SUCCESS	2026-08-24 17:37:34.168748
1046	1001	SUNAP	11230	D202508031030001001	SUCCESS	2026-08-24 17:37:34.174048
1047	1001	NTS_RECEIPT	11230	D202508031030001001	SUCCESS	2026-08-24 17:37:34.211828
\.


--
-- Data for Name: gif_etax_daesa; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_etax_daesa (if_no, epay_no, com_req_meche, com_req_dt, com_req_tm, com_pay_msg_no, access_key, org_c, semok_cd, sunap_amt, sunap_dt, rst_cd, rst_msg, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: gif_etax_sunap; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_etax_sunap (if_no, epay_no, com_req_meche, com_req_dt, com_req_tm, com_pay_msg_no, access_key, org_c, sunap_yn, sunap_amt, sunap_dt, rst_cd, rst_msg, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: gif_etax_sunap_etc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_etax_sunap_etc (if_no, epay_no, com_req_meche, com_req_dt, com_req_tm, com_pay_msg_no, access_key, org_c, sunap_yn, sunap_amt, sunap_dt, rst_cd, rst_msg, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: gif_external_mst; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_external_mst (if_no, enapbu_no, status, rty_cnt, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: gif_hometax; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_hometax (if_no, elctrn_pay_no, user_id, dnt_dt, conb_cd, dnt_amt, cntr_type, mem_ci, biz_no, result_code, result_msg, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: gif_seoul; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_seoul (if_no, enapbu_no, sigu_cd, semok_cd, tax_ym, tax_gubun, sido_cd, nap_nm, nap_gubun, tax_amt, sise, reside_status, mul_gubun, mul_nm, book_no, sys_gubun, error_cd, error_msg, insert_key, insert_ak, result_cnt, if_st_dt, if_ed_dt, nap_id) FROM stdin;
\.


--
-- Data for Name: gif_stnd_buga_now; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_stnd_buga_now (if_no, elct_pay_no, com_msg_len, com_if_id, com_source, com_target, com_msg_key, com_type_cd, com_rst_cd, sys_cd, dept_cd, fisyy, fis_sp, ptcl_cd, imps_dt, init_prcp_tax_amt, lst_prcp_tax_amt, init_due_dt, lst_due_dt, af_due_dt, af_due_amt, imps_sp, decs_sp, txpr_sp, txpr_tel_no, txpr_mphn_no, txpr_eml, new_addr_yn, txpr_road_cd, txpr_bd_flr_sp, txpr_bd_prcp_no, txpr_bd_sub_no, stat_cd, spcl_fis_biz_cd, txpr_zip_no, txpr_lglvil_cd, txpr_twnvil_cd, txpr_mt, txpr_addr_no, txpr_addr_ho, txpr_spcl_addr, txpr_spcl_addr_dong, txpr_spcl_addr_ho, txpr_addr_tong, txpr_addr_ban, txpr_bd_mng_no, obj_nm, tax_obj_sp, tax_obj_new_addr_yn, mng_htm1, mng_htm2, mng_htm3, mng_htm4, mng_htm5, mng_htm6, rmk, init_wrkr_id, bank_cd, txpr_full_addr, txpr_basic_addr, txpr_basic_dtl_addr, result_code, result_msg, imps_key, result, if_st_dt, if_ed_dt, txpr_no, txpr_nm, txpr_dtl_addr) FROM stdin;
\.


--
-- Data for Name: gif_stnd_jijache_now; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_stnd_jijache_now (locgov_code, web_url) FROM stdin;
\.


--
-- Data for Name: gif_stnd_sunap_now; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gif_stnd_sunap_now (if_no, elct_pay_no, com_msg_len, com_if_id, com_source, com_target, com_msg_key, com_type_cd, com_rst_cd, conn_key, result_code, result_msg, result, rcpt_prcp_tax_amt, rcpt_add_amt, divd_recpltt_amt, fis_dt, wrk_dt, rcpt_sp, rcpt_dt, rcpt_typ, bank_cd, bndl_no, trnr_dt, if_st_dt, if_ed_dt) FROM stdin;
\.


--
-- Data for Name: op_common_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code) FROM stdin;
CNTR_STATUS	ko	REQUESTED	신청	\N	1	Y	\N	\N	\N	\N
CNTR_STATUS	ko	COMPLETED	납부완료	\N	2	Y	\N	\N	\N	\N
CNTR_STATUS	ko	CANCELLED	취소	\N	3	Y	\N	\N	\N	\N
DSGN_STATUS	ko	OPEN	진행중	\N	1	Y	\N	\N	\N	\N
DSGN_STATUS	ko	CLOSED	종료	\N	2	Y	\N	\N	\N	\N
SYSTEM_CONFIG	ko	ANNUAL_TOTAL_LIMIT	연간 총 기부한도(원, 전체 지자체 합산)	\N	1	Y	\N	20000000	\N	\N
NTS_STATUS	ko	PENDING	등록대기	\N	1	Y	\N	\N	\N	\N
NTS_STATUS	ko	REGISTERED	등록완료	\N	2	Y	\N	\N	\N	\N
NTS_STATUS	ko	FAILED	등록실패	\N	3	Y	\N	\N	\N	\N
CNTR_PATH	ko	ONLINE	온라인	\N	1	Y	\N	\N	\N	\N
CNTR_PATH	ko	OFFLINE	오프라인(기탁서)	\N	2	Y	\N	\N	\N	\N
DSGN_BSNS_TYPE	ko	100	사회적 취약계층의 지원 및 청소년의 육성ㆍ보호	\N	1	Y	\N	\N	\N	\N
DSGN_BSNS_TYPE	ko	200	지역 주민의 문화ㆍ예술ㆍ보건 등의 증진	\N	2	Y	\N	\N	\N	\N
DSGN_BSNS_TYPE	ko	300	시민참여, 자원봉사 등 지역공동체 활성화 지원(주민참여형 사업)	\N	3	Y	\N	\N	\N	\N
DSGN_BSNS_TYPE	ko	400	그 밖에 주민의 복리 증진에 필요한 사업의 추진(취약계층, 문화ㆍ예술ㆍ보건, 자원봉사에 포함되지 않는 부문)	\N	4	Y	\N	\N	\N	\N
RELAY_TYPE	ko	BUGA	세외수입 부과	\N	1	Y	\N	\N	\N	\N
RELAY_TYPE	ko	SUNAP	세외수입 수납확인	\N	2	Y	\N	\N	\N	\N
RELAY_TYPE	ko	NTS_RECEIPT	국세청 전자기부금영수증 등록	\N	3	Y	\N	\N	\N	\N
RELAY_RESULT	ko	SUCCESS	성공	\N	1	Y	\N	\N	\N	\N
RELAY_RESULT	ko	FAIL	실패	\N	2	Y	\N	\N	\N	\N
CNTR_PATH	ko	100	온라인	\N	1	Y	\N	\N	\N	\N
CNTR_PATH	ko	200	오프라인	\N	2	Y	\N	\N	\N	\N
HONOR_STD	ko	100	우수	\N	1	Y	\N	\N	\N	\N
HONOR_STD	ko	200	최우수	\N	2	Y	\N	\N	\N	\N
HONOR_STD	ko	300	특급	\N	3	Y	\N	\N	\N	\N
\.


--
-- Data for Name: op_event_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_event_code (id, created, created_by, updated, updated_by, change_code, contents, event_code, redirection, type, user_id, utm_query_string, campaign_id) FROM stdin;
\.


--
-- Data for Name: op_event_code_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_event_code_log (id, created, created_by, updated, updated_by, channel, code_type, event_code, event_uid, item_user_code, log_detail, log_type, order_code, source_user_id, uid, user_id, utm_campaign, utm_content, utm_item, utm_medium, utm_source) FROM stdin;
\.


--
-- Data for Name: op_event_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_event_item (id, created, created_by, updated, updated_by, version, item_id, ordering, event_id) FROM stdin;
\.


--
-- Data for Name: op_event_reply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_event_reply (id, created, created_by, updated, updated_by, version, content, data_status, event_id, user_id) FROM stdin;
\.


--
-- Data for Name: op_honor_view_hist; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_honor_view_hist (view_dt, user_id, lclgv_cd) FROM stdin;
\.


--
-- Data for Name: op_island; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_island (id, created, created_by, updated, updated_by, address, island_type, zipcode) FROM stdin;
\.


--
-- Data for Name: op_sido_mapping; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_sido_mapping (sido_mapping_group_key, sido_name, sido_data) FROM stdin;
\.


--
-- Data for Name: op_spel_dstr_zn; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_spel_dstr_zn (id, psdnt_noti_no, upper_locgov_nm, upper_locgov_code, locgov_nm, locgov_code, noti_reason, noti_date, end_date, frst_reg_dt) FROM stdin;
1000	34521	서울특별시	11000	강남구	11230	집중호우 특별재난지역 선포	20260101	20261231	2026-08-24 05:11:57.163578
\.


--
-- Name: g_catalog_card_news_mng_card_news_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_catalog_card_news_mng_card_news_id_seq', 1000, false);


--
-- Name: g_cntr_chenap_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_cntr_chenap_id_seq', 1000, false);


--
-- Name: g_cntr_reqmng_req_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_cntr_reqmng_req_id_seq', 1000, false);


--
-- Name: g_cntr_tax_log_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_cntr_tax_log_log_id_seq', 1000, false);


--
-- Name: g_cntr_wegive_cntr_sn_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_cntr_wegive_cntr_sn_seq', 1000, false);


--
-- Name: g_ctbny_opratn_file_regist_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_ctbny_opratn_file_regist_file_id_seq', 1000, false);


--
-- Name: g_ctbny_opratn_regist_sn_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_ctbny_opratn_regist_sn_seq', 1003, true);


--
-- Name: g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq', 1002, true);


--
-- Name: g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq', 1000, false);


--
-- Name: g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq', 1000, false);


--
-- Name: g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq', 1000, false);


--
-- Name: g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq', 1106, true);


--
-- Name: g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq', 1000, false);


--
-- Name: g_dsgn_prj_notice_prj_notice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgn_prj_notice_prj_notice_id_seq', 1006, true);


--
-- Name: g_dsgncntr_part_mng_dsgncntr_part_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgncntr_part_mng_dsgncntr_part_id_seq', 1000, false);


--
-- Name: g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq', 1000, false);


--
-- Name: g_dsgncntr_prj_mng_prj_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgncntr_prj_mng_prj_id_seq', 1000, false);


--
-- Name: g_dsgncntr_prj_notice_prj_notice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_dsgncntr_prj_notice_prj_notice_id_seq', 1000, false);


--
-- Name: g_pay_log_pay_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_pay_log_pay_log_id_seq', 1000, false);


--
-- Name: g_prj_content_img_desc_prj_content_img_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_prj_content_img_desc_prj_content_img_id_seq', 1000, false);


--
-- Name: g_relay_log_relay_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_relay_log_relay_log_id_seq', 1047, true);


--
-- Name: op_spel_dstr_zn_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_spel_dstr_zn_id_seq', 1000, true);


--
-- Name: donation_levy donation_levy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donation_levy
    ADD CONSTRAINT donation_levy_pkey PRIMARY KEY (cntr_sn);


--
-- Name: g_adm_locgov g_adm_locgov_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_adm_locgov
    ADD CONSTRAINT g_adm_locgov_pkey PRIMARY KEY (adm_cd);


--
-- Name: g_agency_login_confirm_info g_agency_login_confirm_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_agency_login_confirm_info
    ADD CONSTRAINT g_agency_login_confirm_info_pkey PRIMARY KEY (user_session_id, valid_access_cd);


--
-- Name: g_catalog_card_news_img_desc g_catalog_card_news_img_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_card_news_img_desc
    ADD CONSTRAINT g_catalog_card_news_img_desc_pkey PRIMARY KEY (card_news_id, img_seq);


--
-- Name: g_catalog_card_news_mng g_catalog_card_news_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_card_news_mng
    ADD CONSTRAINT g_catalog_card_news_mng_pkey PRIMARY KEY (card_news_id);


--
-- Name: g_catalog_content_img_desc g_catalog_content_img_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_content_img_desc
    ADD CONSTRAINT g_catalog_content_img_desc_pkey PRIMARY KEY (catalog_content_id, img_seq);


--
-- Name: g_catalog_content_mng g_catalog_content_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_content_mng
    ADD CONSTRAINT g_catalog_content_mng_pkey PRIMARY KEY (catalog_content_id);


--
-- Name: g_catalog_mng g_catalog_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_catalog_mng
    ADD CONSTRAINT g_catalog_mng_pkey PRIMARY KEY (catalog_year, catalog_no);


--
-- Name: g_cheer_msg g_cheer_msg_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cheer_msg
    ADD CONSTRAINT g_cheer_msg_pkey PRIMARY KEY (cntr_sn);


--
-- Name: g_cntr_chenap g_cntr_chenap_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_chenap
    ADD CONSTRAINT g_cntr_chenap_pkey PRIMARY KEY (id);


--
-- Name: g_cntr_lmtt g_cntr_lmtt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_lmtt
    ADD CONSTRAINT g_cntr_lmtt_pkey PRIMARY KEY (lmtt_bgn_de, lmtt_end_de, locgov_code);


--
-- Name: g_cntr g_cntr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr
    ADD CONSTRAINT g_cntr_pkey PRIMARY KEY (cntr_sn);


--
-- Name: g_cntr_rcipt g_cntr_rcipt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_rcipt
    ADD CONSTRAINT g_cntr_rcipt_pkey PRIMARY KEY (cntr_sn, elctrn_pay_no, cntr_outpt_sn);


--
-- Name: g_cntr_reqmng g_cntr_reqmng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_reqmng
    ADD CONSTRAINT g_cntr_reqmng_pkey PRIMARY KEY (req_id);


--
-- Name: g_cntr_tax_log g_cntr_tax_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_tax_log
    ADD CONSTRAINT g_cntr_tax_log_pkey PRIMARY KEY (log_id);


--
-- Name: g_cntr_temp2 g_cntr_temp2_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_temp2
    ADD CONSTRAINT g_cntr_temp2_pkey PRIMARY KEY (user_id);


--
-- Name: g_cntr_wegive g_cntr_wegive_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_wegive
    ADD CONSTRAINT g_cntr_wegive_pkey PRIMARY KEY (cntr_sn);


--
-- Name: g_ctbny_opratn_file g_ctbny_opratn_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn_file
    ADD CONSTRAINT g_ctbny_opratn_file_pkey PRIMARY KEY (regist_file_id, regist_sn);


--
-- Name: g_ctbny_opratn g_ctbny_opratn_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn
    ADD CONSTRAINT g_ctbny_opratn_pkey PRIMARY KEY (regist_sn);


--
-- Name: g_ctbny_setup g_ctbny_setup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_setup
    ADD CONSTRAINT g_ctbny_setup_pkey PRIMARY KEY (stdr_year, locgov_code);


--
-- Name: g_disaster_zone g_disaster_zone_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_disaster_zone
    ADD CONSTRAINT g_disaster_zone_pkey PRIMARY KEY (zone_id);


--
-- Name: g_dsgn_dntn_biz_aprv_log g_dsgn_dntn_biz_aprv_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_aprv_log
    ADD CONSTRAINT g_dsgn_dntn_biz_aprv_log_pkey PRIMARY KEY (dsgn_dntn_biz_aprv_id);


--
-- Name: g_dsgn_dntn_biz_cn_img_expln g_dsgn_dntn_biz_cn_img_expln_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_cn_img_expln
    ADD CONSTRAINT g_dsgn_dntn_biz_cn_img_expln_pkey PRIMARY KEY (dsgn_dntn_biz_cn_img_id);


--
-- Name: g_dsgn_dntn_biz_dept_mng g_dsgn_dntn_biz_dept_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_dept_mng
    ADD CONSTRAINT g_dsgn_dntn_biz_dept_mng_pkey PRIMARY KEY (dsgn_dntn_biz_dept_id);


--
-- Name: g_dsgn_dntn_biz_dept_mngr_mpng g_dsgn_dntn_biz_dept_mngr_mpng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_dept_mngr_mpng
    ADD CONSTRAINT g_dsgn_dntn_biz_dept_mngr_mpng_pkey PRIMARY KEY (user_id);


--
-- Name: g_dsgn_dntn_biz_img_mng g_dsgn_dntn_biz_img_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_img_mng
    ADD CONSTRAINT g_dsgn_dntn_biz_img_mng_pkey PRIMARY KEY (dsgn_dntn_biz_img_id);


--
-- Name: g_dsgn_dntn_biz_mng g_dsgn_dntn_biz_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_mng
    ADD CONSTRAINT g_dsgn_dntn_biz_mng_pkey PRIMARY KEY (dsgn_dntn_biz_id);


--
-- Name: g_dsgn_dntn_biz_ntc_cn_img_expln g_dsgn_dntn_biz_ntc_cn_img_expln_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_ntc_cn_img_expln
    ADD CONSTRAINT g_dsgn_dntn_biz_ntc_cn_img_expln_pkey PRIMARY KEY (dsgn_dntn_biz_ntc_id, img_seq);


--
-- Name: g_dsgn_dntn_biz_ntc_file_mng g_dsgn_dntn_biz_ntc_file_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_ntc_file_mng
    ADD CONSTRAINT g_dsgn_dntn_biz_ntc_file_mng_pkey PRIMARY KEY (dsgn_dntn_biz_ntc_id, file_seq);


--
-- Name: g_dsgn_dntn_biz_ntc_mng g_dsgn_dntn_biz_ntc_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_ntc_mng
    ADD CONSTRAINT g_dsgn_dntn_biz_ntc_mng_pkey PRIMARY KEY (dsgn_dntn_biz_ntc_id);


--
-- Name: g_dsgn_dntn_biz_user_msg g_dsgn_dntn_biz_user_msg_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_dntn_biz_user_msg
    ADD CONSTRAINT g_dsgn_dntn_biz_user_msg_pkey PRIMARY KEY (dntn_sn);


--
-- Name: g_dsgn_prj_notice g_dsgn_prj_notice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgn_prj_notice
    ADD CONSTRAINT g_dsgn_prj_notice_pkey PRIMARY KEY (prj_notice_id);


--
-- Name: g_dsgncntr_part_mng g_dsgncntr_part_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_part_mng
    ADD CONSTRAINT g_dsgncntr_part_mng_pkey PRIMARY KEY (dsgncntr_part_id);


--
-- Name: g_dsgncntr_part_user_mppng g_dsgncntr_part_user_mppng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_part_user_mppng
    ADD CONSTRAINT g_dsgncntr_part_user_mppng_pkey PRIMARY KEY (user_id);


--
-- Name: g_dsgncntr_prj_img g_dsgncntr_prj_img_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_img
    ADD CONSTRAINT g_dsgncntr_prj_img_pkey PRIMARY KEY (dsgncntr_prj_image_id);


--
-- Name: g_dsgncntr_prj_mng g_dsgncntr_prj_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_mng
    ADD CONSTRAINT g_dsgncntr_prj_mng_pkey PRIMARY KEY (prj_id);


--
-- Name: g_dsgncntr_prj_notice_file g_dsgncntr_prj_notice_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_notice_file
    ADD CONSTRAINT g_dsgncntr_prj_notice_file_pkey PRIMARY KEY (prj_notice_id, file_seq);


--
-- Name: g_dsgncntr_prj_notice_img_desc g_dsgncntr_prj_notice_img_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_notice_img_desc
    ADD CONSTRAINT g_dsgncntr_prj_notice_img_desc_pkey PRIMARY KEY (prj_notice_id, img_seq);


--
-- Name: g_dsgncntr_prj_notice g_dsgncntr_prj_notice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dsgncntr_prj_notice
    ADD CONSTRAINT g_dsgncntr_prj_notice_pkey PRIMARY KEY (prj_notice_id);


--
-- Name: g_dy_gramt g_dy_gramt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_dy_gramt
    ADD CONSTRAINT g_dy_gramt_pkey PRIMARY KEY (created_at);


--
-- Name: g_giro g_giro_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_giro
    ADD CONSTRAINT g_giro_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_honor_benefit g_honor_benefit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_honor_benefit
    ADD CONSTRAINT g_honor_benefit_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_honor_cntrbtr g_honor_cntrbtr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_honor_cntrbtr
    ADD CONSTRAINT g_honor_cntrbtr_pkey PRIMARY KEY (stdr_year, locgov_code, user_id);


--
-- Name: g_honor_cntrbtr_stdr g_honor_cntrbtr_stdr_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_honor_cntrbtr_stdr
    ADD CONSTRAINT g_honor_cntrbtr_stdr_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_instt_code g_instt_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_instt_code
    ADD CONSTRAINT g_instt_code_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_intrst_locgov g_intrst_locgov_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_intrst_locgov
    ADD CONSTRAINT g_intrst_locgov_pkey PRIMARY KEY (user_id, locgov_code);


--
-- Name: g_lclgv_hnr_user_rwrd_img_expln g_lclgv_hnr_user_rwrd_img_expln_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_hnr_user_rwrd_img_expln
    ADD CONSTRAINT g_lclgv_hnr_user_rwrd_img_expln_pkey PRIMARY KEY (lclgv_cd, img_seq);


--
-- Name: g_lclgv_hnr_user_stng_mng g_lclgv_hnr_user_stng_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_hnr_user_stng_mng
    ADD CONSTRAINT g_lclgv_hnr_user_stng_mng_pkey PRIMARY KEY (lclgv_cd);


--
-- Name: g_link_instt_enc_key_mng g_link_instt_enc_key_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_link_instt_enc_key_mng
    ADD CONSTRAINT g_link_instt_enc_key_mng_pkey PRIMARY KEY (enc_key_ver);


--
-- Name: g_locgov_dept_hist g_locgov_dept_hist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_locgov_dept_hist
    ADD CONSTRAINT g_locgov_dept_hist_pkey PRIMARY KEY (locgov_code, dept_hist_no);


--
-- Name: g_locgov_fav_item_mng g_locgov_fav_item_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_locgov_fav_item_mng
    ADD CONSTRAINT g_locgov_fav_item_mng_pkey PRIMARY KEY (locgov_code, catalog_year, catalog_no);


--
-- Name: g_locgov_image g_locgov_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_locgov_image
    ADD CONSTRAINT g_locgov_image_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_locgov g_locgov_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_locgov
    ADD CONSTRAINT g_locgov_pkey PRIMARY KEY (locgov_code);


--
-- Name: g_next_buga_request g_next_buga_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_next_buga_request
    ADD CONSTRAINT g_next_buga_request_pkey PRIMARY KEY (link_mng_key);


--
-- Name: g_next_buga_request_temp g_next_buga_request_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_next_buga_request_temp
    ADD CONSTRAINT g_next_buga_request_temp_pkey PRIMARY KEY (link_mng_key);


--
-- Name: g_next_sunap_response g_next_sunap_response_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_next_sunap_response
    ADD CONSTRAINT g_next_sunap_response_pkey PRIMARY KEY (link_mng_key);


--
-- Name: g_pay_log g_pay_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_pay_log
    ADD CONSTRAINT g_pay_log_pkey PRIMARY KEY (pay_log_id);


--
-- Name: g_prj_content_img_desc g_prj_content_img_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_prj_content_img_desc
    ADD CONSTRAINT g_prj_content_img_desc_pkey PRIMARY KEY (prj_content_img_id);


--
-- Name: g_relay_log g_relay_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_relay_log
    ADD CONSTRAINT g_relay_log_pkey PRIMARY KEY (relay_log_id);


--
-- Name: gif_etax_daesa gif_etax_daesa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_etax_daesa
    ADD CONSTRAINT gif_etax_daesa_pkey PRIMARY KEY (if_no);


--
-- Name: gif_etax_sunap_etc gif_etax_sunap_etc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_etax_sunap_etc
    ADD CONSTRAINT gif_etax_sunap_etc_pkey PRIMARY KEY (if_no);


--
-- Name: gif_etax_sunap gif_etax_sunap_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_etax_sunap
    ADD CONSTRAINT gif_etax_sunap_pkey PRIMARY KEY (if_no);


--
-- Name: gif_external_mst gif_external_mst_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_external_mst
    ADD CONSTRAINT gif_external_mst_pkey PRIMARY KEY (if_no);


--
-- Name: gif_hometax gif_hometax_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_hometax
    ADD CONSTRAINT gif_hometax_pkey PRIMARY KEY (if_no);


--
-- Name: gif_seoul gif_seoul_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_seoul
    ADD CONSTRAINT gif_seoul_pkey PRIMARY KEY (if_no);


--
-- Name: gif_stnd_buga_now gif_stnd_buga_now_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_stnd_buga_now
    ADD CONSTRAINT gif_stnd_buga_now_pkey PRIMARY KEY (if_no);


--
-- Name: gif_stnd_jijache_now gif_stnd_jijache_now_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_stnd_jijache_now
    ADD CONSTRAINT gif_stnd_jijache_now_pkey PRIMARY KEY (locgov_code);


--
-- Name: gif_stnd_sunap_now gif_stnd_sunap_now_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gif_stnd_sunap_now
    ADD CONSTRAINT gif_stnd_sunap_now_pkey PRIMARY KEY (if_no);


--
-- Name: op_common_code op_common_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_common_code
    ADD CONSTRAINT op_common_code_pkey PRIMARY KEY (code_type, code_language, id);


--
-- Name: op_event_code_log op_event_code_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_event_code_log
    ADD CONSTRAINT op_event_code_log_pkey PRIMARY KEY (id);


--
-- Name: op_event_code op_event_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_event_code
    ADD CONSTRAINT op_event_code_pkey PRIMARY KEY (id);


--
-- Name: op_event_item op_event_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_event_item
    ADD CONSTRAINT op_event_item_pkey PRIMARY KEY (id);


--
-- Name: op_event_reply op_event_reply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_event_reply
    ADD CONSTRAINT op_event_reply_pkey PRIMARY KEY (id);


--
-- Name: op_honor_view_hist op_honor_view_hist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_honor_view_hist
    ADD CONSTRAINT op_honor_view_hist_pkey PRIMARY KEY (view_dt, user_id, lclgv_cd);


--
-- Name: op_island op_island_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_island
    ADD CONSTRAINT op_island_pkey PRIMARY KEY (id);


--
-- Name: op_spel_dstr_zn op_spel_dstr_zn_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_spel_dstr_zn
    ADD CONSTRAINT op_spel_dstr_zn_pkey PRIMARY KEY (id);


--
-- Name: g_cntr fk_g_cntr_cntr_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr
    ADD CONSTRAINT fk_g_cntr_cntr_locgov_code FOREIGN KEY (cntr_locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_cntr_lmtt fk_g_cntr_lmtt_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_lmtt
    ADD CONSTRAINT fk_g_cntr_lmtt_locgov_code FOREIGN KEY (locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_cntr fk_g_cntr_psitn_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr
    ADD CONSTRAINT fk_g_cntr_psitn_locgov_code FOREIGN KEY (psitn_locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_ctbny_opratn_file fk_g_ctbny_opratn_file_regist_sn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn_file
    ADD CONSTRAINT fk_g_ctbny_opratn_file_regist_sn FOREIGN KEY (regist_sn) REFERENCES public.g_ctbny_opratn(regist_sn);


--
-- Name: g_ctbny_opratn fk_g_ctbny_opratn_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_ctbny_opratn
    ADD CONSTRAINT fk_g_ctbny_opratn_locgov_code FOREIGN KEY (locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_honor_cntrbtr fk_g_honor_cntrbtr_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_honor_cntrbtr
    ADD CONSTRAINT fk_g_honor_cntrbtr_locgov_code FOREIGN KEY (locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_intrst_locgov fk_g_intrst_locgov_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_intrst_locgov
    ADD CONSTRAINT fk_g_intrst_locgov_locgov_code FOREIGN KEY (locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_locgov_dept_hist fk_g_locgov_dept_hist_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_locgov_dept_hist
    ADD CONSTRAINT fk_g_locgov_dept_hist_locgov_code FOREIGN KEY (locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: g_relay_log fk_g_relay_log_cntr_locgov_code; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_relay_log
    ADD CONSTRAINT fk_g_relay_log_cntr_locgov_code FOREIGN KEY (cntr_locgov_code) REFERENCES public.g_locgov(locgov_code);


--
-- Name: TABLE donation_levy; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.donation_levy TO donation;


--
-- Name: TABLE g_adm_locgov; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_adm_locgov TO donation;


--
-- Name: TABLE g_agency_login_confirm_info; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_agency_login_confirm_info TO donation;


--
-- Name: TABLE g_catalog_card_news_img_desc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_catalog_card_news_img_desc TO donation;


--
-- Name: TABLE g_catalog_card_news_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_catalog_card_news_mng TO donation;


--
-- Name: SEQUENCE g_catalog_card_news_mng_card_news_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_catalog_card_news_mng_card_news_id_seq TO donation;


--
-- Name: TABLE g_catalog_content_img_desc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_catalog_content_img_desc TO donation;


--
-- Name: TABLE g_catalog_content_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_catalog_content_mng TO donation;


--
-- Name: TABLE g_catalog_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_catalog_mng TO donation;


--
-- Name: TABLE g_cheer_msg; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cheer_msg TO donation;


--
-- Name: TABLE g_cntr; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr TO donation;


--
-- Name: TABLE g_cntr_chenap; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_chenap TO donation;


--
-- Name: SEQUENCE g_cntr_chenap_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_cntr_chenap_id_seq TO donation;


--
-- Name: TABLE g_cntr_lmtt; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_lmtt TO donation;


--
-- Name: TABLE g_cntr_locgov_history; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_locgov_history TO donation;


--
-- Name: TABLE g_cntr_rcipt; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_rcipt TO donation;


--
-- Name: TABLE g_cntr_reqmng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_reqmng TO donation;


--
-- Name: SEQUENCE g_cntr_reqmng_req_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_cntr_reqmng_req_id_seq TO donation;


--
-- Name: TABLE g_cntr_tax_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_tax_log TO donation;


--
-- Name: TABLE g_cntr_tax_log3; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_tax_log3 TO donation;


--
-- Name: SEQUENCE g_cntr_tax_log_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_cntr_tax_log_log_id_seq TO donation;


--
-- Name: TABLE g_cntr_tax_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_tax_temp TO donation;


--
-- Name: TABLE g_cntr_tax_temp_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_tax_temp_log TO donation;


--
-- Name: TABLE g_cntr_temp2; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_temp2 TO donation;


--
-- Name: TABLE g_cntr_wegive; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_wegive TO donation;


--
-- Name: SEQUENCE g_cntr_wegive_cntr_sn_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_cntr_wegive_cntr_sn_seq TO donation;


--
-- Name: TABLE g_ctbny_opratn; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_ctbny_opratn TO donation;


--
-- Name: TABLE g_ctbny_opratn_file; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_ctbny_opratn_file TO donation;


--
-- Name: SEQUENCE g_ctbny_opratn_file_regist_file_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_ctbny_opratn_file_regist_file_id_seq TO donation;


--
-- Name: SEQUENCE g_ctbny_opratn_regist_sn_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_ctbny_opratn_regist_sn_seq TO donation;


--
-- Name: TABLE g_ctbny_setup; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_ctbny_setup TO donation;


--
-- Name: TABLE g_disaster_zone; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_disaster_zone TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_aprv_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_aprv_log TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_cn_img_expln; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_cn_img_expln TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_cn_img_expln_dsgn_dntn_biz_cn_img_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_dept_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_dept_mng TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_dept_mngr_mpng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_dept_mngr_mpng TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_img_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_img_mng TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_img_mng_dsgn_dntn_biz_img_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_mng TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_mng_dsgn_dntn_biz_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_ntc_cn_img_expln; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_ntc_cn_img_expln TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_ntc_file_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_ntc_file_mng TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_ntc_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_ntc_mng TO donation;


--
-- Name: SEQUENCE g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_dntn_biz_ntc_mng_dsgn_dntn_biz_ntc_id_seq TO donation;


--
-- Name: TABLE g_dsgn_dntn_biz_user_msg; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_dntn_biz_user_msg TO donation;


--
-- Name: TABLE g_dsgn_prj_notice; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgn_prj_notice TO donation;


--
-- Name: SEQUENCE g_dsgn_prj_notice_prj_notice_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgn_prj_notice_prj_notice_id_seq TO donation;


--
-- Name: TABLE g_dsgncntr_part_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_part_mng TO donation;


--
-- Name: SEQUENCE g_dsgncntr_part_mng_dsgncntr_part_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgncntr_part_mng_dsgncntr_part_id_seq TO donation;


--
-- Name: TABLE g_dsgncntr_part_user_mppng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_part_user_mppng TO donation;


--
-- Name: TABLE g_dsgncntr_prj_img; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_prj_img TO donation;


--
-- Name: SEQUENCE g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgncntr_prj_img_dsgncntr_prj_image_id_seq TO donation;


--
-- Name: TABLE g_dsgncntr_prj_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_prj_mng TO donation;


--
-- Name: SEQUENCE g_dsgncntr_prj_mng_prj_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgncntr_prj_mng_prj_id_seq TO donation;


--
-- Name: TABLE g_dsgncntr_prj_notice; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_prj_notice TO donation;


--
-- Name: TABLE g_dsgncntr_prj_notice_file; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_prj_notice_file TO donation;


--
-- Name: TABLE g_dsgncntr_prj_notice_img_desc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dsgncntr_prj_notice_img_desc TO donation;


--
-- Name: SEQUENCE g_dsgncntr_prj_notice_prj_notice_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_dsgncntr_prj_notice_prj_notice_id_seq TO donation;


--
-- Name: TABLE g_dy_gramt; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_dy_gramt TO donation;


--
-- Name: TABLE g_giro; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_giro TO donation;


--
-- Name: TABLE g_honor_benefit; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_honor_benefit TO donation;


--
-- Name: TABLE g_honor_cntrbtr; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_honor_cntrbtr TO donation;


--
-- Name: TABLE g_honor_cntrbtr_stdr; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_honor_cntrbtr_stdr TO donation;


--
-- Name: TABLE g_instt_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_instt_code TO donation;


--
-- Name: TABLE g_intrst_locgov; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_intrst_locgov TO donation;


--
-- Name: TABLE g_lclgv_hnr_user_rwrd_img_expln; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_lclgv_hnr_user_rwrd_img_expln TO donation;


--
-- Name: TABLE g_lclgv_hnr_user_stng_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_lclgv_hnr_user_stng_mng TO donation;


--
-- Name: TABLE g_link_instt_enc_key_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_link_instt_enc_key_mng TO donation;


--
-- Name: TABLE g_locgov; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_locgov TO donation;


--
-- Name: TABLE g_locgov_dept_hist; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_locgov_dept_hist TO donation;


--
-- Name: TABLE g_locgov_fav_item_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_locgov_fav_item_mng TO donation;


--
-- Name: TABLE g_locgov_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_locgov_image TO donation;


--
-- Name: TABLE g_next_buga_request; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_next_buga_request TO donation;


--
-- Name: TABLE g_next_buga_request_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_next_buga_request_temp TO donation;


--
-- Name: TABLE g_next_sunap_response; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_next_sunap_response TO donation;


--
-- Name: TABLE g_pay_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_pay_log TO donation;


--
-- Name: SEQUENCE g_pay_log_pay_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_pay_log_pay_log_id_seq TO donation;


--
-- Name: TABLE g_prj_content_img_desc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_prj_content_img_desc TO donation;


--
-- Name: SEQUENCE g_prj_content_img_desc_prj_content_img_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_prj_content_img_desc_prj_content_img_id_seq TO donation;


--
-- Name: TABLE g_relay_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_relay_log TO donation;


--
-- Name: SEQUENCE g_relay_log_relay_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_relay_log_relay_log_id_seq TO donation;


--
-- Name: TABLE gif_etax_daesa; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_etax_daesa TO donation;


--
-- Name: TABLE gif_etax_sunap; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_etax_sunap TO donation;


--
-- Name: TABLE gif_etax_sunap_etc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_etax_sunap_etc TO donation;


--
-- Name: TABLE gif_external_mst; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_external_mst TO donation;


--
-- Name: TABLE gif_hometax; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_hometax TO donation;


--
-- Name: TABLE gif_seoul; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_seoul TO donation;


--
-- Name: TABLE gif_stnd_buga_now; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_stnd_buga_now TO donation;


--
-- Name: TABLE gif_stnd_jijache_now; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_stnd_jijache_now TO donation;


--
-- Name: TABLE gif_stnd_sunap_now; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gif_stnd_sunap_now TO donation;


--
-- Name: TABLE op_common_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_common_code TO donation;


--
-- Name: TABLE op_event_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_event_code TO donation;


--
-- Name: TABLE op_event_code_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_event_code_log TO donation;


--
-- Name: TABLE op_event_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_event_item TO donation;


--
-- Name: TABLE op_event_reply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_event_reply TO donation;


--
-- Name: TABLE op_honor_view_hist; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_honor_view_hist TO donation;


--
-- Name: TABLE op_island; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_island TO donation;


--
-- Name: TABLE op_sido_mapping; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_sido_mapping TO donation;


--
-- Name: TABLE op_spel_dstr_zn; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_spel_dstr_zn TO donation;


--
-- Name: SEQUENCE op_spel_dstr_zn_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_spel_dstr_zn_id_seq TO donation;


--
-- Name: TABLE view_search_locgov; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.view_search_locgov TO donation;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO donation;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO donation;


--
-- PostgreSQL database dump complete
--

\unrestrict BuGAacFr4VxyhNUg2Uah1iuH4glFu1VuEJA3aNnH2JCrnu20fJ44fKqMlb60OXO

