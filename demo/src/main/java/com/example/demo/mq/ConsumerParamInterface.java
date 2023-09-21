package com.example.demo.mq;

@FunctionalInterface
public interface ConsumerParamInterface<T> {
    boolean handler(T data);
}
