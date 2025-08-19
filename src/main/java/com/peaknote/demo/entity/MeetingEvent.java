package com.peaknote.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "meeting_event")
public class MeetingEvent {

    @Id
    private String eventId;

    @Column(name = "series_master_id")
    private String seriesMasterId;

    private String userId;
    private String subject;
    private Instant startTime;
    private Instant endTime;
    private String joinUrl;
    private String meetingId;
    private Instant lastNotifiedAt;
    @Column(name = "transcript_status")
    private String transcriptStatus = "none"; // Default none

    @OneToMany(mappedBy = "meetingEvent")
    private List<MeetingAttendee> attendees;


    
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public Instant getStartTime() {
        return startTime;
    }
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    public Instant getEndTime() {
        return endTime;
    }
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    public String getJoinUrl() {
        return joinUrl;
    }
    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }
    public Instant getLastNotifiedAt() {
        return lastNotifiedAt;
    }
    public void setLastNotifiedAt(Instant lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }
    public String getSeriesMasterId() {
        return seriesMasterId;
    }
    public void setSeriesMasterId(String seriesMasterId) {
        this.seriesMasterId = seriesMasterId;
    }
    public String getMeetingId() {
        return meetingId;
    }
    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }
    public String getTranscriptStatus() {
        return transcriptStatus;
    }
    public void setTranscriptStatus(String transcriptStatus) {
        this.transcriptStatus = transcriptStatus;
    }
    public List<MeetingAttendee> getAttendees() {
        return attendees;
    }
    public void setAttendees(List<MeetingAttendee> attendees) {
        this.attendees = attendees;
    }

}
