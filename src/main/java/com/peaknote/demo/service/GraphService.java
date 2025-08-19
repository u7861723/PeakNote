package com.peaknote.demo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.QueryOption;

import okhttp3.Request;

import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.peaknote.demo.config.AzureProperties;
import com.peaknote.demo.entity.MeetingAttendee;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingAttendeeRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GraphService {


    private final String webhookUrl;
    private final GraphServiceClient<Request> graphClient;
    private final GraphServiceClient<Request> webhookGraphClient;
    private final AzureProperties azureProperties;
    private final MeetingAttendeeRepository meetingAttendeeRepository;

    public GraphService(
            @Value("${notification-url}") String webhookUrl,
            @Qualifier("graphClient") GraphServiceClient<Request> graphClient,
            @Qualifier("webhookGraphClient") GraphServiceClient<Request> webhookGraphClient,
            AzureProperties azureProperties,
            MeetingAttendeeRepository meetingAttendeeRepository) {
        this.webhookUrl = webhookUrl;
        this.graphClient = graphClient;
        this.webhookGraphClient = webhookGraphClient;
        this.azureProperties = azureProperties;
        this.meetingAttendeeRepository = meetingAttendeeRepository;
    }

    // Fetch all tenant users
    public List<User> fetchAllUsers() {
        try {
            List<User> result = new ArrayList<>();
            var page = graphClient.users()
                    .buildRequest()
                    .select("id,mail,userPrincipalName")
                    .top(50)
                    .get();

            while (page != null) {
                result.addAll(page.getCurrentPage());
                page = page.getNextPage() != null ? page.getNextPage().buildRequest().get() : null;
            }
            return result;
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch user list: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch user list", e);
        }
    }

    // Get a single event for a specific user
    public Event getUserEvent(String userId, String eventId) {
        try {
            if (userId == null || userId.trim().isEmpty() || eventId == null || eventId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID and Event ID cannot be empty");
            }
            
            return webhookGraphClient
                    .users(userId)
                    .events(eventId)
                    .buildRequest()
                    .get();
        } catch (Exception e) {
            System.err.println("❌ Failed to get user event: userId=" + userId + ", eventId=" + eventId + ", error=" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get user event", e);
        }
    }

    // Query OnlineMeeting by joinUrl
    public OnlineMeetingCollectionPage getOnlineMeetingsByJoinUrl(String userId, String joinUrl) {
        try {
            if (userId == null || userId.trim().isEmpty() || joinUrl == null || joinUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID and Join URL cannot be empty");
            }
            
            String filter = "JoinWebUrl eq '" + joinUrl + "'";
            QueryOption option = new QueryOption("$filter", filter);

            return graphClient
                    .users(userId)
                    .onlineMeetings()
                    .buildRequest(Collections.singletonList(option))
                    .get();
        } catch (Exception e) {
            System.err.println("❌ Failed to query online meeting by URL: userId=" + userId + ", joinUrl=" + joinUrl + ", error=" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to query online meeting", e);
        }
    }

    //获取系列会议的每个实例
        public EventCollectionPage getEventOccurrences(String userId, String seriesMasterId, String startDateTime, String endDateTime) {
        return webhookGraphClient
                .users(userId)
                .events(seriesMasterId)
                .instances()
                .buildRequest(Arrays.asList(
                        new QueryOption("startDateTime", startDateTime),
                        new QueryOption("endDateTime", endDateTime)
                ))
                .get();
    }

        //创建对Event的订阅
        public Subscription createEventSubscription(String userId, String notificationUrl, String clientState, OffsetDateTime expireTime) {
        Subscription subscription = new Subscription();
        subscription.changeType = "created";
        subscription.notificationUrl = notificationUrl;
        subscription.resource = "/users/" + userId + "/events";
        subscription.expirationDateTime = expireTime;
        subscription.clientState = clientState;

        return webhookGraphClient.subscriptions()
                .buildRequest()
                .post(subscription);
    }

    //创建对Transcript的订阅
    public Subscription createTranscriptSubscription(String meetingId, String notificationUrl, String clientState, OffsetDateTime expireTime) {
        Subscription subscription = new Subscription();
        subscription.changeType = "created";
        subscription.notificationUrl = notificationUrl;
        subscription.resource = "/communications/onlineMeetings/" + meetingId + "/transcripts";
        subscription.expirationDateTime = expireTime;
        subscription.clientState = clientState;
        //subscription.lifecycleNotificationUrl = "https://8fca4ce341a6.ngrok-free.app/webhook/teams-lifecycle";
          subscription.lifecycleNotificationUrl = webhookUrl + "webhook/teams-lifecycle";
        return webhookGraphClient.subscriptions()
                .buildRequest()
                .post(subscription);
    }

        /**
     * 列出所有订阅
     */
    public SubscriptionCollectionPage listAllSubscriptions() {
        return webhookGraphClient.subscriptions()
                .buildRequest()
                .get();
    }

    /**
     * 删除指定订阅
     */
    public void deleteSubscription(String subscriptionId) {
        webhookGraphClient.subscriptions(subscriptionId)
                .buildRequest()
                .delete();
    }

    //续订订阅
    public Subscription renewSubscription(String subscriptionId, OffsetDateTime newExpiration) {
        Subscription subscription = new Subscription();
        subscription.expirationDateTime = newExpiration;

        return graphClient.subscriptions(subscriptionId)
            .buildRequest()
            .patch(subscription);
    }

    public String getAccessToken() {
    ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(azureProperties.getClientId())
            .clientSecret(azureProperties.getClientSecret())
            .tenantId(azureProperties.getTenantId())
            .build();

    TokenRequestContext requestContext = new TokenRequestContext()
            .addScopes("https://graph.microsoft.com/.default");

    AccessToken accessToken = credential.getToken(requestContext).block();
    return accessToken.getToken();
}

public void getAttendees(MeetingEvent meetingEvent, String userId, String meetingId) throws JsonProcessingException {
    // 构造获取 attendanceReports 列表的 URL
    String reportsUrl = String.format(
            "/users/%s/onlineMeetings/%s/attendanceReports",
            userId, meetingId);

    // 先获取 attendanceReports 列表
    Map reportsResponse = graphClient
            .customRequest(reportsUrl, Map.class)
            .buildRequest()
            .get();

    List<Map<String, Object>> reports = (List<Map<String, Object>>) reportsResponse.get("value");
    if (reports == null || reports.isEmpty()) {
        throw new IllegalStateException("未找到任何 attendanceReports，会议可能未生成报告");
    }

    // 只取第一个 report（也可以循环多个）
    String reportId = (String) reports.get(0).get("id");

    // 构造获取详细 report（带参会人员）的 URL，带上 $expand
    String detailUrl = String.format(
            "/users/%s/onlineMeetings/%s/attendanceReports/%s?$expand=attendanceRecords",
            userId, meetingId, reportId);

    // 获取参会人员详情
    Object detailResponse = graphClient
            .customRequest(detailUrl, Object.class)
            .buildRequest()
            .get();

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(detailResponse);
    JsonNode rootNode = new ObjectMapper().readTree(json);
    JsonNode records = rootNode.get("attendanceRecords");
    for(JsonNode record: records){
        String email = record.get("emailAddress").asText();
        String displayName = record.get("identity").get("displayName").asText();
        MeetingAttendee attendee = new MeetingAttendee();
        attendee.setDisplayName(displayName);
        attendee.setUserEmail(email);
        attendee.setMeetingEvent(meetingEvent);
        meetingAttendeeRepository.save(attendee);
    }
    System.out.println("参会详情 JSON：\n" + json);
    }
}

