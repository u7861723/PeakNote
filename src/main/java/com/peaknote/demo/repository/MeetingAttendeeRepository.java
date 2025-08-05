package com.peaknote.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.peaknote.demo.entity.MeetingAttendee;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee,UUID> {

    List<MeetingAttendee> findByMeetingEvent_EventId(String eventId);
    
} 