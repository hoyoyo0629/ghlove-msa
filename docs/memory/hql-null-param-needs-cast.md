---
name: hql-null-param-needs-cast
description: 조건부 검색 @Query에서 LIKE 검색어는 cast(:x as String) 필수 - 안 하면 null이 bytea로 바인딩돼 PostgreSQL이 거절
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T00:15:23.048Z
---

**AS-IS 매퍼의 `<if>` 조건부 WHERE를 `(:x is null or ...)`로 옮길 때, `concat`/`LIKE` 안에만 쓰이는
검색어 파라미터는 반드시 `cast(:x as String)`으로 타입을 줘야 한다.** 양쪽 occurrence 모두:

```java
and (cast(:query as String) is null or p.subject like concat('%', cast(:query as String), '%'))
```

**Why:** Hibernate 6은 파라미터 타입을 쓰이는 문맥에서 추론한다. `p.col = :x` / `>= :x`처럼 비교
대상이 있으면 잡아내지만, **`concat(...)`은 인자 타입을 알려주지 않아** 추론에 실패하고 null을
`Serializable`(=bytea)로 바인딩한다. 그러면 PostgreSQL이
`operator does not exist: character varying ~~ bytea`로 쿼리를 거절한다(검색어를 비우고 목록에
들어가는 순간 500). 2026-10-06 아침 사용자가 **팝업관리 목록**에서 실제로 맞았고, 같은 패턴이던
admin `PopupRepository`·`PolicyRepository`·`OpEmailRepository`·`CommonCodeRepository`·
`IpsSendingMasterRepository` 5개를 그때 고쳤다.

**How to apply:**
- `= :x` / `>= :x` 조건의 파라미터는 cast 불필요(과하게 붙이지 말 것 - 날짜·숫자 타입에 varchar를
  씌우는 실수가 된다).
- cast 대상은 `varchar`가 아니라 **`String`**(Hibernate 문서화 타입명). `varchar`는 네이티브 쿼리에만.
- **nativeQuery=true는 해당 없다** - 네이티브는 자바 타입으로 바인딩되므로 문제가 없다
  (`CommonMessageRepository`, point `GCntrUsePointRepository`가 그 예).
- SQL을 조건부로 조립하는 방식(`QestnarRepository`·`BatchLogRepository`·donation
  `OffgiveAdminRepository`)도 해당 없다 - 값이 있을 때만 파라미터가 붙는다.
- 이 결함은 **기동 시점에도 안 잡힌다**(실행해야 터진다). 조건부 검색을 새로 만들면
  "검색어 비움" 경로를 한 번은 실제로 호출해 볼 것 - [[build-is-mine-restart-is-users]]의 여분 포트 검증.
