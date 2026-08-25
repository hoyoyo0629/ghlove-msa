package com.ghlove.admin.web;

import com.ghlove.admin.domain.Event;
import com.ghlove.admin.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 지역 이벤트 (AS-IS /featured/eventList.html, /featured/eventDetail.html) - 로그인 불필요, 공개 화면. */
@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private static final int PAGE_SIZE_DEFAULT = 8;

    @GetMapping("/events")
    public String list(@RequestParam(required = false, defaultValue = "Y") String ing,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "8") int size,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        Model model) {
        var all = eventService.search(ing, upperLocgovCode, locgovCode, q);
        int pageSize = size > 0 ? size : PAGE_SIZE_DEFAULT;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<Event> pageItems = all.subList(from, to);

        Map<Integer, String> displayDates = new HashMap<>();
        for (Event e : pageItems) {
            displayDates.put(e.getEventId(), formatDate(e.getStartDate()) + " ~ " + formatDate(e.getEndDate()));
        }

        model.addAttribute("events", pageItems);
        model.addAttribute("displayDates", displayDates);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("ing", ing);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("q", q);
        model.addAttribute("size", pageSize);
        model.addAttribute("provinces", eventService.provinces());
        model.addAttribute("cities", eventService.citiesOf(upperLocgovCode));
        return "events/list";
    }

    /** 시/군/구 옵션을 시/도 변경 시 AJAX로 다시 채우기 위한 JSON 엔드포인트. */
    @GetMapping("/events/cities")
    @ResponseBody
    public Map<String, String> cities(@RequestParam String upperLocgovCode) {
        return eventService.citiesOf(upperLocgovCode);
    }

    @GetMapping("/events/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Event event = eventService.findOrThrow(id);
        model.addAttribute("event", event);
        model.addAttribute("displayDate", formatDate(event.getStartDate()) + " ~ " + formatDate(event.getEndDate()));
        return "events/detail";
    }

    private static String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() < 8) {
            return "";
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
    }
}
