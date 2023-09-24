package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MqKeys {

    public final String NORMAL_EXCHANGE = "normal.exchange";

    public final String NORMAL_QUEUE = "normal.queue";

    public final String ROUTING_KEY = "normal.routingKey";

    public final String DIE_EXCHANGE = "die.exchange";

    public final String DIE_QUEUE = "die.queue";

    public final String DIE_ROUTING_KEY = "die.routingKey";

    public final String DYNAMIC_QUEUE = "normal.dynamic";
    public final String DYNAMIC_ROUTING_KEY = "dynamic.routingKey";

}
