---
name: native-query-varchar1-returns-character
description: 네이티브 쿼리에서 varchar(1) 컬럼은 Character로 돌아온다 - (String) 캐스팅하면 목록이 ClassCastException 500
metadata:
  type: project
---

Hibernate 네이티브 쿼리가 `Object[]`로 돌려주는 행에서 **`varchar(1)` 컬럼은 `String`이 아니라
`java.lang.Character`**다. record로 매핑하며 `(String) r[n]`으로 캐스팅하면 그 순간
`ClassCastException: class java.lang.Character cannot be cast to class java.lang.String`으로
**화면이 통째로 500**이 난다.

**실제 사례**
- 2026-10-12 답례품제공자 공지사항(5114) 목록: `NOTICE_FLAG`·`DISPLAY_FLAG`가 `varchar(1)`이라
  목록 진입이 바로 500이었다. `SysNoticeSellerAdminRepository.str()`(= `value.toString()`)로 교정.
- 메뉴관리 답례품관리자 화면에서도 같은 원인으로 500이 났었다
  ([[admin-menu-admin-seller-data-and-pagination]]).
- `QestnarRepository`에도 같은 함정 주석이 이미 달려 있다
  (`CASE WHEN ... THEN 'Y' ELSE 'N' END` 결과도 길이 1이라 Character로 온다).

**Why:** 컬럼 정의가 `character varying(1)`이어도 길이가 1이면 드라이버/Hibernate 조합이
Character로 올려준다. 엔티티 매핑(`createNativeQuery(sql, Xxx.class)`)이나 JPQL을 쓰면
`@Column` 타입대로 변환돼 문제가 없고, **record/Object[] 수동 매핑에서만** 터진다.
컴파일로는 안 잡히고 그 화면을 열어야 드러난다.

**How to apply:** 네이티브 쿼리를 record로 매핑할 때 문자열 칸은 `(String)` 캐스팅 대신
**`value == null ? null : value.toString()`** 류의 헬퍼를 쓴다. 특히 `*_YN`·`*_FLAG`·
`CASE WHEN ... 'Y'/'N'` 처럼 한 글자가 오는 칸은 반드시 그렇게 한다.

**남은 점검거리**: `grep -rn "(String) r\[" admin/src/main/java/com/ghlove/admin/repository/`로
나오는 리포지토리들(Maintenance·ManagerManual·Mnl·QnaIndividual 등)은 현재 select 목록에
길이 1 컬럼이 없어 통과하고 있을 뿐이다. 그 쿼리에 `*_YN` 류를 추가하는 순간 같은 500이 난다.
