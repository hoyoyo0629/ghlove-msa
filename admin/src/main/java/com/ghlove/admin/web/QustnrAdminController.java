package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.QestnarRepository;
import com.ghlove.admin.service.QestnarAdminService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.QustnrSearchParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 설문관리 - AS-IS saleson.shop.qustnr.QustnrManagerController(/opmanager/qustnr) 재현.
 * 목록(검색·화면출력·페이징·선택삭제) / 등록·수정(설문+문항+선택지를 JSON 한 덩어리로 POST) /
 * 결과(문항별 선택지 응답수).
 *
 * 데이터 모델은 AS-IS 4개 표({@code G_QESTNAR}, {@code G_QUSTNR_QESITM}, {@code G_QUSTNR_IEM},
 * {@code G_QUSTNR_RSPNS_RESULT})를 쓴다. 예전 TO-BE는 문항을 자유서술형으로 단순화한
 * {@code op_qustnr*} 표를 따로 만들어 썼는데, 그러면 객관식 선택지·주관식 구분·연계질문(parentSn)·
 * 설문대상(srvyTrgt)이 전부 사라져 AS-IS 화면을 만들 수 없다. 두 모델 다 0행이어서 그대로 옮겼다.
 */
@Controller
@RequestMapping("/admin/surveys")
@RequiredArgsConstructor
public class QustnrAdminController {

    private final QestnarAdminService qestnarAdminService;

    /** AS-IS list / listPost - GET·POST 동일 동작. */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("searchParam") QustnrSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        if (searchParam.getItemsPerPage() == 0) {
            searchParam.setItemsPerPage(10);
        }
        List<QestnarRepository.QestnarRow> all = qestnarAdminService.list(searchParam.getSearchTxt());

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "survey-admin/list";
    }

    /** AS-IS create(GET) - 빈 등록화면. 화면 JS가 질문 1개를 기본으로 추가한다. */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("qestnar", null);
        model.addAttribute("qesitmList", List.of());
        return "survey-admin/form";
    }

    /** AS-IS edit(GET {qustnrSn}) - 설문 상세(= 수정화면). 목록의 설문명 링크가 가는 곳. */
    @GetMapping("/{qustnrSn}")
    public String editForm(@PathVariable long qustnrSn, Model model) {
        model.addAttribute("qestnar", qestnarAdminService.get(qustnrSn));
        model.addAttribute("qesitmList", qestnarAdminService.qesitmViews(qustnrSn));
        return "survey-admin/form";
    }

    /** AS-IS create(POST) - JSON 본문. 응답은 {isSuccess} 모양 그대로(화면 JS가 그걸 본다). */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> create(@RequestBody QestnarAdminService.QestnarForm form, HttpSession session) {
        return run(() -> qestnarAdminService.create(form, managerId(session)));
    }

    /** AS-IS edit(POST {qustnrSn}) - JSON 본문. */
    @PostMapping("/{qustnrSn}")
    @ResponseBody
    public Map<String, Object> edit(@PathVariable long qustnrSn,
                                    @RequestBody QestnarAdminService.QestnarForm form, HttpSession session) {
        return run(() -> {
            qestnarAdminService.update(qustnrSn, form, managerId(session));
            return qustnrSn;
        });
    }

    /** AS-IS deleteQustrn(POST /delete) - 목록 선택삭제(op.common.js Common.updateListData). */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(value = "id", required = false) List<Long> ids) {
        return run(() -> {
            qestnarAdminService.delete(ids);
            return null;
        });
    }

    /** AS-IS result(GET {qustnrSn}/result) - 참여 인원 + 문항별 선택지 응답수. */
    @GetMapping("/{qustnrSn}/result")
    public String result(@PathVariable long qustnrSn, Model model) {
        model.addAttribute("total", qestnarAdminService.responseCount(qustnrSn));
        model.addAttribute("qestnar", qestnarAdminService.get(qustnrSn));
        model.addAttribute("qesitmList", qestnarAdminService.qesitmViews(qustnrSn));
        return "survey-admin/result";
    }

    private Long managerId(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        return manager != null ? manager.getUserId() : null;
    }

    /** AS-IS JsonViewUtils.success()/fail() 응답 모양 - 화면 JS가 isSuccess/errorMessage만 본다. */
    private Map<String, Object> run(java.util.function.Supplier<Object> action) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Object data = action.get();
            result.put("isSuccess", true);
            if (data != null) {
                result.put("data", data);
            }
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }
}
