package com.ghlove.donation.web;

import com.ghlove.donation.repository.MemberDonationAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * admin 일반회원관리(메뉴 4101)가 호출하는 회원별 기부 조회 API (<b>조회 전용</b>).
 *
 * <p>AS-IS는 운영관리 한 덩어리라 회원 상세화면이 기부내역·포인트내역·누적합계를 한 DB에서
 * 조인해 만들었다. MSA에서는 기부가 donation, 포인트 사용이 point, 회원이 member 소유라
 * admin이 세 곳에서 받아 조립한다. 브라우저에 직접 노출되지 않고 admin 콘솔의 OP_MANAGER
 * 로그인 + 메뉴RBAC이 실제 게이트다(기존 {@code /api/admin/donation-totals}와 같은 관행).
 */
@RestController
@RequestMapping("/api/admin/member-donations")
@RequiredArgsConstructor
public class MemberDonationAdminApiController {

    private final MemberDonationAdminRepository repository;

    /** 상세화면 누적합계의 기부금액·발생포인트. */
    @GetMapping("/{userId}/cumulative-total")
    public MemberDonationAdminRepository.CumulativeRow cumulativeTotal(@PathVariable Long userId) {
        return repository.cumulativeTotal(userId);
    }

    /** 상세화면 기부내역 목록(페이징). */
    @GetMapping("/{userId}/cntr-list")
    public CntrListResponse cntrList(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "0") int startRow,
                                     @RequestParam(defaultValue = "10") int size) {
        return new CntrListResponse(repository.cntrCount(userId), repository.cntrList(userId, startRow, size));
    }

    /** 포인트 내역의 적립(OCC) 행 전체 - admin이 point의 사용 행과 합쳐 쓴다. */
    @GetMapping("/{userId}/point-occ-rows")
    public List<MemberDonationAdminRepository.PointOccRow> pointOccRows(@PathVariable Long userId) {
        return repository.pointOccRows(userId);
    }

    /** 목록화면의 기부누적액·발생포인트 일괄 조회. */
    @GetMapping("/totals")
    public Map<Long, MemberDonationAdminRepository.CumulativeRow> totals(@RequestParam List<Long> userIds) {
        return repository.totalsByUserIds(userIds);
    }

    public record CntrListResponse(int count, List<MemberDonationAdminRepository.CntrRow> content) {
    }
}
