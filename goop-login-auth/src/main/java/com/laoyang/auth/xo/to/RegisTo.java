package com.laoyang.auth.xo.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyy
 * @Date 2020-07-03 10:19
 * @Email yangyouyuhd@163.com
 * @Note  注册验证码校验与接口防刷
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisTo {

    private String phone;
    private String code;
    private Long curTime;
}
