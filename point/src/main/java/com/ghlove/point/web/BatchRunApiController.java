package com.ghlove.point.web;

import com.ghlove.point.service.BatchJobScheduler;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 배치 수동 실행 - AS-IS ghlove-batch {@code ManualBatchController GET /batch/run/{jobMethod}}. 내부 전용(/api/admin/** 시크릿). */
@RestController
@RequiredArgsConstructor
public class BatchRunApiController {

    private final BatchJobScheduler batchJobScheduler;
    private final EntityManager entityManager;

    @PostMapping("/api/admin/batch/run/{jobMethod}")
    public String run(@PathVariable String jobMethod) {
        try {
            return "Job triggered manually : " + jobMethod + " - " + batchJobScheduler.runNow(jobMethod);
        } catch (RuntimeException e) {
            return "Error triggering job : " + e.getMessage();
        }
    }

    public record ExpiringPoint(Long userId, String pointEndDe, Long point) {
    }

    /**
     * admin 배치 expirationPointSendMessageBatch(포인트 소멸 예정 안내 메일)용 - AS-IS
     * {@code give-point-expiration-mapper.getGivePointExpirationMailInfoList}의 안쪽 집계:
     * 잔여 포인트가 있고 만료일 90일 전이 오늘인 적립분을 회원·만료일별로 합친다. AS-IS의 G_CNTR 잔여포인트·만료일
     * (수납완료·미삭제 기부)은 TO-BE에서 기부 수납완료 때 쌓이는 적립 원장행(remainingAmount·expirationDate)이다.
     */
    @GetMapping("/api/admin/batch/expiring-points")
    @SuppressWarnings("unchecked")
    public List<ExpiringPoint> expiringPoints() {
        String endDe = LocalDate.now().plusDays(90).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Object[]> rows = entityManager.createQuery("select l.userId, l.expirationDate, sum(l.remainingAmount) from PointLedger l "
                        + "where l.remainingAmount > 0 and l.expirationDate = :endDe group by l.userId, l.expirationDate")
                .setParameter("endDe", endDe).getResultList();
        return rows.stream().map(r -> new ExpiringPoint((Long) r[0], (String) r[1], ((Number) r[2]).longValue())).toList();
    }
}
