---
name: asis-porting-principles
description: ★AS-IS 이식 원칙 모음 - 발명 금지·문구/CSS/JS verbatim·대체 금지·비활성 상태까지 parity·AS-IS 서비스로직이 정본·전수대조·전부 이식 먼저·전환사업 범위·SalesOn 미사용 보류·메뉴위치 먼저·내용으로 대조·구조적 결함 보고
metadata:
  type: feedback
---

AS-IS를 TO-BE로 옮길 때 지키는 원칙들. 반복 지적된 것들이라 모든 이식 작업 전에 확인한다.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## no-invented-features-ask-first

> ★대원칙: AS-IS에 없는 기능/화면/필드/버튼은 발명·추가 절대 금지. 무조건 있는 그대로. 예외는 ISP/RFP의 신규·변경 요청뿐이고 그때도 먼저 사용자에게 물어 결정한다


**★대원칙(2026-10-06, 사용자가 강하게 반복 지시):** AS-IS에 없는 기능·화면·필드·버튼·드롭다운·
엔드포인트는 **내 판단으로 발명하거나 추가하지 않는다. 무조건 AS-IS에 있는 그대로.**
유일한 예외는 **ISP/RFP에 신규 기능 추가나 변경 요청이 명시된 경우**뿐이고, 그때도 **먼저
사용자에게 물어보고 결정**한다. 빼지도, 켜지도, 새로 만들지도 말 것.

**Why:** 반복해서 TO-BE가 AS-IS에 없는 것을 지어넣어 왔다. 대표 사례(2026-10-06 관리자 권한
승인관리 상세 팝업): AS-IS 승인은 관리자가 권한을 고르지 않고 **신청구분(reqstSeCode)이 그대로
부여 권한**이 되는데, TO-BE는 "부여 권한" 드롭다운과 "소속 지자체" 입력을 **발명**해 폼에 넣었다.
AS-IS는 라디오(승인/거절) 하나 + 거절 시 거절사유뿐이다. 이런 발명이 쌓이면 "AS-IS 재현"이라는
전제 자체가 무너지고, 사용자가 매번 화면을 열어 발견해 지적하는 악순환이 된다. 사용자 표현:
"as-is에 없는 기능이나 화면은 니 마음대로 발명/추가 절대 금지. 무조건 있는 그대로가 원칙."

**How to apply:**
- 화면/기능을 손대기 전에 **반드시 4축을 메서드 단위로 먼저 다 읽는다**:
  JSP + 매퍼 SQL + 컨트롤러 + ServiceImpl 본문. 읽기 전에 편집 금지([[asis-screen-port-procedure]]).
- 그 분석에서 TO-BE에만 있는 필드/버튼/엔드포인트/로직이 보이면 = **발명이다. 제거하고 AS-IS로
  되돌린다**(되돌리기는 ASK 불필요 - AS-IS가 정본이므로).
- AS-IS에 없는 **새 동작이 TO-BE 아키텍처상 불가피**한 경우(예: admin op_manager가 member와
  분리돼 있어 승인 시 자격증명을 어떻게든 만들어야 함)는 **임의로 정하지 말고 사용자에게 묻는다.**
- ISP/RFP 신규·변경도 **구현 전에 묻는다.** 근거 없이 "좋아 보여서" 추가 금지.
- 비활성/숨김/주석 상태도 AS-IS 그대로 유지한다([[asis-porting-principles]]).
- 정적요소(CSS/JS/이미지)는 이미 AS-IS에서 전부 복사해 왔다 - "없어서 새로 만든다"가 아니라
  "AS-IS 어디에 있나"를 먼저 찾는다([[asis-porting-principles]]).

관련: [[asis-porting-principles]] [[asis-porting-principles]]
[[asis-porting-principles]] [[asis-porting-principles]]

## copy-as-is-verbatim-never-invent

> 사용자노출 문구·라벨·저장포맷·표시로직은 AS-IS 소스에서 그대로 복사한다. 내 해석으로 새 용어(예: '각인')를 짓거나 라벨을 바꾸지 말 것. AS-IS에 근거 없으면 짓지 말고 확인. 반복 지적받음.


**사용자 반복 지적(2026-09-22, 강하게)**: 내가 계속 AS-IS 정본을 안 보고 **내 판단으로 문구를 지어냄**. 예: `itemTextOptionFlag`를 "각인"이라 부르고 화면에 "각인" 라벨을 붙임(AS-IS 정본은 "필수 추가정보"). 표시도 "필수 추가정보 : " 라벨을 임의로 붙이고, 저장포맷도 "값만 ||"로 함 — **AS-IS는 라벨 없이 `formatTextOption`만 쓰고, 저장은 "제목 : 값 || 제목 : 값"**.

**Why:** 전환은 AS-IS 정본 재현이 목표([[asis-porting-principles]], [[asis-porting-principles]]). 내가 지어낸 용어/라벨/포맷은 근거 없는 변경이고, 사용자가 "기준으로 삼으라"고 여러 번 강조했는데도 반복해서 self-판단함. 이건 신뢰를 깎는 반복 실패다.

**How to apply (예외 없이):**
- 사용자에게 보이는 **모든 문구·라벨·버튼·안내·플레이스홀더**는 AS-IS 소스(JSP/HTML/JS)에서 **그대로 복사**하고, 코드에 근거(file:line)를 남긴다.
- **저장 포맷·표시 함수·검증 메시지**도 AS-IS 로직을 그대로 이식한다(예: `formatTextOption`은 cart/index.html·orderDetail.html 원문 그대로: `제목 : 값`, 값 20자 초과 시 `...`, `<br>` 조인, v-html 렌더).
- AS-IS에 해당 문구/로직이 **없으면 짓지 말고** 사용자에게 확인한다. "내 해석으로 더 명확하게"는 금지.
- 새 용어를 만들어 부르지 말 것(각인❌ → 필수 추가정보⭕). 개발용 코드주석에만 보조설명 허용.
- 정정 사례: 필수 추가정보 라벨 "필수 추가정보 입력"(details-main.html:234), "필수 추가정보 등록/사용여부"(form.jsp:1572/1585), 표시는 `formatTextOption` 그대로([[gift-option-multiform-redesign]]).

**★반복 재발(2026-09-22, 사용자 크게 분노 "하루종일 같은 얘기 몇번…")**: 문구·포맷뿐 아니라 **UI 요소의 위치/존재 자체**도 AS-IS를 먼저 확인해야 한다. 응원메시지(cheerMsg)를 AS-IS는 **특정사업 상세 "응원메시지(기부내역)" 탭에서 본인 기부건만 인라인 편집(saveCheerMsg, 30자)** 하는데, 나(및 이전 세션)는 **기부하기 화면(DonateView)에 없던 입력칸을 지어넣고** 거기에 30자까지 덧발라 두 번 일했다. AS-IS 기부하기 화면엔 응원메시지 입력이 아예 없다.
- **How to apply(강화)**: 새 화면요소를 만들기 전에 **"AS-IS의 어느 화면 어느 위치에 있는가"부터 소스로 확인**한다. 없으면 만들지 말고, 있으면 그 위치·조건(예: 본인건만 giveOrder==1)·동작(인라인 편집/버튼/API)까지 그대로 옮긴다. "탭을 채우려면 필요"식으로 위치를 임의 이동/발명 금지.
- 정정 완료: DonateView 응원메시지 입력 제거, DesignatedDetailView 기부내역 탭에 본인건 인라인 입력+"적용"+`POST /api/designated-donation/saveCheerMsg`(30자) 재현.
관련 [[asis-porting-principles]].

## copy-asis-css-js-assets-verbatim

> AS-IS의 CSS/JS 등 프론트 자산은 그대로 복사해 동일 구현. 대체·단순화·생략 절대 금지(표로 차트 대체 등)


**CSS·JS·차트·에셋을 TO-BE에 "없으니 표로/간단히" 대체하지 말 것. AS-IS 파일을 그대로 복사해 동일하게 구현한다.** 사용자가 여러 번 강하게 지적(2026-10-01 포함, "css건 js건 as-is와 동일하게 하라고 몇번을 말하냐").

**Why:** MSA 전환은 AS-IS 화면을 100% 동일 재현하는 것([[asis-porting-principles]], [[asis-porting-principles]]). 라이브러리가 TO-BE에 없으면 **AS-IS에서 복사해 오면 되는 것**이지, 표·단순화로 바꾸면 화면이 달라진다. "TO-BE에 ~가 없어서"는 대체 사유가 안 됨 — 가져오면 됨.

**How to apply:**
- 화면 구현 전 AS-IS가 쓰는 CSS/JS(차트 등)를 먼저 확인하고, 없으면 AS-IS에서 복사(`ghlove-web/static/content/...`, `ghlove-frontend/...`)해 동일 사용.
- 차트=AS-IS가 Chart.js면 Chart.js로(예: `content/modules/chart.min.js`+`op.chart.js`의 `ChartCommon.drawChart`), 표로 대체 금지.
- 2026-10-01 만족도조사: 처음에 "admin에 차트 라이브러리 없음→표"로 했다가 지적받아 chart.min.js+op.chart.js 복사 후 AS-IS 동일 스택막대로 교정.
- **소급 점검**: 기존에 내가/이전 라운드가 AS-IS 차트/자산을 표·단순화로 바꿔둔 화면(admin give-statistics, 특정사업 analysis 등 통계류) 발견 시 AS-IS대로 교정.

## no-pragmatic-substitutions-in-ports

> 화면 이식 때 '자산이 없으니 대체/생략'·'비슷한 컬럼으로 추정' 금지 - 같은 메커니즘 그대로, 숨김요소·오타까지 (15102에서 반복 지적)


AS-IS 기능의 뒷받침(파일·데이터·외부연계)이 TO-BE에 없어도 **같은 메커니즘으로 똑같이 만든다**. 다른 걸로 대체하거나
(예: "지정기부목록 다운로드"를 storefront 목록 링크로), 생략하거나(예: PDF 파일이 없다고 "기탁서 양식 및 약관 출력" 버튼 제거),
"도달 불가해 보인다"며 숨김 요소를 빼는 것 전부 금지. 없는 자산·데이터는 구현은 동일하게 해 두고 **보고만** 한다.

**Why:** 2026-10-13 기탁서 등록(15102)에서 "야..왜 또 니가 판단하고 발명하냐? as-is랑 동일하게 구현하라는 원칙을 또
잊은거야?" — 상태문구/안내문 발명, 버튼 대체·생략, 숨김요소 생략, CSS 클래스 대신 인라인 width를 지적받았다.
"as-is에 다운로드 파일이 없어도 기능은 동일하게 만들어야 할거아냐."

**How to apply** (화면 이식 체크리스트, [[asis-screen-port-procedure]]·[[asis-porting-principles]]의 보강):
- 마크업은 JSP를 줄 단위로 옮긴다: 클래스·title·name·colgroup/colspan/rowspan·`&nbsp;`·`<font>`까지. AS-IS 오타도
  화면에 영향 있으면 그대로(`btn-defualt`, `cursor: ponter`). 숨김(`display:none`) 요소와 그 JS 로직도 포함.
- 셀렉트 옵션은 하드코딩 말고 AS-IS가 쓰는 공통코드 그대로(PHONE=010/011/017/019, WDR=시도 17개), 문구는
  메시지 카탈로그(`${msg.get('M00159')}`)·공통코드 detail(한도 "2천만").
- 데이터 출처는 이름이 비슷한 컬럼으로 추정하지 말고 **매퍼 SQL로 확인**: 대표답례품은 `OP_ITEM.REPRESENTATIVE_ITEM_YN`이
  아니라 `G_LCLGV_OFF_RPRS_GDS_MNG`였다.
- 화면이 부르는 엔드포인트는 AS-IS 경로에서 `/opmanager`만 뗀 이름, 응답은 `isSuccess/data/errorMessage` 모양 →
  JS를 원문 그대로 옮길 수 있다. 호출처가 0건인 함수(죽은 코드)만 빼고, 뺀 이유를 주석으로 남긴다.

## as-is-parity-includes-disabled-state

> AS-IS 재현 기준점: 화면/서비스에 기능이 있으면 동일하게 다 만든다. 단 AS-IS에서 숨김(CSS)/주석처리/비활성 상태면 우리도 만들되 동일하게 숨김/주석/비활성으로 둔다 — 활성·비활성 상태까지 parity.


**기준점(2026-09-22, 사용자 지시)**: AS-IS 화면단/서버단에 기능이 존재하면 MSA도 **동일하게 전부 구현**한다. 그러나 AS-IS 소스에서 그 기능이 **CSS 숨김·주석처리·비활성** 상태로 두어져 있으면, MSA도 **기능은 만들어 놓되 동일하게 숨김/주석/비활성** 상태로 둔다.

**Why:** 전환사업은 AS-IS 정본 재현이 목표([[asis-porting-principles]], [[asis-porting-principles]]). 기능을 빼버리면 재현 누락이고, 임의로 켜면 근거 없는 신규 활성화다. AS-IS 운영자가 의도적으로 꺼둔 상태(숨김/주석)까지 그대로 옮겨야 "동일"이 성립한다.

**How to apply:**
- 갭목록/인벤토리에서 "미사용/죽은코드"로 보여도, 화면·서비스 마크업에 존재하면 **구현 대상**이다. 단 노출/활성 여부는 AS-IS 상태를 따른다.
- 예: 답례품 옵션형태 라디오 — AS-IS 판매자 등록폼에서 S(선택형)·S3(3조합형)는 노출, **S2(2조합형)·T(텍스트형)는 `class="hidden"`** → MSA도 S2·T를 만들되 판매자 화면에서 숨김([[gift-option-parity-audit]] §0-1).
- AS-IS 코드가 `<%-- --%>`/`/* */`/주석블록으로 꺼져 있으면 MSA도 해당 부분을 주석/비활성으로 남긴다(삭제·활성화 금지).
- 애매하면([[asis-porting-principles]]) 사용자 확인.

## as-is-logic-is-the-spec

> 상세분석·구현의 정본은 AS-IS 서비스로직이다 - 화면 단위가 아니라 컨트롤러/서비스 메서드/매퍼 쿼리 단위로 대조하고, 프론트는 정적요소·이벤트·디자인까지 동일하게 재현한다 (2026-09-10 사용자 지시)


**요구 수준: "AS-IS의 상세한 서비스로직을 그대로 MSA에 구현한다."** 속도보다 정확도가 우선이고, 느려져도 자세히 분석해서 구현한다(2026-09-10 사용자 명시).

- **판단의 정본(canonical source)은 AS-IS 서비스로직**이다. RFP/ISP는 그 위에 얹는 **신규/변경 건**에만 적용한다([[asis-porting-principles]]).
- **프론트도 동일 재현 대상**이다 - 레이아웃뿐 아니라 **정적요소, 이벤트 핸들러, 디자인**까지 같게 보이고 같게 동작해야 한다.

**Why:** 화면 단위로만 훑으면 화면 뒤에 있는 서비스 로직이 통째로 빠진다. 실제 사례(2026-09-10) - 답례품 상세 "기부하기"는 화면상 링크 하나로 보였지만 AS-IS는 클릭 시 `getLocGovLmtt`로 **지자체 기부제한**을 조회해 기간 중이면 이동을 막는다. 화면 대조로는 절대 안 잡히고, AS-IS 컨트롤러/서비스/매퍼를 봐야 나온다. 같은 날 발견한 완료 알림 30여 건, `site-footer` 누락, 별점 ClassCastException도 전부 같은 유형이다.

**How to apply:** 도메인을 분석할 때 화면 목록이 아니라 **AS-IS의 (1) 컨트롤러 엔드포인트, (2) 서비스 메서드, (3) 매퍼 쿼리**를 목록으로 뽑아 MSA 대응을 1:1로 매핑한다. 대응이 없으면 "재현 누락 / 의도적 축소 / RFP·ISP 변경분" 중 무엇인지 명시한다. 화면은 **이벤트 핸들러 목록**(@click/@change/onclick, submit 가로채기)까지 대조한다 - AS-IS 버튼 하나에 사전 조회·검증·분기가 붙어 있는 경우가 많다.

**AS-IS 규모 참고(실측 2026-09-10):** Java 2,436개 파일 / 40.7만 줄, 컨트롤러 268개, MyBatis 매퍼 173개 파일 / 쿼리 2,850개(CUBRID), 라이브 화면 약 267개. 현재 MSA는 Java 778개 파일 / 5.6만 줄 - **아직 상당 부분이 미재현일 수 있다는 전제**로 본다.

## as-is-parity-exhaustive-audit-method

> 모든 서비스에서 AS-IS 재현은 \"발견되면 그때\" 방식 금지. 프론트 이벤트+서비스 검증로직+매퍼를 전수 대조해 갭 목록을 먼저 만들고 그 목록 기준으로 작업. ISP/RFP 신규·변경 요건은 반드시 사용자와 먼저 확인


**사용자가 강하게 지시한 표준 작업방식(2026-09-21).** donation에서 중복기부 확인 로직 같은 걸 하나씩 뒤늦게 발견하는 패턴에 대한 강한 불만에서 나옴.

**★제1원칙: AS-IS를 서비스로직도 화면단도 "일단 똑같이" 구현한다.** 임의 축소·단순화·생략 금지. 기본값은 완전 동일 재현이고, 벗어나는 것은 (a) 외부연계 미개방 같은 물리적 차단이나 (b) ISP/RFP 신규·변경 요건일 때뿐이며, 그 경우도 **사용자와 먼저 확인**한다. 과거 "의도적 축소"로 넘긴 것들도 이 원칙으로 재점검 대상.

**진행 순서(2026-09-21 확정): 먼저 5개 서비스(donation·member·point·gift·order) 전부 전수조사해 서비스별 갭 목록(`docs/<service>-parity-audit.md`)을 완성하고, 그 다음에 서비스별로 하나씩 구현한다.** **admin은 나중으로 미룸(이번 라운드 제외).** 즉 "조사·목록화 단계"와 "구현 단계"를 분리 — 목록이 다 나오기 전엔 개별 구현 착수 금지(중복기부처럼 순수재현 확정건도 목록 완성까지 대기). 산출물: `docs/{donation,member,point,gift,order}-parity-audit.md` **5종 완성(2026-09-21)** → 이제 구현 단계 전환 가능(admin 제외).

**규칙(모든 서비스 공통):**
1. **AS-IS 재현은 전수 대조 기반.** 화면 진입/컨트롤러만 훑지 말고, **① 프론트 이벤트(드롭다운 선택·버튼·confirm/alert 등 화면 동작) ② 서비스 검증 로직(모든 if 분기: 한도·중복·주소지제한·기간·본인인증 등) ③ 매퍼/쿼리**를 전부 대조한다.
2. **갭 목록을 먼저 만들고 그 목록 기준으로 작업한다.** "쓰다가 발견되면 그때 고치기" 금지 — 그래야 누락이 없다.
3. **ISP/RFP의 신규 또는 변경 요건이 걸릴 때는 반드시 사용자와 먼저 확인**하고 진행. 임의 구현/판단 금지. (단순 AS-IS 재현은 확인 없이 진행 가능, [[asis-porting-principles]] 정본 기준)

**Why:** 지금까지 컨트롤러·매퍼 위주로만 분석해 프론트 이벤트·서비스 검증 분기를 놓쳤고(예: 중복기부 확인, 답례품 옵션 주문반영, map-select 오판), 그때그때 발견되며 신뢰를 잃고 작업이 늘어졌다. 전수 목록 기반이라야 끝이 보인다.

**How to apply:** 서비스별로 AS-IS 소스(화면 html의 @click/@change 이벤트 + `XxxController` + `XxxService`의 검증 분기 + `xxx-mapper.xml`)를 훑어 "AS-IS 동작 → MSA 재현 여부(O/X/부분)" 표를 만든다. 산출물은 `docs/<service>-parity-audit.md` 형태로 관리. 기존 2026-09-09 분석문서들은 이 깊이(프론트 이벤트+검증 분기)까지는 안 갔으므로 보강 대상. 관련 [[asis-porting-principles]], [[asis-sources-and-data]], [[asis-porting-principles]], [[asis-porting-principles]].

## port-everything-no-omissions-then-rfp-isp

> MSA 전환 기본선: AS-IS front/back/static 전부 누락없이 이식 먼저, RFP/ISP 변경은 그 위에 별도 확인받고 추가


MSA 전환(신규구축 아님)의 작업 순서 원칙 — 사용자가 라운드마다 반복 강조(2026-10-01 재강조).

**원칙**
1. AS-IS의 **기능·화면·정적자산을 100% 그대로 이식**하는 것이 기본선. 깨진/비활성/에러 상태여도 그대로([[asis-porting-principles]], bix5 404 사례).
2. RFP/ISP에 있는 **변경·수정·신규**는 그 위에 얹되, **반드시 별도로 사용자 확인** 후 진행([[asis-porting-principles]], [[asis-porting-principles]]).
3. 따라서 **front / back / static(css·js·이미지·폰트) 어느 것도 누락이 없어야** 한다. "기능만" 보지 말고 **파일·에셋 단위 커버리지**까지 챙길 것.

**Why:** 전환은 기존 자산을 빠짐없이 옮기는 게 성패 기준. 누락은 곧 기능상실. 사용자가 가장 반복 지적하는 지점.

**How to apply:**
- 라운드 착수 전 **AS-IS 원본 트리(컨트롤러·뷰 JSP·static) ↔ TO-BE** 를 파일 단위로 대조한 **커버리지 원장**(present/missing/gap)을 먼저 만들고 그걸로 구동. "발견되면 그때" 금지.
- back(엔드포인트)·front(뷰/템플릿/Vue)·static(참조된 css·js·img·font 실제 복사여부)을 각각 체크. static-asset-scoping-2026-09-18(보관: docs/archive/memory-fix-log-2026-10.md)처럼 누락 이미지 복사 전례 있음.
- AS-IS 소스: `C:\workspace\ghlove`(ghlove-web/common/api/batch/frontend), 코드데이터는 [[asis-sources-and-data]], 인벤토리 산출물 docs/inventory/([[asis-sources-and-data]]).
- admin 메뉴트리 라운드도 이 기준으로: 메뉴뿐 아니라 각 화면의 뷰+static까지 누락 점검.

## scope-migration-not-greenfield

> 이 프로젝트는 신규 개발이 아니라 전환사업 - 구현 범위는 "AS-IS 로직 + 제안요청서/ISP에 명시된 신규기능"뿐이고, 근거 없는 임의 추가는 하지 않는다 (2026-09-10 사용자 지시)


**ghlove-msa는 신규 개발사업이 아니라 AS-IS(`C:\workspace\ghlove`) 전환사업이다.** 따라서 구현 범위는 딱 두 가지다.

1. **AS-IS에 있는 로직·화면의 재현**
2. **제안요청서(`docs/requirements.md`) 또는 ISP(`docs/isp-detailed-design-summary.md`)에 명시된 신규기능**

**둘 중 어디에도 근거가 없으면 만들지 않는다.** "있으면 좋을 것 같아서", "UX상 자연스러워서" 붙이는 기능은 범위 밖이다.

**Why:** 전환사업의 완료 기준은 "AS-IS와 같게 동작하는가 + 발주처가 요구한 신규기능이 들어갔는가"다. 근거 없는 추가는 검수 대상도 아니면서 유지보수 부담과 AS-IS 대조 노이즈만 늘린다. 실제로 2026-09-10에 헤더 장바구니 개수 배지를 만들었다가 **AS-IS 라이브 레이아웃에 없고 RFP/ISP에도 없다**는 이유로 걷어냈다(AS-IS의 구버전 `header.vue`/`footer.vue`에만 남은 잔재를 라이브 기능으로 오독한 것이 발단이었다 - [[asis-porting-principles]]).

**How to apply:** 기능을 추가하기 전에 근거를 먼저 댄다 - AS-IS 파일·줄번호이거나, RFP의 SFR 항목이거나, ISP 페이지다. 근거를 못 대면 만들지 말고 "AS-IS에도 RFP/ISP에도 없다"고 보고한 뒤 사용자 판단을 받는다. 사용자가 요청한 기능이라도 근거가 없으면 **그 사실을 먼저 알리고** 진행한다(나중에 걷어내는 것보다 싸다). 분석·판단 기준 자체는 [[asis-porting-principles]] 참고.

## check-as-is-source-when-analyzing

> 상세분석 시 기능은 AS-IS+제안요청서(RFP)+ISP요약을 종합 판단하고, 레이아웃은 AS-IS를 기준으로 검토한다 (2026-09-09 사용자 지시, 절대 원칙)


**상세분석을 요청받으면 반드시 이 기준으로 진행한다.** 2026-09-09 사용자가 "절대 잊지 마"라고 못박은 원칙이다.

- **기능적 측면** = AS-IS 소스 + 제안요청서(RFP) + ISP 요약, **셋을 종합**해서 판단한다. 하나만 보고 결론내지 않는다.
- **레이아웃/화면** = **AS-IS를 기준**으로 검토한다. 마크업 구조·클래스명·CSS·열 구성 모두 AS-IS 화면이 정답이다.

**참조할 원본 (실측 확인, 2026-09-09):**
- AS-IS 소스: `C:\workspace\ghlove`
  - `ghlove-api/src/main/java/saleson/api/**` REST 컨트롤러
  - `ghlove-common/` 도메인·서비스·MyBatis 매퍼, SQL은 `src/main/resources/sqlmapper/cubrid/*.xml` (CUBRID)
  - `ghlove-frontend/` Nuxt/Vue 화면. 마이페이지 `mypage/*.html`, 회원 `users/*.html`, 레이아웃 `components/layouts/*.vue`, **CSS `static/css/*.css`**
  - `ghlove-web/`, `ghlove-batch/`, 외부연계 `onepass/ payment/ simpleauth/ magicline/`
- 제안요청서(RFP): `docs/requirements.md` — SFR-001~014 기능요구사항 정리본
- ISP: `docs/isp-detailed-design-summary.md` — 상세설계(6차) 691p PDF 요약. 원본 페이지 번호가 `(원본 p.NNN)`로 달려 있어 필요하면 pdftoppm으로 해당 페이지만 다시 렌더링해 확인 가능.

**Why:** MSA 코드와 주석만 보고 분석하면 (1) 재현이 누락·왜곡된 부분을 "원래 그런 것"으로 오판하고, (2) MSA 주석의 "AS-IS는 이렇다"는 서술 자체가 틀렸는지 검증할 수 없다. 실제로 이 방식으로 여러 결함을 찾았다 - 화면 CSS `/static/` 경로 버그, GNB의 취소반품교환 링크 오류, 국민비서 수신동의가 통째로 disabled로 막혀 있던 재현 누락, `mypage-order.css` 미포함으로 주문목록 표가 맨몸으로 나오던 문제.

**흐름(플로우)은 백엔드 기능이 아니라 프론트 흐름까지 추적해야 한다 (2026-09-17 반복지적).** 사용자가 "여러 번" 요구한 핵심. 백엔드 능력만 재현하고 진입점·UX를 지어내면 안 된다. **사용자 상호작용 흐름은 AS-IS 프론트 `ghlove-frontend/modules/op.saleson.js`(로그인·API 호출 허브) + 컨트롤러 + 서비스를 함께 읽어 "언제/어디서/어떻게 트리거되는지"를 그대로 옮긴다.** 실패 사례(휴면해제): AS-IS는 로그인 중 서버가 `SLEEP_USER` 코드를 내려주면 `op.saleson.js:1169`가 그 자리에서 `confirm("휴면해제 하시겠습니까?")`→`/api/auth/recovery`(자격증명 재입력 없음)로 처리하는데, MSA는 능력(reactivate)만 재현하고 **로그인 링크+아이디/비번 재입력 폼이라는 없는 진입점을 날조**했다(2026-09-17 AS-IS 대조로 바로잡음: 로그인 응답 SLEEP_USER→인라인 confirm→recovery). `users/old/*`처럼 `old/` 폴더나 주석처리된 코드는 라이브가 아니니 실제 활성 경로를 따라간다.

**How to apply:** 대응하는 AS-IS 파일을 먼저 찾아 읽고 MSA 구현과 차이를 짚는다. **화면이면 AS-IS가 링크하는 CSS 목록까지 대조한다** - 클래스명만 베끼고 CSS를 안 들여온 사례가 반복됐다. MSA 주석의 "AS-IS는 …" 서술은 근거로 인용하기 전에 원본으로 확인한다. 차이를 보고할 때는 **"의도적 축소(외부연계 미개방 등)" / "재현 누락" / "버그"를 구분**해서 쓴다 - 이 프로젝트엔 의도적 축소가 많다([[thymeleaf-duplicate-cleanup-deferred]], [[domain-code-facts]], [[deferred-features]]). 화면 문제는 코드만 읽지 말고 실제로 띄워서 확인한다.

## defer-saleson-dependent-unused-features

> AS-IS 기준 동일 구현하되, SalesOn(원제품)에 종속된 기능 중 현재 미사용으로 판단되는 것은 기록만 하고 구현은 보류. DA 설계안 확정 후 재개.


**사용자 기준(2026-09-22)**: 지금 기준으로 AS-IS 확인하고 동일하게 구현하되, **SalesOn(원제품/saleson 패키지)에 종속된 기능 중 현재 미사용이라 판단되는 것은 기록해놓고 구현은 나중으로 미룬다**.

**Why:** DA(데이터아키텍트)쪽에서 설계안이 어떻게 나올지 모르기 때문. SalesOn 종속 미사용 기능을 지금 재현하면 나중에 DA 설계와 어긋나 헛일이 될 수 있다. [[saleson-original-product-leftovers]]가 죽은코드 판정의 최대 변수라는 것과 연결됨.

**How to apply:**
- 갭목록 뽑을 때 각 기능이 (a) 고향사랑 답례품 실사용인지, (b) SalesOn 종속·현재 미사용인지 판정한다.
- (b)로 판정되고 **아직 미구현**이면: **docs 갭목록/메모리에 기록만** 하고 구현은 보류(TODO). 억지로 재현하지 않는다.
- **(b)여도 이미 구현되어 있는 건 그대로 둔다(제거·롤백하지 않음)**. 지우지 말고 유지.
- 판정 근거: MSA DB 데이터 0건 + 화면 숨김/죽은코드 + saleson 패키지 종속 등.
- 이건 [[asis-porting-principles]](비활성이면 숨김 유지)와 다르다: 저건 "만들되 숨김", 이건 "SalesOn 종속 미사용이면 아예 구현 보류·기록".
- 예: 카테고리 op_category 4단 트리(category_class1~4)+op_item_category — MSA DB 0건, SalesOn 종속 → 보류+기록. 답례품 실사용 카테고리는 GIFT_CATEGORY 대분류 + gift_subcategory 3단 메가메뉴(이미 구현).

관련 [[asis-porting-principles]] [[asis-porting-principles]] [[continue-vs-rebuild-msa-decision]].

## verify-menu-location-before-building

> 화면/기능을 포팅하기 전에 AS-IS asis_dump.op_menu에서 진짜 메뉴 위치와 display_flag/status_code를 먼저 확인할 것 - 안 하면 발명된 네비게이션을 만들게 된다


**사고 경위(2026-10-08)**: 포인트사용 정합성검증(7210) 화면이 보는 "AB" 코드 주문을 추적해
AS-IS `OrderAdminServiceImpl`(관리자 주문 수기/엑셀 등록)을 찾았다. 이 기능을 TO-BE에
포팅하면서 **AS-IS 메뉴 구조를 한 번도 확인하지 않고** 바로 자바 소스만 보고 구현한 뒤,
주문목록 화면에 "관리자 주문 등록"이라는 링크를 **발명**해서 추가했다. 사용자가 "AS-IS
메뉴 위치가 어디야? 답례품관리>주문관리>오프라인주문관리에 그 기능이 있다는거 맞아?"라고
묻자 그제서야 확인했는데 - 완전히 다른 기능(오프라인 주문관리=결제수단별 조회화면)과
혼동하고 있었다. `asis_dump.op_menu`(AS-IS 원본, remap 전) 직접 조회로 진짜 위치가 다른
메뉴(3000>3700>3701)이고 **그 가지 전체가 AS-IS 자체에서 비활성**(display_flag='N',
최상위는 status_code='2')이라는 것까지 그제서야 알았다. 결국 두 가지를 임의로 한 것:
① 범위를 혼자 판단해서 줄였다 ② 없는 네비게이션을 발명했다 - 둘 다 사용자 확인 없이.

**교훈**: 화면/기능 이름(JSP·컨트롤러·서비스 클래스명)이 비슷해 보여도 **실제로 같은 메뉴를
가리키는지는 메뉴 테이블로 확인해야 안다.** 특히 "오프라인"·"수기"·"대량" 같은 비슷한
단어가 들어간 여러 기능이 공존할 수 있다(이번에도 오프라인 주문관리/수기결제/대량주문
관리가 전부 다른 기능이었다).

**How to apply**:
1. 어떤 AS-IS 기능을 포팅하기 전에, 그 컨트롤러 URL로 **`asis_dump.op_menu`를 먼저 조회**해서
   진짜 `menu_id`/`menu_parent_id`/`menu_name`/`display_flag`/`status_code`를 확인한다
   (`admin.op_menu`는 TO-BE가 remap한 값일 수 있어 원본과 다를 수 있다 -
   migration-admin-menu-asis-reseed.sql 주석 참고: "menu_url만 TO-BE 컨트롤러로 remap").
2. **상위 체인 전부**(parent의 parent까지)의 display_flag/status_code를 확인한다 - 자기
   자신은 'Y'인데 조상이 'N'이면 실제로는 숨겨진 것이다(2026-10-02 전수동기화가 "조상체인
   전부 display=Y인 가시 트리"만 뽑은 이유).
3. 비활성이면 [[asis-porting-principles]]대로 **기능은 만들되 노출은 AS-IS처럼
   숨긴다** - 네비게이션 링크·메뉴 등록을 자체적으로 만들지 않는다. 화면이 실존해야 하는
   이유(이번처럼 다른 화면이 그 데이터를 참조하는 경우 등)가 있어도 "숨겨서 존재"와
   "눌러서 들어가지는 것"은 다르다.
4. 비슷한 이름의 다른 메뉴가 있으면(오프라인/수기/대량 등) 반드시 URL과 컨트롤러 메서드까지
   대조해서 혼동하지 않는다.

## verify-screen-by-content-not-route

> AS-IS 화면의 MSA 대응물을 찾을 때 라우트·파일명이 아니라 화면 제목과 본문 마크업으로 대조할 것 - 경로명이 다른 화면이 있다 (2026-09-09 오판 사례)


AS-IS 화면이 MSA에 있는지 판단할 때 **라우트 목록이나 템플릿 파일명으로 결론내지 말고, 화면 제목과 본문 마크업으로 대조**한다. MSA에는 AS-IS와 경로명을 다르게 쓴 화면이 있다.

**실제 오판 사례 (2026-09-09, donation 분석):** AS-IS `donation/guide3.html`(연말정산 세액공제 안내)이 MSA에 없다고 보고했다. 근거는 `GuideController`의 라우트가 `/guide1 /guide2 /guide5 /guide6`뿐이고 `templates/guide/`에 guide3.html이 없다는 것이었다. **틀렸다** - `donation/honor.html` + `HonorBenefitController.guide()`가 guide3 재현본이었고, 화면 제목("안내사항 > 연말정산 세액공제 안내")과 AS-IS의 `give-table` PC/모바일 2벌 표까지 그대로 있었다. **컨트롤러 주석에 "AS-IS donation/guide3.html"이라고 명시까지 돼 있었는데 못 봤다.** storefront SPA도 같은 내용을 `TaxCreditGuideView.vue`(라우트 `/honor`)로 갖고 있었다.

그 결과 없는 화면을 새로 만들고 5개 모듈의 GNB 링크까지 바꿨다가 전부 되돌렸다(중복이었고 GNB 링크는 원래가 맞았다).

**Why:** 라우트·파일명은 MSA가 자체 판단으로 바꾼 부분이라 AS-IS와 1:1이 아니다. 이름만 보고 판단하면 (1) 있는 걸 없다고 오판해 중복 구현하고, (2) 그 오판을 근거로 멀쩡한 링크를 망가뜨린다.

**How to apply:** "AS-IS X가 MSA에 없다"를 보고하기 전에 최소 두 가지를 확인한다 - (1) AS-IS 화면의 **제목 문구**로 MSA 템플릿 전체를 grep, (2) 그 기능의 **특징적 클래스/표 구조**(여기서는 `give-table`)로 grep. 컨트롤러/템플릿 주석의 "AS-IS ...html" 표기도 같이 훑는다. 조치 규모를 추정할 때도 "기존 컴포넌트 재사용이라 작다"는 식의 낙관을 검증 없이 쓰지 않는다 - 같은 분석에서 지도 선택(map-select) 작업량도 과소평가했다. 분석 기준 자체는 [[asis-porting-principles]] 참고.

**두 번째 사례 (2026-09-10, donation 기부완료 모달):** 화면 대응표를 만들 때 축소 판정을 `popup-success.html`·`giro-success.html` 같은 **별도 파일 단위로만** 훑어서, AS-IS `donation-main.html` **안에 인라인된** 기부완료 모달(`#donation-c` — 기부 요약표 + "답례품 몰 바로가기")을 통째로 놓쳤다. 사용자가 기능 테스트 중 "기부하면 기부내역 화면으로만 가고 답례품 화면으로 안 넘어간다"고 지적해서 드러났다. 즉 **AS-IS 화면 하나가 파일 하나가 아니다** - 모달·팝업·탭 같은 인라인 영역이 별개 화면 역할을 한다. 대응표를 만들 때 파일 목록을 훑는 것만으로는 부족하고, AS-IS 화면 안의 모달/오버레이 블록(`black-bg`, `overlayer`, `popup` 계열)도 같이 세야 한다. 덧붙여 **링크되지 않은 CSS가 그 단서**였다 - AS-IS가 링크하는 `joind-agf.css`가 MSA 모듈에 파일로만 있고 아무 데서도 링크되지 않았는데, 그게 정확히 이 모달용 CSS였다.

## report-structural-landmines-to-user

> 같은 뿌리에서 또 터질 구조적 결함을 발견하면 조용히 우회하지 말고 사용자에게 알린다 - 범위·선택지·작업량을 재서 보고


**사용자 지시(2026-10-07): "앞으로 이런 상황 또 나오면 알려줘."**

"이런 상황" = 눈앞의 증상을 고치는 것만으로는 **같은 뿌리에서 또 터질** 구조적 결함.
2026-10-07 공통코드 표 두 갈래(admin-common-code-two-tables(보관: docs/archive/memory-fix-log-2026-10.md))가 그 사례다.

**Why:** 사용자가 범위와 작업량을 알아야 "지금 뚫고 갈지, 근본을 고칠지"를 결정할 수 있다.
내가 임의로 넓게 고치면 검증 부담이 커지고, 임의로 좁게 때우면 같은 사고가 반복된다.
실제로 공통코드 건은 이미 두 번 사고가 난 뒤에야 드러났다.

**How to apply:** 발견하면 고치기 전에 이렇게 보고한다.
1. **증상과 진짜 원인**을 분리해서 적는다(증상만 말하지 않는다).
2. **범위를 수치로 잰다** - 같은 패턴이 몇 곳인지, 영향받는 호출/파일이 몇 개인지.
   (예: 맹목 캐스팅 14곳/5파일, `labelsOf` 호출 53곳/31파일)
3. **좁은 수정 vs 근본 수정** 두 선택지와 각각의 작업량·위험을 제시하고 **내 추천을 밝힌다.**
4. 사용자가 좁은 쪽을 고르면 **왜 그렇게 뒀는지 코드 주석·마이그레이션 주석에 남긴다**
   (다음 세션이 같은 함정에 빠지지 않도록).

틀린 단정이 주석으로 남아 있으면 **그것도 정정한다** - 공통코드 건은 "없는 표"라는 오판 주석이
원인 추적을 한참 돌아가게 만들었다.
