package com.peaknote.demo.repository;

import com.peaknote.demo.entity.MeetingEvent;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingEventRepository extends JpaRepository<MeetingEvent, String> {

        /**
     * Query meeting events based on start time range and transcription status
     */
    List<MeetingEvent> findByStartTimeBetweenAndTranscriptStatus(Instant start, Instant end, String transcriptStatus);

    /**
     * Query single meeting event based on meetingId and transcription status
     */
    MeetingEvent findFirstByMeetingIdAndTranscriptStatus(String meetingId, String transcriptStatus);

    /**
     * Query based on eventId
     */
    MeetingEvent findByEventId(String eventId);

    

    @Query("SELECT m.eventId FROM MeetingEvent m WHERE m.joinUrl = :joinUrl")
    List<String> findEventIdsByjoinUrl(@Param("joinUrl") String joinUrl);

}