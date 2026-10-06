-- 개인정보 접근로그 ID 시퀀스
--
-- AS-IS saleson.shop.log.domain.PrivacyAccessLog는 ID를 시퀀스 OP_PRIVACY_ACCESS_LOG_SEQ로
-- 채번한다(@SequenceGenerator, AS-IS initialValue = 2152877 - 운영 데이터 기준값).
-- TO-BE DB의 admin.op_privacy_access_log.id에는 기본값도 시퀀스도 없어서 INSERT가 불가능했다
-- (표가 비어 있어 드러나지 않았다). 엑셀 다운로드 사유를 이 표에 기록하기로 했으므로
-- (PrivacyAccessLogService, 2026-10-03) 시퀀스를 만든다.
--
-- 시작값은 1이다 - AS-IS의 2152877은 운영 누적분을 이어받기 위한 값이고, 운영 데이터를
-- 적재할 때 setval로 맞추면 된다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < migration-admin-privacy-access-log-seq.sql

CREATE SEQUENCE IF NOT EXISTS admin.op_privacy_access_log_seq
    START WITH 1 INCREMENT BY 1 NO MAXVALUE NO CYCLE;

-- 이미 행이 있으면 최대값 뒤로 맞춘다(재실행 안전)
SELECT setval('admin.op_privacy_access_log_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM admin.op_privacy_access_log), 1));

GRANT USAGE, SELECT ON SEQUENCE admin.op_privacy_access_log_seq TO admindb;

SELECT sequence_schema, sequence_name, start_value
  FROM information_schema.sequences
 WHERE sequence_name = 'op_privacy_access_log_seq';
