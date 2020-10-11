package com.laoyang.order.controller.web;

import com.laoyang.common.util.Query;
import com.laoyang.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author yyy
 * @Date 2020-07-14 16:20
 * @Email yangyouyuhd@163.com
 */
@Controller
public class HelloController {

    @Resource
    RabbitTemplate rabbitTemplate;


    @GetMapping("{page}.html")
    public String hello(@PathVariable("page") String name){
        System.out.println("访问了"+name+"页面");
        return name;
    }


    /**
     * 死信队列测试
     * @param message
     * @param channel
     * @param orderEntity
     */
    @SneakyThrows
    @RabbitListener(queues = "order.release.order.queue")
    public void listener(Message message, Channel channel, OrderEntity orderEntity){
        System.out.println("接收时间:"+new Date());
        System.out.println("接收内容:"+orderEntity.getOrderSn());
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @GetMapping("/testDelayQueue")
    @ResponseBody
    public String delayQueue(){
        OrderEntity entity = new OrderEntity();             //Sat Jul 18 12:50:42 CST 2020
        entity.setOrderSn(UUID.randomUUID().toString());    //c0188360-329d-42c2-907d-5ca91988e399
        //给MQ发送消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",entity);
        System.out.println("发送时间："+new Date());
        return entity.getOrderSn();
    }

}
