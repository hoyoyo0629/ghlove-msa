# docs/archive - 역할이 끝난 일회성 리포트

여기 있는 문서는 **특정 시점의 점검·수정 리포트**다. 당시 결론은 이미
메모리 원장(영역별 이식 원장)과 `docs/as-is-parity.md`에 반영돼 있어서,
현재 작업에서 다시 읽을 필요가 없는 것만 모았다.

**옮긴 기준**: 날짜가 박힌 일회성 리포트이거나 더 최신 문서로 대체된 것.

| 문서 | 성격 | 대체된 곳 |
|---|---|---|
| `feature-verification-2026-09-08-*.md` (4) | 2026-09-08 기능검증 라운드 | 영역별 이식 원장(메모리) |
| `fix-report-2026-09-08.md` | 같은 라운드의 수정 리포트 | 동일 |
| `completion-feedback-analysis-2026-09-10.md` | 완성도 피드백 분석 | `as-is-parity.md` 전수 커버리지 원장 |
| `{donation,gift,member,order,point}-service-analysis-*.md` (5) | 초기 서비스별 분석 | `as-is-parity.md` 서비스별 절 |
| `service-difficulty-ranking.md` | 착수 순서 정하려고 만든 난이도 순위 | 작업 순서가 확정돼 불필요 |
| `{gift,member,order,point}-parity-audit.md` (4) | 초기 서비스별 전수대조 | `as-is-parity.md` |
| `cart-review-2026-09-09.md` | 장바구니 상세 리뷰 | 잔여과제는 메모리 `cart-review-remaining-tasks` |
| `spa-static-defect-audit-2026-09-18.md` | SPA 정적요소 결함 배치수정 | 메모리 `static-asset-scoping-2026-09-18` |
| `static-asset-audit-2026-09-18.md` | 정적 에셋 원인규명 | 동일 |

**되돌리기**: `git mv docs/archive/<파일> docs/` 하면 끝이다(히스토리 유지됨).

**2026-10-06 2차 정리**: docs/ 최상위 .md **46개를 6개로 통합**했다. 위 표의 날짜 박힌
리포트는 여기로 옮기고, 나머지는 주제별로 합쳤다 - 어디로 갔는지는 `docs/README.md`의
통합 대조표를 보면 된다. `cart-review-2026-09-09.md`는 코드 주석
(`order/.../OrderService.java`)이 가리키고 있었으므로 그 주석의 경로도 함께 고쳤다.
