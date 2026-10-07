---
name: member-signup-terms-box-asis-content
description: "회원가입 화면 약관 요약 박스를 AS-IS agree.vue와 동일하게 DB 연동+비동의 토글까지 구현(2026-10-07) - 개인정보 수집·이용 동의는 타입1이 아니라 타입6"
metadata:
  node_type: memory
  type: project
---

storefront `SignupView.vue`의 "약관 요약 박스" 2개(이용약관/개인정보 수집·이용 동의)가 하드코딩된
플레이스홀더 텍스트였던 걸 AS-IS `agree.vue`(`getPolicyInfo()`)와 동일하게 admin
약관관리(OP_POLICY) 실데이터를 읽도록 바꿨다.

**★핵심 발견**: "개인정보 수집·이용 동의" 체크박스는 POLICY_TYPE **1**(개인정보처리방침)이
아니라 **6**("개인정보 수집·이용 동의")을 쓴다 - AS-IS `agree.vue`의 `collectionAgree`가
그 증거(`PolicyInfo.POLICY_TYPE_COLLECTION_POLICY` 상수). 타입1은 별도의 전체 개인정보처리방침
문서(`/policy/privacy`)이고, 가입 화면 안의 요약 동의 박스는 그것과 다른, 더 짧은 타입6
콘텐츠다. 체크박스 라벨 문구도 AS-IS는 하드코딩이 아니라 DB `title`을 그대로 쓴다(TO-BE도
동일하게 바꿈 - `title` 없으면 기존 하드코딩 문구로 폴백).

**"알림서비스 수신 동의" 박스**는 AS-IS도 DB가 아니라 `agree.vue`에 **하드코딩된 고정 HTML**이다
(국민비서/Email/SMS/알림톡 체크박스 4개는 TO-BE에 이미 있었음 - 그 설명 본문만 없었다). AS-IS
원문을 그대로 옮겨 넣었다(`/users/modify.html` 링크만 TO-BE 경로 `/mypage/profile`로 교체 -
AS-IS에 없는 페이지로 보내는 것 자체가 더 큰 결함이라서).

**★[2026-10-07 추가] "비동의" 토글**: AS-IS는 필수 항목 2개(이용약관/개인정보수집이용) 체크박스
옆에 "비동의" 체크박스가 나란히 있고, 서로소 토글이다 - `selected()`가 동의 체크 시 비동의를
지우고, `selectDisagree()`가 비동의 체크 시 동의를 지운다(각자 `checkResult`/`disagreeResult`
배열에서 상대 값을 제거하는 방식, AS-IS 소스 확인). "전체약관 동의하기"를 누르면(켜든 끄든)
비동의 둘 다 비운다(`allCheck()`). **`disagreeResult`는 가입 API로 전송되지 않는 순수 화면
상호작용**이다(`UserDomainInfo`에 해당 필드 없음, 프론트 전역 grep으로 사용처 0건 확인) - 그래서
백엔드 변경 없이 프론트만으로 구현했다. TO-BE는 배열 대신 불리언 쌍(`agreeTerms`/`disagreeTerms`,
`agreePrivacy`/`disagreePrivacy`)으로 같은 상호배타 동작을 직접 구현(결과 동일).

**빠진 것(의도적, AS-IS도 agree.vue가 실제로 안 씀)**: `otherAgree`(타입4, "개인정보제3자동의")는
AS-IS `JoinController.getPolicyInfo`가 조회는 하지만 `agree.vue` 템플릿이 전혀 렌더링하지
않는 죽은 응답 필드다 - TO-BE도 안 받는다.

**구현**: admin `PolicyApiController.TYPE_BY_NAME`에 `"collection"→"6"` 추가(AS-IS에 URL
이름이 없던 타입이라 `PolicyInfo` 상수명을 따서 새로 붙임) → member에 `PolicyClient`
(donation과 동일 패턴) + `GET /api/signup-policies`(`SignupPolicyApiController`) 추가 →
storefront가 그걸 불러 두 박스에 `v-html`로 채우고, 동의/비동의 상호배타 핸들러 4개
(`onAgreeTermsChange`/`onDisagreeTermsChange`/`onAgreePrivacyChange`/`onDisagreePrivacyChange`)
추가.

**함정**: 새 파일 2개(`PolicyClient.java`/`SignupPolicyApiController.java`) 추가 직후
`./gradlew compileJava`가 **UP-TO-DATE로 스킵**되는 걸 겪었다(Windows 타임스탬프 해상도
문제로 보임) - `--rerun-tasks`로 강제 재컴파일한 뒤에야 jar에 반영됐다. 새 파일 추가 직후
빌드 크기/타임스탬프가 안 바뀌면 의심할 것.

**상태**: admin·member 둘 다 컴파일·테스트·bootJar 통과, jar 안에 새 클래스 포함 확인.
storefront는 Vite라 재기동 불필요(저장 즉시 반영). **재기동 필요: admin, member.**
