-- AS-IS 메뉴 3700("대량주문 관리")/3701("작업 목록" → /opmanager/order/admin/list)을 그대로
-- 심는다. 2026-10-02 메뉴 전수동기화(migration-admin-menu-asis-reseed.sql)가 "조상체인 전부
-- display=Y인 가시 트리 172행"만 뽑아서 상위 3000("주문관리")이 이미 display_flag='N'/
-- status_code='2'(사용안함)라 이 가지 전체가 통째로 빠졌었다.
--
-- 2026-10-08: AS-IS OrderAdminServiceImpl.insertOrderAdmin(관리자 수기/엑셀 주문등록, "AB"
-- 코드 - 포인트사용 정합성검증(7210)이 이 코드만 본다)을 TO-BE에 포팅하면서, 처음엔 메뉴
-- 위치를 확인하지 않고 주문목록 화면에 임의로 링크를 추가했었다("오프라인 주문관리"(16409)와
-- 혼동 - 그건 무통장입금/ARS 주문 조회화면으로 완전히 다른 기능이다). asis_dump.op_menu
-- 원본 조회로 진짜 위치(3000>3700>3701)를 확인했고, 전부 AS-IS 자체가 비활성이므로
-- TO-BE도 display_flag='N'/status_code 그대로 숨김 유지한다 - 자리만 채워 구조를 맞춘다
-- ([[as-is-parity-includes-disabled-state]]). menu_url은 실제 TO-BE 라우트로 둬서 나중에
-- 혹시 display_flag를 'Y'로 되돌리는 결정이 나면 바로 동작한다.

INSERT INTO admin.op_menu (menu_id, menu_parent_id, menu_type, menu_name, menu_url, menu_seq, display_flag, status_code)
VALUES
    (3700, 3000, 2, '대량주문 관리', NULL, 7, 'N', '1'),
    (3701, 3700, 3, '작업 목록', '/admin/orders/manual', 1, 'N', '1')
ON CONFLICT (menu_id) DO NOTHING;
