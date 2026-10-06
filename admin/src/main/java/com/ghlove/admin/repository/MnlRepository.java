package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Mnl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 사용자매뉴얼 ({@code G_MNL}) - 메뉴 5202. 목록 조회는 {@link MnlAdminRepository}(AS-IS SQL)다. */
public interface MnlRepository extends JpaRepository<Mnl, Integer> {

    /** AS-IS getManualByMenuSeCode - 같은 화면에 두 번 등록하는 것을 막는 중복 확인용. */
    Optional<Mnl> findFirstByMenuSeCode(String menuSeCode);
}
