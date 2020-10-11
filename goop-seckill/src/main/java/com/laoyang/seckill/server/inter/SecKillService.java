package com.laoyang.seckill.server.inter;

import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-23 13:56
 * @Email yangyouyuhd@163.com
 */
public interface SecKillService {

    /**
     * 上架三天内需要秒杀的商品
     */
    void uploadSecKillSkuLatest3Days();

    /**
     * 获取当前参与秒杀的商品s
     * @return
     */
    List<SecKillSkuRedisTo> getCurrentSecKillSkus();

    /**
     *  查询商品的秒杀info
     * @param skuId
     * @return
     */
    SecKillSkuRedisTo getSkuSeckilInfo(Long skuId);

    /**
     * 秒杀
     * @param killId  秒杀场次和商品Id
     * @param code      校验码
     * @param num       秒杀数量
     * @return
     */
    String kill(String killId, String code, Integer num);
}
