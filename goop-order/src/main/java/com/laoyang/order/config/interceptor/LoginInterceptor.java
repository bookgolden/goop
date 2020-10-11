package com.laoyang.order.config.interceptor;

import com.laoyang.common.constant.GlobalConstant;
import com.laoyang.common.vo.user.MemberSessionTo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author yyy
 * @Date 2020-07-10 15:29
 * @Email yangyouyuhd@163.com
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberSessionTo> orderThread = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

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
}
