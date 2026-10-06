package com.ghlove.admin.web;

import com.ghlove.admin.repository.MenuTreeRepository;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuAdminService;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 메뉴관리 - AS-IS saleson.shop.menu.MenuManagerController(/opmanager/menu) 재현.
 * 한 화면에서 관리자(OP_MENU)/답례품관리자(OP_MENU_SELLER) 트리를 전환해 보고, 등록·수정은
 * {@code Common.popup} 800x600 팝업이며 팝업 안에서 1차·2차 메뉴를 고르면 ajax로 메뉴ID를
 * 채번해 받는다. 관리자 메뉴는 메뉴명이 OP_COMMON_MESSAGE의 {@code MENU_<id>} 코드로도 저장된다.
 *
 * 예전 TO-BE는 계층/경로/메뉴타입·코드·순서·상태 편집·채번·답례품관리자 트리·문구 동기화가
 * 모두 없었고 팝업도 아니었다.
 */
@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class MenuAdminController {

    private final MenuAdminService menuAdminService;
    private final CommonMessageService commonMessageService;

    /**
     * AS-IS menuList - where(OPMANAGER/SELLER) 기본값은 OPMANAGER이고, 메뉴상태 코드값을
     * 사용(M00083)/사용안함(M00089) 문구로 바꿔 내려준다.
     *
     * AS-IS는 목록에 {@code LIMIT 10}(framework paginationFooter)을 걸면서도 JSP에 페이저
     * 태그를 넣지 않아 11번째 메뉴부터는 화면에서 볼 수 없다 - 컨트롤러가 Pagination을 만들어
     * 넘기는 걸 보면 페이징이 의도였으므로, 여기서는 페이저를 함께 렌더해 전체를 볼 수 있게 했다.
     */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@RequestParam(value = "where", required = false) String where,
                       @RequestParam(defaultValue = "1") int page,
                       HttpServletRequest request, Model model) {
        String menuGubun = MenuAdminService.GUBUN_SELLER.equals(where)
                ? MenuAdminService.GUBUN_SELLER : MenuAdminService.GUBUN_MANAGER;

        List<MenuTreeRepository.MenuRow> all = menuAdminService.tree(menuGubun);
        Pagination pagination = Pagination.of(all.size(), page).withLinkFrom(request);

        List<MenuRowView> rows = all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .map(r -> new MenuRowView(r, statusLabel(r.statusCode())))
                .toList();

        model.addAttribute("where", menuGubun);
        model.addAttribute("menuCount", all.size());
        model.addAttribute("menuList", rows);
        model.addAttribute("pagination", pagination);
        return "menu-admin/list";
    }

    /** 목록 한 줄 - AS-IS는 Menu.statusCode를 문구로 덮어써 내려주므로 따로 담아 전달한다. */
    public record MenuRowView(MenuTreeRepository.MenuRow row, String statusLabel) {
    }

    private String statusLabel(String statusCode) {
        if ("1".equals(statusCode)) {
            return commonMessageService.get("M00083");   // 사용
        }
        if ("2".equals(statusCode)) {
            return commonMessageService.get("M00089");   // 사용안함
        }
        return statusCode;
    }

    /** AS-IS menuInsert(GET create) - 팝업. 1차 메뉴 목록과 1레벨 기준 채번된 메뉴ID를 미리 채운다. */
    @GetMapping("/create")
    public String createForm(@RequestParam(value = "menuGubun", required = false) String menuGubun, Model model) {
        String gubun = MenuAdminService.GUBUN_SELLER.equals(menuGubun)
                ? MenuAdminService.GUBUN_SELLER : MenuAdminService.GUBUN_MANAGER;
        model.addAttribute("menuGubun", gubun);
        model.addAttribute("firstMenuList", menuAdminService.firstMenus(gubun));
        model.addAttribute("menu", new MenuAdminService.MenuForm(
                menuAdminService.nextMenuId(gubun, 1, 0), 0, 1, null, null, null, null, "Y", "1"));
        model.addAttribute("isNew", true);
        return "menu-admin/form";
    }

    @PostMapping("/create")
    public String create(@RequestParam("menuGubun") String menuGubun,
                         @RequestParam("menuId") Integer menuId,
                         @RequestParam(value = "menuParentId", required = false) Integer menuParentId,
                         @RequestParam(value = "menuType", required = false) Integer menuType,
                         @RequestParam("menuName") String menuName,
                         @RequestParam(value = "menuCode", required = false) String menuCode,
                         @RequestParam(value = "menuUrl", required = false) String menuUrl,
                         @RequestParam(value = "menuSeq", required = false) Integer menuSeq,
                         @RequestParam(value = "displayFlag", required = false) String displayFlag,
                         @RequestParam(value = "statusCode", required = false) String statusCode,
                         Model model) {
        MenuAdminService.MenuForm form = new MenuAdminService.MenuForm(menuId, menuParentId, menuType,
                menuName, menuCode, menuUrl, menuSeq, displayFlag, statusCode);
        try {
            menuAdminService.createMenu(menuGubun, form);
            model.addAttribute("message", commonMessageService.get("M00632"));  // 등록되었습니다
            return "common/popup-result";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("menuGubun", menuGubun);
            model.addAttribute("firstMenuList", menuAdminService.firstMenus(menuGubun));
            model.addAttribute("menu", form);
            model.addAttribute("isNew", true);
            return "menu-admin/form";
        }
    }

    /** AS-IS menuUpdate(GET edit?menuGubun=&menuId=&menuPath=) - 팝업. 1·2차 셀렉트는 JS가 숨긴다. */
    @GetMapping("/edit")
    public String editForm(@RequestParam("menuGubun") String menuGubun,
                           @RequestParam("menuId") Integer menuId,
                           @RequestParam(value = "menuPath", required = false) String menuPath,
                           Model model) {
        model.addAttribute("menuGubun", menuGubun);
        model.addAttribute("firstMenuList", menuAdminService.firstMenus(menuGubun));
        model.addAttribute("menu", menuAdminService.formOf(menuGubun, menuId));
        model.addAttribute("menuPath", menuPath);
        model.addAttribute("isNew", false);
        return "menu-admin/form";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam("menuGubun") String menuGubun,
                       @RequestParam("menuId") Integer menuId,
                       @RequestParam(value = "menuParentId", required = false) Integer menuParentId,
                       @RequestParam(value = "menuType", required = false) Integer menuType,
                       @RequestParam("menuName") String menuName,
                       @RequestParam(value = "menuCode", required = false) String menuCode,
                       @RequestParam(value = "menuUrl", required = false) String menuUrl,
                       @RequestParam(value = "menuSeq", required = false) Integer menuSeq,
                       @RequestParam(value = "displayFlag", required = false) String displayFlag,
                       @RequestParam(value = "statusCode", required = false) String statusCode,
                       Model model) {
        MenuAdminService.MenuForm form = new MenuAdminService.MenuForm(menuId, menuParentId, menuType,
                menuName, menuCode, menuUrl, menuSeq, displayFlag, statusCode);
        try {
            menuAdminService.updateMenu(menuGubun, form);
            model.addAttribute("message", commonMessageService.get("M01673"));  // 수정되었습니다
            return "common/popup-result";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("menuGubun", menuGubun);
            model.addAttribute("firstMenuList", menuAdminService.firstMenus(menuGubun));
            model.addAttribute("menu", form);
            model.addAttribute("isNew", false);
            return "menu-admin/form";
        }
    }

    /** AS-IS deleteMenu(POST delete) - ajax. 목록 JS가 하위메뉴 수를 먼저 보지만 서버도 막는다. */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("menuGubun") String menuGubun,
                                      @RequestParam("menuId") Integer menuId) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            menuAdminService.deleteMenu(menuGubun, menuId);
            result.put("isSuccess", true);
        } catch (ManagerException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    /** AS-IS secondMenuList - 1차 메뉴를 고르면 그 아래 2차 목록을 돌려준다. */
    @PostMapping("/secondMenuList")
    @ResponseBody
    public Map<String, Object> secondMenuList(@RequestParam("menuParentId") String menuParentId,
                                              @RequestParam("menuGubun") String menuGubun) {
        Integer parentId = menuParentId == null || menuParentId.isBlank() ? 0 : Integer.valueOf(menuParentId);
        List<Map<String, Object>> data = menuAdminService.childMenus(menuGubun, parentId).stream()
                .map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("menuId", r.menuId());
                    item.put("menuName", r.menuName());
                    return item;
                })
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", data);
        return result;
    }

    /** AS-IS getMenuId - 레벨·부모가 정해지면 다음 메뉴ID를 채번해 돌려준다. */
    @PostMapping("/menuId")
    @ResponseBody
    public Map<String, Object> menuId(@RequestParam("menuParentId") String menuParentId,
                                      @RequestParam("menuGubun") String menuGubun,
                                      @RequestParam("menuType") Integer menuType) {
        Integer parentId = menuParentId == null || menuParentId.isBlank() ? 0 : Integer.valueOf(menuParentId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", menuAdminService.nextMenuId(menuGubun, menuType, parentId));
        return result;
    }
}
