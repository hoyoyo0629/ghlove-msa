-- 오프라인담당자(메뉴 4601) 검증용 시드.
--
-- 개발DB에 ROLE_ADMIN_7(오프라인 주담당자)·ROLE_ADMIN_8(오프라인 부담당자) 계정이 0건이어서
-- 목록/상세/권한분기/주담당자 정원(지점당 2명)/권한이관을 확인할 수 없었다. AS-IS 화면이
-- 실제로 다루는 모양 - 같은 지점(BANK_CODE)에 주담당자 2명 + 부담당자 여럿 - 으로 넣는다.
--
-- BANK_CODE는 공통코드 OFF_BANK_LIST: 011 농협은행 / 012 농축협 / 035 제주은행.
-- STATUS_CODE는 이 프로젝트 표기(ACTIVE/LOCKED)다 - AS-IS의 '9'/'2'에 대응하고 화면은
-- Manager.getAsIsStatusCode()로 변환해 9/2 라디오를 맞춘다.
-- PASSWORD는 BCrypt('nacf1234') - AS-IS 등록의 기본 비밀번호다.
--
-- 지점 011에 주담당자를 정확히 2명 두었기 때문에, 그 지점에서 부담당자를 주담당자로 올리려 하면
--  - 시스템/행안부 관리자로 시도 → ERR_MAIN_CNT("주관리자는 최대 2명까지만 등록이 가능합니다.")
--  - 오프라인 주담당자 본인으로 시도 → NEED_AUTH_SWAP(권한 승계 동의 → 본인 부담당자+중지)
-- 두 분기를 바로 재현할 수 있다.

INSERT INTO admin.op_manager (
    user_id, login_id, password, user_name, email, phone_number, status_code,
    login_count, login_fail_count, created_date, updated_date,
    authority, bank_code, emp_id, psitn_nm, ofcps_nm, info_updt_de, deny_date
) VALUES
-- 농협은행(011) 주담당자 2명 = 정원 꽉 찬 지점
(9701, 'nh.main1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김주담', 'nh.main1@nonghyup.com', '02-3011-1001', 'ACTIVE',
 0, 0, '20260901100000', '20260901100000', 'ROLE_ADMIN_7', '011', 'E0010001', '서울중앙지점', 'nh.main1@nonghyup.com', '20260901', NULL),
(9702, 'nh.main2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '이주담', 'nh.main2@nonghyup.com', '02-3011-1002', 'ACTIVE',
 0, 0, '20260901100100', '20260901100100', 'ROLE_ADMIN_7', '011', 'E0010002', '서울남부지점', 'nh.main2@nonghyup.com', '20260901', NULL),
-- 농협은행(011) 부담당자 - 승격 시도 대상(권한이관 분기 확인용)
(9703, 'nh.sub1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '박부담', 'nh.sub1@nonghyup.com', '02-3011-1003', 'ACTIVE',
 0, 0, '20260901100200', '20260901100200', 'ROLE_ADMIN_8', '011', 'E0010003', '서울중앙지점', 'nh.sub1@nonghyup.com', '20260901', NULL),
-- 중지 상태 - 목록의 '중지'/중지일자 칸과 사용여부 검색 확인용
(9704, 'nh.sub2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '최부담', 'nh.sub2@nonghyup.com', '02-3011-1004', 'LOCKED',
 0, 0, '20260901100300', '20260915093000', 'ROLE_ADMIN_8', '011', 'E0010004', '서울북부지점', 'nh.sub2@nonghyup.com', '20260901', '20260915093000'),
-- 농축협(012) - 주담당자 1명(정원 여유) + 부담당자 1명. 지점 스코프 확인용(011 담당자에게는 안 보여야 한다)
(9705, 'nhch.main1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '정주담', 'nhch.main1@nonghyup.com', '031-500-2001', 'ACTIVE',
 0, 0, '20260902110000', '20260902110000', 'ROLE_ADMIN_7', '012', 'E0120001', '수원농축협', 'nhch.main1@nonghyup.com', '20260902', NULL),
(9706, 'nhch.sub1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '한부담', 'nhch.sub1@nonghyup.com', '031-500-2002', 'ACTIVE',
 0, 0, '20260902110100', '20260902110100', 'ROLE_ADMIN_8', '012', 'E0120002', '용인농축협', 'nhch.sub1@nonghyup.com', '20260902', NULL),
-- 제주은행(035) - 부담당자만. INFO_UPDT_DE가 비어 있어 "지점정보 미입력" 상태를 재현한다
(9707, 'jeju.sub1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '강부담', 'jeju.sub1@nonghyup.com', '064-700-3001', 'ACTIVE',
 0, 0, '20260903120000', '20260903120000', 'ROLE_ADMIN_8', '035', 'E0350001', '제주본점', 'jeju.sub1@nonghyup.com', NULL, NULL)
ON CONFLICT (user_id) DO NOTHING;

-- op_manager.user_id는 시퀀스에서 받는다 - 수동 INSERT한 ID보다 뒤로 밀어 둔다.
SELECT setval('admin.op_manager_user_id_seq',
              GREATEST((SELECT COALESCE(MAX(user_id), 1) FROM admin.op_manager), 9707));
