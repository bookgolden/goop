package com.laoyang.cart.feign;

import com.laoyang.cart.xo.vo.CartItemVo;
import com.laoyang.cart.xo.vo.SkuInfoVo;
import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-13 9:52
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-product")
public interface ProductFeignService {

    /**
     * 查询skuId商品的详情
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R<SkuInfoVo> info(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId查询pms_sku_sale_attr_value表中的信息
     * @param skuId
     * @return
     */
    @GetMapping("/product/skusaleattrvalue/attrlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);


    @RequestMapping("/product/skuinfo/{skuId}/price")
    BigDecimal getInfo(@PathVariable("skuId") Long skuId);

}
