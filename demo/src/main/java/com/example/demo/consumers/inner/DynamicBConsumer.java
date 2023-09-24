package com.example.demo.consumers.inner;

import com.example.demo.mq.BaseConsumer;
import com.example.demo.mq.ConsumerParamInterface;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DynamicBConsumer implements ConsumerParamInterface<String> {

    @Override
    public boolean handler(String data) throws InterruptedException {
        log.info("b 消费 : {}", data);
        return true;
    }
}