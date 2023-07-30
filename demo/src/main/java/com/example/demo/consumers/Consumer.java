package com.example.demo.consumers;

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
public class Consumer {

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) throws Exception {
        boolean success = false;
        int retryCount = 3;
        System.out.println(message.toString());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        while (!success && retryCount-- > 0){
            try {
                // 处理消息
                log.info("收到消息: {}, deliveryTag = {}", data, deliveryTag);
                // 正常处理完毕，手动确认，此处不确认让他进入死信队列
//                success = true;
//                channel.basicAck(deliveryTag, false);
                Thread.sleep(3 * 1000L);
            }catch (Exception e){
                log.error("程序异常：{}", e.getMessage());
            }
        }
        // 达到最大重试次数后仍然消费失败
        if(!success){
            try {
                log.info("move to die queue");
                // 手动拒绝，移至死信队列
                /**
                 *
                 deliveryTag – the tag from the received AMQP.Basic.GetOk or AMQP.Basic.Deliver
                 multiple – true to reject all messages up to and including the supplied delivery tag; false to reject just the supplied delivery tag.
                 requeue – true if the rejected message(s) should be requeued rather than discarded/dead-lettered
                 */
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}