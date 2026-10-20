package com.ghlove.gift.service;

import com.ghlove.gift.domain.SeasonFood;
import com.ghlove.gift.repository.SeasonFoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 제철식품관 관리(AS-IS opmanager/seasonal-food, SeasonalFoodServiceImpl) - 월별 제철 키워드.
 *
 * <p>AS-IS 저장 구조는 <b>키워드마다 한 행</b>(G_SEASON_FOOD: 월 + 등록순번)이고, 화면은 한 달의 키워드를 쉼표로 이어 한 칸에 보여준다
 * (getSeasonalFoodDisplay의 GROUP_CONCAT). 저장은 그 달의 행을 전부 지우고 쉼표로 나눈 키워드를 1번부터 다시 넣는다.
 */
@Service
@RequiredArgsConstructor
public class SeasonFoodAdminService {

    private final SeasonFoodRepository repository;

    /** 월 한 칸 - 키워드는 쉼표로 이은 글자(없는 달은 빈 글자). */
    public record MonthKeywords(int seasonalFoodMonth, String seasonalFoodKeyword) {
    }

    /** AS-IS getSeasonalFood → makeFullSeasonalKeyword: 데이터가 없는 달도 빈 키워드로 채워 1~12월 전부, 월 순서로. */
    @Transactional(readOnly = true)
    public List<MonthKeywords> list() {
        Map<Integer, StringBuilder> byMonth = new LinkedHashMap<>();
        for (SeasonFood row : repository.findAllByOrderBySeasonFoodMonthAscRegSeqAsc()) {
            StringBuilder sb = byMonth.computeIfAbsent(row.getSeasonFoodMonth(), m -> new StringBuilder());
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(row.getSeasonFoodKeyword() == null ? "" : row.getSeasonFoodKeyword());
        }
        List<MonthKeywords> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            StringBuilder sb = byMonth.get(month);
            result.add(new MonthKeywords(month, sb == null ? "" : sb.toString()));
        }
        return result;
    }

    /** AS-IS saveSeasonalFood - 달마다 지우고 다시 넣는다. 저장 뒤 목록을 돌려준다(화면이 표를 다시 그린다). */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<MonthKeywords> save(List<MonthKeywords> months, long userId) {
        LocalDateTime now = LocalDateTime.now();
        for (MonthKeywords m : months) {
            repository.deleteByMonth(m.seasonalFoodMonth());
            int seq = 0;
            for (String keyword : (m.seasonalFoodKeyword() == null ? "" : m.seasonalFoodKeyword()).split(",")) {
                if (keyword == null || keyword.trim().isEmpty()) {
                    continue;
                }
                SeasonFood row = new SeasonFood();
                row.setSeasonFoodMonth(m.seasonalFoodMonth());
                row.setRegSeq(++seq);
                row.setSeasonFoodKeyword(keyword.trim());
                row.setFrstRegisterId(userId);
                row.setFrstRegistPnttm(now);
                row.setLastUpdusrId(userId);
                row.setLastUpdtPnttm(now);
                repository.save(row);
            }
        }
        repository.flush();
        return list();
    }
}
