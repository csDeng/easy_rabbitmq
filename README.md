

在现代分布式系统中，消息队列扮演着至关重要的角色。它们可以实现应用程序之间的异步通信，并确保数据的可靠传输和处理。而在这个领域中，RabbitMQ作为一种强大而受欢迎的消息队列解决方案，具备了高效、可靠和灵活的特性。

然而，即使使用了RabbitMQ，我们仍然会遇到一些不可预料的情况，例如消费者无法处理某些消息、消息过期或者队列溢出等。为了解决这些问题，RabbitMQ引入了死信队列（Dead Letter Queue）的概念，为开发人员提供了一种有效的错误处理机制。

那么，究竟什么是死信队列呢？

本文结合Spring Boot使用RabbitMQ的死信队列，着重从是什么、为什么、怎么用几个方面对死信队列进行简单介绍。

## 1. 是什么：

- 死信队列（`Dead Letter Queue`）是一种特殊的消息队列，用于存储无法被消费的消息。
- 当消息满足某些条件无法被正常消费时，将被发送到死信队列中进行处理。
- 死信队列提供了一种**延迟处理**、异常消息处理等场景的解决方案。

## 2. 为什么

- 用来处理消费者无法正确处理的消息，**避免消息丢失或积压**。
- **实现延迟消息处理**，例如订单超时未支付，可以将该消息发送到死信队列，然后再进行后续处理。
- 用于实现消息重试机制，当消费者处理失败时，将消息重新发送到死信队列进行重试。
- 提高了系统的可伸缩性和容错性，能够应对高并发和异常情况。

## 3. 怎么用

1. 在Spring Boot中配置和使用死信队列：
   - 首先，在`pom.xml`文件中添加`RabbitMQ`的依赖项。
   - 然后，在`application.properties`文件中配置RabbitMQ连接信息。
   - 接下来，创建生产者和消费者代码，并通过注解将队列和交换机进行绑定。
   - 在队列的声明中添加死信队列的相关参数，如`x-dead-letter-exchange`和`x-dead-letter-routing-key`等。
   - 最后，在消费者中编写处理消息的逻辑，包括对异常消息进行处理，并设置是否重新发送到死信队列。



> 简而言之，死信队列可以认为是一个正常队列的备用队列（或者说是兜底队列），当正常队列的消息无法消费的时候mq会重新把该消息发送到死信交换机，由死信交换机根据路由键将消息投递到备用队列，启动服务备用方案。
>
> 消息从正常队列到死信队列的三种情况：
>
> 1、消息被否定确认使用 `channel.basicNack` 或 `channel.basicReject` ，并且此时`requeue` 属性被设置为`false`。
>
> 2、消息在队列中的时间超过了设置的TTL()）时间。
>
> 3、消息数量超过了队列的容量限制()。
>
> 当一个队列中的消息满足上述三种情况任一个时，改消息就会从原队列移至死信队列，若改队列没有绑定死信队列则消息被丢弃。

## 4. 实战

以下是一个简单的Spring Boot集成RabbitMQ的死信队列示例代码：

* 配置

```properties
spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.port=5672
spring.rabbitmq.username=rabbit
spring.rabbitmq.password=123456
# 开启消费者手动确认
spring.rabbitmq.listener.type=direct

# 发送到队列失败时的手动处理
spring.rabbitmq.listener.direct.acknowledge-mode=manual
spring.rabbitmq.publisher-returns=true

# 发送到交换机手动确认
spring.rabbitmq.publisher-confirm-type=simple
```

* 配置类

```java
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
        args.put("x-message-ttl", 1000); // 队列中的消息未被消费则1秒后过期
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
```

* 生产者类

```java
@Component
public class Producer {

    @Resource
    private  MqKeys mqKeys;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(mqKeys.NORMAL_EXCHANGE, mqKeys.ROUTING_KEY, message);
    }
}
```

* 消费者类

```java
@Component
@RabbitListener(queues = "normal.queue")
@Slf4j
public class Consumer {

    @RabbitHandler
    public void handleMessage(String data, Message message, Channel channel) {
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
```

* 死信队列的兜底策略类

```java
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

```

以上代码演示了如何在Spring Boot中配置一个普通队列和一个死信队列，然后通过生产者发送消息到普通队列，在消费者中处理消息，并模拟了当发生异常时将消息重新发送到死信队列。



## 参考连接

* [rabbit 官网][Dead Letter Exchanges — RabbitMQ](https://www.rabbitmq.com/dlx.html)

* [V1具体代码仓库](https://github.com/csDeng/easy_rabbitmq/tree/7bd37246a6d48666951ecad72c30c00978bbc50e)

## 升级

然后在rabbitmq中使用了死信队列作为兜底的情况下，如果为每一个消费类型绑定一个队列，可能会存在部分任务消费频率过低，从而导致连接空等，造成服务资源的极大浪费。升级采用“线程池+消息动态”尝试解决上述资源浪费的连接痛点。详情请看[V2](./README_V2.md)

