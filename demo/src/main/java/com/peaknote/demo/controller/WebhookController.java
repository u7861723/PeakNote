package com.peaknote.demo.controller;

import com.peaknote.demo.service.GraphEventService;
import com.peaknote.demo.service.GraphService;
import com.peaknote.demo.service.MessageProducer;
import com.peaknote.demo.service.PayloadParserService;
import com.peaknote.demo.service.SubscriptionService;
import com.peaknote.demo.service.TranscriptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final GraphEventService graphEventService;
    private final TranscriptService transcriptService;
    private final PayloadParserService payloadParserService;
    private final SubscriptionService subscriptionService;
    private final MessageProducer messageProducer;
    private final GraphService graphService;

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
            messageProducer.sendEventMessage(payload);
            // String userId = payloadParserService.extractUserIdFromEventPayload(payload);
            // String eventId = payloadParserService.extractEventIdFromEventPayload(payload);
            // boolean isRecurring = (graphService.getUserEvent(userId, eventId).recurrence!=null); // 示例判断
            // graphEventService.processEvent(userId, eventId, isRecurring);

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

            messageProducer.sendTranscriptMessage(payload);
            // TranscriptInfo transcriptInfo = payloadParserService.parseTranscriptInfo(payload);
            //             // 自动关闭订阅
            // String subscriptionId = payloadParserService.extractSubscriptionId(payload);


            // if (subscriptionId != null) {
            //     graphService.deleteSubscription(subscriptionId);
            //     log.info("✅ 已关闭订阅 ID: {}", subscriptionId);
            // }

            // transcriptService.downloadTranscriptContent(
            //         transcriptInfo.getUserId(),
            //         transcriptInfo.getMeetingId(),
            //         transcriptInfo.getTranscriptId(),
            //         accessToken
            // );
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ 处理 transcript webhook 失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed");
        }
    }

    
    /**
     * 接收 Graph 生命周期通知的回调接口（lifecycleNotificationUrl）
     */
    @PostMapping("/teams-lifecycle")
    public ResponseEntity<String> handleLifecycleNotification(HttpServletRequest request,
                                                              @RequestBody(required = false) String payload) {
        String validationToken = request.getParameter("validationToken");
        if (validationToken != null) {
            // Graph 验证流程，要求返回验证 token
            log.info("✅ 收到 Graph 生命周期验证请求，返回 token: {}", validationToken);
            return ResponseEntity.ok(validationToken);
        }

        // 没有 validationToken，就是生命周期事件通知（例如续订、异常提醒）
        log.info("✅ 收到 Graph 生命周期事件通知，内容: {}", payload);

        // TODO: 这里可以解析 payload 并根据事件类型进行处理，例如自动续订订阅
        // 例如根据 payload 中的 lifecycleEvent 字段判断
        // {
        //   "lifecycleEvent": "reauthorizationRequired",
        //   "subscriptionId": "...",
        //   ...
        // }
        //
        // 你可以调用后端服务重新调用订阅接口更新 expirationDateTime

        return ResponseEntity.ok("Received lifecycle event");
    }
}
