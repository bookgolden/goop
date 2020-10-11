package com.laoyang.cart.xo.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-10 15:24
 * @Email yangyouyuhd@163.com
 */

@Data
public class CartItemVo implements Serializable {

    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;

    /**
     * 商品套餐属性
     */
    private List<String> skuAttrValues;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");
}
