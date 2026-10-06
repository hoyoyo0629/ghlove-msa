package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지자체담당자관리 (메뉴 4402) - AS-IS saleson.shop.user.LocgovPersonInChargeManagerController
 * ({@code /opmanager/user/locgov-charger}) 재현. 구조갭 분리의 나머지 절반이다
 * ({@link OperPersonInChargeAdminController} 주석 참고, menu_url은
 * {@code /admin/person-in-charge/locgov}).
 *
 * <p>AS-IS는 로그인 권한을 두 부류로 나눠 화면을 바꾼다({@code adminRoleType}):
 * <ul>
 *   <li><b>SYS</b>(ROLE_ADMIN_1~4): 전체 지자체를 보고 지자체명 검색·컬럼이 있으며 수정에서
 *       시도·시군구를 바꿀 수 있다. 아이디·이름은 링크가 아니고 <b>지자체명이 링크</b>다.</li>
 *   <li><b>LOC</b>(ROLE_ADMIN_5·6): 자기 지자체만 본다. 지자체명 컬럼이 없고 아이디·이름이 링크다.
 *       등급(Grade)이 또 갈린다 - 주관리자(M)는 수정 가능, <b>부관리자(S)는 회원구분·사용여부
 *       라디오가 disabled</b>이고, 주관리자가 <b>자기 자신</b>을 볼 때는 사용여부만 disabled다.</li>
 * </ul>
 *
 * <p>AS-IS 비활성 상태 보존: 삭제 버튼은 권한 조건을 통과해도 {@code style="display:none;"}으로
 * <b>화면에 보이지 않는다</b>(목록·상세 모두). 엔드포인트는 남겨 두고 버튼만 숨긴 채로 옮겼다.
 */
@Controller
@RequiredArgsConstructor
public class LocgovPersonInChargeAdminController {

    /** AS-IS 화면이 다루는 권한 - 지자체 주·부관리자. */
    private static final List<String> LOCGOV_AUTHORITIES = List.of("ROLE_ADMIN_5", "ROLE_ADMIN_6");

    private static final List<String> SYS_ROLES =
            List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");

    private final PersonInChargeAdminService service;
    private final LocgovClient locgovClient;

    /** AS-IS GET /list - 조회하지 않고 빈 목록. */
    @GetMapping("/admin/person-in-charge/locgov")
    public String list(@ModelAttribute("searchParam") PersonInChargeSearchParam searchParam,
                       HttpServletRequest request, HttpSession session, Model model) {
        searchParam.applyDefaults();
        fill(model, List.of(), 0,
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request),
                session);
        return "person-in-charge/locgov-list";
    }

    /** AS-IS POST /list - 실제 검색. LOC 권한은 자기 지자체만. */
    @PostMapping("/admin/person-in-charge/locgov")
    public String search(@ModelAttribute("searchParam") PersonInChargeSearchParam searchParam,
                         HttpServletRequest request, HttpSession session, Model model) {
        searchParam.applyDefaults();
        Manager viewer = viewer(session);
        Map<String, LocgovClient.LocgovInfo> locgovs = locgovByCode();

        List<Manager> all = service.search(PersonInChargeAdminService.SCOPE_LOCGOV,
                        searchParam.getSrchStartCreated(), searchParam.getSrchEndCreated(), viewer, 0, 1000)
                .content().stream()
                .filter(m -> searchParam.matchesAuthorities(m.getAuthority(), LOCGOV_AUTHORITIES))
                .filter(m -> searchParam.matchesStatus(m.getStatusCode()))
                .filter(m -> matchesKeyword(searchParam, m, locgovs))
                .filter(m -> inViewerScope(viewer, m))
                .toList();

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        fill(model, all.stream().skip(pagination.getStartRow()).limit(pagination.getItemsPerPage()).toList(),
                all.size(), pagination, session);
        model.addAttribute("locgovNames", locgovDisplayNames(locgovs));
        return "person-in-charge/locgov-list";
    }

    /**
     * AS-IS GET /edit/{userId}. 지자체 담당자(LOC)가 <b>다른 지자체</b> 담당자를 열면 AS-IS는
     * 목록으로 되돌린다 - 같게 처리한다.
     */
    @GetMapping("/admin/person-in-charge/locgov/edit/{userId}")
    public String editForm(@PathVariable Long userId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        Manager details = service.get(userId);
        if (!inViewerScope(viewer, details)) {
            return "redirect:/admin/person-in-charge/locgov";
        }
        Map<String, LocgovClient.LocgovInfo> locgovs = locgovByCode();
        LocgovClient.LocgovInfo own = details.getLocgovCode() == null ? null : locgovs.get(details.getLocgovCode());

        model.addAttribute("details", details);
        model.addAttribute("upperLocgovNm", own == null ? null : own.upperLocgovNm());
        model.addAttribute("locgovNm", own == null ? null : own.locgovNm());
        model.addAttribute("upperLocgovCode", own == null ? null : own.upperLocgovCode());
        // AS-IS는 상위 지자체 목록을 공통코드 WDR에서 가져온다 - TO-BE는 지자체 목록에서 시도를 추린다
        model.addAttribute("upperLocgovCodeList", upperLocgovList(locgovs));
        applyRoleFlags(model, viewer, details);
        return "person-in-charge/locgov-edit";
    }

    /** AS-IS GET /locgov/{upperLocgovCode}/list - 시도를 고르면 시군구를 채우는 ajax. */
    @GetMapping("/admin/person-in-charge/locgov/locgov/{upperLocgovCode}/list")
    @ResponseBody
    public Map<String, Object> locgovList(@PathVariable String upperLocgovCode) {
        List<Map<String, String>> data = locgovClient.allLocgovs().stream()
                .filter(l -> upperLocgovCode.equals(l.upperLocgovCode()))
                .sorted(Comparator.comparing(l -> l.locgovNm() == null ? "" : l.locgovNm()))
                .map(l -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("locgovCode", l.locgovCode());
                    row.put("locgovNm", l.locgovNm());
                    return row;
                })
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        return response;
    }

    /** AS-IS POST /edit - ajax. 지자체도 함께 바뀔 수 있다. */
    @PostMapping("/admin/person-in-charge/locgov/edit")
    @ResponseBody
    public Map<String, Object> edit(@RequestParam Long userId,
                                    @RequestParam String authority,
                                    @RequestParam(required = false) String locgovCode,
                                    @RequestParam(required = false) String psitnDeptNm,
                                    @RequestParam(required = false) String ofcpsNm,
                                    @RequestParam(required = false) String statusCode,
                                    HttpSession session) {
        Manager viewer = viewer(session);
        Manager target = service.get(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("isSuccess", true);
        response.put("data", data);
        if (!inViewerScope(viewer, target)) {
            data.put("code", "ERR_NOT_ALLOW");
            return response;
        }
        try {
            // 화면은 AS-IS 그대로 '9'/'2'를 보내고 컬럼값은 ACTIVE/LOCKED라 경계에서 바꾼다
            service.update(PersonInChargeAdminService.SCOPE_LOCGOV, userId, target.getUserName(),
                    target.getEmail(), authority, PersonInChargeAdminService.toStatusCode(statusCode),
                    psitnDeptNm, ofcpsNm, locgovCode);
            data.put("code", "SUCC");
        } catch (ManagerException e) {
            data.put("code", e.getMessage() != null && e.getMessage().contains("주담당자")
                    ? "ERR_MAIN_CNT" : "FAIL");
            data.put("message", e.getMessage());
        } catch (RuntimeException e) {
            data.put("code", "FAIL");
        }
        return response;
    }

    /** AS-IS POST /delete - 화면 버튼은 숨겨져 있지만 엔드포인트는 그대로 둔다(AS-IS 동일). */
    @PostMapping("/admin/person-in-charge/locgov/delete")
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
        try {
            for (Long userId : userIdList) {
                service.delete(PersonInChargeAdminService.SCOPE_LOCGOV, userId, viewer);
            }
            data.put("code", "SUCC");
            data.put("isLogout", userIdList.contains(viewer.getUserId()) ? "Y" : "N");
        } catch (ManagerException e) {
            data.put("code", "ERR_NOT_ALLOW");
        }
        return response;
    }

    /** AS-IS adminRoleType/adminRoleGrade와 그에 따른 입력 가능 여부. */
    private void applyRoleFlags(Model model, Manager viewer, Manager details) {
        String role = viewer == null ? null : viewer.getAuthority();
        boolean sys = role != null && SYS_ROLES.contains(role);
        boolean locMain = "ROLE_ADMIN_5".equals(role);
        boolean locSub = "ROLE_ADMIN_6".equals(role);

        model.addAttribute("adminRole", role);
        model.addAttribute("adminRoleType", sys ? "SYS" : (locMain || locSub ? "LOC" : null));
        model.addAttribute("adminRoleGrade", locMain ? "M" : (locSub ? "S" : null));
        // 수정 버튼: SYS 또는 지자체 주관리자
        model.addAttribute("editable", sys || locMain);
        // 부관리자는 회원구분·사용여부를 못 바꾼다
        model.addAttribute("roleFieldsDisabled", locSub);
        // 주관리자가 자기 자신을 볼 때는 사용여부를 못 바꾼다
        model.addAttribute("statusDisabled", locSub
                || (locMain && details != null && viewer != null
                    && viewer.getUserId().equals(details.getUserId())));
    }

    private void fill(Model model, List<Manager> list, int count, Pagination pagination, HttpSession session) {
        Manager viewer = viewer(session);
        String role = viewer == null ? null : viewer.getAuthority();
        boolean sys = role != null && SYS_ROLES.contains(role);
        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("adminRole", role);
        model.addAttribute("adminRoleType", sys ? "SYS" : ("ROLE_ADMIN_5".equals(role)
                || "ROLE_ADMIN_6".equals(role) ? "LOC" : null));
        model.addAttribute("adminRoleGrade", "ROLE_ADMIN_5".equals(role) ? "M"
                : ("ROLE_ADMIN_6".equals(role) ? "S" : null));
        if (!model.containsAttribute("locgovNames")) {
            model.addAttribute("locgovNames", locgovDisplayNames(locgovByCode()));
        }
    }

    /** LOC 권한은 자기 지자체 담당자만 볼 수 있다(AS-IS edit의 되돌리기 규칙). */
    private static boolean inViewerScope(Manager viewer, Manager target) {
        if (viewer == null || target == null) {
            return target != null;
        }
        if (!LOCGOV_AUTHORITIES.contains(viewer.getAuthority())) {
            return true;
        }
        return viewer.getLocgovCode() != null && viewer.getLocgovCode().equals(target.getLocgovCode());
    }

    /** 검색구분 - SYS만 쓰는 LOCGOV_NM은 지자체명으로 찾는다. */
    private boolean matchesKeyword(PersonInChargeSearchParam p, Manager m,
                                   Map<String, LocgovClient.LocgovInfo> locgovs) {
        if (!"LOCGOV_NM".equals(p.getSrchKey())) {
            return p.matchesKeyword(m.getLoginId(), m.getUserName(), m.getPhoneNumber());
        }
        if (p.getSrchValue() == null || p.getSrchValue().isBlank()) {
            return true;
        }
        LocgovClient.LocgovInfo info = m.getLocgovCode() == null ? null : locgovs.get(m.getLocgovCode());
        if (info == null) {
            return false;
        }
        String name = (info.upperLocgovNm() == null ? "" : info.upperLocgovNm())
                + " " + (info.locgovNm() == null ? "" : info.locgovNm());
        return name.contains(p.getSrchValue().trim());
    }

    private Map<String, LocgovClient.LocgovInfo> locgovByCode() {
        Map<String, LocgovClient.LocgovInfo> map = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> map.put(l.locgovCode(), l));
        return map;
    }

    /** 목록의 지자체명 칸 - AS-IS는 상위지자체명 + ' ' + 지자체명이다. */
    private static Map<String, String> locgovDisplayNames(Map<String, LocgovClient.LocgovInfo> locgovs) {
        Map<String, String> names = new LinkedHashMap<>();
        locgovs.forEach((code, l) -> names.put(code,
                (l.upperLocgovNm() == null ? "" : l.upperLocgovNm() + " ")
                        + (l.locgovNm() == null ? "" : l.locgovNm())));
        return names;
    }

    /** 시도 목록(코드+이름) - 지자체 목록에서 중복 없이 추린다. */
    private static List<Map<String, String>> upperLocgovList(Map<String, LocgovClient.LocgovInfo> locgovs) {
        Map<String, String> distinct = new LinkedHashMap<>();
        locgovs.values().stream()
                .filter(l -> l.upperLocgovCode() != null && !l.upperLocgovCode().isBlank())
                .sorted(Comparator.comparing(l -> l.upperLocgovNm() == null ? "" : l.upperLocgovNm()))
                .forEach(l -> distinct.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm()));
        return distinct.entrySet().stream()
                .map(e -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("id", e.getKey());
                    row.put("label", e.getValue());
                    return row;
                })
                .toList();
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
