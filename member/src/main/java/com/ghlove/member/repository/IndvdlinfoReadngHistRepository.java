package com.ghlove.member.repository;

import com.ghlove.member.domain.IndvdlinfoReadngHist;
import org.springframework.data.jpa.repository.JpaRepository;

/** 개인정보 열람 이력 (AS-IS G_INDVDLINFO_READNG_HIST) - 일반회원관리 4101의 "개인정보 열람"이 쌓는다. */
public interface IndvdlinfoReadngHistRepository extends JpaRepository<IndvdlinfoReadngHist, Long> {
}
