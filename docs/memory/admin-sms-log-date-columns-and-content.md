---
name: admin-sms-log-date-columns-and-content
description: "문자전송이력 목록의 생성일시/전송시작일시/전송완료일시 컬럼매핑 오류 + 발송내용/오류내용 렌더링을 AS-IS와 동일하게 수정(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

[[admin-sms-log-phone-status-display]]에 이어 문자전송이력(`/admin/send-sms-logs`) 나머지
컬럼도 AS-IS 소스(`ghlove-web`의 `SmsController`+`sms-log/list.jsp`, `ghlove-common`의
`TifIpsSndngMDisplay`/`SmsIpsServiceImpl`/`sms-mapper.xml`)와 전수 대조했다.

**1. 날짜 3컬럼 - 필드 자체가 잘못 매핑돼 있었다(포맷 문제가 아니었다)**

AS-IS JSP 바인딩: "생성일시"→`esbInitTimeF`, "전송시작일시"→`esbTxTimeF`, "전송완료일시"→
`esbComptTimeF`(전부 `DateUtils.datetime()`로 `yyyyMMddHHmmss`→`yyyy-MM-dd HH:mm:ss` 포맷).
`INFO_CRT_DT`는 이 화면에 전혀 안 쓴다(매퍼 SELECT 목록에도 없음) - 적재 시 공통값으로만
기록되는 컬럼이다.

TO-BE는 "생성일시"에 `infoCrtDt`(완전히 다른 컬럼), "전송시작일시"에 `esbInitTime`(한 칸씩
밀림), "전송완료일시"에 `esbComptTime`(우연히 맞음, 포맷 없음)을 쓰고 있었다 - 컬럼 자체가
밀린 버그. `esbTxTime` 필드는 엔티티에 있는데 화면에서 전혀 안 쓰이고 있었다.

수정: 템플릿을 `@opDate.ymdHms(item.esbInitTime)` / `@opDate.ymdHms(item.esbTxTime)` /
`@opDate.ymdHms(item.esbComptTime)`로 교체. 안 쓰던 `IpsSendingMaster.getInfoCrtDtText()`는
삭제(AS-IS도 이 화면에 안 쓴다).

**검색 날짜 필터도 틀려 있었다**: AS-IS는 "생성일자" 라벨이지만 실제로는
`ESB_INIT_TIME BETWEEN CONCAT(start,'000000') AND CONCAT(end,'999999')`로 거른다(매퍼
그대로). TO-BE `SmsIpsService.search()`는 `infoCrtDt`(LocalDateTime)로 거르고 있었다 -
화면에 안 보이는 컬럼으로 검색하던 셈. `search()` 시그니처를 `LocalDateTime`에서
`String searchStartDate/searchEndDate`(yyyyMMdd)로 바꾸고 `esbInitTime` 문자열 BETWEEN
비교로 교체했다. 컨트롤러의 `startOfDay`/`startOfNextDay`/`parse` 변환 헬퍼는 제거(더 이상
필요 없음).

**검색내용(이름,전화번호 등) 필터도 과했다**: AS-IS 매퍼는 `SNDNG_CNTNTS`만 LIKE한다
(`PRVC_IDNTFC_INFO` 조건 없음). TO-BE는 `prvcIdntfcInfo`(CI)까지 같이 LIKE하고 있었다 -
AS-IS에 없는 조건이라 제거.

**2. 발송내용 - 원시값이 아니라 렌더링된 알림문구였다**

AS-IS "발송내용" 컬럼은 `SNDNG_CNTNTS`(파이프 구분 원시값, 예:
`정철교|2023.06.23...|K0000006106|산채냉면 혼합세트|롯데택배|01090293686`) 그대로가 아니라
`TifIpsSndngMDisplay.getSmsContent()` - 문자종류(SVC_ID→SmsType) 24종별 완전히 다른 한국어
안내문구 템플릿에 파이프 항목을 꽂아 넣은 결과를 20자로 잘라 보여준다(전체는 title 툴팁).
JOIN_MEMBERSHIP/OVERPAYMENT/OVERPAYMENT_CANCEL 3종은 `donationVerification.donationLimitAmt()
.getDetail()`(공통코드 `DONATION_LIMIT_AMT`의 올해 DETAIL, 예: "2천만")도 끼워 넣는다.

TO-BE는 `sndngCntnts` 원시값을 그대로, 자르기도 없이 보여주고 있었다 - 완전히 다른 내용.

수정: AS-IS `getSmsContent()`의 24개 분기를 그대로 포팅한
`SmsContentRenderer`(`@Component("smsContent")`, Thymeleaf에서 `@smsContent.render(svcId,
sndngCntnts, limitAmtString)`로 호출)를 신설. `limitAmtString`은
`SmsIpsService.donationLimitAmtDetail()`(공통코드 `DONATION_LIMIT_AMT` + 올해 연도 id 조회,
이미 2024~2030년치가 적재돼 있음 - [[admin-common-code-asis-sync-2026-10-07]])로 구해 모델에
담아 템플릿에 전달.

**3. 오류내용 - 20자 자르기+툴팁 누락**

AS-IS는 `esbErrMsg`도 20자로 자르고 전체는 title 툴팁(발송내용과 동일한 `op:strcut` 패턴).
TO-BE는 자르기 없이 전체를 그대로 찍고 있었다 - 수정.

**새 헬퍼 `TextCut`(`@Component("strcut")`)**: AS-IS EL 함수 `op:strcut`
(`com.onlinepowers.framework.util.StringUtils.strcut`) 재현. ⚠️ **프레임워크 원본 소스가
이 저장소에 없다**(`ghlove/build.gradle`의 `includeBuild '../opframework'`가 주석처리돼
빠져 있어 `DateUtils`/`StringUtils` 등 진짜 구현을 못 봤다) - "N자 초과 시 잘라서 '...' 붙이기"
라는 가장 흔한 규칙으로 재현했을 뿐, 바이트 단위 처리 등 디테일은 확인 못했다. 나중에
`opframework` 소스를 구하면 재검증 필요.

**검증**: `SmsContentRendererTest`(PRESENT_SHIPPING·JOIN_MEMBERSHIP 렌더링, 알 수 없는
코드/빈 값/깨진 파이프 항목에서 예외 대신 빈 문자열), `TextCutTest`, 그리고
`SmsIpsServiceTest.donationLimitAmtDetailReadsSeededCommonCode`(실제 DB에서 "2천만" 조회
확인).

**4. 검색창 레이아웃 + 버튼 색상 (2026-10-07 추가)**

AS-IS는 4열 테이블(`<colgroup>` 220px/auto/220px/auto)로 "문자 구분"+"검색내용"을 한 행에
나란히 배치하고, "생성일자" 행은 `colspan="3"`으로 합쳐서 datepicker 2개 + day_btns(오늘/1주/
1개월/3개월/1년 바로가기, `Common.DateButtonEvent`/`EventHandler.calendarStartDateAndEndDateVaild`
로 동작)까지 포함한다. TO-BE는 2열 3행으로 전부 세로로 쌓여 있었고 day_btns도 없었다 - 구조를
AS-IS대로 재구성.

초기화 버튼도 AS-IS는 `btn btn-default`(검색 버튼과 다른 옅은 색)인데 TO-BE는 검색 버튼과 같은
`btn-dark-gray`를 쓰고 있었다 - `btn-default`로 교정(AS-IS sms-log/list.jsp 원본 class 그대로;
단 exceldownload-log-list.jsp처럼 AS-IS 안에서도 화면별로 버튼 색 조합이 다른 경우가 있어
화면마다 그 화면 자신의 AS-IS JSP를 봐야 한다).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**
