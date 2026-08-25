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
}
