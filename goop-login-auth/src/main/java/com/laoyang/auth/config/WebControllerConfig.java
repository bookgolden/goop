package com.laoyang.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yyy
 * @Date 2020-07-02 21:41
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Configuration
public class WebControllerConfig implements WebMvcConfigurer {

    /**
     * @param registry
     * @GetMapping("/to/login")
     * public String toLogin(){
     *      return "login";
     * }
     * @GetMapping("/to/regis")
     * public String toRegis(){
     *      return "regis";
     * }
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/to/login").setViewName("login");
        registry.addViewController("/to/regis").setViewName("regis");

    }
}
