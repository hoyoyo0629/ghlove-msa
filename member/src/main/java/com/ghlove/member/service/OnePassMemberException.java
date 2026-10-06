package com.ghlove.member.service;

/**
 * 디지털원패스 연계회원이라 관리자가 탈퇴시킬 수 없을 때 - AS-IS 일반회원관리(메뉴 4101)의
 * 결과코드 {@code ERR_ONE_PASS}에 대응한다. 화면이 "이미 탈퇴된 회원입니다."와 다른 문구를
 * 띄워야 해서 일반 {@link MemberException}과 구분한다.
 */
public class OnePassMemberException extends MemberException {

    public OnePassMemberException(String message) {
        super(message);
    }
}
