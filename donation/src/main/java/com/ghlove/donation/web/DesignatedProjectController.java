package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.LocgovMapProvince;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AS-IS designated-donation/index-main.html·details.html 대응 - GNB "기부 > 특정사업에
 * 기부하기"의 진입점. 운영중인 홈페이지(ilovegohyang.go.kr)를 직접 확인해 기본 정렬
 * (모금율순)·페이지당 12건·사업구분 라벨을 그대로 맞췄다. 배너 슬라이더(현재 운영중인
 * 홈페이지에도 배너가 없는 상태라 이번 스코프에서도 제외)와 지도 기반 지자체 선택
 * 팝업(mall-header의 것과 별개 컴포넌트), 상세 페이지의 이미지 갤러리·응원메시지·
 * 공지사항 탭·SNS 공유는 이번 스코프에서 제외한다(사용자 확인).
 */
@Controller
@RequiredArgsConstructor
public class DesignatedProjectController {

    private final DonationService donationService;
    private static final int PAGE_SIZE = 12;

    @GetMapping("/designated-donation")
    public String list(@RequestParam(required = false, defaultValue = "OPEN") String status,
                        @RequestParam(required = false) String bsnsType,
                        @RequestParam(required = false, defaultValue = "RATE") String sort,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String locgovCode,
                        Model model) {
        var allProjects = donationService.designatedProjects(status, bsnsType, sort, q, locgovCode);

        int totalPages = (int) Math.ceil(allProjects.size() / (double) PAGE_SIZE);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int fromIndex = Math.min((currentPage - 1) * PAGE_SIZE, allProjects.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allProjects.size());
        List<DesignatedProject> projects = allProjects.subList(fromIndex, toIndex);

        Map<String, Locgov> locgovsByCode = locgovsByCode();
        model.addAttribute("projects", projects);
        model.addAttribute("locgovsByCode", locgovsByCode);
        model.addAttribute("status", status);
        model.addAttribute("bsnsType", bsnsType);
        model.addAttribute("sort", sort);
        model.addAttribute("q", q);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("selectedLocgov", locgovCode != null ? locgovsByCode.get(locgovCode) : null);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("bsnsTypes", donationService.codesOf("DSGN_BSNS_TYPE"));
        model.addAttribute("mapProvinces", LocgovMapProvince.ALL);
        model.addAttribute("locgovsByProvince", donationService.activeLocgovs().stream()
                .collect(Collectors.groupingBy(Locgov::getUpperLocgovCode,
                        Collectors.collectingAndThen(
                                Collectors.toMap(Locgov::getLocgovNm, Function.identity(), (a, b) -> a,
                                        LinkedHashMap::new),
                                m -> List.copyOf(m.values())))));

        Map<Long, BigDecimal> raisedAmounts = new LinkedHashMap<>();
        Map<Long, BigDecimal> rates = new LinkedHashMap<>();
        Map<Long, String> gaugeClasses = new LinkedHashMap<>();
        Map<Long, Boolean> overGoal = new LinkedHashMap<>();
        for (DesignatedProject p : projects) {
            raisedAmounts.put(p.getDsgnDntnBizId(), donationService.raisedAmount(p.getDsgnDntnBizId()));
            BigDecimal rate = donationService.fundingRatePercent(p);
            rates.put(p.getDsgnDntnBizId(), rate);
            gaugeClasses.put(p.getDsgnDntnBizId(), gaugeClassOf(rate));
            overGoal.put(p.getDsgnDntnBizId(), rate.compareTo(HUNDRED) > 0);
        }
        model.addAttribute("raisedAmounts", raisedAmounts);
        model.addAttribute("rates", rates);
        model.addAttribute("gaugeClasses", gaugeClasses);
        model.addAttribute("overGoal", overGoal);
        return "designated-list";
    }

    @GetMapping("/designated-donation/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) String error, Model model) {
        var project = donationService.findProject(id).orElse(null);
        if (project == null) {
            return "redirect:/designated-donation";
        }
        BigDecimal rate = donationService.fundingRatePercent(project);
        model.addAttribute("project", project);
        model.addAttribute("locgov", locgovsByCode().get(project.getLclgvCd()));
        model.addAttribute("raisedAmount", donationService.raisedAmount(id));
        model.addAttribute("participantCount", donationService.participantCountOf(id));
        model.addAttribute("rate", rate);
        model.addAttribute("gaugeClass", gaugeClassOf(rate));
        model.addAttribute("overGoal", rate.compareTo(HUNDRED) > 0);
        model.addAttribute("errorMessage", error);
        model.addAttribute("periodText", formatYmd(project.getDsgnDntnBizBgngYmd()) + " ~ " + formatYmd(project.getDsgnDntnBizEndYmd()));
        model.addAttribute("cheerMessages", donationService.cheerMessagesOf(id));
        var notices = donationService.noticesOf(id);
        model.addAttribute("notices", notices);
        Map<Long, String> noticeDates = new LinkedHashMap<>();
        for (var n : notices) {
            String pnttm = n.getFrstRegistPnttm();
            noticeDates.put(n.getPrjNoticeId(), pnttm != null && pnttm.length() >= 8 ? formatYmd(pnttm.substring(0, 8)) : "");
        }
        model.addAttribute("noticeDates", noticeDates);
        return "designated-detail";
    }

    private Map<String, Locgov> locgovsByCode() {
        Map<String, Locgov> map = new LinkedHashMap<>();
        donationService.activeLocgovs().forEach(l -> map.put(l.getLocgovCode(), l));
        return map;
    }

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** SpEL의 T(java.math.BigDecimal).valueOf(100)은 long/double 오버로드가 모호해
     *  TemplateProcessingException을 던진다(응답 스트리밍이 이미 시작된 뒤라 청크가
     *  끊긴 채로 클라이언트에 전달됨) - 그래서 이 비교는 템플릿이 아니라 여기서 미리 한다. */
    private static String gaugeClassOf(BigDecimal rate) {
        int cmp = rate.compareTo(HUNDRED);
        return cmp < 0 ? "lt_goal" : (cmp == 0 ? "goal" : "gt_goal");
    }

    /** AS-IS는 상세화면의 기간을 "2025-12-26 ~ 2026-12-13"처럼 하이픈으로 표시한다
     *  (DB엔 YYYYMMDD로 저장되어 있음 - 목록 카드는 그대로 두고 상세만 맞춘다, 확인 완료). */
    private static String formatYmd(String ymd) {
        if (ymd == null || ymd.length() < 8) {
            return ymd;
        }
        return ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6, 8);
    }
}
