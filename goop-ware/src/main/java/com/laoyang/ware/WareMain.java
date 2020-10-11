package com.laoyang.ware;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class WareMain {

    public static void main(String[] args) {
        SpringApplication.run(WareMain.class, args);
        System.out.println("\n\n-----WARE SUCCESS-----\n\n");
    }

}
