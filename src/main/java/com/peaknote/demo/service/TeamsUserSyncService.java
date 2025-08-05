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
        List<User> graphUsers = graphService.fetchAllUsers();

        for (User gUser : graphUsers) {
            if (gUser.id == null || (gUser.mail == null && gUser.userPrincipalName == null)) continue;

            TeamsUser user = new TeamsUser();
            user.setOid(gUser.id);
            user.setEmail(gUser.mail != null ? gUser.mail : gUser.userPrincipalName);

            UserRepository.save(user);  // insert or update
        }
    }
}
