package com.laoyang.seckill.config.interceptor;

import com.laoyang.common.vo.user.MemberSessionTo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yyy
 * @Date 2020-07-10 15:29
 * @Email yangyouyuhd@163.com
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberSessionTo> orderThread = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 如果当前请求uri 是/kill请求、则登录拦截
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", uri);

        if (match) {
            // 从redis中获取用户session、
            MemberSessionTo memberSession = (MemberSessionTo) request.getSession().getAttribute("loginUser");
            if (memberSession != null) {
                orderThread.set(memberSession);
                return true;
            } else {
                String contextPath = request.getContextPath();
                String curPath = request.getScheme() + "://" + request.getServerName() + ":" +
                        request.getServerPort() + contextPath + request.getRequestURI();
                response.sendRedirect("http://auth.goop.com/to/login?redirect_url=" + curPath);
                return false;
            }
        }
        return true;
    }
}
