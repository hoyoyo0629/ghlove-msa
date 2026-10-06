-- 개인정보 열람 이력(member.g_indvdlinfo_readng_hist)의 READNG_SN 시퀀스.
--
-- AS-IS는 공통 채번 서비스(sequenceService.getId("G_INDVDLINFO_READNG_HIST"))로 일련번호를 받는데
-- TO-BE에는 그 채번 테이블이 없고 이 테이블에도 시퀀스/DEFAULT가 없었다(READNG_SN은 NOT NULL).
-- 일반회원관리(메뉴 4101) 상세의 "개인정보 열람"이 이 이력을 남기므로 시퀀스를 만들어 붙인다.
-- 엑셀 다운로드 사유 로그에 했던 것과 같은 처리다(migration-admin-privacy-access-log-seq.sql).

CREATE SEQUENCE IF NOT EXISTS member.g_indvdlinfo_readng_hist_readng_sn_seq;

SELECT setval('member.g_indvdlinfo_readng_hist_readng_sn_seq',
              GREATEST((SELECT COALESCE(MAX(readng_sn), 0) FROM member.g_indvdlinfo_readng_hist), 1));
