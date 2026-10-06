package com.ghlove.gift.repository;

import com.ghlove.gift.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    List<Seller> findByCommunityBusinessYn(String communityBusinessYn);

    List<Seller> findAllByOrderBySellerIdDesc();

    /** 판매자 셀프포털 로그인용 - member의 ROLE_PROVIDER 계정(GH_AUTH JWT의 userId)과
     *  이 지자체 답례품 제공업체 레코드를 잇는 유일한 연결고리. */
    Optional<Seller> findByMemberUserId(Long memberUserId);

    /**
     * 이메일 발송(AS-IS opmanager/email)에서 발송대상을 "답례품"으로 고른 경우의 수신자 -
     * AS-IS {@code emailMapper.sendSellerUserList}의 조건을 그대로 옮긴 것이다
     * (STATUS_CODE=2 AND ITEM_APPROVAL_TYPE=1, 이름·이메일이 비어있지 않은 업체).
     *
     * ITEM_APPROVAL_TYPE은 일부러 {@link Seller} 엔티티에 매핑하지 않는다 - 이 컬럼은
     * DEFAULT '1'을 갖고 있고 admin 입점업체 등록 폼(SellerDetail)에는 이 값이 없어서,
     * 엔티티에 넣으면 등록 시 NULL이 명시적으로 들어가 기본값을 덮어쓴다. 그래서 읽기만
     * 네이티브 쿼리로 한다.
     */
    @Query(value = "select user_name, email from op_seller"
            + " where status_code = '2' and item_approval_type = '1'"
            + "   and email is not null and email <> ''"
            + "   and user_name is not null and user_name <> ''", nativeQuery = true)
    List<Object[]> findEmailSendTargets();
}
