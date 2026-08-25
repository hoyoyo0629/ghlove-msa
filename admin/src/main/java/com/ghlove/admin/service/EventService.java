package com.ghlove.admin.service;

import com.ghlove.admin.domain.Event;
import com.ghlove.admin.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지역 이벤트 (AS-IS /featured/eventList.html). 시딩 규모가 작아 FAQ/자료실과
 * 동일하게 검색/필터/정렬을 인메모리로 처리한다.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final EventRepository eventRepository;

    /** ing=Y: 오늘이 진행기간 내(startDate<=오늘<=endDate). ing=N: 종료(endDate<오늘). */
    public List<Event> search(String ing, String upperLocgovCode, String locgovCode, String keyword) {
        String today = LocalDate.now().format(YYYYMMDD);
        List<Event> all = eventRepository.findByUseYn("Y");

        List<Event> filtered = all.stream()
                .filter(e -> "N".equals(ing)
                        ? (e.getEndDate() != null && e.getEndDate().compareTo(today) < 0)
                        : (e.getStartDate() != null && e.getStartDate().compareTo(today) <= 0
                           && e.getEndDate() != null && e.getEndDate().compareTo(today) >= 0))
                .toList();

        if (upperLocgovCode != null && !upperLocgovCode.isBlank()) {
            filtered = filtered.stream().filter(e -> upperLocgovCode.equals(e.getUpperLocgovCode())).toList();
        }
        if (locgovCode != null && !locgovCode.isBlank()) {
            filtered = filtered.stream().filter(e -> locgovCode.equals(e.getLocgovCode())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            filtered = filtered.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().contains(kw))
                    .toList();
        }

        return filtered.stream()
                .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    public Event findOrThrow(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트가 존재하지 않습니다: " + eventId));
    }

    /** 시/도 드롭다운 - 실제로 이벤트가 등록된 지자체 기준으로만 노출한다(코드 -> 이름). */
    public Map<String, String> provinces() {
        Map<String, String> result = new LinkedHashMap<>();
        eventRepository.findByUseYn("Y").stream()
                .filter(e -> e.getUpperLocgovCode() != null)
                .sorted(Comparator.comparing(Event::getUpperLocgovCode))
                .forEach(e -> result.putIfAbsent(e.getUpperLocgovCode(), e.getUpperLocgovNm()));
        return result;
    }

    /** 시/군/구 드롭다운 - 선택된 시/도에 속한 지자체만 노출한다. */
    public Map<String, String> citiesOf(String upperLocgovCode) {
        Map<String, String> result = new LinkedHashMap<>();
        if (upperLocgovCode == null || upperLocgovCode.isBlank()) {
            return result;
        }
        eventRepository.findByUseYn("Y").stream()
                .filter(e -> upperLocgovCode.equals(e.getUpperLocgovCode()) && e.getLocgovCode() != null)
                .sorted(Comparator.comparing(Event::getLocgovCode))
                .forEach(e -> result.putIfAbsent(e.getLocgovCode(), e.getLocgovNm()));
        return result;
    }
}
