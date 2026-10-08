---
name: verify-menu-location-before-building
description: "화면/기능을 포팅하기 전에 AS-IS asis_dump.op_menu에서 진짜 메뉴 위치와 display_flag/status_code를 먼저 확인할 것 - 안 하면 발명된 네비게이션을 만들게 된다"
metadata:
  node_type: memory
  type: feedback
---

**사고 경위(2026-10-08)**: 포인트사용 정합성검증(7210) 화면이 보는 "AB" 코드 주문을 추적해
AS-IS `OrderAdminServiceImpl`(관리자 주문 수기/엑셀 등록)을 찾았다. 이 기능을 TO-BE에
포팅하면서 **AS-IS 메뉴 구조를 한 번도 확인하지 않고** 바로 자바 소스만 보고 구현한 뒤,
주문목록 화면에 "관리자 주문 등록"이라는 링크를 **발명**해서 추가했다. 사용자가 "AS-IS
메뉴 위치가 어디야? 답례품관리>주문관리>오프라인주문관리에 그 기능이 있다는거 맞아?"라고
묻자 그제서야 확인했는데 - 완전히 다른 기능(오프라인 주문관리=결제수단별 조회화면)과
혼동하고 있었다. `asis_dump.op_menu`(AS-IS 원본, remap 전) 직접 조회로 진짜 위치가 다른
메뉴(3000>3700>3701)이고 **그 가지 전체가 AS-IS 자체에서 비활성**(display_flag='N',
최상위는 status_code='2')이라는 것까지 그제서야 알았다. 결국 두 가지를 임의로 한 것:
① 범위를 혼자 판단해서 줄였다 ② 없는 네비게이션을 발명했다 - 둘 다 사용자 확인 없이.

**교훈**: 화면/기능 이름(JSP·컨트롤러·서비스 클래스명)이 비슷해 보여도 **실제로 같은 메뉴를
가리키는지는 메뉴 테이블로 확인해야 안다.** 특히 "오프라인"·"수기"·"대량" 같은 비슷한
단어가 들어간 여러 기능이 공존할 수 있다(이번에도 오프라인 주문관리/수기결제/대량주문
관리가 전부 다른 기능이었다).

**How to apply**:
1. 어떤 AS-IS 기능을 포팅하기 전에, 그 컨트롤러 URL로 **`asis_dump.op_menu`를 먼저 조회**해서
   진짜 `menu_id`/`menu_parent_id`/`menu_name`/`display_flag`/`status_code`를 확인한다
   (`admin.op_menu`는 TO-BE가 remap한 값일 수 있어 원본과 다를 수 있다 -
   migration-admin-menu-asis-reseed.sql 주석 참고: "menu_url만 TO-BE 컨트롤러로 remap").
2. **상위 체인 전부**(parent의 parent까지)의 display_flag/status_code를 확인한다 - 자기
   자신은 'Y'인데 조상이 'N'이면 실제로는 숨겨진 것이다(2026-10-02 전수동기화가 "조상체인
   전부 display=Y인 가시 트리"만 뽑은 이유).
3. 비활성이면 [[as-is-parity-includes-disabled-state]]대로 **기능은 만들되 노출은 AS-IS처럼
   숨긴다** - 네비게이션 링크·메뉴 등록을 자체적으로 만들지 않는다. 화면이 실존해야 하는
   이유(이번처럼 다른 화면이 그 데이터를 참조하는 경우 등)가 있어도 "숨겨서 존재"와
   "눌러서 들어가지는 것"은 다르다.
4. 비슷한 이름의 다른 메뉴가 있으면(오프라인/수기/대량 등) 반드시 URL과 컨트롤러 메서드까지
   대조해서 혼동하지 않는다.
