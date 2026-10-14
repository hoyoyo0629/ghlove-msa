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
 * <p>오프라인 접수분은 공통코드 CNTR_PATH 200(오프라인 농협)·300(행정복지센터)이다 - 조건 전체는 {@link #search}.
 */
@Repository
@RequiredArgsConstructor
public class OffgiveAdminRepository {

    /** AS-IS 기부상태 숫자코드 → TO-BE 낱말. */
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

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
    /**
     * AS-IS {@code SlaveOffgiveMapper.getOffgiveList} 그대로(2026-10-14 SQL 대조 재작성):
     * <ul>
     *   <li>접수경로 200(오프라인 농협)·300(행정복지센터) 둘 다. role OFF_MAIN/OFF_SUB는 200만, OFF_CENTER는 300만.</li>
     *   <li>삭제여부 조건이 없다(AS-IS 그대로 - 삭제된 기부도 목록에 보인다).</li>
     *   <li>검색: 접수번호·전자납부번호는 <b>일치</b>, 기부지자체는 "시도 시군구" 부분일치, 지점/센터명은 접수지점명
     *       또는 행정복지센터명 부분일치.</li>
     *   <li>신청일·금액은 시작/끝이 <b>둘 다</b> 있을 때만 BETWEEN.</li>
     *   <li>지점명 조건(shRceptBankNm)은 OFF_MAIN/OFF_SUB일 때만.</li>
     *   <li>지점/센터명: 경로 300이면 행정복지센터명, 아니면 접수지점명.</li>
     *   <li>특정사업명: {@code NVL(DSGN_DNTN_BIZ_TTL, '자치단체에 기부하기')} - 특정사업이 아닌 일반(지자체) 기부는 이 문구.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public List<Row> search(String shKeyword, String shText, String startDate, String endDate,
                            BigDecimal amountFrom, BigDecimal amountTo, String asIsStatusCode,
                            String rceptBankCode, String rceptBankNm, String role) {
        StringBuilder sql = new StringBuilder("""
                select c.cntr_sn, c.cntr_sttus_code, c.elctrn_pay_no, c.sttemnt_pay_de,
                       c.cntr_locgov_code, l.upper_locgov_nm, l.locgov_nm, c.user_id, c.cntr_amt,
                       c.rcept_bank_code,
                       case when c.cntr_path_code = '300' then w.pbadms_wlfr_cntr_nm else c.rcept_bank_nm end,
                       c.frst_regist_pnttm,
                       coalesce(d.dsgn_dntn_biz_ttl, '자치단체에 기부하기'), c.cntr_point, c.rtnpsnt_reqst_code
                  from donation.g_cntr c
                  left join donation.g_dsgn_dntn_biz_mng d on d.dsgn_dntn_biz_id = c.dsgn_dntn_biz_id
                  left join donation.g_lclgv_pbadms_wlfr_cntr_mng w on cast(w.pbadms_wlfr_cntr_id as varchar) = c.rcept_bank_code
                  join donation.g_locgov l on l.locgov_code = c.cntr_locgov_code
                 where c.cntr_path_code in ('200', '300')
                """);

        if (notBlank(shText) && notBlank(shKeyword)) {
            switch (shKeyword) {
                case "CNTR_SN" -> sql.append("   and c.cntr_sn = :shText\n");
                case "LOCGOV_NM" ->
                        sql.append("   and concat(l.upper_locgov_nm, ' ', l.locgov_nm) like concat('%', :shText, '%')\n");
                case "ELCTRN_PAY_NO" -> sql.append("   and c.elctrn_pay_no = :shText\n");
                case "RCEPT_BANK_NM" -> sql.append("   and (c.rcept_bank_nm like concat('%', :shText, '%')\n"
                        + "        or w.pbadms_wlfr_cntr_nm like concat('%', :shText, '%'))\n");
                default -> { }
            }
        }
        boolean dateRange = notBlank(startDate) && notBlank(endDate);
        if (dateRange) {
            sql.append("   and substr(c.frst_regist_pnttm, 1, 8) between :startDate and :endDate\n");
        }
        boolean amountRange = amountFrom != null && amountTo != null;
        if (amountRange) {
            sql.append("   and c.cntr_amt between :amountFrom and :amountTo\n");
        }
        String status = toStatusCode(asIsStatusCode);
        if (status != null) {
            sql.append("   and c.cntr_sttus_code = :status\n");
        }
        if (notBlank(rceptBankCode)) {
            sql.append("   and c.rcept_bank_code = :rceptBankCode\n");
        }
        boolean offBranch = "OFF_MAIN".equals(role) || "OFF_SUB".equals(role);
        if (offBranch) {
            sql.append("   and c.cntr_path_code = '200'\n");
        }
        if ("OFF_CENTER".equals(role)) {
            sql.append("   and c.cntr_path_code = '300'\n");
        }
        boolean bankNmFilter = notBlank(rceptBankNm) && offBranch;
        if (bankNmFilter) {
            sql.append("   and c.rcept_bank_nm = :rceptBankNm\n");
        }
        sql.append(" order by c.frst_regist_pnttm desc");

        Query q = entityManager.createNativeQuery(sql.toString());
        if (notBlank(shText) && notBlank(shKeyword)) {
            q.setParameter("shText", shText);
        }
        if (dateRange) {
            q.setParameter("startDate", startDate);
            q.setParameter("endDate", endDate);
        }
        if (amountRange) {
            q.setParameter("amountFrom", amountFrom);
            q.setParameter("amountTo", amountTo);
        }
        if (status != null) {
            q.setParameter("status", status);
        }
        if (notBlank(rceptBankCode)) {
            q.setParameter("rceptBankCode", rceptBankCode);
        }
        if (bankNmFilter) {
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

    /**
     * 기탁서 상세 한 건 - AS-IS {@code SlaveOffgiveMapper.getOffgive}의 g_cntr·g_locgov·지정기부·변경신청 부분.
     * 회원 정보(아이디·이름·생년월일·연락처·주소)는 member, 선택 답례품·배송지는 order, 포인트는 point,
     * 시도명(WDR 공통코드)은 admin이 채운다.
     *
     * <p>금년도 누적 기부액은 AS-IS {@code YEAR(CNTR_DE)=올해 AND USER_ID AND DELETE_AT='N' AND
     * CNTR_STTUS_CODE='200' AND STTEMNT_PAY_DE IS NOT NULL}이다. 상태 200만 TO-BE 낱말 COMPLETED로 바꾸고 나머지는 그대로.
     */
    public record Detail(String cntrSn, String frstRegistPnttm, String infoAgreAt, String upperLocgovCode,
                         String locgovCode, String locgovNm, BigDecimal cntrAmt, BigDecimal cntrAmtYearTotal,
                         String rceptBankNm, String elctrnPayNo, String cntrSttusCode, Long reqId,
                         String prjSubject, Long userId) {
    }

    @SuppressWarnings("unchecked")
    public java.util.Optional<Detail> detail(String cntrSn) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                select gc.cntr_sn, gc.frst_regist_pnttm, gc.info_agre_at, gl.upper_locgov_code,
                       gl.locgov_code, gl.locgov_nm, gc.cntr_amt,
                       (select coalesce(sum(a.cntr_amt), 0) from donation.g_cntr a
                         where substr(a.cntr_de, 1, 4) = to_char(current_date, 'YYYY')
                           and a.user_id = gc.user_id and a.delete_at = 'N'
                           and a.cntr_sttus_code = :completed and a.sttemnt_pay_de is not null) as cntr_amt_year_total,
                       gc.rcept_bank_nm, gc.elctrn_pay_no, gc.cntr_sttus_code,
                       (select gcr.req_id from donation.g_cntr_reqmng gcr
                         where gcr.req_status_code = '100' and gcr.cntr_sn = gc.cntr_sn limit 1) as req_id,
                       coalesce(dpm.dsgn_dntn_biz_ttl, '자치단체기부') as prj_subject,
                       gc.user_id
                  from donation.g_cntr gc
                  left join donation.g_dsgn_dntn_biz_mng dpm on gc.dsgn_dntn_biz_id = dpm.dsgn_dntn_biz_id
                  join donation.g_locgov gl on gc.cntr_locgov_code = gl.locgov_code
                 where gc.cntr_sn = :cntrSn
                """)
                .setParameter("completed", STATUS_COMPLETED)
                .setParameter("cntrSn", cntrSn)
                .getResultList();
        return rows.stream().findFirst().map(r -> new Detail(str(r[0]), str(r[1]), str(r[2]), str(r[3]),
                str(r[4]), str(r[5]), decimal(r[6]), decimal(r[7]), str(r[8]), str(r[9]), str(r[10]),
                r[11] == null ? null : ((Number) r[11]).longValue(), str(r[12]),
                r[13] == null ? null : ((Number) r[13]).longValue()));
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
