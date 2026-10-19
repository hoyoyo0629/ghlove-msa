---
name: admin-representative-banner-port
description: 답례품관리>대표배너관리(16251) AS-IS 이식 - G_REPRST_BANNER PROCESS_TYPE=ITEM, 지정기부 배너와 서비스 공유, 옛 /admin/main-banners 삭제, 이미지 크기 맞춤(ThumbUtils) 추가
metadata:
  type: project
---

2026-10-16 이식. AS-IS `opmanager/representative-banner/list`(메뉴 16251, 권한 ROLE_ADMIN_1~4). 같은 테이블 `G_REPRST_BANNER`를 **PROCESS_TYPE**으로 나눠 쓰는 두 화면 중 하나다:
ITEM(이 화면, AS-IS 4건) / DESIGNATED_DONATION(특정사업 기부 배너관리 17301, `/designated-projects/banners`, AS-IS 5건). AS-IS JSP 두 벌은 URL만 다르다(diff 확인).

**구성**: admin `ItemRepresentativeBannerController`(`/representative-banner`: list·form/{id}(0=등록)·change-ordering·delete) + `templates/representative-banner/list|form.html`(지정기부 템플릿을 URL만 바꿔 복사, 등록모드 판단은 AS-IS 그대로
`location.href.includes("/0")`) + **기존 `RepresentativeBannerService`를 PROCESS_TYPE 파라미터로 일반화**(`list(String)`, `create(processType,…)`, 상수 `PROCESS_TYPE_ITEM`). 메뉴 URL `/representative-banner`
(`database/ddl/migration-admin-menu-16251-representative-banner.sql` - **적용 필요**). 지정기부 컨트롤러·템플릿은 그대로.

**삭제한 것**: 같은 화면의 TO-BE 축소 구현(`MainBannerAdminController`, `MainBannerRepBannerService`, `templates/main-banner-admin/*` - 노출순서 입력칸·토글/삭제 버튼 등 AS-IS에 없던 것, 5MB 한도). 아무도 안 썼다.
(`MainBannerImageStorageService`는 운영콘텐츠 쪽이 써서 그대로.)

**AS-IS 특징(그대로)**: 목록 항상 15행(빈 행엔 [등록] → form/0), 순서는 select(1~15)+저장 시 필수·중복 검사, 사용여부 "사용/중지"; 등록 순서는 `MAX(DISPLAY_ORDER)+1`을 **PROCESS_TYPE 무관 테이블 전체**로 센다(그래서 ITEM 첫 배너가 2번부터 시작);
"배너 설명"은 별표가 없지만 textarea에 required 클래스가 붙어 validator가 필수로 막는다; 이미지 X는 배너가 아니라 파일명만 비움; 수정은 새 파일을 안 올리면 기존 유지; 저장은 시스템 관리자(SYS)만(JS alert), 서버 검사 없음.

**이번에 추가한 것(지정기부에도 적용됨)**: AS-IS `ThumbUtils.create`는 업로드 이미지를 PC 1280×400 / 모바일 768×280으로 **비율 무시하고 맞춰** 저장한다 - TO-BE 서비스엔 그게 없었다. `fitToSize`로 추가(RGB로 저장, 투명 PNG는 검게, 읽을 수 없는 pdf 등은 원본 유지).
저장 파일명은 TO-BE 관례(UUID, DB엔 `/uploads/rep-banner/…` 경로) 그대로 - AS-IS의 `yyyyMMddHHmmss_pc.ext`·`/upload/representativeBanner/{id}/`와 다르다.

**안 한 것/알려진 것**: AS-IS ITEM 실데이터 4건(제철식품관·특산물관·지역이벤트·원산지 표기)은 적재하지 않았다 - 이미지 원본이 없어 미리보기가 깨지고 지정기부 5건도 같은 이유로 안 올렸다(결정 필요).
`file_camera.gif` 404·`/ban-word/list-all` 404는 AS-IS/공통 갭. 등록·수정 완료 안내는 TO-BE 공통 모달(`fragments/alert`)이라 확인 버튼을 눌러야 화면이 풀린다(Playwright에서 `.gh-alert-ok` 클릭 필요).
검증 중 `admin/uploads/admin/rep-banner/*`를 통째로 지웠다가 추적 중이던 파일 2개는 git으로 복구, 추적 안 되던 테스트 이미지 2개(1004번 행 참조)는 같은 이름으로 대체 이미지를 다시 넣었다 - **업로드 폴더는 내가 만든 파일만 지울 것**.
admin 재기동 필요. 관련 [[admin-designated-donation-area-port-progress]], [[admin-categories-port]].
