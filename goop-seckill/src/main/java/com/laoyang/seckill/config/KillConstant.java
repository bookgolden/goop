package com.laoyang.seckill.config;

/**
 * @author yyy
 * @Date 2020-07-23 13:09
 * @Email yangyouyuhd@163.com
 */
public class KillConstant {
    /**
     * 秒杀场次 存储key 前缀
     */
    public static final String KILL_CACHE_PREFIX = "secKill:sessions:";
    /**
     * 关联商品 info 存储key 前缀
     */
    public static final String KILL_SKU_CACHE_PREFIX = "secKill:sku:";
    /**
     * 关联商品 库存 存储key 前缀
     */
    public static final String KILL_SKU_STOCK_SEMAPHORE= "secKill:stock:";
    /**
     * 秒杀上架 分布式锁
     */
    public static final String KILL_UP_LOCK= "secKill:up:lock";
}
