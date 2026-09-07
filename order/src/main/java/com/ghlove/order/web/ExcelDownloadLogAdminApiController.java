package com.ghlove.order.web;

import com.ghlove.order.domain.ExcelDownloadLog;
import com.ghlove.order.repository.ExcelDownloadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 콘솔의 "엑셀다운로드 로그" 화면(ExcelDownloadLogAdminController, admin 서비스
 * /admin/excel-download-logs)이 조회하는 cross-service API. 실제 로그 적재는
 * OrderAdminService.exportCsv()가 이미 하고 있다(주문목록 CSV 다운로드 시 사유 입력 강제 +
 * OD_EXCEL_DOWNLOAD_LOG 적재) - 이 컨트롤러는 그 이력을 admin이 읽어갈 수 있게 조회만 연다.
 * AdminApiAuthInterceptor(공유시크릿)로 게이트된다(order의 WebConfig 참고).
 */
@RestController
@RequiredArgsConstructor
public class ExcelDownloadLogAdminApiController {

    private final ExcelDownloadLogRepository excelDownloadLogRepository;

    @GetMapping("/api/admin/excel-download-logs")
    public List<ExcelDownloadLogDto> list() {
        return excelDownloadLogRepository.findAllByOrderByDownloadLogIdDesc().stream()
                .map(ExcelDownloadLogDto::of)
                .toList();
    }

    public record ExcelDownloadLogDto(Long downloadLogId, Long managerId, String managerName, String downloadReason,
                                       String searchCondition, Integer rowCount, String createdDate) {
        static ExcelDownloadLogDto of(ExcelDownloadLog log) {
            return new ExcelDownloadLogDto(log.getDownloadLogId(), log.getManagerId(), log.getManagerName(),
                    log.getDownloadReason(), log.getSearchCondition(), log.getRowCount(),
                    log.getCreatedDate() == null ? null : log.getCreatedDate().toString());
        }
    }
}
