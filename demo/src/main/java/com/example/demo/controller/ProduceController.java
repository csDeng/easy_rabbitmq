package com.example.demo.controller;

import com.example.demo.producers.DynamicProducer;
import com.example.demo.producers.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

@RestController
@Slf4j
public class ProduceController {

    @Resource
    private Producer producer;

    @Resource
    private DynamicProducer dynamicProducer;


    @GetMapping("/test")
    public String test() throws InterruptedException {
        for(int i=0; i<10; i++) {
            producer.sendMessage("die msg");
            Thread.sleep(1000L);
            log.info("produce----");
        }

        return "ok";
    }

    @GetMapping("/dynamic/{type}")
    public String dynamic(@PathVariable(value = "type") Integer type) throws InterruptedException {
        log.info("type={}",type);
        String c = "a";
        if(type > 1) {
            c = "b";
        }
        dynamicProducer.sendMessage(c);
        log.info("dynamic produce----");
        return "ok";
    }

}
