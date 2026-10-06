-- 연계 로그 4화면(메뉴 1413~1416) 검증용 시드데이터
--
-- 실제 연계(쓰기)는 납부게이트웨이 이식 라운드 소관이라 네 표가 모두 비어 있다. 화면 자체는
-- 이식했으므로, 조회·검색·페이징·성공/실패 분기를 눈으로 확인할 수 있도록 개발DB에만 넣는
-- 샘플이다. **운영 데이터가 아니다** - 값은 AS-IS 코드체계를 따른 가공 데이터다.
--
-- 날짜는 "오늘" 기준으로 만든다 - 네 화면 모두 날짜 범위 기본값이 오늘이라 고정 날짜로 넣으면
-- 화면에 들어가도 아무것도 안 보인다.
--
-- 1415(지방부과)의 지자체명 두 칸은 G_LOCGOV를 SGB_CD = ADMINIST_INSTT_CODE로 LEFT JOIN해
-- 채운다. 개발DB에 ADMINIST_INSTT_CODE가 있는 지자체는 '1234567'(서울특별시 종로구) 한 곳뿐이라,
-- 첫 행만 그 코드를 쓰고 나머지는 다른 코드를 써서 **조인 실패 시 빈칸으로 나오는 것까지**
-- 확인할 수 있게 했다(AS-IS도 LEFT JOIN이라 같은 모습이 된다).
--
-- 재실행 안전: 넣기 전에 시드 키(IF_NO 'SEED%', LINK_MNG_KEY 'SEED%')만 지운다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < seed-donation-levy-link-logs.sql

BEGIN;

DELETE FROM donation.gif_seoul             WHERE if_no LIKE 'SEED%';
DELETE FROM donation.gif_etax_sunap        WHERE if_no LIKE 'SEED%';
DELETE FROM donation.g_next_buga_request   WHERE link_mng_key LIKE 'SEED%';
DELETE FROM donation.g_next_sunap_response WHERE link_mng_key LIKE 'SEED%';

-- 1413 서울세외 부과연계 로그 (gif_seoul) - 성공 2건 / 실패 1건 (ERROR_CD='0'이 성공)
INSERT INTO donation.gif_seoul
    (if_no, enapbu_no, sigu_cd, semok_cd, tax_ym, tax_gubun, sido_cd, nap_nm, nap_gubun,
     tax_amt, sise, reside_status, mul_gubun, mul_nm, book_no, sys_gubun,
     error_cd, error_msg, insert_key, insert_ak, result_cnt, if_st_dt, if_ed_dt, nap_id)
VALUES
    ('SEED-SB-001', '1100001234567890', '1111000', '10101', to_char(now(), 'YYYYMM'), '1', '11',
     '홍길동', '1', 100000, 0, '1', '1', '고향사랑기부금', '0000000001', 'GH',
     '0', NULL, 'SEED', 'SEED', '1', now() - interval '3 hour', now() - interval '3 hour', 'donor001'),
    ('SEED-SB-002', '1100001234567891', '1114000', '10101', to_char(now(), 'YYYYMM'), '1', '11',
     '김기부', '1', 300000, 0, '1', '1', '고향사랑기부금', '0000000002', 'GH',
     '0', NULL, 'SEED', 'SEED', '1', now() - interval '2 hour', now() - interval '2 hour', 'donor002'),
    ('SEED-SB-003', '1100001234567892', '1117000', '10101', to_char(now(), 'YYYYMM'), '1', '11',
     '이실패', '1', 50000, 0, '1', '1', '고향사랑기부금', '0000000003', 'GH',
     'E01', '납세자 정보가 일치하지 않습니다.', 'SEED', 'SEED', '0',
     now() - interval '1 hour', now() - interval '1 hour', 'donor003');

-- 1414 서울 수납연계 로그 (gif_etax_sunap) - 수납 2건 / 미수납 1건 (SUNAP_YN='Y'가 성공)
INSERT INTO donation.gif_etax_sunap
    (if_no, epay_no, com_req_meche, com_req_dt, com_req_tm, com_pay_msg_no, access_key,
     org_c, sunap_yn, sunap_amt, sunap_dt, rst_cd, rst_msg, if_st_dt, if_ed_dt)
VALUES
    ('SEED-SS-001', '1100001234567890', 'WEB', to_char(now(), 'YYYYMMDD'), '103015',
     'MSG000000001', 'AK0000001', '1111000', 'Y', 100000, to_char(now(), 'YYYYMMDD'),
     '000', '정상처리', now() - interval '3 hour', now() - interval '3 hour'),
    ('SEED-SS-002', '1100001234567891', 'MOBILE', to_char(now(), 'YYYYMMDD'), '113022',
     'MSG000000002', 'AK0000002', '1114000', 'Y', 300000, to_char(now(), 'YYYYMMDD'),
     '000', '정상처리', now() - interval '2 hour', now() - interval '2 hour'),
    -- SUNAP_DT는 NOT NULL이라 미수납 건도 빈 문자열로 둔다(NULL 불가)
    ('SEED-SS-003', '1100001234567892', 'WEB', to_char(now(), 'YYYYMMDD'), '121145',
     'MSG000000003', 'AK0000003', '1117000', 'N', 0, '',
     '900', '수납대기 상태입니다.', now() - interval '1 hour', now() - interval '1 hour');

-- 1415 지방세외 부과연계 로그 (g_next_buga_request) - 연계성공(LINK_RST_CD='000') 2건 / 실패 1건
INSERT INTO donation.g_next_buga_request
    (link_mng_key, sgb_cd, link_trgt_cd, dpt_cd, spcl_fis_biz_cd, fyr, act_se_cd, rprs_txm_cd,
     oper_item_cd, lvy_ymd, frst_pct_amt, frst_pid_ymd, pyr_se_cd, pyr_no, pyr_nm,
     rprs_pyr_no, rprs_pyr_nm, pyr_stt_cd, lotno_road_addr_se_cd, zip, road_nm_cd, bmno, bsno,
     stdg_cd, dong_cd, road_nm_daddr, gl_nm, mng_item_cn1,
     buga_status_cd, link_rst_cd, link_rst_msg, frst_regist_pnttm)
VALUES
    ('SEED-NB-001', '1234567', '01', '1500000', '000', to_char(now(), 'YYYY'), '2', '01101', '01',
     to_char(now(), 'YYYYMMDD'), '100000', to_char(now() + interval '14 day', 'YYYYMMDD'),
     '1', '8001011234567', '홍길동', '8001011234567', '홍길동', '1',
     '2', '30128', '411103007001', '13', '0', '3611010100', '3611053000',
     '정부2청사로 13', '고향사랑기부금', '고향사랑e음 연계',
     '01', '000', '정상등록 (epayNo=1100001234567890)', now() - interval '1 hour'),
    ('SEED-NB-002', '1114000', '01', '1500000', '000', to_char(now(), 'YYYY'), '2', '01101', '01',
     to_char(now(), 'YYYYMMDD'), '300000', to_char(now() + interval '14 day', 'YYYYMMDD'),
     '1', '8505052345678', '김기부', '8505052345678', '김기부', '1',
     '2', '30116', '411103007002', '411', '0', '3611010200', '3611054000',
     '한누리대로 411', '고향사랑기부금', '고향사랑e음 연계',
     '01', '000', '정상등록 (epayNo=1100001234567891)', now() - interval '2 hour'),
    ('SEED-NB-003', '1117000', '01', '1500000', '000', to_char(now(), 'YYYY'), '2', '01101', '01',
     to_char(now(), 'YYYYMMDD'), '50000', to_char(now() + interval '14 day', 'YYYYMMDD'),
     '1', '9009093456789', '이실패', '9009093456789', '이실패', '1',
     '2', '30128', '411103007003', '1', '0', '3611010300', '3611055000',
     '정부2청사로 1', '고향사랑기부금', '고향사랑e음 연계',
     '99', 'E99', '필수항목 누락 (epayNo=1100001234567892)', now() - interval '3 hour');

-- 1416 지방세외 수납연계 로그 (g_next_sunap_response) - 3건
INSERT INTO donation.g_next_sunap_response
    (link_mng_key, sgb_cd, sgb_nm, taxn_no, unty_taxn_no, dpt_cd, dpt_nm,
     spcl_fis_biz_cd, spcl_fis_biz_nm, fyr, act_se_cd, act_se_nm, rprs_txm_cd, rprs_txm_nm,
     oper_item_cd, oper_item_nm, lvy_no, itm_no, epay_no, rcvmt_no, rcvmt_se_cd, rcvmt_se_nm,
     rcvmt_ymd, act_ymd, tsf_ymd, rcvmt_pct_amt, rcvmt_adtn_amt, rcvmt_intr_amt, bank_nm,
     rcvmt_ty_cd, rcvmt_ty, rsve_item1, rsve_item2, rsve_item3, rsve_item4, rsve_item5,
     frst_regist_pnttm)
VALUES
    ('SEED-NS-001', '1234567', '종로구', to_char(now(), 'YYYY') || '00000001',
     'U' || to_char(now(), 'YYYY') || '0001', '1500000', '세정과',
     '000', '고향사랑기부금특별회계', to_char(now(), 'YYYY'), '2', '특별회계',
     '01101', '고향사랑기부금', '01', '고향사랑기부금', '000001', '01',
     '1100001234567890', '01', '1', '전액수납',
     to_char(now(), 'YYYYMMDD'), to_char(now(), 'YYYYMMDD'),
     to_char(now() + interval '1 day', 'YYYYMMDD'),
     '100000', '0', '0', '농협은행', '1', '계좌이체',
     NULL, NULL, NULL, NULL, NULL, now() - interval '1 hour'),
    ('SEED-NS-002', '1114000', '중구', to_char(now(), 'YYYY') || '00000002',
     'U' || to_char(now(), 'YYYY') || '0002', '1500000', '세정과',
     '000', '고향사랑기부금특별회계', to_char(now(), 'YYYY'), '2', '특별회계',
     '01101', '고향사랑기부금', '01', '고향사랑기부금', '000002', '01',
     '1100001234567891', '02', '1', '전액수납',
     to_char(now(), 'YYYYMMDD'), to_char(now(), 'YYYYMMDD'),
     to_char(now() + interval '1 day', 'YYYYMMDD'),
     '300000', '0', '0', '국민은행', '1', '계좌이체',
     NULL, NULL, NULL, NULL, NULL, now() - interval '2 hour'),
    ('SEED-NS-003', '1117000', '용산구', to_char(now(), 'YYYY') || '00000003',
     'U' || to_char(now(), 'YYYY') || '0003', '1500000', '세정과',
     '000', '고향사랑기부금특별회계', to_char(now(), 'YYYY'), '2', '특별회계',
     '01101', '고향사랑기부금', '01', '고향사랑기부금', '000003', '01',
     '1100001234567892', '03', '2', '부분수납',
     to_char(now(), 'YYYYMMDD'), to_char(now(), 'YYYYMMDD'),
     to_char(now() + interval '1 day', 'YYYYMMDD'),
     '30000', '500', '100', '신한은행', '2', '가상계좌',
     NULL, NULL, NULL, NULL, NULL, now() - interval '3 hour');

COMMIT;

SELECT 'gif_seoul' AS t, count(*) FROM donation.gif_seoul
UNION ALL SELECT 'gif_etax_sunap', count(*) FROM donation.gif_etax_sunap
UNION ALL SELECT 'g_next_buga_request', count(*) FROM donation.g_next_buga_request
UNION ALL SELECT 'g_next_sunap_response', count(*) FROM donation.g_next_sunap_response;
