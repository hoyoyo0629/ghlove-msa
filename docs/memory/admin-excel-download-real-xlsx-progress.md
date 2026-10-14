---
name: admin-excel-download-real-xlsx-progress
description: "admin의 '엑셀 다운로드'가 CSV가 아니라 AS-IS처럼 진짜 암호화된 .xlsx가 되도록 교체하는 작업 - 2/7 완료, 나머지 진척상황과 재개 체크리스트(2026-10-08)"
metadata:
  node_type: memory
  type: project
---

**배경**: 오프라인담당자 목록의 "전체 목록 엑셀 다운로드"가 CSV를 내려주고 있다고 사용자가
지적(AS-IS는 Apache POI로 만든 진짜 .xlsx + 비밀번호암호화). 확인해보니 admin 전체에서
**7개 화면이 전부 CSV**였고, 그 중 3개(답례품목록→gift, 주문목록→order, 답례품리뷰→gift)는
admin이 아니라 **다른 서비스를 호출**해서 그 서비스가 CSV를 만들어 돌려주는 구조다.
사용자 결정: "공통 유틸 만들어서 7곳 전부, admin부터 확인하고 문제없으면 나머지 3개
서비스도." 비밀번호는 AS-IS `saleson.common.file.ExcelDownloadView`가 다운받는 관리자의
**로그인ID 그대로**(변환 없음)를 Apache POI agile 암호화로 적용한다.

**★핵심 교훈(2026-10-08) - AS-IS에 클래스가 있다고 그게 실제로 쓰인다는 뜻이 아니다**:
`*ExcelView`(AbstractSXSSFExcelView+HeaderCell 패턴) 클래스 이름으로 먼저 조사했는데,
7개 화면 중 **최소 3개(OffPersonInChargeExcelView, OffgiveExcelView, "MaintenanceExcelView"
는 존재 자체가 없음)가 죽은 코드**였다 - 실제 컨트롤러는 그 클래스를 전혀 생성하지 않고,
`*ServiceImpl.stream*()`가 `SXSSFWorkbook`을 직접 만들어 공용 `ExcelDownloadView`(암호화+
스트리밍만 담당)로 내려보내는 "새 패턴"을 쓴다. 두 패턴의 제목·시트명·컬럼폭·번호매김
방향(오름차순 vs totalCount 역순)·일부 컬럼 조립 로직(HTML 비이스케이프, 마스킹 유무)이
서로 다르다. 사용자 지시: "당연히 죽은코드가 아닌 살아있는 코드를 기준으로 동일하게
구현해야지." → **이후 모든 작업은 반드시 컨트롤러에서 실제로 생성하는 클래스/메서드를
그렙으로 확인하고 시작할 것.**

**완료(2/7, admin 내부)**:
1. **오프라인담당자**(`OffPersonInChargeAdminController.downloadExcel`) - 실제 살아있는
   `PersonInChargeServiceImpl#streamChargerData` 기준으로 구현. 시트 "CHARGER_DATA", 제목
   "오프라인 회원 목록", 컬럼폭 {2000,3000,10000,3000,3000,5000,3000,5000}, 번호는
   **totalCount에서 역순**(올림차순 아님), 소속 지점은 `bankNm==null?"-":HTML비이스케이프(+
   psitnNm)`.
2. **기부혜택증 열람현황**(`LclgvHnrUserAdminController.exportViewHistory`) - 이 화면은 드물게
   `LclgvHnrUserViewHistExcelView`가 **진짜로 살아있는** 클래스였다(검증됨). 시트
   "honorViewHist", 제목 "기부혜택증 열람현황", 6컬럼, 번호는 오름차순, 열람횟수는 천단위
   콤마.

공통 인프라(둘 다 이걸 쓴다):
- `admin/build.gradle`에 `org.apache.poi:poi:4.1.2` + `poi-ooxml:4.1.2`(AS-IS와 같은 버전,
  jar 복사 아니라 일반 Gradle 의존성 - AS-IS도 Maven Central에서 받는다, vendor jar 아님).
- `web/support/ExcelDownloadWriter.java`(신규, `@Component`) - AS-IS
  `saleson.common.file.ExcelDownloadView`(암호화+스트리밍) +
  `saleson.common.file.ExcelCellStyleUtils`(title/header/data 셀 스타일, 회색25% 헤더배경
  등 실제 값 그대로) 재현. `createStyles()`/`writeTitleAndHeaderRow()`/`dataCell()`/
  `write(response, workbook, fileName, password)` 4개 공개 메서드.

3. **기부금 접수관리(오프라인기부접수, 15101)** - 2026-10-14 완료. `OffgiveReceiptAdminController.downloadExcel`이
   `streamOffgiveList` 그대로(시트 OFFGIVE_DATA, 제목 "오프라인 기부금 접수 관리 목록", 폭 {2000,7500,3000,7500,5000,7500,
   5000,5000,7500,7500,7500}, No 역순 콤마, 이름 비마스킹, 수납일·금액 원값, 신고일은 Timestamp 문자열 "…:56.0",
   지자체/지점은 Java 문자열 더하기라 null이면 "null"). 화면은 신청일 확인 alert 후 privacy-access 모달(openPrivacyModal).
   같은 날 목록 SQL도 AS-IS getOffgiveList로 재작성(경로 200·300, 사업명 g_dsgn_dntn_biz_mng+'자치단체에 기부하기',
   일치검색, 복지센터명, delete_at 조건 제거, role 전달). 복호화 검증법: admin bootJar의 BOOT-INF/lib POI로 javac 소도구.

**미완료 - 컬럼 자체가 부족함이 추가로 발견됨(파일형식 문제보다 큰 갭)**:
- **운영유지관리**(`MaintenanceAdminController`) - 지금 CSV는 7컬럼(접수일/제목/요청경로/
  업무구분/완료구분/담당자/처리내용)뿐인데 실제 AS-IS(`MaintenanceServiceImpl#
  streamAllMaintenanceData`)는 **15컬럼**(작성자/CSR번호/내용/답변/등록일시/수정일시/
  처리시작일/처리종료일 등 추가). `GSrMaintenance` 엔티티엔 원본 데이터 대부분 이미 있다
  (`srNo`/`bbsCn`/`processCn`/`frstCrtDt`/`lastMdfcnDt`/`processStartDate`/
  `processEndDate` 등 - CSR번호/내용/답변/등록일시/수정일시/처리시작일/처리종료일은 그대로
  매핑 가능). **확인 필요한 것 2가지**: ① "작성자"(AS-IS `reqManagerNm`)에 대응하는 필드가
  엔티티에 안 보임(조인/매핑 추가 필요할 수 있음) ② 업무구분/처리구분/완료구분/요청경로는
  AS-IS가 코드값→라벨로 변환해서 보여주는데 지금 엔티티엔 원본 코드값(`reqType`/
  `processType`/`processState`/`reqChannel`)만 있어 라벨 매핑(공통코드?) 추가 필요.
  **참고**: AS-IS `listMaintenance()`(화면·엑셀 공용)가 `bbsCn`/`processCn`을 HTML태그
  제거+100자 자르기 하므로, 엑셀도 똑같이 잘린 텍스트가 들어간다(버그 아니라 AS-IS 그대로).
- **오프라인기부접수**(`OffgiveReceiptAdminController`) - 아직 구조 조사 전. 실제 살아있는
  코드는 `OffgiveServiceImpl#streamOffgiveList`(11컬럼: No/접수번호(지자체)/기부상태/
  전자납부번호/수납일/기부 지자체/이름/기부 금액/지점·센터명/신고일/특정사업에 기부하기
  사업명) - 죽은 `OffgiveExcelView`(10컬럼, 이름마스킹 "홍*동")와는 전혀 다르다. **이름
  마스킹은 쓰지 않는다**(사용자 확인: 살아있는 코드 기준). 번호도 totalCount 역순, 정렬은
  `FRST_REGIST_PNTTM DESC`(최신 신고가 먼저 조회되지만 번호는 역순이라 가장 오래된 게 1번).
  롤 기반 스코프 제한 있음(ROLE_ADMIN_7/8은 자기 지점만).

**사용자 결정(2026-10-08)**: "AS-IS 전체 컬럼으로 확장하고 동일하게 맞추자" - 토큰 여유
부족으로 이번 세션엔 착수 안 함, **다음 세션에서 재개**. 재개 시 순서:
1. 운영유지관리: "작성자" 필드 출처 확인(엔티티에 없으면 AS-IS 매퍼/서비스에서 조인
   로직 찾기) + 4개 코드값→라벨 매핑 테이블/방식 확인 → 15컬럼으로 재작성.
2. 오프라인기부접수: `OffgiveServiceImpl#streamOffgiveList`/`OffgiveManagerController
   #downloadExcelProcessByLocgovListPrj` 전체를 읽고 11컬럼 그대로 재작성(TO-BE
   `OffgiveReceiptAdminController`의 현재 데이터 모델이 이 11개를 다 갖고 있는지부터 확인).
3. 둘 다 끝나면 admin 7개 중 4개 완료 → 사용자 확인 후 gift·order 서비스(답례품목록/
   주문목록/답례품리뷰, 3곳)로 같은 작업 확장.

**상태**: admin `compileJava+test+bootJar` EXIT=0(완료된 2곳만 반영, 나머지 2곳은 아직
CSV 그대로). **재기동 필요: admin**(완료된 2화면만 테스트 가능).
