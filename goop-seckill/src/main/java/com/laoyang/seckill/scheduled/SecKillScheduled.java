package com.laoyang.seckill.scheduled;

import com.laoyang.seckill.config.KillConstant;
import com.laoyang.seckill.server.inter.SecKillService;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author yyy
 * @Date 2020-07-22 20:21
 * @Email yangyouyuhd@163.com
 */
@Service
public class SecKillScheduled {

    @Resource
    SecKillService secKillService;

    @Resource
    RedissonClient redissonClient;



    @SneakyThrows
    @Async
    @Scheduled(cron = "*/2 * * * * *")
    public void uploadLate3Days(){
        RLock lock = redissonClient.getLock(KillConstant.KILL_UP_LOCK);
        boolean state = lock.tryLock(10, TimeUnit.SECONDS);
        if(state) {
            try {
                secKillService.uploadSecKillSkuLatest3Days();
            }finally {
                lock.unlock();
            }
        }
    }
}
