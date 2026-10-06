package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * AS-IS saleson.shop.email.support.SendParam - 발송 요청.
 * 개별(E) 발송일 때만 화면이 {@code sendUserList[i].userName / .email}로 수신자를 함께 보낸다.
 */
@Getter
@Setter
public class SendParam {

    private long emailId;

    private List<EmailSendTarget> sendUserList = new ArrayList<>();
}
