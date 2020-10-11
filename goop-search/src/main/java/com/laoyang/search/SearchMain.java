package com.laoyang.search;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SearchMain {


    public static void main(String[] args) {
        SpringApplication.run(SearchMain.class,args);
        System.out.println("\n\n\n\n---SEARCH SUCCESS--\n\n\n");
    }

}
