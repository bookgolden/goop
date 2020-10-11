package com.laoyang.order.test;

import com.alibaba.fastjson.JSON;
import com.laoyang.order.entity.OrderEntity;
import com.laoyang.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author yyy
 * @Date 2020-07-14 11:11
 * @Email yangyouyuhd@163.com
 */
@Slf4j
@Service
@RabbitListener(
        queues = {"hello-java-queue"}   //可监听多个队列
)
public class RabbitReceiveService {

    /**
     * 使用三类型的参数接收
     * 1、org.springframework.amqp.core.Message
     * 原生的、具有消息头和消息体 详细信息
     * 2、T<发送消息的实体类型> 、然后解析json精确接收
     * 3、Channel
     * 使用当前传输数据的长连接的管道接收
     */
//    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {
        //拿到主体内容
        byte[] body = message.getBody();
        Object o = JSON.parseObject(body, OrderReturnReasonEntity.class);
        log.info("消息体转换的数据:" + o);
        //拿到的消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        log.info("消息头转换的数据：" + messageProperties);
        log.info("实体接收数据：" + content);
        log.info("管道接收的数据" + channel.getConnection().getServerProperties());
    }

    @SneakyThrows
    @RabbitHandler
    public void receiveA(Message message, OrderReturnReasonEntity content, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (deliveryTag % 2 == 0) {
            //确认收货、
            channel.basicAck(deliveryTag, false);
        } else {
            //拒绝收货
            channel.basicNack(deliveryTag, false, true);
        }
        log.info("receiveA接收数据：" + content + "\n 并确认收货" + deliveryTag);
    }

    @SneakyThrows
    @RabbitHandler
    public void receiveB(Message message, OrderEntity content, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (deliveryTag % 2 == 0) {
            channel.basicAck(deliveryTag, false);
        } else {
            channel.basicNack(deliveryTag, false, true);
        }
        log.info("receiveB接收数据：" + content + "\n 并确认收货" + deliveryTag);
    }
}
