package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** AS-IS data-board/list.html(자료실) - OP_DATA_BOARD는 원래 raw SQL로만 채워지던
 * 테이블이라 시딩된 기존 행은 ID가 임의값이었다. 관리자 CRUD(DataBoardAdminController)
 * 추가를 위해 op_data_board_id_seq(2000부터 시작, 기존 최대값보다 충분히 큼)를 새로
 * 만들어 매핑했다 - 이후 등록되는 행만 시퀀스를 쓰고 기존 행 ID는 그대로 유지된다. */
@Entity
@Table(name = "OP_DATA_BOARD")
@Getter
@Setter
public class DataBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataBoardIdSeq")
    @SequenceGenerator(name = "dataBoardIdSeq", sequenceName = "op_data_board_id_seq", allocationSize = 1)
    @Column(name = "DATA_ID")
    private Integer dataId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "HITS")
    private Integer hits;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "NOTICE_FLAG")
    private String noticeFlag;

    @Column(name = "USE_YN")
    private String useYn;
}
