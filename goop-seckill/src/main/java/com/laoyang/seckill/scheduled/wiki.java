package com.laoyang.seckill.scheduled;

/**
 * @author yyy
 * @Date 2020-07-22 19:30
 * @Email yangyouyuhd@163.com
 */
public class wiki {
    /**
     * 1、在Spring中表达式是6位组成，不允许第七位的年份
     * 2、在周几的的位置,1-7代表周一到周日
     * 3、定时任务不该阻塞。默认是阻塞的
     *      1）、可以让业务以异步的方式，自己提交到线程池
     *          CompletableFuture.runAsync(() -> {
     *              },execute);
     *      2）、支持定时任务线程池；设置 TaskSchedulingProperties
     *          spring.task.scheduling.pool.size: 5
     *      3）、让定时任务异步执行
     *          开启异步任务
     * 解决：使用异步任务 + 定时任务来完成定时任务不阻塞的功能
     */


    /**
     * 定时任务
     *      1、EnableScheduling 开启定时任务
     *      2、@Scheduled 开启一个定时任务
     *      3、TaskSchedulingAutoConfiguration
     * 异步任务
     *      1、EnableAsync 开启异步任务
     *      2、@Async 开启一个异步任务
     *      3、TaskExecutionAutoConfiguration
     */


}
