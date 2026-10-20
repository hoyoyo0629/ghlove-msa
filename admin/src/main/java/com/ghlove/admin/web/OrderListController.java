package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.GiftClient;
import com.ghlove.admin.service.ItemManageClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MemberAdminClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OrderAdminClient;
import com.ghlove.admin.service.PrivacyAccessLogService;
import com.ghlove.admin.web.support.AdminRole;
import com.ghlove.admin.web.support.DataMasking;
import com.ghlove.admin.web.support.ExcelDownloadWriter;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 관리자 > 답례품관리 > 주문관리 > 전체주문관리 (AS-IS opmanager/order/list, 메뉴 16401) - 주문 품목 한 줄 단위 조회와 엑셀 다운로드.
 *
 * <p>AS-IS {@code OrderManagerController.list/searchList/allListExcelDownload}를 그대로 옮겼다. 처음 열면(GET) <b>빈 목록</b>이고 검색 폼을 POST할 때만
 * 조회한다("검색 버튼을 클릭하면 조건에 맞는 검색결과를 확인할 수 있습니다"). 주문일 기본값은 오늘, 시간은 00시~23시. 소속 지자체가 있는 관리자는
 * 롤과 상관없이 그 지자체 주문만 본다. 지자체 칸·시도/시군구 검색은 시스템/행안부(SYS)에게만 보인다.
 *
 * <p>★개인정보 마스킹 - AS-IS는 ROLE_ADMIN_5/6(지자체 담당자)·ROLE_ISMS만 원문을 보고 나머지는 이름·휴대폰·상세주소·생년월일을 가린다
 * ({@code ShopUtils.needMasking}). 단 AS-IS 마스킹 코드의 오타까지 그대로다 - "핸드폰번호" 칸은 마스킹 때 <b>수취인</b> 휴대폰을 가려 보여주고, 마스킹이 없을 때만
 * 주문자 휴대폰을 보여준다. 엑셀은 항상 원문(NoneMasking).
 *
 * <p>★주문번호 링크의 상세 화면(AS-IS order/all/order-detail)은 {@link #orderDetail} - 읽기 화면만 옮겼다(클레임 처리·관리자 접수·이력은 설계 결정 대기).
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderListController {

    /** AS-IS OrderParam.getStatus() 기본값 - 상태를 하나도 안 보냈을 때(처음 화면)의 체크 상태. 0(입금대기)은 화면에 체크박스가 없다. */
    private static final List<String> DEFAULT_STATUS =
            List.of("0", "10", "20", "30", "35", "40", "50", "55", "58", "59", "60", "65", "69", "70", "75");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /** AS-IS 다운로드 URL(접근로그 등록 키) - ExcelDownloadWriter/PrivacyAccess 패턴 /{@literal **}/order/list/order-excel-download. */
    private static final String EXCEL_URL = "/order/list/order-excel-download";
    private static final int EXCEL_PAGE_SIZE = 1000;
    private static final Pattern BIRTHDAY = Pattern.compile("^(19|20)\\d{2}([-.]?)(0[1-9]|1[0-2])\\2(0[1-9]|[12]\\d|3[01])$");

    private final OrderAdminClient orderClient;
    private final MemberAdminClient memberClient;
    private final GiftClient giftClient;
    private final ItemManageClient itemManageClient;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final PrivacyAccessLogService privacyAccessLogService;
    private final ExcelDownloadWriter excelDownloadWriter;
    private final FlashRedirect flashRedirect;

    /** gift 서비스 주소 - 상품 이미지(/uploads/..)가 거기서 서빙된다. */
    @Value("${ghlove.gift-service.base-url}")
    private String giftBaseUrl;

    /** 대민 사이트 주소 - 품목 이미지가 여는 상품 상세(AS-IS saleson.url.frontend + 상세 경로). */
    @Value("${ghlove.storefront.base-url:http://localhost:5173}")
    private String storefrontUrl;

    // ---- 목록 ----

    /** AS-IS GET list - 조회하지 않고 검색 폼만(총 0건). */
    @GetMapping("/list")
    public String list(@ModelAttribute("orderParam") OrderParam param, HttpServletRequest request, HttpSession session, Model model) {
        param.applyDefaults(false);
        fill(model, param, List.of(), 0, Pagination.of(0, param.getPage(), param.getItemsPerPage()).withLinkFrom(request), manager(session));
        return "order/list-all";
    }

    /** AS-IS POST list - 실제 검색. */
    @PostMapping("/list")
    public String search(@ModelAttribute("orderParam") OrderParam param, HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        param.applyDefaults(true);
        OrderAdminClient.LineSearch search = toSearch(param, manager);

        int total = orderClient.searchLines(search, 0, 1).total();
        Pagination pagination = Pagination.of(total, param.getPage(), param.getItemsPerPage()).withLinkFrom(request);
        List<OrderAdminClient.LineRow> rows = orderClient.searchLines(search, pagination.getStartRow(), pagination.getItemsPerPage()).rows();

        fill(model, param, toViews(rows, needMasking(manager)), total, pagination, manager);
        return "order/list-all";
    }

    private void fill(Model model, OrderParam param, List<LineView> views, int total, Pagination pagination, Manager manager) {
        model.addAttribute("list", views);
        model.addAttribute("pagination", pagination);
        model.addAttribute("totalCount", total);
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("adminRole", AdminRole.of(manager));
        // 날짜 빠른선택 "전체"는 ROLE_ADMIN_CALL(콜센터 겸임 권한)에게만 - TO-BE 관리자에는 이 권한이 없어 보이는 사람이 없다.
        model.addAttribute("callRole", false);
        model.addAttribute("checkedStatus", param.checkedStatus());
    }

    // ---- 엑셀 ----

    /**
     * AS-IS GET list/order-excel-download - 사유 모달(개인정보 접근로그)을 방금 거치지 않았으면 되돌리고, 조회된 내역이 없어도 되돌린다.
     * 시스템/행안부(ROLE_ADMIN_1~4)는 지자체 칸이 있는 21컬럼, 그 밖(지자체 담당자 등)은 20컬럼이다. 마스킹 없이 원문.
     */
    @GetMapping("/list/order-excel-download")
    public String excel(@ModelAttribute("orderParam") OrderParam param, HttpServletRequest request, HttpSession session,
                        HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        param.applyDefaults(true);
        String redirectUrl = "/order/list";
        String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();

        if (!privacyAccessLogService.existsRecentExcelAccessLog(EXCEL_URL, manager, request)) {
            return flashRedirect.to(redirectUrl + queryString, "엑셀 다운로드 사유 정보가 없습니다.");
        }

        OrderAdminClient.LineSearch search = toSearch(param, manager);
        OrderAdminClient.LinePage first = orderClient.searchLines(search, 0, 1);
        if (first.rows().isEmpty()) {
            return flashRedirect.to(redirectUrl + queryString, "조회된 내역이 없습니다.");
        }

        boolean system = MenuService.UNRESTRICTED_ROLES.contains(manager.getAuthority());
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        Sheet sheet = workbook.createSheet("ALL_ORDER_LIST");
        ExcelDownloadWriter.CellStyles styles = excelDownloadWriter.createStyles(workbook);

        String[] headers = system
                ? new String[]{"No", "지자체", "주문일", "주문번호", "주문자", "아이디", "수취인", "상호명", "판매자 연락처", "답례품정보", "답례품옵션", "수량", "판매가",
                        "주문상태", "배송정보", "필수추가정보", "핸드폰번호", "우편번호", "주소(도로명)", "생년월일", "배송요청사항"}
                : new String[]{"No", "주문일", "주문번호", "주문자", "아이디", "수취인", "상호명", "판매자 연락처", "답례품정보", "답례품옵션", "수량", "판매가",
                        "주문상태", "배송정보", "필수추가정보", "핸드폰번호", "우편번호", "주소(도로명)", "생년월일", "배송요청사항"};
        int[] widths = system
                ? new int[]{2000, 5000, 5000, 5000, 3000, 3000, 3000, 5000, 10000, 10000, 8000, 2000, 3000, 3000, 10000, 5000, 5000, 3000, 10000, 3000, 10000}
                : new int[]{2000, 5000, 5000, 3000, 3000, 3000, 5000, 10000, 10000, 8000, 2000, 3000, 3000, 10000, 5000, 5000, 3000, 10000, 3000, 10000};
        int rowIndex = excelDownloadWriter.writeTitleAndHeaderRow(sheet, styles, "전체주문 목록", headers, widths);

        // AS-IS는 1000건씩 끊어 읽으며 No를 전체 건수에서 거꾸로 센다
        int no = first.total();
        for (int offset = 0; offset < first.total(); offset += EXCEL_PAGE_SIZE) {
            List<LineView> views = toViews(orderClient.searchLines(search, offset, EXCEL_PAGE_SIZE).rows(), false);
            if (views.isEmpty()) {
                break;
            }
            for (LineView v : views) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeight((short) 400);
                int col = 0;
                excelDownloadWriter.dataCell(row, col++, styles, String.format("%,d", no--));
                if (system) {
                    excelDownloadWriter.dataCell(row, col++, styles, v.locgovNm());
                }
                excelDownloadWriter.dataCell(row, col++, styles, v.createdDateText());
                excelDownloadWriter.dataCell(row, col++, styles, v.orderCode());
                excelDownloadWriter.dataCell(row, col++, styles, v.userName());
                excelDownloadWriter.dataCell(row, col++, styles, v.loginId());
                excelDownloadWriter.dataCell(row, col++, styles, v.receiveName());
                excelDownloadWriter.dataCell(row, col++, styles, v.companyName());
                excelDownloadWriter.dataCell(row, col++, styles, v.telephoneNumber());
                excelDownloadWriter.dataCell(row, col++, styles, v.itemName());
                excelDownloadWriter.dataCell(row, col++, styles, v.optionText());
                excelDownloadWriter.dataCell(row, col++, styles, String.format("%,d", v.quantity()));
                excelDownloadWriter.dataCell(row, col++, styles, String.format("%,d", v.saleAmount()));
                excelDownloadWriter.dataCell(row, col++, styles, v.statusLabel());
                excelDownloadWriter.dataCell(row, col++, styles, v.excelDeliveryText());
                excelDownloadWriter.dataCell(row, col++, styles, v.textOption());
                excelDownloadWriter.dataCell(row, col++, styles, v.mobile());
                excelDownloadWriter.dataCell(row, col++, styles, v.zipcode());
                excelDownloadWriter.dataCell(row, col++, styles, v.address());
                excelDownloadWriter.dataCell(row, col++, styles, v.birthday());
                excelDownloadWriter.dataCell(row, col, styles, v.requestNote() != null && !v.requestNote().isEmpty() ? v.requestNote() : "배송 요청 사항 없음");
            }
        }

        String fileName = "전체주문목록_" + LocalDateTime.now().format(FILE_DATETIME) + ".xlsx";
        excelDownloadWriter.write(response, workbook, fileName, manager.getLoginId());
        return null;
    }

    // ---- 주문 상세 ----

    /**
     * AS-IS GET order/all/order-detail/{출고번호}/{주문번호} - 주문 상세정보(상단 정보 + 탭: 일반·취소·반품·교환·이력).
     * 담당 지자체가 있는 관리자는 그 지자체 주문만 열 수 있고, 없으면 "주문 내역이 없습니다."로 목록에 돌아간다.
     *
     * <p>★이번 이식은 <b>읽기 화면</b>이다. 취소/반품/교환 신청(관리자 접수) 폼, 클레임 처리 폼(승인·거절·회수), 환불/결제변경/송장변경, 이력 기록은
     * TO-BE 클레임 모델(od_claim)과 AS-IS 신청 모델이 달라 설계 결정 뒤에 옮긴다(docs/as-is-parity.md 22절 admin-order-list-all-port 단계 C·D).
     */
    @GetMapping("/all/order-detail/{orderSequence}/{orderCode}")
    public String orderDetail(@PathVariable int orderSequence, @PathVariable String orderCode, HttpSession session, Model model) {
        Manager manager = manager(session);
        OrderAdminClient.DetailView detail = orderClient.detailView(orderCode, orderSequence, manager.getLocgovCode());
        if (detail == null) {
            return flashRedirect.to("/order/list", "주문 내역이 없습니다.");
        }
        model.addAttribute("d", toDetailPage(detail, needMasking(manager)));
        return "order/detail-all";
    }

    private DetailPage toDetailPage(OrderAdminClient.DetailView d, boolean masking) {
        boolean member = d.userId() != null && d.userId() > 0;
        MemberAdminClient.OrderInfo user = member ? memberClient.orderInfos(List.of(d.userId())).get(d.userId()) : null;

        // AS-IS OrderEncryptor.masking - 이름 2칸·이메일·휴대폰. TO-BE에는 주문 시점의 발송인/구매자 연락처가 없어 회원 정보로 채운다.
        String userName = nz(user == null ? null : user.userName());
        String buyerName = userName;
        String mobile = nz(user == null ? null : user.phoneNumber());
        String email = nz(user == null ? null : user.email());
        String receiveName = nz(d.receiverName());
        String receiveMobile = nz(d.receiverPhone());
        String addressDetail = nz(d.addressDetail());
        if (masking) {
            userName = DataMasking.name(userName);
            buyerName = DataMasking.name(buyerName);
            mobile = DataMasking.phoneNumber(mobile);
            email = DataMasking.email(email);
            receiveName = DataMasking.name(receiveName);
            receiveMobile = DataMasking.phoneNumber(receiveMobile);
            addressDetail = DataMasking.addressDetail(addressDetail);
        }

        List<Long> itemIds = d.lines().stream().map(OrderAdminClient.DetailLine::itemId).filter(id -> id != null && id > 0).distinct().toList();
        Map<Long, String> thumbnails = itemManageClient.thumbnails(itemIds);
        Map<Long, GiftClient.SellerDetail> sellers = giftClient.sellers().stream()
                .collect(Collectors.toMap(GiftClient.SellerDetail::sellerId, s -> s, (a, b) -> a));

        Map<Long, DetailItem> byLine = new LinkedHashMap<>();
        long itemAmountTotal = 0;
        for (OrderAdminClient.DetailLine l : d.lines()) {
            GiftClient.SellerDetail seller = l.sellerId() == null ? null : sellers.get(l.sellerId());
            long itemAmount = (long) (l.unitPrice() + (l.optionPrice() == null ? 0 : l.optionPrice())) * l.quantity();
            itemAmountTotal += itemAmount;
            String thumb = l.itemId() == null ? null : thumbnails.get(l.itemId());
            boolean shipped = has(l.shippedDate());
            String tracking = "";
            if (has(l.carrierUrl()) && has(l.invoiceNo())) {
                // AS-IS OrderItem.getDeliveryShippingUrl + JSP replaceAll('-', '')
                tracking = l.carrierUrl().replace("%INVOICE%", l.invoiceNo()).replace("-", "");
            }
            byLine.put(l.lineId(), new DetailItem(
                    l.itemId() == null ? "" : "G2" + String.format("%09d", l.itemId()), nz(l.itemName()), optionText(l.optionName(), l.optionPrice()),
                    textOptions(l.textOption()),
                    has(thumb) ? giftBaseUrl + "/uploads/" + thumb : "/content/images/common/no-image-gray.gif",
                    l.itemId() == null ? "#" : storefrontUrl + "/gifts/" + l.itemId(),
                    seller == null ? "" : nz(seller.sellerName()), seller == null ? "" : nz(seller.telephoneNumber()),
                    String.format("%,d", l.quantity()), String.format("%,d", itemAmount), String.format("%,d", l.pointAmount()),
                    statusLabel(l.statusCode()), shipped, nz(l.carrierName()), nz(l.invoiceNo()), shipped ? dateOnly(l.shippedDate()) : "", tracking));
        }

        List<ClaimView> cancelActive = new ArrayList<>();
        List<ClaimView> cancelHistory = new ArrayList<>();
        List<ClaimView> returnActive = new ArrayList<>();
        List<ClaimView> returnHistory = new ArrayList<>();
        List<ClaimView> exchangeActive = new ArrayList<>();
        List<ClaimView> exchangeHistory = new ArrayList<>();
        for (OrderAdminClient.DetailClaim c : d.claims()) {
            DetailItem item = byLine.get(c.lineId());
            if (item == null) {
                continue;
            }
            String code = claimStatusCode(c.claimType(), c.status());
            ClaimView view = new ClaimView(item, code, claimStatusLabel(c.claimType(), code), nz(c.reason()), dateText(c.createdDate()),
                    dateOnly(c.createdDate()), timeOnly(c.createdDate()));
            boolean active = switch (c.claimType() == null ? "" : c.claimType()) {
                case "CANCEL", "RETURN" -> "01".equals(code);
                case "EXCHANGE" -> "01".equals(code) || "10".equals(code);
                default -> false;
            };
            switch (c.claimType() == null ? "" : c.claimType()) {
                case "CANCEL" -> (active ? cancelActive : cancelHistory).add(view);
                case "RETURN" -> (active ? returnActive : returnHistory).add(view);
                case "EXCHANGE" -> (active ? exchangeActive : exchangeHistory).add(view);
                default -> {
                }
            }
        }

        // 결제정보 - AS-IS는 한 줄(판매가·결제포인트 모두 주문 품목 합계, 결제상태, 결제일). TO-BE는 주문 생성 시각이 곧 포인트 결제 시각이다.
        String totalText = String.format("%,d", itemAmountTotal);
        return new DetailPage(d.orderId(), d.orderSequence(), dateText(d.createdDate()),
                member ? userName + " [" + nz(user == null ? null : user.loginId()) + "]" : "비회원", buyerName, mobile, email,
                totalText, d.paid() ? "결제완료" : "결제취소", dateText(d.createdDate()),
                receiveName, receiveMobile, nz(d.zipcode()), nz(d.address()), addressDetail, nz(d.requestNote()),
                new ArrayList<>(byLine.values()), cancelActive, cancelHistory, returnActive, returnHistory, exchangeActive, exchangeHistory);
    }

    /** TO-BE 클레임 상태 → AS-IS 신청 상태코드(OrderCancelApply/OrderReturnApply/OrderExchangeApply.claimStatus). */
    private static String claimStatusCode(String type, String status) {
        return switch (status == null ? "" : status) {
            case "REQUESTED" -> "01";
            case "APPROVED" -> "EXCHANGE".equals(type) ? "10" : "03";
            case "COMPLETED" -> "EXCHANGE".equals(type) ? "03" : "04";
            case "REJECTED" -> "99";
            default -> "";
        };
    }

    /** AS-IS getClaimStatusLabel - 유형마다 03·04 문구가 다르다. */
    private static String claimStatusLabel(String type, String code) {
        return switch (code) {
            case "01" -> "신청";
            case "02" -> "보류";
            case "03" -> "RETURN".equals(type) ? "환불승인 대기" : "EXCHANGE".equals(type) ? "교환상품 발송" : "환불대기";
            case "04" -> "RETURN".equals(type) ? "환불 완료" : "완료";
            case "10" -> "회수중";
            case "11" -> "회수완료";
            case "99" -> "거절";
            default -> "-";
        };
    }

    /** yyyyMMddHHmmss → yyyy-MM-dd. */
    private static String dateOnly(String raw) {
        return raw == null || raw.length() < 8 ? "" : raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
    }

    /** yyyyMMddHHmmss → HH:mm:ss (AS-IS op:timeFormat). */
    private static String timeOnly(String raw) {
        return raw == null || raw.length() < 14 ? "" : raw.substring(8, 10) + ":" + raw.substring(10, 12) + ":" + raw.substring(12, 14);
    }

    // ---- 검색 조건 조립 ----

    private OrderAdminClient.LineSearch toSearch(OrderParam p, Manager manager) {
        // AS-IS getAllOrderListByParamForManager - 소속 지자체가 있는 관리자는 롤과 상관없이 시도·시군구를 그 지자체로 강제
        String shWdr = p.getShWdr();
        String shLocgovCode = p.getShLocgovCode();
        if (has(manager.getLocgovCode())) {
            shWdr = upperLocgovCode(manager.getLocgovCode());
            shLocgovCode = manager.getLocgovCode();
        }
        List<String> locgovCodes = null;
        if (has(shLocgovCode)) {
            locgovCodes = List.of(shLocgovCode);
        } else if (has(shWdr)) {
            final String upper = shWdr;
            locgovCodes = locgovClient.allLocgovs().stream().filter(l -> upper.equals(l.upperLocgovCode())).map(LocgovClient.LocgovInfo::locgovCode).toList();
        }

        List<Long> sellerIds = null;
        List<Long> userIds = null;
        if (has(p.getQuery()) && has(p.getWhere())) {
            if ("COMPANY_NAME".equals(p.getWhere())) {
                String q = p.getQuery();
                sellerIds = giftClient.sellers().stream().filter(s -> s.companyName() != null && s.companyName().contains(q)).map(GiftClient.SellerDetail::sellerId).toList();
            } else if ("USER_NAME".equals(p.getWhere())) {
                userIds = memberClient.userIdsByName(p.getQuery());
            }
        }

        // 상태를 하나도 안 골랐으면 상태 조건을 걸지 않는다(AS-IS: 빈 배열이면 status 조건 생략)
        List<String> statuses = p.statusList().isEmpty() ? null : p.statusList();
        return new OrderAdminClient.LineSearch(locgovCodes, p.getWhere(), p.getQuery(), sellerIds, userIds,
                p.getSearchStartDate(), p.getSearchStartDateTime(), p.getSearchEndDate(), p.getSearchEndDateTime(), statuses);
    }

    private String upperLocgovCode(String locgovCode) {
        return locgovClient.allLocgovs().stream().filter(l -> locgovCode.equals(l.locgovCode())).map(LocgovClient.LocgovInfo::upperLocgovCode).findFirst().orElse(null);
    }

    // ---- 표시용 한 줄 조립 ----

    private List<LineView> toViews(List<OrderAdminClient.LineRow> rows, boolean masking) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = rows.stream().map(OrderAdminClient.LineRow::userId).filter(id -> id != null && id > 0).distinct().toList();
        Map<Long, MemberAdminClient.OrderInfo> users = memberClient.orderInfos(userIds);
        Map<Long, GiftClient.SellerDetail> sellers = giftClient.sellers().stream()
                .collect(Collectors.toMap(GiftClient.SellerDetail::sellerId, s -> s, (a, b) -> a));
        Map<String, String> locgovNames = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            locgovNames.put(l.locgovCode(), (l.upperLocgovNm() == null ? "" : l.upperLocgovNm() + " ") + l.locgovNm());
        }

        List<LineView> views = new ArrayList<>();
        for (OrderAdminClient.LineRow r : rows) {
            MemberAdminClient.OrderInfo user = r.userId() == null ? null : users.get(r.userId());
            GiftClient.SellerDetail seller = r.sellerId() == null ? null : sellers.get(r.sellerId());

            String userName = user == null ? "" : nz(user.userName());
            String loginId = user == null ? "" : nz(user.loginId());
            String receiveName = nz(r.receiverName());
            String addressDetail = nz(r.addressDetail());
            String birthday = normalizeBirthday(user == null ? null : user.birthday());
            // AS-IS 마스킹(OrderListEncryptor.masking) - 이름 3종·상세주소·생년월일, 그리고 "핸드폰번호"는 수취인 휴대폰을 가려서(오타 그대로)
            String mobile = nz(user != null && has(user.phoneNumber()) ? user.phoneNumber() : r.receiverPhone());
            if (masking) {
                userName = DataMasking.name(userName);
                receiveName = DataMasking.name(receiveName);
                addressDetail = DataMasking.addressDetail(addressDetail);
                birthday = DataMasking.birthday(birthday);
                mobile = DataMasking.phoneNumber(nz(r.receiverPhone()));
            }

            int unit = (r.unitPrice() == null ? 0 : r.unitPrice()) + (r.optionPrice() == null ? 0 : r.optionPrice());
            int quantity = r.quantity() == null ? 0 : r.quantity();
            views.add(new LineView(
                    r.locgovCode() == null ? "" : locgovNames.getOrDefault(r.locgovCode(), ""),
                    r.createdDate(), dateText(r.createdDate()), r.orderId(), r.orderSequence(),
                    userName, loginId, receiveName,
                    seller == null ? "" : nz(seller.companyName()), seller == null ? "" : nz(seller.telephoneNumber()),
                    nz(r.itemName()), r.itemId() == null ? "" : "G2" + String.format("%09d", r.itemId()), optionText(r),
                    textOptions(r.textOption()), r.textOption() == null ? "" : r.textOption(),
                    quantity, (long) unit * quantity, statusLabel(r.statusCode()), deliveryText(r), excelDeliveryText(r),
                    mobile, nz(r.zipcode()), (nz(r.address()) + " " + addressDetail), birthday, r.requestNote()));
        }
        return views;
    }

    /** AS-IS OrderItem.getOrderStatusLabel. */
    private static String statusLabel(String code) {
        return switch (code == null ? "" : code) {
            case "0" -> "입금대기";
            case "10" -> "결제완료";
            case "20" -> "배송준비중";
            case "30" -> "배송중";
            case "35" -> "배송완료";
            case "40" -> "구매확정";
            case "50" -> "교환처리중";
            case "55" -> "교환배송중";
            case "58" -> "교환배송완료";
            case "59" -> "교환거절";
            case "60" -> "반품처리중";
            case "65" -> "반품완료";
            case "69" -> "반품거절";
            case "70" -> "취소처리중";
            case "75" -> "취소완료";
            default -> "-";
        };
    }

    /**
     * AS-IS "배송정보" 칸 - 발송일이 있을 때만 보인다. 송장번호가 없으면 "송장 번호 : 없음 (택배사 정보 없음)"인데, AS-IS는 이 칸의 택배사 표기를
     * {@code ${택배사명 ? 택배사명 : '택배사 정보 없음'}}(문자열을 불린으로 쓰는 식)으로 써서 택배사명이 있어도 <b>항상</b> "택배사 정보 없음"이 나온다 - 그대로 둔다.
     */
    private static String deliveryText(OrderAdminClient.LineRow r) {
        if (r.shippedDate() == null) {
            return "";
        }
        if (!has(r.invoiceNo())) {
            return "송장 번호 : 없음 (택배사 정보 없음)";
        }
        return "송장 번호 : " + r.invoiceNo() + "(" + nz(r.carrierName()) + ")";
    }

    /** AS-IS 엑셀 배송정보 - 송장번호가 있으면 "송장번호 : X(택배사)", 없으면 "송장 번호 없음"(발송 여부와 상관없이). */
    private static String excelDeliveryText(OrderAdminClient.LineRow r) {
        return has(r.invoiceNo()) ? "송장번호 : " + r.invoiceNo() + "(" + nz(r.carrierName()) + ")" : "송장 번호 없음";
    }

    /** 옵션 표기 - 옵션명에 추가금액이 있으면 AS-IS viewOptionTextNoUl처럼 " (+N원)"을 붙인다. */
    private static String optionText(OrderAdminClient.LineRow r) {
        return optionText(r.optionName(), r.optionPrice());
    }

    private static String optionText(String optionName, Integer optionPrice) {
        if (!has(optionName)) {
            return "";
        }
        int price = optionPrice == null ? 0 : optionPrice;
        return price == 0 ? optionName : optionName + " (" + (price > 0 ? "+" : "-") + String.format("%,d", Math.abs(price)) + "원)";
    }

    /** AS-IS getViewItemTextOption - '||'로 이어 둔 필수 추가정보를 한 줄씩. */
    private static List<String> textOptions(String textOption) {
        if (!has(textOption)) {
            return List.of();
        }
        return Arrays.asList(textOption.split("\\|\\|", -1));
    }

    /** AS-IS OrderList.getBirthday - (19|20)YYYY[-.]MM[-.]DD 형식이면 yyyy-MM-dd로. */
    private static String normalizeBirthday(String raw) {
        String b = raw == null ? "" : raw;
        if (BIRTHDAY.matcher(b).matches()) {
            String d = b.replaceAll("[-.]", "");
            return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
        }
        return b;
    }

    /** AS-IS op:datetime - yyyyMMddHHmmss → yyyy-MM-dd HH:mm:ss. */
    private static String dateText(String raw) {
        if (raw == null || raw.length() < 14) {
            return raw == null ? "" : raw;
        }
        return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8) + " " + raw.substring(8, 10) + ":" + raw.substring(10, 12) + ":" + raw.substring(12, 14);
    }

    // ---- 공통 ----

    /** AS-IS ShopUtils.needMasking - ROLE_ADMIN_5/6(지자체 담당자)·ROLE_ISMS가 아니면 가린다. */
    private static boolean needMasking(Manager manager) {
        return !MenuService.isLocgovScoped(manager);
    }

    private List<Map<String, String>> wdrOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("id", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ---- 화면용 타입 ----

    /** 목록 한 줄(AS-IS OrderList). 마스킹이 필요한 칸은 이미 가려진 값이다. */
    public record LineView(String locgovNm, String createdDateRaw, String createdDateText, String orderCode, int orderSequence,
                           String userName, String loginId, String receiveName, String companyName, String telephoneNumber,
                           String itemName, String itemUserCode, String optionText, List<String> textOptions, String textOption,
                           int quantity, long saleAmount, String statusLabel, String deliveryText, String excelDeliveryText,
                           String mobile, String zipcode, String address, String birthday, String requestNote) {
        /** AS-IS 목록의 "주문자" 칸 - 회원이 아니면(아이디가 비면) 비회원. */
        public boolean guest() {
            return loginId == null || loginId.isEmpty();
        }
    }

    /** 주문 상세의 품목 한 줄(AS-IS OrderItem) - 숫자는 천단위 쉼표를 넣은 글자, 마스킹이 필요한 값은 이미 가려져 있다. */
    public record DetailItem(String itemUserCode, String itemName, String optionText, List<String> textOptions, String imageSrc, String previewUrl,
                             String sellerName, String telephoneNumber, String quantity, String itemAmount, String saleAmount, String statusLabel,
                             boolean shipped, String carrierName, String invoiceNo, String shippedDate, String trackingUrl) {
        /** AS-IS: 송장번호가 없으면 택배사명(없으면 "택배사 정보 없음") + [송장정보 없음]. */
        public String carrierLabel() {
            return carrierName == null || carrierName.isEmpty() ? "택배사 정보 없음" : carrierName;
        }
    }

    /** 취소·반품·교환 신청 한 건(AS-IS OrderCancelApply 등) - 대상 품목은 수량·금액이 품목 그대로다(TO-BE 클레임은 품목 단위 전체). */
    public record ClaimView(DetailItem item, String statusCode, String statusLabel, String reason, String createdDateText, String createdDate, String createdTime) {
    }

    /** 주문 상세 화면 전체(AS-IS order-detail.jsp가 쓰는 order + 클레임 목록). */
    public record DetailPage(String orderCode, int orderSequence, String createdDateText, String memberText, String buyerName, String mobile, String email,
                             String totalAmount, String paymentStatus, String payDateText,
                             String receiveName, String receiveMobile, String zipcode, String address, String addressDetail, String memo,
                             List<DetailItem> items,
                             List<ClaimView> cancelActive, List<ClaimView> cancelHistory, List<ClaimView> returnActive, List<ClaimView> returnHistory,
                             List<ClaimView> exchangeActive, List<ClaimView> exchangeHistory) {
    }

    /** AS-IS OrderParam 중 전체주문관리 검색 폼이 보내는 것. */
    @Getter
    @Setter
    public static class OrderParam {
        private String shWdr = "";
        private String shLocgovCode = "";
        private String where = "";
        private String query = "";
        private String searchDateType = "OI.CREATED_DATE";
        private String searchStartDate;
        private String searchStartDateTime = "00";
        private String searchEndDate;
        private String searchEndDateTime = "23";
        private String[] status;
        private String opOrderDefault;
        private String opOrderClaim;
        private int itemsPerPage = 10;
        private int page = 1;
        /** 폼이 제출된 요청인지(체크박스 해제 상태를 구분) - POST/엑셀은 true, 처음 열기는 false. */
        private boolean submitted;

        /** 검색일자는 비어 있으면 오늘. 처음 열기(GET)는 체크박스를 전부 체크, 제출된 요청은 보낸 값 그대로. */
        void applyDefaults(boolean submitted) {
            this.submitted = submitted;
            String today = LocalDate.now().format(DAY);
            if (searchStartDate == null || searchStartDate.isBlank() || searchEndDate == null || searchEndDate.isBlank()) {
                searchStartDate = today;
                searchEndDate = today;
            }
            if (!submitted) {
                opOrderDefault = "Y";
                opOrderClaim = "Y";
            }
        }

        /** 화면 체크박스에 표시할 상태 - 처음 열기는 기본 전체, 제출된 요청은 보낸 것만. */
        Set<String> checkedStatus() {
            return new LinkedHashSet<>(submitted ? statusList() : DEFAULT_STATUS);
        }

        List<String> statusList() {
            return status == null ? List.of() : Arrays.stream(status).filter(s -> s != null && !s.isBlank()).toList();
        }
    }
}
