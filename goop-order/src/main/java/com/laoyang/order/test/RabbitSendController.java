package com.laoyang.order.test;

/**
 * @author yyy
 * @Date 2020-07-14 11:10uu
 * @Email yangyouyuhd@163.com
 */

import com.laoyang.order.entity.OrderEntity;
import com.laoyang.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RabbitSendController {

    @Resource
    RabbitTemplate rabbitTemplate;



    @GetMapping("/send")
    public String sendMsg(@RequestParam("n") int n){
        System.out.println("start send msg 。。。。。。");
        for (int i = 0; i < n; i++) {
            if(i %2== 0){
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setName("OrderReturnReasonEntity--"+i);
                rabbitTemplate.convertAndSend(
                        "hello-java-exchange",
                        "hello.java",
                        entity);
            }else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn("OrderEntity--"+i);
                rabbitTemplate.convertAndSend(
                        "hello-java-exchange",
                        "hello.java",
                        entity);
            }
        }
        System.out.println("ok");
        return "ok";
    }
}


