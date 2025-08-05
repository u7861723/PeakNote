package com.peaknote.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.peaknote.demo.entity.MeetingInstance;

public interface MeetingInstanceRepository extends JpaRepository<MeetingInstance, String> {
    boolean existsByCallRecordId(String eventId);
}

