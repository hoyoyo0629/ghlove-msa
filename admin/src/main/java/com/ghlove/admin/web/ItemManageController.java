package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.service.CategoryTeamAdminClient;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ItemManageClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.OffgiveClient;
import com.ghlove.admin.web.support.AdminRole;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 관리자 > 답례품관리 > 답례품관리 (AS-IS opmanager/item/list, 메뉴 16301) - 답례품 목록·일괄처리(품절/공개)·삭제신청·변경사항 로그와
 * 지자체 대표 답례품(최대 4) / 오프라인 대표 답례품(최대 2) 관리.
 *
 * <p>AS-IS {@code ItemManagerController}의 목록 계열을 그대로 옮겼다. 목록은 GET/POST 둘 다 조회하고(검색 폼이 POST), 검색일자는 비어 있으면
 * 오늘로 채운다. 지자체 담당자(소속 지자체가 있는 관리자)는 자기 지자체 판매자의 답례품만 본다. 대표 답례품 영역은 지자체 정·부담당자
 * (ROLE_ADMIN_5/6)이거나, 그 밖의 관리자가 시/군/구를 골랐을 때만 나오고 저장은 5/6만 한다(서버도 같은 검사를 한다).
 *
 * <p>이 화면이 여는 다른 화면 - 등록(item/create)·수정(item/edit/{코드})은 아직 AS-IS로 옮기지 않아 기존 TO-BE 폼으로 넘긴다.
 */
@Controller
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemManageController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_RPRS = 4;
    private static final int MAX_OFF_RPRS = 2;

    private final ItemManageClient client;
    private final CategoryTeamAdminClient teamClient;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;
    private final OffgiveClient offgiveClient;
    private final ManagerRepository managerRepository;

    /** gift 서비스 주소 - 상품 이미지(/uploads/..)가 거기서 서빙된다. */
    @Value("${ghlove.gift-service.base-url}")
    private String giftBaseUrl;

    /** 대민 사이트 주소 - 목록의 미리보기 아이콘이 여는 상품 상세(AS-IS saleson.url.frontend + 상세 경로). */
    @Value("${ghlove.storefront.base-url:http://localhost:5173}")
    private String storefrontUrl;

    // ---- 목록 ----

    @GetMapping({"", "/", "/list"})
    public String list(@ModelAttribute("itemParam") ItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        fillList(param, request, session, model);
        return "item/list";
    }

    /** AS-IS POST list - 검색 폼 제출. GET과 같은 조회다. */
    @PostMapping("/list")
    public String listPost(@ModelAttribute("itemParam") ItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        fillList(param, request, session, model);
        return "item/list";
    }

    private void fillList(ItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        param.applyDefaults();

        // AS-IS invokeItemList - 소속 지자체가 있는 관리자는 그 지자체로 한정(locgovService.getLocgovCodeByOpId).
        String locgov = has(manager.getLocgovCode()) ? manager.getLocgovCode() : null;
        ItemManageClient.Search search = param.toSearch(locgov);

        int count = client.count(search);
        Pagination pagination = Pagination.of(count, param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        List<ItemManageClient.ItemRow> rows = client.list(search, pagination.getStartRow(), pagination.getItemsPerPage());

        Map<String, String> locgovNames = locgovNames();
        List<ItemView> views = new ArrayList<>();
        for (ItemManageClient.ItemRow r : rows) {
            views.add(new ItemView(r, locgovNames.get(r.sellerLocgovCode()), imageSrc(r.thumbnail())));
        }

        String role = AdminRole.of(manager);
        boolean locgovCharger = isLocgovCharger(manager);
        model.addAttribute("list", views);
        model.addAttribute("pagination", pagination);
        model.addAttribute("totalCount", count);
        model.addAttribute("adminRole", role);
        model.addAttribute("locgovCharger", locgovCharger);
        // AS-IS 날짜 빠른선택 중 "전체" 버튼은 ROLE_ADMIN_1 / ROLE_ADMIN_5에게만 보인다.
        model.addAttribute("canSelectAllDates", "ROLE_ADMIN_1".equals(manager.getAuthority()) || "ROLE_ADMIN_5".equals(manager.getAuthority()));
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("storefrontUrl", storefrontUrl);
        model.addAttribute("categoryTeamGroupList", teamClient.list());

        // 대표 답례품 영역 - 5/6은 소속 지자체, 그 밖에는 화면에서 고른 시/군/구
        String rprsLocgov = locgovCharger ? manager.getLocgovCode() : (has(param.getShLocgovCode()) ? param.getShLocgovCode() : null);
        boolean showRelation = locgovCharger || has(param.getShLocgovCode());
        model.addAttribute("showRelation", showRelation);
        if (showRelation && has(rprsLocgov)) {
            List<ItemManageClient.RprsRow> rprs = client.rprs(rprsLocgov);
            List<ItemManageClient.OffRprsRow> offRprs = client.offRprs(rprsLocgov);
            model.addAttribute("lclgvRprsGdsList", rprs.stream().map(r -> new RprsView(r, imageSrc(r.thumbnail()))).toList());
            model.addAttribute("lclgvOffRprsGdsList", offRprs.stream().map(r -> new OffRprsView(r, imageSrc(r.thumbnail()))).toList());
            // AS-IS lclgvOffRprsGdsListJSON - 화면 시작 때 "수정 필요 내역" 안내를 만드는 데 쓴다
            model.addAttribute("lclgvOffRprsGdsListJson", offRprs.stream().map(ItemManageController::offRprsJson).toList());
        } else {
            model.addAttribute("lclgvRprsGdsList", List.of());
            model.addAttribute("lclgvOffRprsGdsList", List.of());
            model.addAttribute("lclgvOffRprsGdsListJson", List.of());
        }
    }

    /** 시도 코드로 사용중인 시군구 목록 - AS-IS item/options-by-locgovCode(give-state 서비스 재사용)와 같은 [{LOCGOV_CODE, LOCGOV_NM}]. */
    @PostMapping("/options-by-locgovCode")
    @ResponseBody
    public List<Map<String, Object>> optionsByLocgovCode(@RequestParam(name = "code", defaultValue = "0") String code) {
        return offgiveClient.locgovOptions(code);
    }

    // ---- 일괄 처리 / 삭제 (Common.updateListData가 #listForm을 serialize해서 id=..&id=.. 로 보낸다) ----

    @PostMapping("/list/update-display/{flag}")
    @ResponseBody
    public Map<String, Object> updateDisplay(@PathVariable String flag, @RequestParam(value = "id", required = false) List<Long> id,
                                             HttpSession session) {
        if (id != null && !id.isEmpty()) {
            client.updateDisplay(id, flag, manager(session).getUserId());
        }
        return success();
    }

    @PostMapping("/list/update-label/{flag}")
    @ResponseBody
    public Map<String, Object> updateLabel(@PathVariable String flag, @RequestParam(value = "id", required = false) List<Long> id,
                                           HttpSession session) {
        if (id != null && !id.isEmpty()) {
            client.updateLabel(id, flag, manager(session).getUserId());
        }
        return success();
    }

    /** AS-IS list/delete - 바로 지우지 않고 삭제신청(40)으로 바꾼다. */
    @PostMapping("/list/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(value = "id", required = false) List<Long> id, HttpSession session) {
        if (id != null && !id.isEmpty()) {
            client.delete(id, manager(session).getUserId());
        }
        return success();
    }

    // ---- 변경사항 팝업 ----

    @GetMapping("/popup/log/{itemId}")
    public String logPopup(@PathVariable long itemId, @RequestParam(defaultValue = "1") int page, HttpServletRequest request, Model model) {
        int count = client.logCount(itemId);
        Pagination pagination = Pagination.of(count, page).withLinkFrom(request);
        List<ItemManageClient.LogRow> rows = client.logList(itemId, pagination.getStartRow(), pagination.getItemsPerPage());
        Map<Long, Manager> managers = managerRepository.findAllById(rows.stream().map(ItemManageClient.LogRow::createdManagerId)
                        .filter(java.util.Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Manager::getUserId, Function.identity(), (a, b) -> a));
        List<LogView> views = new ArrayList<>();
        for (ItemManageClient.LogRow r : rows) {
            Manager m = r.createdManagerId() == null ? null : managers.get(r.createdManagerId());
            views.add(new LogView(r, m == null ? null : m.getLoginId(), m == null ? null : m.getUserName()));
        }
        model.addAttribute("list", views);
        model.addAttribute("pagination", pagination);
        model.addAttribute("itemId", itemId);
        return "item/log-popup";
    }

    // ---- 대표 답례품 저장 ----

    /** AS-IS saveLclgvRprsGds - 5/6만. 최대 4개. 비어 있으면 전체 삭제. 화면 JS가 itemIds를 배열로 보낸다(itemIds[]). */
    @PostMapping("/saveLclgvRprsGds")
    @ResponseBody
    public Map<String, Object> saveRprs(HttpServletRequest request, HttpSession session) {
        Manager manager = manager(session);
        if (!isLocgovCharger(manager)) {
            return failure("권한이 없습니다.");
        }
        List<Long> itemIds = itemIds(request);
        if (itemIds.size() > MAX_RPRS) {
            return failure("쵀대 4개까지 등록 가능합니다.");
        }
        client.saveRprs(manager.getLocgovCode(), itemIds, manager.getUserId());
        return success();
    }

    /** AS-IS saveLclgvOffRprsGds - 5/6만. 최대 2개. 본문은 [{itemId, option}] JSON. */
    @PostMapping("/saveLclgvOffRprsGds")
    @ResponseBody
    public Map<String, Object> saveOffRprs(@RequestBody(required = false) List<Map<String, String>> optionList, HttpSession session) {
        Manager manager = manager(session);
        if (!isLocgovCharger(manager)) {
            return failure("권한이 없습니다.");
        }
        List<Map<String, String>> list = optionList == null ? List.of() : optionList;
        if (list.size() > MAX_OFF_RPRS) {
            return failure("쵀대 2개까지 등록 가능합니다.");
        }
        List<ItemManageClient.OffRprsInput> inputs = new ArrayList<>();
        for (Map<String, String> m : list) {
            inputs.add(new ItemManageClient.OffRprsInput(Long.valueOf(m.get("itemId")), m.get("option")));
        }
        client.saveOffRprs(manager.getLocgovCode(), inputs, manager.getUserId());
        return success();
    }

    // ---- 오프라인 대표 답례품 옵션 조회 (5/6만 - 아니면 AS-IS도 예외) ----

    @PostMapping("/getOptionList")
    @ResponseBody
    public List<Map<String, Object>> optionList(@RequestParam(required = false) Long itemId, HttpSession session) {
        requireLocgovCharger(session);
        if (itemId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "옵션 조회에 문제가 발생하였습니다.");
        }
        return client.optionList(itemId);
    }

    @PostMapping("/getComboOptionList")
    @ResponseBody
    public List<Map<String, String>> comboOptionList(@RequestBody(required = false) Map<String, Object> optionMap, HttpSession session) {
        requireLocgovCharger(session);
        if (optionMap == null || optionMap.get("itemId") == null) {
            return List.of();
        }
        return client.comboOptionList(optionMap);
    }

    @PostMapping(value = "/getSelectOption", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String selectOption(@RequestParam Long itemId, HttpSession session) {
        requireLocgovCharger(session);
        return client.selectedOption(itemId);
    }

    // ---- 아직 AS-IS로 옮기지 않은 화면으로의 연결 ----

    /** AS-IS item/create (등록 폼) - 아직 옮기지 않아 기존 TO-BE 등록 폼으로 넘긴다. */
    @GetMapping("/create")
    public String create() {
        return "redirect:/admin/gift-items/new";
    }

    /** AS-IS item/edit/{상품코드} (수정 폼) - 상품코드(G2+9자리)에서 번호를 뽑아 기존 TO-BE 수정 폼으로 넘긴다. */
    @GetMapping("/edit/{itemUserCode}")
    public String edit(@PathVariable String itemUserCode) {
        String digits = itemUserCode.startsWith("G2") ? itemUserCode.substring(2) : itemUserCode;
        if (!digits.matches("\\d{1,18}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "답례품을 찾을 수 없습니다.");
        }
        return "redirect:/admin/gift-items/" + Long.parseLong(digits) + "/edit";
    }

    // ---- 도우미 ----

    private static Map<String, Object> success() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", true);
        return m;
    }

    private static Map<String, Object> failure(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", false);
        m.put("errorMessage", message);
        return m;
    }

    /** jQuery가 배열을 itemIds[]=1&itemIds[]=2 로 보내고, 폼이면 itemIds=1 로 보낸다 - 둘 다 받는다. */
    private static List<Long> itemIds(HttpServletRequest request) {
        List<Long> ids = new ArrayList<>();
        for (String name : new String[]{"itemIds[]", "itemIds"}) {
            String[] values = request.getParameterValues(name);
            if (values != null) {
                for (String v : values) {
                    if (has(v)) {
                        ids.add(Long.valueOf(v.trim()));
                    }
                }
            }
        }
        return ids;
    }

    private static boolean isLocgovCharger(Manager manager) {
        return "ROLE_ADMIN_5".equals(manager.getAuthority()) || "ROLE_ADMIN_6".equals(manager.getAuthority());
    }

    private void requireLocgovCharger(HttpSession session) {
        if (!isLocgovCharger(manager(session))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "권한이 없습니다.");
        }
    }

    /** 상품 이미지 - 없으면 AS-IS noimage.path(/content/images/common/no-image-gray.gif). */
    private String imageSrc(String thumbnail) {
        return has(thumbnail) ? giftBaseUrl + "/uploads/" + thumbnail : "/content/images/common/no-image-gray.gif";
    }

    private List<Map<String, String>> wdrOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("id", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    /** 지자체코드 → "시도 시군구"(AS-IS CONCAT(UPPER_LOCGOV_NM, ' ', LOCGOV_NM)). */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            names.put(l.locgovCode(), (l.upperLocgovNm() == null ? "" : l.upperLocgovNm() + " ") + l.locgovNm());
        }
        return names;
    }

    /** JS가 읽는 AS-IS Item JSON 중 이 화면이 쓰는 키만. */
    private static Map<String, Object> offRprsJson(ItemManageClient.OffRprsRow r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("itemId", r.itemId());
        m.put("itemName", r.itemName());
        m.put("dataStatusCode", r.dataStatusCode());
        m.put("itemOptionFlag", r.itemOptionFlag());
        m.put("changedOption", r.changedOption());
        m.put("optionStockQuantity", r.optionStockQuantity());
        m.put("stockQuantity", r.stockQuantity());
        return m;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    // ---- 화면용 보조 타입 ----

    public record ItemView(ItemManageClient.ItemRow item, String locgovNm, String imageSrc) {
    }

    public record RprsView(ItemManageClient.RprsRow item, String imageSrc) {
    }

    public record OffRprsView(ItemManageClient.OffRprsRow item, String imageSrc) {
    }

    public record LogView(ItemManageClient.LogRow log, String managerLoginId, String userName) {
    }

    /** AS-IS ItemParam 중 목록 검색 폼이 보내는 것. */
    @Getter
    @Setter
    public static class ItemParam {
        private String categoryId = "";
        private String shWdr = "";
        private String shLocgovCode = "";
        private String where = "";
        private String query = "";
        private String srchDateType = "";
        private String searchStartDate;
        private String searchEndDate;
        private int categoryGroupId;
        private String categoryClass1 = "";
        private String categoryClass2 = "";
        private String categoryClass3 = "";
        private String categoryClass4 = "";
        private String saleStatus = "";
        private String displayFlag = "";
        private long sellerId;
        private String orderBy = "";
        private String sort = "DESC";
        private int itemsPerPage = 10;
        private int page = 1;

        void applyDefaults() {
            String today = LocalDate.now().format(DAY);
            if (searchStartDate == null || searchStartDate.isBlank()) {
                searchStartDate = today;
            }
            if (searchEndDate == null || searchEndDate.isBlank()) {
                searchEndDate = today;
            }
            // 정렬조건은 CategoryId가 있는 경우에만 허용.
            if ("ORDERING".equals(orderBy) && (categoryId == null || categoryId.isBlank())) {
                orderBy = "";
                sort = "DESC";
            }
        }

        /** AS-IS ItemParam.getCategoryClass - 선택한 가장 깊은 차수. */
        String categoryClass() {
            for (String c : new String[]{categoryClass4, categoryClass3, categoryClass2, categoryClass1}) {
                if (c != null && !c.isBlank()) {
                    return c;
                }
            }
            return "";
        }

        ItemManageClient.Search toSearch(String locgov) {
            Integer categoryIdNumber = null;
            try {
                categoryIdNumber = categoryId == null || categoryId.isBlank() ? null : Integer.valueOf(categoryId.trim());
            } catch (NumberFormatException ignored) {
                // 숫자가 아니면 정렬 조건에서만 쓰이므로 없는 것으로 본다
            }
            return new ItemManageClient.Search(locgov, shLocgovCode, where, query, srchDateType, searchStartDate, searchEndDate,
                    categoryGroupId, categoryClass(), categoryIdNumber, saleStatus, displayFlag, sellerId, orderBy, sort);
        }
    }
}
