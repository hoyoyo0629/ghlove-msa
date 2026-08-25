package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.LocgovMapProvince;
import com.ghlove.donation.service.NoticeClient;
import com.ghlove.donation.service.PointClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 안내사항 &gt; 기금사업 소개 (AS-IS donation/list-select.html). AS-IS는 Vue SPA라 시·도/
 * 시·군·구/탭/페이지네이션을 클릭해도 전체 페이지가 다시 로드되지 않고 필요한 영역만
 * 갱신된다 - 이를 재현하려고 진입 시점(딥링크·새로고침)에는 전체 페이지를 서버에서
 * 렌더링하고(list()), 그 이후 모든 상호작용(시·도 변경, 지자체 선택, 탭 전환, 페이지네이션)은
 * 순수 JS fetch로 필요한 조각(fragment)만 다시 받아 innerHTML로 교체한다(cities()/
 * content()). "기금사업소개" 탭은 별도 스키마 없이 이미 있는 특정사업기부
 * (DesignatedProject) 목록을 locgovCode로 재사용한다. "지자체공지사항" 탭은 admin
 * 서비스의 OP_NOTICE를 LOCGOV_CODE로 필터링해 크로스서비스로 조회한다(NoticeClient).
 */
@Controller
@RequiredArgsConstructor
public class FundProjectController {

    private final LocgovRepository locgovRepository;
    private final DonationService donationService;
    private final PointClient pointClient;
    private final NoticeClient noticeClient;

    private static final int PAGE_SIZE = 5;

    @GetMapping("/list-select")
    public String listSelect(@RequestParam(required = false) String upperLocgovCode,
                              @RequestParam(required = false) String locgovCode,
                              @RequestParam(required = false, defaultValue = "tab1") String tab,
                              @RequestParam(required = false, defaultValue = "1") int projectPage,
                              @RequestParam(required = false, defaultValue = "1") int noticePage,
                              Model model) {
        populateModel(model, upperLocgovCode, locgovCode, tab, projectPage, noticePage);
        return "guide/list-select";
    }

    /** 시·도 변경 시 fetch로 호출 - 시·군·구 그리드 조각만 돌려준다. */
    @GetMapping("/list-select/cities")
    public String cities(@RequestParam(required = false) String upperLocgovCode, Model model) {
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", null);
        if (upperLocgovCode != null && !upperLocgovCode.isBlank()) {
            model.addAttribute("cities", citiesOf(upperLocgovCode));
        }
        return "guide/list-select :: citiesFragment";
    }

    /** 지자체 선택·탭 전환(딥링크 포함이 아닌 새로고침 없는 페이지네이션)·페이지네이션 시
     *  fetch로 호출 - 우측 탭 콘텐츠 조각만 돌려준다. */
    @GetMapping("/list-select/content")
    public String content(@RequestParam String upperLocgovCode,
                           @RequestParam String locgovCode,
                           @RequestParam(required = false, defaultValue = "tab1") String tab,
                           @RequestParam(required = false, defaultValue = "1") int projectPage,
                           @RequestParam(required = false, defaultValue = "1") int noticePage,
                           Model model) {
        populateModel(model, upperLocgovCode, locgovCode, tab, projectPage, noticePage);
        return "guide/list-select :: contentFragment";
    }

    private void populateModel(Model model, String upperLocgovCode, String locgovCode, String tab,
                                int projectPage, int noticePage) {
        model.addAttribute("provinces", LocgovMapProvince.ALL);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("tab", tab);

        if (upperLocgovCode != null && !upperLocgovCode.isBlank()) {
            model.addAttribute("cities", citiesOf(upperLocgovCode));
        }

        if (locgovCode != null && !locgovCode.isBlank()) {
            locgovRepository.findById(locgovCode).ifPresent(locgov -> {
                model.addAttribute("locgov", locgov);
                model.addAttribute("displayBudgetAmt", formatNumber(locgov.getLocgovBudgetAmt()));
                model.addAttribute("displayPopltnCo", formatNumber(locgov.getLocgovPopltnCo()));
                model.addAttribute("pointRate", pointClient.pointRateOf(locgovCode));
                model.addAttribute("giftMallUrl", "http://localhost:8084/?locgovCode=" + locgovCode);

                List<DesignatedProject> allProjects = donationService.designatedProjects(null, null, "LATEST", null, locgovCode);
                addPage(model, allProjects, projectPage, "project");
                Map<Long, String> projectDisplayDates = new HashMap<>();
                allProjects.forEach(p -> projectDisplayDates.put(p.getDsgnDntnBizId(), formatYmd(p.getDsgnDntnBizBgngYmd())));
                model.addAttribute("projectDisplayDates", projectDisplayDates);

                List<NoticeClient.NoticeSummary> allNotices = noticeClient.noticesByLocgov(locgovCode);
                addPage(model, allNotices, noticePage, "notice");
            });
        }
    }

    private List<Locgov> citiesOf(String upperLocgovCode) {
        return locgovRepository.findByUseAtOrderByLocgovNm("Y").stream()
                .filter(l -> upperLocgovCode.equals(l.getUpperLocgovCode()))
                .toList();
    }

    private void addPage(Model model, List<?> all, int page, String prefix) {
        int totalPages = (int) Math.ceil(all.size() / (double) PAGE_SIZE);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * PAGE_SIZE, all.size());
        int to = Math.min(from + PAGE_SIZE, all.size());
        model.addAttribute(prefix + "Items", all.subList(from, to));
        model.addAttribute(prefix + "CurrentPage", currentPage);
        model.addAttribute(prefix + "TotalPages", totalPages);
    }

    /** dsgnDntnBizBgngYmd는 yyyyMMdd 문자열이라 화면 표시용으로 yyyy-MM-dd로 바꾼다. */
    private static String formatYmd(String ymd) {
        if (ymd == null || ymd.length() < 8) {
            return "";
        }
        return ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6, 8);
    }

    /** locgovBudgetAmt/locgovPopltnCo는 각각 Long/String(레거시 컬럼 원형)이라 공통으로
     *  콤마 포맷을 태우려면 문자열로 통일해 숫자만 파싱해야 한다. */
    private static String formatNumber(Object value) {
        if (value == null) {
            return "";
        }
        try {
            long n = Long.parseLong(value.toString().trim());
            return NumberFormat.getNumberInstance(Locale.KOREA).format(n);
        } catch (NumberFormatException e) {
            return value.toString();
        }
    }
}
