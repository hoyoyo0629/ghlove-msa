# AS-IS 전체 커버리지 맵 — 어느 화면이 어느 인벤토리에 들어가는가

> 6개 MSA 서비스(member/donation/point/gift/order/admin)에 걸리지 않는 영역이 상당하다.
> 누락 없이 다루기 위해 AS-IS 프론트 **최상위 디렉터리 전부**를 도메인에 배정하고,
> 어디에도 안 걸리는 것은 **7번째 시트 `common`** 으로 모은다.
> 작성 2026-09-10. 생사 판정 기준은 [[as-is-inventory-procedure]].

`live` = 백업/날짜본/`old/` 제외 후 남는 .html 수 (도달 가능 여부는 도메인별 인벤토리에서 개별 판정)

## 배정표

| AS-IS 디렉터리 | live/total | 배정 | 비고 |
|---|---|---|---|
| `users/` | 13/27 | **member** | 완료 |
| `mypage/` (회원분) | 21/36 | **member** | 배송지·회원정보 |
| `mypage/` (cntrList, receipt*, honorList*, intrstLocGov) | ↑ 포함 | **donation** | 기부내역·기부확인증·명예의전당·관심지자체 |
| `mypage/` (cntrPoint, cntrPointDetail) | ↑ 포함 | **point** | |
| `mypage/` (orderList, orderDetail, orderCancel, deliveryInfo) | ↑ 포함 | **order** | |
| `mypage/` (review, writeReview, favorItem, inquiryItem) | ↑ 포함 | **gift** | |
| `donation/` | 30/44 | **donation** | guide1~6 안내화면 포함 |
| `designated-donation/` | 3/3 | **donation** | 지정기부 |
| `featured/` | 5/5 | **donation** | 지역이벤트(기부하기 하위) |
| `goods/` | 4/4 | **gift** | 답례품 |
| `items/` | 2/3 | **gift** | 답례품 상세 |
| `catalog/` | 3/3 | **gift** | |
| `category/` | 1/1 | **gift** | |
| `community-business/` | 1/1 | **gift** | 답례품 > 마을기업 |
| `cart/` | 1/2 | **order** | |
| `order/` | 2/5 | **order** | |
| `admin/` | 7/7 | **admin** | 주문대행 등 |
| **`notice/`** | 2/3 | **common** | 공지사항 |
| **`faq/`** | 1/2 | **common** | FAQ |
| **`qna/`** | 4/4 | **common** | 1:1 문의 |
| **`data-board/`** | 2/2 | **common** | 고객센터 > 자료실 |
| **`qustnr/`** | 2/2 | **common** | 온라인 설문 |
| **`event/`** | 8/8 | **common** | 이벤트 |
| **`totalsearch/`** | 2/2 | **common** | 통합검색 (RFP 신규 대상 확인 필요) |
| **`policy/`** | 21/21 | **common** | 약관·개인정보처리방침 원문 ([[policy-content-db-migration-deferred]]) |
| **`guide/`** | 1/2 | **common** | 사이트맵 |
| **`error/`** | 1/1 | **common** | 오류 화면 |
| **`popup/`** | 1/1 | **common** | 팝업 |
| **`newsletter/`** | 77/77 | **common** | 소식지 — 발행분 정적 아카이브. 애플리케이션 기능인지 콘텐츠인지 판정 필요 |
| **`components/layouts/`** | (vue) | **common** | **GNB/LNB/헤더/푸터/플로팅** — 전 화면 공통 |
| **루트 `*.html`** | 11개 | **common** | `main.html`(메인), `index.html`, `intro.html`, `check.html`, `healthcheck.html` + 백업 6 |
| `pkiContent/` | 13/13 | 제외 | 공동인증서 SDK 정적자산(외부 제공) |
| `webDrm/` | 31/31 | 제외 | 문서보안 SDK 정적자산(외부 제공) |
| `modules/`, `static/`, `common/`, `public/`, `components/`(레이아웃 외) | - | 제외 | 스크립트·이미지 등 자산 |

## `common` 시트가 다룰 항목

1. **레이아웃/내비게이션** — GNB(대메뉴 구조·드롭다운), LNB(도메인별 좌측메뉴), 헤더(로그인상태·검색·장바구니), 푸터, 플로팅 버튼, 브레드크럼
   - 살아있는 레이아웃 컴포넌트 판별이 선행돼야 한다(`header_ali.vue` 계열이 live, `header.vue`/`header_ali_as-is.vue`/`header_ali_20240529.vue` 등은 사장 후보 — member 작업에서 `donation-lnb_ali.vue`가 **활성 등록 0**으로 확인된 전례가 있다).
2. **게시판류** — 공지사항, FAQ, 1:1문의(QnA), 자료실, 설문, 이벤트. 각각 목록·상세·검색·페이징·첨부·권한.
3. **안내/정책 화면** — 약관 21종, 사이트맵, 오류화면, 팝업.
4. **통합검색** — 전 도메인 교차검색. RFP 신규 요건 여부 확인 필요.
5. **메인화면** — 배너 캐러셀, 추천 답례품, 기부현황 위젯 등 여러 도메인을 끌어 쓰는 화면.
6. **소식지(newsletter)** 77건의 성격 판정.

## 서버측 미배정 영역 (별도 확인 대상)

프론트에 대응 화면이 없는 백엔드 기능도 `common` 또는 admin으로 배정해야 한다:
- `ghlove-batch` (배치 1개 컨트롤러 + 배치 잡)
- `ghlove-common`의 공통 인프라 — 코드관리, 파일업로드, 메시지, 시퀀스, 토큰, 캐시, 이력(change-log), 엑셀다운로드 로그, 금칙어(banword), 접속통계(access)
- 외부연계 모듈 `onepass/`, `payment/`, `simpleauth/`, `magicline/`

---

## 판정 결과 (2026-09-10, [[as-is-inventory-common]] 작성 시점)

작성 당시 열어뒀던 질문들의 답:

| 미결 항목 | 판정 |
|---|---|
| 살아있는 레이아웃 컴포넌트 판별 | 41개 중 **live 14 / 사장 27**. `header_ali`·`footer_ali`가 주 레이아웃. `customer-lnb_ali`(12곳 전부 주석)·`donation-lnb_ali`(6곳 전부 주석)·`totalSearch`(등록 주석)는 사장. `subsearch_ali.vue`는 6화면이 등록하나 **파일 자체가 없음** |
| 통합검색이 RFP 신규 대상인가 | **아니다 — AS-IS 기존 기능**이다. 다만 헤더 진입은 주석 처리돼 사장이고, 살아있는 진입점은 **메인화면 검색창 하나**. 자동완성·최근검색어·인기검색어는 재현 대상 아님 |
| 소식지 77건의 성격 | **콘텐츠(정적 아카이브)**. 컨트롤러·매퍼 없음. 진입점은 플로팅 버튼 1곳. 코드 이관 대상 아님 |
| 외부연계 모듈 `onepass/`·`payment/`·`simpleauth/`·`magicline/` | **자바 코드가 0개** — 전부 인증서·키·설정 자산 디렉터리다. 실제 코드는 `saleson/api/{payment,magicline}`, `saleson/common/sns` 에 있고 각각 donation(PG)·member(전자서명·SNS) 도메인 소관 |
| `ghlove-common` 공통 인프라 | common 시트 §8에 전수 배정 |
| `ghlove-batch` | common 시트 §10 — 잡 78개 분류 완료 |

추가로 확인된 것:

- `com.onlinepowers.board` **범용 게시판 프레임워크는 통째로 미사용** (24 엔드포인트/47 쿼리).
  live 프론트에서 `/board/` 링크 0건 → 이관 대상에서 제외. `DemoController`(샘플) 5종도 동일.
- `com.onlinepowers.framework.*` 매퍼 52쿼리는 인터페이스가 `libs/opframework-3.15.0.jar`
  내부라 **소스 grep으로 사장 판정이 불가능** → 판정보류로 분리.
- AS-IS **메인화면의 섹션 6종(MD추천·신규·인기 답례품, 스타일북, 이벤트, 프로모션)은
  메서드만 있고 호출·바인딩이 없다** → 재현 불필요.
