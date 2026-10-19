package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.RepresentativeBanner;
import com.ghlove.admin.service.RepresentativeBannerException;
import com.ghlove.admin.service.RepresentativeBannerService;
import com.ghlove.admin.web.support.AdminRole;
import com.ghlove.admin.web.support.FlashRedirect;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 대표배너관리 (AS-IS opmanager/representative-banner, 메뉴 16251) - 답례품 대표배너(G_REPRST_BANNER, PROCESS_TYPE='ITEM').
 *
 * <p>AS-IS {@code RepresentativeBannerManagerController}를 그대로 옮겼다. 특정사업 기부 배너관리({@link RepresentativeBannerController},
 * PROCESS_TYPE='DESIGNATED_DONATION')와 같은 테이블·같은 서비스·같은 JSP 모양이고 URL과 처리유형만 다르다 - 서비스는 그쪽과 공유한다.
 * 등록·수정·순서저장 권한은 AS-IS처럼 서버가 아니라 화면(JS)이 시스템 관리자(adminRole == SYS)만 허용한다. 지정기부 쪽에 있는 부서정보
 * 가드는 이 화면에는 없다. 등록은 {@code form/0}, 수정은 {@code form/{id}} 로 같은 폼을 쓰고, 이미지 "삭제"는 배너가 아니라 이미지 파일명만 비운다.
 */
@Controller
@RequestMapping("/representative-banner")
@RequiredArgsConstructor
public class ItemRepresentativeBannerController {

    /** AS-IS 목록은 항상 15행이다 - 등록된 배너 뒤를 빈 슬롯으로 채운다. */
    private static final int MAX_SLOTS = 15;

    /** M00406. */
    private static final String SAVED = "저장되었습니다.";

    private static final String LIST_URL = "/representative-banner/list";

    private final RepresentativeBannerService bannerService;
    private final FlashRedirect flashRedirect;

    @GetMapping({"", "/", "/list"})
    public String list(HttpSession session, Model model) {
        List<RepresentativeBanner> banners = bannerService.list(RepresentativeBannerService.PROCESS_TYPE_ITEM);
        model.addAttribute("bannerList", banners);
        model.addAttribute("bannerCount", banners.size());
        model.addAttribute("emptySlots", Math.max(0, MAX_SLOTS - banners.size()));
        model.addAttribute("adminRole", AdminRole.of(manager(session)));
        return "representative-banner/list";
    }

    /** AS-IS GET form/{id} - id 0은 등록(배너 없음), 그 밖은 수정. */
    @GetMapping("/form/{representativeBannerId}")
    public String form(@PathVariable Integer representativeBannerId, HttpSession session, Model model) {
        putFormModel(model, representativeBannerId > 0 ? bannerService.get(representativeBannerId) : null, manager(session));
        return "representative-banner/form";
    }

    /** AS-IS POST form/{id} - 등록/수정 후 목록으로(저장되었습니다). 노출순서는 폼에 없고 등록 때 맨 뒤(MAX+1)로 붙는다. */
    @PostMapping("/form/{representativeBannerId}")
    public String formAction(@PathVariable Integer representativeBannerId,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String linkUrl,
                             @RequestParam(required = false) String bannerContent,
                             @RequestParam(required = false, defaultValue = "Y") String useYn,
                             @RequestParam(required = false) MultipartFile uploadFilePc,
                             @RequestParam(required = false) MultipartFile uploadFileMobile,
                             HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            if (representativeBannerId == 0) {
                bannerService.create(RepresentativeBannerService.PROCESS_TYPE_ITEM, title, linkUrl, bannerContent, useYn,
                        uploadFilePc, uploadFileMobile, manager.getUserId());
            } else {
                bannerService.update(representativeBannerId, title, linkUrl, bannerContent, useYn, uploadFilePc, uploadFileMobile,
                        manager.getUserId());
            }
            return flashRedirect.to(LIST_URL, SAVED);
        } catch (RepresentativeBannerException e) {
            putFormModel(model, representativeBannerId > 0 ? bannerService.get(representativeBannerId) : null, manager);
            model.addAttribute("redirectMessage", e.getMessage());
            return "representative-banner/form";
        }
    }

    /** AS-IS POST change-ordering - id[]와 ordering[]을 같은 인덱스로 짝지어 노출순서를 저장한다. */
    @ResponseBody
    @PostMapping("/change-ordering")
    public Map<String, Object> changeOrdering(@RequestParam(value = "id", required = false) List<Integer> id,
                                              @RequestParam(value = "ordering", required = false) List<Integer> ordering,
                                              HttpSession session) {
        bannerService.changeOrdering(id, ordering, manager(session).getUserId());
        return Map.of("isSuccess", true);
    }

    /** AS-IS POST delete - 이미지 삭제(배너 행은 그대로, deleteFlag가 가리키는 pc/mobile 이미지 파일명만 비운다). */
    @ResponseBody
    @PostMapping("/delete")
    public Map<String, Object> deleteImage(@RequestParam Integer representativeBannerId, @RequestParam String deleteFlag,
                                           HttpSession session) {
        bannerService.clearImage(representativeBannerId, deleteFlag, manager(session).getUserId());
        return Map.of("isSuccess", true);
    }

    private void putFormModel(Model model, RepresentativeBanner banner, Manager manager) {
        model.addAttribute("edit", banner != null);
        model.addAttribute("banner", banner);
        model.addAttribute("hasImagePc", banner != null && hasText(banner.getFileNamePc()));
        model.addAttribute("hasImageMobile", banner != null && hasText(banner.getFileNameMobile()));
        model.addAttribute("adminRole", AdminRole.of(manager));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
