package com.laoyang.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportResource;

/**
 * @author yyy
 * @Date 2020-07-27 16:00
 * @Email yangyouyuhd@163.com
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SecKillMainPlus {

    public static void main(String[] args) {
        SpringApplication.run(SecKillMainPlus.class,args);
        System.out.println("\n\n\n\n--KILL SUCCESS----\n\n\n");
    }
}