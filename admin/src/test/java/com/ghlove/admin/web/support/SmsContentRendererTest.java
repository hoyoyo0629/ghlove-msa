package com.ghlove.admin.web.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsContentRendererTest {

    private final SmsContentRenderer renderer = new SmsContentRenderer();

    @Test
    void rendersPresentShippingTemplateFromPipeDelimitedContent() {
        // AS-IS 실데이터 샘플(마이그레이션된 TIF_IPS_SNDNG_M 11161번 행)과 같은 입력.
        String rendered = renderer.render("812-A005",
                "정철교|2023.06.23.(금) 오후 03:52|K0000006106|산채냉면 혼합세트|롯데택배|01090293686", "2천만");

        assertThat(rendered).contains("[고향사랑e음 답례품 배송 알림]")
                .contains("- [정철교]님이 주문하신 답례품이 발송되었습니다.")
                .contains("신청일시 : 2023.06.23.(금) 오후 03:52")
                .contains("주문번호 : K0000006106")
                .contains("상품명 : 산채냉면 혼합세트")
                .contains("배송사 : 롯데택배");
    }

    @Test
    void rendersLimitAmtIntoJoinMembershipTemplate() {
        String rendered = renderer.render("812-A001", "홍길동|01012345678", "2천만");

        assertThat(rendered).contains("[고향사랑e음 회원가입 안내]")
                .contains("[홍길동]님 회원가입을 진심으로 축하드립니다")
                .contains("1인당 연간 최대 2천만원까지 가능");
    }

    @Test
    void unknownSvcIdReturnsEmpty() {
        assertThat(renderer.render("UNKNOWN", "a|b", "2천만")).isEmpty();
    }

    @Test
    void blankContentReturnsEmpty() {
        assertThat(renderer.render("812-A001", "", "2천만")).isEmpty();
        assertThat(renderer.render("812-A001", null, "2천만")).isEmpty();
    }

    @Test
    void malformedContentMissingPipeSegmentsReturnsEmptyInsteadOfThrowing() {
        // PRESENT_SHIPPING은 5개 파이프 항목(p[0]~p[4])을 쓰는데 1개만 준다.
        assertThat(renderer.render("812-A005", "이름만있음", "2천만")).isEmpty();
    }
}
