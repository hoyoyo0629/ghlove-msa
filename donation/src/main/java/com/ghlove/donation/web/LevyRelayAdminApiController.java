package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.LevyRelayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 납부(세외수입) 부과요청 연계 트리거 - AS-IS
 * {@code POST /opmanager/offgive/nextBugaRequest}(지방세외 차세대)와
 * {@code POST /opmanager/offgive/getSdonationCharge}(서울시 세외)에 대응한다.
 *
 * <p>AS-IS는 이 두 호출이 <b>기탁서 등록 화면(메뉴 15102) 안에서</b> 일어난다. 그 화면은 본인인증
 * 연계·권한(ROLE_ADMIN_11) 결정이 남아 보류 중이라, 연계 경로만 먼저 완결해 두고 여기로 직접
 * 트리거해 확인할 수 있게 했다 - 연계가 꺼져 있으면(기본값) AS-IS LOCAL 분기와 같은 가짜 응답을
 * 만들고 요청/응답을 연계로그 표에 남기므로 admin <b>1413 서울세외 부과연계 로그</b>·
 * <b>1415 지방세외 부과연계 로그</b> 화면에서 그대로 확인된다.
 *
 * <p>admin 콘솔 전용이고 브라우저에 직접 노출되지 않는다(다른 {@code /api/admin/**}과 같은 관행).
 */
@RestController
@RequestMapping("/api/admin/levy")
@RequiredArgsConstructor
public class LevyRelayAdminApiController {

    private final LevyRelayService levyRelayService;

    /** 지방세외수입 차세대 부과요청. */
    @PostMapping("/next-buga")
    public ResponseEntity<?> nextBuga(@RequestParam String locgovCode,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String pyrNo,
                                      @RequestParam(required = false) String pyrNm,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String prjId,
                                      @RequestParam(defaultValue = "200") String cntrPathCode,
                                      @RequestParam(required = false) String zip,
                                      @RequestParam(required = false) String roadNmDaddr) {
        try {
            LevyRelayService.BugaResult result = levyRelayService.requestNextBuga(locgovCode, userId,
                    pyrNo, pyrNm, amount, prjId, cntrPathCode, zip, roadNmDaddr);
            return ResponseEntity.ok(LevyRelayService.toMap(result));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 서울시 세외수입 부과정보 등록. AS-IS 화면이 보내는 값들(systemCd·semokCd·taxGubun·sidoCd·
     * napGubun·resideStatus·mulGubun·mulNm·sysGubun)은 그대로 받는다 - 서버가 채우는 값은
     * 서비스가 AS-IS대로 만든다.
     */
    @PostMapping("/seoul-buga")
    public ResponseEntity<?> seoulBuga(@RequestParam String locgovCode,
                                       @RequestParam(required = false) String napId,
                                       @RequestParam(required = false) String napNm,
                                       @RequestParam BigDecimal amount,
                                       @RequestParam(required = false) String systemCd,
                                       @RequestParam(required = false) String semokCd,
                                       @RequestParam(required = false) String taxGubun,
                                       @RequestParam(required = false) String sidoCd,
                                       @RequestParam(required = false) String napGubun,
                                       @RequestParam(required = false) String resideStatus,
                                       @RequestParam(required = false) String mulGubun,
                                       @RequestParam(required = false) String mulNm,
                                       @RequestParam(required = false) String sysGubun) {
        try {
            LevyRelayService.BugaResult result = levyRelayService.requestSeoulBuga(locgovCode, napId, napNm,
                    amount, systemCd, semokCd, taxGubun, sidoCd, napGubun, resideStatus, mulGubun, mulNm,
                    sysGubun);
            return ResponseEntity.ok(LevyRelayService.toMap(result));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
