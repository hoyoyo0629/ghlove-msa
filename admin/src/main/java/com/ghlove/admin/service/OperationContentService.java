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
    private static final DateTimeFormatter NOTICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final NoticeRepository noticeRepository;
    private final BannerRepository bannerRepository;
    private final PopupRepository popupRepository;

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
    public Banner createBanner(String title, String contents, String linkUrl, String imageUrl, Integer displayOrder) {
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
        banner.setCreatedDate(LocalDateTime.now());
        return bannerRepository.save(banner);
    }

    public Banner banner(Integer id) {
        return bannerRepository.findById(id).orElseThrow(() -> new ContentException("배너를 찾을 수 없습니다."));
    }

    @Transactional
    public Banner updateBanner(Integer id, String title, String contents, String linkUrl, String imageUrl, Integer displayOrder) {
        if (title == null || title.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Banner banner = banner(id);
        banner.setTitle(title);
        banner.setContents(contents);
        banner.setLinkUrl(linkUrl);
        banner.setImageUrl(imageUrl);
        banner.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        return bannerRepository.save(banner);
    }

    private static final int MAIN_BANNER_MAX = 6;

    /** 메인화면 배너 캐러셀 - AS-IS와 동일하게 최대 6개까지만 노출한다(노출순서 오름차순). */
    public List<Banner> activeBanners() {
        return bannerRepository.findByDisplayFlagOrderByDisplayOrderAsc(USE_Y).stream()
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

    @Transactional
    public Popup createPopup(String subject, String content, String popupType, String startDate, String endDate) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Popup popup = new Popup();
        popup.setSubject(subject);
        popup.setContent(content);
        popup.setPopupType(popupType);
        popup.setStartDate(startDate);
        popup.setEndDate(endDate);
        popup.setUseYn(USE_Y);
        popup.setCreatedDate(LocalDateTime.now());
        return popupRepository.save(popup);
    }

    @Transactional
    public Popup togglePopup(Integer id) {
        Popup popup = popupRepository.findById(id).orElseThrow(() -> new ContentException("팝업을 찾을 수 없습니다."));
        popup.setUseYn(USE_Y.equals(popup.getUseYn()) ? USE_N : USE_Y);
        return popupRepository.save(popup);
    }
}
