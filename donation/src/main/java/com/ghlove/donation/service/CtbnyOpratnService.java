package com.ghlove.donation.service;

import com.ghlove.donation.domain.CtbnyOpratn;
import com.ghlove.donation.domain.CtbnyOpratnFile;
import com.ghlove.donation.repository.CtbnyOpratnFileRepository;
import com.ghlove.donation.repository.CtbnyOpratnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 기부금 지출내역 (AS-IS opmanager/give/give-operation) - 지자체 담당자가 고향사랑기부금
 * 사용 내역을 등록/수정/삭제한다. admin 서비스가 관리 화면(로그인/권한)을 갖고, 이 서비스는
 * donation 자신의 DB(G_CTBNY_OPRATN)에 대한 실제 쓰기를 담당한다 - DB per Service 원칙상
 * admin이 이 테이블을 직접 건드릴 수 없다.
 */
@Service
@RequiredArgsConstructor
public class CtbnyOpratnService {

    private final CtbnyOpratnRepository ctbnyOpratnRepository;
    private final CtbnyOpratnFileRepository ctbnyOpratnFileRepository;
    private final FileStorageService fileStorageService;

    public List<CtbnyOpratn> listByLocgov(String locgovCode) {
        return ctbnyOpratnRepository.findByLocgovCodeOrderByExpndtrDeDesc(locgovCode);
    }

    public List<CtbnyOpratn> listAll() {
        return ctbnyOpratnRepository.findAll();
    }

    public CtbnyOpratn findOrThrow(Long registSn) {
        return ctbnyOpratnRepository.findById(registSn)
                .orElseThrow(() -> new DonationException("지출내역을 찾을 수 없습니다."));
    }

    public List<CtbnyOpratnFile> filesOf(Long registSn) {
        return ctbnyOpratnFileRepository.findByRegistSnOrderBySortOrdrAsc(registSn);
    }

    public Optional<CtbnyOpratnFile> file(Long fileId) {
        return ctbnyOpratnFileRepository.findById(fileId);
    }

    @Transactional
    public CtbnyOpratn create(String locgovCode, String bsnsPurpsCode, String bsnsNm, String bsnsCn,
                               LocalDateTime expndtrDe, BigDecimal expndtrAmt, String rm, Long managerId,
                               List<MultipartFile> files) {
        validate(bsnsNm, bsnsCn, expndtrDe, expndtrAmt);
        CtbnyOpratn entity = new CtbnyOpratn();
        entity.setLocgovCode(locgovCode);
        entity.setBsnsPurpsCode(bsnsPurpsCode);
        entity.setBsnsNm(bsnsNm);
        entity.setBsnsCn(bsnsCn);
        entity.setExpndtrDe(expndtrDe);
        entity.setExpndtrAmt(expndtrAmt);
        entity.setRm(rm);
        entity.setFrstRegisterId(managerId);
        entity.setFrstRegistPnttm(LocalDateTime.now());
        entity.setLastUpdusrId(managerId);
        entity.setLastUpdtPnttm(LocalDateTime.now());
        entity = ctbnyOpratnRepository.save(entity);
        storeFiles(entity.getRegistSn(), files);
        return entity;
    }

    @Transactional
    public CtbnyOpratn update(Long registSn, String bsnsPurpsCode, String bsnsNm, String bsnsCn,
                               LocalDateTime expndtrDe, BigDecimal expndtrAmt, String rm, Long managerId,
                               List<MultipartFile> files) {
        validate(bsnsNm, bsnsCn, expndtrDe, expndtrAmt);
        CtbnyOpratn entity = findOrThrow(registSn);
        entity.setBsnsPurpsCode(bsnsPurpsCode);
        entity.setBsnsNm(bsnsNm);
        entity.setBsnsCn(bsnsCn);
        entity.setExpndtrDe(expndtrDe);
        entity.setExpndtrAmt(expndtrAmt);
        entity.setRm(rm);
        entity.setLastUpdusrId(managerId);
        entity.setLastUpdtPnttm(LocalDateTime.now());
        entity = ctbnyOpratnRepository.save(entity);
        storeFiles(entity.getRegistSn(), files);
        return entity;
    }

    @Transactional
    public void delete(Long registSn) {
        ctbnyOpratnFileRepository.deleteAll(filesOf(registSn));
        ctbnyOpratnRepository.deleteById(registSn);
    }

    @Transactional
    public void deleteFile(Long fileId) {
        ctbnyOpratnFileRepository.deleteById(fileId);
    }

    private void storeFiles(Long registSn, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        int order = filesOf(registSn).size();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String storedName = fileStorageService.store(file);
            CtbnyOpratnFile entity = new CtbnyOpratnFile();
            entity.setRegistSn(registSn);
            entity.setFileNm(storedName);
            entity.setOrginlFileNm(file.getOriginalFilename());
            entity.setSortOrdr(order++);
            ctbnyOpratnFileRepository.save(entity);
        }
    }

    private static void validate(String bsnsNm, String bsnsCn, LocalDateTime expndtrDe, BigDecimal expndtrAmt) {
        if (bsnsNm == null || bsnsNm.isBlank()) {
            throw new DonationException("사업명을 입력해 주세요.");
        }
        if (bsnsCn == null || bsnsCn.isBlank()) {
            throw new DonationException("사용내용을 입력해 주세요.");
        }
        if (expndtrDe == null) {
            throw new DonationException("지출일자를 입력해 주세요.");
        }
        if (expndtrAmt == null || expndtrAmt.signum() < 0) {
            throw new DonationException("사용금액은 0원 이상이어야 합니다.");
        }
        if (expndtrAmt.compareTo(BigDecimal.valueOf(2_100_000_000L)) >= 0) {
            throw new DonationException("사용 금액은 21억 이하로 입력해 주세요.");
        }
    }
}
