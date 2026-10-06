package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.DesignatedProjectClient;
import com.ghlove.admin.service.RepresentativeBannerException;
import com.ghlove.admin.service.RepresentativeBannerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 지정기부 대표배너 관리 (AS-IS opmanager/designated-donation/banner). */
@Controller
@RequestMapping("/designated-projects/banners")
@RequiredArgsConstructor
public class RepresentativeBannerController {

    /** AS-IS 가드 문구 그대로. */
    private static final String DEPARTMENT_REQUIRED = "부서정보가 없어서 처리가 불가능합니다.";

    /** AS-IS가 통과시키는 권한 - 최고관리자(1~4)·지자체담당자(5~6). */
    private static final java.util.Set<String> MASTER_OR_LOCGOV = java.util.Set.of(
            "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4",
            "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    private final RepresentativeBannerService bannerService;
    private final DesignatedProjectClient designatedProjectClient;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerService.list());
        return "designated/banner-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("banner", null);
        return "designated/banner-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner", bannerService.get(id));
        return "designated/banner-form";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam(required = false) String linkUrl,
                          @RequestParam(required = false) String bannerContent, @RequestParam Integer displayOrder,
                          @RequestParam(required = false) MultipartFile pcImage,
                          @RequestParam(required = false) MultipartFile mobileImage,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            bannerService.create(title, linkUrl, bannerContent, displayOrder, pcImage, mobileImage, manager.getUserId());
            return "redirect:/designated-projects/banners";
        } catch (RepresentativeBannerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("banner", null);
            return "designated/banner-form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String title,
                          @RequestParam(required = false) String linkUrl,
                          @RequestParam(required = false) String bannerContent, @RequestParam Integer displayOrder,
                          @RequestParam(required = false) MultipartFile pcImage,
                          @RequestParam(required = false) MultipartFile mobileImage,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            bannerService.update(id, title, linkUrl, bannerContent, displayOrder, pcImage, mobileImage, manager.getUserId());
            return "redirect:/designated-projects/banners";
        } catch (RepresentativeBannerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("banner", bannerService.get(id));
            return "designated/banner-form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id, HttpSession session) {
        bannerService.toggle(id, manager(session).getUserId());
        return "redirect:/designated-projects/banners";
    }

    /**
     * AS-IS POST {@code /banner/change-ordering} - 노출순서 일괄 변경(ajax, JSON).
     * {@code id[]}와 {@code ordering[]}을 같은 인덱스로 짝지어 보낸다.
     */
    @PostMapping("/change-ordering")
    @ResponseBody
    public Map<String, Object> changeOrdering(@RequestParam(value = "id", required = false) List<Integer> id,
                                              @RequestParam(value = "ordering", required = false) List<Integer> ordering,
                                              HttpSession session) {
        Manager manager = manager(session);
        String denied = denyReason(manager);
        if (denied != null) {
            return Map.of("isSuccess", false, "errorMessage", denied);
        }
        bannerService.changeOrdering(id, ordering, manager.getUserId());
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS POST {@code /banner/delete} - <b>이미지 삭제</b>다(배너 행을 지우지 않는다).
     * {@code deleteFlag}가 {@code pc}/{@code mobile} 중 어느 쪽 이미지를 비울지 정한다 -
     * 자세한 내용은 {@link RepresentativeBannerService#clearImage} 주석.
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteImage(@RequestParam("representativeBannerId") Integer representativeBannerId,
                                           @RequestParam("deleteFlag") String deleteFlag,
                                           HttpSession session) {
        Manager manager = manager(session);
        String denied = denyReason(manager);
        if (denied != null) {
            return Map.of("isSuccess", false, "errorMessage", denied);
        }
        try {
            bannerService.clearImage(representativeBannerId, deleteFlag, manager.getUserId());
        } catch (RepresentativeBannerException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS {@code checkDsgncntrAuthPartInfo()} 가드 - 배너 화면의 모든 입구에 걸려 있다.
     * 0이면 "부서정보가 없어서 처리가 불가능합니다."로 막는다.
     *
     * <p>AS-IS 분기: <b>특정사업 담당자({@code ROLE_ADMIN_10})</b>는 자기 부서 id를 조회해
     * 없으면 0(차단) · <b>지자체담당자·최고관리자</b>는 -1(통과) · 그 외는 0(차단).
     *
     * <p><b>근사 1건</b>: ROLE_ADMIN_10의 "자기 부서 id" 조회 API가 donation 쪽에 아직 없어
     * (있는 것은 지자체별 부서 <b>목록</b>이다) 그 지자체에 등록된 부서가 하나도 없으면 막는다.
     * 담당자 개인에게 부서가 배정됐는지까지는 보지 못한다 - donation API가 생기면 그 값으로 바꿀 것.
     *
     * @return 막아야 하면 안내문구, 통과면 {@code null}
     */
    private String denyReason(Manager manager) {
        String authority = manager == null ? null : manager.getAuthority();
        if (authority == null) {
            return DEPARTMENT_REQUIRED;
        }
        if ("ROLE_ADMIN_10".equals(authority)) {
            return designatedProjectClient.departments(manager.getLocgovCode()).isEmpty()
                    ? DEPARTMENT_REQUIRED : null;
        }
        // 최고관리자(1~4)·지자체담당자(5~6)는 AS-IS가 -1을 돌려 통과시킨다
        if (MASTER_OR_LOCGOV.contains(authority)) {
            return null;
        }
        return DEPARTMENT_REQUIRED;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
