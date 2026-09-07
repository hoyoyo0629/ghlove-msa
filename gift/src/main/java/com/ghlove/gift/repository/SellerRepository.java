package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> findByCommunityBusinessYn(String communityBusinessYn);

    List<Seller> findAllByOrderBySellerIdDesc();

    /** 판매자 셀프포털 로그인용 - member의 ROLE_PROVIDER 계정(GH_AUTH JWT의 userId)과
     *  이 지자체 답례품 제공업체 레코드를 잇는 유일한 연결고리. */
    Optional<Seller> findByMemberUserId(Long memberUserId);
}
