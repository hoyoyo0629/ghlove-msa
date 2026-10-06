---
name: receipt-commercial-sw-replacement
description: "[번복됨 2026-09-18] 기부영수증 상용SW(OZ Report·Fasoo)는 그대로 유지하기로 결정 - 자체 PDF 대체는 나중에 재검토(보류). PoC 코드는 남아있음"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-18T02:33:48.823Z
---

**⚠️ 결정 번복 (2026-09-18):** 기부영수증 출력의 **OZ Report·Fasoo 상용SW를 그대로 유지**하기로 함(발주처 방침). 자체 PDF 대체는 **적용하지 않고 보류** — "기록해뒀다 나중에 적용" 상태. 아래는 직전(2026-09-17) 대체 방향과 PoC 내용으로, **지금은 실행 대기(보류)**. 상용SW 유지가 현재 정본이므로, 영수증/확인증 화면은 AS-IS OZ Viewer + FSW 전제로 다룰 것. 자체 PDF 전환을 다시 꺼낼 때 이 메모리 갱신.

---
(이하 2026-09-17 대체 검토 내용 — 현재 보류)

기부금영수증 출력의 AS-IS 상용SW 2종을 자체 구현으로 대체하기로 함(2026-09-17, 고객 회의 제안). **전환이 아니라 상용SW 대체 개발**이라 신규 스코프임을 명시할 것.

**AS-IS 실체 (혼동 주의):**
- **Fasoo Secure Web(FSW) 4.2** = 브라우저 화면보호 클라이언트(네이티브 에이전트). AS-IS `ghlove-frontend/webDrm/`, `mypage/receiptPrint.html` **단 1곳**에서만 로드. OZ Viewer로 렌더된 영수증에 캡처/저장/소스보기 차단.
- **Fasoo Enterprise DRM 5** = 서버/DB 문서보안. ISP SW인벤토리에만 "재활용" 전제로 등장, 앱 코드엔 없음. **FSW와 별개 제품.**
- OZ Report = 영수증 렌더(`3.cntr_receipt_new.ozr`).

**판정:** RFP/ISP 어디에도 화면보안(FSW) 요구 근거 **없음**(요약본 기준). FSW는 원제품(SalesOn) 관성 잔재로 추정. → 상용SW 제거 가능. 상세 근거는 `docs/as-is-inventory-donation.md` §6.

**대체 원칙(고객 슬라이드 TO-BE PLAN, PoC로 실현):** 직인 미전송(조회 HTML엔 직인 없음) + 서버사이드 PDF 합성(비트맵 병합·해상도 상한) + AES-256-GCM(기존 SealCipher). 정본은 국세청 전자기부금영수증.

**구현 상태(공식 영수증 PoC 완료):** donation `ReceiptPdfService`(Java2D 래스터+PDFBox), `GET /receipts/official/{cntrSn}/pdf`, 조회 템플릿은 직인 제거+PDF버튼(`location.pathname+'/pdf'`로 프록시 접두사 무관). testuser01 검증 완료.

**남은 것:** ① 워터마크(발급일시·문서번호) 미적용 ② 확인증(`certificate-print.html`)도 직인 쓰므로 동일 패턴 확장 가능(별개 기능) ③ **최종 제거 전 발주처 「보안요구사항 정의서」 별첨에 출력물/화면 유출방지 항목 있는지 확인 필요**. 관련 [[saleson-original-product-leftovers]], [[check-as-is-source-when-analyzing]].
