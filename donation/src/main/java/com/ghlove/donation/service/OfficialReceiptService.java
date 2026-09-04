package com.ghlove.donation.service;

import com.ghlove.donation.domain.CntrReceiptLog;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.DonationLevy;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.CntrReceiptLogRepository;
import com.ghlove.donation.repository.DonationLevyRepository;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.LocgovRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 기부금영수증(단건, 공식) 출력 - AS-IS mypage/cntrList.html의 "영수증 출력" →
 * receiptPrint.html(OZReport `3.cntr_receipt_new.ozr`)에 해당. 다건을 모아 보여주는
 * "확인증"(ReceiptService/certificate*)과는 별개의 AS-IS 화면이다.
 *
 * AS-IS는 OZ Report Server가 CNTR_SN만 받아 서버 내부에서 나머지 필드를 직접 조회해
 * 렌더링했지만(리포트 템플릿 자체는 상용 OZ Studio로 저작되어 원문 레이아웃을 알 수
 * 없음), 이 서비스는 같은 정보(성명/생년월일/기부지자체/기부금액/기부일자/전자납부번호/
 * 발급번호/직인)를 동일한 소스 테이블에서 직접 조합해 SSR로 재현한다.
 */
@Service
@RequiredArgsConstructor
public class OfficialReceiptService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final DateTimeFormatter RAW_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DonationRepository donationRepository;
    private final LocgovRepository locgovRepository;
    private final DonationLevyRepository donationLevyRepository;
    private final CntrReceiptLogRepository cntrReceiptLogRepository;
    private final LocgovSealService locgovSealService;
    private final MemberClient memberClient;

    public OfficialReceipt build(Long userId, String cntrSn) {
        Donation donation = ownedCompletedDonation(userId, cntrSn);
        Locgov locgov = locgovRepository.findById(donation.getCntrLocgovCode()).orElse(null);
        DonationLevy levy = donationLevyRepository.findById(cntrSn).orElse(null);
        MemberInfo member = memberClient.fetch(userId);
        String sealImageDataUri = locgov != null
                ? locgovSealService.sealDataUri(locgov.getLocgovCode()).orElse(null)
                : null;

        String locgovDisplay = locgov != null
                ? (nullToEmpty(locgov.getUpperLocgovNm()) + " " + nullToEmpty(locgov.getLocgovNm())).trim()
                : "";
        long issuedBefore = cntrReceiptLogRepository.countByCntrSn(cntrSn);

        return new OfficialReceipt(
                member.userName(),
                formatDisplayDate(member.birthday()),
                cntrSn,
                formatDisplayDate(donation.getCntrDe()),
                locgovDisplay,
                locgov != null ? locgov.getBizrno() : null,
                donation.getCntrAmt(),
                levy != null ? levy.getBugaNo() : null,
                formatDisplayDate(RAW_DATE.format(LocalDate.now())),
                (issuedBefore + 1),
                sealImageDataUri,
                locgov != null ? locgov.getOffcsNm() : null
        );
    }

    /** AS-IS OZPrintCommand_OZViewer(code=="0") 콜백 - 실제 인쇄가 이뤄졌을 때만 이력 1행 추가. */
    @Transactional
    public void logPrint(Long userId, String cntrSn) {
        ownedCompletedDonation(userId, cntrSn); // 소유권 재검증
        DonationLevy levy = donationLevyRepository.findById(cntrSn).orElse(null);

        CntrReceiptLog log = new CntrReceiptLog();
        log.setCntrOutptSn(cntrReceiptLogRepository.maxCntrOutptSn() + 1);
        log.setCntrSn(cntrSn);
        log.setElctrnPayNo(levy != null ? levy.getBugaNo() : null);
        log.setIssuDe(RAW_DATE.format(LocalDate.now()));
        log.setFrstRegisterId(userId);
        log.setFrstRegistPnttm(java.time.LocalDateTime.now());
        log.setLastUpdusrId(userId);
        log.setLastUpdtPnttm(java.time.LocalDateTime.now());
        cntrReceiptLogRepository.save(log);
    }

    private Donation ownedCompletedDonation(Long userId, String cntrSn) {
        List<Donation> found = donationRepository.findByCntrSnInAndUserIdAndCntrSttusCode(
                List.of(cntrSn), userId, STATUS_COMPLETED);
        if (found.isEmpty()) {
            throw new DonationException("영수증 정보가 없습니다.");
        }
        return found.get(0);
    }

    private String formatDisplayDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() != 8) {
            return "";
        }
        LocalDate date = LocalDate.parse(yyyyMMdd, RAW_DATE);
        return date.getYear() + "." + date.getMonthValue() + "." + date.getDayOfMonth() + ".";
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public record OfficialReceipt(
            String userName,
            String birthdayDisplay,
            String cntrSn,
            String cntrDeDisplay,
            String locgovDisplay,
            String bizrno,
            java.math.BigDecimal cntrAmt,
            String elctrnPayNo,
            String issueDateDisplay,
            long issueCount,
            String sealImageDataUri,
            String offcsNm
    ) {}
}
