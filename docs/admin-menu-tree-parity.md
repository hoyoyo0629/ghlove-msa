# admin 메뉴 트리 parity 대조 (AS-IS OP_MENU ↔ TO-BE)

작성 2026-10-01. 근거: AS-IS live 덤프(`OP_MENU_202610010945.sql` 355행 / `OP_MENU_RIGHT_202610010946.sql` 503행, CUBRID)를
`asis_dump` 스키마에 적재해 추출. TO-BE는 `admin.op_menu`(현재 122행, 7그룹 2단).

## 0. 핵심 사실
- AS-IS·TO-BE 둘 다 상단 GNB + 좌측 LNB를 **100% OP_MENU DB로 렌더링**. 즉 "상단 11개 vs 7개"는 **데이터 차이**.
- AS-IS는 **root(0) 아래 상단 21개 중 display='Y' 11개**가 노출(나머지 10개는 SalesOn 전자상거래 잔재라 숨김). TO-BE는 임의 7그룹으로 재편돼 있음.
- **깊이 차이**: AS-IS = 3단(상단GNB → LNB 섹션그룹 → 링크). TO-BE `navTreeFor`/`admin-nav.html` = 2단. → 렌더링 코드도 3단화 필요.
- **URL 체계 불일치**: AS-IS는 전부 `/opmanager/...`, TO-BE는 `/admin/*`·`/codes`·`/community/*`·`/shop-statistics/*` 등. 철자도 다름(`code/list`↔`codes`, `community/srBbs/list`↔`community/sr-bbs`). → 덤프 그대로 재시드하면 전부 404. **구조·명칭·순서·숨김·권한은 AS-IS에서, menu_url은 TO-BE 컨트롤러로 remap**.

## 1. AS-IS 상단 11개(노출, seq순) + 숨김 10개
노출: 시스템관리(1000) · 회원관리(4000) · 기부금관리(14000) · 오프라인기부금접수(15000) · 답례품관리(16000) ·
고객센터(5000) · 특정사업 기부 관리(17000) · 커뮤니티(11000) · 통계(6000) · 기부혜택증관리(19000) · 기부현황 대시보드(20000).

숨김(display='N', SalesOn 잔재 → 숨김 그대로 유지): 디자인/전시관리(7000) · 이벤트(12000) · 주문관리(3000) ·
마켓 관리(10000) · 입점업체관리(8000) · 정산관리(9000) · UMS 캠페인(13000) · 소식지관리(18000) · 상품정보(2000) · 페이지관리(9999).

- 노출 11개 상단 아래 **가시(display=Y) 행 188 / 링크 140개**.

## 2. 매핑/갭 (가시 링크 140개 분류)
범례: ✅ TO-BE 동일화면 존재 · 🟡 유사/통합/흡수(재확인) · ❌ TO-BE 없음(갭)

### 시스템관리 > 시스템 관리(1400)
| AS-IS | AS-IS url | TO-BE | 상태 |
|---|---|---|---|
| 팝업관리 설정 | popup/list | /popups | ✅ |
| 사용자 권한 관리 | user-group/role/list | /admin/roles | 🟡 역할+메뉴권한 통합 |
| 사용자 권한그룹 관리 | user-group/list | /admin/roles | 🟡 동일화면 통합 |
| 관리자 권한 승인관리 | manager-request/list | /admin/manager-requests | ✅ |
| ISMS관리 | isms/isms-config | /isms-config | ✅ |
| 메인 배너 관리 | user-login-banner/index | (Banner.bannerType) | 🟡 로그인배너 흡수, 전용화면 없음 |
| 공통코드 관리 | code/list | /codes | ✅ |
| 사용자로그관리 | log/user/login-log | /log/user-login | ✅ |
| 관리자로그관리 | log/login-log | /log/login | ✅ |
| 이메일 설정 | mail-config/list | /mail-config | ✅ |
| 약관관리 | config/policy/list | /policy | ✅ |
| Batch Job | batch-job/list | /batch-job | ✅ |
| 메세지 관리 | message/list | — | ❌ 공통메시지(보류) |
| 배송업체 관리 | delivery-company/list | — | ❌ 갭 |
| 메뉴관리 | menu/list | /admin/menus | ✅ |
| 설문관리 | qustnr/list | /admin/surveys | ✅ |
| 엑셀다운로드사유관리 | log/exceldownload-log | /admin/excel-download-logs | ✅ |
| 서울부과/수납·지방부과/수납 로그 (4) | log/gif-* | /log/levy | 🟡 연계로그로 통합(4→1) |
| 문자전송이력 | sms-log/list | /admin/send-sms-logs | ✅ |
| 배치 실행로그 조회 | batch-log/list | — | ❌ 배치로그(보류) |
| 포인트사용 정합성검증 | point-check/list | /reconciliation/order-point | 🟡 유사 |
| 이메일 발송 | email/list | /email | ✅ |
| 결제정보 설정 | config/payment-config | /site-config | 🟡 SiteConfigExt |
| 온라인 입금 계좌 | account-number/list | — | ❌ 갭 |
| 카드 혜택 관리 | card-benefits/list | — | ❌ 갭 |
| PG 설정 | config/pg | /site-config | 🟡 SiteConfigExt |

### 회원관리(4000)
| 일반회원관리 | user/customer/list | /admin/members | ✅ |
| 탈퇴회원리스트 | user/secede-user/list | /admin/secede-users | ✅ |
| 휴면회원관리 | user/sleep-user/list | /admin/sleep-users | ✅ |
| 지자체관리 | user/locgov/list | /admin/locgovs | ✅ |
| 지자체담당자관리 | user/locgov-charger/list | /admin/person-in-charge | 🟡 재확인 |
| 운영관리자 | user/oper-charger/list | /admin/managers | 🟡 재확인 |
| 오프라인담당자 | user/off-charger/list | /admin/off-person-in-charge | ✅ |
| 답례품 관리자 | user/rtnpsnt/list | /seller | 🟡 제공자=판매자 |

### 기부금관리(14000)
| 기부금모금현황 | give/give-state/list | /give-state | ✅ |
| 기부포인트현황 | give/give-point/list | /give-point | ✅ |
| 기부금 운용정보 | give/give-operation/list | /give-operation | ✅ |
| 기부금·포인트 변경 | give/give-state/reqmng-list | /give-reqmng | ✅ |
| 기부금전체현황 | give/give-state/detail_list | /give-state | 🟡 상세목록(동일컨트롤러?) |
| 기금사업 등록/관리 | give/give-notice/list | — | 🟡/❌ 기금사업소개 재확인 |

### 오프라인기부금접수(15000)
| 기탁서 등록 | offgive/create | /offgive | 🟡 (/offgive 내 등록) |
| 기부금 접수관리 | offgive/list/ | /offgive | ✅ |

### 답례품관리(16000)
| 카테고리관리 | categories/list | /admin/gift-categories | ✅ |
| 카테고리 그룹 관리 | categories-team-group/list | /admin/category-teams | ✅ |
| 답례품제공자 관리 | seller/list | /seller | ✅ |
| 대표배너관리 | representative-banner/list | /admin/main-banners | 🟡 재확인 |
| 답례품관리 | item/list | /admin/gift-items | ✅ |
| 답례품승인관리 | item/seller/list | /admin/gift-items | 🟡 승인필터 재확인 |
| 전체주문관리/신규주문/배송준비중/배송중/배송완료/구매확정/오프라인주문 (7) | order/* | /admin/orders | 🟡 상태필터 통합 |
| 신규주문(모바일)/발송준비중(모바일) (2) | order/*-mobile | — | ❌ 모바일 전용(보류) |
| 주문취소/반품/교환/환불 관리 (4) | order/{cancel,return,exchange,refund}/list | /admin/claims | 🟡 클레임큐 통합 |
| 정산예정/확정/마감 내역 (3) | remittance/* | /settlements(/pending) | 🟡 통합 |
| 답례품후기 관리 | item/review/list | /admin/gift-reviews | ✅ |
| 답례품 Q&A | qna-item/list | — | 🟡/❌ (gift 판매자Q&A?) |
| 이벤트 관리 | featured/list | /admin/featured | ✅ |
| 관리자 문의 | qna-admin/list | /qna-admin | ✅ |
| 제철식품관 관리 | seasonal-food/list | — | 🟡/❌ (gift측 구현, admin 없음) |
| 특산물관 관리 | speciality-item/list | — | 🟡/❌ |
| 미완료 주문/정산 목록 (2) | temp-process/* | — | ❌ 갭 |

### 고객센터(5000)
| 지자체공지사항 | locgov-notice/list | /admin/notices | 🟡 재확인 |
| 공지사항 | notice/list | /admin/notices | ✅ |
| 자료실 | data-board/list | /admin/data-board | ✅ |
| 1:1 문의 | qna/list | /admin/shop-inquiries | 🟡 구버전 |
| Q&A | qna-open/list | — | 🟡/❌ |
| FAQ | faq/list | /community/faq-bbs | 🟡 |
| 운영관리 SR게시판 | maintenance/list | /admin/maintenance | ✅ |
| 답례품제공자 공지사항 | sellerNotice/list | /admin/seller-notices | ✅ |
| 관리자매뉴얼 | manual/manager/list | /admin/manuals | 🟡 |
| 사용자매뉴얼 | manual/list | /admin/manuals | 🟡 |

### 특정사업 기부 관리(17000)
| 특정사업 기부 목록 | designated-donation/list | /designated-projects | ✅ |
| 특정사업 기부 등록 | designated-donation/form | /designated-projects | 🟡 |
| 지자체별통계/월별통계 (2) | designated-donation/analysis/* | — | ❌ 갭 |
| 특정사업 기부 배너관리 | designated-donation/banner/list | — | ❌ 갭 |
| 특정사업 기부 권한승인 | designated-donation/request/list | — | ❌ 갭 |

### 커뮤니티(11000)
| SR 게시판 | community/srBbs/list | /community/sr-bbs | ✅ |
| 소통방 | community/bbs/list | /community/bbs | ✅ |
| 자료실 | community/databoard/list | /community/databoard | ✅ |
| 담당자용 FAQ | community/faqBbs/list | /community/faq-bbs | ✅ |
| 오프라인 담당자 SR 게시판 | community/offSrBbs/list | /community/off-sr-bbs | ✅ |
| (지자체FAQ 11403은 AS-IS에서 display='N' 숨김) | community/locv-faq | /community/locv-faq | ✅ 숨김유지 |

### 통계(6000)
| 기부금 모금현황 전체/지자체별 (2) | give/statistics/{all,locgov} | /give-statistics | 🟡 |
| 매출통계 일/월/년/결제타입 (4) | shop-statistics/sales/{day,month,year,payment} | 동일 | ✅ |
| 기부금 운영현황 | give/statistics/operate | — | 🟡/❌ |
| 판매자별/상품별/카테고리별/지역별 통계 (4) | sales/{seller,item,category,area} | 동일(category→categories) | ✅/🟡 |
| 관심지자체 | statistics/locgov/like | /statistics/interest-locgov | ✅ |
| 만족도 조사 | cntnts-stsfdg/list | — | ❌ 갭 |
| 답례품 구매현황 전체/지자체별/월지자체/농협/카테고리 (5) | sales/* | 동일 | ✅ |
| 이벤트/상품이벤트 통계 (2) | event-statistics/* | — | ❌ 갭 |
| 답례품 선호도 | shop-statistics/wish/list | /shop-statistics/wish/list | ✅ |
| GA 사용자/페이지/세션/유입/캠페인 통계 (5) | google-analytics/* | — | ❌ GA(보류) |
| 접속통계 | stats/visit/index | /stats | 🟡 방문통계 갭 |
| 방문자접속경로 | stats/referer | — | ❌ 갭 |
| 보고서 일일/총괄/누계/지자체누계/지자체/연간 (6) | shop-statistics/{dashboard/day,report/*} | 동일 | ✅ |
| 외국인 기부 전체/지자체별 (2) | give/statistics/foreigner/* | — | ❌ 갭 |

### 기부혜택증관리(19000)
| 기부혜택증 설정 관리/설정 (2) | lclgvHnrUser/.../ {list,form} | /admin/honor-users | 🟡 |
| 기부혜택증 열람현황 | .../lclgvHnrUserViewHist/list | — | ❌ 갭 |

### 기부현황 대시보드(20000)
| 기부현황 대시보드 | bix5-access | — | ❌ BIX5 외부BI(보류) |

## 3. 갭 요약 (❌ + 보류성 🟡)
- **보류(SalesOn/외부/모바일/GA/BIX5)**: 모바일 주문 2, GA 통계 5, BIX5 대시보드, 배치로그, 공통메시지. → 숨김 처리 + 기록.
- **진짜 재현갭(고향사랑 기능인데 TO-BE 없음)**: 특정사업 통계 2·배너·권한승인(4), 외국인기부 2, 만족도조사, 이벤트통계 2, 기부혜택증 열람현황, 방문자접속경로, 접속통계(방문통계-기존식별), 배송업체·온라인입금계좌·카드혜택 관리, 기금사업소개(재확인). → 메뉴 구조엔 넣되 화면은 후속 라운드.

## 4. 반영 방식 결정 (사용자 확인 필요 → §5)
- **D1 menu_id 체계**: AS-IS 원본 id(1000/1400/1401…) 그대로 재사용 권장(향후 덤프 재대조·verbatim 유리). 현 TO-BE id는 폐기·전면 교체.
- **D2 AS-IS 노출인데 TO-BE 화면 없는 링크**: 이번 패스는 **숨김(display='N')+갭목록 기록**(죽은 링크 노출 금지), 화면 생기면 노출 복원. (AS-IS 노출상태와의 의도적·추적되는 일시 divergence)
- 공통: 숨김 상단 10개·숨김 하위는 AS-IS display 그대로 복제(링크 404 무관, 안 보임).

## 5. 다음 단계(결정 후)
1. 재시드 SQL: `admin.op_menu` 전면 교체(AS-IS 구조+TO-BE url remap) + `op_menu_right`(AS-IS 권한, ROLE_ADMIN_1~6만; 7/8/10/11은 미구현 역할이라 제외).
2. `MenuService.navTreeFor` + `fragments/admin-nav.html` 3단화(GNB→섹션→링크), `inc_header.jsp` 구조 기준.
3. 변경영향: 현 7그룹 전제 화면·`ManagerAuthAdvice`(activeTopMenuId 계산)·site-config-nav 등 확인.
4. 컴파일까지. 재기동은 사용자.
</content>
</invoke>
