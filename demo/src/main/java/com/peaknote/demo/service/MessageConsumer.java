package com.peaknote.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.peaknote.demo.model.TranscriptInfo;

import static com.peaknote.demo.config.RabbitMQConfig.EVENT_QUEUE;
import static com.peaknote.demo.config.RabbitMQConfig.TRANSCRIPT_QUEUE;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final PayloadParserService payloadParserService;
    private final GraphService graphService;
    private final GraphEventService graphEventService;
    private final TranscriptService transcriptService;
    private final String accessToken;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = EVENT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleEventMessage(String payload) {
        try {
            log.info("✅ 消费 Event 消息");
            String userId = payloadParserService.extractUserIdFromEventPayload(payload);
            String eventId = payloadParserService.extractEventIdFromEventPayload(payload);
            String type = payloadParserService.extractChangeType(payload);
            String key = eventId + ":" + type;
            // Redis 去重
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(success)) {
                log.warn("⚠️ 重复消息，已忽略: {}", key);
                return;
            }
            boolean isRecurring = (graphService.getUserEvent(userId, eventId).recurrence != null);
            graphEventService.processEvent(userId, eventId, isRecurring);
        } catch (Exception e) {
            log.error("❌ 处理 Event 消息失败: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = TRANSCRIPT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleTranscriptMessage(String payload) {
        try {
            log.info("✅ 消费 Transcript 消息");
            TranscriptInfo transcriptInfo = payloadParserService.parseTranscriptInfo(payload);
            String subscriptionId = payloadParserService.extractSubscriptionId(payload);
            String type = payloadParserService.extractChangeType(payload);

            String key = subscriptionId + ":" + type;

            // Redis 去重
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(success)) {
                log.warn("⚠️ 重复消息，已忽略: {}", key);
                return;
            }

            if (subscriptionId != null) {
                graphService.deleteSubscription(subscriptionId);
                log.info("✅ 已关闭订阅 ID: {}", subscriptionId);
            }

            transcriptService.downloadTranscriptContent(
                    transcriptInfo.getUserId(),
                    transcriptInfo.getMeetingId(),
                    transcriptInfo.getTranscriptId(),
                    accessToken
            );
        } catch (Exception e) {
            log.error("❌ 处理 Transcript 消息失败: {}", e.getMessage(), e);
        }
    }
}
