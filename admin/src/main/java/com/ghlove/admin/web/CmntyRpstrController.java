package com.ghlove.admin.web;

import com.ghlove.admin.domain.CmntyRpstr;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntyRpstrRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** 지자체 자료실 (AS-IS opmanager/community/databoard). */
@Controller
@RequestMapping("/community/databoard")
@RequiredArgsConstructor
public class CmntyRpstrController {

    private final CmntyRpstrRepository cmntyRpstrRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", cmntyRpstrRepository.findAllByOrderByRpstrIdDesc());
        return "community/databoard/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("item", new CmntyRpstr());
        return "community/databoard/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("item", cmntyRpstrRepository.findById(id).orElseThrow());
        return "community/databoard/form";
    }

    @PostMapping
    public String create(CmntyRpstr form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        form.setRpstrId(null);
        form.setUseYn("Y");
        form.setInqCnt(0L);
        form.setFrstCrtId(manager.getUserId());
        form.setFrstCrtDt(LocalDateTime.now());
        cmntyRpstrRepository.save(form);
        return "redirect:/community/databoard";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, CmntyRpstr form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        CmntyRpstr item = cmntyRpstrRepository.findById(id).orElseThrow();
        item.setRpstrTtl(form.getRpstrTtl());
        item.setRpstrCn(form.getRpstrCn());
        item.setNoticeYn(form.getNoticeYn() != null ? "Y" : "N");
        item.setLastMdfcnId(manager.getUserId());
        item.setLastMdfcnDt(LocalDateTime.now());
        cmntyRpstrRepository.save(item);
        return "redirect:/community/databoard";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        cmntyRpstrRepository.deleteById(id);
        return "redirect:/community/databoard";
    }
}
