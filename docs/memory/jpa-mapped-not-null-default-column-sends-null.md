---
name: jpa-mapped-not-null-default-column-sends-null
description: NOT NULL DEFAULT 컬럼을 엔티티에 매핑하면 JPA가 insert에 NULL을 명시해 DB 기본값이 무력화된다 - g_cntr.delete_at 매핑(10-06) 뒤로 기부 insert 전부 실패, 10-14 15102 테스트에서 발견
metadata:
  type: feedback
---

DB 기본값에 기대던 컬럼(`NOT NULL DEFAULT ...`)을 엔티티 필드로 매핑하면, 값을 안 넣어도 Hibernate가 insert 컬럼 목록에
포함시켜 NULL을 보낸다 → 기본값이 안 먹고 NOT NULL 위반.

**Why:** 2026-10-06 커밋 04bc80d에서 집계 조건용으로 `Donation.deleteAt`(G_CNTR.DELETE_AT, NOT NULL DEFAULT 'N')을
매핑한 뒤 `donation.g_cntr` insert가 한 건도 성공하지 못했다(10-01 이후 행 0건 - 온라인·기탁서 기부 전부). 10-14 기탁서
등록(15102) 테스트에서 "기탁서 등록에 실패하였습니다. 고객센터에 문의 하세요."로 드러남. 새 Donation을 만드는 유일한 곳
`DonationService.saveRequested`에서 `setDeleteAt("N")`으로 수정(AS-IS insert는 컬럼을 빼고 기본값을 탄다).

**How to apply:** 기존 테이블 컬럼을 엔티티에 새로 매핑할 때 `information_schema.columns`로 is_nullable/column_default를
확인하고, NOT NULL+기본값이면 생성 지점에서 같은 값을 넣거나 `@Column(insertable=false)`/`@ColumnDefault`+`@DynamicInsert`를 쓴다.
g_cntr의 다른 기본값 컬럼(cntr_point, foreign_status_code)은 미매핑이라 안전 - 매핑하면 같은 문제.
