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
        log.info("✅ Received Graph validation GET request, returning token: {}", token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/notification")
    public ResponseEntity<String> handleNotification(HttpServletRequest request,
                                                     @RequestBody(required = false) String payload) {
        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ Received Graph validation POST request, returning token: {}", token);
            return ResponseEntity.ok(token);
        }

        try {
            log.info("✅ Received event webhook, starting processing");
            messageProducer.sendEventMessage(payload);
            // String userId = payloadParserService.extractUserIdFromEventPayload(payload);
            // String eventId = payloadParserService.extractEventIdFromEventPayload(payload);
            // boolean isRecurring = (graphService.getUserEvent(userId, eventId).recurrence!=null); // Example judgment
            // graphEventService.processEvent(userId, eventId, isRecurring);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ Failed to process event webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed");
        }
    }

    @PostMapping("/teams-transcript")
    public ResponseEntity<String> handleTeamsTranscript(HttpServletRequest request,
                                                        @RequestBody(required = false) String payload) {
        String token = request.getParameter("validationToken");
        if (token != null) {
            log.info("✅ Received Graph validation POST request, returning token: {}", token);
            return ResponseEntity.ok(token);
        }

        try {
            log.info("✅ Received transcript webhook, starting processing");

            messageProducer.sendTranscriptMessage(payload);
            // TranscriptInfo transcriptInfo = payloadParserService.parseTranscriptInfo(payload);
            //             // Automatically close subscription
            // String subscriptionId = payloadParserService.extractSubscriptionId(payload);


            // if (subscriptionId != null) {
            //     graphService.deleteSubscription(subscriptionId);
            //     log.info("✅ Closed subscription ID: {}", subscriptionId);
            // }

            // transcriptService.downloadTranscriptContent(
            //         transcriptInfo.getUserId(),
            //         transcriptInfo.getMeetingId(),
            //         transcriptInfo.getTranscriptId(),
            //         accessToken
            // );
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ Failed to process transcript webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed");
        }
    }

    
    /**
     * Callback interface for receiving Graph lifecycle notifications (lifecycleNotificationUrl)
     */
    @PostMapping("/teams-lifecycle")
    public ResponseEntity<String> handleLifecycleNotification(HttpServletRequest request,
                                                              @RequestBody(required = false) String payload) {
        String validationToken = request.getParameter("validationToken");
        if (validationToken != null) {
            // Graph validation process, requires returning validation token
            log.info("✅ Received Graph lifecycle validation request, returning token: {}", validationToken);
            return ResponseEntity.ok(validationToken);
        }

        // No validationToken means lifecycle event notification (e.g., renewal, exception reminder)
        log.info("✅ Received Graph lifecycle event notification, content: {}", payload);

        // TODO: Here you can parse payload and process according to event type, such as auto-renewal subscription
        // For example, judge based on lifecycleEvent field in payload
        // {
        //   "lifecycleEvent": "reauthorizationRequired",
        //   "subscriptionId": "...",
        //   ...
        // }
        //
        // You can call backend service to re-call subscription interface to update expirationDateTime

        return ResponseEntity.ok("Received lifecycle event");
    }
}
