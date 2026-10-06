package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Policy;
import com.ghlove.admin.repository.PolicyRepository;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.PolicyParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 약관관리 - AS-IS saleson.shop.config.ConfigManagerController의 policy/* 재현
 * ({@code /opmanager/config/policy/list|detail/{id}|create|delete/{id}|delete-list}).
 * 예전 TO-BE 목록은 검색·화면출력·페이징·선택삭제가 없고 컬럼도 달랐다.
 */
@Controller
@RequiredArgsConstructor
public class PolicyController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PolicyRepository policyRepository;

    /** AS-IS policyList - itemsPerPage가 0이면 10으로 채우는 분기까지 동일. */
    @GetMapping("/policy")
    public String list(@ModelAttribute("policyParam") PolicyParam policyParam,
                       HttpServletRequest request, Model model) {
        if (policyParam.getItemsPerPage() == 0) {
            policyParam.setItemsPerPage(10);
        }
        String keyword = policyParam.queryCondition();
        List<Policy> found = policyRepository.search(
                "TITLE".equals(policyParam.getWhere()) ? keyword : null,
                "CONTENT".equals(policyParam.getWhere()) ? keyword : null,
                policyParam.policyTypeCondition(),
                policyParam.exhibitionStatusCondition());

        Pagination pagination = Pagination.of(found.size(), policyParam.getPage(), policyParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("policyList", found.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("totalCount", found.size());
        model.addAttribute("pagination", pagination);
        return "policy/list";
    }

    /** AS-IS createGetPolicy(policy/create) - 등록은 같은 form.jsp를 빈 객체로 띄운다. */
    @GetMapping("/policy/create")
    public String createForm(Model model) {
        model.addAttribute("policy", new Policy());
        return "policy/form";
    }

    /** AS-IS updateGetPolicy(policy/detail/{policyId}) - 목록의 제목 링크가 가는 곳(= 수정화면). */
    @GetMapping("/policy/detail/{policyId}")
    public String detail(@PathVariable Integer policyId, Model model) {
        model.addAttribute("policy", policyRepository.findById(policyId).orElseThrow());
        return "policy/form";
    }

    @PostMapping("/policy/create")
    public String create(@ModelAttribute Policy form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        form.setPolicyId(null);
        form.setCreatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        form.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        if (manager != null) {
            form.setCreatedUserId(manager.getUserId() != null ? manager.getUserId().intValue() : null);
            form.setUpdatedLoginId(manager.getLoginId());
        }
        policyRepository.save(form);
        return "redirect:/policy";
    }

    /** AS-IS updatePostPolicy(policy/detail/{policyId}) - 저장 후 목록으로 돌아간다. */
    @PostMapping("/policy/detail/{policyId}")
    public String update(@PathVariable Integer policyId, @ModelAttribute Policy form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        Policy policy = policyRepository.findById(policyId).orElseThrow();
        policy.setPolicyType(form.getPolicyType());
        policy.setTitle(form.getTitle());
        policy.setContent(form.getContent());
        policy.setExhibitionStatus(form.getExhibitionStatus());
        policy.setExhibitionStartDate(form.getExhibitionStartDate());
        policy.setExhibitionEndDate(form.getExhibitionEndDate());
        policy.setUpdatedDate(DATE_FORMAT.format(LocalDateTime.now()));
        if (manager != null) {
            policy.setUpdatedLoginId(manager.getLoginId());
        }
        policyRepository.save(policy);
        return "redirect:/policy";
    }

    /** AS-IS deletePolicy - 상세/목록의 삭제가 GET으로 들어온다(location.replace). */
    @RequestMapping(value = "/policy/delete/{policyId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String delete(@PathVariable Integer policyId) {
        policyRepository.deleteById(policyId);
        return "redirect:/policy";
    }

    /** AS-IS deleteListData(delete-list) - 목록 선택삭제(op.common.js Common.updateListData). */
    @PostMapping("/policy/delete-list")
    @ResponseBody
    public Map<String, Object> deleteList(@RequestParam(value = "id", required = false) List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            policyRepository.deleteAllById(ids);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }
}
