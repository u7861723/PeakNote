package com.peaknote.demo.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


@Configuration
public class RabbitListenerConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 这里配置并发消费者数量
        factory.setConcurrentConsumers(3);      // 初始并发消费者数量，比如 3 个线程
        factory.setMaxConcurrentConsumers(10);  // 最大并发消费者数量，比如最多 10 个线程

        // 配置每个消费者一次最多预取多少条消息（可选）
        factory.setPrefetchCount(3);

        // ✅ 配置重试拦截器
        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder
                .stateless()
                .maxAttempts(2)                        // 最大尝试次数（1次原始 + 2次重试）
                .backOffOptions(10000, 2.0, 30000)    // 初始 10s，乘以 2，最大 30s
                .recoverer(new RejectAndDontRequeueRecoverer()) // 重试结束后直接丢弃
                .build();

        factory.setAdviceChain(interceptor);

        // ✅ 如果你要 JSON 消息，可以加上转换器
        // factory.setMessageConverter(new Jackson2JsonMessageConverter());

        return factory;
    }
}

