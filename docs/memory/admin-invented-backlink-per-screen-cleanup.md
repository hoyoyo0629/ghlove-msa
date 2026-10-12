---
name: admin-invented-backlink-per-screen-cleanup
description: AS-IS에 0건인 상단 "← …" 링크가 admin 21화면에 있다. 일괄 제거 금지, 화면별로 정리(충돌 위험)
metadata:
  type: project
---

TO-BE admin 템플릿 21곳이 제목 위에 `<p><a>&larr; …</a></p>` 되돌아가기 링크를 갖고 있는데
**AS-IS opmanager view 트리 전체에 이 패턴은 0건**이다(`grep -rn '&larr;\|←' ... --include=*.jsp`).
AS-IS가 목록으로 돌아가는 수단은 화면 하단 `.btn_all` 의 `목록` 버튼이다.

**Why:** 사용자 결정(2026-10-08) - **일괄 제거하지 말고 화면별로** 정리한다. 다른 개발자와 같은
파일을 동시에 고칠 위험이 있어서다. 또 링크만 지우면 하단 목록 버튼이 없는 화면은 화면 안에서
목록으로 갈 길이 사라져, 조치가 화면마다 다르다(삭제만 / 삭제+하단 목록 버튼 추가 / AS-IS 실화면
확인 먼저).

**How to apply:** 해당 영역을 작업할 때 `docs/admin-invented-backlink-cleanup-ledger.md` 의
화면별 "조치" 한 줄만 수행한다. 원장에 AS-IS 짝 JSP·목록 버튼 줄번호·메뉴 등록 여부가 다 들어 있다.
2026-10-08 기준 2화면(특정사업 지자체별통계·월별통계) 처리, 19화면 잔여.

곁가지 발견: `designated/analysis.html`(`/designated-projects/analysis`, "지정기부 모금분석")은
`op_menu`에 없고 AS-IS `designated-donation/analysis/` 에는 `locgov.jsp`·`month.jsp` 둘뿐이다.
**AS-IS에 없는 TO-BE 전용 화면**이라 존치 여부를 물어야 한다([[no-invented-features-ask-first]]).
