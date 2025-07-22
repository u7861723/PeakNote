package com.peaknote.demo.service;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.peaknote.demo.entity.GraphUserSubscription;
import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.peaknote.demo.repository.GraphUserSubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final GraphService graphService;;
    private final UserRepository userRepository;
    private final GraphUserSubscriptionRepository graphUserSubscriptionRepository;

    /**
     * 为所有用户创建订阅
     */
    public void createSubscriptionsForAllUsers() {
        try {
            List<TeamsUser> userIds = userRepository.findAll(); // 这里需你自己实现获取租户内用户 ID 列表
            for (TeamsUser user : userIds) {
                createEventSubscription(user.getOid());
            }
        } catch (Exception e) {
            log.error("❌ 创建订阅失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 为单个用户创建 events 订阅
     */
    public void createEventSubscription(String userId) {
        try {
            // subscription.changeType = "created,updated,deleted";
            String notificationUrl = "https://68316233e15d.ngrok-free.app/webhook/notification";
            OffsetDateTime expireTime = OffsetDateTime.now().plusHours(2);
            String clientState = "yourCustomState";
            Subscription created = graphService.createEventSubscription(userId, notificationUrl, clientState, expireTime);

            log.info("✅ 成功为用户 {} 创建订阅: {}", userId, created.id);

            //存进数据库
            GraphUserSubscription graphUserSubscription = new GraphUserSubscription();
            graphUserSubscription.setId(created.id);
            graphUserSubscription.setExpirationDateTime(expireTime);
            graphUserSubscriptionRepository.save(graphUserSubscription);
        } catch (Exception e) {
            log.error("❌ 用户 {} 创建订阅失败: {}", userId, e.getMessage(), e);
        }
    }


    /**
     * 列出并删除所有现有订阅
     */
    public void listAndDeleteAllSubscriptions() {
        try {
            SubscriptionCollectionPage subscriptions = graphService.listAllSubscriptions();

            if (subscriptions.getCurrentPage().isEmpty()) {
                log.info("✅ 当前没有任何订阅");
                return;
            }

            for (Subscription sub : subscriptions.getCurrentPage()) {
                log.info("➡️ 准备删除订阅: ID={}, Resource={}, Expires={}",
                        sub.id, sub.resource, sub.expirationDateTime);

                graphService.deleteSubscription(sub.id);
                log.info("🗑️ 已删除订阅: {}", sub.id);
            }

            log.info("✅ 所有订阅已删除完成");

        } catch (Exception e) {
            log.error("❌ 删除订阅时出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 仅列出所有订阅（不删除）
     */
    public void listAllSubscriptions() {
        try {
            SubscriptionCollectionPage subscriptions = graphService.listAllSubscriptions();

            if (subscriptions.getCurrentPage().isEmpty()) {
                log.info("✅ 当前没有任何订阅");
                return;
            }

            for (Subscription sub : subscriptions.getCurrentPage()) {
                log.info("🔎 订阅信息: ID={}, Resource={}, Expires={}",
                        sub.id, sub.resource, sub.expirationDateTime);
            }

        } catch (Exception e) {
            log.error("❌ 获取订阅列表失败: {}", e.getMessage(), e);
        }
    }

    //添加对transcript的订阅
    public void createTranscriptSubscription(String meetingId) {
        try {
            OffsetDateTime expireTime = OffsetDateTime.now().plusHours(8);
            String clientState = UUID.randomUUID().toString();
            String notificationUrl = "https://68316233e15d.ngrok-free.app/webhook/teams-transcript"; // ✅ 修改成你自己的回调地址

            Subscription created = graphService.createTranscriptSubscription(meetingId, notificationUrl, clientState, expireTime);

            log.info("✅ 为会议 {} 创建 transcript 订阅成功，订阅 ID: {}", meetingId, created.id);
        } catch (Exception e) {
            log.error("❌ 会议 {} 创建 transcript 订阅失败: {}", meetingId, e.getMessage(), e);
        }
    }
}
