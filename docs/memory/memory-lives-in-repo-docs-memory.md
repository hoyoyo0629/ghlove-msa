---
name: memory-lives-in-repo-docs-memory
description: "세션 메모리의 실제 저장소는 C:\\workspace\\ghlove-msa\\docs\\memory 이고 원래 .claude 경로는 거기로 가는 정션이다. 2026-10-20부터 주제별 파일 관리(완료 수정은 메모리 금지, 같은 주제는 기존 파일 수정)"
metadata:
  node_type: memory
  type: project
---

**2026-10-06, 사용자 요청으로 세션 메모리를 저장소 안으로 옮겼다.** 실제 파일 위치는
`C:\workspace\ghlove-msa\docs\memory\` 이고, Claude Code가 보는 원래 경로
`C:\Users\<계정>\.claude\projects\c--workspace-ghlove-msa\memory` 는 그 폴더를 가리키는
**디렉터리 정션**이다. 어느 쪽 경로로 써도 파일은 한 벌만 생긴다.

**Why:** 사용자가 "MEMORY.md도 저장소에서 같이 관리하고 싶다"고 했다. 복사해서 동기화하는
방식은 drift가 생기므로, 방향을 뒤집어 **실제 저장소를 repo 안에 두고 원래 경로를 링크**로
만들었다. 이러면 메모리를 쓸 때마다 자동으로 저장소에 들어가 사용자가 커밋할 수 있다.
Claude Code가 메모리를 찾는 경로는 시스템 설정에 박혀 있어 바꿀 수 없으므로 정션이 필수다.

**How to apply:**
- 메모리를 읽고 쓸 때는 `docs/memory/` 경로를 쓴다(원래 경로도 같은 곳이지만 혼동을 줄인다).
- **[2026-10-20 사용자 결정으로 규칙 변경] 주제별 파일로 관리한다.** 하루 10~20개씩 늘던 메모리
  158개를 35개로 통폐합했다(원문 보존: 주제 파일 9개로 합침, 영역 원장 23개는 `docs/as-is-parity.md`
  22절로, 완료된 수정 일지 25개는 `docs/archive/memory-fix-log-2026-10.md`로).
  - **완료된 수정·데이터 적재는 메모리에 남기지 않는다** - git 커밋에 있다. 다시 쓸 교훈이 있으면
    해당 함정 파일(`admin-screen-pitfalls`·`backend-db-pitfalls` 등)에 절을 하나 덧붙인다.
  - **같은 주제는 기존 파일을 고친다.** 새 파일은 기존 어느 파일에도 맞지 않는 새 주제일 때만.
    영역 이식 진척은 `docs/as-is-parity.md` 22절 + `admin-port-progress` 한 줄.
  - "재기동 필요" 같은 일회성 안내는 메모리가 아니라 그때 답변으로만 알린다.
  - `MEMORY.md`는 매 세션 통째로 읽히는 색인 - 파일당 한 줄, 짧게. 주제 파일의 `description`에
    안에 든 키워드를 나열해 관련성 판정이 되게 한다.
- `docs/` 최상위 문서는 6개 고정(`docs/README.md`). 새 원장도 새 파일이 아니라 기존 문서에 절로 추가한다.
- 폴더를 옮기거나 지우면 메모리가 끊긴다. 정션 재생성 방법은 `docs/README.md`에 적어뒀다.
- 메모리가 저장소에 들어가므로 **git 이력·원격에 올라간다**(비밀값을 메모리에 쓰지 말 것).
