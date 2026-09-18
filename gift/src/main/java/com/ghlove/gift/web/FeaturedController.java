package com.ghlove.gift.web;

import com.ghlove.gift.domain.Featured;
import com.ghlove.gift.service.FeaturedService;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.LocgovClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 지역이벤트 (AS-IS `featured/eventList.html`, `featured/eventDetail.html`).
 *
 * <p>살아있는 푸터(`footer_ali.vue:225,229`)가 `?ing=Y`(진행중)/`?ing=N`(종료)로 진입한다.
 * 운영관리 등록 API는 이미 있었고 공개 화면만 없어(gift 인벤토리 §4 재현누락) 여기서 채운다.
 */
@Controller
@RequiredArgsConstructor
public class FeaturedController {

    private final FeaturedService featuredService;
    private final GiftService giftService;
    private final LocgovClient locgovClient;

    /** 지역이벤트 목록. AS-IS 푸터 링크와 동일하게 `ing` 파라미터로 진행중/종료를 가른다. */
    @GetMapping("/events")
    public String list(@RequestParam(required = false, defaultValue = "Y") String ing, Model model) {
        boolean ongoing = !"N".equalsIgnoreCase(ing);
        List<Featured> events = featuredService.events(ongoing);
        model.addAttribute("events", events);
        model.addAttribute("ing", ongoing ? "Y" : "N");
        model.addAttribute("progressions", events.stream().collect(java.util.stream.Collectors.toMap(
                Featured::getFeaturedId, featuredService::progressionOf, (a, b) -> a)));
        return "events";
    }

    /** 지역이벤트 상세 - AS-IS와 동일하게 `featuredUrl`로 연다. */
    @GetMapping("/events/{featuredUrl}")
    public String detail(@PathVariable String featuredUrl, Model model) {
        Featured event;
        try {
            event = featuredService.detail(featuredUrl);
        } catch (GiftException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("events", List.of());
            model.addAttribute("ing", "Y");
            model.addAttribute("progressions", java.util.Map.of());
            return "events";
        }
        var items = featuredService.itemsOf(event.getFeaturedId());
        model.addAttribute("event", event);
        model.addAttribute("progression", featuredService.progressionOf(event));
        model.addAttribute("items", items);
        model.addAttribute("thumbnails", giftService.thumbnailsOf(items.stream().map(g -> g.getItemId()).toList()));
        model.addAttribute("locgovNames", locgovClient.namesByCode());
        return "event-detail";
    }
}
