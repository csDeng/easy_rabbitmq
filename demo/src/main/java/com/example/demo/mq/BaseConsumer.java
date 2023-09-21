package com.example.demo.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public abstract class BaseConsumer<T> {
    private Channel channel;
    private Long deliveryTag;

    private boolean success = false;

    public void handleMessage(T data, Message message, Channel channel, ConsumerParamInterface<T> consume) {
        this.channel = channel;
        int retryCount = 3;
        this.deliveryTag = message.getMessageProperties().getDeliveryTag();
        while (!success && retryCount-- > 0) {
            try {
                // 处理消息
                boolean handler = consume.handler(data);
                if (handler) {
                    ack();
                    break;
                }
            } catch (Exception e) {
                log.error("程序异常：{}", e.getMessage());
            }
        }
        // 达到最大重试次数后仍然消费失败
        if (!success) {
            try {
                log.info("move to die queue");
                // 手动拒绝，移至死信队列
                nack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    protected void ack() throws IOException {
        check();
        channel.basicAck(deliveryTag, false);
        success = true;
    }

    protected void nack() throws IOException {
        check();
        /**
         *
         deliveryTag – the tag from the received AMQP.Basic.GetOk or AMQP.Basic.Deliver
         multiple – true to reject all messages up to and including the supplied delivery tag; false to reject just the supplied delivery tag.
         requeue – true if the rejected message(s) should be requeued rather than discarded/dead-lettered
         */
        channel.basicNack(deliveryTag, false, false);

    }

    private void check() {
        if (Objects.isNull(channel) || Objects.isNull(deliveryTag)) {
            throw new RuntimeException("please init base consumer...");
        }
    }

}
