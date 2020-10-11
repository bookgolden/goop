package com.laoyang.order.config.excep;


import com.laoyang.common.util.R;

/**
 * @author yyy
 * @Date 2020-07-16 14:32
 * @Email yangyouyuhd@163.com
 */
public class OrderSubmitException extends RuntimeException {
    public Integer code;
    public String msg;

    public static final OrderSubmitException TOKEN_CHECK_ERR = new OrderSubmitException(20020,"token防重码校验失败");
    public static final OrderSubmitException PRICE_CHECK_ERR = new OrderSubmitException(20030,"订单验价不通过,请确认后在提交");
    public static final OrderSubmitException LOCK_STOCK_ERR = new OrderSubmitException(20040,"当前订单存在商品库存不足");

    public OrderSubmitException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
