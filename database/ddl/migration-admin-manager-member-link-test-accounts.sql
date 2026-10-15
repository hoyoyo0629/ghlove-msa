-- =====================================================================================================
-- 관리자 계정 ↔ 회원 계정 연결 (2026-10-14, A안) - 개발DB 테스트 관리자 계정 정리
--
-- AS-IS는 관리자(OP_MANAGER)가 회원(OP_USER)과 같은 USER_ID를 쓴다(승인 시 회원 정보 복사, 오프라인 담당자는 회원부터 생성).
-- TO-BE 개발DB의 테스트 관리자 17건은 회원과 따로 채번돼 있어서
--   - 회원이 없거나(15건), 같은 번호의 엉뚱한 회원과 겹치거나(admin01=1000↔testuser01, operator01=1001↔testuser02,
--     locgovtest2=1002↔testuser03), 같은 아이디 회원이 다른 번호에 있었다(locgovtest2=회원 1015, hoyoyo0629=회원 1016).
-- 정리:
--   1) 같은 아이디 회원이 있으면 관리자 번호를 그 회원 번호로 옮긴다: locgovtest2 1002→1015, hoyoyo0629 9708→1016
--      (두 계정의 비밀번호는 각자 그대로 둔다 - 다음 비밀번호 변경 때부터 서로 맞춰진다).
--   2) 회원이 없는 관리자는 회원을 만든다. 번호가 회원과 겹치는 admin01·operator01은 회원 시퀀스의 새 번호(1113·1114)로
--      옮기고, 나머지는 관리자 번호 그대로 회원을 만든다. 비밀번호는 관리자 비밀번호(해시)를 그대로 쓴다.
--   3) 오프라인 담당자(ROLE_ADMIN_7/8)가 아닌 관리자는 로그인 때 본인인증(CI)이 필요하다(AS-IS EMPTY_CI) - 테스트용 CI를 넣는다.
--   4) 관리자 번호가 바뀐 4건은 관리자 번호를 담는 칸(등록자·수정자·처리 관리자 등)도 같이 바꾼다.
-- 1113·1114는 nextval('member.op_user_user_id_seq')로 미리 받아 둔 번호다.
-- 백업: 적용 전 admin.op_manager / admin.g_mngr_reqst / member.op_user / member.op_user_detail 을 pg_dump로 받아 둠.
-- =====================================================================================================
BEGIN;

CREATE TEMP TABLE manager_id_map (old_id bigint PRIMARY KEY, new_id bigint NOT NULL) ON COMMIT DROP;
INSERT INTO manager_id_map VALUES (1000, 1113), (1001, 1114), (1002, 1015), (9708, 1016);

-- 2) 회원이 없는 관리자 → 회원 생성 (번호: 매핑이 있으면 새 번호, 없으면 관리자 번호)
INSERT INTO member.op_user (user_id, login_id, password, user_name, email, status_code, login_count, login_fail_count,
                            password_type, password_expired_date, created_date, updated_date, sbscrb_se_code,
                            login_path_code, mber_ci, foreign_status_code, mfa_enabled)
SELECT COALESCE(mp.new_id, m.user_id), m.login_id, m.password, m.user_name, COALESCE(NULLIF(m.email, ''), m.login_id),
       'ACTIVE', 0, 0, 'N', to_char(now() + interval '180 day', 'YYYYMMDD'),
       to_char(now(), 'YYYYMMDDHH24MISS'), to_char(now(), 'YYYYMMDDHH24MISS'), '100', '100',
       CASE WHEN m.authority IN ('ROLE_ADMIN_7', 'ROLE_ADMIN_8') THEN NULL ELSE 'TEST-CI-' || m.login_id END, '0', 'N'
FROM admin.op_manager m
LEFT JOIN manager_id_map mp ON mp.old_id = m.user_id
WHERE m.login_id NOT IN (SELECT login_id FROM member.op_user);

-- 회원 상세: 오프라인 담당자는 AS-IS처럼 빈 행(휴대폰은 관리자 쪽), 그 외는 인증서 등록 화면 확인용 휴대폰·생년월일
INSERT INTO member.op_user_detail (user_id, level_id, use_flag, phone_number, birthday)
SELECT u.user_id, 1, 'Y',
       CASE WHEN m.authority IN ('ROLE_ADMIN_7', 'ROLE_ADMIN_8') THEN NULL
            ELSE '010-7777-' || lpad((u.user_id % 10000)::text, 4, '0') END,
       CASE WHEN m.authority IN ('ROLE_ADMIN_7', 'ROLE_ADMIN_8') THEN NULL ELSE '19800101' END
FROM member.op_user u
JOIN admin.op_manager m ON m.login_id = u.login_id
WHERE NOT EXISTS (SELECT 1 FROM member.op_user_detail d WHERE d.user_id = u.user_id);

-- 3) 이미 있던 연결 회원(locgovtest2·hoyoyo0629)도 CI가 없으면 테스트 CI
UPDATE member.op_user u SET mber_ci = 'TEST-CI-' || u.login_id
FROM admin.op_manager m
WHERE m.login_id = u.login_id AND COALESCE(u.mber_ci, '') = ''
  AND m.authority NOT IN ('ROLE_ADMIN_7', 'ROLE_ADMIN_8');

-- 1)·2) 관리자 번호 옮기기 - op_community_locgovfaq 등 FK가 있어 새 행을 넣고 참조를 옮긴 뒤 옛 행을 지운다
INSERT INTO admin.op_manager
SELECT (jsonb_populate_record(m, jsonb_build_object('user_id', mp.new_id))).*
FROM admin.op_manager m JOIN manager_id_map mp ON mp.old_id = m.user_id;

UPDATE admin.op_community_locgovfaq t SET admin_id = mp.new_id FROM manager_id_map mp WHERE t.admin_id = mp.old_id;
UPDATE admin.op_community_comment t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_community_freeboard t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_manager_password_log t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_manager_change_log t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_manager_hist t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_manager_login t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;
UPDATE admin.op_qna_answer t SET user_id = mp.new_id FROM manager_id_map mp WHERE t.user_id = mp.old_id;

DELETE FROM admin.op_manager m USING manager_id_map mp WHERE m.user_id = mp.old_id;

-- 4) 관리자 번호를 담는 칸
UPDATE admin.g_mngr_reqst t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE admin.g_qestnar t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE admin.g_qustnr_iem t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE admin.g_qustnr_qesitm t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE admin.g_reprst_banner t SET frst_register_id = mp.new_id FROM manager_id_map mp WHERE t.frst_register_id = mp.old_id;
UPDATE admin.g_reprst_banner t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE admin.op_faq t SET created_by = mp.new_id FROM manager_id_map mp WHERE t.created_by = mp.old_id;
UPDATE admin.op_privacy_access_log t SET manager_id = mp.new_id FROM manager_id_map mp WHERE t.manager_id = mp.old_id;
UPDATE admin.op_privacy_access_log_hist t SET manager_id = mp.new_id FROM manager_id_map mp WHERE t.manager_id = mp.old_id;
UPDATE ord.od_order t SET hold_manager_id = mp.new_id FROM manager_id_map mp WHERE t.hold_manager_id = mp.old_id;
UPDATE ord.od_claim_memo t SET manager_id = mp.new_id FROM manager_id_map mp WHERE t.manager_id = mp.old_id;
UPDATE ord.od_excel_download_log t SET manager_id = mp.new_id FROM manager_id_map mp WHERE t.manager_id = mp.old_id;
UPDATE donation.g_cntr_rcipt t SET frst_register_id = mp.new_id FROM manager_id_map mp WHERE t.frst_register_id = mp.old_id;
UPDATE donation.g_cntr_rcipt t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE donation.g_cntr_reqmng t SET frst_register_id = mp.new_id FROM manager_id_map mp WHERE t.frst_register_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_cn_img_expln t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_dept_mng t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_dept_mng t SET last_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.last_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_img_mng t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_mng t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_mng t SET last_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.last_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_ntc_cn_img_expln t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_ntc_file_mng t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_ntc_mng t SET frst_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.frst_rgtr_id = mp.old_id;
UPDATE donation.g_dsgn_dntn_biz_ntc_mng t SET last_rgtr_id = mp.new_id FROM manager_id_map mp WHERE t.last_rgtr_id = mp.old_id;
UPDATE donation.g_locgov t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;
UPDATE donation.g_locgov_dept_hist t SET frst_register_id = mp.new_id FROM manager_id_map mp WHERE t.frst_register_id = mp.old_id;
UPDATE donation.g_locgov_dept_hist t SET last_updusr_id = mp.new_id FROM manager_id_map mp WHERE t.last_updusr_id = mp.old_id;

-- 확인: 모든 관리자가 같은 번호·같은 아이디의 회원을 갖는다
DO $$
DECLARE bad int;
BEGIN
  SELECT count(*) INTO bad FROM admin.op_manager m
  LEFT JOIN member.op_user u ON u.user_id = m.user_id AND u.login_id = m.login_id
  WHERE u.user_id IS NULL;
  IF bad > 0 THEN RAISE EXCEPTION '회원과 연결되지 않은 관리자 %건', bad; END IF;
END $$;

COMMIT;
