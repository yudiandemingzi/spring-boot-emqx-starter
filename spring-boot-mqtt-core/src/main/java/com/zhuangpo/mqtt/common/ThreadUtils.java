package com.zhuangpo.mqtt.common;

import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.concurrent.*;


/**
 *  创建线程池
 * 
 * @author xub
 * @since 2023/7/31 上午11:16
 */
public class ThreadUtils {
    /**
     *  获取CPU核数
     */
    static int cpuNums = Runtime.getRuntime().availableProcessors();
    /** 线程池核心池的大小*/
    private static int corePoolSize = 10;
    /** 线程池的最大线程数*/
    private static int maximumPoolSize = cpuNums * 5;
    /** 阻塞队列容量*/
    private static int queueCapacity = 100;
    /** 活跃时间*/
    private static int keepAliveTimeSecond = 300;

    public static ExecutorService executorService = null;

    static{
        //建立10个核心线程，线程请求个数超过20，则进入队列等待
        executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSecond,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(queueCapacity),new ThreadFactoryBuilder().build());
    }
}
