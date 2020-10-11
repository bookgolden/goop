package com.laoyang.cart;

import com.laoyang.cart.xo.vo.CartItemVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yyy
 * @Date 2020-07-13 12:02
 * @Email yangyouyuhd@163.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BoundHashOps {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void  hello(){
        String key = "goop:cart:1";
        BoundHashOperations<String,String, CartItemVo> ops = redisTemplate.boundHashOps(key);
        CartItemVo itemVo = ops.get("2");
        System.out.println(itemVo);

        ops.put("2",new CartItemVo());
        System.out.println(ops.get("2"));
    }
}
