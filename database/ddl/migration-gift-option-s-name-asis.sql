-- 선택형(S) 옵션 저장규칙을 AS-IS와 같게 이관 (2026-10-13).
-- 대상 DB: gift (public 스키마)
--
-- AS-IS OP_ITEM_OPTION의 선택형(S)은 option_name1 = 옵션 제목(예: 색상), option_name2 = 옵션 내용(예: 빨강)이다
-- (opmanager/item/form.jsp 옵션 적용: {'optionName1': 제목, 'optionName2': 내용}, 실데이터 31,583행 전부 이 모양 -
-- 예: '토마토' / '완숙 토마토 4kg'). TO-BE GiftOptionService는 값을 option_name1에 넣고 option_name2를 비워
-- 두고 있었다 - AS-IS 조회(selectOffRprsOption 등 option_name2 기준)와 어긋난다.
--
-- 이관 대상: 옵션 내용(option_name2)이 비어 있는 TO-BE S행. 기존 값은 option_name2(옵션 내용)로 옮기고,
-- option_name1(옵션 제목)에는 시드 답례품에 맞는 제목을 넣는다(운영 데이터 아님, TO-BE 시드 5건):
--   1025 강남구 수제 마들렌 3구 세트(플레인/딸기맛/초코맛) → '맛', 1026 [검증]옵션전부품절(레드/블루) → '색상'
-- 다른 답례품에 같은 모양의 S행이 생겨 있으면 제목을 정할 수 없어 아래 마지막 SELECT에 남는다(수동 확인).

UPDATE op_item_option
   SET option_name2 = option_name1,
       option_name1 = CASE item_id WHEN 1025 THEN '맛' WHEN 1026 THEN '색상' END
 WHERE option_type = 'S'
   AND (option_name2 IS NULL OR option_name2 = '')
   AND item_id IN (1025, 1026);

-- 남은 이관 대상(있으면 제목 확인 필요)
SELECT item_option_id, item_id, option_name1, option_name2
  FROM op_item_option
 WHERE option_type = 'S'
   AND (option_name2 IS NULL OR option_name2 = '');
