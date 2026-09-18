package com.ghlove.gift.service;

import com.ghlove.gift.domain.Featured;
import com.ghlove.gift.domain.FeaturedItem;
import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.repository.FeaturedItemRepository;
import com.ghlove.gift.repository.FeaturedRepository;
import com.ghlove.gift.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 지역이벤트(기획전) 공개 조회 (AS-IS `EventController` `/api/event`).
 *
 * <p>진입은 살아있는 푸터 `footer_ali.vue:225,229`의 `?ing=Y`(진행중)·`?ing=N`(종료)이다.
 * 운영관리 등록(`FeaturedAdminApiController`)은 이미 있었고 <b>공개 화면만 없었다</b>.
 */
@Service
@RequiredArgsConstructor
public class FeaturedService {

    /** AS-IS `featuredType='1'` - 기획전 타입 1이 지역이벤트다. */
    private static final String EVENT_TYPE = "1";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FeaturedRepository featuredRepository;
    private final FeaturedItemRepository featuredItemRepository;
    private final GiftRepository giftRepository;

    /** @param ongoing true=진행중, false=종료 (AS-IS `ingCond` Y/N) */
    public List<Featured> events(boolean ongoing) {
        String today = DATE.format(LocalDate.now());
        return ongoing
                ? featuredRepository.findOngoing(EVENT_TYPE, today)
                : featuredRepository.findEnded(EVENT_TYPE, today);
    }

    /** 상세 - AS-IS와 동일하게 `featuredUrl`로 찾는다. 사용중지된 이벤트는 열리지 않는다. */
    public Featured detail(String featuredUrl) {
        return featuredRepository.findByFeaturedUrlAndFeaturedFlag(featuredUrl, "Y")
                .orElseThrow(() -> new GiftException("이벤트를 찾을 수 없습니다."));
    }

    /** 이벤트에 편성된 답례품 (AS-IS `GET /api/event/items`). 편성 순서를 유지한다. */
    public List<Gift> itemsOf(Integer featuredId) {
        List<Integer> itemIds = featuredItemRepository.findByFeaturedIdOrderByDisplayOrder(featuredId)
                .stream().map(FeaturedItem::getItemId).toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        List<Gift> gifts = giftRepository.findAllById(itemIds.stream().map(Integer::longValue).toList());
        // findAllById는 순서를 보장하지 않는다 - 편성 순서(DISPLAY_ORDER)대로 다시 세운다.
        return itemIds.stream()
                .map(id -> gifts.stream().filter(g -> g.getItemId().equals(id.longValue())).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /** 진행 상태 표시용 - AS-IS `progression`(1:예정 2:진행중 3:종료). */
    public String progressionOf(Featured f) {
        String today = DATE.format(LocalDate.now());
        if (f.getEndDate() != null && !f.getEndDate().isBlank() && f.getEndDate().compareTo(today) < 0) {
            return "종료";
        }
        if (f.getStartDate() != null && !f.getStartDate().isBlank() && f.getStartDate().compareTo(today) > 0) {
            return "예정";
        }
        return "진행중";
    }
}
