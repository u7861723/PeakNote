package com.peaknote.demo.service;

import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.options.QueryOption;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingEventRepository;
import okhttp3.Request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;


@Service
public class GraphEventService {

    @Autowired
    public SubscriptionService subscriptionService;

    private final GraphServiceClient<Request> graphClient;
    private final GraphServiceClient<Request> webhookGraphClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MeetingEventRepository repository;

    public GraphEventService(@Qualifier("graphClient")GraphServiceClient<Request> graphClient,@Qualifier("webhookGraphClient")GraphServiceClient<Request> webhookGraphClient, MeetingEventRepository repository) {
        this.graphClient = graphClient;
        this.repository = repository;
        this.webhookGraphClient = webhookGraphClient;
    }

    @Transactional
    public void processEvent(String payload) throws JsonMappingException, JsonProcessingException {
        JsonNode json = objectMapper.readTree(payload);
        JsonNode valueNode = json.get("value").get(0);
        JsonNode resourceData = valueNode.get("resourceData");
        String eventId = resourceData.get("id").asText();
        String resource = valueNode.get("resource").asText();
        String userId = extractUserIdFromResource(resource);
        // 再次查询完整事件
        Event event = webhookGraphClient
            .users(userId)
            .events(eventId)
            .buildRequest()
            .get();

        if (event.recurrence != null) {
            addSeriesInstances(userId, eventId);
        } else {
            repository.save(convertEvent(event, userId));
        }
    }

    public void addSeriesInstances(String userId,String seriesMasterId) {
        try {
            String startDateTime = OffsetDateTime.now().toString();
            String endDateTime = OffsetDateTime.now().plusMonths(2).toString(); // 或者你需要的窗口

            EventCollectionPage occurrences = webhookGraphClient
                .users(userId)
                .events(seriesMasterId)
                .instances()
                .buildRequest(
                    Arrays.asList(
                        new QueryOption("startDateTime", startDateTime),
                        new QueryOption("endDateTime", endDateTime)
                    )
                )
                .get();

            if (occurrences.getCurrentPage().isEmpty()) {
                System.out.println("✅ 没有生成 occurrence（可能太早）");
                return;
            }

            for (Event occurrence : occurrences.getCurrentPage()) {
                repository.save(convertEvent(occurrence, userId));
            }

        } catch (Exception e) {
            System.err.println("❌ 获取 occurrences 出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Instant parseGraphDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        String cleaned = dateTimeStr;
        if (cleaned.contains(".")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("."));
        }
        cleaned = cleaned + "Z"; // 加上 UTC 时区
        return OffsetDateTime.parse(cleaned).toInstant();
    }

    private MeetingEvent convertEvent(Event event, String userId) {
        var entity = new MeetingEvent();
        entity.setEventId(event.id);
        entity.setUserId(userId);
        entity.setSubject(event.subject);
        entity.setJoinUrl(event.onlineMeeting != null ? event.onlineMeeting.joinUrl : null);
        entity.setMeetingId(getOnlineMeetingIdByJoinUrl(userId,event.onlineMeeting.joinUrl));
        entity.setStartTime(parseGraphDateTime(event.start.dateTime));
        entity.setEndTime(parseGraphDateTime(event.end.dateTime));
        return entity;
    }
    public String extractUserIdFromResource(String resource) {
        // e.g. "Users/{userId}/Events/{eventId}"
        String[] parts = resource.split("/");
        return parts.length >= 2 ? parts[1] : null;
    }

       public String getOnlineMeetingIdByJoinUrl(String userId, String joinUrl) {
        try {
            // 拼接 filter
            String filter = "JoinWebUrl eq '" + joinUrl + "'";

            QueryOption option = new QueryOption("$filter", filter);

            // 调用 API
            OnlineMeetingCollectionPage meetings = graphClient
                .users(userId)
                .onlineMeetings()
                .buildRequest(Collections.singletonList(option))
                .get();

            // 只取第一个
            if (!meetings.getCurrentPage().isEmpty()) {
                String meetingId = meetings.getCurrentPage().get(0).id;
                System.out.println("✅ OnlineMeeting ID: " + meetingId);


                //订阅webhook transcript
                subscriptionService.createTranscriptSubscription(meetingId);




                return meetingId;
            } else {
                System.out.println("❌ 没有找到对应会议");
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ 查询出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    

}
