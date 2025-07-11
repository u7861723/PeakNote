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

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点执行
    public void renewSubscriptions() {
        List<GraphUserSubscription> subscriptions = subscriptionRepository.findAll();

        for (GraphUserSubscription sub : subscriptions) {
            // 检查距离过期时间是否小于 25 小时
            if (sub.getExpirationDateTime().isBefore(OffsetDateTime.now().plusHours(25))) {
                // 续订逻辑
                OffsetDateTime newExpire = OffsetDateTime.now().plusDays(3); // 例如续订三天
                graphService.renewSubscription(sub.getId(),newExpire);
                // 这里你也可以更新数据库里的 expirationDateTime
                sub.setExpirationDateTime(newExpire);
                subscriptionRepository.save(sub);
            }
        }
    }
}
