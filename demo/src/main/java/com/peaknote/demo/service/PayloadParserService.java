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
     * 解析 transcript webhook payload，返回封装好的 TranscriptInfo
     */
    public TranscriptInfo parseTranscriptInfo(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        String resource = valueNode.get("resource").asText();

        // resource 示例：users('userId')/onlineMeetings('meetingId')/transcripts('transcriptId')
        String cleaned = resource.replace("users('", "")
                .replace("')/onlineMeetings('", ",")
                .replace("')/transcripts('", ",")
                .replace("')", "");

        String[] parts = cleaned.split(",");

        if (parts.length == 3) {
            String userId = parts[0];
            String meetingId = parts[1];
            String transcriptId = parts[2];
            log.info("✅ 成功解析 transcript payload，userId={}, meetingId={}, transcriptId={}", userId, meetingId, transcriptId);
            return new TranscriptInfo(userId, meetingId, transcriptId);
        } else {
            log.error("❌ 无法解析 transcript resource 格式，原始 resource: {}", resource);
            throw new RuntimeException("Invalid transcript resource format: " + resource);
        }
    }

    /**
     * 提取用户 ID（针对事件 webhook）
     */
    public String extractUserIdFromEventPayload(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        String resource = valueNode.get("resource").asText();

        // 示例 resource: Users/{userId}/Events/{eventId}
        String[] parts = resource.split("/");
        if (parts.length >= 2) {
            String userId = parts[1];
            log.info("✅ 成功解析 event payload，userId={}", userId);
            return userId;
        } else {
            log.error("❌ 无法解析事件 resource 格式，原始 resource: {}", resource);
            throw new RuntimeException("Invalid event resource format: " + resource);
        }
    }

    /**
     * 提取事件 ID（针对事件 webhook）
     */
    public String extractEventIdFromEventPayload(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);
        JsonNode resourceData = valueNode.get("resourceData");

        if (resourceData != null && resourceData.has("id")) {
            String eventId = resourceData.get("id").asText();
            log.info("✅ 成功解析 event payload，eventId={}", eventId);
            return eventId;
        } else {
            log.error("❌ 无法在 resourceData 中找到 id 字段");
            throw new RuntimeException("Event payload missing id in resourceData");
        }
    }
    
        public String extractSubscriptionId(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode valueNode = root.get("value").get(0);

        if (valueNode.has("subscriptionId")) {
            return valueNode.get("subscriptionId").asText();
        } else {
            log.warn("⚠️ webhook payload 不包含 subscriptionId");
            return null;
        }
    }
}
