package com.example.demo.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;
import java.util.Objects;

public abstract class BaseProducer<T>{

    private String exchange;
    private String routingKey;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void init(String e, String r) {
        exchange = e;
        routingKey = r;
    }

    private void check() {
        if(Objects.isNull(exchange) || Objects.isNull(routingKey)) {
            throw new RuntimeException("please init BaseProducer first");
        }
    }
    public void sendMessage(T message) {
        check();
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
