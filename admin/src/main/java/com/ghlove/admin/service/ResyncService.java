package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * StatsService ReadModel 재동기화 (SFR-009 "원장 복구·재동기화 절차"). Kafka 컨슈머
 * 오프셋/보존기간과 무관하게, 각 원장서비스의 현재 상태 전체 스냅샷을 불러와 upsert한다 -
 * admin DB만 재프로비저닝됐거나(오늘 실제로 겪은 사고), admin이 장기간 다운돼 토픽 보존기간을
 * 넘긴 경우 등 Kafka 리플레이만으로는 못 잡는 정합성 문제를 여기서 복구한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResyncService {

    private final DonationClient donationClient;
    private final OrderClient orderClient;
    private final PointClient pointClient;
    private final GiftClient giftClient;
    private final StatsService statsService;

    public Result resyncAll() {
        var donations = donationClient.allForResync();
        statsService.resyncDonations(donations);

        var orders = orderClient.allForResync();
        statsService.resyncOrders(orders);

        var ledger = pointClient.allForResync();
        statsService.resyncPointLedger(ledger);

        var gifts = giftClient.allForResync();
        statsService.resyncGifts(gifts);

        log.info("Stats ReadModel resync complete: donations={} orders={} pointLedger={} gifts={}",
                donations.size(), orders.size(), ledger.size(), gifts.size());
        return new Result(donations.size(), orders.size(), ledger.size(), gifts.size());
    }

    public record Result(int donationCount, int orderCount, int pointLedgerCount, int giftCount) {
    }

    /**
     * 기부포인트 적립 백필 - ReadModel 재동기화와는 별개의, 실제 잔액에 영향을 주는 쓰기
     * 작업이라 의도적으로 분리했다. donation의 완료 기부 전체를 훑어 point의 기존 멱등
     * 엔드포인트(REF_KEY+EARN 중복체크)를 호출한다 - 이미 정상 적립된 건은 자동 no-op이라
     * 몇 번을 실행해도 안전하다(오늘 point DB 재프로비저닝으로 실제 발생한 적립 누락을 복구).
     */
    public PointBackfillResult backfillPointCredits() {
        var donations = donationClient.allForResync();
        int credited = 0;
        int alreadyOk = 0;
        int failed = 0;
        for (var d : donations) {
            if (!"COMPLETED".equals(d.cntrSttusCode())) {
                continue;
            }
            try {
                boolean wasCredited = pointClient.creditForDonation(
                        d.cntrSn(), d.userId(), d.cntrLocgovCode(), d.cntrAmt(), d.cntrDe());
                if (wasCredited) {
                    credited++;
                } else {
                    alreadyOk++;
                }
            } catch (RuntimeException e) {
                log.warn("Point credit backfill failed for cntrSn={}", d.cntrSn(), e);
                failed++;
            }
        }
        log.info("Point credit backfill complete: credited={} alreadyOk={} failed={}", credited, alreadyOk, failed);
        return new PointBackfillResult(credited, alreadyOk, failed);
    }

    public record PointBackfillResult(int newlyCredited, int alreadyCredited, int failed) {
    }
}
