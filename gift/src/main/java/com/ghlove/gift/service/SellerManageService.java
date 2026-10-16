package com.ghlove.gift.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 답례품제공자 관리(AS-IS opmanager/seller, 메뉴 16151) - 판매자(OP_SELLER) + 판매자 계정(OP_SELLER_USER) 목록/등록/수정,
 * 아이디 생성, 비밀번호 초기화, 첨부파일 삭제. AS-IS SellerServiceImpl + SellerUserServiceImpl + seller-mapper.xml/seller-user-mapper.xml.
 *
 * <p>TO-BE 결정(2026-10-15 사용자): 계정은 AS-IS처럼 OP_SELLER_USER까지 같이 저장한다. AS-IS와 다른 점 - 사용자번호는 gift 자체 채번(MAX+1),
 * 역할(ROLE_SELLER*)은 gift에 역할표가 없어 만들지 않고, 비밀번호는 salt+hash.5 대신 BCrypt, 개인정보는 암호화 없이 저장(TO-BE 전체 방침).
 * 옮기지 않은 것: 담당MD 상품 일괄수정·판매자 배송비 상품 일괄수정(화면 영역이 AS-IS에서도 hidden), 국민비서 등록 문자(CI가 있는 폼 값이 안 와서 AS-IS도 안 나감).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SellerManageService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String[] FILE_TYPES = {"file1", "file2", "file3"};

    private final EntityManager em;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${gift.upload.dir}")
    private String uploadDir;

    // ---- 목록 ----

    public record Search(String statusCode, String where, String query, String startDate, String endDate,
                         String itemApprovalType, String locgov) {
    }

    public record ListRow(Long sellerId, String loginId, String companyName, String representativeName, String phoneNumber,
                          String statusCode, String itemApprovalType, String createdDate, String locgovCode,
                          String mberDn, String mberFinDn, String mberCi) {
    }

    /** AS-IS getSellerCount - OP_SELLER만 센다(계정 join 없음). */
    @Transactional(readOnly = true)
    public int count(Search s) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = where(s, params);
        Query q = em.createNativeQuery("select count(distinct s.seller_id) from public.op_seller s" + where);
        params.forEach(q::setParameter);
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS getSellerListByParam - OP_SELLER inner join OP_SELLER_USER, 등록일 내림차순. 인증서/본인인증 칸은 등록 여부 Y/N. */
    @Transactional(readOnly = true)
    public List<ListRow> list(Search s, int offset, int limit) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = where(s, params);
        Query q = em.createNativeQuery("select s.seller_id, s.login_id, s.company_name, s.representative_name, s.phone_number, "
                + "s.status_code, s.item_approval_type, s.created_date, s.locgov_code, "
                + "case when su.mber_dn is null or su.mber_dn = '' then 'N' else 'Y' end, "
                + "case when su.mber_fin_dn is null or su.mber_fin_dn = '' then 'N' else 'Y' end, "
                + "case when su.mber_ci is null or su.mber_ci = '' then 'N' else 'Y' end "
                + "from public.op_seller s inner join public.op_seller_user su on s.login_id = su.login_id"
                + where + " order by s.created_date desc nulls last, s.seller_id desc");
        params.forEach(q::setParameter);
        q.setFirstResult(Math.max(offset, 0)).setMaxResults(limit > 0 ? limit : 10);
        List<ListRow> rows = new ArrayList<>();
        for (Object o : q.getResultList()) {
            Object[] r = (Object[]) o;
            rows.add(new ListRow(((Number) r[0]).longValue(), str(r[1]), str(r[2]), str(r[3]), str(r[4]), str(r[5]), str(r[6]),
                    str(r[7]), str(r[8]), str(r[9]), str(r[10]), str(r[11])));
        }
        return rows;
    }

    /** AS-IS sqlSellerWhere - 조건이 없는 항목은 건너뛴다. 검색 구분은 상호명/아이디/대표자명/휴대폰(+이름/담당자명, 화면엔 없음). */
    private static String where(Search s, Map<String, Object> params) {
        List<String> conds = new ArrayList<>();
        if (has(s.statusCode())) {
            conds.add("s.status_code = :statusCode");
            params.put("statusCode", s.statusCode());
        }
        if (has(s.query())) {
            String column = switch (s.where() == null ? "" : s.where()) {
                case "SELLER_NAME" -> "s.seller_name";
                case "USER_NAME" -> "s.user_name";
                case "COMPANY_NAME" -> "s.company_name";
                case "LOGIN_ID" -> "s.login_id";
                case "REPRESENTATIVE_NAME" -> "s.representative_name";
                case "PHONE_NUMBER" -> "s.phone_number";
                default -> null;
            };
            if (column != null) {
                conds.add(column + " like concat('%', cast(:query as varchar), '%')");
                params.put("query", s.query());
            }
        }
        if (has(s.startDate())) {
            conds.add("s.created_date >= concat(cast(:startDate as varchar), '000000')");
            params.put("startDate", s.startDate());
        }
        if (has(s.endDate())) {
            conds.add("s.created_date <= concat(cast(:endDate as varchar), '235959')");
            params.put("endDate", s.endDate());
        }
        if (has(s.itemApprovalType())) {
            conds.add("s.item_approval_type = :itemApprovalType");
            params.put("itemApprovalType", s.itemApprovalType());
        }
        if (has(s.locgov())) {
            conds.add("s.locgov_code = :locgov");
            params.put("locgov", s.locgov());
        }
        return conds.isEmpty() ? "" : " where " + String.join(" and ", conds);
    }

    // ---- 상세 ----

    /**
     * AS-IS getSellerById - OP_SELLER 전 칸 + 계정(OP_SELLER_USER)의 사용자번호·SMS수신동의·본인인증 등록여부(Y/N) + 이메일 앞/뒤.
     * 키는 칼럼명을 camelCase로 바꾼 것이고 비밀번호 해시는 내려주지 않는다. 없으면 null.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> detail(long sellerId) {
        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery("select s.*, su.user_id as user_id, su.receive_sms as receive_sms, "
                        + "case when su.mber_ci is null or su.mber_ci = '' then 'N' else 'Y' end as mber_ci, "
                        + "substr(s.email, 1, position('@' in s.email) - 1) as email_before, "
                        + "substr(s.email, position('@' in s.email) + 1) as email_after "
                        + "from public.op_seller s inner join public.op_seller_user su on su.login_id = s.login_id "
                        + "where s.seller_id = :id", Tuple.class)
                .setParameter("id", sellerId).getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        for (TupleElement<?> e : rows.get(0).getElements()) {
            if ("password".equals(e.getAlias())) {
                continue;
            }
            m.put(camel(e.getAlias()), rows.get(0).get(e.getAlias()));
        }
        return m;
    }

    // ---- 아이디 생성 ----

    /**
     * AS-IS makeSellerId - RPAD(지자체코드, 10, LPAD(같은 접두 계정 수 + 순번, 5, '0')) 를 비어 있는 아이디가 나올 때까지 순번을 올린다.
     * (AS-IS는 회원표(OP_USER) 중복도 보지만 TO-BE 판매자는 회원과 아이디 공간이 따로라 판매자/계정표만 본다.)
     */
    @Transactional(readOnly = true)
    public String makeSellerId(String locgovCode) {
        String code = locgovCode == null ? "" : locgovCode;
        int count = ((Number) em.createNativeQuery("select count(*) from public.op_seller_user where login_id like concat(cast(:c as varchar), '%')")
                .setParameter("c", code).getSingleResult()).intValue();
        int plus = 0;
        String loginId;
        do {
            plus++;
            String fill = String.format("%05d", count + plus);
            StringBuilder sb = new StringBuilder(code);
            while (sb.length() < 10) {
                sb.append(fill);
            }
            loginId = sb.substring(0, 10);
        } while (loginIdExists(loginId));
        return loginId;
    }

    @Transactional(readOnly = true)
    public boolean loginIdExists(String loginId) {
        long n = ((Number) em.createNativeQuery("select (select count(*) from public.op_seller where login_id = :l) "
                        + "+ (select count(*) from public.op_seller_user where login_id = :l)")
                .setParameter("l", loginId).getSingleResult()).longValue();
        return n > 0;
    }

    // ---- 등록 ----

    /**
     * AS-IS insertSeller + insertSellerMasterUser. 전화/팩스/사업자번호는 앞·가운데·뒤 입력칸을 하이픈으로 이어 저장하고(대표번호는 "미선택"이면
     * 국번 생략), 정산주기는 화면에 입력칸이 없어 월정산(4)/8일로 고정한다. 계정은 상태 9(정상)로 만들며 임시비밀번호(1111)는 BCrypt로 저장한다.
     * @return 새 판매자번호
     */
    public long create(Map<String, String> p, MultipartFile[] files, Long createdUserId, String locgovCode, String passwordExpiredDate) {
        long sellerId = ((Number) em.createNativeQuery("select nextval('op_seller_seller_id_seq')").getSingleResult()).longValue();
        String loginId = p.get("loginId");
        String hash = passwordEncoder.encode(p.get("password") == null ? "" : p.get("password"));
        String now = LocalDateTime.now().format(TS);

        String tel1 = p.get("telephoneNumber1");
        String telephoneNumber = ("미선택".equals(tel1) ? "" : tel1 + "-") + p.get("telephoneNumber2") + "-" + p.get("telephoneNumber3");
        String phoneNumber = join(p, "phoneNumber");
        String[] fileNames = new String[3];
        for (int i = 0; i < 3; i++) {
            fileNames[i] = files != null && i < files.length && files[i] != null && files[i].getSize() > 0 ? storeFile(sellerId, files[i]) : null;
        }

        Map<String, Object> v = new LinkedHashMap<>();
        v.put("seller_id", col(sellerId, "bigint"));
        v.put("seller_name", col(p.get("sellerName"), "varchar"));
        v.put("login_id", col(loginId, "varchar"));
        v.put("password", col(hash, "varchar"));
        v.put("user_name", col(p.get("userName"), "varchar"));
        v.put("telephone_number", col(telephoneNumber, "varchar"));
        v.put("phone_number", col(phoneNumber, "varchar"));
        v.put("fax_number", col(join(p, "faxNumber"), "varchar"));
        v.put("email", col(p.get("email"), "varchar"));
        v.put("post", col(p.get("post"), "varchar"));
        v.put("address", col(p.get("address"), "varchar"));
        v.put("address_detail", col(p.get("addressDetail"), "varchar"));
        v.put("second_user_name", col(p.get("secondUserName"), "varchar"));
        v.put("second_telephone_number", col(join(p, "secondTelephoneNumber"), "varchar"));
        v.put("second_phone_number", col(join(p, "secondPhoneNumber"), "varchar"));
        v.put("second_email", col(p.get("secondEmail"), "varchar"));
        v.put("company_name", col(p.get("companyName"), "varchar"));
        v.put("representative_name", col(p.get("representativeName"), "varchar"));
        v.put("business_number", col(join(p, "businessNumber"), "varchar"));
        v.put("business_location", col(p.get("businessLocation"), "varchar"));
        v.put("business_type", col(p.get("businessType"), "varchar"));
        v.put("business_items", col(p.get("businessItems"), "varchar"));
        v.put("commission_rate", col(dbl(p.get("commissionRate")), "double precision"));
        v.put("remittance_type", col("4", "varchar"));
        v.put("remittance_day", col("8", "varchar"));
        v.put("bank_name", col(p.get("bankName"), "varchar"));
        v.put("bank_in_name", col(p.get("bankInName"), "varchar"));
        v.put("bank_account_number", col(p.get("bankAccountNumber"), "varchar"));
        v.put("shipping_flag", col(p.get("shippingFlag"), "varchar"));
        v.put("shipping", col(num(p.get("shipping")), "integer"));
        v.put("shipping_free_amount", col(num(p.get("shippingFreeAmount")), "integer"));
        v.put("shipping_extra_charge1", col(num(p.get("shippingExtraCharge1")), "integer"));
        v.put("shipping_extra_charge2", col(num(p.get("shippingExtraCharge2")), "integer"));
        v.put("item_approval_type", col(p.get("itemApprovalType"), "varchar"));
        v.put("sms_send_time", col(p.get("smsSendTime"), "varchar"));
        v.put("md_id", col(has(p.get("mdId")) ? Integer.valueOf(p.get("mdId").trim()) : null, "integer"));
        v.put("md_name", col(p.get("mdName"), "varchar"));
        v.put("status_code", col(p.get("statusCode"), "varchar"));
        v.put("created_date", col(now, "varchar"));
        v.put("created_user_id", col(createdUserId, "bigint"));
        v.put("updated_date", col(now, "varchar"));
        v.put("updated_user_id", col(createdUserId, "bigint"));
        v.put("mail_order_number", col(p.get("mailOrderNumber"), "varchar"));
        v.put("buy_safety_use_confirm_number", col(p.get("buySafetyUseConfirmNumber"), "varchar"));
        v.put("tax_type", col(p.get("taxType"), "varchar"));
        v.put("file_name_certificate1", col(fileNames[0], "varchar"));
        v.put("file_name_certificate2", col(fileNames[1], "varchar"));
        v.put("file_name_certificate3", col(fileNames[2], "varchar"));
        v.put("locgov_code", col(locgovCode, "varchar"));
        v.put("adult_item_yn", col(flag(p, "adultItemYn"), "varchar"));
        v.put("community_business_yn", col(flag(p, "communityBusinessYn"), "varchar"));
        insert("op_seller", v);

        long userId = ((Number) em.createNativeQuery("select coalesce(max(user_id), 0) + 1 from public.op_seller_user").getSingleResult()).longValue();
        Map<String, Object> u = new LinkedHashMap<>();
        u.put("user_id", col(userId, "bigint"));
        u.put("login_id", col(loginId, "varchar"));
        u.put("password", col(hash, "varchar"));
        u.put("phone_number", col(phoneNumber, "varchar"));
        u.put("user_name", col(p.get("sellerName"), "varchar"));
        u.put("email", col(p.get("email"), "varchar"));
        u.put("login_count", col(0L, "bigint"));
        u.put("status_code", col(9L, "bigint"));
        u.put("created_date", col(now, "varchar"));
        u.put("updated_date", col(now, "varchar"));
        u.put("password_expired_date", col(passwordExpiredDate, "varchar"));
        u.put("locgov_code", col(loginId == null ? null : loginId.substring(0, Math.min(5, loginId.length())), "varchar"));
        u.put("receive_sms", col(p.get("receiveSms"), "varchar"));
        insert("op_seller_user", u);
        return sellerId;
    }

    // ---- 수정 ----

    /**
     * AS-IS updateSeller - 값이 있는 칸만 고치고(전화·팩스·사업자번호는 앞자리가 오면 다시 이어 붙인다), 판매자 이름·휴대폰·이메일은 계정에도 같이 반영한다.
     * 영업상태는 계정 상태로도 옮긴다(사용 2→9 정상, 중지 3→2 차단, 해지 4→3 탈퇴, 승인대기 5→1 가입대기). 새 첨부를 올리면 이전 파일은 지운다.
     */
    public void update(long sellerId, Map<String, String> p, MultipartFile[] files, Long updatedUserId) {
        @SuppressWarnings("unchecked")
        List<Object[]> cur = em.createNativeQuery("select login_id, file_name_certificate1, file_name_certificate2, file_name_certificate3 "
                + "from public.op_seller where seller_id = :id").setParameter("id", sellerId).getResultList();
        if (cur.isEmpty()) {
            throw new GiftException("판매자를 찾을 수 없습니다.");
        }
        String loginId = str(cur.get(0)[0]);
        String[] oldFiles = {str(cur.get(0)[1]), str(cur.get(0)[2]), str(cur.get(0)[3])};

        Map<String, Object> set = new LinkedHashMap<>();
        if (has(p.get("sellerName"))) {
            set.put("seller_name", col(p.get("sellerName"), "varchar"));
        }
        if (has(p.get("userName"))) {
            set.put("user_name", col(p.get("userName"), "varchar"));
        }
        if (p.get("telephoneNumber1") != null) {
            String tel1 = p.get("telephoneNumber1");
            set.put("telephone_number", col(("미선택".equals(tel1) ? "" : tel1 + "-") + p.get("telephoneNumber2") + "-" + p.get("telephoneNumber3"), "varchar"));
        }
        String phoneNumber = null;
        if (p.get("phoneNumber1") != null) {
            phoneNumber = join(p, "phoneNumber");
            set.put("phone_number", col(phoneNumber, "varchar"));
        }
        if (p.get("faxNumber1") != null) {
            set.put("fax_number", col(join(p, "faxNumber"), "varchar"));
        }
        if (p.get("email") != null) {
            set.put("email", col(p.get("email"), "varchar"));
        }
        putIf(set, "post", p.get("post"));
        putIf(set, "address", p.get("address"));
        putIf(set, "address_detail", p.get("addressDetail"));
        putNotNull(set, "second_user_name", p.get("secondUserName"));
        if (p.get("secondTelephoneNumber1") != null) {
            set.put("second_telephone_number", col(join(p, "secondTelephoneNumber"), "varchar"));
        }
        if (p.get("secondPhoneNumber1") != null) {
            set.put("second_phone_number", col(join(p, "secondPhoneNumber"), "varchar"));
        }
        putNotNull(set, "second_email", p.get("secondEmail"));
        putIf(set, "company_name", p.get("companyName"));
        putIf(set, "representative_name", p.get("representativeName"));
        if (p.get("businessNumber1") != null) {
            set.put("business_number", col(join(p, "businessNumber"), "varchar"));
        }
        putIf(set, "business_location", p.get("businessLocation"));
        putIf(set, "business_type", p.get("businessType"));
        putIf(set, "business_items", p.get("businessItems"));
        set.put("commission_rate", col(dbl(p.get("commissionRate")), "double precision"));
        putIf(set, "remittance_type", p.get("remittanceType"));
        putIf(set, "remittance_day", p.get("remittanceDay"));
        putIf(set, "bank_name", p.get("bankName"));
        putIf(set, "bank_in_name", p.get("bankInName"));
        putIf(set, "bank_account_number", p.get("bankAccountNumber"));
        putIf(set, "shipping_flag", p.get("shippingFlag"));
        set.put("shipping", col(num(p.get("shipping")), "integer"));
        set.put("shipping_free_amount", col(num(p.get("shippingFreeAmount")), "integer"));
        set.put("shipping_extra_charge1", col(num(p.get("shippingExtraCharge1")), "integer"));
        set.put("shipping_extra_charge2", col(num(p.get("shippingExtraCharge2")), "integer"));
        putNotNull(set, "item_approval_type", p.get("itemApprovalType"));
        putNotNull(set, "sms_send_time", p.get("smsSendTime"));
        if (p.get("mdId") != null) {
            set.put("md_id", col(has(p.get("mdId")) ? Integer.valueOf(p.get("mdId").trim()) : null, "integer"));
        }
        putNotNull(set, "md_name", p.get("mdName"));
        putIf(set, "status_code", p.get("statusCode"));
        putNotNull(set, "mail_order_number", p.get("mailOrderNumber"));
        putNotNull(set, "buy_safety_use_confirm_number", p.get("buySafetyUseConfirmNumber"));
        putIf(set, "tax_type", p.get("taxType"));
        String adult = flag(p, "adultItemYn");
        putIf(set, "adult_item_yn", adult);
        for (int i = 0; i < 3; i++) {
            if (files != null && i < files.length && files[i] != null && files[i].getSize() > 0) {
                if (oldFiles[i] != null) {
                    deleteStored(sellerId, oldFiles[i]);
                }
                set.put("file_name_certificate" + (i + 1), col(storeFile(sellerId, files[i]), "varchar"));
            }
        }
        set.put("updated_date", col(LocalDateTime.now().format(TS), "varchar"));
        set.put("updated_user_id", col(updatedUserId, "bigint"));
        putIf(set, "community_business_yn", flag(p, "communityBusinessYn"));
        update("op_seller", set, "seller_id = :wid", sellerId);

        Map<String, Object> su = new LinkedHashMap<>();
        if (has(p.get("sellerName"))) {
            su.put("user_name", col(p.get("sellerName"), "varchar"));
        }
        if (has(phoneNumber)) {
            su.put("phone_number", col(phoneNumber, "varchar"));
        }
        if (p.get("email") != null) {
            su.put("email", col(p.get("email"), "varchar"));
        }
        if (has(p.get("statusCode"))) {
            su.put("status_code", col(switch (p.get("statusCode")) {
                case "1", "2" -> 9L;
                case "3" -> 2L;
                case "4" -> 3L;
                case "5" -> 1L;
                default -> 2L;
            }, "bigint"));
        }
        su.put("receive_sms", col(p.get("receiveSms"), "varchar"));
        updateByLoginId("op_seller_user", su, loginId);
    }

    // ---- 비밀번호 초기화 / 첨부 삭제 ----

    /** AS-IS initSellerPassword - 판매자·계정 비밀번호를 바꾸고 계정은 "비밀번호 변경 필요(PWD_CHG_SELLER_YN='Y')"·로그인 실패 0회로 만든다. */
    public void initPassword(long sellerId, String rawPassword, Long updatedUserId) {
        if (!has(rawPassword)) {
            throw new GiftException("문제가 발생했습니다.");
        }
        String hash = passwordEncoder.encode(rawPassword);
        String now = LocalDateTime.now().format(TS);
        int n = em.createNativeQuery("update public.op_seller set password = :p, updated_date = :d, updated_user_id = :u where seller_id = :id")
                .setParameter("p", hash).setParameter("d", now).setParameter("u", updatedUserId).setParameter("id", sellerId).executeUpdate();
        if (n == 0) {
            throw new GiftException("판매자를 찾을 수 없습니다.");
        }
        em.createNativeQuery("update public.op_seller_user set password = :p, updated_date = :d, pwd_chg_seller_yn = 'Y', login_fail_count = 0 "
                        + "where login_id = (select login_id from public.op_seller where seller_id = :id)")
                .setParameter("p", hash).setParameter("d", now).setParameter("id", sellerId).executeUpdate();
    }

    /** AS-IS deleteFile - 저장된 파일을 지우고 해당 첨부 칸을 비운다. fileType은 file1/file2/file3. */
    public void deleteFile(long sellerId, String fileType, Long updatedUserId) {
        int idx = java.util.Arrays.asList(FILE_TYPES).indexOf(fileType);
        if (idx < 0) {
            return;
        }
        String column = "file_name_certificate" + (idx + 1);
        List<?> rows = em.createNativeQuery("select " + column + " from public.op_seller where seller_id = :id").setParameter("id", sellerId).getResultList();
        if (!rows.isEmpty() && rows.get(0) != null) {
            deleteStored(sellerId, rows.get(0).toString());
        }
        em.createNativeQuery("update public.op_seller set " + column + " = null, updated_date = :d, updated_user_id = :u where seller_id = :id")
                .setParameter("d", LocalDateTime.now().format(TS)).setParameter("u", updatedUserId).setParameter("id", sellerId).executeUpdate();
    }

    // ---- 파일 ----

    /** AS-IS saveFile - {업로드폴더}/seller/{판매자번호}/{원본파일명}. 경로 이탈을 막으려고 파일명만 쓴다. */
    private String storeFile(long sellerId, MultipartFile file) {
        String original = file.getOriginalFilename();
        String name = original == null ? "file" : Paths.get(original.replace('\\', '/')).getFileName().toString();
        try {
            Path dir = Paths.get(uploadDir, "seller", String.valueOf(sellerId));
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GiftException("파일 업로드에 실패 하였습니다.");
        }
        return name;
    }

    private void deleteStored(long sellerId, String fileName) {
        try {
            Path target = Paths.get(uploadDir, "seller", String.valueOf(sellerId)).resolve(Paths.get(fileName).getFileName().toString());
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // AS-IS fileStorage.delete도 실패를 삼킨다
        }
    }

    // ---- SQL 조립 ----

    /** 칼럼 값 + 바인딩할 SQL 타입. null 바인딩이 bytea로 잡히지 않게 모든 값에 cast를 건다. */
    private record Col(Object value, String type) {
    }

    private static Col col(Object value, String type) {
        return new Col(value, type);
    }

    private void insert(String table, Map<String, Object> values) {
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            if (i > 0) {
                cols.append(", ");
                vals.append(", ");
            }
            cols.append(e.getKey());
            vals.append("cast(:p").append(i).append(" as ").append(((Col) e.getValue()).type()).append(")");
            i++;
        }
        Query q = em.createNativeQuery("insert into public." + table + " (" + cols + ") values (" + vals + ")");
        i = 0;
        for (Object c : values.values()) {
            q.setParameter("p" + i++, ((Col) c).value());
        }
        q.executeUpdate();
    }

    private void update(String table, Map<String, Object> set, String where, Object whereValue) {
        if (set.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> e : set.entrySet()) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(" = cast(:p").append(i).append(" as ").append(((Col) e.getValue()).type()).append(")");
            i++;
        }
        Query q = em.createNativeQuery("update public." + table + " set " + sb + " where " + where);
        i = 0;
        for (Object c : set.values()) {
            q.setParameter("p" + i++, ((Col) c).value());
        }
        q.setParameter("wid", whereValue);
        q.executeUpdate();
    }

    private void updateByLoginId(String table, Map<String, Object> set, String loginId) {
        if (set.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> e : set.entrySet()) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(" = cast(:p").append(i).append(" as ").append(((Col) e.getValue()).type()).append(")");
            i++;
        }
        Query q = em.createNativeQuery("update public." + table + " set " + sb + " where login_id = :wid");
        i = 0;
        for (Object c : set.values()) {
            q.setParameter("p" + i++, ((Col) c).value());
        }
        q.setParameter("wid", loginId);
        q.executeUpdate();
    }

    // ---- helpers ----

    /** "{base}1-{base}2-{base}3". */
    private static String join(Map<String, String> p, String base) {
        return p.get(base + "1") + "-" + p.get(base + "2") + "-" + p.get(base + "3");
    }

    /** 체크박스 + hidden "!name"=N 쌍(스프링 field default) - 체크가 오면 그 값, 안 오고 hidden이 오면 N. */
    private static String flag(Map<String, String> p, String name) {
        if (p.get(name) != null) {
            return p.get(name);
        }
        return p.get("!" + name);
    }

    private static void putIf(Map<String, Object> set, String column, String value) {
        if (has(value)) {
            set.put(column, col(value, "varchar"));
        }
    }

    private static void putNotNull(Map<String, Object> set, String column, String value) {
        if (value != null) {
            set.put(column, col(value, "varchar"));
        }
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static int num(String s) {
        if (!has(s)) {
            return 0;
        }
        return Integer.parseInt(s.replace(",", "").trim());
    }

    private static double dbl(String s) {
        return has(s) ? Double.parseDouble(s.trim()) : 0d;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String camel(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                up = true;
            } else {
                sb.append(up ? Character.toUpperCase(c) : c);
                up = false;
            }
        }
        return sb.toString();
    }
}
