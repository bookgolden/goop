package com.laoyang.auth.xo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author yyy
 * @Date 2020-07-03 11:45
 * @Email yangyouyuhd@163.com
 * @Note 用户注册/登录VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6 ,max = 18,message = "长度必须为6-18个字符")
    private String username;

    @NotEmpty(message = "密码必须提交")
    @Length(min = 6 ,max = 18,message = "长度必须为6-18个字符")
    private String password;

    @NotEmpty(message = "手机号必须提交")
    @Pattern(regexp = "^[0]([3-9])[0-9]{9}$",message = "手机号格式非法")
    private String phone;

    @NotEmpty(message = "验证码必须提交")
    @Length(min = 6,max = 6)
    private String code ;
}
