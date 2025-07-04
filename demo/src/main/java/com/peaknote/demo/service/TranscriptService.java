package com.peaknote.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;

import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;


@Service
public class TranscriptService {

    private final GraphServiceClient<Request> graphClient;
    private static final Logger log = LoggerFactory.getLogger(TranscriptService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TranscriptService(@Qualifier("graphClient") GraphServiceClient<Request> graphClient) {
        this.graphClient = graphClient;
    }

 /**
     * 下载 transcript 内容
     *
     * @param userId       Teams 用户 ID（即 organizer 的 objectId）
     * @param meetingId    在线会议 ID（从 webhook payload 里解析出来）
     * @param transcriptId transcript 的 ID（从 webhook payload 里解析出来）
     * @param accessToken  你刚刚拿到的 token
     * @return transcript 文件内容（例如 VTT 格式）
     */
    public String downloadTranscriptContent(String userId, String meetingId, String transcriptId, String accessToken) {
        try {
            String url = "https://graph.microsoft.com/beta/users/" + userId
                    + "/onlineMeetings/" + meetingId
                    + "/transcripts/" + transcriptId + "/content";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "text/vtt")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ 成功获取 transcript 内容！");
                return response.body();
            } else {
                System.err.println("❌ 下载 transcript 失败，状态码: " + response.statusCode());
                System.err.println("响应内容: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ 下载 transcript 出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 payload 中提取 userId, meetingId, transcriptId
     */
    public TranscriptInfo parseIds(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode valueNode = root.get("value").get(0);
            String resource = valueNode.get("resource").asText();

            // resource 示例：
            // users('userId')/onlineMeetings('meetingId')/transcripts('transcriptId')
            String cleaned = resource.replace("users('", "")
                                     .replace("')/onlineMeetings('", ",")
                                     .replace("')/transcripts('", ",")
                                     .replace("')", "");

            String[] parts = cleaned.split(",");

            if (parts.length == 3) {
                String userId = parts[0];
                String meetingId = parts[1];
                String transcriptId = parts[2];
                log.info("✅ 解析出 userId: {}, meetingId: {}, transcriptId: {}", userId, meetingId, transcriptId);
                return new TranscriptInfo(userId, meetingId, transcriptId);
            } else {
                log.error("❌ 解析 resource 失败，结果: {}", cleaned);
                return null;
            }

        } catch (Exception e) {
            log.error("❌ 解析 payload 出错: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 用于封装返回的 id 信息
     */
    public static class TranscriptInfo {
        public String userId;
        public String meetingId;
        public String transcriptId;

        public TranscriptInfo(String userId, String meetingId, String transcriptId) {
            this.userId = userId;
            this.meetingId = meetingId;
            this.transcriptId = transcriptId;
        }
    }


}
