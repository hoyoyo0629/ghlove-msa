# member 연동해지(디지털원패스/카카오) parity audit

작성 2026-09-22. AS-IS 전수대조 후 구현. 근거는 모두 AS-IS 소스(file:line).

## 1. AS-IS 전수 (확인 완료)

### 진입점 — 회원정보수정 `ghlove-frontend/users/modify.html`
회원구분 필드 옆(96~107) 버튼 2개 + 하단 회원탈퇴 버튼(431):
- `디지털원패스\n연동 해지` — `v-if="param.userKeyYN=='Y'"` → `onepassCancel()`
- `카카오\n연동 해지` — `v-if="param.kakaoUserKeyYN=='Y'"` → `kakaoLinkClear()`
- `회원탈퇴` — `v-if="param.userKeyYN=='N'"` → `goToSecede()` = `/users/secede.html`(일반, 비번 필요)

**onepassCancel()** (956):
- userKey/intfToken 쿠키 없으면 alert `"연동해지를 위해서는 디지털 원패스로 다시 로그인 해주세요."`
- confirm `"디지털원패스 회원 연동해지를 하시겠습니까?"`
  - `loginPathCode=='300'`(원패스가입) → `/users/onepass_secede.html` (연동해지+탈퇴)
  - else(일반가입+원패스 부가연동) → POST `/api/auth/onepass-unlink` (연동만 해제, reload)

**kakaoLinkClear()** (1408): POST `/api/kakao-link/kakao-link-clear`
- `result=='SUCCESS'` → alert `"카카오 계정 연동해제가 완료되었습니다."` reload (연동만 해제)
- `result=='CHECK_SECEDE'` → confirm `"카카오 계정 연동해제시 탈퇴가 같이 진행됩니다. 진행하시겠습니까?"` → `/users/secede-kakao.html`

### 연동해지 탈퇴화면
`users/onepass_secede.html` (제목 "연동해지"):
- init POST `/api/user/getSecedeInfo` → {loginId,userName,leaveCodeList}
- 탈퇴사유(leaveCode) 필수, 불편사항(leaveReason)
- submit POST `/api/auth/onepass-cancel` {userKey,intfToken,leaveCode,leaveReason}
  → `info.value=='00'`이면 alert(info.message) 후 logout

`users/secede-kakao.html` (제목 "회원탈퇴"):
- init POST `/api/user/getSecedeInfo` → +pointList(지자체별 잔여포인트 표)
- 탈퇴사유 필수 → confirm `"회원 탈퇴 시 회원 서비스를 모두 사용할 수 없습니다.\n정말 탈퇴하시겠습니까?"`
- submit POST `/api/kakao-link/kakao-link-secede` {loginId,password,leaveCode,leaveReason}
  → `result=='SUCCESS'`이면 alert `"탈퇴처리가 정상처리 되었습니다."` 후 logout / `errMsg`면 alert

### AS-IS 백엔드
- `AuthController#onepassCancel` (1830): `ApiSendHandler.InterLockRelease(userKey,intfToken)`(원패스 외부 연계해지 API) 성공 시 `userService.updateUserKeyStatusCode(userId,leaveCode,leaveReason)`(=일반 탈퇴처리 동일 계열: 당해 기부액 스냅샷 → OP_USER/DETAIL 개인정보 NULL → 관심지자체/답례품/권한 삭제).
- `KakaoLinkController#kakaoLinkSecede` (106): `kakaoLinkService.kakaoLinkSecedeByUserId(userId,leaveCode,leaveReason)`(카카오 unlink + 탈퇴처리).
- `KakaoLinkServiceImpl#kakaoLinkClearByUserId` (161): `loginPathCode` 100/300 → 카카오 유저키 삭제+톡키트 unlink → SUCCESS / `500` → CHECK_SECEDE.
- `getSecedeInfo` (UserController:129): {loginId,userName,leaveCodeList=LEAVE_CODE,pointList,loginPathCode}.

## 2. MSA 매핑 결정 (기록 — 임의발명 아님)

| AS-IS | MSA 대응 | 근거 |
|---|---|---|
| `userKeyYN=='Y'` (원패스 연동) | `loginPathCode=='300'` | MSA 원패스=CI기반 가입, 별도 userKey 컬럼 없음. loginPathCode 300=ONEPASS([[ExternalLoginService]] LOGIN_PATH_BY_PROVIDER) |
| `kakaoUserKeyYN=='Y'` | `loginPathCode=='500'` 또는 `kakaoUserKey!=null` | 카카오 가입경로(500) 또는 부가연동 키 보유. mock 카카오가입은 kakaoUserKey를 안 넣으므로(프로필 신원) 500도 포함해야 버튼이 뜬다 |
| InterLockRelease / 카카오 톡키트 unlink | `OnePassClient.releaseInterlock()` / `KakaoCertClient.unlink()` | 로그인과 동일하게 실연계 off면 통과(mock), on이면 실제 API. 현재 off |
| userKey/intfToken 쿠키 가드 | (재현 안 함) | MSA 인증은 GH_AUTH JWT+세션. 원패스 세션쿠키 자체가 없어, 재현 시 항상 실패→기능검증 불가. 가드 의도(원패스 인증세션 필요)를 loginPathCode로 대체 |
| `getSecedeInfo` | 기존 `GET /api/withdraw-info` 재사용 | {loginId,userName,leaveCodeList,pointSummary} 동일 필드 |

## 3. 구현 (2026-09-22)
- member `ProfileResponse` += loginPathCode/userKeyYN/kakaoUserKeyYN.
- member `MemberService`: `withdraw`(비번확인)에서 공통본문 `applyWithdrawal` 추출, `secedeExternal`(비번 없이 탈퇴) + `clearKakaoLink`(500→CHECK_SECEDE, 그 외→키삭제 SUCCESS) 추가.
- member `OnePassClient.releaseInterlock()` / `KakaoCertClient.unlink()` 추가(off면 통과).
- member `AccountUnlinkApiController`(신규): `POST /api/auth/onepass-cancel`, `/api/kakao-link/kakao-link-clear`, `/api/kakao-link/kakao-link-secede`. 성공 시 session invalidate + 쿠키 clear.
- storefront `ProfileView.vue`: 회원구분 옆 연동해지 버튼 2개(조건부) + 회원탈퇴 버튼 `userKeyYN=='N'` 조건 + onepassCancel/kakaoLinkClear.
- storefront `OnepassSecedeView.vue`(/mypage/onepass-secede), `KakaoSecedeView.vue`(/mypage/secede-kakao) 신규 + 라우트.

## 4. 보류(기록) — DA/외부연계 개방 대기
- **`/api/auth/onepass-unlink`(원패스 연동만 해제, 탈퇴X)**: MSA는 원패스가 가입경로(300)여서 "탈퇴 없는 원패스 부가연동 해제" 상태가 성립하지 않음(onepassCancel은 300→항상 secede). 부가연동 도입/DA 설계 시. → [[member-service-deferred-items]]
- **실 연계해지 API 본체**(InterLockRelease/카카오 톡키트 unlink): 방화벽·실 IdP 개방 시 client 내부만 교체. 현재 off 통과.
- 카카오 mock 가입이 kakaoUserKey를 채우지 않는 점은 mock 한계(실연계 on이면 채워짐).
