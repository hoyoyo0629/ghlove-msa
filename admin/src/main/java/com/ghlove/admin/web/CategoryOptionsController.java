package com.ghlove.admin.web;

import com.ghlove.admin.service.ItemManageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 카테고리 select 옵션 조각(AS-IS 프론트 {@code CategoriesController}의 {@code /categories/options-by-groupid}·{@code /categories/options}) -
 * 관리자 화면의 "그룹 → 1~4차 카테고리" 연동 select(op.shop.js ShopEventHandler.categorySelectboxChagneEvent)가 부르는 HTML {@code <option>} 목록이다.
 * AS-IS는 프론트 경로라 관리자 메뉴 권한과 무관하게 열려 있다(WebConfig의 인증 예외 목록에 같은 경로가 있다).
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryOptionsController {

    private final ItemManageClient client;

    /** 그룹의 1차 카테고리. categoryClass1은 다시 그렸을 때 선택해 둘 값. */
    @PostMapping(value = "/options-by-groupid", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String optionsByGroupId(@RequestParam(defaultValue = "0") int categoryGroupId,
                                   @RequestParam(required = false, defaultValue = "") String categoryClass1) {
        return html(client.categoryOptions(categoryGroupId), categoryClass1);
    }

    /** 부모 코드(categoryClass) 바로 아래 차수의 카테고리. */
    @PostMapping(value = "/options", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String options(@RequestParam(required = false, defaultValue = "") String categoryClass,
                          @RequestParam(required = false, defaultValue = "") String categoryClass1) {
        if (categoryClass.isEmpty()) {
            return "";
        }
        return html(client.categoryChildren(categoryClass), categoryClass1);
    }

    /** AS-IS options.jsp / options-by-groupid.jsp - value는 차수만큼 앞부분 코드(3자리×차수), 일치하면 selected, rel은 카테고리번호. */
    private static String html(List<ItemManageClient.CategoryOption> options, String selectedClass) {
        StringBuilder sb = new StringBuilder();
        for (ItemManageClient.CategoryOption o : options) {
            int level = parseLevel(o.categoryLevel());
            String code = o.categoryCode() == null ? "" : o.categoryCode();
            String value = code.substring(0, Math.min(code.length(), 3 * level));
            sb.append("<option value=\"").append(HtmlUtils.htmlEscape(value)).append('"')
                    .append(value.equals(selectedClass) ? " selected=\"selected\"" : "")
                    .append(" rel=\"").append(o.categoryId() == null ? "" : o.categoryId()).append("\">")
                    .append(HtmlUtils.htmlEscape(o.categoryName() == null ? "" : o.categoryName())).append("</option>\n");
        }
        return sb.toString();
    }

    private static int parseLevel(String level) {
        try {
            return Integer.parseInt(level == null ? "1" : level.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
