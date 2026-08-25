package com.ghlove.admin.service;

import com.ghlove.admin.domain.AllowIp;
import com.ghlove.admin.repository.AllowIpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 접속IP허용목록 (AS-IS opmanager/access). */
@Service
@RequiredArgsConstructor
public class AccessService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AllowIpRepository allowIpRepository;

    public List<AllowIp> list() {
        return allowIpRepository.findByDisplayFlagOrderByAllowIpIdDesc("Y");
    }

    @Transactional
    public AllowIp register(String accessType, String remoteAddr, String managerLoginId) {
        if (remoteAddr == null || remoteAddr.isBlank()) {
            throw new AccessException("IP 주소를 입력해 주세요.");
        }
        AllowIp allowIp = new AllowIp();
        allowIp.setAccessType(accessType);
        allowIp.setRemoteAddr(remoteAddr);
        allowIp.setDisplayFlag("Y");
        allowIp.setCreatedUser(managerLoginId);
        allowIp.setCreatedDate(LocalDateTime.now().format(DATE_FORMAT));
        return allowIpRepository.save(allowIp);
    }

    /** AS-IS delete는 실제로 물리삭제하지만, 이 프로젝트는 다른 관리 목록과 동일하게 DISPLAY_FLAG
     *  토글(비표시)로 통일한다 - 감사 이력 보존. */
    @Transactional
    public void remove(Integer allowIpId, String managerLoginId) {
        AllowIp allowIp = allowIpRepository.findById(allowIpId)
                .orElseThrow(() -> new AccessException("허용 IP를 찾을 수 없습니다."));
        allowIp.setDisplayFlag("N");
        allowIp.setUpdatedUser(managerLoginId);
        allowIp.setUpdatedDate(LocalDateTime.now().format(DATE_FORMAT));
        allowIpRepository.save(allowIp);
    }
}
