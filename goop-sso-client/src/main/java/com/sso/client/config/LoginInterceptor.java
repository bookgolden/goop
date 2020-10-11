package com.sso.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 自定义拦截
 * 拦截未登录用户、并重定向到远程登录
 * @author yyy
 */
public class LoginInterceptor implements HandlerInterceptor {


    @Value("${sso.server.url}")
    String ssoUrl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("LoginInterceptor.preHandle");

        // 先判断当前Session 有无该用户、
        HttpSession session = request.getSession();
        Object user = session.getAttribute("loginUser");
        if (user != null) {
            //当前系统 重复访问
            return true;
        }

        // 试图从请求域中获取token
        String token = request.getParameter("token");
        if (token != null) {
            /**
             *  代表刚刚登录成功、重定向回来、带了个token参数
             *  根据token 去远程获取其info、将其存入当前session
             */
            ResponseEntity<String> forEntity = new RestTemplate()
                    .getForEntity("http://xxlssoserver.com:8080/info?token=" + token, String.class);
            String userSession = forEntity.getBody();
            // 将用户数据 存入当前系统的session
            request.getSession().setAttribute("loginUser", userSession);

            return true;
        }

        // return "redirect:" + ssoUrl + "/login.html?redirect_url=http://client01.com:8081/emps";
        // 首次登录、重定向到远程
        String contextPath = request.getContextPath();
        String curPath = request.getScheme() + "://" + request.getServerName() + ":" +
                request.getServerPort() + contextPath + request.getRequestURI();
        response.sendRedirect(ssoUrl + "/login.html?redirect_url=" + curPath);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("LoginInterceptor.postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("LoginInterceptor.afterCompletion");
    }
}
