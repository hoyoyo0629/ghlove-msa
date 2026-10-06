---
name: pending-verify-dormancy-receipt
description: 2026-09-17 구현한 휴면해제·영수증출력 변경은 donation·member 재기동 후 2026-09-18(내일) 5173에서 수동검증 대기 - 컴파일/빌드는 통과
metadata: 
  node_type: memory
  type: project
  modified: 2026-09-17T12:51:33.312Z
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
---

**2026-09-17 저녁 시점: 코드는 완료(compileJava·vite build 통과)했으나 런타임 미검증.** 사용자가 집이라 재기동 불가, 2026-09-18(다음 근무일)에 회사에서 확인 예정. 검증 전까지 이 변경들이 실동작 확인됐다고 단정하지 말 것.

**재기동 필요:** donation(8082)·member(8081). storefront(5173)는 dev면 자동 반영.

**재기동 필요 서비스 추가**: gift(8084)도 — E 수정으로 GiftPublicApi detail DTO·GiftController 변경됨.

**검증 항목:**
1. **영수증 출력**(donation) - 기부내역(MyDonationsView) → 완료건 "영수증출력" → PC는 팝업으로 영수증+PDF, 없는/미완료 건번호는 `alert("영수증 정보가 없습니다.")` 후 창 닫힘, 모바일은 `alert("영수증 출력은 PC에서 확인 부탁드립니다.")`. 신규 엔드포인트 `GET /api/my/receipts/official/{cntrSn}` 때문에 **재기동 전엔 404가 정상**(사용자가 이미 404 목격).
2. **휴면해제**(member) - 휴면(상태4) 계정으로 로그인 → 비번 본인확인 후 `SLEEP_USER` → 로그인 화면에서 `confirm("휴면해제 하시겠습니까?")` → 예 → `/api/auth/recovery`로 해제 → "다시 로그인" 안내. 링크·재입력 폼 없음(AS-IS op.saleson.js:1169 동일).
3. **[AS-IS 충실도 감사 수정분, 2026-09-17]** — 상세는 `docs/as-is-fidelity-audit-2026-09-17.md`:
   - **A1 비번만료/임시비번**(member): passwordType 'T' 회원 로그인 → "임시 비밀번호 사용자 입니다." → find-idpw. 만료일 지난 회원 로그인 → 변경 모달("변경하고 로그인"/"나중에 변경"). (테스트 데이터: OP_USER의 PASSWORD_TYPE='T' 또는 PASSWORD_EXPIRED_DATE 과거일자 세팅 필요)
   - **D1/D2 완료알림**(order/gift): 주문취소·구매확정·반품/교환·문의등록·장바구니삭제 시 완료 문구 뜨는지.
   - **E 재입고·좋아요**(gift): 품절 답례품 상세 "재입고 알림" 버튼, 상품평 "좋아요 N" 버튼.
   - **A3 세션만료 401**(storefront 전용, 재기동 불필요): 로그인 후 쿠키(GH_AUTH) 만료/삭제 상태에서 보호 API 호출(예: 마이페이지 액션) → "로그인 후 이용이 가능합니다." alert + `/login?target=` 이동되는지.
   - **G 체크아웃 검증**(storefront 전용): 체크아웃에서 받는분/연락처/우편번호/배송지/상세주소 비운 채 결제 → 각 필드 alert로 막히는지, 포인트 부족 시 "…포인트가 부족합니다." 뜨는지.

관련: [[thymeleaf-duplicate-cleanup-deferred]](2차 진행분), [[check-as-is-source-when-analyzing]](AS-IS 흐름추적 원칙). 검증 끝나면 이 메모리 삭제.
