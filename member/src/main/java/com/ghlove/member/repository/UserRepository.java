package com.ghlove.member.repository;

import com.ghlove.member.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    Optional<User> findByUserNameAndEmail(String userName, String email);

    Optional<User> findByLoginIdAndUserNameAndEmail(String loginId, String userName, String email);

    Optional<User> findByMberCi(String mberCi);

    List<User> findByStatusCodeAndLoginDateBefore(String statusCode, String cutoff);

    /** SFR-002 데이터 파기 대상 - 탈퇴일이 유예기간을 지났고, 아직 파기(익명화)되지 않은 회원.
     *  파기 여부는 userName이 파기 마커로 바뀌었는지로 판별한다(별도 플래그 컬럼 없이 재사용). */
    List<User> findByStatusCodeAndLeaveDateBeforeAndUserNameNot(String statusCode, String cutoff, String destroyedMarker);

    /** admin 회원관리(D2~D5 gap-fill) 검색용 - 가입일 범위. 세부 필터(아이디/이름/주소/구독구분/
     *  이메일수신동의)는 저트래픽 화면이라 OffgiveController와 동일하게 서비스단 인메모리 필터로 처리한다. */
    List<User> findByCreatedDateBetweenOrderByCreatedDateDesc(String from, String to);

    /** 탈퇴회원 조회(SecedeUser) - 탈퇴일 범위. */
    List<User> findByStatusCodeAndLeaveDateBetweenOrderByLeaveDateDesc(String statusCode, String from, String to);

    /** 휴면회원 조회(SleepUser) - 최종 로그인일 범위. */
    List<User> findByStatusCodeAndLoginDateBetweenOrderByLoginDateDesc(String statusCode, String from, String to);
}
