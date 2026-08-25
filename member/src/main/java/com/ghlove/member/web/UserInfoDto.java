package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDetail;

/**
 * address는 donation 서비스의 기부하기 "거주지 확인"용 - AS-IS는 행정정보공동이용센터에
 * 주민등록번호로 조회해 실주소를 받아오지만, 그 연계는 이 MSA에 없어(다른 외부연계와
 * 동일한 의도적 축소) 회원이 등록해 둔 주소를 거주지로 쓴다. loginId는 특정사업 상세화면
 * "응원메시지(기부내역)" 탭의 이름/ID/날짜 표시용(AS-IS cntrList의 loginId 필드).
 */
public record UserInfoDto(Long userId, String userName, String birthday, String address, String loginId,
                           String phoneNumber, String email) {
    public static UserInfoDto of(User user, UserDetail detail) {
        return new UserInfoDto(user.getUserId(), user.getUserName(),
                detail != null ? detail.getBirthday() : null,
                detail != null ? detail.getAddress() : null,
                user.getLoginId(),
                detail != null ? detail.getPhoneNumber() : null,
                user.getEmail());
    }
}
