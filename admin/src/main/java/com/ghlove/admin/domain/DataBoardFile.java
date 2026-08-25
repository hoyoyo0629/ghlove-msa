package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** AS-IS OP_DATA_BOARD_FILE - fileName은 디스크 저장 파일명(uploads/admin/data-board/ 하위),
 * orgFileName은 다운로드 시 사용자에게 보여줄 실제 원본 파일명이다. */
@Entity
@Table(name = "OP_DATA_BOARD_FILE")
@Getter
@Setter
public class DataBoardFile {

    @Id
    @Column(name = "DATA_FILE_ID")
    private String dataFileId;

    @Column(name = "DATA_ID")
    private Integer dataId;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_TY")
    private String fileTy;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "ORG_FILE_NAME")
    private String orgFileName;
}
