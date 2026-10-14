---
name: integration-bypass-local-only
description: "외부연계는 실경로=AS-IS 그대로, 우회=로컬에서만. 실명인증·행정주소는 요청URL localhost(AS-IS와 같은 조건·값), 세외수입 부과는 donation local 프로파일(AS-IS ServiceType.LOCAL). 다른 연계는 아직 단일설정 false"
metadata:
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T05:50:12.161Z
---

2026-10-13 사용자 질문 "연계기능들 as-is랑 서비스로직은 동일하게 구현하되 로컬테스트에서만 우회하게 수정한거 맞지?"에
"아니요"(단일 설정 enabled=false라 운영에서도 우회, SCI·외국인주소는 실경로가 스텁)로 답하고 15102 연계 3종을 고쳤다.

**기준**:
- 실명인증(SCI): AS-IS sci-call.jsp/sci-result.jsp 그대로 - `admin/libs/sciSecu_v2.jar`(AS-IS 벤더 jar 복사)로
  암호화 폼 자동제출 → `/offgive/sci-result`(로그인 인터셉터 제외, AS-IS isManagerPage 예외와 같음).
  우회 = **요청 URL에 localhost** → MOCKCI/MOCKDI+주민번호(사용자가 AS-IS에 넣게 한 우회와 같은 조건·값).
- 행정정보공동이용센터: 내국인(admin-addressinfo url)·외국인 거소(in-link-url + /api/mopas/rsgstadresinfoForeigner,
  getForeignApiData 전체 이식, G_ADM_LOCGOV 매핑은 donation `/api/offgive/adm-locgov-code`). 우회 = localhost, 경기도 시흥시.
- 세외수입 부과(nextBuga/서울 getSdonationCharge): donation `spring.profiles.default: local` + `LocalTaxClient.serviceTypeLocal`
  (= AS-IS ServiceType.LOCAL 가짜 전자납부번호 "99"+시각). 운영은 `--spring.profiles.active=production` 등으로 띄워야 실호출.
  특정사업(prjId) 기부가능 검증·DTO 전체 필드 전송·응답 putAll도 AS-IS대로.
- **TO-BE가 지어냈던 값 교정**: LINK_TRGT_CD "GHLOVE"→"1741000HLE01001", 부과 URL wetax.go.kr/… → AS-IS
  `https://192.168.84.55:4145/api/nextgnr/send`, 서울 `http://192.168.84.55:4142/api/seoul/epaymentno`.

**아직 남은 것(같은 기준으로 점검 필요)**: donation `completeDonation`의 registerLevy/confirmPayment·NTS는 TO-BE 모크
(`local-tax.enabled`/`nts.enabled`, 납부게이트웨이 이식 라운드 [[donation-payment-gateway-port-decision]]), admin의
ums/nh/smart-delivery/email/ems/sms-ips/magicline, member의 onepass/kakao/naver/ums는 여전히 단일설정 enabled=false.
데이터: `donation.g_adm_locgov` 0건(외국인 실경로에 필요).
