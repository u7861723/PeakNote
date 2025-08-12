package com.peaknote.demo.controller;

import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.UserRepository;
import com.peaknote.demo.service.SubscriptionService;
import com.peaknote.demo.service.GraphService;
import com.peaknote.demo.service.TeamsUserSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/debug")
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);
    
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final GraphService graphService;
    private final TeamsUserSyncService teamsUserSyncService;

    @GetMapping("/users")
    public String checkUsers() {
        try {
            List<TeamsUser> users = userRepository.findAll();
            log.info("Found {} users in database", users.size());
            
            StringBuilder result = new StringBuilder();
            result.append("Users in database: ").append(users.size()).append("\n");
            
            for (TeamsUser user : users) {
                result.append("- OID: ").append(user.getOid())
                      .append(", Email: ").append(user.getEmail()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error checking users: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/subscriptions")
    public String checkSubscriptions() {
        try {
            subscriptionService.listAllSubscriptions();
            return "Check logs for subscription details";
        } catch (Exception e) {
            log.error("Error listing subscriptions: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/create-subscriptions")
    public String createSubscriptions() {
        try {
            subscriptionService.createSubscriptionsForAllUsers();
            return "Subscription creation attempted - check logs";
        } catch (Exception e) {
            log.error("Error creating subscriptions: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/test-auth")
    public String testAuth() {
        try {
            String token = graphService.getAccessToken();
            return "Authentication successful - token length: " + token.length();
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            return "Auth Error: " + e.getMessage();
        }
    }

    @PostMapping("/sync-users")
    public String syncUsers() {
        try {
            teamsUserSyncService.syncUsers();
            List<TeamsUser> users = userRepository.findAll();
            return "User sync completed. Total users: " + users.size();
        } catch (Exception e) {
            log.error("Error syncing users: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}