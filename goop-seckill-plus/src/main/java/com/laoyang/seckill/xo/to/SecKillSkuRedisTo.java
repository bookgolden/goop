package com.laoyang.seckill.xo.to;

import com.laoyang.common.vo.product.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yyy
 * @Date 2020-07-22 21:09
 * @Email yangyouyuhd@163.com
 */
@Data
public class SecKillSkuRedisTo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;

    /**
     * 活动场次名
     */
    private String name;


    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;


    /**
     * 当前商品秒杀的开始时间
     */
    private Long startTime;

    /**
     * 当前商品秒杀的结束时间
     */
    private Long endTime;

    /**
     * 当前商品秒杀的随机码
     */
    private String randomCode;

    /**
     * Sku 基本详情
     */
    private SkuInfoVo skuInfoVo;
}
