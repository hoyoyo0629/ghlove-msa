package com.ghlove.donation.service;

import com.ghlove.donation.domain.CntrReqmng;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.repository.CntrReqmngRepository;
import com.ghlove.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기부금 변경신청 관리 (AS-IS opmanager/give/give-reqmng, GiveStateManagerController의
 * reqmng-list/req_form/reqReg/approveReq/cancelReq). 실제 도메인효과(기부취소=
 * DonationService.cancelDonation, 포인트생성=PointClient.creditForDonation)는 이미 있는
 * 기능을 그대로 재사용한다 - 승인 시점에만 트리거되는 새 워크플로 계층일 뿐이다.
 *
 * AS-IS 원본 Java는 신청(reqReg)에서 등록과 승인을 같은 요청 안에서 연달아 처리하지만,
 * 화면(list.jsp/list_locgov.jsp)은 REQ_STATUS_CODE=100(요청/대기) 상태에 대해 별도의
 * "요청승인"/"요청취소" 버튼을 보여준다 - DDL 주석(100:요청 200:취소 999:승인)과도 맞는
 * 정상적인 신청→승인 2단계 흐름으로 재현한다.
 */
@Service
@RequiredArgsConstructor
public class CntrReqmngService {

    private static final String STATUS_REQUESTED = "100";
    private static final String STATUS_CANCELLED = "200";
    private static final String STATUS_APPROVED = "999";
    private static final String TYPE_CANCEL = "100";
    private static final String TYPE_POINT_CREATE = "200";

    private final CntrReqmngRepository cntrReqmngRepository;
    private final DonationRepository donationRepository;
    private final DonationService donationService;
    private final PointClient pointClient;
    private final MemberClient memberClient;

    public List<CntrReqmng> listAll() {
        return cntrReqmngRepository.findAllByOrderByFrstRegistPnttmDesc();
    }

    public List<CntrReqmng> listByLocgov(String locgovCode) {
        return cntrReqmngRepository.findByLocgovCodeOrderByFrstRegistPnttmDesc(locgovCode);
    }

    public Donation donationOf(String cntrSn) {
        return donationRepository.findById(cntrSn)
                .orElseThrow(() -> new DonationException("기부내역을 찾을 수 없습니다."));
    }

    /** 포인트 사용이력 존재 여부 - AS-IS getCntrUsePointCheck()와 동일한 목적. 원 적립분보다
     *  남은 잔액(remainingAmount)이 적으면 이미 일부/전부 사용된 것이다. */
    public boolean hasUsedPoints(String cntrSn) {
        PointClient.EarnLot lot = pointClient.earnLotOf(cntrSn);
        return lot != null && lot.remainingAmount() != null && lot.earnedAmount() != null
                && lot.remainingAmount() < lot.earnedAmount();
    }

    @Transactional
    public CntrReqmng submit(String cntrSn, String cntrReqmngCode, String discription,
                              LocalDateTime taxSysCancelDe, String relatedDocDptNm, String relatedDocNum,
                              LocalDateTime relatedDocDe, Long managerId) {
        Donation donation = donationOf(cntrSn);
        validate(cntrReqmngCode, discription, taxSysCancelDe, relatedDocDptNm, relatedDocNum, relatedDocDe);
        if (hasUsedPoints(cntrSn)) {
            throw new DonationException("기부금 사용이력이 존재해 변경 신청을 할 수 없습니다.");
        }

        MemberInfo member = memberClient.fetchOrNull(donation.getUserId());

        CntrReqmng reqmng = new CntrReqmng();
        reqmng.setCntrSn(cntrSn);
        reqmng.setLocgovCode(donation.getCntrLocgovCode());
        reqmng.setCntrAmt(donation.getCntrAmt());
        reqmng.setLoginId(member != null ? member.loginId() : null);
        reqmng.setUserName(member != null ? member.userName() : null);
        reqmng.setSttemntPayDe(parseDate(donation.getCntrDe()));
        reqmng.setCntrReqmngCode(cntrReqmngCode);
        reqmng.setDiscription(discription);
        reqmng.setTaxSysCancelDe(taxSysCancelDe);
        reqmng.setRelatedDocDptNm(relatedDocDptNm);
        reqmng.setRelatedDocNum(relatedDocNum);
        reqmng.setRelatedDocDe(relatedDocDe);
        reqmng.setFrstRegisterId(managerId);
        reqmng.setFrstRegistPnttm(LocalDateTime.now());
        reqmng.setReqStatusCode(STATUS_REQUESTED);
        return cntrReqmngRepository.save(reqmng);
    }

    @Transactional
    public CntrReqmng approve(Long reqId, Long managerId) {
        CntrReqmng reqmng = findOrThrow(reqId);
        if (!STATUS_REQUESTED.equals(reqmng.getReqStatusCode())) {
            throw new DonationException("대기 중인 요청만 승인할 수 있습니다.");
        }
        if (hasUsedPoints(reqmng.getCntrSn())) {
            throw new DonationException("기부금 사용이력이 존재해 승인을 할 수 없습니다.");
        }

        if (TYPE_CANCEL.equals(reqmng.getCntrReqmngCode())) {
            donationService.cancelDonation(reqmng.getCntrSn());
        } else if (TYPE_POINT_CREATE.equals(reqmng.getCntrReqmngCode())) {
            Donation donation = donationOf(reqmng.getCntrSn());
            try {
                pointClient.creditForDonation(donation.getCntrSn(), donation.getUserId(),
                        donation.getCntrLocgovCode(), donation.getCntrAmt(), donation.getCntrDe());
            } catch (RestClientException e) {
                throw new DonationException("포인트 생성 처리 중 오류가 발생했습니다.");
            }
        }

        reqmng.setReqStatusCode(STATUS_APPROVED);
        reqmng.setApprDt(LocalDateTime.now());
        reqmng.setLastUpdusrId(managerId);
        reqmng.setLastUpdtPnttm(LocalDateTime.now());
        return cntrReqmngRepository.save(reqmng);
    }

    @Transactional
    public CntrReqmng cancel(Long reqId, Long managerId) {
        CntrReqmng reqmng = findOrThrow(reqId);
        if (!STATUS_REQUESTED.equals(reqmng.getReqStatusCode())) {
            throw new DonationException("대기 중인 요청만 취소할 수 있습니다.");
        }
        reqmng.setReqStatusCode(STATUS_CANCELLED);
        reqmng.setCancleDt(LocalDateTime.now());
        reqmng.setLastUpdusrId(managerId);
        reqmng.setLastUpdtPnttm(LocalDateTime.now());
        return cntrReqmngRepository.save(reqmng);
    }

    private CntrReqmng findOrThrow(Long reqId) {
        return cntrReqmngRepository.findById(reqId)
                .orElseThrow(() -> new DonationException("변경신청을 찾을 수 없습니다."));
    }

    private static void validate(String cntrReqmngCode, String discription, LocalDateTime taxSysCancelDe,
                                  String relatedDocDptNm, String relatedDocNum, LocalDateTime relatedDocDe) {
        if (!TYPE_CANCEL.equals(cntrReqmngCode) && !TYPE_POINT_CREATE.equals(cntrReqmngCode)) {
            throw new DonationException("요청분류를 선택해 주세요.");
        }
        if (discription == null || discription.trim().length() < 10) {
            throw new DonationException("사유는 10자 이상 작성해 주세요.");
        }
        if (TYPE_CANCEL.equals(cntrReqmngCode)) {
            if (taxSysCancelDe == null) {
                throw new DonationException("세외수입시스템 과오납 결의일자를 입력해 주세요.");
            }
            if (relatedDocDptNm == null || relatedDocDptNm.isBlank()) {
                throw new DonationException("관련문서 생산부서명을 입력해 주세요.");
            }
            if (relatedDocNum == null || relatedDocNum.isBlank()) {
                throw new DonationException("관련문서 문서번호를 입력해 주세요.");
            }
            if (relatedDocDe == null) {
                throw new DonationException("관련문서 시행일을 입력해 주세요.");
            }
        }
    }

    private static LocalDateTime parseDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() < 8) {
            return null;
        }
        return java.time.LocalDate.parse(yyyyMMdd.substring(0, 8),
                java.time.format.DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
    }
}
