package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Policy;
import com.ghlove.admin.repository.PolicyRepository;
import com.ghlove.admin.service.CommonCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 약관관리 (AS-IS opmanager/config/policy). */
@Controller
@RequiredArgsConstructor
public class PolicyController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PolicyRepository policyRepository;
    private final CommonCodeService commonCodeService;

    @GetMapping("/policy")
    public String list(Model model) {
        model.addAttribute("policies", policyRepository.findAllByOrderByPolicyTypeAscPolicyIdDesc());
        model.addAttribute("typeLabels", commonCodeService.labelsOf("POLICY_TYPE"));
        return "policy/list";
    }

    @GetMapping("/policy/new")
    public String createForm(Model model) {
        model.addAttribute("policy", new Policy());
        model.addAttribute("typeLabels", commonCodeService.labelsOf("POLICY_TYPE"));
        return "policy/form";
    }

    @GetMapping("/policy/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("policy", policyRepository.findById(id).orElseThrow());
        model.addAttribute("typeLabels", commonCodeService.labelsOf("POLICY_TYPE"));
        return "policy/form";
    }

    @PostMapping("/policy")
    public String create(Policy form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        form.setPolicyId(null);
        form.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        form.setCreatedUserId(manager.getUserId().intValue());
        form.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        form.setUpdatedLoginId(manager.getLoginId());
        policyRepository.save(form);
        return "redirect:/policy";
    }

    @PostMapping("/policy/{id}")
    public String update(@PathVariable Integer id, Policy form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        Policy policy = policyRepository.findById(id).orElseThrow();
        policy.setPolicyType(form.getPolicyType());
        policy.setTitle(form.getTitle());
        policy.setContent(form.getContent());
        policy.setExhibitionStatus(form.getExhibitionStatus());
        policy.setExhibitionStartDate(form.getExhibitionStartDate());
        policy.setExhibitionEndDate(form.getExhibitionEndDate());
        policy.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        policy.setUpdatedLoginId(manager.getLoginId());
        policyRepository.save(policy);
        return "redirect:/policy";
    }

    @PostMapping("/policy/{id}/delete")
    public String delete(@PathVariable Integer id) {
        policyRepository.deleteById(id);
        return "redirect:/policy";
    }
}
