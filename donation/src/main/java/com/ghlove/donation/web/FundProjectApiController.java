package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.LocgovMapProvince;
import com.ghlove.donation.service.NoticeClient;
import com.ghlove.donation.service.PointClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** storefront(Vue3 SPA)용 "기금사업 소개"(안내사항 &gt; list-select) JSON API -
 *  {@link FundProjectController}(Thymeleaf list-select.html)와 완전히 같은
 *  {@link DonationService}/{@link PointClient}/{@link NoticeClient} 로직을 재사용한다.
 *  AS-IS/Thymeleaf 버전은 서버가 조각(fragment) HTML을 내려주지만, 이 SPA는 진짜 Vue라
 *  하나의 JSON 응답(fund)으로 지자체정보/기금사업소개/지자체공지사항 탭 데이터를 한번에
 *  내려주고 탭 전환은 클라이언트에서만 처리한다. */
@RestController
@RequiredArgsConstructor
public class FundProjectApiController {

    private final LocgovRepository locgovRepository;
    private final DonationService donationService;
    private final PointClient pointClient;
    private final NoticeClient noticeClient;

    private static final int PAGE_SIZE = 5;

    public record ProvinceDto(String code, String name) {
    }

    @GetMapping("/api/list-select/provinces")
    public List<ProvinceDto> provinces() {
        return LocgovMapProvince.ALL.stream().map(p -> new ProvinceDto(p.code(), p.name())).toList();
    }

    public record CityDto(String locgovCode, String locgovNm) {
    }

    @GetMapping("/api/list-select/cities")
    public List<CityDto> cities(@RequestParam String upperLocgovCode) {
        return locgovRepository.findByUseAtOrderByLocgovNm("Y").stream()
                .filter(l -> upperLocgovCode.equals(l.getUpperLocgovCode()))
                .map(l -> new CityDto(l.getLocgovCode(), l.getLocgovNm()))
                .toList();
    }

    public record FundLocgovDto(String locgovCode, String locgovNm, String upperLocgovNm, String displayBudgetAmt,
                                 String displayPopltnCo, String chargerNm, String chargerCttpc, String chargerPsitnDept,
                                 String locgovHmpg, String locgovIntrcnCn, java.math.BigDecimal pointRate, String giftMallUrl) {
    }

    public record ProjectRowDto(Long dsgnDntnBizId, String dsgnDntnBizTtl, String dateText) {
    }

    public record NoticeRowDto(Integer noticeId, String subject, String createdDate) {
    }

    public record FundContentResponse(FundLocgovDto locgov, List<ProjectRowDto> projects, int projectCurrentPage,
                                       int projectTotalPages, List<NoticeRowDto> notices, int noticeCurrentPage,
                                       int noticeTotalPages) {
    }

    @GetMapping("/api/list-select/fund")
    public FundContentResponse fund(@RequestParam String locgovCode,
                                     @RequestParam(required = false, defaultValue = "1") int projectPage,
                                     @RequestParam(required = false, defaultValue = "1") int noticePage) {
        Locgov locgov = locgovRepository.findById(locgovCode).orElse(null);
        if (locgov == null) {
            return null;
        }
        FundLocgovDto locgovDto = new FundLocgovDto(locgov.getLocgovCode(), locgov.getLocgovNm(), locgov.getUpperLocgovNm(),
                formatNumber(locgov.getLocgovBudgetAmt()), formatNumber(locgov.getLocgovPopltnCo()),
                locgov.getChargerNm(), locgov.getChargerCttpc(), locgov.getChargerPsitnDept(), locgov.getLocgovHmpg(),
                locgov.getLocgovIntrcnCn(), pointClient.pointRateOf(locgovCode), "http://localhost:8084/?locgovCode=" + locgovCode);

        List<DesignatedProject> allProjects = donationService.designatedProjects(null, null, "LATEST", null, locgovCode);
        int projectTotalPages = (int) Math.ceil(allProjects.size() / (double) PAGE_SIZE);
        int projectCurrentPage = Math.max(1, Math.min(projectPage, Math.max(projectTotalPages, 1)));
        int pFrom = Math.min((projectCurrentPage - 1) * PAGE_SIZE, allProjects.size());
        int pTo = Math.min(pFrom + PAGE_SIZE, allProjects.size());
        List<ProjectRowDto> projects = allProjects.subList(pFrom, pTo).stream()
                .map(p -> new ProjectRowDto(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl(), formatYmd(p.getDsgnDntnBizBgngYmd())))
                .toList();

        List<NoticeClient.NoticeSummary> allNotices = noticeClient.noticesByLocgov(locgovCode);
        int noticeTotalPages = (int) Math.ceil(allNotices.size() / (double) PAGE_SIZE);
        int noticeCurrentPage = Math.max(1, Math.min(noticePage, Math.max(noticeTotalPages, 1)));
        int nFrom = Math.min((noticeCurrentPage - 1) * PAGE_SIZE, allNotices.size());
        int nTo = Math.min(nFrom + PAGE_SIZE, allNotices.size());
        List<NoticeRowDto> notices = allNotices.subList(nFrom, nTo).stream()
                .map(n -> new NoticeRowDto(n.noticeId(), n.subject(), n.createdDate()))
                .toList();

        return new FundContentResponse(locgovDto, projects, projectCurrentPage, projectTotalPages,
                notices, noticeCurrentPage, noticeTotalPages);
    }

    private static String formatYmd(String ymd) {
        if (ymd == null || ymd.length() < 8) {
            return "";
        }
        return ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6, 8);
    }

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
