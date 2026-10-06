package com.ghlove.admin.service;

import com.ghlove.admin.domain.CmntyFile;
import com.ghlove.admin.domain.CmntyRpstr;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntyFileRepository;
import com.ghlove.admin.repository.CmntyRpstrAdminRepository;
import com.ghlove.admin.repository.CmntyRpstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 자료실 (메뉴 11402) - AS-IS {@code CmntyServiceImpl}의 {@code *Rpstr*} 메서드 이식
 * ({@code opmanager/community/databoard/*}, 컨트롤러는 {@code LocGovDataBoardManagerController}).
 * 지자체 담당자끼리 자료를 공유하는 게시판이다.
 *
 * <p><b>다른 커뮤니티 게시판과 다른 점</b>: <b>댓글도 비밀글도 없다</b>(표에 컬럼이 없다).
 * 본문 첨부만 있고, 등록/수정 화면이 <b>하나의 템플릿</b>을 공유한다(AS-IS도 edit이 form.jsp를 쓴다).
 *
 * <p><b>★ TO-BE {@code /admin/data-board}(메뉴 5107 고객센터 자료실)와 전혀 다른 화면이다</b> -
 * 그쪽은 {@code OP_DATA_BOARD}(시민 대상)이고 이쪽은 {@code G_CMNTY_RPSTR}(담당자 대상)이다.
 *
 * <p><b>AS-IS 규칙 그대로</b>: 게시글 삭제는 소프트 삭제인데 <b>첨부 행은 건드리지 않는다</b>
 * (AS-IS deleteRpstr이 게시글만 UPDATE한다). 수정은 제목·내용·상단공지만 바꾸고 첨부는 더하기만 한다.
 */
@Service
@RequiredArgsConstructor
public class CmntyRpstrAdminService {

    /** AS-IS create 화면이 내려주는 role - 자료실은 <b>ROLE_ADMIN_10까지</b> 포함한다(SR은 1~6). */
    private static final List<String> SCREEN_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2",
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10");

    public static final String SUBDIR = "cmnty/databoard";

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CmntyRpstrAdminRepository rpstrAdminRepository;
    private final CmntyRpstrRepository rpstrRepository;
    private final CmntyFileRepository fileRepository;
    private final CmntyFileStorageService fileStorageService;
    private final LocgovClient locgovClient;

    public record RpstrRow(Long rpstrId, String rpstrTtl, String noticeYn, Long inqCnt,
                           String userName, String authority, String upperLocgovNm, String locgovNm,
                           LocalDateTime frstCrtDt, Long attachedFileCnt) {
    }

    public record RpstrDetail(Long rpstrId, String rpstrTtl, String rpstrCn, String noticeYn,
                              Long inqCnt, Long frstCrtId, String userName, String authority,
                              String upperLocgovNm, String locgovNm, LocalDateTime frstCrtDt) {
    }

    public record FileRow(Long fileId, String orgnlAtchFileNm, String fileSize,
                          LocalDateTime frstCrtDt, Long frstCrtId) {
    }

    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query) {
        return rpstrAdminRepository.count(locgovCode, startDt, endDt, searchRole, where, query);
    }

    public List<RpstrRow> list(String locgovCode, String startDt, String endDt, String searchRole,
                               String where, String query, int offset, int limit) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyRpstrAdminRepository.Row> rows = rpstrAdminRepository.list(locgovCode, startDt,
                endDt, searchRole, where, query, offset, limit);
        List<RpstrRow> result = new ArrayList<>(rows.size());
        for (CmntyRpstrAdminRepository.Row r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new RpstrRow(r.rpstrId(), r.rpstrTtl(), r.noticeYn(), r.inqCnt(),
                    r.userName(), r.authority(), names[0], names[1], r.frstCrtDt(),
                    r.attachedFileCnt()));
        }
        return result;
    }

    /** AS-IS는 상세 진입에서 조회수를 먼저 올린다(컨트롤러가 호출). */
    public void increaseInqCnt(long rpstrId) {
        rpstrAdminRepository.updateInqCnt(rpstrId);
    }

    /** AS-IS getRpstrDetail. 글이 없으면 {@code null}. */
    public RpstrDetail detail(long rpstrId) {
        CmntyRpstrAdminRepository.DetailRow row = rpstrAdminRepository.detail(rpstrId);
        if (row == null) {
            return null;
        }
        String[] names = namesOf(locgovNames(), row.locgovCode());
        return new RpstrDetail(row.rpstrId(), row.rpstrTtl(), row.rpstrCn(), row.noticeYn(),
                row.inqCnt(), row.frstCrtId(), row.userName(), row.authority(), names[0], names[1],
                row.frstCrtDt());
    }

    /** AS-IS getRpstrfileList. */
    public List<FileRow> fileList(long rpstrId) {
        List<FileRow> result = new ArrayList<>();
        for (CmntyFile f : fileRepository.findByRpstrIdAndUseYnOrderByAtchFileSeqAsc(rpstrId, USE_Y)) {
            result.add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                    CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                    f.getFrstCrtId()));
        }
        return result;
    }

    /** AS-IS insertRpstr - 등록 후 첨부 업로드. */
    @Transactional
    public void insert(String rpstrTtl, String rpstrCn, String noticeYn, MultipartFile[] files,
                       Manager writer) {
        CmntyRpstr rpstr = new CmntyRpstr();
        rpstr.setRpstrTtl(rpstrTtl);
        rpstr.setRpstrCn(CmntyText.decode(rpstrCn));
        rpstr.setNoticeYn(noticeYn);
        rpstr.setUseYn(USE_Y);
        rpstr.setInqCnt(0L);
        rpstr.setFrstCrtId(writer.getUserId());
        rpstr.setFrstCrtDt(LocalDateTime.now());
        rpstrRepository.save(rpstr);

        storeFiles(rpstr.getRpstrId(), files, writer);
    }

    /**
     * AS-IS updateRpstr - 제목·내용·상단공지만 바꾼다.
     *
     * <p><b>AS-IS 결함 - 고쳤다</b>: 다른 게시판과 달리 AS-IS 자료실 수정에도 작성자 확인이 없어
     * URL로 남의 글을 고칠 수 있다. 화면은 작성자에게만 수정 버튼을 보여주므로 정상 흐름은 그대로다.
     */
    @Transactional
    public void update(long rpstrId, String rpstrTtl, String rpstrCn, String noticeYn,
                       MultipartFile[] files, Manager editor) {
        CmntyRpstr rpstr = aliveRpstr(rpstrId);
        requireOwner(rpstr.getFrstCrtId(), editor, "본인 게시글만 수정 가능합니다.");

        rpstr.setRpstrTtl(rpstrTtl);
        rpstr.setRpstrCn(CmntyText.decode(rpstrCn));
        rpstr.setNoticeYn(noticeYn);
        rpstr.setLastMdfcnId(editor.getUserId());
        rpstr.setLastMdfcnDt(LocalDateTime.now());
        rpstrRepository.save(rpstr);

        storeFiles(rpstrId, files, editor);
    }

    /** AS-IS deleteRpstr - 게시글만 소프트 삭제(첨부는 그대로 둔다). 작성자 확인은 위와 같은 이유로 추가. */
    @Transactional
    public void delete(long rpstrId, Manager editor) {
        CmntyRpstr rpstr = aliveRpstr(rpstrId);
        requireOwner(rpstr.getFrstCrtId(), editor, "본인 게시글만 삭제 가능합니다.");

        rpstr.setUseYn(USE_N);
        rpstr.setLastMdfcnId(editor.getUserId());
        rpstr.setLastMdfcnDt(LocalDateTime.now());
        rpstrRepository.save(rpstr);
    }

    /** AS-IS deleteFileByFileId. */
    @Transactional
    public void deleteFile(long fileId, Manager editor) {
        CmntyFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        CmntyRpstr rpstr = aliveRpstr(file.getRpstrId());
        requireOwner(rpstr.getFrstCrtId(), editor, "본인 게시글의 첨부만 삭제 가능합니다.");

        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        fileRepository.save(file);
    }

    public CmntyFile file(long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    /** AS-IS create 화면이 내려주는 role. */
    public String screenRole(Manager viewer) {
        if (viewer == null || viewer.getAuthority() == null) {
            return "";
        }
        return SCREEN_ROLES.contains(viewer.getAuthority()) ? viewer.getAuthority() : "";
    }

    private void storeFiles(Long rpstrId, MultipartFile[] files, Manager writer) {
        if (files == null) {
            return;
        }
        for (MultipartFile multipartFile : files) {
            CmntyFileStorageService.Stored stored = fileStorageService.store(multipartFile, SUBDIR);
            if (stored == null) {
                continue;
            }
            CmntyFile file = new CmntyFile();
            file.setRpstrId(rpstrId);
            file.setOrgnlAtchFileNm(stored.orgnlAtchFileNm());
            file.setAtchFileNm(stored.atchFileNm());
            file.setAtchFileExtnNm(stored.atchFileExtnNm());
            file.setAtchFileSz(stored.atchFileSz());
            file.setAtchFileSeq(fileRepository.nextSeq(rpstrId));
            file.setAtchFilePathNm(stored.atchFilePathNm());
            file.setUseYn(USE_Y);
            file.setFrstCrtId(writer.getUserId());
            file.setFrstCrtDt(LocalDateTime.now());
            fileRepository.save(file);
        }
    }

    private CmntyRpstr aliveRpstr(long rpstrId) {
        CmntyRpstr rpstr = rpstrRepository.findById(rpstrId)
                .orElseThrow(() -> new CmntyException("해당 글은 존재하지 않습니다."));
        if (!USE_Y.equals(rpstr.getUseYn())) {
            throw new CmntyException("해당 글은 존재하지 않습니다.");
        }
        return rpstr;
    }

    private static void requireOwner(Long ownerId, Manager actor, String message) {
        if (actor == null || actor.getUserId() == null || !actor.getUserId().equals(ownerId)) {
            throw new CmntyException(message);
        }
    }

    private Map<String, String[]> locgovNames() {
        Map<String, String[]> map = new HashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.put(l.locgovCode(), new String[]{l.upperLocgovNm(), l.locgovNm()});
        }
        return map;
    }

    private static String[] namesOf(Map<String, String[]> locgovNames, String locgovCode) {
        String[] names = locgovCode == null ? null : locgovNames.get(locgovCode);
        if (names == null) {
            return new String[]{"", ""};
        }
        return new String[]{names[0] == null ? "" : names[0], names[1] == null ? "" : names[1]};
    }
}
