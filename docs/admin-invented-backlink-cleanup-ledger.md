# admin 상단 "← …" 되돌아가기 링크 정리 원장 (화면별)

작성 2026-10-08. 전수 조사는 끝났고, **정리는 화면별로** 한다(다른 개발자와 동시 수정 충돌을
피하려는 사용자 결정). 각 화면을 담당 영역 작업할 때 이 표의 "조치"만 수행하면 된다.

## 무엇이 문제인가

TO-BE admin 템플릿 21곳이 제목 위에 `<p><a>&larr; …</a></p>` 형태의 되돌아가기 링크를 갖고 있다.
**AS-IS opmanager view 트리 전체에 이 패턴은 0건이다.**

```
$ grep -rn '&larr;\|←' ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n --include=*.jsp | wc -l
0
```

AS-IS가 상세화면에서 목록으로 돌아가는 방법은 **화면 하단 `.btn_all` 영역의 `목록` 버튼**이다
(예: `give/give-operation/detail.jsp:237`, `give/give-point/form.jsp:214`,
`give/give-state/form.jsp:466`). 즉 조치는 "링크 삭제"가 아니라 경우에 따라
**"링크 삭제 + AS-IS 하단 목록 버튼 추가"** 다. 링크만 지우면 화면 안에서 목록으로 갈 길이 없어진다
(LNB는 남지만 AS-IS와 다른 상태가 된다).

[[no-invented-features-ask-first]] / [[copy-as-is-verbatim-never-invent]] 위반 사례다.

## 이미 처리한 화면 (2026-10-08)

| 화면 | 메뉴 | 조치 | 비고 |
|---|---|---|---|
| `designated/analysis-locgov.html` | 17201 지자체별통계 | 링크 삭제 | AS-IS `.location`만. 하단 목록 버튼 AS-IS에도 없음(통계 목록 화면이라 불필요) |
| `designated/analysis-month.html` | 17202 월별통계 | 링크 삭제 | 동일 |

## 남은 화면 (19곳)

"하단목록" = TO-BE 템플릿에 `목록` 버튼이 이미 있는지. "AS-IS 목록버튼" = AS-IS 짝 JSP에 있는지.

### A. 링크만 삭제하면 끝 (가장 안전)

| 화면 | 메뉴 | AS-IS 짝 | 하단목록 | 조치 |
|---|---|---|---|---|
| `give/give-operation-detail.html` | 14104 기부금 운용정보 | `give/give-operation/detail.jsp` | 있음 | 상단 링크 삭제 |

### B. 링크 삭제 + AS-IS 하단 `목록` 버튼 추가

| 화면 | 메뉴 | AS-IS 짝 | AS-IS 목록버튼 | 조치 |
|---|---|---|---|---|
| `give/give-point-detail.html` | 14102 기부포인트현황 | `give/give-point/form.jsp` | `:214` | 링크 삭제 + 하단 목록 버튼 |
| `give/give-state-detail.html` | 14101/14103 기부금모금현황 | `give/give-state/form.jsp` | `:466` | 링크 삭제 + 하단 목록 버튼 |
| `order-admin/detail.html` | 16401~16409 주문관리 | `order/detail/order-detail.jsp` | 있음 | 링크 삭제 + 하단 목록 버튼 |
| `statistics-locgov/detail.html` | 6801 관심지자체 | `statistics/locgov/detail.jsp` | 있음 | 링크 삭제 + 하단 목록 버튼 |

### C. AS-IS 실화면 확인이 먼저 필요 (AS-IS 짝에 목록 버튼이 없음)

AS-IS가 팝업으로 띄우는지, 브라우저 뒤로가기에 의존하는지 실화면을 봐야 판정된다.

| 화면 | 메뉴 | AS-IS 짝 | AS-IS 목록버튼 |
|---|---|---|---|
| `shop-statistics/sales-area-detail.html` | 메뉴 없음 | `shop-statistics/sales/area-detail.jsp` | 없음 |
| `shop-statistics/sales-brand-detail.html` | 메뉴 없음 | `shop-statistics/sales/brand-detail.jsp` | 없음 |
| `shop-statistics/sales-item-detail.html` | 메뉴 없음 | `shop-statistics/sales/item-detail.jsp` | 없음 |
| `shop-statistics/sales-user-detail.html` | 메뉴 없음 | `shop-statistics/sales/user-order.jsp` | 없음 |
| `shop-statistics/sales-locgov-detail.html` | 6502 지자체별 | `shop-statistics/sales/locgov.jsp` | 있음 |
| `order-admin/claim-queue.html` | 16451~16454 (`/admin/claims`) | `order/detail/cancel-*`·`return-*` 계열 | 미확정 |
| `settlements/detail.html` | 16502/16503 (`/settlements`) | AS-IS에 `settlement` 디렉터리 없음(`sellerconfirm*` 후보) | 미확정 |
| `settlements/pending.html` | 16501 정산예정 내역 | 동일 | 미확정 |

### D. 접근 경로가 없어 링크 자체가 사문 — 영역 작업 때 함께 판정

| 화면 | 상태 |
|---|---|
| `coupon/offline-codes.html` | `op_menu`에 `/coupon` 없음. [[coupon-feature-unused-hide-ui]] 로 진입점을 숨긴 상태 |
| `coupon/regular-list.html` | 동일 |
| `coupon/target-items.html` | 동일 |
| `coupon/target-users.html` | 동일 |
| `coupon/usage.html` | 동일 |

### E. 별건 — TO-BE 전용 발명 화면 (링크 문제가 아님)

| 화면 | 사실 |
|---|---|
| `designated/analysis.html` (`/designated-projects/analysis`, 제목 "지정기부 모금분석") | `op_menu`에 없고 AS-IS `designated-donation/analysis/` 에는 `locgov.jsp`·`month.jsp` 둘뿐이다. 즉 **AS-IS에 없는 화면**이다. 링크만 지울 게 아니라 화면 존치 여부를 사용자에게 확인해야 한다 |

## 재확인 명령

```bash
# 남은 곳 세기
grep -rn '&larr;' admin/src/main/resources/templates/ | wc -l

# AS-IS에 이 패턴이 없다는 근거
grep -rn '&larr;\|←' <AS-IS>/ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n --include=*.jsp | wc -l   # 0

# 메뉴 등록 여부
SELECT menu_id, menu_name, menu_url, status_code FROM admin.op_menu WHERE menu_url LIKE '%<경로>%';
```
