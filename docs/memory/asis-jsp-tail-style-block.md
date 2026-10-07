---
name: asis-jsp-tail-style-block
description: "AS-IS JSP 끝의 페이지 전용 <style> 블록을 빠뜨리면 공용 CSS가 화면을 깨뜨린다 - 이식 시 JSP를 끝까지 읽을 것. 2026-10-07 전수 점검 완료(실결함 2건)"
metadata:
  type: project
---

AS-IS opmanager JSP는 **파일 맨 끝(</script> 다음)에 페이지 전용 `<style>` 블록**을 두는 경우가 많다
(실측 **116개 화면**). 본문·JS만 옮기고 이 꼬리를 놓치면, 공용 `opmanager.css`가 그 화면에서
의도와 다르게 먹는다. 공용 CSS를 verbatim 복사해도 **페이지 오버라이드까지 옮겨야** parity다.

## 2026-10-07 전수 점검 결과 (사용자 지시 B안)

AS-IS 116개 화면에서 선택자 **342개**를 뽑아 TO-BE 템플릿+정적 CSS와 대조.
그중 **마크업이 실제로 그 클래스를 쓰는데 규칙이 없는 것**만 진짜 결함으로 판정했다.

**실결함 2건 - 둘 다 고침:**

1. `community/faqBbs/list.jsp` → `templates/community/faqBbs/list.html`
   `.admin_wrap .tabs li.active { border-bottom: 1px solid #999; }`
   공용 CSS는 활성 탭 아래 테두리를 **흰색으로 뚫는다**(탭 한 줄 전제). 담당자용 FAQ는 탭이
   두 줄이라 선택 시 밑줄이 끊겼다. AS-IS도 같은 이유로 덮어쓴 것.
2. `user/login_main.jsp` → `templates/admin/login.html`
   `.disabled-div { pointer-events: none; opacity: 0.6; }`
   **기능 결함이었다.** JS가 이메일 인증 단계에서 앞 단계에 이 클래스를 붙여 잠그는데
   규칙이 없어 **클래스만 붙고 계속 클릭·수정이 됐다.**

**오탐으로 제외한 것(기록해 둠 - 다시 조사하지 말 것):**
- `.sortable-placeholder td`(designated-donation 4화면) - **AS-IS도 `.sortable()` 호출이 0건**인
  죽은 스타일. TO-BE는 노출순서를 입력칸+[순서변경] 일괄저장으로 구현했고 그게 AS-IS 동작이다.
- `.board_write_table .label p`(access/write.jsp) - AS-IS `.label` 안은 `<p>`가 아니라
  `<span class="required_mark">`다. AS-IS에서도 안 먹는 규칙.
- `.offCharge`(user/pki/form.jsp) - TO-BE의 `offChargerPasswordInit`(팝업명) 오탐. 클래스 사용 없음.
- order·item·shop-statistics·catalog·seller 등 **미이식/보류 영역**은 범위 밖.

## 다음에 화면을 이식할 때

**JSP는 끝까지 읽는다.** `</script>` 뒤에 `<style>`이 더 있는지 확인하고, 있으면 함께 옮긴다.
옮긴 뒤에는 **왜 필요한지 주석을 단다** - 공용 CSS와 중복돼 보여 나중에 지워지기 쉽다.
판정 기준: *마크업이 그 클래스를 쓰는가*. 쓰지 않으면 스타일 누락이 아니라 기능 미이식이다.

관련: [[asis-screen-port-procedure]] · [[copy-asis-css-js-assets-verbatim]] · [[report-structural-landmines-to-user]]
