package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AS-IS saleson.shop.email.domain.EmailSend - 이메일 발송 수신자 한 명.
 * 발송대상 조회(권한별/답례품), 개별발송 폼이 넘기는 수신자, EMS 리포트의 발송인원 목록이
 * 모두 이 모양을 쓴다(AS-IS 동일).
 */
@Getter
@Setter
@NoArgsConstructor
public class EmailSendTarget {

    private String email;

    private String userName;

    /** EMS 리포트 결과코드 - C:성공 F:실패. */
    private String resultCode;

    /** EMS 실발송 시각 yyyyMMddHHmmss. */
    private String processedDate;

    public EmailSendTarget(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    /** AS-IS EmailSend.getResultNm() 그대로. */
    public String getResultNm() {
        if (resultCode == null || resultCode.isEmpty()) {
            return null;
        }
        if ("C".equals(resultCode)) {
            return "성공";
        }
        if ("F".equals(resultCode)) {
            return "실패";
        }
        return resultCode;
    }
}
