package com.ghlove.member.repository;

import com.ghlove.member.domain.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    /** 아이디/비밀번호 찾기 본인인증(휴대폰) - 같은 번호를 여러 회원이 등록했을 가능성을
     *  배제하지 않고 이름까지 맞춰봐야 하므로, userId 목록만 받아 서비스단에서 이름을 대조한다. */
    List<UserDetail> findByPhoneNumber(String phoneNumber);
}
