package com.ghlove.admin.web;

import com.ghlove.admin.domain.Faq;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.FaqAdminService;
import com.ghlove.admin.service.FaqException;
import com.ghlove.admin.service.FaqType;
import com.ghlove.admin.web.support.Pagination;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * FAQ 관리 (메뉴 5104) - AS-IS {@code FaqManagerController}({@code /opmanager/faq}) 이식.
 * 여기서 등록한 FAQ가 <b>공개 고객센터 FAQ 페이지에 그대로 나간다</b>({@code /faqs}·{@code /api/faqs},
 * AS-IS {@code /faq/list.html}·{@code /api/faq}) - 같은 {@code OP_FAQ}를 읽는다.
 *
 * <p><b>★ 메뉴 5104가 엉뚱한 화면을 가리키고 있었다</b>: {@code op_menu}의 5104 'FAQ'가
 * {@code /community/faq-bbs}(커뮤니티 <b>담당자FAQ</b> 11405)를 가리켜, 고객센터 FAQ를 누르면
 * 다른 메뉴의 화면이 열렸다. 이번에 {@code /faq-admin}으로 바로잡았다
 * (database/ddl/migration-admin-faq-5104.sql).
 *
 * <p><b>★ 공개 FAQ가 다른 표를 보고 있었다</b>: TO-BE 초기 시드가 FAQ 63건을
 * {@code op_community_locgovfaq}(AS-IS에서 중지된 11403 지자체FAQ의 표)에 자체 코드로 넣고
 * 공개화면을 거기에 물려 두었다. AS-IS 정본은 {@code OP_FAQ} + {@link FaqType} enum이라
 * 그쪽으로 되돌렸다 - 질문유형 라벨 11건이 AS-IS enum과 글자까지 같아 코드만 1:1로 바꿨다.
 *
 * <p><b>AS-IS 동작 그대로</b>: 목록 <b>진입(GET)은 조회하지 않고 빈 목록</b>이고 검색(POST)에서만
 * 조회한다. 정렬은 {@code id DESC} 고정, 삭제는 행 삭제, 저장은 등록·수정 모두 {@code use_yn='Y'}다.
 * 수정 저장 후에는 목록이 아니라 <b>수정화면으로 되돌아온다</b>(AS-IS가 그렇게 리다이렉트한다).
 */
@Controller
@RequiredArgsConstructor
public class FaqAdminController {

    private final FaqAdminService faqAdminService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;

    /** AS-IS GET faq/list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping("/faq-admin")
    public String list(@ModelAttribute("searchParam") FaqSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "faq-admin/list";
    }

    /** AS-IS POST faq/list - 검색. */
    @PostMapping("/faq-admin")
    public String searchList(@ModelAttribute("searchParam") FaqSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "faq-admin/list";
    }

    private void fillList(FaqSearchParam searchParam, HttpServletRequest request, Model model,
                          boolean doSearch) {
        searchParam.applyDefaults();

        List<FaqAdminService.FaqRow> all = doSearch
                ? faqAdminService.search(searchParam.getWhere(), searchParam.getQuery(),
                        searchParam.getFaqType())
                : List.of();

        // AS-IS는 JPA Pageable(size=10/20/50, page) - 전체 건수와 현재 페이지만 쓰면 모양이 같다
        int size = searchParam.getSize();
        int totalCount = all.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int currentPage = Math.max(1, Math.min(searchParam.getPage(), Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * size, totalCount);
        int to = Math.min(from + size, totalCount);

        model.addAttribute("faqList", all.subList(from, to));
        model.addAttribute("faqCount", totalCount);
        // 순번은 AS-IS op:numbering(pageContent, index)와 같이 "전체건수 - (앞 페이지 건수 + 행번호)"
        model.addAttribute("firstNumber", totalCount - from);
        model.addAttribute("pagination", Pagination.of(totalCount, currentPage, size).withLinkFrom(request));
        model.addAttribute("faqTypes", FaqType.options());
    }

    /** AS-IS GET faq/create. */
    @GetMapping("/faq-admin/create")
    public String createForm(Model model) {
        model.addAttribute("faq", new Faq());
        model.addAttribute("faqTypes", FaqType.options());
        return "faq-admin/form";
    }

    /** AS-IS POST faq/create - 끝나면 목록으로 가며 "등록되었습니다."를 띄운다. */
    @PostMapping("/faq-admin/create")
    public String create(@RequestParam String faqType, @RequestParam String title,
                         @RequestParam String content, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            faqAdminService.create(faqType, title, content, manager.getUserId());
        } catch (FaqException e) {
            return flashRedirect.to("/faq-admin/create", e.getMessage());
        }
        return flashRedirect.to("/faq-admin", "등록되었습니다.");
    }

    /** AS-IS GET faq/edit/{id}. */
    @GetMapping("/faq-admin/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("faq", faqAdminService.find(id));
        } catch (FaqException e) {
            return flashRedirect.to("/faq-admin", e.getMessage());
        }
        model.addAttribute("faqTypes", FaqType.options());
        return "faq-admin/form";
    }

    /**
     * AS-IS POST faq/edit/{id} - 저장 후 <b>목록이 아니라 수정화면으로</b> 되돌아온다.
     * AS-IS는 {@code url} 파라미터가 있으면 리다이렉트에 붙여 주는데 그 값을 보내는 화면이 없다 -
     * 모양만 유지한다.
     */
    @PostMapping("/faq-admin/edit/{id}")
    public String edit(@PathVariable Long id, @RequestParam String faqType, @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(required = false) String url, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            faqAdminService.update(id, faqType, title, content, manager.getUserId());
        } catch (FaqException e) {
            return flashRedirect.to("/faq-admin/edit/" + id, e.getMessage());
        }
        String redirect = flashRedirect.to("/faq-admin/edit/" + id, "수정되었습니다.");
        return url == null ? redirect : redirect + "&url=" + encode(url);
    }

    /**
     * AS-IS POST faq/delete - 목록에서 체크한 FAQ를 <b>행 삭제</b>한다(ajax, JSON).
     * 실패 문구도 AS-IS 그대로 "삭제에 실패했습니다."
     */
    @PostMapping("/faq-admin/delete")
    @ResponseBody
    public Map<String, Object> deleteList(@RequestParam(value = "id", required = false) List<Long> id) {
        try {
            faqAdminService.deleteList(id);
        } catch (FaqException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "삭제에 실패했습니다.");
        }
        return Map.of("isSuccess", true);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** AS-IS FaqDto의 검색 파라미터 부분(Param: page/size/where/query) + faqType. */
    public static class FaqSearchParam {
        private String where;
        private String query;
        private String faqType;
        private int page;
        private int size;

        /** AS-IS @PageableDefault와 화면 select 기본값. */
        public void applyDefaults() {
            if (page <= 0) {
                page = 1;
            }
            if (size <= 0) {
                size = 10;
            }
            // AS-IS 화면이 hidden으로 고정해 보내는 값
            if (where == null || where.isBlank()) {
                where = "title";
            }
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getFaqType() {
            return faqType;
        }

        public void setFaqType(String faqType) {
            this.faqType = faqType;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
