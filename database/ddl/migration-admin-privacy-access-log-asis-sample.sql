-- 시스템관리 > 엑셀다운로드사유관리(1411, admin.op_privacy_access_log) AS-IS 실데이터 샘플 30건
-- export: Desktop\고향사랑e음\1.AS-IS\1. DB\_op_privacy_access_log__202610071247.sql (2026-10-07)
-- 원본 표는 전체 접근로그(수억건 추정, 파일 2.9GB)라 TASK='엑셀 다운로드' + login_type='MANAGER'인
-- 행만 일부(화면 검증용 샘플 30건) 옮겼다. ip/프로필 컬럼은 AS-IS pCrypto 암호화 원문 그대로
-- (TO-BE는 복호화 모듈이 없어 화면에 암호문으로 보인다 - PrivacyAccessLog.java 주석과 동일한 상황).

INSERT INTO admin.op_privacy_access_log
    (id, created_at, ip, login_type, manager_id, method, name, reason_type, reason, task, url, user_id)
VALUES
    (14388919,'2026-01-06 14:20:46','^`0Ob3gi]6hYFlnWCZHjWTkA==','MANAGER',3988558,'POST','정산 확정 리스트 엑셀 다운로드','2','주문내역 확인 정산','엑셀 다운로드','/opmanager/remittance/confirm/list-excel',3988558),
    (14388889,'2026-01-06 14:20:23','^`pZT228WjMvx3nNTfQNiKaA==','MANAGER',4166042,'POST','전체 주문 엑셀 다운로드','2','업무참고용','엑셀 다운로드','/opmanager/order/list/order-excel-download',4166042),
    (14388800,'2026-01-06 14:19:08','^`Mg3FnNC2Vn4dk[Ra8oO[Qg==','MANAGER',3378905,'POST','배송준비중(모바일) 주문 엑셀 다운로드','4','답례품 지급 내역 확인 등','엑셀 다운로드','/opmanager/order/shipping-ready-mobile/order-excel-download',3378905),
    (14388970,'2026-01-06 14:21:33','^`7CPuNenYDzk4[k6IjOgwgw==','MANAGER',4104713,'POST','전체 주문 엑셀 다운로드','4','업체측 외상매출금 잔액확인서 발급 첨부자료용','엑셀 다운로드','/opmanager/order/list/order-excel-download',4104713),
    (14389720,'2026-01-06 14:35:47','^`6LDNRD1BMfN6jb4EsUS2aA==','MANAGER',47293,'POST','기부금 모금현황 상세 목록','1','모금실적확인','엑셀 다운로드','/opmanager/give/give-state/detail/download-excel',47293),
    (14389274,'2026-01-06 14:27:34','^`0S52f72OVFKTvfl9GtjXFQ==','MANAGER',3965816,'POST','전체 주문 엑셀 다운로드','2','답례품 주문내역 확인','엑셀 다운로드','/opmanager/order/list/order-excel-download',3965816),
    (14389269,'2026-01-06 14:27:28','^`7CPuNenYDzk4[k6IjOgwgw==','MANAGER',4104713,'POST','정산 확정 상세 리스트 엑셀 다운로드','4','외상매출금 잔액확인서 증빙 첨부용','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2065870',4104713),
    (14389256,'2026-01-06 14:27:15','^`icz9L0DOgMCmglt4JA6q5Q==','MANAGER',3475812,'POST','전체 주문 엑셀 다운로드','5','답례품 실적 확인','엑셀 다운로드','/opmanager/order/list/order-excel-download',3475812),
    (14389187,'2026-01-06 14:26:06','^`Mg3FnNC2Vn4dk[Ra8oO[Qg==','MANAGER',3378905,'POST','배송준비중(모바일) 주문 엑셀 다운로드','4','답례품 발송 내역','엑셀 다운로드','/opmanager/order/shipping-ready-mobile/order-excel-download',3378905),
    (14390704,'2026-01-06 14:53:01','^`tjQ]pImy[SeNLA9OtKw]LA==','MANAGER',2031959,'POST','전체 주문 엑셀 다운로드','1','답례품 제공실적확인','엑셀 다운로드','/opmanager/order/list/order-excel-download',2031959),
    (14390391,'2026-01-06 14:48:19','^`b1LYXlZeAnhOEcwJw5wDPQ==','MANAGER',3460256,'POST','전체 주문 엑셀 다운로드','2','통계자료 확용','엑셀 다운로드','/opmanager/order/list/order-excel-download',3460256),
    (14390235,'2026-01-06 14:45:27','^`bN2S8vdCixoXcD5ZQOAYyQ==','MANAGER',3607283,'POST','기부금 모금현황 상세 목록','1','기부금 모금 실적 확인','엑셀 다운로드','/opmanager/give/give-state/detail/download-excel',3607283),
    (14390078,'2026-01-06 14:42:17','^`0Ob3gi]6hYFlnWCZHjWTkA==','MANAGER',3988558,'POST','구매확정 주문 엑셀 다운로드','2','답례품 정산','엑셀 다운로드','/opmanager/order/confirm/order-excel-download',3988558),
    (14391727,'2026-01-06 15:13:14','^`6LDNRD1BMfN6jb4EsUS2aA==','MANAGER',4002532,'POST','기부금 모금현황 상세 목록','1','기부금 모금 실적 확인','엑셀 다운로드','/opmanager/give/give-state/detail/download-excel',4002532),
    (14391718,'2026-01-06 15:12:55','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067912',3387235),
    (14391673,'2026-01-06 15:12:05','^`wD8Ddzw1HfAPwNHLihktPQ==','MANAGER',3370927,'POST','구매확정 주문 엑셀 다운로드','2','답례품 주문내역 확인','엑셀 다운로드','/opmanager/order/confirm/order-excel-download',3370927),
    (14391668,'2026-01-06 15:12:01','^`NXm[8SMr9aiRg0E3vjAhcA==','MANAGER',3959887,'POST','정산 확정 리스트 엑셀 다운로드','4','정산용입니다.','엑셀 다운로드','/opmanager/remittance/confirm/list-excel',3959887),
    (14391606,'2026-01-06 15:11:06','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067911',3387235),
    (14391547,'2026-01-06 15:09:47','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067910',3387235),
    (14391537,'2026-01-06 15:09:28','^`oaJLDGdm7G4ITcwsTk[Gjw==','MANAGER',3317285,'POST','구매확정 주문 엑셀 다운로드','1','ㅁㄴㅇㅁㄴㅇㅁㄴㅇ','엑셀 다운로드','/opmanager/order/confirm/order-excel-download',3317285),
    (14391510,'2026-01-06 15:08:56','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067909',3387235),
    (14391475,'2026-01-06 15:07:57','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067908',3387235),
    (14391437,'2026-01-06 15:07:10','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067907',3387235),
    (14391412,'2026-01-06 15:06:33','^`98Zn7699Ck3u]DYUo1UeJw==','MANAGER',4887210,'POST','전체 주문 엑셀 다운로드','2','답례품 주문내역 확인','엑셀 다운로드','/opmanager/order/list/order-excel-download',4887210),
    (14391503,'2026-01-06 15:08:45','^`]HSZCGWxC0snBs4QiX1Z7w==','MANAGER',4525277,'POST','전체 주문 엑셀 다운로드','4','답례품 정산','엑셀 다운로드','/opmanager/order/list/order-excel-download',4525277),
    (14391403,'2026-01-06 15:06:15','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067906',3387235),
    (14391365,'2026-01-06 15:05:36','^`oaJLDGdm7G4ITcwsTk[Gjw==','MANAGER',3317285,'POST','구매확정 주문 엑셀 다운로드','1','ㅁㄴㅇㅁㄴㅇㅁㄴㅇ','엑셀 다운로드','/opmanager/order/confirm/order-excel-download',3317285),
    (14391313,'2026-01-06 15:04:35','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067905',3387235),
    (14391251,'2026-01-06 15:03:25','^`OeFiMVeHXdNcgKVcSA9xuQ==','MANAGER',3387235,'POST','정산 확정 상세 리스트 엑셀 다운로드','2','주문내역 확인','엑셀 다운로드','/opmanager/remittance/confirm/detailNew-excel/view/2067904',3387235),
    (14391051,'2026-01-06 15:00:15','^`e]z0r]XwZGDSOuoYWyMnuw==','MANAGER',3447883,'POST','기부금 모금현황 상세 목록','1','전체 기부자 이력관리','엑셀 다운로드','/opmanager/give/give-state/detail/download-excel',3447883)
ON CONFLICT (id) DO NOTHING;

SELECT setval('admin.op_privacy_access_log_seq', (SELECT max(id) FROM admin.op_privacy_access_log) + 1, false);
