package com.peaknote.demo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.QueryOption;

import okhttp3.Request;

import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
public class GraphService {

    private final GraphServiceClient<Request> graphClient;
    private final GraphServiceClient<Request> webhookGraphClient;

    public GraphService(
        @Qualifier("graphClient")GraphServiceClient<Request> graphClient,
        @Qualifier("webhookGraphClient") GraphServiceClient<Request> webhookGraphClient) {
        this.graphClient = graphClient;
        this.webhookGraphClient = webhookGraphClient;
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
}

