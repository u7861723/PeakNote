package com.peaknote.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.microsoft.graph.requests.GraphServiceClient;
import com.peaknote.demo.service.GraphEventService;
import com.peaknote.demo.service.GraphService;
import com.peaknote.demo.service.TranscriptService;
import com.peaknote.demo.service.TranscriptService.TranscriptInfo;

import jakarta.servlet.http.HttpServletRequest;
import okhttp3.Request;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//接收webhook请求，调用graphEventService解析payload并作出相应处理
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final GraphEventService graphEventService;
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GraphServiceClient<Request> graphClient;
    private final TranscriptService transcriptService;
    private final String accessToken;


    public WebhookController(@Qualifier("getAccessToken") String accessToken, @Qualifier("webhookGraphClient")GraphServiceClient<Request> graphClient, GraphService graphService, GraphEventService graphEventService, TranscriptService transcriptService) {
        this.graphClient = graphClient;
        this.graphEventService = graphEventService;
        this.transcriptService = transcriptService;
        this.accessToken = accessToken;
    }


    @GetMapping("/notification")
    public ResponseEntity<String> validateGet(@RequestParam("validationToken") String token) {
        log.info("✅ 收到 Graph 验证 GET 请求, 返回 token: {}", token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/notification")
    public ResponseEntity<String> validateOrReceivePost(HttpServletRequest request, @RequestBody(required = false) String payload) {
        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ 收到 Graph 验证 POST 请求, 返回 token: {}", token);
            return ResponseEntity.ok(token);
        }

        try {
            log.info("✅ 收到 Graph webhook POST 请求 payload: {}", payload);
            graphEventService.processEvent(payload);
            // 暂时不做任何处理，只打印
            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            log.error("❌ 解析 webhook payload 失败: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body("Failed");
        }
    }

    /**
     * GET 请求 — 用于验证 subscription 创建
     */
@PostMapping("/teams-transcript")
public ResponseEntity<String> handleTeamsTranscript(HttpServletRequest request, @RequestBody(required = false) String payload) {
    try {
        System.out.println("✅ 收到 Teams transcript 回调 Payload:");

        System.out.println(payload);

        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ 收到 Graph 验证 POST 请求, 返回 token: {}", token);
            return ResponseEntity.ok(token);
        }

        TranscriptInfo transaction = transcriptService.parseIds(payload);
        String userId = transaction.userId;
        String meetingId = transaction.meetingId;
        String transcriptId = transaction.transcriptId;
        System.out.println(transcriptService.downloadTranscriptContent(userId, meetingId, transcriptId,accessToken));
    

        // ⚡️ 实际收到通知后可以在这里处理 transcript（例如写库、下载文件等）
        System.out.println("📄 收到实际 transcript 通知，无需返回验证 token");
        return ResponseEntity.ok("OK");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Failed");
    }
}
}
