package com.peaknote.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meeting_url_access")
public class MeetingUrlAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_oid", nullable = false)
    private TeamsUser owner;

    @ManyToOne
    @JoinColumn(name = "shared_with_oid", nullable = false)
    private TeamsUser sharedWith;

    @Column(nullable = false)
    private String joinUrl;

    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TeamsUser getOwner() {
        return owner;
    }

    public void setOwner(TeamsUser owner) {
        this.owner = owner;
    }

    public TeamsUser getSharedWith() {
        return sharedWith;
    
    }

    public void setSharedWith(TeamsUser sharedWith) {
        this.sharedWith = sharedWith;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MeetingUrlAccess [id=" + id + ", owner=" + owner + ", sharedWith=" + sharedWith + ", joinUrl=" + joinUrl
                + ", createdAt=" + createdAt + "]";
    }

}