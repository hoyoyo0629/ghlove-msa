package com.ghlove.admin.service;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.repository.DataBoardFileRepository;
import com.ghlove.admin.repository.DataBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 고객센터 자료실 (AS-IS data-board/list.html). FAQ와 동일하게 시딩 규모가 작아
 * 검색/정렬은 인메모리로 처리한다. "파일명" 검색(where=ORG_FILE_NAME)은 첨부파일
 * 테이블을 조인해야 하므로 dataId 집합으로 필터링한다.
 */
@Service
@RequiredArgsConstructor
public class DataBoardService {

    private final DataBoardRepository dataBoardRepository;
    private final DataBoardFileRepository dataBoardFileRepository;

    public List<DataBoard> search(String where, String keyword, String sort) {
        List<DataBoard> all = dataBoardRepository.findByUseYn("Y");

        List<DataBoard> filtered = all;
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            if ("ORG_FILE_NAME".equals(where)) {
                filtered = all.stream()
                        .filter(d -> dataBoardFileRepository.findByDataIdOrderByOrdering(d.getDataId()).stream()
                                .anyMatch(f -> f.getOrgFileName() != null && f.getOrgFileName().contains(kw)))
                        .toList();
            } else {
                filtered = all.stream()
                        .filter(d -> d.getSubject() != null && d.getSubject().contains(kw))
                        .toList();
            }
        }

        Comparator<DataBoard> comparator = switch (sort == null ? "" : sort) {
            case "CREATED_DATE__ASC" -> Comparator.comparing(DataBoard::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "HITS__DESC" -> Comparator.comparing(DataBoard::getHits, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(DataBoard::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
        return filtered.stream().sorted(comparator).toList();
    }

    public DataBoard findOrThrow(Integer dataId) {
        return dataBoardRepository.findById(dataId)
                .orElseThrow(() -> new IllegalArgumentException("자료실 게시물이 존재하지 않습니다: " + dataId));
    }

    public List<DataBoardFile> filesOf(Integer dataId) {
        return dataBoardFileRepository.findByDataIdOrderByOrdering(dataId);
    }

    /** AS-IS는 상세 조회 시점에 조회수를 올린다(목록의 아코디언 방식인 FAQ와 다름). */
    @Transactional
    public void addHit(Integer dataId) {
        dataBoardRepository.findById(dataId).ifPresent(d -> d.setHits((d.getHits() == null ? 0 : d.getHits()) + 1));
    }

    public DataBoardFile fileOrThrow(String dataFileId) {
        return dataBoardFileRepository.findById(dataFileId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일이 존재하지 않습니다: " + dataFileId));
    }
}
