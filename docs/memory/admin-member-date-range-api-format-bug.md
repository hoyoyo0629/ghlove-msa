---
name: admin-member-date-range-api-format-bug
description: "탈퇴회원리스트(4105)·휴면회원관리(4107)·대시보드 오늘가입 위젯이 member 검색 API에 yyyyMMdd를 그대로 보내 500→빈 결과로 집어삼켜지던 버그, 2026-10-08 수정"
metadata:
  node_type: memory
  type: project
---

**사고 경위**: 사용자가 일반회원관리에서 회원 하나를 탈퇴시켰는데 탈퇴회원리스트(4105,
`/admin/secede-users`)에 조회가 안 된다고 보고했다. 실데이터 확인 결과 탈퇴 자체는 정상
처리됐다(`member.op_user` user_id 1080, status_code=WITHDRAWN, leave_date=오늘). member의
검색 API를 "yyyy-MM-dd" 형식으로 직접 호출하면 정확히 그 행을 돌려준다 - **member 쪽은
멀쩡하다.**

**진짜 원인**: `SecedeUserAdminController`/`SleepUserAdminController`가 화면 날짜입력칸의
`yyyyMMdd`(8자리, 구분자 없음) 값을 **변환 없이 그대로** `MemberAdminClient.searchSecede()`/
`searchSleep()`에 넘겼다. member 쪽 `AdminMemberService.rangeStart/rangeEnd`는
`LocalDate.parse(dateStr)`를 쓰는데 이건 기본적으로 "yyyy-MM-dd"만 받는다 - "20261008"처럼
구분자 없는 문자열을 넣으면 `DateTimeParseException`이 터져 member API가 500을 낸다.
그런데 `MemberAdminClient`의 모든 검색 메서드는 `catch (RestClientException e)`로 **에러를
빈 결과(`List.of(), 0, 0`)로 조용히 삼켜버리는 패턴**이라, 화면에는 그냥 "검색결과 없음"으로만
보이고 로그를 안 보면 원인을 알 수 없다.

같은 패턴이 하나 더 있었다: `AdminHomeController`(관리자 홈 대시보드)의 "오늘 가입 회원수"
위젯도 `yyyyMMdd` 포맷의 `today`를 그대로 `memberAdminClient.search(today, today, ...)`에
넘기고 있어서, **대시보드가 항상 0으로 보이던 상태**였다(같은 try/catch(Exception ignored)
패턴). 이건 사용자가 보고하지 않았지만 같은 버그라 같이 고쳤다.

`GeneralCustomerSearchParam`(일반회원관리, 4101)는 처음부터 `getFromDateForApi()`/
`getToDateForApi()`로 yyyyMMdd→yyyy-MM-dd 변환을 해주고 있어서 **그 화면만 이 버그가
없었다** - 그래서 지금까지 안 걸렸다.

**조치**: `SecedeUserSearchParam`/`SleepUserSearchParam`에도 같은 모양의
`getSrchStart*DateForApi()`/`getSrchEnd*DateForApi()`(8자리 아니면 null, 맞으면
"yyyy-MM-dd"로 변환)를 추가하고, 두 컨트롤러가 그걸 쓰도록 바꿨다. `AdminHomeController`는
멤버 호출 직전에 `LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)`로 별도 변환한
값을 쓰도록(주문 쪽 `today`는 yyyyMMdd 그대로 유지 - 두 서비스 요구 포맷이 다르다).

**교훈/재발방지**: admin이 다른 서비스의 날짜범위 검색 API를 호출하는 자리를 새로 만들 때마다
"그 서비스가 yyyyMMdd를 받는지 yyyy-MM-dd를 받는지" 확인하고, 화면 입력칸 포맷(yyyyMMdd)과
다르면 반드시 변환 메서드를 만들 것. `MemberAdminClient`처럼 예외를 빈 결과로 삼키는 클라이언트는
이런 포맷 버그를 "데이터 없음"으로 둔갑시키므로, 검색결과가 이상하게 0건이면 **먼저 해당
서비스 API를 올바른 포맷으로 직접 curl 호출해 재현**해보는 게 디버깅 1순위다.

**검증**: member API를 `fromDate=20261008`(500 재현) vs `fromDate=2026-10-08`(정상, 1건)으로
직접 비교해 원인 확정. 수정 후 admin `compileJava+test+bootJar` EXIT=0.

**관련**: [[admin-member-area-port-progress]](4105/4107 최초 포팅 당시 기록), 휴면회원관리
시드데이터는 [[admin-sleep-user-seed-data]] 참고. **재기동 필요: admin.**
