package com.ghlove.donation.web;

import com.ghlove.donation.repository.LevyLinkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 연계 로그 조회 (admin 메뉴 1413~1416)용 <b>조회 전용</b> API - AS-IS
 * {@code saleson.shop.log.LogManagerController}의 gif-seoul-buga / gif-seoul-sunap /
 * gif-stnd-buga / gif-stnd-sunap 네 화면이 읽던 표가 donation 소유라 여기서 내려준다.
 *
 * 실제 연계(쓰기)는 납부게이트웨이 이식 라운드 소관이고, 이 API는 조회만 한다.
 */
@RestController
@RequestMapping("/api/admin/levy-logs")
@RequiredArgsConstructor
public class LevyLinkLogAdminApiController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LevyLinkLogRepository levyLinkLogRepository;

    /** 1413 서울세외 부과연계 로그 - 등록일(연계시작일시) 범위는 AS-IS처럼 필수다. */
    @GetMapping("/seoul-buga")
    public List<LevyLinkLogRepository.SeoulBugaRow> seoulBuga(
            @RequestParam(required = false) String srchTxt,
            @RequestParam(required = false) String srchErrorCd,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return levyLinkLogRepository.seoulBugaList(srchTxt, srchErrorCd,
                startOfDay(startDate), endOfDay(endDate));
    }

    /** 1414 서울 수납연계 로그. */
    @GetMapping("/seoul-sunap")
    public List<LevyLinkLogRepository.SeoulSunapRow> seoulSunap(
            @RequestParam(required = false) String srchTxt,
            @RequestParam(required = false) String srchRstCd,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return levyLinkLogRepository.seoulSunapList(srchTxt, srchRstCd,
                startOfDay(startDate), endOfDay(endDate));
    }

    /** 1415 지방세외 부과연계 로그 - 날짜는 부과일자(LVY_YMD) yyyyMMdd 문자열 비교다(AS-IS 동일). */
    @GetMapping("/stnd-buga")
    public List<LevyLinkLogRepository.StndBugaRow> stndBuga(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String bugaStatusCd,
            @RequestParam(required = false) String linkRstCd,
            @RequestParam(required = false) String epayNo,
            @RequestParam(required = false) String srchlinkRstYn,
            @RequestParam(required = false) String srchpyrNm) {
        return levyLinkLogRepository.stndBugaList(startDate, endDate, bugaStatusCd, linkRstCd,
                epayNo, srchlinkRstYn, srchpyrNm);
    }

    /** 1416 지방세외 수납연계 로그 - 날짜는 수납일자(RCVMT_YMD) 문자열 비교다. */
    @GetMapping("/stnd-sunap")
    public List<LevyLinkLogRepository.StndSunapRow> stndSunap(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String epayNo) {
        return levyLinkLogRepository.stndSunapList(startDate, endDate, epayNo);
    }

    /** AS-IS는 날짜가 비면 오늘로 채워 보내므로 여기선 안전망으로만 둔다(둘 다 비면 전 구간). */
    private static LocalDateTime startOfDay(String yyyymmdd) {
        LocalDate date = parse(yyyymmdd);
        return date == null ? LocalDateTime.of(1900, 1, 1, 0, 0) : date.atStartOfDay();
    }

    private static LocalDateTime endOfDay(String yyyymmdd) {
        LocalDate date = parse(yyyymmdd);
        return date == null ? LocalDateTime.of(2999, 12, 31, 23, 59, 59)
                : date.atTime(LocalTime.of(23, 59, 59));
    }

    private static LocalDate parse(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(yyyymmdd, DAY);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
