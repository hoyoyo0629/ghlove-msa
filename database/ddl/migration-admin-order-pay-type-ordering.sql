-- ORDER_PAY_TYPE 공통코드 ordering 보정 (2026-10-14)
--
-- 배경: 결제타입별 매출(sales/payment) 화면에서 체크박스/표 순서가 AS-IS와 다르다는 지적.
-- TO-BE 내부(체크박스 ↔ 표① ↔ 표②)는 서로 일치했지만, AS-IS 실제 화면 캡처와 비교하니
-- 전체 순서 자체가 AS-IS와 달랐다. 원인은 ORDER_PAY_TYPE 12종 중 7종(실시간 계좌이체·포인트·
-- PAYCO 간편결제·네이버페이·카카오페이·휴대전화·에스크로)의 ordering이 비어 있고,
-- 나머지(신용카드·은행 입금·가상계좌·현금·기부포인트 등)만 부분적으로 값이 있어
-- "NULLS FIRST" 규칙상 빈 값 그룹이 앞으로 몰려 전체 순서가 뒤섞여 있었다.
--
-- 사용자가 AS-IS 화면을 직접 캡처해 보여준 실제 순서를 ordering 1~12로 그대로 반영한다:
--   1 실시간 계좌이체(realtimebank) · 2 포인트(point) · 3 PAYCO 간편결제(payco) ·
--   4 네이버페이(naverpay) · 5 카카오페이(kakaopay) · 6 휴대전화(hp) · 7 에스크로(escrow) ·
--   8 신용카드(card) · 9 은행 입금(bank) · 10 가상계좌(vbank) · 11 현금(cash) ·
--   12 기부포인트(give-point)
--
-- ORDER_PAY_TYPE은 이 화면(admin ShopStatisticsTierBController) 외에 다른 TO-BE 화면이
-- 참조하지 않는다(2026-10-14 전수 확인) - 이 변경이 다른 화면에 영향을 주지 않는다.
-- use_yn='N'인 4종(대금상환·편의점·ARS·수기결제)은 화면에 보이지 않으므로 손대지 않았다.

UPDATE admin.op_common_code SET ordering = 1  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'realtimebank';
UPDATE admin.op_common_code SET ordering = 2  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'point';
UPDATE admin.op_common_code SET ordering = 3  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'payco';
UPDATE admin.op_common_code SET ordering = 4  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'naverpay';
UPDATE admin.op_common_code SET ordering = 5  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'kakaopay';
UPDATE admin.op_common_code SET ordering = 6  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'hp';
UPDATE admin.op_common_code SET ordering = 7  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'escrow';
UPDATE admin.op_common_code SET ordering = 8  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'card';
UPDATE admin.op_common_code SET ordering = 9  WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'bank';
UPDATE admin.op_common_code SET ordering = 10 WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'vbank';
UPDATE admin.op_common_code SET ordering = 11 WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'cash';
UPDATE admin.op_common_code SET ordering = 12 WHERE code_type = 'ORDER_PAY_TYPE' AND id = 'give-point';
