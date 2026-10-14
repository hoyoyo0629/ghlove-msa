-- 회원 가입구분·회원 지자체코드 AS-IS 정합 (2026-10-14).
-- 대상 DB: ghlove_core (member 스키마, donation.g_cntr 교차조회)
--
-- 1) OP_USER.SBSCRB_SE_CODE - AS-IS 공통코드 SBSCRB_SE(100 온라인 / 200 오프라인)인데 TO-BE가 발명값 'GENERAL'을
--    넣어 왔다(admin 일반회원 화면 가입구분 공백, 오프라인 검색 불가). AS-IS: 온라인·간편가입은 join-mapper
--    updateUserInfo가 '100', 기탁서 즉석가입은 OffgiveServiceImpl.insertOffgive가 '200'. 즉석가입 계정은
--    TO-BE가 'walkin'+숫자 아이디로 만든다(MemberService.generateLoginId). 탈퇴 회원은 AS-IS처럼 NULL 유지.
-- 2) OP_USER.LOCGOV_CODE - 컬럼은 있었으나 매핑이 없어 전부 NULL. 즉석가입은 AS-IS처럼 행정지 지자체
--    (그 회원 첫 기탁서의 psitn_locgov_code)로 채운다. 온라인 가입자는 원천값(주소검색 admCd)이 남아 있지 않아
--    NULL 그대로 - 회원정보 수정에서 저장하면 채워진다(AS-IS updateUserEtcInfo).

UPDATE member.op_user SET sbscrb_se_code = '200'
 WHERE sbscrb_se_code = 'GENERAL' AND login_id LIKE 'walkin%';

UPDATE member.op_user SET sbscrb_se_code = '100'
 WHERE sbscrb_se_code = 'GENERAL';

UPDATE member.op_user u
   SET locgov_code = (SELECT c.psitn_locgov_code
                        FROM donation.g_cntr c
                       WHERE c.user_id = u.user_id AND c.cntr_path_code IN ('200', '300')
                       ORDER BY c.frst_regist_pnttm
                       LIMIT 1)
 WHERE u.sbscrb_se_code = '200' AND u.locgov_code IS NULL;
