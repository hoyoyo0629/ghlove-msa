---
name: frontend-tech-stack-review-2026-10-19
description: 화면 기술 검토(10-19) - 대민 Vue3+JS(TS 근거 없음·필수 아님), 관리자·답례품관리자 Thymeleaf 유지 제안, RFP 프론트/백 분리 해석(A 사이트단위/B 정적자원단위)은 PL·PM 확인 대기. PL 공유 문서 있음
metadata:
  type: project
---

대민 storefront = Vue 3 + JavaScript(TS 파일 0). RFP·WBS·SFR·착수계·AS-IS 분석문서에 TypeScript 근거 없음(WBS는 "Vue 2→3"만). ISP PDF는 그림 위주라 일부만 확인.
관리자(admin 315 템플릿)·답례품 관리자(gift /seller/*)는 Thymeleaf로 이식 계속 제안.
★확인 대기: RFP "정적 자원(Frontend)과 API(Backend) 실행환경 분리"·"프론트엔드만 인그레스"·Frontend/Backend 네임스페이스 - 관리자에 A(사이트 단위, Thymeleaf 유지 가능) vs B(정적자원 단위, SPA 전환 필요) 어느 해석인지.
별도 RFP 신규: "역할별 관리자 페이지 분리 - 지자체 전용 대시보드 별도 앱(경량화)" 미착수, Vue3 단독앱 선택지.

**Why:** 사용자가 PL 공유 요청(2026-10-19). 문서: https://claude.ai/code/artifact/13347c46-107a-48d5-b338-2e3e1995a220
**How to apply:** 결정 나기 전엔 관리자 화면을 SPA로 바꾸는 작업 제안·착수 금지, Thymeleaf 이식 그대로. 관련 [[admin-frontend-is-thymeleaf-not-jsp]], [[thymeleaf-duplicate-cleanup-deferred]].

**같은 날 추가 - 전자정부 표준프레임워크(★우선 결정)**: RFP 기술표준 부록 체크표 "개발프레임워크: 전자정부 표준프레임워크", 빌드환경 "표준프레임워크 빌드 시 프라이빗 레파지토리", ISP 아키텍처 Pod에 "표준프레임워크" 표기 → TO-BE 전제. 현재 6개 서비스 egov 의존성 0(Boot 3.3.4/JDK17).
eGovFrame 5.0 = Boot 3.5·Spring 6.2·Jakarta·JDK17+ 라 적용 가능. 선택지 A 핵심적용(제안, Boot 3.5 업그레이드+rte 핵심모듈+서비스 기반클래스+프라이빗 저장소) / B 부분적용(사유 기재, 감리 불확실) / C 전면(MyBatis 등 데이터접근까지). 같은 문서에 절 추가, 결정 대기.
**How to apply(추가):** 결정 전에 Boot 버전 올리거나 egov 도입 착수 금지. 결정 나면 6개 서비스 일괄 작업.
