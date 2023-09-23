package com.example.demo.consumers;

import com.example.demo.mq.BaseConsumer;
import com.example.demo.mq.ConsumerParamInterface;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "normal.queue")
@Slf4j
public class Consumer extends BaseConsumer<String> {

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) throws IOException {
        super.handleMessage(data, message, channel, d->{
            log.info("普通队列收到消息: {}, 但是拒绝让他进入死信队列.", data);
            return false;
        });
    }
}