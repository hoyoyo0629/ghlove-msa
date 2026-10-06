---
name: admin-small-areas-port-progress
description: "소규모 메뉴영역 묶음 이식 원장(기부혜택증3·오프라인기부금접수2·대시보드1·커뮤니티5) - 사용자가 정한 순서, 구조갭·AS-IS 매핑"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-04T14:11:08.278Z
---

[[asis-screen-port-procedure]] 절차로 진행하는 **세 번째 묶음 = 소규모 영역 4개**. 사용자가 정한 순서:
**기부혜택증관리 3 → 오프라인기부금접수 2 → 기부현황 대시보드 1 → 커뮤니티 5**(2026-10-04).
앞선 영역: 시스템관리 25/25([[admin-system-area-port-progress]]), 회원관리 8/8([[admin-member-area-port-progress]]).

**AS-IS url 실측**(`OP_MENU_202610010945.sql`) - TO-BE menu_url과 대조해 구조갭을 먼저 찾았다:

| 영역 | id | 메뉴 | AS-IS url | TO-BE url | 상태 |
|---|---|---|---|---|---|
| 기부혜택증 | 19101 | 기부혜택증 설정 관리 | `/opmanager/lclgvHnrUser/lclgvHnrUserMng/list` | /admin/honor-users | **완료** |
| 기부혜택증 | 19102 | 기부혜택증 설정 | `/opmanager/lclgvHnrUser/lclgvHnruserMng/form` | /admin/honor-users/**form** | **완료**(분리) |
| 기부혜택증 | 19103 | 기부혜택증 열람현황 | `/opmanager/lclgvHnrUser/lclgvHnrUserViewHist/list` | /admin/honor-users/view-history | **완료** |
| 오프라인기부금접수 | 15101 | 기부금 접수관리 | `/opmanager/offgive/list/` | /offgive/**list** | **완료**(분리) |
| 오프라인기부금접수 | 15102 | 기탁서 등록 | `/opmanager/offgive/create` | /offgive/**new** | **보류**(아래 ★★ - 결정 필요) |
| 대시보드 | 20101 | 기부현황 대시보드 | `/opmanager/bix5-access` | /admin/bix5-access | |
| 커뮤니티 | 11401 | 소통방 | `/opmanager/community/bbs/list` | /community/bbs | |
| 커뮤니티 | 11402 | 자료실 | `/opmanager/community/databoard/list` | /community/databoard | |
| 커뮤니티 | 11404 | SR 게시판 | `/opmanager/community/srBbs/list` | /community/sr-bbs | |
| 커뮤니티 | 11405 | 담당자용 FAQ | `/opmanager/community/faqBbs/list` | /community/faq-bbs | |
| 커뮤니티 | 11406 | 오프라인 담당자 SR 게시판 | `/opmanager/community/offSrBbs/list` | /community/off-sr-bbs | |

**★기부혜택증관리 3화면 완료 - 2026-10-04**: AS-IS `LclgvHnrUserManagerController` 8개 엔드포인트 전부
(목록 GET/POST·설정폼 GET·저장·대표이미지삭제·열람현황 GET/POST·엑셀) + 템플릿 3개 재작성
(`honor-user-admin/{list,form,view-history}.html`). AS-IS JSP 1,295줄(form 748 + list 264 + viewHist 283) 대비 기존 TO-BE는 331줄이었다.
- **구조갭 해소**: 19101(목록)·19102(설정 폼)가 같은 URL이었다 → 19102를 `/admin/honor-users/form`으로 분리
  (`migration-admin-menu-19102-honor-user-form.sql`, 적용 완료). 4402·4501과 같은 유형.
- **★소유 서비스를 donation으로 옮겼다**: 기부혜택증 설정(`g_lclgv_hnr_user_stng_mng`)·이미지설명
  (`g_lclgv_hnr_user_rwrd_img_expln`)·열람이력(`op_honor_view_hist`)이 전부 donation 스키마에 있고 형제 표
  (`g_honor_benefit`·`g_honor_cntrbtr`·`g_honor_cntrbtr_stdr`)도 그렇다. admin 스키마에도 **같은 이름의 빈 표**가
  있었는데(예전 TO-BE 구현이 만든 것) 실데이터는 donation에 쌓인다.
- **★발견한 결함 1건 고침**: 예전 TO-BE 열람현황 화면이 **admin 스키마의 빈 `op_honor_view_hist`를 읽어 항상 0건**이었다.
  실제 열람이력은 storefront 열람 시 donation이 쌓는다(실측 5건). donation API로 바꿨다.
  admin 쪽 죽은 엔티티·레포 4개(`LclgvHnrUserStngMng`/`OpHonorViewHist` + 레포)는 **삭제**했고, 빈 표 자체는 남겼다
  (DROP은 파괴적이라 사용자 판단 - op_qustnr* 선례와 동일).
- **열람현황은 집계 화면이다**: AS-IS는 `(열람일자 yyyy-MM-dd, USER_ID, LCLGV_CD)`로 GROUP BY해 `COUNT(*)`를 열람횟수로 쓰고
  OP_USER·G_LOCGOV를 **INNER JOIN**한다. TO-BE는 회원이 member 소유라 donation이 지자체만 조인해 묶음을 주고
  **사용자명은 member 일괄조회**로 채운다 - AS-IS가 INNER JOIN이므로 **회원을 못 찾은 행은 admin이 버린다**. 정렬도 AS-IS대로 지자체코드→사용자명.
- 신규 API: donation `/api/admin/honor-users`(목록·상세·저장·대표이미지·삭제·`/view-hist`), member `GET /api/admin/members/names?userIds=`(회원 이름 일괄조회 - 첫 사용처가 이 화면이다).
- AS-IS verbatim: h3가 메뉴명이 아니라 **"기부혜택증 기준목록"/"기부혜택증 열람현황목록"/"{지자체명} 기부혜택증 기준 등록"**(수정인데 '등록') /
  출력개수 select가 다른 화면과 달리 **10·50·100·200·500** / **골드·실버 등급 금액은 입력칸·컬럼 모두 주석처리**(발급기준금액=브론즈 하나만 씀) /
  목록의 **등록 버튼도 주석처리** / 열람현황 날짜버튼에 **15일**이 있다 / 열람현황 표 caption이 "지자체 기부혜택증 기준 목록"(복사 흔적) /
  설정폼의 발급기준 라디오 옆에 공통코드 **detail 설명문**을 그대로 찍고 물음표 아이콘이 안내이미지(honor_rule4.png) 팝업을 띄운다 /
  이미지 설명표 안내문구와 순번 readonly / 메인이미지 옆 '기본 이미지'(default_honor_img.png) 동시 노출 / 미리보기는 swiper 카드 모달.
- 공통코드 `HNR_USER_SLCTN_SE_CD` 3종 실측: BF_YR_1YR(기준1 전년도) / NOW_CRTR_1YR_WTHN(기준2 최근1년) / JEJU_REQ_CRTR(기준3 회계연도) - label+detail 모두 있어 AS-IS 화면과 그대로 맞는다.
- 권한분기: 시스템·행안부(1~4)는 목록, **지자체 담당자(5·6)는 목록 없이 자기 지자체 설정폼으로 리다이렉트**(남의 것을 열면 자기 것으로 되돌림).
  열람현황도 지자체 담당자는 자기 지자체로 강제 스코프되고 **지자체 검색칸 자체가 숨는다**.
- 설정 저장은 AS-IS가 UPDATE 한 방이라 행이 없으면 아무 일도 없었는데(실측 0건), **없으면 만들고 있으면 수정**으로 바꿨다.
  대표이미지는 AS-IS처럼 새 파일이 올라왔을 때만 갱신하고, 이미지 설명은 지자체 단위로 전부 지우고 다시 넣는다(AS-IS 동일).
- **검증**: 열람현황 집계 5건(날짜·회원·지자체 묶음)·회원 INNER JOIN 판정(user 1000=테스트유저2)을 SQL로 대조. admin·donation·member bootJar 전부 EXIT=0.
- **주의**: AS-IS가 기간 기본값을 오늘로 채우므로, 실측 열람이력이 2026-09-11자라 **기본 검색은 0건이 정상**이다(기간을 넓혀야 보인다).

**★기부금 접수관리(15101) 완료 - 2026-10-04**: AS-IS `GET/POST /opmanager/offgive/list` + `/list/download-excel` 이식
(`OffgiveReceiptAdminController` + `offgive/list.html` 재작성, donation `GET /api/admin/offgive/search` 신규).
- **구조갭 해소**: 15101·15102가 같은 URL(`/offgive`)이었다 → 15101=`/offgive/list`, 15102=`/offgive/new`로 분리
  (`migration-admin-menu-15101-15102-offgive-split.sql`, 적용 완료). 예전 `/offgive` 링크는 `/offgive/list`로 리다이렉트.
- 예전 TO-BE 목록은 검색조건 4개(지자체·기간·이름)뿐이었다 → **AS-IS 검색조건 전체**(검색구분 4종·신청일·금액범위+바로가기버튼·기부상태·소속지점)와 **11컬럼** 복원.
- **★AS-IS 기부상태 숫자코드 번역**: AS-IS는 `100 신고 / 200 수납 / 300 과오납`인데 TO-BE `g_cntr.cntr_sttus_code`는 낱말
  (`REQUESTED`/`COMPLETED`/`CANCELLED`)이다. **화면 라디오 값은 AS-IS대로 100/200/300 유지, 변환은 donation 레포 경계에서** 한다
  (담당자 사용여부 9/2↔ACTIVE/LOCKED와 같은 방식). 오프라인분만 보이게 `cntr_path_code='200'`으로 한정.
- **권한별 스코프(AS-IS role)**: 1~4=SYSTEM(전체 + **소속지점 라디오 검색칸 노출**) / 7=OFF_MAIN(자기 **지점코드**만 고정 - 같은 은행의 모든 지점을 본다) /
  8=OFF_SUB(**지점코드+지점명** 고정 = 자기 지점만) / 그 외=LOC(지점조건 없음). role을 못 정하면 "권한이 없습니다."
- AS-IS verbatim: 이름 **마스킹**(`첫글자 + ' * ' + 세번째글자부터` - 두 글자면 뒤가 빈다) / 지점·센터명 = 은행명+공백+지점명 /
  신고일은 신청시각 앞 19자 / 빈 목록은 표 밖 `no_content` div(M00473) / '출력' 버튼·체크박스 칸은 주석처리 /
  **기탁서 등록 버튼은 SYSTEM이 아닐 때만** 노출 / 날짜버튼 모델 키가 `year`(다른 화면은 year1) / 안내문구 "기탁서 등록 후 정상 처리까지 최대 1시간 가량 소요될 수 있습니다."
- **검증**: 오프라인 접수분 6건이 11컬럼으로 조회됨(SQL 대조). 다만 실데이터의 `rcept_bank_code`가 전부 빈 값이어서
  지점 스코프는 데이터로 걸러지지 않는다(기존 TO-BE 접수 폼이 지점코드를 안 넣었다 - 로직이 아니라 데이터 문제).
  `rcept_bank_nm` 2건은 **한글 인코딩이 깨져 있다**(과거 테스트데이터 적재 흔적, 이번 작업과 무관).

**★★기탁서 등록(15102) 보류 - 결정 필요 (2026-10-04)**: "2화면 영역"으로 보이지만 실측은
**AS-IS 컨트롤러 1,191줄 / 28개 엔드포인트 / form.jsp 1,995줄**이고 다음에 묶여 있다:
1. **본인인증(SCI) 외부연계** - `sci-call`/`sci-result`/`user-check`/`getPublicKey`/`getUserAddressInfo`(내국인)·`getUserAddressInfoForeign`(외국인).
   기존 TO-BE는 이걸 **"담당자가 현장에서 신분증 직접 확인"으로 대체**해 두었고, 그 판단은 `OffgiveController` 주석에
   **"사용자 확인 없이 진행된 판단"**이라고 스스로 적어 놓았다 → 이식할지/대체를 유지할지 사용자 결정이 필요하다.
2. **납부 게이트웨이** - `nextBugaRequest`(지방세외 부과요청)·`getSdonationCharge`(서울 기부금 조회). [[donation-payment-gateway-port-decision]] 라운드와 함께 가야 한다.
3. **ROLE_ADMIN_11(OFF_CENTER, 오프라인 센터)** - AS-IS 권한분기에 있는데 TO-BE OP_ROLE에는 **없다**(실측 1~8만).
   15101에는 분기 자리만 남겨 뒀다. 권한을 새로 만드는 일이라 결정 필요.
4. 기탁서 서식 팝업 3종(`popup/form-policy`·`form-apply/{cntrSn}`·`form-list`)·기부한도 확인(`maxCheck`)·지정기부 선택(`getDesignatedDonationList`)·
   취소(`offgiveCancel`/`offgiveCancelReq` - 기존 TO-BE는 give-reqmng으로 대체)·`offRprs`·`updatePhoneNumber`.
→ 독립 세션 + 위 1·3 결정이 선행돼야 한다. 그래서 15101만 완결하고 15102는 기존 TO-BE 접수 폼(`/offgive/new`)을 가리키게 두었다.

**★기부현황 대시보드(20101) = 추가 작업 없음 (2026-10-04 재검증)**: 이전 라운드가 이미 올바르게 처리해 뒀고
이번에 독립 검증했다.
- AS-IS `ghlove-web`에 `/opmanager/bix5-access` **핸들러도 JSP도 없다**(bix5 grep 0건) → 클릭하면 404
  (`public/error/404.html`, 120줄). 즉 AS-IS에서 "메뉴는 있으나 눌러보면 페이지 오류"다.
- TO-BE `Bix5AccessController`가 그 404 페이지를 verbatim 복사해 **404 상태로** 돌려준다
  ([[as-is-parity-includes-disabled-state]] 그대로). 핵심문구("페이지 오류"/"페이지가 존재하지 않습니다") 대조 일치.
- **BIX5 통계 배치**(`Bix5Service` 20개 메서드 + `bix5-mapper.xml`)는 ghlove-common에 있지만 **AS-IS 웹에서 호출부가 0건**이다
  (별도 배치 배포나 외부 BI 도구가 테이블을 읽는 구조). TO-BE DB에도 bix5 테이블이 없다 → 지금 붙일 대상이 없다.
  외부 BI 연계는 운영 인프라 사안이라 기록만 한다.

**★커뮤니티 5화면 실측 - 소규모가 아니다(2026-10-04). 다음 세션 과제**:
- AS-IS 컨트롤러가 3개로 나뉘어 있다: `community/freeboard/CommunityManagerController`(**1,872줄**, 엔드포인트 40+ -
  bbs·srBbs·faqBbs·offSrBbs를 한 클래스에서 다룬다), `community/locgFaq/LocgFaqManagerController`,
  `community/locgovDataBoard/LocGovDataBoardManagerController`.
- JSP는 게시판마다 list/form/edit/detail 4종씩: freeboard 791 / databoard 904 / srBbs 1,202 / faqBbs 1,049 / offSrBbs 1,143 = **합계 ~5,100줄**.
  (그 외 `oldFreeBoard`·`locgfaq` 폴더도 있다 - live 여부 확인 필요.)
- 게시판마다 **댓글(cmnt) CRUD + 첨부파일 업로드/삭제/다운로드**가 붙어 있다(srBbs·faqBbs·offSrBbs는 댓글에도 파일이 붙는다).
- TO-BE 템플릿 디렉터리는 `community/{board,databoard,locv-faq}` 3개뿐 - 5종 게시판과 1:1이 아니다(대조 필요).
→ 한 세션에 한 게시판씩 잡는 것이 맞다. 시작 전 TO-BE 기존 자산과 1:1 대조부터([[asis-screen-port-procedure]]).

**★커뮤니티 1:1 대조 결과 - "0/5"의 정확한 뜻 (2026-10-04, 사용자가 순서 일임: "나머지도 니가 순서 정해서 해")**
- **TO-BE에 골격이 있었다**(기능 기준 0/5는 맞지만 백지가 아니다): `CmntyBoardController`(226줄, `/community/{boardType}`로
  bbs·sr-bbs·off-sr-bbs·faq-bbs 4종을 한 컨트롤러에서) + `CmntyRpstrController`(70) + `LocgFaqAdminController`(101) = **397줄**,
  템플릿 6개 **400줄**, 도메인 7개·리포지토리 6개. AS-IS는 컨트롤러 **2,493줄** + JSP **5,638줄** + 엔드포인트 **56개**
  (bbs 11 / srBbs 15 / faqBbs 15 / offSrBbs 15)다. 골격에는 **detail·edit 템플릿이 아예 없고** 검색·페이징·조회수·비밀글·
  첨부파일·댓글파일이 전부 없다 → 라인 기준 약 7%.
- **골격의 댓글 설계는 AS-IS에 없는 발명이다**: `G_CMNTY_COMMENT`(boardType 판별컬럼 단일표). AS-IS는 게시판마다 전용 표
  (`g_cmnty_cmnt` / `g_cmnty_{sr,faq,off_sr}_bbs_cmnt` + `_file`)를 쓴다. **AS-IS 표 16개가 TO-BE admin 스키마에 이미 다 있다**
  (전부 0건). 그래서 이식하는 게시판은 AS-IS 표로 갈아탄다. 발명 표는 남은 3개 게시판이 아직 쓰므로 그때까지 두고,
  다 끝나면 죽은 표가 된다(DROP은 파괴적이라 사용자 판단 - op_qustnr*·honor 선례와 동일).
- **★숨겨진 6번째 게시판**: AS-IS 메뉴 `11403 지자체FAQ`(`/opmanager/community/locv-faq/list`)가 **display_flag='N',
  status_code='2'(중지)**다. JSP 4종 538줄 + 컨트롤러 343줄이 실재한다. [[as-is-parity-includes-disabled-state]]대로
  코드는 이식하고 메뉴는 중지 유지. **TO-BE op_menu에는 11403 행이 아예 없어서 중지 상태로 추가해야 한다.**
- **★URL 충돌 1건(구조갭 5번째)**: TO-BE `5104 FAQ`와 `11405 담당자용 FAQ`가 **둘 다 `/community/faq-bbs`**다.
  AS-IS에선 전자가 `/opmanager/faq/list`(고객센터 FAQ), 후자가 `/opmanager/community/faqBbs/list`(지자체 담당자용 운영 FAQ)로
  **서로 다른 화면**이다. 5104는 고객센터(11화면) 영역 소속이니 그 라운드에서 자기 URL을 받아야 한다.
  (5107 자료실·5113 운영관리SR은 TO-BE URL이 달라서 충돌 아님 - 확인했다.)
- **내가 정한 순서**(의존 패턴 기준): ① 11401 소통방(댓글만, 첨부 없음 - 댓글 패턴 확정) → ② 11404 SR(본문+**댓글** 첨부 =
  최대 복잡도, 파일 패턴 확정) → ③ 11406 오프라인SR → ④ 11405 담당자FAQ(②와 구조 거의 동일) → ⑤ 11402 자료실 →
  ⑥ 11403 지자체FAQ(중지 유지).
- 정적자산은 이미 복사돼 있다: `static/content/modules/cmnty/{bbs,cmnt,srBbs,srBbsCmnt,faqBbsCmnt,offSrBbsCmnt}.js` +
  `content/css/community.css`. **AS-IS 원본(`ghlove-web/.../content/modules/cmnty/`)은 지금 비어 있으니 TO-BE 복사본이 정본 사본이다.**
  `op.common.js`에 `Common.responseHandler`·`DateButtonEvent`·`searchDateMonth`·`getEditorContent`가, `fragments/smarteditor.html`에
  스마트에디터2가 이미 있다. JSON 계약: 성공 `{isSuccess:true, data:"<문구>"}` / 실패 `{isSuccess:false, errorMessage:"<문구>"}`.

**★①소통방(11401) 완료 - 2026-10-04**: AS-IS `CommunityManagerController`의 bbs/cmnt **11개 엔드포인트 전부** +
템플릿 4종 신규(`community/freeboard/{list,detail,form,edit}.html` - AS-IS JSP 791줄 대응). admin bootJar EXIT=0.
- 신규: `domain/CmntyCmnt`(AS-IS `G_CMNTY_CMNT`), `repository/{CmntyCmntRepository,CmntyBbsAdminRepository}`,
  `service/{CmntyBbsAdminService,CmntyException}`, `web/CmntyBbsAdminController`(`/community` 기준 - AS-IS 경로 모양대로
  게시글은 `bbs/*`, 댓글은 `cmnt/*`), `web/support/CmntyBbsSearchParam`.
- `CmntyBoardController`에서 **bbs만 떼어냈다**(TITLES·switch 5곳). 남은 3개는 이식될 때까지 그대로 - 게시판 이식할 때마다 떼어내면 된다.
- **★AS-IS 조건의 TO-BE 번역 2건**: ① 작성자 권한 - AS-IS는 `(select min(authority) from op_user_role where user_id=...)`인데
  **TO-BE에 `op_user_role` 표가 없다**(`op_manager.authority` 한 컬럼) → 그 값을 그대로 쓴다. 소속 검색(searchRole)도 같은 치환.
  ② 작성자 소속 지자체명 - AS-IS는 `g_locgov` 조인인데 TO-BE에선 donation 소유 → 레포는 `locgov_code`만 주고
  서비스가 `LocgovClient.allLocgovs()`로 이름을 채운다(19101과 같은 방식).
- **★AS-IS와 다른 점 - 소통방은 진입(GET)에서도 조회한다**(`list`와 `searchList`가 같은 코드). 다른 운영화면의
  "검색해야 조회" 규칙과 반대라 착각하기 쉽다.
- AS-IS verbatim: 상단 빨간 안내문구("타인의 개인정보를 작성하지 않도록 각별히 유의 해주시기 바랍니다." - 그 위
  "행안부와 지자체를 위한…"은 **주석처리** 상태 유지) / **'작성자' 컬럼과 검색구분 'USERNAME' 옵션 모두 주석처리** /
  소속 표기 3분기(1·2 "시스템 관리자"는 **이름 안 찍음**, 3·4 "행정안전부"+이름, 그 외 상위지자체명+지자체명+이름) /
  공지 라벨 + 댓글 빨간 `[N]` / 상단공지 체크박스는 **1~4에만** 노출 / 체크박스 id는 `noticeYn1`·`isSecret1`
  (bbs.js가 그 id로 읽어서 **바꾸면 등록이 깨진다**) / 제목 maxlength가 등록 255·수정 1000으로 **서로 다른데 그대로 뒀다**(컬럼은 varchar(300)) /
  빈 목록은 표 밖 `.no_content`(M00170) / 정렬 `notice_yn desc, frst_crt_dt desc` / 삭제는 전부 `use_yn='N'` 소프트삭제.
- **권한규칙(AS-IS 그대로)**: 글·댓글 수정/삭제는 **작성자 본인만** - 시스템 관리자도 예외 없다("본인 게시글만 삭제 가능합니다." 등 4문구).
  비밀글은 작성자 + **ROLE_ADMIN_1·2만** 열람(3·4·5·6은 남의 비밀글 못 봄). `role`은 1~6만 값이 들어가므로 ROLE_ADMIN_10은 빈 값 취급.
  op_menu_right 실측 11401=1,2,3,4,5,6,10 → AS-IS export와 **일치**(수정 불필요).
- **★조회수는 비밀글 차단보다 먼저 올라간다**(AS-IS 순서) + **수정화면 진입에서도 올라간다**(상세와 같은 메서드를 쓴다).
- **AS-IS 결함 1건 안전하게 고침**: `detailBbs`가 글을 못 찾아도 `detail.getIsSecret()`을 먼저 호출해 **NPE**가 난다 →
  컨트롤러의 "해당 글은 존재하지 않습니다." 리다이렉트가 **도달 불가 코드**였다(삭제된 글 링크 = 500). null 반환으로 바꿔 본래 의도대로 안내.
- **의도적 차이 1건(보안)**: AS-IS는 댓글 본문도 `escapeXml="false"`로 raw 렌더링해 **저장형 XSS**가 가능하다.
  댓글칸은 일반 textarea라 HTML이 필요없고 `<pre>`가 줄바꿈을 살리므로 이스케이프했다(정상 글은 보이는 모습 동일).
  **게시글 본문은 에디터 HTML이라 AS-IS대로 raw + nl2br 유지.**
- 체크박스 초기상태도 방식만 다르다(결과 동일): AS-IS는 form:checkbox value에 현재값을 넣어 **항상 체크**로 그린 뒤
  인라인 JS가 'N'이면 푸는 식 → 서버에서 'Y'일 때만 체크해 내려주고 그 보정 스크립트를 뺐다.
- JS 자산은 경로 접두어만 고쳤다(`/opmanager/community/...` → `/community/...`). AS-IS `cmnt/add/`의 **끝 슬래시**는
  Boot 3에서 404라 뗐다. 파일 머리에 "바꾼 것은 경로뿐" 주석을 달았다.
- **검증**(`seed-admin-cmnty-bbs.sql`, 적용 완료 - 글 6건·댓글 3건): 공지 상단고정 / 소프트삭제 글·댓글 제외(6→5, 댓글 3→2) /
  댓글수 `[2]` / 소속검색 지자체 3건·시스템 1건 / 시군구 26350 1건 / 제목 LIKE 1건 / 등록일 범위 3건 - 전부 SQL 대조 일치.
  admindb 롤의 `g_cmnty_cmnt` 권한·시퀀스 2개 USAGE도 확인.

**★②SR 게시판(11404) 완료 - 2026-10-04**: AS-IS {@code srBbs/*} **15개 엔드포인트 전부** + 템플릿 4종
(`community/srBbs/{list,detail,form,edit}.html` - AS-IS JSP 1,202줄 대응). admin bootJar EXIT=0.
- 신규: `domain/{CmntySrBbsFile,CmntySrBbsCmnt,CmntySrBbsCmntFile}`,
  `repository/{CmntySrBbsFileRepository,CmntySrBbsCmntRepository,CmntySrBbsCmntFileRepository,CmntySrBbsAdminRepository}`,
  `service/{CmntySrBbsAdminService,CmntyFileStorageService,CmntyText}`, `web/CmntySrBbsAdminController`.
  라우트는 기존 op_menu 값(`/community/sr-bbs`)에 맞췄다 - **DB 변경 없음**. 하위경로는 AS-IS 그대로
  (`/list`·`/form`·`/detail/{id}`·`/edit/{id}`·`/deleteSrBbs/{id}`·`/delete-item-image`·`/cmnt/*`·`/file-download/{id}`).
- `CmntyBoardController`에서 **sr-bbs도 떼어냈다**(남은 것은 off-sr-bbs·faq-bbs 2종).
- **★★AS-IS 전역 모달 1개가 TO-BE에 아예 없었다 - 이식**: `include/modal/file-upload-agree.jsp`
  = **"자료 업로드 전 보안 점검"** 모달(개인정보 포함 여부·암호화·공유범위 3문항, 전부 '예'여야
  파일창이 열린다). AS-IS는 `layouts/common/inc_common.jsp`에서 **모든 운영관리 화면에 전역 포함**한다.
  → `fragments/file-upload-agree.html`로 옮기고 **첨부가 있는 화면에서만** 포함했다(숨은 모달이라 동작 동일,
  admin-nav 전역조각을 건드리지 않아 210페이지에 영향 없음). `fileUploadPopupOpen(fileInputId)`가 진입점.
  **남은 첨부 화면(오프라인SR·담당자FAQ·자료실)도 이 조각을 쓸 것.** bootstrap.css/js는 opmanager-head가 이미 로드.
- **`CmntyFileStorageService` 신규 - AS-IS 파일규칙 전용**: 50MB / 확장자 18종 화이트리스트 /
  저장명 `yyyyMMddHHmmssSSS_원본파일명` / `ATCH_FILE_SEQ = max+1`. 기존 `AdminFileStorageService`
  (20MB·UUID명·화이트리스트 없음)를 쓰지 않은 이유는 **화면 안내문구에 "1개(50MB 이하) / jp(e)g, png, ppt(x)…"가
  그대로 적혀 있어서**다. 크기표기 공식(B/KB/MB)도 AS-IS 경계조건 그대로 `formatSize`에 옮겼다.
  ★보안상 조인 것 1건: AS-IS는 원본 파일명을 저장명에 그대로 이어 붙여 `../` 경로이탈이 가능 → 경로부분을 뗀다.
- **★AS-IS 결함 5건 고쳤다**(상세는 `CmntySrBbsAdminService` 클래스 주석):
  ① **수정에 작성자 확인이 없다** - 같은 클래스의 `deleteSrBbs`와 소통방 `updateBbs`에는 있다.
     화면은 작성자에게만 수정 버튼을 주지만 **URL 직접호출로 남의 글을 고칠 수 있었다**(인가 취약점).
     댓글 첨부 등록·삭제, 본문 첨부 삭제도 같은 이유로 권한을 맞췄다(댓글 첨부 삭제는 화면 규칙대로 **등록자 또는 1·2**).
  ② `getSrBbsDetail`에 **use_yn 조건이 없어 삭제된 글이 URL로 열린다**(소통방은 있다 - 두 화면이 서로 달랐다).
  ③ **첨부 2개 이상이면 상세화면이 500**: 파일 크기를 스칼라 서브쿼리 하나로 뽑아 모든 첨부에 같은 값을 찍는데,
     행이 2건 이상이면 SQL 자체가 실패한다 → 파일마다 자기 크기를 쓴다(현재 첨부 1개 제한이라 보이는 결과는 같다).
  ④ **댓글 첨부 크기가 항상 빈 값**(매퍼가 `fileSize`를 안 채워 괄호만 나왔다) → 같은 공식으로 채웠다.
  ⑤ 댓글 수정 시 `LAST_MDFCN_ID`가 null로 들어간다(입력 DTO를 그대로 넘겨서) → 수정자 ID를 넣는다.
- AS-IS verbatim: 상단 안내문구 "개선하고 싶은 기능에 대한 건의 및 시스템 오류 사항에 대해 작성 부탁드립니다." /
  제목 옆 **첨부 아이콘(cli-icon_file-list.png)·비밀글 아이콘(icon_lock.png)** / 등록 버튼은 M00088이 아니라 "등록" 하드코딩 /
  **댓글 입력칸이 목록 위**(소통방은 아래)이고 **댓글 정렬이 오름차순**(소통방은 내림차순) /
  등록·수정은 ajax가 아니라 **multipart 폼 전송** 후 목록 리다이렉트("등록되었습니다."/"수정에 성공하였습니다.") /
  댓글 첨부만 **숨은 multipart 폼** → 상세 리다이렉트("파일이 등록되었습니다.") /
  파일이 디스크에 없으면 **alert+history.back() HTML 조각**을 돌려준다 /
  삭제 확인문구 "등록된 게시글을 삭제합니다. 글 삭제 시 댓글도 함께 삭제됩니다." /
  비밀글 차단 문구가 상세 "비밀글입니다."·수정 "비밀글은 본인만 수정 가능 합니다."(소통방과 다름) /
  **댓글 첨부 클라이언트 검사는 30MB인데 서버는 50MB**(AS-IS 불일치 - 그대로 둠) /
  제목 maxlength 등록 255·수정 1000(AS-IS 불일치 - 그대로 둠) / 등록화면 오타 "이미 등록된 파일이이 있습니다."(수정화면은 정상) /
  **수정화면은 조회수를 올리지 않는다**(소통방은 올린다 - 상세와 같은 메서드를 쓰기 때문).
- AS-IS **죽은 코드는 옮기지 않았다**: srBbs/form.jsp의 `downloadItemImage`/`deleteItemImage`/`deleteDataboard` 세 함수는
  **자료실 URL을 가리키는 복사 흔적**이고 등록화면엔 호출할 요소가 없다. validator의 `$("#databoardParam")`도 없는 요소다.
- **검증**(`seed-admin-cmnty-sr-bbs.sql`, 적용 완료 - 글 5·본문첨부 2·댓글 3·댓글첨부 2): 공지 상단고정 /
  소프트삭제 글·댓글·첨부 전부 제외 / 비밀글+첨부 행이 아이콘 2개 조건 충족 / 댓글 오름차순 /
  파일크기 3구간(512B·36KB·2MB) 모두 확인. 첨부 행만 넣고 디스크 파일은 안 만들어
  **"파일이 존재하지 않습니다…" 경로까지** 확인 가능하게 뒀다.
**★③오프라인 담당자 SR(11406) 완료 - 2026-10-04**: AS-IS {@code offSrBbs/*} **15개 엔드포인트 전부** +
템플릿 4종(`community/offSrBbs/{list,detail,form,edit}.html` - AS-IS JSP 1,143줄 대응). admin bootJar EXIT=0.
- 신규: `domain/{CmntyOffSrBbsFile,CmntyOffSrBbsCmnt,CmntyOffSrBbsCmntFile}`, 리포지토리 4개
  (3 JPA + `CmntyOffSrBbsAdminRepository`), `service/CmntyOffSrBbsAdminService`,
  `web/CmntyOffSrBbsAdminController`(`/community/off-sr-bbs` - 기존 op_menu 값, **DB 변경 없음**),
  `web/support/CmntyOffSrBbsSearchParam`. `CmntyFileStorageService`·`CmntyText`·`fragments/file-upload-agree`는 ②의 것을 재사용.
- **11404와 다른 점은 "소속"뿐이다**: 작성자 소속이 지자체가 아니라 **`[은행명] 지점명`**이고,
  은행명은 공통코드 **OFF_BANK_LIST** label(AS-IS도 서브쿼리로 바로 가져온다), 지점명은 `op_manager.psitn_nm`.
  검색도 지자체(시도/시군구) 대신 **은행 select(shBank)**, 검색구분이 제목 / **소속지점(PSITN, LIKE)**,
  소속 구분이 **오프라인 담당자(ROLE_ADMIN_7 → 7·8을 함께)**다. 소속 컬럼 폭 250px(11404는 150px).
  삭제 경로만 AS-IS가 `deleteOffSrBbs/{bbsId}`로 다르다.
- **★댓글에는 지자체 담당자 분기가 있다**: 메뉴 권한 실측이 **1·2·7·8**(지자체 5·6은 들어올 수 없음)인데
  AS-IS 상세화면 댓글에는 **5·6(지자체) 분기와 7·8(지점) 분기가 따로** 있고 otherwise도 지자체명이다
  (과거 데이터 대비). 그대로 옮겼고 시드로 그 경로까지 확인했다.
- AS-IS 결함 5건은 ②와 **같은 내용·같은 방식으로** 고쳤다(수정 권한 없음 / 삭제된 글 URL 열림 /
  첨부 2개 이상 상세 500 / 댓글 첨부 크기 빈 값 / 댓글 수정자 ID null).
- **제목(h3)은 `activeMenu.menuName`을 쓴다**: `MENU_11406`이 op_common_message에 **없어서**(실측)
  `msg.get()`이 코드 문자열("MENU_11406")을 그대로 뱉는다. AS-IS JSP의 h3는 원래 비어 있고 레이아웃이 채우므로,
  **op_menu.menu_name(AS-IS 원문 '오프라인 담당자 SR 게시판')**을 쓰는 것이 맞다. **11405도 같은 상황이니 같게 처리할 것.**
- **검증**(`seed-admin-cmnty-off-sr-bbs.sql`, 적용 완료 - 글 6·본문첨부 1·댓글 4·댓글첨부 1):
  소속 표기 3종(시스템/행안부/[농협은행] 서울중앙지점·[농축협] 수원농축협·[제주은행] 제주본점) /
  공지 상단고정 / 비밀글 / 소프트삭제 제외 / 댓글 3건 오름차순(시스템·**지자체(5)**·오프라인부담당자(8)) /
  은행검색 012=1건 · 소속지점 LIKE '지점'=1건 · 소속 7·8=3건 - 전부 SQL 대조 일치.
  작성자는 4601 시드(`seed-admin-off-person-in-charge.sql`)의 담당자 9701·9705·9707·9704를 재사용했다.
- `CmntyBoardController`는 이제 **faq-bbs 하나만** 남았다(`listOf`도 제거). ④가 끝나면 이 클래스와
  `community/board/*` 템플릿·`G_CMNTY_COMMENT` 표가 **전부 죽은 코드**가 된다.

**★④담당자용 FAQ(11405) 완료 - 2026-10-04**: AS-IS {@code faqBbs/*} **15개 엔드포인트 전부** + 템플릿 4종
(`community/faqBbs/{list,detail,form,edit}.html`). admin bootJar EXIT=0. 라우트 `/community/faq-bbs`(기존 op_menu, DB변경 없음).
- **★AS-IS 질문유형은 Java enum이었다**: `enumMapper.get("FaqType")` → `saleson.common.enumeration.FaqType` 11종
  (F_LOGIN 회원가입/로그인 · F_CNTR_SYSTEM 기부하기 · F_CNTR_POINT 기부포인트 · F_OFF_CNTR 오프라인기부 ·
  F_CNTR_DESIGNATED 특정사업기부 · F_API_PLATFORM 세액공제 · F_PRESENT_PURC 답례품 · F_ORDER 주문/배송 ·
  F_OPEN 민간플랫폼 · F_SYSTEM 시스템 · F_ETC 기타). **저장값은 enum 이름**(getCode()=name()).
  TO-BE 관례대로 공통코드 **`CMNTY_FAQ_TYPE`**으로 적재(`migration-admin-cmnty-faq-type-codes.sql`, 적용 완료).
  ★예전 TO-BE 골격은 **쇼핑몰 FAQ 코드(`FAQ_TYPE` 1~6 회원정보/주문결제/…)**를 쓰고 있었다 - 코드셋이 전혀 달라 잘못된 연결이었다.
- **★AS-IS가 노출하지 않는 기능은 그대로 비활성 유지**: FAQ 상세화면에는 SR·오프라인SR과 달리
  **댓글 '파일등록' 버튼과 댓글 첨부 목록이 없다**. 숨은 업로드 폼·스크립트·서버 엔드포인트는 AS-IS에 그대로 있어
  같이 옮기고 버튼·목록만 렌더링하지 않았다 → **이 게시판은 본문 첨부만 실제로 쓰인다**.
- AS-IS verbatim(11404와 다른 점): 상단 빨간 안내문구가 **없다** / 소속 컬럼 대신 **질문유형** 컬럼 /
  질문유형이 select가 아니라 **탭**(한 줄 6개, '전체'가 첫 탭, 누르면 바로 검색) /
  등록·수정 화면의 **상단공지 체크박스에 권한조건이 없다**(SR은 1~4만) /
  첨부 파일칸이 **보안 점검 모달 없이 그냥 file input** / 수정 확인문구가 "게시글을 수정하시겠습니까?"(SR은 복사 흔적으로 '등록') /
  삭제 경로가 `deleteFaqBbs/{bbsId}`.
- **Thymeleaf 제약 1건 처리**: AS-IS는 `forEach` 안에서 `</ul><ul>`로 탭 줄을 바꾸는데 Thymeleaf는 태그를 그렇게 쪼갤 수 없다
  → `CmntyFaqBbsAdminService.faqTypeTabRows()`가 **6칸씩 묶어** 내려준다(AS-IS 줄바꿈·빈칸 패딩 규칙 그대로, 보이는 결과 동일).
- `faqBbsCmnt.js` **AS-IS 오타 1건 고침**: 삭제 URL이 `deletefaqBbs`(소문자 f)로 컨트롤러와 안 맞았다.
  상세화면 버튼에 `.delete` 클래스가 없어 바인딩되지 않는 죽은 코드지만 잘못된 경로를 남기지 않았다.
- **★임시 골격 제거 완료**: `CmntyBoardController`·발명 엔티티 `CmntyComment`+리포지토리·`community/board/*` 템플릿 **삭제**.
  4개 게시판이 모두 AS-IS 표로 옮겨가 완전히 죽은 코드가 됐다. 빈 표 `G_CMNTY_COMMENT`는 남겨 뒀다(DROP은 사용자 판단).
  `CmntyBoard` 매핑 슈퍼클래스는 4개 엔티티가 실제로 공유하므로 유지.
- **검증**(`seed-admin-cmnty-faq-bbs.sql`, 적용 완료 - 글 6·첨부 1·댓글 3): 질문유형 라벨 매핑 / 공지 상단고정 /
  비밀글 / 소프트삭제 제외 / 질문유형 탭 필터(F_OFF_CNTR=1건). **골격이 남긴 `faq_type='1'` 행은 질문유형이 빈 값으로 나온다** -
  AS-IS도 코드를 못 찾으면 빈 값이라 동작 일치(확인용으로 남겨 뒀다).

**★⑤자료실(11402) 완료 - 2026-10-04**: AS-IS `LocGovDataBoardManagerController` 이식 + 템플릿 3종
(`community/databoard/{list,detail,form}.html`). admin bootJar EXIT=0. 라우트 `/community/databoard`(기존 op_menu).
- 신규: `domain/CmntyFile`(AS-IS `G_CMNTY_FILE` - 다른 게시판과 달리 **RPSTR_ID**를 갖는다),
  `repository/{CmntyFileRepository,CmntyRpstrAdminRepository}`, `service/CmntyRpstrAdminService`,
  `web/support/CmntyRpstrSearchParam`. 기존 `CmntyRpstrController`(70줄 골격)를 전면 교체.
- **다른 게시판과 다른 점**: **댓글도 비밀글도 없다**(표에 컬럼 자체가 없다) / 등록·수정이 **템플릿 하나를 공유**한다
  (AS-IS edit도 form.jsp를 돌려준다 - 그래서 edit.jsp가 없다) / 첨부 파라미터가 **`detailImageFiles[]`**(대괄호!) /
  등록화면 role 판정에 **ROLE_ADMIN_10이 포함**(SR·FAQ는 1~6) / 컬럼 5개(No./소속/제목/조회수/**등록일 yyyy-MM-dd**) /
  검색구분 '전체'의 값이 **빈 문자열**(다른 게시판은 'ALL') / 수정 완료문구가 M00289("수정되었습니다.") /
  삭제 ajax 응답에 **메시지가 없다** / 파일 없을 때 다운로드가 **alert 스크립트가 아니라 오류 응답**(AS-IS ApiResponseEntity.error).
- **게시글 삭제 시 첨부 행은 건드리지 않는다**(AS-IS deleteRpstr이 게시글만 UPDATE) - 그대로 따랐다.
- **★AS-IS 죽은 엔드포인트 2개는 이식하지 않았다**(기록만):
  ① `POST /deleteDataboard`(체크박스 일괄삭제) - 목록 표에 **체크박스 컬럼이 아예 없어** 호출 불가인 죽은 UI이고,
     게다가 이 엔드포인트는 `LocgovDataBoardService`를 거쳐 **전혀 다른 표 `op_community_databoard`**를 지운다
     (화면이 보여주는 `g_cmnty_rpstr`와 무관 - 눌렀더라도 아무 행도 안 지워지고 오류만 났을 것이다).
  ② `POST search-date` - 날짜만 계산해 모델에 담고 JSON에는 아무 값도 싣지 않으며, 호출하는 곳도 없다.
  같은 이유로 list.jsp의 `deleteNotice`·`deleteCheckDataboard`·`checkedEventSet`, detail.jsp의 `formatBytes`(인자 없이 호출돼 NaN),
  `deleteItemImage`(**1:1문의 URL `/opmanager/qna/delete-item-image`를 가리키는 복사 흔적**)도 옮기지 않았다.
- **AS-IS 결함 고침**: 목록의 첨부 유무를 `(select orgnl_atch_file_nm ...)` 스칼라 서브쿼리로 뽑아 **첨부 2건 이상이면 SQL 실패** →
  개수를 세어 아이콘 판정. 상세 첨부 크기도 같은 유형(파일별 자기 크기로). 수정·삭제에 **작성자 확인 추가**(다른 게시판과 동일한 인가 누락).
- **검증**(`seed-admin-cmnty-rpstr.sql`, 적용 완료 - 글 5·첨부 2): 소속 3종·공지 상단고정·첨부 아이콘·소프트삭제 제외·등록일 포맷 확인.
- **★부수 수정(5개 게시판 전부)**: 목록 정렬이 `order by notice_yn desc`인데 PostgreSQL은 **DESC에서 NULL을 먼저** 놓는다.
  옛 골격이 남긴 `notice_yn IS NULL` 행이 **공지보다 위로 올라오는** 것을 실측 → 5개 리포지토리 모두
  `coalesce(a.notice_yn,'N') desc`로 바꿨다(정상 데이터에는 영향 없음, 컬럼 기본값은 'N').

**★★⑥지자체FAQ(11403) - 메뉴 등록만으로 종결. 결정 완료 - 2026-10-04**
사용자 결정: **"② AS-IS가 꺼 둔 화면이니 메뉴 등록만으로 종결"**(내가 권한 방안). **다시 열지 말 것.**
결정 근거와 하지 않은 일을 `LocgFaqAdminController` 클래스 주석에도 박아 뒀다(다음 세션이 "미이식 화면"으로 오인하지 않게).
- AS-IS op_menu 실측: `(11403,11400,'지자체FAQ','/opmanager/community/locv-faq/list',3,'N','2')` →
  **display_flag='N', status_code='2'(중지)**. TO-BE에는 행이 **아예 없었다** →
  `migration-admin-menu-11403-locgov-faq-stopped.sql`로 **중지 상태 그대로** 등록(적용 완료).
  status_code='2'라 `MenuService.visibleMenus()`(='1'만)에 안 걸려 nav·breadcrumb에 영향 없다.
  AS-IS op_menu_right에도 11403 행이 없어(메뉴가 꺼져 권한 미부여) 추가하지 않았다.
- **★화면을 덮지 않은 이유**: AS-IS `LocgFaqManagerController`가 다루는 표 `op_community_locgovfaq`는
  **TO-BE에서 고객센터 공개 FAQ(`/faqs`·`/api/faqs` → `FaqService`)의 실데이터 63행**이고,
  같은 URL(`/community/locv-faq`)에 그걸 관리하는 TO-BE 화면(`LocgFaqAdminController` 101줄)이 **살아서 쓰이고 있다**.
  **AS-IS가 꺼 둔 화면을 살아있는 공개 FAQ 관리 화면 위에 덮으면 운영 중 기능이 깨질 수 있다** → 사용자 결정 사항으로 올림.
- **★덤으로 찾은 live 결함(고객센터 영역, 이번에 손대지 않음)**: `database/ddl/service-admin.sql`(4962행~)이
  `FAQ_TYPE`에 **JOIN/DONATE/POINT/OFFLINE/DESIGNATED/TAX/GIFT/ORDER/PRIVATE/SYSTEM/ETC 11행**(라벨은 AS-IS FaqType과 동일)을
  넣으려 하는데, 그 INSERT가 **`ADMIN_COMMON_CODE`라는 다른 표 이름**을 쓴다 → 실제 `admin.op_common_code`에는
  들어가지 않았고 `FAQ_TYPE`에는 쇼핑몰 코드 1~6만 있다. 그래서 `op_community_locgovfaq`의 63행
  (faq_type=JOIN/DONATE/…)은 **질문유형 라벨을 못 찾아 빈 값으로 보인다**(공개 FAQ·관리화면 양쪽).
  고객센터(11화면) 라운드에서 그 11행을 올바른 표에 넣으면 해결된다. 담당자용 FAQ(11405)는 `CMNTY_FAQ_TYPE`을 쓰므로 무관.
- AS-IS 자산(이식 시 참고): 컨트롤러 343줄(list GET/POST·create GET/POST·**list/{id}가 상세**·edit/{id} GET/POST·
  delete/{id}·deleteDataboard) + JSP 4종 538줄. 질문유형은 `FaqType.values()`(enum 전체)를 쓰는데
  **실데이터 코드셋과 애초에 맞지 않는다**(enum은 F_* 이고 데이터는 JOIN/DONATE/…) - 메뉴가 중지된 배경으로 보인다.

- **커뮤니티 영역 종료**: 게시판 **5개 이식 완료**(11401 소통방 · 11404 SR · 11406 오프라인SR · 11405 담당자FAQ · 11402 자료실)
  **+ 11403 지자체FAQ는 메뉴 등록만으로 종결**(AS-IS 중지 화면, 사용자 결정 ②). 추가 작업 없음.
- 이 영역에서 만든 공용 자산(다음 게시판·첨부 화면에서 재사용): `service/CmntyFileStorageService`(AS-IS 50MB·확장자 18종·
  `yyyyMMddHHmmssSSS_원본명`·크기표기 공식), `service/CmntyText`(decode·nl2br), `service/CmntyException`,
  `fragments/file-upload-agree.html`(자료 업로드 전 보안 점검 모달 - **AS-IS는 전역 포함이니 첨부 있는 신규 화면은 반드시 붙일 것**).
  - ④는 **②·③과 구조가 거의 같다**(본문+댓글 첨부, 표 `g_cmnty_faq_bbs*`). 추가되는 것은
    **FAQ 구분(FAQ_TYPE 공통코드)** 하나이고 AS-IS 매퍼·서비스 메서드 이름만 `*FaqBbs*`로 바뀐다.
    **②의 파일 7개를 그대로 복제해 이름·표·FAQ_TYPE만 바꾸면 된다**(③을 그렇게 했다).
    `faqBbsCmnt.js`도 이미 복사돼 있으니 경로 접두어만 고치면 된다. 제목은 `activeMenu.menuName`.
  - ⑤ 자료실(11402)은 **별도 컨트롤러**(`LocGovDataBoardManagerController` 278줄 + databoard JSP 904줄)이고
    TO-BE에 `CmntyRpstrController`(70줄) + `CmntyRpstr` 엔티티 골격이 있다. 표는 `g_cmnty_rpstr` + `g_cmnty_file`.
    **주의: TO-BE `/admin/data-board`(메뉴 5107, 고객센터 자료실)와는 다른 화면이다** - 혼동 금지.
  - ⑥ 지자체FAQ(11403)는 AS-IS에서 **메뉴 중지(display_flag='N', status_code='2')**다.
    코드는 이식하고 **TO-BE op_menu에 11403 행을 중지 상태로 추가**해야 한다(현재 행 자체가 없다).
    AS-IS 컨트롤러 `LocgFaqManagerController` 343줄 + JSP 538줄, TO-BE `LocgFaqAdminController` 101줄 골격 있음.

**★납부 게이트웨이 부과요청 2건 구현 완료 - 2026-10-04 (사용자 승인: "시드데이터 생성해서 구현하고 기능확인 가능하면 동일하게")**
15102 보류사유 중 **2번(납부 연계)만 분리해 완결**했다. 화면(15102)은 1·3 보류라 못 열지만 연계 경로는 독립 검증된다.
- **AS-IS가 이미 목 분기를 갖고 있다**: `nextBugaRequest`는 `ServiceType.LOCAL`이면 전자납부번호를
  `"99" + yyyyMMddHHmmssSSS`로 만들고 `linkRstCd="000"`, `linkRstMsg="LOCAL FAKE : {번호}"`로 응답한다.
  그 분기를 그대로 옮겼으므로 **목 구현도 AS-IS verbatim**이다(내 발명이 아니다).
- 신규: `service/integration/NextBugaRequest`(차세대 부과요청 전문 - g_next_buga_request 컬럼과 1:1),
  `SeoulBugaRequest`(서울 세외 **~90키** payload를 키 순서·빈 값·0까지 verbatim),
  `LocalTaxClient.sendNextBuga/sendSeoulBuga`(기존 5필드 자리표시자 payload를 AS-IS 실제 전문으로 교체),
  `LevyRelayService`(AS-IS 고정값·파생값 조립), `LevyLinkLogWriteRepository`, `POST /api/admin/levy/{next-buga,seoul-buga}`.
- **AS-IS 파생값 공식 그대로**: `sgbCd`=지자체 행정기관코드 / `dptCd`=처리부서코드에서 **뒤 4자리 뗀 값** /
  `spclFisBizCd`=회계구분 51·61이면 **7092**, 그 외 0000(AS-IS 주석 "2024.03.04 개발원 요청") /
  `fyr`=올해 · `actSeCd`=회계구분 · `rprsTxmCd`=224102 · `operItemCd`=000 · `pyrSeCd`=01 · `pyrSttCd`=10 ·
  `lotnoRoadAddrSeCd`=02 · `mngItemCn1`="고향사랑기부금" / 회계구분이 비면 지자체코드 3~5자리 '000'이면 31, 아니면 41
  (AS-IS `getLocgovFisSp` - 4401 회계구분 분기와 같은 규칙) / 서울 `siguCd`=행정기관코드(AS-IS `getSiguCdSeoul`).
- **채번 시퀀스 2개 신규**(`migration-donation-levy-relay-seq.sql`, 적용 완료):
  `g_cntr_link_mng_key_seq`(연계관리키 = yyyyMMddHHmmss+seq), `gif_seoul_book_no_seq`(서울 대장번호).
  AS-IS는 linkMngKey에 기부번호 시퀀스(g_cntr_cntr_sn)를 쓰는데 TO-BE는 기부번호를 앱에서 만들어 그 시퀀스가 없어 전용으로 만들었다.
- **★연계로그 적재는 "목일 때만" 한다**: AS-IS 애플리케이션에는 `g_next_buga_request`·`gif_seoul`에 **INSERT가 없다**
  (실측: 매퍼에 SELECT만 - 연계서버/상대 시스템이 적재한다). 그래서 `enabled=false`일 때만 적재해
  개발환경에서 admin **1413 서울세외 부과연계 로그**·**1415 지방세외 부과연계 로그** 화면으로 흐름을 확인하게 했다.
  실연계(`enabled=true`)에서는 적재하지 않는다(상대 시스템 표에 끼어들면 안 된다).
- **AS-IS 결함 1건 안전하게 고침**: 전자납부번호를 `linkRstMsg.split(":")[1]`로 꺼내는데 ':'이 없으면
  `ArrayIndexOutOfBoundsException`이 나고 바깥 catch가 삼켜 **요청이 조용히 실패**한다 → ':' 없으면 빈 값으로 두고 결과코드만 돌려준다.
- 설정: `ghlove.integrations.local-tax.link-trgt-cd`(AS-IS 상수 LINK_TRGT_CD) 추가, `enabled: false` 기본 유지.
- **검증**: 목 INSERT를 SQL로 재현해 1413·1415 화면 질의에 잡히는 것까지 확인
  (1415: link_mng_key=202610040853491·dpt_cd=처리부서코드-4·link_rst_cd=000·"LOCAL FAKE : 99…" /
   1413: enapbu_no="99"+ts·error_cd=0). 기존 시드의 정상/오류 행과 함께 보인다. donation bootJar EXIT=0.
- **남은 연계(미착수)**: `localSunapConfirm`(지방세외 수납확인)·`etaxSunapInfo`(서울 수납확인)는 1414·1416 수납연계
  로그와 짝인데 AS-IS에서 **배치**(`NgDonationBatchServiceImpl`)가 호출한다 - 배치 쪽 라운드에서 같이 보는 게 맞다.
