package com.peaknote.demo.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meetings")
public class Meeting {
    
    @Id
    @Column
    private String id;

    @ManyToOne
    @JoinColumn(name = "organizer_oid", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private String joinUrl; 

    private String subject;

    private Instant startTime;
    private Instant endTime;
    private String meetingType;

    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public User getOrganizer() {
        return organizer;
    }
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }
    public String getJoinUrl() {
        return joinUrl;
    }
    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
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
    public String getMeetingType() {
        return meetingType;
    }
    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }
    @Override
    public String toString() {
        return "Meeting [id=" + id + ", organizer=" + organizer + ", joinUrl=" + joinUrl + ", subject=" + subject
                + ", startTime=" + startTime + ", endTime=" + endTime + ", meetingType=" + meetingType + "]";
    }

}
