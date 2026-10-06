-- 납부(세외수입) 연계 요청에 필요한 채번 시퀀스 2개.
--
-- AS-IS ngdonation-mapper 실측:
--   getLinkMngKeyNextValue : SELECT concat(DATE_FORMAT(now(),'%Y%m%d%H%i%s'), g_cntr_cntr_sn.next_value)
--                            → 지방세외 차세대 부과요청의 연계관리키(LINK_MNG_KEY)
--   getBookNoSeoul         : SELECT gif_seoul_book_no_seq.next_value
--                            → 서울 세외 부과요청의 대장번호(BOOK_NO, 원천시스템 유일키)
--
-- TO-BE에는 두 시퀀스가 없었다(실측: donation 스키마에 g_cntr_wegive_cntr_sn_seq 하나뿐).
-- AS-IS가 linkMngKey에 쓰는 g_cntr_cntr_sn은 기부 일련번호 시퀀스인데, TO-BE는 기부번호를
-- 애플리케이션에서 만들고 있어 같은 이름의 시퀀스가 없다 - 연계관리키 전용으로 따로 만든다
-- (AS-IS도 값의 유일성만 요구하고 기부번호와 연결해 쓰지는 않는다).

CREATE SEQUENCE IF NOT EXISTS donation.g_cntr_link_mng_key_seq;
CREATE SEQUENCE IF NOT EXISTS donation.gif_seoul_book_no_seq;

GRANT USAGE, SELECT, UPDATE ON SEQUENCE donation.g_cntr_link_mng_key_seq TO donation, admindb, member, point, orderdb, gift;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE donation.gif_seoul_book_no_seq  TO donation, admindb, member, point, orderdb, gift;

SELECT sequence_schema, sequence_name FROM information_schema.sequences
 WHERE sequence_name IN ('g_cntr_link_mng_key_seq', 'gif_seoul_book_no_seq')
 ORDER BY 2;
