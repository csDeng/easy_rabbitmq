package com.example.demo.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public abstract class BaseConsumer<T> {
//    private Channel channel;

    public void handleMessage(T data, Message message, Channel channel, ConsumerParamInterface<T> consume) throws IOException {
        // 注意 channel 链接不可以赋值给其他变量进行封装确认，不然只能消费一条消息
        // 具体原因是channel 其实是一个socket 的抽象，属于唯一对象
        // this.channel = channel;
        int retryCount = 3;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        while (retryCount-- > 0) {
            try {
                // 处理消息
                boolean handler = consume.handler(data);
                if (handler) {
                    channel.basicAck(deliveryTag, false);
                    return;
                }
            } catch (Exception e) {
                log.error("程序异常：{}", e.getMessage());
            }
        }

        log.info("move to die queue");
        // 手动拒绝，移至死信队列
        /**
         *
         deliveryTag – the tag from the received AMQP.Basic.GetOk or AMQP.Basic.Deliver
         multiple – true to reject all messages up to and including the supplied delivery tag; false to reject just the supplied delivery tag.
         requeue – true if the rejected message(s) should be requeued rather than discarded/dead-lettered
         */
        channel.basicNack(deliveryTag, false, false);
    }

}
