package com.laoyang.cart.xo.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-10 15:25
 * @Email yangyouyuhd@163.com
 */

@Data
public class CartVo {
    /**
     * 购物项集合
     */
    List<CartItemVo> items;

    /**
     * 商品总数量
     */
    private Integer countNum = 0;

    /**
     * 商品类型总数量
     */
    private Integer countType = 0;

    /**
     * 总优惠价
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    /**
     * 商品总价
     */
    private BigDecimal totalAmount = new BigDecimal("0.00");


    public CartVo init() {
        // 计算商品总数量、和总优惠价、和未优惠总价
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                if (item.getCount() != null) {
                    countNum += item.getCount();
                }
                if (item.getReduce() != null && item.getCheck()) {
                    // 价格不为空、是选中状态的才计算总优惠
                    reduce = reduce.add(item.getReduce());
                }
                if (item.getPrice() != null && item.getCheck()) {
                    // 价格不为空、是选中状态的才计算总价
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        // 计算商品类型总数量
        countType = items.size();

        //计算商品优惠后总价
        totalAmount = totalAmount.subtract(reduce);
        return this;
    }
}
