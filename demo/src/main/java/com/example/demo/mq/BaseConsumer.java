package com.example.demo.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
@Component
public abstract class BaseConsumer<T> {
//    private Channel channel;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        /*
        corePoolSize：核心线程数大小,不管它们创建以后是不是空闲的。线程池需要保持 corePoolSize 数量的线程，除非设置了 allowCoreThreadTimeOut；
        maximumPoolSize：最大线程数,线程池中最多允许创建 maximumPoolSize 个线程;
        keepAliveTime：存活时间,如果经过 keepAliveTime 时间后，超过核心线程数的线程还没有接受到新的任务，那就回收;
        unit： keepAliveTime 的时间单位;
        workQueue：存放待执行任务的队列：当提交的任务数超过核心线程数大小后，再提交的任务就存放在这里。它仅仅用来存放被 execute 方法提交的 Runnable 任务;阻塞队列成员表:
        */
        executorService = new ThreadPoolExecutor(
                0,
                10,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    public void handleMessage(T data, Message message, Channel channel, ConsumerParamInterface<T> consume) throws IOException, ExecutionException, InterruptedException {
        // 注意 channel 链接不可以赋值给其他变量进行封装确认，不然只能消费一条消息
        // 具体原因是channel 其实是一个socket 的抽象，属于唯一对象
        // this.channel = channel;

        executorService.submit(() -> {
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
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
