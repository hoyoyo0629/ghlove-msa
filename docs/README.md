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

## `memory/` - 세션 메모리 (주제별 파일)

`docs/memory/` 는 **Claude Code 세션 메모리의 실제 저장소**다(2026-10-06에 저장소 안으로 옮겼다).
원래 위치인
`C:\Users\<계정>\.claude\projects\c--workspace-ghlove-msa\memory` 는 이제 이 폴더를 가리키는
**디렉터리 정션**이라서, 어느 쪽 경로로 쓰든 파일은 여기 한 벌만 생긴다. Claude Code가 보는
경로는 바꿀 수 없으므로 정션이 사라지면 메모리도 끊긴다 - 폴더를 옮기거나 지우지 말 것.
정션 재생성:

```powershell
New-Item -ItemType Junction `
  -Path "$env:USERPROFILE\.claude\projects\c--workspace-ghlove-msa\memory" `
  -Target "C:\workspace\ghlove-msa\docs\memory"
```

**2026-10-20부터 주제별 파일로 관리한다(158개 → 35개 통폐합).** 원문은 버리지 않고 옮겼다:
주제 파일 9개(`asis-porting-principles`·`admin-screen-pitfalls`·`backend-db-pitfalls`·`tooling-and-local-env`·
`asis-sources-and-data`·`domain-code-facts`·`da-design-pending`·`deferred-features`·`pl-shared-documents`)로 합치고,
영역 이식 원장 23개는 `as-is-parity.md` §22, 완료된 수정 일지 25개는 `archive/memory-fix-log-2026-10.md`로 옮겼다.

- `MEMORY.md` 는 세션마다 통째로 읽히는 **색인** - 파일당 한 줄.
- **완료된 수정·데이터 적재는 메모리에 쓰지 않는다**(git 커밋에 있다). 다시 쓸 교훈만 해당 함정 파일에 절로 덧붙인다.
- **같은 주제는 기존 파일을 고친다.** 새 파일은 어느 파일에도 맞지 않는 새 주제일 때만.

## 정본이 어디인가 - 중요

- **진척 상태**(어느 화면까지 이식했는지)는 `as-is-parity.md` §22(영역별 이식 원장)가 정본이고, 메모리 `admin-port-progress`에 영역별 한 줄 요약이 있다(2026-10-20 메모리에서 이동). 다른 절에 적힌 "완료"는 작성 시점의 판정이다.
- **기능 사양**은 AS-IS 소스(`C:\workspace\ghlove`)가 정본이다. 문서는 대조 결과일 뿐이다.
- **보류·결정 사항**은 메모리 `da-design-pending`·`deferred-features`·`pl-shared-documents`와 `*-decision` 항목이 정본이다.

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
| `batch-job-port-ledger.md`, `admin-invented-backlink-cleanup-ledger.md` (2026-10-20) | `as-is-parity.md` §23, §24 |
| `cart-review-2026-09-09.md`, `{donation,member,order,point}-service-analysis-*.md`, `spa-static-defect-audit-*`, `static-asset-audit-*`, `feature-verification-2026-09-08-member.md` | `archive/` |

## 새 문서를 만들기 전에

위 6개 중 **들어갈 자리가 있으면 거기에 절을 추가한다.** 날짜 박힌 새 파일을 만드는 것은
또 46개로 가는 길이다. 끝난 일회성 점검·수정 결과는 문서에도 메모리에도 새 파일로 남기지 않는다 -
필요하면 해당 절이나 주제 메모리에 한 단락 덧붙인다.
