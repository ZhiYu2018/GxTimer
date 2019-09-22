package com.gexiang.io;

import com.gexiang.repository.entity.TimerReq;
import com.gexiang.server.WorkerPool;
import com.gexiang.vo.GxResult;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoSubscriber implements Subscriber<String> {
    private static Logger logger = LoggerFactory.getLogger(IoSubscriber.class);
    private final TimerReq req;
    private final StringBuilder sb;
    private final WorkerPool<GxResult<TimerReq, Object>> workerPool;

    public IoSubscriber(TimerReq req, WorkerPool<GxResult<TimerReq, Object>> workerPool){
        this.req = req;
        this.sb  = new StringBuilder();
        this.workerPool = workerPool;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(0);
    }

    @Override
    public void onNext(String s) {
        sb.append(s);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.warn("appId:{}, jobId:{}, error:{}", req.getAppId(), req.getJobId(), throwable.getMessage());
        workerPool.push(new GxResult<>(req, throwable));
    }

    @Override
    public void onComplete() {
        String content = sb.toString();
        logger.debug("appId:{}, jobId:{}, onComplete, result {}", req.getAppId(), req.getJobId(), content);
        workerPool.push(new GxResult<>(req, content));
    }
}
