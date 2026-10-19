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
 * 오프라인기부(기탁서) 접수 (AS-IS opmanager/offgive, form.jsp 1,995줄). 담당자가 은행 창구/
 * 행정복지센터에서 현장 접수한 기부를 등록한다 - 실제 도메인효과(원장 기록)는 이미 있는
 * {@link DonationService#registerOfflineDonation}을 재사용한다.
 *
 * <p><b>2단계 상태(2026-10-12 전환)</b>: AS-IS는 등록 시 {@code cntrSttusCode='100'}(신고)로만
 * 저장하고, 실제 {@code '200'}(수납완료) 전환은 세외수입시스템 수납이력을 폴링하는 별도
 * 배치({@code NgDonationBatchServiceImpl.cntrSunapBatch}등)가 비동기로 처리한다 - 등록
 * 즉시완료가 아니다. TO-BE도 같은 배치(fiveMinuteSunapCheckBatch·dayCompleteSunapCheckBatch, DonationBatchJobs)가
 * 수납확인 후 처리한다(2026-10-15 이식, 그 전의 관리자 수동 "수납처리" 버튼은 제거). 취소/변경은 이미 있는
 * give-reqmng 워크플로를 그대로 쓴다(CntrReqmngService).
 *
 * <p><b>전자납부번호(elctrnPayNo)</b>는 AS-IS form.jsp가 등록 제출 <b>전에</b> 먼저
 * {@code nextBugaRequest}/{@code getSdonationCharge}(세외수입 부과요청)로 받아와 hidden
 * field에 채운 뒤 최종 POST에 실어 보낸다 - 이 서비스는 그 값을 그대로 받아 저장할 뿐,
 * 자체적으로 부과요청을 하지 않는다(그 호출은 admin 쪽 LevyRelayClient가 등록 전에 한다).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OffgiveService {

    private final DonationService donationService;
    private final DonationRepository donationRepository;
    private final MemberClient memberClient;
    private final FileStorageService fileStorageService;
    private final OrderClient orderClient;
    private final com.ghlove.donation.repository.LevyLinkLogWriteRepository levyLinkLogWriteRepository;

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

    /** 등록 시 받는 전체 필드 - AS-IS form.jsp가 {@code #offgive}로 직렬화해 POST하는 값들. */
    public record RegisterRequest(
            Long userId, String userName, String jumin1, String jumin2, String mberCi, String mberDi,
            String phoneNumber, String address, String addressDetail,
            String psitnLocgovCode, String locgovCode, BigDecimal amount, Long dsgnDntnBizId,
            String rtnpsntReqstCode, String rcvUserName, String rcvPhoneNumber,
            String rcvAddress, String rcvAddressDetail, List<GiftLine> giftOrders,
            String rceptBankCode, String rceptBankNm, String rcepterNm,
            String elctrnPayNo, String signatureImage, String birthday, String rcvPost,
            String locgovUpperCode, String linkMngKey,
            // AS-IS insertOffgive 즉석가입 USER_DETAIL.POST·GENDER
            String post, String gender) {
    }

    /** AS-IS 서울시 시도코드 - 기탁서 등록 시 서울(세외수입 서울시) 분기 기준. */
    private static final String SEOUL_UPPER_CODE = "11000";
    private static final java.time.format.DateTimeFormatter YMD = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");

    public record GiftLine(Long itemId, Long itemOptionId, Integer quantity) {
    }

    /**
     * 방문 시민 접수 - userId가 없으면 CI(mberCi)로 기존회원을 먼저 찾고(실명인증은 admin이
     * 이미 끝낸 상태), 그래도 없으면 즉석 가입시킨다(walkIn* 파라미터, AS-IS sciResponse의
     * user-check가 실패했을 때와 동일). 생년월일은 주민등록번호 앞 7자리에서 역산한다
     * (AS-IS {@code getDateOfBirth}).
     */
    @Transactional
    public Result register(RegisterRequest req) {
        MemberClient.WalkInResult walkIn = null;
        Long targetUserId = req.userId();
        if (targetUserId == null) {
            // AS-IS goSave()가 주민번호로 계산해 hidden #birthday에 넣어 보낸다(주민번호 칸은 disabled라 안 넘어옴).
            String birthday = req.birthday() != null && !req.birthday().isBlank()
                    ? req.birthday() : birthdayOf(req.jumin1(), req.jumin2());
            walkIn = memberClient.registerWalkIn(req.userName(), req.phoneNumber(), birthday, req.address(),
                    req.mberCi(), req.mberDi(), req.post(), req.addressDetail(), req.gender(), req.psitnLocgovCode());
            targetUserId = walkIn.userId();
        }

        // AS-IS insertOffgive - 서울(시도 11000)은 연계관리키를 지금 새로 채번해 기부일련번호로 쓰고(서울대상 Y,
        // 납부유효일 오늘+1), 그 외는 화면이 부과요청 응답에서 받아 온 연계관리키(#linkMngKey)를 그대로 쓴다
        // (서울대상 N, 납부유효일 오늘). 수납 확인·미납 삭제 배치가 이 키로 기부와 부과건을 맞춘다.
        // AS-IS는 서울 외인데 연계관리키가 비면 빈 문자열을 기부일련번호로 넣는다(두 번째부터 PK 충돌) -
        // 그때는 같은 규칙으로 새로 채번한다.
        boolean seoul = SEOUL_UPPER_CODE.equals(req.locgovUpperCode());
        String cntrSn = seoul || req.linkMngKey() == null || req.linkMngKey().isBlank()
                ? levyLinkLogWriteRepository.nextLinkMngKey() : req.linkMngKey();
        java.time.LocalDate today = java.time.LocalDate.now();

        Donation donation = donationService.registerOfflineDonation(cntrSn, targetUserId, req.locgovCode(),
                req.amount(), req.rceptBankCode(), req.rceptBankNm(), req.dsgnDntnBizId(), req.psitnLocgovCode(),
                req.rtnpsntReqstCode());
        donation.setSeoulTrgetAt(seoul ? "Y" : "N");
        donation.setPayValidDe((seoul ? today.plusDays(1) : today).format(YMD));
        donation.setElctrnPayNo(req.elctrnPayNo());
        donation.setRcepterNm(req.rcepterNm());

        String signatureFileNm = storeSignature(req.signatureImage());
        if (signatureFileNm != null) {
            donation.setSignatureFileNm(signatureFileNm);
        }
        donation = donationRepository.save(donation);

        // AS-IS insertOffgive - orderItems가 있으면 saveOffGiveOrder(답례품 '제공받지않음'이면 form.jsp
        // goSave()가 orderItems를 비워 보낸다). 주문은 '입금대기'로 만들어지고 수납완료 뒤 결제처리된다.
        if (req.giftOrders() != null && !req.giftOrders().isEmpty()) {
            try {
                orderClient.createOffgiveOrder(targetUserId, donation.getCntrSn(), req.giftOrders(),
                        req.rcvUserName(), req.rcvPhoneNumber(), req.rcvAddress(), req.rcvAddressDetail(),
                        req.rcvPost());
            } catch (RuntimeException e) {
                // AS-IS도 saveOffGiveOrder 예외를 로그만 남기고 삼킨다(insertOffgive error2) - 기부 등록은 유지.
                log.warn("Offgive gift order failed for cntrSn={} - donation registration still succeeds",
                        donation.getCntrSn(), e);
            }
        }

        return new Result(donation, walkIn);
    }

    /** AS-IS getDateOfBirth - 주민번호 뒷자리 첫 숫자로 세기(18/19/20) 판정. */
    private static String birthdayOf(String jumin1, String jumin2) {
        if (jumin1 == null || jumin1.length() < 6 || jumin2 == null || jumin2.isEmpty()) {
            return null;
        }
        char division = jumin2.charAt(0);
        String century = switch (division) {
            case '1', '2', '5', '6' -> "19";
            case '3', '4', '7', '8' -> "20";
            default -> "18";
        };
        return century + jumin1;
    }

    /** AS-IS MagicLineController(매직라인 전자서명 연계, signedFormR)를 대체하는 순수 프론트
     *  canvas 서명패드 캡처본 - "data:image/png;base64,...." 형태의 data URL로 받아 PNG 바이트만
     *  디코드해 저장한다. 서명은 법적 필수 요소가 아니므로(단순 UX 증빙) 실패해도 접수 자체는
     *  막지 않고 경고만 남긴다. */
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
