package com.ghlove.donation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * admin 일반회원관리(메뉴 4101)가 쓰는 회원별 기부 조회 - AS-IS
 * {@code slave-generalcustomer-mapper.xml}의 기부 쪽 쿼리를 그대로 옮긴 <b>조회 전용</b>
 * 레포지토리다.
 *
 * <ul>
 *   <li>{@code getGeneralCustomerCumulativeTotal} - 누적 기부금액·발생포인트</li>
 *   <li>{@code getGeneralCustomerCntrListByParam} - 상세화면의 기부내역 목록</li>
 *   <li>{@code getGeneralCustomerPointListByParam}의 OCC(적립) 쪽 - 포인트 내역의 절반.
 *       나머지 절반(USE)은 {@code g_cntr_use_point}를 가진 point 서비스가 내려주고 admin이
 *       합쳐서 정렬·잔액계산한다(AS-IS는 한 DB라 UNION ALL 한 방이었다).</li>
 *   <li>목록화면의 기부누적액·포인트잔액 컬럼용 일괄 집계</li>
 * </ul>
 *
 * <p><b>AS-IS 조건의 TO-BE 번역</b>: AS-IS는 "납부완료된 기부"를
 * {@code STTEMNT_PAY_DE IS NOT NULL AND DELETE_AT = 'N' AND CNTR_STTUS_CODE = '200'}으로
 * 집었다. 이 프로젝트는 상태코드를 <b>낱말</b>로 쓰고({@code COMPLETED}/{@code REQUESTED}/
 * {@code CANCELLED} - 실측도 그렇다) {@code STTEMNT_PAY_DE}(납부일)는 아직 어디서도 쓰지 않는다
 * (납부 게이트웨이 이식이 별도 라운드로 결정돼 있다 - [[donation-payment-gateway-port-decision]]).
 * 그래서 AS-IS의 "완료 + 납부일 있음"에 대응하는 TO-BE 조건은
 * {@code cntr_sttus_code = 'COMPLETED' AND delete_at = 'N'}이다 - AS-IS 문자열을 그대로 쓰면
 * ('200', 납부일 NOT NULL) 모든 화면이 0건이 된다. donation의 다른 누적 조회
 * ({@code DonationTotalAdminApiController})도 같은 기준이다.
 *
 * <p>엔티티({@code Donation})가 cntr_point·cntr_blce_point·sttemnt_pay_de·elctrn_pay_no를
 * 매핑하지 않고 있어 네이티브 쿼리로 읽는다 - 조회 전용이라 엔티티를 넓히지 않는 쪽이 안전하다.
 */
@Repository
@RequiredArgsConstructor
public class MemberDonationAdminRepository {

    /** AS-IS {@code CNTR_STTUS_CODE = '200'}(납부완료)에 대응하는 TO-BE 값. */
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final EntityManager entityManager;

    /** 누적 기부금액·발생포인트 (AS-IS 누적합계의 g_cntr 쪽 두 칸). */
    public record CumulativeRow(BigDecimal totalCntrAmt, BigDecimal totalCntrPoint) {
    }

    /**
     * 상세화면 기부내역 한 행. 기부형태(cntrPathCode)·민간연계기관(linkInsttCd) <b>라벨은
     * admin의 OP_COMMON_CODE</b>에 있어 코드만 내려주고 admin이 붙인다(AS-IS는 한 DB라 조인했다).
     */
    public record CntrRow(String cntrDe, String cntrUpperLocgovNm, String cntrLocgovNm,
                          String cntrPathCode, String linkInsttCd, BigDecimal cntrAmt,
                          BigDecimal cntrPoint, String elctrnPayNo, String sttemntPayDe) {
    }

    /** 포인트 내역의 적립(OCC) 행 - admin이 point의 사용(USE) 행과 합쳐 쓴다. */
    public record PointOccRow(String cntrDe, String cntrLocgovCode, String cntrUpperLocgovNm,
                              String cntrLocgovNm, BigDecimal cntrAmt, BigDecimal cntrPoint,
                              String frstRegistPnttm) {
    }

    public CumulativeRow cumulativeTotal(Long userId) {
        Query q = entityManager.createNativeQuery("""
                select coalesce(sum(c.cntr_amt), 0), coalesce(sum(c.cntr_point), 0)
                  from donation.g_cntr c
                 where c.user_id = :userId
                   and c.cntr_sttus_code = :status
                   and c.delete_at = 'N'
                """);
        q.setParameter("userId", userId);
        q.setParameter("status", STATUS_COMPLETED);
        Object[] row = (Object[]) q.getSingleResult();
        return new CumulativeRow(decimal(row[0]), decimal(row[1]));
    }

    /** AS-IS getGeneralCustomerCntrCountByParam - 지자체 INNER JOIN이라 지자체가 없으면 빠진다. */
    public int cntrCount(Long userId) {
        Query q = entityManager.createNativeQuery("""
                select count(*)
                  from donation.g_cntr c
                  join donation.g_locgov l on l.locgov_code = c.cntr_locgov_code
                 where c.user_id = :userId
                   and c.cntr_sttus_code = :status
                   and c.delete_at = 'N'
                """);
        q.setParameter("userId", userId);
        q.setParameter("status", STATUS_COMPLETED);
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS getGeneralCustomerCntrListByParam - 정렬은 등록시각 내림차순. */
    @SuppressWarnings("unchecked")
    public List<CntrRow> cntrList(Long userId, int startRow, int size) {
        Query q = entityManager.createNativeQuery("""
                select c.cntr_de, l.upper_locgov_nm, l.locgov_nm, c.cntr_path_code, c.link_instt_cd,
                       c.cntr_amt, c.cntr_point, c.elctrn_pay_no, c.sttemnt_pay_de
                  from donation.g_cntr c
                  join donation.g_locgov l on l.locgov_code = c.cntr_locgov_code
                 where c.user_id = :userId
                   and c.cntr_sttus_code = :status
                   and c.delete_at = 'N'
                 order by c.frst_regist_pnttm desc
                 offset :startRow limit :size
                """);
        q.setParameter("userId", userId);
        q.setParameter("status", STATUS_COMPLETED);
        q.setParameter("startRow", startRow);
        q.setParameter("size", size);
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new CntrRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]),
                        decimal(r[5]), decimal(r[6]), str(r[7]), str(r[8])))
                .toList();
    }

    /** 포인트 내역의 적립(OCC) 행 전체 - 회원 한 명 분량이라 페이징 없이 모두 내려준다. */
    @SuppressWarnings("unchecked")
    public List<PointOccRow> pointOccRows(Long userId) {
        Query q = entityManager.createNativeQuery("""
                select c.cntr_de, c.cntr_locgov_code, l.upper_locgov_nm, l.locgov_nm,
                       c.cntr_amt, c.cntr_point, c.frst_regist_pnttm
                  from donation.g_cntr c
                  left join donation.g_locgov l on l.locgov_code = c.cntr_locgov_code
                 where c.user_id = :userId
                   and c.cntr_sttus_code = :status
                   and c.delete_at = 'N'
                """);
        q.setParameter("userId", userId);
        q.setParameter("status", STATUS_COMPLETED);
        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new PointOccRow(str(r[0]), str(r[1]), str(r[2]), str(r[3]),
                        decimal(r[4]), decimal(r[5]), str(r[6])))
                .toList();
    }

    /**
     * 목록화면(4101)의 기부누적액·발생포인트 일괄 조회 - 행마다 호출하면 N+1이 된다.
     * 포인트잔액은 admin이 여기 발생포인트에서 point의 사용포인트를 빼서 만든다(AS-IS 공식).
     */
    @SuppressWarnings("unchecked")
    public Map<Long, CumulativeRow> totalsByUserIds(List<Long> userIds) {
        Map<Long, CumulativeRow> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        Query q = entityManager.createNativeQuery("""
                select c.user_id, coalesce(sum(c.cntr_amt), 0), coalesce(sum(c.cntr_point), 0)
                  from donation.g_cntr c
                 where c.user_id in (:userIds)
                   and c.cntr_sttus_code = :status
                   and c.delete_at = 'N'
                 group by c.user_id
                """);
        q.setParameter("userIds", userIds);
        q.setParameter("status", STATUS_COMPLETED);
        List<Object[]> rows = q.getResultList();
        for (Object[] r : rows) {
            result.put(((Number) r[0]).longValue(), new CumulativeRow(decimal(r[1]), decimal(r[2])));
        }
        return result;
    }

    /** PostgreSQL이 CASE/문자 리터럴을 bpchar로 잡으면 Hibernate가 Character를 주므로 toString으로 받는다. */
    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal d ? d : new BigDecimal(value.toString());
    }
}
