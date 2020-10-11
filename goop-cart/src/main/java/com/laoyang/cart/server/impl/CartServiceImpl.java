package com.laoyang.cart.server.impl;

import com.alibaba.fastjson.TypeReference;
import com.laoyang.cart.config.CartConstant;
import com.laoyang.cart.config.interceptor.LoginInterceptor;
import com.laoyang.cart.feign.ProductFeignService;
import com.laoyang.cart.server.CartService;
import com.laoyang.cart.xo.to.UserStatusTo;
import com.laoyang.cart.xo.vo.CartItemVo;
import com.laoyang.cart.xo.vo.CartVo;
import com.laoyang.cart.xo.vo.SkuInfoVo;
import com.laoyang.common.util.R;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-07-10 17:26
 * @Email yangyouyuhd@163.com
 */
@Service
public class CartServiceImpl implements CartService {
    @Resource
    RedisTemplate redisTemplate;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    ThreadPoolExecutor executor;


    /**
     * 获取购物车数据
     * 未登录
     * 获取user-key对应的数据
     * 已登录
     * 将临时购物车数据合并到专属购物车并删除
     * 在获取userId对应的数据
     *
     * @return
     */
    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        UserStatusTo userStatusTo = LoginInterceptor.cartThread.get();
        if (userStatusTo.getUserId() != null) {
            // 已登录

            // 用户专属购物车 和临时购物车的key
            String userCartKey = CartConstant.CART_CACHE_PREFIX + userStatusTo.getUserId();
            String tempCartKey = CartConstant.CART_CACHE_PREFIX + userStatusTo.getHasUserKey();

            //2、查询临时购物车
            List<CartItemVo> tempCart = getCartItems(tempCartKey);
            if (tempCart != null && tempCart.size() > 0) {
                // 临时购物车有数据、合并到专属购物车、并删除
                for (CartItemVo cartItemVo : tempCart) {
                    addCartItem(cartItemVo.getSkuId(), cartItemVo.getCount());
                }
                //清空临时购物车
                delCart(tempCartKey);
            }

            //3、查询合并后的专属购物车数据
            List<CartItemVo> cartItems = getCartItems(userCartKey);
            cartVo.setItems(cartItems);
        } else {
            //没登录

            String tempKey = CartConstant.CART_CACHE_PREFIX + userStatusTo.getUserKey();
            //获取临时购物车里面的所有购物项 、goop:cart:user-key
            List<CartItemVo> tempCart = getCartItems(tempKey);
            cartVo.setItems(tempCart);
        }
        return cartVo.init();
    }

    /**
     * 根据 key 所有购物项
     *
     * @param cartKey
     * @return
     */
    private List<CartItemVo> getCartItems(String cartKey) {
        //获取购物车里面的所有购物项
        BoundHashOperations<String, String, CartItemVo> bindOps = redisTemplate.boundHashOps(cartKey);
        List<CartItemVo> cartItemVos = bindOps.values();
        return cartItemVos;
    }


    @Override
    public void delCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer check) {
        //查询购物项
        CartItemVo cartItem = getCartItem(skuId);
        //修改商品状态
        cartItem.setCheck(check == 1);

        BoundHashOperations<String, String, CartItemVo> redisBindOps = redisBindOps();
        redisBindOps.put(skuId.toString(), cartItem);
    }

    /**
     * 修改购物项数量
     *
     * @param skuId
     * @param num
     */
    @Override
    public void updateItemCount(Long skuId, Integer num) {

        //查询购物车里面的商品
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        cartItem.setTotalPrice(cartItem.getPrice().multiply(new BigDecimal(num)));

        BoundHashOperations<String, String, CartItemVo> redisBindOps = redisBindOps();
        redisBindOps.put(skuId.toString(), cartItem);
    }


    /**
     * 删除购物项
     *
     * @param skuId
     */
    @Override
    public void deleteItem(Integer skuId) {
        BoundHashOperations<String, String, CartItemVo> redisBindOps = redisBindOps();
        redisBindOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCheckedItems() {
        UserStatusTo userStatusTo = LoginInterceptor.cartThread.get();
        String userCartKey = CartConstant.CART_CACHE_PREFIX + userStatusTo.getUserId();
        List<CartItemVo> items = getCartItems(userCartKey);

        List<CartItemVo> res = items.stream()
                // 过滤出 选中的购物项
                .filter(item -> item.getCheck())
                // 更新购物项价格
                .map(item -> {
                    BigDecimal price = productFeignService.getInfo(item.getSkuId());
                    item.setPrice(price);
                    return item;
                }).collect(Collectors.toList());

        return res;
    }


    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, String, CartItemVo> redisOps = redisBindOps();
        CartItemVo itemVo = redisOps.get(skuId.toString());
        return itemVo;
    }


    /**
     * 如果redis没有该购物项、
     * 1、查询sku info 封装
     * 2、查询属性值列表封装
     * 如果有此购物项
     * 将数量新增即可
     *
     * @param skuId
     * @param num   数量
     * @return
     */
    @SneakyThrows
    @Override
    public CartItemVo addCartItem(Long skuId, Integer num) {
        // BoundHashOperations<“goop:cart:userId”, "skuId", CartItemVo>  限定泛型记得使用RedisTemplate
        BoundHashOperations<String, String, CartItemVo> keyOps = redisBindOps();

        // 从redis 获取该购物项
        CartItemVo res = keyOps.get(skuId.toString());
        if (res == null) {
            // 当前购物项为空、查询添加
            CartItemVo cartItemVo = new CartItemVo();
            // sku 详情封装
            CompletableFuture<Void> getInfo = CompletableFuture.runAsync(() -> {
                //1、远程查询当前要添加商品的info
                R<SkuInfoVo> skuInfoRes = productFeignService.info(skuId);
                SkuInfoVo skuInfo = skuInfoRes.get("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setPrice(skuInfo.getPrice());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setTitle(skuInfo.getSkuTitle());
            }, executor);
            // sku 属性集合封装
            CompletableFuture<Void> saleAttr = CompletableFuture.runAsync(() -> {
                List<String> saleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(saleAttrValues);
            }, executor);

            // 等待
            CompletableFuture.anyOf(getInfo, saleAttr).get();

            // 购物项总价格
            cartItemVo.setTotalPrice(cartItemVo.getPrice().multiply(new BigDecimal(cartItemVo.getCount())));
            //插入
            keyOps.put(skuId.toString(), cartItemVo);
            return cartItemVo;
        } else {
            // 已有购物项、更新数量
            res.setCount(res.getCount() + num);
            // 购物项总价格
            res.setTotalPrice(res.getPrice().multiply(new BigDecimal(res.getCount())));
            keyOps.put(skuId.toString(), res);
            return res;
        }
    }


    /**
     * 购物项目存于redis、使用哈希存储
     * 登录用户：goop:cart:userId:skuId:value
     * 临时用户:goop:cart:user-key:skuId:value
     * 将当前添加购物车的请求对redis的操作
     * 绑定到redis的一个key
     *
     * @return
     */
    private BoundHashOperations<String, String, CartItemVo> redisBindOps() {
        String key = CartConstant.CART_CACHE_PREFIX;
        UserStatusTo userStatusTo = LoginInterceptor.cartThread.get();
        if (userStatusTo.getUserId() != null) {
            key += userStatusTo.getUserId();
        } else {
            key += userStatusTo.getUserKey();
        }

        BoundHashOperations<String, String, CartItemVo> hashOperations = redisTemplate.boundHashOps(key);
        return hashOperations;

    }
}

