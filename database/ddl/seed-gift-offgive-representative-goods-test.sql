-- 기탁서 등록(admin 15102) 기능테스트용 "지자체 오프라인 대표 답례품" 매핑 - 테스트 데이터 (2026-10-13).
-- 대상 DB: gift (public 스키마)
--
-- AS-IS 실데이터(export `_g_lclgv_off_rprs_gds_mng__202610071247.sql`, 약 290건)는 AS-IS 상품ID
-- (8322, 2015379 ...)를 가리켜 TO-BE 시드 상품(1000~1028)과 하나도 맞지 않아 그대로 넣을 수 없다.
-- 그래서 AS-IS 규칙(지자체당 최대 2개, 정렬순서 1·2, 옵션 없는 상품은 options='')대로 TO-BE 상품을
-- 매핑한다. 운영 데이터가 아니다 - frst_rgtr_id = 0 으로 구분한다.
--
-- 옵션 상품은 넣지 않는다: TO-BE gift는 S형 옵션값을 option_name1에 저장하는데 AS-IS 조회
-- (selectOffRprsOption)는 option_name2(옵션값)로 찾으므로 매칭되지 않는다(별도 보고 사항).
--
-- 테스트 경로:
--   서울 강남구(11230)      - 등록 시 서울 세외(goSeoul) 경로, 대표답례품 2개(2개 하드코딩 로직 확인)
--   부산 해운대구(26350)    - 서울 외 세외(goContry2 → nextBugaRequest) 경로, 1개
--   제주특별자치도(50000)   - 시도 선택만으로 조회됨(본청), 모금중 지정기부 사업(1100)도 있어 같이 확인
--
-- 되돌리기: DELETE FROM g_lclgv_off_rprs_gds_mng WHERE frst_rgtr_id = 0;

INSERT INTO g_lclgv_off_rprs_gds_mng (lclgv_cd, gds_id, sort_seq, options, frst_rgtr_id) VALUES
    ('11230', 1000, 1, '', 0),   -- 강남구 한우 선물세트 (80,000)
    ('11230', 1020, 2, '', 0),   -- 강남구 프리미엄 사과 선물세트 5kg (30,000)
    ('26350', 1001, 1, '', 0),   -- 해운대 건어물 세트 (35,000)
    ('50000', 1003, 1, '', 0),   -- 제주 감귤 생활용품 세트 (25,000)
    ('50000', 1014, 2, '', 0);   -- 제주 지역사랑상품권 5만원권 (30,000)

SELECT m.lclgv_cd, m.sort_seq, m.gds_id, i.item_name, i.sale_price, i.stock_quantity
  FROM g_lclgv_off_rprs_gds_mng m
  JOIN op_item i ON i.item_id = m.gds_id
 ORDER BY m.lclgv_cd, m.sort_seq;
