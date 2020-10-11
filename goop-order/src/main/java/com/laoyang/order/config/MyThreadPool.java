package com.laoyang.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author yyy
 */
@Data
@ConfigurationProperties(prefix = "cart.thread")
@Configuration
public class MyThreadPool {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize,
                maxSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());

        return executor;
    }
}
