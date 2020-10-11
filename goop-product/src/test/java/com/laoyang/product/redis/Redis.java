package com.laoyang.product.redis;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Redis {


    @Autowired(required = false)
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient client;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void stringTest() {
        List<String> list = Arrays.asList("A", "B", "C", "D");
        String str = StringUtils.join(list.toArray(), "-");
        System.out.println(str);
        System.out.println(Arrays.toString(str.split("-")));
        System.err.println("???");
    }


    /**
     *
     */
    @Test
    public void redis() {
        ValueOperations<String, String> forValue = stringRedisTemplate.opsForValue();
        forValue.set("hello", "world" + UUID.randomUUID());
        String hello = forValue.get("hello");
        System.out.println(hello);
    }


    @Test
    public void client() {
        System.out.println(client);
    }


}
