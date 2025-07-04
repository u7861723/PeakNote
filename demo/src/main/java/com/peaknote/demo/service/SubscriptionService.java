package com.peaknote.demo.service;

import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.UserRepository;

import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final GraphServiceClient<Request> graphClient;
    private final UserRepository userRepository;

    public SubscriptionService(@Qualifier("webhookGraphClient")GraphServiceClient<Request> graphClient, UserRepository userRepository) {
        this.graphClient = graphClient;
        this.userRepository = userRepository;
    }

    /**
     * 为所有用户创建订阅
     */
    public void createSubscriptionsForAllUsers() {
        try {
            List<TeamsUser> userIds = userRepository.findAll(); // 这里需你自己实现获取租户内用户 ID 列表
            for (TeamsUser user : userIds) {
                String userId = user.getOid();
                createEventSubscription(userId);
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
            Subscription subscription = new Subscription();
            // subscription.changeType = "created,updated,deleted";
            subscription.changeType = "created";
            subscription.notificationUrl = "https://2c12-123-51-17-200.ngrok-free.app/webhook/notification";
            subscription.resource = "/users/" + userId + "/events";
            subscription.expirationDateTime = OffsetDateTime.now().plusHours(2); // 最多可设置 4230 分钟
            subscription.clientState = "yourCustomState"; // 可随意设置用于验证

            Subscription createdSubscription = graphClient.subscriptions()
                    .buildRequest()
                    .post(subscription);

            log.info("✅ 成功为用户 {} 创建订阅: {}", userId, createdSubscription.id);
        } catch (Exception e) {
            log.error("❌ 用户 {} 创建订阅失败: {}", userId, e.getMessage(), e);
        }
    }

     /**
     * 查看并删除所有现有订阅
     */
    public void listAndDeleteAllSubscriptions() {
        try {
            // 获取所有订阅
            SubscriptionCollectionPage subscriptions = graphClient.subscriptions()
                    .buildRequest()
                    .get();

            if (subscriptions.getCurrentPage().isEmpty()) {
                log.info("✅ 当前没有任何订阅");
                return;
            }

            // 遍历并删除
            for (Subscription sub : subscriptions.getCurrentPage()) {
                log.info("➡️ 找到订阅: ID={}, Resource={}, Expires={}", 
                        sub.id, sub.resource, sub.expirationDateTime);

                // 删除该订阅
                graphClient.subscriptions(sub.id)
                        .buildRequest()
                        .delete();
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
            SubscriptionCollectionPage subscriptions = graphClient.subscriptions()
                    .buildRequest()
                    .get();

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
            Subscription subscription = new Subscription();
            subscription.changeType = "created";
            subscription.notificationUrl = "https://2c12-123-51-17-200.ngrok-free.app/webhook/teams-transcript";
            subscription.resource = "/communications/onlineMeetings/" + meetingId + "/transcripts";;
            subscription.expirationDateTime = OffsetDateTime.now().plusHours(1); // 最多支持 1 天，先设置 1 小时测试
            subscription.clientState = UUID.randomUUID().toString(); // 可用于校验回调

            Subscription createdSub = graphClient.subscriptions()
                    .buildRequest()
                    .post(subscription);

            System.out.println("✅ 订阅成功，ID: " + createdSub.id + ", Expire: " + createdSub.expirationDateTime);
        } catch (Exception e) {
            System.err.println("❌ 创建订阅失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
