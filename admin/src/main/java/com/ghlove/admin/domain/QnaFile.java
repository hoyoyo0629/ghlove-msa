package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 1:1 문의 첨부파일. FILE_NAME은 로컬 디스크에 저장된 실제 파일명(UUID 기반),
 *  ORG_FILE_NAME은 사용자가 업로드한 원본 파일명(다운로드 시 노출용). */
@Entity
@Table(name = "OP_QNA_FILE")
@Getter
@Setter
@NoArgsConstructor
public class QnaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQnaFileIdSeq")
    @SequenceGenerator(name = "opQnaFileIdSeq", sequenceName = "op_qna_file_qna_file_id_seq", allocationSize = 1)
    @Column(name = "QNA_FILE_ID")
    private Integer qnaFileId;

    @Column(name = "QNA_ID")
    private Integer qnaId;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "ORG_FILE_NAME")
    private String orgFileName;
}
