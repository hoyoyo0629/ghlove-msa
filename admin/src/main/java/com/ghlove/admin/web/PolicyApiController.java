package com.ghlove.admin.web;

import com.ghlove.admin.domain.Policy;
import com.ghlove.admin.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 약관/정책 본문 조회 전용 JSON API (다른 서비스용, 쓰기 경로 아님) - BannerApiController와
 * 같은 패턴이다.
 *
 * <p>AS-IS는 이 셋(이용약관/개인정보처리방침/저작권정책)을 OP_POLICY에 저장하고 opmanager
 * 약관관리 화면에서 편집했으며, 공개 화면은 {@code /api/policy/clause|protect|copyright}로
 * 받아 뿌렸다. TO-BE도 편집은 admin의 약관관리(/policy)가 담당하므로, 공개 화면이 읽을
 * 창구를 여기서 열어준다 - 경로 이름은 AS-IS API와 같게 맞춰 대응 관계가 보이게 했다.
 *
 * <p>전시중인 본문이 없으면 404를 준다. 호출측(donation)은 그때 기존 리소스 원문으로
 * 폴백하므로, DB 적재 전에도 화면이 비지 않는다.
 */
@RestController
@RequiredArgsConstructor
public class PolicyApiController {

    /** AS-IS ApiPolicyController의 엔드포인트 이름 → POLICY_TYPE 매핑.
     *  "collection"(타입 6, 개인정보 수집·이용 동의)은 AS-IS ApiPolicyController에는 없고
     *  회원가입 전용 JoinController.getPolicyInfo가 같은 조회를 한다 - 이름은 AS-IS
     *  PolicyInfo.POLICY_TYPE_COLLECTION_POLICY 상수명을 따서 새로 붙였다(기능은 AS-IS에
     *  이미 있는 것, URL 이름만 신규). */
    private static final Map<String, String> TYPE_BY_NAME = Map.of(
            "clause", Policy.TYPE_AGREEMENT,
            "protect", Policy.TYPE_PROTECT_POLICY,
            "trader-raw", Policy.TYPE_TRADER_RAW,
            "marketing", Policy.TYPE_MARKETING_AGREEMENT,
            "copyright", Policy.TYPE_COPYRIGHT,
            "collection", "6");

    private final PolicyRepository policyRepository;

    public record PolicyDto(Integer policyId, String policyType, String title, String content,
                            String createdDate, String updatedDate) {}

    @GetMapping("/api/policies/{name}")
    public ResponseEntity<PolicyDto> current(@PathVariable String name) {
        String policyType = TYPE_BY_NAME.get(name);
        if (policyType == null) {
            return ResponseEntity.notFound().build();
        }
        return policyRepository
                .findFirstByPolicyTypeAndExhibitionStatusOrderByCreatedDateDesc(policyType, Policy.EXHIBITION_ON)
                .map(p -> ResponseEntity.ok(new PolicyDto(p.getPolicyId(), p.getPolicyType(), p.getTitle(),
                        p.getContent(), p.getCreatedDate(), p.getUpdatedDate())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
