package com.ghlove.donation.web;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 회원별 기부누적액 일괄 조회 (<b>조회 전용</b>) - admin 휴면회원관리(메뉴 4107)의
 * "기부누적액" 컬럼용이다.
 *
 * <p>AS-IS는 운영관리 한 덩어리라 휴면회원 목록 쿼리가 기부누적액까지 한 번에 조인했지만,
 * MSA에서는 기부가 donation 소유라 admin이 목록의 userId들을 모아 이 API로 한 번에 받아
 * 채운다(행마다 호출하면 N+1이 된다).
 *
 * <p>누적액은 <b>완료(COMPLETED)된 기부만</b> 합산한다 - 진행중/취소 건을 누적액에 넣으면
 * 실제 기부실적과 어긋난다(donation의 다른 누적 조회도 같은 기준이다).
 */
@RestController
@RequestMapping("/api/admin/donation-totals")
@RequiredArgsConstructor
public class DonationTotalAdminApiController {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final DonationRepository donationRepository;

    /** @return userId -> 기부누적액. 기부가 없는 회원은 결과에 들어있지 않다(호출부에서 '-'로 본다). */
    @GetMapping
    public Map<Long, BigDecimal> totals(@RequestParam List<Long> userIds) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Long userId : userIds) {
            BigDecimal sum = donationRepository.findByUserIdOrderByFrstRegistPnttmDesc(userId).stream()
                    .filter(d -> STATUS_COMPLETED.equals(d.getCntrSttusCode()))
                    .map(Donation::getCntrAmt)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.signum() != 0) {
                result.put(userId, sum);
            }
        }
        return result;
    }
}
