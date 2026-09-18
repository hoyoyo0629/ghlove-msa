package com.ghlove.member.repository;

import com.ghlove.member.domain.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    /** 아이디/비밀번호 찾기 본인인증(휴대폰) - 같은 번호를 여러 회원이 등록했을 가능성을
     *  배제하지 않고 이름까지 맞춰봐야 하므로, userId 목록만 받아 서비스단에서 이름을 대조한다.
     *
     *  <p>저장된 번호에 하이픈이 있는 행과 없는 행이 섞여 있다(가입 화면은 하이픈 없이 저장하고,
     *  옮겨온 데이터는 010-0000-0000 형태다). 문자열 정확일치로 찾으면 이용자가 어느 형식으로
     *  입력하느냐에 따라 절반이 "일치하는 회원 정보를 찾을 수 없습니다"가 되므로, 양쪽 모두
     *  숫자만 남겨 비교한다. */
    @Query(value = "SELECT * FROM OP_USER_DETAIL WHERE REGEXP_REPLACE(PHONE_NUMBER, '[^0-9]', '', 'g') = :digits",
            nativeQuery = true)
    List<UserDetail> findByPhoneNumberDigits(@Param("digits") String digits);
}
