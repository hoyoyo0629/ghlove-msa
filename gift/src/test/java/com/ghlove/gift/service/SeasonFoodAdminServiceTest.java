package com.ghlove.gift.service;

import com.ghlove.gift.domain.SeasonFood;
import com.ghlove.gift.repository.SeasonFoodRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** AS-IS SeasonalFoodServiceImpl(목록 GROUP_CONCAT + makeFullSeasonalKeyword, 저장 = 달마다 지우고 키워드별 한 행) 재현 검증. */
class SeasonFoodAdminServiceTest {

    private final SeasonFoodRepository repository = mock(SeasonFoodRepository.class);
    private final SeasonFoodAdminService service = new SeasonFoodAdminService(repository);

    private static SeasonFood row(int month, int seq, String keyword) {
        SeasonFood f = new SeasonFood();
        f.setSeasonFoodMonth(month);
        f.setRegSeq(seq);
        f.setSeasonFoodKeyword(keyword);
        return f;
    }

    @Test
    void listJoinsKeywordsByMonthAndFillsMissingMonthsWithEmpty() {
        when(repository.findAllByOrderBySeasonFoodMonthAscRegSeqAsc())
                .thenReturn(List.of(row(1, 1, "귤"), row(1, 2, "레몬"), row(3, 1, "딸기")));

        List<SeasonFoodAdminService.MonthKeywords> months = service.list();

        assertThat(months).hasSize(12);
        assertThat(months.get(0)).isEqualTo(new SeasonFoodAdminService.MonthKeywords(1, "귤,레몬"));
        assertThat(months.get(1)).isEqualTo(new SeasonFoodAdminService.MonthKeywords(2, ""));
        assertThat(months.get(2)).isEqualTo(new SeasonFoodAdminService.MonthKeywords(3, "딸기"));
        assertThat(months.get(11).seasonalFoodMonth()).isEqualTo(12);
    }

    @Test
    void saveDeletesTheMonthThenInsertsOneRowPerKeywordFromSeqOne() {
        when(repository.findAllByOrderBySeasonFoodMonthAscRegSeqAsc()).thenReturn(List.of());

        service.save(List.of(new SeasonFoodAdminService.MonthKeywords(5, "딸기, 매실 ,,참외")), 777L);

        InOrder order = inOrder(repository);
        order.verify(repository).deleteByMonth(5);
        ArgumentCaptor<SeasonFood> saved = ArgumentCaptor.forClass(SeasonFood.class);
        verify(repository, times(3)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(SeasonFood::getRegSeq).containsExactly(1, 2, 3);
        assertThat(saved.getAllValues()).extracting(SeasonFood::getSeasonFoodKeyword).containsExactly("딸기", "매실", "참외");
        assertThat(saved.getAllValues()).allSatisfy(f -> {
            assertThat(f.getSeasonFoodMonth()).isEqualTo(5);
            assertThat(f.getFrstRegisterId()).isEqualTo(777L);
            assertThat(f.getLastUpdusrId()).isEqualTo(777L);
            assertThat(f.getFrstRegistPnttm()).isNotNull();
            assertThat(f.getLastUpdtPnttm()).isNotNull();
        });
    }

    @Test
    void saveWithEmptyKeywordOnlyClearsTheMonth() {
        when(repository.findAllByOrderBySeasonFoodMonthAscRegSeqAsc()).thenReturn(List.of());

        service.save(List.of(new SeasonFoodAdminService.MonthKeywords(7, "")), 1L);

        verify(repository).deleteByMonth(7);
        verify(repository, times(0)).save(any(SeasonFood.class));
    }
}
