package com.laoyang.seckill.config.interceptor;

import com.laoyang.seckill.config.KillConstant;
import com.laoyang.seckill.controller.SecKillController;
import com.laoyang.seckill.scheduled.server.SecKillUpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-07-10 17:53
 * @Email yangyouyuhd@163.com
 */
@Configuration
public class WebInterceptorConfig extends WebMvcConfigurerAdapter {



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
