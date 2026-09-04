package com.ghlove.gift.repository;

import com.ghlove.gift.domain.StyleBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StyleBookRepository extends JpaRepository<StyleBook, Long> {
    List<StyleBook> findAllByOrderByOrderingAsc();
}
