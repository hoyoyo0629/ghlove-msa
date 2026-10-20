package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.service.CategoryTeamAdminClient;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ItemApprovalSmsService;
import com.ghlove.admin.service.ItemManageClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.OffgiveClient;
import com.ghlove.admin.service.SellerManageClient;
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
 * <p>같은 컨트롤러가 답례품승인관리(AS-IS opmanager/item/seller/list, 메뉴 16351)도 맡는다 - 목록(/seller/list)·승인처리/등록보류(/list/update)·
 * 미리보기 팝업(/seller/preview). AS-IS도 두 화면이 ItemManagerController 한 곳이다.
 *
 * <p>이 화면이 여는 다른 화면 - 등록(item/create)·수정(item/edit/{코드}, item/seller/edit/{코드})은 아직 AS-IS로 옮기지 않아 기존 TO-BE 폼으로 넘긴다.
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
    private final ItemApprovalSmsService approvalSmsService;
    private final SellerManageClient sellerManageClient;

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

    // ---- 답례품승인관리 (AS-IS opmanager/item/seller/list, 메뉴 16351) ----

    /**
     * AS-IS listPending - GET 전용(검색 폼이 GET). 수정일 기본값은 오늘이고, 대상은 승인 대기 계열(20 등록대기·30 재등록신청·21 등록보류·
     * 40 삭제대기, 31·41은 목록에서 등록대기로 보인다)이며 지자체 담당자는 자기 지자체 판매자만 본다. 지자체 칸·검색은 시스템/행안부(SYS)에게만.
     */
    @GetMapping("/seller/list")
    public String sellerList(@ModelAttribute("itemParam") ItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        param.applyDefaults();

        String locgov = has(manager.getLocgovCode()) ? manager.getLocgovCode() : null;
        ItemManageClient.Search search = param.toSearch(locgov, ItemManageClient.PENDING_LIST);

        int count = client.count(search);
        Pagination pagination = Pagination.of(count, param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        List<ItemManageClient.ItemRow> rows = client.list(search, pagination.getStartRow(), pagination.getItemsPerPage());

        Map<String, String> locgovNames = locgovNames();
        List<ItemView> views = new ArrayList<>();
        for (ItemManageClient.ItemRow r : rows) {
            views.add(new ItemView(r, locgovNames.get(r.sellerLocgovCode()), imageSrc(r.thumbnail())));
        }
        model.addAttribute("list", views);
        model.addAttribute("pagination", pagination);
        model.addAttribute("totalCount", count);
        model.addAttribute("adminRole", AdminRole.of(manager));
        model.addAttribute("wdr", wdrOptions());
        // AS-IS 날짜 빠른선택 "전체"는 ROLE_ADMIN_CALL(콜센터 겸임 권한)에게만 - TO-BE 관리자에는 이 권한이 없어 보이는 사람이 없다.
        model.addAttribute("callRole", false);
        return "item/seller-list";
    }

    /**
     * AS-IS list/update - 승인처리(approval)·등록보류(reject). Common.updateListData가 #listForm(id, processType, approvalMessage)을 보낸다.
     * 처리 뒤 국민비서 알림을 보낸다(승인: 삭제신청 승인 제외, 보류: 사유 포함). 이 화면이 보내지 않는 구분(단가 수정·간편관리)은 사용하지 않는다.
     */
    @PostMapping("/list/update")
    @ResponseBody
    public Map<String, Object> updateList(@RequestParam(value = "id", required = false) List<Long> id,
                                          @RequestParam(value = "processType", defaultValue = "") String processType,
                                          @RequestParam(value = "approvalMessage", required = false) String approvalMessage,
                                          HttpSession session) {
        if (id != null && !id.isEmpty()) {
            long managerId = manager(session).getUserId();
            switch (processType) {
                case "approval" -> approvalSmsService.approved(client.approve(id, managerId));
                case "reject" -> approvalSmsService.rejected(client.reject(id, approvalMessage, managerId), approvalMessage);
                default -> {
                }
            }
        }
        return success();
    }

    // ---- 상품검색 팝업 (AS-IS opmanager/item/find-item - Shop.findItem이 연다. 이벤트 관리 "상품 추가"가 쓴다) ----

    /**
     * AS-IS findItem(GET/POST, layout=base) - 소속 지자체가 없는 관리자는 404(PageNotFoundException). 조건은 setFindItemModel 그대로:
     * 정상(1)·공개(Y) 상품, 사용(2) 판매자, 그 지자체 판매자 상품만. 판매자 select는 그 지자체 판매자 전부(상태 무관).
     */
    @GetMapping("/find-item")
    public String findItem(@ModelAttribute("itemParam") FindItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        if (!has(manager.getLocgovCode())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        param.setConditionType("FIND_ITEM_POPUP");
        ItemManageClient.Search search = new ItemManageClient.Search(manager.getLocgovCode(), null, param.getWhere(), param.getQuery(), null,
                null, null, param.getCategoryGroupId(), param.categoryClass(), null, null, "Y", param.getSellerId(), null, null,
                ItemManageClient.FIND_LIST, "1");
        int count = client.count(search);
        Pagination pagination = Pagination.of(count, param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        List<ItemView> views = new ArrayList<>();
        for (ItemManageClient.ItemRow r : client.list(search, pagination.getStartRow(), pagination.getItemsPerPage())) {
            views.add(new ItemView(r, null, imageSrc(r.thumbnail())));
        }
        model.addAttribute("list", views);
        model.addAttribute("pagination", pagination);
        model.addAttribute("categoryTeamGroupList", teamClient.list());
        model.addAttribute("sellerList", sellerManageClient.sellersByLocgov(manager.getLocgovCode()));
        return "item/find-item";
    }

    /** AS-IS findItemPost - GET과 같다(검색 폼이 POST). */
    @PostMapping("/find-item")
    public String findItemPost(@ModelAttribute("itemParam") FindItemParam param, HttpServletRequest request, HttpSession session, Model model) {
        return findItem(param, request, session, model);
    }

    /** AS-IS item/seller/preview(layout=blank) - 답례품 상세 미리보기 팝업. 지자체명은 AS-IS CASE(시도=시군구면 시도만). */
    @GetMapping("/seller/preview")
    public String sellerPreview(@RequestParam("itemUserCode") String itemUserCode, Model model) {
        ItemManageClient.Preview preview = client.preview(itemUserCode);
        if (preview == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "답례품정보가 없습니다.");
        }
        String locgovNm = "";
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            if (l.locgovCode().equals(preview.locgovCode())) {
                locgovNm = l.locgovCode().equals(l.upperLocgovCode()) ? l.upperLocgovNm() : l.upperLocgovNm() + " " + l.locgovNm();
                break;
            }
        }
        List<String> imageSrcs = new ArrayList<>();
        for (ItemManageClient.PreviewImage image : preview.images()) {
            imageSrcs.add(has(image.imageName()) ? giftBaseUrl + "/uploads/" + image.imageName() : "");
        }
        // 3조합(S3) 첫 드롭다운 - AS-IS는 c:set prev1로 "직전에 담은 이름과 다르고 숨김(N)인" 1단계 옵션명만 담는다(연속 중복만 제거).
        List<String> level1Names = new ArrayList<>();
        String prev1 = "";
        for (ItemManageClient.PreviewOption opt : preview.options()) {
            String name1 = opt.optionName1() == null ? "" : opt.optionName1();
            if (!name1.equals(prev1) && "N".equals(opt.optionHideFlag())) {
                prev1 = name1;
                level1Names.add(name1);
            }
        }
        // AS-IS 스크립트의 OPTIONS - 숨김 N·공개 Y인 옵션을 문자열 값으로(최대주문수량은 AS-IS 조건식이 깨져 항상 999, 재고는 재고연동 Y일 때만 수량).
        List<Map<String, String>> optionsJs = new ArrayList<>();
        for (ItemManageClient.PreviewOption opt : preview.options()) {
            if ("N".equals(opt.optionHideFlag()) && "Y".equals(opt.optionDisplayFlag())) {
                Map<String, String> o = new LinkedHashMap<>();
                o.put("id", String.valueOf(opt.itemOptionId()));
                o.put("n1", opt.optionName1() == null ? "" : opt.optionName1());
                o.put("n2", opt.optionName2() == null ? "" : opt.optionName2());
                o.put("n3", opt.optionName3() == null ? "" : opt.optionName3());
                o.put("price", opt.optionPrice() == null ? "" : String.valueOf(opt.optionPrice()));
                o.put("soldOutYN", opt.optionSoldOutFlag() == null ? "" : opt.optionSoldOutFlag());
                o.put("stockCnt", "Y".equals(opt.optionStockFlag()) ? String.valueOf(opt.optionStockQuantity()) : "999");
                o.put("orderMaxCnt", "999");
                optionsJs.add(o);
            }
        }
        model.addAttribute("itemInfo", preview);
        model.addAttribute("sellerInfo", preview.seller());
        model.addAttribute("locgovNm", locgovNm);
        model.addAttribute("imageSrcs", imageSrcs);
        model.addAttribute("level1Names", level1Names);
        model.addAttribute("optionsJs", optionsJs);
        return "item/seller-preview";
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
    @GetMapping({"/edit/{itemUserCode}", "/seller/edit/{itemUserCode}"})
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
        /** AS-IS op:nl2br(item.dataStatusMessage) - 평문이라 이스케이프한 뒤 줄바꿈만 <br/>로(승인관리 "등록보류" 레이어가 읽는다). */
        public String statusMessageHtml() {
            return com.ghlove.admin.service.QnaOpenAdminService.escapeNl2br(item.dataStatusMessage());
        }
    }

    public record RprsView(ItemManageClient.RprsRow item, String imageSrc) {
    }

    public record OffRprsView(ItemManageClient.OffRprsRow item, String imageSrc) {
    }

    public record LogView(ItemManageClient.LogRow log, String managerLoginId, String userName) {
    }

    /** AS-IS ItemParam 중 상품검색 팝업(find-item.jsp)이 쓰는 것. targetId는 연 화면의 담을 목록 id(예: 이벤트 관리 "prod"). */
    @Getter
    @Setter
    public static class FindItemParam {
        private String targetId;
        private String conditionType;
        private int categoryGroupId;
        private String categoryClass1 = "";
        private String categoryClass2 = "";
        private String categoryClass3 = "";
        private String categoryClass4 = "";
        private String where = "";
        private String query = "";
        private long sellerId;
        private int itemsPerPage = 10;
        private int page = 1;

        /** AS-IS ItemParam.getCategoryClass - 선택한 가장 깊은 차수. */
        String categoryClass() {
            for (String c : new String[]{categoryClass4, categoryClass3, categoryClass2, categoryClass1}) {
                if (c != null && !c.isBlank()) {
                    return c;
                }
            }
            return "";
        }
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
        private String dataStatusCode = "";
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
            return toSearch(locgov, null);
        }

        /** listType = 답례품승인관리면 ItemManageClient.PENDING_LIST(AS-IS conditionType ITEM_LIST_PENDING_APPROVAL), 아니면 null. */
        ItemManageClient.Search toSearch(String locgov, String listType) {
            Integer categoryIdNumber = null;
            try {
                categoryIdNumber = categoryId == null || categoryId.isBlank() ? null : Integer.valueOf(categoryId.trim());
            } catch (NumberFormatException ignored) {
                // 숫자가 아니면 정렬 조건에서만 쓰이므로 없는 것으로 본다
            }
            return new ItemManageClient.Search(locgov, shLocgovCode, where, query, srchDateType, searchStartDate, searchEndDate,
                    categoryGroupId, categoryClass(), categoryIdNumber, saleStatus, displayFlag, sellerId, orderBy, sort,
                    listType, dataStatusCode);
        }
    }
}
