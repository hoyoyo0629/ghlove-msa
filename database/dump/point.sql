--
-- PostgreSQL database dump
--

\restrict FDlxUUA9JfBZBIEBvPhLA38XjLtII2KeYbsO8DSfO7DlEjJPucIphAiTbs0UYFr

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

ALTER TABLE IF EXISTS ONLY public.op_attendance_event DROP CONSTRAINT IF EXISTS fk_op_attendance_event_attendance_id;
ALTER TABLE IF EXISTS ONLY public.op_attendance_config DROP CONSTRAINT IF EXISTS fk_op_attendance_config_attendance_id;
ALTER TABLE IF EXISTS ONLY public.op_attendance_check DROP CONSTRAINT IF EXISTS fk_op_attendance_check_attendance_id;
DROP INDEX IF EXISTS public.idx_pt_point_reservation_user;
DROP INDEX IF EXISTS public.idx_pt_point_ledger_user;
DROP INDEX IF EXISTS public.idx_pt_point_ledger_ref;
ALTER TABLE IF EXISTS ONLY public.pt_point_reservation DROP CONSTRAINT IF EXISTS pt_point_reservation_pkey;
ALTER TABLE IF EXISTS ONLY public.pt_point_ledger DROP CONSTRAINT IF EXISTS pt_point_ledger_pkey;
ALTER TABLE IF EXISTS ONLY public.pt_point_balance DROP CONSTRAINT IF EXISTS pt_point_balance_pkey;
ALTER TABLE IF EXISTS ONLY public.pt_locgov_point_rate DROP CONSTRAINT IF EXISTS pt_locgov_point_rate_pkey;
ALTER TABLE IF EXISTS ONLY public.op_point_used DROP CONSTRAINT IF EXISTS op_point_used_pkey;
ALTER TABLE IF EXISTS ONLY public.op_point DROP CONSTRAINT IF EXISTS op_point_pkey;
ALTER TABLE IF EXISTS ONLY public.op_point_config DROP CONSTRAINT IF EXISTS op_point_config_pkey;
ALTER TABLE IF EXISTS ONLY public.op_common_code DROP CONSTRAINT IF EXISTS op_common_code_pkey;
ALTER TABLE IF EXISTS ONLY public.op_attendance DROP CONSTRAINT IF EXISTS op_attendance_pkey;
ALTER TABLE IF EXISTS ONLY public.op_attendance_event DROP CONSTRAINT IF EXISTS op_attendance_event_pkey;
ALTER TABLE IF EXISTS ONLY public.op_attendance_config DROP CONSTRAINT IF EXISTS op_attendance_config_pkey;
ALTER TABLE IF EXISTS ONLY public.op_attendance_check DROP CONSTRAINT IF EXISTS op_attendance_check_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_use_point DROP CONSTRAINT IF EXISTS g_cntr_use_point_pkey;
ALTER TABLE IF EXISTS ONLY public.g_cntr_use_point_history DROP CONSTRAINT IF EXISTS g_cntr_use_point_history_pkey;
ALTER TABLE IF EXISTS public.pt_point_reservation ALTER COLUMN reservation_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.pt_point_ledger ALTER COLUMN ledger_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_point ALTER COLUMN point_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_attendance_event ALTER COLUMN attendance_event_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_attendance_config ALTER COLUMN attendance_config_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_attendance_check ALTER COLUMN attendance_check_id DROP DEFAULT;
ALTER TABLE IF EXISTS public.op_attendance ALTER COLUMN attendance_id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.pt_point_reservation_reservation_id_seq;
DROP TABLE IF EXISTS public.pt_point_reservation;
DROP SEQUENCE IF EXISTS public.pt_point_ledger_ledger_id_seq;
DROP TABLE IF EXISTS public.pt_point_ledger;
DROP TABLE IF EXISTS public.pt_point_balance;
DROP TABLE IF EXISTS public.pt_locgov_point_rate;
DROP TABLE IF EXISTS public.op_point_used;
DROP SEQUENCE IF EXISTS public.op_point_point_id_seq;
DROP TABLE IF EXISTS public.op_point_config;
DROP TABLE IF EXISTS public.op_point;
DROP TABLE IF EXISTS public.op_common_code;
DROP SEQUENCE IF EXISTS public.op_attendance_event_attendance_event_id_seq;
DROP TABLE IF EXISTS public.op_attendance_event;
DROP SEQUENCE IF EXISTS public.op_attendance_config_attendance_config_id_seq;
DROP TABLE IF EXISTS public.op_attendance_config;
DROP SEQUENCE IF EXISTS public.op_attendance_check_attendance_check_id_seq;
DROP TABLE IF EXISTS public.op_attendance_check;
DROP SEQUENCE IF EXISTS public.op_attendance_attendance_id_seq;
DROP TABLE IF EXISTS public.op_attendance;
DROP TABLE IF EXISTS public.g_cntr_use_point_history;
DROP TABLE IF EXISTS public.g_cntr_use_point;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: g_cntr_use_point; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_use_point (
    cntr_sn character varying(50) NOT NULL,
    use_sn integer NOT NULL,
    point_use_de character varying(8) NOT NULL,
    cntr_use_point bigint,
    user_id bigint NOT NULL,
    psitn_locgov_code character varying(10) NOT NULL,
    cntr_locgov_code character varying(10) NOT NULL,
    use_se_code character varying(10),
    use_cn character varying(2000),
    order_code character varying(50) NOT NULL,
    url_adres character varying(200),
    frst_register_id bigint,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL,
    last_updusr_id bigint,
    last_updt_pnttm timestamp without time zone
);


--
-- Name: g_cntr_use_point_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.g_cntr_use_point_history (
    cntr_sn character varying(50) NOT NULL,
    point_use_de character varying(8) NOT NULL,
    cntr_use_point bigint NOT NULL,
    user_id bigint NOT NULL,
    psitn_locgov_code character varying(10) NOT NULL,
    cntr_locgov_code character varying(10) NOT NULL,
    order_code character varying(50) NOT NULL,
    remark character varying(300) NOT NULL,
    frst_register_id bigint NOT NULL,
    frst_regist_pnttm timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: op_attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_attendance (
    attendance_id bigint NOT NULL,
    year character varying(4) NOT NULL,
    month character varying(2) NOT NULL,
    content_top character varying,
    content_bottom character varying,
    updated_by character varying(20),
    updated_date character varying(14),
    created_by character varying(20),
    created_date character varying(14)
);


--
-- Name: op_attendance_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_attendance_attendance_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_attendance_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_attendance_attendance_id_seq OWNED BY public.op_attendance.attendance_id;


--
-- Name: op_attendance_check; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_attendance_check (
    attendance_check_id bigint NOT NULL,
    attendance_id bigint,
    user_id bigint NOT NULL,
    checked_date character varying(8) NOT NULL,
    checked_time character varying(8) NOT NULL
);


--
-- Name: op_attendance_check_attendance_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_attendance_check_attendance_check_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_attendance_check_attendance_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_attendance_check_attendance_check_id_seq OWNED BY public.op_attendance_check.attendance_check_id;


--
-- Name: op_attendance_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_attendance_config (
    attendance_config_id bigint NOT NULL,
    attendance_id bigint NOT NULL,
    event_code character varying(20) NOT NULL,
    continue_yn character varying(1) NOT NULL,
    days integer NOT NULL,
    updated_by character varying(20),
    updated_date character varying(14),
    created_by character varying(20),
    created_date character varying(14)
);


--
-- Name: op_attendance_config_attendance_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_attendance_config_attendance_config_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_attendance_config_attendance_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_attendance_config_attendance_config_id_seq OWNED BY public.op_attendance_config.attendance_config_id;


--
-- Name: op_attendance_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_attendance_event (
    attendance_event_id bigint NOT NULL,
    attendance_id bigint,
    user_id bigint NOT NULL,
    event_code character varying(20) NOT NULL,
    continue_yn character varying(1),
    days integer DEFAULT 0 NOT NULL,
    checked_days integer DEFAULT 0 NOT NULL,
    success_yn character varying(1) DEFAULT 'N'::character varying NOT NULL,
    updated_date character varying(14) NOT NULL
);


--
-- Name: op_attendance_event_attendance_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_attendance_event_attendance_event_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_attendance_event_attendance_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_attendance_event_attendance_event_id_seq OWNED BY public.op_attendance_event.attendance_event_id;


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
-- Name: op_point; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_point (
    point_id integer NOT NULL,
    point_type character varying(20) DEFAULT 'point'::character varying NOT NULL,
    saved_type character varying(1) DEFAULT '2'::character varying NOT NULL,
    saved_year character varying(4),
    saved_month character varying(2),
    saved_point integer DEFAULT 0 NOT NULL,
    point integer DEFAULT 0 NOT NULL,
    reason character varying(255) NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    manager_user_id bigint DEFAULT 0 NOT NULL,
    order_code character varying(50),
    order_sequence integer,
    item_sequence integer,
    expiration_date character varying(8) NOT NULL,
    created_date character varying(14) NOT NULL
);


--
-- Name: op_point_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_point_config (
    point_config_id integer DEFAULT 0 NOT NULL,
    config_type character varying(1) NOT NULL,
    period_type character varying(1) NOT NULL,
    point_type character varying(1) NOT NULL,
    point double precision DEFAULT 0.00000000000000 NOT NULL,
    start_date character varying(8) NOT NULL,
    start_time character varying(2) NOT NULL,
    end_date character varying(8) NOT NULL,
    end_time character varying(2) NOT NULL,
    repeat_day character varying(2) NOT NULL,
    item_id integer DEFAULT 0,
    status_code character varying(1) DEFAULT '1'::character varying,
    created_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14)
);


--
-- Name: op_point_point_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.op_point_point_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: op_point_point_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.op_point_point_id_seq OWNED BY public.op_point.point_id;


--
-- Name: op_point_used; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.op_point_used (
    point_used_id integer DEFAULT 0 NOT NULL,
    point_used_group_id integer NOT NULL,
    point_id integer DEFAULT 0 NOT NULL,
    used_type character varying(1) DEFAULT '0'::character varying NOT NULL,
    point integer DEFAULT 0 NOT NULL,
    details character varying(255) NOT NULL,
    order_code character varying(50),
    manager_user_id bigint DEFAULT 0 NOT NULL,
    created_date character varying(14) NOT NULL,
    remaining_point integer DEFAULT 0
);


--
-- Name: pt_locgov_point_rate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pt_locgov_point_rate (
    stdr_year character varying(4) NOT NULL,
    locgov_code character varying(50) NOT NULL,
    point_rate numeric(5,2) NOT NULL
);


--
-- Name: pt_point_balance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pt_point_balance (
    user_id bigint NOT NULL,
    balance bigint DEFAULT 0 NOT NULL,
    updated_date timestamp without time zone
);


--
-- Name: pt_point_ledger; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pt_point_ledger (
    ledger_id bigint NOT NULL,
    user_id bigint NOT NULL,
    locgov_code character varying(50),
    txn_type character varying(20) NOT NULL,
    point_amount bigint NOT NULL,
    reason character varying(255),
    ref_key character varying(100),
    expiration_date character varying(8),
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    remaining_amount bigint
);


--
-- Name: pt_point_ledger_ledger_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pt_point_ledger_ledger_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pt_point_ledger_ledger_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pt_point_ledger_ledger_id_seq OWNED BY public.pt_point_ledger.ledger_id;


--
-- Name: pt_point_reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pt_point_reservation (
    reservation_id bigint NOT NULL,
    user_id bigint NOT NULL,
    amount bigint NOT NULL,
    ref_key character varying(50),
    reason character varying(255),
    status character varying(20) NOT NULL,
    created_date timestamp without time zone DEFAULT now() NOT NULL,
    resolved_date timestamp without time zone
);


--
-- Name: pt_point_reservation_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pt_point_reservation_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pt_point_reservation_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pt_point_reservation_reservation_id_seq OWNED BY public.pt_point_reservation.reservation_id;


--
-- Name: op_attendance attendance_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance ALTER COLUMN attendance_id SET DEFAULT nextval('public.op_attendance_attendance_id_seq'::regclass);


--
-- Name: op_attendance_check attendance_check_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_check ALTER COLUMN attendance_check_id SET DEFAULT nextval('public.op_attendance_check_attendance_check_id_seq'::regclass);


--
-- Name: op_attendance_config attendance_config_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_config ALTER COLUMN attendance_config_id SET DEFAULT nextval('public.op_attendance_config_attendance_config_id_seq'::regclass);


--
-- Name: op_attendance_event attendance_event_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_event ALTER COLUMN attendance_event_id SET DEFAULT nextval('public.op_attendance_event_attendance_event_id_seq'::regclass);


--
-- Name: op_point point_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_point ALTER COLUMN point_id SET DEFAULT nextval('public.op_point_point_id_seq'::regclass);


--
-- Name: pt_point_ledger ledger_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_point_ledger ALTER COLUMN ledger_id SET DEFAULT nextval('public.pt_point_ledger_ledger_id_seq'::regclass);


--
-- Name: pt_point_reservation reservation_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_point_reservation ALTER COLUMN reservation_id SET DEFAULT nextval('public.pt_point_reservation_reservation_id_seq'::regclass);


--
-- Data for Name: g_cntr_use_point; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_use_point (cntr_sn, use_sn, point_use_de, cntr_use_point, user_id, psitn_locgov_code, cntr_locgov_code, use_se_code, use_cn, order_code, url_adres, frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm) FROM stdin;
\.


--
-- Data for Name: g_cntr_use_point_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.g_cntr_use_point_history (cntr_sn, point_use_de, cntr_use_point, user_id, psitn_locgov_code, cntr_locgov_code, order_code, remark, frst_register_id, frst_regist_pnttm) FROM stdin;
\.


--
-- Data for Name: op_attendance; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_attendance (attendance_id, year, month, content_top, content_bottom, updated_by, updated_date, created_by, created_date) FROM stdin;
\.


--
-- Data for Name: op_attendance_check; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_attendance_check (attendance_check_id, attendance_id, user_id, checked_date, checked_time) FROM stdin;
\.


--
-- Data for Name: op_attendance_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_attendance_config (attendance_config_id, attendance_id, event_code, continue_yn, days, updated_by, updated_date, created_by, created_date) FROM stdin;
\.


--
-- Data for Name: op_attendance_event; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_attendance_event (attendance_event_id, attendance_id, user_id, event_code, continue_yn, days, checked_days, success_yn, updated_date) FROM stdin;
\.


--
-- Data for Name: op_common_code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code) FROM stdin;
PT_TXN_TYPE	ko	EARN	적립	\N	1	Y	\N	\N	\N	\N
PT_TXN_TYPE	ko	USE	사용	\N	2	Y	\N	\N	\N	\N
PT_TXN_TYPE	ko	REVERSE	취소회수	\N	3	Y	\N	\N	\N	\N
SYSTEM_CONFIG	ko	DEFAULT_POINT_RATE	기본 포인트 적립률(%, 지자체 설정 없을 때)	\N	1	Y	\N	30	\N	\N
SYSTEM_CONFIG	ko	POINT_VALID_DAYS	포인트 유효기간(일)	\N	2	Y	\N	1825	\N	\N
PT_TXN_TYPE	ko	RESTORE	주문취소 복원	\N	4	Y	\N	\N	\N	\N
PT_TXN_TYPE	ko	EXPIRE	유효기간 소멸	\N	5	Y	\N	\N	\N	\N
PT_RESERVATION_STATUS	ko	RESERVED	예약중	\N	1	Y	\N	\N	\N	\N
PT_RESERVATION_STATUS	ko	CONFIRMED	확정	\N	2	Y	\N	\N	\N	\N
PT_RESERVATION_STATUS	ko	RELEASED	해제됨	\N	3	Y	\N	\N	\N	\N
SYSTEM_CONFIG	ko	POINT_EXPIRY_NOTICE_DAYS	소멸 예정 안내 기준일(일)	\N	1	Y	\N	30	\N	\N
\.


--
-- Data for Name: op_point; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_point (point_id, point_type, saved_type, saved_year, saved_month, saved_point, point, reason, user_id, manager_user_id, order_code, order_sequence, item_sequence, expiration_date, created_date) FROM stdin;
\.


--
-- Data for Name: op_point_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_point_config (point_config_id, config_type, period_type, point_type, point, start_date, start_time, end_date, end_time, repeat_day, item_id, status_code, created_user_id, created_date) FROM stdin;
\.


--
-- Data for Name: op_point_used; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.op_point_used (point_used_id, point_used_group_id, point_id, used_type, point, details, order_code, manager_user_id, created_date, remaining_point) FROM stdin;
\.


--
-- Data for Name: pt_locgov_point_rate; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pt_locgov_point_rate (stdr_year, locgov_code, point_rate) FROM stdin;
2026	11230	30.00
2026	26350	30.00
2026	36110	35.00
2026	50000	30.00
2026	46150	40.00
\.


--
-- Data for Name: pt_point_balance; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pt_point_balance (user_id, balance, updated_date) FROM stdin;
1002	690000	2026-08-24 15:54:58.384199
1000	1047000	2026-08-24 15:54:58.413179
1001	1140000	2026-08-25 14:18:35.312609
\.


--
-- Data for Name: pt_point_ledger; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pt_point_ledger (ledger_id, user_id, locgov_code, txn_type, point_amount, reason, ref_key, expiration_date, created_date, remaining_amount) FROM stdin;
3	1000	11230	EARN	450000	기부포인트 적립 (30%)	D202501151030001000	20310823	2026-08-24 15:54:58.100666	450000
5	1002	46150	EARN	360000	기부포인트 적립 (30%)	D202503051030001002	20310823	2026-08-24 15:54:58.281823	360000
6	1000	50000	EARN	270000	기부포인트 적립 (30%)	D202504201030001000	20310823	2026-08-24 15:54:58.315191	270000
7	1001	11230	EARN	600000	기부포인트 적립 (30%)	D202505121030001001	20310823	2026-08-24 15:54:58.347783	600000
8	1002	26350	EARN	330000	기부포인트 적립 (30%)	D202506181030001002	20310823	2026-08-24 15:54:58.378095	330000
9	1000	46150	EARN	327000	기부포인트 적립 (30%)	D202507251030001000	20310823	2026-08-24 15:54:58.406811	327000
10	1001	11230	EARN	330000	기부포인트 적립 (30%)	D202508031030001001	20310823	2026-08-24 15:54:58.437992	330000
4	1001	26350	EARN	240000	기부포인트 적립 (30%)	D202502101030001001	20310823	2026-08-24 15:54:58.245219	210000
\.


--
-- Data for Name: pt_point_reservation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pt_point_reservation (reservation_id, user_id, amount, ref_key, reason, status, created_date, resolved_date) FROM stdin;
\.


--
-- Name: op_attendance_attendance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_attendance_attendance_id_seq', 1000, false);


--
-- Name: op_attendance_check_attendance_check_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_attendance_check_attendance_check_id_seq', 1000, false);


--
-- Name: op_attendance_config_attendance_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_attendance_config_attendance_config_id_seq', 1000, false);


--
-- Name: op_attendance_event_attendance_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_attendance_event_attendance_event_id_seq', 1000, false);


--
-- Name: op_point_point_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.op_point_point_id_seq', 1000, false);


--
-- Name: pt_point_ledger_ledger_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pt_point_ledger_ledger_id_seq', 11, true);


--
-- Name: pt_point_reservation_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pt_point_reservation_reservation_id_seq', 1, false);


--
-- Name: g_cntr_use_point_history g_cntr_use_point_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_use_point_history
    ADD CONSTRAINT g_cntr_use_point_history_pkey PRIMARY KEY (cntr_sn);


--
-- Name: g_cntr_use_point g_cntr_use_point_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.g_cntr_use_point
    ADD CONSTRAINT g_cntr_use_point_pkey PRIMARY KEY (cntr_sn, use_sn);


--
-- Name: op_attendance_check op_attendance_check_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_check
    ADD CONSTRAINT op_attendance_check_pkey PRIMARY KEY (attendance_check_id);


--
-- Name: op_attendance_config op_attendance_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_config
    ADD CONSTRAINT op_attendance_config_pkey PRIMARY KEY (attendance_config_id);


--
-- Name: op_attendance_event op_attendance_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_event
    ADD CONSTRAINT op_attendance_event_pkey PRIMARY KEY (attendance_event_id);


--
-- Name: op_attendance op_attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance
    ADD CONSTRAINT op_attendance_pkey PRIMARY KEY (attendance_id);


--
-- Name: op_common_code op_common_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_common_code
    ADD CONSTRAINT op_common_code_pkey PRIMARY KEY (code_type, code_language, id);


--
-- Name: op_point_config op_point_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_point_config
    ADD CONSTRAINT op_point_config_pkey PRIMARY KEY (point_config_id);


--
-- Name: op_point op_point_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_point
    ADD CONSTRAINT op_point_pkey PRIMARY KEY (point_id);


--
-- Name: op_point_used op_point_used_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_point_used
    ADD CONSTRAINT op_point_used_pkey PRIMARY KEY (point_used_id);


--
-- Name: pt_locgov_point_rate pt_locgov_point_rate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_locgov_point_rate
    ADD CONSTRAINT pt_locgov_point_rate_pkey PRIMARY KEY (stdr_year, locgov_code);


--
-- Name: pt_point_balance pt_point_balance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_point_balance
    ADD CONSTRAINT pt_point_balance_pkey PRIMARY KEY (user_id);


--
-- Name: pt_point_ledger pt_point_ledger_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_point_ledger
    ADD CONSTRAINT pt_point_ledger_pkey PRIMARY KEY (ledger_id);


--
-- Name: pt_point_reservation pt_point_reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pt_point_reservation
    ADD CONSTRAINT pt_point_reservation_pkey PRIMARY KEY (reservation_id);


--
-- Name: idx_pt_point_ledger_ref; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_point_ledger_ref ON public.pt_point_ledger USING btree (ref_key, txn_type);


--
-- Name: idx_pt_point_ledger_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_point_ledger_user ON public.pt_point_ledger USING btree (user_id);


--
-- Name: idx_pt_point_reservation_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pt_point_reservation_user ON public.pt_point_reservation USING btree (user_id, status);


--
-- Name: op_attendance_check fk_op_attendance_check_attendance_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_check
    ADD CONSTRAINT fk_op_attendance_check_attendance_id FOREIGN KEY (attendance_id) REFERENCES public.op_attendance(attendance_id);


--
-- Name: op_attendance_config fk_op_attendance_config_attendance_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_config
    ADD CONSTRAINT fk_op_attendance_config_attendance_id FOREIGN KEY (attendance_id) REFERENCES public.op_attendance(attendance_id);


--
-- Name: op_attendance_event fk_op_attendance_event_attendance_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.op_attendance_event
    ADD CONSTRAINT fk_op_attendance_event_attendance_id FOREIGN KEY (attendance_id) REFERENCES public.op_attendance(attendance_id);


--
-- Name: TABLE g_cntr_use_point; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_use_point TO point;


--
-- Name: TABLE g_cntr_use_point_history; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.g_cntr_use_point_history TO point;


--
-- Name: TABLE op_attendance; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_attendance TO point;


--
-- Name: SEQUENCE op_attendance_attendance_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_attendance_attendance_id_seq TO point;


--
-- Name: TABLE op_attendance_check; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_attendance_check TO point;


--
-- Name: SEQUENCE op_attendance_check_attendance_check_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_attendance_check_attendance_check_id_seq TO point;


--
-- Name: TABLE op_attendance_config; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_attendance_config TO point;


--
-- Name: SEQUENCE op_attendance_config_attendance_config_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_attendance_config_attendance_config_id_seq TO point;


--
-- Name: TABLE op_attendance_event; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_attendance_event TO point;


--
-- Name: SEQUENCE op_attendance_event_attendance_event_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_attendance_event_attendance_event_id_seq TO point;


--
-- Name: TABLE op_common_code; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_common_code TO point;


--
-- Name: TABLE op_point; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_point TO point;


--
-- Name: TABLE op_point_config; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_point_config TO point;


--
-- Name: SEQUENCE op_point_point_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.op_point_point_id_seq TO point;


--
-- Name: TABLE op_point_used; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.op_point_used TO point;


--
-- Name: TABLE pt_locgov_point_rate; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.pt_locgov_point_rate TO point;


--
-- Name: TABLE pt_point_balance; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.pt_point_balance TO point;


--
-- Name: TABLE pt_point_ledger; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.pt_point_ledger TO point;


--
-- Name: SEQUENCE pt_point_ledger_ledger_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.pt_point_ledger_ledger_id_seq TO point;


--
-- Name: TABLE pt_point_reservation; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON TABLE public.pt_point_reservation TO point;


--
-- Name: SEQUENCE pt_point_reservation_reservation_id_seq; Type: ACL; Schema: public; Owner: -
--

GRANT ALL ON SEQUENCE public.pt_point_reservation_reservation_id_seq TO point;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO point;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: -
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO point;


--
-- PostgreSQL database dump complete
--

\unrestrict FDlxUUA9JfBZBIEBvPhLA38XjLtII2KeYbsO8DSfO7DlEjJPucIphAiTbs0UYFr

