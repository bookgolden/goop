package com.laoyang.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yyy
 * @Date 2020-07-13 21:30
 * @Email yangyouyuhd@163.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class mqAdminTest {


    @Autowired
    private AmqpAdmin amqpAdmin;


    /**
     * 1、如何创建Exchange、Queue、Binding
     *      1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {

        Exchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功：","hello-java-exchange");
    }


    /**
     * String name,         队列名
     * boolean durable,     是否持久化
     * boolean exclusive,   是否只允许一人连接 、排他队列
     * boolean autoDelete,  自动删除
     * @Nullable Map<String, Object> arguments
     */
    @Test
    public void testCreateQueue() {
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：","hello-java-queue");
    }


    /**
     * String destination,          目的地队列或子交换机的名字
     * Binding.DestinationType destinationType,   目的地类型、队列/交换机
     * String exchange,             交换机名称
     * String routingKey,            路由键
     * Map<String, Object> arguments    自定义参
     */
    @Test
    public void createBinding() {

        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功：","hello-java-binding");

    }
}
