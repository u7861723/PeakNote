package com.peaknote.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meeting_transcript")
public class MeetingTranscript {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "meeting_event_event_id")
    private MeetingEvent meetingEvent;

    @Lob
    private String contentText;

    private String downloadUrl;
    private Instant createdAt;
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getContentText() {
        return contentText;
    }
    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
    public String getDownloadUrl() {
        return downloadUrl;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public MeetingEvent getMeetingEvent() {
        return meetingEvent;
    }
    public void setMeetingEvent(MeetingEvent meetingEvent) {
        this.meetingEvent = meetingEvent;
    }
    @Override
    public String toString() {
        return "MeetingTranscript [id=" + id + ", meetingEvent=" + meetingEvent + ", contentText=" + contentText
                + ", downloadUrl=" + downloadUrl + ", createdAt=" + createdAt + "]";
    }
    
}