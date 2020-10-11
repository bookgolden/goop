package com.laoyang.seckill.scheduled.server;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.laoyang.common.to.mq.SeckillOrderTo;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.coupon.SecKillSessionVo;
import com.laoyang.common.vo.coupon.SecKillSkuRelationVo;
import com.laoyang.common.vo.product.SkuInfoVo;
import com.laoyang.common.vo.user.MemberSessionTo;
import com.laoyang.seckill.config.KillConstant;
import com.laoyang.seckill.config.interceptor.LoginInterceptor;
import com.laoyang.seckill.feign.CouponFeignService;
import com.laoyang.seckill.feign.ProductFeignService;
import com.laoyang.seckill.scheduled.server.inter.SecKillUpService;
import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
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
 * @Date 2020-07-27 16:04
 * @Email yangyouyuhd@163.com
 */
@Slf4j
@Service
public class SecKillUpServiceImpl implements SecKillUpService {
    @Resource
    CouponFeignService couponFeignService;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 获取当前正处于秒杀时间的秒杀场次集合
     *  即当前时间 处于 秒杀场次 起止时间之间
     *      * secKill:sessions:sessionsId-time = skuIdList
     *      * secKill:sku:sessionId:skuId = secKillInfo + sku Info
     * @return
     */
    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSessionList() {
        // 获取所有秒杀场次的key\从Redis中查询到所有key以secKill:sessions:开头
        Set<String> keys = stringRedisTemplate.keys(KillConstant.KILL_CACHE_PREFIX + "*");
        for (String key : keys) {
            /**
             *  如何封装、便如何拆解
             *  secKill:sessions:sessionsId-time = skuIdList
             *  secKill:sku:sessionId:skuId = secKillInfo + sku Info
             */
            String replace = key.replace(KillConstant.KILL_CACHE_PREFIX, "");

            // [sessionId,startTime,endTIme]
            String[] keyArr = replace.split("-",-1);
            //获取存入Redis商品的开始时间
            long startTime = Long.parseLong(keyArr[1]);
            //获取存入Redis商品的结束时间
            long endTime = Long.parseLong(keyArr[2]);
            // 获取当前时间
            long currentTime = System.currentTimeMillis();

            if (currentTime >= startTime && currentTime <= endTime) {
                /**
                 * 当前秒杀场次正处于秒杀时间、获取其关联的SkuIdList
                 */
                List<String> cachedSkuIdList = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hasOps =
                        stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX+keyArr[0]);

                // 批量获取sku info 的json
                List<String> listValue = hasOps.multiGet(cachedSkuIdList);
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

    /**
     * 调用couponFeignService、获取最近三天的秒杀场次
     * 数据来源MySQL、为最新数据
     *      * secKill:sessions:sessionsId-time = skuIdList
     *      * secKill:sku:sessionId:skuId = secKillInfo + sku Info
     */
    @SneakyThrows
    @Override
    public void uploadSecKillSkuLatest3Days() {
        /**
         * 调用couponFeignService、获取最近三天的秒杀场次
         * 数据来源MySQL、为最新数据
         */
        R<List<SecKillSessionVo>> res = couponFeignService.getLate3DaySession();
        if (res.getCode() != 200) {
            throw new RuntimeException("couponFeignService.getLate3DaySession()远程调用失败、状态码："+res.getCode()+"错误信息:"+res.get("msg"));
        }
        List<SecKillSessionVo> data = res.getDate(new TypeReference<List<SecKillSessionVo>>() {});
        saveSecKillInfos(data);
        saveSecKillSkuInfos(data);
    }

    @Override
    public SecKillSkuRedisTo getSkuSecKilInfo(Long skuId) {
        //1、找到所有需要秒杀的商品的key信息---seckill:skus
        BoundHashOperations<String, String, String> hashOps =
                stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);

        //拿到所有的key
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            /**
             *  key 的格式为 sessionId-skuId
             */
            String reg = "\\d-" + skuId;
            for (String key : keys) {
                //如果匹配上了
                if (Pattern.matches(reg,key)) {
                    //从Redis中取出数据来
                    String skuVal = hashOps.get(key);
                    //进行序列化
                    SecKillSkuRedisTo redisTo = JSON.parseObject(skuVal, SecKillSkuRedisTo.class);

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


    /**
     * 校验当前商品是否允许参与秒杀
     * @param killId
     * @param code
     * @return
     */
    @Override
    public boolean killCheck(String killId, String code) {
        if(killId == null || code == null ){
            return false;
        }

        /**
         * 1、绑定哈希操作
         * 2、根据killId(秒杀活动Id+skuId)获取当前秒杀商品的详细信息
         */
        BoundHashOperations<String, String, String> hashOps =
                stringRedisTemplate.boundHashOps(KillConstant.KILL_SKU_CACHE_PREFIX);
        String skuInfoValue = hashOps.get(killId);
        if (StringUtils.isEmpty(skuInfoValue)) {
            return false;
        }

        /**
         *  校验、当前商品是否还在秒杀时间内
         */
        SecKillSkuRedisTo redisTo = JSON.parseObject(skuInfoValue, SecKillSkuRedisTo.class);
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime < startTime || currentTime > endTime) {
            return false;
        }

        /**
         * 效验随机码 和SkuId (getPromotionSessionId() + "-" getSkuId();)
         */
        String randomCode = redisTo.getRandomCode();
        String hashKey = redisTo.getPromotionSessionId() + "-" +redisTo.getSkuId();

        if ( !(randomCode.equals(code) && killId.equals(hashKey))) {
            return false;
        }
        return true;
    }


    /**
     *  已校验过的秒杀请求
     *      处于秒杀时间段
     *      验证码ok
     *
     * @param killId    sessionId-skuId
     * @param num       秒杀数量
     * @return
     */
    @SneakyThrows
    @Override
    public String kill(String killId,Integer num) {

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
            throw new NullPointerException("skuInfo must not is null");
        }

        /**
         *  校验、当前商品是否还在秒杀时间内
         */
        SecKillSkuRedisTo redisTo = JSON.parseObject(skuInfoValue, SecKillSkuRedisTo.class);
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime < startTime || currentTime > endTime) {
            throw new RuntimeException("不在秒杀场次时间段");
        }

        /**
         * 效验随机码 和SkuId (getPromotionSessionId() + "-" getSkuId();)
         */
        String randomCode = redisTo.getRandomCode();
        String hashKey = redisTo.getPromotionSessionId() + "-" +redisTo.getSkuId();
        /**
         *  校验当前可供秒杀库存是否满足所需秒杀数量
         *  是否满足每人限制秒杀数量
         */
        // 每人限购数量
        Integer seckillLimit = redisTo.getSeckillLimit();
        // 剩余库存数量
        String seckillStock = stringRedisTemplate.opsForValue()
                .get(KillConstant.KILL_SKU_STOCK_SEMAPHORE + randomCode);


        Integer count = Integer.valueOf(seckillStock);
        //判断信号量是否大于0,并且买的数量不能超过库存
        if ((count > 0 && num <= seckillLimit && count > num )) {
            throw new RuntimeException("库存不足or超出限购");
        }

        /**
         * 库存充足并且符合限购条件
         *  返回skuId、重定向到订单确认页面、让用户提交订单
         */
        return redisTo.getSkuId().toString();
    }



    /**
     * 怎么存：
     *      集合内每个元素都是一个秒杀场次、
     *      需要纪录场次的base信息和关联的商品信息
     *      可以设计为list存储、
     *          key = secKill:sessions:sessionId:startTime-endTime
     *          val == skuIdList
     * 存在问题:
     *      1、上一次扫描缓存了1、2、3的秒杀场次、然后取消了3场次、新增了4场次、这次扫描的话便只有1、2、4场次的信息
     *      要求：
     *          将4场次的信息新建缓存、
     *          将3场次的信息删除或者较短过期时间、自生自灭
     *      2、秒杀场次关联了商品的cachedSkuIdList、如果关联的sku 发生了 增删、该如何更新缓存和释放库存
     *      要求:
     *          关联的cachedSkuIdList 发生增删后、能及时感知并更新cachedSkuIdList
     *          关联sku 删除后、也应该及时释放库存
     *
     * @param sessions 秒杀场次的集合
     */
    private void saveSecKillInfos(List<SecKillSessionVo> sessions) {
        for (SecKillSessionVo killSession : sessions) {
            Long sessionId = killSession.getId();
            String key = KillConstant.KILL_CACHE_PREFIX + sessionId +"-"+ killSession.getStartTime().getTime() + "-" + killSession.getEndTime().getTime();
            Boolean exist = stringRedisTemplate.hasKey(key);
            if (!exist) {
                // 不存在这个key、或者起止时间被修改、新建缓存
                List<String> skuIdList = killSession.getSecKillSkuRelationVoList()
                        .stream().map(item -> killSession.getId()+"-"+item.getSkuId().toString())
                        .collect(Collectors.toList());
                ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
                opsForList.leftPushAll(key, skuIdList);
            }else {
                // 存在这个key、 校验关联skuIds、是否有需要释放库存

                // 新扫描的秒杀关联skuIds
                List<String> skuIdList = killSession.getSecKillSkuRelationVoList()
                        .stream().map(item -> killSession.getId()+"-"+item.getSkuId())
                        .collect(Collectors.toList());
                // 已缓存的秒杀关联skuIds
                List<String> cachedSkuIdList = stringRedisTemplate.opsForList().range(key, -100, 100);

                for (String skuId : skuIdList) {
                    if(!cachedSkuIdList.contains(skuId)){
                        // 已缓存的skuList 不包含新扫描的SkuId、代表是新关联的、需要重新push到List
                        // 这个逻辑在saveSecKillSkuInfos处理
                    }
                }

                /**
                 *  在cachedSkuIdList 中将skuIdList含有的项删除、
                 *      剩下的就是对已取消的秒杀关联商品、需要去释放库存
                 *      以消息队列通知
                 */
                cachedSkuIdList.removeAll(skuIdList);

                if (!cachedSkuIdList.isEmpty()) {
                    for (String skuKey : cachedSkuIdList) {
                        /**
                         *  需要发送skuId、和秒杀的数量(释放库存)
                         *  秒杀的数量封装在 skuInfo内
                         */
                        String hashKey = KillConstant.KILL_SKU_CACHE_PREFIX;
                        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(hashKey);
                        String skuVal = hashOps.get(skuKey);

                        SecKillSkuRedisTo to = JSON.parseObject(skuVal, SecKillSkuRedisTo.class);
                        Long killCount = to.getSkuInfoVo().getKillCount();

                        // 删除对应关联商品项
                        hashOps.delete(skuKey);

                        // 将 skuId、killCount 通知到仓储服务释放库存
//                    rabbitTemplate.convertAndSend();
                    }
                }
            }
            /**
             * 不管存不存在 、都要给这个key、重新续命4H、不会叠加
             * 秒杀扫描间隔1H、秒杀活动持续时间2H、秒杀结束后释放库存、设置过期时间4H差不多够了
             *  也相当于为缓存续命、
             *      如果已缓存的某个秒杀场次被取消了、那么本次扫描的集合内将不会携带哪个key、也不会为之续命了
             */
            stringRedisTemplate.expire(key, 4, TimeUnit.DAYS);
        }
    }


    /**
     * 缓存秒杀活动所关联的商品信息
     * secKill:sessions:sessionsId-time = List<sessionsId-skuId>
     * secKill:sku:sessionId:skuId = secKillInfo + sku Info
     * @param sessions
     */
    private void saveSecKillSkuInfos(List<SecKillSessionVo> sessions) {
        String prefix = KillConstant.KILL_SKU_CACHE_PREFIX;
        // 以secKill:sku:sessionId: 绑定操作
        BoundHashOperations<String, String, String> opsHash = stringRedisTemplate.boundHashOps(prefix);
        // 遍历每个秒杀场次
        for (SecKillSessionVo session : sessions) {
            // 遍历所关联的商品集合、如果是新增加的、便缓存到redis
            for (SecKillSkuRelationVo skuRelation : session.getSecKillSkuRelationVoList()) {
                String key =session.getId().toString() +"-"+ skuRelation.getSkuId();
                String cachedSkuInfo = opsHash.get(key);

                if (StringUtils.isEmpty(cachedSkuInfo)) {
                    // sku info not exist
                    // 封装to
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();


                    /**
                     *  Sku基本信息封装
                     *  远程查询sku info by skuId
                     *  封装
                     */
                    SkuInfoVo skuInfo = productFeignService.info(skuRelation.getSkuId())
                            .get("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    skuInfo.setKillCount(skuRelation.getSeckillCount().longValue());
                    secKillSkuRedisTo.setSkuInfoVo(skuInfo);

                    /**
                     *  sku秒杀信息、直接对拷
                     */
                    BeanUtils.copyProperties(skuRelation, secKillSkuRedisTo);

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
                    semaphore.trySetPermits(skuRelation.getSeckillCount().intValue());
                    // 存储的过期设置、应该和商品共存亡、要及时续命、此法不行、
                    // semaphore.expire(4,TimeUnit.MINUTES);
                    /**
                     *  SkuId 作 key
                     *  sku Info + kill Info 作value
                     */
                    secKillSkuRedisTo.setName(session.getName());
                    String value = JSON.toJSONString(secKillSkuRedisTo);
                    opsHash.put(key, value);
                }
                /**
                 *  设置 secKill:sku:sessionId的过期时间、
                 */
                opsHash.expire(4,TimeUnit.DAYS);
            }
        }
    }
}
