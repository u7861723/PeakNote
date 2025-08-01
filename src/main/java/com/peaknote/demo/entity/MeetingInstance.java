package com.peaknote.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "meeting_instance")
public class MeetingInstance {
    
    @Id
    @Column(name = "event_id")
    private String eventId;  // Microsoft Graph 中每个实例的唯一标识

    private String seriesMasterId;  // 用于标识属于哪个重复会议系列

    public String getSeriesMasterId() {
        return seriesMasterId;
    }

    public void setSeriesMasterId(String seriesMasterId) {
        this.seriesMasterId = seriesMasterId;
    }

    @Column(nullable = false)
    private String joinUrl;

    private String callRecordId;

    private Instant startTime;
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private TranscriptStatus transcriptStatus;

    @Column(nullable = false)
    private String createdBy;

    private Instant createdAt;

public enum TranscriptStatus {
    NONE,           // 未处理
    PROCESSING,     // 提交转录请求后
    AVAILABLE,      // 成功获取转录
    FAILED          // 获取转录失败
}

    public String geteventId() {
        return eventId;
    }

    public void seteventId(String id) {
        this.eventId = id;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public String getCallRecordId() {
        return callRecordId;
    }

    public void setCallRecordId(String callRecordId) {
        this.callRecordId = callRecordId;
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

    public TranscriptStatus getTranscriptStatus() {
        return transcriptStatus;
    }

    public void setTranscriptStatus(TranscriptStatus transcriptStatus) {
        this.transcriptStatus = transcriptStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MeetingInstance [id=" + eventId + ", joinUrl=" + joinUrl + ", callRecordId=" + callRecordId + ", startTime="
                + startTime + ", endTime=" + endTime + ", transcriptStatus=" + transcriptStatus + ", createdBy="
                + createdBy + ", createdAt=" + createdAt + "]";
    }
}