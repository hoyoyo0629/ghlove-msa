-- 2026-10-19 송장 택배사를 AS-IS처럼 배송업체 ID(ord.op_delivery_company)로 저장하도록 바꾸면서, 예전 TO-BE 화면이 넣던
-- 임의 코드(CJ/POST/HANJIN/LOTTE)를 같은 업체의 ID로 바꾼다(CJ대한통운 2, 우체국택배 3, 한진택배 4, 롯데택배 5 -
-- op_delivery_company AS-IS 데이터 기준). 스마트택배 배치가 이 ID로 SMART_DELIVERY_COMPANY 코드를 찾는다. 실행 DB: ghlove_core.
UPDATE ord.od_order SET carrier_code = CASE carrier_code WHEN 'CJ' THEN '2' WHEN 'POST' THEN '3' WHEN 'HANJIN' THEN '4'
                                                         WHEN 'LOTTE' THEN '5' ELSE carrier_code END
 WHERE carrier_code IN ('CJ', 'POST', 'HANJIN', 'LOTTE');
UPDATE ord.od_shipment SET carrier_code = CASE carrier_code WHEN 'CJ' THEN '2' WHEN 'POST' THEN '3' WHEN 'HANJIN' THEN '4'
                                                            WHEN 'LOTTE' THEN '5' ELSE carrier_code END
 WHERE carrier_code IN ('CJ', 'POST', 'HANJIN', 'LOTTE');
