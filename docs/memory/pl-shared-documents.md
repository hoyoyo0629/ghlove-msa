---
name: pl-shared-documents
description: PL 공유 문서와 기술 결정 대기 - 업무 프로세스 분석·표준프레임워크·데이터 접근(JPA+MyBatis)·화면 기술(대민 Vue3 JS, 관리자 Thymeleaf)·상용SW·외부연계 목록 링크와 내용
metadata:
  type: reference
---

PL과 공유한 문서, 그 안의 결정 대기 사항.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## business-process-analysis-doc

> PL 공유 "고향사랑e음 업무 프로세스 분석" 문서(계정 분석 문서에 탭 추가, 10-20) - 계정·온라인기부·오프라인기탁서·배치 4탭. 옛 SFR-002 재검토·점검 리포트 아티팩트는 현재 결정과 달라 삭제


https://claude.ai/artifact/PtsRHAtPPVt5tKyya2oZBJ (= claude.ai/code/artifact/b9670dea-4671-461b-b6eb-80d0e87d82b5)
탭: 계정 유형별 생성·승인(10-14 원본, 흐름도 포함) / 온라인 기부·답례품 주문 / 오프라인 기탁서 / 배치 운영 구조.
온라인 탭 핵심: TO-BE는 부과·납부·수납이 아직 `/api/my/donations/{cntrSn}/complete` 한 번(completeDonation)으로 단순화 - 납부 게이트웨이 라운드에서 AS-IS 분리 구조로.
2026-10-20 사용자 결정으로 삭제한 옛 아티팩트: SFR-002 접근제어 재검토(09-04, OP_MANAGER 분리·GENERAL 등 현재와 다름), 회원·기부·포인트 점검 리포트+사본(09-05, ISP/RFP 1차근거 시기·포인트 소멸 자동화 등 현재와 반대).
기술 결정 문서는 별도: [[pl-shared-documents]]. 업무흐름이 바뀌면 이 문서 탭도 고칠 것.

## commercial-sw-and-integrations-list

> 상용SW(도입예정 15종+AS-IS 전용)·외부연계(시스템 20+API 2) 목록과 TO-BE 상태는 PL 공유 문서 3번째 탭. 원천은 Desktop 서드파티솔루션현황_20261002.xlsx·연계 현황_261001.hwpx


PL 공유 문서 https://claude.ai/code/artifact/13347c46-107a-48d5-b338-2e3e1995a220 의 탭 "상용SW·외부연계 목록"(2026-10-20).
원천: Desktop\고향사랑e음\0. 문서\고향사랑e음_서드파티솔루션현황_20261002.xlsx(연락처 시트는 개인 연락처라 공유 문서에 넣지 않음, 파일이 Excel로 열려 있으면 공유모드로 복사해 읽기), 고향사랑e음_연계 현황_261001.hwpx, 9월 아티팩트 "상용 SW 인벤토리"(AS-IS 소스 조사).
TO-BE 상태 중 '일부·미확인'(지로·이택스·민간플랫폼 API·국정자원 카톡·Open API·PrivacyDB·WEEDS)은 AS-IS 범위 대조가 더 필요. 미이식: 팝빌 SMS, 구삐, 디지털서비스개방, Konan 검색.
PL 문서 통합(10-20): 옛 "통계 보고서 메뉴 분석DB 구조 분석" 아티팩트는 삭제하고 2번째 탭으로 이동.

**SalesOn 화면 자산(10-20 추가)**: TO-BE 서버는 SalesOn·opframework 의존 0(gradle·import·jar). 단 AS-IS에서 verbatim 복사한 CSS·JS 중 "Copyright Onlinepowers" 표기 29개(admin 21·gift 3·member 1·storefront public/css 4) + 표기 없는 admin op.*.js 16개 + saleson_logo.gif 2개 → 라이선스 확인 필요로 PL 문서 3번째 탭에 절 추가. 쓸 수 없다고 결론나면 같은 동작으로 재작성 대상.

## frontend-tech-stack-review-2026-10-19

> 화면 기술 검토(10-19) - 대민 Vue3+JS(TS 근거 없음·필수 아님), 관리자·답례품관리자 Thymeleaf 유지 제안, RFP 프론트/백 분리 해석(A 사이트단위/B 정적자원단위)은 PL·PM 확인 대기. PL 공유 문서 있음


대민 storefront = Vue 3 + JavaScript(TS 파일 0). RFP·WBS·SFR·착수계·AS-IS 분석문서에 TypeScript 근거 없음(WBS는 "Vue 2→3"만). ISP PDF는 그림 위주라 일부만 확인.
관리자(admin 315 템플릿)·답례품 관리자(gift /seller/*)는 Thymeleaf로 이식 계속 제안.
★확인 대기: RFP "정적 자원(Frontend)과 API(Backend) 실행환경 분리"·"프론트엔드만 인그레스"·Frontend/Backend 네임스페이스 - 관리자에 A(사이트 단위, Thymeleaf 유지 가능) vs B(정적자원 단위, SPA 전환 필요) 어느 해석인지.
별도 RFP 신규: "역할별 관리자 페이지 분리 - 지자체 전용 대시보드 별도 앱(경량화)" 미착수, Vue3 단독앱 선택지.

**Why:** 사용자가 PL 공유 요청(2026-10-19). 문서: https://claude.ai/code/artifact/13347c46-107a-48d5-b338-2e3e1995a220
**How to apply:** 결정 나기 전엔 관리자 화면을 SPA로 바꾸는 작업 제안·착수 금지, Thymeleaf 이식 그대로. 관련 [[pl-shared-documents]], [[thymeleaf-duplicate-cleanup-deferred]].

**같은 날 추가 - 전자정부 표준프레임워크(★우선 결정)**: RFP 기술표준 부록 체크표 "개발프레임워크: 전자정부 표준프레임워크", 빌드환경 "표준프레임워크 빌드 시 프라이빗 레파지토리", ISP 아키텍처 Pod에 "표준프레임워크" 표기 → TO-BE 전제. 현재 6개 서비스 egov 의존성 0(Boot 3.3.4/JDK17).
eGovFrame 5.0 = Boot 3.5·Spring 6.2·Jakarta·JDK17+ 라 적용 가능. 선택지 A 핵심적용(제안, Boot 3.5 업그레이드+rte 핵심모듈+서비스 기반클래스+프라이빗 저장소) / B 부분적용(사유 기재, 감리 불확실) / C 전면(MyBatis 등 데이터접근까지). 같은 문서에 절 추가, 결정 대기.
**How to apply(추가):** 결정 전에 Boot 버전 올리거나 egov 도입 착수 금지. 결정 나면 6개 서비스 일괄 작업.

## data-access-jpa-mybatis-proposal

> 회의 의견 "간단조회 JPA·나머지 MyBatis"에 대한 제안(10-20) - 쓰기·단건=JPA, 목록·검색·통계·엑셀=MyBatis, 신규 이식부터 점진 적용. 표준프레임워크 결정과 함께 PL 확인 대기


현재 TO-BE native SQL 약 280곳(admin 103·gift 87·donation 59·기타 29), JPQL 42곳 - 조건부 문자열 조립 때문에 null 바인딩 500·Character 캐스팅 500 반복([[backend-db-pitfalls]], [[backend-db-pitfalls]]).
제안: 쓰기·ID 단건=JPA / 목록·검색·통계·엑셀·AS-IS 매퍼 이식 조회=MyBatis. 같은 기능에서 같은 테이블을 양쪽으로 만지지 않기(flush·1차캐시 불일치).
기존 코드는 일괄 전환 안 함 - admin에 설정 잡고 목록 화면 1개 시범 → 신규 이식부터 → 손댈 때/장애 난 곳.

**Why:** 사용자 요청으로 PL 공유 문서 [[pl-shared-documents]]에 "데이터 접근 기술" 절+우선 체크항목 추가(2026-10-20).
**How to apply:** 결정 전엔 MyBatis 도입 착수 금지(표준프레임워크 실행환경과 같이 설정). 결정 나면 admin 시범부터.

## admin-frontend-is-thymeleaf-not-jsp

> admin 서비스 프론트는 Vue3 전환 미정 상태로 유지 - AS-IS는 JSP지만 ghlove-msa/admin은 이미 Thymeleaf로 구현됨


admin 서비스 프론트는 Vue3로 전환할지 정해진 바 없어 현 상태를 유지하기로 했다(2026-09-08). 단, "AS-IS와 동일하게 JSP" 는 사실과 다르다: AS-IS `C:\workspace\ghlove\ghlove-web`의 opmanager는 JSP 531개가 맞지만, `ghlove-msa/admin`은 이미 **Thymeleaf 220개**로 구현돼 있고 JSP 파일과 jasper/jstl 의존성은 0이다.

**Why:** admin은 Spring Boot 3.3.4 + jar 패키징이라 JSP가 아예 동작하지 않는다(war + tomcat-embed-jasper로 되돌려야 하는데 Boot가 권장하지 않는 조합). Thymeleaf든 JSP든 서버 렌더링이라는 성격은 같아서, 나중에 Vue3로 갈 때 버리는 양도 동일하다 - 즉 아무것도 안 바꾸는 것이 "미정이니 그대로 두자"의 올바른 이행이다.

**How to apply:** admin 프론트 이야기가 나오면 JSP 전환을 제안하지 말고 Thymeleaf 유지 또는 Vue3 전환 두 갈래로만 논의한다. 이 결정이 [[thymeleaf-duplicate-cleanup-deferred]]의 보류 사유다.

**[2026-10-07 재확인]** 사용자가 작업 중 반복되는 Thymeleaf 결함(th:onclick/인라인JS 함정 등)에
답답함을 표하며 Vue3 전면전환을 문의 → 범위(이미 AS-IS와 맞춘 225개 화면 전부 재작업)를
설명한 뒤 "일단 지금 상태로 유지, **플랫폼 구성 끝나고 SW/웹 기술스택이 확정되면 그때 재검토**"로
명시적으로 재확인했다. 즉 보류 사유가 "미정"에서 "플랫폼/기술스택 확정 이후"로 더 구체화됨 -
다음에 이 얘기가 나오면 그 트리거(기술스택 확정 시점)를 먼저 확인할 것.
