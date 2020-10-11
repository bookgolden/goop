package com.laoyang.product.thread;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import java.util.concurrent.*;

/**
 * @author yyy
 * @Date 2020-06-29 16:48
 * @Email yangyouyuhd@163.com
 * @Note
 */
@SpringBootTest
public class ManyThreadTest {
    //Executors.newFixedThreadPool(10);
    ExecutorService executor =
            new ThreadPoolExecutor(5, 20,
                    60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue(),
                    new DefaultManagedAwareThreadFactory(),
                    new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * 花式异步单线程
     * 链式调用的顺序影响程序的进行
     * .handleAsync()固定最后执行
     */
    @Test
    public void async() throws Throwable {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("业务执行");
            int i = 10 / 2;
            return i;
        }, executor).whenCompleteAsync((u, throwable) -> {
            System.out.println("进入监听器");
            System.out.println("结果：" + u);
            System.out.println("异常：" + throwable);
        }, executor).exceptionally(throwable -> {
            System.out.println("进入异常通知");
            System.out.println(throwable);
            return -1;
        }).handleAsync((res, the) -> {
            if (res != null) {
                System.out.println("处理结果");
                return ++res;
            }
            if (the != null) {
                System.out.println("处理异常");
                System.out.println(the.getMessage());
                return 0;
            }
            return -1;
        }, executor);
        System.out.println("异步的值+  " + future.get());
    }

    /**
     * 花式异步三线程
     *  2个线程都。。。才。。。
     *  一条串联电路
     */
    @Test
    public void both() {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("1线程开始");
            return 1;
        }, executor);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("2线程开始");
            return 2;
        }, executor);


        //f1 f2 都执行完后执行3  没有入参和出参
        CompletableFuture<Void> future2 = f2.runAfterBothAsync(f1, () -> {
            System.out.println("3最终执行");
        }, executor);


        //f1 f2 都执行完后执行3  有入参没出参
        CompletableFuture<Void> future1 = f1.thenAcceptBothAsync(f2, (res1, res2) -> {
            System.out.println("res1:" + res1);
            System.out.println("res2:" + res2);
        }, executor);


        //f1 f2 都执行完后执行3  有入参有出参、对f1 f2结果在处理后返回新值
        CompletableFuture<Integer> future = f1.thenCombineAsync(f2, (res1, res2) -> {
            System.out.println(res1 + "+" + res2 + "=" + (res1 + res2));
            return res1 + res2;
        }, executor);

        try {
            System.out.println("future:" + future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 花式异步三线程
     * 只要f1 f2 有一个完成任务、就执行任务3
     * 一条并联电路、通一条就ok
     */
    @Test
    public void either() {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("1线程执行");
            return 1;
        }, executor);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("2线程执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        }, executor);

        // f1 f2 任一完成就执行新任务
        f1.runAfterEitherAsync(f2, () -> {
            System.out.println("3线程执行");
        }, executor);

        // f1 f2 谁先完成就拿其返回值执行新任务
        f1.acceptEitherAsync(f2, (res) -> {
            System.out.println("之前返回结果：" + res);
        }, executor);

        // f1 f2 谁先完成就拿其返回值执行新任务、并拥有返回值
        CompletableFuture<Integer> future = f1.applyToEitherAsync(f2, (res) -> {
            System.out.println("之前返回结果：" + res);
            return res * 10;
        }, executor);

        try {
            System.out.println("任务3处理后结果"+future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     *  花式多线程
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void many() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("1线程执行");
            return 1;
        }, executor);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("2线程执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        }, executor);
        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
                System.out.println("3线程执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        }, executor);
        //必须全部完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(f1, f2, f3);
        System.out.println(allOf.get());
        System.out.println(allOf.join());

        //完成一个即可
        CompletableFuture<Object> future = CompletableFuture.anyOf(f1, f2, f3);
        System.out.println(future.get());
        System.out.println(future.join());

        System.out.println("main end ");
    }

    public static void main(String[] args) {
        try {
            new ManyThreadTest().many();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

