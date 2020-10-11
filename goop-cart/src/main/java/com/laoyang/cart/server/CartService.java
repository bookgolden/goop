package com.laoyang.cart.server;

import com.laoyang.cart.xo.vo.CartItemVo;
import com.laoyang.cart.xo.vo.CartVo;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-10 17:23
 * @Email yangyouyuhd@163.com
 */
public interface CartService {

    /**
     *  获取临时用户的临时购物车、or
     *  获取登录用户的专属购物车
     * @return
     */
    CartVo getCart();

    /**
     * 添加一个购物项、
     * @param skuId
     * @param num   数量
     * @return
     */
    CartItemVo addCartItem(Long skuId, Integer num);

    /**
     * 根据skuId 查询一个购物项
     * @param skuId
     * @return
     */
    CartItemVo getCartItem(Long skuId);


    /**
     * 根据key 清空购物车
     * @param cartKey
     */
    void delCart(String cartKey);

    /**
     * 更改购物项选中状态
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);


    /**
     *  更新购物项数量
     * @param skuId
     * @param num
     */
    void updateItemCount(Long skuId, Integer num);

    /**
     * 删除购物项、根据key
     * @param skuId
     */
    void deleteItem(Integer skuId);

    /**
     * 封装用户已选中购物项的最新数据
     * @return
     */
    List<CartItemVo> getCheckedItems();
}
