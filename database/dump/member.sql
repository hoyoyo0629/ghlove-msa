--
-- PostgreSQL database dump
--

\restrict LFzFOu9N3EYKxkOZyI2RiS3EwG4yuktQsQpdBZnHmnOUjxcmTf2dPlpj3gsKiyk

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

ALTER TABLE IF EXISTS ONLY public.op_user_parent DROP CONSTRAINT IF EXISTS fk_op_user_parent_user_id;
ALTER TABLE IF EXISTS ONLY public.op_user_group DROP CONSTRAINT IF EXISTS fk_op_user_group_user_id;
ALTER TABLE IF EXISTS ONLY public.op_user_detail DROP CONSTRAINT IF EXISTS fk_op_user_detail_user_id;
ALTER TABLE IF EXISTS ONLY public.op_user_delivery DROP CONSTRAINT IF EXISTS fk_op_user_delivery_user_id;
ALTER TABLE IF EXISTS ONLY public.op_user_birthday DROP CONSTRAINT IF EXISTS fk_op_user_birthday_user_id;
ALTER TABLE IF EXISTS ONLY public.op_sns_user DROP CONSTRAINT IF EXISTS fk_op_sns_user_user_id;
ALTER TABLE IF EXISTS ONLY public.op_sns_user_detail DROP CONSTRAINT IF EXISTS fk_op_sns_user_detail_sns_user_id;
ALTER TABLE IF EXISTS ONLY public.op_session_attributes DROP CONSTRAINT IF EXISTS fk_op_session_attributes_session_primary_id;
ALTER TABLE IF EXISTS ONLY public.op_user_sns DROP CONSTRAINT IF EXISTS op_user_sns_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_sleep DROP CONSTRAINT IF EXISTS op_user_sleep_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_role_request DROP CONSTRAINT IF EXISTS op_user_role_request_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_role DROP CONSTRAINT IF EXISTS op_user_role_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user DROP CONSTRAINT IF EXISTS op_user_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_parent DROP CONSTRAINT IF EXISTS op_user_parent_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_login DROP CONSTRAINT IF EXISTS op_user_login_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_login_log DROP CONSTRAINT IF EXISTS op_user_login_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_level DROP CONSTRAINT IF EXISTS op_user_level_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_group DROP CONSTRAINT IF EXISTS op_user_group_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_event DROP CONSTRAINT IF EXISTS op_user_event_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_detail DROP CONSTRAINT IF EXISTS op_user_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_delivery DROP CONSTRAINT IF EXISTS op_user_delivery_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_ci DROP CONSTRAINT IF EXISTS op_user_ci_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_change_log DROP CONSTRAINT IF EXISTS op_user_change_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_birthday_temp DROP CONSTRAINT IF EXISTS op_user_birthday_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_birthday DROP CONSTRAINT IF EXISTS op_user_birthday_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_auth DROP CONSTRAINT IF EXISTS op_user_auth_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_agree DROP CONSTRAINT IF EXISTS op_user_agree_pkey;
ALTER TABLE IF EXISTS ONLY public.op_user_action_log DROP CONSTRAINT IF EXISTS op_user_action_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_sns_user DROP CONSTRAINT IF EXISTS op_sns_user_pkey;
ALTER TABLE IF EXISTS ONLY public.op_sns_user_detail DROP CONSTRAINT IF EXISTS op_sns_user_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_session DROP CONSTRAINT IF EXISTS op_session_pkey;
ALTER TABLE IF EXISTS ONLY public.op_session_attributes DROP CONSTRAINT IF EXISTS op_session_attributes_pkey;
ALTER TABLE IF EXISTS ONLY public.op_role_hierarchy DROP CONSTRAINT IF EXISTS op_role_hierarchy_pkey;
ALTER TABLE IF EXISTS ONLY public.op_group DROP CONSTRAINT IF EXISTS op_group_pkey;
ALTER TABLE IF EXISTS ONLY public.op_customer DROP CONSTRAINT IF EXISTS op_customer_pkey;
ALTER TABLE IF EXISTS ONLY public.op_common_code DROP CONSTRAINT IF EXISTS op_common_code_pkey;
ALTER TABLE IF EXISTS ONLY public.g_naver_auth_login_info_mng DROP CONSTRAINT IF EXISTS g_naver_auth_login_info_mng_pkey;
ALTER TABLE IF EXISTS ONLY public.g_mber_secsn DROP CONSTRAINT IF EXISTS g_mber_secsn_pkey;
ALTER TABLE IF EXISTS ONLY public.g_indvdlinfo_readng_hist DROP CONSTRAINT IF EXISTS g_indvdlinfo_readng_hist_pkey;
ALTER TABLE IF EXISTS public.op_user_sns ALTER COLUMN sns_user_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_role_request ALTER COLUMN request_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_parent ALTER COLUMN parent_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_login_log ALTER COLUMN login_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_event ALTER COLUMN event_sn DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_delivery ALTER COLUMN user_delivery_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_change_log ALTER COLUMN change_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_agree ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user_action_log ALTER COLUMN action_log_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_user ALTER COLUMN user_id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.op_user_user_id_seq;
DROP SEQUENCE IF EXISTS public.op_user_sns_sns_user_id_seq;
DROP TABLE IF EXISTS public.op_user_sns;
DROP TABLE IF EXISTS public.op_user_sleep;
DROP SEQUENCE IF EXISTS public.op_user_role_request_request_id_seq;
DROP TABLE IF EXISTS public.op_user_role_request;
DROP TABLE IF EXISTS public.op_user_role;
DROP SEQUENCE IF EXISTS public.op_user_parent_parent_id_seq;
DROP TABLE IF EXISTS public.op_user_parent;
DROP SEQUENCE IF EXISTS public.op_user_login_log_login_log_id_seq;
DROP SEQUENCE IF EXISTS public.op_user_login_log_id_seq;
DROP TABLE IF EXISTS public.op_user_login_log;
DROP TABLE IF EXISTS public.op_user_login;
DROP TABLE IF EXISTS public.op_user_level_log;
DROP TABLE IF EXISTS public.op_user_level;
DROP TABLE IF EXISTS public.op_user_group_log;
DROP TABLE IF EXISTS public.op_user_group;
DROP SEQUENCE IF EXISTS public.op_user_event_event_sn_seq;
DROP TABLE IF EXISTS public.op_user_event;
DROP TABLE IF EXISTS public.op_user_detail;
DROP SEQUENCE IF EXISTS public.op_user_delivery_user_delivery_id_seq;
DROP TABLE IF EXISTS public.op_user_delivery;
DROP TABLE IF EXISTS public.op_user_ci;
DROP SEQUENCE IF EXISTS public.op_user_change_log_change_log_id_seq;
DROP TABLE IF EXISTS public.op_user_change_log;
DROP TABLE IF EXISTS public.op_user_birthday_temp;
DROP TABLE IF EXISTS public.op_user_birthday;
DROP TABLE IF EXISTS public.op_user_auth;
DROP SEQUENCE IF EXISTS public.op_user_agree_id_seq;
DROP TABLE IF EXISTS public.op_user_agree;
DROP SEQUENCE IF EXISTS public.op_user_action_log_action_log_id_seq;
DROP TABLE IF EXISTS public.op_user_action_log;
DROP TABLE IF EXISTS public.op_user;
DROP TABLE IF EXISTS public.op_sns_user_detail;
DROP TABLE IF EXISTS public.op_sns_user;
DROP TABLE IF EXISTS public.op_session_attributes;
DROP TABLE IF EXISTS public.op_session;
DROP TABLE IF EXISTS public.op_role_hierarchy;
DROP TABLE IF EXISTS public.op_join_config;
DROP TABLE IF EXISTS public.op_group;
DROP TABLE IF EXISTS public.op_customer;
DROP TABLE IF EXISTS public.op_common_code;
DROP TABLE IF EXISTS public.g_naver_auth_login_info_mng;
DROP TABLE IF EXISTS public.g_mber_secsn;
DROP TABLE IF EXISTS public.g_indvdlinfo_readng_hist;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: g_indvdlinfo_readng_hist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_indvdlinfo_readng_hist (
    readng_sn bigint NOT NULL,
    user_id bigint NOT NULL,
    readng_dt timestamp without time zone NOT NULL,
    trget_user_id bigint NOT NULL
);


--
-- Name: g_mber_secsn; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_mber_secsn (
    secsn_year character varying(4) NOT NULL,
    user_id bigint NOT NULL,
    mber_ci character varying(200) NOT NULL,
    cntr_amt integer,
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_naver_auth_login_info_mng; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_naver_auth_login_info_mng (
    auth_temp_id character varying(50) NOT NULL,
    access_token character varying(10000) NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
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
-- Name: op_customer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_customer (
    customer_code character varying(50) NOT NULL,
    customer_name character varying(255),
    user_id bigint,
    customer_type character varying(50),
    customer_group character varying(50),
    business_number character varying(50),
    tel_number character varying(50),
    boss_name character varying(255),
    category character varying(255),
    event character varying(255),
    zipcode character varying(20),
    address text,
    address_detail character varying(255),
    memo text,
    staff_name character varying(255),
    staff_department character varying(255),
    staff_tel_number character varying(50),
    staff_phone_number character varying(50),
    bank_number character varying(50),
    bank_name character varying(255),
    bank_in_name character varying(255),
    bank_cms_code character varying(50),
    customer_staff_name character varying(255),
    customer_staff_position character varying(255),
    customer_staff_tel_number character varying(50),
    customer_staff_phone_number character varying(50),
    customer_staff_email character varying(255),
    dm_zipcode character varying(20),
    dm_address text,
    dm_address_detail character varying(255),
    business_number_code character varying(50),
    fax_group character varying(50),
    fax_number character varying(50),
    homepage character varying(255),
    create_date character varying(20),
    update_date character varying(20)
);


--
-- Name: op_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_group (
    group_code character varying(10) NOT NULL,
    group_name character varying(30),
    group_explanation character varying(50),
    created_date character varying(30),
    created_user_id character varying(30),
    updated_date character varying(30),
    updated_user_id character varying(30)
);


--
-- Name: op_join_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_join_config (
    shop_config_id bigint NOT NULL,
    nickname character varying(1),
    company_name character varying(1),
    company_name_katakana character varying(1),
    postion character varying(1),
    user_name character varying(1),
    user_name_katakana character varying(1),
    business character varying(1),
    email character varying(1),
    address character varying(1),
    tel character varying(1),
    fax character varying(1),
    phone character varying(1),
    receive_mail character varying(1),
    catalog_app character varying(1),
    sex character varying(1),
    age character varying(1),
    birthday character varying(14),
    business_number character varying(50),
    created_date character varying(14) DEFAULT '00000000000000'::character varying NOT NULL,
    updated_date character varying(14) DEFAULT '00000000000000'::character varying NOT NULL,
    receive_sms character varying(1)
);


--
-- Name: op_role_hierarchy; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_role_hierarchy (
    authority character varying(20) NOT NULL,
    parent_authority character varying(20) NOT NULL
);


--
-- Name: op_session; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_session (
    primary_id character(36) NOT NULL,
    session_id character(36) NOT NULL,
    creation_time bigint NOT NULL,
    last_access_time bigint NOT NULL,
    max_inactive_interval integer NOT NULL,
    expiry_time bigint NOT NULL,
    principal_name character varying(100)
);


--
-- Name: op_session_attributes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_session_attributes (
    session_primary_id character(36) NOT NULL,
    attribute_name character varying(200) NOT NULL,
    attribute_bytes bytea
);


--
-- Name: op_sns_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_sns_user (
    sns_user_id bigint NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    certified_date character varying(14)
);


--
-- Name: op_sns_user_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_sns_user_detail (
    sns_detail_id bigint NOT NULL,
    sns_user_id bigint NOT NULL,
    sns_id character varying(20) NOT NULL,
    sns_type character varying(20) NOT NULL,
    sns_name character varying(350) NOT NULL,
    email character varying(350),
    created_order integer DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user (
    user_id bigint NOT NULL,
    login_id character varying(300) NOT NULL,
    password character varying(300),
    user_name character varying(300),
    email character varying(300),
    status_code character varying(20),
    login_count bigint,
    sleep_mail_send_date character varying(14),
    login_date character varying(14),
    deny_date character varying(14),
    leave_date character varying(14),
    login_fail_count integer DEFAULT 0 NOT NULL,
    login_try_date character varying(14),
    password_type character varying(1) DEFAULT 'N'::character varying NOT NULL,
    password_expired_date character varying(8) DEFAULT '20240101'::character varying,
    updated_date character varying(14),
    created_date character varying(14),
    locgov_code character varying(10),
    mber_ci character varying(200),
    mber_di character varying(200),
    mber_dn character varying(200),
    mber_fin_dn character varying(200),
    sbscrb_se_code character varying(10),
    login_path_code character varying(10),
    user_key character varying(30),
    kakao_user_key character varying(30),
    foreign_status_code character varying(2) DEFAULT '0'::character varying NOT NULL,
    naver_user_key character varying(100),
    create_date character varying(255)
);


--
-- Name: op_user_action_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_action_log (
    action_log_id integer NOT NULL,
    created_date character varying(14) NOT NULL,
    remote_addr character varying(300),
    request_uri character varying(500),
    request_method character varying(5),
    login_id character varying(300) NOT NULL
);


--
-- Name: op_user_action_log_action_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_action_log_action_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_action_log_action_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_action_log_action_log_id_seq OWNED BY public.op_user_action_log.action_log_id;


--
-- Name: op_user_agree; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_agree (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    agree character varying(1),
    login_id character varying(255),
    policy_id integer,
    policy_type character varying(1),
    title character varying(255),
    user_id bigint
);


--
-- Name: op_user_agree_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_agree_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_agree_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_agree_id_seq OWNED BY public.op_user_agree.id;


--
-- Name: op_user_auth; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_auth (
    app_key character varying(50) NOT NULL,
    service_type character varying(50) NOT NULL,
    service_mode character varying(10) NOT NULL,
    service_target character varying(50) NOT NULL,
    user_ip character varying(50) NOT NULL,
    auth_key character varying(80),
    auth_name character varying(20),
    auth_sex character varying(1),
    auth_birth_day character varying(8),
    data_status_code character varying(1) DEFAULT '0'::character varying NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_user_birthday; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_birthday (
    user_id bigint DEFAULT 0 NOT NULL,
    birthday character varying(300),
    created_date character varying(14),
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: op_user_birthday_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_birthday_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    birthday character varying(300),
    created_date character varying(14),
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: op_user_change_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_change_log (
    change_log_id integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL,
    parameter character varying(2000),
    remote_addr character varying(100),
    manager_id bigint DEFAULT 0 NOT NULL
);


--
-- Name: op_user_change_log_change_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_change_log_change_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_change_log_change_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_change_log_change_log_id_seq OWNED BY public.op_user_change_log.change_log_id;


--
-- Name: op_user_ci; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_ci (
    user_id bigint NOT NULL,
    mber_ci character varying(200)
);


--
-- Name: op_user_delivery; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_delivery (
    user_delivery_id integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    default_flag character varying(1) DEFAULT 'N'::character varying,
    title character varying(50),
    user_name character varying(350),
    phone character varying(210),
    mobile character varying(210),
    new_zipcode character varying(5),
    zipcode character varying(10),
    sido character varying(20),
    sigungu character varying(20),
    eupmyeondong character varying(20),
    address character varying(1000),
    address_detail character varying(1785),
    created_date character varying(14)
);


--
-- Name: op_user_delivery_user_delivery_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_delivery_user_delivery_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_delivery_user_delivery_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_delivery_user_delivery_id_seq OWNED BY public.op_user_delivery.user_delivery_id;


--
-- Name: op_user_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_detail (
    user_id bigint DEFAULT 0 NOT NULL,
    group_code character varying(10),
    level_id integer NOT NULL,
    user_level_expiration_date character varying(8),
    new_post character varying(50),
    post character varying(300),
    address character varying(300),
    address_detail character varying(300),
    tel_number character varying(140),
    phone_number character varying(300),
    fax_number character varying(20),
    receive_email character varying(1),
    receive_sms character varying(1),
    receive_push character varying(1),
    gender character varying(300),
    age character varying(3) DEFAULT '0'::character varying,
    point integer DEFAULT 0,
    buy_count integer DEFAULT 0,
    buy_price integer DEFAULT 0,
    last_buy_date character varying(14),
    leave_reason character varying(255),
    site_flag character varying(100),
    use_flag character varying(1) DEFAULT 'Y'::character varying,
    birthday_type character varying(1),
    birthday character varying(300),
    leave_code character varying(10),
    leave_user_id bigint,
    receive_pbanc character varying(1),
    receive_kakao character varying(1)
);


--
-- Name: op_user_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_event (
    event_sn integer NOT NULL,
    user_id bigint NOT NULL,
    user_name character varying(300),
    phone_number character varying(300),
    event_name character varying(300),
    create_date timestamp without time zone DEFAULT now()
);


--
-- Name: op_user_event_event_sn_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_event_event_sn_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_event_event_sn_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_event_event_sn_seq OWNED BY public.op_user_event.event_sn;


--
-- Name: op_user_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_group (
    user_id bigint DEFAULT 0 NOT NULL,
    group_id integer NOT NULL
);


--
-- Name: op_user_group_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_group_log (
    user_id bigint DEFAULT 0 NOT NULL,
    group_code character varying(10),
    group_name character varying(10),
    admin_user_name character varying(50),
    created_date character varying(14)
);


--
-- Name: op_user_level; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_level (
    level_id integer DEFAULT 0 NOT NULL,
    group_code character varying(10) DEFAULT 'default'::character varying NOT NULL,
    depth integer NOT NULL,
    level_name character varying(20) NOT NULL,
    file_name character varying(255),
    price_start integer DEFAULT 0 NOT NULL,
    price_end integer DEFAULT 0 NOT NULL,
    discount_rate double precision DEFAULT 0.000000 NOT NULL,
    point_rate double precision DEFAULT 0.000000 NOT NULL,
    shipping_coupon_count integer DEFAULT 0 NOT NULL,
    retention_period integer DEFAULT 0 NOT NULL,
    reference_period integer DEFAULT 0 NOT NULL,
    except_reference_period integer DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_user_level_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_level_log (
    user_id bigint DEFAULT 0 NOT NULL,
    group_code character varying(10),
    level_id integer,
    level_name character varying(20),
    admin_user_name character varying(50),
    created_date character varying(14)
);


--
-- Name: op_user_login; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_login (
    id bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    session_id character varying(1000) NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: op_user_login_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_login_log (
    login_log_id integer NOT NULL,
    login_type character varying(100) NOT NULL,
    login_id character varying(300) NOT NULL,
    success_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    remote_addr character varying(300),
    memo character varying(200),
    login_date character varying(14) NOT NULL
);


--
-- Name: op_user_login_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_login_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_login_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_login_log_id_seq OWNED BY public.op_user_login_log.login_log_id;


--
-- Name: op_user_login_log_login_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_login_log_login_log_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_login_log_login_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_login_log_login_log_id_seq OWNED BY public.op_user_login_log.login_log_id;


--
-- Name: op_user_parent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_parent (
    parent_id bigint NOT NULL,
    user_id bigint,
    user_name character varying(300),
    gender character varying(300),
    national_info character varying(300),
    ci character varying(300),
    di character varying(300),
    dn character varying(300),
    cell_corp character varying(300),
    cell_no character varying(300),
    created_at timestamp without time zone
);


--
-- Name: op_user_parent_parent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_parent_parent_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_parent_parent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_parent_parent_id_seq OWNED BY public.op_user_parent.parent_id;


--
-- Name: op_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_role (
    user_id bigint DEFAULT 0 NOT NULL,
    authority character varying(50) NOT NULL
);


--
-- Name: op_user_role_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_role_request (
    request_id bigint NOT NULL,
    user_id bigint NOT NULL,
    requested_role character varying(50) NOT NULL,
    reason character varying(255),
    status character varying(20) NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    processed_date timestamp without time zone
);


--
-- Name: op_user_role_request_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_role_request_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_role_request_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_role_request_request_id_seq OWNED BY public.op_user_role_request.request_id;


--
-- Name: op_user_sleep; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_sleep (
    user_id bigint DEFAULT 0 NOT NULL,
    user_name character varying(500),
    email character varying(500),
    new_post character varying(500),
    post character varying(500),
    address character varying(500),
    address_detail character varying(500),
    tel_number character varying(500),
    phone_number character varying(500),
    fax_number character varying(500),
    birthday_type character varying(500),
    birthday character varying(500),
    created_date character varying(14)
);


--
-- Name: op_user_sns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_user_sns (
    sns_user_id bigint NOT NULL,
    sns_id character varying(50) NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    sns_type character varying(20) NOT NULL,
    sns_name character varying(350),
    email character varying(420),
    created_order integer DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL,
    certified_date character varying(14)
);


--
-- Name: op_user_sns_sns_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_sns_sns_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_sns_sns_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_sns_sns_user_id_seq OWNED BY public.op_user_sns.sns_user_id;


--
-- Name: op_user_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_user_user_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_user_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_user_user_id_seq OWNED BY public.op_user.user_id;


--
-- Name: op_user user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user ALTER COLUMN user_id SET DEFAULT nextval('public.op_user_user_id_seq'::regclass);


--
-- Name: op_user_action_log action_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_action_log ALTER COLUMN action_log_id SET DEFAULT nextval('public.op_user_action_log_action_log_id_seq'::regclass);


--
-- Name: op_user_agree id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_agree ALTER COLUMN id SET DEFAULT nextval('public.op_user_agree_id_seq'::regclass);


--
-- Name: op_user_change_log change_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_change_log ALTER COLUMN change_log_id SET DEFAULT nextval('public.op_user_change_log_change_log_id_seq'::regclass);


--
-- Name: op_user_delivery user_delivery_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_delivery ALTER COLUMN user_delivery_id SET DEFAULT nextval('public.op_user_delivery_user_delivery_id_seq'::regclass);


--
-- Name: op_user_event event_sn; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_event ALTER COLUMN event_sn SET DEFAULT nextval('public.op_user_event_event_sn_seq'::regclass);


--
-- Name: op_user_login_log login_log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_login_log ALTER COLUMN login_log_id SET DEFAULT nextval('public.op_user_login_log_id_seq'::regclass);


--
-- Name: op_user_parent parent_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_parent ALTER COLUMN parent_id SET DEFAULT nextval('public.op_user_parent_parent_id_seq'::regclass);


--
-- Name: op_user_role_request request_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_role_request ALTER COLUMN request_id SET DEFAULT nextval('public.op_user_role_request_request_id_seq'::regclass);


--
-- Name: op_user_sns sns_user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_sns ALTER COLUMN sns_user_id SET DEFAULT nextval('public.op_user_sns_sns_user_id_seq'::regclass);


--
-- Data for Name: g_indvdlinfo_readng_hist; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_indvdlinfo_readng_hist (readng_sn, user_id, readng_dt, trget_user_id) FROM stdin;
\.


--
-- Data for Name: g_mber_secsn; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_mber_secsn (secsn_year, user_id, mber_ci, cntr_amt, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_naver_auth_login_info_mng; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_naver_auth_login_info_mng (auth_temp_id, access_token, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: op_common_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code) FROM stdin;
USER_STATUS	ko	ACTIVE	정상	\N	1	Y	\N	\N	\N	\N
USER_STATUS	ko	DORMANT	휴면	\N	2	Y	\N	\N	\N	\N
USER_STATUS	ko	LOCKED	잠금	\N	3	Y	\N	\N	\N	\N
USER_STATUS	ko	WITHDRAWN	탈퇴	\N	4	Y	\N	\N	\N	\N
USER_TYPE	ko	GENERAL	일반회원	\N	1	Y	\N	\N	\N	\N
USER_TYPE	ko	OVERSEAS	재외국민	\N	2	Y	\N	\N	\N	\N
USER_TYPE	ko	LOCALGOV	지자체 담당자	\N	3	Y	\N	\N	\N	\N
USER_TYPE	ko	PROVIDER	답례품 제공자	\N	4	Y	\N	\N	\N	\N
USER_TYPE	ko	ADMIN	시스템운영자	\N	5	Y	\N	\N	\N	\N
LOGIN_PATH	ko	EMAIL	이메일	\N	1	Y	\N	\N	\N	\N
LOGIN_PATH	ko	KAKAO	카카오	\N	2	Y	\N	\N	\N	\N
LOGIN_PATH	ko	NAVER	네이버	\N	3	Y	\N	\N	\N	\N
ROLE	ko	ROLE_USER	일반 사용자	\N	1	Y	\N	\N	\N	\N
ROLE	ko	ROLE_LOCALGOV	지자체 담당자	\N	2	Y	\N	\N	\N	\N
ROLE	ko	ROLE_PROVIDER	답례품 제공자	\N	3	Y	\N	\N	\N	\N
ROLE	ko	ROLE_ADMIN	시스템 운영자	\N	4	Y	\N	\N	\N	\N
LOGIN_PATH	ko	ONEPASS	디지털원패스	\N	4	Y	\N	\N	\N	\N
ROLE_REQUEST_STATUS	ko	REQUESTED	심사중	\N	1	Y	\N	\N	\N	\N
ROLE_REQUEST_STATUS	ko	APPROVED	승인	\N	2	Y	\N	\N	\N	\N
ROLE_REQUEST_STATUS	ko	REJECTED	반려	\N	3	Y	\N	\N	\N	\N
SYSTEM_CONFIG	ko	DORMANT_INACTIVE_DAYS	휴면 전환 기준(미접속 일수)	\N	1	Y	\N	365	\N	\N
LEAVE_CODE	ko	001	탈퇴 후 재가입을 위해서	\N	1	Y	\N	\N	\N	\N
LEAVE_CODE	ko	002	사고 싶은 상품이 없어서	\N	2	Y	\N	\N	\N	\N
LEAVE_CODE	ko	003	자주 이용하지 않아서	\N	3	Y	\N	\N	\N	\N
LEAVE_CODE	ko	004	서비스 및 고객지원이 만족스럽지 않아서	\N	4	Y	\N	\N	\N	\N
LEAVE_CODE	ko	005	광고성 알림이 너무 많이 와서	\N	5	Y	\N	\N	\N	\N
LOGIN_PATH	ko	100	ID/PWD	\N	1	Y	\N	\N	\N	\N
LOGIN_PATH	ko	200	간편인증	\N	2	Y	\N	\N	\N	\N
LOGIN_PATH	ko	300	디지털원패스	\N	3	Y	\N	\N	\N	\N
LOGIN_PATH	ko	400	PKI	\N	4	Y	\N	\N	\N	\N
LOGIN_PATH	ko	500	카카오인증	\N	5	Y	\N	\N	\N	\N
LOGIN_PATH	ko	600	네이버인증	\N	6	Y	\N	\N	\N	\N
LOGIN_PATH	ko	700	민간개방API	\N	7	Y	\N	\N	\N	\N
\.


--
-- Data for Name: op_customer; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_customer (customer_code, customer_name, user_id, customer_type, customer_group, business_number, tel_number, boss_name, category, event, zipcode, address, address_detail, memo, staff_name, staff_department, staff_tel_number, staff_phone_number, bank_number, bank_name, bank_in_name, bank_cms_code, customer_staff_name, customer_staff_position, customer_staff_tel_number, customer_staff_phone_number, customer_staff_email, dm_zipcode, dm_address, dm_address_detail, business_number_code, fax_group, fax_number, homepage, create_date, update_date) FROM stdin;
\.


--
-- Data for Name: op_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_group (group_code, group_name, group_explanation, created_date, created_user_id, updated_date, updated_user_id) FROM stdin;
\.


--
-- Data for Name: op_join_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_join_config (shop_config_id, nickname, company_name, company_name_katakana, postion, user_name, user_name_katakana, business, email, address, tel, fax, phone, receive_mail, catalog_app, sex, age, birthday, business_number, created_date, updated_date, receive_sms) FROM stdin;
\.


--
-- Data for Name: op_role_hierarchy; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_role_hierarchy (authority, parent_authority) FROM stdin;
\.


--
-- Data for Name: op_session; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_session (primary_id, session_id, creation_time, last_access_time, max_inactive_interval, expiry_time, principal_name) FROM stdin;
\.


--
-- Data for Name: op_session_attributes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_session_attributes (session_primary_id, attribute_name, attribute_bytes) FROM stdin;
\.


--
-- Data for Name: op_sns_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_sns_user (sns_user_id, user_id, certified_date) FROM stdin;
\.


--
-- Data for Name: op_sns_user_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_sns_user_detail (sns_detail_id, sns_user_id, sns_id, sns_type, sns_name, email, created_order, created_date) FROM stdin;
\.


--
-- Data for Name: op_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user (user_id, login_id, password, user_name, email, status_code, login_count, sleep_mail_send_date, login_date, deny_date, leave_date, login_fail_count, login_try_date, password_type, password_expired_date, updated_date, created_date, locgov_code, mber_ci, mber_di, mber_dn, mber_fin_dn, sbscrb_se_code, login_path_code, user_key, kakao_user_key, foreign_status_code, naver_user_key, create_date) FROM stdin;
1001	testuser02	$2a$10$yOXnj2HdziAgZa6rm8WHruQPZgeW4CGRZ23AVuRAptxkt5FWyUGKO	테스트유저2	testuser02@example.com	ACTIVE	0	\N	\N	\N	\N	0	\N	N	20240101	20260824173534	20260824173534	\N	\N	\N	\N	\N	GENERAL	100	\N	\N	0	\N	\N
1002	testuser03	$2a$10$jcJDWhNjeSMVs./0OUI5UuxH70rQApqMMF3gjVV9k5ltFscenvbOS	테스트유저3	testuser03@example.com	ACTIVE	0	\N	\N	\N	\N	0	\N	N	20240101	20260824173535	20260824173535	\N	\N	\N	\N	\N	GENERAL	100	\N	\N	0	\N	\N
1000	testuser01	$2a$10$Brz49yWUGq.d4gMBseVKLe.AEL1l7rJJrFetrOniq4z.jqjci1hGS	테스트유저1	testuser01@example.com	ACTIVE	5	\N	20260825144336	\N	\N	0	20260825141116	N	20240101	20260825144237	20260824173534	\N	\N	\N	\N	\N	GENERAL	100	\N	\N	0	\N	\N
\.


--
-- Data for Name: op_user_action_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_action_log (action_log_id, created_date, remote_addr, request_uri, request_method, login_id) FROM stdin;
1000	20260825084954	0:0:0:0:0:0:0:1	/profile	POST	testuser01
\.


--
-- Data for Name: op_user_agree; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_agree (id, created, created_by, updated, updated_by, agree, login_id, policy_id, policy_type, title, user_id) FROM stdin;
\.


--
-- Data for Name: op_user_auth; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_auth (app_key, service_type, service_mode, service_target, user_ip, auth_key, auth_name, auth_sex, auth_birth_day, data_status_code, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_birthday; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_birthday (user_id, birthday, created_date, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: op_user_birthday_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_birthday_temp (user_id, birthday, created_date, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: op_user_change_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_change_log (change_log_id, user_id, created_date, parameter, remote_addr, manager_id) FROM stdin;
1000	1000	20260825084954	PROFILE_UPDATE	0:0:0:0:0:0:0:1	1000
1001	1000	20260825144237	PASSWORD_RESET	\N	1000
\.


--
-- Data for Name: op_user_ci; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_ci (user_id, mber_ci) FROM stdin;
\.


--
-- Data for Name: op_user_delivery; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_delivery (user_delivery_id, user_id, default_flag, title, user_name, phone, mobile, new_zipcode, zipcode, sido, sigungu, eupmyeondong, address, address_detail, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_detail (user_id, group_code, level_id, user_level_expiration_date, new_post, post, address, address_detail, tel_number, phone_number, fax_number, receive_email, receive_sms, receive_push, gender, age, point, buy_count, buy_price, last_buy_date, leave_reason, site_flag, use_flag, birthday_type, birthday, leave_code, leave_user_id, receive_pbanc, receive_kakao) FROM stdin;
1001	\N	1	\N	\N	\N	\N	\N	\N	010-0000-0002	\N	\N	\N	\N	\N	0	0	0	0	\N	\N	\N	Y	\N	19820101	\N	\N	\N	\N
1002	\N	1	\N	\N	\N	\N	\N	\N	010-0000-0003	\N	\N	\N	\N	\N	0	0	0	0	\N	\N	\N	Y	\N	19830101	\N	\N	\N	\N
1000	\N	1	\N	\N		�����		\N	010-1234-5678	\N	Y	N	\N	\N	0	0	0	0	\N	\N	\N	Y	\N	19810101	\N	\N	\N	N
\.


--
-- Data for Name: op_user_event; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_event (event_sn, user_id, user_name, phone_number, event_name, create_date) FROM stdin;
\.


--
-- Data for Name: op_user_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_group (user_id, group_id) FROM stdin;
\.


--
-- Data for Name: op_user_group_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_group_log (user_id, group_code, group_name, admin_user_name, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_level; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_level (level_id, group_code, depth, level_name, file_name, price_start, price_end, discount_rate, point_rate, shipping_coupon_count, retention_period, reference_period, except_reference_period, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_level_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_level_log (user_id, group_code, level_id, level_name, admin_user_name, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_login; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_login (id, created_at, updated_at, session_id, user_id) FROM stdin;
\.


--
-- Data for Name: op_user_login_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_login_log (login_log_id, login_type, login_id, success_flag, remote_addr, memo, login_date) FROM stdin;
2	EMAIL	testuser01	Y	0:0:0:0:0:0:0:1	\N	20260824173720
3	EMAIL	testuser01	N	0:0:0:0:0:0:0:1	비밀번호 불일치	20260824173721
4	EMAIL	testuser01	Y	0:0:0:0:0:0:0:1	\N	20260825084953
5	100	testuser01	N	0:0:0:0:0:0:0:1	비밀번호 불일치	20260825141115
6	100	testuser01	N	0:0:0:0:0:0:0:1	비밀번호 불일치	20260825141115
7	100	testuser01	N	0:0:0:0:0:0:0:1	비밀번호 불일치	20260825141115
8	100	testuser01	N	0:0:0:0:0:0:0:1	비밀번호 불일치	20260825141116
9	100	testuser01	Y	0:0:0:0:0:0:0:1	\N	20260825144248
10	100	testuser01	Y	0:0:0:0:0:0:0:1	\N	20260825144259
11	100	testuser01	Y	0:0:0:0:0:0:0:1	\N	20260825144336
\.


--
-- Data for Name: op_user_parent; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_parent (parent_id, user_id, user_name, gender, national_info, ci, di, dn, cell_corp, cell_no, created_at) FROM stdin;
\.


--
-- Data for Name: op_user_role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_role (user_id, authority) FROM stdin;
1000	ROLE_USER
1001	ROLE_USER
1002	ROLE_USER
\.


--
-- Data for Name: op_user_role_request; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_role_request (request_id, user_id, requested_role, reason, status, created_date, processed_date) FROM stdin;
\.


--
-- Data for Name: op_user_sleep; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_sleep (user_id, user_name, email, new_post, post, address, address_detail, tel_number, phone_number, fax_number, birthday_type, birthday, created_date) FROM stdin;
\.


--
-- Data for Name: op_user_sns; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_user_sns (sns_user_id, sns_id, user_id, sns_type, sns_name, email, created_order, created_date, certified_date) FROM stdin;
\.


--
-- Name: op_user_action_log_action_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_action_log_action_log_id_seq', 1000, true);


--
-- Name: op_user_agree_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_agree_id_seq', 1000, false);


--
-- Name: op_user_change_log_change_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_change_log_change_log_id_seq', 1001, true);


--
-- Name: op_user_delivery_user_delivery_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_delivery_user_delivery_id_seq', 1000, false);


--
-- Name: op_user_event_event_sn_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_event_event_sn_seq', 1000, false);


--
-- Name: op_user_login_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_login_log_id_seq', 11, true);


--
-- Name: op_user_login_log_login_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_login_log_login_log_id_seq', 1000, false);


--
-- Name: op_user_parent_parent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_parent_parent_id_seq', 1000, false);


--
-- Name: op_user_role_request_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_role_request_request_id_seq', 1, false);


--
-- Name: op_user_sns_sns_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_sns_sns_user_id_seq', 1, false);


--
-- Name: op_user_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_user_user_id_seq', 1011, true);


--
-- Name: g_indvdlinfo_readng_hist g_indvdlinfo_readng_hist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_indvdlinfo_readng_hist
    ADD CONSTRAINT g_indvdlinfo_readng_hist_pkey PRIMARY KEY (readng_sn);


--
-- Name: g_mber_secsn g_mber_secsn_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_mber_secsn
    ADD CONSTRAINT g_mber_secsn_pkey PRIMARY KEY (secsn_year, user_id, mber_ci);


--
-- Name: g_naver_auth_login_info_mng g_naver_auth_login_info_mng_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_naver_auth_login_info_mng
    ADD CONSTRAINT g_naver_auth_login_info_mng_pkey PRIMARY KEY (access_token);


--
-- Name: op_common_code op_common_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_common_code
    ADD CONSTRAINT op_common_code_pkey PRIMARY KEY (code_type, code_language, id);


--
-- Name: op_customer op_customer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_customer
    ADD CONSTRAINT op_customer_pkey PRIMARY KEY (customer_code);


--
-- Name: op_group op_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_group
    ADD CONSTRAINT op_group_pkey PRIMARY KEY (group_code);


--
-- Name: op_role_hierarchy op_role_hierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_role_hierarchy
    ADD CONSTRAINT op_role_hierarchy_pkey PRIMARY KEY (authority);


--
-- Name: op_session_attributes op_session_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_session_attributes
    ADD CONSTRAINT op_session_attributes_pkey PRIMARY KEY (session_primary_id, attribute_name);


--
-- Name: op_session op_session_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_session
    ADD CONSTRAINT op_session_pkey PRIMARY KEY (primary_id);


--
-- Name: op_sns_user_detail op_sns_user_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_sns_user_detail
    ADD CONSTRAINT op_sns_user_detail_pkey PRIMARY KEY (sns_detail_id);


--
-- Name: op_sns_user op_sns_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_sns_user
    ADD CONSTRAINT op_sns_user_pkey PRIMARY KEY (sns_user_id);


--
-- Name: op_user_action_log op_user_action_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_action_log
    ADD CONSTRAINT op_user_action_log_pkey PRIMARY KEY (action_log_id);


--
-- Name: op_user_agree op_user_agree_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_agree
    ADD CONSTRAINT op_user_agree_pkey PRIMARY KEY (id);


--
-- Name: op_user_auth op_user_auth_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_auth
    ADD CONSTRAINT op_user_auth_pkey PRIMARY KEY (app_key, service_type);


--
-- Name: op_user_birthday op_user_birthday_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_birthday
    ADD CONSTRAINT op_user_birthday_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_birthday_temp op_user_birthday_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_birthday_temp
    ADD CONSTRAINT op_user_birthday_temp_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_change_log op_user_change_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_change_log
    ADD CONSTRAINT op_user_change_log_pkey PRIMARY KEY (change_log_id);


--
-- Name: op_user_ci op_user_ci_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_ci
    ADD CONSTRAINT op_user_ci_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_delivery op_user_delivery_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_delivery
    ADD CONSTRAINT op_user_delivery_pkey PRIMARY KEY (user_delivery_id);


--
-- Name: op_user_detail op_user_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_detail
    ADD CONSTRAINT op_user_detail_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_event op_user_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_event
    ADD CONSTRAINT op_user_event_pkey PRIMARY KEY (event_sn);


--
-- Name: op_user_group op_user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_group
    ADD CONSTRAINT op_user_group_pkey PRIMARY KEY (user_id, group_id);


--
-- Name: op_user_level op_user_level_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_level
    ADD CONSTRAINT op_user_level_pkey PRIMARY KEY (level_id);


--
-- Name: op_user_login_log op_user_login_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_login_log
    ADD CONSTRAINT op_user_login_log_pkey PRIMARY KEY (login_log_id);


--
-- Name: op_user_login op_user_login_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_login
    ADD CONSTRAINT op_user_login_pkey PRIMARY KEY (id);


--
-- Name: op_user_parent op_user_parent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_parent
    ADD CONSTRAINT op_user_parent_pkey PRIMARY KEY (parent_id);


--
-- Name: op_user op_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user
    ADD CONSTRAINT op_user_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_role op_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_role
    ADD CONSTRAINT op_user_role_pkey PRIMARY KEY (user_id, authority);


--
-- Name: op_user_role_request op_user_role_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_role_request
    ADD CONSTRAINT op_user_role_request_pkey PRIMARY KEY (request_id);


--
-- Name: op_user_sleep op_user_sleep_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_sleep
    ADD CONSTRAINT op_user_sleep_pkey PRIMARY KEY (user_id);


--
-- Name: op_user_sns op_user_sns_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_sns
    ADD CONSTRAINT op_user_sns_pkey PRIMARY KEY (sns_user_id);


--
-- Name: op_session_attributes fk_op_session_attributes_session_primary_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_session_attributes
    ADD CONSTRAINT fk_op_session_attributes_session_primary_id FOREIGN KEY (session_primary_id) REFERENCES public.op_session(primary_id);


--
-- Name: op_sns_user_detail fk_op_sns_user_detail_sns_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_sns_user_detail
    ADD CONSTRAINT fk_op_sns_user_detail_sns_user_id FOREIGN KEY (sns_user_id) REFERENCES public.op_sns_user(sns_user_id);


--
-- Name: op_sns_user fk_op_sns_user_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_sns_user
    ADD CONSTRAINT fk_op_sns_user_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: op_user_birthday fk_op_user_birthday_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_birthday
    ADD CONSTRAINT fk_op_user_birthday_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: op_user_delivery fk_op_user_delivery_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_delivery
    ADD CONSTRAINT fk_op_user_delivery_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: op_user_detail fk_op_user_detail_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_detail
    ADD CONSTRAINT fk_op_user_detail_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: op_user_group fk_op_user_group_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_group
    ADD CONSTRAINT fk_op_user_group_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: op_user_parent fk_op_user_parent_user_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_user_parent
    ADD CONSTRAINT fk_op_user_parent_user_id FOREIGN KEY (user_id) REFERENCES public.op_user(user_id);


--
-- Name: TABLE g_indvdlinfo_readng_hist; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_indvdlinfo_readng_hist TO member;


--
-- Name: TABLE g_mber_secsn; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_mber_secsn TO member;


--
-- Name: TABLE g_naver_auth_login_info_mng; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_naver_auth_login_info_mng TO member;


--
-- Name: TABLE op_common_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_common_code TO member;


--
-- Name: TABLE op_customer; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_customer TO member;


--
-- Name: TABLE op_group; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_group TO member;


--
-- Name: TABLE op_join_config; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_join_config TO member;


--
-- Name: TABLE op_role_hierarchy; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_role_hierarchy TO member;


--
-- Name: TABLE op_session; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_session TO member;


--
-- Name: TABLE op_session_attributes; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_session_attributes TO member;


--
-- Name: TABLE op_sns_user; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_sns_user TO member;


--
-- Name: TABLE op_sns_user_detail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_sns_user_detail TO member;


--
-- Name: TABLE op_user; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user TO member;


--
-- Name: TABLE op_user_action_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_action_log TO member;


--
-- Name: SEQUENCE op_user_action_log_action_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_action_log_action_log_id_seq TO member;


--
-- Name: TABLE op_user_agree; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_agree TO member;


--
-- Name: SEQUENCE op_user_agree_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_agree_id_seq TO member;


--
-- Name: TABLE op_user_auth; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_auth TO member;


--
-- Name: TABLE op_user_birthday; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_birthday TO member;


--
-- Name: TABLE op_user_birthday_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_birthday_temp TO member;


--
-- Name: TABLE op_user_change_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_change_log TO member;


--
-- Name: SEQUENCE op_user_change_log_change_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_change_log_change_log_id_seq TO member;


--
-- Name: TABLE op_user_ci; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_ci TO member;


--
-- Name: TABLE op_user_delivery; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_delivery TO member;


--
-- Name: SEQUENCE op_user_delivery_user_delivery_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_delivery_user_delivery_id_seq TO member;


--
-- Name: TABLE op_user_detail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_detail TO member;


--
-- Name: TABLE op_user_event; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_event TO member;


--
-- Name: SEQUENCE op_user_event_event_sn_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_event_event_sn_seq TO member;


--
-- Name: TABLE op_user_group; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_group TO member;


--
-- Name: TABLE op_user_group_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_group_log TO member;


--
-- Name: TABLE op_user_level; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_level TO member;


--
-- Name: TABLE op_user_level_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_level_log TO member;


--
-- Name: TABLE op_user_login; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_login TO member;


--
-- Name: TABLE op_user_login_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_login_log TO member;


--
-- Name: SEQUENCE op_user_login_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_login_log_id_seq TO member;


--
-- Name: SEQUENCE op_user_login_log_login_log_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_login_log_login_log_id_seq TO member;


--
-- Name: TABLE op_user_parent; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_parent TO member;


--
-- Name: SEQUENCE op_user_parent_parent_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_parent_parent_id_seq TO member;


--
-- Name: TABLE op_user_role; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_role TO member;


--
-- Name: TABLE op_user_role_request; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_role_request TO member;


--
-- Name: SEQUENCE op_user_role_request_request_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_role_request_request_id_seq TO member;


--
-- Name: TABLE op_user_sleep; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_sleep TO member;


--
-- Name: TABLE op_user_sns; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_user_sns TO member;


--
-- Name: SEQUENCE op_user_sns_sns_user_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_sns_sns_user_id_seq TO member;


--
-- Name: SEQUENCE op_user_user_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_user_user_id_seq TO member;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO member;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO member;


--
-- PostgreSQL database dump complete
--

\unrestrict LFzFOu9N3EYKxkOZyI2RiS3EwG4yuktQsQpdBZnHmnOUjxcmTf2dPlpj3gsKiyk

