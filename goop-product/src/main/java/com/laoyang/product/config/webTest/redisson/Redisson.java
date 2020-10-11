package com.laoyang.product.config.webTest.redisson;

import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author yyy
 * @Date 2020-06-28 16:51
 * @Email yangyouyuhd@163.com
 */
public class Redisson {

    @Autowired
    RedissonClient client;


    /**
     * 联锁（MultiLock）
     * 基于Redis的Redisson分布式联锁RedissonMultiLock对象可以将多个RLock对象关联为一个联锁，每个RLock对象实例可以来自于不同的Redisson实例。
     * 必须所有锁都锁上才ok
     */

    /**
     * 8.4. 红锁（RedLock）
     * 基于Redis的Redisson红锁RedissonRedLock对象实现了Redlock介绍的加锁算法。该对象也可以用来将多个RLock对象关联为一个红锁，每个RLock对象实例可以来自于不同的Redisson实例。、
     * 大部分锁成功了就Ok
     */

    public void read(){

        RReadWriteLock readWriteLock = client.getReadWriteLock("read-write");
        try {
            readWriteLock.readLock().tryLock(100,5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}