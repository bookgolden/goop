package com.laoyang.product.feign;

import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yyy
 * @Date 2020-07-24 15:31
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-seckill")
public interface SecKillFeignService {


    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSeckilInfo(@PathVariable("skuId") Long skuId) ;
}
