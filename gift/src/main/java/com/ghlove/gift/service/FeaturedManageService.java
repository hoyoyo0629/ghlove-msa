package com.ghlove.gift.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 이벤트 관리(AS-IS opmanager/featured - FeaturedManagerController·FeaturedServiceImpl·featured-mapper).
 * OP_FEATURED는 지자체별 이벤트다(AS-IS 1,063건 전부 FEATURED_CLASS 0·FEATURED_TYPE 1).
 *
 * <p>SQL은 매퍼의 getFeaturedCountByParam/getFeaturedListByParam/getFeaturedById/insertFeatured/updateFeaturedById/
 * updateFeaturedImageById/deleteFeaturedsById/getFeaturedItemListByParam/insertFeaturedItem을 옮겼다. 진행상태(PROGRESSION)는
 * AS-IS가 {@code DATE_FORMAT(NOW(), '%Y%m%d%H')}와 비교한다 - 개발DB 시간대가 UTC라 한국 시각 문자열을 넘겨 비교한다.
 * G_LOCGOV는 gift DB에 없어서 시도 필터의 "그 시도에 속한 지자체 코드" 목록과 지자체명은 admin이 채운다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturedManageService {

    private static final DateTimeFormatter NOW_KEY = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /** AS-IS Const.DATENANO_FORMAT(yyyyMMddHHmmssSSSS) - 첨부 저장 파일명. */
    private static final DateTimeFormatter DATENANO = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    /** AS-IS fileSave의 FileUtils.isPossibleUploadFile 허용 목록. */
    private static final String AVAILABLE_EXTENSION = "*.jpg;*.jpeg;*.gif;*.png;";

    private final EntityManager em;
    private final GiftService giftService;

    @Value("${gift.upload.dir}")
    private String uploadDir;

    /** 목록 조건 - AS-IS FeaturedParam 중 이 화면이 쓰는 것. locgovCodes는 시도를 골랐을 때 그 시도에 속한 지자체 코드(admin이 채움). */
    public record Search(String featuredType, String where, String query, String progression, String displayListFlag,
                         String locgovCode, String upperLocgovCode, List<String> locgovCodes) {
    }

    public record Row(Integer featuredId, String featuredName, String progression, String displayListFlag, String startDate,
                      String endDate, String createdDate, String locgovCode) {
    }

    public record Item(Long itemId, String itemUserCode, String itemName, Integer salePrice, String thumbnail) {
    }

    public record Detail(Integer featuredId, Integer featuredClass, String featuredType, String featuredUrl, String featuredName,
                         String featuredContent, String featuredImage, String featuredImageMobile, String featuredListImage,
                         String featuredFlag, String displayListFlag, String link, String prodState, String startDate,
                         String startTime, String endDate, String endTime, String featuredHost, String featuredPhoneNo1,
                         String featuredPhoneNo2, String featuredPhoneNo3, String locgovCode, String createdDate, List<Item> items,
                         List<String> userGroups) {
    }

    /** 등록·수정 폼 입력값 - AS-IS Featured 도메인 중 form.jsp가 보내는 것. */
    public record Form(String featuredType, String featuredName, String startDate, String startTime, String endDate, String endTime,
                       String featuredPhoneNo1, String featuredPhoneNo2, String featuredPhoneNo3, String featuredHost, String link,
                       String featuredContent, String prodState, String displayListFlag, String featuredFlag, String prodString) {
    }

    // ================= 목록 =================

    @Transactional(readOnly = true)
    public int count(Search s) {
        Map<String, Object> p = new LinkedHashMap<>();
        String sql = "select count(*) from public.op_featured f" + where(s, p);
        Query q = em.createNativeQuery(sql);
        p.forEach(q::setParameter);
        if (sql.contains(":nowKey")) {
            q.setParameter("nowKey", nowKey());
        }
        return ((Number) q.getSingleResult()).intValue();
    }

    /** AS-IS getFeaturedListByParam - sort가 "date"로 고정이라 CREATED_DATE DESC. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Row> list(Search s, int offset, int limit) {
        Map<String, Object> p = new LinkedHashMap<>();
        String where = where(s, p);
        Query q = em.createNativeQuery("select f.featured_id, f.featured_name, " + PROGRESSION_CASE + " as progression,"
                + " f.display_list_flag, f.start_date, f.end_date, f.created_date, f.locgov_code"
                + " from public.op_featured f" + where + " order by f.created_date desc limit :limit offset :offset");
        p.forEach(q::setParameter);
        q.setParameter("nowKey", nowKey());
        q.setParameter("limit", limit > 0 ? limit : 10).setParameter("offset", Math.max(offset, 0));
        List<Row> rows = new ArrayList<>();
        for (Object[] r : (List<Object[]>) q.getResultList()) {
            rows.add(new Row(((Number) r[0]).intValue(), s(r[1]), s(r[2]), s(r[3]), s(r[4]), s(r[5]), s(r[6]), s(r[7])));
        }
        return rows;
    }

    /** AS-IS PROGRESSION CASE 식 그대로(CUBRID CONCAT은 NULL이 끼면 NULL - PG의 ||와 같다). */
    private static final String PROGRESSION_CASE = """
            case
                when (f.start_date is not null or f.start_date != '') and (f.start_date || f.start_time) > :nowKey then '1'
                when (
                        ((f.start_date is null or f.start_date = '') and (f.end_date is null or f.end_date = ''))
                        or (f.start_date is not null and f.start_date != '' and f.end_date is not null and f.end_date != ''
                            and (f.start_date || f.start_time) <= :nowKey and (f.end_date || f.end_time) >= :nowKey)
                        or (f.start_date is not null and f.start_date != '' and f.end_date is null or f.end_date = ''
                            and (f.start_date || f.start_time) <= :nowKey)
                     ) then '2'
                when (f.end_date is not null and f.end_date != '') and (f.end_date || f.end_time) < :nowKey then '3'
            end""";

    /** AS-IS getFeaturedCountByParam·getFeaturedListByParam의 WHERE(두 쿼리가 같다). */
    private String where(Search s, Map<String, Object> p) {
        List<String> w = new ArrayList<>();
        if (has(s.featuredType())) {
            w.add("f.featured_type = :featuredType");
            p.put("featuredType", s.featuredType());
        }
        if (has(s.query())) {
            if ("FEATURED_NAME".equals(s.where())) {
                w.add("f.featured_name like '%' || cast(:query as varchar) || '%'");
                p.put("query", s.query());
            } else if ("FEATURED_URL".equals(s.where())) {
                w.add("f.featured_url like '%' || cast(:query as varchar) || '%'");
                p.put("query", s.query());
            }
        }
        if (has(s.displayListFlag())) {
            w.add("f.display_list_flag = :displayListFlag");
            p.put("displayListFlag", s.displayListFlag());
        }
        switch (s.progression() == null ? "" : s.progression()) {
            case "1" -> w.add("(f.start_date is not null or f.start_date != '') and (f.start_date || f.start_time) > :nowKey");
            case "2" -> w.add("""
                    (
                        ((f.start_date is null or f.start_date = '') and (f.end_date is null or f.end_date = ''))
                        or (f.start_date is not null and f.start_date != '' and f.end_date is not null and f.end_date != ''
                            and (f.start_date || f.start_time) <= :nowKey and (f.end_date || f.end_time) >= :nowKey)
                        or (f.start_date is not null and f.start_date != '' and f.end_date is null or f.end_date = ''
                            and (f.start_date || f.start_time) <= :nowKey)
                    )""");
            case "3" -> w.add("(f.end_date is not null and f.end_date != '') and (f.end_date || f.end_time) < :nowKey");
            default -> {
            }
        }
        if (has(s.locgovCode())) {
            w.add("f.locgov_code = :locgovCode");
            p.put("locgovCode", s.locgovCode());
        } else if (has(s.upperLocgovCode())) {
            List<String> codes = s.locgovCodes() == null ? List.of() : s.locgovCodes();
            if (codes.isEmpty()) {
                w.add("1 = 0");
            } else {
                w.add("f.locgov_code in (:locgovCodes)");
                p.put("locgovCodes", codes);
            }
        }
        return w.isEmpty() ? "" : " where " + String.join(" and ", w);
    }

    /** AS-IS DATE_FORMAT(NOW(), '%Y%m%d%H') - 한국 시각. */
    private static String nowKey() {
        return LocalDateTime.now(KST).format(NOW_KEY);
    }

    // ================= 상세 =================

    /** AS-IS getFeaturedById(번호만으로 찾는다 - 지자체 확인 없음) + getFeaturedItemListByParam + getUserDefGroupById. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Detail detail(int featuredId) {
        List<Object[]> rows = em.createNativeQuery("""
                select featured_id, featured_class, featured_type, featured_url, featured_name, featured_content, featured_image,
                       featured_image_mobile, featured_list_image, featured_flag, display_list_flag, link, prod_state, start_date,
                       start_time, end_date, end_time, featured_host, featured_phone_no1, featured_phone_no2, featured_phone_no3,
                       locgov_code, created_date
                  from public.op_featured where featured_id = :id""").setParameter("id", featuredId).getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        // AS-IS getFeaturedItemListByParam - 판매중(defaultOnSaleWhereQuery)·대민 노출·사용 판매자 상품만, 편성 순서대로.
        // TO-BE 답례품은 ITEM_TYPE을 채우지 않는다(전부 NULL) - AS-IS 기본값 '1'(일반상품)로 본다.
        List<Object[]> itemRows = em.createNativeQuery("""
                select i.item_id, i.item_user_code, i.item_name, i.sale_price
                  from public.op_featured_item fi
                       inner join public.op_item i on fi.item_id = i.item_id
                       inner join public.op_seller s on i.seller_id = s.seller_id
                 where fi.featured_id = :id
                   and i.data_status_code = '1' and i.display_flag = 'Y' and i.item_data_type = '1'
                   and coalesce(i.item_type, '1') in ('1', '3')
                   and i.front_display_flag = 'Y'
                   and s.status_code = '2'
                 group by i.item_id, i.item_user_code, i.item_name, i.sale_price
                 order by min(fi.display_order)""").setParameter("id", featuredId).getResultList();
        List<Long> ids = itemRows.stream().map(x -> ((Number) x[0]).longValue()).toList();
        Map<Long, String> thumbs = ids.isEmpty() ? Map.of() : giftService.thumbnailsOf(ids);
        List<Item> items = new ArrayList<>();
        for (Object[] x : itemRows) {
            long itemId = ((Number) x[0]).longValue();
            String stored = s(x[1]);
            items.add(new Item(itemId, stored != null && !stored.isBlank() ? stored : "G2" + String.format("%09d", itemId), s(x[2]),
                    x[3] == null ? null : ((Number) x[3]).intValue(), thumbs.get(itemId)));
        }
        List<String> groups = new ArrayList<>();
        for (Object g : em.createNativeQuery("""
                select fi.user_def_group from public.op_featured_item fi
                 where fi.featured_id = :id and fi.user_def_group is not null
                 group by fi.user_def_group order by min(fi.user_def_group_order)""").setParameter("id", featuredId).getResultList()) {
            groups.add(g == null ? null : g.toString());
        }
        return new Detail(((Number) r[0]).intValue(), r[1] == null ? null : ((Number) r[1]).intValue(), s(r[2]), s(r[3]), s(r[4]),
                s(r[5]), s(r[6]), s(r[7]), s(r[8]), s(r[9]), s(r[10]), s(r[11]), s(r[12]), s(r[13]), s(r[14]), s(r[15]), s(r[16]),
                s(r[17]), s(r[18]), s(r[19]), s(r[20]), s(r[21]), s(r[22]), items, groups);
    }

    // ================= 등록 / 수정 =================

    /**
     * AS-IS insertFeatured - 기존 전부 ORDERING+1 → 새 번호(시퀀스)·ORDERING 1 → 첨부 저장 → insert → 선택 상품.
     * FEATURED_URL은 번호 그대로, FEATURED_CODE는 ""(구분 체크박스가 화면에 없다), FEATURED_CLASS는 0(폼이 안 보냄 - int 기본값).
     */
    @Transactional
    public int create(Form f, String locgovCode, MultipartFile featuredFile, MultipartFile featuredFileMobile, MultipartFile featuredListFile) {
        em.createNativeQuery("update public.op_featured set ordering = ordering + 1").executeUpdate();
        int id = ((Number) em.createNativeQuery("select nextval('public.op_featured_featured_id_seq')").getSingleResult()).intValue();
        String featuredImage = save(id, featuredFile, 2);
        String featuredImageMobile = save(id, featuredFileMobile, 3);
        String featuredListImage = save(id, featuredListFile, 5);
        em.createNativeQuery("""
                insert into public.op_featured (featured_id, featured_class, featured_type, featured_url, featured_code, featured_name,
                    featured_simple_content, featured_content, featured_image, featured_image_mobile, thumbnail_image, thumbnail_image_mobile,
                    featured_flag, display_list_flag, link, link_target_flag, link_rel_flag, ordering, title, keywords, description,
                    header_contents1, themaword_title, themaword_description, created_date, list_type, prod_state, start_date, start_time,
                    end_date, end_time, reply_used_flag, featured_host, featured_phone_no1, featured_phone_no2, featured_phone_no3,
                    featured_list_image, thumbnail_list_image, locgov_code)
                values (:id, 0, :featuredType, :url, '', :featuredName, '', :featuredContent, :featuredImage, :featuredImageMobile, null, null,
                    :featuredFlag, :displayListFlag, :link, 'N', 'N', 1, null, null, null, null, null, null, :createdDate, null, :prodState,
                    :startDate, :startTime, :endDate, :endTime, 'N', :featuredHost, :phone1, :phone2, :phone3, :featuredListImage, null,
                    :locgovCode)""")
                .setParameter("id", id)
                .setParameter("featuredType", f.featuredType())
                .setParameter("url", String.valueOf(id))
                .setParameter("featuredName", nz(f.featuredName()))
                .setParameter("featuredContent", nz(f.featuredContent()))
                .setParameter("featuredImage", featuredImage)
                .setParameter("featuredImageMobile", featuredImageMobile)
                .setParameter("featuredFlag", f.featuredFlag())
                .setParameter("displayListFlag", f.displayListFlag())
                .setParameter("link", nz(f.link()))
                .setParameter("createdDate", LocalDateTime.now(KST).format(DATETIME))
                .setParameter("prodState", f.prodState())
                .setParameter("startDate", f.startDate())
                .setParameter("startTime", f.startTime())
                .setParameter("endDate", f.endDate())
                .setParameter("endTime", f.endTime())
                .setParameter("featuredHost", f.featuredHost())
                .setParameter("phone1", f.featuredPhoneNo1())
                .setParameter("phone2", f.featuredPhoneNo2())
                .setParameter("phone3", f.featuredPhoneNo3())
                .setParameter("featuredListImage", featuredListImage)
                .setParameter("locgovCode", locgovCode)
                .executeUpdate();
        if (f.prodString() != null && !f.prodString().isEmpty()) {
            saveItems(id, f.prodString());
        }
        return id;
    }

    /**
     * AS-IS updateFeaturedById - 이미지 3칸은 새로 올렸을 때만 바꾸고, 지자체·등록일·정렬순서·형태(FEATURED_TYPE)는 안 바꾼다.
     * 폼이 보내지 않는 칸(FEATURED_CLASS 0, FEATURED_CODE "", 설명 "", 링크 새창/nofollow N, SEO 6칸 NULL, 댓글 N)은 기본값으로 덮는다.
     * 선택 상품은 지우고 다시 넣는다(prodString이 ""이면 지우기만).
     */
    @Transactional
    public void update(int id, Form f, MultipartFile featuredFile, MultipartFile featuredFileMobile, MultipartFile featuredListFile) {
        String featuredImage = save(id, featuredFile, 2);
        String featuredImageMobile = save(id, featuredFileMobile, 3);
        String featuredListImage = save(id, featuredListFile, 5);
        StringBuilder sql = new StringBuilder("""
                update public.op_featured set featured_class = 0, featured_code = '', featured_name = :featuredName,
                    featured_simple_content = '', featured_content = :featuredContent, featured_flag = :featuredFlag,
                    display_list_flag = :displayListFlag, featured_url = :url,""");
        if (featuredImage != null && !featuredImage.isEmpty()) {
            sql.append(" featured_image = :featuredImage,");
        }
        if (featuredImageMobile != null && !featuredImageMobile.isEmpty()) {
            sql.append(" featured_image_mobile = :featuredImageMobile,");
        }
        sql.append("""
                 link = :link, link_target_flag = 'N', link_rel_flag = 'N',
                    title = null, keywords = null, description = null, header_contents1 = null, themaword_title = null,
                    themaword_description = null, start_date = :startDate, start_time = :startTime, end_date = :endDate,
                    end_time = :endTime, prod_state = :prodState, reply_used_flag = 'N', featured_host = :featuredHost,""");
        if (featuredListImage != null && !featuredListImage.isEmpty()) {
            sql.append(" featured_list_image = :featuredListImage,");
        }
        sql.append(" featured_phone_no1 = :phone1, featured_phone_no2 = :phone2, featured_phone_no3 = :phone3 where featured_id = :id");
        Query q = em.createNativeQuery(sql.toString())
                .setParameter("featuredName", nz(f.featuredName()))
                .setParameter("featuredContent", nz(f.featuredContent()))
                .setParameter("featuredFlag", f.featuredFlag())
                .setParameter("displayListFlag", f.displayListFlag())
                .setParameter("url", String.valueOf(id))
                .setParameter("link", nz(f.link()))
                .setParameter("startDate", f.startDate())
                .setParameter("startTime", f.startTime())
                .setParameter("endDate", f.endDate())
                .setParameter("endTime", f.endTime())
                .setParameter("prodState", f.prodState())
                .setParameter("featuredHost", f.featuredHost())
                .setParameter("phone1", f.featuredPhoneNo1())
                .setParameter("phone2", f.featuredPhoneNo2())
                .setParameter("phone3", f.featuredPhoneNo3())
                .setParameter("id", id);
        if (featuredImage != null && !featuredImage.isEmpty()) {
            q.setParameter("featuredImage", featuredImage);
        }
        if (featuredImageMobile != null && !featuredImageMobile.isEmpty()) {
            q.setParameter("featuredImageMobile", featuredImageMobile);
        }
        if (featuredListImage != null && !featuredListImage.isEmpty()) {
            q.setParameter("featuredListImage", featuredListImage);
        }
        q.executeUpdate();
        em.createNativeQuery("delete from public.op_featured_item where featured_id = :id").setParameter("id", id).executeUpdate();
        if (f.prodString() != null && !f.prodString().isEmpty()) {
            saveItems(id, f.prodString());
        }
    }

    /**
     * AS-IS saveFeaturedItem(사용자 그룹 없음 분기) - prodString을 ","로 나눈 묶음마다 "~"로 나눈 상품번호를 0부터 순서대로 넣는다.
     * 숫자가 아닌 조각은 건너뛴다. USER_DEF_GROUP은 "", USER_DEF_GROUP_ORDER는 NULL.
     */
    private void saveItems(int featuredId, String prodString) {
        String now = LocalDateTime.now(KST).format(DATETIME);
        for (String group : prodString.split(",")) {
            int displayOrder = 0;
            for (String itemId : group.split("~")) {
                int parsed;
                try {
                    parsed = Integer.parseInt(itemId);
                } catch (NumberFormatException e) {
                    continue;
                }
                em.createNativeQuery("""
                        insert into public.op_featured_item (featured_id, item_id, display_order, created_date, user_def_group, user_def_group_order)
                        values (:featuredId, :itemId, :displayOrder, :createdDate, '', null)""")
                        .setParameter("featuredId", featuredId)
                        .setParameter("itemId", parsed)
                        .setParameter("displayOrder", displayOrder++)
                        .setParameter("createdDate", now)
                        .executeUpdate();
            }
        }
    }

    /**
     * AS-IS fileSave - 확장자 검사(이미지 4종, 대소문자 무시 indexOf) → {업로드}/featured/{번호}/ 에
     * yyyyMMddHHmmssSSSS(+_mobile/_list).확장자로 저장(같은 이름이 있으면 먼저 지운다). 빈 파일이면 null.
     */
    private String save(int featuredId, MultipartFile file, int fileType) {
        if (file == null || file.getSize() <= 0) {
            return null;
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot > -1 ? original.substring(dot + 1) : "";
        if (AVAILABLE_EXTENSION.toLowerCase().indexOf(ext.toLowerCase()) <= -1) {
            throw new GiftException("이미지 파일만 업로드가 가능합니다.");
        }
        LocalDateTime now = LocalDateTime.now(KST);
        String nano = now.format(DATENANO) + String.format("%04d", now.getNano() / 1_000_000);
        String suffix = switch (fileType) {
            case 3 -> "_mobile";
            case 5 -> "_list";
            default -> "";
        };
        String fileName = nano + suffix + "." + ext;
        try {
            Path dir = Paths.get(uploadDir, "featured", String.valueOf(featuredId));
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.deleteIfExists(target);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            // AS-IS도 업로드 실패는 로그만 남기고 파일명은 그대로 저장한다.
            log.error("fileStorage.upload featured {} : {}", featuredId, e.getMessage(), e);
        }
        return fileName;
    }

    // ================= 이미지 삭제 / 선택 삭제 =================

    /**
     * AS-IS deleteImageByItemId - 칸만 비운다(2 상단 PC·3 상단 mobile·5 목록). AS-IS도 지우려는 파일 경로에 "featured/"가 한 번 더 붙어
     * ({@code /featured/{번호}/featured/{파일}}) 실제 파일은 지워지지 않는다 - 그래서 디스크는 건드리지 않는다.
     */
    @Transactional
    public boolean deleteImage(int featuredId, int fileType) {
        String column = switch (fileType) {
            case 2 -> "featured_image";
            case 3 -> "featured_image_mobile";
            case 5 -> "featured_list_image";
            default -> null;
        };
        if (column == null) {
            return false;   // AS-IS는 choose에 걸리는 칸이 없어 SQL 오류가 난다
        }
        return em.createNativeQuery("update public.op_featured set " + column + " = '' where featured_id = :id")
                .setParameter("id", featuredId).executeUpdate() > 0;
    }

    /** AS-IS deleteFeaturedsById - OP_FEATURED만 지운다(OP_FEATURED_ITEM은 남는다). */
    @Transactional
    public int delete(List<Integer> featuredIds) {
        if (featuredIds == null || featuredIds.isEmpty()) {
            return 0;
        }
        return em.createNativeQuery("delete from public.op_featured where featured_id in (:ids)")
                .setParameter("ids", featuredIds).executeUpdate();
    }

    private static boolean has(String v) {
        return v != null && !v.isEmpty();
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }

    private static String s(Object o) {
        return o == null ? null : o.toString();
    }
}
