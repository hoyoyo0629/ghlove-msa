package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.AdminMainClient.ChartRow;
import com.ghlove.admin.service.AdminMainClient.CountRow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 메인화면 - AS-IS {@code MainServiceImpl}의 opmanager 메인 메서드 이식.
 * 다른 서비스 소유 데이터는 {@link AdminMainClient}로, admin 소유 표(G_MNGR_REQST·OP_QNA·OP_CALL·REPORT_DASHBOARD)는
 * 여기서 AS-IS 매퍼 SQL 그대로 읽는다.
 */
@Service
@RequiredArgsConstructor
public class AdminMainService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AdminMainClient client;
    private final LocgovClient locgovClient;
    private final EntityManager entityManager;

    /**
     * AS-IS {@code LocgovServiceImpl.getLoginUserAdminRoleCheck()} - 5·6 LOC / 7·8 OFF / 11 WCM / 그 외(1~4·10 포함) SYS.
     * 화면의 adminRoleType(JSP가 따로 정한다)과 다르다 - 10은 화면에선 LOC인데 쿼리는 SYS로 돈다(AS-IS 그대로).
     */
    public static String queryRole(Manager manager) {
        String auth = manager.getAuthority();
        if ("ROLE_ADMIN_5".equals(auth) || "ROLE_ADMIN_6".equals(auth)) return "LOC";
        if ("ROLE_ADMIN_7".equals(auth) || "ROLE_ADMIN_8".equals(auth)) return "OFF";
        if ("ROLE_ADMIN_11".equals(auth)) return "WCM";
        return "SYS";
    }

    /** AS-IS main/index.jsp 상단 c:choose - 7·8·11 OFF / 5·6·10 LOC / 1~4 SYS / 그 외 없음(아무 위젯도 안 보인다). */
    public static String screenRole(String authority) {
        if (authority == null) return null;
        return switch (authority) {
            case "ROLE_ADMIN_7", "ROLE_ADMIN_8", "ROLE_ADMIN_11" -> "OFF";
            case "ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_10" -> "LOC";
            case "ROLE_ADMIN_1", "ROLE_ADMIN_2", "ROLE_ADMIN_3", "ROLE_ADMIN_4" -> "SYS";
            default -> null;
        };
    }

    /** 전체회원/금일가입회원/관리자권한요청(대기)/오프라인접수현황/총기부현황/총답례품현황 - AS-IS getOpmanagerMainInfo 행 순서 그대로. */
    public List<CountRow> mainInfo(Manager manager) {
        String role = queryRole(manager);
        String locgovCd = manager.getLocgovCode();
        // AS-IS: 7·11은 소속지점(은행)만, 8은 은행+지점명까지, 그 외는 지점 조건 없음.
        String auth = manager.getAuthority();
        String bankCode = "";
        String bankNm = "";
        if ("ROLE_ADMIN_7".equals(auth) || "ROLE_ADMIN_11".equals(auth)) {
            bankCode = manager.getBankCode();
        } else if ("ROLE_ADMIN_8".equals(auth)) {
            bankCode = manager.getBankCode();
            bankNm = manager.getPsitnNm();
        }

        Map<String, Long> subscribers = client.subscribers();
        List<CountRow> donation = client.donationMainInfo(role, locgovCd, bankCode, bankNm);
        List<CountRow> point = client.pointMainInfo(role, locgovCd);
        Map<String, Long> present = client.presentCount(role, locgovCd);
        // 답례품 금액 행의 지자체명 라벨은 기부 행과 같은 G_LOCGOV 값이다(donation이 만든다).
        // 지자체코드가 없으면(SYS) 라벨이 null이라 map(...).findFirst()는 NPE - 행을 찾은 뒤 라벨을 꺼낸다.
        String label = donation.stream().filter(r -> "this-year-cntramt".equals(r.id())).findFirst()
                .map(CountRow::label).orElse(null);

        List<CountRow> rows = new ArrayList<>();
        rows.add(new CountRow("today-subscribers", "금일 가입자", subscribers.getOrDefault("todaySubscribers", 0L)));
        rows.add(new CountRow("all-subscribers", "전체 가입자", subscribers.getOrDefault("allSubscribers", 0L)));
        rows.add(new CountRow("request-ready", "권한요청 대기", requestReady(role, locgovCd)));
        rows.addAll(donation);
        rows.add(new CountRow("this-year-presentamt", label, countOf(point, "this-year-presentamt")));
        rows.add(new CountRow("this-year-present", "총답례품현황(건)", present.getOrDefault("thisYear", 0L)));
        rows.add(new CountRow("total-presentamt", label, countOf(point, "total-presentamt")));
        rows.add(new CountRow("total-present", "총답례품현황(건)", present.getOrDefault("total", 0L)));
        return rows;
    }

    /** 게시판 현황 - QnA(QNA_TYPE 2)·1:1 문의(QNA_TYPE 0)의 전체/답변대기. */
    public List<CountRow> boardInfo() {
        String base = "select count(*) from op_qna where qna_type = :type and data_status_code = '0'";
        return List.of(
                new CountRow("total-qnaopen", "QnA 총건수", qna(base, "2")),
                new CountRow("reply-qnaopen", "QnA 답변대기건수", qna(base + " and answer_count = 0", "2")),
                new CountRow("total-qna", "1대1 총건수", qna(base, "0")),
                new CountRow("reply-qna", "1대1 답변대기건수", qna(base + " and answer_count = 0", "0")));
    }

    public List<ChartRow> amountChart(Manager manager) {
        return client.amountChart(queryRole(manager), manager.getLocgovCode());
    }

    public List<ChartRow> countChart(Manager manager) {
        return client.countChart(queryRole(manager), manager.getLocgovCode());
    }

    public List<CountRow> chartInfo(Manager manager) {
        return client.chartInfo(queryRole(manager), manager.getLocgovCode());
    }

    /** 하단 기부·답례품 현황 표(AS-IS main-search) - 기부 10행 뒤에 답례품 신청 2행. */
    public List<CountRow> tableInfo(String date) {
        List<CountRow> rows = new ArrayList<>(client.donationTableInfo(date));
        rows.addAll(client.pointTableInfo(date));
        return rows;
    }

    /** 콜 수 현황 - 그날 OP_CALL 합계(없으면 0). 일일 총 콜수에 민간 플랫폼은 들어가지 않는다(AS-IS 식). */
    public CallState callState(String date) {
        Object[] r = (Object[]) entityManager.createNativeQuery("select sum(call_kookmin), sum(call_lov), "
                        + "sum(call_giver), sum(call_nhbank), sum(call_kookmin + call_lov + call_giver + call_nhbank) "
                        + "from op_call where call_date = :date")
                .setParameter("date", date).getSingleResult();
        CallState s = new CallState();
        s.callKookmin = num(r[0]);
        s.callLov = num(r[1]);
        s.callGiver = num(r[2]);
        s.callNhbank = num(r[3]);
        s.callTotal = num(r[4]);
        return s;
    }

    /** 콜 등록 - 오늘 날짜 행을 지우고 다시 넣는다(AS-IS opmanagerdeleteCall + opmanagerMainCall). */
    @Transactional
    public void insertCall(long kookmin, long lov, long giver, long nhbank, long platform, Long registId) {
        String today = LocalDate.now().format(YMD);
        entityManager.createNativeQuery("delete from op_call where call_date = :date")
                .setParameter("date", today).executeUpdate();
        entityManager.createNativeQuery("insert into op_call (call_kookmin, call_lov, call_giver, call_date, "
                        + "call_nhbank, call_platform, frst_register_id) values (:k, :l, :g, :d, :n, :p, :r)")
                .setParameter("k", kookmin).setParameter("l", lov).setParameter("g", giver).setParameter("d", today)
                .setParameter("n", nhbank).setParameter("p", platform).setParameter("r", registId).executeUpdate();
    }

    /** 기부현황 복사 영역 - REPORT_DASHBOARD의 그날 행 + 전날 대비 증감(AS-IS dashBoardReport). 행이 없으면 전부 0. */
    public CallState dashboardReport(String date) {
        String prev = LocalDate.parse(date, YMD).minusDays(1).format(YMD);
        List<?> list = entityManager.createNativeQuery("""
                        select present, total_user, total_cntr, total_cntramt, on_cntr_cnt, on_cntr_amt,
                               off_cntr_cnt, off_cntr_amt, present_cnt, present_amt,
                               on_cntr_prj_cnt, on_cntr_prj_amt, off_cntr_prj_cnt, off_cntr_prj_amt,
                               call_kookmin, call_lov, call_giver, call_nhbank, call_amt,
                               total_cntramt - (select p.total_cntramt from report_dashboard p
                                                where p.created_date = :prev order by p.report_createdate desc limit 1),
                               total_cntr - (select p.total_cntr from report_dashboard p
                                             where p.created_date = :prev order by p.report_createdate desc limit 1)
                        from report_dashboard where created_date = :date
                        order by report_createdate desc limit 1
                        """)
                .setParameter("date", date).setParameter("prev", prev).getResultList();
        CallState s = new CallState();
        if (list.isEmpty()) {
            return s;
        }
        Object[] r = (Object[]) list.get(0);
        s.present = num(r[0]);
        s.userAmt = num(r[1]);
        s.totalcntr = num(r[2]);
        s.totalcntramt = num(r[3]);
        s.onlinecntr = num(r[4]);
        s.onlinecntramt = num(r[5]);
        s.offlinecntr = num(r[6]);
        s.offlinecntramt = num(r[7]);
        s.totalpresent = num(r[8]);
        s.totalpresentamt = num(r[9]);
        s.onlineprjcntr = num(r[10]);
        s.onlineprjcntramt = num(r[11]);
        s.offlineprjcntr = num(r[12]);
        s.offlineprjcntramt = num(r[13]);
        s.callKookmin = num(r[14]);
        s.callLov = num(r[15]);
        s.callGiver = num(r[16]);
        s.callNhbank = num(r[17]);
        s.callAmt = num(r[18]);
        s.totalCntrAmtCal = num(r[19]);
        s.totalCntrCal = num(r[20]);
        return s;
    }

    /** 답례품 미등록 지자체명(AS-IS opamanagerMainGift) - G_LOCGOV 중 답례품을 가진 지자체코드에 없는 곳. */
    public List<String> giftUnregisteredLocgovNames() {
        Set<String> withItems = new HashSet<>(client.giftItemLocgovCodes());
        return locgovClient.allLocgovs().stream().filter(l -> !withItems.contains(l.locgovCode()))
                .map(LocgovClient.LocgovInfo::locgovNm).toList();
    }

    public long giftItemCount(String date) {
        Long n = client.giftItemCount(date);
        return n == null ? 0L : n;
    }

    public Map<String, Long> notificationAgree() {
        return client.notificationAgree();
    }

    /**
     * 관리자 메인 팝업 노출기간 판정 - AS-IS {@code codeMapper.getCodePopAdmin}. 공통코드 POP_ADMIN의 label이
     * {@code yyyyMMddHH(시작) + yyyyMMddHH(끝)} 20자리이고, 지금 시각(yyyyMMddHH)이 그 사이면 띄운다.
     * id=period/detail=9990 설문조사, id=period2/detail=9991 엑셀암호화 안내.
     */
    public boolean popupOpen(String id, String detail) {
        String now = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        Object n = entityManager.createNativeQuery("""
                        select count(*) from op_common_code
                        where code_type = 'POP_ADMIN' and detail = :detail and id = :id and use_yn = 'Y'
                          and :now between concat(substring(label, 1, 8), substring(label, 9, 2))
                                       and concat(substring(label, 11, 8), substring(label, 19, 2))
                        """)
                .setParameter("detail", detail).setParameter("id", id).setParameter("now", now).getSingleResult();
        return num(n) > 0;
    }

    /**
     * 오프라인 담당자 로그인 직후 안내 - AS-IS {@code ManagerRequestServiceImpl.getMsgLoginOFFManager}.
     * 7·8만 대상이고(11 복지센터는 아니다) 은행명은 OFF_BANK_LIST 라벨이다. 그 외 권한은 빈 문자열.
     */
    public String offManagerLoginMessage(Manager manager, String bankNm) {
        String auth = manager.getAuthority();
        if (!"ROLE_ADMIN_7".equals(auth) && !"ROLE_ADMIN_8".equals(auth)) {
            return "";
        }
        String psitnNm = manager.getPsitnNm();
        String userNm = manager.getUserName();
        if (psitnNm == null || userNm == null) {
            return "오프라인담당자 미입력 정보가 있습니다.\n회원정보를 수정해주시기 바랍니다.";
        }
        return bankNm + " " + psitnNm + "지점 " + userNm + "님으로 로그인 하셨습니다.\n해당 정보가 다를 경우 정보를 수정해주시기 바랍니다.";
    }

    /** 권한요청 대기 - SYS는 전체, 그 외는 자기 지자체(AS-IS getOpmanagerMainInfo request-ready). */
    private long requestReady(String role, String locgovCd) {
        boolean sys = "SYS".equals(role);
        var q = entityManager.createNativeQuery("select count(*) from g_mngr_reqst where confm_sttus_code = '200'"
                + (sys ? "" : " and locgov_code = :locgovCd"));
        if (!sys) q.setParameter("locgovCd", locgovCd == null ? "" : locgovCd);
        return num(q.getSingleResult());
    }

    private long qna(String sql, String type) {
        return num(entityManager.createNativeQuery(sql).setParameter("type", type).getSingleResult());
    }

    private static long countOf(List<CountRow> rows, String id) {
        return rows.stream().filter(r -> id.equals(r.id())).mapToLong(CountRow::count).findFirst().orElse(0L);
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    /** AS-IS {@code CallState} - 필드가 전부 원시 long이라 값이 없으면 0으로 나간다(화면 JS가 그대로 읽는 이름). */
    @lombok.Getter
    public static class CallState {
        private long present;
        private long userAmt;
        private long totalcntr;
        private long totalcntramt;
        private long onlinecntr;
        private long onlinecntramt;
        private long offlinecntr;
        private long offlinecntramt;
        private long totalpresent;
        private long totalpresentamt;
        private long totalCntrCal;
        private long totalCntrAmtCal;
        private long onlineprjcntr;
        private long onlineprjcntramt;
        private long offlineprjcntr;
        private long offlineprjcntramt;
        private long callAmt;
        private long callKookmin;
        private long callLov;
        private long callGiver;
        private long callNhbank;
        private long callTotal;
        private long frstregisterid;
    }
}
