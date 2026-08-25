package com.ghlove.donation.repository;

import com.ghlove.donation.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByUserIdOrderByFrstRegistPnttmDesc(Long userId);

    List<Donation> findByUserIdAndCntrSttusCodeOrderByCntrDeDesc(Long userId, String cntrSttusCode);

    List<Donation> findByCntrSnInAndUserIdAndCntrSttusCode(List<String> cntrSnList, Long userId, String cntrSttusCode);

    List<Donation> findByUserIdAndCntrDeStartingWithAndCntrSttusCode(Long userId, String yearPrefix, String cntrSttusCode);

    List<Donation> findByUserIdAndCntrLocgovCodeAndCntrDeStartingWithAndCntrSttusCode(
            Long userId, String cntrLocgovCode, String yearPrefix, String cntrSttusCode);

    /** 마이페이지 "관심지자체"의 "나의 기부현황" - 연도 무관 전체 완료 기부 합산. */
    List<Donation> findByUserIdAndCntrLocgovCodeAndCntrSttusCode(Long userId, String cntrLocgovCode, String cntrSttusCode);

    List<Donation> findByDsgnDntnBizIdAndCntrSttusCode(Long dsgnDntnBizId, String cntrSttusCode);

    /** 사이트 전체(전 회원) 합계용 - 메인화면 "총 기부금" 위젯. */
    List<Donation> findByCntrDeBetweenAndCntrSttusCode(String startDe, String endDe, String cntrSttusCode);

    /** admin 지정기부 모금분석(analysis) 화면용 - 지정기부 전체를 지자체×월별로 집계한다. */
    List<Donation> findByDsgnDntnBizIdIsNotNullAndCntrSttusCode(String cntrSttusCode);

    /** admin 오프라인기부(offgive) 목록용 - 접수경로가 OFFLINE인 기부만. */
    List<Donation> findByCntrPathCodeOrderByFrstRegistPnttmDesc(String cntrPathCode);
}
