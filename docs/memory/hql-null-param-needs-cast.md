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

**[2026-10-07 추가] ★Long/LocalDateTime은 cast로 안 고쳐진다**: `admin.PrivacyAccessLogRepository`의
`(:managerId is null or ...)` / `(:from is null or ...)`에서도 같은 "could not determine data
type of parameter"로 500이 났다(1411 엑셀다운로드사유관리). `cast(:x as Long)`로 양쪽
occurrence를 감쌌더니 그 에러는 없어졌지만, **재기동 후 새 에러** `cannot cast type bytea to
bigint`가 났다 - Hibernate가 null **Long** 값 자체를 bytea로 바인딩해버려서 cast(bytea→bigint)가
거절당한 것이다(String의 cast(:x as String)은 운영에서 실제로 잘 작동하는데 Long은 안 되는
비대칭 - 내부 이유는 못 밝혔으니 "String은 되고 Long/LocalDateTime은 안 된다"는 경험적 사실만
믿을 것). **결론: String이 아닌 타입에 cast를 시도하지 말고 바로 "조건부 조립"으로 가라** -
`PrivacyAccessLogService`가 `EntityManager`로 JPQL을 문자열 조립하는 방식(아래 "해당 없음"
목록과 같은 패턴)으로 바꿔서 해결했다. [[admin-excel-download-log-500-fix]] 참고.

**How to apply:**
- **String** 파라미터의 `= :x` / `>= :x`조건은 cast 불필요. **String이 아닌 타입**(Long/
  LocalDateTime 등)이 "is null" 분기에만 쓰이면 cast로 때우려 하지 말고 바로 조건부 조립으로
  전환할 것 - cast는 String에서만 검증된 해법이다.
- cast 대상은 `varchar`가 아니라 Hibernate 문서화 타입명(`String`). `varchar`/`bigint` 같은
  SQL 타입명은 네이티브 쿼리에만.
- **nativeQuery=true는 해당 없다** - 네이티브는 자바 타입으로 바인딩되므로 문제가 없다
  (`CommonMessageRepository`, point `GCntrUsePointRepository`가 그 예).
- SQL을 조건부로 조립하는 방식(`QestnarRepository`·`BatchLogRepository`·donation
  `OffgiveAdminRepository`)도 해당 없다 - 값이 있을 때만 파라미터가 붙는다.
- 이 결함은 **기동 시점에도 안 잡힌다**(실행해야 터진다). 조건부 검색을 새로 만들면
  "검색어 비움" 경로를 한 번은 실제로 호출해 볼 것 - [[build-is-mine-restart-is-users]]의 여분 포트 검증.
