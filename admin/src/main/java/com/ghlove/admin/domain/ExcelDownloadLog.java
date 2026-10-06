package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <b>쓰이지 않는다 - AS-IS에 없는 표다.</b>
 *
 * 예전 TO-BE가 엑셀 다운로드 이력을 담으려고 만든 표인데, AS-IS는 그 이력을 개인정보
 * 접근로그({@code OP_PRIVACY_ACCESS_LOG})에 남기고 "엑셀다운로드 사유 관리"(메뉴 1411) 화면도
 * 그 표를 읽는다. 사용자 확인 후 기록 지점을 AS-IS와 같게 옮겼고(2026-10-03,
 * {@link com.ghlove.admin.service.PrivacyAccessLogService}), 이 엔티티/표는 과거 행을 잃지 않도록
 * 남겨만 둔다(표 삭제는 파괴적이라 사용자 판단 필요). 새 코드는 이걸 쓰지 말 것.
 */
@Deprecated
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
