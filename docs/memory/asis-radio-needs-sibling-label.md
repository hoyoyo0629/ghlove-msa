---
name: asis-radio-needs-sibling-label
description: AS-IS form:radiobutton은 input+형제 label(for) 구조라, label로 감싸면 .input-form flex가 죽어 CSS가 틀어진다
metadata:
  type: project
---

AS-IS 운영관리 화면의 라디오/체크박스는 Spring `form:radiobutton` 이 만들어낸
**`<input id="xxx1">` + 형제 `<label for="xxx1">`** 구조다. TO-BE에서 `<label><input>텍스트</label>`
로 감싸 두면 화면이 틀어진다.

**Why:** `opmanager.css:2496` 의
`.admin_wrap .input-form { display:flex; gap:5px; align-items:center }` 는 input과 label이
**각각 flex 아이템일 때만** 간격·수직정렬이 먹는다. label로 감싸면 자식이 label 하나뿐이라 gap이
사라지고, `opmanager.css:414` 의 `input[type=radio] { margin-right:-5px !important }` 때문에
글자가 라디오에 달라붙는다. AS-IS CSS를 그대로 복사해 썼는데도 안 나오는 전형적인 마크업 불일치다
([[copy-asis-css-js-assets-verbatim]]).

**How to apply:** `.input-form` 안의 라디오는 반드시 `<input id><label for>` 형제 구조로 쓴다.
검증은 눈이 아니라 실측으로 — AS-IS와 TO-BE에서 `gapPx`(label.left - input.right)와 중심선 Y를
같은 스크립트로 찍어 비교한다. 정상값은 **gapPx 0**(margin -5px가 gap 5px를 상쇄), input/label 중심선 일치.

2026-10-08 특정사업 지자체별통계·월별통계 2화면 수정 완료. **남은 곳 2개**:
`admin/.../templates/access/form.html`, `admin/.../templates/featured-admin/list.html`
(화면이 달라 각각 AS-IS 확인 후 처리).
