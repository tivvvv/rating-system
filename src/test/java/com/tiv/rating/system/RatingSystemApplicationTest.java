package com.tiv.rating.system;

import com.tiv.rating.system.util.IdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RatingSystemApplicationTest {

    @Resource
    private IdGenerator idGenerator;

    private ExecutorService executorService = Executors.newFixedThreadPool(500);

    @Test
    public void testIdGenerator() throws InterruptedException {
        // 计数器
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            // 每个任务生成100个ID
            for (int i = 0; i < 100; i++) {
                long id = idGenerator.nextId("order");
                System.out.println("id = " + id);
            }
            // 任务完成,计数器减1
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        // 提交300个任务到线程池并发执行
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        // 阻塞主线程,等待所有任务执行完毕
        latch.await();
        long end = System.currentTimeMillis();
        // 输出总耗时/ms
        System.out.println("time = " + (end - begin));
    }

}