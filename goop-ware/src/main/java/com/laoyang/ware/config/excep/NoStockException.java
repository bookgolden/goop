package com.laoyang.ware.config.excep;


/**
 * @author yyy
 * @Date 2020-07-15 22:18
 * @Email yangyouyuhd@163.com
 */
public class NoStockException extends RuntimeException {

    public Integer skuId;


    public NoStockException(Integer skuId) {
        super(skuId.toString()+"号商品库存不足！");
        this.skuId = skuId;
    }
}
