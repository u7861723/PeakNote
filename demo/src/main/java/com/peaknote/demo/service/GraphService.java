package com.peaknote.demo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
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


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GraphService {


    private final String webhookUrl;

    @Qualifier("graphClient")
    private final GraphServiceClient<Request> graphClient;

    @Qualifier("webhookGraphClient")
    private final GraphServiceClient<Request> webhookGraphClient;

    private final AzureProperties azureProperties;

    public GraphService(
            @Value("${notification-url}") String webhookUrl,
            @Qualifier("graphClient") GraphServiceClient<Request> graphClient,
            @Qualifier("webhookGraphClient") GraphServiceClient<Request> webhookGraphClient,
            AzureProperties azureProperties) {
        this.webhookUrl = webhookUrl;
        this.graphClient = graphClient;
        this.webhookGraphClient = webhookGraphClient;
        this.azureProperties = azureProperties;
    }

    //获取所有租户用户
    public List<User> fetchAllUsers() {
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
    }

    //获取某个用户的单个事件
        public Event getUserEvent(String userId, String eventId) {
        return webhookGraphClient
                .users(userId)
                .events(eventId)
                .buildRequest()
                .get();
    }

    //根据joinUrl查询OnlineMeting
        public OnlineMeetingCollectionPage getOnlineMeetingsByJoinUrl(String userId, String joinUrl) {
        String filter = "JoinWebUrl eq '" + joinUrl + "'";
        QueryOption option = new QueryOption("$filter", filter);

        return graphClient
                .users(userId)
                .onlineMeetings()
                .buildRequest(Collections.singletonList(option))
                .get();
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
}

