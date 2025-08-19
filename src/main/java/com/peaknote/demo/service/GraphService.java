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
import com.peaknote.demo.util.ErrorHandler;
import com.peaknote.demo.exception.PeakNoteException;

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
                throw new IllegalArgumentException("User ID and Event ID cannot be empty");}
            ErrorHandler.validateNotEmpty(userId, "User ID");
            ErrorHandler.validateNotEmpty(eventId, "Event ID");
            
            return webhookGraphClient
                    .users(userId)
                    .events(eventId)
                    .buildRequest()
                    .get();
         }catch (Exception e) {
            System.err.println("❌ Failed to get user event: userId=" + userId + ", eventId=" + eventId + ", error=" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get user event", e);
        }
    }

    // Query OnlineMeeting by joinUrl
    public OnlineMeetingCollectionPage getOnlineMeetingsByJoinUrl(String userId, String joinUrl) {
        try {
            if (userId == null || userId.trim().isEmpty() || joinUrl == null || joinUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID and Join URL cannot be empty");}
            ErrorHandler.validateNotEmpty(userId, "User ID");
            ErrorHandler.validateNotEmpty(joinUrl, "Join URL");
            
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

    // Get each instance of a series meeting
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

        // Create subscription for Event
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

    // Create subscription for Transcript
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
     * List all subscriptions
     */
    public SubscriptionCollectionPage listAllSubscriptions() {
        return webhookGraphClient.subscriptions()
                .buildRequest()
                .get();
    }

    /**
     * Delete specified subscription
     */
    public void deleteSubscription(String subscriptionId) {
        webhookGraphClient.subscriptions(subscriptionId)
                .buildRequest()
                .delete();
    }

    // Renew subscription
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
    // Construct URL to get attendanceReports list
    String reportsUrl = String.format(
            "/users/%s/onlineMeetings/%s/attendanceReports",
            userId, meetingId);

    // First get the attendanceReports list
    Map reportsResponse = graphClient
            .customRequest(reportsUrl, Map.class)
            .buildRequest()
            .get();

    List<Map<String, Object>> reports = (List<Map<String, Object>>) reportsResponse.get("value");
    if (reports == null || reports.isEmpty()) {
        throw new IllegalStateException("No attendanceReports found, meeting may not have generated reports");
    }

    // Only take the first report (can also loop through multiple)
    String reportId = (String) reports.get(0).get("id");

    // Construct URL to get detailed report (with attendees) using $expand
    String detailUrl = String.format(
            "/users/%s/onlineMeetings/%s/attendanceReports/%s?$expand=attendanceRecords",
            userId, meetingId, reportId);

    // Get attendee details
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
    System.out.println("Attendee details JSON:\n" + json);
    }
}

