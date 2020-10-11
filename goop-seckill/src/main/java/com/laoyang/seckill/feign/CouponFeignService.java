package com.laoyang.seckill.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.coupon.SecKillSessionVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-22 20:14
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-coupon")
public interface CouponFeignService {

    @GetMapping(value = "/coupon/seckillsession/Late3DaySession")
    R<List<SecKillSessionVo>> getLate3DaySession() ;
}
