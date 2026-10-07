package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.FeaturedAdminClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 이벤트 관리 (AS-IS opmanager/featured - FeaturedManagerController, 메뉴 "이벤트 관리" MENU_12000).
 *  OP_FEATURED는 지자체별 이벤트(class=0,type=1)다. gift 서비스의 OP_FEATURED/OP_FEATURED_ITEM을
 *  cross-service로 다루되, 검색·지자체 스코핑·진행상태·지자체명·페이징은 admin에서 처리한다
 *  (gift 조회 API는 전체 목록만 주므로 - 1406·배송업체 관리와 동일 패턴). */
@Controller
@RequestMapping("/admin/featured")
@RequiredArgsConstructor
public class FeaturedAdminController {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FeaturedAdminClient client;
    private final LocgovClient locgovClient;

    /** 목록 한 줄 - AS-IS featured/list.jsp가 쓰는 파생값(progression·locgovName) 포함. */
    public record FeaturedRow(FeaturedAdminClient.FeaturedDto featured, String progression, String locgovName) {
        public Integer featuredId() { return featured.featuredId(); }
        public String featuredName() { return featured.featuredName(); }
        public String displayListFlag() { return featured.displayListFlag(); }
        public String startDate() { return featured.startDate(); }
        public String endDate() { return featured.endDate(); }
        public String createdDate() { return featured.createdDate(); }
    }

    /**
     * AS-IS FeaturedManagerController.list(GET=빈 목록)/searchList(POST=검색). GET으로 들어오면
     * 검색폼만 보이고 결과는 비운다(AS-IS와 동일). featuredType은 PC(=1) 고정(모바일 데이터는 없음).
     * 지자체 스코핑: ROLE_ADMIN_5~8은 자기 지자체만, 1~4(시스템/행안부)는 전체 + 시도/시군구 필터.
     */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@RequestParam(required = false, defaultValue = "FEATURED_NAME") String where,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String progression,
                        @RequestParam(required = false) String displayListFlag,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String role = manager != null ? manager.getAuthority() : null;
        boolean systemOrMois = MenuService.UNRESTRICTED_ROLES.contains(role);       // 1~4: 전체
        boolean locgovScoped = "ROLE_ADMIN_5".equals(role) || "ROLE_ADMIN_6".equals(role)
                || "ROLE_ADMIN_7".equals(role) || "ROLE_ADMIN_8".equals(role);      // 5~8: 자기 지자체
        boolean canManage = "ROLE_ADMIN_5".equals(role) || "ROLE_ADMIN_6".equals(role); // 등록/삭제/체크박스

        Map<String, String> locgovNames = locgovNames();

        boolean isSearch = "POST".equalsIgnoreCase(request.getMethod());
        List<FeaturedRow> rows = new ArrayList<>();
        int total = 0;
        if (isSearch) {
            // 스코핑: 5~8은 자기 지자체 강제, 1~4는 폼의 시도/시군구 필터, 그 외는 없음("00000")
            String scopeLocgov = locgovScoped ? (manager.getLocgovCode() != null ? manager.getLocgovCode() : "00000")
                    : (systemOrMois ? blankToNull(locgovCode) : "00000");
            String scopeUpper = systemOrMois ? blankToNull(upperLocgovCode) : null;

            List<FeaturedAdminClient.FeaturedDto> all = client.list("1", null);  // PC 이벤트 전체
            String today = YMD.format(LocalDate.now());
            List<FeaturedRow> matched = all.stream()
                    .filter(f -> matchesQuery(f, where, query))
                    .filter(f -> displayListFlag == null || displayListFlag.isBlank()
                            || displayListFlag.equals(f.displayListFlag()))
                    .filter(f -> matchesLocgov(f, scopeLocgov, scopeUpper))
                    .map(f -> new FeaturedRow(f, progressionOf(f, today), locgovNames.getOrDefault(f.locgovCode(), f.locgovCode())))
                    .filter(r -> progression == null || progression.isBlank() || progression.equals(r.progression()))
                    .sorted(Comparator.comparing((FeaturedRow r) -> r.featuredId() == null ? 0 : r.featuredId()).reversed())
                    .toList();
            total = matched.size();
            Pagination pagination = Pagination.of(total, page, itemsPerPage).withLinkFrom(request);
            rows = matched.stream().skip(pagination.getStartRow()).limit(pagination.getItemsPerPage()).toList();
            model.addAttribute("pagination", pagination);
        } else {
            model.addAttribute("pagination", Pagination.of(0, 1, itemsPerPage).withLinkFrom(request));
        }

        model.addAttribute("featuredList", rows);
        model.addAttribute("featuredCount", total);
        model.addAttribute("where", where);
        model.addAttribute("query", query);
        model.addAttribute("progression", progression);
        model.addAttribute("displayListFlag", displayListFlag);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("itemsPerPage", itemsPerPage);
        model.addAttribute("sido", sido());
        model.addAttribute("systemOrMois", systemOrMois);
        model.addAttribute("canManage", canManage);
        model.addAttribute("giftBaseUrl", client.giftServiceBaseUrl());
        return "featured-admin/list";
    }

    /** AS-IS 시군구 ajax(/common/getLocgovCode) 대응 - 선택한 시도의 시군구 목록. */
    @GetMapping("/locgov-children")
    @ResponseBody
    public List<Map<String, String>> locgovChildren(@RequestParam("upper") String upperLocgovCode) {
        List<Map<String, String>> result = new ArrayList<>();
        locgovClient.allLocgovs().stream()
                .filter(l -> upperLocgovCode.equals(l.upperLocgovCode()) && !l.locgovCode().equals(l.upperLocgovCode()))
                .forEach(l -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("LOCGOV_CODE", l.locgovCode());
                    m.put("LOCGOV_NM", l.locgovNm());
                    result.add(m);
                });
        return result;
    }

    /** AS-IS checkedDelete(POST /checked-delete) - ROLE_ADMIN_5/6만. */
    @PostMapping("/checked-delete")
    public String checkedDelete(@RequestParam(required = false) List<Integer> featuredIds,
                                HttpSession session, RedirectAttributes redirect) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String role = manager != null ? manager.getAuthority() : null;
        if (!("ROLE_ADMIN_5".equals(role) || "ROLE_ADMIN_6".equals(role))) {
            redirect.addFlashAttribute("errorMessage", "삭제 불가능 합니다.");
            return "redirect:/admin/featured";
        }
        if (featuredIds != null) {
            for (Integer id : featuredIds) {
                try {
                    client.delete(id);
                } catch (ManagerException ignore) {
                    // 일부 실패해도 나머지는 진행(AS-IS deleteFeaturedsById는 일괄 삭제)
                }
            }
        }
        return "redirect:/admin/featured";
    }

    private boolean matchesQuery(FeaturedAdminClient.FeaturedDto f, String where, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.trim();
        if ("FEATURED_URL".equals(where)) {
            return f.featuredUrl() != null && f.featuredUrl().contains(q);
        }
        return f.featuredName() != null && f.featuredName().contains(q);
    }

    private boolean matchesLocgov(FeaturedAdminClient.FeaturedDto f, String scopeLocgov, String scopeUpper) {
        if (scopeLocgov != null) {
            return scopeLocgov.equals(f.locgovCode());
        }
        if (scopeUpper != null) {
            return locgovClient.allLocgovs().stream()
                    .anyMatch(l -> scopeUpper.equals(l.upperLocgovCode()) && l.locgovCode().equals(f.locgovCode()));
        }
        return true;
    }

    /** AS-IS PROGRESSION CASE(1 미진행/2 진행중/3 진행완료)의 날짜정밀 대응. */
    private String progressionOf(FeaturedAdminClient.FeaturedDto f, String today) {
        String s = f.startDate();
        String e = f.endDate();
        boolean hasS = s != null && !s.isBlank();
        boolean hasE = e != null && !e.isBlank();
        if (hasS && s.compareTo(today) > 0) {
            return "1";   // 미진행(시작 전)
        }
        if (hasE && e.compareTo(today) < 0) {
            return "3";   // 진행완료(종료 후)
        }
        return "2";       // 진행중(기간 내 / 무기한)
    }

    private Map<String, String> locgovNames() {
        Map<String, String> names = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> names.put(l.locgovCode(),
                l.locgovCode().equals(l.upperLocgovCode()) ? l.upperLocgovNm()
                        : (l.upperLocgovNm() + " " + l.locgovNm())));
        return names;
    }

    /** 시도(상위 지자체) 중복 없이 - ROLE_ADMIN_1~4 검색폼의 시도 select. */
    private List<Map<String, String>> sido() {
        Map<String, String> distinct = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> distinct.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm()));
        List<Map<String, String>> result = new ArrayList<>();
        distinct.forEach((code, nm) -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("value", code);
            m.put("label", nm);
            result.add(m);
        });
        return result;
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("mode", "create");
        // Spring의 MapAccessor(Thymeleaf map.key 접근)는 키가 아예 없으면(Map.of()처럼 빈 맵)
        // "찾을 수 없음"으로 취급해 예외를 던진다(값이 null인 키는 정상 처리) - 템플릿이 읽는
        // 모든 키를 null로 미리 채워둬야 신규등록 폼이 500 없이 렌더링된다.
        Map<String, Object> emptyFeatured = new HashMap<>();
        for (String key : List.of("featuredClass", "featuredType", "featuredCode", "featuredUrl", "featuredName",
                "featuredSimpleContent", "featuredContent", "link", "featuredImage", "thumbnailImage", "featuredFlag",
                "displayListFlag", "ordering", "startDate", "endDate", "locgovCode")) {
            emptyFeatured.put(key, null);
        }
        model.addAttribute("featured", emptyFeatured);
        model.addAttribute("items", List.of());
        return "featured-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Map<String, Object> result = client.get(id);
        model.addAttribute("mode", "edit");
        model.addAttribute("featured", result.get("featured"));
        model.addAttribute("items", result.get("items"));
        model.addAttribute("giftBaseUrl", client.giftServiceBaseUrl());
        return "featured-admin/form";
    }

    @PostMapping("/create")
    public String create(@RequestParam Integer featuredClass, @RequestParam String featuredType,
                          @RequestParam String featuredUrl, @RequestParam String featuredCode,
                          @RequestParam String featuredName, @RequestParam String featuredSimpleContent,
                          @RequestParam String featuredContent, @RequestParam(required = false) String link,
                          @RequestParam(required = false) String featuredFlag,
                          @RequestParam(required = false) String displayListFlag,
                          @RequestParam(required = false) Integer ordering,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) String locgovCode,
                          @RequestParam(required = false) MultipartFile image,
                          @RequestParam(required = false) MultipartFile thumbnailImage,
                          RedirectAttributes redirect) {
        try {
            client.create(formFields(featuredClass, featuredType, featuredUrl, featuredCode, featuredName,
                    featuredSimpleContent, featuredContent, link, featuredFlag, displayListFlag, ordering,
                    startDate, endDate, locgovCode), image, thumbnailImage);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/featured?featuredType=" + featuredType;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam Integer featuredClass, @RequestParam String featuredType,
                          @RequestParam String featuredUrl, @RequestParam String featuredCode,
                          @RequestParam String featuredName, @RequestParam String featuredSimpleContent,
                          @RequestParam String featuredContent, @RequestParam(required = false) String link,
                          @RequestParam(required = false) String featuredFlag,
                          @RequestParam(required = false) String displayListFlag,
                          @RequestParam(required = false) Integer ordering,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) String locgovCode,
                          @RequestParam(required = false) MultipartFile image,
                          @RequestParam(required = false) MultipartFile thumbnailImage,
                          RedirectAttributes redirect) {
        try {
            client.update(id, formFields(featuredClass, featuredType, featuredUrl, featuredCode, featuredName,
                    featuredSimpleContent, featuredContent, link, featuredFlag, displayListFlag, ordering,
                    startDate, endDate, locgovCode), image, thumbnailImage);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/featured/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, @RequestParam(required = false, defaultValue = "1") String featuredType,
                          RedirectAttributes redirect) {
        try {
            client.delete(id);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/featured?featuredType=" + featuredType;
    }

    @PostMapping("/{id}/items")
    public String replaceItems(@PathVariable Integer id, @RequestParam(required = false) String itemIdsCsv,
                                RedirectAttributes redirect) {
        List<Long> ids = (itemIdsCsv == null || itemIdsCsv.isBlank())
                ? List.of()
                : java.util.Arrays.stream(itemIdsCsv.split("[,\\s]+"))
                        .filter(s -> !s.isBlank())
                        .map(Long::parseLong)
                        .toList();
        try {
            client.replaceItems(id, ids);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/featured/" + id + "/edit";
    }

    private Map<String, Object> formFields(Integer featuredClass, String featuredType, String featuredUrl,
                                            String featuredCode, String featuredName, String featuredSimpleContent,
                                            String featuredContent, String link, String featuredFlag,
                                            String displayListFlag, Integer ordering, String startDate,
                                            String endDate, String locgovCode) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("featuredClass", featuredClass);
        fields.put("featuredType", featuredType);
        fields.put("featuredUrl", featuredUrl);
        fields.put("featuredCode", featuredCode);
        fields.put("featuredName", featuredName);
        fields.put("featuredSimpleContent", featuredSimpleContent);
        fields.put("featuredContent", featuredContent);
        fields.put("link", link);
        fields.put("featuredFlag", featuredFlag);
        fields.put("displayListFlag", displayListFlag);
        fields.put("ordering", ordering);
        fields.put("startDate", startDate);
        fields.put("endDate", endDate);
        fields.put("locgovCode", locgovCode);
        return fields;
    }
}
