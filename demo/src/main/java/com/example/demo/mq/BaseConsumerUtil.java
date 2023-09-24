package com.example.demo.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaseConsumerUtil {

    public boolean invokeByClassName(String name) {
        try {
            Class<?> targetClass = Class.forName(name);
            Class<?> targetInterface = ConsumerParamInterface.class;
            System.out.println(targetClass);
            System.out.println(targetInterface);
            if (targetInterface.isAssignableFrom(targetClass)) {
                ConsumerParamInterface instance = (ConsumerParamInterface<?>)targetClass.getDeclaredConstructor().newInstance();
                return instance.handler("hello");
            } else {
                log.error("{} does not implement the interface.", targetClass);
            }
        } catch (ClassNotFoundException e) {
            log.error("Class or interface not found."+e.getMessage());
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
        }
        return false;
    }

}
