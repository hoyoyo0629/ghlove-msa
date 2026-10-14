package com.ghlove.admin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 국민비서 답례품 주문 문자(AS-IS procOffOrder) 회귀 테스트 - 발송내용 형식, 같은 (판매자, 답례품) 1회,
 * 제공자 문자의 이름 칸이 구매자 이름인지, 수신 미동의 건너뛰기를 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class PresentOrderSmsServiceTest {

    @Mock
    private SmsIpsService smsIpsService;
    @Mock
    private MemberAdminClient memberAdminClient;
    @Mock
    private GiftClient giftClient;

    @InjectMocks
    private PresentOrderSmsService presentOrderSmsService;

    @Test
    void formatsDateLikeAsIsReceiverInfo() {
        // 2023-02-16은 목요일(AS-IS SmsType 예시와 같은 날)
        assertThat(PresentOrderSmsService.localDateTimeToStr(LocalDateTime.of(2023, 2, 16, 13, 30)))
                .isEqualTo("2023.02.16.(목) 오후 01:30");
        assertThat(PresentOrderSmsService.localDateTimeToStr(LocalDateTime.of(2023, 2, 16, 9, 5)))
                .isEqualTo("2023.02.16.(목) 오전 09:05");
        assertThat(PresentOrderSmsService.localDateTimeToStr(LocalDateTime.of(2023, 2, 19, 12, 0)))
                .isEqualTo("2023.02.19.(일) 오후 12:00");
    }

    @Test
    void sendsBuyerAndDistinctSellerMessagesLikeAsIs() {
        when(memberAdminClient.smsReceiver(77L)).thenReturn(
                new MemberAdminClient.SmsReceiver(77L, "홍길동", "010-1234-5678", "0", "BUYER-CI"));
        when(giftClient.sellerSmsReceiver(9001L)).thenReturn(
                new GiftClient.SellerSmsReceiver("강화농협", "SELLER-CI", "010-9999-0000", "0"));
        when(giftClient.sellerSmsReceiver(9002L)).thenReturn(
                new GiftClient.SellerSmsReceiver("미동의업체", "SELLER2-CI", "010-8888-0000", "1"));

        presentOrderSmsService.send("K1000000001", 77L, List.of(
                        new PresentOrderSmsService.BuyItem(1000L, 9001L, "강화쌀", "무게 : 20kg"),
                        new PresentOrderSmsService.BuyItem(1000L, 9001L, "강화쌀", "무게 : 20kg"),
                        new PresentOrderSmsService.BuyItem(2000L, 9002L, "순무|김치", null)),
                LocalDateTime.of(2023, 2, 16, 13, 30));

        // 구매자: 첫 품목 + (옵션) + " 등", 전화번호 '-' 제거
        verify(smsIpsService).insertTifIpsSndngM(SmsType.PRESENT_ORDER, "BUYER-CI",
                "홍길동|2023.02.16.(목) 오후 01:30|K1000000001|강화쌀(무게 : 20kg) 등|01012345678");
        // 제공자: 같은 (판매자, 답례품)은 한 번, 이름 칸은 구매자 이름, " 등" 없음
        verify(smsIpsService).insertTifIpsSndngM(SmsType.PRESENT_ORDER, "SELLER-CI",
                "홍길동|2023.02.16.(목) 오후 01:30|K1000000001|강화쌀(무게 : 20kg)|01099990000");
        // 수신 미동의 제공자는 보내지 않는다
        verify(smsIpsService, never()).insertTifIpsSndngM(eq(SmsType.PRESENT_ORDER), eq("SELLER2-CI"), anyString());
    }

    @Test
    void sellerStillReceivesWhenBuyerLookupFails() {
        when(memberAdminClient.smsReceiver(77L)).thenReturn(null);
        when(giftClient.sellerSmsReceiver(9001L)).thenReturn(
                new GiftClient.SellerSmsReceiver("강화농협", "SELLER-CI", "01099990000", "0"));

        presentOrderSmsService.send("K1000000002", 77L, List.of(
                new PresentOrderSmsService.BuyItem(1000L, 9001L, "강화쌀", null)), LocalDateTime.of(2023, 2, 16, 9, 5));

        // AS-IS: 구매자 조회 NPE를 잡고 이름은 빈 문자열인 채로 제공자 문자를 보낸다
        verify(smsIpsService).insertTifIpsSndngM(SmsType.PRESENT_ORDER, "SELLER-CI",
                "|2023.02.16.(목) 오전 09:05|K1000000002|강화쌀|01099990000");
    }
}
