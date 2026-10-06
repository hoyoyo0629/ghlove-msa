package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.PersonInChargeAdminService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.PersonInChargeSearchParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 운영관리자 (메뉴 4501) - AS-IS saleson.shop.user.OperPersonInChargeManagerController
 * ({@code /opmanager/user/oper-charger}) 재현.
 *
 * <p><b>구조갭 정리</b>: TO-BE는 AS-IS의 지자체담당자관리(4402)와 운영관리자(4501)를
 * {@code /admin/person-in-charge} 한 화면의 {@code scope} 탭으로 통합해 두고 두 메뉴가 같은 URL을
 * 가리켰다(시스템관리의 1404·1405와 같은 유형). AS-IS는 <b>컨트롤러·JSP·검색조건·컬럼이 모두
 * 다른 별개 화면</b>이라 분리한다 - 이 컨트롤러가 4501이고 menu_url은
 * {@code /admin/person-in-charge/oper}다({@code migration-admin-menu-4501-oper-charger.sql}).
 *
 * <p>AS-IS 동작: 진입(GET)에서는 조회하지 않고(빈 목록) 검색(POST)해야 조회된다. 등록일 기본값은
 * 오늘. 대상 권한은 ROLE_ADMIN_1~4이고, 삭제는 <b>ROLE_ADMIN_1·3만</b> 가능하다(삭제 버튼도
 * 그 권한에만 보인다). 수정에서 행안부 주담당자(ROLE_ADMIN_3)는 전체 2명까지만 지정할 수 있다.
 *
 * <p>AS-IS 결함 1건 - <b>고쳤다</b>: oper-charger/list.jsp의 삭제가
 * {@code /opmanager/user/locgov-charger/delete}(지자체담당자 엔드포인트)로 POST한다(복사 흔적).
 * 삭제 버튼이 ROLE_ADMIN_1·3에만 보이고 그 두 권한은 양쪽 엔드포인트에서 모두 허용되므로
 * 실제로 관측되는 동작은 같다 - 화면이 분리된 TO-BE에서 남겨두면 오히려 앞뒤가 안 맞아
 * 자기 엔드포인트로 바로잡았다.
 */
@Controller
@RequiredArgsConstructor
public class OperPersonInChargeAdminController {

    /** AS-IS 화면이 다루는 권한 - 시스템/행안부 주·부. */
    private static final List<String> OPER_AUTHORITIES =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");

    /** 삭제 버튼 노출 권한(AS-IS list.jsp의 c:if). */
    private static final List<String> DELETE_VISIBLE_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_3");

    private final PersonInChargeAdminService service;

    /** AS-IS GET /list - 조회하지 않고 빈 목록. */
    @GetMapping("/admin/person-in-charge/oper")
    public String list(@ModelAttribute("searchParam") PersonInChargeSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        searchParam.applyDefaults();
        fill(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request),
                session);
        return "person-in-charge/oper-list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping("/admin/person-in-charge/oper")
    public String search(@ModelAttribute("searchParam") PersonInChargeSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        searchParam.applyDefaults();
        Manager viewer = viewer(session);

        List<Manager> all = service.search(PersonInChargeAdminService.SCOPE_OPERATOR,
                        searchParam.getSrchStartCreated(), searchParam.getSrchEndCreated(), viewer, 0, 1000)
                .content().stream()
                .filter(m -> searchParam.matchesAuthorities(m.getAuthority(), OPER_AUTHORITIES))
                .filter(m -> searchParam.matchesStatus(m.getStatusCode()))
                .filter(m -> searchParam.matchesKeyword(m.getLoginId(), m.getUserName(), m.getPhoneNumber()))
                .toList();

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        fill(model, all.stream().skip(pagination.getStartRow()).limit(pagination.getItemsPerPage()).toList(),
                all.size(), pagination, session);
        return "person-in-charge/oper-list";
    }

    /** AS-IS GET /edit/{userId}. */
    @GetMapping("/admin/person-in-charge/oper/edit/{userId}")
    public String editForm(@PathVariable Long userId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        model.addAttribute("details", service.get(userId));
        model.addAttribute("adminRole", viewer == null ? null : viewer.getAuthority());
        model.addAttribute("loginUserId", viewer == null ? null : viewer.getUserId());
        model.addAttribute("editable", viewer != null && DELETE_VISIBLE_ROLES.contains(viewer.getAuthority()));
        return "person-in-charge/oper-edit";
    }

    /** AS-IS POST /edit - ajax. 응답은 {isSuccess, data:{code}}이고 화면은 code=="SUCC"만 성공으로 본다. */
    @PostMapping("/admin/person-in-charge/oper/edit")
    @ResponseBody
    public Map<String, Object> edit(@RequestParam Long userId,
                                    @RequestParam(required = false) String userName,
                                    @RequestParam(required = false) String email,
                                    @RequestParam String authority,
                                    @RequestParam(required = false) String statusCode,
                                    @RequestParam(required = false) String psitnDeptNm,
                                    @RequestParam(required = false) String ofcpsNm) {
        // 화면은 AS-IS 그대로 '9'/'2'를 보내고 컬럼값은 ACTIVE/LOCKED라 경계에서 바꾼다
        return result(() -> service.update(PersonInChargeAdminService.SCOPE_OPERATOR, userId, userName, email,
                authority, PersonInChargeAdminService.toStatusCode(statusCode), psitnDeptNm, ofcpsNm));
    }

    /**
     * AS-IS POST /delete - ajax. 응답은 {isSuccess, data:{code, isLogout}}이고,
     * 삭제 대상에 본인이 포함되면 isLogout=Y로 화면이 로그아웃시킨다.
     */
    @PostMapping("/admin/person-in-charge/oper/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(name = "userIdList", required = false) List<Long> userIdList,
                                      HttpSession session) {
        Manager viewer = viewer(session);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", "FAIL");
        data.put("isLogout", "N");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        if (userIdList == null || userIdList.isEmpty() || viewer == null) {
            return response;
        }
        // AS-IS: 허용권한이 아니면 ERR_NOT_ALLOW
        if (!DELETE_VISIBLE_ROLES.contains(viewer.getAuthority())) {
            data.put("code", "ERR_NOT_ALLOW");
            return response;
        }
        try {
            for (Long userId : userIdList) {
                service.delete(PersonInChargeAdminService.SCOPE_OPERATOR, userId, viewer);
            }
            data.put("code", "SUCC");
            data.put("isLogout", userIdList.contains(viewer.getUserId()) ? "Y" : "N");
        } catch (ManagerException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    private void fill(Model model, List<Manager> list, int count, Pagination pagination, HttpSession session) {
        Manager viewer = viewer(session);
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("adminRole", viewer == null ? null : viewer.getAuthority());
        model.addAttribute("deletable", viewer != null && DELETE_VISIBLE_ROLES.contains(viewer.getAuthority()));
    }

    private Map<String, Object> result(Runnable action) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        try {
            action.run();
            data.put("code", "SUCC");
        } catch (ManagerException e) {
            // AS-IS는 주담당자 정원 초과를 ERR_MAIN_CNT로 구분해 화면에서 별도 문구를 띄운다
            data.put("code", e.getMessage() != null && e.getMessage().contains("주담당자")
                    ? "ERR_MAIN_CNT" : "FAIL");
            data.put("message", e.getMessage());
        } catch (RuntimeException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
