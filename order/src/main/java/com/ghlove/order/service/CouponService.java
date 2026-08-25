package com.ghlove.order.service;

import com.ghlove.order.domain.*;
import com.ghlove.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 쿠폰 (AS-IS opmanager/coupon + coupon-regular + coupon-use, saleson CouponService/ShopUtils
 * 재구현). 관리자CRUD/발급대상판정/자동발급 트리거/체크아웃 할인적용/오프라인코드까지 이
 * 한 서비스에 모은다 - AS-IS는 여러 Manager 화면으로 나뉘어 있었지만 전부 같은
 * OP_COUPON/OP_COUPON_USER를 다루는 하나의 도메인이라 여기서는 자연스럽게 합쳐진다.
 *
 * 이 프로젝트엔 회원등급 체계가 없어 targetUserType=3(회원등급별)은 1(전체회원)과 동일하게
 * 처리한다. WEB/MOBILE/APP 채널 구분도 이 프로젝트엔 웹 채널만 있어 값만 저장하고 실제
 * 필터링에는 쓰지 않는다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ISSUE_TYPE_PERIOD = "1";
    private static final String APPLY_TYPE_PERIOD = "1";
    private static final String APPLY_TYPE_DAYS_AFTER_DOWNLOAD = "2";
    private static final String TARGET_USER_SELECTED = "2";
    private static final String TARGET_ITEM_SPECIFIC = "2";
    private static final String STATUS_DOWNLOADED = "0";
    private static final String STATUS_USED = "1";
    private static final String FLAG_Y = "Y";

    private final CouponRepository couponRepository;
    private final CouponRegularRepository couponRegularRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponOfflineRepository couponOfflineRepository;
    private final CouponTargetItemRepository couponTargetItemRepository;
    private final CouponTargetUserRepository couponTargetUserRepository;
    private final MemberClient memberClient;

    private static String today() {
        return LocalDate.now().format(DATE);
    }

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    // ==================== 관리자 CRUD ====================

    public List<Coupon> list() {
        return couponRepository.findAllByOrderByCouponIdDesc();
    }

    public Coupon get(Integer couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new OrderException("쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public Coupon create(Coupon form) {
        form.setCouponId(null);
        form.setCreatedDate(now());
        form.setUpdatedDate(now());
        return couponRepository.save(form);
    }

    @Transactional
    public Coupon update(Integer couponId, Coupon form) {
        Coupon coupon = get(couponId);
        coupon.setCouponType(form.getCouponType());
        coupon.setCouponName(form.getCouponName());
        coupon.setCouponComment(form.getCouponComment());
        coupon.setIssueType(form.getIssueType());
        coupon.setIssueStartDate(form.getIssueStartDate());
        coupon.setIssueEndDate(form.getIssueEndDate());
        coupon.setApplyType(form.getApplyType());
        coupon.setApplyDay(form.getApplyDay());
        coupon.setApplyStartDate(form.getApplyStartDate());
        coupon.setApplyEndDate(form.getApplyEndDate());
        coupon.setTargetTimeType(form.getTargetTimeType());
        coupon.setTargetUserType(form.getTargetUserType());
        coupon.setTargetUserLevel(form.getTargetUserLevel());
        coupon.setPayRestriction(form.getPayRestriction());
        coupon.setConcurrently(form.getConcurrently());
        coupon.setPayType(form.getPayType());
        coupon.setPay(form.getPay());
        coupon.setDiscountLimitPrice(form.getDiscountLimitPrice());
        coupon.setTargetItemType(form.getTargetItemType());
        coupon.setOfflineFlag(form.getOfflineFlag());
        coupon.setBirthday(form.getBirthday());
        coupon.setDownloadLimit(form.getDownloadLimit());
        coupon.setDownloadUserLimit(form.getDownloadUserLimit());
        coupon.setMultipleDownloadFlag(form.getMultipleDownloadFlag());
        coupon.setDirectInputFlag(form.getDirectInputFlag());
        coupon.setDirectInputValue(form.getDirectInputValue());
        coupon.setUpdatedDate(now());
        return couponRepository.save(coupon);
    }

    @Transactional
    public void delete(Integer couponId) {
        couponRepository.deleteById(couponId);
        couponTargetItemRepository.deleteByCouponId(couponId);
        couponTargetUserRepository.deleteByCouponId(couponId);
    }

    @Transactional
    public Coupon togglePublish(Integer couponId) {
        Coupon coupon = get(couponId);
        coupon.setCouponFlag(FLAG_Y.equals(coupon.getCouponFlag()) ? "N" : FLAG_Y);
        coupon.setDataStatusCode("1");
        coupon.setUpdatedDate(now());
        return couponRepository.save(coupon);
    }

    // ==================== 발급대상(선택회원/특정상품) 관리 ====================

    public List<CouponTargetItem> targetItemsOf(Integer couponId) {
        return couponTargetItemRepository.findByCouponId(couponId);
    }

    public List<CouponTargetUser> targetUsersOf(Integer couponId) {
        return couponTargetUserRepository.findByCouponId(couponId);
    }

    @Transactional
    public void addTargetItem(Integer couponId, Long itemId) {
        if (couponTargetItemRepository.existsByCouponIdAndItemId(couponId, itemId)) {
            return;
        }
        CouponTargetItem t = new CouponTargetItem();
        t.setCouponId(couponId);
        t.setItemId(itemId);
        t.setCreatedDate(now());
        couponTargetItemRepository.save(t);
    }

    @Transactional
    public void removeTargetItem(Integer couponId, Long itemId) {
        couponTargetItemRepository.deleteById(new CouponTargetItemId(itemId, couponId));
    }

    @Transactional
    public void addTargetUser(Integer couponId, Long userId) {
        if (couponTargetUserRepository.existsByCouponIdAndUserId(couponId, userId)) {
            return;
        }
        CouponTargetUser t = new CouponTargetUser();
        t.setCouponId(couponId);
        t.setUserId(userId);
        t.setCreatedDate(now());
        couponTargetUserRepository.save(t);
    }

    @Transactional
    public void removeTargetUser(Integer couponId, Long userId) {
        couponTargetUserRepository.deleteById(new CouponTargetUserId(userId, couponId));
    }

    // ==================== 오프라인 코드 ====================

    @Transactional
    public List<CouponOffline> generateOfflineCodes(Integer couponId, int count) {
        List<CouponOffline> created = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CouponOffline c = new CouponOffline();
            c.setCouponId(couponId);
            c.setOfflineCode(randomCode());
            c.setPublishedDate(now());
            created.add(couponOfflineRepository.save(c));
        }
        return created;
    }

    public List<CouponOffline> offlineCodesOf(Integer couponId) {
        return couponOfflineRepository.findByCouponIdOrderByCouponOfflineIdDesc(couponId);
    }

    private String randomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append('-');
            }
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** 오프라인 코드 입력으로 쿠폰 다운로드 (직접입력형은 Coupon.directInputValue 자체가 코드). */
    @Transactional
    public CouponIssue claimByOfflineCode(Long userId, String code) {
        if (code == null || code.isBlank()) {
            throw new OrderException("코드를 입력해 주세요.");
        }
        String normalized = code.trim().toUpperCase();

        List<Coupon> directMatches = couponRepository.findAll().stream()
                .filter(c -> FLAG_Y.equals(c.getCouponFlag()) && FLAG_Y.equals(c.getOfflineFlag())
                        && FLAG_Y.equals(c.getDirectInputFlag()) && normalized.equalsIgnoreCase(c.getDirectInputValue()))
                .toList();
        if (!directMatches.isEmpty()) {
            return claim(userId, directMatches.get(0).getCouponId());
        }

        CouponOffline offline = couponOfflineRepository.findByOfflineCode(normalized)
                .orElseThrow(() -> new OrderException("유효하지 않은 쿠폰 코드입니다."));
        if (FLAG_Y.equals(offline.getUsedFlag())) {
            throw new OrderException("이미 사용된 쿠폰 코드입니다.");
        }
        Coupon coupon = get(offline.getCouponId());
        CouponIssue issue = issueTo(coupon, userId);
        offline.setUsedFlag(FLAG_Y);
        offline.setUsedDate(now());
        offline.setUserId(userId);
        couponOfflineRepository.save(offline);
        return issue;
    }

    // ==================== 회원용: 다운로드 가능목록 / 다운로드 / 내 쿠폰함 ====================

    /** targetTimeType=1(일반)인, 지금 이 회원이 다운로드 가능한 쿠폰 목록. */
    public List<Coupon> claimableCoupons(Long userId) {
        String t = today();
        return couponRepository.findByCouponFlagAndTargetTimeType(FLAG_Y, "1").stream()
                .filter(c -> !FLAG_Y.equals(c.getOfflineFlag()))
                .filter(c -> withinIssuePeriod(c, t))
                .filter(c -> eligibleForUser(c, userId))
                .filter(c -> downloadable(c, userId))
                .toList();
    }

    private boolean withinIssuePeriod(Coupon c, String t) {
        if (!ISSUE_TYPE_PERIOD.equals(c.getIssueType())) {
            return true;
        }
        String start = c.getIssueStartDate();
        String end = c.getIssueEndDate();
        return (start == null || start.isBlank() || t.compareTo(start) >= 0)
                && (end == null || end.isBlank() || t.compareTo(end) <= 0);
    }

    private boolean eligibleForUser(Coupon c, Long userId) {
        if (TARGET_USER_SELECTED.equals(c.getTargetUserType())) {
            return couponTargetUserRepository.existsByCouponIdAndUserId(c.getCouponId(), userId);
        }
        return true; // 1:전체회원, 3:회원등급(체계 없어 전체와 동일)
    }

    private boolean downloadable(Coupon c, Long userId) {
        long totalIssued = couponIssueRepository.countByCouponId(c.getCouponId());
        if (c.getDownloadLimit() != null && c.getDownloadLimit() > 0 && totalIssued >= c.getDownloadLimit()) {
            return false;
        }
        long userIssued = couponIssueRepository.countByCouponIdAndUserId(c.getCouponId(), userId);
        if (!FLAG_Y.equals(c.getMultipleDownloadFlag())) {
            boolean hasUnused = couponIssueRepository.findByCouponIdAndUserId(c.getCouponId(), userId).stream()
                    .anyMatch(i -> STATUS_DOWNLOADED.equals(i.getDataStatusCode()));
            if (hasUnused) {
                return false;
            }
        }
        int userLimit = c.getDownloadUserLimit() != null ? c.getDownloadUserLimit() : -1;
        return userLimit <= 0 || userIssued < userLimit;
    }

    @Transactional
    public CouponIssue claim(Long userId, Integer couponId) {
        Coupon coupon = get(couponId);
        if (!FLAG_Y.equals(coupon.getCouponFlag())) {
            throw new OrderException("발행되지 않은 쿠폰입니다.");
        }
        if (!downloadable(coupon, userId)) {
            throw new OrderException("더 이상 다운로드할 수 없는 쿠폰입니다.");
        }
        return issueTo(coupon, userId);
    }

    /** 쿠폰 설정을 스냅샷으로 복사해 CouponIssue를 만든다 - 수동다운로드/자동발급 공통 진입점. */
    @Transactional
    public CouponIssue issueTo(Coupon coupon, Long userId) {
        CouponIssue issue = new CouponIssue();
        issue.setCouponId(coupon.getCouponId());
        issue.setUserId(userId);
        issue.setCouponType(coupon.getCouponType());
        issue.setCouponName(coupon.getCouponName());
        issue.setCouponComment(coupon.getCouponComment());
        issue.setPayRestriction(coupon.getPayRestriction());
        issue.setConcurrently(coupon.getConcurrently());
        issue.setPayType(coupon.getPayType());
        issue.setPay(coupon.getPay());
        issue.setDiscountLimitPrice(coupon.getDiscountLimitPrice());
        issue.setTargetItemType(coupon.getTargetItemType());
        issue.setDataStatusCode(STATUS_DOWNLOADED);
        issue.setDownloadDate(now());

        String applyType = coupon.getApplyType();
        issue.setApplyType(applyType);
        if (APPLY_TYPE_PERIOD.equals(applyType)) {
            issue.setApplyStartDate(coupon.getApplyStartDate());
            issue.setApplyEndDate(coupon.getApplyEndDate());
        } else if (APPLY_TYPE_DAYS_AFTER_DOWNLOAD.equals(applyType) && coupon.getApplyDay() != null) {
            issue.setApplyStartDate(today());
            issue.setApplyEndDate(LocalDate.now().plusDays(coupon.getApplyDay()).format(DATE));
        }
        issue.setCreatedDate(now());
        return couponIssueRepository.save(issue);
    }

    /** 마이페이지 "내 쿠폰함". */
    public record MyCoupons(List<CouponIssue> usable, List<CouponIssue> used) {
    }

    public MyCoupons myIssues(Long userId) {
        String t = today();
        List<CouponIssue> all = couponIssueRepository.findByUserIdOrderByCreatedDateDesc(userId);
        List<CouponIssue> usable = new ArrayList<>();
        List<CouponIssue> used = new ArrayList<>();
        for (CouponIssue i : all) {
            if (STATUS_USED.equals(i.getDataStatusCode())) {
                used.add(i);
            } else if (isExpired(i, t)) {
                used.add(i);
            } else {
                usable.add(i);
            }
        }
        return new MyCoupons(usable, used);
    }

    private boolean isExpired(CouponIssue i, String t) {
        String end = i.getApplyEndDate();
        return end != null && !end.isBlank() && t.compareTo(end) > 0;
    }

    // ==================== 체크아웃 통합 ====================

    /** 특정 상품라인에 지금 적용 가능한(미사용+기간내+상품조건 만족) 이 회원의 쿠폰 발급건. */
    public List<CouponIssue> usableIssuesForItem(Long userId, Long itemId) {
        String t = today();
        return couponIssueRepository.findByUserIdOrderByCreatedDateDesc(userId).stream()
                .filter(i -> STATUS_DOWNLOADED.equals(i.getDataStatusCode()))
                .filter(i -> !isExpired(i, t))
                .filter(i -> itemEligible(i, itemId))
                .toList();
    }

    private boolean itemEligible(CouponIssue issue, Long itemId) {
        if (!TARGET_ITEM_SPECIFIC.equals(issue.getTargetItemType())) {
            return true;
        }
        return couponTargetItemRepository.existsByCouponIdAndItemId(issue.getCouponId(), itemId);
    }

    /** 체크아웃 시점 할인 미리보기 (마감 확정 전, 검증만). */
    public long previewDiscount(Long userId, Integer couponIssueId, Long itemId, long lineTotal, int quantity) {
        CouponIssue issue = couponIssueRepository.findByCouponUserIdAndUserId(couponIssueId, userId)
                .orElseThrow(() -> new OrderException("쿠폰을 찾을 수 없습니다."));
        if (!STATUS_DOWNLOADED.equals(issue.getDataStatusCode()) || isExpired(issue, today()) || !itemEligible(issue, itemId)) {
            throw new OrderException("사용할 수 없는 쿠폰입니다.");
        }
        return CouponDiscountCalculator.discount(issue, lineTotal, quantity);
    }

    /** 주문 생성 시점에 실제로 쿠폰을 소진시키고 할인액을 확정한다 (OrderService.createOrder에서 호출). */
    @Transactional
    public long applyToOrder(Long userId, Integer couponIssueId, String orderId, Long itemId, long lineTotal, int quantity) {
        long discount = previewDiscount(userId, couponIssueId, itemId, lineTotal, quantity);
        CouponIssue issue = couponIssueRepository.findById(couponIssueId).orElseThrow();
        issue.setDataStatusCode(STATUS_USED);
        issue.setUsedDate(now());
        issue.setOrderCode(orderId);
        issue.setDiscountAmount((int) discount);
        couponIssueRepository.save(issue);
        return discount;
    }

    // ==================== 자동발급 트리거 ====================

    private static final String TARGET_TIME_SIGNUP = "2";
    private static final String TARGET_TIME_BIRTHDAY = "3";
    private static final String TARGET_TIME_AFTER_ITEM_PURCHASE = "4";
    private static final String TARGET_TIME_FIRST_PURCHASE = "5";

    private List<Long> eligibleUserIds(Coupon coupon, List<MemberClient.MemberSnapshot> members) {
        if (TARGET_USER_SELECTED.equals(coupon.getTargetUserType())) {
            return couponTargetUserRepository.findByCouponId(coupon.getCouponId()).stream()
                    .map(CouponTargetUser::getUserId).toList();
        }
        return members.stream().map(MemberClient.MemberSnapshot::userId).toList();
    }

    /** 회원가입 트리거 - member는 이벤트를 발행하지 않아 매일 배치가 "쿠폰 발급 시작일 이후
     *  가입했고 아직 이 쿠폰을 받은 적 없는" 회원을 찾아 발급한다. */
    @Transactional
    public void issueSignupCoupons() {
        List<Coupon> targets = couponRepository.findByCouponFlagAndTargetTimeType(FLAG_Y, TARGET_TIME_SIGNUP);
        if (targets.isEmpty()) {
            return;
        }
        List<MemberClient.MemberSnapshot> members = memberClient.allForResync();
        for (Coupon coupon : targets) {
            if (!withinIssuePeriod(coupon, today())) {
                continue;
            }
            Set<Long> already = couponIssueRepository.findByCouponIdOrderByCreatedDateDesc(coupon.getCouponId()).stream()
                    .map(CouponIssue::getUserId).collect(Collectors.toSet());
            for (Long userId : eligibleUserIds(coupon, members)) {
                if (!already.contains(userId)) {
                    issueTo(coupon, userId);
                }
            }
        }
    }

    /** 생일 트리거 - 매일 배치가 오늘이 생일(MMdd 일치)인 회원을 찾아 발급한다. */
    @Transactional
    public void issueBirthdayCoupons() {
        List<Coupon> targets = couponRepository.findByCouponFlagAndTargetTimeType(FLAG_Y, TARGET_TIME_BIRTHDAY);
        if (targets.isEmpty()) {
            return;
        }
        String mmdd = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
        List<MemberClient.MemberSnapshot> members = memberClient.allForResync();
        List<Long> birthdayUsers = members.stream()
                .filter(m -> m.birthday() != null && m.birthday().length() >= 8 && m.birthday().substring(4, 8).equals(mmdd))
                .map(MemberClient.MemberSnapshot::userId)
                .toList();
        String thisYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        for (Coupon coupon : targets) {
            if (!withinIssuePeriod(coupon, today())) {
                continue;
            }
            Set<Long> alreadyThisYear = couponIssueRepository.findByCouponIdOrderByCreatedDateDesc(coupon.getCouponId()).stream()
                    .filter(i -> i.getCreatedDate() != null && i.getCreatedDate().startsWith(thisYear))
                    .map(CouponIssue::getUserId).collect(Collectors.toSet());
            for (Long userId : birthdayUsers) {
                if (eligibleForUser(coupon, userId) && !alreadyThisYear.contains(userId)) {
                    issueTo(coupon, userId);
                }
            }
        }
    }

    /** 특정 상품 구매후 발행 - order.saga ORDER_CONFIRMED에서 order 서비스 자신이 직접 호출. */
    @Transactional
    public void issueAfterItemPurchase(Long userId, Long itemId) {
        List<Coupon> targets = couponRepository.findByCouponFlagAndTargetTimeType(FLAG_Y, TARGET_TIME_AFTER_ITEM_PURCHASE);
        for (Coupon coupon : targets) {
            if (!withinIssuePeriod(coupon, today()) || !eligibleForUser(coupon, userId)) {
                continue;
            }
            if (TARGET_ITEM_SPECIFIC.equals(coupon.getTargetItemType())
                    && !couponTargetItemRepository.existsByCouponIdAndItemId(coupon.getCouponId(), itemId)) {
                continue;
            }
            if (downloadable(coupon, userId)) {
                issueTo(coupon, userId);
            }
        }
    }

    /** 첫구매 발행 - 이 회원의 CONFIRMED 주문이 이번이 처음일 때만 order 서비스가 호출. */
    @Transactional
    public void issueFirstPurchaseCoupons(Long userId) {
        List<Coupon> targets = couponRepository.findByCouponFlagAndTargetTimeType(FLAG_Y, TARGET_TIME_FIRST_PURCHASE);
        for (Coupon coupon : targets) {
            if (withinIssuePeriod(coupon, today()) && eligibleForUser(coupon, userId) && downloadable(coupon, userId)) {
                issueTo(coupon, userId);
            }
        }
    }

    // ==================== 정기발행쿠폰 (AS-IS OP_COUPON_REGULAR) ====================
    // "정기발행"은 관리자가 등록한 템플릿(CouponRegular)을 발행기간 동안 매일 배치가 훑어,
    // 아직 그날의 실제 발행분(Coupon)이 없으면 하나 찍어내 곧바로 발행(couponFlag=Y) 상태로
    // 만든다. 이렇게 만든 Coupon은 이후 위의 일반 발급 경로(수동다운로드/자동트리거)를
    // 그대로 탄다 - 별도 발급경로를 새로 만들지 않기 위한 설계.

    public List<CouponRegular> listRegular() {
        return couponRegularRepository.findAllByOrderByCouponIdDesc();
    }

    public CouponRegular getRegular(Integer couponId) {
        return couponRegularRepository.findById(couponId).orElseThrow(() -> new OrderException("정기발행쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public CouponRegular createRegular(CouponRegular form) {
        form.setCouponId(null);
        form.setCreatedDate(now());
        form.setUpdatedDate(now());
        return couponRegularRepository.save(form);
    }

    @Transactional
    public CouponRegular updateRegular(Integer couponId, CouponRegular form) {
        CouponRegular r = getRegular(couponId);
        r.setCouponName(form.getCouponName());
        r.setCouponComment(form.getCouponComment());
        r.setIssueType(form.getIssueType());
        r.setIssueStartDate(form.getIssueStartDate());
        r.setIssueEndDate(form.getIssueEndDate());
        r.setTargetTimeType(form.getTargetTimeType());
        r.setTargetUserType(form.getTargetUserType());
        r.setPayRestriction(form.getPayRestriction());
        r.setConcurrently(form.getConcurrently());
        r.setPayType(form.getPayType());
        r.setPay(form.getPay());
        r.setDiscountLimitPrice(form.getDiscountLimitPrice());
        r.setTargetItemType(form.getTargetItemType());
        r.setDownloadLimit(form.getDownloadLimit());
        r.setDownloadUserLimit(form.getDownloadUserLimit());
        r.setMultipleDownloadFlag(form.getMultipleDownloadFlag());
        r.setCouponFlag(form.getCouponFlag());
        r.setUpdatedDate(now());
        return couponRegularRepository.save(r);
    }

    @Transactional
    public void deleteRegular(Integer couponId) {
        couponRegularRepository.deleteById(couponId);
    }

    /** 매일 배치: 활성 정기발행쿠폰마다 "오늘 발행분" Coupon이 없으면 하나 생성한다. */
    @Transactional
    public void reissueRegularCoupons() {
        String t = today();
        for (CouponRegular r : couponRegularRepository.findByCouponFlag(FLAG_Y)) {
            if (ISSUE_TYPE_PERIOD.equals(r.getIssueType())) {
                String start = r.getIssueStartDate();
                String end = r.getIssueEndDate();
                if ((start != null && !start.isBlank() && t.compareTo(start) < 0)
                        || (end != null && !end.isBlank() && t.compareTo(end) > 0)) {
                    continue;
                }
            }
            String marker = "[정기#" + r.getCouponId() + "/" + t + "]";
            boolean alreadyToday = couponRepository.findAll().stream()
                    .anyMatch(c -> c.getCouponComment() != null && c.getCouponComment().contains(marker));
            if (alreadyToday) {
                continue;
            }
            Coupon c = new Coupon();
            c.setCouponType(r.getCouponType());
            c.setCouponName(r.getCouponName());
            c.setCouponComment(marker + " " + (r.getCouponComment() != null ? r.getCouponComment() : ""));
            c.setIssueType("0");
            c.setApplyType("2");
            c.setApplyDay(7);
            c.setTargetTimeType("1");
            c.setTargetUserType(r.getTargetUserType());
            c.setPayRestriction(r.getPayRestriction());
            c.setConcurrently(r.getConcurrently());
            c.setPayType(r.getPayType());
            c.setPay(r.getPay());
            c.setDiscountLimitPrice(r.getDiscountLimitPrice());
            c.setTargetItemType(r.getTargetItemType());
            c.setCouponFlag(FLAG_Y);
            c.setDataStatusCode("1");
            c.setDownloadLimit(r.getDownloadLimit());
            c.setDownloadUserLimit(r.getDownloadUserLimit());
            c.setMultipleDownloadFlag(r.getMultipleDownloadFlag());
            create(c);
            log.info("정기발행쿠폰 {}({}) -> 오늘의 발행분 Coupon #{} 생성", r.getCouponId(), r.getCouponName(), c.getCouponId());
        }
    }

    // ==================== 사용내역(관리자, AS-IS coupon-use) ====================

    public List<CouponIssue> usageOf(Integer couponId) {
        return couponIssueRepository.findByCouponIdOrderByCreatedDateDesc(couponId);
    }

    public record CouponCounts(long downloadCount, long usedCount) {
    }

    public CouponCounts countsOf(Integer couponId) {
        List<CouponIssue> all = couponIssueRepository.findByCouponIdOrderByCreatedDateDesc(couponId);
        long used = all.stream().filter(i -> STATUS_USED.equals(i.getDataStatusCode())).count();
        return new CouponCounts(all.size(), used);
    }
}
