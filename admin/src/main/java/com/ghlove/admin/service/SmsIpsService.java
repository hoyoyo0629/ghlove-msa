package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.CommonCodeId;
import com.ghlove.admin.domain.IpsSendingMaster;
import com.ghlove.admin.repository.CommonCodeRepository;
import com.ghlove.admin.repository.IpsSendingMasterRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 국민비서(IPS) 문자발송 - AS-IS {@code saleson.common.sms.SmsIpsServiceImpl}의
 * {@code giveSendSms} / {@code insertTifIpsSndngM} / {@code initTifIpsSndngM} 이식.
 *
 * <p><b>이 서비스는 문자를 직접 보내지 않는다</b>. {@code TIF_IPS_SNDNG_M}에 한 행 적재하면
 * 행정망 ESB가 집어가 국민비서로 발송한다(그래서 AS-IS도 "발송"이 INSERT 한 번이다).
 * 적재한 행은 <b>문자전송이력(7208)</b> 화면이 그대로 읽는다
 * ({@link com.ghlove.admin.web.SendLogAdminController}).
 *
 * <p><b>AS-IS 규칙 그대로</b>:
 * <ul>
 *   <li>수신동의({@code RECEIVE_SMS})가 <b>'0'인 회원만</b> 보낸다 - 그 외는 전송하지 않고
 *       "SMS 수신 미동의로 전송 실패"를 로그에 남긴다(AS-IS도 {@code log.error}다).</li>
 *   <li>수신자 식별값은 <b>회원 CI</b>({@code PRVC_IDNTFC_INFO = MBER_CI})다. CI가 없으면
 *       {@code ReceiverInfo}가 만들어지지 않아 전송하지 않는다.</li>
 *   <li>발송내용은 문자종류별 파이프 구분 문자열이다. Q&A·회원가입·탈퇴·비밀번호변경·관리자로그인은
 *       {@code 이름|수신전화번호} 두 칸이다({@code GiveUserSmsInfo.getReceiverInfo}).</li>
 *   <li>공통값: {@code ESB_STATUS_CD='N'}(미처리), {@code ESB_WORK_GBN='I'},
 *       {@code SVC_GRP_ID}/{@code PRVC_IDNTFC_SE_CD}/{@code ESB_IF_ID}는 설정값,
 *       {@code SVC_ID}는 문자종류 코드({@link SmsType#getCode()}).</li>
 *   <li>{@code LIST_SN}과 {@code INSTT_CRT_SN}에 <b>같은 채번값</b>을 넣는다(AS-IS도 동일).</li>
 *   <li>발송내용이나 수신자 식별값이 비면 그 건은 건너뛴다.</li>
 * </ul>
 *
 * <p><b>개발환경 가드</b>: {@code ghlove.integrations.sms-ips.enabled=false}면 적재를 건너뛴다.
 * 적재하면 실제 문자가 나갈 수 있어서다 - AS-IS 소스에도 같은 이유의 로컬 가드가 들어가 있다.
 */
@Service
@Slf4j
public class SmsIpsService {

    /** AS-IS 수신동의 값 - 이 값일 때만 보낸다. */
    private static final String RECEIVE_SMS_AGREED = "0";

    /** AS-IS 공통코드 DONATION_LIMIT_AMT - 연도별 한도금액 DETAIL(예: "2천만"). */
    private static final String DONATION_LIMIT_AMT_CODE_TYPE = "DONATION_LIMIT_AMT";

    private final IpsSendingMasterRepository ipsSendingMasterRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final EntityManager entityManager;
    private final boolean enabled;
    private final String svcGrpId;
    private final String prvcIdntfcSeCd;
    private final String esbIfId;

    public SmsIpsService(IpsSendingMasterRepository ipsSendingMasterRepository,
                         CommonCodeRepository commonCodeRepository,
                         EntityManager entityManager,
                         @Value("${ghlove.integrations.sms-ips.enabled:false}") boolean enabled,
                         @Value("${ghlove.integrations.sms-ips.svc-grp-id:}") String svcGrpId,
                         @Value("${ghlove.integrations.sms-ips.prvc-idntfc-se-cd:}") String prvcIdntfcSeCd,
                         @Value("${ghlove.integrations.sms-ips.esb-if-id:}") String esbIfId) {
        this.ipsSendingMasterRepository = ipsSendingMasterRepository;
        this.commonCodeRepository = commonCodeRepository;
        this.entityManager = entityManager;
        this.enabled = enabled;
        this.svcGrpId = svcGrpId;
        this.prvcIdntfcSeCd = prvcIdntfcSeCd;
        this.esbIfId = esbIfId;
    }

    /**
     * AS-IS {@code SmsIpsServiceImpl.getSmsSendList}가 각 행에 채워주는
     * {@code donationVerification.donationLimitAmt().getDetail()} - 공통코드
     * {@code DONATION_LIMIT_AMT}의 올해 DETAIL. 목록의 "발송내용" 렌더링에 쓰인다.
     */
    @Transactional(readOnly = true)
    public String donationLimitAmtDetail() {
        String year = String.valueOf(LocalDate.now().getYear());
        return commonCodeRepository.findById(new CommonCodeId(DONATION_LIMIT_AMT_CODE_TYPE, "ko", year))
                .map(CommonCode::getDetail)
                .orElse("");
    }

    /**
     * AS-IS {@code sms-mapper.getSmsSendList} - 문자 구분(SVC_ID)·검색내용(SNDNG_CNTNTS)·
     * 생성일 범위로 걸러 최신순(AS-IS는 INSTT_CRT_SN DESC, TO-BE는 같은 값이 들어가는 LIST_SN
     * DESC)으로 돌려준다.
     *
     * <p>검색내용은 {@code SNDNG_CNTNTS}만 본다 - AS-IS 매퍼에 {@code PRVC_IDNTFC_INFO}(CI)
     * 조건은 없다(화면 안내문구 "이름, 전화번호 등"은 발송내용 안에 그 값들이 파이프로 들어가
     * 있어서다). 생성일 범위는 화면 라벨이 "생성일자"지만 실제로는 {@code INFO_CRT_DT}가 아니라
     * {@code ESB_INIT_TIME}(연계발생일시, yyyyMMddHHmmss 문자열)을
     * {@code BETWEEN CONCAT(start,'000000') AND CONCAT(end,'999999')}로 비교한다 - AS-IS
     * 그대로다.
     *
     * <p>조건절을 Java에서 조립한다 - {@code (:x is null or ...)} 형태로 JPQL에 그대로 두면
     * null 파라미터의 타입을 PostgreSQL이 못 정해서 500이 난다
     * (String은 {@code cast(:x as String)}으로 고쳐지지만 LocalDateTime/Long은 cast로도
     * 안 고쳐진다 - [[hql-null-param-needs-cast]], [[admin-excel-download-log-500-fix]] 참고).
     */
    @Transactional(readOnly = true)
    public List<IpsSendingMaster> search(String svcId, String query, String searchStartDate, String searchEndDate) {
        StringBuilder jpql = new StringBuilder("select m from IpsSendingMaster m where 1 = 1");
        if (svcId != null) {
            jpql.append(" and m.svcId = :svcId");
        }
        if (query != null) {
            jpql.append(" and m.sndngCntnts like concat('%', :query, '%')");
        }
        if (searchStartDate != null) {
            jpql.append(" and m.esbInitTime between concat(:startDate, '000000') and concat(:endDate, '999999')");
        }
        jpql.append(" order by m.listSn desc");

        var typedQuery = entityManager.createQuery(jpql.toString(), IpsSendingMaster.class);
        if (svcId != null) {
            typedQuery.setParameter("svcId", svcId);
        }
        if (query != null) {
            typedQuery.setParameter("query", query);
        }
        if (searchStartDate != null) {
            typedQuery.setParameter("startDate", searchStartDate);
            typedQuery.setParameter("endDate", searchEndDate != null ? searchEndDate : searchStartDate);
        }
        return typedQuery.getResultList();
    }

    /**
     * AS-IS {@code giveSendSms(List<GiveUserSmsInfo>, SmsType)} - 수신동의·CI를 확인해 적재한다.
     *
     * <p>AS-IS는 수신자가 없으면({@code getQnaUserInfo}가 null) {@code Arrays.asList(null)}을
     * 넘겨 <b>NPE로 500</b>이 난다(비회원·탈퇴회원 문의에 답변하면 답변은 저장된 뒤 화면이 깨진다).
     * 여기서는 null을 그냥 건너뛴다 - 정상 데이터의 결과는 완전히 같다.
     */
    @Transactional
    public void send(SmsType type, MemberAdminClient.SmsReceiver receiver) {
        if (receiver == null) {
            log.warn("{} SMS 전송 대상 없음 - 회원정보를 찾지 못했다(AS-IS는 이 경우 NPE)", type.getTitle());
            return;
        }
        if (!RECEIVE_SMS_AGREED.equals(receiver.receiveSms())) {
            log.error("사용자 ID : {} SMS 수신 미동의로 전송 실패", receiver.userId());
            return;
        }
        String contents = contentsOf(type, receiver);
        if (receiver.mberCi() == null || receiver.mberCi().isBlank() || contents == null) {
            log.error("사용자 ID : {} {} SMS 전송 실패", receiver.userId(), type.getTitle());
            return;
        }
        if (!enabled) {
            log.warn("[sms-ips disabled] 국민비서 적재 건너뜀: svcId={}, userId={}",
                    type.getCode(), receiver.userId());
            return;
        }

        IpsSendingMaster row = new IpsSendingMaster();
        // AS-IS initTifIpsSndngM - 공통값
        row.setInfoCrtDt(LocalDateTime.now().withNano(0));
        row.setSvcGrpId(svcGrpId);
        row.setPrvcIdntfcSeCd(prvcIdntfcSeCd);
        row.setEsbStatusCd("N");
        row.setEsbWorkGbn("I");
        row.setEsbIfId(esbIfId);
        // AS-IS insertTifIpsSndngM - 건별값. LIST_SN과 INSTT_CRT_SN에 같은 채번값을 넣는다.
        long sn = ipsSendingMasterRepository.nextListSn();
        row.setListSn(sn);
        row.setInsttCrtSn(sn);
        row.setPrvcIdntfcInfo(receiver.mberCi());
        row.setSndngCntnts(contents);
        row.setSvcId(type.getCode());
        ipsSendingMasterRepository.save(row);
    }

    /**
     * AS-IS {@code GiveUserSmsInfo.getReceiverInfo} - 문자종류별 발송내용(구분자 {@code |}).
     * 지금 쓰는 종류만 옮겼다 - 기부·과오납·명예기부는 지자체명·금액이 더 붙고, 그 발송은
     * donation 쪽 기능이라 그 라운드에서 이식한다.
     */
    private static String contentsOf(SmsType type, MemberAdminClient.SmsReceiver receiver) {
        if (receiver.phoneNumber() == null || receiver.phoneNumber().isBlank()) {
            return null;
        }
        return switch (type) {
            case QNA, JOIN_MEMBERSHIP, SECESSION, PASSWORD_CHANGE_COMPLETE, MANAGER_LOGIN ->
                    receiver.userName() + "|" + receiver.phoneNumber();
            default -> null;
        };
    }
}
