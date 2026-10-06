package com.ghlove.admin.web;

import com.ghlove.admin.repository.BatchLogRepository;
import com.ghlove.admin.web.support.BatchLogParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 배치 실행로그 조회 (메뉴 7209) - AS-IS saleson.shop.batchlog.BatchlogManagerController
 * (/opmanager/batch-log/list) 재현. TO-BE에는 이 화면이 없었고 op_menu 7209의 menu_url도 비어
 * 있었다({@code migration-admin-menu-7209-batch-log.sql}로 /batch-log를 넣었다).
 *
 * 작업명 검색 + 실행날짜 범위 + 7컬럼(작업명/배치구분/실행날짜/시작시간/종료시간/결과/오류내용).
 * AS-IS와 같이 날짜가 둘 다 비어 있으면 <b>오늘</b>로 채워서 조회한다.
 *
 * <b>읽는 표 {@code admin.op_batch_execution}에 아직 쌓이는 것이 없다</b> - AS-IS는
 * {@code saleson.common.scheduling}의 배치 실행이 끝날 때마다 이 표에 결과를 MERGE하는데,
 * TO-BE에는 그 기록 지점이 없다(화면만 먼저 준비된 상태 - send-mail-log와 같은 상황). 기록 쪽은
 * 각 서비스의 배치 실행 지점에 붙여야 해서 별도 과제로 남긴다.
 */
@Controller
@RequiredArgsConstructor
public class BatchLogAdminController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BatchLogRepository batchLogRepository;

    @RequestMapping(value = { "/batch-log", "/batch-log/list" },
            method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("batchLogParam") BatchLogParam param,
                       HttpServletRequest request, Model model) {
        // AS-IS: 시작일·종료일이 둘 다 비면 오늘로 채운다
        if (isBlank(param.getSearchStartDate()) && isBlank(param.getSearchEndDate())) {
            String today = DATE_FORMAT.format(LocalDate.now());
            param.setSearchStartDate(today);
            param.setSearchEndDate(today);
        }
        if (param.getItemsPerPage() <= 0) {
            param.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }

        // AS-IS는 searchType이 JOBNAME일 때만 작업명 LIKE를 건다
        String jobNameQuery = "JOBNAME".equals(param.getSearchType()) ? param.getQuery() : null;
        List<BatchLogRepository.BatchLogRow> all = batchLogRepository.getBatchLogList(
                jobNameQuery, param.getSearchStartDate(), param.getSearchEndDate());

        Pagination pagination = Pagination.of(all.size(), param.getPage(), param.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("batchLogList", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("totalCount", all.size());
        model.addAttribute("pagination", pagination);
        return "batch-log/list";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
