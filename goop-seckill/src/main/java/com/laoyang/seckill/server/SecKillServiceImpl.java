package com.laoyang.seckill.server;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.laoyang.common.to.mq.SeckillOrderTo;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.coupon.SecKillSessionVo;
import com.laoyang.common.vo.product.SkuInfoVo;
import com.laoyang.common.vo.user.MemberSessionTo;
import com.laoyang.seckill.config.KillConstant;
import com.laoyang.seckill.config.interceptor.LoginInterceptor;
import com.laoyang.seckill.feign.CouponFeignService;
import com.laoyang.seckill.feign.ProductFeignService;
import com.laoyang.seckill.server.inter.SecKillService;
import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-07-23 13:56
 * @Email yangyouyuhd@163.com
 */
@Service
public class SecKillServiceImpl implements SecKillService {
    @Resource
    CouponFeignService couponFeignService;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RabbitTemplate rabbitTemplate;


    @Override
    public void uploadSecKillSkuLatest3Days() {
        R<List<SecKillSessionVo>> res = couponFeignService.getLate3DaySession();
        if (res.getCode() == 200) {
            List<SecKillSessionVo> data = res.getDate(new TypeReference<List<SecKillSessionVo>>() {
            });
            saveSecKillInfos(data);
            saveSecKillSkuInfos(data);
        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {

        //1、确定当前属于哪个秒杀场次
        long currentTime = System.currentTimeMillis();

        //从Redis中查询到所有key以secKill:sessions开头的所有数据
        Set<String> keys = stringRedisTemplate.keys(KillConstant.KILL_CACHE_PREFIX + "*");
        for (String key : keys) {
            /**
             *   secKill:sessions:startTime-endTime
             *   当时如何封装便如何拆解
             */
            String replace = key.replace(KillConstant.KILL_CACHE_PREFIX, "");
            String[] s = replace.split("-");
            //获取存入Redis商品的开始时间
            long startTime = Long.parseLong(s[0]);
            //获取存入Redis商品的结束时间
            long endTime = Long.parseLong(s[1]);
            //判断是否处于当前秒杀场次
            if (currentTime >= startTime && currentTime <= endTime) {
                //2、获取这个秒杀场次需要的所有商品信息
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hasOps =
                        stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);

                List<String> listValue = hasOps.multiGet(range);
                if (listValue != null && listValue.size() >= 0) {

                    List<SecKillSkuRedisTo> collect = listValue.stream().map(item -> {
                        SecKillSkuRedisTo redisTo = JSON.parseObject(item, SecKillSkuRedisTo.class);
                        // redisTo.setRandomCode(null);当前秒杀开始需要随机码
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckilInfo(Long skuId) {
        //1、找到所有需要秒杀的商品的key信息---seckill:skus
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);

        //拿到所有的key
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            //4-45 正则表达式进行匹配
            String reg = "\\d-" + skuId;
            for (String key : keys) {
                //如果匹配上了
                if (Pattern.matches(reg,key)) {
                    //从Redis中取出数据来
                    String redisValue = hashOps.get(key);
                    //进行序列化
                    SecKillSkuRedisTo redisTo = JSON.parseObject(redisValue, SecKillSkuRedisTo.class);

                    //随机码
                    Long currentTime = System.currentTimeMillis();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    //如果当前时间小于秒杀活动开始时间或者大于活动结束时间
                    if (currentTime < startTime || currentTime > endTime) {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public String kill(String killId, String code, Integer num) {
        //获取当前用户的信息
        MemberSessionTo user = LoginInterceptor.orderThread.get();

        /**
         * 1、绑定哈希操作
         * 2、根据killId(秒杀活动Id+skuId)获取当前秒杀商品的详细信息
         */
        BoundHashOperations<String, String, String> hashOps =
                stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);
        String skuInfoValue = hashOps.get(killId);
        if (StringUtils.isEmpty(skuInfoValue)) {
            return null;
        }
        /**
         *  校验、当前商品是否还在秒杀时间内
         */
        SecKillSkuRedisTo redisTo = JSON.parseObject(skuInfoValue, SecKillSkuRedisTo.class);
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime >= startTime && currentTime <= endTime) {

            /**
             * 效验随机码 和SkuId (getPromotionSessionId() + "-" getSkuId();)
             */
            String randomCode = redisTo.getRandomCode();
            String skuId = redisTo.getPromotionSessionId() + "-" +redisTo.getSkuId();
            if (randomCode.equals(code) && killId.equals(skuId)) {
                /**
                 *  校验当前可供秒杀库存是否满足所需秒杀数量
                 *  是否满足每人限制秒杀数量
                 */
                Integer seckillLimit = redisTo.getSeckillLimit();
                //获取信号量
                String seckillCount = stringRedisTemplate.opsForValue()
                        .get(KillConstant.KILL_SKU_STOCK_SEMAPHORE + randomCode);
                Integer count = Integer.valueOf(seckillCount);
                //判断信号量是否大于0,并且买的数量不能超过库存
                if (count > 0 && num <= seckillLimit && count > num ) {
                    //4、验证这个人是否已经买过了（幂等性处理）,如果秒杀成功，就去占位。userId-sessionId-skuId
                    //SETNX 原子性处理
                    String redisKey = user.getId() + "-" + skuId;
                    //设置自动过期(活动结束时间-当前时间)
                    Long ttl = endTime - currentTime;
                    Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    if (aBoolean) {
                        //占位成功说明从来没有买过,分布式锁(获取信号量-1)
                        RSemaphore semaphore = redissonClient.getSemaphore(KillConstant.KILL_SKU_STOCK_SEMAPHORE + randomCode);
                        //TODO 秒杀成功，快速下单
                        boolean semaphoreCount = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                        //保证Redis中还有商品库存
                        if (semaphoreCount) {
                            //创建订单号和订单信息发送给MQ
                            // 秒杀成功 快速下单 发送消息到 MQ 整个操作时间在 10ms 左右
                            String orderSn = IdWorker.getTimeId();
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(orderSn);
                            orderTo.setMemberId(user.getId());
                            orderTo.setNum(num);
                            orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                            orderTo.setSkuId(redisTo.getSkuId());
                            orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                            return orderSn;
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * 缓存秒杀活动信息
     *
     * @param secKillInfos  封装了所有秒杀场次的info
     */
    private void saveSecKillInfos(List<SecKillSessionVo> secKillInfos) {
        /**
         * redis 存储数据结构
         *   缓存两处
         *      秒杀活动及关联商品Ids
         *         key:start-end DateTime
         *         val:SkuIds
         *      关联商品详情数据
         *         key:promotionSessionId + SkuId
         *         val:SkuInfo
         */
        for (SecKillSessionVo secKillInfo : secKillInfos) {
            long startTime = secKillInfo.getStartTime().getTime();
            long endTime = secKillInfo.getEndTime().getTime();
            String key = KillConstant.KILL_CACHE_PREFIX + startTime + "-" + endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            /**
             *  如果秒杀场次已缓存、并且开始、结束时间未被修改、则key存在
             *  前提是 不存在开始——结束时间都相同的2个秒杀场次
             */
            if (!hasKey) {
                List<String> value = secKillInfo.getSecKillSkuRelationVoList().stream()
                        .map(item -> item.getPromotionSessionId() + "-" + item.getSkuId())
                        .collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, value);
            }
        }
    }

    /**
     * 缓存秒杀活动所关联的商品信息
     *
     * @param sessions
     */
    private void saveSecKillSkuInfos(List<SecKillSessionVo> sessions) {

        // 遍历每个秒杀场次
        sessions.forEach(session -> {
            // 以secKill:sku:PromotionSessionId 绑定操作
            BoundHashOperations<String, String, String> hashOps =
                    stringRedisTemplate
                            .boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);
            session.getSecKillSkuRelationVoList()
                    // 遍历每个秒杀场次内的每个关联商品信息
                    .forEach(secKillSkuRelationVo -> {
                        Long skuId = secKillSkuRelationVo.getSkuId();
                        String key = secKillSkuRelationVo.getPromotionSessionId() + "-" + skuId;
                        Boolean hasKey = hashOps.hasKey(key);
                        if (!hasKey) {
                            SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();

                            /**
                             *  Sku基本信息封装
                             *  远程查询sku info by skuId
                             *  封装
                             */
                            SkuInfoVo skuInfo = productFeignService.info(skuId).get("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            secKillSkuRedisTo.setSkuInfoVo(skuInfo);

                            /**
                             *  sku秒杀信息、直接对拷
                             */
                            BeanUtils.copyProperties(secKillSkuRelationVo, secKillSkuRedisTo);

                            /**
                             *  秒杀场次起止时间、
                             */
                            secKillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                            secKillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                            /**
                             * 秒杀商品唯一随机码
                             */
                            String randomCode = RandomUtil.randomString(6);
                            secKillSkuRedisTo.setRandomCode(randomCode);

                            /**
                             * 分布式信号量total == 秒杀商品总库存
                             *  存储格式：secKill:stock:randomCode、具体到每个秒杀商品的可供秒杀数量
                             */
                            RSemaphore semaphore = redissonClient.getSemaphore(KillConstant.KILL_SKU_STOCK_SEMAPHORE + randomCode);
                            semaphore.trySetPermits(secKillSkuRelationVo.getSeckillCount().intValue());

                            /**
                             *  秒杀活动场次 Id + SkuId 作 key
                             *  sku Info + kill Info 作value
                             */

                            String value = JSON.toJSONString(secKillSkuRedisTo);
                            hashOps.put(key, value);
                        }
                    });
        });
    }
}
