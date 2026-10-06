-- 구조갭 해소: 메뉴 5110 지자체공지사항과 5111 공지사항이 **둘 다 `/admin/notices`**를 가리키고 있었다.
--
-- AS-IS는 컨트롤러·매퍼가 둘로 나뉘어 있고 조회 범위가 다르다(표는 하나 - `op_notice`):
--   - 5111 공지사항       `/opmanager/notice/list`        · NoticeManagerController
--       → 검색조건의 지자체코드를 **'00000'으로 고정**해 **전체공지만** 본다.
--   - 5110 지자체공지사항 `/opmanager/locgov-notice/list` · LocgovNoticeManagerController
--       → **지자체 담당자(LOC)일 때만** 자기 지자체로 스코프하고, 시스템·행안부는 지자체 조건을
--         걸지 않아 전체공지까지 포함해 모든 행을 본다(AS-IS getLocgovNoticeList의 조건부 WHERE).
--         목록에 지자체명(g_locgov 조인)을 함께 보여준다.
--
-- 그래서 5110만 자기 URL로 떼어낸다. 등록·수정·삭제·게시토글은 AS-IS처럼 한 경로
-- (`/admin/notices/...`)를 공유한다 - 같은 표의 같은 자원이다.
--
-- 참고: TO-BE 데이터는 전체공지의 locgov_code를 비워 두었다(실측 빈 값 14행, 지자체코드 16행).
-- AS-IS의 '00000' 관례와 달라서 화면 쪽에서 NULL·빈값·'00000'을 모두 전체공지로 본다
-- (OperationContentController.isWholeNotice - 담당자 사용여부 9/2↔ACTIVE/LOCKED와 같은 경계 번역).

update admin.op_menu
   set menu_url = '/admin/locgov-notices'
 where menu_id = 5110 and menu_url = '/admin/notices';
