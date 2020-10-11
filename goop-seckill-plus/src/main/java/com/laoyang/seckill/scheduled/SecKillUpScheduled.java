package com.laoyang.seckill.scheduled;

import com.laoyang.seckill.config.KillConstant;
import com.laoyang.seckill.scheduled.server.inter.SecKillUpService;
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
 * @Date 2020-07-27 16:02
 * @Email yangyouyuhd@163.com
 *
 *       定时扫描秒杀场次
 *           未来三天内所有的秒杀场次、已上架的不继续上架
 *           所有秒杀场次所关联的商品、发生了增删的要能及时更新
 *            所有秒杀场次关联的商品的修改、。。
 *            秒杀场次info发生修改了应该。。
 *
 */
@Service
public class SecKillUpScheduled {

    @Resource
    SecKillUpService secKillService;

    @Resource
    RedissonClient redissonClient;



    /**
     *  每小时扫描一次db、
     *  将未来三天的秒杀活动及关联商品上架到redis
     *  保证数据最新且一致！
     */
    @Async
    @SneakyThrows
    @Scheduled(cron = "* */1 * * * *")
    public void uploadLate3Days(){
        RLock lock = redissonClient.getLock(KillConstant.KILL_UP_LOCK);
        boolean isLock = lock.tryLock(10, TimeUnit.SECONDS);
        if(isLock) {
            try {
                secKillService.uploadSecKillSkuLatest3Days();
            }finally {
                lock.unlock();
            }
        }
    }
}
