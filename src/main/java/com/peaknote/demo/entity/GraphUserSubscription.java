package com.peaknote.demo.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "graph_subscription")
public class GraphUserSubscription {
    @Id
    private String id;  // subscriptionId

    private OffsetDateTime expirationDateTime;

    // getter & setter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getExpirationDateTime() {
        return expirationDateTime;
    }
    public void setExpirationDateTime(OffsetDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }
}
