# AS-IS 인벤토리 — gift 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

## 1. 화면 생사 판정

### 1-1. 래퍼 → 실화면 2단 구조 (donation과 동일)
| 래퍼 | 실화면 |
|---|---|
| `goods/index.html` (inbound 160) | `goods/index-main.html` — `location.replace('/goods/index-main.html' + search)` (`:82~88`) |
| `goods/searchGoods.html` (41) | `goods/searchGoods-main.html` (34) |
| `items/details.html` (58) | `items/details-main.html` (58) — `:71~77` |

### 1-2. live
`goods/index-main.html`(답례품 목록), `goods/searchGoods-main.html`(검색), `items/details-main.html`(상세),
`category/index.html`(160, 카테고리), `community-business/communityList-main.html`(마을기업),
`featured/eventList.html`(21)·`eventDetail.html`(5)·`detail.html`(12) — **지역이벤트**,
`components/layouts/item_tab-ali.vue` — 상세화면 탭 컴포넌트(`items/details-main.html:1241`에서 활성 등록),
mypage 답례품분: `review.html`(12), `writeReview.html`(2), `favorItem.html`(10), `inquiry.html`(11), `inquiryItem.html`(10)

### 1-3. **`catalog/` 디렉터리 전체가 도달불가**
`catalog/`에는 `catalog-main.html`·`catalog-faq.html`·`index_old.html` 세 파일이 있는데,
- 유일한 진입 리다이렉트가 `header_ali_20240529.vue:1023`의 `$s.redirect('/catalog/index.html')`인데 **`catalog/index.html`은 존재하지 않는다**
- 그 `header_ali_20240529.vue` 자체도 **활성 등록 0** (사장 레이아웃)
- `catalog-main.html`을 가리키는 유일한 참조는 `header_news.vue:29`의 `menuUrl` 비교문이고, `header_news.vue`는 `catalog-main`/`catalog-faq` 두 화면만 등록한다 → **서로만 참조하는 고립 덩어리**

→ `catalog/` 3화면 + `header_news.vue` + `/api/catalog/*` 6엔드포인트 + `catalog-mapper.xml` 34쿼리가 통째로 사장 후보다.
> member의 `sign-certificate.html`(오타난 파일명), donation의 `guide4.html`(파일 없음)과 같은 유형 — **존재하지 않는 파일을 가리키는 진입점**.

### 1-4. 지역이벤트의 진입은 푸터다
`featured/eventList.html`은 살아있는 푸터 `footer_ali.vue`(활성 등록 81곳)의 `:225,229`에서 `?ing=Y`(진행중)·`?ing=N`(종료)로 진입한다. `donation-lnb_ali.vue`에도 메뉴가 있으나 그 컴포넌트는 사장이다(donation §1-3).
제목은 `기부하기 > 지역이벤트`지만 데이터는 `featured-mapper`(답례품 기획전)이라 gift에 배정한다.

## 2. 컨트롤러 엔드포인트

### 2-1. `/api/item` (ItemController, 13 + 상세/목록 2)
프론트는 `modules/op.saleson.js` 래퍼를 거치므로 **래퍼 호출처**로 판정했다.

| 엔드포인트 | 래퍼 | live 호출처 | 판정 |
|---|---|---|---|
| `GET /{itemUserCode}` | `getItem` | `item_tab-ali.vue`, `details-main`, `donation-main`, `totalsearch/index` | live |
| `GET /reviews` | `getItemReviewsForDetail` | `item_tab-ali.vue`, `details-main` | live |
| `GET /review/info/{code}` | `getItemReviewInfo` | `mypage/writeReview.html` | live |
| `POST /review/add-like/{id}` | `addItemReviewLike` | `item_tab-ali.vue` | live |
| `POST /wishlist` | `addToWishList` | 14개 화면(목록·상세·카테고리·이벤트·마을기업·지자체선택 등) | live |
| `POST /remove-wishlist` | `removeToWishList` | `details-main`, `event/seasonal`, `modules/goods.item.js` | live |
| `GET relation` | `viewItemRelations` | `item_tab-ali.vue`, `details-main` | live |
| `GET /qna` | `getItemQna` | `item_tab-ali.vue`, `details-main` | live |
| **`GET /restock`** | `getRestockNotice` | `item_tab-ali.vue`, `details-main` | live |
| **`POST /restock`** | `restockNotice` | `item_tab-ali.vue` | live |
| `GET /list/list-new` | `getItemsNew` | `goods/index-main`, `searchGoods-main`, `donation/list-select` | live |
| `GET /getUserCntrPoint` | (직접) | `items/details-main.html` | live |
| `GET /coupons` | `downloadItemCouponList` | `item_tab-ali.vue` | live (쿠폰은 [[coupon-feature-unused-hide-ui]]) |
| `POST /download-all-coupons` | `downloadAllItemCouponList` | `item_tab-ali.vue` | live (동상) |
| `POST /review` | (직접) | 확인필요 | |

### 2-2. `/api/category` (7) — 전부 live
`updated-check`(`op.vue.js`), `''`(목록), `/current`, `/filter`, `/price-areas`, `/category-path`, `/best`

### 2-3. `/api/categories/searchResult` — **죽은코드**
래퍼 `searchGoods`의 호출처가 **0곳**이다. 검색 화면 `goods/searchGoods-main.html`이 실제로 쓰는 것은 `getItemsNew`(`/api/item/list/list-new`) + `getCategoryPath` + 관심지자체 2종이다.

### 2-4. `/api/catalog` (6) — **전부 죽은코드 후보**
`getCatalogYearNoList`, `getCatalogNewItem`, `getDsgnDonationItem`, `getLocgovFavItem`, `getCatalogSeasonalItem`, `getCatalogMainInfo`. 유일한 소비 화면이 §1-3의 고립된 `catalog/` 덩어리다.

### 2-5. `/api/display` (7)
`best`, `md`, `new`, `group-best`, `lately`, `promotion`, `style-book` — 래퍼는 전부 존재. 개별 호출처는 메인화면(common 도메인) 소관이 섞여 있어 common 시트에서 함께 정리한다.

## 3. 매퍼 쿼리 (357 중 사장 34)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `item-mapper.xml` | `saleson.shop.item.ItemMapper` | 173 | **22** |
| `categories-mapper.xml` | `...categories.CategoriesMapper` | 52 | 2 |
| `catalog-mapper.xml` | `...catalog.CatalogMngMapper` | 34 | 1 (+§1-3이면 전량) |
| `display-mapper.xml` | `...display.DisplayMapper` | 30 | 0 |
| `featured-mapper.xml` | `...featured.FeaturedMapper` | 25 | 1 |
| `item-front-mapper.xml` | `...item.ItemFrontMapper` | 18 | 0 |
| `wishlist-mapper.xml` | `...wishlist.WishlistMapper` | 13 | 4 |
| `item-addition-mapper.xml` | `...item.ItemAdditionMapper` | 6 | **5 / 6** |
| `inquiry-mapper.xml` | `...inquiry.InquiryMapper` | 6 | 0 |
| **합계** | | **357** | **35** |

### 3-1. 사장 쿼리의 성격 — 상품옵션·추가상품
`item-mapper` 사장 22건 중 **13건이 `ItemOption` 관련**이다: `getItemOptionCodeById`·`getItemOptionDefaultGroupList`·`getItemOptionGroupList`(XML 고아), `getItemOptionByItemOption`, `getItemOptionListByItemOptionIds(ForOrder)`, `getItemOptionListByItemOptions`, `getItemOptionListByWishlistHash`, `getItemListByOptionCode`, `updateItemOption`, `updateItemOptionStockScheduleDate`, `getItemAddOptionCountByParam`, `getItemAddOptionListByParam`.
`item-addition-mapper`는 **6개 중 5개가 사장**(추가상품 기능 자체가 미사용).
나머지: `deleteItemById`, `insertFullItem`, `insertItemListForExcel`, `mergeItemHits`, `updateItemHitsAll`, `updateItemStockScheduleDate`, `getItemCountByDeliveryChargeId`, `getItemCountByDeliveryId`, `getItemListByOpenMarketItemParam`(오픈마켓 연동 잔재)
`wishlist` 사장 4건: `deleteWishlistById`, `getWishlistById`, `getWishlistDuplicate`, `updateWishlist`(XML 고아)

> 원제품(SalesOn)의 일반 커머스 기능(상품옵션·추가상품·오픈마켓·재고입고예정)이 고향사랑e음 답례품에서는 쓰이지 않는다는 뜻이다. point의 `OP_POINT` 체계, `starpoint-mapper.xmlx`와 같은 계열의 잔재다.

## 4. MSA 대응 요약

MSA gift는 컨트롤러 다수 / 엔드포인트 약 150개로 **대부분이 운영관리 API**다(카테고리 3계층·팀그룹·기획전·랭킹·스타일북·모바일카테고리편집·판매자·브랜드·리뷰관리).

**대응있음**
- `goods/index-main` → `GET /`, `list.html`
- `goods/searchGoods-main` → 검색(9파일에 구현)
- `items/details-main` + `item_tab-ali.vue` → `GET /gifts/{itemId}`, `detail.html`
- `category/index` → `GET /api/categories`
- `community-business/communityList-main` → `GET /community-business`
- mypage `review`/`writeReview` → `GET /my/reviews`, `POST /gifts/{id}/reviews`
- mypage `inquiry`/`inquiryItem` → `GET /my/qna`, `/my/inquiries`, `POST /gifts/{id}/inquiries`
- mypage `favorItem`(관심상품) → `GET /wishlist`, `wishlist.html`

**재현누락**
1. **재입고 알림**(`GET/POST /api/item/restock`) — AS-IS 상세화면 탭에서 살아있다. MSA는 `restock`/`재입고` 참조 **0건**.
2. **지역이벤트**(`featured/eventList`·`eventDetail`·`detail`) — 살아있는 푸터에서 진입하는 사용자 기능인데, MSA `featured`는 **운영관리 API(`/api/admin/featured`)만** 있고 공개 화면이 없다.

**의도적축소 / 이미 결정된 것**
- 쿠폰(`/api/item/coupons`, `download-all-coupons`) — [[coupon-feature-unused-hide-ui]]
- 판매자 포털 — [[provider-portal-split-deferred]], **무인증 문제는 [[gift-seller-portal-unauthenticated]] (최우선 보안 과제)**

## 5. 조치 후보

**A. 보안 — 최우선**
- `gift-seller-portal-unauthenticated`: `sellerId` 쿼리파라미터를 신뢰해 임의 판매자로 등록·수정이 가능하다(쓰기 경로). 이번 인벤토리에서도 `GiftController.register/edit/discontinue`가 `@RequestParam Long sellerId`를 그대로 받는 것을 재확인했다.

**B. 재현누락**
1. 재입고 알림
2. 지역이벤트 사용자 화면

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `catalog/` 3화면 + `header_news.vue` + `/api/catalog/*` 6 + `catalog-mapper` (§1-3 확인 후 전량)
- `/api/categories/searchResult`
- `item-mapper` 22건(옵션 13 포함), `item-addition-mapper` 5/6, `wishlist-mapper` 4건
- `goods/index.html`·`searchGoods.html`·`items/details.html`은 래퍼라 사장이 아니다 — **실화면과 짝으로 봐야 한다**

**D. 확인 필요**
- `POST /api/item/review` 호출처
- `/api/display/*` 7종의 개별 호출처(메인화면 — common 시트에서)
- MSA 운영관리 API 대량분의 admin 도메인 배정

---

## 6. 조치 결과 (2026-09-10)

### 6-1. **판매자 포털 무인증 해소** (보안 최우선)

`GiftController`의 제공자 화면 전체가 `sellerId`를 **쿼리파라미터·hidden input·화면 입력칸**으로 받아 그대로 신뢰했다. 값만 바꾸면 남의 답례품을 **등록·수정·판매중지·폐지·재고조정**하고 **남의 문의에 답변**할 수 있었다.

**서버가 신원을 결정하도록 전환**
- `GiftController.currentSellerId(request)` — GH_AUTH JWT의 userId → `OP_SELLER.MEMBER_USER_ID` → sellerId. `SellerPortalController`가 이미 쓰던 방식을 그대로 가져왔다.
- 적용 엔드포인트 9개: `GET/POST /register`, `GET/POST /gifts/{id}/edit`, `POST /gifts/{id}/discontinue`, `POST /gifts/{id}/stop`, `POST /gifts/{id}/stock`, `GET /my`, `GET /my/inquiries`, `POST /inquiries/{id}/answer`
- `sellerGate()` — 로그인 없으면 로그인으로, 로그인은 했으나 연결된 판매자가 없으면 안내 화면(어느 쪽인지 구분하지 않으면 로그인 무한루프가 된다).

**서비스 계층에도 소유권 검사** (컨트롤러를 우회해도 막히도록)
- `GiftService.stop(itemId, sellerId)` — **이전에는 소유권 검사가 아예 없었다**(`stop(itemId)`).
- `GiftService.adjustStock(itemId, sellerId, qty)` — 동상.
- `InquiryService.answer(inquiryId, sellerId, answer)` — 문의가 달린 답례품의 소유자만 답변 가능. 운영자 대리답변은 `answerAsOperator()`로 분리해 소유권 검사를 타지 않게 했다(운영관리 경로는 내부 시크릿으로 이미 보호됨).

**화면에서 신원 주장 제거**
`register.html`의 "제공자 ID" 입력칸, `my.html`·`inquiries.html`·`edit.html`의 `sellerId` hidden/입력/링크 파라미터를 모두 제거.

#### 검증
| 시나리오 | 결과 |
|---|---|
| 비로그인 `/my` | 302 → member 로그인 (`target=.../my`) |
| 로그인했으나 판매자 아님(userId 1002) | "연결된 판매자 정보가 없습니다" 안내 |
| 판매자 계정(userId 1014 ↔ seller 9001) | 답례품 관리 화면 정상 |
| **판매자 아닌 회원이 `sellerId=9001`을 실어 재고를 9999로 조작** | 차단, 재고 **46 유지** |
| **판매자 9001이 판매자 9002의 답례품(1001) 재고 조작** | `본인이 등록한 답례품만 재고를 조정할 수 있습니다.` |
| 동, 판매중지 | `본인이 등록한 답례품만 판매중지할 수 있습니다.` |
| 동, 폐지 | `본인이 등록한 답례품만 폐지할 수 있습니다.` |
| 대상 답례품 상태 | 재고 **30 / APPROVED 유지** |
| 정상 경로(본인 답례품 46→50) | 성공, 이후 46으로 원복 |

### 6-2. 재입고 알림 (재현누락 → 구현)

AS-IS는 상세화면 탭에서 `GET /api/item/restock`으로 신청 여부를 확인하고 `POST`로 신청한다(`ItemController:850~896`). MSA는 `restock` 참조가 **0건**이었다 — 다만 **`OP_RESTOCK_NOTICE` 테이블은 이미 이관돼 있었다**(코드만 없던 상태).

- `RestockNotice` 엔티티 + `RestockNoticeRepository` + `RestockNoticeService`
- `POST /gifts/{itemId}/restock-notice`, 상세화면은 `restockRequested`로 버튼/완료문구를 가른다
- **품절(`SOLD_OUT='1'`)일 때만** 신청 가능, **중복 신청 차단** — AS-IS는 중복 검사가 없어 같은 신청이 계속 쌓였다. 여기서는 쌓지 않는다.
- 스키마: PK에 시퀀스 DEFAULT가 없어 INSERT가 실패했다. 다른 gift 테이블과 같은 관례로 `op_restock_notice_id_seq`를 만들고 `database/ddl/service-gift.sql`에도 반영했다.

#### 검증
품절 답례품에 버튼 노출 → 신청(302, DB 1행) → 재진입 시 "재입고 알림을 신청하셨습니다" → 중복 신청 차단 → **재고 있는 답례품에 신청 시 거절** → 비로그인 시 로그인 리다이렉트. 검증 데이터 삭제 확인.

### 6-3. 미처리
- **지역이벤트 사용자 화면**(`featured/eventList`·`eventDetail`·`detail`) — 살아있는 푸터에서 진입하는 기능인데 MSA는 운영관리 API만 있다. 화면 3개 신규 작성이라 규모가 있어 별도 진행.
- 리뷰 좋아요(`POST /api/item/review/add-like/{id}`)
- `POST /api/item/review` 호출처, `/api/category/filter`·`price-areas` 대응, `/api/display/*` (common 시트에서)

### 6-4. 지역이벤트 공개 화면 (재현누락 → 구현)

운영관리 등록 API(`/api/admin/featured`)는 이미 있었고 **공개 화면만 없었다**. 진입은 살아있는 푸터 `footer_ali.vue:225,229`의 `?ing=Y`(진행중)·`?ing=N`(종료)다.

- `FeaturedService` — AS-IS `EventController.list()`(`:88~97`)의 목록 조건을 옮겼다: `featuredType='1'` + `featuredFlag='Y'` + `displayListFlag='Y'`. AS-IS가 `conditionType='FRONT'`(진행중) / `progression='3'`(종료)로 갈랐던 것을 **시작·종료일 비교**로 표현했다(MSA `OP_FEATURED`에 `progression` 컬럼이 없다).
- `FeaturedController` — `GET /events?ing=Y|N`, `GET /events/{featuredUrl}`. 상세 진입 키는 AS-IS와 동일하게 `featuredUrl`.
- `events.html` / `event-detail.html` — 기존 `list.html`의 실제 카드 마크업(`goods-list-group` / `goods-list-items` / `img_frame` / `card_title`)을 그대로 따랐다.
- 이벤트 답례품 편성은 `OP_FEATURED_ITEM`의 `DISPLAY_ORDER` 순서를 유지한다(`findAllById`는 순서를 보장하지 않아 다시 정렬).
- **범위 제외**: AS-IS의 이벤트 댓글(`getEventReply`/`createEventReply`)은 게시판 성격이라 common 도메인(게시판류)에서 함께 다루는 것이 맞다.

### 6-5. 상품평 좋아요 (재현누락 → 구현)

`OP_ITEM_REVIEW_LIKE` 테이블과 `OP_ITEM_REVIEW.LIKE_COUNT` 컬럼은 **이미 이관돼 있었고 코드만 없었다**(재입고 알림과 같은 상황). `Review` 엔티티에 `likeCount` 매핑조차 없었다.

- `ReviewLike` 엔티티 + 저장소 + `ReviewService.like(itemReviewId, userId, ip)`
- `POST /reviews/{itemReviewId}/like` (JSON), 상세화면 "도움돼요" 버튼
- **AS-IS 동작을 그대로**: 취소가 없는 **1회성**이다(`ItemServiceImpl:5523~5553`). 토글이 아니라서 이미 누른 상태면 `liked:false`를 돌려주고 아무 일도 하지 않는다 — 관심답례품(wishlist)의 토글과 다르다.
- 중복 판정은 **로그인이면 `USER_ID`, 비로그인이면 `IP`** (AS-IS와 동일). 비로그인도 누를 수 있다.

#### 검증 (6-4, 6-5)
| 확인 | 결과 |
|---|---|
| 이벤트 없음 | "진행중인 지역이벤트가 없습니다." |
| 진행중 목록 | 가을 지역이벤트만 |
| 종료 목록 | 봄 지역이벤트만 |
| 상세(`/events/autumn-fair`) | 이벤트명 + "진행중" 배지 |
| 답례품 편성 후 상세 | "이벤트 답례품" + 편성한 답례품명 |
| 리뷰 좋아요 (로그인) | `{"liked":true}` |
| 동, 중복 | `{"liked":false}` |
| 리뷰 좋아요 (비로그인, IP) | `{"liked":true}` |
| 동, 중복 | `{"liked":false}` |
| `LIKE_COUNT` 집계 | 2 (로그인 1 + 비로그인 1) |

작업 중 발견: `OP_ITEM_REVIEW_LIKE.ID`에도 시퀀스 DEFAULT가 없어(재입고 알림과 같은 문제) `op_item_review_like_id_seq`를 만들고 `database/ddl/service-gift.sql`에 반영했다.

검증 데이터(이벤트 2, 편성 2, 좋아요 2, LIKE_COUNT)는 전부 삭제·원복 확인.
