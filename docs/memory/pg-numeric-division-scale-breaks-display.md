---
name: pg-numeric-division-scale-breaks-display
description: PostgreSQL numeric 나눗셈이 스케일 16+로 나와 비율 컬럼이 0E-20 / 0.61000000000000000000 으로 찍힌다
metadata:
  type: project
---

AS-IS(CUBRID) 식 `FLOOR(a*10000.0/b)/100` 을 그대로 옮기면 PostgreSQL에서는 결과 스케일이
**16자리 이상**으로 잡힌다. 실측(2026-10-08, 개발DB):

```
FLOOR(16662000*10000.0/2699500000)/100  ->  1.5700000000000000
```

이 값이 `BigDecimal`로 올라오면 화면까지 스케일이 따라가서 **요약은 `0.61000000000000000000%`,
0은 `BigDecimal(0, scale 20).toString()` 인 `0E-20%`** 로 찍힌다. 특정사업 지자체별통계에서 실제로
그렇게 깨져 있었다.

**Why:** CUBRID는 같은 식에서 2자리를 돌려줬다. DB만 바꿔 이식하면 SQL은 "AS-IS와 글자까지 같은데"
화면 숫자가 달라지므로, 소스 대조만으로는 안 잡히고 화면을 봐야 발견된다.

**How to apply:** 비율·달성율·점유율처럼 나눗셈 결과를 내려주는 컬럼은 세 군데를 같이 맞춘다.
1. SQL에서 `CAST(... AS numeric(20, 2))` 로 **스케일만** 고정(값은 AS-IS 식 그대로 둔다).
2. Java 타입을 AS-IS DTO와 같게 — AS-IS가 `float`면 `float`로 받는다(`BigDecimal` 금지).
3. 표시도 AS-IS를 따른다. AS-IS는 **요약과 목록이 다르다** - 요약은 `op:numberFormat`(=
   `NumberFormat.getNumberInstance()`, 소수 최대 3자리·0이면 생략)을 거치고, 목록은 포맷터 없이
   float을 그대로 찍는다(`0.0`, `0.61`). 그래서 `#numbers.formatDecimal`을 쓰면 자리수가 고정돼
   틀린다. 요약용으로 `admin/web/support/OpNum.java`(`${@opNum.format(값)}`)를 만들어 뒀다.

다른 통계 화면(월별통계·기부금현황·판매통계)에 같은 형태의 비율 컬럼이 더 있을 수 있어 소급 점검
대상이다. [[admin-date-format-opdate-helper]] 와 같은 성격의 전수 교정 건이다.
