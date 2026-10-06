# gift 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적/독자**: AS-IS "서비스로직도 화면단도 똑같이" 재현 전수 갭 목록. 방식 `[[as-is-parity-exhaustive-audit-method]]`. 작성 2026-09-21.
>
> **★ gift는 이미 전수 기능감사가 있다: `docs/as-is-feature-audit-gift.md`** (AS-IS 기능 전수 목록 + 미구현 Gap 상세 + 우선순위). 이 문서는 그것을 **gift parity audit의 정본**으로 삼고, 이번 세션(2026-09-21) 변경 델타와 추가 발견만 갱신한다.

## 1. 기존 전수감사 요약 (as-is-feature-audit-gift.md)

미구현(X)으로 이미 목록화된 것 — 우선순위별:
- **높음**: 상품 옵션(단일/2단/3단/텍스트옵션/추가구성), **배송비 정책**(출고지/기본배송비/무료기준/도서산간), 판매자 셀프서비스·미니몰·PKI, 상품 수정신청 승인 워크플로우
- **중간**: 도서산간 우편번호 마스터, 반품/교환 배송비, 판매자 하위직원, 재입고 알림, 입점문의, 랭킹관리, 카테고리 3단 트리/속성필터, 검색어 관리(자동완성/인기/금칙어)
- **낮음**: 판매처(오프라인 매장), 상품 복제등록, 다중이미지 순서변경

→ 상세·근거는 `as-is-feature-audit-gift.md` 참조. **이 목록이 gift 구현 백로그**다.

## 2. 이번 세션(2026-09-21) 델타 — 갱신

| 항목 | 기존 상태 | 현재 | 비고 |
|---|---|---|---|
| **상품 옵션** | 미구현(X) | **부분 구현** | **단일옵션(S) 선택 + 장바구니·주문·주문상세 반영 완료**(이번 세션). 단 **2단/3단(S2/S3)·텍스트옵션(각인)·추가구성상품은 여전히 X**. `[[order-single-item-vs-multiitem-decision]]` 재설계 시 옵션은 OrderItem 속성으로 이관 |
| 판매자 로그인 기반 전환 | — | 완료 | `[[gift-seller-portal-unauthenticated]]` 2026-09-10 |

## 3. 추가 발견 (이번 전수에서 새로 확인)

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 3-1 | **답례품 상세 수량 선택(+/− 버튼)·최대주문수량(orderMaxQuantity)** — `items/details-main.html`에 수량 조절 UI + "최대 주문 수량" 표시 | **✅완료(2026-09-21)**: Gift 엔티티 orderMaxQuantity 매핑 + GiftDetailView +/− 선택(1~min(최대,재고) 클램프)·최대수량 표시·선택수량 장바구니/구매 반영. **서버측 강제 완료**: GiftItemInfo에 orderMaxQuantity 전파 + CartService 담기/수량변경/체크아웃 3경로 requireOrderMaxQuantity 검증. | **O** |
| 3-2 | 옵션별 재고·품절 표시(재고 N개/[품절]) | **✅완료(2026-09-21)**: OptionDto에 stockTracked/stockQuantity 추가, 옵션 select에 `[품절]` 또는 `| 재고 N개`(optionStockFlag=='Y' && qty>0) AS-IS와 동일 렌더 | **O** |
| 3-3 | 품절 상품 표시(itemSoldOutFlag) | soldOut 처리 | **O** |
| 3-4 | 후기/문의(Q&A) 작성·표시 | GiftDetailView 재현 | **O** |
| 3-5 | 관심답례품(위시) 토글 모달 | 선택/해제 모달 | **O**(이번 세션) |

## 4. ★ gift 최종 갭 목록 (확정)

**정본 = `as-is-feature-audit-gift.md`의 미구현 목록** + 아래 델타:
- 옵션: 단일옵션은 이번 세션 완료, **텍스트옵션·2/3단·추가구성 X**
- **수량 선택 UI·최대주문수량**(3-1) — ✅완료(2026-09-21)
- 옵션 재고 수치 표시(3-2) — ✅완료(2026-09-21)
- 배송비 정책·판매자 셀프서비스·카테고리트리·검색어관리 등 **높음/중간 항목은 as-is-feature-audit-gift.md 그대로 유효**

> **gift audit 완료.** 갭 정본은 기존 `as-is-feature-audit-gift.md` + 이번 델타(옵션 부분완료, 수량선택 X). 다음: order 전수조사.