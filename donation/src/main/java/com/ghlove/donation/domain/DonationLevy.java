package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 세외수입 부과/수납 + 국세청 홈택스 전자기부금영수증 등록 추적 (기부 1건당 1행).
 * AS-IS NgDonationRelayServiceImpl의 실제 연계 흐름을 재현하되, 방화벽이 열리기
 * 전까지는 LocalTaxClient/NtsClient가 모크 응답으로 채운다.
 */
@Entity
@Table(name = "DONATION_LEVY")
@Getter
@Setter
@NoArgsConstructor
public class DonationLevy {

    @Id
    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "BUGA_NO")
    private String bugaNo;

    @Column(name = "BUGA_DATE")
    private LocalDateTime bugaDate;

    @Column(name = "SUNAP_YN")
    private String sunapYn;

    @Column(name = "SUNAP_DATE")
    private LocalDateTime sunapDate;

    @Column(name = "NTS_STATUS")
    private String ntsStatus;

    @Column(name = "NTS_RECEIPT_NO")
    private String ntsReceiptNo;

    @Column(name = "NTS_REGISTERED_DATE")
    private LocalDateTime ntsRegisteredDate;

    @Column(name = "NTS_ERROR_MESSAGE")
    private String ntsErrorMessage;
}
