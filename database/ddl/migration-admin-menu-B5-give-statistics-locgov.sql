-- B5 기부 통계 '지자체별'(6707) - AS-IS give/statistics/locgov/list.jsp 전용 화면으로 재배선.
-- 6901(운영현황)은 전용화면 완성 시 분리 예정(현재 통합 /give-statistics 유지).
UPDATE admin.op_menu SET menu_url = '/give-statistics/locgov', display_flag = 'Y' WHERE menu_id = 6707;
