package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Popup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopupRepository extends JpaRepository<Popup, Integer> {
    List<Popup> findAllByOrderByPopupIdDesc();

    List<Popup> findByUseYnOrderByPopupIdDesc(String useYn);
}
