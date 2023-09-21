package com.example.demo.consumers;

import com.example.demo.mq.BaseConsumer;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "die.queue")
@Slf4j
public class DieConsumer extends BaseConsumer<String> {

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) throws Exception {
        super.handleMessage(data,message, channel, d->{
            log.info("死信队列收到消息: {}", d);
            return true;
        });
    }
}