package com.laoyang.order.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.ware.FareVo;
import com.laoyang.common.vo.ware.SkuStockVo;
import com.laoyang.common.vo.ware.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-15 14:01
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-ware")
public interface WmsFeignService {

    @GetMapping("/ware/waresku/hasstock")
    R<List<SkuStockVo>> selectSkuStock(@RequestBody List<Long> skuIds);

    @GetMapping(value = "/ware/wareinfo/fare")
    R<FareVo> getFare(@RequestParam("addrId") Long addrId);

    @PostMapping(value = "/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo) ;

}


