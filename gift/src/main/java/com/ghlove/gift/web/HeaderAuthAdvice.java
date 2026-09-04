package com.ghlove.gift.web;

import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.LocgovClient;
import com.ghlove.gift.service.LocgovMapProvince;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SFR-010: fragments/header.html의 로그인/로그아웃 상태 표시가 GH_AUTH 쿠키를 보고
 * 판단할 수 있도록 모든 컨트롤러에 loggedIn 모델 속성을 공통으로 주입한다. 예전에는
 * 이 서비스에 로그인 상태를 알 방법이 없어(게이트웨이 세션 미전파) 헤더가 항상
 * 비로그인 상태로 고정 표시됐는데, 이제는 공유 JWT 쿠키로 실제 상태를 알 수 있다.
 *
 * fragments/mall-header.html이 모든 화면에서 요구하는 categoryGroups/mapProvinces/
 * locgovsByProvince도 여기서 같이 주입한다 - order 서비스 CartController에는 이미
 * 있던 패턴인데(order/CategoryClient가 gift의 /api/categories를 원격 호출) gift
 * 자신은 이 세 속성을 채우는 코드가 아예 없었다(발견: "전체 카테고리"/"지자체몰
 * 선택하기"를 눌러도 내용이 비어 있던 진짜 원인 - GNB 컴포넌트만 옮기고 데이터를
 * 채우는 컨트롤러 코드는 빠뜨렸었다). gift는 자기 자신의 categoryTree()를 원격 호출
 * 없이 바로 쓸 수 있다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class HeaderAuthAdvice {

    private final JwtVerifier jwtVerifier;
    private final GiftService giftService;
    private final LocgovClient locgovClient;

    @ModelAttribute("loggedIn")
    public boolean loggedIn(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).isPresent();
    }

    @ModelAttribute("categoryGroups")
    public Object categoryGroups() {
        return giftService.categoryTree();
    }

    @ModelAttribute("mapProvinces")
    public List<LocgovMapProvince> mapProvinces() {
        return LocgovMapProvince.ALL;
    }

    /** donation의 G_LOCGOV는 초기 테스트 시드와 이후 실제 행정구역 전량 시드가 같은
     *  시/군/구를 서로 다른 LOCGOV_CODE로 중복 보유하는 레거시 데이터가 섞여있다 -
     *  지도 그리드에 같은 구/군 이름이 두 번 뜨지 않도록 시/도 그룹 안에서 이름
     *  기준으로 중복 제거한다(먼저 나온 코드를 채택). */
    @ModelAttribute("locgovsByProvince")
    public java.util.Map<String, List<LocgovClient.LocgovInfo>> locgovsByProvince() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.groupingBy(LocgovClient.LocgovInfo::upperLocgovCode,
                        Collectors.collectingAndThen(
                                Collectors.toMap(LocgovClient.LocgovInfo::locgovNm, Function.identity(), (a, b) -> a,
                                        java.util.LinkedHashMap::new),
                                m -> List.copyOf(m.values()))));
    }
}
