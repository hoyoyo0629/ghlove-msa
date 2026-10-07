---
name: admin-featured-event-port-progress
description: "이벤트 관리(featured, 메뉴 16601 /admin/featured) AS-IS 이식 진척 - OP_FEATURED=지자체별 이벤트(class0/type1). 목록 완료, 등록/수정 폼 잔여"
metadata:
  node_type: memory
  type: project
---

**featured = 지자체별 이벤트 관리**(AS-IS 메뉴 "이벤트 관리" MENU_12000, opmanager/featured). 처음엔
"기획전/미사용"으로 오판했으나 사용자가 AS-IS OP_FEATURED 1,063건(전부 `FEATURED_CLASS=0,
FEATURED_TYPE=1`, 이름 "이벤트-NNNN", 지자체별, 기간 있음)을 확인해줌. 실사용 기능. [[featured-unused-skip]]
데이터 export: `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\OP_FEATURED_202610061741.sql`.

**범위(사용자 승인 2026-10-06): 관리화면(목록+등록/수정+삭제), PC·이벤트(type=1) 기준.** 스토어프론트
노출은 보류.

**★정정: OP_FEATURED 테이블은 미배포가 아니다.** 처음 `ghlove_core.gift.op_featured`로 조회해
"미배포"로 오판했는데, **gift는 별도 DB**(`jdbc:postgresql://localhost:15432/gift`)다. gift DB의
`op_featured`는 `service-gift.sql`에 **AS-IS 46컬럼 전부** 정의돼 있고 실재한다(0행). `op_featured_item`도
있다. 따라서 **DDL 작업 불필요**(사용자가 덤프 DDL 주기로 했으나 안 줘도 됨 - AS-IS DDL은
`...db-dump\테이블dump(ddl만...)\op_featured.sql`에도 있음). gift DB 조회는
`docker exec -i ghlove-postgres psql -U postgres -d gift`.

**AS-IS 4축 요지(FeaturedManagerController + featured/list.jsp + featured-mapper):**
- `/opmanager/featured`(PC type1) · `/opmanager/featured-mobile`(type2, 데이터 없음). GET=빈 목록, POST=검색.
- 지자체 스코핑: ROLE_ADMIN_5~8 자기 지자체만, 1~4(시스템/행안부) 전체 + 시도/시군구 필터. 그 외 "00000".
- 검색: 이벤트명(FEATURED_NAME LIKE)·진행상태(1미진행/2진행중/3진행완료, START/END_DATE+TIME vs NOW yyyyMMddHH)·공개유무(DISPLAY_LIST_FLAG Y/N).
- 목록 컬럼: (5/6만)체크박스·No·(1~4만)지자체(locgovName)·이벤트명(수정링크)·진행상태·공개여부·진행기간(99999999 센티넬→상시게시/개방형)·등록일.
- 등록/삭제 버튼 ROLE_ADMIN_5/6만. 선택삭제=checked-delete. 등록/수정은 ROLE_ADMIN_5~8.
- 채번: insertFeatured. 상세 featured/form.jsp(이벤트 내용 에디터·이미지·기간/상시·아이템).

**TO-BE 진척:**
- **목록 완료(2026-10-06)**: `FeaturedAdminController.list`를 GET(빈)/POST(검색)으로, 지자체 스코핑·
  진행상태·공개유무·이벤트명 검색·지자체명·페이징·시도필터·선택삭제 구현. **gift 변경 없이** admin이
  `client.list("1",null)` 전체를 받아 메모리에서 필터(1406·배송업체 패턴). 진행상태는 날짜정밀(gift
  FeaturedDto에 START/END_TIME 없음 - AS-IS는 시정밀, 표시상 차이 미미). locgovName·시도·시군구는
  LocgovClient(donation)로 해소, 시군구는 `GET /admin/featured/locgov-children` ajax(AS-IS
  /common/getLocgovCode 대응). 진행기간은 `@opDate.ymd`+99999999 로직([[admin-date-format-opdate-helper]]).
  **URL 충돌 해소**: 검색 POST가 `/admin/featured`라 기존 create(POST 베이스)와 충돌 → create를
  `POST /admin/featured/create`로 이동(form.html action도 수정).
- **폼 진행 중(2026-10-06)**: AS-IS `featured/form.jsp`(907줄) 활성 필드 = 이벤트명·진행기간(시작/종료
  날짜+시각 select hours)·대표연락처(phone1/2/3)·주최주관(host)·홈페이지(link)·목록이미지(type5,
  5MB,300x300)·상단PC이미지(type2,20MB,1120)·상단모바일이미지(type3,20MB,350)·상세내용(스마트에디터)·
  상품편성(prodString→OP_FEATURED_ITEM). 저장은 ROLE_ADMIN_5~8, 이미지 삭제 ajax.
  - **[완료] gift Featured 엔티티 확장**: startTime/endTime/featuredHost/featuredPhoneNo1~3/
    featuredImageMobile/featuredListImage/thumbnailImageMobile/thumbnailListImage/prodState 매핑 추가.
    gift 빌드·기동 확인(컬럼은 테이블에 이미 있음).
  - **[잔여] gift create/update API**: 위 필드 수신 + 이미지 3슬롯(목록/PC/모바일) 업로드·삭제 + 아이템.
  - **[잔여] admin form.html 전면 재작성**(현재 발명형 "기획전" 간이폼) + FeaturedAdminController/Client 확장.
  - 그 다음 스토어프론트(보류).
- 미검증: 런타임 클릭(세션 필요). 빌드·기동·가드테스트 통과.
