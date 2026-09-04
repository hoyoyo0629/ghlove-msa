package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Ranking;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.RankingRepository;
import com.ghlove.gift.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 답례품 판매 랭킹 공개 API (AS-IS opmanager/ranking이 관리하는 노출용 랭킹 페이지, B9) -
 * 로그인 불필요, storefront가 카테고리/그룹별 TOP N 답례품을 노출할 때 쓴다. 관리자가
 * {@link RankingAdminApiController}에서 수동 큐레이션한 OP_RANKING 순서를 그대로 읽기만 한다.
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingApiController {

    private static final int DEFAULT_LIMIT = 10;

    private final RankingRepository rankingRepository;
    private final GiftRepository giftRepository;
    private final GiftService giftService;

    @GetMapping("/{categoryGroupCode}")
    public List<RankingItem> topN(@PathVariable String categoryGroupCode,
                                   @RequestParam(required = false) Integer limit) {
        int n = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        List<Ranking> rankings = rankingRepository.findByCategoryUrlOrderByOrderingAsc(categoryGroupCode).stream()
                .limit(n).toList();
        if (rankings.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = rankings.stream().map(r -> r.getItemId().longValue()).toList();
        Map<Long, Gift> itemsById = new LinkedHashMap<>();
        giftRepository.findAllById(itemIds).forEach(g -> itemsById.put(g.getItemId(), g));
        Map<Long, String> thumbnails = giftService.thumbnailsOf(itemIds);

        List<RankingItem> result = new java.util.ArrayList<>();
        int rank = 1;
        for (Ranking r : rankings) {
            Long itemId = r.getItemId().longValue();
            Gift item = itemsById.get(itemId);
            if (item == null) {
                continue; // 삭제된 상품은 조용히 건너뜀
            }
            String thumbnailUrl = thumbnails.get(itemId) != null ? "/uploads/" + thumbnails.get(itemId) : null;
            result.add(new RankingItem(rank++, itemId, item.getItemName(), item.getSalePrice(),
                    item.getLocgovCode(), thumbnailUrl));
        }
        return result;
    }

    public record RankingItem(int rank, Long itemId, String itemName, Integer salePrice,
                               String locgovCode, String thumbnailUrl) {
    }
}
