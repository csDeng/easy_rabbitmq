package com.example.demo;

import com.example.demo.consumers.inner.DynamicBConsumer;
import com.example.demo.mq.BaseConsumerUtil;
import com.example.demo.mq.ConsumerParamInterface;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@SpringBootTest(classes = DemoApplication.class)
public class ReflectTest {

    @Resource
    private BaseConsumerUtil baseConsumerUtil;

    private final static HashMap<String, String> clz = new HashMap<String, String>(){{
        put("a", "com.example.demo.consumers.inner.DynamicAConsumer");
        put("b", DynamicBConsumer.class.getName());
    }};

    @Test
    public void test(){
        System.out.println(baseConsumerUtil.invokeByClassName(clz.get("a")));
        System.out.println(baseConsumerUtil.invokeByClassName(clz.get("b")));
    }

}
