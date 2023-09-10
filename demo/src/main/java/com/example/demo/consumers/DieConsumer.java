package com.example.demo.consumers;

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
public class DieConsumer {

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) throws Exception {
        boolean success = false;
        int retryCount = 3;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        while (!success && retryCount-- > 0){
            try {
                // 模拟兜底策略
                log.info("死信队列消息处理，data=: {}, deliveryTag = {}", data, deliveryTag);
                success = true;
                channel.basicAck(deliveryTag, false);
            }catch (Exception e){
                log.error("程序异常：{}", e.getMessage());
            }
        }
        // 达到最大重试次数后仍然消费失败
        if(!success){
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}