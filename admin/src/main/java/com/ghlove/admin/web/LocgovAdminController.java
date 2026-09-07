package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovAdminClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.PointClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 지자체 마스터관리 (AS-IS opmanager/user/locgov - LocgovManagerController). 이전에는 admin
 * 콘솔에 이 화면 자체가 없어(as-is-feature-audit 라운드에서 확인된 gap, 배치 D의 D1) donation
 * 서비스 안에 인증 없는 급조 화면(LocgovSealAdminController, "/admin/locgov-seals" - 직인
 * 업로드만)으로 임시 대체돼 있었다. 이 컨트롤러가 그 기능을 정식 화면의 "직인" 섹션으로
 * 완전히 흡수하고, 담당자정보/주소/예산/포인트지급률/부서/기부금모금제한/답례품 배경이미지
 * 까지 AS-IS 필드 전체를 다룬다.
 *
 * RBAC은 offgive와 동일한 패턴: SYS(ROLE_ADMIN_1~4)는 전체 조회+등록/삭제, LOC(ROLE_ADMIN_5/6)
 * 는 자기 지자체 1건만 조회/수정 가능(AS-IS의 "2. 지자체 관리자인 경우 등록 또는 수정페이지로
 * 이동" 분기와 동일).
 *
 * 포인트 지급률은 donation이 아니라 point 서비스(PT_LOCGOV_POINT_RATE)에 쓴다 - donation의
 * G_CTBNY_SETUP.POINT_RATE는 시드 데이터만 있고 실제 적립 계산(PointService#currentPointRateOf)
 * 이 읽는 진짜 테이블이 아니라는 걸 확인했다(자세한 내용은 LocgovAdminClient/PointClient 주석).
 */
@Controller
@RequestMapping("/admin/locgovs")
@RequiredArgsConstructor
public class LocgovAdminController {

    private final LocgovAdminClient locgovAdminClient;
    private final PointClient pointClient;

    private static final int PAGE_SIZE = 20;

    @GetMapping
    public String list(@RequestParam(required = false) String locgovNm,
                        @RequestParam(required = false) String chargerNm,
                        @RequestParam(required = false) String chargerCttpc,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") int page,
                        HttpSession session, Model model) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer)) {
            // AS-IS: 지자체 담당자는 목록이 아니라 바로 자기 지자체의 등록/수정 화면으로 간다.
            return "redirect:/admin/locgovs/" + viewer.getLocgovCode();
        }

        var result = locgovAdminClient.search(null, locgovNm, chargerNm, chargerCttpc, startDate, endDate, page,
                PAGE_SIZE);
        model.addAttribute("result", result);
        model.addAttribute("locgovNm", locgovNm);
        model.addAttribute("chargerNm", chargerNm);
        model.addAttribute("chargerCttpc", chargerCttpc);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentYear", String.valueOf(LocalDate.now().getYear()));
        return "locgov-admin/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer)) {
            return "redirect:/admin/locgovs/" + viewer.getLocgovCode();
        }
        model.addAttribute("mode", "create");
        model.addAttribute("registrable", locgovAdminClient.registrable());
        return "locgov-admin/form";
    }

    @GetMapping("/{locgovCode}")
    public String detail(@PathVariable String locgovCode, HttpSession session, Model model) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer) && !locgovCode.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/locgovs/" + viewer.getLocgovCode();
        }
        try {
            var detail = locgovAdminClient.get(locgovCode);
            boolean registered = detail.bizrno() != null && !detail.bizrno().isBlank();
            model.addAttribute("mode", registered ? "edit" : "create-existing");
            model.addAttribute("detail", detail);
            model.addAttribute("deptHist", locgovAdminClient.deptHist(locgovCode));
            model.addAttribute("lmttList", locgovAdminClient.lmttList(locgovCode));
            model.addAttribute("pointRateHistory", pointClient.locgovPointRateHistory(locgovCode));
            model.addAttribute("currentYear", String.valueOf(LocalDate.now().getYear()));
            model.addAttribute("locgovScoped", MenuService.isLocgovScoped(viewer));
            model.addAttribute("honorBenefit", locgovAdminClient.honorBenefit(locgovCode));
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "locgov-admin/form";
    }

    /** 지자체별 기부혜택 안내문구 (SFR-003 "지자체별 기부혜택 관리") - donation 자체에 무인증으로
     *  노출돼 있던 /honor/benefit를 여기 지자체관리 화면으로 흡수했다. */
    @PostMapping("/{locgovCode}/honor-benefit")
    public String updateHonorBenefit(@PathVariable String locgovCode, @RequestParam String benefitDesc,
                                      HttpSession session) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer) && !locgovCode.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/locgovs/" + viewer.getLocgovCode();
        }
        locgovAdminClient.updateHonorBenefit(locgovCode, benefitDesc);
        return "redirect:/admin/locgovs/" + locgovCode;
    }

    @PostMapping("/{locgovCode}/register")
    public String register(@PathVariable String locgovCode, @ModelAttribute LocgovFormRequest form,
                            @RequestParam(required = false) MultipartFile offcsFile,
                            @RequestParam(required = false) MultipartFile addPcFile,
                            @RequestParam(required = false) MultipartFile addMbFile,
                            HttpSession session, Model model) {
        Manager viewer = manager(session);
        try {
            locgovAdminClient.register(locgovCode, form.toFields(), offcsFile, addPcFile, addMbFile);
            registerPointRate(locgovCode, form, viewer);
            return "redirect:/admin/locgovs/" + locgovCode;
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("mode", "create");
            model.addAttribute("registrable", locgovAdminClient.registrable());
            return "locgov-admin/form";
        }
    }

    @PostMapping("/{locgovCode}")
    public String update(@PathVariable String locgovCode, @ModelAttribute LocgovFormRequest form,
                          @RequestParam(required = false) MultipartFile offcsFile,
                          @RequestParam(required = false) MultipartFile addPcFile,
                          @RequestParam(required = false) MultipartFile addMbFile,
                          HttpSession session, Model model) {
        Manager viewer = manager(session);
        if (MenuService.isLocgovScoped(viewer) && !locgovCode.equals(viewer.getLocgovCode())) {
            return "redirect:/admin/locgovs/" + viewer.getLocgovCode();
        }
        try {
            locgovAdminClient.update(locgovCode, form.toFields(), offcsFile, addPcFile, addMbFile);
            registerPointRate(locgovCode, form, viewer);
            return "redirect:/admin/locgovs/" + locgovCode;
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("mode", "edit");
            model.addAttribute("detail", locgovAdminClient.get(locgovCode));
            model.addAttribute("deptHist", locgovAdminClient.deptHist(locgovCode));
            model.addAttribute("lmttList", locgovAdminClient.lmttList(locgovCode));
            model.addAttribute("pointRateHistory", pointClient.locgovPointRateHistory(locgovCode));
            model.addAttribute("currentYear", String.valueOf(LocalDate.now().getYear()));
            model.addAttribute("locgovScoped", MenuService.isLocgovScoped(viewer));
            model.addAttribute("honorBenefit", locgovAdminClient.honorBenefit(locgovCode));
            return "locgov-admin/form";
        }
    }

    @PostMapping("/{locgovCode}/delete")
    public String deactivate(@PathVariable String locgovCode, HttpSession session) {
        Manager viewer = manager(session);
        if (!MenuService.isLocgovScoped(viewer)) {
            locgovAdminClient.deactivate(locgovCode, viewer.getUserId());
        }
        return "redirect:/admin/locgovs";
    }

    /** 포인트 지급률 등록/수정 - AS-IS user/locgov/edit.jsp "포인트 지급률" 항목. */
    @PostMapping("/{locgovCode}/point-rate")
    public String upsertPointRate(@PathVariable String locgovCode, @RequestParam String stdrYear,
                                   @RequestParam BigDecimal pointRate, HttpSession session, Model model) {
        Manager viewer = manager(session);
        try {
            pointClient.upsertLocgovPointRate(stdrYear, locgovCode, pointRate, viewer.getUserName());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("mode", "edit");
            model.addAttribute("detail", locgovAdminClient.get(locgovCode));
            model.addAttribute("deptHist", locgovAdminClient.deptHist(locgovCode));
            model.addAttribute("lmttList", locgovAdminClient.lmttList(locgovCode));
            model.addAttribute("pointRateHistory", pointClient.locgovPointRateHistory(locgovCode));
            model.addAttribute("currentYear", String.valueOf(LocalDate.now().getYear()));
            model.addAttribute("locgovScoped", MenuService.isLocgovScoped(viewer));
            model.addAttribute("honorBenefit", locgovAdminClient.honorBenefit(locgovCode));
            return "locgov-admin/form";
        }
        return "redirect:/admin/locgovs/" + locgovCode;
    }

    /** 기부금모금제한(기간+사유) 등록 - AS-IS G_CNTR_LMTT. */
    @PostMapping("/{locgovCode}/lmtt")
    public String createLmtt(@PathVariable String locgovCode, @RequestParam String lmttBgnDe,
                              @RequestParam String lmttEndDe, @RequestParam String violtResnCode,
                              @RequestParam(required = false) String violtResnCn, HttpSession session, Model model) {
        Manager viewer = manager(session);
        try {
            locgovAdminClient.createLmtt(locgovCode, stripDashes(lmttBgnDe), stripDashes(lmttEndDe), violtResnCode,
                    violtResnCn, viewer.getUserName(), viewer.getUserId());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/locgovs/" + locgovCode;
    }

    @PostMapping("/{locgovCode}/lmtt/delete")
    public String deleteLmtt(@PathVariable String locgovCode, @RequestParam String lmttBgnDe,
                              @RequestParam String lmttEndDe) {
        locgovAdminClient.deleteLmtt(locgovCode, lmttBgnDe, lmttEndDe);
        return "redirect:/admin/locgovs/" + locgovCode;
    }

    /** 직인 미리보기 - donation의 암호화 저장소에서 복호화된 바이트를 그대로 스트리밍한다
     *  (폐기된 LocgovSealAdminController#preview 대체). */
    @GetMapping(value = "/{locgovCode}/seal", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> seal(@PathVariable String locgovCode) {
        byte[] bytes = locgovAdminClient.sealBytes(locgovCode);
        return bytes != null ? ResponseEntity.ok(bytes) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{locgovCode}/image/{type}")
    @ResponseBody
    public ResponseEntity<byte[]> image(@PathVariable String locgovCode, @PathVariable String type) {
        byte[] bytes = locgovAdminClient.imageBytes(locgovCode, type);
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{locgovCode}/image/{type}/delete")
    public String deleteImage(@PathVariable String locgovCode, @PathVariable String type, HttpSession session) {
        locgovAdminClient.deleteImage(locgovCode, type, manager(session).getUserId());
        return "redirect:/admin/locgovs/" + locgovCode;
    }

    private void registerPointRate(String locgovCode, LocgovFormRequest form, Manager viewer) {
        if (form.pointRate() != null) {
            pointClient.upsertLocgovPointRate(String.valueOf(LocalDate.now().getYear()), locgovCode,
                    form.pointRate(), viewer.getUserName());
        }
    }

    private static String stripDashes(String ymd) {
        return ymd != null ? ymd.replace("-", "") : null;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    /** AS-IS setParam()의 formData와 1:1 대응(사업자번호/연락처는 AS-IS처럼 3분할 입력받지
     *  않고 단일 텍스트로 받는다 - 데이터 모델은 동일, 화면 입력만 단순화). multipart 필드는
     *  컨트롤러 파라미터로 직접 받지 않고 이 폼 객체를 통해 한 번에 클라이언트로 넘긴다. */
    public record LocgovFormRequest(String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                                     String locgovHmpg, String locgovIntrcnCn, String locgovZip, String bassAdres,
                                     String dtlAdres, Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                                     String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                                     String chargerPsitnDept, String processDeptCode, String administInsttCode,
                                     String fisSp, String offcsNm, BigDecimal pointRate) {
        Map<String, Object> toFields() {
            Map<String, Object> m = new HashMap<>();
            m.put("bizrno", bizrno);
            m.put("chargerNm", chargerNm);
            m.put("chargerCttpc", chargerCttpc);
            m.put("chargerEmail", chargerEmail);
            m.put("locgovHmpg", locgovHmpg);
            m.put("locgovIntrcnCn", locgovIntrcnCn);
            m.put("locgovZip", locgovZip);
            m.put("bassAdres", bassAdres);
            m.put("dtlAdres", dtlAdres);
            m.put("locgovBudgetAmt", locgovBudgetAmt);
            m.put("locgovPopltnCo", locgovPopltnCo);
            m.put("locgovAr", locgovAr);
            m.put("locgovSpcprd", locgovSpcprd);
            m.put("gcctUseAt", nvl(gcctUseAt, "N"));
            m.put("etrcshUseAt", nvl(etrcshUseAt, "N"));
            m.put("achlqrSleAt", nvl(achlqrSleAt, "N"));
            m.put("chargerPsitnDept", chargerPsitnDept);
            m.put("processDeptCode", processDeptCode);
            m.put("administInsttCode", administInsttCode);
            m.put("fisSp", fisSp);
            m.put("offcsNm", offcsNm);
            return m;
        }

        private static String nvl(String v, String def) {
            return (v == null || v.isBlank()) ? def : v;
        }
    }
}
