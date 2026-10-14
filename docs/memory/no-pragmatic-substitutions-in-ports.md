---
name: no-pragmatic-substitutions-in-ports
description: "화면 이식 때 '자산이 없으니 대체/생략'·'비슷한 컬럼으로 추정' 금지 - 같은 메커니즘 그대로, 숨김요소·오타까지 (15102에서 반복 지적)"
metadata:
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T01:34:39.295Z
---

AS-IS 기능의 뒷받침(파일·데이터·외부연계)이 TO-BE에 없어도 **같은 메커니즘으로 똑같이 만든다**. 다른 걸로 대체하거나
(예: "지정기부목록 다운로드"를 storefront 목록 링크로), 생략하거나(예: PDF 파일이 없다고 "기탁서 양식 및 약관 출력" 버튼 제거),
"도달 불가해 보인다"며 숨김 요소를 빼는 것 전부 금지. 없는 자산·데이터는 구현은 동일하게 해 두고 **보고만** 한다.

**Why:** 2026-10-13 기탁서 등록(15102)에서 "야..왜 또 니가 판단하고 발명하냐? as-is랑 동일하게 구현하라는 원칙을 또
잊은거야?" — 상태문구/안내문 발명, 버튼 대체·생략, 숨김요소 생략, CSS 클래스 대신 인라인 width를 지적받았다.
"as-is에 다운로드 파일이 없어도 기능은 동일하게 만들어야 할거아냐."

**How to apply** (화면 이식 체크리스트, [[asis-screen-port-procedure]]·[[copy-as-is-verbatim-never-invent]]의 보강):
- 마크업은 JSP를 줄 단위로 옮긴다: 클래스·title·name·colgroup/colspan/rowspan·`&nbsp;`·`<font>`까지. AS-IS 오타도
  화면에 영향 있으면 그대로(`btn-defualt`, `cursor: ponter`). 숨김(`display:none`) 요소와 그 JS 로직도 포함.
- 셀렉트 옵션은 하드코딩 말고 AS-IS가 쓰는 공통코드 그대로(PHONE=010/011/017/019, WDR=시도 17개), 문구는
  메시지 카탈로그(`${msg.get('M00159')}`)·공통코드 detail(한도 "2천만").
- 데이터 출처는 이름이 비슷한 컬럼으로 추정하지 말고 **매퍼 SQL로 확인**: 대표답례품은 `OP_ITEM.REPRESENTATIVE_ITEM_YN`이
  아니라 `G_LCLGV_OFF_RPRS_GDS_MNG`였다.
- 화면이 부르는 엔드포인트는 AS-IS 경로에서 `/opmanager`만 뗀 이름, 응답은 `isSuccess/data/errorMessage` 모양 →
  JS를 원문 그대로 옮길 수 있다. 호출처가 0건인 함수(죽은 코드)만 빼고, 뺀 이유를 주석으로 남긴다.
