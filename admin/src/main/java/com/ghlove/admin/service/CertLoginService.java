package com.ghlove.admin.service;

import com.ghlove.admin.domain.LocgovOfficer;
import com.ghlove.admin.repository.LocgovOfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * 매직라인4웹(PKI) 지자체 담당자 인증서 로그인 (AS-IS MagicLineController -
 * 브라우저 플러그인이 로컬 인증서 저장소의 인증서를 서버로 넘기면 서버가 검증).
 * 인증서 파싱/Subject DN 매칭은 방화벽 없이도 로컬에서 동작하는 부분이라 실제로
 * 구현했고, 정부 PKI 루트에 대한 OCSP 폐기여부 조회만 연계(외부망) 대상이라
 * ghlove.integrations.magicline.ocsp-enabled=false 동안 생략한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertLoginService {

    @Value("${ghlove.integrations.magicline.ocsp-enabled}")
    private boolean ocspEnabled;

    private final LocgovOfficerRepository locgovOfficerRepository;

    public X509Certificate parseCertificate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CertLoginException("인증서 파일을 선택해 주세요.");
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(file.getInputStream());
        } catch (CertificateException | IOException e) {
            throw new CertLoginException("인증서 파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }

    public LocgovOfficer authenticate(X509Certificate cert) {
        if (ocspEnabled) {
            // 방화벽이 열리면 여기서 정부 PKI 루트/OCSP 응답자에 폐기여부를 조회한다.
            log.info("[magicline] OCSP 폐기여부 조회는 아직 미구현 - ocsp-enabled=true지만 실제 호출 없음");
        } else {
            log.info("[magicline] OCSP 폐기여부 조회 생략 (연계 미개방)");
        }

        String subjectDn = cert.getSubjectX500Principal().getName();
        LocgovOfficer officer = locgovOfficerRepository.findById(subjectDn)
                .orElseThrow(() -> new CertLoginException("등록되지 않은 인증서입니다: " + subjectDn));
        if (!"Y".equals(officer.getUseYn())) {
            throw new CertLoginException("사용이 중지된 담당자 인증서입니다.");
        }
        return officer;
    }
}
