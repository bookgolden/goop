package com.laoyang.third.controller;

import com.laoyang.common.util.R;
import com.laoyang.third.component.SmsComponent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-07-03 8:56
 * @Email yangyouyuhd@163.com
 * @Note
 */

@Controller
public class SmsController {

    @Resource
    SmsComponent smsComponent;

    @GetMapping("/sms/phone")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsComponent.smsSend(phone,code);
        return R.ok();
    }



}
