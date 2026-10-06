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

    /** AS-IS getCodeTypeList - 왼쪽 코드구분 패널(사용중인 코드만, 코드구분명 오름차순). */
    public Map<String, Long> codeTypeList() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : commonCodeRepository.codeTypeCounts(LANGUAGE_KO)) {
            counts.put((String) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    /** AS-IS getCodeList - 검색구분(ID/LABEL)에 따라 해당 컬럼만 LIKE 검색한다. */
    public List<CommonCode> search(String codeType, String where, String query) {
        String blankedCodeType = (codeType == null || codeType.isBlank()) ? null : codeType;
        String keyword = (query == null || query.isBlank()) ? null : query;
        String idQuery = "ID".equals(where) ? keyword : null;
        String labelQuery = "LABEL".equals(where) ? keyword : null;
        return commonCodeRepository.search(LANGUAGE_KO, blankedCodeType, idQuery, labelQuery);
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
        CommonCode form = new CommonCode();
        form.setCodeType(codeType);
        form.setId(id);
        form.setLabel(label);
        form.setDetail(detail);
        form.setOrdering(ordering);
        form.setUseYn(USE_Y);
        return create(form);
    }

    /**
     * AS-IS insertCode(code-mapper.xml) - CODE_TYPE, LANGUAGE, ID, LABEL, DETAIL, ORDERING,
     * USE_YN, UP_ID, CODE_VALUE, EXTENSION_CODE, MAPPING_CODE 전부 저장한다. 예전엔 앞 6개만
     * 받아 상위ID·코드값·확장코드·매핑코드가 등록화면에서 버려졌다.
     * AS-IS는 입력값을 대문자로 바꾸지 않으므로(그대로 INSERT) 여기서도 손대지 않는다.
     */
    @Transactional
    public CommonCode create(CommonCode form) {
        if (form.getCodeType() == null || form.getCodeType().isBlank()
                || form.getId() == null || form.getId().isBlank()) {
            throw new CommonCodeException("코드유형과 코드ID는 필수입니다.");
        }
        CommonCodeId key = new CommonCodeId(form.getCodeType().trim(), LANGUAGE_KO, form.getId().trim());
        if (commonCodeRepository.existsById(key)) {
            throw new CommonCodeException("이미 존재하는 코드입니다.");
        }

        CommonCode code = new CommonCode();
        code.setCodeType(key.getCodeType());
        code.setLanguage(key.getLanguage());
        code.setId(key.getId());
        applyForm(code, form);
        return commonCodeRepository.save(code);
    }

    @Transactional
    public CommonCode update(String codeType, String id, String label, String detail, Integer ordering) {
        CommonCode form = new CommonCode();
        form.setLabel(label);
        form.setDetail(detail);
        form.setOrdering(ordering);
        form.setUseYn(get(codeType, id).getUseYn());
        return update(codeType, id, form);
    }

    /** AS-IS updateCode - 키(CODE_TYPE/LANGUAGE/ID)를 제외한 전 컬럼 갱신. */
    @Transactional
    public CommonCode update(String codeType, String id, CommonCode form) {
        CommonCode code = get(codeType, id);
        applyForm(code, form);
        return commonCodeRepository.save(code);
    }

    private void applyForm(CommonCode code, CommonCode form) {
        code.setLabel(form.getLabel());
        code.setDetail(form.getDetail());
        code.setOrdering(form.getOrdering());
        code.setUseYn(form.getUseYn() != null ? form.getUseYn() : USE_Y);
        code.setUpId(form.getUpId());
        code.setCodeValue(form.getCodeValue());
        code.setExtensionCode(form.getExtensionCode());
        code.setMappingCode(form.getMappingCode());
    }

    /** AS-IS deleteCode - 목록의 삭제 링크(하드 삭제). 사용중단은 폼의 사용유무(Y/N)로 한다. */
    @Transactional
    public void delete(String codeType, String id) {
        commonCodeRepository.deleteById(new CommonCodeId(codeType, LANGUAGE_KO, id));
    }
}
