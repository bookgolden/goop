package com.laoyang.order;

import com.laoyang.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author yyy
 * @Date 2020-07-14 10:30
 * @Email yangyouyuhd@163.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class mqTemplate {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("reason");
        reasonEntity.setStatus(1);
        reasonEntity.setSort(2);
//        String msg = "Hello World";

        //默认使用jdk的序列化机制、可以定制为json发送
        rabbitTemplate.convertAndSend(
                "hello-java-exchange",      //目标交换机
                "hello.java",             //路由键
                reasonEntity,                           //消息对象
                new CorrelationData(UUID.randomUUID().toString())   //header域会多2行UUID
        );
        log.info("消息发送完成:{}",reasonEntity);
    }
}
