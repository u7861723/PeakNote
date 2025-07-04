package com.peaknote.demo.service;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
public class GraphEventService {

    private final GraphService graphService;
    private final MeetingEventRepository repository;

    public GraphEventService(GraphService graphService, MeetingEventRepository repository) {
        this.graphService = graphService;
        this.repository = repository;
    }

    @Transactional
    public void processEvent(String userId, String eventId, boolean isRecurring) {
        Event event = graphService.getUserEvent(userId, eventId);

        if (isRecurring) {
            addSeriesInstances(userId, eventId);
        } else {
            repository.save(convertEvent(event, userId));
        }
    }

    public void addSeriesInstances(String userId, String seriesMasterId) {
        String startDateTime = OffsetDateTime.now().toString();
        String endDateTime = OffsetDateTime.now().plusMonths(2).toString();

        EventCollectionPage occurrences = graphService.getEventOccurrences(userId, seriesMasterId, startDateTime, endDateTime);

        if (occurrences.getCurrentPage().isEmpty()) {
            System.out.println("✅ 没有 occurrence（可能太早）");
            return;
        }

        for (Event occurrence : occurrences.getCurrentPage()) {
            repository.save(convertEvent(occurrence, userId));
        }
    }

    private MeetingEvent convertEvent(Event event, String userId) {
        var entity = new MeetingEvent();
        entity.setEventId(event.id);
        entity.setUserId(userId);
        entity.setSubject(event.subject);
        entity.setJoinUrl(event.onlineMeeting != null ? event.onlineMeeting.joinUrl : null);
        entity.setStartTime(parseGraphDateTime(event.start.dateTime));
        entity.setEndTime(parseGraphDateTime(event.end.dateTime));

        if (event.onlineMeeting != null && event.onlineMeeting.joinUrl != null) {
            OnlineMeetingCollectionPage meetings = graphService.getOnlineMeetingsByJoinUrl(userId, event.onlineMeeting.joinUrl);
            if (!meetings.getCurrentPage().isEmpty()) {
                String meetingId = meetings.getCurrentPage().get(0).id;
                entity.setMeetingId(meetingId);
            }
        }

        return entity;
    }

    private Instant parseGraphDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        String cleaned = dateTimeStr.contains(".") ? dateTimeStr.substring(0, dateTimeStr.indexOf(".")) : dateTimeStr;
        return OffsetDateTime.parse(cleaned + "Z").toInstant();
    }
}
