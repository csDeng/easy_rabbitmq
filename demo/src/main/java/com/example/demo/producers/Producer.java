package com.example.demo.producers;

import com.example.demo.config.MqKeys;
import com.example.demo.mq.BaseProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Producer extends BaseProducer<String> {

    @Resource
    private MqKeys mqKey;

    public void sendMessage(String message) {
        super.init(mqKey.NORMAL_EXCHANGE, mqKey.ROUTING_KEY);
        super.sendMessage(message);
    }
}