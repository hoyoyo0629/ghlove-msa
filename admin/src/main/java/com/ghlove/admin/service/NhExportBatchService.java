package com.ghlove.admin.service;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.domain.NhExportLog;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.repository.NhExportLogRepository;
import com.ghlove.admin.service.integration.NhDataClient;
import com.ghlove.admin.service.integration.NhExportRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NH농협 데이터 연계 배치 (수동 트리거 - point의 소멸 배치와 같은 패턴).
 * admin은 donation 서비스 DB를 직접 읽을 수 없으므로(DB per Service), 이미 구축된
 * STAT_DONATION_LEDGER(통계 ReadModel)를 원천으로 삼아 아직 반출하지 않은
 * COMPLETED 기부 건만 NhDataClient로 내보낸다.
 */
@Service
@RequiredArgsConstructor
public class NhExportBatchService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final DonationLedgerRepository donationLedgerRepository;
    private final NhExportLogRepository nhExportLogRepository;
    private final NhDataClient nhDataClient;

    public List<NhExportLog> history() {
        return nhExportLogRepository.findAll();
    }

    @Transactional
    public int runExportBatch() {
        List<DonationLedger> targets = donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> !nhExportLogRepository.existsById(d.getCntrSn()))
                .toList();
        if (targets.isEmpty()) {
            return 0;
        }

        List<NhExportRow> rows = targets.stream()
                .map(d -> new NhExportRow(d.getCntrSn(), d.getUserId(), d.getLocgovCode(), d.getAmount()))
                .toList();
        String status = nhDataClient.export(rows);

        for (DonationLedger d : targets) {
            NhExportLog log = new NhExportLog();
            log.setCntrSn(d.getCntrSn());
            log.setUserId(d.getUserId());
            log.setLocgovCode(d.getLocgovCode());
            log.setAmount(d.getAmount());
            log.setExportStatus(status);
            log.setExportedDate(LocalDateTime.now());
            nhExportLogRepository.save(log);
        }
        return targets.size();
    }
}
