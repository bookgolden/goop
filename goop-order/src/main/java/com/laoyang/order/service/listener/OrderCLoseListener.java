package com.laoyang.order.service.listener;

import com.laoyang.order.entity.OrderEntity;
import com.laoyang.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-07-19 11:50
 * @Email yangyouyuhd@163.com
 */
@Component
@RabbitListener(queues = "order.release.order.queue")
public class OrderCLoseListener {

    @Resource
    private OrderService orderService;


    @SneakyThrows
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) {
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
