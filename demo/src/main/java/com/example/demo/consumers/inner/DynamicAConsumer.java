package com.example.demo.consumers.inner;

import com.example.demo.mq.ConsumerParamInterface;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicAConsumer implements ConsumerParamInterface<String> {

    @Override
    public boolean handler(String data) throws InterruptedException {
        log.info("a 消费-{}", data);
        return true;
    }
}