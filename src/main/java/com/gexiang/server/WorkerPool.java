package com.gexiang.server;

import com.gexiang.AppStatus;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WorkerPool<T> {
    private static Logger logger = LoggerFactory.getLogger(WorkerPool.class);
    private volatile boolean stop;
    private AtomicInteger queNum;
    private int maxQueSize;
    private ConcurrentLinkedDeque<T> grpcConsumersList;
    private RateLimiter rateLimiter;
    private Semaphore semaphore;
    private Consumer<T> consumer;
    private List<Thread> threads;

    public WorkerPool(int num, int limiter, int maxQueSize, String threadName, Consumer<T> consumer){
        stop = false;
        queNum = new AtomicInteger(0);
        this.maxQueSize = maxQueSize;
        grpcConsumersList = new ConcurrentLinkedDeque<>();
        rateLimiter = RateLimiter.create(limiter);
        semaphore  = new Semaphore(0);
        threads   = new ArrayList<>();
        this.consumer = consumer;
        for(int n = 0; n < num; n++){
            Thread th = new Thread(()->{ WorkerPool.this.work(); }, String.format("%s.%d", threadName, n));
            th.start();
            threads.add(th);
        }

        logger.info("Thread {} init ok", threadName);
    }

    public void setStop(){
        stop = true;
    }

    public int push(T grpcConsumer){
        /**增加队列数**/
        int qn = queNum.decrementAndGet();
        if(qn > maxQueSize){
            queNum.decrementAndGet();
            logger.warn("Queue size {} is to many!!!", qn);
            return -1;
        }

        grpcConsumersList.offerLast(grpcConsumer);
        semaphore.release();
        return 0;
    }

    private void work(){
        logger.info("{} is running ......", Thread.currentThread().getName());
        while (!AppStatus.getAppStatus()){
            try {
                if (semaphore.tryAcquire(3, TimeUnit.SECONDS)) {
                    /**限速，执行**/
                    rateLimiter.acquire();
                    T grpcConsumer = grpcConsumersList.pollFirst();
                    if(grpcConsumer == null){
                        continue;
                    }

                    queNum.decrementAndGet();
                    consumer.accept(grpcConsumer);
                }
            }catch (Throwable t){
                logger.warn("Forward work:{}", t.getMessage());
            }
        }
    }
}
