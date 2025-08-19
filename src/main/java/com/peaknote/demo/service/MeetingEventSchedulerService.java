package com.peaknote.demo.service;

import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
     * Execute daily at 01:00
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void subscribeRecentMeetings() {
        LocalDate today = LocalDate.now();
        OffsetDateTime startOfDay = today.atStartOfDay(ZoneOffset.systemDefault()).toOffsetDateTime();
        OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneOffset.systemDefault()).toOffsetDateTime();

        List<MeetingEvent> events = meetingEventRepository.findByStartTimeBetweenAndTranscriptStatus(
                startOfDay.toInstant(), endOfDay.toInstant(), "none"
        );

        if (events.isEmpty()) {
            log.info("✅ No meetings in the last 5 minutes need transcript subscription");
            return;
        }

        for (MeetingEvent event : events) {
            try {
                log.info("📄 Creating transcript subscription for meeting: eventId={}, meetingId={}", event.getEventId(), event.getMeetingId());
                subscriptionService.createTranscriptSubscription(event.getMeetingId());

                event.setTranscriptStatus("subscribed");
                meetingEventRepository.save(event);
                log.info("✅ Meeting {} status updated to subscribed", event.getEventId());
            } catch (Exception e) {
                log.error("❌ Failed to create subscription for meeting {}: {}", event.getEventId(), e.getMessage(), e);
            }
        }
    }
}
