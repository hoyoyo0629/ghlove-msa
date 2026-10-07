-- 1406 관리자 권한 승인관리 검증용 시드 + REQST_SE_CODE 코드표 AS-IS 정렬.
-- 2026-10-06, AS-IS 개발DB(ghlove2) 직접 조회로 확인한 값 기준.
--
-- 왜 필요한가: TO-BE admin.g_mngr_reqst 가 1행뿐이어서 검색·상태필터·페이징·이력 팝업을
-- 눌러볼 수 없었다(AS-IS는 8,910행). 상태 3종·신청구분 4종·1인 다건(이력)·거절사유까지
-- 섞어 20건을 넣는다.
--
-- AS-IS 확인 사항
--  · CONFM_STTUS_CODE(코드표 CFM_STATUS) = 100 승인 / 200 대기 / 300 거절.
--    (TO-BE ManagerRequestController.CONFM_STTUS_LABELS 와 일치함을 확인했다)
--  · REQST_SE_CODE = ROLE_ADMIN_6 지자체 / ROLE_ADMIN_8 오프라인 / ROLE_ADMIN_4 행정안전부
--    / ROLE_ADMIN_2 시스템 / ROLE_ADMIN_10 지정기부(use_yn=N) / ROLE_ADMIN_11 행정복지센터(use_yn=N).
--    **6개가 전부다.** TO-BE에 있던 LOCALGOV·PROVIDER·OPERATOR 3개는 AS-IS에 없는 추가분이라
--    아래에서 use_yn='N' 으로 내린다(행 삭제는 과거 데이터가 참조할 수 있어 하지 않는다).
--  · LOCGOV_CODE 는 지자체(6)·오프라인(8) 신청에만 채워져 있고 시스템(2)·행안부(4)는 비어 있다.
--  · AS-IS는 LOGIN_ID·OFCPS_NM·CTTPC 를 암호화해 저장한다(`^`...==`). TO-BE는 평문이므로
--    시드도 평문으로 둔다 - 암호화 도입은 별도 과제.
--
-- user_id 는 member.op_user 에 실제로 있는 값만 쓴다(목록이 MemberClient 로 이름·이메일을
-- 채우므로 없는 id를 넣으면 빈칸으로 뜬다).
--
-- 재실행 안전: 시드분(user_id, reqst_sn)을 먼저 지우고 다시 넣는다. 기존 운영/수동 데이터는
-- 건드리지 않되, reqst_se_code 가 AS-IS에 없는 '1' 이던 1행만 바로잡는다.

-- 0) AS-IS에 없는 신청구분 3개를 비활성으로 (화면 select에서 사라진다)
-- [2026-10-07 폐기] admin.admin_common_code는 공통코드 표 통합으로 DROP됐다(AS-IS 실명
-- op_common_code로 일원화). op_common_code의 REQST_SE_CODE는 애초부터 LOCALGOV/PROVIDER/
-- OPERATOR가 없고 AS-IS 실제 6종(ROLE_ADMIN_2/4/6/8/10/11)뿐이라 이 UPDATE 자체가 불필요 -
-- 재실행하면 "relation does not exist" 에러만 나므로 주석 처리.
-- UPDATE admin.admin_common_code
--    SET use_yn = 'N'
--  WHERE code_type = 'REQST_SE_CODE'
--    AND id IN ('LOCALGOV', 'PROVIDER', 'OPERATOR');

-- 0-2) 기존 1행의 신청구분이 AS-IS에 없는 '1' 이었다. locgov_code 가 채워져 있으므로 지자체로.
UPDATE admin.g_mngr_reqst
   SET reqst_se_code = 'ROLE_ADMIN_6'
 WHERE reqst_se_code = '1';

-- 1) 시드 재실행 안전 - 아래에서 넣는 조합만 제거
DELETE FROM admin.g_mngr_reqst
 WHERE user_id IN (1000,1001,1002,1013,1014,1016,1017,1018,1033,1034,1039,1043,1035,1027,1028,1029)
   AND frst_register_id = 999999;   -- 시드 표식

-- 2) 시드 20건
--    frst_register_id = 999999 를 시드 표식으로 쓴다(운영 데이터와 구분).
--    등록일시는 "오늘"을 기준으로 역산해 넣는다 - 날짜범위 검색을 눌러볼 수 있게.
INSERT INTO admin.g_mngr_reqst (
    user_id, reqst_sn, login_id, locgov_code, reqst_se_code,
    psitn_code, psitn_nm, psitn_dept_nm, ofcps_nm, cttpc,
    confm_sttus_code, reject_resn,
    frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm
) VALUES
-- ── 대기(200) 10건 : 승인/거절 버튼을 눌러볼 대상
 (1013, 1, 'localgovtest', '11110', 'ROLE_ADMIN_6', NULL, NULL, '자치행정과', '김민수', '02-2148-1001', '200', NULL, 999999, to_char(now() - interval  '1 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1014, 1, 'providertest', '28125', 'ROLE_ADMIN_6', NULL, NULL, '세무과',     '이영희', '032-770-1002', '200', NULL, 999999, to_char(now() - interval  '2 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1016, 1, 'hoyoyo0629',   NULL,    'ROLE_ADMIN_2', NULL, NULL, '정보화담당관실', '김진호', '044-205-1003', '200', NULL, 999999, to_char(now() - interval  '3 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1017, 1, 'e2etest01',    NULL,    'ROLE_ADMIN_4', NULL, NULL, '지역균형발전과', '박철수', '044-205-1004', '200', NULL, 999999, to_char(now() - interval  '4 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1018, 1, 'rlawnan01',    '12840', 'ROLE_ADMIN_8', NULL, NULL, '민원봉사과', '김주무', '061-390-1005', '200', NULL, 999999, to_char(now() - interval  '5 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1033, 1, 'test001',      '11680', 'ROLE_ADMIN_6', NULL, NULL, '기획예산과', '최지은', '02-3423-1006', '200', NULL, 999999, to_char(now() - interval  '6 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1034, 1, 'test002',      '41000', 'ROLE_ADMIN_8', NULL, NULL, '복지정책과', '정대현', '031-8008-1007', '200', NULL, 999999, to_char(now() - interval  '8 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1035, 1, 'walkin574243', '48000', 'ROLE_ADMIN_6', NULL, NULL, '행정과',     '한소영', '055-211-1008', '200', NULL, 999999, to_char(now() - interval '10 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1039, 1, 'test005',      NULL,    'ROLE_ADMIN_2', NULL, NULL, '시스템운영팀', '오준호', '044-205-1009', '200', NULL, 999999, to_char(now() - interval '12 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
 (1043, 1, 'test006',      '50000', 'ROLE_ADMIN_8', NULL, NULL, '세정과',     '윤서진', '063-280-1010', '200', NULL, 999999, to_char(now() - interval '14 day', 'YYYYMMDDHH24MISS'), NULL, NULL),
-- ── 승인(100) 7건
 (1000, 1, 'testuser01',   '11000', 'ROLE_ADMIN_6', NULL, NULL, '총무과',     '강현우', '02-2133-1011', '100', NULL, 999999, to_char(now() - interval '20 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '19 day', 'YYYYMMDDHH24MISS')),
 (1001, 1, 'testuser02',   '26000', 'ROLE_ADMIN_6', NULL, NULL, '재정과',     '송미라', '051-888-1012', '100', NULL, 999999, to_char(now() - interval '25 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '24 day', 'YYYYMMDDHH24MISS')),
 (1002, 1, 'testuser03',   '28000', 'ROLE_ADMIN_8', NULL, NULL, '민원과',     '배성호', '032-440-1013', '100', NULL, 999999, to_char(now() - interval '30 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '29 day', 'YYYYMMDDHH24MISS')),
 (1027, 1, 'kakao_MOCK',   NULL,    'ROLE_ADMIN_4', NULL, NULL, '균형발전지원과', '신예린', '044-205-1014', '100', NULL, 999999, to_char(now() - interval '35 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '34 day', 'YYYYMMDDHH24MISS')),
 (1028, 1, 'naver_MOCK',   NULL,    'ROLE_ADMIN_2', NULL, NULL, '정보보호팀', '류태경', '044-205-1015', '100', NULL, 999999, to_char(now() - interval '40 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '39 day', 'YYYYMMDDHH24MISS')),
 (1029, 1, 'finance_cert_MOCK', '11110', 'ROLE_ADMIN_6', NULL, NULL, '문화체육과', '임하늘', '02-2148-1016', '100', NULL, 999999, to_char(now() - interval '50 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '49 day', 'YYYYMMDDHH24MISS')),
 (1033, 2, 'test001',      '11680', 'ROLE_ADMIN_6', NULL, NULL, '기획예산과', '최지은', '02-3423-1006', '100', NULL, 999999, to_char(now() - interval '60 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '59 day', 'YYYYMMDDHH24MISS')),
-- ── 거절(300) 3건 : 거절사유가 상세 팝업에 보인다
 (1034, 2, 'test002',      '41000', 'ROLE_ADMIN_8', NULL, NULL, '복지정책과', '정대현', '031-8008-1007', '300', '소속 부서 확인이 되지 않습니다. 담당자 확인 후 재신청해 주세요.', 999999, to_char(now() - interval '70 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '69 day', 'YYYYMMDDHH24MISS')),
 (1039, 2, 'test005',      NULL,    'ROLE_ADMIN_2', NULL, NULL, '시스템운영팀', '오준호', '044-205-1009', '300', '시스템 권한은 정보화담당관실 승인이 선행되어야 합니다.', 999999, to_char(now() - interval '80 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '79 day', 'YYYYMMDDHH24MISS')),
 (1043, 2, 'test006',      '50000', 'ROLE_ADMIN_8', NULL, NULL, '세정과',     '윤서진', '063-280-1010', '300', '연락처가 유효하지 않습니다.', 999999, to_char(now() - interval '90 day', 'YYYYMMDDHH24MISS'), 1000, to_char(now() - interval '89 day', 'YYYYMMDDHH24MISS'));
