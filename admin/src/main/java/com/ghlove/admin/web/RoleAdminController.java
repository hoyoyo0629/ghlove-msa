package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.RoleAdminService;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 권한 관리 - AS-IS saleson.shop.usergroup.UserGroupController(/opmanager/user-group) 재현.
 * 이름과 달리 회원그룹이 아니라 관리자 권한관리이고, 메뉴 두 개가 이 컨트롤러를 공유한다:
 * <ul>
 *   <li>1405 사용자 권한그룹 관리 = {@code /opmanager/user-group/list} - 그룹 목록(그룹명·설명·인원·생성일자),
 *       생성·수정은 {@code Common.popup} 800x330 팝업</li>
 *   <li>1404 사용자 권한 관리 = {@code /opmanager/user-group/role/list} - 왼쪽 그룹 목록에서 고른 그룹의
 *       메뉴권한(OP_MENU_RIGHT)을 3단 체크박스로 편집</li>
 * </ul>
 * AS-IS 목록의 삭제 버튼은 JSP에서 주석처리되어 있어 노출되지 않는다 - 비활성 상태까지 맞춘다.
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleAdminService roleAdminService;
    private final CommonMessageService commonMessageService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;

    /** AS-IS list(/user-group/list) - 1405. */
    @GetMapping
    public String list(@RequestParam(required = false) String errorMessage,
                       @RequestParam(defaultValue = "1") int page,
                       HttpServletRequest request, Model model) {
        List<RoleAdminService.AdminRoleRow> all = roleAdminService.adminRoleRows();

        Pagination pagination = Pagination.of(all.size(), page).withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("totalCount", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("errorMessage", errorMessage);
        return "role-admin/list";
    }

    /** AS-IS groupInsert(GET create) - 팝업(layout=base). */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("role", null);
        model.addAttribute("type", "등록");
        return "role-admin/form";
    }

    /**
     * AS-IS groupInsertAction(POST create). 주의: AS-IS 등록 폼은 authority를
     * {@code <input type="hidden" id="authority" />}로만 두고 name이 없어 서버로 보내지 않는다
     * - 즉 AS-IS 생성 버튼은 권한코드 없이 INSERT를 시도하는 상태다. 여기서 권한코드를 임의로
     * 지어내면 AS-IS에 없는 동작이 되므로, 값이 없으면 저장하지 않고 사유를 알려준다.
     */
    @PostMapping("/create")
    public String create(@RequestParam(required = false) String authority,
                         @RequestParam String roleName,
                         @RequestParam(required = false) String roleDesc, Model model) {
        try {
            roleAdminService.create(authority, roleName, roleDesc, null);
            model.addAttribute("message", commonMessageService.get("M00632"));  // 등록되었습니다
            return "common/popup-result";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("role", null);
            model.addAttribute("type", "등록");
            return "role-admin/form";
        }
    }

    /** AS-IS groupUpdate(GET edit?authority=) - 팝업. */
    @GetMapping("/edit")
    public String editForm(@RequestParam("authority") String authority, Model model) {
        model.addAttribute("role", roleAdminService.get(authority));
        model.addAttribute("type", "수정");
        return "role-admin/form";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam("authority") String authority, @RequestParam String roleName,
                       @RequestParam(required = false) String roleDesc, Model model) {
        try {
            roleAdminService.update(authority, roleName, roleDesc, null);
            model.addAttribute("message", commonMessageService.get("M01673"));  // 수정되었습니다
            return "common/popup-result";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("role", roleAdminService.get(authority));
            model.addAttribute("type", "수정");
            return "role-admin/form";
        }
    }

    /** AS-IS deleteListData(POST delete) - 목록 삭제버튼이 주석처리돼 호출부가 없지만 엔드포인트는 유지. */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("authority") String authority) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            roleAdminService.delete(authority);
            result.put("isSuccess", true);
        } catch (ManagerException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    /**
     * AS-IS groupRoleList(/user-group/role/list) - 1404. authority가 없으면 AS-IS JS가
     * 목록 첫 행을 골라 다시 조회하므로(fnGroupSearch의 target==null 분기) 여기서 같은 선택을 해준다.
     * 왼쪽 그룹 목록은 AS-IS가 {@code Pagination.getInstance(totalCount, Integer.MAX_VALUE)}로
     * 전체를 뿌리므로 페이징하지 않는다.
     */
    @GetMapping("/matrix")
    public String matrix(@RequestParam(value = "userAuthority", required = false) String userAuthority,
                         Model model) {
        String authority = (userAuthority == null || userAuthority.isBlank())
                ? roleAdminService.firstAdminAuthority() : userAuthority;
        if (authority == null) {
            model.addAttribute("list", List.of());
            model.addAttribute("authority", "");
            return "role-admin/matrix";
        }
        var role = roleAdminService.get(authority);
        model.addAttribute("list", roleAdminService.matrixRoleRows());
        model.addAttribute("matrix", roleAdminService.matrixFor(authority));
        model.addAttribute("role", role);
        model.addAttribute("authority", authority);
        model.addAttribute("roleName", role.getRoleName());
        model.addAttribute("roleDesc", role.getRoleDesc());
        return "role-admin/matrix";
    }

    /**
     * AS-IS groupRoleInsertAction(POST role/list) - 메뉴권한 저장 후 같은 화면으로 돌아간다.
     * AS-IS 화면이 authority 외에 roleName·roleDesc도 hidden으로 보내고 서버가 OP_ROLE까지
     * 갱신하므로({@code RoleServiceImpl.updateRole}) 같이 받는다.
     */
    @PostMapping("/matrix")
    public String saveMatrix(@RequestParam("authority") String authority,
                             @RequestParam(required = false) String roleName,
                             @RequestParam(required = false) String roleDesc,
                             @RequestParam(required = false) List<Integer> menuIds) {
        roleAdminService.saveMatrix(authority, roleName, roleDesc, menuIds);
        // AS-IS는 ViewUtils.redirect("/opmanager/user-group/role/list", M00406, "")로 돌아간다 -
        // userAuthority를 붙이지 않으므로 저장 후 선택이 첫 행(답례품관리자)으로 되돌아간다.
        // 문구는 flash scope로 넘어간다(3인자 redirect = setJavascript + 2인자 redirect, 바이트코드 확인).
        return flashRedirect.to("/admin/roles/matrix", commonMessageService.get("M00406"));  // 저장되었습니다.
    }

    /** 이전 TO-BE 경로(/admin/roles/{authority}/matrix)로 들어온 링크를 AS-IS 경로로 넘긴다. */
    @GetMapping("/{authority}/matrix")
    public String matrixLegacy(@PathVariable String authority) {
        return "redirect:/admin/roles/matrix?userAuthority=" + authority;
    }
}
