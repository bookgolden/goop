package com.laoyang.cart.config.interceptor;

import com.laoyang.cart.config.CartConstant;
import com.laoyang.cart.xo.to.UserStatusTo;
import com.laoyang.common.constant.GlobalConstant;
import com.laoyang.common.vo.user.MemberSessionTo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.UUID;

/**
 * @author yyy
 * @Date 2020-07-10 15:29
 * @Email yangyouyuhd@163.com
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserStatusTo> cartThread = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //封装用户状态
        UserStatusTo userStatusTo = new UserStatusTo();

        // 从redis中获取用户session、
        MemberSessionTo memberSession = (MemberSessionTo) request.getSession().getAttribute("loginUser");
        if (memberSession != null) {
            userStatusTo.setUserId(memberSession.getId());
        }
        /**
         * 遍历所有请求cookie、获取user-key的值
         */
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if ("user-key".equals(cookie.getName())) {
                    userStatusTo.setUserKey(cookie.getValue());
                    break;
                }
            }
        }

        /**
         * 如果是初次访问、又没登录又没user-key、就手动设置
         * 同时修改当前用户还没有user-key的cookie
         */
        if (userStatusTo.getUserKey() == null) {
            userStatusTo.setUserKey(UUID.randomUUID().toString().replace("-","+"));
            userStatusTo.setHasUserKey(false);
        }

        //将用户状态数据存入当前线程
        cartThread.set(userStatusTo);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /**
         * 对于初次访问用户、没有user——key的cookie
         * 需要手动响应一个user-key的cookie
         */
        UserStatusTo userStatusTo = cartThread.get();
        if (!userStatusTo.getHasUserKey()){
            Cookie userCookie = new Cookie(CartConstant.USER_KEY_COOKIE_NAME,userStatusTo.getUserKey());

            userCookie.setDomain(GlobalConstant.DOMAIN_GOOP);
            userCookie.setMaxAge(CartConstant.USER_KEY_COOKIE_TIMEOUT);

            response.addCookie(userCookie);
        }
    }
}
