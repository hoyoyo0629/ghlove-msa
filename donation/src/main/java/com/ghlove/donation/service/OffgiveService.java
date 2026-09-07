package com.ghlove.donation.service;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

/**
 * 오프라인기부(기탁서) 접수 (AS-IS opmanager/offgive). 접수 담당자가 은행 창구/행정복지
 * 센터에서 현장 접수한 기부를 등록한다 - 실제 도메인효과(원장 기록, 세외수입/포인트
 * 연계)는 이미 있는 DonationService.registerOfflineDonation()/completeDonation()을 그대로
 * 재사용한다(offgive 전용 로직은 방문 시민 즉석가입뿐). AS-IS는 등록 즉시 반영되고 별도
 * 승인단계가 없다(OffgiveServiceImpl.insertOffgive 확인) - 취소/변경은 이미 있는
 * give-reqmng 워크플로를 그대로 쓴다(CntrReqmngService, 새로 만들지 않음).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OffgiveService {

    private final DonationService donationService;
    private final DonationRepository donationRepository;
    private final MemberClient memberClient;
    private final FileStorageService fileStorageService;

    private static final String SIGNATURE_SUBDIR = "offgive-signature";

    /** AS-IS 공통코드 CNTR_PATH 실제값(09.공통코드 목록.xlsx) - 200:오프라인. */
    private static final String PATH_OFFLINE = "200";

    public List<Donation> list() {
        return donationRepository.findByCntrPathCodeOrderByFrstRegistPnttmDesc(PATH_OFFLINE);
    }

    /** AS-IS offgive/list.jsp의 검색조건(지자체/접수일자 범위) 재현 - startDate/endDate는
     *  "yyyyMMdd" 문자열, 비어있으면 전체기간으로 취급한다. */
    public List<Donation> search(String locgovCode, String startDate, String endDate) {
        String start = (startDate == null || startDate.isBlank()) ? "00000000" : startDate;
        String end = (endDate == null || endDate.isBlank()) ? "99999999" : endDate;
        String locgov = (locgovCode == null || locgovCode.isBlank()) ? null : locgovCode;
        return donationRepository.searchOffline(PATH_OFFLINE, locgov, start, end);
    }

    public Donation findOrThrow(String cntrSn) {
        Donation donation = donationRepository.findById(cntrSn)
                .orElseThrow(() -> new DonationException("기부내역을 찾을 수 없습니다."));
        if (!PATH_OFFLINE.equals(donation.getCntrPathCode())) {
            throw new DonationException("오프라인 접수 기부가 아닙니다.");
        }
        return donation;
    }

    /**
     * 방문 시민 접수 - userId가 없으면 즉석 가입시킨 뒤(walkIn* 파라미터) 그 계정으로
     * 등록한다. 현장에서 이미 현금/계좌이체로 접수받은 것이므로 등록과 동시에 완료
     * 처리한다(REQUESTED를 거치지 않음 - AS-IS도 등록 즉시 반영).
     */
    @Transactional
    public Result register(Long userId, String walkInName, String walkInPhone, String walkInBirthday,
                            String walkInAddress, String locgovCode, BigDecimal amount,
                            String rceptBankCode, String rceptBankNm, String signatureImage) {
        MemberClient.WalkInResult walkIn = null;
        Long targetUserId = userId;
        if (targetUserId == null) {
            walkIn = memberClient.registerWalkIn(walkInName, walkInPhone, walkInBirthday, walkInAddress);
            targetUserId = walkIn.userId();
        }

        Donation donation = donationService.registerOfflineDonation(targetUserId, locgovCode, amount, rceptBankCode, rceptBankNm);
        Donation completed = donationService.completeDonation(donation.getCntrSn());

        String signatureFileNm = storeSignature(signatureImage);
        if (signatureFileNm != null) {
            completed.setSignatureFileNm(signatureFileNm);
            completed = donationRepository.save(completed);
        }
        return new Result(completed, walkIn);
    }

    /** AS-IS MagicLineController(매직라인 전자서명 연계)를 대체하는 순수 프론트 canvas 서명패드
     *  캡처본 - "data:image/png;base64,...." 형태의 data URL로 받아 PNG 바이트만 디코드해
     *  저장한다. 서명은 법적 필수 요소가 아니므로(단순 UX 증빙) 실패해도 접수 자체는 막지
     *  않고 경고만 남긴다. */
    private String storeSignature(String signatureImage) {
        if (signatureImage == null || signatureImage.isBlank()) {
            return null;
        }
        try {
            String base64 = signatureImage;
            int comma = signatureImage.indexOf(',');
            if (signatureImage.startsWith("data:") && comma >= 0) {
                base64 = signatureImage.substring(comma + 1);
            }
            byte[] data = Base64.getDecoder().decode(base64);
            return fileStorageService.storeBytes(data, ".png", SIGNATURE_SUBDIR);
        } catch (RuntimeException e) {
            log.warn("Failed to store offgive signature capture - registration proceeds without it", e);
            return null;
        }
    }

    public record Result(Donation donation, MemberClient.WalkInResult walkIn) {
    }
}
