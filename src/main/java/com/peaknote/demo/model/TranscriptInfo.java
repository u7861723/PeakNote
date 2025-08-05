package com.peaknote.demo.model;

public class TranscriptInfo {

    private String userId;
    private String meetingId;
    private String transcriptId;

    public TranscriptInfo() {
    }

    public TranscriptInfo(String userId, String meetingId, String transcriptId) {
        this.userId = userId;
        this.meetingId = meetingId;
        this.transcriptId = transcriptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    @Override
    public String toString() {
        return "TranscriptInfo{" +
                "userId='" + userId + '\'' +
                ", meetingId='" + meetingId + '\'' +
                ", transcriptId='" + transcriptId + '\'' +
                '}';
    }
}
