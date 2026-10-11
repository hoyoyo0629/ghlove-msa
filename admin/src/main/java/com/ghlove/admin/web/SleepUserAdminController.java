package com.ghlove.admin.web;

import com.ghlove.admin.service.DonationClient;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.PointClient;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.SleepUserSearchParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 휴면회원관리 (메뉴 4107) - AS-IS saleson.shop.user.SleepUserManagerController
 * ({@code /opmanager/user/sleep-user}) 재현.
 *
 * <p><b>AS-IS 핵심 동작: GET(진입)에서는 목록을 조회하지 않는다</b> - 회원탈퇴관리(4105)와 같이
 * 빈 목록/0건을 내려주고 검색(POST)해야 조회된다. 최종 방문일 기본값은 오늘이다.
 * 예전 TO-BE는 GET에서 바로 조회하고 주소·기부누적액·포인트잔액 3컬럼이 없었다.
 *
 * <p>8컬럼을 서비스 경계에 맞춰 나눠 채운다:
 * <ul>
 *   <li>최종 방문일·아이디·이름·<b>주소</b> → member({@code searchSleep})</li>
 *   <li><b>기부누적액</b> → donation {@code /api/admin/donation-totals}(완료 기부 합산)</li>
 *   <li><b>포인트잔액</b> → point {@code /api/admin/point-balances}</li>
 * </ul>
 * 두 API 모두 목록의 userId를 한 번에 보내는 일괄 조회다(행마다 호출하면 N+1).
 *
 * <p>휴면 해제는 AS-IS와 같이 ajax({@code POST /wakeup})이고 응답은 {@code {isSuccess, data:"SUCC"}}다.
 */
@Controller
@RequiredArgsConstructor
public class SleepUserAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MemberAdminClient memberAdminClient;
    private final DonationClient donationClient;
    private final PointClient pointClient;

    /** 화면 한 줄 - AS-IS SleepUser 도메인에 대응. */
    public record Row(Long userId, String loginDate, String loginId, String userName,
                      String address, String addressDetail,
                      BigDecimal totalCntrAmt, Long totalCntrBlcePoint) {
    }

    /** AS-IS GET /list - 조회하지 않고 빈 목록을 보여준다. */
    @GetMapping("/admin/sleep-users")
    public String list(@ModelAttribute("searchParam") SleepUserSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        applyDefaults(searchParam);
        model.addAttribute("list", List.of());
        model.addAttribute("count", 0);
        model.addAttribute("pagination",
                Pagination.of(0, searchParam.getPage(), searchParam.getItemsPerPage()).withLinkFrom(request));
        return "sleep-user-admin/list";
    }

    /** AS-IS POST /list - 실제 검색. */
    @PostMapping("/admin/sleep-users")
    public String search(@ModelAttribute("searchParam") SleepUserSearchParam searchParam,
                         HttpServletRequest request, Model model) {
        applyDefaults(searchParam);

        // member API는 page가 0부터다. 이름/아이디는 AS-IS 안내대로 정확히 일치해야 하므로
        // member의 부분일치 결과를 admin에서 완전일치로 한 번 더 좁힌다.
        List<MemberAdminClient.SleepRow> found = memberAdminClient.searchSleep(
                        searchParam.getSrchStartLoginDateForApi(), searchParam.getSrchEndLoginDateForApi(),
                        blankToNull(searchParam.getSrchKey()), blankToNull(searchParam.getSrchValue()), 0, 1000)
                .content().stream()
                .filter(r -> matchesExactly(searchParam, r))
                .toList();

        Pagination pagination = Pagination.of(found.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<MemberAdminClient.SleepRow> pageRows = found.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList();

        // 현재 페이지 회원들만 일괄로 기부누적액·포인트잔액을 받아 채운다
        List<Long> userIds = pageRows.stream().map(MemberAdminClient.SleepRow::userId).toList();
        Map<Long, BigDecimal> donationTotals = donationClient.donationTotalsByUserIds(userIds);
        Map<Long, Long> pointBalances = pointClient.balancesByUserIds(userIds);

        model.addAttribute("list", pageRows.stream()
                .map(r -> new Row(r.userId(), r.loginDate(), r.loginId(), r.userName(),
                        r.address(), r.addressDetail(),
                        donationTotals.get(r.userId()), pointBalances.get(r.userId())))
                .toList());
        model.addAttribute("count", found.size());
        model.addAttribute("pagination", pagination);
        return "sleep-user-admin/list";
    }

    /** AS-IS POST /wakeup - ajax. 응답은 {isSuccess, data}이고 화면은 data=="SUCC"만 성공으로 본다. */
    @PostMapping("/admin/sleep-users/wakeup")
    @ResponseBody
    public Map<String, Object> wakeup(@RequestParam(name = "userIdList", required = false) List<Long> userIdList) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (userIdList == null || userIdList.isEmpty()) {
                result.put("isSuccess", true);
                result.put("data", "FAIL");
                return result;
            }
            memberAdminClient.wakeup(userIdList);
            result.put("isSuccess", true);
            result.put("data", "SUCC");
        } catch (RuntimeException e) {
            result.put("isSuccess", true);
            result.put("data", "FAIL");
        }
        return result;
    }

    /** AS-IS 화면 안내 "※ "이름, 아이디"는 정확하게 입력해야 합니다." - 완전일치. */
    private static boolean matchesExactly(SleepUserSearchParam p, MemberAdminClient.SleepRow r) {
        String value = blankToNull(p.getSrchValue());
        if (value == null) {
            return true;
        }
        return "USER_NAME".equals(p.getSrchKey())
                ? value.equals(r.userName())
                : value.equals(r.loginId());
    }

    /** AS-IS 컨트롤러 - 최종 방문일이 비면 오늘로 채운다. */
    private static void applyDefaults(SleepUserSearchParam p) {
        String today = LocalDate.now().format(DAY);
        if (p.getSrchStartLoginDate() == null || p.getSrchStartLoginDate().isBlank()) {
            p.setSrchStartLoginDate(today);
        }
        if (p.getSrchEndLoginDate() == null || p.getSrchEndLoginDate().isBlank()) {
            p.setSrchEndLoginDate(today);
        }
        if (p.getItemsPerPage() <= 0) {
            p.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
