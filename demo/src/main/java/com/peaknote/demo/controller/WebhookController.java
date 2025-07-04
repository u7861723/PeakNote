package com.peaknote.demo.controller;

import com.peaknote.demo.model.TranscriptInfo;
import com.peaknote.demo.service.GraphEventService;
import com.peaknote.demo.service.GraphService;
import com.peaknote.demo.service.PayloadParserService;
import com.peaknote.demo.service.SubscriptionService;
import com.peaknote.demo.service.TranscriptService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final GraphEventService graphEventService;
    private final TranscriptService transcriptService;
    private final PayloadParserService payloadParserService;
    private final SubscriptionService subscriptionService;
    private final String accessToken;
    private final GraphService graphService;

    public WebhookController(@Qualifier("getAccessToken") String accessToken,
                             GraphEventService graphEventService,
                             TranscriptService transcriptService,
                             PayloadParserService payloadParserService,
                             SubscriptionService subscriptionService,
                             GraphService graphService) {
        this.accessToken = accessToken;
        this.graphEventService = graphEventService;
        this.transcriptService = transcriptService;
        this.payloadParserService = payloadParserService;
        this.subscriptionService = subscriptionService;
        this.graphService = graphService;
    }

    @GetMapping("/notification")
    public ResponseEntity<String> validateGet(@RequestParam("validationToken") String token) {
        log.info("✅ 收到 Graph 验证 GET 请求，返回 token: {}", token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/notification")
    public ResponseEntity<String> handleNotification(HttpServletRequest request,
                                                     @RequestBody(required = false) String payload) {
        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ 收到 Graph 验证 POST 请求，返回 token: {}", token);
            return ResponseEntity.ok(token);
        }

        try {
            log.info("✅ 收到事件 webhook，开始处理");
            String userId = payloadParserService.extractUserIdFromEventPayload(payload);
            String eventId = payloadParserService.extractEventIdFromEventPayload(payload);

            boolean isRecurring = payload.contains("seriesMasterId"); // 示例判断
            graphEventService.processEvent(userId, eventId, isRecurring);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ 处理事件 webhook 失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed");
        }
    }

    @PostMapping("/teams-transcript")
    public ResponseEntity<String> handleTeamsTranscript(HttpServletRequest request,
                                                        @RequestBody(required = false) String payload) {
        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ 收到 Graph 验证 POST 请求，返回 token: {}", token);
            return ResponseEntity.ok(token);
        }

        try {
            log.info("✅ 收到 transcript webhook，开始处理");
            TranscriptInfo transcriptInfo = payloadParserService.parseTranscriptInfo(payload);
                        // 自动关闭订阅
            String subscriptionId = payloadParserService.extractSubscriptionId(payload);


            if (subscriptionId != null) {
                graphService.deleteSubscription(subscriptionId);
                log.info("✅ 已关闭订阅 ID: {}", subscriptionId);
            }

            transcriptService.downloadTranscriptContent(
                    transcriptInfo.getUserId(),
                    transcriptInfo.getMeetingId(),
                    transcriptInfo.getTranscriptId(),
                    accessToken
            );
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ 处理 transcript webhook 失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed");
        }
    }
}
