package com.gexiang.io;

import com.gexiang.repository.entity.TimerReq;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoSubscriber implements Subscriber<String> {
    private static Logger logger = LoggerFactory.getLogger(IoSubscriber.class);
    private final TimerReq req;
    private final StringBuilder sb;

    public IoSubscriber(TimerReq req){
        this.req = req;
        this.sb  = new StringBuilder();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.info("appId:{}, jobId:{}, onSubscribe:{}", req.getAppId(), req.getJobId(), subscription.getClass());
        subscription.request(0);
    }

    @Override
    public void onNext(String s) {
        sb.append(s);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.warn("appId:{}, jobId:{}, error:{}", req.getAppId(), req.getJobId(), throwable.getMessage());
    }

    @Override
    public void onComplete() {
        logger.info("appId:{}, jobId:{}, onComplete, result {}",
                   req.getAppId(), req.getJobId(), sb.toString());
    }
}
