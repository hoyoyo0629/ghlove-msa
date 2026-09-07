package com.ghlove.admin.web;

import com.ghlove.admin.domain.LclgvHnrUserStngMng;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.OpHonorViewHist;
import com.ghlove.admin.repository.LclgvHnrUserStngMngRepository;
import com.ghlove.admin.repository.OpHonorViewHistRepository;
import com.ghlove.admin.service.LocgovClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 지자체 명예직원(명예시도민증) 관리 (AS-IS opmanager/lclgvHnrUser - LclgvHnrUserManagerController).
 * 2탭 구성: (1) 지자체별 명예사용자 선정기준 등록/수정/삭제, (2) 열람이력 조회 + CSV다운로드. */
@Controller
@RequestMapping("/admin/honor-users")
@RequiredArgsConstructor
public class LclgvHnrUserAdminController {

    private final LclgvHnrUserStngMngRepository hnrUserRepository;
    private final OpHonorViewHistRepository viewHistRepository;
    private final LocgovClient locgovClient;

    @GetMapping
    public String list(Model model) {
        Map<String, String> locgovNames = locgovNameMap();
        model.addAttribute("rows", hnrUserRepository.findAllByOrderByLclgvCd());
        model.addAttribute("locgovNames", locgovNames);
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        return "honor-user-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("row", new LclgvHnrUserStngMng());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("isNew", true);
        return "honor-user-admin/form";
    }

    @GetMapping("/{lclgvCd}/edit")
    public String editForm(@PathVariable String lclgvCd, Model model) {
        model.addAttribute("row", hnrUserRepository.findById(lclgvCd).orElseThrow());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("isNew", false);
        return "honor-user-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute LclgvHnrUserStngMng form, HttpSession session) {
        Manager manager = manager(session);
        form.setUseYn("Y");
        form.setLastRegDt(LocalDateTime.now());
        form.setLastRgtrId(manager.getUserId());
        hnrUserRepository.save(form);
        return "redirect:/admin/honor-users";
    }

    @PostMapping("/{lclgvCd}")
    public String update(@PathVariable String lclgvCd, @ModelAttribute LclgvHnrUserStngMng form, HttpSession session) {
        Manager manager = manager(session);
        LclgvHnrUserStngMng row = hnrUserRepository.findById(lclgvCd).orElseThrow();
        row.setGldGrdDntnAmt(form.getGldGrdDntnAmt());
        row.setSlvrGrdDntnAmt(form.getSlvrGrdDntnAmt());
        row.setBrnzGrdDntnAmt(form.getBrnzGrdDntnAmt());
        row.setHnrUserStngTtl(form.getHnrUserStngTtl());
        row.setHnrUserRwrd(form.getHnrUserRwrd());
        row.setHnrUserSlctnSeCd(form.getHnrUserSlctnSeCd());
        row.setUseYn(form.getUseYn() != null ? "Y" : "N");
        row.setLastRegDt(LocalDateTime.now());
        row.setLastRgtrId(manager.getUserId());
        hnrUserRepository.save(row);
        return "redirect:/admin/honor-users/" + lclgvCd + "/edit";
    }

    @PostMapping("/{lclgvCd}/delete")
    public String delete(@PathVariable String lclgvCd) {
        hnrUserRepository.deleteById(lclgvCd);
        return "redirect:/admin/honor-users";
    }

    @GetMapping("/view-history")
    public String viewHistory(Model model) {
        Map<String, String> locgovNames = locgovNameMap();
        model.addAttribute("histories", viewHistRepository.findAllByOrderByViewDtDesc());
        model.addAttribute("locgovNames", locgovNames);
        return "honor-user-admin/view-history";
    }

    @GetMapping("/view-history/export")
    public void exportViewHistory(HttpServletResponse response) throws IOException {
        Map<String, String> locgovNames = locgovNameMap();
        List<OpHonorViewHist> rows = viewHistRepository.findAllByOrderByViewDtDesc();

        StringBuilder csv = new StringBuilder("﻿");
        csv.append("열람일시,회원ID,지자체\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OpHonorViewHist h : rows) {
            csv.append(csvEscape(h.getViewDt() != null ? h.getViewDt().format(fmt) : "")).append(',')
                    .append(csvEscape(h.getUserId() != null ? String.valueOf(h.getUserId()) : "")).append(',')
                    .append(csvEscape(locgovNames.getOrDefault(h.getLclgvCd(), h.getLclgvCd()))).append('\n');
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"honor-view-history.csv\"; filename*=UTF-8''"
                        + java.net.URLEncoder.encode("명예직원_열람이력.csv", StandardCharsets.UTF_8));
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> locgovNameMap() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::locgovCode, LocgovClient.LocgovInfo::locgovNm,
                        (a, b) -> a));
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
