package com.ghlove.point.web;

import com.ghlove.point.repository.PointBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 회원별 포인트 잔액 일괄 조회 (<b>조회 전용</b>) - admin 휴면회원관리(메뉴 4107)의
 * "포인트잔액" 컬럼용이다.
 *
 * <p>AS-IS는 운영관리 한 덩어리라 휴면회원 목록 쿼리가 포인트 잔액까지 한 번에 조인했지만,
 * MSA에서는 잔액({@code PT_POINT_BALANCE})이 point 소유라 admin이 목록의 userId들을 모아
 * 이 API로 한 번에 받아 채운다(행마다 호출하면 N+1이 된다).
 */
@RestController
@RequestMapping("/api/admin/point-balances")
@RequiredArgsConstructor
public class PointBalanceAdminApiController {

    private final PointBalanceRepository pointBalanceRepository;

    /** @return userId -> 잔액. 잔액 행이 없는 회원은 결과에 들어있지 않다(호출부에서 0으로 본다). */
    @GetMapping
    public Map<Long, Long> balances(@RequestParam List<Long> userIds) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        pointBalanceRepository.findAllById(userIds)
                .forEach(b -> result.put(b.getUserId(), b.getBalance() == null ? 0L : b.getBalance()));
        return result;
    }
}
