package com.sso.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @dataTotal 数据量条
 */
@Controller
public class LoginController {

    Map<String,String> session = new HashMap<>();

    /**
     * 根据token 获取用户的session数据
     * @param token
     * @return
     */
    @GetMapping("info")
    @ResponseBody
    public String getInfoByToken(@RequestParam("token")String token){
        return session.get(token);
    }


    /**
     * 接收客户端的重定向登录请求、
     * 并将来源url存入登录表单隐藏域
     * @param redirect_url  来源url
     * @param model     //
     * @param token 存储在浏览器本地的cookie数据
     * @return
     */
    @GetMapping("/login.html")
    public String toLogin(String redirect_url,
                          Model model,
                          @CookieValue(value = "token",required = false) String token) {
        if(token !=null){
            // 代表当前用户在其他系统登录过、不重复登录
            return "redirect:"+redirect_url+"?token="+token;
        }

        model.addAttribute("url",redirect_url);
        return "login";
    }

    /**
     * 处理用户的登录请求、
     * 如果登录成功则、存储用户数据于session
     * @param username
     * @param url
     * @return
     */
    @GetMapping("login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {
        if (username != null) {

            //以 随机6位码为 token 当前用户名为session数据
            String token = UUID.randomUUID().toString().substring(0, 6);
            session.put(token,username);

            //首次登录后、将当前token 存储到浏览器的默认域
            response.addCookie(new Cookie("token",token));
            return "redirect:" + url+"?token="+ token;
        }

        return "login";
    }
}
