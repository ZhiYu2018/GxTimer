package com.gexiang.io;

import com.gexiang.vo.GxTimeCbRequest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CbSubscriber implements Subscriber<Void> {
    private static Logger logger = LoggerFactory.getLogger(CbSubscriber.class);
    private final GxTimeCbRequest gxCbRequest;
    public CbSubscriber(GxTimeCbRequest gxCbRequest){
        this.gxCbRequest = gxCbRequest;
    }
    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(1L);
    }

    @Override
    public void onNext(Void aVoid) {
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("Call back appId:{}, jobId:{}, exceptions:{}", gxCbRequest.getAppId(),
                     gxCbRequest.getJobId(), throwable.getMessage());
    }

    @Override
    public void onComplete() {
    }
}
