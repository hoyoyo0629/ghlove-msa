package com.ghlove.donation.service;

import com.ghlove.donation.domain.InterestLocgov;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.InterestLocgovRepository;
import com.ghlove.donation.repository.LocgovRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 관심 지자체 통계 (AS-IS opmanager/statistics/StatisticsLocgovManagerController). 마이페이지
 * "관심지자체"(G_INTRST_LOCGOV) 원본 등록행을 월별/지자체별로 집계한다 - 전용 통계 테이블은
 * 없고, 등록행 자체가 REGIST_DE(yyyyMMdd)를 갖고 있어 그때그때 스캔해서 집계하는 방식으로
 * 충분하다(회원 관심지자체 등록 총량이 대량 배치성 수치가 아니라서).
 */
@Service
@RequiredArgsConstructor
public class InterestLocgovStatsService {

    private final InterestLocgovRepository interestLocgovRepository;
    private final LocgovRepository locgovRepository;

    /** 목록 화면 상단의 연도 선택 드롭다운용 - REGIST_DE가 있는 행에서 실제 등장하는 연도만. */
    public List<String> availableYears() {
        TreeSet<String> years = new TreeSet<>(Comparator.reverseOrder());
        for (InterestLocgov row : interestLocgovRepository.findAll()) {
            if (row.getRegistDe() != null && row.getRegistDe().length() >= 4) {
                years.add(row.getRegistDe().substring(0, 4));
            }
        }
        if (years.isEmpty()) {
            years.add(String.valueOf(LocalDate.now().getYear()));
        }
        return List.copyOf(years);
    }

    /** 목록 화면: 연도(선택) + 지자체명 검색(선택)으로 필터링한 지자체별 등록건수 + 그 연도의
     *  월별 합계(전체 지자체 합산, 상단 요약용). */
    public ListResult search(String year, String locgovNmKeyword) {
        List<InterestLocgov> all = interestLocgovRepository.findAll();
        List<InterestLocgov> filtered = all.stream()
                .filter(r -> year == null || year.isBlank()
                        || (r.getRegistDe() != null && r.getRegistDe().startsWith(year)))
                .toList();

        Map<String, Locgov> locgovByCode = new LinkedHashMap<>();
        locgovRepository.findAll().forEach(l -> locgovByCode.put(l.getLocgovCode(), l));

        Map<String, Long> countByLocgov = new LinkedHashMap<>();
        for (InterestLocgov r : filtered) {
            countByLocgov.merge(r.getLocgovCode(), 1L, Long::sum);
        }

        List<LocgovRow> rows = countByLocgov.entrySet().stream()
                .map(e -> {
                    Locgov l = locgovByCode.get(e.getKey());
                    String nm = l != null ? l.getLocgovNm() : e.getKey();
                    String upperNm = l != null ? l.getUpperLocgovNm() : null;
                    return new LocgovRow(e.getKey(), nm, upperNm, e.getValue());
                })
                .filter(r -> locgovNmKeyword == null || locgovNmKeyword.isBlank()
                        || (r.locgovNm() != null && r.locgovNm().contains(locgovNmKeyword.trim())))
                .sorted(Comparator.comparingLong(LocgovRow::count).reversed())
                .toList();

        Map<String, Long> monthlyTotals = new LinkedHashMap<>();
        for (InterestLocgov r : filtered) {
            if (r.getRegistDe() != null && r.getRegistDe().length() >= 6) {
                monthlyTotals.merge(r.getRegistDe().substring(0, 6), 1L, Long::sum);
            }
        }
        List<MonthlyStat> monthly = monthlyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyStat(e.getKey(), e.getValue()))
                .toList();

        long total = filtered.size();
        return new ListResult(total, rows, monthly);
    }

    /** 상세 화면: 특정 지자체의 월별 등록 추이(연도 선택 시 그 연도만, 없으면 전체 기간). */
    public DetailResult detail(String locgovCode, String year) {
        List<InterestLocgov> rows = interestLocgovRepository.findAll().stream()
                .filter(r -> locgovCode.equals(r.getLocgovCode()))
                .filter(r -> year == null || year.isBlank()
                        || (r.getRegistDe() != null && r.getRegistDe().startsWith(year)))
                .toList();

        Map<String, Long> monthlyTotals = new LinkedHashMap<>();
        for (InterestLocgov r : rows) {
            if (r.getRegistDe() != null && r.getRegistDe().length() >= 6) {
                monthlyTotals.merge(r.getRegistDe().substring(0, 6), 1L, Long::sum);
            }
        }
        List<MonthlyStat> monthly = monthlyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyStat(e.getKey(), e.getValue()))
                .toList();

        Locgov l = locgovRepository.findById(locgovCode).orElse(null);
        String locgovNm = l != null ? l.getLocgovNm() : locgovCode;
        String upperLocgovNm = l != null ? l.getUpperLocgovNm() : null;

        return new DetailResult(locgovCode, locgovNm, upperLocgovNm, rows.size(), monthly);
    }

    public record LocgovRow(String locgovCode, String locgovNm, String upperLocgovNm, long count) {
    }

    public record MonthlyStat(String yearMonth, long count) {
    }

    public record ListResult(long total, List<LocgovRow> byLocgov, List<MonthlyStat> monthly) {
    }

    public record DetailResult(String locgovCode, String locgovNm, String upperLocgovNm, long total,
                                List<MonthlyStat> monthly) {
    }
}
