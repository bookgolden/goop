package com.laoyang.seckill.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.product.SkuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yyy
 * @Date 2020-07-22 21:25
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-product")
public interface ProductFeignService {

    /**
     *  获取sku info
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R<SkuInfoVo> info(@PathVariable("skuId") Long skuId);
}
