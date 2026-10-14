---
name: local-env-changes-never-pushed
description: "내 PC 전용 설정 변경은 절대 푸시하지 않는다. 추적 파일은 skip-worktree, 무시 규칙은 .git/info/exclude. 현재 .vscode/settings.json·.claude/settings.local.json 이 skip-worktree 상태"
metadata:
  type: project
---

이 저장소는 **다른 사람과 공유**한다(origin: github.com/hoyoyo0629/ghlove-msa).
내 PC 환경에 맞추려고 바꾼 것은 저장소에 올라가면 안 된다.

## 두 가지 수단을 구분해서 쓴다

| 대상 | 수단 |
|---|---|
| 이미 추적 중인 공유 파일인데 내 값만 다름 | `git update-index --skip-worktree <file>` |
| 내 PC에만 있는 폴더/파일을 무시 | `.git/info/exclude` (공유 `.gitignore` 아님) |

`git rm --cached` 는 **쓰지 말 것** — 푸시하면 다른 사람 저장소에서 그 파일이 삭제된다.

## 현재 적용된 것

- `.vscode/settings.json` — skip-worktree. 작업트리는 JDK `jdk-17.0.20.101-hotspot`,
  저장소(HEAD)는 `jdk-17.0.20.8-hotspot` 인 채로 둔다. [[vscode-java-jdk-path-breaks-on-update]]
- `.claude/settings.local.json` — skip-worktree(2026-10-12 적용). **이름은 local 인데
  `be67f74 Initial commit` 에 딸려 들어가 추적되고 있었다**(무시 규칙도 없었음). 내용이
  PC별 허용목록(내 PID·내 JDK 경로)이라 고치면 동료 것을 덮어쓴다. 저장소 쪽은
  `.8` 경로와 죽은 항목(특정 PID taskkill 등)이 남은 채로 둔다.
- `.git/info/exclude` — `/doc/`(로컬 참고문서), `/infra/kafka-local/`, `/point/gradle.properties`

확인: `git ls-files -v | findstr "^S"` 가 skip-worktree 목록.

## 함정

skip-worktree 가 걸린 파일은 **다른 사람이 그 파일을 고쳐 올리면 `git pull` 이 거부된다**
(`Your local changes would be overwritten`). 그때만 임시로 푼다:
`--no-skip-worktree` → `git stash` → `pull` → `stash pop` → 다시 `--skip-worktree`.

또한 내 수정이 `git status` 에 안 보이므로, 그 파일을 또 고칠 일이 생기면
"왜 안 올라가지" 로 헤매기 쉽다. 고치기 전에 skip-worktree 목록을 먼저 본다.

`.claude/settings.local.json` 은 **내가 못 고친다** — Bash 쓰기든 편집 도구든 자동 승인
모드가 Self-Modification 으로 막는다(내 권한 허용목록이라서). 내용 정리가 필요하면
바꿀 JSON 전문을 사용자에게 드리고 직접 붙여넣거나 `/permissions` 로 편집하시게 한다.
git 인덱스 조작(`update-index --skip-worktree`)은 막히지 않는다.
