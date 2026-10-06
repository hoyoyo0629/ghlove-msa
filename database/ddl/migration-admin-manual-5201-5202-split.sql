-- 매뉴얼 2화면 분리 (메뉴 5201 관리자매뉴얼 / 5202 사용자매뉴얼)
--
-- AS-IS는 한 컨트롤러(ManualManagerController)가 두 화면을 담당한다.
--   /opmanager/manual/user/list    → 사용자매뉴얼 (g_mnl)           → 메뉴 5202
--   /opmanager/manual/manager/list → 관리자매뉴얼 (op_menu에 파일첨부) → 메뉴 5201
-- TO-BE는 5201·5202와 **중지된 5109까지 셋 다 /admin/manuals**를 가리켜 구분되지 않았다.
--
-- (1) 메뉴 URL 분리
--     5202 사용자매뉴얼 → /admin/manuals
--     5201 관리자매뉴얼 → /admin/manuals/manager
--     5109 '매뉴얼관리'(구)는 AS-IS에서 중지된 메뉴라 건드리지 않는다(display_flag='N', status_code='2').
--
-- (2) g_mnl 채번 시퀀스
--     AS-IS는 sequenceService.getId("G_MNL")로 채번한다. TO-BE 표에는 기본값이 없었다.
--
-- op_manual 표(TO-BE가 같은 용도로 새로 만든 것)는 남겨 둔다 - 두 표 모두 0행이라 옮길
-- 데이터가 없고, 표 정리는 사용자 판단 대상이다.

-- (2) 채번 시퀀스
create sequence if not exists admin.g_mnl_seq as integer start with 1 increment by 1;
select setval('admin.g_mnl_seq', coalesce((select max(mnl_sn) + 1 from admin.g_mnl), 1), false);

-- (1) 메뉴 URL 분리
update admin.op_menu set menu_url = '/admin/manuals'         where menu_id = 5202;
update admin.op_menu set menu_url = '/admin/manuals/manager' where menu_id = 5201;
