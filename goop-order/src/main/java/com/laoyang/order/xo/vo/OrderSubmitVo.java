package com.laoyang.order.xo.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author yyy
 * @Date 2020-07-15 20:11
 * @Email yangyouyuhd@163.com
 */
@Data
@ToString
public class OrderSubmitVo {

    /**
     *  收获地址的id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;
    //订单购物项 redis中去获取

    //优惠、发票

    /**
    * 防重令牌
    */
    private String orderToken;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String remarks;
}
