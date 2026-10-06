package com.ghlove.donation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 오프라인 기부금 접수관리 목록 (admin 메뉴 15101) - AS-IS
 * {@code OffgiveManagerController.POST /list} + offgive 매퍼의 목록 조회를 옮긴 <b>조회 전용</b>
 * 레포지토리다.
 *
 * <p>AS-IS 화면 11컬럼: 접수번호(지자체)·기부상태·전자납부번호·수납일·기부지자체·이름·기부금액·
 * 지점/센터명·신고일·특정사업명. 이름은 회원(member) 소유라 여기서는 USER_ID만 내려주고
 * admin이 member에서 받아 채운다(AS-IS는 한 DB라 조인했다).
 *
 * <p><b>AS-IS 조건의 TO-BE 번역</b>: 기부상태는 AS-IS가 숫자코드
 * ({@code 100} 신고 / {@code 200} 수납 / {@code 300} 과오납)인데 이 프로젝트는 같은 컬럼에
 * 낱말({@code REQUESTED}/{@code COMPLETED}/{@code CANCELLED})을 쓴다(실측도 그렇다).
 * 화면 라디오 값은 AS-IS대로 100/200/300을 유지하고 <b>변환은 여기 경계에서</b> 한다 -
 * 담당자 사용여부 9/2를 ACTIVE/LOCKED로 바꾼 것과 같은 방식이다.
 *
 * <p>오프라인 접수분만 보여야 하므로 {@code CNTR_PATH_CODE = '200'}(오프라인)으로 한정한다
 * (공통코드 CNTR_PATH: 100 온라인 / 200 오프라인).
 */
@Repository
@RequiredArgsConstructor
public class OffgiveAdminRepository {

    /** AS-IS 기부상태 숫자코드 → TO-BE 낱말. */
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    /** 공통코드 CNTR_PATH - 오프라인 접수. */
    private static final String PATH_OFFLINE = "200";

    private final EntityManager entityManager;

    /**
     * 목록 한 행. {@code cntrSttusCode}는 TO-BE 낱말 그대로 내려주고 화면 표기(신고/수납/과오납)는
     * admin이 만든다. {@code userId}에 해당하는 이름도 admin이 member에서 채운다.
     */
    public record Row(String cntrSn, String cntrSttusCode, String elctrnPayNo, String sttemntPayDe,
                      String cntrLocgovCode, String upperLocgovNm, String locgovNm, Long userId,
                      BigDecimal cntrAmt, String rceptBankCode, String rceptBankNm, String frstRegistPnttm,
                      String prjSubject, Long cntrPoint, String rtnpsntReqstCode) {
    }

    /**
     * AS-IS 검색조건 전체:
     * <ul>
     *   <li>검색구분 - CNTR_SN(접수번호) / LOCGOV_NM(기부 지자체) / ELCTRN_PAY_NO(전자납부번호) /
     *       RCEPT_BANK_NM(지점·센터명)</li>
     *   <li>신청일 범위(FRST_REGIST_PNTTM) - AS-IS 컨트롤러가 비면 오늘로 채운다</li>
     *   <li>금액 범위(CNTR_AMT)</li>
     *   <li>기부상태 - 100 신고 / 200 수납 / 300 과오납</li>
     *   <li>소속지점(RCEPT_BANK_CODE) - 오프라인 담당자는 자기 지점으로 고정된다</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public List<Row> search(String shKeyword, String shText, String startDate, String endDate,
                            BigDecimal amountFrom, BigDecimal amountTo, String asIsStatusCode,
                            String rceptBankCode, String rceptBankNm) {
        StringBuilder sql = new StringBuilder("""
                select c.cntr_sn, c.cntr_sttus_code, c.elctrn_pay_no, c.sttemnt_pay_de,
                       c.cntr_locgov_code, l.upper_locgov_nm, l.locgov_nm, c.user_id, c.cntr_amt,
                       c.rcept_bank_code, c.rcept_bank_nm, c.frst_regist_pnttm,
                       p.prj_subject, c.cntr_point, c.rtnpsnt_reqst_code
                  from donation.g_cntr c
                  left join donation.g_locgov l on l.locgov_code = c.cntr_locgov_code
                  left join donation.g_dsgncntr_prj_mng p on p.prj_id = c.prj_id
                 where c.cntr_path_code = :pathCode
                   and c.delete_at = 'N'
                """);

        if (notBlank(shText) && notBlank(shKeyword)) {
            switch (shKeyword) {
                case "CNTR_SN" -> sql.append("   and c.cntr_sn like concat('%', :shText, '%')\n");
                case "LOCGOV_NM" ->
                        sql.append("   and concat(l.upper_locgov_nm, ' ', l.locgov_nm) like concat('%', :shText, '%')\n");
                case "ELCTRN_PAY_NO" -> sql.append("   and c.elctrn_pay_no like concat('%', :shText, '%')\n");
                case "RCEPT_BANK_NM" -> sql.append("   and c.rcept_bank_nm like concat('%', :shText, '%')\n");
                default -> { }
            }
        }
        if (notBlank(startDate)) {
            sql.append("   and c.frst_regist_pnttm >= concat(:startDate, '000000')\n");
        }
        if (notBlank(endDate)) {
            sql.append("   and c.frst_regist_pnttm <= concat(:endDate, '235959')\n");
        }
        if (amountFrom != null) {
            sql.append("   and c.cntr_amt >= :amountFrom\n");
        }
        if (amountTo != null) {
            sql.append("   and c.cntr_amt <= :amountTo\n");
        }
        String status = toStatusCode(asIsStatusCode);
        if (status != null) {
            sql.append("   and c.cntr_sttus_code = :status\n");
        }
        if (notBlank(rceptBankCode)) {
            sql.append("   and c.rcept_bank_code = :rceptBankCode\n");
        }
        // 오프라인 부담당자·센터는 지점명까지 자기 것으로 고정된다(AS-IS shRceptBankNm)
        if (notBlank(rceptBankNm)) {
            sql.append("   and c.rcept_bank_nm = :rceptBankNm\n");
        }
        sql.append(" order by c.frst_regist_pnttm desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        q.setParameter("pathCode", PATH_OFFLINE);
        if (notBlank(shText) && notBlank(shKeyword)) {
            q.setParameter("shText", shText);
        }
        if (notBlank(startDate)) {
            q.setParameter("startDate", startDate);
        }
        if (notBlank(endDate)) {
            q.setParameter("endDate", endDate);
        }
        if (amountFrom != null) {
            q.setParameter("amountFrom", amountFrom);
        }
        if (amountTo != null) {
            q.setParameter("amountTo", amountTo);
        }
        if (status != null) {
            q.setParameter("status", status);
        }
        if (notBlank(rceptBankCode)) {
            q.setParameter("rceptBankCode", rceptBankCode);
        }
        if (notBlank(rceptBankNm)) {
            q.setParameter("rceptBankNm", rceptBankNm);
        }

        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new Row(str(r[0]), str(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]), str(r[6]),
                        r[7] == null ? null : ((Number) r[7]).longValue(), decimal(r[8]),
                        str(r[9]), str(r[10]), str(r[11]), str(r[12]),
                        r[13] == null ? null : ((Number) r[13]).longValue(), str(r[14])))
                .toList();
    }

    /** AS-IS 기부상태 라디오값(100/200/300)을 TO-BE 낱말로. 빈 값(전체)이면 null. */
    private static String toStatusCode(String asIsStatusCode) {
        if (asIsStatusCode == null || asIsStatusCode.isBlank()) {
            return null;
        }
        return switch (asIsStatusCode) {
            case "100" -> STATUS_REQUESTED;
            case "200" -> STATUS_COMPLETED;
            case "300" -> STATUS_CANCELLED;
            default -> null;
        };
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof BigDecimal d ? d : new BigDecimal(value.toString());
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
