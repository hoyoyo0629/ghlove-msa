-- B5 지정기부 월별통계(특정사업 월별통계) - 메뉴 17202를 전용 월별통계 화면으로 재배선.
-- B1에서 17201/17202를 임시로 /designated-projects/analysis(통합 모금분석)에 함께 걸어뒀으나,
-- AS-IS designated-donation/analysis/month.jsp를 완전 재현한 전용 화면이 생겨 월별통계를 분리한다.
-- 17201(지자체별통계)은 추후 analysis/locgov 전용화면 완성 시 분리 예정(현재는 통합화면 유지).
UPDATE admin.op_menu SET menu_url = '/designated-projects/analysis/month', display_flag = 'Y' WHERE menu_id = 17202;
