package com.laoyang.member.feign;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.order.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author yyy
 * @Date 2020-07-19 20:31
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-order")
public interface OrderFeignService {

    /**
     *  订单列表数据
     * @param params
     * @return
     */
    @PostMapping("/order/order/listWithItem")
    R<OrderVo> listWithItem(@RequestBody Map<String, Object> params);
}
