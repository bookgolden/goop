package com.laoyang.ware.feign;

import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yyy
 * @Date 2020-07-15 15:05
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-member")
public interface MemberFeignService {
    /**
     * 根据id获取用户地址信息
     * @param id
     * @return
     */
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}
