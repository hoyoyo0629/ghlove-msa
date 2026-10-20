---
name: domain-code-facts
description: 도메인 코드·데이터 사실 - 가입구분 100/200·수신동의 0=수신·판매자 2/3/5·주문번호 K+10·op_qna QNA_TYPE·FAQ 정본 op_faq·S옵션 name1/name2·수납완료면 수납일 있음·가입축하포인트 0·명예등급 0·쿠폰 미사용·G_QNA_ADMIN=판매자→지자체 문의·주문 AS-IS 상태코드 도출/point_outcome DEDUCTED·FAILED/클레임 처리코드·TO-BE op_item.item_type 전부 NULL(=1로 읽기)
metadata:
  type: project
---

코드값·데이터 규칙처럼 코드만 봐서는 헷갈리는 사실들.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## member-sbscrb-se-code-general-mismatch

> [해결 2026-10-14] OP_USER 가입구분이 발명값 'GENERAL'이던 것 → AS-IS SBSCRB_SE(100 온라인/200 오프라인)로, OP_USER.LOCGOV_CODE 매핑·저장 신설(G_INSTT_CODE 313건 적재). 회원구분 표시는 코드 아닌 상수 '일반회원'


**AS-IS 규칙(소스 확인)**:
- 가입구분 SBSCRB_SE_CODE: 온라인·간편가입 = join-mapper `updateUserInfo`의 '100', 기탁서 즉석가입 = `insertOffgive`의 '200', 탈퇴 = NULL.
- 회원 지자체코드 LOCGOV_CODE: 가입·회원정보수정 = 주소검색 admCd 앞 5자리 → `G_INSTT_CODE.locgov_mapng_code`
  (`updateUserInfo`/`updateUserEtcInfo`, 수정화면은 재검색 안 하면 저장값을 다시 보내 재매핑, 못 찾으면 NULL),
  즉석가입 = 행정지 지자체(psitnLocgovCode, 매핑 없음), 탈퇴 = NULL. AS-IS에서도 **저장만 하고 읽는 화면/로직은 없다**.
- 회원가입·수정 화면 "회원구분"은 코드가 아니라 화면 상수 `디폴트등급: ["일반회원"]`.

**TO-BE 수정(2026-10-14)**: User.locgovCode 매핑, MemberService(signup/walk-in/updateProfile/withdraw)·ExternalLoginService,
member DonationClient.locgovMapngCode(donation `/api/offgive/locgov-mapng-code`, 조회 실패는 예외 - null로 삼키면 저장값을 지운다),
storefront Signup/Profile의 jusoCallBack 7번째 인자 admCd, 즉석가입은 donation→member로 psitnLocgovCode 전달.
`USER_TYPE` 코드 라벨 조회(AuthApi /auth/me, ProfileApi)는 상수 `MemberService.MEMBER_LEVEL_LABEL`로 교체.
데이터: `migration-donation-g-instt-code-asis-load.sql`(AS-IS export 313건, 표가 0건이었다),
`migration-member-sbscrb-se-code-asis.sql`(GENERAL→100 67건, walkin→200 7건, 즉석가입 지자체 4건 채움).

**How to apply:** 온라인 가입자 기존 67건의 LOCGOV_CODE는 원천값이 없어 NULL - 회원정보 수정 저장 시 채워진다.
member.op_common_code의 USER_TYPE(GENERAL 등)은 이제 쓰는 곳이 없는 TO-BE 발명 코드(지우지는 않았다).

## receive-sms-encoding-split

> 수신동의 컬럼 Y/N·0 혼용 문제(AS-IS 0=수신, 1=비수신) - 2026-10-15 member 저장·읽기·가입을 0/1로 통일하고 기존 행 변환 완료


**AS-IS 정본**: 수신여부 컬럼은 **`0`=수신, `1`=비수신**이다
(`saleson.shop.user.domain.GeneralCustomer` 주석 "이메일 수신여부 (0: 수신, 1:비수신)").

**TO-BE는 두 갈래로 쓰고 있다**(2026-10-05 발견, 미수정):
- 쓰기: `member/service/MemberService.java:652`는 **`Y`/`N`**, `member/service/ExternalLoginService.java:214`는 **`0`**
- 읽기: `member/web/ProfileApiController.java:62`는 `"Y".equals(...)`,
  admin `templates/member-admin/details.html`·`info-access.html`은 `== '0'`(AS-IS대로 이식한 쪽)

**지금 벌어지는 일**: 일반 가입으로 수신동의한 회원은 `Y`가 저장되어
① 운영자 회원상세에서 **"비동의"로 잘못 표시**되고 ② Q&A 답변 국민비서 문자 대상에서도 빠진다
([[ips-sms-sender-lives-in-admin]]은 AS-IS대로 `'0'`만 동의로 본다 - 그쪽을 고치는 게 아니다).

**고치려면**: member 쓰기·읽기를 `0`/`1`로 통일 + **기존 행 변환**(`Y`→`0`, `N`→`1`).
같은 의심이 `receive_email`·`receive_pbanc`·`receive_kakao`에도 있어 네 컬럼을 함께 봐야 한다.
데이터 변환은 사용자 판단 대상이라 보류 중이다([[commit-only-when-asked]]와 같은 선).

**★해소(2026-10-15, 사용자 승인 "데이터 변환까지 진행")**: member 회원정보수정 저장·읽기를 0/1로(`MemberService.RECEIVE_AGREE/DISAGREE`,
`ProfileApiController`), **가입도 수신동의를 저장하게 함** - 그전엔 가입 화면에 체크박스만 있고 서버로 안 보내 61명이 빈 값이었다.
AS-IS join.html처럼 기본 "1", 약관동의에서 체크한 항목만 "0"(국민비서=receiveSms, SMS=receivePbanc, Email, 알림톡=receiveKakao).
기존 Y/N 4명 변환(`database/ddl/migration-member-receive-flags-asis-codes.sql`, 백업은 스크래치패드). 기존 빈 값 회원은 그대로 둠.

## gift-seller-status-asis-codes

> 판매자(OP_SELLER) 상태코드를 AS-IS 2=사용/3=중지/5=승인대기로 전환(2026-10-15) - TO-BE 발명 0/1/2반려/3이 AS-IS '2'와 충돌했었다


AS-IS `OP_SELLER.STATUS_CODE`는 **2=사용 / 3=중지 / 5=승인대기** 셋뿐이다(seller/list.jsp 라벨, form.jsp 라디오 기본값 2).
AS-IS 카탈로그·발송 쿼리는 전부 `S.STATUS_CODE = '2'`로 살아있는 업체를 고른다.

TO-BE는 0대기/1승인/2반려/3중지를 새로 만들어 써서 **같은 '2'가 AS-IS 사용 ↔ TO-BE 반려**로 충돌했다
(gift `SellerRepository.findEmailSendTargets`가 AS-IS '2'를 그대로 써 반려 업체를 고르게 돼 있었다).
2026-10-15 관리자 메인 이식 중 발견 → 사용자 승인으로 전환:
- 코드: `Seller.STATUS_ACTIVE/STOPPED/WAITING`, 등록 기본값 5, admin `seller/list.html` 라벨·승인 버튼(→2), 메인 집계(gift AdminDashboardService) '2'.
- 데이터: 18곳 1→2(`database/ddl/migration-gift-seller-status-asis-codes.sql`, gift DB), seed `service-gift.sql`도 '2'로.
- 반려 상태는 AS-IS에 없어 없앴다(전환 시점 0건).

**남은 것**: TO-BE 판매자 관리 화면(목록·등록) 자체가 AS-IS seller/list.jsp·form.jsp와 다른 발명 화면이다(상태 라디오 없음,
등록 후 승인 버튼 흐름). AS-IS는 등록 폼에서 상태를 고르고 기본값이 '2'(사용) - 화면 재이식은 서비스 로직 전수조사 때.
관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-main-dashboard-asis-port), [[service-logic-full-audit-planned]].

## order-code-format-matches-asis

> 주문번호 생성 포맷을 TO-BE 발명값(O+시각+난수)에서 AS-IS와 동일한 K+10자리 시퀀스로 교체, 기존 19건도 일괄 변환(2026-10-08)


**발견 경위**: 시스템관리 > 포인트사용 정합성검증(7210) 화면을 보다가 AS-IS 주문번호가
"AB23060903668531" 형태인 걸 사용자가 지적 - 추적해보니 AB는 **관리자 수기/엑셀 주문등록**
기능(`OrderAdminServiceImpl.getNewOrderCode("A")` = "A" + `sequenceService.getId(...)`가
만드는 "B..." 값)에서만 나오는 별개 코드이고, **일반 고객 주문**은
`OrderServiceImpl.getNewOrderCode(OrderCodePrefix.FRONT)` = **"K" + 10자리 0-패딩 숫자**
(`orderMapper.getOrderCodeNum()`, 예: K0000006106)라는 걸 확인했다. 사용자가 이어서
"일반 주문번호도 K로 시작하게 바꾸고 기존 데이터도 같은 포맷으로 바꿔달라"고 요청.

**수정**: `OrderService.generateOrderId()`를 `"O" + yyyyMMddHHmmss + 난수4자리`(TO-BE가
지어낸 값, AS-IS와 무관)에서 `"K" + String.format("%010d", nextval)`로 교체.
`OrderRepository.nextOrderCodeSeq()`(네이티브 `nextval('ord.op_order_code_seq')`) 추가 -
AS-IS `getOrderCodeNum()`과 같은 원자적 채번. `CartService`(멀티아이템 체크아웃)도 같은
메서드를 쓰므로 한 곳만 고치면 됨.

**기존 데이터 일괄 변환** (`migration-order-code-asis-format.sql`): 기존 19건을
`created_date` 오름차순으로 K0000000001~19로 매핑해 **order_id를 참조하는 모든 활성
테이블을 한 트랜잭션에서 같이** 바꿨다 - 하나만 바꾸면 조인이 끊어진다:
`ord.od_order`(PK) · `od_order_item` · `od_shipment` · `od_claim` ·
`point.g_cntr_use_point`(멀티아이템 접미사 `#N` 보존 필요) ·
`admin.stat_order_ledger`/`stat_claim_ledger`(order 서비스 readmodel 사본).
`ord.op_order*`류(AS-IS DDL만 있고 TO-BE 코드가 안 쓰는 레거시 표)는 전수 확인 결과 전부
0행이라 건드릴 필요 없었다. 시퀀스는 `setval(..., 19, true)`로 맞춰 다음 채번이 20부터
이어지게 했다.

**검증**: `OrderCodeGenerationTest`(`@SpringBootTest`, 실제 DB로 `K\d{10}` 포맷 확인 +
연속 호출 시 증가 확인 - 레포지토리를 목으로 때우면 SQL 자체가 실행되는지 검증이 안 돼서
([[backend-db-pitfalls]] 교훈) 실제 컨텍스트로 돌렸다). 마이그레이션 적용 후
`ord.od_order`/`point.g_cntr_use_point` 직접 조회로 전부 K-코드로 바뀐 것 확인.

**후속**: AS-IS의 "관리자 수기/엑셀 주문등록" 기능(AB코드, OrderAdminServiceImpl)을
TO-BE에 포팅하고 7210(포인트사용 정합성검증)이 AS-IS처럼 "AB%" 주문만 보도록 고쳤다 -
[[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-order-manual-registration-port-progress) 참고(2026-10-08 완료, 핵심 단건등록만
우선구현·엑셀업로드는 미포팅).

**상태**: order compileJava+test+bootJar 통과, DB 마이그레이션 적용 완료. **재기동 필요: order.**

## op-qna-split-by-qna-type

> op_qna 한 표를 QNA_TYPE으로 세 화면이 나눠 쓴다 - 0=1:1문의(5102), 1=상품문의(중지), 2=공개Q&A(5112). 공개 쪽 필터 누락은 2026-10-05 수정


**AS-IS `Qna` 상수**: `QNA_GROUP_TYPE_INDIVIDUAL = "0"` · `QNA_GROUP_TYPE_ITEM = "1"` ·
`QNA_GROUP_TYPE_QNA = "2"`. `OP_QNA`/`OP_QNA_ANSWER`/`OP_QNA_FILE` 한 세트를 세 화면이 나눠 쓴다.

| QNA_TYPE | 화면 | 운영자 메뉴 | TO-BE URL |
|---|---|---|---|
| `0` | 1:1 문의(마이페이지 개인문의) | 5102 | `/admin/inquiries` |
| `1` | 상품문의 | 5103 **중지** | - |
| `2` | 고객센터 공개 Q&A 게시판 | 5112 | `/qna-admin` |

**새 글을 넣는 코드는 반드시 `qna_type`을 넣어야 한다.** 빠뜨리면 그 글은 어느 운영자 화면에도
보이지 않는다(두 화면 모두 타입으로 거른다). TO-BE `QnaService.ask()`가 그 상태였다.

**조회하는 코드도 타입으로 걸러야 한다.** 2026-10-05에 공개 쪽 3곳을 고쳤다:
`ask()`(→`'0'` 저장) · `myInquiries()`(→`'0'`) · `publicBoard()`·`publicBoardDetail()`(→`'2'`).
필터가 없던 동안 **마이페이지 1:1문의의 제목·작성자가 공개 Q&A 게시판에 노출**되고
비밀글이 아니면 id만 바꿔 본문까지 열람할 수 있었다.

**관련 주의**
- 관리자 문의(16651, 예전 TO-BE 전용 메뉴 5120 "내부문의"는 없어짐)는 이 표가 아니라 **`g_qna_admin`**이다(`/admin/internal-inquiry`, 아래 g-qna-admin-is-seller-to-locgov).
- 1:1문의 목록은 작성자명을 **마스킹하지 않고**, 공개 Q&A(5112) 목록은 **마스킹한다** - 화면마다 다르다.
- 답변 저장 시 **5112만** 국민비서 문자를 보낸다([[ips-sms-sender-lives-in-admin]]). 5102는 안 보낸다.
- 삭제·첨부 로직은 AS-IS가 두 화면에서 같은 서비스 메서드를 부르므로 TO-BE도
  `QnaOpenAdminService`의 것을 공유한다.
- **공개 Q&A 글쓰기 경로가 TO-BE에 없다**(AS-IS `api/qna`에는 있다) - `'2'` 글은 시드뿐이다.

## public-faq-canonical-table

> 공개 고객센터 FAQ의 정본은 op_faq + FaqType enum이다 - 초기 시드가 지자체FAQ 표에 자체코드로 넣어둔 것을 2026-10-05에 교정했다


**AS-IS 정본**: 고객센터 FAQ는 **`op_faq`** 한 표를 세 곳이 같이 쓴다 -
운영자 FAQ 관리(메뉴 **5104**, `/opmanager/faq`), 공개 페이지(`/faq/list.html`),
공개 API(`/api/faq`). 통합검색 뷰 `view_search_faq`도 `op_faq`를 보고 `/faq/list.html`로 링크한다.
질문유형은 공통코드가 아니라 **Java enum `FaqType`**(`F_LOGIN`·`F_CNTR_SYSTEM`…)이고
AS-IS 세 화면 모두 `enumMapper.get("FaqType")`로 가져온다.

**2026-10-05 교정 전 TO-BE**: 초기 시드가 FAQ 63건을 **`op_community_locgovfaq`**
(AS-IS에서 **중지된** 메뉴 11403 '지자체FAQ'의 표)에 넣고 질문유형 코드도 자체 코드
(`JOIN`·`DONATE`…)로 바꿔 두었으며 공개화면이 거기를 읽고 있었다. `op_faq`는 0행이었고
메뉴 5104는 엉뚱하게 `/community/faq-bbs`(커뮤니티 담당자FAQ 11405)를 가리켰다.

**지금 상태**
- TO-BE 관리화면: `/faq-admin`(`FaqAdminController`) → `op_faq`, 유형은 `admin/service/FaqType`
- TO-BE 공개: `/faqs`·`/api/faqs`(`FaqService`) → `op_faq`, 유형 라벨도 enum
- 63건은 코드 1:1 번역해 `op_faq`로 이관(`database/ddl/migration-admin-faq-5104.sql`).
  **원본 행은 지우지 않았다** - 중지된 11403 화면(`/community/locv-faq`,
  `LocgFaqAdminController`)이 계속 그 표를 보고, 그 행들은 여전히 `JOIN`… 코드다.
  그래서 `op_common_code`의 `FAQ_TYPE` 11건도 **지우지 말 것**(그 화면의 라벨이다).
- 표 정리(`op_community_locgovfaq` 정리 여부)는 사용자 판단 대상으로 남겨 두었다.

FAQ 본문은 **스마트에디터 HTML**이라 공개화면에서 그대로 렌더해야 한다
(AS-IS가 `v-html`을 쓴다 - `th:text`로 두면 태그가 글자로 보인다).

## gift-option-s-name-convention

> gift 선택형(S) 옵션은 AS-IS처럼 option_name1=옵션 제목·option_name2=옵션 내용(2026-10-13 전환+5행 이관). 스냅샷 라벨은 '제목 : 내용'. S2·S3 제목(item_option_title1~3)은 TO-BE 미매핑


AS-IS OP_ITEM_OPTION 선택형(S) 실데이터 31,583행 전부 name1=제목('토마토')·name2=내용('완숙 토마토 4kg')
(opmanager/item/form.jsp 옵션적용: optionName1=제목, optionName2=내용, itemOptionTitle1/2='옵션 제목'/'옵션 내용').
TO-BE는 값을 name1에 넣고 name2를 비워 AS-IS 조회(selectOffRprsOption 등 name2 기준)와 어긋났었다.

**2026-10-13 전환**: GiftOptionService(saveOptions/register/edit, 검증문구 "옵션명을/옵션값을 입력해 주세요."),
admin 단건 옵션 API·화면(optionTitle 추가), 판매자 edit.html(S도 2칸 '옵션 제목/옵션 내용'), storefront 상세
(드롭다운 제목=첫 옵션 name1, 항목=name2 - AS-IS details-main.html), 스냅샷 라벨 "제목 : 내용"(AS-IS viewOptionTextNoUl).
기존 S행 5건 이관(`migration-gift-option-s-name-asis.sql`: 1025 '맛', 1026 '색상' - TO-BE 시드라 제목을 붙임).

**남은 갭**: S2·S3의 단계별 제목(OP_ITEM.ITEM_OPTION_TITLE1~3)을 TO-BE Gift 엔티티가 매핑하지 않아 저장·표시가 안 된다
(AS-IS는 조합형 드롭다운에 제목을 띄우고 옵션명 텍스트에 "제목: 값 | ..."로 쓴다). 판매자 옵션 에디터도 AS-IS의
"입력 도우미 + 적용" 구조가 아니다.

관련: [[gift-option-multiform-redesign]], [[offgive-gift-order-asis-flow]]

## donation-sttemnt-pay-de-populated

> 2026-10-14부터 g_cntr.sttemnt_pay_de(수납일)를 채운다 - 수납완료=IFNULL(수납확인일,오늘)+delete_at N, 과오납승인=NULL+delete_at Y, 기존 41건 백필, 빼 뒀던 AS-IS "수납일 있음" 조건 복구. "수납완료면 수납일 있음"이 불변식


AS-IS는 수납완료로 바꾸는 모든 UPDATE(ngdonation sunapSuccess/sunapResultSuccess, externalapi sunapSuccess,
give-state sunapUpdateY)에서 STTEMNT_PAY_DE·DELETE_AT='N'을 같이 넣고, 과오납 승인(giveCancelProcess)은 NULL·'Y'로 바꾼다.
TO-BE는 수납일을 donation_levy에만 남기고 g_cntr엔 안 넣어(Donation 엔티티 주석 "항상 비어 있다") 집계마다 AS-IS 조건을
빼고 번역해 왔다 - 기부금 접수관리 목록 수납일 공란으로 사용자가 발견.

**Why:** 화면·엑셀·집계가 AS-IS와 같은 규칙으로 계산되게 하려고(사용자 결정 2026-10-14).

**How to apply:**
- 불변식: TO-BE에서 COMPLETED이면 sttemnt_pay_de가 있다. 그래서 표시 없이 COMPLETED만 거는 나머지 20여 쿼리는 결과가 같다 -
  새 쿼리 이식 때는 AS-IS대로 `sttemnt_pay_de is not null`까지 그대로 쓴다.
- 복구한 곳: UnpaidDonationBatchRepository(미납=sttemnt null, 상태 번역 제거), OffgiveAdminRepository.detail 누적액,
  MemberDonationAdminRepository 5쿼리, GiveStatRepository(연·월도 수납일 기준), DesignatedStatRepository 6곳,
  DonationRepository.findDesignatedByStatus. admin 회원 기부내역 수납일은 yyyy-MM-dd.
- 남은 차이(납부게이트웨이·배치 라운드): 수납일 출처(AS-IS 실제 수납이력 vs TO-BE 만든 /sunap·로컬 가짜), 수납 시점/방법
  (배치·민간개방API·변경신청 300 수납처리 없음), 포인트 칸(point 원장 설계). [[batch-job-port-decisions]],
  [[donation-payment-gateway-port-decision]]
- 데이터: `migration-donation-sttemnt-pay-de-backfill.sql`(levy 수납일 → 없으면 cntr_de).

## signup-point-built-but-off

> 회원가입 축하포인트 - AS-IS 동일하게 구현하되 설정값 0(비활성)으로 둠. point가 MEMBER_JOINED 구독해 적립, POINT_JOIN 양수로 바꾸면 켜짐


**2026-09-23 구현 완료 · 설정 0(AS-IS 동일).** 사용자 지시: "as-is하고 동일하게 기능 구현해놓고 설정도 동일하게 0으로". [[asis-porting-principles]] 원칙(기능은 만들되 AS-IS에서 꺼져 있으면 우리도 꺼둠)에 부합.

- **동작**: member가 `member.lifecycle`로 발행하는 `MEMBER_JOINED`를 point의 `MemberEventListener`(group point-service)가 구독 → `PointService.creditForSignup(userId)`.
- **금액**: `SYSTEM_CONFIG/POINT_JOIN`(point.OP_COMMON_CODE, =AS-IS OP_CONFIG.POINT_JOIN, 관리자 config/point.jsp "회원가입시 포인트"). **현재 '0' = 미적립**(AS-IS `earnPoint("join")`의 `point!=0` 가드 재현). 시드: `database/ddl/migration-point-signup-config.sql`.
- **멱등/스코프**: REF_KEY `JOIN-<userId>`로 회원당 1회, locgov 미연결(AS-IS DEFAULT_POINT_CODE), 만료는 openLot 공통.
- **켜는 법**: `point.op_common_code`의 POINT_JOIN code_value를 양수로. **기능검증**은 임시로 양수 설정 → 가입 → 마이페이지/point 잔액에 "회원가입 포인트" EARN 확인 → 0으로 복원(설정은 DB에서 live 읽어 재기동 불요).
- AS-IS가 실제 0(비활성)인 근거: 리포에 POINT_JOIN 시드 없음 + 사용자가 가입 테스트서 적립 본 적 없음. 상세 `docs/member-event-publishing-design.md` §4-3.
- 관련: [[deferred-features]](이벤트 발행 Phase A), [[asis-porting-principles]](원래 보류였다가 사용자 지시로 구현).

## honor-tier-thresholds-unset

> 명예기부자 등급 기준금액(g_locgov STDR_1/2/3LEVEL_AMT)이 전 지자체 0 — 운영데이터 적재 필요


명예기부자(기부혜택증) 등급 산정 기준금액인 `donation.g_locgov`의 `STDR_1LEVEL_AMT`/`STDR_2LEVEL_AMT`/
`STDR_3LEVEL_AMT`가 **2026-09-09 기준 전 지자체 0**이다. 원래는 지자체 담당자가 관리자 화면에서
설정하는 값인데 그 화면이 아직 없어서(`docs/as-is-feature-audit-member.md` §2-5) 채울 방법도 없다.

**Why:** 0을 임계값으로 그대로 쓰면 누적액이 항상 0 이상이라 100원만 기부해도 최고등급("특급")이
붙는다. 2026-09-09에 `DonationService.upsertHonorTier`가 양수 구간만 유효한 기준으로 보도록 방어를
넣어서 지금은 **기준금액이 0인 지자체엔 등급이 아예 부여되지 않는다**(등급 기능이 사실상 비활성).

**How to apply:** 기부혜택증 화면이 "비어 있다"는 얘기가 나오면 버그가 아니라 이 데이터 미설정이
원인이다. 운영데이터를 받거나 지자체 명예사용자 관리 화면을 만들 때 이 세 컬럼을 함께 채워야
등급이 살아난다. 관련: policy-content-db-migration-deferred(보관: docs/archive/memory-fix-log-2026-10.md)

## coupon-feature-unused-hide-ui

> 쿠폰 기능은 AS-IS에서도 미사용 - 화면 진입점만 숨기고 서비스 로직/DB/API는 그대로 둔다


2026-09-09 사용자 확인: AS-IS에서도 쿠폰 관련 기능은 현재 사용하지 않는다. 나중에 **화면에서 안 보이게만** 하고 **서비스 로직은 그대로 둔다**(삭제 아님).

**Why:** 나중에 다시 켤 수 있어야 하고, 쿠폰 로직은 주문 금액 계산에 얽혀 있어 걷어내면 회귀 위험이 크다.

**2026-09-09 조치 완료.** 자동발급 중단 + 회원 API 404 + 화면 진입점 전부 숨김. 코드는 하나도 지우지 않았다. 백엔드는 `order/application.yml`의 `ghlove.coupon.enabled: false` **마스터 스위치 하나**로 좌우된다(배치·회원API·Thymeleaf 쿠폰화면·체크아웃 쿠폰적용).

**How to apply (되살릴 때):** ① `ghlove.coupon.enabled: true` → 백엔드 전부 복구 ② `OrderService.issuePurchaseTriggeredCoupons()` 본문 주석 해제(구매트리거만 스위치 밖) ③ `storefront/src/router/index.js` 쿠폰 라우트 3줄 주석 해제 + `/mypage` redirect 3줄 삭제 ④ `storefront MyPageView.vue`와 `member/templates/mypage.html`의 "쿠폰함" 타일 주석 해제 ⑤ `CheckoutView.vue`의 `COUPON_ENABLED = true`.

**의도적으로 안 막은 것:** admin 관리 API `/api/admin/coupons**`(`CouponApiController`). admin `CouponAdminController`가 `OrderClient`로 실제 호출해서 막으면 admin 쿠폰 화면이 500이 된다. 그 화면들은 `admin-nav.html`에 링크가 없어 원래부터 메뉴 도달 불가라 그대로 뒀다. 상세 표는 `docs/cart-review-2026-09-09.md` §4-5.

관련: [[deferred-features]]

## g-qna-admin-is-seller-to-locgov

> G_QNA_ADMIN(관리자 문의 16651)은 "답례품 제공자(판매자) → 자기 지자체" 문의다 - 지자체 담당자가 운영관리에서 답하고, 시스템·행안부(ROLE_ADMIN_1~4)는 보기만. 예전 TO-BE는 "지자체 → 본사 문의"로 거꾸로 읽었다 (2026-10-20 확인)


AS-IS 흐름: 판매자가 판매자 포털 `/seller/qna-locgov`('지자체 문의')로 글을 쓰면 `G_QNA_ADMIN`(+`_FILE`)에 들어가고, 운영관리 `/opmanager/qna-admin`(TO-BE `/admin/internal-inquiry`)에서
그 지자체 담당자가 `G_QNA_ADMIN_ANSWER`(+`_FILE`)로 답한다. 작성자는 `OP_SELLER_USER`(판매자 계정)이고, 목록의 "지자체" 칸은 **판매자 계정의 지자체코드**다(문의 행의 지자체 아님).

**How to apply:** 운영관리 쪽에서 문의를 "새로 쓰는" 기능을 만들지 말 것(예전 발명 화면이 그랬다). 판매자 포털의 작성 화면은 TO-BE에 아직 없어서(판매자 포털 이식 때)
첨부 저장 위치(`{admin.upload.dir}/qnaadmin/{문의번호}/`)를 그쪽과 공유해야 한다. op_qna(QNA_TYPE 0/1/2)와는 별개 표 - 위 op-qna-split-by-qna-type.

## order-status-derivation-for-asis-codes

> TO-BE 주문 모델(od_order·od_shipment·od_order_item·od_claim)에서 AS-IS 주문 상태 숫자코드·결제상태·클레임 처리코드를 도출하는 규칙 - 전체주문관리(16401) 목록·상세에서 확정, 남은 주문 메뉴(16402~16409)도 같은 규칙을 쓸 것 (2026-10-19)


**AS-IS 주문 상태 숫자코드**(품목 줄 단위, `OrderLineListService` SQL의 CASE): 그 품목의 **최신 클레임 우선** -
CANCEL 요청/승인 70·완료 75 / RETURN 요청/승인 60·완료 65·거절 69 / EXCHANGE 요청/승인 50·완료 58·거절 59 (CANCEL 거절은 클레임 무시하고 아래로)
→ 주문 WAITING_DEPOSIT·PENDING 0 → CANCELLED 75 → 구매확정일 있음·배송 CONFIRMED 40 → DELIVERED 35 → SHIPPED·IN_TRANSIT 30 → 그 밖 10.
**배송준비중(20)·교환배송중(55)에 대응하는 TO-BE 상태가 없다** - 그 체크박스·탭은 항상 0건(발명해서 채우지 말 것).

**포인트 차감 결과 값**: `point_outcome`은 `DEDUCTED`/`FAILED`(`SUCCESS` 아님). 멀티아이템 주문은 헤더 `od_order.point_outcome`이 NULL이고 **출고(`od_shipment.point_outcome`)에** 들어 있다 →
"결제취소" 판정은 헤더 FAILED **또는** 출고 중 FAILED가 하나라도 있을 때. 그 밖은 취소완료·입금대기 주문도 AS-IS 실화면처럼 "결제완료"(처음 `!= SUCCESS`로 짰다가 반품완료 주문이 결제취소로 나와 교정).

**클레임 처리코드(상세 클레임 탭)**: REQUESTED→01 신청, APPROVED→03(취소 환불대기·반품 환불승인 대기)/교환은 10 회수중, COMPLETED→04(취소 완료·반품 환불 완료)/교환은 03 교환상품 발송, REJECTED→99 거절.
처리 대기 = 취소·반품은 01, 교환은 01·10. 출고번호(AS-IS ORDER_SEQUENCE, 0부터)는 TO-BE에서 같은 주문 안 출고 ID 순서의 **1부터**.
원장: docs/as-is-parity.md 22절 admin-order-list-all-port.

## tobe-op-item-item-type-null

> TO-BE 답례품(gift.op_item)은 ITEM_TYPE이 전부 NULL이다(AS-IS 기본값 '1' 일반상품). AS-IS SQL의 `ITEM_TYPE IN ('1','3')`(defaultOnSaleWhereQuery 등)을 그대로 옮기면 상품이 0건이 된다 - NULL을 '1'로 읽을 것 (2026-10-20)


이벤트 관리(16601) 수정 화면의 편성 상품 조회(AS-IS getFeaturedItemListByParam = defaultOnSaleWhereQuery + FRONT_DISPLAY_FLAG + 판매자 상태 2)를 옮기다 발견했다. 개발DB 26건 전부 `item_type` NULL, `front_display_flag` Y.

**How to apply:** AS-IS 판매중 조건(`DATA_STATUS_CODE='1' AND DISPLAY_FLAG='Y' AND ITEM_DATA_TYPE='1' AND ITEM_TYPE IN ('1','3')`)을 옮길 때 `coalesce(i.item_type, '1') in ('1', '3')`로 쓴다.
상품코드(ITEM_USER_CODE)가 비어 있는 것과 같은 계열의 TO-BE 데이터 공백이다(그쪽은 `G2`+9자리로 계산 - 22절 admin-item-manage-port).
