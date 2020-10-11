package com.laoyang.seckill.scheduled.server.inter;

import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-27 16:03
 * @Email yangyouyuhd@163.com
 */
public interface SecKillUpService {

    /**
     * 获取当前正处于秒杀时间的秒杀场次集合
     * @return
     */
     List<SecKillSkuRedisTo> getCurrentSecKillSessionList();


    /**
     * 处理秒杀任务缓存至redis
     *  如果发生增删改、必须能得到感知并更新缓存
     */
    void uploadSecKillSkuLatest3Days();

    /**
     *  查询某商品是否参与秒杀活动
     * @param skuId
     * @return
     */
    SecKillSkuRedisTo getSkuSecKilInfo(Long skuId);

    /**
     *  真实秒杀接口
     *      处理秒杀请求
     * @param killId
     * @param num
     * @return
     */
    String kill(String killId, Integer num);


    /**
     * 校验当前秒杀请求是否合法
     * @param killId
     * @param code
     * @return
     */
    boolean killCheck(String killId, String code);

}
