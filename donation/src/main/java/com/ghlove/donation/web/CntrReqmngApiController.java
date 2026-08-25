package com.ghlove.donation.web;

import com.ghlove.donation.domain.CntrReqmng;
import com.ghlove.donation.service.CntrReqmngService;
import com.ghlove.donation.service.DonationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** 기부금 변경신청 관리 - admin 서비스(운영관리 콘솔)가 호출하는 cross-service API.
 *  CtbnyOpratnApiController와 동일한 이유로 별도 인증 없이 열려있다(admin의 OP_MANAGER
 *  로그인이 실제 게이트, 이 API 자체는 브라우저에 직접 노출되지 않음). */
@RestController
@RequiredArgsConstructor
public class CntrReqmngApiController {

    private final CntrReqmngService cntrReqmngService;

    @GetMapping("/api/cntr-reqmng")
    public List<CntrReqmngDto> list(@RequestParam(required = false) String locgovCode) {
        List<CntrReqmng> list = (locgovCode == null || locgovCode.isBlank())
                ? cntrReqmngService.listAll()
                : cntrReqmngService.listByLocgov(locgovCode);
        return list.stream().map(CntrReqmngDto::of).toList();
    }

    @GetMapping("/api/cntr-reqmng/donation/{cntrSn}")
    public ResponseEntity<?> donationInfo(@PathVariable String cntrSn) {
        try {
            var donation = cntrReqmngService.donationOf(cntrSn);
            boolean usedPoints = cntrReqmngService.hasUsedPoints(cntrSn);
            return ResponseEntity.ok(new DonationInfoDto(donation.getCntrSn(), donation.getCntrDe(),
                    donation.getUserId(), donation.getCntrLocgovCode(), donation.getCntrAmt(),
                    donation.getCntrSttusCode(), usedPoints));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/cntr-reqmng")
    public ResponseEntity<?> submit(@RequestParam String cntrSn, @RequestParam String cntrReqmngCode,
                                     @RequestParam String discription,
                                     @RequestParam(required = false) String taxSysCancelDe,
                                     @RequestParam(required = false) String relatedDocDptNm,
                                     @RequestParam(required = false) String relatedDocNum,
                                     @RequestParam(required = false) String relatedDocDe,
                                     @RequestParam Long managerId) {
        try {
            CntrReqmng saved = cntrReqmngService.submit(cntrSn, cntrReqmngCode, discription,
                    parseDate(taxSysCancelDe), relatedDocDptNm, relatedDocNum, parseDate(relatedDocDe), managerId);
            return ResponseEntity.ok(CntrReqmngDto.of(saved));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/cntr-reqmng/{reqId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long reqId, @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(CntrReqmngDto.of(cntrReqmngService.approve(reqId, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/cntr-reqmng/{reqId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long reqId, @RequestParam Long managerId) {
        try {
            return ResponseEntity.ok(CntrReqmngDto.of(cntrReqmngService.cancel(reqId, managerId)));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private static LocalDateTime parseDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            return null;
        }
        return LocalDate.parse(yyyyMMdd, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    public record CntrReqmngDto(Long reqId, String loginId, String userName, String locgovCode,
                                 String sttemntPayDe, BigDecimal cntrAmt, String cntrReqmngCode,
                                 String discription, Long frstRegisterId, String frstRegistPnttm,
                                 String reqStatusCode, String cntrSn, String apprDt, String cancleDt) {
        static CntrReqmngDto of(CntrReqmng e) {
            return new CntrReqmngDto(e.getReqId(), e.getLoginId(), e.getUserName(), e.getLocgovCode(),
                    isoOrNull(e.getSttemntPayDe()), e.getCntrAmt(), e.getCntrReqmngCode(), e.getDiscription(),
                    e.getFrstRegisterId(), isoOrNull(e.getFrstRegistPnttm()), e.getReqStatusCode(), e.getCntrSn(),
                    isoOrNull(e.getApprDt()), isoOrNull(e.getCancleDt()));
        }

        private static String isoOrNull(LocalDateTime dt) {
            return dt != null ? dt.toString() : null;
        }
    }

    public record DonationInfoDto(String cntrSn, String cntrDe, Long userId, String locgovCode,
                                   BigDecimal cntrAmt, String cntrSttusCode, boolean pointsUsed) {
    }
}
