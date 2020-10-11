package com.laoyang.product.redis;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class countDownLatch {

    @Autowired
    RedissonClient client;

    /**
     *  闭锁测试2
     * @throws InterruptedException
     */
    @Test
    public void countDownLatchTest() throws InterruptedException {
        RCountDownLatch downLatch = client.getCountDownLatch("lock");
        downLatch.trySetCount(5);
        System.out.println("开始等待、");
        downLatch.await();
        System.out.println("等待结束、");
    }

    /**
     *  闭锁测试2
     * @throws InterruptedException
     */
    public void down() throws InterruptedException {
        Thread.sleep(100);
        RCountDownLatch downLatch = client.getCountDownLatch("lock");
        System.out.println("获取锁");
        downLatch.countDown();//计数-1
        System.out.println("走了一个,还剩"+downLatch.getCount()+"个");
        if(downLatch.getCount() > 0){
            down();
        }
    }
    @Test
    public void test() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    down();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatchTest();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
