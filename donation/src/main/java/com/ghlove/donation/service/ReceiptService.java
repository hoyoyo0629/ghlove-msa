package com.ghlove.donation.service;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.domain.SpecialDisasterZone;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.SpecialDisasterZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기부확인증 (AS-IS 마이페이지 > 기부확인증, receiptList.html / getReceiptInitInfo /
 * getReceiptPopInfo). AS-IS는 Vue SPA 모달/새 탭 인쇄 방식이지만 이 서비스는 Thymeleaf
 * SSR이라, 목록 화면에서 체크한 기부건을 서버로 제출해 확인증 페이지를 렌더링하는
 * 방식으로 동일한 내용/서식을 재현한다 (필드/문구/집계 로직은 AS-IS와 동일).
 */
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final DateTimeFormatter RAW_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DonationRepository donationRepository;
    private final LocgovRepository locgovRepository;
    private final SpecialDisasterZoneRepository specialDisasterZoneRepository;
    private final MemberClient memberClient;

    /** AS-IS getReceiptListInfo + getReceiptCntrAmtAndTotalCnt. */
    public ReceiptListResult receiptList(Long userId, String locgovCode, String upperLocgovCode,
                                          String searchStartDate, String searchEndDate) {
        List<Donation> donations = donationRepository
                .findByUserIdAndCntrSttusCodeOrderByCntrDeDesc(userId, STATUS_COMPLETED);

        Map<String, Locgov> locgovs = locgovRepository.findAll().stream()
                .collect(Collectors.toMap(Locgov::getLocgovCode, l -> l));

        List<Donation> filtered = donations.stream()
                .filter(d -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(d.getCntrLocgovCode()))
                .filter(d -> upperLocgovCode == null || upperLocgovCode.isBlank()
                        || upperLocgovCode.equals(upperCodeOf(locgovs, d.getCntrLocgovCode())))
                .filter(d -> withinRange(d.getCntrDe(), searchStartDate, searchEndDate))
                .toList();

        List<ReceiptRow> rows = filtered.stream()
                .map(d -> {
                    Locgov l = locgovs.get(d.getCntrLocgovCode());
                    return new ReceiptRow(d.getCntrSn(), formatDisplayDate(d.getCntrDe()),
                            l != null ? l.getUpperLocgovNm() : null, l != null ? l.getLocgovNm() : null,
                            d.getCntrAmt());
                })
                .toList();

        BigDecimal total = filtered.stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReceiptListResult(rows, total, filtered.size());
    }

    /** AS-IS getReceiptCntrPopInfo + getReceiptPopListInfo + getTopLocGov, 회원 성명/생년월일은 member 서비스 조회. */
    public Certificate buildCertificate(Long userId, List<String> cntrSnList) {
        if (cntrSnList == null || cntrSnList.isEmpty()) {
            throw new DonationException("선택한 기부내역이 없습니다.\n선택한 기부내역만 기부확인증에 출력됩니다.");
        }

        // 본인 소유 + 완료 상태 건만 인정한다 (체크박스 목록은 사용자가 제출하는 값이라
        // AS-IS의 로그인 세션 기반 필터보다 한 겹의 소유권 검증을 추가한 것).
        List<Donation> donations = donationRepository
                .findByCntrSnInAndUserIdAndCntrSttusCode(cntrSnList, userId, STATUS_COMPLETED);
        if (donations.isEmpty()) {
            throw new DonationException("선택한 기부내역이 없습니다.\n선택한 기부내역만 기부확인증에 출력됩니다.");
        }

        Map<String, Locgov> locgovs = locgovRepository.findAll().stream()
                .collect(Collectors.toMap(Locgov::getLocgovCode, l -> l));
        Map<String, List<SpecialDisasterZone>> spelByLocgov = specialDisasterZoneRepository.findAll().stream()
                .filter(z -> z.getLocgovCode() != null)
                .collect(Collectors.groupingBy(SpecialDisasterZone::getLocgovCode));

        List<Donation> sorted = donations.stream()
                .sorted(Comparator.comparing(Donation::getCntrDe).reversed())
                .toList();

        List<ReceiptDetailRow> rows = sorted.stream()
                .map(d -> {
                    Locgov l = locgovs.get(d.getCntrLocgovCode());
                    return new ReceiptDetailRow(d.getCntrSn(), formatDisplayDate(d.getCntrDe()),
                            l != null ? l.getUpperLocgovNm() : null, l != null ? l.getLocgovNm() : null,
                            d.getCntrAmt(), l != null ? l.getBizrno() : null,
                            spelDstrYn(spelByLocgov.get(d.getCntrLocgovCode()), d.getCntrDe()));
                })
                .toList();

        BigDecimal totalCntrAmt = donations.stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
        String topLocGov = topLocGovDisplay(donations, locgovs);

        MemberInfo member = memberClient.fetch(userId);

        return new Certificate(member.userName(), formatDisplayDate(member.birthday()), topLocGov,
                totalCntrAmt, donations.size(), formatNowDate(LocalDate.now()), rows);
    }

    /** AS-IS: 선택 건 중 LOCGOV_CODE 오름차순으로 첫 지자체명 + (지자체가 2곳 이상이면) "등 N건". */
    private String topLocGovDisplay(List<Donation> donations, Map<String, Locgov> locgovs) {
        List<String> distinctCodes = donations.stream()
                .map(Donation::getCntrLocgovCode)
                .distinct()
                .sorted()
                .toList();
        if (distinctCodes.isEmpty()) {
            return "";
        }
        Locgov top = locgovs.get(distinctCodes.get(0));
        String name = top != null ? (nullToEmpty(top.getUpperLocgovNm()) + " " + nullToEmpty(top.getLocgovNm())).trim() : "";
        if (distinctCodes.size() > 1) {
            return name + " 등 " + (distinctCodes.size() - 1) + "건";
        }
        return name;
    }

    /** AS-IS getReceiptPopListInfo의 특별재난지역 판정: cntrDe가 [NOTI_DATE, END_DATE] 구간(문자열 비교, 포함) 안이면 Y. */
    private String spelDstrYn(List<SpecialDisasterZone> zones, String cntrDe) {
        if (zones == null || cntrDe == null) {
            return "N";
        }
        for (SpecialDisasterZone z : zones) {
            if (z.getNotiDate() == null || z.getEndDate() == null) {
                continue;
            }
            if (cntrDe.compareTo(z.getNotiDate()) >= 0 && cntrDe.compareTo(z.getEndDate()) <= 0) {
                return "Y";
            }
        }
        return "N";
    }

    private String upperCodeOf(Map<String, Locgov> locgovs, String locgovCode) {
        Locgov l = locgovs.get(locgovCode);
        return l != null ? l.getUpperLocgovCode() : null;
    }

    private boolean withinRange(String cntrDe, String start, String end) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return true;
        }
        // <input type=date>는 yyyy-MM-dd로 제출되는데 CNTR_DE는 yyyyMMdd로 저장돼 있어
        // 하이픈을 제거하지 않으면 문자열 비교가 어긋난다.
        String startRaw = start.replace("-", "");
        String endRaw = end.replace("-", "");
        return cntrDe != null && cntrDe.compareTo(startRaw) >= 0 && cntrDe.compareTo(endRaw) <= 0;
    }

    /** yyyyMMdd -> "yyyy.M.d." (legacy DATE_FORMAT '%Y.%c.%e.'와 동일한 표기, 앞자리 0 없음). */
    private String formatDisplayDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() != 8) {
            return "";
        }
        LocalDate date = LocalDate.parse(yyyyMMdd, RAW_DATE);
        return date.getYear() + "." + date.getMonthValue() + "." + date.getDayOfMonth() + ".";
    }

    /** "yyyy년 M월 d일" - 확인증 발급일 표기. */
    private String formatNowDate(LocalDate date) {
        return date.getYear() + "년 " + date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
