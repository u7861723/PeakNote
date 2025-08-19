package com.peaknote.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peaknote.demo.model.TranscriptInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PayloadParserService {

    private static final Logger log = LoggerFactory.getLogger(PayloadParserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse transcript webhook payload, return encapsulated TranscriptInfo
     */
    public TranscriptInfo parseTranscriptInfo(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        String resource = valueNode.get("resource").asText();

        // resource example: users('userId')/onlineMeetings('meetingId')/transcripts('transcriptId')
        String cleaned = resource.replace("users('", "")
                .replace("')/onlineMeetings('", ",")
                .replace("')/transcripts('", ",")
                .replace("')", "");

        String[] parts = cleaned.split(",");

        if (parts.length == 3) {
            String userId = parts[0];
            String meetingId = parts[1];
            String transcriptId = parts[2];
            log.info("✅ Successfully parsed transcript payload, userId={}, meetingId={}, transcriptId={}", userId, meetingId, transcriptId);
            return new TranscriptInfo(userId, meetingId, transcriptId);
        } else {
            log.error("❌ Unable to parse transcript resource format, original resource: {}", resource);
            throw new RuntimeException("Invalid transcript resource format: " + resource);
        }
    }

    /**
     * Extract user ID (for event webhook)
     */
    public String extractUserIdFromEventPayload(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        String resource = valueNode.get("resource").asText();

        // Example resource: Users/{userId}/Events/{eventId}
        String[] parts = resource.split("/");
        if (parts.length >= 2) {
            String userId = parts[1];
            log.info("✅ Successfully parsed event payload, userId={}", userId);
            return userId;
        } else {
            log.error("❌ Unable to parse event resource format, original resource: {}", resource);
            throw new RuntimeException("Invalid event resource format: " + resource);
        }
    }

    /**
     * Extract event ID (for event webhook)
     */
    public String extractEventIdFromEventPayload(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        JsonNode resourceData = valueNode.get("resourceData");

        if (resourceData != null && resourceData.has("id")) {
            String eventId = resourceData.get("id").asText();
            log.info("✅ Successfully parsed event payload, eventId={}", eventId);
            return eventId;
        } else {
            log.error("❌ Unable to find id field in resourceData");
            throw new RuntimeException("Event payload missing id in resourceData");
        }
    }
    
        public String extractSubscriptionId(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);

        if (valueNode.has("subscriptionId")) {
            return valueNode.get("subscriptionId").asText();
        } else {
            log.warn("⚠️ Webhook payload does not contain subscriptionId");
            return null;
        }
    }

        public String extractChangeType(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode valueArray = root.get("value");
            if (valueArray != null && valueArray.isArray() && valueArray.size() > 0) {
                JsonNode first = valueArray.get(0);
                return first.get("changeType").asText();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse changeType", e);
        }
        return null;
    }
}
