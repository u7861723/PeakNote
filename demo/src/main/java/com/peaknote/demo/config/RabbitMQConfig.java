package com.peaknote.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EVENT_QUEUE = "peaknote.event.queue";
    public static final String TRANSCRIPT_QUEUE = "peaknote.transcript.queue";
    public static final String EXCHANGE_NAME = "peaknote.exchange";
    public static final String EVENT_ROUTING_KEY = "peaknote.event.routingKey";
    public static final String TRANSCRIPT_ROUTING_KEY = "peaknote.transcript.routingKey";

    @Bean
    public Queue eventQueue() {
        return new Queue(EVENT_QUEUE, true);
    }

    @Bean
    public Queue transcriptQueue() {
        return new Queue(TRANSCRIPT_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding eventBinding(Queue eventQueue, DirectExchange exchange) {
        return BindingBuilder.bind(eventQueue).to(exchange).with(EVENT_ROUTING_KEY);
    }

    @Bean
    public Binding transcriptBinding(Queue transcriptQueue, DirectExchange exchange) {
        return BindingBuilder.bind(transcriptQueue).to(exchange).with(TRANSCRIPT_ROUTING_KEY);
    }
}
