-- 시스템관리 > 배송업체 관리 (ord.op_delivery_company) AS-IS 실데이터 적재
-- export: Desktop\고향사랑e음\1.AS-IS\1. DB\_op_delivery_company__202610071247.sql (2026-10-07)
-- TO-BE는 테이블/컬럼은 이미 AS-IS와 동일하게 존재하나 데이터 0건 상태였음(관리자 등록화면만 신규).

INSERT INTO ord.op_delivery_company
    (delivery_company_id, delivery_company_name, tel_number, delivery_company_url, send_flag, delivery_number_parameter, use_flag)
VALUES
    (2000101,'투데이','1544-6213','https://www.amazing.today/track','1',NULL,'Y'),
    (4,'한진택배','1588-0011','https://www.hanjin.co.kr/kor/CMS/DeliveryMgr/WaybillResult.do?mCode=MN038&schLang=KR&wblnum=%INVOICE%','1',NULL,'Y'),
    (5,'롯데택배','1588-2121','https://www.lotteglogis.com/open/tracking?invno=%INVOICE%','1',NULL,'Y'),
    (6,'로젠택배','1588-9988','https://www.ilogen.com/web/personal/trace/%INVOICE%','1',NULL,'Y'),
    (8,'CVSnet 편의점택배','1577-1287','https://www.cvsnet.co.kr/invoice/tracking.do?invoice_no=%INVOICE%','1',NULL,'Y'),
    (34,'우편등기','','https://service.epost.go.kr/iservice/usr/trace/usrtrc001k01.jsp','1',NULL,'N'),
    (10,'경동택배','080-873-2178','https://kdexp.com/main.kd','1',NULL,'Y'),
    (33,'우편등기','','https://service.epost.go.kr/iservice/usr/trace/usrtrc001k01.jsp','1',NULL,'N'),
    (12,'일양로지스','1588-0002','http://www.ilyanglogis.com/functionality/tracking_result.asp?hawb_no=%INVOICE%','1',NULL,'Y'),
    (31,'우편등기','1588-1300','https://service.epost.go.kr/trace.RetrieveDomRigiTraceList.comm?sid1=%INVOICE%','1',NULL,'Y'),
    (32,'우편등기','','https://service.epost.go.kr/iservice/usr/trace/usrtrc001k01.jsp','1',NULL,'N'),
    (14,'건영택배','1588-9966','http://www.kunyoung.com/goods/goods_01.php?mulno=%INVOICE%','1',NULL,'Y'),
    (15,'천일택배','051-647-1001','http://www.chunil.co.kr/HTrace/HTrace.jsp?transNo=%INVOICE%','1',NULL,'Y'),
    (16,'EMS','1588-1300','https://service.epost.go.kr/trace.RetrieveEmsRigiTraceList.comm?POST_CODE=%INVOICE%&displayHeader=N','1',NULL,'N'),
    (17,'DHL','1588-0001','https://mydhl.express.dhl/kr/ko/tracking.html#/results?id=%INVOICE%','1',NULL,'N'),
    (18,'TNT Express','1588-0588','http://www.tnt.com/express/ko_kr/site/home/applications/tracking.html?cons=%INVOICE%','1',NULL,'N'),
    (19,'UPS','1588-6886','https://wwwapps.ups.com/WebTracking/track?track=yes&loc=en_kr&trackNums=%INVOICE%','1',NULL,'N'),
    (20,'Fedex','080-023-8000','https://www.fedex.com/apps/fedextrack/?action=track&ascend_header=1&clienttype=dotcomreg&cntry_code=kr&language=korean&tracknumbers=%INVOICE%','1',NULL,'N'),
    (21,'USPS','','https://tools.usps.com/go/TrackConfirmAction?tLabels=%INVOICE%','1',NULL,'N'),
    (22,'i-Parcel','','https://tracking.i-parcel.com/Home/Index?trackingnumber=%INVOICE%','1',NULL,'N'),
    (30,'대신택배','043-222-4582','https://www.ds3211.co.kr/freight/internalFreightSearch.ht?billno=%INVOICE%','1',NULL,'Y'),
    (24,'LX판토스','02-3771-2114','http://totprd.pantos.com/jsp/gsi/vm/popup/notLoginTrackingListExpressPoPup.jsp?quickType=HBL_NO&quickNo=%INVOICE%','1',NULL,'N'),
    (25,'GSMNtoN','','http://expressweb.co.kr/member/loginTracing.do?fwdCode=gexp&bound=AI&langCode=KOR&DeliveryCode=&HBLNO=%INVOICE%','1',NULL,'N'),
    (26,'유한회사 우리택배','1533-2010','http://honamlogis.co.kr/page/?pid=tracking_number','1',NULL,'N'),
    (3,'우체국택배','1588-1300','https://service.epost.go.kr/trace.RetrieveDomRigiTraceList.comm?sid1=%INVOICE%&displayHeader=N','1',NULL,'Y'),
    (2,'CJ대한통운','1588-1255','https://www.cjlogistics.com/ko/tool/parcel/newTracking?gnbInvcNo=%INVOICE%','1',NULL,'Y'),
    (27,'농협택배','1644-6702','https://ex.nhlogis.co.kr/dlvy/dlvy/view.do?invNo=%INVOICE%','1',NULL,'Y'),
    (28,'합동택배','1899-3392','https://hdexp.co.kr/delivery_search.hd','1',NULL,'Y'),
    (29,'직접배송/수령','1522-2431','https://shop.ilovegohyang.go.kr/kwa-shop_mypage','1',NULL,'Y')
ON CONFLICT (delivery_company_id) DO NOTHING;

-- 신규등록 시퀀스가 기존 최대 ID(2000101)와 충돌하지 않도록 보정
SELECT setval('ord.op_delivery_company_delivery_company_id_seq', 2000101, true);
