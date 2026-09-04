# AS-IS 기능감사 — gift(답례품) 서비스

조사범위: `ghlove-web/src/main/java/saleson/shop/*`(opmanager 백엔드, saleson 커스터마이징), `ghlove-api/src/main/java/saleson/api/*`(공개 API 계층), `ghlove-frontend/*`(고객 화면), 대비 `ghlove-msa/gift`, `admin`, `order`, `storefront`.

saleson은 상용 쇼핑몰 엔진(`libs/saleson-license-*.jar`)이고 실제 커스터마이징 소스는 `ghlove-web`(opmanager 관리자 콘솔 + 판매자 콘솔)과 `ghlove-api`(공개 REST API)에 있다. 도메인 모델 상당수는 `ghlove-common`에 공유되어 있다.

## AS-IS 기능 전수 목록

| 기능 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 답례품(Item) 등록/승인/반려/노출/재고 | `ghlove-web/.../shop/item/ItemManagerController.java`, `ghlove-api/.../item/ItemController.java` | **완료** | `gift/GiftController`, `GiftService` |
| 답례품 상세/목록/카탈로그 조회 API | `ghlove-api/.../item/ItemController.java`, `ItemDataSupport.java` | **완료** | `GiftApiController`, `GiftPublicApiController` |
| 답례품 재고 0 자동품절/판매중지/재고조정 | `ItemManagerController`, `SellerItemController` | **완료** | `GiftService`(stop/stock), `GiftOrderStock` SAGA 참여 |
| 리뷰(작성/이미지/평점) | `shop/review/ReviewController.java` | **완료** | `Review`, `ReviewImage`, `ReviewService` |
| 문의(1:1, 상품별) | `shop/qna/QnaController.java`(`/qna`,`/inquiry`), `QnaItemManagerController` | **완료** | `Inquiry`, `InquiryService` |
| 찜(위시리스트) | (saleson 표준기능, mypage 연계) | **완료** | `Wishlist`, `WishlistService` |
| 썸네일 자동생성(소/중/대) | `saleson.common.thumbnail` | **완료** | `ThumbnailService` |
| 대표상품관리(`REPRESENTATIVE_ITEM_YN`) | (view_reimplementation_round에서 확인) | **완료** | `GiftController` `/admin/representative-items` |
| 브랜드 관리(지자체별 답례품 브랜드) | `shop/brand/BrandManagerController.java` | **부분구현** | 관리자 CRUD(`BrandApiController`)만 존재, **고객용 공개 브랜드 페이지 없음** |
| 제철식품관(공개 노출) | `shop/seasonalfood/SeasonalFoodManagerController.java` | **부분구현** | `SeasonFoodItem`으로 공개 목록(`/seasonal`)은 됨. **관리자가 월별/키워드로 직접 지정하는 CRUD 없음**(TO-BE는 배치가 채운다고 가정, AS-IS는 수동 큐레이션 화면) |
| 특산물관(`G_SPCL_ITEM*`) | `shop/specialityitem/SpecialityItemManagerController.java` | **의도적 제외 확인됨** | Controller는 있으나 대응 Service 클래스 자체가 소스에 없음(껍데기). 기존 메모(`dormant-saleson-boilerplate-tables`)에서 saleson 미사용 보일러플레이트로 이미 확인됨. 재확인 완료 |
| 사은품(GiftItem)/사은품그룹(GiftGroup) — 주문시 증정품 | `shop/giftitem/*`, `shop/giftgroup/*` | **의도적 제외 확인됨** | `GiftItemManagerController`/`GiftItemController`/`GiftItemMobileController` 전체 메서드가 주석처리(죽은 코드). 유일하게 살아있던 `GiftGroup` CRUD도 참조하는 쪽(`OrderManagerController` 등)에서 전부 주석처리되어 실사용 0건. AS-IS 자체 죽은 코드로 재확인 |
| **상품 옵션(단일/2단/3단 선택형, 텍스트옵션(각인), 옵션별 가격/재고, 추가구성상품)** | `ghlove-frontend/items/details-main.html`(itemOptionType S/S2/S3, itemTextOptionFlag, addOptionList) | **미구현** | Gift 도메인에 옵션 개념 자체가 없음. `storefront/.../GiftDetailView.vue` 주석에 "스코프 밖"이라 적혀있으나 AS-IS에서 활발히 쓰이는 살아있는 기능이라 스킵 근거가 되지 않음(표준규칙 위반) |
| **배송비 정책(출고지/기본배송비/무료배송기준/도서산간 추가배송비)** | `shop/shipment/Shipment*Controller.java`, `saleson.shop.shipment.domain.Shipment`(ghlove-common) | **미구현** | `order` 서비스 Java 소스 전체에 shipping 관련 코드 0건. cart/checkout 템플릿엔 "무료배송" 정적 텍스트만 있고 실제 계산로직 없음 |
| **도서산간 우편번호 마스터(배송비 자동판정)** | `shop/island/Island*Controller.java` | **미구현** | 배송비 정책과 연동되는 우편번호→도서산간 판정 테이블/화면 전체 없음 |
| 반품/교환 배송비 정책 | `shop/shipmentreturn/ShipmentReturn*Controller.java` | **미구현** | 위 배송비 정책과 동일 계열, 없음 |
| **판매자(제공자) 자체 로그인/셀프서비스 콘솔** | `shop/seller/SellerController.java`(자체 로그인/세션/비번변경/PKI인증서), `shop/seller/mall/MallController.java`(`/mall/{sellerLoginId}` 미니몰+qna+review 탭) | **미구현** | TO-BE는 provider가 member 서비스 JWT(PROVIDER 롤)로 로그인 — 이 자체는 합리적 단순화. 그러나 미니몰 공개페이지, 판매자 공지사항(`SellerISysNoticeController`), PKI 인증서 로그인은 대응 기능 없음 |
| 판매자 하위 직원 계정관리 | `shop/seller/user/SellerUserController.java` | **미구현** | 제공사 조직 내 담당자 여러 명을 등록/관리하는 기능. member 서비스에 대응 개념 없음(계정=1인 1롤) |
| **상품 수정신청 승인 워크플로우** | `shop/seller/item/SellerItemController.java`(`sale-edit` — 수정요청 큐, 지자체/본사 승인 후 반영) | **미구현** | `GiftController.edit()`는 즉시 반영(승인절차 없음). AS-IS는 최초 등록만 승인필요, 이후 가격/내용 변경은 별도 재승인 큐를 거침 |
| 판매자→지자체 담당자 문의 | `shop/qnaadmin/QnaLocgovManagerController.java`(`/seller/qna-locgov`), `QnaAdminManagerController.java`(`/opmanager/qna-admin`) | **미구현** | `admin`의 `QnaAdminController`(`/qna-admin`)는 이름이 같지만 실은 별개 기능(일반 회원 1:1문의 관리자 답변 화면, `OP_QNA` 기반). AS-IS `QNA_ADMIN` 테이블 기반의 "제공자↔지자체 문의"는 대응 없음 |
| 재입고 알림 신청 | `shop/restocknotice/RestockNoticeController.java` | **미구현** | 품절 상품에 대해 회원이 SMS 재입고알림을 신청하는 기능, 대응 없음 |
| **입점문의(신규 판매처 지원서)** | `shop/storeinquiry/StoreInquiryController.java`(`/store-inquiry/inquiry`, 파일첨부) | **미구현** | 신규 제공자가 되고 싶은 업체가 공개 폼으로 지원하는 페이지, 대응 없음 |
| 판매처(오프라인 매장) 정보 관리 | `shop/store/StoreManagerController.java` | **미구현(저우선)** | `Store`(이름/주소/영업시간/storeType) 마스터 관리, 실사용 범위 불명확 — 후속 확인 필요 |
| 랭킹관리(관리자 큐레이션 TOP상품) | `shop/ranking/Ranking*Controller.java`(공개 `/ranking`, 관리 `/opmanager/ranking`) | **미구현** | 판매량 기반이 아닌 관리자가 직접 지정하는 카테고리별 랭킹 페이지 |
| 카테고리 관리(3단 트리)/카테고리 필터(속성 검색)/모바일 카테고리 편집/카테고리 팀그룹 | `shop/categories/CategoriesManagerController.java`, `categoriesfilter/*`, `mobilecategoriesedit/*`, `categoriesteamgroup/*` | **미구현** | `gift`는 `GIFT_CATEGORY` 공통코드 + `GiftSubcategory`(GNB 메뉴용, 필터링 미사용)로 단순화되어 있음. AS-IS의 계층형 카테고리 트리 관리, 카테고리별 속성 필터 관리, 모바일 전용 카테고리 편집 화면은 전부 없음 |
| 검색어 관리(자동완성/인기검색어/금칙어) | `shop/keyword/KeywordController.java`, `shop/search/SearchManagerController.java` | **미구현** | 답례품 카탈로그 자체 검색의 자동완성/인기검색어/금칙어 관리. (참고: round11에서 스킵한 "통합검색"은 이것과 다른 사이트 전체검색 기능이며 AS-IS에서도 죽은코드였음 — 이 항목은 그것과 무관한 별개 기능이며 살아있음) |
| 상품 복제등록 | `shop/item/ItemManagerController.java`(`copy/{itemId}`) | **미구현(저우선)** | 기존 상품을 복사해서 새 상품 초안을 만드는 편의기능 |
| 다중 이미지 순서변경(드래그) | `ItemImage`에 `ordering` 컬럼은 있음 | **미구현(저우선)** | 값은 있으나 등록/수정 화면에 순서 재배치 UI 없음(업로드 순서 고정) |
| 리뷰/QnA 신고·숨김(관리자 모더레이션) | (AS-IS에 opmanager 화면 있음) | **의도적 제외 확인됨** | 기존 메모에 명시된 스킵결정, 재확인만(이번 라운드에서 별도 조사 안함) |
| 쿠폰 | `shop/coupon/*` | **완료(order 서비스 소관)** | `coupon_subsystem_round` 메모 |
| 이벤트/기획전(공지성 GNB)/팝업 배너 | `shop/event/*`, `shop/popup/*` | **완료(admin 콘솔 소관)** | `gnb_expansion_round`, `admin_console_real_asis_markup_round`에서 처리 확인(`admin/templates/content/popup-*.html` 등) |

## Gap 상세

### 높음(HIGH) — 핵심 커머스 로직, 다수 화면/데이터에 영향

1. **상품 옵션 시스템 부재**: 단일/2단/3단 선택형 옵션(옵션별 가격·재고), 텍스트옵션(각인문구 등 최대 3개), 추가구성상품(add-on) 전체가 Gift 도메인에 없다. `Gift` 엔티티가 단일 SKU/단일 가격/단일 재고만 가정하고 있어, 옵션을 추가하려면 `Gift`, 장바구니(`order`), 주문라인, 재고관리(`GiftOrderStock`), 등록/수정 화면(`register.html`/`edit.html`), 상세화면(`GiftDetailView.vue`)까지 전부 손대야 한다. `storefront/src/views/gift/GiftDetailView.vue` 상단 주석에 "스코프 밖"이라 명시돼 있지만 이는 AS-IS 죽은코드가 아니라 실사용 기능이므로 표준 스킵기준(AS-IS 자체 죽은코드 확인)을 충족하지 못한다.
2. **배송비 계산 로직 전체 부재**: `order` 서비스 Java 소스에 shipping 관련 코드가 0건이다. AS-IS는 판매자별 출고지/기본배송비/무료배송기준(`Shipment`), 반품배송비 정책(`ShipmentReturn`), 도서산간 우편번호 판정(`Island`)을 조합해 배송비를 계산한다. TO-BE는 cart/checkout 템플릿에 "무료배송" 정적 텍스트만 있다. 관리자가 배송비 정책을 설정할 화면도, 체크아웃 시 실제 배송비를 더하는 로직도 없다.
3. **판매자 자체 셀프서비스 콘솔 부재**: AS-IS는 판매자 전용 로그인/세션(`SellerController`), 미니몰 공개페이지(`/mall/{sellerLoginId}` — 자체 QnA/리뷰 탭 포함), 하위직원 계정관리(`SellerUserController`), 판매자 공지사항(`SellerISysNoticeController`), 상품 수정신청 승인워크플로우(`SellerItemController` sale-edit)를 갖춘 별도 콘솔이다. TO-BE는 member 서비스의 PROVIDER 롤 하나로 이를 대체했는데(합리적 단순화), 그 결과 미니몰 공개페이지·하위직원 관리·수정재승인 워크플로우가 통째로 빠졌다. 특히 수정재승인 워크플로우 부재는 실제 운영상 리스크(제공자가 승인 없이 가격을 즉시 바꿀 수 있음)로 이어질 수 있다.

### 중간(MEDIUM)

4. **입점문의(신규 판매처 지원서) 부재**: `/store-inquiry/inquiry` 공개 폼(파일첨부 포함)이 대응 없이 완전히 빠져 있다.
5. **판매자→지자체 담당자 문의 부재**: `QNA_ADMIN` 기반의 제공자-지자체 소통채널(`/seller/qna-locgov`, `/opmanager/qna-admin`)이 없다. admin의 `/qna-admin`은 이름만 같은 별개 기능(일반회원 1:1문의)이라 혼동 주의.
6. **제철식품관 관리자 수동 큐레이션 부재**: 공개 노출(`SeasonFoodItem`)은 되지만, AS-IS처럼 관리자가 월별로 상품을 검색해 지정하는 CRUD 화면이 없다(TO-BE는 배치가 채운다고 가정하나 실제 AS-IS는 수동 큐레이션).
7. **랭킹관리(관리자 큐레이션 TOP상품) 부재**: 카테고리별 "랭킹" 공개 페이지 및 관리자 CRUD가 없다.
8. **고급 카테고리 관리 부재**: 계층형 카테고리 트리 관리, 카테고리별 속성 필터 관리, 모바일 전용 카테고리 편집이 없다(TO-BE는 공통코드 기반 단순 대분류만).
9. **검색어 관리(자동완성/인기검색어/금칙어) 부재**: 답례품 카탈로그 자체 검색용 자동완성 API, 인기검색어, 금칙어 관리가 없다.
10. **재입고 알림 신청 부재**: 품절상품 SMS 알림 신청 기능이 없다.
11. **브랜드 공개페이지 부재**: `Brand`는 관리자 CRUD만 있고 고객이 브랜드별로 상품을 모아볼 수 있는 공개 페이지가 없다.

### 낮음(LOW)

12. 상품 복제등록(기존 상품 복사해서 신규 초안 생성) 편의기능 없음.
13. 다중 이미지 순서변경 UI 없음(`ordering` 컬럼은 존재).
14. 판매처(오프라인 매장) 정보 관리(`Store`) 대응 없음 — 실사용 범위 불명확, 후속 확인 필요.

## 완전히 구현 확인됨

답례품 등록·승인·반려·노출·재고관리, 재고 0 자동품절, 리뷰(이미지 포함), 상품별 1:1문의, 찜(위시리스트), 썸네일 자동생성, 대표상품관리, 브랜드 관리자 CRUD, 판매자 관리자 CRUD(기본), 쿠폰(order), 이벤트/기획전/팝업배너(admin), 제철식품관 공개노출, order SAGA 참여(재고예약/복원), 답례품몰 Vue3 프론트(목록/상세/리뷰/QnA/찜/마이페이지). 사은품(GiftItem/GiftGroup), 특산물관(G_SPCL_ITEM)은 AS-IS 자체 죽은 코드로 재확인되어 스킵이 타당함.
