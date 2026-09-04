package com.ghlove.donation.service;

import com.ghlove.donation.domain.CntrReqmng;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.repository.CntrReqmngRepository;
import com.ghlove.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
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

    /** AS-IS list.jsp의 검색조건(지자체/요청분류/승인여부/기간)+페이지네이션 재현.
     *  startDate/endDate는 "yyyyMMdd" 형식이고, endDate는 자정 기준으로 다음날 0시 미만까지 포함한다. */
    public Page<CntrReqmng> search(String locgovCode, String cntrReqmngCode, String reqStatusCode,
                                    String startDate, String endDate, int page, int size) {
        LocalDateTime start = parseDateStart(startDate);
        LocalDateTime end = parseDateEndExclusive(endDate);
        return cntrReqmngRepository.search(blankToNull(locgovCode), blankToNull(cntrReqmngCode),
                blankToNull(reqStatusCode), start, end, PageRequest.of(page, size));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime FAR_FUTURE = LocalDateTime.of(2999, 12, 31, 23, 59);

    private static LocalDateTime parseDateStart(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            return EPOCH;
        }
        return LocalDate.parse(yyyyMMdd, java.time.format.DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
    }

    private static LocalDateTime parseDateEndExclusive(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            return FAR_FUTURE;
        }
        return LocalDate.parse(yyyyMMdd, java.time.format.DateTimeFormatter.BASIC_ISO_DATE).plusDays(1).atStartOfDay();
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
                              String taxSysCancelDe, String relatedDocDptNm, String relatedDocNum,
                              String relatedDocDe, Long managerId) {
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
        reqmng.setSttemntPayDe(truncate8(donation.getCntrDe()));
        reqmng.setCntrReqmngCode(cntrReqmngCode);
        reqmng.setDiscription(discription);
        reqmng.setTaxSysCancelDe(truncate8(taxSysCancelDe));
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

    private static void validate(String cntrReqmngCode, String discription, String taxSysCancelDe,
                                  String relatedDocDptNm, String relatedDocNum, String relatedDocDe) {
        if (!TYPE_CANCEL.equals(cntrReqmngCode) && !TYPE_POINT_CREATE.equals(cntrReqmngCode)) {
            throw new DonationException("요청분류를 선택해 주세요.");
        }
        if (discription == null || discription.trim().length() < 10) {
            throw new DonationException("사유는 10자 이상 작성해 주세요.");
        }
        if (TYPE_CANCEL.equals(cntrReqmngCode)) {
            if (taxSysCancelDe == null || taxSysCancelDe.isBlank()) {
                throw new DonationException("세외수입시스템 과오납 결의일자를 입력해 주세요.");
            }
            if (relatedDocDptNm == null || relatedDocDptNm.isBlank()) {
                throw new DonationException("관련문서 생산부서명을 입력해 주세요.");
            }
            if (relatedDocNum == null || relatedDocNum.isBlank()) {
                throw new DonationException("관련문서 문서번호를 입력해 주세요.");
            }
            if (relatedDocDe == null || relatedDocDe.isBlank()) {
                throw new DonationException("관련문서 시행일을 입력해 주세요.");
            }
        }
    }

    /** sttemnt_pay_de/tax_sys_cancel_de는 DB가 VARCHAR(8)이라 "yyyyMMdd"보다 길면 저장이
     *  실패한다 - 날짜 입력 위젯이 넘길 수 있는 부가 포맷("yyyy-MM-dd" 등)을 방어적으로 자른다. */
    private static String truncate8(String yyyyMMdd) {
        if (yyyyMMdd == null) {
            return null;
        }
        String digitsOnly = yyyyMMdd.replace("-", "");
        return digitsOnly.length() > 8 ? digitsOnly.substring(0, 8) : digitsOnly;
    }
}
