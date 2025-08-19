package com.peaknote.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.UserRepository;
import com.microsoft.graph.models.User;

@Service
public class TeamsUserSyncService {

    private final GraphService graphService;
    private final UserRepository UserRepository;

    public TeamsUserSyncService(GraphService graphService, UserRepository UserRepository) {
        this.graphService = graphService;
        this.UserRepository = UserRepository;
    }

    public void syncUsers() {
        try {
            List<User> graphUsers = graphService.fetchAllUsers();
            
            if (graphUsers == null || graphUsers.isEmpty()) {
                System.out.println("⚠️ No user data retrieved");
                return;
            }

            int successCount = 0;
            int errorCount = 0;
            
            for (User gUser : graphUsers) {
                try {
                    if (gUser.id == null || (gUser.mail == null && gUser.userPrincipalName == null)) {
                        System.out.println("⚠️ Skipping invalid user data: id=" + gUser.id);
                        continue;
                    }

                    TeamsUser user = new TeamsUser();
                    user.setOid(gUser.id);
                    user.setEmail(gUser.mail != null ? gUser.mail : gUser.userPrincipalName);

                    UserRepository.save(user);  // insert or update
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ Failed to save user data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✅ User sync complete: " + successCount + " successful, " + errorCount + " failed");
        } catch (Exception e) {
            System.err.println("❌ User sync service failed: " + e.getMessage());
            e.printStackTrace();
            throw new PeakNoteException("User sync failed", e);
        }
    }
}
