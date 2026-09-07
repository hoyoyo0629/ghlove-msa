package com.ghlove.donation.web;

import com.ghlove.donation.domain.NtsReceiptLog;
import com.ghlove.donation.repository.NtsReceiptLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** admin 콘솔 "연계 로그 관리 - 기부금영수증 국세청연계" 화면이 부르는 cross-service API -
 * 다른 admin 전용 API와 동일하게 별도 인증 없이 열려있다(admin 콘솔에서만 호출되는 내부용). */
@RestController
@RequestMapping("/api/nts-receipt-logs")
@RequiredArgsConstructor
public class NtsReceiptLogApiController {

    private final NtsReceiptLogRepository repository;

    public record Row(Long logSn, String cntrSn, String logType, String requestDate,
                       String responseCode, String responseMessage, String processStatus) {
    }

    @GetMapping
    public List<Row> list(@RequestParam String logType) {
        return repository.findByLogTypeOrderByLogSnDesc(logType).stream()
                .map(l -> new Row(l.getLogSn(), l.getCntrSn(), l.getLogType(),
                        l.getRequestDate() != null ? l.getRequestDate().toString() : null,
                        l.getResponseCode(), l.getResponseMessage(), l.getProcessStatus()))
                .toList();
    }
}
