---
name: local-env-changes-never-pushed
description: "내 PC 전용 설정 변경은 절대 푸시하지 않는다. 추적 파일은 skip-worktree, 무시 규칙은 .git/info/exclude. 현재 .vscode/settings.json 이 skip-worktree 상태"
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
- `.git/info/exclude` — `/doc/`(로컬 참고문서), `/infra/kafka-local/`, `/point/gradle.properties`

확인: `git ls-files -v | findstr "^S"` 가 skip-worktree 목록.

## 함정

skip-worktree 가 걸린 파일은 **다른 사람이 그 파일을 고쳐 올리면 `git pull` 이 거부된다**
(`Your local changes would be overwritten`). 그때만 임시로 푼다:
`--no-skip-worktree` → `git stash` → `pull` → `stash pop` → 다시 `--skip-worktree`.

또한 내 수정이 `git status` 에 안 보이므로, 그 파일을 또 고칠 일이 생기면
"왜 안 올라가지" 로 헤매기 쉽다. 고치기 전에 skip-worktree 목록을 먼저 본다.
