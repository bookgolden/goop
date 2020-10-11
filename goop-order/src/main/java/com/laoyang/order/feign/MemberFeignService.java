package com.laoyang.order.feign;

import com.laoyang.common.vo.member.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-14 20:00
 * @Email yangyouyuhd@163.com
 */
@FeignClient("goop-member")
public interface MemberFeignService {

    /**
     * 获取用户的收货列表
     * @param memberId
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> listReceiveAddress(@PathVariable("memberId") Long memberId);
}
