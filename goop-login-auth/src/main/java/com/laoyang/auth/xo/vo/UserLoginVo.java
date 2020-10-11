package com.laoyang.auth.xo.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author yyy
 * @Date 2020-07-03 22:19
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Data
public class UserLoginVo {


//    @NotEmpty(message = "用户名必须提交")
//    @Length(min = 6 ,max = 18,message = "长度必须为6-18个字符")
    private String loginacct;

//    @NotEmpty(message = "密码必须提交")
//    @Length(min = 6 ,max = 18,message = "长度必须为6-18个字符")
    private String password;

//    @NotEmpty(message = "手机号必须提交")
//    @Pattern(regexp = "^[0]([3-9])[0-9]{9}$",message = "手机号格式非法")
//    private String phone;
}
