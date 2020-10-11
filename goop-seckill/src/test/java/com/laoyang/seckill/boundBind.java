package com.laoyang.seckill;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author yyy
 * @Date 2020-08-02 11:12
 * @Email yangyouyuhd@163.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class boundBind {

    @Resource
    StringRedisTemplate stringRedisTemplate;


    @Test
    public void insert(){
        String a = "A";
        String b = "B";
        String c = "C";
        ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
        /**
         *  a:1、2、3、4、5、6、7、b
         *  a:b:1\2\3\4\5\\c
         *  a:b:c:1\2\3\4\\
         */
        for (int i = 0; i < 10; i++) {
            opsForList.leftPush(a+":"+i,a+"-"+i);
            for (int j = 0; j < 10; j++) {
                opsForList.leftPush(a+":"+b+":"+j,a+b+"-"+j);
                for (int k = 0; k < 10; k++) {
                    opsForList.leftPush(a+":"+b+":"+c+":"+k,a+b+c+"-"+k);
                }
            }
        }
    }

    @Test
    public void select (){
        String a = "A";
        String b = "B";
        String c = "C";
        Set<String> aKey = stringRedisTemplate.keys(a+":?");
        Set<String> bKey = stringRedisTemplate.keys(a+":"+b+":?");
        Set<String> cKey = stringRedisTemplate.keys(a+":"+b+":"+c+":?");
        System.out.println(StringUtils.join(aKey,"-"));
        System.out.println(StringUtils.join(bKey,"-"));
        System.out.println(StringUtils.join(cKey,"-"));
    }
    @Test
    public void select2 (){
        Set<String> keys = stringRedisTemplate.keys("A:*:1");
        System.out.println(keys);
    }
}

