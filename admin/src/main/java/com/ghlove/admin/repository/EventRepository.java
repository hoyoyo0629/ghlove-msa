package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByUseYn(String useYn);
}
