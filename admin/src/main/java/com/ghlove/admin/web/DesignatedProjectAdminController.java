package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.DesignatedProjectClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 지정기부(designated-donation) 관리 (AS-IS opmanager/designated-donation). 실제
 *  데이터는 donation 서비스에 있고(DesignatedProjectClient), 여기는 admin 콘솔의
 *  로그인/RBAC과 화면만 담당한다. 갤러리 이미지 관리는 공개 상세화면 자체가 대표이미지
 *  1장 구조로 이미 스코프 밖(designated-detail.html) - 여기서도 다시 만들지 않는다. */
@Controller
@RequiredArgsConstructor
public class DesignatedProjectAdminController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    private final DesignatedProjectClient designatedProjectClient;
    private final LocgovClient locgovClient;

    @GetMapping("/designated-projects")
    public String list(Model model) {
        model.addAttribute("projects", designatedProjectClient.listAll());
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("bsnsTypes", designatedProjectClient.codesOf("DSGN_BSNS_TYPE"));
        return "designated/list";
    }

    @GetMapping("/designated-projects/new")
    public String newForm(Model model) {
        model.addAttribute("project", null);
        commonFormAttrs(model, null);
        return "designated/form";
    }

    @GetMapping("/designated-projects/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("project", designatedProjectClient.get(id));
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/designated-projects";
        }
        commonFormAttrs(model, id);
        model.addAttribute("notices", designatedProjectClient.noticesOf(id));
        model.addAttribute("approvalLog", designatedProjectClient.approvalLogOf(id));
        return "designated/form";
    }

    private void commonFormAttrs(Model model, Long id) {
        model.addAttribute("statusLabels", statusLabels());
        model.addAttribute("bsnsTypes", designatedProjectClient.codesOf("DSGN_BSNS_TYPE"));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("departments", designatedProjectClient.departments(null));
        model.addAttribute("projectId", id);
    }

    @PostMapping("/designated-projects")
    public String create(@ModelAttribute DesignatedProjectClient.ProjectForm form,
                          @RequestParam(required = false) MultipartFile image,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.create(toYmd(form), image, canSelfApprove(manager), manager.getUserId());
            return "redirect:/designated-projects";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("project", form);
            commonFormAttrs(model, null);
            return "designated/form";
        }
    }

    @PostMapping("/designated-projects/{id}")
    public String update(@PathVariable Long id, @ModelAttribute DesignatedProjectClient.ProjectForm form,
                          @RequestParam(required = false) MultipartFile image,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.update(id, toYmd(form), image, canSelfApprove(manager), manager.getUserId());
            return "redirect:/designated-projects/" + id;
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("project", form);
            commonFormAttrs(model, id);
            model.addAttribute("notices", designatedProjectClient.noticesOf(id));
            model.addAttribute("approvalLog", designatedProjectClient.approvalLogOf(id));
            return "designated/form";
        }
    }

    /** <input type="date">는 yyyy-MM-dd로 제출되지만 donation은 AS-IS 원본 그대로
     *  yyyyMMdd(8자리)로 저장한다 - give-operation 라운드에서 겪은 것과 동일한 형식차,
     *  여기서 미리 변환해 넘긴다. */
    private static DesignatedProjectClient.ProjectForm toYmd(DesignatedProjectClient.ProjectForm form) {
        return new DesignatedProjectClient.ProjectForm(form.dsgnDntnBizTtl(), form.dsgnDntnBizCn(),
                stripDashes(form.dsgnDntnBizBgngYmd()), stripDashes(form.dsgnDntnBizEndYmd()), form.goalAmt(),
                form.dsgnDntnBizSttsCd(), form.rlsYn(), form.lclgvCd(), form.dsgnDntnBizSeCd(),
                form.bsnsSubType(), form.contentEtc(), form.deptId());
    }

    private static String stripDashes(String ymd) {
        return ymd != null ? ymd.replace("-", "") : null;
    }

    @PostMapping("/designated-projects/{id}/notices")
    public String createNotice(@PathVariable Long id, @RequestParam String subject, @RequestParam String content) {
        designatedProjectClient.createNotice(id, subject, content);
        return "redirect:/designated-projects/" + id;
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/update")
    public String updateNotice(@PathVariable Long id, @PathVariable Long noticeId,
                                @RequestParam String subject, @RequestParam String content) {
        designatedProjectClient.updateNotice(noticeId, subject, content);
        return "redirect:/designated-projects/" + id;
    }

    @PostMapping("/designated-projects/{id}/notices/{noticeId}/delete")
    public String deleteNotice(@PathVariable Long id, @PathVariable Long noticeId) {
        designatedProjectClient.deleteNotice(noticeId);
        return "redirect:/designated-projects/" + id;
    }

    @GetMapping("/designated-projects/analysis")
    public String analysis(@RequestParam(required = false) String year, Model model) {
        model.addAttribute("years", designatedProjectClient.analysisYears());
        model.addAttribute("year", year);
        model.addAttribute("locgovRows", designatedProjectClient.analysisByLocgov(year));
        model.addAttribute("monthRows", designatedProjectClient.analysisByMonth(year));
        java.util.Map<String, String> locgovNames = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            locgovNames.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm());
        }
        model.addAttribute("locgovNames", locgovNames);
        return "designated/analysis";
    }

    @GetMapping("/designated-projects/departments")
    public String departments(Model model) {
        model.addAttribute("departments", designatedProjectClient.departments(null));
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "designated/departments";
    }

    @PostMapping("/designated-projects/departments")
    public String createDepartment(@RequestParam String deptNm, @RequestParam String locgovCode,
                                    HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            designatedProjectClient.createDepartment(deptNm, locgovCode, manager.getUserId());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", designatedProjectClient.departments(null));
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "designated/departments";
        }
        return "redirect:/designated-projects/departments";
    }

    @PostMapping("/designated-projects/departments/{deptId}/toggle")
    public String toggleDepartment(@PathVariable Long deptId, HttpSession session) {
        Manager manager = manager(session);
        designatedProjectClient.toggleDepartment(deptId, manager.getUserId());
        return "redirect:/designated-projects/departments";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean canSelfApprove(Manager manager) {
        return "ROLE_ADMIN".equals(manager.getAuthority());
    }

    private static java.util.Map<String, String> statusLabels() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put(STATUS_PENDING, "대기");
        map.put(STATUS_OPEN, "진행중");
        map.put(STATUS_CLOSED, "종료");
        return map;
    }

    private java.util.Map<String, String> provinces() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}
