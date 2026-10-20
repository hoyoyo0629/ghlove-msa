package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.SeasonalFoodClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 관리자 > 답례품관리 > 제철식품관 관리 > 제철식품관 관리(AS-IS opmanager/seasonal-food/list, 메뉴 16751) - 월별 제철 키워드 목록·수정·등록.
 *
 * <p>AS-IS {@code SeasonalFoodManagerController}를 그대로 옮겼다. 목록은 1~12월을 모두 보이고, [수정]·[등록] 버튼은 시스템/행안부(ROLE_ADMIN_1~4)에게만
 * 보인다(JS는 ROLE_ADMIN_5/6만 빼고 모두 받는다 - AS-IS 그대로). 수정/등록은 팝업(700x310)이 부모 창에 postMessage로 저장을 맡기고, 부모가 ajax로
 * {@code saveSeasonalFood}를 부르면 서버가 저장한 뒤 목록 전체를 돌려줘 표를 다시 그린다.
 */
@Controller
@RequestMapping("/seasonal-food")
@RequiredArgsConstructor
public class SeasonalFoodController {

    /** AS-IS removeInvalidChar - 한글·영문·숫자·쉼표 말고는 전부 지운다(공백도 지워진다). */
    private static final Pattern INVALID_CHAR = Pattern.compile("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9,]");

    private final SeasonalFoodClient seasonalFoodClient;

    /** AS-IS GET list - 제철식품관 관리 페이지. */
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        Manager manager = manager(session);
        model.addAttribute("seasonFoodData", seasonalFoodClient.list());
        // AS-IS op:hasRole - 표의 [관리] 칸·[등록] 버튼은 ROLE_ADMIN_1~4, 저장용 JS는 ROLE_ADMIN_5/6이 아닐 때
        model.addAttribute("canManage", MenuService.UNRESTRICTED_ROLES.contains(manager.getAuthority()));
        model.addAttribute("canScript", !MenuService.isLocgovScoped(manager));
        return "seasonal-food/list";
    }

    /** AS-IS GET popup-month - 키워드 수정 팝업(layout=base). */
    @GetMapping("/popup-month")
    public String popupMonth() {
        return "seasonal-food/popup-month";
    }

    /** AS-IS GET popup-keyword - 키워드 등록 팝업(layout=base). */
    @GetMapping("/popup-keyword")
    public String popupKeyword() {
        return "seasonal-food/popup-keyword";
    }

    public record SaveRequest(List<SeasonalFoodClient.MonthKeywords> seasonalFoods) {
    }

    /** AS-IS POST saveSeasonalFood - 응답은 JsonViewUtils 모양({isSuccess, data}). 저장에 실패하면 "저장에 실패했습니다.". */
    @PostMapping("/saveSeasonalFood")
    @ResponseBody
    public Map<String, Object> save(@RequestBody SaveRequest request, HttpSession session) {
        Manager manager = manager(session);
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<SeasonalFoodClient.MonthKeywords> months = new ArrayList<>();
            for (SeasonalFoodClient.MonthKeywords m : request.seasonalFoods()) {
                months.add(new SeasonalFoodClient.MonthKeywords(m.seasonalFoodMonth(), removeInvalidChar(m.seasonalFoodKeyword())));
            }
            result.put("isSuccess", true);
            result.put("data", seasonalFoodClient.save(months, manager.getUserId() == null ? 0L : manager.getUserId()));
        } catch (RuntimeException e) {
            result.clear();
            result.put("isSuccess", false);
            result.put("errorMessage", "저장에 실패했습니다.");
        }
        return result;
    }

    /** AS-IS removeInvalidChar - 허용 문자 말고는 지우고, 쉼표로 나눠 빈 값을 버린 뒤 다시 쉼표로 잇는다. */
    private static String removeInvalidChar(String keyword) {
        if (keyword == null) {
            return "";
        }
        List<String> valid = new ArrayList<>();
        for (String s : INVALID_CHAR.matcher(keyword).replaceAll("").split(",")) {
            if (s != null && !s.isEmpty()) {
                valid.add(s);
            }
        }
        return String.join(",", valid);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
