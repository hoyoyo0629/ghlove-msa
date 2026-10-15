package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CategoriesAdminClient;
import com.ghlove.admin.service.CategoryTeamAdminClient;
import com.ghlove.admin.web.support.FlashRedirect;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리관리 (AS-IS opmanager/categories, 메뉴 16101) - OP_CATEGORY 1~4차 트리(jsTree 드래그앤드롭 이동).
 *
 * <p>AS-IS {@code CategoriesManagerController}를 그대로 옮겼다. 화면은 목록 한 장이고 오른쪽 폼({@code create}/{@code edit})은
 * 목록이 ajax로 HTML 조각을 받아 끼워 넣는다. 등록·수정은 목록 안의 {@code #categoryForm}을 submit해 리다이렉트로 돌아온다.
 *
 * <p>★ 권한: 조회는 누구나 하지만 등록·수정·삭제는 AS-IS가 화면(JS)에서 {@code adminRole != "SYS"}면 막는다
 * ({@code LocgovService.getLoginUserAdminRoleCheck}: ROLE_ADMIN_5/6=LOC, 7/8=OFF, 11=WCM, 그 밖은 SYS).
 * 서버 쪽 검사는 AS-IS에도 없다.
 *
 * <p>AS-IS의 {@code /change}, {@code /change-ordering-category1}은 화면(JS)이 부르지 않는 죽은 엔드포인트라 옮기지 않았다.
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriesManagerController {

    private final CategoriesAdminClient categoriesClient;
    private final CategoryTeamAdminClient teamClient;
    private final FlashRedirect flashRedirect;

    /** AS-IS {@code saleson.url.frontend} - 수정폼의 URL 미리보기 아이콘이 여는 프론트 주소(운영 AS-IS는 view.type=api). */
    @Value("${ghlove.storefront.url:http://localhost:5173}")
    private String frontendUrl;

    // ---- 화면 ----

    @GetMapping({"", "/", "/list"})
    public String list(@RequestParam(required = false, defaultValue = "") String categoryGroupId,
                       HttpSession session, Model model) {
        model.addAttribute("categoryTeamGroupList", teamClient.list());
        model.addAttribute("categoryGroupId", categoryGroupId);
        model.addAttribute("adminRole", adminRole(session));
        return "categories/list";
    }

    /** 오른쪽 폼 조각(2차 카테고리 추가 버튼이 연다) - AS-IS는 layout=blank. */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("categoryTeamGroupList", teamClient.list());
        return "categories/create";
    }

    @PostMapping("/create")
    public String createAction(CategoryParam param) {
        categoriesClient.create(param.toForm());
        String redirectUri = "/categories/list";
        if (param.getCategoryGroupId() != null && param.getCategoryGroupId() > 0) {
            redirectUri = redirectUri + "?categoryGroupId=" + param.getCategoryGroupId();
        }
        return flashRedirect.to(redirectUri, "등록되었습니다.");
    }

    @PostMapping("/low-create/{categoryCode}")
    public String lowCreateAction(@PathVariable String categoryCode, CategoryParam param) {
        categoriesClient.createSub(param.toForm());
        return flashRedirect.to("/categories/list", "등록되었습니다.");
    }

    @GetMapping("/edit/{categoryCode}")
    public String edit(@PathVariable String categoryCode, Model model) {
        CategoriesAdminClient.Category category = categoriesClient.byCode(categoryCode);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
        }
        model.addAttribute("categoryTeamGroupList", teamClient.list());
        model.addAttribute("categories", category);
        model.addAttribute("level", Integer.parseInt(category.categoryLevel()));
        model.addAttribute("frontendUrl", frontendUrl);
        return "categories/edit";
    }

    @PostMapping("/edit")
    public String editAction(@RequestParam(value = "cateGroupId", defaultValue = "") String cateGroupId,
                             CategoryParam param) {
        categoriesClient.update(param.toForm());
        return flashRedirect.to("/categories/list?categoryGroupId=" + cateGroupId, "수정되었습니다.");
    }

    // ---- ajax ----

    @ResponseBody
    @GetMapping("/edit-seo/{categoryCode}")
    public CategoriesAdminClient.Category editSeo(@PathVariable String categoryCode) {
        return categoriesClient.byCode(categoryCode);
    }

    @ResponseBody
    @GetMapping("/tree-list")
    public List<Map<String, Object>> treeList(@RequestParam(required = false) String categoryGroupId) {
        return categoriesClient.treeLevel1(categoryGroupId);
    }

    @ResponseBody
    @GetMapping("/tree-list/{nodeId}")
    public List<Map<String, Object>> treeLists(@PathVariable String nodeId) {
        return categoriesClient.treeChildren(nodeId);
    }

    @ResponseBody
    @PostMapping("/move")
    public Map<String, Object> move(@RequestParam(required = false) String categoryGroupId,
                                    @RequestParam(required = false) String parentCode,
                                    @RequestParam(required = false) String parentLevel,
                                    @RequestParam(required = false) String parentGroupId,
                                    @RequestParam(required = false) String currentCode,
                                    @RequestParam(required = false) String currentLevel,
                                    @RequestParam(required = false) String previousCode,
                                    @RequestParam(required = false) String previousLevel,
                                    @RequestParam(required = false) String previousOrdering) {
        try {
            categoriesClient.move(new CategoriesAdminClient.Move(categoryGroupId, parentCode, parentLevel,
                    parentGroupId, currentCode, currentLevel, previousCode, previousLevel, previousOrdering));
        } catch (RuntimeException e) {
            return failure("실패했습니다.");
        }
        return success();
    }

    @ResponseBody
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestParam String categoryCode, @RequestParam String categoryLevel) {
        categoriesClient.delete(categoryCode, categoryLevel);
        return success();
    }

    @ResponseBody
    @PostMapping("/code-check")
    public Map<String, Object> codeCheck(@RequestParam(required = false) String categoryUrl,
                                         @RequestParam(required = false) String categoryCode) {
        if (categoriesClient.count(categoryUrl, categoryCode) > 0) {
            return failure("코드 또는 URL 존재 합니다.");
        }
        return success();
    }

    // ---- helpers ----

    /** AS-IS LocgovServiceImpl.getLoginUserAdminRoleCheck - 지자체(5,6)=LOC, 오프라인(7,8)=OFF, 복지센터(11)=WCM, 그 밖은 SYS. */
    private static String adminRole(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String auth = manager != null ? manager.getAuthority() : null;
        if (auth == null) {
            return "SYS";
        }
        return switch (auth) {
            case "ROLE_ADMIN_5", "ROLE_ADMIN_6" -> "LOC";
            case "ROLE_ADMIN_7", "ROLE_ADMIN_8" -> "OFF";
            case "ROLE_ADMIN_11" -> "WCM";
            default -> "SYS";
        };
    }

    private static Map<String, Object> success() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isSuccess", true);
        return r;
    }

    private static Map<String, Object> failure(String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isSuccess", false);
        r.put("errorMessage", message);
        return r;
    }

    /** 폼 바인딩용 - 중첩 {@code categoriesSeo.*}를 받으려고 레코드가 아닌 세터 클래스로 둔다. */
    @Getter
    @Setter
    public static class CategoryParam {
        private Integer categoryId;
        private String categoryCode;
        private Integer categoryGroupId;
        private String categoryUrl;
        private String categoryName;
        private String categoryLevel;
        private String categoryFlag;
        private String accessType;
        private String categoryClass1;
        private String categoryClass2;
        private String categoryClass3;
        private String categoryClass4;
        private SeoParam categoriesSeo = new SeoParam();

        CategoriesAdminClient.Form toForm() {
            return new CategoriesAdminClient.Form(categoryId, categoryCode, categoryGroupId, categoryUrl,
                    categoryName, categoryLevel, categoryFlag, accessType, categoryClass1, categoryClass2,
                    categoryClass3, categoryClass4,
                    new CategoriesAdminClient.Seo(categoriesSeo.title, categoriesSeo.keywords,
                            categoriesSeo.description, categoriesSeo.headerContents1, categoriesSeo.headerContents2,
                            categoriesSeo.headerContents3, categoriesSeo.themawordTitle,
                            categoriesSeo.themawordDescription));
        }
    }

    @Getter
    @Setter
    public static class SeoParam {
        private String title;
        private String keywords;
        private String description;
        private String headerContents1;
        private String headerContents2;
        private String headerContents3;
        private String themawordTitle;
        private String themawordDescription;
    }
}
