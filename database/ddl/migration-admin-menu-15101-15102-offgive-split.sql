-- 오프라인기부금접수 구조갭 해소: 15101(기부금 접수관리=목록)과 15102(기탁서 등록=접수 폼)가
-- TO-BE에서 같은 URL(/offgive)을 가리키고 있었다.
--
-- AS-IS export(OP_MENU_202610010945.sql) 실측:
--   (15101,15100,'기부금 접수관리','/opmanager/offgive/list/',2,'Y','1')
--   (15102,15100,'기탁서 등록',   '/opmanager/offgive/create',1,'Y','1')
-- 즉 15101은 목록, 15102는 접수(기탁서) 등록 폼인 별개 화면이다
-- (4402·4501, 19101·19102와 같은 유형의 구조갭).
--
-- 15101은 AS-IS 형태로 이식 완료(/offgive/list - OffgiveReceiptAdminController).
-- 15102는 기존 TO-BE 접수 폼(/offgive/new)을 가리키게 한다 - AS-IS 기탁서 등록 화면(form.jsp
-- 1,995줄 + 본인인증 SCI 연계·지방세외 부과요청·서울 기부금 조회·기탁서 서식 팝업 3종)은
-- 아직 이식하지 않았고 납부 게이트웨이 라운드·본인인증 연계 결정과 함께 진행해야 한다.
-- 예전 /offgive 링크는 OffgiveController가 /offgive/list로 리다이렉트한다.

UPDATE admin.op_menu SET menu_url = '/offgive/list' WHERE menu_id = 15101;
UPDATE admin.op_menu SET menu_url = '/offgive/new'  WHERE menu_id = 15102;

SELECT menu_id, menu_name, menu_url, status_code
  FROM admin.op_menu
 WHERE menu_id IN (15101, 15102)
 ORDER BY menu_id;
