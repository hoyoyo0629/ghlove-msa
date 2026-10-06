---
name: commit-only-when-asked
description: User handles git commits themselves; do not commit or ask about committing unless explicitly requested
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 5f9abc33-96d2-44ea-be30-0f402bad6590
  modified: 2026-09-08T05:42:25.012Z
---

사용자가 git 커밋을 직접 관리한다. 별도로 요청하지 않는 한 커밋하지 말고, 커밋 여부를 묻지도 말 것(작업 끝에 "커밋할까요?"류 확인 금지).

**Why:** 2026-09-08, 기능검증·수정 작업 후 매번 "커밋할까요?"라고 물었더니, 커밋은 본인이 직접 하니 앞으로 따로 부탁하지 않으면 확인하지 말라고 명시적으로 지시함.

**How to apply:** 코드 수정을 마치면 변경 요약만 보고하고 끝낸다. "커밋을 진행할까요?" 같은 문구를 붙이지 않는다. 사용자가 명시적으로 커밋을 요청할 때만 git commit을 수행한다.
