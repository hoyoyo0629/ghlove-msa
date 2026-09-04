package com.ghlove.admin.web;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.service.DataBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** storefront(Vue3 SPA)용 고객센터 "자료실" JSON API - {@link DataBoardController}(Thymeleaf)의
 * `/data-board` GET/`/data-board/{id}` GET과 완전히 같은 조합. 첨부파일 다운로드
 * (`/data-board/file-download/{fileId}`)는 이미 순수 파일 스트리밍 응답이라 프론트가
 * 그대로 재사용한다(여기서 다시 만들지 않음). */
@RestController
@RequiredArgsConstructor
public class DataBoardApiController {

    private final DataBoardService dataBoardService;

    public record DataBoardRowDto(Integer dataId, String subject, boolean noticeFlag, String fileType,
                                   String displayDate, Integer hits, int no) {
    }

    public record DataBoardListResponse(List<DataBoardRowDto> boards, int totalCount, int currentPage, int totalPages) {
    }

    @GetMapping("/api/data-board")
    public DataBoardListResponse list(@RequestParam(required = false, defaultValue = "SUBJECT") String where,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false, defaultValue = "CREATED_DATE__DESC") String sort,
                                       @RequestParam(required = false, defaultValue = "10") int size,
                                       @RequestParam(required = false, defaultValue = "1") int page) {
        var all = dataBoardService.search(where, q, sort);
        int pageSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<DataBoard> pageItems = all.subList(from, to);

        List<DataBoardRowDto> rows = new java.util.ArrayList<>();
        for (int i = 0; i < pageItems.size(); i++) {
            DataBoard b = pageItems.get(i);
            String fileType = dataBoardService.filesOf(b.getDataId()).stream().findFirst()
                    .map(DataBoardFile::getFileTy).orElse(null);
            int no = all.size() - ((currentPage - 1) * pageSize) - i;
            rows.add(new DataBoardRowDto(b.getDataId(), b.getSubject(), "Y".equals(b.getNoticeFlag()), fileType,
                    formatDate(b.getCreatedDate()), b.getHits(), no));
        }
        return new DataBoardListResponse(rows, all.size(), currentPage, Math.max(totalPages, 1));
    }

    public record DataBoardFileDto(String dataFileId, String orgFileName) {
    }

    public record DataBoardDetailDto(Integer dataId, String subject, String content, String displayDate,
                                      Integer hits, List<DataBoardFileDto> files) {
    }

    @GetMapping("/api/data-board/{id}")
    public ResponseEntity<DataBoardDetailDto> detail(@PathVariable Integer id) {
        DataBoard board;
        try {
            board = dataBoardService.findOrThrow(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).build();
        }
        dataBoardService.addHit(id);
        var files = dataBoardService.filesOf(id).stream()
                .map(f -> new DataBoardFileDto(f.getDataFileId(), f.getOrgFileName()))
                .toList();
        return ResponseEntity.ok(new DataBoardDetailDto(board.getDataId(), board.getSubject(), board.getContent(),
                formatDate(board.getCreatedDate()), board.getHits(), files));
    }

    private static String formatDate(String createdDate) {
        if (createdDate == null || createdDate.length() < 8) {
            return "";
        }
        return createdDate.substring(0, 4) + "-" + createdDate.substring(4, 6) + "-" + createdDate.substring(6, 8);
    }
}
