package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpEmailFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** AS-IS emailMapper.getEmailFileList / insertEmailFile / getEmailFile 대응. */
public interface OpEmailFileRepository extends JpaRepository<OpEmailFile, Integer> {

    List<OpEmailFile> findByEmailIdOrderByOrdering(Long emailId);

    /** AS-IS INSERT의 {@code (SELECT NVL(MAX("ORDERING"),1) ...)} - MAX+1이 아니라 MAX 그대로다. */
    @Query("select coalesce(max(f.ordering), 1) from OpEmailFile f where f.emailId = :emailId")
    Integer nextOrdering(@Param("emailId") Long emailId);
}
