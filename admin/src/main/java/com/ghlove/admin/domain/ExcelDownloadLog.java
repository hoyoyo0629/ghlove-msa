package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** admin 콘솔 자체(회원/통계 등)에서 실행하는 엑셀 다운로드 이력 - order 서비스는 자기
 * 소유 데이터(주문목록)에 대한 다운로드 이력을 OD_EXCEL_DOWNLOAD_LOG에 이미 별도로 쌓고
 * 있다(order.ExcelDownloadLog). 이 테이블은 그 외 admin 서비스가 직접 소유한 데이터를
 * 관리자가 내려받을 때를 대비한 그릇으로, ExcelDownloadLogAdminController가 두 출처를
 * 하나의 화면(/admin/excel-download-logs)으로 합쳐 보여준다. */
@Entity
@Table(name = "OP_EXCEL_DOWNLOAD_LOG")
@Getter
@Setter
@NoArgsConstructor
public class ExcelDownloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOWNLOAD_LOG_ID")
    private Long downloadLogId;

    @Column(name = "MANAGER_ID")
    private Long managerId;

    @Column(name = "MANAGER_NAME")
    private String managerName;

    @Column(name = "DOWNLOAD_REASON")
    private String downloadReason;

    @Column(name = "SEARCH_CONDITION")
    private String searchCondition;

    @Column(name = "ROW_COUNT")
    private Integer rowCount;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
