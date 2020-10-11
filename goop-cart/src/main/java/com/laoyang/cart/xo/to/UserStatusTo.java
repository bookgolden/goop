package com.laoyang.cart.xo.to;

import lombok.Data;

/**
 * @author yyy
 * @Date 2020-07-10 15:26
 * @Email yangyouyuhd@163.com
 * @apiNote 纪录当前用户的状态、
 */
@Data
public class UserStatusTo {

    /**
     * 已登陆、数据都存专属购物车
     */
    private Long userId;

    /**
     * 未登录、数据存在临时购物车、以user-key为key、
     * 下次登录后、直接加到专属购物车
     */
    private String userKey;

    /**
     * 是否有user-key的cookie
     */
    private Boolean hasUserKey = true;


}
