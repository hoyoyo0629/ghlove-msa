---
name: op-qna-split-by-qna-type
description: "op_qna 한 표를 QNA_TYPE으로 세 화면이 나눠 쓴다 - 0=1:1문의(5102), 1=상품문의(중지), 2=공개Q&A(5112). 공개 쪽 필터 누락은 2026-10-05 수정"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-05T04:13:13.811Z
---

**AS-IS `Qna` 상수**: `QNA_GROUP_TYPE_INDIVIDUAL = "0"` · `QNA_GROUP_TYPE_ITEM = "1"` ·
`QNA_GROUP_TYPE_QNA = "2"`. `OP_QNA`/`OP_QNA_ANSWER`/`OP_QNA_FILE` 한 세트를 세 화면이 나눠 쓴다.

| QNA_TYPE | 화면 | 운영자 메뉴 | TO-BE URL |
|---|---|---|---|
| `0` | 1:1 문의(마이페이지 개인문의) | 5102 | `/admin/inquiries` |
| `1` | 상품문의 | 5103 **중지** | - |
| `2` | 고객센터 공개 Q&A 게시판 | 5112 | `/qna-admin` |

**새 글을 넣는 코드는 반드시 `qna_type`을 넣어야 한다.** 빠뜨리면 그 글은 어느 운영자 화면에도
보이지 않는다(두 화면 모두 타입으로 거른다). TO-BE `QnaService.ask()`가 그 상태였다.

**조회하는 코드도 타입으로 걸러야 한다.** 2026-10-05에 공개 쪽 3곳을 고쳤다:
`ask()`(→`'0'` 저장) · `myInquiries()`(→`'0'`) · `publicBoard()`·`publicBoardDetail()`(→`'2'`).
필터가 없던 동안 **마이페이지 1:1문의의 제목·작성자가 공개 Q&A 게시판에 노출**되고
비밀글이 아니면 id만 바꿔 본문까지 열람할 수 있었다.

**관련 주의**
- 내부문의(5120)는 이 표가 아니라 **`g_qna_admin`**이다(`/admin/internal-inquiry`).
- 1:1문의 목록은 작성자명을 **마스킹하지 않고**, 공개 Q&A(5112) 목록은 **마스킹한다** - 화면마다 다르다.
- 답변 저장 시 **5112만** 국민비서 문자를 보낸다([[ips-sms-sender-lives-in-admin]]). 5102는 안 보낸다.
- 삭제·첨부 로직은 AS-IS가 두 화면에서 같은 서비스 메서드를 부르므로 TO-BE도
  `QnaOpenAdminService`의 것을 공유한다.
- **공개 Q&A 글쓰기 경로가 TO-BE에 없다**(AS-IS `api/qna`에는 있다) - `'2'` 글은 시드뿐이다.
