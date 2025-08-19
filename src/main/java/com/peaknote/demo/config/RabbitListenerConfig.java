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

        // Configure concurrent consumer count here
        factory.setConcurrentConsumers(3);      // Initial concurrent consumer count, e.g., 3 threads
        factory.setMaxConcurrentConsumers(10);  // Maximum concurrent consumer count, e.g., up to 10 threads

        // Configure how many messages each consumer can prefetch at once (optional)
        factory.setPrefetchCount(3);

        // ✅ Configure retry interceptor
        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder
                .stateless()
                .maxAttempts(2)                        // Maximum attempt count (1 original + 2 retries)
                .backOffOptions(10000, 2.0, 30000)    // Initial 10s, multiply by 2, maximum 30s
                .recoverer(new RejectAndDontRequeueRecoverer()) // Discard directly after retry ends
                .build();

        factory.setAdviceChain(interceptor);

        // ✅ If you want JSON messages, you can add a converter
        // factory.setMessageConverter(new Jackson2JsonMessageConverter());

        return factory;
    }
}

