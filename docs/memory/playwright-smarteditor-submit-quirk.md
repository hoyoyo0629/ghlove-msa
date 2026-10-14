---
name: playwright-smarteditor-submit-quirk
description: Playwright로 SmartEditor2(iframe) 폼을 실제 버튼클릭으로 submit하면 세션을 잃은 것처럼 /admin/login으로 튕긴다 - 이미 배포된 faq-admin/create에서도 재현되는 공통 인프라 현상
metadata:
  type: feedback
---

admin의 `fragments/smarteditor :: init/editor(id)`를 쓰는 등록·수정 화면에서 Playwright로
실제 제출버튼을 클릭하면(iframe 안에 타이핑해 넣었든 아니든) 서버 응답이 `/admin/login`으로
리다이렉트된다. 이미 검증 완료된 `faq-admin/create`로 교차 재현해 확인했다 - **화면별 버그가
아니라 SmartEditor2(2010년대 iframe 기반 에디터) + 레거시 `.validator()` 플러그인 + 헤드리스
Chromium 상호작용에서 생기는 공통 현상**이다.

**Why:** `Common.getEditorContent(id)`는 `editors.getById[id].exec("UPDATE_CONTENTS_FIELD", [])`를
호출하는데, 헤드리스/자동화 환경에서 에디터 인스턴스가 완전히 붙지 않은 채로 제출 이벤트가
발생하면 이 호출이 예외를 던지는 것으로 보인다. 실제 사람이 쓰는 브라우저에서 문제가 되는지는
확인하지 않았다(가능성 낮음 - 운영 화면이라 가정).

**How to apply:** 화면 로직 자체를 검증할 때는 버튼 클릭 대신 폼을 **직접 submit()**한다:
```js
await page.evaluate((content) => {
  document.getElementById('content').value = content;   // 숨은 textarea에 직접 채움
  document.getElementById('notice').submit();            // <form id="notice"> 직접 제출
}, '본문');
```
이렇게 하면 서버 라우팅·검증·저장 로직은 정확히 검증되고(302→200 확인됨), 제출 버튼
클릭이라는 자동화 전용 변수만 피하게 된다. 에디터 자체의 렌더링(툴바·사진/동영상 버튼)은
스크린샷으로 별도 확인하면 된다 - 단, 페이지 로드 직후 스크린샷을 찍으면 iframe이 서버가
내려준 초기 textarea 값을 아직 안 그렸을 수 있다(바인딩 자체는 `page.$eval('#content', el
=> el.value)`로 textarea 원시값을 읽어 확인할 것 - 이건 Thymeleaf 서버 렌더링이라 즉시 정확하다).

부수 발견: 같은 화면에서 `/ban-word/list-all` 404가 콘솔에 찍힌다(금칙어 조회 엔드포인트가
TO-BE에 없음) - smarteditor/validator 공용 자산이 참조하는 것으로 보이며 제출 자체를 막지는
않는다. 전 화면 공통 갭이라 이 메모와 별개로 다룰 것.
