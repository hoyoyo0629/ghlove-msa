# 업로드·파일처리 AS-IS 전수 대조 (2026-10-06)

## 왜 이 문서가 생겼나

팝업관리(1311) 점검 중 사용자가 지적: **"as-is 서비스로직을 메서드 단위까지 상세하게 확인하고
이식하기로 한거 아니었어? 왜 자꾸 이런 문제가 생기는거야?"**

원인은 내 절차 위반이다. 나는 화면 이식을 **JSP + 매퍼 SQL** 기준으로 맞추고
`*ServiceImpl` **메서드 본문을 끝까지 읽지 않았다**. 업로드 검증·조건부 정리·파일 삭제는
**매퍼에 흔적이 전혀 없고 서비스 계층에만 있다**. 그래서 화면마다 같은 누락이 반복됐다.

확정된 기준(사용자 지시 2026-10-06): **JSP + 매퍼 SQL + 서비스 로직 3종 전부.**

이 문서는 그 기준으로 **AS-IS 업로드 지점 전수**를 뽑아 TO-BE와 대조한 결과다.
(AS-IS 탐색 근거: `grep -rn "AVAILABLE_EXTENSION *=\|availableExtensions *=" --include=*.java`
→ 30개 지점 + 화이트리스트가 없는 지점은 서비스 본문 개별 확인)

---

## AS-IS 업로드 규칙 원본 표

| AS-IS 서비스(메서드) | 확장자 | 용량 | 저장 파일명 | 기존파일 삭제 |
|---|---|---|---|---|
| `PopupServiceImpl.saveImage` | **5종** jpg gif bmp png jpeg | 5MB | `getNewFileName` | 교체·삭제 시 **O** |
| `QnaServiceImple` | **12종** jpg jpeg gif png hwp doc docx xls xlsx ppt pptx pdf | 5MB | `getNewFileName` | **O**(7곳) |
| `CmntyServiceImpl` | **18종**(16종+hwpx,7z) | 50MB | `yyyyMMddHHmmssSSS_원본명` | - |
| `ManualServiceImpl` | **16종** | 50MB | 동일 | - |
| `DataboardServiceImpl` | **16종** | 50MB | 동일 | O(1곳) |
| `LocgovDataBoardServiceImple` | **16종** | 50MB | 동일 | - |
| `MaintenanceServiceImpl` | **18종** | 50MB | 동일 | X |
| `SysNoticeSellerServiceImpl` | **18종** | 50MB | 동일 | X |
| `EmailServiceImpl` | **12종** jpg jpeg gif bmp png hwp doc docx pdf zip ppt pptx | 10MB | `CustomFileService` | - |
| `GiveOperationServiceImpl` | 12종(동일) | 10MB | `CustomFileService` | - |
| `RemittanceServiceImpl` | 16종 | - | `CustomFileService` | - |
| `SellerconfirmServiceimpl` | 16종 | - | `CustomFileService` | - |
| `CustomFileService`(공용) | **17종**(16종+hwpx) | - | 프로그램별 경로 | O |
| `LocgovServiceImpl` | 5종 jpg jpeg gif bmp png | - | - | - |
| `CategoriesFilterServiceImpl`·`StyleBookServiceImpl` | 3종 jpg jpeg png | - | - | - |
| `RepresentativeBannerServiceImpl.uploadFile` | **없음**(JSP `accept`만) | **없음** | `yyyyMMddHHmmss_pc.ext` | X (`// TODO :: 기존 파일 삭제 추가 필요`) |
| `DesignatedDonationServiceImpl.saveDesignatedDonationInfo` | **없음**(`ImageIO.read` 실패로 거름) | 없음 | `yyyyMMddHHmmssSSS_{사이즈}.ext` **사이즈별 다건** | 삭제 시 변형 3개 **O** |

---

## A. 순수 누락 — AS-IS에 있는데 TO-BE에 없었다 (2026-10-06 수정 완료)

| TO-BE | 누락 내용 | 조치 |
|---|---|---|
| `PopupImageStorageService` | 확장자 5종 검사 **전무** | 추가(AS-IS 순서: 확장자→용량, 문구 verbatim) |
| `OperationContentService.createPopup` | 이미지 저장 시 `setContent("")` 없음 | 추가 |
| `OperationContentService.createPopup` | 형태 '3' + 파일없음 → 이미지링크·배경색 `""` 없음 | 추가 |
| `OperationContentService.updatePopup` | 형태 '3'→텍스트 전환 시 **이미지·링크·배경색이 그대로 남음** | AS-IS 분기 3갈래 그대로 이식 |
| `OperationContentService.updatePopup` | 이미지 교체 시 **옛 파일이 디스크에 쌓임** | 교체 전 `delete` 추가 |
| `OperationContentService.deletePopupImage` | 컬럼만 null, **디스크 파일 잔존** | AS-IS 순서(①파일 삭제 ②컬럼 null)로 수정 |
| `OperationContentController` | 형태≠'3'인데 파일을 보내면 TO-BE는 저장했다 | AS-IS처럼 형태 '3'일 때만 저장 |
| `QnaFileStorageService` | 확장자 10종 — **xls·xlsx 빠져 엑셀 첨부가 거부** | 12종으로 교정 |
| `DataBoardFileStorageService` | 확장자 검사 전무 + 20MB(AS-IS 50MB) | 16종 + 50MB |
| `AdminFileStorageService`(운영유지관리·제공자공지) | 확장자 검사 전무 + 20MB(AS-IS 50MB) | 18종 + 50MB |

이미 맞던 것: `CmntyFileStorageService`(18종) · `ManualFileStorageService`(16종/50MB) ·
`EmailAttachmentStorageService`(12종/10MB) · `MainBannerImageStorageService`(AS-IS는 화면 JS만
검사하므로 그 목록과 동일하게 둔 것 — 주석에 사유 있음).

## B. TO-BE가 발명한 차이 — **사용자 판단 필요**

1. **대표배너 수정 시 이미지 유지 vs 비움.** AS-IS `editRepresentativeBanner`는 수정에서도
   파일을 다시 안 올리면 `setFileNamePc("")`로 **컬럼을 비운다**(제목만 고쳐도 이미지가 사라진다).
   디스크 파일은 `// TODO :: 기존 파일 삭제 추가 필요` 주석과 함께 남긴다.
   TO-BE는 "새 파일 있을 때만 교체"다. → **AS-IS 미완성 코드로 보여 현행 유지를 권한다.**
2. **대표배너 등록 시 PC·모바일 이미지 필수.** TO-BE가 만든 검증("PC/모바일 이미지를 모두
   첨부해 주세요.")이고 AS-IS는 **둘 다 없어도 통과**(빈 문자열 저장)한다.
3. **AS-IS에 확장자 검사가 없는 지점**(대표배너·특정사업 이미지·donation `FileStorageService`)에
   화이트리스트를 넣을지. 보안상 넣는 게 맞지만 AS-IS보다 엄격해지는 **의도적 편차**라 기록·승인
   대상이다(기존 승인 편차: 업로드 파일명 경로성분 제거, 사용자 입력 HTML 이스케이프).
4. **TO-BE 임의 용량 제한 20MB**(donation `FileStorageService`) — AS-IS엔 제한이 없다.

## C. 아직 구현 안 된 기능 — 특정사업(17000) 영역 잔여

내가 ①(대표배너)·③(등록/수정 폼)을 "완료"로 적었지만 **파일처리 기준으로는 미완**이다.

1. **사업 이미지가 다건 + 썸네일 사이즈별 저장이다.** AS-IS는 `prjImageFiles` 배열을 받아
   `ShopUtils.getThumbnailType()`의 사이즈마다 리사이즈해 `yyyyMMddHHmmssSSS_{사이즈}.ext`로
   저장하고 `G_DSGNCNTR_PRJ_IMG`에 **ordering과 함께 여러 행**을 넣는다. 첫 이미지를
   `PRJ_IMAGE`(목록 노출용)로 올린다. 삭제는 변형 파일 3개를 지우고 행을 삭제한다
   (`deletePrjImageById`).
   TO-BE는 **단일 이미지 1장**을 프로젝트 행의 `imageUrl`에 저장하고, TO-BE DB에 이미 있는
   **`donation.g_dsgncntr_prj_img`를 쓰지 않는다** → "AS-IS 표가 TO-BE 스키마에 이미 있는데
   안 쓰고 다른 데 저장" 안티패턴 **7번째 사례**.
2. **사업 공지사항 첨부파일 미이식.** AS-IS는 `prjNoticeFiles`를 `CustomFileService`로 저장하고
   `deletePrjNoticeFile`·다운로드까지 있다. TO-BE 공지 API는 `subject`·`content`만 받는다.
   TO-BE DB에 `g_dsgncntr_prj_notice_file`·`g_dsgncntr_prj_notice_img_desc`가 비어 있다.
3. **저장 파일명 규칙.** AS-IS는 `yyyyMMddHHmmssSSS_원본명`(자료실·운영유지관리·제공자공지·
   커뮤니티·매뉴얼)과 `yyyyMMddHHmmss_pc.ext`(대표배너)다. TO-BE는 `UUID.ext`를 쓰는 곳이 많다
   (매뉴얼만 AS-IS 규칙). 원본 파일명을 그대로 쓰면 경로성분 제거가 전제다.

## D. 다음 화면부터 쓰는 체크리스트

AS-IS 서비스 메서드(저장·수정·삭제 각각)를 읽을 때 반드시 확인한다:

1. **입력 검증** — 확장자 화이트리스트 / 용량 / 형식. 검사 **순서와 문구**까지(보통 확장자 먼저).
   AS-IS는 확장자 검사를 목록 + 파일명 `endsWith` **두 번** 하는 곳이 있다(CWE-434 주석).
2. **조건부 분기** — 타입·형태 값에 따라 **다른 컬럼을 비우는지**(팝업 형태 전환이 그 사례).
3. **파일 I/O** — 저장 경로, 파일명 규칙, **파생 파일**(썸네일 사이즈별), **기존 파일 삭제 선행**.
4. **부수효과** — 다른 표 insert/delete, 문자·메일 발송, 이력 적재, 시퀀스 채번.
5. **다건 여부** — 단건으로 보이는 화면이 실제로는 배열(`MultipartFile[]`)인지.

관련 메모리: `asis-screen-port-procedure` · `as-is-logic-is-the-spec` ·
`as-is-parity-exhaustive-audit-method` · `copy-as-is-verbatim-never-invent`

---

## E. 2026-10-06 2차 - 컨트롤러 단위 누락과 업로드 상한 (사용자 지적으로 추가)

사용자 지시로 기준이 **"JSP + 매퍼 SQL + 서비스 로직 + 컨트롤러"** 4종, 각각 **메서드 단위**가 됐다.

### E-1. 컨트롤러가 통째로 빠져 있었다 - 스마트에디터 팝업 3종
스마트에디터 툴바의 **사진 / 동영상 / CTP** 버튼은 AS-IS
`saleson/common/module/smarteditor/SmartEditorController`(엔드포인트 5개)를 부른다.
TO-BE에 **이 컨트롤러가 아예 없어서** 세 버튼이 모두 오류였다. 메뉴 트리에 없는 공통 모듈이라
화면 단위 이식 목록에 잡히지 않았다 → 영역 이식 전에 **AS-IS 컨트롤러 메서드 전수 체크표**를
만들어야 한다(공통/모듈 컨트롤러 포함: 에디터·주소검색·파일다운로드·팝업결과).

이식한 AS-IS 규칙(컨트롤러 본문 기준):
- 이미지: `file[]` 다건, **jpg·jpeg·png·gif만**(아닌 건 조용히 버림), **건당 5MB** 초과는
  파일명을 모아 "… 파일은 용량 제한을 넘어 업로드에 실패했습니다."
- 결과 문서는 HTML을 **이스케이프**해 심고 JS가 DOMParser로 되돌려 에디터에 넣는다(왕복 재현 필수)
- 동영상: **업로드가 아니다**. youtu.be → youtube.com/embed 치환 + 반응형 iframe(서버 저장 없음)
- CTP: html·ctp·css·jpg·jpeg·png·gif / 5MB / **html(ctp) 1개 이상 필수**,
  `<body>` 안쪽만 추출 + 상대경로 이미지 치환, 오류는 ERROR_01/02/03

검증(여분 포트 8099 + 실제 업로드): 이미지 정상·비이미지·5MB초과·다건혼합 4종,
CTP 정상(body추출+경로치환)·ERROR_01·02·03 4종 **전부 AS-IS대로 확인**, 파일 디스크 저장 확인.

### E-2. ★업로드 상한이 설정되지 않아 1MB 넘는 모든 업로드가 413이었다
`spring.servlet.multipart`가 **admin·donation·member·order·point에 아예 없었다** → 스프링 기본
**파일 1MB / 요청 10MB**. 그래서 서비스 로직의 50MB·10MB·5MB 규칙에 **도달 자체가 불가능**했다
(매뉴얼·자료실·커뮤니티 50MB, 이메일 10MB, 팝업·Q&A·배너·에디터 5MB 전부). gift만 10MB/30MB로
근거 없는 값이 들어 있었다.
→ AS-IS `application-production.yml`과 같게 **50MB / 50MB**로 맞췄다(AS-IS 개발 프로필은 20MB지만
그 값으로는 50MB 규칙에 못 닿는다). admin·donation·member·gift 적용, order·point는 업로드가 없다.

### E-3. 공용 저장 서비스의 화이트리스트를 그대로 쓰면 안 되는 경우
`AdminFileStorageService`의 기본 18종에는 **html·ctp·css가 없어** CTP 업로드가
"유효하지 않은 파일입니다."로 터졌다 → 호출부가 허용목록·용량을 넘기는 형태
(`store(file, subdir, allowedExtensions, maxSize)`)를 추가했다. **화면별 AS-IS 목록이 다르다**는
것을 전제로 쓸 것.
