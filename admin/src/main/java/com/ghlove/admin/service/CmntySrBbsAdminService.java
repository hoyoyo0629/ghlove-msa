package com.ghlove.admin.service;

import com.ghlove.admin.domain.CmntySrBbs;
import com.ghlove.admin.domain.CmntySrBbsCmnt;
import com.ghlove.admin.domain.CmntySrBbsCmntFile;
import com.ghlove.admin.domain.CmntySrBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntySrBbsAdminRepository;
import com.ghlove.admin.repository.CmntySrBbsCmntFileRepository;
import com.ghlove.admin.repository.CmntySrBbsCmntRepository;
import com.ghlove.admin.repository.CmntySrBbsFileRepository;
import com.ghlove.admin.repository.CmntySrBbsRepository;
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
 * SR 게시판 (메뉴 11404) - AS-IS {@code CmntyServiceImpl}의 {@code *SrBbs*} 메서드 이식
 * ({@code opmanager/community/srBbs/*}). 기능개선 건의·시스템 오류 신고 게시판으로,
 * <b>본문 첨부 + 댓글 첨부</b>가 모두 있는 가장 복잡한 게시판이다.
 *
 * <p><b>AS-IS 규칙 그대로</b>:
 * <ul>
 *   <li>삭제는 전부 소프트 삭제({@code USE_YN='N'})이고, 게시글을 지우면 <b>댓글 첨부 → 댓글 →
 *       본문 첨부 → 게시글</b> 순서로 함께 지운다(AS-IS deleteSrBbs 순서 그대로). 디스크 파일은
 *       지우지 않는다.</li>
 *   <li>비밀글은 작성자 본인과 시스템 관리자(ROLE_ADMIN_1·2)만 볼 수 있다.</li>
 *   <li>첨부 순번({@code ATCH_FILE_SEQ})은 게시글/댓글 안에서 {@code max+1}이다.</li>
 *   <li>본문은 화면이 {@code encodeURIComponent}로 보내므로 저장 전에 URL 디코딩한다.</li>
 * </ul>
 *
 * <p><b>AS-IS 결함 4건 - 고쳤다</b>
 * <ol>
 *   <li><b>수정 권한 없음</b>: AS-IS {@code updateSrBbs}에는 작성자 확인이 <b>없다</b>
 *       - 같은 클래스의 {@code deleteSrBbs}와 소통방 {@code updateBbs}에는 있다. 화면은 작성자에게만
 *       수정 버튼을 보여주지만 URL로 직접 호출하면 <b>남의 글을 고칠 수 있다</b>. 작성자 확인을 넣었다
 *       (정상 흐름은 작성자만 들어오므로 막히는 동작이 없다). 댓글 첨부 등록·삭제, 본문 첨부 삭제도
 *       같은 이유로 권한을 맞췄다(댓글 첨부 삭제는 화면 규칙대로 <b>파일 등록자 또는 ROLE_ADMIN_1·2</b>).</li>
 *   <li><b>상세가 삭제된 글도 보여준다</b>: {@code getSrBbsDetail}에 {@code use_yn} 조건이 없다
 *       (소통방은 있다) - {@link CmntySrBbsAdminRepository#detail(long)} 주석 참고.</li>
 *   <li><b>첨부가 2개 이상이면 상세화면이 깨진다</b>: AS-IS는 파일 크기를
 *       {@code (SELECT ATCH_FILE_SZ FROM ... WHERE BBS_ID = A.BBS_ID AND USE_YN='Y')} 스칼라
 *       서브쿼리 <b>하나</b>로 뽑아 화면의 모든 첨부에 같은 값을 찍는다. 행이 2건 이상이면 SQL 자체가
 *       실패한다(화면이 500). <b>파일마다 자기 크기</b>를 쓴다. 지금 화면은 첨부 1개 제한이라 보이는
 *       결과는 같다.</li>
 *   <li><b>댓글 첨부 크기가 항상 빈 값</b>: 화면이 {@code cmntFile.fileSize}를 찍는데 AS-IS 매퍼가
 *       그 필드를 채우지 않아 괄호만 나온다 - 본문 첨부와 같은 공식으로 채웠다.</li>
 *   <li>댓글 수정 시 {@code LAST_MDFCN_ID}에 입력 DTO의 빈 작성자 ID가 들어가 null이 된다
 *       (소통파 쪽은 조회한 행을 넘겨 제대로 들어간다) - 수정자 ID를 넣는다.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class CmntySrBbsAdminService {

    /** AS-IS: 남의 비밀글·남의 댓글 첨부까지 손댈 수 있는 권한. */
    private static final List<String> SYSTEM_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");

    /** AS-IS가 화면에 내려주는 role - 1~6만 값이 들어간다. */
    private static final List<String> SCREEN_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2",
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    /** AS-IS dto.getUploadPath() 자리 - 본문 첨부와 댓글 첨부를 나눠 둔다. */
    public static final String SUBDIR_BBS = "cmnty/sr-bbs";
    public static final String SUBDIR_CMNT = "cmnty/sr-bbs-cmnt";

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CmntySrBbsAdminRepository srBbsAdminRepository;
    private final CmntySrBbsRepository srBbsRepository;
    private final CmntySrBbsFileRepository srBbsFileRepository;
    private final CmntySrBbsCmntRepository cmntRepository;
    private final CmntySrBbsCmntFileRepository cmntFileRepository;
    private final CmntyFileStorageService fileStorageService;
    private final LocgovClient locgovClient;

    /** 목록 한 행 - 제목 옆 아이콘 판정용으로 첨부개수·비밀글여부가 같이 온다. */
    public record BbsRow(Long bbsId, String bbsTtl, String noticeYn, String isSecret, Long inqCnt,
                         String userName, String authority, String upperLocgovNm, String locgovNm,
                         LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt, Long cmntCnt,
                         Long attachedFileCnt) {
    }

    /** 상세 한 행. */
    public record BbsDetail(Long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                            Long inqCnt, Long frstCrtId, String userName, String authority,
                            String upperLocgovNm, String locgovNm, LocalDateTime frstCrtDt,
                            boolean secretBlocked) {
    }

    /** 첨부파일 한 건 - 크기는 AS-IS 표기(B/KB/MB)로 만들어 둔다. */
    public record FileRow(Long fileId, String orgnlAtchFileNm, String fileSize,
                          LocalDateTime frstCrtDt, Long frstCrtId) {
    }

    /** 댓글 한 행 - 첨부파일 목록이 같이 붙는다. */
    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String upperLocgovNm, String locgovNm, Long frstCrtId,
                          LocalDateTime frstCrtDt, List<FileRow> files) {
    }

    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query) {
        return srBbsAdminRepository.count(locgovCode, startDt, endDt, searchRole, where, query);
    }

    public List<BbsRow> list(String locgovCode, String startDt, String endDt, String searchRole,
                             String where, String query, int offset, int limit) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntySrBbsAdminRepository.Row> rows = srBbsAdminRepository.list(locgovCode, startDt, endDt,
                searchRole, where, query, offset, limit);
        List<BbsRow> result = new ArrayList<>(rows.size());
        for (CmntySrBbsAdminRepository.Row r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new BbsRow(r.bbsId(), r.bbsTtl(), r.noticeYn(), r.isSecret(), r.inqCnt(),
                    r.userName(), r.authority(), names[0], names[1], r.frstCrtDt(), r.lastMdfcnDt(),
                    r.cmntCnt(), r.attachedFileCnt()));
        }
        return result;
    }

    /** AS-IS는 상세 진입에서 조회수를 먼저 올린다(컨트롤러가 호출). */
    public void increaseInqCnt(long bbsId) {
        srBbsAdminRepository.updateInqCnt(bbsId);
    }

    /** AS-IS getSrBbsDetail - 비밀글 열람 가능 여부까지 판정한다. 글이 없으면 {@code null}. */
    public BbsDetail detail(long bbsId, Manager viewer) {
        CmntySrBbsAdminRepository.DetailRow row = srBbsAdminRepository.detail(bbsId);
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
                row.frstCrtDt(), secretBlocked);
    }

    /** AS-IS getSrBbsfileList - 본문 첨부 목록. */
    public List<FileRow> fileList(long bbsId) {
        List<FileRow> result = new ArrayList<>();
        for (CmntySrBbsFile f : srBbsFileRepository.findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            result.add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                    CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                    f.getFrstCrtId()));
        }
        return result;
    }

    /** AS-IS selectSrBbsCmntList - 댓글 + 각 댓글의 첨부. */
    public List<CmntRow> cmntList(long bbsId) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntySrBbsAdminRepository.CmntRow> rows = srBbsAdminRepository.cmntList(bbsId);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> cmntIds = rows.stream().map(CmntySrBbsAdminRepository.CmntRow::cmntId).toList();
        Map<Long, List<FileRow>> filesByCmnt = new LinkedHashMap<>();
        for (CmntySrBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
            filesByCmnt.computeIfAbsent(f.getCmntId(), key -> new ArrayList<>())
                    .add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                            CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                            f.getFrstCrtId()));
        }

        List<CmntRow> result = new ArrayList<>(rows.size());
        for (CmntySrBbsAdminRepository.CmntRow r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new CmntRow(r.cmntId(), r.bbsId(), r.cmntCn(), r.userName(), r.authority(),
                    names[0], names[1], r.frstCrtId(), r.frstCrtDt(),
                    filesByCmnt.getOrDefault(r.cmntId(), List.of())));
        }
        return result;
    }

    /** AS-IS insertSrBbs - 게시글 등록 후 첨부 업로드. 조회수는 0으로 넣는다. */
    @Transactional
    public void insert(String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                       MultipartFile[] files, Manager writer) {
        CmntySrBbs bbs = new CmntySrBbs();
        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setUseYn(USE_Y);
        bbs.setInqCnt(0L);
        bbs.setFrstCrtId(writer.getUserId());
        bbs.setFrstCrtDt(LocalDateTime.now());
        srBbsRepository.save(bbs);

        storeBbsFiles(bbs.getBbsId(), files, writer);
    }

    /** AS-IS updateSrBbs + <b>작성자 확인 추가</b>(클래스 주석 결함 ①). */
    @Transactional
    public void update(long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                       MultipartFile[] files, Manager editor) {
        CmntySrBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 수정 가능합니다.");

        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(LocalDateTime.now());
        srBbsRepository.save(bbs);

        storeBbsFiles(bbsId, files, editor);
    }

    /**
     * AS-IS deleteSrBbs - 본인 글만. 댓글 첨부 → 댓글 → 본문 첨부 → 게시글 순으로 소프트 삭제한다.
     */
    @Transactional
    public void delete(long bbsId, Manager editor) {
        CmntySrBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        List<CmntySrBbsCmnt> comments = cmntRepository.findByBbsIdAndUseYn(bbsId, USE_Y);
        if (!comments.isEmpty()) {
            List<Long> cmntIds = comments.stream().map(CmntySrBbsCmnt::getCmntId).toList();
            for (CmntySrBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
                f.setUseYn(USE_N);
                f.setLastMdfcnId(editor.getUserId());
                f.setLastMdfcnDt(now);
                cmntFileRepository.save(f);
            }
            for (CmntySrBbsCmnt c : comments) {
                c.setUseYn(USE_N);
                c.setLastMdfcnId(editor.getUserId());
                c.setLastMdfcnDt(now);
                cmntRepository.save(c);
            }
        }
        for (CmntySrBbsFile f : srBbsFileRepository.findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            f.setUseYn(USE_N);
            f.setLastMdfcnId(editor.getUserId());
            f.setLastMdfcnDt(now);
            srBbsFileRepository.save(f);
        }

        bbs.setUseYn(USE_N);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(now);
        srBbsRepository.save(bbs);
    }

    /** AS-IS deleteSrBbsFileByFileId - 본문 첨부 삭제(수정화면). 글 작성자만. */
    @Transactional
    public void deleteBbsFile(long fileId, Manager editor) {
        CmntySrBbsFile file = srBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        CmntySrBbs bbs = aliveBbs(file.getBbsId());
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글의 첨부만 삭제 가능합니다.");

        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        srBbsFileRepository.save(file);
    }

    /** AS-IS addSrBbsCmnt. */
    @Transactional
    public void addCmnt(long bbsId, String cmntCn, Manager writer) {
        aliveBbs(bbsId);
        CmntySrBbsCmnt cmnt = new CmntySrBbsCmnt();
        cmnt.setBbsId(bbsId);
        cmnt.setCmntCn(cmntCn);
        cmnt.setUseYn(USE_Y);
        cmnt.setFrstCrtId(writer.getUserId());
        cmnt.setFrstCrtDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    /** AS-IS updateSrBbsCmnt - 본인 댓글만. */
    @Transactional
    public void updateCmnt(long cmntId, String cmntCn, Manager editor) {
        CmntySrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 수정 가능합니다.");

        cmnt.setCmntCn(cmntCn);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    /** AS-IS deleteSrBbsCmnt - 본인 댓글만. 그 댓글의 첨부도 함께 소프트 삭제. */
    @Transactional
    public void deleteCmnt(long cmntId, Manager editor) {
        CmntySrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        for (CmntySrBbsCmntFile f : cmntFileRepository.findByCmntIdAndUseYn(cmntId, USE_Y)) {
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

    /** AS-IS fileUploadHandlerSrBbsCmnt - 댓글 첨부 등록(본인 댓글만 - 결함 ①). */
    @Transactional
    public long addCmntFiles(long cmntId, MultipartFile[] files, Manager writer) {
        CmntySrBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), writer, "본인 댓글에만 파일을 등록할 수 있습니다.");

        if (files != null) {
            for (MultipartFile multipartFile : files) {
                CmntyFileStorageService.Stored stored = fileStorageService.store(multipartFile, SUBDIR_CMNT);
                if (stored == null) {
                    continue;
                }
                CmntySrBbsCmntFile file = new CmntySrBbsCmntFile();
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

    /**
     * AS-IS deleteSrBbsCmntFileByFileId - 댓글 첨부 삭제. 화면이 X 아이콘을
     * <b>파일 등록자 또는 시스템 관리자</b>에게만 보여주므로 서버도 같게 맞췄다(결함 ①).
     */
    @Transactional
    public void deleteCmntFile(long fileId, Manager editor) {
        CmntySrBbsCmntFile file = cmntFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        if (!isSystemAdmin(editor) && !isOwner(file.getFrstCrtId(), editor)) {
            throw new CmntyException("본인이 등록한 파일만 삭제 가능합니다.");
        }
        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        cmntFileRepository.save(file);
    }

    /** 다운로드용 본문 첨부 조회. */
    public CmntySrBbsFile bbsFile(long fileId) {
        return srBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    /** 다운로드용 댓글 첨부 조회. */
    public CmntySrBbsCmntFile cmntFile(long fileId) {
        return cmntFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    /** AS-IS가 등록·수정·상세 화면에 내려주는 role. */
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
            CmntySrBbsFile file = new CmntySrBbsFile();
            file.setBbsId(bbsId);
            file.setOrgnlAtchFileNm(stored.orgnlAtchFileNm());
            file.setAtchFileNm(stored.atchFileNm());
            file.setAtchFileExtnNm(stored.atchFileExtnNm());
            file.setAtchFileSz(stored.atchFileSz());
            file.setAtchFileSeq(srBbsFileRepository.nextSeq(bbsId));
            file.setAtchFilePathNm(stored.atchFilePathNm());
            file.setUseYn(USE_Y);
            file.setFrstCrtId(writer.getUserId());
            file.setFrstCrtDt(LocalDateTime.now());
            srBbsFileRepository.save(file);
        }
    }

    private CmntySrBbs aliveBbs(long bbsId) {
        CmntySrBbs bbs = srBbsRepository.findById(bbsId)
                .orElseThrow(() -> new CmntyException("해당 글은 존재하지 않습니다."));
        if (!USE_Y.equals(bbs.getUseYn())) {
            throw new CmntyException("해당 글은 존재하지 않습니다.");
        }
        return bbs;
    }

    private CmntySrBbsCmnt aliveCmnt(long cmntId) {
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
}
