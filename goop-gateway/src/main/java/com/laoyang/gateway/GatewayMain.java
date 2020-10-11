package com.laoyang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GatewayMain {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class,args);
        System.out.println("\n\n\n--GATEWAY SUCCESS--\n\n\n");
    }
}
