package com.example.demo.consumers;

import com.example.demo.consumers.inner.DynamicAConsumer;
import com.example.demo.consumers.inner.DynamicBConsumer;
import com.example.demo.mq.BaseConsumer;
import com.example.demo.mq.BaseConsumerUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Component
@RabbitListener(queues = "normal.dynamic")
@Slf4j
public class DynamicConsumer extends BaseConsumer<String> {

    @Resource
    private BaseConsumerUtil baseConsumerUtil;

    private final static HashMap<String, String> clz = new HashMap<String, String>(){{
        put("a", DynamicAConsumer.class.getName());
        put("b", DynamicBConsumer.class.getName());
    }};

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) throws IOException, ExecutionException, InterruptedException {
        handleMessage(data, message, channel, d->{
            String s = clz.getOrDefault(d,"");
            log.info("d动态队列收到消息: {}", data);
            return baseConsumerUtil.invokeByClassName(s);
        });
    }
}