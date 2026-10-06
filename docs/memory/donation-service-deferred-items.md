---
name: donation-service-deferred-items
description: "donation 서비스 상세분석(2026-09-09) 잔여 - 납부 시 답례품 선택(RFP 신규요구), point 동기호출, 조회모델, 지도 선택, PG 연계"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T08:55:48.846Z
---

2026-09-09 donation을 3축(AS-IS+RFP+ISP) 기준으로 분석한 결과 중 미조치 항목. 원문은 `docs/donation-service-analysis-2026-09-09.md`. **이번 턴 코드 변경은 없다**(§5에 이유 기록).

- **기부금 납부 시 답례품 선택** — RFP SFR-003이 "기부금 납부 시 답례품 선택 기능 추가 등 기부 프로세스 통합 개선"을 명시적으로 요구한다. **AS-IS에 없던 신규 개선 요구**라 참고할 원본이 없고, 현재는 기부 완료 후 별도로 답례품몰로 가는 분리된 흐름이다. 기부↔답례품 흐름 통합 설계 결정 필요.
- ~~**`PointClient.creditForDonation()` 동기 호출**~~ → **[2026-09-21 완료] 이벤트 전환.** give-reqmng 승인의 "포인트생성"이 point로 동기 REST 쓰기를 하지 않고, 정상 완료 경로와 똑같이 `donationEventPublisher.publishCompleted()`로 `donation.lifecycle` COMPLETED 이벤트를 재발행하도록 바꿨다(`CntrReqmngService.approve`). donation의 PointClient에서 `creditForDonation` 쓰기 메서드 자체를 제거(조회 전용화). 안전 근거: 소비자 둘 다 멱등(point `existsByRefKeyAndTxnType(cntrSn,EARN)`, admin 통계 `stat_donation_ledger` cntrSn PK upsert)이라 재발행해도 중복 없음. **E2E 실측 완료**: 시드 기부건 승인→point EARN 15000(50000의 30%)+admin 통계 COMPLETED 반영, 2차 재발행에도 EARN 1건·통계 1건 유지(멱등 확인), 검증 후 물리 정리로 잔액 복원. point 엔드포인트 `/api/admin/credit-for-donation`은 admin ResyncService(운영 백필)가 여전히 사용해 **유지**. 트레이드오프: 동기와 달리 승인 즉시 성공+적립은 결과적 정합성(mainline `completeDonation`과 동일 성격). 상세: [[api-vs-kafka-decision-criteria]].
- **ISP 조회모델(기부요약/영수증/한도, 결제상태/대사뷰) 없음** — [[point-readmodel-paused-pending-db-design]]과 같은 유형. DB 설계 후 같은 시점에 판단.
- ~~**기부하기 지도 선택(map-select) 없음**~~ → **[2026-09-21 철회] 갭 아님.** AS-IS 운영도 `map-select.html`로 진입하는 버튼·링크·메뉴가 0곳이다(모든 참조가 `goToBack`뿐, 순방향 진입 없음). 콘텐츠관리 메뉴에서 명시 제외(list.jsp:81), list-select의 지도 안내이미지는 "고객 요청 삭제"로 주석처리, LNB 지도탭 하이라이트도 주석처리. 즉 지도선택은 파일만 남은 死화면이고 AS-IS도 목록선택(list-select)만 노출한다. MSA는 이미 `ListSelectView.vue`(목록)를 구현해 운영 동등. 최초 분석이 **파일 존재만 보고 운영 도달가능성을 검증 안 한** 오판(사용자가 라이브 화면에서 발견). [[verify-screen-by-content-not-route]]. 참고: header의 `show-map-select`는 답례품몰/장바구니 지역필터용 별개 팝업이다.
- **결제/PG 연계 화면 + ISP 결제 이벤트**(결제세션생성/승인요청/정산캡처) — `LocalTaxClient`는 부과등록·수납확인이 구현돼 있고 `enabled:false`(방화벽 미개방)일 뿐이다. 연계 개방 후 착수.
- ~~**하루 중복기부 확인 로직**~~ → **[2026-09-21 완료]** GET /api/donate/today-duplicate + hasTodayDonation(오늘 cntrDe·같은지자체·비취소) + DonateView confirm. 상세 `docs/donation-parity-audit.md` §9.

**[2026-09-22 donation 재대조 라운드 — 잔여 정리]** 기존 갭 대부분 ✅완료(9/21), 이번에 마감:
- ~~지정기부 응원메시지 30자(5-4)~~ **✅완료**: MSA가 100자로 지어놨던 걸 AS-IS 30자로 정합(DonateView maxlength/placeholder/제출검증 alert "30자 까지 입력가능합니다.", 서버 truncate 30). [[copy-as-is-verbatim-never-invent]] 사례.
- 지정기부 완료취소 불가(5-5) **갭 아님**: AS-IS 원문이 "…취소 안되용~ 테스트 문구"(테스트 잔재). MSA 완료취소 허용이 맞음.
- quick 빠른기부(quick-donation.html) **미사용 시안 → 보류+기록**: AS-IS 소스 주석에 "시안, 어디에도 미연결, 동작 안 함, 신규구현 대상 아님. 실제 기부는 donation-main.html" 명시. [[defer-saleson-dependent-unused-features]] 유형.
- **잔여 실작업**: 응원메시지 인라인 편집(saveCheerMsg, AS-IS는 기부내역 탭 재편집) 미구현 — MSA는 제출시점 수집으로 적응(결정 필요). 나머지는 아래 외부연계·조회모델·RFP 신규만 남음.

**충족 확인된 것(재조사 불필요):** 연간 기부한도 이중 검증(완료 시점 재검증 포함), 기부완료 이벤트→포인트 적립(RFP ★), 영수증·기부확인증·국세청 연계, 기탁·오프라인, 지정기부사업, 명예기부자. 기부 상태코드는 AS-IS 4종 중 900(서울 세외수입 수납배치)만 없고 그 연계에 종속돼 의도적 축소. **CSS 링크 문제 없음**(클래스 단위 대조로 확인) - 이 결함 유형의 실사례는 order의 `mypage-order.css` 하나뿐이다.

**주의:** 연말정산 세액공제 안내(AS-IS guide3)는 **이미 `/honor`에 구현돼 있다** - 없다고 오판한 적이 있으니 다시 착수하지 말 것([[verify-screen-by-content-not-route]]).
