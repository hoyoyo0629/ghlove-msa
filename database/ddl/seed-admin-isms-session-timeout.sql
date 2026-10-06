-- ISMS 보안설정: 운영자 자동 로그아웃 시간(분)
--
-- AS-IS는 이 값을 ISMS 설정에서 읽는다(SellerUserServiceImpl:528
-- configIsmsService.getIsmsConfigValueByKey("SESSION_TIMEOUT_MANAGER"), 파싱 실패 시 기본 60분).
-- 읽은 값이 inc_head.jsp의 OP_MANAGER_TIMEOUT으로 내려가고 op.manager.js가 분 단위로 쓴다.
-- TO-BE 표에 이 키가 없어 화면에서 조정할 수 없었다 - AS-IS 기본값으로 넣어 둔다.
--
-- 참고: 서버 세션 만료는 별개다(AS-IS SessionListener가 30분, TO-BE는
-- server.servlet.session.timeout: 30m). 서버 30분이 먼저 끝나므로 보통 그쪽이 먼저 작동한다.

insert into admin.op_config_isms (key, value, description, use_yn, ordering, isms_type, update_date)
values ('SESSION_TIMEOUT_MANAGER', '60', '운영자 화면 자동 로그아웃 시간(분)', 'Y', 10, '0',
        to_char(now(), 'YYYYMMDDHH24MISS'))
on conflict (key) do nothing;
