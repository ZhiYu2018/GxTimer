package com.gexiang.server;

import com.gexiang.AppStatus;
import com.gexiang.Util.Helper;
import com.gexiang.constant.ConstValue;
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

public class ProducerConsumer <T>{
    private static Logger logger = LoggerFactory.getLogger(ProducerConsumer.class);
    private volatile boolean stop;
    private long interval;
    private ConcurrentLinkedDeque<T> grpcConsumersList;
    private AtomicInteger queNum;
    private RateLimiter rateLimiter;
    private Semaphore semaphore;
    private Consumer<T> consumer;
    private Consumer<ProducerConsumer<T>> producer;
    private List<Thread> threads;

    public ProducerConsumer(String name, long interval, Consumer<ProducerConsumer<T>> producer, Consumer<T> consumer){
        logger.info("Thread pool {} init", name);
        this.interval = interval;
        grpcConsumersList = new ConcurrentLinkedDeque<>();
        queNum = new AtomicInteger(0);
        rateLimiter = RateLimiter.create(500);
        semaphore  = new Semaphore(0);
        threads   = new ArrayList<>();
        this.producer = producer;
        this.consumer = consumer;
        threads   = new ArrayList<>();
        Thread th = new Thread(()->{ ProducerConsumer.this.producerWork(); }, String.format("%s.producer", name));
        th.start();
        threads.add(th);
        for(int n = 0; n < 3; n++){
            th = new Thread(()->{ ProducerConsumer.this.consumerWork(); }, String.format("%s.consumer,%d", name, n));
            th.start();
            threads.add(th);
        }

        logger.info("Thread pool {} init ok", name);
    }

    public void setStop(){
        stop = true;
    }

    public int addPool(List<T> list){
        grpcConsumersList.addAll(list);
        queNum.addAndGet(list.size());
        semaphore.release(threads.size() - 1);
        return ((queNum.get() < ConstValue.MAX_QUEUE_SIZE)? 0:-1);
    }

    public int getPoolSize(){
        return queNum.get();
    }

    private void producerWork(){
        while (!AppStatus.getAppStatus()){
            if(queNum.get() < ConstValue.MAX_QUEUE_SIZE){
                producer.accept(this);
            }
            /*****/
            Helper.sleepMills(interval);
        }
    }

    private void consumerWork(){
        while (!AppStatus.getAppStatus()){
            if(queNum.get() > 0){
                T t = grpcConsumersList.poll();
                if(t != null) {
                    /**减少任务数**/
                    queNum.decrementAndGet();
                    /**匀速执行**/
                    rateLimiter.acquire();
                    /**执行任务**/
                    consumer.accept(t);
                    /**有任务继续执行**/
                    continue;
                }
            }
            /**等待唤醒**/
            try{
                semaphore.tryAcquire(1, 3, TimeUnit.SECONDS);
            }catch (Throwable t){

            }
        }
    }

}
