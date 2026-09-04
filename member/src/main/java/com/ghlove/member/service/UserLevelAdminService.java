package com.ghlove.member.service;

import com.ghlove.member.domain.UserLevel;
import com.ghlove.member.domain.UserLevelLog;
import com.ghlove.member.repository.UserLevelLogRepository;
import com.ghlove.member.repository.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * D11 회원등급 관리 (AS-IS UserLevelManagerController, docs/as-is-admin-gap-deep-audit-part2.md
 * D11 참고). GROUP_CODE 라이브 데이터가 전부 'default' 단일값이라 그룹 CRUD는 스코프아웃하고
 * 등급 CRUD만 구현한다(UserLevel.DEFAULT_GROUP_CODE로 고정).
 */
@Service
@RequiredArgsConstructor
public class UserLevelAdminService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserLevelRepository userLevelRepository;
    private final UserLevelLogRepository userLevelLogRepository;

    public List<UserLevel> list() {
        return userLevelRepository.findByGroupCodeOrderByDepthAsc(UserLevel.DEFAULT_GROUP_CODE);
    }

    public UserLevel get(Integer levelId) {
        return userLevelRepository.findById(levelId)
                .orElseThrow(() -> new MemberException("회원등급을 찾을 수 없습니다: " + levelId));
    }

    @Transactional
    public UserLevel create(String levelName, Integer depth, Integer priceStart, Integer priceEnd,
                             Double discountRate, Double pointRate, Integer shippingCouponCount,
                             Integer retentionPeriod, Integer referencePeriod, Integer exceptReferencePeriod,
                             String fileName) {
        if (levelName == null || levelName.isBlank()) {
            throw new MemberException("등급명을 입력해 주세요.");
        }
        UserLevel level = new UserLevel();
        level.setLevelId(nextLevelId());
        level.setGroupCode(UserLevel.DEFAULT_GROUP_CODE);
        level.setDepth(depth != null ? depth : 0);
        level.setLevelName(levelName);
        level.setFileName(fileName);
        level.setPriceStart(nz(priceStart));
        level.setPriceEnd(nz(priceEnd));
        level.setDiscountRate(discountRate != null ? discountRate : 0.0);
        level.setPointRate(pointRate != null ? pointRate : 0.0);
        level.setShippingCouponCount(nz(shippingCouponCount));
        level.setRetentionPeriod(nz(retentionPeriod));
        level.setReferencePeriod(nz(referencePeriod));
        level.setExceptReferencePeriod(nz(exceptReferencePeriod));
        level.setCreatedDate(now());
        return userLevelRepository.save(level);
    }

    @Transactional
    public void update(Integer levelId, String levelName, Integer depth, Integer priceStart, Integer priceEnd,
                        Double discountRate, Double pointRate, Integer shippingCouponCount,
                        Integer retentionPeriod, Integer referencePeriod, Integer exceptReferencePeriod,
                        String fileName) {
        UserLevel level = get(levelId);
        if (levelName == null || levelName.isBlank()) {
            throw new MemberException("등급명을 입력해 주세요.");
        }
        level.setLevelName(levelName);
        level.setDepth(depth != null ? depth : level.getDepth());
        level.setPriceStart(nz(priceStart));
        level.setPriceEnd(nz(priceEnd));
        level.setDiscountRate(discountRate != null ? discountRate : 0.0);
        level.setPointRate(pointRate != null ? pointRate : 0.0);
        level.setShippingCouponCount(nz(shippingCouponCount));
        level.setRetentionPeriod(nz(retentionPeriod));
        level.setReferencePeriod(nz(referencePeriod));
        level.setExceptReferencePeriod(nz(exceptReferencePeriod));
        if (fileName != null && !fileName.isBlank()) {
            level.setFileName(fileName);
        }
        userLevelRepository.save(level);
    }

    /** 삭제 - 이 등급을 현재 쓰고 있는 회원이 있으면 거부한다. 회원별 "현재 등급"은
     *  OP_USER_LEVEL_LOG의 가장 최근 로그 행으로 판단한다(회원-등급을 직접 연결하는 라이브
     *  컬럼이 아직 없다 - 등급 자동산정 배치는 이번 라운드 스코프 밖). */
    @Transactional
    public void delete(Integer levelId) {
        long usage = usageCount(levelId);
        if (usage > 0) {
            throw new MemberException("이 등급을 사용 중인 회원이 " + usage + "명 있어 삭제할 수 없습니다.");
        }
        if (!userLevelRepository.existsById(levelId)) {
            throw new MemberException("회원등급을 찾을 수 없습니다.");
        }
        userLevelRepository.deleteById(levelId);
    }

    public long usageCount(Integer levelId) {
        List<UserLevelLog> logsForLevel = userLevelLogRepository.findByLevelIdOrderByCreatedDateDesc(levelId);
        Map<Long, List<UserLevelLog>> byUser = logsForLevel.stream()
                .collect(Collectors.groupingBy(UserLevelLog::getUserId));
        long count = 0;
        for (Long userId : byUser.keySet()) {
            List<UserLevelLog> allForUser = userLevelLogRepository.findByUserIdOrderByCreatedDateDesc(userId);
            UserLevelLog latest = allForUser.stream()
                    .max(Comparator.comparing(UserLevelLog::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(null);
            if (latest != null && levelId.equals(latest.getLevelId())) {
                count++;
            }
        }
        return count;
    }

    private Integer nextLevelId() {
        return userLevelRepository.findAll().stream()
                .map(UserLevel::getLevelId).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private static Integer nz(Integer v) {
        return v != null ? v : 0;
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }
}
