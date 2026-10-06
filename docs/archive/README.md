# docs/archive - 역할이 끝난 일회성 리포트

여기 있는 문서는 **특정 시점의 점검·수정 리포트**다. 당시 결론은 이미
메모리 원장(영역별 이식 원장)과 `docs/coverage-ledger.md`에 반영돼 있어서,
현재 작업에서 다시 읽을 필요가 없는 것만 모았다(2026-10-06 정리).

**옮긴 기준**: 메모리·코드·다른 문서에서 **아무도 참조하지 않고**, 날짜가 박힌
일회성 리포트이거나 더 최신 문서로 대체된 것.

| 문서 | 성격 | 대체된 곳 |
|---|---|---|
| `feature-verification-2026-09-08-*.md` (3) | 2026-09-08 기능검증 라운드 | 영역별 이식 원장 |
| `fix-report-2026-09-08.md` | 같은 라운드의 수정 리포트 | 동일 |
| `completion-feedback-analysis-2026-09-10.md` | 완성도 피드백 분석 | `coverage-ledger.md` |
| `gift-service-analysis-2026-09-09.md` | 초기 서비스 분석 | `gift-*-parity-audit.md` 5종 |
| `service-difficulty-ranking.md` | 착수 순서 정하려고 만든 난이도 순위 | 작업 순서가 확정돼 불필요 |
| `gift|member|order|point-parity-audit.md` (4) | 초기 서비스별 전수대조 | `coverage-ledger.md` + 영역별 원장 |

**되돌리기**: `git mv docs/archive/<파일> docs/` 하면 끝이다(히스토리 유지됨).

**여기 두지 않은 것**: `as-is-inventory-*.md` 7종은 이식할 때 계속 보는 기본 자료라 `docs/`에
남겼다. `admin-parity-audit.md`·`donation-parity-audit.md`·`cart-review-2026-09-09.md`·
`*-service-analysis-*.md` 등 **메모리나 코드 주석이 경로로 가리키는 문서**도 남겼다 -
옮기면 그 포인터가 깨진다.
