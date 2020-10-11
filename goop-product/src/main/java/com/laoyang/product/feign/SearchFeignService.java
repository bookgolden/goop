package com.laoyang.product.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.es.SkuVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yyy
 */
@FeignClient("goop-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productUp(@RequestBody List<SkuVO> skuVOList);

}
