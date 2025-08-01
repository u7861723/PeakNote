package com.peaknote.demo.controller;

import com.peaknote.demo.service.TeamsUserSyncService;
import com.peaknote.demo.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final TeamsUserSyncService teamsUserSyncService;
    private final SubscriptionService subscriptionService;

    public AdminController(TeamsUserSyncService teamsUserSyncService, SubscriptionService subscriptionService) {
        this.teamsUserSyncService = teamsUserSyncService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/sync-users")
    public String syncUsers() {
        try {
            teamsUserSyncService.syncUsers();
            return "✅ Users synced successfully";
        } catch (Exception e) {
            return "❌ Error syncing users: " + e.getMessage();
        }
    }

    @PostMapping("/create-subscriptions")
    public String createSubscriptions() {
        try {
            subscriptionService.createSubscriptionsForAllUsers();
            return "✅ Subscriptions created successfully";
        } catch (Exception e) {
            return "❌ Error creating subscriptions: " + e.getMessage();
        }
    }
}