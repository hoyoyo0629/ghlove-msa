-- 개발 판매자의 지자체코드 보충 (2026-10-16, 답례품관리 목록 이식)
--
-- AS-IS 답례품관리 목록은 판매자의 지자체(OP_SELLER.LOCGOV_CODE)가 있는 답례품만 보여준다(S.LOCGOV_CODE IS NOT NULL, 지자체 검색·표시도 이 값).
-- 답례품제공자 관리 이식 전에 만든 개발용 판매자(9001~9015)는 이 값이 비어 있어 목록이 통째로 비었다. 그 판매자의 답례품이 가진
-- 지자체코드(OP_ITEM.LOCGOV_CODE)가 하나뿐인 경우에 한해 그 값으로 채운다. 이미 값이 있는 판매자는 건드리지 않는다.
--
-- 적용: gift DB(public) - psql -d gift 로 실행. 여러 번 실행해도 안전.

UPDATE op_seller s
   SET locgov_code = x.locgov_code
  FROM (SELECT seller_id, MIN(locgov_code) AS locgov_code
          FROM op_item
         WHERE locgov_code IS NOT NULL AND locgov_code <> ''
         GROUP BY seller_id
        HAVING COUNT(DISTINCT locgov_code) = 1) x
 WHERE s.seller_id = x.seller_id
   AND (s.locgov_code IS NULL OR s.locgov_code = '');
