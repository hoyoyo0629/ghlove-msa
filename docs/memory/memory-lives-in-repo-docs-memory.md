---
name: memory-lives-in-repo-docs-memory
description: "세션 메모리의 실제 저장소는 C:\\workspace\\ghlove-msa\\docs\\memory 이고 원래 .claude 경로는 거기로 가는 정션이다. 메모리 파일은 통합 금지(한 사실=한 파일)"
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
- **이 폴더의 .md는 절대 통합하지 않는다.** `docs/` 최상위 문서를 6개로 합친 규칙
  ([[asis-screen-port-procedure]]와 무관한 문서정리 규칙, `docs/README.md` 참고)과 정반대다.
  `MEMORY.md`는 매 세션 통째로 읽히는 색인이라 한 줄씩 짧아야 하고, 나머지는 한 사실당 한
  파일 + frontmatter `description`으로 관련성을 판정한다 - 합치면 recall이 망가진다.
  파일 수가 많은 게 정상이고, 줄일 대상은 틀린 메모리·중복 메모리뿐이다.
- 폴더를 옮기거나 지우면 메모리가 끊긴다. 정션 재생성 방법은 `docs/README.md`에 적어뒀다.
- 메모리가 저장소에 들어가므로 **git 이력·원격에 올라간다**(비밀값을 메모리에 쓰지 말 것).
