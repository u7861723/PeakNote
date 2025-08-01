package com.peaknote.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.peaknote.demo.entity.MeetingTranscript;

import jakarta.transaction.Transactional;

@Repository
public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, UUID> {

    MeetingTranscript findByMeetingEvent_EventId(String eventId);


    @Modifying
    @Transactional
    @Query("UPDATE MeetingTranscript m SET m.contentText = :contentText WHERE m.meetingEvent.eventId = :eventId")
    void updateContentByEventId(@Param("eventId") String eventId, @Param("contentText") String contentText);
}
