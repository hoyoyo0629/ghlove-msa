package com.ghlove.donation.web;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.MemberClient;
import com.ghlove.donation.service.OffgiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 오프라인기부 접수 - admin 서비스가 호출하는 cross-service API. CtbnyOpratnApiController와
 *  동일한 이유로 별도 인증 없이 열려있다(admin의 OP_MANAGER 로그인이 실제 게이트). */
@RestController
@RequiredArgsConstructor
public class OffgiveApiController {

    private final OffgiveService offgiveService;
    private final com.ghlove.donation.repository.OffgiveAdminRepository offgiveAdminRepository;
    private final com.ghlove.donation.repository.OffgiveFormRepository offgiveFormRepository;
    private final com.ghlove.donation.repository.LocgovRepository locgovRepository;
    private final com.ghlove.donation.repository.UnpaidDonationBatchRepository unpaidDonationBatchRepository;
    private final DonationService donationService;
    private final com.ghlove.donation.service.PointClient pointClient;

    /**
     * AS-IS selectDesignatedDonationList(FRONT/모금중/DONATION) - 기탁서 화면의 사업 선택
     * ({@code getDesignatedDonationList}, locgovCode 지정)과 상단 "지정기부목록 다운로드"
     * ({@code list/prj/download-excel}, 전 지자체·sort=BSNS)가 같이 쓴다.
     */
    @GetMapping("/api/offgive/designated-donation-list")
    public List<com.ghlove.donation.repository.OffgiveFormRepository.DesignatedRow> designatedDonationList(
            @RequestParam(required = false) String locgovCode, @RequestParam(required = false) String sort) {
        return offgiveFormRepository.designatedDonationList(locgovCode, sort);
    }

    /** AS-IS give-state options-by-locgovCode - 시도의 시군구 목록({LOCGOV_CODE, LOCGOV_NM}). */
    @GetMapping("/api/offgive/locgov-options")
    public List<Map<String, Object>> locgovOptions(@RequestParam String code) {
        return offgiveFormRepository.locgovOptions(code);
    }

    /** AS-IS getLocGovLmtt - 기부제한 중이면 {violtResnCn, lmttBgnDe, lmttEndDe}, 아니면 204. */
    @GetMapping("/api/offgive/locgov-lmtt")
    public ResponseEntity<Map<String, Object>> locgovLmtt(@RequestParam String cityCode) {
        Map<String, Object> lmtt = offgiveFormRepository.cntrLmtt(cityCode);
        return lmtt == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(lmtt);
    }

    /** AS-IS getLocgovMapngCode - 행정기관코드 → 고향사랑 지자체코드, 없으면 204. */
    @GetMapping("/api/offgive/locgov-mapng-code")
    public ResponseEntity<Map<String, Object>> locgovMapngCode(@RequestParam String locgovCode) {
        Map<String, Object> mapng = offgiveFormRepository.locgovMapngCode(locgovCode);
        return mapng == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(mapng);
    }

    /**
     * AS-IS convertAdmCdToLocgovCode(selectLocgovByAdmCd) - 행정정보공동이용센터 외국인 거소조회 응답의
     * 행정기관코드 앞 5자리를 고향사랑 지자체코드로 바꾼다(G_ADM_LOCGOV), 없으면 204.
     */
    @GetMapping("/api/offgive/adm-locgov-code")
    public ResponseEntity<Map<String, Object>> admLocgovCode(@RequestParam String admCd) {
        String code = offgiveFormRepository.locgovCodeByAdmCd(admCd);
        return code == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(Map.of("locgovCode", code));
    }

    /** AS-IS offgive/view/{locgovCode}(locgovService.getLocgovDetails) - 화면은 bassAdres만 쓴다. */
    @GetMapping("/api/offgive/locgov-view/{locgovCode}")
    public ResponseEntity<Map<String, Object>> locgovView(@PathVariable String locgovCode) {
        return locgovRepository.findById(locgovCode)
                .map(l -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("bassAdres", l.getBassAdres());
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** AS-IS maxCheck - 개인 연간 잔여한도. 회원이 없으면(즉석가입 전) 전체한도 그대로. */
    @GetMapping("/api/offgive/limit")
    public BigDecimal limit(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return donationService.annualLimit().orElse(BigDecimal.ZERO);
        }
        return donationService.remainingAnnualLimit(userId);
    }

    @GetMapping("/api/offgive")
    public List<DonationDto> list() {
        return offgiveService.list().stream().map(DonationDto::of).toList();
    }

    /** AS-IS offgive/list.jsp 검색조건(지자체/접수일자 범위) 재현. */
    @GetMapping("/api/offgive/search")
    public List<DonationDto> search(@RequestParam(required = false) String locgovCode,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate) {
        return offgiveService.search(locgovCode, startDate, endDate).stream().map(DonationDto::of).toList();
    }

    /**
     * 기부금 접수관리 목록 (admin 메뉴 15101) - AS-IS {@code POST /opmanager/offgive/list}의
     * 검색조건 전체(검색구분·신청일·금액범위·기부상태·소속지점)와 11컬럼을 그대로 돌려준다.
     * 이름은 회원(member) 소유라 USER_ID만 내려주고 admin이 채운다.
     */
    @GetMapping("/api/admin/offgive/search")
    public List<com.ghlove.donation.repository.OffgiveAdminRepository.Row> adminSearch(
            @RequestParam(required = false) String shKeyword,
            @RequestParam(required = false) String shText,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) String cntrSttusCode,
            @RequestParam(required = false) String rceptBankCode,
            @RequestParam(required = false) String rceptBankNm,
            @RequestParam(required = false) String role) {
        return offgiveAdminRepository.search(shKeyword, shText, startDate, endDate, amountFrom, amountTo,
                cntrSttusCode, rceptBankCode, rceptBankNm, role);
    }

    /**
     * 기탁서 상세(AS-IS {@code GET /detail/{id}} getOffgive) - g_cntr 부분({@link
     * com.ghlove.donation.repository.OffgiveAdminRepository#detail}) + 포인트. AS-IS CNTR_POINT·CNTR_USE_POINT·
     * CNTR_POINT-CNTR_USE_POINT 자리는 TO-BE 포인트 원장의 적립분(적립액·잔액)으로 채운다(적립 전이면 0).
     */
    @GetMapping("/api/offgive/{cntrSn}/detail")
    public ResponseEntity<?> detail(@PathVariable String cntrSn) {
        return offgiveAdminRepository.detail(cntrSn)
                .<ResponseEntity<?>>map(d -> {
                    com.ghlove.donation.service.PointClient.EarnLot lot = pointClient.earnLotOf(cntrSn);
                    long cntrPoint = lot != null && lot.earnedAmount() != null ? lot.earnedAmount() : 0L;
                    long cntrBlcePoint = lot != null && lot.remainingAmount() != null ? lot.remainingAmount() : 0L;
                    return ResponseEntity.ok(new OffgiveDetailDto(d, cntrPoint, cntrPoint - cntrBlcePoint, cntrBlcePoint));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("message", "기부내역을 찾을 수 없습니다.")));
    }

    public record OffgiveDetailDto(com.ghlove.donation.repository.OffgiveAdminRepository.Detail offgive,
                                   long cntrPoint, long cntrUsePoint, long cntrBlcePoint) {
    }

    @GetMapping("/api/offgive/{cntrSn}")
    public ResponseEntity<?> get(@PathVariable String cntrSn) {
        try {
            return ResponseEntity.ok(DonationDto.of(offgiveService.findOrThrow(cntrSn)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/offgive")
    public ResponseEntity<?> register(@RequestBody OffgiveService.RegisterRequest req) {
        try {
            OffgiveService.Result result = offgiveService.register(req);
            return ResponseEntity.ok(new RegisterResultDto(DonationDto.of(result.donation()),
                    result.walkIn() != null ? WalkInDto.of(result.walkIn()) : null));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * AS-IS offgive-mapper selectGiveSunapCheck - 주어진 기부일련번호 중 미납으로 삭제된 오프라인 기부만 돌려준다.
     * order의 "오프라인기부, 답례품 주문시 미수납된 답례품 상태 업데이트" 배치(AS-IS allDayOffItemUpdateBatch)가
     * 입금대기 답례품의 기부일련번호를 모아 한 번에 묻는다.
     */
    @PostMapping("/api/offgive/deleted-unpaid")
    public List<String> deletedUnpaid(@RequestBody List<String> cntrSns) {
        return unpaidDonationBatchRepository.deletedUnpaidOffline(cntrSns);
    }

    /** 수납처리(AS-IS 수납확인 배치 대체 - 관리자 수동 트리거). */
    @PostMapping("/api/offgive/{cntrSn}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String cntrSn) {
        try {
            return ResponseEntity.ok(DonationDto.of(offgiveService.confirm(cntrSn)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DonationDto(String cntrSn, String cntrDe, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                               String cntrSttusCode, String cntrPathCode, String rceptBankCode, String rceptBankNm,
                               String rcepterNm, String elctrnPayNo, String psitnLocgovCode, String rtnpsntReqstCode,
                               Long dsgnDntnBizId, String frstRegistPnttm, String signatureFileNm) {
        static DonationDto of(Donation d) {
            return new DonationDto(d.getCntrSn(), d.getCntrDe(), d.getUserId(), d.getCntrLocgovCode(),
                    d.getCntrAmt(), d.getCntrSttusCode(), d.getCntrPathCode(), d.getRceptBankCode(),
                    d.getRceptBankNm(), d.getRcepterNm(), d.getElctrnPayNo(), d.getPsitnLocgovCode(),
                    d.getRtnpsntReqstCode(), d.getDsgnDntnBizId(), d.getFrstRegistPnttm(), d.getSignatureFileNm());
        }
    }

    public record WalkInDto(Long userId, String loginId, String tempPassword) {
        static WalkInDto of(MemberClient.WalkInResult w) {
            return new WalkInDto(w.userId(), w.loginId(), w.tempPassword());
        }
    }

    public record RegisterResultDto(DonationDto donation, WalkInDto walkIn) {
    }
}
