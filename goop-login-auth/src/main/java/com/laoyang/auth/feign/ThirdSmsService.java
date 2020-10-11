package com.laoyang.auth.feign;

import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yyy
 * @Date 2020-07-03 9:57
 * @Email yangyouyuhd@163.com
 * @Note
 */
@FeignClient("goop-third-party")
public interface ThirdSmsService {

    @GetMapping("/sms/phone")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
