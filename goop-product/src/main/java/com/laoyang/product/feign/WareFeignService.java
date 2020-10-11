package com.laoyang.product.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.ware.SkuStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("goop-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R<List<SkuStockVo>> selectSkuStock(@RequestBody List<Long> skuIds);
}
