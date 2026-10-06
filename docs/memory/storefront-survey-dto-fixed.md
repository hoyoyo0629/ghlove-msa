---
name: storefront-survey-dto-fixed
description: storefront 설문참여 화면이 보내던 키가 API와 안 맞아 응답이 빈 값으로 저장되던 버그 - 2026-10-03 수정 완료
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-03T06:23:46.088Z
---

**해결됨(2026-10-03).** [[admin-system-area-port-progress]]의 설문관리(7207)에서 데이터모델을 AS-IS(`G_QESTNAR` 계열)로 교체했는데 storefront(Vue3 SPA) `src/views/SurveyView.vue`가 따라오지 않아 **응답이 빈 값으로 저장되고 있었다**:

- SPA가 보내는 키는 `rspnsCn`인데 API(AS-IS `QustnrRspnsResult`)가 받는 키는 `qustnrIemSn`/`respondAnswerCn`이라 **서버에서 조용히 버려졌다**(에러도 안 났다 - 행은 `qustnr_iem_sn=0, respond_answer_cn=null`로 쌓인다). 에러보다 나쁜 데이터 손실이었다.
- 모든 문항을 textarea로 그려서 **객관식 선택지가 아예 안 보였다**.

수정 내용:
- 문항 종류(`qestnTyCode`)대로 `rtype`=객관식(선택지 라디오) / `stype`=주관식(textarea). `answer_choise_co`는 AS-IS 관리자 폼도 채우지 않아 사실상 단일선택이라 라디오가 맞다.
- payload를 AS-IS 필드명(`qustnrQesitmSn`/`qustnrIemSn`/`respondAnswerCn`)으로 바꾸고, **답하지 않은 문항은 보내지 않는다**(빈 행 방지). 전부 비면 "응답을 입력해 주세요."
- 연계질문(`parentSn`)은 부모 아래 들여써 보여준다. **AS-IS가 부모 답에 따라 조건부로 펼치는지는 확인 못 했다** - 그 프론트(`qustnr/detail_srvy.html`, 별도 Node 앱 localhost:3000)가 AS-IS 저장소에 없다. 지금은 항상 노출.
- `ETC_ANSWER_CN`(기타 답변)은 AS-IS DTO·컬럼에 있지만 입력 UI 근거를 못 찾아 두지 않았다(null 기록).

**★같이 잡은 서버 버그 - 네이티브 쿼리 리터럴은 `(String)` 캐스팅하면 터진다**: 재기동 후 `/api/surveys/active`가 **500**이었다 - `QestnarRepository.toQestnarRow`에서 `ClassCastException: Character cannot be cast to String`. 원인은 `CASE WHEN ... THEN 'Y' ELSE 'N' END as is_show`처럼 **SQL 리터럴에서 나온 값**을 PostgreSQL이 `bpchar`로 정하고 Hibernate가 길이 1이면 `Character`로 돌려주기 때문. 표가 0행인 동안은 드러나지 않았다. → `asString(Object)`(toString) 헬퍼로 받도록 고쳤고 `BatchLogRepository`(결과 라벨 CASE)도 같이 손봤다. **네이티브 쿼리에서 계산된 문자 컬럼은 앞으로도 `(String) r[n]` 금지.** 전 서비스 grep 결과 단일문자 CASE 리터럴은 이 두 곳뿐이었다.

검증: `npm run build` 통과. 재기동 후 `/api/surveys/active` **200** + DTO 모양 확인(choices 채워짐, 문항4의 parentSn=3). 표가 0행이어서 화면 확인이 불가했으므로 **검증용 설문 시드**를 넣었다 - `database/ddl/seed-admin-survey-sample.sql`(QUSTNR_SN=9001, 객관식 4선택지 + 주관식 + 객관식 3선택지 + 그 연계질문, 노출기간은 어제~한달뒤로 **오늘을 포함**해야 이용자 화면에 보인다, SRVY_TRGT='U'라야 `/api/surveys/active`에 걸린다). `qustnr_iem_sn`은 **문항 안에서만 유일**하다(문항1은 1~4, 문항2는 1) - 라디오 name을 문항별로 묶어야 한다.
