---
name: admin-locgov-search-and-depthist-register-fix
description: "지자체관리(4401) 목록 검색에 등록일 넣으면 조회 안 되던 버그(날짜포맷) + 부서코드 이력 팝업 등록자가 항상 빈칸이던 버그(managerId 미전송) 수정, 2026-10-08"
metadata:
  node_type: memory
  type: project
---

**① 목록 검색 - 등록일 넣으면 0건(날짜포맷 버그, [[admin-member-date-range-api-format-bug]]와
같은 클래스의 세 번째 발견)**: `LocgovSearchParam`이 화면의 `yyyyMMdd` 날짜를 변환 없이
`LocgovAdminClient.search()`에 그대로 넘기고, donation의 `LocgovAdminApiController.parseStart/
parseEnd`는 `LocalDate.parse()`(yyyy-MM-dd 전용)를 쓴다. 날짜를 비우면(null) 필터 자체가
안 걸려 문제가 안 드러나고, **날짜를 넣으면 donation이 500 → `LocgovAdminClient`가 빈 결과로
삼켜버려 "조회 안 됨"으로 보인다** - 정확히 사용자가 관찰한 증상("검색값 넣으면 안 되고 지우면
된다")과 일치. `curl startDate=20261008`(500) vs `startDate=2026-10-08`(정상, 성북구 1건)으로
재현 확정.
조치: `LocgovSearchParam`에 `getSrchStartCreatedForApi()`/`getSrchEndCreatedForApi()` 추가,
`LocgovAdminController.search()`가 그걸 쓰도록 변경.
**이 버그 클래스가 벌써 4번째 발견이다** - admin이 다른 서비스의 날짜범위 API를 부를 때마다
매번 확인이 빠졌다. 비슷한 패턴(`getSrchStartCreated()`를 변환 없이 쓰는 곳)이
`LocgovPersonInChargeAdminController`/`OperPersonInChargeAdminController`(→ admin 내부
`PersonInChargeAdminService`)에도 있는데, 거기는 `LocalDate.parse` 실패 시 **예외를 삼키고
기본값(전체기간)으로 폴백**하는 방식이라 500은 안 나지만 **날짜필터를 조용히 무시한다** -
다른 증상(결과가 0건이 아니라 필터링이 안 먹음)이라 이번엔 손대지 않았다. 재발 방지:
[[admin-member-date-range-api-format-bug]]에 "admin이 새 날짜범위 검색을 cross-service로
만들 때마다 ForApi 변환 확인" 교훈이 이미 적혀 있다 - 그런데도 이 화면(4401)은 애초에 그
변환이 아예 없었다(처음부터 빠진 것).

**② 부서코드 이력 팝업("등록자" 항상 빈칸) - 관리자 ID 자체를 안 보내고 있었다**: 지자체관리
수정화면에서 저장하면 `LocgovAdminController.update()`가 `locgovAdminClient.update(...)`를
부르는데 **`managerId`를 넘기는 파라미터가 처음부터 없었다**(등록 `register()`도 마찬가지).
donation 쪽 `LocgovAdminService.update/register`는 `managerId`를 받아 `Locgov.lastUpdusrId`·
`LocgovDeptHist.frstRegisterId/lastUpdusrId`에 제대로 쓰는 코드가 이미 있었는데 - admin이
그 값 자체를 아예 안 보내 항상 null로 들어갔다(실측: locgovCode=11000의 부서이력 2건 모두
`lastUpdusrId: null`). 화면의 `DeptHistRow.getRegisterText()`는 `lastUpdusrId`가 없으면 빈
문자열을 돌려주므로 "등록자" 칸이 항상 비어 보였다 - 로직 자체는 정상이었고 **입력값이
없었던 것**.
조치: `LocgovAdminClient.register()`/`update()`에 `Long managerId` 파라미터 추가,
`toMultipart()`가 managerId를 멀티파트 바디에 실어 보내도록 수정, 컨트롤러 두 곳
(`create()`/`update()`)이 `viewer.getUserId()`를 넘기도록 변경.
**검증**: donation API를 managerId=1000(admin01/관리자)으로 직접 호출해 부서코드를 바꿔보니
새 이력 행(`deptHistNo=4`)에 `lastUpdusrId=1000`이 정확히 들어갔다(기존 1~3번 이력은 버그
당시 데이터라 여전히 null - 과거 데이터는 소급되지 않는다, 그대로 둠). 테스트 중 curl 인코딩
문제로 chargerNm/locgovIntrcnCn/bassAdres/processDeptCode가 잠깐 깨졌던 걸 원래 값으로
직접 SQL 복구했다(2026-10-08 적용 완료, 데이터 유실 없음).

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**
