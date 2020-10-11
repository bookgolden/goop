package com.laoyang.member.feign;


import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 这是一个声明式的远程调用
 */
@FeignClient("goop-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    R membercoupons();

}
