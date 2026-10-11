---
name: admin-oper-charger-authority-status-disable-rules
description: "운영관리자(4501) 상세화면의 회원구분·사용여부 라디오 비활성화 4규칙(AS-IS adminRoleCheck) 신규구현 - TO-BE엔 이 로직 자체가 전혀 없었음, 2026-10-08"
metadata:
  node_type: memory
  type: project
---

**발견 경위**: 사용자가 `/admin/person-in-charge/oper/edit/1005`에서 회원구분(authority)이
AS-IS는 disabled인데 TO-BE는 바뀐다고 지적. 확인해보니 `OperPersonInChargeAdminController`는
`editable`(수정/삭제 버튼 노출)만 계산하고, AS-IS `oper-charger/edit.jsp`의
`adminRoleCheck()`(jQuery로 회원구분·사용여부 라디오에 disabled를 다는 함수) **전체가 아예
포팅되지 않은 상태**였다. 사용자가 짚은 건 그 중 일부(그룹 간 전환 금지)였지만 실제로는
4규칙 전부가 없었다.

**AS-IS 4규칙** (각각 true로 켜지기만 하고 끄는 분기가 없어 OR로 누적됨):
1. 대상 또는 보는 사람이 시스템(ROLE_ADMIN_1·2)이면 → 행안부 옵션(3·4) 비활성
2. 대상 또는 보는 사람이 행안부(3·4)이면 → 시스템 옵션(1·2) 비활성
   (1+2를 합치면 **시스템↔행안부 그룹 전환 자체가 불가능** - 같은 그룹 내 주/부 승격만 가능)
3. 주담당자(1·3)가 **본인 자신**을 보고 있으면 → 사용여부 비활성(자기 자신 중지 방지)
4. 부담당자(2·4)는 → 회원구분·사용여부 **둘 다** 비활성

**조치**: `OperPersonInChargeAdminController.applyRoleFlags()` 신규(private static) -
viewer/target의 authority로 `authorityGovDisabled`/`authoritySysDisabled`/`statusDisabled`
세 불린을 계산해 모델에 담고, `oper-edit.html`의 회원구분 4개·사용여부 2개 라디오에
`th:disabled`로 적용했다(지자체담당자관리 4402가 이미 쓰던 `th:disabled` 패턴과 동일 - AS-IS는
jQuery로 하지만 서버에서 직접 거는 게 TO-BE 기존 관례).

제출 시 값 읽기는 그대로 둬도 된다 - `chargerEdit()`의 `$("input[name=authority]:checked").val()`은
disabled라도 이미 checked인 값은 그대로 읽힌다(jQuery `:checked`는 disabled 여부와 무관) -
AS-IS도 서버쪽 추가 검증 없이 이 방식 하나로 막는다.

**검증(실데이터 추적)**: userId=1005(mois02, ROLE_ADMIN_4 행안부 부담당자)를 시스템 주관리자
(admin01, 1000)가 보면 targetIsGov+viewerIsSys 둘 다 걸려 회원구분 **전부** 비활성(그룹이
다르니 어느 쪽도 못 고름), 사용여부는 가능. 같은 행안부 소속(mois01, ROLE_ADMIN_3)이 보면
행안부 옵션만 활성(주/부 승격 가능)·시스템 옵션은 비활성·사용여부도 가능 - 의도대로 동작.

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**
