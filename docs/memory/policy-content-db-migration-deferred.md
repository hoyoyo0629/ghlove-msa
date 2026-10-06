---
name: policy-content-db-migration-deferred
description: 약관·정책 3건(이용약관/개인정보처리방침/저작권정책) 원문의 DB 이관은 2026-09-09 보류 - 운영데이터 수령 후 적재하고 재검토
metadata: 
  node_type: memory
  type: project
  originSessionId: 961fe3c7-599d-469a-81b9-785e75e64136
  modified: 2026-09-09T01:22:13.091Z
---

개인정보처리방침/저작권정책/이용약관 콘텐츠 관리 구조를 2026-09-09에 admin 기준으로 연결했고, **원문 DB 이관은 운영데이터를 받은 뒤로 미뤘다.**

**현재 상태 (코드 연결은 끝남)**
- 원본은 admin 서비스 약관관리(`/policy` CRUD, `admin.op_policy`). admin에 `GET /api/policies/{clause|protect|copyright|marketing|trader-raw}` 조회 API를 열었다(as-is `ApiPolicyController`와 같은 이름 체계, 인증 게이트 대상 아님).
- donation이 `PolicyClient` → `PolicyContentService`로 그걸 읽고, Thymeleaf 화면(`/policy/*`)과 SPA용 JSON API(`/api/policy/{privacy|copyright|auth}`)가 같은 서비스를 공유한다.
- **폴백**: 전시중(`exhibition_status='Y'`) 본문이 없거나 admin이 죽으면 `donation/src/main/resources/policy-content/{privacy,clause,copyright}.html`(총 1.6MB)을 읽는다. 지금은 이 폴백으로 화면이 뜬다.
- `admin.op_policy`의 시드 placeholder 행(policy_id=1000, 이용약관, content 9자)은 219KB 원문을 덮어써서 `exhibition_status='N'`으로 내려두었다. 되돌리려면 `update admin.op_policy set exhibition_status='Y' where policy_id=1000;`
- 조회 규칙은 as-is `policy-mapper.xml getCurrentPolicyByType`과 동일: 타입 일치 + `EXHIBITION_STATUS='Y'` + `CREATED_DATE DESC LIMIT 1`. 전시 시작/종료일은 as-is도 조건에 쓰지 않는다.

**Why:** as-is는 이 셋을 `OP_POLICY` + opmanager 약관관리로 운영했고 개인정보처리방침은 연 2~4회 개정된다(as-is에 `privacy-form20230101`~`20260604` 이력 존재). 서비스 리소스 파일에 박혀 있으면 운영자가 재배포 없이 개정할 수 없어 연결이 필요했다. 반면 원문 적재는 곧 받을 운영데이터로 하는 게 맞아서(임시 원문을 넣고 다시 덮는 낭비를 피함) 이관만 미뤘다.

**How to apply:** 운영데이터를 받아 `admin.op_policy`에 적재한 뒤 재검토할 것 — ①적재 후 `PolicyContentService`의 폴백과 `policy-content/*.html` 1.6MB 삭제 가능, ②admin 약관관리 폼이 1.3MB 위지윅 본문을 받을 수 있는지 확인 필요, ③`POLICY_TYPE` 공통코드가 없어(`admin.op_code` 테이블 자체가 없다) 약관관리 화면의 타입 라벨이 빈 값으로 보일 수 있다, ④회원가입 화면 약관 요약 박스는 as-is가 `getPolicyInfo()`로 DB에서 받아왔는데 to-be는 member `templates/signup.html`에 하드코딩돼 있어 같이 정리 대상이다.
