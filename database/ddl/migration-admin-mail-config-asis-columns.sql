-- admin.op_mail_config 에 AS-IS OP_MAIL_CONFIG 컬럼을 보충한다.
-- AS-IS는 23컬럼인데 TO-BE는 10컬럼만 만들어 두어 이메일 설정 화면의 "발송여부"(buyer_send_flag,
-- admin_send_flag)조차 저장할 수 없었다. AS-IS DDL(db-dump/테이블dump/op_mail_config.sql)의
-- 타입/기본값을 그대로 따른다. 재실행 가능.
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS buyer_send_flag  varchar(1);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS admin_send_flag  varchar(1);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS seller_send_flag varchar(1);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS buyer_tag_use    varchar(1) DEFAULT 'Y';
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS admin_tag_use    varchar(1) DEFAULT 'Y';
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS seller_tag_use   varchar(1) DEFAULT 'Y';
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS created_date     varchar(14);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_buyer_subject  varchar(100);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_admin_subject  varchar(100);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_seller_subject varchar(100);
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_buyer_content  text;
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_admin_content  text;
ALTER TABLE admin.op_mail_config ADD COLUMN IF NOT EXISTS mobile_seller_content text;
