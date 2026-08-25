package com.ghlove.admin.web;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.service.GiveStateService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MemberClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 기부금 모금현황 (AS-IS opmanager/give/give-state/list.jsp, SFR-007 "지자체별 실적 분석"). */
@Controller
@RequiredArgsConstructor
public class GiveStateController {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final GiveStateService giveStateService;
    private final DonationLedgerRepository donationLedgerRepository;
    private final LocgovClient locgovClient;
    private final MemberClient memberClient;

    @GetMapping("/give-state")
    public String list(@RequestParam(required = false) String cntrYear,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        Model model) {
        List<GiveStateService.GiveStateRow> rows = giveStateService.search(cntrYear, upperLocgovCode, locgovCode);
        model.addAttribute("rows", rows);
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);

        BigDecimal totalAmt = rows.stream().map(GiveStateService.GiveStateRow::cntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalCnt = rows.stream().mapToLong(GiveStateService.GiveStateRow::giveCnt).sum();
        long totalPersons = rows.stream().mapToLong(GiveStateService.GiveStateRow::givePersons).sum();
        long totalPoint = rows.stream().mapToLong(GiveStateService.GiveStateRow::cntrPoint).sum();
        long totalUsePoint = rows.stream().mapToLong(GiveStateService.GiveStateRow::cntrUsePoint).sum();
        model.addAttribute("totalAmt", totalAmt);
        model.addAttribute("totalCnt", totalCnt);
        model.addAttribute("totalPersons", totalPersons);
        model.addAttribute("totalPoint", totalPoint);
        model.addAttribute("totalUsePoint", totalUsePoint);
        return "give/give-state-list";
    }

    /** AS-IS list.jsp의 detail() 클릭 - 해당 연도×지자체의 개별 기부 건 목록. */
    @GetMapping("/give-state/detail")
    public String detail(@RequestParam String cntrYear, @RequestParam String upperLocgovCode,
                          @RequestParam String locgovCode, Model model) {
        List<DonationLedger> list = donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> locgovCode.equals(d.getLocgovCode())
                        && d.getEventDate() != null && d.getEventDate().startsWith(cntrYear))
                .sorted(Comparator.comparing(DonationLedger::getEventDate).reversed())
                .toList();

        LocgovClient.LocgovInfo info = locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode()))
                .findFirst().orElse(null);

        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (DonationLedger d : list) {
            memberByUserId.computeIfAbsent(d.getUserId(), memberClient::fetchOrNull);
        }

        model.addAttribute("list", list);
        model.addAttribute("memberByUserId", memberByUserId);
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("locgovFullNm", info != null ? info.upperLocgovNm() + " " + info.locgovNm() : locgovCode);
        return "give/give-state-detail";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}
