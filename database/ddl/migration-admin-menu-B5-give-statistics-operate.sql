-- B5 기부 통계 '운영현황'(6901) - AS-IS give/statistics/operate 전용 화면으로 재배선.
-- 그동안 통합 /give-statistics(모금현황 통합본)에 걸려 있던 것을 지자체×사용용도 지출 피벗
-- (donation G_CTBNY_OPRATN 집계) 전용 화면으로 분리한다. URL은 바로잡아 둔다(직접 URL 접근용).
UPDATE admin.op_menu SET menu_url = '/give-statistics/operate' WHERE menu_id = 6901;

-- [2026-10-02 정정] 시스템관리자 계정으로 확인한 결과 AS-IS opmanager 통계 nav에 '기부금 운영현황'
-- 메뉴가 실제로는 없다(재시드 파일의 display_flag='Y'가 AS-IS 실 운영본과 불일치했음). AS-IS에서
-- 안 쓰는 메뉴는 TO-BE도 동일하게 숨긴다([[as-is-parity-includes-disabled-state]]). 화면/컨트롤러는
-- 유지하고 nav에서만 숨김(AS-IS처럼 URL 직접 접근은 가능). 섹션(6900)+리프(6901) 모두 N.
-- [2026-10-02 재정정 - 위 블록은 실행하지 말 것] AS-IS op_menu/op_menu_right export를 전수 대조한
-- 결과 AS-IS도 6900/6901은 display_flag='Y'였고, 숨겨진 진짜 이유는 OP_MENU_RIGHT 행이 0건이라
-- 어떤 롤에도 부여되지 않은 것이었다. 애초에 AS-IS nav 쿼리(menu-mapper.xml)는 DISPLAY_FLAG를
-- WHERE에 쓰지 않는다(STATUS_CODE='1' + 권한조인). display_flag를 N으로 덮는 이 UPDATE는
-- 증상만 가린 오진이었으므로 migration-admin-menu-asis-sync.sql이 'Y'로 되돌린다.
-- UPDATE admin.op_menu SET display_flag = 'N' WHERE menu_id IN (6900, 6901);
