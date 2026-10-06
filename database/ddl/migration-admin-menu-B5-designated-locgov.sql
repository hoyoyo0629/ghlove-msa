-- B5 지정기부 지자체별통계 - 메뉴 17201을 전용 지자체별통계 화면으로 재배선.
-- B1에서 17201/17202를 임시로 /designated-projects/analysis(통합 모금분석)에 함께 걸어뒀던 것을,
-- AS-IS designated-donation/analysis/locgov.jsp를 완전 재현한 전용 화면으로 분리한다(월별통계 17202와 짝).
UPDATE admin.op_menu SET menu_url = '/designated-projects/analysis/locgov', display_flag = 'Y' WHERE menu_id = 17201;
