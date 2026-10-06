package com.ghlove.donation.service;

import com.ghlove.donation.domain.LclgvHnrUserRwrdImgExpln;
import com.ghlove.donation.domain.LclgvHnrUserStngMng;
import com.ghlove.donation.repository.HonorUserRwrdImgExplnRepository;
import com.ghlove.donation.repository.HonorUserSettingRepository;
import com.ghlove.donation.repository.LocgovRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 기부혜택증 관리 (admin 메뉴 19101 설정관리 / 19102 설정 / 19103 열람현황) - AS-IS
 * {@code saleson.shop.lclgvHnrUser.LclgvHnrUserMngServiceImpl} + {@code lclgvHnrUser-mapper.xml}
 * 이식. admin 콘솔이 이 서비스의 API를 호출한다(기부혜택증 자산이 전부 donation 소유라서).
 */
@Service
@RequiredArgsConstructor
public class HonorUserAdminService {

    /** 기부혜택증 메인 이미지 저장 하위경로. */
    private static final String IMAGE_SUBDIR = "honor-user";

    private final HonorUserSettingRepository settingRepository;
    private final HonorUserRwrdImgExplnRepository imgExplnRepository;
    private final LocgovRepository locgovRepository;
    private final FileStorageService fileStorageService;
    private final EntityManager entityManager;

    /** 목록 한 행 - AS-IS selectLclgvHnrUserMngList가 내려주는 컬럼. */
    public record SettingRow(String lclgvCd, String upperLocgovCode, String upperLocgovNm, String lclgvCdNm,
                             Integer gldGrdDntnAmt, Integer slvrGrdDntnAmt, Integer brnzGrdDntnAmt,
                             String hnrUserStngTtl, String hnrUserRwrd, String rprsImgNm,
                             String hnrUserSlctnSeCd, String useYn, LocalDateTime lastRegDt, Long lastRgtrId) {
    }

    /** 열람현황 한 행 - AS-IS는 (열람일자, 회원, 지자체)로 묶어 열람횟수를 센다. */
    public record ViewHistRow(String viewYm, Long userId, String lclgvCd, String upperLocgovCode,
                              String upperLocgovNm, String lclgvCdNm, long viewCnt) {
    }

    /**
     * 설정 목록. AS-IS는 공통코드·지자체를 외부조인하고 지자체코드/시도로 걸러낸다.
     * 코드라벨(발급기준 구분명)은 admin의 OP_COMMON_CODE에 있어 코드만 내려준다.
     */
    public List<SettingRow> settingList(String upperLocgovCode, String lclgvCd) {
        return settingRepository.findAllByOrderByLclgvCdAsc().stream()
                .map(this::toRow)
                .filter(r -> lclgvCd == null || lclgvCd.isBlank() || lclgvCd.equals(r.lclgvCd()))
                .filter(r -> upperLocgovCode == null || upperLocgovCode.isBlank()
                        || upperLocgovCode.equals(r.upperLocgovCode()))
                .toList();
    }

    /** 설정 상세. 없으면 null(AS-IS도 빈 결과를 주고 화면이 빈 폼을 보여준다). */
    public SettingRow setting(String lclgvCd) {
        return settingRepository.findById(lclgvCd).map(this::toRow).orElse(null);
    }

    public List<String> imageExplains(String lclgvCd) {
        return imgExplnRepository.findByLclgvCdOrderByImgSeqAsc(lclgvCd).stream()
                .map(LclgvHnrUserRwrdImgExpln::getImgExpln)
                .toList();
    }

    /**
     * 설정 저장 - AS-IS {@code updateLclgvHnrUserMng}.
     *
     * <p>AS-IS는 UPDATE 한 방이라 행이 없으면 아무 일도 일어나지 않는데, 지자체 설정은 처음에
     * 행이 없을 수 있어(실측 0건) <b>없으면 만들고 있으면 수정</b>한다. 대표이미지는 AS-IS처럼
     * <b>새 파일이 올라왔을 때만</b> 갱신한다({@code <if test="rprsImgNm != null ...">}).
     * 이미지 설명은 지자체 단위로 전부 지우고 다시 넣는다(AS-IS 동일).
     */
    @Transactional
    public void saveSetting(String lclgvCd, Integer brnzGrdDntnAmt, String hnrUserStngTtl, String hnrUserRwrd,
                             String hnrUserSlctnSeCd, String useYn, List<String> imageExplains,
                             List<MultipartFile> images, Long managerId) {
        LclgvHnrUserStngMng setting = settingRepository.findById(lclgvCd).orElseGet(() -> {
            LclgvHnrUserStngMng created = new LclgvHnrUserStngMng();
            created.setLclgvCd(lclgvCd);
            created.setFrstRgtrId(managerId);
            created.setFrstRegDt(LocalDateTime.now());
            return created;
        });

        setting.setBrnzGrdDntnAmt(brnzGrdDntnAmt);
        setting.setHnrUserStngTtl(hnrUserStngTtl);
        setting.setHnrUserRwrd(hnrUserRwrd);
        setting.setHnrUserSlctnSeCd(hnrUserSlctnSeCd);
        setting.setUseYn(useYn == null || useYn.isBlank() ? "N" : useYn);

        MultipartFile mainImage = firstNonEmpty(images);
        if (mainImage != null) {
            if (setting.getRprsImgNm() != null && !setting.getRprsImgNm().isBlank()) {
                deleteImageQuietly(setting.getRprsImgNm());
            }
            setting.setRprsImgNm(fileStorageService.store(mainImage, IMAGE_SUBDIR));
        }

        setting.setLastRgtrId(managerId);
        setting.setLastRegDt(LocalDateTime.now());
        settingRepository.save(setting);

        imgExplnRepository.deleteByLclgvCd(lclgvCd);
        if (imageExplains != null) {
            List<LclgvHnrUserRwrdImgExpln> rows = new ArrayList<>();
            int seq = 1;
            for (String explain : imageExplains) {
                if (explain == null || explain.isBlank()) {
                    continue;
                }
                LclgvHnrUserRwrdImgExpln row = new LclgvHnrUserRwrdImgExpln();
                row.setLclgvCd(lclgvCd);
                row.setImgSeq(seq++);
                row.setImgExpln(explain);
                row.setFrstRgtrId(managerId);
                row.setFrstRegDt(LocalDateTime.now());
                rows.add(row);
            }
            imgExplnRepository.saveAll(rows);
        }
    }

    /** 대표이미지 삭제 - AS-IS {@code deleteItemFile}은 RPRS_IMG_NM을 빈 문자열로 만든다. */
    @Transactional
    public int deleteMainImage(String lclgvCd) {
        LclgvHnrUserStngMng setting = settingRepository.findById(lclgvCd).orElse(null);
        if (setting == null) {
            return 0;
        }
        if (setting.getRprsImgNm() != null && !setting.getRprsImgNm().isBlank()) {
            deleteImageQuietly(setting.getRprsImgNm());
        }
        setting.setRprsImgNm("");
        settingRepository.save(setting);
        return 1;
    }

    /** 대표이미지 바이트 - admin이 <img src>로 쓴다. */
    public byte[] mainImageBytes(String lclgvCd) {
        LclgvHnrUserStngMng setting = settingRepository.findById(lclgvCd).orElse(null);
        if (setting == null || setting.getRprsImgNm() == null || setting.getRprsImgNm().isBlank()) {
            return null;
        }
        try {
            return java.nio.file.Files.readAllBytes(
                    fileStorageService.resolve(setting.getRprsImgNm(), IMAGE_SUBDIR));
        } catch (java.io.IOException e) {
            return null;
        }
    }

    /**
     * 기부혜택증 열람현황 - AS-IS {@code lclgvHnrUserViewHist} 재현.
     *
     * <p>AS-IS는 {@code OP_HONOR_VIEW_HIST}를 <b>(열람일자 yyyy-MM-dd, USER_ID, LCLGV_CD)로
     * GROUP BY해 COUNT(*)를 열람횟수</b>로 뽑고, 회원(OP_USER)·지자체(G_LOCGOV)를 INNER JOIN한다.
     * TO-BE에서 회원은 member 소유라 여기서는 지자체만 INNER JOIN하고 <b>사용자명은 admin이
     * member에서 받아 채운다</b> - AS-IS가 INNER JOIN이므로 회원을 못 찾은 행은 admin이 버린다.
     *
     * <p>정렬도 AS-IS 그대로 지자체코드 → (사용자명) 순인데 사용자명이 여기 없으므로
     * 지자체코드·USER_ID 순으로 내려주고 admin이 사용자명으로 다시 정렬한다.
     *
     * <p>AS-IS 날짜 비교는 {@code VIEW_DT >= CONCAT(startDate,'000000')}처럼 문자열 이어붙이기인데
     * TO-BE {@code VIEW_DT}는 실제 TIMESTAMP라 날짜 경계로 비교한다(같은 범위다).
     */
    @SuppressWarnings("unchecked")
    public List<ViewHistRow> viewHist(String startDate, String endDate, String upperLocgovCode, String lclgvCd) {
        StringBuilder sql = new StringBuilder("""
                select to_char(a.view_dt, 'YYYY-MM-DD') as view_ym,
                       a.user_id,
                       a.lclgv_cd,
                       c.upper_locgov_code,
                       c.upper_locgov_nm,
                       c.locgov_nm as lclgv_cd_nm,
                       count(*) as view_cnt
                  from donation.op_honor_view_hist a
                  join donation.g_locgov c on c.locgov_code = a.lclgv_cd
                 where 1 = 1
                """);
        if (notBlank(startDate)) {
            sql.append("   and a.view_dt >= to_timestamp(:startDate, 'YYYYMMDD')\n");
        }
        if (notBlank(endDate)) {
            sql.append("   and a.view_dt < to_timestamp(:endDate, 'YYYYMMDD') + interval '1 day'\n");
        }
        if (notBlank(lclgvCd)) {
            sql.append("   and a.lclgv_cd = :lclgvCd\n");
        }
        if (notBlank(upperLocgovCode)) {
            sql.append("   and c.upper_locgov_code = :upperLocgovCode\n");
        }
        sql.append("""
                 group by to_char(a.view_dt, 'YYYY-MM-DD'), a.user_id, a.lclgv_cd,
                          c.upper_locgov_code, c.upper_locgov_nm, c.locgov_nm
                 order by a.lclgv_cd asc, a.user_id asc
                """);

        Query q = entityManager.createNativeQuery(sql.toString());
        if (notBlank(startDate)) {
            q.setParameter("startDate", startDate);
        }
        if (notBlank(endDate)) {
            q.setParameter("endDate", endDate);
        }
        if (notBlank(lclgvCd)) {
            q.setParameter("lclgvCd", lclgvCd);
        }
        if (notBlank(upperLocgovCode)) {
            q.setParameter("upperLocgovCode", upperLocgovCode);
        }

        List<Object[]> rows = q.getResultList();
        return rows.stream()
                .map(r -> new ViewHistRow(str(r[0]), r[1] == null ? null : ((Number) r[1]).longValue(),
                        str(r[2]), str(r[3]), str(r[4]), str(r[5]), ((Number) r[6]).longValue()))
                .toList();
    }

    private SettingRow toRow(LclgvHnrUserStngMng s) {
        var locgov = locgovRepository.findById(s.getLclgvCd()).orElse(null);
        return new SettingRow(s.getLclgvCd(),
                locgov == null ? null : locgov.getUpperLocgovCode(),
                locgov == null ? null : locgov.getUpperLocgovNm(),
                locgov == null ? null : locgov.getLocgovNm(),
                s.getGldGrdDntnAmt(), s.getSlvrGrdDntnAmt(), s.getBrnzGrdDntnAmt(),
                s.getHnrUserStngTtl(), s.getHnrUserRwrd(), s.getRprsImgNm(),
                s.getHnrUserSlctnSeCd(), s.getUseYn(), s.getLastRegDt(), s.getLastRgtrId());
    }

    private static MultipartFile firstNonEmpty(List<MultipartFile> files) {
        if (files == null) {
            return null;
        }
        return files.stream().filter(f -> f != null && !f.isEmpty()).findFirst().orElse(null);
    }

    private void deleteImageQuietly(String fileName) {
        try {
            java.nio.file.Files.deleteIfExists(fileStorageService.resolve(fileName, IMAGE_SUBDIR));
        } catch (java.io.IOException e) {
            // 파일이 이미 없어도 컬럼 정리는 계속한다(AS-IS도 삭제 실패를 무시한다)
        }
    }

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
