-- B5 기부 통계 '전체'(6706) - AS-IS give/statistics/all/detail.jsp 전용 화면으로 재배선.
-- 기존엔 6706/6707/6901이 통합화면 /give-statistics 하나로 묶여 있었으나, AS-IS대로 분리한다.
-- 6707(지자체별)·6901(운영현황)은 각 전용화면 완성 시 순차 분리 예정(현재는 통합화면 유지).
UPDATE admin.op_menu SET menu_url = '/give-statistics/all', display_flag = 'Y' WHERE menu_id = 6706;
