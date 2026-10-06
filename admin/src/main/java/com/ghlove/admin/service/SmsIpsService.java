package com.ghlove.admin.service;

import com.ghlove.admin.domain.IpsSendingMaster;
import com.ghlove.admin.repository.IpsSendingMasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    private final IpsSendingMasterRepository ipsSendingMasterRepository;
    private final boolean enabled;
    private final String svcGrpId;
    private final String prvcIdntfcSeCd;
    private final String esbIfId;

    public SmsIpsService(IpsSendingMasterRepository ipsSendingMasterRepository,
                         @Value("${ghlove.integrations.sms-ips.enabled:false}") boolean enabled,
                         @Value("${ghlove.integrations.sms-ips.svc-grp-id:}") String svcGrpId,
                         @Value("${ghlove.integrations.sms-ips.prvc-idntfc-se-cd:}") String prvcIdntfcSeCd,
                         @Value("${ghlove.integrations.sms-ips.esb-if-id:}") String esbIfId) {
        this.ipsSendingMasterRepository = ipsSendingMasterRepository;
        this.enabled = enabled;
        this.svcGrpId = svcGrpId;
        this.prvcIdntfcSeCd = prvcIdntfcSeCd;
        this.esbIfId = esbIfId;
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
