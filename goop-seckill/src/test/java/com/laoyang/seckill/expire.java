package com.laoyang.seckill;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author yyy
 * @Date 2020-08-02 12:47
 * @Email yangyouyuhd@163.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class expire {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void expireTest() throws InterruptedException {
        String key = "Hello";
        stringRedisTemplate.opsForValue().set(key,"World");
        stringRedisTemplate.expire(key,1, TimeUnit.HOURS);
        Thread.sleep(1000);
        System.out.println(stringRedisTemplate.getExpire(key));
        stringRedisTemplate.expire(key,1, TimeUnit.HOURS);
        System.out.println(stringRedisTemplate.getExpire(key));
        stringRedisTemplate.delete(key);
    }

}
