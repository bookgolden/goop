package com.laoyang.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yyy
 * @Date 2020-07-09 19:07
 * @Email yangyouyuhd@163.com
 */
@Controller
public class HelloController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "hello";
    }


    @GetMapping("success")
    public String success(){
        return "success";
    }

    @GetMapping("list")
    public String cartList(){
        return "cartList";
    }
}
