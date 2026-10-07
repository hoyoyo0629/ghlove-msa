package com.ghlove.admin.service;

import com.ghlove.admin.domain.CmntyFaqBbs;
import com.ghlove.admin.domain.CmntyFaqBbsCmnt;
import com.ghlove.admin.domain.CmntyFaqBbsCmntFile;
import com.ghlove.admin.domain.CmntyFaqBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntyFaqBbsAdminRepository;
import com.ghlove.admin.repository.CmntyFaqBbsCmntFileRepository;
import com.ghlove.admin.repository.CmntyFaqBbsCmntRepository;
import com.ghlove.admin.repository.CmntyFaqBbsFileRepository;
import com.ghlove.admin.repository.CmntyFaqBbsRepository;
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
 * 담당자용 FAQ (메뉴 11405) - AS-IS {@code CmntyServiceImpl}의 {@code *FaqBbs*} 메서드 이식
 * ({@code opmanager/community/faqBbs/*}). 지자체 담당자용 시스템 운영 FAQ 게시판이다.
 *
 * <p>구조는 SR 게시판({@link CmntySrBbsAdminService})과 같고 <b>질문유형(FAQ_TYPE)</b>이 추가된다.
 * 질문유형 코드·라벨은 AS-IS Java enum {@code FaqType} 11종을 공통코드
 * {@code CMNTY_FAQ_TYPE}으로 옮겨 쓴다({@code migration-admin-cmnty-faq-type-codes.sql}).
 *
 * <p><b>★ 적재 대상 표 주의(2026-10-07)</b>: 위 마이그레이션은 {@code admin.op_common_code}에 넣었는데,
 * 아래 {@link #faqTypes()}가 쓰는 {@link CommonCodeService#labelsOf}는 <b>JPA 경로라
 * {@code admin.ADMIN_COMMON_CODE}를 읽는다</b>. 그래서 탭이 '전체' 하나만 보이는 증상이 났고,
 * {@code migration-admin-cmnty-faq-type-codes-admin-table.sql}로 같은 11건을 그 표에도 넣어 해결했다.
 * 공통코드를 새로 추가할 때는 <b>읽는 쪽이 JPA인지 네이티브 SQL인지 먼저 확인할 것.</b>
 *
 * <p><b>AS-IS 결함도 같은 5건을 같은 방식으로 고쳤다</b>(수정 권한 없음 / 삭제된 글이 URL로 열림 /
 * 첨부 2개 이상이면 상세 500 / 댓글 첨부 크기 빈 값 / 댓글 수정자 ID null) -
 * 내용은 {@link CmntySrBbsAdminService} 클래스 주석에 적어 두었다.
 */
@Service
@RequiredArgsConstructor
public class CmntyFaqBbsAdminService {

    private static final List<String> SYSTEM_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");

    private static final List<String> SCREEN_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2",
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    public static final String SUBDIR_BBS = "cmnty/faq-bbs";
    public static final String SUBDIR_CMNT = "cmnty/faq-bbs-cmnt";

    /** 질문유형 공통코드 - AS-IS enum FaqType을 옮긴 것. */
    public static final String FAQ_TYPE_CODE = "CMNTY_FAQ_TYPE";

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CmntyFaqBbsAdminRepository faqBbsAdminRepository;
    private final CmntyFaqBbsRepository faqBbsRepository;
    private final CmntyFaqBbsFileRepository faqBbsFileRepository;
    private final CmntyFaqBbsCmntRepository cmntRepository;
    private final CmntyFaqBbsCmntFileRepository cmntFileRepository;
    private final CmntyFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;

    /** 목록 한 행 - 소속 컬럼 대신 <b>질문유형</b>을 보여준다(AS-IS 컬럼 구성). */
    public record BbsRow(Long bbsId, String bbsTtl, String faqType, String faqTypeNm, String noticeYn,
                         String isSecret, Long inqCnt, String userName, String authority,
                         String upperLocgovNm, String locgovNm, LocalDateTime frstCrtDt,
                         LocalDateTime lastMdfcnDt, Long cmntCnt, Long attachedFileCnt) {
    }

    public record BbsDetail(Long bbsId, String bbsTtl, String bbsCn, String faqType, String faqTypeNm,
                            String noticeYn, String isSecret, Long inqCnt, Long frstCrtId,
                            String userName, String authority, String upperLocgovNm, String locgovNm,
                            LocalDateTime frstCrtDt, boolean secretBlocked) {
    }

    public record FileRow(Long fileId, String orgnlAtchFileNm, String fileSize,
                          LocalDateTime frstCrtDt, Long frstCrtId) {
    }

    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String upperLocgovNm, String locgovNm, Long frstCrtId,
                          LocalDateTime frstCrtDt, List<FileRow> files) {
    }

    /** 질문유형 목록(코드 → 라벨) - 화면의 탭·select가 쓴다. */
    public Map<String, String> faqTypes() {
        return commonCodeService.labelsOf(FAQ_TYPE_CODE);
    }

    /** 질문유형 탭 한 칸 - {@code id}가 빈 값이면 '전체' 탭이다. */
    public record TypeTab(String id, String label) {
    }

    /**
     * 질문유형 탭을 <b>한 줄에 6칸</b>으로 쪼갠 것 - AS-IS list.jsp가 forEach 안에서
     * {@code </ul><ul>}을 끼워 넣어 줄을 바꾸는 구조를 그대로 재현한다(Thymeleaf는 태그를
     * 그렇게 쪼갤 수 없어 서버에서 미리 묶는다. 보이는 결과는 같다).
     *
     * <p>AS-IS 규칙 그대로: 첫 줄은 <b>'전체' 탭 + 유형 5개</b>로 시작하고, 그 뒤로는
     * 유형 인덱스가 6의 배수가 될 때마다 줄을 바꾼다. 마지막 줄은 빈 칸으로
     * {@code 5 - (유형수 % 6)}개를 덧붙인다(AS-IS {@code <c:forEach begin="${faqMod+1}" end="5">}
     * 그대로 - 유형이 11개인 지금은 {@code 11 % 6 = 5}라서 덧붙는 칸이 없다).
     */
    public List<List<TypeTab>> faqTypeTabRows() {
        List<TypeTab> cells = new ArrayList<>();
        cells.add(new TypeTab("", "전체"));
        faqTypes().forEach((id, label) -> cells.add(new TypeTab(id, label)));

        List<List<TypeTab>> rows = new ArrayList<>();
        List<TypeTab> row = new ArrayList<>();
        for (TypeTab cell : cells) {
            if (row.size() == 6) {
                rows.add(row);
                row = new ArrayList<>();
            }
            row.add(cell);
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }

        int typeCount = cells.size() - 1;
        int pad = 5 - (typeCount % 6);
        if (typeCount % 6 != 0 && pad > 0 && !rows.isEmpty()) {
            List<TypeTab> last = rows.get(rows.size() - 1);
            for (int i = 0; i < pad; i++) {
                last.add(null);
            }
        }
        return rows;
    }

    public int count(String locgovCode, String startDt, String endDt, String searchRole,
                     String where, String query, String shFaqType) {
        return faqBbsAdminRepository.count(locgovCode, startDt, endDt, searchRole, where, query,
                shFaqType);
    }

    public List<BbsRow> list(String locgovCode, String startDt, String endDt, String searchRole,
                             String where, String query, String shFaqType, int offset, int limit) {
        Map<String, String[]> locgovNames = locgovNames();
        Map<String, String> types = faqTypes();
        List<CmntyFaqBbsAdminRepository.Row> rows = faqBbsAdminRepository.list(locgovCode, startDt,
                endDt, searchRole, where, query, shFaqType, offset, limit);
        List<BbsRow> result = new ArrayList<>(rows.size());
        for (CmntyFaqBbsAdminRepository.Row r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new BbsRow(r.bbsId(), r.bbsTtl(), r.faqType(), typeName(types, r.faqType()),
                    r.noticeYn(), r.isSecret(), r.inqCnt(), r.userName(), r.authority(), names[0],
                    names[1], r.frstCrtDt(), r.lastMdfcnDt(), r.cmntCnt(), r.attachedFileCnt()));
        }
        return result;
    }

    public void increaseInqCnt(long bbsId) {
        faqBbsAdminRepository.updateInqCnt(bbsId);
    }

    public BbsDetail detail(long bbsId, Manager viewer) {
        CmntyFaqBbsAdminRepository.DetailRow row = faqBbsAdminRepository.detail(bbsId);
        if (row == null) {
            return null;
        }
        boolean secretBlocked = false;
        if (USE_Y.equalsIgnoreCase(row.isSecret())) {
            secretBlocked = !isSystemAdmin(viewer) && !isOwner(row.frstCrtId(), viewer);
        }
        String[] names = namesOf(locgovNames(), row.locgovCode());
        return new BbsDetail(row.bbsId(), row.bbsTtl(), row.bbsCn(), row.faqType(),
                typeName(faqTypes(), row.faqType()), row.noticeYn(), row.isSecret(), row.inqCnt(),
                row.frstCrtId(), row.userName(), row.authority(), names[0], names[1],
                row.frstCrtDt(), secretBlocked);
    }

    public List<FileRow> fileList(long bbsId) {
        List<FileRow> result = new ArrayList<>();
        for (CmntyFaqBbsFile f : faqBbsFileRepository
                .findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            result.add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                    CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                    f.getFrstCrtId()));
        }
        return result;
    }

    public List<CmntRow> cmntList(long bbsId) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyFaqBbsAdminRepository.CmntRow> rows = faqBbsAdminRepository.cmntList(bbsId);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> cmntIds = rows.stream().map(CmntyFaqBbsAdminRepository.CmntRow::cmntId).toList();
        Map<Long, List<FileRow>> filesByCmnt = new LinkedHashMap<>();
        for (CmntyFaqBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
            filesByCmnt.computeIfAbsent(f.getCmntId(), key -> new ArrayList<>())
                    .add(new FileRow(f.getFileId(), f.getOrgnlAtchFileNm(),
                            CmntyFileStorageService.formatSize(f.getAtchFileSz()), f.getFrstCrtDt(),
                            f.getFrstCrtId()));
        }

        List<CmntRow> result = new ArrayList<>(rows.size());
        for (CmntyFaqBbsAdminRepository.CmntRow r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new CmntRow(r.cmntId(), r.bbsId(), r.cmntCn(), r.userName(), r.authority(),
                    names[0], names[1], r.frstCrtId(), r.frstCrtDt(),
                    filesByCmnt.getOrDefault(r.cmntId(), List.of())));
        }
        return result;
    }

    @Transactional
    public void insert(String bbsTtl, String bbsCn, String faqType, String noticeYn, String isSecret,
                       MultipartFile[] files, Manager writer) {
        CmntyFaqBbs bbs = new CmntyFaqBbs();
        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setFaqType(faqType);
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setUseYn(USE_Y);
        bbs.setInqCnt(0L);
        bbs.setFrstCrtId(writer.getUserId());
        bbs.setFrstCrtDt(LocalDateTime.now());
        faqBbsRepository.save(bbs);

        storeBbsFiles(bbs.getBbsId(), files, writer);
    }

    @Transactional
    public void update(long bbsId, String bbsTtl, String bbsCn, String faqType, String noticeYn,
                       String isSecret, MultipartFile[] files, Manager editor) {
        CmntyFaqBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 수정 가능합니다.");

        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setFaqType(faqType);
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(LocalDateTime.now());
        faqBbsRepository.save(bbs);

        storeBbsFiles(bbsId, files, editor);
    }

    /** AS-IS deleteFaqBbs - 댓글 첨부 → 댓글 → 본문 첨부 → 게시글 순으로 소프트 삭제. */
    @Transactional
    public void delete(long bbsId, Manager editor) {
        CmntyFaqBbs bbs = aliveBbs(bbsId);
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        List<CmntyFaqBbsCmnt> comments = cmntRepository.findByBbsIdAndUseYn(bbsId, USE_Y);
        if (!comments.isEmpty()) {
            List<Long> cmntIds = comments.stream().map(CmntyFaqBbsCmnt::getCmntId).toList();
            for (CmntyFaqBbsCmntFile f : cmntFileRepository.findByCmntIdInAndUseYn(cmntIds, USE_Y)) {
                f.setUseYn(USE_N);
                f.setLastMdfcnId(editor.getUserId());
                f.setLastMdfcnDt(now);
                cmntFileRepository.save(f);
            }
            for (CmntyFaqBbsCmnt c : comments) {
                c.setUseYn(USE_N);
                c.setLastMdfcnId(editor.getUserId());
                c.setLastMdfcnDt(now);
                cmntRepository.save(c);
            }
        }
        for (CmntyFaqBbsFile f : faqBbsFileRepository
                .findByBbsIdAndUseYnOrderByAtchFileSeqAsc(bbsId, USE_Y)) {
            f.setUseYn(USE_N);
            f.setLastMdfcnId(editor.getUserId());
            f.setLastMdfcnDt(now);
            faqBbsFileRepository.save(f);
        }

        bbs.setUseYn(USE_N);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(now);
        faqBbsRepository.save(bbs);
    }

    @Transactional
    public void deleteBbsFile(long fileId, Manager editor) {
        CmntyFaqBbsFile file = faqBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        CmntyFaqBbs bbs = aliveBbs(file.getBbsId());
        requireOwner(bbs.getFrstCrtId(), editor, "본인 게시글의 첨부만 삭제 가능합니다.");

        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        faqBbsFileRepository.save(file);
    }

    @Transactional
    public void addCmnt(long bbsId, String cmntCn, Manager writer) {
        aliveBbs(bbsId);
        CmntyFaqBbsCmnt cmnt = new CmntyFaqBbsCmnt();
        cmnt.setBbsId(bbsId);
        cmnt.setCmntCn(cmntCn);
        cmnt.setUseYn(USE_Y);
        cmnt.setFrstCrtId(writer.getUserId());
        cmnt.setFrstCrtDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    @Transactional
    public void updateCmnt(long cmntId, String cmntCn, Manager editor) {
        CmntyFaqBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 수정 가능합니다.");

        cmnt.setCmntCn(cmntCn);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(LocalDateTime.now());
        cmntRepository.save(cmnt);
    }

    @Transactional
    public void deleteCmnt(long cmntId, Manager editor) {
        CmntyFaqBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), editor, "본인 댓글만 삭제 가능합니다.");

        LocalDateTime now = LocalDateTime.now();
        for (CmntyFaqBbsCmntFile f : cmntFileRepository.findByCmntIdAndUseYn(cmntId, USE_Y)) {
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
        CmntyFaqBbsCmnt cmnt = aliveCmnt(cmntId);
        requireOwner(cmnt.getFrstCrtId(), writer, "본인 댓글에만 파일을 등록할 수 있습니다.");

        if (files != null) {
            for (MultipartFile multipartFile : files) {
                CmntyFileStorageService.Stored stored = fileStorageService.store(multipartFile, SUBDIR_CMNT);
                if (stored == null) {
                    continue;
                }
                CmntyFaqBbsCmntFile file = new CmntyFaqBbsCmntFile();
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
        CmntyFaqBbsCmntFile file = cmntFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
        if (!isSystemAdmin(editor) && !isOwner(file.getFrstCrtId(), editor)) {
            throw new CmntyException("본인이 등록한 파일만 삭제 가능합니다.");
        }
        file.setUseYn(USE_N);
        file.setLastMdfcnId(editor.getUserId());
        file.setLastMdfcnDt(LocalDateTime.now());
        cmntFileRepository.save(file);
    }

    public CmntyFaqBbsFile bbsFile(long fileId) {
        return faqBbsFileRepository.findById(fileId)
                .orElseThrow(() -> new CmntyException("해당 파일은 존재하지 않습니다."));
    }

    public CmntyFaqBbsCmntFile cmntFile(long fileId) {
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
            CmntyFaqBbsFile file = new CmntyFaqBbsFile();
            file.setBbsId(bbsId);
            file.setOrgnlAtchFileNm(stored.orgnlAtchFileNm());
            file.setAtchFileNm(stored.atchFileNm());
            file.setAtchFileExtnNm(stored.atchFileExtnNm());
            file.setAtchFileSz(stored.atchFileSz());
            file.setAtchFileSeq(faqBbsFileRepository.nextSeq(bbsId));
            file.setAtchFilePathNm(stored.atchFilePathNm());
            file.setUseYn(USE_Y);
            file.setFrstCrtId(writer.getUserId());
            file.setFrstCrtDt(LocalDateTime.now());
            faqBbsFileRepository.save(file);
        }
    }

    private CmntyFaqBbs aliveBbs(long bbsId) {
        CmntyFaqBbs bbs = faqBbsRepository.findById(bbsId)
                .orElseThrow(() -> new CmntyException("해당 글은 존재하지 않습니다."));
        if (!USE_Y.equals(bbs.getUseYn())) {
            throw new CmntyException("해당 글은 존재하지 않습니다.");
        }
        return bbs;
    }

    private CmntyFaqBbsCmnt aliveCmnt(long cmntId) {
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

    /** AS-IS 목록은 faqTypes에서 code가 일치하는 title을 찾아 찍고, 없으면 빈 값이다. */
    private static String typeName(Map<String, String> types, String faqType) {
        if (faqType == null) {
            return "";
        }
        return types.getOrDefault(faqType, "");
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
