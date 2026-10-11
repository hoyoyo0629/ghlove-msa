---
name: asis-screen-port-procedure
description: "admin 화면 이식 고정절차 - AS-IS JSP 마크업 구조 그대로 + opmanager.css에 정의된 클래스만 + 라벨은 op_common_message 코드로 조회. 클래스/문구 발명 금지. 2026-10-02 사용자 지시"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T04:00:38.920Z
---

**사용자 지시(2026-10-02): "① 서비스로직은 AS-IS와 대조해 누락 없이, ② 화면은 AS-IS 정적요소 다 적용해서 레이아웃도 동일하게."** 기존에 만든 화면 대부분이 AS-IS와 다르다는 지적에서 나온 규칙이다(예: 팝업관리 - 검색폼 전체 누락, 8컬럼→6컬럼, `노출중/중지` 발명).

**Why:** `opmanager.css`에 정의가 없는 클래스(`small-btn`, `pill`, `ghost`, `inline-form`, `empty`, `field`)를 발명해서 쓰면 CSS가 안 먹어 레이아웃이 어긋나는 게 당연하다. 실측: opmanager 화면 **215개 중 163개**가 발명 클래스 사용 = 재작업 대상.

**★★사용자 지시(2026-10-06): 기준은 "JSP + 매퍼 SQL + <u>서비스 로직</u> + <u>컨트롤러</u>" 4종 전부,
각각 <u>메서드 단위</u>로. 누락 없게.**

**4번째 축 - 컨트롤러 메서드 전수 대조(2026-10-06 추가).** 화면(JSP) 단위로만 훑으면
**메뉴 화면이 아닌 컨트롤러가 통째로 빠진다.** 실제 사례: 스마트에디터 툴바의 사진·동영상·CTP
버튼이 여는 팝업은 AS-IS `common/module/smarteditor/SmartEditorController`(엔드포인트 5개)인데
TO-BE에 **아예 없어서** 버튼 세 개가 전부 오류였다. 메뉴 트리에 없으니 화면 목록에도 안 잡힌다.
→ 영역 이식 전에 **AS-IS 컨트롤러 메서드 목록을 먼저 뽑아 TO-BE 엔드포인트와 1:1 체크표**를
만든다. `grep -rn "@\(Get\|Post\|Request\)Mapping" <AS-IS 영역>` 로 메서드를 전수 세고,
화면이 참조하는 **공통/모듈 컨트롤러**(에디터·주소검색·파일다운로드·팝업결과)도 같이 센다.

나는 그동안 **JSP와 매퍼 SQL만** 대조하고 `*ServiceImpl` **메서드 본문을 끝까지 읽지 않아서**,
서비스 계층에만 있는 분기를 화면마다 반복해서 놓쳤다. 사용자 지적: "왜 자꾸 이런 문제가 생기는거야?
이런식이면 매번 기능확인하고 as-is 확인하고, 구현하고 반복이잖아. 신뢰도가 엄청 낮네?"
→ **매퍼에 없으면 없는 게 아니다. 서비스에 있다.** 실제로 놓쳤던 것들:
- 업로드 **확장자 화이트리스트**(매퍼엔 흔적 0, `ServiceImpl`의 `AVAILABLE_EXTENSION` 상수에만 있다)
- 저장 시 **조건부 정리**(팝업: 형태≠'3'이면 이미지·이미지링크·배경색을 ""로 비운다)
- 저장 시 **다른 컬럼 비움**(팝업: 이미지 저장하면 `setContent("")`)
- **디스크 파일 삭제**(이미지 교체·삭제·형태 전환 때 `fileStorage.delete` 선행)
- **파생 산출물**(특정사업 이미지는 썸네일 사이즈별로 여러 파일 + 다건 행)
자세한 전수 결과: `docs/upload-file-parity-audit.md`

**How to apply - 화면 1개당 고정절차:**
0. **메뉴 위치 확인(2026-10-08 추가)** - 코드만 보고 바로 들어가지 말고 `asis_dump.op_menu`로
   진짜 menu_id/부모/display_flag/status_code를 먼저 확인한다. 상위 체인이 비활성이면
   기능은 만들되 네비게이션은 발명하지 않는다. 자세한 사고 경위: [[verify-menu-location-before-building]]
1. **원본 확정 3종**: `.../opmanager/i18n/<영역>/<화면>.jsp` + `saleson/shop/<영역>/*ManagerController.java`
   + **`saleson/shop/<영역>/*ServiceImpl.java`** + `sqlmapper/cubrid/<영역>-mapper.xml`
2. **로직**: 컨트롤러 메서드·파라미터명·검증·롤 스코프 분기·매퍼 SQL 컬럼까지 1:1 대조. 누락 0.
   **서비스 메서드는 본문을 처음부터 끝까지 읽는다** - 저장/수정/삭제 각각에 대해
   ①입력 검증(확장자·용량·형식) ②조건부 분기(타입·형태에 따라 다른 컬럼을 비우는지)
   ③파일 I/O(저장 경로·파일명 규칙·파생 파일·기존 파일 삭제) ④부수효과(다른 표 insert/delete,
   문자·메일 발송, 이력 적재)를 체크리스트로 확인한다
3. **마크업**: JSP의 div/table/colgroup/thead 구조·class·id·summary·caption을 **그대로** 옮긴다. `<c:forEach>`→`th:each`, `${}`→`th:text`만 치환
4. **클래스**: `opmanager.css`·`bootstrap.css`에 정의된 것만. 새 클래스 발명 금지 - 필요하면 AS-IS에서 찾아라
5. **문구**: `${op:message('M00730')}` → `${msg.get('M00730')}` ([[asis-message-catalog-loaded]]). 한글 직접 입력 금지
6. **스크립트**: AS-IS `<script>` verbatim (`Common.updateListData`, `Message.get`, `page:pagination-manager` 마크업까지)
7. **검증**: `scratchpad/class_audit.js`로 정의 없는 클래스 0 확인. `opmanager`(html 마커)와 AS-IS가 JS 셀렉터로 쓰는 클래스(`<tr class="content">`)는 정상 예외

**공통 부품(이미 만들어둠 - 매 화면 새로 만들지 말 것):**
- `web/support/FlashRedirect.java` = AS-IS `ViewUtils.redirect(url, message)`. **메시지는 flash scope로
  넘기고 URL에는 붙이지 않는다**(AS-IS 바이트코드 확인: `FlashMapUtils.setMessage`). 화면은
  `${redirectMessage}`로 읽는다. 2026-10-06에 `?errorMessage=`·`?message=`·`?done=`로 쿼리에
  싣던 **컨트롤러 22개/53곳 + 템플릿 36개를 전수 전환**했다 - 새 화면에서 쿼리로 붙이지 말 것
- `web/support/Pagination.java` + `fragments/pagination.html :: manager(${pagination})` = AS-IS `WEB-INF/tags/page/pagination-manager.tag` verbatim. `itemNumber = totalItems - (currentPage-1)*itemsPerPage + 1`(JSP 131곳 전부 `itemNumber - i.count` 형태), 기본 10건/페이지, 번호창 10개 단위. `.pagination`은 opmanager.css:629-645에 정의됨
- `fragments/smarteditor.html :: init` / `:: editor('content')` = AS-IS `tags/modules/smarteditorInit.tag`·`smarteditor.tag` verbatim. 자산은 `/content/modules/smarteditor2_3_10`에 이미 있음
- `${msg.get('코드')}` = `${op:message()}` ([[asis-message-catalog-loaded]])
- 선택삭제는 AS-IS `Common.updateListData(url, Message.get("M00306"))` 그대로 + 서버는 `@RequestParam("id") List<Integer>` 받아 `{isSuccess:true}` 반환

**완료: 팝업관리(1311)** - 검색 5조건·페이징·라벨치환(팝업상태/형태/타입)·선택삭제·이미지삭제 복원, 저장 시 버려지던 startTime/endTime/width/height/top/leftPosition/imageLink/backgroundColor 복구, `POPUP_CLOSE`를 "닫기버튼"으로 오해석해 노출판정을 발명한 `useYn`으로 하던 것 → AS-IS대로 `POPUP_CLOSE='1'`, 발명했던 `/toggle` 제거.

**Thymeleaf 제거는 이 작업보다 뒤다(2026-10-02 판단).** 근거: th: 의존이 얇다(18,017줄 중 30%, 대부분 `th:text`/`href`/`replace`/`if`/`each`; 고유기능은 `#numbers` 185·`#lists` 178·`#strings` 80·`th:field` 15·`th:object` 2). 레이아웃 재작업 산출물(AS-IS와 동일한 HTML + opmanager.css 클래스)은 **엔진 중립**이라 나중에 안 버린다. 반대로 SPA를 먼저 하면 복사해온 jQuery 전역자산(op.common.js/op.manager.js/op.chart.js)이 무효화되고 AS-IS verbatim 전략 자체가 깨진다. 작업 중 "얇게 쓰기"는 적용(`#numbers`/`#lists`는 컨트롤러에서 포맷, `th:field`/`th:object`는 plain `name=`). 관련: [[thymeleaf-duplicate-cleanup-deferred]] [[admin-frontend-is-thymeleaf-not-jsp]]

**작업 순서: 메뉴 영역별로 섞어서**(사용자 선택) - 시스템관리 → 회원관리 → … 순으로 한 영역씩, 신규/기존 구분 없이 그 영역 화면 전부를 AS-IS와 맞춘다. 관련: [[copy-asis-css-js-assets-verbatim]] [[copy-as-is-verbatim-never-invent]] [[admin-shell-asis-parity-restored]] [[menu-visibility-verify-against-live-asis]]
