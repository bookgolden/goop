package com.laoyang.product.config.webTest.redisson;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CountDownLock {

    @Autowired
    RedissonClient client;

    /**
     * 每次消耗-1
     * @throws InterruptedException
     */
    @GetMapping("/down")
    public void down() throws InterruptedException {
        RCountDownLatch downLatch = client.getCountDownLatch("door");
        downLatch.countDown();//计数-1
        System.out.println("走了一个,还剩"+downLatch.getCount()+"个");

    }

    /**
     * 创建一把闭锁、 等待全部 释放完毕后解锁
     * @throws InterruptedException
     */
    @GetMapping("/lock")
    public void countDownLatchTest() throws InterruptedException {
        RCountDownLatch downLatch = client.getCountDownLatch("door");
        downLatch.trySetCount(5);
        System.out.println("开始等待、");
        downLatch.await();
        System.out.println("等待结束、");
    }
}
