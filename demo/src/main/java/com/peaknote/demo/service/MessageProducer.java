package com.peaknote.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.peaknote.demo.config.RabbitMQConfig.EXCHANGE_NAME;
import static com.peaknote.demo.config.RabbitMQConfig.EVENT_ROUTING_KEY;
import static com.peaknote.demo.config.RabbitMQConfig.TRANSCRIPT_ROUTING_KEY;

@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEventMessage(String message) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, EVENT_ROUTING_KEY, message);
    }

    public void sendTranscriptMessage(String message) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, TRANSCRIPT_ROUTING_KEY, message);
    }
}
