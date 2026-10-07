---
name: admin-log-screens-asis-sample-data
description: "로그 조회 화면 6개(엑셀다운로드사유/서울부과·수납/지방부과·수납/문자전송이력) 빈 테이블에 AS-IS 실데이터 샘플 20~30건씩 적재(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

시스템관리의 로그 조회 화면들이 테이블이 비어 있어 화면 검증이 안 된다는 지적으로, AS-IS
export(`Desktop\고향사랑e음\1.AS-IS\1. DB\_<table>__202610071247.sql`)에서 각각 20~30건씩
샘플을 뽑아 적재했다. 원본 파일들은 수백만~수십억 행(일부는 2.9GB)짜리 전체 운영 로그라
전부 옮기지 않고, 정규식으로 "한 줄짜리 완전한 INSERT 튜플"만 골라 그대로 복사해 썼다(한글
재입력 없음 - 이중 인코딩 위험 회피, [[perl-double-encoding-when-generating-files]] 참고).

**적재 내역:**
| 화면(메뉴) | 테이블 | 건수 | migration 파일 |
|---|---|---|---|
| 엑셀다운로드사유관리(1411) | admin.op_privacy_access_log (task='엑셀 다운로드') | 30 | migration-admin-privacy-access-log-asis-sample.sql |
| 서울부과로그관리(1413) | donation.gif_seoul | 25 | migration-donation-gif-seoul-asis-sample.sql |
| 서울수납로그관리(1414) | donation.gif_etax_sunap | 25 | migration-donation-gif-etax-sunap-asis-sample.sql |
| 지방부과로그관리(1415) | donation.g_next_buga_request | 24(기존시드 1건과 중복 1건 스킵) | migration-donation-g-next-buga-request-asis-sample.sql |
| 지방수납로그관리(1416) | donation.g_next_sunap_response | 25 | migration-donation-g-next-sunap-response-asis-sample.sql |
| 문자전송이력(7208) | admin.tif_ips_sndng_m | 25 | migration-admin-tif-ips-sndng-m-asis-sample.sql |

**스킵한 화면**: 배치 실행로그 조회(7209, admin.op_batch_execution)는 이미 8건이 있어서
"없으면"이라는 사용자 조건에 해당하지 않아 손대지 않았다. 필요하면 별도로 요청할 것.

**암호화 컬럼 주의**: `ip`(op_privacy_access_log)·`prvc_idntfc_info`(tif_ips_sndng_m)·
`nap_nm` 일부 행(gif_seoul)은 AS-IS가 pCrypto로 암호화한 원문 그대로다. TO-BE는 복호화
모듈이 없어 화면에 암호문(`^`...==` 형태)으로 보인다 - 버그 아니라 기존에 이미 알려진
제약([[admin-excel-download-log-500-fix]]와 무관, 각 엔티티 주석에도 명시돼 있다).

**[2026-10-07 수정] manager_id를 테스트계정으로 변경함**: 적재 직후엔 관리자ID가 안 보여서
사유 수정팝업의 succChk(본인 작성글만 수정 가능)를 테스트할 방법이 없었다. 엑셀다운로드사유
30건 전부 `manager_id`를 사용자의 테스트계정 `admin01`(user_id=1000)로 UPDATE했다(DB 직접,
`UPDATE admin.op_privacy_access_log SET manager_id = 1000 WHERE task = '엑셀 다운로드'`) -
그 결과 관리자ID 칸도 "admin01"로 정상 표시되고, admin01로 로그인하면 전부 본인 글이라 수정
가능 상태로 보인다. 서울/지방 부과·수납·문자전송이력은 manager_id 개념이 없는 표라 해당 없음.

**상태**: 전부 DB 직접 적재(docker exec psql), 코드 변경 없음. **재기동 불필요** - 각 화면이
매 요청마다 DB를 조회하는 구조라 바로 보인다.
