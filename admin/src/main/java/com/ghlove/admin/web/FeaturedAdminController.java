package com.ghlove.admin.web;

import com.ghlove.admin.service.FeaturedAdminClient;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 기획전(Featured) 관리 (AS-IS opmanager/featured, featured-mobile - FeaturedManagerController).
 *  gift 서비스의 OP_FEATURED/OP_FEATURED_ITEM을 cross-service로 CRUD한다. */
@Controller
@RequestMapping("/admin/featured")
@RequiredArgsConstructor
public class FeaturedAdminController {

    private final FeaturedAdminClient client;

    @GetMapping
    public String list(@RequestParam(required = false, defaultValue = "1") String featuredType,
                        @RequestParam(required = false) String featuredName, Model model) {
        model.addAttribute("featuredList", client.list(featuredType, featuredName));
        model.addAttribute("featuredType", featuredType);
        model.addAttribute("featuredName", featuredName);
        model.addAttribute("giftBaseUrl", client.giftServiceBaseUrl());
        return "featured-admin/list";
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

    @PostMapping
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
