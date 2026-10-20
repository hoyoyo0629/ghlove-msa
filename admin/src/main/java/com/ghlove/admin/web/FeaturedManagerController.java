package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.FeaturedAdminClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 > 답례품관리 > 이벤트 관리(메뉴 16601) - AS-IS {@code saleson.shop.featured.FeaturedManagerController}
 * ({@code /opmanager/featured}, list.jsp·form.jsp) 이식. OP_FEATURED는 지자체별 이벤트이고 gift 서비스에 있다.
 *
 * <p>AS-IS 그대로인 것: GET 목록은 조회하지 않고(총 0건) POST만 조회, 시스템/행안부(ROLE_ADMIN_1~4)는 전체 + 시도/시군구 필터,
 * 지자체(ROLE_ADMIN_5~8)는 소속 지자체만(없으면 "00000"), 그 밖은 "00000". 목록의 체크박스·[등록]·[삭제]는 ROLE_ADMIN_5/6에게만,
 * 등록/수정 저장은 ROLE_ADMIN_5~8만(그 밖은 "등록/수정 불가능 합니다."와 함께 /featured/list로), 수정 화면 열기는 권한·지자체 검사가 없다.
 * 선택삭제·이미지삭제는 관리자면 누구나. PC 이벤트(FEATURED_TYPE 1)만 있다 - AS-IS featured-mobile 경로는 메뉴에 없다.
 */
@Controller
@RequestMapping("/featured")
@RequiredArgsConstructor
public class FeaturedManagerController {

    private static final String LIST_URL = "/featured/list";
    /** AS-IS getFeaturedTypeUri() - TO-BE는 PC(featured)만. */
    private static final String FEATURED_TYPE_URI = "featured";
    private static final Set<String> LOCGOV_ROLES = Set.of("ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_7", "ROLE_ADMIN_8");
    private static final Set<String> SYSTEM_ROLES = Set.of("ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4");

    private final FeaturedAdminClient client;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final CommonMessageService commonMessageService;
    private final FlashRedirect flashRedirect;

    /** gift 서비스 주소 - 이벤트 이미지·상품 이미지(/uploads/..)가 거기서 서빙된다. */
    @Value("${ghlove.gift-service.base-url}")
    private String giftBaseUrl;

    // ---- 목록 ----

    /** AS-IS list(GET) - 검색하지 않는다(featuredList 빈 목록, 총 0건). */
    @GetMapping("/list")
    public String list(@RequestParam(value = "itemsPerPage", required = false) String itemsPerPage,
                       @ModelAttribute("featuredParam") FeaturedParam param, HttpSession session, Model model) {
        Manager manager = manager(session);
        setLocgovCode(param, manager);
        param.setItemsPerPage(parseItemsPerPage(itemsPerPage));
        fillListModel(model, manager, param, List.of(), 0, Pagination.of(0, 1, param.getItemsPerPage()).withLink(LIST_URL + "?page=[page]"),
                itemsPerPage);
        return "featured/list";
    }

    /** AS-IS searchList(POST). */
    @PostMapping("/list")
    public String searchList(@RequestParam(value = "itemsPerPage", required = false) String itemsPerPage,
                             @ModelAttribute("featuredParam") FeaturedParam param, HttpSession session, Model model) {
        Manager manager = manager(session);
        setLocgovCode(param, manager);
        param.setItemsPerPage(parseItemsPerPage(itemsPerPage));
        FeaturedAdminClient.Search search = new FeaturedAdminClient.Search("1", param.getWhere(), param.getQuery(), param.getProgression(),
                param.getDisplayListFlag(), param.getLocgovCode(), param.getUpperLocgovCode(), codesUnder(param.getUpperLocgovCode()));
        int count = client.count(search);
        Pagination pagination = Pagination.of(count, param.getPage(), param.getItemsPerPage()).withLink(LIST_URL + "?page=[page]");
        List<FeaturedAdminClient.Row> rows = client.list(search, pagination.getStartRow(), pagination.getItemsPerPage());
        Map<String, String> names = locgovNames();
        List<RowView> views = new ArrayList<>();
        for (FeaturedAdminClient.Row r : rows) {
            views.add(new RowView(r, names.get(r.locgovCode())));
        }
        fillListModel(model, manager, param, views, count, pagination, itemsPerPage);
        return "featured/list";
    }

    private void fillListModel(Model model, Manager manager, FeaturedParam param, List<RowView> list, int count, Pagination pagination,
                               String itemsPerPage) {
        model.addAttribute("featuredList", list);
        model.addAttribute("featuredCount", count);
        model.addAttribute("pagination", pagination);
        model.addAttribute("featuredTypeUri", FEATURED_TYPE_URI);
        model.addAttribute("sido", sidoOptions());
        model.addAttribute("itemsPerPage", itemsPerPage);
        model.addAttribute("roleSystem", SYSTEM_ROLES.contains(manager.getAuthority()));
        model.addAttribute("roleLocgovCharger", "ROLE_ADMIN_5".equals(manager.getAuthority()) || "ROLE_ADMIN_6".equals(manager.getAuthority()));
    }

    /** AS-IS itemsPerPage 문자열 → 숫자(없거나 숫자가 아니면 10). */
    private static int parseItemsPerPage(String itemsPerPage) {
        if (itemsPerPage == null) {
            return 10;
        }
        try {
            return Integer.parseInt(itemsPerPage);
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    /** AS-IS setLocgovCode - 지자체 담당(5~8)은 소속 지자체(없으면 00000), 시스템/행안부(1~4)는 폼 값 그대로, 그 밖은 00000. */
    private static void setLocgovCode(FeaturedParam param, Manager manager) {
        String role = manager.getAuthority();
        if (LOCGOV_ROLES.contains(role)) {
            param.setLocgovCode(has(manager.getLocgovCode()) ? manager.getLocgovCode() : "00000");
        } else if (!SYSTEM_ROLES.contains(role)) {
            param.setLocgovCode("00000");
        }
    }

    // ---- 등록 / 수정 ----

    /** AS-IS create(GET) - ROLE_ADMIN_5~8만. 그 밖은 "등록 불가능 합니다."와 함께 /featured/list로(AS-IS 경로 그대로). */
    @GetMapping("/create")
    public String create(HttpSession session, Model model) {
        Manager manager = manager(session);
        if (!LOCGOV_ROLES.contains(manager.getAuthority())) {
            return flashRedirect.to(LIST_URL, "등록 불가능 합니다.");
        }
        fillFormModel(model, manager, "create", null, "0");
        return "featured/form";
    }

    /** AS-IS createAction - 소속 지자체로 저장하고 목록으로(M00288 등록되었습니다.). */
    @PostMapping("/create")
    public String createAction(@RequestParam Map<String, String> params,
                               @RequestParam(required = false) MultipartFile featuredFile,
                               @RequestParam(required = false) MultipartFile featuredFileMobile,
                               @RequestParam(required = false) MultipartFile featuredListFile,
                               HttpSession session, Model model) {
        Manager manager = manager(session);
        if (!LOCGOV_ROLES.contains(manager.getAuthority())) {
            return flashRedirect.to(LIST_URL, "등록 불가능 합니다.");
        }
        if (!has(manager.getLocgovCode())) {
            // AS-IS ViewUtils.view("소속된 지자체 정보가 없습니다.") - 같은 화면에 안내만 띄운다.
            fillFormModel(model, manager, "create", null, "0");
            model.addAttribute("redirectMessage", "소속된 지자체 정보가 없습니다.");
            return "featured/form";
        }
        try {
            client.create(formParams(params), manager.getLocgovCode(), featuredFile, featuredFileMobile, featuredListFile);
        } catch (ManagerException e) {
            fillFormModel(model, manager, "create", null, "0");
            model.addAttribute("redirectMessage", e.getMessage());
            return "featured/form";
        }
        return flashRedirect.to("/featured/list", msg("M00288"));
    }

    /** AS-IS edit(GET) - 번호로만 찾는다(권한·지자체 검사 없음). */
    @GetMapping("/edit/{featuredId}")
    public String edit(@PathVariable int featuredId, HttpSession session, Model model) {
        Manager manager = manager(session);
        FeaturedAdminClient.Detail featured = client.detail(featuredId);
        if (featured == null) {
            return flashRedirect.to(LIST_URL, null);
        }
        fillFormModel(model, manager, "edit", featured, "1");
        return "featured/form";
    }

    /** AS-IS editAction - ROLE_ADMIN_5~8만, 저장 뒤 목록으로(M00289 수정되었습니다.). */
    @PostMapping("/edit/{featuredId}")
    public String editAction(@PathVariable int featuredId, @RequestParam Map<String, String> params,
                             @RequestParam(required = false) MultipartFile featuredFile,
                             @RequestParam(required = false) MultipartFile featuredFileMobile,
                             @RequestParam(required = false) MultipartFile featuredListFile,
                             HttpSession session, Model model) {
        Manager manager = manager(session);
        if (!LOCGOV_ROLES.contains(manager.getAuthority())) {
            return flashRedirect.to(LIST_URL, "수정 불가능 합니다.");
        }
        if (!has(manager.getLocgovCode())) {
            fillFormModel(model, manager, "edit", client.detail(featuredId), "1");
            model.addAttribute("redirectMessage", "소속된 지자체 정보가 없습니다.");
            return "featured/form";
        }
        try {
            client.update(featuredId, formParams(params), featuredFile, featuredFileMobile, featuredListFile);
        } catch (ManagerException e) {
            fillFormModel(model, manager, "edit", client.detail(featuredId), "1");
            model.addAttribute("redirectMessage", e.getMessage());
            return "featured/form";
        }
        return flashRedirect.to("/featured/list", msg("M00289"));
    }

    /** 폼이 보내는 칸 - AS-IS editAction은 FEATURED_TYPE을 URI로 다시 정한다(PC=1). */
    private static Map<String, String> formParams(Map<String, String> params) {
        Map<String, String> p = new LinkedHashMap<>();
        for (String key : List.of("featuredName", "startDate", "startTime", "endDate", "endTime", "featuredPhoneNo1", "featuredPhoneNo2",
                "featuredPhoneNo3", "featuredHost", "link", "featuredContent", "prodState", "displayListFlag", "featuredFlag", "prodString")) {
            p.put(key, params.get(key));
        }
        p.put("featuredType", "1");
        return p;
    }

    private void fillFormModel(Model model, Manager manager, String mode, FeaturedAdminClient.Detail featured, String featuredCheck) {
        model.addAttribute("mode", mode);
        model.addAttribute("featured", featured != null ? new FormView(featured, giftBaseUrl) : FormView.empty());
        model.addAttribute("list", featured != null ? featured.items().stream().map(i -> new ItemView(i, imageSrc(i.thumbnail()))).toList()
                : List.of());
        model.addAttribute("userGroupList", featured != null && featured.userGroups() != null ? featured.userGroups() : List.of());
        model.addAttribute("featuredCheck", featuredCheck);
        model.addAttribute("featuredTypeUri", FEATURED_TYPE_URI);
        // AS-IS mainPart - 등록은 MENU_12000(이벤트), 수정은 FEATURED_CLASS 1이면 "기획전" 아니면 MENU_12000
        model.addAttribute("mainPart", featured != null && featured.featuredClass() != null && featured.featuredClass() == 1
                ? msg("기획전") : msg("MENU_12000"));
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        model.addAttribute("hours", hours);
        // AS-IS PHONE(휴대폰 앞자리) + TEL(지역번호) 코드를 이어 붙인 select
        List<String> phone = new ArrayList<>(commonCodeService.labelsOf("PHONE").keySet());
        phone.addAll(commonCodeService.labelsOf("TEL").keySet());
        model.addAttribute("phone", phone);
        model.addAttribute("canSave", LOCGOV_ROLES.contains(manager.getAuthority()));
    }

    // ---- 삭제 ----

    /** AS-IS checkedDelete - 관리자면 누구나, OP_FEATURED만 지운다. */
    @PostMapping("/checked-delete")
    public String checkedDelete(@RequestParam(value = "featuredIds", required = false) List<Integer> featuredIds, HttpSession session) {
        manager(session);
        client.delete(featuredIds == null ? List.of() : featuredIds);
        return flashRedirect.to("/featured/list", msg("M00205"));
    }

    /** AS-IS deleteImage(ajax) - 칸을 비운다. 응답은 AS-IS JsonViewUtils.success() 모양. */
    @PostMapping("/delete-image")
    @ResponseBody
    public Map<String, Object> deleteImage(@RequestParam("featuredId") int featuredId, @RequestParam("fileType") int fileType,
                                           HttpSession session) {
        manager(session);
        Map<String, Object> result = new LinkedHashMap<>();
        if (client.deleteImage(featuredId, fileType)) {
            result.put("isSuccess", true);
        } else {
            result.put("isSuccess", false);
            result.put("errorMessage", "삭제 불가능 합니다.");
        }
        return result;
    }

    // ---- 보조 ----

    private Manager manager(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (manager == null) {
            throw new IllegalStateException("관리자 로그인이 필요합니다.");
        }
        return manager;
    }

    private String imageSrc(String thumbnail) {
        return has(thumbnail) ? giftBaseUrl + "/uploads/" + thumbnail : "/content/images/common/no-image-gray.gif";
    }

    /** AS-IS CodeUtils.getCodeList("WDR") - 시도. */
    private List<Map<String, String>> sidoOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("value", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    /** AS-IS 목록 매퍼의 {@code LOCGOV_CODE IN (SELECT LOCGOV_CODE FROM G_LOCGOV WHERE UPPER_LOCGOV_CODE = ?)}. */
    private List<String> codesUnder(String upperLocgovCode) {
        if (!has(upperLocgovCode)) {
            return List.of();
        }
        return locgovClient.allLocgovs().stream().filter(l -> upperLocgovCode.equals(l.upperLocgovCode()))
                .map(LocgovClient.LocgovInfo::locgovCode).toList();
    }

    /** AS-IS LOCGOV_NAME - 시도 자신이면 시도명, 아니면 "시도명 시군구명". */
    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            names.put(l.locgovCode(), l.locgovCode().equals(l.upperLocgovCode()) ? l.upperLocgovNm()
                    : (l.upperLocgovNm() + " " + l.locgovNm()));
        }
        return names;
    }

    private String msg(String code) {
        return commonMessageService.get(code);
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    // ---- 화면용 타입 ----

    public record RowView(FeaturedAdminClient.Row row, String locgovName) {
    }

    public record ItemView(FeaturedAdminClient.Item item, String imageSrc) {
    }

    /** form.jsp가 읽는 featured.* - 이미지 칸은 AS-IS getXxxSrc()처럼 업로드 경로(/featured/{번호}/{파일})를 붙인 주소. */
    public record FormView(Integer featuredId, String featuredType, String featuredName, String featuredContent, String featuredImage,
                           String featuredImageMobile, String featuredListImage, String featuredImageSrc, String featuredImageMobileSrc,
                           String featuredListImageSrc, String featuredFlag, String displayListFlag, String link, String prodState,
                           String startDate, String startTime, String endDate, String endTime, String featuredHost,
                           String featuredPhoneNo1, String featuredPhoneNo2, String featuredPhoneNo3, String featuredUrl) {

        static FormView empty() {
            // AS-IS create - new Featured()에 featuredType "1"만 넣는다(나머지는 도메인 기본값: link "", 문자열은 null).
            return new FormView(0, "1", null, null, null, null, null, "", "", "", null, null, "", null, null, null, null, null,
                    null, null, null, null, null);
        }

        FormView(FeaturedAdminClient.Detail d, String giftBaseUrl) {
            this(d.featuredId(), d.featuredType(), d.featuredName(), d.featuredContent(), d.featuredImage(), d.featuredImageMobile(),
                    d.featuredListImage(), src(giftBaseUrl, d.featuredId(), d.featuredImage()), src(giftBaseUrl, d.featuredId(), d.featuredImageMobile()),
                    src(giftBaseUrl, d.featuredId(), d.featuredListImage()), d.featuredFlag(), d.displayListFlag(), d.link(), d.prodState(),
                    d.startDate(), d.startTime(), d.endDate(), d.endTime(), d.featuredHost(), d.featuredPhoneNo1(), d.featuredPhoneNo2(),
                    d.featuredPhoneNo3(), d.featuredUrl());
        }

        private static String src(String base, Integer id, String file) {
            return file == null || file.isEmpty() ? "" : base + "/uploads/featured/" + id + "/" + file;
        }
    }

    /** AS-IS FeaturedParam 중 목록 폼이 보내는 것. */
    @Getter
    @Setter
    public static class FeaturedParam {
        private String upperLocgovCode;
        private String locgovCode;
        private String where = "FEATURED_NAME";
        private String query;
        private String progression;
        private String displayListFlag;
        private int itemsPerPage = 10;
        private int page = 1;
        /** AS-IS listForm의 hidden featuredCode - 화면이 비워 보낸다. */
        private String featuredCodeChecked;
    }
}
