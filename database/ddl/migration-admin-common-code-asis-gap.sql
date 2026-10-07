-- [2026-10-07 후속 조율로 폐기] admin.admin_common_code는 같은 날 뒤이은 조율로 DROP됐다
-- (공통코드 표를 AS-IS 실명 op_common_code 하나로 통합). 재실행하면 "relation does not exist"
-- 에러만 난다. 이 파일이 하던 71유형/1091행 대조는 이제 op_common_code 쪽에서 이미 끝난
-- 상태다(여기서 찾은 ORDER_STATUS 98/99 2건도 op_common_code에 이미 들어있음, 2026-10-07
-- 확인). 아래는 원문 그대로 보존 - 대조 방법론 참고용.
--
-- AS-IS OP_COMMON_CODE(운영DB export) ↔ TO-BE admin.admin_common_code 전수 대조 결과 보정.
-- 대조: AS-IS 71개 code_type / 1091행이 ADMIN_COMMON_CODE에 이미 전부 이식돼 있었고(code_type 누락 0),
-- 행 단위로 AS-IS에만 있던 것은 아래 2건뿐이다. TO-BE에만 있는 7개 code_type
-- (BSNS_PURPS_CODE/DELIVERY_CARRIER/MANAGER_STATUS/NOTICE_CATEGORY/POLICY_TYPE/POPUP_TYPE/
--  SETTLEMENT_STATUS)은 TO-BE가 의도적으로 추가한 의미코드라 손대지 않는다.
-- POP_ADMIN의 period/period2(값이 '20260803092026081323' 형태의 팝업 노출기간)는 운영 중 계속
-- 바뀌는 운영데이터라 동기화 대상에서 제외했다.
-- 재실행 가능.
INSERT INTO admin.admin_common_code (code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code)
SELECT v.code_type, v.code_language, v.id, v.label, v.detail, v.ordering, v.use_yn, v.up_id, v.code_value, v.extension_code, v.mapping_code
FROM (VALUES
  ('ORDER_STATUS', 'ko', '98', '주문취소완료', NULL, 9, 'N', NULL, NULL, NULL, NULL),
  ('ORDER_STATUS', 'ko', '99', '주문취소완료', NULL, 10, 'N', NULL, NULL, NULL, NULL)
) AS v(code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code)
WHERE NOT EXISTS (
  SELECT 1 FROM admin.admin_common_code a
   WHERE a.code_type = v.code_type AND a.code_language = v.code_language AND a.id = v.id);
