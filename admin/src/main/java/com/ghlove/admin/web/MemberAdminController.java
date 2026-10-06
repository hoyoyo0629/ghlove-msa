package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 회원 계정 사후조치 - <b>AS-IS 일반회원관리(메뉴 4101)에는 없는 TO-BE 전용 기능</b>이다.
 *
 * <p>계정잠금 해제·RBAC 권한 회수는 SFR-002 요건으로 추가된 것이고, AS-IS 4101 상세화면
 * (user/customer/details.jsp)의 버튼은 배송지 관리·개인정보 열람·회원탈퇴·목록 네 개뿐이다.
 * 4101을 AS-IS대로 이식하면서({@link GeneralCustomerAdminController}) 이 두 기능을 없애지 않고
 * 엔드포인트를 여기 남겨 두고, 상세화면 맨 아래 <b>"운영 기능(AS-IS 외)" 블록</b>으로 분리해
 * 노출한다 - AS-IS 화면 영역과 섞지 않기 위한 조치다.
 *
 * <p>목록·상세·개인정보 열람·회원탈퇴는 모두 {@link GeneralCustomerAdminController}로 옮겼다
 * (AS-IS 라우트 모양 {@code /list}·{@code /details/{userId}}·{@code /popup/*}를 그대로 쓴다).
 * 세션 기반으로 비밀번호를 한 번만 묻던 {@code /{userId}/reveal} 게이트도 AS-IS 방식
 * (열람할 때마다 비밀번호 확인 팝업 + 열람이력 적재)으로 대체됐다.
 */
@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberAdminClient memberAdminClient;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;

    /** 계정잠금 해제 (SFR-002, AS-IS 4101에는 없음). */
    @PostMapping("/{userId}/unlock")
    public String unlock(@PathVariable Long userId) {
        try {
            memberAdminClient.unlock(userId);
        } catch (ManagerException e) {
            return flashRedirect.to("/admin/members/details/" + userId, e.getMessage());
        }
        return "redirect:/admin/members/details/" + userId;
    }

    /** RBAC 권한 회수 (SFR-002, AS-IS 4101에는 없음). */
    @PostMapping("/{userId}/roles/{authority}/revoke")
    public String revokeRole(@PathVariable Long userId, @PathVariable String authority) {
        try {
            memberAdminClient.revokeRole(userId, authority);
        } catch (ManagerException e) {
            return flashRedirect.to("/admin/members/details/" + userId, e.getMessage());
        }
        return "redirect:/admin/members/details/" + userId;
    }

}
