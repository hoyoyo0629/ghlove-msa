package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonMessageAdminService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.web.support.MessageParam;
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
 * 메세지 관리 (메뉴 1402) - AS-IS saleson.shop.message.MessageManagerController(/opmanager/message) 재현.
 *
 * OP_COMMON_MESSAGE를 ID 하나당 한국어/일본어 한 행으로 보여주고 등록·수정·선택삭제한다.
 * TO-BE에는 이 화면이 아예 없었다(op_menu 1402의 menu_url도 비어 있어 메뉴를 눌러도 갈 곳이
 * 없었다 - {@code migration-admin-menu-1402-message.sql}로 /message를 넣었다).
 *
 * 등록은 ID 입력 여부로 갈린다(AS-IS 동일): 비워 두면 M+5자리 자동채번, 적어 넣으면 그 ID로
 * 넣는다(메뉴 문구 MENU_xxxx를 직접 넣는 용도).
 */
@Controller
@RequiredArgsConstructor
public class CommonMessageAdminController {

    private final CommonMessageAdminService commonMessageAdminService;
    private final CommonMessageService commonMessageService;

    /** AS-IS GET/POST /opmanager/message/list. */
    @RequestMapping(value = { "/message", "/message/list" },
            method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("messageParam") MessageParam messageParam,
                       HttpServletRequest request, Model model) {
        if (messageParam.getItemsPerPage() <= 0) {
            messageParam.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
        List<CommonMessageAdminService.MessageRow> all = commonMessageAdminService.getMessageList(messageParam);
        Pagination pagination = Pagination.of(all.size(), messageParam.getPage(), messageParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("messageList", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("messageCount", all.size());
        model.addAttribute("pagination", pagination);
        return "message/list";
    }

    /** AS-IS GET /opmanager/message/create. */
    @GetMapping("/message/create")
    public String createForm(Model model) {
        model.addAttribute("message", new CommonMessageAdminService.MessageRow(null, null, null));
        return "message/form";
    }

    /**
     * AS-IS POST /opmanager/message/create - id 파라미터가 비어 있으면 자동채번(insertMessage2),
     * 있으면 그 ID로 등록(insertMessage1)이다.
     */
    @PostMapping("/message/create")
    public String create(@RequestParam(required = false) String id,
                         @RequestParam(required = false) String kMessage,
                         @RequestParam(required = false) String jMessage,
                         RedirectAttributes redirectAttributes) {
        if (id == null || id.isBlank()) {
            commonMessageAdminService.insertMessage2(kMessage, jMessage);
        } else {
            commonMessageAdminService.insertMessage1(id.trim(), kMessage, jMessage);
        }
        // AS-IS ViewUtils.redirect(..., MessageUtils.getMessage("M00632")) = "등록되었습니다"
        redirectAttributes.addFlashAttribute("resultMessage", commonMessageService.get("M00632"));
        return "redirect:/message/list";
    }

    /** AS-IS GET /opmanager/message/edit/{id}. */
    @GetMapping("/message/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("message", commonMessageAdminService.getMessageById(id));
        model.addAttribute("id", id);
        return "message/form";
    }

    /** AS-IS POST /opmanager/message/edit/{id}. */
    @PostMapping("/message/edit/{id}")
    public String edit(@PathVariable String id,
                       @RequestParam(required = false) String kMessage,
                       @RequestParam(required = false) String jMessage,
                       RedirectAttributes redirectAttributes) {
        commonMessageAdminService.updateMessage(id, kMessage, jMessage);
        // AS-IS M01673 = "수정되었습니다."
        redirectAttributes.addFlashAttribute("resultMessage", commonMessageService.get("M01673"));
        return "redirect:/message/list";
    }

    /** AS-IS POST /opmanager/message/delete - Common.updateListData가 #listForm을 serialize해 보낸다. */
    @PostMapping("/message/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(name = "id", required = false) List<String> ids) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            commonMessageAdminService.deleteMessageData(ids);
            result.put("isSuccess", true);
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }
}
