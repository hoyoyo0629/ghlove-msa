--
-- PostgreSQL database dump
--

\restrict DtQdBd8YPM9OvRR8Ftzqs0p2tP8lScJaGlXeXnn9ZhAA565QyxfIvCYkmvkhsOi

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

ALTER TABLE IF EXISTS ONLY public.gift_subcategory_item DROP CONSTRAINT IF EXISTS gift_subcategory_item_subcategory_id_fkey;
ALTER TABLE IF EXISTS ONLY public.op_style_book_item DROP CONSTRAINT IF EXISTS fk_op_style_book_item_style_book_id;
ALTER TABLE IF EXISTS ONLY public.op_restock_notice DROP CONSTRAINT IF EXISTS fk_op_restock_notice_item_id;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_return DROP CONSTRAINT IF EXISTS fk_op_mall_order_return_mall_order_id;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_exchange DROP CONSTRAINT IF EXISTS fk_op_mall_order_exchange_mall_order_id;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_cancel DROP CONSTRAINT IF EXISTS fk_op_mall_order_cancel_mall_order_id;
ALTER TABLE IF EXISTS ONLY public.op_main_display_item DROP CONSTRAINT IF EXISTS fk_op_main_display_item_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_set DROP CONSTRAINT IF EXISTS fk_op_item_set_parent_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_relation DROP CONSTRAINT IF EXISTS fk_op_item_relation_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_other DROP CONSTRAINT IF EXISTS fk_op_item_other_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_ordering DROP CONSTRAINT IF EXISTS fk_op_item_ordering_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_option_image DROP CONSTRAINT IF EXISTS fk_op_item_option_image_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_mall_code DROP CONSTRAINT IF EXISTS fk_op_item_mall_code_mall_config_id;
ALTER TABLE IF EXISTS ONLY public.op_item_mall_code DROP CONSTRAINT IF EXISTS fk_op_item_mall_code_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_info_mobile DROP CONSTRAINT IF EXISTS fk_op_item_info_mobile_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_info DROP CONSTRAINT IF EXISTS fk_op_item_info_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_addition DROP CONSTRAINT IF EXISTS fk_op_item_addition_item_id;
ALTER TABLE IF EXISTS ONLY public.op_item_addition DROP CONSTRAINT IF EXISTS fk_op_item_addition_addition_item_id;
ALTER TABLE IF EXISTS ONLY public.op_gift_item_relation DROP CONSTRAINT IF EXISTS fk_op_gift_item_relation_gift_item_id;
ALTER TABLE IF EXISTS ONLY public.op_gift_group_item DROP CONSTRAINT IF EXISTS fk_op_gift_group_item_gift_item_id;
ALTER TABLE IF EXISTS ONLY public.op_gift_group_item DROP CONSTRAINT IF EXISTS fk_op_gift_group_item_gift_group_id;
ALTER TABLE IF EXISTS ONLY public.op_filter_code DROP CONSTRAINT IF EXISTS fk_op_filter_code_filter_group_id;
ALTER TABLE IF EXISTS ONLY public.op_featured_reply DROP CONSTRAINT IF EXISTS fk_op_featured_reply_featured_id;
ALTER TABLE IF EXISTS ONLY public.op_featured_item DROP CONSTRAINT IF EXISTS fk_op_featured_item_item_id;
ALTER TABLE IF EXISTS ONLY public.op_featured_banner_item DROP CONSTRAINT IF EXISTS fk_op_featured_banner_item_item_id;
ALTER TABLE IF EXISTS ONLY public.op_category_group DROP CONSTRAINT IF EXISTS fk_op_category_group_category_team_id;
ALTER TABLE IF EXISTS ONLY public.op_category_group_banner DROP CONSTRAINT IF EXISTS fk_op_category_group_banner_category_group_id;
ALTER TABLE IF EXISTS ONLY public.op_wishlist DROP CONSTRAINT IF EXISTS op_wishlist_pkey;
ALTER TABLE IF EXISTS ONLY public.op_style_book DROP CONSTRAINT IF EXISTS op_style_book_pkey;
ALTER TABLE IF EXISTS ONLY public.op_style_book_item DROP CONSTRAINT IF EXISTS op_style_book_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_store DROP CONSTRAINT IF EXISTS op_store_pkey;
ALTER TABLE IF EXISTS ONLY public.op_seller_user_login DROP CONSTRAINT IF EXISTS op_seller_user_login_pkey;
ALTER TABLE IF EXISTS ONLY public.op_seller DROP CONSTRAINT IF EXISTS op_seller_pkey;
ALTER TABLE IF EXISTS ONLY public.op_review_filter DROP CONSTRAINT IF EXISTS op_review_filter_pkey;
ALTER TABLE IF EXISTS ONLY public.op_restock_notice DROP CONSTRAINT IF EXISTS op_restock_notice_pkey;
ALTER TABLE IF EXISTS ONLY public.op_ranking DROP CONSTRAINT IF EXISTS op_ranking_pkey;
ALTER TABLE IF EXISTS ONLY public.op_ranking_config DROP CONSTRAINT IF EXISTS op_ranking_config_pkey;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_return DROP CONSTRAINT IF EXISTS op_mall_order_return_pkey;
ALTER TABLE IF EXISTS ONLY public.op_mall_order DROP CONSTRAINT IF EXISTS op_mall_order_pkey;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_exchange DROP CONSTRAINT IF EXISTS op_mall_order_exchange_pkey;
ALTER TABLE IF EXISTS ONLY public.op_mall_order_cancel DROP CONSTRAINT IF EXISTS op_mall_order_cancel_pkey;
ALTER TABLE IF EXISTS ONLY public.op_mall_config DROP CONSTRAINT IF EXISTS op_mall_config_pkey;
ALTER TABLE IF EXISTS ONLY public.op_keyword DROP CONSTRAINT IF EXISTS op_keyword_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_set DROP CONSTRAINT IF EXISTS op_item_set_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_sale_edit DROP CONSTRAINT IF EXISTS op_item_sale_edit_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_review DROP CONSTRAINT IF EXISTS op_item_review_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_review_like DROP CONSTRAINT IF EXISTS op_item_review_like_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_review_image DROP CONSTRAINT IF EXISTS op_item_review_image_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_review_filter DROP CONSTRAINT IF EXISTS op_item_review_filter_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_relation DROP CONSTRAINT IF EXISTS op_item_relation_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item DROP CONSTRAINT IF EXISTS op_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_other DROP CONSTRAINT IF EXISTS op_item_other_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_ordering DROP CONSTRAINT IF EXISTS op_item_ordering_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_option_soldout DROP CONSTRAINT IF EXISTS op_item_option_soldout_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_option DROP CONSTRAINT IF EXISTS op_item_option_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_option_image DROP CONSTRAINT IF EXISTS op_item_option_image_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_notice DROP CONSTRAINT IF EXISTS op_item_notice_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_log DROP CONSTRAINT IF EXISTS op_item_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_info DROP CONSTRAINT IF EXISTS op_item_info_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_info_mobile DROP CONSTRAINT IF EXISTS op_item_info_mobile_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_image DROP CONSTRAINT IF EXISTS op_item_image_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_filter DROP CONSTRAINT IF EXISTS op_item_filter_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_category DROP CONSTRAINT IF EXISTS op_item_category_pkey;
ALTER TABLE IF EXISTS ONLY public.op_item_addition DROP CONSTRAINT IF EXISTS op_item_addition_pkey;
ALTER TABLE IF EXISTS ONLY public.op_gift_item_relation DROP CONSTRAINT IF EXISTS op_gift_item_relation_pkey;
ALTER TABLE IF EXISTS ONLY public.op_gift_item DROP CONSTRAINT IF EXISTS op_gift_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_gift_item_log DROP CONSTRAINT IF EXISTS op_gift_item_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_gift_group DROP CONSTRAINT IF EXISTS op_gift_group_pkey;
ALTER TABLE IF EXISTS ONLY public.op_gift_group_item DROP CONSTRAINT IF EXISTS op_gift_group_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_filter_group DROP CONSTRAINT IF EXISTS op_filter_group_pkey;
ALTER TABLE IF EXISTS ONLY public.op_filter_code DROP CONSTRAINT IF EXISTS op_filter_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_featured_reply DROP CONSTRAINT IF EXISTS op_featured_reply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_featured DROP CONSTRAINT IF EXISTS op_featured_pkey;
ALTER TABLE IF EXISTS ONLY public.op_featured_item DROP CONSTRAINT IF EXISTS op_featured_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_featured_banner DROP CONSTRAINT IF EXISTS op_featured_banner_pkey;
ALTER TABLE IF EXISTS ONLY public.op_display_template DROP CONSTRAINT IF EXISTS op_display_template_pkey;
ALTER TABLE IF EXISTS ONLY public.op_display_sns DROP CONSTRAINT IF EXISTS op_display_sns_pkey;
ALTER TABLE IF EXISTS ONLY public.op_display_group_code DROP CONSTRAINT IF EXISTS op_display_group_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_common_code DROP CONSTRAINT IF EXISTS op_common_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_team DROP CONSTRAINT IF EXISTS op_category_team_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_team_item DROP CONSTRAINT IF EXISTS op_category_team_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category DROP CONSTRAINT IF EXISTS op_category_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_group DROP CONSTRAINT IF EXISTS op_category_group_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_group_banner DROP CONSTRAINT IF EXISTS op_category_group_banner_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_filter DROP CONSTRAINT IF EXISTS op_category_filter_pkey;
ALTER TABLE IF EXISTS ONLY public.op_category_edit DROP CONSTRAINT IF EXISTS op_category_edit_pkey;
ALTER TABLE IF EXISTS ONLY public.op_brand DROP CONSTRAINT IF EXISTS op_brand_pkey;
ALTER TABLE IF EXISTS ONLY public.op_brand_category DROP CONSTRAINT IF EXISTS op_brand_category_pkey;
ALTER TABLE IF EXISTS ONLY public.mig_op_item_image DROP CONSTRAINT IF EXISTS mig_op_item_image_pkey;
ALTER TABLE IF EXISTS ONLY public.gift_subcategory DROP CONSTRAINT IF EXISTS gift_subcategory_pkey;
ALTER TABLE IF EXISTS ONLY public.gift_subcategory_item DROP CONSTRAINT IF EXISTS gift_subcategory_item_pkey;
ALTER TABLE IF EXISTS ONLY public.gift_order_stock DROP CONSTRAINT IF EXISTS gift_order_stock_pkey;
ALTER TABLE IF EXISTS ONLY public.g_spcl_item DROP CONSTRAINT IF EXISTS g_spcl_item_pkey;
ALTER TABLE IF EXISTS ONLY public.g_spcl_item_mng DROP CONSTRAINT IF EXISTS g_spcl_item_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_spcl_item_mng_keyword DROP CONSTRAINT IF EXISTS g_spcl_item_mng_keyword_pkey;
ALTER TABLE IF EXISTS ONLY public.g_season_food DROP CONSTRAINT IF EXISTS g_season_food_pkey;
ALTER TABLE IF EXISTS ONLY public.g_season_food_item DROP CONSTRAINT IF EXISTS g_season_food_item_pkey;
ALTER TABLE IF EXISTS ONLY public.g_myrecent DROP CONSTRAINT IF EXISTS g_myrecent_pkey;
ALTER TABLE IF EXISTS ONLY public.g_lclgv_rprs_gds_mng DROP CONSTRAINT IF EXISTS g_lclgv_rprs_gds_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_lclgv_pbadms_wlfr_cntr_mng DROP CONSTRAINT IF EXISTS g_lclgv_pbadms_wlfr_cntr_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_lclgv_off_rprs_gds_mng DROP CONSTRAINT IF EXISTS g_lclgv_off_rprs_gds_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_item_inquiry DROP CONSTRAINT IF EXISTS g_item_inquiry_pkey;
ALTER TABLE IF EXISTS ONLY public.g_item_content_img_desc DROP CONSTRAINT IF EXISTS g_item_content_img_desc_pkey;
ALTER TABLE IF EXISTS ONLY public.g_intrst_rtnpsnt DROP CONSTRAINT IF EXISTS g_intrst_rtnpsnt_pkey;
ALTER TABLE IF EXISTS ONLY public.g_gds_img_expln_link_view DROP CONSTRAINT IF EXISTS g_gds_img_expln_link_view_pkey;
ALTER TABLE IF EXISTS ONLY public.b_gift_sell DROP CONSTRAINT IF EXISTS b_gift_sell_pkey;
ALTER TABLE IF EXISTS public.op_wishlist ALTER COLUMN wishlist_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_seller ALTER COLUMN seller_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_set ALTER COLUMN item_set_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_review_image ALTER COLUMN item_review_image_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_review ALTER COLUMN item_review_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_ordering ALTER COLUMN item_ordering_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_log ALTER COLUMN item_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item_image ALTER COLUMN item_image_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_item ALTER COLUMN item_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_featured_item ALTER COLUMN reg_seq DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_category_team ALTER COLUMN category_team_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_category_group ALTER COLUMN category_group_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_brand ALTER COLUMN brand_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gift_subcategory_item ALTER COLUMN subcategory_item_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gift_subcategory ALTER COLUMN subcategory_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_lclgv_pbadms_wlfr_cntr_mng ALTER COLUMN pbadms_wlfr_cntr_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_item_inquiry ALTER COLUMN inquiry_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.g_item_content_img_desc ALTER COLUMN item_content_img_id DROP DEFAULT;
DROP VIEW IF EXISTS public.view_search_event;
DROP SEQUENCE IF EXISTS public.op_wishlist_wishlist_id_seq;
DROP TABLE IF EXISTS public.op_wishlist;
DROP TABLE IF EXISTS public.op_style_book_item;
DROP TABLE IF EXISTS public.op_style_book;
DROP TABLE IF EXISTS public.op_store;
DROP TABLE IF EXISTS public.op_seller_user_login;
DROP TABLE IF EXISTS public.op_seller_user;
DROP SEQUENCE IF EXISTS public.op_seller_seller_id_seq;
DROP TABLE IF EXISTS public.op_seller;
DROP TABLE IF EXISTS public.op_review_filter;
DROP TABLE IF EXISTS public.op_restock_notice;
DROP TABLE IF EXISTS public.op_ranking_config;
DROP TABLE IF EXISTS public.op_ranking_batch;
DROP TABLE IF EXISTS public.op_ranking;
DROP TABLE IF EXISTS public.op_mobile_category_edit;
DROP TABLE IF EXISTS public.op_mall_order_return;
DROP TABLE IF EXISTS public.op_mall_order_exchange;
DROP TABLE IF EXISTS public.op_mall_order_cancel;
DROP TABLE IF EXISTS public.op_mall_order;
DROP TABLE IF EXISTS public.op_mall_config;
DROP TABLE IF EXISTS public.op_main_display_item;
DROP TABLE IF EXISTS public.op_keyword;
DROP SEQUENCE IF EXISTS public.op_item_set_item_set_id_seq;
DROP TABLE IF EXISTS public.op_item_set;
DROP TABLE IF EXISTS public.op_item_sale_edit;
DROP SEQUENCE IF EXISTS public.op_item_review_review_id_seq;
DROP TABLE IF EXISTS public.op_item_review_like;
DROP SEQUENCE IF EXISTS public.op_item_review_image_item_review_image_id_seq;
DROP SEQUENCE IF EXISTS public.op_item_review_image_id_seq;
DROP TABLE IF EXISTS public.op_item_review_image;
DROP TABLE IF EXISTS public.op_item_review_filter;
DROP TABLE IF EXISTS public.op_item_review;
DROP TABLE IF EXISTS public.op_item_relation;
DROP TABLE IF EXISTS public.op_item_other;
DROP SEQUENCE IF EXISTS public.op_item_ordering_item_ordering_id_seq;
DROP TABLE IF EXISTS public.op_item_ordering;
DROP TABLE IF EXISTS public.op_item_option_soldout;
DROP TABLE IF EXISTS public.op_item_option_image;
DROP TABLE IF EXISTS public.op_item_option;
DROP TABLE IF EXISTS public.op_item_notice;
DROP TABLE IF EXISTS public.op_item_mall_code;
DROP SEQUENCE IF EXISTS public.op_item_log_item_log_id_seq;
DROP TABLE IF EXISTS public.op_item_log;
DROP SEQUENCE IF EXISTS public.op_item_item_id_seq;
DROP TABLE IF EXISTS public.op_item_info_mobile;
DROP TABLE IF EXISTS public.op_item_info;
DROP SEQUENCE IF EXISTS public.op_item_image_id_seq;
DROP TABLE IF EXISTS public.op_item_image;
DROP TABLE IF EXISTS public.op_item_hit;
DROP TABLE IF EXISTS public.op_item_filter;
DROP TABLE IF EXISTS public.op_item_category;
DROP TABLE IF EXISTS public.op_item_bakup_0611_option;
DROP TABLE IF EXISTS public.op_item_addition;
DROP TABLE IF EXISTS public.op_item;
DROP TABLE IF EXISTS public.op_gift_item_relation;
DROP TABLE IF EXISTS public.op_gift_item_log;
DROP TABLE IF EXISTS public.op_gift_item;
DROP TABLE IF EXISTS public.op_gift_group_item;
DROP TABLE IF EXISTS public.op_gift_group;
DROP TABLE IF EXISTS public.op_filter_group;
DROP TABLE IF EXISTS public.op_filter_code;
DROP TABLE IF EXISTS public.op_featured_reply;
DROP SEQUENCE IF EXISTS public.op_featured_item_reg_seq_seq;
DROP TABLE IF EXISTS public.op_featured_item;
DROP TABLE IF EXISTS public.op_featured_banner_item;
DROP TABLE IF EXISTS public.op_featured_banner;
DROP TABLE IF EXISTS public.op_featured;
DROP TABLE IF EXISTS public.op_display_template;
DROP TABLE IF EXISTS public.op_display_sns;
DROP TABLE IF EXISTS public.op_display_item;
DROP TABLE IF EXISTS public.op_display_image;
DROP TABLE IF EXISTS public.op_display_group_code;
DROP TABLE IF EXISTS public.op_display_editor;
DROP TABLE IF EXISTS public.op_common_code;
DROP TABLE IF EXISTS public.op_category_team_item;
DROP SEQUENCE IF EXISTS public.op_category_team_category_team_id_seq;
DROP TABLE IF EXISTS public.op_category_team;
DROP SEQUENCE IF EXISTS public.op_category_group_category_group_id_seq;
DROP TABLE IF EXISTS public.op_category_group_banner;
DROP TABLE IF EXISTS public.op_category_group;
DROP TABLE IF EXISTS public.op_category_filter;
DROP TABLE IF EXISTS public.op_category_edit;
DROP TABLE IF EXISTS public.op_category;
DROP TABLE IF EXISTS public.op_brand_category;
DROP SEQUENCE IF EXISTS public.op_brand_brand_id_seq;
DROP TABLE IF EXISTS public.op_brand;
DROP TABLE IF EXISTS public.mig_op_item_image;
DROP SEQUENCE IF EXISTS public.gift_subcategory_subcategory_id_seq;
DROP SEQUENCE IF EXISTS public.gift_subcategory_item_subcategory_item_id_seq;
DROP TABLE IF EXISTS public.gift_subcategory_item;
DROP TABLE IF EXISTS public.gift_subcategory;
DROP TABLE IF EXISTS public.gift_order_stock;
DROP TABLE IF EXISTS public.g_spcl_item_mng_keyword;
DROP TABLE IF EXISTS public.g_spcl_item_mng;
DROP TABLE IF EXISTS public.g_spcl_item;
DROP TABLE IF EXISTS public.g_season_food_item;
DROP TABLE IF EXISTS public.g_season_food;
DROP TABLE IF EXISTS public.g_myrecent;
DROP TABLE IF EXISTS public.g_lclgv_rprs_gds_mng;
DROP SEQUENCE IF EXISTS public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq;
DROP TABLE IF EXISTS public.g_lclgv_pbadms_wlfr_cntr_mng;
DROP TABLE IF EXISTS public.g_lclgv_off_rprs_gds_mng;
DROP SEQUENCE IF EXISTS public.g_item_inquiry_inquiry_id_seq;
DROP TABLE IF EXISTS public.g_item_inquiry;
DROP SEQUENCE IF EXISTS public.g_item_content_img_desc_item_content_img_id_seq;
DROP TABLE IF EXISTS public.g_item_content_img_desc;
DROP TABLE IF EXISTS public.g_intrst_rtnpsnt;
DROP TABLE IF EXISTS public.g_gds_img_expln_link_view;
DROP TABLE IF EXISTS public.b_gift_sell;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: b_gift_sell; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.b_gift_sell (
    cntr_ymd character varying(8) NOT NULL,
    upper_locgov_code character varying(10) NOT NULL,
    upper_locgov_nm character varying(50) NOT NULL,
    locgov_code character varying(10) NOT NULL,
    locgov_nm character varying(50) NOT NULL,
    item_id integer NOT NULL,
    item_name character varying(200) NOT NULL,
    sell_cnt bigint DEFAULT 0 NOT NULL,
    sell_price bigint DEFAULT 0 NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_gds_img_expln_link_view; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_gds_img_expln_link_view (
    gds_user_cd character varying(30) NOT NULL,
    img_seq integer NOT NULL,
    img_expln character varying(5000) NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_intrst_rtnpsnt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_intrst_rtnpsnt (
    user_id bigint NOT NULL,
    regist_sn integer NOT NULL,
    locgov_code character varying(10) NOT NULL,
    regist_de character varying(8) NOT NULL,
    category_code character varying(50),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_item_content_img_desc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_item_content_img_desc (
    item_content_img_id bigint NOT NULL,
    item_id bigint NOT NULL,
    img_desc character varying(5000) NOT NULL,
    img_seq integer NOT NULL,
    created_user_id bigint NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_item_content_img_desc_item_content_img_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_item_content_img_desc_item_content_img_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_item_content_img_desc_item_content_img_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_item_content_img_desc_item_content_img_id_seq OWNED BY public.g_item_content_img_desc.item_content_img_id;


--
-- Name: g_item_inquiry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_item_inquiry (
    inquiry_id bigint NOT NULL,
    item_id bigint NOT NULL,
    user_id bigint NOT NULL,
    question text NOT NULL,
    secret_yn character(1) DEFAULT 'N'::bpchar NOT NULL,
    answer text,
    answered_date timestamp without time zone,
    status character varying(20) NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_item_inquiry_inquiry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_item_inquiry_inquiry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_item_inquiry_inquiry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_item_inquiry_inquiry_id_seq OWNED BY public.g_item_inquiry.inquiry_id;


--
-- Name: g_lclgv_off_rprs_gds_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_lclgv_off_rprs_gds_mng (
    lclgv_cd character varying(10) NOT NULL,
    gds_id bigint NOT NULL,
    sort_seq smallint NOT NULL,
    options character varying(4000),
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_lclgv_pbadms_wlfr_cntr_mng (
    pbadms_wlfr_cntr_id bigint NOT NULL,
    lclgv_cd character varying(10) NOT NULL,
    pbadms_wlfr_cntr_nm character varying(50) NOT NULL,
    pbadms_wlfr_cntr_cd character varying(20),
    use_yn character varying(5) DEFAULT 'N'::character varying NOT NULL,
    frst_rgtr_id bigint DEFAULT 0 NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL,
    last_rgtr_id bigint DEFAULT 0 NOT NULL,
    last_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq OWNED BY public.g_lclgv_pbadms_wlfr_cntr_mng.pbadms_wlfr_cntr_id;


--
-- Name: g_lclgv_rprs_gds_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_lclgv_rprs_gds_mng (
    lclgv_cd character varying(10) NOT NULL,
    gds_id bigint NOT NULL,
    sort_seq smallint NOT NULL,
    frst_rgtr_id bigint NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_myrecent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_myrecent (
    recent_id integer NOT NULL,
    keyword character varying(50) NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: g_season_food; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_season_food (
    season_food_month integer NOT NULL,
    reg_seq integer DEFAULT 1 NOT NULL,
    season_food_keyword character varying(1000),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_season_food_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_season_food_item (
    item_id integer DEFAULT 0 NOT NULL,
    season_food_month integer NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_spcl_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_spcl_item (
    spcl_item_mng_id bigint NOT NULL,
    item_id bigint NOT NULL,
    display_order integer NOT NULL,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_spcl_item_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_spcl_item_mng (
    spcl_item_mng_id bigint NOT NULL,
    locgov_code character varying(100) NOT NULL,
    spcl_item_info character varying(1000),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_spcl_item_mng_keyword; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_spcl_item_mng_keyword (
    spcl_item_mng_id bigint NOT NULL,
    reg_seq integer DEFAULT 1 NOT NULL,
    spcl_item_keyword character varying(1000),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: gift_order_stock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gift_order_stock (
    order_id character varying(50) NOT NULL,
    item_id bigint NOT NULL,
    quantity integer NOT NULL,
    status character varying(20) NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: gift_subcategory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gift_subcategory (
    subcategory_id bigint NOT NULL,
    category_code character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    ordering integer NOT NULL
);


--
-- Name: gift_subcategory_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gift_subcategory_item (
    subcategory_item_id bigint NOT NULL,
    subcategory_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    ordering integer NOT NULL
);


--
-- Name: gift_subcategory_item_subcategory_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gift_subcategory_item_subcategory_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gift_subcategory_item_subcategory_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gift_subcategory_item_subcategory_item_id_seq OWNED BY public.gift_subcategory_item.subcategory_item_id;


--
-- Name: gift_subcategory_subcategory_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gift_subcategory_subcategory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gift_subcategory_subcategory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gift_subcategory_subcategory_id_seq OWNED BY public.gift_subcategory.subcategory_id;


--
-- Name: mig_op_item_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mig_op_item_image (
    item_image_id bigint NOT NULL,
    item_id bigint,
    image_name character varying(255),
    ordering integer,
    created_date timestamp without time zone,
    serial_num character varying(50)
);


--
-- Name: op_brand; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_brand (
    brand_id integer NOT NULL,
    brand_name character varying(255) NOT NULL,
    brand_image character varying(255),
    brand_content text,
    display_flag character varying(1),
    updated_user_id bigint DEFAULT 0 NOT NULL,
    updated_date character varying(14),
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14),
    locgov_code character varying(10)
);


--
-- Name: op_brand_brand_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_brand_brand_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_brand_brand_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_brand_brand_id_seq OWNED BY public.op_brand.brand_id;


--
-- Name: op_brand_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_brand_category (
    brand_category_id integer DEFAULT 0 NOT NULL,
    brand_id integer NOT NULL,
    category_id integer DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category (
    category_id integer NOT NULL,
    category_code character varying(50) NOT NULL,
    category_group_id integer,
    category_url character varying(50) NOT NULL,
    category_name character varying(200) NOT NULL,
    category_type character varying(1) NOT NULL,
    category_header character varying,
    category_footer character varying,
    category_banner character varying,
    category_advertisement character varying,
    category_mobile_html character varying,
    category_mobile_html_header character varying,
    category_class1 character varying(3) NOT NULL,
    category_class2 character varying(3) NOT NULL,
    category_class3 character varying(4) NOT NULL,
    category_class4 character varying(4) NOT NULL,
    category_level character varying(1) NOT NULL,
    ordering integer NOT NULL,
    category_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    access_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    background_image character varying(100),
    title character varying,
    keywords character varying,
    description character varying,
    header_contents1 character varying,
    header_contents2 character varying,
    header_contents3 character varying,
    themaword_title character varying,
    themaword_description character varying,
    rank_title character varying,
    rank_keywords character varying,
    rank_description character varying,
    rank_headercontents1 character varying,
    rank_themaword_title character varying,
    rank_themaword_description character varying,
    review_title character varying,
    review_keywords character varying,
    review_description character varying,
    review_headercontents1 character varying,
    review_themaword_title character varying,
    review_themaword_description character varying
);


--
-- Name: op_category_edit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_edit (
    category_edit_id integer DEFAULT 0 NOT NULL,
    code character varying(50) NOT NULL,
    edit_kind character varying(1) NOT NULL,
    edit_position character varying(50) NOT NULL,
    edit_content character varying,
    edit_image character varying(100),
    edit_url character varying(255),
    created_date character varying(14),
    updated_date character varying(14)
);


--
-- Name: op_category_filter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_filter (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    category_id integer NOT NULL,
    filter_group_id bigint NOT NULL,
    ordering integer
);


--
-- Name: op_category_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_group (
    category_group_id integer NOT NULL,
    category_team_id integer NOT NULL,
    name character varying(100),
    code character varying(50),
    created_date character varying(14),
    updated_date character varying(14),
    category_group_flag character varying(1) DEFAULT 'Y'::character varying,
    access_type character varying(1) DEFAULT '1'::character varying,
    defcate character(1),
    title character varying,
    keywords character varying,
    description character varying,
    header_contents1 character varying,
    header_contents2 character varying,
    header_contents3 character varying,
    themaword_title character varying,
    themaword_description character varying,
    rank_title character varying,
    rank_keywords character varying,
    rank_description character varying,
    rank_headercontents1 character varying,
    rank_themaword_title character varying,
    rank_themaword_description character varying,
    ordering integer,
    item_list character varying(250)
);


--
-- Name: op_category_group_banner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_group_banner (
    category_group_banner_id integer NOT NULL,
    category_group_id integer NOT NULL,
    title character varying(255) NOT NULL,
    link_url character varying(255) NOT NULL,
    file_name character varying(255) NOT NULL,
    display_order integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_category_group_category_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_category_group_category_group_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_category_group_category_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_category_group_category_group_id_seq OWNED BY public.op_category_group.category_group_id;


--
-- Name: op_category_team; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_team (
    category_team_id integer NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(50) NOT NULL,
    created_date character varying(14),
    updated_date character varying(14),
    category_team_flag character varying(1) DEFAULT 'Y'::character varying,
    title character varying,
    keywords character varying,
    description character varying,
    header_contents1 character varying,
    header_contents2 character varying,
    header_contents3 character varying,
    themaword_title character varying,
    themaword_description character varying,
    rank_title character varying,
    rank_keywords character varying,
    rank_description character varying,
    rank_headercontents1 character varying,
    rank_themaword_title character varying,
    rank_themaword_description character varying,
    review_title character varying,
    review_keywords character varying,
    review_description character varying,
    review_headercontents1 character varying,
    review_themaword_title character varying,
    review_themaword_description character varying,
    best_item_display_type character varying(1) DEFAULT 'A'::character varying NOT NULL,
    ordering integer
);


--
-- Name: op_category_team_category_team_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_category_team_category_team_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_category_team_category_team_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_category_team_category_team_id_seq OWNED BY public.op_category_team.category_team_id;


--
-- Name: op_category_team_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_category_team_item (
    category_team_item_id integer NOT NULL,
    category_team_id integer NOT NULL,
    item_id integer NOT NULL,
    created_date character varying(14)
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
-- Name: op_display_editor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_editor (
    display_group_code character varying(40) NOT NULL,
    display_sub_code character varying(40),
    view_target character varying(40) DEFAULT 'ALL'::character varying NOT NULL,
    display_editor_content text NOT NULL,
    ordering integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_display_group_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_group_code (
    display_group_code character varying(40) NOT NULL,
    display_group_code_name character varying(50),
    display_template_code character varying(50)
);


--
-- Name: op_display_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_image (
    display_group_code character varying(40) NOT NULL,
    display_sub_code character varying(40),
    view_target character varying(40) DEFAULT 'ALL'::character varying NOT NULL,
    display_image character varying(100) NOT NULL,
    display_url character varying(255),
    display_content character varying(1000),
    ordering integer NOT NULL,
    created_date character varying(14) NOT NULL,
    display_color character varying(20)
);


--
-- Name: op_display_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_item (
    display_group_code character varying(40) NOT NULL,
    display_sub_code character varying(40),
    view_target character varying(40) DEFAULT 'ALL'::character varying NOT NULL,
    item_id integer NOT NULL,
    ordering integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_display_sns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_sns (
    sns_id integer NOT NULL,
    sns_token character varying(255),
    sns_type character varying(50),
    ordering integer,
    updated_date timestamp without time zone,
    created_date timestamp without time zone
);


--
-- Name: op_display_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_display_template (
    display_template_code character varying(40) NOT NULL,
    display_setting_value text NOT NULL
);


--
-- Name: op_featured; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_featured (
    featured_id integer NOT NULL,
    featured_class integer DEFAULT 1 NOT NULL,
    featured_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    featured_url character varying(50) NOT NULL,
    featured_code character varying(50) NOT NULL,
    featured_name character varying(255) NOT NULL,
    featured_simple_content character varying(255) NOT NULL,
    featured_content character varying NOT NULL,
    featured_image character varying(255),
    featured_image_mobile character varying(255),
    thumbnail_image character varying(255),
    thumbnail_image_mobile character varying(255),
    featured_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    link character varying(100) NOT NULL,
    link_target_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    link_rel_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    display_list_flag character varying(1) DEFAULT 'Y'::character varying,
    title character varying,
    keywords character varying,
    description character varying,
    header_contents1 character varying,
    themaword_title character varying,
    themaword_description character varying,
    ordering integer DEFAULT 0 NOT NULL,
    ordering_esthetic integer DEFAULT 0 NOT NULL,
    ordering_nail integer DEFAULT 0 NOT NULL,
    ordering_matsuge_extension integer DEFAULT 0 NOT NULL,
    ordering_hair integer DEFAULT 0 NOT NULL,
    ordering_sale_outlets integer DEFAULT 0 NOT NULL,
    created_date character varying(14),
    list_type character varying(20),
    prod_state character(1),
    access_auth character varying(4),
    start_date character varying(12),
    start_time character varying(2),
    end_date character varying(12),
    end_time character varying(2),
    reply_used_flag character varying(1) DEFAULT 'N'::character varying,
    event_code character varying(10),
    featured_host character varying(255),
    featured_phone_no1 character varying(255),
    featured_phone_no2 character varying(255),
    featured_phone_no3 character varying(255),
    featured_list_image character varying(255),
    thumbnail_list_image character varying(255),
    locgov_code character varying(10)
);


--
-- Name: op_featured_banner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_featured_banner (
    featured_banner_id integer NOT NULL,
    featured_banner_color character varying(20),
    banner_left_top_title character varying(50),
    banner_left_top_link character varying(255),
    banner_left_top_image character varying(255),
    banner_left_bottom1_title character varying(50),
    banner_left_bottom1_link character varying(255),
    banner_left_bottom1_image character varying(255),
    banner_left_bottom2_title character varying(50),
    banner_left_bottom2_link character varying(255),
    banner_left_bottom2_image character varying(255),
    banner_center_title character varying(50),
    banner_center_link character varying(255),
    banner_center_image character varying(255),
    banner_right_top_title character varying(50),
    banner_right_top_link character varying(255),
    banner_right_top_image character varying(255),
    banner_right_bottom1_title character varying(50),
    banner_right_bottom1_link character varying(255),
    banner_right_bottom1_image character varying(255),
    banner_right_bottom2_title character varying(50),
    banner_right_bottom2_link character varying(255),
    banner_right_bottom2_image character varying(255),
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_featured_banner_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_featured_banner_item (
    featured_banner_position character varying(50),
    item_id integer,
    display_order integer,
    created_date character varying(14)
);


--
-- Name: op_featured_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_featured_item (
    reg_seq bigint NOT NULL,
    featured_id integer NOT NULL,
    item_id integer NOT NULL,
    display_order integer NOT NULL,
    created_date character varying(14) NOT NULL,
    user_def_group character varying(255),
    user_def_group_order integer
);


--
-- Name: op_featured_item_reg_seq_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_featured_item_reg_seq_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_featured_item_reg_seq_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_featured_item_reg_seq_seq OWNED BY public.op_featured_item.reg_seq;


--
-- Name: op_featured_reply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_featured_reply (
    id bigint NOT NULL,
    featured_id integer NOT NULL,
    user_id bigint NOT NULL,
    user_name character varying(20) NOT NULL,
    reply_content text,
    data_status character varying(1) DEFAULT '0'::character varying,
    created character varying(14) NOT NULL,
    updated character varying(14),
    created_by bigint NOT NULL,
    updated_by bigint
);


--
-- Name: op_filter_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_filter_code (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    label character varying(1000) NOT NULL,
    label_code character varying(1000),
    label_image character varying(1000),
    ordering integer,
    filter_group_id bigint NOT NULL
);


--
-- Name: op_filter_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_filter_group (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    description character varying(1000) NOT NULL,
    filter_type character varying(255),
    label character varying(1000) NOT NULL
);


--
-- Name: op_gift_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_gift_group (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    data_status character varying(10) NOT NULL,
    group_type character varying(15) NOT NULL,
    name character varying(30) NOT NULL,
    over_order_price integer,
    valid_end_date timestamp without time zone,
    valid_start_date timestamp without time zone
);


--
-- Name: op_gift_group_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_gift_group_item (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    gift_item_id bigint,
    gift_group_id bigint
);


--
-- Name: op_gift_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_gift_item (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    code character varying(30) NOT NULL,
    data_status character varying(10) NOT NULL,
    image character varying(255),
    name character varying(30) NOT NULL,
    price integer NOT NULL,
    seller_id bigint NOT NULL,
    valid_end_date timestamp without time zone,
    valid_start_date timestamp without time zone
);


--
-- Name: op_gift_item_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_gift_item_log (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    data_status character varying(10),
    gift_item_id bigint,
    image character varying(255),
    name character varying(30),
    price integer,
    valid_end_date timestamp without time zone,
    valid_start_date timestamp without time zone
);


--
-- Name: op_gift_item_relation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_gift_item_relation (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    item_id integer NOT NULL,
    gift_item_id bigint
);


--
-- Name: op_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item (
    item_id integer NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    item_code character varying(30),
    item_user_code character varying(30),
    item_name character varying(200) NOT NULL,
    item_summary character varying(200),
    item_data_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    item_type character varying(1),
    item_new_flag character varying(1) DEFAULT 'N'::character varying,
    item_label character varying(1) DEFAULT '1'::character varying NOT NULL,
    item_type1 character varying(1),
    item_type2 character varying(1),
    item_type3 character varying(1),
    item_type4 character varying(1),
    item_type5 character varying(1),
    item_type6 character varying(1),
    item_type7 character varying(1),
    item_type8 character varying(1),
    item_type9 character varying(1),
    item_type10 character varying(1),
    private_type character varying(20) DEFAULT '00'::character varying,
    display_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    minor_allow_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    origin_country character varying(50),
    manufacturer character varying(50),
    brand_id integer,
    brand character varying(50),
    color character varying(20),
    weight character varying(20),
    nonmember_order_type character varying(1),
    tax_type character varying(1) DEFAULT '2'::character varying,
    commission_type character varying(1) DEFAULT '1'::character varying,
    commission_rate double precision DEFAULT 0.000000,
    price_criteria character varying(1) DEFAULT '0'::character varying,
    item_price character varying(20),
    cost_price integer DEFAULT 0,
    supply_price integer DEFAULT 0,
    sale_price integer DEFAULT 0 NOT NULL,
    sale_price_nonmember_flag character varying(1) DEFAULT '0'::character varying NOT NULL,
    sale_price_nonmember integer,
    spot_flag character varying(1) DEFAULT 'N'::character varying,
    spot_date_type character varying(1),
    spot_type character varying(1),
    spot_apply_group character varying(20),
    spot_discount_amount integer DEFAULT 0,
    spot_start_date character varying(8),
    spot_end_date character varying(8),
    spot_start_time character varying(6),
    spot_end_time character varying(6),
    spot_week_day character varying(7),
    sale_point integer DEFAULT 0,
    seller_discount_flag character varying(1),
    seller_discount_type character varying(1),
    seller_discount_amount integer DEFAULT 0,
    seller_point_flag character varying(1) DEFAULT 'N'::character varying,
    sold_out character varying(1) NOT NULL,
    stock_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    stock_quantity integer DEFAULT '-1'::integer,
    stock_code character varying(50),
    stock_schedule_auto_flag character varying(1),
    stock_schedule_type character varying(10),
    stock_schedule_date character varying(20),
    stock_schedule_text character varying(40),
    order_min_quantity integer DEFAULT '-1'::integer,
    order_max_quantity integer DEFAULT '-1'::integer,
    sale_quantity integer DEFAULT 0 NOT NULL,
    item_option_flag character varying(1),
    item_option_type character varying(2),
    item_option_title1 character varying(30),
    item_option_title2 character varying(30),
    item_option_title3 character varying(30),
    item_addition_flag character varying(1),
    free_gift_flag character varying(1),
    free_gift_name character varying(255),
    item_keyword character varying(300),
    detail_content character varying,
    detail_content_mobile character varying,
    item_notice_code character varying(50),
    item_image character varying(255),
    team character varying(30),
    opentime character varying(14),
    base_item text,
    other_flag character varying(1),
    recommend_flag character varying(1),
    relation_item_display_type character varying(1),
    delivery_company_id integer,
    delivery_company_name character varying(30),
    delivery_type character varying(1),
    shipment_id integer,
    shipment_group_code character varying(50),
    shipment_return_type character varying(1),
    shipment_return_id integer,
    shipping_type character varying(1),
    shipping_group_code character varying(20),
    shipping integer,
    shipping_free_amount integer,
    shipping_item_count integer DEFAULT 1,
    shipping_extra_charge1 integer,
    shipping_extra_charge2 integer,
    shipping_return integer,
    item_return_flag character varying(1) DEFAULT 'Y'::character varying,
    hits integer DEFAULT 0 NOT NULL,
    seo_title character varying(255),
    seo_index_flag character varying(1),
    seo_keywords character varying(255),
    seo_description character varying(255),
    seo_header_contents1 character varying(255),
    seo_themaword_title character varying(255),
    seo_themaword_description text,
    md_id character varying(11),
    md_name character varying(50),
    coupon_use_flag character varying(1) DEFAULT 'Y'::character varying,
    data_status_message text,
    data_status_code character varying(20) DEFAULT '1'::character varying NOT NULL,
    updated_user_id bigint DEFAULT 0 NOT NULL,
    updated_date character varying(14),
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14),
    front_display_flag character varying(1) DEFAULT 'Y'::character varying,
    erp_exception_type character varying(1),
    item_number character varying(255),
    item_sale_status_text character varying(255),
    noindex_yn character varying(255),
    naver_shopping_item_name character varying(100),
    naver_shopping_flag character varying(1) DEFAULT 'N'::character varying,
    naver_pay_flag character varying(1) DEFAULT 'Y'::character varying,
    set_discount_type character varying(1),
    set_discount_amount integer,
    representative_item_yn character varying(1) DEFAULT 'N'::character varying,
    adult_item_yn character varying(1) DEFAULT 'N'::character varying,
    mobile_item_yn character varying(1) DEFAULT 'N'::character varying,
    item_seller_code character varying(30),
    item_close_dt character varying(14),
    item_text_option_title1 character varying(51),
    item_text_option_title2 character varying(51),
    item_text_option_title3 character varying(51),
    item_text_option_flag character varying(1) DEFAULT 'N'::character varying,
    category_code character varying(50),
    locgov_code character varying(50),
    display_type character varying(20),
    display_start_date character varying(8),
    display_end_date character varying(8),
    min_donation_amount integer
);


--
-- Name: op_item_addition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_addition (
    item_id integer NOT NULL,
    addition_item_id integer DEFAULT 0 NOT NULL
);


--
-- Name: op_item_bakup_0611_option; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_bakup_0611_option (
    item_id integer DEFAULT 0 NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    item_code character varying(30) NOT NULL,
    item_user_code character varying(30) NOT NULL,
    item_name character varying(200) NOT NULL,
    item_summary character varying(200),
    item_data_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    item_type character varying(1) NOT NULL,
    item_new_flag character varying(1) DEFAULT 'N'::character varying,
    item_label character varying(1) DEFAULT '1'::character varying NOT NULL,
    item_type1 character varying(1),
    item_type2 character varying(1),
    item_type3 character varying(1),
    item_type4 character varying(1),
    item_type5 character varying(1),
    item_type6 character varying(1),
    item_type7 character varying(1),
    item_type8 character varying(1),
    item_type9 character varying(1),
    item_type10 character varying(1),
    private_type character varying(20) DEFAULT '00'::character varying,
    display_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    minor_allow_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    origin_country character varying(50),
    manufacturer character varying(50),
    brand_id integer,
    brand character varying(50),
    color character varying(20),
    weight character varying(20),
    nonmember_order_type character varying(1),
    tax_type character varying(1) DEFAULT '2'::character varying,
    commission_type character varying(1) DEFAULT '1'::character varying,
    commission_rate double precision DEFAULT 0.000000,
    price_criteria character varying(1) DEFAULT '0'::character varying,
    item_price character varying(20),
    cost_price integer DEFAULT 0,
    supply_price integer DEFAULT 0,
    sale_price integer DEFAULT 0 NOT NULL,
    sale_price_nonmember_flag character varying(1) DEFAULT '0'::character varying NOT NULL,
    sale_price_nonmember integer,
    spot_flag character varying(1) DEFAULT 'N'::character varying,
    spot_date_type character varying(1),
    spot_type character varying(1),
    spot_apply_group character varying(20),
    spot_discount_amount integer DEFAULT 0,
    spot_start_date character varying(8),
    spot_end_date character varying(8),
    spot_start_time character varying(6),
    spot_end_time character varying(6),
    spot_week_day character varying(7),
    sale_point integer DEFAULT 0,
    seller_discount_flag character varying(1),
    seller_discount_type character varying(1),
    seller_discount_amount integer DEFAULT 0,
    seller_point_flag character varying(1) DEFAULT 'N'::character varying,
    sold_out character varying(1) NOT NULL,
    stock_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    stock_quantity integer DEFAULT '-1'::integer,
    stock_code character varying(50),
    stock_schedule_auto_flag character varying(1),
    stock_schedule_type character varying(10),
    stock_schedule_date character varying(20),
    stock_schedule_text character varying(40),
    order_min_quantity integer DEFAULT '-1'::integer,
    order_max_quantity integer DEFAULT '-1'::integer,
    sale_quantity integer DEFAULT 0 NOT NULL,
    item_option_flag character varying(1),
    item_option_type character varying(2),
    item_option_title1 character varying(30),
    item_option_title2 character varying(30),
    item_option_title3 character varying(30),
    item_addition_flag character varying(1),
    free_gift_flag character varying(1),
    free_gift_name character varying(255),
    item_keyword character varying(300),
    detail_content character varying,
    detail_content_mobile character varying,
    item_notice_code character varying(50),
    item_image character varying(255),
    team character varying(30),
    opentime character varying(14),
    base_item text,
    other_flag character varying(1),
    recommend_flag character varying(1),
    relation_item_display_type character varying(1),
    delivery_company_id integer,
    delivery_company_name character varying(30),
    delivery_type character varying(1),
    shipment_id integer NOT NULL,
    shipment_group_code character varying(50),
    shipment_return_type character varying(1),
    shipment_return_id integer NOT NULL,
    shipping_type character varying(1) NOT NULL,
    shipping_group_code character varying(20) NOT NULL,
    shipping integer,
    shipping_free_amount integer,
    shipping_item_count integer DEFAULT 1,
    shipping_extra_charge1 integer,
    shipping_extra_charge2 integer,
    shipping_return integer,
    item_return_flag character varying(1) DEFAULT 'Y'::character varying,
    hits integer DEFAULT 0 NOT NULL,
    seo_title character varying(255),
    seo_index_flag character varying(1),
    seo_keywords character varying(255),
    seo_description character varying(255),
    seo_header_contents1 character varying(255),
    seo_themaword_title character varying(255),
    seo_themaword_description text,
    md_id character varying(11),
    md_name character varying(50),
    coupon_use_flag character varying(1) DEFAULT 'Y'::character varying,
    data_status_message text,
    data_status_code character varying(2) DEFAULT '1'::character varying NOT NULL,
    updated_user_id bigint DEFAULT 0 NOT NULL,
    updated_date character varying(14),
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14),
    front_display_flag character varying(1) DEFAULT 'Y'::character varying,
    erp_exception_type character varying(1),
    item_number character varying(255),
    item_sale_status_text character varying(255),
    noindex_yn character varying(255),
    naver_shopping_item_name character varying(100),
    naver_shopping_flag character varying(1) DEFAULT 'N'::character varying,
    naver_pay_flag character varying(1) DEFAULT 'Y'::character varying,
    set_discount_type character varying(1),
    set_discount_amount integer,
    representative_item_yn character varying(1) DEFAULT 'N'::character varying,
    adult_item_yn character varying(1) DEFAULT 'N'::character varying,
    mobile_item_yn character varying(1) DEFAULT 'N'::character varying
);


--
-- Name: op_item_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_category (
    item_category_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    category_id integer DEFAULT 0 NOT NULL,
    ordering integer DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_item_filter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_filter (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    filter_code_id bigint NOT NULL,
    filter_group_id bigint NOT NULL,
    item_id integer NOT NULL,
    ordering integer
);


--
-- Name: op_item_hit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_hit (
    item_id integer NOT NULL,
    hits integer
);


--
-- Name: op_item_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_image (
    item_image_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    image_name character varying(255) NOT NULL,
    ordering integer DEFAULT 0 NOT NULL,
    created_date character varying(14),
    thumbnail_small character varying(255),
    thumbnail_medium character varying(255),
    thumbnail_large character varying(255)
);


--
-- Name: op_item_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_image_id_seq OWNED BY public.op_item_image.item_image_id;


--
-- Name: op_item_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_info (
    item_info_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    info_code character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(1000),
    created_date character varying(14)
);


--
-- Name: op_item_info_mobile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_info_mobile (
    item_info_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    info_code character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(1000),
    created_date character varying(14)
);


--
-- Name: op_item_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_item_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_item_id_seq OWNED BY public.op_item.item_id;


--
-- Name: op_item_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_log (
    item_log_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    item_user_code character varying(30) DEFAULT '0'::character varying NOT NULL,
    item_name character varying(200) NOT NULL,
    display_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    sold_out character varying(1) NOT NULL,
    cost_price integer,
    item_price character varying(20),
    sale_price integer NOT NULL,
    commission_type character varying(1),
    commission_rate double precision,
    price_criteria character varying(1),
    process_page character varying(10),
    created_manager_id integer DEFAULT 0,
    created_seller_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14),
    action_type character varying(30)
);


--
-- Name: op_item_log_item_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_log_item_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_log_item_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_log_item_log_id_seq OWNED BY public.op_item_log.item_log_id;


--
-- Name: op_item_mall_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_mall_code (
    item_id integer,
    mall_config_id integer,
    product_code character varying(50)
);


--
-- Name: op_item_notice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_notice (
    item_notice_code character varying(3) NOT NULL,
    item_notice_title character varying(50) NOT NULL,
    notice_title character varying(50) DEFAULT ''::character varying NOT NULL,
    notice_description character varying(255),
    ordering integer
);


--
-- Name: op_item_option; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_option (
    item_option_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    option_type character varying(2) NOT NULL,
    option_display_type character varying(10),
    option_hide_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    option_name1 character varying(255) NOT NULL,
    option_name2 character varying(255),
    option_name3 character varying(255),
    option_price integer,
    option_cost_price integer,
    option_price_nonmember integer,
    option_stock_flag character varying(1) DEFAULT 'N'::character varying,
    option_stock_quantity integer DEFAULT 0,
    option_stock_code character varying(30),
    option_stock_schedule_date character varying(50) DEFAULT '0'::character varying,
    option_stock_schedule_text character varying(50) DEFAULT '0'::character varying,
    option_sold_out_flag character varying(40) DEFAULT 'N'::character varying,
    option_display_flag character varying(1) DEFAULT 'Y'::character varying,
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_item_option_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_option_image (
    item_option_image_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    item_option_id integer DEFAULT 0 NOT NULL,
    option_name character varying(255) NOT NULL,
    option_image character varying(255)
);


--
-- Name: op_item_option_soldout; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_option_soldout (
    item_id integer NOT NULL,
    item_option_sold_out_flag character varying(1)
);


--
-- Name: op_item_ordering; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_ordering (
    item_ordering_id integer NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    category_id integer DEFAULT 0 NOT NULL,
    ordering integer DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_item_ordering_item_ordering_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_ordering_item_ordering_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_ordering_item_ordering_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_ordering_item_ordering_id_seq OWNED BY public.op_item_ordering.item_ordering_id;


--
-- Name: op_item_other; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_other (
    item_other_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    other_item_id integer DEFAULT 0 NOT NULL,
    counting integer DEFAULT 0 NOT NULL
);


--
-- Name: op_item_relation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_relation (
    item_relation_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    related_item_id integer DEFAULT 0 NOT NULL,
    ordering integer DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_item_review; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_review (
    item_review_id bigint NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    order_code character varying(50) DEFAULT '0'::character varying NOT NULL,
    subject character varying(255) NOT NULL,
    content character varying NOT NULL,
    score integer DEFAULT 0 NOT NULL,
    recommend_flag character varying(1),
    user_id bigint DEFAULT 0 NOT NULL,
    user_name character varying(350),
    seller_id bigint DEFAULT 0 NOT NULL,
    display_flag character varying(1),
    created_date character varying(14),
    point_payment character varying(1) DEFAULT 'N'::character varying,
    point integer DEFAULT 0,
    point_payment_date character varying(14),
    data_status_code character varying(1) DEFAULT '0'::character varying NOT NULL,
    display_options_flag character varying(1) DEFAULT 'N'::character varying,
    options text,
    admin_comment character varying(2000),
    like_count integer DEFAULT 0,
    answer_login_id character varying(300),
    answer_date character varying(14)
);


--
-- Name: op_item_review_filter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_review_filter (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    filter_code_id bigint NOT NULL,
    filter_group_id bigint NOT NULL,
    item_id integer NOT NULL,
    item_review_id integer NOT NULL,
    ordering integer
);


--
-- Name: op_item_review_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_review_image (
    item_review_image_id bigint NOT NULL,
    item_review_id bigint,
    review_image character varying(1000),
    ordering integer,
    created_date character varying(50)
);


--
-- Name: op_item_review_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_review_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_review_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_review_image_id_seq OWNED BY public.op_item_review_image.item_review_image_id;


--
-- Name: op_item_review_image_item_review_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_review_image_item_review_image_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_review_image_item_review_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_review_image_item_review_image_id_seq OWNED BY public.op_item_review_image.item_review_image_id;


--
-- Name: op_item_review_like; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_review_like (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    ip character varying(20),
    item_review_id integer NOT NULL,
    user_id bigint
);


--
-- Name: op_item_review_review_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_review_review_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_review_review_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_review_review_id_seq OWNED BY public.op_item_review.item_review_id;


--
-- Name: op_item_sale_edit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_sale_edit (
    item_sale_edit_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    seller_name character varying(50) NOT NULL,
    item_code character varying(30) NOT NULL,
    item_name character varying(200) NOT NULL,
    sale_price integer DEFAULT 0 NOT NULL,
    item_price character varying(20),
    cost_price integer DEFAULT 0 NOT NULL,
    status character varying(1) DEFAULT '0'::character varying NOT NULL,
    message text,
    created_date character varying(14),
    updated_date character varying(14)
);


--
-- Name: op_item_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_item_set (
    item_set_id bigint NOT NULL,
    parent_item_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    quantity integer DEFAULT 1,
    ordering integer DEFAULT 0,
    created_date character varying(14)
);


--
-- Name: op_item_set_item_set_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_item_set_item_set_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_item_set_item_set_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_item_set_item_set_id_seq OWNED BY public.op_item_set.item_set_id;


--
-- Name: op_keyword; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_keyword (
    keyword character varying(255) NOT NULL,
    keyword_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    created_date character varying(14) DEFAULT ''::character varying NOT NULL,
    keyword_seperation character varying(255) DEFAULT ''::character varying NOT NULL,
    weight integer DEFAULT 0
);


--
-- Name: op_main_display_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_main_display_item (
    template_id character varying(40) NOT NULL,
    item_id integer NOT NULL,
    display_order integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_mall_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mall_config (
    mall_config_id integer NOT NULL,
    mall_login_id character varying(50) NOT NULL,
    mall_type character varying(50) NOT NULL,
    mall_api_key character varying(255) NOT NULL,
    data_status_code character varying(1) NOT NULL,
    status_code character varying(1),
    last_date character varying(14),
    last_search_start_date character varying(14),
    last_search_end_date character varying(14),
    claim_status_code character varying(1),
    last_claim_date character varying(14),
    last_claim_search_start_date character varying(14),
    last_claim_search_end_date character varying(14),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_mall_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mall_order (
    mall_order_id integer NOT NULL,
    item_id integer,
    matched_options character varying(255),
    mall_config_id integer NOT NULL,
    mall_type character varying(50) NOT NULL,
    order_item_status character varying(10) NOT NULL,
    order_item_status_label character varying(50),
    order_code character varying(100) NOT NULL,
    order_index integer NOT NULL,
    shipping_group_code character varying(50) NOT NULL,
    shipping_group_flag character varying(1) NOT NULL,
    addition_item_flag character varying(1) NOT NULL,
    parent_product_code integer NOT NULL,
    shipping_code character varying(50) NOT NULL,
    product_code character varying(50) NOT NULL,
    option_code character varying(100) NOT NULL,
    product_name character varying(255) NOT NULL,
    option_name character varying(255) NOT NULL,
    sell_price integer NOT NULL,
    quantity integer NOT NULL,
    cancel_quantity integer DEFAULT 0,
    seller_discount_amount integer NOT NULL,
    sale_amount integer NOT NULL,
    option_amount integer NOT NULL,
    pay_shipping integer NOT NULL,
    pay_shipping_type character varying(20) NOT NULL,
    island_pay_shipping integer NOT NULL,
    island_pay_shipping_type character varying(5) NOT NULL,
    mall_product_code character varying(100) NOT NULL,
    member_id character varying(40),
    member_type character varying(40),
    member_no integer DEFAULT 0,
    buyer_name character varying(50) NOT NULL,
    buyer_telephone_number character varying(20) NOT NULL,
    buyer_phone_number character varying(20) NOT NULL,
    buyer_zipcode character varying(7),
    buyer_address character varying(100),
    buyer_address_detail character varying(255),
    receiver_name character varying(50) NOT NULL,
    receiver_telephone_number character varying(20) NOT NULL,
    receiver_phone_number character varying(20) NOT NULL,
    receiver_zipcode character varying(7) NOT NULL,
    receiver_address character varying(100) NOT NULL,
    receiver_address_detail character varying(255) NOT NULL,
    content character varying(255) NOT NULL,
    system_message character varying(255),
    pay_date character varying(14) NOT NULL,
    created_date character varying(14) NOT NULL,
    claim_apply_date character varying(14)
);


--
-- Name: op_mall_order_cancel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mall_order_cancel (
    mall_order_id integer NOT NULL,
    claim_code character varying(100) NOT NULL,
    claim_quantity integer NOT NULL,
    claim_status character varying(50) NOT NULL,
    claim_apply_subject character varying(10) NOT NULL,
    cancel_reason character varying(255),
    cancel_reason_text character varying(255),
    cancel_refusal_reson character varying(10),
    cancel_refusal_reson_text character varying(255),
    cancel_apply_date character varying(14) NOT NULL
);


--
-- Name: op_mall_order_exchange; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mall_order_exchange (
    mall_order_id integer NOT NULL,
    claim_code character varying(50) NOT NULL,
    claim_quantity integer NOT NULL,
    claim_status character varying(3) NOT NULL,
    ex_reason character varying(10) NOT NULL,
    ex_reason_text character varying(255) NOT NULL,
    ex_collection_name character varying(40),
    ex_collection_tel_number character varying(40),
    ex_collection_phone_number character varying(40),
    ex_collection_zipcode character varying(10),
    ex_collection_zipcode_seq character varying(10),
    ex_collection_address character varying(255),
    ex_collection_address_detail character varying(255),
    ex_collection_address_type character varying(5),
    ex_collection_address_bilno character varying(50),
    ex_shipping_type character varying(5),
    ex_receiver_name character varying(40) NOT NULL,
    ex_receiver_tel_number character varying(40) NOT NULL,
    ex_receiver_phone_number character varying(40) NOT NULL,
    ex_receiver_zipcode character varying(10) NOT NULL,
    ex_receiver_zipcode_seq character varying(10),
    ex_receiver_address character varying(255) NOT NULL,
    ex_receiver_address_detail character varying(255) NOT NULL,
    ex_receiver_address_type character varying(5) NOT NULL,
    ex_receiver_address_bilno character varying(50),
    ex_shipping_amount integer,
    ex_add_shipping_amount integer,
    ex_shipping_payment_type character varying(5) NOT NULL,
    ex_shipping_number character varying(50),
    ex_shipping_company_code character varying(10),
    ex_refusal_reason character varying(10),
    ex_refusal_reason_text character varying(255),
    resend_delivery_company_code character varying(10),
    resend_delivery_number character varying(50),
    ex_apply_date character varying(14) NOT NULL,
    ex_end_date character varying(14)
);


--
-- Name: op_mall_order_return; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mall_order_return (
    mall_order_id integer NOT NULL,
    claim_code character varying(100) NOT NULL,
    claim_status character varying(20) NOT NULL,
    claim_quantity integer NOT NULL,
    rt_reason character varying(50) NOT NULL,
    rt_reason_text character varying(255) NOT NULL,
    rt_collection_name character varying(40),
    rt_collection_tel_number character varying(40),
    rt_collection_phone_number character varying(40),
    rt_collection_zipcode character varying(10),
    rt_collection_zipcode_seq character varying(10),
    rt_collection_address character varying(255),
    rt_collection_address_detail character varying(255),
    rt_collection_address_type character varying(5),
    rt_collection_address_bilno character varying(50),
    rt_shipping_type character varying(50),
    rt_shipping_amount integer,
    rt_default_shipping_amount integer,
    rt_add_shipping_amount integer,
    rt_add_shipping_type character varying(50),
    rt_deduction_shipping_amount integer,
    rt_shipping_payment_type character varying(50),
    rt_shipping_number character varying(50),
    rt_shipping_company_code character varying(10),
    rt_hold_reason character varying(10),
    rt_hold_reason_text character varying(255),
    rt_refusal_reason character varying(10),
    rt_refusal_reason_text character varying(255),
    rt_apply_date character varying(14) NOT NULL,
    rt_end_date character varying(14)
);


--
-- Name: op_mobile_category_edit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_mobile_category_edit (
    category_edit_id integer DEFAULT 0,
    code character varying(50),
    edit_kind character varying(1),
    edit_position character varying(50),
    edit_content character varying,
    edit_image character varying(100),
    edit_url character varying(100),
    created_date character varying(14),
    updated_date character varying(14)
);


--
-- Name: op_ranking; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_ranking (
    ranking_id integer DEFAULT 0 NOT NULL,
    category_url character varying(50) DEFAULT '0'::character varying NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    ordering integer DEFAULT 0 NOT NULL
);


--
-- Name: op_ranking_batch; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_ranking_batch (
    ranking_type character varying(2) NOT NULL,
    ranking_code character varying(100) NOT NULL,
    item_id integer NOT NULL,
    ordering integer NOT NULL,
    data_status_code character varying(1) DEFAULT '0'::character varying NOT NULL
);


--
-- Name: op_ranking_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_ranking_config (
    rank_config_code character varying(50) NOT NULL,
    sale_price_days integer DEFAULT 0 NOT NULL,
    sale_price_weight integer DEFAULT 0 NOT NULL,
    sale_count_days integer DEFAULT 0 NOT NULL,
    sale_count_weight integer DEFAULT 0 NOT NULL,
    item_review_days integer DEFAULT 0 NOT NULL,
    item_review_weight integer DEFAULT 0 NOT NULL,
    item_hit_weight integer DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_restock_notice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_restock_notice (
    restock_notice_id bigint NOT NULL,
    item_id integer NOT NULL,
    user_id bigint NOT NULL,
    send_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_review_filter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_review_filter (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    category_id integer NOT NULL,
    filter_group_id bigint NOT NULL,
    ordering integer
);


--
-- Name: op_seller; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_seller (
    seller_id bigint NOT NULL,
    seller_name character varying(300),
    login_id character varying(300) NOT NULL,
    password character varying(300),
    user_name character varying(350),
    telephone_number character varying(210),
    phone_number character varying(210),
    fax_number character varying(210),
    email character varying(420),
    post character varying(7),
    address character varying(1000),
    address_detail character varying(1785),
    second_user_name character varying(350),
    second_telephone_number character varying(210),
    second_phone_number character varying(210),
    second_email character varying(420),
    company_name character varying(50),
    representative_name character varying(350),
    business_number character varying(12),
    business_location character varying(255),
    business_type character varying(25),
    business_items character varying(25),
    commission_rate double precision,
    remittance_type character varying(1),
    remittance_day character varying(2),
    bank_name character varying(30),
    bank_in_name character varying(350),
    bank_account_number character varying(350),
    shipping_flag character varying(50) DEFAULT 'N'::character varying,
    shipping integer,
    shipping_free_amount integer,
    shipping_extra_charge1 integer,
    shipping_extra_charge2 integer,
    header_content character varying,
    item_approval_type character varying(1) DEFAULT '1'::character varying,
    sms_send_time character varying(2),
    sms_md_flag character varying(1) DEFAULT 'N'::character varying,
    md_id integer,
    md_name character varying(1000),
    status_code character varying(1),
    created_date character varying(14),
    created_user_id bigint DEFAULT 0 NOT NULL,
    updated_date character varying(14),
    updated_user_id bigint DEFAULT 0 NOT NULL,
    mail_order_number character varying(30),
    buy_safety_use_confirm_number character varying(30),
    tax_type character varying(1) DEFAULT '1'::character varying,
    locgov_code character varying(10),
    file_name_certificate1 character varying(255),
    file_name_certificate2 character varying(255),
    file_name_certificate3 character varying(255),
    adult_item_yn character varying(1) DEFAULT 'N'::character varying,
    community_business_yn character varying(1) DEFAULT 'N'::character varying
);


--
-- Name: op_seller_seller_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_seller_seller_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_seller_seller_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_seller_seller_id_seq OWNED BY public.op_seller.seller_id;


--
-- Name: op_seller_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_seller_user (
    user_id bigint DEFAULT 0 NOT NULL,
    login_id character varying(300),
    password character varying(300),
    user_name character varying(350),
    email character varying(420),
    phone_number character varying(210),
    status_code bigint,
    login_count bigint,
    login_date character varying(14),
    deny_date character varying(14),
    leave_date character varying(14),
    login_fail_count integer DEFAULT 0 NOT NULL,
    login_try_date character varying(14),
    password_type character varying(1) DEFAULT 'N'::character varying NOT NULL,
    password_expired_date character varying(8),
    updated_date character varying(14),
    created_date character varying(14),
    locgov_code character varying(10)
);


--
-- Name: op_seller_user_login; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_seller_user_login (
    id bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    session_id character varying(1000) NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: op_store; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_store (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    address character varying(100) NOT NULL,
    address_detail character varying(250),
    end_time character varying(4),
    name character varying(50) NOT NULL,
    new_post character varying(50),
    post character varying(8) NOT NULL,
    sido character varying(20) NOT NULL,
    start_time character varying(4),
    store_type character varying(255),
    tel_number character varying(20)
);


--
-- Name: op_style_book; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_style_book (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    content character varying(1000),
    image character varying(1000),
    ordering integer NOT NULL,
    title character varying(200)
);


--
-- Name: op_style_book_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_style_book_item (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    item_id integer NOT NULL,
    ordering integer NOT NULL,
    style_book_id bigint
);


--
-- Name: op_wishlist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_wishlist (
    wishlist_id integer NOT NULL,
    wishlist_group_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    item_option character varying(255),
    item_option_group_name character varying(255),
    item_option_name character varying(255),
    user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL,
    temp_option character varying(255),
    item_option_1 character varying(255),
    item_option_2 character varying(255),
    item_option_3 character varying(255),
    item_option_4 character varying(255),
    item_option_5 character varying(255),
    item_option_6 character varying(255),
    item_option_7 character varying(255),
    item_option_8 character varying(255)
);


--
-- Name: op_wishlist_wishlist_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_wishlist_wishlist_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_wishlist_wishlist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_wishlist_wishlist_id_seq OWNED BY public.op_wishlist.wishlist_id;


--
-- Name: view_search_event; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.view_search_event AS
 SELECT featured_id,
    featured_name,
    featured_content,
    featured_host,
    concat('/featured/eventDetail.html?pages=', (featured_id)::character varying) AS detail_url,
    start_date,
    end_date,
    featured_phone_no1,
    featured_phone_no2,
    featured_phone_no3,
    concat('/upload/featured/', (featured_id)::character varying, featured_list_image) AS list_image_url,
    concat('/upload/featured/', (featured_id)::character varying, featured_image_mobile) AS mobile_image_url
   FROM public.op_featured f
  WHERE (((featured_flag)::text = 'Y'::text) AND ((to_char(now(), 'YYYYMMDD'::text) >= (start_date)::text) AND (to_char(now(), 'YYYYMMDD'::text) <= (end_date)::text)));


--
-- Name: g_item_content_img_desc item_content_img_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_item_content_img_desc ALTER COLUMN item_content_img_id SET DEFAULT nextval('public.g_item_content_img_desc_item_content_img_id_seq'::regclass);


--
-- Name: g_item_inquiry inquiry_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_item_inquiry ALTER COLUMN inquiry_id SET DEFAULT nextval('public.g_item_inquiry_inquiry_id_seq'::regclass);


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng pbadms_wlfr_cntr_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_pbadms_wlfr_cntr_mng ALTER COLUMN pbadms_wlfr_cntr_id SET DEFAULT nextval('public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq'::regclass);


--
-- Name: gift_subcategory subcategory_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_subcategory ALTER COLUMN subcategory_id SET DEFAULT nextval('public.gift_subcategory_subcategory_id_seq'::regclass);


--
-- Name: gift_subcategory_item subcategory_item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_subcategory_item ALTER COLUMN subcategory_item_id SET DEFAULT nextval('public.gift_subcategory_item_subcategory_item_id_seq'::regclass);


--
-- Name: op_brand brand_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_brand ALTER COLUMN brand_id SET DEFAULT nextval('public.op_brand_brand_id_seq'::regclass);


--
-- Name: op_category_group category_group_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_group ALTER COLUMN category_group_id SET DEFAULT nextval('public.op_category_group_category_group_id_seq'::regclass);


--
-- Name: op_category_team category_team_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_team ALTER COLUMN category_team_id SET DEFAULT nextval('public.op_category_team_category_team_id_seq'::regclass);


--
-- Name: op_featured_item reg_seq; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_item ALTER COLUMN reg_seq SET DEFAULT nextval('public.op_featured_item_reg_seq_seq'::regclass);


--
-- Name: op_item item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item ALTER COLUMN item_id SET DEFAULT nextval('public.op_item_item_id_seq'::regclass);


--
-- Name: op_item_image item_image_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_image ALTER COLUMN item_image_id SET DEFAULT nextval('public.op_item_image_id_seq'::regclass);


--
-- Name: op_item_log item_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_log ALTER COLUMN item_log_id SET DEFAULT nextval('public.op_item_log_item_log_id_seq'::regclass);


--
-- Name: op_item_ordering item_ordering_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_ordering ALTER COLUMN item_ordering_id SET DEFAULT nextval('public.op_item_ordering_item_ordering_id_seq'::regclass);


--
-- Name: op_item_review item_review_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review ALTER COLUMN item_review_id SET DEFAULT nextval('public.op_item_review_review_id_seq'::regclass);


--
-- Name: op_item_review_image item_review_image_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review_image ALTER COLUMN item_review_image_id SET DEFAULT nextval('public.op_item_review_image_id_seq'::regclass);


--
-- Name: op_item_set item_set_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_set ALTER COLUMN item_set_id SET DEFAULT nextval('public.op_item_set_item_set_id_seq'::regclass);


--
-- Name: op_seller seller_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_seller ALTER COLUMN seller_id SET DEFAULT nextval('public.op_seller_seller_id_seq'::regclass);


--
-- Name: op_wishlist wishlist_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_wishlist ALTER COLUMN wishlist_id SET DEFAULT nextval('public.op_wishlist_wishlist_id_seq'::regclass);


--
-- Data for Name: b_gift_sell; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.b_gift_sell (cntr_ymd, upper_locgov_code, upper_locgov_nm, locgov_code, locgov_nm, item_id, item_name, sell_cnt, sell_price, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_gds_img_expln_link_view; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_gds_img_expln_link_view (gds_user_cd, img_seq, img_expln, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_intrst_rtnpsnt; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_intrst_rtnpsnt (user_id, regist_sn, locgov_code, regist_de, category_code, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_item_content_img_desc; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_item_content_img_desc (item_content_img_id, item_id, img_desc, img_seq, created_user_id, created_date) FROM stdin;
\.


--
-- Data for Name: g_item_inquiry; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_item_inquiry (inquiry_id, item_id, user_id, question, secret_yn, answer, answered_date, status, created_date) FROM stdin;
\.


--
-- Data for Name: g_lclgv_off_rprs_gds_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_lclgv_off_rprs_gds_mng (lclgv_cd, gds_id, sort_seq, options, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_lclgv_pbadms_wlfr_cntr_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_lclgv_pbadms_wlfr_cntr_mng (pbadms_wlfr_cntr_id, lclgv_cd, pbadms_wlfr_cntr_nm, pbadms_wlfr_cntr_cd, use_yn, frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_lclgv_rprs_gds_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_lclgv_rprs_gds_mng (lclgv_cd, gds_id, sort_seq, frst_rgtr_id, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_myrecent; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_myrecent (recent_id, keyword, user_id, created_date) FROM stdin;
\.


--
-- Data for Name: g_season_food; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_season_food (season_food_month, reg_seq, season_food_keyword, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
8	1	한여름 제철 답례품	\N	2026-08-24 05:11:59.06952	\N	\N
\.


--
-- Data for Name: g_season_food_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_season_food_item (item_id, season_food_month, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
1000	8	\N	2026-08-24 05:11:59.072778	\N	\N
1009	8	\N	2026-08-24 05:11:59.072778	\N	\N
1011	8	\N	2026-08-24 05:11:59.072778	\N	\N
1016	8	\N	2026-08-24 05:11:59.072778	\N	\N
1020	8	\N	2026-08-24 05:11:59.072778	\N	\N
\.


--
-- Data for Name: g_spcl_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_spcl_item (spcl_item_mng_id, item_id, display_order, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_spcl_item_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_spcl_item_mng (spcl_item_mng_id, locgov_code, spcl_item_info, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_spcl_item_mng_keyword; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_spcl_item_mng_keyword (spcl_item_mng_id, reg_seq, spcl_item_keyword, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: gift_order_stock; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gift_order_stock (order_id, item_id, quantity, status, created_date) FROM stdin;
O202608251418343010	1001	1	RESERVED	2026-08-25 14:18:35.380357
\.


--
-- Data for Name: gift_subcategory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gift_subcategory (subcategory_id, category_code, name, ordering) FROM stdin;
1	TOUR	지역축제	1
2	TOUR	여행/숙박/캠핑	2
3	TOUR	문화/공연	3
4	TOUR	문화체험/관광체험/레포츠	4
5	TOUR	박물관/미술관/유적	5
6	TOUR	기타 관광서비스	6
7	AGRI	곡물류	1
8	AGRI	과일류	2
9	AGRI	채소/버섯류	3
10	AGRI	임산물	4
11	AGRI	은행/잣/견과류	5
12	AGRI	한방약초	6
13	AGRI	소고기	7
14	AGRI	돼지고기	8
15	AGRI	닭고기	9
16	AGRI	오리고기	10
17	AGRI	달걀	11
18	AGRI	기타 농축산물	12
19	SEAFOOD	생선	1
20	SEAFOOD	건어물	2
21	SEAFOOD	활어회	3
22	SEAFOOD	김/해초	4
23	SEAFOOD	해산물/어패류	5
24	SEAFOOD	홍게/대게	6
25	SEAFOOD	게장	7
26	SEAFOOD	젓갈/소금	8
27	SEAFOOD	기타 수산물	9
28	PROCESSED	양념류	1
29	PROCESSED	한과/떡/제과/간식	2
30	PROCESSED	김치	3
31	PROCESSED	장/오일/장아찌	4
32	PROCESSED	식초	5
33	PROCESSED	밀키트/간편식	6
34	PROCESSED	잼/조청/청류	7
35	PROCESSED	참기름/들기름	8
36	PROCESSED	조미료	9
37	PROCESSED	고추장	10
38	PROCESSED	반찬류	11
39	PROCESSED	차/음료/즙류	12
40	PROCESSED	건강식품	13
41	PROCESSED	차류	14
42	PROCESSED	주류/전통주	15
43	PROCESSED	기타 가공식품	16
44	LIVING	화장품/비누	1
45	LIVING	주방세제	2
46	LIVING	도자기/생활목기	3
47	LIVING	제기/병풍	4
48	LIVING	편백베개/방향제	5
49	LIVING	기타 생활용품	6
50	VOUCHER	지역사랑상품권	1
51	VOUCHER	기타 상품권	2
\.


--
-- Data for Name: gift_subcategory_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gift_subcategory_item (subcategory_item_id, subcategory_id, name, ordering) FROM stdin;
1	7	기타 잡곡	7
2	7	보리	6
3	7	콩/팥	5
4	7	찹쌀	4
5	7	흑미	3
6	7	현미	2
7	7	쌀(백미)	1
8	8	멜론	10
9	8	기타 과일	9
10	8	매실	8
11	8	포도	7
12	8	참외	6
13	8	감/곶감	5
14	8	블루베리	4
15	8	토마토	3
16	8	배	2
17	8	사과	1
18	9	기타 채소	9
19	9	당근	8
20	9	옥수수/호박	7
21	9	버섯	6
22	9	감자/돼지감자	5
23	9	마늘	4
24	9	배추/무	3
25	9	고추/고추가루	2
26	9	고구마	1
27	10	기타 임산물	4
28	10	건나물	3
29	10	산삼/인삼	2
30	10	더덕/산나물	1
31	12	기타 한방	6
32	12	와송	5
33	12	울금	4
34	12	하수오/백수오	3
35	12	도라지	2
36	12	인삼	1
37	40	환/캡슐	4
38	40	분말/가루	3
39	40	엑기스/즙	2
40	40	꿀	1
\.


--
-- Data for Name: mig_op_item_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.mig_op_item_image (item_image_id, item_id, image_name, ordering, created_date, serial_num) FROM stdin;
\.


--
-- Data for Name: op_brand; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_brand (brand_id, brand_name, brand_image, brand_content, display_flag, updated_user_id, updated_date, created_user_id, created_date, locgov_code) FROM stdin;
\.


--
-- Data for Name: op_brand_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_brand_category (brand_category_id, brand_id, category_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category (category_id, category_code, category_group_id, category_url, category_name, category_type, category_header, category_footer, category_banner, category_advertisement, category_mobile_html, category_mobile_html_header, category_class1, category_class2, category_class3, category_class4, category_level, ordering, category_flag, access_type, background_image, title, keywords, description, header_contents1, header_contents2, header_contents3, themaword_title, themaword_description, rank_title, rank_keywords, rank_description, rank_headercontents1, rank_themaword_title, rank_themaword_description, review_title, review_keywords, review_description, review_headercontents1, review_themaword_title, review_themaword_description) FROM stdin;
\.


--
-- Data for Name: op_category_edit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_edit (category_edit_id, code, edit_kind, edit_position, edit_content, edit_image, edit_url, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_category_filter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_filter (id, created, created_by, updated, updated_by, category_id, filter_group_id, ordering) FROM stdin;
\.


--
-- Data for Name: op_category_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_group (category_group_id, category_team_id, name, code, created_date, updated_date, category_group_flag, access_type, defcate, title, keywords, description, header_contents1, header_contents2, header_contents3, themaword_title, themaword_description, rank_title, rank_keywords, rank_description, rank_headercontents1, rank_themaword_title, rank_themaword_description, ordering, item_list) FROM stdin;
\.


--
-- Data for Name: op_category_group_banner; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_group_banner (category_group_banner_id, category_group_id, title, link_url, file_name, display_order, created_date) FROM stdin;
\.


--
-- Data for Name: op_category_team; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_team (category_team_id, name, code, created_date, updated_date, category_team_flag, title, keywords, description, header_contents1, header_contents2, header_contents3, themaword_title, themaword_description, rank_title, rank_keywords, rank_description, rank_headercontents1, rank_themaword_title, rank_themaword_description, review_title, review_keywords, review_description, review_headercontents1, review_themaword_title, review_themaword_description, best_item_display_type, ordering) FROM stdin;
\.


--
-- Data for Name: op_category_team_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_category_team_item (category_team_item_id, category_team_id, item_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_common_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code) FROM stdin;
GIFT_CATEGORY	ko	AGRI	농축산물	\N	1	Y	\N	\N	\N	\N
GIFT_CATEGORY	ko	SEAFOOD	수산물	\N	2	Y	\N	\N	\N	\N
GIFT_CATEGORY	ko	PROCESSED	가공식품	\N	3	Y	\N	\N	\N	\N
GIFT_CATEGORY	ko	LIVING	생활용품	\N	4	Y	\N	\N	\N	\N
GIFT_CATEGORY	ko	VOUCHER	지역상품권	\N	5	Y	\N	\N	\N	\N
GIFT_STATUS	ko	PENDING	승인대기	\N	1	Y	\N	\N	\N	\N
GIFT_STATUS	ko	APPROVED	승인	\N	2	Y	\N	\N	\N	\N
GIFT_STATUS	ko	REJECTED	반려	\N	3	Y	\N	\N	\N	\N
GIFT_STATUS	ko	STOPPED	판매중지	\N	4	Y	\N	\N	\N	\N
GIFT_INQUIRY_STATUS	ko	WAITING	답변대기	\N	1	Y	\N	\N	\N	\N
GIFT_INQUIRY_STATUS	ko	ANSWERED	답변완료	\N	2	Y	\N	\N	\N	\N
GIFT_DISPLAY_TYPE	ko	ALWAYS	상시노출	\N	1	Y	\N	\N	\N	\N
GIFT_DISPLAY_TYPE	ko	LIMITED	한시노출	\N	2	Y	\N	\N	\N	\N
GIFT_STATUS	ko	DISCONTINUED	폐지	\N	5	Y	\N	\N	\N	\N
GIFT_CATEGORY	ko	TOUR	관광서비스	\N	0	Y	\N	\N	\N	\N
\.


--
-- Data for Name: op_display_editor; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_editor (display_group_code, display_sub_code, view_target, display_editor_content, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_display_group_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_group_code (display_group_code, display_group_code_name, display_template_code) FROM stdin;
\.


--
-- Data for Name: op_display_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_image (display_group_code, display_sub_code, view_target, display_image, display_url, display_content, ordering, created_date, display_color) FROM stdin;
\.


--
-- Data for Name: op_display_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_item (display_group_code, display_sub_code, view_target, item_id, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_display_sns; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_sns (sns_id, sns_token, sns_type, ordering, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_display_template; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_display_template (display_template_code, display_setting_value) FROM stdin;
\.


--
-- Data for Name: op_featured; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_featured (featured_id, featured_class, featured_type, featured_url, featured_code, featured_name, featured_simple_content, featured_content, featured_image, featured_image_mobile, thumbnail_image, thumbnail_image_mobile, featured_flag, link, link_target_flag, link_rel_flag, display_list_flag, title, keywords, description, header_contents1, themaword_title, themaword_description, ordering, ordering_esthetic, ordering_nail, ordering_matsuge_extension, ordering_hair, ordering_sale_outlets, created_date, list_type, prod_state, access_auth, start_date, start_time, end_date, end_time, reply_used_flag, event_code, featured_host, featured_phone_no1, featured_phone_no2, featured_phone_no3, featured_list_image, thumbnail_list_image, locgov_code) FROM stdin;
\.


--
-- Data for Name: op_featured_banner; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_featured_banner (featured_banner_id, featured_banner_color, banner_left_top_title, banner_left_top_link, banner_left_top_image, banner_left_bottom1_title, banner_left_bottom1_link, banner_left_bottom1_image, banner_left_bottom2_title, banner_left_bottom2_link, banner_left_bottom2_image, banner_center_title, banner_center_link, banner_center_image, banner_right_top_title, banner_right_top_link, banner_right_top_image, banner_right_bottom1_title, banner_right_bottom1_link, banner_right_bottom1_image, banner_right_bottom2_title, banner_right_bottom2_link, banner_right_bottom2_image, created_user_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_featured_banner_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_featured_banner_item (featured_banner_position, item_id, display_order, created_date) FROM stdin;
\.


--
-- Data for Name: op_featured_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_featured_item (reg_seq, featured_id, item_id, display_order, created_date, user_def_group, user_def_group_order) FROM stdin;
\.


--
-- Data for Name: op_featured_reply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_featured_reply (id, featured_id, user_id, user_name, reply_content, data_status, created, updated, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: op_filter_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_filter_code (id, created, created_by, updated, updated_by, label, label_code, label_image, ordering, filter_group_id) FROM stdin;
\.


--
-- Data for Name: op_filter_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_filter_group (id, created, created_by, updated, updated_by, description, filter_type, label) FROM stdin;
\.


--
-- Data for Name: op_gift_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_gift_group (id, created, created_by, updated, updated_by, data_status, group_type, name, over_order_price, valid_end_date, valid_start_date) FROM stdin;
\.


--
-- Data for Name: op_gift_group_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_gift_group_item (id, created, created_by, updated, updated_by, gift_item_id, gift_group_id) FROM stdin;
\.


--
-- Data for Name: op_gift_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_gift_item (id, created, created_by, updated, updated_by, code, data_status, image, name, price, seller_id, valid_end_date, valid_start_date) FROM stdin;
\.


--
-- Data for Name: op_gift_item_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_gift_item_log (id, created, created_by, updated, updated_by, data_status, gift_item_id, image, name, price, valid_end_date, valid_start_date) FROM stdin;
\.


--
-- Data for Name: op_gift_item_relation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_gift_item_relation (id, created, created_by, updated, updated_by, item_id, gift_item_id) FROM stdin;
\.


--
-- Data for Name: op_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item (item_id, seller_id, item_code, item_user_code, item_name, item_summary, item_data_type, item_type, item_new_flag, item_label, item_type1, item_type2, item_type3, item_type4, item_type5, item_type6, item_type7, item_type8, item_type9, item_type10, private_type, display_flag, minor_allow_flag, origin_country, manufacturer, brand_id, brand, color, weight, nonmember_order_type, tax_type, commission_type, commission_rate, price_criteria, item_price, cost_price, supply_price, sale_price, sale_price_nonmember_flag, sale_price_nonmember, spot_flag, spot_date_type, spot_type, spot_apply_group, spot_discount_amount, spot_start_date, spot_end_date, spot_start_time, spot_end_time, spot_week_day, sale_point, seller_discount_flag, seller_discount_type, seller_discount_amount, seller_point_flag, sold_out, stock_flag, stock_quantity, stock_code, stock_schedule_auto_flag, stock_schedule_type, stock_schedule_date, stock_schedule_text, order_min_quantity, order_max_quantity, sale_quantity, item_option_flag, item_option_type, item_option_title1, item_option_title2, item_option_title3, item_addition_flag, free_gift_flag, free_gift_name, item_keyword, detail_content, detail_content_mobile, item_notice_code, item_image, team, opentime, base_item, other_flag, recommend_flag, relation_item_display_type, delivery_company_id, delivery_company_name, delivery_type, shipment_id, shipment_group_code, shipment_return_type, shipment_return_id, shipping_type, shipping_group_code, shipping, shipping_free_amount, shipping_item_count, shipping_extra_charge1, shipping_extra_charge2, shipping_return, item_return_flag, hits, seo_title, seo_index_flag, seo_keywords, seo_description, seo_header_contents1, seo_themaword_title, seo_themaword_description, md_id, md_name, coupon_use_flag, data_status_message, data_status_code, updated_user_id, updated_date, created_user_id, created_date, front_display_flag, erp_exception_type, item_number, item_sale_status_text, noindex_yn, naver_shopping_item_name, naver_shopping_flag, naver_pay_flag, set_discount_type, set_discount_amount, representative_item_yn, adult_item_yn, mobile_item_yn, item_seller_code, item_close_dt, item_text_option_title1, item_text_option_title2, item_text_option_title3, item_text_option_flag, category_code, locgov_code, display_type, display_start_date, display_end_date, min_donation_amount) FROM stdin;
1002	9003	\N	\N	순천 지역사랑상품권 3만원권	순천시 지역사랑상품권	1	\N	N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	00	Y	Y	\N	\N	\N	\N	\N	\N	\N	2	1	0	0	\N	0	0	30000	0	\N	N	\N	\N	\N	0	\N	\N	\N	\N	\N	0	\N	\N	0	N	1	N	0	\N	\N	\N	\N	\N	-1	-1	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	순천시 가맹점에서 사용 가능한 상품권입니다.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1	\N	\N	\N	Y	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	Y	\N	APPROVED	0	\N	0	20260824051158	Y	\N	\N	\N	\N	\N	N	Y	\N	\N	N	N	N	\N	\N	\N	\N	\N	N	VOUCHER	46150	\N	\N	\N	\N
1003	9004	\N	\N	제주 감귤 생활용품 세트	검수 전 신규 등록 상품	1	\N	N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	00	N	Y	\N	\N	\N	\N	\N	\N	\N	2	1	0	0	\N	0	0	25000	0	\N	N	\N	\N	\N	0	\N	\N	\N	\N	\N	0	\N	\N	0	N	0	N	100	\N	\N	\N	\N	\N	-1	-1	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	제주 감귤로 만든 천연 비누 세트입니다.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1	\N	\N	\N	Y	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	Y	\N	PENDING	0	\N	0	20260824051158	Y	\N	\N	\N	\N	\N	N	Y	\N	\N	N	N	N	\N	\N	\N	\N	\N	N	LIVING	50000	\N	\N	\N	\N
1000	9001	\N	\N	강남구 한우 선물세트	1++등급 한우 등심 선물세트 1kg	1	\N	N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	00	Y	Y	\N	\N	\N	\N	\N	\N	\N	2	1	0	0	\N	0	0	80000	0	\N	N	\N	\N	\N	0	\N	\N	\N	\N	\N	0	\N	\N	0	N	0	N	50	\N	\N	\N	\N	\N	-1	-1	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	엄선된 강남구 인증 한우입니다.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1	\N	\N	\N	Y	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	Y	\N	APPROVED	0	\N	0	20260824051158	Y	\N	\N	\N	\N	\N	N	Y	\N	\N	N	N	N	\N	\N	\N	\N	\N	N	AGRI	11230	\N	\N	\N	\N
1001	9002	\N	\N	해운대 건어물 세트	멸치, 오징어채 등 건어물 모음	1	\N	N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	00	Y	Y	\N	\N	\N	\N	\N	\N	\N	2	1	0	0	\N	0	0	35000	0	\N	N	\N	\N	\N	0	\N	\N	\N	\N	\N	0	\N	\N	0	N	0	N	30	\N	\N	\N	\N	\N	-1	-1	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	해운대 앞바다에서 잡은 건어물입니다.	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	1	\N	\N	\N	Y	0	\N	\N	\N	\N	\N	\N	\N	\N	\N	Y	\N	APPROVED	0	\N	0	20260824051158	Y	\N	\N	\N	\N	\N	N	Y	\N	\N	N	N	N	\N	\N	\N	\N	\N	N	SEAFOOD	26350	\N	\N	\N	\N
\.


--
-- Data for Name: op_item_addition; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_addition (item_id, addition_item_id) FROM stdin;
\.


--
-- Data for Name: op_item_bakup_0611_option; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_bakup_0611_option (item_id, seller_id, item_code, item_user_code, item_name, item_summary, item_data_type, item_type, item_new_flag, item_label, item_type1, item_type2, item_type3, item_type4, item_type5, item_type6, item_type7, item_type8, item_type9, item_type10, private_type, display_flag, minor_allow_flag, origin_country, manufacturer, brand_id, brand, color, weight, nonmember_order_type, tax_type, commission_type, commission_rate, price_criteria, item_price, cost_price, supply_price, sale_price, sale_price_nonmember_flag, sale_price_nonmember, spot_flag, spot_date_type, spot_type, spot_apply_group, spot_discount_amount, spot_start_date, spot_end_date, spot_start_time, spot_end_time, spot_week_day, sale_point, seller_discount_flag, seller_discount_type, seller_discount_amount, seller_point_flag, sold_out, stock_flag, stock_quantity, stock_code, stock_schedule_auto_flag, stock_schedule_type, stock_schedule_date, stock_schedule_text, order_min_quantity, order_max_quantity, sale_quantity, item_option_flag, item_option_type, item_option_title1, item_option_title2, item_option_title3, item_addition_flag, free_gift_flag, free_gift_name, item_keyword, detail_content, detail_content_mobile, item_notice_code, item_image, team, opentime, base_item, other_flag, recommend_flag, relation_item_display_type, delivery_company_id, delivery_company_name, delivery_type, shipment_id, shipment_group_code, shipment_return_type, shipment_return_id, shipping_type, shipping_group_code, shipping, shipping_free_amount, shipping_item_count, shipping_extra_charge1, shipping_extra_charge2, shipping_return, item_return_flag, hits, seo_title, seo_index_flag, seo_keywords, seo_description, seo_header_contents1, seo_themaword_title, seo_themaword_description, md_id, md_name, coupon_use_flag, data_status_message, data_status_code, updated_user_id, updated_date, created_user_id, created_date, front_display_flag, erp_exception_type, item_number, item_sale_status_text, noindex_yn, naver_shopping_item_name, naver_shopping_flag, naver_pay_flag, set_discount_type, set_discount_amount, representative_item_yn, adult_item_yn, mobile_item_yn) FROM stdin;
\.


--
-- Data for Name: op_item_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_category (item_category_id, item_id, category_id, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_filter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_filter (id, created, created_by, updated, updated_by, filter_code_id, filter_group_id, item_id, ordering) FROM stdin;
\.


--
-- Data for Name: op_item_hit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_hit (item_id, hits) FROM stdin;
\.


--
-- Data for Name: op_item_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_image (item_image_id, item_id, image_name, ordering, created_date, thumbnail_small, thumbnail_medium, thumbnail_large) FROM stdin;
\.


--
-- Data for Name: op_item_info; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_info (item_info_id, item_id, info_code, title, description, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_info_mobile; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_info_mobile (item_info_id, item_id, info_code, title, description, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_log (item_log_id, item_id, item_user_code, item_name, display_flag, sold_out, cost_price, item_price, sale_price, commission_type, commission_rate, price_criteria, process_page, created_manager_id, created_seller_id, created_date, action_type) FROM stdin;
\.


--
-- Data for Name: op_item_mall_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_mall_code (item_id, mall_config_id, product_code) FROM stdin;
\.


--
-- Data for Name: op_item_notice; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_notice (item_notice_code, item_notice_title, notice_title, notice_description, ordering) FROM stdin;
\.


--
-- Data for Name: op_item_option; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_option (item_option_id, item_id, option_type, option_display_type, option_hide_flag, option_name1, option_name2, option_name3, option_price, option_cost_price, option_price_nonmember, option_stock_flag, option_stock_quantity, option_stock_code, option_stock_schedule_date, option_stock_schedule_text, option_sold_out_flag, option_display_flag, created_user_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_option_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_option_image (item_option_image_id, item_id, item_option_id, option_name, option_image) FROM stdin;
\.


--
-- Data for Name: op_item_option_soldout; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_option_soldout (item_id, item_option_sold_out_flag) FROM stdin;
\.


--
-- Data for Name: op_item_ordering; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_ordering (item_ordering_id, item_id, category_id, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_other; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_other (item_other_id, item_id, other_item_id, counting) FROM stdin;
\.


--
-- Data for Name: op_item_relation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_relation (item_relation_id, item_id, related_item_id, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_review; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_review (item_review_id, item_id, order_code, subject, content, score, recommend_flag, user_id, user_name, seller_id, display_flag, created_date, point_payment, point, point_payment_date, data_status_code, display_options_flag, options, admin_comment, like_count, answer_login_id, answer_date) FROM stdin;
\.


--
-- Data for Name: op_item_review_filter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_review_filter (id, created, created_by, updated, updated_by, filter_code_id, filter_group_id, item_id, item_review_id, ordering) FROM stdin;
\.


--
-- Data for Name: op_item_review_image; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_review_image (item_review_image_id, item_review_id, review_image, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_item_review_like; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_review_like (id, created, created_by, updated, updated_by, ip, item_review_id, user_id) FROM stdin;
\.


--
-- Data for Name: op_item_sale_edit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_sale_edit (item_sale_edit_id, item_id, seller_id, seller_name, item_code, item_name, sale_price, item_price, cost_price, status, message, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_item_set; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_item_set (item_set_id, parent_item_id, item_id, quantity, ordering, created_date) FROM stdin;
\.


--
-- Data for Name: op_keyword; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_keyword (keyword, keyword_type, created_date, keyword_seperation, weight) FROM stdin;
\.


--
-- Data for Name: op_main_display_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_main_display_item (template_id, item_id, display_order, created_date) FROM stdin;
\.


--
-- Data for Name: op_mall_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mall_config (mall_config_id, mall_login_id, mall_type, mall_api_key, data_status_code, status_code, last_date, last_search_start_date, last_search_end_date, claim_status_code, last_claim_date, last_claim_search_start_date, last_claim_search_end_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_mall_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mall_order (mall_order_id, item_id, matched_options, mall_config_id, mall_type, order_item_status, order_item_status_label, order_code, order_index, shipping_group_code, shipping_group_flag, addition_item_flag, parent_product_code, shipping_code, product_code, option_code, product_name, option_name, sell_price, quantity, cancel_quantity, seller_discount_amount, sale_amount, option_amount, pay_shipping, pay_shipping_type, island_pay_shipping, island_pay_shipping_type, mall_product_code, member_id, member_type, member_no, buyer_name, buyer_telephone_number, buyer_phone_number, buyer_zipcode, buyer_address, buyer_address_detail, receiver_name, receiver_telephone_number, receiver_phone_number, receiver_zipcode, receiver_address, receiver_address_detail, content, system_message, pay_date, created_date, claim_apply_date) FROM stdin;
\.


--
-- Data for Name: op_mall_order_cancel; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mall_order_cancel (mall_order_id, claim_code, claim_quantity, claim_status, claim_apply_subject, cancel_reason, cancel_reason_text, cancel_refusal_reson, cancel_refusal_reson_text, cancel_apply_date) FROM stdin;
\.


--
-- Data for Name: op_mall_order_exchange; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mall_order_exchange (mall_order_id, claim_code, claim_quantity, claim_status, ex_reason, ex_reason_text, ex_collection_name, ex_collection_tel_number, ex_collection_phone_number, ex_collection_zipcode, ex_collection_zipcode_seq, ex_collection_address, ex_collection_address_detail, ex_collection_address_type, ex_collection_address_bilno, ex_shipping_type, ex_receiver_name, ex_receiver_tel_number, ex_receiver_phone_number, ex_receiver_zipcode, ex_receiver_zipcode_seq, ex_receiver_address, ex_receiver_address_detail, ex_receiver_address_type, ex_receiver_address_bilno, ex_shipping_amount, ex_add_shipping_amount, ex_shipping_payment_type, ex_shipping_number, ex_shipping_company_code, ex_refusal_reason, ex_refusal_reason_text, resend_delivery_company_code, resend_delivery_number, ex_apply_date, ex_end_date) FROM stdin;
\.


--
-- Data for Name: op_mall_order_return; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mall_order_return (mall_order_id, claim_code, claim_status, claim_quantity, rt_reason, rt_reason_text, rt_collection_name, rt_collection_tel_number, rt_collection_phone_number, rt_collection_zipcode, rt_collection_zipcode_seq, rt_collection_address, rt_collection_address_detail, rt_collection_address_type, rt_collection_address_bilno, rt_shipping_type, rt_shipping_amount, rt_default_shipping_amount, rt_add_shipping_amount, rt_add_shipping_type, rt_deduction_shipping_amount, rt_shipping_payment_type, rt_shipping_number, rt_shipping_company_code, rt_hold_reason, rt_hold_reason_text, rt_refusal_reason, rt_refusal_reason_text, rt_apply_date, rt_end_date) FROM stdin;
\.


--
-- Data for Name: op_mobile_category_edit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_mobile_category_edit (category_edit_id, code, edit_kind, edit_position, edit_content, edit_image, edit_url, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_ranking; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_ranking (ranking_id, category_url, item_id, ordering) FROM stdin;
\.


--
-- Data for Name: op_ranking_batch; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_ranking_batch (ranking_type, ranking_code, item_id, ordering, data_status_code) FROM stdin;
\.


--
-- Data for Name: op_ranking_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_ranking_config (rank_config_code, sale_price_days, sale_price_weight, sale_count_days, sale_count_weight, item_review_days, item_review_weight, item_hit_weight, created_date) FROM stdin;
\.


--
-- Data for Name: op_restock_notice; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_restock_notice (restock_notice_id, item_id, user_id, send_flag, created_date) FROM stdin;
\.


--
-- Data for Name: op_review_filter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_review_filter (id, created, created_by, updated, updated_by, category_id, filter_group_id, ordering) FROM stdin;
\.


--
-- Data for Name: op_seller; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_seller (seller_id, seller_name, login_id, password, user_name, telephone_number, phone_number, fax_number, email, post, address, address_detail, second_user_name, second_telephone_number, second_phone_number, second_email, company_name, representative_name, business_number, business_location, business_type, business_items, commission_rate, remittance_type, remittance_day, bank_name, bank_in_name, bank_account_number, shipping_flag, shipping, shipping_free_amount, shipping_extra_charge1, shipping_extra_charge2, header_content, item_approval_type, sms_send_time, sms_md_flag, md_id, md_name, status_code, created_date, created_user_id, updated_date, updated_user_id, mail_order_number, buy_safety_use_confirm_number, tax_type, locgov_code, file_name_certificate1, file_name_certificate2, file_name_certificate3, adult_item_yn, community_business_yn) FROM stdin;
9001	미등록 판매자 9001	seller9001	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	미등록 판매자 9001	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	20260825021624	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	N
9002	미등록 판매자 9002	seller9002	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	미등록 판매자 9002	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	20260825021624	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	N
9003	미등록 판매자 9003	seller9003	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	미등록 판매자 9003	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	20260825021624	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	N
9004	미등록 판매자 9004	seller9004	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	미등록 판매자 9004	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	20260825021624	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	N
9502	청주시마을기업협동조합	seller9502	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	\N	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	Y
9504	충주시마을기업협동조합	seller9504	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	\N	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	Y
9509	북구마을기업협동조합	seller9509	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	N	\N	\N	\N	\N	\N	1	\N	N	\N	\N	1	\N	0	\N	0	\N	\N	1	\N	\N	\N	\N	N	Y
\.


--
-- Data for Name: op_seller_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_seller_user (user_id, login_id, password, user_name, email, phone_number, status_code, login_count, login_date, deny_date, leave_date, login_fail_count, login_try_date, password_type, password_expired_date, updated_date, created_date, locgov_code) FROM stdin;
\.


--
-- Data for Name: op_seller_user_login; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_seller_user_login (id, created_at, updated_at, session_id, user_id) FROM stdin;
\.


--
-- Data for Name: op_store; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_store (id, created, created_by, updated, updated_by, address, address_detail, end_time, name, new_post, post, sido, start_time, store_type, tel_number) FROM stdin;
\.


--
-- Data for Name: op_style_book; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_style_book (id, created, created_by, updated, updated_by, content, image, ordering, title) FROM stdin;
\.


--
-- Data for Name: op_style_book_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_style_book_item (id, created, created_by, updated, updated_by, item_id, ordering, style_book_id) FROM stdin;
\.


--
-- Data for Name: op_wishlist; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_wishlist (wishlist_id, wishlist_group_id, item_id, item_option, item_option_group_name, item_option_name, user_id, created_date, temp_option, item_option_1, item_option_2, item_option_3, item_option_4, item_option_5, item_option_6, item_option_7, item_option_8) FROM stdin;
1000	1000	1000	\N	\N	\N	1000	20260824051158	\N	\N	\N	\N	\N	\N	\N	\N	\N
1001	1001	1001	\N	\N	\N	1000	20260824051158	\N	\N	\N	\N	\N	\N	\N	\N	\N
1002	1002	1002	\N	\N	\N	1001	20260824051158	\N	\N	\N	\N	\N	\N	\N	\N	\N
1003	1003	1003	\N	\N	\N	1001	20260824051158	\N	\N	\N	\N	\N	\N	\N	\N	\N
1004	1004	1000	\N	\N	\N	1002	20260824051158	\N	\N	\N	\N	\N	\N	\N	\N	\N
\.


--
-- Name: g_item_content_img_desc_item_content_img_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_item_content_img_desc_item_content_img_id_seq', 1000, false);


--
-- Name: g_item_inquiry_inquiry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_item_inquiry_inquiry_id_seq', 1, false);


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq', 1000, false);


--
-- Name: gift_subcategory_item_subcategory_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gift_subcategory_item_subcategory_item_id_seq', 40, true);


--
-- Name: gift_subcategory_subcategory_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gift_subcategory_subcategory_id_seq', 51, true);


--
-- Name: op_brand_brand_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_brand_brand_id_seq', 1000, true);


--
-- Name: op_category_group_category_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_category_group_category_group_id_seq', 1000, false);


--
-- Name: op_category_team_category_team_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_category_team_category_team_id_seq', 1000, false);


--
-- Name: op_featured_item_reg_seq_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_featured_item_reg_seq_seq', 1000, false);


--
-- Name: op_item_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_image_id_seq', 1, false);


--
-- Name: op_item_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_item_id_seq', 1003, true);


--
-- Name: op_item_log_item_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_log_item_log_id_seq', 1000, false);


--
-- Name: op_item_ordering_item_ordering_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_ordering_item_ordering_id_seq', 1000, false);


--
-- Name: op_item_review_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_review_image_id_seq', 1, false);


--
-- Name: op_item_review_image_item_review_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_review_image_item_review_image_id_seq', 1000, false);


--
-- Name: op_item_review_review_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_review_review_id_seq', 1, false);


--
-- Name: op_item_set_item_set_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_item_set_item_set_id_seq', 1000, false);


--
-- Name: op_seller_seller_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_seller_seller_id_seq', 10001, true);


--
-- Name: op_wishlist_wishlist_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_wishlist_wishlist_id_seq', 1004, true);


--
-- Name: b_gift_sell b_gift_sell_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.b_gift_sell
    ADD CONSTRAINT b_gift_sell_pkey PRIMARY KEY (cntr_ymd, locgov_code, item_id);


--
-- Name: g_gds_img_expln_link_view g_gds_img_expln_link_view_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_gds_img_expln_link_view
    ADD CONSTRAINT g_gds_img_expln_link_view_pkey PRIMARY KEY (gds_user_cd, img_seq);


--
-- Name: g_intrst_rtnpsnt g_intrst_rtnpsnt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_intrst_rtnpsnt
    ADD CONSTRAINT g_intrst_rtnpsnt_pkey PRIMARY KEY (user_id, regist_sn);


--
-- Name: g_item_content_img_desc g_item_content_img_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_item_content_img_desc
    ADD CONSTRAINT g_item_content_img_desc_pkey PRIMARY KEY (item_content_img_id);


--
-- Name: g_item_inquiry g_item_inquiry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_item_inquiry
    ADD CONSTRAINT g_item_inquiry_pkey PRIMARY KEY (inquiry_id);


--
-- Name: g_lclgv_off_rprs_gds_mng g_lclgv_off_rprs_gds_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_off_rprs_gds_mng
    ADD CONSTRAINT g_lclgv_off_rprs_gds_mng_pkey PRIMARY KEY (gds_id, lclgv_cd);


--
-- Name: g_lclgv_pbadms_wlfr_cntr_mng g_lclgv_pbadms_wlfr_cntr_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_pbadms_wlfr_cntr_mng
    ADD CONSTRAINT g_lclgv_pbadms_wlfr_cntr_mng_pkey PRIMARY KEY (pbadms_wlfr_cntr_id);


--
-- Name: g_lclgv_rprs_gds_mng g_lclgv_rprs_gds_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_lclgv_rprs_gds_mng
    ADD CONSTRAINT g_lclgv_rprs_gds_mng_pkey PRIMARY KEY (gds_id, lclgv_cd);


--
-- Name: g_myrecent g_myrecent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_myrecent
    ADD CONSTRAINT g_myrecent_pkey PRIMARY KEY (recent_id);


--
-- Name: g_season_food_item g_season_food_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_season_food_item
    ADD CONSTRAINT g_season_food_item_pkey PRIMARY KEY (item_id, season_food_month);


--
-- Name: g_season_food g_season_food_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_season_food
    ADD CONSTRAINT g_season_food_pkey PRIMARY KEY (season_food_month, reg_seq);


--
-- Name: g_spcl_item_mng_keyword g_spcl_item_mng_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_spcl_item_mng_keyword
    ADD CONSTRAINT g_spcl_item_mng_keyword_pkey PRIMARY KEY (spcl_item_mng_id, reg_seq);


--
-- Name: g_spcl_item_mng g_spcl_item_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_spcl_item_mng
    ADD CONSTRAINT g_spcl_item_mng_pkey PRIMARY KEY (spcl_item_mng_id);


--
-- Name: g_spcl_item g_spcl_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_spcl_item
    ADD CONSTRAINT g_spcl_item_pkey PRIMARY KEY (spcl_item_mng_id, item_id);


--
-- Name: gift_order_stock gift_order_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_order_stock
    ADD CONSTRAINT gift_order_stock_pkey PRIMARY KEY (order_id);


--
-- Name: gift_subcategory_item gift_subcategory_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_subcategory_item
    ADD CONSTRAINT gift_subcategory_item_pkey PRIMARY KEY (subcategory_item_id);


--
-- Name: gift_subcategory gift_subcategory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_subcategory
    ADD CONSTRAINT gift_subcategory_pkey PRIMARY KEY (subcategory_id);


--
-- Name: mig_op_item_image mig_op_item_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mig_op_item_image
    ADD CONSTRAINT mig_op_item_image_pkey PRIMARY KEY (item_image_id);


--
-- Name: op_brand_category op_brand_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_brand_category
    ADD CONSTRAINT op_brand_category_pkey PRIMARY KEY (brand_category_id);


--
-- Name: op_brand op_brand_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_brand
    ADD CONSTRAINT op_brand_pkey PRIMARY KEY (brand_id);


--
-- Name: op_category_edit op_category_edit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_edit
    ADD CONSTRAINT op_category_edit_pkey PRIMARY KEY (category_edit_id);


--
-- Name: op_category_filter op_category_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_filter
    ADD CONSTRAINT op_category_filter_pkey PRIMARY KEY (id);


--
-- Name: op_category_group_banner op_category_group_banner_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_group_banner
    ADD CONSTRAINT op_category_group_banner_pkey PRIMARY KEY (category_group_banner_id);


--
-- Name: op_category_group op_category_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_group
    ADD CONSTRAINT op_category_group_pkey PRIMARY KEY (category_group_id);


--
-- Name: op_category op_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category
    ADD CONSTRAINT op_category_pkey PRIMARY KEY (category_id);


--
-- Name: op_category_team_item op_category_team_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_team_item
    ADD CONSTRAINT op_category_team_item_pkey PRIMARY KEY (category_team_item_id);


--
-- Name: op_category_team op_category_team_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_team
    ADD CONSTRAINT op_category_team_pkey PRIMARY KEY (category_team_id);


--
-- Name: op_common_code op_common_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_common_code
    ADD CONSTRAINT op_common_code_pkey PRIMARY KEY (code_type, code_language, id);


--
-- Name: op_display_group_code op_display_group_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_display_group_code
    ADD CONSTRAINT op_display_group_code_pkey PRIMARY KEY (display_group_code);


--
-- Name: op_display_sns op_display_sns_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_display_sns
    ADD CONSTRAINT op_display_sns_pkey PRIMARY KEY (sns_id);


--
-- Name: op_display_template op_display_template_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_display_template
    ADD CONSTRAINT op_display_template_pkey PRIMARY KEY (display_template_code);


--
-- Name: op_featured_banner op_featured_banner_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_banner
    ADD CONSTRAINT op_featured_banner_pkey PRIMARY KEY (featured_banner_id);


--
-- Name: op_featured_item op_featured_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_item
    ADD CONSTRAINT op_featured_item_pkey PRIMARY KEY (reg_seq);


--
-- Name: op_featured op_featured_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured
    ADD CONSTRAINT op_featured_pkey PRIMARY KEY (featured_id);


--
-- Name: op_featured_reply op_featured_reply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_reply
    ADD CONSTRAINT op_featured_reply_pkey PRIMARY KEY (id);


--
-- Name: op_filter_code op_filter_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_filter_code
    ADD CONSTRAINT op_filter_code_pkey PRIMARY KEY (id);


--
-- Name: op_filter_group op_filter_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_filter_group
    ADD CONSTRAINT op_filter_group_pkey PRIMARY KEY (id);


--
-- Name: op_gift_group_item op_gift_group_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_group_item
    ADD CONSTRAINT op_gift_group_item_pkey PRIMARY KEY (id);


--
-- Name: op_gift_group op_gift_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_group
    ADD CONSTRAINT op_gift_group_pkey PRIMARY KEY (id);


--
-- Name: op_gift_item_log op_gift_item_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_item_log
    ADD CONSTRAINT op_gift_item_log_pkey PRIMARY KEY (id);


--
-- Name: op_gift_item op_gift_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_item
    ADD CONSTRAINT op_gift_item_pkey PRIMARY KEY (id);


--
-- Name: op_gift_item_relation op_gift_item_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_item_relation
    ADD CONSTRAINT op_gift_item_relation_pkey PRIMARY KEY (id);


--
-- Name: op_item_addition op_item_addition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_addition
    ADD CONSTRAINT op_item_addition_pkey PRIMARY KEY (item_id, addition_item_id);


--
-- Name: op_item_category op_item_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_category
    ADD CONSTRAINT op_item_category_pkey PRIMARY KEY (item_category_id);


--
-- Name: op_item_filter op_item_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_filter
    ADD CONSTRAINT op_item_filter_pkey PRIMARY KEY (id);


--
-- Name: op_item_image op_item_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_image
    ADD CONSTRAINT op_item_image_pkey PRIMARY KEY (item_image_id);


--
-- Name: op_item_info_mobile op_item_info_mobile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_info_mobile
    ADD CONSTRAINT op_item_info_mobile_pkey PRIMARY KEY (item_info_id);


--
-- Name: op_item_info op_item_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_info
    ADD CONSTRAINT op_item_info_pkey PRIMARY KEY (item_info_id);


--
-- Name: op_item_log op_item_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_log
    ADD CONSTRAINT op_item_log_pkey PRIMARY KEY (item_log_id);


--
-- Name: op_item_notice op_item_notice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_notice
    ADD CONSTRAINT op_item_notice_pkey PRIMARY KEY (item_notice_code, notice_title);


--
-- Name: op_item_option_image op_item_option_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_option_image
    ADD CONSTRAINT op_item_option_image_pkey PRIMARY KEY (item_option_image_id);


--
-- Name: op_item_option op_item_option_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_option
    ADD CONSTRAINT op_item_option_pkey PRIMARY KEY (item_option_id);


--
-- Name: op_item_option_soldout op_item_option_soldout_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_option_soldout
    ADD CONSTRAINT op_item_option_soldout_pkey PRIMARY KEY (item_id);


--
-- Name: op_item_ordering op_item_ordering_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_ordering
    ADD CONSTRAINT op_item_ordering_pkey PRIMARY KEY (item_ordering_id);


--
-- Name: op_item_other op_item_other_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_other
    ADD CONSTRAINT op_item_other_pkey PRIMARY KEY (item_other_id);


--
-- Name: op_item op_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item
    ADD CONSTRAINT op_item_pkey PRIMARY KEY (item_id);


--
-- Name: op_item_relation op_item_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_relation
    ADD CONSTRAINT op_item_relation_pkey PRIMARY KEY (item_relation_id);


--
-- Name: op_item_review_filter op_item_review_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review_filter
    ADD CONSTRAINT op_item_review_filter_pkey PRIMARY KEY (id);


--
-- Name: op_item_review_image op_item_review_image_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review_image
    ADD CONSTRAINT op_item_review_image_pkey PRIMARY KEY (item_review_image_id);


--
-- Name: op_item_review_like op_item_review_like_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review_like
    ADD CONSTRAINT op_item_review_like_pkey PRIMARY KEY (id);


--
-- Name: op_item_review op_item_review_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_review
    ADD CONSTRAINT op_item_review_pkey PRIMARY KEY (item_review_id);


--
-- Name: op_item_sale_edit op_item_sale_edit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_sale_edit
    ADD CONSTRAINT op_item_sale_edit_pkey PRIMARY KEY (item_sale_edit_id);


--
-- Name: op_item_set op_item_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_set
    ADD CONSTRAINT op_item_set_pkey PRIMARY KEY (item_set_id);


--
-- Name: op_keyword op_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_keyword
    ADD CONSTRAINT op_keyword_pkey PRIMARY KEY (keyword, keyword_type, created_date);


--
-- Name: op_mall_config op_mall_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_config
    ADD CONSTRAINT op_mall_config_pkey PRIMARY KEY (mall_config_id);


--
-- Name: op_mall_order_cancel op_mall_order_cancel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_cancel
    ADD CONSTRAINT op_mall_order_cancel_pkey PRIMARY KEY (mall_order_id, claim_code);


--
-- Name: op_mall_order_exchange op_mall_order_exchange_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_exchange
    ADD CONSTRAINT op_mall_order_exchange_pkey PRIMARY KEY (mall_order_id, claim_code);


--
-- Name: op_mall_order op_mall_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order
    ADD CONSTRAINT op_mall_order_pkey PRIMARY KEY (mall_order_id);


--
-- Name: op_mall_order_return op_mall_order_return_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_return
    ADD CONSTRAINT op_mall_order_return_pkey PRIMARY KEY (mall_order_id, claim_code);


--
-- Name: op_ranking_config op_ranking_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_ranking_config
    ADD CONSTRAINT op_ranking_config_pkey PRIMARY KEY (rank_config_code);


--
-- Name: op_ranking op_ranking_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_ranking
    ADD CONSTRAINT op_ranking_pkey PRIMARY KEY (ranking_id);


--
-- Name: op_restock_notice op_restock_notice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_restock_notice
    ADD CONSTRAINT op_restock_notice_pkey PRIMARY KEY (restock_notice_id);


--
-- Name: op_review_filter op_review_filter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_review_filter
    ADD CONSTRAINT op_review_filter_pkey PRIMARY KEY (id);


--
-- Name: op_seller op_seller_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_seller
    ADD CONSTRAINT op_seller_pkey PRIMARY KEY (seller_id);


--
-- Name: op_seller_user_login op_seller_user_login_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_seller_user_login
    ADD CONSTRAINT op_seller_user_login_pkey PRIMARY KEY (id);


--
-- Name: op_store op_store_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_store
    ADD CONSTRAINT op_store_pkey PRIMARY KEY (id);


--
-- Name: op_style_book_item op_style_book_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_style_book_item
    ADD CONSTRAINT op_style_book_item_pkey PRIMARY KEY (id);


--
-- Name: op_style_book op_style_book_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_style_book
    ADD CONSTRAINT op_style_book_pkey PRIMARY KEY (id);


--
-- Name: op_wishlist op_wishlist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_wishlist
    ADD CONSTRAINT op_wishlist_pkey PRIMARY KEY (wishlist_id);


--
-- Name: op_category_group_banner fk_op_category_group_banner_category_group_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_group_banner
    ADD CONSTRAINT fk_op_category_group_banner_category_group_id FOREIGN KEY (category_group_id) REFERENCES public.op_category_group(category_group_id);


--
-- Name: op_category_group fk_op_category_group_category_team_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_category_group
    ADD CONSTRAINT fk_op_category_group_category_team_id FOREIGN KEY (category_team_id) REFERENCES public.op_category_team(category_team_id);


--
-- Name: op_featured_banner_item fk_op_featured_banner_item_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_banner_item
    ADD CONSTRAINT fk_op_featured_banner_item_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_featured_item fk_op_featured_item_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_item
    ADD CONSTRAINT fk_op_featured_item_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_featured_reply fk_op_featured_reply_featured_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_featured_reply
    ADD CONSTRAINT fk_op_featured_reply_featured_id FOREIGN KEY (featured_id) REFERENCES public.op_featured(featured_id);


--
-- Name: op_filter_code fk_op_filter_code_filter_group_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_filter_code
    ADD CONSTRAINT fk_op_filter_code_filter_group_id FOREIGN KEY (filter_group_id) REFERENCES public.op_filter_group(id);


--
-- Name: op_gift_group_item fk_op_gift_group_item_gift_group_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_group_item
    ADD CONSTRAINT fk_op_gift_group_item_gift_group_id FOREIGN KEY (gift_group_id) REFERENCES public.op_gift_group(id);


--
-- Name: op_gift_group_item fk_op_gift_group_item_gift_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_group_item
    ADD CONSTRAINT fk_op_gift_group_item_gift_item_id FOREIGN KEY (gift_item_id) REFERENCES public.op_gift_item(id);


--
-- Name: op_gift_item_relation fk_op_gift_item_relation_gift_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_gift_item_relation
    ADD CONSTRAINT fk_op_gift_item_relation_gift_item_id FOREIGN KEY (gift_item_id) REFERENCES public.op_gift_item(id);


--
-- Name: op_item_addition fk_op_item_addition_addition_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_addition
    ADD CONSTRAINT fk_op_item_addition_addition_item_id FOREIGN KEY (addition_item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_addition fk_op_item_addition_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_addition
    ADD CONSTRAINT fk_op_item_addition_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_info fk_op_item_info_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_info
    ADD CONSTRAINT fk_op_item_info_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_info_mobile fk_op_item_info_mobile_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_info_mobile
    ADD CONSTRAINT fk_op_item_info_mobile_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_mall_code fk_op_item_mall_code_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_mall_code
    ADD CONSTRAINT fk_op_item_mall_code_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_mall_code fk_op_item_mall_code_mall_config_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_mall_code
    ADD CONSTRAINT fk_op_item_mall_code_mall_config_id FOREIGN KEY (mall_config_id) REFERENCES public.op_mall_config(mall_config_id);


--
-- Name: op_item_option_image fk_op_item_option_image_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_option_image
    ADD CONSTRAINT fk_op_item_option_image_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_ordering fk_op_item_ordering_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_ordering
    ADD CONSTRAINT fk_op_item_ordering_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_other fk_op_item_other_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_other
    ADD CONSTRAINT fk_op_item_other_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_relation fk_op_item_relation_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_relation
    ADD CONSTRAINT fk_op_item_relation_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_item_set fk_op_item_set_parent_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_item_set
    ADD CONSTRAINT fk_op_item_set_parent_item_id FOREIGN KEY (parent_item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_main_display_item fk_op_main_display_item_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_main_display_item
    ADD CONSTRAINT fk_op_main_display_item_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_mall_order_cancel fk_op_mall_order_cancel_mall_order_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_cancel
    ADD CONSTRAINT fk_op_mall_order_cancel_mall_order_id FOREIGN KEY (mall_order_id) REFERENCES public.op_mall_order(mall_order_id);


--
-- Name: op_mall_order_exchange fk_op_mall_order_exchange_mall_order_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_exchange
    ADD CONSTRAINT fk_op_mall_order_exchange_mall_order_id FOREIGN KEY (mall_order_id) REFERENCES public.op_mall_order(mall_order_id);


--
-- Name: op_mall_order_return fk_op_mall_order_return_mall_order_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_mall_order_return
    ADD CONSTRAINT fk_op_mall_order_return_mall_order_id FOREIGN KEY (mall_order_id) REFERENCES public.op_mall_order(mall_order_id);


--
-- Name: op_restock_notice fk_op_restock_notice_item_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_restock_notice
    ADD CONSTRAINT fk_op_restock_notice_item_id FOREIGN KEY (item_id) REFERENCES public.op_item(item_id);


--
-- Name: op_style_book_item fk_op_style_book_item_style_book_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_style_book_item
    ADD CONSTRAINT fk_op_style_book_item_style_book_id FOREIGN KEY (style_book_id) REFERENCES public.op_style_book(id);


--
-- Name: gift_subcategory_item gift_subcategory_item_subcategory_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gift_subcategory_item
    ADD CONSTRAINT gift_subcategory_item_subcategory_id_fkey FOREIGN KEY (subcategory_id) REFERENCES public.gift_subcategory(subcategory_id);


--
-- Name: TABLE b_gift_sell; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.b_gift_sell TO gift;


--
-- Name: TABLE g_gds_img_expln_link_view; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_gds_img_expln_link_view TO gift;


--
-- Name: TABLE g_intrst_rtnpsnt; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_intrst_rtnpsnt TO gift;


--
-- Name: TABLE g_item_content_img_desc; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_item_content_img_desc TO gift;


--
-- Name: SEQUENCE g_item_content_img_desc_item_content_img_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_item_content_img_desc_item_content_img_id_seq TO gift;


--
-- Name: TABLE g_item_inquiry; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_item_inquiry TO gift;


--
-- Name: SEQUENCE g_item_inquiry_inquiry_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_item_inquiry_inquiry_id_seq TO gift;


--
-- Name: TABLE g_lclgv_off_rprs_gds_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_lclgv_off_rprs_gds_mng TO gift;


--
-- Name: TABLE g_lclgv_pbadms_wlfr_cntr_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_lclgv_pbadms_wlfr_cntr_mng TO gift;


--
-- Name: SEQUENCE g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.g_lclgv_pbadms_wlfr_cntr_mng_pbadms_wlfr_cntr_id_seq TO gift;


--
-- Name: TABLE g_lclgv_rprs_gds_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_lclgv_rprs_gds_mng TO gift;


--
-- Name: TABLE g_myrecent; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_myrecent TO gift;


--
-- Name: TABLE g_season_food; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_season_food TO gift;


--
-- Name: TABLE g_season_food_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_season_food_item TO gift;


--
-- Name: TABLE g_spcl_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_spcl_item TO gift;


--
-- Name: TABLE g_spcl_item_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_spcl_item_mng TO gift;


--
-- Name: TABLE g_spcl_item_mng_keyword; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_spcl_item_mng_keyword TO gift;


--
-- Name: TABLE gift_order_stock; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gift_order_stock TO gift;


--
-- Name: TABLE gift_subcategory; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gift_subcategory TO gift;


--
-- Name: TABLE gift_subcategory_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.gift_subcategory_item TO gift;


--
-- Name: SEQUENCE gift_subcategory_item_subcategory_item_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.gift_subcategory_item_subcategory_item_id_seq TO gift;


--
-- Name: SEQUENCE gift_subcategory_subcategory_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.gift_subcategory_subcategory_id_seq TO gift;


--
-- Name: TABLE mig_op_item_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.mig_op_item_image TO gift;


--
-- Name: TABLE op_brand; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_brand TO gift;


--
-- Name: SEQUENCE op_brand_brand_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_brand_brand_id_seq TO gift;


--
-- Name: TABLE op_brand_category; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_brand_category TO gift;


--
-- Name: TABLE op_category; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category TO gift;


--
-- Name: TABLE op_category_edit; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_edit TO gift;


--
-- Name: TABLE op_category_filter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_filter TO gift;


--
-- Name: TABLE op_category_group; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_group TO gift;


--
-- Name: TABLE op_category_group_banner; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_group_banner TO gift;


--
-- Name: SEQUENCE op_category_group_category_group_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_category_group_category_group_id_seq TO gift;


--
-- Name: TABLE op_category_team; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_team TO gift;


--
-- Name: SEQUENCE op_category_team_category_team_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_category_team_category_team_id_seq TO gift;


--
-- Name: TABLE op_category_team_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_category_team_item TO gift;


--
-- Name: TABLE op_common_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_common_code TO gift;


--
-- Name: TABLE op_display_editor; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_editor TO gift;


--
-- Name: TABLE op_display_group_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_group_code TO gift;


--
-- Name: TABLE op_display_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_image TO gift;


--
-- Name: TABLE op_display_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_item TO gift;


--
-- Name: TABLE op_display_sns; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_sns TO gift;


--
-- Name: TABLE op_display_template; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_display_template TO gift;


--
-- Name: TABLE op_featured; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_featured TO gift;


--
-- Name: TABLE op_featured_banner; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_featured_banner TO gift;


--
-- Name: TABLE op_featured_banner_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_featured_banner_item TO gift;


--
-- Name: TABLE op_featured_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_featured_item TO gift;


--
-- Name: SEQUENCE op_featured_item_reg_seq_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_featured_item_reg_seq_seq TO gift;


--
-- Name: TABLE op_featured_reply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_featured_reply TO gift;


--
-- Name: TABLE op_filter_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_filter_code TO gift;


--
-- Name: TABLE op_filter_group; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_filter_group TO gift;


--
-- Name: TABLE op_gift_group; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_gift_group TO gift;


--
-- Name: TABLE op_gift_group_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_gift_group_item TO gift;


--
-- Name: TABLE op_gift_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_gift_item TO gift;


--
-- Name: TABLE op_gift_item_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_gift_item_log TO gift;


--
-- Name: TABLE op_gift_item_relation; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_gift_item_relation TO gift;


--
-- Name: TABLE op_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item TO gift;


--
-- Name: TABLE op_item_addition; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_addition TO gift;


--
-- Name: TABLE op_item_bakup_0611_option; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_bakup_0611_option TO gift;


--
-- Name: TABLE op_item_category; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_category TO gift;


--
-- Name: TABLE op_item_filter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_filter TO gift;


--
-- Name: TABLE op_item_hit; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_hit TO gift;


--
-- Name: TABLE op_item_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_image TO gift;


--
-- Name: SEQUENCE op_item_image_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_image_id_seq TO gift;


--
-- Name: TABLE op_item_info; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_info TO gift;


--
-- Name: TABLE op_item_info_mobile; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_info_mobile TO gift;


--
-- Name: SEQUENCE op_item_item_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_item_id_seq TO gift;


--
-- Name: TABLE op_item_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_log TO gift;


--
-- Name: SEQUENCE op_item_log_item_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_log_item_log_id_seq TO gift;


--
-- Name: TABLE op_item_mall_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_mall_code TO gift;


--
-- Name: TABLE op_item_notice; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_notice TO gift;


--
-- Name: TABLE op_item_option; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_option TO gift;


--
-- Name: TABLE op_item_option_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_option_image TO gift;


--
-- Name: TABLE op_item_option_soldout; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_option_soldout TO gift;


--
-- Name: TABLE op_item_ordering; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_ordering TO gift;


--
-- Name: SEQUENCE op_item_ordering_item_ordering_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_ordering_item_ordering_id_seq TO gift;


--
-- Name: TABLE op_item_other; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_other TO gift;


--
-- Name: TABLE op_item_relation; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_relation TO gift;


--
-- Name: TABLE op_item_review; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_review TO gift;


--
-- Name: TABLE op_item_review_filter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_review_filter TO gift;


--
-- Name: TABLE op_item_review_image; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_review_image TO gift;


--
-- Name: SEQUENCE op_item_review_image_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_review_image_id_seq TO gift;


--
-- Name: SEQUENCE op_item_review_image_item_review_image_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_review_image_item_review_image_id_seq TO gift;


--
-- Name: TABLE op_item_review_like; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_review_like TO gift;


--
-- Name: SEQUENCE op_item_review_review_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_review_review_id_seq TO gift;


--
-- Name: TABLE op_item_sale_edit; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_sale_edit TO gift;


--
-- Name: TABLE op_item_set; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_item_set TO gift;


--
-- Name: SEQUENCE op_item_set_item_set_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_item_set_item_set_id_seq TO gift;


--
-- Name: TABLE op_keyword; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_keyword TO gift;


--
-- Name: TABLE op_main_display_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_main_display_item TO gift;


--
-- Name: TABLE op_mall_config; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mall_config TO gift;


--
-- Name: TABLE op_mall_order; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mall_order TO gift;


--
-- Name: TABLE op_mall_order_cancel; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mall_order_cancel TO gift;


--
-- Name: TABLE op_mall_order_exchange; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mall_order_exchange TO gift;


--
-- Name: TABLE op_mall_order_return; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mall_order_return TO gift;


--
-- Name: TABLE op_mobile_category_edit; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_mobile_category_edit TO gift;


--
-- Name: TABLE op_ranking; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_ranking TO gift;


--
-- Name: TABLE op_ranking_batch; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_ranking_batch TO gift;


--
-- Name: TABLE op_ranking_config; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_ranking_config TO gift;


--
-- Name: TABLE op_restock_notice; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_restock_notice TO gift;


--
-- Name: TABLE op_review_filter; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_review_filter TO gift;


--
-- Name: TABLE op_seller; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_seller TO gift;


--
-- Name: SEQUENCE op_seller_seller_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_seller_seller_id_seq TO gift;


--
-- Name: TABLE op_seller_user; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_seller_user TO gift;


--
-- Name: TABLE op_seller_user_login; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_seller_user_login TO gift;


--
-- Name: TABLE op_store; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_store TO gift;


--
-- Name: TABLE op_style_book; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_style_book TO gift;


--
-- Name: TABLE op_style_book_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_style_book_item TO gift;


--
-- Name: TABLE op_wishlist; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_wishlist TO gift;


--
-- Name: SEQUENCE op_wishlist_wishlist_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_wishlist_wishlist_id_seq TO gift;


--
-- Name: TABLE view_search_event; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.view_search_event TO gift;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO gift;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO gift;


--
-- PostgreSQL database dump complete
--

\unrestrict DtQdBd8YPM9OvRR8Ftzqs0p2tP8lScJaGlXeXnn9ZhAA565QyxfIvCYkmvkhsOi

