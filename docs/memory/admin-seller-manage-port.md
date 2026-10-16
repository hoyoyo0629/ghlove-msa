---
name: admin-seller-manage-port
description: 답례품관리>답례품제공자 관리(16151) AS-IS 이식 - 목록/등록/수정 + 계정(OP_SELLER_USER) 같이 저장, 사용자 결정 2건(계정 AS-IS처럼·실데이터 9,229건 보류), 안 옮긴 것(shadow 로그인 등), 알려진 한계
metadata:
  type: project
---

2026-10-15 이식. AS-IS `opmanager/seller/list`(메뉴 16151, 권한 ROLE_ADMIN_1~6). 기존 TO-BE 화면은 상태변경 버튼이 있는 축소 구현(목록+짧은 폼)이라
AS-IS `SellerManagerController`(list/create/edit/makeSellerId/init-seller-password/deleteFile)에 맞춰 교체했다(경로 `/seller`, `/seller/list`, `/seller/create`,
`/seller/edit/{id}`; 메뉴 URL은 `/seller` 그대로라 migration 불필요).

**사용자 결정(2026-10-15, AskUserQuestion)**: ①계정은 AS-IS처럼 **OP_SELLER + OP_SELLER_USER 같이 저장**(design-decisions §4의 "OP_SELLER만"과 다르게 - 목록의 인증서·본인인증
등록여부 3칸과 SMS수신동의가 OP_SELLER_USER에만 있어서). 사용자번호는 gift 자체 채번(MAX+1), 역할(ROLE_SELLER*)은 gift에 역할표가 없어 안 만듦, 비번은 salt+hash.5 대신 **BCrypt**
(gift에 spring-security-crypto 의존성 추가), 개인정보는 평문(TO-BE 전체 방침). ②AS-IS 실데이터 **9,229건은 보류**(아이디·이름·연락처·계좌가 암호화돼 있어 복호화 키·개인정보 규모 때문, op_manager와 같은 이유).

**구성**: gift `SellerManageService`·`SellerManageApiController`(`/api/admin/seller-manage`, multipart 폼 필드명 그대로) ↔ admin `SellerManageClient`·`SellerAdminController`·`templates/seller/list|form.html`·
`web/support/AdminRole`. 옛 `/api/admin/sellers`(이메일 발송대상·문자수신정보·답례품 판매자 select가 씀)는 그대로 둠 - 그 create/update/status는 이제 admin이 안 쓰는 죽은 엔드포인트.
`database/ddl/migration-gift-seller-user-asis-columns.sql`(OP_SELLER_USER에 pwd_chg_seller_yn·mber_dn·mber_fin_dn 추가 + 계정행 없던 기존 판매자 18건 보강 - **적용 필요**).

**AS-IS 특징(그대로)**: GET 진입은 빈 목록(POST 검색에서만, 등록일 기본=오늘, 페이지 링크는 op.manager.js가 폼 POST) · 목록은 OP_SELLER inner join OP_SELLER_USER(계정행 없으면 안 보임),
건수는 join 없이 OP_SELLER만 셈 · 지자체 칸/시도·시군구 검색은 SYS만 · 등록버튼·비밀번호 초기화는 SYS면 alert로 막힘 · 폼 저장은 ROLE_ADMIN_5/6만(그 밖은 form id=impossible-form + alert) ·
담당자정보/주문담당자/배송비/관리항목/수수료·정산주기 영역은 hidden이지만 값은 전송돼 등록 시 팩스·담당자 전화가 "010--"처럼 이어 붙고, 정산주기는 월정산(4)/8일로 고정 · 상태→계정상태 매핑(2→9,3→2,4→3,5→1) ·
첨부 3종 5MB·확장자 검사, 새 첨부 올리면 옛 파일 삭제(폴더 `gift/uploads/gift/seller/{id}/원본파일명`) · 비번 초기화=1111 + 계정 PWD_CHG_SELLER_YN='Y'.
지자체 담당자의 목록·등록은 서버가 자기 지자체로 강제(AS-IS는 파라미터로 덮어쓸 수 있는 구멍이 있었음).

**안 옮긴 것**: 목록의 "로그인"(shadow-login - 제공자 포털 로그인 세션이 아직 없음, design-decisions §5 #1·#2 이후), 삭제/일괄삭제(AS-IS도 버튼 주석처리), find/find-popup/find-md 팝업(다른 화면용·MD 영역 hidden),
비밀번호 변경 팝업(호출 주석처리), 변경이력 팝업(본문 주석처리), id-duplicate-Check(화면이 안 부름), 담당MD·배송비 상품 일괄수정(hidden 영역 기능), 국민비서 등록 문자(AS-IS도 CI 폼값이 없어 안 나감).

**알려진 것**: 개발 판매자 18건은 locgov_code가 없어 지자체 담당자(pwtest_loc, 11230)에겐 0건 - 검증은 pwtest_loc로 새로 등록해서 함(끝나고 삭제·시퀀스 10001 복원). `/ban-word/list-all` 404는 `_filter` 폼 공통 갭.
AS-IS CSS의 `file_camera.gif` 경로(`/images/btn/`)는 AS-IS에도 404(파일은 `/images/icon/`에만) - 임의로 복사하지 않음.
테스트 로그인은 `#op_username`+`idLogin()` 방식([[admin-categories-team-group-port]] 참고). admin+gift 재기동 필요. 관련 [[admin-categories-port]], [[gift-seller-status-asis-codes]].
