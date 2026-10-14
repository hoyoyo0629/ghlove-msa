-- 기부 수납일(G_CNTR.STTEMNT_PAY_DE) 백필 (2026-10-14).
-- 대상 DB: ghlove_core (donation 스키마)
--
-- AS-IS는 수납완료로 바꾸는 모든 UPDATE(ngdonation sunapSuccess/sunapResultSuccess, externalapi sunapSuccess,
-- give-state sunapUpdateY)에서 STTEMNT_PAY_DE를 채운다. TO-BE completeDonation은 수납확인 결과의 수납일을
-- donation_levy에만 남기고 g_cntr에는 넣지 않아, 수납완료 기부 전부가 수납일이 비어 있었다(코드는 같은 날 수정).
-- 기존 수납완료분은 donation_levy.sunap_date(수납확인 결과)로, 그것도 없으면 AS-IS sunapSuccess의 대체값처럼
-- 기부일(cntr_de)로 채운다.

UPDATE donation.g_cntr c
   SET sttemnt_pay_de = coalesce(
           (SELECT to_char(l.sunap_date, 'YYYYMMDD') FROM donation.donation_levy l WHERE l.cntr_sn = c.cntr_sn),
           c.cntr_de)
 WHERE c.cntr_sttus_code = 'COMPLETED'
   AND c.sttemnt_pay_de IS NULL;
