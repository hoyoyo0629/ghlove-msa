package com.ghlove.donation.web;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.service.DonationException;
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

    @GetMapping("/api/offgive/{cntrSn}")
    public ResponseEntity<?> get(@PathVariable String cntrSn) {
        try {
            return ResponseEntity.ok(DonationDto.of(offgiveService.findOrThrow(cntrSn)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/offgive")
    public ResponseEntity<?> register(@RequestParam(required = false) Long userId,
                                       @RequestParam(required = false) String walkInName,
                                       @RequestParam(required = false) String walkInPhone,
                                       @RequestParam(required = false) String walkInBirthday,
                                       @RequestParam(required = false) String walkInAddress,
                                       @RequestParam String locgovCode, @RequestParam BigDecimal amount,
                                       @RequestParam(required = false) String rceptBankCode,
                                       @RequestParam(required = false) String rceptBankNm) {
        try {
            OffgiveService.Result result = offgiveService.register(userId, walkInName, walkInPhone, walkInBirthday,
                    walkInAddress, locgovCode, amount, rceptBankCode, rceptBankNm);
            return ResponseEntity.ok(new RegisterResultDto(DonationDto.of(result.donation()),
                    result.walkIn() != null ? WalkInDto.of(result.walkIn()) : null));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DonationDto(String cntrSn, String cntrDe, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                               String cntrSttusCode, String cntrPathCode, String rceptBankCode, String rceptBankNm,
                               String frstRegistPnttm) {
        static DonationDto of(Donation d) {
            return new DonationDto(d.getCntrSn(), d.getCntrDe(), d.getUserId(), d.getCntrLocgovCode(),
                    d.getCntrAmt(), d.getCntrSttusCode(), d.getCntrPathCode(), d.getRceptBankCode(),
                    d.getRceptBankNm(), d.getFrstRegistPnttm());
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
