package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Ranking;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.RankingRepository;
import com.ghlove.gift.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 답례품 판매 랭킹 관리자 API (AS-IS opmanager/ranking - RankingManagerController, B9) -
 * admin 콘솔(/admin/ranking)이 부르는 cross-service API. 다른 admin cross-service API와
 * 동일하게 별도 인증 없이 열려있다. OP_RANKING을 카테고리(그룹)코드별 수동 큐레이션
 * 순서목록으로 다룬다 - 공개페이지는 {@link RankingApiController}가 담당.
 */
@RestController
@RequestMapping("/api/admin/ranking")
@RequiredArgsConstructor
public class RankingAdminApiController {

    private final RankingRepository rankingRepository;
    private final GiftRepository giftRepository;
    private final GiftService giftService;

    /** 이미 데이터가 있는 그룹코드 목록(드롭다운용) - 없으면 빈 목록. */
    @GetMapping("/groups")
    public List<String> groups() {
        return rankingRepository.findDistinctCategoryUrls();
    }

    @GetMapping
    public List<RankingRow> list(@RequestParam String categoryUrl) {
        List<Ranking> rows = rankingRepository.findByCategoryUrlOrderByOrderingAsc(categoryUrl);
        return toRows(rows);
    }

    /** 랭킹에 추가할 상품을 이름으로 찾는 간단 검색 - 이미 gift가 갖고 있는 상품 목록에서 필터. */
    @GetMapping("/item-search")
    public List<ItemOption> itemSearch(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return giftRepository.findByDataStatusCodeAndDisplayFlagAndItemNameContainingIgnoreCaseOrderByItemIdDesc(
                        "APPROVED", "Y", keyword.trim())
                .stream().limit(20)
                .map(g -> new ItemOption(g.getItemId(), g.getItemName(), g.getLocgovCode(), g.getSalePrice()))
                .toList();
    }

    @PostMapping
    public RankingRow add(@RequestBody AddForm form) {
        if (form.categoryUrl() == null || form.categoryUrl().isBlank() || form.itemId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "그룹코드와 상품은 필수입니다.");
        }
        int itemId = Math.toIntExact(form.itemId());
        if (rankingRepository.existsByCategoryUrlAndItemId(form.categoryUrl(), itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 이 그룹에 등록된 상품입니다.");
        }
        if (!giftRepository.existsById(form.itemId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다: " + form.itemId());
        }
        int nextOrdering = rankingRepository.findByCategoryUrlOrderByOrderingAsc(form.categoryUrl()).stream()
                .mapToInt(r -> r.getOrdering() == null ? 0 : r.getOrdering()).max().orElse(-1) + 1;
        Ranking r = new Ranking();
        r.setCategoryUrl(form.categoryUrl());
        r.setItemId(itemId);
        r.setOrdering(nextOrdering);
        rankingRepository.save(r);
        return toRows(List.of(r)).get(0);
    }

    @PostMapping("/{rankingId}/delete")
    public void delete(@PathVariable Integer rankingId) {
        rankingRepository.deleteById(rankingId);
    }

    @PostMapping("/{rankingId}/move")
    public void move(@PathVariable Integer rankingId, @RequestParam String direction) {
        Ranking target = rankingRepository.findById(rankingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."));
        List<Ranking> siblings = rankingRepository.findByCategoryUrlOrderByOrderingAsc(target.getCategoryUrl());
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getRankingId().equals(rankingId)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return;
        }
        int otherIdx = "up".equalsIgnoreCase(direction) ? idx - 1 : idx + 1;
        if (otherIdx < 0 || otherIdx >= siblings.size()) {
            return;
        }
        Ranking a = siblings.get(idx);
        Ranking b = siblings.get(otherIdx);
        Integer aOrd = a.getOrdering();
        a.setOrdering(b.getOrdering());
        b.setOrdering(aOrd);
        rankingRepository.saveAll(List.of(a, b));
    }

    private List<RankingRow> toRows(List<Ranking> rankings) {
        List<Long> itemIds = rankings.stream().map(r -> r.getItemId().longValue()).toList();
        Map<Long, Gift> itemsById = new LinkedHashMap<>();
        giftRepository.findAllById(itemIds).forEach(g -> itemsById.put(g.getItemId(), g));
        Map<Long, String> thumbnails = giftService.thumbnailsOf(itemIds);
        return rankings.stream().map(r -> {
            Long itemId = r.getItemId().longValue();
            Gift item = itemsById.get(itemId);
            String thumbnailUrl = thumbnails.get(itemId) != null ? "/uploads/" + thumbnails.get(itemId) : null;
            return new RankingRow(r.getRankingId(), r.getCategoryUrl(), itemId, r.getOrdering(),
                    item != null ? item.getItemName() : ("상품#" + itemId),
                    item != null ? item.getSalePrice() : null,
                    item != null ? item.getLocgovCode() : null,
                    thumbnailUrl);
        }).toList();
    }

    public record AddForm(String categoryUrl, Long itemId) {
    }

    public record ItemOption(Long itemId, String itemName, String locgovCode, Integer salePrice) {
    }

    public record RankingRow(Integer rankingId, String categoryUrl, Long itemId, Integer ordering,
                              String itemName, Integer salePrice, String locgovCode, String thumbnailUrl) {
    }
}
