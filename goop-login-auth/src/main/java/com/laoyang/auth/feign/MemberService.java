package com.laoyang.auth.feign;

import com.laoyang.auth.xo.to.RegisTo;
import com.laoyang.auth.xo.vo.UserLoginVo;
import com.laoyang.auth.xo.vo.UserRegisVo;
import com.laoyang.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yyy
 * @Date 2020-07-03 16:39
 * @Email yangyouyuhd@163.com
 * @Note
 */
@FeignClient("goop-member")
public interface MemberService {

    @PostMapping("/member/member/regis")
    R regis(@RequestBody UserRegisVo regisTo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);

}
