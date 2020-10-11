package com.laoyang.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author yyy
 * @Date 2020-07-09 19:06
 * @Email yangyouyuhd@163.com
 */
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
public class CartMain {

    public static void main(String[] args) {
        SpringApplication.run(CartMain.class,args);
        System.out.println("\n\n\n--CART SUCCESS----\n\n\n");
    }
}
