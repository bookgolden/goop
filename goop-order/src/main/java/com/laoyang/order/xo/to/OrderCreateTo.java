package com.laoyang.order.xo.to;

import com.laoyang.order.entity.OrderEntity;
import com.laoyang.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-16 9:06
 * @Email yangyouyuhd@163.com
 * @apiNote 创建订单时用户封装需要的各类数据to
 */
@Data
public class OrderCreateTo {

    /**
     *  订单的基本数据和收货地址
     */
    private OrderEntity order;

    /**
     * 关联的购物项集合
     */
    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     *      OrderEntity内已封装
     */
    private BigDecimal payPrice;

    /**
     * 运费
     *   OrderEntity内已封装
     */
    private BigDecimal fare;

}
