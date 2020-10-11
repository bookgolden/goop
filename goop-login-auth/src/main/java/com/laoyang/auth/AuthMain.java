package com.laoyang.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 *  SpringSession核心原理(过滤器+装饰者模式)
 *      @EnableRedisHttpSession导入了RedisHttpSessionConfiguration配置类
 *      主要注入了一个SessionRepository的操作实现类RedisIndexedSessionRepository
 *      还注入了一个SessionRepositoryFilter过滤器
 *          该过滤器通过在拦截任何请求时、将sessionRepository和request和response分别装饰成自己的
 *          实现类、该实现类的关键是重写了getSession方法、 将session的获取从一同被装饰的sessionRepository中获取
 * @author yyy
 * @Date 2020-07-02 20:16
 * @Email yangyouyuhd@163.com
 * @Note
 */
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
public class AuthMain {

    public static void main(String[] args) {
        SpringApplication.run(AuthMain.class,args);
        System.out.println("\n\n\n--AUTH SUCCESS!---\n\n\n");
    }
}
