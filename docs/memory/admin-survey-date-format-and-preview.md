---
name: admin-survey-date-format-and-preview
description: "설문관리(1409 아님 7207) 목록 기간 날짜 포맷 + 미리보기 링크 AS-IS 동일화(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

**기간 날짜 포맷**: `G_QESTNAR.QUSTNR_BGN_DE/QUSTNR_END_DE`는 AS-IS·TO-BE 둘 다 `VARCHAR(8)`
(`yyyyMMdd`)로 저장한다. AS-IS `qustnr-mapper.xml`의 `getQustnrList`는 `DATE_FORMAT(...,
'%Y-%m-%d')`로 포맷해서 내려주는데, TO-BE `QestnarRepository.getQustnrList`는 포맷 없이 원본을
그대로 돌려줬고 템플릿도 그대로 찍어서 "20260803" 식으로 보였다(AS-IS는 "2026-08-03"). SQL을
건드리지 않고 이미 프로젝트 전역에 쓰는 [[admin-date-format-opdate-helper]] 패턴대로
`survey-admin/list.html`에서 `${@opDate.ymd(item.qustnrBgnDe)}`로 템플릿 레벨에서 포맷했다
(8자리 숫자 문자열을 받는 `ymd()`가 그대로 맞는다 - 이 헬퍼는 길이로만 분기해서 14자리
`yyyyMMddHHmmss`든 8자리 `yyyyMMdd`든 다 처리된다). 참고로 수정/상세 폼의 `getQustnr`은
AS-IS도 포맷 없이 원본을 그대로 내려줘서(`qustnr-mapper.xml` 141번 줄) datepicker 입력값이
AS-IS도 "20260803"으로 보인다 - 이건 AS-IS 자체 동작이라 안 건드렸다(목록만 지적받음).

**미리보기**: 이전 TO-BE `preview(qustnrSn)`는 `/api/surveys/{id}`(JSON API 원본)을 새 창으로
열어서 미리보기가 아니었다. AS-IS는 `saleson.url.frontend + /qustnr/detail_srvy.html?qustnrSn=`을
연다(별도 프론트의 실제 설문 참여 화면). TO-BE storefront에 이미 그 역할의 실화면
(`storefront/src/views/SurveyView.vue`, 라우트 `/survey/:id`)이 있어서 `preview()`가 그
URL(`http://localhost:5173/survey/{id}`)을 열도록 고쳤다. 절대URL 하드코딩은
[[production-domain-migration-plan]]의 기존 29곳과 같은 유형(서비스마다 오리진이 달라 당장은
상대경로가 안 됨) - 그 계획이 진행되면 같이 정리된다.

**상태**: 템플릿만 수정(Java 변경 없음), admin compileJava+test+bootJar 통과. **재기동 필요: admin.**
