package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.CommonCodeId;
import com.ghlove.admin.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private static final String LANGUAGE_KO = "ko";
    private static final String USE_Y = "Y";
    private static final String USE_N = "N";

    private final CommonCodeRepository commonCodeRepository;

    /** 코드유형별 개수 - 관리 화면의 목차 역할. */
    public Map<String, Long> codeTypeCounts() {
        Map<String, Long> counts = new TreeMap<>();
        for (CommonCode code : commonCodeRepository.findAllByOrderByCodeTypeAscOrderingAsc()) {
            counts.merge(code.getCodeType(), 1L, Long::sum);
        }
        return new LinkedHashMap<>(counts);
    }

    public List<CommonCode> listByType(String codeType) {
        return commonCodeRepository.findByCodeTypeOrderByOrdering(codeType);
    }

    /** No-hardcoding principle: other admin features (notices/popups/...) look up labels here, never a Java enum/switch. */
    public Map<String, String> labelsOf(String codeType) {
        return commonCodeRepository.findByCodeTypeOrderByOrdering(codeType).stream()
                .filter(c -> USE_Y.equals(c.getUseYn()))
                .collect(Collectors.toMap(CommonCode::getId, CommonCode::getLabel, (a, b) -> a, LinkedHashMap::new));
    }

    public CommonCode get(String codeType, String id) {
        return commonCodeRepository.findById(new CommonCodeId(codeType, LANGUAGE_KO, id))
                .orElseThrow(() -> new CommonCodeException("코드를 찾을 수 없습니다."));
    }

    @Transactional
    public CommonCode create(String codeType, String id, String label, String detail, Integer ordering) {
        if (codeType == null || codeType.isBlank() || id == null || id.isBlank()) {
            throw new CommonCodeException("코드유형과 코드ID는 필수입니다.");
        }
        CommonCodeId key = new CommonCodeId(codeType.trim().toUpperCase(), LANGUAGE_KO, id.trim().toUpperCase());
        if (commonCodeRepository.existsById(key)) {
            throw new CommonCodeException("이미 존재하는 코드입니다.");
        }

        CommonCode code = new CommonCode();
        code.setCodeType(key.getCodeType());
        code.setLanguage(key.getLanguage());
        code.setId(key.getId());
        code.setLabel(label);
        code.setDetail(detail);
        code.setOrdering(ordering);
        code.setUseYn(USE_Y);
        return commonCodeRepository.save(code);
    }

    @Transactional
    public CommonCode update(String codeType, String id, String label, String detail, Integer ordering) {
        CommonCode code = get(codeType, id);
        code.setLabel(label);
        code.setDetail(detail);
        code.setOrdering(ordering);
        return commonCodeRepository.save(code);
    }

    /** 활성/비활성 전환 (삭제 대신 - 다른 서비스가 이미 참조 중일 수 있으므로 소프트 처리). */
    @Transactional
    public CommonCode toggleUse(String codeType, String id) {
        CommonCode code = get(codeType, id);
        code.setUseYn(USE_Y.equals(code.getUseYn()) ? USE_N : USE_Y);
        return commonCodeRepository.save(code);
    }
}
