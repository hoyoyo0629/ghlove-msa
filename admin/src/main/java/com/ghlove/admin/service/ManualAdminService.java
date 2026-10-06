package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.Menu;
import com.ghlove.admin.domain.Mnl;
import com.ghlove.admin.repository.CommonCodeRepository;
import com.ghlove.admin.repository.ManagerManualRepository;
import com.ghlove.admin.repository.MenuRepository;
import com.ghlove.admin.repository.MnlAdminRepository;
import com.ghlove.admin.repository.MnlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 매뉴얼 관리 - AS-IS {@code ManualManagerController} + {@code ManualServiceImpl} 이식.
 * <b>한 컨트롤러가 두 화면을 담당하던 것</b>을 메뉴대로 나눠 담았다:
 *
 * <ul>
 *   <li><b>사용자매뉴얼(5202)</b> - {@code g_mnl}. 공개 화면 한 페이지에 대한 설명을 등록한다.
 *       {@link #list}/{@link #create}/{@link #update}/{@link #delete} 등.</li>
 *   <li><b>관리자매뉴얼(5201)</b> - 표 없이 <b>{@code op_menu}에 파일만 붙인다</b>.
 *       {@link #managerTree}/{@link #attachManagerManual} 등.</li>
 * </ul>
 *
 * <p><b>AS-IS 그대로</b>:
 * <ul>
 *   <li>사용자매뉴얼 목록 <b>진입(GET)은 조회하지 않고 빈 목록</b>이고, 등록일 기본값이
 *       <b>오늘~오늘</b>로 채워진다(검색하면 그 범위로 걸린다).</li>
 *   <li>등록은 같은 화면(코드)으로 <b>두 번 등록할 수 없다</b> - "이미 등록 되어 있습니다."</li>
 *   <li>{@code menu_url}·{@code menu_nm}은 입력값이 아니라 공통코드 {@code MENU_URL}의
 *       label·detail을 베껴 넣는다.</li>
 *   <li>삭제는 <b>행 삭제</b>이고, 첨부 삭제는 파일 컬럼만 비운다(행은 남는다).</li>
 *   <li>관리자매뉴얼 등록은 {@code op_menu}의 파일 컬럼 UPDATE 한 번이다 - 파일을 고르지 않으면
 *       <b>아무 일도 일어나지 않는다</b>(AS-IS도 그렇다).</li>
 * </ul>
 *
 * <p><b>AS-IS 결함 2건 - 고쳤다</b>:
 * <ol>
 *   <li>등록·수정이 파일 루프 안에서 UPDATE를 <b>두 번</b> 실행한다("파일이 있으면" 뒤에
 *       "파일이 없으면" 주석을 달고 또 한 번). 결과는 같지만 쓸모없는 왕복이라 한 번만 한다.</li>
 *   <li>첨부 삭제 매퍼({@code deleteManualFile})가 파라미터를 {@code mnlSn} 하나만 받는데
 *       SQL에서 {@code last_updusr_id = #{lastUpdusrId}}를 쓴다 - MyBatis가 단일 파라미터를
 *       이름과 무관하게 꽂아 주므로 <b>최종수정자에 매뉴얼 번호가 들어간다</b>.
 *       로그인한 운영자 id를 넣는다.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class ManualAdminService {

    /** AS-IS가 화면 URL·화면명을 베껴 오는 공통코드 유형. */
    private static final String MENU_URL_CODE_TYPE = "MENU_URL";

    private final MnlRepository mnlRepository;
    private final MnlAdminRepository mnlAdminRepository;
    private final ManagerManualRepository managerManualRepository;
    private final MenuRepository menuRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ManualFileStorageService manualFileStorageService;

    // ------------------------------------------------------------------ 사용자매뉴얼 (5202)

    public int count(String where, String query, String startCreateDate, String endCreateDate) {
        return mnlAdminRepository.count(where, query, startCreateDate, endCreateDate);
    }

    public List<MnlAdminRepository.Row> list(String where, String query, String startCreateDate,
                                             String endCreateDate, int offset, int limit) {
        return mnlAdminRepository.list(where, query, startCreateDate, endCreateDate, offset, limit);
    }

    public Mnl find(Integer mnlSn) {
        return mnlRepository.findById(mnlSn)
                .orElseThrow(() -> new ManualException("정보가 없습니다."));
    }

    /** 등록·수정 화면의 '페이지 구분' 셀렉트 - AS-IS CodeUtils.getCodeList("MENU_URL"). */
    public List<CommonCode> menuUrlList() {
        return commonCodeRepository.findByCodeTypeOrderByOrdering(MENU_URL_CODE_TYPE).stream()
                .filter(c -> "Y".equals(c.getUseYn()))
                .toList();
    }

    /** AS-IS insertManual - 중복확인 → 저장 → 첨부 반영. */
    @Transactional
    public Mnl create(String menuSeCode, String menuSj, String menuCn, MultipartFile file,
                      Long managerUserId) {
        if (mnlRepository.findFirstByMenuSeCode(menuSeCode).isPresent()) {
            throw new ManualException("이미 등록 되어 있습니다.");
        }
        CommonCode code = codeOf(menuSeCode);

        Mnl mnl = new Mnl();
        mnl.setMenuSeCode(menuSeCode);
        // AS-IS insertManual: menu_url ← 코드 label, menu_nm ← 코드 detail
        mnl.setMenuUrl(code.getLabel());
        mnl.setMenuNm(code.getDetail());
        mnl.setMenuSj(menuSj);
        mnl.setMenuCn(menuCn);
        mnl.setInqireCo(0);
        mnl.setFrstRegisterId(managerUserId);
        mnl.setFrstRegistPnttm(LocalDateTime.now());
        attach(mnl, file, managerUserId);
        return mnlRepository.save(mnl);
    }

    /**
     * AS-IS updateManual - 화면 구분·제목·내용과 (고른 경우) 첨부를 바꾼다.
     * AS-IS는 {@code menu_url}/{@code menu_nm}을 수정 시 다시 쓰지 않아 <b>화면 구분을 바꾸면
     * URL·화면명이 예전 값으로 남는다</b>(목록은 코드를 조인해 보여주므로 화면에는 드러나지 않는다).
     * 그 결함은 눈에 보이지 않는 저장값 불일치라 코드에서 다시 채워 맞춘다.
     */
    @Transactional
    public Mnl update(Integer mnlSn, String menuSeCode, String menuSj, String menuCn,
                      MultipartFile file, Long managerUserId) {
        Mnl mnl = find(mnlSn);
        CommonCode code = codeOf(menuSeCode);
        mnl.setMenuSeCode(menuSeCode);
        mnl.setMenuUrl(code.getLabel());
        mnl.setMenuNm(code.getDetail());
        mnl.setMenuSj(menuSj);
        mnl.setMenuCn(menuCn);
        attach(mnl, file, managerUserId);
        mnl.setLastUpdusrId(managerUserId);
        mnl.setLastUpdtPnttm(LocalDateTime.now());
        return mnlRepository.save(mnl);
    }

    /** AS-IS deleteManual - 행 삭제. 디스크 파일은 AS-IS도 남긴다(첨부 삭제 버튼만 지운다). */
    @Transactional
    public void delete(Integer mnlSn) {
        mnlRepository.findById(mnlSn).ifPresent(mnlRepository::delete);
    }

    /**
     * AS-IS manualListDelete - 목록에서 체크한 건을 지운다.
     * 하나도 선택하지 않으면 AS-IS는 {@code "FAIL"}을 돌려주고 화면이 "오류가 발생했습니다."를 띄운다.
     *
     * @return AS-IS와 같은 결과코드 - 모두 지웠으면 {@code SUCC}, 선택이 없으면 {@code FAIL}
     */
    @Transactional
    public String deleteList(List<Integer> mnlSns) {
        if (mnlSns == null || mnlSns.isEmpty()) {
            return "FAIL";
        }
        for (Integer mnlSn : mnlSns) {
            if (mnlSn != null) {
                delete(mnlSn);
            }
        }
        return "SUCC";
    }

    /** AS-IS deleteItemImageByItemId - 디스크 파일을 지우고 행의 파일 컬럼을 비운다. */
    @Transactional
    public void deleteFile(Integer mnlSn, Long managerUserId) {
        Mnl mnl = find(mnlSn);
        manualFileStorageService.delete(mnl.getFileNm());
        mnl.setFileNm(null);
        mnl.setOrginlFileNm(null);
        mnl.setFileTy(null);
        // AS-IS는 여기에 매뉴얼 번호가 들어간다(클래스 주석의 결함 2) - 운영자 id를 넣는다
        mnl.setLastUpdusrId(managerUserId);
        mnl.setLastUpdtPnttm(LocalDateTime.now());
        mnlRepository.save(mnl);
    }

    private void attach(Mnl mnl, MultipartFile file, Long managerUserId) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String storedName = manualFileStorageService.store(file);
        mnl.setFileNm(storedName);
        mnl.setFileTy(manualFileStorageService.extensionOf(storedName));
        mnl.setOrginlFileNm(file.getOriginalFilename());
        mnl.setLastUpdusrId(managerUserId);
    }

    private CommonCode codeOf(String menuSeCode) {
        return menuUrlList().stream()
                .filter(c -> c.getId().equals(menuSeCode))
                .findFirst()
                .orElseThrow(() -> new ManualException("페이지 구분을 선택해 주세요."));
    }

    // ---------------------------------------------------------------- 관리자매뉴얼 (5201)

    /** 2단 묶음 하나 - 화면에서 표 하나로 그린다. */
    public record ManagerGroup(ManagerManualRepository.Dept2 group,
                               List<ManagerManualRepository.Dept3> menus) {
    }

    /**
     * AS-IS getAllMenuList - 로그인한 운영자가 볼 수 있는 메뉴를 2단으로 묶어 돌려준다.
     *
     * <p>AS-IS는 사용자의 역할 목록에서 {@code ROLE_ADMIN_1~8} 중 하나를 골라 쓰는데
     * (반복문이 첫 바퀴에 break하므로 사실상 <b>첫 역할</b>이다) TO-BE는 담당자 권한이
     * {@code op_manager.authority} 한 칸이라 그 값을 그대로 쓴다.
     */
    public List<ManagerGroup> managerTree(String authority) {
        if (authority == null || authority.isBlank()) {
            return List.of();
        }
        List<ManagerGroup> groups = new ArrayList<>();
        for (ManagerManualRepository.Dept2 dept2 : managerManualRepository.dept2(authority)) {
            groups.add(new ManagerGroup(dept2, managerManualRepository.dept3(authority, dept2.menuId())));
        }
        return groups;
    }

    /** AS-IS getOpMenu - 등록 팝업이 보여줄 메뉴 한 건. */
    public ManagerManualRepository.Dept3 managerMenu(Integer menuId) {
        ManagerManualRepository.Dept3 menu = managerManualRepository.menu(menuId);
        if (menu == null) {
            throw new ManualException("정보가 없습니다.");
        }
        return menu;
    }

    /**
     * AS-IS insertManagerManual + updateManagerManual - {@code op_menu}에 파일을 붙인다.
     * <b>파일을 고르지 않으면 아무 일도 하지 않는다</b>(AS-IS도 그렇다 - 등록 외 바꿀 값이 없다).
     */
    @Transactional
    public void attachManagerManual(Integer menuId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ManualException("정보가 없습니다."));
        String storedName = manualFileStorageService.store(file);
        menu.setFileNm(storedName);
        menu.setOrginlFileNm(file.getOriginalFilename());
        menuRepository.save(menu);
    }

    /** AS-IS managerDeleteItemImageByItemId - 디스크 파일을 지우고 메뉴의 파일 컬럼을 비운다. */
    @Transactional
    public void deleteManagerManualFile(Integer menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ManualException("정보가 없습니다."));
        manualFileStorageService.delete(menu.getFileNm());
        menu.setFileNm(null);
        menu.setOrginlFileNm(null);
        menuRepository.save(menu);
    }

    /**
     * AS-IS 화면이 쓰는 {@code role} 값 - {@code ROLE_ADMIN_1~4}(시스템·행안부)면 {@code SYSTEM},
     * 그 외는 {@code SYSTEM_NO}다. {@code SYSTEM}일 때만 메뉴명이 <b>등록 팝업 링크</b>가 된다
     * (그 외는 글자만 - 다운로드는 누구나 된다).
     */
    public static String screenRole(String authority) {
        if ("ROLE_ADMIN_1".equals(authority) || "ROLE_ADMIN_2".equals(authority)
                || "ROLE_ADMIN_3".equals(authority) || "ROLE_ADMIN_4".equals(authority)) {
            return "SYSTEM";
        }
        return "SYSTEM_NO";
    }
}
