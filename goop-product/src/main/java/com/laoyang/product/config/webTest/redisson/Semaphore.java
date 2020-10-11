package com.laoyang.product.config.webTest.redisson;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyy
 */
@RestController
public class Semaphore {
    @Autowired
    RedissonClient client;

    /**
     * 消费一个信号量
     * @return
     */
    @GetMapping("/park")
    public String park(){
        RSemaphore park = client.getSemaphore("park");
        park.tryAcquire();//尝试获取一个信号量、如果没有、不阻塞返回false
        System.out.println("可用："+park.availablePermits());
        return "park";
    }

    /**
     * 增加一个信号量
     * @return
     */
    @GetMapping("/go")
    public String go(){
        RSemaphore park = client.getSemaphore("park");
        park.release(); //增加一个信号量
        System.out.println("可用："+park.availablePermits());
        return "ok";
    }

}
