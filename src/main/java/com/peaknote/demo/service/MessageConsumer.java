package com.peaknote.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.model.TranscriptInfo;
import com.peaknote.demo.repository.MeetingEventRepository;

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
    private final StringRedisTemplate stringRedisTemplate;
    private final MeetingEventRepository meetingEventRepository;
    // private final AttendanceService attendanceService;

    @RabbitListener(queues = EVENT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleEventMessage(String payload) {
        try {
            log.info("✅ Consuming Event message");
            String userId = payloadParserService.extractUserIdFromEventPayload(payload);
            String eventId = payloadParserService.extractEventIdFromEventPayload(payload);
            String type = payloadParserService.extractChangeType(payload);
            String key = eventId + ":" + type;
            // Redis deduplication
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(success)) {
                log.warn("⚠️ Duplicate message, ignored: {}", key);
                return;
            }
            boolean isRecurring = (graphService.getUserEvent(userId, eventId).recurrence != null);
            graphEventService.processEvent(userId, eventId, isRecurring);
        } catch (Exception e) {
            log.error("❌ Failed to process Event message: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = TRANSCRIPT_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleTranscriptMessage(String payload) {
        try {
            log.info("✅ Consuming Transcript message");
            TranscriptInfo transcriptInfo = payloadParserService.parseTranscriptInfo(payload);
            String subscriptionId = payloadParserService.extractSubscriptionId(payload);
            String type = payloadParserService.extractChangeType(payload);

            String key = subscriptionId + ":" + type;

            // Redis deduplication
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 5, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(success)) {
                log.warn("⚠️ Duplicate message, ignored: {}", key);
                return;
            }

            if (subscriptionId != null) {
                graphService.deleteSubscription(subscriptionId);
                log.info("✅ Closed subscription ID: {}", subscriptionId);
            }

            MeetingEvent meetingEvent = meetingEventRepository.findFirstByMeetingIdAndTranscriptStatus(transcriptInfo.getMeetingId(), "subscribed");
            if (meetingEvent == null) {
                log.error("❌ Meeting event not found, eventId={}", transcriptInfo.getMeetingId());
                return;
            }

            transcriptService.downloadTranscriptContent(
                    transcriptInfo.getUserId(),
                    transcriptInfo.getMeetingId(),
                    transcriptInfo.getTranscriptId(),
                    graphService.getAccessToken()
            );
            
            graphService.getAttendees(meetingEvent, transcriptInfo.getUserId(), transcriptInfo.getMeetingId());


        } catch (Exception e) {
            log.error("❌ Failed to process Transcript message: {}", e.getMessage(), e);
        }
    }
}
