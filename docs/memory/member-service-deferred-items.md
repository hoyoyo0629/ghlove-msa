---
name: member-service-deferred-items
description: "member 서비스 상세분석(2026-09-09) 잔여 5건 - 이벤트 발행 전무, 조회모델, 전자서명, Keycloak, 연동해지. 각각 결정이 필요해 보류"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T10:37:40.015Z
---

2026-09-09 member 서비스를 3축(AS-IS+RFP+ISP) 기준으로 분석한 결과 중 **결정이 필요해 미조치로 남긴 5건**. 분석 원문·근거는 `docs/member-service-analysis-2026-09-09.md`.

- ~~**이벤트 발행이 전혀 없음**~~ — **[2026-09-22 Phase A 발행 구현완료]** member.lifecycle 토픽에 `MEMBER_JOINED`(signup/walk-in/외부가입)·`LOGIN_SUCCEEDED`·`LOGIN_FAILED`·`MEMBER_WITHDRAWN`·`MEMBER_DORMANT` 발행(`MemberEventPublisher`, 기존 donation/gift/order/point 패턴 동형, publish-only 비차단). build.gradle spring-kafka + application.yml producer + KafkaTopicConfig 추가. 컴파일 OK, **재기동+발행 확인 대기**(ghlove-kafka 9092 기동중). 설계·결정근거 `docs/member-event-publishing-design.md`(사용자 확정: Phase A만·감사는 이벤트축적 방향). **보류(Phase B)**: admin 회원통계/감사 소비 ReadModel은 [[defer-readmodels-pending-da-design]]로 DA 설계 후. 본인인증(외부연계)·ProviderAccount(포털분리) 이벤트도 보류.
- **ISP 조회모델 3종(회원정보뷰/인증이력뷰/세션뷰) 없음** — point ReadModel과 같은 유형. [[point-readmodel-paused-pending-db-design]]이 DB 설계 대기 중이라 같은 시점에 같이 판단하는 게 맞다.
- **전자서명(공동인증서) 등록/해제 없음** — AS-IS `/signRegister` `/signRemove` `/checkSign` `/getNonce` + `users/sign-certificate.html`. ISP도 회원관리 POD SW목록에 MagicLine 4(PKI공인인증, 상용)를 명시해 근거가 2축. **결정 필요**: 다른 외부연계와 같은 의도적 축소로 볼지.
- **Keycloak 미도입** — ISP p.481이 계정통합관리로 Keycloak(OIDC IAM) 채택, AD/LDAP 연동까지 전제. 현재 서비스별 자체 JWT. 로컬 단계에선 합리적 생략이나 **클라우드 이관 시 중앙 IAM이 설계 전제**.
- ~~**디지털원패스/카카오 연동해지 화면 없음**~~ — **[2026-09-22 구현 완료]** AS-IS `onepass_secede.html`/`secede-kakao.html` + `modify.html` 연동해지 버튼 재현. member `AccountUnlinkApiController`(onepass-cancel/kakao-link-clear/kakao-link-secede) + `secedeExternal`/`clearKakaoLink`, `OnePassClient.releaseInterlock()`/`KakaoCertClient.unlink()`(off면 통과), storefront `ProfileView` 버튼 2개 + `OnepassSecedeView`/`KakaoSecedeView`. 매핑: userKeyYN≡loginPathCode300, kakaoUserKeyYN≡500or키보유. 컴파일·빌드 OK, **재기동+E2E 대기**. 상세 `docs/member-social-unlink-parity-audit.md`. **보류**: `onepass-unlink`(탈퇴없는 원패스 부가연동 해제)는 MSA 모델상 성립X, 실 연계해지 API 본체는 외부개방 시.

**Why:** 다섯 건 모두 코드를 더 쓰는 문제가 아니라 인프라/외부연계/설계 범위 결정이 선행돼야 하는 항목이다.

**How to apply:** 같은 세션에서 조치한 것(비밀번호 유효기간 정책, `.list-none` CSS)은 이미 반영·검증됐다. 비밀번호 정책 관련 주의: `op_user.password_expired_date`의 기존 `20240101`은 AS-IS 스키마 이관 placeholder라 정책 도입 시점 기준으로 한 번 밀어놨다(DDL에 마이그레이션 기록) - 다시 초기화하면 전 회원이 즉시 만료 처리되니 주의. 분석은 3축 기준을 따랐다([[check-as-is-source-when-analyzing]]).
