---
name: saleson-original-product-leftovers
description: AS-IS는 커머스 패키지(SalesOn) 위에 고향사랑e음을 얹은 구조 - 원제품 경로가 통째로 남아 있고 전용 경로만 실사용된다. 죽은코드 판정의 최대 변수
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-10T14:05:36.121Z
---

AS-IS `ghlove`는 온라인파워(주)의 커머스 패키지 **SalesOn + opframework 3.15.0** 위에 고향사랑e음을 얹은 구조다. 그래서 **같은 기능의 경로가 원제품용·고향사랑용 두 벌 있고, 실사용되는 것은 고향사랑 전용 경로뿐**이다. 전수 인벤토리(2026-09-10) 6개 도메인 전부에서 같은 패턴이 나왔다.

확인된 사례:
- **원제품 PC 쇼핑몰 + 모바일 웹 컨트롤러 61개 / 440 엔드포인트가 통째로 사장** — `views/front/`에 뷰가 16개(카테고리 조각·에디터 업로드)뿐이고, Vue SPA의 백엔드 호출 198건이 전부 `/api/*`다
- **범용 게시판 프레임워크 `com.onlinepowers.board`** 24 엔드포인트 / 47 쿼리 — live 프론트에서 `/board/` 링크 0건
- donation: `/api/ngdonation`(원본) vs `/api/regiontax`(적용본)
- point: `OP_POINT`(원제품 포인트) vs 기부포인트, `starpoint-mapper.xmlx`
- order: `save`/`pay`(일반 커머스 결제) vs `giveGoodsSavePay`(답례품 저장+결제)
- gift: 상품옵션·추가상품·오픈마켓·재고입고예정 쿼리 34개 사장
- 파일명 `*_saleson` 접미사는 원제품 원본이라는 표시

**Why:** 이걸 모르면 "MSA에 없다 = 재현 누락"으로 잘못 세게 된다. 실제로는 AS-IS에서도 도달 불가라 재현 대상이 아닌 것이 도메인마다 수십~수백 건씩 있다. 반대로 원제품 경로만 보고 "AS-IS는 이렇게 한다"고 판단하면 실제 로직을 놓친다.

**How to apply:** AS-IS에서 같은 기능의 경로가 둘 이상 보이면 먼저 어느 쪽이 실사용인지 가린다 — 프론트 호출부(`/tmp/live_grep.sh`), 뷰 존재 여부, 매퍼 호출부 3중으로 본다. 판정은 `죽은코드`로 목록에 남기되 재현 대상에서 뺀다. 단 `com.onlinepowers.framework.*` 네임스페이스 매퍼(77쿼리)는 인터페이스가 `libs/opframework-3.15.0.jar` 안에 있어 **소스 grep으로 사장 판정이 불가능하다** — 반드시 `보류`로 둔다. 절차는 [[as-is-inventory-procedure]], 기준은 [[scope-migration-not-greenfield]].
