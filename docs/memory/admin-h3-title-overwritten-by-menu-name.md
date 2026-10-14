---
name: admin-h3-title-overwritten-by-menu-name
description: 운영관리 화면의 h3 제목은 op.manager.js가 3단 메뉴명으로 덮어쓴다 - 템플릿에 뭘 적든 화면엔 메뉴명이 보인다(.custom 클래스만 예외)
metadata:
  type: project
---

`static/content/modules/op.manager.js:46`:

```js
$('.contents h3').eq(0).not('.custom').find('span').text(thirdMenuName.replace('- ', ''));
```

**첫 번째 `.contents h3`의 `<span>` 텍스트를 LNB 3단 메뉴명으로 덮어쓴다.** AS-IS에서 그대로
복사해 온 자산이라 AS-IS도 같은 동작이고, 그래서 AS-IS JSP 상당수가 `<h3><span></span></h3>`처럼
**빈 span**을 둔다. 덮어쓰기를 피하려면 h3에 `class="custom"`을 준다.

**Why:** 이걸 모르면 화면 이식할 때 두 가지로 헛짚는다.
- JSP의 h3에 적힌 문구(자료실 list.jsp는 `${op:message('MENU_5110')}` = "기금사업소개 및
  공지사항", form.jsp는 "글쓰기")를 "AS-IS 화면에 그렇게 보이는 복사 흔적"으로 오해한다.
  실제로는 JS가 메뉴명("자료실")으로 덮어써서 **화면 결과는 멀쩡하다**. 2026-10-12 자료실
  이식 중 Playwright 검증이 FAIL로 잡아내 확인했다.
- 반대로 h3가 비어 있는 AS-IS JSP(지자체공지사항 등)를 보고 "제목이 없는 화면"으로 오해한다.

**How to apply:** 화면 이식 시 h3 span에는 적당한 기본값(메뉴명이나 AS-IS 원문)을 넣어 두고,
**화면에 실제로 보이는 제목은 `op_menu`의 3단 메뉴명이라는 전제로 검증**한다. 제목이 메뉴명과
달라야 하는 화면이라면 그때만 `class="custom"`을 쓴다(AS-IS에 그런 화면이 있는지 먼저 확인).
Playwright로 제목을 단언할 때 기대값은 **메뉴명**이다.
