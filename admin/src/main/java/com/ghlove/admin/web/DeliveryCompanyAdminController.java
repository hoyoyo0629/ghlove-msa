package com.ghlove.admin.web;

import com.ghlove.admin.service.DeliveryReturnClient;
import com.ghlove.admin.service.DeliveryReturnClient.DeliveryCompanyDto;
import com.ghlove.admin.service.DeliveryReturnClient.DeliveryCompanyForm;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.web.support.DeliveryCompanyParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 배송업체 관리 - AS-IS saleson.shop.deliverycompany.DeliveryCompanyManagerController
 * (/opmanager/delivery-company) 재현. 표는 order 서비스의 {@code ord.op_delivery_company}이고
 * (컬럼은 AS-IS와 동일) admin은 {@link DeliveryReturnClient}로 cross-service CRUD 한다.
 *
 * 예전 TO-BE는 검색·화면출력·페이징·선택삭제가 없고 행별 삭제 버튼만 있었다. order 서비스 API가
 * 전체 목록을 주고 삭제는 id 단건이라, 검색·페이징은 admin에서, 선택삭제는 id별 반복 호출로 맞췄다
 * (order 쪽 API는 건드리지 않았다).
 */
@Controller
@RequestMapping("/admin/delivery-companies")
@RequiredArgsConstructor
public class DeliveryCompanyAdminController {

    private final DeliveryReturnClient client;

    /** AS-IS deliveryCompanyList / searchDeliveryCompanyList - GET·POST 동일 동작. */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("deliveryCompanyParam") DeliveryCompanyParam deliveryCompanyParam,
                       HttpServletRequest request, Model model) {
        if (deliveryCompanyParam.getItemsPerPage() == 0) {
            deliveryCompanyParam.setItemsPerPage(10);
        }
        List<DeliveryCompanyDto> all = client.deliveryCompanies().stream()
                .filter(c -> deliveryCompanyParam.matches(c.deliveryCompanyName(), c.telNumber()))
                .toList();

        Pagination pagination = Pagination.of(all.size(), deliveryCompanyParam.getPage(),
                deliveryCompanyParam.getItemsPerPage()).withLinkFrom(request);
        model.addAttribute("deliveryCompanyList", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("deliveryCompanyCount", all.size());
        model.addAttribute("pagination", pagination);
        return "delivery-company-admin/list";
    }

    /** AS-IS deliveryCompanyInsert(GET create) - kind='등록'은 저장 확인문구에 쓰인다. */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("deliveryCompany", null);
        model.addAttribute("kind", "등록");
        return "delivery-company-admin/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute DeliveryCompanyForm form, RedirectAttributes redirect) {
        try {
            client.createDeliveryCompany(form);
            redirect.addFlashAttribute("message", "택배사가 등록되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/delivery-companies";
    }

    /** AS-IS deliveryCompanyUpdate(GET edit/{id}) - 목록의 업체명 링크가 가는 곳. */
    @GetMapping("/edit/{deliveryCompanyId}")
    public String editForm(@PathVariable Integer deliveryCompanyId, Model model) {
        model.addAttribute("deliveryCompany", client.deliveryCompany(deliveryCompanyId));
        model.addAttribute("kind", "수정");
        return "delivery-company-admin/form";
    }

    @PostMapping("/edit/{deliveryCompanyId}")
    public String update(@PathVariable Integer deliveryCompanyId,
                         @ModelAttribute DeliveryCompanyForm form, RedirectAttributes redirect) {
        try {
            client.updateDeliveryCompany(deliveryCompanyId, form);
            redirect.addFlashAttribute("message", "택배사가 수정되었습니다.");
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/delivery-companies";
    }

    /**
     * AS-IS deleteCompanyListData(POST delete) - 목록 선택삭제.
     * order 서비스 API가 단건 삭제만 제공하므로 id별로 호출한다.
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(value = "id", required = false) List<Integer> ids) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            result.put("isSuccess", true);
            return result;
        }
        try {
            ids.forEach(client::deleteDeliveryCompany);
            result.put("isSuccess", true);
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }
}
