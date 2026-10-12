package com.ghlove.admin.service;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.repository.DataBoardAdminRepository;
import com.ghlove.admin.repository.DataBoardFileRepository;
import com.ghlove.admin.repository.DataBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 자료실 관리 (메뉴 5107) - AS-IS {@code DataboardManagerController} +
 * {@code DataboardServiceImpl}({@code insertDataboard}/{@code updateDataboard}/
 * {@code deleteDataboard}/{@code databoardListDelete}/{@code deleteItemImageByItemId}) 이식.
 *
 * <p><b>첨부 규칙(AS-IS 서비스 본문 그대로)</b>: 다중 업로드({@code detailImageFiles[]}),
 * 확장자 16종·50MB({@link DataBoardFileStorageService}), 저장명
 * {@code yyyyMMddHHmmssSSSS_원본명}, {@code ORDERING}은 글당 {@code MAX+1},
 * <b>수정은 기존 첨부를 지우지 않고 추가만 한다</b>(지우려면 첨부 옆 [x]를 눌러야 한다).
 */
@Service
@RequiredArgsConstructor
public class DataBoardAdminService {

    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String NOTICE_FLAG_DEFAULT = "N";

    private final DataBoardAdminRepository dataBoardAdminRepository;
    private final DataBoardRepository dataBoardRepository;
    private final DataBoardFileRepository dataBoardFileRepository;
    private final DataBoardFileStorageService fileStorageService;

    public int count(String where, String query, String startCreateDate, String endCreateDate) {
        return dataBoardAdminRepository.count(where, query, startCreateDate, endCreateDate);
    }

    public List<DataBoard> list(String where, String query, String startCreateDate, String endCreateDate,
                                 int offset, int limit) {
        return dataBoardAdminRepository.list(where, query, startCreateDate, endCreateDate, offset, limit);
    }

    public DataBoard dataBoard(Integer dataId) {
        return dataBoardRepository.findById(dataId)
                .orElseThrow(() -> new ContentException("자료를 찾을 수 없습니다."));
    }

    public List<DataBoardFile> filesOf(Integer dataId) {
        return dataBoardFileRepository.findByDataIdOrderByOrdering(dataId);
    }

    /** AS-IS {@code insertDataboard} - 본문 저장 후 첨부를 하나씩 저장한다. */
    @Transactional
    public DataBoard create(String subject, String content, String noticeFlag, String useYn,
                            String userName, MultipartFile[] files) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        DataBoard board = new DataBoard();
        board.setSubject(subject);
        board.setContent(content);
        board.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        board.setUseYn(useYn);
        board.setUserName(userName);
        board.setHits(0);
        board.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        DataBoard saved = dataBoardRepository.save(board);

        attachAll(saved.getDataId(), files);
        return saved;
    }

    /**
     * AS-IS {@code updateDataboard} - 본문을 고치고 올라온 첨부가 있으면 <b>덧붙인다</b>.
     *
     * <p>AS-IS 매퍼는 화면에 입력칸이 없는 {@code URL}·{@code TARGET_OPTION}·{@code REL_OPTION}·
     * {@code VISIBLE_TYPE}·{@code SELLER_SELECT_FLAG}까지 UPDATE해 수정할 때마다 NULL로
     * 덮어쓴다(공지사항 5111과 같은 결함). 엔티티에 두지 않아 건드리지 않는다.
     */
    @Transactional
    public DataBoard update(Integer dataId, String subject, String content, String noticeFlag,
                            String useYn, String userName, MultipartFile[] files) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        DataBoard board = dataBoard(dataId);
        board.setSubject(subject);
        board.setContent(content);
        board.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        board.setUseYn(useYn);
        board.setUserName(userName);
        dataBoardRepository.save(board);

        attachAll(dataId, files);
        return board;
    }

    /**
     * AS-IS {@code deleteDataboard}는 {@code OP_DATA_BOARD} 한 행만 지워서 첨부 행과 디스크
     * 파일이 <b>고아로 남는다</b>. 그 결함은 옮기지 않고 첨부까지 같이 정리한다.
     */
    @Transactional
    public void delete(Integer dataId) {
        List<DataBoardFile> files = filesOf(dataId);
        files.forEach(f -> fileStorageService.delete(f.getFileName()));
        dataBoardFileRepository.deleteAll(files);
        dataBoardRepository.delete(dataBoard(dataId));
    }

    /**
     * AS-IS {@code databoardListDelete} - 요청 건수와 삭제 건수가 같을 때만 "SUCC"다.
     * 공지사항(5110·5111)과 달리 불일치해도 <b>예외를 던지지 않고</b> "FAIL"을 돌려준다.
     */
    @Transactional
    public String deleteList(List<Integer> dataIds) {
        if (dataIds == null || dataIds.isEmpty()) {
            return "FAIL";
        }
        int deleted = 0;
        for (Integer id : dataIds) {
            if (dataBoardRepository.existsById(id)) {
                delete(id);
                deleted++;
            }
        }
        return deleted == dataIds.size() ? "SUCC" : "FAIL";
    }

    /** AS-IS {@code deleteItemImageByItemId} - 디스크 파일을 지우고 행도 지운다. */
    @Transactional
    public void deleteFile(Integer dataFileId) {
        dataBoardFileRepository.findById(dataFileId).ifPresent(f -> {
            fileStorageService.delete(f.getFileName());
            dataBoardFileRepository.delete(f);
        });
    }

    public DataBoardFile file(Integer dataFileId) {
        return dataBoardFileRepository.findById(dataFileId)
                .orElseThrow(() -> new ContentException("첨부파일이 존재하지 않습니다."));
    }

    private void attachAll(Integer dataId, MultipartFile[] files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            // AS-IS: 원본 파일명이 비어 있으면(빈 file input) 건너뛴다
            if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }
            String storedName = fileStorageService.store(file);

            DataBoardFile entity = new DataBoardFile();
            entity.setDataFileId(dataBoardAdminRepository.nextFileId());
            entity.setDataId(dataId);
            entity.setFileName(storedName);
            entity.setFileTy(extensionOf(file.getOriginalFilename()));
            entity.setOrgFileName(file.getOriginalFilename());
            entity.setOrdering(nextOrdering(dataId));
            entity.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
            dataBoardFileRepository.save(entity);
        }
    }

    /** AS-IS insertDataboardFile: {@code (SELECT IFNULL(MAX(ORDERING)+1, 1) ... WHERE DATA_ID = ?)}. */
    private int nextOrdering(Integer dataId) {
        return filesOf(dataId).stream()
                .map(DataBoardFile::getOrdering)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private static String extensionOf(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase();
    }
}
