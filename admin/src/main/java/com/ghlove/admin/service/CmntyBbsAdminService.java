package com.ghlove.admin.service;

import com.ghlove.admin.domain.CmntyBbs;
import com.ghlove.admin.domain.CmntyCmnt;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.CmntyBbsAdminRepository;
import com.ghlove.admin.repository.CmntyBbsRepository;
import com.ghlove.admin.repository.CmntyCmntRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 소통방 (메뉴 11401) - AS-IS {@code CmntyServiceImpl}의 {@code *Bbs}/{@code *Cmnt} 메서드를
 * 이식한 것이다({@code opmanager/community/bbs/*}, {@code opmanager/community/cmnt/*}).
 *
 * <p><b>AS-IS 규칙 그대로</b>:
 * <ul>
 *   <li>글/댓글 삭제는 소프트 삭제({@code USE_YN='N'})이고, <b>본인 것만</b> 지울 수 있다 -
 *       남의 글이면 "본인 게시글만 삭제 가능합니다."로 막는다(수정·댓글도 같은 문구 계열).
 *       <b>시스템 관리자도 예외가 없다</b> - AS-IS가 작성자만 보기 때문이다.</li>
 *   <li>비밀글({@code IS_SECRET='Y'})은 작성자 본인과 시스템 관리자(ROLE_ADMIN_1·2)만 볼 수
 *       있다. 행안부(3·4)·지자체(5·6)는 남의 비밀글을 볼 수 없다.</li>
 *   <li>상세·수정 화면에 들어갈 때마다 조회수가 1 올라간다. <b>비밀글로 막히는 경우에도
 *       먼저 올라간다</b> - AS-IS가 조회수 증가를 비밀글 판정보다 앞에서 하기 때문이다.</li>
 *   <li>본문은 스마트에디터가 {@code encodeURIComponent}로 보내므로 저장 전에 URL 디코딩한다.</li>
 * </ul>
 *
 * <p><b>AS-IS 결함 1건 - 안전하게 고쳤다</b>: AS-IS {@code detailBbs}는 글을 못 찾아도
 * {@code detail.getIsSecret()}을 먼저 호출해 <b>NPE</b>가 난다. 그래서 컨트롤러에 있는
 * "해당 글은 존재하지 않습니다." 리다이렉트는 <b>도달할 수 없는 코드</b>다(삭제된 글 링크를
 * 누르면 안내 대신 500이 뜬다). 여기서는 없으면 {@code null}을 돌려줘 컨트롤러가 본래
 * 의도대로 안내 후 목록으로 돌려보낸다.
 *
 * <p>작성자 소속 지자체명은 {@code g_locgov}(donation 소유)에서 와야 해서
 * {@link LocgovClient}로 한 번 받아 코드→이름으로 채운다 - 자세한 내용은
 * {@link CmntyBbsAdminRepository} 클래스 주석.
 */
@Service
@RequiredArgsConstructor
public class CmntyBbsAdminService {

    /** AS-IS: 남의 비밀글까지 볼 수 있는 권한. */
    private static final List<String> SECRET_VISIBLE_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");

    /** AS-IS detailBbs/createBbs가 화면에 내려주는 role - 1~6만 값이 들어간다. */
    private static final List<String> SCREEN_ROLES = List.of("ROLE_ADMIN_1", "ROLE_ADMIN_2",
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6");

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CmntyBbsAdminRepository cmntyBbsAdminRepository;
    private final CmntyBbsRepository cmntyBbsRepository;
    private final CmntyCmntRepository cmntyCmntRepository;
    private final LocgovClient locgovClient;

    /** 목록 한 행 - 소속 표기(권한/지자체명)는 AS-IS JSP가 분기하므로 재료를 그대로 내려준다. */
    public record BbsRow(Long bbsId, String bbsTtl, String noticeYn, Long inqCnt, String userName,
                         String authority, String upperLocgovNm, String locgovNm,
                         LocalDateTime frstCrtDt, LocalDateTime lastMdfcnDt, Long cmntCnt) {
    }

    /** 상세 한 행. {@code secretBlocked}가 AS-IS {@code isSecretYn='Y'}(열람 차단)에 대응한다. */
    public record BbsDetail(Long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                            Long inqCnt, Long frstCrtId, String userName, String authority,
                            String upperLocgovNm, String locgovNm, LocalDateTime frstCrtDt,
                            LocalDateTime lastMdfcnDt, boolean secretBlocked) {
    }

    /** 댓글 한 행. */
    public record CmntRow(Long cmntId, Long bbsId, String cmntCn, String userName, String authority,
                          String upperLocgovNm, String locgovNm, Long frstCrtId, LocalDateTime frstCrtDt) {
    }

    public int countBbs(String locgovCode, String startDt, String endDt, String searchRole,
                        String where, String query) {
        return cmntyBbsAdminRepository.countBbs(locgovCode, startDt, endDt, searchRole, where, query);
    }

    public List<BbsRow> listBbs(String locgovCode, String startDt, String endDt, String searchRole,
                                String where, String query, int offset, int limit) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyBbsAdminRepository.Row> rows = cmntyBbsAdminRepository.listBbs(locgovCode, startDt,
                endDt, searchRole, where, query, offset, limit);
        List<BbsRow> result = new ArrayList<>(rows.size());
        for (CmntyBbsAdminRepository.Row r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new BbsRow(r.bbsId(), r.bbsTtl(), r.noticeYn(), r.inqCnt(), r.userName(),
                    r.authority(), names[0], names[1], r.frstCrtDt(), r.lastMdfcnDt(), r.cmntCnt()));
        }
        return result;
    }

    /**
     * AS-IS detailBbs - 조회수를 올리고 비밀글 열람 가능 여부를 판정한다. 글이 없으면 {@code null}.
     *
     * <p>{@code viewer}는 지금 보고 있는 담당자다. AS-IS는 로그인 사용자의 권한 중 1~6에
     * 해당하는 것만 화면 role로 쓴다 - 오프라인 담당자(7·8) 등은 빈 값이 되어 남의 비밀글을
     * 볼 수 없다.
     */
    public BbsDetail detailBbs(long bbsId, Manager viewer) {
        // AS-IS: 비밀글 판정보다 먼저 올린다(막히는 경우에도 조회수는 올라간다)
        cmntyBbsAdminRepository.updateInqCnt(bbsId);

        CmntyBbsAdminRepository.DetailRow row = cmntyBbsAdminRepository.detailBbs(bbsId);
        if (row == null) {
            return null;
        }

        boolean secretBlocked = false;
        if (USE_Y.equalsIgnoreCase(row.isSecret())) {
            String role = screenRole(viewer);
            boolean systemAdmin = SECRET_VISIBLE_ROLES.contains(role);
            boolean author = viewer != null && viewer.getUserId() != null
                    && viewer.getUserId().equals(row.frstCrtId());
            secretBlocked = !systemAdmin && !author;
        }

        String[] names = namesOf(locgovNames(), row.locgovCode());
        return new BbsDetail(row.bbsId(), row.bbsTtl(), row.bbsCn(), row.noticeYn(), row.isSecret(),
                row.inqCnt(), row.frstCrtId(), row.userName(), row.authority(), names[0], names[1],
                row.frstCrtDt(), row.lastMdfcnDt(), secretBlocked);
    }

    /** AS-IS bbsCmntList - 상세화면의 댓글 목록. */
    public List<CmntRow> bbsCmntList(long bbsId) {
        Map<String, String[]> locgovNames = locgovNames();
        List<CmntyBbsAdminRepository.CmntRow> rows = cmntyBbsAdminRepository.bbsCmntList(bbsId);
        List<CmntRow> result = new ArrayList<>(rows.size());
        for (CmntyBbsAdminRepository.CmntRow r : rows) {
            String[] names = namesOf(locgovNames, r.locgovCode());
            result.add(new CmntRow(r.cmntId(), r.bbsId(), r.cmntCn(), r.userName(), r.authority(),
                    names[0], names[1], r.frstCrtId(), r.frstCrtDt()));
        }
        return result;
    }

    /**
     * AS-IS addBbs - 본문을 URL 디코딩해 저장한다. 조회수는 AS-IS가 폼에서 받은 값(= 빈 값)을
     * 그대로 넣으므로 비워 둔다(조회 쪽에서 coalesce로 0으로 보인다).
     */
    @Transactional
    public void addBbs(String bbsTtl, String bbsCn, String noticeYn, String isSecret, Manager writer) {
        CmntyBbs bbs = new CmntyBbs();
        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setUseYn(USE_Y);
        bbs.setFrstCrtId(writer.getUserId());
        bbs.setFrstCrtDt(LocalDateTime.now());
        cmntyBbsRepository.save(bbs);
    }

    /** AS-IS updateBbs - 본인 글만 수정할 수 있다. */
    @Transactional
    public void updateBbs(long bbsId, String bbsTtl, String bbsCn, String noticeYn, String isSecret,
                          Manager editor) {
        CmntyBbs bbs = aliveBbs(bbsId);
        requireAuthor(bbs.getFrstCrtId(), editor, "본인 게시글만 수정 가능합니다.");

        bbs.setBbsTtl(bbsTtl);
        bbs.setBbsCn(CmntyText.decode(bbsCn));
        bbs.setNoticeYn(noticeYn);
        bbs.setIsSecret(isSecret);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(LocalDateTime.now());
        cmntyBbsRepository.save(bbs);
    }

    /** AS-IS deleteBbs - 본인 글만, 소프트 삭제. */
    @Transactional
    public void deleteBbs(long bbsId, Manager editor) {
        CmntyBbs bbs = aliveBbs(bbsId);
        requireAuthor(bbs.getFrstCrtId(), editor, "본인 게시글만 삭제 가능합니다.");

        bbs.setUseYn(USE_N);
        bbs.setLastMdfcnId(editor.getUserId());
        bbs.setLastMdfcnDt(LocalDateTime.now());
        cmntyBbsRepository.save(bbs);
    }

    /** AS-IS addCmnt. */
    @Transactional
    public void addCmnt(long bbsId, String cmntCn, Manager writer) {
        CmntyCmnt cmnt = new CmntyCmnt();
        cmnt.setBbsId(bbsId);
        cmnt.setCmntCn(cmntCn);
        cmnt.setUseYn(USE_Y);
        cmnt.setFrstCrtId(writer.getUserId());
        cmnt.setFrstCrtDt(LocalDateTime.now());
        cmntyCmntRepository.save(cmnt);
    }

    /** AS-IS updateCmntCn - 본인 댓글만. */
    @Transactional
    public void updateCmnt(long cmntId, String cmntCn, Manager editor) {
        CmntyCmnt cmnt = aliveCmnt(cmntId);
        requireAuthor(cmnt.getFrstCrtId(), editor, "본인 댓글만 수정 가능합니다.");

        cmnt.setCmntCn(cmntCn);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(LocalDateTime.now());
        cmntyCmntRepository.save(cmnt);
    }

    /** AS-IS deleteCmnt - 본인 댓글만, 소프트 삭제. */
    @Transactional
    public void deleteCmnt(long cmntId, Manager editor) {
        CmntyCmnt cmnt = aliveCmnt(cmntId);
        requireAuthor(cmnt.getFrstCrtId(), editor, "본인 댓글만 삭제 가능합니다.");

        cmnt.setUseYn(USE_N);
        cmnt.setLastMdfcnId(editor.getUserId());
        cmnt.setLastMdfcnDt(LocalDateTime.now());
        cmntyCmntRepository.save(cmnt);
    }

    /** AS-IS가 등록·수정 화면에 내려주는 role - 상단공지 체크박스 노출 판정에 쓴다. */
    public String screenRole(Manager viewer) {
        if (viewer == null || viewer.getAuthority() == null) {
            return "";
        }
        return SCREEN_ROLES.contains(viewer.getAuthority()) ? viewer.getAuthority() : "";
    }

    private CmntyBbs aliveBbs(long bbsId) {
        CmntyBbs bbs = cmntyBbsRepository.findById(bbsId)
                .orElseThrow(() -> new CmntyException("해당 글은 존재하지 않습니다."));
        if (!USE_Y.equals(bbs.getUseYn())) {
            throw new CmntyException("해당 글은 존재하지 않습니다.");
        }
        return bbs;
    }

    private CmntyCmnt aliveCmnt(long cmntId) {
        return cmntyCmntRepository.findByCmntIdAndUseYn(cmntId, USE_Y)
                .orElseThrow(() -> new CmntyException("해당 댓글은 존재하지 않습니다."));
    }

    private static void requireAuthor(Long ownerId, Manager actor, String message) {
        if (actor == null || actor.getUserId() == null || !actor.getUserId().equals(ownerId)) {
            throw new CmntyException(message);
        }
    }

    /** 지자체코드 → {상위지자체명, 지자체명}. */
    private Map<String, String[]> locgovNames() {
        Map<String, String[]> map = new HashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.put(l.locgovCode(), new String[]{l.upperLocgovNm(), l.locgovNm()});
        }
        return map;
    }

    /** AS-IS는 지자체를 못 찾으면 빈 문자열을 찍는다(CASE WHEN ... ELSE ''). */
    private static String[] namesOf(Map<String, String[]> locgovNames, String locgovCode) {
        String[] names = locgovCode == null ? null : locgovNames.get(locgovCode);
        if (names == null) {
            return new String[]{"", ""};
        }
        return new String[]{names[0] == null ? "" : names[0], names[1] == null ? "" : names[1]};
    }
}
