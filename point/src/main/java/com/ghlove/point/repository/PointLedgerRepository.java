package com.ghlove.point.repository;

import com.ghlove.point.domain.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {
    List<PointLedger> findByUserIdOrderByCreatedDateDesc(Long userId);

    Optional<PointLedger> findFirstByRefKeyAndTxnType(String refKey, String txnType);

    /** donation 마이페이지 "기부내역 조회"의 "기부포인트" 컬럼용 - REF_KEY(CNTR_SN) 여러 건 일괄 조회. */
    List<PointLedger> findByRefKeyInAndTxnType(List<String> refKeys, String txnType);

    boolean existsByRefKeyAndTxnType(String refKey, String txnType);

    /** FIFO lot 소비 대상: 이 회원의 미소진 적립성 lot을 만료 임박순으로. */
    List<PointLedger> findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(Long userId, Long amount);

    /** 소멸 배치 대상: 만료일이 지났는데도 아직 남아있는 lot 전체 (전 회원). */
    List<PointLedger> findByRemainingAmountGreaterThanAndExpirationDateLessThan(Long amount, String expirationDate);
}
