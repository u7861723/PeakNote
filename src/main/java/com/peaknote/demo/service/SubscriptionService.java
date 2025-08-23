package com.peaknote.demo.service;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.peaknote.demo.entity.GraphUserSubscription;
import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.UserRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.peaknote.demo.repository.GraphUserSubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final String webhookUrl;
    private final GraphService graphService;
    private final UserRepository userRepository;
    private final GraphUserSubscriptionRepository graphUserSubscriptionRepository;

    public SubscriptionService(
            @Value("${notification-url}") String webhookUrl,
            GraphService graphService,
            UserRepository userRepository,
            GraphUserSubscriptionRepository graphUserSubscriptionRepository
    ) {
        this.webhookUrl = webhookUrl;
        this.graphService = graphService;
        this.userRepository = userRepository;
        this.graphUserSubscriptionRepository = graphUserSubscriptionRepository;
    }

    /**
     * Create subscriptions for all users
     */
    public void createSubscriptionsForAllUsers() {
        try {
            List<TeamsUser> userIds = userRepository.findAll(); // You need to implement getting user ID list within tenant
            for (TeamsUser user : userIds) {
                createEventSubscription(user.getOid());
            }
        } catch (Exception e) {
            log.error("❌ Failed to create subscriptions: {}", e.getMessage(), e);
        }
    }

    /**
     * Create events subscription for a single user
     */
    public void createEventSubscription(String userId) {
        try {
            // subscription.changeType = "created,updated,deleted";
            String notificationUrl = webhookUrl + "webhook/notification";
            OffsetDateTime expireTime = OffsetDateTime.now().plusHours(72);
            String clientState = "yourCustomState";
            Subscription created = graphService.createEventSubscription(userId, notificationUrl, clientState, expireTime);

            log.info("✅ Successfully created subscription for user {}: {}", userId, created.id);

            // Save to database
            GraphUserSubscription graphUserSubscription = new GraphUserSubscription();
            graphUserSubscription.setId(created.id);
            graphUserSubscription.setExpirationDateTime(expireTime);
            graphUserSubscriptionRepository.save(graphUserSubscription);
        } catch (Exception e) {
            log.error("❌ Failed to create subscription for user {}: {}", userId, e.getMessage(), e);
        }
    }


    /**
     * List and delete all existing subscriptions
     */
    public void listAndDeleteAllSubscriptions() {
        try {
            SubscriptionCollectionPage subscriptions = graphService.listAllSubscriptions();

            if (subscriptions.getCurrentPage().isEmpty()) {
                log.info("✅ Currently no subscriptions");
                return;
            }

            for (Subscription sub : subscriptions.getCurrentPage()) {
                log.info("➡️ Preparing to delete subscription: ID={}, Resource={}, Expires={}",
                        sub.id, sub.resource, sub.expirationDateTime);

                graphService.deleteSubscription(sub.id);
                log.info("🗑️ Deleted subscription: {}", sub.id);
            }

            log.info("✅ All subscriptions deleted successfully");

        } catch (Exception e) {
            log.error("❌ Error occurred while deleting subscriptions: {}", e.getMessage(), e);
        }
    }

    /**
     * List all subscriptions only (without deleting)
     */
    public void listAllSubscriptions() {
        try {
            SubscriptionCollectionPage subscriptions = graphService.listAllSubscriptions();

            if (subscriptions.getCurrentPage().isEmpty()) {
                log.info("✅ Currently no subscriptions");
                return;
            }

            for (Subscription sub : subscriptions.getCurrentPage()) {
                log.info("🔎 Subscription info: ID={}, Resource={}, Expires={}",
                        sub.id, sub.resource, sub.expirationDateTime);
            }

        } catch (Exception e) {
            log.error("❌ Failed to get subscription list: {}", e.getMessage(), e);
        }
    }

    // Add subscription for transcript
    public void createTranscriptSubscription(String meetingId) {
        try {
            OffsetDateTime expireTime = OffsetDateTime.now().plusHours(23);
            String clientState = UUID.randomUUID().toString();
            String notificationUrl = webhookUrl + "webhook/teams-transcript"; // ✅ Modify to your own callback address

            Subscription created = graphService.createTranscriptSubscription(meetingId, notificationUrl, clientState, expireTime);

            log.info("✅ Successfully created transcript subscription for meeting {}, subscription ID: {}", meetingId, created.id);
        } catch (Exception e) {
            log.error("❌ Failed to create transcript subscription for meeting {}: {}", meetingId, e.getMessage(), e);
        }
    }
}
