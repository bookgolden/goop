package com.laoyang.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.Executors;


/**
 * 使用RabbitMQ
 * 1、引入amqp场景;RabbitAutoConfiguration就会自动生效
 * 2、给容器中自动配置了
 *      RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3、@EnableRabbit:
 *
 * 4、消息可靠投递
 *    1、服务器交换机收到消息就回调
 *    2、消息正确抵达队列进行回调
 *    3、消费端消费完毕确认接收
 *
 * 5、同一个实例内事务方法互调默认失效、因为事务方法都是通过代理对象调用的
 *      直接调用、绕过了代理对象、所有只有第一个事务会生效
 *    引入aop-starter spring-boot-starter-aop 引入了aspectj
 *    @EnableAspectJAutoProxy(exposeProxy = true) 开启aspectj动态代理功能、对外暴露代理对象
 *    同一实例内互调用事务方法、使用
 *          AopContext.currentProxy() 获取当前代理对象
 *          使用该对象 自由调用事务方法
 *
 *
 * 6、分布式事务
 * Seata控制分布式事务
 *  1）、每一个微服务必须创建undo_Log
 *  2）、安装事务协调器：seate-server
 *  3）、整合
 *      1、导入依赖
 *      2、解压并启动seata-server：
 *          registry.conf:注册中心配置    修改 registry ： nacos
 *      3、所有想要用到分布式事务的微服务使用seata DataSourceProxy 代理自己的数据源
 *      4、每个微服务，都必须导入   registry.conf   file.conf
 *          vgroup_mapping.{application.name}-fescar-server-group = "default"
 *      5、启动测试分布式事务
 *      6、给分布式大事务的入口标注@GlobalTransactional
 *      7、每一个远程的小事务用@Trabsactional
 * @author yyy
 */

@EnableRabbit
@EnableSwagger2
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OrderMain {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain.class, args);
        System.out.println("\n\n--ORDER SUCCESS--\n\n");
        Executors.newCachedThreadPool();
    }

}
