---
name: vscode-java-jdk-path-breaks-on-update
description: ".vscode/settings.json 에 Temurin JDK 절대경로가 박혀 있어, JDK 패치 때마다 폴더명이 바뀌며 'Invalid runtime for JavaSE-17' 경고가 뜬다"
metadata:
  type: project
---

`.vscode/settings.json` 의 `java.configuration.runtimes[].path` 와
`java.import.gradle.java.home` 에 Temurin JDK **절대경로**가 박혀 있다.

Temurin 은 패치될 때마다 설치 폴더 이름이 바뀌고 **옛 폴더는 지워진다**:
`jdk-17.0.20.8-hotspot` (17.0.20+8) → `jdk-17.0.20.101-hotspot` (17.0.20.1+1)

그러면 Red Hat Java 확장이
`Invalid runtime for JavaSE-17: The path points to a missing or inaccessible folder`
를 띄우고 **에디터 코드분석만** 죽는다.

**빌드는 멀쩡하다.** Gradle 은 `JAVA_HOME` 을 쓰므로 터미널 빌드/`bootJar` 는 영향 없다.
즉 이 경고는 빌드 실패 신호가 아니다.

## 고치는 법

1. `ls "C:\Program Files\Eclipse Adoptium"` 로 실제 폴더명 확인 (= `JAVA_HOME` 과 일치)
2. settings.json 의 두 줄을 그 경로로 교체
3. VS Code `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → *Restart and delete*
   (단순 새로고침은 실패 캐시가 남아 경고가 재발)

이 파일은 공유 파일이라 수정분을 푸시하면 안 된다 → [[local-env-changes-never-pushed]]

근본 해결책(미적용): 두 줄을 아예 지우면 확장이 `JAVA_HOME` 을 자동 탐지해
업데이트에 영향받지 않는다. 명시 설정 유지를 택해 지금은 경로만 갱신해 둠.
