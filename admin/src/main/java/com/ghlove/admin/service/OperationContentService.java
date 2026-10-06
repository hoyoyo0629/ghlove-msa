package com.ghlove.admin.service;

import com.ghlove.admin.domain.Banner;
import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.domain.Popup;
import com.ghlove.admin.repository.BannerRepository;
import com.ghlove.admin.repository.NoticeRepository;
import com.ghlove.admin.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/** 공지사항/배너/팝업 관리 (SFR-007 "운영 설정 및 관리자 기능"). */
@Service
@RequiredArgsConstructor
public class OperationContentService {

    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    /** AS-IS 팝업상태(POPUP_CLOSE) - '1'=사용 '2'=일시정지 '3'=종료 (popup/form.jsp 라디오). */
    private static final String POPUP_CLOSE_IN_USE = "1";
    /** AS-IS 팝업형태(POPUP_STYLE) '3'=이미지등록 - 이미지를 쓰는 유일한 형태다. */
    private static final String POPUP_STYLE_IMAGE = "3";
    private static final String BANNER_TYPE_MAIN = "MAIN";
    private static final java.util.Set<String> BANNER_TYPES = java.util.Set.of("MAIN", "LOGIN_WEB", "LOGIN_MOBILE");
    private static final DateTimeFormatter NOTICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final NoticeRepository noticeRepository;
    private final BannerRepository bannerRepository;
    private final PopupRepository popupRepository;
    private final PopupImageStorageService popupImageStorageService;

    // ---- 공지사항 ----

    public List<Notice> notices() {
        return noticeRepository.findAllByOrderByNoticeIdDesc();
    }

    public Notice notice(Integer id) {
        return noticeRepository.findById(id).orElseThrow(() -> new ContentException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public Notice createNotice(String subject, String content, String categoryCode, String locgovCode) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = new Notice();
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setCategoryCode(categoryCode);
        notice.setLocgovCode(locgovCode == null || locgovCode.isBlank() ? null : locgovCode);
        notice.setCreatedDate(NOTICE_DATE_FORMAT.format(LocalDateTime.now()));
        notice.setUseYn(USE_Y);
        notice.setHits(0);
        return noticeRepository.save(notice);
    }

    /** AS-IS give-notice의 edit/{noticeId} (기금사업소개 > 지자체공지사항 CRUD 재현). */
    @Transactional
    public Notice updateNotice(Integer id, String subject, String content, String categoryCode, String locgovCode) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = notice(id);
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setCategoryCode(categoryCode);
        notice.setLocgovCode(locgovCode == null || locgovCode.isBlank() ? null : locgovCode);
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Integer id) {
        noticeRepository.delete(notice(id));
    }

    @Transactional
    public Notice toggleNotice(Integer id) {
        Notice notice = notice(id);
        notice.setUseYn(USE_Y.equals(notice.getUseYn()) ? USE_N : USE_Y);
        return noticeRepository.save(notice);
    }

    // ---- 배너 ----

    public List<Banner> banners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Banner createBanner(String title, String contents, String linkUrl, String imageUrl, Integer displayOrder,
                                String bannerType) {
        if (title == null || title.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Banner banner = new Banner();
        banner.setTitle(title);
        banner.setContents(contents);
        banner.setLinkUrl(linkUrl);
        banner.setImageUrl(imageUrl);
        banner.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        banner.setDisplayFlag(USE_Y);
        banner.setBannerType(normalizeBannerType(bannerType));
        banner.setCreatedDate(NOTICE_DATE_FORMAT.format(LocalDateTime.now()));
        return bannerRepository.save(banner);
    }

    public Banner banner(Integer id) {
        return bannerRepository.findById(id).orElseThrow(() -> new ContentException("배너를 찾을 수 없습니다."));
    }

    /** 수정화면 진입용 - AS-IS는 없는 배너면 pageValidFlag='N'으로 "잘못된 접근" 처리를 한다. */
    public Banner bannerOrNull(Integer id) {
        return id == null ? null : bannerRepository.findById(id).orElse(null);
    }

    /**
     * AS-IS mainBannerCreateProcess - 배너명·내용·이미지링크·사용여부 + PC/모바일 이미지.
     * 노출순서는 AS-IS가 등록 시 받지 않고 목록의 순서 저장으로만 바꾸므로 마지막 순서 뒤에 붙인다.
     */
    @Transactional
    public Banner createMainBanner(String title, String contents, String linkUrl, String displayFlag,
                                    String pcFileName, String pcOrgFileName,
                                    String mFileName, String mOrgFileName) {
        if (title == null || title.isBlank()) {
            throw new ContentException("배너명을 입력해 주세요.");
        }
        Banner banner = new Banner();
        banner.setTitle(title);
        banner.setContents(contents);
        banner.setLinkUrl(linkUrl);
        banner.setDisplayFlag(USE_Y.equals(displayFlag) ? USE_Y : USE_N);
        banner.setPcFileName(pcFileName);
        banner.setPcOrgFileName(pcOrgFileName);
        banner.setMFileName(mFileName);
        banner.setMOrgFileName(mOrgFileName);
        banner.setBannerType(normalizeBannerType(null));
        banner.setDisplayOrder(nextBannerDisplayOrder());
        banner.setCreatedDate(NOTICE_DATE_FORMAT.format(LocalDateTime.now()));
        return bannerRepository.save(banner);
    }

    /** AS-IS mainBannerEditProcess - 이미지는 새로 올린 쪽만 바꾼다. */
    @Transactional
    public Banner updateMainBanner(Integer id, String title, String contents, String linkUrl, String displayFlag,
                                    String pcFileName, String pcOrgFileName,
                                    String mFileName, String mOrgFileName) {
        if (title == null || title.isBlank()) {
            throw new ContentException("배너명을 입력해 주세요.");
        }
        Banner banner = banner(id);
        banner.setTitle(title);
        banner.setContents(contents);
        banner.setLinkUrl(linkUrl);
        banner.setDisplayFlag(USE_Y.equals(displayFlag) ? USE_Y : USE_N);
        if (pcFileName != null) {
            banner.setPcFileName(pcFileName);
            banner.setPcOrgFileName(pcOrgFileName);
        }
        if (mFileName != null) {
            banner.setMFileName(mFileName);
            banner.setMOrgFileName(mOrgFileName);
        }
        return bannerRepository.save(banner);
    }

    /**
     * AS-IS changeDisplayOrder - 목록의 순서 셀렉트들을 "bannerId|순서" 문자열 배열로 받아 반영한다.
     * 중복 순서는 화면 JS(validator)가 먼저 막는다.
     */
    @Transactional
    public void changeBannerDisplayOrder(List<String> displayOrderList) {
        if (displayOrderList == null) {
            return;
        }
        for (String entry : displayOrderList) {
            if (entry == null || !entry.contains("|")) {
                continue;
            }
            String[] parts = entry.split("\\|", 2);
            try {
                Integer bannerId = Integer.valueOf(parts[0].trim());
                Integer order = Integer.valueOf(parts[1].trim());
                bannerRepository.findById(bannerId).ifPresent(banner -> {
                    banner.setDisplayOrder(order);
                    bannerRepository.save(banner);
                });
            } catch (NumberFormatException e) {
                // AS-IS도 잘못된 값은 조용히 건너뛴다
            }
        }
    }

    private int nextBannerDisplayOrder() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(Banner::getDisplayOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    @Transactional
    public Banner updateBanner(Integer id, String title, String contents, String linkUrl, String imageUrl,
                                Integer displayOrder, String bannerType) {
        if (title == null || title.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Banner banner = banner(id);
        banner.setTitle(title);
        banner.setContents(contents);
        banner.setLinkUrl(linkUrl);
        banner.setImageUrl(imageUrl);
        banner.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        banner.setBannerType(normalizeBannerType(bannerType));
        return bannerRepository.save(banner);
    }

    /** AS-IS UserLoginBannerManagerController가 별도 화면으로 관리하던 로그인(웹/모바일)
     *  전용 배너를 이 배너관리 화면의 노출위치 필드로 흡수했다 - 값이 없거나 허용 범위를
     *  벗어나면 기본값(MAIN)으로 취급한다. */
    private static String normalizeBannerType(String bannerType) {
        if (bannerType == null || !BANNER_TYPES.contains(bannerType)) {
            return BANNER_TYPE_MAIN;
        }
        return bannerType;
    }

    private static final int MAIN_BANNER_MAX = 6;

    /** 메인화면 배너 캐러셀 - AS-IS와 동일하게 최대 6개까지만 노출한다(노출순서 오름차순). */
    public List<Banner> activeBanners() {
        return activeBanners(BANNER_TYPE_MAIN);
    }

    /** 노출위치별 배너 - 로그인 화면(웹/모바일)은 AS-IS도 캐러셀이 아니라 소수의 고정 배너라
     *  같은 최대개수(6) 제한을 재사용한다. */
    public List<Banner> activeBanners(String bannerType) {
        return bannerRepository.findByDisplayFlagAndBannerTypeOrderByDisplayOrderAsc(USE_Y, normalizeBannerType(bannerType)).stream()
                .limit(MAIN_BANNER_MAX)
                .toList();
    }

    public List<Notice> notices(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return notices();
        }
        return noticeRepository.findAllByOrderByNoticeIdDesc().stream()
                .filter(n -> categoryCode.equals(n.getCategoryCode()))
                .toList();
    }

    /** 고객센터 공지사항 공개 목록 (AS-IS notice/list.html). 게시중(USE_YN='Y')인 것만 노출한다. */
    public List<Notice> searchPublic(String categoryCode, String keyword, String sort) {
        List<Notice> all = noticeRepository.findByUseYnOrderByNoticeIdDesc(USE_Y);

        List<Notice> filtered = (categoryCode == null || categoryCode.isBlank())
                ? all
                : all.stream().filter(n -> categoryCode.equals(n.getCategoryCode())).toList();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            filtered = filtered.stream().filter(n -> n.getSubject() != null && n.getSubject().contains(kw)).toList();
        }

        Comparator<Notice> comparator = "CREATED_DATE__ASC".equals(sort)
                ? Comparator.comparing(Notice::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator.comparing(Notice::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        return filtered.stream().sorted(comparator).toList();
    }

    /** AS-IS는 상세 진입 시점(goDetail)에 조회수를 올린다. */
    @Transactional
    public void addHit(Integer id) {
        noticeRepository.findById(id).ifPresent(n -> n.setHits((n.getHits() == null ? 0 : n.getHits()) + 1));
    }

    /** 기금사업소개(donation :8082)의 "지자체공지사항" 탭이 크로스서비스로 조회하는
     *  지자체별 공지 목록. */
    public List<Notice> noticesByLocgov(String locgovCode) {
        return noticeRepository.findByUseYnAndLocgovCodeOrderByNoticeIdDesc(USE_Y, locgovCode);
    }

    @Transactional
    public Banner toggleBanner(Integer id) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new ContentException("배너를 찾을 수 없습니다."));
        banner.setDisplayFlag(USE_Y.equals(banner.getDisplayFlag()) ? USE_N : USE_Y);
        return bannerRepository.save(banner);
    }

    // ---- 팝업 ----

    public List<Popup> popups() {
        return popupRepository.findAllByOrderByPopupIdDesc();
    }

    /** AS-IS popupList(popup-mapper.xml) - 검색조건 적용 후 POPUP_ID DESC. */
    public List<Popup> popupSearch(com.ghlove.admin.web.support.PopupSearchParam param) {
        return popupRepository.search(param.popupCloseCondition(), param.popupStyleCondition(),
                param.startDateCondition(), param.endDateCondition(), param.queryCondition());
    }

    /**
     * storefront 공개 노출용 - 현재 노출기간 안에 있고 팝업상태가 '사용'인 팝업.
     * AS-IS `displayPopupList`(popup-mapper.xml)는
     * {@code DATE_FORMAT(NOW(),'%Y%m%d%H') BETWEEN CONCAT(START_DATE,START_TIME) AND CONCAT(END_DATE,END_TIME)
     * AND POPUP_CLOSE = 1}이다. POPUP_CLOSE는 AS-IS 등록화면(popup/form.jsp)에서 팝업상태
     * 라디오(1=사용/2=일시정지/3=종료)로 쓰이는 값이고, 예전에 이걸 "닫기버튼 노출여부"로
     * 잘못 해석해 운영자 토글값 useYn으로 판정하고 있었다 - AS-IS 기준으로 바로잡았다.
     * 팝업은 소량이라 기간 필터는 인메모리로 처리한다.
     */
    public List<Popup> displayPopups() {
        String nowHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        return popupRepository.findByPopupCloseOrderByPopupIdDesc(POPUP_CLOSE_IN_USE).stream()
                .filter(p -> withinDisplayWindow(p, nowHour))
                .toList();
    }

    /** AS-IS deletePopupData(listParam) - 목록에서 체크한 팝업 일괄 삭제. */
    @Transactional
    public void deletePopups(List<Integer> popupIds) {
        if (popupIds == null || popupIds.isEmpty()) {
            return;
        }
        popupRepository.deleteAllById(popupIds);
    }

    private boolean withinDisplayWindow(Popup p, String nowHour) {
        String start = displayBound(p.getStartDate(), p.getStartTime());
        String end = displayBound(p.getEndDate(), p.getEndTime());
        if (start == null || end == null) {
            return false; // AS-IS BETWEEN도 날짜가 없으면 노출되지 않는다
        }
        return start.compareTo(nowHour) <= 0 && nowHour.compareTo(end) <= 0;
    }

    /** yyyyMMdd(8자리) + HH(기본 00) → yyyyMMddHH. 형식이 어긋나면 null. */
    private String displayBound(String date, String time) {
        if (date == null || date.length() < 8) {
            return null;
        }
        String hh = (time == null || time.isBlank()) ? "00" : (time.length() == 1 ? "0" + time : time);
        return date.substring(0, 8) + hh;
    }

    @Transactional
    public Popup createPopup(String subject, String content, String popupType, String startDate, String endDate) {
        Popup form = new Popup();
        form.setSubject(subject);
        form.setContent(content);
        form.setPopupType(popupType);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        return createPopup(form, null);
    }

    /**
     * AS-IS insertPopup(popup-mapper.xml)의 컬럼 전부를 저장한다 - POPUP_CLOSE, POPUP_TYPE,
     * POPUP_STYLE, SUBJECT, CONTENT, START_DATE/TIME, END_DATE/TIME, WIDTH, HEIGHT, IMAGE_LINK,
     * TOP_POSITION, LEFT_POSITION, POPUP_IMAGE, BACKGROUND_COLOR. 예전엔 등록화면이 넘기는
     * 시간·크기·위치·이미지링크를 받지 않아 저장 시 버려지고 있었다.
     */
    @Transactional
    public Popup createPopup(Popup form, String popupImage) {
        if (form.getSubject() == null || form.getSubject().isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Popup popup = new Popup();
        applyPopupForm(popup, form);
        // AS-IS insertPopup: 이미지는 팝업형태가 '3'(이미지등록)일 때만 쓴다. 파일이 없으면
        // 이미지링크·배경색을 비운다(AS-IS는 popupImageFile.size()==0 분기에서 ""로 덮는다).
        if (POPUP_STYLE_IMAGE.equals(popup.getPopupStyle())) {
            if (popupImage != null) {
                popup.setPopupImage(popupImage);
                popup.setContent("");   // AS-IS saveImage 마지막 줄: 이미지 팝업은 내용을 비운다
            } else {
                popup.setImageLink("");
                popup.setBackgroundColor("");
            }
        }
        popup.setUseYn(USE_Y);
        popup.setCreatedDate(LocalDateTime.now());
        return popupRepository.save(popup);
    }

    private void applyPopupForm(Popup popup, Popup form) {
        popup.setSubject(form.getSubject());
        popup.setContent(form.getContent());
        popup.setPopupType(form.getPopupType() != null ? form.getPopupType() : "1");
        popup.setPopupStyle(form.getPopupStyle() != null ? form.getPopupStyle() : "1");
        popup.setPopupClose(form.getPopupClose() != null ? form.getPopupClose() : POPUP_CLOSE_IN_USE);
        popup.setStartDate(form.getStartDate());
        popup.setStartTime(form.getStartTime());
        popup.setEndDate(form.getEndDate());
        popup.setEndTime(form.getEndTime());
        popup.setWidth(form.getWidth());
        popup.setHeight(form.getHeight());
        popup.setTopPosition(form.getTopPosition());
        popup.setLeftPosition(form.getLeftPosition());
        popup.setImageLink(form.getImageLink());
        popup.setBackgroundColor(form.getBackgroundColor());
    }

    public Popup popup(Integer id) {
        return popupRepository.findById(id).orElseThrow(() -> new ContentException("팝업을 찾을 수 없습니다."));
    }

    /**
     * AS-IS updatePopup(PopupServiceImpl)의 이미지 분기를 그대로 옮긴다.
     * <ul>
     *   <li>형태 '3' + 새 파일 → 기존 이미지가 있으면 <b>파일까지 지우고</b> 새 이미지로 바꾼다</li>
     *   <li>형태 '3' + 새 파일 없음 → 기존 POPUP_IMAGE를 유지한다</li>
     *   <li>형태 '3'이 아니면 → 기존 이미지를 <b>파일까지 지우고</b> 컬럼을 비우고,
     *       이미지링크·배경색도 ""로 비운다</li>
     * </ul>
     * 예전 TO-BE는 "새 파일이 있으면 교체"만 해서, 이미지등록 → 텍스트입력으로 바꿔 저장해도
     * 이미지와 이미지링크가 그대로 남았고 교체·삭제한 파일이 디스크에 계속 쌓였다.
     */
    @Transactional
    public Popup updatePopup(Integer id, Popup form, String popupImage) {
        if (form.getSubject() == null || form.getSubject().isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Popup popup = popup(id);
        String previousImage = popup.getPopupImage();
        applyPopupForm(popup, form);

        if (POPUP_STYLE_IMAGE.equals(popup.getPopupStyle())) {
            if (popupImage != null) {
                if (previousImage != null) {
                    popupImageStorageService.delete(previousImage);
                }
                popup.setPopupImage(popupImage);
                popup.setContent("");
            } else {
                popup.setPopupImage(previousImage);
            }
        } else {
            if (previousImage != null) {
                popupImageStorageService.delete(previousImage);
            }
            popup.setPopupImage(null);
            popup.setImageLink("");
            popup.setBackgroundColor("");
        }
        return popupRepository.save(popup);
    }

    @Transactional
    public void deletePopup(Integer id) {
        popupRepository.deleteById(id);
    }

    /** AS-IS deletePopupImage - <b>① 디스크 파일을 지우고 ② 그다음</b> POPUP_IMAGE를 null로
     *  만든다(등록화면의 이미지 삭제 아이콘). 예전 TO-BE는 ②만 해서 파일이 남았다. */
    @Transactional
    public void deletePopupImage(Integer id) {
        Popup popup = popup(id);
        if (popup.getPopupImage() != null) {
            popupImageStorageService.delete(popup.getPopupImage());
        }
        popup.setPopupImage(null);
        popupRepository.save(popup);
    }
}
