package com.laoyang.search.controller;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @Resource
    RestHighLevelClient client;
    @Resource
    RestClientBuilder builder;

    @GetMapping("/hello")
    public Object hello(){
        Map map = new HashMap();
        map.put("client",client);
        map.put("builder",builder);
        return "SUCCESS";
    }
}
