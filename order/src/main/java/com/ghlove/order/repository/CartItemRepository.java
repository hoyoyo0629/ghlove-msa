package com.ghlove.order.repository;

import com.ghlove.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedDateDesc(Long userId);

    Optional<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

    /** 옵션까지 같은 행만 합산한다 - 같은 답례품이라도 옵션이 다르면 별도 장바구니 행이다. */
    Optional<CartItem> findByUserIdAndItemIdAndItemOptionId(Long userId, Long itemId, Long itemOptionId);

    /** 각인(textOption)까지 같은 행만 합산 - 각인이 다르면 별도 라인(AS-IS 동일). */
    Optional<CartItem> findByUserIdAndItemIdAndItemOptionIdAndTextOption(Long userId, Long itemId,
                                                                        Long itemOptionId, String textOption);

    List<CartItem> findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List<Long> cartItemIds, Long userId);

    /** 파생 delete는 트랜잭션 없이는 실행되지 않는다 - 조회(view)를 트랜잭션 밖으로 빼면서
     *  "품절 항목 자동 삭제"가 스스로 트랜잭션을 열도록 여기에 직접 선언한다. */
    @Modifying
    @Transactional
    void deleteByCartItemIdInAndUserId(List<Long> cartItemIds, Long userId);
}
