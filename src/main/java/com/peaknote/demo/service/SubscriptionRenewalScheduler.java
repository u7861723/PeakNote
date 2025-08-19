package com.peaknote.demo.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.peaknote.demo.entity.GraphUserSubscription;
import com.peaknote.demo.repository.GraphUserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionRenewalScheduler {

    private final GraphUserSubscriptionRepository subscriptionRepository;
    private final GraphService graphService;

    @Scheduled(cron = "0 0 2 * * ?") // Execute daily at 2:00 AM
    public void renewSubscriptions() {
        List<GraphUserSubscription> subscriptions = subscriptionRepository.findAll();

        for (GraphUserSubscription sub : subscriptions) {
            // Check if time to expiration is less than 25 hours
            if (sub.getExpirationDateTime().isBefore(OffsetDateTime.now().plusHours(25))) {
                // Renewal logic
                OffsetDateTime newExpire = OffsetDateTime.now().plusDays(3); // For example, renew for 3 days
                graphService.renewSubscription(sub.getId(),newExpire);
                // Here you can also update the expirationDateTime in the database
                sub.setExpirationDateTime(newExpire);
                subscriptionRepository.save(sub);
            }
        }
    }
}
