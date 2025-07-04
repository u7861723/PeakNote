package com.peaknote.demo.service;

import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MeetingEventSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(MeetingEventSchedulerService.class);

    private final MeetingEventRepository meetingEventRepository;
    private final SubscriptionService subscriptionService;

    public MeetingEventSchedulerService(MeetingEventRepository meetingEventRepository,
                                        SubscriptionService subscriptionService) {
        this.meetingEventRepository = meetingEventRepository;
        this.subscriptionService = subscriptionService;
    }

    /**
     * 每 5 分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void subscribeRecentMeetings() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime fiveMinutesAgo = now.minusMinutes(5);

                List<MeetingEvent> events = meetingEventRepository.findByStartTimeBetweenAndTranscriptStatus(
                fiveMinutesAgo.toInstant(), now.toInstant(), "none"
        );

        if (events.isEmpty()) {
            log.info("✅ 最近 5 分钟没有会议需要订阅 transcript");
            return;
        }

        for (MeetingEvent event : events) {
            try {
                log.info("📄 为会议创建 transcript 订阅: eventId={}, meetingId={}", event.getEventId(), event.getMeetingId());
                subscriptionService.createTranscriptSubscription(event.getMeetingId());

                event.setTranscriptStatus("subscribed");
                meetingEventRepository.save(event);
                log.info("✅ 已更新会议 {} 状态为 subscribed", event.getEventId());
            } catch (Exception e) {
                log.error("❌ 为会议 {} 创建订阅失败: {}", event.getEventId(), e.getMessage(), e);
            }
        }
    }
}
