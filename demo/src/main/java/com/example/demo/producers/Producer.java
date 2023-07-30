package com.example.demo.producers;

import com.example.demo.config.MqKeys;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Producer {

    @Resource
    private  MqKeys mqKeys;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(mqKeys.NORMAL_EXCHANGE, mqKeys.ROUTING_KEY, message);
    }
}