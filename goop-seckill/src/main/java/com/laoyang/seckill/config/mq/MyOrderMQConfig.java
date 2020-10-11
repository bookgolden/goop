package com.laoyang.seckill.config.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author yyy
 * @Date 2020-07-18 12:30
 * @Email yangyouyuhd@163.com
 * @apiNote 死信队列配置
 */
//@Configuration
public class MyOrderMQConfig {

    /**
     *  延迟队列
     *              Queue(String name,  队列名字
     *             boolean durable,  是否持久化
     *             boolean exclusive,  是否排他
     *             boolean autoDelete, 是否自动删除
     *             Map<String, Object> arguments) 属性
     * @return
     */@Bean
    public Queue orderDelayQueue() {

        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }

    /**
     *  普通队列、接收延迟后的消息
     * @return
     */
    @Bean
    public Queue orderReleaseQueue() {
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    /**
     * TopicExchange
     *      持久化不自动删除的普通topic交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }


    /**
     * 与延迟队列的绑定关系
     * String destination, 目的地（队列名或者交换机名字）
     * DestinationType destinationType, 目的地类型（Queue、Exhcange）
     * String exchange,
     * String routingKey,
     * Map<String, Object> arguments
     * @return
     */
    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    /**
     * 与消息释放队列的绑定关系
     * @return
     */
    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }
}
