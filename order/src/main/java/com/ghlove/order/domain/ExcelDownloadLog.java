package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** admin 주문관리 콘솔 "엑셀다운로드" 이력 - AS-IS opmanager는 개인정보(수취인 주소/연락처
 * 등)가 포함된 다운로드마다 사유 입력을 강제하고 이력을 남긴다(개인정보보호법 접근·이용
 * 로그 요건). 이 프로젝트도 동일하게 사유 없이는 다운로드를 실행하지 않는다. */
@Entity
@Table(name = "OD_EXCEL_DOWNLOAD_LOG")
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
