package com.laoyang.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author yyy
 * @Date 2020-07-22 19:10
 * @Email yangyouyuhd@163.com
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SecKillMain {

    public static void main(String[] args) {
        SpringApplication.run(SecKillMain.class,args);
        System.out.println("\n\n\n\n--KILL SUCCESS----\n\n\n");
    }
}
