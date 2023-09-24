package com.example.demo.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitCof {

    @Resource
    private MqKeys mqKeys;

    @Bean("normalQueue")
    public Queue normalQueue() {
        /**
         * 为普通队列绑定交换机
         */
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", mqKeys.DIE_EXCHANGE);
        args.put("x-dead-letter-routing-key", mqKeys.DIE_ROUTING_KEY);
        args.put("x-message-ttl", 1); // 队列中的消息未被消费则5秒后过期
        return new Queue(mqKeys.NORMAL_QUEUE, true, false, false, args);
    }

    @Bean("normalExchange")
    public Exchange normalExchange() {
        return new DirectExchange(mqKeys.NORMAL_EXCHANGE);
    }

    @Bean("normalBind")
    public Binding normalBinding(@Qualifier("normalQueue") Queue normalQueue, @Qualifier("normalExchange") Exchange normalExchange) {
        return BindingBuilder.bind(normalQueue).to(normalExchange).with(mqKeys.ROUTING_KEY).noargs();
    }

    @Bean("dynamicQueue")
    public Queue dynamicQueue() {
        return new Queue(mqKeys.DYNAMIC_QUEUE, true, false, false);
    }

    @Bean("dynamicBind")
    public Binding dynamicBinding(@Qualifier("dynamicQueue") Queue dynamicQueue,  @Qualifier("normalExchange") Exchange normalExchange) {
        return BindingBuilder.bind(dynamicQueue).to(normalExchange).with(mqKeys.DYNAMIC_ROUTING_KEY).noargs();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("dieQueue")
    public Queue dlQueue() {
        return new Queue(mqKeys.DIE_QUEUE, true, false, false);
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean("dieExchange")
    public Exchange dlExchange() {
        return new DirectExchange(mqKeys.DIE_EXCHANGE);
    }

    @Bean("dieBind")
    public Binding dlBinding(@Qualifier("dieQueue") Queue dlQueue, @Qualifier("dieExchange") Exchange dlExchange) {
        return BindingBuilder.bind(dlQueue).to(dlExchange).with(mqKeys.DIE_ROUTING_KEY).noargs();
    }



    @Resource
    private ConnectionFactory connectionFactory;

    @Bean
    public RabbitTemplate rabbitTemplate() {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMandatory(true);


        /**
         * 消费者确认收到消息后，手动ack回调处理
         * spring.rabbitmq.publisher-confirm-type=simple
         */
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause)->{
            if(!ack) {
                log.info("消息投递到交换机失败，correlationData={} ,ack={}, cause={}", correlationData == null ? "null" : correlationData.getId(), ack, cause);
            } else {
                log.info("消息成功投递到交换机，correlationData={} ,ack={}, cause={}", correlationData == null ? "null" : correlationData.getId(), ack, cause);
            }
        });

        /**
         * 消息投递到队列失败回调处理
         * spring.rabbitmq.listener.direct.acknowledge-mode=manual
         * spring.rabbitmq.publisher-returns=true
         */
        rabbitTemplate.setReturnsCallback((returnedMessage)->{
            Message message = returnedMessage.getMessage();
            log.error("分发到到队列失败, body->{}", message.getBody());
        });
        return rabbitTemplate;
    }
}