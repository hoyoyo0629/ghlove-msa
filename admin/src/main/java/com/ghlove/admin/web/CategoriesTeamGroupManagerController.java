package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CategoriesTeamGroupAdminClient;
import com.ghlove.admin.web.support.FlashRedirect;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 카테고리 그룹 관리 (AS-IS opmanager/categories-team-group, 메뉴 16102) - 대그룹(OP_CATEGORY_TEAM) 아래
 * 1차 카테고리 그룹(OP_CATEGORY_GROUP) 목록·등록·수정·삭제·정렬순서변경.
 *
 * <p>AS-IS {@code CategoriesTeamGroupManagerController}를 옮겼다. 등록·수정·순서변경은 AS-IS가 화면(JS)에서
 * {@code adminRole != 'SYS'}면 막는다(서버 검사는 AS-IS에도 없다).
 *
 * <p>옮기지 않은 것(AS-IS도 화면에서 닿는 길이 없다): 대그룹 등록·수정·삭제 화면({@code /team/**} - 목록의 대그룹 수정 버튼이
 * 주석처리돼 있고 상품 검색 팝업은 SalesOn 종속), "1차 카테고리로 변경" 팝업({@code /group/change-group-to-category} - 그룹 폼의
 * 버튼이 주석처리), {@code ordering-edit/{type}}(대응하는 입력칸이 없는 죽은 핸들러), GET으로 부를 수 없는 {@code /group/delete/{id}}.
 */
@Controller
@RequestMapping("/categories-team-group")
@RequiredArgsConstructor
public class CategoriesTeamGroupManagerController {

    private final CategoriesTeamGroupAdminClient client;
    private final FlashRedirect flashRedirect;

    // ---- 목록 ----

    @GetMapping({"", "/", "/list"})
    public String list(@RequestParam(required = false, defaultValue = "") String categoryType,
                       HttpSession session, Model model) {
        return listView(categoryType, session, model);
    }

    @PostMapping("/list")
    public String searchList(@RequestParam(required = false, defaultValue = "") String categoryType,
                             HttpSession session, Model model) {
        return listView(categoryType, session, model);
    }

    private String listView(String categoryType, HttpSession session, Model model) {
        if ("team".equals(categoryType)) {
            model.addAttribute("categoriesTeamGroupList", client.teamList());
        } else if ("group".equals(categoryType)) {
            model.addAttribute("categoriesTeamGroupList", client.groupList());
        } else {
            model.addAttribute("categoriesTeamGroupList", client.teamGroupList());
        }
        model.addAttribute("categoryType", categoryType);
        model.addAttribute("adminRole", adminRole(session));
        return "categories-team-group/list";
    }

    /** AS-IS change-ordering/{team|group} - 화면이 보낸 id[]/ordering[] 짝대로 순서를 저장한다. */
    @ResponseBody
    @PostMapping("/change-ordering/{type}")
    public Map<String, Object> changeOrdering(@PathVariable String type,
                                              @RequestParam(value = "id", required = false) String[] id,
                                              @RequestParam(value = "ordering", required = false) String[] ordering) {
        List<Integer> ids = toInts(id);
        List<Integer> orderings = toInts(ordering);
        if ("team".equals(type)) {
            client.teamOrdering(ids, orderings);
        } else {
            client.groupOrdering(ids, orderings);
        }
        return success();
    }

    // ---- 그룹 등록/수정/삭제 ----

    @GetMapping("/group/create")
    public String createForm(Model model) {
        model.addAttribute("categoriesTeamSelectBox", client.teamList());
        model.addAttribute("categoriesGroupInList", client.categoriesInGroups());
        model.addAttribute("categoriesGroup", new CategoriesTeamGroupAdminClient.GroupRow(0, null, null, null, "Y", null, null));
        model.addAttribute("categoriesList", List.of());
        return "categories-team-group/group-form";
    }

    @PostMapping("/group/create")
    public String createAction(GroupParam param) {
        client.createGroup(param.toForm(null));
        return flashRedirect.to("/categories-team-group/list", "등록되었습니다.");
    }

    @GetMapping("/group/edit/{categoryGroupId}")
    public String editForm(@PathVariable int categoryGroupId, Model model) {
        CategoriesTeamGroupAdminClient.GroupRow group = client.group(categoryGroupId);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.");
        }
        model.addAttribute("categoriesTeamSelectBox", client.teamList());
        model.addAttribute("categoriesGroupInList", client.categoriesInGroups());
        model.addAttribute("categoriesList", client.categoriesOf(categoryGroupId));
        model.addAttribute("categoriesGroup", group);
        return "categories-team-group/group-form";
    }

    @PostMapping("/group/edit/{categoryGroupId}")
    public String editAction(@PathVariable int categoryGroupId, GroupParam param) {
        client.updateGroup(param.toForm(categoryGroupId));
        return flashRedirect.to("/categories-team-group/list", "수정되었습니다.");
    }

    @ResponseBody
    @PostMapping("/group/delete")
    public Map<String, Object> deleteGroup(@RequestParam int categoryGroupId) {
        client.deleteGroup(categoryGroupId);
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

    private static List<Integer> toInts(String[] values) {
        List<Integer> list = new ArrayList<>();
        if (values != null) {
            Arrays.stream(values).forEach(v -> list.add(Integer.parseInt(v.trim())));
        }
        return list;
    }

    private static Map<String, Object> success() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("isSuccess", true);
        return r;
    }

    /** 그룹 폼 바인딩 - AS-IS {@code CategoriesGroup} + {@code CategoriesSearchParam.categoryCodes}. */
    @Getter
    @Setter
    public static class GroupParam {
        private int categoryTeamId;
        private String name;
        private String code;
        private String categoryGroupFlag = "Y";
        private String accessType;
        private String[] categoryCodes;

        CategoriesTeamGroupAdminClient.GroupForm toForm(Integer categoryGroupId) {
            return new CategoriesTeamGroupAdminClient.GroupForm(categoryGroupId, categoryTeamId, name, code,
                    categoryGroupFlag, accessType, categoryCodes == null ? null : Arrays.asList(categoryCodes));
        }
    }
}
