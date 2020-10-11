package com.laoyang.product.config.webTest;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-06-28 16:39
 * @Email yangyouyuhd@163.com
 */
@RestController
public class main {


    @Resource
    RedissonClient redissonClient;

    @GetMapping("/unlock")
    public Object hello(){
        RLock lock = redissonClient.getLock("lock");
        try {
            System.out.println("try");
            lock.unlock();
        }catch (Exception e){
            System.out.println("catch");
            lock.unlock();
            throw e;
        }finally {
            System.out.println("finally");
            lock.unlock();
        }
        return lock;
    }
}
