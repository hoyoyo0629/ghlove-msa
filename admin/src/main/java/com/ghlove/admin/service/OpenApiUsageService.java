package com.ghlove.admin.service;

import com.ghlove.admin.domain.OpenApiUsageStat;
import com.ghlove.admin.event.OpenApiUsageEvent;
import com.ghlove.admin.repository.OpenApiUsageStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** SFR-010 "민간개방 API... 관련 통계" - gift가 발행하는 open-api.usage를 소비해 쌓고
 *  (STAT_POINT_LEDGER와 같은 append-only 원본 로그), 조회 시점에 컨슈머별로 집계한다. */
@Service
@RequiredArgsConstructor
public class OpenApiUsageService {

    private final OpenApiUsageStatRepository openApiUsageStatRepository;

    @Transactional
    public void onEvent(OpenApiUsageEvent event) {
        OpenApiUsageStat stat = new OpenApiUsageStat();
        stat.setConsumerUsername(event.consumerUsername());
        stat.setServiceName(event.service());
        stat.setEndpoint(event.endpoint());
        stat.setCalledAt(event.calledAt());
        stat.setCreatedDate(LocalDateTime.now());
        openApiUsageStatRepository.save(stat);
    }

    /** 최근 500건 기준 컨슈머별 호출건수 - 표시용 통계라 전체 스캔 대신 최근분만 집계한다. */
    public Map<String, Long> callCountByConsumer() {
        List<OpenApiUsageStat> recent = openApiUsageStatRepository.findTop500ByOrderByUsageIdDesc();
        return recent.stream()
                .collect(Collectors.groupingBy(OpenApiUsageStat::getConsumerUsername, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public List<OpenApiUsageStat> recentCalls() {
        return openApiUsageStatRepository.findTop500ByOrderByUsageIdDesc();
    }
}
