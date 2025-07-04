package com.peaknote.demo.repository;

import com.peaknote.demo.entity.MeetingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingEventRepository extends JpaRepository<MeetingEvent, String> {
}