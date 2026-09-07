package com.ghlove.admin.web;

import com.ghlove.admin.domain.ExcelDownloadLog;
import com.ghlove.admin.repository.ExcelDownloadLogRepository;
import com.ghlove.admin.service.OrderAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

/**
 * 엑셀다운로드 로그 (AS-IS opmanager/log의 LogManagerController exceldownload-log 재현) -
 * 관리자가 회원/주문/통계 등에서 엑셀(CSV)을 다운로드할 때마다 사유를 입력하고 그 이력을
 * 남기는 감사로그 조회 화면. 다운로드 자체는 각 도메인이 소유한 서비스에서 이미 일어나므로
 * (주문관리 CSV 다운로드는 order 서비스, OrderAdminService.exportCsv 참고) 이 화면은 그
 * 출처별 로그를 한 화면에서 조회하는 뷰어다. admin 서비스 자체 소유 데이터(회원/통계 등)의
 * 다운로드 기능이 생기면 OP_EXCEL_DOWNLOAD_LOG(admin 자체 테이블)에 같은 방식으로 적재하면
 * 되고, 여기는 그 두 출처를 CREATED_DATE 내림차순으로 합쳐서 보여준다.
 */
@Controller
@RequiredArgsConstructor
public class ExcelDownloadLogAdminController {

    private final ExcelDownloadLogRepository excelDownloadLogRepository;
    private final OrderAdminClient orderAdminClient;

    @GetMapping("/admin/excel-download-logs")
    public String list(Model model) {
        List<Row> rows = new java.util.ArrayList<>();
        for (ExcelDownloadLog log : excelDownloadLogRepository.findAllByOrderByDownloadLogIdDesc()) {
            rows.add(new Row("관리자콘솔", log.getManagerId(), log.getManagerName(), log.getDownloadReason(),
                    log.getSearchCondition(), log.getRowCount(),
                    log.getCreatedDate() == null ? null : log.getCreatedDate().toString()));
        }
        for (OrderAdminClient.ExcelDownloadLogRow log : orderAdminClient.excelDownloadLogs()) {
            rows.add(new Row("주문관리", log.managerId(), log.managerName(), log.downloadReason(),
                    log.searchCondition(), log.rowCount(), log.createdDate()));
        }
        rows.sort(Comparator.comparing((Row r) -> r.createdDate() == null ? "" : r.createdDate()).reversed());
        model.addAttribute("rows", rows);
        return "log/excel-download-log-list";
    }

    public record Row(String source, Long managerId, String managerName, String downloadReason,
                       String searchCondition, Integer rowCount, String createdDate) {
    }
}
