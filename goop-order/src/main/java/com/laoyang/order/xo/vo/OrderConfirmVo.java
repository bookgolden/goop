package com.laoyang.order.xo.vo;

import com.laoyang.common.vo.member.MemberAddressVo;
import com.laoyang.common.vo.order.OrderItemVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yyy
 * @Date 2020-07-14 19:28
 * @Email yangyouyuhd@163.com
 */
@Data
public class OrderConfirmVo {
    /**
     * 会员收获地址列表
     */
    List<MemberAddressVo> memberAddressVos;
    /**
     * 所有选中的购物项
     */
    List<OrderItemVo> items;
    /**
     * 优惠券（会员积分）
     */
    private Integer integration;
    /**
     * 防止重复提交的令牌
     **/
    private String orderToken;
    /**
     * 库存锁定
     */
    Map<Long, Long> stocks;
    /**
     * 订单总额
     **/
    BigDecimal PriceTotal = new BigDecimal("0.00");
    /**
     * 优惠总额
     */
    BigDecimal reduceTotal = new BigDecimal("0.00");
    /**
     * 应付价格
     */
    BigDecimal payPrice = new BigDecimal("0.00");


    /**
     *  总共有 x 件商品
     * @return
     */
    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    /**
     * 计算商品的
     *      总面板价、总优惠价、应付价
     */
    public OrderConfirmVo postInit() {
        for (OrderItemVo item : items) {
            // 获取商品数量
            BigDecimal total = new BigDecimal(item.getCount().toString());
            // 计算当前商品的总价格
            BigDecimal itemPrice = item.getPrice().multiply(total);
            // 计算当前商品的总优惠
            BigDecimal itemDeduce = item.getReduce().multiply(total);
            // 加至
            PriceTotal = PriceTotal.add(itemPrice);
            reduceTotal = reduceTotal.add(itemDeduce);
        }

        // 总面板价- 总优惠价
        payPrice = PriceTotal.subtract(reduceTotal);
        return this;
    }
}
