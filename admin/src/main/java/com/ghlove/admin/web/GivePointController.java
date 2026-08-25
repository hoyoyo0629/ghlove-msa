package com.ghlove.admin.web;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.service.GiveStateService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.service.PointClient;
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

/** 기부포인트 현황 (AS-IS opmanager/give/give-point). give-state와 같은 년도×지자체
 *  ReadModel을 재사용하되(GiveStateService), 포인트잔액(발생-사용) 컬럼을 더 보여준다.
 *  상세화면은 건별(납부번호=CNTR_SN) 발생/사용/잔액을 보여주는데, 이 값은 point 서비스의
 *  FIFO lot(REMAINING_AMOUNT)을 그대로 읽어야 정확하다 - 포인트 사용은 개별 기부건과
 *  무관하게 지자체 단위로 소비되지만, lot 자체는 어느 기부에서 났는지(REF_KEY=CNTR_SN)
 *  추적되므로 "이 기부로 생긴 포인트 중 얼마가 남았는지"는 정확히 계산 가능하다. */
@Controller
@RequiredArgsConstructor
public class GivePointController {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final GiveStateService giveStateService;
    private final DonationLedgerRepository donationLedgerRepository;
    private final LocgovClient locgovClient;
    private final MemberClient memberClient;
    private final PointClient pointClient;

    @GetMapping("/give-point")
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

        long totalPoint = rows.stream().mapToLong(GiveStateService.GiveStateRow::cntrPoint).sum();
        long totalUsePoint = rows.stream().mapToLong(GiveStateService.GiveStateRow::cntrUsePoint).sum();
        model.addAttribute("totalAmt", rows.stream().map(GiveStateService.GiveStateRow::cntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("totalPoint", totalPoint);
        model.addAttribute("totalUsePoint", totalUsePoint);
        model.addAttribute("totalBlcePoint", totalPoint - totalUsePoint);
        return "give/give-point-list";
    }

    @GetMapping("/give-point/detail")
    public String detail(@RequestParam String cntrYear, @RequestParam String upperLocgovCode,
                          @RequestParam String locgovCode,
                          @RequestParam(required = false) String shUserName,
                          Model model) {
        List<DonationLedger> yearList = completedByLocgov(locgovCode).stream()
                .filter(d -> d.getEventDate() != null && d.getEventDate().startsWith(cntrYear))
                .toList();

        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (DonationLedger d : yearList) {
            memberByUserId.computeIfAbsent(d.getUserId(), memberClient::fetchOrNull);
        }

        List<DonationLedger> filtered = yearList.stream()
                .filter(d -> matchesName(memberByUserId.get(d.getUserId()), shUserName))
                .sorted(Comparator.comparing(DonationLedger::getEventDate).reversed())
                .toList();

        Map<String, PointClient.EarnLot> lotByCntrSn = new LinkedHashMap<>();
        pointClient.earnedLots(filtered.stream().map(DonationLedger::getCntrSn).toList())
                .forEach(lot -> lotByCntrSn.put(lot.cntrSn(), lot));

        List<Row> rows = filtered.stream().map(d -> {
            PointClient.EarnLot lot = lotByCntrSn.get(d.getCntrSn());
            long earned = lot != null && lot.earnedAmount() != null ? lot.earnedAmount() : 0L;
            long remaining = lot != null && lot.remainingAmount() != null ? lot.remainingAmount() : 0L;
            MemberClient.MemberInfo member = memberByUserId.get(d.getUserId());
            return new Row(d.getCntrSn(), d.getEventDate(), member != null ? member.userName() : "-",
                    member != null ? member.loginId() : "-", d.getAmount(), earned, earned - remaining, remaining);
        }).toList();

        long yearEarned = rows.stream().mapToLong(Row::earnedPoint).sum();
        long yearUsed = rows.stream().mapToLong(Row::usedPoint).sum();
        long yearBalance = rows.stream().mapToLong(Row::blcePoint).sum();

        List<DonationLedger> allYears = completedByLocgov(locgovCode);
        List<PointClient.EarnLot> allLots = pointClient.earnedLots(allYears.stream().map(DonationLedger::getCntrSn).toList());
        long totalEarned = allLots.stream().mapToLong(l -> l.earnedAmount() != null ? l.earnedAmount() : 0L).sum();
        long totalRemaining = allLots.stream().mapToLong(l -> l.remainingAmount() != null ? l.remainingAmount() : 0L).sum();

        LocgovClient.LocgovInfo info = locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode())).findFirst().orElse(null);

        model.addAttribute("list", rows);
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("shUserName", shUserName);
        model.addAttribute("locgovFullNm", info != null ? info.upperLocgovNm() + " " + info.locgovNm() : locgovCode);
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("yearEarned", yearEarned);
        model.addAttribute("yearUsed", yearUsed);
        model.addAttribute("yearBalance", yearBalance);
        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalUsed", totalEarned - totalRemaining);
        model.addAttribute("totalBalance", totalRemaining);
        return "give/give-point-detail";
    }

    private List<DonationLedger> completedByLocgov(String locgovCode) {
        return donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> locgovCode.equals(d.getLocgovCode()))
                .toList();
    }

    private static boolean matchesName(MemberClient.MemberInfo member, String shUserName) {
        if (shUserName == null || shUserName.isBlank()) {
            return true;
        }
        return member != null && member.userName() != null && member.userName().contains(shUserName.trim());
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    public record Row(String cntrSn, String eventDate, String userName, String loginId,
                       BigDecimal cntrAmt, long earnedPoint, long usedPoint, long blcePoint) {
    }
}
