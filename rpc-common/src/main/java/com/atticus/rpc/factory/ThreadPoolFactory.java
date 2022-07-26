package com.atticus.rpc.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建ThreadPool（线程池）的工具类
 */
@NoArgsConstructor
public class ThreadPoolFactory {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 利用Map管理多个线程池
     */
    private static Map<String, ExecutorService> threadPoolMap = new ConcurrentHashMap<>();


    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        // computeIfAbsent()：如果对应的value存在，则直接返回value；
        // 如果不存在，则使用第二个参数（函数）计算的值作为value返回，并保存为该key的value
        ExecutorService pool = threadPoolMap.computeIfAbsent(threadNamePrefix,
                k -> createThreadPool(threadNamePrefix, daemon));
        // isShutDown()：当调用shutdown()方法或shutdownNow()方法后返回为true
        // isTerminated()：1 当调用shutdown()方法，并且所有提交的任务完成后，返回为true；
        // 2 当调用shutdownNow()方法，成功停止后返回为true
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPoolMap.remove(threadNamePrefix);
            // 重新构建一个线程池并存入Map中
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPoolMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    public static void shutDownAll() {
        logger.info("关闭所有线程池......");
        // 利用parallelStream()并行关闭所有线程池
        threadPoolMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            logger.info("关闭线程池 [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                // 阻塞直到关闭请求后所有任务执行完成，或者发生超时，或者当前线程被中断（以先发生者为准）
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("关闭线程池失败");
                // 直接关闭不再等任务执行完成
                executorService.shutdownNow();
            }
        });
    }

    public static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 设置上限为100个线程的阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        // 创建线程池
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建ThreadFactory，如果threadNamePrefix不为空，则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名称的前缀，指定有意义的线程名称，方便出错时回溯
     * @param daemon           指定是否为Daemon Thread（守护线程），当所有的非守护线程结束时，
     *                         程序也就终止了，同时会杀死进程中的所有守护进程
     * @return 线程工厂
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            // 利用guava中的ThreadFactoryBuilder自定义创建线程工厂
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}
