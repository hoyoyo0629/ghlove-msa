package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.AdminMainService;
import com.ghlove.admin.service.CommonCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 관리자 메인화면 - AS-IS {@code OpmanagerController.index}(/opmanager, main/index.jsp) +
 * {@code CommonController}의 {@code /common/opmanager/*} 위젯 엔드포인트 + {@code MainController}의 콜 등록 팝업 이식.
 * 데이터 엔드포인트 주소는 화면 JS가 부르는 AS-IS 경로 그대로 두었다({@code /common/manager/notice/list}와 같은 방식).
 * 응답 봉투도 AS-IS JsonViewUtils 모양({@code isSuccess}·{@code data}).
 *
 * <p>이전 TO-BE 화면(오늘 주문 건수·오늘 신규 회원·최근 공지사항)은 AS-IS에 없는 위젯이라 걷어냈다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminHomeController {

    /** AS-IS UserServiceImpl이 로그인 직후 세션에 남기는 표시 - 메인 진입 때 한 번 읽고 지운다. */
    public static final String SESSION_LOGIN_NOW = "loginNow";

    private final AdminMainService mainService;
    private final CommonCodeService commonCodeService;

    @GetMapping("/admin")
    public String home(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        model.addAttribute("adminRole", manager.getAuthority());
        model.addAttribute("adminRoleType", AdminMainService.screenRole(manager.getAuthority()));
        model.addAttribute("serverIp", serverIp());

        // AS-IS: 로그인 직후 첫 진입이면 오프라인 담당자에게 은행·지점·이름 확인 문구를 띄운다.
        if ("Y".equals(session.getAttribute(SESSION_LOGIN_NOW))) {
            session.removeAttribute(SESSION_LOGIN_NOW);
            String bankNm = commonCodeService.labelsOf("OFF_BANK_LIST").get(manager.getBankCode());
            String message = mainService.offManagerLoginMessage(manager, bankNm);
            if (!message.isEmpty()) {
                model.addAttribute("alertMessage", message);
            }
        }

        model.addAttribute("showPopup", mainService.popupOpen("period", "9990") ? "Y" : "");
        model.addAttribute("showPopup2", mainService.popupOpen("period2", "9991") ? "Y" : "");
        return "admin/home";
    }

    /**
     * AS-IS {@code OpmanagerController.dashboard}(/opmanager/dashboard, main/dashboard.jsp) - 메인의 옛 사본.
     * AS-IS에도 메뉴·링크가 없어 주소로만 들어간다. 로그인 안내·설문/엑셀 팝업 판정이 없고, 기부일자 칸이
     * GET 폼(searchParam)에 묶여 {@code ?shCntrDeStart=} 값이 다시 채워진다. 데이터는 메인과 같은 엔드포인트를 쓴다.
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model,
            @RequestParam(required = false) String shCntrDeStart) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        model.addAttribute("adminRole", manager.getAuthority());
        model.addAttribute("adminRoleType", AdminMainService.screenRole(manager.getAuthority()));
        model.addAttribute("serverIp", serverIp());
        model.addAttribute("shCntrDeStart", shCntrDeStart);
        return "admin/dashboard";
    }

    // ---- 위젯 데이터 (AS-IS CommonController /common/opmanager/*) ----

    @PostMapping("/common/opmanager/main-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mainInfo(HttpSession session, HttpServletRequest request) {
        return ajax(request, () -> mainService.mainInfo(manager(session)));
    }

    @PostMapping("/common/opmanager/main-boardInfo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> boardInfo(HttpServletRequest request) {
        return ajax(request, mainService::boardInfo);
    }

    @PostMapping("/common/opmanager/main-amountChart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> amountChart(HttpSession session, HttpServletRequest request) {
        return ajax(request, () -> mainService.amountChart(manager(session)));
    }

    @PostMapping("/common/opmanager/main-countChart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> countChart(HttpSession session, HttpServletRequest request) {
        return ajax(request, () -> mainService.countChart(manager(session)));
    }

    @PostMapping("/common/opmanager/main-chartInfo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chartInfo(HttpSession session, HttpServletRequest request) {
        return ajax(request, () -> mainService.chartInfo(manager(session)));
    }

    @PostMapping("/common/opmanager/main-search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mainSearch(HttpServletRequest request,
            @RequestParam(required = false) String shCntrDeStart) {
        if (isAjax(request) && (shCntrDeStart == null || shCntrDeStart.isEmpty())) {
            return ResponseEntity.ok(failure("기부일자가 없습니다."));
        }
        return ajax(request, () -> mainService.tableInfo(shCntrDeStart));
    }

    @PostMapping("/common/opmanager/call-search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> callSearch(HttpServletRequest request,
            @RequestParam(required = false) String shCntrDeStart) {
        return ajax(request, () -> mainService.callState(shCntrDeStart));
    }

    /** AS-IS는 이 둘과 get-user·알림동의에 ajax 확인이 없다. */
    @GetMapping("/common/opmanager/gift-search")
    @ResponseBody
    public Map<String, Object> giftSearch() {
        return result(mainService::giftUnregisteredLocgovNames);
    }

    @GetMapping("/common/opmanager/gift-amount")
    @ResponseBody
    public Map<String, Object> giftAmount(@RequestParam(required = false) String shCntrDeStart) {
        return result(() -> mainService.giftItemCount(shCntrDeStart));
    }

    @PostMapping("/common/opmanager/get-user")
    @ResponseBody
    public Map<String, Object> getUser(@RequestParam String shCntrDeStart) {
        return result(() -> mainService.dashboardReport(shCntrDeStart));
    }

    /** AS-IS: 실패해도 빈 결과로 성공 응답(로그만 남긴다). */
    @PostMapping("/common/opmanager/main-notification-agree-member")
    @ResponseBody
    public Map<String, Object> notificationAgree() {
        Object data;
        try {
            data = mainService.notificationAgree();
        } catch (RuntimeException e) {
            log.error("ERROR: getNotificationAgreeMemberCount - 알림동의유저수 카운팅 실패");
            data = Map.of();
        }
        return success(data);
    }

    // ---- 콜 수 등록 팝업 (AS-IS MainController /opmanager/popup/insertCall) ----

    @GetMapping("/admin/popup/insertCall")
    public String insertCallForm(Model model) {
        model.addAttribute("today", java.time.LocalDate.now().toString());
        return "admin/popup/insert-call";
    }

    /** AS-IS: 성공 {status: OK}, 실패 {status: FAILED_DEPENDENCY}. */
    @PostMapping("/admin/popup/insertCall")
    @ResponseBody
    public Map<String, Object> insertCall(HttpSession session,
            @RequestParam(defaultValue = "0") long callKookmin, @RequestParam(defaultValue = "0") long callLov,
            @RequestParam(defaultValue = "0") long callGiver, @RequestParam(defaultValue = "0") long callNhbank,
            @RequestParam(defaultValue = "0") long callPlatform) {
        try {
            mainService.insertCall(callKookmin, callLov, callGiver, callNhbank, callPlatform,
                    manager(session).getUserId());
            return Map.of("status", "OK");
        } catch (RuntimeException e) {
            log.error("콜 등록 실패", e);
            return Map.of("status", "FAILED_DEPENDENCY");
        }
    }

    // ---- 공통 ----

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS {@code if (!requestContext.isAjaxRequest()) throw new NotAjaxRequestException()} - ajax가 아니면 거절한다. */
    private static ResponseEntity<Map<String, Object>> ajax(HttpServletRequest request, Supplier<Object> body) {
        if (!isAjax(request)) {
            return ResponseEntity.badRequest().body(failure("잘못된 접근입니다."));
        }
        return ResponseEntity.ok(result(body));
    }

    private static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private static Map<String, Object> result(Supplier<Object> body) {
        try {
            return success(body.get());
        } catch (RuntimeException e) {
            log.error("관리자 메인 조회 실패", e);
            return failure("처리 중 오류가 발생했습니다.");
        }
    }

    private static Map<String, Object> success(Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", true);
        body.put("data", data);
        return body;
    }

    private static Map<String, Object> failure(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", false);
        body.put("errorMessage", message);
        return body;
    }

    /** AS-IS getServerIp() - 서버 자신의 IP(화면 hidden #serverIp). */
    private static String serverIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }
}
