package com.laoyang.seckill.controller;

import cn.hutool.core.util.RandomUtil;
import com.laoyang.seckill.scheduled.SecKillScheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyy
 * @Date 2020-07-22 19:11
 * @Email yangyouyuhd@163.com
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return RandomUtil.randomString(6);
    }

    @Autowired
    SecKillScheduled recKillScheduled;

    @GetMapping("/upload")
    public String xx(){
        recKillScheduled.uploadLate3Days();
        return "ok";
    }

}
