package com.peaknote.demo.repository;

import com.peaknote.demo.entity.MeetingEvent;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingEventRepository extends JpaRepository<MeetingEvent, String> {

        /**
     * 根据开始时间区间和转录状态查询会议事件
     */
    List<MeetingEvent> findByStartTimeBetweenAndTranscriptStatus(Instant start, Instant end, String transcriptStatus);

    /**
     * 根据 meetingId 和转录状态查询单个会议事件
     */
    MeetingEvent findFirstByMeetingIdAndTranscriptStatus(String meetingId, String transcriptStatus);

    /**
     * 根据 eventId 查询
     */
    MeetingEvent findByEventId(String eventId);

    

    @Query("SELECT m.eventId FROM MeetingEvent m WHERE m.joinUrl = :joinUrl")
    List<String> findEventIdsByjoinUrl(@Param("joinUrl") String joinUrl);

}