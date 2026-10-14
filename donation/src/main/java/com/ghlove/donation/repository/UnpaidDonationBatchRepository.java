package com.ghlove.donation.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 미납 기부 삭제 배치 - AS-IS ngdonation-mapper {@code deleteCntrData}(서울 미결제분)·
 * {@code deleteNotSunapStndBatch}(지방세외 현세대 미납분)와, 그 삭제건에 딸린 오프라인 답례품 주문을 정리할 때
 * order가 확인하는 {@code offgive-mapper.selectGiveSunapCheck}.
 *
 * <p>미납 조건은 AS-IS 그대로 {@code STTEMNT_PAY_DE IS NULL}이다(2026-10-14 - 수납완료가 수납일을 넣게 되어
 * 상태 REQUESTED로 대신 보던 번역을 걷어냈다). selectGiveSunapCheck는 AS-IS처럼 상태 '100'(REQUESTED)과
 * 수납일 없음을 둘 다 본다. {@code LAST_UPDT_PNTTM}은 AS-IS 문자열과 달리 timestamp라 now()를 넣는다.
 */
@Repository
@RequiredArgsConstructor
public class UnpaidDonationBatchRepository {

    private final EntityManager entityManager;

    /**
     * AS-IS deleteCntrData - 서울 대상 미결제 기부를 기부일 다음 날부터 삭제한다
     * ({@code datediff(오늘, cntr_de) > 0 AND 미납 AND DELETE_AT='N' AND SEOUL_TRGET_AT='Y'}).
     */
    @Transactional
    public int deleteSeoulUnpaid() {
        return entityManager.createNativeQuery("""
                UPDATE donation.g_cntr
                   SET delete_at = 'Y'
                     , last_updt_pnttm = now()
                     , last_updusr_id = 0
                 WHERE cntr_de < to_char(current_date, 'YYYYMMDD')
                   AND sttemnt_pay_de IS NULL
                   AND delete_at = 'N'
                   AND seoul_trget_at = 'Y'
                """).executeUpdate();
    }

    /**
     * AS-IS deleteNotSunapStndBatch - 서울 외 미납 기부를 납부유효일 다음 날부터 삭제한다
     * ({@code datediff(오늘, pay_valid_de) > 0 AND 미납 AND DELETE_AT='N' AND SEOUL_TRGET_AT='N'}).
     */
    @Transactional
    public int deleteNotSunapStnd() {
        return entityManager.createNativeQuery("""
                UPDATE donation.g_cntr
                   SET delete_at = 'Y'
                     , last_updt_pnttm = now()
                     , last_updusr_id = 0
                 WHERE pay_valid_de < to_char(current_date, 'YYYYMMDD')
                   AND sttemnt_pay_de IS NULL
                   AND delete_at = 'N'
                   AND seoul_trget_at = 'N'
                """).executeUpdate();
    }

    /**
     * AS-IS offgive-mapper selectGiveSunapCheck - 주어진 기부일련번호 중 "오프라인(200·300)·미납·삭제됨"인 것.
     * order의 미수납 답례품 정리 배치(AS-IS allDayOffItemUpdateBatch)가 부른다.
     */
    @SuppressWarnings("unchecked")
    public List<String> deletedUnpaidOffline(List<String> cntrSns) {
        if (cntrSns == null || cntrSns.isEmpty()) {
            return List.of();
        }
        return entityManager.createNativeQuery("""
                SELECT cntr_sn
                  FROM donation.g_cntr
                 WHERE cntr_path_code IN ('200', '300')
                   AND cntr_sttus_code = 'REQUESTED'
                   AND sttemnt_pay_de IS NULL
                   AND delete_at = 'Y'
                   AND cntr_sn IN (:cntrSns)
                """)
                .setParameter("cntrSns", cntrSns)
                .getResultList();
    }
}
