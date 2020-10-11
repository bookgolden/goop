package com.laoyang.order.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.product.SpuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yyy
 * @Date 2020-07-16 9:58
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-product")
public interface ProductFeignService {


    /**
     * 根据skuId查询spu的信息
     * @param skuId
     * @return
     */
    @GetMapping(value = "/product/spuinfo/skuId/{skuId}")
    R<SpuInfoVo>  getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);

}
