package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OperationContentService;
import com.ghlove.admin.service.OrderAdminClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** 관리자 로그인 직후 첫 화면(대시보드) - AS-IS CommonController의 opmanager/main-* 위젯 세트
 * (주문/회원/공지 카운트) 재현. TO-BE는 그동안 로그인하면 곧바로 첫 접근가능 메뉴로
 * 넘어가버려 이 랜딩 화면 자체가 없었다. */
@Controller
@RequiredArgsConstructor
public class AdminHomeController {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderAdminClient orderAdminClient;
    private final MemberAdminClient memberAdminClient;
    private final OperationContentService operationContentService;

    @GetMapping("/admin")
    public String home(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String today = LocalDate.now().format(YMD);

        long todayOrderCount = 0;
        try {
            String locgovCode = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null;
            todayOrderCount = orderAdminClient.search(manager, locgovCode, null, null, null, today, today, 0, 1)
                    .totalElements();
        } catch (Exception ignored) {
            // 위젯 하나가 실패해도 나머지 대시보드는 정상 노출되어야 한다.
        }

        long todayMemberCount = 0;
        try {
            todayMemberCount = memberAdminClient.search(today, today, null, null, null, null, 0, 1).totalElements();
        } catch (Exception ignored) {
        }

        model.addAttribute("todayOrderCount", todayOrderCount);
        model.addAttribute("todayMemberCount", todayMemberCount);
        model.addAttribute("recentNotices", operationContentService.notices(null).stream().limit(5).toList());
        return "admin/home";
    }
}
