package com.ghlove.member.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** AS-IS join.html 3단계(회원정보입력)의 아이디/비밀번호 형식 규칙(checkId/checkPwd)과 동일하게 맞춘다. */
@Getter
@Setter
public class SignupForm {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Pattern(regexp = "^[a-z0-9_]{6,20}$", message = "아이디는 영문 소문자/숫자/밑줄(_)만 사용해 6~20자로 입력해 주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 9, max = 20, message = "비밀번호는 9~20자로 입력해 주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해 주세요.")
    private String userName;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String phoneNumber;

    /** yyyyMMdd. AS-IS는 본인인증 결과로 자동 채워지는 읽기전용 필드지만, 이 프로젝트는
     * 외부 인증 연계가 없어 직접 입력받는다 (SSR/no-gateway 상 의도적인 차이). */
    private String birthday;

    private String address;
    private String addressDetail;
}
