# docs - 문서 지도

2026-10-06에 최상위 `.md` **46개를 6개로 통합**했다. 서비스별로 쪼개져 있던 분석·대조
문서가 계속 늘어나 "어느 파일에 적었는지"를 매번 찾아야 했기 때문이다. 내용은 **그대로
옮기고 중복만 제거**했으며(헤딩만 한 단계 내림), 통합 전 파일명은 각 절 머리에 적어 둬서
git 이력으로 원본을 찾을 수 있다.

## 지금 보는 문서 6개

| 문서 | 무엇이 들어있나 | 언제 보나 |
|---|---|---|
| `README.md` | 이 지도 | 어디에 뭐가 있는지 모를 때 |
| `requirements.md` | **RFP 기능요구(SFR-001~014)** + **ISP 상세설계(6차) 요약** | 신규/변경 기능의 근거를 확인할 때 |
| `as-is-parity.md` | AS-IS↔TO-BE **대조·갭·커버리지 21개 문서** (공통 → admin → member → donation → gift → order → point) | 이식 범위·누락 확인 |
| `as-is-inventory.md` | AS-IS **소스 인벤토리 7영역** (표 원본은 `inventory/*.tsv`, `inventory/as-is-inventory.xlsx`) | AS-IS 파일을 찾을 때 |
| `design-decisions.md` | 아직 살아 있는 **설계·재설계 계획 7건** | 설계 선택지·근거를 볼 때 |
| `local-run-guide.md` | 로컬 기동 절차 | 띄울 때 |

`archive/` 는 역할이 끝난 날짜 박힌 리포트다(`archive/README.md` 참고).

## 정본이 어디인가 - 중요

- **진척 상태**(어느 화면까지 이식했는지)는 이 문서들이 아니라 **메모리 원장**이 정본이다
  (`admin-system-area-port-progress`, `admin-member-area-port-progress`,
  `admin-customer-center-area-port-progress`, `admin-designated-donation-area-port-progress`,
  `admin-small-areas-port-progress`). 문서에 적힌 "완료"는 작성 시점의 판정이다.
- **기능 사양**은 AS-IS 소스(`C:\workspace\ghlove`)가 정본이다. 문서는 대조 결과일 뿐이다.
- **보류·결정 사항**은 메모리의 `defer-*` / `*-decision` 항목이 정본이다.

## 통합 대조표 - 예전 파일이 어디로 갔나

| 통합 전 | 어디로 |
|---|---|
| `as-is-coverage-map.md`, `coverage-ledger.md` | `as-is-parity.md` §1, §2 |
| `as-is-fidelity-audit-2026-09-17.md` | `as-is-parity.md` §3 |
| `as-is-feature-audit-{admin,member,donation,gift,order,point}.md` | `as-is-parity.md` §4, §10, §12, §14, §20, §21 |
| `admin-parity-audit.md`, `admin-menu-tree-parity.md` | `as-is-parity.md` §5, §6 |
| `as-is-admin-gap-deep-audit-part{1,2}.md` | `as-is-parity.md` §7, §8 |
| `upload-file-parity-audit.md` | `as-is-parity.md` §9 |
| `member-social-unlink-parity-audit.md` | `as-is-parity.md` §11 |
| `donation-parity-audit.md` | `as-is-parity.md` §13 |
| `gift-{option,category-tree,search-keyword,delivery-fee,seller-selfservice}-parity-audit.md` | `as-is-parity.md` §15~§19 |
| `as-is-inventory-{common,admin,member,donation,gift,order,point}.md` | `as-is-inventory.md` §1~§7 |
| `account-model-design.md` | `design-decisions.md` §1 |
| `cross-service-transaction-patterns.md` | `design-decisions.md` §2 |
| `member-event-publishing-design.md` | `design-decisions.md` §3 |
| `gift-option-redesign-plan.md` | `design-decisions.md` §4 |
| `order-multiitem-redesign-plan.md` | `design-decisions.md` §5 |
| `b5-designated-month-stats-spec.md` | `design-decisions.md` §6 |
| `thymeleaf-decommission-map.md` | `design-decisions.md` §7 |
| `isp-detailed-design-summary.md` | `requirements.md` 후반부 |
| `cart-review-2026-09-09.md`, `{donation,member,order,point}-service-analysis-*.md`, `spa-static-defect-audit-*`, `static-asset-audit-*`, `feature-verification-2026-09-08-member.md` | `archive/` |

## 새 문서를 만들기 전에

위 6개 중 **들어갈 자리가 있으면 거기에 절을 추가한다.** 날짜 박힌 새 파일을 만드는 것은
또 46개로 가는 길이다. 일회성 점검 결과는 문서가 아니라 **메모리**에 남기는 쪽이 맞다 -
다음 세션이 읽는 것은 메모리이기 때문이다.
