package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 기부금 운영현황 통계 (AS-IS opmanager give/statistics/operate) - 지자체×사용용도(BSNS_PURPS_CODE)
 * 지출 집계. 실제 지출내역은 donation G_CTBNY_OPRATN에 있고 {@link CtbnyOpratnClient}가 cross-service
 * API로 조회한다. AS-IS 매퍼(give-statistics-mapper.xml getGiveOperateList/getGiveOperateListByLocgov)는
 * CNTR_USE 코드가 '100~400' 4종이라 AMT_100..400을 하드코딩했지만, TO-BE는 BSNS_PURPS_CODE가 6종
 * (VULNERABLE/YOUTH/CULTURE/COMMUNITY/WELFARE/ETC)이고 AS-IS JSP가 useList/codeList로 동적 컬럼을
 * 렌더했으므로 여기서도 코드그룹 전체를 동적으로 집계한다(하드코딩 없음, [[copy-as-is-verbatim-never-invent]]).
 * 집계는 admin에서 수행(데이터 소량, 쓰기 대행 클라이언트 재사용으로 donation 무수정).
 */
@Service
@RequiredArgsConstructor
public class GiveOperateStatService {

    private static final String PURPS_CODE_TYPE = "BSNS_PURPS_CODE";

    private final CtbnyOpratnClient ctbnyOpratnClient;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;

    /** 사용용도 코드목록(ordering순, 사용중만) - AS-IS CodeUtils.getCodeList("CNTR_USE") 대응. 동적 컬럼의 근거. */
    public List<CodeLabel> codeList() {
        return commonCodeService.labelsOf(PURPS_CODE_TYPE).entrySet().stream()
                .map(e -> new CodeLabel(e.getKey(), e.getValue()))
                .toList();
    }

    /** 지자체별 용도별 지출 집계(AS-IS getGiveOperateList) - 지출이 없는 지자체도 0으로 전부 표시(LEFT JOIN G_LOCGOV). */
    public List<OperateListRow> operateList(String year, String locgovCode) {
        List<String> codes = codeList().stream().map(CodeLabel::id).toList();

        Map<String, Map<String, Long>> byLocgov = new java.util.HashMap<>();
        for (CtbnyOpratnClient.Row r : ctbnyOpratnClient.listAll()) {
            if (notBlank(year) && (r.expndtrDe() == null || !r.expndtrDe().startsWith(year))) {
                continue;
            }
            if (notBlank(locgovCode) && !locgovCode.equals(r.locgovCode())) {
                continue;
            }
            byLocgov.computeIfAbsent(r.locgovCode(), k -> new java.util.HashMap<>())
                    .merge(r.bsnsPurpsCode(), amt(r), Long::sum);
        }

        List<OperateListRow> out = new ArrayList<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            if (notBlank(locgovCode) && !locgovCode.equals(l.locgovCode())) {
                continue;
            }
            Map<String, Long> sums = byLocgov.getOrDefault(l.locgovCode(), Map.of());
            Map<String, Long> amts = new LinkedHashMap<>();
            long total = 0;
            for (String code : codes) {
                long v = sums.getOrDefault(code, 0L);
                amts.put(code, v);
                total += v;
            }
            out.add(new OperateListRow(l.upperLocgovCode(), l.upperLocgovNm(), l.locgovCode(), l.locgovNm(), amts, total));
        }
        out.sort(Comparator.comparing(OperateListRow::upperLocgovCode, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(OperateListRow::locgovCode, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    /** 특정 지자체의 연도별 용도별 지출(금액·건수) + 합계행(AS-IS getGiveOperateListByLocgov, WITH ROLLUP). */
    public List<OperateDetailRow> operateDetailByLocgov(String locgovCode) {
        List<String> codes = codeList().stream().map(CodeLabel::id).toList();

        // 연도 desc 정렬 유지
        TreeMap<String, Map<String, long[]>> byYear = new TreeMap<>(Comparator.reverseOrder());
        for (CtbnyOpratnClient.Row r : ctbnyOpratnClient.listByLocgov(locgovCode)) {
            String y = (r.expndtrDe() != null && r.expndtrDe().length() >= 4) ? r.expndtrDe().substring(0, 4) : "0";
            Map<String, long[]> perCode = byYear.computeIfAbsent(y, k -> new java.util.HashMap<>());
            long[] ac = perCode.computeIfAbsent(r.bsnsPurpsCode(), k -> new long[2]);
            ac[0] += amt(r);     // 금액
            ac[1] += 1;          // 건수
        }

        List<OperateDetailRow> out = new ArrayList<>();
        long[] grandAmt = new long[codes.size()];
        long[] grandCnt = new long[codes.size()];
        for (Map.Entry<String, Map<String, long[]>> e : byYear.entrySet()) {
            Map<String, long[]> perCode = e.getValue();
            Map<String, Long> amts = new LinkedHashMap<>();
            Map<String, Long> cnts = new LinkedHashMap<>();
            long sumAmt = 0, sumCnt = 0;
            for (int i = 0; i < codes.size(); i++) {
                long[] ac = perCode.getOrDefault(codes.get(i), new long[2]);
                amts.put(codes.get(i), ac[0]);
                cnts.put(codes.get(i), ac[1]);
                sumAmt += ac[0];
                sumCnt += ac[1];
                grandAmt[i] += ac[0];
                grandCnt[i] += ac[1];
            }
            out.add(new OperateDetailRow(e.getKey(), amts, cnts, sumAmt, sumCnt));
        }

        // 합계행(AS-IS ROLLUP, cntrYear='0' → JS가 '합계'로 렌더)
        Map<String, Long> tAmt = new LinkedHashMap<>();
        Map<String, Long> tCnt = new LinkedHashMap<>();
        long gAmt = 0, gCnt = 0;
        for (int i = 0; i < codes.size(); i++) {
            tAmt.put(codes.get(i), grandAmt[i]);
            tCnt.put(codes.get(i), grandCnt[i]);
            gAmt += grandAmt[i];
            gCnt += grandCnt[i];
        }
        out.add(new OperateDetailRow("0", tAmt, tCnt, gAmt, gCnt));
        return out;
    }

    private static long amt(CtbnyOpratnClient.Row r) {
        return r.expndtrAmt() == null ? 0L : r.expndtrAmt().longValue();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public record CodeLabel(String id, String label) {
    }

    public record OperateListRow(String upperLocgovCode, String upperLocgovNm, String locgovCode, String locgovNm,
                                 Map<String, Long> amts, long expndtrSum) {
    }

    public record OperateDetailRow(String cntrYear, Map<String, Long> amts, Map<String, Long> cnts,
                                   long expndtrSum, long expndtrCnt) {
    }
}
