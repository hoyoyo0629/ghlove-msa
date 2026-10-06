package com.ghlove.admin.service;

import com.ghlove.admin.domain.CmntyOffSrBbs;
import com.ghlove.admin.domain.CmntyOffSrBbsCmnt;
import com.ghlove.admin.domain.CmntyOffSrBbsCmntFile;
import com.ghlove.admin.domain.CmntyOffSrBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntyOffSrBbsAdminRepository;
import com.ghlove.admin.repository.CmntyOffSrBbsCmntFileRepository;
import com.ghlove.admin.repository.CmntyOffSrBbsCmntRepository;
import com.ghlove.admin.repository.CmntyOffSrBbsFileRepository;
import com.ghlove.admin.repository.CmntyOffSrBbsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 오프라인 담당자 SR 게시판 (메뉴 11406) - AS-IS {@code CmntyServiceImpl}의 {@code *OffSrBbs*}
 * 메서드 이식({@code opmanager/community/offSrBbs/*}). 오프라인 기부(은행 지점) 담당자의
 * 건의·오류 신고 게시판이고, 본문·댓글 양쪽에 첨부파일이 붙는 구조는
 * {@link CmntySrBbsAdminService}(11404)와 같다.
 *
 * <p><b>SR 게시판과 다른 점</b>은 작성자 소속 표기가 지자체가 아니라 <b>[은행명] 지점명</b>이라는 것뿐이다
 * (댓글에는 지자체 담당자도 달 수 있어 지자체명도 같이 내려준다) -
 * 자세한 조회 차이는 {@link CmntyOffSrBbsAdminRepository} 주석 참고.
 *
 * <p><b>AS-IS 결함도 같은 5건을 같은 방식으로 고쳤다</b>(수정 권한 없음 / 삭제된 글이 URL로 열림 /
 * 첨부 2개 이상이면 상세 500 / 댓글 첨부 크기 빈 값 / 댓글 수정자 ID null) -
 * 내용은 {@link CmntySrBbsAdminService} 클래스 주석에 적어 두었다.
 */
@Service
@RequiredArgsConstructor
public class CmntyOffSrBbsAdminService {

    private static final List<String> SYSTEM_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");

    /** AS-IS가 화면에 내려주는 role - 1~6만 값이 들어간다(오프라인 담당자는 빈 값이다). */
    private static final List<String> SCREEN_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2",
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    public static final String SUBDIR_BBS = "cmnty/off-sr-bbs";
    public static final String SUBDIR_CMNT = "cmnty/off-sr-bbs-cmnt";

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CmntyOffSrBbsAdminRepository offSrBbsAdminRepository;
    private final CmntyOffSrBbsRepository offSrBbsRepository;
    private final CmntyOffSrBbsFileRepository offSrBbsFileRepository;
    private final CmntyOffSrBbsCmntRepository cmntRepository;
    private final CmntyOffSrBbsCmntFileRepository cmntFileRepository;
    private final CmntyFileStorageService fileStorageService;
    private final LocgovClient locgovClient;

    public record BbsRow(Long bbsId, String bbsTtl, String noticeYn, String isSecret, Long inqCnt,
                         String userName, String authority, String upperLocgovNm, String locgovNm,
                         String bankNm, String psitnNm, LocalDateTime frstCrtDt,
                         LocalDateTime lastMdfcnDt, Long cmntCnt, Long attachedFileCnt) {
    }

    public record BbsDetail(Long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                            Long inqCnt, Long frstCrtId, String userName, String authority,
                            String upperLocgovNm, String locgovNm, String bankNm, String psitnNm,
                            LocalDateTime frstCrtDt, boolean secretBlocked) {
    }

    public record FileRow(Long fileId, String orgnlAtchFileNm, String fileSize,
                          LocalDateTime frstCrtDt, Long frstCrtId) {
    }

    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String upperLocgovNm, String locgovNm, String bankNm, String psitnNm,
                          Long frstCrtId, LocalDateTime frstCrtDt, List<FileRow> files) {
    }

    public int count(String startDt, String endDt, String searchRole, String shBank,
                     String where, String query) {
        return offSrBbsAdminRepository.count(startDt, endDt, searchRole, shBank, where, query);
    }

    public List<BbsRow> list(String startDt, String endDt, String searchRole, String shBank,
                             String where, String query, int offset, int limit) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyOffSrBbsAdminRepository.Row> rows = offSrBbsAdminRepository.list(startDt, endDt,
                searchRole, shBank, where, query, offset, limit);
        List<BbsRow> result = new ArrayList<>(rows.size());
        for (CmntyOffSrBbsAdminRepository.Row r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new BbsRow(r.bbsId(), r.bbsTtl(), r.noticeYn(), r.isSecret(), r.inqCnt(),
                    r.userName(), r.authority(), names[0], names[1], nvl(r.bankNm()), nvl(r.psitnNm()),
                    r.frstCrtDt(), r.lastMdfcnDt(), r.cmntCnt(), r.attachedFileCnt()));
        }
        return result;
    }

    public void increaseInqCnt(long bbsId) {
        offSrBbsAdminRepository.updateInqCnt(bbsId);
    }

    public BbsDetail detail(long bbsId, Manager viewer) {
        CmntyOffSrBbsAdminRepository.DetailRow row = offSrBbsAdminRepository.detail(bbsId);
        if (row == null) {
            return null;
        }
        boolean secretBlocked = false;
        if (USE_Y.equalsIgnoreCase(row.isSecret())) {
            secretBlocked = !isSystemAdmin(viewer) && !isOwner(row.frstCrtId(), viewer);
        }
        String[] names = namesOf(locgovNames(), row.locgovCode());
        return new BbsDetail(row.bbsId(), row.bbsTtl(), row.bbsCn(), row.noticeYn(), row.isSecret(),
                row.inqCnt(), row.frstCrtId(), row.userName(), row.authority(), names[0], names[1],
                nvl(row.bankNm()), nvl(row.psitnNm()), row.frstCrtDt(), secretBlocked);
    }

    public List<FileRow> fileList(long bbsId) {
        List<FileRow> result = new ArrayList<>();
        for (CmntyOffSrBbsFile f : offSrBbsFileRepository
                .findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            result.add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                    CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                    f.getFrstCrtId()));
        }
        return result;
    }

    public List<CmntRow> cmntList(long bbsId) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyOffSrBbsAdminRepository.CmntRow> rows = offSrBbsAdminRepository.cmntList(bbsId);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> cmntIds = rows.stream().map(CmntyOffSrBbsAdminRepository.CmntRow::cmntId).toList();
        Map<Long, List<FileRow>> filesByCmnt = new LinkedHashMap<>();
        for (CmntyOffSrBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
            filesByCmnt.computeIfAbsent(f.getCmntId(), key -> new ArrayList<>())
                    .add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                            CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                            f.getFrstCrtId()));
        }

        List<CmntRow> result = new ArrayList<>(rows.size());
        for (CmntyOffSrBbsAdminRepository.CmntRow r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new CmntRow(r.cmntId(), r.bbsId(), r.cmntCn(), r.userName(), r.authority(),
                    names[0], names[1], nvl(r.bankNm()), nvl(r.psitnNm()), r.frstCrtId(),
                    r.frstCrtDt(), filesByCmnt.getOrDefault(r.cmntId(), List.of())));
        }
        return result;
    }

    @Transactional
    public void insert(String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                       MultipartFile[] files, Manager writer) {
        CmntyOffSrBbs bbs = new CmntyOffSrBbs();
        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setUseYn(USE_Y);
        bbs.setInqCnt(0L);
        bbs.setFrstCrtId(writer.getUserId());
        bbs.setFrstCrtDt(LocalDateTime.now());
        offSrBbsRepository.save(bbs);

        storeBbsFiles(bbs.getBbsId(), files, writer);
    }

    @Transactional
    public void update(long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                       MultipartFile[] files, Manager editor) {
        CmntyOffSrBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 수정 가능합니다.");

        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(LocalDateTime.now());
        offSrBbsRepository.save(bbs);

        storeBbsFiles(bbsId, files, editor);
    }

    /** AS-IS deleteOffSrBbs - 댓글 첨부 → 댓글 → 본문 첨부 → 게시글 순으로 소프트 삭제. */
    @Transactional
    public void delete(long bbsId, Manager editor) {
        CmntyOffSrBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        List<CmntyOffSrBbsCmnt> comments = cmntRepository.findByBbsIdAndUseYn(bbsId, USE_Y);
        if (!comments.isEmpty()) {
            List<Long> cmntIds = comments.stream().map(CmntyOffSrBbsCmnt::getCmntId).toList();
            for (CmntyOffSrBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
                f.setUseYn(USE_N);
                f.setLastMdfcnId(editor.getUserId());
                f.setLastMdfcnDt(now);
                cmntFileRepository.save(f);
            }
            for (CmntyOffSrBbsCmnt c : comments) {
                c.setUseYn(USE_N);
                c.setLastMdfcnId(editor.getUserId());
                c.setLastMdfcnDt(now);
                cmntRepository.save(c);
            }
        }
        for (CmntyOffSrBbsFile f : offSrBbsFileRepository
                .findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            f.setUseYn(USE_N);
            f.setLastMdfcnId(editor.getUserId());
            f.setLastMdfcnDt(now);
            offSrBbsFileRepository.save(f);
        }

        bbs.setUseYn(USE_N);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(now);
        offSrBbsRepository.save(bbs);
    }

    @Transactional
    public void deleteBbsFile(long fileId, Manager editor) {
        CmntyOffSrBbsFile file = offSrBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        CmntyOffSrBbs bbs = aliveBbs(file.getBbsId());
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글의 첨부만 삭제 가능합니다.");

        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        offSrBbsFileRepository.save(file);
    }

    @Transactional
    public void addCmnt(long bbsId, String cmntCn, Manager writer) {
        aliveBbs(bbsId);
        CmntyOffSrBbsCmnt cmnt = new CmntyOffSrBbsCmnt();
        cmnt.setBbsId(bbsId);
        cmnt.setCmntCn(cmntCn);
        cmnt.setUseYn(USE_Y);
        cmnt.setFrstCrtId(writer.getUserId());
        cmnt.setFrstCrtDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    @Transactional
    public void updateCmnt(long cmntId, String cmntCn, Manager editor) {
        CmntyOffSrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 수정 가능합니다.");

        cmnt.setCmntCn(cmntCn);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    @Transactional
    public void deleteCmnt(long cmntId, Manager editor) {
        CmntyOffSrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        for (CmntyOffSrBbsCmntFile f : cmntFileRepository.findByCmntIdAndUseYn(cmntId, USE_Y)) {
            f.setUseYn(USE_N);
            f.setLastMdfcnId(editor.getUserId());
            f.setLastMdfcnDt(now);
            cmntFileRepository.save(f);
        }
        cmnt.setUseYn(USE_N);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(now);
        cmntRepository.save(cmnt);
    }

    @Transactional
    public long addCmntFiles(long cmntId, MultipartFile[] files, Manager writer) {
        CmntyOffSrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), writer, "본인 댓글에만 파일을 등록할 수 있습니다.");

        if (files != null) {
            for (MultipartFile multipartFile : files) {
                CmntyFileStorageService.Stored stored = fileStorageService.store(multipartFile, SUBDIR_CMNT);
                if (stored == null) {
                    continue;
                }
                CmntyOffSrBbsCmntFile file = new CmntyOffSrBbsCmntFile();
                file.setCmntId(cmntId);
                file.setOrgnlAtchFileNm(stored.orgnlAtchFileNm());
                file.setAtchFileNm(stored.atchFileNm());
                file.setAtchFileExtnNm(stored.atchFileExtnNm());
                file.setAtchFileSz(stored.atchFileSz());
                file.setAtchFileSeq(cmntFileRepository.nextSeq(cmntId));
                file.setAtchFilePathNm(stored.atchFilePathNm());
                file.setUseYn(USE_Y);
                file.setFrstCrtId(writer.getUserId());
                file.setFrstCrtDt(LocalDateTime.now());
                cmntFileRepository.save(file);
            }
        }
        return cmnt.getBbsId();
    }

    @Transactional
    public void deleteCmntFile(long fileId, Manager editor) {
        CmntyOffSrBbsCmntFile file = cmntFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        if (!isSystemAdmin(editor) && !isOwner(file.getFrstCrtId(), editor)) {
            throw new CmntyException("본인이 등록한 파일만 삭제 가능합니다.");
        }
        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        cmntFileRepository.save(file);
    }

    public CmntyOffSrBbsFile bbsFile(long fileId) {
        return offSrBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    public CmntyOffSrBbsCmntFile cmntFile(long fileId) {
        return cmntFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    public String screenRole(Manager viewer) {
        if (viewer == null || viewer.getAuthority() == null) {
            return "";
        }
        return SCREEN_ROLES.contains(viewer.getAuthority()) ? viewer.getAuthority() : "";
    }

    private void storeBbsFiles(Long bbsId, MultipartFile[] files, Manager writer) {
        if (files == null) {
            return;
        }
        for (MultipartFile multipartFile : files) {
            CmntyFileStorageService.Stored stored = fileStorageService.store(multipartFile, SUBDIR_BBS);
            if (stored == null) {
                continue;
            }
            CmntyOffSrBbsFile file = new CmntyOffSrBbsFile();
            file.setBbsId(bbsId);
            file.setOrgnlAtchFileNm(stored.orgnlAtchFileNm());
            file.setAtchFileNm(stored.atchFileNm());
            file.setAtchFileExtnNm(stored.atchFileExtnNm());
            file.setAtchFileSz(stored.atchFileSz());
            file.setAtchFileSeq(offSrBbsFileRepository.nextSeq(bbsId));
            file.setAtchFilePathNm(stored.atchFilePathNm());
            file.setUseYn(USE_Y);
            file.setFrstCrtId(writer.getUserId());
            file.setFrstCrtDt(LocalDateTime.now());
            offSrBbsFileRepository.save(file);
        }
    }

    private CmntyOffSrBbs aliveBbs(long bbsId) {
        CmntyOffSrBbs bbs = offSrBbsRepository.findById(bbsId)
                .orElseThrow(() -> new CmntyException("해당 글은 존재하지 않습니다."));
        if (!USE_Y.equals(bbs.getUseYn())) {
            throw new CmntyException("해당 글은 존재하지 않습니다.");
        }
        return bbs;
    }

    private CmntyOffSrBbsCmnt aliveCmnt(long cmntId) {
        return cmntRepository.findByCmntIdAndUseYn(cmntId, USE_Y)
                .orElseThrow(() -> new CmntyException("해당 댓글은 존재하지 않습니다."));
    }

    private static boolean isSystemAdmin(Manager viewer) {
        return viewer != null && SYSTEM_ROLES.contains(viewer.getAuthority());
    }

    private static boolean isOwner(Long ownerId, Manager actor) {
        return actor != null && actor.getUserId() != null && actor.getUserId().equals(ownerId);
    }

    private static void requireOwner(Long ownerId, Manager actor, String message) {
        if (!isOwner(ownerId, actor)) {
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

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
