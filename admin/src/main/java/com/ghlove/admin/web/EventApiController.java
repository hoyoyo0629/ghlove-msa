package com.ghlove.admin.web;

import com.ghlove.admin.domain.Event;
import com.ghlove.admin.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "지역 이벤트" JSON API - {@link EventController}(Thymeleaf)의
 * `/events` GET/`/events/{id}` GET과 완전히 같은 조합. 시/군/구 캐스케이드
 * (`GET /events/cities`)는 이미 순수 JSON 응답이라 프론트가 그대로 재사용한다(여기서
 * 다시 만들지 않음). */
@RestController
@RequiredArgsConstructor
public class EventApiController {

    private final EventService eventService;

    public record EventRowDto(Integer eventId, String title, String listImage, String hostName, String phone,
                               String displayDate) {
    }

    public record EventListResponse(List<EventRowDto> events, int totalCount, int currentPage, int totalPages,
                                     Map<String, String> provinces, Map<String, String> cities) {
    }

    @GetMapping("/api/events")
    public EventListResponse list(@RequestParam(required = false, defaultValue = "Y") String ing,
                                   @RequestParam(required = false) String upperLocgovCode,
                                   @RequestParam(required = false) String locgovCode,
                                   @RequestParam(required = false) String q,
                                   @RequestParam(required = false, defaultValue = "8") int size,
                                   @RequestParam(required = false, defaultValue = "1") int page) {
        var all = eventService.search(ing, upperLocgovCode, locgovCode, q);
        int pageSize = size > 0 ? size : 8;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<EventRowDto> rows = all.subList(from, to).stream()
                .map(e -> new EventRowDto(e.getEventId(), e.getTitle(), e.getListImage(), e.getHostName(), e.getPhone(),
                        formatDate(e.getStartDate()) + " ~ " + formatDate(e.getEndDate())))
                .toList();
        return new EventListResponse(rows, all.size(), currentPage, Math.max(totalPages, 1),
                eventService.provinces(), eventService.citiesOf(upperLocgovCode));
    }

    public record EventDetailDto(Integer eventId, String title, String content, String listImage, String linkUrl,
                                  String hostName, String phone, String displayDate) {
    }

    @GetMapping("/api/events/{id}")
    public ResponseEntity<EventDetailDto> detail(@PathVariable Integer id) {
        Event event;
        try {
            event = eventService.findOrThrow(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(new EventDetailDto(event.getEventId(), event.getTitle(), event.getContent(),
                event.getListImage(), event.getLinkUrl(), event.getHostName(), event.getPhone(),
                formatDate(event.getStartDate()) + " ~ " + formatDate(event.getEndDate())));
    }

    private static String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() < 8) {
            return "";
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
    }
}
