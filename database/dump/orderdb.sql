--
-- PostgreSQL database dump
--

\restrict 8YYnIu5ITg2eG8r48qITnw4rCKe6ij9ABiUeM3BW5HL4VTFEwhJoudY8APHC8eR

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

ALTER TABLE IF EXISTS ONLY public.op_cashbill_issue DROP CONSTRAINT IF EXISTS fk_op_cashbill_issue_cashbill_id;
ALTER TABLE IF EXISTS ONLY public.op_cart_set DROP CONSTRAINT IF EXISTS fk_op_cart_set_parent_cart_id;
ALTER TABLE IF EXISTS ONLY public.op_shipment_return DROP CONSTRAINT IF EXISTS op_shipment_return_pkey;
ALTER TABLE IF EXISTS ONLY public.op_shipment DROP CONSTRAINT IF EXISTS op_shipment_pkey;
ALTER TABLE IF EXISTS ONLY public.op_remittance DROP CONSTRAINT IF EXISTS op_remittance_pkey;
ALTER TABLE IF EXISTS ONLY public.op_remittance_detail DROP CONSTRAINT IF EXISTS op_remittance_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_temp DROP CONSTRAINT IF EXISTS op_order_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_shipping DROP CONSTRAINT IF EXISTS op_order_shipping_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_shipping_info DROP CONSTRAINT IF EXISTS op_order_shipping_info_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_shipping_cp_buy_temp DROP CONSTRAINT IF EXISTS op_order_shipping_cp_buy_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_shipping_buy_temp DROP CONSTRAINT IF EXISTS op_order_shipping_buy_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_set_return_apply DROP CONSTRAINT IF EXISTS op_order_set_return_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_set_exchange_apply DROP CONSTRAINT IF EXISTS op_order_set_exchange_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_set_cancel_apply DROP CONSTRAINT IF EXISTS op_order_set_cancel_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_return_apply DROP CONSTRAINT IF EXISTS op_order_return_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_refund DROP CONSTRAINT IF EXISTS op_order_refund_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order DROP CONSTRAINT IF EXISTS op_order_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_pg_data DROP CONSTRAINT IF EXISTS op_order_pg_data_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_payment DROP CONSTRAINT IF EXISTS op_order_payment_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_payment_buy_temp DROP CONSTRAINT IF EXISTS op_order_payment_buy_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_log DROP CONSTRAINT IF EXISTS op_order_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_item_temp DROP CONSTRAINT IF EXISTS op_order_item_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_item_set DROP CONSTRAINT IF EXISTS op_order_item_set_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_item_hold DROP CONSTRAINT IF EXISTS op_order_item_hold_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_item_hold_hist DROP CONSTRAINT IF EXISTS op_order_item_hold_hist_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_item_buy_temp DROP CONSTRAINT IF EXISTS op_order_item_buy_temp_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_gift_item DROP CONSTRAINT IF EXISTS op_order_gift_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_gift_item_log DROP CONSTRAINT IF EXISTS op_order_gift_item_log_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_exchange_apply DROP CONSTRAINT IF EXISTS op_order_exchange_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_claim_shipping DROP CONSTRAINT IF EXISTS op_order_claim_shipping_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_cancel_fail DROP CONSTRAINT IF EXISTS op_order_cancel_fail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_cancel_apply DROP CONSTRAINT IF EXISTS op_order_cancel_apply_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_admin DROP CONSTRAINT IF EXISTS op_order_admin_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_admin_detail DROP CONSTRAINT IF EXISTS op_order_admin_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_order_add_payment DROP CONSTRAINT IF EXISTS op_order_add_payment_pkey;
ALTER TABLE IF EXISTS ONLY public.op_delivery_hope DROP CONSTRAINT IF EXISTS op_delivery_hope_pkey;
ALTER TABLE IF EXISTS ONLY public.op_delivery_company DROP CONSTRAINT IF EXISTS op_delivery_company_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon_user DROP CONSTRAINT IF EXISTS op_coupon_user_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon_target_user DROP CONSTRAINT IF EXISTS op_coupon_target_user_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon_target_item DROP CONSTRAINT IF EXISTS op_coupon_target_item_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon_regular DROP CONSTRAINT IF EXISTS op_coupon_regular_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon DROP CONSTRAINT IF EXISTS op_coupon_pkey;
ALTER TABLE IF EXISTS ONLY public.op_coupon_offline DROP CONSTRAINT IF EXISTS op_coupon_offline_pkey;
ALTER TABLE IF EXISTS ONLY public.op_condition DROP CONSTRAINT IF EXISTS op_condition_pkey;
ALTER TABLE IF EXISTS ONLY public.op_condition_detail DROP CONSTRAINT IF EXISTS op_condition_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.op_common_code DROP CONSTRAINT IF EXISTS op_common_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_claim_memo DROP CONSTRAINT IF EXISTS op_claim_memo_pkey;
ALTER TABLE IF EXISTS ONLY public.op_cashbill DROP CONSTRAINT IF EXISTS op_cashbill_pkey;
ALTER TABLE IF EXISTS ONLY public.op_cashbill_issue DROP CONSTRAINT IF EXISTS op_cashbill_issue_pkey;
ALTER TABLE IF EXISTS ONLY public.op_cash_receipt DROP CONSTRAINT IF EXISTS op_cash_receipt_pkey;
ALTER TABLE IF EXISTS ONLY public.op_cart_set DROP CONSTRAINT IF EXISTS op_cart_set_pkey;
ALTER TABLE IF EXISTS ONLY public.op_cart DROP CONSTRAINT IF EXISTS op_cart_pkey;
ALTER TABLE IF EXISTS ONLY public.od_order DROP CONSTRAINT IF EXISTS od_order_pkey;
ALTER TABLE IF EXISTS ONLY public.od_claim DROP CONSTRAINT IF EXISTS od_claim_pkey;
ALTER TABLE IF EXISTS ONLY public.od_cart_item DROP CONSTRAINT IF EXISTS od_cart_item_user_id_item_id_key;
ALTER TABLE IF EXISTS ONLY public.od_cart_item DROP CONSTRAINT IF EXISTS od_cart_item_pkey;
ALTER TABLE IF EXISTS ONLY public.g_remittance_file DROP CONSTRAINT IF EXISTS g_remittance_file_pkey;
ALTER TABLE IF EXISTS ONLY public.g_order_agency_order_log DROP CONSTRAINT IF EXISTS g_order_agency_order_log_pkey;
ALTER TABLE IF EXISTS ONLY public.g_agency_private_key DROP CONSTRAINT IF EXISTS g_agency_private_key_pkey;
ALTER TABLE IF EXISTS public.op_order_item_hold_hist ALTER COLUMN reg_seq DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_coupon_user ALTER COLUMN coupon_user_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_coupon_regular ALTER COLUMN coupon_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_coupon_offline ALTER COLUMN coupon_offline_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_coupon ALTER COLUMN coupon_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.od_claim ALTER COLUMN claim_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.od_cart_item ALTER COLUMN cart_item_id DROP DEFAULT;
DROP TABLE IF EXISTS public.op_shipment_return;
DROP TABLE IF EXISTS public.op_shipment;
DROP TABLE IF EXISTS public.op_remittance_detail;
DROP TABLE IF EXISTS public.op_remittance;
DROP TABLE IF EXISTS public.op_order_temp;
DROP TABLE IF EXISTS public.op_order_shipping_info;
DROP TABLE IF EXISTS public.op_order_shipping_cp_buy_temp;
DROP TABLE IF EXISTS public.op_order_shipping_buy_temp;
DROP TABLE IF EXISTS public.op_order_shipping;
DROP TABLE IF EXISTS public.op_order_set_return_apply;
DROP TABLE IF EXISTS public.op_order_set_exchange_apply;
DROP TABLE IF EXISTS public.op_order_set_cancel_apply;
DROP TABLE IF EXISTS public.op_order_send_message_log;
DROP TABLE IF EXISTS public.op_order_sales;
DROP TABLE IF EXISTS public.op_order_return_apply;
DROP TABLE IF EXISTS public.op_order_refund;
DROP TABLE IF EXISTS public.op_order_pg_data;
DROP TABLE IF EXISTS public.op_order_payment_buy_temp;
DROP TABLE IF EXISTS public.op_order_payment;
DROP TABLE IF EXISTS public.op_order_log;
DROP TABLE IF EXISTS public.op_order_item_temp;
DROP TABLE IF EXISTS public.op_order_item_set_temp;
DROP TABLE IF EXISTS public.op_order_item_set_buy_temp;
DROP TABLE IF EXISTS public.op_order_item_set;
DROP SEQUENCE IF EXISTS public.op_order_item_hold_hist_reg_seq_seq;
DROP TABLE IF EXISTS public.op_order_item_hold_hist;
DROP TABLE IF EXISTS public.op_order_item_hold;
DROP TABLE IF EXISTS public.op_order_item_buy_temp;
DROP TABLE IF EXISTS public.op_order_item;
DROP TABLE IF EXISTS public.op_order_gift_item_log;
DROP TABLE IF EXISTS public.op_order_gift_item;
DROP TABLE IF EXISTS public.op_order_exchange_apply;
DROP TABLE IF EXISTS public.op_order_claim_shipping;
DROP TABLE IF EXISTS public.op_order_cancel_fail;
DROP TABLE IF EXISTS public.op_order_cancel_apply;
DROP TABLE IF EXISTS public.op_order_batch_target;
DROP TABLE IF EXISTS public.op_order_admin_detail;
DROP TABLE IF EXISTS public.op_order_admin;
DROP TABLE IF EXISTS public.op_order_add_payment;
DROP TABLE IF EXISTS public.op_order;
DROP TABLE IF EXISTS public.op_delivery_hope;
DROP TABLE IF EXISTS public.op_delivery_company;
DROP SEQUENCE IF EXISTS public.op_coupon_user_coupon_user_id_seq;
DROP TABLE IF EXISTS public.op_coupon_user;
DROP TABLE IF EXISTS public.op_coupon_target_user;
DROP TABLE IF EXISTS public.op_coupon_target_item;
DROP SEQUENCE IF EXISTS public.op_coupon_regular_coupon_id_seq;
DROP TABLE IF EXISTS public.op_coupon_regular;
DROP SEQUENCE IF EXISTS public.op_coupon_offline_coupon_offline_id_seq;
DROP TABLE IF EXISTS public.op_coupon_offline;
DROP SEQUENCE IF EXISTS public.op_coupon_coupon_id_seq;
DROP TABLE IF EXISTS public.op_coupon;
DROP TABLE IF EXISTS public.op_condition_detail;
DROP TABLE IF EXISTS public.op_condition;
DROP TABLE IF EXISTS public.op_common_code;
DROP TABLE IF EXISTS public.op_claim_memo;
DROP TABLE IF EXISTS public.op_cashbill_issue;
DROP TABLE IF EXISTS public.op_cashbill;
DROP TABLE IF EXISTS public.op_cash_receipt;
DROP TABLE IF EXISTS public.op_cart_set;
DROP TABLE IF EXISTS public.op_cart;
DROP TABLE IF EXISTS public.od_order;
DROP SEQUENCE IF EXISTS public.od_claim_claim_id_seq;
DROP TABLE IF EXISTS public.od_claim;
DROP SEQUENCE IF EXISTS public.od_cart_item_cart_item_id_seq;
DROP TABLE IF EXISTS public.od_cart_item;
DROP TABLE IF EXISTS public.g_remittance_file;
DROP TABLE IF EXISTS public.g_order_agency_order_log;
DROP TABLE IF EXISTS public.g_agency_private_key;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: g_agency_private_key; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_agency_private_key (
    user_session_id character varying(50) NOT NULL,
    private_key character varying(5000) NOT NULL,
    public_key character varying(5000) NOT NULL,
    frst_reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_order_agency_order_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_order_agency_order_log (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    manager_id bigint NOT NULL,
    manager_nm character varying(50) NOT NULL,
    manager_lclgv_cd character varying(10),
    pbadms_wlfr_cntr_id bigint NOT NULL,
    user_id bigint NOT NULL,
    reg_dt timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: g_remittance_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_remittance_file (
    remittance_id bigint NOT NULL,
    file_seq integer NOT NULL,
    file_name character varying(255),
    org_file_name character varying(255),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone,
    path_name character varying(255)
);


--
-- Name: od_cart_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.od_cart_item (
    cart_item_id bigint NOT NULL,
    user_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity integer NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    updated_date timestamp without time zone
);


--
-- Name: od_cart_item_cart_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.od_cart_item_cart_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: od_cart_item_cart_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.od_cart_item_cart_item_id_seq OWNED BY public.od_cart_item.cart_item_id;


--
-- Name: od_claim; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.od_claim (
    claim_id bigint NOT NULL,
    order_id character varying(50) NOT NULL,
    claim_type character varying(20) NOT NULL,
    reason character varying(255),
    status character varying(20) NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    processed_date timestamp without time zone
);


--
-- Name: od_claim_claim_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.od_claim_claim_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: od_claim_claim_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.od_claim_claim_id_seq OWNED BY public.od_claim.claim_id;


--
-- Name: od_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.od_order (
    order_id character varying(50) NOT NULL,
    user_id bigint NOT NULL,
    item_id bigint NOT NULL,
    seller_id bigint,
    item_name character varying(255),
    quantity integer NOT NULL,
    unit_price integer NOT NULL,
    point_amount bigint NOT NULL,
    order_status character varying(20) NOT NULL,
    stock_outcome character varying(20),
    point_outcome character varying(20),
    cancel_reason character varying(255),
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    updated_date timestamp without time zone,
    carrier_code character varying(50),
    invoice_no character varying(50),
    delivery_status character varying(20),
    shipped_date timestamp without time zone,
    delivered_date timestamp without time zone,
    confirmed_date timestamp without time zone,
    delivery_address character varying(255),
    delivery_address_detail character varying(255),
    locgov_code character varying(50),
    receiver_name character varying(100),
    receiver_phone character varying(20),
    request_note character varying(150),
    coupon_issue_id integer,
    discount_amount bigint DEFAULT 0 NOT NULL
);


--
-- Name: op_cart; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_cart (
    cart_id integer DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    quantity integer NOT NULL,
    options character varying(4000),
    shipping_payment_type character varying(1) NOT NULL,
    shipping_group_code character varying(20) NOT NULL,
    addition_item_flag character varying(1) NOT NULL,
    parent_item_id integer NOT NULL,
    set_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    created_date character varying(14),
    text_option character varying(4000)
);


--
-- Name: op_cart_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_cart_set (
    cart_id integer DEFAULT 0 NOT NULL,
    item_id integer DEFAULT 0 NOT NULL,
    quantity integer NOT NULL,
    options character varying(4000),
    parent_cart_id integer,
    created_date character varying(14)
);


--
-- Name: op_cash_receipt; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_cash_receipt (
    cash_receipt_id integer NOT NULL,
    cash_receipt_pg_service_type character varying(40),
    order_code character varying(40) NOT NULL,
    order_sequence integer NOT NULL,
    payment_sequence integer NOT NULL,
    cash_receipt_amount integer NOT NULL,
    cash_receipt_tax_free_amount integer NOT NULL,
    cash_receipt_name character varying(50) NOT NULL,
    cash_receipt_code character varying(50) NOT NULL,
    cash_receipt_type character varying(1) NOT NULL,
    cash_receipt_issue_number character varying(50),
    cash_receipt_issue_date character varying(14),
    cash_receipt_status_code character varying(1) NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_cashbill; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_cashbill (
    id bigint NOT NULL,
    cashbill_code character varying(20),
    cashbill_type character varying(20),
    created_by character varying(50),
    created_date character varying(14),
    customer_name character varying(50),
    order_code character varying(50),
    pg_service character varying(20)
);


--
-- Name: op_cashbill_issue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_cashbill_issue (
    id bigint NOT NULL,
    amount bigint NOT NULL,
    canceled_date character varying(14),
    cashbill_id bigint,
    cashbill_issue_type character varying(20),
    cashbill_status character varying(20),
    created_date character varying(14),
    issued_date character varying(14),
    item_name character varying(200),
    mgt_key character varying(255),
    tax_type character varying(20),
    update_by character varying(100),
    updated_date character varying(14)
);


--
-- Name: op_claim_memo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_claim_memo (
    claim_memo_id integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    user_name character varying(350) NOT NULL,
    order_code character varying(50),
    claim_status character varying(1) NOT NULL,
    memo character varying(1500) NOT NULL,
    manager_user_id bigint DEFAULT 0 NOT NULL,
    manager_login_id character varying(60) NOT NULL,
    data_status_code double precision NOT NULL,
    created_date character varying(14) NOT NULL
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
-- Name: op_condition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_condition (
    condition_id integer NOT NULL,
    category_code character varying(50),
    condition_title character varying(255),
    use_yn character(1),
    created_date character varying(20),
    updated_date character varying(20)
);


--
-- Name: op_condition_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_condition_detail (
    detail_id integer NOT NULL,
    condition_id integer,
    detail_title character varying(255),
    use_yn character(1),
    ordering integer,
    created_date character varying(20),
    updated_date character varying(20)
);


--
-- Name: op_coupon; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon (
    coupon_id integer NOT NULL,
    coupon_type character varying(255) DEFAULT ''::character varying,
    coupon_name character varying(100) NOT NULL,
    coupon_comment character varying(500),
    coupon_issue_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_issue_start_date character varying(14),
    coupon_issue_end_date character varying(14),
    coupon_apply_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_apply_day integer,
    coupon_apply_start_date character varying(14),
    coupon_apply_end_date character varying(14),
    coupon_target_time_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_target_user_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_target_user_level character varying(4000),
    coupon_target_user text,
    coupon_pay_restriction integer DEFAULT '-1'::integer NOT NULL,
    coupon_concurrently character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_pay_type character varying(1) NOT NULL,
    coupon_pay integer NOT NULL,
    coupon_discount_limit_price integer DEFAULT '-1'::integer,
    coupon_target_item_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_target_item text,
    coupon_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    coupon_offline_flag character varying(1) DEFAULT 'N'::character varying,
    coupon_birthday character varying(2),
    data_status_code character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_download_limit integer DEFAULT '-1'::integer NOT NULL,
    coupon_download_user_limit integer DEFAULT '-1'::integer NOT NULL,
    coupon_mulitple_download_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    created_date character varying(14),
    update_user_name character varying(50),
    updated_date character varying(14),
    direct_input_flag character varying(1) DEFAULT 'N'::character varying,
    direct_input_value character varying(100)
);


--
-- Name: op_coupon_coupon_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_coupon_coupon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_coupon_coupon_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_coupon_coupon_id_seq OWNED BY public.op_coupon.coupon_id;


--
-- Name: op_coupon_offline; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon_offline (
    coupon_offline_id integer NOT NULL,
    coupon_id integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    coupon_offline_code character varying(30) NOT NULL,
    coupon_used_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    coupon_used_date character varying(14),
    published_date character varying(20) NOT NULL
);


--
-- Name: op_coupon_offline_coupon_offline_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_coupon_offline_coupon_offline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_coupon_offline_coupon_offline_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_coupon_offline_coupon_offline_id_seq OWNED BY public.op_coupon_offline.coupon_offline_id;


--
-- Name: op_coupon_regular; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon_regular (
    coupon_id integer NOT NULL,
    coupon_type character varying(255) DEFAULT ''::character varying,
    coupon_name character varying(100) NOT NULL,
    coupon_comment character varying(500),
    coupon_issue_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_issue_start_date character varying(14),
    coupon_issue_end_date character varying(14),
    coupon_target_time_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_target_user_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_target_user_level character varying(4000),
    coupon_target_user text,
    coupon_pay_restriction integer DEFAULT '-1'::integer NOT NULL,
    coupon_concurrently character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_pay_type character varying(1) NOT NULL,
    coupon_pay integer NOT NULL,
    coupon_discount_limit_price integer DEFAULT '-1'::integer,
    coupon_target_item_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_target_item text,
    coupon_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    coupon_birthday character varying(2),
    data_status_code character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_download_limit integer DEFAULT '-1'::integer NOT NULL,
    coupon_download_user_limit integer DEFAULT '-1'::integer NOT NULL,
    coupon_mulitple_download_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    created_date character varying(14),
    update_user_name character varying(50),
    updated_date character varying(14)
);


--
-- Name: op_coupon_regular_coupon_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_coupon_regular_coupon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_coupon_regular_coupon_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_coupon_regular_coupon_id_seq OWNED BY public.op_coupon_regular.coupon_id;


--
-- Name: op_coupon_target_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon_target_item (
    item_id integer NOT NULL,
    coupon_id integer NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_coupon_target_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon_target_user (
    user_id bigint DEFAULT 0 NOT NULL,
    coupon_id integer DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_coupon_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_coupon_user (
    coupon_user_id integer NOT NULL,
    coupon_id integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    coupon_type character varying(255) NOT NULL,
    coupon_name character varying(100) NOT NULL,
    coupon_comment character varying(500) NOT NULL,
    coupon_apply_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    coupon_apply_start_date character varying(14),
    coupon_apply_end_date character varying(14),
    coupon_pay_restriction integer DEFAULT '-1'::integer NOT NULL,
    coupon_concurrently character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_pay_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    coupon_pay integer NOT NULL,
    coupon_discount_limit_price integer DEFAULT '-1'::integer,
    coupon_target_item_type character varying(1) DEFAULT '1'::character varying NOT NULL,
    data_status_code character varying(1),
    coupon_download_date character varying(14) NOT NULL,
    coupon_used_date character varying(14),
    order_code character varying(50),
    order_sequence integer,
    item_sequence integer,
    discount_amount integer,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_coupon_user_coupon_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_coupon_user_coupon_user_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_coupon_user_coupon_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_coupon_user_coupon_user_id_seq OWNED BY public.op_coupon_user.coupon_user_id;


--
-- Name: op_delivery_company; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_delivery_company (
    delivery_company_id integer NOT NULL,
    delivery_company_name character varying(30) NOT NULL,
    tel_number character varying(20),
    delivery_company_url character varying(255),
    send_flag character varying(1) DEFAULT '1'::character varying NOT NULL,
    delivery_number_parameter character varying(255),
    use_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL
);


--
-- Name: op_delivery_hope; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_delivery_hope (
    delivery_hope_id integer NOT NULL,
    delivery_hope_time character varying(50),
    delivery_hope_index integer
);


--
-- Name: op_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    order_total_amount integer NOT NULL,
    pay_amount integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    login_id character varying(300),
    buyer_name character varying(350) NOT NULL,
    phone character varying(210),
    mobile character varying(210),
    email character varying(420),
    zipcode character varying(7),
    new_zipcode character varying(5),
    sido character varying(20),
    sigungu character varying(20),
    eupmyeondong character varying(20),
    address character varying(1000),
    address_detail character varying(1785),
    data_status_code character varying(1) NOT NULL,
    ip character varying(300),
    return_bank_name character varying(350),
    return_bank_in_name character varying(350),
    return_virtual_no character varying(350),
    order_admin_memo character varying(1500),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_add_payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_add_payment (
    add_payment_id integer NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    order_code character varying(50),
    order_sequence integer,
    refund_code character varying(50),
    issue_code character varying(50) NOT NULL,
    subject character varying(255) NOT NULL,
    add_payment_type integer NOT NULL,
    amount integer NOT NULL,
    sales_date character varying(14),
    sales_cancel_date character varying(14),
    remittance_id bigint,
    remittance_amount integer NOT NULL,
    remittance_expected_date character varying(8),
    remittance_date character varying(8),
    remittance_status_code character varying(1) DEFAULT '1'::character varying NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_order_admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_admin (
    work_date character varying(8) NOT NULL,
    work_sequence integer NOT NULL,
    insert_manager_name character varying(50) NOT NULL,
    data_status_code character varying(1) NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_admin_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_admin_detail (
    work_date character varying(8) NOT NULL,
    work_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    order_group_code character varying(10) NOT NULL,
    template_version character varying(8) NOT NULL,
    excel_data character varying NOT NULL,
    data_status_code character varying(1) DEFAULT '1'::character varying NOT NULL,
    sale_price integer,
    update_manager_name character varying(50),
    updated_date character varying(14),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_batch_target; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_batch_target (
    batch_key character varying(255) NOT NULL,
    order_code character varying(50) NOT NULL,
    start_date character varying(14),
    batch_type character varying(20),
    target_index integer,
    target_code character varying(50),
    error_message text
);


--
-- Name: op_order_cancel_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_cancel_apply (
    claim_code character varying(50) NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    parent_item_sequence integer,
    cancel_apply_date character varying(8) NOT NULL,
    refund_code character varying(50) DEFAULT ''::character varying,
    claim_status character varying(2) NOT NULL,
    claim_apply_subject character varying(2) NOT NULL,
    claim_apply_quantity integer NOT NULL,
    cancel_reason character varying(1) NOT NULL,
    cancel_reason_text character varying(255) NOT NULL,
    cancel_reason_detail character varying(255) NOT NULL,
    cancel_memo text,
    cancel_refusal_reason_text character varying(255),
    updated_date character varying(14),
    created_date character varying(14)
);


--
-- Name: op_order_cancel_fail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_cancel_fail (
    id bigint NOT NULL,
    approval_type character varying(50) NOT NULL,
    cancel_amount integer NOT NULL,
    cancel_reason character varying(255) NOT NULL,
    cancel_requester character varying(1) NOT NULL,
    order_code character varying(50) NOT NULL,
    pay_date character varying(14) NOT NULL,
    pg_key character varying(100) NOT NULL,
    pg_service_type character varying(50) NOT NULL,
    tax_amount integer NOT NULL,
    tax_free_amount integer NOT NULL
);


--
-- Name: op_order_claim_shipping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_claim_shipping (
    order_code character varying(50) DEFAULT ''::character varying NOT NULL,
    order_sequence integer DEFAULT 0 NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    refund_code character varying(50) DEFAULT ''::character varying NOT NULL,
    amount integer NOT NULL,
    remittance_amount integer NOT NULL,
    manager_user_name character varying(50) NOT NULL,
    created_date character varying(50) NOT NULL
);


--
-- Name: op_order_exchange_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_exchange_apply (
    claim_code character varying(50) NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    parent_item_sequence integer,
    exchange_apply_date character varying(8) NOT NULL,
    claim_apply_quantity integer NOT NULL,
    shipment_return_seller_id bigint DEFAULT 0 NOT NULL,
    shipment_return_id integer NOT NULL,
    claim_status character varying(2) NOT NULL,
    claim_apply_subject character varying(2) NOT NULL,
    exchange_receive_name character varying(210) NOT NULL,
    exchange_receive_phone character varying(210),
    exchange_receive_mobile character varying(210) NOT NULL,
    exchange_receive_zipcode character varying(7),
    exchange_receive_sido character varying(20),
    exchange_receive_sigungu character varying(20),
    exchange_receive_eupmyeondong character varying(20),
    exchange_receive_address character varying(1000),
    exchange_receive_address2 character varying(1785),
    exchange_delivery_company_id integer,
    exchange_delivery_company_name character varying(50),
    exchange_delivery_number character varying(50),
    exchange_delivery_company_url character varying(255),
    exchange_delivery_date character varying(14),
    exchange_shipping_ask_type character varying(1) NOT NULL,
    exchange_shipping_number character varying(30),
    exchange_shipping_company_name character varying(30),
    exchange_shipping_company_url character varying(255),
    exchange_shipping_start_date character varying(8),
    exchange_reason character varying(1) NOT NULL,
    exchange_reason_text character varying(255) NOT NULL,
    exchange_reason_detail character varying(255) NOT NULL,
    exchange_memo text,
    exchange_refusal_reason_text character varying(255),
    updated_date character varying(14),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_gift_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_gift_item (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    gift_group_id bigint NOT NULL,
    gift_item_code character varying(30) NOT NULL,
    gift_item_id bigint NOT NULL,
    gift_item_name character varying(30) NOT NULL,
    gift_order_status character varying(15) NOT NULL,
    gift_sequence integer NOT NULL,
    group_type character varying(15) NOT NULL,
    image character varying(255),
    item_sequence integer NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    price integer NOT NULL,
    seller_id bigint NOT NULL,
    valid_end_date timestamp without time zone,
    valid_start_date timestamp without time zone
);


--
-- Name: op_order_gift_item_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_gift_item_log (
    id bigint NOT NULL,
    created timestamp without time zone,
    created_by bigint,
    updated timestamp without time zone,
    updated_by bigint,
    gift_group_id bigint NOT NULL,
    gift_item_code character varying(30) NOT NULL,
    gift_item_id bigint NOT NULL,
    gift_item_name character varying(30) NOT NULL,
    gift_order_status character varying(15) NOT NULL,
    gift_sequence integer NOT NULL,
    group_type character varying(15) NOT NULL,
    item_sequence integer NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    org_gift_order_status character varying(15) NOT NULL,
    user_type character varying(10)
);


--
-- Name: op_order_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    order_status character varying(2) NOT NULL,
    shipping_sequence integer NOT NULL,
    shipping_info_sequence integer NOT NULL,
    device_type character varying(15) NOT NULL
);


--
-- Name: op_order_item_buy_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_buy_temp (
    user_id bigint NOT NULL,
    session_id character varying(120) NOT NULL,
    order_code character varying(50) NOT NULL,
    item_sequence integer NOT NULL,
    shipping_index integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    options text,
    coupon_user_id integer DEFAULT 0,
    add_coupon_user_id bigint DEFAULT 0 NOT NULL,
    shipping_payment_type character varying(1) NOT NULL,
    addition_item_flag character varying(1) NOT NULL,
    parent_item_sequence integer NOT NULL,
    parent_item_id integer NOT NULL,
    escrow_status character varying(2) DEFAULT 'N'::character varying NOT NULL,
    created_date character varying(14) NOT NULL,
    campaign_code character varying(50),
    set_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL
);


--
-- Name: op_order_item_hold; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_hold (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    order_status character varying(2) NOT NULL,
    shipping_sequence integer NOT NULL,
    shipping_info_sequence integer NOT NULL,
    device_type character varying(15) NOT NULL,
    addition_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    parent_item_sequence integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    guest_flag character varying(1) NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    category_team_id integer,
    category_group_id integer,
    category_id integer,
    shipment_id integer DEFAULT 0 NOT NULL,
    shipment_return_id integer DEFAULT 0 NOT NULL,
    coupon_user_id integer,
    add_coupon_user_id bigint DEFAULT 0 NOT NULL,
    item_id integer,
    item_code character varying(30),
    item_user_code character varying(30) NOT NULL,
    item_name character varying(200) NOT NULL,
    free_gift_name character varying(255),
    image_src character varying(255),
    shipment_group_code character varying(50),
    shipping_return integer NOT NULL,
    purchase_price integer NOT NULL,
    cost_price integer NOT NULL,
    price integer NOT NULL,
    option_price integer NOT NULL,
    sale_price integer NOT NULL,
    quantity integer DEFAULT 0 NOT NULL,
    claim_quantity integer DEFAULT 0,
    order_quantity integer DEFAULT 0 NOT NULL,
    coupon_discount_price integer DEFAULT 0,
    spot_sale_flag character varying(1) DEFAULT 'N'::character varying,
    spot_type character varying(1),
    spot_discount_price integer,
    level_id integer,
    level_name character varying(50),
    user_level_discount_rate double precision,
    user_level_discount_price integer,
    escrow_status character varying(2) DEFAULT 'N'::character varying NOT NULL,
    tax_type character varying(1) NOT NULL,
    commission_base_price integer NOT NULL,
    commission_rate double precision NOT NULL,
    commission_price integer NOT NULL,
    commission_type character varying(1) DEFAULT '1'::character varying,
    supply_price integer NOT NULL,
    remittance_id bigint,
    remittance_type character varying(1),
    remittance_day character varying(2),
    remittance_expected_date character varying(8),
    remittance_date character varying(8),
    remittance_status_code character varying(1) DEFAULT '1'::character varying,
    admin_discount_price integer,
    admin_discount_detail text,
    seller_discount_price integer,
    seller_discount_detail text,
    brand character varying(50),
    options text,
    delivery_type character varying(1) NOT NULL,
    delivery_company_id integer,
    delivery_company_name character varying(50),
    delivery_number character varying(100),
    delivery_company_url character varying(255),
    shipment_return_type character varying(1) NOT NULL,
    point_type character varying(1),
    point_config_type character varying(1) DEFAULT '1'::character varying,
    point integer DEFAULT 0,
    point_log character varying(255),
    earn_point integer,
    seller_point integer NOT NULL,
    earn_point_flag character varying(1) DEFAULT 'N'::character varying,
    return_point_flag character varying(1) DEFAULT 'Y'::character varying,
    cancel_flag character varying(1) DEFAULT 'N'::character varying,
    refund_status character varying(1) DEFAULT '0'::character varying,
    item_return_flag character varying(1) DEFAULT 'Y'::character varying,
    revenue_sales_status character varying(2) DEFAULT ''::character varying NOT NULL,
    pay_date character varying(14) DEFAULT ''::character varying,
    shipping_ready_date character varying(14) DEFAULT ''::character varying,
    shipping_date character varying(14) DEFAULT ''::character varying,
    shipping_finish_date character varying(14) DEFAULT ''::character varying,
    cancel_request_date character varying(14),
    cancel_request_finish_date character varying(14),
    sales_date character varying(14),
    sales_cancel_date character varying(14),
    confirm_date character varying(14) DEFAULT ''::character varying,
    return_request_date character varying(14),
    return_request_finish_date character varying(14),
    exchange_request_date character varying(14),
    updated_admin_user_name character varying(300),
    created_date character varying(14) NOT NULL,
    campaign_code character varying(50),
    set_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    set_discount_type character varying(1),
    set_discount_price integer DEFAULT 0,
    locgov_code character varying(10),
    mobile_number character varying(100),
    etc_amt integer DEFAULT 0,
    filler1 character varying(300),
    filler2 character varying(300),
    filler3 character varying(300),
    filler4 character varying(300),
    filler5 character varying(300),
    filler6 character varying(300),
    filler7 character varying(300),
    filler8 character varying(300),
    filler9 character varying(300),
    filler10 character varying(300),
    hold_confirm_status character varying(1) DEFAULT 'N'::character varying,
    hold_confirm_date_seller character varying(14),
    hold_confirm_date_mgr character varying(14),
    hold_reject_date_mgr character varying(14),
    holid_confirm_seller_user_id bigint,
    holid_confirm_mgr_id bigint,
    holid_reject_mgr_id bigint
);


--
-- Name: op_order_item_hold_hist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_hold_hist (
    reg_seq bigint NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    hold_confrim_status_bef character varying(1) NOT NULL,
    hold_confrim_status_aft character varying(1) NOT NULL,
    created_user_tp character varying(1) NOT NULL,
    created_user_id bigint NOT NULL,
    created_date character varying(14) DEFAULT 'TO_CHAR(SYS_DATETIME,'::character varying
);


--
-- Name: op_order_item_hold_hist_reg_seq_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_order_item_hold_hist_reg_seq_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_order_item_hold_hist_reg_seq_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_order_item_hold_hist_reg_seq_seq OWNED BY public.op_order_item_hold_hist.reg_seq;


--
-- Name: op_order_item_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_set (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    set_item_sequence integer NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    guest_flag character varying(1) NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    item_id integer,
    item_code character varying(30),
    item_user_code character varying(30) NOT NULL,
    item_name character varying(200) NOT NULL,
    image_src character varying(255),
    purchase_price integer NOT NULL,
    cost_price integer NOT NULL,
    price integer NOT NULL,
    option_price integer NOT NULL,
    sale_price integer NOT NULL,
    quantity integer DEFAULT 0 NOT NULL,
    claim_quantity integer DEFAULT 0 NOT NULL,
    order_quantity integer DEFAULT 0 NOT NULL,
    spot_sale_flag character varying(1) DEFAULT 'N'::character varying,
    spot_type character varying(1),
    spot_discount_price integer,
    tax_type character varying(1) NOT NULL,
    commission_base_price integer NOT NULL,
    commission_rate double precision NOT NULL,
    commission_price integer NOT NULL,
    commission_type character varying(1) DEFAULT '1'::character varying,
    supply_price integer NOT NULL,
    admin_discount_price integer,
    admin_discount_detail text,
    seller_discount_price integer,
    seller_discount_detail text,
    brand character varying(50),
    options text,
    created_date character varying(14)
);


--
-- Name: op_order_item_set_buy_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_set_buy_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    order_code character varying(50),
    item_sequence integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    options text,
    set_item_sequence integer NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_order_item_set_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_set_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    item_sequence integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    options text,
    set_item_sequence integer NOT NULL,
    created_date character varying(14),
    text_option character varying(4000)
);


--
-- Name: op_order_item_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_item_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    item_sequence integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    options text,
    coupon_user_id integer DEFAULT 0,
    add_coupon_user_id bigint DEFAULT 0 NOT NULL,
    shipping_payment_type character varying(1) NOT NULL,
    addition_item_flag character varying(1) NOT NULL,
    parent_item_sequence integer NOT NULL,
    parent_item_id integer,
    created_date character varying(14) NOT NULL,
    campaign_code character varying(50),
    set_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    text_option character varying(4000)
);


--
-- Name: op_order_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_log (
    id bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_at_text character varying(255),
    created_by character varying(500),
    ip character varying(300),
    item_name character varying(255),
    item_sequence integer,
    log_type character varying(30) NOT NULL,
    order_code character varying(50),
    order_sequence integer,
    order_status character varying(20),
    org_order_status character varying(20),
    updated_at_text character varying(255),
    updated_by character varying(500),
    user_type character varying(10)
);


--
-- Name: op_order_payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_payment (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    payment_sequence integer NOT NULL,
    payment_type character varying(1) NOT NULL,
    order_pg_data_id integer DEFAULT 0 NOT NULL,
    approval_type character varying(50) NOT NULL,
    device_type character varying(14) NOT NULL,
    card_easy_type character varying(50),
    bank_virtual_no character varying(255),
    bank_in_name character varying(700),
    bank_date character varying(14),
    amount integer NOT NULL,
    tax_free_amount integer NOT NULL,
    cancel_amount integer DEFAULT 0 NOT NULL,
    remaining_amount integer DEFAULT 0 NOT NULL,
    pay_date character varying(14),
    now_payment_flag character varying(1) NOT NULL,
    created_date character varying(14) NOT NULL,
    refund_flag character varying(1),
    payment_summary character varying(1785)
);


--
-- Name: op_order_payment_buy_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_payment_buy_temp (
    order_code character varying(50) NOT NULL,
    approval_type character varying(50) NOT NULL,
    service_type character varying(50) NOT NULL,
    amount integer NOT NULL,
    tax_free_amount integer NOT NULL,
    bank_virtual_no character varying(255),
    bank_in_name character varying(100),
    bank_date character varying(14),
    service_mid character varying(100),
    service_key character varying(100),
    created_date character varying(100)
);


--
-- Name: op_order_pg_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_pg_data (
    order_pg_data_id integer DEFAULT 0 NOT NULL,
    order_code character varying(50) DEFAULT '0'::character varying NOT NULL,
    pg_service_type character varying(50) NOT NULL,
    pg_service_mid character varying(100) NOT NULL,
    pg_service_key character varying(100) NOT NULL,
    pg_payment_type character varying(100) NOT NULL,
    pg_key character varying(100) NOT NULL,
    pg_auth_code character varying(100),
    pg_proc_info text NOT NULL,
    part_cancel_flag character varying(1) DEFAULT 'Y'::character varying NOT NULL,
    part_cancel_detail character varying(500),
    pg_amount integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_refund; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_refund (
    refund_code character varying(50) NOT NULL,
    refund_date character varying(8) NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    refund_status_code character varying(1) NOT NULL,
    request_manager_user_name character varying(350),
    process_manager_user_name character varying(350),
    return_bank_name character varying(350),
    return_virtual_no character varying(350),
    return_bank_in_name character varying(350),
    created_date character varying(14) NOT NULL,
    updated_date character varying(14)
);


--
-- Name: op_order_return_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_return_apply (
    claim_code character varying(50) NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    parent_item_sequence integer,
    return_apply_date character varying(8) NOT NULL,
    refund_code character varying(50),
    shipment_return_seller_id bigint DEFAULT 0 NOT NULL,
    shipment_return_id integer NOT NULL,
    claim_apply_quantity integer NOT NULL,
    claim_status character varying(2) NOT NULL,
    return_reason character varying(1) NOT NULL,
    return_reason_text character varying(255) NOT NULL,
    return_reason_detail character varying(255) NOT NULL,
    claim_apply_subject character varying(2) NOT NULL,
    previous_order_status character varying(2),
    return_reserve_name character varying(210) NOT NULL,
    return_reserve_phone character varying(210),
    return_reserve_mobile character varying(210) NOT NULL,
    return_reserve_zipcode character varying(10),
    return_reserve_sido character varying(20),
    return_reserve_sigungu character varying(20),
    return_reserve_eupmyeondong character varying(20),
    return_reserve_address character varying(1000),
    return_reserve_address2 character varying(1785),
    return_shipping_ask_type character varying(1) NOT NULL,
    return_shipping_number character varying(30),
    return_shipping_company_name character varying(30),
    return_shipping_company_url character varying(255),
    collection_shipping_amount integer,
    return_memo text,
    return_refusal_reason_text character varying(255),
    updated_date character varying(14),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_sales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_sales (
    user_id bigint,
    pay_date character varying(8),
    amount integer DEFAULT 0
);


--
-- Name: op_order_send_message_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_send_message_log (
    order_code character varying(50) NOT NULL,
    template_id character varying(50) NOT NULL,
    delivery_number character varying(50),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_set_cancel_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_set_cancel_apply (
    claim_code character varying(20) DEFAULT ''::character varying NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    set_item_sequence integer NOT NULL,
    set_claim_code character varying(50) NOT NULL,
    cancel_apply_date character varying(8) NOT NULL,
    refund_code character varying(50) DEFAULT ''::character varying,
    claim_status character varying(2) NOT NULL,
    claim_apply_quantity integer NOT NULL,
    updated_date character varying(14),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_set_exchange_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_set_exchange_apply (
    claim_code character varying(20) DEFAULT ''::character varying NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    set_item_sequence integer NOT NULL,
    set_claim_code character varying(50) NOT NULL,
    exchange_apply_date character varying(8) NOT NULL,
    claim_apply_quantity integer NOT NULL,
    claim_status character varying(2) NOT NULL,
    updated_date character varying(14),
    created_date character varying(14)
);


--
-- Name: op_order_set_return_apply; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_set_return_apply (
    claim_code character varying(20) DEFAULT ''::character varying NOT NULL,
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    item_sequence integer NOT NULL,
    set_item_sequence integer NOT NULL,
    set_claim_code character varying(50) NOT NULL,
    return_apply_date character varying(8) NOT NULL,
    refund_code character varying(50),
    claim_apply_quantity integer NOT NULL,
    claim_status character varying(2) NOT NULL,
    updated_date character varying(14),
    created_date character varying(14)
);


--
-- Name: op_order_shipping; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_shipping (
    order_code character varying(50) NOT NULL,
    order_sequence integer NOT NULL,
    shipping_sequence integer NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    shipping_type character varying(1) NOT NULL,
    shipment_group_code character varying(50),
    shipping_group_code character varying(20),
    island_type character varying(20),
    shipping_item_count integer NOT NULL,
    shipping integer NOT NULL,
    shipping_extra_charge1 integer NOT NULL,
    shipping_extra_charge2 integer NOT NULL,
    shipping_free_amount integer NOT NULL,
    real_shipping integer NOT NULL,
    previous_real_shipping integer,
    discount_shipping integer,
    shipping_coupon_count integer,
    shipping_payment_type character varying(1) NOT NULL,
    pay_shipping integer NOT NULL,
    previous_pay_shipping integer,
    return_shipping integer,
    previous_return_shipping integer,
    return_flag character varying(1) DEFAULT 'N'::character varying,
    remittance_id bigint,
    remittance_amount integer NOT NULL,
    previous_remittance_amount integer,
    remittance_expected_date character varying(8),
    remittance_status_code character varying(1) DEFAULT '1'::character varying NOT NULL,
    remittance_date character varying(8)
);


--
-- Name: op_order_shipping_buy_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_shipping_buy_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    order_code character varying(50) NOT NULL,
    shipping_index integer NOT NULL,
    receive_name character varying(1000),
    receive_mobile character varying(1000),
    receive_phone character varying(1000),
    receive_zipcode character varying(10),
    receive_new_zipcode character varying(5),
    receive_sido character varying(20),
    receive_sigungu character varying(20),
    receive_eupmyeondong character varying(20),
    receive_address character varying(1000),
    receive_address_detail character varying(255),
    content character varying(255),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_shipping_cp_buy_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_shipping_cp_buy_temp (
    order_code character varying(50) NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    shipping_group_code character varying(20) NOT NULL,
    use_coupon_count integer NOT NULL,
    discount_amount integer NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_order_shipping_info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_shipping_info (
    order_code character varying(50) DEFAULT '0'::character varying NOT NULL,
    order_sequence integer DEFAULT 0 NOT NULL,
    shipping_info_sequence integer DEFAULT 0 NOT NULL,
    receive_new_zipcode character varying(5),
    receive_zipcode character varying(10),
    receive_sido character varying(20),
    receive_sigungu character varying(20),
    receive_eupmyeondong character varying(20),
    receive_address character varying(1000),
    receive_address_detail character varying(1785),
    receive_name character varying(350) NOT NULL,
    receive_phone character varying(210),
    receive_mobile character varying(210) NOT NULL,
    memo character varying(255),
    created_date character varying(14) NOT NULL,
    updated_date character varying(14)
);


--
-- Name: op_order_temp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_order_temp (
    user_id bigint DEFAULT 0 NOT NULL,
    session_id character varying(120) NOT NULL,
    order_code character varying(50) DEFAULT ''::character varying NOT NULL,
    user_name character varying(1000),
    point_discount_amount integer DEFAULT 0,
    item_coupon_discount_amount integer DEFAULT 0,
    cart_coupon_discount_amount integer DEFAULT 0,
    shipping_coupon_use_count integer DEFAULT 0,
    shipping_coupon_discount_amt integer DEFAULT 0,
    cart_coupon_use_data text,
    order_pay_amount integer NOT NULL,
    company_name character varying(50),
    email character varying(1000),
    mobile character varying(1000),
    phone character varying(1000),
    zipcode character varying(10),
    new_zipcode character varying(5),
    sido character varying(20),
    sigungu character varying(20),
    eupmyeondong character varying(20),
    address character varying(1000),
    address_detail character varying(1000),
    delivery_req_day character varying(10),
    delivery_req_hour character varying(100),
    receipt_name character varying(50),
    cash_receipt_type character varying(1) DEFAULT '0'::character varying,
    cash_receipt_code character varying(30),
    cashbill_type character varying(20) DEFAULT 'NONE'::character varying,
    cashbill_code character varying(30),
    save_delivery_flag character varying(1) DEFAULT 'N'::character varying,
    save_delivery_name character varying(50),
    device_type character varying(50),
    created_date character varying(14) NOT NULL
);


--
-- Name: op_remittance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_remittance (
    remittance_id bigint NOT NULL,
    remittance_status_code character varying(1) DEFAULT '1'::character varying,
    confirm_date character varying(8),
    confirm_date_seller character varying(8),
    payment_date character varying(8),
    finishing_date character varying(8),
    seller_id bigint NOT NULL,
    finishing_amount integer DEFAULT 0,
    finishing_manager_name character varying(1000),
    bank_name character varying(1000),
    bank_in_name character varying(210),
    bank_account_number character varying(350),
    created_date character varying(8),
    remittance_ym character varying(6)
);


--
-- Name: op_remittance_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_remittance_detail (
    remittance_id bigint NOT NULL,
    remittance_detail_id integer NOT NULL,
    order_code character varying(50) NOT NULL,
    remittance_key character varying(50),
    item_type character varying(50),
    item_user_code character varying(30),
    item_name character varying(200),
    options character varying(4000),
    sale_price integer,
    seller_discount_price integer,
    seller_discount_detail character varying(4000),
    seller_point integer,
    set_item_flag character varying(1) DEFAULT 'N'::character varying NOT NULL,
    set_discount_price integer,
    commission_base_price integer,
    commission_rate double precision,
    commission_price integer,
    commission_type character varying(1) DEFAULT '1'::character varying,
    supply_price integer,
    remittance_price integer,
    quantity integer,
    created_date character varying(14),
    order_sequence integer DEFAULT 0,
    item_sequence integer DEFAULT 0,
    etc_amt integer DEFAULT 0
);


--
-- Name: op_shipment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_shipment (
    shipment_id integer DEFAULT 0 NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    address_name character varying(100) NOT NULL,
    name character varying(1000),
    telephone_number character varying(1000),
    zipcode character varying(10) NOT NULL,
    address character varying(1000),
    address_detail character varying(255) NOT NULL,
    default_address_flag character varying(1) NOT NULL,
    shipping integer NOT NULL,
    shipping_free_amount integer NOT NULL,
    shipping_extra_charge1 integer NOT NULL,
    shipping_extra_charge2 integer NOT NULL,
    shipment_group_code character varying(50),
    created_date character varying(14),
    updated_date character varying(14)
);


--
-- Name: op_shipment_return; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_shipment_return (
    shipment_return_id integer DEFAULT 0 NOT NULL,
    seller_id bigint DEFAULT 0 NOT NULL,
    address_name character varying(100) NOT NULL,
    name character varying(350),
    telephone_number character varying(140),
    zipcode character varying(10) NOT NULL,
    address character varying(1000),
    address_detail character varying(1785),
    default_address_flag character varying(255) NOT NULL,
    created_date character varying(14)
);


--
-- Name: od_cart_item cart_item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_cart_item ALTER COLUMN cart_item_id SET DEFAULT nextval('public.od_cart_item_cart_item_id_seq'::regclass);


--
-- Name: od_claim claim_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_claim ALTER COLUMN claim_id SET DEFAULT nextval('public.od_claim_claim_id_seq'::regclass);


--
-- Name: op_coupon coupon_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon ALTER COLUMN coupon_id SET DEFAULT nextval('public.op_coupon_coupon_id_seq'::regclass);


--
-- Name: op_coupon_offline coupon_offline_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_offline ALTER COLUMN coupon_offline_id SET DEFAULT nextval('public.op_coupon_offline_coupon_offline_id_seq'::regclass);


--
-- Name: op_coupon_regular coupon_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_regular ALTER COLUMN coupon_id SET DEFAULT nextval('public.op_coupon_regular_coupon_id_seq'::regclass);


--
-- Name: op_coupon_user coupon_user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_user ALTER COLUMN coupon_user_id SET DEFAULT nextval('public.op_coupon_user_coupon_user_id_seq'::regclass);


--
-- Name: op_order_item_hold_hist reg_seq; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_hold_hist ALTER COLUMN reg_seq SET DEFAULT nextval('public.op_order_item_hold_hist_reg_seq_seq'::regclass);


--
-- Data for Name: g_agency_private_key; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_agency_private_key (user_session_id, private_key, public_key, frst_reg_dt) FROM stdin;
\.


--
-- Data for Name: g_order_agency_order_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_order_agency_order_log (order_code, order_sequence, item_sequence, manager_id, manager_nm, manager_lclgv_cd, pbadms_wlfr_cntr_id, user_id, reg_dt) FROM stdin;
\.


--
-- Data for Name: g_remittance_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_remittance_file (remittance_id, file_seq, file_name, org_file_name, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, path_name) FROM stdin;
\.


--
-- Data for Name: od_cart_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.od_cart_item (cart_item_id, user_id, item_id, quantity, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: od_claim; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.od_claim (claim_id, order_id, claim_type, reason, status, created_date, processed_date) FROM stdin;
\.


--
-- Data for Name: od_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.od_order (order_id, user_id, item_id, seller_id, item_name, quantity, unit_price, point_amount, order_status, stock_outcome, point_outcome, cancel_reason, created_date, updated_date, carrier_code, invoice_no, delivery_status, shipped_date, delivered_date, confirmed_date, delivery_address, delivery_address_detail, locgov_code, receiver_name, receiver_phone, request_note, coupon_issue_id, discount_amount) FROM stdin;
\.


--
-- Data for Name: op_cart; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_cart (cart_id, session_id, user_id, item_id, quantity, options, shipping_payment_type, shipping_group_code, addition_item_flag, parent_item_id, set_item_flag, created_date, text_option) FROM stdin;
\.


--
-- Data for Name: op_cart_set; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_cart_set (cart_id, item_id, quantity, options, parent_cart_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_cash_receipt; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_cash_receipt (cash_receipt_id, cash_receipt_pg_service_type, order_code, order_sequence, payment_sequence, cash_receipt_amount, cash_receipt_tax_free_amount, cash_receipt_name, cash_receipt_code, cash_receipt_type, cash_receipt_issue_number, cash_receipt_issue_date, cash_receipt_status_code, created_date) FROM stdin;
\.


--
-- Data for Name: op_cashbill; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_cashbill (id, cashbill_code, cashbill_type, created_by, created_date, customer_name, order_code, pg_service) FROM stdin;
\.


--
-- Data for Name: op_cashbill_issue; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_cashbill_issue (id, amount, canceled_date, cashbill_id, cashbill_issue_type, cashbill_status, created_date, issued_date, item_name, mgt_key, tax_type, update_by, updated_date) FROM stdin;
\.


--
-- Data for Name: op_claim_memo; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_claim_memo (claim_memo_id, user_id, user_name, order_code, claim_status, memo, manager_user_id, manager_login_id, data_status_code, created_date) FROM stdin;
\.


--
-- Data for Name: op_common_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code) FROM stdin;
ORDER_STATUS	ko	PENDING	처리중	\N	1	Y	\N	\N	\N	\N
ORDER_STATUS	ko	CONFIRMED	주문확정	\N	2	Y	\N	\N	\N	\N
ORDER_STATUS	ko	CANCELLED	취소됨	\N	3	Y	\N	\N	\N	\N
ORDER_STATUS	ko	CLAIM_REQUESTED	반품/교환 신청중	\N	4	Y	\N	\N	\N	\N
CLAIM_TYPE	ko	RETURN	반품	\N	1	Y	\N	\N	\N	\N
CLAIM_TYPE	ko	EXCHANGE	교환	\N	2	Y	\N	\N	\N	\N
CLAIM_STATUS	ko	REQUESTED	접수	\N	1	Y	\N	\N	\N	\N
CLAIM_STATUS	ko	APPROVED	승인	\N	2	Y	\N	\N	\N	\N
CLAIM_STATUS	ko	REJECTED	거절	\N	3	Y	\N	\N	\N	\N
CLAIM_STATUS	ko	COMPLETED	처리완료	\N	4	Y	\N	\N	\N	\N
DELIVERY_STATUS	ko	SHIPPED	발송완료	\N	1	Y	\N	\N	\N	\N
DELIVERY_STATUS	ko	IN_TRANSIT	배송중	\N	2	Y	\N	\N	\N	\N
DELIVERY_STATUS	ko	DELIVERED	배송완료	\N	3	Y	\N	\N	\N	\N
DELIVERY_STATUS	ko	CONFIRMED	구매확정	\N	4	Y	\N	\N	\N	\N
DELIVERY_CARRIER	ko	CJ	CJ대한통운	\N	1	Y	\N	\N	\N	\N
DELIVERY_CARRIER	ko	POST	우체국택배	\N	2	Y	\N	\N	\N	\N
DELIVERY_CARRIER	ko	HANJIN	한진택배	\N	3	Y	\N	\N	\N	\N
DELIVERY_CARRIER	ko	LOTTE	롯데택배	\N	4	Y	\N	\N	\N	\N
\.


--
-- Data for Name: op_condition; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_condition (condition_id, category_code, condition_title, use_yn, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_condition_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_condition_detail (detail_id, condition_id, detail_title, use_yn, ordering, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_coupon; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon (coupon_id, coupon_type, coupon_name, coupon_comment, coupon_issue_type, coupon_issue_start_date, coupon_issue_end_date, coupon_apply_type, coupon_apply_day, coupon_apply_start_date, coupon_apply_end_date, coupon_target_time_type, coupon_target_user_type, coupon_target_user_level, coupon_target_user, coupon_pay_restriction, coupon_concurrently, coupon_pay_type, coupon_pay, coupon_discount_limit_price, coupon_target_item_type, coupon_target_item, coupon_flag, coupon_offline_flag, coupon_birthday, data_status_code, coupon_download_limit, coupon_download_user_limit, coupon_mulitple_download_flag, created_date, update_user_name, updated_date, direct_input_flag, direct_input_value) FROM stdin;
\.


--
-- Data for Name: op_coupon_offline; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon_offline (coupon_offline_id, coupon_id, user_id, coupon_offline_code, coupon_used_flag, coupon_used_date, published_date) FROM stdin;
1	2	0	ZSRP-FV5T-E6L8	N	\N	20260825142023
2	2	0	4T3P-5S2W-F7YJ	N	\N	20260825142023
3	2	0	8BBX-F43W-8ZXF	N	\N	20260825142023
4	2	0	3VDE-BBSC-26PV	N	\N	20260825142023
5	2	0	YSN4-WH7U-6U7H	N	\N	20260825142023
\.


--
-- Data for Name: op_coupon_regular; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon_regular (coupon_id, coupon_type, coupon_name, coupon_comment, coupon_issue_type, coupon_issue_start_date, coupon_issue_end_date, coupon_target_time_type, coupon_target_user_type, coupon_target_user_level, coupon_target_user, coupon_pay_restriction, coupon_concurrently, coupon_pay_type, coupon_pay, coupon_discount_limit_price, coupon_target_item_type, coupon_target_item, coupon_flag, coupon_birthday, data_status_code, coupon_download_limit, coupon_download_user_limit, coupon_mulitple_download_flag, created_date, update_user_name, updated_date) FROM stdin;
\.


--
-- Data for Name: op_coupon_target_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon_target_item (item_id, coupon_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_coupon_target_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon_target_user (user_id, coupon_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_coupon_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_coupon_user (coupon_user_id, coupon_id, user_id, coupon_type, coupon_name, coupon_comment, coupon_apply_type, coupon_apply_start_date, coupon_apply_end_date, coupon_pay_restriction, coupon_concurrently, coupon_pay_type, coupon_pay, coupon_discount_limit_price, coupon_target_item_type, data_status_code, coupon_download_date, coupon_used_date, order_code, order_sequence, item_sequence, discount_amount, created_date) FROM stdin;
\.


--
-- Data for Name: op_delivery_company; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_delivery_company (delivery_company_id, delivery_company_name, tel_number, delivery_company_url, send_flag, delivery_number_parameter, use_flag) FROM stdin;
\.


--
-- Data for Name: op_delivery_hope; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_delivery_hope (delivery_hope_id, delivery_hope_time, delivery_hope_index) FROM stdin;
\.


--
-- Data for Name: op_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order (order_code, order_sequence, order_total_amount, pay_amount, user_id, login_id, buyer_name, phone, mobile, email, zipcode, new_zipcode, sido, sigungu, eupmyeondong, address, address_detail, data_status_code, ip, return_bank_name, return_bank_in_name, return_virtual_no, order_admin_memo, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_add_payment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_add_payment (add_payment_id, seller_id, order_code, order_sequence, refund_code, issue_code, subject, add_payment_type, amount, sales_date, sales_cancel_date, remittance_id, remittance_amount, remittance_expected_date, remittance_date, remittance_status_code, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_admin; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_admin (work_date, work_sequence, insert_manager_name, data_status_code, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_admin_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_admin_detail (work_date, work_sequence, item_sequence, order_group_code, template_version, excel_data, data_status_code, sale_price, update_manager_name, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_batch_target; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_batch_target (batch_key, order_code, start_date, batch_type, target_index, target_code, error_message) FROM stdin;
\.


--
-- Data for Name: op_order_cancel_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_cancel_apply (claim_code, order_code, order_sequence, item_sequence, parent_item_sequence, cancel_apply_date, refund_code, claim_status, claim_apply_subject, claim_apply_quantity, cancel_reason, cancel_reason_text, cancel_reason_detail, cancel_memo, cancel_refusal_reason_text, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_cancel_fail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_cancel_fail (id, approval_type, cancel_amount, cancel_reason, cancel_requester, order_code, pay_date, pg_key, pg_service_type, tax_amount, tax_free_amount) FROM stdin;
\.


--
-- Data for Name: op_order_claim_shipping; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_claim_shipping (order_code, order_sequence, seller_id, refund_code, amount, remittance_amount, manager_user_name, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_exchange_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_exchange_apply (claim_code, order_code, order_sequence, item_sequence, parent_item_sequence, exchange_apply_date, claim_apply_quantity, shipment_return_seller_id, shipment_return_id, claim_status, claim_apply_subject, exchange_receive_name, exchange_receive_phone, exchange_receive_mobile, exchange_receive_zipcode, exchange_receive_sido, exchange_receive_sigungu, exchange_receive_eupmyeondong, exchange_receive_address, exchange_receive_address2, exchange_delivery_company_id, exchange_delivery_company_name, exchange_delivery_number, exchange_delivery_company_url, exchange_delivery_date, exchange_shipping_ask_type, exchange_shipping_number, exchange_shipping_company_name, exchange_shipping_company_url, exchange_shipping_start_date, exchange_reason, exchange_reason_text, exchange_reason_detail, exchange_memo, exchange_refusal_reason_text, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_gift_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_gift_item (id, created, created_by, updated, updated_by, gift_group_id, gift_item_code, gift_item_id, gift_item_name, gift_order_status, gift_sequence, group_type, image, item_sequence, order_code, order_sequence, price, seller_id, valid_end_date, valid_start_date) FROM stdin;
\.


--
-- Data for Name: op_order_gift_item_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_gift_item_log (id, created, created_by, updated, updated_by, gift_group_id, gift_item_code, gift_item_id, gift_item_name, gift_order_status, gift_sequence, group_type, item_sequence, order_code, order_sequence, org_gift_order_status, user_type) FROM stdin;
\.


--
-- Data for Name: op_order_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item (order_code, order_sequence, item_sequence, order_status, shipping_sequence, shipping_info_sequence, device_type) FROM stdin;
\.


--
-- Data for Name: op_order_item_buy_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_buy_temp (user_id, session_id, order_code, item_sequence, shipping_index, item_id, quantity, options, coupon_user_id, add_coupon_user_id, shipping_payment_type, addition_item_flag, parent_item_sequence, parent_item_id, escrow_status, created_date, campaign_code, set_item_flag) FROM stdin;
\.


--
-- Data for Name: op_order_item_hold; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_hold (order_code, order_sequence, item_sequence, order_status, shipping_sequence, shipping_info_sequence, device_type, addition_item_flag, parent_item_sequence, user_id, guest_flag, seller_id, category_team_id, category_group_id, category_id, shipment_id, shipment_return_id, coupon_user_id, add_coupon_user_id, item_id, item_code, item_user_code, item_name, free_gift_name, image_src, shipment_group_code, shipping_return, purchase_price, cost_price, price, option_price, sale_price, quantity, claim_quantity, order_quantity, coupon_discount_price, spot_sale_flag, spot_type, spot_discount_price, level_id, level_name, user_level_discount_rate, user_level_discount_price, escrow_status, tax_type, commission_base_price, commission_rate, commission_price, commission_type, supply_price, remittance_id, remittance_type, remittance_day, remittance_expected_date, remittance_date, remittance_status_code, admin_discount_price, admin_discount_detail, seller_discount_price, seller_discount_detail, brand, options, delivery_type, delivery_company_id, delivery_company_name, delivery_number, delivery_company_url, shipment_return_type, point_type, point_config_type, point, point_log, earn_point, seller_point, earn_point_flag, return_point_flag, cancel_flag, refund_status, item_return_flag, revenue_sales_status, pay_date, shipping_ready_date, shipping_date, shipping_finish_date, cancel_request_date, cancel_request_finish_date, sales_date, sales_cancel_date, confirm_date, return_request_date, return_request_finish_date, exchange_request_date, updated_admin_user_name, created_date, campaign_code, set_item_flag, set_discount_type, set_discount_price, locgov_code, mobile_number, etc_amt, filler1, filler2, filler3, filler4, filler5, filler6, filler7, filler8, filler9, filler10, hold_confirm_status, hold_confirm_date_seller, hold_confirm_date_mgr, hold_reject_date_mgr, holid_confirm_seller_user_id, holid_confirm_mgr_id, holid_reject_mgr_id) FROM stdin;
\.


--
-- Data for Name: op_order_item_hold_hist; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_hold_hist (reg_seq, order_code, order_sequence, item_sequence, hold_confrim_status_bef, hold_confrim_status_aft, created_user_tp, created_user_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_item_set; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_set (order_code, order_sequence, item_sequence, set_item_sequence, user_id, guest_flag, seller_id, item_id, item_code, item_user_code, item_name, image_src, purchase_price, cost_price, price, option_price, sale_price, quantity, claim_quantity, order_quantity, spot_sale_flag, spot_type, spot_discount_price, tax_type, commission_base_price, commission_rate, commission_price, commission_type, supply_price, admin_discount_price, admin_discount_detail, seller_discount_price, seller_discount_detail, brand, options, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_item_set_buy_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_set_buy_temp (user_id, session_id, order_code, item_sequence, item_id, quantity, options, set_item_sequence, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_item_set_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_set_temp (user_id, session_id, item_sequence, item_id, quantity, options, set_item_sequence, created_date, text_option) FROM stdin;
\.


--
-- Data for Name: op_order_item_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_item_temp (user_id, session_id, item_sequence, item_id, quantity, options, coupon_user_id, add_coupon_user_id, shipping_payment_type, addition_item_flag, parent_item_sequence, parent_item_id, created_date, campaign_code, set_item_flag, text_option) FROM stdin;
\.


--
-- Data for Name: op_order_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_log (id, created_at, updated_at, created_at_text, created_by, ip, item_name, item_sequence, log_type, order_code, order_sequence, order_status, org_order_status, updated_at_text, updated_by, user_type) FROM stdin;
\.


--
-- Data for Name: op_order_payment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_payment (order_code, order_sequence, payment_sequence, payment_type, order_pg_data_id, approval_type, device_type, card_easy_type, bank_virtual_no, bank_in_name, bank_date, amount, tax_free_amount, cancel_amount, remaining_amount, pay_date, now_payment_flag, created_date, refund_flag, payment_summary) FROM stdin;
\.


--
-- Data for Name: op_order_payment_buy_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_payment_buy_temp (order_code, approval_type, service_type, amount, tax_free_amount, bank_virtual_no, bank_in_name, bank_date, service_mid, service_key, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_pg_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_pg_data (order_pg_data_id, order_code, pg_service_type, pg_service_mid, pg_service_key, pg_payment_type, pg_key, pg_auth_code, pg_proc_info, part_cancel_flag, part_cancel_detail, pg_amount, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_refund; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_refund (refund_code, refund_date, order_code, order_sequence, refund_status_code, request_manager_user_name, process_manager_user_name, return_bank_name, return_virtual_no, return_bank_in_name, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_order_return_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_return_apply (claim_code, order_code, order_sequence, item_sequence, parent_item_sequence, return_apply_date, refund_code, shipment_return_seller_id, shipment_return_id, claim_apply_quantity, claim_status, return_reason, return_reason_text, return_reason_detail, claim_apply_subject, previous_order_status, return_reserve_name, return_reserve_phone, return_reserve_mobile, return_reserve_zipcode, return_reserve_sido, return_reserve_sigungu, return_reserve_eupmyeondong, return_reserve_address, return_reserve_address2, return_shipping_ask_type, return_shipping_number, return_shipping_company_name, return_shipping_company_url, collection_shipping_amount, return_memo, return_refusal_reason_text, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_sales; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_sales (user_id, pay_date, amount) FROM stdin;
\.


--
-- Data for Name: op_order_send_message_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_send_message_log (order_code, template_id, delivery_number, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_set_cancel_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_set_cancel_apply (claim_code, order_code, order_sequence, item_sequence, set_item_sequence, set_claim_code, cancel_apply_date, refund_code, claim_status, claim_apply_quantity, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_set_exchange_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_set_exchange_apply (claim_code, order_code, order_sequence, item_sequence, set_item_sequence, set_claim_code, exchange_apply_date, claim_apply_quantity, claim_status, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_set_return_apply; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_set_return_apply (claim_code, order_code, order_sequence, item_sequence, set_item_sequence, set_claim_code, return_apply_date, refund_code, claim_apply_quantity, claim_status, updated_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_shipping; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_shipping (order_code, order_sequence, shipping_sequence, seller_id, shipping_type, shipment_group_code, shipping_group_code, island_type, shipping_item_count, shipping, shipping_extra_charge1, shipping_extra_charge2, shipping_free_amount, real_shipping, previous_real_shipping, discount_shipping, shipping_coupon_count, shipping_payment_type, pay_shipping, previous_pay_shipping, return_shipping, previous_return_shipping, return_flag, remittance_id, remittance_amount, previous_remittance_amount, remittance_expected_date, remittance_status_code, remittance_date) FROM stdin;
\.


--
-- Data for Name: op_order_shipping_buy_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_shipping_buy_temp (user_id, session_id, order_code, shipping_index, receive_name, receive_mobile, receive_phone, receive_zipcode, receive_new_zipcode, receive_sido, receive_sigungu, receive_eupmyeondong, receive_address, receive_address_detail, content, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_shipping_cp_buy_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_shipping_cp_buy_temp (order_code, user_id, shipping_group_code, use_coupon_count, discount_amount, created_date) FROM stdin;
\.


--
-- Data for Name: op_order_shipping_info; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_shipping_info (order_code, order_sequence, shipping_info_sequence, receive_new_zipcode, receive_zipcode, receive_sido, receive_sigungu, receive_eupmyeondong, receive_address, receive_address_detail, receive_name, receive_phone, receive_mobile, memo, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_order_temp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_order_temp (user_id, session_id, order_code, user_name, point_discount_amount, item_coupon_discount_amount, cart_coupon_discount_amount, shipping_coupon_use_count, shipping_coupon_discount_amt, cart_coupon_use_data, order_pay_amount, company_name, email, mobile, phone, zipcode, new_zipcode, sido, sigungu, eupmyeondong, address, address_detail, delivery_req_day, delivery_req_hour, receipt_name, cash_receipt_type, cash_receipt_code, cashbill_type, cashbill_code, save_delivery_flag, save_delivery_name, device_type, created_date) FROM stdin;
\.


--
-- Data for Name: op_remittance; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_remittance (remittance_id, remittance_status_code, confirm_date, confirm_date_seller, payment_date, finishing_date, seller_id, finishing_amount, finishing_manager_name, bank_name, bank_in_name, bank_account_number, created_date, remittance_ym) FROM stdin;
\.


--
-- Data for Name: op_remittance_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_remittance_detail (remittance_id, remittance_detail_id, order_code, remittance_key, item_type, item_user_code, item_name, options, sale_price, seller_discount_price, seller_discount_detail, seller_point, set_item_flag, set_discount_price, commission_base_price, commission_rate, commission_price, commission_type, supply_price, remittance_price, quantity, created_date, order_sequence, item_sequence, etc_amt) FROM stdin;
\.


--
-- Data for Name: op_shipment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_shipment (shipment_id, seller_id, address_name, name, telephone_number, zipcode, address, address_detail, default_address_flag, shipping, shipping_free_amount, shipping_extra_charge1, shipping_extra_charge2, shipment_group_code, created_date, updated_date) FROM stdin;
\.


--
-- Data for Name: op_shipment_return; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_shipment_return (shipment_return_id, seller_id, address_name, name, telephone_number, zipcode, address, address_detail, default_address_flag, created_date) FROM stdin;
\.


--
-- Name: od_cart_item_cart_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.od_cart_item_cart_item_id_seq', 1, true);


--
-- Name: od_claim_claim_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.od_claim_claim_id_seq', 1, false);


--
-- Name: op_coupon_coupon_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_coupon_coupon_id_seq', 2, true);


--
-- Name: op_coupon_offline_coupon_offline_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_coupon_offline_coupon_offline_id_seq', 5, true);


--
-- Name: op_coupon_regular_coupon_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_coupon_regular_coupon_id_seq', 1, true);


--
-- Name: op_coupon_user_coupon_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_coupon_user_coupon_user_id_seq', 1000, true);


--
-- Name: op_order_item_hold_hist_reg_seq_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_order_item_hold_hist_reg_seq_seq', 1000, false);


--
-- Name: g_agency_private_key g_agency_private_key_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_agency_private_key
    ADD CONSTRAINT g_agency_private_key_pkey PRIMARY KEY (user_session_id);


--
-- Name: g_order_agency_order_log g_order_agency_order_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_order_agency_order_log
    ADD CONSTRAINT g_order_agency_order_log_pkey PRIMARY KEY (order_code, order_sequence, item_sequence);


--
-- Name: g_remittance_file g_remittance_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_remittance_file
    ADD CONSTRAINT g_remittance_file_pkey PRIMARY KEY (remittance_id, file_seq);


--
-- Name: od_cart_item od_cart_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_cart_item
    ADD CONSTRAINT od_cart_item_pkey PRIMARY KEY (cart_item_id);


--
-- Name: od_cart_item od_cart_item_user_id_item_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_cart_item
    ADD CONSTRAINT od_cart_item_user_id_item_id_key UNIQUE (user_id, item_id);


--
-- Name: od_claim od_claim_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_claim
    ADD CONSTRAINT od_claim_pkey PRIMARY KEY (claim_id);


--
-- Name: od_order od_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.od_order
    ADD CONSTRAINT od_order_pkey PRIMARY KEY (order_id);


--
-- Name: op_cart op_cart_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cart
    ADD CONSTRAINT op_cart_pkey PRIMARY KEY (cart_id);


--
-- Name: op_cart_set op_cart_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cart_set
    ADD CONSTRAINT op_cart_set_pkey PRIMARY KEY (cart_id);


--
-- Name: op_cash_receipt op_cash_receipt_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cash_receipt
    ADD CONSTRAINT op_cash_receipt_pkey PRIMARY KEY (cash_receipt_id);


--
-- Name: op_cashbill_issue op_cashbill_issue_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cashbill_issue
    ADD CONSTRAINT op_cashbill_issue_pkey PRIMARY KEY (id);


--
-- Name: op_cashbill op_cashbill_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cashbill
    ADD CONSTRAINT op_cashbill_pkey PRIMARY KEY (id);


--
-- Name: op_claim_memo op_claim_memo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_claim_memo
    ADD CONSTRAINT op_claim_memo_pkey PRIMARY KEY (claim_memo_id);


--
-- Name: op_common_code op_common_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_common_code
    ADD CONSTRAINT op_common_code_pkey PRIMARY KEY (code_type, code_language, id);


--
-- Name: op_condition_detail op_condition_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_condition_detail
    ADD CONSTRAINT op_condition_detail_pkey PRIMARY KEY (detail_id);


--
-- Name: op_condition op_condition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_condition
    ADD CONSTRAINT op_condition_pkey PRIMARY KEY (condition_id);


--
-- Name: op_coupon_offline op_coupon_offline_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_offline
    ADD CONSTRAINT op_coupon_offline_pkey PRIMARY KEY (coupon_offline_id);


--
-- Name: op_coupon op_coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon
    ADD CONSTRAINT op_coupon_pkey PRIMARY KEY (coupon_id);


--
-- Name: op_coupon_regular op_coupon_regular_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_regular
    ADD CONSTRAINT op_coupon_regular_pkey PRIMARY KEY (coupon_id);


--
-- Name: op_coupon_target_item op_coupon_target_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_target_item
    ADD CONSTRAINT op_coupon_target_item_pkey PRIMARY KEY (item_id, coupon_id);


--
-- Name: op_coupon_target_user op_coupon_target_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_target_user
    ADD CONSTRAINT op_coupon_target_user_pkey PRIMARY KEY (user_id, coupon_id);


--
-- Name: op_coupon_user op_coupon_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_coupon_user
    ADD CONSTRAINT op_coupon_user_pkey PRIMARY KEY (coupon_user_id);


--
-- Name: op_delivery_company op_delivery_company_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_delivery_company
    ADD CONSTRAINT op_delivery_company_pkey PRIMARY KEY (delivery_company_id);


--
-- Name: op_delivery_hope op_delivery_hope_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_delivery_hope
    ADD CONSTRAINT op_delivery_hope_pkey PRIMARY KEY (delivery_hope_id);


--
-- Name: op_order_add_payment op_order_add_payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_add_payment
    ADD CONSTRAINT op_order_add_payment_pkey PRIMARY KEY (add_payment_id);


--
-- Name: op_order_admin_detail op_order_admin_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_admin_detail
    ADD CONSTRAINT op_order_admin_detail_pkey PRIMARY KEY (work_date, work_sequence, item_sequence);


--
-- Name: op_order_admin op_order_admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_admin
    ADD CONSTRAINT op_order_admin_pkey PRIMARY KEY (work_date, work_sequence);


--
-- Name: op_order_cancel_apply op_order_cancel_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_cancel_apply
    ADD CONSTRAINT op_order_cancel_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_cancel_fail op_order_cancel_fail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_cancel_fail
    ADD CONSTRAINT op_order_cancel_fail_pkey PRIMARY KEY (id);


--
-- Name: op_order_claim_shipping op_order_claim_shipping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_claim_shipping
    ADD CONSTRAINT op_order_claim_shipping_pkey PRIMARY KEY (order_code, order_sequence, seller_id, refund_code);


--
-- Name: op_order_exchange_apply op_order_exchange_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_exchange_apply
    ADD CONSTRAINT op_order_exchange_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_gift_item_log op_order_gift_item_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_gift_item_log
    ADD CONSTRAINT op_order_gift_item_log_pkey PRIMARY KEY (id);


--
-- Name: op_order_gift_item op_order_gift_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_gift_item
    ADD CONSTRAINT op_order_gift_item_pkey PRIMARY KEY (id);


--
-- Name: op_order_item_buy_temp op_order_item_buy_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_buy_temp
    ADD CONSTRAINT op_order_item_buy_temp_pkey PRIMARY KEY (user_id, session_id, order_code, item_sequence);


--
-- Name: op_order_item_hold_hist op_order_item_hold_hist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_hold_hist
    ADD CONSTRAINT op_order_item_hold_hist_pkey PRIMARY KEY (reg_seq);


--
-- Name: op_order_item_hold op_order_item_hold_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_hold
    ADD CONSTRAINT op_order_item_hold_pkey PRIMARY KEY (order_code, order_sequence, item_sequence);


--
-- Name: op_order_item_set op_order_item_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_set
    ADD CONSTRAINT op_order_item_set_pkey PRIMARY KEY (order_code, order_sequence, item_sequence, set_item_sequence);


--
-- Name: op_order_item_temp op_order_item_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_item_temp
    ADD CONSTRAINT op_order_item_temp_pkey PRIMARY KEY (user_id, session_id, item_sequence, created_date);


--
-- Name: op_order_log op_order_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_log
    ADD CONSTRAINT op_order_log_pkey PRIMARY KEY (id);


--
-- Name: op_order_payment_buy_temp op_order_payment_buy_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_payment_buy_temp
    ADD CONSTRAINT op_order_payment_buy_temp_pkey PRIMARY KEY (order_code);


--
-- Name: op_order_payment op_order_payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_payment
    ADD CONSTRAINT op_order_payment_pkey PRIMARY KEY (order_code, order_sequence, payment_sequence);


--
-- Name: op_order_pg_data op_order_pg_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_pg_data
    ADD CONSTRAINT op_order_pg_data_pkey PRIMARY KEY (order_pg_data_id);


--
-- Name: op_order op_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order
    ADD CONSTRAINT op_order_pkey PRIMARY KEY (order_code, order_sequence);


--
-- Name: op_order_refund op_order_refund_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_refund
    ADD CONSTRAINT op_order_refund_pkey PRIMARY KEY (refund_code);


--
-- Name: op_order_return_apply op_order_return_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_return_apply
    ADD CONSTRAINT op_order_return_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_set_cancel_apply op_order_set_cancel_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_set_cancel_apply
    ADD CONSTRAINT op_order_set_cancel_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_set_exchange_apply op_order_set_exchange_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_set_exchange_apply
    ADD CONSTRAINT op_order_set_exchange_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_set_return_apply op_order_set_return_apply_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_set_return_apply
    ADD CONSTRAINT op_order_set_return_apply_pkey PRIMARY KEY (claim_code);


--
-- Name: op_order_shipping_buy_temp op_order_shipping_buy_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_shipping_buy_temp
    ADD CONSTRAINT op_order_shipping_buy_temp_pkey PRIMARY KEY (user_id, session_id, order_code, shipping_index);


--
-- Name: op_order_shipping_cp_buy_temp op_order_shipping_cp_buy_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_shipping_cp_buy_temp
    ADD CONSTRAINT op_order_shipping_cp_buy_temp_pkey PRIMARY KEY (order_code, user_id, shipping_group_code);


--
-- Name: op_order_shipping_info op_order_shipping_info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_shipping_info
    ADD CONSTRAINT op_order_shipping_info_pkey PRIMARY KEY (order_code, order_sequence, shipping_info_sequence);


--
-- Name: op_order_shipping op_order_shipping_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_shipping
    ADD CONSTRAINT op_order_shipping_pkey PRIMARY KEY (order_code, order_sequence, shipping_sequence);


--
-- Name: op_order_temp op_order_temp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_order_temp
    ADD CONSTRAINT op_order_temp_pkey PRIMARY KEY (user_id, session_id, order_code);


--
-- Name: op_remittance_detail op_remittance_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_remittance_detail
    ADD CONSTRAINT op_remittance_detail_pkey PRIMARY KEY (remittance_id, remittance_detail_id);


--
-- Name: op_remittance op_remittance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_remittance
    ADD CONSTRAINT op_remittance_pkey PRIMARY KEY (remittance_id);


--
-- Name: op_shipment op_shipment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_shipment
    ADD CONSTRAINT op_shipment_pkey PRIMARY KEY (shipment_id);


--
-- Name: op_shipment_return op_shipment_return_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_shipment_return
    ADD CONSTRAINT op_shipment_return_pkey PRIMARY KEY (shipment_return_id);


--
-- Name: op_cart_set fk_op_cart_set_parent_cart_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cart_set
    ADD CONSTRAINT fk_op_cart_set_parent_cart_id FOREIGN KEY (parent_cart_id) REFERENCES public.op_cart(cart_id);


--
-- Name: op_cashbill_issue fk_op_cashbill_issue_cashbill_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_cashbill_issue
    ADD CONSTRAINT fk_op_cashbill_issue_cashbill_id FOREIGN KEY (cashbill_id) REFERENCES public.op_cashbill(id);


--
-- Name: TABLE g_agency_private_key; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_agency_private_key TO orderdb;


--
-- Name: TABLE g_order_agency_order_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_order_agency_order_log TO orderdb;


--
-- Name: TABLE g_remittance_file; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_remittance_file TO orderdb;


--
-- Name: TABLE od_cart_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.od_cart_item TO orderdb;


--
-- Name: SEQUENCE od_cart_item_cart_item_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.od_cart_item_cart_item_id_seq TO orderdb;


--
-- Name: TABLE od_claim; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.od_claim TO orderdb;


--
-- Name: SEQUENCE od_claim_claim_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.od_claim_claim_id_seq TO orderdb;


--
-- Name: TABLE od_order; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.od_order TO orderdb;


--
-- Name: TABLE op_cart; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_cart TO orderdb;


--
-- Name: TABLE op_cart_set; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_cart_set TO orderdb;


--
-- Name: TABLE op_cash_receipt; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_cash_receipt TO orderdb;


--
-- Name: TABLE op_cashbill; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_cashbill TO orderdb;


--
-- Name: TABLE op_cashbill_issue; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_cashbill_issue TO orderdb;


--
-- Name: TABLE op_claim_memo; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_claim_memo TO orderdb;


--
-- Name: TABLE op_common_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_common_code TO orderdb;


--
-- Name: TABLE op_condition; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_condition TO orderdb;


--
-- Name: TABLE op_condition_detail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_condition_detail TO orderdb;


--
-- Name: TABLE op_coupon; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon TO orderdb;


--
-- Name: SEQUENCE op_coupon_coupon_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_coupon_coupon_id_seq TO orderdb;


--
-- Name: TABLE op_coupon_offline; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon_offline TO orderdb;


--
-- Name: SEQUENCE op_coupon_offline_coupon_offline_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_coupon_offline_coupon_offline_id_seq TO orderdb;


--
-- Name: TABLE op_coupon_regular; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon_regular TO orderdb;


--
-- Name: SEQUENCE op_coupon_regular_coupon_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_coupon_regular_coupon_id_seq TO orderdb;


--
-- Name: TABLE op_coupon_target_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon_target_item TO orderdb;


--
-- Name: TABLE op_coupon_target_user; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon_target_user TO orderdb;


--
-- Name: TABLE op_coupon_user; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_coupon_user TO orderdb;


--
-- Name: SEQUENCE op_coupon_user_coupon_user_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_coupon_user_coupon_user_id_seq TO orderdb;


--
-- Name: TABLE op_delivery_company; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_delivery_company TO orderdb;


--
-- Name: TABLE op_delivery_hope; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_delivery_hope TO orderdb;


--
-- Name: TABLE op_order; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order TO orderdb;


--
-- Name: TABLE op_order_add_payment; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_add_payment TO orderdb;


--
-- Name: TABLE op_order_admin; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_admin TO orderdb;


--
-- Name: TABLE op_order_admin_detail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_admin_detail TO orderdb;


--
-- Name: TABLE op_order_batch_target; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_batch_target TO orderdb;


--
-- Name: TABLE op_order_cancel_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_cancel_apply TO orderdb;


--
-- Name: TABLE op_order_cancel_fail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_cancel_fail TO orderdb;


--
-- Name: TABLE op_order_claim_shipping; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_claim_shipping TO orderdb;


--
-- Name: TABLE op_order_exchange_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_exchange_apply TO orderdb;


--
-- Name: TABLE op_order_gift_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_gift_item TO orderdb;


--
-- Name: TABLE op_order_gift_item_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_gift_item_log TO orderdb;


--
-- Name: TABLE op_order_item; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item TO orderdb;


--
-- Name: TABLE op_order_item_buy_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_buy_temp TO orderdb;


--
-- Name: TABLE op_order_item_hold; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_hold TO orderdb;


--
-- Name: TABLE op_order_item_hold_hist; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_hold_hist TO orderdb;


--
-- Name: SEQUENCE op_order_item_hold_hist_reg_seq_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_order_item_hold_hist_reg_seq_seq TO orderdb;


--
-- Name: TABLE op_order_item_set; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_set TO orderdb;


--
-- Name: TABLE op_order_item_set_buy_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_set_buy_temp TO orderdb;


--
-- Name: TABLE op_order_item_set_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_set_temp TO orderdb;


--
-- Name: TABLE op_order_item_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_item_temp TO orderdb;


--
-- Name: TABLE op_order_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_log TO orderdb;


--
-- Name: TABLE op_order_payment; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_payment TO orderdb;


--
-- Name: TABLE op_order_payment_buy_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_payment_buy_temp TO orderdb;


--
-- Name: TABLE op_order_pg_data; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_pg_data TO orderdb;


--
-- Name: TABLE op_order_refund; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_refund TO orderdb;


--
-- Name: TABLE op_order_return_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_return_apply TO orderdb;


--
-- Name: TABLE op_order_sales; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_sales TO orderdb;


--
-- Name: TABLE op_order_send_message_log; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_send_message_log TO orderdb;


--
-- Name: TABLE op_order_set_cancel_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_set_cancel_apply TO orderdb;


--
-- Name: TABLE op_order_set_exchange_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_set_exchange_apply TO orderdb;


--
-- Name: TABLE op_order_set_return_apply; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_set_return_apply TO orderdb;


--
-- Name: TABLE op_order_shipping; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_shipping TO orderdb;


--
-- Name: TABLE op_order_shipping_buy_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_shipping_buy_temp TO orderdb;


--
-- Name: TABLE op_order_shipping_cp_buy_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_shipping_cp_buy_temp TO orderdb;


--
-- Name: TABLE op_order_shipping_info; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_shipping_info TO orderdb;


--
-- Name: TABLE op_order_temp; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_order_temp TO orderdb;


--
-- Name: TABLE op_remittance; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_remittance TO orderdb;


--
-- Name: TABLE op_remittance_detail; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_remittance_detail TO orderdb;


--
-- Name: TABLE op_shipment; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_shipment TO orderdb;


--
-- Name: TABLE op_shipment_return; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_shipment_return TO orderdb;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO orderdb;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO orderdb;


--
-- PostgreSQL database dump complete
--

\unrestrict 8YYnIu5ITg2eG8r48qITnw4rCKe6ij9ABiUeM3BW5HL4VTFEwhJoudY8APHC8eR

