package com.example.demo.controller;

import com.example.demo.producers.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class ProduceController {

    @Resource
    private Producer producer;


    @GetMapping("/test")
    public String test() throws InterruptedException {
        for(int i=0; i<10; i++) {
            producer.sendMessage("die msg");
            Thread.sleep(1000L);
            log.info("produce----");
        }


        return "ok";
    }

}
