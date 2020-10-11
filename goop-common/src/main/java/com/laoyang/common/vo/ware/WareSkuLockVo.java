package com.laoyang.common.vo.ware;

import com.laoyang.common.vo.order.OrderItemVo;
import lombok.Data;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-16 11:12
 * @Email yangyouyuhd@163.com
 */
@Data
public class WareSkuLockVo {
    private String orderSn;

    /**
     * 需要锁库存的所有订单项信息
     */
    private List<OrderItemVo> locks;

}

